 package com.mss.application;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.mss.application.fragments.RoutePointsOrdersFragment;
 import com.mss.application.fragments.RoutePointsOrdersFragment.OnOrderSelectedListener;
 import com.mss.domain.models.Order;
 import com.mss.domain.models.RoutePoint;
 import com.mss.domain.models.Status;
 import com.mss.domain.services.RoutePointService;
 import com.mss.domain.services.StatusService;
 import com.mss.infrastructure.ormlite.DatabaseHelper;
 
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 public class RoutePointActivity extends SherlockFragmentActivity implements OnTabChangeListener, OnOrderSelectedListener, LoaderCallbacks<RoutePoint> {
 
 	private static final String TAG = RoutePointActivity.class.getSimpleName();
 	
 	public static final String TAB_GENERAL = "General";
     public static final String TAB_DETAILS = "Details";
     private TabHost mTabHost;
     private int mCurrentTab;
     
     public static final int LOADER_ID_ROUTE_POINT = 0;    
 	public static final int REQUEST_SHOW_ROUTE_POINT = 0;
 	static final int PICK_STATUS_REQUEST = 1;
 	
 	public static final long ROUTE_POINT_ID_NEW = 0;
 	public static final String EXTRA_ROUTE_POINT_ID = "route_point_id";
 	
 	private long mRoutePointId;
 	private RoutePoint mRoutePoint;
 	
 	private DatabaseHelper mDatabaseHelper;
 	private RoutePointService mRoutePointService;
 	private StatusService mStatusService;
 	
 	private TextView mName;
 	private TextView mAddress;
 	private TextView mStatus;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_route_point);
 		
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
         setupTabs();
         
         mTabHost.setOnTabChangedListener(this);
         mTabHost.setCurrentTab(mCurrentTab);     
 
         mName = (TextView) findViewById(R.id.route_point_name_text_view);
         mAddress = (TextView) findViewById(R.id.route_point_address_text_view);
         mStatus = (TextView) findViewById(R.id.route_point_status_text_view);
         
         mRoutePointId = getIntent().getLongExtra(getString(R.string.key_id), ROUTE_POINT_ID_NEW);
 		getSupportLoaderManager().initLoader(LOADER_ID_ROUTE_POINT, null, this);
         
 		RoutePointsOrdersFragment fragment = getRoutePointsOrdersFragment();		
 		fragment.addOnOrderSelectedListener(this);
 		
 		mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
 		try {
 			mRoutePointService = new RoutePointService(mDatabaseHelper);
 			mStatusService = new StatusService(mDatabaseHelper);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 		
 		if (getSupportActionBar() != null) {
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 			//getSupportActionBar().setDisplayShowTitleEnabled(false);
 		}
 	}
 	
 	protected RoutePointsOrdersFragment getRoutePointsOrdersFragment() {
 		return (RoutePointsOrdersFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_order_list);
 	}
 	
 	private void setupTabs() {
         mTabHost.setup(); // you must call this before adding your tabs!
         mTabHost.addTab(newTab(TAB_GENERAL, R.string.label_tab_general, R.id.tab_general));
         mTabHost.addTab(newTab(TAB_DETAILS, R.string.label_tab_details, R.id.tab_details));
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
             return;
         }
         if (TAB_DETAILS.equals(tabId)) {
             mCurrentTab = 1;
             return;
         }
     }
     
     @Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
     	if (mRoutePointService.canBeEditedOrDeleted(mRoutePointId)) {
     		getSupportMenuInflater().inflate(R.menu.menu_route_point_editable, menu);
     	} else {
     		getSupportMenuInflater().inflate(R.menu.menu_route_point, menu);
     	}
 		return true;
 	}
 	
 	@Override
 	public void onOrderSelected(Order order, int position, long id) {
 		Intent intent = new Intent(this, OrderActivity.class);
 		intent.putExtra(OrderActivity.KEY_ORDER_ID, id);
 		startActivityForResult(intent, OrderActivity.REQUEST_SHOW_ORDER);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 			case RoutePointEditActivity.REQUEST_EDIT_ROUTE_POINT:			
 				break;
			case OrderEditActivity.REQUEST_ADD_ORDER:	
				invalidateOptionsMenu();
 				break;			
 			case PICK_STATUS_REQUEST:
 				long statusId = data.getLongExtra("status_id", 0l);
 				
 				Status status = mStatusService.getById(statusId);
 				if (status != null && mRoutePoint != null) {
 					mRoutePointService.changePointStatus(mRoutePoint, status);
 				}
 				break;
 			default:
 				break;
 			}
 		
 			getSupportLoaderManager().restartLoader(LOADER_ID_ROUTE_POINT, null, this);
 		} else if (requestCode == OrderActivity.REQUEST_SHOW_ORDER) {
 			getSupportLoaderManager().restartLoader(LOADER_ID_ROUTE_POINT, null, this);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// ActionBar's Home button clicked
 
 			Intent upIntent = new Intent(this, RouteActivity.class);
 			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
 				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
 				finish();
 			} else {
 				NavUtils.navigateUpTo(this, upIntent);
 			}
 			return true;
 		case R.id.menu_item_edit: {
 			Intent i = new Intent(this, RoutePointEditActivity.class);				
 			i.putExtra(RoutePointEditActivity.KEY_ROUTE_POINT_ID, mRoutePointId);
 			startActivityForResult(i, RoutePointEditActivity.REQUEST_EDIT_ROUTE_POINT);
 			}
 			return true;
 		case R.id.menu_item_change_status: {
 			Intent i = new Intent(this, StatusesActivity.class);	
 			startActivityForResult(i, PICK_STATUS_REQUEST);
 			}
 			return true;
 		case R.id.menu_item_delete: {
 				if (mRoutePoint != null) {
 					new AlertDialog.Builder(this)
 			        	.setTitle(R.string.dialog_delete_confirmation_title) 
 			        	.setMessage(R.string.dialog_delete_confirmation_message) 
 			        	.setIcon(R.drawable.ic_action_delete)
 			        	.setPositiveButton(R.string.dialog_delete_confirmation_positive_button, 
 			        			new DialogInterface.OnClickListener() {
 			        		public void onClick(DialogInterface dialog, int whichButton) { 
 			        			mRoutePointService.deletePoint(mRoutePoint);
 			        			finish();
 			        			dialog.dismiss();
 			        		}   
 			        	})
 			        	.setNegativeButton(R.string.dialog_delete_confirmation_negative_button, new DialogInterface.OnClickListener() {
 			        		public void onClick(DialogInterface dialog, int which) {
 			        			dialog.dismiss();
 			        		}
 			        	})
 			        	.create()
 			        	.show();
 				}
 				return true;
 			}
 		case R.id.menu_item_add: {
 				Intent intent = new Intent(this, OrderEditActivity.class);
 				intent.putExtra(OrderEditActivity.KEY_ROUTE_POINT_ID, mRoutePointId);
 				startActivityForResult(intent, OrderEditActivity.REQUEST_ADD_ORDER);
 			}
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	@Override
 	public Loader<RoutePoint> onCreateLoader(int id, Bundle bundle) {
 		switch (id) {
 		case LOADER_ID_ROUTE_POINT:
 			try {
 				return new RoutePointLoader(this, mRoutePointId);
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<RoutePoint> loader, RoutePoint data) {
 		mRoutePoint = data;
 		
 		if (mRoutePoint != null) {
 			mName.setText(mRoutePoint.getShippingAddressName());
 			mAddress.setText(mRoutePoint.getShippingAddressValue());	
 			mStatus.setText(mRoutePoint.getStatusName());
 			
 			getRoutePointsOrdersFragment().refresh(mRoutePointId);
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<RoutePoint> arg0) {
 		mRoutePoint = null;	
 	}
 }
