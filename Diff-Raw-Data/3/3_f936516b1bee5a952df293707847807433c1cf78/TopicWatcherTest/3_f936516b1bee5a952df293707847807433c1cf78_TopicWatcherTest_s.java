 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.web.scheduler.job;
 
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 import org.junit.After;
 import org.junit.Test;
 import org.quartz.JobDetail;
 import org.quartz.ListenerManager;
 import org.quartz.Scheduler;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 import org.quartz.listeners.JobChainingJobListener;
 
 /**
  * @author jun.ozeki
  */
 public class TopicWatcherTest {
 
 //    @Rule public SchedulerResource resource = new SchedulerResource();
     private Scheduler scheduler;
     
     @After
     public void after() throws Exception {
         if (scheduler != null)
             scheduler.shutdown();
     }
     
     @Test
     public void runJob() throws Exception {
         // setup
         JobDetail job = newJob(TopicWatcher.class)
                                     .withIdentity("topicWatcher", "jal5971")
                                     .storeDurably()
                                     .build();
         JobDetail speak = newJob(TopicSpeaker.class)
                                     .withIdentity("topicSpeaker", "jal5971")
                                     .storeDurably()
                                     .build();
         Trigger trigger = newTrigger()
                                     .startNow()
                                     .withIdentity("weatherTopic", "jal5971")
                                     .withSchedule(simpleSchedule().withIntervalInSeconds(5).repeatForever())
                                     .build();
 //        Scheduler scheduler = resource.scheduler();
         scheduler = StdSchedulerFactory.getDefaultScheduler();
         scheduler.scheduleJob(job, trigger);
 //        scheduler.scheduleJob(speak, trigger);
         scheduler.addJob(speak, true);
         
 //        scheduler.scheduleJob(speak, trigger);
         
         JobChainingJobListener listener = new JobChainingJobListener("chainGrp"); 
         listener.addJobChainLink(job.getKey(), speak.getKey());
         ListenerManager manager = scheduler.getListenerManager();
         manager.addJobListener(listener);
         
         scheduler.start();
         Thread.sleep(60 * 1000);
     }
     
     @Test
     public void chainJob() throws Exception {
         // setup 
         JobDetail watcher = newJob(TopicWatcher.class)
                                 .withIdentity("topicWatcher", "jal5971")
                                 .build();
         JobDetail speaker = newJob(TopicSpeaker.class)
                                 .withIdentity("topicSpeaker", "jal5971")
                                 .build();
     }
 }
