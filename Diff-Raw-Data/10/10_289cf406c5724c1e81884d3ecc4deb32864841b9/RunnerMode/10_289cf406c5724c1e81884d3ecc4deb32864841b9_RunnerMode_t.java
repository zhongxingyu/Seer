 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.runner;
 
 import com.google.jstestdriver.util.LogConfigBuilder;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 public enum RunnerMode {
   DEBUG(new LogConfigBuilder()
             .useFileHandler()
             .useConsoleHandler()
             .finest("com.google.javascript.jstestdriver")
             .finest("com.google.jstestdriver")
             .severe("org.mortbay"), true),
  DEBUG_OBSERVE(new LogConfigBuilder()
             .useFileHandler()
             .useConsoleHandler()
             .fine("com.google.javascript.jstestdriver")
             .fine("com.google.jstestdriver")
             .severe("org.mortbay"), false),
   DEBUG_QUIET(new LogConfigBuilder()
             .useFileHandler()
             .finest("com.google.javascript.jstestdriver")
             .finest("com.google.jstestdriver")
             .severe("org.mortbay"), true),
   DEBUG_NO_TRACE(new LogConfigBuilder()
                  .useConsoleHandler()
                  .fine("com.google.javascript.jstestdriver")
                  .fine("com.google.testing.jsunitfarm")
                  .fine("com.google.jstestdriver")
                  .severe("org.mortbay"), true),
   DEBUG_JETTY(new LogConfigBuilder()
                  .useConsoleHandler()
                  .fine("com.google.javascript.jstestdriver")
                  .fine("com.google.testing.jsunitfarm")
                  .fine("com.google.jstestdriver")
                  .fine("org.mortbay"), true),
   PROFILE(new LogConfigBuilder()
             .useFileHandler()
             .useConsoleHandler()
             .info("com.google.jstestdriver.util.StopWatch")
             .severe("com.google.javascript.jstestdriver")
             .severe("com.google.jstestdriver")
             .severe("org.mortbay"), false),
   INFO(new LogConfigBuilder()
             .useConsoleHandler()
             .info("com.google.javascript.jstestdriver")
             .info("com.google.jstestdriver")
             .severe("org.mortbay"), false),
   QUIET(new LogConfigBuilder()
             .useFileHandler()
             .warn("com.google.javascript.jstestdriver")
             .warn("com.google.jstestdriver")
             .severe("org.mortbay"), false);
 
   private final LogConfigBuilder configBuilder;
   private final boolean isDebug;
 
   private RunnerMode(LogConfigBuilder configBuilder, boolean isDebug) {
     this.configBuilder = configBuilder;
     this.isDebug = isDebug;
   }
   
   public InputStream getLogConfig() {
     return new ByteArrayInputStream(configBuilder.build().getBytes());
   }
 
   public boolean isDebug() {
     return isDebug;
   }
 }
