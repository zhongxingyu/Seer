 package no.uib.info331.geomusic.utils;
 
 import no.uib.info331.geomusic.GeoConcertApplication;
 import no.uib.info331.geomusic.R;
 import no.uib.info331.geomusic.WelcomeActivity;
 import android.app.Activity;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Log;
 import de.umass.lastfm.Event;
 import de.umass.lastfm.Geo;
 import de.umass.lastfm.PaginatedResult;
 
 public class FetchEventListAsyncTask extends
 		AsyncTask<Location, Integer, PaginatedResult<Event>> {
 
 	Activity activity;
 	
 	public FetchEventListAsyncTask(Activity activity) {
 		super();
 		this.activity = activity;
 	}
 	
 	@Override
 	protected PaginatedResult<Event> doInBackground(Location... locations) {
 		// TODO Auto-generated method stub
 		Location loc=locations[0];
 			double latitude=loc.getLatitude();
 			double longitude=loc.getLongitude();
 			
 			Log.d("FetchEventList", "Get events with location: " + loc.getLatitude() + " : " + loc.getLongitude());
 
 			PaginatedResult<Event> events = Geo.getEvents(latitude,longitude,1, activity.getString(R.string.lastfm_api_key));
 
 			Log.d("DEBUG", "" + events.isEmpty());
 			return events;
 		
 	}
 	
 	@Override
 	protected void onPostExecute(PaginatedResult<Event> result) {
 		super.onPostExecute(result);
 		GeoConcertApplication app = (GeoConcertApplication) activity.getApplication();
		app.setEvents(result);
 		if(activity instanceof WelcomeActivity) {
 			
 			((WelcomeActivity) activity).showEventListActivity();
 		}
 	}
 	
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		// TODO Auto-generated method stub
 		super.onProgressUpdate(values);
 	}
 
 }
