 package org.glom.web.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class OnlineGlom implements EntryPoint {
 
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private FlexTable tablesFlexTable = new FlexTable();
 	private TableNameServiceAsync tableNameSvc = GWT
 			.create(TableNameService.class);
 
 	public void onModuleLoad() {
 		// create table to hold the table names
 		tablesFlexTable.setText(0, 0, "Tables");
 
 		// add some style
 		tablesFlexTable.setCellPadding(6);
		mainPanel.add(tablesFlexTable);
 		
 		// associate the main panel with the HTML host page
 		RootPanel.get("tableList").add(mainPanel);
 		
		// Initialize the service proxy.
 		if (tableNameSvc == null) {
 			tableNameSvc = GWT.create(TableNameService.class);
 		}
 
		// Set up the callback object.
 		AsyncCallback<GlomTable[]> callback = new AsyncCallback<GlomTable[]>() {
 			public void onFailure(Throwable caught) {
				System.out.println("BOrk");
 			}
 
 			public void onSuccess(GlomTable[] result) {
 				for (int i = 0; i < result.length; i++) {
 					tablesFlexTable.setText(i + 1, 0, result[i].getName());
 				}
 			}
 
 		};
 
 		// make the call to get the names
 		tableNameSvc.getNames(callback);
 	}
 }
