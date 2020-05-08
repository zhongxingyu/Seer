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
 package net.mysocio.routes.lj;
 
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mysocio.connection.readers.ISource;
 import net.mysocio.connection.readers.lj.LjSource;
 import net.mysocio.data.lj.LjMessage;
 import net.mysocio.data.management.DataManagerFactory;
import net.mysocio.data.management.HibernateDataManager;
 
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
 public class LjRouteBuilder extends RouteBuilder {
 	private static final Logger logger = LoggerFactory.getLogger(LjRouteBuilder.class);
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
     	List<ISource> sources = DataManagerFactory.getDataManager().getSources(LjSource.class);
     	for (ISource source : sources) {
     		from("rss:" + source.getUrl() + "?consumer.delay=100").
             bean(new SomeBean(source.getId()));
 		}
         
     }
 
     public static class SomeBean {
     	private Long id;
 
         public SomeBean(Long id) {
         	this.id = id;
 		}
 
 		public void someMethod(@Body SyndFeed feed) {
         	List<SyndEntryImpl> entries = feed.getEntries();
         	List<LjMessage> messages = new ArrayList<LjMessage>();
         	for (SyndEntryImpl entry : entries) {
         		LjMessage message = new LjMessage(entry.getLink());
         		message.setSourceId(id);
         		message.setPublishedDate(entry.getPublishedDate().getTime());
         		message.setTitle(entry.getTitle());
         		message.setText(entry.getDescription().getValue());
 			}
 //        	DataManager.saveObjects(messages);
         	System.out.println(id.toString());
         }
     }
 }
