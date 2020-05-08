 package net.anei.cadpage.vendors;
 
 import net.anei.cadpage.C2DMReceiver;
 import net.anei.cadpage.HttpService;
 import net.anei.cadpage.SmsPopupUtils;
 import net.anei.cadpage.HttpService.HttpRequest;
 import net.anei.cadpage.ManagePreferences;
 import net.anei.cadpage.R;
 import net.anei.cadpage.donation.DonationManager;
 import net.anei.cadpage.donation.MainDonateEvent;
 import net.anei.cadpage.donation.UserAcctManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 
 abstract class Vendor {
 
   private String vendorCode;
   private int titleId;
   private int summaryId;
   private int textId;
   private int iconId;
   private int logoId;
   private Uri baseURI;
   private String triggerCode;
   
   private String title = null;
   
   private Uri discoverUri = null;;
   
   private SharedPreferences prefs;
   
   // True if vendor is active (ie has successfully registered with server
   private boolean enabled = false;
   
   // True if we have have identified text pages coming from this vendor
   private boolean textPage = false;
   
   // True if we are in processing of registering with vendor server
   private boolean inProgress = false;
   
   // Account and security token used to identify user account to vendor
   private String account;
   private String token;
   
   // Vendor preference that may need to be updated when status changes
   private VendorPreference preference = null;
   
   // Vendor Activity that may need to be updated when status changes
   private VendorActivity activity = null;
   
   
   Vendor(int titleId, int summaryId, int textId, int iconId, int logoId,
          String urlString, String triggerCode) {
     
     // Calculate vendor code by stripping "Vendor" off the end of the class name
     String clsName = this.getClass().getName();
     int pt = clsName.lastIndexOf('.');
     clsName = clsName.substring(pt+1);
     pt = clsName.indexOf("Vendor");
     vendorCode = clsName.substring(0,pt);
     
     this.titleId = titleId;
     this.summaryId = summaryId;
     this.textId = textId;
     this.iconId = iconId;
     this.logoId = logoId; 
     this.baseURI = Uri.parse(urlString);
     this.triggerCode = triggerCode;
   }
   
   /**
    * @return resource ID of text string name of this vendor
    */
   int getTitleId() {
     return titleId;
   }
 
   /**
    * @return resource ID of text summary description of this vendor
    */
   int getSummaryId() {
     return summaryId;
   }
   
   /**
    * @return vendor text description resource ID
    */
   int getTextId() {
     return textId;
   }
   
   /**
    * @return vendor Icon resource ID
    */
   int getIconId() {
     return iconId;
   }
   
   /**
    * @return vendor logo resource ID
    */
   int getLogoId() {
     return logoId;
   }
   
   /**
    * @return vendor code by which this vendor is recognized
    */
   String getVendorCode() {
     return vendorCode;
   }
   
   /**
    * @return base vendor URI that we use to communicate with this vendor
    */
   Uri getBaseURI() {
     return baseURI;
   }
   
   /**
    * @return trigger code at beginning of SMS text page that identifies this vendor
    */
   String getTrigerCode() {
     return triggerCode;
   }
 
   /**
    * @return true if users registered with this vendor are sponsored by the agency
    */
   boolean isSponsored() {
     return false;
   }
   
   /**
    * @return true if service is up and running and should be available for everyone,
    * false if should only be available for developers
    */
   boolean isAvailable() {
     return false;
   }
   
   /**
    * Register VendorPreference associated with this vendor
    * @param preference Vendor preference object
    */
   void registerPreference(VendorPreference preference) {
     this.preference = preference;
   }
   
   /**
    * Register Activity associated with this vendor
    * @param activity vendor activity
    */
   void registerActivity(VendorActivity activity) {
     this.activity = activity;
   }
   
   /**
    * Perform initial setup that requires a context
    * @param context current context
    */
   void setup(Context context) {
     title = context.getString(titleId);
     prefs = context.getSharedPreferences(vendorCode + "Vendor", Context.MODE_PRIVATE);
     enabled = prefs.getBoolean("enabled", false);
     account = prefs.getString("account", null);
     token = prefs.getString("token", null);
     
     textPage = prefs.getBoolean("textPage", false);
   }
   
   /**
    * @return Name of sponsoring agency if an active vendor is sponsoring Cadpage
    */
   public String getSponsor() {
     if (isSponsored() && isEnabled()) return title;
     return null;
   }
   
   /**
    * Save the critical status members to persistant storage
    */
   private void saveStatus() {
     SharedPreferences.Editor editor = prefs.edit();
     editor.putBoolean("enabled", enabled);
     editor.putString("account", account);
     editor.putString("token", token);
     editor.commit();
   }
   
   /**
    * Set the text page received flag to selected value
    * @param textPage new value
    */
   public void setTextPage(boolean textPage) {
     if (enabled) return;
     if (textPage == this.textPage) return;
     this.textPage = textPage;
     SharedPreferences.Editor editor = prefs.edit();
     editor.putBoolean("textPage", textPage);
     editor.commit();
   }
   
   /**
    * Append vendor status info to logging buffer 
    * @param sb String buffer accumulated log information
    */
   void addStatusInfo(StringBuilder sb) {
     sb.append("\n\nVendor:" + vendorCode);
     sb.append("\nenabled:" + enabled);
     sb.append("\ntextPage:" + textPage);
     if (enabled) {
       sb.append("\naccount:" + account);
       sb.append("\ntoken:" + token);
     }
   }
 
   /**
    * @return enabled status of vendor
    */
   boolean isEnabled() {
     return enabled;
   }
 
   /**
    * Process user request for more information about vendor
    * @param context current context
    */
   void moreInfoReq(Context context) {
     Uri uri = baseURI.buildUpon().appendQueryParameter("req", "info").build();
     viewPage(context, uri);
   }
 
   /**
    * Process user request to register with vendor
    * @param context Current context
    */
   void registerReq(Context context) {
     registerReq(context, null);
   }
 
   /**
    * Process vendor discovery request
    * @param context current context
    * @param uri URI included in discover request or null if user request
    */
   public void registerReq(Context context, Uri uri) {
     
     // If already enabled, we don't have to do anything
     if (enabled) return;
     
     // Set registration in progress flag
     // and save the discovery URI
     inProgress = true;
     discoverUri = uri;
     
     // See if we already have a registration ID, if we do, use it to send
     // registration request to vendor server
     String regId = ManagePreferences.registrationId();
     if (regId != null) {
       registerC2DMId(context, regId);
     }
     
     // If we don't request one and and send the request to the server when
     // it comes back in
     else {
       C2DMReceiver.register(context);
     }
   }
 
   /**
    * Process user request to unregister from this service
    * @param context current context
    */
   void unregisterReq(Context context) {
     
     if (!enabled) return;
     
     // Disable access and save that status change
     enabled = false;
     saveStatus();
     reportStatusChange();
     
     // Send an unregister request to the vendor server
     // we really don't care how it responds
     Uri uri = buildRequestUri("unregister", ManagePreferences.registrationId());
     HttpService.addHttpRequest(context, new HttpRequest(uri){});
     
     // Finally unregister from Google C2DM service.  If there are other vendor
     // services that are still active, they will request a new registration ID
     C2DMReceiver.unregister(context);
   }
   
   /**
    * Called when a new or changed C2DM registration ID is reported
    * @param context current context
    * @param registrationId registration ID
    */
   void registerC2DMId(final Context context, String registrationId) {
     
     // If we are in process of registering with server, send the web registration request
     if (inProgress) {
       
       // If we got a discovery URI from the vendor, send directly to that
       // It isn't our problem if it isn't accepted
       if (discoverUri != null) {
         Uri uri = discoverUri.buildUpon().appendQueryParameter("CadpageRegId", registrationId).build();
         HttpService.addHttpRequest(context, new HttpRequest(uri){});
       } 
       
       // Otherwise build a registration URL and display it in the web browser 
       else {
         Uri uri = buildRequestUri("register", registrationId);
         viewPage(context, uri);
       }
       inProgress = false;
       discoverUri = null;
     }
     
     // Otherwise, if we are registered with this server, pass the new registration
     // ID to them
     else if (enabled) {
       Uri uri = buildRequestUri("reregister", registrationId);
       HttpService.addHttpRequest(context, new HttpService.HttpRequest(uri){
         @Override
         public void processError(int status, String result) {
           
           // If response was successful, we don't care about any details
           if (status % 100 == 2) return;
           showNotice(context, R.string.vendor_register_err_msg, result);
           enabled = false;
           saveStatus();
           reportStatusChange();
         }});            
     }
   }
 
   /**
    * Handle vendor register/unregister request
    * @param context current context
    * @param type REGISTER/UNREGISTER request type
    * @param account vendor account
    * @param token vendor security token
    */
   void vendorRequest(Context context, String type, String account, String token) {
     
     boolean register = type.equals("REGISTER");
     
     boolean change = (this.enabled != register);
     this.enabled = register;
     this.account = account;
     this.token = token;
     saveStatus();
     
     if (change) {
       reportStatusChange();
       showNotice(context, register ? R.string.vendor_connect_msg : R.string.vendor_disconnect_msg, null);
       if (register) setTextPage(false);
       else C2DMReceiver.unregister(context);
     }
   }
 
   /**
    * Report enabled status change to all interested parties
    */
   private void reportStatusChange() {
     if (isSponsored()) {
       DonationManager.instance().reset();
       MainDonateEvent.instance().refreshStatus();
     }
     if (preference != null) preference.update();
     if (activity != null) activity.update();
   }
 
   /**
    * Build a request URI
    * @param req request type
    * @param registrationId registration ID
    * @return request URI
    */
   private Uri buildRequestUri(String req, String registrationId) {
     String phone = UserAcctManager.instance().getPhoneNumber();
     Uri.Builder builder = baseURI.buildUpon();
     builder = builder.appendQueryParameter("req", req);
     builder = builder.appendQueryParameter("vendor", getVendorCode());
     if (account != null) builder = builder.appendQueryParameter("account", account);
     if (token != null) builder = builder.appendQueryParameter("token", token);
     if (phone != null) builder = builder.appendQueryParameter("phone", phone);
     builder = builder.appendQueryParameter("type", "C2DM");
     if (registrationId != null) builder = builder.appendQueryParameter("CadpageRegId", registrationId);
     
     // Add random seed to register request to defeat any browser cache
     if (req.equals("register")) builder = builder.appendQueryParameter("seed", "" + System.currentTimeMillis() % 10000);
     return builder.build();
   }
   
   /**
    * Display direct paging notification to user
    * @param context current context
    * @param msgId message resource ID
    */
   private void showNotice(Context context, int msgId, String extra) {
     String title = context.getString(titleId);
     String message = context.getString(msgId, title, extra);
     NoticeActivity.showNotice(context, message);
   }
   
   /**
    * Display selected URI in web viewer
    * @param context current context
    * @param uri URI to be displayed
    */
   private void viewPage(Context context, Uri uri) {
    if (!SmsPopupUtils.haveNet(activity)) return;
     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     context.startActivity(intent);
   }
 }
