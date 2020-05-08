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
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.perf4j.GroupedTimingStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 public class HCatReducer extends MapReduceBase implements Reducer<LongWritable, HCatMapper.ArrayStopWatchWritable, LongWritable, GroupedTimingStatistics> {
     private static final Logger LOG = LoggerFactory.getLogger(HCatReducer.class);
 
     @Override
     public void reduce(LongWritable timeStamp, Iterator<HCatMapper.ArrayStopWatchWritable> stopWatchArrayList, OutputCollector<LongWritable, GroupedTimingStatistics> collector, Reporter reporter) throws IOException {
         GroupedTimingStatistics statistics = new GroupedTimingStatistics();
         LOG.info("Going through statistics for time: " + timeStamp);
         while (stopWatchArrayList.hasNext()) {
             HCatMapper.ArrayStopWatchWritable stopWatchArray = stopWatchArrayList.next();
             HCatMapper.StopWatchWritable[] stopWatches = (HCatMapper.StopWatchWritable[]) stopWatchArray.toArray();
             for (HCatMapper.StopWatchWritable stopWatch : stopWatches) {
                 statistics.addStopWatch(stopWatch.getStopWatch());
             }
             LOG.info("Stats:" + stopWatchArray);
         }
         LOG.info("Final statistics for " + timeStamp + " " + statistics);
         collector.collect(timeStamp, statistics);
     }
 }
