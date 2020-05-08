 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.rapidcontext.core.js.JsSerializer;
 import org.rapidcontext.core.proc.ProcedureException;
 import org.rapidcontext.core.security.SecurityContext;
 
 /**
  * The main command-line application.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class ScriptApplication {
 
     /**
      * The base application directory. Defaults to the current
      * directory.
      */
     public File appDir = new File(".");
 
     /**
      * The local add-on directory. Defaults to the current
      * directory.
      */
     public File localDir = new File(".");
 
     /**
      * The user name used for authentication. Defaults to the
      * current user name.
      */
     public String user = System.getProperty("user.name");
 
     /**
      * The command source text, as logged and/or visible for
      * introspection.
      */
     public String source = "command-line [" + user + "]";
 
     /**
      * The post command execution delay in seconds. Default to
      * zero (0).
      */
     public int delay = 0;
 
     /**
      * The command trace flag. Defaults to false.
      */
     public boolean trace = false;
 
     /**
      * Runs a single command.
      *
      * @param params         the procedure name and arguments
      *
      * @throws SecurityException if the user couldn't authenticate
      */
     public void runSingle(String[] params) throws SecurityException {
         ApplicationContext  ctx;
 
         ctx = ApplicationContext.init(appDir, localDir, true);
         SecurityContext.auth(user);
         exec(ctx, new LinkedList(Arrays.asList(params)));
         ApplicationContext.destroy();
     }
 
     /**
      * Runs the commands specified by the prefix and lines read from
      * standard input.
      *
      * @param prefix         the procedure name and argument prefixes
      *
      * @throws SecurityException if the user couldn't authenticate
      * @throws IOException if the input stream couldn't be read
      */
     public void runStdin(String[] prefix)
     throws SecurityException, IOException {
 
         BufferedReader  reader;
 
         reader = new BufferedReader(new InputStreamReader(System.in));
         execStream(prefix, reader, 0);
     }
 
     /**
      * Runs the commands specified by the prefix and lines read from
      * a file.
      *
      * @param prefix         the procedure name and argument prefixes
      * @param file           the file to read
      *
      * @throws SecurityException if the user couldn't authenticate
      * @throws FileNotFoundException if the file couldn't be opened
      * @throws IOException if the input stream couldn't be read
      */
     public void runFile(String[] prefix, File file)
     throws SecurityException, FileNotFoundException, IOException {
 
         BufferedReader  reader;
         int             lines = 0;
 
         reader = new BufferedReader(new FileReader(file));
         while (reader.readLine() != null) {
             lines++;
         }
         reader.close();
         reader = new BufferedReader(new FileReader(file));
         execStream(prefix, reader, lines);
     }
 
     /**
      * Executes a single procedure call.
      *
      * @param ctx            the application context
      * @param params         the procedure name and arguments
      */
     private void exec(ApplicationContext ctx, LinkedList params) {
         String        name;
         String[]      args;
         StringBuffer  traceBuffer = (trace ? new StringBuffer() : null);
         Object        res;
 
         try {
             name = (String) params.removeFirst();
             args = (String[]) params.toArray(new String[params.size()]);
             res = ctx.execute(name, args, source, traceBuffer);
             System.out.println(JsSerializer.serialize(res));
         } catch (ProcedureException e) {
             System.err.println("ERROR: " + e.getMessage());
         }
         if (traceBuffer != null) {
             System.out.println();
             System.out.print(traceBuffer.toString());
         }
     }
 
     /**
      * Executes a stream of procedure calls.
      *
      * @param prefix         the procedure name and argument prefixes
      * @param reader         the input stream to process
      * @param lines          the number of lines in the file
      *
      * @throws IOException if the input stream couldn't be read
      */
     private void execStream(String[] prefix, BufferedReader reader, int lines)
     throws IOException {
 
         ApplicationContext  ctx;
         String              line;
         int                 pos = 0;
         long                startTime;
         Date                doneTime = null;
         double              d;
         LinkedList          params;
 
         ctx = ApplicationContext.init(appDir, localDir, true);
         SecurityContext.auth(user);
         startTime = System.currentTimeMillis();
         while ((line = reader.readLine()) != null) {
             pos++;
             System.out.print("Processing command " + pos);
             if (lines > 0) {
                 System.out.print(" of " + lines);
             }
             if (doneTime != null) {
                 System.out.print(", until " + doneTime);
             }
             System.out.println(":");
             if (line.trim().length() <= 0) {
                 System.out.println("    <empty, skipped>");
             } else {
                 params = new LinkedList(Arrays.asList(prefix));
                 params.addAll(Arrays.asList(line.split("\\s+")));
                 System.out.println("  " + toString(params));
                 System.out.print("  ==> ");
                 exec(ctx, params);
                 System.out.println();
                 if (delay > 0) {
                     try {
                         Thread.sleep(delay * 1000);
                     } catch (InterruptedException e) {
                         // Nothing to do here
                     }
                 }
             }
             if (lines > 0 && pos % 10 == 0) {
                 d = System.currentTimeMillis() - startTime;
                 d = (d / pos) * (lines - pos);
                 doneTime = new Date(System.currentTimeMillis() + (long) d);
             }
         }
         reader.close();
         ApplicationContext.destroy();
     }
 
     /**
      * Returns a string representation of a procedure parameter list.
      *
      * @param params         the procedure parameters
      *
      * @return the string representation
      */
     private String toString(LinkedList params) {
         StringBuffer  buffer = new StringBuffer();
         Iterator      iter = params.iterator();
 
         while (iter.hasNext()) {
             if (buffer.length() > 0) {
                 buffer.append(" ");
             }
             buffer.append(iter.next());
         }
         return buffer.toString();
     }
 }
