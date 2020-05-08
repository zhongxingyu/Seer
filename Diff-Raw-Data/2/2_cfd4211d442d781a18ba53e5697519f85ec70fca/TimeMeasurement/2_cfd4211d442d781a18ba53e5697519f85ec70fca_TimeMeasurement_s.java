 package com.coremedia.contribution.timemeasurement;
 
 import etm.core.configuration.BasicEtmConfigurator;
 import etm.core.configuration.EtmManager;
 import etm.core.monitor.EtmPoint;
 import etm.core.timer.DefaultTimer;
 
 /**
  * Helper class that works as an initializer and connector to the JETM library.
  * <p/>
  * Usage:
  * Configure the module "timemeasurement" as a dependency in Maven.
  * The measurement will work if the Java SystemProperty "timemeasurement.enabled" is set to "true"
  * or enabled via the JMX Bean "TimeMeasurement:name=TimeMeasurement".
  * Otherwise, the calls to this class will do nothing.
  * That way, you won't have to remove the calls from your production code if you don't want to and you can also
  * enable time measurement in a production environment via JMX.
  * <p/>
  * This is an example on how to measure time in your code:
  * <p/>
  * EtmPoint etmPoint = null;
  * try {
  * etmPoint = TimeMeasurement.start("nameOfTheMeasurementPoint");
  * //business logic
  * }
  * finally {
  * TimeMeasurement.stop(etmPoint);
  * }
  * <p/>
  * The method "stop" should be called in the finally clause. If an exception occurs in the business logic, the
  * Measurement won't be stopped otherwise.
  * <p/>
  * The class registers the following JMX-MBean: "TimeMeasurement:service=PerformanceMonitor"
  * The method "renderResultsAsText" returns the results as formatted text.
  * Mind that JETM uses blanks to format, so make sure you use a fixed width font when displaying the results.
  * <p/>
  * As a convenience, this class provides two ways of obtaining the measurement data without using JMX:
  * <p/>
  * {@link TimeMeasurement#toStdOut() }
  * and
  * {@link TimeMeasurement#getMeasurementResults()}
  */
 public final class TimeMeasurement implements TimeMeasurementMBean {
   private static TimeMeasurement instance = new TimeMeasurement();
   private boolean active;
   private boolean isUseNested;
   private boolean isUseMillis;
   private static boolean performanceMBeanCreated;
   private static JetmConnector measurement;
 
   /**
    * Private constructor
    */
   private TimeMeasurement() {
     active = isMeasurementEnabled();
     isUseMillis = isUseMillisEnabled();
     isUseNested = isUseNestedEnabled();
   }
 
   /**
    * static block initializes the Jetm library and the connector
    */
   static {
     configureEtmManager(isUseNestedEnabled(), isUseMillisEnabled());
     JmxRegistrationHandler.registerTimeMeasurementMBean();
     chooseJetmConnector(isMeasurementEnabled());
   }
 
   /**
    * Start measurement.
    *
    * @param pointName name of the measurement point to create
    * @return measurement point
    */
   public static EtmPoint start(final String pointName) {
     return measurement.start(pointName);
   }
 
   /**
    * Stop measurement.
    *
    * @param point the measurement point to stop
    */
   public static void stop(final EtmPoint point) {
     measurement.stop(point);
   }
 
   /**
    * Print formatted measurement information to StandardOut
    */
   public static void toStdOut() {
     measurement.toStdOut();
   }
 
   /**
   * Print formatted measurement information to StandardOut
    */
   public static void toLog() {
     measurement.toLog();
   }
 
   /**
    * Get all measurement data as a String
    *
    * @return formatted measurement data
    */
   public static String getMeasurementResults() {
     return measurement.getMeasurementResults();
   }
 
   /**
    * True of the Java SystemProperty "timemeasurement.enabled" is set to "true".
    * False, if the SystemProperty is set to false or not set at all.
    *
    * @return true if enabled
    */
   public static boolean isMeasurementEnabled() {
     return Boolean.getBoolean("timemeasurement.enabled");
   }
 
   /**
    * True of the Java SystemProperty "timemeasurement.enabled" is set to "true".
    * False, if the SystemProperty is set to false or not set at all.
    * <p/>
    * Set SystemProperty to true if measurement should be done in milliseconds instead of nanoseconds.
    *
    * @return true if enabled
    */
   public static boolean isUseMillisEnabled() {
     return Boolean.getBoolean("timemeasurement.useMillis");
   }
 
   /**
    * True of the Java SystemProperty "timemeasurement.isUseNested" is set to "true".
    * False, if the SystemProperty is set to false or not set at all.
    * <p/>
    * Set SystemProperty to true if measurement results should be returned nested.
    *
    * @return true if enabled
    */
   public static boolean isUseNestedEnabled() {
     return Boolean.getBoolean("timemeasurement.useNested");
   }
 
   @Override
   public boolean isActive() {
     return active;
   }
 
   @Override
   public void setActive(final boolean active) {
     this.active = active;
     chooseJetmConnector(active);
   }
 
   @Override
   public boolean isUseNested() {
     return isUseNested;
   }
 
   @Override
   public boolean isUseMillis() {
     return isUseMillis;
   }
 
   @Override
   public void writeMeasurementResultsToLog() {
     measurement.toLog();
   }
 
   /**
    * Returns the current instance.
    *
    * @return {@link TimeMeasurementMBean} instance
    */
   public static TimeMeasurementMBean getMBean() {
     return instance;
   }
 
   /**
    * Configures the EtmManager.
    *
    * @param isUseMillis set to true to measure in milliseconds.
    * @param isUseNested set to true to return nested results.
    */
   private static void configureEtmManager(final boolean isUseNested, final boolean isUseMillis) {
     DefaultTimer defaultTimer = null;
     if (isUseMillis) {
       //use DefaultTimer instance to measure in milliseconds instead of nanoseconds
       defaultTimer = new DefaultTimer();
     }
 
     BasicEtmConfigurator.configure(isUseNested, defaultTimer);
   }
 
   /**
    * Returns a WorkingJetmConnector if measurement is enabled, a dummy otherwise
    * Starts or stops the {@link etm.core.configuration.EtmManager#getEtmMonitor()} as necessary.
    *
    * @param isMeasurementEnabled if true, measurement will take place
    */
   private static void chooseJetmConnector(final boolean isMeasurementEnabled) {
     if (isMeasurementEnabled) {
       measurement = new WorkingJetmConnector();
       EtmManager.getEtmMonitor().start();
       if (!performanceMBeanCreated) {
         JmxRegistrationHandler.registerEtmMonitorMBean();
         performanceMBeanCreated = true;
       }
     } else {
       if (EtmManager.getEtmMonitor().isStarted()) {
         EtmManager.getEtmMonitor().stop();
       }
       if (performanceMBeanCreated) {
         JmxRegistrationHandler.unregisterEtmMonitorMBean();
         performanceMBeanCreated = false;
       }
       measurement = new DummyJetmConnector();
     }
   }
 
   public static void reset() {
     EtmManager.reset();
     configureEtmManager(isUseNestedEnabled(), isUseMillisEnabled());
     chooseJetmConnector(isMeasurementEnabled());
   }
 
 }
