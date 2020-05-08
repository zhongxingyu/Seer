 package vooga.rts.networking.server;
 
 /**
  * Represents a timeStamp that will be sent across the network. Uses the built in system time to
  * calculate time.
  * 
  * @author Henrique Moraes
  * @author David Winegar
  * 
  */
 public class SystemTimeStamp extends TimeStamp {
     private static final long serialVersionUID = -7292452477020372150L;
 
     /**
      * Creates a timestamp object with the current time as the initial time
      */
     public SystemTimeStamp () {
         resetStamp();
     }
 
     /**
      * sets the received message time for this stamp
      * 
      * @return difference in elapsed time since this stamp was created
      */
     @Override
     public long stamp () {
         setFinalTime(System.currentTimeMillis());
         return getDifference();
     }
 
     /**
     * Stamps as initial time plus .
      */
     @Override
     public long stamp (long time) {
        setFinalTime(getInitialTime() + time);
         return getDifference();
     }
 
     /**
      * @return difference in elapsed time since this stamp was created
      *         in milliseconds
      */
     @Override
     public long getDifference () {
         if (getFinalTime() == DEFAULT_VALUE)
             setFinalTime(System.currentTimeMillis());
         return getFinalTime() - getInitialTime();
     }
 
     /**
      * Resets this stamp setting the initial time as the current one
      */
     @Override
     public void resetStamp () {
         setInitialTime(System.currentTimeMillis());
         setFinalTime(DEFAULT_VALUE);
     }
 
 }
