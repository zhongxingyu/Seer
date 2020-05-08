 package com.example.bato;
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.SeriesSelection;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYValueSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.app.AlertDialog.Builder;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Graph extends Fragment
 {
 	Context mContext;
 	private CalendarDbAdapter mDbHelper;
 	long Day;
 	long Year;
 	double Hour;
 	int adapter_minutes;
     ArrayList<Double> minutes = new ArrayList<Double>();
     ArrayList<Integer> mood = new ArrayList<Integer>();
     Calendar cal = Calendar.getInstance();
     long Add;
     GraphicalView chartView;
     LinearLayout layout;
     Map<Double, Integer> mMap;
     TextView thoughts;
     TextView event;
     View events;
     double mapping;
     
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	        Bundle savedInstanceState)
 	{
 		super.onCreateView(inflater, container, savedInstanceState);
 		mContext = this.getActivity();
 	    mDbHelper=new CalendarDbAdapter(mContext);
 	    mDbHelper.open();
 	    View view = inflater.inflate(R.layout.activity_graph, container, false);
 
 		Button next = (Button) view.findViewById(R.id.next);
 		Button previous = (Button) view.findViewById(R.id.previous);
 	    Day = cal.get(Calendar.DAY_OF_YEAR);
 	    Year = cal.get(Calendar.YEAR);
 		layout = (LinearLayout) view.findViewById(R.id.graph);	
 
 	    next.setOnClickListener(new OnClickListener()
 	    {
 
 			@Override
 			public void onClick(View arg0) 
 			{
 				Day++;
 				if (Day > 365)
 				{
 					Day = 0;
 					Year++;
 				}
 				layout.removeView(chartView);
 				chartView = generate(Day, Year);
 		    	layout.addView(chartView);
 
 			}
 	    });
 	    previous.setOnClickListener(new OnClickListener()
 	    {
 
 			public void onClick(View arg0) 
 			{
 		    	Day--;
 				if (Day < 0)
 				{
 					Day = 0;
 					Year--;
 				}
 				layout.removeView(chartView);
 		    	chartView = generate(Day, Year);
 		    	layout.addView(chartView);
 			}
 	    	
 	    });
 	    chartView = generate(Day, Year);
 		layout.addView(chartView);
 
 
 	    return view;
 
 		}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		mDbHelper.close();
 	}
 	
 	public GraphicalView generate(final long Day, final long Year)
 	{
 	    Cursor calendar = mDbHelper.fetchDay(Year, Day);
 	    mMap = new HashMap<Double, Integer>();
 	    Calendar cal = Calendar.getInstance();  
 	    cal.set(Calendar.YEAR, (int)Year);  
 	    cal.set(Calendar.DAY_OF_YEAR, (int)Day);  
 	    Log.e("Day is",""+Day);
 	    Date date = cal.getTime();  
 	    String sDate = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);  
 	    minutes.clear();
 	    mood.clear();
 	    	while (calendar.moveToNext())
 	    	{
 	    		Hour = (double)((double) (calendar.getInt(calendar.getColumnIndexOrThrow(CalendarDbAdapter.COLUMN_NAME_MINUTES)))/60);
 	    		mMap.put(Hour, calendar.getInt(calendar.getColumnIndexOrThrow(CalendarDbAdapter.COLUMN_NAME_MINUTES)));
 	    		minutes.add(Hour);
 	    		mood.add(calendar.getInt(calendar.getColumnIndexOrThrow(CalendarDbAdapter.COLUMN_NAME_FEELING)));
 	    	}
 	     
 	    
 	    if (! calendar.moveToFirst())
 	    {
 	    	Toast.makeText(mContext, "No events this day!", Toast.LENGTH_SHORT).show();
 	    }
 	    
 	    else
 	    {
 			SharedPreferences prefs = getActivity().getSharedPreferences(
 				      "com.example.app", Context.MODE_PRIVATE);
 			if (prefs.getBoolean("Graph", true) == true)
 			{
 			AlertDialog.Builder builder = new Builder(getActivity());
 			builder.setTitle("Note");
 			builder.setMessage("Click each point to see how what you were doing and what you were thinking are related to how you were feeling");		
 			builder.setPositiveButton(android.R.string.ok, null);
 			builder.create();
 			builder.show();
 			prefs.edit().putBoolean("Graph", false).commit();
 			}
 
 	    }
 		
 		XYValueSeries series = new XYValueSeries("Mood by Time"); 
 		if (minutes.size() > 0)
 		{
 			for( int i = 0; i < minutes.size(); i++)
 			{
 				series.add(minutes.get(i), mood.get(i));
 			}
 		}
 		
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		dataset.addSeries(series);
 		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph
 		XYSeriesRenderer renderer = new XYSeriesRenderer();// This will be used to customize line 1
 		mRenderer.addSeriesRenderer(renderer);
 		double[] limits = new double [] {1, 24, 0, 7};
 		mRenderer.setPanLimits(limits);
 		mRenderer.setPanEnabled(true, false);
 
 		for (int i = 1; i < 24; i++)
 		{
 			if (i == 1)
 			{
 				mRenderer.addXTextLabel(i, "12 AM");
 
 			}
 			if ( i== 4 || i == 7 || i == 10)
 			{
 				mRenderer.addXTextLabel(i, ""+((i-1)+" AM"));
 			}
 			
 			if (i == 13 )
 			{
 				mRenderer.addXTextLabel(i, ""+(12 +" PM"));
 			}
 			
 			if (i == 16 || i == 19 || i == 22)
 			{
 				mRenderer.addXTextLabel(i, ""+((i -13 ) +" PM"));
 			}
 			
 			
 		}
 		
 		for (int i = 0; i < 7; i++)
 		{
 			if (i == 0)
 			{
 			mRenderer.addYTextLabel(i, "Terrible");
 			}
 			
 			if (i == 3)
 			{
 				mRenderer.addYTextLabel(i, "Neutral");
 			}
 			
 			if (i == 6)
 			{
 				mRenderer.addYTextLabel(i, "Fantastic");
 			}
 			
 		}
 		
 		mRenderer.setXLabels(0);
 		mRenderer.setYLabels(0);
 		mRenderer.setAxisTitleTextSize(20);
 		mRenderer.setLabelsTextSize(10);
 		mRenderer.setYLabelsAngle(310);
 		mRenderer.setAxesColor(Color.CYAN);
 		mRenderer.setXLabelsColor(Color.RED);
 		mRenderer.setYLabelsColor(0, Color.RED);
 		mRenderer.setYAxisMax(7);
 		mRenderer.setYAxisMin(0);
 		mRenderer.setXAxisMax(23);
		mRenderer.setPointSize(10);
 		mRenderer.setChartTitle(sDate);
 		mRenderer.setClickEnabled(true);
 		mRenderer.setXLabelsAngle(45);
 		mRenderer.setLabelsTextSize(18);
 		mRenderer.setXLabelsPadding(30);
 
 		// Customization time for line 1!
 		renderer.setColor(Color.GRAY);
 		renderer.setLineWidth(3);
 		renderer.setPointStyle(PointStyle.CIRCLE);
 		renderer.setFillPoints(true);
 		// Customization time for line 2!
 
 		final GraphicalView chartView;
 		chartView = ChartFactory.getLineChartView(mContext, dataset, mRenderer);
 	    chartView.setOnClickListener(new View.OnClickListener()
 	    {
 
 			@Override
 			public void onClick(View arg0) {
 				SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
 				if (seriesSelection != null)
 				{
 					
 				  mapping = seriesSelection.getXValue();
 				  if (mMap.get(mapping) != null)
 				  {
 				  adapter_minutes = mMap.get(mapping);
 			      Cursor fetchThoughtActivity = mDbHelper.fetchCalendar(Year, Day, adapter_minutes);
 				  if (fetchThoughtActivity.moveToFirst())
 				  {
 				  String thought = fetchThoughtActivity.getString(fetchThoughtActivity.getColumnIndexOrThrow(CalendarDbAdapter.COLUMN_NAME_THOUGHT));
 				  String activity = fetchThoughtActivity.getString(fetchThoughtActivity.getColumnIndexOrThrow(CalendarDbAdapter.COLUMN_NAME_ACTIVITY));
 				  AlertDialog.Builder builder = new Builder(mContext);
 				  events = getActivity().getLayoutInflater().inflate(R.layout.dialog_mood, null);
 				  builder.setView(events);
 				  builder.setTitle("Thought, Activity, and Mood");
 				  thoughts = (TextView) events.findViewById(R.id.thought);
 				  event = (TextView) events.findViewById(R.id.activity);
 				  thoughts.setText(thought);
 				  event.setText(activity);
 				  builder.setPositiveButton(android.R.string.ok, null);
 				  builder.create();
 				  builder.show();
 				  }
 
 				  }
 				}
 			}
 	    	
 	    });
 		return chartView;
 		}
 	
 	
 }
 
