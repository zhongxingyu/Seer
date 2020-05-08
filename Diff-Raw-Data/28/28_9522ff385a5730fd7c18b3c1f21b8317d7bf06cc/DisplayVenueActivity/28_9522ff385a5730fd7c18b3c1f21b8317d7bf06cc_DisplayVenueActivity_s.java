 package com.ucb.dcm;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.ucb.dcm.data.DataService;
 import com.ucb.dcm.data.Show;
 import com.ucb.dcm.data.Venue;
 import com.ucb.dcm.list.ShowsListAdapter;
 import com.ucb.dcm.list.VenueScheduleAdapter;
 
 import java.util.ArrayList;
 
 /**
  * Created by kurtguenther on 6/9/13.
  */
 public class DisplayVenueActivity extends SherlockListActivity {
 
     public static final String VENUE_KEY = "venue_key";
 
     private Venue mVenue;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_venue);
 
 
         //Hook up the data
         mVenue = (Venue) getIntent().getSerializableExtra(VENUE_KEY);
 
         setTitle(mVenue.name);
 
         Cursor c = mVenue.getSchedule();
         VenueScheduleAdapter mAdpt = new VenueScheduleAdapter(this, c);
         setListAdapter(mAdpt);
     }
 
 }
