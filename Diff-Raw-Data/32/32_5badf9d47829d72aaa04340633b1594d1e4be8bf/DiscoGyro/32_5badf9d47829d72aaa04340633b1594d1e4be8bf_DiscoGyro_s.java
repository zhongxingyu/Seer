 package org.discobots.aerialassist.utils;
 
 import edu.wpi.first.wpilibj.Gyro;
 
 /**
  * Extends gyro to allow us to reset the angle to what ever value we want.
  * @author JAG
  */
 public class DiscoGyro extends Gyro{
 
     private double m_resetAngleValue =0;
 
     public DiscoGyro(int channel){
         super(channel);
     }
 
     /**
      *
      * @return the value of the gyro plus our reset angle positions
      */
     public double getAngle(){
        double angle=super.getAngle();
        angle%=360;
        if (angle>180)
            angle=-360+angle;
        if (angle<-180)
            angle=360+angle;
//        return m_resetAngleValue + super.getAngle();
 //        return super.getAngle() % 360;
        return angle;
     }
 
     public void setAngle(double angle){
         m_resetAngleValue = angle;
         super.reset();
     }
 
     /**
      * Reset by setting th angle to zero and reseting the gyro accumulator
      */
     public void reset(){
         reset(0);
     }
 
     /**
      * Reset the gyro and set to what you want. Is just another name for setAngle
      * @param angle
      */
     public void reset(double angle){
         setAngle(angle);
     }
     /**
      * Return the angle with our adjustments
      * @return
      */
     public double PIDGet(){
         return getAngle();
     }
 }
