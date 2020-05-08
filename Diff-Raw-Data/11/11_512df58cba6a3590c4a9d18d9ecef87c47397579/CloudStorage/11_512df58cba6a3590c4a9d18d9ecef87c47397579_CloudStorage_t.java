 package com.kii.cloud.sync;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 
 import android.content.Context;
import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.kii.cloud.storage.EasyClient;
 import com.kii.cloud.storage.dataType.KiiUser;
 import com.kii.cloud.storage.manager.AuthManager;
 import com.kii.cloud.storage.response.CloudExecutionException;
 import com.kii.cloud.storage.response.UserResult;
import com.kii.mobilesdk.bridge.AppUtil;
 import com.kii.mobilesdk.bridge.KiiUMInfo;
 import com.kii.sync.KiiClient;
 import com.kii.sync.SyncMsg;
 
 /**
  * The Authentication is using the Cloud Storage
  */
 public class CloudStorage implements Authentication {
 
     static final String TAG = "CloudStorageAuthentication";
 
     static final String PROPERTY_COUNTRY = "country";
 
     static final String URL_PFS = "/app/sync/pfs";
     static final String URL_RESOURCE = "/app/resource";
     static final String URL_USERGRID = ":12110";
 
     final String mBaseURL;
 
     KiiClient mSyncClient = null;
     AuthManager mUserMgr = null;
     Context mContext;
 
     public CloudStorage(Context context, KiiClient client, String baseURL) {
         mSyncClient = client;
         mContext = context;
 
         mBaseURL = baseURL;
 
        Bundle data = AppUtil.getAppInfo(context);
        String appId = data.getString(AppUtil.PREF_UM_APP_ID);
         if (TextUtils.isEmpty(appId)) {
             throw new RuntimeException(
                      "AppId meta data is not found in Manifest");
         }
        String appKey = data.getString(AppUtil.PREF_UM_APP_KEY);
         if (TextUtils.isEmpty(appKey)) {
             throw new RuntimeException("AppKey meta data is not found in Manifest");
         }
 
         EasyClient.start(context, appId, appKey);
         EasyClient.getInstance().setBaseURL(baseURL + URL_USERGRID);
         mUserMgr = EasyClient.getUserManager();
 
     }
 
     @Override
     public int changePassword(String oldPassword, String newPassword) {
 
         if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
             return SyncMsg.ERROR_INVALID_INPUT;
         }
 
         KiiUMInfo um = mSyncClient.getKiiUMInfo();
         if (um == null) {
             return SyncMsg.ERROR_USERNAME_EMPTY;
         }
 
         String username = um.getUserId();
         String password = um.getPassword();
 
         if (oldPassword.compareTo(password) == 0) {
             if (mUserMgr.getLoginUser() == null) {
 
                 try {
                     mUserMgr.login(username, oldPassword);
                 } catch (CloudExecutionException e) {
                     return SyncMsg.ERROR_AUTHENTICAION_ERROR;
                 } catch (IOException e) {
                     return SyncMsg.ERROR_IO;
                 }
             }
             UserResult result = null;
             try {
                 result = mUserMgr.changePassword(oldPassword, newPassword);
             } catch (CloudExecutionException e) {
                 return SyncMsg.ERROR_SERVER_TEMP_ERROR;
             } catch (IOException e) {
                 return SyncMsg.ERROR_IO;
             }
 
             if (result.getKiiUser() != null) {
                 return SyncMsg.OK;
             }
         }
         return SyncMsg.ERROR_AUTHENTICAION_ERROR;
     }
 
     @Override
     public int register(String email, String password, String country,
             String nickName, String mobile) {
         if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
             return SyncMsg.ERROR_INVALID_INPUT;
         }
 
         KiiUser user = new KiiUser();
         String userName = "cloud" + System.currentTimeMillis();
 
         try {
 
             // auto generate the username
             user.setUsername(userName);
 
             user.setEmail(email);
 
             if (!TextUtils.isEmpty(country)) {
                 user.put(PROPERTY_COUNTRY, country);
             }
             if (!TextUtils.isEmpty(nickName)) {
                 user.setName(nickName);
             }
             if (!TextUtils.isEmpty(mobile)) {
                 user.setName(mobile);
             }
         } catch (IllegalArgumentException e) {
             Log.e(TAG, "IllegalArgumentException:" + e.getMessage());
             return SyncMsg.ERROR_INVALID_INPUT;
         } catch (JSONException e) {
             Log.e(TAG, "JSONException:" + e.getMessage());
             return SyncMsg.ERROR_INVALID_INPUT;
         }
 
         UserResult result;
         try {
             result = mUserMgr.createUser(user, password);
         } catch (CloudExecutionException e) {
             return SyncMsg.ERROR_INVALID_INPUT;
         } catch (IOException e) {
             return SyncMsg.ERROR_IO;
         } catch (IllegalArgumentException e) {
             Log.e(TAG, "JSONException:" + e.getMessage());
             return SyncMsg.ERROR_INVALID_INPUT;
         } catch (JSONException e) {
             Log.e(TAG, "JSONException:" + e.getMessage());
             return SyncMsg.ERROR_INVALID_INPUT;
         }
         user = result.getKiiUser();
 
         if (userName.compareTo(user.getUsername()) == 0)
             return SyncMsg.OK;
 
         return SyncMsg.ERROR_UNKNOWN_STATUSCODE;
     }
 
     @Override
     public int login(String email, String password) {
         UserResult result;
         try {
             result = mUserMgr.login(email, password);
         } catch (CloudExecutionException e) {
             return SyncMsg.ERROR_AUTHENTICAION_ERROR;
         } catch (IOException e) {
             return SyncMsg.ERROR_IO;
         }
         String accType = "KII_ID";
         KiiUser user = result.getKiiUser();
         if (user != null) {
             if (email.contains("@")) {
                 accType = "EMAIL";
             }
             KiiUMInfo info = new KiiUMInfo(mContext, email, password, accType, email);
             mSyncClient.setKiiUMInfo(info);
         }
         return 0;
     }
 
     @Override
     public int logout() {
         // clear the session
         mUserMgr.logout();
         return 0;
     }
 
 }
