 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.simplerestserver;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import de.cismet.commons.simplerestserver.container.GrizzlyRESTContainer;
 
 /**
  * Starter class for the {@link WebServerMain}.
  *
  * @author   martin.scholl@cismet.de
  * @version  1.0, 20100511
  */
 public final class WebServerMain {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String CLI_SYNTAX = "java WebServerMain"; // NOI18N
 
     public static final String OPTION_SHORT_HELP = "h";   // NOI18N
     public static final String OPTION_LONG_HELP = "help"; // NOI18N
 
     public static final String OPTION_SHORT_PORT = "p";      // NOI18N
     public static final String OPTION_LONG_PORT = "port";    // NOI18N
     public static final String OPTION_DEFAULT_PORT = "9986"; // NOI18N // unassigned port, see:
                                                              // http://www.iana.org/assignments/port-numbers
 
     public static final String OPTION_SHORT_LOGFILE = "l";                     // NOI18N
     public static final String OPTION_LONG_LOGFILE = "logfile";                // NOI18N
     public static final String OPTION_DEFAULT_LOGFILE = "simpleWebServer.log"; // NOI18N
 
     public static final String OPTION_SHORT_DEBUG = "d";    // NOI18N
     public static final String OPTION_LONG_DEBUG = "debug"; // NOI18N
 
     public static final String OPTION_SHORT_CONSOLE = "c";      // NOI18N
     public static final String OPTION_LONG_CONSOLE = "console"; // NOI18N
 
     private static final Set<WebServiceContainer> CONTAINERS = new HashSet<WebServiceContainer>(2, 1);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WebServerMain object.
      */
     private WebServerMain() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Starts up the WebServer.
      *
      * @param  args  cli args
      */
     public static void main(final String[] args) {
         final Options options = createOptions();
         final CommandLineParser parser = new PosixParser();
 
         CommandLine cmd = null;
         try {
             cmd = parser.parse(options, args, true);
         } catch (final ParseException ex) {
             System.err.println(
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.main(String[]).cliParseFailed", // NOI18N
                     ex.getMessage()));
             final HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(CLI_SYNTAX, options);
             System.exit(1);
         }
 
         assert cmd != null : "CommandLine must not be null"; // NOI18N
 
         if (cmd.hasOption(OPTION_SHORT_HELP)) {
             new HelpFormatter().printHelp(CLI_SYNTAX, options);
         } else {
             try {
                 // first try to create the configuration from the commandline
                 final WebServerConfig config = createConfig(cmd);
 
                 // register hooks to assure consistent state in any situation
                 Runtime.getRuntime().addShutdownHook(new RestServerShutdownHook());
                 Thread.setDefaultUncaughtExceptionHandler(new WebServerExceptionHandler());
 
                 // configure logging
                 initLog4j(config);
 
                 // create and start containers
                 createContainers(config);
                 for (final WebServiceContainer container : CONTAINERS) {
                     container.up();
                 }
 
                 // redirect the system out and error streams
                 redirectSystemOutAndErr();
             } catch (final Exception e) {
                 System.err.println(
                     NbBundle.getMessage(
                         WebServerMain.class,
                         "WebServerMain.main(String[]).illegalOptionValue")); // NOI18N
                 e.printStackTrace(System.err);
                 new HelpFormatter().printHelp(CLI_SYNTAX, options);
                 System.exit(1);
             }
         }
     }
 
     /**
      * Creates CLI options:<br>
      * <br>
      *
      * <ul>
      *   <li>help</li>
      *   <li>port</li>
      *   <li>logfile</li>
      *   <li>debug</li>
      *   <li>console</li>
      * </ul>
      *
      * @return  initialised CLI options
      */
     private static Options createOptions() {
         final Options options = new Options();
 
         final Option help = new Option(
                 OPTION_SHORT_HELP,
                 OPTION_LONG_HELP,
                false,
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.createOptions().helpDescription")); // NOI18N
         help.setRequired(false);
 
         final Option port = new Option(
                 OPTION_SHORT_PORT,
                 OPTION_LONG_PORT,
                 true,
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.createOptions().portDescription", // NOI18N
                     OPTION_DEFAULT_PORT));
         port.setRequired(false);
 
         final Option log = new Option(
                 OPTION_SHORT_LOGFILE,
                 OPTION_LONG_LOGFILE,
                 true,
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.createOptions().logDescription", // NOI18N
                     OPTION_DEFAULT_LOGFILE));
         log.setRequired(false);
 
         final Option debug = new Option(
                 OPTION_SHORT_DEBUG,
                 OPTION_LONG_DEBUG,
                 false,
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.createOptions().debugDescription")); // NOI18N
         debug.setRequired(false);
 
         final Option console = new Option(
                 OPTION_SHORT_CONSOLE,
                 OPTION_LONG_CONSOLE,
                 false,
                 NbBundle.getMessage(
                     WebServerMain.class,
                     "WebServerMain.createOptions().consoleDescription")); // NOI18N
         console.setRequired(false);
 
         options.addOption(help);
         options.addOption(port);
         options.addOption(log);
         options.addOption(debug);
         options.addOption(console);
 
         return options;
     }
 
     /**
      * Creates a {@link WebServerConfig} from the <code>CommandLine</code>.
      *
      * @param   cmd  the <code>CommandLine</code>
      *
      * @return  the initialised <code>WebServerConfig</code>
      *
      * @throws  IllegalArgumentException  if an illegal argument was provided as an option value
      *
      * @see     WebServerConfig
      */
     private static WebServerConfig createConfig(final CommandLine cmd) throws IllegalArgumentException {
         final int port = Integer.valueOf(cmd.getOptionValue(OPTION_SHORT_PORT, OPTION_DEFAULT_PORT));
         final File logFile = new File(cmd.getOptionValue(OPTION_SHORT_LOGFILE, OPTION_DEFAULT_LOGFILE));
         final boolean debug = cmd.hasOption(OPTION_SHORT_DEBUG);
         final boolean console = cmd.hasOption(OPTION_SHORT_CONSOLE);
 
         return new WebServerConfig(port, logFile, debug, console);
     }
 
     /**
      * Initialises the log4j logging facilities.
      *
      * @param   config  the initialised <code>WebServerConfig</code>
      *
      * @throws  IllegalArgumentException  if the given config is null
      */
     private static void initLog4j(final WebServerConfig config) {
         if (config == null) {
             throw new IllegalArgumentException("config must not be null"); // NOI18N
         }
 
         final Properties properties = new Properties();
 
         final StringBuilder rootLogger = new StringBuilder();
         if (config.isDebug()) {
             rootLogger.append("DEBUG"); // NOI18N
         } else {
             rootLogger.append("INFO");  // NOI18N
         }
 
        // init socket appender
        properties.put("log4j.appender.SOCKET", "org.apache.log4j.net.SocketAppender"); // NOI18N
        properties.put("log4j.appender.SOCKET.remoteHost", "localhost");                // NOI18N
        properties.put("log4j.appender.SOCKET.port", "4445");                           // NOI18N
        properties.put("log4j.appender.SOCKET.locationInfo", "true");                   // NOI18N
        rootLogger.append(", SOCKET");                                                  // NOI18N

         // init file appender
         properties.put("log4j.appender.FILE", "org.apache.log4j.RollingFileAppender");         // NOI18N
         properties.put("log4j.appender.FILE.file", config.getLogFile().getAbsolutePath());     // NOI18N
         properties.put("log4j.appender.FILE.MaxFileSize", "10000KB");                          // NOI18N
         properties.put("log4j.appender.FILE.MaxBackupIndex", "7");                             // NOI18N
         properties.put("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");        // NOI18N
         properties.put("log4j.appender.FILE.layout.ConversionPattern", "%d %t %p %l :: %m%n"); // NOI18N
         rootLogger.append(", FILE");                                                           // NOI18N
 
         // init console appender
         if (config.isConsole()) {
             properties.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");             // NOI18N
             properties.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");        // NOI18N
             properties.put("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %t %p %l :: %m%n"); // NOI18N
             rootLogger.append(", CONSOLE");                                                           // NOI18N
         }
 
         // init root logger
         properties.put("log4j.rootLogger", rootLogger.toString()); // NOI18N
 
         PropertyConfigurator.configure(properties);
     }
 
     /**
      * Creates {@link WebServiceContainer}s using the given config.
      *
      * @param   config  the <code>WebServerConfig</code>
      *
      * @throws  WebServerException  if a container could not be created
      */
     private static void createContainers(final WebServerConfig config) throws WebServerException {
         try {
             final WebServerConfig grizzlyConfig = config.clone();
 
             // create grizzly container for rest based services
             final ServerParamProvider spp = Lookup.getDefault().lookup(ServerParamProvider.class);
             if ((spp == null) || (spp.getServerParams() == null) || spp.getServerParams().isEmpty()) {
                if (grizzlyConfig.getServerParams() == null) {
                     System.err.println("[WARN] no server parameters provided, nothing will be served"); // NOI18N
                } else if (grizzlyConfig.getServerParams().isEmpty()) {
                     System.err.println("[WARN] server parameters empty, nothing will be served");       // NOI18N
                 }
             } else {
                 System.out.println("[INFO] found ServerParamProvider, using its parameters");           // NOI18N
 
                grizzlyConfig.clearServerParams();
                grizzlyConfig.putAllServerParams(spp.getServerParams());
             }
 
             CONTAINERS.add(new GrizzlyRESTContainer(grizzlyConfig));
         } catch (final CloneNotSupportedException ex) {
             throw new WebServerException("cannot create grizzly configuration", ex); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private static void redirectSystemOutAndErr() {
         final Logger stdOut = Logger.getLogger("STDOUT"); // NOI18N
         final Logger stdErr = Logger.getLogger("STDERR"); // NOI18N
 
         System.setOut(new PrintStream(new Log4JOutputStream(stdOut)));
         System.setErr(new PrintStream(new Log4JOutputStream(stdErr)));
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Pipes a outputstream to log4j.
      *
      * @version  1.0, 20100518
      */
     private static final class Log4JOutputStream extends ByteArrayOutputStream {
 
         //~ Instance fields ----------------------------------------------------
 
         private final transient Logger log;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new Log4JPrintStream object.
          *
          * @param  log  the log4j logger to use for logging
          */
         public Log4JOutputStream(final Logger log) {
             this.log = log;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @throws  IOException  DOCUMENT ME!
          */
         @Override
         public void flush() throws IOException {
             synchronized (this) {
                 super.flush();
                 final String message = this.toString();
                 super.reset();
 
                 if (log.isDebugEnabled()
                             && !message.isEmpty()
                             && !message.equals(System.getProperty("line.separator"))) { // NOI18N
                     log.debug(message);
                 }
             }
         }
     }
 
     /**
      * Shutdown hook for the <code>WebServer</code> that tries to cleanly shutdown all running
      * {@link WebServiceContainer}s.
      *
      * @version  1.0, 20100511
      */
     private static final class RestServerShutdownHook extends Thread {
 
         //~ Static fields/initializers -----------------------------------------
 
         private static final transient Logger LOG = Logger.getLogger(RestServerShutdownHook.class);
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          */
         @Override
         public void run() {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("shutting down all containers"); // NOI18N
             }
 
             for (final WebServiceContainer wsc : CONTAINERS) {
                 try {
                     wsc.down();
                 } catch (final WebServerException ex) {
                     LOG.error("could not shutdown webservice container: " + wsc, ex); // NOI18N
                 }
             }
         }
     }
 }
