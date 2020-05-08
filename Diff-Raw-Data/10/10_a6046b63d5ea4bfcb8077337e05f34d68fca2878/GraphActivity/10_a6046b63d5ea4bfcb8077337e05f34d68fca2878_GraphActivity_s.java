 /*
  * Copyright (C) 2007 The Android Open Source Project
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package net.redgeek.android.eventrend.graph;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import net.redgeek.android.eventrend.EvenTrendActivity;
 import net.redgeek.android.eventrend.Preferences;
 import net.redgeek.android.eventrend.R;
 import net.redgeek.android.eventrend.backgroundtasks.CorrelateTask;
 import net.redgeek.android.eventrend.calendar.CalendarActivity;
 import net.redgeek.android.eventrend.db.CategoryDbTable;
 import net.redgeek.android.eventrend.graph.plugins.LinearMatrixCorrelator;
 import net.redgeek.android.eventrend.primitives.TimeSeries;
 import net.redgeek.android.eventrend.primitives.TimeSeriesCollector;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.DynamicSpinner;
 import net.redgeek.android.eventrend.util.GUITaskQueue;
 import net.redgeek.android.eventrend.util.ProgressIndicator;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Debug;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.CompoundButton;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.ZoomControls;
 
 public class GraphActivity extends EvenTrendActivity {
   // Menu items
   private static final int MENU_GRAPH_FILTER_ID = Menu.FIRST;
   private static final int MENU_GRAPH_CORRELATE_ID = Menu.FIRST + 1;
   private static final int MENU_GRAPH_SNAP_TO_PERIOD_ID = Menu.FIRST + 2;
   private static final int MENU_CALENDAR_VIEW_ID = Menu.FIRST + 3;
   private static final int MENU_GRAPH_PREFS_ID = Menu.FIRST + 4;
 
   // Dialogs
   private static final int DIALOG_GRAPH_FILTER = 0;
   private static final int DIALOG_GRAPH_CORRELATE = 1;
 
   // UI elements
   private ToggleButton mShowTrendsToggle;
   private ToggleButton mShowGoalsToggle;
   private ToggleButton mShowMarkersToggle;
   private ZoomControls mZoomControls;
   private LinearLayout mGraphControls;
   private GraphView mGraphView;
   private DynamicSpinner mAggregationSpinner;
   private LinearLayout mAggregationSpinnerLayout;
   private LinearLayout mGraphPlotLayout;
   private LinearLayout mGraphZoomLayout;
   private TextView mGraphStatus;
   private GraphFilterListAdapter mGFLA;
   private Dialog mFilterDialog;
   private ProgressIndicator.Titlebar mProgress;
 
   // Special aggregation period:
   private static final String AUTO_AGGREGATION = "Automatic";
 
   // Listeners
   private Spinner.OnItemSelectedListener mAggregationSpinnerListener;
   private CompoundButton.OnCheckedChangeListener mShowTrendsToggleListener;
   private CompoundButton.OnCheckedChangeListener mShowGoalsToggleListener;
   private CompoundButton.OnCheckedChangeListener mShowMarkersToggleListener;
 
   // Private data
   private TimeSeriesCollector mTSC;
   private ArrayList<Integer> mSeriesEnabled;
   private int mHistory;
   private float mSmoothing;
   private float mSensitivity;
   private int mDecimals;
 
   // Saved across orientation changes
   private long mStartMs;
   private long mEndMs;
   private long mAggregation = CategoryDbTable.KEY_PERIOD_MS_AUTO;
 
   // Tasks
   private CorrelateTask mCorrelator;
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
 
     getPrefs();
     setupData(icicle);
     setupTasks();
     setupUI();
     populateFields();
   }
 
   private void getPrefs() {
     mHistory = Preferences.getHistory(getCtx());
     mSmoothing = Preferences.getSmoothingConstant(getCtx());
     mDecimals = Preferences.getDecimalPlaces(getCtx());
     mSensitivity = Preferences.getStdDevSensitivity(getCtx());
   }
 
   private void setupTasks() {
     mCorrelator = new CorrelateTask();
   }
 
   private void setupData(Bundle icicle) {
     mTSC = new TimeSeriesCollector(getDbh());
     mTSC.setHistory(mHistory);
     mTSC.setSmoothing(mSmoothing);
     mTSC.setSensitivity(mSensitivity);
     mTSC.setInterpolators(((EvenTrendActivity) getCtx()).getInterpolators());
     mTSC.updateTimeSeriesMetaLocking(true);
 
     mSeriesEnabled = getIntent().getIntegerArrayListExtra(VIEW_DEFAULT_CATIDS);
     if (mSeriesEnabled != null) {
       for (int i = 0; i < mSeriesEnabled.size(); i++) {
         Integer j = mSeriesEnabled.get(i);
         mTSC.setSeriesEnabled(j.longValue(), true);
       }
     }
 
     Calendar cal = Calendar.getInstance();
     mStartMs = getIntent().getLongExtra(GRAPH_START_MS,
         cal.getTimeInMillis() - DateUtil.DAY_MS * 7);
     mEndMs = getIntent().getLongExtra(GRAPH_END_MS, cal.getTimeInMillis());
     mAggregation = getIntent().getLongExtra(GRAPH_START_MS,
         CategoryDbTable.KEY_PERIOD_MS_AUTO);
 
     if (icicle != null) {
       mStartMs = icicle.getLong(GRAPH_START_MS);
       mEndMs = icicle.getLong(GRAPH_END_MS);
       mAggregation = icicle.getLong(GRAPH_AGGREGATION);
     }
   }
 
   private void setupUI() {
     setupListeners();
 
     requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     setContentView(R.layout.graph_view);
 
     mGraphStatus = (TextView) findViewById(R.id.graph_status);
 
     mZoomControls = createZoomControls();
     mGraphView = new GraphView(this, mZoomControls, mTSC);
     mGraphView.resetZoom();
 
     mGraphControls = (LinearLayout) findViewById(R.id.graph_controls);
     mGraphControls.setGravity(Gravity.CENTER_HORIZONTAL
         | Gravity.CENTER_VERTICAL);
 
     mAggregationSpinner = new DynamicSpinner(getCtx());
     mAggregationSpinner.setPrompt("Aggregate By");
     mAggregationSpinnerLayout = (LinearLayout) findViewById(R.id.graph_view_agg_menu);
     mAggregationSpinnerLayout.addView(mAggregationSpinner,
         new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
             LayoutParams.WRAP_CONTENT));
     mAggregationSpinner.addSpinnerItem(AUTO_AGGREGATION, new Long(
         CategoryDbTable.KEY_PERIOD_MS_AUTO));
     for (int i = 0; i < CategoryDbTable.KEY_PERIODS.length; i++) {
       mAggregationSpinner.addSpinnerItem(CategoryDbTable.KEY_PERIODS[i],
           new Long(i));
     }
     mAggregationSpinner.setOnItemSelectedListener(mAggregationSpinnerListener);
 
     mGraphPlotLayout = (LinearLayout) findViewById(R.id.graph_plot);
     mGraphPlotLayout.addView(mGraphView);
     mGraphZoomLayout = (LinearLayout) findViewById(R.id.graph_zoom);
     mGraphZoomLayout.addView(mZoomControls, new LinearLayout.LayoutParams(
         LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
     mShowTrendsToggle = (ToggleButton) findViewById(R.id.graph_view_trends);
     mShowTrendsToggle.setOnCheckedChangeListener(mShowTrendsToggleListener);
 
     mShowGoalsToggle = (ToggleButton) findViewById(R.id.graph_view_goals);
     mShowGoalsToggle.setOnCheckedChangeListener(mShowGoalsToggleListener);
 
     mShowMarkersToggle = (ToggleButton) findViewById(R.id.graph_view_markers);
     mShowMarkersToggle.setOnCheckedChangeListener(mShowMarkersToggleListener);
 
     mProgress = new ProgressIndicator.Titlebar(getCtx());
   }
 
   private void setupListeners() {
     mAggregationSpinnerListener = new Spinner.OnItemSelectedListener() {
       public void onItemSelected(AdapterView parent, View v, int position,
           long id) {
         String period = ((TextView) v).getText().toString();
         if (period.equals(AUTO_AGGREGATION)) {
           mAggregation = CategoryDbTable.KEY_PERIOD_MS_AUTO;
           mTSC.setAutoAggregation(true);
         } else {
           mAggregation = CategoryDbTable.mapPeriodToMs(period);
           mTSC.setAutoAggregation(false);
           mTSC.setAggregationMs(mAggregation);
         }
         graph();
         return;
       }
 
       public void onNothingSelected(AdapterView arg0) {
         return;
       }
     };
 
     mShowTrendsToggleListener = new CompoundButton.OnCheckedChangeListener() {
       public void onCheckedChanged(CompoundButton view, boolean isChecked) {
         mGraphView.setTrendView(isChecked);
         graph();
       }
     };
 
     mShowGoalsToggleListener = new CompoundButton.OnCheckedChangeListener() {
       public void onCheckedChanged(CompoundButton view, boolean isChecked) {
         mGraphView.setGoalView(isChecked);
         graph();
       }
     };
 
     mShowMarkersToggleListener = new CompoundButton.OnCheckedChangeListener() {
       public void onCheckedChanged(CompoundButton view, boolean isChecked) {
         mGraphView.setMarkersView(isChecked);
         graph();
       }
     };
   }
 
   private ZoomControls createZoomControls() {
     ZoomControls zoomControls = new ZoomControls(this);
     zoomControls.setLayoutParams(new FrameLayout.LayoutParams(
         LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM
             + Gravity.CENTER_HORIZONTAL));
     zoomControls.setVisibility(View.INVISIBLE);
     return zoomControls;
   }
 
   private void populateFields() {
     mGraphView.getGraph().setGraphRange(mStartMs, mEndMs);
     if (mAggregation == CategoryDbTable.KEY_PERIOD_MS_AUTO) {
       mTSC.setAutoAggregation(true);
       mAggregationSpinner.setSelection(0);
     } else {
       mTSC.setAutoAggregation(false);
       mTSC.setAggregationMs(mAggregation);
       // XXX ugly hack. We add one because we inserted "Auto" ahead of
       // everything else
       mAggregationSpinner.setSelection(CategoryDbTable
           .mapMsToIndex(mAggregation) + 1);
     }
   }
 
   @Override
   public void executeNonGuiTask() throws Exception {
     mCorrelator.correlate();
   }
 
   @Override
   public void afterExecute() {
     showDialog(DIALOG_GRAPH_CORRELATE);
   }
 
   @Override
   public void onFailure(Throwable t) {
     mTSC.unlock();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     boolean result = super.onCreateOptionsMenu(menu);
     menu.add(0, MENU_GRAPH_FILTER_ID, 0, R.string.graph_filter).setIcon(
         R.drawable.filter_small);
     menu.add(0, MENU_GRAPH_CORRELATE_ID, 0, R.string.graph_correlate).setIcon(
         R.drawable.correlate_small);
     menu.add(0, MENU_GRAPH_SNAP_TO_PERIOD_ID, 0, R.string.graph_snap_to)
         .setIcon(R.drawable.snapto);
     menu.add(0, MENU_CALENDAR_VIEW_ID, 0, R.string.menu_calendar).setIcon(
         android.R.drawable.ic_menu_today);
     menu.add(0, MENU_GRAPH_PREFS_ID, 0, R.string.menu_app_prefs).setIcon(
         android.R.drawable.ic_menu_preferences);
     return result;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
       case MENU_GRAPH_FILTER_ID:
         showDialog(DIALOG_GRAPH_FILTER);
         removeDialog(DIALOG_GRAPH_CORRELATE);
         return true;
       case MENU_GRAPH_CORRELATE_ID:
         LinearMatrixCorrelator c = new LinearMatrixCorrelator();
         mCorrelator.setCorrelator(c);
         mCorrelator.setTimeSeries(mTSC.getAllEnabledSeries());
         GUITaskQueue.getInstance().addTask(mProgress, this);
         return true;
       case MENU_GRAPH_SNAP_TO_PERIOD_ID:
         mGraphView.snapToSpan();
         graph();
         return true;
       case MENU_CALENDAR_VIEW_ID:
         calendarView();
         return true;
       case MENU_GRAPH_PREFS_ID:
         editPrefs();
         return true;
     }
     return super.onOptionsItemSelected(item);
   }
 
   private void editPrefs() {
     Intent i = new Intent(this, Preferences.class);
     startActivityForResult(i, PREFS_EDIT);
   }
 
   private void graph() {
     mGraphView.updateData();
     mGraphView.invalidate();
   }
 
   private void calendarView() {
     Intent i = new Intent(this, CalendarActivity.class);
     i.putIntegerArrayListExtra(VIEW_DEFAULT_CATIDS, mSeriesEnabled);
     i.putExtra(GRAPH_START_MS, mGraphView.getGraph().getGraphStart());
     startActivityForResult(i, CALENDAR_VIEW);
   }
 
   public TextView getGraphStatusTextView() {
     return mGraphStatus;
   }
 
   @Override
   protected Dialog onCreateDialog(int id) {
     switch (id) {
       case DIALOG_GRAPH_FILTER:
         mFilterDialog = filterDialog("Show Series");
         return mFilterDialog;
       case DIALOG_GRAPH_CORRELATE:
         Dialog d;
         d = correlateDialog("Correlations");
         return d;
       default:
     }
     return null;
   }
 
   private Dialog filterDialog(String title) {
     Builder b = new AlertDialog.Builder(getCtx());
     b.setTitle(title);
 
     mGFLA = new GraphFilterListAdapter(this, mTSC);
     for (int i = 0; i < mTSC.numSeries(); i++) {
       TimeSeries ts = mTSC.getSeries(i);
       long id = ts.getDbRow().getId();
       String name = ts.getDbRow().getCategoryName();
       String color = ts.getDbRow().getColor();
       int rank = ts.getDbRow().getRank();
       mGFLA.addItem(new GraphFilterRow(id, name, color, rank));
     }
 
     b.setAdapter(mGFLA, new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
       }
     });
 
     b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
         ((GraphActivity) getCtx()).graph();
       }
     });
     Dialog d = b.create();
 
     return d;
   }
 
   private Dialog correlateDialog(String title) {
     Builder b = new AlertDialog.Builder(getCtx());
     b.setTitle(title);
     b.setMessage(mCorrelator.mOutput);
     b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
       public void onClick(DialogInterface dialog, int whichButton) {
       }
     });
     Dialog d = b.create();
     return d;
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     super.onActivityResult(requestCode, resultCode, intent);
     graph();
   }
 
   @Override
   protected void onSaveInstanceState(Bundle outState) {
     if (mSeriesEnabled != null)
      outState.putIntegerArrayList("defaultCatIds", mSeriesEnabled);
 
     outState.putLong(GRAPH_START_MS, mGraphView.getGraph().getGraphStart());
     outState.putLong(GRAPH_END_MS, mGraphView.getGraph().getGraphEnd());
     outState.putLong(GRAPH_AGGREGATION, mAggregation);
     super.onSaveInstanceState(outState);
   }
 
   @Override
   protected void onResume() {
     getPrefs();
     mTSC.setHistory(mHistory);
     mGraphView.getGraph().setDecimals(mDecimals);
     mGraphView.setColorScheme();
     graph();
     super.onResume();
   }
   
   @Override
   protected void onPause() {
     super.onPause();
   }
 }
