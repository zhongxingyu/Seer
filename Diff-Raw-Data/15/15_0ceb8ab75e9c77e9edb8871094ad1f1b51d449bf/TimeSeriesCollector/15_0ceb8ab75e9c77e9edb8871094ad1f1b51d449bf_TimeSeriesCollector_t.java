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
 
 package net.redgeek.android.eventrend.primitives;
 
 import android.database.Cursor;
 
 import net.redgeek.android.eventrend.db.CategoryDbTable;
 import net.redgeek.android.eventrend.db.EvenTrendDbAdapter;
 import net.redgeek.android.eventrend.graph.TimeSeriesPainter;
 import net.redgeek.android.eventrend.graph.plugins.TimeSeriesInterpolator;
 import net.redgeek.android.eventrend.synthetic.Formula;
 import net.redgeek.android.eventrend.synthetic.FormulaCache;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.Number;
 import net.redgeek.android.eventrend.util.DateUtil.Period;
 import net.redgeek.android.eventrend.util.Number.TrendState;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class TimeSeriesCollector {
   private ArrayList<TimeSeries> mSeries;
 
   private long mAggregationMs;
   private int mHistory = 20;
   private float mSmoothing = 0.1f;
   private float mSensitivity = 1.0f;
   private Lock mLock;
   private boolean mAutoAggregation;
   private int mAutoAggregationOffset;
 
   private DatapointCache mDatapointCache;
   private FormulaCache mFormulaCache;
   private ArrayList<TimeSeriesInterpolator> mInterpolators;
 
   private EvenTrendDbAdapter mDbh;
   private DateUtil mAutoAggSpan;
   private Calendar mCal1;
   private Calendar mCal2;
 
   private long mCollectionStart;
   private long mCollectionEnd;
   private long mQueryStart;
   private long mQueryEnd;
 
  private TimeSeriesPainter mDefaultPainter;
 
   public TimeSeriesCollector(EvenTrendDbAdapter dbh) {
     initialize(dbh, null);
   }
 
   public TimeSeriesCollector(EvenTrendDbAdapter dbh, TimeSeriesPainter painter) {
     initialize(dbh, painter);
   }
 
   public void initialize(EvenTrendDbAdapter dbh, TimeSeriesPainter painter) {
     mDbh = dbh;
     mSeries = new ArrayList<TimeSeries>();
 
     mAutoAggSpan = new DateUtil();
     mDatapointCache = new DatapointCache(mDbh);
     mFormulaCache = new FormulaCache();
     mAutoAggregation = false;
     mAutoAggregationOffset = 0;
     mCal1 = Calendar.getInstance();
     mCal2 = Calendar.getInstance();
 
    mDefaultPainter = painter;
 
     mLock = new ReentrantLock();
   }
 
   @Override
   public String toString() {
     return mSeries.toString();
   }
 
   public EvenTrendDbAdapter getDbh() {
     return mDbh;
   }
 
   public void updateTimeSeriesMeta(boolean disableByDefault) {
     TimeSeries ts;
     Formula formula;
 
     waitForLock();
     Cursor c = mDbh.fetchAllCategories();
     c.moveToFirst();
     for (int i = 0; i < c.getCount(); i++) {
       CategoryDbTable.Row row = new CategoryDbTable.Row(c);
       updateTimeSeriesMeta(row, disableByDefault);
       c.moveToNext();
     }
     c.close();
 
     // cycle through again, we may have had a series that was dependent on
     // another series that was created later
     for (int i = 0; i < mSeries.size(); i++) {
       setDependents(mSeries.get(i));
       setDependees(mSeries.get(i));
     }
 
     unlock();
   }
 
   public void updateTimeSeriesMeta(CategoryDbTable.Row row, boolean disable) {
     TimeSeries ts = getSeriesById(row.getId());
 
     if (ts == null) {
      if (mDefaultPainter == null) {
        TimeSeriesPainter p = new TimeSeriesPainter.Default();
        ts = new TimeSeries(row, mHistory, mSmoothing, p);
      } else {
        ts = new TimeSeries(row, mHistory, mSmoothing, mDefaultPainter);
      }
       mSeries.add(ts);
       mDatapointCache.addCacheableCategory(row.getId(), mHistory);
     }
 
     ts.setDbRow(row);
     setSeriesInterpolator(ts, row.getInterpolation());
 
     if (row.getSynthetic() == true) {
       Formula formula = mFormulaCache.getFormula(Long.valueOf(row.getId()));
       if (formula == null)
         formula = new Formula();
       formula.setFormula(row.getFormula());
       mFormulaCache.setFormula(row.getId(), formula);
     }
 
     if (disable)
       setSeriesEnabled(row.getId(), false);
 
     setDependents(ts);
     setDependees(ts);
   }
 
   public void updateTimeSeriesStats(long catId) {
     TimeSeries ts = getSeriesById(catId);
     if (ts == null)
       return;
 
     ts.recalcStatsAndBounds(mSmoothing, mHistory);
   }
 
   public void updateTimeSeriesStats(TimeSeries ts) {
     if (ts == null)
       return;
 
     ts.recalcStatsAndBounds(mSmoothing, mHistory);
   }
 
   public void updateTimeSeriesData(boolean flushCache) {
     updateTimeSeriesData(mQueryStart, mQueryEnd, flushCache);
   }
 
   public void updateTimeSeriesData(long start, long end, boolean flushCache) {
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.isEnabled() == true) {
         long catId = ts.getDbRow().getId();
         updateTimeSeriesData(catId, start, end, flushCache);
       }
     }
   }
 
   public void updateTimeSeriesData(long catId, boolean flushCache) {
     updateTimeSeriesData(catId, mQueryStart, mQueryEnd, flushCache);
   }
 
   public void updateTimeSeriesData(long catId, long start, long end,
       boolean flushCache) {
     waitForLock();
     if (flushCache == true)
       mDatapointCache.refresh(catId);
 
     if (mDatapointCache.isCategoryCacheValid(catId) == true) {
       gatherSeries(start, end);
     }
     unlock();
   }
 
   public void setSmoothing(float smoothing) {
     mSmoothing = smoothing;
     for (int i = 0; i < mSeries.size(); i++) {
       mSeries.get(i).recalcStatsAndBounds(mSmoothing, mHistory);
     }
   }
 
   public void setHistory(int history) {
     mHistory = history;
     for (int i = 0; i < mSeries.size(); i++) {
       mSeries.get(i).recalcStatsAndBounds(mSmoothing, mHistory);
     }
   }
 
   public void setSensitivity(float sensitivity) {
     mSensitivity = sensitivity;
   }
 
   public void setInterpolators(ArrayList<TimeSeriesInterpolator> list) {
     mInterpolators = list;
   }
 
   public Boolean lock() {
     return mLock.tryLock();
   }
 
   public void waitForLock() {
     while (mLock.tryLock() == false) {
     }
     return;
   }
 
   public void unlock() {
     mLock.unlock();
   }
 
   public void setAutoAggregation(boolean b) {
     mAutoAggregation = b;
   }
 
   public void setAutoAggregationOffset(int offset) {
     mAutoAggregationOffset = offset;
   }
 
   public boolean getAutoAggregation() {
     return mAutoAggregation;
   }
 
   public void setAggregationMs(long millis) {
     mAggregationMs = millis;
   }
 
   public void setSeriesInterpolator(TimeSeries ts, String type) {
     TimeSeriesInterpolator tsi = null;
     for (int i = 0; i < mInterpolators.size(); i++) {
       tsi = mInterpolators.get(i);
       if (type.equals(tsi.getName()))
         break;
     }
 
     if (tsi != null)
       ts.setInterpolator(tsi);
 
     return;
   }
 
   public void clearSeries() {
     TimeSeries ts;
 
     waitForLock();
     for (int i = 0; i < mSeries.size(); i++) {
       ts = mSeries.get(i);
       if (ts != null) {
         ts.clearSeries();
       }
     }
     mSeries.clear();
     unlock();
   }
 
   public boolean isSeriesEnabled(long catId) {
     TimeSeries ts = getSeriesById(catId);
     if (ts == null)
       return false;
     return ts.isEnabled();
   }
 
   public void setSeriesEnabled(long catId, boolean b) {
     TimeSeries ts = getSeriesById(catId);
     if (ts == null)
       return;
     ts.setEnabled(b);
   }
 
   public void toggleSeriesEnabled(long catId) {
     if (isSeriesEnabled(catId))
       setSeriesEnabled(catId, false);
     else
       setSeriesEnabled(catId, true);
     return;
   }
 
   public int numSeries() {
     return mSeries.size();
   }
 
   public TimeSeries getSeries(int i) {
     return mSeries.get(i);
   }
 
   public TimeSeries getSeriesById(long catId) {
     waitForLock();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.getDbRow().getId() == catId) {
         unlock();
         return ts;
       }
     }
     unlock();
     return null;
   }
 
   public TimeSeries getSeriesByName(String name) {
     waitForLock();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.getDbRow().getCategoryName().equals(name)) {
         unlock();
         return ts;
       }
     }
     unlock();
     return null;
   }
 
   public ArrayList<TimeSeries> getAllSeries() {
     return mSeries;
   }
 
   public ArrayList<TimeSeries> getAllEnabledSeries() {
     ArrayList<TimeSeries> list = new ArrayList<TimeSeries>();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.isEnabled())
         list.add(ts);
     }
     return list;
   }
 
   public Datapoint getVisibleFirstDatapoint() {
     Datapoint first = null;
     waitForLock();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.isEnabled() == true) {
         Datapoint d = ts.getFirstVisible();
         if (first == null)
           first = d;
         else if (d.mMillis < first.mMillis)
           first = d;
       }
     }
     unlock();
     return first;
   }
 
   public Datapoint getVisibleLastDatapoint() {
     Datapoint last = null;
     waitForLock();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts != null && ts.isEnabled() == true) {
         Datapoint d = ts.getFirstVisible();
         if (last == null)
           last = d;
         else if (d.mMillis > last.mMillis)
           last = d;
       }
     }
     unlock();
     return last;
   }
 
   public void clearCache() {
     mDatapointCache.clearCache();
     clearSeries();
   }
 
   public synchronized void gatherLatestDatapoints(long catId, int history) {
     waitForLock();
     mDatapointCache.populateLatest(catId, history);
     TimeSeries ts = getSeriesById(catId);
 
     if (ts.getDbRow().getSynthetic() == false) {
       ts.clearSeries();
 
       ArrayList<Datapoint> l = mDatapointCache.getLast(ts.getDbRow().getId(),
           history);
       if (l != null) {
         l = aggregateDatapoints(l, ts.getDbRow().getType());
         ts.setDatapoints(null, l, null);
       }
     }
 
     unlock();
   }
 
   public synchronized void gatherSeries(long milliStart, long milliEnd) {
     ArrayList<Datapoint> pre, range, post;
     boolean has_data;
     long oldAggregationMs = mAggregationMs;
 
     waitForLock();
 
     mQueryStart = milliStart;
     mQueryEnd = milliEnd;
 
     setCollectionTimes(milliStart, milliEnd);
     for (int i = 0; i < mSeries.size(); i++) {
       has_data = false;
 
       TimeSeries ts = mSeries.get(i);
       if (ts.getDbRow().getSynthetic())
         continue;
 
       if (ts.isEnabled() == false) {
         boolean skip = true;
         for (int j = 0; j < ts.getDependees().size(); j++) {
           if (ts.getDependees().get(j).isEnabled() == true) {
             skip = false;
             break;
           }
         }
         if (skip == true)
           continue;
       }
 
       mDatapointCache.populateRange(ts.getDbRow().getId(), mCollectionStart,
           mCollectionEnd, mAggregationMs);
 
       pre = mDatapointCache.getDataBefore(ts.getDbRow().getId(), mHistory,
           mCollectionStart);
       if (pre != null && pre.size() > 0) {
         has_data = true;
         pre = aggregateDatapoints(pre, ts.getDbRow().getType());
       }
 
       range = mDatapointCache.getDataInRange(ts.getDbRow().getId(),
           mCollectionStart, mCollectionEnd);
       if (range != null && range.size() > 0) {
         has_data = true;
         range = aggregateDatapoints(range, ts.getDbRow().getType());
       }
 
       post = mDatapointCache.getDataAfter(ts.getDbRow().getId(), 1,
           mCollectionEnd);
       if (post != null && range.size() > 0) {
         has_data = true;
         post = aggregateDatapoints(post, ts.getDbRow().getType());
       }
 
       if (has_data == true) {
         ts.setDatapoints(pre, range, post);
       }
     }
 
     generateSynthetics();
 
     mAggregationMs = oldAggregationMs;
 
     unlock();
 
     return;
   }
 
   public Datapoint getLastDatapoint(long catId) {
     ArrayList<Datapoint> list = mDatapointCache.getLast(catId, 1);
     if (list == null || list.size() < 1)
       return null;
     return list.get(0);
   }
 
   public void updateCategoryTrend(long catId) {
     String trendStr = "trend_unknown";
     float stdDev = 0.0f;
     float lastTrend = 0.0f;
     float newTrend = 0.0f;
 
     gatherLatestDatapoints(catId, mHistory);
     TimeSeries ts = getSeriesById(catId);
     if (ts == null)
       return;
 
     lastTrend = ts.getTrendStats().mTrendPrev;
     newTrend = ts.getTrendStats().mTrend;
     stdDev = ts.getValueStats().mStdDev;
 
     TrendState state = Number.getTrendState(lastTrend, newTrend, ts.getDbRow()
         .getGoal(), mSensitivity, stdDev);
     trendStr = Number.mapTrendStateToString(state);
 
     mDbh.updateCategoryTrend(catId, trendStr, newTrend);
 
     if (ts.getDependees().size() > 0) {
       for (int i = 0; i < ts.getDependees().size(); i++) {
         TimeSeries dependee = ts.getDependees().get(i);
 
         for (int j = 0; j < dependee.getDependents().size(); j++) {
           TimeSeries tmp = dependee.getDependents().get(j);
           gatherLatestDatapoints(tmp.getDbRow().getId(), mHistory);
         }
 
         Formula formula = mFormulaCache.getFormula(dependee.getDbRow().getId());
         ArrayList<Datapoint> calculated = formula.apply(dependee
             .getDependents());
         dependee.setDatapoints(null, calculated, null);
 
         lastTrend = dependee.getTrendStats().mTrendPrev;
         newTrend = dependee.getTrendStats().mTrend;
         stdDev = dependee.getValueStats().mStdDev;
 
         state = Number.getTrendState(lastTrend, newTrend, dependee.getDbRow()
             .getGoal(), mSensitivity, stdDev);
         trendStr = Number.mapTrendStateToString(state);
 
         mDbh.updateCategoryTrend(dependee.getDbRow().getId(), trendStr,
             newTrend);
       }
     }
   }
 
   private void generateSynthetics() {
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries synth = mSeries.get(i);
       if (synth.getDbRow().getSynthetic() == false
           || synth.isEnabled() == false)
         continue;
 
       generateSynthetic(synth);
     }
   }
 
   private void generateSynthetic(TimeSeries synth) {
     Formula formula = mFormulaCache.getFormula(synth.getDbRow().getId());
 
     long ms;
     long firstVisibleMs = Long.MAX_VALUE;
     long lastVisibleMs = Long.MIN_VALUE;
 
     for (int j = 0; j < synth.getDependents().size(); j++) {
       TimeSeries ts = synth.getDependents().get(j);
       List<Datapoint> range = ts.getVisible();
 
       if (range != null) {
         ms = range.get(0).mMillis;
         if (ms < firstVisibleMs)
           firstVisibleMs = ms;
         ms = range.get(range.size() - 1).mMillis;
         if (ms > lastVisibleMs)
           lastVisibleMs = ms;
       }
     }
 
     ArrayList<Datapoint> calculated = formula.apply(synth.getDependents());
     ArrayList<Datapoint> pre = new ArrayList<Datapoint>();
     ArrayList<Datapoint> visible = new ArrayList<Datapoint>();
     ArrayList<Datapoint> post = new ArrayList<Datapoint>();
 
     for (int j = 0; j < calculated.size(); j++) {
       Datapoint d = calculated.get(j);
       d.mCatId = synth.getDbRow().getId();
       d.mSynthetic = true;
       if (d.mMillis < firstVisibleMs)
         pre.add(d);
       else if (d.mMillis <= lastVisibleMs)
         visible.add(d);
       else
         post.add(d);
     }
 
     pre = aggregateDatapoints(pre, synth.getDbRow().getType());
     visible = aggregateDatapoints(visible, synth.getDbRow().getType());
     post = aggregateDatapoints(post, synth.getDbRow().getType());
 
     synth.setDatapoints(pre, visible, post);
   }
 
   private ArrayList<Datapoint> aggregateDatapoints(ArrayList<Datapoint> list,
       String type) {
     Datapoint accumulator = null;
     Datapoint d = null;
 
     if (mAggregationMs == 0)
       return list;
 
     ArrayList<Datapoint> newList = new ArrayList<Datapoint>();
 
     for (int i = 0; i < list.size(); i++) {
       d = list.get(i);
 
       if (i == 0) {
         accumulator = new Datapoint(d);
         continue;
       }
 
       if (inSameAggregationPeriod(accumulator, d) == false) {
         newList.add(accumulator);
         accumulator = new Datapoint(d);
         accumulator.mNEntries = 1;
       } else {
         if (type.equals(CategoryDbTable.KEY_TYPE_SUM)) {
           accumulator.mValue.y += d.mValue.y;
           accumulator.mNEntries++;
         } else if (type.equals(CategoryDbTable.KEY_TYPE_AVERAGE)) {
           if (accumulator.mNEntries + d.mNEntries != 0) {
             accumulator.mNEntries++;
             float oldMean = accumulator.mValue.y;
             accumulator.mValue.y += ((d.mValue.y - oldMean) / accumulator.mNEntries);
           }
         }
       }
     }
 
     if (accumulator != null)
       newList.add(accumulator);
 
     return newList;
   }
 
   private void setCollectionTimes(long milliStart, long milliEnd) {
     if (mAutoAggregation == true) {
       mAutoAggSpan.setSpanOffset(mAutoAggregationOffset);
       mAutoAggSpan.setSpan(milliStart, milliEnd);
       mAggregationMs = DateUtil.mapPeriodToLong(mAutoAggSpan.getSpan());
     }
 
     // this adjustment is to make sure that the edges of the visible range
     // doesn't
     // span periods (for the purposes of calculations, not display --
     // anything
     // that's
     // in the visible range array, but not actually on-screen due to
     // aggregation
     // back
     // to the first datapoint in the period, will just be drawn off screen.
     // Note
     // that
     // this will also cause the scaling to take into account these
     // off-screen
     // points,
     // but that's okay, and perhaps desireable, since it's more likely to
     // have
     // no
     // datapoints visible within the range, having only aggregated points on
     // either
     // side, when aggregating.
     if (mAggregationMs != 0) {
       Period p = DateUtil.mapLongToPeriod(mAggregationMs);
 
       mCal1.setTimeInMillis(milliStart);
       DateUtil.setToPeriodStart(mCal1, p);
       milliStart = mCal1.getTimeInMillis();
 
       mCal2.setTimeInMillis(milliEnd);
       DateUtil.setToPeriodStart(mCal2, p);
 
       int step = 1;
       if (p == Period.QUARTER)
         step = 3;
       mCal2.add(DateUtil.mapLongToCal(mAggregationMs), step);
 
       milliEnd = mCal2.getTimeInMillis();
     }
 
     mCollectionStart = milliStart;
     mCollectionEnd = milliEnd;
 
     return;
   }
 
   private boolean inSameAggregationPeriod(Datapoint d1, Datapoint d2) {
     if (d1 == null || d2 == null)
       return false;
 
     mCal1.setTimeInMillis(d1.mMillis);
     mCal2.setTimeInMillis(d2.mMillis);
     return DateUtil.inSamePeriod(mCal1, mCal2, mAggregationMs);
   }
 
   private void setDependents(TimeSeries synth) {
     if (synth.getDbRow().getSynthetic() == false)
       return;
 
     Formula formula = mFormulaCache.getFormula(synth.getDbRow().getId());
     ArrayList<String> names = formula.getDependentNames();
     if (names == null)
       return;
 
     synth.getDependents().clear();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries ts = mSeries.get(i);
       if (ts == null || ts == synth)
         continue;
 
       if (names.contains(ts.getDbRow().getCategoryName()))
         synth.addDependent(ts);
     }
 
     return;
   }
 
   private void setDependees(TimeSeries ts) {
     ts.getDependees().clear();
     for (int i = 0; i < mSeries.size(); i++) {
       TimeSeries dependee = getSeries(i);
       if (ts == null || ts == dependee)
         continue;
 
       if (dependee.getDbRow().getSynthetic() == true) {
         Formula formula = mFormulaCache.getFormula(dependee.getDbRow().getId());
         ArrayList<String> names = formula.getDependentNames();
 
         if (names.contains(ts.getDbRow().getCategoryName()))
           ts.addDependee(dependee);
       }
     }
 
     return;
   }
 }
