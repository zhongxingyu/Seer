 /**
  * Copyright 2010 Mark Nuttall-Smith
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.mns.mojoinvest.server.guice;
 
 import com.google.appengine.tools.appstats.AppstatsFilter;
 import com.google.appengine.tools.appstats.AppstatsServlet;
 import com.google.appengine.tools.mapreduce.MapReduceServlet;
 import com.google.apphosting.utils.remoteapi.RemoteApiServlet;
 import com.google.common.collect.Maps;
 import com.google.inject.Singleton;
 import com.google.inject.servlet.ServletModule;
 import com.googlecode.objectify.ObjectifyFactory;
 import com.mns.mojoinvest.server.engine.model.dao.*;
 import com.mns.mojoinvest.server.mustache.MustacheViewProcessor;
 import com.mns.mojoinvest.server.servlet.*;
 import com.mns.mojoinvest.server.servlet.blob.SuccessfulUploadServlet;
 import com.sun.jersey.api.json.JSONConfiguration;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Mark Nuttall-Smith
  */
 public class MojoServletModule extends ServletModule {
 
     @Override
     public void configureServlets() {
 
         //TODO: Move this stuff to another module
         // Model object managers
         bind(ObjectifyFactory.class).in(Singleton.class);
         bind(QuoteDao.class).to(ObjectifyQuoteDao.class);
         bind(FundDao.class).to(ObjectifyFundDao.class);
         bind(CalculatedValueDao.class).to(ObjectifyCalculatedValueDao.class);
 
         //Filters
         Map<String, String> appstatsInit = Maps.newHashMap();
         appstatsInit.put("logMessage", "Appstats available: /appstats/details?time={ID}");
         filter("/dispatch/*").through(AppstatsFilter.class, appstatsInit);
         bind(AppstatsFilter.class).in(Singleton.class);
 
         Map<String, String> params = new HashMap<String, String>();
         params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
         params.put("com.sun.jersey.config.property.packages", "com.mns.mojoinvest.server.resource");
         params.put(ServletContainer.PROPERTY_WEB_PAGE_CONTENT_REGEX,
                "/(_ah|jsp|css|images|js|lib|mustache|mapreduce|pipeline|upload-success|tools|appstats).*");
         filter("/*").through(GuiceContainer.class, params);
 
         //Servlets
         serve("/remote_api").with(RemoteApiServlet.class);
         bind(RemoteApiServlet.class).in(Singleton.class);
 
         serve("/_ah/pipeline/*").with(com.google.appengine.tools.pipeline.impl.servlets.PipelineServlet.class);
         bind(com.google.appengine.tools.pipeline.impl.servlets.PipelineServlet.class).in(Singleton.class);
 
         serve("/mapreduce/*").with(MapReduceServlet.class);
         bind(MapReduceServlet.class).in(Singleton.class);
 
         serve("/upload-success").with(SuccessfulUploadServlet.class);
         bind(SuccessfulUploadServlet.class).in(Singleton.class);
 
         serve("/pipeline").with(PipelineServlet.class);
         serve("/tools/quoteviewer").with(QuoteViewerServlet.class);
         serve("/tools/fundviewer").with(FundViewerServlet.class);
         serve("/tools/rankingviewer").with(RankingViewerServlet.class);
         serve("/tools/calculator").with(SMACalculatorServlet.class);
         serve("/tools/clearcache").with(ClearCacheServlet.class);
         serve("/tools/fundindexes").with(UpdateFundIndexesServlet.class);
         serve("/tools/test2").with(Test2Servlet.class);
 
         serve("/appstats/*").with(AppstatsServlet.class);
         bind(AppstatsServlet.class).in(Singleton.class);
 
         bind(MustacheViewProcessor.class)
 //                .toInstance(new MustacheViewProcessor("mustache", false));
                 .toInstance(new MustacheViewProcessor("mustache", true));
 
 
     }
 
 }
