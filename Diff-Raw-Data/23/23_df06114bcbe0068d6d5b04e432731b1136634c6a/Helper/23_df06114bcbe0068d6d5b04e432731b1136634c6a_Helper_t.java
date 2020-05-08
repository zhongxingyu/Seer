 // This file is part of Q-Cumberless Testing.
 //
 // Q-Cumberless Testing is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // Q-Cumberless Testing is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with Q-Cumberless Testing.  If not, see <http://www.gnu.org/licenses/>.
 //
 // Copyright 2012
 
 // Daniel Andersen (dani_ande@yahoo.dk)
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.util.Util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Helper {
     public static List<String> executeCommand(String command) {
         final List<String> output = new LinkedList<String>();
         executeCommand(command, null, new LogListener() {
             public void start() {
             }
 
             public void finish() {
             }
 
             public void logLine(String log) {
                 output.add(log);
             }
 
             public void error(Throwable t) {
                 t.printStackTrace();
             }
         });
         return output;
     }
 
     public static void executeCommand(String command, String dir, LogListener logListener) {
         System.out.println("Executing: '" + command + (!Util.isEmpty(dir) ? "' from dir '" + dir + "'" : "'"));
         BufferedReader stdin = null;
         try {
             logListener.start();
 
             Process exec = dir != null ? 
                     Runtime.getRuntime().exec(command, null, new File(dir)) :
                     Runtime.getRuntime().exec(command);
 
             stdin = new BufferedReader(new InputStreamReader((exec.getInputStream())));
 
             String line;
            while ((line = stdin.readLine()) != null) {
                logListener.logLine(line);
            }
            try {
                int res = exec.waitFor();
                if (res > 0) {
                    throw new Exception("Process failed with return value: " + res);
                 }
            } catch (Exception e) {
                e.printStackTrace();
             }
             logListener.finish();
         } catch (Throwable t) {
             t.printStackTrace();
             logListener.error(t);
         } finally {
             Util.close(stdin);
         }
     }
 
     public static void executeCommand(String command, LogListener logListener, ExecutorStopper executorStopper) {
         executeCommand(command, null, logListener, executorStopper);
     }
 
     public static void executeCommand(String command, String dir, LogListener logListener, ExecutorStopper executorStopper) {
         System.out.println("Executing with stopper: " + command);
         BufferedReader stdin = null;
         try {
             logListener.start();
 
             Process exec = dir != null ?
                     Runtime.getRuntime().exec(command, null, new File(dir)) :
                     Runtime.getRuntime().exec(command);
 
             stdin = new BufferedReader(new InputStreamReader((exec.getInputStream())));
 
             String line;
             boolean running = true;
             while (running && !executorStopper.isStopped()) {
                 while ((line = stdin.readLine()) != null && running && !executorStopper.isStopped()) {
                     logListener.logLine(line);
                 }
                 try {
                     int res = exec.exitValue();
                     running = false;
                     if (res > 0) {
                         throw new Exception("Process failed with return value: " + res);
                     }
                 } catch (IllegalThreadStateException e) {
                     running = true;
                 }
             }
             logListener.finish();
         } catch (Throwable t) {
             t.printStackTrace();
             logListener.error(t);
         } finally {
             Util.close(stdin);
         }
     }
 
     public static class ExecutorStopper {
         private boolean stopped = false;
 
         public void stop() {
             stopped = true;
         }
 
         public boolean isStopped() {
             return stopped;
         }
     }
 }
