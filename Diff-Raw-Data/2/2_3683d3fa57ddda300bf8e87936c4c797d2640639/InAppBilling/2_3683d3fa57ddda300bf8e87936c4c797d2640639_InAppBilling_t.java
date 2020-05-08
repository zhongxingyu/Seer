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
 
 import com.baroq.pico.google.iab.IabHelper;
 import com.baroq.pico.google.iab.IabResult;
 import com.baroq.pico.google.iab.Inventory;
 import com.baroq.pico.google.iab.Purchase;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class InAppBilling extends CordovaPlugin{
     private static final String TAG = "PICO-GOOG-IAP";
    private static final String PUBLIC_KEY = "GET_THIS_FROM_GOOGLE_PLAY_DEVELOPER_CONSOLE_SERVICES_AND_API";
     
     private static final int ACT_CB_IAP = 10001;
     private static final int ACT_CB_SUB = 10002;
     
     private static final String ACTION_OPEN = "open";
     private static final String ACTION_CLOSE = "close";
     private static final String ACTION_INV = "inventory";
     private static final String ACTION_BUY = "buy";
     private static final String ACTION_SUB = "subscribe";
     private static final String ACTION_CONSUME = "consume";
 
     // The helper object
     IabHelper mHelper;
 
     // Plugin action handler
     @Override
     public boolean execute(String action, JSONArray data,  CallbackContext callbackContext) {
         PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
         pluginResult.setKeepCallback(true);
 
         try{
             if (ACTION_OPEN.equals(action)) {
                 open(cordova.getActivity(), PUBLIC_KEY, callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             } else if (ACTION_CLOSE.equals(action)){
                 close();
                 callbackContext.success();
             } else if (ACTION_INV.equals(action)){
                 List<String> moreSkus = new ArrayList<String>();
 
                 for (int i=0, l=data.length(); i<l; i++) {
                     moreSkus.add( data.getString(i) );
                 }
 
                 inventory(moreSkus, callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             } else if (ACTION_BUY.equals(action)){
                 // buy in app item, data[0]: sku, data[1]: payload
                 buy(cordova.getActivity(), data.getString(0), data.getString(1), callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             } else if (ACTION_SUB.equals(action)){
                 // subscribe in app service, data[0]: sku, data[1]: payload
                 subscribe(cordova.getActivity(), data.getString(0), data.getString(1), callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             } else if (ACTION_CONSUME.equals(action)){
                 consume(new Purchase(IabHelper.ITEM_TYPE_INAPP, data.getString(0), ""), callbackContext);
                 callbackContext.sendPluginResult(pluginResult);
             } else{
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
         close();
     }
 
     private void open(Activity activity, String key, final CallbackContext callbackContext){
         close();
         // Create the helper, passing it our context and the public key to verify signatures with
         Log.d(TAG, "Creating IAB helper.");
         mHelper = new IabHelper(activity, key);
 
         // enable debug logging (for a production application, you should set this to false).
         mHelper.enableDebugLogging(true);
 
         Log.d(TAG, "Setup onActivityCallback to this");
         cordova.setActivityResultCallback(this);
 
         // Start setup. This is asynchronous and the specified listener
         // will be called once setup completes.
         Log.d(TAG, "Starting setup.");
         mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
             public void onIabSetupFinished(IabResult result) {
                 Log.d(TAG, "Setup finished.");
 
                 if (!result.isSuccess()) {
                     // Oh noes, there was a problem.
                     callbackContext.error("Problem setting up in-app billing: " + result);
                     return;
                 }
 
                 // Have we been disposed of in the meantime? If so, quit.
                 if (mHelper == null) {
                     callbackContext.error("The billing helper has been disposed");
                     return;
                 }
 
                 // IAB is fully set up. Now, let's get an inventory of stuff we own.
                 Log.d(TAG, "Setup successful.");
                 callbackContext.success("Init successful");
             }
         });
     }
 
     private void close(){
         if (null != mHelper){
             mHelper.dispose();
             mHelper = null;
         }
     }
 
     private void inventory(List<String> moreSkus, final CallbackContext callbackContext){
         Log.d(TAG, "Querying inventory.");
         mHelper.queryInventoryAsync(true, moreSkus, new IabHelper.QueryInventoryFinishedListener() {
             public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                 Log.d(TAG, "Query inventory finished.");
                 
                 // Have we been disposed of in the meantime? If so, quit.
                 if (mHelper == null) {
                     callbackContext.error("The billing helper has been disposed");
                     return;
                 }
                 
                 // Is it a failure?
                 if (result.isFailure()) {
                     callbackContext.error("Failed to query inventory: " + result);
                     return;
                 }
 
                 Log.d(TAG, "detail list size: "+inventory.jsonSkuDetailsList.size());
                 JSONObject json = new JSONObject();
                 JSONArray ownedSkus = new JSONArray();
                 JSONArray purchaseDataList = new JSONArray();
                 JSONArray signatureList = new JSONArray();
                 JSONArray skuDetailsList = new JSONArray();
                 int i, l;
                 ArrayList<String> list1, list2, list3;
                 try{
                     list1 = inventory.jsonOwnedSkus;
                     list2 = inventory.jsonPurchaseDataList;
                     list3 = inventory.jsonSignatureList;
                     for(i=0, l=list1.size(); i<l; i++){
                         ownedSkus.put(list1.get(i));
                         purchaseDataList.put(new JSONObject(list2.get(i)));
                         signatureList.put(list3.get(i));
                     }
                     list1 = inventory.jsonSkuDetailsList;
                     for(i=0, l=list1.size(); i<l; i++){
                         skuDetailsList.put(new JSONObject(list1.get(i)));
                     }
                     json.put("ownedSkus", ownedSkus);
                     json.put("purchaseDataList", purchaseDataList);
                     json.put("signatureList", signatureList);
                     json.put("skuDetailsList", skuDetailsList);
                 }catch(JSONException ex){
                     callbackContext.error("Failed to contruct inventory json: " + ex);
                     return;
                 }
                 
                 Log.d(TAG, "Query inventory was successful.");
                 PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                 callbackContext.sendPluginResult(pluginResult);
             }
         });
     }
 
     private void buy(Activity act, String sku, String payload, CallbackContext callbackContext){
 
         if (mHelper == null){
             callbackContext.error("Did you forget to initialize the plugin?");
             return;
         } 
 
         Log.d(TAG, "launching purchase flow for item purchase.");
         purchase(act, sku, payload, IabHelper.ITEM_TYPE_INAPP, ACT_CB_IAP, callbackContext);
     }
 
     private void subscribe(Activity act, String sku, String payload, CallbackContext callbackContext){
 
         if (mHelper == null){
             callbackContext.error("Did you forget to initialize the plugin?");
             return;
         } 
 
         if (!mHelper.subscriptionsSupported()) {
             callbackContext.error("Subscriptions not supported on your device yet. Sorry!");
             return;
         }
 
         Log.d(TAG, "Launching purchase flow for subscription.");
         purchase(act, sku, payload, IabHelper.ITEM_TYPE_SUBS, ACT_CB_SUB, callbackContext);
     }
 
     private void purchase(Activity act, String sku, String payload, String itemType, int cbId, final CallbackContext callbackContext){
         mHelper.launchPurchaseFlow(act, sku, itemType, cbId, 
             new IabHelper.OnIabPurchaseFinishedListener() {
                 public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                     Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
 
                     // if we were disposed of in the meantime, quit.
                     if (mHelper == null) {
                         callbackContext.error("The billing helper has been disposed");
                         return;
                     }
 
                     if (result.isFailure()) {
                         callbackContext.error("Error purchasing: " + result);
                         return; 
                     }
 
                     JSONObject json = new JSONObject();
                     try{
                         json.put("purchaseData", new JSONObject(purchase.getOriginalJson()));
                         json.put("itemType", purchase.getItemType());
                         json.put("signature", purchase.getSignature());
                     }catch(JSONException ex){
                         callbackContext.error("Failed to contruct purchase json: " + ex);
                         return;
                     }
                     
                     Log.d(TAG, "Query purchase was successful.");
                     PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                     callbackContext.sendPluginResult(pluginResult);
                 }
             }, payload);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
 
         // Pass on the activity result to the helper for handling
         if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
             // not handled, so handle it ourselves (here's where you'd
             // perform any handling of activity results not related to in-app
             // billing...
             super.onActivityResult(requestCode, resultCode, data);
         } else {
             Log.d(TAG, "onActivityResult handled by "+TAG);
         }
     }
 
     private void consume(Purchase purchase, final CallbackContext callbackContext){
 
         if (mHelper == null){
             callbackContext.error("Did you forget to initialize the plugin?");
             return;
         } 
 
         mHelper.consumeAsync(purchase, new IabHelper.OnConsumeFinishedListener() {
             public void onConsumeFinished(Purchase purchase, IabResult result) {
                 Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
                 
                 // We know this is the "gas" sku because it's the only one we consume,
                 // so we don't check which sku was consumed. If you have more than one
                 // sku, you probably should check...
                 if (result.isSuccess()) {
                     // successfully consumed, so we apply the effects of the item in our
                     // game world's logic
                     
                     // remove the item from the inventory
                     Log.d(TAG, "Consumption successful. .");
                     
                     callbackContext.success(purchase.getOriginalJson());
                     
                 } else {
                     callbackContext.error("Error while consuming: " + result);
                 }
                 
             }
         });
     }
 };
