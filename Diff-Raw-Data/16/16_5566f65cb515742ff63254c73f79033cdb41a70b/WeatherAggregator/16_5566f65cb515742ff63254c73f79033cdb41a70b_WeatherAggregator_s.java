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
 package org.komusubi.feeder.aggregator;
 
 import org.komusubi.feeder.aggregator.scraper.WeatherAnnouncementScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherTopicScraper;
 import org.komusubi.feeder.aggregator.topic.WeatherTopic;
 import org.komusubi.feeder.aggregator.topic.WeatherTopics;
 import org.komusubi.feeder.aggregator.topic.WeatherTopics.Announcement;
 import org.komusubi.feeder.aggregator.topic.WeatherTopics.Title;
 import org.komusubi.feeder.model.Topics;
 
 /**
  * @author jun.ozeki
  */
 public class WeatherAggregator implements Aggregator {
 
     private WeatherTopicScraper topicScraper;
     private WeatherAnnouncementScraper announceScraper;
     private WeatherTitleScraper titleScraper;
     
     /*
      * create new instance.
      * default constructor.
      */
 //    public WeatherAggregator(Scraper scraper...) {
 //        this(new WeatherTopicScraper(), new WeatherAnnouncementScraper(), new WeatherTitleScraper());
 //    }
     
     /**
      * create new instance.
      * @param scraper
      * @param announceScraper
      * @param titleScraper
      */
     public WeatherAggregator(WeatherTopicScraper scraper, 
                             WeatherAnnouncementScraper announceScraper,
                             WeatherTitleScraper titleScraper) {
         this.topicScraper = scraper;
         this.announceScraper = announceScraper;
         this.titleScraper = titleScraper;
     }
     
     /**
      * @see org.komusubi.feeder.aggregator.Aggregator#aggregate()
      */
     @Override
     public Topics aggregate() {
         
        WeatherTopics topics = new WeatherTopics();
         for (Announcement announce: announceScraper) {
 //            topics.add()
         }
         for (WeatherTopic topic: topicScraper) {
             topics.add(topic);
         }
         for (Title title: titleScraper) {
             
         }
         return topics;
     }
 }
