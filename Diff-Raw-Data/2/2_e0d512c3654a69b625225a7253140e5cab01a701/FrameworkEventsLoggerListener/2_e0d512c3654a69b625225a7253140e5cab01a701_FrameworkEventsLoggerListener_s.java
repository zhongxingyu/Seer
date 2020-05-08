 package org.mosaic.runner.logging;
 
 import org.osgi.framework.FrameworkEvent;
 import org.osgi.framework.FrameworkListener;
 import org.osgi.framework.startlevel.FrameworkStartLevel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author arik
  */
 public class FrameworkEventsLoggerListener implements FrameworkListener {
 
     private final Logger logger = LoggerFactory.getLogger( "org.mosaic.osgi.framework" );
 
     @Override
     public void frameworkEvent( FrameworkEvent event ) {
         @SuppressWarnings( "ThrowableResultOfMethodCallIgnored" )
         Throwable throwable = event.getThrowable();
         String throwableMsg = throwable != null ? throwable.getMessage() : "";
 
         switch( event.getType() ) {
             case FrameworkEvent.STARTED:
                 this.logger.info( "Started the OSGi Framework" );
                 break;
 
             case FrameworkEvent.ERROR:
                 this.logger.error( "OSGi Framework error has occurred: {}", throwableMsg, throwable );
                 break;
 
             case FrameworkEvent.PACKAGES_REFRESHED:
                this.logger.info( "Refreshed OSGi packages have been refreshed" );
                 break;
 
             case FrameworkEvent.STARTLEVEL_CHANGED:
                 FrameworkStartLevel startLevel = event.getBundle().adapt( FrameworkStartLevel.class );
                 this.logger.info( "OSGi Framework start level has been changed to: {}", startLevel.getStartLevel() );
                 break;
 
             case FrameworkEvent.WARNING:
                 this.logger.warn( "OSGi Framework warning has occurred: {}", throwableMsg, throwable );
                 break;
 
             case FrameworkEvent.INFO:
                 this.logger.info( "OSGi Framework informational has occurred: {}", throwableMsg, throwable );
                 break;
 
             case FrameworkEvent.STOPPED:
                 this.logger.info( "Stopped the OSGi Framework" );
                 break;
 
             case FrameworkEvent.STOPPED_UPDATE:
                 this.logger.info( "Restarting the OSGi Framework" );
                 break;
 
             case FrameworkEvent.STOPPED_BOOTCLASSPATH_MODIFIED:
                 this.logger.info( "Restarting the OSGi Framework due to boot class-path modification" );
                 break;
         }
     }
 }
