 package net.aparsons.sentinel;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import net.aparsons.sentinel.core.Constants;
 import net.aparsons.sentinel.core.Sentinel;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Launcher {
     
     private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
     
     private static Sentinel sentinel;
     
     public static Options getOptions() {
         final Options options = new Options();
         
         final Option keyOption = new Option(Constants.OPTION_SENTINEL_KEY, true, "WhiteHat Sentinel API key");
         keyOption.setRequired(false);
         
         final Option syncOption = new Option(Constants.OPTION_SENTINEL_SYNC, false, "Download latest WhiteHat Sentinel data");
         syncOption.setRequired(false);
         
         options.addOption(keyOption);
         options.addOption(syncOption);
         
         return options;
     }
     
     private static void printUsage() {
         new HelpFormatter().printHelp("java -jar Sentinel-" + Constants.VERSION + ".jar [options]", getOptions());
     }
     
     public static void main(String[] args) {
        logger.info("Starting WhiteHat Sentinel");
         
         CommandLineParser parser = new GnuParser();
         
         try {
             CommandLine cmdLine = parser.parse(getOptions(), args);
 
             sentinel = new Sentinel();
             
             if (cmdLine.hasOption(Constants.OPTION_SENTINEL_KEY)) {
                 sentinel.setKey(cmdLine.getOptionValue(Constants.OPTION_SENTINEL_KEY));
             }
             
             if (sentinel.getKey() == null) {
                 logger.warn("WhiteHat Sentinel API key is not set");
                 System.out.print("Enter your WhiteHat Sentinel API key: ");
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                 String key = null;
 
                 try {
                     key = reader.readLine();
                 } catch (IOException ioe) {
                     logger.error(ioe.getMessage(), ioe);
                     System.exit(1);
                 }
 
                 sentinel.setKey(key);
             }
 
             if (cmdLine.hasOption(Constants.OPTION_SENTINEL_SYNC)) {
                 logger.info("Starting sync task");
                 sentinel.sync();
             }
             
         } catch (NullPointerException npe) {
             logger.error(npe.getMessage(), npe);
             printUsage();
         } catch (ParseException pe) {
             logger.error(pe.getMessage(), pe);
             printUsage();
         }
     }
     
 }
