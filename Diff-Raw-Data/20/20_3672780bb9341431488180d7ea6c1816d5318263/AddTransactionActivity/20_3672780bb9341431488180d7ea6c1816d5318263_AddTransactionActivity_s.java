 package no.kantega.android;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 import no.kantega.android.models.Transaction;
 import no.kantega.android.models.TransactionTag;
 import no.kantega.android.models.TransactionType;
 import no.kantega.android.utils.DatabaseHelper;
 import no.kantega.android.utils.DatabaseOpenHelper;
 import no.kantega.android.utils.FmtUtil;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 public class AddTransactionActivity extends Activity {
     private DatabaseHelper db;
    private TextView mDateDisplay;
     private Button mPickDate;
     private List<CharSequence> list;
 
     private int mYear;
     private int mMonth;
     private int mDay;
 
     private String selectedTransactionTag;
 
     static final int DATE_DIALOG_ID = 0;
 
     private OnClickListener addTransactionButtonListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
             boolean newTransactionOk = true;
 
             Transaction t = new Transaction();
             TransactionTag ttag = new TransactionTag();
             TransactionType ttype = new TransactionType();
 
             ttag.setName(selectedTransactionTag);
             ttype.setName("Kontant");
 
             Date d = FmtUtil.stringToDate("yyyy-MM-dd", String.format("%s-%s-%s", mYear, mMonth, mDay));
 
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
                 db.insert(t);
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
 
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
         mPickDate = (Button) findViewById(R.id.pickDate);
 
         mPickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
 
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
 
         updateDisplay();
 
         Spinner spinner = (Spinner) findViewById(R.id.spinner_category);
         //ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
         //        this, R.array.category_array, android.R.layout.simple_spinner_item);
         this.db = new DatabaseHelper(new DatabaseOpenHelper(
                 getApplicationContext()).getReadableDatabase());
         fillCategoryList();
         ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, list);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
     }
 
     private void fillCategoryList() {
         ArrayList<TransactionTag> transactionTagList = new ArrayList<TransactionTag>(db.getAllTags());
         list = new ArrayList<CharSequence>();
 
         for (int i = 0; i < transactionTagList.size(); i++) {
             list.add(transactionTagList.get(i).getName());
         }
 
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
                         mDay);
         }
         return null;
     }
 
     // updates the date we display in the TextView
     private void updateDisplay() {
        mDateDisplay.setText(new StringBuilder()
                 // Month is 0 based so add 1
                 .append(mMonth + 1).append("-").append(mDay).append("-")
                 .append(mYear).append(" "));
     }
 
     private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                               int dayOfMonth) {
             mYear = year;
             mMonth = monthOfYear;
             mDay = dayOfMonth;
             updateDisplay();
         }
     };
 
     class MyOnItemSelectedListener implements OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent,
                                    View view, int pos, long id) {
             selectedTransactionTag = parent.getItemAtPosition(pos).toString();
             Toast.makeText(parent.getContext(), "The category is " +
                     selectedTransactionTag, Toast.LENGTH_LONG).show();
         }
 
         public void onNothingSelected(AdapterView parent) {
             // Do nothing.
         }
     }
 
 }
