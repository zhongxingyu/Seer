 package com.terradue.dsi;
 
 /*
  *  Copyright 2012 Terradue srl
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import static com.google.inject.Guice.createInjector;
 import static com.google.inject.name.Names.named;
 import static java.lang.Runtime.getRuntime;
 import static java.lang.String.format;
 import static java.lang.System.currentTimeMillis;
 import static java.lang.System.getProperty;
 import static java.lang.System.setProperty;
 import static org.nnsoft.guice.rocoto.Rocoto.expandVariables;
 import static org.slf4j.LoggerFactory.getILoggerFactory;
 import static org.slf4j.LoggerFactory.getLogger;
 
 import java.io.File;
 import java.util.Date;
 
 import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
 import org.slf4j.Logger;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.google.inject.Inject;
 import com.sun.jersey.api.client.Client;
 import com.terradue.dsi.wire.RestClientModule;
 
 abstract class BaseTool
     extends ConfigurationModule
     implements Tool
 {
 
     protected final Logger logger = getLogger( getClass() );
 
     @Parameter( names = { "-h", "--help" }, description = "Display help information." )
     private boolean printHelp;
 
     @Parameter( names = { "-v", "--version" }, description = "Display version information." )
     private boolean showVersion;
 
     @Parameter( names = { "-X", "--debug" }, description = "Produce execution debug output." )
     private boolean debug;
 
     @Parameter( names = { "-H", "--host" }, description = "The DSI web service URI." )
     protected String serviceHost = "94.100.247.129";
 
     @Parameter( names = { "-u", "--username" }, description = "The DSI account username." )
     private String username;
 
     @Parameter( names = { "-p", "--password" }, description = "The DSI account password." )
     private String password;
 
     private File dsiCertificate;
 
     // injected
 
     @Inject
     protected Client restClient;
 
     protected String serviceUrl;
 
     public void setRestClient( Client restClient )
     {
         this.restClient = restClient;
     }
 
     public void setServiceUrl( String serviceUrl )
     {
         this.serviceUrl = serviceUrl;
     }
 
     @Override
     public final int execute( String... args )
     {
         final JCommander commander = new JCommander( this );
         commander.setProgramName( getProperty( "app.name" ) );
 
         commander.parse( args );
 
         if ( printHelp )
         {
             commander.usage();
             return -1;
         }
 
         if ( showVersion )
         {
             printVersionInfo();
             return -1;
         }
 
         if ( debug )
         {
             setProperty( "log.level", "DEBUG" );
         }
         else
         {
             setProperty( "log.level", "INFO" );
         }
 
         // assume SLF4J is bound to logback in the current environment
         final LoggerContext lc = (LoggerContext) getILoggerFactory();
 
         try
         {
             JoranConfigurator configurator = new JoranConfigurator();
             configurator.setContext( lc );
             // the context was probably already configured by default configuration
             // rules
             lc.reset();
             configurator.doConfigure( getClass().getClassLoader().getResourceAsStream( "logback-config.xml" ) );
         }
         catch ( JoranException je )
         {
             // StatusPrinter should handle this
         }
 
         // validation
 
         if ( username == null )
         {
             return printAndExit( "DSI Username not specified. Please type `%s -h` for the usage." );
         }
 
         if ( password == null )
         {
             return printAndExit( "DSI Password not specified. Please type `%s -h` for the usage." );
         }
 
         dsiCertificate = new File( getProperty( "basedir" ), format( "certs/%s.pem", username ) );
         if ( !dsiCertificate.exists() )
         {
             return printAndExit( "DSI certificate %s does not exist, put %s.pem certificate under %s/certs directory",
                                  dsiCertificate, username, getProperty( "basedir" ) );
         }
 
         logger.info( "" );
         logger.info( "------------------------------------------------------------------------" );
         logger.info( "{}", getProperty( "app.name" ) );
         logger.info( "------------------------------------------------------------------------" );
         logger.info( "" );
 
         long start = currentTimeMillis();
         int exit = 0;
 
         Throwable error = null;
 
         try
         {
             createInjector( expandVariables( this ), new RestClientModule() ).injectMembers( this );
             execute();
         }
         catch ( Throwable t )
         {
             exit = -1;
             error = t;
         }
         finally
         {
             logger.info( "" );
             logger.info( "------------------------------------------------------------------------" );
             logger.info( "{} {}", getProperty( "app.name" ), ( exit < 0 ) ? "FAILURE" : "SUCCESS" );
 
             if ( exit < 0 )
             {
                 logger.info( "" );
 
                if ( debug )
                 {
                     logger.error( "Execution terminated with errors", error );
                 }
                 else
                 {
                     logger.error( "Execution terminated with errors: {}", error.getMessage() );
                 }
 
                 logger.info( "" );
             }
 
             logger.info( "Total time: {}s", ( ( currentTimeMillis() - start ) / 1000 ) );
             logger.info( "Finished at: {}", new Date() );
 
             final Runtime runtime = getRuntime();
             final int megaUnit = 1024 * 1024;
             logger.info( "Final Memory: {}M/{}M", ( runtime.totalMemory() - runtime.freeMemory() ) / megaUnit,
                          runtime.totalMemory() / megaUnit );
 
             logger.info( "------------------------------------------------------------------------" );
         }
 
         return exit;
     }
 
     protected abstract void execute()
         throws Exception;
 
     @Override
     protected void bindConfigurations()
     {
         bindSystemProperties();
 
         // commons settings
         bindProperty( "dsi.username" ).toValue( username );
         bindProperty( "dsi.password" ).toValue( password );
 
         bindProperty( "service.host" ).toValue( serviceHost );
         bindProperty( "service.url" ).toValue( "https://${service.host}/services/api" );
 
         // services
         bindProperty( "service.appliances" ).toValue( "${service.url}/appliances" );
         bindProperty( "service.upload" ).toValue( "${service.appliances}/uploadTicket" );
         bindProperty( "service.deployments" ).toValue( "${service.url}/deployments" );
         bindProperty( "service.accounts" ).toValue( "${service.url}/accounts" );
         bindProperty( "service.clouds" ).toValue( "${service.url}/clouds" );
         bindProperty( "service.storages" ).toValue( "${service.url}/networkStorages" );
 
         // certificate
         bind( File.class ).annotatedWith( named( "user.certificate" ) ).toInstance( dsiCertificate );
     }
 
     private static int printAndExit( String messageTemplate, Object...args )
     {
         System.out.printf( messageTemplate, args );
         return -1;
     }
 
     private static void printVersionInfo()
     {
         System.out.printf( "%s %s%n",
                            getProperty( "project.name" ),
                            getProperty( "project.version" ) );
         System.out.printf( "Java version: %s, vendor: %s%n",
                            getProperty( "java.version" ),
                            getProperty( "java.vendor" ) );
         System.out.printf( "Java home: %s%n", getProperty( "java.home" ) );
         System.out.printf( "Default locale: %s_%s, platform encoding: %s%n",
                            getProperty( "user.language" ),
                            getProperty( "user.country" ),
                            getProperty( "sun.jnu.encoding" ) );
         System.out.printf( "OS name: \"%s\", version: \"%s\", arch: \"%s\", family: \"%s\"%n",
                            getProperty( "os.name" ),
                            getProperty( "os.version" ),
                            getProperty( "os.arch" ),
                            getOsFamily() );
     }
 
     private static final String getOsFamily()
     {
         String osName = getProperty( "os.name" ).toLowerCase();
         String pathSep = getProperty( "path.separator" );
 
         if ( osName.indexOf( "windows" ) != -1 )
         {
             return "windows";
         }
         else if ( osName.indexOf( "os/2" ) != -1 )
         {
             return "os/2";
         }
         else if ( osName.indexOf( "z/os" ) != -1 || osName.indexOf( "os/390" ) != -1 )
         {
             return "z/os";
         }
         else if ( osName.indexOf( "os/400" ) != -1 )
         {
             return "os/400";
         }
         else if ( pathSep.equals( ";" ) )
         {
             return "dos";
         }
         else if ( osName.indexOf( "mac" ) != -1 )
         {
             if ( osName.endsWith( "x" ) )
             {
                 return "mac"; // MACOSX
             }
             return "unix";
         }
         else if ( osName.indexOf( "nonstop_kernel" ) != -1 )
         {
             return "tandem";
         }
         else if ( osName.indexOf( "openvms" ) != -1 )
         {
             return "openvms";
         }
         else if ( pathSep.equals( ":" ) )
         {
             return "unix";
         }
 
         return "undefined";
     }
 
 }
