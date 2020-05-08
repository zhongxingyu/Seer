 package org.peerbox.fileshare;
 
 
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.URI;
 import java.util.HashMap;
 
 import org.peerbox.fileshare.messages.FileRequest;
 import org.peerbox.fileshare.messages.FileResponse;
 import org.peerbox.fileshare.messages.Request;
 import org.peerbox.fileshare.messages.Response;
 import org.peerbox.fileshare.messages.SharedDirectoryRequest;
 import org.peerbox.fileshare.messages.SharedDirectoryResponse;
 import org.peerbox.friend.Friend;
 import org.peerbox.network.http.HttpClient;
 import org.peerbox.network.http.HttpClientListener;
 import org.peerbox.network.http.HttpStaticFileServer;
 import org.peerbox.rpc.RPCEvent;
 import org.peerbox.rpc.RPCHandler;
 import org.peerbox.rpc.RPCResponseListener;
 
 import com.google.gson.Gson;
 
 
 public class FileShareManager {
 	
 	protected File mySharedDirectory;
 	protected HashMap<String, FileRequestInfo> requestIDtoFileRequest; 
 	protected final RPCHandler rpcHandler;
 	protected final String rpcServiceName;
 	protected File myDownloadDirectory;
 	
 	public FileShareManager(RPCHandler rpcHandler) {
 		this(rpcHandler, null, null);
 	}
 	
 	public FileShareManager(RPCHandler rpcHandler, String sharedPathName, String downloadPath) {
 		mySharedDirectory = sharedPathName != null ? new File(sharedPathName) : null;
 		myDownloadDirectory = downloadPath != null ? new File(downloadPath) : null;
 		requestIDtoFileRequest = new HashMap<String, FileRequestInfo>();
 		rpcServiceName = "fileshare";
 		this.rpcHandler = rpcHandler;
 		new HttpStaticFileServer(30000, this);
 		rpcHandler.registerServiceListener(rpcServiceName, new FileshareRequestListener(this));
 	}
 	
 	public RPCHandler getRPC() {
 		return rpcHandler;
 	}
 	
 	//TODO: handle OS specific slashes
 	public FileInfo[] getSharedContents(String relativePath) {
 		if (mySharedDirectory == null) {
 			return null;
 		}
 		File requestDir = null;
 		if(!relativePath.isEmpty()) {
 			//TODO: make sure that path exists and is a directory!!
 			if(File.separatorChar != '/') {
				relativePath = relativePath.replace("/", File.separator);
 			}
 			requestDir = new File(mySharedDirectory.getAbsolutePath().concat(File.separator+relativePath));
 		}
 		else {
 			//System.out.println("requesting a file");
 			requestDir = new File(mySharedDirectory.getAbsolutePath());
 		}
 		try {
 			if(requestDir.getCanonicalPath().startsWith(mySharedDirectory.getAbsolutePath()) && requestDir.isDirectory()) {
 				FileFilter f = null;
 				File[] contents = requestDir.listFiles(f);
 				FileInfo[] contentInfo = new FileInfo[contents.length];
 				for(int i = 0; i<contents.length; i++) {
 					String name = contents[i].getName();
 					String fileOrDir = null;
 					if(contents[i].isFile()) {
 						fileOrDir = "file";
 					}
 					else {
 						fileOrDir = "directory";
 					}
 					long fileSize = contents[i].length();
 					
 					contentInfo[i] = new FileInfo(name, fileOrDir, fileSize);
 				}
 				return contentInfo;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	protected <T extends Response> void sendRequestRPC(Friend targetFriend, Request request, final Class<T> responseClass, final ResponseListener<T> callback) {
 		final Gson gson = new Gson();
 		String requestData = gson.toJson(request);
 		getRPC().sendRequest(targetFriend.getNetworkAddress(), "fileshare", requestData, new RPCResponseListener() {
 			public void onResponseReceived(RPCEvent event) {
 				try {
 					callback.onResponseReceived((T) gson.fromJson(event.getDataString(), responseClass));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			@Override
 			public void onTimeout() {
 				callback.onFailure();
 			}
 		});
 	}
 	
 	public void getSharedDirectory(String relativePath, Friend friend, ResponseListener<SharedDirectoryResponse> response) {
 		//System.out.println("relativePath is:"+relativePath);
 		SharedDirectoryRequest request = new SharedDirectoryRequest(relativePath);
 		this.sendRequestRPC(friend, request, SharedDirectoryResponse.class, response);
 	}
 
 	public void getFile(String relativePath, Friend tofriend, String file, ResponseListener<FileResponse> response) {
 		//TODO: figure out how to get friend sending request
 		FileRequest request = new FileRequest(file, relativePath);
 		this.sendRequestRPC(tofriend, request, FileResponse.class, response);
 	}
 	public boolean setRequestIDtoFileRequest(String relativePath, String requestID, final String filename, long expiration) {
 		if (filename == null || requestID == null) {
 			return false;
 		}
 		if (mySharedDirectory == null) {
 			return false;
 		}
 		if(File.separatorChar != '/') {
			relativePath = relativePath.replace("/", File.separator);
 		}
 		File requestDir = new File(mySharedDirectory.getAbsolutePath().concat(relativePath));
 		File[] files = requestDir.listFiles(new FilenameFilter() {
 			public boolean accept(File arg0, String arg1) {
 				if(filename.equals(arg1))
 					return true;
 				else
 					return false;
 			}
 			
 		});
		if(files != null && files.length == 1) {
 			try {
 				FileRequestInfo fileInfo = new FileRequestInfo(expiration, files[0].getCanonicalPath());
 				requestIDtoFileRequest.put(requestID, fileInfo);
 				return true;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			return false;
 		}
 		return false;
 	}
 	public String getFilePath(URI uri) {
 		String requestID = uri.getPath();
 		if(requestID.length() > 1) {
 			requestID = requestID.substring(1);
 			FileRequestInfo fri = requestIDtoFileRequest.get(requestID);
 			if(fri != null) {
 				//System.out.println("expiration:"+fri.getExpiration()+" current time:"+System.currentTimeMillis()/1000);
 				if(fri.getExpiration() > (System.currentTimeMillis()/1000)){
 					return requestIDtoFileRequest.get(requestID).getFilePath();
 				}
 			}
 		}
 		return null;
 
 	}
 	
 	/**
 	 * Sets the shared file directory.
 	 * If filePath is null, removes the file share.
 	 * If filePath is invalid, keeps existing share.
 	 * Resets any pending file requests if file share is changed.
 	 * @param filePath
 	 * @return
 	 */
 	public boolean setSharedFilePath(String filePath) {
 		if (filePath == null) {
 			mySharedDirectory = null;
 			requestIDtoFileRequest.clear();
 			return true;
 		}
 		
 		//TODO: make sure filepath will exist!!!
 		File desiredSharedDirectory = new File(filePath);
 		if(!desiredSharedDirectory.isDirectory()) {
 			return false;
 		}
 		else {
 			mySharedDirectory = desiredSharedDirectory;
 		}
 		requestIDtoFileRequest.clear();
 		return true;
 	}
 	public boolean setDownloadFilePath(String filePath) {
 		if (filePath == null) {
 			myDownloadDirectory = null;
 			return true;
 		}
 		
 		//TODO: make sure filepath will exist!!!
 		File desiredSharedDirectory = new File(filePath);
 		if(!desiredSharedDirectory.isDirectory()) {
 			return false;
 		}
 		else {
 			myDownloadDirectory = desiredSharedDirectory;
 		}
 		return true;
 	}
 
 	public void download(URI fileLocURI, String name, HttpClientListener httpClientListener) {
 		if (myDownloadDirectory == null) {
 			System.out.println("No download directory specified");
 			return;
 		}
 		
 		File downloadPath = new File(myDownloadDirectory.getAbsolutePath().concat(File.separator+name));
 		new HttpClient(fileLocURI, downloadPath, httpClientListener);
 	}
 	
 }
