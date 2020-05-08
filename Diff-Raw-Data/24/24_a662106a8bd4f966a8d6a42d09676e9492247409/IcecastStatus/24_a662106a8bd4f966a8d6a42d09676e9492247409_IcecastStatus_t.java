 package com.portaboom.android.icecastStatus;
 
 /**
  * $Id$
  * 
  * @author elspicyjack at gmail dot com
  * @version $Revision$
  *
  * NOTE: Please do not e-mail the author directly regarding this code.  
  * The proper forum for support is the Streambake Google Groups list at
  * http://groups.google.com/group/streambake or <streambake@groups.google.com>
  * 
 */
 
 /* FIXME
- Javadocs!!!
- Tests!!!
 - Pass this object in to the downloader and parser so that they can make
   callbacks when needed
 - abstract the status URL; the user should be able to enter the server name
   and port in a dialog somewhere and different URL's can be tried 
   in the order of most preferred to least preferred until a URL 
   is found that doesn't 404
 - Call the Prefs screen if the application is started and there's
   no URL set in the properties for this application
 */
 
 // android imports
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class IcecastStatus extends Activity {
     static final String LOGTAG = "IcecastStatus";
     private String statURL = "http://stream.portaboom.com:7767";
     private String fetchedText = "";
     private static final int URL_DIALOG_KEY = 0;
     private TextView tv = null;
     private ScrollView sv = null;
     
     /** 
      * onCreate - Called when the activity is first created. 
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	// FIXME check the statURL here; pop up the preferences screen 
     	// if it's not set
     	Log.d(LOGTAG, "entering onCreate; statURL is " + statURL);
     	super.onCreate(savedInstanceState);
     	// run the fetching sequence
     	this.doFetch();
     } // public void onCreate(Bundle savedInstanceState)
     
     /**
      * onCreateOptionsMenu - Create the options menu
      * 
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = this.getMenuInflater();
     	inflater.inflate(R.menu.menu, menu);
     	return true;
     } // public boolean onCreateOptionsMenu(Menu menu)
     
     /** 
      * onOptionsItemSelected - react based on which option was selected
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch( item.getItemId() ) {
     	case R.id.prefs:
     		//startActivity(new Intent(this, Prefs.class));
     		this.doFetch();
     		return true;
     	case R.id.reload:
     		// rerun the fetching sequence
     		this.doFetch();
     		return true;
     	} // switch( item.getItemId() )
     	return false;
     } // public boolean onOptionsItemSelected(MenuItem item)
     
     /** 
      * getCurrentURL - The current URL to use for downloading status pages
      * @return String statURL 
      */
     public String getCurrentURL() {
     	return statURL;
     } // public String getCurrentURL()
     
     @Override
     /** 
      * onCreateDialog - set up the dialog for URL fetching
      */
     protected Dialog onCreateDialog(int dialogID) {
     	Log.d(LOGTAG, "Creating dialog object");
     	ProgressDialog dialog = new ProgressDialog(this);
     	dialog.setTitle(R.string.fetching_status_page);
     	dialog.setMessage(R.string.fetching_from_url + statURL);
     	dialog.setIndeterminate(true);
     	dialog.setCancelable(true);
     	return dialog;
     }
     
     private void doFetch () {
         // pop up the dialog that shows what base URL 
         // we will be downloading from
         Log.d(LOGTAG, "displaying URL fetch dialog");
         this.showDialog(URL_DIALOG_KEY);
     
         // create the fetching object
         HTTPStatusDownload hsd = new HTTPStatusDownload();
         // try and fetch the status URL
         Log.d(LOGTAG, "Fetching statURL: " + statURL);
         try {
         	fetchedText = hsd.fetch(statURL);
         } catch (Throwable t) {
         	Toast 
             .makeText(this, "Request failed: " + t.toString(), 4000);
             //.show();
         }
 
         // create the output text box
         Log.d(LOGTAG, "Parsing output of statURL");
         ParseStatus pst = new ParseStatus();
         
        // FIXME move this to a view object?
         tv = new TextView(this);
         tv.setText( "Status URL: " + statURL + "\n" + pst.parse( fetchedText ) );
         sv = new ScrollView(this);
         sv.addView(tv);
         setContentView(sv);
         this.dismissDialog(URL_DIALOG_KEY);
     } // private void doFetch ()
 } // public class IcecastStatus extends Activity
 
