 package com.nevermindsoft.kiln;
 
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Level;
 import org.apache.log4j.spi.LoggingEvent;
 
 /**
  * This Log4j adaptor pushes messages to a pre-configured remote server
  *
  * User: Roger Cracel
  * Date: 12/20/12
  * Time: 3:56 PM
  */
 public class RemoteServiceAppender extends AppenderSkeleton {
 
     private PublisherThread processor;
     private Thread          thread;
 
     private String moduleName      = "Unknown";
     private String environmentName = "Unknown";
     private String apiKey          = "xxx-xxx-xxx";
     private String serverUrl       = "http://localhost:8080/polar/api/submit";
     private int    maxRequestItems = 200;
     private int    sleepTime       = 2000;
 
     /**
      * Constructor
      */
     public RemoteServiceAppender() {
 
     }
 
     /**
      * This method is invoked after all properties are set on this appender. This is where the work actually begins, and
      * invoking this method will cause the remote server pushing thread to begging monitoring and pushing items to the
      * remote server.
      */
     @Override
     public void activateOptions() {
         processor = new PublisherThread( moduleName, apiKey, environmentName, serverUrl, maxRequestItems, sleepTime );
 
         thread = new Thread( processor );
 
         thread.start();
 
         if ( thread.isAlive() ) {
             KilnLogger.log( Level.INFO, this.getClass().getSimpleName() + " started....");
         } else {
             KilnLogger.log( Level.INFO,  this.getClass().getSimpleName());
         }
     }
 
     /**
      * Subclasses of <code>AppenderSkeleton</code> should implement this
      * method to perform actual logging. See also {@link #doAppend
      * AppenderSkeleton.doAppend} method.
      *
      * @since 0.9.0
      */
     @Override
     protected void append( LoggingEvent event ) {
        event.getNDC();
        event.getThreadName();

        // Get a copy of this thread's MDC.
        event.getMDCCopy();
        event.getLocationInformation();

        event.getRenderedMessage();
        event.getThrowableStrRep();

         if ( !processor.queue( event ) ) {
             KilnLogger.log( Level.ERROR, "Could not queue event");
         }
     }
 
     /**
      * Release any resources allocated within the appender such as file
      * handles, network connections, etc.
      * <p/>
      * <p>It is a programming error to append to a closed appender.
      *
      * @since 0.8.4
      */
     @Override
     public void close() {
         processor.flush();
         processor.stop();
     }
 
     /**
      * Configurators call this method to determine if the appender
      * requires a layout. If this method returns <code>true</code>,
      * meaning that layout is required, then the configurator will
      * configure an layout using the configuration information at its
      * disposal.  If this method returns <code>false</code>, meaning that
      * a layout is not required, then layout configuration will be
      * skipped even if there is available layout configuration
      * information at the disposal of the configurator..
      * <p/>
      * <p>In the rather exceptional case, where the appender
      * implementation admits a layout but can also work without it, then
      * the appender should return <code>true</code>.
      *
      * @since 0.8.4
      */
     @Override
     public boolean requiresLayout() {
         return false;
     }
 
 
     public void setModuleName(String moduleName) {
         this.moduleName = moduleName;
     }
 
     public void setServerUrl(String serverUrl) {
         this.serverUrl = serverUrl;
     }
 
     public void setMaxRequestItems(int maxRequestItems) {
         this.maxRequestItems = maxRequestItems;
     }
 
     public void setSleepTime(int sleepTime) {
         this.sleepTime = sleepTime;
     }
 
     public void setEnvironmentName( String environmentName ) {
         this.environmentName = environmentName;
     }
 
     public void setApiKey( String apiKey ) {
         this.apiKey = apiKey;
     }
 }
