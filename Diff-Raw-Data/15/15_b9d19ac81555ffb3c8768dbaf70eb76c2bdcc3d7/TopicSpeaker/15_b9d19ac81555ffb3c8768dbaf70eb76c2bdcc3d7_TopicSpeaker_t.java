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
 
 import javax.inject.Inject;
 
 import org.komusubi.feeder.aggregator.topic.WeatherTopics;
 import org.komusubi.feeder.model.Message;
 import org.komusubi.feeder.model.Topics;
 import org.komusubi.feeder.sns.SocialNetwork;
 import org.komusubi.feeder.sns.Speaker;
 import org.komusubi.feeder.sns.twitter.Twitter4j;
import org.komusubi.feeder.sns.twitter.spi.TweetMessageProvider;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author jun.ozeki
  */
 public class TopicSpeaker implements Job {
 
     private static final Logger logger = LoggerFactory.getLogger(TopicSpeaker.class);
     private Speaker speaker;
     private Topics topics;
 
     /**
      * carete new instance.
      */
     public TopicSpeaker() {
         // default implement is twitter4j.
        this(new Twitter4j(), new WeatherTopics(new TweetMessageProvider()));
     }
 
     /**
      * create new instance.
      * @param socialNetwork
      */
     @Inject
     public TopicSpeaker(SocialNetwork socialNetwork, Topics topics) {
         this(new Speaker(socialNetwork), topics);
     }
 
     /**
      * create new instance.
      * @param speaker
      */
     public TopicSpeaker(Speaker speaker, Topics topics) {
         this.speaker = speaker;
         this.topics = topics;
     }
 
     /**
      * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
      */
     @Override
     public void execute(JobExecutionContext context) throws JobExecutionException {
         logger.info("つぶやき処理開始");
         Message message = topics.message();
         if (message.size() <= 0) 
             return;
         Message msg = speaker.extract(message);
         speaker.talk(msg);
     }
 
 }
