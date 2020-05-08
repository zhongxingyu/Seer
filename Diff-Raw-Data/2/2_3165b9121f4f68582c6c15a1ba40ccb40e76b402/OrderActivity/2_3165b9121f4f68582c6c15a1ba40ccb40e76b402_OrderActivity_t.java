 package com.ben.software.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.ben.software.Order;
 import com.ben.software.R;
 import com.ben.software.db.DatabaseHelper;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class OrderActivity extends Activity {
 
     private static final String LOG_TAG = "activity.OrderActivity";
     private List<Map<String, String>> mAutoCompleteList;
     private ListView mOrderListView;
     private List<Map<String, String>> mDataList;
     private EditText mCountEditor;
     private EditText mRemarkEditor;
     private SimpleAdapter mSimpleAdapter;
 
     private long mCurrentCuisineId = -1;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setTitle(getResources().getText(R.string.order_title) + " - 17桌");
         setContentView(R.layout.order);
 
         DatabaseHelper dbHelper = new DatabaseHelper(this);
         dbHelper.clearTestData();
         dbHelper.addTestData();
         dbHelper.close();
 
         mAutoCompleteList = createAutoCompleteList();
         SimpleAdapter adapter = new AutoCompleteAdapter(this, mAutoCompleteList,
                 android.R.layout.simple_dropdown_item_1line,
                 new String[] {"cuisine"}, new int[] {android.R.id.text1}, "id", "cuisine");
 
         AutoCompleteTextView input = (AutoCompleteTextView) findViewById(R.id.orderText);
         input.setThreshold(1);
         input.setAdapter(adapter);
 
         input.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 Log.v(LOG_TAG, "AdapterView.OnItemClickListener - onItemClick position: " + position + " id: " + id);
                 mCurrentCuisineId = id;
             }
         });
 
         input.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             public void onItemSelected(AdapterView<?> parent, View view,
                     int position, long id) {
                 Log.v(LOG_TAG, "AdapterView.OnItemSelectedListener - onItemSelected position: " + position + " id: " + id);
             }
 
             public void onNothingSelected(AdapterView<?> parent) {
                 Log.v(LOG_TAG, "AdapterView.OnItemSelectedListener - onNothingSelected");
             }
         });
 
         input.addTextChangedListener(new TextWatcher() {
 
             public void onTextChanged(CharSequence aS, int aStart, int aBefore,
                     int aCount) {
                 Log.v(LOG_TAG, "TextWatcher.onTextChanged - aS: " + aS);
 //                mCurrentCuisineId = -1;
             }
 
             public void beforeTextChanged(CharSequence aS, int aStart, int aCount,
                     int aAfter) {
                 // TODO Auto-generated method stub
 
             }
 
             public void afterTextChanged(Editable aS) {
                 // TODO Auto-generated method stub
 
             }
         });
 
         input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
             public boolean onEditorAction(TextView aV, int aActionId,
                     KeyEvent aEvent) {
                 Log.v(LOG_TAG, "TextView.OnEditorActionListener - onEditorAction");
                 return false;
             }
         } );
 
         mCountEditor = (EditText) findViewById(R.id.countText);
         mRemarkEditor = (EditText) findViewById(R.id.remarkText);
 
         mDataList = new ArrayList<Map<String, String>>();
 
         mOrderListView = (ListView) findViewById(R.id.orderList);
 
         mOrderListView.addHeaderView(createListHeader());
         mSimpleAdapter = new SimpleAdapter(this, (List<Map<String, String>>) mDataList,
                 R.layout.orderlist_row, new String[] {"code", "name", "price", "count", "remark"},
                 new int[] {R.id.cuisineCodeText, R.id.cuisineNameText,
                             R.id.cuisinePriceText, R.id.cuisineCountText,
                             R.id.cuisineRemarkText});
         mOrderListView.setAdapter(mSimpleAdapter);
 
         Button addButton = (Button) findViewById(R.id.addCuisine);
         addButton.setOnClickListener( new View.OnClickListener() {
 
             public void onClick(View aView) {
                 Log.v(LOG_TAG, "View.OnClickListener - onClick");
                 if (mCurrentCuisineId != -1) {
                     // TODO: Find in database.
                     String[] projection = new String[] {"_id", "name", "code" ,"price","remark"};
                     Uri uri = ContentUris.withAppendedId(Order.CuisineColumns.CONTENT_URI,mCurrentCuisineId);
                     Cursor c = managedQuery(uri, projection, null, null, null);
                     if (c != null) {
                         if (c.moveToFirst()) {
                             Map<String, String> map = new HashMap<String, String>();
                             map.put("id", Long.toString(mCurrentCuisineId));
                             map.put("name", c.getString(c.getColumnIndex("name")));
                             map.put("code", c.getString(c.getColumnIndex("code")));
                             map.put("price", c.getString(c.getColumnIndex("price")));
                             map.put("count", mCountEditor.getText().toString());
                             map.put("remark", mRemarkEditor.getText().toString());
 
                             mDataList.add(map);
                             mSimpleAdapter.notifyDataSetChanged();
                         }
                     } else {
                         return;
                     }
 
                 } else {
                  // TODO: 根据名字查找，判断是否存在，添加
                 }
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate menu from XML resource
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.order, menu);
 
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Log.v(LOG_TAG, "onOptionsItemSelected");
         switch (item.getItemId()) {
         case R.id.order_commit:
             Toast.makeText(this, "提交成功", Toast.LENGTH_LONG).show();
             Intent intent = new Intent(this, FunctionListActivity.class);
             startActivity(intent);
             finish();
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     private View createListHeader() {
 
         LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
         View headerView = inflater.inflate(R.layout.orderlist_row, null);
         TextView codeHeader = (TextView) headerView.findViewById(R.id.cuisineCodeText);
         codeHeader.setText(R.string.cuisine_code);
         TextView nameHeader = (TextView) headerView.findViewById(R.id.cuisineNameText);
         nameHeader.setText(R.string.cuisine_name);
         TextView priceHeader = (TextView) headerView.findViewById(R.id.cuisinePriceText);
         priceHeader.setText(R.string.cuisine_price);
         TextView countHeader = (TextView) headerView.findViewById(R.id.cuisineCountText);
         countHeader.setText(R.string.cuisine_count);
         TextView remarkHeader = (TextView) headerView.findViewById(R.id.cuisineRemarkText);
         remarkHeader.setText(R.string.cuisine_remark);
         remarkHeader.setGravity(Gravity.CENTER);
         return headerView;
     }
 
     private List<Map<String, String>> createAutoCompleteList() {
         Log.v(LOG_TAG, "createAutoCompleteList");
         ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
         String[] projection = new String[] {"_id", "name", "code", "price", "discount", "remark"};
         Cursor c = managedQuery(Order.CuisineColumns.CONTENT_URI, projection, null, null, null);
 
         if (c != null) {
             boolean succeeded = c.moveToFirst();
             while (succeeded) {
                 Map<String,String> map = new HashMap<String, String>();
                 String name = c.getString(c.getColumnIndex("name"));
                 String id = Integer.toString(c.getInt(c.getColumnIndex("_id")));
                 String code = Integer.toString(c.getInt(c.getColumnIndex("code")));
 
                if (code != null && !code.isEmpty()) {
                     name = code + " " + name;
                 }
 
                 map.put("cuisine",name);
                 map.put("id", id);
                 list.add(map);
                 succeeded = c.moveToNext();
             }
         }
 
         return list;
     }
 
 }
