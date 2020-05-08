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
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author brandarp
  */
 public class TsdAggregator {
 
     static final Logger _Logger = Logger.getLogger(TsdAggregator.class);
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
 
         Options options = new Options();
         options.addOption("f", "file", true, "file to be parsed");
         options.addOption("s", "service", true, "service name");
         options.addOption("h", "host", true, "host the metrics were generated on");
         options.addOption("u", "uri", true, "metrics server uri");
         options.addOption("o", "output", true, "output file");
         options.addOption("p", "parser", true, "parser to use to parse log lines");
         options.addOption("d", "period", true, "aggregation time period in ISO 8601 standard notation (multiple allowed)");
         options.addOption("t", "statistic", true, "statistic of aggregation to record (multiple allowed)");
         options.addOption("e", "extension", true, "extension of files to parse - uses a union of arguments as a regex (multiple allowed)");
         options.addOption("", "tail", false, "\"tail\" or follow the file and do not terminate");
         options.addOption("", "rrd", false, "create or write to rrd databases");
        options.addOption("", "remet", false, "send data to a local remet server");
         CommandLineParser parser = new PosixParser();
         CommandLine cl;
         try {
             cl = parser.parse(options, args);
         } catch (ParseException e1) {
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption("f")) {
             System.err.println("no file found, must specify file on the command line");
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption("s")) {
             System.err.println("service name must be specified");
             printUsage(options);
             return;
         }
 
         if (!cl.hasOption("h")) {
             System.err.println("host name must be specified");
             printUsage(options);
             return;
         }
 
        if (!cl.hasOption("u") && !cl.hasOption("o") && !cl.hasOption("remet")) {
             System.err.println("metrics server uri or output file not specified");
             printUsage(options);
             return;
         }
 
         Class parserClass = QueryLogLineData.class;
         if (cl.hasOption("p")) {
             String lineParser = cl.getOptionValue("p");
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
        if (cl.hasOption("remet")) {
            periodOptions = new String[] {"PT2S"};
        }
         if (cl.hasOption("d")) {
             periodOptions = cl.getOptionValues("d");
         }
 
 
         Pattern filter = Pattern.compile(".*");
         if (cl.hasOption("e")) {
             String[] filters = cl.getOptionValues("e");
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
 
        Boolean tailFile = cl.hasOption("tail") || cl.hasOption("remet");
 
         Set<Statistic> statisticsClasses = new HashSet<Statistic>();
         if (cl.hasOption("t")) {
             String[] statisticsStrings = cl.getOptionValues("t");
             for (String statString : statisticsStrings) {
                 try {
                     _Logger.info("Looking up statistic " + statString);
                     Class statClass = ClassLoader.getSystemClassLoader().loadClass(statString);
                     Statistic stat;
                     try {
                         stat = (Statistic)statClass.newInstance();
                         statisticsClasses.add(stat);
                     } catch (InstantiationException ex) {
                         _Logger.error("Could not instantiate statistic [" + statString + "]", ex);
                         return;
                     } catch (IllegalAccessException ex) {
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
        } else if (cl.hasOption("remet")) {
            statisticsClasses.add(new NStatistic());
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
 
         String fileName = cl.getOptionValue("f");
         String hostName = cl.getOptionValue("h");
         String serviceName = cl.getOptionValue("s");
         String metricsUri = "";
         String outputFile = "";
         Boolean outputRRD = false;
         if (cl.hasOption("u")) {
             metricsUri = cl.getOptionValue("u");
        } else if (cl.hasOption("remet")) {
            metricsUri = "http://localhost:9000/report";
         }
 
         if (cl.hasOption("o")) {
             outputFile = cl.getOptionValue("o");
         }
 
         if (cl.hasOption("rrd")) {
             outputRRD = true;
         }
 
         _Logger.info("using file " + fileName);
         _Logger.info("using hostname " + hostName);
         _Logger.info("using servicename " + serviceName);
         _Logger.info("using uri " + metricsUri);
         _Logger.info("using output file " + outputFile);
         _Logger.info("using filter (" + filter.pattern() + ")");
         if (outputRRD) {
             _Logger.info("outputting rrd files");
         }
 
         Set<Period> periods = new HashSet<Period>();
         PeriodFormatter periodParser = ISOPeriodFormat.standard();
         for (String p : periodOptions) {
             periods.add(periodParser.parsePeriod(p));
         }
 
         MultiListener listener = new MultiListener();
        if (!metricsUri.equals("") && !options.hasOption("remet")) {
             AggregationListener httpListener = new HttpPostListener(metricsUri);
             listener.addListener(new BufferingListener(httpListener, 50));
         }
 
         if (!outputFile.equals("")) {
             AggregationListener fileListener = new FileListener(outputFile);
             //listener = new BufferingListener(fileListener, 500);
             listener.addListener(fileListener);
         }
 
         if (outputRRD) {
             listener.addListener(new RRDClusterListener());
         }
 
         HashMap<String, TSData> aggregations = new HashMap<String, TSData>();
 
         ArrayList<String> files = new ArrayList<String>();
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
                     Tailer t = Tailer.create(fileHandle, tailListener, 1000l, false);
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
         if (tailFile) {
             while (true) {
                 try {
                     Thread.sleep(30000l);
                     _Logger.info("Checking rotations on " + aggregations.size() + " TSData objects");
                     for (Map.Entry<String, TSData> entry : aggregations.entrySet()) {
                         _Logger.info("Check rotate on " + entry.getKey());
                         entry.getValue().checkRotate();
                     }
                 } catch (InterruptedException e) {
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
                 } else {
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
