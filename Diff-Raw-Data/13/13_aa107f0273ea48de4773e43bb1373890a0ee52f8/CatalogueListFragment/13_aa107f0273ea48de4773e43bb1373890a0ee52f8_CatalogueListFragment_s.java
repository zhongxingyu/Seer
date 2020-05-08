 package by.bsu.courseproject.ui;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.database.DatabaseUtilsCompat;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.TextView;
 import by.bsu.courseproject.R;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import static by.bsu.courseproject.db.DBConstants.Columns;
 
 
 public class CatalogueListFragment extends ListFragment
     implements LoaderManager.LoaderCallbacks<Cursor>, TextWatcher {
 
   public static final String ARG_PROJECTION = "PROJECTION";
   public static final String ARG_FILTER_COLUMNS = "FILTER_COLUMNS";
   public static final String ARG_ADAPTER_FROM = "ADAPTER_FROM";
   public static final String ARG_ADAPTER_TO = "ADAPTER_TO";
 
   private static final int IDM_DELETE = 10;
 
 
   public static final int ALL = 0;
   public static final int TODAY = 1;
   public static final int WEEK = 2;
   public static final int MONTH = 3;
 
   private int mFilter = 0;
   public static final String FILTER = "FILTER";
 
   SimpleCursorAdapter mAdapter;
   String mCurFilter;
   Intent data = null;
   Uri contentURI = null;
 
 
   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
     Intent data = getActivity().getIntent();
 
     super.onActivityCreated(savedInstanceState);
 
     setEmptyText(this.getString(R.string.label_no_data));
 
 
     String[] from;
     from = data.getStringArrayExtra(ARG_ADAPTER_FROM);
 
     int[] to = data.getIntArrayExtra(ARG_ADAPTER_TO);
 
     if (data.getIntExtra(CatalogueFragment.FROM_LIST, -1) != -1 && data.getIntExtra(CatalogueFragment.FROM_LIST, -1) != CatalogueList.PROJECT) {
       mAdapter = new SimpleCursorAdapter(getActivity(),
                                          R.layout.man_item, null,
                                          from, to, 0);
     }
     if (data.getIntExtra(CatalogueFragment.FROM_LIST, -1) == CatalogueList.PROJECT) {
       mAdapter = new SimpleCursorAdapter(getActivity(),
                                          android.R.layout.simple_list_item_2, null,
                                          from, to, 0);
       getActivity().setTitle("Проекты (все)");
 
       setViewBinder();
 
     }
 
     setListAdapter(mAdapter);
 
     TextView filter = (TextView) getActivity().findViewById(R.id.editFilter);
     if (filter != null) filter.addTextChangedListener(this);
     registerForContextMenu(getListView());
getListView().setBackgroundColor(getResources().getColor(R.color.gray));
   }
 
 
   private void setViewBinder() {
 
     mAdapter.setViewBinder(new ViewBinder() {
 
       public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 
         if (columnIndex == cursor.getColumnIndex(Columns.PROJECT_PROJECTNAME)) {
           Calendar calendar = Calendar.getInstance();
           String datePattern = "yyyyMMdd";
           SimpleDateFormat sdf = new SimpleDateFormat();
           sdf.applyPattern(datePattern);
           Date date = new Date(calendar.getTimeInMillis());
           String today = sdf.format(date);
 
           String dateDB = cursor.getString(cursor.getColumnIndex(Columns.PROJECT_PROJECTDUEDATE));
           ((TextView) view).setText(cursor.getString(columnIndex));
           View parentView = (View) view.getParent();
           if (dateDB != null && dateDB.compareTo(today) < 0) {
            parentView.setBackgroundColor(getResources().getColor(R.color.red));
           } else {
             parentView.setBackgroundColor(0);
           }
           return true;
         }
         return false;
 
       }
     });
   }
 
   @Override
   public void onResume() {
     super.onResume();
     setListShown(false);
     getLoaderManager().restartLoader(0, null, this);
   }
 
   public void afterTextChanged(Editable s) {
     mCurFilter = s.toString().isEmpty() ? null : s.toString();
     getLoaderManager().restartLoader(0, null, this);
 
   }
 
   public void beforeTextChanged(CharSequence s, int start, int count,
                                 int after) {
   }
 
   public void onTextChanged(CharSequence s, int start, int before, int count) {
   }
 
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
     data = getActivity().getIntent();
     contentURI = data.getData();
     String[] projection = data.getStringArrayExtra(ARG_PROJECTION);
     String[] filterColumns = data.getStringArrayExtra(ARG_FILTER_COLUMNS);
 
     String select = "";
     String tempSelect = "";
     int days = 0;
 
     if (data.getExtras().getInt(CatalogueFragment.FROM_LIST, -1) == CatalogueList.PROJECT) {
 
       Calendar calendar = Calendar.getInstance();
       String datePattern = "yyyyMMdd";
       SimpleDateFormat sdf = new SimpleDateFormat();
       sdf.applyPattern(datePattern);
       Date date = new Date(calendar.getTimeInMillis());
       String today = sdf.format(date);
 
       switch (mFilter) {
 
       case TODAY:
         tempSelect = Columns.PROJECT_PROJECTDUEDATE + " = " + today;
         break;
       case WEEK:
         days = 7;
         calendar = Calendar.getInstance();
         calendar.add(Calendar.DAY_OF_MONTH, days);
         date = new Date(calendar.getTimeInMillis());
         sdf.applyPattern(datePattern);
         String week = sdf.format(date);
         tempSelect = Columns.PROJECT_PROJECTDUEDATE
                      + " >= " + today
                      + " AND " + Columns.PROJECT_PROJECTDUEDATE
                      + " < " + week;
         tempSelect = DatabaseUtilsCompat.concatenateWhere(tempSelect, "PROJECTDUEDATE IS NOT NULL");
         break;
       case MONTH:
         days = 31;
         calendar = Calendar.getInstance();
         calendar.add(Calendar.DAY_OF_MONTH, days);
         date = new Date(calendar.getTimeInMillis());
         sdf.applyPattern(datePattern);
         String month = sdf.format(date);
         tempSelect = Columns.PROJECT_PROJECTDUEDATE
                      + " >= " + today
                      + " AND " + Columns.PROJECT_PROJECTDUEDATE
                      + " < " + month;
         tempSelect = DatabaseUtilsCompat.concatenateWhere(tempSelect, "PROJECTDUEDATE IS NOT NULL");
         break;
       }
     }
 
     if (mCurFilter != null) {
       select = "(";
       for (int i = 0; i < filterColumns.length; i++) {
         if (i != 0) {
           select += " OR ";
         }
         select += filterColumns[i] + " LIKE '%" + mCurFilter + "%'";
       }
       select += ")";
     } else {
       select = null;
     }
 
     if (!tempSelect.equals("")) {
       select = DatabaseUtilsCompat.concatenateWhere(select, tempSelect);
     }
 
     String sel;
 
     if (data.getExtras().getInt(CatalogueFragment.FROM_LIST, -1) == CatalogueList.INVESTOR) {
       sel = Columns.PERSON_DISCRIMINATOR + "=\"I\"";
       select = (select == null) ? sel : select + " AND " + sel;
     }
 
     if (data.getExtras().getInt(CatalogueFragment.FROM_LIST, -1) == CatalogueList.CUSTOMER) {
       sel = Columns.PERSON_DISCRIMINATOR + "=\"C\"";
       select = (select == null) ? sel : select + " AND " + sel;
     }
 
     if (data.getExtras().getInt(CatalogueFragment.FROM_LIST, -1) == CatalogueList.EMPLOYEE) {
       sel = Columns.PERSON_DISCRIMINATOR + "=\"E\"";
       select = (select == null) ? sel : select + " AND " + sel;
     }
 
     return new CursorLoader(getActivity(), contentURI,
                             projection, select, null,
                             filterColumns[0] + " ASC");
   }
 
   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
     mAdapter.swapCursor(arg1);
 
     if (isResumed()) {
       setListShown(true);
     } else {
       setListShownNoAnimation(true);
     }
   }
 
   public void onLoaderReset(Loader<Cursor> arg0) {
     mAdapter.swapCursor(null);
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View v,
                                   ContextMenuInfo menuInfo) {
 
     Cursor cursor = getActivity().getContentResolver().query(contentURI,
                                                              new String[]{Columns._ID}, null, null, null);
     if (cursor.moveToFirst()) {
       menu.setHeaderIcon(android.R.drawable.ic_menu_more);
       switch (data.getIntExtra(CatalogueFragment.FROM_LIST, -1)) {
       case CatalogueList.PROJECT:
         menu.setHeaderTitle(R.string.label_project);
         break;
       case CatalogueList.EMPLOYEE:
         menu.setHeaderTitle(R.string.label_employee);
         break;
       case CatalogueList.CUSTOMER:
         menu.setHeaderTitle(R.string.label_customer);
         break;
       case CatalogueList.INVESTOR:
         menu.setHeaderTitle(R.string.label_investor);
         break;
       }
 
       menu.add(Menu.NONE, IDM_DELETE, Menu.NONE, R.string.label_delete);
     }
   }
 
   public boolean changeFilter(int filter) {
     mFilter = filter;
     setListShown(false);
 
     // Prepare the loader.  Either re-connect with an existing one,
     // or start a new one.
     getLoaderManager().restartLoader(0, null, this);
 
     return true;
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     if (item.getItemId() == IDM_DELETE) {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
       Uri uri = ContentUris.withAppendedId(contentURI, info.id);
       getActivity().getContentResolver().delete(uri, null, null);
       return true;
     }
     return super.onContextItemSelected(item);
   }
 
 
   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
     super.onListItemClick(l, v, position, id);
 
     // Constructs a new URI from the incoming URI and the row ID
     Uri uri = ContentUris.withAppendedId(getActivity().getIntent().getData(), id);
     Intent intent = new Intent();
     intent.setData(uri);
 
     if ((Intent.ACTION_PICK).equals(getActivity().getIntent().getAction())) {
       getActivity().setResult(Activity.RESULT_OK, intent);
       getActivity().finish();
     } else {
       switch (data.getIntExtra(CatalogueFragment.FROM_LIST, -1)) {
       case CatalogueList.PROJECT:
         intent.setClass(getActivity(), Project.class);
         break;
       case CatalogueList.EMPLOYEE:
         intent.setClass(getActivity(), Employee.class);
         break;
       case CatalogueList.CUSTOMER:
         intent.putExtra(CustomerInvestor.INV, CustomerInvestor.cust);
         intent.setClass(getActivity(), CustomerInvestor.class);
         break;
       case CatalogueList.INVESTOR:
         intent.putExtra(CustomerInvestor.INV, CustomerInvestor.inv);
         intent.setClass(getActivity(), CustomerInvestor.class);
         break;
       }
       intent.putExtra(Employee.FROM_LIST, Employee.ITEM);
       startActivity(intent);
     }
   }
 
 }
 
 
 
 
 
 
 
