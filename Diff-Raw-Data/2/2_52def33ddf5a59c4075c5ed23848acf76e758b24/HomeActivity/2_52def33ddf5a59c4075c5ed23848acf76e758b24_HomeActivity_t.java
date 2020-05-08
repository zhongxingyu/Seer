 
 package org.inftel.ssa.mobile.ui;
 
 import static android.content.ContentResolver.requestSync;
 
 import org.inftel.ssa.mobile.R;
 import org.inftel.ssa.mobile.SsaConstants;
 import org.inftel.ssa.mobile.authenticator.AuthenticatorActivity;
 import org.inftel.ssa.mobile.contentproviders.ProjectContentProvider;
 import org.inftel.ssa.mobile.util.AnalyticsUtils;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class HomeActivity extends BaseActivity {
     private static final String TAG = "HomeActivity";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        checkIfExistsAnAccount(); // or show a login dialog
 
         AnalyticsUtils.getInstance(this).trackPageView("/Home");
 
         setContentView(R.layout.activity_home);
         getActivityHelper().setupActionBar(null, 0);
     }
 
     private void checkIfExistsAnAccount() {
         // Check if the user is logged in
         AccountManager am = AccountManager.get(this);
         Account[] acconts = am.getAccountsByType(SsaConstants.ACCOUNT_TYPE);
         if (acconts.length == 0) {
             // If not connected, show login dialog
             final Intent intent = new Intent(this, AuthenticatorActivity.class);
             startActivityForResult(intent, 0);
         }
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         getActivityHelper().setupHomeActivity();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.refresh_menu_items, menu);
         super.onCreateOptionsMenu(menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_refresh) {
             triggerRefresh();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void triggerRefresh() {
         Account account = findAccount();
         if (account != null) {
             requestSync(account, ProjectContentProvider.AUTHORITY, new Bundle());
         } else {
             Toast.makeText(this, "Must be registered to synchronize", Toast.LENGTH_LONG);
         }
     }
 
     private void updateRefreshStatus(boolean refreshing) {
         getActivityHelper().setRefreshActionButtonCompatState(refreshing);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == 0) {
             Log.d(TAG, "login result received");
             if (resultCode == RESULT_OK) {
                 Toast.makeText(this, "Loggin success", Toast.LENGTH_LONG).show();
             } else {
                 Toast.makeText(this, "Loggin canceled", Toast.LENGTH_LONG).show();
             }
         } else {
             super.onActivityResult(requestCode, resultCode, data);
         }
     }
 }
