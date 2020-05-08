 package org.barbon.acash;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.database.Cursor;
 
 import android.os.Bundle;
 
 import android.text.format.DateFormat;
 
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import android.widget.BaseAdapter;
 import android.widget.CursorAdapter;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import java.util.Date;
 
 public class NewExpense extends Activity {
     private EditText transferAmount, expenseDescription;
 
     private TextView transferDateView;
     private Date transferDate;
 
     private static final int DATE_DIALOG_ID = 0;
 
     // set the transaction date
     private DatePickerDialog.OnDateSetListener dateSet =
         new DatePickerDialog.OnDateSetListener() {
             public void onDateSet(DatePicker view, int year,
                                   int month, int day) {
                 setTransferDate(year, month, day);
             }
         };
 
     // reset the dialog date when the user closes it
     private DialogInterface.OnDismissListener dateDismissed =
         new DialogInterface.OnDismissListener() {
             public void onDismiss(DialogInterface dialog) {
                 removeDialog(DATE_DIALOG_ID);
             }
         };
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newexpense);
 
         ExpenseDatabase db = ExpenseDatabase.getInstance(this);
 
         setAccountData(R.id.from_account, db.getFromAccountList());
         setAccountData(R.id.to_account, db.getToAccountList());
 
         transferDateView = (TextView) findViewById(R.id.transfer_date);
         transferAmount = (EditText) findViewById(R.id.transfer_amount);
         expenseDescription = (EditText) findViewById(R.id.expense_description);
 
         setTransferDate(new Date());
         transferAmount.setText("");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = new MenuInflater(this);
 
         inflater.inflate(R.menu.newtransaction, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.new_account:
             addNewAccount();
 
             return true;
         case R.id.expense_list:
             showExpenseList();
 
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DATE_DIALOG_ID:
             DatePickerDialog dateDialog =
                 new DatePickerDialog(this, dateSet,
                                      transferDate.getYear() + 1900,
                                      transferDate.getMonth(),
                                     transferDate.getDate());
 
             dateDialog.setOnDismissListener(dateDismissed);
 
             return dateDialog;
         }
 
         return null;
     }
 
     private void setTransferDate(Date date) {
         transferDate = date;
 
         java.text.DateFormat dateFormat =
             DateFormat.getDateFormat(getApplicationContext());
 
         transferDateView.setText(dateFormat.format(transferDate));
     }
 
     private void setTransferDate(int year, int month, int day) {
         setTransferDate(new Date(year - 1900, month, day));
     }
 
     private void setAccountData(int id, Cursor data) {
         Spinner spinner = (Spinner) findViewById(id);
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(
             this, android.R.layout.simple_spinner_item, data,
             new String[] { ExpenseDatabase.ACCOUNT_DESCRIPTION_COLUMN },
             new int[] { android.R.id.text1 });
         adapter.setDropDownViewResource(
             android.R.layout.simple_spinner_dropdown_item);
 
         startManagingCursor(data); // TODO deprecated
 
         spinner.setAdapter(adapter);
     }
 
     private int getAccountId(int id) {
         Spinner spinner = (Spinner) findViewById(id);
 
         return (int) spinner.getSelectedItemId();
     }
 
     private void addNewAccount() {
         Intent intent = new Intent("org.barbon.acash.NEW_ACCOUNT");
 
         startActivity(intent);
     }
 
     private void showExpenseList() {
         Intent intent = new Intent("org.barbon.acash.EXPENSE_LIST");
 
         startActivity(intent);
     }
 
     // event handlers
 
     public void onDateViewClicked(View v) {
         // show date picker when clicking on the transfer date
         showDialog(DATE_DIALOG_ID);
     }
 
     public void onAddExpense(View v) {
         // add a new expense
         ExpenseDatabase db = ExpenseDatabase.getInstance(this);
 
         double amount = Double.parseDouble(transferAmount.getText().toString());
         String description = expenseDescription.getText().toString();
 
         db.insertExpense(getAccountId(R.id.from_account),
                          getAccountId(R.id.to_account),
                          amount, transferDate, description);
     }
 }
