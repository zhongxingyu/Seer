 /*
  * Copyright (C) 2010-2013 by PhonyTive LLC (http://phonytive.com)
  * http://astivetoolkit.org
  *
  * This file is part of Astive Toolkit(ATK)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.astivetoolkit.server.monitor;
 
 import java.io.IOException;
 import java.net.SocketPermission;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
 import org.astivetoolkit.AstiveException;
 import org.astivetoolkit.agi.AgiCommandHandler;
 import org.astivetoolkit.agi.AgiException;
 import org.astivetoolkit.agi.AgiResponse;
 import org.astivetoolkit.agi.Connection;
 import org.astivetoolkit.agi.fastagi.FastAgiConnection;
 import org.astivetoolkit.agi.fastagi.FastAgiResponse;
 import org.astivetoolkit.astivlet.AstivletRequest;
 import org.astivetoolkit.astivlet.AstivletResponse;
 import org.astivetoolkit.server.AstivletProcessor;
 import org.astivetoolkit.server.ConnectionManager;
 import org.astivetoolkit.server.FastAgiConnectionManager;
 import org.astivetoolkit.server.FastAgiServerSocket;
 import org.astivetoolkit.server.security.AstPolicy;
 import org.astivetoolkit.server.security.AstPolicyUtil;
 import org.astivetoolkit.util.AppLocale;
 
 /**
  *
  * @since 1.0.0
  * @see ConnectionMonitor
  */
 public class FastAgiConnectionMonitor implements ConnectionMonitor {
     // A usual logging class
 
     private static final Logger LOG = Logger.getLogger(FastAgiConnectionMonitor.class);
     private ConnectionManager manager;
     private FastAgiServerSocket server;
     private ThreadPoolExecutor threadPoolExecutor;
 
     /**
      * Creates a new FastAgiConnectionMonitor object.
      *
      * @param server the server to monitor.
      * @param threads maximum threads allow for the server.
      */
     public FastAgiConnectionMonitor(FastAgiServerSocket server, int threads) {
         if (LOG.isDebugEnabled()) {
             LOG.debug(AppLocale.getI18n("messageStartingConnectionMonitor"));
         }
 
         this.server = server;
         manager = new FastAgiConnectionManager();
 
         int corePoolSize = threads;
         int maxPoolSize = threads;
         // TODO: This should be a parameter
         long keepAliveTime = 0L;
 
         BlockingQueue<Runnable> threadPool = new LinkedBlockingQueue <Runnable>();
         
         threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                 keepAliveTime, TimeUnit.MILLISECONDS,
                     threadPool);
 
         //threadPoolExecutor.prestartAllCoreThreads();
         if (LOG.isDebugEnabled()) {
             LOG.debug(AppLocale.getI18n("messageDone"));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void processConnection(final Connection conn)
             throws AstiveException {
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(AppLocale.getI18n("messageProcessingCall"));
             }
 
             FastAgiConnection fastConn = (FastAgiConnection) conn;
 
             StringBuilder sbr = new StringBuilder();
             sbr.append(fastConn.getSocket().getInetAddress().getHostAddress());
             sbr.append(":");
             sbr.append(fastConn.getSocket().getPort());
 
             SocketPermission sp = new SocketPermission(sbr.toString(), AstPolicy.DEFAULT_ACTION);
 
             if (AstPolicyUtil.hasPermission(sp)) {
                 AgiCommandHandler cHandler = new AgiCommandHandler(conn);
                 FastAgiResponse response = new FastAgiResponse(cHandler);
                 AstivletRequest aRequest =
                         new AstivletRequest(cHandler.getAgiRequest().getLines(), fastConn);
                 AstivletResponse aResponse = new AstivletResponse((AgiResponse) response);
 
                 AstivletProcessor.invokeAstivlet(aRequest, aResponse);
 
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("messageDone");
                 }
             } else {
                 LOG.error(AppLocale.getI18n("errorUnableToPlaceCallCheckNetPermissions"));
 
                 try {
                     fastConn.getSocket().close();
                 } catch (IOException ex) {
                     // Drop connection
                 }
             }
         } catch (AgiException ex) {
             LOG.error(AppLocale.getI18n("errorUnexpectedFailure", new Object[]{ex.getMessage()}));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void run() {
         while (!Thread.currentThread().isInterrupted()) {
             final FastAgiConnection conn;
 
             try {
                 conn = server.acceptConnection();
 
                 // TODO: This should be configurable.
                if (threadPoolExecutor.getMaximumPoolSize() <= threadPoolExecutor.getActiveCount()) {
                     conn.close();
                     continue;
                 }
                 
                 Runnable task = new Runnable() {
                     @Override
                     public void run() {
                         manager.add(conn);
 
                         try {
                             processConnection(conn);
                         } catch (AstiveException ex) {
                             LOG.warn(ex.getMessage());
                         }
 
                         try {
                             manager.remove(conn);
                         } catch (IOException ex) {
                             System.out.println("DBG 1");
                             LOG.error(AppLocale.getI18n("errorConnectionClosed",
                                     new Object[]{ex.getMessage()}));
                         }
                     }
                 };
                 
                 LOG.debug("Task count :: " +  threadPoolExecutor.getActiveCount());
                 threadPoolExecutor.execute(task); 
             } catch (IOException ex) {
                 if(!server.isRunning()) {
                     LOG.debug(AppLocale.getI18n("messageStoppingFastAgiConnectionMonitor"));   
                     Thread.currentThread().interrupt();
                     LOG.debug(AppLocale.getI18n("messageDone"));
                 }
             }
         }
     }
 }
