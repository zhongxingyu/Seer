 package net.anei.cadpage.vendors;
 
 import net.anei.cadpage.C2DMReceiver;
 import net.anei.cadpage.CadPageApplication;
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
   private String emailAddress;
   
   private String title = null;
   
   private Uri discoverUri = null;;
   
   private SharedPreferences prefs;
   
   // True if vendor is active (ie has successfully registered with server
   private boolean enabled = false;
   
   // True if vendor was active, but was inactivated because the vendor failed
   // to respond to a reregister request
   private boolean broken = false;
   
   // True if we have have identified text pages coming from this vendor
   private boolean textPage = false;
   
   // When user asks to disable text page checking, it only works until they
   // install the next release.  The disabled release ID is stored here
   private boolean disableTextPageCheck = false;
   
   // True if we are in processing of registering with vendor server
   private boolean inProgress = false;
   
   // Account and security token used to identify user account to vendor
   private String account;
   private String token;
   
   // Vendor preference that may need to be updated when status changes
   private VendorPreference preference = null;
   
   // Vendor Activity that may need to be updated when status changes
   private VendorActivity activity = null;
   
   // Client version code
   private String clientVersion = null;
   
   
   Vendor(int titleId, int summaryId, int textId, int iconId, int logoId,
          String urlString, String triggerCode, String emailAddress) {
     
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
     this.emailAddress = emailAddress;
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
    * @param request type of request this will be used for
    * @return base vendor URI that we use to communicate with this vendor
    */
   Uri getBaseURI(String req) {
     return getBaseURI();
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
    * Determine if the sender address of a messages indicates that it originated with this vendor
    * @param address  message sender addres
    * @return true if this address is associated with this vendor
    */
   boolean isVendorAddress(String address) {
     
     // Always return false.  Vendor classes usually override this to add their own logic
     return false;
   }
   
   /**
    * @return support email address
    */
   String getEmailAddress() {
     return emailAddress;
   }
 
   /**
    * get custom response menu for a given index
    * @param index requested index
    * @return custom response menu if defined, null otherwise
    */
   String getResponseMenu(int index) {
     return null;
   }
 
   /**
    * @return true if users registered with this vendor are sponsored by the agency
    */
   boolean isSponsored() {
     return false;
   }
   
   /**
    * @return GCM request status for this vendor
    *   0 - GCM not supported or must be manually enabled by user
    *   1 - GCM mode forced for new registrations
    *   2 - GCM mode forced for all user - C2DM mode is not supported
    */
   int getGCMStatus() {
     return 0;
   }
   
   /**
    * @return true if service is up and running and should be available for everyone,
    * false if should only be available for developers
    */
   boolean isAvailable() {
     return false;
   }
   
   /**
    * @return interface version id - generally used to determine which response menus are available
    */
   int getVersion() {
     return 0;
   }
   
   /**
    * @return client version code
    */
   String getClientVersion() {
     if (clientVersion == null) {
       clientVersion = "" + getVersion() + "-" + CadPageApplication.getVersionCode();
     }
     return clientVersion;
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
     broken = prefs.getBoolean("broken", false);
     account = prefs.getString("account", null);
     token = prefs.getString("token", null);
     
     textPage = prefs.getBoolean("textPage", false);
     disableTextPageCheck = prefs.getBoolean("disableTextPageCheck", false);
   }
   
   /**
    * @return Name of sponsoring agency if an active vendor is sponsoring Cadpage
    */
   String getSponsor() {
     if (isSponsored() && isEnabled()) return title;
     return null;
   }
   
   /**
    * Save the critical status members to persistant storage
    */
   private void saveStatus() {
     SharedPreferences.Editor editor = prefs.edit();
     editor.putBoolean("enabled", enabled);
     editor.putBoolean("broken", broken);
     editor.putString("account", account);
     editor.putString("token", token);
     editor.commit();
   }
   
   /**
    * Set the text page received flag to selected value
    * @param textPage new value
    */
   void setTextPage(boolean textPage) {
     if (enabled) return;
     if (textPage == this.textPage) return;
     this.textPage = textPage;
     SharedPreferences.Editor editor = prefs.edit();
     editor.putBoolean("textPage", textPage);
     editor.commit();
   }
   
   /**
    * Called when users asks us not to bug them about text paging status
    */
   void setDisableTextPageCheck(boolean disableTextPageCheck) {
     if (disableTextPageCheck == this.disableTextPageCheck) return;
     if (disableTextPageCheck && !textPage) return;
     this.disableTextPageCheck = disableTextPageCheck;
     SharedPreferences.Editor editor = prefs.edit();
     if (textPage) {
       textPage = false;
       editor.putBoolean("textPage", textPage);
     }
     editor.putBoolean("disableTextPageCheck", disableTextPageCheck);
     editor.commit();
   }
   
   /**
    * Append vendor status info to logging buffer 
    * @param sb String buffer accumulated log information
    */
   void addStatusInfo(StringBuilder sb) {
     sb.append("\n\nVendor:" + vendorCode);
     sb.append("\nenabled:" + enabled);
     sb.append("\nbroken:" + broken);
     sb.append("\ntextPage:" + textPage);
     sb.append("\ndisableTextPageCheck:" + disableTextPageCheck);
     if (enabled || broken) {
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
    * @return broken status of vendor
    */
   boolean isBroken() {
     return broken;
   }
   
   /**
    * @param status context of request
    *         0 - return raw status
    *         1 - should we show startup register screen
    *         2 - should we show register screen in donation menus
    * @return text page status of vendor
    */
   boolean isTextPage(int status) {
     
     // If we haven't see a text page then the answer is always no
     if (!textPage) return false;
     
    // If vendor is enabled, answer is always no
    if (enabled) return false;
    
     // If startup request, return true unless user has asked us not to
     if (status == 1) return !disableTextPageCheck;
     
     // If donation menu, return true if vendor is sponsored
     if (status == 2) return isSponsored();
     
     // Otherewise, always return true
     return true;
   }
 
   /**
    * Process user request for more information about vendor
    * @param context current context
    */
   void moreInfoReq(Context context) {
     if (!SmsPopupUtils.haveNet(context)) return;
     Uri uri = baseURI.buildUpon().appendQueryParameter("req", "info").build();
     viewPage(context, uri);
   }
 
   /**
    * Process user request vendor user profile
    * @param context current context
    */
   void profileReq(Context context) {
     if (!SmsPopupUtils.haveNet(context)) return;
     Uri uri = buildRequestUri("profile", ManagePreferences.registrationId());
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
   void registerReq(Context context, Uri uri) {
     
     // If already enabled, we don't have to do anything
     if (enabled) {
       sendReregister(context, ManagePreferences.registrationId());
       return;
     }
 
     // Make sure we have network connectivity
     if (!SmsPopupUtils.haveNet(context)) return;
 
     // Set registration in progress flag
     // and save the discovery URI
     inProgress = true;
     discoverUri = uri;
     
     //  If GCM status requests it, switch us to GCM mode
     if (getGCMStatus() >= 1) C2DMReceiver.switchGCMMode(context, true);
     
     // See if we already have a registration ID, if we do, use it to send
     // registration request to vendor server
     String regId = ManagePreferences.registrationId();
     if (regId != null) {
       registerC2DMId(context, regId);
     }
     
     // If we don't request one and and send the request to the server when
     // it comes back in
     else {
       if (!C2DMReceiver.register(context)) showNotice(context, R.string.vendor_reg_failure_msg, null);
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
         Uri uri = discoverUri.buildUpon()
             .appendQueryParameter("type", ManagePreferences.gcmEnabled() ? "GCM" : "C2DM")
             .appendQueryParameter("CadpageRegId", registrationId)
             .build();
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
     else if (enabled || broken) {
       sendReregister(context, registrationId);            
     }
   }
   
   /**
    * Send reregister request to vendor
    * @param context current context
    * @param registrationId registration ID
    */
   private void sendReregister(final Context context, String registrationId) {
     Uri uri = buildRequestUri("reregister", registrationId);
     HttpService.addHttpRequest(context, new HttpService.HttpRequest(uri){
       
       @Override
       protected void processContent(String content) {
         processError(200, null);
       }
 
       @Override
       public void processError(int status, String result) {
         
         // Don't know what negative result means, but lets ignore it
         if (status < 0) return;
         
         // A successful return indicates link is working, anything else
         // indicates that it is broken
         boolean newStat = ((status / 100) == 2);
         
         // A 299 response indicates that the server has been having trouble with our registration ID
         // and we should request another one.
         if (status == 299) C2DMReceiver.register(context);
         
         // If nothing has changed, all is well
         if (enabled == newStat) return;
         
         // If we are using the new GCM protocol, it has it's own way of getting the new registration ID
         // to our servers.  So we don't have to do anything drastic if the reregister request fails
         if (enabled && ManagePreferences.gcmEnabled()) return;
         
         // If response was successful, we don't care about any details
         enabled = newStat;
         if (! enabled) broken = (status != 400);
         showNotice(context, enabled ? R.string.vendor_connect_msg : R.string.vendor_disconnect_msg, null);
         saveStatus();
         reportStatusChange();
       }});
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
     this.broken = false;
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
     Uri.Builder builder = getBaseURI(req).buildUpon();
     builder = builder.appendQueryParameter("req", req);
     builder = builder.appendQueryParameter("vendor", getVendorCode());
     if (account != null) builder = builder.appendQueryParameter("account", account);
     if (token != null) builder = builder.appendQueryParameter("token", token);
     if (phone != null) builder = builder.appendQueryParameter("phone", phone);
     builder = builder.appendQueryParameter("type", ManagePreferences.gcmEnabled() ? "GCM" : "C2DM");
     if (registrationId != null) builder = builder.appendQueryParameter("CadpageRegId", registrationId);
     builder.appendQueryParameter("version", getClientVersion());
     
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
     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     context.startActivity(intent);
   }
 }
