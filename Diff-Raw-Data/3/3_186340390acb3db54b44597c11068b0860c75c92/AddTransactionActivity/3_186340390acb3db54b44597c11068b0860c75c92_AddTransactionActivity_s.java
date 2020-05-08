 package no.kantega.android;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 import no.kantega.android.controllers.Transactions;
 import no.kantega.android.models.Transaction;
 import no.kantega.android.models.TransactionTag;
 import no.kantega.android.models.TransactionType;
 import no.kantega.android.utils.FmtUtil;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class AddTransactionActivity extends Activity {
 
     private Transactions db;
     private Button pickDate;
     private List<String> categories;
     private int pickYear;
     private int pickMonth;
     private int pickDay;
     private String selectedTransactionTag;
     private static final int DATE_DIALOG_ID = 0;
     private OnClickListener addTransactionButtonListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             boolean newTransactionOk = true;
             Transaction t = new Transaction();
             TransactionTag ttag = new TransactionTag();
             TransactionType ttype = new TransactionType();
             ttag.setName(selectedTransactionTag);
             ttype.setName("Kontant");
            Date d = FmtUtil.stringToDate("yyyy-MM-dd", String.format("%s-%s-%s", pickYear, pickMonth, pickDay));
             EditText etamount = (EditText) findViewById(R.id.edittext_amount);
             EditText ettext = (EditText) findViewById(R.id.edittext_text);
             if (etamount.getText().toString().trim() != "" && FmtUtil.isNumber(etamount.getText().toString())) {
                 t.setAmountOut(Double.parseDouble(etamount.getText().toString()));
             } else {
                 Toast.makeText(getApplicationContext(), "Invalid amount", Toast.LENGTH_LONG).show();
                 newTransactionOk = false;
             }
             if (newTransactionOk) {
                 t.setAmountIn(0.0);
                 t.setText(ettext.getText().toString());
                 t.setTag(ttag);
                 t.setType(ttype);
                 t.setAccountingDate(d);
                 t.setFixedDate(d);
                 t.setTimestamp(new Date().getTime());
                 t.setInternal(true);
                 t.setDirty(true);
                 t.setChanged(false);
                 db.add(t);
                 Toast.makeText(getApplicationContext(), "Transaction added", Toast.LENGTH_LONG).show();
                 finish();
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.addtransaction);
         Button addTransaction = (Button) findViewById(R.id.button_add_transaction);
         addTransaction.setOnClickListener(addTransactionButtonListener);
         pickDate = (Button) findViewById(R.id.pickDate);
         pickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
         final Calendar c = Calendar.getInstance();
         pickYear = c.get(Calendar.YEAR);
         pickMonth = c.get(Calendar.MONTH);
         pickDay = c.get(Calendar.DAY_OF_MONTH);
         updateDisplay();
         Spinner spinner = (Spinner) findViewById(R.id.spinner_category);
         this.db = new Transactions(getApplicationContext());
         fillCategoryList();
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
         selectedTransactionTag = adapter.getItem(0);
     }
 
     private void fillCategoryList() {
         ArrayList<TransactionTag> transactionTagList = new ArrayList<TransactionTag>(db.getTags());
         categories = new ArrayList<String>();
         for (int i = 0; i < transactionTagList.size(); i++) {
             categories.add(transactionTagList.get(i).getName());
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this, mDateSetListener, pickYear, pickMonth,
                         pickDay);
         }
         return null;
     }
 
     // updates the date we display in the TextView
     private void updateDisplay() {
         pickDate.setText(new StringBuilder()
                 // Month is 0 based so add 1
                 .append(pickMonth + 1).append("-").append(pickDay).append("-")
                 .append(pickYear).append(" "));
     }
 
     private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                               int dayOfMonth) {
             pickYear = year;
             pickMonth = monthOfYear;
             pickDay = dayOfMonth;
             updateDisplay();
         }
     };
 
     private class MyOnItemSelectedListener implements OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent,
                                    View view, int pos, long id) {
             selectedTransactionTag = parent.getItemAtPosition(pos).toString();
         }
 
         public void onNothingSelected(AdapterView parent) {
             // Do nothing.
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.close();
     }
 }
