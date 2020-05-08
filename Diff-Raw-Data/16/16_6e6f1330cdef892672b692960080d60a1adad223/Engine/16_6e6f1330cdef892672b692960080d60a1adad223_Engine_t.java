 package com.amee.base.engine;
 
 import com.amee.base.transaction.TransactionController;
 import org.apache.commons.cli.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateTimeZone;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.tanukisoftware.wrapper.WrapperListener;
 import org.tanukisoftware.wrapper.WrapperManager;
 
 import java.io.Serializable;
 import java.util.TimeZone;
 
 /**
  * The main 'Engine' class that bootstraps the application.
  * <p/>
  * See: http://wrapper.tanukisoftware.org/jdoc/org/tanukisoftware/wrapper/WrapperListener.html
  */
 public class Engine implements WrapperListener, Serializable {
 
     private final Log log = LogFactory.getLog(getClass());
 
     private ApplicationContext springContext;
     private TransactionController transactionController;
 
     // These are used to determine the PID of the instance in the init script.
     private String appName = "amee";
     private String serverName = "localhost";
     private String instanceName = "live";
 
     public Engine() {
         super();
     }
 
     public Engine(String appName, String serverName, String instanceName) {
         this();
         this.appName = appName;
         this.serverName = serverName;
         this.instanceName = instanceName;
     }
 
     public static void main(String[] args) {
         start(new Engine(), args);
     }
 
     protected static void start(WrapperListener wrapperListener, String[] args) {
         WrapperManager.start(wrapperListener, args);
     }
 
     public Integer start(String[] args) {
 
         parseOptions(args);
 
         log.debug("Starting Engine...");
 
         // Initialise Spring ApplicationContext.
         springContext = new ClassPathXmlApplicationContext(new String[]{"applicationContext*.xml"});
 
         // Initialise TransactionController (for controlling Spring).
         transactionController = (TransactionController) springContext.getBean("transactionController");
 
         // Do onStart callback wrapped in a transaction.
         boolean started;
         try {
             transactionController.begin(true);
             started = onStart();
             log.debug("...Engine started.");
         } finally {
             transactionController.end();
         }
 
         // Handle result.
         if (started) {
             return null;
         } else {
             // An arbitrary error code to indicate startup failure.
             return 1;
         }
     }
 
     protected void parseOptions(String[] args) {
 
         CommandLine line = null;
         CommandLineParser parser = new GnuParser();
         Options options = new Options();
 
         // Define appName option.
         Option appNameOpt = OptionBuilder.withArgName("appName")
                 .hasArg()
                 .withDescription("The app name")
                 .create("appName");
         appNameOpt.setRequired(true);
         options.addOption(appNameOpt);
 
         // Define serverName option.
         Option serverNameOpt = OptionBuilder.withArgName("serverName")
                 .hasArg()
                 .withDescription("The server name")
                 .create("serverName");
         serverNameOpt.setRequired(true);
         options.addOption(serverNameOpt);
 
         // Define instanceName option.
         Option instanceNameOpt = OptionBuilder.withArgName("instanceName")
                 .hasArg()
                 .withDescription("The instance name")
                 .create("instanceName");
         instanceNameOpt.setRequired(true);
         options.addOption(instanceNameOpt);
 
         // Define timeZone option.
         Option timeZoneOpt = OptionBuilder.withArgName("timeZone")
                 .hasArg()
                 .withDescription("The time zone")
                 .create("timeZone");
         timeZoneOpt.setRequired(false);
         options.addOption(timeZoneOpt);
 
         // Parse the options.
         try {
             line = parser.parse(options, args);
         } catch (ParseException exp) {
             new HelpFormatter().printHelp("java " + this.getClass().getName(), options);
             System.exit(-1);
         }
 
         // Handle appName.
         if (line.hasOption(appNameOpt.getOpt())) {
             appName = line.getOptionValue(appNameOpt.getOpt());
         }
 
         // Handle serverName.
         if (line.hasOption(serverNameOpt.getOpt())) {
             serverName = line.getOptionValue(serverNameOpt.getOpt());
         }
 
         // Handle instanceName.
         if (line.hasOption(instanceNameOpt.getOpt())) {
             instanceName = line.getOptionValue(instanceNameOpt.getOpt());
         }
 
         // Handle timeZone.
         if (line.hasOption(timeZoneOpt.getOpt())) {
             String timeZoneStr = line.getOptionValue(timeZoneOpt.getOpt());
             if (!StringUtils.isBlank(timeZoneStr)) {
                 TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
                 if (timeZone != null) {
                     TimeZone.setDefault(timeZone);
                     DateTimeZone.setDefault(DateTimeZone.forTimeZone(timeZone));
                 }
             }
         }
         log.info("parseOptions() Time Zone is: " + TimeZone.getDefault().getDisplayName() + " (" + TimeZone.getDefault().getID() + ")");
     }
 
     protected boolean onStart() {
         // Do nothing.
         return true;
     }
 
     protected boolean onShutdown() {
         // Do nothing.
         return true;
     }
 
     public int stop(int exitCode) {
         try {
             log.debug("Stopping Engine...");
             onShutdown();
             log.debug("...Engine stopped.");
         } catch (Exception e) {
            log.error("Caught Exception: " + e, e);
         }
         return exitCode;
     }
 
     public void controlEvent(int event) {
         log.debug("controlEvent() " + event);
         // Do nothing.
     }
 
     public ApplicationContext getSpringContext() {
         return springContext;
     }
 
     public TransactionController getTransactionController() {
         return transactionController;
     }
 
     public String getAppName() {
         return appName;
     }
 
     public String getServerName() {
         return serverName;
     }
 
     public String getInstanceName() {
         return instanceName;
     }
 }
