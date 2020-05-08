 package android.HLMPConnect.Managers;
 
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Hashtable;
 import android.os.Handler;
 import android.util.Log;
 
 import hlmp.CommLayer.Communication;
 import hlmp.CommLayer.NetUser;
 import hlmp.CommLayer.Observers.RemoveUserEventObserverI;
 import hlmp.SubProtocol.FileTransfer.FileInformation;
 import hlmp.SubProtocol.FileTransfer.FileInformationList;
 import hlmp.SubProtocol.FileTransfer.FileTransferProtocol;
 import hlmp.SubProtocol.FileTransfer.ControlI.FileHandlerI;
 import hlmp.SubProtocol.FileTransfer.ControlI.FileListHandlerI;
 
 import android.HLMPConnect.FilesActivity;
 import android.HLMPConnect.HLMPApplication;
 import android.HLMPConnect.FileTransfer.CommunityFilesActivity;
 import android.HLMPConnect.FileTransfer.DownloadFilesActivity;
 import android.HLMPConnect.FileTransfer.StateFilesActivity;
 
 
 public class FilesManager implements FileHandlerI, FileListHandlerI, RemoveUserEventObserverI {
 
 	private static final String MSG_TAG = "HLMP -> FilesManager";
 
 	private static final String START	= "START";
 	private static final String END		= "END";
 	private static final String SIZE	= "SIZE";
 	
 	protected Communication communication;
 	protected FileTransferProtocol fileTransferProtocol;
 	protected Handler communityFilesHandler;
 	protected Handler downloadFilesHandler;
 	protected Handler stateFilesHandler;
 	protected Hashtable<InetAddress, FileInformationList> communityFiles;
 	protected HashMap<String, HashMap<String, String>> stateFiles;
 	protected Hashtable<String, Hashtable<String, Long>> downloadTimes;
 	protected FilesActivity filesActivity;
 	protected HLMPApplication application;
 
 	
 	
 	public FilesManager(HLMPApplication application) {
 		this.application = application;
 		this.communityFiles = new Hashtable<InetAddress, FileInformationList>();
 		this.stateFiles = new HashMap<String, HashMap<String, String>>();
 		this.downloadTimes = new Hashtable<String, Hashtable<String, Long>>();
 	}
 
 
 	public Hashtable<InetAddress, FileInformationList> getCommunityFiles() {
 		return communityFiles;
 	}
 
 	public HashMap<String, HashMap<String, String>> getStateFiles() {
 		return this.stateFiles;
 	}
 	
 	public FilesActivity getFilesActivity() {
 		return this.filesActivity;
 	}
 	
 	public void setCommunityFilesHandler(Handler communityFilesHandler) {
 		this.communityFilesHandler = communityFilesHandler;
 	}
 	
 	public void setDownloadFilesHandler(Handler downloadFilesHandler) {
 		this.downloadFilesHandler = downloadFilesHandler;
 	}
 
 	public void setStateFilesHandler(Handler stateFilesHandler) {
 		this.stateFilesHandler = stateFilesHandler;
 	}
 	
 	public void setFileTranfersProtocol(FileTransferProtocol fileTransferProtocol) {
 		this.fileTransferProtocol = fileTransferProtocol;
 	}
 
 	public void setCommunication(Communication communication) {
 		this.communication = communication;
 	}
 	
 	public void setFilesActivity(FilesActivity filesActivity) {
 		this.filesActivity = filesActivity;
 	}
 
 
 	private void writeTrafficStats(String fileHandlerId) {
 		Hashtable<String, Long> fileMap = this.downloadTimes.get(fileHandlerId);
 		double seconds = (fileMap.get(END)- fileMap.get(START))/1000.0;
 		double size_kb = fileMap.get(SIZE)/1024.0;
 		this.application.saveTimeRecord(seconds, size_kb);
 	}
 	
 	private void addFileToStateFiles(String fileHandlerId, String fileName, String size) {
 		long size_long = 0;
 		if (size != null) {
 			size_long = Long.valueOf(size).longValue();
 		}
 		size = "" + size_long/1024 + " KB";
 		String progress = "0";
 		
 		HashMap<String, String> fileMap = new HashMap<String, String>();
 		fileMap.put(StateFilesActivity.ID, fileHandlerId);
 		fileMap.put(StateFilesActivity.FILENAME, fileName);
 		fileMap.put(StateFilesActivity.SIZE, size);
 		fileMap.put(StateFilesActivity.PROGRESS, progress);
 		
 		this.stateFiles.put(fileHandlerId, fileMap);
 	}
 	
 	public void clearData() {
 		communityFiles.clear();
 		stateFiles.clear();
 		downloadTimes.clear();
 	}
 	// FileTransferProtocol Manager API
 	
 	public void addFileInformationToFileData(FileInformation fileInformation) {
 		fileTransferProtocol.getFileData().addFile(fileInformation);
 	}
 	
 	public void sendFileList() {
 		NetUser[] netUserList = this.communication.getNetUserList().userListToArray();
 		for (NetUser netUser : netUserList) {
 			this.fileTransferProtocol.sendFileList(netUser);
 		}
 	}
 	
 	public void sendFileRequest(NetUser netUser, FileInformation fileInformation) {
 		this.fileTransferProtocol.sendFileRequest(netUser, fileInformation);
 	}
 
 	//	// Implements HLMP RemoveUserEventObserverI
 	
 	public void removeUserEventUpdate(NetUser netUser) {
 		this.removeFileList(netUser);
 	}
 	
 	
 	
 	// Implements HLMP FileListHandler
 	
 	public synchronized void addFileList(NetUser netUser, FileInformationList fileList) {
 		if (fileList.toArray().length != 0) {
 			Log.d(MSG_TAG, "addFileList user:" + netUser.getName());
 			this.communityFiles.put(netUser.getIp(), fileList);
 			if (this.communityFilesHandler != null) {
 				this.communityFilesHandler.obtainMessage(CommunityFilesActivity.UPDATE_USERS_LIST).sendToTarget();
 			}
 		}
 	}
 
 	public void removeFileList(NetUser netUser) {
 		Log.d(MSG_TAG, "removeFileList user:" + netUser.getName());
 		this.communityFiles.remove(netUser.getIp());
 		if (this.communityFilesHandler != null) {
 			this.communityFilesHandler.obtainMessage(CommunityFilesActivity.UPDATE_USERS_LIST).sendToTarget();
 		}
 	}
 
 	
 	// Implements HLMP FiletHandler
 	
 	//	// Download
 	
 	public void downloadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
 		String size = null;
 		for (FileInformation fileInformation : communityFiles.get(netUser.getIp()).toArray()) {
 			if (fileInformation.getName() == fileName) {
 				size = "" + fileInformation.getSize();
 			}
 		}
 		
 		this.addFileToStateFiles(fileHandlerId, "(DOWNLOAD) " + fileName, size);
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(StateFilesActivity.ADD_DOWNLOAD).sendToTarget();
 		}
 		
 		Hashtable<String, Long> fileMap = new Hashtable<String, Long>();
 		fileMap.put(SIZE, Long.valueOf(size));
 		this.downloadTimes.put(fileHandlerId, fileMap);
 	}
 
 	public void downloadFileOpened(String fileHandlerId) {
		long now = System.currentTimeMillis();
		this.downloadTimes.get(fileHandlerId).put(START, Long.valueOf(now));
 	}
 
 	public void downloadFileTransfer(String fileHandlerId, int percent) {
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(
 					StateFilesActivity.UPDATE_DOWNLOAD_PERCENT,
 					percent,
 					0,
 					fileHandlerId).sendToTarget();
 		}
 	}
 
 	public void downloadFileComplete(String fileHandlerId, String path) {
 		long now = System.currentTimeMillis();
 		this.downloadTimes.get(fileHandlerId).put(END, Long.valueOf(now));
 		this.writeTrafficStats(fileHandlerId);
 		
 		if (this.downloadFilesHandler != null) {
 			this.downloadFilesHandler.obtainMessage(DownloadFilesActivity.UPDATE_USERS_LIST).sendToTarget();
 		}
 		
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(
 					StateFilesActivity.UPDATE_DOWNLOAD_PERCENT,
 					100,
 					0,
 					fileHandlerId).sendToTarget();
 		}
 	}
 
 	public void downloadFileFailed(String fileHandlerId) {
 		// TODO: Notificar en el state que hubo un problema
 	}
 
 	//	// Upload
 	
 	public void uploadFileQueued(NetUser netUser, String fileHandlerId, String fileName) {
 		String size = null;
 		for (FileInformation fileInformation : fileTransferProtocol.getFileData().getFileList().toArray()) {
 			if (fileInformation.getName() == fileName) {
 				size = "" + fileInformation.getSize();
 			}
 		}
 		this.addFileToStateFiles(fileHandlerId, "(TRANSFER) " + fileName, size);
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(StateFilesActivity.ADD_UPLOAD).sendToTarget();
 		}
 	}
 
 	public void uploadFileOpened(String fileHandlerId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void uploadFileTransfer(String fileHandlerId, int percent) {
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(
 					StateFilesActivity.UPDATE_UPLOAD_PERCENT,
 					percent,
 					0,
 					fileHandlerId).sendToTarget();
 		}
 	}
 
 	public void uploadFileComplete(String fileHandlerId) {
 		if (this.stateFilesHandler != null) {
 			this.stateFilesHandler.obtainMessage(
 					StateFilesActivity.UPDATE_UPLOAD_PERCENT,
 					100,
 					0,
 					fileHandlerId).sendToTarget();
 		}
 	}
 
 	public void uploadFileFailed(String fileHandlerId) {
 		// TODO: Notificar en la lista de estados que no se puede seguir subiendo
 	}
 
 
 }
