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
 package com.google.jstestdriver.server;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.jstestdriver.annotations.MaxFormContentSize;
 import com.google.jstestdriver.annotations.Port;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.servlet.Context;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.servlet.GzipFilter;
 
 import javax.servlet.Servlet;
 
 /**
  * Sippin' on Jetty and Guice.
  *
  * @author rdionne@google.com (Robert Dionne)
  */
 public class JettyModule extends AbstractModule {
 
   private final int port;
   private final HandlerPathPrefix handlerPrefix;
 
   public JettyModule(int port, HandlerPathPrefix handlerPrefix) {
     this.port = port;
     this.handlerPrefix = handlerPrefix;
   }
 
   @Override
   protected void configure() {
     bindConstant().annotatedWith(Port.class).to(port);
     bindConstant().annotatedWith(MaxFormContentSize.class).to(Integer.MAX_VALUE);
   }
 
   @Provides @Singleton SocketConnector provideSocketConnector(@Port Integer port) {
     SocketConnector connector = new SocketConnector();
     connector.setPort(port);
     return connector;
   }
 
   @Provides @Singleton ServletHolder servletHolder(Servlet handlerServlet) {
     return new ServletHolder(handlerServlet);
   }
 
   @Provides @Singleton Server provideJettyServer(
       SocketConnector connector,
       @MaxFormContentSize Integer maxFormContentSize,
       ServletHolder servletHolder) {
     Server server = new Server();
     server.setGracefulShutdown(1);
     server.addConnector(connector);
 
     Context context = new Context(server, "/", Context.SESSIONS);
     context.setMaxFormContentSize(maxFormContentSize);
 
     context.addFilter(GzipFilter.class, handlerPrefix.prefixPath("/test/*"), Handler.DEFAULT);
     // TODO(rdionne): Fix HttpServletRequest#getPathInfo() provided by RequestHandlerServlet.
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/cache"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/capture/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/cmd"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/favicon.ico"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/fileSet"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/forward/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/heartbeat"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/hello"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/auth", "jstd"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/proxy/*", "jstd"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/log"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/query/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/runner/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/slave/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/test/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/quit"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/quit/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/static/*"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/bcr"));
     context.addServlet(servletHolder, handlerPrefix.prefixPath("/bcr/*"));
     context.addServlet(servletHolder, "/*");
 
     return server;
   }
 }
