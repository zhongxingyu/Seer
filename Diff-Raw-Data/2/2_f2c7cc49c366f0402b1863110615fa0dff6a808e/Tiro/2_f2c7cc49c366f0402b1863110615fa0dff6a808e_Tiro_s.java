 package syndeticlogic.tiro;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 import org.apache.commons.lang3.SystemUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import syndeticlogic.tiro.controller.IOControllerFactory;
 import syndeticlogic.tiro.monitor.AbstractMonitor;
import syndeticlogic.tiro.monitor.Monitor.Platform;
 import syndeticlogic.tiro.persistence.Controller;
 import syndeticlogic.tiro.persistence.ControllerMeta;
 import syndeticlogic.tiro.persistence.JdbcDao;
 import syndeticlogic.tiro.persistence.Trial;
 import syndeticlogic.tiro.persistence.TrialMeta;
 import syndeticlogic.tiro.trial.TrialRunner;
 import syndeticlogic.tiro.trial.TrialRunnerFactory;
 
 public class Tiro {
 
     public static TrialRunner createSequentialScanTrial() throws Exception {
         // Properties p = .load("catena-perf-sql.properties");
         // JdbcDao results = new JdbcDao(p);
         // results.insertTrialMeta();
         // results.insertTrial();
         // results.insertController();
         return null;
     }
 
     public static Properties load(String propsName) throws Exception {
         URL url = ClassLoader.getSystemResource(propsName);
         Properties props = new Properties();
         props.load(url.openStream());
         return props;
     }
 
     private final Options options = new Options();
     private final CommandLineParser parser;
     private final String usage;
     private final String header;
     private String[] args;
     private static final Log log = LogFactory.getLog(Tiro.class);
     private Map<String, Object> config;
     private final JdbcDao jdbcDao;
     private int retries;
     private boolean concurrent;
     private boolean init;
 
     public Tiro(String[] args) throws Exception {
         this.args = args;
         usage = "catena-analyzer.sh -config <config-file-path> [options]...";
         header = "Runs various performance trials based on settings in the properties file."
                 + System.getProperty("line.separator")
                 + System.getProperty("line.separator") + "Options:";
 
         Option help = new Option("help", "Print this message and exit.");
 
         Option concurrent = new Option(
                 "concurrent",
                 "Turns on concurent trial execution.  Generally, trials will be run sequentially, but when this flag is supplied all trials will run concurrently");
 
         Option init = new Option("init",
                 "Creates the enviroment necessary to use the tool.  Needs to be run once.  Any previous data will be destroyed.");
 
         @SuppressWarnings("static-access")
         Option properties = OptionBuilder
                 .withArgName("file-path")
                 .hasArg()
                 .withDescription("Path to JSON file that describes the trials to run.  See the example config in the documentation for configuration options.")
                 .create("config");
 
         @SuppressWarnings("static-access")
         Option retries = OptionBuilder
                 .withArgName("n")
                 .hasArg()
                 .withDescription("Retries.  Repeats the trials described in the properties file n times where is bounded between 1 and 50 inclusive.")
                 .create("retries");
 
         options.addOption(help);
         options.addOption(concurrent);
         options.addOption(init);
         options.addOption(properties);
         options.addOption(retries);
         parser = new GnuParser();
         jdbcDao = new JdbcDao(Tiro.load("tiro-sqlite.properties"));
 
     }
 
     @SuppressWarnings("unchecked")
     private boolean parse() throws Exception {
         boolean ret = false;
         if (args != null && args.length > 0) {
             CommandLine line = null;
             try {
                 line = parser.parse(options, args);
                 assert line != null;
                 if (line.hasOption("help")) {
                     HelpFormatter formatter = new HelpFormatter();
                     formatter.printHelp(80, usage, header, options, "");
                     return false;
                 }
 
                 if (line.hasOption("init")) {
                     this.init = true;
                 }
 
                 if (line.hasOption("concurrent")) {
                     this.concurrent = true;
                 }
 
                 if (line.hasOption("config")) {
                     String jsonConfigFile = line.getOptionValue("config");
                     ObjectMapper mapper = new ObjectMapper(new JsonFactory() {
                         @Override
                         public JsonParser createJsonParser(File file) throws IOException, JsonParseException {
                             JsonParser p = super.createJsonParser(file);
                             p.enable(JsonParser.Feature.ALLOW_COMMENTS);
                             p.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
                             p.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
                             p.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
                             return p;
                         }
                     });
                     try {
                         config = mapper.readValue(new File(jsonConfigFile), Map.class);
                     } catch (IOException e) {
                         log.error("Error parsing configuration file.  The configuration file is JSON; ensure that the syntax is\n\t valid: http://json.org/.  Details of the exception follow.\n", e);
                         return false;
                     }
                     if (line.hasOption("retries")) {
                         String retries = line.getOptionValue("retries");
                         if (retries != null && !retries.equals("")) {
                             this.retries = new Integer(retries.trim()).intValue();
                             if (this.retries <= 0 || this.retries > 50) {
                                 throw new org.apache.commons.cli.ParseException("retries is bounded between 1 and 50 inclusive");
                             }
                         }
                     }
                     ret = true;
                 } else {
                     log.fatal("No config file set");
                 }
             } catch (org.apache.commons.cli.ParseException e) {
                 log.error("Exception parsing arguments", e);
             }
         } else {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printUsage(new PrintWriter(System.out), 80, usage);
             formatter = new HelpFormatter();
             formatter.printHelp(80, usage, header, options, "");
         }
         return ret;
     }
 
     public List<TrialRunner> buildTrials() {
         if (init) {
             jdbcDao.createTables();
         }
         LinkedList<TrialRunner> trialRunners = new LinkedList<TrialRunner>();
         jdbcDao.initialize();
         System.out.println(config);
         @SuppressWarnings("unchecked")
         List<Map<String, Object>> trialMetaJsons = (List<Map<String, Object>>) config.get("trials");
         for (Map<String, Object> trialMetaJson : trialMetaJsons) {
             @SuppressWarnings("unchecked")
             List<Map<String, String>> controllerMetaJsons = (List<Map<String, String>>) trialMetaJson.get("controllers");
             TrialMeta trialMeta = new TrialMeta((String) trialMetaJson.get("name"));
             jdbcDao.insertTrialMeta(trialMeta);
             Trial trial = new Trial(trialMeta);
             //jdbcDao.insertTrial(trial);
 
             Controller[] controllers = new Controller[controllerMetaJsons.size()];
             int i = 0;
             for (Map<String, String> controller : controllerMetaJsons) {
                 ControllerMeta cmeta = new ControllerMeta(controller.get("controller"), controller.get("executor"), controller.get("memory"), controller.get("device"));
                 jdbcDao.insertControllerMeta(cmeta);
                 controllers[i++] = new Controller(trialMeta, cmeta);
             }
             //jdbcDao.insertControllers(controllers);
             TrialRunnerFactory trialRunnerFactory = new TrialRunnerFactory(new IOControllerFactory(), jdbcDao);
             trialRunners.add(trialRunnerFactory.createTrialRunner(trial, controllers));
             System.out.println("Trial = " + trialMetaJson);
         }
         return trialRunners;
     }
 
     public void configurePlatform() {
         if (SystemUtils.IS_OS_MAC_OSX)
             AbstractMonitor.setPlatform(Platform.OSX);
         else if (SystemUtils.IS_OS_LINUX)
             AbstractMonitor.setPlatform(Platform.Linux);
         else if (SystemUtils.IS_OS_WINDOWS)
             AbstractMonitor.setPlatform(Platform.Windows);
         else
             throw new RuntimeException("unsupported platform");
     }
 
     public void cleanUpSystem() {
     }
 
     public void run() throws Exception {
         if (!parse()) {
             log.fatal("Failed to parse the command line - exiting.");
             return;
         }
         configurePlatform();
 
         int count = 0;
         do {
             List<TrialRunner> runners = buildTrials();
             for (TrialRunner runner : runners) {
                 runner.startTrial();
                 if (!concurrent) {
                     runner.waitForTrialCompletion();
                 }
                 cleanUpSystem();
             }
 
             if (concurrent) {
                 for (TrialRunner runner : runners) {
                     runner.waitForTrialCompletion();
                 }
             }
         } while (++count < retries);
     }
 
     public static void main(String[] args) throws Exception {
         Tiro apprentice = new Tiro(args);
         apprentice.run();
     }
 }
