 package syndeticlogic.tiro;
 
 import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 //import org.codehaus.jackson.
 //import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import syndeticlogic.tiro.controller.ControllerMeta;
 import syndeticlogic.tiro.trial.TrialMeta;
 import syndeticlogic.tiro.trial.TrialResultsJdbcDao;
 import syndeticlogic.tiro.trial.TrialRunner;
 
 public class Tiro {
     
     public static TrialRunner createSequentialScanTrial() throws Exception {
         //Properties p = .load("catena-perf-sql.properties");
         //TrialResultsJdbcDao results = new TrialResultsJdbcDao(p);
         //results.insertTrialMeta();
         //results.insertTrial();
         //results.insertController();
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
     private Map<String,Object> config;
     private final TrialResultsJdbcDao jdbcDao;
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
             jdbcDao = new TrialResultsJdbcDao(Tiro.load("tiro-sqlite.properties"));
             
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
                     } catch(IOException e) {
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
         if(init) {
             jdbcDao.createTables();
         }
         
         jdbcDao.initialize();
         System.out.println(config);
         @SuppressWarnings("unchecked")
         List<Map<String, Object>> trials = (List<Map<String, Object>>) config.get("trials");
         for(Map<String, Object> trial : trials) {
             @SuppressWarnings("unchecked")
             List<Map<String, String>> controllers = (List<Map<String, String>>) trial.get("controllers");
             TrialMeta meta = new TrialMeta((String)trial.get("name"));
            jdbcDao.insertTrialMeta(meta);
            for(Map<String,String> controller: controllers) {
                ControllerMeta cmeta = new ControllerMeta(controller.get("controller"), controller.get("executor"), controller.get("memory"), controller.get("device"));
                jdbcDao.insertControllerMeta(cmeta);
            }
             System.out.println("Trial = "+ trial);
         }
         
         return null;
     }
     
     public static void main(String[] args) throws Exception {
         Tiro t3happrentice = new Tiro(args);
         if (!t3happrentice.parse()) {
             log.fatal("Failed to parse the command line - exiting.");
             return;
         }
         
         int count = 0;
         do {
         
             List<TrialRunner> runners = t3happrentice.buildTrials();
         
             for(TrialRunner runner : runners) {
                 runner.startTrial();
                 if(!t3happrentice.concurrent) {
                     runner.waitForTrialCompletion();
                 }
             }
 
             if(t3happrentice.concurrent){
                 for(TrialRunner runner : runners) {
                     runner.waitForTrialCompletion();
                 }
             }   
         } while(++count < t3happrentice.retries);
     }    
 }
