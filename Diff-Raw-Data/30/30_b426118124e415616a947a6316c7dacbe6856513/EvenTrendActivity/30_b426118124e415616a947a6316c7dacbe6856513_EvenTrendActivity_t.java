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
 
 package net.redgeek.android.eventrend;
 
 import java.util.ArrayList;
 
 import net.redgeek.android.eventrend.db.EvenTrendDbAdapter;
 import net.redgeek.android.eventrend.graph.plugins.CubicInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.LinearInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.StepEarlyInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.StepLateInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.StepMidInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.TimeSeriesInterpolator;
 import net.redgeek.android.eventrend.util.DialogUtil;
 import net.redgeek.android.eventrend.util.GUITask;
 import net.redgeek.android.eventrend.util.GUITaskQueue;
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.Bundle;
 
 /**
  * Base class for all activities in the application. Note that not all activity
  * need be ListActivities, but most are, and it's convenient to wrap a bunch of
  * common setup, teardown, and sundry tasks in this one. The only penalty is a
  * little extra overhead in activities that don't actually use a list, and the
  * requirement for (an invisible) ListView be present in the root xml layout
  * file for the activity.
  * 
  * <p>
  * The common elements are creating constants for Intent indices, common Bundle
  * keys, storing the Context of the activity, creating a DialogUtil instance,
  * creating and opening a DatabaseAdapter, and starting and stopping the
  * GUITaskQueue on pause and resume.
  * 
  * @author barclay
  * 
  */
 public class EvenTrendActivity extends ListActivity implements GUITask {
   public static final String TAG = "EvenTrend";
   
   public static final int CATEGORY_LIST = 0;
   public static final int CATEGORY_CREATE = 1;
   public static final int CATEGORY_EDIT = 2;
   public static final int ENTRY_LIST = 3;
   public static final int ENTRY_EDIT = 4;
   public static final int GRAPH_VIEW = 5;
   public static final int CALENDAR_VIEW = 6;
   public static final int IMPORT_REPLACE = 7;
   // protected static final int IMORT_MERGE = 8;
   public static final int PREFS_EDIT = 9;
   public static final int FORMULA_EDIT = 10;
 
   public static final String CATEGORY_ID = "CategoryId";
   public static final String FORMULA = "Formula";
   public static final String VIEW_DEFAULT_CATIDS = "ViewIdsDefault";
   public static final String GRAPH_START_MS = "GraphStartMS";
   public static final String GRAPH_END_MS = "GraphEndMS";
   public static final String GRAPH_AGGREGATION = "GraphAggregation";
   public static final String CALENDAR_PERIOD = "CalendarPeriod";
   public static final String FORMULA_TEXT = "FormulaText";
  
  public static final int    RESULT_DELETED = 100;
 
   private Context mCtx;
   private EvenTrendDbAdapter mDbh;
   private DialogUtil mDialogUtil;
 
   // Plugings
   private ArrayList<TimeSeriesInterpolator> mInterpolators;
 
   @Override
   public void onCreate(Bundle icicle) {
     super.onCreate(icicle);
 
     mCtx = this;
     mDialogUtil = new DialogUtil(mCtx);
     mDbh = new EvenTrendDbAdapter.SqlAdapter(this);
     mDbh.open();
 
     mInterpolators = new ArrayList<TimeSeriesInterpolator>();
     registerPlugins();
   }
 
   @Override
   protected void onPause() {
     GUITaskQueue.getInstance().stop();
     super.onPause();
   }
 
   @Override
   protected void onResume() {
     GUITaskQueue.getInstance().start();
     super.onResume();
   }
 
   @Override
   protected void onDestroy() {
     mDbh.close();
     super.onResume();
   }
 
   public void afterExecute() {
   }
 
   public void executeNonGuiTask() throws Exception {
   }
 
   public void onFailure(Throwable t) {
   }
 
   public Context getCtx() {
     return mCtx;
   }
 
   public EvenTrendDbAdapter getDbh() {
     return mDbh;
   }
 
   public DialogUtil getDialogUtil() {
     return mDialogUtil;
   }
 
   public ArrayList<TimeSeriesInterpolator> getInterpolators() {
     return mInterpolators;
   }
 
   public static ArrayList<TimeSeriesInterpolator> getInterpolatorsCopy() {
     ArrayList<TimeSeriesInterpolator> list = new ArrayList<TimeSeriesInterpolator>();
     registerInterpolators(list);
     return list;
   }
 
   public TimeSeriesInterpolator getInterpolator(String name) {
     TimeSeriesInterpolator tsi = null;
 
     if (name == null)
       return null;
 
     for (int i = 0; i < mInterpolators.size(); i++) {
       tsi = mInterpolators.get(i);
       if (name.equals(tsi.getName()))
         return tsi;
     }
 
     return null;
   }
   
   private void registerPlugins() {
     registerInterpolators(mInterpolators);
   }
 
   private static void registerInterpolators(ArrayList<TimeSeriesInterpolator> list) {
     registerInterpolator(list, new LinearInterpolator());
     registerInterpolator(list, new CubicInterpolator());
     registerInterpolator(list, new StepEarlyInterpolator());
     registerInterpolator(list, new StepMidInterpolator());
     registerInterpolator(list, new StepLateInterpolator());
   }
 
   private static void registerInterpolator(ArrayList<TimeSeriesInterpolator> list,
       TimeSeriesInterpolator tsi) {
     list.add(tsi);
   }
 }
