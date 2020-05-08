 /*
   Copyright 2008 Vichheann Saing
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
 package com.piaction.tools.ant.listener;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Properties;
 
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.BuildEvent;
 
 public class WinColorConsoleLogger extends DefaultLogger
 {
 
   private WinColorConsole _console;
   private boolean _colorSet = false;
   private short _errColor = Color.FOREGROUND_RED.winCode();
   private short _warnColor = Color.FOREGROUND_MAGENTA.winCode();
   private short _infoColor = Color.FOREGROUND_CYAN.winCode();
   private short _verboseColor = Color.FOREGROUND_GREEN.winCode();
   private short _debugColor = Color.FOREGROUND_BLUE.winCode();
 
   private void readColors()
   {
     String userColorFile = System.getProperty("ant.logger.defaults");
     String systemColorFile = "/org/apache/tools/ant/listener/defaults.properties";
 
     InputStream in = null;
 
     try
     {
       Properties prop = new Properties();
 
       if (userColorFile != null)
       {
          in = new FileInputStream(userColorFile);
       }
       else
       {
         in = getClass().getResourceAsStream(systemColorFile);
       }
 
       if (in != null)
       {
         prop.load(in);
       }
 
       String errC = prop.getProperty("AnsiColorLogger.ERROR_COLOR");
       String warn = prop.getProperty("AnsiColorLogger.WARNING_COLOR");
       String info = prop.getProperty("AnsiColorLogger.INFO_COLOR");
       String verbose = prop.getProperty("AnsiColorLogger.VERBOSE_COLOR");
       String debug = prop.getProperty("AnsiColorLogger.DEBUG_COLOR");
       if (errC != null) {
         _errColor = Color.ansiCodeToWinCode(errC);
       }
       if (warn != null) {
         _warnColor = Color.ansiCodeToWinCode(warn);
       }
       if (info != null) {
         _infoColor = Color.ansiCodeToWinCode(info);
       }
       if (verbose != null) {
         _verboseColor = Color.ansiCodeToWinCode(verbose);
       }
       if (debug != null) {
         _debugColor = Color.ansiCodeToWinCode(debug);
       }
     }
     catch (IOException ioe)
     {
       //Ignore - we will use the defaults.
     }
     finally
     {
       if (in != null)
       {
         try
         {
           in.close();
         }
         catch (IOException e)
         {
           //Ignore - We do not want this to stop the build.
         }
       }
     }
   }
 
  
  public void buildStarted(BuildEvent event)
   {
    super.buildStarted(event);
     _console = new WinColorConsole();
     _console.keepColors();
     if(!_colorSet)
     {
       readColors();
       _colorSet = true;
     }
   }
 
   public void buildFinished(BuildEvent event)
   {
     super.buildFinished(event);
     _console.restoreColors();
   }
   /**
    * @see DefaultLogger#printMessage
    */
   /** {@inheritDoc}. */
   protected void printMessage(final String message,
                               final PrintStream stream,
                               final int priority)
   {
      if (message != null && stream != null) 
       {
         switch (priority)
         {
           case Project.MSG_ERR:
               _console.setColor(_errColor);
               break;
           case Project.MSG_WARN:
               _console.setColor(_warnColor);
               break;
           case Project.MSG_INFO:
               _console.setColor(_infoColor);
               break;
           case Project.MSG_VERBOSE:
               _console.setColor(_verboseColor);
               break;
           case Project.MSG_DEBUG:
               // Fall through
           default:
               _console.setColor(_debugColor);
               break;
         }
         stream.println(message);
       }
   }
 }
