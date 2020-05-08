 package com.edinarobotics.utils.autonomous.conditions;
 
 import com.edinarobotics.utils.autonomous.LoopCondition;
 
 /**
  * A {@link LoopCondition} that can be used to make something run a fixed
  * number of times. It returns {@code true} while the counter is less than
  * a given value.
  * This class is used by {@link com.edinarobotics.utils.autonomous.ForLoopStep}.
  */
 public class CounterCondition implements LoopCondition{
     private int countTo;
     private int value;
     
     /**
      * Constructs a CounterCondition that will return {@code true} from
      * {@link #get()} for {@code countTo} calls.
      * @param countTo the number of times to return {@code true} from
      * {@link #get()}.
      */
     public CounterCondition(int countTo){
         this.countTo = countTo;
         value = 0;
     }
     
     /**
      * Returns {@code true} if get() has been called fewer times than
      * {@code countTo}.
      */
     public boolean get(){
        return value<countTo;
     }
     
     /**
      * Resets the counter so that this object can be used again.
      */
     public void reset(){
         value = 0;
     }
 }
