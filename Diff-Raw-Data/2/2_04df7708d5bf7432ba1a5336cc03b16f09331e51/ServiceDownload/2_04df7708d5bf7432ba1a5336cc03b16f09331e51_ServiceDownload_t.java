 /*
  * ServiceDownload, part of Aptoide
  * Copyright (C) 2012 Duarte Silveira
  * duarte.silveira@caixamagica.pt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 package cm.aptoide.pt2.services;
 
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeoutException;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Service;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import cm.aptoide.pt2.exceptions.AptoideExceptionDownload;
 import cm.aptoide.pt2.exceptions.AptoideExceptionNotFound;
 import cm.aptoide.pt2.views.ViewCache;
 import cm.aptoide.pt2.views.ViewDownload;
 import cm.aptoide.pt2.views.ViewLogin;
 
 /**
  * ServiceDownload
  *
  * @author dsilveira
  *
  */
 public class ServiceDownload extends Service {
 
 	// This is the object that receives interactions from service clients.
     private final IBinder binder = new ServiceDownloadBinder();
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class ServiceDownloadBinder extends Binder {
     	public ServiceDownload getService() {
             return ServiceDownload.this;
         }
     }
     
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.d("Aptoide-ServiceDownload", "Bound");
 		return binder;
 	}
 	
 	private static Handler progressUpdateHandler = new Handler() {
 		@Override
         public void handleMessage(Message msg) {
         	//update msg.what object id
         }
 	};
 
 	private DownloadManager downloadManager;
 
     private class DownloadManager{
     	private ExecutorService installedColectorsPool;
     	
     	public DownloadManager(){
     		installedColectorsPool = Executors.newSingleThreadExecutor();
     	}
     	
     	public void downloadApk(ViewDownload download, ViewCache cache){
     		downloadApk(download, cache, null);
     	}
     	
     	public void downloadApk(ViewDownload download, ViewCache cache, ViewLogin login){
         	try {
 				installedColectorsPool.execute(new DownloadApk(download, cache, login));
 			} catch (Exception e) { }
         }
     	
     	private class DownloadApk implements Runnable{
 
     		ViewDownload download;
     		ViewCache cache;
     		ViewLogin login;
     		
 			public DownloadApk(ViewDownload download, ViewCache cache, ViewLogin login) {
 				this.download = download;
 				this.cache = cache;
 				this.login = login;
 			}
     		
 			@Override
 			public void run() {
 //	    		Log.d("Aptoide-ManagerDownloads", "apk download: "+download.getCache());
 	    		if(!cache.isCached() || !cache.checkMd5()){
 	    			try {
 	    				download(download, cache, login);
 	    			} catch (Exception e) {
 	    				try {
 	    					download(download, cache, login);
 	    				} catch (Exception e2) {
 	    					download(download, cache, login);
 	    				}
 	    			}
 	    		}
 	    		Log.d("Aptoide-ManagerDownloads", "apk download: "+download.getRemotePath());
 			}
     		
     	}
     }
 	
     
     
     @Override
     public void onCreate() {
 		downloadManager = new DownloadManager();
     	super.onCreate();
     }
     
     
 //	private 
 	
 
 	
 //	private String getUserAgentString(){
 //		ViewClientStatistics clientStatistics = getClientStatistics();
 //		return String.format(Constants.USER_AGENT_FORMAT
 //				, clientStatistics.getAptoideVersionNameInUse(), clientStatistics.getScreenDimensions().getFormattedString()
 //				, clientStatistics.getAptoideClientUUID(), getServerUsername());
 //	}
 	
 
 	
 	public void installApp(ViewCache apk){
 //		if(isAppScheduledToInstall(appHashid)){
 //			unscheduleInstallApp(appHashid);
 //		}
 		Intent install = new Intent(Intent.ACTION_VIEW);
 		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		install.setDataAndType(Uri.fromFile(apk.getFile()),"application/vnd.android.package-archive");
 		Log.d("Aptoide", "Installing app: "+apk.getLocalPath());
 		startActivity(install);
 	}
 	
 	public void downloadApk(ViewDownload download, ViewCache cache){
 		downloadManager.downloadApk(download, cache);
 	}
 	
 	public void downloadApk(ViewDownload download, ViewCache cache, ViewLogin login){
 		downloadManager.downloadApk(download, cache, login);
 	}
 	
 	private void download(ViewDownload download, ViewCache cache, ViewLogin login){
 		boolean overwriteCache = false;
 		boolean resuming = false;
 		boolean isLoginRequired = (login != null);
 		
 		String localPath = cache.getLocalPath();
 		String remotePath = download.getRemotePath();
 		long targetBytes;
 		
 		FileOutputStream fileOutputStream = null;
 		
 		try{
 			fileOutputStream = new FileOutputStream(localPath, !overwriteCache);
 			DefaultHttpClient httpClient = new DefaultHttpClient();
 			HttpGet httpGet = new HttpGet(remotePath);
 			Log.d("Aptoide-download","downloading from: "+remotePath+" to: "+localPath);
 //			Log.d("Aptoide-download","downloading with: "+getUserAgentString());
 			Log.d("Aptoide-download","downloading mode private: "+isLoginRequired);
 
 //			httpGet.setHeader("User-Agent", getUserAgentString());
 			
 			long resumeLength = cache.getFileLength();
 			if(!overwriteCache){
 				if(resumeLength > 0){
 					resuming = true;
 				}
 				Log.d("Aptoide-download","downloading from [bytes]: "+resumeLength);
 				httpGet.setHeader("Range", "bytes="+resumeLength+"-");
 				download.setProgress(resumeLength);
 			}
 
 			if(isLoginRequired){	
 				URL url = new URL(remotePath);
 				httpClient.getCredentialsProvider().setCredentials(
 						new AuthScope(url.getHost(), url.getPort()),
 						new UsernamePasswordCredentials(login.getUsername(), login.getPassword()));
 			}
 
 			HttpResponse httpResponse = httpClient.execute(httpGet);
 			if(httpResponse == null){
 				Log.d("Aptoide-ManagerDownloads","Problem in network... retry...");	
 				httpResponse = httpClient.execute(httpGet);
 				if(httpResponse == null){
 					Log.d("Aptoide-ManagerDownloads","Major network exception... Exiting!");
 					/*msg_al.arg1= 1;
 						 download_error_handler.sendMessage(msg_al);*/
 					if(!resuming){
 						cache.clearCache();
 					}
 					throw new TimeoutException();
 				}
 			}
 
 			if(httpResponse.getStatusLine().getStatusCode() == 401){
 				Log.d("Aptoide-ManagerDownloads","401 Timed out!");
 				fileOutputStream.close();
 				if(!resuming){
 					cache.clearCache();
 				}
 				throw new TimeoutException();
 			}else if(httpResponse.getStatusLine().getStatusCode() == 404){
 				fileOutputStream.close();
 				if(!resuming){
 					cache.clearCache();
 				}
 				throw new AptoideExceptionNotFound("404 Not found!");
 			}else{
 				
 //				Log.d("Aptoide-ManagerDownloads", "Download target size: "+notification.getProgressCompletionTarget());
 				
 //				if(download.isSizeKnown()){
 //					targetBytes = download.getSize()*Constants.KBYTES_TO_BYTES;	//TODO check if server sends kbytes or bytes
 //					notification.setProgressCompletionTarget(targetBytes);
 //				}else{
 
				if(httpResponse.containsHeader("Content-Length")){
 					targetBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
 					Log.d("Aptoide-ManagerDownloads","Download targetBytes: "+targetBytes);
 					download.setProgressTarget(targetBytes);
 				}
 //				}
 				
 
 				InputStream inputStream= null;
 				
 				if((httpResponse.getEntity().getContentEncoding() != null) && (httpResponse.getEntity().getContentEncoding().getValue().equalsIgnoreCase("gzip"))){
 
 					Log.d("Aptoide-ManagerDownloads","with gzip");
 					inputStream = new GZIPInputStream(httpResponse.getEntity().getContent());
 
 				}else{
 
 //					Log.d("Aptoide-ManagerDownloads","No gzip");
 					inputStream = httpResponse.getEntity().getContent();
 
 				}
 				
 				byte data[] = new byte[8096];
 				/** in percentage */
 				int progressTrigger = 5; 
 				int bytesRead;
 
 				while((bytesRead = inputStream.read(data, 0, 8096)) > 0) {
 					download.incrementProgress(bytesRead);
 					fileOutputStream.write(data,0,bytesRead);
 					if(download.getProgressPercentage() % progressTrigger == 0){
 						progressUpdateHandler.sendEmptyMessage(cache.hashCode());
 					}
 				}
 				Log.d("Aptoide-ManagerDownloads","Download done! Name: "+download.getRemotePath()+" localPath: "+localPath);
 				download.setCompleted();
 				fileOutputStream.flush();
 				fileOutputStream.close();
 				inputStream.close();
 
 				if(cache.hasMd5Sum()){
 					if(!cache.checkMd5()){
 						cache.clearCache();
 						throw new AptoideExceptionDownload("md5 check failed!");
 					}
 				}
 				
 				installApp(cache);
 
 			}
 		}catch (Exception e) {
 			try {
 				fileOutputStream.flush();
 				fileOutputStream.close();	
 			} catch (Exception e1) { }		
 			e.printStackTrace();
 			if(cache.getFileLength() > 0){
 				download.setCompleted();
 				progressUpdateHandler.sendEmptyMessage(cache.hashCode());
 //				scheduleInstallApp(cache.getId());
 			}
 			throw new AptoideExceptionDownload(e);
 		}
 	}
 
 }
