 package com.bizo.asperatus.jmx;
 
 import java.util.List;
 
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.openmbean.CompositeData;
 
 import com.bizo.asperatus.jmx.configuration.MetricConfiguration;
 import com.bizo.asperatus.jmx.configuration.MetricConfigurationException;
 import com.bizo.asperatus.model.Dimension;
 import com.bizo.asperatus.tracker.MetricTracker;
 
 /**
  * This runnable executes a single pull of data from JMX and pushes the information to Asperatus.
  */
 public class MetricRunnable implements Runnable {
   private final MBeanServer mBeanServer;
   private final ObjectName jmxName;
   private final MetricTracker tracker;
   private final List<Dimension> dimensions;
   private final MetricConfiguration config;
   private final ErrorHandler errorHandler;
 
   /**
    * Creates a new MetricRunnable.
    * 
    * @param config
    *          the configuration to pull
    * @param server
    *          the MBeanServer containing data
    * @param tracker
    *          the Asperatus tracker that will receive data
    * @param dimensions
    *          the dimensions to send to Asperatus
    * @param errorHandler
    *          the handler that processes error notifications
    */
   public MetricRunnable(
       final MetricConfiguration config,
       final MBeanServer server,
       final MetricTracker tracker,
       final List<Dimension> dimensions,
       final ErrorHandler errorHandler) {
     mBeanServer = server;
     this.tracker = tracker;
     this.dimensions = dimensions;
     this.config = config;
     this.errorHandler = errorHandler;
 
     try {
       jmxName = new ObjectName(config.getObjectName());
     } catch (final MalformedObjectNameException moan) {
       throw new MetricConfigurationException(moan);
     }
   }
 
   @Override
   public void run() {
     try {
       final Object result = mBeanServer.getAttribute(jmxName, config.getAttribute());
       if (config.getCompositeDataKey() != null) {
         if (result instanceof CompositeData) {
           final CompositeData cData = (CompositeData) result;
           final Object compositeDataValue = cData.get(config.getCompositeDataKey());
           if (compositeDataValue != null && compositeDataValue instanceof Number) {
             track((Number) compositeDataValue);
          } else {
            typeError(compositeDataValue, Number.class);
           }
         } else {
           typeError(result, CompositeData.class);
         }
       } else if (result instanceof Number) {
         track((Number) result);
       } else {
         typeError(result, Number.class);
       }
     } catch (final Exception e) {
       errorHandler.handleError("Error while getting data for metric " + config.getMetricName(), e);
     }
   }
 
   private void track(final Number value) {
     tracker.track(config.getMetricName(), value, config.getUnit(), dimensions);
   }
 
   private void typeError(final Object result, final Class<?> expectedType) {
     final String actualType = result != null ? result.getClass().getName() : "null";
     errorHandler.handleError(
       String.format("Metric %s returned a %s, required a %s", config.getMetricName(), actualType, expectedType),
       null);
   }
 }
