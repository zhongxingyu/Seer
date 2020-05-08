 package mc.inappbilling.v3;
 
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import mc.inappbilling.IInAppBilling;
 import mc.inappbilling.Inventory;
 import mc.inappbilling.OnCompleteListener;
 import mc.inappbilling.Product;
 import mc.inappbilling.Purchase;
 import mc.inappbilling.v3.InAppBillingHelper.OnConsumeFinishedListener;
 import mc.inappbilling.v3.InAppBillingHelper.OnIabPurchaseFinishedListener;
 import mc.inappbilling.v3.InAppBillingHelper.QueryInventoryFinishedListener;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class InAppBillingV3Impl implements IInAppBilling, OnIabPurchaseFinishedListener
 {
     private static final String LOGTAG = InAppBillingV3Impl.class.getSimpleName();
     private static Map<Integer, InAppBillingV3Impl> cache = new WeakHashMap<Integer, InAppBillingV3Impl>();
 
     private Context context;
     private Integer requestId;// never make it int, because it's key in cache map
     private InAppBillingHelper iabHelper;
     private OnCompleteListener<Purchase> purchaseCompleteListener;
 
     public static void createNewInAppBillingV3Impl(Context context, String appLicenseKey, OnCompleteListener<InAppBillingV3Impl> listener)
     {
         new InAppBillingV3Impl(context, appLicenseKey, listener);
     }
 
     private InAppBillingV3Impl(Context context, String appLicenseKey, final OnCompleteListener<InAppBillingV3Impl> readyToUseListener)
     {
         this.context = context;
         this.requestId = hashCode();
         cache.put(requestId, this);
 
         iabHelper = new InAppBillingHelper(context, appLicenseKey);
         boolean success = iabHelper.startSetup(new InAppBillingHelper.OnIabSetupFinishedListener()
         {
             public void onIabSetupFinished(final InAppBillingResult result)
             {
                 Log.d(LOGTAG, "Setup finished.");
 
                 if(!result.isSuccess())
                 {
                     Log.e(LOGTAG, "Problem setting up in-app billing: " + result);
                     if(readyToUseListener != null)
                     {
                        readyToUseListener.complete(null);
                     }
                 }
                 else
                 {
                     if(readyToUseListener != null)
                     {
                         readyToUseListener.complete(InAppBillingV3Impl.this);
                     }
                 }
             }
         });
         if(!success)
         {
             new Thread()
             {
                 public void run()
                 {
                     Log.e(LOGTAG, "Problem setting up in-app billing. Cannot bind market service.");
                     if(readyToUseListener != null)
                     {
                         readyToUseListener.complete(null);
                     }
                 };
             }.start();
         }
     }
 
     InAppBillingHelper getInAppBillingHelper()
     {
         return iabHelper;
     }
 
     static InAppBillingV3Impl getCachedInstance(int requestId)
     {
         return cache.get(requestId);
     }
 
     @Override
     public boolean isBillingSupported()
     {
         // TODO: implement
         // Log.e(LOGTAG, "InAppBillingHelper not initialized.");
         return true;
     }
 
     @Override
     public void getProduct(String productId, OnCompleteListener<Product> listener)
     {
         // TODO: implement
         // Log.e(LOGTAG, "InAppBillingHelper not initialized.");
     }
 
     @Override
     public void buyProduct(final String sku, final OnCompleteListener<Purchase> listener)
     {
         purchaseCompleteListener = listener;
         Intent in = new Intent(context, InAppBillingActivity.class);
         in.putExtra("sku", sku);
         in.putExtra("requestId", requestId);
         in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         context.startActivity(in);
     }
 
     @Override
     public void getPurchases(final OnCompleteListener<Inventory> listener)
     {
         Log.d(LOGTAG, "Querying inventory.");
         try
         {
             iabHelper.queryInventoryAsync(new QueryInventoryFinishedListener()
             {
                 public void onQueryInventoryFinished(InAppBillingResult result, Inventory inventory)
                 {
                     Log.d(LOGTAG, "Query inventory finished.");
                     if(result.isFailure())
                     {
                         Log.d(LOGTAG, "Failed to query inventory: " + result);
                         return;
                     }
 
                     Log.d(LOGTAG, "Query inventory was successful.");
                     if(listener != null)
                     {
                         listener.complete(inventory);
                     }
                 }
 
             });
         }
         catch(IllegalStateException e)
         {
             Log.e(LOGTAG, e.getMessage(), e);
         }
     }
 
     @Override
     public void consumePurchase(final Purchase purchase, final OnCompleteListener<Boolean> listener)
     {
         if(purchase == null)
         {
             Log.w(LOGTAG, "Purchase is null");
             return;
         }
 
         Log.d(LOGTAG, "Purchase is " + purchase.getSku() + ". Starting consumption.");
         try
         {
             iabHelper.consumeAsync(purchase, new OnConsumeFinishedListener()
             {
                 @Override
                 public void onConsumeFinished(Purchase resPurchase, InAppBillingResult result)
                 {
                     boolean res = false;
                     Log.d(LOGTAG, "Consumption finished. Purchase: " + resPurchase + ", result: " + result);
 
                     // TODO: maybe need to check if purchase.sku == resPurchase.sku
 
                     if(result.isSuccess())
                     {
                         Log.d(LOGTAG, "Consumption successful. Provisioning.");
                         res = true;
                     }
                     else
                     {
                         Log.d(LOGTAG, "Error while consuming: " + result);
                     }
                     Log.d(LOGTAG, "End consumption flow.");
 
                     if(listener != null)
                     {
                         listener.complete(res);
                     }
                 }
             });
         }
         catch(IllegalStateException e)
         {
             Log.e(LOGTAG, e.getMessage(), e);
         }
     }
 
     @Override
     public void close()
     {
         Log.d(LOGTAG, "Destroying helper.");
         if(iabHelper != null)
         {
             iabHelper.dispose();
         }
         iabHelper = null;
         cache.remove(requestId);
     }
 
     @Override
     public void onIabPurchaseFinished(InAppBillingResult result, Purchase purchase)
     {
         Log.d(LOGTAG, "Purchase finished: " + result + ", purchase: " + purchase);
         if(result.isFailure())
         {
             Log.e(LOGTAG, "Error purchasing: " + result);
         }
         else
         {
             Log.d(LOGTAG, "Purchase successful.");
        }
        if(purchaseCompleteListener != null)
        {
            purchaseCompleteListener.complete(purchase);
         }
     }
 }
