 package com.imaginea.android.sugarcrm;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.text.InputType;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.CursorAdapter;
 import android.widget.Filterable;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Accounts;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsColumns;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.UserColumns;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Users;
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.Util;
 import com.imaginea.android.sugarcrm.util.ViewUtil;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 public class EditDetailsActivity extends Activity {
 
     private final static String TAG = "EditDetailsActivity";
 
     private int MODE = -1;
 
     private ViewGroup mDetailsTable;
 
     private Cursor mCursor;
 
     private String mSugarBeanId;
 
     private String mModuleName;
 
     private String mRowId;
 
     private String[] mSelectFields;
 
     private Uri mIntentUri;
 
     private DatabaseHelper mDbHelper;
 
     private LoadContentTask mTask;
 
     private Messenger mMessenger;
 
     private StatusHandler mStatusHandler;
 
     private AutoSuggestAdapter mAccountAdapter;
 
     private AutoSuggestAdapter mUserAdapter;
 
     private Cursor mAccountCursor;
 
     private Cursor mUserCursor;
 
     private String mSelectedAccountName;
 
     private String mSelectedUserName;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.edit_details);
 
         mDbHelper = new DatabaseHelper(this);
 
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
 
         mModuleName = Util.CONTACTS;
         if (extras != null) {
             // i always get the module name
             mModuleName = extras.getString(RestUtilConstants.MODULE_NAME);
 
             mRowId = intent.getStringExtra(Util.ROW_ID);
             mSugarBeanId = intent.getStringExtra(RestUtilConstants.BEAN_ID);
         }
         // when the user comes from the relationships, intent.getData() won't be null
         mIntentUri = intent.getData();
 
         if (intent.getData() != null && mRowId != null) {
             // TODO: this case is intentionally left as of now
             // this case comes into picture only if the user can change the accountName to which it
             // is associated
             MODE = Util.EDIT_RELATIONSHIP_MODE;
         } else if (mRowId != null) {
             MODE = Util.EDIT_ORPHAN_MODE;
         } else if (intent.getData() != null) {
             MODE = Util.NEW_RELATIONSHIP_MODE;
         } else {
             MODE = Util.NEW_ORPHAN_MODE;
         }
 
         if (intent.getData() == null && MODE == Util.EDIT_ORPHAN_MODE) {
             intent.setData(Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId));
         } else if (intent.getData() == null && MODE == Util.NEW_ORPHAN_MODE) {
             intent.setData(mDbHelper.getModuleUri(mModuleName));
         }
 
         mSelectFields = mDbHelper.getModuleProjections(mModuleName);
 
         /*
          * if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) { mCursor =
          * getContentResolver().query(getIntent().getData(), mSelectFields, null, null,
          * mDbHelper.getModuleSortOrder(mModuleName)); }
          */
         // startManagingCursor(mCursor);
         // setContents();
 
         mTask = new LoadContentTask();
         mTask.execute(null);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         SugarService.unregisterMessenger(mMessenger);
         if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
             mTask.cancel(true);
         }
 
         if (mAccountCursor != null)
             mAccountCursor.close();
 
         if (mUserCursor != null)
             mUserCursor.close();
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (mMessenger == null) {
             mStatusHandler = new StatusHandler();
             mMessenger = new Messenger(mStatusHandler);
         }
         SugarService.registerMessenger(mMessenger);
     }
 
     class LoadContentTask extends AsyncTask<Object, Object, Object> {
 
         int staticRowsCount;
 
         final int STATIC_ROW = 1;
 
         final int DYNAMIC_ROW = 2;
 
         final int SAVE_BUTTON = 3;
 
         final int INPUT_TYPE = 4;
 
         LoadContentTask() {
             mDetailsTable = (ViewGroup) findViewById(R.id.accountDetalsTable);
 
             // as the last child is the SAVE button, count - 1 has to be done.
             staticRowsCount = mDetailsTable.getChildCount() - 1;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
 
             TextView tv = (TextView) findViewById(R.id.headerText);
             if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) {
                 tv.setText(String.format(getString(R.string.editDetailsHeader), mModuleName));
             } else if (MODE == Util.NEW_ORPHAN_MODE || MODE == Util.NEW_RELATIONSHIP_MODE) {
                 tv.setText(String.format(getString(R.string.newDetailsHeader), mModuleName));
             }
 
             mAccountCursor = getContentResolver().query(mDbHelper.getModuleUri(Util.ACCOUNTS), Accounts.LIST_PROJECTION, null, null, null);
             mAccountAdapter = new AccountsSuggestAdapter(getBaseContext(), mAccountCursor);
 
             mUserCursor = getContentResolver().query(mDbHelper.getModuleUri(Util.USERS), Users.DETAILS_PROJECTION, null, null, null);
             mUserAdapter = new UsersSuggestAdapter(getBaseContext(), mUserCursor);
         }
 
         @Override
         protected void onProgressUpdate(Object... values) {
             super.onProgressUpdate(values);
 
             switch ((Integer) values[0]) {
 
             case STATIC_ROW:
                 String fieldName = (String) values[1];
 
                 View editRow = (View) values[2];
                 editRow.setVisibility(View.VISIBLE);
 
                 TextView labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 AutoCompleteTextView valueView = (AutoCompleteTextView) values[5];
                 valueView.setText((String) values[6]);
 
                 // set the adapter to auto-suggest
                 if (!Util.ACCOUNTS.equals(mModuleName)
                                                 && fieldName.equals(ModuleFields.ACCOUNT_NAME)) {
                     valueView.setAdapter(mAccountAdapter);
                     valueView.setOnItemClickListener(new AccountsClickedItemListener());
                 } else if (fieldName.equals(ModuleFields.ASSIGNED_USER_NAME)) {
                     valueView.setAdapter(mUserAdapter);
                     valueView.setOnItemClickListener(new UsersClickedItemListener());
                 }
                 break;
 
             case DYNAMIC_ROW:
                 fieldName = (String) values[1];
 
                 editRow = (View) values[2];
                 editRow.setVisibility(View.VISIBLE);
 
                 labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 valueView = (AutoCompleteTextView) values[5];
                 valueView.setText((String) values[6]);
 
                 // set the adapter to auto-suggest
                 if (!Util.ACCOUNTS.equals(mModuleName)
                                                 && fieldName.equals(ModuleFields.ACCOUNT_NAME)) {
                     valueView.setAdapter(mAccountAdapter);
                     valueView.setOnItemClickListener(new AccountsClickedItemListener());
                 } else if (fieldName.equals(ModuleFields.ASSIGNED_USER_NAME)) {
                     valueView.setAdapter(mUserAdapter);
                     valueView.setOnItemClickListener(new UsersClickedItemListener());
                 }
 
                 mDetailsTable.addView(editRow);
                 break;
 
             case INPUT_TYPE:
                 valueView = (AutoCompleteTextView) values[1];
                 valueView.setInputType((Integer) values[2]);
                 break;
 
             }
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             try {
                 if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) {
                     mCursor = getContentResolver().query(getIntent().getData(), mSelectFields, null, null, mDbHelper.getModuleSortOrder(mModuleName));
                 }
                 setContents();
             } catch (Exception e) {
                 Log.e(TAG, e.getMessage(), e);
                 return Util.FETCH_FAILED;
             }
 
             return Util.FETCH_SUCCESS;
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
 
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
 
             // close the cursor irrespective of the result
             if (mCursor != null && !mCursor.isClosed())
                 mCursor.close();
 
             if (isCancelled())
                 return;
             int retVal = (Integer) result;
             switch (retVal) {
             case Util.FETCH_FAILED:
                 break;
             case Util.FETCH_SUCCESS:
                 // set visibility for the SAVE button
                 findViewById(R.id.save).setVisibility(View.VISIBLE);
                 break;
             default:
 
             }
         }
 
         private void setContents() {
 
             String[] detailsProjection = mSelectFields;
 
             if (mDbHelper == null)
                 mDbHelper = new DatabaseHelper(getBaseContext());
 
             if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) {
                 if (!isCancelled()) {
                     mCursor.moveToFirst();
                     mSugarBeanId = mCursor.getString(1); // beanId has columnIndex 1
                 }
             }
 
             LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             Map<String, ModuleField> fieldNameVsModuleField = mDbHelper.getModuleFields(mModuleName);
             Map<String, String> fieldsExcludedForEdit = mDbHelper.getFieldsExcludedForEdit();
 
             int rowsCount = 0; // to keep track of number of rows being used
             for (int i = 0; i < detailsProjection.length; i++) {
                 // if the task gets cancelled
                 if (isCancelled())
                     break;
 
                 String fieldName = detailsProjection[i];
 
                 // if the field name is excluded in details screen, skip it
                 if (fieldsExcludedForEdit.containsKey(fieldName)) {
                     continue;
                 }
 
                 // get the attributes of the moduleField
                 ModuleField moduleField = fieldNameVsModuleField.get(fieldName);
 
                 ViewGroup tableRow;
                 TextView textViewForLabel;
                 AutoCompleteTextView editTextForValue;
                 if (staticRowsCount > rowsCount) {
                     tableRow = (ViewGroup) mDetailsTable.getChildAt(rowsCount);
                     textViewForLabel = (TextView) tableRow.getChildAt(0);
                     editTextForValue = (AutoCompleteTextView) tableRow.getChildAt(1);
                 } else {
                     tableRow = (ViewGroup) inflater.inflate(R.layout.edit_table_row, null);
                     textViewForLabel = (TextView) tableRow.getChildAt(0);
                     editTextForValue = (AutoCompleteTextView) tableRow.getChildAt(1);
                 }
 
                 String label;
                 if (moduleField.isRequired()) {
                     label = moduleField.getLabel() + " *";
                 } else {
                     label = moduleField.getLabel();
                 }
 
                 int command = STATIC_ROW;
                 if (staticRowsCount < rowsCount) {
                     command = DYNAMIC_ROW;
                 }
 
                 if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) {
                     String value = mCursor.getString(mCursor.getColumnIndex(fieldName));
                     if (!TextUtils.isEmpty(value)) {
                         publishProgress(command, fieldName, tableRow, textViewForLabel, label, editTextForValue, value);
                     } else {
                         publishProgress(command, fieldName, tableRow, textViewForLabel, label, editTextForValue, "");
                     }
                     setInputType(editTextForValue, moduleField);
 
                 } else {
                     publishProgress(command, fieldName, tableRow, textViewForLabel, label, editTextForValue, "");
                 }
                 rowsCount++;
             }
 
         }
 
         /*
          * takes care of basic validation automatically for some fields
          */
         private void setInputType(TextView editTextForValue, ModuleField moduleField) {
             if (Log.isLoggable(TAG, Log.VERBOSE))
                 Log.v(TAG, "ModuleField type:" + moduleField.getType());
             if (moduleField.getType().equals("phone")) {
                 // editTextForValue.setInputType(InputType.TYPE_CLASS_PHONE);
                 publishProgress(INPUT_TYPE, editTextForValue, InputType.TYPE_CLASS_PHONE);
             }
         }
     }
 
     /**
      * on click listener for saving a module item
      * 
      * @param v
      */
     public void saveModuleItem(View v) {
         boolean hasError = false;
         String[] detailsProjection = mSelectFields;
 
         Map<String, String> modifiedValues = new LinkedHashMap<String, String>();
         if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE)
             modifiedValues.put(RestUtilConstants.ID, mSugarBeanId);
 
         Map<String, String> fieldsExcludedForEdit = mDbHelper.getFieldsExcludedForEdit();
         int rowsCount = 0;
 
         for (int i = 0; i < detailsProjection.length; i++) {
 
             String fieldName = detailsProjection[i];
 
             // if the field name is excluded in details screen, skip it
             if (fieldsExcludedForEdit.containsKey(fieldName)) {
                 continue;
             }
 
             AutoCompleteTextView editText = (AutoCompleteTextView) ((ViewGroup) mDetailsTable.getChildAt(rowsCount)).getChildAt(1);
             String fieldValue = editText.getText().toString();
             Log.i(TAG, fieldName + " : " + fieldValue);
 
             // TODO: validation
 
             if (!Util.ACCOUNTS.equals(mModuleName) && fieldName.equals(ModuleFields.ACCOUNT_NAME)) {
                 if (mSelectedAccountName != null && fieldValue != null) {
                     if (!mSelectedAccountName.equals(fieldValue)) {
                         // account name is incorrect.
                         hasError = true;
                         // TODO : Set the background color to red
                         editText.setBackgroundColor(R.color.blue);
                     }
                 }
             } else if (fieldName.equals(ModuleFields.ASSIGNED_USER_NAME)) {
                 if (mSelectedUserName != null && fieldValue != null) {
                     if (!mSelectedUserName.equals(fieldValue)) {
                         // user name is incorrect.
                         hasError = true;
                         // TODO : Set the background color to red
                         editText.setBackgroundColor(R.color.blue);
                     }
                 }
             }
 
             // add the fieldName : fieldValue in the ContentValues
             modifiedValues.put(fieldName, editText.getText().toString());
             rowsCount++;
         }
 
         if (!hasError) {
 
             if (MODE == Util.EDIT_ORPHAN_MODE || MODE == Util.EDIT_RELATIONSHIP_MODE) {
                 ServiceHelper.startServiceForUpdate(getBaseContext(), Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId), mModuleName, mSugarBeanId, modifiedValues);
             } else if (MODE == Util.NEW_RELATIONSHIP_MODE) {
                 modifiedValues.put(ModuleFields.DELETED, Util.NEW_ITEM);
                 ServiceHelper.startServiceForInsert(getBaseContext(), mIntentUri, mModuleName, modifiedValues);
             } else if (MODE == Util.NEW_ORPHAN_MODE) {
                 modifiedValues.put(ModuleFields.DELETED, Util.NEW_ITEM);
                 ServiceHelper.startServiceForInsert(getBaseContext(), mDbHelper.getModuleUri(mModuleName), mModuleName, modifiedValues);
             }
 
             // finish();
         }
         ViewUtil.dismissVirtualKeyboard(getBaseContext(), v);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Hold on to this
         // Inflate the currently selected menu XML resource.
         MenuItem item;
         item = menu.add(1, R.id.save, 1, R.string.save);
         item.setIcon(android.R.drawable.ic_menu_save);
         item.setAlphabeticShortcut('s');
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.save:
            saveModuleItem(getCurrentFocus());
             return true;
         default:
             return true;
         }
         // return false;
     }
 
     /*
      * Status Handler, Handler updates the screen based on messages sent by the SugarService or any
      * tasks
      */
     private class StatusHandler extends Handler {
         StatusHandler() {
         }
 
         public void handleMessage(Message message) {
             switch (message.what) {
             case R.id.status:
                 if (Log.isLoggable(TAG, Log.DEBUG))
                     Log.d(TAG, "Display Status");
                 ViewUtil.makeToast(getBaseContext(), (String) message.obj);
                 finish();
                 break;
             }
         }
     }
 
     public static class AutoSuggestAdapter extends CursorAdapter implements Filterable {
         protected ContentResolver mContent;
 
         protected DatabaseHelper mDbHelper;
 
         public AutoSuggestAdapter(Context context, Cursor c) {
             super(context, c);
             mContent = context.getContentResolver();
             mDbHelper = new DatabaseHelper(context);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             final LayoutInflater inflater = LayoutInflater.from(context);
             final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.autosuggest_list_item, parent, false);
             ((TextView) view.getChildAt(0)).setText(cursor.getString(2));
             return view;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "bindView : " + cursor.getString(2));
             ((TextView) ((LinearLayout) view).getChildAt(0)).setText(cursor.getString(2));
         }
 
         @Override
         public String convertToString(Cursor cursor) {
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "convertToString : " + cursor.getString(2));
             return cursor.getString(2);
         }
 
     }
 
     public static class AccountsSuggestAdapter extends AutoSuggestAdapter {
 
         public AccountsSuggestAdapter(Context context, Cursor c) {
             super(context, c);
         }
 
         @Override
         public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
             if (getFilterQueryProvider() != null) {
                 return getFilterQueryProvider().runQuery(constraint);
             }
 
             StringBuilder buffer = null;
             String[] args = null;
             if (constraint != null) {
                 buffer = new StringBuilder();
                 buffer.append("UPPER(");
                 buffer.append(AccountsColumns.NAME);
                 buffer.append(") GLOB ?");
                 args = new String[] { constraint.toString().toUpperCase() + "*" };
             }
 
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "constraint " + (constraint != null ? constraint.toString() : ""));
 
             return mContent.query(mDbHelper.getModuleUri(Util.ACCOUNTS), Accounts.LIST_PROJECTION, buffer == null ? null
                                             : buffer.toString(), args, Accounts.DEFAULT_SORT_ORDER);
         }
     }
 
     public static class UsersSuggestAdapter extends AutoSuggestAdapter {
 
         public UsersSuggestAdapter(Context context, Cursor c) {
             super(context, c);
         }
 
         @Override
         public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
             if (getFilterQueryProvider() != null) {
                 return getFilterQueryProvider().runQuery(constraint);
             }
 
             StringBuilder buffer = null;
             String[] args = null;
             if (constraint != null) {
                 buffer = new StringBuilder();
                 buffer.append("UPPER(");
                 buffer.append(UserColumns.USER_NAME);
                 buffer.append(") GLOB ?");
                 args = new String[] { constraint.toString().toUpperCase() + "*" };
             }
 
             if (Log.isLoggable(TAG, Log.DEBUG))
                 Log.d(TAG, "constraint " + (constraint != null ? constraint.toString() : ""));
 
             return mContent.query(mDbHelper.getModuleUri(Util.USERS), Users.DETAILS_PROJECTION, buffer == null ? null
                                             : buffer.toString(), args, null);
         }
     }
 
     public class AccountsClickedItemListener implements AdapterView.OnItemClickListener {
 
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
             try {
                 // Remembers the selected account name
                 Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                 mSelectedAccountName = cursor.getString(2);
             } catch (Exception e) {
                 Log.e(TAG, "cannot get the clicked index " + position);
             }
 
         }
     }
 
     public class UsersClickedItemListener implements AdapterView.OnItemClickListener {
 
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
             try {
                 // Remembers the selected username
                 Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                 mSelectedUserName = cursor.getString(2);
             } catch (Exception e) {
                 Log.e(TAG, "cannot get the clicked index " + position);
             }
 
         }
     }
 }
