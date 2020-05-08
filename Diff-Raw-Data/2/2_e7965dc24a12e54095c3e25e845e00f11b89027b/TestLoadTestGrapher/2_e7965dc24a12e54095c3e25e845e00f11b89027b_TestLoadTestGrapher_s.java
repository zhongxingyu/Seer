 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hcatalog.hcatmix.load;
 
 import org.apache.hcatalog.hcatmix.load.hadoop.ReduceResult;
 import org.apache.hcatalog.hcatmix.load.test.LoadTestGrapher;
 import org.apache.hcatalog.hcatmix.publisher.LoadTestResultsPublisher;
 import org.perf4j.GroupedTimingStatistics;
 import org.perf4j.TimingStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import java.util.Map;
 import java.util.Random;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import static org.testng.Assert.assertEquals;
 
 public class TestLoadTestGrapher {
     private static final Logger LOG = LoggerFactory.getLogger(TestLoadTestGrapher.class);
 
     @Test
     public void testGraphURL() throws Exception {
         SortedMap<Long, ReduceResult> timeSeries = new TreeMap<Long, ReduceResult>();
         timeSeries.put((long) 22494667, getReduceResult(108.8, 38.1, 545, 6, 98369, 450));
         timeSeries.put((long) 22494668, getReduceResult(211.4, 38.6, 391, 80, 93289, 600));
         timeSeries.put((long) 22494669, getReduceResult(313.8, 40.6, 469, 242, 91568, 750));
         timeSeries.put((long) 22494670, getReduceResult(417.4, 41.1, 619, 346, 90476, 900));
         timeSeries.put((long) 22494671, getReduceResult(523.4, 45.2, 696, 437, 89336, 1050));
         timeSeries.put((long) 22494672, getReduceResult(630.2, 46.7, 814, 555, 88539, 1200));
 
         // See that no exception is thrown
         LoadTestResultsPublisher publisher = new LoadTestResultsPublisher(timeSeries);
         publisher.publishAll();
     }
 
     private ReduceResult getReduceResult(double mean, double standardDeviation, long max, long min, int count, int threadCount) {
         GroupedTimingStatistics statistics = new GroupedTimingStatistics();
         SortedMap<String, TimingStatistics> statisticsByTag = new TreeMap<String, TimingStatistics>();
 
         TimingStatistics timingStatistics = new TimingStatistics(mean, standardDeviation, max, min, count);
         statisticsByTag.put("getDatabase", timingStatistics);
         timingStatistics = new TimingStatistics(mean + new Random().nextInt(100), standardDeviation, max, min, count);
         statisticsByTag.put("getTable", timingStatistics);
         statistics.setStatisticsByTag(statisticsByTag);
        return new ReduceResult(statistics, threadCount);
     }
 }
