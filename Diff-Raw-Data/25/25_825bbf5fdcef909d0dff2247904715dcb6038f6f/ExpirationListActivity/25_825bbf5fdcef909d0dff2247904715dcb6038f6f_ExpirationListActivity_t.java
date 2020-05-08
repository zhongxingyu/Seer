 package com.openfridge;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 public class ExpirationListActivity extends Activity {
 	private static final int ROW_HEIGHT = 100;
 
 	// Tag for debug log
 	private static final String DEBUG_TAG = "Openfridge";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.d(DEBUG_TAG, "checkpoint");
 
 		// This code gets the xml and sets up the SAX Parser
 		FridgeFoodDataClient client = new FridgeFoodDataClient();
 		try {
 			client.reloadFoods();
 		} catch (Exception e) {
 			// For debugging
 			e.printStackTrace();
 		}
 		setContentView(R.layout.expiration_list);
 		
 		/*
 		// Setup the Listview
 		List<FridgeFood> good = Arrays.asList(new FridgeFood("2011-10-28",
 				"Milk", "2011-11-01", "2", "2011-10-28", "1"), new FridgeFood(
 				"2011-10-28", "Eggs", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Leftovers", "2011-11-01", "2",
 						"2011-10-28", "1"), new FridgeFood("2011-10-28",
 						"Kale", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Beef", "2011-11-01", "2",
 						"2011-10-28", "1")
 
 		);
 		List<FridgeFood> nearly = Arrays.asList(new FridgeFood("2011-10-28",
 				"Leftovers", "2011-11-01", "1", "2011-10-28", "1"));
 		List<FridgeFood> expired = Collections.<FridgeFood> emptyList();
 		*/
 		
 		List<FridgeFood> good = client.getGoodFoods();
 		List<FridgeFood> nearly = client.getNearFoods();
 		List<FridgeFood> expired = client.getExpiredFoods();
 		Log.d(DEBUG_TAG, "number of good items: " + good.size());
 		
 		initFridgeFoodListView(R.id.pastLV, expired,
 				new PastFridgeItemClickListener());
 		initFridgeFoodListView(R.id.nearLV, nearly,
 				new PastFridgeItemClickListener());
 		initFridgeFoodListView(R.id.goodLV, good,
 				new PastFridgeItemClickListener());
 	}
 
 	private void initFridgeFoodListView(int viewId, List<FridgeFood> foods,
 			OnItemClickListener listener) {
 		ListView listView = (ListView) findViewById(viewId);
 		listView.setTextFilterEnabled(true);
 		listView.setAdapter(new ArrayAdapter<FridgeFood>(this,
 				R.layout.list_item_with_remove, R.id.text, foods));
 		listView.setOnItemClickListener(listener);
 
 		// Make items not focusable to avoid listitem / button conflicts
 		listView.setItemsCanFocus(false);
 
 		ListAdapter listAdapter = listView.getAdapter();
 
 		int rows = listAdapter.getCount();
 		// int height = android.R.attr.listPreferredItemHeight * rows;
 		// for some reason listPreferredItemHeight didn't have a reasonable
 		// value...
 		int height = ROW_HEIGHT * rows;
 
 		ViewGroup.LayoutParams params = listView.getLayoutParams();
 		params.height = height;
 		listView.setLayoutParams(params);
 		listView.requestLayout();
 	}
 
 	public void removeItem(View view) {
 		startActivity(MainMenuActivity.expire);
 	}
 
 	public void loadItemEdit(View view) {
 		startActivity(MainMenuActivity.itemEdit);
 	}
 }
