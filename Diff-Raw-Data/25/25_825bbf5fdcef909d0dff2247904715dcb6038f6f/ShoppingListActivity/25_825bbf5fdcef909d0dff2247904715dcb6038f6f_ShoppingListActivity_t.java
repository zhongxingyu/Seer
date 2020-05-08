 package com.openfridge;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ShoppingListActivity extends Activity {
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         FridgeFoodDataClient client = new FridgeFoodDataClient();
 		try {
 			client.reloadFoods();
 		} catch (Exception e) {
 			// For debugging
 			e.printStackTrace();
 		}
 		List<FridgeFood> good = Arrays.asList(new FridgeFood("2011-10-28",
 				"Milk", "2011-11-01", "2", "2011-10-28", "1"), new FridgeFood(
 				"2011-10-28", "Eggs", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Leftovers", "2011-11-01", "2",
 						"2011-10-28", "1"), new FridgeFood("2011-10-28",
 						"Kale", "2011-11-01", "2", "2011-10-28", "1"),
 				new FridgeFood("2011-10-28", "Beef", "2011-11-01", "2",
 						"2011-10-28", "1")
 
 		);
 
         setContentView(R.layout.shopping_list);
 		initFridgeFoodListView(R.id.shoppingLV, good);
 
 	}
 	private void initFridgeFoodListView(int viewId, List<FridgeFood> foods) {
 		ListView listView = (ListView) findViewById(viewId);
 		listView.setTextFilterEnabled(true);
 		listView.setAdapter(new ArrayAdapter<FridgeFood>(this,
 				R.layout.list_item_with_remove, R.id.text, foods));
 		
 		// Make items not focusable to avoid listitem / button conflicts
 		listView.setItemsCanFocus(false);
 	}
 	public void removeItem(View view) {
 		startActivity(MainMenuActivity.expire);
 	}
 
 	public void loadItemEdit(View view) {
 		startActivity(MainMenuActivity.itemEdit);
 	}
 }
