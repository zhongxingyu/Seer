 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 package esg.common.shell;
 
 /**
    Description:
    Top level class for ESGF Shell implementation...
 **/
 
 import jline.*;
 
 import java.io.*;
 import java.util.*;
 
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.apache.commons.cli.*;
 
 import esg.common.ESGException;
 import esg.common.ESGRuntimeException;
 import esg.common.shell.cmds.*;
 import esg.common.util.ESGFProperties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import static esg.common.shell.ESGFEnv.*;
 
 public class ESGFShell {
 
     private static Log log = LogFactory.getLog(ESGFShell.class);
 
     public static final Character MASK = '*';
     public static final String PIPE_RE = "\\|";
     public static final String SEMI_RE = ";";
 
     private static final Pattern commandLineParsingPattern = Pattern.compile("((?<=(\"))[\\w-? ]*(?=(\"(\\s|$))))|((?<!\")[-]*[\\w-?]+(?!\"))",Pattern.CASE_INSENSITIVE);
     private final Matcher commandLineMatcher = commandLineParsingPattern.matcher("");
 
     private static final String commandTypeRegex = "^[ ]*\\[([a-zA-Z]*)\\][ ]*$";
     private static final Pattern commandTypePattern = Pattern.compile(commandTypeRegex,Pattern.CASE_INSENSITIVE);
     private final Matcher typeMatcher = commandTypePattern.matcher("");
 
     private static final String commandEntryRegex = "[ ]*([a-zA-Z0-9-_]*)[ ]*(?:->|=)[ ]*([a-zA-Z0-9-_./]*)[ ]*$";
     private static final Pattern commandEntryPattern = Pattern.compile(commandEntryRegex,Pattern.CASE_INSENSITIVE);
     private final Matcher entryMatcher = commandEntryPattern.matcher("");
 
     String commandName = null;
     String resource = null;
 
     private Map<String,ESGFCommand> commandMap = null;
     private List<Completor> completors = null;
 
     public ESGFShell(ESGFEnv env) {
         loadCommands();
 
         completors = new LinkedList<Completor>();
         completors.add(new SimpleCompletor(commandMap.keySet().toArray(new String[]{})));
         env.getReader().addCompletor(new ArgumentCompletor(completors));
     }
 
     /**
        This is where we look through the command list and load up the
        commands made available by this shell
     */
     private void loadCommands() {
         System.out.print("Loading ESGF Builtin Shell Commands ");
         commandMap = new HashMap<String,ESGFCommand>();
         //commandMap.put("test",new esg.common.shell.cmds.ESGFtest()); //now loaded as contrib command
         commandMap.put("clear",new esg.common.shell.cmds.ESGFclear());
         commandMap.put("ls",new esg.common.shell.cmds.ESGFls());
 
         commandMap.put("set", new esg.common.shell.cmds.ESGFCommand() {
                 public String getCommandName() { return "set"; }
                 public void doInitOptions(){}
                 public ESGFEnv doEval(CommandLine line, ESGFEnv env) {
                     log.trace("inside the \"set\" command's doEval");
                     try{
                         env.putContext(DEFAULT,line.getArgs()[0],line.getArgs()[1]);
                     }catch(Throwable t) {}
                     return env;
                 }
             });
         
         commandMap.put("unset", new esg.common.shell.cmds.ESGFCommand() {
                 public String getCommandName() { return "unset"; }
                 public void doInitOptions(){}
                 public ESGFEnv doEval(CommandLine line, ESGFEnv env) {
                     log.trace("inside the \"unset\" command's doEval");
                     try{
                         env.removeContext(DEFAULT,line.getArgs()[0]);
                     }catch(Throwable t) {}
                     return env;
                 }
             });
         
         //---
         //security / administrative commands
         //(NOTE: Class loading these because they are apart of the esgf-security project... not resident to the node-manager.
         //       Avoids circular dependencies between esgf-security and esgf-node-manager...)
         //See loadCommand method below...
         //---
         loadCommand("useradd   -> esg.node.security.shell.cmds.ESGFuseradd");
         loadCommand("userdel   -> esg.node.security.shell.cmds.ESGFuserdel");
         loadCommand("usermod   -> esg.node.security.shell.cmds.ESGFusermod");
         loadCommand("groupadd  -> esg.node.security.shell.cmds.ESGFgroupadd");
         loadCommand("groupdel  -> esg.node.security.shell.cmds.ESGFgroupdel");
         loadCommand("groupmod  -> esg.node.security.shell.cmds.ESGFgroupmod");
         loadCommand("roleadd   -> esg.node.security.shell.cmds.ESGFroleadd");
         loadCommand("roledel   -> esg.node.security.shell.cmds.ESGFroledel");
         loadCommand("rolemod   -> esg.node.security.shell.cmds.ESGFrolemod");
         loadCommand("associate -> esg.node.security.shell.cmds.ESGFassociate");
         loadCommand("passwd    -> esg.node.security.shell.cmds.ESGFpasswd");
         loadCommand("show      -> esg.node.security.shell.cmds.ESGFshow");
 
         //---
         //search
         //---
         //This command must live on the index server node...
         loadCommand("ingest -> esg.node.search.shell.cmds.ESGFingest");
         //loadCommand("search -> new esg.common.shell.cmds.search.ESGFsearch");
 
         //---
         //copy / replication commands
         //---
         //commandMap.put("cpds"     ,new esg.common.shell.cmds.ESGFcpds());
         //commandMap.put("realize"  ,new esg.common.shell.cmds.ESGFrealize());
         //commandMap.put("replicate",new esg.common.shell.cmds.ESGFreplicate());
 
         //Help command...
         commandMap.put("help",new esg.common.shell.cmds.ESGFCommand() {
                 public String getCommandName() { return "help"; }
                 public String getInfo() { return "prints this command list"; }
                 public void doInitOptions(){}
                 public ESGFEnv doEval(CommandLine line, ESGFEnv env) {
                     log.trace("inside the \"help\" command's doEval");
                     try{
                         for(String commandName : ESGFShell.this.commandMap.keySet()) {
                             env.getWriter().println(commandName+"  --- "+ESGFShell.this.commandMap.get(commandName).getInfo());
                             //formatter.printUsage(env.getWriter(),
                             //                     env.getReader().getTermwidth(),
                             //                     commandName,
                             //                     ESGFShell.this.commandMap.get(commandName).getOptions());
                         }
                         env.getWriter().flush();
                     }catch(Throwable t) {}
                     return env;
                 }
             });
         commandMap.put("?", commandMap.get("help"));
         
         System.out.println();
         loadCommandsFromFile();
         System.out.println();
         log.info("("+commandMap.size()+") commands loaded");
     }
 
     private void loadCommandsFromFile() {
         System.out.print("Loading ESGF Contrib Shell Commands ");
 
         String configDir = null;
         String line = null;
         String commandType = null;
 
         if (null != (configDir = System.getenv().get("ESGF_HOME"))) {
             configDir = configDir+File.separator+"config";
             BufferedReader in = null;
             try {
                 File commandList = new File(configDir+File.separator+"esgf_contrib_commands");
                 if(commandList.exists()) {
                     in = new BufferedReader(new FileReader(commandList));
                     try{
                         while ((line = in.readLine()) != null) {
                             line = line.trim();
                             if (line.isEmpty() || line.startsWith("#")) continue; //skip blank and comment lines...
 
                             //Regex for pulling out [commandType]
                             //if the regex gets a hit, set the commandType accordingly.
                             typeMatcher.reset(line);
                             if(typeMatcher.find()) {
                                 String foundCommandType = typeMatcher.group(1);
                                 if(foundCommandType != null) {
                                     commandType = foundCommandType;
                                     log.trace("command implementation = ["+commandType+"]");
                                 }
                                 continue;
                             }
                             if (commandType == null) continue;
                             loadCommand(commandType,line);
                         }
                     }catch(java.io.IOException ex) {
                         log.error(ex);
                     }finally {
                         if(null != in) in.close();
                     }
                 }else{
                     log.trace("Could not find command file: ["+commandList.getPath()+"]");
                 }
             }catch(Throwable t) {
                 log.error(t);
             }
         }else {
             log.warn("ESGF_HOME not found in environment");
         }
     }
 
     /**
        Loads commands into the shell
      */
     private void loadCommand(String line) { this.loadCommand("java",line); }
     private void loadCommand(String commandType, String line) {
         //Regex to parse the line for commandName and resource information...
         System.out.print(".");
         entryMatcher.reset(line);
         if(entryMatcher.find()) {
             commandName = entryMatcher.group(1);
             resource = entryMatcher.group(2);
             log.trace("preparing to load ["+commandName+"] -> ["+resource+"]");
         }else{
             log.warn("Malformed shell command entry: ["+line+"]");
             return;
         }
 
         //-----
         //NOTE: yes yes, I know... there is a sexier way to do this
         //with enums, but I have to Brody this right now son.
         //-----
         if(commandType.equalsIgnoreCase("java")) {            loadJavaCommand(commandName,resource);
         }else if(commandType.equalsIgnoreCase("clojure"))   { loadClojureCommand(commandName,resource);
         }else if(commandType.equalsIgnoreCase("scala"))     { loadScalaCommand(commandName,resource);
         }else if(commandType.equalsIgnoreCase("jython"))    { loadJythonCommand(commandName,resource);
         }else if(commandType.equalsIgnoreCase("groovy"))    { loadGroovyCommand(commandName,resource);
         }else if(commandType.equalsIgnoreCase("beanshell")) { loadBeanShellCommand(commandName,resource);
         }else {
             log.warn("Unknown command implementation language ["+commandType+"]");
         }
     }
 
     //Handles (loads)  Java shell command entries
     private void loadJavaCommand(String commandName, String resource) {
         try{
             commandMap.put(commandName,(ESGFCommand)(Class.forName(resource).newInstance()));
         } catch(Exception e) {
             log.trace(" unable to load "+commandName+": "+e.getMessage());
         }
     }
 
     private void loadClojureCommand(String commandName, String resource)   { log.warn("Clojure commands not yet supported ["+commandName+"]->["+resource+"]"); }
     private void loadScalaCommand(String commandName, String resource)     { log.warn("Scala commands not yet supported ["+commandName+"]->["+resource+"]"); }
     private void loadJythonCommand(String commandName, String resource)    { log.warn("Jython commands not yet supported ["+commandName+"]->["+resource+"]"); }
     private void loadGroovyCommand(String commandName, String resource)    { log.warn("Groovy commands not yet supported ["+commandName+"]->["+resource+"]"); }
     private void loadBeanShellCommand(String commandName, String resource) { log.warn("BeanShell commands not yet supported ["+commandName+"]->["+resource+"]"); }
 
     public static void usage() {
         System.out.println("Usage: java " + ESGFShell.class.getName()+" yadda yadda yadda");
     }
 
     private void eval(String[] commands, ESGFEnv env) throws ESGException, IOException {
 
         //-------------------------
         // "quit/exit" command
         //-------------------------
         if (commands[0].equalsIgnoreCase("quit") || commands[0].equalsIgnoreCase("exit")) {
             if(getMode(env) == null) {
                 System.exit(0);
             }else{
                 clearMode(env);
                 clearUserName(env);
                 env.removeContext(SYS,"auth");
                 return;
             }
         }
 
         //-------------------------
         // "su" command
         //-------------------------
         if (commands[0].compareTo("su") == 0) {
             String password = null;
             while ((password = env.getReader().readLine("password> ", MASK)) != null) {
                 if(env.getEnv().getAdminPassword().equals(password) /*password.equals("foobar")*/) {
                     env.putContext(SYS,"user.name","rootAdmin");
                     env.putContext(SYS,"auth",true);
                     env.putContext(USER,"mode","admin");
                     break;
                 }else {
                     env.getWriter().println("incorrect password :-(");
                     env.getWriter().flush();
                 }
             }
             env.getWriter().println();
             env.getWriter().flush();
             return;
         }
 
         //-------------------------
         // "id" command
         //-------------------------
         if (commands[0].compareTo("id") == 0) {
             env.getWriter().println(getUserName(env)+":"+env.getContext(SYS,"auth"));
             env.getWriter().flush();
             return;
         }
 
         //-------------------------
         // show env object
         //-------------------------
         if (commands[0].compareTo("env") == 0) {
             env.getWriter().println(env);
             env.getWriter().flush();
             return;
         }
 
         //-------------------------
         // reload commands
         //-------------------------
         if (commands[0].compareTo("rehash") == 0) {
             loadCommands();
             return;
         }
 
         //-------------------------
 
         for(String commandLine : commands) {
             System.out.println("======> commandLine ["+commandLine+"] ");
             commandLineMatcher.reset(commandLine);
 
             List<String> argsList = new ArrayList<String>();
             String commandName = null;
                for(int i=0; commandLineMatcher.find(); i++) {
                 if(i == 0) {
                     commandName = commandLineMatcher.group();
                     System.out.println("Command: "+commandName);
                 }
                 else {
                     argsList.add(commandLineMatcher.group());
                     System.out.println("arg("+(i-1)+"): "+argsList.get(i-1));
                 }
             }
 
             if((commandName == null) || (commandName.equals(""))) continue;
            System.out.println("======> command ["+commandName+"] ");
 
             ESGFCommand command = commandMap.get(commandName);
             if(null == command) {
                 env.getWriter().println(commandName+": command not found :-(");
                 continue;
             }
 
             command.init(env);
             command.eval(argsList.toArray(new String[] {}),env);
         }
         env.getWriter().flush();
     }
 
     //------------------------
     // Helper Methods for common tasks...
     //------------------------
 
     //helper method to encapsulate common task of getting username
     public String getUserName(ESGFEnv env) {
         String whoami = null;
         if ((whoami = (String)env.getContext(SYS,"user.name")) == null) {
             whoami = clearUserName(env);
         }
         return whoami;
     }
 
     //helper method to encapsulate common task of clearing (resetting) username
     public String clearUserName(ESGFEnv env) {
         String whoami = System.getProperty("user.name");
         env.putContext(SYS,"user.name",whoami);
         return whoami;
     }
     
     //helper method to encapsulate common task of getting mode
     public String getMode(ESGFEnv env) {
         String mode = null;
         if ((mode = (String)env.getContext(USER,"mode")) == null) {
             clearMode(env);
         }
         return mode;
     }
 
     //helper method to encapsulate common task of clearing mode
     public void clearMode(ESGFEnv env) {
         env.putContext(USER,"mode",null);
     }
 
     //------------------------
     //------------------------
 
     public static void main(String[] args) throws IOException {
         if ( (args.length > 0) && (args[0].equals("--help")) ) {
             usage();
             return;
         }
 
         String hostname = "<?>";
         try{
             hostname = java.net.InetAddress.getLocalHost().getHostName().split("\\.",2)[0];
         }catch (java.net.UnknownHostException e) {
             log.error(e);
         }
 
         ConsoleReader reader = new ConsoleReader();
         reader.setBellEnabled(false);
         //String debugFile = System.getProperty("java.io.tmpdir")+File.separator+"writer.debug";
         //log.trace("("+debugFile+")");
         //reader.setDebug(new PrintWriter(new FileWriter(debugFile, true)));
 
         PrintWriter writer = new PrintWriter(System.out);
         ESGFProperties esgfProperties = null;
         try{
             esgfProperties = new ESGFProperties();
         }catch (Throwable t) {
             System.out.println(t.getMessage());
         }
         ESGFEnv env = new ESGFEnv(reader,writer,esgfProperties);
         ESGFShell shell = new ESGFShell(env);
 
         String mode = null;
         String line = null;
 
         while ((line = reader.readLine(shell.getUserName(env)+"@"+hostname+":[esgf-sh]"+( ((mode = shell.getMode(env)) == null) ? "" : ":["+mode+"]")+"> ")) != null) {
         
             try{
                 shell.eval(line.trim().split(SEMI_RE),env);
             }catch(Throwable t) {
                 System.out.println(t.getMessage());
                 //t.printStackTrace();
                 env.getWriter().flush();
             }
         }
     }
 }
 
