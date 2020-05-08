 package com.photohack.bythepeople;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import java.util.concurrent.ExecutionException;
 
 public class NotesActivity extends Activity implements LocationListener{
 
 	private LocationManager locationManager;
 	private String provider;
 	private IncidentData dataToSend;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent intent = getIntent();
 		dataToSend = new IncidentData();
 		final String videoUri;
 		if (!intent.hasExtra("" + LaunchActivity.VIDEO)) {
 			videoUri = null;
 			Log.e(LaunchActivity.TAG, "Something is wrong, videoUri was not " +
 					"passed to NotesActivity");
 		} else {
 			videoUri = intent.getStringExtra("" + LaunchActivity.VIDEO);
 		}
 		dataToSend.setVideoFile(videoUri);
 		// TODO: pass a Report object which contains a note and a video.
 		setContentView(R.layout.notes_activity);
 		Button doneBtn  = (Button)findViewById(R.id.done);
 		doneBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText notes_edittext = (EditText)(findViewById(R.id.notes_edittext));
 				String note = notes_edittext.getText().toString();
 				dataToSend.setNote(note);
 				submitNoteAndVideo(dataToSend);
 				startThankYouAction();
 			}});
 		Button cancelBtn  = (Button)findViewById(R.id.cancel);
 		cancelBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				finish();
 			}});
 
 		// location stuff
 		// Get the location manager
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		// Define the criteria how to select the locatioin provider -> use
 		// default
 		Criteria criteria = new Criteria();
 		provider = locationManager.getBestProvider(criteria, false);
 		Location location = locationManager.getLastKnownLocation(provider);
 
 		// Initialize the location fields
 		if (location != null) {
 			System.out.println("Provider " + provider + " has been selected.");
 			onLocationChanged(location);
 		} else {
 			Toast.makeText(getApplicationContext(), "Location not available", Toast.LENGTH_SHORT).show();
 			
 		}
 	}
 
 	public void submitNoteAndVideo(final IncidentData incidentData) {
     // In weird case, videoPath might be null.
     // TODO: submit location as well.
     String path = incidentData.getVideoFile();
     String note = incidentData.getNote();
     Log.d(LaunchActivity.TAG, "video path is " + path
           + " and note is " + note);
     if (path == null) {
       Log.e(LaunchActivity.TAG, "Video path is null, canceling the report upload process.");
     } else {
       UploadVideoTask videoUploader = new UploadVideoTask();
      videoUploader.execute(new String[] { path });
       String uploadUri = null;
       try {
         uploadUri = videoUploader.get();
       } catch (InterruptedException e) {
         e.printStackTrace();
         Log.e(LaunchActivity.TAG, "video uploading failed.");
       } catch (ExecutionException e) {
         e.printStackTrace();
         Log.e(LaunchActivity.TAG, "video uploading failed.");
       }
       if (uploadUri != null) {
         Log.d(LaunchActivity.TAG, "Video uploading succeeded.");
         incidentData.setVideoUri(uploadUri);
         // Do network operations on a new thread to avoid networkonmainthread
         // exception.
         UploadIncidentDataTask incidentUplader = new UploadIncidentDataTask();
        incidentUplader.execute(new IncidentData[] { incidentData });
         boolean success = false;
         try {
           success = incidentUplader.get();
         } catch (InterruptedException e) {
           e.printStackTrace();
           Log.e(LaunchActivity.TAG, "incident data uploading failed.");
         } catch (ExecutionException e) {
           e.printStackTrace();
           Log.e(LaunchActivity.TAG, "incident data uploading failed.");
         }
       }
     }
 	}
 
 	public void startThankYouAction(){
 		Intent doneIntent = new Intent(this,ThankYouActivity.class);
 		startActivity(doneIntent);
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		double lat =  (location.getLatitude());
 		double lng = (long) (location.getLongitude());
 		System.out.println("Lat " + lat + " has been selected.");
 		Toast.makeText(getApplicationContext(), "Latitude:" + lat + " Longitude: " + lng, Toast.LENGTH_LONG).show();
 		dataToSend.setLatitude(lat);
 		dataToSend.setLongitude(lng);
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		Toast.makeText(this, "Enabled new provider " + provider,
 				Toast.LENGTH_SHORT).show();
 	}
 
   @Override
   public void onProviderDisabled(String provider) {
     Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
   }
 }
