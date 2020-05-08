 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.ui.timeserious;
 
 import com.od.jtimeseries.net.httpd.JTimeSeriesHttpd;
 import com.od.jtimeseries.net.udp.UdpServer;
 import com.od.jtimeseries.ui.config.ConfigAware;
 import com.od.jtimeseries.ui.config.ConfigAwareTreeManager;
 import com.od.jtimeseries.ui.config.ConfigInitializer;
 import com.od.jtimeseries.ui.config.TimeSeriousConfig;
 import com.od.jtimeseries.ui.displaypattern.DisplayNameCalculator;
 import com.od.jtimeseries.ui.timeseries.RemoteHttpTimeSeriesCollection;
 import com.od.jtimeseries.ui.timeserious.action.ApplicationActionModels;
 import com.od.jtimeseries.ui.timeserious.frame.FrameManager;
 import com.od.jtimeseries.ui.timeserious.rootcontext.TimeSeriousRootContext;
 import com.od.jtimeseries.ui.util.JideInitialization;
 import com.od.jtimeseries.ui.util.LocalJmxMetrics;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import od.configutil.util.ConfigManagerException;
 
 import javax.swing.*;
 import java.io.IOException;
 import java.util.Arrays;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 26-Mar-2010
  * Time: 14:15:20
  *
  * Standalone UI for time series exploration
  */
 public class TimeSerious implements ConfigAware {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(TimeSerious.class);
 
     private ConfigInitializer configInitializer = new ConfigInitializer();
     private ApplicationActionModels applicationActionModels = new ApplicationActionModels();
     private UiTimeSeriesServerDictionary udpPingHttpServerDictionary = new UiTimeSeriesServerDictionary();
     private DisplayNameCalculator displayNameCalculator = new DisplayNameCalculator();
     private ConfigAwareTreeManager configTreeManager = new ConfigAwareTreeManager(this);
    private TimeSeriousRootContext rootContext = new TimeSeriousRootContext(
        udpPingHttpServerDictionary,
        displayNameCalculator,
        configTreeManager
    );
 
     private FrameManager frameManager = new FrameManager(
         udpPingHttpServerDictionary,
         applicationActionModels,
         displayNameCalculator,
         rootContext,
         configTreeManager,
         configInitializer
     );
     private TimeSeriousConfig config;
 
     public void start() {
         Runnable r = new StartServicesRunnable();
         new Thread(r, "Startup Services Thread").start();
 
         try {
             config = configInitializer.loadConfig();
             configTreeManager.restoreConfig(config);
         } catch (ConfigManagerException e) {
            logMethods.error("Failed to load config or create default config, cannot start TimeSerious", e);
         }
 
     }
 
     public void prepareConfigForSave(TimeSeriousConfig config) {
     }
 
     public void restoreConfig(TimeSeriousConfig config) {
     }
 
     public java.util.List<ConfigAware> getConfigAwareChildren() {
         return Arrays.asList(displayNameCalculator, rootContext, frameManager, RemoteHttpTimeSeriesCollection.getConfigAware());
     }
 
     public void clearConfig() {
     }
 
 
     public static void main(String[] args) {
 
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
 
                 JideInitialization.applyLicense();
                 JideInitialization.setupJide();
 
                 new LookAndFeelManager().installLookAndFeel();
 
                 new TimeSerious().start();
             }
         });
 
     }
 
     private void startServerForUdpServerUpdates() {
         UdpServer server = new UdpServer(17000);
         server.addUdpMessageListener(udpPingHttpServerDictionary);
         server.startReceive();
     }
 
     private void startJmxAndLocalHttpd() {
         LocalJmxMetrics localJmxMetrics = LocalJmxMetrics.getInstance();
         localJmxMetrics.startJmxManagementService(17001);
         localJmxMetrics.startLocalMetricCollection();
 
         int httpdPort = 17002;
         try {
             JTimeSeriesHttpd httpd = new JTimeSeriesHttpd(httpdPort, localJmxMetrics.getRootContext());
             httpd.start();
         } catch (IOException e) {
             logMethods.error("Could not start timeserious httpd for local metrics", e);
         }
     }
 
 
     private class StartServicesRunnable implements Runnable {
 
         public void run() {
             try {
                 logMethods.info("Starting services");
                 startJmxAndLocalHttpd();
                 startServerForUdpServerUpdates();
                 logMethods.info("Finished starting services");
             } catch (Throwable t) {
                 logMethods.error("Error starting services", t);
             }
         }
     }
 }
