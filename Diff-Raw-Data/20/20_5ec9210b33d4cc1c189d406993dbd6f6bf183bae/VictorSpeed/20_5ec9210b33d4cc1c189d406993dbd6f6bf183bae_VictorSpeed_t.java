 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.speed;
 
 import edu.wpi.first.wpilibj.*;
 /**
  *
  * @author admin
  */
 public class VictorSpeed implements JoeSpeed {
 
     public double getRPM() {
        double circumference = 2 * Math.PI * WHEEL_RADIUS;
        int seconds = 60;
        return (seconds * encoder.getRate()/(circumference));  //Converted from Distance/Second to RPM
     }
 
     public void setRPM(double rpm) {
        set(rpm);
     }
     Encoder encoder;
     Victor victor;
     PIDController controller;
     double lastTime;
     
     double PROPORTIONAL     = 0.00365;
     double INTEGRAL         = 0.00;
     double DERIVATIVE       = 0.000012;
     
     /**
      * Make an actual speed controller complete with a Victor, Encoder and PIDController
      * @param victorChannel The PWM channel for the victor.
      * @param encoderAChannel Digital in for the encoder.
      * @param encoderBChannel Input for the other encoder.
      * @param reverse Not used.  Was for reversing encoder direction.
      */
     public VictorSpeed(int victorChannel, int encoderAChannel, int encoderBChannel) {
         victor = new Victor(victorChannel);
 
         encoder = new Encoder(encoderAChannel, encoderBChannel, false, CounterBase.EncodingType.k4X);
         encoder.setDistancePerPulse(ENCODER_RPM_PER_PULSE);
         encoder.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate); // use e.getRate() for feedback
         encoder.start();
 
         controller = new PIDController(PROPORTIONAL, INTEGRAL, DERIVATIVE, encoder, victor);
         controller.setOutputRange(-1, 1);
         controller.enable();
     }
 
     
     /**
      * Set the PWM value.
      *
      * The PWM value is set using a range of -1.0 to 1.0, appropriately
      * scaling the value for the FPGA.
      *
      * @param output The speed value between -1.0 and 1.0 to set.
      */
     public void pidWrite(double output) {
         victor.set(output);
     }
 
     /**
      * Set a wheel's speed setpoint.
      * @param speedRPM The desired wheel speed in RPM (revolutions per minute).
      */
     public void set(double speedRPM) {
         controller.setSetpoint(speedRPM);
     }
 
     public double get() {
         return victor.get();
     }
 
     public void disable() {
         victor.disable();
     }
 }
