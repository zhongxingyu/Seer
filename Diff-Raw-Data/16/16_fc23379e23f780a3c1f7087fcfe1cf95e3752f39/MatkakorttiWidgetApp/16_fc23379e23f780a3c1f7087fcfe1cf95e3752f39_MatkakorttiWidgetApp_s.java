 package fi.iki.dezgeg.matkakorttiwidget.gui;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import fi.iki.dezgeg.matkakorttiwidget.matkakortti.Card;
 import fi.iki.dezgeg.matkakorttiwidget.matkakortti.MatkakorttiApi;
 
 public class MatkakorttiWidgetApp extends Application {
     public static final boolean DEBUG = true;
     private static final String ERROR_REPORT_URL = "https://tmtynkky.users.cs.helsinki.fi/error.php";
     private static long MIN_REPORT_INTERVAL_MS = 24 * 3600 * 1000;
 
     private static PackageInfo packageInfo;
     private static SharedPreferences prefs;
 
     private Thread.UncaughtExceptionHandler prevUncaughtExceptionHandler;
 
     // Synchronized since when the app is started from the IDE,
     // the SettingsActivity and the widget updater run concurrently ->
     // both log in to Oma Matkakortti simultaneously -> session errors.
     public static synchronized List<Card> getCardList() throws Exception {
         String username = prefs.getString("username", "<not set>");
         String password = prefs.getString("password", "");
 
         if (DEBUG && username.equals("dezgegt"))
             return Arrays.asList(
                     new Card("Tyhja", "a", BigDecimal.ZERO, null),
                     new Card("Rahaa", "b", BigDecimal.TEN, null),
                     new Card("Kautta", "c", BigDecimal.ZERO, new Date(113, 11, 28)),
                     new Card("Raha+Kausi", "d", new BigDecimal("99.99"), new Date()),
                     new Card("Paljon rahaa", "e", new BigDecimal("199.99"), null));
         return new MatkakorttiApi(username, password).getCards();
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         try {
             packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
 
         prevUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread thread, Throwable e) {
                 try {
                     reportException("UncaughtExceptionHandler", e).get(8, TimeUnit.SECONDS);
                 } catch (Throwable t) {
                     // ignore
                 }
 
                 if (prevUncaughtExceptionHandler != null)
                     prevUncaughtExceptionHandler.uncaughtException(thread, e);
             }
         });
     }
 
     public static AsyncTask<Void, Void, Void> reportException(final String where, final Throwable e) {
         e.printStackTrace();
         if (prefs.getLong("lastErrorReport", new Date().getTime()) - new Date().getTime() <= MIN_REPORT_INTERVAL_MS)
             return null;
         prefs.edit().putLong("lastErrorReport", new Date().getTime()).commit();
 
        return new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... voids) {
                 try {
                     StringWriter sw = new StringWriter();
                     PrintWriter pw = new PrintWriter(sw);
                     e.printStackTrace(pw);
 
                     List<NameValuePair> data = new ArrayList<NameValuePair>();
                     data.add(new BasicNameValuePair("where", where));
                     data.add(new BasicNameValuePair("backtrace", sw.toString()));
                     if (packageInfo != null) {
                         data.add(new BasicNameValuePair("version", String.format("%s (%s %s)",
                                 packageInfo.versionName, packageInfo.versionCode, DEBUG ? "debug" : "release")));
                     }
 
                     HttpClient httpclient = new DefaultHttpClient();
                     HttpPost post = new HttpPost(ERROR_REPORT_URL);
                     try {
                         post.setEntity(new UrlEncodedFormEntity(data));
                     } catch (UnsupportedEncodingException uee) {
                     }
                     try {
                         httpclient.execute(post);
                         System.out.println("Error reported.");
                     } catch (IOException e1) {
                     }
                 } catch (Throwable t) {
                     // ignore
                 }
                 return null;
             }
         }.execute();
     }
 
 }
