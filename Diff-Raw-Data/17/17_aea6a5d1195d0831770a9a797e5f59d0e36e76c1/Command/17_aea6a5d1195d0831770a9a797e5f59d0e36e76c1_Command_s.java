 package com.orbious.util;
 
 /**
  * A singleton instance for applications using shutdown hooks.
  * Example Usage: e.g. from within a main thread:
  *
  *     Runtime.getRuntime().addShutdownHook(new Thread() {
       public void run() {
         logger.warn("Shutdown hook called");
           Command.instance().shutdown(true);
           while ( !Command.instance().canExit() ) {
             try {
               sleep(10000);
             } catch ( InterruptedException ignored ) { }
           }
       }
     });;
  *
  */
 public class Command {
 
   private static Command instance;
 
   private boolean shutdown = false;
  private boolean can_exit = false;
 
   private Command() { }
 
   public static Command instance() {
     if ( instance == null ) {
       instance = new Command();
     }
 
     return instance;
   }
 
   public boolean shutdown() {
     return shutdown;
   }
 
   public void shutdown(boolean shutdown) {
     this.shutdown = shutdown;
   }
 
   public boolean canExit() {
     return can_exit;
   }
 
   public void canExit(boolean canExit) {
     can_exit = canExit;
   }
 
 }
 
