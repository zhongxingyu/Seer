 package no.uib.info331.geomusic;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import de.umass.lastfm.Event;
 
 public class EventInfoActivity extends Activity {
 
 
 	private ListView artistList;
 	private ArrayList<String> artists;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_event_info);
 
 		final GeoConcertApplication application = (GeoConcertApplication) getApplication();
 
 		//Catching intent for read the parameters
 		Intent intent = getIntent();
 
 		// Reading the id of the event from the intent
 		int id = intent.getIntExtra("id", -1);
 
 		if(id == -1) {
 			Log.d("ERROR", "negative id");
 		} else {
 			/* Finding the event from the list */
 			final Event e = application.findEvent(id);
 			
 			if(e != null)
 			{
 				/* Setting the activity field */
 				TextView eventTitle = (TextView) findViewById(R.id.eventName);
 				eventTitle.setText(e.getTitle());
 	
 				TextView eventTimeStamp = (TextView) findViewById(R.id.eventDateAndTime);
 				eventTimeStamp.setText(e.getStartDate().toString());
 	
 				TextView eventVenue = (TextView) findViewById(R.id.eventLocation);
 				eventVenue.setText(e.getVenue().getName() + " , " + e.getVenue().getStreet() + " @ " +e.getVenue().getCity());
 	
 				TextView eventDescription = (TextView) findViewById(R.id.eventDescription);
 				eventDescription.setText(Html.fromHtml(e.getDescription()).toString());
 				Log.d("EventInfoActivity eventDescription", "" + e.getDescription());
 	
 				artistList = (ListView) findViewById(R.id.artistList);
 				
 				artists = new ArrayList<String>(e.getArtists());
 				
 	
 				artistList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, artists));
 				artistList.setOnItemClickListener(new OnItemClickListener()  {
 	
 					@Override
 					public void onItemClick(AdapterView<?> arg0, View arg1,
 							int position, long id) {
 						String artist = (String) artistList.getItemAtPosition(position);
 						Intent intent = new Intent(EventInfoActivity.this, ArtistInfoActivity.class);
 						intent.putExtra("ArtistName", artist);
 						startActivity(intent);
 					}
 				});
 				
 				Button mapButton = (Button) findViewById(R.id.eventDirection);
 				mapButton.setOnClickListener(new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 	
 						/* Used the event location for the destination location */
 						
 						Double eventLatitude = (double) e.getVenue().getLatitude();
 						Double eventLongitude = (double) e.getVenue().getLongitude();
 						if(application.getLocation() == null)
 							Log.d("error", "current location null");
 						else
 						{
 							Double currentLatitude = application.getLocation().getLatitude();
 							Double currentLongitude = application.getLocation().getLongitude();
 							if((eventLatitude == null) || (eventLongitude == null) || (currentLatitude == null) || (currentLongitude == null))
 								Log.d("error", "nullpointerexception");
 							else
 							{
 								/* Used the application to get the actual location for the source location */
 								Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
 								Uri.parse("http://maps.google.com/maps?saddr="+currentLatitude+","+currentLongitude+"&daddr="+eventLatitude+","+eventLongitude));
 								startActivity(intent);
 							}
 						}
 					}
 				});
 				
 				findViewById(R.id.addToCalendarButton).setOnClickListener(new OnClickListener() {
 					
 				@Override
 				public void onClick(View v) {
 						
 						try {
 							long eventStartInMillis = e.getStartDate().getTime();
 							long eventEndInMillis = e.getStartDate().getTime() + 60*60*1000;
 							
 							Intent intent = new Intent(Intent.ACTION_EDIT);
 							intent.setType("vnd.android.cursor.item/event");
 							
 							intent.putExtra("title", e.getTitle());
 							intent.putExtra("description", e.getDescription());
 							intent.putExtra("beginTime", eventStartInMillis);
 							intent.putExtra("endTime", eventEndInMillis);
 							startActivity(intent);
 						}
 						catch(Exception e) {
 							AlertDialog alertDialog = new AlertDialog.Builder(EventInfoActivity.this).setNegativeButton("Take me back", new DialogInterface.OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									dialog.cancel();
 									
 								}
 							}).create();
 							alertDialog.setTitle("No calendar");
 							alertDialog.setMessage("No calendar found");
 							alertDialog.show();
 						}
 	
 	
 	
 	
 						
 					}
 				});
 			}
 			else
 			{
 				Log.d("error", "no event");
 				TextView eventTitle = (TextView) findViewById(R.id.eventName);
 				eventTitle.setText("no event");
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_event_info, menu);
 		return true;
 	}
 	
 	
 	/* new code for save the data */
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		Log.d("log", "onPause");
 
		GeoConcertApplication application = (GeoConcertApplication)getApplication();
		if(application.getEvents() != null)
			application.writeObject();
 	}
 
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 		Log.d("log", "onRestart");

 		GeoConcertApplication application = (GeoConcertApplication)getApplication();
 		if(application.getEvents() == null)
 			application.readObject();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		Log.d("log", "onResume");
 
 		GeoConcertApplication application = (GeoConcertApplication)getApplication();
 		if(application.getEvents() == null)
 			application.readObject();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		Log.d("log", "onStop");
 		
		GeoConcertApplication application = (GeoConcertApplication)getApplication();
		if(application.getEvents() == null)
			application.writeObject();
 	}
 	
 }
