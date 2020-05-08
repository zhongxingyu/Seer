 package org.unitymind.vk.sdk;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.*;
 import android.util.Log;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class ApiService extends Service {
     private static final String TAG = ApiService.class.getName();
 
     public static final String SERVICE_STATE  = "org.unitymind.vk.sdk.SERVICE_STATE";
 
     public static final int MSG_CLIENT_API_CALL = 1;
     public static final int MSG_TOKEN_API_CALL = 2;
 
     public static final int MSG_SET_CREDENTIALS = 3;
     public static final int MSG_CLEAR_CREDENTIALS = 4;
     public static final int MSG_GET_STATE = 5;
 
     private Account account;
 
     class ApiCallHandler extends Handler {
 
         @Override
         public void handleMessage(Message msg) {
             Bundle msg_data = msg.getData();
             final Messenger replyTo = msg.replyTo;
 
             switch (msg.what) {
                 case MSG_SET_CREDENTIALS:
                     account.user_id = msg_data.getLong("user_id");
                     account.access_token = msg_data.getString("access_token");
                     account.save();
                     Log.i(TAG, "Account credentials is saved!");
                     Log.d(TAG, account.toString());
                    broadcastState();
                     break;
 
                 case MSG_CLEAR_CREDENTIALS:
                     account.clear();
                     Log.w(TAG, "Account credentials is cleared!");
                    broadcastState();
                     break;
 
                 case MSG_GET_STATE:
                     Bundle payload = new Bundle();
                     payload.putBoolean("registered", account.access_token != null);
                     payload.putBoolean("hasNetwork", true);
                     Log.d(TAG, "Get state is called!");
                     sendServiceReply(replyTo, payload);
                     break;
 
                 case MSG_CLIENT_API_CALL:
                     RequestParams request_params = Utils.paramsFromJson(msg_data.getString("params"));
 
                     request_params.put("client_id", Constants.CLIENT_ID);
                     request_params.put("client_secret", Constants.CLIENT_SECRET);
 
                     ApiRestClient.get(msg_data.getString("method"), request_params, new AsyncHttpResponseHandler() {
                         @Override
                         public void onSuccess(String body) {
                             Log.d(TAG, "response=" + body);
                             sendApiReply(replyTo, true, body);
                         }
 
                         @Override
                         public void onFailure(Throwable ex, String body) {
                             Log.e(TAG, ex.getMessage());
                             sendApiReply(replyTo, false, body);
                         }
                     });
 
                     break;
                 default:
                     super.handleMessage(msg);
             }
         }
 
         private void sendApiReply(Messenger replyTo, boolean success, String body) {
             if (replyTo != null) {
                 Message replyMsg = Message.obtain();
                 Bundle replyData = new Bundle();
 
                 replyData.putBoolean("success", success);
                 replyData.putString("body", body);
 
                 replyMsg.setData(replyData);
 
                 try {
                     replyTo.send(replyMsg);
                 } catch (RemoteException ex) {
                     Log.e(TAG, "ApiCallHandler error: " + ex.getMessage());
                 }
             }
         }
 
         private void sendServiceReply(Messenger replyTo, Bundle payload) {
             if (replyTo != null) {
                 Message replyMsg = Message.obtain();
                 replyMsg.setData(payload);
                 try {
                     replyTo.send(replyMsg);
                 } catch (RemoteException ex) {
                     Log.e(TAG, "ServiceCallHandler error: " + ex.getMessage());
                 }
             }
         }
     }
 
     private final Messenger messenger = new Messenger(new ApiCallHandler());
 
     @Override
     public void onCreate() {
         super.onCreate();
         account = new Account(ApiService.this);
         account.restore();
         Log.d(TAG, account.toString());
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                broadcastState();
             }
         }, 0, 60000);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return messenger.getBinder();
     }

    private void broadcastState() {
        Intent intent = new Intent(SERVICE_STATE);
        intent.putExtra("registered", account.access_token != null);
        intent.putExtra("hasNetwork", true);
        sendBroadcast(intent);
        Log.d(TAG, "Service state broadcasted!");
    }
 }
