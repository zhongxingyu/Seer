 package com.appspot.manup.smsproxy.proxy;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.FormBodyPart;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.appspot.manup.smsproxy.ControlProxyActivity;
 import com.appspot.manup.smsproxy.SmsMessageInfo;
 
 import com.appspot.manup.smsproxy.R;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.util.Log;
 
 public final class SmsProxyService extends PersistentIntentService
 {
     private static final int NOTIFICATION_ID = 1;
 
     private static final String TAG = SmsProxyService.class.getSimpleName();
     private static final String ACTION_HANDLE_SMS = "com.hackathon.android.HANDLE_SMS";
     private static final String EXTRA_PDUS = "pdus";
     private static final String FORM_MESSAGE = "message";
     private static final String FORM_NUMBER = "number";
     private static final String PREFS_API_URI = "api_uri";
     private static final String PREFS_API_PORT = "api_port";
 
     private final SmsReplyResponseHandler mSmsReplyResponseHandler = new SmsReplyResponseHandler();
     private URI mApiUri = null;
     private HttpClient mHttpClient = new DefaultHttpClient();
 
     private final BroadcastReceiver mSmsBroadcastReciever = new BroadcastReceiver()
     {
         @Override
         public void onReceive(final Context context, final Intent broadcastIntent)
         {
             final Intent intent = new Intent(context, SmsProxyService.class);
             intent.setAction(ACTION_HANDLE_SMS);
             intent.putExtras(broadcastIntent);
             startService(intent);
         } // onReceive
     };
 
     public SmsProxyService()
     {
         super(TAG);
     } // SmsServerService
 
     @Override
     public void onCreate()
     {
         super.onCreate();
         SmsProxyManager.setRunning(true);
         SmsProxyManager.sendMessage("Starting server.");
         buildApiUri();
         registerSmsBroadcastReciever();
         startForeground();
     } // onCreate
 
     private void buildApiUri()
     {
         boolean error = false;
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         final String apiUri = prefs.getString(PREFS_API_URI, null);
        final int apiPort = prefs.getInt(PREFS_API_PORT, 80);
         if (apiUri == null)
         {
             error = true;
         } // if
         else
         {
             try
             {
                 mApiUri = new URI("http", null, apiUri, apiPort, null, null, null );
             } // try
             catch (final URISyntaxException e)
             {
                 error = true;
             } // catch
         } // else
         if (error)
         {
             SmsProxyManager.sendMessage("API URI invalid. Please correct and restart.");
             stopSelf();
         } // if
         else
         {
             SmsProxyManager.sendMessage("API URI: " + mApiUri);
         } // else
     } // buildApiUri
 
     private void registerSmsBroadcastReciever()
     {
         registerReceiver(mSmsBroadcastReciever,
                 new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
     } // registerSmsBroadcastReciever
 
     private void startForeground()
     {
         final Notification notification = new Notification(
                 R.drawable.sms_icon,
                 null /* tickerText */,
                 0L /* when */);
         notification.setLatestEventInfo(this,
                 getString(R.string.app_name),
                 getString(R.string.notificaiton_message),
                 PendingIntent.getActivity(
                         this,
                         0 /* requestCode */,
                         new Intent(this, ControlProxyActivity.class),
                         0 /* flags */));
         startForeground(NOTIFICATION_ID, notification);
     } // startForeground
 
     @Override
     public IBinder onBind(final Intent intent)
     {
         return null;
     } // onBind
 
     @Override
     protected void onHandleIntent(final Intent intent)
     {
         final SmsMessageInfo[] smsList = getSmsList(intent);
         if (smsList != null)
         {
             try
             {
                 handleSmsList(smsList);
             } // catch
             catch (final ClientProtocolException e)
             {
                 Log.e(TAG, "Failed to handle message.", e);
                 SmsProxyManager.sendMessage(
                         "[Error] Error during server communication (" + e.getMessage() + ")");
             } // catch
             catch (final UnsupportedEncodingException e)
             {
                 Log.e(TAG, "Failed to handle message.", e);
                 SmsProxyManager.sendMessage(
                         "[Error] Message contained unknown characters (" + e.getMessage() + ")");
             } // catch
             catch (final IOException e)
             {
                 Log.e(TAG, "Failed to handle message.", e);
                 SmsProxyManager.sendMessage(
                         "[Error] Cannot parse server response (" + e.getMessage() + ")");
             } // catch
         } // if
         else
         {
             Log.d(TAG, "Intent does not contain a valid list of SMS messages. Ignoring " + intent
                     + ".");
         } // else
     } // onHandleIntent
 
     private SmsMessageInfo[] getSmsList(final Intent intent)
     {
         final Bundle extras = intent.getExtras();
         if (extras == null || !extras.containsKey(EXTRA_PDUS))
         {
             return null;
         } // if
         Object obj = extras.get(EXTRA_PDUS);
         if (!(obj instanceof Object[]))
         {
             Log.w(TAG, "[getSmsList] pdus extra not instance of Objectp[].");
             return null;
         } // if
         final Object[] objArray = (Object[]) obj;
         final SmsMessageInfo[] smsList = new SmsMessageInfo[objArray.length];
         for (int objArrayIndex = 0; objArrayIndex < objArray.length; objArrayIndex++)
         {
             if (!(objArray[objArrayIndex] instanceof byte[]))
             {
                 Log.w(TAG, "[getSmsList] Array object not istance of byte[].");
                 return null;
             } // if
             try
             {
                 smsList[objArrayIndex] = SmsMessageInfo.fromPdu((byte[]) objArray[objArrayIndex]);
             }
             catch (final IOException e)
             {
                 Log.w(TAG, "[getSmsList] Passed invalid message.");
                 return null;
             } // catch
         } // for
         return smsList;
     } // getSmsList
 
     private void handleSmsList(final SmsMessageInfo[] smsList) throws ClientProtocolException,
             UnsupportedEncodingException, IOException
     {
         for (final SmsMessageInfo sms : smsList)
         {
             handleSms(sms);
         } // for
     } // handleSmsList
 
     private void handleSms(final SmsMessageInfo sms) throws ClientProtocolException,
             UnsupportedEncodingException, IOException
     {
         final SmsManager smsManager = SmsManager.getDefault();
 
         for (final SmsMessageInfo reply : execute(buildRequest(sms)))
         {
             SmsProxyManager.sendMessage("[Out] " + reply.getNumber() + " | " + reply.getMessage());
             smsManager.sendTextMessage(
                         reply.getNumber(),
                         null /* scAddress */,
                         reply.getMessage(),
                         null /* sentIntent */,
                         null /* deliveryIntent */);
         } // for
     } // handleSms
 
     private HttpPost buildRequest(final SmsMessageInfo sms) throws UnsupportedEncodingException
     {
         SmsProxyManager.sendMessage("[IN] " + sms.getNumber() + " | " + sms.getMessage());
         final HttpPost post = new HttpPost(mApiUri);
         final MultipartEntity form = new MultipartEntity();
         form.addPart(new FormBodyPart(FORM_MESSAGE, new StringBody(sms.getMessage())));
         form.addPart(new FormBodyPart(FORM_NUMBER, new StringBody(sms.getNumber())));
         post.setEntity(form);
         return post;
     } // buildRequest
 
     private SmsMessageInfo[] execute(final HttpPost request) throws ClientProtocolException,
             IOException
     {
         SmsProxyManager.sendMessage("Contacting server.");
         return mHttpClient.execute(request, mSmsReplyResponseHandler);
     } // execute
 
     @Override
     public void onDestroy()
     {
         SmsProxyManager.sendMessage("Stopping server.");
         stopForeground(true);
         unregisterReceiver(mSmsBroadcastReciever);
         mHttpClient.getConnectionManager().shutdown();
         SmsProxyManager.setRunning(false);
         super.onDestroy();
     } // onDestroy
 
 } // SmsService
