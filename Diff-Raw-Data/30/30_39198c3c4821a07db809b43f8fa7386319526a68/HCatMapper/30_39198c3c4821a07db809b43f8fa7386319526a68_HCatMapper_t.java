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
 
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.perf4j.StopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.hadoop.security.token.Token;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
 * Author: malakar
 */
 public class HCatMapper extends MapReduceBase implements Mapper<LongWritable, Text, LongWritable, HCatMapper.ArrayStopWatchWritable> {
     public static final int THREAD_INCREMENT_COUNT = 5;
     public static final long THREAD_INCREMENT_INTERVAL = 5 * 60 * 1000;
     public static final int MAP_TIMEOUT_MINUTES = 3;
     public static final int MAP_TIMEOUT_BUFFER_IN_MINUTES = 1;
     public static final int TIME_SERIES_INTERVAL_IN_MINUTES = 1;
     private static final Logger LOG = LoggerFactory.getLogger(HCatMapper.class);
 
     private long expiryTimeInMillis;
     private long expiryTimeWithBufferInMillis;
     private Token token;
 
    public HCatMapper() {
    }

     @Override
     public void configure(JobConf jobConf) {
         super.configure(jobConf);
         long startTime = System.currentTimeMillis();
         expiryTimeInMillis = startTime + MAP_TIMEOUT_MINUTES * 60 * 1000;
         expiryTimeWithBufferInMillis = System.currentTimeMillis() + MAP_TIMEOUT_MINUTES * 60 * 1000
                             + MAP_TIMEOUT_BUFFER_IN_MINUTES * 60 * 1000;
         token = jobConf.getCredentials().getToken(new Text(HadoopLoadGenerator.METASTORE_TOKEN_KEY));
         try {
             LOG.info("Kerberos token received from job launcher: " + token.encodeToUrlString());
         } catch (IOException e) {
             LOG.error("Couldn't encode token to URL string");
         }
         try {
             UserGroupInformation.getCurrentUser().addToken(token);
         } catch (IOException e) {
             LOG.info("Error adding token to user", e);
         }
 
     }
 
     @Override
     public void map(LongWritable longWritable, Text text, OutputCollector<LongWritable, ArrayStopWatchWritable> collector, final Reporter reporter) throws IOException {
         LOG.info(MessageFormat.format("Input: {0}={1}", longWritable, text));
         final List<Future<SortedMap<Long, ArrayStopWatchWritable>>> futures = new ArrayList<Future<SortedMap<Long, ArrayStopWatchWritable>>>();
         final List<Task> tasks = new ArrayList<Task>();
         tasks.add(new Task.ReadTask(token));
 
         ThreadCreatorTimer createNewThreads = new ThreadCreatorTimer(expiryTimeInMillis, tasks, futures, reporter);
 
         Timer newThreadCreator = new Timer(true);
         newThreadCreator.scheduleAtFixedRate(createNewThreads, 0, THREAD_INCREMENT_INTERVAL);
         try {
             Thread.sleep(expiryTimeWithBufferInMillis - System.currentTimeMillis());
         } catch (InterruptedException e) {
             LOG.error("Got interrupted while sleeping for timer thread to finish");
         }
         newThreadCreator.cancel();
         LOG.info("Time is over, will collect the futures now");
         SortedMap<Long, ArrayStopWatchWritable> stopWatches = new TreeMap<Long, ArrayStopWatchWritable>();
         for (Future<SortedMap<Long, ArrayStopWatchWritable>> future : futures) {
             try {
                 stopWatches.putAll(future.get());
             } catch (Exception e) {
                 LOG.error("Error while getting thread results", e);
             }
         }
         LOG.info("Collected all the statistics for #threads: " + createNewThreads.getThreadCount());
         for (Map.Entry<Long, ArrayStopWatchWritable> entry : stopWatches.entrySet()) {
             collector.collect(new LongWritable(entry.getKey()), entry.getValue());
         }
     }
 
     public static class MetaStoreWorker implements Callable<SortedMap<Long, ArrayStopWatchWritable>> {
         private final long expiryTime;
         private final List<Task> tasks;
 
         public MetaStoreWorker(final long expiryTime, List<Task> tasks) {
             this.tasks = tasks;
 
             this.expiryTime = expiryTime;
         }
 
         @Override
         public SortedMap<Long, ArrayStopWatchWritable> call() throws Exception {
             SortedMap<Long, ArrayStopWatchWritable> timeSeriesStopWatches = new TreeMap<Long, ArrayStopWatchWritable>();
 
             List<StopWatchWritable> stopWatches = new ArrayList<StopWatchWritable>();
             long currentCheckPoint = 0;
             metastoreCalls: while(true) {
                 for (Task task : tasks) {
                     if(currentTimeInMinutes() >= currentCheckPoint + TIME_SERIES_INTERVAL_IN_MINUTES) {
                         if(currentCheckPoint != 0) { // Not first time
                             ArrayStopWatchWritable arrayStopWatchWritable = new ArrayStopWatchWritable(stopWatches.toArray(new StopWatchWritable[0]));
                             timeSeriesStopWatches.put(currentCheckPoint, arrayStopWatchWritable);
                         }
                         stopWatches = new ArrayList<StopWatchWritable>();
                         currentCheckPoint = nextCheckpoint();
                         LOG.info(Thread.currentThread().getName() + ": Checkpoint is:" + currentCheckPoint);
                     }
 
                     StopWatch stopWatch = new StopWatch(task.getName());
                     task.doTask();
                     stopWatch.stop();
                     stopWatches.add(StopWatchWritable.fromStopWatch(stopWatch));
                     if(System.currentTimeMillis() > expiryTime) {
                         LOG.info(Thread.currentThread().getName() + ": Stopped doing work as thread expired");
                         break metastoreCalls;
                     }
                 }
             }
             for (Task task : tasks) {
                 task.close();
             }
             return timeSeriesStopWatches;
         }
 
         private static long nextCheckpoint() {
             long checkPoint = currentTimeInMinutes();
             return checkPoint - (checkPoint % TIME_SERIES_INTERVAL_IN_MINUTES);
         }
 
         private static long currentTimeInMinutes() {
             return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
         }
     }
 
     public static class ArrayStopWatchWritable extends ArrayWritable {
         public ArrayStopWatchWritable() {
             super(StopWatchWritable.class);
         }
 
         public ArrayStopWatchWritable(StopWatchWritable[] values) {
             super(StopWatchWritable.class, values);
         }
     }
 
     public static class StopWatchWritable implements Writable {
         private StopWatch stopWatch;
 
        public StopWatchWritable() {
        }

         @Override
         public void write(DataOutput dataOutput) throws IOException {
             dataOutput.writeLong(stopWatch.getStartTime());
             dataOutput.writeLong(stopWatch.getElapsedTime());
             dataOutput.writeUTF(stopWatch.getTag());
         }
 
         @Override
         public void readFields(DataInput dataInput) throws IOException {
             long startTime = dataInput.readLong();
             long elapsedTime = dataInput.readLong();
             String tag = dataInput.readUTF();
             stopWatch = new StopWatch(startTime, elapsedTime, tag, null);
         }
 
         public StopWatchWritable(StopWatch stopWatch) {
             this.stopWatch = stopWatch;
         }
 
         public static StopWatchWritable fromStopWatch(StopWatch stopWatch) {
             return new StopWatchWritable(stopWatch);
         }
 
         public StopWatch getStopWatch() {
             return  stopWatch;
         }
 
     }
 
     public static class ThreadCreatorTimer extends TimerTask {
         private int threadCount;
         private final long expiryTimeInMillis;
         private final List<Task> tasks;
         private final List<Future<SortedMap<Long, ArrayStopWatchWritable>>> futures;
         private final Reporter reporter;
         enum COUNTERS { NUM_THREADS};
 
         public ThreadCreatorTimer(final long expiryTimeInMillis, List<Task> tasks, List<Future<SortedMap<Long, ArrayStopWatchWritable>>> futures, Reporter reporter) {
             this.expiryTimeInMillis = expiryTimeInMillis;
             this.tasks = tasks;
             this.futures = futures;
             this.reporter = reporter;
             threadCount = 0;
         }
 
         public void run() {
             LOG.info("About to create " + THREAD_INCREMENT_COUNT + " threads.");
             final ExecutorService executorPool = Executors.newFixedThreadPool(THREAD_INCREMENT_COUNT);
             Collection<MetaStoreWorker> workers = new ArrayList<MetaStoreWorker>(THREAD_INCREMENT_COUNT);
             for (int i = 0; i < THREAD_INCREMENT_COUNT; i++) {
                 workers.add(new MetaStoreWorker(expiryTimeInMillis, tasks));
             }
 
             for (MetaStoreWorker worker : workers) {
                 futures.add(executorPool.submit(worker));
             }
             threadCount += THREAD_INCREMENT_COUNT;
             LOG.info("Current number of threads: " + threadCount);
             reporter.progress();
             reporter.setStatus(MessageFormat.format("#Threads: {0}, Progress: {1}%", threadCount, getProgress()));
             reporter.incrCounter(COUNTERS.NUM_THREADS, THREAD_INCREMENT_COUNT);
         }
 
         public long getProgress() {
             final long startTime = expiryTimeInMillis - MAP_TIMEOUT_MINUTES * 60 * 1000;
             return (System.currentTimeMillis() - startTime) / (expiryTimeInMillis -startTime);
         }
         public int getThreadCount() {
             return threadCount;
         }
     }
 }
