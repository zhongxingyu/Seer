 /**
  * PHR_AndroidNative
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.nativeapp.eshop.activity;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xml.sax.SAXException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.AssetManager;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.DisplayMetrics;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.photon.phresco.nativeapp.R;
 import com.photon.phresco.nativeapp.eshop.config.ConfigReader;
 import com.photon.phresco.nativeapp.eshop.config.Configuration;
 import com.photon.phresco.nativeapp.eshop.core.Constants;
 import com.photon.phresco.nativeapp.eshop.logger.PhrescoLogger;
 import com.photon.phresco.nativeapp.eshop.model.appconfig.AppConfig;
 import com.photon.phresco.nativeapp.eshop.model.errormessage.ErrorManager;
 
 /**
  * Parent class which extends Activity class, for all the activities in
  * application
  * 
  * @author viral_b
  * 
  */
 public class PhrescoActivity extends Activity {
 
 	private static final String TAG = "PhrescoActivity  *********** ";
 	private static final int ERROR_DIALOG_FLAG = 0;
 	private String errMessage = "errorMessage";
 	private String cancelFlag = "cancelFlag";
 
 	private static AppConfig appConfigJSONObj;
 	private JSONObject appConfigJSONResponseObj = null;
 //	private static Properties properties;
 	private int screenHeight;
 	private int screenWidth;
 	

 	private static final String WEB_SERVICE = "WebService";
 
 	private String protocol = "protocol";
 	private String host = "host";
 	private String port = "port";
 	private String context = "context";
 	private String additionalContext = "additional_context";
 
 	/**
 	 * @return the appConfigJSONObj
 	 */
 	public static AppConfig getAppConfigJSONObj() {
 		return appConfigJSONObj;
 	}
 
 	/**
 	 * @param appConfigJSONObj
 	 *            the appConfigJSONObj to set
 	 */
 	public static void setAppConfigJSONObj(AppConfig appConfigJSONObj) {
 		PhrescoActivity.appConfigJSONObj = appConfigJSONObj;
 	}
 
 	/**
 	 * @return the appConfigJSONResponseObj
 	 */
 	public JSONObject getAppConfigJSONResponseObj() {
 		return appConfigJSONResponseObj;
 	}
 
 	/**
 	 * @param appConfigJSONResponseObj
 	 *            the appConfigJSONResponseObj to set
 	 */
 	public void setAppConfigJSONResponseObj(JSONObject appConfigJSONResponseObj) {
 		this.appConfigJSONResponseObj = appConfigJSONResponseObj;
 	}
 
 	/**
 	 * @return the propertiesr
 	 *//*
 	public static Properties getProperties() {
 		return properties;
 	}
 
 	*//**
 	 * @param properties
 	 *            the properties to set
 	 *//*
 	public static void setProperties(Properties properties) {
 		PhrescoActivity.properties = properties;
 	}*/
 
 	/**
 	 * @return the screenHeight
 	 */
 	public int getScreenHeight() {
 		return screenHeight;
 	}
 
 	/**
 	 * @param screenHeight
 	 *            the screenHeight to set
 	 */
 	public void setScreenHeight(int screenHeight) {
 		this.screenHeight = screenHeight;
 	}
 
 	/**
 	 * @return the screenWidth
 	 */
 	public int getScreenWidth() {
 		return screenWidth;
 	}
 
 	/**
 	 * @param screenWidth
 	 *            the screenWidth to set
 	 */
 	public void setScreenWidth(int screenWidth) {
 		this.screenWidth = screenWidth;
 	}
 
 	private DisplayMetrics metrics = new DisplayMetrics();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		try {
 			super.onCreate(savedInstanceState);
 
 			this.getWindow().setSoftInputMode(
 					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 			this.getWindow().setSoftInputMode(
 					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
 
 			setScreenHeight(getDeviceScreenHeight());
 			setScreenWidth(getDeviceScreenWidth());
 
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "onCreate : Exception: " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		return;
 	}
 
 	/**
 	 * Read phresco-env-config.xml file to get to connect to web service
 	 */
 	public void readConfigXML() {
 		try {
 
 			Resources resources = getResources();
 			AssetManager assetManager = resources.getAssets();
 
 			// Read from the /assets directory
 			InputStream inputStream = assetManager
 					.open(Constants.PHRESCO_ENV_CONFIG);
 
 			ConfigReader confReaderObj = new ConfigReader(inputStream);
 
 			PhrescoLogger.info(TAG + "Default ENV = " + confReaderObj.getDefaultEnvName());
 
 			List<Configuration> configByEnv = confReaderObj.getConfigByEnv(confReaderObj.getDefaultEnvName());
 
 			for (Configuration configuration : configByEnv) {
 				String envName = configuration.getEnvName();
 				String envType = configuration.getType();
 				PhrescoLogger.info(TAG + "envName = " + envName	+ " ----- envType = " + envType);
 				//properties = configuration.getProperties();
 
				 if(envType.equalsIgnoreCase("webservice")) {
				    String configName = configuration.getName();
				    String configJsonString = confReaderObj.getConfigAsJSON(envName, WEB_SERVICE, configName);
 					getWebServiceURL(configJsonString);
 				} else if (envType.equalsIgnoreCase("server")) {					
 					/*String configJsonString = confReaderObj.getConfigAsJSON(envName, SERVER, SERVER_CONFIG_NAME);
 					getServerURL(configJsonString);*/
 				}
 
 			}
 
 		} catch (ParserConfigurationException ex) {
 			PhrescoLogger.info(TAG
 					+ "readConfigXML : ParserConfigurationException: "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		} catch (SAXException ex) {
 			PhrescoLogger.info(TAG + "readConfigXML : SAXException: "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		} catch (IOException ex) {
 			PhrescoLogger.info(TAG + "readConfigXML : IOException: "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "readConfigXML : Exception: "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	private void getWebServiceURL(String configJsonString) {
 		try {
 			JSONObject jsonObject = new JSONObject(configJsonString);
 			String webServiceProtocol = jsonObject.getString(protocol).endsWith("://") ? jsonObject.getString(protocol) : jsonObject.getString(protocol) + "://"; // http://
 
 			String webServiceHost = jsonObject.getString(port).equalsIgnoreCase("") 
 					? (jsonObject.getString(host).endsWith("/") 
 								? jsonObject.getString(host): jsonObject.getString(host) + "/")	
 					: jsonObject.getString(host); // localhost/
 													// localhost
 
 			String webServicePort = jsonObject.getString(port).equalsIgnoreCase("") 
 					? "" : (jsonObject.getString(port).startsWith(":") 
 								? jsonObject.getString(port) : ":" + jsonObject.getString(port)); // ""
 																						// (blank)
 																						// :1313
 
 			String webServiceContext = jsonObject.getString(context).startsWith("/") 
 					? jsonObject.getString(context) : "/"	+ jsonObject.getString(context); // /phresco
 
 			Constants.setWebContextURL(webServiceProtocol + webServiceHost
 					+ webServicePort + webServiceContext + "/");
 			Constants.setRestAPI(Constants.REST_API);
 			PhrescoLogger.info(TAG + "getWebServiceURL() - Constants.webContextURL : "
 					+ Constants.getWebContextURL() + Constants.getRestAPI());
 		} catch (JSONException e) {
 			PhrescoLogger.info(TAG + " EnvConstuctor -  Exception " + e.toString());
 			PhrescoLogger.warning(e);
 		}
 	}
 	
 	/*
 	 * Don't Remove this method
 	 */
 	private void getServerURL(String configJsonString) {
 		try {
 			JSONObject jsonObject = new JSONObject(configJsonString);
 			String webServiceProtocol = jsonObject.getString(protocol).endsWith(
 					"://") ? jsonObject.getString(protocol) : jsonObject.getString(protocol) + "://"; // http://
 
 			String webServiceHost = jsonObject.getString(port).equalsIgnoreCase(
 					"") ? (jsonObject.getString(host).endsWith("/") ? jsonObject.getString(host) : jsonObject.getString(host) + "/")
 					: jsonObject.getString(host); // localhost/
 													// localhost
 
 			String webServicePort = jsonObject.getString(port).equalsIgnoreCase(
 					"") ? ""
 					: (jsonObject.getString(port).startsWith(":") ? jsonObject.getString(port) : ":" + jsonObject.getString(port)); // ""
 																						// (blank)
 																						// :1313
 
 			String webServiceContext = jsonObject.getString(context).startsWith(
 					"/") ? jsonObject.getString(context) : "/"
 					+ jsonObject.getString(context); // /phresco
 
 			String webServiceAdditionalContext = null;
 			
 			try {
 				webServiceAdditionalContext = jsonObject.getString(
 						additionalContext).startsWith("/") ? jsonObject.getString(additionalContext) : "/"
 						+ jsonObject.getString(additionalContext);
 			} catch (Exception e) {
 				webServiceAdditionalContext = null;
 			}
 
 			if (webServiceAdditionalContext != null
 					&& webServiceAdditionalContext.length() > 1) {// > 1 beacuse of
 																	// "/"
 				Constants.setWebContextURL(webServiceProtocol + webServiceHost
 						+ webServicePort + webServiceContext
 						+ webServiceAdditionalContext + "&userAgent=android");
 			} else {
 				Constants.setWebContextURL(webServiceProtocol + webServiceHost
 								+ webServicePort + webServiceContext
 								+ "?userAgent=android");
 			}
 
 			PhrescoLogger.info(TAG + "getServerURL() - Constants.webContextURL : " + Constants.getWebContextURL());
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			PhrescoLogger.info(TAG + " EnvConstuctor -  Exception " + e.toString());
 			PhrescoLogger.warning(e);
 		}
 	}
 	
 	
 
	
 
 	/**
 	 * Make a standard toast that just contains a text view with the text from a
 	 * resource, for long time
 	 * 
 	 * @param str
 	 *            The resource id of the string resource to use. Can be
 	 *            formatted text.
 	 */
 	public void toast(int str) {
 		Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
 		toast.setGravity(LinearLayout.VERTICAL, 0, 0);
 		toast.show();
 	}
 
 	/**
 	 * Make a standard toast that just contains a text view, for long time
 	 * 
 	 * @param str
 	 *            The text to show. Can be formatted text.
 	 */
 	public void toast(String str) {
 		Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
 		toast.setGravity(LinearLayout.VERTICAL, 0, 0);
 		toast.show();
 	}
 
 	/**
 	 * Make a standard toast that just contains a text view with the text from a
 	 * resource.
 	 * 
 	 * @param str
 	 *            The resource id of the string resource to use. Can be
 	 *            formatted text.
 	 * @param length
 	 *            How long to display the message. Either LENGTH_SHORT or
 	 *            LENGTH_LONG
 	 */
 	public void toast(int str, int length) {
 		Toast toast = Toast.makeText(this, str, length);
 		toast.setGravity(LinearLayout.VERTICAL, 0, 0);
 		toast.show();
 	}
 
 	/**
 	 * The screen density expressed as dpi
 	 * 
 	 * @return int
 	 * 
 	 * @return May be either DENSITY_LOW, DENSITY_MEDIUM, or DENSITY_HIGH.
 	 */
 	protected int getScreenDensity() {
 		int screenDensity = 0;
 		try {
 			getWindowManager().getDefaultDisplay().getMetrics(metrics);
 			switch (metrics.densityDpi) {
 			case DisplayMetrics.DENSITY_LOW:
 				PhrescoLogger.info(TAG + "DENSITY_LOW");
 				break;
 			case DisplayMetrics.DENSITY_MEDIUM:
 				PhrescoLogger.info(TAG + "DENSITY_MEDIUM");
 				break;
 			case DisplayMetrics.DENSITY_HIGH:
 				PhrescoLogger.info(TAG + "DENSITY_HIGH");
 				break;
 			case DisplayMetrics.DENSITY_XHIGH:
 				PhrescoLogger.info(TAG + "DENSITY_EXTRA_HIGH");
 				break;
 			}
 			PhrescoLogger.info(TAG + "densityDpi: " + metrics.densityDpi);
 			PhrescoLogger.info(TAG + "density: " + metrics.density);
 			PhrescoLogger.info(TAG + "heightPixels: " + metrics.heightPixels);
 			PhrescoLogger.info(TAG + "widthPixels: " + metrics.widthPixels);
 
 			screenDensity = metrics.densityDpi;
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "getScreenDensity: Exception: "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 		return screenDensity;
 	}
 
 	private int getDeviceScreenHeight() {
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		return metrics.heightPixels;
 	}
 
 	private int getDeviceScreenWidth() {
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		return metrics.widthPixels;
 	}
 
 	/**
 	 * Show the error message dialog box with OK button
 	 * 
 	 * @param errorMessage
 	 */
 	public void showErrorDialog(String errorMessage) {
 		try {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(errorMessage)
 					.setTitle(R.string.app_name)
 					.setCancelable(false)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									finish();
 									// System.runFinalizersOnExit(true);
 									android.os.Process
 											.killProcess(android.os.Process
 													.myPid());
 								}
 							});
 			@SuppressWarnings("unused")
 			AlertDialog alert = builder.show();
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "showErrorDialog: " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	/**
 	 * Show error message dialog box with OK and Cancel buttons
 	 * 
 	 * @param errorMessage
 	 * @param cancelFlag
 	 */
 	public void showErrorDialog(String errorMessage, boolean cancelFlag) {
 		PhrescoLogger
 				.info(TAG + "showErrorDialog : cancelFlag = " + cancelFlag);
 		try {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					})
 
 			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.cancel();
 				}
 			}).setMessage(errorMessage).setTitle(R.string.app_name)
 					.setCancelable(false);
 
 			@SuppressWarnings("unused")
 			AlertDialog alert = builder.show();
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "showErrorDialog: " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	/**
 	 * Handler to call the method to show the error message dialog
 	 */
 	private Handler showErrorDialogHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			PhrescoLogger.info(TAG + " - Inside showErrorDialogHandler  : ");
 			super.handleMessage(msg);
 			processError(msg);
 		}
 	};
 
 	/**
 	 * Process the error message dialog as per the data recevied
 	 * 
 	 * @param msg
 	 */
 	private void processError(Message msg) {
 		try {
 			Bundle data = msg.getData();
 			if (data != null) {
 				if (data.getBoolean(cancelFlag)) {
 					showErrorDialog(data.getString(errMessage),
 							data.getBoolean(cancelFlag));
 				} else {
 					showErrorDialog(data.getString(errMessage));
 				}
 			}
 
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + " - splashHandler  - Exception : "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	/**
 	 * Call the error dialog handler method to open the normal error dialog box
 	 * with OK button
 	 */
 	public void showNormalErrorDialog() {
 		try {
 			PhrescoLogger
 					.info(TAG
 							+ " processOnComplete - readAppConfigJSON - showErrorDialogHandler :");
 			Message msg = new Message();
 			Bundle errorMessageBundle = new Bundle();
 			errorMessageBundle.putString(errMessage,
 					getString(R.string.http_connect_error_message));
 			msg.setData(errorMessageBundle);
 			msg.what = ERROR_DIALOG_FLAG;
 			showErrorDialogHandler.sendMessage(msg);
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + " - showNormalErrorDialog  - Exception : "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	/**
 	 * Call the error dialog handler method to open the normal error dialog box
 	 * with OK and Cancel buttons
 	 */
 	public void showErrorDialogWithCancel() {
 		try {
 			PhrescoLogger.info(TAG
 					+ " processOnComplete - showErrorDialogHandler :");
 			Message msg = new Message();
 			Bundle errorMessageBundle = new Bundle();
 			errorMessageBundle.putString(errMessage,
 					getString(R.string.http_connect_error_message));
 			errorMessageBundle.putBoolean(cancelFlag, true);
 			msg.setData(errorMessageBundle);
 			msg.what = ERROR_DIALOG_FLAG;
 			showErrorDialogHandler.sendMessage(msg);
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG
 					+ " - showErrorDialogWithCancel  - Exception : "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 	}
 
 	/**
 	 * Get the ErrorManager GSON object, when there is error returned from web
 	 * server
 	 * 
 	 * @param errorJSONObj
 	 * @return ErrorManager
 	 */
 	public static ErrorManager getErrorGSONObject(JSONObject errorJSONObj) {
 		ErrorManager errorManagerObj = null;
 		PhrescoLogger.info(TAG + "getErrorGSONObject() - JSON STRING : "
 				+ errorJSONObj.toString());
 		try {
 			Gson jsonObj = new Gson();
 			errorManagerObj = jsonObj.fromJson(errorJSONObj.toString(),
 					ErrorManager.class);
 			PhrescoLogger.info(TAG
 					+ "getErrorGSONObject() - ERROR JSON OBJECT : "
 					+ errorJSONObj.toString());
 		} catch (Exception ex) {
 			PhrescoLogger.info(TAG + "getErrorGSONObject: Exception : "
 					+ ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 		return errorManagerObj;
 	}
 
 }
