 /**
  * AptoideServiceData, part of Aptoide
  * Copyright (C) 2011 Duarte Silveira
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
 package pt.caixamagica.aptoide.appsbackup.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import pt.caixamagica.aptoide.appsbackup.AIDLLogin;
 import pt.caixamagica.aptoide.appsbackup.AIDLUpload;
 import pt.caixamagica.aptoide.appsbackup.Aptoide;
 import pt.caixamagica.aptoide.appsbackup.BazaarLogin;
 import pt.caixamagica.aptoide.appsbackup.EnumAppsSorting;
 import pt.caixamagica.aptoide.appsbackup.ManageRepos;
 import pt.caixamagica.aptoide.appsbackup.R;
 import pt.caixamagica.aptoide.appsbackup.SelfUpdate;
 import pt.caixamagica.aptoide.appsbackup.Splash;
 import pt.caixamagica.aptoide.appsbackup.Upload;
 import pt.caixamagica.aptoide.appsbackup.data.cache.ManagerCache;
 import pt.caixamagica.aptoide.appsbackup.data.cache.ViewCache;
 import pt.caixamagica.aptoide.appsbackup.data.database.ManagerDatabase;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionExtras;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionStats;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionsInfo;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayCategory;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListApps;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListComments;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListRepos;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListsDimensions;
 import pt.caixamagica.aptoide.appsbackup.data.listeners.ViewMyapp;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplication;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplicationInstalled;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewIconInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewLogin;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewRepository;
 import pt.caixamagica.aptoide.appsbackup.data.notifications.ManagerNotifications;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.EnumAgeRating;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.ManagerPreferences;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.ViewSettings;
 import pt.caixamagica.aptoide.appsbackup.data.system.ManagerSystemSync;
 import pt.caixamagica.aptoide.appsbackup.data.system.ViewHwFilters;
 import pt.caixamagica.aptoide.appsbackup.data.system.ViewScreenDimensions;
 import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumDownloadType;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginCreateStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerUploadApkStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ManagerDownloads;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ManagerUploads;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewApk;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewDownload;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewDownloadInfo;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewDownloadStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewIconDownloadPermissions;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewListAppsDownload;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewServerLogin;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewUploadInfo;
 import pt.caixamagica.aptoide.appsbackup.data.xml.EnumInfoType;
 import pt.caixamagica.aptoide.appsbackup.data.xml.ManagerXml;
 import pt.caixamagica.aptoide.appsbackup.data.xml.ViewLatestVersionInfo;
 import pt.caixamagica.aptoide.appsbackup.debug.AptoideLog;
 import pt.caixamagica.aptoide.appsbackup.debug.InterfaceAptoideLog;
 import pt.caixamagica.aptoide.appsbackup.debug.exceptions.AptoideExceptionConnectivity;
 import pt.caixamagica.aptoide.appsbackup.debug.exceptions.AptoideExceptionDatabase;
 import pt.caixamagica.aptoide.appsbackup.debug.exceptions.AptoideExceptionDownload;
 import pt.caixamagica.aptoide.appsbackup.debug.exceptions.AptoideExceptionSpaceInSDCard;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Parcel;
 import android.os.RemoteException;
 import android.os.StrictMode;
 import android.util.Log;
 import android.widget.Toast;
 import pt.caixamagica.aptoide.appsbackup.AIDLAppInfo;
 import pt.caixamagica.aptoide.appsbackup.AIDLAptoideInterface;
 import pt.caixamagica.aptoide.appsbackup.AIDLReposInfo;
 import pt.caixamagica.aptoide.appsbackup.AIDLSelfUpdate;
 import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;
 
 /**
  * AptoideServiceData, Aptoide's data I/O manager for the activity classes
  * 
  * @author dsilveira
  * @since 3.0
  *
  */
 public class AptoideServiceData extends Service implements InterfaceAptoideLog {
 
 	private final String TAG = "Aptoide-ServiceData";
 	private boolean isRunning = false;
 	
 	private ViewDisplayListsDimensions displayListsDimensions;
 	
 	private ArrayList<Integer> reposInserting;
 	private ArrayList<ViewMyapp> waitingMyapps;
 	private ViewDisplayListRepos waitingMyappRepos;
 	private ViewLatestVersionInfo waitingSelfUpdate;
 	
 	private AIDLSelfUpdate selfUpdateClient = null;
 	private AIDLUpload uploadClient = null;
 	private AIDLLogin loginClient = null;
 	private HashMap<EnumServiceDataCallback, AIDLAptoideInterface> aptoideClients;
 	private HashMap<Integer, AIDLAppInfo> appInfoClients;
 	private HashMap<Integer, ViewDisplayListComments> appInfoComments;
 	private AIDLReposInfo reposInfoClient = null;
 //	private AIDLSearch searchClient = null;	//TODO implement sort blocking in search when loading repo from bare
 
 	private ManagerPreferences managerPreferences;
 	private ManagerSystemSync managerSystemSync;
 	private ManagerDatabase managerDatabase;
 	private ManagerDownloads managerDownloads;
 	private ManagerUploads managerUploads;
 	private ManagerNotifications managerNotifications;
 	private ManagerXml managerXml;
 	
 	private ExecutorService cachedThreadPool;		//TODO in the future turn this into a priorityThreadPool, with a highest priority thread able to pause execution of other threads
 //	private ExecutorService scheduledThreadPool;
 	
 	Handler delayedExecutionHandler = new Handler();
     
 	private AtomicBoolean syncingInstalledApps;
 	private AtomicBoolean addingRepo;
 	private AtomicBoolean automaticBackupOn;
 	private AtomicBoolean registeredNetworkStateChangeReceiver;
 	
 	/**
 	 * When binding to the service, we return an interface to our AIDL stub
 	 * allowing clients to send requests to the service.
 	 */
 	@Override
 	public IBinder onBind(Intent intent) {
 		AptoideLog.d(AptoideServiceData.this, "binding new client");
 		return aptoideServiceDataCallReceiver;
 	}
 	
 	private final AIDLAptoideServiceData.Stub aptoideServiceDataCallReceiver = new AIDLAptoideServiceData.Stub() {
 		
 		@Override
 		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
 			try {
 				return super.onTransact(code, data, reply, flags);
 			} catch (RuntimeException e) {
 				Log.w("Aptoide-ServiceData", "Unexpected serviceData exception", e);
 				throw e;
 			}
 		}
 
 		@Override
 		public void callRegisterSelfUpdateObserver(AIDLSelfUpdate selfUpdateClient) throws RemoteException {
 			registerSelfUpdateObserver(selfUpdateClient);
 		}
 
 		@Override
 		public void callAcceptSelfUpdate() throws RemoteException {
 			downloadSelfUpdate(waitingSelfUpdate);
 			waitingSelfUpdate = null;
 		}
 
 		@Override
 		public void callRejectSelfUpdate() throws RemoteException {
 			rejectSelfUpdate();
 		}
 
 		@Override
 		public void callSyncInstalledApps() throws RemoteException {
 	    	syncInstalledApps();			
 		}
 
 		@Override
 		public void callCacheInstalledAppsIcons() throws RemoteException {
 			managerSystemSync.cacheInstalledIcons();
 		}
 
 		@Override
 		public String callGetAptoideVersionName() throws RemoteException {
 			return managerSystemSync.getAptoideVersionNameInUse();
 		}
 		
 		@Override
 		public void callStoreScreenDimensions(ViewScreenDimensions screenDimensions) throws RemoteException {
 			displayListsDimensions = new ViewDisplayListsDimensions(screenDimensions);
 			Log.d("Aptoide-ServiceData","displayListsDimensions"+displayListsDimensions);
 			storeScreenDimensions(screenDimensions);	
 		}
 
 		@Override
 		public ViewDisplayListsDimensions callGetDisplayListsDimensions() throws RemoteException {
 			return displayListsDimensions;
 		}
 
 		@Override
 		public int callRegisterAvailableAppsObserver(AIDLAptoideInterface availableAppsObserver) throws RemoteException {
 			return registerAvailableDataObserver(availableAppsObserver);
 		}
 		
 		@Override
 		public void callUnregisterAvailableAppsObserver(AIDLAptoideInterface availableAppsObserver) throws RemoteException {
 			unregisterAvailableDataObserver(availableAppsObserver);
 		}
 		
 		@Override
 		public void callRegisterInstalledAppsObserver(AIDLAptoideInterface installedAppsObserver) throws RemoteException {
 			registerInstalledDataObserver(installedAppsObserver);
 		}
 		
 		@Override
 		public ViewDisplayListApps callGetInstalledApps() throws RemoteException {
 			return getInstalledApps();
 		}
 
 		@Override
 		public void callRegisterReposObserver(AIDLReposInfo reposInfoObserver) throws RemoteException {
 			registerReposObserver(reposInfoObserver);
 		}
 
 		@Override
 		public void callAddRepo(ViewRepository repository) throws RemoteException {
 //			if(repoIsManaged(repository.getHashid())){
 //				//TDO check for delta
 //				updateAvailableLists();
 //			}else{
 				addRepoBare(repository);	//TODO serialize requests (handle one at a time)			
 //			}
 		}
 
 		@Override
 		public void callRemoveRepo(int repoHashid) throws RemoteException {
 			removeRepo(repoHashid);
 		}
 
 		@Override
 		public void callSetInUseRepo(int repoHashid) throws RemoteException {
 			toggleInUseRepo(repoHashid, true);
 		}
 
 		@Override
 		public void callUnsetInUseRepo(int repoHashid) throws RemoteException {
 			toggleInUseRepo(repoHashid, false);
 		}
 
 		@Override
 		public void callUpdateRepos() throws RemoteException {
 			getDeltas(true);
 		}
 
 		@Override
 		public void callDelayedUpdateRepos() throws RemoteException {
 			delayedExecutionHandler.postDelayed(new Runnable() {
 				public void run() {
 					try {
 						getDeltas(true);
 					} catch (Exception e) { }
 				}
 			}, 15000);
 		}
 
 		@Override
 		public void callRemoveLogin(int repoHashid) throws RemoteException {
 			removeLogin(repoHashid);
 		}
 
 		@Override
 		public void callUpdateLogin(ViewRepository repo) throws RemoteException {
 			updateLogin(repo);
 		}
 
 		@Override
 		public ViewDisplayListRepos callGetRepos() throws RemoteException {
 			return getRepos();
 		}
 
 		@Override
 		public void callNoRepos() throws RemoteException {
 			noAvailableListData();
 		}
 
 		@Override
 		public void callLoadingRepos() throws RemoteException {
 			loadingAvailableListData();
 		}
 
 		@Override
 		public boolean callAnyReposInUse() throws RemoteException {
 			return areAnyReposInUse();
 		}
 		
 		
 
 		@Override
 		public boolean callAreListsByCategory() throws RemoteException {
 			return getShowApplicationsByCategory();
 		}
 
 		@Override
 		public void callSetListsBy(boolean byCategory) throws RemoteException {
 			setShowApplicationsByCategory(byCategory);
 		}
 
 		@Override
 		public ViewDisplayCategory callGetCategories() throws RemoteException {
 			return getCategories();
 		}
 
 		@Override
 		public int callGetAppsSortingPolicy() throws RemoteException {
 			return getAppsSortingPolicy();
 		}
 
 		@Override
 		public void callSetAppsSortingPolicy(int sortingPolicy) throws RemoteException {
 			setAppsSortingPolicy(sortingPolicy);
 		}
 
 		@Override
 		public boolean callGetShowSystemApps() throws RemoteException {
 			return getShowSystemApps();
 		}
 
 		@Override
 		public void callSetShowSystemApps(boolean show) throws RemoteException {
 			setShowSystemApps(show);
 		}
 
 		@Override
 		public int callGetTotalAvailableApps() throws RemoteException {
 			return getTotalAvailableApps();
 		}
 
 		@Override
 		public int callGetTotalAvailableAppsInCategory(int categoryHashid) throws RemoteException {
 			return getTotalAvailableAppsInCategory(categoryHashid);
 		}
 
 		@Override
 		public ViewDisplayListApps callGetAvailableAppsByCategory(int offset, int range, int categoryHashid) throws RemoteException {
 			return getAvailableApps(offset, range, categoryHashid);
 		}
 
 		@Override
 		public ViewDisplayListApps callGetAvailableApps(int offset, int range) throws RemoteException {
 			return getAvailableApps(offset, range);
 		}
 
 		@Override
 		public ViewDisplayListApps callGetAllAvailableApps() throws RemoteException {
 			return getAllAvailableApps();
 		}
 
 		@Override
 		public ViewDisplayListApps callGetUpdatableApps() throws RemoteException {
 			return getUpdatableApps();
 		}
 
 		@Override
 		public void callUpdateAll() throws RemoteException {
 			updateAll();
 		}
 
 		@Override
 		public ViewDisplayListApps callGetAppSearchResults(String searchString) throws RemoteException {
 			return getAppSearchResults(searchString);
 		}
 
 		@Override
 		public void callRegisterAppInfoObserver(AIDLAppInfo appInfoObserver, int appHashid) throws RemoteException {
 			registerAppInfoObserver(appInfoObserver, appHashid);
 		}
 
 		@Override
 		public void CallFillAppInfo(int appHashid) throws RemoteException {
 			fillAppInfo(appHashid);
 		}
 
 		@Override
 		public void callAddVersionDownloadInfo(int appHashid, int repoHashid) throws RemoteException {
 			addRepoAppDownloadInfo(appHashid, repoHashid);
 		}
 
 		@Override
 		public void callAddVersionStatsInfo(int appHashid, int repoHashid) throws RemoteException {
 			addRepoAppStats(appHashid, repoHashid);
 		}
 
 		@Override
 		public void callAddVersionExtraInfo(int appHashid, int repoHashid) throws RemoteException {
 			addRepoAppExtras(appHashid, repoHashid);
 		}
 
 		@Override
 		public void callRetrieveVersionComments(int appHashid, int repoHashid) throws RemoteException {
 			retrieveRepoAppComments(appHashid, repoHashid);
 		}
 
 		@Override
 		public ViewDisplayAppVersionsInfo callGetAppInfo(int appHashid) throws RemoteException {
 			return getAppInfo(appHashid);
 		}
 
 		@Override
 		public int callGetAppVersionDownloadSize(int appFullHashid) throws RemoteException {
 			return getAppVersionDownloadSize(appFullHashid);
 		}
 
 		@Override
 		public ViewDisplayAppVersionStats callGetAppStats(int appFullHashid) throws RemoteException {
 			return getAppStats(appFullHashid);
 		}
 
 		@Override
 		public ViewDisplayAppVersionExtras callGetAppExtras(int appFullHashid) throws RemoteException {
 			return getAppExtras(appFullHashid);
 		}
 
 		@Override
 		public ViewDisplayListComments callGetVersionComments(int appFullHashid) throws RemoteException {
 			return getAppComments(appFullHashid);
 		}
 
 		@Override
 		public void callRegisterLoginObserver(AIDLLogin loginObserver) throws RemoteException {
 			registerLoginObserver(loginObserver);
 		}
 
 		@Override
 		public int callServerLoginCreate(ViewServerLogin serverLogin) throws RemoteException {
 			return serverLoginCreate(serverLogin);
 		}
 
 		@Override
 		public void callServerLoginAfterCreate(ViewServerLogin serverLogin) throws RemoteException {
 			serverLoginAfterCreate(serverLogin);
 		}
 
 		@Override
 		public String callGetServerToken() throws RemoteException {
 			return getServerToken();
 		}
 
 		@Override
 		public int callServerLogin(ViewServerLogin serverLogin) throws RemoteException {
 			return serverLogin(serverLogin);
 		}
 
 		@Override
 		public ViewServerLogin callGetServerLogin() throws RemoteException {
 			return getServerLogin();
 		}
 
 		@Override
 		public void callClearServerLogin() throws RemoteException {
 			clearServerLogin();
 		}
 
 		@Override
 		public int callAddAppVersionLike(String repoName, int appHashid, boolean like) throws RemoteException {
 			return addAppVersionLike(repoName, appHashid, like);
 		}
 
 		@Override
 		public int callAddAppVersionComment(String repoName, int appHashid, String commentBody, String subject, long answerTo) throws RemoteException {
 			return addAppVersionComment(repoName, appHashid, commentBody, subject, answerTo);
 		}
 
 		@Override
 		public void callScheduleInstallApp(int appHashid) throws RemoteException {
 			scheduleInstallApp(appHashid);
 		}
 
 		@Override
 		public void callUnscheduleInstallApp(int appHashid) throws RemoteException {
 			unscheduleInstallApp(appHashid);			
 		}
 
 		@Override
 		public boolean callIsAppScheduledToInstall(int appHashid) throws RemoteException {
 			return isAppScheduledToInstall(appHashid);
 		}
 
 		@Override
 		public ViewDisplayListApps callGetScheduledApps() throws RemoteException {
 			return getScheduledApps();
 		}
 
 		@Override
 		public void callInstallApp(int appHashid) throws RemoteException {
 			downloadApp(appHashid);
 		}
 
 		@Override
 		public void callUninstallApp(int appHashid) throws RemoteException {
 			uninstallApp(appHashid);
 		}
 
 		@Override
 		public void callUninstallApps(ViewListIds appHashids) throws RemoteException {
 			uninstallApps(appHashids);
 		}
 
 		@Override
 		public void callRegisterMyappReceiver(AIDLAptoideInterface myappObserver) throws RemoteException {
 			registerMyappReceiver(myappObserver);
 		}
 
 		@Override
 		public void callReceiveMyapp(String uriString) throws RemoteException {
 			receiveMyapp(uriString);
 		}
 
 		@Override
 		public ViewMyapp callGetWaitingMyapp() throws RemoteException {
 			if(!waitingMyapps.isEmpty()){
 				return waitingMyapps.remove(0);
 			}else{
 				return null;
 			}
 		}
 
 		@Override
 		public void callInstallMyapp(ViewMyapp myapp) throws RemoteException {
 			downloadMyapp(myapp);
 			manageMyappRepos();
 		}
 
 		@Override
 		public void callRejectedMyapp() throws RemoteException {
 			manageMyappRepos();
 		}
 
 		@Override
 		public ViewDisplayListRepos callGetWaitingMyappRepos() throws RemoteException {
 			return waitingMyappRepos;
 		}
 
 		@Override
 		public ViewSettings callGetSettings() throws RemoteException {
 			return getSettings();
 		}
 
 		@Override
 		public ViewIconDownloadPermissions callGetIconDownloadPermissions() throws RemoteException {
 			return getIconDownloadPermissions();
 		}
 
 		@Override
 		public void callSetIconDownloadPermissions(ViewIconDownloadPermissions iconDownloadPermissions) throws RemoteException {
 			setIconDownloadPermissions(iconDownloadPermissions);
 		}
 
 		@Override
 		public void callClearIconCache() throws RemoteException {
 			clearIconCache();
 		}
 
 		@Override
 		public void callClearApkCache() throws RemoteException {
 			clearApkCache();
 		}
 
 		@Override
 		public ViewHwFilters callGetHwFilters() throws RemoteException {
 			return getHwFilters();
 		}
 
 		@Override
 		public void callSetHwFilter(boolean on) throws RemoteException {
 			setHwFilter(on);
 		}
 
 		@Override
 		public void callSetAgeRating(int rating) throws RemoteException {
 			setAgeRating(EnumAgeRating.reverseOrdinal(rating));
 		}
 
 		@Override
 		public void callSetAutomaticInstall(boolean on) throws RemoteException {
 			setAutomaticInstall(on);
 		}
 
 		@Override
 		public void callResetAvailableApps() throws RemoteException {
 			resetAvailableLists();
 		}
 
 		@Override
 		public void callRegisterUploadObserver(AIDLUpload uploadObserver) throws RemoteException {
 			registerUploadObserver(uploadObserver);
 		}
 
 		@Override
 		public ViewUploadInfo callGetUploadInfo(int appHashid) throws RemoteException {
 			return getUploadInfo(appHashid);
 		}
 
 		@Override
 		public void callUploadApk(ViewApk uploadingApk) throws RemoteException {
 			uploadApk(uploadingApk);
 		}
 
 		@Override
 		public boolean callIsInsertingRepo() throws RemoteException {
 //			return (reposInserting.size() > 0);
 			return addingRepo.get();
 		}
 		
 	}; 
 
 	private Handler toastHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			Toast.makeText(AptoideServiceData.this, msg.what, Toast.LENGTH_SHORT).show();
 		}
 	};
 	
 	public void registerSelfUpdateObserver(AIDLSelfUpdate selfUpdateClient){
 		this.selfUpdateClient = selfUpdateClient;
 	}
 	
 	public void registerUploadObserver(AIDLUpload uploadClient){
 		this.uploadClient = uploadClient;
 	}
 	
 	public void registerLoginObserver(AIDLLogin loginClient){
 		this.loginClient = loginClient;
 	}
 
 	public int registerAvailableDataObserver(AIDLAptoideInterface availableAppsObserver){
     	
 		if(!getManagerCache().isFreeSpaceInSdcard()){
 			Toast.makeText(this, "No Available SDcard with enough free space for Aptoide To run!", Toast.LENGTH_SHORT).show();
 			try {
 				availableAppsObserver.shutDown();
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		aptoideClients.put(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST, availableAppsObserver);
     	AptoideLog.d(AptoideServiceData.this, "Registered Available Data Observer");
     	if( managerDatabase.getTotalAvailableApps(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating()) > Constants.MAX_APPLICATIONS_IN_STATIC_LIST_MODE){
         	AptoideLog.d(AptoideServiceData.this, "Switching Available List to dynamic");
     		switchAvailableListToDynamic();
     		loadingAvailableListData();
     	}
     	
     	if(managerPreferences.isServerInconsistenState()){
     		serverLoginAfterCreate(managerPreferences.getServerInconsistentStore());
     		return EnumAvailableAppsStatus.NO_REPO_IN_USE.ordinal();
     	}else{
     		return checkIfAnyReposInUse();
     	}
 	}
 	
 	public void unregisterAvailableDataObserver(AIDLAptoideInterface availableAppsObserver){
 		try {
 			aptoideClients.remove(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST);
 			shouldIShutDown();
 		} catch (Exception e) {	}
 	}
 	
 	public void registerInstalledDataObserver(AIDLAptoideInterface installedAppsObserver){
 		aptoideClients.put(EnumServiceDataCallback.UPDATE_INSTALLED_LIST, installedAppsObserver);
 //		if(!syncingInstalledApps.get()){
 //			try {
 //				installedAppsObserver.newInstalledListDataAvailable();AptoideLog.d(AptoideServiceData.this, "!syncing installed apps");
 //			} catch (RemoteException e) {
 //				e.printStackTrace();
 //			}
 //		}
     	AptoideLog.d(AptoideServiceData.this, "Registered Installed Data Observer");
 	}
 	
 	public void registerMyappReceiver(AIDLAptoideInterface myappObserver){
 		aptoideClients.put(EnumServiceDataCallback.HANDLE_MYAPP, myappObserver);
     	AptoideLog.d(AptoideServiceData.this, "Registered Myapp Observer");		
 	}
 	
 	
 	public void registerAppInfoObserver(AIDLAppInfo appInfoObserver, int appHashid){
 		appInfoClients.put(appHashid, appInfoObserver);
     	AptoideLog.d(AptoideServiceData.this, "Registered App Info Observer: "+appHashid);
 	}
 	
 	
 	public void registerReposObserver(AIDLReposInfo reposInfoObserver){
 		reposInfoClient = reposInfoObserver;
 	}
 	
 	
 	private BroadcastReceiver installedAppsChangeListener = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context receivedContext, Intent receivedIntent) {
 			if(receivedIntent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
 				String packageName = receivedIntent.getData().getEncodedSchemeSpecificPart();
 				Log.d("Aptoide-ServiceData", "installedAppsChangeListener - package added: "+packageName);
 				addInstalledApp(packageName);
				if(managerPreferences.isAutomaticInstallOn() && getServerToken() != null){
 					Log.d("Aptoide-AppsBackup", "preparing to auto-upload: "+packageName);
 					
 					ViewListIds uploads = new ViewListIds();
 					uploads.add(managerSystemSync.getAppHashid(packageName));
 					
 					if(managerUploads.isPermittedConnectionAvailable(managerPreferences.getIconDownloadPermissions())){
 						if(managerPreferences.getToken() != null){
 							Intent upload = new Intent(AptoideServiceData.this, Upload.class);
 							upload.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 							upload.putIntegerArrayListExtra("uploads", uploads);
 							startActivity(upload);
 						}else{
 							Log.d("Aptoide-ServiceData", "can't backup with no server login configured" );
 //							Toast.makeText(Aptoide.this, R.string.login_required, Toast.LENGTH_SHORT).show();
 							Intent login = new Intent(AptoideServiceData.this, BazaarLogin.class);
 							login.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 							login.putExtra("InvoqueType", BazaarLogin.InvoqueType.NO_CREDENTIALS_SET.ordinal());
 							login.putIntegerArrayListExtra("uploads", uploads);
 							startActivity(login);
 						}
 					}else{
 						Log.d("Aptoide-ServiceData", "can't backup at this moment, no permitted connection available" );						
 					}
 				}
 				
 			}else if(receivedIntent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
 				String packageName = receivedIntent.getData().getEncodedSchemeSpecificPart();
 				Log.d("Aptoide-ServiceData", "installedAppsChangeListener - package removed: "+packageName);
 				removeInstalledApp(packageName);
 			}
 		}
 	};
 	
 	private void registerInstalledAppsChangeReceiver(){
 		IntentFilter installedAppsChangeFilter = new IntentFilter();
 		installedAppsChangeFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
 		installedAppsChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
 		installedAppsChangeFilter.addDataScheme(Constants.SCHEME_PACKAGE);
 		registerReceiver(installedAppsChangeListener, installedAppsChangeFilter);
 	}
 	
 	
 	private BroadcastReceiver networkStateChangeListener = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context receivedContext, Intent receivedIntent) {
 			if(receivedIntent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
 				boolean connectivity = managerDownloads.isPermittedConnectionAvailable(getIconDownloadPermissions());
 				Log.d("Aptoide-ServiceData", "networkStateChangeListener - permitted conectivity changed to: "+connectivity);
 				if(connectivity){
 					installAllScheduledApps();
 				}
 			}
 		}
 	};
 	
 	private void registerNetworkStateChangeReceiver(){
 		if(!registeredNetworkStateChangeReceiver.get()){
 			IntentFilter networkStateChangeFilter = new IntentFilter();
 			networkStateChangeFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 			registerReceiver(networkStateChangeListener, networkStateChangeFilter);
 			Log.d("Aptoide-ServiceData", "networkStateChangeListener - registered as receiver");
 			registeredNetworkStateChangeReceiver.set(true);
 		}
 	}
 	
 	private void unregisterNetworkStateChangeReceiver(){
 		if(registeredNetworkStateChangeReceiver.get()){
 			unregisterReceiver(networkStateChangeListener);
 			Log.d("Aptoide-ServiceData", "networkStateChangeListener - unregistered as receiver");
 			registeredNetworkStateChangeReceiver.set(false);
 		}
 	}
 	
 	
 	public ViewDisplayListsDimensions getDisplayListsDimensions(){
 		return displayListsDimensions;
 	}
 	
 	public String getTag() {
 		return TAG;
 	}
 
 
 	public ManagerPreferences getManagerPreferences() {
 		return managerPreferences;
 	}
 	
 	public ManagerSystemSync getManagerSystemSync() {
 		return managerSystemSync;
 	}	
 	
 	public ManagerDatabase getManagerDatabase() {
 		return managerDatabase;
 	}	
 	
 	public ManagerDownloads getManagerDownloads() {
 		return managerDownloads;
 	}	
 	
 	public ManagerUploads getManagerUploads() {
 		return managerUploads;
 	}
 	
 	public ManagerCache getManagerCache() {
 		return managerDownloads.getManagerCache();
 	}
 
 	public ManagerNotifications getManagerNotifications() {
 		return managerNotifications;
 	}
 
 	public ManagerXml getManagerXml(){
 		return managerXml;
 	}
 
 	
 	public boolean isInsertingDataInDb(){
 		return (reposInserting.size() > 0 || syncingInstalledApps.get());
 	}
 
 
 	@Override
 	public void onCreate() {
 	    if(!isRunning){
 
 //	    	splash();
 	    	
 	    	managerDatabase = new ManagerDatabase(this);
 			managerPreferences = new ManagerPreferences(this);
 			managerSystemSync = new ManagerSystemSync(this);
 			managerNotifications = new ManagerNotifications(this);
 			managerDownloads = new ManagerDownloads(this);
 			managerUploads = new ManagerUploads(this);
 			managerXml = new ManagerXml(this);
 			
 	    	
 	    	reposInserting = new ArrayList<Integer>();
 			
 			waitingMyapps = new ArrayList<ViewMyapp>();
 			waitingMyappRepos = new ViewDisplayListRepos(0);
 			waitingSelfUpdate = null;
 	    	
 	    	cachedThreadPool = Executors.newCachedThreadPool();
 //	    	scheduledThreadPool = Executors.newScheduledThreadPool(Constants.MAX_PARALLEL_SERVICE_REQUESTS);
 	    	
 			aptoideClients = new HashMap<EnumServiceDataCallback, AIDLAptoideInterface>();
 			appInfoClients = new HashMap<Integer, AIDLAppInfo>();
 			
 			appInfoComments = new HashMap<Integer, ViewDisplayListComments>();
 
 			
 			syncingInstalledApps = new AtomicBoolean(false);
 			addingRepo = new AtomicBoolean(false);
 			automaticBackupOn = new AtomicBoolean(false);
 			
 			registerInstalledAppsChangeReceiver();
 			
 //			registeredNetworkStateChangeReceiver = new AtomicBoolean(false);
 //			if(managerPreferences.isAutomaticInstallOn()){
 //				registerNetworkStateChangeReceiver();
 //			}
 			
 			checkForSelfUpdate();
 			
 //			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 			
 			isRunning = true;
 			Log.d("Aptoide ServiceData", "Service started");
 
 	    }
 		super.onCreate();
 	}
 
 
 	@Override
 	public void onDestroy() {
 		if(!addingRepo.get() && !automaticBackupOn.get()){
 			managerNotifications.destroy();
 			try {
 				unregisterReceiver(installedAppsChangeListener);
 			} catch (Exception e) {	}
 	//		unregisterNetworkStateChangeReceiver();
 			cachedThreadPool.shutdownNow();
 	//		Toast.makeText(this, R.string.aptoide_stopped, Toast.LENGTH_LONG).show();
 			stopSelf();
 			Log.d("Aptoide ServiceData", "Service stopped");
 			super.onDestroy();
 		}else{
 			Log.d("Aptoide ServiceData", "Deferring Service stopping");
 		}
 	}
 	
 	private void shouldIShutDown(){
 		Log.d("Aptoide ServiceData", "Should I stop?");
 		if(aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST) == null){
 			onDestroy();
 		}
 	}
 
 	
 	public void checkForSelfUpdate(){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				try {
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					Log.d("Aptoide-ServiceData", "Checking for self-update");
 					ViewCache cache;
 					cache = managerDownloads.downloadLatestVersionInfo();
 					managerXml.latestVersionInfoParse(cache);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				}  catch (AptoideExceptionDownload e2) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e2.printStackTrace();
 				} catch (Exception e3){
 					e3.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void parsingLatestVersionInfoFinished(final ViewLatestVersionInfo latestVersionInfo){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				int currentVersion = managerSystemSync.getAptoideVersionInUse();
 				if( currentVersion < latestVersionInfo.getVersionCode()){
 					Log.d("Aptoide-ServiceData", "Using version "+currentVersion+", suggest update to "+latestVersionInfo.getVersionCode()+"!");
 					waitingSelfUpdate = latestVersionInfo;
 					handleSelfUpdate();
 				}else{
 					Log.d("Aptoide-ServiceData", "Using version "+currentVersion+", up to date!");
 				}
 			}
 		});
 	}
 	
 	public void rejectSelfUpdate(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					if(selfUpdateClient != null){
 						try {
 							selfUpdateClient.cancelUpdateActivity();
 						} catch (Exception e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					selfUpdateClient = null;
 					waitingSelfUpdate = null;
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void downloadSelfUpdate(final ViewLatestVersionInfo latesVersionInfo){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					
 					String aptoide = getApplicationInfo().name;
 					int aptoideHash = (aptoide+"|"+latesVersionInfo.getVersionCode()).hashCode();
 					ViewDownload download = getManagerDownloads().prepareApkDownload( aptoideHash, aptoide
 										, latesVersionInfo.getRemotePath(), latesVersionInfo.getSize(), latesVersionInfo.getMd5sum());
 					ViewCache apk = managerDownloads.downloadApk(download);
 					AptoideLog.d(AptoideServiceData.this, "installing from: "+apk.getLocalPath());	
 					installApp(apk, aptoideHash);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				}catch (Exception e){
 					e.printStackTrace();
 				}
 				rejectSelfUpdate();
 			}
 		});		
 	}
 	
 	public int checkIfAnyReposInUse(){
 		AptoideLog.d(AptoideServiceData.this, "checking if any repos in use");
 		if(managerDatabase.anyReposInUse()){
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //					resetAvailableLists();
 					getDeltas(true);
 				}
 			});
 			return EnumAvailableAppsStatus.REPO_IN_USE.ordinal();
 		}else{
 			return EnumAvailableAppsStatus.NO_REPO_IN_USE.ordinal();
 		}
 		
 	}
 	
 	public void storeScreenDimensions(final ViewScreenDimensions screenDimensions){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setScreenDimensions(screenDimensions);
 				AptoideLog.d(AptoideServiceData.this, "Stored Screen Dimensions: "+managerPreferences.getScreenDimensions());
 			}
 		});
 	}
 	
 	public void clearIconCache(){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Clearing icon cache");
 				getManagerCache().clearIconCache();
 //				Toast.makeText(AptoideServiceData.this, getString(R.id.done), Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	public void clearApkCache(){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Clearing apk cache");
 				getManagerCache().clearApkCache();
 //				Toast.makeText(AptoideServiceData.this, getString(R.id.done), Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	public ViewSettings getSettings(){
 		ViewSettings settings = managerPreferences.getSettings();
 		AptoideLog.d(AptoideServiceData.this, "Getting settings: "+settings);
 		return settings;
 	}
 	
 	public ViewHwFilters getHwFilters(){
 		ViewHwFilters filters = managerSystemSync.getHwFilters();
 		AptoideLog.d(AptoideServiceData.this, "Getting hw filters: "+filters);
 		return filters;
 	}
 	
 	public void setHwFilter(final boolean on){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setHwFilter(on);
 				AptoideLog.d(AptoideServiceData.this, "Setting hw filter: "+on);
 			}
 		});
 	}
 	
 	public void setAgeRating(final EnumAgeRating rating){
 		if(!rating.equals(EnumAgeRating.unrecognized)){
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					managerPreferences.setAgeRating(rating);
 					AptoideLog.d(AptoideServiceData.this, "Setting age rating: "+rating);
 				}
 			});
 		}
 	}
 	
 	public void setAutomaticInstall(final boolean on){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setAutomaticInstall(on);
 				AptoideLog.d(AptoideServiceData.this, "Setting automatic install: "+on);
 				if(on){
 //					registerNetworkStateChangeReceiver();
 					automaticBackupOn.set(true);
 				}else{
 					automaticBackupOn.set(false);
 //					unregisterNetworkStateChangeReceiver();
 				}
 			}
 		});
 	}
 	
 	public ViewIconDownloadPermissions getIconDownloadPermissions(){
 		ViewIconDownloadPermissions permissions = managerPreferences.getIconDownloadPermissions();
 		AptoideLog.d(AptoideServiceData.this, "Getting icon download permissions: "+permissions);
 		return permissions;
 	}
 	
 	public void setIconDownloadPermissions(final ViewIconDownloadPermissions iconDownloadPermissions){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setIconDownloadPermissions(iconDownloadPermissions);
 				AptoideLog.d(AptoideServiceData.this, "Setting icon download permissions: "+iconDownloadPermissions);
 			}
 		});
 	}
 	
 	public void syncInstalledApps(){
 		syncingInstalledApps.set(true);
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				managerDatabase.insertInstalledApplications(managerSystemSync.getInstalledApps());
 				AptoideLog.d(AptoideServiceData.this, "Sync'ed Installed Apps");				
 				syncingInstalledApps.set(false);
 				resetInstalledLists();
 				
 //				managerSystemSync.cacheInstalledIcons();
 			}
 		});
 	}
 	
 	public void repoInserted(){
 		delayedExecutionHandler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				try{
 					loginClient.repoInserted();
 				} catch (Exception e) { }
 			}
 		}, 1000);
 	}
 	
 	
 	public void refreshInstalledLists(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_INSTALLED_LIST).refreshInstalledDisplay(); 
 //					Looper.prepare();
 //					Toast.makeText(getApplicationContext(), "installed list now available in next -> tab", Toast.LENGTH_LONG).show();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	
 	public void resetInstalledLists(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_INSTALLED_LIST).newInstalledListDataAvailable(); 
 //					Looper.prepare();
 //					Toast.makeText(getApplicationContext(), "installed list now available in next -> tab", Toast.LENGTH_LONG).show();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 
 	
 	public void refreshAvailableDisplay(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).refreshAvailableDisplay();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void updateAvailableLists(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).newAvailableListDataAvailable();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void resetAvailableLists(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 				    	if( managerDatabase.getTotalAvailableApps(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating()) > Constants.MAX_APPLICATIONS_IN_STATIC_LIST_MODE){
 				        	AptoideLog.d(AptoideServiceData.this, "Switching Available List to dynamic");
 				    		switchAvailableListToDynamic();
 				    		loadingAvailableListData();
 				    	}
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).resetAvailableListData();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void switchAvailableListToStatic(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //					AptoideLog.d(AptoideServiceData.this, "switching to static available apps!");
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).switchAvailableToStaticList();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void switchAvailableListToDynamic(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //					AptoideLog.d(AptoideServiceData.this, "switching to dynamic available apps!");
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).switchAvailableToDynamicList();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void noAvailableListData(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "No available apps!");
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).noAvailableListDataAvailable();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void loadingAvailableListData(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "Loading available apps!");
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).loadingAvailableListDataAvailable();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void loadingAvailableProgressSetCompletionTarget(final int progressCompletionTarget){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "Loading available apps progress completion target: "+progressCompletionTarget);
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).loadingAvailableListProgressSetCompletionTarget(progressCompletionTarget);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void loadingAvailableProgressUpdate(final int currentProgress){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //				AptoideLog.d(AptoideServiceData.this, "Loading available apps progress update: "+currentProgress);
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).loadingAvailableListProgressUpdate(currentProgress);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void loadingAvailableProgressIndeterminate(){
 		try{
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "Loading available apps progress indeterminate");
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).loadingAvailableListProgressIndeterminate();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		}catch (Exception e) { }
 	}
 	
 	
 	
 	
 	
 
 	
 	public void uploadingProgressSetCompletionTarget(final int appHashid, final int progressCompletionTarget){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "Uploading app: "+appHashid+" progress completion target: "+progressCompletionTarget);
 					try {
 						uploadClient.uploadingProgressSetCompletionTarget(appHashid, progressCompletionTarget);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void uploadingProgressUpdate(final int appHashid, final int currentProgress){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //				AptoideLog.d(AptoideServiceData.this, "Loading available apps progress update: "+currentProgress);
 					try {
 						uploadClient.uploadingProgressUpdate(appHashid, currentProgress);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void uploadingProgressIndeterminate(final int appHashid){
 		try{
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					AptoideLog.d(AptoideServiceData.this, "Uploading app: "+appHashid+" progress indeterminate");
 					try {
 						uploadClient.uploadingProgressIndeterminate(appHashid);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}	
 				}
 			});
 		}catch (Exception e) { }
 	}
 	
 	
 	
 	
 	
 	
 	public void updateReposLists(){
 //		try {
 //			cachedThreadPool.execute(new Runnable() {
 //				@Override
 //				public void run() {
 //					try {
 //						reposInfoClient.updateReposBasicInfo();
 //					} catch (RemoteException e) {
 //						// TODO Auto-generated catch block
 //						e.printStackTrace();
 //					}	
 //				}
 //			});
 //		} catch (Exception e) { }
 	}
 	
 //	public void insertedRepo(final int repoHashid){
 //		cachedThreadPool.execute(new Runnable() {
 //			@Override
 //			public void run() {
 //				try {
 //					reposInfoClient.insertedRepo(repoHashid);
 //				} catch (RemoteException e) {
 //					// TODO Auto-generated catch block
 //					e.printStackTrace();
 //				}	
 //			}
 //		});
 //	}
 	
 	public boolean areAnyReposInUse(){
 		AptoideLog.d(AptoideServiceData.this, "Are any repos in use");
 		return managerDatabase.anyReposInUse();
 	}
 	
 
 	public ViewDisplayListRepos getRepos(){
 		AptoideLog.d(AptoideServiceData.this, "Getting Repos");
 		return managerDatabase.getReposDisplayInfo();
 	}
 	
 	
 	public boolean repoIsManaged(int repoHashid){
 		return managerDatabase.isRepoManaged(repoHashid);
 	}
 	
 	public void removeRepo(final int repoHashid){
 		if(reposInserting.contains(repoHashid)){
 			reposInserting.remove(Integer.valueOf(repoHashid));
 		}
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Removing repo: "+repoHashid);
 				managerDatabase.removeRepository(repoHashid);
 				resetAvailableLists();				
 			}
 		});
 	}
 	
 	public void toggleInUseRepo(final int repoHashid, final boolean inUse){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Setting repo: "+repoHashid+" inUse: "+inUse);
 				managerDatabase.toggleRepositoryInUse(repoHashid, inUse);
 				resetAvailableLists();
 				if(inUse){
 					getDelta(repoHashid);
 				}
 			}
 		});
 	}
 	
 	public void removeLogin(final int repoHashid){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Setting repo to private: "+repoHashid);
 				managerDatabase.removeLogin(repoHashid);
 			}
 		});
 	}
 	
 	public void updateLogin(final ViewRepository repo){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "updating repo's login: "+repo);
 				managerDatabase.updateLogin(repo);
 			}
 		});
 	}
 	
 	
 	public void getDelta(final int repoHashid){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				try{
 //					ViewRepository repository = managerDatabase.getRepoIfUpdateNeeded(repoHashid);
 					ViewRepository repository = managerDatabase.getRepository(repoHashid);
 					AptoideLog.d(AptoideServiceData.this, "updating repo: "+repository);
 					if(repository != null){
 						reposInserting.add(repoHashid);
 						if(!managerDownloads.isConnectionAvailable()){
 							throw new AptoideExceptionConnectivity();
 						}
 						if(!getManagerCache().isFreeSpaceInSdcard()){
 							throw new AptoideExceptionSpaceInSDCard();
 						}
 						ViewCache cache = null;
 						if(reposInserting.contains(repoHashid)){
 							cache = managerDownloads.startRepoDeltaDownload(repository);
 						}
 	//					Looper.prepare();
 	//					Toast.makeText(getApplicationContext(), "finished downloading bare list", Toast.LENGTH_LONG).show();
 						if(reposInserting.contains(repoHashid)){
 							managerXml.repoDeltaParse(repository, cache);
 						}
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void getDeltas(final boolean force){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				ArrayList<ViewRepository> repositorys = managerDatabase.getReposNeedingUpdate(force);
 				for (ViewRepository repository : repositorys) {
 					AptoideLog.d(AptoideServiceData.this, "checking updates for repo: "+repository.getRepoName());
 					try{
 						int repoHashid = repository.getHashid();
 						if(repository != null){
 							reposInserting.add(repoHashid);
 							if(!managerDownloads.isConnectionAvailable()){
 								throw new AptoideExceptionConnectivity();
 							}
 							if(!getManagerCache().isFreeSpaceInSdcard()){
 								throw new AptoideExceptionSpaceInSDCard();
 							}
 							ViewCache cache = null;
 							if(reposInserting.contains(repoHashid)){
 								cache = managerDownloads.startRepoDeltaDownload(repository);
 							}
 							if(reposInserting.contains(repoHashid)){
 								managerXml.repoDeltaParse(repository, cache);
 							}
 						}
 					} catch (AptoideExceptionConnectivity e) {
 						toastHandler.sendEmptyMessage(R.string.no_network_connection);
 						e.printStackTrace();
 					} catch (AptoideExceptionSpaceInSDCard e) {
 //						toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 						e.printStackTrace();
 					} catch (AptoideExceptionDownload e) {
 //						toastHandler.sendEmptyMessage(R.string.download_failed);
 						e.printStackTrace();
 					} catch (Exception e){
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 	}
 	
 	public void parsingRepoDeltaFinished(ViewRepository repository, int repoSizeDifferential){
 		if(reposInserting.contains(repository.getHashid())){
 			reposInserting.remove(Integer.valueOf(repository.getHashid()));
 			if(repoSizeDifferential != 0){
 				if(repoSizeDifferential > 0){
 					parsingRepoIconsFinished(repository);
 				}
 				resetAvailableLists();AptoideLog.d(AptoideServiceData.this, "parsing repo delta finished");
 				resetInstalledLists();
 			}
 			
 	//		insertedRepo(repository.getHashid());
 		}
 	}
 		
 	
 	
 	public void addRepoBare(final ViewRepository originalRepository){
 		addingRepo.set(true);
 		startedLoadingRepos();
 		reposInserting.add(originalRepository.getHashid());
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				ViewRepository repository = originalRepository;
 //				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = null;
 					if(reposInserting.contains(repository.getHashid())){
 						loadingAvailableProgressIndeterminate();
 						loadingAvailableListData();
 						cache = managerDownloads.startRepoBareDownload(repository);
 					}
 	//				Looper.prepare();
 	//				Toast.makeText(getApplicationContext(), "finished downloading bare list", Toast.LENGTH_LONG).show();
 					if(reposInserting.contains(repository.getHashid())){
 						managerXml.repoBareParse(repository, cache);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					resetAvailableLists();
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					resetAvailableLists();
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					resetAvailableLists();
 					e.printStackTrace();
 				} catch (Exception e){
 					resetAvailableLists();
 					e.printStackTrace();
 				}
 			}
 		});
 		
 	}
 	
 	public void parsingRepoBareFinished(ViewRepository repository){
 //		if(reposInserting.size() == 1){	// If contains only the currently being parsed repo
 			finishedLoadingRepos();
 //		}
 		if(reposInserting.contains(repository.getHashid())){
 //			if(managerPreferences.getShowApplicationsByCategory() ){//)|| repository.getSize() < getDisplayListsDimensions().getFastReset()){
 				resetAvailableLists();
 //			}
 //			insertedRepo(repository.getHashid());
 //			addRepoStats(repository);
 			addRepoIconsInfo(repository);
 			resetInstalledLists();AptoideLog.d(AptoideServiceData.this, "parsing repo bare finished!");
 		}
 	}
 	
 	public void addRepoStats(final ViewRepository repository){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = null;
 					if(reposInserting.contains(repository.getHashid())){
 						cache = managerDownloads.startRepoStatsDownload(repository);
 					}
 					if(reposInserting.contains(repository.getHashid())){	
 						managerXml.repoStatsParse(repository, cache);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					Toast.makeText(AptoideServiceData.this, R.string.no_network_connection, Toast.LENGTH_SHORT);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void parsingRepoStatsFinished(ViewRepository repository){
 		if(reposInserting.contains(repository.getHashid())){
 			updateAvailableLists();
 			addRepoIconsInfo(repository);
 //			addRepoDownload(repository);
 //			Toast.makeText(AptoideServiceData.this, "app stats available", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	
 	public void addRepoDownload(final ViewRepository repository){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = null;
 					if(reposInserting.contains(repository.getHashid())){
 						cache = managerDownloads.startRepoDownloadDownload(repository);
 					}
 					if(reposInserting.contains(repository.getHashid())){
 						managerXml.repoDownloadParse(repository, cache);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 		
 	}
 	
 	public void parsingRepoDownloadInfoFinished(ViewRepository repository){
 		
 	}
 	
 	
 	public void addRepoIconsInfo(final ViewRepository repository){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = null;
 					if(reposInserting.contains(repository.getHashid())){
 						cache = managerDownloads.startRepoIconDownload(repository);
 					}
 					if(reposInserting.contains(repository.getHashid())){
 						managerXml.repoIconParse(repository, cache);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void parsingRepoIconsFinished(ViewRepository repository){
 //		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 //		AptoideLog.d(AptoideServiceData.this, "parsing repo icons finished, reposInserting: "+reposInserting+" repo: "+repository);
 		if(reposInserting.contains(repository.getHashid())){
 			reposInserting.remove(Integer.valueOf(repository.getHashid()));
 			addingRepo.set(false);
 			getRepoIcons(new ViewDownloadStatus(repository, Constants.FIRST_ELEMENT, EnumDownloadType.ICON));
 		}
 	}
 	
 	public void getRepoIcons(final ViewDownloadStatus downloadStatus){
 
 		AptoideLog.d(AptoideServiceData.this, "getRepoIcons offset: "+downloadStatus.getOffset()+" repoSize: "+downloadStatus.getRepository().getSize());
 		if(downloadStatus.getRepository().getSize() < downloadStatus.getOffset()){
 //			refreshAvailableDisplay();
 //			resetAvailableLists();
 //			addingRepo.set(false);
 			shouldIShutDown();
 			return;
 		}else{
 //			if(downloadStatus.getOffset() >  Constants.FIRST_ELEMENT){
 //				refreshAvailableDisplay();
 //			}
 			
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 					try{
 						if(!managerDownloads.isPermittedConnectionAvailable(managerPreferences.getIconDownloadPermissions())){
 							throw new AptoideExceptionConnectivity();
 						}else{
 							if(!getManagerCache().isFreeSpaceInSdcard()){
 								throw new AptoideExceptionSpaceInSDCard();
 							}
 							AptoideLog.d(AptoideServiceData.this, "getting repo icons");
 		
 							managerDownloads.getRepoIcons(downloadStatus, managerDatabase.getIconsDownloadInfo(downloadStatus.getRepository(), downloadStatus.getOffset(), displayListsDimensions.getCacheSize()));
 							//TODO find some way to track global parsing completion status, probably in managerXml
 						}
 					} catch (AptoideExceptionConnectivity e) {
 						toastHandler.sendEmptyMessage(R.string.no_permitted_network_connection);
 						e.printStackTrace();
 					} catch (AptoideExceptionSpaceInSDCard e) {
 //						toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 						e.printStackTrace();
 					} catch (AptoideExceptionDownload e) {
 //						toastHandler.sendEmptyMessage(R.string.download_failed);
 						e.printStackTrace();
 					} catch (Exception e){
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 		
 	}
 	
 	
 //	public void addRepoDownloadsInfo(final ViewRepository repository){
 //		try{
 //
 //			new Thread(){
 //				public void run(){
 //					this.setPriority(Thread.MAX_PRIORITY);
 //					if(!managerDownloads.isConnectionAvailable()){
 //						AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
 //					}
 //					if(!getManagerCache().isFreeSpaceInSdcard()){
 //						//TODO raise exception
 //					}
 //					ViewCache cache = managerDownloads.startRepoAppDownloads(repository);
 //					
 //					managerXml.repoDownloadParse(repository, cache);
 //					//TODO find some way to track global parsing completion status, probably in managerXml
 //				}
 //			}.start();
 //
 //
 //		} catch(Exception e){
 //			/** this should never happen */
 //			//TODO handle exception
 //			e.printStackTrace();
 //		}
 //	}
 	
 	
 	public void fillAppInfo(final int appHashid){
 //		final ViewAppVersionRepository anyVersion = managerDatabase.getAppAnyVersionRepo(appHashid);
 //		if(anyVersion == null){
 //			updateAppInfo(appHashid, EnumServiceDataCallback.UPDATE_APP_INFO);
 //			AptoideLog.d(AptoideServiceData.this, "App not in any repo");
 //			return;
 //		}
 //		final ViewRepository repository = managerDatabase.getAppRepo(appHashid);
 //		
 //		if(!managerDownloads.isIconCached(appHashid)){
 //			scheduledThreadPool.execute(new Runnable() {
 //				@Override
 //				public void run() {
 //					ViewDownloadInfo downloadInfo = managerDatabase.getIconDownloadInfo(anyVersion.getRepository(), anyVersion.getAppHashid());
 //					if(downloadInfo != null){
 //						managerDownloads.getIcon(downloadInfo, anyVersion.getRepository().isLoginRequired(), anyVersion.getRepository().getLogin());
 //						updateAppInfo(appHashid, EnumServiceDataCallback.REFRESH_ICON);
 //					}
 //				}
 //			});
 //		}else{
 //			scheduledThreadPool.execute(new Runnable() {
 //				@Override
 //				public void run() {
 //					updateAppInfo(appHashid, EnumServiceDataCallback.REFRESH_ICON);
 //				}
 //			});
 //		}
 //		
 //		if(repository != null){
 //			addAppVersionInfo(repository, appHashid);
 //		}
 //		addRepoAppExtras(anyVersion.getRepository(), anyVersion.getAppHashid());
 	}
 	
 	
 //	public void addAppVersionInfo(ViewRepository repository, int appHashid){
 //		addRepoAppDownloadInfo(repository, appHashid);
 //		addRepoAppStats(repository, appHashid);		
 //		//TODO parallel get Comments
 //	}
 //	
 //	public void addAppVersionInfo(int appHashid){
 //		addAppVersionInfo(managerDatabase.getAppRepo(appHashid), appHashid);
 //	}
 	
 	public void addAppVersionInfo(final int appHashid,final int repoHashid){
 //		ViewRepository repository = managerDatabase.getRepository(repoHashid);
 //		addRepoAppDownloadInfo(repository, appHashid);
 //		addRepoAppStats(repository, appHashid);
 //		addRepoAppExtras(repository, appHashid);
 		//TODO parallel get Comments
 	}
 	
 	
 	public void addRepoAppDownloadInfo(final int appHashid, final int repoHashid){
 	
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				try{
 					int appFullHashid = (appHashid+"|"+repoHashid).hashCode();
 					if(!managerDatabase.isAppDownloadInfoPresent(appFullHashid)){
 						ViewRepository repository = managerDatabase.getRepository(repoHashid);
 						if(!managerDownloads.isConnectionAvailable()){
 							throw new AptoideExceptionConnectivity();
 						}
 						if(!getManagerCache().isFreeSpaceInSdcard()){
 							throw new AptoideExceptionSpaceInSDCard();
 						}
 						ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.DOWNLOAD);
 						
 						managerXml.repoAppDownloadParse(repository, cache, appHashid);
 					}else{
 						updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_DOWNLOAD_INFO);
 						AptoideLog.d(AptoideServiceData.this, "App downloadInfo present for:"+appFullHashid);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (AptoideExceptionDatabase e) {
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 					
 	}
 	
 	public void parsingIconFromDownloadInfoFinished(ViewIconInfo iconInfo, ViewRepository repository){
 		if(managerDownloads.isPermittedConnectionAvailable(getIconDownloadPermissions())){
 			ViewDownloadInfo downloadInfo = new ViewDownloadInfo(repository.getIconsPath()+iconInfo.getIconRemotePathTail(), Integer.toString(iconInfo.getAppFullHashid()), iconInfo.getAppFullHashid(), EnumDownloadType.ICON);
 			managerDownloads.getIcon(downloadInfo, repository.isLoginRequired(), repository.getLogin());
 //			updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.REFRESH_ICON);
 		}
 	}
 	
 	public void parsingRepoAppDownloadInfoFinished(ViewRepository repository, int appHashid){
 		int appFullHashid = (appHashid+"|"+repository.getHashid()).hashCode();
 		updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_DOWNLOAD_INFO);
 	}
 	
 	
 	public void addRepoAppStats(final int appHashid, final int repoHashid){
 
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				try{
 					ViewRepository repository = managerDatabase.getRepository(repoHashid);
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.STATS);
 					
 					managerXml.repoAppStatsParse(repository, cache, appHashid);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (AptoideExceptionDatabase e) {
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 					
 	}
 	
 	public void parsingRepoAppStatsFinished(ViewRepository repository, int appHashid){
 		int appFullHashid = (appHashid+"|"+repository.getHashid()).hashCode();
 		updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_STATS);
 	}
 	
 	
 	
 	public void addRepoAppExtras(final int appHashid, final int repoHashid){
 		
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				try{
 					int appFullHashid = (appHashid+"|"+repoHashid).hashCode();
 					if(!managerDatabase.isAppExtraInfoPresent(appFullHashid)){
 						ViewRepository repository = managerDatabase.getRepository(repoHashid);
 						if(!managerDownloads.isConnectionAvailable()){
 							throw new AptoideExceptionConnectivity();
 						}
 						if(!getManagerCache().isFreeSpaceInSdcard()){
 							throw new AptoideExceptionSpaceInSDCard();
 						}
 						ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.EXTRAS);
 						
 						managerXml.repoAppExtrasParse(repository, cache, appHashid);
 					}else{
 						updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_EXTRAS);
 						updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.REFRESH_SCREENS);
 						AptoideLog.d(AptoideServiceData.this, "App extra Info present for:"+appFullHashid);
 					}
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (AptoideExceptionDatabase e) {
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 					
 	}
 	
 	public void parsingRepoAppExtrasFinished(ViewRepository repository, int appHashid){
 		int appFullHashid = (appHashid+"|"+repository.getHashid()).hashCode();
 		updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_EXTRAS);
 		getAppScreens(repository, appHashid);
 	}
 	
 	public void getAppScreens(final ViewRepository repository, final int appHashid){
 		
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 //					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 					try{
 						if(!managerDownloads.isConnectionAvailable()){
 							throw new AptoideExceptionConnectivity();
 						}
 						if(!getManagerCache().isFreeSpaceInSdcard()){
 							throw new AptoideExceptionSpaceInSDCard();
 						}
 						managerDownloads.getAppScreens(repository, managerDatabase.getScreensDownloadInfo(repository, appHashid));
 
 					} catch (AptoideExceptionConnectivity e) {
 						toastHandler.sendEmptyMessage(R.string.no_network_connection);
 						e.printStackTrace();
 					} catch (AptoideExceptionSpaceInSDCard e) {
 //						toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 						e.printStackTrace();
 					} catch (AptoideExceptionDownload e) {
 //						toastHandler.sendEmptyMessage(R.string.download_failed);
 						e.printStackTrace();
 					} catch (AptoideExceptionDatabase e) {
 						e.printStackTrace();
 					} catch (Exception e){
 						e.printStackTrace();
 					}
 				}
 			});
 	}
 	
 	public void gettingAppScreensFinished(int appHashid){
 		AptoideLog.d(AptoideServiceData.this, "Finished getting screens - appHashid: "+appHashid);
 		updateAppInfo(appHashid, Constants.EMPTY_INT, EnumServiceDataCallback.REFRESH_SCREENS);
 	}
 	
 	
 	public void retrieveRepoAppComments(final int appHashid, final int repoHashid){
 		int appFullHashid = (appHashid+"|"+repoHashid).hashCode();
 		if(appInfoComments.containsKey(appFullHashid)){
 			if(appInfoComments.get(appFullHashid) != null && !(appInfoComments.get(appFullHashid).size()>0)){
 				updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_COMMENTS);
 			}
 			return;
 		}
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 //				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
 				try{
 					ViewRepository repository = managerDatabase.getRepository(repoHashid);
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.COMMENTS);
 					
 					managerXml.repoAppCommentsParse(repository, cache, appHashid);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 //					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 //					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				} catch (AptoideExceptionDatabase e) {
 					e.printStackTrace();
 				} catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void parsingRepoAppCommentsFinished(int repoHashid, int appHashid, ViewDisplayListComments comments){
 		int appFullHashid = (appHashid+"|"+repoHashid).hashCode();
 		appInfoComments.put(appFullHashid, comments);
 		updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_COMMENTS);
 	}
 	
 	public void parsingRepoAppCommentsError(int repoHashid, int appHashid, String Error){
 //		updateAppInfo(appHashid, appFullHashid, EnumServiceDataCallback.UPDATE_APP_NO_COMMENTS);
 	}
 	
 	
 	public void updateAppInfo(int appHashid, int appFullHashid, EnumServiceDataCallback callBack){
 		AptoideLog.d(AptoideServiceData.this, "appInfo AIDL : "+appInfoClients.get(appHashid));
 		try {
 			switch (callBack) {
 				case REFRESH_ICON:
 					appInfoClients.get(appHashid).refreshIcon();
 					break;
 					
 //				case UPDATE_APP_INFO:
 //					appInfoClients.get(appHashid).newAppInfoAvailable(appFullHashid);
 //					break;
 					
 				case UPDATE_APP_DOWNLOAD_INFO:
 					appInfoClients.get(appHashid).newAppDownloadInfoAvailable(appFullHashid);
 					break;
 					
 				case UPDATE_APP_STATS:
 					appInfoClients.get(appHashid).newStatsInfoAvailable(appFullHashid);
 					break;
 					
 				case UPDATE_APP_EXTRAS:
 					appInfoClients.get(appHashid).newExtrasAvailable(appFullHashid);
 					break;
 					
 				case UPDATE_APP_COMMENTS:
 					appInfoClients.get(appHashid).newCommentsAvailable(appFullHashid);
 					break;
 					
 				case REFRESH_SCREENS:
 					appInfoClients.get(appHashid).refreshScreens();
 					break;
 					
 				default:
 					break;
 			}
 			
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public ViewDisplayAppVersionsInfo getAppInfo(int appHashid){
 		AptoideLog.d(AptoideServiceData.this, "Getting App Versions Info: "+appHashid);
 		return managerDatabase.getAppDisplayInfo(appHashid);
 	}
 	
 	public int getAppVersionDownloadSize(int appFullHashid){
 		return managerDatabase.getAppVersionDownloadSize(appFullHashid);
 	}
 	
 	public ViewDisplayAppVersionStats getAppStats(int appFullHashid){
 		return managerDatabase.getAppVersionStats(appFullHashid);
 	}
 	
 	public ViewDisplayAppVersionExtras getAppExtras(int appFullHashid){
 		return managerDatabase.getAppVersionExtras(appFullHashid);
 	}
 	
 	public ViewDisplayListComments getAppComments(int appFullHashid){
 		return appInfoComments.remove(Integer.valueOf(appFullHashid));
 	}
 
 	public String getServerToken() {
 		return managerPreferences.getToken();	
 	}
 	
 	public ViewServerLogin getServerLogin(){
 		ViewLogin login = managerPreferences.getServerLogin();
 		ViewServerLogin serverLogin = new ViewServerLogin(login.getUsername(), login.getPassword());
 		ViewRepository repoInUse = managerDatabase.getRepoInUse();
 		serverLogin.setRepoName(repoInUse.getRepoName());
 		if(repoInUse.isLoginRequired()){
 			serverLogin.setRepoPrivate(repoInUse.getLogin().getUsername(), repoInUse.getLogin().getPassword());
 		}
 		return serverLogin;
 	}
 
 	public int serverLoginInsertRepo(ViewServerLogin serverLogin){
 		EnumServerLoginStatus repoConnectionStatus;
 		ViewRepository repoInUse = null;
 		if(managerDatabase.anyReposInUse()){
 			try {
 				repoInUse = managerDatabase.getRepoInUse();
 			} catch (AptoideExceptionDatabase e) {
 				Log.d("AptoideAppsBakup-ServiceData", e.getMessage());
 				repoInUse = null;
 			}
 		}
 		if(repoInUse != null && (repoInUse.getUri().equals(serverLogin.getRepoUri()) 
 				&& (!repoInUse.isLoginRequired() || 
 						( repoInUse.getLogin().getUsername().equals(serverLogin.getPrivUsername())
 						&& repoInUse.getLogin().getPassword().equals(serverLogin.getPrivPassword()) )))){
 			repoConnectionStatus = EnumServerLoginStatus.SUCCESS;
 			repoInserted();
 		}else{
 
 			repoConnectionStatus = getManagerDownloads().checkServerConnection(serverLogin);
 			if(repoConnectionStatus == EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE){
 				repoConnectionStatus = getManagerDownloads().checkServerConnection(serverLogin);
 			}
 			if(repoConnectionStatus == EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE){
 				repoConnectionStatus = getManagerDownloads().checkServerConnection(serverLogin);
 			}
 			AptoideLog.d(AptoideServiceData.this, "repoConnection status: "+repoConnectionStatus);
 			if(repoConnectionStatus == EnumServerLoginStatus.SUCCESS){
 				if( repoInUse == null ){
 					ViewRepository dormentRepo;
 					try {
 						dormentRepo = managerDatabase.getRepository(serverLogin.getRepoUri().hashCode());
 						if(dormentRepo.getUri().equals(serverLogin.getRepoUri()) 
 								&& (!dormentRepo.isLoginRequired() ||
 										( dormentRepo.getLogin().getUsername().equals(serverLogin.getPrivUsername())
 										&& dormentRepo.getLogin().getPassword().equals(serverLogin.getPrivPassword()) ))){
 							AptoideLog.d(AptoideServiceData.this, "reactivating repo: "+dormentRepo.getUri());
 							managerDatabase.toggleRepositoryInUse(dormentRepo.getHashid(), true);
 							repoInserted();
 //							resetAvailableLists();
 							resetInstalledLists();
 							getDelta(dormentRepo.getHashid());
 						}else{
 							throw new AptoideExceptionDatabase("requested repo previously unknown!");
 						}
 					} catch (Exception e) {
 						try {
 							AptoideLog.d(AptoideServiceData.this, e.getMessage());
 						} catch (Exception e1) { }
 						ViewRepository repo = new ViewRepository(serverLogin.getRepoUri());
 						if(serverLogin.isRepoPrivate()){
 							repo.setLogin(new ViewLogin(serverLogin.getPrivUsername(), serverLogin.getPrivPassword()));
 						}
 						AptoideLog.d(AptoideServiceData.this, "inserting repo: "+repo);
 						addRepoBare(repo);
 					}
 				}else if( !repoInUse.getUri().equals(serverLogin.getRepoUri()) ){
 					AptoideLog.d(AptoideServiceData.this, "deactivating repo: "+serverLogin.getRepoName());
 					managerDatabase.toggleRepositoryInUse(repoInUse.getHashid(), false);
 
 					ViewRepository dormentRepo;
 					try {
 						dormentRepo = managerDatabase.getRepository(serverLogin.getRepoUri().hashCode());
 						if(dormentRepo != null && (dormentRepo.getUri().equals(serverLogin.getRepoUri()) 
 								&& (!dormentRepo.isLoginRequired() ||
 										( dormentRepo.getLogin().getUsername().equals(serverLogin.getPrivUsername())
 										&& dormentRepo.getLogin().getPassword().equals(serverLogin.getPrivPassword()) )))){
 							AptoideLog.d(AptoideServiceData.this, "reactivating repo: "+dormentRepo.getUri());
 							managerDatabase.toggleRepositoryInUse(dormentRepo.getHashid(), true);
 							repoInserted();
 //							resetAvailableLists();
 							resetInstalledLists();
 							getDelta(dormentRepo.getHashid());
 						}else{
 							throw new AptoideExceptionDatabase("requested repo previously unknown!");
 						}
 					} catch (Exception e) {
 						try {
 							AptoideLog.d(AptoideServiceData.this, e.getMessage());
 						} catch (Exception e1) { }
 						ViewRepository repo = new ViewRepository(serverLogin.getRepoUri());
 						if(serverLogin.isRepoPrivate()){
 							repo.setLogin(new ViewLogin(serverLogin.getPrivUsername(), serverLogin.getPrivPassword()));
 						}
 						AptoideLog.d(AptoideServiceData.this, "inserting repo: "+repo);
 						addRepoBare(repo);
 					}
 				}else if( !repoInUse.getLogin().getUsername().equals(serverLogin.getPrivUsername())
 						|| !repoInUse.getLogin().getPassword().equals(serverLogin.getPrivPassword()) ){
 					repoInUse.setLogin(new ViewLogin(serverLogin.getPrivUsername(), serverLogin.getPrivPassword()));
 					AptoideLog.d(AptoideServiceData.this, "updating repo's login: "+repoInUse);
 					managerDatabase.updateLogin(repoInUse);
 					repoInserted();
 				}
 			}else{
 				managerPreferences.clearServerLogin();
 			}
 
 		}
 		return repoConnectionStatus.ordinal();
 	}
 	
 	public int serverLoginCreate(ViewServerLogin serverLogin) { //TODO refactor enums
 		EnumServerLoginCreateStatus loginCreateStatus;
 //		if(!addingRepo.get()){
 			ViewLogin storedLogin = managerPreferences.getServerLogin();
 			AptoideLog.d(AptoideServiceData.this, "serverLoginCreate stored: "+storedLogin);
 			AptoideLog.d(AptoideServiceData.this, "serverLoginCreate received: "+serverLogin);
 			if(serverLogin.getUsername().equals(storedLogin.getUsername()) && serverLogin.getPasshash().equals(storedLogin.getPassword())){
 				loginCreateStatus = EnumServerLoginCreateStatus.SUCCESS;
 			}else{
 				loginCreateStatus = getManagerUploads().loginCreate(serverLogin);
 			}
 //		}else{
 //			loginCreateStatus = EnumServerLoginCreateStatus.PREVIOUS_LOGIN_STILL_FINISHING_UP;
 //		}
 		AptoideLog.d(AptoideServiceData.this, "serverLoginCreate status: "+loginCreateStatus);
 		return loginCreateStatus.ordinal();
 	}
 	
 	public void serverLoginAfterCreate(final ViewServerLogin serverLogin){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).switchAvailableToWaitingOnServer();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 		    	Log.d("Aptoide-LoginAfterCreate", "Trying to Login with: "+serverLogin);
 		    	
 		    	if( getManagerUploads().login(serverLogin) == EnumServerLoginStatus.SUCCESS ){
 		    		repoInserted();
 		        	String token = managerPreferences.getToken();
 					if(EnumServerLoginStatus.reverseOrdinal(serverLoginInsertRepo(serverLogin)) == EnumServerLoginStatus.REPO_SERVICE_UNAVAILABLE){
 						managerPreferences.setServerInconsistentStore(serverLogin, token);
 						delayedExecutionHandler.postDelayed(new Runnable() {
 							public void run() {
 								try {
 									serverLoginAfterCreate(serverLogin);
 								} catch (Exception e) { }
 							}
 						}, 15000);
 					}
 		    	}else{
 		    		delayedExecutionHandler.postDelayed(new Runnable() {
 		    			public void run() {
 		    				try {
 		    					serverLoginAfterCreate(serverLogin);
 		    				} catch (Exception e) { }
 		    			}
 		    		}, 15000);
 		    	}
 			}
 		});
 	}
 
 	public int serverLogin(ViewServerLogin serverLogin) {
 		EnumServerLoginStatus loginStatus;
 //		if(!addingRepo.get()){
 			ViewLogin storedLogin = managerPreferences.getServerLogin();
 			AptoideLog.d(AptoideServiceData.this, "serverLogin stored: "+storedLogin);
 			AptoideLog.d(AptoideServiceData.this, "serverLogin received: "+serverLogin);
 			if(serverLogin.getUsername().equals(storedLogin.getUsername()) && serverLogin.getPasshash().equals(storedLogin.getPassword())){
 				loginStatus = EnumServerLoginStatus.SUCCESS;
 			}else{
 				loginStatus = getManagerUploads().login(serverLogin);
 			}
 //		}else{
 //			loginStatus = EnumServerLoginStatus.PREVIOUS_LOGIN_STILL_FINISHING_UP;
 //		}
 		AptoideLog.d(AptoideServiceData.this, "serverLogin status: "+loginStatus);
 		if(loginStatus != EnumServerLoginStatus.SUCCESS){
 			return loginStatus.ordinal();
 		}else{
 			return serverLoginInsertRepo(serverLogin);
 		}
 		
 	}
 	
 	public void clearServerLogin(){
 		if(!addingRepo.get()){
 //		if(reposInserting.size() > 0){
 			managerPreferences.clearServerLogin();
 			try {
 				ViewRepository repoInUse = managerDatabase.getRepoInUse();
 				AptoideLog.d(AptoideServiceData.this, "disabling repo: "+repoInUse);
 				managerDatabase.toggleRepositoryInUse(repoInUse.getHashid(), false);
 				switchAvailableListToStatic();
 //				resetAvailableLists();
 				resetInstalledLists();
 			} catch (Exception e) {
 				AptoideLog.d(AptoideServiceData.this, "already cleared server login");
 			}
 		}else{
 			AptoideLog.d(AptoideServiceData.this, getString(R.string.updating_repo_please_wait));
 //			Toast.makeText(getApplicationContext(), getResources().getString(R.string.updating_repo_please_wait), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public int addAppVersionLike(String repoName, int appHashid, boolean like) {
 		return getManagerUploads().addAppVersionLike(repoName, appHashid, like).ordinal();
 	}
 
 	public int addAppVersionComment(String repoName, int appHashid, String commentBody, String subject, long answerTo) {
 		return getManagerUploads().addAppVersionComment(repoName, appHashid, commentBody, subject, answerTo).ordinal();
 	}
 	
 	
 	
 	public boolean getShowApplicationsByCategory(){
 		return managerPreferences.getShowApplicationsByCategory();
 	}
 	
 	public void setShowApplicationsByCategory(final boolean byCategory){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setShowApplicationsByCategory(byCategory);
 			}
 		});
 	}
 	
 	
 	
 	public boolean getShowSystemApps(){
 		return managerPreferences.getShowSystemApplications();
 	}
 	
 	public void setShowSystemApps(final boolean show){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setShowSystemApplications(show);
 				resetInstalledLists();
 			}
 		});
 	}
 	
 	
 	
 	public int getTotalAvailableApps(){
 		AptoideLog.d(AptoideServiceData.this, "Getting Total Available Apps");
 		return managerDatabase.getTotalAvailableApps(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating());
 	}
 	
 	public int getTotalAvailableAppsInCategory(int categoryHashid){
 		AptoideLog.d(AptoideServiceData.this, "Getting Total Available Apps in category"+categoryHashid);
 		return managerDatabase.getTotalAvailableApps(categoryHashid, managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating());
 	}
 	
 	public ViewDisplayCategory getCategories(){
 		AptoideLog.d(AptoideServiceData.this, "Getting Categories");
 		return managerDatabase.getCategoriesDisplayInfo(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating());
 	}
 	
 	public int getAppsSortingPolicy(){
 		return managerPreferences.getAppsSortingPolicy();
 	}
 	
 	public void finishedLoadingRepos(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).finishedLoadingRepos();
 //					searchClients //TODO implement sort blocking in search when loading repo from bare
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void startedLoadingRepos(){
 		try {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						aptoideClients.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).startedLoadingRepos();
 //					searchClients //TODO implement sort blocking in search when loading repo from bare
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 		} catch (Exception e) { }
 	}
 	
 	public void setAppsSortingPolicy(final int sortingPolicy){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerPreferences.setAppsSortingPolicy(sortingPolicy);
 				resetInstalledLists();
 //				resetAvailableLists();
 				AptoideLog.d(AptoideServiceData.this, "setting sorting policy to: "+EnumAppsSorting.reverseOrdinal(sortingPolicy));
 			}
 		});
 	}
 	
 	
 	public ViewDisplayListApps getInstalledApps(){
 		AptoideLog.d(AptoideServiceData.this, "Getting Installed Apps");
 		return managerDatabase.getInstalledAppsDisplayInfo(EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()), managerPreferences.getShowSystemApplications());
 	}
 	
 	public ViewDisplayListApps getAllAvailableApps(){
 		AptoideLog.d(AptoideServiceData.this, "Getting All Available Apps");
 		return managerDatabase.getAvailableAppsDisplayInfo(0, 0, managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating(), EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()));
 	}
 	
 	public ViewDisplayListApps getAvailableApps(int offset, int range){
 		AptoideLog.d(AptoideServiceData.this, "Getting Available Apps");
 		return managerDatabase.getAvailableAppsDisplayInfo(offset, range, managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating(), EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()));
 	}
 	
 	public ViewDisplayListApps getAvailableApps(int offset, int range, int categoryHashid){
 		AptoideLog.d(AptoideServiceData.this, "Getting Available Apps for category: "+categoryHashid);
 		return managerDatabase.getAvailableAppsDisplayInfo(offset, range, categoryHashid, managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating(), EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()));
 	}
 	
 	public ViewDisplayListApps getUpdatableApps(){
 		AptoideLog.d(AptoideServiceData.this, "Getting Updatable Apps");
 		return managerDatabase.getUpdatableAppsDisplayInfo(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating(), EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()));
 	}
 	
 	
 	public void updateAll(){
 		AptoideLog.d(AptoideServiceData.this, "Updating All Updatable Apps");
 		ViewListAppsDownload appsDownload = managerDatabase.getUpdatableAppsDownloadInfo(managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating());
 		
 		for(final ViewDownload download : appsDownload.getDownloadsList()) {
 			cachedThreadPool.execute(new Runnable() {
 				@Override
 				public void run() {
 					try{
 						if(!managerDownloads.isConnectionAvailable()){
 							throw new AptoideExceptionConnectivity();
 						}
 						if(!getManagerCache().isFreeSpaceInSdcard()){
 							throw new AptoideExceptionSpaceInSDCard();
 						}
 						ViewCache apk = managerDownloads.downloadApk(download);
 						AptoideLog.d(AptoideServiceData.this, "installing from: "+apk.getLocalPath());	
 						installApp(apk, download.getNotification().getTargetsHashid());
 					} catch (AptoideExceptionConnectivity e) {
 						toastHandler.sendEmptyMessage(R.string.no_network_connection);
 						e.printStackTrace();
 					} catch (AptoideExceptionSpaceInSDCard e) {
 						toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 						e.printStackTrace();
 					} catch (AptoideExceptionDownload e) {
 						toastHandler.sendEmptyMessage(R.string.download_failed);
 						e.printStackTrace();
 					}catch (Exception e){
 						e.printStackTrace();
 					}
 				}
 			});
 		}
 		for(Entry<Integer, ArrayList<Integer>> repoAppsList : appsDownload.getNoInfoMap().entrySet()){
 			for (Integer appHashidValue : repoAppsList.getValue()) {
 				final int repoHashid = repoAppsList.getKey();
 				final int appHashid = appHashidValue;
 				cachedThreadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						try{
 							ViewRepository repository = managerDatabase.getRepository(repoHashid);
 							if(!managerDownloads.isConnectionAvailable()){
 								throw new AptoideExceptionConnectivity();
 							}
 							if(!getManagerCache().isFreeSpaceInSdcard()){
 								throw new AptoideExceptionSpaceInSDCard();
 							}
 							ViewCache cache = managerDownloads.repoAppDownload(repository, appHashid);
 							
 							installApp(cache, appHashid);
 						} catch (AptoideExceptionConnectivity e) {
 							toastHandler.sendEmptyMessage(R.string.no_network_connection);
 							e.printStackTrace();
 						} catch (AptoideExceptionSpaceInSDCard e) {
 	//						toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 							e.printStackTrace();
 						} catch (AptoideExceptionDownload e) {
 	//						toastHandler.sendEmptyMessage(R.string.download_failed);
 							e.printStackTrace();
 						} catch (AptoideExceptionDatabase e) {
 							e.printStackTrace();
 						} catch (Exception e){
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		}
 		
 	}
 	
 	
 	public ViewDisplayListApps getAppSearchResults(String searchString){
 		AptoideLog.d(AptoideServiceData.this, "Getting App Search Results: "+searchString);
 		return managerDatabase.getAppSearchResultsDisplayInfo(searchString, managerPreferences.isHwFilterOn(), managerPreferences.getAgeRating(), EnumAppsSorting.reverseOrdinal(managerPreferences.getAppsSortingPolicy()));
 	}
 	
 	
 	public void scheduleInstallApp(final int appHashid){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerDatabase.insertApplicationToInstall(appHashid);
 			}
 		});
 	}
 	
 	public void unscheduleInstallApp(final int appHashid){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerDatabase.removeApplicationToInstall(appHashid);
 			}
 		});
 	}
 	
 	public boolean isAppScheduledToInstall(final int appHashid){
 		return managerDatabase.isApplicationScheduledToInstall(appHashid);
 	}
 	
 	public ViewDisplayListApps getScheduledApps(){
 		return managerDatabase.getScheduledAppsInfo();
 	}
 	
 	public void installAllScheduledApps(){	//TODO could use some optimization throughout
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				ViewListIds appsList = managerDatabase.getApplicationsScheduledToInstall();
 
 				if(appsList != null){
 					for (Integer apphashid : appsList) {
 						unscheduleInstallApp(apphashid);
 						downloadApp(apphashid);
 					}
 				}
 //				downloadApp(-1255082329);
 			}
 		});
 	}
 	
 	
 	public void receiveMyapp(final String uriString){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				AptoideLog.d(AptoideServiceData.this, "Receiving Myapp file: "+uriString);
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 	//				launchAptoide();
 					String[] slashSplitUriString = uriString.split("/");
 					String myappName = slashSplitUriString[slashSplitUriString.length-1];
 					ViewCache cache;
 					if(uriString.startsWith(Constants.SCHEME_FILE_PREFIX)){
 						cache = managerDownloads.getManagerCache().cacheMyapp(uriString.substring(Constants.SCHEME_FILE_PREFIX.length()), myappName);
 					}else{
 						AptoideLog.d(AptoideServiceData.this, "Preparing download of Myapp file: "+myappName);
 						cache = managerDownloads.downloadMyapp(uriString, myappName);
 					}
 					AptoideLog.d(AptoideServiceData.this, "Preparing parsing of Myapp file: "+cache.getLocalPath());
 					
 					managerXml.myappParse(cache, myappName);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				}catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void parsingMyappFinished(ViewMyapp myapp, ViewDisplayListRepos newRepos){
 		ViewDisplayListRepos notManagedRepos = managerDatabase.excludeManagedRepos(newRepos);
 		if(!notManagedRepos.isEmpty()){
 			waitingMyappRepos.addAll(notManagedRepos);
 		}
 		if(myapp != null){
 			if(!managerDatabase.isApplicationInstalled(myapp.getPackageName())){
 				waitingMyapps.add(myapp);
 				cachedThreadPool.execute(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							aptoideClients.get(EnumServiceDataCallback.HANDLE_MYAPP).handleMyapp();
 						} catch (RemoteException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}	
 					}
 				});
 			}else{
 				Toast.makeText(this, "Application "+myapp.getName()+" already installed!", Toast.LENGTH_LONG).show();
 				manageMyappRepos();
 			}
 		}
 	}
 	
 	public void downloadMyapp(final ViewMyapp myapp){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewDownload download = getManagerDownloads().prepareApkDownload(myapp.hashCode(), myapp.getName()
 											, myapp.getRemotePath(), myapp.getSize(), myapp.getMd5sum());
 					ViewCache apk = managerDownloads.downloadApk(download);
 					AptoideLog.d(AptoideServiceData.this, "installing from: "+apk.getLocalPath());	
 					installApp(apk, myapp.hashCode());
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				}catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});		
 	}
 	
 	public void manageMyappRepos(){
 		if(!waitingMyappRepos.isEmpty()){
 			manageRepos(true);
 		}
 	}
 
 	
 	public void downloadApp(final int appHashid){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				try{
 					if(!managerDownloads.isConnectionAvailable()){
 						throw new AptoideExceptionConnectivity();
 					}
 					if(!getManagerCache().isFreeSpaceInSdcard()){
 						throw new AptoideExceptionSpaceInSDCard();
 					}
 					ViewCache apk = managerDownloads.downloadApk(managerDatabase.getAppDownload(appHashid));
 //					ViewCache apk = managerDownloads.downloadApk(managerDownloads.prepareApkDownload(appHashid, "Angry Birds Space   v.1.0.1"
 //																									, "http://mirror.apk04.bazaarandroid.com/apks/4/aptoide-f63c6f2461f65f32b6d144d6d2ff982e/apps/com-rovio-angrybirdsspace-ads-1010-380310-8d7a26ef4246dd6bebf520d519dc4db7.apk"
 //																									, 24722432, "8d7a26ef4246dd6bebf520d519dc4db7"));
 					AptoideLog.d(AptoideServiceData.this, "installing from: "+apk.getLocalPath());	
 					installApp(apk, appHashid);
 				} catch (AptoideExceptionConnectivity e) {
 					toastHandler.sendEmptyMessage(R.string.no_network_connection);
 					e.printStackTrace();
 				} catch (AptoideExceptionSpaceInSDCard e) {
 					toastHandler.sendEmptyMessage(R.string.no_space_left_in_sdcard);
 					e.printStackTrace();
 				} catch (AptoideExceptionDownload e) {
 					toastHandler.sendEmptyMessage(R.string.download_failed);
 					e.printStackTrace();
 				}catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	public void uploadApk(final ViewApk uploadingApk){
 		cachedThreadPool.execute(new Runnable() {
 			
 			@Override
 			public void run() {
 				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 				EnumServerUploadApkStatus status = managerUploads.uploadApk(uploadingApk);
 				AptoideLog.d(AptoideServiceData.this, "upload done: "+uploadingApk.getName()+"  status: "+status);
 //				if(status.equals(EnumServerUploadApkStatus.SUCCESS)){
 //					delayedExecutionHandler.postDelayed(new Runnable() {
 //			            public void run() {
 //			            	getDeltas(true);
 //			            }
 //			        }, 5000);
 //				}
 				try {
 					uploadClient.uploadDone(uploadingApk.getAppHashid(), status.ordinal());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 	
 	
 	public void addInstalledApp(final String packageName){
 //		cachedThreadPool.execute(new Runnable() {
 //			@Override
 //			public void run() {
 				ViewApplicationInstalled installedApp = managerSystemSync.getInstalledApp(packageName);
 				if(installedApp != null){
 					managerDatabase.insertInstalledApplication(installedApp);
 //					if(automaticBackupOn.get()){
 //						ViewUploadInfo uploadInfo = getUploadInfo(installedApp.getHashid());
 //						Log.d("Aptoide-AppsBackup", "preparing to auto-upload: "+uploadInfo);
 //						int appFullHashid = (uploadInfo.getAppHashid()+"|"+("http://"+uploadInfo.getRepoName()+".bazaarandroid.com/").hashCode()).hashCode();
 //						Log.d("Aptoide-AppsBackup", "appFullHashid: "+appFullHashid);
 //						if(!managerDatabase.isAppDownloadInfoPresent(appFullHashid)){
 //							Log.d("Aptoide-AppsBackup", "auto-uploading: "+uploadInfo.getAppName());
 //							ViewApk uploadingApk = new ViewApk(uploadInfo.getAppHashid(), uploadInfo.getAppName(), uploadInfo.getLocalPath());
 //							uploadingApk.setRepository(uploadInfo.getRepoName());
 //							uploadApk(uploadingApk);
 //						}
 //					}
 				}
 //			}
 //		});
 	}
 	
 	public void removeInstalledApp(final String packageName){
 		cachedThreadPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				managerDatabase.removeInstalledApplication(packageName);
 			}
 		});
 	}
 	
 	public void installApp(ViewCache apk, int appHashid){
 		if(isAppScheduledToInstall(appHashid)){
 			unscheduleInstallApp(appHashid);
 		}
 		Intent install = new Intent(Intent.ACTION_VIEW);
 		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		install.setDataAndType(Uri.fromFile(apk.getFile()),"application/vnd.android.package-archive");
 		AptoideLog.d(AptoideServiceData.this, "Installing app: "+appHashid);
 		startActivity(install);
 	}
 	
 	public void uninstallApp(int appHashid){
 		Uri uri = Uri.fromParts("package", managerDatabase.getAppPackageName(appHashid), null);
 		Intent remove = new Intent(Intent.ACTION_DELETE, uri);
 		remove.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		AptoideLog.d(AptoideServiceData.this, "Removing app: "+appHashid);
 		startActivity(remove);
 	}
 	
 	public void uninstallApps(ViewListIds appHashids){
 		for (Integer appHashid : appHashids) {
 			uninstallApp(appHashid);
 		}
 	}
 	
 	public ViewUploadInfo getUploadInfo(int appHashid){
 		String packageName = managerDatabase.getAppPackageName(appHashid);
 		if(packageName == null){
 			return null;
 		}
 		ViewUploadInfo uploadInfo = managerSystemSync.getUploadInfo(packageName, appHashid);
 		if(managerPreferences.isServerInconsistenState()){
 			uploadInfo.setRepoName(managerPreferences.getInconsistentRepoName());
 		}else{
 			uploadInfo.setRepoName(managerDatabase.getRepoInUse().getRepoName());
 		}
 		return uploadInfo;
 	}
 	
 	
 	public void splash(){
 		Intent splash = new Intent(AptoideServiceData.this, Splash.class);
 		splash.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(splash);    				
 	}
 	
 	public void handleSelfUpdate(){
 		Intent selfUpdate = new Intent(AptoideServiceData.this, SelfUpdate.class);
 		selfUpdate.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(selfUpdate);    				
 	}
 	
 	public void manageRepos(boolean myappReposWaiting){
 		Intent manageRepos = new Intent(AptoideServiceData.this, ManageRepos.class);
 		manageRepos.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 		if(myappReposWaiting){
 			manageRepos.putExtra(Constants.MYAPP_NEW_REPOS_WAITING, true);
 		}
 		startActivity(manageRepos);  		
 	}
 	
 	public void manageRepos(){
 		manageRepos(false);
 	}
 	
 
 	public void launchAptoide() {
 		Intent aptoide = new Intent(this, Aptoide.class);
 		aptoide.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(aptoide);
 	}
 	
 	
 	public ViewClientStatistics getStatistics(){
 		ViewClientStatistics statistics = new ViewClientStatistics(managerSystemSync.getAptoideVersionNameInUse());
 		managerPreferences.completeStatistics(statistics);
 		return statistics;
 	}
 
 }
