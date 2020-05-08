 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Talon;
 
 /**
  *
  * @author Arfan
  */
 public class Climber {
     
     private static Climber instance;
     private Talon wench;
     private Solenoid deployer;
     
     public static Climber getInstance() {
         if (instance == null)
             instance = new Climber();
         return instance;
     }
     
     private Climber() {
         wench = new Talon(Constants.WENCH_CHANNEL);
         deployer = new Solenoid(Constants.CLIMBER_SOLENOID_CHANNEL);
        
     }
     
     public void forwardWench() {
         wench.set(1);
     }
     
     public void stopWench() {
         wench.set(0);
     }
     
     public void reverseWench() {
         wench.set(-1);
     }
     
     public void setWench(double val) {
         wench.set(val);
     }
     
     public void deploy() {
         deployer.set(true);
     }
     
     public void withdraw() {
        deployer.set(false);
     }
     
 }
