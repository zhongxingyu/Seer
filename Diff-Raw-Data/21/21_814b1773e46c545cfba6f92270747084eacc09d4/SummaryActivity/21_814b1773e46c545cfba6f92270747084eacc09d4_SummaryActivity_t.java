 package org.measureit.androet;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
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
 import java.util.List;
 import org.measureit.androet.db.Account;
 import org.measureit.androet.db.Transaction;
 import org.measureit.androet.ui.ActivitySwitch;
 import org.measureit.androet.ui.TextViewBuilder;
 import org.measureit.androet.util.Constants;
 import org.measureit.androet.util.Helper;
 
//TODO: replace context menu with gesture

 /**
  *
  * @author ezsi
  */
 public class SummaryActivity extends Activity {
     private final ArrayList<Transaction> listItems = new ArrayList<Transaction>();
     private ListView listView;
     private ArrayAdapter listAdapter;
     private Account account;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         listView = new ListView(this);
         listView.setBackgroundColor(Color.WHITE);
         listView.setCacheColorHint(Color.WHITE);
         registerForContextMenu(listView);
         setContentView(listView);
         listAdapter =  new TransactionAdapter(this,android.R.layout.simple_list_item_1 , listItems);
         listView.setOnItemClickListener(new OnItemClickListener() {
 
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ActivitySwitch.to(SummaryActivity.this, TransactionsActivity.class)
                         .add("account", account)
                         .add("transaction", listItems.get(position))
                         .execute();
             }
         });
         listView.setAdapter(listAdapter);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         int listItemIndex = (int) ((AdapterContextMenuInfo)item.getMenuInfo()).id;
         Transaction selectedTransaction = (Transaction) listView.getItemAtPosition(listItemIndex);
         
         final String itemTitle = item.toString();
         if(Constants.TRANSACTION_ADD.equals(itemTitle)){
             ActivitySwitch activitySwitch = ActivitySwitch.to(this, TransactionActivity.class).add("accountId", account.getId());
             if(selectedTransaction.getCategory() != null) // it's a real category and not a group
                 activitySwitch = activitySwitch.add("categoryId", selectedTransaction.getCategory().getId());
             activitySwitch.execute();
         }
         return super.onContextItemSelected(item);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         menu.add(Constants.TRANSACTION_ADD);
         super.onCreateContextMenu(menu, v, menuInfo);
     }
     
     @Override
     protected void onResume() {
         account = (Account) this.getIntent().getSerializableExtra("account");
         setTitle(account.getName() + " Summary");
         refreshTransactionList();
         super.onResume();
     }
         
     private void refreshTransactionList(){
         listItems.clear();       
         List<Transaction> transactions = Transaction.sumByCategory(account);
         int year = 0;
         int month = 0;
         Transaction groupTransaction = null;
         double sum = 0;
         for(Transaction tr : transactions){
             if(tr.getYear() != year || tr.getMonth() != month){
                 if(groupTransaction != null)
                     groupTransaction.setAmount(sum);
                 sum = 0;
                 year = tr.getYear();
                 month = tr.getMonth();
                 groupTransaction = new Transaction(-1, tr.getAccountId(), null, 0, "", null, year, month);
                 listItems.add(groupTransaction);
             }
             sum += tr.getAmount();
             listItems.add(tr);
         }
         if(groupTransaction != null)
             groupTransaction.setAmount(sum);
         listAdapter.notifyDataSetChanged();
     }
     
     private class TransactionAdapter extends ArrayAdapter<Transaction> {
 
         private ArrayList<Transaction> items;
 
         public TransactionAdapter(Context context, int textViewResourceId, ArrayList<Transaction> items) {
                 super(context, textViewResourceId, items);
                 this.items = items;
         }
         
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             final TableRow.LayoutParams cellLp = new TableRow.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 1.0f);
             cellLp.setMargins(2, 2, 2, 2);
             
             final TableLayout.LayoutParams rowParams = new TableLayout
                     .LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
             rowParams.setMargins(4, 2, 6, 2);
             
             Transaction tr = items.get(position);
             TableLayout tableLayout = new TableLayout(SummaryActivity.this);
             
             TableRow row1 = new TableRow(SummaryActivity.this);
             row1.setLayoutParams(rowParams);
             String text = "";
             int textColor = Constants.HEADER_TEXT_COLOR;
             if(tr.getCategory() == null){
                 Calendar date = Helper.resetDate(Calendar.getInstance());
                 date.set(Calendar.YEAR, tr.getYear());
                 date.set(Calendar.MONTH, tr.getMonth());
                 text = DateFormat.format("yyyy. MMMM ", date).toString(); 
                 textColor = Constants.HIGHLIGHT_COLOR;
             }else
                 text = "  "+tr.getCategory().getName();
             row1.addView(TextViewBuilder.text(SummaryActivity.this, text).gravity(Gravity.BOTTOM)
                     .size(Constants.HEADER_TEXT_SIZE).color(textColor).build());
             
             TextView amountTextView = TextViewBuilder.text(SummaryActivity.this, account.getCurrency().getSymbol()
                     +" "+tr.getAmount()).size(Constants.TEXT_SIZE+2).color(textColor)
                     .gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT).build();
             amountTextView.setLayoutParams(cellLp);
             row1.addView(amountTextView);
              
             TableRow row2 = new TableRow(SummaryActivity.this);
             row2.setLayoutParams(rowParams);
             row2.addView(TextViewBuilder.text(SummaryActivity.this, "   "+tr.getDescription())
                     .size(Constants.TEXT_SIZE).color(Color.LTGRAY).build());
             
             tableLayout.addView(row1);
             tableLayout.addView(row2);
             return tableLayout;
         }
     }
 }
