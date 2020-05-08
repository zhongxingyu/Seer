 package org.measureit.androet;
 
 import org.measureit.androet.util.Constants;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.Calendar;
 import org.measureit.androet.db.Account;
 import org.measureit.androet.db.Backup;
 import org.measureit.androet.db.Transaction;
 import org.measureit.androet.ui.ActivitySwitch;
 import org.measureit.androet.ui.TextViewBuilder;
 
 // TODO: add currency conversion
 // TODO: add PIN protection
// TODO: fix expense calculation
 
 public class AccountsActivity extends Activity{
     private final ArrayList<Account> listItems = new ArrayList<Account>();
     private ListView listView;
     private ArrayAdapter listAdapter;    
     private static Context context;
     private Account selectedAccount;
     private DialogInterface.OnClickListener confirmDialogClickListener = new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which) {
             if(which == DialogInterface.BUTTON_POSITIVE){
                 if(!selectedAccount.isGroup()) // only non group accounts have real trasactions
                     Transaction.delete(selectedAccount);
                 Account.delete(selectedAccount);
                 refreshAccountList();
             }
         }
     };
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         context = getApplicationContext();
         listView = new ListView(this);
         listView.setBackgroundColor(Color.WHITE);
         listView.setCacheColorHint(Color.WHITE);
         registerForContextMenu(listView);
         listView.setOnItemClickListener(new OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ActivitySwitch.to(AccountsActivity.this, SummaryActivity.class).add("account", listItems.get(position)).execute();
             }
         });
         setContentView(listView);
         listAdapter =  new AccountAdapter(this,android.R.layout.simple_list_item_1 , listItems);
         listView.setAdapter(listAdapter);
     }
     
     public static Context getAppContext(){
         return context;
     }
         
     @Override
     protected void onResume() {
         refreshAccountList();
         super.onResume();
     }
 
     private void refreshAccountList(){
         listItems.clear(); 
         listItems.addAll(Account.list());
         if(listItems.isEmpty())
             listItems.add(new Account(0, "Tap me long!", 0, 0, "EUR", false));
         listAdapter.notifyDataSetChanged();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         final String itemTitle = item.toString();
         if(Constants.ACCOUNT_SAVE_DB.equals(itemTitle))
             Backup.save();
         else if(Constants.ACCOUNT_LOAD_DB.equals(itemTitle))
             Backup.load();
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Constants.ACCOUNT_SAVE_DB);
         menu.add(Constants.ACCOUNT_LOAD_DB);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         int listItemIndex = (int) ((AdapterContextMenuInfo)item.getMenuInfo()).id;
         selectedAccount = (Account) listView.getItemAtPosition(listItemIndex);
         
         final String itemTitle = item.toString();
         if(Constants.TRANSACTION_ADD.equals(itemTitle))
             ActivitySwitch.to(this, TransactionActivity.class).add("accountId", selectedAccount.getId()).execute();
         else if(Constants.ACCOUNT_CREATE_GROUP.equals(itemTitle))
             ActivitySwitch.to(AccountsActivity.this, GroupActivity.class).execute();
         else if(Constants.ACCOUNT_CREATE.equals(itemTitle))
             ActivitySwitch.to(AccountsActivity.this, AccountActivity.class).execute();
         else if(Constants.ACCOUNT_EDIT.equals(itemTitle)){
             if(selectedAccount.isGroup())
                 ActivitySwitch.to(AccountsActivity.this, GroupActivity.class).add("account", selectedAccount).execute();
             else
                 ActivitySwitch.to(AccountsActivity.this, AccountActivity.class).add("account", selectedAccount).execute();
         }else if(Constants.ACCOUNT_DELETE.equals(itemTitle)){
             (new AlertDialog.Builder(this))
                 .setMessage("Are you sure you want to delete \""+selectedAccount.getName()+"\" account?").setPositiveButton("Yes", confirmDialogClickListener)
                 .setNegativeButton("No", confirmDialogClickListener).show();
         }else if(Constants.ACCOUNT_TRANSFER_MONEY.equals(itemTitle))
             ActivitySwitch.to(AccountsActivity.this, TransferActivity.class).add("account", selectedAccount).execute();
         return super.onContextItemSelected(item);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         menu.add(Constants.TRANSACTION_ADD);
         menu.add(Constants.ACCOUNT_CREATE);
         menu.add(Constants.ACCOUNT_CREATE_GROUP);
         menu.add(Constants.ACCOUNT_EDIT);        
         menu.add(Constants.ACCOUNT_DELETE);
         menu.add(Constants.ACCOUNT_TRANSFER_MONEY);
         super.onCreateContextMenu(menu, v, menuInfo);
     }
     
     
     private class AccountAdapter extends ArrayAdapter<Account> {
         private ArrayList<Account> items;
 
         public AccountAdapter(Context context, int textViewResourceId, ArrayList<Account> items) {
                 super(context, textViewResourceId, items);
                 this.items = items;
         }
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
             cellParams.setMargins(2, 2, 2, 2);
             
             final TableLayout.LayoutParams rowParams = new TableLayout
                     .LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
             rowParams.setMargins(4, 2, 6, 2);
             
             Account account = items.get(position);
             TableLayout tableLayout = new TableLayout(AccountsActivity.this);
             final int textColor = account.isGroup() ? Constants.HIGHLIGHT_COLOR : Constants.HEADER_TEXT_COLOR;            
             TableRow row1 = new TableRow(AccountsActivity.this);
             row1.setLayoutParams(rowParams);
             row1.addView(TextViewBuilder.text(AccountsActivity.this, account.getName())
                     .size(Constants.HEADER_TEXT_SIZE).color(textColor).build());
             TextView amountTextView = TextViewBuilder.text(AccountsActivity.this, account.getCurrency().getSymbol()
                     +" "+account.getBalance()).size(Constants.TEXT_SIZE+2)
                     .gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT).color(textColor).build();
             amountTextView.setLayoutParams(cellParams);
             row1.addView(amountTextView);
 
             TableRow row2 = new TableRow(AccountsActivity.this);
             row2.setLayoutParams(rowParams);
             double expense = Transaction.sum(account, Calendar.getInstance().get(Calendar.MONTH)+1);
             StringBuilder expenseSb = new StringBuilder();
             if(expense < 0)
                 expenseSb.append(Constants.ACCOUNT_EXPENSE).append(Double.toString(Math.abs(expense)));
             else if (expense > 0)
                 expenseSb.append(Constants.ACCOUNT_INCOME).append(Double.toString(Math.abs(expense)));
             
             int expenseColor = Constants.TEXT_COLOR;
             if(expense < 0 && account.getBudget() > 0){
                 double availableBudget = account.getBudget()+expense;
                 expenseSb.append(" / ").append(Double.toString(availableBudget));
                 if(availableBudget < 0)
                     expenseColor = Constants.WARNING_COLOR;
             }
             row2.addView(TextViewBuilder.text(AccountsActivity.this, expenseSb.toString()).color(expenseColor).build());
             
             tableLayout.addView(row1);
             tableLayout.addView(row2);
             return tableLayout;
         }
     }
     
 }
