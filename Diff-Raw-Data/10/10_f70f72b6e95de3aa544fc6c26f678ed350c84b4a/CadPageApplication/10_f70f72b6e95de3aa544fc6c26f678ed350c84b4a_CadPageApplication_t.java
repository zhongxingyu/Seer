 package net.anei.cadpage;
 
 import net.anei.cadpage.donation.UserAcctManager;
 import net.anei.cadpage.vendors.VendorManager;
 import android.app.Application;
 import android.content.Context;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 
 /**
  * Main CadPage application
  * which is where we need to do our one time initialization
  */
 public class CadPageApplication extends Application {
 
   /* (non-Javadoc)
    * @see android.app.Application#onCreate()
    */
   @Override
   public void onCreate() {
     super.onCreate();
    Log.v("Intialization startup");
     getVersionInfo(this);
     try {
       VendorManager.instance().setup(this);
 
       ManagePreferences.setupPreferences(this);
      
       UserAcctManager.setup(this);
       
       // Reload log buffer queue
       SmsMsgLogBuffer.setup(this);
       
       // Reload existing message queue
       SmsMessageQueue.setupInstance(this);
       
       // Restore any pending SMS message merges
       SmsMsgAccumulator.setup(this);
       
       // See if a new version of Cadpage has been installed
       if (ManagePreferences.newVersion(versionCode)) {
         
         // Reset vendor status
         VendorManager.instance().newReleaseReset(this);
 
         // If we have a GCM registration ID, we are supposed to request a new one
         if (ManagePreferences.gcmEnabled() &&
             ManagePreferences.registrationId() != null) {
            C2DMReceiver.register(this);
         }
       }
       
     } catch (Exception ex) {
       TopExceptionHandler.initializationFailure(this, ex);
     }
     
     // Reinitialize any Widget triggers.  This shouldn't be necessary, but it
     // seems to help avoid sporadic problems with unresponsive Widgets.
     CadPageWidget.reinit(this);
   
     TopExceptionHandler.enable(this);
     Log.v("Initialization complete");
     
   }
   
   
   private static String nameVersion = null;
   private static int versionCode = -1;
   
   private void getVersionInfo(Context context) {
 
     if (nameVersion == null) {
       //Try and find app version name and code
       PackageManager pm = context.getPackageManager();
       try {
         //Get version number, not sure if there is a better way to do this
         PackageInfo info = pm.getPackageInfo(SmsPopupConfigActivity.class.getPackage().getName(), 0);
         nameVersion = " v" + info.versionName;
         versionCode = info.versionCode;
       } catch (NameNotFoundException e) {
         nameVersion = "";
         versionCode = 0;
       }
       nameVersion = context.getString(R.string.app_name) + nameVersion;
     }
   }
   
   public static String getNameVersion() {
     return nameVersion;
   }
   
   public static int getVersionCode() {
     return versionCode;
   }
   
   public static boolean isBetaRelease() {
     return versionCode % 10 > 0;
   }
 }
