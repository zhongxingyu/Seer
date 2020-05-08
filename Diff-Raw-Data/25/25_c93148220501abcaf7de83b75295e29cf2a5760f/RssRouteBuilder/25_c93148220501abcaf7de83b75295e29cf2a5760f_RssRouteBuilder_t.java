 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.mysocio.data.camel.routes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mysocio.data.management.DataManagerFactory;
 import net.mysocio.data.rss.RssMessage;
 import net.mysocio.sources.rss.RssSource;
 
 import org.apache.camel.Body;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.spring.Main;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 /**
  * A simple example router from a file system to an ActiveMQ queue and then to a file system
  *
  * @version $Revision: 813332 $
  */
 public class RssRouteBuilder extends RouteBuilder {
 	private static final Logger logger = LoggerFactory.getLogger(RssRouteBuilder.class);
     /**
      * Allow this route to be run as an application
      *
      * @param args
      * @throws Exception 
      */
     public static void main(String[] args) throws Exception {
         new Main().run(args);
     }
 
     public void configure() {
     	List<RssSource> sources = DataManagerFactory.getDataManager().getObjects(RssSource.class);
     	for (RssSource source : sources) {
     		if (logger.isDebugEnabled()){
     			logger.debug("Creating route for RSS feed on url" + source.getUrl());
     		}
    		from("rss:" + source.getUrl() + "?consumer.delay=2000").
             bean(new RssMessagesRidingBean(source.getId()));
 		}
         
     }
 
     public static class RssMessagesRidingBean {
     	private String id;
 
         public RssMessagesRidingBean(String id) {
         	this.id = id;
 		}
 
 		public void readMessages(@Body SyndFeed feed) {
         	List<SyndEntryImpl> entries = feed.getEntries();
         	List<RssMessage> messages = new ArrayList<RssMessage>();
         	if (logger.isDebugEnabled()){
     			logger.debug("Got " + entries.size() + " messages for feed: " + feed.getUri());
     		}
         	for (SyndEntryImpl entry : entries) {
         		RssMessage message = new RssMessage(entry.getLink());
         		message.setSourceId(id);
         		message.setDate(entry.getPublishedDate().getTime());
         		String title = entry.getTitle();
 				message.setTitle(title);
         		String text = entry.getDescription().getValue();
 				message.setText(text);
         		if (logger.isDebugEnabled()){
         			logger.debug("Message title: " + title);
         			logger.debug("Message text: " + text);
         		}
 			}
         	DataManagerFactory.getDataManager().saveObjects(messages);
         }
     }
 }
