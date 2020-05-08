 package com.thorgaming.throwme.billing;
 
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.HashSet;
 import java.util.Random;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentSender.SendIntentException;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Base64;
 
 import com.android.vending.billing.IMarketBillingService;
 import com.thorgaming.throwme.ThrowMe;
 
 /**
  * @author Thomas Cheyney
  * @version 1.0
  */
 public class BillingService extends Service implements ServiceConnection {
 
 	/**
 	 * An instance of the billing service with the android system to call to initiate purchases/queries
 	 */
 	private static IMarketBillingService mService;
 	/**
 	 * Stores whether the device supports billing 
 	 */
 	private boolean supported = false;
 	/**
 	 * Random used to generate knownOnces
 	 */
 	private Random random = new Random();
 	/**
 	 * List of knownOnces, these verify responses are a result of requests made by this device
 	 */
 	public static HashSet<Long> knownOnce = new HashSet<Long>();
 	/**
 	 * Database handler where past purchases are stored
 	 */
 	public static PurchaseDatabase purchases;
 	/**
 	 * Id of restore request
 	 */
 	public static long restoreId = 0;
 
 	public BillingService() {
 
 	}
 
 	public BillingService(Activity activity) {
 		super();
 		attachBaseContext(ThrowMe.getInstance());
 		purchases = new PurchaseDatabase(this);
 		attachService();
 	}
 
 	/**
 	 * Attaches this service to the main activity
 	 */
 	private void attachService() {
 		try {
			bindService(new Intent("com.android.vending.billing.MarketBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 	}
	
	public void detatchService() {
		unbindService(this);
		mService = null;
	}
 
 	@Override
 	public void onServiceConnected(ComponentName arg0, IBinder service) {
 		mService = IMarketBillingService.Stub.asInterface(service);
 		if (checkBillingSupported()) {
 			supported = true;
 			SharedPreferences prefs = ThrowMe.getInstance().getPreferences(Context.MODE_PRIVATE);
 			boolean initialized = prefs.getBoolean("dbINIT", false);
 			if (!initialized) {
 				long rand = random.nextLong();
 				knownOnce.add(rand);
 
 				Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
 				request.putLong("NONCE", rand);
 				try {
 					restoreId = mService.sendBillingRequest(request).getLong("REQUEST_ID", -1);
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Constructs bundles for requests
 	 * 
 	 * @param method Billing request method
 	 * @return A bundle to be used in a request
 	 */
 	private Bundle makeRequestBundle(String method) {
 		Bundle request = new Bundle();
 		request.putString("BILLING_REQUEST", method);
 		request.putInt("API_VERSION", 1);
 		request.putString("PACKAGE_NAME", getPackageName());
 		return request;
 	}
 
 	/**
 	 * Checks if the device supports in-app billing
 	 * 
 	 * @return True if the device supports in-app billing, false otherwise
 	 */
 	private boolean checkBillingSupported() {
 		Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
 		try {
 			Bundle response = mService.sendBillingRequest(request);
 			int responseCode = response.getInt("RESPONSE_CODE");
 			boolean billingSupported = responseCode == 0;
 			return billingSupported;
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Handles change of purchase information, detailed information about transactions is passed via JSON.
 	 * 
 	 * @param signedData A signed JSON string
 	 * @param signature The signature used
 	 */
 	public void purchaseStateChanged(String signedData, String signature) {
 		boolean verified = false;
 		if (signature.length() > 0) {
 			String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4GbR3FqjQIqFkxFBWoKqCmIXAEMwdK8E13+AQuMU4i0fVw8kLMFZbk7T1YPezQnBm6ozwJSBrQA+M4HOdKguqnGE+hDtFzWCq5/mZh7VM8/9Sow7EFvlbQll2DR/8OQE1aXGcRKEf51H9a7i5VswOsqwiTAP7BqtbGo/aujo1NxtwX/OYDGIIEx/V7r1lBQCfgNEM9+dn6Ahr4ETPVU9QLhyP2F99vKBhgJ4euQj0/zpaA0jjItMhrfTRAwPXVvWnh65+ECOlpQ6WNZZF2kHBjr5ocHH+zEJDGKrs0DOQ3WDiraoaqmBXRB85vHtQQRV/8KxJHpjtWC2k0eLrfoH4wIDAQAB";
 
 			PublicKey key = generatePublicKey(base64EncodedPublicKey);
 			verified = verify(key, signedData, signature);
 			if (!verified) {
 				System.out.println("Could not verify!");
 			}
 		}
 
 		JSONObject jObject;
 		JSONArray jTransactionsArray = null;
 		int numTransactions = 0;
 		long nonce = 0L;
 		try {
 			jObject = new JSONObject(signedData);
 
 			nonce = jObject.optLong("nonce");
 			if (knownOnce.contains(nonce)) {
 				jTransactionsArray = jObject.optJSONArray("orders");
 				if (jTransactionsArray != null) {
 					numTransactions = jTransactionsArray.length();
 				}
 
 				for (int i = 0; i < numTransactions; i++) {
 					JSONObject jElement = jTransactionsArray.getJSONObject(i);
 					int response = jElement.getInt("purchaseState");
 
 					String productId = jElement.getString("productId");
 					long purchaseTime = jElement.getLong("purchaseTime");
 					String orderId = jElement.optString("orderId", "");
 					String notifyId = null;
 					if (jElement.has("notificationId")) {
 						notifyId = jElement.getString("notificationId");
 
 						Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
 						request.putStringArray("NOTIFY_IDS", new String[] {notifyId});
 						try {
 							mService.sendBillingRequest(request);
 						} catch (RemoteException e) {
 							e.printStackTrace();
 						}
 					}
 					String developerPayload = jElement.optString("developerPayload", null);
 
 					if (response == 0 && !verified) {
 						continue;
 					}
 					purchases.updatePurchase(orderId, productId, response, purchaseTime, developerPayload);
 					ThrowMe.getInstance().billingDirty = true;
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		knownOnce.remove(nonce);
 	}
 	
 	/**
 	 * Called when a transaction changes state, then sends a GET_PURCHASE_INFORMATION request to get the data about the transaction
 	 * 
 	 * @param notifyId Id to query to get information about this transaction
 	 */
 	public void notify(String notifyId) {
 		long rand = random.nextLong();
 		knownOnce.add(rand);
 
 		Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
 		request.putLong("NONCE", rand);
 
 		request.putStringArray("NOTIFY_IDS", new String[] {notifyId});
 		try {
 			mService.sendBillingRequest(request);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Triggered when a market request completes with a response code indicating if it was successful
 	 * This checks if the request to initialise the database is attempted
 	 * 
 	 * @param requestId
 	 * @param responseCodeIndex
 	 */
 	public void checkResponseCode(long requestId, int responseCodeIndex) {
 		if (requestId == restoreId && restoreId > 0) {
 			SharedPreferences prefs = ThrowMe.getInstance().getPreferences(Context.MODE_PRIVATE);
 			Editor edit = prefs.edit();
 			edit.putBoolean("dbINIT", true);
 			edit.commit();
 		}
 	}
 
 	/**
 	 * Calls the market service to start a purchase given the id of an item
 	 * 
 	 * @param marketId Id of the item to be sold
 	 */
 	public void startPurchase(String marketId) {
 		if (mService != null) {
 			if (supported) {
 				Bundle request = makeRequestBundle("REQUEST_PURCHASE");
 				request.putString("ITEM_ID", marketId);
 				try {
 					Bundle response = mService.sendBillingRequest(request);
 					PendingIntent pendingIntent = response.getParcelable("PURCHASE_INTENT");
 					Intent intent = new Intent();
 					if (pendingIntent == null) {
 						System.out.println("Error in response!");
 					} else {
 						try {
 							ThrowMe.getInstance().startIntentSender(pendingIntent.getIntentSender(), intent, 0, 0, 0);
 						} catch (SendIntentException e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				}
 			} else {
 				System.out.println("Unsupported!");
 			}
 		} else {
 			System.out.println("Service gone! :O");
 			attachService();
 		}
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName arg0) {
 		mService = null;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	/**
 	 * Decodes the base-64 key for use
 	 * 
 	 * @param encodedPublicKey The encoded public key
 	 * @return The decoded public key
 	 */
 	public static PublicKey generatePublicKey(String encodedPublicKey) {
 		try {
 			byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
 			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 			return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		} catch (InvalidKeySpecException e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 
 	/**
 	 * Logic to check signed data with public key
 	 * 
 	 * @param publicKey The decoded public key
 	 * @param signedData JSON data about transactions
 	 * @param signature Signature on data
 	 * @return True if response verified from android market
 	 */
 	public static boolean verify(PublicKey publicKey, String signedData, String signature) {
 		try {
 			Signature sig = Signature.getInstance("SHA1withRSA");
 			sig.initVerify(publicKey);
 			sig.update(signedData.getBytes());
 			if (sig.verify(Base64.decode(signature, Base64.DEFAULT))) {
 				return true;
 			}
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		} catch (SignatureException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
