 package com.alextoombs.citiloco;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TimePicker;
 import android.widget.TimePicker.OnTimeChangedListener;
 import android.widget.Toast;
 
 import com.alextoombs.cityplan.R;
 
 /**
  * Activity to enter parameters to guide recommended itinerary
  * 
  * @author Alex Toombs
  * @date 4/6/13
  * @version 1.0
  *
  */
 public class ParameterScreen extends Activity {
 	private static final boolean DEBUG = SplashScreen.DEBUG;
 	private static final String TAG = SplashScreen.TAG;
 	
 	private int costProgress = 0;
 	private int amHour = 0;
 	private int amMinute = 0;
 	private int pmHour = 0;
 	private int pmMinute = 0;
 	
 	private ServerSend serverGet;
 	private HttpResponse rsp;
 	
 	String cityName = null;
 	double lat = 0;
 	double lng = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.parameter_screen);
 		if(DEBUG)
 			Log.i(TAG, "Parameter Entry Screen Activity created");
 		
 		// extras from last activity
 		cityName = getIntent().getStringExtra("cityname");
 		lat = Double.parseDouble(getIntent().getStringExtra("lat"));
 		lng = Double.parseDouble(getIntent().getStringExtra("lng"));
 		
 		// objects
 		final SeekBar costSlider = (SeekBar)findViewById(R.id.costSlider);
 		final TimePicker amTimePicker = (TimePicker)findViewById(R.id.amTimePicker);
 		final TimePicker pmTimePicker = (TimePicker)findViewById(R.id.pmTimePicker);
 		final Button sendButton = (Button)findViewById(R.id.sendButton);
 		
 		// listener for costSlider to obtain total cost
 		costSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 		    @Override
 		    public void onStopTrackingTouch(SeekBar seekBar) {
 		        // TODO Auto-generated method stub
 		    	   // toast with value
 		        Toast.makeText(getApplicationContext(), "Your Selected Cost:  $" + costProgress, Toast.LENGTH_SHORT).show();
 		    }
 
 		    @Override
 		    public void onStartTrackingTouch(SeekBar seekBar) {
 		        // TODO Auto-generated method stub
 		    }
 
 		    @Override
 		    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
 		        // TODO Auto-generated method stub
 		        costProgress = progress;
 		        
 		        if(DEBUG)
 		        	Log.i(TAG, "Current progress: " + costProgress);
 		    }
 		});
 		        
 		// timepicker listener for am
 		amTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
 			public void onTimeChanged (TimePicker view, int hourOfDay, int minute) {
 				view.setCurrentHour(hourOfDay);
 				view.setCurrentMinute(minute);
 			}
 		});
 		
 		// timepicker listener for pm
 		pmTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
 			public void onTimeChanged (TimePicker view, int hourOfDay, int minute) {
 				view.setCurrentHour(hourOfDay);
 				view.setCurrentMinute(minute);
 			}
 		});
 		
 		// initialize times for TimePicker objects
 		amTimePicker.setIs24HourView(true);
 		pmTimePicker.setIs24HourView(true);
 		amTimePicker.setCurrentHour(8);
 		amTimePicker.setCurrentMinute(8);
 		pmTimePicker.setCurrentHour(17);
 		pmTimePicker.setCurrentMinute(17);
 		
 		// get times for time picker
 		amHour = amTimePicker.getCurrentHour();
 		amMinute = amTimePicker.getCurrentMinute();
 		pmHour = pmTimePicker.getCurrentHour();
 		pmMinute = pmTimePicker.getCurrentMinute();
 		
 		if(DEBUG) {
 			Log.i(TAG, "Current cost: $" + costProgress);
 			Log.i(TAG, "Current AM time: " + amHour + ":" + amMinute + "AM");
 			Log.i(TAG, "Current PM time: " + pmHour + ":" + pmMinute + "PM");
 		}
 		
 		// Button to send values to server
         sendButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view){ 
             	if(DEBUG)
             		Log.i(TAG,"Sending value to server for calculations");
             	
             	// connecting with server in separate thread
             	serverGet = new ServerSend();
             	if(DEBUG)
             		Log.i(TAG, "Trying to communicate with server now");
             	serverGet.execute((Void) null);
             	if(DEBUG) {
             		try {
             			Log.i(TAG, "Connection successful.  Response: " + rsp);
             		} catch(NullPointerException npe) {
             			Log.e(TAG, "NPE! Stuff: " + npe.toString());
             		}
             	}
             }
         });
 	}
 	
 	/**
 	 * ServerSend is a class that will run in a separate thread to prevent UI locks.
 	 * @author Alex Toombs
 	 * @date 4/6/2013
 	 * @version 1.0
 	 *
 	 */
 	private class ServerSend extends AsyncTask<Void, Void, Void> {
 		protected Void doInBackground(Void... params) {
 			HttpResponse response = null;
 			
 			// Attempt to communicate with server and send values via HTTP get; throws a whole buncha errors
 			try {        
 			    HttpClient client = new DefaultHttpClient();
 			    HttpGet request = new HttpGet();
 			    
 			    // load values into HttpGet request
 			    List<NameValuePair> getParams = new LinkedList<NameValuePair>();
 
 			    if (lat != 0.0 && lng != 0.0){
			        getParams.add(new BasicNameValuePair("LAT", String.valueOf(lat)));
			        getParams.add(new BasicNameValuePair("LON", String.valueOf(lng)));
 			    }
 			    
 			    // send cost as integer
			    getParams.add(new BasicNameValuePair("MAX_COST", String.valueOf(costProgress)));
 			    
 			    // send times as a double
 			    double amTime = amHour + amMinute/60;	
			    getParams.add(new BasicNameValuePair("START_TIME", String.valueOf(amTime)));
 			    double pmTime = amHour + pmMinute/60;
			    getParams.add(new BasicNameValuePair("END_TIME", String.valueOf(pmTime)));
 
 			    String paramString = URLEncodedUtils.format(getParams, "utf-8");
			    String url = "http://ec2-54-245-37-80.us-west-2.compute.amazonaws.com/index.php%3Fstart=%3CSTART_TIME%3D%26end=%3CEND_TIME%3D%26cost=%3CMAX_COST%3D%26lat=%3CLAT%3D%26lon=%3CLON%3D";
 			    url += paramString;
 			    
 			    // actually send the request
 			    request.setURI(new URI(url));
 			    response = client.execute(request);
 			} catch (URISyntaxException e) {
 				Log.e(TAG, "URISyntaxException: " + e.toString());
 			    e.printStackTrace();
 			} catch (ClientProtocolException e) {
 			    // TODO Auto-generated catch block
 				Log.e(TAG, "Client protocol exception: " + e.toString());
 			    e.printStackTrace();
 			} catch (IOException e) {
 			    // TODO Auto-generated catch block
 				Log.i(TAG, "IOException: " + e.toString());
 			    e.printStackTrace();
 			}   
 			rsp = response;
 			return null;
 		}
 	}
 }
