 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.badgerbots.lib;
 
 import edu.wpi.first.wpilibj.*;
 
 /**
  *
  * @author james
  */
 public class PIDMotor implements SpeedController{
 
     private final Encoder encoder;
     private final Jaguar motor;
     private final double p, i, d;
 
     private double lastSpeed;
 
     public PIDMotor(Jaguar jag, Encoder enc, double p, double i, double d) {
         motor = jag;
         encoder = enc;
         this.p = p;
         this.i = 0.0;
         this.d = 0.0;
         lastSpeed = 0.0;
         if (i != 0) {
            throw new UnsupportedOperationException("i is not yet supported");
         }
         if (d != 0) {
            throw new UnsupportedOperationException("d is not yet supported");
         }
     }
     
     private double getMotorOutput(double speed) {
         double expected = speed;
        double error = speed - encoder.getRate();
         double pValue = error * p;
 
        lastSpeed += p;
         return lastSpeed;
     }
 
     public void set(double speed) {
 
         motor.set(getMotorOutput(speed));
     }
 
    @Override
     public double get() {
         return lastSpeed;
     }
 
    @Override
     public void set(double speed, byte syncGroup) {
         motor.set(getMotorOutput(speed), syncGroup);
     }
 
    @Override
     public void disable() {
         motor.disable();
     }
 
    @Override
     public void pidWrite(double output) {
         motor.pidWrite(output);
     }
 }
