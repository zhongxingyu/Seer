 /*
  * Sonar C# Plugin :: Core
  * Copyright (C) 2010 Jose Chillan, Alexandre Victoor and SonarSource
  * dev@sonar.codehaus.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.csharp.api.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Synchronously execute a native command line. It's much more limited than the Apache Commons Exec library. For example it does not allow
  * to get process output, to run asynchronously or to automatically quote command-line arguments.
  * 
  * TODO : This class has been introduced in Sonar 2.7, and should then be removed when plugin dependency to Sonar is upgraded to 2.7+
  */
 public final class CommandExecutor {
 
   private static final CommandExecutor INSTANCE = new CommandExecutor();
 
   private CommandExecutor() {
   }
 
   public static CommandExecutor create() {
     // stateless object, so a single singleton can be shared
     return INSTANCE;
   }
 
   public int execute(Command command, long timeoutMilliseconds) {
     ExecutorService executorService = null;
     Process process = null;
     try {
       LoggerFactory.getLogger(getClass()).debug("Executing command: " + command);
       ProcessBuilder builder = new ProcessBuilder(command.toStrings());
       process = builder.start();
 
       // consume and display the error and output streams
       StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
       StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
       outputGobbler.start();
       errorGobbler.start();
 
       final Process finalProcess = process;
       Callable<Integer> call = new Callable<Integer>() {
 
         public Integer call() throws Exception { // NOSONAR The "throws Exception" is part of this API
           finalProcess.waitFor();
           return finalProcess.exitValue();
         }
       };
 
       executorService = Executors.newSingleThreadExecutor();
       Future<Integer> ft = executorService.submit(call);
       return ft.get(timeoutMilliseconds, TimeUnit.MILLISECONDS);
 
     } catch (TimeoutException te) {
       if (process != null) {
         process.destroy();
       }
       throw new CommandException(command, "Timeout exceeded: " + timeoutMilliseconds + " ms", te);
 
     } catch (Exception e) {
       throw new CommandException(command, e);
 
     } finally {
       if (executorService != null) {
         executorService.shutdown();
       }
     }
   }
 
   private static class StreamGobbler extends Thread {
 
     private InputStream is;
 
     StreamGobbler(InputStream is) {
       this.is = is;
     }
 
     public void run() {
       Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
       InputStreamReader isr = new InputStreamReader(is);
       BufferedReader br = new BufferedReader(isr);
       try {
         String line;
         while ((line = br.readLine()) != null) {
           logger.info(line);
         }
       } catch (IOException ioe) {
        logger.error("Error while reading Obeo analyzer output", ioe);
 
       } finally {
         IOUtils.closeQuietly(br);
         IOUtils.closeQuietly(isr);
       }
     }
   }
 }
