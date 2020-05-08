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
 import tsdaggregator.publishing.*;
 import tsdaggregator.statistics.*;
 
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
 
    private static final Logger _Logger = Logger.getLogger(TsdAggregator.class);
     private static final String REMET_DEFAULT_URI = "http://localhost:7090/report";
     private static final String MONITORD_DEFAULT_URI = "http://monitord:8080/results";
     private static final Map<String, Class<? extends Statistic>> STATISTIC_MAP;
     static {
         STATISTIC_MAP = new HashMap<>();
         STATISTIC_MAP.put("n", NStatistic.class);
         STATISTIC_MAP.put("mean", MeanStatistic.class);
         STATISTIC_MAP.put("sum", SumStatistic.class);
         STATISTIC_MAP.put("p0", TP0.class);
         STATISTIC_MAP.put("min", TP0.class);
         STATISTIC_MAP.put("p100", TP100.class);
         STATISTIC_MAP.put("max", TP100.class);
         STATISTIC_MAP.put("p50", TP50.class);
         STATISTIC_MAP.put("median", TP50.class);
         STATISTIC_MAP.put("p90", TP90.class);
         STATISTIC_MAP.put("p99", TP99.class);
         STATISTIC_MAP.put("p99.9", TP99p9.class);
         STATISTIC_MAP.put("p999", TP99p9.class);
         STATISTIC_MAP.put("first", FirstStatistic.class);
         STATISTIC_MAP.put("last", LastStatistic.class);
     }
 
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
         final Option counterStatisticOption = OptionBuilder.withArgName("stat").withLongOpt("counterstat").hasArgs().withDescription("statistics of aggregation to record for counters (multiple allowed)").create("cs");
         final Option timerStatisticOption = OptionBuilder.withArgName("stat").withLongOpt("timerstat").hasArgs().withDescription("statistics of aggregation to record for timers (multiple allowed)").create("ts");
 		final Option gaugeStatisticOption = OptionBuilder.withArgName("stat").withLongOpt("gaugestat").hasArgs().withDescription("statistics of aggregation to record for gauge (multiple allowed)").create("gs");
         final Option extensionOption = OptionBuilder.withArgName("extension").withLongOpt("extension").hasArgs().withDescription("extension of files to parse - uses a union of arguments as a regex (multiple allowed)").create("e");
         final Option tailOption = OptionBuilder.withLongOpt("tail").hasArg(false).withDescription("\"tail\" or follow the file and do not terminate").create("l");
         final Option rrdOption = OptionBuilder.withLongOpt("rrd").hasArg(false).withDescription("create or write to rrd databases").create();
         final Option remetOption = OptionBuilder.withLongOpt("remet").hasOptionalArg().withArgName("uri").withDescription("send data to a local remet server").create();
         final Option monitordOption = OptionBuilder.withLongOpt("monitord").hasOptionalArg().withArgName("uri").withDescription("send data to a monitord server").create();
         options.addOption(inputFileOption );
         options.addOption(serviceOption);
         options.addOption(hostOption);
         options.addOption(clusterOption);
         options.addOption(uriOption);
         options.addOption(outputFileOption);
         options.addOption(parserOption);
         options.addOption(periodOption);
         options.addOption(timerStatisticOption);
         options.addOption(counterStatisticOption);
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
 
         List<String> periodOptions = new ArrayList<>();
         periodOptions.add("PT5M");
         if (cl.hasOption(remetOption.getLongOpt())) {
             periodOptions.add("PT1S");
         }
 
         if (cl.hasOption(periodOption.getLongOpt())) {
             periodOptions = Arrays.asList(cl.getOptionValues(periodOption.getLongOpt()));
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
 
         Set<Statistic> timerStatsClasses = new HashSet<>();
         Set<Statistic> counterStatsClasses = new HashSet<>();
 		Set<Statistic> gaugeStatsClasses = new HashSet<>();
         if (cl.hasOption(counterStatisticOption.getLongOpt())) {
             String[] statisticsStrings = cl.getOptionValues(counterStatisticOption.getLongOpt());
             buildStats(counterStatsClasses, statisticsStrings);
         } else {
             counterStatsClasses.add(new MeanStatistic());
             counterStatsClasses.add(new SumStatistic());
             counterStatsClasses.add(new NStatistic());
         }
 
         if (cl.hasOption(timerStatisticOption.getLongOpt())) {
             String[] statisticsStrings = cl.getOptionValues(timerStatisticOption.getLongOpt());
             buildStats(timerStatsClasses, statisticsStrings);
         } else {
             timerStatsClasses.add(new TP50());
             timerStatsClasses.add(new TP99());
             timerStatsClasses.add(new MeanStatistic());
             timerStatsClasses.add(new NStatistic());
         }
 
 		if (cl.hasOption(gaugeStatisticOption.getLongOpt())) {
 			String[] statisticsStrings = cl.getOptionValues(gaugeStatisticOption.getLongOpt());
 			buildStats(gaugeStatsClasses, statisticsStrings);
 		} else {
 			timerStatsClasses.add(new TP0());
 			timerStatsClasses.add(new TP100());
 			timerStatsClasses.add(new MeanStatistic());
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
         String remetUri = "";
         String monitordUri = "";
         String outputFile = "";
         Boolean outputRRD = false;
         Boolean outputRemet = false;
         Boolean outputMonitord = false;
         if (cl.hasOption(uriOption.getLongOpt())) {
             metricsUri = cl.getOptionValue(uriOption.getLongOpt());
         }
 
         if (cl.hasOption(remetOption.getLongOpt())) {
             outputRemet = true;
             remetUri = cl.getOptionValue(remetOption.getLongOpt());
             if (remetUri == null) {
                 remetUri = REMET_DEFAULT_URI;
             }
 
         }
 
         if (cl.hasOption(monitordOption.getLongOpt())) {
             outputMonitord = true;
             monitordUri = cl.getOptionValue(monitordOption.getLongOpt());
             if (monitordUri == null) {
                 monitordUri = MONITORD_DEFAULT_URI;
             }
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
         _Logger.info("using remetURI uri " + remetUri);
         _Logger.info("using monitord uri " + monitordUri);
         _Logger.info("using output file " + outputFile);
         _Logger.info("using filter (" + filter.pattern() + ")");
         _Logger.info("using counter stats " + counterStatsClasses.toString());
         _Logger.info("using timer stats " + timerStatsClasses.toString());
 		_Logger.info("using gauge stats " + gaugeStatsClasses.toString());
         if (outputRRD) {
             _Logger.info("outputting rrd files");
         }
 
         Set<Period> periods = new HashSet<>();
         PeriodFormatter periodParser = ISOPeriodFormat.standard();
         for (String p : periodOptions) {
             periods.add(periodParser.parsePeriod(p));
         }
 
         MultiPublisher listener = new MultiPublisher();
         if (!metricsUri.equals("")) {
             _Logger.info("Adding buffered HTTP POST listener");
             AggregationPublisher httpListener = new HttpPostPublisher(metricsUri);
             listener.addListener(new BufferingPublisher(httpListener, 50));
         }
 
         if (outputRemet) {
             _Logger.info("Adding remet listener");
             AggregationPublisher httpListener = new HttpPostPublisher(remetUri);
             //we don't want to buffer remet responses
             listener.addListener(httpListener);
         }
 
         if (outputMonitord) {
             _Logger.info("Adding monitord listener");
             AggregationPublisher monitordListener = new MonitordPublisher(monitordUri, cluster, hostName);
             listener.addListener(monitordListener);
         }
 
         if (!outputFile.equals("")) {
             _Logger.info("Adding file listener");
             AggregationPublisher fileListener = new FilePublisher(outputFile);
             listener.addListener(fileListener);
         }
 
         if (outputRRD) {
             _Logger.info("Adding RRD listener");
             listener.addListener(new RRDClusterPublisher());
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
 
                 LineProcessor processor = new LineProcessor(parserClass, timerStatsClasses, counterStatsClasses, gaugeStatsClasses, hostName, serviceName, periods, listener, aggregations);
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
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         long rotationCheck = 30000;
         long rotateOn = 60000;
         if (outputRemet) {
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
 
     private static void buildStats(Set<Statistic> statsClasses, String[] statisticsStrings) {
         for (String statString : statisticsStrings) {
             try {
                 _Logger.info("Looking up statistic " + statString);
                 Class statClass;
                 if (STATISTIC_MAP.containsKey(statString)) {
                     statClass = STATISTIC_MAP.get(statString);
                 } else {
                     statClass = ClassLoader.getSystemClassLoader().loadClass(statString);
                 }
                 Statistic stat;
                 try {
                     stat = (Statistic)statClass.newInstance();
                     statsClasses.add(stat);
                 } catch (InstantiationException | IllegalAccessException ex) {
                     final String error = "Could not instantiate statistic [" + statString + "]";
                     _Logger.error(error, ex);
                     throw new IllegalArgumentException(error, ex);
                 }
                 if (!Statistic.class.isAssignableFrom(statClass)) {
                     final String error = "Statistic class [" + statString + "] does not implement required Statistic interface";
                     _Logger.error(error);
                     throw new IllegalArgumentException(error);
                 }
             } catch (ClassNotFoundException ex) {
                 final String error = "could not find statistic class [" + statString + "] on classpath";
                 _Logger.error(error);
                 throw new IllegalArgumentException(error, ex);
             }
         }
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
 
    private static void printUsage(Options options) {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp("tsdaggregator", options, true);
     }
 }
