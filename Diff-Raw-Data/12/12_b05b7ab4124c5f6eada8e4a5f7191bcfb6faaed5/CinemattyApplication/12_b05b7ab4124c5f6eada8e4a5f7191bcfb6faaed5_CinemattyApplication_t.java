 package com.dedaulus.cinematty;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import com.dedaulus.cinematty.framework.*;
 import com.dedaulus.cinematty.framework.tools.*;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 /*
  * User: Dedaulus
  * Date: 13.03.11
  * Time: 21:41
  */
 public class CinemattyApplication extends Application {
     private UniqueSortedList<Cinema> mCinemas;
     private UniqueSortedList<Movie> mMovies;
     private UniqueSortedList<MovieActor> mActors;
     private UniqueSortedList<MovieGenre> mGenres;
 
     private City mCurrentCity;
 
     private boolean mLocationListenStarted = false;
     private List<LocationClient> mLocationClients;
     private final LocationListener mLocationListener = new LocationListener() {
         public void onLocationChanged(Location location) {
             updateCurrentLocation(location);
         }
 
         public void onStatusChanged(String s, int i, Bundle bundle) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void onProviderEnabled(String s) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void onProviderDisabled(String s) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
     };
 
     private Location mCurrentLocation;
 
     private Stack<CurrentState> mState = new Stack<CurrentState>();
 
     private PictureRetriever mPictureRetriever = null;
     private static final String LOCAL_PICTURES_FOLDER = "pictures";
 
     private static final String FAV_CINEMAS_FILE = "cinematty_fav_cinemas";
     private static final String PREFERENCES_FILE = "cinematty_preferences";
     private static final String PREF_CINEMA_SORT_ORDER = "cinema_sort_order";
     private static final String PREF_CURRENT_CITY = "current_city";
 
     {
         mCinemas = new UniqueSortedList<Cinema>(new DefaultComparator<Cinema>());
         mMovies = new UniqueSortedList<Movie>(new DefaultComparator<Movie>());
         mActors = new UniqueSortedList<MovieActor>(new DefaultComparator<MovieActor>());
         mGenres = new UniqueSortedList<MovieGenre>(new DefaultComparator<MovieGenre>());
         mLocationClients = new ArrayList<LocationClient>();
     }
 
     public void retrieveData() throws IOException, ParserConfigurationException, SAXException {
         ScheduleReceiver receiver = new ScheduleReceiver(this, mCurrentCity.getFileName());
         StringBuffer pictureFolder = new StringBuffer();
         receiver.getSchedule(mCinemas, mMovies, mActors, mGenres, pictureFolder);
 
         String remotePictureFolder = getString(R.string.settings_url) + "/" + pictureFolder.toString();
         if (mPictureRetriever == null) {
             mPictureRetriever = new PictureRetriever(this, remotePictureFolder, LOCAL_PICTURES_FOLDER);
         } else {
             mPictureRetriever.setRemotePictureFolder(remotePictureFolder);
         }
 
         Map<String, ?> favs = getFavouriteCinemas();
         for (String caption : favs.keySet()) {
             int cinemaId = mCinemas.indexOf(new Cinema(caption));
             if (cinemaId != -1) {
                 mCinemas.get(cinemaId).setFavourite(((Long)favs.get(caption)).longValue());
             }
         }
     }
 
     public boolean isUpToDate() {
         //return mCinemas.size() != 0;
         return false;
     }
 
     public UniqueSortedList<Cinema> getCinemas() {
         return mCinemas;
     }
 
     public UniqueSortedList<Movie> getMovies() {
         return mMovies;
     }
 
     public UniqueSortedList<MovieActor> getActors() {
         return mActors;
     }
 
     public UniqueSortedList<MovieGenre> getGenres() {
         return mGenres;
     }
 
     public PictureRetriever getPictureRetriever() {
         return mPictureRetriever;
     }
 
     public CurrentState getCurrentState() {
         return mState.peek();
     }
 
     public void setCurrentState(CurrentState currentState) {
         mState.add(currentState);
     }
 
     public void revertCurrentState() {
         mState.pop();
     }
 
     public void saveFavouriteCinemas() {
         SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         for (Cinema cinema : mCinemas) {
             if (cinema.getFavourite() > 0) {
                 editor.putLong(cinema.getCaption(), cinema.getFavourite());
             } else {
                 editor.remove(cinema.getCaption());
             }
         }
 
         editor.commit();
     }
 
     private Map<String, ?> getFavouriteCinemas() {
         SharedPreferences preferences = getSharedPreferences(FAV_CINEMAS_FILE + mCurrentCity.getId(), MODE_PRIVATE);
         return preferences.getAll();
     }
 
     public void saveCinemasSortOrder(CinemaSortOrder sortOrder) {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(PREF_CINEMA_SORT_ORDER, sortOrder.ordinal());
 
         editor.commit();
     }
 
     public CinemaSortOrder getCinemaSortOrder() {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         int order = preferences.getInt(PREF_CINEMA_SORT_ORDER, CinemaSortOrder.BY_CAPTION.ordinal());
 
         for (CinemaSortOrder sortOrder : CinemaSortOrder.values()) {
             if (sortOrder.ordinal() == order) return sortOrder;
         }
 
         return CinemaSortOrder.BY_CAPTION;
     }
 
     public void saveCurrentCityId(int id) {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putInt(PREF_CURRENT_CITY, id);
 
         editor.commit();
     }
 
     public int getCurrentCityId() {
         SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
         return preferences.getInt(PREF_CURRENT_CITY, 1);
     }
 
     public City getCurrentCity() {
         return mCurrentCity;
     }
 
     public void setCurrentCity(City city) {
         mCurrentCity = city;
     }
 
     public Location getCurrentLocation() {
         return mCurrentLocation;
     }
 
     public void updateCurrentLocation(Location location) {
         mCurrentLocation = LocationHelper.selectBetterLocation(location, mCurrentLocation);
 
         for (LocationClient client : mLocationClients) {
             client.onLocationChanged(location);
         }
     }
 
     public void addLocationClient(LocationClient client) {
         if (!mLocationClients.contains(client)) {
             mLocationClients.add(client);
         }
     }
 
     public void removeLocationClient(LocationClient client) {
         mLocationClients.remove(client);
     }
 
     public void startListenLocation() {
         if (mLocationListenStarted) return;
 
         LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
 
         Criteria criteria = new Criteria();
 
         criteria.setAccuracy(Criteria.ACCURACY_COARSE);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(true);
         criteria.setPowerRequirement(Criteria.POWER_LOW);
         String coarseProvider = locationManager.getBestProvider(criteria, true);
 
         criteria.setAccuracy(Criteria.ACCURACY_FINE);
         String fineProvider = locationManager.getBestProvider(criteria, true);
 
         Location coarseLocation = null;
         if (coarseProvider != null) {
             coarseLocation = locationManager.getLastKnownLocation(coarseProvider);
             locationManager.requestLocationUpdates(coarseProvider, LocationHelper.TIME_LISTEN_TIMEOUT, 10, mLocationListener);
         }
 
         Location fineLocation = null;
         if (fineProvider != null) {
             fineLocation = locationManager.getLastKnownLocation(fineProvider);
             locationManager.requestLocationUpdates(fineProvider, LocationHelper.TIME_LISTEN_TIMEOUT, 10, mLocationListener);
         }
 
         if (coarseLocation == null && fineLocation == null) {
             mCurrentLocation = null;
         } else if (coarseLocation != null && fineLocation == null) {
             mCurrentLocation = coarseLocation;
         } else if (fineLocation != null && coarseLocation == null) {
             mCurrentLocation = fineLocation;
         } else {
             mCurrentLocation = LocationHelper.selectBetterLocation(coarseLocation, fineLocation);
         }
 
         mLocationListenStarted = true;
     }
 
     public void stopListenLocation() {
         LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
         locationManager.removeUpdates(mLocationListener);
         locationManager.removeUpdates(mLocationListener);

        mLocationListenStarted = false;
     }
 }
