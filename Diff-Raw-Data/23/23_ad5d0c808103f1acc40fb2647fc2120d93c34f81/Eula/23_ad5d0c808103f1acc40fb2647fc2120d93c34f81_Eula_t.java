 package gov.cdc.pttadvisor;
 
 /**
  * This code is based off the example given here:
  * http://blog.donnfelker.com/2011/02/17/android-a-simple-eula-for-your-android-apps/
  */
 
 import gov.cdc.pttadvisor.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.webkit.WebView;
 import android.view.View;
 
 /**
  *  A class to display a EULA dialog if 1) it's never been agreed to before or 2) the app's version
  *  number was updated
  *  @author Will
  *
  */
 public class Eula {
 	//private Activity mActivity;
	private Activity mActivity;
 	
 	/**
 	 * Create the Eula object, with the app's context.
 	 * @param context
 	 */
 	public Eula(Activity context) {
		mActivity = context;
 	}
 	
 	/**
 	 * just a string prepended to variables to denote them as used by the Eula class
 	 */
 	private String EULA_PREFIX = "eula_";
 	
 	/**
 	 * Retrieve and return the PackageInfo of this app; this contains, among other things,
 	 * the version number.
 	 * @return
 	 */
 	private PackageInfo getPackageInfo() {
         PackageInfo pi = null;
         try {
              pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
         return pi;
     }
 
 
 	/**
 	 * Main method.  Decides whether or not to display the EULA dialog; if so, display it.  If the 
 	 * user agrees, close the dialog and continue the app.  If the user cancels, kill the app. 
 	 */
     public void show(boolean forceShow) {
         PackageInfo versionInfo = getPackageInfo();
 
         // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
         final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
         boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
 
         if(hasBeenShown == false || forceShow){
 
             // build the dialog box, setting the content as the webview above
             AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
 
             // Inflate the activty_eula_webview.xml view
             LayoutInflater factory = LayoutInflater.from(mActivity);
             View view = factory.inflate(R.layout.activity_eula_webview, null);
 
             // get the child WebView and populate it with the content of assets/eula.html
             WebView webView = (WebView) view.findViewById(R.id.eulaWebView);
             webView.loadUrl("file:///android_asset/html/eula.html");
             
             // Set the View for this dialog
             dialog.setView(view);
             
             // default behavior-- assumes this is the accept/decline dialog shown when first starting the app
             if (!forceShow) {
             dialog.setPositiveButton("Accept", new Dialog.OnClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     // Mark this version as read.
                     SharedPreferences.Editor editor = prefs.edit();
                     editor.putBoolean(eulaKey, true);
                     editor.commit();
                     
                     // they accepted, so kick off the initialization of the MainView
                    MainView mainView = (MainView) mActivity;
                    mainView.initialize();
                     dialogInterface.dismiss();
                 }
             });
             dialog.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
 
             	@Override
             	public void onClick(DialogInterface dialog, int which) {
             		// Close the activity as they have declined the EULA
             		mActivity.finish();
         		} 
                     
             });
             } else {
             	// if forceShow is true, then only show a "Dismiss" button
             	dialog.setNeutralButton("Dismiss", new Dialog.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 });
             }
 
             dialog.show();
         } else {
         	// the EULA has already been shown before, so just go ahead and initialize the MainView()
        	MainView mainView = (MainView) mActivity;
            mainView.initialize();
         }
     }
 	
     /**
      * A wrapper to tell whether or not the EULA has already been accepted in the past
      * @return true if the EULA has been accepted before
      */
     public boolean hasBeenAccepted() {
         PackageInfo versionInfo = getPackageInfo();
         final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
         final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
         boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
         return hasBeenShown;
     }
 }
