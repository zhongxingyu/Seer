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
 package org.komusubi.feeder.sns.twitter.strategy;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.komusubi.common.util.Resolver;
 import org.komusubi.feeder.model.Message;
 import org.komusubi.feeder.model.Message.Script;
 import org.komusubi.feeder.model.Page;
 import org.komusubi.feeder.model.Topic;
 import org.komusubi.feeder.sns.GateKeeper;
 import org.komusubi.feeder.sns.twitter.Twitter4j;
 import org.komusubi.feeder.sns.twitter.Twitter4jException;
 import org.komusubi.feeder.utils.ResolverUtils.DateResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author jun.ozeki
  */
 public class SleepStrategy implements GateKeeper {
 
     /**
      * 
      * @author jun.ozeki
      */
     public static interface PageCache {
         void refresh();
         boolean exists(Message message);
         void store(Message message);
     }
 
     public static class FilePageCache implements PageCache {
 
         private static final Logger logger = LoggerFactory.getLogger(SleepStrategy.class);
         private static final String CHARSET = "UTF-8";
         private File file;
         private ArrayList<String> items;
         private String lineSeparator = System.getProperty("line.separator");
 
         /**
          * 
          * @param path
          */
         @Inject
         public FilePageCache(@Named("tweet store file")String path) {
             this(new File(path)); 
         }
         
         /**
          * 
          * @param file
          */
         public FilePageCache(File file) {
             this.file = file;
             items = new ArrayList<String>();
         }
 
         /**
          * @see org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.PageCache#refresh()
          */
         @Override
         public void refresh() {
             cache();
             int retainCount = 20;
             if (items.size() <= retainCount)
                 return;
             File tmp;
             try {
                 tmp = File.createTempFile("feeder-store", ".tmp");
             } catch (IOException e) {
                 throw new Twitter4jException(e);
             }
 
             try (BufferedWriter writer = new BufferedWriter(
                                             new OutputStreamWriter(new FileOutputStream(tmp), CHARSET))) {
                 for (int i = items.size() - retainCount; items.size() > i; i++) {
                     writer.write("tweet:");
                     writer.write(items.get(i));
                     writer.write(lineSeparator);
                 }
             } catch (IOException e) {
                 throw new Twitter4jException(e);
             }
             // replace file
             tmp.renameTo(file);
             items.clear();
         }
 
         public List<String> cache() {
 
             if (!file.exists() || items.size() > 0)
                 return items;
 
             try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET))) {
                 String line;
                 StringBuilder builder = new StringBuilder();
                 while ((line = reader.readLine()) != null) {
                     if (line.startsWith("tweet:")) {
                         if (builder.length() > 0) {
                             if (builder.toString().endsWith(lineSeparator))
                                 builder.deleteCharAt(builder.length() - 1);
                             items.add(builder.toString());
                         }
                         builder = new StringBuilder(line.substring("tweet:".length()));
                         builder.append(lineSeparator);
                         continue;
                     }
                     builder.append(line)
                             .append(lineSeparator);
                 }
                 if (builder.length() > 0) {
                     if (builder.toString().endsWith(lineSeparator))
                         builder.deleteCharAt(builder.length() - 1);
                     items.add(builder.toString());
                 }
             } catch (IOException e) {
                 throw new Twitter4jException(e);
             }
             return items;
         }
 
         /**
          * @see org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.PageCache#exists(org.komusubi.feeder.model.Message)
          */
         @Override
         public boolean exists(Message message) {
             
             // found same script to be tweet and history one.
             for (Script script: message) {
                 for (String item: cache()) {
                     if (script.trimedLine().equals(item)) {
                         logger.info("deplicated script found: {}", script.line());
                         return true;
                     }
                 }
             }
             return false;
         }
 
         /**
          * @see org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.PageCache#store(org.komusubi.feeder.model.Message)
          */
         @Override
         public void store(Message message) {
             
             try (BufferedWriter writer = new BufferedWriter(
                                             new OutputStreamWriter(new FileOutputStream(file, true), CHARSET))) {
                 for (Script script: message) {
                     writer.write("tweet:");
                     writer.write(script.trimedLine());
                     writer.write(lineSeparator);
                 }
             } catch (IOException e) {
                 throw new Twitter4jException(e);
             }
         }
         
     }
 
     /**
      * 
      * @author jun.ozeki
      */
     public static class TimelinePageCache implements PageCache {
         private static final Logger logger = LoggerFactory.getLogger(SleepStrategy.class);
         private static final long CACHE_DURATION = 60 * 60 * 1000;
         private Date date;
         private Page page;
         private Resolver<Date> resolver;
         private long cacheDuration;
         private Twitter4j twitter4j;
 
         /**
          * create new instance.
          */
         public TimelinePageCache() {
             this(new Twitter4j(), new DateResolver(), CACHE_DURATION);
         }
 
         /**
          * create new instance.
          * @param twitter4j
          */
         public TimelinePageCache(Twitter4j twitter4j) {
             this(twitter4j, new DateResolver(), CACHE_DURATION);
         }
 
         /**
          * create new instance.
          * @param date
          */
         @Inject
         public TimelinePageCache(Twitter4j twitter4j, Resolver<Date> resolver, @Named("cache duration") long duration) {
             this.twitter4j = twitter4j;
             this.resolver = resolver;
             this.cacheDuration = duration;
             init();
         }
 
         public Page page() {
             return page;
         }
 
         public Date date() {
             return date;
         }
 
         private void init() {
             this.date = resolver.resolve();
             page = twitter4j.history().next();
         }
 
         public boolean outdated() {
             Date current = resolver.resolve();
             if (current.getTime() - date.getTime() > cacheDuration) {
                 return true;
             }
             return false;
         }
 
         /**
          * @return
          */
         public void refresh() {
             if (!outdated())
                 return;
             init();
         }
 
         public boolean exists(Message message) {
             for (Script script: message) {
                 boolean found = false;
                 for (Topic t: page.topics()) {
                     logger.info("script: {}", script.line());
                     logger.info("topic:  {}", t.message().text());
                     if (script.line().equals(t.message().text())) {
                         found = true;
                         break;
                     }
                 }
                 if (!found)
                     return false;
             }
             return true;
         }
 
         /**
          * @see org.komusubi.feeder.sns.twitter.strategy.SleepStrategy.PageCache#store(org.komusubi.feeder.model.Message)
          */
         @Override
         public void store(Message message) {
            // nothing to do 
         }
     }
 
     private long milliSecond;
     private PageCache cache;
 
     /**
      * create new instance.
      */
     public SleepStrategy() {
         this(1);
     }
 
     /**
      * create new instance.
      * @param sleepSecond
      */
     public SleepStrategy(long sleepSecond) {
         this(sleepSecond, new TimelinePageCache());
     }
 
     /**
      * create new instance.
      * @param sleepSecond
      * @param cache
      */
     @Inject
     public SleepStrategy(@Named("tweet sleep interval") long sleepSecond, PageCache cache) {
         this.milliSecond = sleepSecond * 1000;
         this.cache = cache;
     }
 
     // PageCache is NOT static class, because access to twitter4j instance.
 //    private void init(PageCache cache) {
 //        if (cache == null)
 //            this.cache = new PageCache();
 //        else
 //            this.cache = cache;
 //    }
 
     /**
      * @see org.komusubi.feeder.sns.GateKeeper#available()
      */
     @Override
     public boolean available(Message message) {
         boolean result = false;
         cache.refresh();
         if (!cache.exists(message))
             result = true;
         try {
             Thread.sleep(milliSecond);
         } catch (InterruptedException ignore) {
             result = true;
         }
         return result;
     }
 
     /**
      * @see org.komusubi.feeder.sns.GateKeeper#store(org.komusubi.feeder.model.Message)
      */
     @Override
     public void store(Message message) {
         cache.store(message);
     }
 
 }
