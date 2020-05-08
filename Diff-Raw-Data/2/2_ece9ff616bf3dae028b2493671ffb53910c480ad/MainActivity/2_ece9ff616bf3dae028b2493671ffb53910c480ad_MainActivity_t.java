 package edu.fsu.cs.fsu_class_heat;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.SupportMapFragment;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 
 
 public class MainActivity extends FragmentActivity {
 	
 	Cursor mCursor;
 	GoogleMap map;
 	MarkerOptions mol;
 	final int loveCap = 15, hcbCap = 10;
 	
 	// Integers to keep count of the number of class
 	int HCB = 0;
 	int LOV = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
         map = ( (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapView)).getMap();
             
         map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
         LatLng loc = new LatLng(30.44388, -84.29806);
         CameraUpdate center = CameraUpdateFactory.newLatLng(loc);
         CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
         
         map.moveCamera(center);
         map.animateCamera(zoom);
         
         // ***** Database Portion *****
         mCursor = getContentResolver().query(class_database.CONTENT_URI, null, null, null, null);
         
         Log.i("marker", String.valueOf(mCursor));
 
         if(mCursor != null) {
         	
         	// if contentprovider is empty, load the dataset into content provider
         	// if contentprovider is not empty, there is no need to import the dataset
         	if(mCursor.getCount( ) <= 0) {
                 Uri mNewUri;
                 ContentValues mNewValues = new ContentValues();
 
                 // Retrieve dataset textfile in res/raw/class_dataset.txt
                 InputStream inputStream = this.getResources().openRawResource(R.raw.class_dataset);
                 InputStreamReader inputreader = new InputStreamReader(inputStream);
                 BufferedReader buffreader = new BufferedReader(inputreader);
                 
                 String line;
                 
                 // Iterate each line of the dataset file
                 // Split each line into tokens and load into the database
                 try {
                     while (( line = buffreader.readLine()) != null) {
                     	
                     	StringTokenizer line_tokens = new StringTokenizer(line, "\t");
                     	
                         mNewValues.put(class_database.COLUMN_BUILDING, line_tokens.nextToken( ));
                         mNewValues.put(class_database.COLUMN_DAYS, line_tokens.nextToken( ));
                         mNewValues.put(class_database.COLUMN_BEGIN, line_tokens.nextToken( ));
                         mNewValues.put(class_database.COLUMN_END, line_tokens.nextToken( ));
                         mNewUri = getContentResolver().insert(class_database.CONTENT_URI, mNewValues);
                       }
                  } catch (IOException e) {
              	   
                  }
                 
                 // Reinitialize database cursor
                 mCursor = getContentResolver().query(class_database.CONTENT_URI, null, null, null, null);
         	}
         }
         mCursor.close( );
 
         // ***** End of Database Portion *****
         
         
         
         LatLng love = new LatLng(30.446112,-84.299566);
 
         //count_classes(getDay(), getTime());
         count_classes('M', 1303);
         
         Log.i("marker", String.valueOf(LOV));
 
 
         float classpercent_lov;
 
         classpercent_lov =(float) LOV/loveCap; 
         if(classpercent_lov == 0)
         {
         	mol = new MarkerOptions().position(love).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).visible(false).draggable(false);
         }
 
         else if(classpercent_lov <= .5 && classpercent_lov > 0)
         {
         	mol = new MarkerOptions().position(love).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).visible(true).draggable(false);
         }
 
         else if(classpercent_lov > .5 && classpercent_lov <= .75 )
         {
         	mol = new MarkerOptions().position(love).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).visible(true).draggable(false);
         }
 
         else if(classpercent_lov > .75)
         {
         	mol = new MarkerOptions().position(love).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).visible(true).draggable(false);
         }
 
 
         map.addMarker(mol);      
 		map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	//returns integer time hhmm
 	public int getTime()
 	{
 		Time now = new Time();
 		now.setToNow();
 		String timestr = now.format("%H%M");
 		int timeint = Integer.valueOf(timestr);
 
 		//or we could Query(timeint);
 
 		return timeint;
 	}
 	
 	public char getDay()
 	{
 
 		char daychar = ' ';
 		Time day = new Time();
 
 		day.setToNow();
 
 		int dayint = day.weekDay;
 
 		if(dayint == 1)
 		{
 			daychar = 'M';
 		}
 		else if(dayint == 2)
 		{
 			daychar = 'T';
 		}
 		else if(dayint == 3)
 		{
 			daychar = 'W';
 		}
 		else if(dayint == 4)
 		{
 			daychar = 'R';
 		}
 		else if(dayint == 5)
 		{
 			daychar = 'F';
 		}
 
 		return daychar;
 	}
 	
    	// Count the number of classes going on at a given time
 	// Call example: Monday 3:00 PM will be count_classes('M', 1500);
     	public void count_classes(char CURRENT_DAY, int CURRENT_TIME) {
 
         // Main loop: iterate through contentprovider and take count of classes
     		mCursor = getContentResolver().query(class_database.CONTENT_URI, null, null, null, null);
     		mCursor.moveToFirst();	 
 
     		// Reset counts
     		HCB = 0;
     		LOV = 0;
 
     		while(mCursor.isAfterLast( ) == false) {
 
     			// Get data from database
     			String CLASS_DAYS = mCursor.getString(2).trim( );
     			int CLASS_BEGIN = Integer.valueOf(mCursor.getString(3).trim( ));
     			int CLASS_END = Integer.valueOf(mCursor.getString(4).trim( ));
 
     			if(mCursor.getString(1).equals("HCB")) {
     				if(CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
     					if(CLASS_BEGIN <= CURRENT_TIME && CURRENT_TIME <= CLASS_END) {
     						HCB++;
     					}
     				}
     			}
     			else if(mCursor.getString(1).equals("LOV")) {
     				if(CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
     					if(CLASS_BEGIN <= CURRENT_TIME && CURRENT_TIME <= CLASS_END) {
     						LOV++;
     					}
     				}
     			}
     			mCursor.moveToNext( );
     		}
     		mCursor.close( );
     	}
 
 }
