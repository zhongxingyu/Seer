 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.arpnetworking.tsdaggregator;
 
 import com.arpnetworking.tsdaggregator.aggserver.AggregationServer;
 import com.arpnetworking.tsdaggregator.publishing.*;
 import com.arpnetworking.tsdaggregator.statistics.Statistic;
 import com.google.common.base.Charsets;
 import org.apache.commons.io.input.Tailer;
 import org.apache.log4j.Logger;
 import org.joda.time.Period;
 import org.vertx.java.core.AsyncResult;
 import org.vertx.java.core.AsyncResultHandler;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.platform.PlatformLocator;
 import org.vertx.java.platform.PlatformManager;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 /**
  * @author barp
  */
 public class TsdAggregator {
 
     private static final Logger _Logger = Logger.getLogger(TsdAggregator.class);
 
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
 
         @Nonnull CommandLineParser parser = new CommandLineParser(new DefaultHostResolver());
         Configuration commandLineConfig;
         try {
             commandLineConfig = parser.parse(args);
         } catch (ConfigException e) {
             System.err.println("error parsing options: " + e.getMessage());
             parser.printUsage(System.err);
             return;
         }
 
         @Nonnull List<Configuration> configurations = new ArrayList<>();
         configurations.add(commandLineConfig);
 
         @Nonnull List<String> configFiles = commandLineConfig.getConfigFiles();
         @Nonnull ConfigFileParser configFileParser = new ConfigFileParser(new DefaultHostResolver());
         for (String configFile : configFiles) {
             try {
                 @Nonnull Configuration fileConfig = configFileParser.parse(configFile);
                 configurations.add(fileConfig);
             } catch (ConfigException e) {
                 _Logger.warn("Could not parse config file " + configFile, e);
             }
         }
 
         for (@Nonnull final Configuration config : configurations) {
             @Nonnull Thread runnerThread = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     startConfiguration(config);
                 }
             });
 
             runnerThread.setDaemon(false);
             runnerThread.start();
         }
     }
 
     public static void startConfiguration(@Nonnull final Configuration config) {
         if (!config.isValid()) {
             return;
         }
         Class<? extends LogParser> parserClass = config.getParserClass();
         LogParser logParser;
         try {
             logParser = parserClass.newInstance();
         } catch (@Nonnull InstantiationException | IllegalAccessException e) {
             _Logger.error("Could not instantiate parser class", e);
             return;
         }
 
         Set<Period> periods = config.getPeriods();
         Set<Statistic> counterStatsClasses = config.getCounterStatistics();
         Set<Statistic> timerStatsClasses = config.getTimerStatistics();
         Set<Statistic> gaugeStatsClasses = config.getGaugeStatistics();
 
         Pattern filter = config.getFilterPattern();
 
         Boolean tailFile = config.shouldTailFiles();
 
         @Nonnull List<String> fileNames = config.getFiles();
         String hostName = config.getHostName();
 
         String cluster = config.getClusterName();
         String serviceName = config.getServiceName();
         String metricsUri = config.getMetricsUri();
         String monitordUri = config.getMonitordAddress();
         String outputFile = config.getOutputFile();
         Boolean outputRRD = config.shouldUseRRD();
         String remetUri = config.getRemetAddress();
 
         _Logger.info("using files ");
         for (String file : fileNames) {
             _Logger.info("    " + file);
         }
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
         _Logger.info("using periods " + periods.toString());
         if (outputRRD) {
             _Logger.info("outputting rrd files");
         }
 
         //The agg server needs to deploy itself and the redis module
         PlatformManager platformManager = PlatformLocator.factory.createPlatformManager();
         if (config.shouldStartClusterAggServer()) {
             if (!startAggServer(config, platformManager)) {
                 return;
             }
         }
 
         @Nonnull AggregationPublisher publisher = getPublisher(config, platformManager);
 
         @Nonnull LineProcessor processor =
                 new LineProcessor(logParser, timerStatsClasses, counterStatsClasses, gaugeStatsClasses, hostName,
                         serviceName, periods, publisher);
 
         @Nonnull ArrayList<String> files = getFileList(filter, fileNames);
         for (String f : files) {
             try {
                 _Logger.info("Reading file " + f);
                 if (tailFile) {
                     @Nonnull File fileHandle = new File(f);
                     @Nonnull LogTailerListener tailListener = new LogTailerListener(processor);
                     Tailer.create(fileHandle, tailListener, 500L, false);
                 } else {
                     //check the first 4 bytes of the file for utf markers
                     @Nullable FileInputStream fis = null;
                     @Nullable BufferedReader reader = null;
                     try {
                         fis = new FileInputStream(f);
                         @Nonnull byte[] header = new byte[4];
                         if (fis.read(header) < 4) {
                             //If there are less than 4 bytes, we should move on
                             continue;
                         }
                         Charset encoding = Charsets.UTF_8;
                         if (header[0] == -1 && header[1] == -2) {
                             _Logger.info("Detected UTF-16 encoding");
                             encoding = Charsets.UTF_16;
                         }
 
                         @Nonnull InputStreamReader fileReader = new InputStreamReader(new FileInputStream(f), encoding);
                         reader = new BufferedReader(fileReader);
                         String line;
                         while ((line = reader.readLine()) != null) {
                             processor.invoke(line);
                         }
                     } finally {
                         if (fis != null) {
                             try {
                                 fis.close();
                             } catch (Exception ignored) {
                             }
                         }
 
                         if (reader != null) {
                             try {
                                 reader.close();
                             } catch (Exception ignored) {
                             }
                         }
                     }
                 }
 
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         processor.closeAggregations();
 
         if (tailFile || config.shouldStartClusterAggServer()) {
             while (true) {
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e) {
                     break;
                 }
             }
         }
 
         publisher.close();
     }
 
     private static boolean startAggServer(@Nonnull final Configuration config,
                                           @Nonnull final PlatformManager platformManager) {
         int port = config.getClusterAggServerPort();
         @Nonnull JsonObject conf = new JsonObject();
         conf.putNumber("port", port);
         conf.putString("name", config.getHostName());
         @Nonnull JsonArray redisHosts =
                 new JsonArray(config.getRedisHosts().toArray(new String[config.getRedisHosts().size()]));
         conf.putArray("redisAddress", redisHosts);
 
         ClassLoader classLoader = TsdAggregator.class.getClassLoader();
         if (!(classLoader instanceof URLClassLoader)) {
             throw new IllegalStateException("Unable to reflect on classes to start vertx modules");
         }
         @Nonnull URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
         URL[] clURLs = urlClassLoader.getURLs();
         @Nonnull ArrayList<URL> filteredURLs = new ArrayList<>();
         for (@Nonnull URL checkUrl : clURLs) {
             String name = checkUrl.toString().toLowerCase();
             if (name.contains("vertx-core-") || name.contains("vertx-platform-")) {
                 continue;
             }
             filteredURLs.add(checkUrl);
         }
 
         platformManager.deployVerticle(AggregationServer.class.getCanonicalName(), conf,
                 filteredURLs.toArray(new URL[filteredURLs.size()]), 1, null, new AsyncResultHandler<String>() {
             @Override
             public void handle(@Nonnull final AsyncResult<String> event) {
                 if (event.succeeded()) {
                     _Logger.info("Aggregation server started, deployment id " + event.result());
 
                 } else {
                     _Logger.error("Error starting aggregation server", event.cause());
                 }
             }
         });
         return true;
     }
 
     @Nonnull
     private static ArrayList<String> getFileList(@Nonnull final Pattern filter, @Nonnull final List<String> files) {
         @Nonnull ArrayList<String> fileList = new ArrayList<>();
         for (String fileName : files) {
             @Nonnull File file = new File(fileName);
             if (file.isFile()) {
                 fileList.add(fileName);
             } else if (file.isDirectory()) {
                 _Logger.info("File given is a directory, will recursively process");
                 findFilesRecursive(file, fileList, filter);
             }
         }
         return fileList;
     }
 
     @Nonnull
     private static AggregationPublisher getPublisher(@Nonnull final Configuration config,
                                                      final PlatformManager platformManager) {
         @Nonnull MultiPublisher listener = new MultiPublisher();
 
         String hostName = config.getHostName();
         String cluster = config.getClusterName();
         String metricsUri = config.getMetricsUri();
         String monitordUri = config.getMonitordAddress();
         String outputFile = config.getOutputFile();
         boolean outputRRD = config.shouldUseRRD();
         boolean outputMonitord = config.shouldUseMonitord();
         boolean outputRemet = config.useRemet();
         String remetUri = config.getRemetAddress();
         boolean outputUpstreamAgg = config.shouldUseUpstreamAgg();
         String upstreamAggHost = config.getClusterAggHost();
         if (!metricsUri.equals("")) {
             _Logger.info("Adding buffered HTTP POST listener");
             @Nonnull AggregationPublisher httpListener = new HttpPostPublisher(metricsUri);
             listener.addListener(new BufferingPublisher(httpListener, 50));
         }
 
 
         if (outputRemet) {
             _Logger.info("Adding remet listener");
             @Nonnull AggregationPublisher httpListener = new HttpPostPublisher(remetUri);
             //we don't want to buffer remet responses
             listener.addListener(httpListener);
         }
 
         if (outputMonitord) {
             _Logger.info("Adding monitord listener");
             @Nonnull AggregationPublisher monitordListener = new MonitordPublisher(monitordUri, cluster, hostName);
             listener.addListener(monitordListener);
         }
 
         if (!outputFile.equals("")) {
             _Logger.info("Adding file listener");
             @Nonnull AggregationPublisher fileListener = new FilePublisher(outputFile);
             listener.addListener(fileListener);
         }
 
         if (outputRRD) {
             _Logger.info("Adding RRD listener");
             listener.addListener(new RRDClusterPublisher());
         }
 
         if (outputUpstreamAgg) {
             _Logger.info("Adding upstream aggregation listener");
             listener.addListener(new AggServerPublisher(upstreamAggHost, hostName, cluster));
         }
         return listener;
     }
 
     private static void findFilesRecursive(@Nonnull File dir, @Nonnull ArrayList<String> files,
                                            @Nonnull Pattern filter) {
         String[] list = dir.list();
         Arrays.sort(list);
         for (String f : list) {
             @Nonnull File entry = new File(dir, f);
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
 }
