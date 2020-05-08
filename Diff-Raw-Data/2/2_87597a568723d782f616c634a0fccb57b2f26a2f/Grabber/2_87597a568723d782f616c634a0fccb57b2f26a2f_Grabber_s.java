 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.stuy.subsystems;
 
 import edu.stuy.commands.grabber.GrabberStop;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.command.Subsystem;
 
 /**
  *
  */
 public class Grabber extends Subsystem {
     private static Grabber instance = null;
     
     private Victor upperMotor;
     private Victor lowerMotor;
     private DigitalInput limitSwitch;
 
     public static Grabber getInstance() {
         if(instance == null) {
             instance = new Grabber();
 
             // Set default command here, like this:
             // instance.setDefaultCommand(new CommandIWantToRun());
             instance.setDefaultCommand(new GrabberStop());
         }
         return instance;
     }
 
     // Initialize your subsystem here
     private Grabber() {
         upperMotor = new Victor(4);
         lowerMotor = new Victor(1);
         limitSwitch = new DigitalInput(11);
     }
     
     public void setMotors(double upperSpeed, double lowerSpeed) {
         upperMotor.set(upperSpeed);
        upperMotor.set(lowerSpeed);
     }
     
     public boolean isTubeAcquired() {
         return !limitSwitch.get();
     }
 }
 
