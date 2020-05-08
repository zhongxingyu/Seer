 package net.thumbtack.research.nosql;
 
 import net.thumbtack.research.nosql.clients.Database;
 import net.thumbtack.research.nosql.clients.DatabasePool;
 import net.thumbtack.research.nosql.scenarios.Scenario;
 import net.thumbtack.research.nosql.scenarios.ScenarioPool;
 import org.apache.commons.cli.*;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: vkornev
  * Date: 12.08.13
  * Time: 18:52
  *
  * Main class. This is main runnable class.
  * Command line arguments
  *  c config      -   config file name
  *  d database    -   database name. Supported databases: cassandra.
  */
 public class Researcher {
     private static final Logger log = LoggerFactory.getLogger(Researcher.class);
 
     private static final String CLI_CONFIG = "config";
     private static final String CLI_DATABASE = "database";
     private static final String CLI_SCENARIO = "scenario";
     private static final String CLI_THREADS = "threads";
     private static final String CLI_WRITES = "writes";
     private static final String CLI_HELP = "help";
 
     public static void main(String[] args) throws ParseException {
 
         Options options = getOptions();
         CommandLine commandLine = new GnuParser().parse(options, args);
 
         if (!isCommandLineValid(commandLine, options)) return;
 
         int threadsCount = Integer.valueOf(commandLine.getOptionValue(CLI_THREADS));
         long writesCount = Long.valueOf(commandLine.getOptionValue(CLI_WRITES));
 
         Configurator config = new Configurator(commandLine.getOptionValue(CLI_CONFIG));
         ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threadsCount, threadsCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
         List<Long> successfulWrites = new ArrayList<>();
         List<Long> failedWrites = new ArrayList<>();
 
         List<Database> dbs = new ArrayList<Database>(threadsCount);
         Database db;
 
         for (int i=0; i < threadsCount; i++) {
             try {
                 db = DatabasePool.get(commandLine.getOptionValue(CLI_DATABASE));
                 db.init(config);
                 dbs.add(db);
             } catch (Exception e) {
                 e.printStackTrace();
                 log.error(e.getMessage());
                 throw new RuntimeException(e);
             }
         }
 
         log.info("Start scenarios");
 
         for (Database initDB : dbs) {
             try {
                 Scenario sc = ScenarioPool.get(commandLine.getOptionValue(CLI_SCENARIO));
                 sc.init(initDB, writesCount / threadsCount, successfulWrites, failedWrites);
                 threadPool.submit(sc);
             } catch (Exception e) {
                 e.printStackTrace();
                 log.error(e.getMessage());
                 throw new RuntimeException(e);
             }
         }
 
 
         while (threadPool.getActiveCount() > 0) {}
 
         threadPool.shutdown();
 
         long successful = 0;
         for (Long s: successfulWrites) {
             successful += s;
         }
         long failed = 0;
         for (Long f: failedWrites) {
             failed += f;
         }
 
         log.warn("Total writes: " + (successful + failed));
         log.warn("Failed writes: " + failed);
     }
 
     private static Options getOptions() {
         return  new Options()
                 .addOption(CLI_CONFIG.substring(0, 1), CLI_CONFIG, true, "Config file name")
                 .addOption(CLI_DATABASE.substring(0, 1), CLI_DATABASE, true, "Database name. Supported databases: " + Arrays.toString(DatabasePool.DATABASES))
                 .addOption(CLI_SCENARIO.substring(0, 1), CLI_SCENARIO, true, "Scenario name. Supported scenarios: " + Arrays.toString(ScenarioPool.SCENARIOS))
                 .addOption(CLI_THREADS.substring(0, 1), CLI_THREADS, true, "Clients threads count")
                .addOption(CLI_WRITES.substring(0, 1), CLI_WRITES, true, "Clients threads count")
                 .addOption(CLI_HELP.substring(0, 1), CLI_HELP, false, "Show this is help");
     }
 
     private static boolean isCommandLineValid(CommandLine commandLine, Options options) {
         if (commandLine.hasOption(CLI_HELP)
                 || !commandLine.hasOption(CLI_CONFIG)
                 || !commandLine.hasOption(CLI_DATABASE)
                 || !commandLine.hasOption(CLI_SCENARIO)
                 || !commandLine.hasOption(CLI_THREADS)
                 || !commandLine.hasOption(CLI_WRITES)) {
             new HelpFormatter().printHelp("nosql-research -c <config file name> -d <database> -s <scenario name> " +
                     "-t <threads count> -w <writes count> [-h]", options);
             return false;
         }
         return true;
     }
 }
