 package com.mss.application;
 
 import java.util.Date;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.ActionMode.Callback;
 import com.mss.application.fragments.DailyOrdersFragment;
 import com.mss.application.fragments.DailyOrdersFragment.OnDateChangedListener;
 import com.mss.application.fragments.DailyOrdersFragment.OnOrderSelectedListener;
 import com.mss.domain.models.Order;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.util.Log;
 
 public class DailyOrdersActivity extends SherlockFragmentActivity implements OnOrderSelectedListener, OnDateChangedListener, Callback, LoaderCallbacks<List<Order>> {
 
 	private static final String TAG = DailyOrdersActivity.class.getSimpleName();
 	private static final boolean DEBUG = BuildConfig.DEBUG && false;
 
 	/// RoutePoints-specific Loader id
 	private static final int LOADER_ID_DAILY_ORDERS = 0;
 
 	private DailyOrderAdapter mOrderAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_daily_orders);
 		
 		try {
 			mOrderAdapter = new DailyOrderAdapter(this);
 		} catch (Throwable e) {
 			Log.e(TAG, e.getMessage());
 		}
 		getSupportLoaderManager().initLoader(LOADER_ID_DAILY_ORDERS, null, this);
 
 		DailyOrdersFragment fragment = getDailyOrdersFragment();
 		fragment.addOnOrderSelectedListener(this);
 		fragment.addOnDateChangedListener(this);
 		
 		if (getSupportActionBar() != null)
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 	}
 
 	protected DailyOrdersFragment getDailyOrdersFragment() {
 		return (DailyOrdersFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_daily_orders);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		getSupportLoaderManager().restartLoader(LOADER_ID_DAILY_ORDERS, null, this);
 		
 		switch (requestCode) {
 			case OrderActivity.REQUEST_SHOW_ORDER:
 				if (data != null) {
 					long orderId = data.getLongExtra(OrderActivity.KEY_ORDER_ID, 0l);
 					if (orderId != 0l) {
 						showOrderById(orderId);
 					}
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
 		return true;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 		// Note that we handle Edit and Delete items here, even if they were
 		// added by the NoteFragment.
 
 		switch (item.getItemId()) {
 			case android.R.id.home: {
 					Intent intent=new Intent();
 					setResult(RESULT_CANCELED, intent);
 					finish();
 				}
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	@Override
 	public void onOrderSelected(Order order, int position, long id) {
 		if (DEBUG)
 			Log.d(TAG, "onNoteSelected: " + position);
 
 		Intent intent = new Intent(this, OrderActivity.class);
 		intent.putExtra(OrderActivity.KEY_ORDER_ID, id);
 		startActivityForResult(intent, OrderActivity.REQUEST_SHOW_ORDER);
 	}
 
 	@Override
 	public void onDateChanged(Date date) {
 		getSupportLoaderManager().restartLoader(LOADER_ID_DAILY_ORDERS, null, this);
 	}
 	
 	@Override
 	public boolean onCreateActionMode(ActionMode mode,
 		com.actionbarsherlock.view.Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.action_mode_list, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode,
 		com.actionbarsherlock.view.Menu menu) {
 		return false;
 	}
 
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
 		switch (item.getItemId()) {
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 	}
 
 	@Override
 	public Loader<List<Order>> onCreateLoader(int id, Bundle args) {
 		switch (id) {
 		case LOADER_ID_DAILY_ORDERS:
 			try {
 				return new DailyOrdersLoader(this, getDailyOrdersFragment().getDate());
 			} catch (Throwable e) {
 				Log.e(TAG, e.getMessage());
 			}
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<List<Order>> loader, List<Order> data) {
 		switch (loader.getId()) {
 		case LOADER_ID_DAILY_ORDERS:
 
 			int count = 0;
			int amount = 0;
 			for (Order order : data) {
 				count ++;				
 				amount += order.getAmount();
 			}
 			
 			mOrderAdapter.swapData(data);
 			DailyOrdersFragment fragment = getDailyOrdersFragment();
 			fragment.setCount(count);
			fragment.setAmount(amount);
 			fragment.setListAdapter(mOrderAdapter);
 			
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<List<Order>> loader) {
 		mOrderAdapter.swapData(null);
 	}
 	
 	public void showOrderById(long id) {
 		Order n;
 		try {
 			n = mOrderAdapter.getItemById(id);
 			onOrderSelected(n, -1, id);
 		} catch (Throwable e) {
 			Log.e(TAG, e.toString());
 		}
 	}
 }
