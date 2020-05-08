 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.deca.decabotz.subsystems;
 
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.deca.decabotz.RobotMap;
 import org.deca.decabotz.commands.RetractRearLift;
 
 /**
  *
  * @author Sciencelab
  */
 public class RearLifter extends Subsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     Solenoid solenoidOneExtend;
     Solenoid solenoidOneRetract;
     Solenoid solenoidTwoExtend;
     Solenoid solenoidTwoRetract;
 
     public RearLifter() {
         solenoidOneExtend = new Solenoid(RobotMap.solenoidOneExtendID);
         solenoidOneRetract = new Solenoid(RobotMap.solenoidOneRetractID);
         solenoidTwoExtend = new Solenoid(RobotMap.solenoidTwoExtendID);
         solenoidTwoRetract = new Solenoid(RobotMap.solenoidTwoRetractID);
 
 
     }
 
     public void initDefaultCommand() {
        setDefaultCommand(new RetractRearLift());
 
     }
 
     public void LiftExtend() {
         solenoidOneExtend.set(true);
         solenoidTwoExtend.set(true);
         solenoidOneRetract.set(false);
         solenoidTwoRetract.set(false);
     }
 
     public void LiftRetract() {
         solenoidOneExtend.set(false);
         solenoidTwoExtend.set(false);
         solenoidOneRetract.set(true);
         solenoidTwoRetract.set(true);
     }
 }
