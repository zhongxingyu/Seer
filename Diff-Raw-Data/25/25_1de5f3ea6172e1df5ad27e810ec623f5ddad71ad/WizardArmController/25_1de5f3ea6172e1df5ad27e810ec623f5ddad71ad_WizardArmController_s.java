 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 
 /**
  *
  * @author robowizards
  */
 public class WizardArmController {
     
    private final double ROTATION_SPEED = .2;
     
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
     }
     
     public void rotateArmsBackward(){
         armRotateController.set(-ROTATION_SPEED);
     }
     
     public void stopArmRotation(){
         armRotateController.set(0);
     }
     
 }
