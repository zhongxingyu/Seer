 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author robowizards
  */
 public class WizardArmController {
     
    private final double ROTATION_SPEED = .6;
     
     private final Relay armLiftRelay;
     private final Jaguar armRotateController;
     
     public WizardArmController(final int climbArmRelayChannel, final int rotateArmJaguarChannel){
         this.armLiftRelay = new Relay(climbArmRelayChannel);
         this.armRotateController = new Jaguar(rotateArmJaguarChannel);
     }
     
     public void raiseClimbArms(){
         armLiftRelay.set(Relay.Value.kForward);
     }
     
     public void lowerClimbArms(){
         armLiftRelay.set(Relay.Value.kReverse);
     }
     
     public void stopClimbArms(){
         armLiftRelay.set(Relay.Value.kOff);
     }
     
     public void rotateArmsForward(){
         armRotateController.set(ROTATION_SPEED);
        SmartDashboard.putString("Test", "Negative");
     }
     
     public void rotateArmsBackward(){
         armRotateController.set(-ROTATION_SPEED);
        SmartDashboard.putString("Test", "Positive");
     }
     
     public void stopArmRotation(){
         armRotateController.set(0);
     }
     
 }
