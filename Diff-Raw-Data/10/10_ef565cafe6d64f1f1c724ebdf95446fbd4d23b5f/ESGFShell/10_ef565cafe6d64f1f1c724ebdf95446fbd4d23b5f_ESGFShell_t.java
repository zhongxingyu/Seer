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
         log.info("Loading ESGF Builtin Shell Commands...");
         commandMap = new HashMap<String,ESGFCommand>();
         commandMap.put("test",new esg.common.shell.cmds.ESGFtest());
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
         //(NOTE: Class loading these because they are apart of the esgf-security project... not resident to the node-manager)
         //(      Also to avoid circular dependencies between esgf-security and esgf-node-manager...)
         //---
         try{ commandMap.put("useradd",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFuseradd").newInstance())); } catch(Exception e) { log.info(" unable to load useradd: "+e.getMessage()); }
         try{ commandMap.put("userdel",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFuserdel").newInstance())); } catch(Exception e) { log.info(" unable to load userdel: "+e.getMessage()); }
         try{ commandMap.put("usermod",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFusermod").newInstance())); } catch(Exception e) { log.info(" unable to load usermod: "+e.getMessage()); }
         try{ commandMap.put("groupadd", (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFgroupadd").newInstance()));} catch(Exception e) { log.info(" unable to load groupadd: "+e.getMessage()); }
         try{ commandMap.put("groupdel", (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFgroupdel").newInstance()));} catch(Exception e) { log.info(" unable to load groupdel: "+e.getMessage()); }
         try{ commandMap.put("groupmod", (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFgroupmod").newInstance()));} catch(Exception e) { log.info(" unable to load groupmod: "+e.getMessage()); }
         try{ commandMap.put("roleadd",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFroleadd").newInstance()));} catch(Exception e) { log.info(" unable to load roleadd: "+e.getMessage()); }
         try{ commandMap.put("roledel",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFroledel").newInstance()));} catch(Exception e) { log.info(" unable to load roledel: "+e.getMessage()); }
         try{ commandMap.put("rolemod",  (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFrolemod").newInstance()));} catch(Exception e) { log.info(" unable to load rolemod: "+e.getMessage()); }
         try{ commandMap.put("associate",(ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFassociate").newInstance()));    } catch(Exception e) { log.info(" unable to load associate: "+e.getMessage()); }
         try{ commandMap.put("passwd",   (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFpasswd").newInstance()));  } catch(Exception e) { log.info(" unable to load passwd: "+e.getMessage()); }
         try{ commandMap.put("show",     (ESGFCommand)(Class.forName("esg.node.security.shell.cmds.ESGFshow").newInstance()));    } catch(Exception e) { log.info(" unable to load show: "+e.getMessage()); }
 
         //---
         //search
         //---
         //commandMap.put("search",new esg.node.search.shell.cmds.ESGFsearch());
         //commandMap.put("mark"  ,new esg.node.search.shell.cmds.ESGFmark());
         //commandMap.put("unmark",new esg.node.search.shell.cmds.ESGFunmark());
 
         //---
         //copy / replication commands
         //---
         //commandMap.put("cpds"     ,new esg.common.shell.cmds.ESGFcpds());
         //commandMap.put("realize"  ,new esg.common.shell.cmds.ESGFrealize());
         //commandMap.put("replicate",new esg.common.shell.cmds.ESGFreplicate());
 
 
         log.info("("+commandMap.size()+") commands loaded");
     }
 
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
                System.out.println("Admin Password Read is: "+env.getEnv().getAdminPassword());
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
 
         for(String commandLine : commands) {
             String[] commandLineParts = commandLine.trim().split(" ",2);
             String commandName = commandLineParts[0].trim();
             if((commandName == null) || (commandName.equals(""))) continue;
             
             log.trace("======> command ["+commandName+"] ");
 
             ESGFCommand command = commandMap.get(commandName);
             if(null == command) {
                 env.getWriter().println(commandName+": command not found :-(");
                 continue;
             }
 
             command.init(env);
             String[] args = null;
             if(commandLineParts.length == 2) {
                 args = commandLineParts[1].trim().split("\\s");
                 
                 if(log.isTraceEnabled()) {
                     log.trace("args ("+args.length+") <");
                     for(String arg : args) {
                         log.trace("["+arg+"] ");
                     }
                     log.trace(">");
                 }
             }
             command.eval(args,env);
         }
         env.getWriter().flush();
 
         if (commands[0].compareTo("rehash") == 0) {
             loadCommands();
         }
                
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
             hostname = java.net.InetAddress.getLocalHost().getHostName();
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
 
