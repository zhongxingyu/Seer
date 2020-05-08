 package no.hiof.stud.localstories;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import no.hiof.stud.localstories.RangeSeekBar.OnRangeSeekBarChangeListener;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 	public final static String EVENT_ID = "1";
 	public final static String EXTRA_MESSAGE = "no.hiof.stud.localstories.MESSAGE";
 
     //Search
     private int yearFrom=-1950;
     private int yearTo=2013;
     private float distance=5;
     private Search search = new Search();
     
     // Centering if user comes from event-view
     public float cLat;
     public float cLng;
     
     //GPS Location
     public static float lat   = 59.128221f;   //in DecimalDegrees
     public static float lng   = 11.385012f;   //in DecimalDegrees
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		
 		setContentView(R.layout.activity_main);
 
 		// Restrict application to portrait view only
 		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		
 		//Load events
 		Load.load();
 
 	    Log.i("LocalStories", "Start");
         
 		// create RangeSeekBar as Date range between 1950 BCE and now
         Date minDate = null;
 		try {
 			minDate = new SimpleDateFormat("yyyy").parse("-1950");
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         Date maxDate = new Date();
         RangeSeekBar<Long> seekBar = new RangeSeekBar<Long>(minDate.getTime(), maxDate.getTime(), this);
        
         // Initiate minValue as year zero
         Long yearZero = -62146336523773L;
         seekBar.setSelectedMinValue(yearZero);
         TextView fromYear = (TextView) findViewById(R.id.fromYearValue);
         fromYear.setText("0 CE");
         
         
         seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Long>() {
         	@Override
             public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                 // handle changed range values
         	    Calendar myCal;
         	    myCal= new GregorianCalendar();
         	    myCal.setTime(new Date(minValue));
             	yearFrom = myCal.get(Calendar.YEAR) + myCal.get(Calendar.ERA);
          	    myCal= new GregorianCalendar();
         	    myCal.setTime(new Date(maxValue));
         		yearTo = myCal.get(Calendar.YEAR) + myCal.get(Calendar.ERA);
         		
         		// Fix for one-off errors in UI
         		yearFrom--; yearTo--;
         		
         		//TEST on update yearSeek
         		TextView fromYear = (TextView) findViewById(R.id.fromYearValue);
         		TextView toYear = (TextView) findViewById(R.id.toYearValue);
         		
         		
         		// FIX for displaying correct era
         		Long yearZero = -62146336523773L;
         		String baFrom, baTo;
         		
         		if (minValue < yearZero)
         		{
         			baFrom = " BCE";
         			
         			if (maxValue < yearZero)
         			{
         				baTo = " BCE";
         			}
         			else
         			{
         				baTo = " CE";
         			}
         		}
         		else
         		{
         			baFrom = " CE";
         			baTo = " CE";
         		}
         		
         		fromYear.setText(yearFrom+baFrom);
         		toYear.setText(yearTo+baTo);
         		//END TEST
         		search();
                 Log.i("LocalStories", "User selected new date range: MIN=" + new Date(minValue) + ", MAX=" + new Date(maxValue));
                 }
         });
         // add RangeSeekBar to pre-defined layout
         ViewGroup layout = (ViewGroup) findViewById(R.id.range);
         layout.addView(seekBar);
         
         // Handle Radius-seekbar
         SeekBar radiusSB = (SeekBar) findViewById(R.id.RadiusSeekBar);
        radiusSB.setMax(500);
         
         radiusSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 				// TODO Auto-generated method stub
 				((TextView) findViewById(R.id.radiusValue)).setText(arg1+"km");
 				distance = arg1;
 				search();
 				Log.i("LocalStories", "User selected new radius: " + arg1);
 				
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onStopTrackingTouch(SeekBar arg0) {
 				// TODO Auto-generated method stub
 				
 			}
         });
         
         search();
         
         // Create new fragment and transaction => osmdroid
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         MapFragment fragment = new MapFragment();
         
         // Adding
         fragmentTransaction.add(R.id.fragment_container, fragment, "map_Fragment");
         //fragmentTransaction.attach(fragment);
         //fragmentTransaction.show(fragment);
         
         // Commiting
         fragmentTransaction.commit();
         
         // Execute pending operations (commit) IMMEDEATELY
         fragmentManager.executePendingTransactions();
        mapView(this.findViewById(android.R.id.content));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public void search()
 	{	
 		//search.resetSearch();
 	    Log.i("LocalStories", "From "+ yearFrom);
 	    Log.i("LocalStories", "To " + yearTo);
 	    search.setYear(yearFrom, yearTo);
 	    EditText freeText = (EditText) findViewById(R.id.edit_message);
 	    String txt = freeText.getText().toString();
 	    search.setText(txt);
 	    search.setLocation(lat, lng, distance/111); //Dividing by 111 to transform between km and lat/long
 	    search.start();
 	    updateFragment();
 	}
 	
 	public void updateFragment()
 	{
 		// Find current fragment        
         Fragment frag = getSupportFragmentManager().findFragmentByTag("map_Fragment");
         try
         {
         	if (frag.isVisible())
             {
             	mapView(this.findViewById(android.R.id.content));
             }
             
             frag = getSupportFragmentManager().findFragmentByTag("list_Fragment");
             if (frag.isVisible())
             {
             	listView(this.findViewById(android.R.id.content));
             }
         }
     	catch (NullPointerException e)
     	{
     		Log.i("LocalStories", "First run of method updateFragment(), via method search() - no need to update.");
     	}  
 	}
 	
 	/** Called when the user clicks the Search button */
 	public void sendMessage(View view) {
 		
 	    // Do something in response to search- button
 		/*Intent intent = new Intent(this, DisplayMessageActivity.class);
 	    EditText editText = (EditText) findViewById(R.id.edit_message);
 	    String message = editText.getText().toString();
 	    intent.putExtra(EXTRA_MESSAGE, message);
 	    startActivity(intent);*/
 	    //SEARCH
 		search();
 	    //TODO 
 	    //      Display: search.getList();
 	    Log.i("LocalStories", Search.getList().size()+" results");
 	    //Intent intent = new Intent(this, ResultPageActivity.class);
 	    //startActivity(intent);
         
 	}
 	
 	/** Called when the user clicks the "Map View" button */
 	public void mapView(View view) {
 		// Create new fragment and transaction => osmdroid
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         MapFragment fragment = new MapFragment();
 
         // Replacing
         fragmentTransaction.replace(R.id.fragment_container, fragment, "map_Fragment");
         fragmentTransaction.addToBackStack(null);
         
         // Commiting
         fragmentTransaction.commit();
         
         // Execute pending operations (commit) IMMEDEATELY
         fragmentManager.executePendingTransactions();
 	}
 	
 	/** Called when the user clicks the "List View" button */
 	public void listView(View view) {
 		// Create new fragment and transaction => osmdroid
         FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         ListFragment fragment = new ListFragment();
 
         // Replacing
         fragmentTransaction.replace(R.id.fragment_container, fragment, "list_Fragment");
         fragmentTransaction.addToBackStack(null);
         
         // Commiting
         fragmentTransaction.commit();
         
         // Execute pending operations (commit) IMMEDEATELY
         fragmentManager.executePendingTransactions();
 	}
 	
 	/** Called when the user clicks the "Update Location" button */
 	public void updateLocation(View view) {
 		// TODO: Manually update current location
         Log.i("LocalStories", "Manually update current location here...");
 	}
 	
 	/** Called when the user clicks the "Show Location" button */
 	public void showLocation(View view) {
		try {
	    	getIntent().removeExtra(EventActivity.EVENT_ID);
		} catch (Exception e) {
			// TODO: handle exception
		}
 		mapView(this.findViewById(android.R.id.content));
 	}
 
 }
