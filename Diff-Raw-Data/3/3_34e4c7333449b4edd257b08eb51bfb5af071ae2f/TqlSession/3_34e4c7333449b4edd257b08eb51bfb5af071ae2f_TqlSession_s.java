 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 
 package org.mulgara.itql;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 import javax.xml.parsers.FactoryConfigurationError;
 
 // Third party packages
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import org.mulgara.query.Answer;
 import org.mulgara.query.TuplesException;
 
 
 /**
  * Command line shell for working with TQL sessions.
  *
  * @created September 11, 2007
  * @author Paul Gearon
  * @copyright &copy; 2007 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class TqlSession {
 
   private static final String USER_DIR = "user.dir";
 
   private static final int DEFAULT_HEIGHT = 480;
 
   private static final int DEFAULT_WIDTH = 640;
 
   public static final String SHELL_NAME = "TQL Shell";
 
   /** Line separator. */
   private static final String EOL = System.getProperty("line.separator");
 
   /** The prompt. */
   public final static String PS1 = "TQL> ";
 
   /** The secondary prompt, indicating an incomplete command. */
   public final static String PS2 = "---> ";
 
   /** The log4j configuration file path (within the JAR file) */
   private final static String LOG4J_CONFIG_PATH = "log4j-tql.xml";
 
   /** The logging category to use */
   private final static Logger log = Logger.getLogger(TqlSession.class);
 
   /** The default path to the pre-loading script */
   private final static String PRE_LOADING_SCRIPT_PATH = "default-pre.tql";
 
   /** The default path to the post-loading script */
   private final static String POST_LOADING_SCRIPT_PATH = "default-post.tql";
   
   /** The command prompt */
   final static String PROMPT = "TQL> ";
 
 
   /** The graphical UI. */
   private TqlSessionUI gui;
 
   /** The messages from the previous queries. */
   private List<String> messages = new ArrayList<String>();
 
   /** The answers from the previous queries. */
   private List<Answer> answers = new ArrayList<Answer>();
 
   /** The file name (URL) of the script to execute if -s is given. */
   private String scriptLocation = null;
 
   //
   // Members
   //
 
   /** The TQL auto-interpreter associated with this session */
   private final TqlAutoInterpreter autoTql;
 
   /** The URL of the post-loading script */
   private URL postLoadingScriptUrl = null;
 
   /** The URL of the pre-loading script */
   private URL preLoadingScriptUrl = null;
 
   /** A functor for splitting commands apart. */
   private CommandSplitter commandSplitter = null;
   
   /** A flag to indicate that an executed command was complete */
   private boolean incomplete = false;
 
   /** The last command that was run. */
   private String lastCommand = "";
 
   /**
    * Start an interactive TQL session from the command prompt.
    * @param args command line parameters
    * @throws MalformedURLException Provided URL for a script file is invalid.
    */
   public static void main(String[] args) throws MalformedURLException {
 
     // create a new session to work with
     TqlSession tqlSession = new TqlSession();
 
     try {
 
       // set the default pre- and post-loading scripts
       tqlSession.setDefaultLoadingScripts();
 
       // parse the command line options
       ItqlOptionParser optsParser = new ItqlOptionParser(args);
       optsParser.parse();
       boolean startSession = tqlSession.handleOptions(optsParser);
 
       if (log.isDebugEnabled()) log.debug("Processed command line options");
 
       // execute the pre-loading script - we need to do this after we get the
       // command line options as we can override the defaults
       tqlSession.executeLoadingScript(tqlSession.getPreLoadingScriptURL());
 
       if (log.isDebugEnabled()) log.debug("Executed pre-loading script");
 
       // if we can, execute this session using std in and std out
       if (startSession) tqlSession.runInterface();
       else {
         // otherwise, run the scripts we were given
         tqlSession.executeScript(tqlSession.getScript());
         tqlSession.executeLoadingScript(tqlSession.getPostLoadingScriptURL());
         tqlSession.close();
       }
 
     } catch (ItqlOptionParser.UnknownOptionException uoe) {
       errorTermination(tqlSession, "Invalid command line option: " + uoe.getOptionName());
     } catch (ItqlOptionParser.IllegalOptionValueException iove) {
       String optionMsg = "-" + iove.getOption().shortForm() + ", --" + iove.getOption().longForm() + " = " + iove.getValue();
       errorTermination(tqlSession, "Invalid command line option value specified: " + optionMsg);
     }
   }
 
   
   /**
    * Convenience method to log errors and terminate the program.
    * @param session The current session to close.
    * @param message The error message to log.
    */
   private static void errorTermination(TqlSession session, String message) {
     log.warn(message);
     System.err.println(message);
     session.printUsage(System.out);
     session.close();
   }
 
   /**
    * Constructor.  Creates a new TQL session.
    */
   public TqlSession() {
     // load the default logging configuration
     this.loadLoggingConfig();
     autoTql = new TqlAutoInterpreter();
     commandSplitter = new TqlCommandSplitter();
   }
 
 
   /**
    * Returns a list of messages (Strings) from the execution of the last
    * command or series of commands.  Successful and unsuccessful commands will
    * have valid string objects.
    * @return all the accumulated messages from the execution of the previous commands.
    */
   List<String> getLastMessages() {
     return messages;
   }
 
 
   /**
    * Returns a list of Answers from the execution of the last command or series
    * of commands.  Failures will not be included.
    * @return all the accumulated Answers from the execution of the previous commands.
    */
   List<Answer> getLastAnswers() {
     return answers;
   }
 
 
   /**
    * Indicates if the last issued command was complete. If not, then a semicolon was not found
    * to terminate the command.
    * @return <code>false</code> only if the last command was complete. <code>true</code> if it completed.
    */
   boolean isCommandIncomplete() {
     return incomplete;
   }
 
 
   /**
    * Executes a series of commands the given command.  Accumulates all the
    * results of these commands into the answers and messages lists.
    * @param command The command to execute
    */
   void executeCommand(String command) {
     // Reset answers and messages
     answers.clear();
     messages.clear();
 
     boolean previouslyIncomplete = incomplete;
 
     // presume ensuing commands are complete
     incomplete = false;
     for (String query: commandSplitter.split(command)) {
       // clear out empty commands
       if (incomplete) incomplete = false;
 
       // check if we need to follow on
       if (previouslyIncomplete) {
         query = lastCommand + query;
         previouslyIncomplete = false;
       }
       lastCommand = query + " ";
 
       if (log.isDebugEnabled()) log.debug("Starting execution of command \"" + query + "\"");
 
       // execute the command
       if (!autoTql.executeCommand(query)) {
         close();
         return;
       }
       
       String msg = autoTql.getLastMessage();
       if (msg == null) {
         if (log.isDebugEnabled()) log.debug("Need to follow on for an incomplete command.");
         incomplete = true;
         continue;
       }
 
       if (log.isDebugEnabled()) log.debug("Completed execution of commmand \"" + command + "\"");
 
       Exception e = autoTql.getLastException();
       if (e != null) log.warn("Couldn't execute command", e);
 
       // Add the message and answer
       messages.add(msg);
 
       Answer answer = autoTql.getLastAnswer();
       if (answer != null) answers.add(answer);
     }
   }
 
   /**
    * Executes a script given by URL name. {@see executeScript} for implementation.
    * @param script The string for the script URL.
    * @throws MalformedURLException The given script name cannot be represented as a URL.
    */
   private void executeScript(String script) throws MalformedURLException {
     if (script == null) return;
     executeScript(new URL(script));
   }
 
   /**
    * Executes a script.  This is done separately to {@link org.mulgara.query.operation.ExecuteScript}
    * as it expects to use a single established connection, while this method will establish new
    * connections for each line, as appropriate.
    * @param scriptURL the URL of the script to load. May be <code>null</code> in which
    *        case nothing will be done.
    */
   private void executeScript(URL scriptURL) {
 
     if (scriptURL == null) return;
 
     // log that we're executing the script
     log.debug("Executing script from " + scriptURL);
 
     // keep a record of the line number
     int line = 0;
 
     try {
 
       // create a reader to read the contents of the script
       BufferedReader scriptIn = new BufferedReader(new InputStreamReader(scriptURL.openStream()));
 
       String command;
       while ((command = scriptIn.readLine()) != null) {
 
         line++;
         command = command.trim();
 
         if (!command.equals("")) {
           autoTql.executeCommand(command);
 
           Answer answer = autoTql.getLastAnswer();
           if (answer != null) {
             printAnswer(answer, System.out);
             answer.close();
           }
 
           String lastMessage = autoTql.getLastMessage();
           if ((lastMessage != null) && (lastMessage != "") && (gui != null)) System.out.println(lastMessage);
         }
 
       }
     } catch (TuplesException te) {
       System.err.println("Error accessing results (line " + line + "): " + te.getMessage());
       log.warn("Unable to complete script - " + scriptURL + " (line " + line + ") - " + te);
     } catch (IOException ioe) {
       System.err.println("Could not execute script (line " + line + "): " + ioe);
       log.warn("Unable to execute script - " + scriptURL + " (line " + line + ") - " + ioe);
     }
   }
 
 
   /**
    * Create a UI and start it up.  Returns when the GUI has been exited.
    */
   private void runInterface() {
     // Create the UI.
     JFrame mainWindow = new JFrame(SHELL_NAME);
     mainWindow.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
     mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
     gui = new TqlSessionUI(this, System.in, System.out);
     mainWindow.getContentPane().add(gui);
 
     if (log.isInfoEnabled()) log.info("Starting TQL interpreter");
 
     motdInitialization();
 
     // Start the application, by making the UI visible
     mainWindow.setVisible(true);
 
     if (log.isInfoEnabled()) log.info("Stopping TQL interpreter");
     if (log.isDebugEnabled()) log.debug("Executed post-loading script");
   }
 
 
   /**
    * Initiates a session using the given <code>session</code>
    * @param session the interactive session to issue commands to
    * @param in the stream to read commands from
    * @param out the stream to print responses to
    */
   private void motdInitialization() {
     gui.print("TQL Command Line Interface" + EOL);
     gui.print(EOL + "Type \"help ;\", then enter for help." + EOL + EOL + PS1);
   }
 
 
   /**
    * Returns the location of the script to run.
    * @return A string with the location of the script to run.
    */
   private String getScript() throws MalformedURLException {
     return scriptLocation;
   }
 
 
   /**
    * @return the URL of the pre-loading script
    */
   private URL getPreLoadingScriptURL() {
     return preLoadingScriptUrl;
   }
 
 
   /**
    * @return the URL of the post-loading script
    */
   private URL getPostLoadingScriptURL() {
     return postLoadingScriptUrl;
   }
 
 
   /**
    * Closes the session associated with this interpreter, and ends the program.
    * Subclasses that override this method <strong>must</strong> call
    * <code>super.close()</code>.
    */
   private void close() {
     // Close the session, if any
     if (autoTql != null) autoTql.close();
     System.exit(0);
   }
 
 
   /**
    * Locates and sets the default loading scripts.
    */
   private void setDefaultLoadingScripts() {
     preLoadingScriptUrl = locateScript(PRE_LOADING_SCRIPT_PATH);
     postLoadingScriptUrl = locateScript(POST_LOADING_SCRIPT_PATH);
   }
 
 
   /**
    * <p>Locates the loading script with the given path.</p>
    * <p>This locates scripts in the following order:</p>
    * <ol>
    *   <li> Current working directory;</li>
    *   <li> System classpath (if embedded in a JAR).</li>
    * </ol>
    * <p>Note. These could be overwritten by the command-line options <code>-o</code>
    * and <code>-p</code>.</p>
    *
    * @param scriptPath the path to the script to locate
    * @return a URL to the script, <code>null</code> if the script could not be found
    */
   private URL locateScript(String scriptPath) {
 
     URL scriptUrl = null;
 
     // find the current directory
     String currentDirectory = System.getProperty(USER_DIR);
 
     // append a "/" if we need to
     if (!currentDirectory.endsWith("/")) currentDirectory += File.separator;
 
     log.debug("Looking for script " + scriptPath + " in " + currentDirectory);
 
     // try to find the script
     File loadingScript = new File(currentDirectory + scriptPath);
 
     if (loadingScript.exists() && loadingScript.isFile()) {
 
       // found the URL. Return it.
       log.debug("Found loading script - " + loadingScript);
       try {
         scriptUrl = loadingScript.toURL();
       } catch (MalformedURLException mue) {
         log.warn("Unable to convert loading script filename to URL - " + mue.getMessage());
         System.err.println("Unable to convert loading script filename " + "to URL - " + loadingScript);
       }
 
     } else {
       log.debug("Looking for loading script " + scriptPath + " in classpath");
       // try to obtain the URL from the classpath
       URL loadingScriptUrl = ClassLoader.getSystemResource(scriptPath);
       if (loadingScriptUrl != null) {
         log.debug("Found loading script at - " + loadingScriptUrl);
         scriptUrl = loadingScriptUrl;
       }
     }
 
     return scriptUrl;
   }
 
 
   /**
    * Executes the pre-loading script.
    * @param loadingScriptUrl the URL of the loading (pre/post) script to execute
    */
   private void executeLoadingScript(URL loadingScriptUrl) {
     if (loadingScriptUrl != null) {
       log.debug("Executing loading script " + loadingScriptUrl);
       executeScript(loadingScriptUrl);
     }
   }
 
 
   /**
    * Processes the command line options passed to the interpreter.
    * @param parser the command line option parser to use to parse the command line options
    * @return <code>true</code> if the UI is required, <code>false</code> if the input is a script.
    */
   private boolean handleOptions(ItqlOptionParser parser) {
 
     log.debug("Processing command line options");
 
     try {
       // find out if the user wants help
       if (parser.getOptionValue(ItqlOptionParser.HELP) != null) {
         printUsage(System.out);
         return false;  // don't start the UI
       } else {
 
         // dump the interpreter configuration
         if (null != parser.getOptionValue(ItqlOptionParser.DUMP_CONFIG)) dumpConfig();
 
         // load an external interpreter configuration file
         String itqlConf = (String)parser.getOptionValue(ItqlOptionParser.ITQL_CONFIG);
         if (itqlConf != null) loadItqlConfig(new URL(itqlConf));
 
         // load an external logging configuration file
         String logConf = (String)parser.getOptionValue(ItqlOptionParser.LOG_CONFIG);
         if (logConf != null) loadLoggingConfig(new URL((String)logConf));
 
         // find out whether to execute pre-and post loading scripts
         if (null == parser.getOptionValue(ItqlOptionParser.NO_LOAD)) {
 
           // override the default pre-loading script
           String preScript = (String)parser.getOptionValue(ItqlOptionParser.PRE_SCRIPT);
           if (preScript != null) preLoadingScriptUrl = new URL(preScript);
 
           // override the default post-loading script
           String postScript = (String)parser.getOptionValue(ItqlOptionParser.POST_SCRIPT);
           if (postScript != null) postLoadingScriptUrl = new URL(preScript);
 
         } else {
 
           log.debug("Pre-loading and post-loading scripts disabled");
           preLoadingScriptUrl = null;
           postLoadingScriptUrl = null;
         }
 
         // If there is a script to run, then return false, else true for no script
         scriptLocation = (String)parser.getOptionValue(ItqlOptionParser.SCRIPT);
         return null == scriptLocation;
       }
     } catch (MalformedURLException e) {
       log.warn("Invalid URL on command line - " + e.getMessage());
       System.err.println("Invalid URL - " + e.getMessage());
       printUsage(System.out);
     } catch (Exception e) {
       log.warn("Could not start interpreter - " + e.getMessage());
       System.err.println("Error - " + e.getMessage());
     }
     // fall through from exception
     return false;
   }
 
 
   /**
    * Prints the usage instructions for the interpreter.
    * @param out An output stream to print the instructions to.
    */
   private void printUsage(PrintStream out) {
     // build the usage message
     StringBuffer usage = new StringBuffer();
     usage.append("Usage: java -jar <jarfile> ");
 
     usage.append("[-h|-n] ");
     usage.append("[-l <url>] ");
     usage.append("[-o <url>] ");
     usage.append("[-p <url>] ");
     usage.append("[-s <url>]");
     usage.append(EOL).append(EOL);
 
     usage.append("-h, --help          display this help screen").append(EOL);
     usage.append("-n, --noload        do not execute pre- and post-loading ");
     usage.append("scripts (useful with -s)").append(EOL);
 
     usage.append("-l, --logconfig     use an external logging configuration file").append(EOL);
     
     usage.append("-o, --postload      execute an iTQL script after interpreter stops,").append(EOL);
     usage.append("                    overriding default post-loading script").append(EOL);
     usage.append("-p, --preload       execute an iTQL script before interpreter starts,").append(EOL);
     usage.append("                    overriding default pre-loading script").append(EOL);
     usage.append("-s, --script        execute an iTQL script and quit").append(EOL);
     usage.append(EOL);
     usage.append("The intepreter executes default pre- and post-loading scripts. These can be").append(EOL);
     usage.append("used to load aliases etc. into the interpreter to simplify commands. The").append(EOL);
     usage.append("default scripts are contained within the JAR file, however you can overide").append(EOL);
     usage.append("these by placing files named default-pre.itql and default-post.itql in").append(EOL);
     usage.append("the directory from which you run the interpreter, or by using the -p and").append(EOL);
     usage.append("-o options.").append(EOL);
 
     // print the usage
     out.println(usage.toString());
   }
 
 
   /**
    * Dunps the current interpreter configuration to the current directory. This
    * will dump the entire interpreter configuration including the logging and
    * application logging.
    */
   private void dumpConfig() {
     // we don't support this feature yet
     throw new UnsupportedOperationException();
   }
 
 
   /**
    * Loads an external TQL interpreter configuration file. This will use the
    * configuration in the file located at <code>itqlConfURL</code>, instead of
    * the configuration contained within the distribution JAR file.
    *
    * @param configUrl the URL of the external iTQL interpreter configuration file
    * @return <code>true</code> for successful loading of the file.
    */
   private boolean loadItqlConfig(URL configUrl) {
     // we don't support this feature yet
     throw new UnsupportedOperationException();
   }
 
 
   /**
    * Loads an external XML log4j configuration file. This will use the
    * configuration in the file located at <code>logConfURL</code>, instead of
    * the configuration contained within the distribution JAR file.
    * @param logConfUrl the URL of the external XML log4j configuration file
    * @throws Exception if unable to complete the method sucessfully
    */
   private void loadLoggingConfig(URL logConfUrl) throws Exception {
     // configure the logging service
     DOMConfigurator.configure(logConfUrl);
     log.info("Using new logging configuration from " + logConfUrl);
   }
 
 
   /**
    * Loads the embedded logging configuration (from the JAR file).
    */
   private void loadLoggingConfig() {
     // get a URL from the classloader for the logging configuration
     URL log4jConfigUrl = ClassLoader.getSystemResource(LOG4J_CONFIG_PATH);
 
     // if we didn't get a URL, tell the user that something went wrong
     if (log4jConfigUrl == null) {
       System.err.println("Unable to find logging configuration file in JAR " +
           "with " + LOG4J_CONFIG_PATH + ", reverting to default configuration.");
       BasicConfigurator.configure();
     } else {
       try {
         // configure the logging service
         DOMConfigurator.configure(log4jConfigUrl);
         log.info("Using logging configuration from " + log4jConfigUrl);
       } catch (FactoryConfigurationError e) {
         System.err.println("Unable to configure logging service");
       }
     }
   }
 
 
   /**
    * Prints an answer to a print stream.
    * @param answer The answer to print
    * @param out The print stream to send the answer to.
    * @throws TuplesException There was an error moving through the data in the answer.
    */
   private void printAnswer(Answer answer, PrintStream out) throws TuplesException {
     answer.beforeFirst();
     if (answer.isUnconstrained()) {
       out.println("[ true ]");
     } else {
       while (answer.next()) {
         out.print("[ ");
         for (int index = 0; index < answer.getNumberOfVariables(); index++) {
           out.print(String.valueOf(answer.getObject(index)));
           if (index < (answer.getNumberOfVariables() - 1)) out.print(", ");
         }
         out.println(" ]");
       }
     }
   }
 
 }
