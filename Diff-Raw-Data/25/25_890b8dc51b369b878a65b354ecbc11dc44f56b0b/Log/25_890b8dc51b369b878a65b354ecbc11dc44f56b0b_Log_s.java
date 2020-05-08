 
 /*
 * Log.java            $Revision: 1.3 $ $Date: 2001/04/26 16:35:05 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.util;
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 
 
 /**
  * Class Log
  *
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision, $Date: 2001/04/26 16:35:05 $
  */
 public class Log {
 
     /**
      * Used to determine if a message of <code>sev</code> will be logged.
      */
     public static boolean isLogged(int sev)
     {
         return log.isLogged(sev);
     }
 
     /**
      * Method logEntry
      *
      *
      * @param sev
      * @param message
      *
      */
     public static void logEntry(int sev, String message)
     {
         log.logEntry(sev, getClassName(), message);
     }
 
     /**
      * Method logEntry
      *
      *
      * @param sev
      * @param exception
      *
      */
     public static void logEntry(int sev, Throwable exception)
     {
         log.logEntry(sev, getClassName(), exception);
     }
 
     /**
      * Method logEntry
      *
      *
      * @param sev
      * @param service
      * @param message
      *
      */
     public static void logEntry(int sev, String service, String message)
     {
         log.logEntry(sev, service, message);
     }
 
     /**
      * Method logEntry
      *
      *
      * @param sev
      * @param service
      * @param exception
      *
      */
     public static void logEntry(int sev, String service, Throwable exception)
     {
         log.logEntry(sev, service, exception);
     }
 
     /**
      * Method setLogService
      *
      *
      * @param log
      *
      */
     public static void setLogService(LogService log)
     {
         Log.log = log;
     }
 
     private static String parse(String trace)
     {
         int pos = trace.indexOf("logEntry");
 
         if (pos != -1) {
             pos = trace.indexOf("at", pos);
             String sub = trace.substring(pos + 3,
                                          trace.indexOf("(", pos + 3));
 
             return sub;
         }
 
         return "";
     }
 
     private static String getClassName()
     {
         Exception e = new Exception();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         PrintWriter pw = new PrintWriter(os, true);
 
         e.printStackTrace(pw);
 
         return parse(os.toString());
     }
 
     /** System is unusable */
     public static final int SEV_EMERGENCY = 0;
 
     /** A condition that should be corrected immediately */
     public static final int SEV_ALERT = 1;
 
     /** Critical conditions */
     public static final int SEV_CRITICAL = 2;
 
     /** Errors */
     public static final int SEV_ERROR = 3;
 
     /** Warning messages */
     public static final int SEV_WARNING = 4;
 
     /** Normal but significant conditions that may require special handling */
     public static final int SEV_NOTICE = 5;
 
     /** Informational messages */
     public static final int SEV_INFORMATIONAL = 6;
 
     /**
      * Messages that contain information normally of use only when debugging
      *  a program
      */
     public static final int SEV_DEBUG = 7;
 
     /**
      * Messages that contain a LOT of information normally of use only when debugging
      *  a program
      */
     public static final int SEV_DEBUG_VERBOSE = 8;
 
     //    private static LogService log = new NullLog();
     private static LogService log = new NullLog();
 
     private static class NullLog implements LogService {
 
         /**
          * Method logEntry
          *
          *
          * @param sev
          * @param service
          * @param message
          *
          */
         public void logEntry(int sev, String service, String message)
         {
             return;
         }
 
         /**
          * Method logEntry
          *
          *
          * @param sev
          * @param service
          * @param exception
          *
          */
         public void logEntry(int sev, String service, Throwable exception)
         {
             return;
         }
 
         public boolean isLogged(int sev)
         {
             return false;
         }
     }
 }
