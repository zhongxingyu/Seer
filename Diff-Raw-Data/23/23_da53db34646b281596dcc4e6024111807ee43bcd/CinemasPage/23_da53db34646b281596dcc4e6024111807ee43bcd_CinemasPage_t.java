 package com.dedaulus.cinematty.activities.Pages;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.dedaulus.cinematty.*;
 import com.dedaulus.cinematty.activities.CinemaActivity;
 import com.dedaulus.cinematty.activities.adapters.CinemaItemAdapter;
 import com.dedaulus.cinematty.framework.Cinema;
 import com.dedaulus.cinematty.framework.tools.*;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * User: Dedaulus
  * Date: 04.09.11
  * Time: 20:17
  */
 public class CinemasPage implements SliderPage, LocationClient {
     private Context context;
     private ApplicationSettings settings;
     private ActivitiesState activitiesState;
     private LocationState locationState;
     private Location locationFix;
     private long timeLocationFix;
     private CinemaItemAdapter cinemaListAdapter;
     private boolean binded = false;
     private boolean visible = false;
 
     public CinemasPage(Context context, CinemattyApplication app) {
         this.context = context;
 
         settings = app.getSettings();
         activitiesState = app.getActivitiesState();
         locationState = app.getLocationState();
     }
 
     public View getView() {
         LayoutInflater layoutInflater = LayoutInflater.from(context);
         View view = layoutInflater.inflate(R.layout.cinema_list, null, false);
 
         return bindView(view);
     }
 
     public String getTitle() {
         return context.getString(R.string.cinemas_caption);
     }
 
     public void onResume() {
         if (binded) {
             locationState.startLocationListening();
             locationState.addLocationClient(this);
             cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
             if (settings.getCinemaSortOrder() == CinemaSortOrder.BY_DISTANCE) {
                 locationFix = locationState.getCurrentLocation();
                if (locationFix != null) {
                    timeLocationFix = locationFix.getTime();
                }
             }
         }
     }
 
     public void onPause() {
         locationState.removeLocationClient(this);
         locationState.stopLocationListening();
         settings.saveFavouriteCinemas();
     }
 
     public void onStop() {}
 
     @Override
     public void setVisible(boolean visible) {
         this.visible = visible;
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = ((SherlockActivity) context).getSupportMenuInflater();
         inflater.inflate(R.menu.cinema_sort_menu, menu);
 
         switch (settings.getCinemaSortOrder()) {
         case BY_CAPTION:
             menu.findItem(R.id.submenu_cinema_sort_by_caption).setChecked(true);
             break;
 
         case BY_FAVOURITE:
             menu.findItem(R.id.submenu_cinema_sort_by_favourite).setChecked(true);
             break;
 
         case BY_DISTANCE:
             menu.findItem(R.id.submenu_cinema_sort_by_distance).setChecked(true);
             break;
 
         default:
             break;
         }
 
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_cinema_sort:
                 return true;
 
             case R.id.submenu_cinema_sort_by_caption:
                 cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_CAPTION, null));
                 settings.saveCinemaSortOrder(CinemaSortOrder.BY_CAPTION);
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_cinema_sort_by_favourite:
                 cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_FAVOURITE, null));
                 settings.saveCinemaSortOrder(CinemaSortOrder.BY_FAVOURITE);
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_cinema_sort_by_distance:
                 cinemaListAdapter.sortBy(new CinemaComparator(CinemaSortOrder.BY_DISTANCE, locationState.getCurrentLocation()));
                 locationFix = locationState.getCurrentLocation();
                if (locationFix != null) {
                    timeLocationFix = locationFix.getTime();
                }
                 settings.saveCinemaSortOrder(CinemaSortOrder.BY_DISTANCE);
                 item.setChecked(true);
                 return true;
 
             default:
                 return true;
         }
     }
 
     private View bindView(View view) {
         view.findViewById(R.id.day_indicator).findViewById(R.id.caption).setVisibility(View.GONE);
         IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
         cinemaListAdapter = new CinemaItemAdapter(context, notifier, new ArrayList<Cinema>(settings.getCinemas().values()), locationState.getCurrentLocation());
         ListView list = (ListView)view.findViewById(R.id.cinema_list);
         list.setAdapter(cinemaListAdapter);
         list.setOnScrollListener(notifier);
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 onCinemaItemClick(adapterView, view, i, l);
             }
         });
         binded = true;
         onResume();
 
         return view;
     }
 
     private void onCinemaItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         CinemaItemAdapter adapter = (CinemaItemAdapter)adapterView.getAdapter();
         ListView list = (ListView)view.getParent();
         Cinema cinema = (Cinema)adapter.getItem(i - list.getHeaderViewsCount());
         String cookie = UUID.randomUUID().toString();
 
         ActivityState state = new ActivityState(ActivityState.MOVIE_LIST_W_CINEMA, cinema, null, null, null);
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(context, CinemaActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_SHOWTIME_PAGE_ID);
         context.startActivity(intent);
     }
 
     public void onLocationChanged(Location location) {
         cinemaListAdapter.setLocation(location);
         if (locationFix == null) {
             locationFix = location;
             timeLocationFix = location.getTime();
         } else if (settings.getCinemaSortOrder() == CinemaSortOrder.BY_DISTANCE) {
             if (location.getTime() - timeLocationFix < Constants.TIME_CHANGED_ENOUGH) return;
             timeLocationFix = location.getTime();
 
             if (locationFix.distanceTo(location) < Constants.LOCATION_CHANGED_ENOUGH) return;
             locationFix = location;
 
             CinemaComparator cmp = new CinemaComparator(CinemaSortOrder.BY_DISTANCE, location);
             if (!cinemaListAdapter.isSorted(cmp)) {
                 if (visible) {
                     final ProgressDialog progressDialog = new ProgressDialog(context);
                     progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                     progressDialog.setMessage(context.getString(R.string.location_changed));
                     progressDialog.setCancelable(true);
                     progressDialog.show();
 
                     final Context context = this.context;
                     new Thread(new Runnable() {
                         public void run() {
                             try {
                                 Thread.sleep(Constants.LOCATION_CHANGED_ENOUGH_MESSAGE_TIMEOUT);
                             } catch (InterruptedException e){}
                             ((Activity)context).runOnUiThread(new Runnable() {
                                 public void run() {
                                     progressDialog.cancel();
                                 }
                             });
                         }
                     }).start();
                 }
 
                 cinemaListAdapter.sortBy(cmp);
             }
         }
     }
 }
