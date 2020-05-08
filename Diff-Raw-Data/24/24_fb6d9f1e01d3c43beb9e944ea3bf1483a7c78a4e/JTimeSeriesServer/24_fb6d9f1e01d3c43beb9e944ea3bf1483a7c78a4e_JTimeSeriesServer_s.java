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
 package com.od.jtimeseries.server;
 
 import com.od.jtimeseries.component.AbstractJTimeSeriesComponent;
 import com.od.jtimeseries.component.jmx.JmxManagementService;
 import com.od.jtimeseries.component.managedmetric.ManagedMetricInitializer;
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.net.httpd.JTimeSeriesHttpd;
 import com.od.jtimeseries.net.udp.HttpServerAnnouncementMessage;
 import com.od.jtimeseries.net.udp.UdpClient;
 import com.od.jtimeseries.net.udp.UdpServer;
 import com.od.jtimeseries.server.jmx.ServerConfigJmx;
 import com.od.jtimeseries.server.message.AppendToSeriesMessageListener;
 import com.od.jtimeseries.server.message.ClientAnnouncementMessageListener;
 import com.od.jtimeseries.server.serialization.TimeSeriesSerializer;
 import com.od.jtimeseries.server.summarystats.SummaryStatisticsCalculator;
 import com.od.jtimeseries.component.util.path.PathMapper;
 import com.od.jtimeseries.util.time.Time;
 import com.sun.jdmk.comm.HtmlAdaptorServer;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import javax.management.ObjectName;
 import java.io.IOException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 16-May-2009
  * Time: 15:32:19
  *
  */
 public class JTimeSeriesServer extends AbstractJTimeSeriesComponent {
 
     public static long startTime = System.currentTimeMillis();
 
     private int serverAnnouncementPingPeriodSeconds = 30;
     private int jmxManagementPort;
     private TimeSeriesContext rootContext;
     private UdpClient udpClient;
     private TimeSeriesSerializer fileSerializer;
     private HttpServerAnnouncementMessage serverAnnouncementMessage;
     private ServerConfigJmx serverConfigJmx;
     private UdpServer udpServer;
     private ManagedMetricInitializer managedMetricInitializer;
     private SummaryStatisticsCalculator summaryStatisticsCalculator;
     private HtmlAdaptorServer htmlAdaptorServer;
     private JTimeSeriesHttpd httpdServer;
     private PathMapper pathMapper;
 
     static {
         initialize(JTimeSeriesServer.class);
     }
 
     public JTimeSeriesServer() {}
 
     private void startup() {
         logMethods.logInfo("Starting JTimeSeriesServer");
         try {
             doStartup();
             logMethods.logInfo("JTimeSeriesServer is up. Time taken to start was " + serverConfigJmx.getSecondsToStartServer() + " seconds");
         } catch ( Throwable t) {
             logMethods.logError("Error starting JTimeseriesServer", t);
         }
     }
 
     private void doStartup() throws IOException {
        setupServerMetrics();
         startSeriesDirectoryManager();
         new JmxManagementService().startJmxManagementService(jmxManagementPort);
         startSummaryStats();
         startUdpServer();
         startServerAnnouncementPings();
         startJmx();
         startTimeSeriesHttpServer();

         serverConfigJmx.setSecondsToStartServer((int)(System.currentTimeMillis() - startTime) / 1000);
     }
 
     private void startServerAnnouncementPings() {
         logMethods.logInfo("Starting Server Announcement Pings");
         udpClient.sendRepeatedMessage(serverAnnouncementMessage, Time.seconds(serverAnnouncementPingPeriodSeconds));
     }
 
     private void startUdpServer() {
         logMethods.logInfo("Adding UDP message listeners");
         udpServer.addUdpMessageListener(new AppendToSeriesMessageListener(rootContext, pathMapper));
         udpServer.addUdpMessageListener(new ClientAnnouncementMessageListener(udpClient));
         logMethods.logInfo("Starting UDP server on port " + udpServer.getPort());
         udpServer.startReceive();
     }
 
     private void setupServerMetrics() {
         logMethods.logInfo("Setting up server metrics series");
         managedMetricInitializer.initializeServerMetrics();
 
         //start scheduling for any series (e.g server metrics) which require it
        rootContext.startScheduling().startDataCapture();
     }
 
     private void startSummaryStats() {
         logMethods.logInfo("Starting summary stats");
         summaryStatisticsCalculator.start();
     }
 
     private void startSeriesDirectoryManager() {
         logMethods.logInfo("Starting Series Directory Manager");
         SeriesDirectoryManager seriesDirectoryManager = (SeriesDirectoryManager)ctx.getBean("seriesDirectoryManager");
         seriesDirectoryManager.removeOldTimeseriesFiles();
         seriesDirectoryManager.loadExistingSeries();
     }
 
     private void startJmx() {
         logMethods.logInfo("Starting JMX Html Adapter Interface on port " + htmlAdaptorServer.getPort());
         try {
             MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer();
 
             ObjectName configMBeanName = new ObjectName("JTimeSeriesServerConfig:name=JTimeSeriesServerConfig");
             mBeanServer.registerMBean(serverConfigJmx, configMBeanName);
             mBeanServer.registerMBean(htmlAdaptorServer, new ObjectName("adaptor:protocol=HTTP"));
 
             htmlAdaptorServer.start();
         } catch (Exception e) {
             logMethods.logError("Failed to start JMX interface", e);
         }
     }
 
     private void startTimeSeriesHttpServer() throws IOException {
         logMethods.logInfo("Starting HTTP Server on port " + httpdServer.getPort());
         httpdServer.setRequestMonitor(new ServerHttpRequestMonitor());
         httpdServer.start();
     }
 
     public void setServerAnnouncementPingPeriodSeconds(int serverAnnouncementPingPeriodSeconds) {
         this.serverAnnouncementPingPeriodSeconds = serverAnnouncementPingPeriodSeconds;
     }
 
     public void setRootContext(TimeSeriesContext rootContext) {
         this.rootContext = rootContext;
     }
 
     public void setUdpClient(UdpClient udpClient) {
         this.udpClient = udpClient;
     }
 
     public void setFileSerializer(TimeSeriesSerializer timeseriesSerializer) {
         this.fileSerializer = timeseriesSerializer;
     }
 
     public void setServerAnnouncementMessage(HttpServerAnnouncementMessage announceMessage) {
         this.serverAnnouncementMessage = announceMessage;
     }
 
     public void setServerConfigJmx(ServerConfigJmx serverConfigJmx) {
         this.serverConfigJmx = serverConfigJmx;
     }
 
     public void setUdpServer(UdpServer udpServer) {
         this.udpServer = udpServer;
     }
 
     public void setManagedMetricInitializer(ManagedMetricInitializer metricInitializer) {
         this.managedMetricInitializer = metricInitializer;
     }
 
     public void setSummaryStatisticsCalculator(SummaryStatisticsCalculator summaryStatisticsCalculator) {
         this.summaryStatisticsCalculator = summaryStatisticsCalculator;
     }
 
     public void setHtmlAdaptorServer(HtmlAdaptorServer htmlAdaptorServer) {
         this.htmlAdaptorServer = htmlAdaptorServer;
     }
 
     public void setHttpdServer(JTimeSeriesHttpd httpdServer) {
         this.httpdServer = httpdServer;
     }
 
     public void setJmxManagementPort(int jmxManagementPort) {
         this.jmxManagementPort = jmxManagementPort;
     }
 
     public void setPathMapper(PathMapper pathMapper) {
         this.pathMapper = pathMapper;
     }
 
     public static void main(String[] args) throws IOException {
         JTimeSeriesServer server = (JTimeSeriesServer)ctx.getBean("timeSeriesServer");
         server.startup();
     }
 }
