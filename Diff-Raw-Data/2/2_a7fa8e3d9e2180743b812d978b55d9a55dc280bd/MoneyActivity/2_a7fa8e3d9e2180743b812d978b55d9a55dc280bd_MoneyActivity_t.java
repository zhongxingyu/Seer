 package de.winterberg.android.money;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 /**
  * Main activity shows each monetary category as a list.
  *
  * @author Benjamin Winterberg
  */
 public class MoneyActivity extends ListActivity implements AmountDaoAware {
     static final String TAG = "MoneyActivity";
     static final String KEY_CATEGORY = "CATEGORY_NAME";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.category);
         initListAdapter();
         initItemListeners();
     }
 
     private void initListAdapter() {
         List<String> categories = getAmountDao().findDistinctCategories();
         ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.category_item, categories);
         setListAdapter(arrayAdapter);
     }
 
     private void initItemListeners() {
         getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 String category = ((TextView) view).getText().toString();
                 onCategoryClick(category);
             }
         });
         getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                 final String category = ((TextView) view).getText().toString();
                 openRemoveCategoryDialog(category);
                 return true;
             }
         });
     }
 
     private void openRemoveCategoryDialog(final String category) {
         DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 switch (which) {
                     case DialogInterface.BUTTON_POSITIVE:
                         doRemove(category);
                         break;
                 }
             }
         };
 
         new AlertDialog.Builder(MoneyActivity.this)
                 .setTitle(R.string.remove_category_label)
                 .setMessage(R.string.remove_category_confirm)
                 .setPositiveButton(R.string.ok, dialogClickListener)
                 .setNegativeButton(R.string.cancel, dialogClickListener)
                 .show();
     }
 
     @SuppressWarnings("unchecked")
     private void doRemove(String category) {
         getAmountDao().removeAll(category);
         ArrayAdapter<String> listAdapter = (ArrayAdapter<String>) getListAdapter();
         listAdapter.remove(category);
     }
 
     private void onCategoryClick(String category) {
         Log.d(TAG, "onItemClick: " + category);
         Intent intent = new Intent(getApplicationContext(), CategoryTabActivity.class);
         intent.putExtra(KEY_CATEGORY, category);
         startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.add_category:
                 openAddCategoryDialog();
                 return true;
             case R.id.settings:
                 startActivity(new Intent(this, PrefsActivity.class));
                 return true;
             case R.id.about:
                 startActivity(new Intent(this, AboutActivity.class));
                 return true;
             case R.id.exit:
                 finish();
                 return true;
         }
         return false;
     }
 
     public void onAddCategoryButtonBlick(View view) {
         openAddCategoryDialog();
     }
 
     private void openAddCategoryDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this)
                 .setMessage(R.string.add_category_description)
                 .setTitle(R.string.add_category_label);
 
         final EditText editText = new EditText(this);
         editText.setSingleLine();
 
         builder.setView(editText);
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialogInterface, int i) {
                 String category = editText.getText().toString();
                 if (exists(category)) {
                     Toast.makeText(getApplicationContext(), R.string.category_already_exists, Toast.LENGTH_LONG).show();
                     return;
                 }
                 addCategory(category);
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialogInterface, int i) {
                 // do nothing
             }
         });
 
         builder.show();
     }
 
     @SuppressWarnings("unchecked")
     private void addCategory(String category) {
        getAmountDao().save(category, "0.0", AmountActivity.ACTION_PLUS, new BigDecimal(0.0));
         ArrayAdapter<String> listAdapter = (ArrayAdapter<String>) getListAdapter();
         listAdapter.add(category);
     }
 
     @SuppressWarnings("unchecked")
     private boolean exists(String category) {
         ArrayAdapter<String> listAdapter = (ArrayAdapter<String>) getListAdapter();
         return listAdapter.getPosition(category) > -1;
     }
 
     public AmountDao getAmountDao() {
         MoneyApplication application = (MoneyApplication) getApplication();
         return application.getAmountDao();
     }
 }
