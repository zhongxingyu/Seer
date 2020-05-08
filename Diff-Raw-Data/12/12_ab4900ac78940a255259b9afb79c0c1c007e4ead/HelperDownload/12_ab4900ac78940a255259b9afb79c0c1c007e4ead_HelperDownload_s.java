 /*
  * HelperDownload, part of Aptoide
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
 package cm.aptoide.pt.services;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import cm.aptoide.pt.ApplicationAptoide;
 import cm.aptoide.pt.R;
 import cm.aptoide.pt.exceptions.AptoideExceptionDownload;
 import cm.aptoide.pt.exceptions.AptoideExceptionNotFound;
 import cm.aptoide.pt.util.Constants;
 import cm.aptoide.pt.util.NetworkUtils;
 import cm.aptoide.pt.views.*;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.holoeverywhere.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.TimeZone;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeoutException;
 import java.util.zip.GZIPInputStream;
 
 /**
  * HelperDownload, manages the actual download processes, and updates the download manager about the status of each download
  *
  * @author dsilveira
  *
  */
 public class HelperDownload{
 
 	ServiceDownloadManager serviceDownloadManager;
 
 
 	public HelperDownload(ServiceDownloadManager serviceDownloadManager) {
 		this.serviceDownloadManager = serviceDownloadManager;
 		downloadManager = new DownloadManager();
 	}
 
 	private Handler toastHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Toast.makeText(serviceDownloadManager.getApplicationContext(), msg.what, Toast.LENGTH_SHORT).show();
 		}
 	};
 
 	private DownloadManager downloadManager;
 
     private class DownloadManager{
     	private ExecutorService downloadsPool;
     	private HashMap<Integer, ViewDownload> ongoingDownloads;
 
     	public DownloadManager(){
     		downloadsPool = Executors.newFixedThreadPool(Constants.MAX_PARALLEL_DOWNLOADS);
     		ongoingDownloads = new HashMap<Integer, ViewDownload>();
     	}
 
     	public void downloadApk(ViewDownload download, ViewCache cache, boolean isPaid){
     		downloadApk(download, cache, null, isPaid);
     	}
 
     	public void downloadApk(ViewDownload download, ViewCache cache, ViewLogin login, boolean isPaid){
     		ongoingDownloads.put(cache.hashCode(), download);
         	try {
 				downloadsPool.execute(new DownloadApk(download, cache, login, isPaid));
 			} catch (Exception e) { }
         }
 
     	private class DownloadApk implements Runnable{
 
     		ViewDownload download;
     		ViewCache cache;
     		ViewLogin login;
             boolean isPaid;
 
 			public DownloadApk(ViewDownload download, ViewCache cache, ViewLogin login, boolean isPaid) {
 				this.download = download;
 				this.cache = cache;
 				this.login = login;
                 this.isPaid = isPaid;
 			}
 
 			@Override
 			public void run() {
 	    		Log.d("Aptoide-ManagerDownloads", "apk download: "+cache);
 //				if(cache.isCached() && cache.hasMd5Sum() && cache.checkMd5()){
 //	    			download.setCompleted();
 //					serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 //	    		}else{
 					toastHandler.sendEmptyMessage(download.getStatus().equals(EnumDownloadStatus.RESUMING)?R.string.resuming_download:R.string.starting_download);
 	    			try {
 
 	    				download(download, cache, login, isPaid);
 	    			} catch (Exception e) {
 //	    				try {
 //	    					download(download, cache, login);
 //	    				} catch (Exception e2) {
 //	    					try {
 //								download(download, cache, login);
 //							} catch (Exception e3) {
 ////								e3.printStackTrace();
 //							}
 //	    				}
 //	    			}
 	    		}
 	    		ongoingDownloads.remove(cache.hashCode());
 			}
 
     	}
 
     	private void download(ViewDownload download, ViewCache cache, ViewLogin login, boolean isPaid){
     		boolean overwriteCache = isPaid;
     		boolean resuming = false;
     		boolean isLoginRequired = (login != null);
 
     		String localPath = cache.getLocalPath();
     		String remotePath = download.getRemotePath();
     		long targetBytes;
 
             File targetFile = new File(localPath);
             File parent = targetFile.getParentFile();
 
             if(!parent.exists() && !parent.mkdirs()){
                 Log.d("Aptoide-DownloadManager","Couldn't create dir: " + parent);
             }
 
     		FileOutputStream fileOutputStream = null;
 
 
 
     		try{
     			fileOutputStream = new FileOutputStream(localPath, !overwriteCache);
 
     			DefaultHttpClient httpClient = new DefaultHttpClient();
         		HttpParams httpParameters = new BasicHttpParams();
         		// Set the timeout in milliseconds until a connection is established.
         		// The default value is zero, that means the timeout is not used.
         		int timeoutConnection = Constants.SERVER_CONNECTION_TIMEOUT;
         		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
         		// Set the default socket timeout (SO_TIMEOUT)
         		// in milliseconds which is the timeout for waiting for data.
         		int timeoutSocket = Constants.SERVER_READ_TIMEOUT;
         		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
         		httpClient.setParams(httpParameters);
 
     			HttpGet httpGet = new HttpGet(remotePath);
     			Log.d("Aptoide-download","downloading from: "+remotePath+" to: "+localPath);
     			Log.d("Aptoide-download","downloading with: "+NetworkUtils.getUserAgentString(serviceDownloadManager.getApplicationContext()));
     			Log.d("Aptoide-download","downloading mode private: "+isLoginRequired);
 
     			httpGet.setHeader("User-Agent", NetworkUtils.getUserAgentString(serviceDownloadManager.getApplicationContext()));
 
 
 
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
     				Log.d("Aptoide-download","Problem in network... retry...");
     				httpResponse = httpClient.execute(httpGet);
     				if(httpResponse == null){
     					Log.d("Aptoide-download","Major network exception... Exiting!");
     					if(!resuming){
     						cache.clearCache();
     					}
     					throw new TimeoutException();
     				}
     			}
 
     			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
     			Log.d("Aptoide-download","Server Response Status Code: "+httpStatusCode);
 
     			switch (httpStatusCode) {
 					case 401:
 						fileOutputStream.close();
 	    				if(!resuming){
 	    					cache.clearCache();
 	    				}
 	    				download.setFailReason(EnumDownloadFailReason.TIMEOUT);
 	    				throw new TimeoutException(httpStatusCode+" "+httpResponse.getStatusLine().getReasonPhrase());
 					case 403:
						fileOutputStream.close();
 	    				if(!resuming){
 	    					cache.clearCache();
 	    				}
 	    				download.setFailReason(EnumDownloadFailReason.IP_BLACKLISTED);
 	    				throw new AptoideExceptionDownload(httpStatusCode+" "+httpResponse.getStatusLine().getReasonPhrase());
 					case 404:
 						fileOutputStream.close();
 	    				if(!resuming){
 	    					cache.clearCache();
 	    				}
 	    				download.setFailReason(EnumDownloadFailReason.NOT_FOUND);
 	    				throw new AptoideExceptionNotFound(httpStatusCode+" "+httpResponse.getStatusLine().getReasonPhrase());
 					case 416:
 						fileOutputStream.close();
 	    				if(!resuming){
 	    					cache.clearCache();
 	    				}
 	    				download.setCompleted();
 	    				serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 						return;
 
 					default:
 						if(httpResponse.containsHeader("Content-Length")){
 	    					targetBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
 	    					Log.d("Aptoide-download","Download targetBytes: "+targetBytes);
 	    					if(resuming){
 	    						targetBytes += download.getProgress();
 	    					}
 	    					download.setProgressTarget(targetBytes);
 	    				}
 
                         if(isPaid && !httpResponse.getFirstHeader("Content-Type").getValue().equals("application/vnd.android.package-archive")){
                             try{
 
                                 download.setFailReason(EnumDownloadFailReason.PAIDAPP_NOTFOUND);
                                 JSONObject object = new NetworkUtils().getJsonObject(remotePath, ApplicationAptoide.getContext());
                                 throw new AptoideExceptionDownload(object.getString("errors"));
 
                             }catch (JSONException e){
 
                                 throw new Exception();
 
                             }
 
 
 
 
                         }
 
 	    				BufferedInputStream inputStream= null;
 
 	    				if((httpResponse.getEntity().getContentEncoding() != null) && (httpResponse.getEntity().getContentEncoding().getValue().equalsIgnoreCase("gzip"))){
 
 	    					Log.d("Aptoide-download","with gzip");
 	    					inputStream = new BufferedInputStream(new GZIPInputStream(httpResponse.getEntity().getContent(),8*1024));
 
 	    				}else{
 
 //	    					Log.d("Aptoide-ManagerDownloads","No gzip");
 
 	    					inputStream = new BufferedInputStream(httpResponse.getEntity().getContent(),8*1024);
 
 	    				}
 
     					Log.d("Aptoide-download", "head download   id: "+cache.hashCode()+" "+download);
 //    					serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 
 	    				byte data[] = new byte[Constants.DOWNLOAD_CHUNK_SIZE];
 	    				/** trigger in miliseconds */
 	    				int progressTrigger = Constants.DOWNLOAD_UPDATE_TRIGGER;
 	    				int bytesRead;
 	    				long intervalStartTime = Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis();
 	    				long currentTime;
 	    				long intervalEndTime = intervalStartTime;
 	    				long intervalStartProgress = download.getProgress();
     					float formatConversion = ((float)Constants.MILISECONDS_TO_SECONDS/Constants.KILO_BYTE);
 
 	    				while((bytesRead = inputStream.read(data, 0, Constants.DOWNLOAD_CHUNK_SIZE)) > 0) {
 
 
 
 	    					if(download.getStatus().equals(EnumDownloadStatus.STOPPED) || download.getStatus().equals(EnumDownloadStatus.PAUSED)) {
 			    				fileOutputStream.flush();
 			    				fileOutputStream.close();
 			    				inputStream.close();
 		    					Log.d("Aptoide-download", "stop download   id: "+cache.hashCode()+" "+download.getStatus());
 			    				return;
 	    					}
 
 	    					currentTime = Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis();
 	    					fileOutputStream.write(data,0,bytesRead);
 							download.incrementProgress(bytesRead);
 	    					if((currentTime - intervalStartTime) > progressTrigger ){
 	        					intervalEndTime = currentTime;
 	        					try {
 	        						int speed = Math.round(formatConversion*((download.getProgress() - intervalStartProgress)/(intervalEndTime - intervalStartTime)));
 	        						Log.d("Aptoide-download", "progress increase: "+((download.getProgress() - intervalStartProgress)/Constants.KILO_BYTE)+" interval: "+((intervalEndTime - intervalStartTime))+" speed: "+speed);
 									download.setSpeedInKBps(speed);
 								} catch (ArithmeticException e) {
 									download.setSpeedInKBps(0);
 								}
 	        					intervalStartTime = intervalEndTime;
 	        					intervalStartProgress = download.getProgress();
 
 	    	    				download.setStatus(EnumDownloadStatus.DOWNLOADING);
 	    	    				serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 //		    					Log.d("Aptoide-download", "*downloading* id: "+cache.hashCode()+" "+download);
 	    					}
 	    				}
 	    				Log.d("Aptoide-download","Download done! Name: "+download.getRemotePathTail()+" localPath: "+localPath);
 
 	    				fileOutputStream.flush();
 	    				fileOutputStream.close();
 	    				inputStream.close();
 
 	    				if(cache.hasMd5Sum()){
 	    					if(!cache.checkMd5()){
 	    						cache.clearCache();
 	    	    				download.setFailReason(EnumDownloadFailReason.MD5_CHECK_FAILED);
 	    						throw new AptoideExceptionDownload("md5 check failed!");
 	    					}
 	    				}
 
 	    				download.setCompleted();
 	    				serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 						return;
 				}
 
     		}catch (SocketTimeoutException e) {
     			try {
     				fileOutputStream.flush();
     				fileOutputStream.close();
     			} catch (Exception e1) { }
     			e.printStackTrace();
     			download.setStatus(EnumDownloadStatus.FAILED);
     			download.setFailReason(EnumDownloadFailReason.TIMEOUT);
     			serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 				return;
 			}catch (Exception e) {
     			try {
     				fileOutputStream.flush();
     				fileOutputStream.close();
     			} catch (Exception e1) { }
     			e.printStackTrace();
 //    			if(cache.getFileLength() > 0){
     				download.setStatus(EnumDownloadStatus.FAILED);
     				if(download.getFailReason().equals(EnumDownloadFailReason.NO_REASON) && !NetworkUtils.isConnectionAvailable(serviceDownloadManager)){
     					download.setFailReason(EnumDownloadFailReason.CONNECTION_ERROR);
     				}
     				serviceDownloadManager.updateDownloadStatus(cache.hashCode(), download);
 //    				scheduleInstallApp(cache.getId());
 //    			}
     			if(download.getFailReason().equals(EnumDownloadFailReason.IP_BLACKLISTED)){
     				return;
     			}else{
     				throw new AptoideExceptionDownload(e);
     			}
     		}
     	}
     }
 
 
 
 	public void downloadApk(ViewDownload download, ViewCache cache, boolean paid) {
 		Log.d("Aptoide-HelperDownload", "starting apk download: "+download.getRemotePath());
 		downloadManager.downloadApk(download, cache, paid);
 	}
 
 	public void downloadPrivateApk(ViewDownload download, ViewCache cache, ViewLogin login, boolean paid) {
 		Log.d("Aptoide-HelperDownload", "starting apk download: "+download.getRemotePath());
 		downloadManager.downloadApk(download, cache, login, paid);
 	}
 
 	public void pauseDownload(int appId) {
 		Log.d("Aptoide-HelperDownload", "pausing apk download  id: "+appId);
 		downloadManager.ongoingDownloads.get(appId).setStatus(EnumDownloadStatus.PAUSED);
 	}
 
 	public void stopDownload(int appId) {
 		Log.d("Aptoide-HelperDownload", "stoping apk download  id: "+appId);
 		downloadManager.ongoingDownloads.get(appId).setStatus(EnumDownloadStatus.STOPPED);
 	}
 
 	public void shutdownNow(){
 		Log.d("Aptoide-HelperDownload", "shuting down");
 		for (ViewDownload download : downloadManager.ongoingDownloads.values()) {
 			download.setStatus(EnumDownloadStatus.STOPPED);
 		}
 		downloadManager.downloadsPool.shutdownNow();
 	}
 
 }
