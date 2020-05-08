 package org.amplafi.dsl;
 
 import groovy.lang.Binding;
 import groovy.lang.Closure;
 import groovy.lang.GroovyShell;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.TreeMap;
 
 import org.amplafi.flow.definitions.FarReachesServiceInfo;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.testng.reporters.Files;
 
 import com.sworddance.util.NotNullIterator;
 
 /**
  * @author Paul
  */
 public class ScriptRunner {
 
     private FarReachesServiceInfo serviceInfo;
 
     private String key;
 
     /** Allows re-running of last script */
     private Closure lastScript;
 
     /**
      * Description of all scripts known to this runner.
      */
     public Map<String, File> scriptLookup = new TreeMap<String, File>();
 
     /**
      * Map of param name to param value that will be passed to the scripts.
      */
     private Map<String, String> paramsmap;
 
     private Log log = null;
 
     private static final String NL = System.getProperty("line.separator");
 
     /**
      * Default path for test scripts
      */
     public static final String DEFAULT_SCRIPT_PATH = "src/test/resources/testscripts";
 
     /**
      * Constructs a Script runner with individual parameters that can be overridden in scripts.
      * 
      * @param host - host address e.g. http://www.farreach.es
      * @param port - e.g. 80
      * @param apiVersion - e.g. apiv1
      * @param key - Api Key string
      */
     public ScriptRunner(FarReachesServiceInfo serviceInfo, String key) {
         this.serviceInfo = serviceInfo;
         this.key = key;
         initDefaultScriptFolder();
     }
 
     private void initDefaultScriptFolder() {
         URL commandScriptsFolder = getClass().getResource("/commandScripts/");
         if (commandScriptsFolder != null) {
             processScriptsInFolder(commandScriptsFolder.getPath());
         }
     }
 
     /**
      * Constructs a Script runner with individual parameters that can be overridden in scripts.
      * passes a map of parameters to the script
      * 
      * @param host - host address e.g. http://www.farreach.es
      * @param port - e.g. 80
      * @param apiVersion - e.g. apiv1
      * @param key - Api Key string
      * @param paramsmap - map of paramname to param value
      * @param verbose - print verbose output.
      */
     public ScriptRunner(FarReachesServiceInfo serviceInfo, String key, Map<String, String> paramsmap, boolean verbose) {
         this.serviceInfo = serviceInfo;
         this.key = key;
         this.paramsmap = paramsmap;
         initDefaultScriptFolder();
     }
 
     /**
      * This method runs all of the scripts in the DEFAULT_SCRIPT_PATH.
      * 
      * @return
      */
     public List<String> findAllTestScripts() {
         return findAllScripts(DEFAULT_SCRIPT_PATH);
     }
 
     /**
      * This method finds all scripts below the specified file path.
      * 
      * @param search path
      * @return list of script paths
      */
     public List<String> findAllScripts(String path) {
         List<String> list = new ArrayList<String>();
        File dir = new File(DEFAULT_SCRIPT_PATH);
         File[] files = dir.listFiles();
         for (File file : NotNullIterator.<File> newNotNullIterator(files)) {
             try {
                 list.add(file.getCanonicalPath());
             } catch (Exception e) {
 
             }
         }
         Collections.sort(list);
         return list;
     }
 
     /**
      * Loads and runs one script specified by the file parameter.
      * 
      * @param filePath is the full path to the script.
      * @throws IOException
      */
     public Object loadAndRunOneScript(String filePathOrName) {
         File file;
         if (scriptLookup.containsKey(filePathOrName)) {
             file = scriptLookup.get(filePathOrName);
         } else {
             file = new File(filePathOrName);
         }
         try {
             String script = Files.readFile(file);
             getLog().debug("loadAndRunOneScript() start to run runScriptSource() method");
             Object value = runScriptSource(script, file.getName());
             getLog().debug("loadAndRunOneScript() start to run runScriptSource() method");
             return value;
         } catch (IOException e) {
             throw new IllegalStateException(e);
         }
     }
 
     public void reRunLastScript() {
         if (lastScript != null) {
             lastScript.call();
         } else {
             getLog().error("No script was previously run.");
         }
     }
 
     /**
      * Runs or describes a script from source code.
      * 
      * @param sourceCode
      * @param execOrDescibe - If true then execute the source code as a command script if false run
      *            the code as a description DSL to obtain its description
      * @return The groovy closure (Why?)
      * @throws NoDescriptionException - Thrown if the description DSL does not find a description
      *             directive
      * @throws EarlyExitException - thrown to prevent the description dsl from running any commands.
      */
     public Object runScriptSource(String sourceCode, String scriptName) {
         // The script code must be pre-processed to add the contents of the file
         // into a call to FlowTestBuil der.build then the processed script is run
         // with the GroovyShell.
         getLog().debug("runScriptSource() start to get closure");
         Closure closure = getClosure(sourceCode, paramsmap, scriptName);
         getLog().debug("runScriptSource() finished to get closure");
         if (key == null || key.equals("")) {
             closure.setDelegate(new FlowTestDSL(serviceInfo, this));
         } else {
             closure.setDelegate(new FlowTestDSL(serviceInfo, key, this));
         }
         closure.setResolveStrategy(Closure.DELEGATE_FIRST);
         lastScript = closure;
         return lastScript.call();
     }
 
     /**
      * Takes the source code string and wraps in into a valid groovy script that when run will
      * return a closure. that can be either configured to describe itself or to run as a sequence of
      * commands.
      * 
      * @param sourceCode
      * @param paramsmap
      * @return
      */
     Closure getClosure(String sourceCode, Map<String, String> paramsmap, String scriptName) {
         StringBuffer scriptSb = new StringBuffer();
         // Extract the import statements from the input source code and re-add them
         // to the top of the groovy program.
         scriptSb.append(getImportLines(sourceCode));
         // All the imports are prepended to the first line of the user script so error messages
         // have the correct line number in them
         scriptSb.append("import org.amplafi.flow.utils.*;import org.amplafi.dsl.*;import org.amplafi.json.*; def source = {").append(NL);
         scriptSb.append(getValidClosureCode(sourceCode));
         scriptSb.append("}; return source;");
         String script = scriptSb.toString();
         System.out.println(script);
         Binding binding = new Binding(paramsmap);
         binding.setVariable("serviceInfo", serviceInfo);
         GroovyShell shell = new GroovyShell(ScriptRunner.class.getClassLoader(), binding);
         Closure closure = (Closure) shell.evaluate(script, scriptName);
         return closure;
     }
 
     /**
      * Creates an un-configured closure (no delegate set).
      * 
      * @param scriptName
      * @param callParamsMap - map of parameters name to value
      * @return
      * @throws IOException
      */
     public Closure createClosure(String scriptName, Map<String, String> callParamsMap) {
         String filePath = getScriptPath(scriptName);
 
         if (filePath == null) {
             URL scriptResource = getClass().getResource("/commandScripts/" + scriptName + ".groovy");
             if (scriptResource != null) {
                 filePath = scriptResource.getPath();
             }
         }
 
         if (filePath != null) {
             File file = new File(filePath);
             String sourceCode;
             try {
                 sourceCode = Files.readFile(file);
                 return getClosure(sourceCode, callParamsMap, file.getName());
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
         } else {
             getLog().error("Script " + scriptName + " does not exist");
             return null;
         }
     }
 
     /**
      * Obtain the script file path from the short name in the script description directive. This
      * should be called after processScriptsInFolder(...) or it will return null.
      * 
      * @param scriptName
      * @return file path string
      */
     String getScriptPath(String scriptName) {
         String filePath = null;
         File file = scriptLookup.get(scriptName);
         if (file != null) {
             filePath = file.getPath();
         }
         return filePath;
     }
 
     /**
      * Process all the scripts in the folder path and determine .
      * 
      * @param path
      * @return map of all scripts and their descriptions
      */
     public Map<String, File> processScriptsInFolder(String path) {
         List<File> ret = new ArrayList<File>();
         List<String> scriptPaths = findAllScripts(path);
         for (String scriptPath : scriptPaths) {
             ret.add(new File(scriptPath));
         }
         for (File file : ret) {
             String fileName = file.getName();
             int postfixPosition = fileName.indexOf(".groovy");
             if (postfixPosition > 0) {
                 scriptLookup.put(fileName.substring(0, postfixPosition), file);
             }
         }
         return scriptLookup;
     }
 
     /**
      * Get the relative path by the absolute path.
      * 
      * @param filePath is the full path to the script.
      */
     String getRelativePath(String filePath) {
         String relativePath = filePath;
         String currentPath = System.getProperty("user.dir");
         if (filePath.contains(currentPath)) {
             relativePath = filePath.substring(currentPath.length());
         }
         return relativePath;
     }
 
     /**
      * Returns the script source with import lines removed.
      * 
      * @param source - original source code.
      * @return - modified code
      */
     String getValidClosureCode(String source) {
         StringBuffer sb = new StringBuffer();
         Scanner s = new Scanner(source);
         while (s.hasNextLine()) {
             String line = s.nextLine();
             if (!line.startsWith("import")) {
                 //Escape " for groovy..
                 line = line.replaceAll("\"", "\\\"");
                 sb.append(line).append(NL);
             }
         }
         s.close();
         return sb.toString();
     }
 
     /**
      * Returns the all of the import lines from the script so they can be put in the correct
      * location in the wrapper script.
      * 
      * @param source - source code
      * @return - string of import statements.
      */
     private String getImportLines(String source) {
         StringBuffer sb = new StringBuffer();
         Scanner s = new Scanner(source);
         while (s.hasNextLine()) {
             String line = s.nextLine();
             if (line.startsWith("import")) {
                 sb.append(line).append(NL);
             }
         }
         s.close();
         return sb.toString();
     }
 
     public void setScriptLookup(Map<String, File> scriptLookup) {
         this.scriptLookup = scriptLookup;
     }
 
     /**
      * Get the logger for this class.
      */
     public synchronized Log getLog() {
         if (this.log == null) {
             this.log = LogFactory.getLog(ScriptRunner.class);
         }
         return this.log;
     }
 
 }
