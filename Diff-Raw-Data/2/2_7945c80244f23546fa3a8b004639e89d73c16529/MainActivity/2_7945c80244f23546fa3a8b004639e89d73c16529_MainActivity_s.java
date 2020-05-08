 package no.hiof.stud.localstories;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import no.hiof.stud.localstories.RangeSeekBar.OnRangeSeekBarChangeListener;
 
 import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
 import org.osmdroid.util.GeoPoint;
 import org.osmdroid.views.MapController;
 import org.osmdroid.views.MapView;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ZoomControls;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	public final static String EXTRA_MESSAGE = "no.hiof.stud.localstories.MESSAGE";
 	
 	private MapView         mMapView;
     private MapController   mMapController;
 
     ZoomControls zoom;
 
     //Search
     private int yearFrom;
     private int yearTo;
     private Search search = new Search();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		//Load events
 		Load.load();
         
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
                 		
                 		//TEST on update yearSeek
                 		TextView fromYear = (TextView) findViewById(R.id.fromYearValue);
                 		TextView toYear = (TextView) findViewById(R.id.toYearValue);
                 		
                 		// FIX for displaying correct era
                 		// Log.i("LocalStories", "Min is now" + minValue);
                 		String baFrom, baTo;
                 		Long yearZero = -62146336523773L;
                 		
                 		if (minValue < yearZero)
                 		{
                 			baFrom = " BCE";
                 			
                			if (yearTo < yearZero)
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
                         Log.i("LocalStories", "User selected new date range: MIN=" + new Date(minValue) + ", MAX=" + new Date(maxValue));
                 }
         });
         // add RangeSeekBar to pre-defined layout
         ViewGroup layout = (ViewGroup) findViewById(R.id.range);
         layout.addView(seekBar);
         
         // Handle Radius-seekbar
         SeekBar radiusSB = (SeekBar) findViewById(R.id.RadiusSeekBar);
         
         radiusSB.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
 
 			@Override
 			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 				// TODO Auto-generated method stub
 				((TextView) findViewById(R.id.radiusValue)).setText(arg1+"km");
 				Log.i("LocalStories", "User selected new radiuse: " + arg1);
 				
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
         
         // osm-code here
      	mMapView = (MapView) findViewById(R.id.mapview);
         mMapView.setTileSource(TileSourceFactory.MAPNIK);
         
         mMapView.setClickable(true);
         mMapView.setMultiTouchControls(true);
         mMapView.setBuiltInZoomControls(true);
         
         mMapController = mMapView.getController();
         mMapController.setZoom(13);
         
         zoom = (ZoomControls) findViewById(R.id.map_zoom_controls);
         zoom.setOnZoomInClickListener(new OnClickListener() {
 			
     		@Override
     		public void onClick(View v) {
     			// TODO Auto-generated method stub
     			
     			mMapController.setZoom(mMapView.getZoomLevel()+1);
     		}
     	});
      
             zoom.setOnZoomOutClickListener(new View.OnClickListener() {
     			
     		@Override
     		public void onClick(View v) {
     			// TODO Auto-generated method stub
     			
     			mMapController.setZoom(mMapView.getZoomLevel()-1);
     		}
     	});
         
         float lat   = 59.123389f;   //in DecimalDegrees
         float lng   = 11.446778f;   //in DecimalDegrees
         
         // HIOF: GeoPoint gPt = new GeoPoint(59128879,11353987);
         GeoPoint gPt = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
         //Centre map near to Halden
         mMapController.setCenter(gPt);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/** Called when the user clicks the Send button */
 	public void sendMessage(View view) {
 	    // Do something in response to button
 		/*Intent intent = new Intent(this, DisplayMessageActivity.class);
 	    EditText editText = (EditText) findViewById(R.id.edit_message);
 	    String message = editText.getText().toString();
 	    intent.putExtra(EXTRA_MESSAGE, message);
 	    startActivity(intent);*/
 	    //SEARCH
 		search.resetSearch();
 	    Log.i("LocalStories", "From "+ yearFrom);
 	    Log.i("LocalStories", "To " + yearTo);
 	    search.setYear(yearFrom, yearTo);
 	    EditText freeText = (EditText) findViewById(R.id.edit_message);
 	    String txt = freeText.getText().toString();
 	    search.setText(txt);
 	    //TODO ADD LOCATION
 	    //search.setLocation(x, y, dist);
 	    search.start();
 	    //TODO Switch to resultpage
 	    //      Display: search.getList();
 	    Log.i("LocalStories", search.getList().size()+" results");
         
 	}
 
 }
