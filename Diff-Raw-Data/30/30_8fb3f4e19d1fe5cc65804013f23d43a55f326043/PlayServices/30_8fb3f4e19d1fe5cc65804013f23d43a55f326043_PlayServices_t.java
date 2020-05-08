 // ref: http://developer.android.com/google/play/billing/billing_integrate.html
 package com.baroq.pico.google;
 
 import org.apache.cordova.api.CordovaPlugin;
 import org.apache.cordova.api.PluginResult;
 import org.apache.cordova.api.CallbackContext;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.google.android.gms.appstate.AppStateClient;
 import com.google.android.gms.appstate.AppStateBuffer;
 import com.google.android.gms.appstate.AppState;
 import com.google.android.gms.appstate.OnStateLoadedListener;
 import com.google.android.gms.appstate.OnStateListLoadedListener;
 import com.google.android.gms.appstate.OnStateDeletedListener;
 import com.baroq.pico.google.gms.GmsHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PlayServices   extends     CordovaPlugin
                             implements
                                         GmsHelper.GmsHelperListener,
                                         OnStateLoadedListener,
                                         OnStateListLoadedListener,
                                         OnStateDeletedListener {
     private static final String TAG = "PICO-GOOG-GMS";
     private static final boolean DEBUG_ENABLED = true;    
 
     private static final String ACTION_SETUP = "setup";
     private static final String ACTION_SIGNIN = "signin";
     private static final String ACTION_SIGNOUT = "signout";
     private static final String ACTION_AS_MAX_KEYS = "getMaxNumKeys";
     private static final String ACTION_AS_MAX_SIZE = "getMaxStateSize";
     private static final String ACTION_AS_STATE_DEL = "deleteState";
     private static final String ACTION_AS_STATE_LIST = "listStates";
     private static final String ACTION_AS_STATE_LOAD = "loadState";
     private static final String ACTION_AS_STATE_RESOLVE = "resolveState";
     private static final String ACTION_AS_STATE_UPDATE = "updateState";
     private static final String ACTION_AS_STATE_UPDATE_NOW = "updateStateImmediate";
     
     private static final int GMS_SIGNIN = 1;
     private static final int STATE_LOADED = 2;
     private static final int STATE_LIST_LOADED = 3;
     private static final int STATE_CONFLICTED = 4;
     private static final int STATE_DELETED = 5;
 
     GmsHelper mHelper;
     CallbackContext connectionCB;
     int clientTypes = GmsHelper.CLIENT_NONE;
 
     // Plugin action handler
     @Override
     public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
         PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
         pluginResult.setKeepCallback(true);
 
         try{
             if (!ACTION_SETUP.equals(action) && !ACTION_SIGNIN.equals(action) && null == mHelper){
                 callbackContext.error("Please setup and signin to use PlayServices plugin");
                 return false;
             }
             if (ACTION_SETUP.equals(action)) {
                 int l = data.length();
                 if (0 == l) {
                     callbackContext.error("Expecting at least 1 parameter for action: "+action);
                     return false;
                 }
                 clientTypes = data.getInt(0);
                 String[] extraScopes = new String[l-1];
                 for(int i=1; i<l; i++){
                     extraScopes[i-1] = data.getString(i);
                 }
                 setup(clientTypes, extraScopes, callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             }else if (ACTION_SIGNIN.equals(action)){
                 mHelper.beginUserInitiatedSignIn();
                 callbackContext.success();
             }else if (ACTION_SIGNOUT.equals(action)){
                 signout();
                 callbackContext.success();
             }else if (ACTION_AS_MAX_KEYS.equals(action)){
                pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxNumKeys());
                 pluginResult.setKeepCallback(false);
                 callbackContext.sendPluginResult(pluginResult);
             }else if (ACTION_AS_MAX_SIZE.equals(action)){
                 pluginResult = new PluginResult(PluginResult.Status.OK, mHelper.getAppStateClient().getMaxStateSize());
                 pluginResult.setKeepCallback(false);
                 callbackContext.sendPluginResult(pluginResult);
             }else if (ACTION_AS_STATE_DEL.equals(action)){
                 int key = data.getInt(0);
                 mHelper.getAppStateClient().deleteState(this, key);
                 callbackContext.success();
             }else if (ACTION_AS_STATE_LIST.equals(action)){
                 mHelper.getAppStateClient().listStates(this);
                 callbackContext.success();
             }else if (ACTION_AS_STATE_LOAD.equals(action)){
                 int key = data.getInt(0);
                 mHelper.getAppStateClient().loadState(this, key);
                 callbackContext.success();
             }else if (ACTION_AS_STATE_RESOLVE.equals(action)){
                 int key = data.getInt(0);
                 String resolvedVersion = data.getString(1);
                 String value = data.getString(2);
                 mHelper.getAppStateClient().resolveState(this, key, resolvedVersion, value.getBytes());
                 callbackContext.success();
             }else if (ACTION_AS_STATE_UPDATE.equals(action)){
                 int key = data.getInt(0);
                 String value= data.getString(1);
Log.d(TAG, "mHelper.getAppStateClient().updateState(this, "+key+", "+value+");");
                 mHelper.getAppStateClient().updateState(key, value.getBytes());
                 callbackContext.success();
             }else if (ACTION_AS_STATE_UPDATE_NOW.equals(action)){
                 int key = data.getInt(0);
                 String value = data.getString(1);
Log.d(TAG, "mHelper.getAppStateClient().updateStateImmediate(this, "+key+", "+value+");");
                 mHelper.getAppStateClient().updateStateImmediate(this, key, value.getBytes());
                 callbackContext.success();
             }else{
                 callbackContext.error("Unknown action: " + action);
                 return false;
             }
         }catch(JSONException ex){
             callbackContext.error(ex.getMessage());
             return false;
         }
 
         return true;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         signout();
     }
 
     @Override
     public void onSignInSucceeded(){
         JSONObject json = new JSONObject();
         try{
             json.put("type", GMS_SIGNIN);
             json.put("signin", true);
         }catch(JSONException ex){
            Log.e(TAG, "signin succeeded exception: "+ex.getMessage());
             return;
         }
         Log.d(TAG, "signin succeeded");
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     @Override
     public void onSignInFailed(String reason){
         JSONObject json = new JSONObject();
         try{
             json.put("type", GMS_SIGNIN);
             json.put("signin", false);
             json.put("message", reason);
         }catch(JSONException ex){
            Log.e(TAG, "signin failed exception: "+ex.getMessage());
             return;
         }
         Log.d(TAG, "signin failed");
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     @Override
     public void onActivityResult(int requestCode, int responseCode, Intent data) {
         Log.d(TAG, "onActivityResult(" + requestCode + "," + responseCode + "," + data);
 
         super.onActivityResult(requestCode, responseCode, data);
         mHelper.onActivityResult(requestCode, responseCode, data);
     }
 
     @Override           
     public void onStateLoaded(int statusCode, int stateKey, byte[] localData) {
         JSONObject json = new JSONObject();
         try{
             json.put("type", STATE_LOADED);
             json.put("status", statusCode);
             switch (statusCode) {
                 case AppStateClient.STATUS_OK:
                     // Data was successfully loaded from the cloud: merge with local data.
                     json.put("stateKey", stateKey);
                     json.put("data", new JSONObject(new String(localData)));
                     break;        
                 case AppStateClient.STATUS_STATE_KEY_NOT_FOUND:
                     // key not found means there is no saved data. To us, this is the same as
                     // having empty data, so we treat this as a success.
                     break;
                 case AppStateClient.STATUS_STATE_KEY_LIMIT_EXCEEDED:
                     // if the application already has data present in the maximum number of state keys.
                     break;
                 case AppStateClient.STATUS_NETWORK_ERROR_NO_DATA:
                     // can't reach cloud, and we have no local state. Warn user that
                     // they may not see their existing progress, but any new progress won't be lost.
                     break;
                 case AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA:
                     // can't reach cloud, but we have locally cached data.
                     break;
                 case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                     // need to reconnect AppStateClient
                     mHelper.reconnectClients(clientTypes);
                     break;
                 case AppStateClient.STATUS_INTERNAL_ERROR:
                     // if an unexpected error occurred in the service.
                     break;
                 default:
                     // error
                     break;
             }
         }catch(JSONException ex){
            Log.e(TAG, "STATE_LOADED ["+statusCode+"] ["+stateKey+"] exception: "+ex.getMessage());
             return;
         }
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     @Override
     public void onStateConflict(int stateKey, String resolvedVersion, byte[] localData, byte[] serverData) {
         // Need to resolve conflict between the two states.
         // We do that by taking the union of the two sets of cleared levels,
         // which means preserving the maximum star rating of each cleared
         // level:
         JSONObject json = new JSONObject();
         try{
             json.put("type", STATE_CONFLICTED);
             json.put("stateKey", stateKey);
             json.put("version", resolvedVersion);
             json.put("localData", new JSONObject(new String(localData)));
             json.put("serverData", new JSONObject(new String(serverData)));
         }catch(JSONException ex){
            Log.e(TAG, "STATE_CONFLICTED ["+stateKey+"] ["+resolvedVersion+"] exception: "+ex.getMessage());
             return;
         }
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     @Override
     public void onStateListLoaded(int statusCode, AppStateBuffer buffer) {
         JSONObject json = new JSONObject();
         try{
             json.put("type", STATE_LIST_LOADED);
             json.put("status", statusCode);
             switch (statusCode) {
                 case AppStateClient.STATUS_OK:
                     JSONArray jsonStates = new JSONArray();
                     json.put("states", jsonStates);
                     AppState state;
                     JSONObject jsonState;
                    for(int i=0,l=buffer.getCount();i<l;i++){
                         state = buffer.get(i);
                         jsonState = new JSONObject();
                         if (state.hasConflict()){
                             jsonState.put("type", STATE_CONFLICTED);
                             jsonState.put("stateKey", state.getKey());
                             jsonState.put("version", state.getConflictVersion());
                             jsonState.put("localVersion", state.getLocalVersion());
                             jsonState.put("localData", new JSONObject(new String(state.getLocalData())));
                             jsonState.put("serverData", new JSONObject(new String(state.getConflictData())));
                         }else{
                             jsonState.put("type", STATE_LOADED);
                             jsonState.put("status", statusCode);
                             jsonState.put("stateKey", state.getKey());
                             jsonState.put("data", new JSONObject(new String(state.getLocalData())));
                         }
                         jsonStates.put(jsonState);
                     }
                     // Data was successfully loaded from the cloud: merge with local data.
                     break;        
                 case AppStateClient.STATUS_NETWORK_ERROR_NO_DATA:
                     // can't reach cloud, and we have no local state. Warn user that
                     // they may not see their existing progress, but any new progress won't be lost.
                     break;
                 case AppStateClient.STATUS_NETWORK_ERROR_STALE_DATA:
                     // can't reach cloud, but we have locally cached data.
                     break;
                 case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                     // need to reconnect AppStateClient
                     mHelper.reconnectClients(clientTypes);
                     break;
                 case AppStateClient.STATUS_INTERNAL_ERROR:
                     // if an unexpected error occurred in the service.
                     break;
                 default:
                     // error
                     break;
             }
         }catch(JSONException ex){
            Log.e(TAG, "STATE_LIST_LOADED ["+statusCode+"]["+buffer.getCount()+"] exception: "+ex.getMessage());
             return;
         }
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     @Override
     public void onStateDeleted(int statusCode, int stateKey){
         JSONObject json = new JSONObject();
         try{
             json.put("type", STATE_CONFLICTED);
             json.put("statusCode", statusCode);
             json.put("stateKey", stateKey);
             switch (statusCode) {
                 case AppStateClient.STATUS_OK:
                     // if data was successfully deleted from the server.
                     break;        
                 case AppStateClient.STATUS_INTERNAL_ERROR:
                     // if an unexpected error occurred in the service 
                     break;
                 case AppStateClient.STATUS_NETWORK_ERROR_OPERATION_FAILED:
                     // if the device was unable to communicate with the network. In this case, the operation is not retried automatically.
                     break;
                 case AppStateClient.STATUS_CLIENT_RECONNECT_REQUIRED:
                     // need to reconnect AppStateClient
                     mHelper.reconnectClients(clientTypes);
                     break;
                 default:
                     // error
                     break;
             }
         }catch(JSONException ex){
            Log.e(TAG, "STATE_DELETED ["+statusCode+"] ["+stateKey+"] exception: "+ex.getMessage());
             return;
         }
 
         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
         pluginResult.setKeepCallback(true);
         connectionCB.sendPluginResult(pluginResult);
     }
 
     private void setup(int clientTypes, String[] extraScopes, final CallbackContext callbackContext){
         mHelper = new GmsHelper(cordova.getActivity());
         connectionCB = callbackContext;
 
         Log.d(TAG, "Setup onActivityCallback to this");
         cordova.setActivityResultCallback(this);
 
         mHelper.enableDebugLog(DEBUG_ENABLED, TAG);
 
         mHelper.setup(this, clientTypes, extraScopes);
     }
 
     private void signout(){
         mHelper.signOut();
     }
 };
