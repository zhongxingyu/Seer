 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 package dk.fujitsu.issuecheck;
 
 import java.io.*;
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 public class Shell {
     private StringBuffer standard;
     private StringBuffer error;
     private Integer exitValue;
 
 
     public void execute(String[] args) throws IOException, InterruptedException {
         Runtime runtime;
         Process process;
 
         runtime = Runtime.getRuntime();
         process = runtime.exec(args);
 
         exitValue = null;
         standard = new StringBuffer();
         error = new StringBuffer();
 
 
         new StreamGobbler(process.getInputStream(), standard).start();
         new StreamGobbler(process.getErrorStream(), error).start();
 
         process.waitFor();
 
         exitValue = process.exitValue();
     }
 
     public StringBuffer getStandard() {
         return standard;
     }
 
     public StringBuffer getError() {
         return error;
     }
 
     public Integer getExitValue() {
         return exitValue;
     }
 
     private class StreamGobbler extends Thread {
         private InputStream stream;
         private StringBuffer buffer;
 
         StreamGobbler(InputStream stream, StringBuffer buffer) {
             this.stream = stream;
             this.buffer = buffer;
         }
 
         public void run() {
             Reader reader;
             char[] data;
             int length;
 
             data = new char[4096];
 
             reader = new BufferedReader(new InputStreamReader(stream));
             try {
                 while((length = reader.read(data)) != -1) {
                     buffer.append(data, 0, length);
                 }
             } catch (IOException x) {
                 System.out.println("failed reading line " + x.getMessage());
             }
         }
     }
 }
