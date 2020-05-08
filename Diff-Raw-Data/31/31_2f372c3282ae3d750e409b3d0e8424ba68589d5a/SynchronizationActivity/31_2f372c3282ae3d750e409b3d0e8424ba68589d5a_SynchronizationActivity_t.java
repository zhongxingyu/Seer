 package com.mss.application;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.mss.application.services.Constants;
 import com.mss.application.services.DummyProvider;
 import com.mss.application.services.SynchronizationAdapter;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
import android.provider.Settings;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SynchronizationActivity extends SherlockFragmentActivity {  
 
     private Account mAccount;
     private boolean mFullSync;
 
     // UI references.
     private CheckBox mFullSyncView;
     private TextView mLastSync;
     private View mSyncFormView;
     private View mSyncStatusView;
     private TextView mSyncStatusMessageTextView;
     
     SharedPreferences mSharedPreferences;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_synchronization);
                 
         // Set up the login form.
         mFullSyncView = (CheckBox) findViewById(R.id.full_synchronization_checkbox);
         mLastSync = (TextView) findViewById(R.id.last_sync_text_view);
         
         mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         String lastSync = mSharedPreferences.getString("last_sync", "");
         mLastSync.setText(lastSync);
 
         mSyncFormView = findViewById(R.id.sync_form);
         mSyncStatusView = findViewById(R.id.sync_status);
         mSyncStatusMessageTextView = (TextView)findViewById(R.id.sync_status_message);
         
         findViewById(R.id.sync_run_button).setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
            	try {
            		AccountManager accountManager = AccountManager.get(getApplicationContext());
                    Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
                    if (accounts.length == 0) {
                    	startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
                    	return;
                    }
            		
             		mFullSyncView = (CheckBox)findViewById(R.id.full_synchronization_checkbox);
                     mFullSync = mFullSyncView.isChecked();
 
                     Bundle bundle = new Bundle();
                     bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                     bundle.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                     bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                     bundle.putBoolean("full_sync", mFullSync);
                     ContentResolver.requestSync(mAccount, DummyProvider.getAuthority(), bundle);                    
                     showProgress(true);
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
             }
         });
         
        AccountManager accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length > 0) {
        	mAccount = accounts[0];        
        	showProgress(ContentResolver.isSyncActive(mAccount, DummyProvider.getAuthority()));
        }
         
         if (getSupportActionBar() != null)
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
     }
     
     @Override
     public void onResume() {
       super.onResume();
 
       // Register mMessageReceiver to receive messages.
       LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
           new IntentFilter(SynchronizationAdapter.SYNC_EVENT_KEY));
     }
     
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
    
     private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
     	@Override
     	public void onReceive(Context context, Intent intent) {
     	    // Extract data included in the Intent
     	    int messageId = intent.getIntExtra(SynchronizationAdapter.MESSAGE_KEY, 0);
     	    String status = intent.getStringExtra(SynchronizationAdapter.STATUS_KEY);
     	    if (status == null || 
     	    	(!status.equals(SynchronizationAdapter.SYNC_FINISHED_KEY) &&
     	    	!status.equals(SynchronizationAdapter.SYNC_FAILED_KEY))) {    	    	
     	    	mSyncStatusMessageTextView.setText(getString(messageId));    	    
     	    } else {
     	    	Log.d("receiver", "Got status: " + status);
     	    	String lastSync = mSharedPreferences.getString("last_sync", "");    	    	
                 mLastSync.setText(lastSync);
     	    	showProgress(false);
     	    	
     	    	if (status.equals(SynchronizationAdapter.SYNC_FINISHED_KEY)) {
     	    		Toast.makeText(context, getString(R.string.alert_sync_finished), Toast.LENGTH_LONG).show();
     	    	} else if (status.equals(SynchronizationAdapter.SYNC_FAILED_KEY)) {
     	    		Toast.makeText(context, getString(R.string.alert_sync_failed), Toast.LENGTH_LONG).show();
     	    	}
     	    }
     	}
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu_synchronization, menu);
 		
         return true;
     }
 
     @Override
 	public boolean onOptionsItemSelected(MenuItem item)  {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			Intent upIntent = new Intent(this, MainActivity.class);
 			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
 				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
 				finish();
 			} else {
 				NavUtils.navigateUpTo(this, upIntent);
 			}
 			return true;
 		case R.id.menu_item_cancel_sync:
 			ContentResolver.cancelSync(mAccount, DummyProvider.getAuthority());
     		showProgress(false);		
     		return true;
 		default:
 			break;
 		}
 		
 		return false;
 	}
 
 	/**
      * Shows the progress UI and hides the login form.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     public void showProgress(final boolean show) {
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             mSyncStatusView.setVisibility(View.VISIBLE);
             mSyncStatusView.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 1 : 0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                         	mSyncStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                         }
                     });
 
             mSyncFormView.setVisibility(View.VISIBLE);
             mSyncFormView.animate()
                     .setDuration(shortAnimTime)
                     .alpha(show ? 0 : 1)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                         	mSyncFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                         }
                     });
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
         	mSyncStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
         	mSyncFormView.setVisibility(show ? View.GONE : View.VISIBLE);
         }
     }
 }
 
 
