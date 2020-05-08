 package org.motechproject.ananya.kilkari.functional.test.utils;
 
 public abstract class TimedRunner<T>  {
 
     private int tries;
     private int intervalSleep;
 
     public TimedRunner() {
         this(5, 1000);
     }
     public TimedRunner(int tries, int intervalSleep) {
         this.tries = tries;
         this.intervalSleep = intervalSleep;
     }
     /*
      * Function to run within the timeout. It returns a boolean. If the value is true, the code
      * will break out of the loop immediately else try again within the timeout period.
      */
     protected abstract TimedRunnerResponse<T> run();
 
     public T executeWithTimeout() {
         for (int i = 0; i < tries; i++) {
            TimedRunnerResponse<T> result = run();
            if (result != null) {
                return result.getObj();
            }
             try {
                 Thread.sleep(intervalSleep);
             } catch (InterruptedException e) {
                 throw new RuntimeException("Thread was interrupted.", e);
             }
         }
        return defaultResponse();
     }
 
    protected T defaultResponse() {
        return null;
    }
 }
 
