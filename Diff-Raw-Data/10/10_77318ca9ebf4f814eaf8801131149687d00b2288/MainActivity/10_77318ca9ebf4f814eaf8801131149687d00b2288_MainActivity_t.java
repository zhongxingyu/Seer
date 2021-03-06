 package temp.gvm;
 
 import temp.gvm.api.Voice;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ArrayAdapter;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import com.google.android.gms.common.AccountPicker;
 
 public class MainActivity extends Activity
 {
     private final static int REQUEST_ACCOUNT = 1;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setListNavigationCallbacks(
                 // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                         actionBar.getThemedContext(),
                         android.R.layout.simple_list_item_1,
                         android.R.id.text1,
                         new String[]{ "Inbox", "All Texts", "Trash" }),
 
                 // Provide a listener to be called when an item is selected.
                 new ActionBar.OnNavigationListener() {
                     public boolean onNavigationItemSelected(int position, long id) {
                         // Take action here, e.g. switching to the
                         // corresponding fragment.
                         return true;
                     }
                 });
         
         this.setupVoice();
     }
 
     protected void setupVoice()
     {
         // Have the user select the account they want to use
         boolean hasAlreadySetupAccount = false;
         if (hasAlreadySetupAccount) {
             // TODO check for an already selected account.
             // this.doLogin(accountName);
         } else {
             try {
                 // Choose a google account that has Google Voice.
                 Intent accountIntent = AccountPicker.newChooseAccountIntent(null, null, new String[] { "com.google" }, false, null, "grandcentral", new String[] { "service_grandcentral" }, null);
                 this.startActivityForResult(accountIntent, MainActivity.REQUEST_ACCOUNT);
                Log.i("Purple","Starting Account Chooser Intent");
             } catch (Exception e) {
                 // TODO No account support
                Log.e("Purple","No Account Support");
             }
         }
     }
 
     protected void doLogin(String accountName)
     {
         // Get the Account Manager
         AccountManager manager = AccountManager.get(this);
         // Create an account object for the selected account
         Account account = new Account(accountName, "com.google");
        Log.i("Purple","Getting Token for Grandcentral Service");
         manager.getAuthToken(account, "grandcentral", null, true, new TokenCallback(), null);
     }
     
     protected void setupLayout()
     {
         this.setContentView(R.layout.activity_main);
     }
 
     private class TokenCallback implements AccountManagerCallback<Bundle>
     {
         @Override
         public void run(AccountManagerFuture<Bundle> authToken)
         {
             String tok = null;
             Bundle result;
             try {
                 result = authToken.getResult();
                 tok = (String) result.get(AccountManager.KEY_AUTHTOKEN);
                 if (tok != null) {
                     ((PurpleApplication) MainActivity.this.getApplication()).googleVoice = new Voice(tok);
                 }
                 MainActivity.this.setupLayout();
             } catch (Exception e) {
                 // TODO Error Checking
                 e.printStackTrace();
             }
         }
     }
 
     protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
     {
         if (requestCode == MainActivity.REQUEST_ACCOUNT && resultCode == RESULT_OK) {
             String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
             this.doLogin(accountName);
         } else {
             // TODO Error Checking
         }
     }
 }
