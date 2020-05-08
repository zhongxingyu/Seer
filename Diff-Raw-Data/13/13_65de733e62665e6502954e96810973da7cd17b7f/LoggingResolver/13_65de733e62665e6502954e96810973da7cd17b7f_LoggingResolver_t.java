 package org.mosaic.launcher.logging;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.status.ErrorStatus;
 import ch.qos.logback.core.status.Status;
 import ch.qos.logback.core.status.StatusChecker;
 import ch.qos.logback.core.util.StatusPrinter;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.file.DirectoryStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import javax.annotation.Nonnull;
 import org.mosaic.launcher.SystemError;
 import org.mosaic.launcher.home.HomeResolver;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import static ch.qos.logback.core.status.StatusUtil.filterStatusListByTimeThreshold;
 import static org.mosaic.launcher.SystemError.bootstrapError;
 import static org.mosaic.launcher.home.HomeResolver.getWelcomeLines;
 
 /**
  * @author arik
  */
 public class LoggingResolver
 {
     public static void initServerLogging()
     {
         // clean logs directory if we're in dev mode
         if( Boolean.getBoolean( "dev" ) )
         {
             System.out.println( "Cleaning logs directory..." );
             try( DirectoryStream<Path> stream = Files.newDirectoryStream( HomeResolver.logs ) )
             {
                 for( Path path : stream )
                 {
                     deletePath( path );
                 }
             }
             catch( IOException e )
             {
                 throw bootstrapError( "Could not clean logs directory at '" + HomeResolver.logs + "': " + e.getMessage(), e );
             }
         }
 
         org.mosaic.launcher.logging.AppenderRegistry appenderRegistry = new org.mosaic.launcher.logging.AppenderRegistry();
 
         // obtain logger context from Logback
         LoggerContext lc = ( LoggerContext ) LoggerFactory.getILoggerFactory();
         lc.reset();
 
         // disable logback packaging source calculation (causes problems when bundles disappear, on felix shutdown, etc)
         lc.setPackagingDataEnabled( false );
 
         // apply built-in configuration
         applyBuiltinLogbackConfiguration( lc, appenderRegistry );
 
         // apply logback configuration found (optionally) in the server home
         applyServerLogbackConfiguration( lc, appenderRegistry );
 
         // install JUL-to-SLF4J adapter
         SLF4JBridgeHandler.removeHandlersForRootLogger();
         SLF4JBridgeHandler.install();
 
         // install a default uncaught-exception-handler for threads which logs the exception to slf4j
         Thread.setDefaultUncaughtExceptionHandler( new Thread.UncaughtExceptionHandler()
         {
             @Override
             public void uncaughtException( Thread t, Throwable e )
             {
                 LoggerFactory.getLogger( "org.mosaic" ).error( e.getMessage(), e );
             }
         } );
 
         // print mosaic header to log file
         // when in dev mode, both the stdout AND the logging output go to the console, so avoid printing this twice
         StringBuilder buffer = new StringBuilder( 5000 );
         for( String line : getWelcomeLines() )
         {
             buffer.append( line ).append( '\n' );
         }
         LoggerFactory.getLogger( "org.mosaic" ).info( "\n\n" + buffer + "\n\n" );
     }
 
     private static void deletePath( @Nonnull Path path ) throws IOException
     {
        if( Files.isDirectory( path ) )
         {
            try( DirectoryStream<Path> stream = Files.newDirectoryStream( path ) )
             {
                for( Path child : stream )
                 {
                     LoggingResolver.deletePath( child );
                 }
             }
         }
         Files.delete( path );
     }
 
     private static void applyBuiltinLogbackConfiguration( @Nonnull LoggerContext lc,
                                                           @Nonnull org.mosaic.launcher.logging.AppenderRegistry appenderRegistry )
     {
         URL builtInLogbackConfig = org.mosaic.launcher.logging.LoggingResolver.class.getClassLoader().getResource( "logback-builtin.xml" );
         if( builtInLogbackConfig != null )
         {
             JoranConfigurator configurator = new LogbackBuiltinConfigurator( appenderRegistry );
             configurator.setContext( lc );
             try
             {
                 configurator.doConfigure( builtInLogbackConfig );
                 checkForErrors( lc );
             }
             catch( JoranException e )
             {
                 throw SystemError.bootstrapError( "Error while applying built-in Logback configuration: %s", e, e.getMessage() );
             }
         }
         else
         {
             throw SystemError.bootstrapError( "Could not find built-in Logback configuration in class-path at '/logback-builtin.xml'" );
         }
     }
 
     private static void applyServerLogbackConfiguration( @Nonnull LoggerContext lc,
                                                          @Nonnull AppenderRegistry appenderRegistry )
     {
         Path logbackConfigFile = HomeResolver.etc.resolve( "logback.xml" );
         if( Files.exists( logbackConfigFile ) )
         {
             try
             {
                 LogbackRestrictedConfigurator configurator = new LogbackRestrictedConfigurator( appenderRegistry );
                 configurator.setContext( lc );
                 configurator.doConfigure( logbackConfigFile.toFile() );
                 checkForErrors( lc );
             }
             catch( JoranException e )
             {
                 throw SystemError.bootstrapError( "Error while applying Logback configuration in '%s': %s", e, logbackConfigFile, e.getMessage() );
             }
         }
     }
 
     private static void checkForErrors( @Nonnull LoggerContext lc )
     {
         if( new StatusChecker( lc ).getHighestLevel( 0 ) >= ErrorStatus.WARN )
         {
             System.out.println();
             System.out.printf( "LOGGING CONFIGURATION ERRORS DETECTED:\n" );
             System.out.println();
             System.out.println();
             StatusPrinter.printInCaseOfErrorsOrWarnings( lc );
 
             StringBuilder sb = new StringBuilder();
             for( Status s : filterStatusListByTimeThreshold( lc.getStatusManager().getCopyOfStatusList(), 0 ) )
             {
                 StatusPrinter.buildStr( sb, "", s );
             }
 
             throw SystemError.bootstrapError( "LOGGING CONFIGURATION ERRORS DETECTED:\n" + sb );
         }
     }
 }
