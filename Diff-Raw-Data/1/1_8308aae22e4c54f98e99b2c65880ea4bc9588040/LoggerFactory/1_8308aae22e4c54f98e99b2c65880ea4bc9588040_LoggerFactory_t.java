 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio mdiponio
  * @date 5th October 2009
  *
  * Changelog:
  * - 05/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Returns an instance of a ILogger implementation.
  */
 public class LoggerFactory
 {
     /** System error stream logger. */
     public static final short SYSTEM_ERR_LOGGER = 0;
     
     /** File logger. */
     public static final short FILE_LOGGER = 1;
     
     /** Rolled file logger. */
     public static final short ROLLED_FILE_LOGGER = 2;
     
     /** Syslog logger. */
     public static final short SYSLOG_LOGGER = 3;
     
     /** Windows Events logger. */
     public static final short WINEVENTS_LOGGER = 4;
     
     /**
      * Types of loggers.
      */
     public enum LoggerType
     {
         SYSTEM_ERROR, FILE, ROLLED_FILE, SYSLOG, WINEVENTS
     }
     
     
     /** Logger instance. */
     private static volatile ILogger logger;
     
     static 
     {
         LoggerFactory.logger = LoggerFactory.getLoggerInternalInstance();
     }
     
     /**
      * Don't want instances of this class.
      */
     private LoggerFactory()
     { 
         /* Singleton constructor. */
     }
     
     /**
      * Returns an instance of a ILogger implementation.
      * 
      * @return ILogger instance
      */
     public static ILogger getLoggerInstance()
     {        
         return LoggerFactory.logger;
     }
     
     /**
      * Gets the ILogger subclass.
      * 
      * @return ILogger instance.
      */
     private static ILogger getLoggerInternalInstance()
     {
         switch (LoggerFactory.getLoggerType())
         {
             case SYSTEM_ERROR:
                 return new SystemErrLogger();
             case FILE:
                 return new FileLogger();
             case ROLLED_FILE:
                 return new RolledFileLogger();
             case SYSLOG:
                 return new SyslogLogger();
             case WINEVENTS:
                 return new WinEventsLogger();
                 
             default:
                 /* DODGY -> Stupid fraking compiler, anyway using system 
                  * standard error stream logger. */
                 return new SystemErrLogger();
         }
     }
     
     /**
      * Returns the configured type. If the configuration <code>Logger_Type</code>
      * fails to be loaded, the standard error stream logger is used as the
      * default.
      * 
      * @return LoggerType enumeration value
      */
     private static LoggerType getLoggerType()
     {
         final String type = ConfigFactory.getInstance().getProperty("Logger_Type");
         
         if (type == null)
         {
             LoggerFactory.printFatalLogConfigurationError("Logger_Type");
         }
         
         if (type.equalsIgnoreCase("SystemErr")) return LoggerType.SYSTEM_ERROR;
         else if (type.equalsIgnoreCase("File")) return LoggerType.FILE;
         else if (type.equalsIgnoreCase("RolledFile")) return LoggerType.ROLLED_FILE;
         else if (type.equalsIgnoreCase("Syslog")) return LoggerType.SYSLOG;
         else if (type.equalsIgnoreCase("WinEvents")) return LoggerType.WINEVENTS;
         else
         {
             LoggerFactory.printFatalLogConfigurationError("Logger_Type");
             
             /* This is to satisfy the compiler, but the rig client should crash out. */
             return LoggerType.SYSTEM_ERROR;
         }
     }
 
     /**
      * Prints a message to stderr to specify some mandatory parameter is missing.
      */
     private static void printFatalLogConfigurationError(String prop)
     {
         System.err.println("FATAL: Failed loading configuration of logger type ('" + prop + "').");
         System.err.println("'Logger_Type' specifies the destination of logging messages, with the options:");
         System.err.println("\t* SystemErr - System error stream.");
         System.err.println("\t* File - Text file.");
         System.err.println("\t* Rolled - Text file, rolled for maximum size.");
         System.err.println("\t* Syslog - Unix UDP Syslog server.");
         System.err.println("\t* WinEvents - Windows Event Log (only works on Windows).");
         System.err.println("'Log_Level' specifies the level to log messages at, with the options:");
         System.err.println("\t* ERROR - events that cause unexpected results and stop the expected program");
         System.err.println("\t          execution sequence");
         System.err.println("\t* WARN  - events that cause undesired results.");
         System.err.println("\t* INFO  - events that are regular in occurrence, however are useful for");
         System.err.println("\t          audit trails.");
         System.err.println("\t* DEBUG - debugging messages.");
         System.err.println("Shutting down...");
         
         System.exit(2);
     }
     
     /**
      * Returns the configured logging level. If the configured value fails
      * to be loaded, </code>ILogger.DEBUG</code> is returned as the default.
      * 
      * @return logging level
      */
     static short getLoggingLevel()
     {
         final String type = ConfigFactory.getInstance().getProperty("Log_Level");
         if (type == null)
         {
             LoggerFactory.printFatalLogConfigurationError("Log_Level");
         }
             
         
         if (type.equalsIgnoreCase("ERROR")) return ILogger.ERROR;
         else if (type.equalsIgnoreCase("WARN")) return ILogger.WARN;
         else if (type.equalsIgnoreCase("INFO")) return ILogger.INFO;
        else if (type.equalsIgnoreCase("DEBUG")) return ILogger.DEBUG;
         else
         {
             LoggerFactory.printFatalLogConfigurationError("Log_Level");
             
             /* This is to satisfy the compiler, but the rig client should crash out. */
             return ILogger.DEBUG;
         }
     }
     
     /**
      * Returns the configured formatting strings for each of the log types.
      * 
      * @return formatting strings
      */
     static Map<Integer, String> getFormatStrings()
     {
         Map<Integer, String> frmStrings = new HashMap<Integer, String>(6);
         IConfig config = ConfigFactory.getInstance();
         
         final String def = config.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         String frm = config.getProperty("FATAL_Log_Format");
         frmStrings.put(ILogger.FATAL, frm == null ? def : frm);
         
         frm = config.getProperty("PRIORITY_Log_Format");
         frmStrings.put(ILogger.PRIORITY, frm == null ? def : frm);
         
         frm = config.getProperty("ERROR_Log_Format");
         frmStrings.put(ILogger.ERROR, frm == null ? def : frm);
         
         frm = config.getProperty("WARN_Log_Format");
         frmStrings.put(ILogger.WARN, frm == null ? def : frm);
         
         frm = config.getProperty("INFO_Log_Format");
         frmStrings.put(ILogger.INFO, frm == null ? def : frm);
         
         frm = config.getProperty("DEBUG_Log_Format");
         frmStrings.put(ILogger.DEBUG, frm == null ? def : frm);
         
         return frmStrings;
     }
 }
