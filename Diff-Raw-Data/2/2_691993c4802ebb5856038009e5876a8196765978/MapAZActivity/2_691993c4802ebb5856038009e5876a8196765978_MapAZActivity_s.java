 package com.example.wifimanagerprototype;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MapAZActivity extends Activity {
 	private final ArrayList<String> listAll = new ArrayList<String>();
 	private final ArrayList<String> listPublic = new ArrayList<String>();
 	private final ArrayList<String> listPrivate = new ArrayList<String>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_map_az);
 		
 		ListView listview = (ListView) findViewById(R.id.listview_az);
 		//final ArrayList<String> list = new ArrayList<String>();
 		this.listAll.add(getString(R.string.network_eduroam));
 		this.listAll.add(getString(R.string.network_wifi));
 		this.listPublic.add(getString(R.string.network_wifi));
 		this.listPrivate.add(getString(R.string.network_eduroam));
 		//list.add("Eduroam");
 		//list.add("WiFi");
 		
 		listview.setAdapter(new StableArrayAdapter(this, android.R.layout.simple_list_item_1, this.listAll));
 		
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				//final String item = (String) parent.getItemAtPosition(position);
 				loadHeatmap(null);
 			}
 		});
 		
 		listview.setOnTouchListener(new OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if (event.getAction() == MotionEvent.ACTION_MOVE) {
                     return true;
                 }
                 return false;
             }
 
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	case R.id.action_home:
         		startActivity(new Intent(this, MainActivity.class));
         		return true;
         	case R.id.action_az:
                 return true;
         	case R.id.action_favorite:
         		startActivity(new Intent(this, MapFavoriteActivity.class));
                 return true;
         	case R.id.action_relocate:
         		startActivity(new Intent(this, MapRelocateActivity.class));
                 return true;
             case R.id.action_settings:
             	startActivity(new Intent(this, SettingsActivity.class));
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 	
 	public void loadHeatmap(View view) {
     	startActivity(new Intent(this, MapGreenActivity.class));
     }
 	
 	public void filterAll(View view) {
 		setListAdapter(this.listAll);
 		findViewById(R.id.select_all).setBackgroundResource(R.drawable.search_type_active);
 		findViewById(R.id.select_public).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_private).setBackgroundResource(R.drawable.search_type);
 	}
 	
 	public void filterPublic(View view) {
 		setListAdapter(this.listPublic);
 		findViewById(R.id.select_all).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_public).setBackgroundResource(R.drawable.search_type_active);
 		findViewById(R.id.select_private).setBackgroundResource(R.drawable.search_type);
 	}
 	
 	public void filterPrivate(View view) {
 		setListAdapter(this.listPrivate);
 		findViewById(R.id.select_all).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_public).setBackgroundResource(R.drawable.search_type);
 		findViewById(R.id.select_private).setBackgroundResource(R.drawable.search_type_active);
 	}
 	
 	/** Takes an Array of Strings and sets the ListView to pull it's data from
 	 ** that array.
 	 **/
 	private void setListAdapter(ArrayList<String> list) {
 		// Add some rows
 	    /*List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
 
 	    HashMap<String, Object> map = new HashMap<String, Object>();
 	    map.put("title", "First title"); // This will be shown in R.id.title
 	    map.put("description", "description 1"); // And this in R.id.description
 	    fillMaps.add(map);
 
 	    map = new HashMap<String, Object>();
 	    map.put("title", "Second title");
 	    map.put("description", "description 2");
 	    fillMaps.add(map);
 
 	    SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.row, from, to);
 	    setListAdapter(adapter);*/
 		
 		ListView listview = (ListView) findViewById(R.id.listview_az);
 		listview.setAdapter(new StableArrayAdapter(this, android.R.layout.simple_list_item_1, list));
 	}
 	
 	/** Code from http://www.vogella.com/articles/AndroidListView/article.html **/
 	private class StableArrayAdapter extends ArrayAdapter<String> {
 		HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
 		List<String> data = null;
 
 	    public StableArrayAdapter(Context context, int textViewResourceId,
 	    		List<String> objects) {
 	    	super(context, textViewResourceId, objects);
 	    	this.data = objects;
 	    	for (int i = 0; i < objects.size(); ++i) {
 	    		mIdMap.put(objects.get(i), i);
 	    	}
 	    }
 
 	    @Override
 	    public long getItemId(int position) {
 	    	String item = getItem(position);
 	    	return mIdMap.get(item);
 	    }
 
 	    @Override
 	    public boolean hasStableIds() {
 	    	return true;
 	    }
 	    
 	    /** This stuff doesn't work :( **/
 	    
 	    @Override
 	    public View getView(int position, View convertView, ViewGroup parent) {
 	    	ViewHolder holder = null;
 	    	LayoutInflater inflator = getLayoutInflater();
 	    	if(convertView == null) {
	    		convertView = inflator.inflate(R.layout.list_networks, parent);
 	    		holder = new ViewHolder(convertView);
 	    		convertView.setTag(holder);
 	    	} else {
 	    		holder = (ViewHolder) convertView.getTag();
 	    	}
 	    	holder.getMainText().setText(data.get(position));
 	    	holder.getDistanceText().setText(data.get(position));
 	    	return convertView;
 	    }
 	    
 		private class ViewHolder {
 			private View row;
 			private TextView mainText = null, distanceText = null;
 
 			public ViewHolder(View row) {
 				this.row = row;
 			}
 
 			public TextView getMainText() {
 				if (this.mainText == null) {
 					this.mainText = (TextView) this.row.findViewById(R.id.list_text);
 				}
 				return this.mainText;
 			}
 
 			public TextView getDistanceText() {
 				if (this.distanceText == null) {
 					this.distanceText = (TextView) this.row.findViewById(R.id.list_distance);
 				}
 				return this.distanceText;
 			}
 		}
 	}
 
 }
