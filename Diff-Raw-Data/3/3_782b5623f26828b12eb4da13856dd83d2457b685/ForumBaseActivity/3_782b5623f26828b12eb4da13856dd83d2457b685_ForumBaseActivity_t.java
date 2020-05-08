 package com.normalexception.forum.rx8club.activities;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import ch.boye.httpclientandroidlib.client.ClientProtocolException;
 
 import com.normalexception.forum.rx8club.Log;
 import com.normalexception.forum.rx8club.MainApplication;
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.activities.utils.UtilitiesDialog;
 import com.normalexception.forum.rx8club.html.LoginFactory;
 import com.normalexception.forum.rx8club.preferences.Preferences;
 
 /**
  * Abstract activity that handles all common tasks for the activities
  * contained in the application.  The most common are menu buttons and
  * GUI handlers
  */
 public abstract class ForumBaseActivity extends FragmentActivity implements OnClickListener {
 	
 	protected AsyncTask<Void,?,Void> updaterTask;
 	protected ProgressDialog loadingDialog;
 	
 	protected static final int LOGOFF_MENU = 0;
 	protected static final int ABOUT_MENU = 1;
 	protected static final int MAIN_MENU = 2;
 	protected static final int OPTIONS_MENU = 3;
 	protected static final int USERCP_MENU = 4;
 	protected static final int UTILS_MENU = 5;
 	
 	private static String TAG = "ForumBaseActivity";
 	
 	protected String thisPage = "1", finalPage = "1";
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.view.View.OnClickListener#onClick(android.view.View)
 	 */
 	@Override
 	public void onClick(View arg0) {
 		// Before anything, make sure we are still logged in
		if(!LoginFactory.getInstance().isLoggedIn() && 
				!LoginFactory.getInstance().isGuestMode()) {
 			try {
 				LoginFactory.getInstance().login();
 			} catch (NoSuchAlgorithmException e1) {
 			} catch (ClientProtocolException e1) {
 			} catch (IOException e1) {}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu){ 
 		menu.add(0,MAIN_MENU,0,R.string.menuGoMain);
         menu.add(0,LOGOFF_MENU,0,
         		LoginFactory.getInstance().isLoggedIn()? 
         				R.string.menuLogoff : R.string.menuLogon);
         menu.add(0,OPTIONS_MENU,0,R.string.menuPreferences);
         if(LoginFactory.getInstance().isLoggedIn())
         	menu.add(0,USERCP_MENU,0,R.string.menuControlPanel);
         menu.add(0,UTILS_MENU,0,R.string.menuUtilities);
         menu.add(0,ABOUT_MENU,0,R.string.menuAbout);
         return true; 
     } 
 	
 	/**
 	 * Close all activities and return to login
 	 */
 	public void returnToLoginPage(boolean clearPrefs) {
 		Intent _intent = null;
 		LoginFactory.getInstance().logoff(clearPrefs);
 		_intent = new Intent(this, LoginActivity.class);
 		_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(_intent);
 		finish();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
     public boolean onOptionsItemSelected (MenuItem item)
     { 
 		Intent _intent = null;
         switch(item.getItemId())
         {
            case(LOGOFF_MENU):
    				Log.v(TAG, "Logoff Pressed");
            
            		// Lets make sure the user didn't accidentally click this
 				DialogInterface.OnClickListener dialogClickListener = 
 						new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						switch (which){
 					    	case DialogInterface.BUTTON_POSITIVE:
 					    		returnToLoginPage(true);
 				   				break;
 				        }
 				    }
 				};
 	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder
 					.setMessage(R.string.dialogLogoffConfirm)
 					.setPositiveButton(R.string.Yes, dialogClickListener)
 				    .setNegativeButton(R.string.No, dialogClickListener);
 				if(LoginFactory.getInstance().isGuestMode())
 					returnToLoginPage(true);
 				else
 					builder.show();
 				
    				break;
            case(USERCP_MENU):
            	   	_intent = 
            			   new Intent(MainApplication.getAppContext(), UserCpActivity.class);
         	   break;
            case(ABOUT_MENU):
            		_intent =
            				new Intent(MainApplication.getAppContext(), AboutActivity.class);
            		break;
            case(MAIN_MENU):
         	   	_intent =
         	   			new Intent(MainApplication.getAppContext(), MainActivity.class);
            		_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            		finish();
            		break;
            case(OPTIONS_MENU):
         	   	_intent =
         	   			new Intent(MainApplication.getAppContext(), Preferences.class);
            		break;
            case(UTILS_MENU):
         	   UtilitiesDialog ud = new UtilitiesDialog(this);
            	   ud.show();
         	   break;
         } 
         
         if(_intent != null)
         	startActivity(_intent);
         
         return false; 
     } 
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 	    if ((keyCode == KeyEvent.KEYCODE_BACK))
 	    {
 	    	Log.v(TAG, "Back pressed, Finishing Activity");
 	        finish();
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
      * Update the pagination text
      * @param thisPage	the page we are currently on
      * @param finalPage	the last page of the category/thread
      */
     protected void updatePagination(String thisPage, String finalPage) {
     	try {
 	    	final boolean first, prev, next, last;
 	    	this.thisPage = thisPage;
 	    	this.finalPage = finalPage;
 	
 			final TextView pagination = (TextView)findViewById(R.id.paginationText);
 	    	String label = pagination.getText().toString();            	
 	    	label = label.replace("X", thisPage);
 	    	label = label.replace("Y", finalPage);
 	    	final String finalizedLabel = label;
 	    	
 	    	// Set up our buttons
 	    	prev = Integer.parseInt(thisPage) == 1?
 	    			false : true;
 	    	first = Integer.parseInt(thisPage) == 1?
 	    			false : true;
 	    	next = Integer.parseInt(finalPage) > Integer.parseInt(thisPage)?
 	    			true : false;
 	    	last = Integer.parseInt(thisPage) == Integer.parseInt(finalPage)?
 	    			false : true;
 	    	
 	    	// Update GUI components
 	    	runOnUiThread(new Runnable() {
 	    		public void run() {
 	    			findViewById(R.id.previousButton).setEnabled(prev);
 					findViewById(R.id.firstButton).setEnabled(first);
 					findViewById(R.id.nextButton).setEnabled(next);
 					findViewById(R.id.lastButton).setEnabled(last);
 					pagination.setText(finalizedLabel);
 	    		}
 	    	});
     	} catch (Exception e) { 
     		Log.d(TAG, "Error Parsing Pagination");
     	}
     }
 }
