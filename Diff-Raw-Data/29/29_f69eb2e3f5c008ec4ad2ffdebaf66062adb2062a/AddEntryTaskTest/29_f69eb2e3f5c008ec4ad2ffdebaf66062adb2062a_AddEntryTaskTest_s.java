 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.redgeek.android.eventrend.test.backgroundtasks;
 
 import junit.framework.TestCase;
 
 import net.redgeek.android.eventrend.backgroundtasks.AddEntryTask;
 import net.redgeek.android.eventrend.db.CategoryDbTable;
 import net.redgeek.android.eventrend.db.EntryDbTable;
 import net.redgeek.android.eventrend.graph.plugins.LinearInterpolator;
 import net.redgeek.android.eventrend.graph.plugins.TimeSeriesInterpolator;
 import net.redgeek.android.eventrend.primitives.TimeSeriesCollector;
 import net.redgeek.android.eventrend.test.common.DbTestReader;
 import net.redgeek.android.eventrend.test.common.MockEvenTrendDbAdapter;
 import net.redgeek.android.eventrend.test.common.MockTimeSeriesPainter;
 import net.redgeek.android.eventrend.util.DateUtil;
 import net.redgeek.android.eventrend.util.DateUtil.Period;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 public class AddEntryTaskTest extends TestCase {
   private TimeSeriesCollector newTSC(MockEvenTrendDbAdapter dbh) {
     MockTimeSeriesPainter painter = new MockTimeSeriesPainter();
     ArrayList<TimeSeriesInterpolator> interpolators = new ArrayList<TimeSeriesInterpolator>();
     interpolators.add(new LinearInterpolator());
 
     TimeSeriesCollector tsc = new TimeSeriesCollector(dbh, painter);
     tsc.setHistory(20);
     tsc.setSmoothing(0.1f);
     tsc.setSensitivity(1.0f);
     tsc.setInterpolators(interpolators);
 
     return tsc;
   }
   
   private void changeTimestamp(MockEvenTrendDbAdapter dbh, long rowId, long ts) {
     EntryDbTable.Row entry = dbh.fetchEntry(rowId);
     entry.setTimestamp(ts);
     dbh.updateEntry(entry);
   }
 
   private String makeFilePath(String filename) {
     String sep = System.getProperty("file.separator");
     String path = System.getProperty("user.dir");
     String[] subdir = new String[] { "test", "net", "redgeek", "android",
         "eventrend", "test", "backgroundtasks", "dbtestdata" };
 
     for (int i = 0; i < subdir.length; i++) {
       path += sep + subdir[i];
     }
     path += sep + filename;
     return path;
   }
 
   // TODO:  Note this isn't quite a reproducible test until I get around to 
   // mocking out the system clock calls
   public void testAddEntry() {
     AddEntryTask addTask;
     TimeSeriesCollector tsc;
     CategoryDbTable.Row cat;
     EntryDbTable.Row entry;
     long base_ms, add_ms;
 
     MockEvenTrendDbAdapter dbh = new MockEvenTrendDbAdapter();
     DbTestReader reader = new DbTestReader(dbh);
     tsc = newTSC(dbh);
     
     // no zerofill (no aggregation)
     reader.populateFromFile(makeFilePath("addentry_nozerofill.xml"));
     tsc.updateTimeSeriesMetaLocking(false);
     cat = dbh.fetchCategory(1);
     base_ms = System.currentTimeMillis();
     add_ms = DateUtil.mapPeriodToLong(Period.HOUR);
     changeTimestamp(dbh, 1, base_ms);
     
     addTask = new AddEntryTask(tsc, 2, 10);
     addTask.AddEntry(cat, base_ms + add_ms, 9.0f);
     entry = dbh.fetchEntry(2);
     // check the fresh entry
     assertNotNull(entry);
     assertEquals(9.0f, entry.getValue());
     assertEquals(base_ms + add_ms, entry.getTimestamp());
     assertEquals(1, entry.getCategoryId());
     assertEquals(1, entry.getNEntries());
     // check the timeseries has been updated
     assertNotNull(tsc.getSeries(0).getDatapoints());
     assertEquals(2, tsc.getSeries(0).getDatapoints().size());
     assertEquals(base_ms, tsc.getSeries(0).getDatapoints().get(0).mMillis);
     assertEquals(1.0f, tsc.getSeries(0).getDatapoints().get(0).mValue.y);
     assertEquals(base_ms + add_ms, tsc.getSeries(0).getDatapoints().get(1).mMillis);
     assertEquals(9.0f, tsc.getSeries(0).getDatapoints().get(1).mValue.y);
     // check to "last add" fields
     assertEquals(2, addTask.mLastAddId);
     assertEquals(9.0f, addTask.mLastAddValue);
     assertEquals(base_ms + add_ms, addTask.mLastAddTimestamp);
     assertFalse(addTask.mLastAddUpdate);
 
     
     // zerofill, 1h aggregation, new entry in aggregation period
     reader.populateFromFile(makeFilePath("addentry_zerofill_1h.xml"));
     tsc.clearSeriesLocking();
     tsc.updateTimeSeriesMetaLocking(false);
     cat = dbh.fetchCategory(1);
     base_ms = System.currentTimeMillis();
     add_ms = -1000L;
     changeTimestamp(dbh, 1, base_ms + add_ms);
 
     addTask = new AddEntryTask(tsc, 2, 10);   
     addTask.AddEntry(cat, base_ms, 9.0f);
     entry = dbh.fetchEntry(1);
     // check the updated entry
     assertNotNull(entry);
     assertEquals(10.0f, entry.getValue());
     assertEquals(base_ms + add_ms, entry.getTimestamp());
     assertEquals(1, entry.getCategoryId());
     assertEquals(2, entry.getNEntries());
     // check the timeseries has been updated
     assertNotNull(tsc.getSeries(0).getDatapoints());
     assertEquals(1, tsc.getSeries(0).getDatapoints().size());
     assertEquals(base_ms + add_ms, tsc.getSeries(0).getDatapoints().get(0).mMillis);
     assertEquals(10.0f, tsc.getSeries(0).getDatapoints().get(0).mValue.y);
     // check to "last add" fields
     assertEquals(1, addTask.mLastAddId);
     assertEquals(1.0f, addTask.mLastAddOldValue);
     assertEquals(10.0f, addTask.mLastAddValue);
     assertEquals(base_ms, addTask.mLastAddTimestamp);
     assertTrue(addTask.mLastAddUpdate);
 
     
     // zerofill, 1h aggregation, new entry past aggregation period
     reader.populateFromFile(makeFilePath("addentry_zerofill_1h.xml"));
     tsc.clearSeriesLocking();
     tsc.updateTimeSeriesMetaLocking(false);
     cat = dbh.fetchCategory(1);
     base_ms = System.currentTimeMillis();
     add_ms = -DateUtil.mapPeriodToLong(Period.HOUR);
     changeTimestamp(dbh, 1, base_ms + (2 * add_ms));
 
     addTask = new AddEntryTask(tsc, 2, 10);        
     addTask.AddEntry(cat, base_ms, 9.0f);
     // check the fresh entries
     entry = dbh.fetchEntry(2);
     assertNotNull(entry);
     assertEquals(0.0f, entry.getValue());
     assertEquals(base_ms + (add_ms - (base_ms % add_ms)), entry.getTimestamp());
     assertEquals(1, entry.getCategoryId());
    assertEquals(0, entry.getNEntries());
     entry = dbh.fetchEntry(3);
     assertNotNull(entry);
     assertEquals(9.0f, entry.getValue());
    assertEquals(base_ms + (add_ms - (base_ms % add_ms)) + -add_ms, entry.getTimestamp());
     assertEquals(1, entry.getCategoryId());
     assertEquals(1, entry.getNEntries());
     // check the timeseries has been updated
     assertNotNull(tsc.getSeries(0).getDatapoints());
     assertEquals(3, tsc.getSeries(0).getDatapoints().size());
     assertEquals(base_ms + (2 * add_ms), tsc.getSeries(0).getDatapoints().get(0).mMillis);
     assertEquals(1.0f, tsc.getSeries(0).getDatapoints().get(0).mValue.y);
     assertEquals(base_ms + (add_ms - (base_ms % add_ms)), tsc.getSeries(0).getDatapoints().get(1).mMillis);
     assertEquals(0.0f, tsc.getSeries(0).getDatapoints().get(1).mValue.y);
    assertEquals(base_ms + (add_ms - (base_ms % add_ms)) + -add_ms, tsc.getSeries(0).getDatapoints().get(2).mMillis);
     assertEquals(9.0f, tsc.getSeries(0).getDatapoints().get(2).mValue.y);
     // check to "last add" fields
     assertEquals(3, addTask.mLastAddId);
     assertEquals(9.0f, addTask.mLastAddValue);
     assertEquals(base_ms, addTask.mLastAddTimestamp);
    assertTrue(addTask.mLastAddUpdate);
   }
 }
