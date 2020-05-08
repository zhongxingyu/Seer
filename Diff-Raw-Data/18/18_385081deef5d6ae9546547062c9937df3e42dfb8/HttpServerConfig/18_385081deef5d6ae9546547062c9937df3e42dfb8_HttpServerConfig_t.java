 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.httpserver;
 
 import java.io.File;
 import java.util.concurrent.TimeUnit;
 
 import org.skife.config.Config;
 import org.skife.config.Default;
 import org.skife.config.DefaultNull;
 import org.skife.config.TimeSpan;
 
 public abstract class HttpServerConfig
 {
     @Config("ness.httpserver.class")
     @Default("com.nesscomputing.httpserver.GalaxyJetty8HttpServer")
     public Class<? extends HttpServer> getServerClass()
     {
         return GalaxyJetty8HttpServer.class;
     }
 
     /**
      * Use the X-Forwarded-For headers to set remote host, port etc. instead of
      * the host connecting.
      */
     @Config("ness.httpserver.forwarded")
     @Default("false")
     public boolean isForwarded()
     {
         return false;
     }
 
     @Config("ness.httpserver.max-threads")
     @Default("30")
     public int getMaxThreads()
     {
         return 30;
     }
 
     @Config("ness.httpserver.min-threads")
     @Default("10")
     public int getMinThreads()
     {
         return 10;
     }
 
     @Config("ness.httpserver.thread-max-idletime")
     @Default("30000")
     public int getThreadMaxIdletime()
     {
         return 30000;
     }
 
     @Config("ness.httpserver.max-idletime")
     @Default("30000")
     public int getMaxIdletime()
     {
         return 30000;
     }
 
     @Config("ness.httpserver.shutdown-timeout")
    @DefaultNull
     public TimeSpan getShutdownTimeout()
     {
     	return new TimeSpan(1, TimeUnit.SECONDS);
     }
 
     @Config("ness.httpserver.ssl-keystore")
     @DefaultNull
     public String getSSLKeystorePath()
     {
         return null;
     }
 
     @Config("ness.httpserver.ssl-keystore-type")
     @Default("JKS")
     public String getSSLKeystoreType()
     {
         return "JKS";
     }
 
     @Config("ness.httpserver.ssl-keystore-password")
     @Default("changeit")
     public String getSSLKeystorePassword()
     {
         return "changeit";
     }
 
     @Config("ness.httpserver.header-size")
     @Default("16384")
     public int getResponseHeaderSize() {
         return 16384;
     }
 
     @Config("ness.httpserver.jmx-enabled")
     @Default("false")
     public boolean isJmxEnabled() {
         return false;
     }
 
     /**
      * Sets the location on the filesystem that is returned as the base
      * path from servlets and static content handlers.
      */
     @Config("ness.httpserver.servlet-context-basepath")
     @DefaultNull
     public File getServletContextBasePath()
     {
         return null;
     }
 }
