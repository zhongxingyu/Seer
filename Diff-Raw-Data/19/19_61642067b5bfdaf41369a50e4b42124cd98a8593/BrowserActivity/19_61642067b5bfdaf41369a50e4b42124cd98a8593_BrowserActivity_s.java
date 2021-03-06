 package pro.dbro.timelapse;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class BrowserActivity extends SherlockListActivity {
 	
 	// These parallel arrays map plain-text timelapse properties to view resource IDs within a ListView item
 	private static final String[] BROWSER_LIST_ITEM_KEYS = {"title", "body", "timelapse", "thumbnail"};
 	private static final int[] BROWSER_LIST_ITEM_VALUES = {R.id.list_item_headline, R.id.list_item_body, R.id.list_item_container, R.id.list_item_image};
 	
 	// The adapter which connects the application data to the ListView
 	SimpleAdapter browserAdapter;
 	
 	public static TimeLapseApplication c;
 	
 	/** Called when the activity is first created. */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.browser);
         
         c = (TimeLapseApplication)getApplicationContext();
 
         // Establish LocalBroadcastManager for communication with other Classes
         LocalBroadcastManager.getInstance(this).registerReceiver(browserActivityMessageReceiver,
       	      new IntentFilter(String.valueOf(R.id.browserActivity_message)));
         
         // Load Timelapses from external storage
         Log.d("OnCreate","Beginning filesystem read");
         new FileUtils.ParseTimeLapsesFromFilesystem().execute("");
     }
     
     public static TimeLapseApplication getContext() {
         return c;
     }
     
     // Handle listview item select
     @Override 
     public void onListItemClick(ListView l, View v, int position, long id) {
         if(((String)v.getTag(R.id.view_onclick_action)).equals("camera")){
         	//  launch CameraActivity
         	Intent intent = new Intent(BrowserActivity.this, CameraActivity.class);
         	intent.putExtra("timelapse_id", (Integer)v.getTag(R.id.view_related_timelapse));
             startActivity(intent);
         }
         else if(((String)v.getTag(R.id.view_onclick_action)).equals("view")){
         	Intent intent = new Intent(BrowserActivity.this, TimeLapseViewerActivity.class);
         	intent.putExtra("timelapse_id", (Integer)v.getTag(R.id.view_related_timelapse));
             startActivity(intent);
         }
     }
     
     // Populate ActionBar
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.layout.browser_menu, menu);
         return true;
     }
     
     // Handle ActionBar Events
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_add:
             	// Go to TimelapseViewer with new TimeLapse
             	Intent intent = new Intent(BrowserActivity.this, TimeLapseViewerActivity.class);
             	intent.putExtra("timelapse_id", -1); // indicate TimeLapseViewerActivity to create a new TimeLapse
                 startActivity(intent);
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     protected void onResume(){
     	super.onResume();
     	
     	//TimeLapseApplication tla = (TimeLapseApplication)getApplicationContext();
     	//ArrayList<TimeLapse> valuesList = new ArrayList<TimeLapse>(tla.time_lapse_map.values());
     	//Log.d("BrowserActivity","onResume. Populating Listview: " + valuesList.toString());
     	//populateListView(valuesList);
     }
     
     // TimeLapseViewerActivity starts this activity with a bundled intent message to update view
     // Deprecated: FileUtils.SaveTimeLapseOnFilesystem will signal re-draw
    /*
     @Override
     protected void onNewIntent (Intent intent){
     	Log.d("onNewIntent","called");
     	if (( intent.hasExtra("updateListView"))){
     		TimeLapseApplication tla = (TimeLapseApplication)getApplicationContext();
         	ArrayList<TimeLapse> valuesList = new ArrayList<TimeLapse>(tla.time_lapse_map.values());
         	Log.d("onNewIntent","Refreshing Listview: " + valuesList.toString());
         	populateListView(valuesList);
     	}
     }
     */
     
     @Override
     protected void onPause(){
     	super.onPause();
     	Log.d("BrowserActivity","onPause");
     }
     
     @Override
 	protected void onDestroy() {
 	  // Unregister since the activity is about to be closed.
 	  LocalBroadcastManager.getInstance(this).unregisterReceiver(browserActivityMessageReceiver);
 	  super.onDestroy();
 	}
     
     // Receives messages from other components
     // i.e: when the application state is done being read from the filesystem
     private BroadcastReceiver browserActivityMessageReceiver = new BroadcastReceiver() {
     	  @Override
     	  public void onReceive(Context context, Intent intent) {
     	    // Populate ListView with received data
     		  int type = intent.getIntExtra("type", -1);
     		if(type != -1){
     			if(type == R.id.filesystem_parse_complete){
 		    		Log.d("Broadcast Receiver", "Received filesystem read result: " + ((ArrayList<TimeLapse>) intent.getSerializableExtra("result")).toString());
 		    		TimeLapseApplication  tla = (TimeLapseApplication)getApplicationContext();
 		    		tla.setTimeLapses((ArrayList<TimeLapse>) intent.getSerializableExtra("result"));
 		    	    populateListView((ArrayList<TimeLapse>) intent.getSerializableExtra("result"));
     			}
     			else if(type == R.id.filesystem_modified){
     				Log.d("BroadcastReceiver","Smart ListView refresh");
     				int timelapse_id = intent.getIntExtra("timelapse_id", -1);
     				if (timelapse_id == -1)
     					return;
     				TimeLapseApplication  tla = (TimeLapseApplication)getApplicationContext();
     				TimeLapse timelapse = tla.time_lapse_map.get(timelapse_id);
     				
     				int view_id = Integer.parseInt(String.valueOf(timelapse_id));
     				RelativeLayout browser_list_item = ((RelativeLayout) findViewById(view_id));
     				((TextView)browser_list_item.findViewById(R.id.list_item_headline)).setText(timelapse.name);
     				((TextView)browser_list_item.findViewById(R.id.list_item_body)).setText(timelapse.description);
     				if(timelapse.image_count != 0){
 	    				Bitmap thumb_bitmap = BitmapFactory.decodeFile(timelapse.thumbnail_path);
 	    	    	    ((ImageView)browser_list_item.findViewById(R.id.list_item_image)).setImageBitmap(thumb_bitmap);
     				}
     			}
     		}
     	  }
     };
     
     private void populateListView(ArrayList<TimeLapse> data){
     	// Populate ListView
         // (Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to)
         browserAdapter = new SimpleAdapter(this.getApplicationContext(), loadItems(data), R.layout.browser_list_item, BROWSER_LIST_ITEM_KEYS, BROWSER_LIST_ITEM_VALUES);
         browserAdapter.setViewBinder(new BrowserViewBinder());
         setListAdapter(browserAdapter);
     }
     
     // Create Map describing ListView contents. Fed as "data" to SimpleAdapter constructor
     private List<Map<String, String>> loadItems(ArrayList<TimeLapse> list){
     	// Sort TimeLapse list by modified date
     	Collections.sort(list, new TimeLapse.TimeLapseComparator());
     	List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
     	
     	// Relate ListView row element identifiers to TimeLapse fields
     	for(int x = 0; x < list.size();x++){
     		HashMap<String, String> itemMap = new HashMap<String, String>();
     		itemMap.put("title", list.get(x).name);
     		itemMap.put("body", list.get(x).description);
     		itemMap.put("timelapse", String.valueOf(list.get(x).id));
     		if(list.get(x).image_count > 0){
     			File thumb_dir = new File(FileUtils.getOutputMediaDir(list.get(x).id), "thumbnails");
     			File thumb_image = new File(thumb_dir, String.valueOf(list.get(x).image_count)+"_thumb.jpeg");
     			if(thumb_image.exists())
     				itemMap.put("thumbnail", thumb_image.getAbsolutePath());
     		}
     		mapList.add(itemMap);
     	}
     	Log.d("maplist_in",list.toString());
     	Log.d("maplist_out",mapList.toString());
     	return mapList;
     }
 
 }
