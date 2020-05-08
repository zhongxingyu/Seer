 /**
  * ManagerPreferences,		auxilliary class to Aptoide's ServiceData
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
 
 package cm.aptoide.pt.preferences;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.content.pm.PackageManager;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import cm.aptoide.pt.ApplicationAptoide;
 import cm.aptoide.pt.util.Constants;
 import cm.aptoide.pt.views.ViewIconDownloadPermissions;
 
 import java.util.UUID;
 
 /**
  * ManagerPreferences, manages aptoide's preferences I/O
  *
  * @author dsilveira
  * @since 3.0
  *
  */
 public class ManagerPreferences{
 
         private SharedPreferences getPreferences;
         private SharedPreferences.Editor setPreferences;
 
 
         public ManagerPreferences(Context context) {
 
             getPreferences = PreferenceManager.getDefaultSharedPreferences(context);
             setPreferences = getPreferences.edit();
             Log.v("Aptoide-ManagerPreferences", "gotSharedPreferences: "+Constants.FILE_PREFERENCES);
             if(getAptoideClientUUID() == null){
 //    			createLauncherShortcut(context);
                try {
                    ManagerPreferences.setAptoideVersionName(context, context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                 setAptoideClientUUID( UUID.randomUUID().toString() );
             }
         }
 
 
 
     public static void setAptoideVersionName(Context context, String aptoideVersionName){
 
         PreferenceManager.getDefaultSharedPreferences(context).edit().putString("versionName", aptoideVersionName);
         PreferenceManager.getDefaultSharedPreferences(context).edit().commit();
 
     }
 
         public void createLauncherShortcut(Context context){
 
 //            removePreviousShortcuts(context, true);
 //            removePreviousShortcuts(context, false);
 
             Log.d("ManagerPreferences", "Creating Icon " + Constants.APTOIDE_CLASS_NAME);
 
             Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
     //		shortcutIntent.putExtra(context.getPackageName(), context.getString(R.string.description));
 
             final Intent intent = new Intent();
             intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
 
             intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, ApplicationAptoide.MARKETNAME);
 
             Parcelable iconResource;
             try{
             	iconResource = Intent.ShortcutIconResource.fromContext(context, context.getResources().getIdentifier("icon_" + ApplicationAptoide.BRAND,"drawable",context.getPackageName()));
             }catch(Exception e){
             	e.printStackTrace();
             	iconResource = Intent.ShortcutIconResource.fromContext(context, context.getResources().getIdentifier("icon_brand_aptoide","drawable",context.getPackageName()));
             }
             intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
             intent.putExtra("duplicate", false);
             intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
             context.sendBroadcast(intent);
         }
 
 
         public static void removePreviousShortcuts(Context context, boolean brand) {
 
             final Intent intent = new Intent();
             Log.d("ManagerPreferences", "Removing Icon");
             Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
 
             if(brand){
                 shortcutIntent.setClassName(context, Constants.APTOIDE_CLASS_NAME + "-" + ApplicationAptoide.BRAND);
             }else{
                 shortcutIntent.setClassName(context, Constants.APTOIDE_CLASS_NAME);
             }
 
     //		shortcutIntent.putExtra(context.getPackageName(), context.getString(R.string.description));
             intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
 
             intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, ApplicationAptoide.MARKETNAME);
             intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
 
             context.sendBroadcast(intent);
         }
 
 
 
         public SharedPreferences getPreferences() {
             return getPreferences;
         }
 
         public SharedPreferences.Editor setPreferences() {
             return setPreferences;
         }
 
 
         private void setAptoideClientUUID(String uuid){
             setPreferences.putString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), uuid);
             setPreferences.commit();
         }
 
         public String getAptoideClientUUID(){
             return getPreferences.getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), null);
         }
 
     //	public void setScreenDimensions(ViewScreenDimensions screenDimensions){
     //		setPreferences.putInt(EnumPreferences.SCREEN_WIDTH.name(), screenDimensions.getWidth());
     //		setPreferences.putInt(EnumPreferences.SCREEN_HEIGHT.name(), screenDimensions.getHeight());
     //		setPreferences.putFloat(EnumPreferences.SCREEN_DENSITY.name(), screenDimensions.getDensity());
     //		setPreferences.commit();
     //	}
     //
     //	public ViewScreenDimensions getScreenDimensions(){
     //		return new ViewScreenDimensions(getPreferences.getInt(EnumPreferences.SCREEN_WIDTH.name(), Constants.NO_SCREEN), getPreferences.getInt(EnumPreferences.SCREEN_HEIGHT.name(), Constants.NO_SCREEN), getPreferences.getFloat(EnumPreferences.SCREEN_DENSITY.name(), Constants.NO_SCREEN));
     //	}
     //
     //	public void completeStatistics(ViewClientStatistics statistics){
     //		statistics.completeStatistics(getAptoideClientUUID(), getScreenDimensions());
     //	}
     //
     //
     //	public boolean getShowApplicationsByCategory(){
     //		return getPreferences.getBoolean(EnumPreferences.SHOW_APPLICATIONS_BY_CATEGORY.name(), false);
     //	}
     //
     //	public void setShowApplicationsByCategory(boolean byCategory){
     //		setPreferences.putBoolean(EnumPreferences.SHOW_APPLICATIONS_BY_CATEGORY.name(), byCategory);
     //		setPreferences.commit();
     //	}
     //
     //	public boolean getShowSystemApplications(){
     //		return getPreferences.getBoolean(EnumPreferences.SHOW_SYSTEM_APPS.name(), false);
     //	}
     //
     //	public void setShowSystemApplications(boolean show){
     //		setPreferences.putBoolean(EnumPreferences.SHOW_SYSTEM_APPS.name(), show);
     //		setPreferences.commit();
     //	}
     //
     //	public int getAppsSortingPolicy(){
     //		return getPreferences.getInt(EnumPreferences.SORT_APPLICATIONS_BY.name(), EnumAppsSorting.ALPHABETIC.ordinal());
     //	}
     //
     //	public void setAppsSortingPolicy(int sortingPolicy){
     //		setPreferences.putInt(EnumPreferences.SORT_APPLICATIONS_BY.name(), sortingPolicy);
     //		setPreferences.commit();
     //	}
     //
     //	public void setHwFilter(boolean on){
     //		setPreferences.putBoolean(EnumPreferences.IS_HW_FILTER_ON.name(), on);
     //		setPreferences.commit();
     //	}
     //
     //	public boolean isHwFilterOn(){
     //		return getPreferences.getBoolean(EnumPreferences.IS_HW_FILTER_ON.name(), false);
     //	}
 
     //	public void setIconDownloadPermissions(ViewIconDownloadPermissions iconDownloadPermissions){
     //		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIFI.name(), iconDownloadPermissions.isWiFi());
     //		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.ETHERNET.name(), iconDownloadPermissions.isEthernet());
     //		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.WIMAX.name(), iconDownloadPermissions.isWiMax());
     //		setPreferences.putBoolean(EnumPreferences.DOWNLOAD_ICONS_.name()+EnumIconDownloadsPermission.MOBILE.name(), iconDownloadPermissions.isMobile());
     //		setPreferences.commit();
     //	}
 
         public ViewIconDownloadPermissions getIconDownloadPermissions(){
             ViewIconDownloadPermissions permissions = new ViewIconDownloadPermissions(
                                                         getPreferences.getBoolean("wifi", true),
                                                         getPreferences.getBoolean("ethernet", true),
                                                         getPreferences.getBoolean("4g", true),
                                                         getPreferences.getBoolean("3g", true) );
             return permissions;
         }
 
     public static void removePreviousShortcuts2(Context context) {
 
         final Intent intent = new Intent();
         Log.d("ManagerPreferences", "Removing Icon");
         Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
 
         //		shortcutIntent.putExtra(context.getPackageName(), context.getString(R.string.description));
         intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
 
         intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, ApplicationAptoide.MARKETNAME);
         intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
 
         context.sendBroadcast(intent);
 
 
     }
 
     //	public void setAutomaticInstall(boolean on){
     //		setPreferences.putBoolean(EnumPreferences.AUTOMATIC_INSTALL.name(), on);
     //		setPreferences.commit();
     //	}
     //
     //	public boolean isAutomaticInstallOn(){
     //		return getPreferences.getBoolean(EnumPreferences.AUTOMATIC_INSTALL.name(), false);
     //	}
     //
     //	public void setAgeRating(EnumAgeRating rating){
     //		setPreferences.putInt(EnumPreferences.AGE_RATING.name(), rating.ordinal());
     //		setPreferences.commit();
     //	}
     //
     //	public EnumAgeRating getAgeRating(){
     //		return EnumAgeRating.reverseOrdinal(getPreferences.getInt(EnumPreferences.AGE_RATING.name(), EnumAgeRating.No_Filter.ordinal()));
     //	}
     //
     //
     //	public ViewSettings getSettings(){
     //		return new ViewSettings(getIconDownloadPermissions(), isHwFilterOn(), getAgeRating(), isAutomaticInstallOn());
     //	}
 
         //TODO move these next 3 values to database to support multiple servers
 
     //	public void setServerLogin(ViewLogin login){
     //		AptoideLog.v(this, "set serverLogin: "+login);
     //		setPreferences.putString(EnumPreferences.SERVER_USERNAME.name(), login.getUsername());
     //		setPreferences.putString(EnumPreferences.SERVER_PASSHASH.name(), login.getPassword());
     //		setPreferences.commit();
     //	}
     //
     //	public void clearServerLogin(){
     //		AptoideLog.v(this, "cleared serverLogin");
     //		setPreferences.remove(EnumPreferences.SERVER_USERNAME.name());
     //		setPreferences.remove(EnumPreferences.SERVER_PASSHASH.name());
     //		setPreferences.remove(EnumPreferences.SERVER_TOKEN.name());
     //		setPreferences.commit();
     //	}
     //
     //	public ViewLogin getServerLogin(){
     ////		if(getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), null) == null && getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), null) == null){
     ////			AptoideLog.v(this, "get serverLogin: username: "+getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), "")
     ////												+" password: "+getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), ""));
     ////			return null; //TODO null object
     ////		}else{
     //			ViewLogin storedLogin = new ViewLogin( getPreferences.getString(EnumPreferences.SERVER_USERNAME.name(), "")
     //												, getPreferences.getString(EnumPreferences.SERVER_PASSHASH.name(), "") );
     //			AptoideLog.v(this, "get serverLogin: "+storedLogin);
     //			return storedLogin;
     ////		}
     //	}
 
 
 }
