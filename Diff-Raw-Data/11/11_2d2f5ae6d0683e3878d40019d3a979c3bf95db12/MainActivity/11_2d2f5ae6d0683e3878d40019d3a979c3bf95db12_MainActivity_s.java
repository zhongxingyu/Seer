 package com.example.delteutgifter;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.*;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class MainActivity extends Activity implements SetNamesDialog.SetNamesDialogListener, EditExpenseDialog.EditExpenseDialogListener {
 
     private String person1Name = null;
     private String person2Name = null;
     private float paidByPerson1 = 0;
     private float paidByPerson2 = 0;
     private boolean namesSet = false;
     private static ListView expenseLogListView;
     private static ExpenseLogItemAdapter expenseLogItemAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
     }
 
     protected void onStart() {
         super.onStart();
 
         // Restoring saved names and expenses
         SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
         person1Name = sharedPref.getString("person1Name", null);
         person2Name = sharedPref.getString("person2Name", null);
         namesSet = sharedPref.getBoolean("namesSet", false);
         String serializedExpenses = sharedPref.getString("expenses", "");
         Expense.deserializeExpenses(serializedExpenses);
         setButtonNames();
 
         // Setting names if they have not previously been set
         if (!namesSet) {
             // Showing the "Set Names" dialog
             DialogFragment dialog = new SetNamesDialog();
             dialog.show(getFragmentManager(), "SetNamesDialogFragment");
         }
 
         calculateNewBalances();
         displayBalance();
         initializeExpenseLog();
 
 
     }
 
     private void initializeExpenseLog() {
         expenseLogListView = (ListView) findViewById(R.id.logView);
         expenseLogItemAdapter = new ExpenseLogItemAdapter(this, android.R.layout.simple_list_item_1, Expense.getAllExpenses());
         expenseLogListView.setAdapter(expenseLogItemAdapter);
         expenseLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                 showEditExpenseDialog(position);
             }
         });
 
     }
 
     private void showEditExpenseDialog(int expenseIndex) {
         Expense expenseToEdit = Expense.getAllExpenses().get(expenseIndex);
         EditExpenseDialog dialog = new EditExpenseDialog();
         dialog.setExpense(expenseIndex);
         dialog.show(getFragmentManager(), "EditExpenseDialogFragment");
     }
 
     protected void onStop() {
         super.onStop();
         SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPref.edit();
         editor.putString("expenses", Expense.serializeExpenses());
         editor.commit();
 
     }
 
     private void setButtonNames() {
         Button person1Button = (Button) findViewById(R.id.person1button);
         Button person2Button = (Button) findViewById(R.id.person2button);
         person1Button.setText(person1Name);
         person2Button.setText(person2Name);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     public void paidByPerson1(View view) {
         EditText editText = (EditText) findViewById(R.id.ETenterAmount);
         float amountPaid;
         try {
             amountPaid = Float.parseFloat(editText.getText().toString());
         } catch (NumberFormatException e) {
             e.printStackTrace();
             return;
         }
         Calendar timestamp = Calendar.getInstance();
 
         Expense expense = new Expense(person1Name, amountPaid, timestamp);
         Expense.addExpense(expense);
 
         calculateNewBalances();
         ((EditText) findViewById(R.id.ETenterAmount)).setText("");
 
         // Refresh the log
         expenseLogListView.invalidateViews();
     }
 
     public void paidByPerson2(View view) {
         EditText editText = (EditText) findViewById(R.id.ETenterAmount);
         float amountPaid;
         try {
             amountPaid = Float.parseFloat(editText.getText().toString());
         } catch (NumberFormatException e) {
             e.printStackTrace();
             return;
         }
         Calendar timestamp = Calendar.getInstance();
 
         Expense expense = new Expense(person2Name, amountPaid, timestamp);
         Expense.addExpense(expense);
 
         calculateNewBalances();
         ((EditText) findViewById(R.id.ETenterAmount)).setText("");
         // Refresh the log
         expenseLogListView.invalidateViews();
     }
 
     public void calculateNewBalances() {
         paidByPerson1 = 0;
         paidByPerson2 = 0;
 
         ArrayList<Expense> expenses = Expense.getAllExpenses();
 
         for (Expense expense : expenses) {
             if (expense.getName().equals(person1Name)) {
                 paidByPerson1 += expense.getAmount();
             } else if (expense.getName().equals(person2Name)) {
                 paidByPerson2 += expense.getAmount();
             }
         }
 
         displayBalance();
     }
 
     public void displayBalance() {
         double balance = (paidByPerson1 - paidByPerson2) / 2;
         String balanceText;
         if (balance < 0) {
             balanceText = person1Name + " owes: " + (-balance) + ",-";
         } else if (balance > 0) {
             balanceText = person2Name + " owes: " + (balance) + ",-";
         } else {
             balanceText = "You're even!";
         }
         ((TextView) findViewById(R.id.TFbalance)).setText(balanceText);
     }
 
     // The dialog fragment receives a reference to this Activity through the
     // Fragment.onAttach() callback, which it uses to call the following method
     @Override
     public void onSetNamesDialogPositiveClick(DialogFragment dialog) {
         EditText person1Entered = (EditText) dialog.getDialog().findViewById(R.id.ETperson1Name);
         EditText person2Entered = (EditText) dialog.getDialog().findViewById(R.id.ETperson2Name);
         person1Name = person1Entered.getText().toString();
         person2Name = person2Entered.getText().toString();
         setButtonNames();
         namesSet = true;
 
         // Saving names
         SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPref.edit();
         editor.putString("person1Name", person1Name);
         editor.putString("person2Name", person2Name);
         editor.putBoolean("namesSet", namesSet);
         editor.commit();
     }
 
     /**
      * Saves changes made in edit dialog
      *
      * @param dialog    Includes all info from the dialog, including the expense to edit
      * @param expenseId
      */
     @Override
     public void onEditExpenseDialogPositiveClick(EditExpenseDialog dialog, int expenseId) {
         Log.d("Progress", "Getting Amount EditText contents");
         EditText amountEditText = (EditText) dialog.getDialog().findViewById(R.id.edit_amount);
         Log.d("Progress", "Getting Description EditText contents");
         EditText descriptionEditText = (EditText) dialog.getDialog().findViewById(R.id.description);
 
         Log.d("Progress", "Setting new amount");
         Expense.getAllExpenses().get(expenseId).setAmount(Float.parseFloat(amountEditText.toString()));
         Log.d("Progress", "Setting new description");
         Expense.getAllExpenses().get(expenseId).setDescription(descriptionEditText.toString());
     }
 
     /**
      * Deletes the expense clicked
      *
      * @param dialog
      * @param expenseId
      */
     @Override
     public void onEditExpenseDialogNeutralClick(EditExpenseDialog dialog, int expenseId) {
         Expense.deleteExpense(dialog.getExpenseId());
     }
 }
