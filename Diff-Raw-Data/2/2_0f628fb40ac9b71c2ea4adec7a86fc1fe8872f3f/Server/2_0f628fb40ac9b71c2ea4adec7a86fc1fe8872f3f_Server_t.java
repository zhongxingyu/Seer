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
 package com.google.eclipse.javascript.jstestdriver.core;
 
 import static java.lang.String.format;
 
 import com.google.common.collect.Maps;
 import com.google.eclipse.javascript.jstestdriver.core.model.SlaveBrowserRootData;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.DefaultURLRewriter;
 import com.google.jstestdriver.DefaultURLTranslator;
 import com.google.jstestdriver.FileInfo;
 import com.google.jstestdriver.FilesCache;
 import com.google.jstestdriver.ServerStartupAction;
 
 /**
  * TODO: This should not use CaptureBrowsers and ServerStartupAction directly but go through the
  * IDEPluginActionBuilder
  *
  * Server responsible for starting a singleton instance of the JSTestDriver Server. Also has a
  * handle on the captured slave browsers.
  *
  * @author shyamseshadri@gmail.com (Shyam Seshadri)
  */
 public class Server {
 
   /**
    * URL Format of the server. Needs to be injected with an integer port number.
    */
   private static final String SERVER_URL_FORMAT = "http://localhost:%d";
 
   /**
    * URL Format of the browser capture url. Needs to be injected with a integer port number.
    */
   private static final String SERVER_CAPTURE_URL_FORMAT = "http://localhost:%d/capture";
 
   private final ServerStartupAction startupAction;
   private final CapturedBrowsers capturedBrowsers;
   private boolean started = false;
 
   private static volatile Server instance;
   private final int port;
 
   /**
    * Creates a Server at the given port and returns it the first time. Every call after the first
    * returns the instance created initially, ignoring the port value passed to it.
    *
    * @param port the integer port at which to create the server on.
    * @return an initialized server.
    */
   public static Server getInstance(int port) {
     // TODO(shyamseshadri): Handle the port ugliness separately. Try to move to a nicer singleton
     // pattern.
     if (instance == null) {
       synchronized (Server.class) {
         if (instance == null) {
           instance = new Server(port);
         }
       }
     }
     return instance;
   }
 
   private Server(int port) {
     this.port = port;
     capturedBrowsers = new CapturedBrowsers();
     this.startupAction =
       new ServerStartupAction(port, capturedBrowsers, new FilesCache(
           Maps.<String, FileInfo>newHashMap()),
           new DefaultURLTranslator(),
           new DefaultURLRewriter());
   }
 
   /**
    * Gets the singleton instance of the Server, {@code null} if not initialized yet.
    * @return the singleton instance of the server.
    */
   public static Server getInstance() {
     return instance;
   }
 
   /**
    * @return the captured browsers
    */
   public CapturedBrowsers getCapturedBrowsers() {
     synchronized (this) {
       return capturedBrowsers;
     }
   }
 
   /**
    * @return true if the server has been started
    */
   public synchronized boolean isStarted() {
     return started;
   }
 
   /**
    * @return if there is at least one captured browser to run the tests on
    */
   public boolean isReadyToRunTests() {
     return capturedBrowsers.getSlaveBrowsers().size() > 0;
   }
 
   /**
    * Starts the JS Test Driver server
    */
   public synchronized void start() {
     if (!started) {
      startupAction.run(null);
       started = true;
     }
   }
 
   public String getCaptureUrl() {
     return format(SERVER_CAPTURE_URL_FORMAT, port);
   }
 
   public String getServerUrl() {
     return format(SERVER_URL_FORMAT, port);
   }
 
   /**
    * Stops the JS Test Driver server
    */
   public synchronized void stop() {
     if (started) {
       startupAction.getServer().stop();
       // TODO(shyamseshadri): captured browsers should have a clear method
       capturedBrowsers.getSlaveBrowsers().clear();
       capturedBrowsers.getBrowsers().clear();
       SlaveBrowserRootData.getInstance().clear();
       started = false;
     }
   }
 }
