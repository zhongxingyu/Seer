 package com.github.barcodescanner.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.barcodescanner.R;
 import com.github.barcodescanner.database.DatabaseHelper;
 import com.github.barcodescanner.database.DatabaseHelperFactory;
 import com.github.barcodescanner.database.Product;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class DatabaseActivity extends Activity {
 
 	@SuppressWarnings("unused")
 	private static final String TAG = "DatabaseActivity";
 	
 	DatabaseHelper db;
 	private List<Product> items = new ArrayList<Product>();
 	private ListView list;
 	private boolean isOwner;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_database);
 		
 		isOwner = getIntent().getExtras().getBoolean("isOwner");
 
 		DatabaseHelperFactory.init(this);
 		db = DatabaseHelperFactory.getInstance();
 
 		list = (ListView) findViewById(R.id.list);
 		items = db.getProducts();
 		SpecialAdapter adapter = new SpecialAdapter(this, items);
 		list.setAdapter(adapter);
 
 	}
 
 	static class ViewHolder {
 		TextView name, id, price;
 	}
 
 	/**
 	 * When called, this function deletes an item given from a view from the database
 	 * and the SpecialAdapter generated view.
 	 * 
 	 * @param v The view that shows the item in the database
 	 */
 	public void deleteItem(View v) {
 		if (isOwner) {
 			ImageButton button = (ImageButton) v;
 			TableRow row = (TableRow) button.getParent();
 			TextView idView = (TextView) row.getChildAt(2);
 			String id = idView.getText().toString();
 			db.removeProduct(id);
 			
 			items = db.getProducts();
 			SpecialAdapter adapter = new SpecialAdapter(this, items);
 			list.setAdapter(adapter);
 		}
 	}
 	
 	public void editItem(View v) {
 		if (isOwner) {
			// TODO Code this, and get a matching edit button graphic from the android resources.
			// Also do that for the delete button.
 		}
 	}
 
 	/**
 	 * A special adapter that generates the view that shows the items in the database.
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
 		 * Given a view, this function returns a view that shows each item in the database in a top-down fashion.
 		 * Every other item has a darker gray background, in order to more easily differentiate
 		 * between each item.
 		 * 
 		 * @param int position 
 		 * @param View convertView The view to add all the items to.
 		 * @param ViewGroup parent Not used, but this function overrides another function so it stays
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
 			
 			// if a customer is viewing the database, hide the delete button
 			if (!isOwner) {
 				convertView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
 			}
 			
 			return convertView;
 		}
 	}
 }
