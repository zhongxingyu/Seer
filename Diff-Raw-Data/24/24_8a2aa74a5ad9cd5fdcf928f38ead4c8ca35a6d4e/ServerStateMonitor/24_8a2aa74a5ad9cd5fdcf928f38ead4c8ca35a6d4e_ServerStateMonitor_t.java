 public class ServerStateMonitor {
     public final static int
         IDLE = 0,
         MOVIE = 1,
         IDLE_FORCED = 2,
         MOVIE_FORCED = 3;
     private int mode = IDLE;
 
     public synchronized int getMode() {
         return mode;
     }
 
     public synchronized int getModeBlocking() {
         try {
             notify();
             wait();
         } catch (InterruptedException e) { e.printStackTrace(); }
 
         return getMode();
     }
 
     public synchronized void setMode(int newMode) {
         if (mode != newMode) {
            if (newMode == MOVIE) {
                 mode = newMode;
                 try {
                     notify();
                     wait();
                 } catch (InterruptedException e) { e.printStackTrace(); }
            } else if (newMode == IDLE_FORCED || newMode == MOVIE_FORCED) {
                 mode = newMode;
             }
         }
     }
 
     public synchronized void unsetForcedMode() {
         if (mode == IDLE_FORCED) {
             mode = IDLE;
         } else if (mode == MOVIE_FORCED) {
             mode = MOVIE;
         }
     }
 }
