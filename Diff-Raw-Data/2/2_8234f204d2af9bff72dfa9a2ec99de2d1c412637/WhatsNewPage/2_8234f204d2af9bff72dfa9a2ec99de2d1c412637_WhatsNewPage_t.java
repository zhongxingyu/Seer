 package com.dedaulus.cinematty.activities.Pages;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.dedaulus.cinematty.*;
 import com.dedaulus.cinematty.activities.CinemaActivity;
 import com.dedaulus.cinematty.activities.MovieActivity;
 import com.dedaulus.cinematty.activities.adapters.CinemaItemWithScheduleAdapter;
 import com.dedaulus.cinematty.activities.adapters.PosterItemAdapter;
 import com.dedaulus.cinematty.framework.Cinema;
 import com.dedaulus.cinematty.framework.Movie;
 import com.dedaulus.cinematty.framework.MoviePoster;
 import com.dedaulus.cinematty.framework.tools.*;
 
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 04.09.11
  * Time: 20:17
  */
 public class WhatsNewPage implements SliderPage, LocationClient {
     private Context context;
     private CinemattyApplication app;
     private ApplicationSettings settings;
     private ActivitiesState activitiesState;
     private LocationState locationState;
     private PosterItemAdapter posterItemAdapter;
     private Location locationFix;
     private long timeLocationFix;
     private List<Cinema> closestCinemas;
     private boolean showSchedule = false;
     private View view;
     Timer timer;
     private Boolean binded = false;
     private boolean visible = false;
 
     {
         closestCinemas = new ArrayList<Cinema>();
     }
 
     public WhatsNewPage(Context context, CinemattyApplication app) {
         this.context = context;
         this.app = app;
 
         settings = app.getSettings();
         activitiesState = app.getActivitiesState();
         locationState = app.getLocationState();
     }
 
     public View getView() {
         LayoutInflater layoutInflater = LayoutInflater.from(context);
         view = layoutInflater.inflate(R.layout.whats_new, null, false);
 
         return bindView(view);
     }
 
     public String getTitle() {
         return context.getString(R.string.whats_new_caption);
     }
 
     public void onResume() {
         if (binded) {
             locationState.startLocationListening();
             locationState.addLocationClient(this);
             Location location = locationState.getCurrentLocation();
             if (location != null) {
                 onLocationChanged(location);
             }
             //locationFix = locationState.getCurrentLocation();
             //if (locationFix != null) {
             //    timeLocationFix = locationFix.getTime();
             //}
             posterItemAdapter.onResume();
         }
     }
 
     public void onPause() {
         locationState.removeLocationClient(this);
         locationState.stopLocationListening();
     }
 
     public void onStop() {
         posterItemAdapter.onStop();
     }
 
     @Override
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = ((SherlockActivity)context).getSupportMenuInflater();
 
         if (!closestCinemas.isEmpty()) {
             inflater.inflate(R.menu.near_menu, menu);
         }
 
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_near:
                 showSchedule = !showSchedule;
                 posterItemAdapter.setClosestCinemas(closestCinemas, showSchedule);
                 return true;
 
             default:
                 return true;
         }
     }
 
     private View bindView(View view) {
         GridView whatsNewGrid = (GridView)view.findViewById(R.id.whats_new_grid);
         posterItemAdapter = new PosterItemAdapter(context, new ArrayList<MoviePoster>(settings.getPosters()), app.getImageRetrievers().getPosterImageRetriever());
         whatsNewGrid.setAdapter(posterItemAdapter);
         whatsNewGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 onPosterItemClick(adapterView, view, i, l);
             }
         });
 
         View cinemaView = view.findViewById(R.id.closest_cinema);
         Button button = (Button)cinemaView.findViewById(R.id.button);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!closestCinemas.isEmpty()) {
                     Cinema cinema = closestCinemas.get(0);
                     onCinemaItemClick(cinema);
                 }
             }
         });
 
         binded = true;
         onResume();
 
         return view;
     }
 
     private void onCinemaItemClick(Cinema cinema) {
         String cookie = UUID.randomUUID().toString();
         ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_CINEMA, cinema, null, null, null);
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(context, CinemaActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_DESCRIPTION_PAGE_ID);
         context.startActivity(intent);
     }
 
     private void onPosterItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         PosterItemAdapter adapter = (PosterItemAdapter)adapterView.getAdapter();
         MoviePoster poster = (MoviePoster)adapter.getItem(i);
         String cookie = UUID.randomUUID().toString();
         ActivityState state = new ActivityState(ActivityState.MOVIE_INFO, null, poster.getMovie(), null, null);
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(context, MovieActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         context.startActivity(intent);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         boolean justStarted = false;
         if (locationFix == null) {
             justStarted = true;
             locationFix = location;
             timeLocationFix = location.getTime();
         }
 
         if (!justStarted) {
             if (location.getTime() - timeLocationFix < Constants.TIME_CHANGED_ENOUGH) return;
             timeLocationFix = location.getTime();
 
             if (locationFix.distanceTo(location) < Constants.LOCATION_CHANGED_ENOUGH) return;
             locationFix = location;
         }
 
         Set<Movie> movies = new HashSet<Movie>();
         for (MoviePoster poster : settings.getPosters()) {
             movies.add(poster.getMovie());
         }
 
         closestCinemas.clear();
         for (Cinema cinema : settings.getCinemas().values()) {
             Coordinate coordinate = cinema.getCoordinate();
             if (coordinate == null) continue;
 
             float[] distance = new float[1];
             Location.distanceBetween(coordinate.latitude, coordinate.longitude, location.getLatitude(), location.getLongitude(), distance);
             if (distance[0] < Constants.CLOSEST_CINEMA_DISTANCE) {
                 Collection<Pair<Movie, List<Calendar>>> showtimes = cinema.getShowTimes(Constants.TODAY_SCHEDULE).values();
                 for (Pair<Movie, List<Calendar>> showtime : showtimes) {
                     if (movies.contains(showtime.first)) {
                         closestCinemas.add(cinema);
                         break;
                     }
                 }
             }
         }
 
         Collections.sort(closestCinemas, new CinemaComparator(CinemaSortOrder.BY_DISTANCE, location));
         View cinemaView = view.findViewById(R.id.closest_cinema);
 
         if (timer != null) {
             timer.cancel();
             timer = null;
         }
 
         if (closestCinemas.isEmpty()) {
             cinemaView.setVisibility(View.GONE);
         } else {
             Button button = (Button)cinemaView.findViewById(R.id.button);
             button.setText(closestCinemas.get(0).getName());
             final ImageView imageView = (ImageView)cinemaView.findViewById(R.id.icon);
             timer = new Timer(true);
             timer.scheduleAtFixedRate(new TimerTask() {
                 boolean state;
                 @Override
                 public void run() {
                     ((Activity)context).runOnUiThread(new Runnable() {
                         public void run() {
                             imageView.setImageResource(state ? R.drawable.ic_near : R.drawable.ic_near2);
                         }
                     });
                     state = !state;
                 }
             }, 750, 750);
             cinemaView.setVisibility(View.VISIBLE);
         }
 
        SherlockActivity parent = (SherlockActivity)context;
         parent.invalidateOptionsMenu();
     }
 }
