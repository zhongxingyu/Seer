 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.templates.commands.RunClimber;
 import edu.wpi.first.wpilibj.templates.variablestores.VstM;
 
 /**
  *
  * @author Robotics
  */
 public class Climber extends Subsystem {
 
     //objects needed by climber
     private DigitalInput lowerLimit = new DigitalInput(VstM.Relays.LOWER_LIMIT_SWITCH);
     private DigitalInput upperLimit = new DigitalInput(VstM.Relays.UPPER_LIMIT_SWITCH);
     private Jaguar climber = new Jaguar(VstM.PWM.CLIMBER_MOTOR);
     
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
     public void initDefaultCommand() {
         setDefaultCommand(new RunClimber());
     }
     
     public void runLadder(double speed) {
         climber.set(speed);
     }
 
     public void stop() {
         climber.stopMotor();
     }
 
 
     public boolean isAtLowerLimit() {
         /* 
         * this returns if it's at the lower limit, assuming
          * that the switch is normally open.
         */
         return lowerLimit.get(); //TODO: invert if normally closed
     }
     
     public boolean isAtUpperLimit() {
         /* 
         * this returns if it's at the upper limit, assuming
          * that the switch is normally open.
         */
         return upperLimit.get(); //TODO: invert if normally closed
     }
     
     
 }
