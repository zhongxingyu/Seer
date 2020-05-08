 package org.buzzrobotics.subsystems;
 
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.Relay;
 import org.buzzrobotics.RobotMap;
 
 /**
  * Floor Light
  * Shows that we are on KEY.
  * @author Peter Polis
  */
 public class FloorLight extends Subsystem {
     Relay floor_light;
     public FloorLight(){
        floor_light = new Relay(RobotMap.FloorLightRelayPort);
     }
     
     public void turnOnLight(){
         floor_light.set(Relay.Value.kForward);
     }
     
     public void turnOffLight(){
        floor_light.set(Relay.Value.kOff);
     }
     
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
     }
 }
