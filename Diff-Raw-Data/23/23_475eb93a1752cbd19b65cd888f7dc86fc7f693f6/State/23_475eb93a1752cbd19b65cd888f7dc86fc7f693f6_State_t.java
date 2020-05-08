 package state_machine;
 
 import core.StateEstimator;
 
 public abstract class State {
     protected StateMachine sm;
     protected StateEstimator se;
     
     long startTime;
 	protected long tooLong;
 	protected State prev;
     
     public State() {
         startTime = System.currentTimeMillis();
         sm = StateMachine.getInstance();
         se = StateEstimator.getInstance();
     }
     
     public State step() {
         State next = this.transition();
        prev = this;
         if (System.currentTimeMillis() - startTime >= this.tooLong){
         	return new TimeoutState();
         }
         else {
         	next.run();
         }
         
         return next;
         
     }
     
     protected abstract State transition();
     protected abstract void run();
 }
