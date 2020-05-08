 package com.rrs_apps.android.share_to_irc.account;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.googlecode.androidannotations.annotations.EFragment;
 
 /**
  * IrcAccountListFragment displays a list of all Share To IRC accounts.
  */
 @EFragment
 public class IrcAccountListFragment extends SherlockListFragment {
     public interface Listener {
         void accountClicked(Account acct);
     }
 
     /**
      * Simple Account subclass to change the behavior of toString()
      */
     class IrcAccount extends Account {
         public IrcAccount(Account acct) {
             super(acct.name, acct.type);
         }
 
         @Override
         public String toString() {
             return name;
         }
     }
 
     private ArrayAdapter<Account> mAdapter;
     private Listener listener;
 
     @Override
     public void onStart() {
         super.onStart();
 
         if (mAdapter == null) {
             mAdapter = new ArrayAdapter<Account>(getActivity(),
                     android.R.layout.simple_list_item_single_choice);
 
             setListAdapter(mAdapter);
         }
 
         reloadAccounts();
     }
 
     public void reloadAccounts() {
         mAdapter.clear();
 
         Account[] accounts = AccountManager.get(getActivity()).getAccountsByType(
                 IrcAccountHandler.ACCOUNT_TYPE_SHARE_TO_IRC);
 
         for (Account acct : accounts) {
             mAdapter.add(new IrcAccount(acct));
         }
 
        clearSelection();

        mAdapter.notifyDataSetChanged();
    }

    public void clearSelection() {
        for (int i = 0; i < getListView().getCount(); i++) {
            getListView().setItemChecked(i, false);
        }
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         if (listener != null) {
             listener.accountClicked((Account) l.getItemAtPosition(position));
         }
     }
 
     public void setListener(Listener listener) {
         this.listener = listener;
     }
 
     public Account getSelectedAccount() {
         if (getListView().getCheckedItemPosition() == ListView.INVALID_POSITION) {
             return null;
         }
 
         return mAdapter.getItem(getListView().getCheckedItemPosition());
     }
 
     public List<Account> getSelectedAccounts() {
         ArrayList<Account> selectedAccounts = new ArrayList<Account>();
 
         SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();
 
         for (int i = 0; i < checkedItems.size(); i++) {
             selectedAccounts.add(mAdapter.getItem(checkedItems.keyAt(i)));
         }
 
         return selectedAccounts;
     }
 }
