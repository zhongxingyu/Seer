 package de.skuzzle.polly.process;
 
 
 /**
  * This {@link ProcessWatcher} kills the observed process on timeout.
  * 
  * @author Simon
  */
 public class KillingProcessWatcher extends ProcessWatcher {
     
     private boolean killOnError;
     
     
     
     /**
      * Creates a new KillingProcessWatcher with a timeout.
      * 
      * @param timeout The timeout to wait before the process is destroyed.
     * @param killOnError If set to <code>true</code>, the watched process is killed
      *      if an error occurred during waiting (this can happen before time ran out).
      * @throws IllegalArgumentException if timeout is < 0.
      */
     public KillingProcessWatcher(int timeout, boolean killOnError) {
         super(timeout);
         if (timeout < 0) {
             throw new IllegalArgumentException("timeout must be positive");
         }
         this.killOnError = killOnError;
     }
     
     
     
     @Override
     public void processExit(ProcessWrapper proc, int exitType) {
         if (exitType == EXIT_TYPE_TIMEOUT) {
             proc.getProcess().destroy();
         } else if (this.killOnError && exitType == EXIT_TYPE_ERROR) {
             proc.getProcess().destroy();
         }
     }
 }
