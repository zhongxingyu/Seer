 package com.frca.purtges.requests;
 
 import android.app.Activity;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.frca.purtges.Const.Ids;
 import com.frca.purtges.services.EndpointService;
 import com.frca.purtges.requests.callbacks.QueryTask;
 import com.frca.purtges.requests.callbacks.ResultCallback;
 import com.frca.purtges.userdataendpoint.model.UserData;
 import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
 
 public class RequestManager {
 
     private final GoogleAccountCredential credential;
 
     private final EndpointHolder endpoints;
 
     private final NetworkRunnable networkRunnable = new NetworkRunnable();
 
     private final Thread workingThread = new Thread(networkRunnable);
 
     public RequestManager(Activity activity, String selectedAccount) {
         credential = GoogleAccountCredential.usingAudience(activity, Ids.AUDIENCE_SCOPE);
         endpoints = new EndpointHolder(credential);
 
         if (selectedAccount != null) {
             credential.setSelectedAccountName(selectedAccount);
         }
         else
             activity.startActivityForResult(credential.newChooseAccountIntent(), EndpointService.REQUEST_ACCOUNT_PICKER);
     }
 
     public boolean isPrepared() {
         if (TextUtils.isEmpty(credential.getSelectedAccountName()))
             return false;
 
         return true;
     }
 
     public void setAccountName(String selectedAccount) {
         credential.setSelectedAccountName(selectedAccount);
     }
 
     public GoogleAccountCredential getCredential() {
         return credential;
     }
 
     public EndpointHolder getEndpoints() {
         return endpoints;
     }
 
     private void addTask(NetworkTask task) {
         task.setTaskName(getCurrentMethodName());
 
         Log.d("TASK_MGMNT", "Adding task `" + task.getTaskName() + "` to queue.");
         networkRunnable.addTask(task);
 
         if (!workingThread.isAlive())
             workingThread.start();
     }
 
     private void addTask(QueryTask queryTask, ResultCallback resultCallback) {
         addTask(new NetworkTask(queryTask, resultCallback));
     }
 
     public void getOwnUserData(ResultCallback callback) {
         com.frca.purtges.userdataendpoint.model.Key key = new com.frca.purtges.userdataendpoint.model.Key();
         key.setId(Long.valueOf(0));
         getUserData(key, callback);
     }
 
     public void getUserData(final com.frca.purtges.userdataendpoint.model.Key id, ResultCallback callback) {
         addTask(new QueryTask() {
             @Override
             public Object query() throws Exception {
                 return endpoints.userData().claimUserData(id).execute();
             }
         }, callback);
     }
 
     public void insertUserData(final UserData userData, ResultCallback callback) {
         addTask(new QueryTask() {
 
             @Override
             public Object query() throws Exception {
                 return endpoints.userData().insertUserData(userData).execute();
             }
         }, callback);
     }
 
     public void updateUserData(final UserData userData, ResultCallback callback) {
         addTask(new QueryTask() {
 
             @Override
             public Object query() throws Exception {
                 return endpoints.userData().updateUserData(userData).execute();
             }
         }, callback);
     }
 
     public String getCurrentMethodName() {
         final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[4].getMethodName();
     }
 }
