 package com.dedaulus.cinematty.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.text.style.UnderlineSpan;
 import android.view.*;
 import android.widget.*;
 import com.dedaulus.cinematty.CinemattyApplication;
 import com.dedaulus.cinematty.R;
 import com.dedaulus.cinematty.activities.adapters.MovieItemWithScheduleAdapter;
 import com.dedaulus.cinematty.activities.adapters.SortableAdapter;
 import com.dedaulus.cinematty.activities.adapters.StoppableAndResumable;
 import com.dedaulus.cinematty.framework.Movie;
 import com.dedaulus.cinematty.framework.tools.*;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * User: Dedaulus
  * Date: 12.10.11
  * Time: 10:05
  */
 public class MovieWithScheduleListActivity extends Activity {
     private CinemattyApplication mApp;
     private SortableAdapter<Movie> mMovieListAdapter;
     private UniqueSortedList<Movie> mScopeMovies;
     private ActivityState mState;
     private String mStateId;
     private int mCurrentDay;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.movie_list);
 
         mApp = (CinemattyApplication)getApplication();
         if (!mApp.isDataActual()) {
             boolean b = false;
             try {
                 b = mApp.retrieveData(true);
             } catch (Exception e) {}
             if (!b) {
                 mApp.restart();
                 finish();
                 return;
             }
         }
 
         mStateId = getIntent().getStringExtra(Constants.ACTIVITY_STATE_ID);
         mState = mApp.getState(mStateId);
         if (mState == null) throw new RuntimeException("ActivityState error");
 
         findViewById(R.id.movie_list_title).setVisibility(View.VISIBLE);
         View captionView = findViewById(R.id.cinema_panel_in_movie_list);
         ListView list = (ListView)findViewById(R.id.movie_list);
 
         switch (mState.activityType) {
         case ActivityState.MOVIE_LIST_W_CINEMA:
             captionView.setVisibility(View.GONE);
             LayoutInflater layoutInflater = LayoutInflater.from(this);
             View view = layoutInflater.inflate(R.layout.cinema_info, null, false);
             setCinemaHeader(view);
 
             list.addHeaderView(view, null, false);
 
             setCurrentDay(mApp.getCurrentDay());
 
             TextView textView = (TextView)findViewById(R.id.titlebar_caption);
             textView.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View view) {
                     registerForContextMenu(view);
                     view.showContextMenu();
                 }
             });
             break;
 
         default:
             throw new RuntimeException("ActivityType error");
         }
 
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 onScheduleItemClick(adapterView, view, i, l);
             }
         });
     }
 
     @Override
     protected void onResume() {
         ((StoppableAndResumable)mMovieListAdapter).onResume();
         if (mCurrentDay != mApp.getCurrentDay()) {
             setCurrentDay(mApp.getCurrentDay());
         }
         mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder(), mApp.getCurrentDay()));
 
         super.onResume();
     }
 
     @Override
     protected void onStop() {
         ((StoppableAndResumable)mMovieListAdapter).onStop();
         mApp.dumpData();
 
         super.onStop();
     }
 
     @Override
     public void onBackPressed() {
         mApp.removeState(mStateId);
 
         super.onBackPressed();
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         menu.clear();
 
         MenuInflater inflater = getMenuInflater();
 
         inflater.inflate(R.menu.home_menu, menu);
 
         if (mState.cinema.getAddress() != null) {
             inflater.inflate(R.menu.show_map_menu, menu);
         }
 
         if (mState.cinema.getPhone() != null) {
             inflater.inflate(R.menu.call_menu, menu);
         }
 
         inflater.inflate(R.menu.select_day_menu, menu);
         if (mCurrentDay == Constants.TODAY_SCHEDULE) {
             menu.findItem(R.id.menu_day).setTitle(R.string.tomorrow);
         } else {
             menu.findItem(R.id.menu_day).setTitle(R.string.today);
         }
 
         inflater.inflate(R.menu.movie_sort_menu, menu);
         switch (mApp.getMovieSortOrder()) {
         case BY_CAPTION:
             menu.findItem(R.id.submenu_movie_sort_by_caption).setChecked(true);
             break;
 
         case BY_POPULAR:
             menu.findItem(R.id.submenu_movie_sort_by_popular).setChecked(true);
             break;
 
         default:
             break;
         }
 
         inflater.inflate(R.menu.about_menu, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_home:
             mApp.goHome(this);
             return true;
 
         case R.id.menu_call:
             onCinemaPhoneClick(null);
             return true;
 
         case R.id.menu_show_map:
             onCinemaAddressClick(null);
             return true;
 
         case R.id.menu_day:
             int day = mCurrentDay == Constants.TODAY_SCHEDULE ? Constants.TOMORROW_SCHEDULE : Constants.TODAY_SCHEDULE;
             setCurrentDay(day);
             mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder(), day));
             return true;
 
         case R.id.menu_movie_sort:
             return true;
 
         case R.id.submenu_movie_sort_by_caption:
             mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_CAPTION, mApp.getCurrentDay()));
             mApp.saveMovieSortOrder(MovieSortOrder.BY_CAPTION);
             item.setChecked(true);
             return true;
 
         case R.id.submenu_movie_sort_by_popular:
             mMovieListAdapter.sortBy(new MovieComparator(MovieSortOrder.BY_POPULAR, mApp.getCurrentDay()));
             mApp.saveMovieSortOrder(MovieSortOrder.BY_POPULAR);
             item.setChecked(true);
             return true;
 
         case R.id.menu_about:
             mApp.showAbout(this);
             return true;
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.select_day_submenu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.submenu_select_day_today:
             if (mCurrentDay != Constants.TODAY_SCHEDULE) {
                 setCurrentDay(Constants.TODAY_SCHEDULE);
                 mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder(), Constants.TODAY_SCHEDULE));
             }
             return true;
         case R.id.submenu_select_day_tomorrow:
             if (mCurrentDay != Constants.TOMORROW_SCHEDULE) {
                 setCurrentDay(Constants.TOMORROW_SCHEDULE);
                 mMovieListAdapter.sortBy(new MovieComparator(mApp.getMovieSortOrder(), Constants.TOMORROW_SCHEDULE));
             }
             return true;
         default:
             return super.onContextItemSelected(item);
         }
     }
 
     private void changeTitleBar() {
         findViewById(R.id.movie_list_title_day).setVisibility(View.VISIBLE);
         TextView text = (TextView)findViewById(R.id.titlebar_caption);
         switch (mApp.getCurrentDay()) {
         case Constants.TODAY_SCHEDULE:
             text.setText(R.string.today);
             break;
         case Constants.TOMORROW_SCHEDULE:
             text.setText(R.string.tomorrow);
             break;
         }
     }
 
     private void onScheduleItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter)adapterView.getAdapter();
         MovieItemWithScheduleAdapter adapter = (MovieItemWithScheduleAdapter)headerAdapter.getWrappedAdapter();
         ListView list = (ListView)view.getParent();
         Movie movie = (Movie)adapter.getItem(i - list.getHeaderViewsCount());
         String cookie = UUID.randomUUID().toString();
 
         ActivityState state = mState.clone();
         state.movie = movie;
         state.activityType = ActivityState.MOVIE_INFO_W_SCHED;
         mApp.setState(cookie, state);
 
         Intent intent = new Intent(this, MovieActivity.class);
         intent.putExtra(Constants.ACTIVITY_STATE_ID, cookie);
         startActivity(intent);
     }
 
     public void onHomeButtonClick(View view) {
         mApp.goHome(this);
     }
 
     public void onDayButtonClick(View view) {
         registerForContextMenu(view);
         view.showContextMenu();
     }
 
     private void setCurrentDay(int day) {
         mApp.setCurrentDay(day);
         mCurrentDay = day;
 
         changeTitleBar();
 
         mScopeMovies = mState.cinema.getMovies(mApp.getCurrentDay());
         if (mScopeMovies == null) {
             mScopeMovies = new UniqueSortedList<Movie>(null);
             findViewById(R.id.no_schedule).setVisibility(View.VISIBLE);
         } else {
             findViewById(R.id.no_schedule).setVisibility(View.GONE);
         }
 
         StoppableAndResumable sar = (StoppableAndResumable)mMovieListAdapter;
         if (sar != null) sar.onStop();
         mMovieListAdapter = new MovieItemWithScheduleAdapter(this, new ArrayList<Movie>(mScopeMovies), mState.cinema, mApp.getCurrentDay(), mApp.getPictureRetriever());
         sar = (StoppableAndResumable)mMovieListAdapter;
         sar.onResume();
         ListView list = (ListView)findViewById(R.id.movie_list);
         list.setAdapter(mMovieListAdapter);
     }
 
     private void setCinemaHeader(View view) {
         setCinemaFavourite(view);
         setCinemaCaption(view);
         setCinemaAddress(view);
         setCinemaPhone(view);
         setCinemaUrl(view);
     }
 
     private void setCinemaFavourite(View view) {
         ImageView favIcon = (ImageView)view.findViewById(R.id.fav_icon_in_cinema_info);
 
         if (mState.cinema.getFavourite() > 0) {
             favIcon.setImageResource(android.R.drawable.btn_star_big_on);
         } else {
             favIcon.setImageResource(android.R.drawable.btn_star_big_off);
         }
     }
 
     private void setCinemaCaption(View view) {
         TextView caption = (TextView)view.findViewById(R.id.cinema_caption);
         caption.setText(mState.cinema.getCaption());
     }
 
     private void setCinemaAddress(View view) {
         View panel = view.findViewById(R.id.cinema_address_panel);
         if (mState.cinema.getAddress() != null) {
             TextView address = (TextView)view.findViewById(R.id.cinema_address);
             address.setText(mState.cinema.getAddress());
 
             TextView into = (TextView)view.findViewById(R.id.cinema_into);
             if (mState.cinema.getInto() != null) {
                 into.setText(mState.cinema.getInto());
                 into.setVisibility(View.VISIBLE);
             } else {
                 into.setVisibility(View.GONE);
             }
 
             TextView metro = (TextView)view.findViewById(R.id.cinema_metro);
             if (mState.cinema.getMetro() != null) {
                 metro.setText(getString(R.string.metro_near) + ": " + mState.cinema.getMetro());
                 metro.setVisibility(View.VISIBLE);
             } else {
                 metro.setVisibility(View.GONE);
             }
 
             panel.setVisibility(View.VISIBLE);
         } else {
             panel.setVisibility(View.GONE);
         }
     }
 
     private void setCinemaPhone(View view) {
         View panel = view.findViewById(R.id.cinema_phone_panel);
         if (mState.cinema.getPhone() != null) {
             TextView phone = (TextView)view.findViewById(R.id.cinema_phone);
             phone.setText(mState.cinema.getPhone());
 
             panel.setVisibility(View.VISIBLE);
         }
         else {
             panel.setVisibility(View.GONE);
         }
     }
 
     private void setCinemaUrl(View view) {
         TextView url = (TextView)view.findViewById(R.id.cinema_url);
         if (mState.cinema.getUrl() != null) {
             StringBuilder buf = new StringBuilder(mState.cinema.getUrl());
 
             if (mState.cinema.getUrl().startsWith("http://")) {
                 buf.delete(0, "http://".length());
             }
 
             int slashPos = buf.indexOf("/");
             if (slashPos != -1) {
                 buf.delete(slashPos, buf.length());
             }
 
             SpannableString str = new SpannableString(buf.toString());
             str.setSpan(new UnderlineSpan(), 0, buf.length(), 0);
 
             url.setText(str);
 
             url.setVisibility(View.VISIBLE);
         } else {
             url.setVisibility(View.GONE);
         }
     }
 
     public void onCinemaAddressClick(View view) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse("geo:0,0?q=Россия, " + mApp.getCurrentCity().getName() + ", " + mState.cinema.getAddress()));
         startActivity(intent);
     }
 
     public void onCinemaPhoneClick(View view) {
         Intent intent = new Intent(Intent.ACTION_DIAL);
         intent.setData(Uri.parse("tel:+7" + mState.cinema.getPlainPhone()));
         startActivity(intent);
     }
 
     public void onCinemaUrlClick(View view) {
         if (mState.cinema.getUrl() != null) {
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setData(Uri.parse(mState.cinema.getUrl()));
             startActivity(intent);
         }
     }
 
     public void onCinemaFavIconClick(View view) {
         if (mState.cinema.getFavourite() > 0) {
             mState.cinema.setFavourite(false);
             ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_off);
         } else {
             mState.cinema.setFavourite(true);
             ((ImageView)view).setImageResource(android.R.drawable.btn_star_big_on);
         }
     }
 }
