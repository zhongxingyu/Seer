 package fi.vincit.babyschedule.activities;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import utils.BabyEvent;
 import utils.ConsumedTime;
 import utils.ScheduleDatabase;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 import com.jjoe64.graphview.BarGraphView;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView.GraphViewSeries;
 
 import fi.vincit.babyschedule.R;
 
 public class BarStatistics extends Activity implements OnItemSelectedListener {
 	
 	Spinner mStatsChooser;
 	LinearLayout mGraphLayout;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.barstatistics);				
 		
 		mStatsChooser = (Spinner) findViewById(R.id.stats_chooser);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
 				 R.array.statistics_types, 
 				 android.R.layout.simple_spinner_item);
 
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		mStatsChooser.setAdapter(adapter);   
 		mStatsChooser.setOnItemSelectedListener(this);
 		
 		mGraphLayout = (LinearLayout) findViewById(R.id.bargraphlayout);		
 		
 		mGraphLayout.addView(generateMilkDataGraphView());	
 	}
 	
 	private GraphView generateMilkDataGraphView() {
 		
 		ArrayList<Date> dates = ScheduleDatabase.getActionDatesForAction(Settings.getCurrentBabyName(), getString(R.string.milk_activity));
 		Date oldest = dates.get(0);
 		Date now = new Date();
 		long numberOfDays = (now.getTime() - oldest.getTime());
 		Log.i("BabySchedule", "BarStatistics: how many ms ago: " + numberOfDays);
 		numberOfDays /= 24*60*60*1000;
 		Log.i("BabySchedule", "BarStatistics: how many days ago: " + numberOfDays);
 		numberOfDays++;
 		
 		
 		if( numberOfDays < 7 )
 			numberOfDays = 7;
 		GraphViewData[] data = new GraphViewData[(int)numberOfDays];
 		int maxAmountOfMilk = 0;
 		// Acquire milk data for milk / day
 		int indexForValueArray = 0;
 		
 		for( int i = (int)numberOfDays-1; i >= 0; i-- ) {
 			ArrayList<BabyEvent> events = ScheduleDatabase.getAllDbActionsFromNumberOfDaysAgo(Settings.getCurrentBabyName(), 
 																getString(R.string.milk_activity), i);
 			int daysCombined = 0;
 			for(BabyEvent event : events ) {
 				daysCombined += event.getFreeValue();
 				if( maxAmountOfMilk < event.getFreeValue() ) 
 					maxAmountOfMilk = event.getFreeValue();
 			}
 			
 			data[indexForValueArray] = new GraphViewData(indexForValueArray+1, daysCombined);
 			
 			indexForValueArray++;
 		}
 		
 		GraphViewSeries dataSeries = new GraphViewSeries(data);
 
 		// graph with dynamically generated horizontal and vertical labels
 		GraphView graphView = new BarGraphView(
 					this // context
 					, getString(R.string.milk_per_day) // heading
 			);
 		
 		// custom static labels
 		graphView.addSeries(dataSeries); // data
 		graphView.setManualYAxisBounds(maxAmountOfMilk, 0);
 		graphView.setScrollable(true);		
 		
 		if( numberOfDays > 30 ) {
 			graphView.setViewPort(numberOfDays-30, 30);
 		}
 		
 		return graphView;
 	}
 	
 	private GraphView generateSleepGraphView() {
 		
 		ArrayList<Date> dates = ScheduleDatabase.getActionDatesForAction(Settings.getCurrentBabyName(), getString(R.string.go_to_sleep));
 		ArrayList<Date> napDates = ScheduleDatabase.getActionDatesForAction(Settings.getCurrentBabyName(), getString(R.string.go_to_nap));
 		Date oldest = dates.get(0);
 		Date oldestNap  = napDates.get(0);
 		if( oldestNap.getDate() < oldest.getDate() ) {
 			oldest = oldestNap;
 		}
 		Date now = new Date();
 		long numberOfDays = (now.getTime() - oldest.getTime());
 		Log.i("BabySchedule", "BarStatistics: how many ms ago: " + numberOfDays);
 		numberOfDays /= 24*60*60*1000;
 		Log.i("BabySchedule", "BarStatistics: how many days ago: " + numberOfDays);
 		numberOfDays++;
 		
 		if( numberOfDays < 7 )
 			numberOfDays = 7;
 		GraphViewData[] data = new GraphViewData[(int)numberOfDays];
 		int indexForValueArray = 0;
 		
 		for( int i = (int)numberOfDays-1; i >= 0; i-- ) {				
 			ArrayList<BabyEvent> events = ScheduleDatabase.getAllDbActionsFromNumberOfDaysAgo(Settings.getCurrentBabyName(), 
 																getString(R.string.go_to_sleep), i);
 			events.addAll(ScheduleDatabase.getAllDbActionsFromNumberOfDaysAgo(Settings.getCurrentBabyName(), 
 																getString(R.string.go_to_nap), i));
 			ConsumedTime daysCombined = new ConsumedTime();
 			for(BabyEvent event : events ) {
				daysCombined = daysCombined.addition(ScheduleDatabase.getDurationOfSleepStartedAt(Settings.getCurrentBabyName(), event.getActionDate()));
 			}
 			
 			data[indexForValueArray] = new GraphViewData(indexForValueArray+1, daysCombined.getHours());
 			
 			indexForValueArray++;
 		}
 		
 		GraphViewSeries dataSeries = new GraphViewSeries(data);
 
 		// graph with dynamically generated horizontal and vertical labels
 		GraphView graphView = new BarGraphView(
 					this // context
 					, getString(R.string.sleep_per_day) // heading
 			);
 		
 		// custom static labels
 		graphView.addSeries(dataSeries); // data
 		graphView.setManualYAxisBounds(24, 0);
 		graphView.setScrollable(true);		
 		
 		if( numberOfDays > 30 ) {
 			graphView.setViewPort(numberOfDays-30, 30);
 		}
 		
 		return graphView;
 	}
 	
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 		String item = (String)mStatsChooser.getSelectedItem();
 		if( item.equalsIgnoreCase(getString(R.string.milk_stats_str)) ) {
 			mGraphLayout.removeAllViews();
 			mGraphLayout.addView(generateMilkDataGraphView());
 		}
 		else if( item.equalsIgnoreCase(getString(R.string.sleep_stats_str)) ) {
 			mGraphLayout.removeAllViews();
 			mGraphLayout.addView(generateSleepGraphView());
 		}
 		
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
