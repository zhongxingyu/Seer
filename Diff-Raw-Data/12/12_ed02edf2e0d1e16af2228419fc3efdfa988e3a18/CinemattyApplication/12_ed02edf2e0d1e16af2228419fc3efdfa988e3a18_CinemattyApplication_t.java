 package com.dedaulus.cinematty;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Application;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.DisplayMetrics;
 import com.dedaulus.cinematty.activities.MainActivity;
 import com.dedaulus.cinematty.activities.PreferencesActivity;
 import com.dedaulus.cinematty.activities.StartupActivity;
 import com.dedaulus.cinematty.framework.*;
 import com.dedaulus.cinematty.framework.tools.*;
 import org.json.JSONException;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 /*
  * User: Dedaulus
  * Date: 13.03.11
  * Time: 21:41
  */
 public class CinemattyApplication extends Application {
     public static int NEW_INSTALLATION = 0;
     public static int NEW_VERSION = 1;
     public static int OLD_VERSION = 2;
     
     private static final String SCHEDULES_FOLDER_KEY = "schedules_folder";
     private static final String PICTURES_FOLDER_KEY  = "pictures_folder";
     private static final String FRAMES_FOLDER_KEY    = "frames_folder";
     private static final String POSTERS_FOLDER_KEY   = "posters_folder";
     private static final String SHARED_PAGE_KEY      = "shared_page";
     private static final String CONNECT_DUMP_FILE    = "connect_dump.xml";
 
     private static final String VERSION_FILE    = "cinematty_preferences";
     private static final String CURRENT_VERSION = "current_version";
     private static final String LAUNCH_NUMBER   = "launch_number";
 
     private class ApplicationSettingsImpl implements ApplicationSettings {
         private static final String FAV_CINEMAS_FILE                  = "cinematty_fav_cinemas";
         private static final String FAV_ACTORS_FILE                   = "cinematty_fav_actors";
         private static final String PREFERENCES_FILE                  = "cinematty_preferences";
         private static final String PREF_CINEMA_SORT_ORDER            = "cinema_sort_order";
         private static final String PREF_MOVIE_SORT_ORDER             = "movie_sort_order";
         private static final String PREF_MOVIE_W_SCHEDULE_SORT_ORDER  = "movie_w_schedule_sort_order";
         private static final String PREF_CURRENT_CITY                 = "current_city";
 
         private Map<String, Cinema> cinemas;
         private Map<String, Movie> movies;
         private Map<String, MovieActor> actors;
         private Map<String, MovieGenre> genres;
         private List<MoviePoster> posters;
 
         private Map<String, Cinema> favoriteCinemas;
         private Map<String, MovieActor> favoriteActors;
 
         private int currentDay = Constants.TODAY_SCHEDULE;
 
         public ApplicationSettingsImpl(
                 Map<String, Cinema> cinemas, 
                 Map<String, Movie> movies, 
                 Map<String, MovieActor> actors, 
                 Map<String, MovieGenre> genres, 
                 List<MoviePoster> posters) {
             this.cinemas = new TreeMap<String, Cinema>(cinemas);
             this.movies = new TreeMap<String, Movie>(movies);
             this.actors = new TreeMap<String, MovieActor>(actors);
             this.genres = new TreeMap<String, MovieGenre>(genres);
             this.posters = new ArrayList<MoviePoster>(posters);
             
             favoriteCinemas = new HashMap<String, Cinema>();
             SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + currentCity.getId(), MODE_PRIVATE);
             if (preferences != null) {
                 Map<String, ?> favs = preferences.getAll(); 
                 for (String name : favs.keySet()) {
                     Cinema cinema = cinemas.get(name);
                     if (cinema != null) {
                         cinema.setFavourite((Long)favs.get(name));
                         favoriteCinemas.put(name, cinema);
                     }
                 }
             }
 
             favoriteActors = new HashMap<String, MovieActor>();
             preferences = getSharedPreferences(FAV_ACTORS_FILE, MODE_PRIVATE);
             if (preferences != null) {
                 Map<String, ?> favs = preferences.getAll();
                 for (String name : favs.keySet()) {
                     MovieActor actor = actors.get(name);
                     if (actor != null) {
                         actor.setFavourite((Long)favs.get(name));
                         favoriteActors.put(name, actor);
                     }
                 }
             }
         }
 
         @Override
         public void setCurrentDay(int day) {
             currentDay = day;
         }
 
         @Override
         public int getCurrentDay() {
             return currentDay;
         }
 
         @Override
         public Map<String, Cinema> getCinemas() {
             return cinemas;
         }
 
         @Override
         public Map<String, Movie> getMovies() {
             return movies;
         }
 
         @Override
         public Map<String, MovieActor> getActors() {
             return actors;
         }
 
         @Override
         public Map<String, MovieGenre> getGenres() {
             return genres;
         }
 
         @Override
         public List<MoviePoster> getPosters() {
             return posters;
         }
 
         @Override
         public void saveFavouriteActors() {
             SharedPreferences preferences = getSharedPreferences(FAV_ACTORS_FILE, MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             for (MovieActor actor : actors.values()) {
                 if (actor.getFavourite() > 0) {
                     editor.putLong(actor.getName(), actor.getFavourite());
                 } else {
                     editor.remove(actor.getName());
                 }
             }
             editor.commit();
         }
 
         @Override
         public Map<String, MovieActor> getFavouriteActors() {
             return favoriteActors;
         }
 
         @Override
         public void saveFavouriteCinemas() {
             SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + currentCity.getId(), MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             for (Cinema cinema : cinemas.values()) {
                 if (cinema.getFavourite() > 0) {
                     editor.putLong(cinema.getName(), cinema.getFavourite());
                 } else {
                     editor.remove(cinema.getName());
                 }
             }
             editor.commit();
         }
 
         @Override
         public Map<String, Cinema> getFavouriteCinemas() {
             return favoriteCinemas;
         }
 
         @Override
         public void saveCinemaSortOrder(CinemaSortOrder sortOrder) {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putInt(PREF_CINEMA_SORT_ORDER, sortOrder.ordinal());
             editor.commit();
         }
 
         @Override
         public CinemaSortOrder getCinemaSortOrder() {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             int order = preferences.getInt(PREF_CINEMA_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());
             for (CinemaSortOrder sortOrder : CinemaSortOrder.values()) {
                 if (sortOrder.ordinal() == order) return sortOrder;
             }
 
             return CinemaSortOrder.BY_CAPTION;
         }
 
         @Override
         public void saveMovieSortOrder(MovieSortOrder sortOrder) {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putInt(PREF_MOVIE_SORT_ORDER, sortOrder.ordinal());
             editor.commit();
         }
 
         @Override
         public MovieSortOrder getMovieSortOrder() {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             int order = preferences.getInt(PREF_MOVIE_SORT_ORDER, MovieSortOrder.BY_POPULAR.ordinal());
             for (MovieSortOrder sortOrder : MovieSortOrder.values()) {
                 if (sortOrder.ordinal() == order) return sortOrder;
             }
 
             return MovieSortOrder.BY_POPULAR;
         }
 
         @Override
         public void saveMovieWithScheduleSortOrder(MovieSortOrder sortOrder) {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             SharedPreferences.Editor editor = preferences.edit();
             editor.putInt(PREF_MOVIE_W_SCHEDULE_SORT_ORDER, sortOrder.ordinal());
             editor.commit();
         }
 
         @Override
         public MovieSortOrder getMovieWithScheduleSortOrder() {
             SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
             int order = preferences.getInt(PREF_MOVIE_W_SCHEDULE_SORT_ORDER, MovieSortOrder.BY_TIME_LEFT.ordinal());
             for (MovieSortOrder sortOrder : MovieSortOrder.values()) {
                 if (sortOrder.ordinal() == order) return sortOrder;
             }
 
             return MovieSortOrder.BY_TIME_LEFT;
         }
 
         @Override
         public void saveCurrentCity(City city) {
             saveCurrentCityImpl(city);
         }
 
         @Override
         public City getCurrentCity() {
             return getCurrentCityImpl();
         }
     }
 
     private class LocationStateImpl implements LocationState {
         private boolean isLocationListeningStarted = false;
         private List<LocationClient> locationClients;
         private final LocationListener locationListener;
         private Location currentLocation;
 
         {
             locationClients = new ArrayList<LocationClient>();
 
             locationListener = new LocationListener() {
                 public void onLocationChanged(Location location) {
                     updateCurrentLocation(location);
                 }
 
                 public void onStatusChanged(String s, int i, Bundle bundle) {}
 
                 public void onProviderEnabled(String s) {}
 
                 public void onProviderDisabled(String s) {}
             };
         }
 
         @Override
         public void addLocationClient(LocationClient client) {
             if (!locationClients.contains(client)) {
                 locationClients.add(client);
             }
         }
 
         @Override
         public void removeLocationClient(LocationClient client) {
             locationClients.remove(client);
         }
 
         @Override
         public Location getCurrentLocation() {
             return currentLocation;
         }
 
         @Override
         public void updateCurrentLocation(Location location) {
             currentLocation = LocationHelper.selectBetterLocation(location, currentLocation);
             for (LocationClient client : locationClients) {
                 client.onLocationChanged(location);
             }
         }
 
         @Override
         public void startLocationListening() {
             if (isLocationListeningStarted) return;
 
             LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 
             Criteria criteria = new Criteria();
             criteria.setAltitudeRequired(false);
             criteria.setBearingRequired(false);
             criteria.setCostAllowed(true);
 
             criteria.setAccuracy(Criteria.ACCURACY_COARSE);
             criteria.setPowerRequirement(Criteria.POWER_LOW);
             String coarseProvider = locationManager.getBestProvider(criteria, true);
 
             criteria.setAccuracy(Criteria.ACCURACY_FINE);
             criteria.setPowerRequirement(Criteria.POWER_HIGH);
             String fineProvider = locationManager.getBestProvider(criteria, true);
 
             Location coarseLocation = null;
             if (coarseProvider != null) {
                 coarseLocation = locationManager.getLastKnownLocation(coarseProvider);
                 locationManager.requestLocationUpdates(
                         coarseProvider,
                         LocationHelper.COARSE_TIME_LISTEN_TIMEOUT,
                         LocationHelper.COARSE_LISTEN_DISTANCE,
                         locationListener);
             }
 
             Location fineLocation = null;
             if (fineProvider != null) {
                 fineLocation = locationManager.getLastKnownLocation(fineProvider);
                 locationManager.requestLocationUpdates(
                         fineProvider,
                         LocationHelper.FINE_TIME_LISTEN_TIMEOUT,
                         LocationHelper.FINE_LISTEN_DISTANCE,
                         locationListener);
             }
 
             if (coarseLocation == null && fineLocation == null) {
                 currentLocation = null;
             } else if (coarseLocation != null && fineLocation == null) {
                 currentLocation = coarseLocation;
             } else if (coarseLocation == null) {
                 currentLocation = fineLocation;
             } else {
                 currentLocation = LocationHelper.selectBetterLocation(coarseLocation, fineLocation);
             }
 
             isLocationListeningStarted = true;
         }
 
         @Override
         public void stopLocationListening() {
             LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
             locationManager.removeUpdates(locationListener);
             locationManager.removeUpdates(locationListener);
             isLocationListeningStarted = false;
         }
     }
 
     private class ActivitiesStateImpl implements ActivitiesState {
         private static final String DUMP_FILE = "activities_state_dump.xml";
 
         private Map<String, ActivityState> states;
 
        public ActivitiesStateImpl(ApplicationSettings settings, boolean local) {
             ActivitiesStateRestorer restorer = new ActivitiesStateRestorer(new File(getCacheDir(), DUMP_FILE), settings);
            if (local) {
                states = restorer.getStates();
            } else {
                states = new HashMap<String, ActivityState>();
            }
         }
 
         @Override
         public ActivityState getState(String cookie) {
             return states.get(cookie);
         }
 
         @Override
         public void setState(String cookie, ActivityState state) {
             states.put(cookie, state);
         }
 
         @Override
         public void removeState(String cookie) {
             states.remove(cookie);
         }
 
         @Override
         public void dump() {
             StringBuilder xml = new StringBuilder();
             Calendar date = Calendar.getInstance();
             xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><data date=\"");
 
             String year = Integer.toString(date.get(Calendar.YEAR));
             String month = Integer.toString(date.get(Calendar.MONTH));
             String day = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
             String hour = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
             String min = Integer.toString(date.get(Calendar.MINUTE));
             String sec = Integer.toString(date.get(Calendar.SECOND));
             xml.append(year).append(".").append(month).append(".").append(day).append(".").append(hour).append(".").append(min).append(".").append(sec);
             xml.append("\">");
 
             for (String cookie : states.keySet()) {
                 xml.append("<state cookie=\"").append(cookie).append("\"");
                 ActivityState state = states.get(cookie);
                 String cinema = state.cinema != null ? state.cinema.getName() : null;
                 String movie = state.movie != null ? state.movie.getName() : null;
                 String actor = state.actor != null ? state.actor.getName() : null;
                 String genre = state.genre != null ? state.genre.getName() : null;
 
                 xml.append(" type=\"").append(state.activityType).append("\"");
                 if (cinema != null) xml.append(" cinema=\"").append(cinema).append("\"");
                 if (movie != null) xml.append(" movie=\"").append(movie).append("\"");
                 if (actor != null) xml.append(" actor=\"").append(actor).append("\"");
                 if (genre != null) xml.append(" genre=\"").append(genre).append("\"");
                 xml.append(" />");
             }
             xml.append("</data>");
 
             try {
                 Writer output = new BufferedWriter(new FileWriter(new File(getCacheDir(), DUMP_FILE)));
                 output.write(xml.toString());
                 output.close();
             } catch (IOException e){}
         }
     }
 
     private class ImageRetrieversImpl implements ImageRetrievers {
         private static final String MOVIE_SMALL_IMAGE_RETRIEVER = "movie_small";
         private static final String MOVIE_IMAGE_RETRIEVER       = "movie_big";
         private static final String FRAME_IMAGE_RETRIEVER       = "frame";
         private static final String POSTER_IMAGE_RETRIEVER      = "poster";
 
         private MovieImageRetriever movieSmallImageRetriever;
         private MovieImageRetriever movieImageRetriever;
         private FrameImageRetriever frameImageRetriever;
         private PosterImageRetriever posterImageRetriever;
 
         private ImageRetrieversImpl(DisplayMetrics displayMetrics, Map<String, String> connectStrings) throws ImageRetriever.ObjectAlreadyExists {
             movieSmallImageRetriever = new MovieImageRetriever(MOVIE_SMALL_IMAGE_RETRIEVER, displayMetrics, connectStrings.get(PICTURES_FOLDER_KEY), getCacheDir());
             movieImageRetriever = new MovieImageRetriever(MOVIE_IMAGE_RETRIEVER, displayMetrics, connectStrings.get(PICTURES_FOLDER_KEY), getCacheDir());
             frameImageRetriever = new FrameImageRetriever(FRAME_IMAGE_RETRIEVER, displayMetrics, connectStrings.get(FRAMES_FOLDER_KEY), getCacheDir());
             posterImageRetriever = new PosterImageRetriever(POSTER_IMAGE_RETRIEVER, displayMetrics, connectStrings.get(POSTERS_FOLDER_KEY), getCacheDir());
         }
 
         @Override
         public MovieImageRetriever getMovieImageRetriever() {
             return movieImageRetriever;
         }
 
         @Override
         public MovieImageRetriever getMovieSmallImageRetriever() {
             return movieSmallImageRetriever;
         }
 
         @Override
         public FrameImageRetriever getFrameImageRetriever() {
             return frameImageRetriever;
         }
 
         @Override
         public PosterImageRetriever getPosterImageRetriever() {
             return posterImageRetriever;
         }
     }
     
     private Map<String, String> connectStrings;
 
     private ApplicationSettings settings;
     private ActivitiesState activitiesState;
     private LocationState locationState;
     private ImageRetrievers imageRetrievers;
     
     private City currentCity;
 
     private SyncStatus syncStatus;
 
     {
         connectStrings = new HashMap<String, String>();
         locationState = new LocationStateImpl();
     }
 
     public void resetSyncStatus() {
         syncStatus = null;
     }
     
     public SyncStatus syncSchedule(Activity activity, boolean local) {
         if (syncStatus != null) return syncStatus;
 
         try {
             if (local) {
                 if (restoreConnect()) {
                     syncStatus = SyncStatus.OK;
                 }
             } else {
                 syncStatus = downloadConnect();
             }
         } catch (JSONException e) {
             syncStatus = SyncStatus.BAD_RESPONSE;
         }
 
         if (syncStatus != SyncStatus.OK) {
             return syncStatus;
         }
 
         Movie.setSharedUrl(connectStrings.get(SHARED_PAGE_KEY));
 
         try {
             StringBuilder urlBuilder = new StringBuilder(connectStrings.get(SCHEDULES_FOLDER_KEY));
             urlBuilder.append(getCurrentCityImpl().getZipFileName());
             URL input = new URL(urlBuilder.toString());
             File output = new File(getCacheDir(), getCurrentCityImpl().getFileName());
             ScheduleReceiver receiver = new ScheduleReceiver(input, output);
             
             Map<String, Cinema> cinemas = new HashMap<String, Cinema>();
             Map<String, Movie> movies = new HashMap<String, Movie>();
             Map<String, MovieActor> actors = new HashMap<String, MovieActor>();
             Map<String, MovieGenre> genres = new HashMap<String, MovieGenre>();
             List<MoviePoster> posters = new ArrayList<MoviePoster>();
             syncStatus = receiver.getSchedule(cinemas, movies, actors, genres, posters, local);
             if (syncStatus != SyncStatus.OK) {
                 return syncStatus;
             }
 
             settings = new ApplicationSettingsImpl(cinemas, movies, actors, genres, posters);
            activitiesState = new ActivitiesStateImpl(settings, local);
         } catch (MalformedURLException e) {
             return (syncStatus = SyncStatus.BAD_RESPONSE);
         }
 
         DisplayMetrics displayMetrics = new DisplayMetrics();
         activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
         try {
             imageRetrievers = new ImageRetrieversImpl(displayMetrics, connectStrings);
         } catch (ImageRetriever.ObjectAlreadyExists e) {
             throw new RuntimeException(e);
         }
         
         return syncStatus;
     }
 
     public ApplicationSettings getSettings() {
         return settings;
     }
 
     public ActivitiesState getActivitiesState() {
         return activitiesState;
     }
 
     public LocationState getLocationState() {
         return locationState;
     }
 
     public ImageRetrievers getImageRetrievers() {
         return imageRetrievers;
     }
 
     public void saveCurrentCity(City city) {
         saveCurrentCityImpl(city);
     }
     
     public City getCurrentCity() {
         return getCurrentCityImpl();
     }
 
     public void restart() {
         resetSyncStatus();
 
         Intent intent = new Intent(this, StartupActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);
     }
 
     public void goHome(Context context) {
         Intent intent = new Intent(this, MainActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         context.startActivity(intent);
     }
 
     public void showPreferences(Activity activity) {
         activity.startActivity(new Intent(activity, PreferencesActivity.class));
     }
 
     public void showProblemResolver(Context context) {
         Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 
         String email = context.getString(R.string.email);
         String subject = String.format(
                 context.getString(R.string.support_email_subject),
                 context.getString(R.string.app_name),
                 context.getString(R.string.app_version),
                 context.getString(R.string.app_version_code),
                 getCurrentCity().getName());
 
         emailIntent.setType("plain/text");
         emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
         emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
 
         context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.support_email_intent_caption)));
     }
 
     public int getVersionState() {
         SharedPreferences preferences = getSharedPreferences(VERSION_FILE, MODE_PRIVATE);
         int lastVersion = preferences.getInt(CURRENT_VERSION, -1);
         int currentVersion = Integer.parseInt(getString(R.string.app_version_code));
 
         if (lastVersion < currentVersion) {
             SharedPreferences.Editor editor = preferences.edit();
             editor.putInt(CURRENT_VERSION, currentVersion);
             editor.commit();
             if (lastVersion == -1) {
                 return NEW_INSTALLATION;
             } else {
                 return NEW_VERSION;
             }
         }
 
         return OLD_VERSION;
     }
 
     public void showRateUsIfNeeded(final Context context) {
         SharedPreferences preferences = getSharedPreferences(VERSION_FILE, MODE_PRIVATE);
         int launchNumber = preferences.getInt(LAUNCH_NUMBER, 0);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(LAUNCH_NUMBER, launchNumber + 1);
         editor.commit();
 
         if (launchNumber == 5) {
             AlertDialog.Builder builder = new AlertDialog.Builder(context);
             builder.setMessage("Спасибо что до сих пор пользуетесь нашим приложением! Мы будем очень рады, если вы добавите комментарий в Google Play Store.")
                     .setCancelable(true)
                     .setPositiveButton("Нивапрос!", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             Intent intent = new Intent(Intent.ACTION_VIEW);
                             intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.dedaulus.cinematty"));
                             context.startActivity(intent);
                         }
                     });
             AlertDialog alert = builder.create();
             alert.show();
         }
     }
     
     public Map<String, String> getConnect() {
         return connectStrings;
     }
 
     private SyncStatus downloadConnect() throws JSONException {
         WebServerTalker talker = new WebServerTalker(getString(R.string.connect_url), Integer.valueOf(getString(R.string.app_version_code)));
         SyncStatus status = talker.connect();
         connectStrings = talker.getResponse();
         if (status == SyncStatus.OK) {
             dumpConnect();
         }
         return status;
     }
 
     private void dumpConnect() {
         StringBuilder xml = new StringBuilder();
         Calendar date = Calendar.getInstance();
         xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><data date=\"");
 
         String year = Integer.toString(date.get(Calendar.YEAR));
         String month = Integer.toString(date.get(Calendar.MONTH));
         String day = Integer.toString(date.get(Calendar.DAY_OF_MONTH));
         String hour = Integer.toString(date.get(Calendar.HOUR_OF_DAY));
         String min = Integer.toString(date.get(Calendar.MINUTE));
         String sec = Integer.toString(date.get(Calendar.SECOND));
         xml.append(year).append(".").append(month).append(".").append(day).append(".").append(hour).append(".").append(min).append(".").append(sec);
         xml.append("\">");
 
         for (Map.Entry<String, String> entry : connectStrings.entrySet()) {
             xml.append("<folder name=\"").append(entry.getKey()).append("\" value=\"").append(entry.getValue()).append("\" />");
         }
 
         xml.append("</data>");
 
         try {
             Writer output = new BufferedWriter(new FileWriter(new File(getCacheDir(), CONNECT_DUMP_FILE)));
             output.write(xml.toString());
             output.close();
         } catch (IOException e){}
     }
 
     private boolean restoreConnect() {
         ConnectRestorer receiver = new ConnectRestorer(new File(getCacheDir(), CONNECT_DUMP_FILE));
         connectStrings = receiver.getConnect();
         return !connectStrings.isEmpty();
     }
 
     private void saveCurrentCityImpl(City city) {
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putString(ApplicationSettingsImpl.PREF_CURRENT_CITY, String.valueOf(city.getId()));
         editor.commit();
         currentCity = city;
     }
 
     private City getCurrentCityImpl() {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         try {
             SAXParser parser = factory.newSAXParser();
             CityHandler handler = new CityHandler();
             parser.parse(getResources().openRawResource(R.raw.cities), handler);
 
             SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
             int id = Integer.parseInt(preferences.getString(ApplicationSettingsImpl.PREF_CURRENT_CITY, "-1"));
 
             List<City> cities = handler.getCityList();
             for (City city : cities) {
                 if (city.getId() == id) {
                     currentCity = city;
                     return currentCity;
                 }
             }
             currentCity = cities.get(0);
             return currentCity;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
