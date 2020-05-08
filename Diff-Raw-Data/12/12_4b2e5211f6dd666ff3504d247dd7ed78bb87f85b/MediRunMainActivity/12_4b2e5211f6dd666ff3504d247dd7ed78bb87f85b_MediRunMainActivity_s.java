 package fun.app.medirun;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.SortedSet;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.chart.PointStyle;
 import org.achartengine.model.TimeSeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.media.AudioRecord.OnRecordPositionUpdateListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.NavUtils;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewStub.OnInflateListener;
 import android.webkit.WebView.FindListener;
 import android.widget.Button;
 import android.widget.CalendarView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.CalendarView.OnDateChangeListener;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.Toast;
 
 // Difference between Activity and FragmentActivity:
 // http://stackoverflow.com/questions/10477997/difference-between-activity-and-fragmentactivity
 
 public class MediRunMainActivity extends FragmentActivity {
 
 	public static final boolean DEBUG_FLAG = false;
 	public final static String YEAR = "fun.app.medirun.YEAR";
 	public final static String MONTH = "fun.app.medirun.MONTH";
 	public final static String DAY = "fun.app.medirun.DAY";
 	public final static String MEDIMAP = "fun.app.medirun.MEDIMAP";
 	public final static String CURRENT_MEDI_DATE = "fun.app.medirun.CURRENT_MEDI_DATE";
 	public final static String CURRENT_MEDI_MINS = "fun.app.medirun.CURRENT_MEDI_MINS";
 	public final static String CURRENT_PRAN_MINS = "fun.app.medirun.CURRENT_PRAN_MINS";
 	
 	public final static String EXISTING_MEDIMINS = "fun.app.medirun.EXISTING_MEDMINS";
 	public final static String EXISTING_PRANMINS = "fun.app.medirun.EXISTING_PRANMINS";
 	public final static String EXISTING_RUNMILES = "fun.app.medirun.EXISTING_RUNMILES";
 
 
 	public final static String CURRENT_RUN_DATE = "fun.app.medirun.CURRENT_RUN_DATE";
 	public final static String CURRENT_RUN_MILES = "fun.app.medirun.CURRENT_RUN_MILES";
 	
 
 	public final static String logTag = "MediRunMainActivity";
 	private Date currentMinMediDate;
 	private Date currentMaxMediDate;
 
 
 	private MediRunDataStore mediRunStore;
 	private static final int MEDI_ACTIVITY = 1;
 	private static final int RUN_ACTIVITY = 2;
 	private static final double TEXT_SIZE_FOR_CHART = 18.0;
 	private static final double LINE_WIDTH_FOR_CHART = 4.0; 
 	private static final double POINT_SIZE_FOR_CHART = 7.0;
 
 	private GraphicalView mMediChart;
 	private GraphicalView mRunChart;
 
 	private XYMultipleSeriesDataset mMediDataset = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer mMediRenderer = new XYMultipleSeriesRenderer();
 	private TimeSeries mCurrentMediSeries;
 	private XYSeriesRenderer mCurrentMediRenderer;
 	private XYSeriesRenderer mCurrentPranRenderer;
 	
 	private TimeSeries mCurrentPranSeries;
 
 	private XYMultipleSeriesDataset mRunDataset = new XYMultipleSeriesDataset();
 	private XYMultipleSeriesRenderer mRunRenderer = new XYMultipleSeriesRenderer();
 	private TimeSeries mCurrentRunSeries;
 	private XYSeriesRenderer mCurrentRunRenderer;
 	
 	private static final boolean disableClearAllData = true;
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
 	 * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
 	 * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
 	 * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
     // Intialization methods for each chart - creates
 	// new data sets; 
 	private void initMediChart() {
 		mCurrentMediSeries = new TimeSeries("Meditation minutes");
 		mMediDataset.addSeries(mCurrentMediSeries);
 		mCurrentPranSeries = new TimeSeries("Pranayama minutes");
 		mMediDataset.addSeries(mCurrentPranSeries);
 		mCurrentMediRenderer = new XYSeriesRenderer();
 		mCurrentPranRenderer = new XYSeriesRenderer();
 		mMediRenderer.addSeriesRenderer(mCurrentMediRenderer);
 		mMediRenderer.addSeriesRenderer(mCurrentPranRenderer);
 		//setChartProperties(mMediRenderer, mCurrentMediRenderer, null, null, new String("Meditation Mins"), Color.RED, PointStyle.DIAMOND);
 	}
 
 	private void initRunChart() {
 		mCurrentRunSeries = new TimeSeries("Run Miles");
 		mRunDataset.addSeries(mCurrentRunSeries);
 		mCurrentRunRenderer = new XYSeriesRenderer();
 		mRunRenderer.addSeriesRenderer(mCurrentRunRenderer);
		mRunRenderer.setXLabels(0);
 		//setChartProperties(mRunRenderer, mCurrentRunRenderer, null, null, new String("Run Miles"), Color.GREEN, PointStyle.CIRCLE);	
 	}
 	
 	private void setChartProperties(XYMultipleSeriesRenderer parentR, XYSeriesRenderer currentR, Date minDate, Date maxDate, String yLabel, int color, PointStyle pointStyle) {
 		if (minDate != null)
 		  parentR.setXAxisMin(minDate.getTime());
 		if (maxDate != null)
 		  parentR.setXAxisMax(maxDate.getTime());
 		parentR.setYAxisMin(0.0);
 		currentR.setColor(color);
 		currentR.setPointStyle(pointStyle);
 		currentR.setFillPoints(true);
 		
 		parentR.setShowGrid(true);
 		parentR.setPointSize((float)POINT_SIZE_FOR_CHART);
 		parentR.setLabelsTextSize((float)TEXT_SIZE_FOR_CHART);
 		parentR.setAxisTitleTextSize((float)TEXT_SIZE_FOR_CHART);
 		parentR.setLegendTextSize((float)TEXT_SIZE_FOR_CHART);
 		parentR.setYTitle(yLabel);
 		//parentR.setYLabelsAlign(Align.CENTER);
 		//parentR.setXLabelsAlign(Align.CENTER);
 		currentR.setLineWidth((float)LINE_WIDTH_FOR_CHART);
 		currentR.setChartValuesTextAlign(Align.CENTER);
 		currentR.setChartValuesTextSize((float)TEXT_SIZE_FOR_CHART);
 	}
 
 	private boolean addMediData() {
 
 		SortedSet<MediRunDataStore.DateIntPair> oList =
 				mediRunStore.getMediDataInOrder();
 		Iterator<MediRunDataStore.DateIntPair> it = oList.iterator();
 		Date minDate=null, maxDate=null;
 
 		if (oList.size() == 0)
 			return false;
 		mCurrentMediSeries.clear();
 		while (it.hasNext())
 		{
 			Entry<Date, Integer> pair = it.next();
 			if (minDate == null)
 				minDate = pair.getKey();
 			else if (pair.getKey().getTime() < minDate.getTime())
 				minDate = pair.getKey();
 
 
 			Log.i(logTag, "Mx:" + pair.getKey().toString() + " My:" + pair.getValue().doubleValue());
 			mCurrentMediSeries.add(pair.getKey(), pair.getValue().doubleValue());
 
 
 			if (maxDate == null)
 				maxDate = pair.getKey();
 			else if (maxDate.getTime() < pair.getKey().getTime())
 				maxDate = pair.getKey();
 		}
 		if (minDate == null || maxDate == null)
 			return false;
 		Log.i(logTag, "minDate:" + minDate.toString() + ", maxDate:" + maxDate.toString());
 		currentMinMediDate = minDate;
 		currentMaxMediDate = maxDate;
 		setChartProperties(mMediRenderer, mCurrentMediRenderer, minDate, maxDate, new String("Meditation Mins"), Color.RED, PointStyle.DIAMOND);
 		return true;
 
 	}
 	
 	private boolean addPranData() {
 
 		SortedSet<MediRunDataStore.DateIntPair> oList =
 				mediRunStore.getPranDataInOrder();
 		Iterator<MediRunDataStore.DateIntPair> it = oList.iterator();
 		Date minDate=null, maxDate=null;
 
 		if (oList.size() == 0)
 			return false;
 		mCurrentPranSeries.clear();
 		while (it.hasNext())
 		{
 			Entry<Date, Integer> pair = it.next();
 			if (minDate == null)
 				minDate = pair.getKey();
 			else if (pair.getKey().getTime() < minDate.getTime())
 				minDate = pair.getKey();
 
 
 			Log.i(logTag, "Mx:" + pair.getKey().toString() + " My:" + pair.getValue().doubleValue());
 			mCurrentPranSeries.add(pair.getKey(), pair.getValue().doubleValue());
 
 
 			if (maxDate == null)
 				maxDate = pair.getKey();
 			else if (maxDate.getTime() < pair.getKey().getTime())
 				maxDate = pair.getKey();
 		}
 		if (minDate == null || maxDate == null)
 			return false;
 		Log.i(logTag, "minDate:" + minDate.toString() + ", maxDate:" + maxDate.toString());
 		if (currentMinMediDate != null && currentMinMediDate.before(minDate))
 			minDate = currentMinMediDate;
 		if (currentMaxMediDate != null && currentMaxMediDate.after(maxDate))
 			maxDate = currentMaxMediDate;
 		setChartProperties(mMediRenderer, mCurrentPranRenderer, minDate, maxDate, new String("Pranayam Mins"), Color.BLUE, PointStyle.CIRCLE);
 		return true;
 
 	}
 
 	private String getDateAsStr(Date d)
 	{
 		String strDate = DateFormat.format("MMM dd, yyyy", d).toString();
 		return strDate;
 	}
 	
 	private boolean addRunData() {
 
 		SortedSet<MediRunDataStore.DateDoublePair> oList =
 				mediRunStore.getRunDataInOrder();
 		//int size = oList.size();
 		//int chunk = size/5;
 		
 		Iterator<MediRunDataStore.DateDoublePair> it = oList.iterator();
 		Date minDate=null, maxDate=null;
 		double maxMiles = -1.0;
 
 		if (oList.size() == 0)
 			return false;
 		mCurrentRunSeries.clear();
 		int cnt = 0;
 		while (it.hasNext())
 		{
 			Entry<Date, Double> pair = it.next();
 
 			//if (chunk!= 0 && cnt%chunk == 0)
 			//	mRunRenderer.addXTextLabel(pair.getKey().getTime(), getDateAsStr(pair.getKey()));
 		
 			if (minDate == null)
 				minDate = pair.getKey();
 			else if (pair.getKey().getTime() < minDate.getTime())
 				minDate = pair.getKey();
 
 			if (maxMiles < pair.getValue().doubleValue())
 				maxMiles = pair.getValue().doubleValue();
 
 			Log.i(logTag, "Rx:" + pair.getKey().toString() + " Ry:" + pair.getValue().doubleValue());
 			mCurrentRunSeries.add(pair.getKey(), pair.getValue().doubleValue());
 
 
 			if (maxDate == null)
 				maxDate = pair.getKey();
 			else if (maxDate.getTime() < pair.getKey().getTime())
 				maxDate = pair.getKey();
 		}
 		if (minDate == null || maxDate == null)
 			return false;
 		
 		setChartProperties(mRunRenderer, mCurrentRunRenderer, minDate, maxDate, new String("Run Miles"), Color.GREEN, PointStyle.CIRCLE);		
 		return true;
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_medi_run_main);
 
 		// Create the adapter that will return a fragment for each of the three primary sections
 		// of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		if (mediRunStore == null)	
 		{
 			mediRunStore = MediRunDataStore.getInstance();
 			mediRunStore.bootUpMediData(this);
 			mediRunStore.bootUpPranData(this);
 			mediRunStore.bootUpRunData(this);
 		}
 		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int arg0) {
 				Log.i(logTag, "onPageSelected:" + String.valueOf(arg0));
 				if (arg0 == 0) {
 					refreshOrCreateMediChart();
 					refreshOrCreateRunChart();
 				}
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				Log.i(logTag, "onPageScrolled:" + String.valueOf(arg0) + "," + 
 						String.valueOf(arg1) + "," + String.valueOf(arg2));
 
 			}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				Log.i(logTag, "onPageScrollStateChanged:" + String.valueOf(arg0));
 
 			}
 		});
 		mViewPager.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
                 Log.i(logTag, "mViewPager: Got click!");
 				refreshOrCreateMediChart();
 				refreshOrCreateRunChart();
 			}
 		});
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	private void updateMediChart(LinearLayout mcl) {
 		if (mMediChart != null)
 			mcl.removeView(mMediChart);
 
 		boolean hasData = addMediData();
 		addPranData();
 		mMediChart = ChartFactory.getTimeChartView(
 				this,
 				mMediDataset, 
 				mMediRenderer, "MMM dd, yyyy");
 				//null);
 		mcl.addView(mMediChart);
 	}
 
 	private void updateRunChart(LinearLayout rcl) {
 		if (mRunChart != null)
 			rcl.removeView(mRunChart);
 		boolean hasData = addRunData();
 
 		mRunChart = ChartFactory.getTimeChartView(
 				this,
 				mRunDataset, 
 				mRunRenderer, "MMM dd, yyyy");
 		
 		/*mRunChart = ChartFactory.getBarChartView(
 				this,
 				mRunDataset, 
 				mRunRenderer, Type.STACKED);
 				//null);*/
 		rcl.addView(mRunChart);
 		
 	}
 
 	private void refreshOrCreateMediChart() {
 
 		LinearLayout mediChartLayout = (LinearLayout)findViewById(R.id.medichart);
 		if (mediChartLayout != null) {
 			if (mMediChart == null) {
 				Log.i(logTag, "mChart is null");
 				initMediChart();
 				updateMediChart(mediChartLayout);
 			} else {
 				updateMediChart(mediChartLayout);
 				mMediChart.repaint();
 			}
 		}
 		mediChartLayout.invalidateChild(mMediChart, null);
 		mediChartLayout.invalidate();
 		Log.i(logTag, "Invalidate called!");
 	}
 
 
 	private void refreshOrCreateRunChart() {
 
 		LinearLayout runChartLayout = (LinearLayout)findViewById(R.id.runchart);
 		if (runChartLayout != null) {
 			if (mRunChart == null) {
 				Log.i(logTag, "mChart is null");
 				initRunChart();
 				updateRunChart(runChartLayout);
 			} else {
 				updateRunChart(runChartLayout);
 				mRunChart.repaint();
 			}
 		}
 		runChartLayout.invalidateChild(mRunChart, null);
 		runChartLayout.invalidate();
 		Log.i(logTag, "Invalidate called!");
 		return;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_medi_run_main, menu);
 		Log.i(logTag, "Inflated activity_graph!");
 
 		refreshOrCreateMediChart();
 		refreshOrCreateRunChart();
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.clearData:
 			Log.i(logTag, "CLEARING ALL DATA...");
 			if (disableClearAllData == false)
    			  mediRunStore.clearAllData();
 			Log.i(logTag, "CLEARED ALL DATA!");
 			refreshOrCreateMediChart();
 			refreshOrCreateRunChart();
 			break;
 		case R.id.emailId:
 			break;
 		case R.id.backupData:
 			String email = new String("");
 			String subject = new String("[MediRun Data]");
 			String emailText = new String("Attached.");
 			View rLayout = findViewById(R.id.graphLayout);
 			mediRunStore.prepareDataForEmail(this, rLayout);
 			mediRunStore.email(this, email, null, subject, emailText);
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 			
 		}
 		return true;
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 	 * sections of the app.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter  {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int i) {
 			Fragment fragment = new DummySectionFragment();
 			Bundle args = new Bundle();
 			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
 			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0: return getString(R.string.title_section1).toUpperCase();
 			case 1: return getString(R.string.title_section2).toUpperCase();
 			case 2: return getString(R.string.title_section3).toUpperCase();
 			}
 			return null;
 		}
 
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
 	 */
 	public class DummySectionFragment extends Fragment  {
 		public DummySectionFragment() {
 		}
 
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 
 			// Get section number
 			Bundle args = getArguments();
 			int sectionNum = args.getInt(ARG_SECTION_NUMBER);
 
 			// Section 2: Meditation
 			// Section 3: Running
 			if (sectionNum != 1) {
 
 				// Create calendar view, can click on it, and it listens to date change
 				CalendarView calView = new CalendarView(getActivity());
 				calView.setClickable(true);
 
 				if (sectionNum == 2) {
 					calView.setOnDateChangeListener(new OnDateChangeListener() {
 
 						@Override
 						public void onSelectedDayChange(CalendarView view, int year, int month,
 								int dayOfMonth) {
 							if (MediRunMainActivity.DEBUG_FLAG)
 								Toast.makeText(getApplicationContext(), ""+dayOfMonth, 0).show();// TODO Auto-generated method stub
 
 							Intent intent = new Intent(getBaseContext(), MediActivity.class);
 							intent.putExtra(YEAR, String.valueOf(year));
 							intent.putExtra(MONTH, String.valueOf(month));
 							intent.putExtra(DAY, String.valueOf(dayOfMonth));
 							int mins = mediRunStore.getMediMinsForDate(year, month, dayOfMonth);
 							if (mins != 0)
 								intent.putExtra(EXISTING_MEDIMINS, String.valueOf(mins));
 							int pranMins = mediRunStore.getPranMinsForDate(year, month, dayOfMonth);
 							if (pranMins != 0)
 								intent.putExtra(EXISTING_PRANMINS, String.valueOf(pranMins));
 							getActivity().startActivityForResult(intent, MEDI_ACTIVITY);
 
 						}
 					});
 				} else {
 					calView.setOnDateChangeListener(new OnDateChangeListener() {
 
 						@Override
 						public void onSelectedDayChange(CalendarView view, int year, int month,
 								int dayOfMonth) {
 							if (MediRunMainActivity.DEBUG_FLAG)
 								Toast.makeText(getApplicationContext(), ""+dayOfMonth, 0).show();// TODO Auto-generated method stub
 
 							Intent intent = new Intent(getBaseContext(), RunActivity.class);
 							intent.putExtra(YEAR, String.valueOf(year));
 							intent.putExtra(MONTH, String.valueOf(month));
 							intent.putExtra(DAY, String.valueOf(dayOfMonth));
 							double miles = mediRunStore.getRunMilesForDate(year, month, dayOfMonth);
 							if (miles != 0)
 								intent.putExtra(EXISTING_RUNMILES, String.valueOf(miles));
 							getActivity().startActivityForResult(intent, RUN_ACTIVITY);
 
 						}
 					});
 
 				}
 				return calView;
 			} // Section 1: Trends
 			else {
 				View v = inflater.inflate(R.layout.activity_graph, container, false);
 				v.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Log.i(logTag, "Got!");
 						// TODO Auto-generated method stub
 						if (true) {
 							refreshOrCreateMediChart();
 							refreshOrCreateRunChart();
 						}
 					}
 				});
 				return v;
 			}
 		}
 
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.i(logTag, "onActivityResult called: (Is resultCode RESULT_OK:" 
 				+ String.valueOf(resultCode == RESULT_OK) + "), (Request code:" +
 				String.valueOf(requestCode) + ")");
 
 		if (resultCode == RESULT_OK && requestCode == MEDI_ACTIVITY) {
 			Date currentDate = (Date)data.getSerializableExtra(CURRENT_MEDI_DATE);
 			String s1 = data.getStringExtra(CURRENT_MEDI_MINS);
 			String s2 = data.getStringExtra(CURRENT_PRAN_MINS);
 			
 			Integer currentMediMins = new Integer(s1);
 			Integer currentPranMins = new Integer(s2);
 			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() + " CurrentMins:" +
 					s1);
 			mediRunStore.appendMediData(currentDate, currentMediMins, currentPranMins);
 			//refreshOrCreateMediChart();
 		} else if (resultCode == RESULT_OK && requestCode == RUN_ACTIVITY) {
 			Date currentDate = (Date)data.getSerializableExtra(CURRENT_RUN_DATE);
 			String s = data.getStringExtra(CURRENT_RUN_MILES);
 			Double currentMiles = new Double(s);
 			Log.i(logTag, "onActivityResult: Got current date " + currentDate.toString() + " CurrentMiles:" +
 					s);
 			mediRunStore.appendRunData(currentDate, currentMiles);
 			//refreshOrCreateRunChart();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		Log.i(logTag, "onPause called");
 		//mediRunStore.flushMediRunData();
 		super.onPause();
 	}	
 }
