 package com.acme.main.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.smartgwt.client.core.KeyIdentifier;
 import com.smartgwt.client.data.RestDataSource;
 import com.smartgwt.client.data.fields.DataSourceIntegerField;
 import com.smartgwt.client.data.fields.DataSourceTextField;
 import com.smartgwt.client.types.DSDataFormat;
 import com.smartgwt.client.util.KeyCallback;
 import com.smartgwt.client.util.Page;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 /**
 * This example shows a {@link ListGrid} receiving data from a
  * {@link RestDataSource} hacked just to let Jetty (in Dev Mode) serve static
  * json paged responses.
  * 
  * @author Davide Cavestro
  * 
  */
 public class Main implements EntryPoint {
 
     @Override
     public void onModuleLoad() {
 
         final RestDataSource dataSource = new RestDataSource() {
             @Override
             protected Object transformRequest(
                     com.smartgwt.client.data.DSRequest dsRequest) {
                 final Integer startRow = dsRequest
                         .getAttributeAsInt("startRow");
                 if (startRow != 0) {
                     final int pagedStartRow = (startRow / 25) * 25;
                    // just to get a valid response for paging (serving fake
                    // responses only for fake requests with startRow multiple
                    // 25)
                     dsRequest.setActionURL("gwt/data_" + pagedStartRow + ".js");
                 }
                 return dsRequest.getData();
             }
         };
         dataSource.setDataFormat(DSDataFormat.JSON);
         dataSource.setFetchDataURL("gwt/data.js");
 
         {// datasource fields initialization
             final DataSourceIntegerField keyField = new DataSourceIntegerField(
                     "id", "ID");
             keyField.setPrimaryKey(true);
             final DataSourceTextField nameField = new DataSourceTextField(
                     "name", "Name");
 
             dataSource.setFields(keyField, nameField);
         }
 
         // UI initialization
         final VLayout layout = new VLayout();
         layout.setMembersMargin(5);
         layout.setMargin(20);
         layout.setWidth(400);
         layout.setHeight(400);
 
         final ListGrid listGrid = new ListGrid();
         listGrid.setWidth100();
         listGrid.setHeight100();
         listGrid.setDataSource(dataSource);
         listGrid.setDataPageSize(25);
        listGrid.setDrawAheadRatio(1);// do not prefetch data
        listGrid.setDrawAllMaxCells(0);// avoid drawing all rows at startup
         listGrid.setAutoFetchData(true);
 
         layout.addMember(listGrid);
 
         final KeyIdentifier debugKey = new KeyIdentifier();
         debugKey.setCtrlKey(true);
         debugKey.setShiftKey(true);
         debugKey.setKeyName("D");
 
         Page.registerKey(debugKey, new KeyCallback() {
             @Override
             public void execute(final String keyName) {
                 SC.showConsole();
             }
         });
 
         layout.show();
     }
 
 }
