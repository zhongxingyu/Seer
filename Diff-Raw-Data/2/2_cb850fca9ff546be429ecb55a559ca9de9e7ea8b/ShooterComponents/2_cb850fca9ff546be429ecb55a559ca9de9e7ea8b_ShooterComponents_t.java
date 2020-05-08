 package com.edinarobotics.zephyr.parts;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  *The wrapper for the shooter components, contains the 2 jaguars driving the shooter
  * along with the rotating jaguar and the piston.
  */
 public class ShooterComponents {
     private Jaguar shooterLeftJaguar;
     private Jaguar shooterRightJaguar;
     private Jaguar shooterRotator;
     private Relay ballLoadPiston;
     /*
      * Constructs shooterLeftJaguar, shooterRightJaguar, shooterRotator and ballLoadPiston
      * with leftJaguar, rightJaguar, rotator and piston respectively.
      */
     public ShooterComponents(int leftJaguar, int rightJaguar, int rotator, int piston){
         shooterLeftJaguar = new Jaguar(leftJaguar);
         shooterRightJaguar = new Jaguar(rightJaguar);
         shooterRotator = new Jaguar(rotator);
         ballLoadPiston = new Relay(piston);
     }
     /*
      * sets the shooterLeftJaguar to speed and shooterRightJaguar to -speed
      */
     public void setSpeed(double speed){
         shooterLeftJaguar.set(-speed);
         shooterRightJaguar.set(speed);
     }
     /*
      * Sets the rotator to speed
      */
     public void rotate(double speed){
         shooterRotator.set(speed);
     }
     /*
      * Sets the piston up if position is true, else it lowers it.
      */
     public void firePiston(boolean position){
        ballLoadPiston.set((position ? Relay.Value.kForward :Relay.Value.kReverse));
     }
 }
