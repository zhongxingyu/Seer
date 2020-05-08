 /**
  * 
  */
 package org.cotrix.web.common.client.resources;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.cellview.client.DataGrid;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface DataGridReportResource extends DataGrid.Resources {
 
 	public static DataGridReportResource INSTANCE = GWT.create(DataGridReportResource.class);
 	
     @Source("previewLoader.gif")
     ImageResource dataGridLoading();
 	
 	@Source({"datagrid-list.css","datagrid-report.css"})
 	DataGridReportStyle dataGridStyle();
 	
 	
 	public interface DataGridReportStyle extends DataGrid.Style {
 		
 	}
 
 }
