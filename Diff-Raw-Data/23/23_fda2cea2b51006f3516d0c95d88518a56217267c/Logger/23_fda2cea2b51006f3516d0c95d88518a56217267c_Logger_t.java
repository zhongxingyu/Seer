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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 
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
 
     private volatile PrintWriter mWriter;
 
     private Logger() {
         try {
             File dest = new File("output.log");
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
      * @param message to add to the log
      */
    public void log(String tag, String message) {
         if (mWriter != null) {
             synchronized (this) {
                 mWriter.println(message);
                 mWriter.flush();
             }
 
         } else {
             System.out.println("Ignored call to log");
         }
 
     }
    
    /**
     * Example usage: <p/>
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
     * @param args Strings to populate fields with
     */
    public void log(String tag, String message, String[] args) {
        //TODO stub method
    }
 
 }
