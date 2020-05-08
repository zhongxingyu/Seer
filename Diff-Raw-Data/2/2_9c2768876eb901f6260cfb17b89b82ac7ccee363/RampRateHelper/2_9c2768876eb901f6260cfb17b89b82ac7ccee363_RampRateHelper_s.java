 package com.edinarobotics.utils.rate;
 
 import edu.wpi.first.wpilibj.Timer;
 
 /**
  * This class implements a ramp rate limiter.
  * It allows the specification of a maximum change in values per second, a
  * target value, and returns the maximum change for a given period of time.
  */
 public class RampRateHelper {
     private double valuesPerSecond;
     private double target;
     private boolean rampUp;
     private boolean rampDown;
     private double previousTime;
     
     /**
      * Constructs a new RampRateHelper with a given rate limit, which will
      * rate limit the value up, down or both.
      * @param valuesPerSecond The maximum change in the value allowed per second.
      * @param rampUp Indicates whether or not to limit the value as it
      * increases.
      * @param rampDown Indicates whether or not to limit the value as it
      * decreases.
      */
     public RampRateHelper(double valuesPerSecond, boolean rampUp, boolean rampDown){
         this.valuesPerSecond = valuesPerSecond;
         this.rampUp = rampUp;
         this.rampDown = rampDown;
         previousTime = Timer.getFPGATimestamp();
     }
     
     /**
      * Changes the rate limit of this RampRateHelper.
      * @param valuesPerSecond The new maximum allowable change in the value
      * allowed per second.
      */
     public void setRampRate(double valuesPerSecond){
         this.valuesPerSecond = valuesPerSecond;
     }
     
     /**
      * Returns the rate limit of this RampRateHelper.
      * @return The current rate limit of this RampRateHelper.
      */
     public double getRampRate(){
         return valuesPerSecond;
     }
     
     /**
      * Sets whether this RampRateHelper should limit the change in the value as
      * it increases.
      * @param rampUp The RampRateHelper will limit the increase in the value
      * if {@code true}, no limit if {@code false}.
      */
     public void setRampUp(boolean rampUp){
         this.rampUp = rampUp;
     }
     
     /**
      * Sets whether this RampRateHelper should limit the change in the value as
      * it decreases.
      * @param rampUp The RampRateHelper will limit the decrease in the value
      * if {@code true}, no limit if {@code false}.
      */
     public void setRampDown(boolean rampDown){
         this.rampDown = rampDown;
     }
     
     /**
      * Indicates whether this RampRateHelper will limit the change in the value
      * as it increases.
      * @return {@code true} if this RampRateHelper will limit the increase of
      * the value, {@code false} otherwise.
      */
     public boolean getRampUp(){
         return rampUp;
     }
     
     /**
      * Indicates whether this RampRateHelper will limit the change in the value
      * as it decreases.
      * @return {@code true} if this RampRateHelper will limit the decrease of
      * the value, {@code false} otherwise.
      */
     public boolean getRampDown(){
         return rampDown;
     }
     
     /**
      * Sets the current target value of this RampRateHelper.
      * The RampRateHelper will bring the value closer to this target
      * at the rate specified by {@link #setRampRate(double)}.
      * @param target The new target value for this RampRateHelper.
      */
     public void setTarget(double target){
         this.target = target;
     }
     
     /**
      * Returns the current target value of this RampRateHelper.
      * @return The current target value of this RampRateHelper.
      * @see #setTarget(double) 
      */
     public double getTarget(){
         return this.target;
     }
     
     /**
     * Returns the suggested <em>change<em> in the value in order to follow
      * the defined ramp rate.<br/>
      * The value defined by this method will ensure that the value will
      * reach the given target value in the shortest allowable time, as defined
      * by {@link #setRampUp(boolean)}, {@link #setRampDown(boolean)}, and
      * {@link #setRampRate(double)}.
      * @param currentValue The current value that is to be changed at the given
      * ramp rate.
      * @return The suggested change of the value in order to follow the defined
      * rate.
      */
     public double getChange(double currentValue){
         double timeDelta = Timer.getFPGATimestamp() - previousTime;
         previousTime = Timer.getFPGATimestamp();
         double difference = Math.abs(getTarget() - currentValue);
         byte direction = signum(Math.abs(target) - Math.abs(currentValue));
         byte returnSign = signum(getTarget() - currentValue);
         double maxChange = getRampRate() * timeDelta;
         if((direction > 0 && getRampUp()) || (direction < 0 && getRampDown())){
             if(difference < maxChange){
                 return returnSign * difference;
             }
             return returnSign * maxChange;
         }
         return returnSign * difference;
     }
     
     private byte signum(double value){
         if (value < 0) {
             return -1;
         } else if (value > 0) {
             return 1;
         }
         return 0;
     }
 }
