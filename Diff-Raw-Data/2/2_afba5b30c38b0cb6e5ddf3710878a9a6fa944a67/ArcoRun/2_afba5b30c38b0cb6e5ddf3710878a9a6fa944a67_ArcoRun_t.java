 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.arco;
 
 import com.sun.grid.logging.SGELog;
 import com.sun.grid.arco.sql.ArcoDbConnectionPool;
 import java.io.*;
 import java.util.Properties;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.transform.TransformerException;
 import com.sun.grid.arco.model.NamedObject;
 import com.sun.grid.arco.model.Query;
 import com.sun.grid.arco.model.StorageType;
 import com.sun.grid.arco.sql.SQLQueryResult;
 import com.sun.grid.logging.SGEFormatter;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Locale;
 
 /**
  * Command line to for ARCo
  * with this class ARCo queries
  */
 public class ArcoRun {
    /** In this mode arcorun will export the result of a query */
    public static final int MODE_EXPORT = 0;
    /** In this mode arcorun will list the names of all available queries */
    public static final int MODE_LIST   = 1;
    /** In this mode arcorun will print a usage message */
    public static final int MODE_HELP   = 2;
    /** In this mode arcorun will print the version string */
    public static final int MODE_VERSION = 3;
    /** the default log level */
    public static final Level DEFAULT_LOG_LEVEL        =  Level.INFO;
    /** export the query result as xml */
    public static final int OUTPUT_FORMAT_XML  = 0;
    /** export the query result as csv */
    public static final int OUTPUT_FORMAT_CSV  = 1;
    /** export the query result as pdf */
    public static final int OUTPUT_FORMAT_PDF  = 2;
    /** export the query result as html */
    public static final int OUTPUT_FORMAT_HTML = 3;
    
    /** Logger of this application */
    private Logger logger;
    /** The log hander for this appliation */
    private ConsoleHandler consoleHandler;
    /** The log level for this application */
    private Level logLevel = Level.INFO;
    /** The arco configuration file */
    private File arcoConfigFile;
    /** the output file */
    private File outputFile = null;
    /** the output format */
    private int outputFormat = OUTPUT_FORMAT_XML;
    /** the latebinging properties */
    private Properties latebindings = new Properties();
    /** name of the query */
    private String queryName;
    /** name of the cluster on which to execute the query*/
    private String clusterName = "";
    /** name of the result, if null it is taken from the query */
    private String resultName;
    
    /** the mode of the application */
    private int mode = MODE_EXPORT;
    /** directory where arco is installed ( System property arco.home) */
    private File arcoHome;
    
    /**
     *  Run the application
     */
    public void run() throws IOException, TransformerException, ArcoException {
       switch( mode ) {
          case MODE_EXPORT:    export(); break;
          case MODE_HELP:      usage();  break;
          case MODE_LIST:      list();   break;
          case MODE_VERSION:   System.out.println( getVersion() );
          break;
          default:
             throw new IllegalArgumentException("Unknown mode " + mode );
       }
    }
    
    /**
     *  get the version string of this application
     *  @return the version string in the form "<product> <version> - arcorun"
     */
    public static String getVersion() {
       return ArcoVersion.PLUGIN_NAME + " - " +
             ArcoVersion.VERSION + " - arcorun";
    }
    
    private void initConfigFile() {
       // Setup the configuration file
       String cf = System.getProperty("arco.config");
       arcoConfigFile = new File(cf + File.separator +"config.xml");
    }
    
    /**
     *  list all available queries
     */
    private void list() throws ArcoException {
       
       ArcoDbConnectionPool dbConnections = ArcoDbConnectionPool.getInstance();
       
       dbConnections.setConfigurationFile( arcoConfigFile );
       
       StorageType storage = dbConnections.getConfig().getStorage();
       
       File storageDir = new File( storage.getRoot() );
       File queryDir = new File( storageDir, storage.getQueries() );
       
       QueryManager queryManager = new QueryManager(queryDir);
       
       NamedObject [] queries = queryManager.getAvailableObjects();
       
       for( int i = 0; i < queries.length; i++ ) {
          System.out.println( queries[i].getName() );
       }
    }
    
    /**
     * Loads and executes a query. It saves the result as xml and
     * exports it to cvs.
     * @throws ArcoException   on any expected error
     * @throws IOException     if the result could not be written due to an I/O Error
     * @throws javax.xml.transform.TransformerException  if a transformation with a XSL stylesheet failed
     */
    public void export() throws IOException, TransformerException, ArcoException {
       SGELog.entering( getClass(), "run" );
       int clusterId = 0;
       
       ArcoDbConnectionPool dbConnections = ArcoDbConnectionPool.getInstance();
       
       dbConnections.setConfigurationFile( arcoConfigFile );
       try {
          dbConnections.init();
       } catch (SQLException sqle) {
          throw new ArcoException("ArcoRun.poolError");
       }
       
       if (!clusterName.equals("")) {  
          clusterId = dbConnections.getClusterIndex(clusterName);
       }
       
       if (clusterId == -1) {
           throw new ArcoException("ArcoRun.invalidClusterName", new Object [] {clusterName});
       }
       
       StorageType storage = dbConnections.getConfig().getStorage();
       
       File storageDir = new File( storage.getRoot() );
       File queryDir = new File( storageDir, storage.getQueries() );
       File resultDir = new File( storageDir, storage.getResults() );
       
       QueryManager queryManager = new QueryManager( queryDir );
       
       Query query = queryManager.getQueryByName(queryName);
       
       if (query == null) {
          throw new ArcoException("ArcoRun.queryNotFound", new Object [] { queryName });
       }
       
       //clusterName must be set on QueryType, before the query si executed
       query.setClusterName(clusterName);
       QueryResult queryResult = new SQLQueryResult(query, dbConnections);
       
       if(queryResult.hasLateBinding() ) {
          Enumeration propNameEnum = latebindings.propertyNames();
          String propName = null;
          while( propNameEnum.hasMoreElements() ) {
             propName = (String)propNameEnum.nextElement();
             queryResult.setLateBinding(propName,  latebindings.getProperty(propName ) );
          }
          
          Collection lateBindingNames = queryResult.getLateBindingsNames();
          Iterator iter = lateBindingNames.iterator();
          while(iter.hasNext()) {
             propName = (String)iter.next();
             if( queryResult.getLateBinding(propName) == null ) {
                throw new ArcoException("ArcoRun.missingLatebinding",
                      new Object[] { propName } );
             }
          }
       }
       
       queryResult.execute();
       
       if( queryResult.getRowCount() > 0 ) {
          
          if(this.resultName != null ) {
             queryResult.getQuery().setName(resultName);
          }
          
          ResultExportManager exportManager = new ResultExportManager( arcoHome );
          
          switch( outputFormat ) {
             case OUTPUT_FORMAT_XML:
                exportManager.export( ResultExportManager.TYPE_XML, queryResult,createOutputStream(), Locale.getDefault() );
                break;
             case OUTPUT_FORMAT_CSV:
                exportManager.export( ResultExportManager.TYPE_CSV,  queryResult,createOutputStream(), Locale.getDefault() );
                break;
             case OUTPUT_FORMAT_PDF:
                exportManager.export( ResultExportManager.TYPE_PDF,  queryResult,createOutputStream(), Locale.getDefault() );
                break;
             case OUTPUT_FORMAT_HTML:
                exportManager.export( ResultExportManager.TYPE_HTML, queryResult, outputFile, Locale.getDefault() );
                break;
             default:
                throw new IllegalStateException("unknown output format " + outputFormat );
          }
       } else {
          SGELog.warning( "ArcoRun.noResult" );
       }
       dbConnections.releaseConnections();
       SGELog.exiting( getClass(), "run" );
    }
    
    /**
     *  create the output stream where the result of the export will be stored
     *  @return the output stream
     *  @throws com.sun.grid.arco.ArcoException if the stream could not be created
     */
    private OutputStream createOutputStream() throws ArcoException {
       try {
          if( this.outputFile == null ) {
             return System.out;
          } else {
             return new FileOutputStream( outputFile );
          }
       } catch( IOException ioe ) {
          throw new ArcoException("ArcoRun.outputFileOpenError", ioe,
                new Object[] { ioe.getMessage() } );
       }
    }
    
    /**
     *   intializes the logging
     */
    private void initLogging() {
       
       int columns [] = {
          SGEFormatter.COL_LEVEL_LONG, SGEFormatter.COL_MESSAGE
       };
       
       logger = Logger.getLogger( "arcoLogger", "com.sun.grid.arco.web.arcomodule.Resources" );
       
       consoleHandler = new ConsoleHandler();
       SGEFormatter  formatter = new SGEFormatter( "arcorun", false, columns );
       
       formatter.setDelimiter( ": " );
       
       consoleHandler.setFormatter( formatter );
       logger.addHandler( consoleHandler );
       logger.setUseParentHandlers( false );
       
       SGELog.init( logger );
    }
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       
       ArcoRun app = new ArcoRun();
       
       //set the default config file from SWC property
       app.initConfigFile();
       app.initLogging();
       int exit = 0;
       try {
          
          try {
             app.setArguments(args);
          } catch( ArcoException ae ) {
             SGELog.severe_p(ae, ae.getMessage(), ae.getParameter() );
             app.usage();
             exit = -1;
          }
          if( exit == 0 ) {
             app.run();
          }
          
       } catch( ArcoException ae ) {
          SGELog.severe_p( ae, ae.getMessage(), ae.getParameter() );
          exit = -2;
       } catch (Exception ex) {
          SGELog.severe( ex, "Unexpected Error: {0}", ex.getMessage() );
          exit = -3;
       }
       System.exit( exit );
    }
    
    
    /**
     * Set the command line arguments
     * @param args list of commandline parameters
     * @throws ArcoException if the argument are not valid
     */
    public void setArguments(String[] args) throws ArcoException {
       if (args.length < 1) {
          throw new ArcoException("ArcoRun.illegalArgumentCount");
       }
       
       String arcoHomeStr =  System.getProperty( "arco.home" );
       
       if( arcoHomeStr == null ) {
          throw new ArcoException("ArcoRun.missingArcoHome");
       }
       
       arcoHome = new File( arcoHomeStr );
       
       if( !arcoHome.isDirectory() ) {
          throw new ArcoException("ArcoRun.arcoHomeIsNoDirectory", new Object[] { arcoHome } );
       }
       // parse the options
       
       for( int i = 0; i < args.length; i++ ) {
          if( args[i].equals( "-c" ) ) {
             i++;
             if( i >= args.length ) {
                throw new ArcoException("ArcoRun.arcoConfFileisMissing");
             }
             arcoConfigFile = new File( args[i] );
          } else if ( args[i].equals( "-d" ) ) {
             i++;
             if( i >= args.length ) {
                throw new ArcoException("ArcoRun.debugLevelIsMissing");
             }
             try {
                logLevel = Level.parse( args[i] );
                SGELog.info( "set debug level to {0}", logLevel.toString() );
                logger.setLevel( logLevel );
                consoleHandler.setLevel( logLevel );
             } catch( IllegalArgumentException ile ) {
                throw new ArcoException( "ArcoRun.invalidLogLevel", new Object[] { args[i] } );
             }
          } else if ( args[i].equals( "-o" ) ) {
             i++;
             if( i >= args.length ) {
                throw new ArcoException( "ArcoRun.outputFileMissing");
             }
             outputFile = new File( args[i] );
          } else if ( args[i].equals( "-f" ) ) {
             i++;
             if( i >= args.length ) {
                throw new ArcoException( "ArcoRun.outputFormatMissing");
             }
             
             outputFormat = strToOutputFormat( args[i] );
             if( outputFormat < 0 ) {
                throw new ArcoException( "ArcoRun.unknownOutputFormat", new Object[] {args[i]} );
             }
          } else if( args[i].equals( "-lb" ) ) {
             i++;
             if( i >= args.length ) {
                throw new ArcoException("ArcoRun.missingLateBindingParameter");
             }
             int index = args[i].indexOf( '=' );
             if( index <= 0 ) {
                throw new ArcoException("ArcoRun.invalidLateBindingParameter", new Object[] { args[i] } );
             }
             String name  = args[i].substring( 0, index ).trim();
             String value = args[i].substring( index + 1 ,args[i].length() ).trim();
             
             SGELog.fine( "late binding {0} = {1}", name, value );
             latebindings.setProperty( name, value );
          } else if ( args[i].equals( "-lbfile" ) ) {
             
             i++;
             if( i >= args.length ) {
                throw new ArcoException("ArcoRun.missingLateBindingFile");
             }
             
             Properties props = new Properties();
             
             try {
                FileInputStream fin = new FileInputStream( args[i] );
                props.load( fin );
                
                Enumeration names = props.propertyNames();
                String name = null;
                String value = null;
                while( names.hasMoreElements() ) {
                   name = (String)names.nextElement();
                   value = props.getProperty( name );
                   SGELog.fine( "late binding {0} = {1}", name, value );
                   latebindings.setProperty( name, value );
                }
             } catch( IOException ioe ) {
                throw new ArcoException("ArcoRun.latebindingIOError", ioe, new Object[] { args[i], ioe.getMessage() } );
             }
          } else if ( args[i].equals("-n")) {
             i++;
             if(i >= args.length ) {
                throw new ArcoException("ArcoRun.missingResultName");
             }
             this.resultName = args[i];
             SGELog.fine("resultName is {0}", resultName);
          } else if (args[i].equals("-cl")) {
             i++;
             if(i >= args.length ) {
                throw new ArcoException("ArcoRun.missingClusterName");
             }
             this.clusterName = args[i];
             SGELog.info("clusterName is {0}", clusterName);
          } else if ( args[i].equals( "-l" ) ) {
             mode = MODE_LIST;
          } else if ( args[i].equals( "-help" ) || args[i].equals("-?") ) {
             mode = MODE_HELP;
          } else if ( args[i].equals( "-v" ) ) {
             mode = MODE_VERSION;
          } else if( i == args.length - 1 ) {
             queryName = args[i];
          } else {
             throw new ArcoException("ArcoRun.unknownOption", new Object[] { args[i] } );
          }
       }
       
       if( mode == MODE_EXPORT ) {
          
          if( queryName == null ) {
             throw new ArcoException("ArcoRun.missingQueryName");
          }
          
          if( outputFormat == OUTPUT_FORMAT_HTML ) {
             if( outputFile == null  ) {
                throw new ArcoException("ArcoRun.missingOutputForHTML" );
             } else if ( !outputFile.isDirectory() ) {
                throw new ArcoException("ArcoRun.invalidOutputForHTML", new Object[] { outputFile } );
             }
          }
       }
       SGELog.fine( "parameters list is ok" );
    }
    
    /**
     * convert a string into a output format constant
     * @param str   the string
     * @return the output format constant or <code>-1</code>
     */
    public static int strToOutputFormat( String str ) {
       if( str.equals( "xml" ) ) {
          return OUTPUT_FORMAT_XML;
       } else if ( str.equals( "csv" ) ) {
          return OUTPUT_FORMAT_CSV;
       } else if ( str.equals( "pdf" ) ) {
          return OUTPUT_FORMAT_PDF;
       } else if ( str.equals( "html" ) ) {
          return OUTPUT_FORMAT_HTML;
       } else {
          return -1;
       }
    }
    
    
    /**
     * Print the usage of htis tool to standard err.
     */
    public void usage() {
       System.err.println( getVersion() );
       System.err.println( "arcorun  [-c <file>] [-d <debug level>]" );
       System.err.println( "         [-l] [-help] [-?] [-v] [-o <output file>] [-n <result name>]");
       System.err.println( "         [-f <format>] (-lb name=value)* [-lbfile <file>] [-cl <cluster name>]  <query name> )" );
       System.err.println( );
       System.err.println( "       -l                  list all available query names");
       System.err.println( "       -help | -?          print this help message");
       System.err.println( "       -v                  print version");
       System.err.println( "       -c <file>           path to set configuration file of arco" );
      System.err.println( "                           (default: " + "$SGE_ROOT/$SGE_CELL/arco/reporting/config.xml)" );
       System.err.println( "       -d <level>          debug level for arco run (FINE INFO WARNING)");
       System.err.println( "       -o <output file>    path of the file where the result of the query will be stored");
       System.err.println( "                           (default stout)");
       System.err.println( "       -f <format>         format of the output (xml|csv|pdf|html). if the output format" );
       System.err.println( "                           is html the output file must be a directory.");
       System.err.println( "                           (default xml)");
       System.err.println( "       -lb <name>=<value>  specify the a late binding parameter for the query");
       System.err.println( "       -lbfile <file>      where late binding parameters are specified");
       System.err.println( "       -n <result name>    sets the name of the result, if this option is not specified");
       System.err.println( "                           the result takes the name of the given query");
       System.err.println( "       -cl <cluster name>  name of the cluster on which the query should be executed, " );
       System.err.println( "                           if this option is not specified the default cluster is used");
       System.err.println( "       <query name>        name of the query which should be executed" );
       
       System.err.println( );
    }
 }
