 package com.openfridge;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class ExpirationListActivity extends Activity {
 	//Tag for debug log
 	private static final String DEBUG_TAG = "Openfridge";

 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Log.d(DEBUG_TAG, "checkpoint");
 
 		//This code gets the xml and sets up the SAX Parser
 		try {
 			FridgeFoodDataClient client = new FridgeFoodDataClient();
 
 			setContentView(R.layout.expiration_list);
 
 			//Attach our data Array to the listview
 
 			//TODO: need to have 3 separate arrays, but this should
 			//be wrapped in the DataClient class, not here.
 			//Setup the Listview
 			
 			initFridgeFoodListView(R.id.pastLV, client.getPastFoods(), 
 			        new PastFridgeItemClickListener());
 			initFridgeFoodListView(R.id.nearLV, client.getPastFoods(), 
                     new PastFridgeItemClickListener());
 			initFridgeFoodListView(R.id.goodLV, client.getPastFoods(), 
                     new PastFridgeItemClickListener());
 		} catch (Exception e) {
 			// For debugging
 			e.printStackTrace();
 		}
 
 	}
 	
 	private void initFridgeFoodListView(int viewId, ArrayList<FridgeFood> foods,
 	        OnItemClickListener listener) {
 	    ListView listView = (ListView) findViewById(viewId);
         listView.setChoiceMode(2);   //CHOICE_MODE_MULTIPLE
         listView.setTextFilterEnabled(true);
         listView.setAdapter(new ArrayAdapter<FridgeFood>(this,
                 android.R.layout.simple_list_item_checked, foods));
         listView.setOnItemClickListener(listener);
 	}
 }
