 package com.malcom.library.android.module.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Locale;
 import java.util.concurrent.TimeUnit;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.telephony.TelephonyManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 
 import com.malcom.library.android.MCMDefines;
 import com.malcom.library.android.exceptions.ApplicationConfigurationNotFoundException;
 import com.malcom.library.android.exceptions.ConfigModuleNotInitializedException;
 import com.malcom.library.android.exceptions.ConfigurationException;
 import com.malcom.library.android.module.core.MCMCoreAdapter;
 import com.malcom.library.android.utils.ToolBox;
 import com.malcom.library.android.utils.ToolBox.HTTP_METHOD;
 
 /**
  * Class for configuration module
  * 
  * @author Malcom Ventures, S.L.
  * @since  2012
  */
 public class MCMConfigManager {
 	
 	/** MALCOM API endpoint */
 	private final static String GLOBAL_CONF = "v1/globalconf/";
 	/** MALCOM library remote configuration file name */
 	private final static String CONFIG_FILE_NAME = "config.json";
 	/** MALCOM library basic bundled configuration. This name is used when saving the remote configuration data. */
 	private final static String BUNDLE_CONFIG_FILE_NAME = "mcmconfiginfo.json";
 	/** MALCOM splash image file name used when store the remote file in disk */
 	private final static String CONFIG_SPLASH_IMAGE_NAME = "splash.img";
 	/** MALCOM off-line splash image file name used when there is no network and no splash has never been downloaded. */
 	private final static String CONFIG_SPLASH_ASSETS_IMAGE_NAME = "splash.img";
 	/** Value returned by Malcom when an application does not have a configuration specified. */
 	private final static String NO_CONFIGURATION_DATA = "{}\n";
 	
 	/** Library shared preferences "interstitial times shown" parameter */
 	private final static String LIB_PREFENCES_INTERSTITIAL_TIMES_SHOWN = "insterstitialTimesShown";
 	private final static String LIB_PREFENCES_INTERSTITIAL_TIMES_TO_SHOW = "insterstitialTimesToShow";
 	
 	private static MCMConfigManager instance = null;
 
 	/** Malcom application configuration */
 	private Configuration configuration = null;
 	
 	private static int configLayoutResId = 0;
 	
 	private LinearLayout splash_layout;
 	private ImageView splashImageView;
 	private LinearLayout splash_progress_zone;
 	
 	private LinearLayout interstitialLayout;
 	private WebView interstitial;
 	private Button interstitialClose;
 	private LinearLayout interstitial_progress_zone;
 			
 	private Activity activity;
 	
 	private ProgressDialog dialog;	
 	private boolean isLoading = false;
 	
 	private String originalTitle;
 	
 	private boolean executeAfterLoad;
 	
 	private static boolean configShown;
 	
 
 
 	protected MCMConfigManager() {
 		// Exists only to defeat instantiation.
 	}	
 	
 	// Singleton
 	public static MCMConfigManager getInstance() {
 
 		if (instance == null) {
 			instance = new MCMConfigManager();
 		}
 		return instance;
 	}
 	
 	
 	
 	// PUBLIC FUNCTIONS
 	
 
 	/**
 	 * Loads the configuration for the application. First it tries to get from the server 
 	 * if the device is on-line, if not, it will try to load previously downloaded one and,
 	 * if there is no previous one, it will load the bundled default (configurable) 
 	 * configuration stored in the assets directory.
 	 *
 	 * @param  activity  	Activity in which to display the information.
 	 * @param  appPackage 	package of the application
 	 */
 	public void createConfig(final Activity activity, String appPackage) {
 
 		this.activity = activity;
 		
 		executeAfterLoad = true;
 
         // Gets the resources ids
 		configLayoutResId = activity.getResources().getIdentifier("config_layout", "id", appPackage);
         int resSplashID = activity.getResources().getIdentifier("image_view", "id", appPackage);
         int resInterstitialID = activity.getResources().getIdentifier("webview", "id", appPackage);
         int resInterstitialCloseButtonID = activity.getResources().getIdentifier("web_view_close", "id", appPackage);
         int resInterstitialLayoutID = activity.getResources().getIdentifier("webview_layout", "id", appPackage);
         int resInterstitialProgressZoneID = activity.getResources().getIdentifier("progresszone", "id", appPackage);
         int resSplashProgressZoneID = activity.getResources().getIdentifier("splash_progresszone", "id", appPackage);
         int resSplashLayoutID = activity.getResources().getIdentifier("splash_layout", "id", appPackage);
         
         this.splashImageView = (ImageView) activity.findViewById(resSplashID);
 
         // Initializes the layout components
         initializeLayoutComponents(resSplashID, resInterstitialLayoutID, resInterstitialID, 
 				resInterstitialCloseButtonID, resInterstitialProgressZoneID,
 				resSplashProgressZoneID, resSplashLayoutID);
 
         // Loads the remote configuration from server
         loadConfiguration();
 
 	}
 	
 	private void initializeLayoutComponents(int resSplashID, int resInterstitialLayoutID, int resInterstitialID,
 											int resInterstitialCloseButtonID, int resInterstitialProgressZoneID,
 											int resSplashProgressZoneID, int resSplashLayoutID){
 		
 		if (resSplashID != 0) {
 		
 			this.splashImageView = (ImageView) activity.findViewById(resSplashID);
 			
 		}
 		if (resInterstitialLayoutID != 0) {
 		
 			this.interstitialLayout = (LinearLayout) activity.findViewById(resInterstitialLayoutID);
 			
 		}
 		if (resInterstitialCloseButtonID != 0) {
 		
 			this.interstitialClose = (Button) activity.findViewById(resInterstitialCloseButtonID);
 		
 		}
 		this.originalTitle = this.activity.getTitle().toString();
 		if (resInterstitialProgressZoneID != 0) {
 		
 			this.interstitial_progress_zone = (LinearLayout) activity.findViewById(resInterstitialProgressZoneID);
 			
 		}
 		if (resSplashProgressZoneID != 0) {
 		
 			this.splash_progress_zone = (LinearLayout) activity.findViewById(resSplashProgressZoneID);
 			
 		}
 		if (resSplashLayoutID != 0) {
 		
 			this.splash_layout = (LinearLayout) activity.findViewById(resSplashLayoutID);
 			
 		}
 		
 		if (resInterstitialID != 0) {
 		
 			this.interstitial = (WebView) activity.findViewById(resInterstitialID);
 		
 			if (this.interstitial != null) {
 			
 				this.interstitial.setWebViewClient(new WebViewClient(){
 	
 					@Override
 					public boolean shouldOverrideUrlLoading(WebView view, String url) {
 						//It seems that Android is opening the url in the system
 						//default navigator instead in the webview. We avoid this
 						//with the code below.
 						view.loadUrl(url);
 						return true;				
 					}
 				});
 				
 				this.interstitial.setWebChromeClient(new WebChromeClient(){
 
 					@Override
 					public void onProgressChanged(WebView view, int newProgress) {
 						//We show the loading icon while the page is being loaded.
 						if(newProgress < 100 && interstitial_progress_zone.getVisibility() == ProgressBar.GONE){
 							interstitial_progress_zone.setVisibility(ProgressBar.VISIBLE);
 						}
 						
 						if(newProgress == 100) {
 							//...and hide the loading once is loaded.
 							interstitial_progress_zone.setVisibility(ProgressBar.GONE);
 						}				
 					}
 					
 				});
 				
 			}
 			
 		}
 		
 	}
 
     public void getProperty(String key, MCMCoreAdapter.ConfigListener listener) throws ConfigModuleNotInitializedException {
         if(isConfigurationLoaded()){
             listener.onReceivedParameter(key, (String) configuration.getProperty(key));
         }else{
             //TODO: Pedro - Cargamos el fichero de configuración y cuando termina llamamos al callback con el(acentuado)
             executeAfterLoad = false;
             loadConfiguration();
             if(isLoading)
                 throw new ConfigModuleNotInitializedException("Config module is being initialized, wait until is fully initialized before call this method.");
             else
                 throw new ConfigModuleNotInitializedException("Config module has not been initialized, use useConfigModule().");
         }
 
     }
 	
 	
 	/**
 	 * Get a value for key
 	 *
 	 * @param  key  Key string for value
 	 * @return value for key or null if the property key does not exist or if the key is null.
 	 */
 	public String getKeyValue(String key) {
 		
 		if (isConfigurationLoaded()) {
 			if (!TextUtils.isEmpty(key)) {
 				if(configuration!=null){
 					return configuration.getProperty(key)!=null?(String)configuration.getProperty(key):null;
 				}else{
 					Log.e(MCMDefines.LOG_TAG,"CONFIG_GET_KEY_VALUE. There is no config available (not even the default one)!. " +
 							"Please do not forget to include in your project the file '"+ BUNDLE_CONFIG_FILE_NAME +"' file.");
 					throw new ConfigModuleNotInitializedException("Config module has not been initialized, use useConfigModule().");
 				}
 			}else{
 				return null;
 			}
 		} else {
 			if(isLoading)
 				throw new ConfigModuleNotInitializedException("Config module is being initialized, wait until is fully initialized.");
 			else				
 				throw new ConfigModuleNotInitializedException("Config module has not been initialized, use useConfigModule().");
 		}
 	}
 	
 	
 	
 	
 	
 	// AUXILIAR METHODS
 	
 	// AUXILIAR FUNCTIONS
 	
 	
 	private void showProgressDialog(){
 		if(!isLoading){
 			
 			isLoading = true;
 			dialog = new ProgressDialog(activity);
 			//dialog.setMessage("Loading application...");			
 			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			dialog.setCancelable(false);
 			dialog.show();
 		}
 	}
 	
 	
 	private void closeProgressDialog(){
 		isLoading = false;
 		dialog.dismiss();
 	}
 	
 	
 	
 	// -- Configuration loading options. Local and Remote.
 	
 	private void loadConfiguration(){
 		
 		configShown = true;
 		if(ToolBox.network_haveNetworkConnection(activity)){
             Log.d(MCMDefines.LOG_TAG, "READ_CONGIF_DOWNLOAD. On-Line. Downloading configuration.");
             new DownloadConfigurationFile().execute();
 		}else{
 			loadLocalConfiguration();
 		}
 	}
 	
 	
 	private void loadLocalConfiguration(){
 		if(activity.getFileStreamPath(CONFIG_FILE_NAME).exists()){
 			config_useDownloaded();			
 		} else {
 			config_useBundled();			
 		}
 		
 		if(executeAfterLoad){
 			executeConfig(false);
 		}
 	}
 	
 	// -- End Configuration loading options. Local and Remote.
 	
 	
 	// -- Configuration load options methods.
 	
 	
 	private void config_useDownloaded(){
 		Log.d(MCMDefines.LOG_TAG, "READ_CONGIF_DOWNLOADED. Off-line. The configuration was already downloaded, using this configuration.");
 		try{
 			//showProgressDialog();
 			
 			FileInputStream fIn = activity.openFileInput(CONFIG_FILE_NAME);
 			InputStreamReader isr = new InputStreamReader(fIn);
 			char[] data = new char[fIn.available()]; 
 			isr.read(data);
 
 			loadConfigurationFileFromDisk(new String(data));
 		}catch(Exception e){
 			Log.e(MCMDefines.LOG_TAG, "READ_CONGIF_DOWNLOADED_ERROR. Problems obtaining the configuration from '"+ CONFIG_FILE_NAME +"':"+e.getMessage(),e);
 		} finally {
 			//closeProgressDialog();
 		}
 	}
 	
 	
 	private void config_useBundled(){
 		Log.d(MCMDefines.LOG_TAG, "READ_CONGIF_BUNDLED. Off-line. The configuration was not previously downloaded. using the default bundled one.");
 		try{
 			//showProgressDialog();
 			
 			InputStream assetIn = activity.getAssets().open(BUNDLE_CONFIG_FILE_NAME);
 			InputStreamReader isr = new InputStreamReader(assetIn);
 			char[] assetData = new char[assetIn.available()]; 
 			isr.read(assetData);
 			
 			loadConfigurationFileFromDisk(new String(assetData));
 		}catch(Exception e){
 			Log.e(MCMDefines.LOG_TAG, "READ_CONGIF_BUNDLED_ERROR. Problems obtaining the default configuration from '"+ BUNDLE_CONFIG_FILE_NAME +"':"+e.getMessage(),e);
 		} finally {
 			//closeProgressDialog();
 		}
 	}
 	
 	// -- End configuration load options methods.
 	
 	
 	
 	
 	private void executeConfig(boolean isNetworkAccess) {
 		//((RelativeLayout)activity.findViewById(configLayoutResId)).setVisibility(View.VISIBLE);
 		Log.d(MCMDefines.LOG_TAG, "Execute config with configuration: "+configuration);
 		if(configuration!=null){
 			if(configuration.isSplash()){
 				showSplash(isNetworkAccess);
 			}
 			if(configuration.isInterstitial()) {				
 				showInterstitial(isNetworkAccess);
 			} 
 			if(configuration.isAlert()) {
 				//((RelativeLayout)activity.findViewById(configLayoutResId)).setVisibility(View.GONE);
 				showAlert();
 			}/*else {
 				((RelativeLayout)activity.findViewById(configLayoutResId)).setVisibility(View.GONE);
 				//There is configuration but none of the possibilities are active.
 				activity.startActivity(principalActivity);
 				activity.finish();
 			}*/
 		}else{
 			Log.e(MCMDefines.LOG_TAG, "EXECUTE_CONFIG_ERROR. There is no configuration available to execute!");
 		}
 	}
 	
 	
 	
 	private boolean loadConfigurationFile(Activity context) {
 		boolean configLoaded = false;
 		try{
 			URL configPath = obtainConfigurationURLPath();		
 			configuration = new Configuration(getJSONfromURL(configPath.toString()),Locale.getDefault().getLanguage());
 			Log.i(MCMDefines.LOG_TAG, "CONFIG_LANGUAGE." + Locale.getDefault().getLanguage());
 			configLoaded = true;
 		}catch(ApplicationConfigurationNotFoundException e){
 			Log.i(MCMDefines.LOG_TAG, "CONFIGURATION_GET_FROM_SERVER. Application does not have a configuration.");
 		}catch(ConfigurationException e){
 			Log.e(MCMDefines.LOG_TAG, "LOAD_CONFIG_FILE_FROM_SERVER_ERROR." + e.getMessage());
 		}
 		
 		return configLoaded;
 	}
 	
 	
 	private void loadConfigurationFileFromDisk(String configData) {
 		try {
 			JSONObject jObject = new JSONObject(configData);
 			configuration = new Configuration(jObject,Locale.getDefault().getLanguage());
 		} catch( Exception e){			
 			Log.e(MCMDefines.LOG_TAG, "LOAD_CONFIG_FROM_DISK_ERROR. "+e.getMessage());
 		}
 	}
 	
 	
 	
 	private URL obtainConfigurationURLPath() throws ConfigurationException{
 		try{
            String deviceId = ToolBox.device_getId(activity);

 			String path = MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_BASEURL) + GLOBAL_CONF
 					+ MCMCoreAdapter.getInstance().coreGetProperty(MCMCoreAdapter.PROPERTIES_MALCOM_APPID) + "/"
					+ deviceId + "/" + CONFIG_FILE_NAME;
 			
 			URL url = new URL(path);
 			Log.d(MCMDefines.LOG_TAG, "CONFIG. " + path);
 			return url;
 		
 		}catch(MalformedURLException e){
 			Log.e(MCMDefines.LOG_TAG, "CONFIG_GET_SERVER_PATH." + e.getMessage());
 			throw new ConfigurationException("Bad server configuration path!",ConfigurationException.CONFIGURATION_EXCEPTION_BAD_SERVER_CONFIG_PATH);
 		}
     }
     
     
 	private JSONObject getJSONfromURL(String url) throws ApplicationConfigurationNotFoundException{
     	String result = "";
     	JSONObject jObject = null;
     	    	
     	try{
     		result = ToolBox.net_httpclient_doAction(HTTP_METHOD.GET, url, null, null);
     	    
     	    //Check for non set configuration data for the application.
     	    if(result.equalsIgnoreCase(NO_CONFIGURATION_DATA))
     	    	throw new ApplicationConfigurationNotFoundException();
     	    
     	    //try parse the string to a JSON object
     	    jObject = new JSONObject(result);
     	        	    
     	}catch(JSONException e){
             Log.e(MCMDefines.LOG_TAG, "Error getting the configuration from http connection "+e.toString());
     	}catch(IOException e){
     	    Log.e(MCMDefines.LOG_TAG, "Error getting the configuration from http connection "+e.toString());
     	}
     	
     	return jObject;    	
     }
 	
     
 	private synchronized void saveConfigurationFile() {
     	
     	FileOutputStream fos;
 		try {
 			if(configuration!=null){
 				fos = activity.openFileOutput(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
 				
 				fos.write(configuration.getConfigDataRaw().getBytes());
 				fos.close();		
 			}
 		} catch (Exception e) {
 			Log.e(MCMDefines.LOG_TAG, "CONFIGURATION_SAVE_ERROR."+e.getMessage());
 		}
     }
     
     
 	
     // -- Show/Remove Splash, interstitial and alert functions
     
     private void showSplash(final boolean isNetworkAccess){
     	//Get the splash image.
     	Log.d(MCMDefines.LOG_TAG, "Show splash");
     	if (splash_layout != null) {
 	    	
     		this.splash_layout.setVisibility(View.VISIBLE);
 	    	this.splash_progress_zone.setVisibility(View.VISIBLE);
 	    	
 	    	PrepareAndShowSplashImage prepareSplashImage = new PrepareAndShowSplashImage(isNetworkAccess);
 	    	prepareSplashImage.execute();
 	    	
     	}
     }
     
     
     
     
 
     private void removeSplash(boolean isNetworkAccess) {
     	Log.d(MCMDefines.LOG_TAG, "Remove splash");
     	//((RelativeLayout)activity.findViewById(configLayoutResId)).setVisibility(View.GONE);
     	this.splashImageView.postInvalidate();
     	this.splashImageView.setImageBitmap(null);
     	this.splashImageView.setVisibility(View.GONE);
     	this.splash_layout.setVisibility(View.GONE);
     	
     	updateFullscreenStatus(false, true);
 	}   
         
     private void showInterstitial(boolean isNetworkAccess){
     	
     	Log.d(MCMDefines.LOG_TAG, "Show Intersitial");
     	if(isNetworkAccess && interstitial != null && interstitial_checkTimesShown()){
     		
 	    	interstitial.getSettings().setJavaScriptEnabled(true);
 	    	interstitial.loadUrl(configuration.getInterstitialWeb());
 	    	interstitial.setVisibility(View.VISIBLE);
 	    	
 	    	interstitialClose.setText("Close");
 	    	interstitialClose.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	                removeInterstitial();
 	            }
 	        });
 	    	
 	    	updateFullscreenStatus(true, false);
 	    	
 	    	interstitialClose.setVisibility(View.VISIBLE);
 	    	interstitialLayout.setVisibility(View.VISIBLE);
     	}else{
     		removeInterstitial();
     	}
     }
     
     private boolean interstitial_checkTimesShown(){
     	boolean show = true;
     	
     	//Check if there are still times to be shown
 		SharedPreferences librarySettings = activity.getSharedPreferences(MCMCoreAdapter.MALCOM_LIBRARY_PREFERENCES_FILE_NAME, 0);
 		SharedPreferences.Editor editor = librarySettings.edit();
 		if(configuration.getInterstitialTimesToShow()!=null){
 			
 			//1º Look for changes.
 			int timesToShow = librarySettings.getInt(LIB_PREFENCES_INTERSTITIAL_TIMES_TO_SHOW, -1);
 			if(timesToShow!=-1){
 				//If the value is different than the previous one we save it and reset the counter.
 				if(timesToShow!=configuration.getInterstitialTimesToShow().intValue()){
 					timesToShow = configuration.getInterstitialTimesToShow().intValue();
 					editor.putInt(LIB_PREFENCES_INTERSTITIAL_TIMES_TO_SHOW, timesToShow);
 					editor.putInt(LIB_PREFENCES_INTERSTITIAL_TIMES_SHOWN, 0);
 					editor.commit();
 				}
 			}else{
 				//The variable did not exist, we save it.
 				timesToShow = configuration.getInterstitialTimesToShow().intValue();
 				editor.putInt(LIB_PREFENCES_INTERSTITIAL_TIMES_TO_SHOW, timesToShow);
 				editor.commit();
 			}
 			
 			//2º See if there are remaining times to show.
 			int timesShown = librarySettings.getInt(LIB_PREFENCES_INTERSTITIAL_TIMES_SHOWN, 0);
 			if(timesShown==timesToShow){
 				show=false;				
 			}else{
 				timesShown++;
 				editor.putInt(LIB_PREFENCES_INTERSTITIAL_TIMES_SHOWN, timesShown);
 				editor.commit();
 			}
 		}else{
 			editor.remove(LIB_PREFENCES_INTERSTITIAL_TIMES_SHOWN);
 			editor.commit();
 		}
 		
 		return show;
     }
     
     private void removeInterstitial(){
     	
     	Log.d(MCMDefines.LOG_TAG, "Remove intersitial");
     	if (configLayoutResId != 0) {
     	
     		if (interstitial != null) {
     		
 	    		//((RelativeLayout)activity.findViewById(configLayoutResId)).setVisibility(View.GONE);
 	    	
 		    	interstitial.postInvalidate();
 		    	interstitial.setVisibility(View.GONE);
 		    	
 		    	interstitialClose.postInvalidate();
 		    	interstitialClose.setVisibility(View.GONE);    	
 		    	interstitialLayout.setVisibility(View.GONE);
 		    	
 		    	updateFullscreenStatus(false, false);
 		    	
     		}	
 	    	
     	}
     }
     
     
     private void showAlert() {
 
     	Log.d(MCMDefines.LOG_TAG, "Show alert");
 		try {			
 			boolean showAlert = true;
 			
 			String alertAppVersion = configuration.getAlertAppStoreVersion();						
 			String appVersionName = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
 						
 			Log.d(MCMDefines.LOG_TAG, "App Version: "+alertAppVersion);
 			Log.d(MCMDefines.LOG_TAG, "Version Name: "+appVersionName);
 			
 			if (alertAppVersion != null) {
 				alertAppVersion= normalisedVersion(alertAppVersion);
 				appVersionName = normalisedVersion(appVersionName);
 				showAlert = showAlertIfVersion(configuration.getAlertVersionCondition(), appVersionName, alertAppVersion);
 			}
 			
 			if (showAlert) {
 				AlertDialog alertDialog = ConfigurationUtils.createAlertDialog(activity, configuration);
 				if(alertDialog != null)
 						alertDialog.show();
 			}
 			
 		} catch (Exception e) {
 			Log.e("SHOW_ALERT_ERROR",e.getMessage());
 			
 		}
 	}
     
     // -- End Show/Remove Splash, interstitial and alert functions
 
     
         
     private void updateFullscreenStatus(boolean useFullscreen, boolean isSplash)
     {   
        if(useFullscreen)
        {
             activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
             activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         }
         else
         {
         	activity.setTitle(originalTitle);
         	activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         	activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
         }
        
        
        if(isSplash){
     	   splashImageView.requestLayout();
        }else{
     	   interstitialLayout.requestLayout();
        }
     }
     
     
     private boolean showAlertIfVersion(String compareString, String appVersion, String alertAppVersion) throws NameNotFoundException {
 		
     	if (!compareString.equals("NONE")) {
 			
 			int cmpVersions = appVersion.compareTo(alertAppVersion);			
 			return 	
 					(compareString.equals("GREATER_EQUAL") && cmpVersions >= 0 ) ||
 					(compareString.equals("GREATER") && cmpVersions > 0 ) ||
 					(compareString.equals("LESS") && cmpVersions < 0 ) ||
 					(compareString.equals("LESS_EQUAL") && cmpVersions <= 0 ) ||
 					(compareString.equals("EQUAL") && cmpVersions == 0 );
 			
 		} else {
 			return true;			
 		}
 	}
     
     
     private String normalisedVersion(String version) {    	
         return ConfigurationUtils.normalisedVersion(version, ".", 4);
     }    
     
         
     // -- Configuration status functions.
     
     public boolean isConfigurationLoaded(){
     	return (configuration!=null && !isLoading);
     }
     
     
     public boolean isConfigurationLoading(){
     	return isLoading;
     }
 	
     // -- End Configuration status functions.
     
     
     
     /** Background tasks */
 	
     /*
      * This thread class downloads the configuration and execute it. 
      * 
      * @author Malcom Ventures S.L
      * @since  2012
      *
      */
     private class DownloadConfigurationFile extends AsyncTask<Void, Float, Integer>{
     	
    	 	 protected void onPreExecute() {
          }
 
          protected Integer doInBackground(Void ...valores) {        	 
         	 if(!loadConfigurationFile(activity)){
         		//If there is a failure downloading the config, we try from our local store.			
         		if(activity.getFileStreamPath(CONFIG_FILE_NAME).exists()){
     				config_useDownloaded();			
     			} else {
     				config_useBundled();			
     			}
         	 }
         	 
         	 return 0;
          }
 
          protected void onPostExecute(Integer bytes) {
         	 saveConfigurationFile();
         	 
         	 if(executeAfterLoad)
         		 executeConfig(true);
          }
     }
     
     /**
      * This task gets and displays the splash image.
 	 *
 	 * First, it downloads the splash image if possible. Then, it tries to retrieve the downloaded image
 	 * or, if it is not found, it tries to get the image from the 'assets' folder.
 	 *
 	 * When the image Bitmap is loaded, it publishes progress with that Bitmap so the splash is displayed.
 	 * After that, it sleeps for a while (that depends on the configuration) and then it publishes progress
 	 * again but this time with no Bitmap, which means the splash must be removed from the display.
      */
     private class PrepareAndShowSplashImage extends AsyncTask<Boolean, Bitmap, Void> {
 
     	private boolean isNetworkAccess;
 
     	public PrepareAndShowSplashImage(boolean isNetworkAccess) {
     		this.isNetworkAccess = isNetworkAccess;
     	}
 
 		@Override
 		protected Void doInBackground(Boolean... params) {
 
 			// Obtain the splash image
 			if (isNetworkAccess) {
 				//We try to get the image and update the stored one.
 	    		downloadSplashFile(configuration.getSplashImageUrl());	    		
 	    	}
 
 			try {
 
 				final Bitmap splashBitmap = loadSplashBitmap();
 
 				// Show splash for a while
 				publishProgress(splashBitmap);
 				sleepSeconds(configuration.getSplashAnimationDelay());
 
 			} catch (Exception e) {
 				Log.e(MCMDefines.LOG_TAG, "Error loading splash image BitMap", e);
 			}
 
 			// Hide splash
 			publishProgress();
 
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Bitmap... splashBitmap) {
 
 			// The progress zone is hidden in both cases because it might happen
 			// that the splash image is never loaded
 			splash_progress_zone.setVisibility(View.GONE);
 
 			if (splashBitmap.length == 1) // Show splash
 			{
 				//Set the image in the view.
 				splashImageView.setImageBitmap(splashBitmap[0]);
 				updateFullscreenStatus(true, true);
 				splashImageView.setVisibility(View.VISIBLE);
 			}
 			else if (splashBitmap.length == 0) // Hide splash
 			{
 				removeSplash(isNetworkAccess);
 			}
 		}
 
 		/*
 		 * Downloads the image of the http address saving 
 		 * it to the application internal private storage.
 		 *  
 		 * @param imageHttpAddress
 		 */
 		private synchronized void downloadSplashFile(String imageHttpAddress) {
 			
 			URL imageUrl = null;
 			Log.d(MCMDefines.LOG_TAG, "DOWNLOAD_SPLASH_IMAGE: " + imageHttpAddress);
 			
 			try {
 				imageUrl = new URL(imageHttpAddress);	
 				
 				HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
 				conn.setUseCaches(true);
 				conn.connect();
 				
 				//We save the image for off-line later usage.
 				File f = ToolBox.storage_getAppInternalStorageFilePath(activity, CONFIG_SPLASH_IMAGE_NAME);
 				FileOutputStream fOut = new FileOutputStream(f);
 				ToolBox.storage_copyStream(conn.getInputStream(), fOut, 1024);
 				fOut.close();										
 			} 
 			catch (Exception e) {				
 				Log.e(MCMDefines.LOG_TAG, "DOWNLOAD_SPLASH_IMAGE_ERROR: ("+ imageUrl.toString()+") : " + e.getMessage());
 			}
 		}
 
 		private Bitmap loadSplashBitmap() throws Exception
 		{
 			final Bitmap splashBitmap;
 
 			if (ToolBox.storage_checkIfFileExistsInInternalStorage(activity, CONFIG_SPLASH_IMAGE_NAME)) {
 				// Load splash image from disk
 				splashBitmap = ToolBox.media_loadBitmapFromInternalStorage(activity, CONFIG_SPLASH_IMAGE_NAME);
 			} else {
 				// No previously downloaded splash image so we use the provided one in the assets folder.
 				splashBitmap = loadSplashFromAssetsFolder();
 			}
 			return splashBitmap;
 		}
 
 		private void sleepSeconds(int seconds) {
 			try {
 				Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
 			} catch (InterruptedException e) {
 				Log.e(MCMDefines.LOG_TAG, "Unexpected interruption", e);
 			}
 		}
 
 		/*
 		 * Loads the splash image provided in the assets folder.
 		 *  
 		 * @return
 		 */
 		private Bitmap loadSplashFromAssetsFolder() throws IOException
 		{
     		InputStream is= activity.getAssets().open(CONFIG_SPLASH_ASSETS_IMAGE_NAME);
     		return BitmapFactory.decodeStream(is);
 		}
     	
     }
 
 }
