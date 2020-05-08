 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 
 /**
  *
  * @author Arfan
  */
 public class Shooter {
 
     private static Shooter instance;
     private Victor shooter;
     private DigitalInput hopperSensor;
     private Solenoid shooterIn;
     private Solenoid shooterOut;
     private Servo fire;
     private double lastTime = 0.0;
     
     
     private Shooter() {
         shooter = new Victor(Constants.SHOOTER_CHANNEL);
         hopperSensor = new DigitalInput(Constants.HOPPER_SENSOR);
         shooterIn = new Solenoid(Constants.SHOOTER_PLUNGER_IN);
         shooterOut = new Solenoid(Constants.SHOOTER_PLUNGER_OUT);   
         fire = new Servo(Constants.SHOOTER_SERVO_CHANNEL);
     }
     
     public static Shooter getInstance() {
         if (instance == null) {
             instance = new Shooter();
         }
         return instance;
     }
     
     public void setSpeed(double speed) {
         shooter.set(speed);
     }
     
     public void shoot() {
         shooter.set(1);
     }
     
     public void shootReverse() {
         shooter.set(-1);
     }
     
     public void stop() {
         shooter.set(0);
     }
     
     public boolean isHopperFull() {
         return hopperSensor.get();
     }
     
     public void fire() {
         double lastTime = Timer.getFPGATimestamp();
         shooterIn.set(false); 
         shooterOut.set(true);
         try {
             Thread.sleep(400);
         } catch (InterruptedException e) {
             System.err.println(e);
         }
         shooterOut.set(false);
         shooterIn.set(true);
     }
     
     public void manualShooterControl(Gamepad gamepad) {
        if(gamepad.getRightTrigger()) {
            shoot();
        }
        else if(gamepad.getLeftTrigger()) {
             shootReverse();
         }
         else {
            stop();
         }
     }
     
 }
