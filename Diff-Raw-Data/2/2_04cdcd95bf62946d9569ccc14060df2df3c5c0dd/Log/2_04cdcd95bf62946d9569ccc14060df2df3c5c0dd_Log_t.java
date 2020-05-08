 /*******************************************************************************
  * Copyright (C) 2013 Artem Yankovskiy (artemyankovskiy@gmail.com).
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package ru.neverdark.phototools.utils;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.os.Environment;
 
 public class Log {
     /** true if DEBUG enabled or false if DEBUG disable */
    private static final boolean DEBUG = false;
     /** true if write log to file or false in other case */
     private static final boolean WRITE_FILE = false;
 
     /**
      * Logs message with class name, method name and line number
      * 
      * @param message
      *            message for logging
      */
     private static void log(String message) {
         Throwable stack = new Throwable().fillInStackTrace();
         StackTraceElement[] trace = stack.getStackTrace();
         String APP = trace[2].getClassName() + "." + trace[2].getMethodName()
                 + ":" + trace[2].getLineNumber();
         android.util.Log.i(APP, message);
     }
 
     /**
      * Function logged message to the LogCat as information message
      * 
      * @param message
      *            message for logging
      */
     public static void message(String message) {
         if (DEBUG == true) {
             log(message);
         }
     }
 
     /**
      * Saves messages from logcat to file This function must be called only once
      * from application !!! WARNING: This function erase all previous logcat
      * messages from your devices
      */
     public static void saveLogcatToFile() {
         if (DEBUG == true && WRITE_FILE == true) {
             String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
             File outputFile = new File(
                     Environment.getExternalStorageDirectory(), fileName);
             try {
                 Process process1 = Runtime.getRuntime().exec("logcat -c ");
                 Process process = Runtime.getRuntime().exec(
                         "logcat -f " + outputFile.getAbsolutePath());
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Function logged values to the LogCat as information message
      * 
      * @param variable
      *            variable name for logging
      * @param value
      *            value of the variable
      */
     public static void variable(String variable, String value) {
         if (DEBUG == true) {
             String message = variable + " = " + value;
             log(message);
         }
     }
 }
