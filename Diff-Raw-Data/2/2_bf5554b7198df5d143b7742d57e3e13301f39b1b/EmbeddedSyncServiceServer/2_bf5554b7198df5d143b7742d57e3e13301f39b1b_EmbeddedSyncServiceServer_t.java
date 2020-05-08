 package de.consistec.syncframework.server;
 
 /*
  * #%L
  * doppelganger
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import de.consistec.syncframework.common.TableSyncStrategies;
 import de.consistec.syncframework.impl.commands.RequestCommand;
 import de.consistec.syncframework.impl.proxy.http_servlet.SyncAction;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.mortbay.jetty.testing.ServletTester;
 
 /**
  * This represents an instance of the Jetty
  * servlet container so that we can start and stop it.
  *
  * @author marcel
  * @company consistec Engineering and Consulting GmbH
  * @date 25.01.13 13:21
  */
 public class EmbeddedSyncServiceServer {
 
     public static final String CONFIG_FILE = "/test_syncframework.properties";
 
 
     private static ServletTester tester;
     private static String baseUrl;
     private boolean debugEnabled;
     private ContextListenerMock listener;
 
     public void init() throws Exception {
         tester = new ServletTester();
         tester.setContextPath("/");
        tester.setResourceBase("./apps/server/SyncServer/target/test-classes/server-tests");
         System.out.println("+++++++++++++++++++++++");
         System.out.println(tester.getResourceBase());
         System.out.println("+++++++++++++++++++++++");
         listener = new ContextListenerMock(CONFIG_FILE);
         tester.addEventListener(listener);
         tester.addServlet(SyncServiceServlet.class, "/SyncServer/SyncService");
         baseUrl = tester.createSocketConnector(true);
     }
 
     public void start() throws Exception {
         tester.start();
     }
 
     public void stop() throws Exception {
         tester.stop();
     }
 
     public URI getServerURI() throws URISyntaxException {
         return new URI(baseUrl + "/SyncServer/SyncService");
     }
 
     public void addRequest(SyncAction action, RequestCommand request) {
         listener.addRequest(action, request);
     }
 
     public void setDebugEnabled(final boolean debugEnabled) {
         listener.setDebugEnabled(debugEnabled);
     }
 
     public void setTableSyncStrategies(TableSyncStrategies tableSyncStrategies) {
         listener.setTableSyncStrategies(tableSyncStrategies);
     }
 }
