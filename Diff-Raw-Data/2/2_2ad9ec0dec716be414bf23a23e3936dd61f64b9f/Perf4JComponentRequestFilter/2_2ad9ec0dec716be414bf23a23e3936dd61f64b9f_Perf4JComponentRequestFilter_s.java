 /*
    Copyright 2011 Kalixia, SARL.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.kalixia.tapestry.perf4j.services;
 
 import org.apache.tapestry5.services.ComponentEventRequestParameters;
 import org.apache.tapestry5.services.ComponentRequestFilter;
 import org.apache.tapestry5.services.ComponentRequestHandler;
 import org.apache.tapestry5.services.PageRenderRequestParameters;
 import org.perf4j.StopWatch;
 import org.perf4j.slf4j.Slf4JStopWatch;
 
 import java.io.IOException;
 
 public class Perf4JComponentRequestFilter implements ComponentRequestFilter {
     public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler) throws IOException {
         StopWatch watch = profilePage(parameters.getActivePageName());
         handler.handleComponentEvent(parameters);
        watch.stop(watch.getTag(), "Component event handled<<<<<");
     }
 
     public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler) throws IOException {
         StopWatch watch = profilePage(parameters.getLogicalPageName());
         handler.handlePageRender(parameters);
         watch.stop(watch.getTag(), "Page rendered");
     }
 
     private StopWatch profilePage(String pageName) {
         String tag = "page." + pageName.replace('/', '.');
         StopWatch watch = new Slf4JStopWatch(tag);
         watch.start();
         return watch;
     }
 }
