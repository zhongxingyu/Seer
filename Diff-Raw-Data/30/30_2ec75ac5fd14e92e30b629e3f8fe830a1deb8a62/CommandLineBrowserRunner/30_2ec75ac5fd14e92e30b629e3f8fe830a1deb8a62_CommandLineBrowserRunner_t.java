 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.browser;
 
 import com.google.jstestdriver.FileUploader;
 import com.google.jstestdriver.ProcessFactory;
 import com.google.jstestdriver.SlaveBrowser;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Runs a browser from the command line.
  * @author corbinrsmith@gmail.com (Cory Smith)
  */
 public class CommandLineBrowserRunner implements BrowserRunner {
   private static final Logger logger =
       LoggerFactory.getLogger(CommandLineBrowserRunner.class);
   private final String browserPath;
   private final String browserArgs;
   private final ProcessFactory processFactory;
   private Process process;
  private final String os;
 
   public CommandLineBrowserRunner(String browserPath,
                                   String browserArgs,
                                   ProcessFactory processFactory) {
    this(browserPath, browserArgs, processFactory, System.getProperty("os.name"));
  }
  
  public CommandLineBrowserRunner(String browserPath,
                                  String browserArgs,
                                  ProcessFactory processFactory,
                                  String os) {
     this.browserPath = browserPath;
     this.processFactory = processFactory;
     this.browserArgs = browserArgs;
    this.os = os;
   }
 
   @Override
   public void startBrowser(String serverAddress) {
     try {
       String processArgs = "";
       if (this.browserArgs.contains("%s")) {
         processArgs = this.browserArgs.replace("%s", serverAddress);
       } else {
         if (this.browserArgs.length() > 0) {
           processArgs = this.browserArgs + " ";
         }
         processArgs += serverAddress;
       }
       String[] args = processArgs.split(" ");

       String[] finalArgs;

       if (os.toLowerCase().contains("mac os")) {
         finalArgs = new String[args.length + 3];
         finalArgs[0] = "open";
         finalArgs[1] = "-a";
         finalArgs[2] = browserPath;
         System.arraycopy(args, 0, finalArgs, 3, args.length);
       } else {
         finalArgs = new String[args.length + 1];
         finalArgs[0] = browserPath;
         System.arraycopy(args, 0, finalArgs, 1, args.length);
       }
       process = processFactory.start(finalArgs);
     } catch (IOException e) {
       logger.error("Could not start: {} because {}", browserPath, e.toString());
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public void stopBrowser() {
     try {
       process.destroy();
       if (process.exitValue() != 0) {
         logger.warn("Unexpected shutdown " + process + " " + process.exitValue());
       }
     } catch (IllegalThreadStateException e) {
       logger.warn("Process refused to exit [" + browserPath +" ]: "+ process);
     }
   }
 
   @Override
   public int getTimeout() {
     return 30;
   }
 
   @Override
   public int getNumStartupTries() {
     return 1;
   }
 
   @Override
   public long getHeartbeatTimeout() {
     return SlaveBrowser.TIMEOUT;
   }
 
   @Override
   public int getUploadSize() {
     return FileUploader.CHUNK_SIZE;
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result
         + ((browserPath == null) ? 0 : browserPath.hashCode());
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null) {
       return false;
     }
     if (!(obj instanceof CommandLineBrowserRunner)) {
       return false;
     }
     CommandLineBrowserRunner other = (CommandLineBrowserRunner) obj;
     return (((browserPath == null && other.browserPath == null) ||
              browserPath.equals(other.browserPath)) &&
             ((browserArgs == null && other.browserArgs == null) ||
              browserArgs.equals(other.browserArgs)));
   }
 
   @Override
   public String toString() {
     return "CommandLineBrowserRunner [\nbrowserPath=" + browserPath +
         "\nargs=" + browserArgs +
         ",\nprocess=" + process + ",\n process log={\n" + getLog() + "\n}]";
   }
 
   private String getLog() {
     StringBuilder log = new StringBuilder("error:\n");
     if (process == null) {
       return "no process log";
     }
     InputStream errorStream = process.getErrorStream();
     InputStream outputStream = process.getInputStream();
     byte[] buffer = new byte[512];
     try {
       while(errorStream.available() > 0) {
         errorStream.read(buffer);
         log.append(buffer);
       }
     } catch (IOException e) {
       log.append("io exception reading error");
     }
     log.append("\ninput:\n");
     try {
       while(outputStream.available() > 0) {
         outputStream.read(buffer);
         log.append(buffer);
       }
     } catch (IOException e) {
       log.append("io exception reading input");
     }
     return log.toString();
   }
 }
