 /*
  * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.sisu.bl.support;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 import static org.sonatype.sisu.filetasks.FileTaskRunner.onDirectory;
 import static org.sonatype.sisu.filetasks.builder.FileRef.file;
 import static org.sonatype.sisu.filetasks.builder.FileRef.path;
 
 import java.io.File;
 import java.util.List;
 import java.util.UUID;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Provider;
 
 import org.apache.tools.ant.DirectoryScanner;
 import org.sonatype.sisu.bl.Bundle;
 import org.sonatype.sisu.bl.BundleConfiguration;
 import org.sonatype.sisu.bl.BundleStatistics;
 import org.sonatype.sisu.bl.internal.support.BundleLifecycle;
 import org.sonatype.sisu.bl.jmx.JMXConfiguration;
 import org.sonatype.sisu.bl.support.port.PortReservationService;
 import org.sonatype.sisu.filetasks.FileTask;
 import org.sonatype.sisu.filetasks.FileTaskBuilder;
 import org.sonatype.sisu.goodies.common.SimpleFormat;
 import org.sonatype.sisu.goodies.common.Time;
 import com.google.common.base.Stopwatch;
 import com.google.common.base.Throwables;
 
 /**
  * Default bundle implementation.
  *
  * @since 1.0
  */
 @Named
 public abstract class DefaultBundle<B extends Bundle, BC extends BundleConfiguration>
     extends BundleLifecycle<B, BC>
     implements Bundle<B, BC>
 {
 
     /**
      * File tasks builder used to manipulate files necessary to prepare bundle target directory.
      * Cannot be null.
      */
     private final FileTaskBuilder fileTaskBuilder;
 
     /**
      * Configuration provider used to create default bundle configurations.
      * Cannot be null.
      */
     private final Provider<BC> configurationProvider;
 
     /**
      * List of running bundles.
      * Cannot be null.
      */
     private final RunningBundles runningBundles;
 
     /**
      * Bundle statistics.
      */
     private final BundleStatistics statistics;
 
     /**
      * Port reservation service used to generate an random port to be used by running application.
      * Cannot be null.
      */
     protected PortReservationService portReservationService;
 
     /**
      * Time it took to boot the application.
      */
     protected Time bootingTime;
 
     /**
      * Bundle name.
      */
     private String name;
 
     /**
      * Bundle configuration.
      * Cannot be null.
      */
     private BC configuration;
 
     /**
      * True if application is running, false otherwise.
      */
     private boolean running;
 
     private Integer jmxRemotePort;
 
     /**
      * temp holder of the time in seconds until application became alive
      */
     private long secondsUntilAlive = 0;
 
 
     /**
      * Constructor. Creates the bundle with a default configuration and a not running state.
      *
      * @param name                  application name
      * @param configurationProvider configuration provider
      * @param runningBundles        running bundles
      * @param fileTaskBuilder       file task builder
      */
     @Inject
     public DefaultBundle( final String name,
                           final Provider<BC> configurationProvider,
                           final RunningBundles runningBundles,
                           final FileTaskBuilder fileTaskBuilder,
                           final PortReservationService portReservationService )
     {
         this.name = checkNotNull( name );
         this.configurationProvider = checkNotNull( configurationProvider );
         this.runningBundles = checkNotNull( runningBundles );
         this.fileTaskBuilder = checkNotNull( fileTaskBuilder );
         this.portReservationService = checkNotNull( portReservationService );
         bootingTime = Time.millis( 0 );
         statistics = new Statistics();
     }
 
     /**
      * Starts application and waits for it to boot. if successfully started sets the state to running.
      * <p/>
      * {@inheritDoc}
      *
      * @throws Exception if a problem occurred during startup of application, wait period or it could not determine if
      *                   application is started in specified timeout
      * @see Bundle#start()
      */
     @Override
     public void doStart()
     {
         bootingTime = Time.millis( 0 );
         final Stopwatch bootingWatch = new Stopwatch();
         try
         {
             startApplication();
             running = true;
             getRunningBundles().add( this );
             bootingWatch.start();
             waitForBoot();
         }
         catch ( RuntimeException e )
         {
             doStop();
             throw e;
         }
         finally
         {
             bootingWatch.stop();
             bootingTime = Time.millis( bootingWatch.elapsedMillis() );
         }
     }
 
     /**
      * Stops application, if running.
      * <p/>
      * {@inheritDoc}
      *
      * @see Bundle#stop()
      */
     @Override
     public void doStop()
     {
         if ( running )
         {
             try
             {
                 stopApplication();
             }
             finally
             {
                 unconfigure();
                 running = false;
                 getRunningBundles().remove( this );
             }
         }
     }
 
     /**
      * Prepares application target directory for running by:<br/>
      * - ensuring a valid configuration<br/>
      * - cleanup of target directory<br/>
      * - unpacking bundle<br/>
      * - configure<br/>
      * - applying overlays
      * <p/>
      * {@inheritDoc}
      */
     @Override
     public void doPrepare()
     {
         bootingTime = Time.millis( 0 );
         log.debug( "Using configuration {}", getConfiguration() );
         validateConfiguration();
         createBundle();
         renameApplicationDirectory();
         try
         {
             configure();
         }
         catch ( Exception e )
         {
             throw Throwables.propagate( e );
         }
         applyOverlays();
     }
 
     @Override
     public void doClean()
     {
         bootingTime = Time.millis( 0 );
         deleteTarget();
     }
 
     @Override
     public BundleStatistics statistics()
     {
         return statistics;
     }
 
     protected FileTaskBuilder getFileTaskBuilder()
     {
         checkState( fileTaskBuilder != null );
         return fileTaskBuilder;
     }
 
     protected RunningBundles getRunningBundles()
     {
         checkState( runningBundles != null );
         return runningBundles;
     }
 
     @Override
     public BC getConfiguration()
     {
         if ( this.configuration == null )
         {
             this.configuration = configurationProvider.get();
             if ( configuration.getId() == null )
             {
                 configuration.setId( generateId() );
             }
         }
         return configuration;
     }
 
     @Override
     public B setConfiguration( BC configuration )
     {
         this.configuration = configuration;
         return (B) this;
     }
 
     @Override
     public boolean isRunning()
     {
         return running;
     }
 
     /**
      * Starts the application.
      */
     protected abstract void startApplication();
 
     /**
      * Stops the application.
      */
     protected abstract void stopApplication();
 
     /**
      * Template method for subclasses to perform configuration tasks when bundle is starting, if necessary.
      */
     protected void configure()
         throws Exception
     {
         JMXConfiguration jmxConfig = getConfiguration().getJmxConfiguration();
         if ( JMXConfiguration.RANDOM_JMX_REMOTE_PORT.equals( jmxConfig.getRemotePort() ) )
         {
             this.jmxRemotePort = getPortReservationService().reservePort();
         }
     }
 
     /**
      * Template method for subclasses to perform un-configuration tasks when bundle is stopping, if necessary.
      */
     protected void unconfigure()
     {
         JMXConfiguration jmxConfig = getConfiguration().getJmxConfiguration();
         if ( JMXConfiguration.RANDOM_JMX_REMOTE_PORT.equals( jmxConfig.getRemotePort() ) && this.jmxRemotePort != null )
         {
             getPortReservationService().cancelPort( this.jmxRemotePort );
         }
         this.jmxRemotePort = null;
     }
 
     /**
      * Checks if application is alive.
      *
      * @return true if application is alive, false otherwise
      */
     protected boolean applicationAlive()
     {
         return true;
     }
 
     /**
      * Generates a random id for application.
      *
      * @return generated id
      * @since 1.2
      */
     protected String generateId()
     {
         return name + "-" + UUID.randomUUID();
     }
 
     /**
      * Renames application directory that usually has a name that contains the version to a name easy to be used in
      * overlays = application name.
      */
     private void renameApplicationDirectory()
     {
         BundleConfiguration config = getConfiguration();
 
         DirectoryScanner ds = new DirectoryScanner();
         ds.setBasedir( config.getTargetDirectory() );
         ds.setIncludes( new String[]{ name + "-*" } );
         ds.scan();
         String[] dirs = ds.getIncludedDirectories();
 
         if ( dirs.length == 1 && new File( config.getTargetDirectory(), dirs[0] ).exists() )
         {
             onDirectory( config.getTargetDirectory() ).apply(
                 getFileTaskBuilder().rename( path( dirs[0] ) )
                     .to( name )
             );
         }
     }
 
     /**
      * Waits for application to boot for configured timeout period.
      */
     private void waitForBoot()
     {
         long start = System.currentTimeMillis();
         int startTimeout = getConfiguration().getStartTimeout();
 
        log.info( "{} ({}) waiting to boot for {} seconds", startTimeout );
 
         final boolean applicationAlive = new TimedCondition()
         {
             @Override
             protected boolean isSatisfied()
             {
                 return applicationAlive();
             }
         }.await( Time.seconds( startTimeout ) );
         this.secondsUntilAlive = ( System.currentTimeMillis() - start ) / 1000;
         if ( applicationAlive )
         {
             logApplicationIsAlive();
         }
         else
         {
             throw new RuntimeException(
                 SimpleFormat.format("Did not detect %s (%s) running in the configured timeout of %s seconds", getName(), getConfiguration().getId(), startTimeout)
             );
         }
     }
 
     /**
      * Template method to eventually log the fact that application is alive.
      */
     protected void logApplicationIsAlive()
     {
         log.info( "{} ({}) started in {} seconds", getName(), getConfiguration().getId(), secondsUntilAlive);
     }
 
     /**
      * @return the name this bundle was created with
      */
     protected String getName()
     {
         return name;
     }
 
     /**
      * Bundle specific jmx port, possibly altered from what may have been originally configured as part of
      * {@link JMXConfiguration}.
      */
     protected Integer getJmxRemotePort()
     {
         return jmxRemotePort == null ? getConfiguration().getJmxConfiguration().getRemotePort() : this.jmxRemotePort;
     }
 
     /**
      * Creates application in target directory by unpacking the bundle or copying it if bundle is a directory.
      *
      * @since 1.2
      */
     protected void createBundle()
     {
         BundleConfiguration config = getConfiguration();
         File bundle = config.getBundle();
         if ( bundle == null )
         {
             return;
         }
         if ( bundle.isDirectory() )
         {
             onDirectory( config.getTargetDirectory() ).apply(
                 getFileTaskBuilder().copy().directory( file( bundle ) )
                     .to().directory( path( "/" ) )
             );
         }
         else
         {
             onDirectory( config.getTargetDirectory() ).apply(
                 getFileTaskBuilder().expand( file( bundle ) )
                     .to().directory( path( "/" ) )
             );
         }
     }
 
     /**
      * Validates configuration:<br/>
      * - id is set
      * - bundle is set<br/>
      * - target directory is set
      *
      * @throws RuntimeException if any of above is not true
      */
     private void validateConfiguration()
         throws RuntimeException
     {
         BundleConfiguration config = getConfiguration();
         if ( config.getId() == null || config.getId().trim().length() == 0 )
         {
             throw new RuntimeException( "Id must be set in bundle configuration" );
         }
         if ( config.getBundle() == null )
         {
             log.warn( "There is no bundle to be created." );
         }
         if ( config.getTargetDirectory() == null )
         {
             throw new RuntimeException( "Target directory must be set in bundle configuration" );
         }
     }
 
     /**
      * Deletes target directory.
      */
     private void deleteTarget()
     {
         final File targetDirectory = getConfiguration().getTargetDirectory();
         if ( targetDirectory == null )
         {
             throw new RuntimeException( "Target directory must be set in bundle configuration" );
         }
         onDirectory( targetDirectory ).apply(
             getFileTaskBuilder().delete().directory( path( "/" ) )
         );
     }
 
     /**
      * Applies overlays to target directory.
      */
     private void applyOverlays()
     {
         BundleConfiguration config = getConfiguration();
         List<FileTask> overlays = config.getOverlays();
         if ( overlays == null )
         {
             return;
         }
         onDirectory( config.getTargetDirectory() ).apply( overlays );
     }
 
     @Override
     public String toString()
     {
         StringBuilder sb = new StringBuilder();
         sb.append( getName() ).append( " bundle" );
         sb.append( " [id: " ).append( getConfiguration().getId() ).append( "]" );
         sb.append( isRunning() ? " [running]" : " [not running]" );
         return sb.toString();
     }
 
     protected PortReservationService getPortReservationService()
     {
         checkState( portReservationService != null );
         return portReservationService;
     }
 
     private class Statistics
         implements BundleStatistics
     {
 
         @Override
         public Time cleanupTime()
         {
             return cleanupTime;
         }
 
         @Override
         public Time preparationTime()
         {
             return preparationTime;
         }
 
         @Override
         public Time startupTime()
         {
             return startupTime;
         }
 
         @Override
         public Time bootingTime()
         {
             return bootingTime;
         }
 
         @Override
         public Time stoppingTime()
         {
             return stoppingTime;
         }
     }
 
 }
