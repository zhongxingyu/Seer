 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.activities;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.adapters.ImmediateTransactionAdapter;
 import net.alexjf.tmm.adapters.TabAdapter;
 import net.alexjf.tmm.adapters.TabAdapter.OnTabFragmentCreateListener;
 import net.alexjf.tmm.domain.DatabaseHelper;
 import net.alexjf.tmm.domain.ImmediateTransaction;
 import net.alexjf.tmm.domain.MoneyNode;
 import net.alexjf.tmm.exceptions.DatabaseException;
 import net.alexjf.tmm.fragments.DateIntervalBarFragment;
 import net.alexjf.tmm.fragments.DateIntervalBarFragment.OnDateIntervalChangedListener;
 import net.alexjf.tmm.fragments.ImmedTransactionEditorFragment.ImmedTransactionEditOldInfo;
 import net.alexjf.tmm.fragments.ImmedTransactionListFragment;
 import net.alexjf.tmm.fragments.ImmedTransactionListFragment.OnImmedTransactionActionListener;
 import net.alexjf.tmm.fragments.ImmedTransactionStatsFragment;
 import net.alexjf.tmm.interfaces.IWithAdapter;
 import net.alexjf.tmm.utils.Utils;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MoneyNodeDetailsActivity extends SherlockFragmentActivity 
     implements OnDateIntervalChangedListener, OnImmedTransactionActionListener {
     private static final int REQCODE_ADD = 0;
     private static final int REQCODE_PREFS = 1;
 
     private MoneyNode currentMoneyNode;
     private ImmediateTransactionAdapter immedTransAdapter;
     private Date startDate;
     private Date endDate;
     private BigDecimal income;
     private BigDecimal expense;
     private String currency;
 
     private TextView balanceTextView;
     private TextView totalTransactionsTextView;
     private TextView incomeTextView;
     private TextView expenseTextView;
     private ViewPager viewPager;
 
     public MoneyNodeDetailsActivity() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         income = BigDecimal.valueOf(0);
         expense = BigDecimal.valueOf(0);
         setContentView(R.layout.activity_moneynode_details);
 
         Intent intent = getIntent();
         currentMoneyNode = (MoneyNode) intent.getParcelableExtra(
                 MoneyNode.KEY_MONEYNODE);
         currency = currentMoneyNode.getCurrency();
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         actionBar.setDisplayUseLogoEnabled(true);
 
         setTitle(currentMoneyNode.getName());
 
         balanceTextView = (TextView) findViewById(R.id.balance_value);
         incomeTextView = (TextView) findViewById(R.id.income_value);
         expenseTextView = (TextView) findViewById(R.id.expense_value);
         totalTransactionsTextView = (TextView) 
             findViewById(R.id.total_transactions_value);
         viewPager = (ViewPager) findViewById(R.id.view_pager);
 
         immedTransAdapter = new ImmediateTransactionAdapter(this);
 
         Bundle args = new Bundle();
         args.putString(MoneyNode.KEY_CURRENCY, currency);
 
         final TabAdapter tabAdapter = new TabAdapter(this, viewPager);
         tabAdapter.addTab(actionBar.newTab().setText(R.string.list),
                 ImmedTransactionListFragment.class, args);
         tabAdapter.addTab(actionBar.newTab().setText(R.string.stats),
                 ImmedTransactionStatsFragment.class, args);
 
         tabAdapter.setOnTabFragmentCreateListener(new OnTabFragmentCreateListener() {
             public void onTabFragmentCreated(Fragment fragment, int position) {
                 Log.d("TMM", "Tab fragment created");
                 IWithAdapter fragmentWithAdapter = 
                     (IWithAdapter) fragment;
                 fragmentWithAdapter.setAdapter(immedTransAdapter);
             }
         });
 
         DateIntervalBarFragment dateBar = (DateIntervalBarFragment) 
             getSupportFragmentManager().findFragmentById(R.id.dateinterval_bar);
         if (!dateBar.isAllTime()) {
             startDate = dateBar.getStartDate();
             endDate = dateBar.getEndDate();
         } else {
             startDate = null;
             endDate = null;
         }
 
         updateData();
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         DatabaseHelper.getInstance().close();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main_moneynode_details, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_addTransaction:
                 Intent intent = new Intent(this, 
                     ImmedTransactionEditActivity.class);
                 intent.putExtra(MoneyNode.KEY_MONEYNODE, currentMoneyNode);
                 startActivityForResult(intent, REQCODE_ADD);
                 return true;
             case R.id.menu_preferences:
                 intent = new Intent(this,
                     PreferencesActivity.class);
                 startActivityForResult(intent, REQCODE_PREFS);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, 
             Intent data) {
         if (resultCode == Activity.RESULT_OK) {
             switch (requestCode) {
                 case REQCODE_ADD:
                     ImmediateTransaction transaction = (ImmediateTransaction) 
                         data.getParcelableExtra(ImmediateTransaction.KEY_TRANSACTION);
                     immedTransAdapter.add(transaction);
 
                     try {
                         transaction.load();
                         BigDecimal value = transaction.getValue();
                         switch(value.signum()) {
                             case 1:
                                 income = income.add(value);
                                 break;
                             case -1:
                                 expense = expense.add(value);
                                 break;
                         }
                     } catch (DatabaseException e) {
                         Log.e("TMM", "Error loading transaction after adding.", e);
                     }
 
                     break;
                 case REQCODE_PREFS:
                     if (data.getBooleanExtra(
                                 PreferencesActivity.KEY_FORCEDATAREFRESH,
                                 false)) {
                         updateData();
                     }
                     break;
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     public void onDateIntervalChanged(Date startDate, Date endDate) {
         this.startDate = startDate;
         this.endDate = endDate;
 
         if (startDate != null) {
             Log.d("TMM", "Selected new date interval: " + startDate.toString() + "-" + endDate.toString());
         } else {
             Log.d("TMM", "Selected new date interval: all time");
         }
 
         updateData();
         updateGui();
     }
 
     private void updateData() {
         List<ImmediateTransaction> immediateTransactions = null;
         try {
             immediateTransactions = currentMoneyNode.getImmediateTransactions(
                     startDate, endDate);
             immedTransAdapter.clear();
 
             income = BigDecimal.valueOf(0);
             expense = BigDecimal.valueOf(0);
             for (ImmediateTransaction transaction : immediateTransactions) {
                 try {
                     transaction.load();
                 } catch (DatabaseException e) {
                     Log.e("TMM", "Error loading transaction " + transaction.getId(), e);
                     continue;
                 }
                 BigDecimal value = transaction.getValue();
 
                 if (value.signum() > 0) {
                     income = income.add(value);
                 } else {
                     expense = expense.add(value);
                 }
 
                 immedTransAdapter.add(transaction);
             }
         } catch (DatabaseException e) {
             Log.e("TMM", "Unable to get immediate transactions", e);
         }
     }
 
     private void updateGui() {
         immedTransAdapter.notifyDataSetChanged();
         immedTransAdapter.sort(new ImmediateTransaction.DateComparator(true));
         updateDetailsPanel();
     }
 
     private void updateDetailsPanel() {
         BigDecimal balance = income.add(expense);
 
         // If we are not limiting the start date or it is less than or equal to
         // money node creationDate, add initialBalance
         if (startDate == null || 
             startDate.compareTo(currentMoneyNode.getCreationDate()) <= 0) {
             BigDecimal initialBalance = currentMoneyNode.getInitialBalance();
             balance = balance.add(initialBalance);
             balanceTextView.setText(balance + " " + currency + 
                    "\n(Init. bal: " + initialBalance + " " + currency + ")");
         } else {
             balanceTextView.setText(balance + " " + currency);
         }
 
         totalTransactionsTextView.setText(Integer.toString(immedTransAdapter.getCount()));
         incomeTextView.setText(income + " " + currency);
         expenseTextView.setText(expense + " " + currency);
     }
 
     @Override
     public void onImmedTransactionRemoved(ImmediateTransaction transaction) {
         BigDecimal value = transaction.getValue();
         switch (value.signum()) {
             case 1:
                 income = income.subtract(value);
                 break;
             case -1:
                 expense = expense.subtract(value);
                 break;
         }
     }
 
     @Override
     public void onImmedTransactionEdited(ImmediateTransaction transaction,
             ImmedTransactionEditOldInfo oldInfo) {
         BigDecimal originalValue = oldInfo.getValue();
         BigDecimal newValue = transaction.getValue();
         BigDecimal delta = newValue.subtract(originalValue);
         BigDecimal incomeDelta = BigDecimal.valueOf(0);
         BigDecimal expenseDelta = BigDecimal.valueOf(0);
 
         // If immediate transaction is no longer on the period being considered,
         // remove it
         if (!Utils.dateBetween(transaction.getExecutionDate(), 
                     startDate, endDate)) {
             immedTransAdapter.remove(transaction);
             onImmedTransactionRemoved(transaction);
             return;
         }
 
         // If values didn't change, quit
         if (delta.signum() == 0) {
             return;
         }
 
         // If the 2 values share the same signal, we only need to modify either
         // the income or expense
         if (originalValue.signum() == newValue.signum()) {
             switch (originalValue.signum()) {
                 // Case bigger than 0
                 case 1:
                     incomeDelta = incomeDelta.add(delta);
                     break;
                 // Case smaller than 0
                 case -1:
                     expenseDelta = expenseDelta.add(delta);
                     break;
             }
         } 
         // Else we need to modify both income and expense
         else {
             switch (originalValue.signum()) {
                 // Case bigger than 0
                 case 1:
                     incomeDelta = incomeDelta.subtract(originalValue);
                     expenseDelta = expenseDelta.add(newValue);
                     break;
 
                 // Case smaller than 0
                 case -1:
                     expenseDelta = expenseDelta.subtract(originalValue);
                     incomeDelta = incomeDelta.add(newValue);
                     break;
             }
         }
 
         income = income.add(incomeDelta);
         expense = expense.add(expenseDelta);
     }
 
     @Override
     public void onImmedTransactionSelected(ImmediateTransaction transaction) {
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         updateGui();
     }
 }
