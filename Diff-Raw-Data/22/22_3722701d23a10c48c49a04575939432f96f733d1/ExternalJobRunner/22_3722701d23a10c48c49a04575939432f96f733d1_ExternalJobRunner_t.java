 /* File:        $Id$
  * Revision:    $Revision$
  * Author:      $Author$
  * Date:        $Date$
  *
  * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package dk.statsbiblioteket.doms.radiotv.extractor;
 
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ExternalProcessTimedOutException;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.Util;
 import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor.FlashTranscoderProcessor;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Field;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class ExternalJobRunner {
     private final StringBuffer standard_out = new StringBuffer();
     private final StringBuffer standard_err = new StringBuffer();
     private int exit_code;
     private static final Logger log = Logger.getLogger(ExternalJobRunner.class);
    private String logString = "";
 
     private long timeout = 0L;
 
     private long harvesterThreadTimeout = 60*3600*1000L;
 
     public long getHarvesterThreadTimeout() {
         return harvesterThreadTimeout;
     }
 
     public void setHarvesterThreadTimeout(long harvesterThreadTimeout) {
         this.harvesterThreadTimeout = harvesterThreadTimeout;
     }
 
     public ExternalJobRunner(final String... command) throws IOException, InterruptedException, ExternalProcessTimedOutException {
          this(0L, command);
     }
 
 
 
 
     /**
      * Runs an external command, blocking until the external processRecursively ends after which
      * the output and errors can be read.
      * @param command
      * @throws java.io.IOException
      * @throws InterruptedException
      */
     public ExternalJobRunner(long timeout, final String... command) throws IOException, InterruptedException, ExternalProcessTimedOutException {
         for (String commandS:command) {
            if (commandS!= null && !commandS.equals("null")) logString += commandS + " ";
         }
 
         if (timeout != 0L) {
             this.timeout = timeout;
         }
 
         final Process p;
         if (command.length == 1) {
              p = Runtime.getRuntime().exec(command[0]);
         } else {
              p = Runtime.getRuntime().exec(command);
         }
 
         class StreamHarvester implements Runnable {
             public static final String OUT = "standard_out";
             public static final String ERR = "standard_err";
             private StringBuffer buffer;
             private InputStream stream;
             private String stream_type;
 
             public boolean started = false;
 
             public StreamHarvester (String stream_type) {
                 this.stream_type = stream_type;
                 if (stream_type.equals(OUT)) {
                     buffer = standard_out;
                     stream = p.getInputStream();
                 } else if (stream_type.equals(ERR)) {
                     buffer = standard_err;
                     stream = p.getErrorStream();
                 } else {
                     throw new RuntimeException("Argument to " +
                             "StreamHarvester constructor '"+stream_type+"' is not recognised");
                 }
             }
 
             public void run() {
                 log.debug("Starting harvesting thread for '" + stream_type + "' for '" + logString + "'");
                 synchronized(buffer) {
                     started = true;
                     synchronized (this) {
                         notifyAll();
                     }
                     String line;
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                     try {
                         while ((line = reader.readLine()) != null) {
                             buffer.append(line+"\n");
                         }
                     } catch (IOException e) {
                         log.error("Harvesting error for '"  + stream_type + "' for '" + logString + "' (has process timed out and been killed?)", e);
                         p.destroy();
                         try {
                             killUnixProcess(p);
                         } catch (Exception e1) {
                             log.warn(e);
                         }
                     } finally {
                         try {
                             log.debug("Closing stream for '" + logString + "'");
                             stream.close();
                         } catch (IOException e) {
                             log.error("Error closing an InputStream for '" + logString + "'", e);
                         }
                     }
                 }
             }
 
         }
 
         StreamHarvester out_harvester = new StreamHarvester(StreamHarvester.OUT);
         StreamHarvester err_harvester = new StreamHarvester(StreamHarvester.ERR);
         synchronized(out_harvester) {
             (new Thread(out_harvester)).start();
             if (!out_harvester.started) {
                 try {
                     log.debug("Waiting for stdout harvester for: '" + logString + "'");
                     out_harvester.wait(harvesterThreadTimeout);
                     log.debug("Finished waiting for stdout harvester for: '" + logString + "'");
                 } catch (InterruptedException e) {
                     log.warn("Failed to start harvester thread.", e);
                     killInterruptedProcess(p,e);
                     return;
                 }
             }
         }
         synchronized (err_harvester) {
             (new Thread(err_harvester)).start();            
             if (!err_harvester.started){
                 try {
                     log.debug("Waiting for stderr harvester for: '" + logString + "'");
                     err_harvester.wait(harvesterThreadTimeout);
                     log.debug("Finished waiting for stderr harvester for: '" + logString + "'");
                 } catch (InterruptedException e) {
                     log.warn("Failed to start harvester thread.", e);
                     killInterruptedProcess(p,e);
                     return;
                 }
             }
         }
         log.debug("Waiting for '" + logString + "'");
         Timer timer = new Timer();        
         if (this.timeout != 0L) {
             timer.schedule(new InterruptScheduler(Thread.currentThread()), this.timeout);
         }
         try {
             p.waitFor();
         } catch (InterruptedException e) {
             killInterruptedProcess(p, e);
             throw new ExternalProcessTimedOutException();
         } finally {
             try {
                 timer.cancel();
             } catch (Exception e) {
                 log.warn(e);
             }
             try {
                 p.getOutputStream().close();
             } catch (IOException e) {
                 log.warn(e);
             }
         }
         log.debug("Finished waiting for '" + logString + "'");
         exit_code = p.exitValue();
     }
 
     private void killInterruptedProcess(Process p, InterruptedException e) {
         log.error("Process '" + logString + "' timed out. Destroying.");
         p.destroy();
         try {
             killUnixProcess(p);
         } catch (Exception e1) {
             log.warn(e);
         }
     }
 
 
     public String getOutput() {
         synchronized(standard_out) {
             return standard_out.toString();
         }
     }
 
     public String getError() {
         synchronized(standard_err) {
             return standard_err.toString();
         }
     }
 
     public int getExitValue() {
         return exit_code;
     }
 
     public static void runClipperCommand(long timeout, String clipperCommand) throws ProcessorException, ExternalProcessTimedOutException {
         log.info("Executing '" + clipperCommand + "'");
         try {
             ExternalJobRunner runner = new ExternalJobRunner(timeout, new String[]{"bash", "-c", clipperCommand});
             if (runner.getExitValue() != 0) {
                 log.warn("Command '" + clipperCommand + "' returned with exit value '" + runner.getExitValue() + "'");
                 log.warn("Standard out:\n" + runner.getOutput());
                log.warn("Standard err:\n" + runner.getError());
             } else {
                 log.info("Command '" + clipperCommand + "' returned with exit value '" + runner.getExitValue() + "'");                
             }
         } catch (IOException e) {
             throw new ProcessorException(e);
         } catch (InterruptedException e) {
             throw new ProcessorException(e);
         }
     }
 
     public static void runClipperCommand(String clipperCommand) throws ProcessorException, ExternalProcessTimedOutException {
         runClipperCommand(0L, clipperCommand);
     }
 
 
     public static int getUnixPID(Process process) throws Exception
     {
         System.out.println(process.getClass().getName());
         if (process.getClass().getName().equals("java.lang.UNIXProcess"))
         {
             Class cl = process.getClass();
             Field field = cl.getDeclaredField("pid");
             field.setAccessible(true);
             Object pidObject = field.get(process);
             return (Integer) pidObject;
         } else
         {
             throw new IllegalArgumentException("Needs to be a UNIXProcess");
         }
     }
 
     public static int killUnixProcess(Process process) throws Exception
     {
         int pid = getUnixPID(process);
         return Runtime.getRuntime().exec("kill " + pid).waitFor();
     }
 
     private class InterruptScheduler extends TimerTask
     {
         Thread target = null;
 
         public InterruptScheduler(Thread target)
         {
             this.target = target;
         }
 
         @Override
         public void run()
         {
             target.interrupt();
         }
 
     }
 
 }
 
 
