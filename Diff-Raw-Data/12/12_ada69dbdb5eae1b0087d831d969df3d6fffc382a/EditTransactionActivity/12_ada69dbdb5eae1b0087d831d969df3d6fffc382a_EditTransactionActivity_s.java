 package no.kantega.android.afp;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.*;
 import no.kantega.android.afp.controllers.Transactions;
 import no.kantega.android.afp.models.Transaction;
 import no.kantega.android.afp.models.TransactionTag;
 import no.kantega.android.afp.utils.FmtUtil;
 import no.kantega.android.afp.utils.HttpUtil;
 
 import java.io.IOException;
 import java.util.*;
 
 public class EditTransactionActivity extends Activity {
 
     private static final String TAG = EditTransactionActivity.class.
             getSimpleName();
     private static final String PROPERTIES_FILE = "url.properties";
     private Transactions db;
     private List<String> categories;
     private ArrayAdapter<String> adapter;
     private Transaction t;
     private String selectedTransactionTag;
     private int pickYear;
     private int pickMonth;
     private int pickDay;
     private static final int DATE_DIALOG_ID = 0;
     private EditText text;
     private Button date;
     private EditText amount;
     private Spinner category;
     private TextView suggestedTag;
     private String suggestUrl;
     private final View.OnClickListener editTransactionButtonListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             if (t.isInternal()) {
                 boolean editTransactionOk = true;
                 TransactionTag ttag = t.getTag();
                 selectedTransactionTag = category.getSelectedItem().toString();
                 ttag.setName(selectedTransactionTag);
                 Date d = FmtUtil.stringToDate("yyyy-MM-dd", String.format("%s-%s-%s", pickYear, pickMonth + 1, pickDay));
                 if (FmtUtil.isNumber(amount.getText().toString())) {
                     t.setAmountOut(Double.parseDouble(amount.getText().toString()));
                 } else {
                     Toast.makeText(getApplicationContext(), R.string.invalid_amount,
                             Toast.LENGTH_LONG).show();
                     editTransactionOk = false;
                 }
                 if (editTransactionOk) {
                     t.setText(text.getText().toString());
                     t.setTag(ttag);
                     t.setAccountingDate(d);
                     t.setDirty(true);
                     t.setChanged(true);
                     db.update(t);
                     Toast.makeText(getApplicationContext(), R.string.transaction_updated,
                             Toast.LENGTH_LONG).show();
                     finish();
                 }
             } else {
                TransactionTag ttag = new TransactionTag();
                ttag.setName(selectedTransactionTag);
                 t.setTag(ttag);
                 t.setDirty(true);
                 t.setChanged(true);
                 db.update(t);
                 Toast.makeText(getApplicationContext(), R.string.transaction_updated, Toast.LENGTH_LONG).show();
                 finish();
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edittransaction);
         Bundle extras = getIntent().getExtras();
         t = (Transaction) extras.getSerializable("transaction");
         this.db = new Transactions(getApplicationContext());
         Button editButton = (Button) findViewById(R.id.edittransaction_button_edittransaction);
         editButton.setOnClickListener(editTransactionButtonListener);
         setupViews();
         checkInternal();
         readProperties();
     }
 
     private void readProperties() {
         try {
             final Properties properties = new Properties();
             properties.load(getAssets().open(PROPERTIES_FILE));
             suggestUrl = properties.get("suggestTag").toString();
         } catch (IOException e) {
             Log.e(TAG, "IOException", e);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         new SuggestionsTask().execute(suggestUrl,
                 FmtUtil.trimTransactionText(t.getText()));
     }
 
     private void updateSpinnerPosition(String tag) {
         int spinnerPosition;
         if (selectedTransactionTag != null && !selectedTransactionTag.equals("Not tagged")) {
             spinnerPosition = adapter.getPosition(selectedTransactionTag);
         } else if (selectedTransactionTag == null || selectedTransactionTag.equals("Not tagged") && tag != null) {
             spinnerPosition = adapter.getPosition(tag);
             selectedTransactionTag = tag;
         } else {
             spinnerPosition = adapter.getPosition("Not tagged");
         }
         category.setSelection(spinnerPosition);
     }
 
     private void setupViews() {
         text = (EditText) findViewById(R.id.edittransaction_edittext_text);
         date = (Button) findViewById(R.id.edittransaction_button_pickDate);
         amount = (EditText) findViewById(R.id.edittransaction_edittext_amount);
         category = (Spinner) findViewById(R.id.edittransaction_spinner_category);
         suggestedTag = (TextView) findViewById(R.id.suggested_tag);
         //currentTag = (TextView) findViewById(R.id)
         text.setText(FmtUtil.trimTransactionText(t.getText()));
         date.setText(FmtUtil.dateToString("yyyy-MM-dd", t.getAccountingDate()));
         amount.setText(String.valueOf(t.getAmountOut()));
         date.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showDialog(DATE_DIALOG_ID);
             }
         });
         final Calendar c = Calendar.getInstance();
         pickYear = c.get(Calendar.YEAR);
         pickMonth = c.get(Calendar.MONTH);
         pickDay = c.get(Calendar.DAY_OF_MONTH);
         updateDisplay();
         fillCategoryList();
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         category.setAdapter(adapter);
         category.setOnItemSelectedListener(new MyOnItemSelectedListener());
         selectedTransactionTag = t.getTag().getName();
         if (selectedTransactionTag != null && selectedTransactionTag.length() > 0 &&
                 !selectedTransactionTag.equals("Not tagged")) {
             category.setSelection(adapter.getPosition(selectedTransactionTag));
         } else {
             category.setSelection(adapter.getPosition("Not tagged"));
         }
     }
 
     private void checkInternal() {
         if (!t.isInternal()) {
             text.setEnabled(false);
             date.setEnabled(false);
             amount.setEnabled(false);
         }
     }
 
     private void updateDisplay() {
         date.setText(new StringBuilder()
                 // Month is 0 based so add 1
                 .append(pickMonth + 1).append("-").append(pickDay).append("-")
                 .append(pickYear).append(" "));
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
 
     private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                               int dayOfMonth) {
             pickYear = year;
             pickMonth = monthOfYear;
             pickDay = dayOfMonth;
             updateDisplay();
         }
     };
 
     private void fillCategoryList() {
         categories = new ArrayList<String>();
         categories.add("Not tagged");
         for (TransactionTag tag : db.getTags()) {
             categories.add(tag.getName());
         }
     }
 
     private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent,
                                    View view, int pos, long id) {
             if (selectedTransactionTag != null && !selectedTransactionTag.equals("Not tagged")) {
                 selectedTransactionTag = parent.getItemAtPosition(pos).toString();
             } else {
                 selectedTransactionTag = null;
             }
         }
 
         public void onNothingSelected(AdapterView parent) {
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         db.close();
     }
 
     private class SuggestionsTask extends AsyncTask<String, Integer, String> {
 
         @Override
         protected String doInBackground(String... params) {
             return HttpUtil.post(params[0], params[1], "text/plain");
         }
 
         @Override
         protected void onPostExecute(String s) {
             if (s != null) {
                 suggestedTag.setText(s);
                updateSpinnerPosition(s.trim().length() == 0 ? null : s);
             }
         }
     }
 }
