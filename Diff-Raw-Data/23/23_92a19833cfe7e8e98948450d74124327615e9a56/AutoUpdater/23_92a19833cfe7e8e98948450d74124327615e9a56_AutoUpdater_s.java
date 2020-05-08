 package com.wifi.sapguestconnect.autoupdate;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.wifi.sapguestconnect.R;
 import com.wifi.sapguestconnect.common.CommonFacade;
 import com.wifi.sapguestconnect.log.LogManager;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Looper;
 import android.os.Message;
 
 public class AutoUpdater 
 {
	private static final String APPLICATION_DATA_BASE_URL = "http://privatewificonnect.googlecode.com/files/";
	private static final String APPLICATION_PROPERTIES_URL = "https://privatewificonnect.googlecode.com/svn/trunk/WiFiConnect/application.properties";	
 	private static final String DOWNLOAD_FILEPATH = "/sdcard/download";
 
 	
 	private final Context mContext;
 	private DownloadDialogHandler mDownloadHandler = null;
 	private Resources mResources = null;
 	
 	private AutoUpdater(Context context)
 	{
 		LogManager.LogFunctionCall("AutoUpdater", "C'tor()");
 		
 		this.mContext = context;
 		this.mResources = context.getResources();
 		
 		mDownloadHandler = new DownloadDialogHandler(new DownloadDialog(context));
 	}
 	
 	
     /*
      * Update checking. We go to a predefined URL and fetch a properties style file containing
      * information on the update. These properties are:
      * 
      * versionCode: An integer, version of the new update, as defined in the manifest. Nothing will
      *              happen unless the update properties version is higher than currently installed.
      * fileName: A string, URL of new update apk. If not supplied then download buttons
      *           will not be shown, but instead just a message and an OK button.
      * message: A string. A yellow-highlighted message to show to the user. Eg for important
      *          info on the update. Optional.
      * title: A string, title of the update dialog. Defaults to "Update available".
      * 
      * Only "versionCode" is mandatory.
      */
     private void checkForUpdate() 
     {
     	LogManager.LogFunctionCall("AutoUpdater", "checkForUpdate()");
     	
     	if (!isUpdateEnabled()) 
     	{
     		return;
     	}
     	
     	new Thread(new Runnable(){
 			public void run(){
 				LogManager.LogInfoMsg("AutoUpdater", "checkForUpdate().Thread.run()", "Checking for updates...");
 				
 				Looper.prepare();
 				// Getting Properties
 				Properties updateProperties = queryForProperty(APPLICATION_PROPERTIES_URL);
 				if (updateProperties != null && updateProperties.containsKey("versionCode")) {
 				  
 					int availableVersion = Integer.parseInt(updateProperties.getProperty("versionCode"));
 					int installedVersion = CommonFacade.getVersionCode(mContext);
					String fileName = updateProperties.getProperty("fileName", "");
 					String updateMessage = updateProperties.getProperty("message", "");
 					String updateTitle = updateProperties.getProperty("title", "Update available");
 					if (availableVersion > installedVersion) 
 					{
						displayUpdateDialog(fileName, updateMessage, updateTitle);
 					}
 				}
 				Looper.loop();
 			}
     	}).start();
     }
    
    private void downloadUpdate(final String downloadFileUrl, final String fileName) {
     	LogManager.LogFunctionCall("AutoUpdater", "downloadUpdate()");
     	new Thread(new Runnable(){
 			public void run(){
 				LogManager.LogInfoMsg("AutoUpdater", "downloadUpdate().Thread.run()", "Downloading update...");
 				Message msg = Message.obtain();
             	msg.obj = DownloadStates.MESSAGE_DOWNLOAD_STARTING;
             	mDownloadHandler.sendMessage(msg);
				downloadUpdateFile(downloadFileUrl, fileName);
 				Intent intent = new Intent(Intent.ACTION_VIEW); 
 			    intent.setDataAndType(android.net.Uri.fromFile(new File(DOWNLOAD_FILEPATH+"/"+fileName)),"application/vnd.android.package-archive"); 
 			    mContext.startActivity(intent);
 			}
     	}).start();
     }
     
     private boolean isUpdateEnabled()
     {
     	LogManager.LogFunctionCall("AutoUpdater", "isUpdateEnabled()");
     	return true;
     }
     
 	private Properties queryForProperty(String url) {
 		LogManager.LogFunctionCall("AutoUpdater", "queryForProperty()");
 		Properties properties = null; 
 		HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet(String.format(url));
         try {
             HttpResponse response = client.execute(request);
 
             StatusLine status = response.getStatusLine();
             if (status.getStatusCode() == 200) {
 	            HttpEntity entity = response.getEntity();
 	            properties = new Properties();
 	            properties.load(entity.getContent());
             }
         } catch (IOException e) {
 
         }
         
         if (properties == null)
         {
         	properties = new Properties();
         }
         
 		return properties;
 	}
 	
 	private boolean downloadUpdateFile(String downloadFileUrl, String destinationFilename) {
 		LogManager.LogFunctionCall("AutoUpdater", "downloadUpdateFile()");
 		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) == false) {
 			return false;
 		}
 		File downloadDir = new File(DOWNLOAD_FILEPATH);
 		if (downloadDir.exists() == false) {
 			downloadDir.mkdirs();
 		}
 		else {
 			File downloadFile = new File(DOWNLOAD_FILEPATH+"/"+destinationFilename);
 			if (downloadFile.exists()) {
 				downloadFile.delete();
 			}
 		}
 		return this.downloadFile(downloadFileUrl, DOWNLOAD_FILEPATH, destinationFilename);
 	}
 	
 	private boolean downloadFile(String url, String destinationDirectory, String destinationFilename) {
 		LogManager.LogFunctionCall("AutoUpdater", "downloadFile()");
 		boolean filedownloaded = true;
 		HttpClient client = new DefaultHttpClient();
         HttpGet request = new HttpGet(String.format(url));
         Message msg = Message.obtain();
         try {
             HttpResponse response = client.execute(request);
             StatusLine status = response.getStatusLine();
             if (status.getStatusCode() == 200) {
 	            HttpEntity entity = response.getEntity();
 	            InputStream instream = entity.getContent();
 	            int fileSize = (int)entity.getContentLength();
 	            FileOutputStream out = new FileOutputStream(new File(destinationDirectory+"/"+destinationFilename));
 	            byte buf[] = new byte[8192];
 	            int len;
 	            int totalRead = 0;
 	            while((len = instream.read(buf)) > 0) {
 	            	msg = Message.obtain();
 	            	msg.obj = DownloadStates.MESSAGE_DOWNLOAD_PROGRESS;
 	            	totalRead += len;
 	            	msg.arg1 = totalRead / 1024;
 	            	msg.arg2 = fileSize / 1024;
 	            	mDownloadHandler.sendMessage(msg);
 	            	out.write(buf,0,len);
             	}
 	            out.close();
             }
             else {
             	throw new IOException();
             }
         } 
         catch (IOException e) 
         {
         	LogManager.LogException(e, "AutoUpdater", "downloadFile()");
         	filedownloaded = false;
         }
         msg = Message.obtain();
         msg.obj = DownloadStates.MESSAGE_DOWNLOAD_COMPLETE;
         mDownloadHandler.sendMessage(msg);
         return filedownloaded;
 	}
 	
	private void displayUpdateDialog(final String fileName, String updateTitle, String updateMessage)
 	{
 		LogManager.LogFunctionCall("AutoUpdater", "displayUpdateDialog()");
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
 		builder.setMessage(mResources.getString(R.string.new_version_update_question))
 		       .setCancelable(false)
 		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) 
 		           {
 		        	   LogManager.LogFunctionCall("AutoUpdater", "displayUpdateDialog().AlertDialog.Builder.PositiveButton.onClick()");
 		        	   dialog.cancel();
		        	   downloadUpdate(APPLICATION_DATA_BASE_URL + fileName, fileName);
 		           }
 		       })
 		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   LogManager.LogFunctionCall("AutoUpdater", "displayUpdateDialog().AlertDialog.Builder.NegativeButton.onClick()");
 		                dialog.cancel();
 		           }
 		       });
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	
 	public static void CheckForUpdate(Context context)
 	{
 		LogManager.LogFunctionCall("AutoUpdater", "CheckForUpdate()");
 		
 		AutoUpdater autoUpdater = new AutoUpdater(context);
 		autoUpdater.checkForUpdate();
 	}
 }
