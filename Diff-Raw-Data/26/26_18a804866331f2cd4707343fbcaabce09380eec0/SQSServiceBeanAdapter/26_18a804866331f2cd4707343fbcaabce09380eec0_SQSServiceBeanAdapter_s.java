/**
  * Elastic Grid
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

 package com.elasticgrid.amazon.sqs;
 
 import com.elasticgrid.utils.amazon.AWSUtils;
import org.rioproject.core.jsb.ServiceBeanContext;
import org.rioproject.jsb.ServiceBeanAdapter;
import org.rioproject.watch.Calculable;
import org.rioproject.watch.PeriodicWatch;
 import com.xerox.amazonws.sqs2.MessageQueue;
 import com.xerox.amazonws.sqs2.QueueService;
 import com.xerox.amazonws.sqs2.SQSException;
 import net.jini.config.Configuration;
 import net.jini.config.ConfigurationException;
 import java.io.IOException;
 import java.rmi.Remote;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * {@link ServiceBeanAdapter} easing development with Amazon SQS.
  */
 public abstract class SQSServiceBeanAdapter extends ServiceBeanAdapter implements SQSListener {
     private QueueService queueService;
     private ScheduledExecutorService pool;
     protected MessageQueue queue;
     protected String awsAccessId;
     protected String awsSecretKey;
     protected String COMPONENT = SQSServiceBeanAdapter.class.getPackage().getName() + ".SQS";
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     @Override
     protected Object createProxy() {
         try {
             // try to load properties from $HOME/.eg/aws.properties
             Properties awsProperties = AWSUtils.loadEC2Configuration();
            awsAccessId = (String) awsProperties.get(AWSUtils.AWS_ACCESS_ID);
            awsSecretKey = (String) awsProperties.get(AWSUtils.AWS_SECRET_KEY);
             Boolean secured = Boolean.parseBoolean((String) awsProperties.get("aws.sqs.secured"));
             // fall-back to JSB configuration
             Configuration config = context.getConfiguration();
             if (awsAccessId == null)
                 awsAccessId = (String) config.getEntry(COMPONENT, "awsAccessId", String.class);
             if (awsSecretKey == null)
                 awsSecretKey = (String) config.getEntry(COMPONENT, "awsSecretKey", String.class);
             if (secured == null)
                 secured = (Boolean) config.getEntry(COMPONENT, "secured", Boolean.class, true);
             // read other JSB configuration parameters
             final String queueName = (String) config.getEntry(COMPONENT, "queueName", String.class);
             queueService = new QueueService(awsAccessId, awsSecretKey, secured);
             queue = com.xerox.amazonws.sqs2.SQSUtils.connectToQueue(queueName, awsAccessId, awsSecretKey);
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (ConfigurationException e) {
             throw new RuntimeException(e);
         } catch (SQSException e) {
             throw new RuntimeException(e);
         }
         return super.createProxy();
     }
 
     public QueueService getQueueService() {
         return queueService;
     }
 
     @Override
     public void initialize(ServiceBeanContext context) throws Exception {
         super.initialize(context);
         // gather configuration
         Configuration config = context.getConfiguration();
         int visibilityTimeout = (Integer) config.getEntry(COMPONENT, "visibilityTimeout", Integer.class, 0);
         Long watchPeriod = (Long) config.getEntry(COMPONENT, "watchPeriod", Long.class, 5000L);
         // setup visibility timeout
         if (visibilityTimeout != 0)
             queue.setVisibilityTimeout(visibilityTimeout);
         // setup watches
         final String queueName = (String) config.getEntry(COMPONENT, "queueName", String.class);
         PeriodicWatch watch = new PeriodicWatch(String.format("Queue %s", queueName)) {
             public void checkValue() {
                 try {
                     int numberOfMessages = queue.getApproximateNumberOfMessages();
                     logger.log(Level.FINEST,
                             "Found {0,choice,0#no message|1#one message|1<{0} messages} in queue {1}",
                             new Object[]{numberOfMessages, queueName});
                     addWatchRecord(new Calculable(id, numberOfMessages, System.currentTimeMillis()));
                 } catch (SQSException e) {
                     logger.log(Level.WARNING,
                             "Could not compute approximate number of messages for queue {0}", queue.getUrl());
                 }
             }
         };
         watch.setPeriod(watchPeriod);
         watchRegistry.register(watch);
         // setup the SQS polling thread pool
         pool = Executors.newScheduledThreadPool(1);
     }
 
     public long getApproximateBacklog() {
         try {
             return queue.getApproximateNumberOfMessages();
         } catch (SQSException e) {
             throw new RuntimeException("Can't fetch the approximate number of messages left in queue", e);
         }
     }
 
     @Override
     public void advertise() throws IOException {
         int pollingPeriod;
         try {
             pollingPeriod = (Integer) context.getConfiguration().getEntry(COMPONENT, "pollingPeriod", Integer.class, 10);
         } catch (ConfigurationException e) {
             IOException ioe = new IOException("Can't retrieve configuration for the polling period");
             ioe.initCause(e);
             throw ioe;
         }
         logger.log(Level.INFO, "Started polling of SQS queue ''{0}''", SQSUtils.getQueueName(queue));
         pool.scheduleAtFixedRate(new SQSQueuePollingTask(this, queue), 0, pollingPeriod, TimeUnit.SECONDS);
         super.advertise();
     }
 
     @Override
     public void destroy() {
         super.destroy();
         pool.shutdown();
     }
 
     public Remote getExportedProxy() {
         return super.getExportedProxy();
     }
 }
