 package com.openske.engine;
 
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
 
     public Engine() {
         // Engine Initialization
        outputWriter = new PrintWriter(System.out, true);
         // Drools Initialization
         drools = new DroolsFacade();
         drools.setOutputWriter(outputWriter);
         // Mark engine as not started yet
         started = false;
     }
 
     public void run() {
         if(this.isStarted()) {
             outputWriter.format("OpenSKE engine is already running");
             return;
         } else {
             try {
                 // Marking the engine as started from the beginning,
                 // since it's running in it's own thread
                 started = true;
                 drools.loadRules();
                 drools.loadFacts();
                 drools.fireAllRules();
             }
             catch(Throwable t) {
                 t.printStackTrace(outputWriter);
             }
         }
     }
     
     public void stop() {
         if(this.isStarted()) {
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
    
    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.run();
        engine.stop();
    }
 }
