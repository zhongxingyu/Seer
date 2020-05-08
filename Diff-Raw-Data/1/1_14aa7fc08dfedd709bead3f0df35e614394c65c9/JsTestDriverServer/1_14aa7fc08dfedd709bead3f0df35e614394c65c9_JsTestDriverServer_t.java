 /*
  * Copyright 2008 Google Inc.
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
 package com.google.jstestdriver;
 
 import java.io.File;
 import java.util.List;
 import java.util.Observable;
 
 import javax.servlet.Servlet;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.jstestdriver.config.Configuration;
 import com.google.jstestdriver.config.DefaultConfiguration;
 import com.google.jstestdriver.config.YamlParser;
 import com.google.jstestdriver.guice.TestResultPrintingModule;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class JsTestDriverServer extends Observable {
 
   private final Server server = new Server();
 
   private final int port;
   private final CapturedBrowsers capturedBrowsers;
   private final FilesCache filesCache;
   private final URLTranslator urlTranslator;
   private final URLRewriter urlRewriter;
   private Context context;
 
   public JsTestDriverServer(int port, CapturedBrowsers capturedBrowsers,
       FilesCache preloadedFilesCache, URLTranslator urlTranslator,
       URLRewriter urlRewriter) {
     this.port = port;
     this.capturedBrowsers = capturedBrowsers;
     this.filesCache = preloadedFilesCache;
     this.urlTranslator = urlTranslator;
     this.urlRewriter = urlRewriter;
     initJetty(this.port);
     initServlets();
   }
 
   private void initServlets() {
     ForwardingMapper forwardingMapper = new ForwardingMapper();
 
     addServlet("/", new HomeServlet(capturedBrowsers));
     addServlet("/hello", new HelloServlet());
     addServlet("/heartbeat", new HeartbeatServlet(capturedBrowsers));
     addServlet("/capture", new CaptureServlet(new BrowserHunter(
       capturedBrowsers)));
     addServlet("/runner/*", new StandaloneRunnerServlet(new BrowserHunter(
       capturedBrowsers), filesCache, new StandaloneRunnerFilesFilterImpl(),
       new SlaveResourceService(SlaveResourceService.RESOURCE_LOCATION)));
     addServlet("/slave/*", new SlaveResourceServlet(new SlaveResourceService(
       SlaveResourceService.RESOURCE_LOCATION)));
     addServlet("/cmd", new CommandServlet(capturedBrowsers, urlTranslator,
       urlRewriter, forwardingMapper));
     addServlet("/query/*", new BrowserQueryResponseServlet(capturedBrowsers,
       urlTranslator, forwardingMapper));
     addServlet("/fileSet", new FileSetServlet(capturedBrowsers, filesCache));
     addServlet("/cache", new FileCacheServlet());
     addServlet("/test/*", new TestResourceServlet(filesCache));
     addServlet("/forward/*", new ForwardingServlet(forwardingMapper,
       "localhost", port));
   }
 
   private void addServlet(String url, Servlet servlet) {
     context.addServlet(new ServletHolder(servlet), url);
   }
 
   private void initJetty(int port) {
     SocketConnector connector = new SocketConnector();
 
     connector.setPort(port);
     server.addConnector(connector);
     context = new Context(server, "/", Context.SESSIONS);
    context.setMaxFormContentSize(Integer.MAX_VALUE);
   }
 
   public void start() {
     try {
       server.start();
       setChanged();
       notifyObservers(Event.STARTED);
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   public void stop() {
     try {
       server.stop();
       setChanged();
       notifyObservers(Event.STOPPED);
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }
 
   public enum Event {
     STARTED, STOPPED
   }
 
   public static void main(String[] args) {
     try {
       YamlParser parser = new YamlParser(new DefaultPathRewriter());
       Flags flags = new FlagsParser().parseArgument(args);
       File config = new File(flags.getConfig());
       List<Module> modules = Lists.newLinkedList();
       modules.add(new TestResultPrintingModule(System.out, flags.getTestOutput()));
 
       Configuration configuration = new DefaultConfiguration();
       if (flags.hasWork()) {
         if (!config.exists()) {
           throw new RuntimeException("Config file doesn't exist: " + flags.getConfig());
         }
         configuration = parser.parse(config.getParentFile(), new java.io.FileReader(config));
         modules.addAll(new PluginLoader().load(configuration.getPlugins()));
       }
 
       Injector injector = Guice.createInjector(
           new JsTestDriverModule(flags,
               configuration.getFilesList(),
               modules,
               configuration.createServerAddress(flags.getServer(), flags.getPort())));
 
       injector.getInstance(ActionRunner.class).runActions();
     } catch (Exception e) {
       System.err.println(e.getMessage());
       e.printStackTrace(System.err);
       System.exit(1);
     }
   }
 }
