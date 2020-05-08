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
 package org.komusubi.feeder.web;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.komusubi.feeder.aggregator.scraper.HtmlScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherAnnouncementScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherTitleScraper;
 import org.komusubi.feeder.aggregator.scraper.WeatherTopicScraper;
 import org.komusubi.feeder.aggregator.site.WeatherTopicSite;
 import org.komusubi.feeder.aggregator.topic.WeatherTopics;
 import org.komusubi.feeder.model.Site;
 import org.komusubi.feeder.sns.SocialNetwork;
 import org.komusubi.feeder.sns.Speaker;
 import org.komusubi.feeder.sns.twitter.Twitter4j;
 import org.komusubi.feeder.sns.twitter.spi.TweetMessageProvider;
 import org.komusubi.feeder.web.scheduler.QuartzModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.guice.JerseyServletModule;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 /**
  * bootstrap.
  * @author jun.ozeki
  */
 public class Bootstrap extends GuiceServletContextListener {
 
     /**
      * @see com.google.inject.servlet.GuiceServletContextListener#getInjector()
      */
     @Override
     protected Injector getInjector() {
         return buildInjector();
     }
 
     /**
      * build injector this module.
      * @return
      */
     public Injector buildInjector() {
         return Guice.createInjector(new WebModule(),
                                     new Jal5971Module(),
                                     new QuartzModule());
     }
 
     /**
      * servlet module.
      * @author jun.ozeki
      */
     public static class WebModule extends JerseyServletModule {
         
         /**
          * @see com.google.inject.servlet.ServletModule#configureServlets()
          */
         @Override
         protected void configureServlets() {
             Map<String, String> param = new HashMap<String, String>();
             param.put(PackagesResourceConfig.PROPERTY_PACKAGES, Jal5971Resource.class.getPackage().getName());
             serve("/*").with(GuiceContainer.class, param);
             bind(Jal5971Resource.class);
         }
     }
     
     /**
      * jal5971 module.
      * @author jun.ozeki
      */
     public static class Jal5971Module extends AbstractModule {
 
         @Override
         protected void configure() {
            bind(Speaker.class);
             bind(SocialNetwork.class).to(Twitter4j.class);
            bind(WeatherTopics.class);
             bind(WeatherTopicScraper.class);
             bind(WeatherAnnouncementScraper.class);
             bind(WeatherTitleScraper.class);
            bind(TweetMessageProvider.class);
             bind(HtmlScraper.class);
             bind(Site.class).to(WeatherTopicSite.class);
         }
         
     }
 }
