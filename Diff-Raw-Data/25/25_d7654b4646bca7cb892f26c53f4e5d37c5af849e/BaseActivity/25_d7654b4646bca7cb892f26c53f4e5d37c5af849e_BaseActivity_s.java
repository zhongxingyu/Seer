 package com.four_envelope.android.activity;
 
 import com.four_envelope.android.Constants;
 import com.four_envelope.android.R;
 import com.four_envelope.android.operation.UpdateListener;
 import com.four_envelope.android.store.StoreClient;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.Window;
 
 public abstract class BaseActivity extends Activity implements UpdateListener {
 
//	protected AsyncTask<Object, Object, Object> mGetContent;
 	protected int mContentView; 
 	
 	protected int mMenuRes = R.menu.base_menu;
 	protected int mProgressRes = R.string.progress_msg_load;
 	
 	public boolean refreshContent;
 	
 	public ProgressDialog dialog;
 	
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         
         setContentView(mContentView);
         
 		StoreClient.setPreferences( getSharedPreferences( Constants.APP_PREFS_NAME, 0) );
 //		StoreClient.logout();
 		
 		dialog = new ProgressDialog(this);
 		dialog.setMessage( getText( mProgressRes ) );
 		
         if ( StoreClient.isLogged() )
         	requestPageContent();
         else
         	Invoke.User.authenticate(this);
 	}
 	
 	abstract void fillPageContent();
 	
     protected void requestPageContent() {
     	showProgress();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == Invoke.Extras.AUTH_REQUEST) {
             if (resultCode == RESULT_OK) {
             	requestPageContent();
             }
         }
     }
 
     /**
      * Shows the progress UI for a lengthy operation.
      */
     protected void showProgress(int progressRes) {
 		dialog.setMessage( getText( progressRes ) );
 		showProgress();
     }
 	
     protected void showProgress() {
 		dialog.setIndeterminate(true);
 		dialog.setCancelable(true);
 		dialog.show();
 
     	setProgressBarIndeterminateVisibility(true);
     }
     
     /**
      * Hides the progress UI for a lengthy operation.
      */
     protected void hideProgress() {
     	setProgressBarIndeterminateVisibility(false);
 
     	dialog.dismiss();
     }    
 
     public boolean onCreateOptionsMenu(Menu menu) {  
     	MenuInflater inflater = getMenuInflater();
         inflater.inflate( mMenuRes, menu );
         return true;
       }  
     
     public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 			case R.id.menu_info:
 				Invoke.User.status(this);
 				return true;
 
 			case R.id.menu_login:
 				StoreClient.logout();
 				Invoke.User.authenticate(this);
 				return true;
 
 			case R.id.menu_refresh:
 				if ( StoreClient.isLogged() ) {
 					refreshContent = true;
 					requestPageContent();
 				}
 		        else
 		        	Invoke.User.authenticate(this);
 				
 				return true;
 				
 			default:
 				return super.onOptionsItemSelected(item);
 		}
     }
 
     @Override
 	public Context getUpdateContext() {
 		return getApplicationContext();
 	}
     
 	@Override
 	public void onUpdate() {
 		fillPageContent();			
 		hideProgress();
 	}
     
 }
