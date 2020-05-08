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
 
 package com.hhnn.tools.ant.listener;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Properties;
 
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.BuildEvent;
 
 public class WinColorConsoleLogger extends DefaultLogger
 {
   private WinColorConsole console;
   private Properties properties;
   private boolean colorSet = false;
   private int errColor = Color.FOREGROUND_RED.winCode();
   private int warnColor = Color.FOREGROUND_MAGENTA.winCode();
   private int infoColor = Color.FOREGROUND_CYAN.winCode();
   private int verboseColor = Color.FOREGROUND_GREEN.winCode();
   private int debugColor = Color.FOREGROUND_BLUE.winCode();
 
  private static boolean shouldRestore;

   private static final String SYSTEM_COLOR_FILE = "/org/apache/tools/ant/listener/defaults.properties";
 
   public WinColorConsoleLogger()
   {
     super();
     console = new WinColorConsoleImpl();
     Runtime.getRuntime().addShutdownHook(new ShutdownThread(console));
   }
 
   private void readColors()
   {
     String userColorFile;
     userColorFile = System.getProperty("ant.logger.defaults");
 
     if (userColorFile == null)
     {
       userColorFile = SYSTEM_COLOR_FILE;
     }
 
     InputStream inputFile = getClass().getResourceAsStream(userColorFile);
 
     if (inputFile == null)
     {
       try
       {
         inputFile = new FileInputStream(userColorFile);
       }
       catch (FileNotFoundException e)
       {
         log(e.getMessage());
       }
     }
 
     try
     {
       setProperties(new Properties());
       getProperties().load(inputFile);
       setErrColor(Color.ansiCodeToWinCode(properties.getProperty("AnsiColorLogger.ERROR_COLOR")));
       setWarnColor(Color.ansiCodeToWinCode(properties.getProperty("AnsiColorLogger.WARNING_COLOR")));
       setInfoColor(Color.ansiCodeToWinCode(properties.getProperty("AnsiColorLogger.INFO_COLOR")));
       setVerboseColor(Color.ansiCodeToWinCode(properties.getProperty("AnsiColorLogger.VERBOSE_COLOR")));
       setDebugColor(Color.ansiCodeToWinCode(properties.getProperty("AnsiColorLogger.DEBUG_COLOR")));
     }
     catch (IOException e)
     {
       log(e.getMessage());
     }
     finally
     {
       if (inputFile != null)
       {
         try
         {
           inputFile.close();
         }
         catch (IOException e)
         {
           log(e.getMessage());
         }
       }
     }
   }
 
   private void init()
   {
    console.keepColors();
     if (!isColorSet())
     {
       readColors();
       setColorSet(true);
     }
   }
 
   public void buildStarted(final BuildEvent event)
   {
     super.buildStarted(event);
     init();
   }
 
   public void buildFinished(final BuildEvent event)
   {
     super.buildFinished(event);
     console.restoreColors();
   }
 
   protected void printMessage(final String message, final PrintStream stream,
                               final int priority)
   {
    /*if (console == null)
    {
      init();
      shouldRestore = true;
    }*/
 
     if (message != null && stream != null)
     {
       switch (priority)
       {
       case Project.MSG_ERR:
         console.setColor(getErrColor());
         break;
       case Project.MSG_WARN:
         console.setColor(getWarnColor());
         break;
       case Project.MSG_INFO:
         console.setColor(getInfoColor());
         break;
       case Project.MSG_VERBOSE:
         console.setColor(getVerboseColor());
         break;
       case Project.MSG_DEBUG:
         // Fall through
       default:
         console.setColor(getDebugColor());
         break;
       }
       stream.println(message);
     }

    if (shouldRestore)
    {
      console.restoreColors();
    }
   }
 
   public WinColorConsole getConsole()
   {
     return console;
   }
 
   public void setConsole(final WinColorConsole console)
   {
     this.console = console;
   }
 
   private Properties getProperties()
   {
     return properties;
   }
 
   private void setProperties(final Properties properties)
   {
     this.properties = properties;
   }
 
   private boolean isColorSet()
   {
     return colorSet;
   }
 
   private void setColorSet(final boolean set)
   {
     colorSet = set;
   }
 
   private int getWarnColor()
   {
     return warnColor;
   }
 
   private void setWarnColor(final int warnColor)
   {
     this.warnColor = warnColor;
   }
 
   private int getErrColor()
   {
     return errColor;
   }
 
   private void setErrColor(final int errColor)
   {
     this.errColor = errColor;
   }
 
   private int getInfoColor()
   {
     return infoColor;
   }
 
   private void setInfoColor(final int infoColor)
   {
     this.infoColor = infoColor;
   }
 
   private int getVerboseColor()
   {
     return verboseColor;
   }
 
   private void setVerboseColor(final int verboseColor)
   {
     this.verboseColor = verboseColor;
   }
 
   private int getDebugColor()
   {
     return debugColor;
   }
 
   private void setDebugColor(final int debugColor)
   {
     this.debugColor = debugColor;
   }
 
   class ShutdownThread extends Thread
   {
     private final WinColorConsole console;
 
     ShutdownThread(final WinColorConsole console)
     {
       super();
       this.console = console;
     }
 
     private WinColorConsole getConsole()
     {
       return console;
     }
 
     public void run()
     {
       getConsole().restoreColors();
     }
   }
 }
