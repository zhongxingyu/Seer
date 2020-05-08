 package com.imaginea.android.sugarcrm;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.method.LinkMovementMethod;
 import android.text.method.MovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.ui.BaseActivity;
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.Util;
 import com.imaginea.android.sugarcrm.util.ViewUtil;
 
 import java.net.URLEncoder;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * ModuleDetailFragment is used to show details for all modules.
  * 
  */
 public class ModuleDetailFragment extends Fragment {
 
     private String mRowId;
 
     private String mSugarBeanId;
 
     private String mModuleName;
 
     private Cursor mCursor;
 
     private String[] mSelectFields;
 
     private ViewGroup mDetailsTable;
 
     private String[] mRelationshipModules;
 
     private Uri mUri;
 
     private DatabaseHelper mDbHelper;
 
     private LoadContentTask mTask;
 
     private static final int HEADER = 1;
 
     private static final int STATIC_ROW = 2;
 
     private static final int DYNAMIC_ROW = 3;
 
     private ProgressDialog mProgressDialog;
 
     private static final String LOG_TAG = ModuleDetailFragment.class.getSimpleName();
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.account_details, container, false);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         Intent intent = getActivity().getIntent();
         if (getFragmentManager().findFragmentById(R.id.list_frag) != null)
             intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
         Bundle extras = intent.getExtras();
         mRowId = (String) intent.getStringExtra(Util.ROW_ID);
         mSugarBeanId = (String) intent.getStringExtra(RestUtilConstants.BEAN_ID);
         mModuleName = "Contacts";
         if (extras != null)
             mModuleName = extras.getString(RestUtilConstants.MODULE_NAME);
 
         mDbHelper = new DatabaseHelper(getActivity().getBaseContext());
         if (intent.getData() == null) {
             if (mRowId == null)
                 mRowId = "1";
             mUri = Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId);
         }
         mSelectFields = mDbHelper.getModuleProjections(mModuleName);
         // mCursor = getContentResolver().query(getIntent().getData(),
         // mSelectFields, null, null,
         // mDbHelper.getModuleSortOrder(mModuleName));
         // startManagingCursor(mCursor);
         // setContents();
 
         mRelationshipModules = mDbHelper.getModuleRelationshipItems(mModuleName);
 
         // ListView listView = (ListView) findViewById(android.R.id.list);
         // listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
         // {
         // @Override
         // public void onItemClick(AdapterView<?> arg0, View view, int position,
         // long id) {
         // if (Log.isLoggable(LOG_TAG, Log.INFO)) {
         // Log.i(LOG_TAG, "clicked on " + mRelationshipModules[position]);
         // }
         // openListScreen(mRelationshipModules[position]);
         // }
         // });
 
         /*
          * RelationshipAdapter adapter = new RelationshipAdapter(this);
          * adapter.setRelationshipArray(mRelationshipModules); listView.setAdapter(adapter);
          */
         mTask = new LoadContentTask();
         mTask.execute(null, null, null);
     }
 
     /**
      * <p>
      * openListScreen
      * </p>
      * 
      * @param moduleName
      *            a {@link java.lang.String} object.
      */
     protected void openListScreen(String moduleName) {
         // if (mModuleName.equals("Accounts")) {
         Intent detailIntent = new Intent(ModuleDetailFragment.this.getActivity(), ModulesActivity.class);
         if (mDbHelper == null)
             mDbHelper = new DatabaseHelper(getActivity().getBaseContext());
         Uri uri = Uri.withAppendedPath(mDbHelper.getModuleUri(mModuleName), mRowId);
         uri = Uri.withAppendedPath(uri, moduleName);
         detailIntent.setData(uri);
         detailIntent.putExtra(RestUtilConstants.MODULE_NAME, moduleName);
         detailIntent.putExtra(RestUtilConstants.BEAN_ID, mSugarBeanId);
         startActivity(detailIntent);
         // } else {
         // Toast.makeText(this, "Not yet supported!",
         // Toast.LENGTH_SHORT).show();
         // }
     }
 
     /** {@inheritDoc} */
     @Override
     public void onPause() {
         super.onPause();
         if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
             mTask.cancel(true);
         }
     }
 
     /** {@inheritDoc} */
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the currently selected menu XML resource.
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.details_activity_menu, menu);
 
         SubMenu relationshipMenu = menu.addSubMenu(1, R.string.related, 0, getString(R.string.related));
         relationshipMenu.setIcon(R.drawable.menu_related);
         if (mRelationshipModules.length > 0) {
             for (int i = 0; i < mRelationshipModules.length; i++) {
                 relationshipMenu.add(0, Menu.FIRST + i, 0, mRelationshipModules[i]);
             }
         } else {
             menu.setGroupEnabled(1, false);
         }
 
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.home:
             Intent myIntent = new Intent(ModuleDetailFragment.this.getActivity(), DashboardActivity.class);
             ModuleDetailFragment.this.startActivity(myIntent);
             return true;
         case R.string.related:
             return true;
         default:
             if (Log.isLoggable(LOG_TAG, Log.INFO)) {
                 Log.i(LOG_TAG, "item id : " + item.getItemId());
             }
             openListScreen(mRelationshipModules[item.getItemId() - 1]);
             return true;
         }
     }
 
     class LoadContentTask extends AsyncTask<Object, Object, Object> {
 
         int staticRowsCount;
 
         LoadContentTask() {
             mDetailsTable = (ViewGroup) getActivity().findViewById(R.id.accountDetalsTable);
 
             staticRowsCount = mDetailsTable.getChildCount();
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             TextView tv = (TextView) getActivity().findViewById(R.id.headerText);
            // TODO - cleanup
            if (tv != null)
                tv.setText(String.format(getString(R.string.detailsHeader), mModuleName));
 
             mProgressDialog = ViewUtil.getProgressDialog(ModuleDetailFragment.this.getActivity(), getString(R.string.loading), true);
             mProgressDialog.show();
         }
 
         @Override
         protected void onProgressUpdate(Object... values) {
             super.onProgressUpdate(values);
 
             switch ((Integer) values[0]) {
 
             case HEADER:
                 TextView titleView = (TextView) values[2];
                 // TODO
                 if (titleView != null)
                     titleView.setText((String) values[3]);
                 break;
 
             case STATIC_ROW:
                 ViewGroup detailRow = (ViewGroup) values[2];
                 detailRow.setVisibility(View.VISIBLE);
 
                 TextView labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 TextView valueView = (TextView) values[5];
                 final String value = (String) values[6];
                 valueView.setText(value);
 
                 // handle the map
                 String fieldName = (String) values[1];
                 if (ModuleFields.SHIPPING_ADDRESS_COUNTRY.equals(fieldName)
                                                 || ModuleFields.BILLING_ADDRESS_COUNTRY.equals(fieldName)) {
                     if (!TextUtils.isEmpty(value)) {
                         valueView.setLinksClickable(true);
                         valueView.setClickable(true);
 
                         SpannableString spannableString = new SpannableString(value);
                         spannableString.setSpan(new InternalURLSpan(new OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 Log.i(LOG_TAG, "trying to locate - " + value);
                                 Uri uri = Uri.parse("geo:0,0?q=" + URLEncoder.encode(value));
                                 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                 intent.setData(uri);
                                 startActivity(Intent.createChooser(intent, getString(R.string.showAddressMsg)));
                             }
                         }), 0, value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
                         valueView.setText(spannableString);
 
                         // for trackball movement
                         MovementMethod m = valueView.getMovementMethod();
                         if ((m == null) || !(m instanceof LinkMovementMethod)) {
                             if (valueView.getLinksClickable()) {
                                 valueView.setMovementMethod(LinkMovementMethod.getInstance());
                             }
                         }
                     }
                 }
 
                 break;
 
             case DYNAMIC_ROW:
                 detailRow = (ViewGroup) values[2];
                 detailRow.setVisibility(View.VISIBLE);
 
                 labelView = (TextView) values[3];
                 labelView.setText((String) values[4]);
                 valueView = (TextView) values[5];
                 final String value2 = (String) values[6];
                 valueView.setText(value2);
 
                 // handle the map
                 fieldName = (String) values[1];
                 if (ModuleFields.SHIPPING_ADDRESS_COUNTRY.equals(fieldName)
                                                 || ModuleFields.BILLING_ADDRESS_COUNTRY.equals(fieldName)) {
                     if (!TextUtils.isEmpty(value2)) {
                         valueView.setLinksClickable(true);
                         valueView.setClickable(true);
 
                         SpannableString spannableString = new SpannableString(value2);
                         spannableString.setSpan(new InternalURLSpan(new OnClickListener() {
                             @Override
                             public void onClick(View v) {
                                 Log.i(LOG_TAG, "trying to locate - " + value2);
                                 Uri uri = Uri.parse("geo:0,0?q=" + URLEncoder.encode(value2));
                                 Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                 intent.setData(uri);
                                 startActivity(Intent.createChooser(intent, getString(R.string.showAddressMsg)));
                             }
                         }), 0, value2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
                         valueView.setText(spannableString);
 
                         // for trackball movement
                         MovementMethod m = valueView.getMovementMethod();
                         if ((m == null) || !(m instanceof LinkMovementMethod)) {
                             if (valueView.getLinksClickable()) {
                                 valueView.setMovementMethod(LinkMovementMethod.getInstance());
                             }
                         }
                     }
                 }
 
                 mDetailsTable.addView(detailRow);
                 break;
             }
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             try {
                 mCursor = getActivity().getContentResolver().query(mUri, mSelectFields, null, null, mDbHelper.getModuleSortOrder(mModuleName));
             } catch (Exception e) {
                 Log.e(LOG_TAG, e.getMessage(), e);
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
 
            if (mCursor.getCount() > 0)
                setContents();
            else {

            }
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
                 break;
             default:
 
             }
 
             mProgressDialog.cancel();
         }
 
         private void setContents() {
 
             String[] detailsProjection = mSelectFields;
 
             if (mDbHelper == null)
                 mDbHelper = new DatabaseHelper(getActivity().getBaseContext());
 
             TextView textViewForTitle = (TextView) getActivity().findViewById(R.id.accountName);
             String title = "";
             List<String> titleFields = Arrays.asList(mDbHelper.getModuleListSelections(mModuleName));
 
             if (!isCancelled())
                 mCursor.moveToFirst();
 
             LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
             List<String> billingAddressGroup = mDbHelper.getBillingAddressGroup();
             List<String> shippingAddressGroup = mDbHelper.getShippingAddressGroup();
             List<String> durationGroup = mDbHelper.getDurationGroup();
 
             String value = "";
             Map<String, ModuleField> fieldNameVsModuleField = mDbHelper.getModuleFields(mModuleName);
             Map<String, String> fieldsExcludedForDetails = mDbHelper.getFieldsExcludedForDetails();
 
             // LinearLayout tableRow =
             // (LinearLayout)inflater.inflate(R.layout.table_row, null);
 
             int rowsCount = 0;
             for (int i = 0; i < detailsProjection.length; i++) {
                 // if the task gets cancelled
                 if (isCancelled())
                     break;
 
                 String fieldName = detailsProjection[i];
 
                 // if the field name is excluded in details screen, skip it
                 if (fieldsExcludedForDetails.containsKey(fieldName)) {
                     continue;
                 }
 
                 int columnIndex = mCursor.getColumnIndex(fieldName);
                 if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
                     Log.d(LOG_TAG, "Col:" + columnIndex + " moduleName : " + mModuleName
                                                     + " fieldName : " + fieldName);
                 }
 
                 String tempValue = mCursor.getString(columnIndex);
 
                 // get the attributes of the moduleField
                 ModuleField moduleField = fieldNameVsModuleField.get(fieldName);
 
                 if (moduleField != null) {
                     ViewGroup tableRow;
                     TextView textViewForLabel;
                     TextView textViewForValue;
                     // first two columns in the detail projection are ROW_ID and
                     // BEAN_ID
                     if (staticRowsCount > rowsCount) {
                         tableRow = (ViewGroup) mDetailsTable.getChildAt(rowsCount);
                         textViewForLabel = (TextView) tableRow.getChildAt(0);
                         textViewForValue = (TextView) tableRow.getChildAt(1);
                     } else {
                         tableRow = (ViewGroup) inflater.inflate(R.layout.table_row, null);
                         textViewForLabel = (TextView) tableRow.getChildAt(0);
                         textViewForValue = (TextView) tableRow.getChildAt(1);
                     }
 
                     // set the title
                     if (titleFields.contains(fieldName) && tempValue != null) {
                         title = title + tempValue + " ";
                         publishProgress(HEADER, fieldName, textViewForTitle, title);
                         continue;
                     }
 
                     String label = moduleField.getLabel();
 
                     // check for the billing and shipping address groups only if
                     // the module is
                     // 'Accounts'
                     if (Util.ACCOUNTS.equals(mModuleName)) {
                         if (billingAddressGroup.contains(fieldName)) {
                             if (fieldName.equals(ModuleFields.BILLING_ADDRESS_STREET)) {
                                 // First field in the group
                                 value = (!TextUtils.isEmpty(tempValue)) ? tempValue + ", " : "";
                                 continue;
                             } else if (fieldName.equals(ModuleFields.BILLING_ADDRESS_COUNTRY)) {
                                 // last field in the group
 
                                 value = value + (!TextUtils.isEmpty(tempValue) ? tempValue : "");
                                 label = getActivity().getBaseContext().getString(R.string.billing_address);
                             } else {
                                 value = value
                                                                 + (!TextUtils.isEmpty(tempValue) ? tempValue
                                                                                                 + ", "
                                                                                                 : "");
                                 continue;
                             }
                         } else if (shippingAddressGroup.contains(fieldName)) {
                             if (fieldName.equals(ModuleFields.SHIPPING_ADDRESS_STREET)) {
                                 // First field in the group
                                 value = (!TextUtils.isEmpty(tempValue)) ? tempValue + ", " : "";
                                 continue;
                             } else if (fieldName.equals(ModuleFields.SHIPPING_ADDRESS_COUNTRY)) {
                                 // Last field in the group
 
                                 value = value + (!TextUtils.isEmpty(tempValue) ? tempValue : "");
                                 label = getActivity().getBaseContext().getString(R.string.shipping_address);
                             } else {
 
                                 value = value
                                                                 + (!TextUtils.isEmpty(tempValue) ? tempValue
                                                                                                 + ", "
                                                                                                 : "");
                                 continue;
                             }
                         } else {
                             value = tempValue;
                         }
                     } else if (durationGroup.contains(fieldName)) {
                         if (fieldName.equals(ModuleFields.DURATION_HOURS)) {
                             // First field in the group
                             value = (!TextUtils.isEmpty(tempValue)) ? tempValue + "hr " : "";
                             continue;
                         } else if (fieldName.equals(ModuleFields.DURATION_MINUTES)) {
                             // Last field in the group
                             value = value
                                                             + (!TextUtils.isEmpty(tempValue) ? tempValue
                                                                                             + "mins "
                                                                                             : "");
                             label = getActivity().getBaseContext().getString(R.string.duration);
                         }
                     } else {
                         value = tempValue;
                     }
 
                     if (moduleField.getType().equals("phone"))
                         textViewForValue.setAutoLinkMask(Linkify.PHONE_NUMBERS);
 
                     int command = staticRowsCount < rowsCount ? DYNAMIC_ROW : STATIC_ROW;
 
                     if (!TextUtils.isEmpty(value)) {
                         publishProgress(command, fieldName, tableRow, textViewForLabel, label, textViewForValue, value);
                     } else {
                         publishProgress(command, fieldName, tableRow, textViewForLabel, label, textViewForValue, getString(R.string.notAvailable));
                     }
 
                     rowsCount++;
                 } else {
                     // module fields is null
                 }
 
             }
         }
     }
 
     static class InternalURLSpan extends ClickableSpan {
         OnClickListener mListener;
 
         public InternalURLSpan(OnClickListener listener) {
             super();
             mListener = listener;
         }
 
         @Override
         public void onClick(View widget) {
             Log.i("ModuleDetailFragment", "InternalURLSpan onClick");
             mListener.onClick(widget);
         }
    }
 }
