 package org.sammelbox.controller.synchronization;
 
 import java.io.File;
 
 import com.jeromewagener.soutils.messaging.SoutilsObserver;
 
// TODO fully comment
 public interface SyncServerService extends SoutilsObserver {
 	/** Creates a ZIP-archive which contains a reduced version of the 
 	 * Sammelbox home for synchronization with mobile devices */
	File zipHomeForSynchronziation();
 	
 	/** Returns the current synchronization code */
	String getSynchronizationCode();
 	/** Returns a hash of the current synchronization code */
	String getHashedSynchronizationCode();
 	
	void startBeaconingHashedSynchronizationCode();
	void stopBeaconingHashedSynchronizationCode();
 	
	void startCommunicationChannel();
	void stopCommunicationChannel();
 	
	void openFileTransferServer(String storageLocationAsAbsolutPath);
	long getFileTransferProgressPercentage();
	void stopFileTransferServer();
 	
 	public static class Default {
 		public static SyncServerService getServiceInstance() {
 			return new SyncServerServiceImpl();
 		}
 	}
 }
