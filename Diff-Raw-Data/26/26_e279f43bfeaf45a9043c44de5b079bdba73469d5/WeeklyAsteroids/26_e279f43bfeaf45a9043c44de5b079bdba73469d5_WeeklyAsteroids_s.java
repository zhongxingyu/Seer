 package ie.ucd.asteroid;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class WeeklyAsteroids extends Activity {
 
 	DBAdapter db = new DBAdapter(this);
 	Calc calc = new Calc();
 	//array to store the asteroid list for display
 	ArrayList<String> asteroidList = new ArrayList<String>(); 
 	ArrayList<Double> asteroidSizeList = new ArrayList<Double>();
 	ArrayList<Integer> asteroidIdList = new ArrayList<Integer>();
 	ArrayList<Boolean> asteroidNotifications = new ArrayList<Boolean>();
 
 	private static final String WEEKLYASTEROIDS_TAG = "Weekly_Asteroids";
 	private ListView listView1;
 	private Double approxDiameter;
 	private int asteroidSize;
 	private SharedPreferences.Editor putPrefs;
 	private SharedPreferences getPrefs;
 	private final static String prefName = "SelectAllState";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_weekly_asteroids);
 		
 		//Code for getting data from database: 
 		db.openDB();
 		Cursor c = db.getUpcomingAsteroids();
 		if (c.moveToFirst()) {
 			do {
 				// get info from database
 				Integer objectId = db.getAsteroidID(c);
 				String objectName = db.getName(c);
 				String approachDate = db.getApproachDateDisplay(c);
 				Double maxDiameter = db.getMaxDiameter(c);
 				Double minDiameter = db.getMinDiameter(c);
 				Boolean notificationState = (db.getNotificationState(c) == 1);
 
 				
 				Log.w(WEEKLYASTEROIDS_TAG, "Id, Name, Date, Diams: " + objectId + " " + objectName + " " 
 				+ approachDate + " " + maxDiameter + " "+ minDiameter);
 				
 				//get approximate diameter calculated from min & max diameters
 				approxDiameter = (minDiameter + maxDiameter) / 2;
 				Log.w(WEEKLYASTEROIDS_TAG, "Approx diameter calculated: " + approxDiameter);
 				
 				//add data to arrays
 				asteroidList.add(objectName + "    "  + approachDate);
 				asteroidSizeList.add(approxDiameter);
 				asteroidIdList.add(objectId);
 				asteroidNotifications.add(notificationState);
 
 			} while (c.moveToNext());
 		}
 		c.close(); // Close cursor
 		db.closeDB();
 
 		//put database data into the listview: 
 		Asteroids[] asteroid_data = new Asteroids[asteroidList.size()];
 
 		for(int i = 0; i<asteroidList.size(); i++){
 			
 			asteroidSize = calc.asteroidSizeDecider(asteroidSizeList.get(i), false);
 			asteroid_data[i] = new Asteroids(asteroidSize, asteroidList.get(i), asteroidNotifications.get(i), asteroidIdList.get(i));
 
 		}
 		
 		final LvAsteroidAdapter lvAdapter = new LvAsteroidAdapter(this, R.layout.listview_item_row, asteroid_data);
 		
 		int size = asteroid_data.length;
 		lvAdapter.setIdList(asteroid_data, size);
 		lvAdapter.setCheckboxState(asteroid_data, size);
 		
 		listView1 = (ListView) findViewById(R.id.listView1);
 
 		View header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
 		listView1.addHeaderView(header); 
 		listView1.setAdapter(lvAdapter);
 		listView1.setCacheColorHint(0); 
 		
 		//saving state of checkboxes
 		lvAdapter.setNotifyIcon(listView1);
 		getPrefs = getSharedPreferences(prefName, MODE_PRIVATE);
 		lvAdapter.setSelectAll(getPrefs.getBoolean("selectAll", false));
 		putPrefs = getSharedPreferences(prefName, MODE_PRIVATE).edit();
 		CheckBox selectAll = (CheckBox)findViewById(R.id.notifyicon);
 		selectAll.setChecked(getPrefs.getBoolean("state", false));
 		selectAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton checkboxView, boolean isChecked)
 			{
 				if (isChecked)
 				{
 					lvAdapter.setAllCheckbox(true);
 					lvAdapter.setSelectAll(true);
 					// Update the ListView to the new changes
 					lvAdapter.notifyDataSetChanged();
 					putPrefs.putBoolean("state", true);
 				}
 				else
 				{
 					// Stop notifications only when 'select all' box checked
 					if (lvAdapter.getSelectAll() == true)
 					{
 						lvAdapter.setAllCheckbox(false);
 						lvAdapter.setSelectAll(false);
 						lvAdapter.notifyDataSetChanged();
 					}
 					putPrefs.putBoolean("state", false);
 				}
 				putPrefs.putBoolean("selectAll", lvAdapter.getSelectAll());
 				putPrefs.commit();
 			}
 		});
 		
 		setFonts();
 		
 		
 		//intent to open asteroid Info activity, done this way as not extending listactivity. 
 		final Intent intent_ViewAsteroidInfo = new Intent(this, AsteroidInfo.class);
 		listView1.setOnItemClickListener(new OnItemClickListener(){
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
 				Log.w(WEEKLYASTEROIDS_TAG, "Pos is " + pos);
 				Log.w(WEEKLYASTEROIDS_TAG, "Id is " + id);
 				
 				int listId = (int) id;
 				
 		    	//bundle listview details
 		    	Bundle extras = new Bundle();
 		    	int astId = lvAdapter.getId(listId);
 		    	extras.putInt("pos", astId);
 		    	intent_ViewAsteroidInfo.putExtras(extras);
 		    	startActivity(intent_ViewAsteroidInfo);
 			}
 			});
 
 	}
 
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.activity_weekly_asteroids, menu);
			return true;
		}

 	//code for using custom fonts
 		public void setFonts(){
 
 			//set the desired typeface in that view. 
 			TextView weeklyTv= (TextView) findViewById(R.id.weekly);
 			Typeface weeklyFace=Typeface.createFromAsset(getAssets(),"fonts/Roboto-CondensedItalic.ttf");
 			weeklyTv.setTypeface(weeklyFace);
 
 			TextView notifyTv=(TextView) findViewById(R.id.notify);
 			Typeface notifyFace=Typeface.createFromAsset(getAssets(),"fonts/Roboto-Condensed.ttf");
 			notifyTv.setTypeface(notifyFace);
 
 			TextView selectallTv=(TextView) findViewById(R.id.selectall);
 			selectallTv.setTypeface(notifyFace);
 		}
 	//back button
 		public void close(View v){
 			finish();
 		}
 		
 }
