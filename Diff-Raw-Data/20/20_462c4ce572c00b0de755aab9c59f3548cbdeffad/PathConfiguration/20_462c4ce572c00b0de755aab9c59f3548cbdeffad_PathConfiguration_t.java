 /*
 Copyright 2011 Matt DeVore
 
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
 
 package com.github.matvore.andradion2.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Capable of reading the configured value of installation paths in the Windows
  * registry.
  */
 public class PathConfiguration {
   private static final PathConfiguration INSTANCE = new PathConfiguration();
 
   private File msvcVcDirectory;
   private File windowsSdkDirectory;
 
   private PathConfiguration() {
     // Nothing to do.
   }
   
   public static PathConfiguration getInstance() {
     return INSTANCE;
   }
 
   private String runRegQuery(String command) {
     Process regQueryProcess;
     ProcessResult regQuery;
 
     try {
       regQueryProcess = Runtime.getRuntime().exec(command);
       regQuery = ProcessResult.of(regQueryProcess);
     } catch (IOException e) {
       throw new RegQueryProcessFailedException(
           "IOException when running REG QUERY process.", e);
     } catch (InterruptedException e) {
       throw new RegQueryProcessFailedException(
           "Interrupted when waiting for REG QUERY process to finish.", e);
     }
 
     if (regQuery.getExitCode() != 0) { 
       throw new RegQueryProcessFailedException("Non-zero exit code " +
           regQuery.getExitCode() + ": " + regQuery.getStdout());
     }
 
     return regQuery.getStdout();
   }
 
   /**
    * Returns the install path of Microsoft Visual C++, for instance
    * {@code c:\Program Files\Microsoft Visual Studio 9.0\VC}.
    */
   public synchronized File getMsvcVcDirectory() {
     if (null != msvcVcDirectory) {
       return msvcVcDirectory;
     }
 
     String stdout = runRegQuery(
         "REG QUERY HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\VisualStudio\\" +
         "9.0\\Setup\\VC /v ProductDir");
     Pattern pathCapture = Pattern.compile(
         "\\s+ProductDir\\s+REG_SZ\\s+([^\\r\\n]+)[\\r\\n]");
     Matcher matcher = pathCapture.matcher(stdout);
     if (!matcher.find()) {
       throw new CouldNotParseRegQueryOutputException(
           "REG QUERY output: " + stdout);
     }
 
     msvcVcDirectory = new File(matcher.group(1));
     return msvcVcDirectory;
   }
 
   /**
    * For example, {@code C:\Program Files\Microsoft Visual Studio 9.0\Common7}.
    */
   public File getMsvcCommonDirectory() {
     return new File(getMsvcVcDirectory(), ".." + File.separator + "Common7");
   }
 
   /**
    * For example, {@code C:\Program Files\Microsoft SDKs\Windows\v7.0}.
    */
   public synchronized File getWindowsSdkDirectory() {
     if (null != windowsSdkDirectory) {
       return windowsSdkDirectory;
     }
     String stdout = runRegQuery(
        "REG QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SDKs" +
        "\\Windows\" /v CurrentInstallFolder");
     Pattern pathCapture = Pattern.compile(
         "\\s+CurrentInstallFolder\\s+REG_SZ\\s+([^\\r\\n]+)[\\r\\n]");
     Matcher matcher = pathCapture.matcher(stdout);
     if (!matcher.find()) {
       throw new CouldNotParseRegQueryOutputException(
           "REG QUERY output: " + stdout);
     }
 
     windowsSdkDirectory = new File(matcher.group(1));
     return windowsSdkDirectory;
   }
 }
