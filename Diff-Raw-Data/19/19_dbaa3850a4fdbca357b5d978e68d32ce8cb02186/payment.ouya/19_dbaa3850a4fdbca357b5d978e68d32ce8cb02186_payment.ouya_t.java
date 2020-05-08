 import java.security.*;
 import tv.ouya.console.api.*;
 import tv.ouya.console.internal.util.Strings;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.security.spec.X509EncodedKeySpec;
 import android.accounts.AccountManager;
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 class PaymentWrapper
 {
     public OuyaPayment ouyaPayment;
 
    public void Init(String developerId, String applicationKeyPath, String[] productIds, boolean testMode)
     {
         ouyaPayment = MonkeyGame.instance.ouyaPayment;
        ouyaPayment.Init(developerId, applicationKeyPath, productIds, testMode);
     }
 
     public void Purchase(String productId) {
         try {       
             if (ouyaPayment.productList != null) {       
                 for (Product p : ouyaPayment.productList) {
                     if (p.getIdentifier().equals(productId)) {
                         ouyaPayment.requestPurchase(p);
                     }        
                 }
             }
         } catch (GeneralSecurityException e) {
         } catch (UnsupportedEncodingException e) {
         } catch (JSONException e) {
         }
     }
 
     public boolean IsBought(String productId) {      
         if (ouyaPayment.receiptList != null) {  
             for (Receipt p : ouyaPayment.receiptList) {
                 if (p.getIdentifier().equals(productId)) {
                     return true;
                 }        
             }
         }
         return false;
     }
 
     public boolean IsPurchaseInProgress() {
         return (ouyaPayment.purchaseInProgress > 0);
     }
 
     public int ProductQuantity(String productId) {
         return 0;
     }
 
     public boolean ConsumeProduct(String productId) {
         return false;
     }
 
     public void SetTestMode() {
         ouyaPayment.setTestMode();
     }
 
     public void RestorePurchases()
     {
         ouyaPayment.requestReceipts();
     }
 }
