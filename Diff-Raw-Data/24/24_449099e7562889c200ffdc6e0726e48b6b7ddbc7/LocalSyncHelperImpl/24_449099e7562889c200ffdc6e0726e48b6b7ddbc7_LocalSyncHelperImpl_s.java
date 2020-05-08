 package com.szas.server.gwt.client;
 
 import java.util.ArrayList;
 
 public class LocalSyncHelperImpl implements LocalSyncHelper {
 	
 	private SyncLocalService syncLocalService;
 	public LocalSyncHelperImpl(SyncLocalService syncLocalService) {
 		this.syncLocalService = syncLocalService;
 	}
 	
 	private ArrayList<ServiceHolder> serviceHolders =
 		new ArrayList<ServiceHolder>();
 
 	private static class ServiceHolder {
 		
 		public String className;
 		public LocalDAO<? extends Tuple> localDAO;
 		
 		public ServiceHolder(String className,
 				LocalDAO<?> localService) {
 			this.className = className;
 			this.localDAO = localService;
 		}
 	}
 
 
 	@Override
 	public void append(String className,
 			LocalDAO<?> localService) {
 		ServiceHolder serviceHolder =
 			new ServiceHolder(className, localService);
 		serviceHolders.add(serviceHolder);
 	}
 
 	@Override
 	public void sync() {
 		ArrayList<ToSyncElementsHolder> toSyncElementsHolders = 
 			new ArrayList<ToSyncElementsHolder>();
 		
 		for (ServiceHolder serviceHolder : serviceHolders) {
 			LocalDAO<?> localDAO = serviceHolder.localDAO;
 			ToSyncElementsHolder toSyncElementsHolder = 
 				new ToSyncElementsHolder();
 			toSyncElementsHolder.elementsToSync = 
 				localDAO.getUnknownElementsToSync();
 			toSyncElementsHolder.className =
 				serviceHolder.className;
 			toSyncElementsHolder.lastTimestamp = 
 				localDAO.getLastTimestamp();
 		}
		getSyncLocalService().sync(toSyncElementsHolders, new SyncLocalServiceResult() {
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				
 				fail(caught);
 			}
 
 			@Override
 			public void onSuccess(ArrayList<SyncedElementsHolder> result) {
 				success(result);
 			}
 		});
 	}
 
 	protected void fail(Throwable caught) {
 		// TODO do something if connection fails
 	}
 
 	protected void success(ArrayList<SyncedElementsHolder> result) {
 		// TODO receive data from server
 		// TODO update localServices
 		for (SyncedElementsHolder syncedElementsHolder : result) {
 			LocalDAO<?> localDAO = findLocalDAO(syncedElementsHolder.className);
 			localDAO.setSyncedUnknownElements(syncedElementsHolder.syncedElements);
 			localDAO.setLastTimestamp(syncedElementsHolder.syncTimestamp);
 		}
 		
 	}
 
 	private LocalDAO<?> findLocalDAO(String className) {
 		for (ServiceHolder serviceHolder : serviceHolders) {
 			if (!serviceHolder.className.equals(className))
 				continue;
 			return serviceHolder.localDAO;
 		}
 		// TODO Throw error
 		return null;
 	}
 
 	public void setSyncLocalService(SyncLocalService syncLocalService) {
 		this.syncLocalService = syncLocalService;
 	}
 
 	public SyncLocalService getSyncLocalService() {
 		return syncLocalService;
 	}
 
 }
