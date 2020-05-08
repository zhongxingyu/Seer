 package ru.nia.ledged.android;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.*;
 import ru.nia.ledged.core.AccountTree;
 
 import java.util.Date;
 
 public class TransactionEditor extends Activity {
     AccountTree accounts = new AccountTree();
 
     // input data
     public static final String KEY_LEAVES_ACCOUNTS = "leaves";
     public static final String KEY_DESCRIPTIONS = "descriptions";
 
     // output data
     public static final String KEY_DATE = "date";
     public static final String KEY_DESC = "desc";
     public static final String KEY_ACCOUNTS = "accs";
     public static final String KEY_AMOUNTS = "amounts";
 
     public static final int MIN_POSTINGS_COUNT = 2;
     public static final int MAX_EMPTY_AMOUNTS = 1;
 
     private LinearLayout postingsEditors;
     private EditText dateEdit;
     private AutoCompleteTextView descriptionEdit;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.transaction_editor);
 
         Bundle extras = getIntent().getExtras();
         String[] leaveNames = extras.getStringArray(KEY_LEAVES_ACCOUNTS);
         assert leaveNames != null;
         for (String name : leaveNames) {
             accounts.findOrCreateAccount(name);
         }
 
         String[] descriptions = extras.getStringArray(KEY_DESCRIPTIONS);
 
         dateEdit = (EditText) findViewById(R.id.date);
         dateEdit.setText(DateFormat.format("M-d", new Date()));
         dateEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                 View next = textView.focusSearch(View.FOCUS_DOWN);
                 next.requestFocus();
                 return true;
             }
         });
 
         descriptionEdit = (AutoCompleteTextView) findViewById(R.id.description);
         descriptionEdit.setAdapter(new ArrayAdapter<String>(this, R.layout.completion_item, descriptions));
 
         postingsEditors = (LinearLayout) findViewById(R.id.postings);
 
         Button confirmButtion = (Button) findViewById(R.id.confirm);
         confirmButtion.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 save();
             }
         });
 
         Button cancelButton = (Button) findViewById(R.id.cancel);
         cancelButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 cancel();
             }
         });
 
         ImageButton addPostingEditorButton = (ImageButton) findViewById(R.id.add_posting);
         addPostingEditorButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 addPostingEditor();
             }
         });
     }
 
     private void addPostingEditor() {
         // TODO: custom control
         final LinearLayout postingEditor = new LinearLayout(this);
         LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         vi.inflate(R.layout.posting_editor, postingEditor, true);
 
         AutoCompleteTextView accName = (AutoCompleteTextView) postingEditor.findViewById(R.id.account);
         accName.setAdapter(new AutoCompleteAdapter(this, R.layout.completion_item, accounts));
         accName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                 if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                     AutoCompleteTextView view = (AutoCompleteTextView) textView;
                     view.getText().insert(view.getSelectionStart(), AccountTree.ACCOUNT_SEPARATOR);
                     return true;
                 } else {
                     return false;
                 }
             }
         });
 
         ImageButton delete = (ImageButton) postingEditor.findViewById(R.id.delete);
         delete.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 postingsEditors.removeView(postingEditor);
             }
         });
 
         postingsEditors.addView(postingEditor);
     }
 
     private void save() {
         String date = dateEdit.getText().toString().trim();
         if (date.length() == 0) {
             reportError(R.string.empty_date);
             return;
         }
 
         String desc = descriptionEdit.getText().toString();
         if (desc.trim().length() == 0) {
             reportError(R.string.empty_description);
            return;
         }
 
         int postingsCount = postingsEditors.getChildCount();
         if (postingsCount < MIN_POSTINGS_COUNT) {
             reportError(R.string.not_enough_postings);
             return;
         }
 
         String[] accounts = new String[postingsCount];
         String[] amounts = new String[postingsCount];
         int emptyAmounts = 0;
 
         for (int i = 0; i < postingsCount; ++i) {
             LinearLayout postingEditor = (LinearLayout) postingsEditors.getChildAt(i);
 
             AutoCompleteTextView accName = (AutoCompleteTextView) postingEditor.findViewById(R.id.account);
             accounts[i] = accName.getText().toString().trim();
             if (accounts[i].length() == 0) {
                 reportError(R.string.empty_account);
                 return;
             }
 
             EditText amount = (EditText) postingEditor.findViewById(R.id.amount);
             amounts[i] = amount.getText().toString();
             if (amounts[i].trim().length() == 0) {
                 ++emptyAmounts;
 
                 if (emptyAmounts > MAX_EMPTY_AMOUNTS) {
                     reportError(R.string.too_many_empty_amounts);
                     return;
                 }
             }
         }
 
         Bundle extras = new Bundle();
         extras.putString(KEY_DATE, date);
         extras.putString(KEY_DESC, desc);
         extras.putStringArray(KEY_ACCOUNTS, accounts);
         extras.putStringArray(KEY_AMOUNTS, amounts);
 
         Intent intent = new Intent();
         intent.putExtras(extras);
         setResult(RESULT_OK, intent);
         finish();
     }
 
     private void cancel() {
         setResult(RESULT_CANCELED);
         finish();
     }
 
     private void reportError(int message_res_id) {
         Toast.makeText(this, message_res_id, Toast.LENGTH_LONG).show();
     }
 }
