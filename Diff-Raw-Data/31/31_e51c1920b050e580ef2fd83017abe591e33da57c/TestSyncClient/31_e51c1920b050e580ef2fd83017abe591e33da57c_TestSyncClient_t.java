 package de.consistec.syncframework.client;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.SyncContext;
 import de.consistec.syncframework.common.conflict.ConflictStrategy;
 import de.consistec.syncframework.common.exception.ContextException;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.util.StringUtil;
 import de.consistec.syncframework.impl.adapter.GenericDatabaseAdapter;
 import de.consistec.syncframework.impl.adapter.PostgresDatabaseAdapter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.RollingFileAppender;
 import org.apache.log4j.SimpleLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 //import java.nio.file.Files;
 //import java.nio.file.Path;
 //import java.nio.file.Paths;
 //import java.nio.file.Files;
 //import java.nio.file.Path;
 //import java.nio.file.Paths;

 /**
  * Date: 26.09.12 17:14
  * <p/>
  * <
  * p/>
  * The {@code TestSyncClient} represents the interface for the jmeter tests to the synchronization
  * framework. Two jmeter test categories are using this {@code TestSyncClient}, the jmeter performance tests
  * and the jmeter last tests.
  * To configure the jmeter tests two configuration files are used during the synchronization:
  * <ul>
  * <li>config_posgre.properties file for jmeter last tests and</li>
  * <li>performance_config_postgre.properties file for jmeter performance tests</li>
  * </ul>
  * .
  * <p/>
  * The JMeter tests passes special arguments to configure the
  * synchronization framework. This arguments with its options are listed below:
  * <p/>
  * <ul>
  * <li>s - server properties file name</li>
  * <li>m - thread number which calls this process</li>
  * <li>o - log file name</li>
  * <li>t - number of retries to apply changes</li>
  * <li>r - number to retry the whole sync</li>
  * <p/>
  * </ul>
  * <p/>
  * Passed arguments which are also present as properties in one of the configuration properties files
  * have a higher priority which means that the properties in configuration files are overridden.
  * <p/>
  * For example:
  * <p>In the configration file are following properties configured:</p>
  * framework.server.number_of_get_changes_tries_on_transaction_error=3 and
  * framework.client.number_of_sync_tries_on_transaction_error=3
  * <p/>
  * <p>Ppassed arguments are as follows:</p>
  * -t 5 and -r 7
  * <p/>
  * then the values of the passed arguments 5 and 7 will be used during the synchronization.
  *
  * @author Marcel
  * @since 0.0.1-SNAPSHOT
  */
 public class TestSyncClient {
 
     //    private static final Logger LOGGER = Logger.getRootLogger();
     private static final Logger LOGGER = LoggerFactory.getLogger(TestSyncClient.class.getCanonicalName());
     // obtaining frameworks configuration object
     private static final Config CONF = Config.getInstance();
     private Options options = new Options();
     private String outFile;
     private boolean retrySync = false;
 
     public void start(String[] args) throws SyncException, IOException, ParseException, ClassNotFoundException,
         ContextException {
 
         createOptions();
 
         CommandLineParser parser = new GnuParser();
         try {
             CommandLine line = parser.parse(options, args, true);
             outFile = line.getOptionValue("o", null);
             prepareLogger();
 
             int clientThreadNumber = Integer.valueOf(line.getOptionValue("m")).intValue();
 
             CONF.loadFromFile(new FileInputStream(line.getOptionValue("s")));
 
             LOGGER.debug("Client db adapter: {}", CONF.getClientDatabaseAdapter());
             LOGGER.debug("Client db adapter properties {}", CONF.getClientDatabaseProperties().entrySet());
             LOGGER.debug("Server db adapter: {}", CONF.getServerDatabaseAdapter());
 
             String maxApplyChangesRepeat = line.getOptionValue("t");
             if (!StringUtil.isNullOrEmpty(maxApplyChangesRepeat)) {
                 CONF.setRetryNumberOfApplyChangesOnTransactionError(Integer.parseInt(maxApplyChangesRepeat));
                 LOGGER.info(
                     "RepeatNumberOfSyncOnTransactionError set to {}",
                     Integer.valueOf(CONF.getRetryNumberOfApplyChangesOnTransactionError()));
             }
 
             String maxSyncRepeat = line.getOptionValue("r");
             if (!StringUtil.isNullOrEmpty(maxSyncRepeat)) {
                 CONF.setSyncRetryNumber(Integer.parseInt(maxSyncRepeat));
                 LOGGER.info("SyncRetryNumber set to {}", Integer.valueOf(CONF.getSyncRetryNumber()));
             }
 
             CONF.setConflictStrategy(ConflictStrategy.SERVER_WINS);
             // initial sync
             sync(clientThreadNumber);
 
         } catch (ParseException e) {
             printHelp(e);
         }
     }
 
     private void sync(int clientThreadNumber) throws SyncException, IOException, ContextException {
 
         if (!retrySync) {
             Properties clientDbProps = CONF.getClientDatabaseProperties();
             String oldClientDbUrl = clientDbProps.getProperty(GenericDatabaseAdapter.PROPS_URL);
             if (!StringUtil.isNullOrEmpty(oldClientDbUrl)) {
 
                 clientDbProps.put(GenericDatabaseAdapter.PROPS_URL, oldClientDbUrl + "_" + clientThreadNumber);
                 CONF.setClientDatabaseProperties(clientDbProps);
 
             } else {
 
                 // if using PostgreSQL adapter
                 if (PostgresDatabaseAdapter.class.equals(CONF.getClientDatabaseAdapter())) {
                     String oldDbName = clientDbProps.getProperty(PostgresDatabaseAdapter.PROPS_DB_NAME);
                     clientDbProps.put(PostgresDatabaseAdapter.PROPS_DB_NAME, oldDbName + "_" + clientThreadNumber);
                 }
 
             }
         }
 
         if (CONF.getServerProxy() == null) {
             SyncContext.local().synchronize();
         } else {
             SyncContext.client().synchronize();
         }
     }
 
     private void createOptions() {
         options.addOption(OptionBuilder
             .withLongOpt("settings")
             .withDescription("Properties file with settings for server connection")
             .isRequired()
             .hasArg(true)
             .create("s"));
         options.addOption(OptionBuilder
             .withLongOpt("output")
             .withDescription("Write program output to file")
             .hasArg(true)
             .create("o"));
         options.addOption(OptionBuilder.withLongOpt("number of current user")
             .withDescription("gives the number of user (current jmeter-thread) who call this process")
             .isRequired(false)
             .hasArg(true)
             .create("m"));
         options.addOption(OptionBuilder.withLongOpt("number of apply changes tries")
             .withDescription("gives the repeating number of apply changes if a transaction error occurs.")
             .isRequired(false)
             .hasArg(true)
             .create("t"));
         options.addOption(OptionBuilder.withLongOpt("number of sync tries")
             .withDescription("gives the repeating number of syncs if client revision != server revision.")
             .isRequired(false)
             .hasArg(true)
             .create("r"));
 
     }
 
     private void printHelp(Exception e) {
         System.out.println(e.getMessage());
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp("java -jar TestSyncClient.jar <options>",
             "This tool immediately starts a synchronization with the given properties.", options, "");
     }
 
     /**
      * Prepares files for logger (if outFile isn't null). If outFile is null, log messages will be printed to System.out
      * To file name (before it's extension) timestamp will be appended.
      *
      * @throws IOException well, sheet happens
      */
     private void prepareLogger() throws IOException {
 
         if (outFile != null) {
 
 //            Path logFilePath = Paths.get(outFile);
             File logFilePath = new File(outFile);
 
 //            String fileName = logFilePath.getFileName().toString();
             String fileName = logFilePath.getName();
             String extension = FileUtil.getExtension(fileName);
 
             StringBuilder builder = new StringBuilder(logFilePath.getParent().toString());
             builder.append("/");
             builder.append(fileName.substring(0, fileName.length() - (extension.length() + 1)));
             builder.append("_");
             builder.append(System.currentTimeMillis());
             builder.append(".");
             builder.append(extension);
 
 //            logFilePath = Paths.get(builder.toString());
             logFilePath = new File(builder.toString());
 
             if (logFilePath.getParent() != null) {
 //                Files.createDirectories(logFilePath.getParent());
             }
 
 //            if (!Files.exists(logFilePath)) {
 //                Files.createFile(logFilePath);
 //            }
 
 //            if (!logFilePath.exists()) {
 //                logFilePath.createNewFile();
 //            }
             createFileLogger(logFilePath.toString());
 
         } else {
             createConsoleLogger();
         }
     }
 
     private void createFileLogger(String logFile) {
         try {
             SimpleLayout layout = new SimpleLayout();
             FileAppender appender = new RollingFileAppender(layout, logFile, false);
             appender.setThreshold(Level.ALL);
             BasicConfigurator.configure(appender);
         } catch (IOException e) {
             LOGGER.error("Printing ERROR Statements", e);
         }
     }
 
     private void createConsoleLogger() {
         SimpleLayout layout = new SimpleLayout();
         ConsoleAppender appender = new ConsoleAppender(layout, "System.out");
         appender.setThreshold(Level.ALL);
         BasicConfigurator.configure(appender);
     }
 }
