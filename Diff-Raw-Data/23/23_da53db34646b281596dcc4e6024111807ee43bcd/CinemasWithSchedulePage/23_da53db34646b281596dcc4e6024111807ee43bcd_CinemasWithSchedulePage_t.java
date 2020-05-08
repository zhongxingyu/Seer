 package com.dedaulus.cinematty.activities.Pages;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.util.Pair;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.dedaulus.cinematty.ActivitiesState;
 import com.dedaulus.cinematty.ApplicationSettings;
 import com.dedaulus.cinematty.LocationState;
 import com.dedaulus.cinematty.R;
 import com.dedaulus.cinematty.activities.CinemaActivity;
 import com.dedaulus.cinematty.activities.adapters.CinemaItemWithScheduleAdapter;
 import com.dedaulus.cinematty.framework.Cinema;
 import com.dedaulus.cinematty.framework.tools.*;
 
 import java.util.*;
 
 /**
  * User: Dedaulus
  * Date: 23.02.12
  * Time: 0:45
  */
 public class CinemasWithSchedulePage implements SliderPage, LocationClient {
     private Context context;
     private ApplicationSettings settings;
     private ActivitiesState activitiesState;
     private LocationState locationState;
     private ActivityState state;
     private CinemaItemWithScheduleAdapter cinemaListAdapter;
     private Location locationFix;
     private long timeLocationFix;
     private int currentDay;
     private int dayPart;
     private View pageView;
     private boolean binded = false;
     private boolean visible = false;
 
     public CinemasWithSchedulePage(Context context, ApplicationSettings settings, ActivitiesState activitiesState, LocationState locationState, ActivityState state) {
         this.context = context;
         this.settings = settings;
         this.activitiesState = activitiesState;
         this.locationState = locationState;
         this.state = state;
     }
 
     public View getView() {
         LayoutInflater layoutInflater = LayoutInflater.from(context);
         pageView = layoutInflater.inflate(R.layout.cinema_list, null, false);
         return bindView(pageView);
     }
 
     public String getTitle() {
         return context.getString(R.string.showtime_caption);
     }
 
     public void onResume() {
         if (binded) {
             locationState.startLocationListening();
             locationState.addLocationClient(this);
             if (currentDay != settings.getCurrentDay()) {
                 setCurrentDay(settings.getCurrentDay());
             }
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
 
         inflater.inflate(R.menu.select_day_menu, menu);
         switch (currentDay) {
             case Constants.TODAY_SCHEDULE:
                 menu.findItem(R.id.submenu_select_day_today).setChecked(true);
                 break;
 
             case Constants.TOMORROW_SCHEDULE:
                 menu.findItem(R.id.submenu_select_day_tomorrow).setChecked(true);
                 break;
 
             case Constants.AFTER_TOMORROW_SCHEDULE:
                 menu.findItem(R.id.submenu_select_day_after_tomorrow).setChecked(true);
                 break;
         }
 
         inflater.inflate(R.menu.day_part_menu, menu);
         menu.findItem(R.id.submenu_day_part_whole).setChecked(true);
 
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
             case R.id.menu_select_day:
                 return true;
 
             case R.id.submenu_select_day_today:
                 setCurrentDay(Constants.TODAY_SCHEDULE);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_select_day_tomorrow:
                 setCurrentDay(Constants.TOMORROW_SCHEDULE);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_select_day_after_tomorrow:
                 setCurrentDay(Constants.AFTER_TOMORROW_SCHEDULE);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_day_part_whole:
                 setCurrentDayPart(Constants.WHOLE_DAY);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_day_part_morning:
                 setCurrentDayPart(Constants.IN_MORNING);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_day_part_afternoon:
                 setCurrentDayPart(Constants.IN_AFTERNOON);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_day_part_evening:
                 setCurrentDayPart(Constants.IN_EVENING);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
             case R.id.submenu_day_part_night:
                 setCurrentDayPart(Constants.AT_NIGHT);
                 cinemaListAdapter.sortBy(new CinemaComparator(settings.getCinemaSortOrder(), locationState.getCurrentLocation()));
                 item.setChecked(true);
                 return true;
 
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
         setCurrentDay(settings.getCurrentDay());
         ListView list = (ListView)view.findViewById(R.id.cinema_list);
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
         CinemaItemWithScheduleAdapter adapter = (CinemaItemWithScheduleAdapter)adapterView.getAdapter();
         ListView list = (ListView)view.getParent();
         Cinema cinema = (Cinema)adapter.getItem(i - list.getHeaderViewsCount());
         String cookie = UUID.randomUUID().toString();
 
         ActivityState state = this.state.clone();
         state.cinema = cinema;
         state.activityType = ActivityState.MOVIE_LIST_W_CINEMA;
         activitiesState.setState(cookie, state);
 
         Intent intent = new Intent(context, CinemaActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         intent.putExtra(Constants.CINEMA_PAGE_ID, Constants.CINEMA_DESCRIPTION_PAGE_ID);
         context.startActivity(intent);
     }
 
     private void setCurrentDay(int day) {
         settings.setCurrentDay(day);
         currentDay = day;
 
         TextView dayIndicator = (TextView)pageView.findViewById(R.id.day_indicator).findViewById(R.id.caption);
         StringBuilder builder = new StringBuilder();
         switch (currentDay) {
             case Constants.TODAY_SCHEDULE:
                 builder.append(context.getString(R.string.today));
                 break;
 
             case Constants.TOMORROW_SCHEDULE:
                 builder.append(context.getString(R.string.tomorrow));
                 break;
 
             case Constants.AFTER_TOMORROW_SCHEDULE:
                 builder.append(context.getString(R.string.after_tomorrow));
                 break;
         }
 
         builder.append(" ");
 
         switch (dayPart) {
             case Constants.IN_MORNING:
                 builder.append(context.getString(R.string.in_morning).toLowerCase());
                 break;
 
             case Constants.IN_AFTERNOON:
                 builder.append(context.getString(R.string.in_afternoon).toLowerCase());
                 break;
 
             case Constants.IN_EVENING:
                 builder.append(context.getString(R.string.in_evening).toLowerCase());
                 break;
 
             case Constants.AT_NIGHT:
                 builder.append(context.getString(R.string.at_night).toLowerCase());
                 break;
         }
 
         dayIndicator.setText(builder.toString());
 
         Pair<Calendar, Calendar> timeRange = DataConverter.getTimeRange(dayPart, currentDay);
         List<Cinema> cinemas = DataConverter.getMovieCinemasFromTimeRange(state.movie.getCinemas(currentDay).values(), state.movie, currentDay, timeRange);
 
         if (cinemas.isEmpty()) {
             pageView.findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
         } else {
             pageView.findViewById(R.id.no_schedule).setVisibility(View.GONE);
         }
 
         IdleDataSetChangeNotifier notifier = new IdleDataSetChangeNotifier();
         cinemaListAdapter = new CinemaItemWithScheduleAdapter(context, notifier, new ArrayList<Cinema>(cinemas), state.movie, currentDay, timeRange, locationState.getCurrentLocation());
         ListView list = (ListView)pageView.findViewById(R.id.cinema_list);
         list.setAdapter(cinemaListAdapter);
         list.setOnScrollListener(notifier);
     }
 
     private void setCurrentDayPart(int dayPart) {
         this.dayPart = dayPart;
         setCurrentDay(currentDay);
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
