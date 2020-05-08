 package com.rrs_apps.android.share_to_irc.account;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AccountManagerCallback;
 import android.accounts.AccountManagerFuture;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.FragmentById;
 import com.googlecode.androidannotations.annotations.OnActivityResult;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.OptionsMenu;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.rrs_apps.android.share_to_irc.R;
 import com.rrs_apps.android.share_to_irc.account.IrcAccountListFragment.Listener;
 
 /**
  * ListIrcAccountsActivity displays a list of Share To IRC accounts and allows the user to edit and add accounts.
  */
 @EActivity(R.layout.list_irc_accounts_activity)
 @OptionsMenu({ R.menu.list_irc_accounts_activity_menu, R.menu.delete_account })
 public class ListIrcAccountsActivity extends SherlockFragmentActivity implements Listener,
         com.rrs_apps.android.share_to_irc.account.IrcAccountEditorFragment.Listener {
     private class AccountLongClickListener implements OnItemLongClickListener {
         @Override
         public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            if (mActionMode == null && (editFragment == null || !editFragment.isInLayout())) {
                 listFragment.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
                 mActionMode = startActionMode(new ActionMode.Callback() {
                     @Override
                     public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                         mode.setTitle(R.string.select_accounts);
 
                         return true;
                     }
 
                     @Override
                     public void onDestroyActionMode(ActionMode mode) {
                         mActionMode = null;
 
                         listFragment.reloadAccounts();

                        listFragment.getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
                     }
 
                     @Override
                     public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                         mode.getMenuInflater().inflate(R.menu.delete_account, menu);
 
                         return true;
                     }
 
                     @Override
                     public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                         if (item.getItemId() == R.id.delete_account) {
                             // Delete account
                             for (Account acct : listFragment.getSelectedAccounts()) {
                                 deleteAccount(acct);
                             }
 
                             mode.finish();
                         }
 
                         return true;
                     }
                 });
 
                 listFragment.getListView().setItemChecked(position, true);
             }
 
             return true;
         }
     }
 
     private static final int REQ_CODE_CREATE_ACCOUNT = 0;
 
     @FragmentById(R.id.irc_account_list_fragment)
     IrcAccountListFragment listFragment;
 
     @FragmentById(R.id.irc_account_editor_fragment)
     IrcAccountEditorFragment editFragment;
 
     @ViewById(R.id.no_account_selected)
     TextView noAccountSelectedText;
 
     private ActionMode mActionMode;
 
     @Override
     protected void onResume() {
         super.onResume();
 
         // Send user to account creation if there are no accounts
         Account[] accounts = AccountManager.get(this).getAccountsByType(
                 IrcAccountHandler.ACCOUNT_TYPE_SHARE_TO_IRC);
 
         if (accounts.length == 0) {
             startActivityForResult(new Intent(this, CreateIrcAccountActivity_.class), REQ_CODE_CREATE_ACCOUNT);
         }
     }
 
     @OnActivityResult(REQ_CODE_CREATE_ACCOUNT)
     void finishIfNoAccounts(Intent data) {
         if (data == null) {
             // Account creator was launched, but the user canceled
             finish();
         }
     }
 
     @AfterViews
     void onLoad() {
         // Don't allow selection by default
         listFragment.getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
 
         listFragment.setListener(this);
 
         listFragment.getListView().setOnItemLongClickListener(new AccountLongClickListener());
 
         if (editFragment != null) {
             editFragment.setListener(this);
 
             // Nothing should be selected at this point; hide the editor
             hideEditor();
 
             if (editFragment.isInLayout()) {
                 // Allow selection on list fragment
                 listFragment.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
             }
         }
     }
 
     private void showEditor() {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         ft.show(editFragment);
         ft.commit();
 
         if (noAccountSelectedText != null) {
             noAccountSelectedText.setVisibility(View.GONE);
         }
     }
 
     private void hideEditor() {
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         ft.hide(editFragment);
         ft.commit();
 
         if (noAccountSelectedText != null) {
             noAccountSelectedText.setVisibility(View.VISIBLE);
         }
     }
 
     @Override
     public void accountClicked(Account acct) {
         // Let user edit the account
         if (editFragment != null && editFragment.isInLayout()) {
             editFragment.loadAccount(acct);
 
             // Show the editor
             showEditor();
 
             supportInvalidateOptionsMenu();
         }
         else if (mActionMode == null) {
             // Launch separate editor activity
             startActivity(new Intent(this, EditIrcAccountActivity_.class).putExtra(
                     IrcAccountHandler.ACCOUNT_TYPE_SHARE_TO_IRC, acct));
         }
     }
 
     @Override
     public void onSave(IrcAccountEditorFragment editFragment) {
         Account acct = listFragment.getSelectedAccount();
         editFragment.saveAccount(acct);
 
         Toast.makeText(this, R.string.account_updated, Toast.LENGTH_LONG).show();
     }
 
     @OptionsItem
     void addAccount() {
         startActivity(new Intent(this, CreateIrcAccountActivity_.class));
     }
 
     @OptionsItem
     void deleteAccount() {
         // Show a confirmation before deleting the selected account
         createDeleteConfirmationDialog().show();
     }
 
     private AlertDialog createDeleteConfirmationDialog() {
         return new AlertDialog.Builder(this)
                 .setTitle(R.string.delete_account)
                 .setIcon(android.R.drawable.ic_menu_delete)
                 .setNegativeButton(android.R.string.no, new OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 })
                 .setPositiveButton(android.R.string.yes, new OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         deleteAccount(listFragment.getSelectedAccount());
 
                         // Selected account has been deleted; hide the editor
                         hideEditor();
                     }
                 })
                 .setMessage(
                         String.format(getResources().getString(R.string.delete_account_confirmation),
                                 listFragment.getSelectedAccount().toString())).create();
     }
 
     private void deleteAccount(Account acct) {
         AccountManager.get(ListIrcAccountsActivity.this).removeAccount(acct,
                 new AccountManagerCallback<Boolean>() {
                     @Override
                     public void run(AccountManagerFuture<Boolean> future) {
                         // Refresh the account list after the account is deleted
                         while (!future.isDone() && !future.isCancelled())
                             ;
 
                         listFragment.reloadAccounts();
                     }
                 }, null);
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // Show/hide delete action
         if (listFragment != null && listFragment.isInLayout() && listFragment.getSelectedAccount() != null) {
             menu.findItem(R.id.delete_account).setVisible(true);
             menu.findItem(R.id.delete_account).setEnabled(true);
         }
         else {
             menu.findItem(R.id.delete_account).setVisible(false);
             menu.findItem(R.id.delete_account).setEnabled(false);
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 }
