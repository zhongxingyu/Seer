 package hu.sztaki.ilab.longneck.cli;
 
 import hu.sztaki.ilab.longneck.bootstrap.Bootstrap;
 import hu.sztaki.ilab.longneck.bootstrap.PropertyUtils;
 import hu.sztaki.ilab.longneck.util.OsType;
 import hu.sztaki.ilab.longneck.util.OsUtils;
 import hu.sztaki.ilab.longneck.util.UtilityRunner;
 import java.io.File;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.cli.*;
 import org.apache.log4j.*;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.springframework.context.ApplicationContext;
 
 /**
  * CLI runner for longneck.
  *
  * @author Molnár Péter <molnar.peter@sztaki.mta.hu>
  */
 public class CliRunner {
     /** The logger. */
     private static final Logger LOG = Logger.getLogger(CliRunner.class);
 
     public static void main(String[] args) {
         CliRunner cli = new CliRunner();
         cli.execute(args);
     }
 
     public void execute(String[] args) {
         configureLogging();
         Properties runtimeProperties = new Properties();
 
         Options options = getDefaultOptions();
 
         // Load configuration defaults from jars
         runtimeProperties.putAll(PropertyUtils.readDefaultProperties());
 
         // Config file blacklist
         Set<String> blacklist = new HashSet(
                 Arrays.asList(new String[] { "log4j.properties" }));
 
         // Read configuration files from project config/
         runtimeProperties.putAll(
                 PropertyUtils.readPropertyFiles(new File("config/"), blacklist));
 
         // Read configuration files from per-user directory
         runtimeProperties.putAll(PropertyUtils.readPropertyFiles(
                 new File(OsUtils.getHomeDirectoryPath(OsType.getCurrent())), blacklist));
 
         try {
             // Parse command line
             Parser p = new GnuParser();
             CommandLine cli = p.parse(options, args);
 
             // Check help option
             if (cli.hasOption('h')) {
                 printHelp(options);
                 System.exit(0);
             }
 
             // Copy command line parameters
             for (Option o : cli.getOptions()) {
                 if (cli.hasOption(o.getOpt())) {
                     if (o.hasArg()) {
                         // Add options defined on command line
                         if ("define".equals(o.getLongOpt())) {
                             addCommandLineProperty(runtimeProperties, o.getValue());
                         } else {
                             runtimeProperties.setProperty(o.getLongOpt(), o.getValue());
                         }
                     } else {
                         runtimeProperties.setProperty(o.getLongOpt(), "true");
                     }
                 }
             }
 
             // Create and initialize bootstrap
             Bootstrap bootstrap = new Bootstrap(runtimeProperties);
 
             if (! runtimeProperties.containsKey("executeUtility")) {
                 bootstrap.run();
             } else {
                 ApplicationContext context = bootstrap.getApplicationContext();
                 UtilityRunner ur = (UtilityRunner) context.getBean(runtimeProperties.getProperty("executeUtility"));
                 ur.run(runtimeProperties);
             }
 
             bootstrap.close();
 
         } catch (ParseException ex) {
             LOG.error("Invalid command line specified.", ex);
         } catch (RuntimeException ex) {
             LOG.error("Error during execution.", getRootCause(ex));
         }
     }
 
     public static Throwable getRootCause(Throwable e) {
         for (;;) {
             Throwable t = e.getCause();
             if (t == null) {
                 break;
             }
 
             e = t;
         }
         return e;
     }
 
     public static Options getDefaultOptions() {
         Options options = new Options();
 
         // Add main options
         options.addOption("h", "help", false, "Prints this help screen.");
         options.addOption("p", "processFile", true, "Specifes the process file URL.");
         options.addOption("t", "workerThreadsNum", true,
                 "Number of worker threads on which the process is running. Default: 1");
         options.addOption("T", "truncateBeforeWrite", false,
                 "Truncate the target datastore before processing records.");
         options.addOption("E", "errorTruncateBeforeWrite", false,
                 "Truncate the error datastore before processing records.");
         options.addOption("m", "measureTimeEnabled", false,
                 "Enables time management on threads.");
         options.addOption("X", "executeUtility", true,
                 "Execute built-in utility <name> instead of running a process.");
         options.addOption("D", "define", true,
                 "Define runtime property <name>.");
         options.addOption("l", "maxErrorEventLevel", true,
                 "The maximum level of errors written by the error writer.");
         return options;
     }
 
     public static void configureLogging() {
         // If available, load log configuration from file
         File logXml = new File("config/log4j.xml");
         if (logXml.exists() && logXml.canRead()) {
             DOMConfigurator.configure("config/log4j.xml");
             return;
         }
 
         File logConf = new File("config/log4j.properties");
         if (logConf.exists() && logConf.canRead()) {
             PropertyConfigurator.configure("config/log4j.properties");
             return;
         }
 
         // configure a basic logger by hand
         Logger rootLogger = Logger.getRootLogger();
         rootLogger.removeAllAppenders();
         rootLogger.setLevel(Level.INFO);
         Appender consoleAppender = new ConsoleAppender(
                 new PatternLayout("%-5p [%C line %L] [%t]: %m%n%throwable"));
         rootLogger.addAppender(consoleAppender);
     }
     public static void printHelp(Options options) {
         HelpFormatter hf = new HelpFormatter();
        hf.printHelp("", "\nLongneck data transformation.\n\nOPTIONS:\n",
                 options,
                 "\nmore info: http://longneck.sztaki.hu/\n\n");
     }
 
     public static Map<String, String> parseAdditionalParameters(String[] args) {
         Pattern pattern = Pattern.compile("^-D([\\w\\.]+)=(.+)$");
 
 
         Map<String, String> params = new HashMap<String, String>();
         for (String arg : args) {
             Matcher m = pattern.matcher(arg);
             if (m.matches()) {
                 params.put(m.group(1), m.group(2));
             }
         }
 
         return params;
     }
 
     public static void addCommandLineProperty(Properties runtimeProperties, String option) {
         if (option == null || "".equals(option)) {
             throw new IllegalArgumentException("Property name must not be null.");
         }
 
         if (! option.contains("=")) {
             runtimeProperties.setProperty(option, "true");
         } else {
             String[] parts = option.split("=", 2);
             if (parts[0] == null || "".equals(parts[0])) {
                 throw new IllegalArgumentException("Property name before = must not be null.");
             }
             runtimeProperties.setProperty(parts[0], parts[1]);
         }
     }
 
 }
