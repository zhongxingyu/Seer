 package org.deca.decabotz;
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import org.deca.decabotz.commands.ExtendRearLift;
 import org.deca.decabotz.commands.RetractRearLift;
 import org.deca.decabotz.commands.ShooterBackward;
 import org.deca.decabotz.commands.ShooterForward;
 import org.deca.decabotz.commands.ShooterStop;
 
 /**
  * This class is the glue that binds the controls on the physical operator
  * interface to the commands and command groups that allow control of the robot.
  */
 public class OI {
 
     Joystick operatorGamepad = new Joystick(RobotMap.operatorGamepadID);
     Joystick gamePad = new Joystick(RobotMap.gamepadID);
     Button rearLiftButton = new JoystickButton(operatorGamepad, RobotMap.rearLiftActivateID);
     Button shooterForwardButton = new JoystickButton(gamePad, RobotMap.shooterForwardID);
     Button shooterBackwardButton = new JoystickButton(gamePad, RobotMap.shooterBackwardID);
    
    // Possibly reverse.
     public double getLeftStick() {
         return gamePad.getRawAxis(RobotMap.leftJoyAxisID);
     }
 
     public double getRightStick() {
         return gamePad.getRawAxis(RobotMap.rightJoyAxisID);
 
     }
 
     public double getOperatorStick() {
         return operatorGamepad.getRawAxis(RobotMap.operatorLeftJoyAxisID);
     }
 
     public double getOperatorThrottle() {
         return operatorGamepad.getRawAxis(RobotMap.operatorRightJoyAxisID);
     }
     
 
     public OI() {
         rearLiftButton.whenPressed(new ExtendRearLift());
         rearLiftButton.whenReleased(new RetractRearLift());
         shooterForwardButton.whenPressed(new ShooterForward());
         shooterForwardButton.whenReleased(new ShooterStop());
         shooterBackwardButton.whenPressed(new ShooterBackward());
         shooterBackwardButton.whenReleased(new ShooterStop());
     }
    
 }
