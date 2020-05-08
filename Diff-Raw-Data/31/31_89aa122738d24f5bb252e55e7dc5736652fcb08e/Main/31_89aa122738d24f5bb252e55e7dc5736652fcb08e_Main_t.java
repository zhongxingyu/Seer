 package com.mbutlitsky.mk;
 
 import org.apache.commons.cli.*;
 
 import java.util.Date;
 import java.util.Locale;
 
 @SuppressWarnings("AccessStaticViaInstance")
 public class Main {
    public static final String version = "1.0";
 
     /**
      * <pre>usage: (./runmk.command | runmk.bat) [OPTIONS]
      * -d,--delta <DELTA_FACTOR>   maxDx coeff. (1.1 default)
      * -h                          show this help and exit
      * -pa,--particles <NUM>       number of particles, if set all mk_config.ini options ignored
      * -po,--polka <POLKA>         polochka parameter value (2.0 default)
      * -r,--refresh <SECONDS>      threads status refresh interval (30 sec. default)
      * -w,--workers <NUM>          number of parallel threads (default is MAX(2, CPUs/2)
      * </pre>
      */
     public static void main(String[] args) {
 
         Date start = new Date();
         System.out.println("\nMonte-Karlo game v. " + version + ", (c) Michael Butlitsky 2013\n");
 
         // apache CLI lib options parser
         parseArgs(args);
 
         System.out.println();
         System.out.println(start);// System.currentTimeMillis());
         Locale.setDefault(Locale.US); // for reading/writing '.' delimited doubles properly
 
         try {
             final EnsembleController controller = new EnsembleController();
 
             Runtime.getRuntime().addShutdownHook(new Thread() {
                 public void run() {
                     controller.stop();
                     try {
                         Thread.sleep(EnsembleController.REFRESH_DELAY);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     System.out.println("Got SHUTDOWN hook, gracefully complete.");
                 }
             });
 
 
 //            controller.saveContinueOptions();
             controller.start();
 
 
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("FATAL: something fucked up – " + e.getLocalizedMessage());
             System.exit(1);
         }
 
         Date fin = new Date();
         System.out.println(fin);
         System.out.println("\nJob took " + getHumanTimeDiff(start, fin) + ". Bye!\n");
     }
 
     private static void parseArgs(String[] args) {
         Option temp = OptionBuilder.withArgName("TEMP").hasArg().withDescription("temperature " +
                 "for polochka (1K default)").withLongOpt("temp").create("t");
 
         Option polka = OptionBuilder.withArgName("POLKA").hasArg().withDescription("polochka " +
                 "parameter value (2.0 default)").withLongOpt("polka").create("po");
 
         Option workers = OptionBuilder.withArgName("NUM").hasArg().withDescription("number of " +
                 "parallel threads (default is MAX(2, CPUs/2)").withLongOpt("workers").create("w");
 
         Option particles = OptionBuilder.withArgName("NUM").hasArg().withDescription("number of " +
                 "particles, if set all mk_config.ini options ignored").withLongOpt("particles")
                 .create("pa");
 
         Option delta = OptionBuilder.withArgName("DELTA_FACTOR").hasArg()
                 .withDescription("maxDx coeff. (1.2 default)").withLongOpt("delta").create("d");
 
         Option refresh = OptionBuilder.withArgName("SECONDS").hasArg()
                 .withDescription("threads status refresh interval (7 sec. default)")
                 .withLongOpt("refresh").create("r");
 
 
         Options options = new Options();
         options.addOption("h", false, "show this help and exit");
 //        options.addOption(temp);
         options.addOption(polka);
         options.addOption(delta);
         options.addOption(refresh);
         options.addOption(workers);
         options.addOption(particles);
 
         CommandLineParser parser = new BasicParser();
 
         try {
             // parse the command line arguments
             CommandLine line = parser.parse(options, args);
 
             if (line.hasOption("h")) {
                 // automatically generate the help statement
                 printHelpAndExit(options);
             }
 
             if (line.hasOption("pa")) {
                 Ensemble.DEFAULT_NUM_PARTICLES = Integer.parseInt(line.getOptionValue("pa"));
                 System.out.println("Custom particles number = " + Ensemble.DEFAULT_NUM_PARTICLES);
             }
 
             if (line.hasOption("po")) {
                 EnsemblePolochka.EPSILON = Double.parseDouble(line.getOptionValue("polka"));
             }
             System.out.println("Polochka (electron-ion) = - " + EnsemblePolochka.EPSILON);
 
 //            if (line.hasOption("t")) {
 //                EnsemblePolochka.EPSILON = Double.parseDouble(line.getOptionValue("polka"));
 //            }
 //            System.out.println("Polochka (electron-ion) = – " + EnsemblePolochka.EPSILON);
 
             if (line.hasOption("w")) {
                 EnsembleController.NUM_THREADS = Integer.parseInt(line.getOptionValue("workers"));
                 System.out.println("Custom workers count = " + EnsembleController.NUM_THREADS);
             }
 
             if (line.hasOption("d")) {
                 Ensemble.DELTA_FACTOR = Double.parseDouble(line.getOptionValue("delta"));
             }
             System.out.println("Delta factor = " + Ensemble.DELTA_FACTOR);
 
 
             if (line.hasOption("r")) {
                 EnsembleController.REFRESH_DELAY
                         = 1000 * Integer.parseInt(line.getOptionValue("refresh"));
             }
             System.out.println("Status refresh delay = " + EnsembleController.REFRESH_DELAY / 1000);
 
         } catch (Exception exp) {
             // oops, something went wrong
             System.err.println("Parsing failed, using defaults  Reason: " + exp.getMessage());
             printHelpAndExit(options);
         }
     }
 
     private static void printHelpAndExit(Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("(./runmk.command | runmk.bat) [OPTIONS]", options);
         System.exit(0);
     }
 
     private static String getHumanTimeDiff(Date oldTime, Date newTime) {
         long diffInSeconds = (newTime.getTime() - oldTime.getTime()) / 1000;
 
         long diff[] = new long[]{0, 0, 0, 0};
     /* sec */
         diff[3] = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
     /* min */
         diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
     /* hours */
         diff[1] = (diffInSeconds = (diffInSeconds / 60)); // >= 24 ? diffInSeconds % 24 : diffInSeconds;
 //    /* days */
 //        diff[0] = (diffInSeconds = (diffInSeconds / 24));
 
         return String.format(
 //                "%d day%s, %d hour%s, %d minute%s, %d second%s",
                 "%d hour%s, %d minute%s, %d second%s",
 //                diff[0],
 //                diff[0] > 1 ? "s" : "",
                 diff[1],
                 diff[1] > 1 ? "s" : "",
                 diff[2],
                 diff[2] > 1 ? "s" : "",
                 diff[3],
                 diff[3] > 1 ? "s" : "");
     }
 }
