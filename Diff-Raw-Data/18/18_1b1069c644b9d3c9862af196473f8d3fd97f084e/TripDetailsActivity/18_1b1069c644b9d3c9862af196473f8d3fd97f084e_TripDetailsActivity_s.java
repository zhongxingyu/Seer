 package com.laskowski.simplegpstracker;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.laskowski.simplegpstracker.db.DBUtils;
 import com.laskowski.simplegpstracker.task.CreatePolylineTask;
 
 public class TripDetailsActivity extends BaseActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.trip_item_details);
 
 		mTimeElapsed = (TextView) findViewById(R.id.time);
 		mTotalDistance = (TextView) findViewById(R.id.totaldist);
 		mAvgSpeed = (TextView) findViewById(R.id.avgspeed);
 		mFragmentLayout = (LinearLayout) findViewById(R.id.fragment_layout);
 		mFragmentLayout.setVisibility(View.VISIBLE);
 
 		mMapFragment = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 		mMapFragment.setRetainInstance(true);
 
 		mMap = ((SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map)).getMap();
 
 		findViewById(R.id.curspeed).setVisibility(View.GONE);
 		findViewById(R.id.curspeed_label).setVisibility(View.GONE);
 
 		fetchDetails(savedInstanceState);
 
 	}
 
 	private void fetchDetails(Bundle savedInstanceState) {
 		Long id = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(DBUtils.KEY_TRIP_ID);
 		if (id == null) {
 			Bundle extras = getIntent().getExtras();
 			id = extras != null ? extras.getLong(DBUtils.KEY_TRIP_ID) : null;
 
 			List<LatLng> coords = null;
 
 			// get the trip data first
 			Cursor cur = DBUtils.getInstance(this).fetchTripEntry(id);
 
 			if (cur.getCount() != 0) {
 				cur.moveToFirst();
 
 				mTimeElapsed.setText(cur.getString(2));
 				mAvgSpeed.setText(cur.getString(3));
 				mTotalDistance.setText(cur.getString(4));
 			}
 
 			cur = null;
 			// then coordinates to draw on map
 			cur = DBUtils.getInstance(this).fetchCoordsForTrip(id);
 
 			if (cur.getCount() != 0) {
 				coords = new ArrayList<LatLng>(cur.getCount());
 				cur.moveToFirst();
				coords.add(new LatLng(cur.getLong(1), cur.getLong(1)));
 
 				while (cur.moveToNext()) {
					coords.add(new LatLng(cur.getLong(1), cur.getLong(1)));
 				}
 			}
 
 			new CreatePolylineTask(this).execute(coords
 					.toArray(new LatLng[coords.size()]));
 		}
 	}
 }
