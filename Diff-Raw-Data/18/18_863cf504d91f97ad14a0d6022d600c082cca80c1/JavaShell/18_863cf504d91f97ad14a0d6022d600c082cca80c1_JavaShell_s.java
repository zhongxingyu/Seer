 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.outfit.shell;
 
 import java.io.IOException;
 import java.lang.management.RuntimeMXBean;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 import javax.management.ObjectName;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.shell.insider.Insider;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Starts and shuts down a JVM with an {@link org.novelang.outfit.shell.insider.InsiderAgent}
  * that installs an {@link org.novelang.outfit.shell.insider.Insider} for various tasks.
  * This class takes great care of JVM shutdown.
  * <ul>
  *   <li>
  *     With a {@link org.novelang.outfit.shell.ShutdownStyle#GENTLE} shutdown, it calls
  *     {@link org.novelang.outfit.shell.insider.Insider#shutdown()} that attempts to perform
  *     a nice {@code System.exit()} that calls shutdown hooks.
  *   </li> <li>
  *     The JVM starts with a special system property
  *     {@value org.novelang.outfit.shell.ShutdownTools#SHUTDOWN_TATTOO_PROPERTYNAME} used by
  *     {@link org.novelang.outfit.shell.ShutdownTools#shutdownAllTattooedVirtualMachines()} to recognize JVMs to shut down.
  *   </li> <li>
  *     The {@link org.novelang.outfit.shell.insider.Insider} in the JVM halts when
  *     {@link org.novelang.outfit.shell.insider.Insider#keepAlive()} wasn't called for a few seconds.
  *     The {@link JavaShell} runs a thread doing this, so if its JVM gets down, the JVM it started
  *     soon goes down, too.
  *   </li>
  * </ul>
  *
  * @author Laurent Caillette
  */
 public class JavaShell extends ProcessShell {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( JavaShell.class ) ;
 
   private final BootstrappingJmxKit jmxKit ;
   private final Integer heartbeatPeriodMilliseconds ;
 
   /**
    * A semaphore that is slightly redundant with the one inside {@link org.novelang.outfit.shell.ProcessShell}
    * but this helps preserving encapsulation.
    */
   private final Semaphore ownStartupSensorSemaphore ;
 
   /**
    * The count of permits that {@link #ownStartupSensorSemaphore} will try to acquire.
    * It depends upon the number of sensors inside {@link org.novelang.outfit.shell.TieredStartupSensor}.
    */
   private final int startupSensorSemaphorePermitCount ;
 
   private final long startupTimeoutDuration ;
   private final TimeUnit startupTimeoutTimeUnit ;
 
   /**
    * We use this object even when using always the same {@link JmxKit} because it does useful things.
    */
   private final JmxBeanPool jmxBeanPool ;
 
 
 
   public JavaShell( final JavaShellParameters parameters ) {
     super(
         parameters.getWorkingDirectory(),
         parameters.getNickname(),
         JavaShellTools.createProcessArguments(
             parameters.getJvmArguments(),
             parameters.getJmxKit(),
             parameters.getJmxPortConfiguredAtJvmStartup(),
             parameters.getJavaClasses(),
             parameters.getProgramArguments(),
             calculateHeartbeatFatalDelay( parameters )
         ),
         createTieredStartupSensor(
             LOCAL_INSIDER_STARTED,
             parameters.getStartupSensor()
         )
     ) ;
 
     this.jmxKit = parameters.getJmxKit() ;
     if( this.jmxKit != null ) {
       // Host is always localhost since we create process only on the local machine.
       jmxBeanPool = new JmxBeanPool( "localhost", checkNotNull( parameters.getJmxPortConfiguredAtJvmStartup() ) ) ;
     } else {
       jmxBeanPool = null ;
     }
 
     {
       final Long timeoutDuration = parameters.getStartupTimeoutDuration() ;
       if( timeoutDuration == null ) {
         this.startupTimeoutDuration = STARTUP_TIMEOUT_DURATION ;
       } else {
         checkArgument( timeoutDuration > 0 ) ;
         this.startupTimeoutDuration = timeoutDuration ;
       }
     }
 
 
     {
       final TimeUnit timeUnit = parameters.getStartupTimeoutTimeUnit() ;
       if( timeUnit == null ) {
         this.startupTimeoutTimeUnit = STARTUP_TIMEOUT_UNIT ;
       } else {
         this.startupTimeoutTimeUnit = timeUnit ;
       }
     }
 
     this.heartbeatPeriodMilliseconds = parameters.getHeartbeatPeriodMilliseconds() ;
     this.ownStartupSensorSemaphore = THREADLOCAL_SEMAPHORE.get() ;
     this.startupSensorSemaphorePermitCount = THREADLOCAL_PERMITCOUNT.get() ;
     THREADLOCAL_SEMAPHORE.set( null ) ;
     THREADLOCAL_PERMITCOUNT.set( null ) ;
 
     // Code checker happy: avoid access in both synchronized and unsynchronized context.
     synchronized( stateLock ) {
       heartbeatSender = null ;
       insider = null ;
     }
   }
 
 
   private static final String HEARTBEAT_FATAL_DELAY_PROPERTYNAME = "org.novelang.outfit.shell.heartbeatfataldelay" ;  
 
   private static Integer calculateHeartbeatFatalDelay( final JavaShellParameters parameters ) {
     final String systemProperty = System.getProperty( HEARTBEAT_FATAL_DELAY_PROPERTYNAME ) ;
     if( systemProperty == null ) {
       return parameters.getHeartbeatFatalDelayMilliseconds() ;
     } else {
       return Integer.parseInt( systemProperty ) ;
     }
   }
 
     private static final Predicate< String > LOCAL_INSIDER_STARTED = new Predicate< String >() {
     @Override
     public boolean apply( final String input ) {
       // Seems printed by the JVM itself.
       return input.contains( "Loaded org.novelang.outfit.shell.insider.InsiderAgent." ) ;
     }
   } ;
 
 
   private Insider insider;
   private HeartbeatSender heartbeatSender;
 
   /**
    * We need a dedicated lock for {@link #processIdentifier} otherwise there is a deadlock
    * when watcher threads try to access it while {@link #connect()} executes.
    */
   private final Object processIdentifierLock = new Object() ;
 
   private int processIdentifier = JavaShellTools.UNDEFINED_PROCESS_ID ;
 
   @Override
   public String getNickname() {
     final String defaultNickname = super.getNickname() ;
     final int currentIdentifier ;
     synchronized( processIdentifierLock ) {
       currentIdentifier = processIdentifier ;
     }
     return defaultNickname + (
         currentIdentifier == JavaShellTools.UNDEFINED_PROCESS_ID ? "" : "#" + currentIdentifier ) ;
   }
 
 
 // =======
 // Startup
 // =======
 
 
   public void start()
       throws
       IOException,
       InterruptedException,
       ProcessCreationException,
       ProcessInitializationException
   {
     // There are two steps to ensure JVM is ready.
     // First, launch the process and wait for the magic message in the console
     // claiming InsiderAgent loaded.
     // Second, obtain the JMX connection.
     // Following the principle of the least surprise, we keep the overall timeout
     // as defined. For each step, we wait half of the overall timeout.
     final long adjustedTimeoutDurationMilliseconds =
         startupTimeoutTimeUnit.toMillis( startupTimeoutDuration ) / 2L ;
 
     try {
       synchronized( stateLock ) {
         super.start( adjustedTimeoutDurationMilliseconds, TimeUnit.MILLISECONDS ) ;
         connect() ;
         if( insider != null ) {
           insider.keepAlive() ; // Ensure JMX working.
           startHeartbeatSender() ;
         }
       }
       if( hasDefaultJmxKit() ) {
         synchronized( processIdentifierLock ) {
           final RuntimeMXBean runtimeMXBean = getManagedBean(
               RuntimeMXBean.class, JavaShellTools.RUNTIME_MX_BEAN_OBJECTNAME ) ;
           processIdentifier = JavaShellTools.extractProcessId( runtimeMXBean.getName() ) ;
         }
       }
       ownStartupSensorSemaphore.tryAcquire(
           startupSensorSemaphorePermitCount,
           adjustedTimeoutDurationMilliseconds,
           TimeUnit.MILLISECONDS
       ) ;
       LOGGER.info( "Started ", getNickname(), "." ) ;
 
     } catch( Exception e ) {
       LOGGER.error( "Couldn't start ", getNickname(), ". Cleaning up..." ) ;
       shutdownProcessQuiet() ;
       cleanup() ;
       if( e instanceof ProcessCreationException ) {
         throw ( ProcessCreationException ) e ;
       }
       throw new ProcessInitializationException( "Couldn't initialize " + getNickname(), e ) ;
     }
   }
 
   private void startHeartbeatSender() {
     if( heartbeatPeriodMilliseconds == null ) {
       heartbeatSender = new HeartbeatSender( insider, heartbeatSenderNotifiee, getNickname() ) ;
     } else {
       heartbeatSender = new HeartbeatSender(
           insider,
           heartbeatSenderNotifiee,
           getNickname(),
           heartbeatPeriodMilliseconds
       ) ;
     }
   }
 
 
 
 // ===================
 // Process termination
 // ===================
 
 
     /**
    * Requests the underlying process to shutdown. When requested to shut down in a
    * {@link org.novelang.outfit.shell.ShutdownStyle#GENTLE} or
    * {@link org.novelang.outfit.shell.ShutdownStyle#WAIT} style, the method waits until the
    * process gently terminates.
    *
    * @param shutdownStyle a non-null object.
    * @throws InterruptedException should not happen.
    * @throws java.io.IOException too bad.
    */
   public Integer shutdown( final ShutdownStyle shutdownStyle )
       throws InterruptedException, IOException
   {
     LOGGER.info( "Shutdown (", shutdownStyle, ") requested for ", getNickname(), "..." ) ;
 
     // Code checker happy: avoid access to static member in synchronized context.
     final Logger logger = LOGGER ;
 
     Integer exitStatus = null ;
     synchronized( stateLock ) {
      if( insider == null ) {
        logger.info( "Not started or already shut down." ) ;
      } else {
        try {
           switch( shutdownStyle ) {
             case GENTLE :
               try {
                   if( hasDefaultJmxKit() ) {
                     insider.shutdown() ;
                   }
               } catch( Exception e ) {
                 logger.info( "Shutdown request failed: ", e.getMessage(), ", forcing..." ) ;
                 exitStatus = shutdownProcess( true ) ;
                 break ;
               }
               // ... After asking for shutdown, we wait for natural process end. TODO: add timeout?
             case WAIT :
               exitStatus = shutdownProcess( false ) ;
               break ;
             case FORCED :
               shutdownProcess( true ) ;
               break ;
             default :
               throw new IllegalArgumentException( "Unsupported: " + shutdownStyle ) ;
           }
        } finally {
          shutdownProcessQuiet() ;
          cleanup();
         }
       }
     }
     LOGGER.info( "Shutdown (", shutdownStyle, ") complete for ", getNickname(),
         " with exit status code of ", exitStatus, "." ) ;
     return exitStatus ;
   }
 
   private void cleanup() {
     insider = null ;
     try {
       if( heartbeatSender != null ) {
         heartbeatSender.stop() ;
         heartbeatSender = null ;
       }
       if( jmxBeanPool != null ) {
         jmxBeanPool.disconnectAll() ;
       }
       synchronized( processIdentifierLock ) {
         processIdentifier = JavaShellTools.UNDEFINED_PROCESS_ID ;
       }
     } catch( Exception e ) {
       LOGGER.error( e, "Something went wrong during cleanup." ) ;
     }
     LOGGER.info( "Cleanup done for ", getNickname(), "." ) ;
   }
 
 
   private final HeartbeatSender.Notifiee heartbeatSenderNotifiee = new HeartbeatSender.Notifiee() {
     @Override
     public void onUnreachableProcess() {
       handleUnreachableProcess() ;
     }
   } ;
 
   private void handleUnreachableProcess() {
     final Logger logger = LOGGER ;
     synchronized( stateLock ) {
       if( insider != null ) {
         logger.info( "Couldn't send heartbeat to ", getNickname(), ", cleaning up..." );
         shutdownProcessQuiet() ;
         cleanup() ;
       }
     }
   }
 
   private void shutdownProcessQuiet() {
     try {
       shutdownProcess( true ) ;
     } catch( InterruptedException e ) {
       LOGGER.error( e, "Not supposed to happen during a forced shutdown" ) ;
     }
   }
 
 
 // ===
 // JMX
 // ===
 
   public final boolean hasDefaultJmxKit() {
     return jmxKit != null ;
   }
 
   /**
    * Synchronization on {@link #stateLock} left to caller.
    */
   private void connect() throws IOException, InterruptedException {
     if( jmxKit != null ) {
       insider = getManagedBean( Insider.class, Insider.NAME ) ;
     }
   }
 
 
   /**
    * Returns a proxy on a JMX bean in the launched JVM, using default {@link JmxKit}.
    *
    * @param beanClass a non-null object.
    * @param beanName a non-null object.
    * @return a non-null object.
    *
    * @throws IllegalStateException if {@link #hasDefaultJmxKit()} returns {@code false}.
    */
   public < BEAN > BEAN getManagedBean( final Class< BEAN > beanClass, final ObjectName beanName )
         throws IOException, InterruptedException
   {
     Preconditions.checkState( hasDefaultJmxKit() ) ;
     return jmxBeanPool.getManagedBean( beanClass, beanName, jmxKit ) ;
   }
 
 
 // ==============
 // Default values
 // ==============
 
   private static final long STARTUP_TIMEOUT_DURATION = 20L ;
   private static final TimeUnit STARTUP_TIMEOUT_UNIT = TimeUnit.SECONDS ;
 
 
 // =======================================================================
 // Boring stuff to access member variable before superclass initialization
 // =======================================================================
 
   private static final ThreadLocal< Semaphore > THREADLOCAL_SEMAPHORE =
       new ThreadLocal< Semaphore >() ;
 
   private static final ThreadLocal< Integer > THREADLOCAL_PERMITCOUNT =
       new ThreadLocal< Integer >() ;
 
   private static TieredStartupSensor createTieredStartupSensor(
       final Predicate< String >... startupSensors
   ) {
     final Semaphore semaphore = new Semaphore( 0, true ) ;
     final TieredStartupSensor tieredStartupSensor =
         new TieredStartupSensor( semaphore, startupSensors ) ;
     THREADLOCAL_SEMAPHORE.set( semaphore ) ;
     THREADLOCAL_PERMITCOUNT.set( tieredStartupSensor.getInitialPredicateCount() ) ;
     return tieredStartupSensor ;
   }
 
 
 }
