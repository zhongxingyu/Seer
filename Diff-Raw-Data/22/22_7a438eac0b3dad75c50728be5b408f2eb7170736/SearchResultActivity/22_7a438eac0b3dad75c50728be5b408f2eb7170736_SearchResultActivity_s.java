 package com.qut.spc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.qut.spc.model.*;
 import com.qut.spc.task.ListRequestTask;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 import android.support.v4.app.NavUtils;
 
 public class SearchResultActivity extends Activity {
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search_result);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
 	    String url = getIntent().getStringExtra("url");
 	    String component = getIntent().getStringExtra("component"); 
 	    if (component.equals("panel")) {
 	    	new ComponentListTask<Panel>("panel", Panel.class).execute(url);
 	    } else if (component.equals("inverter")) {
 	    	new ComponentListTask<Inverter>("inverter", Inverter.class).execute(url);
 	    } else if (component.equals("battery")) {
 	    	new ComponentListTask<Battery>("battery", Battery.class).execute(url);
 	    }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_search_result, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
 	class ComponentListTask<T extends SolarComponent> extends ListRequestTask<T> {
 
 		public ComponentListTask(String componentName, Class<T> componentClass) {
 			super(componentName, componentClass);
 		}
 		
 		@Override
 		protected void onPostExecute(List<T> list) {
 			if (list == null) {
 				return;
 			}
 			List<HashMap<String, String>> vwList = new ArrayList<HashMap<String, String>>();
 
 			for (T component : list) {
 				HashMap<String, String> item = new HashMap<String, String>();
 
 				item.put("id", Long.toString(component.getId()));
 				item.put("model", component.getModel());
 				item.put("manufacturer", component.getManufacturer());
 				item.put("price", Double.toString(component.getPrice()));
 				item.put("capacity", Double.toString(component.getCapacity()));
 				vwList.add(item);
 			}
 			String[] from = new String[] { "model", "manufacturer", "price",
 					"capacity" };
 			int[] to = new int[] { R.id.model, R.id.manufacturer, R.id.price,
 					R.id.capacity, };
 			ListAdapter adapter = new SimpleAdapter(SearchResultActivity.this,
 					vwList, R.layout.component_list, from, to);
 
//			setListAdapter(adapter);
 		}
 	}
 }
