 /*=========================================================================
  *
  *  PROJECT:  SlimRoms
  *            Team Slimroms (http://www.slimroms.net)
  *
  *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
  *            All rights reserved
  *
  *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
  *
  *  AUTHORS:     fronti90, mnazim, tchaari, kufikugel
  *  DESCRIPTION: SlimOTA keeps our rom up to date
  *
  *=========================================================================
  */
 
 package com.slim.ota.updater;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.webkit.URLUtil;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 import com.slim.center.SlimCenter;
 import com.slim.ota.R;
 
 public class UpdateChecker extends AsyncTask<Context, Integer, String> {
     private static final String TAG = "UpdateChecker";
 
     private static final int MSG_CREATE_DIALOG = 0;
     private static final int MSG_DISPLAY_MESSAGE = 1;
     private static final int MSG_SET_PROGRESS = 2;
     private static final int MSG_CLOSE_DIALOG = 3;
 
     private String strDevice, slimCurVer;
     private Context mContext;
     private int mId = 1000001;
 
     private boolean mNoLog = true;
 
     public ProgressDialog mProgressDialog;
 
     final Handler mHandler = new Handler() {
 
         public void createWaitDialog(){
             mProgressDialog = new ProgressDialog(mContext);
             mProgressDialog.setTitle(mContext.getString(R.string.title_update));
             mProgressDialog.setIndeterminate(false);
             mProgressDialog.setMax(100);
             mProgressDialog.setMessage(mContext.getString(R.string.toast_text));
             mProgressDialog.show();
         }
 
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_CREATE_DIALOG:
                     createWaitDialog();
                     break;
                 case MSG_DISPLAY_MESSAGE:
                     if (mProgressDialog == null) createWaitDialog();
                     if (mProgressDialog.isShowing()) {
                         mProgressDialog.setCancelable(true);
                         mProgressDialog.setProgress(mProgressDialog.getMax());
                         mProgressDialog.setMessage((String) msg.obj);
                     }
                     break;
                 case MSG_SET_PROGRESS:
                     if (mProgressDialog != null) mProgressDialog.setProgress(((Integer) msg.obj));
                     break;
                 case MSG_CLOSE_DIALOG:
                     if (mProgressDialog != null) mProgressDialog.dismiss();
                     break;
                 default: // should never happen
                     break;
             }
         }
     };
 
     public void getDeviceTypeAndVersion() {
         try {
             FileInputStream fstream = new FileInputStream("/system/build.prop");
             BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
             String strLine;
             while ((strLine = br.readLine()) != null) {
                 String[] line = strLine.split("=");
                 if (line[0].equalsIgnoreCase("ro.product.device")) {
                     strDevice = line[1].trim();
                 } else if (line[0].equalsIgnoreCase("ro.modversion")) {
                     slimCurVer = line[1].trim();
                 }
             }
             br.close();
         } catch (Exception e) {
             Log.e(TAG, "can't get device type and version", e);
         }
     }
 
     @Override
     protected String doInBackground(Context... arg) {
         mContext = arg[0];
         Message msg;
         if (mContext != null && mContext.toString().contains("SlimOTA")) {
             msg = mHandler.obtainMessage(MSG_CREATE_DIALOG);
             mHandler.sendMessage(msg);
         }
         HttpURLConnection urlConnection = null;
         if (!connectivityAvailable(mContext)) return "connectivityNotAvailable";
         try {
             getDeviceTypeAndVersion();
             if (mNoLog == false) Log.d(TAG, "strDevice="+strDevice+ "   slimCurVer="+slimCurVer);
             if (strDevice == null || slimCurVer == null) return null;
             String newUpdateUrl = null;
             String newFileName = null;
             URL url = new URL(mContext.getString(R.string.xml_url));
             urlConnection = (HttpURLConnection) url.openConnection();
             BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             factory.setNamespaceAware(true);
             XmlPullParser xpp = factory.newPullParser();
             xpp.setInput(in);
             int eventType = xpp.getEventType();
             boolean tagMatchesDevice = false;
             boolean inFileName = false;
             boolean inDownloadURL = false;
             while (eventType != XmlPullParser.END_DOCUMENT) {
              if(eventType == XmlPullParser.START_DOCUMENT) {
                  if (mNoLog == false) Log.d(TAG, "Start document");
              } else if(eventType == XmlPullParser.START_TAG) {
                  if (xpp.getName().equalsIgnoreCase(strDevice)) tagMatchesDevice = true;
                  else if (tagMatchesDevice && xpp.getName().equalsIgnoreCase("Filename")) inFileName = true;
                  else if (tagMatchesDevice && xpp.getName().equalsIgnoreCase("DownloadUrl")) inDownloadURL = true;
              } else if(eventType == XmlPullParser.END_TAG) {
                  if (xpp.getName().equalsIgnoreCase(strDevice)) {
                      tagMatchesDevice = false;
                      break;
                  }
                  else if (tagMatchesDevice && xpp.getName().equalsIgnoreCase("Filename")) inFileName = false;
                  else if (tagMatchesDevice && xpp.getName().equalsIgnoreCase("DownloadUrl")) inDownloadURL = false;
              } else if(eventType == XmlPullParser.TEXT) {
                  if (tagMatchesDevice && inFileName) {
                     String tempFileName = xpp.getText().trim();
                     putDataInprefs(mContext, "Filename",tempFileName);
                     if (tempFileName.compareToIgnoreCase(slimCurVer)>0) newFileName = tempFileName;
                  }else if (tagMatchesDevice && inDownloadURL) {
                     String tempDownloadURL = xpp.getText().trim();
                     putDataInprefs(mContext, "DownloadUrl",tempDownloadURL);
                     if (newFileName!=null) newUpdateUrl = tempDownloadURL;
                  }
              }
              eventType = xpp.next();
             }
             return newUpdateUrl;
         } catch(Exception e) {
             Log.e(TAG, "error while connecting to server", e);
             return null;
         } finally {
             if (urlConnection !=null) urlConnection.disconnect();
         }
     }
 
     private void putDataInprefs(Context ctx, String entry, String value) {
         SharedPreferences prefs = ctx.getSharedPreferences(TAG, 0);
         String entryValue = prefs.getString(entry, "");
         if (!entryValue.equals(value)) {
             prefs.edit().putString(entry, value).apply();
         }
     }
 
     public static boolean connectivityAvailable(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo netInfo = cm.getActiveNetworkInfo();
         return (netInfo != null && netInfo.isConnectedOrConnecting() && (netInfo.getType() == ConnectivityManager.TYPE_MOBILE ||
             netInfo.getType() == ConnectivityManager.TYPE_WIFI));
     }
 
 
     @Override
     protected void onPostExecute(String result) {
         super.onPostExecute(result);
         if (mNoLog == false) Log.d("\r\n"+TAG, "result= "+result+"\n context="+mContext.toString()+"\r\n");
         if (mContext != null && mContext.toString().contains("SlimOTA")) {
             Message msg = mHandler.obtainMessage(MSG_CLOSE_DIALOG);
             mHandler.sendMessage(msg);
         } else if (result == null) {
             if (mNoLog == false) Log.d(TAG, "onPostExecute() - no new Update detected!" );
         } else {
             if (mNoLog == false) Log.d(TAG, "new Update available here: " + result);
             if (!URLUtil.isValidUrl(result))
                 showInvalidLink();
             else
                 showNotification();
         }
     }
 
     private void showNotification() {
         Notification.Builder mBuilder = new Notification.Builder(mContext)
             .setContentTitle(mContext.getString(R.string.title_update))
             .setContentText(mContext.getString(R.string.notification_message))
             .setSmallIcon(R.drawable.ic_notification_slimota)
             .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_slimota));
 
         Intent intent = new Intent(mContext, SlimCenter.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         final PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                     0, intent, PendingIntent.FLAG_ONE_SHOT);
         mBuilder.setContentIntent(pendingIntent);
         NotificationManager mNotificationManager =
             (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notif = mBuilder.build();
         notif.flags |= Notification.FLAG_AUTO_CANCEL;
         mNotificationManager.notify(mId, notif);
     }
 
     private void showInvalidLink() {
         if (mContext != null && mContext.toString().contains("SlimOTA")) {
             Message msg = mHandler.obtainMessage(MSG_DISPLAY_MESSAGE, mContext.getString(R.string.bad_url));
             mHandler.sendMessage(msg);
         } else {
             android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(mContext);
             // Setting Dialog Title
             alertDialog.setTitle(mContext.getString(R.string.title_update));
             // Setting Dialog Message
             alertDialog.setMessage(mContext.getString(R.string.bad_url));
             // Setting Positive "OK" Button
             alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog,int which) {
                     dialog.cancel();
                 }
             });
             // Showing Alert Message
             alertDialog.show();
         }
     }
 }
