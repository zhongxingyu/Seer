 package dk.statsbiblioteket.digitv.youseeingester;
 
 import dk.statsbiblioteket.digitv.youseeingester.model.RecordedFile;
 import dk.statsbiblioteket.digitv.youseeingester.model.persistence.RecordedFileDAO;
 import dk.statsbiblioteket.digitv.youseeingester.model.persistence.YouseeDigitvIngesterHibernateUtil;
 import dk.statsbiblioteket.mediaplatform.ingest.model.persistence.HibernateUtilIF;
 import org.apache.commons.cli.*;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 
 import java.io.IOException;
 
 /**
  * @author jrg
  */
 public class YouseeDigitvIngester {
     private static Options options;
 
     private static final Option FILENAME_OPTION = new Option("filename", true,
             "The sb filename of the media file");
     private static final Option STARTTIME_OPTION = new Option("starttime", true,
             "The recording start time of the file");
     private static final Option STOPTIME_OPTION = new Option("stoptime", true,
             "The recording stop time of the file");
     private static final Option CHANNELID_OPTION = new Option("channelid", true,
             "The recording channel ID of the file");
     private static final Option CONFIG_OPTION = new Option("config", true,
             "The config file for the YouSee DigiTV Ingester");
 
     static {
         options = new Options();
         options.addOption(FILENAME_OPTION);
         options.addOption(STARTTIME_OPTION);
         options.addOption(STOPTIME_OPTION);
         options.addOption(CHANNELID_OPTION);
         options.addOption(CONFIG_OPTION);
         for (Object option : options.getOptions()) {
             if (option instanceof Option) {
                 Option option1 = (Option) option;
                 option1.setRequired(true);
             }
         }
 
 
     }
 
     public static void printUsage() {
         final HelpFormatter usageFormatter = new HelpFormatter();
         usageFormatter.printHelp("youseeDigitvIngester", options, true);
     }
 
     /**
      * Main method for ingesting a single file into the digitv system. Parameters (all compulsory) are
      * -filename  the name of the file
      * -starttime the starttime in yyyyMMddHHmmss format
      * -stoptime  the stoptime in yyyyMMddHHmmss format
      * -channelid the name of the channel
      * -config the path to the configuration file (absolute or relativ to current working directory)
      *
      * The configuration file contains the parameters
      * hibernate.config.file.path the path to the hibernate configuration file
      * log4j.config.file.path the path to the log4j configuration file
      *
      * @param args
      */
     public static void main(String[] args) {
         CommandLineParser parser = new PosixParser();
         CommandLine cmd;
         IngestContext context = new IngestContext();  // To hold input params
 
         try {
             cmd = parser.parse(options, args);
         } catch (org.apache.commons.cli.ParseException e) {
             parseError(e.toString());
             return;
         }
 
 
          // Get properties
         String config = cmd.getOptionValue(CONFIG_OPTION.getOpt());
         if (config == null){
             parseError(CONFIG_OPTION.toString());
             return;
         }
         context.setConfig(config);
 
         String filenameAndPath = context.getConfig();
         Properties properties = null;
         try {
             properties = getPropertiesFromPropertyFile(filenameAndPath);
         } catch (Exception e) {
             System.err.println("Could not load config file from path: "
                     + filenameAndPath);
             exit(13);
         }
 
         String pathToLog4jConfig = null;
         if (properties == null) {
             System.err.println("Could not load config file from path: "
                     + filenameAndPath);
             exit(13);
         } else {
             try {
                 pathToLog4jConfig
                         = properties.getProperty("log4j.config.file.path");
             } catch (Exception e) {
                 System.err.println("Missing path to log4 config in config file");
                 exit(13);
             }
         }
 
         File log4jFile = new File(pathToLog4jConfig);
         if (!log4jFile.exists()) {
             System.err.println("Could not load log4j config from " + log4jFile.getAbsolutePath());
             exit(13);                                                                                                                                         System.err.println("Loading log4j config from " + pathToLog4jConfig + " corresponding to " + log4jFile.getAbsolutePath());
 
         }
         DOMConfigurator.configure(pathToLog4jConfig);
         //From here on can assume there is a log4j configuration
         Logger log = Logger.getLogger(YouseeDigitvIngester.class);
         log.info("Loaded log4j configuration from " + log4jFile.getAbsolutePath());
 
         String pathToHibernateConfigFile = "";
         if (properties == null) {
             System.err.println("Could not load config file from path: "
                     + filenameAndPath);
             exit(13);
         } else {
             try {
                 pathToHibernateConfigFile
                         = properties.getProperty("hibernate.config.file.path");
             } catch (Exception e) {
                 log.error("Missing hibernate configuration in config file");
                 System.err.println("Missing data in config file");
                 exit(13);
             }
         }
         log.info("Loaded hibernate configuration from " + pathToHibernateConfigFile);
         // Get each parameter
         String filename = cmd.getOptionValue(FILENAME_OPTION.getOpt());
         if (filename == null){
             parseError(FILENAME_OPTION.toString());
             return;
         }
         context.setFilename(filename);
         log.info("Started ingest of " + filename);
 
         String starttime = cmd.getOptionValue(STARTTIME_OPTION.getOpt());
         if (starttime == null){
             parseError(STARTTIME_OPTION.toString());
             return;
         }
         context.setStarttime(starttime);
         log.info("Start time: " + starttime);
 
         String stoptime = cmd.getOptionValue(STOPTIME_OPTION.getOpt());
         if (stoptime == null){
             parseError(STOPTIME_OPTION.toString());
             return;
         }
         context.setStoptime(stoptime);
         log.info("Stop time: " + stoptime);
 
         String channelid = cmd.getOptionValue(CHANNELID_OPTION.getOpt());
         if (channelid == null){
             parseError(CHANNELID_OPTION.toString());
             return;
         }
         context.setChannelid(channelid);
         log.info("Channel id: " + channelid);
 
 
 
 
         // Do the actual ingesting
         String output = insertDataIntoDigitvDatabase(context,
                 pathToHibernateConfigFile);
 
         System.out.println(output);
         //exit(0);
     }
 
     private static String insertDataIntoDigitvDatabase(IngestContext context,
                                                        String pathToHibernateConfigFile) {
         Logger log = Logger.getLogger(YouseeDigitvIngester.class);
 
         String output;
 
         File cfgFile = new File(pathToHibernateConfigFile);
         HibernateUtilIF util = YouseeDigitvIngesterHibernateUtil.initialiseFactory(cfgFile);
 
         RecordedFileDAO recordedFileDAO = new RecordedFileDAO(util);
 
         String filename = "";
         Date start_date = null;
         Date stop_date = null;
         String channel_id = "";
         try {
            DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
             filename = context.getFilename();
             start_date = df.parse(context.getStarttime());
             stop_date = df.parse(context.getStoptime());
             channel_id = context.getChannelid();
         } catch (java.text.ParseException e) {
             log.error("Could not parse date", e);
             System.err.println("Could not parse date");
             e.printStackTrace(System.err);
             exit(13);
         }
 
         RecordedFile recordedFile = new RecordedFile(filename, start_date,
                 stop_date, channel_id);
         Long returnedId = recordedFileDAO.create(recordedFile);
         log.info("Ingested " + filename + " to id " + returnedId);
         output = "{"
                 + "   \"id\" : \"" + returnedId + "\""
                 + "}";
         return output;
     }
 
     private static Properties getPropertiesFromPropertyFile(
             String filenameAndPath) throws IOException {
         Properties properties = new Properties();
         properties.load(new FileInputStream(filenameAndPath));
         return properties;
     }
 
     private static void parseError(String message){
         System.err.println("Error parsing arguments");
         System.err.println(message);
         printUsage();
         exit(1);
     }
 
     private static void exit(int code){
         System.exit(code);
     }
 
 }
