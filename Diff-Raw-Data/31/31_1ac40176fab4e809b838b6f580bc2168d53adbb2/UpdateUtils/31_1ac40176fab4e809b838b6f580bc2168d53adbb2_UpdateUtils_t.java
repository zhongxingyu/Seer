 package cn.ingenic.updater;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.SystemProperties;
 import android.text.TextUtils;
 import android.util.Log;
 
 class UpdateUtils {
     
     static final String FILENAME_SAVED ="update_ingenic.zip";
     static final String ENCODE="UTF-8";
     static final int FLAG_CHECK_UPDATE=1;
     static final int FLAG_DOWNLOAD_UPDATE=2;
     /** the URL to check update
      * */
     static final String URL_PRODUCTS_UPDATE="http://bocaidong.sinaapp.com/tmp/update_products_list.xml";
     static final String URL_TO_CHECK_UPDATE="http://bocaidong.sinaapp.com/tmp/update_list.xml";
     static final String ro_build_id = "ro.build.id";
     static final String ro_build_display_id = "ro.build.display.id";
     static final String ro_build_date_utc = "ro.build.date.utc";
     static final String ro_product_model = "ro.product.model";
     static final String ro_product_device = "ro.product.device";
     static final String ro_product_platform = "ro.product.platform";
     
     private static final String PREFERENCE_NAME = "update_config";
     private static final String CONFIG_DOWNLOAD_ID = "download_id";
     private static final String CONFIG_UPDATE_INFO = "update_info";
     
     static String getCurrentVersion(){
         return get("ro.build.display.id","unknow");
     }
 
     static String get(String key){
         return SystemProperties.get(key);
     }
 
     static String get(String key, String def){
         String value = SystemProperties.get(key);
         if (TextUtils.isEmpty(value))
             return def;
         else
             return value;
     }
     
     public static void putDownloadInfo(Context context, long id, UpdateInfo info){
     	SharedPreferences pref= context.getSharedPreferences(PREFERENCE_NAME,0);
     	SharedPreferences.Editor editor = pref.edit();
     	editor.putLong(CONFIG_DOWNLOAD_ID, id);
     	editor.putString(CONFIG_UPDATE_INFO, info == null ? "" : info.toString());
     	editor.commit();
     }
     
     public static long getDownloadId(Context context){
     	SharedPreferences pref= context.getSharedPreferences(PREFERENCE_NAME,0);
     	return pref.getLong(CONFIG_DOWNLOAD_ID, 0);
     }
     
     public static UpdateInfo getUpdateInfoCache(Context context){
     	SharedPreferences pref= context.getSharedPreferences(PREFERENCE_NAME,0);
     	return UpdateInfo.createFromString(pref.getString(CONFIG_UPDATE_INFO, ""));
     }
     
     public static void putStringToSP(Context con,String key, String value){
         SharedPreferences pref=con.getSharedPreferences(PREFERENCE_NAME, 0);
         SharedPreferences.Editor editor = pref.edit();
         editor.putString(key, value);
         editor.commit();
     }
     
     public static String getStringFromSP(Context con,String key){
         SharedPreferences pref=con.getSharedPreferences(PREFERENCE_NAME, 0);
         return pref.getString(key, " ");
     }
     
     //:TODO
     public static boolean isNeedUpdate(String newVersion, String oldVersion) {
         return true;
     }
     
     
     static void logVer(){
         String keys[]=new String[]{
                 ro_build_id,
                 ro_build_display_id,
                 ro_build_date_utc,
                 ro_product_model,
                 ro_product_device,
                 ro_product_platform,
         };
         for(String key: keys){
             Log.i("dfdun", key+": "+get(key,"unknown"));
         }
     }
 }
