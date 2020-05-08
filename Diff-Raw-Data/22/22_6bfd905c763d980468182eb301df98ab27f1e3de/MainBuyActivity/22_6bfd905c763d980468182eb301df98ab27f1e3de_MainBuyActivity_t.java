 package com.studentsaleapp.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.studentsaleapp.R;
 import com.studentsaleapp.backend.BackendModel;
 import com.studentsaleapp.backend.SaleItem;
 
 
 @SuppressLint("NewApi")
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class MainBuyActivity extends Activity implements OnItemClickListener {
 
 	/** Static data to populate the test application, 
 	 * for use when the backend is disabled
 	 */
 	public static final String[] titles = new String[] { 
 		"Microwave Oven",
 		"Toaster", 
 		"Desk Lamp", 
 		"Desk", 
 		"Desk Chair", 
 		"Couch", 
 		"Coffee Table", 
 		"Book Shelf" 
 	};
 
 	public static final String[] descriptions = new String[] {
 		"Just a microwave",
 		"Burns a little but if it is watched it is fine",
 		"Turns On and Off, currently has a cool white light bulb in it",
 		"Basic desk four legs and a surface",
 		"Gives Support in all the right places",
 		"Great for a lounge room, quite comfortable, just don't have enough room in the apartment",
 		"Perfect height for a lazy dinner table",
 		"Dimentions: 80L 120H 50D, great condition" 
 	};
 
 	public static final int[] images = new int[] { 
 		R.drawable.microwave800px, 
 		R.drawable.toaster,
 		R.drawable.desklamp,
 		R.drawable.desk,
 		R.drawable.deskchair, 
 		R.drawable.couch,
 		R.drawable.coffeetable,
 		R.drawable.bookshelf,
 	};
 
 	public static final String[] price = new String[] {
 		"$10.00",
 		"$5.50",
 		"$5.30",
 		"$6.00",
 		"$5.00",
 		"$5.00",
 		"$5.00",
 		"$5.00"
 	};
 
 	public static final String[] contact = new String[] {
 		"0430 782 781",
 		"0430 782 782",
 		"0430 782 783",
 		"0430 782 784",
 		"0430 782 785",
 		"0430 782 786",
 		"0430 782 787",
 		"0430 782 788"
 	};
 
 	public static final String[] location = new String[] {
 		"St. Lucia",
 		"Toowong",
 		"Auchenflower",
 		"Beenleigh",
 		"Brisbane CBD",
 		"South Bank",
 		"West End",
 		"Nerang"
 	};
 	/** ----- end static data -----*/
 	
 	/** The adapter list view */
 	private ListView adapterListView;
 	
 	/** The row of items list */
 	private List<BuyRowItem> rowItems;
 
 	/** The backend model */
 	//TODO: Static data used instead of the backend model
 	private BackendModel model;
 
 	@SuppressLint("NewApi")
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Setup the layout
 		setContentView(R.layout.buy_layout);
 		
 		// Get the backend model
 		MainApplication appState = (MainApplication)getApplicationContext();
 		model = appState.getBackendModel();
 
 		// Populate the row items
 		rowItems = new ArrayList<BuyRowItem>();
         ArrayList<SaleItem> temp_rowItems = model.getItemList();
         // Temporary counter to go through static data as images & locations not yet in database.
         int temp_counter = 0;
 		for (SaleItem item : temp_rowItems) {
            rowItems.add(new BuyRowItem(
                    images[temp_counter],
                    item.getTitle(),
                    item.getDescription(),
                    formatPrice(item.getPrice()),
                    item.getContact(),
                    location[temp_counter]
            ));
             temp_counter++;
         }
 
 		// Setup the adapter
 		adapterListView = (ListView) findViewById(R.id.list);
 		BuyListViewAdapter adapter = new BuyListViewAdapter(this,
 				R.layout.single_buy_row_layout, rowItems);
 		adapterListView.setAdapter(adapter);
 		adapterListView.setOnItemClickListener(this);
 	}
 
 	/**
 	 * Creates, populates and starts the single item activity once an item is clicked
 	 */
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 		// Get the item values
 		final int iconimages = images[position];
 		String product = ((TextView) view.findViewById(R.id.title)).getText().toString();
 		String desc = ((TextView) view.findViewById(R.id.desc)).getText().toString();
 		String price = ((TextView) view.findViewById(R.id.price)).getText().toString();
 		String contact = ((TextView) view.findViewById(R.id.contact)).getText().toString();
 		String location = ((TextView) view.findViewById(R.id.location)).getText().toString();
 
 		// Create, populate and start the single item activity
 		Intent singleItem = new Intent(getApplicationContext(), SingleBuyListItemActivity.class);
 		singleItem.putExtra("product", product);
 		singleItem.putExtra("desc", desc);
 		singleItem.putExtra("price", price);
 		singleItem.putExtra("contact", contact);
 		singleItem.putExtra("location", location);
 		singleItem.putExtra("iconimages", iconimages);
 		startActivity(singleItem);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.buy_options_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	} 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch(item.getItemId()) {
 			case R.id.buy_option:
 				return true;
 			case R.id.sell_option:
 				Intent sellIntent = new Intent(this, SellActivity.class);
 				startActivity(sellIntent);
 				return true;
 		}
 		return (super.onOptionsItemSelected(item));
 	}

    private String formatPrice(double price) {
        StringBuffer formattedPrice = new StringBuffer(Double.toString(price));
        formattedPrice.insert(0, '$');
        if (formattedPrice.indexOf(".") < 0) {
            formattedPrice.append(".00");
        }

        return formattedPrice.toString();
    }
 }
