 package com.t2.vas.view;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 import com.t2.chart.LineSeries;
 import com.t2.chart.NotesSeries;
 import com.t2.chart.widget.Chart;
 import com.t2.chart.widget.Chart.ChartEventListener;
 import com.t2.vas.Global;
 import com.t2.vas.GroupNotesSeriesDataAdapter;
 import com.t2.vas.GroupResultsSeriesDataAdapter;
 import com.t2.vas.R;
 import com.t2.vas.ScaleKeyAdapter;
 import com.t2.vas.ScaleResultsSeriesDataAdapter;
 import com.t2.vas.VASAnalytics;
 import com.t2.vas.activity.ABSActivity;
 import com.t2.vas.activity.NoteActivity;
 import com.t2.vas.activity.NotesDialogActivity;
 import com.t2.vas.db.DBAdapter;
 import com.t2.vas.db.tables.Group;
 import com.t2.vas.db.tables.Note;
 import com.t2.vas.db.tables.Result;
 import com.t2.vas.db.tables.Scale;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.Gallery;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewAnimator;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ResultsLayout extends LinearLayout implements ChartEventListener, OnItemSelectedListener {
 	private static final String TAG = ResultsLayout.class.getName();
 	private LayoutInflater layoutInflater;
 	private int resultsGroupBy = ScaleResultsSeriesDataAdapter.GROUPBY_DAY;
 	private LinkedHashMap<Long, ChartLayout> chartLayouts = new LinkedHashMap<Long, ChartLayout>();
 	private Gallery keyListView;
 	private ViewAnimator chartsContainer;
 	private Animation chartInAnimation;
 	private Animation chartOutAnimation;
 	private AnimationSet flashAnimation;
 	private ChartLayout groupChartLayout;
 	private ChartLayout currentChartLayout;
 	//private ScaleKeyAdapter keyListAdapter;
 	private boolean initialized = false;
 	private ArrayList<HashMap<String, Object>> keyAdapterList;
 	private ArrayList<ChartLayout> chartLayoutsList;
 
 
 	public ResultsLayout(Context context) {
 		super(context);
 	}
 
 	public ResultsLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public void init(Group activeGroup, DBAdapter db) {
         layoutInflater = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         keyListView = (Gallery)this.findViewById(R.id.list);
         chartsContainer = (ViewAnimator)this.findViewById(R.id.chartsAnimator);
         chartsContainer.setAnimateFirstView(false);
 
 //        DBAdapter db = new DBAdapter(this.getContext(), Global.Database.name, Global.Database.version);
 //        db.open();
 
         // Determine from which data to pull data.
         Calendar startTimeCal = Calendar.getInstance();
         startTimeCal.add(Calendar.MONTH, -1);
 
         Result resultTable = (Result)db.getTable("result").newInstance();
         Note noteTable = (Note)db.getTable("note").newInstance();
         long resultsEarliestTimestamp = resultTable.getEarliestTimestampSince(activeGroup._id, startTimeCal.getTimeInMillis());
         Long notesEarliestTimestamp = noteTable.getEarliestTimestampSince(startTimeCal.getTimeInMillis());
         long startTime;
 
         if(notesEarliestTimestamp == null) {
         	startTime = resultsEarliestTimestamp;
         } else {
         	startTime = (resultsEarliestTimestamp < notesEarliestTimestamp)?resultsEarliestTimestamp:notesEarliestTimestamp;
         }
         startTimeCal.setTimeInMillis(startTime);
 
 
         Log.v(TAG, "GROUP BY:"+resultsGroupBy);
         // Create the chart for each scale.
         ArrayList<Scale> scales = activeGroup.getScales();
         int scaleCount = scales.size();
         for(int i = 0; i < scaleCount ; i++) {
         	Scale s = scales.get(i);
         	LineSeries lineSeries = new LineSeries(s.max_label +" - "+ s.min_label);
 
         	lineSeries.setSeriesDataAdapter(new ScaleResultsSeriesDataAdapter(
         			db,
         			startTimeCal.getTimeInMillis(),
         			s._id,
         			resultsGroupBy,
         			Global.CHART_LABEL_DATE_FORMAT
         	));
 
         	ChartLayout chartLayout = (ChartLayout)layoutInflater.inflate(R.layout.chart_layout, null);
         	chartLayout.setScale(s);
         	chartLayout.setYMaxLabel(s.max_label);
         	chartLayout.setYMinLabel(s.min_label);
         	chartLayout.setTag((Long)s._id);
        	chartLayout.setMaxYValue(100);
 
         	// Style the colors of the lines and points.
         	setLineSeriesColor(lineSeries, i+1, scaleCount+1);
 
         	// Add the series and add the chart to the list of charts.
         	chartLayout.getChart().setChartEventListener(this);
         	chartLayout.getChart().addSeries("main", lineSeries);
         	chartLayouts.put(s._id, chartLayout);
         }
 
         // Create the chart for the group in general
         NotesSeries notesSeries = new NotesSeries("Notes");
         notesSeries.setSeriesDataAdapter(new GroupNotesSeriesDataAdapter(
         		db,
         		startTimeCal.getTimeInMillis(),
         		resultsGroupBy,
         		Global.CHART_LABEL_DATE_FORMAT
         ));
         LineSeries lineSeries = new LineSeries("");
         lineSeries.setSeriesDataAdapter(new GroupResultsSeriesDataAdapter(
         		db,
         		startTimeCal.getTimeInMillis(),
         		activeGroup._id,
         		resultsGroupBy,
         		Global.CHART_LABEL_DATE_FORMAT
         ));
         setLineSeriesColor(lineSeries, 0, scaleCount+1);
 
         View averageKey = this.findViewById(R.id.averageKey1);
         ((TextView)averageKey.findViewById(R.id.maxLabel)).setText(R.string.average_for);
         ((TextView)averageKey.findViewById(R.id.minLabel)).setText(activeGroup.title);
         ((TextView)averageKey.findViewById(R.id.maxLabel)).setTextColor(Color.WHITE);
         ((TextView)averageKey.findViewById(R.id.minLabel)).setTextColor(Color.WHITE);
         averageKey.findViewById(R.id.innerBox).setBackgroundColor(lineSeries.getFillColor());
         averageKey.findViewById(R.id.outerBox).setBackgroundColor(lineSeries.getStrokeColor());
 
         groupChartLayout = (ChartLayout)this.findViewById(R.id.groupChart);
         groupChartLayout.setYMaxLabel(activeGroup.title);
         groupChartLayout.getChart().setChartEventListener(this);
         groupChartLayout.getChart().addSeries("notes", notesSeries);
         groupChartLayout.getChart().addSeries("main", lineSeries);
         groupChartLayout.setShowLabels(false);
 		groupChartLayout.getChart().setDropShadowEnabled(false);
 		groupChartLayout.getChart().setShowYHilight(false);
         this.currentChartLayout = groupChartLayout;
 
 
         // Populate the key with items.
         chartLayoutsList = new ArrayList<ChartLayout>();
         chartLayoutsList.addAll(this.chartLayouts.values());
         ScaleKeyAdapter keyListAdapter = new ScaleKeyAdapter(this.getContext(), R.layout.key_box_adapter_list_label_right, chartLayoutsList);
         keyListView.setAdapter(keyListAdapter);
                 keyListView.setOnItemSelectedListener(this);
         keyListView.setCallbackDuringFling(false);
 
         this.initialized  = true;
         //Log.v(TAG, "END INIT");
 	}
 
 	private void setLineSeriesColor(LineSeries lineSeries, int currentIndex, int totalCount) {
 		// Style the colors of the lines and points.
     	float hue = currentIndex / (1.00f * totalCount) * 360.00f;
     	lineSeries.setFillColor(Color.HSVToColor(
     			255,
     			new float[]{
     				hue,
     				1.0f,
     				1.0f
     			}
     	));
     	lineSeries.setStrokeColor(Color.HSVToColor(
     			255,
     			new float[]{
     				hue,
     				1.0f,
     				0.25f
     			}
     	));
 
     	lineSeries.setLineFillColor(Color.HSVToColor(
     			255,
     			new float[]{
     				hue,
     				0.65f,
     				1.0f
     			}
     	));
     	lineSeries.setLineStrokeColor(Color.HSVToColor(
     			255,
     			new float[]{
     				hue,
     				0.5f,
     				0.5f
     			}
     	));
 	}
 
 	@Override
 	public boolean onChartClick(Chart c, MotionEvent event) {
 		return false;
 	}
 
 	@Override
 	public boolean onChartLongClick(Chart c, MotionEvent event) {
 		return false;
 	}
 
 
 	public void showChart(ChartLayout chartLayout, boolean animate) {
 		if(this.chartsContainer.getChildCount() > 1) {
 			this.chartsContainer.removeViewAt(0);
 		}
 
 		chartLayout.setShowLabels(true);
 		chartLayout.getChart().setDropShadowEnabled(true);
 		chartLayout.getChart().setShowYHilight(true);
 
 		ChartLayout currentChartLayout = (ChartLayout)this.chartsContainer.getCurrentView();
 		if(currentChartLayout != null) {
 			currentChartLayout.setShowLabels(false);
 			currentChartLayout.getChart().setDropShadowEnabled(false);
 			currentChartLayout.getChart().setShowYHilight(false);
 		}
 
 		ViewGroup parent = (ViewGroup)chartLayout.getParent();
 		if(parent != null) {
 			parent.removeView(chartLayout);
 		}
 
 		this.chartsContainer.addView(chartLayout);
 		this.chartsContainer.showNext();
 
 		VASAnalytics.onEvent(VASAnalytics.EVENT_SCALE_SELECTED);
 	}
 
 
 	public long[] getActiveChartTimeRange() {
 		ChartLayout chartLayout = (ChartLayout)this.chartsContainer.getCurrentView();
 
 		if(chartLayout == null) {
 			return null;
 		}
 		
 		Calendar cal = Calendar.getInstance();
 		Date tmpDate;
 		long startTimestamp = -1;
 		long endTimestamp = -1;
 
 		int seriesIndex = chartLayout.getChart().getYHilightSeriesIndex();
 		if(seriesIndex >= 0) {
 			tmpDate = (Date)chartLayout.getChart().getSeriesAt(0).getLabels().get(seriesIndex).getLabelValue();
 			cal.setTime(tmpDate);
 			startTimestamp = cal.getTimeInMillis();
 
 			if(seriesIndex+1 < chartLayout.getChart().getSeriesAt(0).getLabels().size()) {
 				tmpDate = (Date)chartLayout.getChart().getSeriesAt(0).getLabels().get(seriesIndex+1).getLabelValue();
 				cal.setTime(tmpDate);
 				endTimestamp = cal.getTimeInMillis();
 			}
 		}
 		
 		return new long[]{
 			startTimestamp,
 			endTimestamp
 		};
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		if(!this.initialized) {
 			return;
 		}
 
 		this.showChart(this.chartLayoutsList.get(arg2), true);
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		if(!this.initialized) {
 			return;
 		}
 	}
 }
