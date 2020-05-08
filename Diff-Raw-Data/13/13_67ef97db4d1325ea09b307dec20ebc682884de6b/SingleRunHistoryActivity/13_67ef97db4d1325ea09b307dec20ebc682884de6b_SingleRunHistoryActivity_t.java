 package com.pwr.zpi;
 
 import java.util.LinkedList;
 
 import android.content.Intent;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.utils.LineChart;
 import com.pwr.zpi.utils.Pair;
 import com.pwr.zpi.utils.TimeFormatter;
 
 public class SingleRunHistoryActivity extends FragmentActivity implements
 	OnClickListener {
 	
 	protected static final String RUN_ID = "runID";
 	
 	GoogleMap mMap;
 	LatLngBounds.Builder boundsBuilder;
 	Polyline traceOnMapObject;
 	private TextView distanceTextView;
 	private TextView timeTextView;
 	private TextView avgPaceTextView;
 	private TextView avgSpeedTextView;
 	private Button chartButton;
 	private Button splitsButton;
 	
 	SingleRun run;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.single_run_history_activity);
 		
 		init();
 		loadData(getIntent().getLongExtra(HistoryActivity.ID_TAG, 0));
 		initFields();
 		showData();
 		addListeners();
 		mapCenter();
 	}
 	
 	private void init() {
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
 			.findFragmentById(R.id.map);
 		mMap = mapFragment.getMap();
 		chartButton = (Button) findViewById(R.id.buttonCharts);
 		splitsButton = (Button) findViewById(R.id.buttonSplits);
 	}
 	
 	private void mapCenter() {
 		
 		LinkedList<LinkedList<Pair<Location, Long>>> traceWithTime = run
 			.getTraceWithTime();
 		
 		boundsBuilder = new LatLngBounds.Builder();
 		double lastDistance = 0;
 		double newDistance = 0;
 		for (LinkedList<Pair<Location, Long>> singleTrace : traceWithTime) {
 			PolylineOptions polyLine = new PolylineOptions();
 			
 			Location lastLocation = null;
 			for (Pair<Location, Long> singlePoint : singleTrace) {
 				Location location = singlePoint.first;
 				LatLng latLng = new LatLng(location.getLatitude(),
 					location.getLongitude());
 				polyLine.add(latLng);
 				boundsBuilder.include(latLng);
 				if (lastLocation != null)
 				{
 					newDistance += location.distanceTo(lastLocation);
 					int showDistance = (int) (newDistance / 1000);
 					if (showDistance - (int) (lastDistance / 1000) > 0)
 					{
 						addMarker(location, showDistance);
 					}
 					
 				}
 				lastDistance = newDistance;
 				
 				lastLocation = location;
 				
 			}
 			if (mMap != null) {
 				traceOnMapObject = mMap.addPolyline(polyLine);
 			}
 		}
 		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
 			
 			@Override
 			public void onCameraChange(CameraPosition arg0) {
 				// Move camera.
 				LatLngBounds bounds = boundsBuilder.build();
 				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
 				// Remove listener to prevent position reset on camera move.
 				mMap.setOnCameraChangeListener(null);
 			}
 		});
		LinkedList<LinkedList<Pair<Location, Long>>> trace = run.getTraceWithTime();
		
		if (!trace.isEmpty()) {
			Location start = trace.getFirst().getFirst().first;
			Location finish = trace.getLast().getLast().first;
			addStartAndFinish(new LatLng(start.getLatitude(), start.getLongitude()),
				new LatLng(finish.getLatitude(), finish.getLongitude()));
 		}
 		
 	}
 	
 	private void addStartAndFinish(LatLng startPos, LatLng finishPos)
 	{
 		
 		Marker start = mMap.addMarker(new MarkerOptions()
 			.position(startPos)
 			.icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
 		Marker meta = mMap.addMarker(new MarkerOptions()
 			.position(finishPos)
 			.icon(BitmapDescriptorFactory.fromResource(R.drawable.meta)));
 		
 	}
 	
 	private void addMarker(Location location, int distance) {
 		Marker marker = mMap.addMarker(new MarkerOptions().position(
 			new LatLng(location.getLatitude(), location.getLongitude()))
 			.title(distance + "km"));
 		marker.showInfoWindow();
 	}
 	
 	private void initFields() {
 		distanceTextView = (TextView) findViewById(R.id.TextView1History);
 		timeTextView = (TextView) findViewById(R.id.TextView2History);
 		avgPaceTextView = (TextView) findViewById(R.id.TextView3History);
 		avgSpeedTextView = (TextView) findViewById(R.id.TextView4History);
 	}
 	
 	private void showData() {
 		// show distance
 		distanceTextView
 			.setText(String.format("%.3f", run.getDistance() / 1000));
 		
 		// show time
 		long time = run.getRunTime();
 		timeTextView.setText(TimeFormatter.formatTimeHHMMSS(time));
 		
 		//show avg pace
 		double speed = run.getDistance() / 1000 / run.getRunTime() * 1000 * 60 * 60;
 		double pace = 1 / speed * 60;
 		avgPaceTextView.setText(TimeFormatter.formatTimeMMSSorHHMMSS(pace));
 		
 		//show avgSpeed
 		avgSpeedTextView.setText(String.format("%.2f", speed));
 		
 	}
 	
 	private void addListeners() {
 		chartButton.setOnClickListener(this);
 		splitsButton.setOnClickListener(this);
 	}
 	
 	private void loadData(long runID) {
 		Database database = new Database(this);
 		run = database.getRun(runID);
 		LinkedList<LinkedList<Pair<Location, Long>>> traceWithTime = run
 			.getTraceWithTime();
 		for (LinkedList<Pair<Location, Long>> singleTrace : traceWithTime) {
 			PolylineOptions polyLine = new PolylineOptions();
 			for (Pair<Location, Long> singlePoint : singleTrace) {
 				polyLine.add(new LatLng(singlePoint.first.getLatitude(),
 					singlePoint.first.getLongitude()));
 			}
 			if (mMap != null) {
 				mMap.addPolyline(polyLine);
 			}
 		}
 		
 	}
 	
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 		
 	}
 	
 	@Override
 	public void onClick(View view) {
 		if (view == chartButton) {
 			Intent i = LineChart.getChartForData(run, this);
 			startActivity(i);
 		}
 		else if (view == splitsButton) {
 			Intent i = new Intent(SingleRunHistoryActivity.this,
 				SplitsActivity.class);
 			i.putExtra(RUN_ID, run.getRunID());
 			startActivity(i);
 		}
 	}
 }
