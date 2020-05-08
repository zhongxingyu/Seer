 package org.amplafi.flow.utils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.Properties;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import static org.amplafi.flow.utils.AdminToolCommandLineOptions.*;
 import org.amplafi.dsl.ScriptRunner;
 import org.amplafi.dsl.ScriptDescription;
 import java.util.Map;
 
 /**
  * Command line interface for running scripts to communicate with the
  * Farreach.es wire server. Please read AdminTool.md
  * for more details
  */
 public class AdminTool {
     /** Standard location for admin scripts. */
     public static final String DEFAULT_COMMAND_SCRIPT_PATH = "src/main/resources/commandScripts";
     public static final String DEFAULT_CONFIG_FILE_NAME = "fareaches.fadmin.properties";
     public static final String DEFAULT_HOST = "http://apiv1.farreach.es";
     public static final String DEFAULT_PORT = "80";
     public static final String DEFAULT_API_VERSION = "apiv1";
     private Properties configProperties;
     private String comandScriptPath;
     private String configFileName;
     private Log log;
 
     /**
      * Main entry point for tool.
      * @param args
      */
     public static void main(String[] args) {
         AdminTool adminTool = new AdminTool();
         for (String arg : args) {
             adminTool.getLog().debug("arg: " + arg);
         }
         adminTool.processCommandLine(args);
     }
 
     /**
      * Process command line.
      * @param args
      */
     public void processCommandLine(String[] args) {
         // Process command line options.
         AdminToolCommandLineOptions cmdOptions = null;
         try {
             cmdOptions = new AdminToolCommandLineOptions(args);
         } catch (ParseException e) {
             getLog().error("Could not parse passed arguments, message:", e);
             return;
         }
         // Print help if there has no args.
        if (args.length == 0 || cmdOptions.hasOption(HELP)) {
             cmdOptions.printHelp();
             return;
         }
         String list = cmdOptions.getOptionValue(LIST);
         // Obtain a list of script descriptions from the script runner
         // this will also check for basic script compilation errors or lack of
         // description lines in script.
         ScriptRunner runner = new ScriptRunner("");
         Map<String, ScriptDescription> scriptLookup = runner
                 .processScriptsInFolder(getComandScriptPath());
         if (cmdOptions.hasOption(LIST) || cmdOptions.hasOption(LISTDETAILED)) {
             // If user has asked for a list of commands then list the good
             // scripts with their
             // descriptions.
             for (ScriptDescription sd : runner.getGoodScripts()) {
                 if (cmdOptions.hasOption(LIST)) {
                     emitOutput("     " + sd.getName() + "       - "
                             + sd.getDescription());
                 } else {
                     emitOutput("     " + sd.getName() + "       - "
                             + sd.getDescription() + "       - "
                             + getRelativePath(sd.getPath()));
                 }
             }
             // List scripts that have errors if there are any
             if (!runner.getScriptsWithErrors().isEmpty()) {
                 emitOutput("The following scripts have errors: ");
             }
             for (ScriptDescription sd : runner.getScriptsWithErrors()) {
                 emitOutput("  " + getRelativePath(sd.getPath()) + "       - "
                         + sd.getErrorMesg());
             }
         } else if (cmdOptions.hasOption(HELP)) {
             // TODO print usage if has option help
             if (args.length == 1) {
                 cmdOptions.printHelp();
             } else {
                 for (int i = 1; i < args.length; i++) {
                     String scriptName = args[i];
                     for (ScriptDescription sd : runner.getGoodScripts()) {
                         if (sd.getName().equals(scriptName)) {
                             if (sd.getUsage() != null && !sd.getUsage().equals("")) {
                                 emitOutput("Script Usage: " + sd.getUsage());
                             } else {
                                 emitOutput("Script " + scriptName + 
                                            " does not have usage information");
                             }
                         }
                     }
                 }
             }
         } else if (cmdOptions.hasOption(FILE_PATH)) {
             // run an ad-hoc script from a file
             String filePath = cmdOptions.getOptionValue(FILE_PATH);
             runScript(filePath, scriptLookup, cmdOptions);
         } else {
             runScript(null, scriptLookup, cmdOptions);
         }
         // If the config properties were loaded, save them here.
         saveProperties();
         return;
     }
 
     /**
      * Get the relative path by the absolute path.
      * @param filePath is the full path to the script.
      * @return relative path of the file
      */
     private String getRelativePath(String filePath) {
         String relativePath = filePath;
         String currentPath = System.getProperty("user.dir");
         if (filePath.contains(currentPath)) {
             relativePath = filePath.substring(currentPath.length());
         }
         return relativePath;
     }
 
     /**
      * Runs the named script.
      * @param filePath is the full path to the script
      * @param scriptLookup is the map of ScriptDescription
      * @param cmdOptions is instance of AdminToolCommandLineOptions
      */
     private void runScript(String filePath,
                            Map<String, ScriptDescription> scriptLookup,
                            AdminToolCommandLineOptions cmdOptions) {
         List<String> remainder = cmdOptions.getRemainingOptions();
         try {
             // Get script options if needed
             String host = getOption(cmdOptions, HOST, DEFAULT_HOST);
             String port = getOption(cmdOptions, PORT, DEFAULT_PORT);
             String apiVersion = getOption(cmdOptions, API_VERSION,
                     DEFAULT_API_VERSION);
             String key = getOption(cmdOptions, API_KEY, "");
             // Check if we are running and ad-hoc script
             if (filePath == null) {
                 if (!remainder.isEmpty()) {
                     String scriptName = remainder.get(0);
                     if (scriptLookup.containsKey(scriptName)) {
                         ScriptDescription sd = scriptLookup.get(scriptName);
                         filePath = sd.getPath();
                     }
                     remainder.remove(0);
                 }
             }
             // Get the parameter for the script itself.
             Map<String, String> parammap = getParamMap(remainder);
             // Is verbose switched on?
             boolean verbose = cmdOptions.hasOption(VERBOSE);
             // run the script
             ScriptRunner runner2 = new ScriptRunner(host, port, apiVersion,
                     key, parammap, verbose);
             runner2.processScriptsInFolder(getComandScriptPath());
             if (filePath != null) {
                 runner2.loadAndRunOneScript(filePath);
             } else {
                 getLog().error("No script to run or not found.");
             }
         } catch (IOException ioe) {
             getLog().error("Error : " + ioe);
         }
     }
 
     /**
      * Return the script parameters as a map of param name to param valye.
      * @param remainderList is command arg list
      * @return map of the user input params
      */
     private Map<String, String> getParamMap(List<String> remainderList) {
         Map<String, String> map = new HashMap<String, String>();
         // On linux, options like param1=cat comes through as a single param
         // On windows they come through as 2 params.
         // To match options like param1=cat
         String patternStr = "(\\w+)=(\\S+)";
         Pattern p = Pattern.compile(patternStr);
         for (int i = 0; i < remainderList.size(); i++) {
             Matcher matcher = p.matcher(remainderList.get(i));
             if (matcher.matches()) {
                 // If mathces then we are looking at param1=cat as a single
                 // param
                 map.put(matcher.group(1), matcher.group(2));
             } else {
                 if (remainderList.size() > i + 1) {
                     map.put(remainderList.get(i), remainderList.get(i + 1));
                 }
                 i++;
             }
         }
         return map;
     }
 
     /**
      * Gets the program options, either from the command line, from the saved
      * properties or asks the user.
      * @param cmdOptions - Command line options
      * @param key - name of property
      * @param defaultVal - default value to suggest
      * @return the option value.
      * @throws IOException
      */
     private String getOption(AdminToolCommandLineOptions cmdOptions,
             String key, String defaultVal)
         throws IOException {
         Properties props = getProperties();
         String value = null;
         if (cmdOptions.hasOption(key)) {
             // if option passed in on commandline then use that
             value = cmdOptions.getOptionValue(key);
         } else {
             // if option is in properties then use that
             String prefValue = props.getProperty(key, "");
             if (cmdOptions.hasOption(NOCACHE) || prefValue.equals("")) {
                 // prompt the user for the option
                 System.out.print("Please, Enter : " + key
                         + " ( Enter defaults to: " + defaultVal + ") ");
                 value = getUserInput(key);
                 if ("".equals(value)) {
                     value = defaultVal;
                 }
             } else {
                 return prefValue;
             }
         }
         props.setProperty(key, value);
         return value;
     }
 
     /**
      * Gets the configuration properties, loading it if hasn't been loaded.
      * @return configuration properties.
      */
     public Properties getProperties() {
         if (configProperties == null) {
             configProperties = new Properties();
             try {
                 // load a properties file
                 configProperties.load(new FileInputStream(getConfigFileName()));
             } catch (IOException ex) {
                 getLog().error("Error loading file " + getConfigFileName());
             }
         }
         return configProperties;
     }
 
     /**
      * Saves the configuration properties, loading it if hasn't been loaded.
      */
     public void saveProperties() {
         if (configProperties != null) {
             try {
                 // load a properties file
                 configProperties.store(
                         new FileOutputStream(getConfigFileName()),
                         "Farreach.es Admin tool properties");
             } catch (IOException ex) {
                 getLog().error("Error saving file " + getConfigFileName());
             }
         }
     }
 
     /**
      * @param msg - message to emit
      */
 
     public void emitOutput(String msg) {
         getLog().info(msg);
     }
 
     /**
      * Get the logger for this class.
      */
     public Log getLog() {
         if (this.log == null) {
             this.log = LogFactory.getLog(this.getClass());
         }
         return this.log;
     }
 
     /**
      * Gets the script path for the tool.
      * @return path of the file commandScript
      */
     String getComandScriptPath() {
         if (comandScriptPath != null) {
             return comandScriptPath;
         } else {
             comandScriptPath = DEFAULT_COMMAND_SCRIPT_PATH;
         }
         return comandScriptPath;
     }
 
     /**
      * @param comandScriptPath the comandScriptPath to set
      */
     public void setComandScriptPath(String comandScriptPath) {
         this.comandScriptPath = comandScriptPath;
     }
 
     /**
      * Gets the script path for the tool.
      * @return the name of the config file
      */
     public String getConfigFileName() {
         if (configFileName != null) {
             return configFileName;
         } else {
             configFileName = DEFAULT_CONFIG_FILE_NAME;
         }
         return configFileName;
     }
 
     /**
      * @param configFileName the name of the config file
      */
     public void setConfigFileName(String configFileName) {
         this.configFileName = configFileName;
     }
 
     /**
      * @param key is the key of user input
      * @return the value of user input
      */
     public String getUserInput(String key) {
         BufferedReader consoleIn = new BufferedReader(new InputStreamReader(
                 System.in));
         String value = "";
         try {
             value = consoleIn.readLine();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return value;
     }
 }
