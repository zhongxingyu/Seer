 package at.ac.tuwien.tracker.android.serverConnection;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.web.client.RestTemplate;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import at.ac.tuwien.tracker.android.GpsLoggingService;
 import at.ac.tuwien.tracker.android.IGpsLoggerServiceClient;
 import at.ac.tuwien.tracker.android.R;
 import at.ac.tuwien.tracker.android.common.AppSettings;
 import at.ac.tuwien.tracker.android.common.Constants;
 import at.ac.tuwien.tracker.android.common.Session;
 import at.ac.tuwien.tracker.android.common.Utilities;
 import at.ac.tuwien.tracker.android.serverConnection.adapters.RacingUserAdapter;
 import at.ac.tuwien.tracker.android.serverConnection.dtos.RaceStatisticsDTO;
 import at.ac.tuwien.tracker.android.serverConnection.dtos.RaceStatisticsListDTO;
 import at.ac.tuwien.tracker.android.serverConnection.dtos.UserDTO;
 
 public class RaceMainActivity extends Activity implements IGpsLoggerServiceClient {
 	
 	final static int IDNOTSET = -1;
 	private Intent serviceIntent; 
 	private GpsLoggingService loggingService;
 	
 	
 	private ArrayList<UserDTO> selectedUsers;
 	private int raceId;
 	private boolean isAlone;
 	private List<Integer> colors;
 	private RaceState raceState;
 	
 	//drawings
 	private ImageView img;
 	private Bitmap bmp; 
 	private Canvas canvas;
 	private Paint strokePaint;
 	private Paint filledPaint;
 	
 	private final ServiceConnection gpsServiceConnection = new ServiceConnection()
 	{
 
 		public void onServiceDisconnected(ComponentName name)
 		{
 			loggingService = null;
 		}
 
 		public void onServiceConnected(ComponentName name, IBinder service)
 		{
 			loggingService = ((GpsLoggingService.GpsLoggingBinder) service).getService();
 			GpsLoggingService.SetRaceClient(RaceMainActivity.this);
 			if(!Session.isStarted()){
 				loggingService.StartLogging();
 				Session.setStarted(true);
 			}
 		}
 	};
 	
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_race_main);
 //        getActionBar().setDisplayHomeAsUpEnabled(true);
         
         //default settings
         Utilities.PopulateAppSettings(getApplicationContext());
         //get Extras
         
         Intent intent = getIntent();
         isAlone = intent.getBooleanExtra("alone",false);
         raceId = intent.getIntExtra("raceid", IDNOTSET);
         selectedUsers = intent.getParcelableArrayListExtra("users");
         
         //init state
         if(isAlone){
         	raceState = new AloneRace();
         }else{
         	raceState = new WithFriendsRace();
         }
         
         //set preferences:
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         Editor editor = prefs.edit();
         editor.putString("time_before_logging", "15");
         
         editor.commit();
         
         AppSettings.setMinimumSeconds(15);
         
         //start service if not started and bind
         StartAndBindService();
         
         
         //define the order of colors		
         colors = new ArrayList<Integer>();
         colors.add(Color.CYAN);
         colors.add(Color.BLUE);
         colors.add(Color.GREEN);
         colors.add(Color.MAGENTA);
         colors.add(Color.RED);
         
         
      
         
         
         
         initDrawings();
         drawCircle();
 		
     }
 
     private void initDrawings() {
 		img = (ImageView) findViewById(R.id.imageView1);
 		bmp = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
 		canvas = new Canvas(bmp);
    
 		strokePaint = new Paint();
 		strokePaint.setColor(Color.BLACK);
 		strokePaint.setStrokeWidth(5);
 		strokePaint.setStyle(Style.STROKE);	
 		
 		filledPaint = new Paint();
 //		filledPaint.setColor(Color.GREEN);
 		filledPaint.setStyle(Style.FILL);
 	}
 
 	private void drawCircle() {
 		
 		canvas.drawCircle(150, 150, 140, strokePaint);
 
 		
 	}
 	
 	private void refreshPaint(){
 		img.setBackgroundColor(Color.WHITE);
 		img.setBackgroundDrawable(new BitmapDrawable(bmp));
 	}
 
 	public void showReceivedUpdate(RaceStatisticsListDTO result) {
 		System.out.println(""+result.getRaceName());
 		
 		ListView listView = (ListView) findViewById(R.id.racinguserlist);
         List<String> values = new ArrayList<String>();
       
 		int i = 0;
 		for(RaceStatisticsDTO dto : result.getStats()){
 			System.out.println("Username: "+dto.getUsername()+"Distance: "+dto.getDistance()+"AVG: "+dto.getAvgSpeed());
			int iLap = (int) (dto.getDistance()/10);
 			values.add(""+dto.getUsername()+" "+iLap+". "+"Lap"+" AvgSpeed: "+ dto.getAvgSpeed());
 			
 			//coordinates: 150+ sin (x)* r; 150-cos(x)*r 
 			int r = 140;
			double angle = (dto.getDistance()-iLap)*36;
 			float cx = (float) (150+ (Math.sin(angle)*r));
 			float cy = (float) (150- (Math.cos(angle)*r));
 			filledPaint.setColor(colors.get(i));
 			canvas.drawCircle(cx, cy, 10, filledPaint);
 			if(i<5)	i++; else i=0;
 		}  
 		
 		refreshPaint();
 		RacingUserAdapter adapter = new RacingUserAdapter(this, values, colors);
         listView.setAdapter(adapter); 
 	}
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_race_main, menu);
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
     
     
     private void StartAndBindService()
     {
         Utilities.LogDebug("StartAndBindService FROM RACEACTIVITY - binding now");
         serviceIntent = new Intent(this, GpsLoggingService.class);
         // Start the service in case it isn't already running
         startService(serviceIntent);
         // Now bind to service
         bindService(serviceIntent, gpsServiceConnection, Context.BIND_AUTO_CREATE);
         Session.setRaceBoundToService(true);
     }
     
 	public void OnLocationUpdate(Location loc) {
 		System.out.println("LOCATION UPDATE IN RACEACTIVITYYYYY!!!!!");
 		Utilities.LogDebug("LOCATION UPDATE IN RACEACTIVITYYYYY!!!!!");
 		//send location to webserver
 		raceState.sendUpdate(raceId,loc.getLongitude(),loc.getLatitude(),loc.getAltitude());
 		//retrieve update from webserver
 		raceState.receiveUpdate();
 	}
 	public void OnStopLogging() {
 		loggingService.StopLogging();
 	}
 	
 	
 	public Activity GetActivity() {
 		return this;
 	}
 	
 	public void OnSatelliteCount(int count) {
 		// TODO Auto-generated method stub
 	}
 
 	public void ClearForm() {
 		// TODO Auto-generated method stub
 	}
 
 	public void onFileName(String newFileName) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void OnStatusMessage(String message) {
 		// TODO Auto-generated method stub
 	}
 
 	public void OnFatalMessage(String message) {
 		// TODO Auto-generated method stub
 	}
 
 	//-----------------------------------------------Private Classes -------------------------------------------------------//
 	private abstract class RaceState{
 		
 		void sendUpdate(Integer raceId, Double longitude, Double latitude, Double altitude){
 			if(raceId != RaceMainActivity.IDNOTSET){
 				new SendUpdateTask(raceId,longitude,latitude,altitude).execute();
 			}
 		}
 		abstract void receiveUpdate();
 	}
 	
 	private class AloneRace extends RaceState{
 
 
 		
 		
 		public AloneRace() {
 			//TODO give name
 			new InitRaceTask(null, "testalonerace"+new Date().toString()).execute();
 		}
 
 		@Override
 		void receiveUpdate() {
 			// Do nothing
 			
 		}
 		
 	}
 	
 	private class WithFriendsRace extends RaceState{
 
 
 		@Override
 		void receiveUpdate() {
 			new ReceiveUpdateTask(raceId).execute();
 			
 		}
 		
 	}
 	
 	private class SendUpdateTask extends AsyncTask<Void, Void, String> 
 	{	
 		private Integer raceId;
 		private Double longitude;
 		private Double latitude;
 		private Double altitude;
 		
 		public SendUpdateTask(Integer raceId, Double longitude, Double latitude, Double altitude) {
 			this.raceId = raceId;
 			this.longitude = longitude;
 			this.latitude = latitude;
 			this.altitude = altitude;
 		}
 
 		@Override
 		protected void onPreExecute() 
 		{
 	
 		}
 		
 		@Override
 		protected String doInBackground(Void... params) 
 		{
 			try 
 			{
 				
 				
 				final String url = getString(R.string.base_uri) + Constants.addracelocation;
 
 				// Set the Accept header for "application/json" or "application/xml"
 				HttpHeaders requestHeaders = new HttpHeaders();
 				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
 				acceptableMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
 				requestHeaders.setAccept(acceptableMediaTypes);
 
 				String username = AppSettings.getServer_username();
 				String password = AppSettings.getServer_password();
 
 				MultiValueMap<String, String> requestData = new LinkedMultiValueMap<String, String>();
 				requestData.add("username", username);
 				requestData.add("password", password);
 				requestData.add("raceid", ""+raceId);
 				requestData.add("longitude",""+longitude);
 				requestData.add("latitude",""+latitude);
 				requestData.add("altitude",""+altitude);
 				// Populate the headers in an HttpEntity object to use for the request
 				HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestData, requestHeaders);
 														
 				// Create a new RestTemplate instance
 				RestTemplate restTemplate = new RestTemplate();
 
 				// Perform the HTTP GET request
 				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
 								
 				return  responseEntity.getBody();
 				
 	
 			} 
 			catch(Exception e) 
 			{
 				Utilities.LogError("sending location infos", e);
 			} 
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) 
 		{
 			if(result.equals("success")) System.out.println("OKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
 
 		}
 	}
 	
 	private class ReceiveUpdateTask extends AsyncTask<Void, Void, RaceStatisticsListDTO> 
 	{	
 		
 		Integer raceId;
 		public ReceiveUpdateTask(Integer raceId) {
 			this.raceId = raceId;
 		}
 
 		@Override
 		protected void onPreExecute() 
 		{
 			// before the network request begins, show a progress indicator
 //			showLoadingProgressDialog();
 		}
 		
 		@Override
 		protected RaceStatisticsListDTO doInBackground(Void... params) 
 		{
 			try 
 			{
 				
 				
 				final String url = getString(R.string.base_uri) + Constants.updateracestatus;
 
 				// Set the Accept header for "application/json" or "application/xml"
 				HttpHeaders requestHeaders = new HttpHeaders();
 				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
 				acceptableMediaTypes.add(MediaType.APPLICATION_XML);
 				requestHeaders.setAccept(acceptableMediaTypes);
 
 				String username = AppSettings.getServer_username();
 				String password = AppSettings.getServer_password();
 
 				MultiValueMap<String, String> requestData = new LinkedMultiValueMap<String, String>();
 				requestData.add("username", username);
 				requestData.add("password", password);
 				requestData.add("raceid", ""+raceId);
 
 				// Populate the headers in an HttpEntity object to use for the request
 				HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestData, requestHeaders);
 														
 				// Create a new RestTemplate instance
 				RestTemplate restTemplate = new RestTemplate();
 
 				// Perform the HTTP GET request
 				ResponseEntity<RaceStatisticsListDTO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, RaceStatisticsListDTO.class);
 								
 				// Return the state from the ResponseEntity
 				RaceStatisticsListDTO liste = responseEntity.getBody();
 				return liste;
 				
 	
 			} 
 			catch(Exception e) 
 			{
 				Utilities.LogError("receiveUpdate", e);
 			} 
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(RaceStatisticsListDTO result) 
 		{
 			// hide the progress indicator when the network request is complete
 //			dismissProgressDialog();
 			
 			// return the list of states
 //			refreshStates(result);
 			showReceivedUpdate(result);
 			
 		}
 	}
 	
 	private class InitRaceTask extends AsyncTask<Void, Void, String> 
 	{	
 		ArrayList<UserDTO> selectedUsers;
 		String racename;
 		
 		public InitRaceTask(ArrayList<UserDTO> selectedUsers, String string) {
 			this.selectedUsers = selectedUsers;
 			this.racename = string;
 		}
 
 		@Override
 		protected void onPreExecute() 
 		{
 			// before the network request begins, show a progress indicator
 //			showLoadingProgressDialog();
 		}
 		
 		@Override
 		protected String doInBackground(Void... params) 
 		{
 			try 
 			{
 				
 				final String url = getString(R.string.base_uri) + Constants.initnewrace;
 
 				// Set the Accept header for "application/json" or "application/xml"
 				HttpHeaders requestHeaders = new HttpHeaders();
 				List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
 				acceptableMediaTypes.add(MediaType.MULTIPART_FORM_DATA);
 				requestHeaders.setAccept(acceptableMediaTypes);
 
 				String username = AppSettings.getServer_username();
 				String password = AppSettings.getServer_password();
 
 				MultiValueMap<String, String> requestData = new LinkedMultiValueMap<String, String>();
 				requestData.add("username", username);
 				requestData.add("password", password);
 				requestData.add("racename", racename);
 			
 				requestData.add("useridstring", "");
 
 				// Populate the headers in an HttpEntity object to use for the request
 				HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(requestData, requestHeaders);
 														
 				// Create a new RestTemplate instance
 				RestTemplate restTemplate = new RestTemplate();
 
 				// Perform the HTTP GET request
 				ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
 								
 				// Return the state from the ResponseEntity
 				return responseEntity.getBody();
 				
 	
 			} 
 			catch(Exception e) 
 			{
 //				Log.e(TAG, e.getMessage(), e);
 			} 
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) 
 		{
 //			dismissProgressDialog();
 			
 			//start race Intent+Service
 			
 //			showRace(result,selectedUsers);
 			RaceMainActivity.this.raceId = Integer.parseInt(result);
 		}
 
 		
 	}
 
 
 	
 }
