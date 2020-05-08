 package com.chuckanutbay.webapp.common.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 
 /**
  * Utilities for working with {@link RemoteService} types in this base module.
  * These create* methods are preferred over {@link GWT#create(Class)} within a sub-module
  * as they will will yield an instance with a path that is not relative to the sub-module.
  * This is the preferred/support method of access.
  */
 public class ServiceUtils {
 
 	public static InventoryItemServiceAsync createInventoryItemService() {
		return createServiceHelper(InventoryItemService.class, "InventoryItemService");
 	}
 	public static InventoryLotServiceAsync createInventoryLotService() {
		return createServiceHelper(InventoryLotService.class, "InventoryLotService");
 	}
 
 	private static <T> T createServiceHelper(Class<? extends RemoteService> classLiteral, String relativePath) {
		T service = GWT.create(classLiteral);
 		((ServiceDefTarget)service).setServiceEntryPoint("/common/" + relativePath);
 		return service;
 	}

 }
