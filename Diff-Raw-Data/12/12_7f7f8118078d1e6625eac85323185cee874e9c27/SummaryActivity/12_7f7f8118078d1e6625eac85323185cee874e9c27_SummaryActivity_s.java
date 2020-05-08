 package org.measureit.androet;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
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
 import org.measureit.androet.util.SummaryTransaction;
 
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
         listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 
             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                 if(account.isGroup())
                     return true;
                 Transaction selectedTransaction = listItems.get(position);
                 Log.e(Constants.LOG_NAME, selectedTransaction.toString());
                 ActivitySwitch activitySwitch = ActivitySwitch.to(SummaryActivity.this, TransactionActivity.class)
                         .add("accountId", account.getId());
                 if(selectedTransaction.getCategory() != null) // it's a real category and not a group
                     activitySwitch = activitySwitch.add("categoryId", selectedTransaction.getCategory().getId());
                 activitySwitch.execute();
                 return true;
             }
         });
         listView.setAdapter(listAdapter);
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
         SummaryTransaction summaryTransaction = null;
         double expense = 0;
         double income = 0;
         
         for(Transaction tr : transactions){
             if(tr.getYear() != year || tr.getMonth() != month){
                 if(summaryTransaction != null)
                     setSummary(summaryTransaction, expense, income);
                 expense = 0;
                 income = 0;
                 year = tr.getYear();
                 month = tr.getMonth();
                 summaryTransaction = new SummaryTransaction(tr.getAccountId(), year, month);
                 listItems.add(summaryTransaction);
             }
            if(tr.isTransfer())
                continue;
            if(tr.getCategory().isExpense())
                expense += tr.getAmount();
            else
                income += tr.getAmount();            
             listItems.add(tr);
         }
         if(summaryTransaction != null)
             setSummary(summaryTransaction, expense, income);
         listAdapter.notifyDataSetChanged();
     }
     
     private void setSummary(SummaryTransaction summaryTransaction, double expense, double income){
         summaryTransaction.setExpense(expense);
         summaryTransaction.setIncome(income);
         summaryTransaction.setAmount(income + expense);
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
             cellLp.setMargins(2, 0, 2, 0);
             
             final TableLayout.LayoutParams rowParams = new TableLayout
                     .LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT);
             rowParams.setMargins(4, 2, 6, 2);
             
             Transaction tr = items.get(position);
             TableLayout tableLayout = new TableLayout(SummaryActivity.this);
             
             TableRow row1 = new TableRow(SummaryActivity.this);
             row1.setLayoutParams(rowParams);
             String text = "";
             int textColor = Constants.HEADER_TEXT_COLOR;
             TableRow row2 = new TableRow(SummaryActivity.this);
             row2.setLayoutParams(rowParams);
             if(tr instanceof SummaryTransaction){
                 SummaryTransaction summaryTransaction = (SummaryTransaction) tr;
                 Calendar date = Helper.resetDate(Calendar.getInstance());
                 date.set(Calendar.YEAR, tr.getYear());
                 date.set(Calendar.MONTH, tr.getMonth());
                 text = DateFormat.format("yyyy. MMMM ", date).toString(); 
                 textColor = Constants.HIGHLIGHT_COLOR;
                 TextView amountTextView = TextViewBuilder.text(SummaryActivity.this, 
                     String.format("Expense: %s %.2f", account.getCurrency().getSymbol(), -1 * summaryTransaction.getExpense()))
                     .size(Constants.TEXT_SIZE+2).color(textColor)
                     .gravity(Gravity.CENTER_VERTICAL | Gravity.LEFT).build();
                 
                 row2.addView(amountTextView);
                 amountTextView = TextViewBuilder.text(SummaryActivity.this, 
                     String.format("Income: %s %.2f", account.getCurrency().getSymbol(), summaryTransaction.getIncome()))
                     .size(Constants.TEXT_SIZE+2).color(textColor)
                     .gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT).build();
                 amountTextView.setLayoutParams(cellLp);
                 row2.addView(amountTextView);
             }else
                 text = "  "+tr.getCategory().getName();
             row1.addView(TextViewBuilder.text(SummaryActivity.this, text).gravity(Gravity.BOTTOM)
                     .size(Constants.HEADER_TEXT_SIZE).color(textColor).build());
             
             TextView amountTextView = TextViewBuilder.text(SummaryActivity.this, 
                     String.format("%s %.2f", account.getCurrency().getSymbol(), tr.getAmount()))
                     .size(Constants.TEXT_SIZE+2).color(textColor)
                     .gravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT).build();
             amountTextView.setLayoutParams(cellLp);
             row1.addView(amountTextView);
              
             
 //            row2.addView(TextViewBuilder.text(SummaryActivity.this, "   "+tr.getDescription())
 //                    .size(Constants.TEXT_SIZE).color(Color.LTGRAY).build());
             
             tableLayout.addView(row1);
             tableLayout.addView(row2);
             return tableLayout;
         }
     }
 }
