 package karyon.Android.Applications;
 
 import Karyon.Applications.ICapabilitiesManager;
 import Karyon.Applications.PropertyManagers.IPropertyManager;
 import Karyon.Collections.HashMap;
 import Karyon.DynamicCode.Java;
 import Karyon.Exceptions.CriticalException;
 import Karyon.Logging.ILogger;
 import Karyon.Utilities;
 import Karyon.Version;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import karyon.Android.Activities.Controller;
 import karyon.Android.Activities.ErrorActivity;
 import karyon.Android.Behaviours.ControllerBehaviour;
 import karyon.Android.Logging.AndroidLogger;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 
 /**
  * The Application class is the core Android Application Controller
  * This is the class that should be overwritten in order to start a
  * new Android Application
  */
 public abstract class Application<T extends Application>
     extends Karyon.Applications.Application<T>
 {
     /**
      * Wrapper class for the Android Application.  This is a helper
      * class that links up the Android Framework Application class to the
      * Karyon Application class
      */
     public static class AndroidApplication
         extends android.app.Application
     {
         private Class<? extends Application> m_oApplicationClass;
 
         public AndroidApplication()
         {
             m_oApplicationClass = (Class<? extends Application>)this.getClass().getEnclosingClass();
         }
 
         public <K extends Application> AndroidApplication(Class<K> toAppClass)
         {
             m_oApplicationClass = toAppClass;
         }
 
         @Override
         public final void onCreate()
         {
             super.onCreate();
             try
             {
                 PackageInfo loInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
 
                 // Create the Karyon Application Instance
                 Java.createObject(m_oApplicationClass,
                        new Version(getString(loInfo.applicationInfo.labelRes) + " " + (loInfo.versionName.replace("-SNAPSHOT", ""))),
                         this);
 
             }
             catch (PackageManager.NameNotFoundException ex)
             {
                 // This should never be able to happen as it is the application package being used
                 // This means if it does happen something has gone horribly wrong
                 throw new CriticalException("Unable to create Android Application");
             }
 
             // Start up the Application
             Application.getInstance().start();
         }
 
         @Override
         public void onTerminate()
         {
             super.onTerminate();
             Application.getInstance().stop();
         }
 
         @Override
         public void onConfigurationChanged(Configuration toNewConfig)
         {
             super.onConfigurationChanged(toNewConfig);
         }
 
         @Override
         public void onLowMemory()
         {
             super.onLowMemory();
             Application.getInstance().notifyLowMemory();
         }
     }
 
     /**
      * Helper method to get the android application instance
      * @return the android application instance
      */
     public static Application getInstance()
     {
         return Karyon.Applications.Application.getInstance(Application.class);
     }
     
     private AndroidApplication m_oAndroidApp;
     private DefaultHttpClient m_oHTTPClient;
     
     /**
      * Creates a new instance of the Application, this can only be
      * created one time
      * @param toVersion the version of the application
      */
     public Application(Version toVersion, AndroidApplication toApp)
     {
         super(toVersion);
         Utilities.checkParameterNotNull("toApp", toApp);
         m_oAndroidApp = toApp;
     }
 
     /**
      * Gets the Application Context
      * @return the Application Context
      */
     public Context getApplicationContext()
     {
         return m_oAndroidApp.getApplicationContext();
     }
 
     /**
      * Gets the requested system service specific to the Application context
      * @param tcService the service to get
      * @param toReturnType the type of object that will be returned
      * @param <K> the type of object that is being requested
      * @return the object or null if unable to retrieve the requested service
      */
     public <K extends Object> K getSystemService(String tcService, Class<K>toReturnType)
     {
         return (K)getApplicationContext().getSystemService(tcService);
     }
 
     /**
      * Use a SharedPreferences Property manager for Android Applications
      * @return a new instance of the SharedPreferences Property Manager
      */
     @Override
     protected IPropertyManager createPropertyManager()
     {
         return new SharedPreferencesPropertyManager(m_oAndroidApp);
     }
 
     @Override
     protected ILogger createLogger()
     {
         return new AndroidLogger();
     }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
     // TODO: Refactor below here
 
     private Boolean m_lDebug;
 
     /**
      * Checks if the application is running in debug mode
      *
      * @return true if debug mode, otherwise false
      */
     // TODO: Move this to Environment
     private boolean isDebuggable()
     {
         if (m_lDebug == null)
         {
             boolean llReturn = false;
             PackageManager loPM = m_oAndroidApp.getApplicationContext().getPackageManager();
             try
             {
                 ApplicationInfo loInfo = loPM.getApplicationInfo(m_oAndroidApp.getApplicationContext().getPackageName(), 0);
                 llReturn = (0 != (loInfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
             }
             catch (Throwable ex)
             {
             }
             m_lDebug = llReturn;
         }
         return m_lDebug;
     }
 
 
     /**
      * Gets the host name of the web server
      */
     public String getWebServerHost()
     {
         return getPropertyManager().<String>getProperty("application.webHost");
     }
 
     /**
      * Gets a behaviour for the specified activity, should be overridden in
      * @param toActivityClass the class that we are attempting to get a behaviour for
      * @param toCurrent the system selected behaviour if any
      * @param <K> the type of activity that the behaviour is for
      * @return the behaviour to use, or null for no behaviour
      */
     public <L extends Controller, K extends ControllerBehaviour<L>> K getBehaviourFor(Class<L> toActivityClass, K toCurrent)
     {
         return toCurrent;
     }
     
     /**
      * Starts an activity of the specified type
      * @param toActivityClass the activity class to start
      * @param toParent the parent activity to use
      */
     public void startActivity(Class<? extends Activity>toActivityClass, Activity toParent)
     {
         this.startActivity(toActivityClass, toParent, null);
     }
     
     /**
      * Starts an activity of the specified type
      * @param toActivityClass the activity class to start
      * @param toParent the parent activity to use
      * @param toParameters the parameters to pass to the activity
      */
     public void startActivity(Class<? extends Activity>toActivityClass, Activity toParent, HashMap<String, ?> toParameters)
     {
         Intent loIntent = new Intent(toParent == null ? getApplicationContext() : toParent.getBaseContext(), toActivityClass);
         if (toParent == null)
         {
             loIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         }
         Bundle loParameters = karyon.Android.Utilities.createBundle(toParameters);
         if (loParameters != null)
         {
             loIntent.putExtras(loParameters);
         }
         if (toParent != null)
         {
             toParent.startActivity(loIntent);
         }
         else
         {
             getApplicationContext().startActivity(loIntent);
         }
     }
 
     /**
      * Creates the default android capability manager
      * @return
      */
     @Override
     protected ICapabilitiesManager createCapabilityManager()
     {
         return new AndroidCapabilitiesManager();
     }
 
 
 
     /**
      * Gets the string from the resource id
      * @param tnStringResourceID the resource id
      * @return the string
      */
     public String getString(int tnStringResourceID)
     {
         return m_oAndroidApp.getString(tnStringResourceID);
     }
 
 
 
     // TODO: This should be refactored into a HTTP manager
 
     /**
      * Gets the DefaultHttpClient for all internet transactions
      * @return the default http client
      */
     public DefaultHttpClient getDefaultHttpClient()
     {
         if (m_oHTTPClient == null)
         {
             SchemeRegistry loRegistry = new SchemeRegistry();
             loRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
             loRegistry.register(new Scheme("http", SSLSocketFactory.getSocketFactory(), 443));
 
             BasicHttpParams loParams = new BasicHttpParams();
 
             HttpConnectionParams.setStaleCheckingEnabled(loParams, true);
             HttpConnectionParams.setTcpNoDelay(loParams, true);
 
             m_oHTTPClient = new DefaultHttpClient(loParams);
             m_oHTTPClient.getCookieStore().getCookies();
 
             String lcDomain = getWebServerHost().replaceAll("^(https?://)?[^\\.]+", "");
             lcDomain = lcDomain.indexOf("/") >= 0  ? lcDomain.substring(0, lcDomain.indexOf("/")) : lcDomain;
 
             // Always add the appID and instance cookies
             BasicClientCookie loCookie = new BasicClientCookie("applicationid", getApplicationGUID());
             loCookie.setDomain(lcDomain);
             loCookie.setPath("/");
             m_oHTTPClient.getCookieStore().addCookie(loCookie);
 
             loCookie = new BasicClientCookie("applicationinstanceid", getInstanceGUID());
             loCookie.setDomain(lcDomain);
             loCookie.setPath("/");
             m_oHTTPClient.getCookieStore().addCookie(loCookie);
 
             loCookie = new BasicClientCookie(Application.getInstance().getPropertyManager().getProperty("application.network.sessionCookie", "PHPSESSID"), getInstanceGUID());
             loCookie.setDomain(lcDomain);
             loCookie.setPath("/");
             m_oHTTPClient.getCookieStore().addCookie(loCookie);
         }
         return m_oHTTPClient;
     }
 
 
     // Everything below here needs refactoring
 
 
 
 
     // TODO: Refactor this to track notifications and allow more flexibility
     public Notification createNotification(int tnIconID, String tcTicker, String tcTitle, String tcContent, int tnARGB, Class<? extends Activity> toActivityClass)
     {
         NotificationManager loManager = getSystemService(Context.NOTIFICATION_SERVICE, NotificationManager.class);
 
         Notification loNotification = new Notification(tnIconID, tcTicker, System.currentTimeMillis());
 
         loNotification.ledARGB = tnARGB;
         loNotification.ledOnMS = 500;
         loNotification.ledOffMS = 500;
         loNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
         loNotification.flags |= Notification.FLAG_ONGOING_EVENT;
         loNotification.flags |= Notification.FLAG_AUTO_CANCEL;
 
         loNotification.setLatestEventInfo(getApplicationContext(),
                                           tcTitle, tcContent, null);
 
         Intent loIntent = new Intent(getApplicationContext(), toActivityClass);
         loIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
         PendingIntent loNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, loIntent,  0);
 
         loNotification.contentIntent = loNotificationIntent;
 
         loManager.notify(1, loNotification);
 
         return loNotification;
     }
 
     public void updateNotification(Notification toNotification, int tnIconID, String tcTicker, String tcTitle, String tcContent, int tnARGB)
     {
         NotificationManager loManager = getSystemService(Context.NOTIFICATION_SERVICE, NotificationManager.class);
 
         toNotification.icon = tnIconID;
         toNotification.tickerText = tcTicker;
 
         toNotification.ledARGB = tnARGB;
         toNotification.ledOnMS = 500;
         toNotification.ledOffMS = 500;
 
         toNotification.setLatestEventInfo(getApplicationContext(), tcTitle, tcContent, null);
 
         loManager.notify(1, toNotification);
     }
 
     public void cancelNotification(Notification toNotification)
     {
         NotificationManager loManager = getSystemService(Context.NOTIFICATION_SERVICE, NotificationManager.class);
         loManager.cancelAll();
     }
 
     public final void showError(int tnResourceID)
     {
         showError(m_oAndroidApp.getString(tnResourceID));
     }
 
     public final void showError(final String tcError)
     {
 
         Intent loAlertDialog = new Intent(m_oAndroidApp.getBaseContext(), ErrorActivity.class);
         loAlertDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         loAlertDialog.putExtra("com.youcommentate.message", tcError);
         m_oAndroidApp.startActivity(loAlertDialog);
     }
 
     public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException
     {
         return m_oAndroidApp.openFileOutput(name, mode);
     }
 
     public FileInputStream openFileInput(String name) throws FileNotFoundException
     {
         return m_oAndroidApp.openFileInput(name);
     }
 
     // TODO: Move the following to a FlurryManager (UserInfo Manager??)
 
     /**
      * Gets the flurry API key for this app
      * @return the flurry api key, or null if there is no key
      */
     public String flurryAPIKey()
     {
         return null;
     }
 
     /**
      * Checks if this application uses Flurry for analytics
      * @return true if flurry is used
      */
     public final boolean usesFlurry()
     {
         return flurryAPIKey() != null;
     }
 }
