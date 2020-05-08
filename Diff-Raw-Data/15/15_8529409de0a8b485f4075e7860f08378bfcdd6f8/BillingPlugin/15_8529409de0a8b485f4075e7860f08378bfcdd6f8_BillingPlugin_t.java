 package com.tealeaf.plugin.plugins;
 
 import java.util.Map;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import com.tealeaf.EventQueue;
 import com.tealeaf.TeaLeaf;
 import com.tealeaf.logger;
 import com.tealeaf.event.PluginEvent;
 import android.content.pm.PackageManager;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Iterator;
 import java.io.StringWriter;
 import java.io.PrintWriter;
 
 import com.tealeaf.plugin.IPlugin;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.ServiceConnection;
 import android.util.Log;
 
 import android.content.ComponentName;
 import android.os.IBinder;
 import android.app.PendingIntent;
 import android.os.Build;
 
 import com.tealeaf.EventQueue;
 import com.tealeaf.event.*;
 
 import com.android.vending.billing.IInAppBillingService;
 
 import com.amazon.inapp.purchasing.BasePurchasingObserver;
 import com.amazon.inapp.purchasing.PurchasingManager;
 import com.amazon.inapp.purchasing.ItemDataResponse;
 import com.amazon.inapp.purchasing.PurchaseResponse;
 import com.amazon.inapp.purchasing.Receipt;
 
 public class BillingPlugin implements IPlugin {
 	Context _ctx = null;
 	Activity _activity = null;
 	IInAppBillingService mService = null;
 	ServiceConnection mServiceConn = null;
 	public enum DeviceType {
     	KINDLE, ANDROID
 	}
 	private DeviceType deviceIs = DeviceType.ANDROID;
 	Object mServiceLock = new Object();
 	static private final int BUY_REQUEST_CODE = 123450;
 	
 	private class MyObserver extends BasePurchasingObserver {
 
 		public MyObserver(Activity _activity) {
 		    super(_activity);	 
 		}
 
 		@Override
 		public void onItemDataResponse(ItemDataResponse itemDataResponse) {
 
 		    //Check itemDataResponse.getItemDataRequestStatus();
 		    //Use itemDataResponse to populate catalog data
 
 		}
 
 		@Override
 		public void onPurchaseResponse(PurchaseResponse purchaseResponse) {
 
 		    //Check purchaseResponse.getPurchaseRequestStatus();
 		    //If SUCCESSFUL, fulfill content;
 		    logger.log("{billing} Entering Amazon Kindle Billing Plugin Handler");
 			try {
 				String responseCode = purchaseResponse.getPurchaseRequestStatus().toString();
 				if (responseCode.equals("SUCCESSFUL"))
 				{
 					Receipt receipt = purchaseResponse.getReceipt();
					String shortSKU = receipt.getSku();
					shortSKU = shortSKU.substring(shortSKU.lastIndexOf(".")+1);
					logger.log("{billing} Successfully purchased SKU: \""+ shortSKU+ " \"with token: " + receipt.getPurchaseToken());
					EventQueue.pushEvent(new PurchaseEvent(shortSKU, receipt.getPurchaseToken(), null));
 				}
 				else if(responseCode.equals("ALREADY_ENTITLED"))
 				{
 					logger.log("{billing} WARNING: Already Entitled to the Goods with response code:", responseCode);
 					EventQueue.pushEvent(new PurchaseEvent(null, null, responseCode));					
 				}
 				else if(responseCode.equals("INVALID_SKU"))
 				{
 					logger.log("{billing} WARNING: Ignored null purchase data with response code:", responseCode);
 					EventQueue.pushEvent(new PurchaseEvent(null, null, responseCode));					
 				}				
 				else
 				{
 					logger.log("{billing} WARNING: Ignored null purchase data with response code:", responseCode);
 					EventQueue.pushEvent(new PurchaseEvent(null, null, responseCode));					
 				}
 			} catch (Exception e) {
 				logger.log("{billing} WARNING: Failed to parse purchase data:", e);
 				e.printStackTrace();
 				EventQueue.pushEvent(new PurchaseEvent(null, null, "failed"));
 			}
 		}
 	} 
 
 	public class PurchaseEvent extends com.tealeaf.event.Event {
 		String sku, token, failure;
 
 		public PurchaseEvent(String sku, String token, String failure) {
 			super("billingPurchase");
 			this.sku = sku;
 			this.token = token;
 			this.failure = failure;
 		}
 	}
 
 	public class ConsumeEvent extends com.tealeaf.event.Event {
 		String token, failure;
 
 		public ConsumeEvent(String token, String failure) {
 			super("billingConsume");
 			this.token = token;
 			this.failure = failure;
 		}
 	}
 
 	public class OwnedEvent extends com.tealeaf.event.Event {
 		ArrayList<String> skus, tokens;
 		String failure;
 
 		public OwnedEvent(ArrayList<String> skus, ArrayList<String> tokens, String failure) {
 			super("billingOwned");
 			this.skus = skus;
 			this.tokens = tokens;
 			this.failure = failure;
 		}
 	}
 
 	public class ConnectedEvent extends com.tealeaf.event.Event {
 		boolean connected;
 
 		public ConnectedEvent(boolean connected) {
 			super("billingConnected");
 			this.connected = connected;
 		}
 	}
 
 	public BillingPlugin() {
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 		_ctx = applicationContext;
 
 		mServiceConn = new ServiceConnection() {
 			@Override
 				public void onServiceDisconnected(ComponentName name) {
 					synchronized (mServiceLock) {
 						mService = null;
 					}
 
 					EventQueue.pushEvent(new ConnectedEvent(false));
 				}
 
 			@Override
 				public void onServiceConnected(ComponentName name, 
 						IBinder service) {
 					synchronized (mServiceLock) {
 						mService = IInAppBillingService.Stub.asInterface(service);
 					}
 
 					EventQueue.pushEvent(new ConnectedEvent(true));
 				}
 		};
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
 		logger.log("{billing} Installing listener");
 
 		_activity = activity;
 
 		_ctx.bindService(new 
 				Intent("com.android.vending.billing.InAppBillingService.BIND"),
 				mServiceConn, Context.BIND_AUTO_CREATE);
 	}
 
 	public void onResume() {
 	}
 
 	public void onStart() {
 		final PackageManager packageManager = _ctx.getPackageManager();
 
 		try {
 		    final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(_ctx.getPackageName(), 0);
 		    if ("com.amazon.venezia".equals(packageManager.getInstallerPackageName(applicationInfo.packageName))) {
 		        // App was installed by Amazon App Store
 		        deviceIs = DeviceType.KINDLE;
 		    }
 		    else if("com.android.vending".equals(packageManager.getInstallerPackageName(applicationInfo.packageName))) {
 		    	// App was installed by Google Play Store
 		        deviceIs = DeviceType.ANDROID;
 		    }
 		    else
 		    {
 		    	// Default Market selected as Google Play Store {defaults}
 		    	deviceIs = DeviceType.ANDROID;
 		    }
		    deviceIs = DeviceType.KINDLE;
 		} catch (Exception e) {
 		    e.printStackTrace();
 		}		
 		switch(deviceIs)
 		{
 			case KINDLE:
 						logger.log("{billing} Switched to KINDLE ");
 						try {
 							PurchasingManager.registerObserver(new MyObserver(_activity));
 						} catch (Exception e) {
 							logger.log("{billing} WARNING: Failure in purchase init:", e);
 							e.printStackTrace();
 							StringWriter writer = new StringWriter();
 							PrintWriter printWriter = new PrintWriter( writer );
 							e.printStackTrace( printWriter );
 							printWriter.flush();
 							String stackTrace = writer.toString();
 							logger.log("{billing} onstart stackTrace: "+stackTrace);
 						}
 						break;
 			case ANDROID: 
 						logger.log("{billing} Switched to ANDROID");
 			            break;
 			default: 
 						logger.log("{billing} Switched to ANDROID BY DEFAULT");
 			            deviceIs = DeviceType.ANDROID;
 					 	break;
 		}
 	}
 
 	public void onPause() {
 	}
 
 	public void onStop() {
 	}
 
 	public void onDestroy() {
 		if (mServiceConn != null) {
 			_ctx.unbindService(mServiceConn);
 		}
 	}
 
 	public void isConnected(String jsonData) {
 		synchronized (mServiceLock) {
 			if (mService == null) {
 				EventQueue.pushEvent(new ConnectedEvent(false));
 			} else {
 				EventQueue.pushEvent(new ConnectedEvent(true));
 			}
 		}
 	}
 
 	public void purchaseForKindle(String jsonData) {
 		String sku = null;
		String pkgName = _ctx.getPackageName();
 		logger.log("{billing} In transaction purchase for Amazon Kindle");
 		try {
 			JSONObject jsonObject = new JSONObject(jsonData);
 			sku = jsonObject.getString("sku");
			String fullSKU = pkgName+"."+sku;
			logger.log("{billing} Doing Billing for sku: "+fullSKU);
			String requestId = PurchasingManager.initiatePurchaseRequest(fullSKU);
 		} catch (Exception e) {
 			logger.log("{billing} WARNING: Failure in purchase:", e);
 			e.printStackTrace();
 			EventQueue.pushEvent(new PurchaseEvent(sku, null, "failed"));
 		}
 	}
 	
 	public void purchase(String jsonData) {
 		if(deviceIs == DeviceType.KINDLE)
 		{
 			logger.log("{billing} Initiating purchase for Amazon Kindle");
 			purchaseForKindle(jsonData);
 			return;
 		}
 		boolean success = false;
 		String sku = null;
 
 		try {
 			JSONObject jsonObject = new JSONObject(jsonData);
 			sku = jsonObject.getString("sku");
 
 			logger.log("{billing} Purchasing:", sku);
 
 			Bundle buyIntentBundle = null;
 
 			synchronized (mServiceLock) {
 				if (mService == null) {
 					EventQueue.pushEvent(new PurchaseEvent(sku, null, "service"));
 					return;
 				}
 
 				// TODO: Add additional security with extra field ("1")
 
 				buyIntentBundle = mService.getBuyIntent(3, _ctx.getPackageName(),
 						sku, "inapp", "1");
 			}
 
 			// If unable to create bundle,
 			if (buyIntentBundle == null || buyIntentBundle.getInt("RESPONSE_CODE", 1) != 0) {
 				logger.log("{billing} WARNING: Unable to create intent bundle for sku", sku);
 			} else {
 				PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
 
 				if (pendingIntent == null) {
 					logger.log("{billing} WARNING: Unable to create pending intent for sku", sku);
 				} else {
 					_activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
 							BUY_REQUEST_CODE, new Intent(), Integer.valueOf(0),
 							Integer.valueOf(0), Integer.valueOf(0));
 					success = true;
 				}
 			}
 		} catch (Exception e) {
 			logger.log("{billing} WARNING: Failure in purchase:", e);
 			e.printStackTrace();
 		}
 
 		if (!success && sku != null) {
 			EventQueue.pushEvent(new PurchaseEvent(sku, null, "failed"));
 		}
 	}
 
 	public void consume(String jsonData) {
 		String token = null;
 
 		try {
 			JSONObject jsonObject = new JSONObject(jsonData);
 			final String TOKEN = jsonObject.getString("token");
 			token = TOKEN;
 
 			if(deviceIs == DeviceType.KINDLE)
 			{
 				logger.log("{billing} Consuming:", TOKEN);
 				logger.log("{billing} Consume suceeded:", TOKEN);
 				EventQueue.pushEvent(new ConsumeEvent(TOKEN, null));
 				return;
 			}
 
 			synchronized (mServiceLock) {
 				if (mService == null) {
 					EventQueue.pushEvent(new ConsumeEvent(TOKEN, "service"));
 					return;
 				}
 			}
 
 			logger.log("{billing} Consuming:", TOKEN);
 
 			new Thread() {
 				public void run() {
 					try {
 						logger.log("{billing} Consuming from thread:", TOKEN);
 
 						int response = 1;
 
 						synchronized (mServiceLock) {
 							if (mService == null) {
 								EventQueue.pushEvent(new ConsumeEvent(TOKEN, "service"));
 								return;
 							}
 
 							response = mService.consumePurchase(3, _ctx.getPackageName(), TOKEN);
 						}
 
 						if (response != 0) {
 							logger.log("{billing} Consume failed:", TOKEN, "for reason:", response);
 							EventQueue.pushEvent(new ConsumeEvent(TOKEN, "cancel"));
 						} else {
 							logger.log("{billing} Consume suceeded:", TOKEN);
 							EventQueue.pushEvent(new ConsumeEvent(TOKEN, null));
 						}
 					} catch (Exception e) {
 						logger.log("{billing} WARNING: Failure in consume:", e);
 						e.printStackTrace();
 						EventQueue.pushEvent(new ConsumeEvent(TOKEN, "failed"));
 					}
 				}
 			}.start();
 		} catch (Exception e) {
 			logger.log("{billing} WARNING: Failure in consume:", e);
 			e.printStackTrace();
 			EventQueue.pushEvent(new ConsumeEvent(token, "failed"));
 		}
 	}
 
 	public void getPurchases(String jsonData) {
 		ArrayList<String> skus = new ArrayList<String>();
 		ArrayList<String> tokens = new ArrayList<String>();
 		boolean success = false;
 		logger.log("{billing}=======getPurchase: "+jsonData);
 		try {
 			logger.log("{billing} Getting prior purchases");
 
 			Bundle ownedItems = null;
 
 			synchronized (mServiceLock) {
 				if (mService == null) {
 					EventQueue.pushEvent(new OwnedEvent(null, null, "service"));
 					return;
 				}
 
 				ownedItems = mService.getPurchases(3, _ctx.getPackageName(), "inapp", null);
 			}
 
 			// If unable to create bundle,
 			int responseCode = ownedItems.getInt("RESPONSE_CODE", 1);
 			if (responseCode != 0) {
 				logger.log("{billing} WARNING: Failure to create owned items bundle:", responseCode);
 				EventQueue.pushEvent(new OwnedEvent(null, null, "failed"));
 			} else {
 				ArrayList ownedSkus = 
 					ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
 				ArrayList purchaseDataList = 
 					ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
 				//ArrayList signatureList = 
 				//	ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
 				//String continuationToken = 
 				//	ownedItems.getString("INAPP_CONTINUATION_TOKEN");
 
 				for (int i = 0; i < ownedSkus.size(); ++i) {
 					//String signature = signatureList.get(i);
 					String sku = (String)ownedSkus.get(i);
 					String purchaseData = (String)purchaseDataList.get(i);
 
 					JSONObject json = new JSONObject(purchaseData);
 					String token = json.getString("purchaseToken");
 					logger.log("{billing}====== token: "+token);
 					// TODO: Provide purchase data
 					// TODO: Verify signatures
 
 					if (sku != null && token != null) {
 						skus.add(sku);
 						tokens.add(token);
 					}
 				} 
 
 				// TODO: Use continuationToken to retrieve > 700 items
 
 				EventQueue.pushEvent(new OwnedEvent(skus, tokens, null));
 			}
 		} catch (Exception e) {
 			logger.log("{billing} WARNING: Failure in getPurchases:", e);
 			e.printStackTrace();
 			EventQueue.pushEvent(new OwnedEvent(null, null, "failed"));
 		}
 	}
 
 	private String getResponseCode(Intent data) {
 		try {
 			Bundle bundle = data.getExtras();
 
 			int responseCode = bundle.getInt("RESPONSE_CODE");
 
 			switch (responseCode) {
 				case 0:
 					return "ok";
 				case 1:
 					return "cancel";
 				case 2:
 					return "service";
 				case 3:
 					return "billing unavailable";
 				case 4:
 					return "item unavailable";
 				case 5:
 					return "invalid arguments provided to API";
 				case 6:
 					return "fatal error in API";
 				case 7:
 					return "already owned";
 				case 8:
 					return "item not owned";
 			}
 		} catch (Exception e) {
 		}
 
 		return "unknown error";
 	}
 
 	public void onActivityResult(Integer request, Integer resultCode, Intent data) {
 		if (request == BUY_REQUEST_CODE) {
 			try {
 				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
 				String sku = null;
 				String responseCode = this.getResponseCode(data);
 
 				if (purchaseData == null) {
 					logger.log("{billing} WARNING: Ignored null purchase data with result code:", resultCode, "and response code:", responseCode);
 					EventQueue.pushEvent(new PurchaseEvent(null, null, responseCode));
 				} else {
 					JSONObject jo = new JSONObject(purchaseData);
 					sku = jo.getString("productId");
 					logger.log("{billing}==========onReturn: "+jo.toString());
 					if (sku == null) {
 						logger.log("{billing} WARNING: Malformed purchase json");
 					} else {
 						switch (resultCode) {
 							case Activity.RESULT_OK:
 								String token = jo.getString("purchaseToken");
 
 								logger.log("{billing} Successfully purchased SKU:", sku);
 								EventQueue.pushEvent(new PurchaseEvent(sku, token, null));
 								break;
 							case Activity.RESULT_CANCELED:
 								logger.log("{billing} Purchase canceled for SKU:", sku, "with result code:", resultCode, "and response code:", responseCode);
 								EventQueue.pushEvent(new PurchaseEvent(sku, null, responseCode));
 								break;
 							default:
 								logger.log("{billing} Unexpected result code for SKU:", sku, "with result code:", resultCode, "and response code:", responseCode);
 								EventQueue.pushEvent(new PurchaseEvent(sku, null, responseCode));
 						}
 					}
 				}
 			} catch (JSONException e) {
 				logger.log("{billing} WARNING: Failed to parse purchase data:", e);
 				e.printStackTrace();
 				EventQueue.pushEvent(new PurchaseEvent(null, null, "failed"));
 			}
 		}
 	}
 
 	public void onNewIntent(Intent intent) {
 	}
 
 	public void setInstallReferrer(String referrer) {
 	}
 
 	public void logError(String error) {
 	}
 
 	public boolean consumeOnBackPressed() {
 		return true;
 	}
 
 	public void onBackPressed() {
 	}
 }
