 /*
  * Copyright (c) 2012, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jmxdatamart.Extractor;
 
 import com.google.inject.Inject;
import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.MalformedURLException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
import javax.management.*;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.jmxdatamart.Extractor.MXBean.MultiLayeredAttribute;
import org.jmxdatamart.common.DBException;
import org.jmxdatamart.common.HypersqlHandler;
import org.slf4j.LoggerFactory;
 
 public final class Extractor {
 
   private final Settings configData;
   private MBeanServerConnection mbsc;
   private final org.slf4j.Logger logger = LoggerFactory.getLogger(Extractor.class);
   private final Bean2DB bd = new Bean2DB();
   private final String dbName;
   private final HypersqlHandler hsql;
   private Connection conn;
   private final Lock connLock = new ReentrantLock();
   private Timer timer;
   private final Properties props = new Properties();
 
   @Inject
   public Extractor(Settings configData) {
     timer = null;
     this.configData = configData;
     props.put("username", "sa");
     props.put("password", "whatever");
 
     if (configData.getUrl() == null || configData.getUrl().isEmpty()) {
       mbsc = ManagementFactory.getPlatformMBeanServer();
     } else {
       JMXServiceURL url = null;
       try {
         url = new JMXServiceURL(configData.getUrl());
       } catch (MalformedURLException e) {
         logger.error("Error creating JMX service URL object", e);
         throw new RuntimeException(e);
       }
 
       try {
         mbsc = JMXConnectorFactory.connect(url).getMBeanServerConnection();
       } catch (IOException e) {
         logger.error(e.getMessage(), e);
         throw new RuntimeException(e);
       }
     }
 
     hsql = new HypersqlHandler();
    dbName = configData.getFolderLocation() + File.separator + "Extrator" + new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
 
     if (isPeriodicallyExtract()) {
       periodicallyExtract();
     } else {
       extract();
     }
   }
 
   private void periodicallyExtract() {
     boolean isDaemon = true;
     timer = new Timer("JMX Statistics Extractor", isDaemon);
     long rate = configData.getPollingRate() * 1000;
     int delay = 0;
     timer.scheduleAtFixedRate(new Extract(), delay, rate);
   }
 
   public boolean isPeriodicallyExtract() {
     return this.configData.getPollingRate() > 0;
   }
 
   void extract() {
 
     connLock.lock();
     try {
         conn = hsql.connectDatabase(dbName, props);
 
       for (MBeanData bdata : this.configData.getBeans()) {
         Map<Attribute, Object> result = null;
         if (bdata.isEnable()) {
           if (!bdata.isPattern()) {
             result = MBeanExtract.extract(bdata, mbsc);
             bd.export2DB(conn, bdata, result);
           } else {
             String originalName = bdata.getName();
             ObjectName on;
             try {
               on = new ObjectName(bdata.getName());
             } catch (MalformedObjectNameException ex) {
               logger.error("Non standard name for Objectname " + bdata.getName(), ex);
               continue;
             }
             try {
               for (ObjectInstance oi : mbsc.queryMBeans(on, null)) {
                 String actual = oi.getObjectName().getCanonicalName();
                 bdata.setName(actual);
                 bdata.setAlias(MultiLayeredAttribute.name2alias(actual));
                 result = MBeanExtract.extract(bdata, mbsc);
                 bd.export2DB(conn, bdata, result);
               }
             } catch (IOException ex) {
               logger.error("Error while trying to access MBean Server", ex);
             }
             bdata.setName(originalName);
           }
         }
       }
 
 //      try {
         hsql.shutdownDatabase(conn);
 //      } catch (SQLException e) {
 //        logger.error(e.getMessage(), e);
 //      }
 
       HypersqlHandler.releaseDatabaseResource(null, null, null, conn);
       conn = null;
     } catch (SQLException ex) {
       logger.error("Error while importing to HSQL", ex);
       throw new RuntimeException(ex);
     } catch (DBException ex) {
       logger.error("Error while importing to HSQL", ex);
       throw new RuntimeException(ex);
     } finally {
       connLock.unlock();
     }
     logger.info("Extracted");
   }
 
   public void stop() {
     if (timer != null) {
       try {
         connLock.lock();
         if (conn != null && !conn.isClosed()) {
               hsql.shutdownDatabase(conn);
               HypersqlHandler.releaseDatabaseResource(null, null, null, conn);
             }
       } catch (SQLException ex) {
         LoggerFactory.getLogger(Extractor.class)
                     .error("Error while closing conn durring JVM shutdown", ex);
       } finally {
         connLock.unlock();
       }
       timer.cancel();
     }
   }
 
   private class Extract extends TimerTask {
 
     public Extract() {
       super();
       Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
         @Override
         public void run() {
           try {
             connLock.lock();
             if (conn != null && !conn.isClosed()) {
               hsql.shutdownDatabase(conn);
               HypersqlHandler.releaseDatabaseResource(null, null, null, conn);
             }
           } catch (SQLException ex) {
             LoggerFactory.getLogger(Extractor.class)
                     .error("Error while closing conn durring JVM shutdown", ex);
           } finally {
             connLock.unlock();
           }
         }
       }));
     }
 
     @Override
     public void run() {
       try {
         extract();
       } catch (Exception e) {
         logger.debug("While extracting MBeans", e);
       }
     }
   }
 }
