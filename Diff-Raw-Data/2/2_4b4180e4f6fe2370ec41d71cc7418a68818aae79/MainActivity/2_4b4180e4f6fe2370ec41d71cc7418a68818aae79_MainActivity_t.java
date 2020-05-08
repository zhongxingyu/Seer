 /**
  * Habzy Huang
  * habzyhs@gmail.com
  */
 
 package com.habzy.pillow;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.IntentSender.SendIntentException;
 import android.content.ServiceConnection;
 
 import com.android.vending.billing.IInAppBillingService;
 import com.habzy.pillow.util.CameraUtil;
 import com.habzy.pillow.util.IabHelper;
 import com.habzy.pillow.util.IabResult;
 import com.habzy.pillow.util.Inventory;
 import com.habzy.pillow.util.Purchase;
 import com.habzy.pillow.util.SkuDetails;
 import com.habzy.pillow.util.Util;
 import com.tencent.mm.sdk.openapi.IWXAPI;
 import com.tencent.mm.sdk.openapi.SendMessageToWX;
 import com.tencent.mm.sdk.openapi.WXAPIFactory;
 import com.tencent.mm.sdk.openapi.WXAppExtendObject;
 import com.tencent.mm.sdk.openapi.WXMediaMessage;
 import com.tencent.mm.sdk.openapi.WXTextObject;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 public class MainActivity extends Activity implements OnClickListener {
 
     public static final String APP_ID = "wx4de4ec7895ae07ca";
 
     private static final String APP_SPECIFIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkbGhaVegh5bNOBVzyF7rX/i9FTtPsNxrl/qXZuZHqgBVLtbOmnt9XsT8UB698AtxxQCr10jjFhbzGxhfOSGl5MyZKXBAhjRHAyf2NyoKDNA2IH7yfVxxL9QSIOSE6IAjfVcrBaVcnSai2n8G3uAQGxVQgVmYyGSLtzAPf9kpQLrMTBk+6IF3WYFdu6RqnzIoHbB9vGQTq8O0KuKP6zjgbNzcntJvJda6KzMV0nj4PUozUUonZW0DU5ZxM2t1fjsB97U7eRfMKZisTuHrUiINfM3ZDL5UBtVZnQiigKxPclVm7auHHoU7Jg/NCRj7UCe3NAxlpHxtEGMWPtf0rMnDBwIDAQAB";
     //private static final String APP_SPECIFIC = "";
     
     private static final String TAG = MainActivity.class.getSimpleName();
     
     private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
 
     protected static final int UPDATE_BUY = 0;
 
     private Button mBuyButton;
 
     private Button mShareButton;
     
     private Button mShareAppButton;
 
     private Button mOpenButton;
 
     private IWXAPI api;
 
     private IInAppBillingService mService;
 
     private HashMap<String, String> mHashMap = new HashMap<String, String>();
 
     private String mPillow_0001Name = "pillow_0001";
     
     private IabHelper mHelper = null;
     
     // Listener that's called when we finish querying the items and subscriptions we own
     IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
         public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
             Log.d(TAG, "Query inventory finished.");
             if (result.isFailure()) {
                 complain("Failed to query inventory: " + result);
                 return;
             }
 
             Log.d(TAG, "Query inventory was successful.");
             
             SkuDetails skuDetails = inventory.getSkuDetails(mPillow_0001Name);
             Log.d(TAG, "get inventory skuDetails  " + skuDetails);
             
             String skuPrice = skuDetails.getPrice();
            mHandler.obtainMessage(UPDATE_BUY, skuPrice)
            .sendToTarget();
             Log.d(TAG, "get inventory skuDetails  price:" + skuPrice);
         }
     };
     
     IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
         public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
             Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
             if (result.isFailure()) {
                 complain("Error purchasing: " + result);
                 return;
             }
             if (!verifyDeveloperPayload(purchase)) {
                 complain("Error purchasing. Authenticity verification failed.");
                 return;
             }
 
             Log.d(TAG, "Purchase successful.");
 
             if (purchase.getSku().equals(mPillow_0001Name)) {
                 Log.d(TAG, "Purchase is mPillow_0001Name. Starting mPillow_0001Name consumption.");
                 mHelper.consumeAsync(purchase, mConsumeFinishedListener);
             }
         }
     };
     
     IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
         public void onConsumeFinished(Purchase purchase, IabResult result) {
             Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
 
             // We know this is the "gas" sku because it's the only one we consume,
             // so we don't check which sku was consumed. If you have more than one
             // sku, you probably should check...
             if (result.isSuccess()) {
                 // successfully consumed, so we apply the effects of the item in our
                 // game world's logic, which in our case means filling the gas tank a bit
                 Log.d(TAG, "Consumption successful. Provisioning.");
             } else {
                 complain("Error while consuming: " + result);
             }
             Log.d(TAG, "End consumption flow.");
         }
     };
 
     ServiceConnection mServiceConn = new ServiceConnection() {
         @Override
         public void onServiceDisconnected(ComponentName name) {
             mService = null;
         }
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             mService = IInAppBillingService.Stub.asInterface(service);
             new Thread() {
                 public void run() {
                     ArrayList<String> skuList = new ArrayList<String>();
                     skuList.add(mPillow_0001Name);
                     Bundle querySkus = new Bundle();
                     querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                     if (null != mService) {
                         try {
                             Bundle skuDetails = mService.getSkuDetails(3, getPackageName(),
                                     "inapp", querySkus);
 
                             int response = skuDetails.getInt("RESPONSE_CODE");
                             Log.d(TAG, "response:" + response);
                             if (response == 0) {
                                 ArrayList<String> responseList = skuDetails
                                         .getStringArrayList("DETAILS_LIST");
 
                                 for (String thisResponse : responseList) {
                                     JSONObject object;
                                     try {
                                         object = new JSONObject(thisResponse);
                                         String sku = object.getString("productId");
                                         String price = object.getString("price");
                                         Log.d(TAG, "sku:" + sku + ";price:" + price);
                                         if (sku.equals(mPillow_0001Name)) {
                                             mHashMap.put(mPillow_0001Name, price);
                                             mHandler.obtainMessage(UPDATE_BUY, price)
                                                     .sendToTarget();
                                         }
 
                                     } catch (JSONException e) {
                                         // TODO Auto-generated catch block
                                         e.printStackTrace();
                                     }
                                 }
                             }
 
                         } catch (RemoteException e) {
                             // TODO Auto-generated catch block
                             e.printStackTrace();
                         }
                     }
                 };
             }.start();
         }
     };
 
     private Handler mHandler = new Handler() {
         public void handleMessage(android.os.Message msg) {
             switch (msg.what) {
                 case UPDATE_BUY:
                     mBuyButton.setText("Use " + (String) msg.obj + " To buy");
                     mBuyButton.setVisibility(View.VISIBLE);
                     break;
 
                 default:
                     break;
             }
         };
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
 //        bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"),
 //                mServiceConn, Context.BIND_AUTO_CREATE);
         mHelper = new IabHelper(this, APP_SPECIFIC);
         mHelper.enableDebugLogging(true);
         mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
             public void onIabSetupFinished(IabResult result) {
                 Log.d(TAG, "Setup finished.");
 
                 if (!result.isSuccess()) {
                     complain("Problem setting up in-app billing: " + result);
                     return;
                 }
 
                 Log.d(TAG, "Setup successful. Querying inventory.");
                 
                 ArrayList<String> moreSkuList = new ArrayList<String>();
                 moreSkuList.add(mPillow_0001Name);
                 
                 mHelper.queryInventoryAsync(true, moreSkuList, mGotInventoryListener);
             }
         });
 
         api = WXAPIFactory.createWXAPI(this, APP_ID, true);
         api.registerApp(APP_ID);
 
         mBuyButton = (Button) findViewById(R.id.buy);
         mBuyButton.setOnClickListener(this);
 
         mShareButton = (Button) findViewById(R.id.share);
         mShareButton.setOnClickListener(this);
 
         mOpenButton = (Button) findViewById(R.id.open);
         mOpenButton.setOnClickListener(this);
         
         mShareAppButton = (Button) findViewById(R.id.share_app);
         mShareAppButton.setOnClickListener(this);
     }
     
 
     /*
      * (non-Javadoc)
      * @see android.view.View.OnClickListener#onClick(android.view.View)
      */
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.buy:
                 Log.d(TAG, "Click buy button");
                 buyView();
                 break;
             case R.id.share:
                 Log.d(TAG, "Click share button");
                 sendToWeiXin();
                 break;
             case R.id.open:
                 Log.d(TAG, "Click open button: opened:" + api.openWXApp());
                 break;
             case R.id.share_app:
                 shareAppToWeiXin();
                 break;
             default:
                 break;
         }
 
     }
 
     /**
      * Buy a item
      */
     private void buyView() {
         new Thread() {
             public void run() {
                 /*if (null != mService && mHashMap.containsKey(mPillow_0001Name)) {
                     try {
                         long time = System.currentTimeMillis();
                         Random r = new Random();
                         String payload = time + "p" + r.nextInt(1000) + r.nextInt(1000);
                         Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                 mPillow_0001Name, "inapp", payload);
                         PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                         try {
                             startIntentSenderForResult(pendingIntent.getIntentSender(), 1001,
                                     new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                     Integer.valueOf(0));
                             Log.d(TAG, "startIntentSenderForResult");
                         } catch (SendIntentException e) {
                             Log.e(TAG, "SendIntentException" + e);
                             e.printStackTrace();
                         }
                     } catch (RemoteException e1) {
                         Log.e(TAG, "RemoteException" + e1);
                         e1.printStackTrace();
                     }
                 }*/
                 
                 String payload = "dsjakldjaskl"; 
                 mHelper.launchPurchaseFlow(MainActivity.this, mPillow_0001Name, 1001, 
                         mPurchaseFinishedListener, payload);
             };
         }.start();
     }
 
     private void sendToWeiXin() {
         SendMessageToWX.Req req = new SendMessageToWX.Req();
         creatTextMsg(req);
         boolean result = api.sendReq(req);
         Log.d(TAG, "Is sended:" + result);
     }
 
     /**
      * Create Message type is text.
      */
     private void creatTextMsg(SendMessageToWX.Req req) {
         String text = "Text from Pillow";
         WXTextObject textObj = new WXTextObject();
         textObj.text = text;
 
         WXMediaMessage msg = new WXMediaMessage();
         msg.mediaObject = textObj;
         // msg.title = "Will be ignored";
         msg.description = text;
 
         req.transaction = buildTransaction("text");
         req.message = msg;
         // Default is WXSceneSession.
         // req.scene = SendMessageToWX.Req.WXSceneSession;
     }
     
     private void shareAppToWeiXin() {
         final String dir = SDCARD_ROOT + "/tencent/";
         File file = new File(dir);
         if (!file.exists()) {
             file.mkdirs();
         }
         CameraUtil.takePhoto(MainActivity.this, dir, "send_appdata", 0x101);
     }
 
     private String buildTransaction(final String type) {
         return (type == null) ? String.valueOf(System.currentTimeMillis()) : type
                 + System.currentTimeMillis();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if(mHelper != null){
             mHelper.dispose();
             mHelper = null;
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Log.d(TAG, "onActivityResult:" + requestCode + " " + resultCode);
         if (requestCode == 1001) {
             int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
             String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
             String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
 
             if (resultCode == RESULT_OK) {
                 try {
                     JSONObject jo = new JSONObject(purchaseData);
                     String sku = jo.getString("productId");
                     Log.d(TAG, "You have bought the " + sku + ". Excellent choice, adventurer!");
                 } catch (JSONException e) {
                     Log.e(TAG, "Failed to parse purchase data.");
                     e.printStackTrace();
                 }
             }
         }
         
         if (requestCode == 0x101) {
             final WXAppExtendObject appdata = new WXAppExtendObject();
             final String path = CameraUtil.getResultPhotoPath(this, data, SDCARD_ROOT + "/tencent/");
             appdata.filePath = path;
             appdata.extInfo = "this is ext info";
 
             final WXMediaMessage msg = new WXMediaMessage();
             msg.setThumbImage(Util.extractThumbNail(path, 150, 150, true));
             msg.title = "this is title";
             msg.description = "this is description";
             msg.mediaObject = appdata;
             
             SendMessageToWX.Req req = new SendMessageToWX.Req();
             req.transaction = buildTransaction("appdata");
             req.message = msg;
             req.scene = SendMessageToWX.Req.WXSceneSession;
             api.sendReq(req);
             finish();
         }
     }
     
     void complain(String message) {
         Log.e(TAG, "**** Pillow Error: " + message);
         alert("Error: " + message);
     }
     
     void alert(String message) {
         AlertDialog.Builder bld = new AlertDialog.Builder(this);
         bld.setMessage(message);
         bld.setNeutralButton("OK", null);
         Log.d(TAG, "Showing alert dialog: " + message);
         bld.create().show();
     }
     
     /** Verifies the developer payload of a purchase. */
     boolean verifyDeveloperPayload(Purchase p) {
         String payload = p.getDeveloperPayload();
         Log.d(TAG, "verifyDeveloperPayload:" + payload);
 
         return true;
     }
 
 }
