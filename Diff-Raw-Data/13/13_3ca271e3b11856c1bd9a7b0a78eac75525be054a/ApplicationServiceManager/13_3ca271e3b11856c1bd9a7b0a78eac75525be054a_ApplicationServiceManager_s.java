 /*
  * ApplicationServiceManager, part of Aptoide
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
 package cm.aptoide.pt2;
 
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.Application;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.RemoteViews;
 import cm.aptoide.pt2.services.AIDLServiceDownload;
 import cm.aptoide.pt2.services.ServiceDownload;
 import cm.aptoide.pt2.util.Constants;
 import cm.aptoide.pt2.views.EnumDownloadProgressUpdateMessages;
 import cm.aptoide.pt2.views.EnumDownloadStatus;
 import cm.aptoide.pt2.views.ViewCache;
 import cm.aptoide.pt2.views.ViewDownload;
 import cm.aptoide.pt2.views.ViewDownloadManagement;
 
 /**
  * ApplicationServiceManager, manages interaction between interface classes and services
  *
  * @author dsilveira
  *
  */
 /**
  * ApplicationServiceManager
  *
  * @author dsilveira
  *
  */
 public class ApplicationServiceManager extends Application {
 	private boolean isRunning = false;
 
 	private boolean serviceDownloadSeenRunning = false;
 	private boolean serviceDownloadIsBound = false;
 	private ConnectivityManager connectivityState;
 
 	private HashMap<Integer, ViewDownloadManagement> ongoingDownloads;
 	private HashMap<Integer, ViewDownloadManagement> completedDownloads;
 	private HashMap<Integer, ViewDownloadManagement> failedDownloads;
 	
 	Handler downloadManager;
 	
 	private ViewDownload globaDownloadStatus;
 	
 	private ExecutorService cachedThreadPool;
 	
 	private NotificationManager notificationManager;
 	private WakeLock keepScreenOn;
 
 	private AIDLServiceDownload serviceDownloadCaller = null;
 	
 	private ServiceConnection serviceDownloadConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the object we can use to
 			// interact with the service.  We are communicating with the
 			// service using AIDL, so here we set the remote service interface.
 			serviceDownloadCaller = AIDLServiceDownload.Stub.asInterface(service);
 			serviceDownloadIsBound = true;
 			
 			Log.v("Aptoide-ApplicationServiceManager", "Connected to ServiceData");	
 			
 //			if(!serviceDownloadSeenRunning){
 //			}
             
             try {
                 Log.v("Aptoide-ApplicationServiceManager", "Called for registering as Download Status Observer");
 				serviceDownloadCaller.callRegisterDownloadStatusObserver(serviceDownloadCallback);
 			} catch (RemoteException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 	        
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			serviceDownloadCaller = null;
 			serviceDownloadIsBound = false;
 			
 			Log.v("Aptoide-ApplicationServiceManager", "Disconnected from ServiceData");
 		}
 	};
 	
 	
 	
 	private AIDLDownloadManager.Stub serviceDownloadCallback = new AIDLDownloadManager.Stub() {
 
 		@Override
 		public void updateDownloadStatus(int appId, ViewDownload update) throws RemoteException {
 			ViewDownloadManagement updating = ongoingDownloads.get(appId);
 			updating.updateProgress(update);
 			if(updating.isComplete() || updating.getDownloadStatus().equals(EnumDownloadStatus.STOPPED)
 										   || updating.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 				ViewDownloadManagement download = ongoingDownloads.remove(appId);
 //				Log.d("ManagerDownloads", "download removed from ongoing: "+download);					
 				if(download.isComplete()){
 					completedDownloads.put(download.hashCode(), download);
 					if(downloadManager != null){
 						downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.COMPLETED.ordinal());
 					}
 					installApp(download.getCache());					
 				}else if(download.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 					failedDownloads.put(appId, download);
 					if(downloadManager != null){
 						downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.FAILED.ordinal());
 					}
 				}
 			}else{
 				if(downloadManager != null){
 					downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.UPDATE.ordinal());
 				}
 			}
 			updateGlobalProgress();
 		}
 		
 	};
 	
 	public void registerDownloadManager(Handler downloadManager){
 		this.downloadManager = downloadManager;
 	}
 	
 	public void unregisterDownloadManager(){
 		this.downloadManager = null;
 	}
 	
 	
 	@Override
 	public void onCreate() {
 		if (!isRunning) {
 			connectivityState = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			keepScreenOn = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Full Power");
 			
 			ongoingDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			completedDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			failedDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			
 			globaDownloadStatus = new ViewDownload("local:\\GLOBAL");
 			
 			cachedThreadPool = Executors.newCachedThreadPool();
 			
 			makeSureServiceDownloadIsRunning();
 			isRunning = true;
 		}
 		super.onCreate();
 	}
 
 	private void makeSureServiceDownloadIsRunning(){
     	ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
     	for (RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
 			if(runningService.service.getClassName().equals(Constants.SERVICE_DOWNLOAD_CLASS_NAME)){
 				this.serviceDownloadSeenRunning = true;
 				break;
 			}
 		}
 
     	if(!serviceDownloadIsBound){
 //    		startService(new Intent(this, ServiceDownload.class));	//TODO uncomment this to make service independent of Aptoide's lifecycle
     		bindService(new Intent(this, ServiceDownload.class), serviceDownloadConnection, Context.BIND_AUTO_CREATE);
     	}
     }
 	
 	public boolean isConnectionAvailable(){
 		boolean connectionAvailable = false;
 		try {
 			connectionAvailable = connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable mobile: "+connectionAvailable);	
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable wifi: "+connectionAvailable);
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable wimax: "+connectionAvailable);
 		} catch (Exception e) { }
 		try {
 			connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
 			Log.d("ManagerDownloads", "isConnectionAvailable ethernet: "+connectionAvailable);
 		} catch (Exception e) { }
 		
 		return connectionAvailable;
 	}
 	
 //	public boolean isPermittedConnectionAvailable(ViewIconDownloadPermissions permissions){
 //		boolean connectionAvailable = false;
 //		if(permissions.isWiFi()){
 //			try {
 //				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED;
 //				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wifi: "+connectionAvailable);
 //			} catch (Exception e) { }
 //		} 
 //		if(permissions.isWiMax()){
 //			try {
 //				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(6).getState() == NetworkInfo.State.CONNECTED;
 //				Log.d("ManagerDownloads", "isPermittedConnectionAvailable wimax: "+connectionAvailable);
 //			} catch (Exception e) { }
 //		} 
 //		if(permissions.isMobile()){
 //			try {
 //				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED;
 //				Log.d("ManagerDownloads", "isPermittedConnectionAvailable mobile: "+connectionAvailable);
 //			} catch (Exception e) { }
 //		}
 //		if(permissions.isEthernet()){
 //			try {
 //				connectionAvailable = connectionAvailable || connectivityState.getNetworkInfo(9).getState() == NetworkInfo.State.CONNECTED;
 //				Log.d("ManagerDownloads", "isPermittedConnectionAvailable ethernet: "+connectionAvailable);
 //			} catch (Exception e) { }
 //		}
 //
 //		Log.d("ManagerDownloads", "isPermittedConnectionAvailable: "+connectionAvailable+"  permissions: "+permissions);
 //		return connectionAvailable;
 //	}
 	
 
 	private void setNotification() {
 		
 		String notificationTitle = getString(R.string.aptoide_downloading);
 		RemoteViews contentView = new RemoteViews(Constants.APTOIDE_PACKAGE_NAME, R.layout.notification_progress_bar);
 				
 		contentView.setImageViewResource(R.id.download_notification_icon, R.drawable.ic_notification);
 		contentView.setTextViewText(R.id.download_notification_name, notificationTitle);
 		contentView.setProgressBar(R.id.download_notification_progress_bar, (int)globaDownloadStatus.getProgressTarget(), (int)globaDownloadStatus.getProgress(), (globaDownloadStatus.getProgress() == 0?true:false));	
 		if(ongoingDownloads.size()>1){
 			contentView.setTextViewText(R.id.download_notification_number, getString(R.string.x_apps, ongoingDownloads.size()));
 		}else{
 			contentView.setTextViewText(R.id.download_notification_number, getString(R.string.x_app, ongoingDownloads.size()));
 		}
 		
     	Intent onClick = new Intent();
 		onClick.setClassName(Constants.APTOIDE_PACKAGE_NAME, Constants.APTOIDE_PACKAGE_NAME+".DownloadManager");
 		onClick.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 		onClick.setAction(Constants.APTOIDE_PACKAGE_NAME+".FROM_NOTIFICATION");
     	
     	// The PendingIntent to launch our activity if the user selects this notification
     	PendingIntent onClickAction = PendingIntent.getActivity(this, 0, onClick, 0);
 
     	Notification notification = new Notification(R.drawable.ic_notification, notificationTitle, System.currentTimeMillis());
     	notification.flags |= Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT;
 		notification.contentView = contentView;
 
 
 		// Set the info for the notification panel.
     	notification.contentIntent = onClickAction;
 //    	notification.setLatestEventInfo(this, getText(R.string.aptoide), getText(R.string.add_repo_text), contentIntent);
 
 
 		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     	// Send the notification.
     	// We use the position because it is a unique number.  We use it later to cancel.
     	notificationManager.notify(globaDownloadStatus.hashCode(), notification); 
     	
 //		Log.d("Aptoide-ApplicationServiceManager", "Notification Set");
 	}
 	
 
 	private void dismissNotification(){
 		try {
 			notificationManager.cancel(globaDownloadStatus.hashCode());
 		} catch (Exception e) { }
 	}
 	
 	
 	
 	private synchronized void updateGlobalProgress(){
 		globaDownloadStatus.setProgressTarget(100*ongoingDownloads.size());
 		globaDownloadStatus.setProgress(0);
 		globaDownloadStatus.setSpeedInKBps(0);
 		for (ViewDownloadManagement download : ongoingDownloads.values()) {
 			globaDownloadStatus.incrementProgress(download.getProgress());
 			globaDownloadStatus.incrementSpeed(download.getSpeedInKBps());
 		}
 		if(ongoingDownloads.size() > 0){
 			if(!keepScreenOn.isHeld()){
 				keepScreenOn.acquire();
 			}
 			setNotification();
 			if(downloadManager != null){
 				downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.UPDATE.ordinal());
 			}
 		}else{
 			keepScreenOn.release();
 			dismissNotification();
 		}
 	}
 	
 	
 	
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
 	
 	
 	
 	/**
 	 * getAppDownloading, returns the ViewDownloadManagement of the download with the given appHashid, 
 	 * 					  or the ViewDownloadManagement Null object if it doesn't match any ongoing download
 	 * 
 	 * @param appHashId
 	 * @return ViewDownloadManagement
 	 */
 	public ViewDownloadManagement getAppDownloading(int appHashId){
 		ViewDownloadManagement download = ongoingDownloads.get(appHashId);
 		if(download == null){
 			return new ViewDownloadManagement();
 		}else{
 			return download;
 		}
 	}
 	
 	/**
 	 * startDownload, starts managing the received download, and starts the download itself, 
 	 * 				  to be called by ViewDownloadManagement.startDownload() or by restart()
 	 * 
 	 * @param ViewDownloadManagement
 	 */
 	public void startDownload(final ViewDownloadManagement viewDownload){
 		ViewCache cache = viewDownload.getCache();
 		if(cache.isCached() && cache.hasMd5Sum() && cache.checkMd5()){
 			installApp(cache);
 		}else{
 			if(!ongoingDownloads.containsKey(viewDownload.hashCode())){
 				ongoingDownloads.put(viewDownload.hashCode(), viewDownload);
			}else if(!ongoingDownloads.get(viewDownload.hashCode()).getDownloadStatus().equals(EnumDownloadStatus.PAUSED)){
				return;
 			}
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						if(viewDownload.isLoginRequired()){
 							serviceDownloadCaller.callDownloadPrivateApk(viewDownload.getDownload(), viewDownload.getCache(), viewDownload.getLogin());
 						}else{
 							serviceDownloadCaller.callDownloadApk(viewDownload.getDownload(), viewDownload.getCache());
 						}
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			updateGlobalProgress();
 		}
 	}
 	
 	/**
 	 * pauseDownload, to be called by ViewDownloadManagement.pause()
 	 * 
 	 * @param appHashId
 	 */
 	public void pauseDownload(final int appHashId){
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.PAUSED);
 		if(downloadManager != null){
 			downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.PAUSED.ordinal());
 		}
 		try {
 			serviceDownloadCaller.callPauseDownload(appHashId);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * resumeDownload, to be called by ViewDownloadManagement.resume()
 	 * 
 	 * @param appHashId
 	 */
 	public void resumeDownload(final int appHashId){
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.RESUMING);
 		if(downloadManager != null){
 			downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.RESUMING.ordinal());
 		}
 		startDownload(ongoingDownloads.get(appHashId));
 	}
 
 	/**
 	 * stopDownload, to be called by ViewDownloadManagement.stop()
 	 * 
 	 * @param appHashId
 	 */
 	public void stopDownload(final int appHashId){
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.STOPPED);
 		ViewDownloadManagement download = ongoingDownloads.remove(appHashId);
 		if(downloadManager != null){
 			downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.STOPPED.ordinal());
 		}
 		if(download.getDownloadStatus().equals(EnumDownloadStatus.DOWNLOADING)){
 			try {
 				serviceDownloadCaller.callStopDownload(appHashId);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 		updateGlobalProgress();
 	}
 
 	/**
 	 * restartDownload, to be called by ViewDownloadManagement.restart()
 	 * 
 	 * @param appHashId
 	 */
 	public void restartDownload(final int appHashid){
 		if(failedDownloads.containsKey(appHashid)){
 			startDownload(failedDownloads.remove(appHashid));
 			if(downloadManager != null){
 				downloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.RESTARTING.ordinal());
 			}
 		}		
 	}
 	
 	
 	public boolean areDownloadsOngoing(){
 		return !ongoingDownloads.isEmpty();
 	}
 	
 	public Object[] getDownloadsOngoing(){
 		return ongoingDownloads.values().toArray();
 	}
 	
 	public boolean areDownloadsCompleted(){
 		return !completedDownloads.isEmpty();
 	}
 	
 	public Object[] getDownloadsCompleted(){
 		return completedDownloads.values().toArray();
 	}
 	
 	public boolean areDownloadsFailed(){
 		return !failedDownloads.isEmpty();
 	}
 	
 	public Object[] getDownloadsFailed(){
 		return failedDownloads.values().toArray();
 	}
 	
 }
