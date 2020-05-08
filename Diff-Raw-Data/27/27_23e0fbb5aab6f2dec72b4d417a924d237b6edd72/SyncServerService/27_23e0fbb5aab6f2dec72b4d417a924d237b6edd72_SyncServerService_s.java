 package org.sammelbox.controller.synchronization;
 
 import java.io.File;
 
 import com.jeromewagener.soutils.messaging.SoutilsObserver;
 
 public interface SyncServerService extends SoutilsObserver {
 	/** Creates a ZIP-archive which contains a reduced version of the 
 	 * Sammelbox home for synchronization with mobile devices */
	public File zipHomeForSynchronziation();
 	
 	/** Returns the current synchronization code */
	public String getSynchronizationCode();
 	/** Returns a hash of the current synchronization code */
	public String getHashedSynchronizationCode();
 	
	public void startBeaconingHashedSynchronizationCode();
	public void stopBeaconingHashedSynchronizationCode();
 	
	public void startCommunicationChannel();
	public void stopCommunicationChannel();
 	
	public void openFileTransferServer(String storageLocationAsAbsolutPath);
	public long getFileTransferProgressPercentage();
	public void stopFileTransferServer();
 	
 	public static class Default {
 		public static SyncServerService getServiceInstance() {
 			return new SyncServerServiceImpl();
 		}
 	}
 }
