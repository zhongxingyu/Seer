 package com.kii.push;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.google.android.gcm.GCMRegistrar;
 import com.kii.cloud.storage.Kii;
 import com.kii.cloud.storage.KiiBucket;
 import com.kii.cloud.storage.KiiPushMessage;
 import com.kii.cloud.storage.KiiTopic;
 import com.kii.cloud.storage.KiiUser;
 import com.kii.cloud.storage.exception.app.AppException;
 import com.kii.cloud.storage.exception.app.BadRequestException;
 import com.kii.cloud.storage.exception.app.ConflictException;
 
 public class KiiPushAppTask extends AsyncTask<Object, Void, String> {
 
     private static final String TAG = "KiiPushAppTask";
     int menuId;
     Activity activity;
     String menuString;
     Exception e;
     PrefWrapper pref;
 
     public static class MENU_ID {
         public static final int INSTALL_PUSH = 0;
         public static final int SUBSCRIBE_BUCKET = 1;
         public static final int UNSUBSCRIBE_BUCKET = 2;
         public static final int BUCKET_CONTROLL = 3;
         public static final int UNREGISTER_GCM = 4;
         public static final int CREATE_USCOPE_TOPIC = 5;
         public static final int SUBSCRIBE_USCOPE_TOPIC = 6;
         public static final int SENDMESSAGE_TO_USCOPE_TOPIC = 7;
         public static final int GSCOPE_TOPIC = 8;
         public static final int SUBSCRIBE_ASCOPE_TOPIC = 9;
         public static final int SUBSCRIBE_ABUCKET = 10;
         public static final int LOGIN = Integer.MAX_VALUE;
     }
 
     public KiiPushAppTask(int menuId, String menuString, Activity activity) {
         this.menuId = menuId;
         this.activity = activity;
         this.menuString = menuString;
         this.pref = PrefWrapper.getInstance(activity);
     }
 
     @Override
     protected void onPostExecute(String extra) {
         // TODO: ListActivityCore is horrible abuse of inheritance..
         ListActivityCore.closeDialog(ListActivityCore.DIALOG_PROGRESS,
                 activity);
         StringBuilder b = new StringBuilder();
         b.append(this.menuString);
         b.append(" : ");
         if (!TextUtils.isEmpty(extra)) {
             b.append(" extra: " + extra);
             b.append(" : ");
         }
         b.append(getTaskResultString(this.e));
         String msg = b.toString();
         Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
     }
 
     private String getTaskResultString(Exception result) {
         if (result == null) {
             return "Succeeded.";
         } else {
             if (result instanceof ConflictException) {
                 return "Entity already exist.";
             } else {
                 result.printStackTrace();
                 return "Failed: " + result.getMessage();
             }
         }
     }
 
     @Override
     protected void onPreExecute() {
         // TODO use flagment.
         ListActivityCore.openDialog(ListActivityCore.DIALOG_PROGRESS,
                 "Processing...", activity);
     }
 
     @Override
     protected String doInBackground(Object... args) {
         switch (menuId) {
         case MENU_ID.INSTALL_PUSH:
             return doInstallPush((String) args[0]);
         case MENU_ID.SUBSCRIBE_BUCKET:
             return doSubscribeBucket();
         case MENU_ID.UNSUBSCRIBE_BUCKET:
             return doUnsubscribeBucket();
         case MENU_ID.BUCKET_CONTROLL:
             return doBucketControl();
         case MENU_ID.UNREGISTER_GCM:
             return doUnregisterGCM();
         case MENU_ID.CREATE_USCOPE_TOPIC:
             return doCreateUscopeTopic();
         case MENU_ID.SENDMESSAGE_TO_USCOPE_TOPIC:
             return doSendMessageToUserScopeTopic();
         case MENU_ID.SUBSCRIBE_USCOPE_TOPIC:
             return doSubscribeUserScopeTopic();
         case MENU_ID.SUBSCRIBE_ASCOPE_TOPIC:
             return doSubscribeAppScopeTopic();
         case MENU_ID.SUBSCRIBE_ABUCKET:
             return doSubscribeAppBucket();
         case MENU_ID.LOGIN:
             return doLogin((String)args[0], (String)args[1]);
         default:
             throw new RuntimeException("Unkown id: " + menuId);
         }
     }
 
     private String doSubscribeAppBucket() {
         try {
             String bucketName = PropertyManager.getInstance()
                     .getAppBucketName();
             KiiBucket appBucket = Kii.bucket(bucketName);
             Kii.user().pushSubscription().subscribe(appBucket);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doSubscribeAppScopeTopic() {
         try {
             PropertyManager pm = PropertyManager.getInstance(); 
             KiiTopic aTopic = Kii.topic(pm.getAppTopicName());
             Kii.user().pushSubscription().subscribe(aTopic);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doLogin(String username, String password) {
         try {
             KiiUser.logIn(username, password);
             PrefWrapper.getInstance(this.activity).setUsername(username);
             PrefWrapper.getInstance(this.activity).setPassword(password);
         } catch (BadRequestException e) {
             // If cannot login, create NewUser.
             try {
                 KiiUser user = KiiUser.createWithUsername(username);
                 user.register(password);
                 PrefWrapper.getInstance(this.activity).setUsername(username);
                 PrefWrapper.getInstance(this.activity).setPassword(password);
             } catch (Exception e1) {
                 this.e = e1;
             }
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doUnregisterGCM() {
         try {
             assertGCMRegistred();
            GCMRegistrar.unregister(this.activity);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doBucketControl() {
         throw new RuntimeException("Do not call for this menu.");
     }
 
     private String doUnsubscribeBucket() {
         try {
             assertGCMRegistred();
             KiiUser user = KiiUser.getCurrentUser();
             KiiBucket bucket = user.bucket(Constants.PUSH_BUCKET_NAME);
             user.pushSubscription().unsubscribeBucket(bucket);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doSubscribeBucket() {
         try {
             assertGCMRegistred();
             KiiUser user = KiiUser.getCurrentUser();
             KiiBucket bucket = user.bucket(Constants.PUSH_BUCKET_NAME);
             user.pushSubscription().subscribeBucket(bucket);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doInstallPush(String regId) {
         try {
             Log.v(TAG, "Installing with Registration Id :" + regId);
             KiiUser.pushInstallation().install(regId);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doCreateUscopeTopic() {
         try {
             assertGCMRegistred();
             KiiTopic topic = KiiUser.topic(Constants.USERTOPIC);
             topic.save();
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doSubscribeUserScopeTopic() {
         try {
             assertGCMRegistred();
             KiiTopic topic = KiiUser.topic(Constants.USERTOPIC);
             KiiUser.getCurrentUser().pushSubscription().subscribe(topic);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private String doSendMessageToUserScopeTopic() {
         KiiPushMessage.Data data = new KiiPushMessage.Data();
         data.put("custom_message", "Hello, Kii Push servce!");
         KiiPushMessage msg = KiiPushMessage.buildWith(data).build();
         try {
             assertGCMRegistred();
             KiiTopic topic = KiiUser.topic(Constants.USERTOPIC);
             topic.sendMessage(msg);
         } catch (Exception e) {
             this.e = e;
         }
         return null;
     }
 
     private void assertGCMRegistred() throws IllegalStateException {
        String regId = GCMRegistrar.getRegistrationId(this.activity);
         if (TextUtils.isEmpty(regId)) {
             throw new IllegalStateException("Register GCM before.");
         }
     }
 }
