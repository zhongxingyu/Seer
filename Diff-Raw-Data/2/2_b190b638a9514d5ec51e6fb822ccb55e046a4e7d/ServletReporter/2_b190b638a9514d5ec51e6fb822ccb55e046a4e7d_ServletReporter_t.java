 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.servlet.report;
 
 import com.crashnote.logger.report.LoggerReporter;
 import com.crashnote.servlet.collect.*;
 import com.crashnote.servlet.config.ServletConfig;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * Customized implementation of the core {@link LoggerReporter}. Adds servlet-specific functionality.
  */
 public class ServletReporter<C extends ServletConfig>
     extends LoggerReporter<C> {
 
     private final RequestCollector reqCollector;
     private final SessionCollector sesCollector;
 
     // SETUP ======================================================================================
 
     public ServletReporter(final C config) {
         super(config);
         this.reqCollector = new RequestCollector(config);
         this.sesCollector = new SessionCollector(config);
     }
 
     // INTERFACE ==================================================================================
 
     /**
      * Before each request, start the internal log session
      *
      * @param request  the HTTP request
      */
     public void beforeRequest(final ServletRequest request) {
         startSession();
     }
 
     /**
      * In case of an uncaught exception, process it
      *
      * @param request the HTTP request
     * @param t       the thread where the error occurred
      * @param th      the exception details
      */
     public void uncaughtException(final ServletRequest request, final Thread t, final Throwable th) {
         uncaughtException(t, th);
     }
 
     /**
      * After each request, finish the internal log session
      *
      * @param request  the HTTP request
      * @param response the HTTP response
      */
     public void afterRequest(final ServletRequest request, final ServletResponse response) {
         put("request", reqCollector.collect((HttpServletRequest) request));
         put("session", sesCollector.collect((HttpServletRequest) request));
 
         endSession();
     }
 
     // SHARED =====================================================================================
 
     /**
      * Do not flush reports out immediately - wait till the session has finished
      */
     @Override
     protected boolean isAutoFlush() {
         return false;
     }
 }
