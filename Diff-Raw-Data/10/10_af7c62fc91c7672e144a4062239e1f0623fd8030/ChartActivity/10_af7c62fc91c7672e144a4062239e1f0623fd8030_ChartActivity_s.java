 package com.pwr.zpi;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 
 import com.fima.chartview.ChartView;
 import com.fima.chartview.LinearSeries;
 import com.fima.chartview.LinearSeries.LinearPoint;
 import com.pwr.zpi.adapters.ValueLabelAdapter;
 import com.pwr.zpi.adapters.ValueLabelAdapter.LabelOrientation;
 import com.pwr.zpi.utils.ChartDataHelperContainter;
 
 public class ChartActivity extends Activity {
 	
 	public static final String CHART_DATA_KEY = "chart_data";
 	
 	ChartView speed;
 	ChartView altitude;
 	ChartDataHelperContainter container;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.chart_activity);
 		
 		speed = (ChartView) findViewById(R.id.speed_chart_view);
 		altitude = (ChartView) findViewById(R.id.altitude_chart_view);
 		
 		container = getChartDataFromIntent();
 		
 		new ChartDataLoader().execute(null, null); //overloaded method, two nulls to distinct
 	}
 	
 	private ChartDataHelperContainter getChartDataFromIntent() {
 		return getIntent().getParcelableExtra(CHART_DATA_KEY);
 	}
 	
 	private class ChartDataLoader extends AsyncTask<Void, Void, Void> {
 		
 		LinearSeries seriesSpeed;
 		LinearSeries seriesAltitude;
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			seriesSpeed = new LinearSeries();
 			seriesSpeed.setLineColor(getResources().getColor(R.color.chart_speed));
 			seriesSpeed.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));
 			
 			seriesAltitude = new LinearSeries();
 			seriesAltitude.setLineColor(getResources().getColor(R.color.chart_altitude));
 			seriesAltitude.setLineWidth(getResources().getDimension(R.dimen.chart_line_width));
 			
 			double distance[] = container.getDistance();
 			double altitude[] = container.getAltitude();
 			double speed[] = container.getSpeed();
 			
 			for (int i = 0; i < distance.length; i++) {
				seriesSpeed.addPoint(new LinearPoint(distance[i], speed[i]));
				seriesAltitude.addPoint(new LinearPoint(distance[i], altitude[i]));
 			}
 			
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			// Add chart view data
 			speed.addSeries(seriesSpeed);
 			speed.setLeftLabelAdapter(new ValueLabelAdapter(ChartActivity.this, LabelOrientation.VERTICAL));
 			speed.setBottomLabelAdapter(new ValueLabelAdapter(ChartActivity.this, LabelOrientation.HORIZONTAL));
 			
 			altitude.addSeries(seriesAltitude);
 			altitude.setLeftLabelAdapter(new ValueLabelAdapter(ChartActivity.this, LabelOrientation.VERTICAL));
 			altitude.setBottomLabelAdapter(new ValueLabelAdapter(ChartActivity.this, LabelOrientation.HORIZONTAL));
 			
 			speed.setVisibility(View.VISIBLE);
 			altitude.setVisibility(View.VISIBLE);
 			findViewById(R.id.progressBarSpeed).setVisibility(View.GONE);
 			findViewById(R.id.progressBarAltitude).setVisibility(View.GONE);
 		}
 	}
 }
