 /*
  * Copyright 2012 Stanley Shyiko
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
 package com.github.shyiko.hmp;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @goal start
  * @author <a href="mailto:stanley.shyiko@gmail.com">Stanley Shyiko</a>
  */
 public class StartMojo extends AbstractHadoopMojo {
 
     /**
      * True if Hadoop NameNode should be automatically formatted, false otherwise
      * @parameter expression="${hmp.autoFormatNameNode}" default-value="true"
      */
     private boolean autoFormatNameNode;
 
     /**
      * True if Hadoop should be terminated before JVM shutdown, false otherwise
      * @parameter expression="${hmp.autoShutdown}" default-value="true"
      */
     private boolean autoShutdown;
 
     /**
      * Timeout for NameNode startup
      * @parameter expression="${hmp.nameNodeStartupTimeoutInMilliseconds}" default-value="60000"
      */
     private int nameNodeStartupTimeoutInMilliseconds;
 
     /**
      * Timeout for DataNode startup
      * @parameter expression="${hmp.dataNodeStartupTimeoutInMilliseconds}" default-value="60000"
      */
     private int dataNodeStartupTimeoutInMilliseconds;
 
     /**
      * Timeout for JobTracker startup
      * @parameter expression="${hmp.jobTrackerStartupTimeoutInMilliseconds}" default-value="60000"
      */
     private int jobTrackerStartupTimeoutInMilliseconds;
 
     /**
      * Timeout for TaskTracker startup
      * @parameter expression="${hmp.taskTrackerStartupTimeoutInMilliseconds}" default-value="60000"
      */
     private int taskTrackerStartupTimeoutInMilliseconds;
 
     @Override
     protected void execute(HadoopSettings hadoopSettings) throws MojoExecutionException, MojoFailureException {
         if (getLog().isDebugEnabled()) {
             getLog().debug("Environment: " + hadoopSettings.getEnvironment());
         }
         if (autoFormatNameNode) {
             formatNameNode(hadoopSettings);
         }
         startHadoop(hadoopSettings);
     }
 
     private void formatNameNode(HadoopSettings hadoopSettings) throws MojoFailureException {
         try {
             executeCommand(hadoopSettings, "bin/hdfs namenode -format", "Y\n");
             //https://issues.apache.org/jira/browse/HDFS-107
             //noinspection ResultOfMethodCallIgnored
             String dataNodeDir = hadoopSettings.getConf("dfs.data.dir");
             if (dataNodeDir != null) {
                 new File(dataNodeDir, "current/VERSION").delete();
             } else {
                 getLog().warn("Unable to determine Hadoop dfs.data.dir. DataNode may fail to start");
             }
         } catch (Exception e) {
             throw new MojoFailureException("Unable to format NameNode: " + e.getMessage());
         }
     }
 
     private void startHadoop(HadoopSettings hadoopSettings) {
         startDaemon(hadoopSettings, "NameNode", "bin/hdfs namenode");
         if (nameNodeStartupTimeoutInMilliseconds > 0) {
             waitForDaemonStartup(hadoopSettings, "NameNode", nameNodeStartupTimeoutInMilliseconds,
                     "dfs.http.bindAddress", 50070);
         }
         startDaemon(hadoopSettings, "DataNode", "bin/hdfs datanode");
         if (dataNodeStartupTimeoutInMilliseconds > 0) {
             waitForDaemonStartup(hadoopSettings, "DataNode", dataNodeStartupTimeoutInMilliseconds,
                     "dfs.datanode.http.bindAddress", 50075);
         }
         startDaemon(hadoopSettings, "JobTracker", "bin/mapred jobtracker");
         if (jobTrackerStartupTimeoutInMilliseconds > 0) {
             waitForDaemonStartup(hadoopSettings, "JobTracker", jobTrackerStartupTimeoutInMilliseconds,
                     "mapred.job.tracker.http.bindAddress", 50030);
         }
         startDaemon(hadoopSettings, "TackTracker", "bin/mapred tasktracker");
         if (taskTrackerStartupTimeoutInMilliseconds > 0) {
             waitForDaemonStartup(hadoopSettings, "TaskTracker", taskTrackerStartupTimeoutInMilliseconds,
                     "mapred.task.tracker.http.bindAddress", 50060);
         }
     }
 
     private void waitForDaemonStartup(HadoopSettings hadoopSettings, String name, int timeoutInMilliseconds,
                                       String bindAddressProperty, int defaultPort) {
         int daemonBindPort = defaultPort;
         String daemonBindAddress = hadoopSettings.getConf(bindAddressProperty);
         if (daemonBindAddress != null) {
             int positionOfDelimiter = daemonBindAddress.lastIndexOf(":");
             if (positionOfDelimiter > -1) {
                 daemonBindPort = Integer.valueOf(daemonBindAddress.substring(positionOfDelimiter + 1));
             }
         }
         if (getLog().isInfoEnabled()) {
             getLog().info("Waiting for " + name + " on port " + daemonBindPort);
         }
         if (!waitForPort(daemonBindPort, timeoutInMilliseconds)) {
             getLog().warn("Timed out waiting for " + name);
         }
     }
 
     private boolean waitForPort(int port, int timeoutInMilliseconds) {
         long startTime = System.currentTimeMillis();
         do {
             try {
                new Socket("localhost", port);
                 return true;
             } catch (IOException e) {
                 try {
                     TimeUnit.SECONDS.sleep(1);
                 } catch (InterruptedException e1) {
                 }
             }
         } while (System.currentTimeMillis() - startTime < timeoutInMilliseconds);
         return false;
     }
 
     private void startDaemon(final HadoopSettings hadoopSettings, final String name, final String command) {
         if (getLog().isInfoEnabled()) {
             getLog().info("Starting " + name);
         }
         Thread daemon = new Thread() {
 
             @Override
             public void run() {
                 try {
                     executeCommand(hadoopSettings, command, autoShutdown);
                 } catch (IOException e) {
                     getLog().error(e);
                 }
             }
         };
         daemon.setDaemon(true);
         daemon.start();
     }
 }
