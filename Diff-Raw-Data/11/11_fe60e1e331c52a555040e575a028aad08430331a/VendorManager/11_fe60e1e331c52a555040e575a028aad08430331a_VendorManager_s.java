 package net.anei.cadpage.vendors;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.anei.cadpage.C2DMReceiver;
 import net.anei.cadpage.R;
 import net.anei.cadpage.SmsPopupUtils;
 import net.anei.cadpage.donation.DeveloperToolsManager;
 import android.content.Context;
 import android.net.Uri;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceScreen;
 
 /**
  * This class manages all of the Vendor classes
  */
 public class VendorManager {
   
   // List of supported vendors
   private Vendor[] vendorList = new Vendor[]{
     new CodeMessagingVendor(),
     new Active911Vendor()
   };
   
   /**
    * Initialize vendor status at startup
    * @param context current context
    */
   public void setup(Context context) {
     for (Vendor vendor : vendorList) {
       vendor.setup(context);
     }
   }
   
   /**
    * Set up Vendor configuration preference screen
    * @param context current context
    * @param pref preference to be set up with vendor config menu
    */
   public void setupPreference(final Context context, PreferenceScreen pref) {
     
     Preference reconnectPref = pref.findPreference(context.getString(R.string.pref_reconnect_key));
     reconnectPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
 
       @Override
       public boolean onPreferenceClick(Preference preference) {
         if (SmsPopupUtils.haveNet(context)) reconnect(context);
         return true;
       }});
     
     boolean developer = DeveloperToolsManager.instance().isDeveloper(context);
     int order = 10;
     for (Vendor vendor : vendorList) {
       if (developer || vendor.isAvailable()) {
         pref.addPreference(new VendorPreference(context, vendor, order++));
       }
     }
   }
   
   /**
    * @return Name of sponsoring agency if an active vendor is sponsoring Cadpage
    */
   public String getSponsor() {
     for (Vendor vendor : vendorList) {
       String sponsor = vendor.getSponsor();
       if (sponsor != null) return sponsor;
     }
     return null;
   }
   
   /**
    * Return main icon ID associated with vendor code 
    * @param vendorCode vendor code
    * @return icon resource ID
    */
   public int getVendorIconId(String vendorCode) {
     if (vendorCode == null) return 0;
     Vendor vendor = findVendor(vendorCode);
     if (vendor == null) return 0;
     return vendor.getIconId();
   }
   
   /**
    * Get support email address associated with vendor code
    * @param vendorCode vendor code
    * @return vendor support email address
    */
   public String getEmailAddress(String vendorCode) {
     if (vendorCode == null) return null;
     Vendor vendor = findVendor(vendorCode);
     if (vendor == null) return null;
     return vendor.getEmailAddress();
   }
   
   /**
    * Reconnect all enabled vendors
    * @param context current context
    */
   void reconnect(Context context) {
     for (Vendor vendor : vendorList) vendor.reconnect(context);
   }
   
   /**
    * Called by CD2MReceiver when a new registration ID is defined
    * @param context current context
    * @param registrationId new registration ID
    */
   public void registerC2DMId(Context context, String registrationId) {
     for (Vendor vendor : vendorList) {
       vendor.registerC2DMId(context, registrationId);
     }
   }
   
   /**
    * Called by CD2MReceiver when phone is unregistered from C2DM services
    * @param context current context
    * @param registrationId old registration ID
    */
   public void unregisterC2DMId(Context context, String registrationId) {
     
     // Loop through the vendor list to see if any of them are still enabled
     boolean reregister = false;
     for (Vendor vendor : vendorList) {
       if (vendor.isEnabled()) {
         reregister = true;
         break;
       }
     }
     
     // If any are, we need to request a new registration ID
     if (reregister) C2DMReceiver.register(context);
   }
   
   /**
    * Call by C2DMReceiver when a registration failure is reported
    * @param context current context
    * @param error error message
    */
   public void failureC2DMId(Context context, String error) {
     String errMsg = context.getString(R.string.vendor_registration_error, error);
     NoticeActivity.showNotice(context, errMsg);
     
   }
   
   /**
    * Handle vendor request received as a C2DM message
    * @param context current context
    * @param type request type
    * @param vendorCode vendor code
    * @param account vendor account
    * @param token vendor security token
    */
   public void vendorRequest(Context context, String type, String vendorCode,
                               String account, String token) {
 
     // Identify the correct vendor and pass request to them
     Vendor vendor = findVendor(vendorCode);
     if (vendor == null) return;
     vendor.vendorRequest(context, type, account, token);
   }
   
   /**
    * Check for a C2DM discovery text message
    * @param context current context
    * @param message received text message
    * @return possibly modified text message to be processed, of null if
    * this was a discovery query that should not be visible to anything else
    */
   public String discoverQuery(Context context, String message) {
     
     // There are two kinds of discover messages.  A standalone query
     // always begins with a fixed string and contains an http URL
     if (message.startsWith("*CADPAGEQ*")) {
       Matcher match = HTTP_PATTERN.matcher(message);
       if (match.find()) {
         String urlStr = match.group();
         
         // Confirm that it is a valid URL
         try {
           new URL(urlStr);
         } catch (MalformedURLException ex) {
           return message;
         }
         
         // Find vendor associated with URL
         Vendor vendor = findVendorFromUrl(urlStr);
         if (vendor != null) {
           vendor.registerReq(context, Uri.parse(urlStr));
           return null;
         }
       }
     }
     
     // An inline query is simply a 4+ character prefix unique to each vendor
     // If we find it, all we do is flag the vendor as receiving text pages
     // and strip off the trigger prefix
     else {
       for (Vendor vendor : vendorList) {
         String tag = vendor.getTrigerCode();
         if (tag != null && tag.length()>=4 && message.startsWith(tag)) {
           vendor.setTextPage(true);
           message = message.substring(tag.length());
           break;
         }
       }
     }
     
     return message;
   }
   private static final Pattern HTTP_PATTERN = Pattern.compile("\\bhttps?://[^ ]+");
   
   /**
    * Return vendor code associated with URL string
    * @param urlString URL string
    * @return vendor code if found or null if not
    */
   public String findVendorCodeFromUrl(String urlString) {
     Vendor vendor = findVendorFromUrl(urlString);
     if (vendor == null) return null;
     return vendor.getVendorCode();
   }
 
   /**
    * Find the vendor associated with a URL string
    * @param urlString URL string
    * @return associated vendor or null if not found
    */
   private Vendor findVendorFromUrl(String urlString) {
     // Build URI from URL and extract the top level host name
     Uri uri = Uri.parse(urlString);
     String host = extractHostName(uri);
     if (host == null) return null;
     
     // Loop through the vendors looking for one with a base URL pointing
     // to the same top level host name
     for (Vendor vendor : vendorList) {
       Uri vendorUri = vendor.getBaseURI();
       if (host.equals(extractHostName(vendorUri))) return vendor;
     }
     
     return null;
   }
   
   /**
    * Return top level host name associated with URL
    * @param urlStr URL string
    * @return top level host name extracted from URL
    */
   private static String extractHostName(Uri uri) {
     String host = uri.getHost();
     int pt = host.lastIndexOf('.');
     if (pt > 0) pt = host.lastIndexOf('.', pt-1);
     if (pt >= 0) host = host.substring(pt+1);
     return host;
   }
   
   /**
    * Append vendor status info to logging buffer 
    * @param sb String buffer accumulated log information
    */
   public void addStatusInfo(StringBuilder sb) {
     for (Vendor vendor : vendorList) vendor.addStatusInfo(sb);
   }
   
   /**
    * Find vendor with matching vendor code
    * @param vendorCode vendor code
    * @return matching vendor object, or null if none found.
    */
  Vendor findVendor(String vendorCode) {
     for (Vendor vendor : vendorList) {
       if (vendorCode.equals(vendor.getVendorCode())) return vendor;
     }
     return null;
   }
 
   
   // Support singleton instance of VendorManager
   private static final VendorManager instance = new VendorManager();
   
   public static VendorManager instance() {
     return instance;
   }
 
 }
