 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver.eclipse.core;
 
 import java.util.HashMap;
 
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FilesCache;
 import com.google.jstestdriver.ServerStartupAction;
 
 public class Server {
 
   private ServerStartupAction startupAction;
   public static final int DEFAULT_SERVER_PORT = 4224;
   public static final String SERVER_URL = "http://localhost:%d";
   public static final String SERVER_CAPTURE_URL = "http://localhost:%d/capture";
   private final CapturedBrowsers capturedBrowsers;
   private final int port;
   private boolean started = false;
  
   private static Server instance = null;
   
   private Server(int port) {
     this.port = port;
     capturedBrowsers = new CapturedBrowsers();
   }
  
   public static Server getInstance() {
     return instance;
   }
   
   public CapturedBrowsers getCapturedBrowsers() {
     return capturedBrowsers;
   }
   
   public boolean isStarted() {
     return started;
   }
   
   public boolean isReadyToRunTests() {
     return capturedBrowsers.getSlaveBrowsers().size() > 0;
   }
 
   public void start() {
     // TODO(shyamseshadri): Get rid of this and use the builder when we have something substantial.
     startupAction = new ServerStartupAction(port, capturedBrowsers,
        new FilesCache(new HashMap<String, FileInfo>()));
     startupAction.run();
     started = true;
   }
 
   public void stop() {
     startupAction.getServer().stop();
     capturedBrowsers.getSlaveBrowsers().clear();
     capturedBrowsers.getBrowsers().clear();
     SlaveBrowserRootData.getInstance().clear();
     started = false;
   }
 
   public static Server createInstance(int port) {
     if (instance == null) {
       instance = new Server(port);
     }
     return instance;
   }
 }
