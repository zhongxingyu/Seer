 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.mediators.monitors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sciflex.plugins.synapse.esper.core.activities.MonitoredStatisticActivity;
 import org.sciflex.plugins.synapse.esper.core.workflows.MonitoredRequestResponseWorkflow;
 import org.sciflex.plugins.synapse.esper.core.util.RequestCounter;
 import org.sciflex.plugins.synapse.esper.core.util.TimeLogger;
 import org.sciflex.plugins.synapse.esper.core.util.UUIDGenerator;
 import org.sciflex.plugins.synapse.esper.mediators.SynapseEsperMediator;
 
 /**
  * Provides necessary capability to monitor a Mediator Statistics. The Mediator Statistics
  * monitor works based on a {@link SynapseEsperMediator} type object.
  * Each Mediator Statistics monitor will have at most one such associated
  * {@link SynapseEsperMediator} type object.
  */
 public class MediatorStatisticsMonitor implements MonitoredRequestResponseWorkflow, MonitoredStatisticActivity {
 
     /**
      * Log associated with the Mediator Statistics Monitor.
      */
     private static final Log log = LogFactory.getLog(MediatorStatisticsMonitor.class);
 
     /**
      * The {@link SynapseEsperMediator} whom the Mediator Statistics Monitor will assit.
      */
     private SynapseEsperMediator owner = null;
 
     /**
      * Unique Identifier of this object.
      */
     private String uid = null;
 
     /**
      * Unique Identifier of this object's parent.
      */
     private String parentUID = null;
 
     /**
      * Object used to lock stat fetch operation.
      */
     private Object statFetchLock = new Object();
 
     /**
      * Request Time Logger
      */
     private TimeLogger requestTimeLogger = new TimeLogger();
 
     /**
      * Response Time Logger
      */
     private TimeLogger responseTimeLogger = new TimeLogger();
 
     /**
      * Request Counter
      */
     private RequestCounter requestCounter = new RequestCounter();
 
     /**
      * Constructor accepting the Owner of the EPL.
      *
      * @param owner The Owner of the EPL. The owner cannot be null.
      *              The owner is of type {@link SynapseEsperMediator}.
      */
     public MediatorStatisticsMonitor(SynapseEsperMediator owner) {
         this.owner = owner;
     }
 
     /**
      * Gets the maximum request time.
      *
      * @return maximum request time.
      */
     public synchronized long getMaxRequestTime() {
         return requestTimeLogger.getMaxTime();
     }
 
     /**
      * Gets the maximum response time.
      *
      * @return maximum response time.
      */
     public synchronized long getMaxResponseTime() {
         return responseTimeLogger.getMaxTime();
     }
 
     /**
      * Gets the minimum request time.
      *
      * @return minimum request time.
      */
     public synchronized long getMinRequestTime() {
         return requestTimeLogger.getMinTime();
     }
 
     /**
      * Gets the minimum response time.
      *
      * @return minimum response time.
      */
     public synchronized long getMinResponseTime() {
         return responseTimeLogger.getMinTime();
     }
 
     /**
      * Gets the average request time.
      *
      * @return average request time.
      */
     public synchronized double getAvgRequestTime() {
         return requestTimeLogger.getAvgTime();
     }
 
     /**
      * Gets the average response time.
      *
      * @return average response time.
      */
     public synchronized double getAvgResponseTime() {
         return responseTimeLogger.getAvgTime();
     }
 
     /**
      * Resets counters.
      */
     public void reset() {
         synchronized(statFetchLock) {
             requestTimeLogger.reset();
             responseTimeLogger.reset();
             requestCounter.reset();
         }
     }
 
     /**
      * Retrieves load on mediator as a percentage of total load.
      *
      * @return load as percentage.
      */
     public synchronized double getLoadAsPercentage() {
         double ret = 0.0;
         synchronized(statFetchLock) {
             double x = (double) requestCounter.getRequests();
             double y = (double) requestCounter.getTotalRequests();
            ret = (x * 100.0) / y;
         }
         return ret;
     }
 
     /**
      * Handles Request
      *
      * @param time Request Time in milliseconds
      */
     public void handleRequest(long time) {
         requestCounter.increment();
         requestTimeLogger.add(time);
     }
 
     /**
      * Handles Response
      *
      * @param time Response Time in milliseconds
      */
     public void handleResponse(long time) {
         responseTimeLogger.add(time);
     }
 
 
     /**
      * Sets the Unique Identifier of the resources parent.
      *
      * @param uid sets Unique Identifier for the resource's parent.
      */
     public void setParentUID(String uid) {
         parentUID = uid;
     }
 
     /**
      * Gets the Unique Identifier of the resource's parent.
      *
      * @return The Unique Identifier for the resource's parent.
      */
     public String getParentUID() {
         return parentUID;
     }
 
     /**
      * Returns the Unique Identifier of the resource.
      *
      * @return the Unique Identifier of the resource.
      */
     public String getUID() {
         if (uid == null) {
             uid = UUIDGenerator.createUUID();
         }
         return uid;
     }
 
     /**
      * Returns the Type of the resource.
      *
      * @return the Type of the resource.
      */
     public String getType() {
         return MediatorStatisticsMonitor.class.getName();
     }
 }
