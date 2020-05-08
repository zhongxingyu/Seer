 package edu.westmont.course;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.jjoe64.graphview.*;
 import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TableLayout.LayoutParams;
 import android.widget.TextView;
 
 public class RunStatistics extends Activity {
 
 	private String runName="";
 	private String competeName = "";
 	private PositionsDataSource datasource;
 	private boolean useMetric = false;
 	List<Position> runPositions = new ArrayList<Position>();
 	List<Position> competePositions = new ArrayList<Position>();;
 	final private double metersToMph = 2.236936364;
 	final private double metersToKph = 3.6;

 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_run_statistics);
 		Intent intent = getIntent();
 		runName = intent.getStringExtra(MainActivity.RUN_NAME);
 		competeName = intent.getStringExtra(MainActivity.COMPETE_NAME);
 		useMetric = intent.getBooleanExtra(MainActivity.USE_METRIC, false);
 		Log.i("onCreate","Started RunStatistics. The Run is--" + runName + "-- and the competeName is--" + competeName + "--");
 		datasource = new PositionsDataSource(this);
 		datasource.open();
 		Log.i("RunStatistics","Gathering data from DB for " + runName);
 		if (runName.length() > 0) runPositions = datasource.getCurrentRun(runName);
 		Log.i("RunStatistics","Gathering data from DB for " + competeName);
 		if (competeName.length() > 0) competePositions = datasource.getCurrentRun(competeName);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.run_statistics, menu);
 		Log.v("RunStatistics","Created menu bar in RunStatistics activity.");
 		return true;
 	}
 
 	private double convertSpeed(double value){
 		Log.i("RunStatistics","Converting the speed " + String.valueOf(value) + " to kph or mph.");
 		if (useMetric){
 			return value*metersToKph;
 		}
		return value*metersToMph;
 	}
 
 	private GraphViewSeries getGraphViewSeries(List<Position> input, String graphType, String lineName, int color) {
 		Log.v("RunStatistics","Creating graph view for " + lineName);
 		long startTime = 0;
 		if (input.size()>0){
 			startTime = input.get(0).getTime();
 		}
 		double x,y=0;
 		Point[] points = new Point[input.size()];
 		for (int i=0; i<input.size(); i++){
 			Position position = input.get(i);
 			x = (position.getTime() - startTime) / 1000;
			if (graphType.equals("Altitude")) y = (double) position.getAltitude();
			if (graphType.equals("Speed")) y = (double) position.getSpeed();
 			points[i] = new Point(x,y);
 		}
 		GraphViewSeries gvSeries = null;
 		if (points.length > 0) gvSeries = new GraphViewSeries(lineName, new GraphViewSeriesStyle(color, 5),points);
 		Log.v("RunStatistics","Successfully created graph view for " + lineName);
 		return gvSeries;
 	}
 
 	private void displayGraph(String type){
 		Log.i("RunStatistics","Display the graph with the type of " + type);
 		String yLabel = "";
 		if (type.equals("Altitude")) {
 			if (useMetric) yLabel = " (meters)";
 			if (!useMetric) yLabel = " (feet)";
 		}
 		if (type.equals("Speed")) {
 			if (useMetric) yLabel = " (kph)";
 			if (!useMetric) yLabel = " (mph)";
 		}
 		GraphViewSeries runSeries = getGraphViewSeries(runPositions, type, runName, Color.BLUE);
 		GraphViewSeries competeSeries = getGraphViewSeries(competePositions, type, competeName, Color.RED);
 		Log.v("RunStatistics","Made it past the point initialization. There are: " + competePositions.size() + " points");
 		GraphView graphView = new LineGraphView(this,type+yLabel);
 		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
 			public String formatLabel(double value, boolean isValueX) {
 				if (!isValueX) {
					return ""+(int) convertSpeed(value);
 				}
 				return null; // let graphview generate X-axis label for us
 			}
 		});
 		if (runSeries != null) graphView.addSeries(runSeries);
 		if (competeSeries != null) graphView.addSeries(competeSeries);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
 		graphView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
 		layout.removeViewAt(1);
 		layout.addView(graphView, 1);
 	}
 
 	public void showStats(View view){
 		Log.v("RunStatistics","Displaying Statistics");
 		StringBuilder builder = new StringBuilder();
 		builder = statisticsStringMaker(runName, builder);
 		builder = statisticsStringMaker(competeName, builder);
 		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
 		Log.v("RunStatistics","Constructing TextView");
 		TextView textView = new TextView(getBaseContext());
 		textView.setText(builder.toString(), null);
 		Log.v("RunStatistics","Adding view and displaying statistics.");
 		layout.removeViewAt(1);
 		textView.setVerticalScrollbarPosition(TextView.SCROLLBAR_POSITION_DEFAULT);
 		textView.setMovementMethod(new ScrollingMovementMethod());
 		layout.addView(textView,1);
 	}
 	
 	private StringBuilder statisticsStringMaker(String route, StringBuilder message) {
 		if (route.length() > 0) {
 			Log.i("RunStatistics","Adding statistics for " + route);
 			DistanceFinder ranger = new DistanceFinder(useMetric);
 			message.append("For Route " + route + ":\n");
 			message.append("Total time: " + ranger.getElapsedTimeString(datasource.totalTime(route)*1000) + "\n");
 			message.append("Highest speed: " + ranger.getSpeedString(datasource.highest(route,MySQLiteHelper.COLUMN_SPEED)) + "\n");
 			message.append("Highest Altitude: " + ranger.getAltitudeString(datasource.highest(route, MySQLiteHelper.COLUMN_HIGHEST_ALTITUDE)) + "\n");
 			message.append("Total distance: " + ranger.getDistanceString(datasource.totalDistance(route)) + "\n");
 			message.append("Average speed: " + ranger.getSpeedString(datasource.averageSpeed(route)) + "\n\n");
 		}
 		return message;
 	}
 	
 
 	public void graphAltitude(View view){
 		Log.v("RunStatistics","Graphing Altitude");
 		displayGraph("Altitude");
 	}
 
 	public void graphSpeed(View view){
 		Log.v("RunStatistics","Graphing Speed");
 		displayGraph("Speed");
 	}
 
 	public void showOlderTimes(View view){
 		Log.v("RunStatistics","Displaying previous times");
 		Point[] points = datasource.timeVsNumber(runName);
 		GraphViewSeries exampleSeries = new GraphViewSeries(points);
 		GraphView graphView = new LineGraphView(this,"Previous Times");
 		graphView.addSeries(exampleSeries); // data  
 		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
 		graphView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
 		layout.removeViewAt(1);
 		layout.addView(graphView, 1);
 	}
 }
