 package com.group5.android.fd.activity;
 
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.group5.android.fd.FdConfig;
 import com.group5.android.fd.Main;
 import com.group5.android.fd.R;
 import com.group5.android.fd.adapter.ConfirmAdapter;
 import com.group5.android.fd.entity.CategoryEntity;
 import com.group5.android.fd.entity.OrderEntity;
 import com.group5.android.fd.entity.OrderItemEntity;
 import com.group5.android.fd.entity.TableEntity;
 import com.group5.android.fd.helper.HttpHelper;
 import com.group5.android.fd.helper.UriStringHelper;
 
 public class NewSessionActivity extends Activity {
 	final public static int REQUEST_CODE_TABLE = 1;
 	final public static int REQUEST_CODE_CATEGORY = 2;
 	final public static int REQUEST_CODE_ITEM = 3;
 	final public static int REQUEST_CODE_CONFIRM = 4;
 	
 	public static final String POST_ORDER_STRING = "Go";
 	protected OrderEntity order = new OrderEntity();
 	protected String m_csrfTokenPage = null;
 	
 	
 	//For display confirm View
 	protected ConfirmAdapter m_confirmAdapter;
 	protected ListView m_vwLisView;
 	protected Button confirmButton;
 	protected TextView tblName;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//get intent from Main
 		Intent intent = getIntent();
 		m_csrfTokenPage = intent.getStringExtra(Main.INSTANCE_STATE_KEY_CSRF_TOKEN_PAGE);
 		
 		
 		Object lastNonConfigurationInstance = getLastNonConfigurationInstance();
 		if (lastNonConfigurationInstance != null
 				&& lastNonConfigurationInstance instanceof OrderEntity) {
 			// found our long lost order, yay!
 			order = (OrderEntity) lastNonConfigurationInstance;
 
 			Log.i(FdConfig.DEBUG_TAG, "OrderEntity has been recovered;");
 		}
 
 		// this method should take care of the table for us
 		//startCategoryList();
 		startTableList();
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		// we want to preserve our order information when configuration is
 		// change, say.. orientation change?
 		return order;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		CategoryEntity pendingCategory = null;
 
 		if (resultCode == Activity.RESULT_OK && data != null) {
 			switch (requestCode) {
 			case REQUEST_CODE_TABLE:
 				TableEntity table = (TableEntity) data
 						.getSerializableExtra(TableListActivity.ACTIVITY_RESULT_NAME_TABLE_OBJ);
 				order.setTable(table);
 				startCategoryList();
 				break;
 			case REQUEST_CODE_CATEGORY:
 				pendingCategory = (CategoryEntity) data
 						.getSerializableExtra(CategoryListActivity.ACTIVITY_RESULT_NAME_CATEGORY_OBJ);
 				startItemList(pendingCategory);
 				break;
 			case REQUEST_CODE_ITEM:
 				OrderItemEntity orderItem = (OrderItemEntity) data
 						.getSerializableExtra(ItemListActivity.ACTIVITY_RESULT_NAME_ORDER_ITEM_OBJ);
 				order.addOrderItem(orderItem);
 				startCategoryList();
 				break;
 			
 			}
 		}
 		else if ( resultCode == Activity.RESULT_CANCELED){
 			// xu ly khi activity bi huy boi back
 			switch (requestCode) {
 			case REQUEST_CODE_TABLE:
 				this.finish();
 				break;
 			case REQUEST_CODE_CATEGORY:
 				// tro ve table List dong thoi xoa gia tri table trong order
 				startTableList();
 				break;
 			case REQUEST_CODE_ITEM:
 				startCategoryList();
 				break;
 			
 			}
 		}
 		else if (resultCode == CategoryListActivity.RESULT_OK_BEFORE_CONFIRM){
 			Log.v(FdConfig.DEBUG_TAG, "ConfirmListActivity started");
 			startConfirmList();
 		}
 		
 		
 		/*if (pendingCategory == null) {
 			// no pending category, yet. Display the category list
 			startCategoryList();
 		} else {
 			// a category is pending, display the item list of that category
 			startItemList(pendingCategory);
 		}*/
 		
 		
 	}
 
 	protected void startTableList() {
 		Intent tableIntent = new Intent(this, TableListActivity.class);
 		startActivityForResult(tableIntent,
 				NewSessionActivity.REQUEST_CODE_TABLE);
 	}
 
 	protected void startCategoryList() {
 		if (order.getTableId() == 0) {
 			// before display the category list
 			// we should have a valid table set
 			startTableList();
 		} else {
 			Intent categoryIntent = new Intent(this, CategoryListActivity.class);
 			startActivityForResult(categoryIntent,
 					NewSessionActivity.REQUEST_CODE_CATEGORY);
 		}
 	}
 
 	
 	protected void startItemList(CategoryEntity category) {
 		Intent itemIntent = new Intent(this, ItemListActivity.class);
 		itemIntent.putExtra(ItemListActivity.EXTRA_DATA_NAME_CATEGORY_ID,
 				category.categoryId);
 		startActivityForResult(itemIntent, NewSessionActivity.REQUEST_CODE_ITEM);
 	}
 	
 	protected void startConfirmList(){
 		m_confirmAdapter = new ConfirmAdapter(this, order.getOrderItems()); 
 		initLayout();
 		initListeners();
 		confirmButton.setText(POST_ORDER_STRING);
 		tblName.setText(order.getTableName());
 		m_vwLisView.setAdapter(m_confirmAdapter);
 	}
 	
 	
 	/*
 	 * Cai dat danh cho confirm list
 	 * Bao gom cac thiet lap lay out, listener va ham post du lieu order toi server
 	 */
 	public void initLayout(){
 		setContentView(R.layout.activity_confirm);
 		m_vwLisView = (ListView) findViewById(R.id.m_vwListView);
 		confirmButton = (Button)findViewById(R.id.confirmButton);
 		tblName = (TextView) findViewById(R.id.tblName);
 	}
 	
 	public void initListeners(){
 		confirmButton.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				postOrder();
 				NewSessionActivity.this.finish();
 			}
 		});
 		
 	}
 	
 	public void postOrder(){
 		new AsyncTask<Void, Void, JSONObject>() {
 			@Override
 			protected JSONObject doInBackground(Void... Void) {
				String orderUrl = UriStringHelper.buildUriString("new_order");
 				List<NameValuePair> params = order.getOrderAsParams();
 				JSONObject response = HttpHelper.post(NewSessionActivity.this, orderUrl, 
 													m_csrfTokenPage, params);
 				return response;
 			}
 
 			@Override
 			protected void onPostExecute(JSONObject jsonObject) {
 				//TODO
 			}
 		}.execute();
 	}
 	
 	/*
 	 * thuc hien khi nut Back duoc nhan
 	 * chuyen tro ve CategoryList de tiep tuc chon
 	 */
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		if ( keyCode == KeyEvent.KEYCODE_BACK){
 			startCategoryList();
 			return true;
 		}
 		return false;
 	}
 }
