 package com.github.barcodescanner.database;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.activities.EmptyDatabaseActivity;
 import com.github.barcodescanner.activities.MainActivity;
 import com.github.barcodescanner.product.AddManuallyActivity;
 import com.github.barcodescanner.product.Product;
 import com.github.barcodescanner.product.ProductActivity;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 @SuppressLint("Recycle")
 public class DatabaseActivity extends ListActivity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "DatabaseActivity";
 
 	private DatabaseHelper db;
 	private List<Product> items = new ArrayList<Product>();
 	private ListView list;
 	private boolean adminMode;
 	private EditText searchBar;
 	private String searchQuery;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_database);
 		ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 
 		adminMode = getIntent().getExtras().getBoolean("adminMode");
 
 		DatabaseHelperFactory.init(this);
 		db = DatabaseHelperFactory.getInstance();
 
 		list = (ListView) findViewById(android.R.id.list);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		searchQuery = "";
 		updateSpecialAdapter();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.database, menu);
 		setupSearch(menu);
 		return true;
 	}
 
 	private void setupSearch(Menu menu) {
 		searchBar = (EditText) menu.findItem(R.id.database_menu_search).getActionView();
 		searchBar.setEms(10);
 		searchBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		searchBar.setHint(R.string.database_search_hint);
		searchBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.action_search, 0, 0, 0);
 		searchQuery = "";
 		updateSpecialAdapter();
 
 		/**
 		 * Enabling Search Filter
 		 * */
 		searchBar.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
 				// When a user has changed the text in the search widget, we
 				// update the search query and the special adapter.
 				searchQuery = cs.toString();
 				updateSpecialAdapter();
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				// Not used, but must be implemented.
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// Not used, but must be implemented.
 			}
 		});
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.database_menu_search:
 			// This "hack" is to make sure that the keyboard shows up when the
 			// search bar gains focus. Oh, the things we have to do when we roll
 			// with our own widgets!
 			(new Handler()).postDelayed(new Runnable() {
 
 				public void run() {
 					searchBar.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
 							MotionEvent.ACTION_DOWN, 0, 0, 0));
 					searchBar.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
 							MotionEvent.ACTION_UP, 0, 0, 0));
 				}
 			}, 50);
 			return true;
 		case R.id.database_menu_create:
 			intent = new Intent(this, AddManuallyActivity.class);
 			intent.putExtra("adminMode", adminMode);
 			startActivity(intent);
 			return true;
 		case android.R.id.home:
 			intent = new Intent(this, MainActivity.class);
 			intent.putExtra("adminMode", adminMode);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		ViewGroup currentRow = (ViewGroup) getListView().getChildAt(position);
 
 		TextView nameView = (TextView) currentRow.getChildAt(0);
 		TextView priceView = (TextView) currentRow.getChildAt(1);
 		TextView idView = (TextView) currentRow.getChildAt(2);
 
 		String productName = nameView.getText().toString();
 		Integer productPrice = Integer.parseInt(priceView.getText().toString());
 		String productId = idView.getText().toString();
 
 		Bundle productBundle = new Bundle();
 
 		productBundle.putString("productName", productName);
 		productBundle.putInt("productPrice", productPrice);
 		productBundle.putString("productId", productId);
 
 		productBundle.putBoolean("adminMode", adminMode);
 
 		Intent intent = new Intent(this, ProductActivity.class);
 		intent.putExtras(productBundle);
 		startActivity(intent);
 	}
 
 	/**
 	 * Updates the SpecialAdapter, which in turn updates the view of the list of
 	 * all the items in the database.
 	 */
 	private void updateSpecialAdapter() {
 		if (db.getProducts().size() == 0) {
 			searchBar.setHint(R.string.database_empty);
 			searchBar.clearFocus();
 			searchBar.setFocusableInTouchMode(false);
 			searchBar.setFocusable(false);
 			Intent intent = new Intent(this, EmptyDatabaseActivity.class);
 			intent.putExtra("adminMode", adminMode);
 			startActivity(intent);
 			finish();
 		}
 
 		items = filterList(db.getProducts(), searchQuery);
 		SpecialAdapter adapter = new SpecialAdapter(this, items);
 		list.setAdapter(adapter);
 	}
 
 	/**
 	 * A static class that helps the SpecialAdapter generate the database view.
 	 */
 	static class ViewHolder {
 		TextView name, price, id;
 	}
 
 	/**
 	 * Given a listView containing product information and the edit and delete
 	 * buttons, this function takes the product information and asks the
 	 * database to remove the corresponding product, and then updates the
 	 * Special Adapter that handles the database view.
 	 * 
 	 * @param listView
 	 *            the view containing the information of the product to be
 	 *            removed
 	 */
 	private void deleteItem(ViewGroup listView) {
 		if (adminMode) {
 			ViewGroup currentRow = (ViewGroup) listView.getChildAt(0);
 
 			TextView nameView = (TextView) currentRow.getChildAt(0);
 			TextView idView = (TextView) currentRow.getChildAt(2);
 
 			String name = nameView.getText().toString();
 			String id = idView.getText().toString();
 			db.removeProduct(id);
 
 			Context context = getApplicationContext();
 			CharSequence text = getString(R.string.database_toast_deleted, name);
 
 			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
 			toast.show();
 
 			// TODO Add confirmation dialog?
 
 			updateSpecialAdapter();
 		}
 	}
 
 	/**
 	 * A special adapter that generates the view that shows the items in the
 	 * database.
 	 */
 	private class SpecialAdapter extends BaseAdapter {
 		// Defining the background color of rows. The row will alternate between
 		// grey light and grey dark.
 		private int[] colors = new int[] { 0xAA999999, 0xAA7d7d7d };
 		private LayoutInflater mInflater;
 
 		// The variable that will hold our text data to be tied to list.
 		private List<Product> data;
 
 		public SpecialAdapter(Context context, List<Product> items) {
 			mInflater = LayoutInflater.from(context);
 			this.data = items;
 		}
 
 		@Override
 		public int getCount() {
 			return data.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		/**
 		 * Given a view, this function returns a view that shows each item in
 		 * the database in a top-down fashion. Every other item has a darker
 		 * gray background, in order to more easily differentiate between each
 		 * item.
 		 * 
 		 * @param int position
 		 * @param View
 		 *            convertView The view to add all the items to.
 		 * @param ViewGroup
 		 *            parent Not used, but this function overrides another
 		 *            function so it stays
 		 * 
 		 * @return The finished view that shows all the items in the database.
 		 */
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			// A ViewHolder keeps references to children views to avoid
 			// unnecessary calls to findViewById() on each row.
 			ViewHolder holder;
 
 			if (convertView == null) {
 				convertView = mInflater.inflate(R.layout.row, null);
 
 				holder = new ViewHolder();
 				holder.name = (TextView) convertView.findViewById(R.id.name);
 				holder.price = (TextView) convertView.findViewById(R.id.price);
 				holder.id = (TextView) convertView.findViewById(R.id.id);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 
 			// Bind the data efficiently with the holder.
 			holder.name.setText(data.get(position).getName());
 			holder.price.setText("" + data.get(position).getPrice());
 			holder.id.setText(data.get(position).getBarcode());
 
 			// Set the background color depending of odd/even colorPos result
 			int colorPos = position % colors.length;
 			convertView.setBackgroundColor(colors[colorPos]);
 
 			return convertView;
 		}
 	}
 
 	private List<Product> filterList(List<Product> list, String s) {
 		if (s.equals("")) {
 			return list;
 		}
 		List<Product> newList = new ArrayList<Product>();
 		for (Product p : list) {
 			if (p.getName().toLowerCase(Locale.ENGLISH).contains(s.toLowerCase(Locale.ENGLISH))) {
 				newList.add(p);
 			}
 		}
 		return newList;
 	}
 }
