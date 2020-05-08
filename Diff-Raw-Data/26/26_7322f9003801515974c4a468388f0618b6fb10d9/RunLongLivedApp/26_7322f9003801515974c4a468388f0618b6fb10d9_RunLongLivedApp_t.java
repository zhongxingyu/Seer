 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.hadoop.hoya.exec;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.io.IOUtils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Execute an application.
  *
  * Hadoop's Shell class isn't used because it assumes it is executing
  * a short lived application: 
  */
 public class RunLongLivedApp implements Runnable {
   Log LOG = LogFactory.getLog(RunLongLivedApp.class);
   private final ProcessBuilder builder;
   private Process process;
   private Exception exception;
   private Integer exitCode = null;
   volatile boolean done;
   private Thread execThread;
   private Thread logThread;
 
   public RunLongLivedApp(String... commands) {
     builder = new ProcessBuilder(commands);
   }
 
   public RunLongLivedApp(List<String> commands) {
     builder = new ProcessBuilder(commands);
   }
 
   public ProcessBuilder getBuilder() {
     return builder;
   }
 
   public void setEnv(String key, String val) {
     builder.environment().put(key, val);
   }
 
   /**
    * Bulk set the environment from a map. This does
    * not replace the existing environment, just extend it/overwrite single
    * entries.
    * @param map map to add
    */
   public void setEnv(Map<String, String> map) {
     for (Map.Entry<String, String> entry : map.entrySet()) {
       setEnv(entry.getKey(), entry.getValue());
     }
   }
 
   public String getEnv(String key) {
     return builder.environment().get(key);
   }
 
   public Process getProcess() {
     return process;
   }
 
   public Exception getException() {
     return exception;
   }
 
   public List<String> getCommands() {
     return builder.command();
   }
 
   public String getCommand() {
     return getCommands().get(0);
   }
 
   public boolean isRunning() {
     return process!=null && !done;
   }
 
   public void stop() {
     if (process == null) {
       return;
     }
     process.destroy();
   }
   
   /**
    * Exec the process
    * @return the process
    * @throws IOException
    */
   public Process spawnChildProcess() throws IOException {
     if (process != null) {
       throw new IllegalStateException("Process already started");
     }
     process = builder.start();
     return process;
   }
 
 
   /**
    * Entry point for waiting for the program to finish
    */
   //@Override // Runnable
   public void run() {
     try {
       exitCode = process.waitFor();
     } catch (InterruptedException e) {
       LOG.debug("Process wait interrupted -exiting thread");
    } finally {
      //here the process has finished
      LOG.info("process has finished");
      //tell the logger it has to finish too
      done = true;
      try {
        logThread.join();
      } catch (InterruptedException ie){}
     }
   }
 
   /**
    * Create a thread to wait for this command to complete.
    * THE THREAD IS NOT STARTED.
    * @return the thread
    * @throws IOException Execution problems
    */
   public Thread spawnIntoThread() throws IOException {
     spawnChildProcess();
     Thread execThread = new Thread(this, getCommand());
     return execThread;
   }
   
   public void spawnApplication() throws IOException {
     execThread = spawnIntoThread();
     execThread.start();
     logThread = new Thread(new ProcessStreamReader(LOG, 200));
     logThread.start();
   }
   
 
   /**
    * Class to read data from the two process streams, and, when run in a thread
    * to keep running until the <code>done</code> flag is set. 
    * Lines are fetched from stdout and stderr and logged at info and error
    * respectively.
    */
   
   private class ProcessStreamReader implements Runnable {
     private final Log streamLog;
     private final int sleepTime;
 
     private ProcessStreamReader(Log streamLog, int sleepTime) {
       this.streamLog = streamLog;
       this.sleepTime = sleepTime;
     }
 
     private int readCharNonBlocking(BufferedReader reader) throws IOException {
       if (reader.ready()) {
         return reader.read();
       } else {
         return -1;
       }
     }
 
     /**
      * Read in a line, or, if the limit has been reached, the buffer
      * so far
      * @param reader source of data
      * @param line line to build
      * @param limit limit of line length
      * @return true if the line can be printed
      * @throws IOException IO trouble
      */
     private boolean readAnyLine(BufferedReader reader,
                                 StringBuilder line,
                                 int limit)
       throws IOException {
       int next ;
       while ((-1 != (next = readCharNonBlocking(reader)))) {
         if (next != '\n') {
           line.append((char)next);
           limit--;
           if (line.length() > limit) {
             //enough has been read in to print it any
             return true;
           }
         } else {
           //line end return flag to say so
           return true;
         }
       }
       //here the end of the stream is hit, or the limit
       return false;
     }
     
     
     
     //@Override //Runnable
     @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
     public void run() {
       BufferedReader errReader =null;
       BufferedReader outReader = null;
       StringBuilder outLine = new StringBuilder(256);
       StringBuilder errorLine = new StringBuilder(256);
       try {
         errReader = new BufferedReader(new InputStreamReader(process
                                                    .getErrorStream()));
         outReader = new BufferedReader(new InputStreamReader(process
                                                    .getInputStream()));
           while (!done) {
             boolean processed = false;
             if (readAnyLine(errReader, errorLine, 256)) {
               streamLog.error(errorLine);
               errorLine.setLength(0);
               processed = true;
             }
             if (readAnyLine(outReader, outLine, 256)) {
               streamLog.info(outLine);
               errorLine.setLength(0);
               processed |= true;
             }
             if(!processed) {
               try {
                 Thread.sleep(sleepTime);
               } catch (InterruptedException e) {
                 //ignore this, rely on the done flag
                 LOG.debug("Ignoring ", e);
               }
             }
           }
         //get here, done time
         streamLog.error(errorLine);
         String line= errReader.readLine();
        while (line != null) {
           streamLog.error(line);
          if(Thread.interrupted()) break;
           line = errReader.readLine();
         }

         streamLog.info(outLine);
        line = outReader.readLine();
        while (line != null) {
           streamLog.info(line);
          if(Thread.interrupted()) break;
           line = outReader.readLine();
         }
 
       } catch (Exception e) {
         LOG.debug("End of ProcessStreamReader ", e);
       } finally {
         IOUtils.closeStream(errReader);
         IOUtils.closeStream(outReader);
       }
     }
   }
 }
