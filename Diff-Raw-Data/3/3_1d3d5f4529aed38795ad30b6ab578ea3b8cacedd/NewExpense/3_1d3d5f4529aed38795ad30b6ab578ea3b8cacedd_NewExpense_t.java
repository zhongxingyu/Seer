 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.acash;
 
 import android.app.Dialog;
 
 import android.content.DialogInterface;
 
 import android.database.Cursor;
 
 import android.os.Bundle;
 
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 
 import android.widget.DatePicker;
 
 import java.util.Date;
 
 public class NewExpense extends ExpenseEdit {
     private static final int ABOUT_DIALOG = 1;
 
     private static boolean redirectedToNewAccount;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.newexpense);
 
         expenseView = (ExpenseView) findViewById(R.id.expense_view);
         actionButton = findViewById(R.id.add_expense);
 
         expenseView.setOnContentChangedListener(onExpenseChanged);
 
         contentModified = false;
 
         // enable/disable button when the activity is created
         actionButton.setEnabled(expenseView.isValidExpense());
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         // first-time only about dialog
         if (AboutDialog.showFirstTime(this))
             showDialog(ABOUT_DIALOG);
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         // go to the 'new account' activity if < 2 accounts and the
         // about dialog is not shown
         if (!AboutDialog.showFirstTime(this))
             showNewAccountIfNeeded();
     }
 
     @Override
     protected Dialog onCreateDialog(int id, Bundle bundle) {
        if (id == ABOUT_DIALOG) {
             Dialog about = new AboutDialog(this);
 
             // go to the 'new account' activity if < 2 accounts
             about.setOnCancelListener(
                 new DialogInterface.OnCancelListener() {
                     public void onCancel(DialogInterface dialog) {
                         showNewAccountIfNeeded();
                     }
                 });
 
             return about;
         }
 
         return null;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = new MenuInflater(this);
 
         inflater.inflate(R.menu.newexpense, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.new_account:
             startActivity(Globals.NEW_ACCOUNT_INTENT);
 
             return true;
         case R.id.expense_list:
             startActivity(Globals.EXPENSE_LIST_INTENT);
 
             return true;
         case R.id.account_list:
             startActivity(Globals.ACCOUNT_LIST_INTENT);
 
             return true;
         case R.id.about_acash:
             showDialog(ABOUT_DIALOG);
 
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     // implementation
 
     private void showNewAccountIfNeeded() {
         if (redirectedToNewAccount)
             return;
         redirectedToNewAccount = true;
 
         ExpenseDatabase db = ExpenseDatabase.getInstance(this);
 
         if (db.getAccountCount() < 2)
             startActivity(Globals.NEW_ACCOUNT_INTENT);
     }
 
     // event handlers
 
     public void onAddExpense(View v) {
         // add a new expense
         ExpenseDatabase db = ExpenseDatabase.getInstance(this);
 
         if (!db.insertExpense(expenseView.getExpenseAccountFrom(),
                               expenseView.getExpenseAccountTo(),
                               expenseView.getExpenseAmount(),
                               expenseView.getExpenseDate(),
                               expenseView.getExpenseDescription()))
             // TODO so something
             ;
 
         expenseView.clearExpenseAmount();
         expenseView.clearExpenseDescription();
 
         contentModified = false;
     }
 }
