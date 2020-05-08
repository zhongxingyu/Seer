 package ie.ucd.asteroid;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class WeeklyAsteroids extends Activity {
 
 	DBAdapter db = new DBAdapter(this);
 	//array to store the asteroid list for display
 	ArrayList<String> asteroidList = new ArrayList<String>(); 
 
 	private static final String WEEKLYASTEROIDS_TAG = "Weekly_Asteroids";
 	private ListView listView1;
 	//private CheckBox bellSwitch;
 	//String msg = "";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_weekly_asteroids);
 		
 		//Code for getting data from database: 
 		db.openDB();
 		Cursor c = db.getAllAsteroids();
 		if (c.moveToFirst()) {
 			do {
 				// get info from database
 				String objectName = db.getName(c);
 				String approachDate = db.getApproachDate(c);
 				Double maxDiameter = db.getMaxDiameter(c);
 				Double minDiameter = db.getMinDiameter(c);
 				
 				Log.w(WEEKLYASTEROIDS_TAG, "Name, Date, Diams: " + objectName + " " 
 				+ approachDate + " " + maxDiameter + " "+ minDiameter);
 				
 				//get approximate diameter calculated from min & max diameters
 				Double approxDiameter = (minDiameter + maxDiameter) / 2;
 				Log.w(WEEKLYASTEROIDS_TAG, "Approx diameter calculated: " + approxDiameter);
 				
 				asteroidList.add(objectName + "    "  + approachDate);
 
 			} while (c.moveToNext());
 		}
		c.close(); // Close cursor
 		db.closeDB();
 		
 		//put database data into the listview: 
 		Asteroids[] asteroid_data = new Asteroids[asteroidList.size()];
 		for(int i = 0; i<asteroidList.size(); i++){
 		asteroid_data[i] = new Asteroids(R.drawable.mini_icon_asteroid_large, asteroidList.get(i), true);	
 		}
 
 		LvAsteroidAdapter lvAdapter = new LvAsteroidAdapter(this, R.layout.listview_item_row, asteroid_data);
 
 		listView1 = (ListView) findViewById(R.id.listView1);
 
 		View header = (View) getLayoutInflater().inflate(R.layout.listview_header_row, null);
 
 		listView1.addHeaderView(header); 
 		listView1.setAdapter(lvAdapter);
 
 		setFonts();
 		
 		//intent to open asteroid Info activity, done this way as not extending listactivity. 
 		final Intent intent_ViewAsteroidInfo = new Intent(this, AsteroidInfo.class);
 		listView1.setOnItemClickListener(new OnItemClickListener(){
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				String test="test";
 		    	//bundle listview details
 		    	Bundle extras = new Bundle(); 
 		    	extras.putString("test", test);
 		    	intent_ViewAsteroidInfo.putExtras(extras);
 		    	startActivity(intent_ViewAsteroidInfo);
 			}
 			});
 		
 		//a work in progress...
 		//bellSwitch = (CheckBox) listView1.getChildAt(0).findViewById(R.id.imgNotify);
 		//Log.d(WEEKLYASTEROIDS_TAG, "Attempting  to find bellSwitch");
 
 		
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
 			Typeface selectallFace=Typeface.createFromAsset(getAssets(),"fonts/Roboto-Condensed.ttf");
 			selectallTv.setTypeface(selectallFace);
 
 		}
 	}
