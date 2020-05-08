 /*******************************************************************************
  * Copyright (c) 2012 MASConsult Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.masconsult.bgbanking.activity;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.Window;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.zubhium.ZubhiumSDK;
 
 import eu.masconsult.bgbanking.BankingApplication;
 import eu.masconsult.bgbanking.R;
 import eu.masconsult.bgbanking.activity.fragment.AccountsListFragment;
 import eu.masconsult.bgbanking.activity.fragment.ChooseAccountTypeFragment;
 import eu.masconsult.bgbanking.banks.Bank;
 import eu.masconsult.bgbanking.provider.BankingContract;
 import eu.masconsult.bgbanking.sync.SyncAdapter;
 
 public class HomeActivity extends SherlockFragmentActivity {
 
     private static final String TAG = BankingApplication.TAG + "HomeActivity";
 
     final BroadcastReceiver syncReceiver = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (SyncAdapter.START_SYNC.equals(intent.getAction())) {
                 syncStateChanged(true);
             } else if (SyncAdapter.STOP_SYNC.equals(intent.getAction())) {
                 syncStateChanged(false);
             }
         }
     };
 
     private AccountManager accountManager;
 
     @Override
     protected void onCreate(Bundle arg0) {
         super.onCreate(arg0);
 
         enableZubhiumUpdates(this);
 
         accountManager = AccountManager.get(this);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     }
 
     @Override
     protected void onDestroy() {
         disableZubhiumUpdates(this);
         super.onDestroy();
     }
 
     // TODO: extract to some utility class
     private static ZubhiumSDK getZubhiumSDK(Context context) {
         BankingApplication globalContext = (BankingApplication) context.getApplicationContext();
         return globalContext != null ? globalContext.getZubhiumSDK() : null;
     }
 
     // TODO: extract to some utility class
     private static void enableZubhiumUpdates(Activity activity) {
         ZubhiumSDK sdk = getZubhiumSDK(activity);
         if (sdk != null) {
             /**
              * Lets register kill switch / update receiver Read more :
              * https://www.zubhium.com/docs/sendmessage/
              */
             sdk.registerUpdateReceiver(activity);
         }
     }
 
     // TODO: extract to some utility class
     private static void disableZubhiumUpdates(Activity activity) {
         ZubhiumSDK sdk = getZubhiumSDK(activity);
         if (sdk != null) {
             sdk.unRegisterUpdateReceiver();
         }
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         checkForLoggedAccounts();
 
         FragmentManager fm = getSupportFragmentManager();
 
         // Create the list fragment and add it as our sole content.
         if (fm.findFragmentById(android.R.id.content) == null) {
             AccountsListFragment list = new AccountsListFragment();
             fm.beginTransaction().add(android.R.id.content, list).commit();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(SyncAdapter.START_SYNC);
         intentFilter.addAction(SyncAdapter.STOP_SYNC);
         registerReceiver(syncReceiver, intentFilter);
 
         syncStateChanged(isSyncActive());
     }
 
     @Override
     protected void onPause() {
         unregisterReceiver(syncReceiver);
 
         super.onPause();
     }
 
     protected void checkForLoggedAccounts() {
         Bank[] banks = Bank.values();
         String[] accountTypes = new String[banks.length];
 
         boolean hasAccounts = false;
         for (int i = 0; i < banks.length; i++) {
             accountTypes[i] = banks[i].getAccountType(this);
             if (accountManager.getAccountsByType(banks[i].getAccountType(this)).length > 0) {
                 hasAccounts = true;
             }
         }
 
         if (!hasAccounts) {
             addAccount();
         }
     }
 
     void addAccount() {
         ChooseAccountTypeFragment accountTypesFragment = new ChooseAccountTypeFragment();
         accountTypesFragment.show(getSupportFragmentManager(), "AccountsDialog");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         MenuItem addAccountItem = menu.add("Add account");
         addAccountItem.setIcon(R.drawable.ic_menu_add);
         addAccountItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         addAccountItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 addAccount();
                 return true;
             }
         });
 
         MenuItem sendFeedback = menu.add("Send feedback");
         sendFeedback.setIcon(R.drawable.ic_menu_start_conversation);
         sendFeedback.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         sendFeedback.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 getZubhiumSDK(HomeActivity.this).openFeedbackDialog(HomeActivity.this);
                 return true;
             }
         });
 
         return true;
     }
 
     boolean isSyncActive() {
         for (Bank bank : Bank.values()) {
             for (Account account : accountManager.getAccountsByType(bank.getAccountType(this))) {
                 if (ContentResolver.isSyncActive(account, BankingContract.AUTHORITY)) {
                     Log.v(TAG, bank + " is syncing");
                     return true;
                 }
             }
         }
         Log.v(TAG, "nothing is syncing");
         return false;
     }
 
     void syncStateChanged(boolean syncActive) {
         Log.v(TAG, "syncStateChanged: " + syncActive);
         setProgressBarIndeterminateVisibility(syncActive);
     }
 }
