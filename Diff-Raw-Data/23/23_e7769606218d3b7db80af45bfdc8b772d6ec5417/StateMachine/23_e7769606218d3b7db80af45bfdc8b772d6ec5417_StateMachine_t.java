 package se.tube42.lib.ks;
 
 /**
  * This class represents a state machine, which is a job with states.
  * 
  * NOTE: when you add this class to a JobManager,
  * it will reset state, time countners and all events to zero.
  * @see #onAdd()
  */
 
 public abstract class StateMachine extends Job
 {
     protected int state;
    private int old_state;
     private long time_total, time_state;
     private int events;
     
     public StateMachine()
     {
         init(true);
     }
     
     /**
      * Reset the state machine to its initial state 
      */
     public void init(boolean reset)
     {        
         time_total = time_state = 0;
         state = 0;
        old_state = -1;
         
         if(reset) {
             events = 0;
             reset();
         }
     }
     
     /**
      * Called when inserted into the managers queue.
      * It will cause the state machine to reset to its initial state
      */    
     public final void onAdd()
     {
         init(false);
     }
     
     /**
      * @return state of the machine
      */
     public int getState()
     {
         return state;
     }
         
     public final long execute(long dt_error )
     {
         if(state != old_state) {
            old_state = state;
            time_state = 0;            
         }
                 
        final long dt = dt_error + time_frame;
        time_state += dt;
        time_total += dt;
                
        return update(time_total, time_state, dt);
     }
     
     /**
      * Fire an event, to be handled by the state machine 
      * at some later point (if ever)
      * @param event the event to fire
      */
     
     public final void fire(int event)
     {
         events |= 1 << event;
     }
     
     /**
      * Check if an event is fired. 
      * Will clear the event after checking it!
      * @return true if the event has been fired
      * @param event the event to check
      */
     protected final boolean check(int event)
     {
         int x = 1 << event;        
         
         if( (events & x) != 0) {
             events &= ~x;
             return true;
         }
         return false;
     }
     
     
     /**
      * update the state machine, with the following time steps 
      * (if time is needed)
      * @param time_all total time for this machine
      * @param time_state total time in this state
      * @param dt time for this frame
      * @return time to next update, zero or negative to end execution
      */
     public abstract long update(long time_all, long time_state, long dt);        
 }
