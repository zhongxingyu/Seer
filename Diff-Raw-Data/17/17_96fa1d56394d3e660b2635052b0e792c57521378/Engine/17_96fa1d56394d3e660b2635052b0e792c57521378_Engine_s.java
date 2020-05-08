 package com.openske.engine;
 
 import java.io.File;
 import java.io.PrintWriter;
 
 import com.openske.drools.DroolsFacade;
 
 /**
  * OpenSKE's Engine
  * 
  */
 public class Engine {
 
     protected DroolsFacade drools;
     protected boolean started;
     protected PrintWriter outputWriter;
     protected long runningTime;
     protected static File currentWorkingDirectory;
 
     public Engine() {
         // Engine Initialization
         outputWriter = new PrintWriter(System.out, true);
         // Drools Initialization
         drools = new DroolsFacade();
         drools.setOutputWriter(outputWriter);
         Engine.currentWorkingDirectory = new File(".");
         // Mark engine as not started yet
         started = false;
     }
 
     public void run() {
         if (this.isStarted()) {
             outputWriter.format("OpenSKE engine is already running");
             return;
         } else {
             outputWriter.format("Running OpenSKE engine...");
             try {
                 // Reset the runningTime
                 this.runningTime = 0L;
                 long startTime = System.currentTimeMillis();
                 // Marking the engine as started from the beginning,
                 // since it's running in it's own thread
                 started = true;
                 drools.initialize();
                 drools.loadRules();
                 drools.loadFacts();
                 drools.startProcesses();
                 drools.fireRules();
                 long endTime = System.currentTimeMillis();
                 this.runningTime = endTime - startTime;
                 outputWriter.format("Engine took %d seconds !", this.getRunningTime());
             } catch (Throwable t) {
                 t.printStackTrace(outputWriter);
                 this.stop();
             }
         }
     }
 
     public void stop() {
         if (this.isStarted()) {
             outputWriter.format("Stopping OpenSKE engine...");
             drools.cleanup();
             started = false;
         }
     }
 
     public boolean isStarted() {
         return started;
     }
 
     public PrintWriter getOutputWriter() {
         return outputWriter;
     }
 
     public void setOutputWriter(PrintWriter outputWriter) {
         this.outputWriter = outputWriter;
         // Change Drools output writer as well
         drools.setOutputWriter(outputWriter);
     }
 
     public long getRunningTime() {
         if(this.isStarted()) {
             return runningTime / 1000L;
         } else {
             return 0L;
         }
     }
 
     public static void main(String[] args) {
         Engine engine = new Engine();
         engine.run();
         engine.stop();
     }
 
     public static File currentWorkingDirectory() {
         return currentWorkingDirectory;
     }
 }
