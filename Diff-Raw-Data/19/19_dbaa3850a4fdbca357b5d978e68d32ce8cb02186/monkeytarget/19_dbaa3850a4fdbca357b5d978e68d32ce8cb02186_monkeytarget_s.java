 class BBMonkeyGame extends BBAndroidGame{
 
 	public BBMonkeyGame( AndroidGame game,AndroidGame.GameView view ){
 		super( game,view );
 	}
 }
 
 public class MonkeyGame extends AndroidGame{
 
     public static MonkeyGame instance;
 
     public OuyaPayment ouyaPayment;
 
     private BroadcastReceiver authChangeReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (ouyaPayment != null && ouyaPayment.initialized == true) {
                 ouyaPayment.requestReceipts();
             }
         }
     };
 
     private BroadcastReceiver menuAppearingReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         }
     };
 
 	public static class GameView extends AndroidGame.GameView{
 
 		public GameView( Context context ){
 			super( context );
 		}
 		
 		public GameView( Context context,AttributeSet attrs ){
 			super( context,attrs );
 		}
 	}
 	
     @Override
     public void onStart() {
         super.onStart();
 
         IntentFilter accountsChangedFilter = new IntentFilter();
         accountsChangedFilter.addAction(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
 
         registerReceiver(menuAppearingReceiver, new IntentFilter(OuyaIntent.ACTION_MENUAPPEARING));
         registerReceiver(authChangeReceiver, accountsChangedFilter);
     }
 
     @Override
     public void onStop() {
         unregisterReceiver(menuAppearingReceiver);
         unregisterReceiver(authChangeReceiver);    
         super.onPause();
     }
 
     @Override
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         if (ouyaPayment != null) {
             ouyaPayment.onActivityResult(requestCode, resultCode, data);
         }
     }
     
    @Override
     protected void onSaveInstanceState(final Bundle outState) {
         if (ouyaPayment != null) {
             ouyaPayment.onSaveInstanceState(outState);
         }
     }
 
     @Override
     protected void onDestroy() {
         if (ouyaPayment != null) {
             ouyaPayment.onDestroy();
         }
         super.onDestroy();
     }
 
 	@Override
 	public void onCreate( Bundle savedInstanceState ){
         instance = this;
 		super.onCreate( savedInstanceState );
         OuyaController.init(this);
 
 		setContentView( R.layout.main );
 		
 		_view=(GameView)findViewById( R.id.gameView );
 		
 		_game=new BBMonkeyGame( this,_view );
 		
         if (ouyaPayment == null) {
             ouyaPayment = new OuyaPayment();
         }
 
         ouyaPayment.onCreate(savedInstanceState);
 
 		try{
 				
 			bb_.bbInit();
 			bb_.bbMain();
 			
 		}catch( RuntimeException ex ){
 
 			_game.Die( ex );
 
 			finish();
 		}
 
 		if( _game.Delegate()==null ) finish();
 		
 		_game.Run();
 	}
 };
 
 
 class OuyaPayment {
     
     private static final String PRODUCTS_INSTANCE_STATE_KEY = "Products";
 
     private static final String RECEIPTS_INSTANCE_STATE_KEY = "Receipts";
 
     private static final int PURCHASE_AUTHENTICATION_ACTIVITY_ID = 1;
 
     private static final int GAMER_UUID_AUTHENTICATION_ACTIVITY_ID = 2;
 
     public static String developerId;
 
     public static byte[] applicationKey;
 
     public static List<Purchasable> productIdentifierList;
 
     public OuyaFacade ouyaFacade;
 
     public List<Product> productList;
 
     public List<Receipt> receiptList;
 
     private final Map<String, Product> mOutstandingPurchaseRequests = new HashMap<String, Product>();
 
     public PublicKey publicKey;
 
     public int purchaseInProgress = 0;
 
     public boolean initialized = false;
 
    public void Init(String developerId, String applicationKeyPath, String[] productIds) {
         this.developerId = developerId;
         this.applicationKey = MonkeyGame.instance._game.LoadData(bb_data.g_FixDataPath(applicationKeyPath));
 
         if (this.applicationKey == null) {
             Log.i("[Monkey]", "applicationKeyPath is invalid!");
         }      
 
         productIdentifierList = new ArrayList<Purchasable>();
 
         for (String productId : productIds) {
             productIdentifierList.add(new Purchasable(productId));
         }
 
         initOuyaFacade();
        setTestMode();
         initPublicKey();
 
         initialized = true;
 
         // Request the product list if it could not be restored from the savedInstanceState Bundle
         if(productList == null) {
             requestProducts();
         }
 
         requestReceipts();
     }
 
     protected void initPublicKey() {
         // Create a PublicKey object from the key data downloaded from the developer portal.
         try {
             X509EncodedKeySpec keySpec = new X509EncodedKeySpec(applicationKey);
             KeyFactory keyFactory = KeyFactory.getInstance("RSA");
             publicKey = keyFactory.generatePublic(keySpec);
             Log.i("[Monkey]", "Public Key generated");
         } catch (Exception e) {
             Log.e("[Monkey]", "Unable to create encryption key", e);
         }
     }
 
     protected void initOuyaFacade() {
         ouyaFacade = OuyaFacade.getInstance();
         if (ouyaFacade.isInitialized() == false) {
             ouyaFacade.init(MonkeyGame.instance, developerId);
             Log.i("[Monkey]", "OuyaFacade initialized");
         }
     }
 
     public void setTestMode() {
         Log.i("[Monkey]", "Ouya IAP Testmode enabled!");
         ouyaFacade.setTestMode();
     }
 
     public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
         if(resultCode == MonkeyGame.instance.RESULT_OK && initialized == true) {
             switch (requestCode) {
                 case GAMER_UUID_AUTHENTICATION_ACTIVITY_ID:
                     fetchGamerUUID();
                     break;
                 case PURCHASE_AUTHENTICATION_ACTIVITY_ID:
                     restartInterruptedPurchase();
                     break;
             }
         }
     }
 
     public void restartInterruptedPurchase() {
         final String suspendedPurchaseId = OuyaPurchaseHelper.getSuspendedPurchase(MonkeyGame.instance);
         if(suspendedPurchaseId == null) {
             return;
         }
 
         try {
             for(Product thisProduct : productList) {
                 if(suspendedPurchaseId.equals(thisProduct.getIdentifier())) {
                     requestPurchase(thisProduct);
                     break;
                 }
             }
         } catch (Exception ex) {
             showError(ex.getMessage());
         }
     }
 
     public void onSaveInstanceState(final Bundle outState) {
         if(productList != null) {
             outState.putParcelableArray(PRODUCTS_INSTANCE_STATE_KEY, productList.toArray(new Product[productList.size()]));
             Log.i("[Monkey]", "Product List stored");
         }
         if(receiptList != null) {
             outState.putParcelableArray(RECEIPTS_INSTANCE_STATE_KEY, receiptList.toArray(new Receipt[receiptList.size()]));
             Log.i("[Monkey]", "Receipt List stored");
         }
     }
 
     public void onDestroy() {
         ouyaFacade.shutdown();
     }
 
     public void requestProducts() {
         if (initialized == false) { return; }
         ++purchaseInProgress;
 
         Log.i("[Monkey]", "Request products...");
 
         ouyaFacade.requestProductList(productIdentifierList, new OuyaResponseListener<ArrayList<Product>>() {
 
             @Override
             public void onCancel() {
                 --purchaseInProgress;
             }
 
             @Override
             public void onSuccess(final ArrayList<Product> products) {
                 --purchaseInProgress;
                 productList = products;
                 for (Product p : productList) {
                     Log.i("[Monkey]", "Product found: " + p.getName());
                 }
             }
 
             @Override
             public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                 // Your app probably wants to do something more sophisticated than popping a Toast. This is
                 // here to tell you that your app needs to handle this case: if your app doesn't display
                 // something, the user won't know of the failure.
                 --purchaseInProgress;
                 Toast.makeText(MonkeyGame.instance, "Could not fetch product information (error " + errorCode + ": " + errorMessage + ")", Toast.LENGTH_LONG).show();
             }
         });
     }
 
     private void fetchGamerUUID() {
         if (initialized == false) { return; }
 
         Log.i("[Monkey]", "Fetch Gamer UUID");
 
         ++purchaseInProgress;
         ouyaFacade.requestGamerUuid(new OuyaResponseListener<String>() {
 
             @Override
             public void onCancel() {
                 --purchaseInProgress;
             }
 
             @Override
             public void onSuccess(String result) {
                 --purchaseInProgress;
                 new AlertDialog.Builder(MonkeyGame.instance)
                         .setTitle("Info")
                         .setMessage(result)
                         .setPositiveButton("Ok", null)
                         .show();
             }
 
             @Override
             public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                 --purchaseInProgress;
                 boolean wasHandledByAuthHelper =
                         OuyaAuthenticationHelper.
                                 handleError(
                                         MonkeyGame.instance, errorCode, errorMessage,
                                         optionalData, GAMER_UUID_AUTHENTICATION_ACTIVITY_ID,
                                         new OuyaResponseListener<Void>() {
                                             @Override
                                             public void onSuccess(Void result) {
                                                 fetchGamerUUID();   // Retry the fetch if the error was handled.
                                             }
 
                                             @Override
                                             public void onFailure(int errorCode, String errorMessage,
                                                                   Bundle optionalData) {
                                                 showError("Unable to fetch gamer UUID (error " +
                                                          errorCode + ": " + errorMessage + ")");
                                             }
 
                                             @Override
                                             public void onCancel() {
                                                 showError("Unable to fetch gamer UUID");
                                             }
                                         });
 
                 if (!wasHandledByAuthHelper) {
                     showError("Unable to fetch gamer UUID (error " + errorCode + ": " + errorMessage + ")");
                 }
             }
         });
     }
 
     public void requestReceipts() {
         if (initialized == false) { return; }
         Log.i("[Monkey]", "Request Receipts...");
         ++purchaseInProgress;
         ouyaFacade.requestReceipts(new ReceiptListener());
     }    
 
     public void requestPurchase(final Product product)
         throws GeneralSecurityException, UnsupportedEncodingException, JSONException {
         if (initialized == false) { return; }
 
         Log.i("[Monkey]", "Request purchase...");
         
         SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
 
         // This is an ID that allows you to associate a successful purchase with
         // it's original request. The server does nothing with this string except
         // pass it back to you, so it only needs to be unique within this instance
         // of your app to allow you to pair responses with requests.
         String uniqueId = Long.toHexString(sr.nextLong());
 
         JSONObject purchaseRequest = new JSONObject();
         purchaseRequest.put("uuid", uniqueId);
         purchaseRequest.put("identifier", product.getIdentifier());
         purchaseRequest.put("testing", "true"); // This value is only needed for testing, not setting it results in a live purchase
         String purchaseRequestJson = purchaseRequest.toString();
 
         byte[] keyBytes = new byte[16];
         sr.nextBytes(keyBytes);
         SecretKey key = new SecretKeySpec(keyBytes, "AES");
 
         byte[] ivBytes = new byte[16];
         sr.nextBytes(ivBytes);
         IvParameterSpec iv = new IvParameterSpec(ivBytes);
 
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
         cipher.init(Cipher.ENCRYPT_MODE, key, iv);
         byte[] payload = cipher.doFinal(purchaseRequestJson.getBytes("UTF-8"));
 
         cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
         cipher.init(Cipher.ENCRYPT_MODE, publicKey);
         byte[] encryptedKey = cipher.doFinal(keyBytes);
 
         Purchasable purchasable =
                 new Purchasable(
                         product.getIdentifier(),
                         Base64.encodeToString(encryptedKey, Base64.NO_WRAP),
                         Base64.encodeToString(ivBytes, Base64.NO_WRAP),
                         Base64.encodeToString(payload, Base64.NO_WRAP) );
 
         synchronized (mOutstandingPurchaseRequests) {
             mOutstandingPurchaseRequests.put(uniqueId, product);
         }
         ++purchaseInProgress;
         ouyaFacade.requestPurchase(purchasable, new PurchaseListener(product));
     }
 
     private void showError(final String errorMessage) {
         Toast.makeText(MonkeyGame.instance, errorMessage, Toast.LENGTH_LONG).show();
     }
 
     private class ReceiptListener implements OuyaResponseListener<String> {
 
         @Override
         public void onCancel() {
             --purchaseInProgress;
         }
 
         /**
          * Handle the successful fetching of the data for the receipts from the server.
          *
          * @param receiptResponse The response from the server.
          */
         @Override
         public void onSuccess(String receiptResponse) {
             --purchaseInProgress;
             OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
             List<Receipt> receipts;
             try {
                 JSONObject response = new JSONObject(receiptResponse);
                 if(response.has("key") && response.has("iv")) {
                     receipts = helper.decryptReceiptResponse(response, publicKey);
                 } else {
                     receipts = helper.parseJSONReceiptResponse(receiptResponse);
                 }
             } catch (ParseException e) {
                 throw new RuntimeException(e);
             } catch (JSONException e) {
                 if(e.getMessage().contains("ENCRYPTED")) {
                     // This is a hack for some testing code which will be removed
                     // before the consumer release
                     try {
                         receipts = helper.parseJSONReceiptResponse(receiptResponse);
                     } catch (IOException ioe) {
                         throw new RuntimeException(ioe);
                     }
                 } else {
                     throw new RuntimeException(e);
                 }
             } catch (GeneralSecurityException e) {
                 throw new RuntimeException(e);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
             Collections.sort(receipts, new Comparator<Receipt>() {
                 @Override
                 public int compare(Receipt lhs, Receipt rhs) {
                     return rhs.getPurchaseDate().compareTo(lhs.getPurchaseDate());
                 }
             });
 
             receiptList = receipts;
             Log.i("[Monkey]", "Receipts received");
         }
 
         /**
          * Handle a failure. Because displaying the receipts is not critical to the application we just show an error
          * message rather than asking the user to authenticate themselves just to start the application up.
          *
          * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the
          *                  Ouya server, documented in the {@link OuyaErrorCodes} class.
          *
          * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
          *
          * @param optionalData A Map of optional key/value pairs which provide additional information.
          */
 
         @Override
         public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
             --purchaseInProgress;
             showError("Could not fetch receipts (error " + errorCode + ": " + errorMessage + ")");
         }
     }
 
     /**
      * The callback for when the user attempts to purchase something
      */
     private class PurchaseListener implements OuyaResponseListener<String> {
         /**
          * The ID of the product the user is trying to purchase. This is used in the
          * onFailure method to start a re-purchase if they user wishes to do so.
          */
 
         private Product mProduct;
 
         /**
          * Constructor. Store the ID of the product being purchased.
          */
 
         PurchaseListener(final Product product) {
             mProduct = product;
         }
 
         @Override
         public void onCancel() {
             --purchaseInProgress;
         }
         /**
          * Handle a successful purchase.
          *
          * @param result The response from the server.
          */
         @Override
         public void onSuccess(String result) {
             --purchaseInProgress;
             Product product;
             String id;
             try {
                 OuyaEncryptionHelper helper = new OuyaEncryptionHelper();
 
                 JSONObject response = new JSONObject(result);
                 if(response.has("key") && response.has("iv")) {
                     id = helper.decryptPurchaseResponse(response, publicKey);
                     Product storedProduct;
                     synchronized (mOutstandingPurchaseRequests) {
                         storedProduct = mOutstandingPurchaseRequests.remove(id);
                     }
                     if(storedProduct == null || !storedProduct.getIdentifier().equals(mProduct.getIdentifier())) {
                         onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                         return;
                     }
                 } else {
                     product = new Product(new JSONObject(result));
                     if(!mProduct.getIdentifier().equals(product.getIdentifier())) {
                         onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                         return;
                     }
                 }
             } catch (ParseException e) {
                 onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
             } catch (JSONException e) {
                 if(e.getMessage().contains("ENCRYPTED")) {
                     // This is a hack for some testing code which will be removed
                     // before the consumer release
                     try {
                         product = new Product(new JSONObject(result));
                         if(!mProduct.getIdentifier().equals(product.getIdentifier())) {
                             onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, "Purchased product is not the same as purchase request product", Bundle.EMPTY);
                             return;
                         }
                     } catch (JSONException jse) {
                         onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                         return;
                     }
                 } else {
                     onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                     return;
                 }
             } catch (IOException e) {
                 onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                 return;
             } catch (GeneralSecurityException e) {
                 onFailure(OuyaErrorCodes.THROW_DURING_ON_SUCCESS, e.getMessage(), Bundle.EMPTY);
                 return;
             }
 
             new AlertDialog.Builder(MonkeyGame.instance)
                     .setTitle("Info")
                     .setMessage("You have successfully purchased a " + mProduct.getName() + " for " + Strings.formatDollarAmount(mProduct.getPriceInCents()))
                     .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             dialogInterface.dismiss();
                         }
                     })
                     .show();
             requestReceipts();
         }
 
         /**
          * Handle an error. If the OUYA framework supplies an intent this means that the user needs to
          * either authenticate or re-authenticate themselves, so we start the supplied intent.
          *
          * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the
          *                  Ouya server, documented in the {@link OuyaErrorCodes} class.
          *
          * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
          *
          * @param optionalData A Map of optional key/value pairs which provide additional information.
          */
 
         @Override
         public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
             --purchaseInProgress;
             OuyaPurchaseHelper.suspendPurchase(MonkeyGame.instance, mProduct.getIdentifier());
 
             boolean wasHandledByAuthHelper =
                     OuyaAuthenticationHelper.
                             handleError(
                                     MonkeyGame.instance, errorCode, errorMessage,
                                     optionalData, PURCHASE_AUTHENTICATION_ACTIVITY_ID,
                                     new OuyaResponseListener<Void>() {
                                         @Override
                                         public void onSuccess(Void result) {
                                             restartInterruptedPurchase();   // Retry the purchase if the error was handled.
                                         }
 
                                         @Override
                                         public void onFailure(int errorCode, String errorMessage,
                                                               Bundle optionalData) {
                                             showError("Unable to make purchase (error " +
                                                     errorCode + ": " + errorMessage + ")");
                                         }
 
                                         @Override
                                         public void onCancel() {
                                             showError("Unable to make purchase");
                                         }
                                     });
 
 
             if(!wasHandledByAuthHelper) {
                 // Show the user the error and offer them the ability to re-purchase if they
                 // decide the error is not permanent.
                 new AlertDialog.Builder(MonkeyGame.instance)
                         .setTitle("Error")
                         .setMessage("Unfortunately, your purchase failed [error code " + errorCode + " (" + errorMessage + ")]. Would you like to try again?")
                         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialogInterface, int i) {
                                 dialogInterface.dismiss();
                                 try {
                                     requestPurchase(mProduct);
                                 } catch (Exception ex) {
                                     showError(ex.getMessage());
                                 }
                             }
                         })
                         .setNegativeButton("Cancel", null)
                         .show();
             }
         }
     }
 
     public void onCreate( Bundle savedInstanceState ){
             // Attempt to restore the product and receipt list from the savedInstanceState Bundle
         if(savedInstanceState != null) {
             if(savedInstanceState.containsKey(PRODUCTS_INSTANCE_STATE_KEY)) {
                 Parcelable[] products = savedInstanceState.getParcelableArray(PRODUCTS_INSTANCE_STATE_KEY);
                 productList = new ArrayList<Product>(products.length);
                 for(Parcelable product : products) {
                     productList.add((Product) product);
                 }
                 Log.i("[Monkey]", "Product List restored");
             }
             if(savedInstanceState.containsKey(RECEIPTS_INSTANCE_STATE_KEY))  {
                 Parcelable[] receipts = savedInstanceState.getParcelableArray(RECEIPTS_INSTANCE_STATE_KEY);
                 receiptList = new ArrayList<Receipt>(receipts.length);
                 for(Parcelable receipt : receipts) {
                     receiptList.add((Receipt) receipt);
                 }
                 Log.i("[Monkey]", "Receipt List restored");
             }
         }
     }
 }
