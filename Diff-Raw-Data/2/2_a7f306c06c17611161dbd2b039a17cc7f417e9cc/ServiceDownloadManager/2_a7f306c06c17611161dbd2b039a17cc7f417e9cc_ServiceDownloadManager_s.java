 /*
  * ServiceDownloadManager, part of Aptoide
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
 
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Parcel;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.RemoteViews;
 import cm.aptoide.pt2.AIDLDownloadManager;
 import cm.aptoide.pt2.AIDLDownloadObserver;
 import cm.aptoide.pt2.R;
 import cm.aptoide.pt2.util.Constants;
 import cm.aptoide.pt2.views.EnumDownloadStatus;
 import cm.aptoide.pt2.views.ViewCache;
 import cm.aptoide.pt2.views.ViewDownload;
 import cm.aptoide.pt2.views.ViewDownloadManagement;
 import cm.aptoide.pt2.views.ViewListDownloads;
 
 /**
  * ServiceDownloadManager, manages interaction between interface classes and services
  *
  * @author dsilveira
  *
  */
 public class ServiceDownloadManager extends Service {
 
 	AIDLDownloadManager downloadManager = null;
 
 	private HelperDownload helperDownload;
 	
 	private boolean isRunning = false;
 
 //	private boolean serviceDownloadSeenRunning = false;
 	private boolean serviceDownloadIsBound = false;
 
 	private HashMap<Integer, ViewDownloadManagement> ongoingDownloads;
 	private HashMap<Integer, ViewDownloadManagement> completedDownloads;
 	private HashMap<Integer, ViewDownloadManagement> failedDownloads;
 	
 	private ViewDownload globaDownloadStatus;
 	
 	private ExecutorService cachedThreadPool;
 	
 	private NotificationManager managerNotification;
 	private WakeLock keepScreenOn;
 
 	
 	
 	/**
 	 * When binding to the service, we return an interface to our AIDL stub
 	 * allowing clients to send requests to the service.
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		Log.d("Aptoide-ServiceDownloadManager", "binding new client");
 		return serviceDownloadManagerCallReceiver;
 	}
 	
 	private final AIDLServiceDownloadManager.Stub serviceDownloadManagerCallReceiver = new AIDLServiceDownloadManager.Stub() {
 		
 		@Override
 		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
 			try {
 				return super.onTransact(code, data, reply, flags);
 			} catch (RuntimeException e) {
 				Log.w("Aptoide-ServiceDownloadManager", "Unexpected serviceData exception", e);
 				throw e;
 			}
 		}
 
 		@Override
 		public void callRegisterDownloadManager(AIDLDownloadManager downloadManager) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "registered download manager");
 			registerDownloadManager(downloadManager);
 		}
 
 		@Override
 		public void callUnregisterDownloadManager() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "unregistered download manager");
 			unregisterDownloadManager();
 			
 		}
 
 		@Override
 		public void callRegisterDownloadObserver(int appHashId, AIDLDownloadObserver downloadObserver) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "registered download observer");
 			try {
 				ongoingDownloads.get(appHashId).registerObserver(downloadObserver);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void callUnregisterDownloadObserver(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "unregistered download observer");
 			try {
 				ongoingDownloads.get(appHashId).unregisterObserver();
 				completedDownloads.get(appHashId).unregisterObserver();
 				failedDownloads.get(appHashId).unregisterObserver();
 			} catch (Exception e) {	}
 		}
 
 		@Override
 		public void callInstallApp(ViewCache apk) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "installing app");
 			installApp(apk);			
 		}
 
 		@Override
 		public ViewDownloadManagement callGetAppDownloading(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "checking if app is downloading");
 			return getAppDownloading(appHashId);
 		}
 
 		@Override
 		public void callStartDownload(ViewDownloadManagement download) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "starting app download");
 			startDownload(download);
 		}
 
 		@Override
 		public void callStartDownloadAndObserve( ViewDownloadManagement download, AIDLDownloadObserver downloadObserver) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "starting app download and registering observer");
 			download.registerObserver(downloadObserver);
 			startDownload(download);
 		}
 
 		@Override
 		public void callPauseDownload(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "pausing app download");
 			pauseDownload(appHashId);
 		}
 
 		@Override
 		public void callResumeDownload(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "resuming app download");
 			resumeDownload(appHashId);
 		}
 
 		@Override
 		public void callStopDownload(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "stoping app download");
 			stopDownload(appHashId);
 		}
 
 		@Override
 		public void callRestartDownload(int appHashId) throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "restarting app download");
 			restartDownload(appHashId);
 		}
 
 		@Override
 		public boolean callAreDownloadsOngoing() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "checking if there are downloads ongoing");
 			return areDownloadsOngoing();
 		}
 
 		@Override
 		public ViewListDownloads callGetDownloadsOngoing() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "getting downloads ongoing");
 			return getDownloadsOngoing();
 		}
 
 		@Override
 		public boolean callAreDownloadsCompleted() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "checking if there are downloads completed");
 			return areDownloadsCompleted();
 		}
 
 		@Override
 		public ViewListDownloads callGetDownloadsCompleted() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "getting downloads completed");
 			return getDownloadsCompleted();
 		}
 
 		@Override
 		public boolean callAreDownloadsFailed() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "checking if there are downloads failed");
 			return areDownloadsFailed();
 		}
 
 		@Override
 		public ViewListDownloads callGetDownloadsFailed() throws RemoteException {
 			Log.d("Aptoide-ServiceDownloadManager", "getting downloads failed");
 			return getDownloadsFailed();
 		}
 		
 	}; 
 	
 
 	
 	public void registerDownloadManager(AIDLDownloadManager downloadManager){
 		this.downloadManager = downloadManager;
 	}
 	
 	public void unregisterDownloadManager(){
 		this.downloadManager = null;
 	}
 	
 	public boolean isDownloadManagerRegistered(){
 		return this.downloadManager != null;
 	}
 
 	
 	
 	
 	@Override
 	public void onCreate() {
 		if (!isRunning) {
 			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			keepScreenOn = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "Full Power");
 			
 			ongoingDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			completedDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			failedDownloads = new HashMap<Integer, ViewDownloadManagement>();
 			
 			globaDownloadStatus = new ViewDownload("local:\\GLOBAL");
 			
 			cachedThreadPool = Executors.newCachedThreadPool();
 			
 			helperDownload = new HelperDownload(this);
 			isRunning = true;
 		}
 		super.onCreate();
 	}
 
 
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
 
 
 		managerNotification = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     	// Send the notification.
     	// We use the position because it is a unique number.  We use it later to cancel.
     	managerNotification.notify(globaDownloadStatus.hashCode(), notification); 
     	
 //		Log.d("Aptoide-ApplicationServiceManager", "Notification Set");
 	}
 	
 
 	private void dismissNotification(){
 		try {
 			managerNotification.cancel(globaDownloadStatus.hashCode());
 		} catch (Exception e) { }
 	}
 	
 	
 	
 //	public void updateDownloadStatus(int appId, ViewDownload update){
 //		Log.d("Aptoide", "download update status *************** "+update.getStatus());
 //		Log.d("Aptoide", "ongoing downloads *************** "+ongoingDownloads);
 //		ViewDownloadManagement updating = ongoingDownloads.get(appId);
 //		updating.updateProgress(update);
 //		if(updating.isComplete() || updating.getDownloadStatus().equals(EnumDownloadStatus.STOPPED)
 //				|| updating.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 //
 //			ViewDownloadManagement download = ongoingDownloads.remove(appId);
 //			Log.d("ManagerDownloads", "download removed from ongoing: "+download);					
 //			if(download.isComplete()){
 //				completedDownloads.put(download.hashCode(), download);
 //				if(handlerDownloadManager != null){
 //					handlerDownloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.COMPLETED.ordinal());
 //				}
 //				installApp(download.getCache());					
 //			}else if(download.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 //				failedDownloads.put(appId, download);
 //				if(handlerDownloadManager != null){
 //					handlerDownloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.FAILED.ordinal());
 //				}
 //			}
 //		}else{
 //			if(handlerDownloadManager != null){
 //				handlerDownloadManager.sendEmptyMessage(EnumDownloadProgressUpdateMessages.UPDATE.ordinal());
 //			}
 //		}
 //		updateGlobalProgress();
 //	}	
 	public void updateDownloadStatus(int appId, ViewDownload update){
 		Log.d("Aptoide", "download update status *************** "+update.getStatus());
 		Log.d("Aptoide", "ongoing downloads *************** "+ongoingDownloads);
 		ViewDownloadManagement updating = ongoingDownloads.get(appId);
 		updating.updateProgress(update);
 		try {
 			updating.getObserver().updateDownloadStatus(update);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if(updating.isComplete() || updating.getDownloadStatus().equals(EnumDownloadStatus.STOPPED)
 				|| updating.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 
 			ViewDownloadManagement download = ongoingDownloads.remove(appId);
 			Log.d("ManagerDownloads", "download removed from ongoing: "+download);					
 			if(download.isComplete()){
 				completedDownloads.put(download.hashCode(), download);
 				if(isDownloadManagerRegistered()){
 					try {
 						downloadManager.updateDownloadStatus(EnumDownloadStatus.COMPLETED.ordinal());
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 				installApp(download.getCache());					
 			}else if(download.getDownloadStatus().equals(EnumDownloadStatus.FAILED)){
 				failedDownloads.put(appId, download);
 				if(isDownloadManagerRegistered()){
 					try {
 						downloadManager.updateDownloadStatus(EnumDownloadStatus.FAILED.ordinal());
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}else{
 			if(isDownloadManagerRegistered()){
 				try {
 					downloadManager.updateDownloadStatus(EnumDownloadStatus.DOWNLOADING.ordinal());
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		updateGlobalProgress();
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
 		}else{
 			keepScreenOn.release();
 			dismissNotification();
 		}
 
 		Log.d("Aptoide", "update global progress: ongoing downloads *************** "+ongoingDownloads);
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
 		Log.d("Aptoide", "download being started *************** "+viewDownload.hashCode());
 		ViewCache cache = viewDownload.getCache();
 		if(cache.isCached() && cache.hasMd5Sum() && cache.checkMd5()){
 			installApp(cache);
 		}else{
 //			if(isPermittedConnectionAvailable()){
 				if(!ongoingDownloads.containsKey(viewDownload.hashCode())){
 					ongoingDownloads.put(viewDownload.hashCode(), viewDownload);
 				}else switch (ongoingDownloads.get(viewDownload.hashCode()).getDownloadStatus()) {
 					case SETTING_UP:
 					case PAUSED:
 					case RESUMING:
 					case RESTARTING:					
 						break;
 		
 					default:
 						return;
 				}
 				
 				cachedThreadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						if(viewDownload.isLoginRequired()){
 							helperDownload.downloadPrivateApk(viewDownload.getDownload(), viewDownload.getCache(), viewDownload.getLogin());
 						}else{
 							helperDownload.downloadApk(viewDownload.getDownload(), viewDownload.getCache());
 						}
 					}
 				});
 				updateGlobalProgress();
 				if(isDownloadManagerRegistered()){
 					try {
 						downloadManager.updateDownloadStatus(EnumDownloadStatus.SETTING_UP.ordinal());
 					} catch (RemoteException e) {
 						e.printStackTrace();
 					}
 				}
 //			}
 		}
 	}
 	
 	/**
 	 * pauseDownload, to be called by ViewDownloadManagement.pause()
 	 * 
 	 * @param appHashId
 	 */
 	public void pauseDownload(final int appHashId){
 		Log.d("Aptoide", "download being paused *************** "+appHashId);
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.PAUSED);
 		helperDownload.pauseDownload(appHashId);
 		try {
 			if(isDownloadManagerRegistered()){
 				downloadManager.updateDownloadStatus(EnumDownloadStatus.PAUSED.ordinal());
 			}
 			ongoingDownloads.get(appHashId).getObserver().updateDownloadStatus(ongoingDownloads.get(appHashId).getDownload());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * resumeDownload, to be called by ViewDownloadManagement.resume()
 	 * 
 	 * @param appHashId
 	 */
 	public void resumeDownload(final int appHashId){
 		Log.d("Aptoide", "download being resumed *************** "+appHashId);
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.RESUMING);
 		startDownload(ongoingDownloads.get(appHashId));
 		try {
 			if(isDownloadManagerRegistered()){
 				downloadManager.updateDownloadStatus(EnumDownloadStatus.RESUMING.ordinal());
 			}
 			ongoingDownloads.get(appHashId).getObserver().updateDownloadStatus(ongoingDownloads.get(appHashId).getDownload());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * stopDownload, to be called by ViewDownloadManagement.stop()
 	 * 
 	 * @param appHashId
 	 */
 	public void stopDownload(final int appHashId){
 		Log.d("Aptoide", "download being stopped *************** "+appHashId);
 		ongoingDownloads.get(appHashId).getDownload().setStatus(EnumDownloadStatus.STOPPED);
 		ViewDownloadManagement download = ongoingDownloads.remove(appHashId);
 //		ViewDownloadManagement download = ongoingDownloads.get(appHashId);
 		if(download.getDownloadStatus().equals(EnumDownloadStatus.DOWNLOADING)){
 			helperDownload.stopDownload(appHashId);
 		}
 		updateGlobalProgress();
 		try {
 			if(isDownloadManagerRegistered()){
 				downloadManager.updateDownloadStatus(EnumDownloadStatus.STOPPED.ordinal());
 			}
			download.getObserver().updateDownloadStatus(ongoingDownloads.get(appHashId).getDownload());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * restartDownload, to be called by ViewDownloadManagement.restart()
 	 * 
 	 * @param appHashId
 	 */
 	public void restartDownload(final int appHashId){
 		Log.d("Aptoide", "download being restarted *************** "+appHashId);
 		if(failedDownloads.containsKey(appHashId)){
 			startDownload(failedDownloads.remove(appHashId));
 			try {
 				if(isDownloadManagerRegistered()){
 					downloadManager.updateDownloadStatus(EnumDownloadStatus.RESTARTING.ordinal());
 				}
 				ongoingDownloads.get(appHashId).getObserver().updateDownloadStatus(ongoingDownloads.get(appHashId).getDownload());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 	
 	
 	public boolean areDownloadsOngoing(){
 		return !ongoingDownloads.isEmpty();
 	}
 	
 	public boolean areDownloadsCompleted(){
 		return !completedDownloads.isEmpty();
 	}
 	
 	public boolean areDownloadsFailed(){
 		return !failedDownloads.isEmpty();
 	}
 	
 	
 	public ViewListDownloads getDownloadsOngoing(){
 		ViewListDownloads ongoing = new ViewListDownloads();
 		ongoing.addAll(ongoingDownloads.values());
 		return ongoing;
 	}
 	
 	public ViewListDownloads getDownloadsCompleted(){
 		ViewListDownloads completed = new ViewListDownloads();
 		completed.addAll(completedDownloads.values());
 		return completed;
 	}
 	
 	public ViewListDownloads getDownloadsFailed(){
 		ViewListDownloads failed = new ViewListDownloads();
 		failed.addAll(failedDownloads.values());
 		return failed;
 	}
 	
 }
