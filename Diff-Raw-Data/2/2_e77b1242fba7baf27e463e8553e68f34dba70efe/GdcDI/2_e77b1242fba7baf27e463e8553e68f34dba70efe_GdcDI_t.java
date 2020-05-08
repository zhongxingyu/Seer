 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.processor;
 
 import java.io.*;
 import java.util.*;
 
 import com.gooddata.connector.*;
 import com.gooddata.connector.backend.DerbyConnectorBackend;
 import com.gooddata.connector.backend.MySqlConnectorBackend;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.naming.N;
 import com.gooddata.processor.parser.DIScriptParser;
 import com.gooddata.processor.parser.ParseException;
 
 import com.gooddata.exception.*;
 import org.apache.commons.cli.*;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.gooddata.connector.Connector;
 
 import com.gooddata.util.FileUtil;
 import org.gooddata.connector.backend.ConnectorBackend;
 import org.gooddata.processor.CliParams;
 import org.gooddata.processor.Command;
 import org.gooddata.processor.Executor;
 import org.gooddata.processor.ProcessingContext;
 
 /**
  * The GoodData Data Integration CLI processor.
  *
  * @author jiri.zaloudek
  * @author Zdenek Svoboda <zd@gooddata.org>
  * @version 1.0
  */
 public class GdcDI implements Executor {
 
     private static Logger l = Logger.getLogger(GdcDI.class);
 
     //Options data
     public static String[] CLI_PARAM_USERNAME = {"username","u"};
     public static String[] CLI_PARAM_PASSWORD = {"password","p"};
 
     public static String[] CLI_PARAM_HOST = {"host","h"};
     public static String[] CLI_PARAM_FTP_HOST = {"ftphost","f"};
     public static String[] CLI_PARAM_PROJECT = {"project","i"};
     public static String[] CLI_PARAM_BACKEND = {"backend","b"};
     public static String[] CLI_PARAM_DB_USERNAME = {"dbusername","d"};
     public static String[] CLI_PARAM_DB_PASSWORD = {"dbpassword","c"};
     public static String[] CLI_PARAM_PROTO = {"proto","t"};
     public static String[] CLI_PARAM_EXECUTE = {"execute","e"};
     public static String CLI_PARAM_SCRIPT = "script";
 
     // mandatory options
     public static Option[] mandatoryOptions = {
         new Option(CLI_PARAM_USERNAME[1], CLI_PARAM_USERNAME[0], true, "GoodData username"),
         new Option(CLI_PARAM_PASSWORD[1], CLI_PARAM_PASSWORD[0], true, "GoodData password"),
     };
 
     // optional options
     public static Option[] optionalOptions = {
         new Option(CLI_PARAM_HOST[1], CLI_PARAM_HOST[0], true, "GoodData host"),
         new Option(CLI_PARAM_FTP_HOST[1], CLI_PARAM_FTP_HOST[0], true, "GoodData FTP host"),
         new Option(CLI_PARAM_PROJECT[1], CLI_PARAM_PROJECT[0], true, "GoodData project identifier (a string like nszfbgkr75otujmc4smtl6rf5pnmz9yl)"),
         new Option(CLI_PARAM_BACKEND[1], CLI_PARAM_BACKEND[0], true, "Database backend DERBY or MYSQL"),
         new Option(CLI_PARAM_DB_USERNAME[1], CLI_PARAM_DB_USERNAME[0], true, "Database backend username (not required for the local Derby SQL)"),
         new Option(CLI_PARAM_DB_PASSWORD[1], CLI_PARAM_DB_PASSWORD[0], true, "Database backend password (not required for the local Derby SQL)"),
         new Option(CLI_PARAM_PROTO[1], CLI_PARAM_PROTO[0], true, "HTTP or HTTPS"),
         new Option(CLI_PARAM_EXECUTE[1], CLI_PARAM_EXECUTE[0], true, "Commands and params to execute before the commands in provided files")
     };
 
     private String projectId = null;
     private Connector connector = null;
     private CliParams cliParams = null;
     private Connector[] connectors = null;
 
     private ProcessingContext context = new ProcessingContext();
 
     private static long  LOCK_EXPIRATION_TIME = 1000 * 3600; // 1 hour
 
     private GdcDI(CommandLine ln) {
         try {
             cliParams = parse(ln);
             cliParams.setHttpConfig(new NamePasswordConfiguration(
             		cliParams.get(CLI_PARAM_PROTO[0]), cliParams.get(CLI_PARAM_HOST[0]),
                     cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0])));
             cliParams.setFtpConfig(new NamePasswordConfiguration(
                     cliParams.get(CLI_PARAM_PROTO[0]), cliParams.get(CLI_PARAM_FTP_HOST[0]),
                     cliParams.get(CLI_PARAM_USERNAME[0]), cliParams.get(CLI_PARAM_PASSWORD[0])));
             connectors = instantiateConnectors();
             String execute = cliParams.get(CLI_PARAM_EXECUTE[0]);
             String scripts = cliParams.get(CLI_PARAM_SCRIPT);
             
             if(execute!= null && scripts != null && execute.length()>0 && scripts.length()>0) {
                 throw new InvalidArgumentException("You can't execute a script and use the -e command line parameter at the same time.");
             }
             if(execute!= null && execute.length() > 0) {
                 l.debug("Executing arg="+execute);
                 execute(execute);
             }
             if(scripts!= null && scripts.length() > 0) {
                 String[] sas = scripts.split(",");
                 for(String script : sas) {
                     l.debug("Executing file="+script);
                     execute(new File(script));
                 }
             }
         }
         catch (InvalidArgumentException e) {
             l.error("Invalid command line argument: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Invalid command line argument:",e);
             l.info(commandsHelp());
         }
         catch (InvalidCommandException e) {
             l.error("Invalid command: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Invalid command.",e);
         }
         catch (SfdcException e) {
             l.error("Error communicating with SalesForce: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error communicating with SalesForce.",e);
         }
         catch (ProcessingException e) {
             l.error("Error processing command: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error processing command.",e);
         }
         catch (ModelException e) {
             l.error("Model issue: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Model issue.",e);
         }
         catch (GdcLoginException e) {
             l.error("Error logging to GoodData. Please check your GoodData username and password: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error logging to GoodData. Please check your GoodData username and password.",e);
         }        
         catch (IOException e) {
             l.error("Encountered an IO or database problem: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Encountered an IO or database problem.",e);
         }
         catch (InternalErrorException e) {
             l.error("Internal error: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("REST API invocation error: ",e);
         }
         catch (HttpMethodException e) {
             l.error("Error executing GoodData REST API: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Error executing GoodData REST API.",e);
         }
         catch (GdcRestApiException e) {
             l.error("REST API invocation error: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("REST API invocation error: ", e);
         }
         catch (GdcException e) {
             l.error("Unrecognized error: "+e.getMessage());
             Throwable c = e.getCause();
             while(c!=null) {
                 l.error("Caused by: "+c.getMessage());
                 c = c.getCause();
             }
             l.debug("Unrecognized error: ",e);
         }
 
 
     }
 
     /**
      * Returns all cli options
      * @return all cli options
      */
     public static Options getOptions() {
         Options ops = new Options();
         for( Option o : mandatoryOptions)
             ops.addOption(o);
         for( Option o : optionalOptions)
             ops.addOption(o);
         return ops;
     }
 
     /**
      * Parse and validate the cli arguments
      * @param ln parsed command line
      * @return parsed cli parameters wrapped in the CliParams
      * @throws InvalidArgumentException in case of nonexistent or incorrect cli args
      */
     protected CliParams parse(CommandLine ln) throws InvalidArgumentException {
         l.debug("Parsing cli "+ln);
         CliParams cp = new CliParams();
         for( Option o : mandatoryOptions) {
             String name = o.getLongOpt();
             if (ln.hasOption(name))
                 cp.put(name,ln.getOptionValue(name));
             else {
                 throw new InvalidArgumentException("Missing the '"+name+"' commandline parameter.");
             }
 
         }
 
         for( Option o : optionalOptions) {
             String name = o.getLongOpt();
             if (ln.hasOption(name))
                 cp.put(name,ln.getOptionValue(name));
         }
 
         // use default host if there is no host in the CLI params
         if(!cp.containsKey(CLI_PARAM_HOST[0])) {
             cp.put(CLI_PARAM_HOST[0], Defaults.DEFAULT_HOST);
         }
 
         l.debug("Using host "+cp.get(CLI_PARAM_HOST[0]));
 
         // create default FTP host if there is no host in the CLI params
         if(!cp.containsKey(CLI_PARAM_FTP_HOST[0])) {
             String[] hcs = cp.get(CLI_PARAM_HOST[0]).toString().split("\\.");
             if(hcs != null && hcs.length > 0) {
                 String ftpHost = "";
                 for(int i=0; i<hcs.length; i++) {
                     if(i>0)
                         ftpHost += "." + hcs[i];
                     else
                         ftpHost = hcs[i] + N.FTP_SRV_SUFFIX;
                 }
                 cp.put(CLI_PARAM_FTP_HOST[0],ftpHost);
             }
             else {
                 throw new IllegalArgumentException("Invalid format of the GoodData REST API host: " +
                         cp.get(CLI_PARAM_HOST[0]));
             }
 
         }
 
         l.debug("Using FTP host "+cp.get(CLI_PARAM_FTP_HOST[0]));
 
         // use default protocol if there is no host in the CLI params
         if(!cp.containsKey(CLI_PARAM_PROTO[0])) {
             cp.put(CLI_PARAM_PROTO[0], Defaults.DEFAULT_PROTO);
         }
         else {
             String proto = ln.getOptionValue(CLI_PARAM_PROTO[0]).toLowerCase();
             if(!"http".equalsIgnoreCase(proto) && !"https".equalsIgnoreCase(proto)) {
                 throw new InvalidArgumentException("Invalid '"+CLI_PARAM_PROTO[0]+"' parameter. Use HTTP or HTTPS.");
             }
             cp.put(CLI_PARAM_PROTO[0], proto);
         }
 
         l.debug("Using protocol "+cp.get(CLI_PARAM_PROTO[0]));
 
         // use default backend if there is no host in the CLI params
         if(!cp.containsKey(CLI_PARAM_BACKEND[0])) {
             cp.put(CLI_PARAM_BACKEND[0], Defaults.DEFAULT_BACKEND);
         }
         else {
             String b = ln.getOptionValue(CLI_PARAM_BACKEND[0]).toLowerCase();
             if(!"mysql".equalsIgnoreCase(b) && !"derby".equalsIgnoreCase(b))
                 b = "derby";
             cp.put(CLI_PARAM_BACKEND[0], b);
         }
 
         l.debug("Using backend "+cp.get(CLI_PARAM_BACKEND[0]));
 
         if (ln.getArgs().length == 0 && !ln.hasOption("execute")) {
             throw new InvalidArgumentException("No command has been given, quitting.");
         }
 
         String scripts = "";
         for (final String arg : ln.getArgs()) {
             if(scripts.length()>0)
                 scripts += ","+arg;
             else
                 scripts += arg;
         }
         cp.put(CLI_PARAM_SCRIPT, scripts);
         return cp;
     }
 
 
     /**
      * Executes the commands in String
      * @param commandsStr commansd string
      */
     public void execute(final String commandsStr) {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(commandsStr));
         for(Command command : cmds) {
             boolean processed = false;
             for(int i=0; i<connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if(!processed)
                 this.processCommand(command, cliParams, context);
         }
     }
 
     /**
      * Executes the commands in file
      * @param scriptFile file with commands
      */
     public void execute(final File scriptFile) throws IOException {
         List<Command> cmds = new ArrayList<Command>();
         cmds.addAll(parseCmd(FileUtil.readStringFromFile(scriptFile.getAbsolutePath())));
         for(Command command : cmds) {
             boolean processed = false;
             for(int i=0; i<connectors.length && !processed; i++) {
                 processed = connectors[i].processCommand(command, cliParams, context);
             }
             if(!processed)
                 processed = this.processCommand(command, cliParams, context);
             if(!processed)
                 throw new InvalidCommandException("Unknown command '"+command.getCommand()+"'");
         }
     }
 
     /**
      * Returns the help for commands
      * @return help text
      */
     public static String commandsHelp() {
         try {
         	final InputStream is = CliParams.class.getResourceAsStream("/com/gooddata/processor/COMMANDS.txt");
         	if (is == null)
         		throw new IOException();
             return FileUtil.readStringFromStream(is);
         } catch (IOException e) {
             l.error("Could not read com/gooddata/processor/COMMANDS.txt");
         }
         return "";
     }
     
 
     private static boolean checkJavaVersion() {
         String version = System.getProperty("java.version");
         if(version.startsWith("1.6"))
             return true;
         l.error("You're running Java "+version+". Please use Java 6 (1.6) for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for the Java 6 installation.");
         throw new InternalErrorException("You're running Java "+version+". Please use Java 6 (1.6) for running this tool. " +
                 "Please refer to http://java.sun.com/javase/downloads/index.jsp for the Java 6 installation.");
     }
 
     /**
      * The main CLI processor
      * @param args command line argument
      */
     public static void main(String[] args) {
 
         checkJavaVersion();
         PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
         try {
             Options o = getOptions();
             CommandLineParser parser = new GnuParser();
             new GdcDI(parser.parse(o, args));
         } catch (org.apache.commons.cli.ParseException e) {
             l.error("Error parsing command line parameters: "+e.getMessage());
             l.debug("Error parsing command line parameters",e);
         }
     }
 
     /**
      * Parses the commands
      * @param cmd commands string
      * @return array of commands
      * @throws InvalidCommandException in case there is an invalid command
      */
     protected static List<Command> parseCmd(String cmd) throws InvalidCommandException {
         l.debug("Parsing comands: "+cmd);
         try {
             if(cmd != null && cmd.length()>0) {
                 Reader r = new StringReader(cmd);
                 DIScriptParser parser = new DIScriptParser(r);
                 List<Command> commands = parser.parse();
                 l.debug("Running "+commands.size()+" commands.");
                 for(Command c : commands) {
                     l.debug("Command="+c.getCommand()+" params="+c.getParameters());
                 }
                 return commands;
             }
         }
         catch(ParseException e) {
             throw new InvalidCommandException("Can't parse command '" + cmd + "'");
         }
         throw new InvalidCommandException("Can't parse command (empty command).");
     }
 
 
     /**
      * {@inheritDoc}
      */
     public boolean processCommand(Command c, CliParams cli, ProcessingContext ctx) throws ProcessingException {
         l.debug("Processing command "+c.getCommand());
         try {
         	// take project id from command line, may be override in the script
         	if (cliParams.get(CLI_PARAM_PROJECT[0]) != null) {
             	ctx.setProjectId(cliParams.get(CLI_PARAM_PROJECT[0]));
             }
             if(c.match("CreateProject")) {
                 createProject(c, cli, ctx);
             }
            else if(c.match("DropProject")) {
                 dropProject(c, cli, ctx);
             }
             else if(c.match("OpenProject")) {
                 ctx.setProjectId(c.getParamMandatory("id"));
                 l.debug("Opened project id="+ctx.getProjectId());
             }
             else if(c.match("StoreProject")) {
                 storeProject(c, cli, ctx);
             }
             else if(c.match("RetrieveProject")) {
                 retrieveProject(c, cli, ctx);
             }
             else if(c.match( "Lock")) {
                 lock(c, cli, ctx);
             }
             else {
                 l.debug("No match command "+c.getCommand());
                 return false;
             }
         }
         catch (IOException e) {
             l.debug("Processing command "+c.getCommand()+" failed",e);
             throw new ProcessingException(e);
         }
         l.debug("Processed command "+c.getCommand());
         return true;
     }
 
 
     /**
      * Create new project command processor
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      */
     private void createProject(Command c, CliParams p, ProcessingContext ctx) {
         String name = c.getParamMandatory("name");
         ctx.setProjectId(ctx.getRestApi(p).createProject(name, name));
         String pid = ctx.getProjectId();
         l.info("Project id = '"+pid+"' created.");
     }
 
     /**
      * Drop project command processor
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      */
     private void dropProject(Command c, CliParams p, ProcessingContext ctx) {
         String id = c.getParamMandatory("id");
         ctx.getRestApi(p).dropProject(id);
         l.info("Project id = '"+id+"' dropped.");
     }
 
     /**
      * Store project command processor
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      */
     private void storeProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         String pid = ctx.getProjectId();
         FileUtil.writeStringToFile(pid, fileName);
         l.debug("Stored project id="+pid+" to "+fileName);
     }
 
     /**
      * Retrieve project command processor
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      */
     private void retrieveProject(Command c, CliParams p, ProcessingContext ctx) throws IOException {
         String fileName = c.getParamMandatory("fileName");
         ctx.setProjectId(FileUtil.readStringFromFile(fileName).trim());
         l.debug("Retrieved project id="+ctx.getProjectId()+" from "+fileName);
     }
 
     /**
      * Lock project command processor
      * @param c command
      * @param p cli parameters
      * @param ctx current context
      */
     private void lock(Command c, CliParams p, ProcessingContext ctx) throws IOException {
     	final String path = c.getParamMandatory( "path");
     	final File lock = new File(path);
     	if (!lock.createNewFile()) {
     		if (System.currentTimeMillis() - lock.lastModified() > LOCK_EXPIRATION_TIME) {
     			lock.delete();
     			if (!lock.exists()) {
     				lock(c, p, ctx); // retry
     			}
     		}
             l.debug("A concurrent process found using the " + path + " lock file.");
     		throw new IOException("A concurrent process found using the " + path + " lock file.");
     	}
     	lock.deleteOnExit();
     }
 
     /**
      * Instantiate all known connectors
      * TODO: this should be automated
      * @throws IOException in case of IO issues
      */
     private Connector[] instantiateConnectors() throws IOException {
         String b = cliParams.get(CLI_PARAM_BACKEND[0]);
         ConnectorBackend backend = null;
         if("mysql".equalsIgnoreCase(b))
             backend = MySqlConnectorBackend.create(cliParams.get(CLI_PARAM_DB_USERNAME[0]),
                     cliParams.get(CLI_PARAM_DB_PASSWORD[0]));
         else if("derby".equalsIgnoreCase(b))
             backend = DerbyConnectorBackend.create();
         return new Connector[] {
             CsvConnector.createConnector(backend),
             GaConnector.createConnector(backend),
             SfdcConnector.createConnector(backend),
             JdbcConnector.createConnector(backend),
             TimeDimensionConnector.createConnector()    
         };
     }
 
 }
