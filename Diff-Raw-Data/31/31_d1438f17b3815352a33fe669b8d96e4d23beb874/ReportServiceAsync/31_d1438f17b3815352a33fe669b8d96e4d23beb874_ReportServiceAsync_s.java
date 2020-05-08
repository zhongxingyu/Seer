 package com.chuckanutbay.webapp.common.client;
 
 import java.util.List;
 import java.util.Set;
 
 import com.chuckanutbay.businessobjects.QuickbooksItem;
 import com.chuckanutbay.businessobjects.SalesOrder;
 import com.chuckanutbay.businessobjects.TrayLabel;
 import com.chuckanutbay.webapp.common.shared.InventoryTrayLabelDto;
 import com.chuckanutbay.webapp.common.shared.SalesOrderDto;
 import com.chuckanutbay.webapp.common.shared.SalesOrderLineItemDto;
 import com.chuckanutbay.webapp.common.shared.TrayLabelDto;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.RemoteService;
 
 /**
 * Since this {@link RemoteService} is defined within a base module,
 * doing {@link GWT#create(Class)} within a sub-module will yield an instance with a path relative to that base module.
 * In order to get an instances with a path that is not relative to the sub-module,
 * use {@link ServiceUtils#createPanLabelService()}.
 * This is the preferred/support method of access.
  */
 public interface ReportServiceAsync {
 	
 	void printReport(String name, String printer, AsyncCallback<Void> callback);
 	
 }
