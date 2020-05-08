 /**
  * ManagerSystemSync,		auxilliary class to Aptoide's ServiceData
  * Copyright (C) 2011  Duarte Silveira
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
 
 package pt.caixamagica.aptoide.appsbackup.data.system;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplication;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplicationInstalled;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewUploadInfo;
 import pt.caixamagica.aptoide.appsbackup.ifaceutil.EnumAppStatus;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.os.RemoteException;
 
 /**
  * ManagerSystemSync, manages data synchronization with the underlying android's package database 
  * 
  * @author dsilveira
  * @since 3.0
  *
  */
 public class ManagerSystemSync {
 
 	AptoideServiceData serviceData;
 	PackageManager packageManager;
 	
 	ExecutorService tasksPool = Executors.newSingleThreadExecutor();
 	
 	public ManagerSystemSync(AptoideServiceData serviceData){
 		this.serviceData = serviceData;
 		packageManager = serviceData.getPackageManager();
 	}
 	
 	public int getAptoideVersionInUse(){
 		PackageInfo aptoideInfo;
 		try {
 			aptoideInfo = packageManager.getPackageInfo("pt.caixamagica.aptoide.appsbackup", 0);
    		} catch (NameNotFoundException e) {	
    			/** this should never happen */
    			return -1;
    		}
 		return aptoideInfo.versionCode;
 	}
 	
 	public String getAptoideVersionNameInUse(){
 		PackageInfo aptoideInfo;
 		try {
 			aptoideInfo = packageManager.getPackageInfo("pt.caixamagica.aptoide.appsbackup", 0);
    		} catch (NameNotFoundException e) {	
    			/** this should never happen */
    			return null;
    		}
 		return aptoideInfo.versionName;
 	}
 	
 	public String getAptoideAppName(){
 		PackageInfo aptoideInfo;
 		try {
 			aptoideInfo = packageManager.getPackageInfo("pt.caixamagica.aptoide.appsbackup", 0);
    		} catch (NameNotFoundException e) {	
    			/** this should never happen */
    			return null;
    		}
 		return packageManager.getApplicationLabel(aptoideInfo.applicationInfo).toString();
 	}
 	
 	public int getAppHashid(String packageName){
 		int versionCode = 0;
 		try {
 			versionCode = packageManager.getPackageInfo(packageName, 0).versionCode;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		return (packageName+'|'+versionCode).hashCode();
 	}
 	
 	public ArrayList<ViewApplicationInstalled> getInstalledApps(){
 		List<PackageInfo> systemInstalledList = packageManager.getInstalledPackages(0);
 		ArrayList<ViewApplicationInstalled> installedApps = new ArrayList<ViewApplicationInstalled>(systemInstalledList.size());
 		ViewApplicationInstalled installedApp;
 		for (PackageInfo installedAppInfo : systemInstalledList) {
 			
 			File apk = new File(installedAppInfo.applicationInfo.sourceDir);
 			long timestamp = apk.lastModified();
 			long size = apk.length()/1024;//TODO remove the /1024 after fixing the repo to return Bytes instead of Kbytes
 			
 //			if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[1].equals("system")  || installedAppInfo.applicationInfo.sourceDir.split("[/]+")[2].equals("app-private") || apk.length() > 20000000){
 //				continue;
 //			}
 			
 			EnumAppStatus type = EnumAppStatus.INSTALLED;
 			if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[1].equals("system")){
 				type = EnumAppStatus.SYSTEM;
 			}else if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[2].equals("app-private")){
 				type = EnumAppStatus.PROTECTED;
 			}else if(size > 150000){
 				type = EnumAppStatus.TOO_BIG;
 			}
 			
 			installedApp = new ViewApplicationInstalled((packageManager.getApplicationLabel(installedAppInfo.applicationInfo)).toString(), installedAppInfo.packageName, 
 							((installedAppInfo.versionName==null)?Integer.toBinaryString(installedAppInfo.versionCode):installedAppInfo.versionName), installedAppInfo.versionCode, 
 							timestamp, size, type.ordinal());
 			installedApps.add(installedApp);
 		}
 		return installedApps;
 	}
 	
 	public ViewApplicationInstalled getInstalledApp(String packageName){
 		ViewApplicationInstalled installedApp = null;
 		try {
 			PackageInfo installedAppInfo = packageManager.getPackageInfo(packageName, 0);
 			
 			File apk = new File(installedAppInfo.applicationInfo.sourceDir);
 			long timestamp = apk.lastModified();
 			long size = apk.length()/1024;//TODO remove the /1024 after fixing the repo to return Bytes instead of Kbytes
 
 			EnumAppStatus type = EnumAppStatus.INSTALLED;
 			if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[1].equals("system")){
 				type = EnumAppStatus.SYSTEM;
 			}else if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[2].equals("app-private")){
 				type = EnumAppStatus.PROTECTED;
 			}else if(size > 150000){
 				type = EnumAppStatus.TOO_BIG;
 			}
 			
 			installedApp = new ViewApplicationInstalled((packageManager.getApplicationLabel(installedAppInfo.applicationInfo)).toString(), installedAppInfo.packageName, 
 							((installedAppInfo.versionName==null)?Integer.toBinaryString(installedAppInfo.versionCode):installedAppInfo.versionName), installedAppInfo.versionCode, 
 							timestamp, size, type.ordinal());
 			
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return installedApp;
 	}
 	
 	public ViewUploadInfo getUploadInfo(String packageName, int appHashid){
 		ViewUploadInfo uploadInfo = null;
 		try {
 			ApplicationInfo installedAppInfo = packageManager.getPackageInfo(packageName, 0).applicationInfo;
 			
 			uploadInfo = new ViewUploadInfo(installedAppInfo.sourceDir, installedAppInfo.loadLabel(packageManager).toString(), appHashid);
 			
 			File apk = new File(installedAppInfo.sourceDir);
 			long size = apk.length();
 			
 			uploadInfo.setSize(size);
 			
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return uploadInfo;
 	}
 	
 	public void cacheInstalledIcons(){
 		 tasksPool.execute(new Runnable() {
 			@Override
 			public void run() {
 				List<PackageInfo> systemInstalledList = packageManager.getInstalledPackages(0);
 				for (PackageInfo installedAppInfo : systemInstalledList) {
 					if(installedAppInfo.applicationInfo.sourceDir.split("[/]+")[1].equals("system")){
 						continue;
 						//TODO maybe show it but mark as system
 					}
 					serviceData.getManagerCache().cacheIcon((installedAppInfo.packageName+"|"+installedAppInfo.versionCode).hashCode(), ((BitmapDrawable)installedAppInfo.applicationInfo.loadIcon(packageManager)).getBitmap());
 				}
 				serviceData.refreshInstalledLists();				
 			}
 		});
 	}
 	
 	public ViewHwFilters getHwFilters(){
 		int sdkVersion = Build.VERSION.SDK_INT;
 		int screenSize = serviceData.getResources().getConfiguration().screenLayout&Configuration.SCREENLAYOUT_SIZE_MASK;
 		String glEsVersion = ((ActivityManager) serviceData.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion();
 		
 		return new ViewHwFilters(sdkVersion, screenSize, Float.parseFloat(glEsVersion));
 	}
 	
 //	public Drawable getInstalledAppIcon(String packageName){
 //		try {
 //			return packageManager.getApplicationIcon(packageName);
 //		} catch (NameNotFoundException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //			return null;
 //		}
 //	}
 
 }
