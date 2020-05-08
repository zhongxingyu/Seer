 /*
  * Copyright 2012 Marius Volkhart
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.msoe.se2800.h4;
 
 import org.apache.commons.lang.StringUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.util.Date;
 
 /**
  * Provides a mechanism for reporting events to a file.
  * 
  * @author Marius Volkhart
  */
 public enum Logger {
 
     /**
      * The global instance of the Logger. All operations are thread safe.
      */
     INSTANCE;
 
     public static final String FILE_NAME = "output.log";
     private static final String[] EMPTY = new String[0];
 
     private volatile PrintWriter mWriter;
 
     private Logger() {
         try {
             File dest = new File(FILE_NAME);
             if (dest.exists()) {
                 dest.delete();
             }
             dest.createNewFile();
             mWriter = new PrintWriter(dest);
         } catch (IOException e) {
             mWriter = null;
             System.out
                     .println("Unable to create the log file. All future calls to log() will be ignored.");
         }
     }
 
     /**
      * Adds the provided message to the log. Is thread safe.
      * 
      * @param tag of the class making the call.
      * @param message to add to the log
      */
     public void log(String tag, String message) {
         log(tag, message, EMPTY);
     }
 
     /**
      * Example usage:
      * <p/>
      * <code>
      * public class PerfectPerson {<br/>
      * public static final String TAG = "Logger";<br/>
      * public PerfectPerson() {<br/>
      * Logger.INSTANCE.log(TAG, "My eyes are %s and my hair is %s", new String[]{"Green", "Blonde"});<br/> 
      * }<br/>
      * }<br/>
      * </code><br/>
      * will produce (PerfectPerson, My eyes are Green and my hair is Blonde).
      * 
      * @param tag of the class making the call.
      * @param message to be logged. Use %s to indicate fields.
      * @param args Strings to populate fields with. These need to be in the order they are found in
      *            the message
      */
     public void log(String tag, String message, String[] args) {
 
         if (mWriter != null) {
             synchronized (this) {
                 DateFormat format = DateFormat.getInstance();
 
                 // Print the date/timestamp
                 mWriter.print(format.format(new Date()));
                 mWriter.print(" | ");
 
                 // Print the tag
                 mWriter.print(tag);
                 mWriter.print(" | ");
 
                 if (StringUtils.countMatches(message, "%s") != args.length) {
                     throw new IllegalArgumentException("The number of placeholders in (" + message
                             + ") was not the same as the number of args (" + args.length + ").");
                 }
                 for (String s : args) {
                    message = StringUtils.replaceOnce(message, "%s", s);
                 }
 
                 // Print the message
                 mWriter.println(message);
                 mWriter.flush();
             }
 
         } else {
             System.out.println("Ignored call to log");
         }
 
     }
 
 }
