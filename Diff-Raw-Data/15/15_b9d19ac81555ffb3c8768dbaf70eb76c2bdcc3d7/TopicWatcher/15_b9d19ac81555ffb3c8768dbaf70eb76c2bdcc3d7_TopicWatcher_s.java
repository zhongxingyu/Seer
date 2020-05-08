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
 
 import org.komusubi.feeder.aggregator.topic.WeatherTopics;
 import org.komusubi.feeder.model.Topics;
import org.komusubi.feeder.sns.twitter.spi.TweetMessageProvidier;
 import org.quartz.Job;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author jun.ozeki
  * @deprecated use TopicSpeaker instend of this.
  */
 @Deprecated 
 public class TopicWatcher implements Job {
     
     private static final Logger logger = LoggerFactory.getLogger(TopicWatcher.class);
     private Topics topics;
     
     /**
      * create new instance.
      * default constructor for quartz library.
      */
     public TopicWatcher() {
        this(new WeatherTopics(new TweetMessageProvidier()));
     }
 
     /**
      * create new instance.
      * @param talker
      * @param topics
      */
     public TopicWatcher(Topics topics) {
         this.topics = topics;
     }
     
     /**
      * job start.
      * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
      */
     @Override
     public void execute(JobExecutionContext context) throws JobExecutionException {
 //        logger.info("job start");
         logger.info("トピック監視処理開始");
         JobDataMap map = context.getJobDetail().getJobDataMap();
         if (!map.containsKey("TOPIC_KEY")) {
             map.put("TOPIC_KEY", "test");
         }
 //        Message message = topics.message();
 //        if (message.size() > 0) {
             
 //        }
 //        context.getJobDetail().getJobDataMap().
         
     }
 
 }
