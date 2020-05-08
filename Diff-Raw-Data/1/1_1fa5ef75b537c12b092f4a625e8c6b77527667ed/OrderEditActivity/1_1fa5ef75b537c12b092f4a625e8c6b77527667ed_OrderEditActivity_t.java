 package com.mss.application;
 
 import java.util.Calendar;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.mss.application.fragments.DatePickerFragment;
 import com.mss.application.fragments.OrderPickupItemsFragment;
 import com.mss.application.fragments.TimePickerFragment;
 import com.mss.application.fragments.OrderPickupItemsFragment.OnOrderPickupItemSelectedListener;
 import com.mss.domain.models.Customer;
 import com.mss.domain.models.Order;
 import com.mss.domain.models.OrderPickedUpItem;
 import com.mss.domain.models.OrderPickupItem;
 import com.mss.domain.models.PriceList;
 import com.mss.domain.models.ProductUnitOfMeasure;
 import com.mss.domain.models.Route;
 import com.mss.domain.models.RoutePoint;
 import com.mss.domain.models.ShippingAddress;
 import com.mss.domain.models.Warehouse;
 import com.mss.domain.services.CustomerService;
 import com.mss.domain.services.OrderService;
 import com.mss.domain.services.PriceListService;
 import com.mss.domain.services.ProductService;
 import com.mss.domain.services.RoutePointService;
 import com.mss.domain.services.RouteService;
 import com.mss.domain.services.ShippingAddressService;
 import com.mss.domain.services.WarehouseService;
 import com.mss.infrastructure.ormlite.DatabaseHelper;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TimePicker;
 
 public class OrderEditActivity extends SherlockFragmentActivity implements OnTabChangeListener, OnOrderPickupItemSelectedListener, LoaderCallbacks<Order>{
 	private static final String TAG = OrderEditActivity.class.getSimpleName();
     
 	public static final String TAB_GENERAL = "General";
     public static final String TAB_DETAILS = "Details";
     public static final String TAB_NOTES = "Notes";
     private TabHost mTabHost;
     private int mCurrentTab;
 
     public static final int LOADER_ID_ORDER = 0;
     
     public static final int REQUEST_ADD_ORDER = 10;
 	public static final int REQUEST_EDIT_ORDER = 11;
 	
 	public static final String KEY_ORDER_ID = "id";
 	public static final String KEY_ROUTE_POINT_ID = "route_point_id";
 	public static final String KEY_PRICE_LIST_ID = "price_list_id";
 	
 	static final int PICK_PRICE_LIST_REQUEST = 1;
 	static final int PICK_WAREHOUSE_REQUEST = 2;
 	static final int PICK_PRODUCTS_REQUEST = 3;
 	static final int REQUEST_EDIT_ORDER_PICKUP_ITEM = 4;
 	static final int FILTER_REQUEST = 5;
 	static final int CATEGORIES_QUICK_FILTER_REQUEST = 6;
 
 	private Long mOrderId;
 	private Long mRoutePointId;
 	private Order mOrder;
 	
 	private Warehouse mWarehouse;
 	private PriceList mPriceList;
 	
 	private EditText mOrderDate;
 	private EditText mOrderShippingDate;
 	private EditText mOrderShippingTime;
 	private EditText mOrderCustomer;
 	private EditText mOrderShippingAddress;
 	private EditText mOrderPriceList;
 	private EditText mOrderWarehouse;
 	private EditText mOrderNotes;
 
 	private DatabaseHelper mHelper;
 	private RouteService mRouteService;
 	private RoutePointService mRoutePointService;
 	private CustomerService mCustomerService;
 	private ShippingAddressService mShippingAddressService;
 	private PriceListService mPriceListService;
 	private WarehouseService mWarehouseService;
 	private OrderService mOrderService;
 	private ProductService mProductService;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_order_edit);
 		
 		mHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		try {
 			mRouteService = new RouteService(mHelper);
 			mRoutePointService = new RoutePointService(mHelper);
 			mCustomerService = new CustomerService(mHelper);
 			mShippingAddressService = new ShippingAddressService(mHelper);
 			mPriceListService = new PriceListService(mHelper);
 			mWarehouseService = new WarehouseService(mHelper);
 			mOrderService = new OrderService(mHelper);
 			mProductService = new ProductService(mHelper);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 				
 		mOrderDate = (EditText)findViewById(R.id.order_date_edit_text);
 		mOrderShippingDate = (EditText)findViewById(R.id.order_shipping_date_edit_text);
 		mOrderShippingTime = (EditText)findViewById(R.id.order_shipping_time_edit_text);
 		mOrderCustomer = (EditText)findViewById(R.id.order_customer_edit_text);
 		mOrderShippingAddress = (EditText)findViewById(R.id.order_shipping_address_edit_text);
 		mOrderPriceList = (EditText)findViewById(R.id.order_price_list_edit_text);
 		mOrderWarehouse = (EditText)findViewById(R.id.order_warehouse_edit_text);
 		mOrderNotes = (EditText)findViewById(R.id.order_notes_edit_text);
 		
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         setupTabs();
         
         mTabHost.setOnTabChangedListener(this);
         mTabHost.setCurrentTab(mCurrentTab);        
         
 		mOrderId = getIntent().getLongExtra(getString(R.string.key_id), 0);
 		mRoutePointId = getIntent().getLongExtra(KEY_ROUTE_POINT_ID, 0);
 		getSupportLoaderManager().initLoader(LOADER_ID_ORDER, null, this);
 		
 		if (savedInstanceState == null || !savedInstanceState.getBoolean("restart", false)) {
 			OrderEditContext.Init();
 			if (mOrderId != 0) {
 				Iterable<OrderPickedUpItem> items = mOrderService.getOrderPickedUpItems(mOrderId);
 				for (OrderPickedUpItem orderPickedUpItem : items) {
 					OrderEditContext.getPickedUpItems().put(orderPickedUpItem.getId(), orderPickedUpItem);
 				}
 			}
 		} 
 		
 		mOrderPriceList.setOnClickListener(new TextView.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				if (mOrder.getPriceListId() != 0 && OrderEditContext.getPickedUpItems().size() > 0) {
 					new AlertDialog.Builder(view.getContext())
 	        		.setTitle(R.string.dialog_change_price_confirmation_title) 
 	        		.setMessage(R.string.dialog_change_price_confirmation_message) 
 	        		.setIcon(R.drawable.ic_action_edit)
 	        		.setPositiveButton(R.string.dialog_change_price_confirmation_positive_button, 
 	        				new DialogInterface.OnClickListener() {
 	        			public void onClick(DialogInterface dialog, int whichButton) { 
 	        				Intent priceListsActivity = new Intent(getApplicationContext(), PriceListsActivity.class);
 	    		    		startActivityForResult(priceListsActivity, PICK_PRICE_LIST_REQUEST);
 	        				dialog.dismiss();
 	        			}   
 	        		})
 	        		.setNegativeButton(R.string.dialog_change_price_confirmation_negative_button, new DialogInterface.OnClickListener() {
 	        			public void onClick(DialogInterface dialog, int which) {
 	        				dialog.dismiss();
 	        			}
 	        		})
 	        		.create()
 	        		.show();
 				} else {
 					Intent priceListsActivity = new Intent(getApplicationContext(), PriceListsActivity.class);
 		    		startActivityForResult(priceListsActivity, PICK_PRICE_LIST_REQUEST);
 				}
 			}
         });
 		mOrderPriceList.setKeyListener(null);
 		
 		mOrderWarehouse.setOnClickListener(new TextView.OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Intent warehousesActivity = new Intent(getApplicationContext(), WarehousesActivity.class);
 				startActivityForResult(warehousesActivity, PICK_WAREHOUSE_REQUEST);
 			}
         });
 		mOrderWarehouse.setKeyListener(null);
 		
 		mOrderShippingDate.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {	
 				showDatePicker();
 			}
 		});
 		mOrderShippingDate.setKeyListener(null);
 		
 		mOrderShippingTime.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {	
 				showTimePicker();
 			}
 		});
 		mOrderShippingTime.setKeyListener(null);
 		
 		OrderPickupItemsFragment fragment = getOrderPickupItemsFragment();		
 		fragment.addOnOrderPickupItemSelectedListener(this);
 
 		if (getSupportActionBar() != null)
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}	
 	
 	protected OrderPickupItemsFragment getOrderPickupItemsFragment() {
 		return (OrderPickupItemsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_order_pickup_item_list);
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		  // Save UI state changes to the savedInstanceState.
 		  // This bundle will be passed to onCreate if the process is
 		  // killed and restarted.
 		  savedInstanceState.putBoolean("restart", true);
 	}
 	
 	private void setupTabs() {
         mTabHost.setup(); // you must call this before adding your tabs!
         mTabHost.addTab(newTab(TAB_GENERAL, R.string.label_tab_general, R.id.tab_general));
         mTabHost.addTab(newTab(TAB_DETAILS, R.string.label_tab_details, R.id.tab_details));
         mTabHost.addTab(newTab(TAB_NOTES, R.string.label_tab_notes, R.id.tab_notes));
     }
     
     private TabSpec newTab(String tag, int labelId, int tabContentId) {
         Log.d(TAG, "buildTab(): tag=" + tag);
  
         TabSpec tabSpec = mTabHost.newTabSpec(tag);
         tabSpec.setIndicator(getString(labelId));
         tabSpec.setContent(tabContentId);
         return tabSpec;
     }
      
     @Override
     public void onTabChanged(String tabId) {
         if (TAB_GENERAL.equals(tabId)) {
             mCurrentTab = 0;
             supportInvalidateOptionsMenu();
             return;
         }
         if (TAB_DETAILS.equals(tabId)) {
             mCurrentTab = 1;
             supportInvalidateOptionsMenu();
             return;
         }
         if (TAB_NOTES.equals(tabId)) {
             mCurrentTab = 2;
             supportInvalidateOptionsMenu();
             return;
         }
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		switch (mCurrentTab) {
 		case 1:
 			if (OrderEditContext.getSelectedCategories().size() > 0) {
 				getSupportMenuInflater().inflate(R.menu.menu_order_edit_filtred_details_tab, menu);
 			} else {
 				getSupportMenuInflater().inflate(R.menu.menu_order_edit_details_tab, menu);
 			}
 			
 			SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
 	        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() { 
 	        	public boolean onQueryTextChange(String newText) { 
 	        		search(newText); 
 	        		return true; 
 	        	}
 
 	        	public boolean onQueryTextSubmit(String query) 
 	        	{
 	        		search(query);
 	        		return true;
 	        	}
 	        };
 	    
 	        searchView.setOnQueryTextListener(queryTextListener);	
 			break;
 
 		default:
 			getSupportMenuInflater().inflate(R.menu.menu_order_edit, menu);
 			break;
 		}
 					
 		return true;
 	}
 	
 	public void search(String criteria) { 
 		getOrderPickupItemsFragment().applyFilter(criteria);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    // Check which request we're responding to
 	    if (requestCode == PICK_PRICE_LIST_REQUEST) {
 	        // Make sure the request was successful
 	        if (resultCode == RESULT_OK) {
 	        	long priceListId = data.getLongExtra("price_list_id", 0l);
 	        		        	
 	        	try {
 	        		if (mOrder.getPriceListId() != priceListId) {
 	        			OrderEditContext.getPickedUpItems().clear();
 	        		}
 	        		mPriceList = mPriceListService.getById(priceListId);
 				} catch (Throwable e) {
 					Log.e(TAG, e.getMessage());
 				}	        
 	        	OrderEditContext.setIsModifyed(true);
 	        	getSupportLoaderManager().restartLoader(LOADER_ID_ORDER, null, this);
 	        }
 	    } else if (requestCode == FILTER_REQUEST) {
 	    	if (resultCode == RESULT_OK) {
 	    		supportInvalidateOptionsMenu();
 	    		getSupportLoaderManager().restartLoader(LOADER_ID_ORDER, null, this);
 	    	}
 	    } else if (requestCode == CATEGORIES_QUICK_FILTER_REQUEST) {	    	
 	    	if (resultCode == RESULT_OK) {	    		
 	    		supportInvalidateOptionsMenu();
 	    		getSupportLoaderManager().restartLoader(LOADER_ID_ORDER, null, this);
 	    	}
 	    } else if (requestCode == PICK_WAREHOUSE_REQUEST) {
 	        // Make sure the request was successful
 	        if (resultCode == RESULT_OK) {
 	        	long warehouseId = data.getLongExtra("warehouse_id", 0l);
 	        		        		        		        	
 	        	try {
 	        		mWarehouse = mWarehouseService.getById(warehouseId);
 				} catch (Throwable e) {
 					Log.e(TAG, e.getMessage());
 				}
 	        	OrderEditContext.setIsModifyed(true);
 	        	getSupportLoaderManager().restartLoader(LOADER_ID_ORDER, null, this);
 	        }
 	    } else if (requestCode == REQUEST_EDIT_ORDER_PICKUP_ITEM) {
 	    	if (resultCode == RESULT_OK) {
 	        	long orderPickupItemId = data.getLongExtra("order_pickup_item_id", 0l);
 	        	
 	        	OrderPickupItem orderPickupItem = 
 	        			mOrderService.getOrderPickupItemById(orderPickupItemId);
 	        	ProductUnitOfMeasure productUnitOfMeasure = 
         				mProductService.getProductsUnitOfMeasure(PickupItemContext.getPickedUpItem().getProductUoMId());
 	        	
 	        	OrderPickedUpItem item = new OrderPickedUpItem(
 	        			orderPickupItem.getProductId(),
 	        			orderPickupItem.getProductName(), 
 	        			orderPickupItem.getItemPrice(), 
 	        			PickupItemContext.getPickedUpItem().getCount(), 
 	        			productUnitOfMeasure);
 	        	if (OrderEditContext.getPickedUpItems().containsKey(orderPickupItem.getProductId())) {
 	        		if (PickupItemContext.getPickedUpItem().getCount() == 0) {
 	        			OrderEditContext.getPickedUpItems().remove(orderPickupItem.getProductId());
 	        		}
 	        	}
 	        	
 	        	if (PickupItemContext.getPickedUpItem().getCount() != 0) {
 	        		OrderEditContext.getPickedUpItems().put(orderPickupItem.getProductId(), item);
 	        	}
 	        	
 	        	OrderEditContext.setIsModifyed(true);
 	        	getOrderPickupItemsFragment().refresh(mOrder.getPriceListId());
 	        }
 	    }
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			if (OrderEditContext.getIsModifyed()) {
 				new AlertDialog.Builder(this)
     			.setTitle(R.string.dialog_leave_changed_order_confirmation_title) 
     			.setMessage(R.string.dialog_leave_changed_order_confirmation_message) 
     			.setIcon(R.drawable.ic_action_edit)	
     			.setPositiveButton(R.string.dialog_leave_changed_order_confirmation_positive_button, 
     					new DialogInterface.OnClickListener() {
     				public void onClick(DialogInterface dialog, int whichButton) {
     					finish();
     					dialog.dismiss();
     				}   
     			})
     			.setNegativeButton(R.string.dialog_leave_changed_order_confirmationn_negative_button, new DialogInterface.OnClickListener() {
     				public void onClick(DialogInterface dialog, int which) {
     					dialog.dismiss();
     				}
     			})
     			.create()
     			.show();
 			} else {
 				finish();
 			}
 			
 			return true;
 		case R.id.menu_item_filter: 
 			Intent filterActivity = new Intent(getApplicationContext(), OrderItemPickupFilterActivity.class);
 			startActivityForResult(filterActivity, FILTER_REQUEST);
 			return true;
 		case R.id.menu_item_categories_quick_filter: 
 			Intent categoriesQuickFilterActivity = new Intent(getApplicationContext(), CategoriesQuickFilterActivity.class);
 			startActivityForResult(categoriesQuickFilterActivity, CATEGORIES_QUICK_FILTER_REQUEST);
 			return true;
 		case R.id.menu_item_categories_quick_filter_clear:
 			OrderEditContext.getSelectedCategories().clear();			
 			getOrderPickupItemsFragment().refresh(mOrder.getPriceListId());
 			supportInvalidateOptionsMenu();
 			return true;
 		case R.id.menu_item_save:
 			if (mOrder != null)
 				try {
 					mOrderShippingDate.setError(null);
 					mOrderPriceList.setError(null);
 					mOrderWarehouse.setError(null);
 					
 					boolean cancel = false;
 				    View focusView = null;
 					
 					if (mOrder.getPriceListId() == 0) {
 						mOrderPriceList.setError(getString(R.string.error_field_required));
 				        focusView = mOrderPriceList;
 				        cancel = true;
 					}
 					
 					if (mOrder.getWarehouseId() == 0) {
 						mOrderWarehouse.setError(getString(R.string.error_field_required));
 				        focusView = mOrderWarehouse;
 				        cancel = true;
 					}
 					
 					if (mOrder.getOrderDate().after(mOrder.getShippingDate())) {
 						mOrderShippingDate.setError(getString(R.string.error_shipping_date_must_be_greater_than_order_date));
 				        focusView = mOrderShippingDate;
 				        cancel = true;
 					}
 					
 					if (cancel) {
						mTabHost.setCurrentTab(0); // Set General tab
 				        focusView.requestFocus();
 				    } else {					
 				    	String note = mOrderNotes.getText().toString();
 						mOrder.setNote(note);
 						mOrderService.saveOrder(mOrder, OrderEditContext.getPickedUpItems().values());
 					
 						Intent intent=new Intent();
 						setResult(RESULT_OK, intent);
 				    	finish();
 				    }
 				} catch (Throwable e) {
 					Log.e(TAG, e.getMessage());
 				}
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	private void showDatePicker() {
 		DatePickerFragment date = new DatePickerFragment();
 		/**
 	     * Set Up Current Date Into dialog
 		 */
 		Calendar calender = Calendar.getInstance();
 		calender.setTime(mOrder.getShippingDate());
 		Bundle args = new Bundle();
 		args.putInt("year", calender.get(Calendar.YEAR));
 		args.putInt("month", calender.get(Calendar.MONTH));
 		args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
 		date.setArguments(args);
 		/**
 		 * Set Call back to capture selected date
 		 */
 		date.setCallBack(ondate);
 		date.show(getSupportFragmentManager(), "Date Picker");
 	}
 
 	OnDateSetListener ondate = new OnDateSetListener() {
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {			
 			final Calendar c = Calendar.getInstance();
 			c.setTime(mOrder.getShippingDate());
 			c.set(year, monthOfYear, dayOfMonth);
 			
 			mOrder.setShippingDate(c.getTime());
 			mOrderShippingDate.setText(DateFormat.getDateFormat(getApplicationContext()).format(mOrder.getShippingDate()));
 			OrderEditContext.setIsModifyed(true);
 		}
 	};
 	
 	private void showTimePicker() {
 		TimePickerFragment timePickerFragment = new TimePickerFragment();
 		/**
 	     * Set Up Current Date Into dialog
 		 */
 		Calendar calender = Calendar.getInstance();
 		calender.setTime(mOrder.getShippingDate());
 		Bundle args = new Bundle();
 		args.putInt("hour", calender.get(Calendar.HOUR_OF_DAY));
 		args.putInt("minute", calender.get(Calendar.MINUTE));
 		timePickerFragment.setArguments(args);
 		/**
 		 * Set Call back to capture selected date
 		 */
 		timePickerFragment.setCallBack(onTime);
 		timePickerFragment.show(getSupportFragmentManager(), "Time Picker");
 	}
 
 	OnTimeSetListener onTime = new OnTimeSetListener() {
 		@Override
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 			final Calendar c = Calendar.getInstance();
 			c.setTime(mOrder.getShippingDate());
 			c.set(Calendar.HOUR, hour);
 			c.set(Calendar.MINUTE, minute);
 			
 			mOrder.setShippingDate(c.getTime());
 			mOrderShippingTime.setText(DateFormat.getTimeFormat(getApplicationContext()).format(mOrder.getShippingDate()));
 			OrderEditContext.setIsModifyed(true);
 		}
 	};
 
 	@Override
 	public void onOrderPickupItemSelected(OrderPickupItem orderPickupItem,
 			int position, long id) {
 		Intent intent = new Intent(getApplicationContext(), OrderItemPickupActivity.class);
 		intent.putExtra(OrderItemPickupActivity.KEY_ORDER_PICKUP_ITEM_ID, id);
 		startActivityForResult(intent, REQUEST_EDIT_ORDER_PICKUP_ITEM);
 	}
 
 	@Override
 	public Loader<Order> onCreateLoader(int id, Bundle bundle) {
 		switch (id) {
 		case LOADER_ID_ORDER:
 			try {
 				return new OrderLoader(this, mOrderId);
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Order> orders, Order order) {
 		if (order != null)
 			mOrder = order;
 		
 		java.text.DateFormat dateFormat = DateFormat.getDateFormat(getApplicationContext());
 		java.text.DateFormat timeFormat = DateFormat.getTimeFormat(getApplicationContext());
 		if (mOrder == null) {
 			try {				
 				RoutePoint routePoint = mRoutePointService.getById(mRoutePointId);
 				Route route = mRouteService.getById(routePoint.getRouteId());
 				mOrder = mOrderService.createOrder(route, routePoint);
 				ShippingAddress shippingAddress = mShippingAddressService.getById(routePoint.getShippingAddressId());
 				Customer customer = mCustomerService.getById(shippingAddress.getCustomerId());
 				PriceList priceList = mPriceListService.getDefault();
 				Warehouse warehouse = mWarehouseService.getDefault();
 				
 				mOrder.setShippingDate(mOrder.getOrderDate());
 				mOrder.setCustomer(customer);
 				mOrder.setShippingAddress(shippingAddress);
 				if (priceList != null) {
 					mOrder.setPriceList(priceList);
 				}
 				if (warehouse != null) {
 					mOrder.setWarehouse(warehouse);
 				}
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}			
 		}
 		
 		if (mPriceList != null)
 			mOrder.setPriceList(mPriceList);
 			
 		if(mWarehouse != null)
 			mOrder.setWarehouse(mWarehouse);
 
 		mOrderDate.setText(dateFormat.format(mOrder.getOrderDate()));
 		mOrderShippingDate.setText(dateFormat.format(mOrder.getShippingDate()));
 		mOrderShippingTime.setText(timeFormat.format(mOrder.getShippingDate()));
 		mOrderCustomer.setText(mOrder.getCustomerName());
 		mOrderShippingAddress.setText(mOrder.getShippingAddressName());
 		mOrderPriceList.setText(mOrder.getPriceListName());
 		mOrderWarehouse.setText(mOrder.getWarehouseName());	
 		mOrderNotes.setText(mOrder.getNote());
 			
 		if (mOrder.getPriceListId() != 0) {
 			getOrderPickupItemsFragment().refresh(mOrder.getPriceListId());
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Order> orders) {
 		mOrder = null;
 	}	
 }
