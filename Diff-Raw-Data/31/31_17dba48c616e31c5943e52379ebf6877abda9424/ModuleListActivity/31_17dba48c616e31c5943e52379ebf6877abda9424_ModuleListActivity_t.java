 package com.imaginea.android.sugarcrm;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import com.imaginea.android.sugarcrm.provider.DatabaseHelper;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Recent;
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.Util;
 
 /**
  * ModuleListActivity, lists the view projections for all the modules.
  * 
  * @author chander
  */
 public class ModuleListActivity extends ListActivity {
 
     private ListView mListView;
 
     private View mEmpty;
 
     private View mListFooterView;
 
     private TextView mListFooterText;
 
     private View mListFooterProgress;
 
     private boolean mBusy = false;
 
     private String mModuleName;
 
     private Uri mModuleUri;
 
     // private boolean mStopLoading = false;
 
     private Uri mIntentUri;
 
     private int mCurrentSelection;
 
     // we don't make this final as we may want to use the sugarCRM value
     // dynamically, but prevent
     // others from modiying anyway
     // private static int mMaxResults = 20;
 
     private DatabaseHelper mDbHelper;
 
     private GenericCursorAdapter mAdapter;
 
     private static final int DIALOG_SORT_CHOICE = 1;
 
     private String[] mModuleFields;
 
     private String[] mModuleFieldsChoice;
 
     private int mSortColumnIndex;
 
     private int MODE = Util.LIST_MODE;
 
     private String mSelections = ModuleFields.DELETED + "=?";
 
     private String[] mSelectionArgs = new String[] { Util.EXCLUDE_DELETED_ITEMS };
 
     private SugarCrmApp app;
 
     public final static String LOG_TAG = ModuleListActivity.class.getSimpleName();
 
     /** {@inheritDoc} */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.common_list);
 
         mDbHelper = new DatabaseHelper(getBaseContext());
         app = (SugarCrmApp) getApplication();
 
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         mModuleName = Util.CONTACTS;
         if (extras != null) {
             mModuleName = extras.getString(RestUtilConstants.MODULE_NAME);
         }
 
         // If the list is a list of related items, hide the filterImage and
         // allItems image
         if (intent.getData() != null && intent.getData().getPathSegments().size() >= 3) {
             findViewById(R.id.filterImage).setVisibility(View.GONE);
             findViewById(R.id.allItems).setVisibility(View.GONE);
         }
 
         TextView tv = (TextView) findViewById(R.id.headerText);
         tv.setText(mModuleName);
 
         mListView = getListView();
 
         mIntentUri = intent.getData();
         // mListView.setOnScrollListener(this);
         mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                 Log.e(LOG_TAG, "item clicked");
                 addToRecent(position);
                 openDetailScreen(position);
             }
         });
 
         // button code in the layout - 1.6 SDK feature to specify onClick
         mListView.setItemsCanFocus(true);
         mListView.setFocusable(true);
         mEmpty = findViewById(R.id.empty);
         mListView.setEmptyView(mEmpty);
         registerForContextMenu(getListView());
 
         if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
             Log.d(LOG_TAG, "ModuleName:-->" + mModuleName);
         }
 
         mModuleUri = mDbHelper.getModuleUri(mModuleName);
         if (intent.getData() == null) {
             intent.setData(mModuleUri);
         }
         // Perform a managed query. The Activity will handle closing and
         // requerying the cursor
         // when needed.
         // TODO - optimize this, if we sync up a dataset, then no need to run
         // detail projection
         // here, just do a list projection
         Cursor cursor = managedQuery(getIntent().getData(), mDbHelper.getModuleProjections(mModuleName), mSelections, mSelectionArgs, getSortOrder());
 
         // CRMContentObserver observer = new CRMContentObserver()
         // cursor.registerContentObserver(observer);
         String[] moduleSel = mDbHelper.getModuleListSelections(mModuleName);
         if (moduleSel.length >= 2)
             mAdapter = new GenericCursorAdapter(this, R.layout.contact_listitem, cursor, moduleSel, new int[] {
                     android.R.id.text1, android.R.id.text2 });
         else
             mAdapter = new GenericCursorAdapter(this, R.layout.contact_listitem, cursor, moduleSel, new int[] { android.R.id.text1 });
         setListAdapter(mAdapter);
         // make the list filterable using the keyboard
         mListView.setTextFilterEnabled(true);
 
         TextView tv1 = (TextView) (mEmpty.findViewById(R.id.mainText));
 
         if (mAdapter.getCount() == 0) {
             mListView.setVisibility(View.GONE);
             mEmpty.findViewById(R.id.progress).setVisibility(View.VISIBLE);
             tv1.setVisibility(View.VISIBLE);
             if (mIntentUri != null) {
                 tv1.setText("No " + mModuleName + " found");
             }
         } else {
             mEmpty.findViewById(R.id.progress).setVisibility(View.VISIBLE);
             tv1.setVisibility(View.GONE);
         }
 
         mListFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list_item_footer, mListView, false);
         getListView().addFooterView(mListFooterView);
         mListFooterText = (TextView) findViewById(R.id.status);
 
         mListFooterProgress = mListFooterView.findViewById(R.id.progress);
     }
 
     /**
      * GenericCursorAdapter
      */
     private final class GenericCursorAdapter extends SimpleCursorAdapter implements Filterable {
 
         private int realoffset = 0;
 
         private int limit = 20;
 
         private ContentResolver mContent;
 
         public GenericCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
             super(context, layout, c, from, to);
             mContent = context.getContentResolver();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
 
             View v = super.getView(position, convertView, parent);
             int count = getCursor().getCount();
             Log.d(LOG_TAG, "Get Item" + getItemId(position));
             if (!mBusy && position != 0 && position == count - 1) {
                 mBusy = true;
                 realoffset += count;
                 // Uri uri = getIntent().getData();
                 // TODO - fix this, this is no longer used
                 Uri newUri = Uri.withAppendedPath(Contacts.CONTENT_URI, realoffset + "/" + limit);
                 Log.d(LOG_TAG, "Changing cursor:" + newUri.toString());
                 final Cursor cursor = managedQuery(newUri, Contacts.LIST_PROJECTION, null, null, Contacts.DEFAULT_SORT_ORDER);
                 CRMContentObserver observer = new CRMContentObserver(new Handler() {
 
                     @Override
                     public void handleMessage(Message msg) {
                         super.handleMessage(msg);
                         Log.d(LOG_TAG, "Changing cursor: in handler");
                         // if (cursor.getCount() < mMaxResults)
                         // mStopLoading = true;
                         changeCursor(cursor);
                         mListFooterText.setVisibility(View.GONE);
                         mListFooterProgress.setVisibility(View.GONE);
                         mBusy = false;
                     }
                 });
                 cursor.registerContentObserver(observer);
             }
             if (mBusy) {
                 mListFooterProgress.setVisibility(View.VISIBLE);
                 mListFooterText.setVisibility(View.VISIBLE);
                 mListFooterText.setText("Loading...");
                 // Non-null tag means the view still needs to load it's data
                 // text.setTag(this);
             }
             return v;
         }
 
         @Override
         public String convertToString(Cursor cursor) {
             Log.i(LOG_TAG, "convertToString : " + cursor.getString(2));
             return cursor.getString(2);
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
                 buffer.append(mDbHelper.getModuleListSelections(mModuleName)[0]);
                 buffer.append(") GLOB ?");
                 args = new String[] { constraint.toString().toUpperCase() + "*" };
             }
 
             return mContent.query(mDbHelper.getModuleUri(mModuleName), mDbHelper.getModuleListProjections(mModuleName), buffer == null ? null
                                             : buffer.toString(), args, mDbHelper.getModuleSortOrder(mModuleName));
         }
     }
 
     /**
      * opens the Detail Screen
      * 
      * @param position
      */
     void openDetailScreen(int position) {
         Intent detailIntent = new Intent(ModuleListActivity.this, ModuleDetailsActivity.class);
 
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
         Log.d(LOG_TAG, "beanId:" + cursor.getString(1));
         detailIntent.putExtra(Util.ROW_ID, cursor.getString(0));
         detailIntent.putExtra(RestUtilConstants.BEAN_ID, cursor.getString(1));
         detailIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
 
         startActivity(detailIntent);
     }
 
     /**
      * opens the Edit Screen
      * 
      * @param position
      */
     private void openEditScreen(int position) {
 
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
 
         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
             Log.d(LOG_TAG, "beanId:" + cursor.getString(1));

        Intent editDetailsIntent = new Intent(ModuleListActivity.this, EditDetailsActivity.class);
        editDetailsIntent.putExtra(Util.ROW_ID, cursor.getString(0));
         if (mIntentUri != null)
            editDetailsIntent.setData(Uri.withAppendedPath(mIntentUri, cursor.getString(0)));
 
        editDetailsIntent.putExtra(RestUtilConstants.BEAN_ID, cursor.getString(1));
        editDetailsIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
 
        startActivity(editDetailsIntent);
     }
 
     /**
      * deletes an item
      */
     void deleteItem() {
         Cursor cursor = (Cursor) getListAdapter().getItem(mCurrentSelection);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
 
         String beanId = cursor.getString(1);
         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
             Log.d(LOG_TAG, "beanId:" + beanId);
 
         if (mDbHelper == null)
             mDbHelper = new DatabaseHelper(getBaseContext());
 
         mModuleUri = mDbHelper.getModuleUri(mModuleName);
         Uri deleteUri = Uri.withAppendedPath(mModuleUri, cursor.getString(0));
         getContentResolver().registerContentObserver(deleteUri, false, new DeleteContentObserver(new Handler()));
         ServiceHelper.startServiceForDelete(getBaseContext(), deleteUri, mModuleName, beanId);
         // getContentResolver().delete(mModuleUri, SugarCRMContent.RECORD_ID,
         // new String[] {
         // cursor.getString(0) });
         // detailIntent.putExtra(RestUtilConstants.ID, cursor.getString(0));
         // detailIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
         // startActivity(detailIntent);
     }
 
     private static class DeleteContentObserver extends ContentObserver {
 
         public DeleteContentObserver(Handler handler) {
             super(handler);
         }
 
         @Override
         public boolean deliverSelfNotifications() {
             return super.deliverSelfNotifications();
         }
 
         @Override
         public void onChange(boolean selfChange) {
             super.onChange(selfChange);
             Log.d(LOG_TAG, "Received onCHange");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     protected void onPause() {
         super.onPause();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onSearchRequested() {
         Bundle appData = new Bundle();
         String[] modules = { mModuleName };
         appData.putString(RestUtilConstants.MODULE_NAME, mModuleName);
         appData.putStringArray(RestUtilConstants.MODULES, modules);
         appData.putInt(RestUtilConstants.OFFSET, 0);
         appData.putInt(RestUtilConstants.MAX_RESULTS, 20);
 
         startSearch(null, false, appData, false);
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         MenuHelper.onPrepareOptionsMenu(this, menu, mModuleName);
 
         // get the sort options
         // get the LIST projection
         mModuleFields = mDbHelper.getModuleListSelections(mModuleName);
         // get the module fields for the module
         Map<String, ModuleField> map = mDbHelper.getModuleFields(mModuleName);
         if (map == null) {
             Log.w(LOG_TAG, "Cannot prepare Options as Map is null for module:" + mModuleName);
             return false;
         }
         mModuleFieldsChoice = new String[mModuleFields.length];
         for (int i = 0; i < mModuleFields.length; i++) {
             // add the module field label to be displayed in the choice menu
             ModuleField modField = map.get(mModuleFields[i]);
             if (modField != null)
                 mModuleFieldsChoice[i] = modField.getLabel();
             else
                 mModuleFieldsChoice[i] = "";
             if (mModuleFieldsChoice[i].indexOf(":") > 0) {
                 mModuleFieldsChoice[i] = mModuleFieldsChoice[i].substring(0, mModuleFieldsChoice[i].length() - 1);
             }
         }
 
         if (!mModuleName.equalsIgnoreCase(Util.CONTACTS)) {
             menu.findItem(R.id.importContact).setVisible(false);
         }
 
         return super.onPrepareOptionsMenu(menu);
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         // Inflate the currently selected menu XML resource.
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.list_activity_menu, menu);
 
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.home:
             Intent myIntent = new Intent(ModuleListActivity.this, DashboardActivity.class);
             ModuleListActivity.this.startActivity(myIntent);
             return true;
         case R.id.search:
             onSearchRequested();
             return true;
         case R.id.addItem:
             myIntent = new Intent(ModuleListActivity.this, EditDetailsActivity.class);
             myIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
             Log.v(LOG_TAG, "intetnURI: " + mIntentUri);
             if (mIntentUri != null)
                 myIntent.setData(mIntentUri);
             ModuleListActivity.this.startActivity(myIntent);
             return true;
         case R.id.sort:
             showDialog(DIALOG_SORT_CHOICE);
             return true;
         case R.id.importContact:
             myIntent = new Intent(ModuleListActivity.this, EditDetailsActivity.class);
             myIntent.putExtra(RestUtilConstants.MODULE_NAME, mModuleName);
             myIntent.putExtra(Util.IMPORT_FLAG, Util.CONTACT_IMPORT_FLAG);
             if (mIntentUri != null)
                 myIntent.setData(mIntentUri);
             ModuleListActivity.this.startActivity(myIntent);
             return true;
         }
         return false;
     }
 
     /** {@inheritDoc} */
     @Override
     protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
         super.onPrepareDialog(id, dialog, args);
     }
 
     /** {@inheritDoc} */
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         super.onPrepareDialog(id, dialog);
     }
 
     /** {@inheritDoc} */
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DIALOG_SORT_CHOICE:
             Builder builder = new AlertDialog.Builder(this);
             builder.setIcon(android.R.drawable.ic_dialog_alert);
             builder.setTitle(R.string.sortBy);
 
             mSortColumnIndex = 0;
             builder.setSingleChoiceItems(mModuleFieldsChoice, 0, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     mSortColumnIndex = whichButton;
                 }
             });
             builder.setPositiveButton(R.string.ascending, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String sortOrder = mModuleFields[mSortColumnIndex] + " ASC";
                     sortList(sortOrder);
                 }
             });
             builder.setNegativeButton(R.string.descending, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
                     String sortOrder = mModuleFields[mSortColumnIndex] + " DESC";
                     sortList(sortOrder);
                 }
             });
             return builder.create();
 
         case R.string.delete:
 
             return new AlertDialog.Builder(ModuleListActivity.this).setTitle(id).setMessage(R.string.deleteAlert).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
 
                     deleteItem();
 
                 }
             }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton) {
 
                 }
             }).create();
         }
 
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         menu.setHeaderTitle(R.string.options);
         AdapterView.AdapterContextMenuInfo info;
         try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         } catch (ClassCastException e) {
             Log.e(LOG_TAG, "Bad menuInfo", e);
             return;
         }
 
         if (mDbHelper == null)
             mDbHelper = new DatabaseHelper(getBaseContext());
 
         Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
         int index = cursor.getColumnIndex(ModuleFields.CREATED_BY_NAME);
         String ownerName = cursor.getString(index);
 
         menu.add(1, R.string.view, 2, R.string.view).setEnabled(mDbHelper.isAclEnabled(mModuleName, RestUtilConstants.VIEW, ownerName));
         menu.add(2, R.string.edit, 3, R.string.edit).setEnabled(mDbHelper.isAclEnabled(mModuleName, RestUtilConstants.EDIT, ownerName));
         menu.add(3, R.string.delete, 4, R.string.delete).setEnabled(mDbHelper.isAclEnabled(mModuleName, RestUtilConstants.DELETE, ownerName));
 
         // TODO disable options based on acl actions for the module
 
         // TODO
         if (mDbHelper.getModuleField(mModuleName, ModuleFields.PHONE_WORK) != null)
             menu.add(4, R.string.call, 4, R.string.call);
         if (mDbHelper.getModuleField(mModuleName, ModuleFields.EMAIL1) != null)
             menu.add(5, R.string.email, 4, R.string.email);
 
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 
         AdapterView.AdapterContextMenuInfo info;
 
         try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         } catch (ClassCastException e) {
             Log.e(LOG_TAG, "bad menuInfo", e);
             return false;
         }
         int position = info.position;
 
         addToRecent(position);
 
         switch (item.getItemId()) {
         case R.string.view:
             openDetailScreen(position);
             return true;
 
         case R.string.edit:
             openEditScreen(position);
             return true;
 
         case R.string.delete:
             mCurrentSelection = position;
             showDialog(R.string.delete);
             return true;
 
         case R.string.call:
             callNumber(position);
             return true;
 
         case R.string.email:
             sendMail(position);
             return true;
 
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private void sortList(String sortOrder) {
         String selection = null;
         if (MODE == Util.ASSIGNED_ITEMS_MODE) {
             // TODO: get the user name from Account Manager
             String userName = SugarCrmSettings.getUsername(ModuleListActivity.this);
             selection = ModuleFields.ASSIGNED_USER_NAME + "='" + userName + "'";
         }
         Cursor cursor = managedQuery(getIntent().getData(), mDbHelper.getModuleProjections(mModuleName), selection, null, sortOrder);
         mAdapter.changeCursor(cursor);
         mAdapter.notifyDataSetChanged();
     }
 
     void addToRecent(int position) {
         ContentValues modifiedValues = new ContentValues();
         // push the selected record into recent table
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
 
         String[] moduleSel = mDbHelper.getModuleListSelections(mModuleName);
         Log.e(LOG_TAG, "Name1:" + cursor.getString(2));
         if (moduleSel.length >= 2)
             Log.e(LOG_TAG, "Name2:" + cursor.getString(3));
         // now insert into recent table
         Log.e(LOG_TAG, "Inserting:" + cursor.getString(2));
         modifiedValues.put(Recent.ACTUAL_ID, cursor.getInt(0) + "");
         modifiedValues.put(Recent.BEAN_ID, cursor.getString(1));
         modifiedValues.put(Recent.NAME_1, cursor.getString(2));
         modifiedValues.put(Recent.NAME_2, cursor.getString(3));
         modifiedValues.put(Recent.REF_MODULE_NAME, mModuleName);
         modifiedValues.put(Recent.DELETED, "0");
         Uri insertResultUri = getApplicationContext().getContentResolver().insert(Recent.CONTENT_URI, modifiedValues);
         /*
          * values.put(SugarCRMContent.SUGAR_BEAN_ID, "Sync" + UUID.randomUUID()); Uri
          * insertResultUri = mContext.getContentResolver().insert(mUri, values); // after success
          * url insertion, we set the updatedRow to 1 so we don't get a // fail msg updatedRows = 1;
          */
         Log.i(LOG_TAG, "insertResultURi - " + insertResultUri);
 
     }
 
     /**
      * <p>
      * showAssignedItems
      * </p>
      * 
      * @param view
      *            a {@link android.view.View} object.
      */
     public void showAssignedItems(View view) {
         MODE = Util.ASSIGNED_ITEMS_MODE;
         // TODO: get the user name from Account Manager
         String userName = SugarCrmSettings.getUsername(ModuleListActivity.this);
         String selection = ModuleFields.ASSIGNED_USER_NAME + "='" + userName + "'";
         Cursor cursor = managedQuery(getIntent().getData(), mDbHelper.getModuleProjections(mModuleName), selection, null, getSortOrder());
         mAdapter.changeCursor(cursor);
         mAdapter.notifyDataSetChanged();
     }
 
     /**
      * <p>
      * showAllItems
      * </p>
      * 
      * @param view
      *            a {@link android.view.View} object.
      */
     public void showAllItems(View view) {
         Cursor cursor = managedQuery(getIntent().getData(), mDbHelper.getModuleProjections(mModuleName), null, null, getSortOrder());
         mAdapter.changeCursor(cursor);
         mAdapter.notifyDataSetChanged();
     }
 
     /**
      * <p>
      * showHome
      * </p>
      * 
      * @param view
      *            a {@link android.view.View} object.
      */
     public void showHome(View view) {
         Intent homeIntent = new Intent(this, DashboardActivity.class);
         startActivity(homeIntent);
     }
 
     private String getSortOrder() {
         String sortOrder = null;
         Map<String, String> sortOrderMap = app.getModuleSortOrder(mModuleName);
         for (Entry<String, String> entry : sortOrderMap.entrySet()) {
             sortOrder = entry.getKey() + " " + entry.getValue();
         }
         return sortOrder;
     }
 
     /**
      * <p>
      * callNumber
      * </p>
      * 
      * @param position
      *            a int.
      */
     public void callNumber(int position) {
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
 
         int index = cursor.getColumnIndex(ModuleFields.PHONE_WORK);
         String number = cursor.getString(index);
         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
             Log.d(LOG_TAG, "Work number to call:" + number);
         Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
         startActivity(intent);
     }
 
     /**
      * <p>
      * sendMail
      * </p>
      * 
      * @param position
      *            a int.
      */
     public void sendMail(int position) {
         Cursor cursor = (Cursor) getListAdapter().getItem(position);
         if (cursor == null) {
             // For some reason the requested item isn't available, do nothing
             return;
         }
         // emailAddress
         int index = cursor.getColumnIndex(ModuleFields.EMAIL1);
         String emailAddress = cursor.getString(index);
         if (Log.isLoggable(LOG_TAG, Log.DEBUG))
             Log.d(LOG_TAG, "email :" + emailAddress);
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + emailAddress));
         startActivity(intent);
     }
 }
