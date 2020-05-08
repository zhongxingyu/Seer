 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.pixmob.fm2.ui;
 
 import static org.pixmob.fm2.Constants.DEBUG;
 import static org.pixmob.fm2.Constants.TAG;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.pixmob.fm2.R;
 import org.pixmob.fm2.model.Account;
 import org.pixmob.fm2.model.AccountRepository;
 import org.pixmob.fm2.services.SyncService;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.SystemClock;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.Menu;
 import android.support.v4.view.MenuItem;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * {@link Fragment} displaying {@link Account} instances.
  * @author Pixmob
  */
 public class AccountsFragment extends ListFragment implements
         LoaderCallbacks<List<Account>>, SyncService.Listener, ServiceConnection {
     private AccountAdapter accountAdapter;
     private SyncService syncService;
     private Intent syncServiceIntent;
     private boolean dualPane;
     private int selectedAccountIndex;
     
     @Override
     public void onSyncDone() {
         // Update the list with new content.
         getLoaderManager().getLoader(0).onContentChanged();
     }
     
     @Override
     public void onSyncError(Exception cause) {
         Toast.makeText(getActivity(), R.string.error_sync_failed,
             Toast.LENGTH_SHORT).show();
     }
     
     @Override
     public Loader<List<Account>> onCreateLoader(int id, Bundle args) {
         if (DEBUG) {
             Log.d(TAG, "AccountsFragment.onCreateLoader");
         }
         
         return new AccountListLoader(getActivity());
     }
     
     @Override
     public void onLoaderReset(Loader<List<Account>> loader) {
         if (DEBUG) {
             Log.d(TAG, "AccountsFragment.onLoaderReset");
         }
         
         // Clear the data in the adapter.
         accountAdapter.setData(null);
     }
     
     @Override
     public void onLoadFinished(Loader<List<Account>> loader, List<Account> data) {
         if (DEBUG) {
             Log.d(TAG, "AccountsFragment.onLoadFinished");
         }
         
         // Set the new data in the adapter.
         accountAdapter.setData(data);
         
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
         
         if (dualPane) {
             getListView().setItemChecked(selectedAccountIndex, false);
             
             new Thread() {
                 @Override
                 public void run() {
                     SystemClock.sleep(1000);
                     
                     final AccountDetailsFragment details = (AccountDetailsFragment) getSupportFragmentManager()
                             .findFragmentById(R.id.account_details);
                     if (details != null) {
                         final FragmentTransaction ft = getSupportFragmentManager()
                                 .beginTransaction();
                         ft.remove(details);
                         ft.commit();
                     }
                 }
             }.start();
         }
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         
         setEmptyText(getString(R.string.no_account));
         setHasOptionsMenu(true);
         
         syncServiceIntent = new Intent(getActivity(), SyncService.class);
         
         accountAdapter = new AccountAdapter(getActivity());
         setListAdapter(accountAdapter);
         setListShown(false);
         
         final View detailsFrame = getActivity().findViewById(
             R.id.account_details);
         dualPane = detailsFrame != null
                 && detailsFrame.getVisibility() == View.VISIBLE;
         
         if (savedInstanceState != null) {
             selectedAccountIndex = savedInstanceState.getInt(
                 "selectedAccountIndex", 0);
         }
         
         if (dualPane) {
             getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         }
         
         // Start user account loading.
         getLoaderManager().initLoader(0, null, this);
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("selectedAccountIndex", selectedAccountIndex);
     }
     
     @Override
     public void onStart() {
         super.onStart();
         
         // Bind the service with this fragment in order to register a listener.
         getActivity().bindService(syncServiceIntent, this,
             Context.BIND_AUTO_CREATE);
     }
     
     @Override
     public void onStop() {
         super.onStop();
         
         // The service is not used anymore by this fragment.
         getActivity().unbindService(this);
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         menu.add(Menu.NONE, R.string.menu_refresh, Menu.NONE,
             R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(Menu.NONE, R.string.menu_add_account, Menu.NONE,
             R.string.menu_add_account).setIcon(R.drawable.ic_menu_invite)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(Menu.NONE, R.string.menu_prefs, Menu.NONE, R.string.menu_prefs)
                 .setIcon(R.drawable.ic_menu_preferences)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.string.menu_refresh:
                 onActionRefresh();
                 break;
             case R.string.menu_add_account:
                 onActionAddAccount();
                 break;
             case R.string.menu_prefs:
                 onActionPreferences();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public void onDestroy() {
         super.onDestroy();
         
         // Release resources.
         accountAdapter = null;
         syncServiceIntent = null;
     }
     
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         selectAccount(position);
     }
     
     private void selectAccount(int position) {
         if (position >= accountAdapter.getCount()
                 || accountAdapter.getCount() == 0) {
             selectedAccountIndex = 0;
             if (dualPane) {
                 final AccountDetailsFragment details = (AccountDetailsFragment) getSupportFragmentManager()
                         .findFragmentById(R.id.account_details);
                 if (details != null) {
                     final FragmentTransaction ft = getSupportFragmentManager()
                             .beginTransaction();
                     ft.remove(details);
                     ft.commit();
                 }
             }
         } else {
             selectedAccountIndex = position;
             
             final Account account = accountAdapter.getItem(position);
             if (dualPane) {
                 getListView().setItemChecked(position, true);
                 
                 // Display account details in a fragment.
                 AccountDetailsFragment details = (AccountDetailsFragment) getSupportFragmentManager()
                         .findFragmentById(R.id.account_details);
                 if (details == null || details.getAccount().id != account.id) {
                     details = AccountDetailsFragment.newInstance(account);
                     
                     final FragmentTransaction ft = getSupportFragmentManager()
                             .beginTransaction();
                     ft.replace(R.id.account_details, details);
                     ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                     ft.commit();
                 }
             } else {
                 // Open account details with a new activity.
                 startActivity(new Intent(getActivity(), AccountDetails.class)
                         .putExtra(AccountDetails.EXTRA_ACCOUNT, account));
             }
         }
     }
     
     private void onActionRefresh() {
         // Start the synchronization service in background.
         getActivity().startService(syncServiceIntent);
         
         final AccountDetailsFragment details = (AccountDetailsFragment) getSupportFragmentManager()
                 .findFragmentById(R.id.account_details);
         if (details != null) {
             details.refresh();
         }
     }
     
     private void onActionAddAccount() {
         AddAccountDialogFragment.newInstance().show(
             getSupportFragmentManager(), "dialog");
     }
     
     private void onActionPreferences() {
         startActivity(new Intent(getActivity(), Preferences.class));
     }
     
     @Override
     public void onServiceConnected(ComponentName name, IBinder service) {
         // The service is now bound to this fragment: we can get its instance
         // and register as a listener.
         final SyncService.LocalBinder binder = (SyncService.LocalBinder) service;
         syncService = binder.getService();
         syncService.setListener(this);
     }
     
     @Override
     public void onServiceDisconnected(ComponentName name) {
         // The service is gone: make sure the listener is unset to prevent
         // memory leaks.
         syncService.setListener(null);
         syncService = null;
     }
     
     /**
      * Internal class for displaying {@link Account} instances.
      * @author Pixmob
      */
     private static class AccountAdapter extends ArrayAdapter<Account> {
         private final LayoutInflater layoutInflater;
         
         public AccountAdapter(final Context context) {
             super(context, android.R.layout.simple_list_item_2);
             layoutInflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
         
         /**
          * Set the account list to display. If <code>accounts</code> is
          * <code>null</code>, the list is cleared.
          */
         public void setData(List<Account> accounts) {
            setNotifyOnChange(false);
             clear();
             if (accounts != null) {
                 for (final Account account : accounts) {
                     add(account);
                 }
             }
            notifyDataSetChanged();
         }
         
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final View row;
             if (convertView == null) {
                 // Reusing the same view will save memory.
                 row = layoutInflater.inflate(R.layout.account_row, parent,
                     false);
             } else {
                 row = convertView;
             }
             
             final Account account = getItem(position);
             row.setTag(account);
             
             TextView ctv = (TextView) row.findViewById(R.id.account_name);
             if (account.name == null) {
                 ctv.setText(R.string.unknown_contract);
             } else {
                 ctv.setText(account.name);
             }
             
             ctv = (TextView) row.findViewById(R.id.account_number);
             if (account.phoneNumber == null) {
                 ctv.setText(R.string.unknown_phone_number);
             } else {
                 ctv.setText(account.phoneNumber);
             }
             
             ctv = (TextView) row.findViewById(R.id.account_status);
             if (account.status == 0) {
                 ctv.setText(R.string.unknown_status_step);
             } else {
                 ctv.setText(String.format(
                     getContext().getString(R.string.status_step),
                     account.status));
             }
             
             return row;
         }
     }
     
     /**
      * Background task for loading accounts.
      * @author Pixmob
      */
     private static class AccountListLoader extends
             AsyncTaskLoader<List<Account>> {
         private List<Account> accounts;
         
         public AccountListLoader(final Context context) {
             super(context);
         }
         
         @Override
         protected void onStartLoading() {
             super.onStartLoading();
             
             // Force account loading, since we do not keep track of account
             // updates.
             forceLoad();
         }
         
         @Override
         public List<Account> loadInBackground() {
             if (accounts != null) {
                 deliverResult(accounts);
             }
             
             if (DEBUG) {
                 Log.d(TAG, "Loading user accounts");
             }
             List<Account> newAccounts = Collections.emptyList();
             final AccountRepository accountRepository = new AccountRepository(
                     getContext());
             try {
                 newAccounts = accountRepository.list();
                 Collections.sort(newAccounts, AccountComparator.INSTANCE);
             } catch (Exception e) {
                 Log.e(TAG, "Account loading failed", e);
             }
             
             accounts = newAccounts;
             return newAccounts;
         }
     }
     
     /**
      * {@link Account} comparator.
      * @author Pixmob
      */
     private static class AccountComparator implements Comparator<Account> {
         public static final Comparator<Account> INSTANCE = new AccountComparator();
         
         @Override
         public int compare(Account a1, Account a2) {
             final String n1 = a1.phoneNumber;
             final String n2 = a2.phoneNumber;
             if (n1 == null) {
                 return 1;
             }
             if (n2 == null) {
                 return 0;
             }
             return n1.compareTo(n2);
         }
     }
     
     /**
      * Dialog for creating a new account.
      * @author Pixmob
      */
     public static class AddAccountDialogFragment extends DialogFragment {
         public static AddAccountDialogFragment newInstance() {
             return new AddAccountDialogFragment();
         }
         
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             final LayoutInflater inflater = LayoutInflater.from(getActivity());
             final View content = inflater.inflate(R.layout.add_account_dialog,
                 null);
             
             final TextView loginField = (TextView) content
                     .findViewById(R.id.account_login);
             final TextView passwordField = (TextView) content
                     .findViewById(R.id.account_password);
             
             return new AlertDialog.Builder(getActivity())
                     .setTitle(R.string.menu_add_account)
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setView(content)
                     .setPositiveButton(R.string.dialog_add,
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog,
                                     int which) {
                                 final String login = loginField.getText()
                                         .toString().trim();
                                 final String password = passwordField.getText()
                                         .toString().trim();
                                 new CreateAccountTask(getActivity()
                                         .getApplicationContext(), login,
                                         password).execute();
                             }
                         }).setNegativeButton(R.string.dialog_cancel, null)
                     .create();
         }
     }
     
     private static class CreateAccountTask extends AsyncTask<Void, Void, Void> {
         private final Context context;
         private final String login;
         private final String password;
         
         public CreateAccountTask(final Context context, final String login,
                 final String password) {
             this.context = context;
             this.login = login;
             this.password = password;
         }
         
         @Override
         protected Void doInBackground(Void... params) {
             final AccountRepository accountRepository = new AccountRepository(
                     context);
             accountRepository.create(login, password);
             return null;
         }
         
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             Toast.makeText(context, context.getString(R.string.adding_account),
                 Toast.LENGTH_SHORT).show();
             context.startService(new Intent(context, SyncService.class));
         }
     }
 }
