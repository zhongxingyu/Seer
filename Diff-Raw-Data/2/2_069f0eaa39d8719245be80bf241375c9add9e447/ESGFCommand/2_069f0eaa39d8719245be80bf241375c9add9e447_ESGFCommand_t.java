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
 package esg.common.shell.cmds;
 
 /**
    Description:
    The base class for commands used by ESGF
 **/
 
 import esg.common.shell.*;
 
 import org.apache.commons.cli.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 public abstract class ESGFCommand {
  
     private static Log log = LogFactory.getLog(ESGFCommand.class);
 
     public  static final String helpOptionRegex = "(\\s|^)(--|-)(help|h)(\\s+|$)";
     public  static final Pattern helpOptionPattern = Pattern.compile(helpOptionRegex,Pattern.CASE_INSENSITIVE);
     private final Matcher m = helpOptionPattern.matcher("");
 
     protected CommandLine commandLine = null;
     protected CommandLineParser parser = null;
     protected Options options = null;
     protected HelpFormatter formatter = null;
 
     //-----
     //Setup...
     //-----
     public ESGFCommand() {
         formatter = new HelpFormatter();
         initOptions();
     }
     
     public void init(ESGFEnv env) {}
     abstract public String getCommandName();
 
     //TODO: make this properly abstract...
     public String getInfo() { return "<no info>"; }
 
    public CommandLineParser getCommandLineParser() { return (null == parser) ? this.parser = new GnuParser() : this.parser; }
     public void clearCommandLineParser()  { parser = null; }
     
 
     public void initOptions() {
         getOptions().addOption("h","help", false, "print this message");
         doInitOptions();
     };
     public void doInitOptions() {} //FIX ME?... for some reason when I make this abstract and call from constructor hell breaks loose.
     public Options getOptions() { return (null == options) ? this.options = new Options() : this.options; }
     public ESGFCommand setOptions(Options options) { this.options = options; return this; }
     public void clearOptions() { options = null; }
 
     protected void reset() {
         //TODO: remove command option completor...
         clearCommandLineParser();
         clearOptions();
         initOptions();
     }
 
     protected void showHelp() { formatter.printHelp(getCommandName(), getOptions(), true); }
     
     //-----
     //Execution...
     //-----
     final public ESGFEnv eval(String[] args, ESGFEnv env) {
         try{
 
             //NOTE: I have not been able to find any special handling
             //for 'help' that would by-pass getting caught in the
             //required fields trap... so I have to do it myself before
             //the call to parse the command line which is what puts us
             //in the --help/requiered_field conundrum.
             
             if(null != args) {
                 for(String arg : args) {
                     m.reset(arg);
                     if(m.find()) {
                         showHelp();
                         reset();
                         return env;
                     }
                 }
             }
 
             //TODO:
             //This would also be the place to add an option specific
             //completor. The idea is to be able to complete the next
             //option as you are building the command line.  It will
             //also show you what the required fields are so you can be
             //sure to fill them in
 
             //Perhaps something like this...
             //env.getReader().addCompletor(getCommandOptionCompletor());
             
             commandLine = getCommandLineParser().parse(getOptions(),args);
 
         }catch(ParseException exp) {
             // oops, something went wrong
             System.err.println(exp.getMessage());
             return env;
         }
         doEval(commandLine, env);
         reset();
         return env;
     };
     
     public abstract ESGFEnv doEval(CommandLine line, ESGFEnv env);
 
     //protected ESGFCommandOptionCompletor getCommandOptionCompletor() {
     //    return new ESGFCommandOptionCompletor(getOptions());
     //}
     
 }
