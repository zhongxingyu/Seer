 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tsdaggregator;
 
 import org.apache.commons.cli.*;
 import org.apache.commons.io.input.Tailer;
 import org.apache.log4j.Logger;
 import org.joda.time.Period;
 import org.joda.time.format.ISOPeriodFormat;
 import org.joda.time.format.PeriodFormatter;
 
 import java.io.*;
 import java.net.UnknownHostException;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author brandarp
  */
 public class TsdAggregator {
 
     static final Logger _Logger = Logger.getLogger(TsdAggregator.class);
     private static final String REMET_DEFAULT_URI = "http://localhost:7090/report";
     private static final String MONITORD_DEFAULT_URI = "http://monitord:8080/results";
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
 
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread thread, Throwable throwable) {
                 _Logger.error("Unhandled exception!", throwable);
             }
         });
 
 
         final Options options = new Options();
         final Option inputFileOption = OptionBuilder.withArgName("input_file").withLongOpt("file").hasArg().withDescription("file to be parsed").create("f");
         final Option serviceOption = OptionBuilder.withArgName("service").withLongOpt("service").hasArg().withDescription("service name").create("s");
         final Option hostOption = OptionBuilder.withArgName("host").withLongOpt("host").hasArg().withDescription("host the metrics were generated on").create("h");
         final Option clusterOption = OptionBuilder.withArgName("cluster").withLongOpt("cluster").hasArg().withDescription("name of the cluster the host is in").create("c");
         final Option uriOption = OptionBuilder.withArgName("uri").withLongOpt("uri").hasArg().withDescription("metrics server uri").create("u");
         final Option outputFileOption = OptionBuilder.withArgName("output_file").withLongOpt("output").hasArg().withDescription("output file").create("o");
         final Option parserOption = OptionBuilder.withArgName("parser").withLongOpt("parser").hasArg().withDescription("parser to use to parse log lines").create("p");
         final Option periodOption = OptionBuilder.withArgName("period").withLongOpt("period").hasArgs().withDescription("aggregation time period in ISO 8601 standard notation (multiple allowed)").create("d");
         final Option statisticOption = OptionBuilder.withArgName("stat").withLongOpt("statistic").hasArgs().withDescription("statistic of aggregation to record (multiple allowed)").create("t");
         final Option extensionOption = OptionBuilder.withArgName("extension").withLongOpt("extension").hasArgs().withDescription("extension of files to parse - uses a union of arguments as a regex (multiple allowed)").create("e");
         final Option tailOption = OptionBuilder.withLongOpt("tail").hasArg(false).withDescription("\"tail\" or follow the file and do not terminate").create("l");
         final Option rrdOption = OptionBuilder.withLongOpt("rrd").hasArg(false).withDescription("create or write to rrd databases").create();
         final Option remetOption = OptionBuilder.withLongOpt("remet").hasArg(false).withDescription("send data to a local remet server").create();
         final Option monitordOption = OptionBuilder.withLongOpt("monitord").hasArg(false).withDescription("send data to a monitord server").create();
         options.addOption(inputFileOption );
         options.addOption(serviceOption);
         options.addOption(hostOption);
         options.addOption(clusterOption);
         options.addOption(uriOption);
         options.addOption(outputFileOption);
         options.addOption(parserOption);
         options.addOption(periodOption);
         options.addOption(statisticOption);
         options.addOption(extensionOption);
         options.addOption(tailOption);
         options.addOption(rrdOption);
         options.addOption(remetOption);
         options.addOption(monitordOption);
         CommandLineParser parser = new PosixParser();
         CommandLine cl;
         try {
             cl = parser.parse(options, args);
         } catch (ParseException e1) {
             System.err.println("error parsing options: " + e1.getMessage());
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption(inputFileOption.getLongOpt())) {
             System.err.println("no file found, must specify file on the command line");
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption(serviceOption.getLongOpt())) {
             System.err.println("service name must be specified");
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption(uriOption.getLongOpt()) && !cl.hasOption(outputFileOption.getLongOpt()) &&
                 !cl.hasOption(remetOption.getLongOpt()) && !cl.hasOption(monitordOption.getLongOpt())) {
             System.err.println("metrics server uri or output file not specified");
             printUsage(options);
             return;
         }
 
         Class parserClass = QueryLogLineData.class;
         if (cl.hasOption(parserOption.getLongOpt())) {
             String lineParser = cl.getOptionValue(parserOption.getLongOpt());
             try {
                 parserClass = Class.forName(lineParser);
                 if (!LogLine.class.isAssignableFrom(parserClass)) {
                     _Logger.error("parser class [" + lineParser + "] does not implement required LogLine interface");
                     return;
                 }
             } catch (ClassNotFoundException ex) {
                 _Logger.error("could not find parser class [" + lineParser + "] on classpath");
                 return;
             }
         }
 
         String[] periodOptions = {"PT1M", "PT5M", "PT1H"};
         if (cl.hasOption(remetOption.getLongOpt())) {
             periodOptions = new String[] {"PT1S"};
         }
 
         if (cl.hasOption(periodOption.getLongOpt())) {
             periodOptions = cl.getOptionValues(periodOption.getLongOpt());
         }
 
 
         Pattern filter = Pattern.compile(".*");
         if (cl.hasOption(extensionOption.getLongOpt())) {
             String[] filters = cl.getOptionValues(extensionOption.getLongOpt());
             StringBuilder builder = new StringBuilder();
             int x = 0;
             for (String f : filters) {
                 if (x > 0) {
                     builder.append(" || ");
                 }
                 builder.append("(").append(f).append(")");
             }
             filter = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
         }
 
         Boolean tailFile = cl.hasOption(tailOption.getLongOpt()) || cl.hasOption(remetOption.getLongOpt());
 
         Set<Statistic> statisticsClasses = new HashSet<>();
         if (cl.hasOption(statisticOption.getLongOpt())) {
             String[] statisticsStrings = cl.getOptionValues(statisticOption.getLongOpt());
             for (String statString : statisticsStrings) {
                 try {
                     _Logger.info("Looking up statistic " + statString);
                     Class statClass = ClassLoader.getSystemClassLoader().loadClass(statString);
                     Statistic stat;
                     try {
                         stat = (Statistic)statClass.newInstance();
                         statisticsClasses.add(stat);
                     } catch (InstantiationException | IllegalAccessException ex) {
                         _Logger.error("Could not instantiate statistic [" + statString + "]", ex);
                         return;
                     }
                     if (!Statistic.class.isAssignableFrom(statClass)) {
                         _Logger.error("Statistic class [" + statString + "] does not implement required Statistic interface");
                         return;
                     }
                 } catch (ClassNotFoundException ex) {
                     _Logger.error("could not find statistic class [" + statString + "] on classpath");
                     return;
                 }
             }
         } else if (cl.hasOption(remetOption.getLongOpt())) {
             statisticsClasses.add(new NStatistic());
             statisticsClasses.add(new TP100());
             statisticsClasses.add(new TP99());
             statisticsClasses.add(new TP90());
             statisticsClasses.add(new MeanStatistic());
         } else {
             statisticsClasses.add(new TP0());
             statisticsClasses.add(new TP50());
             statisticsClasses.add(new TP100());
             statisticsClasses.add(new TP90());
             statisticsClasses.add(new TP99());
             statisticsClasses.add(new TP99p9());
             statisticsClasses.add(new MeanStatistic());
             statisticsClasses.add(new NStatistic());
         }
 
         String fileName = cl.getOptionValue(inputFileOption.getLongOpt());
         String hostName = cl.getOptionValue(hostOption.getLongOpt());
         if (!cl.hasOption(hostOption.getLongOpt())) {
             try {
                 hostName = java.net.InetAddress.getLocalHost().getHostName();
             } catch (UnknownHostException e) {
                 _Logger.error("could not lookup hostname of local machine", e);
                 System.err.println("host name not specified and could not determine hostname automatically, please specify explicitly");
                 printUsage(options);
                 return;
             }
         }
 
         String cluster = cl.getOptionValue(clusterOption.getLongOpt());
         String serviceName = cl.getOptionValue(serviceOption.getLongOpt());
         String metricsUri = "";
         String outputFile = "";
         Boolean outputRRD = false;
         if (cl.hasOption(uriOption.getLongOpt())) {
             metricsUri = cl.getOptionValue(uriOption.getLongOpt());
         } else if (cl.hasOption(remetOption.getLongOpt())) {
             metricsUri = REMET_DEFAULT_URI;
         } else if (cl.hasOption(monitordOption.getLongOpt())) {
             metricsUri = MONITORD_DEFAULT_URI;
         }
 
         if (cl.hasOption(outputFileOption.getLongOpt())) {
             outputFile = cl.getOptionValue(outputFileOption.getLongOpt());
         }
 
         if (cl.hasOption(rrdOption.getLongOpt())) {
             outputRRD = true;
         }
 
         _Logger.info("using file " + fileName);
         _Logger.info("using cluster " + cluster);
         _Logger.info("using hostname " + hostName);
         _Logger.info("using servicename " + serviceName);
         _Logger.info("using uri " + metricsUri);
         _Logger.info("using output file " + outputFile);
         _Logger.info("using filter (" + filter.pattern() + ")");
         if (outputRRD) {
             _Logger.info("outputting rrd files");
         }
 
         Set<Period> periods = new HashSet<>();
         PeriodFormatter periodParser = ISOPeriodFormat.standard();
         for (String p : periodOptions) {
             periods.add(periodParser.parsePeriod(p));
         }
 
         MultiListener listener = new MultiListener();
        if (!metricsUri.equals("") && (!options.hasOption(remetOption.getLongOpt()) && !options.hasOption(monitordOption.getLongOpt()))) {
             AggregationListener httpListener = new HttpPostListener(metricsUri);
             listener.addListener(new BufferingListener(httpListener, 50));
         }
 
        if (!metricsUri.equals("") && options.hasOption(remetOption.getLongOpt())) {
             AggregationListener httpListener = new HttpPostListener(metricsUri);
             //we don't want to buffer remet responses
             listener.addListener(httpListener);
         }
 
        if (!metricsUri.equals("") && options.hasOption(monitordOption.getLongOpt())) {
             AggregationListener monitordListener = new MonitordListener(metricsUri, cluster, hostName);
             listener.addListener(monitordListener);
         }
 
         if (!outputFile.equals("")) {
             AggregationListener fileListener = new FileListener(outputFile);
             //listener = new BufferingListener(fileListener, 500);
             listener.addListener(fileListener);
         }
 
         if (outputRRD) {
             listener.addListener(new RRDClusterListener());
         }
 
         Map<String, TSData> aggregations = new ConcurrentHashMap<>();
 
         ArrayList<String> files = new ArrayList<>();
         File file = new File(fileName);
         if (file.isFile()) {
             files.add(fileName);
         } else if (file.isDirectory()) {
             _Logger.info("File given is a directory, will recursively process");
             findFilesRecursive(file, files, filter);
         }
         for (String f : files) {
             try {
                 _Logger.info("Reading file " + f);
 
                 LineProcessor processor = new LineProcessor(parserClass, statisticsClasses, hostName, serviceName, periods, listener, aggregations);
                 if (tailFile) {
                     File fileHandle = new File(f);
                     LogTailerListener tailListener = new LogTailerListener(processor);
                     Tailer.create(fileHandle, tailListener, 500l, false);
                 }
                 else {
                     //check the first 4 bytes of the file for utf markers
                     FileInputStream fis = new FileInputStream(f);
                     byte[] header = new byte[4];
                     if (fis.read(header) < 4) {
                         //If there are less than 4 bytes, we should move on
                         continue;
                     }
                     String encoding = "UTF-8";
                     if (header[0] == -1 && header[1] == -2) {
                         _Logger.info("Detected UTF-16 encoding");
                         encoding = "UTF-16";
                     }
 
                     InputStreamReader fileReader = new InputStreamReader(new FileInputStream(f), Charset.forName(encoding));
                     BufferedReader reader = new BufferedReader(fileReader);
                     String line;
                     while ((line = reader.readLine()) != null) {
                         if (processor.invoke(line))
                             return;
                     }
                 }
 
                 //close all aggregations
                 for (Map.Entry<String, TSData> entry : aggregations.entrySet()) {
                     entry.getValue().close();
                 }
             } catch (FileNotFoundException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
         long rotationCheck = 30000;
         long rotateOn = 60000;
        if (options.hasOption(remetOption.getLongOpt())) {
             rotationCheck = 500;
             rotateOn = 1000;
         }
 
 
         if (tailFile) {
             while (true) {
                 try {
                     Thread.sleep(rotationCheck);
                     //_Logger.info("Checking rotations on " + aggregations.size() + " TSData objects");
                     for (Map.Entry<String, TSData> entry : aggregations.entrySet()) {
                         //_Logger.info("Check rotate on " + entry.getKey());
                         entry.getValue().checkRotate(rotateOn);
                     }
                 } catch (InterruptedException e) {
                     Thread.interrupted();
                     _Logger.error("Interrupted!", e);
                 }
             }
         }
         listener.close();
     }
 
     private static void findFilesRecursive(File dir, ArrayList<String> files, Pattern filter) {
         String[] list = dir.list();
         Arrays.sort(list);
         for (String f : list) {
             File entry = new File(dir, f);
             if (entry.isFile()) {
                 Matcher m = filter.matcher(entry.getPath());
                 if (m.find()) {
                     files.add(entry.getAbsolutePath());
                 }
             } else if (entry.isDirectory()) {
                 findFilesRecursive(entry, files, filter);
             }
         }
     }
 
     public static void printUsage(Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("tsdaggregator", options, true);
     }
 }
