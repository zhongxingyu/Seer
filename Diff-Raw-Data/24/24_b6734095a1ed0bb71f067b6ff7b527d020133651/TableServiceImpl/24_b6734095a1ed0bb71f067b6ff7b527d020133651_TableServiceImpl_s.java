 package com.clark.demos.server;
 
 
 
 import com.clark.demos.client.TableService;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.google.visualization.datasource.base.TypeMismatchException;
 import com.google.visualization.datasource.datatable.ColumnDescription;
 import com.google.visualization.datasource.datatable.DataTable;
 import com.google.visualization.datasource.datatable.value.ValueType;
 import com.google.visualization.datasource.render.JsonRenderer;
 
 @SuppressWarnings("serial")
 public class TableServiceImpl extends RemoteServiceServlet implements
 		TableService {
 
 	@Override
	public String getTable() {		
 	    DataTable data = new DataTable();
 	    data.addColumn( new ColumnDescription("Task", ValueType.TEXT, "Task") );
 	    data.addColumn( new ColumnDescription("Stemming", ValueType.NUMBER, "Stemming") );
 	    data.addColumn( new ColumnDescription("NoStemming", ValueType.NUMBER, "No Stemming") );
 	    try {
			data.addRowFromValues( "Fire", 1.0, 0.8 );
		    data.addRowFromValues( "Flood", 0.5, 0.65 );			
 		} catch (TypeMismatchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return "";
 		}
 	    return JsonRenderer.renderDataTable(data, true, false, false).toString();
 	    }
 
 }
