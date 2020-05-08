 package com.bryanmarty.greenbutton;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.concurrent.Future;
 
 import com.bryanmarty.greenbutton.data.DataPoint;
 import com.bryanmarty.greenbutton.data.IntervalReading;
 import com.bryanmarty.greenbutton.database.TrackManager;
 import com.jjoe64.graphview.BarGraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewSeries;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.AdapterView;
 
 public class GreenButtonGraphActivity extends Activity {
 	
 	LinkedList<IntervalReading> cached_ = null;
 	
 	int actualYear = 2011;
 	int yearIndex = 0;
 	int monthIndex= 0;
 	boolean isDailySelected = false;
 	boolean isMonthlySelected = false;
 	public String[] monthList = {};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.graph);
 		monthList = getResources().getStringArray(R.array.strMonths);
 		
 		Calendar cal = Calendar.getInstance();
 		Spinner monthSpin = (Spinner) findViewById(R.id.cbMonths);
 		Spinner yearSpin = (Spinner) findViewById(R.id.cbYears);
 		
 		monthIndex = cal.get(Calendar.MONTH);
 		monthSpin.setSelection(monthIndex);
 		int year = cal.get(Calendar.YEAR);	
 	
 		monthSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 	
 		    @Override
 		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 		       	monthIndex = position;
 		       	regraph();
 		    };
 	
 		    @Override
 		    public void onNothingSelected(AdapterView<?> parentView) {
 		        // your code here
 		    }
 		    
 		});
 		
 		yearSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			
 		    @Override
 		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 		       	yearIndex = position;
 		       	actualYear = yearIndex + 2011;
 		       	regraph();
 		    };
 	
 		    @Override
 		    public void onNothingSelected(AdapterView<?> parentView) {
 		        // your code here
 		    }
 		    
 		});
 
 	}
 	
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 	}
 	
 	protected void regraph() {
 		RadioButton daily = (RadioButton) findViewById(R.id.rdDaily);
 		if(daily.isChecked()) {
 			isDailySelected = true;
 			isMonthlySelected = false;
 			graphDaily(null);
 		}
 		
 		RadioButton monthly = (RadioButton) findViewById(R.id.rdMonthly);
 		if(monthly.isChecked()) {
 			isDailySelected = false;
 			isMonthlySelected = true;
 			graphMonthly(null);
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		graphDaily(null);
 		super.onStart();
 	}
 	
 	public void graphDaily(View v) {
 		isDailySelected = true;
 		isMonthlySelected = false;
 		showGraph(prepareDaily(monthIndex,actualYear));
 	}
 	
 	public void graphMonthly(View v) {
 		isDailySelected = false;
 		isMonthlySelected = true;
 		showGraph(prepareMonthly(actualYear));
 	}
 
 	public void prepareDataDaily(int month,int year) {
 		LinkedList<IntervalReading> result = new LinkedList<IntervalReading>();
 		
 		//Pull all readings from the beginning of the month
 		Calendar cal = Calendar.getInstance();
 		if(month > -1 && month < 12) {
 			cal.set(Calendar.MONTH, month);			
 		}
 		cal.set(Calendar.DATE, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.set(Calendar.YEAR, year);
 		Date startDate = cal.getTime();
 		
 		cal.set(Calendar.MONTH, (cal.get(Calendar.MONTH)+1)%12);
 		if(month == 11) {
 			cal.set(Calendar.YEAR, year+1);
 		}
 		Date endDate = cal.getTime();
 		
 		Future<LinkedList<IntervalReading>> future = TrackManager.getReadingsBetween(startDate, endDate);
 		try {
 			result = future.get();
 			cached_ = result;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	protected void prepareDataMonthly(int year) {
 		LinkedList<IntervalReading> result = new LinkedList<IntervalReading>();
 		
 		//Pull all readings from the beginning of the month
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.MONTH, 1);
 		cal.set(Calendar.DATE, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.set(Calendar.YEAR, year);
 		
 		Date startDate = cal.getTime();
 		
 		cal.set(Calendar.YEAR, year+1);
 		Date endDate = cal.getTime();
 		
 		Future<LinkedList<IntervalReading>> future = TrackManager.getReadingsBetween(startDate,endDate);
 		try {
 			result = future.get();
 			cached_ = result;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected TreeMap<Integer,DataPoint> prepareDaily(int month, int year) {
 		prepareDataDaily(month, year);
 		TreeMap<Integer,DataPoint> map = new TreeMap<Integer,DataPoint>();
 		for(IntervalReading r : cached_) {
 			Date startDate = r.getStartTime();
 			int day = startDate.getDate();
 			DataPoint d = map.get(day);
 			if(d != null) {
 				d.cost += r.getCost();
 				d.value += r.getValue();
 			} else {
 				d = new DataPoint();
 				d.x = day;
 				d.cost = r.getCost();
 				d.value = r.getValue();
 				map.put(day, d);
 			}
 		}
 		return map;
 	}
 	
 	protected TreeMap<Integer,DataPoint> prepareMonthly(int year) {
 		prepareDataMonthly(year);
 		TreeMap<Integer,DataPoint> map = new TreeMap<Integer,DataPoint>();
 		for (IntervalReading r: cached_) {
 			Date startDate = r.getStartTime();
 			int month = startDate.getMonth();
 			DataPoint d = map.get(month);
 			if( d != null) {
 				d.cost += r.getCost();
 				d.value += r.getValue();
 			} else {
 				d = new DataPoint();
 				d.x = month;
 				d.cost = r.getCost();
 				d.value = r.getValue();
 				map.put(month, d);
 			}
 		}
 		return map;
 	}
 	
 	protected void showGraph(TreeMap<Integer,DataPoint> data) {
 		LinearLayout layout = (LinearLayout) findViewById(R.id.graphLayout);
 		layout.removeAllViews();
 		
 		if(data.size() <= 0) {
 			TextView none = new TextView(this);
 			none.setText("No Records Available");
 			none.setTextColor(Color.WHITE);
 			none.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 		
 			layout.addView(none);
 			return;
 		}
 		
 		GraphViewData[] valuePoints = new GraphViewData[data.size()];
 		GraphViewData[] costPoints = new GraphViewData[data.size()];
 		
 		TreeSet<Integer> keys = new TreeSet<Integer>(data.keySet());
 		int x = 0;
 		for(Integer key : keys) {
 			DataPoint dp = data.get(key);
 			GraphViewData pCost = new GraphViewData(dp.x,dp.cost);
 			GraphViewData pValue = new GraphViewData(dp.x,dp.value/1000L);
 			Log.i("point",dp.x + " - " + dp.value/1000L);
 			valuePoints[x] = pValue;
 			costPoints[x] = pCost;
 			x++;
 		}
 		
 		// init example series data
 		GraphViewSeries valueSeries = new GraphViewSeries("Energy Usage History", Color.WHITE, valuePoints);
 		GraphViewSeries costSeries = new GraphViewSeries(costPoints);
 		
 		GraphView graphView = new BarGraphView(this, "kWh vs. Time")  {
 			
 			@Override
 			 protected String formatLabel(double value, boolean isValueX) {  
 					if (isValueX) {  
 						if(isMonthlySelected) {
 							return monthList[(int) value];
 						} else {
 							return String.valueOf((int) value);
 						}
 					} else {  
 						// y-axis, use default formatter  
 						return super.formatLabel(value, isValueX);  
 					}  
 			 	}  
 			}; 
 	
 		if(isMonthlySelected) {
 			graphView.setViewPort(0,4);
 		}
 		graphView.setViewPort(1, 10);
 		graphView.setScrollable(true);
		
 		//graphView.addSeries(costSeries);
 		graphView.addSeries(valueSeries);
 		layout.addView(graphView);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 
 
 	
 	
 }
