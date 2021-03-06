/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.client.rdr;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;

class ContactCell extends AbstractCell<EntityInfo> {

	public ContactCell(ImageResource image) {
	}

	@Override
	public void render(Context context, EntityInfo value, SafeHtmlBuilder sb) {
		// Value can be null, so do a null check..
		if (value == null) {
			return;
		}

//		sb.appendHtmlConstant("<table><tr><td style='font-size:95%;'>");
		sb.appendHtmlConstant("<a class='gwt-Anchor' href='javascript:'>");
		sb.appendEscaped( value.getName() );
		sb.appendHtmlConstant("</a>");
//		sb.appendHtmlConstant("</td></tr></table>");
	}
}

public class RDRModule implements EntryPoint {

	DockPanel						dock = new DockPanel();

	List<JSONObject>				listEntities;
	ListDataProvider<JSONObject> 	dataProvider;
	SimplePager 					pager = new SimplePager();

	CellTable<JSONObject>			cellList;
	
	VerticalPanel					page = new VerticalPanel();
	HorizontalPanel					mainView = new HorizontalPanel();
	VerticalPanel					leftPanel = new VerticalPanel();
	VerticalPanel					rightPanel = new VerticalPanel();

	EntityDataBox					ppg = null;

	public RDRModule() {
		
	}

	public void onModuleLoad() {
		
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				listEntities = new ArrayList<>();
				for (int i = 0; i < response.isArray().size(); ++i) {
					if (!response.isArray().get(i).isObject().get("name").isString().stringValue().equals("-")) listEntities.add(response.isArray().get(i).isObject());
				}

				cellList = new CellTable<JSONObject>(15, (Resources) GWT.create(TableResources.class));
				cellList.setStyleName("list");
				cellList.setPageSize(30);
				cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
				cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

				final SingleSelectionModel<JSONObject> selectionModel = new SingleSelectionModel<JSONObject>();
			    cellList.setSelectionModel(selectionModel);
			    selectionModel.addSelectionChangeHandler(new Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent arg0) {
						ppg.setSelectedEntity( selectionModel.getSelectedObject().get("name").isString().stringValue() );
					}
			    });
			    
			    Column<JSONObject, String> t = new Column<JSONObject, String>(new TextCell()) {
					@Override
					public String getValue(JSONObject arg0) {
						return arg0.get("name").isString().stringValue();
					}
				};
				
				Column<JSONObject, String> t2 = new Column<JSONObject, String>(new TextCell()) {
					@Override
					public String getValue(JSONObject arg0) {
						return arg0.get("layer").isString().stringValue();
					}
				};

			    cellList.addColumn(t, "Entity");
			    cellList.addColumn(t2, "Layer");
			    cellList.setWidth("100%");
			    
			    if (listEntities.size() > 0) cellList.setRowData(0, listEntities);
				else {
					listEntities.add(new JSONObject());
					cellList.setRowData(0, listEntities);
					listEntities.remove(0);
				}
				cellList.setStyleName("table");
				
				dataProvider = new ListDataProvider<JSONObject>();
				dataProvider.addDataDisplay( cellList );
				
				for( int i = 0; i < listEntities.size(); i++ ) {
					dataProvider.getList().add( listEntities.get(i) );
				}
				
				pager = new SimplePager();
			    pager.setDisplay( cellList );
			    
				ppg = new EntityDataBox();
				
				dock.setWidth( "100%" );
				dock.add(cellList,DockPanel.CENTER);
				dock.add( ppg.asWidget(), DockPanel.EAST );
				dock.setCellWidth( ppg.asWidget(), "60%" );
				
				mainView.setStyleName("mainViewLayer");
				//mainView.setWidth("100%");
				leftPanel.setStyleName("leftPanelLayer");
				leftPanel.setWidth("400px");
				//leftPanel.setHeight("100%");
				rightPanel.setStyleName("rightPanelLayer");
				page.setWidth("100%");
				
				Label title = new Label("Risk Data Repository");
				title.setStyleName("title");
				page.add(title);
				
				leftPanel.add(cellList);
				leftPanel.add(pager);
				rightPanel.add(ppg);
				rightPanel.setWidth("90%");
				mainView.add(leftPanel);
				mainView.add(rightPanel);
				
				page.add(mainView);

				RootPanel.get().add( page );
			}
		});

	}
	
}
