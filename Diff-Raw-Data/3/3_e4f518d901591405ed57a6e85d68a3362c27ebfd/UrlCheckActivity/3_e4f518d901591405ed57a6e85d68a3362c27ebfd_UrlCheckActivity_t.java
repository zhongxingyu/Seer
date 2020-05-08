 
 package jp.coffee_club.tco;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import java.util.regex.Pattern;
 
 public class UrlCheckActivity extends Activity
         implements RedirectChecker.OnResultListener {
     /** logcat tag */
     private static final String TAG = "StartActivity";
 
     /** Intent extra from twicca */
     public static final String EXTRA_EXPANDED_URL = "expanded_url";
 
     /** target URL pattern */
     private static final Pattern mCheckURL[] = new Pattern[] {
             Pattern.compile("^http://bit\\.ly/"),
             Pattern.compile("^http://tinyurl\\.com/"),
             Pattern.compile("^http://j\\.mp/"),
             Pattern.compile("^http://goo\\.gl/"),
             Pattern.compile("^http://youtu\\.be/"),
             Pattern.compile("^http://htn\\.to/"),
             // TODO: load from list or on/off setting
     };
 
     /** progress dialog */
     private ProgressDialog mProgess;
     /** URL checker */
     private RedirectChecker mRedirectChecker;
 
     /**
      * create activity event
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Uri data = getIntent().getData();
         if (data == null) {
             // no Uri data
             showAlert(R.string.app_name);
             finish();
             return;
         }
         String url = data.toString();
         debuglog("input URL: " + url);
 
        Bundle extras = getIntent().getExtras();
        Object expanded_url = (extras != null) ? extras.get(EXTRA_EXPANDED_URL) : null;
         if (expanded_url != null) {
             // expanded_url is android.net.Uri?
             // http://twitter.com/tamacjp/status/251882983202635778
             url = expanded_url.toString();
             debuglog("expanded_url: " + url);
         }
 
         onRedirect(url);
     }
 
     /**
      * stop activity event
      */
     @Override
     protected void onStop() {
         super.onStop();
 
         // stop thread
         if (mRedirectChecker != null) {
             mRedirectChecker.cancel(false);
             mRedirectChecker = null;
         }
 
         // remove progress
         if (mProgess != null) {
             mProgess.dismiss();
             mProgess = null;
         }
 
         // end activity
         finish();
     }
 
     /**
      * URL redirect event
      */
     @Override
     public void onRedirect(String url) {
         // check URL
         for (Pattern pat : mCheckURL) {
             if (pat.matcher(url).find()) {
                 // target URL => check redirect again
                 checkUrl(url);
                 return;
             }
         }
         // not target URL
 
         // open URL
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(url));
         startActivity(intent);
         finish();
     }
 
     /**
      * error occur event
      */
     @Override
     public void onError(int code) {
         // show error alert
         showAlert(code);
 
         if (code == RedirectChecker.RESULT_NOT_REDIRECT) {
             // we can startActivity() with original URL.
             // but into an infinite loop in the worst case.
         }
     }
 
     /**
      * start check URL
      *
      * @param url
      */
     private void checkUrl(String url) {
         if (mProgess == null) {
             // show progress
             mProgess = new ProgressDialog(this);
             mProgess.setOnCancelListener(new DialogInterface.OnCancelListener() {
                 @Override
                 public void onCancel(DialogInterface dialog) {
                     finish();
                 }
             });
             mProgess.show();
         }
 
         // URL redirect
         mRedirectChecker = new RedirectChecker(url, this);
     }
 
     /**
      * show alert by resource id
      *
      * @param msg
      */
     private void showAlert(int msg) {
         showAlert(getString(msg));
     }
 
     /**
      * show alert by string
      *
      * @param msg
      */
     private void showAlert(String msg) {
         // Toast, or AlertDialog?
         Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
         finish();
     }
 
     /**
      * output debug log
      *
      * @param msg
      */
     private void debuglog(String msg) {
         if (BuildConfig.DEBUG) {
             android.util.Log.d(TAG, msg);
         }
     }
 }
