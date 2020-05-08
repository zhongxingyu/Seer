 package org.buzzrobotics;
 
 import org.buzzrobotics.Buzz;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.buttons.NetworkButton;
 import edu.wpi.first.wpilibj.buttons.AnalogIOButton;
 import edu.wpi.first.wpilibj.buttons.DigitalIOButton;
 import edu.wpi.first.wpilibj.buttons.InternalButton;
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.DriverStation;
 import edu.wpi.first.wpilibj.buttons.Button;
 import edu.wpi.first.wpilibj.command.Command;
 import org.buzzrobotics.commands.*;
 import org.buzzrobotics.subsystems.Conveyor;
 import edu.wpi.first.wpilibj.DriverStation;
 import org.buzzrobotics.commandgroups.LoadBalls_Door;
 import org.buzzrobotics.commandgroups.LoadBalls_Door_Done;
 import org.buzzrobotics.utils.SystemsCheck;
 /*
  * Operator Interface
  * @author PETER POLIS aka P^2
  * @author Zach Boyer
  * @author Kyle Deane
  * @author Buzz Robotics 2012 Programming Team
  */
 public class OI {
     
     /*
      * Joystick 1 Button Definition
      * Left Joystick
      * @author Kyle Deane
      */
 
     DriverStation driver = DriverStation.getInstance();
     
    
    //removed public double delay and mode from here. moved it to bottom.
     
     public int delay;
     public int mode;
     
 
     Joystick leftJoy = new Joystick(2);
     Button lbutton1 = new JoystickButton(leftJoy, 1),
            lbutton2 = new JoystickButton(leftJoy, 2),
            lbutton3 = new JoystickButton(leftJoy, 3),
            lbutton4 = new JoystickButton(leftJoy, 4),
            lbutton5 = new JoystickButton(leftJoy, 5),
            lbutton6 = new JoystickButton(leftJoy, 6),
            lbutton7 = new JoystickButton(leftJoy, 7),
            lbutton8 = new JoystickButton(leftJoy, 8),
            lbutton9 = new JoystickButton(leftJoy, 9),
            lbutton10 = new JoystickButton(leftJoy, 10),
            lbutton11 = new JoystickButton(leftJoy, 11);
    
     /*
      * Joystick 2 Button Definitions
      * Right Joystick
      * @author Kyle Deane
      */
     Joystick rightJoy = new Joystick(1);
     Button rbutton1 = new JoystickButton(rightJoy, 1),
            rbutton2 = new JoystickButton(rightJoy, 2),
            rbutton3 = new JoystickButton(rightJoy, 3),
            rbutton4 = new JoystickButton(rightJoy, 4),
            rbutton5 = new JoystickButton(rightJoy, 5),
            rbutton6 = new JoystickButton(rightJoy, 6),
            rbutton7 = new JoystickButton(rightJoy, 7),
            rbutton8 = new JoystickButton(rightJoy, 8),
            rbutton9 = new JoystickButton(rightJoy, 9),
            rbutton10 = new JoystickButton(rightJoy, 10),
            rbutton11 = new JoystickButton(rightJoy, 11),
            rbutton12 = new JoystickButton(rightJoy, 12);
    InternalButton rightHatUp = new InternalButton();
    InternalButton rightHatDown = new InternalButton();
    InternalButton rightHatLeft = new InternalButton();
    InternalButton rightHatRight = new InternalButton();
     
     /**
      * Bind the press of each button to a specific command or command group.
      * 
      * Right Button 1 (Pressed): Shift Up
      * Right Button 1 (Released): Shift Down
      * Right Button 4: Light On
      * Right Button 5: Light Off
      * @author Kyle Deane
      */
     public OI() {
         
        System.out.println(driver.getAlliance());
         if (getRightHatY() == 1.0){rightHatUp.setPressed(true);}else{rightHatUp.setPressed(false);}
         if (getRightHatY() == -1.0){rightHatDown.setPressed(true);}else{rightHatDown.setPressed(false);}
         if (getRightHatX() == 1.0){rightHatRight.setPressed(true);}else{rightHatRight.setPressed(false);}
         if (getRightHatX() == -1.0){rightHatLeft.setPressed(true);}else{rightHatLeft.setPressed(false);}
         
         
         //LEFT Z - does nothing right now.
         
 /****************************
  * Right
  ****************************/
         
         rbutton2.whenPressed(new Rollers_On(-1));
         rbutton2.whenReleased(new Rollers_Off());
         
         rbutton3.whenPressed(new BridgeArm_Move(-1));
         rbutton3.whenReleased(new BridgeArm_Move(0));
         rbutton4.whenPressed(new BridgeArm_Move(1));
         rbutton4.whenReleased(new BridgeArm_Move(0));
         
         rbutton5.whenPressed(new Shifter_Toggle());
         rbutton6.whenPressed(new Brake_Toggle());
         
         rbutton7.whenPressed(new AdjustCamera(0.4,1));  // ( Tilt, Pan) Target???
         rbutton8.whenPressed(new AdjustCamera(0.7, .26));   // ( Tilt, Pan) Ball????
         
             rbutton9.whenPressed(new SystemsCheck());
         
         rbutton10.whenPressed(new ToggleLight());      
 
 /****************************
  * Left
  ****************************/
         lbutton1.whenPressed(new Shooter_Fire());
         
         lbutton2.whenPressed(new Conveyor_Move(1));
         lbutton2.whenReleased(new Conveyor_Move(0));
         
         lbutton3.whenPressed(new Conveyor_Move(-1));
         lbutton3.whenReleased(new Conveyor_Move(0));
         
         lbutton4.whenPressed(new Loader_Forward());
         lbutton4.whenReleased(new Loader_Stop());
         
         lbutton5.whenPressed(new Loader_Reverse());
         lbutton5.whenReleased(new Loader_Stop());
         
         lbutton6.whenPressed(new Rollers_On(-1));
         lbutton6.whenReleased(new Rollers_Off());
 
         lbutton7.whenPressed(new FeederDoor_Toggle());
         
         lbutton8.whenPressed(new ShooterAngle_Move(1));
         lbutton8.whenReleased(new ShooterAngle_Move(0));
         
         lbutton9.whenPressed(new ShooterAngle_Move(-1));
         lbutton9.whenReleased(new ShooterAngle_Move(0));
         
         lbutton10.whenPressed(new ShooterAngle_Set(2.07));
         
         lbutton11.whenPressed(new ShooterAngle_Set(3.70));  
         
     }
     /*
      * getRightStick
      * Returns full value of joystick
      * @return rightJoy
      * @author Kyle Deane
      */
     public Joystick getRightStick() {
         return rightJoy;
     }
     
     /*
      * getLeftStick
      * Retruns full value of left Joystick
      * @return leftJoy
      * @author KYle Deane
      */
     public Joystick getLeftStick() {
         return leftJoy;
     }
     /**
      * @return The value of the left joystick X axis
      * @author Kyle Deane
      */
     public double getLeftX() {
         return leftJoy.getX();
     }
     
     /**
      * @return The value of the left joystick Y axis.
      * @author Kyle Deane
      */
     public double getLeftY() {
         return leftJoy.getY();
     }
     
      /**
      * @return The value of the left joystick Z axis.
      * @author Kyle Deane
      */
     public double getLeftZ() {
         return leftJoy.getZ();
     }
     
     /**
      * @return The value of the right joystick Y axis.
      * @author Kyle Deane
      */
      public double getRightY() {
         return rightJoy.getY();
     }
      
     /**
      * @return The value of the right joystick X axis.
      * @author Kyle Deane
      */
     public double getRightX() {
         return rightJoy.getX();
     }
     
     /*
      * @return value of Right Twist
      * @author Kyle Deane
      */
     public double getRightTwist(){
         return rightJoy.getTwist();
     }
     
     /*
      * @return value of right Hat X Axis (And Y Axis)
      * Used above to turn these values into buttons, because these are binary and shouldn't be an axis.
      */
     public double getRightHatX(){
         return rightJoy.getRawAxis(5);
     }
     public double getRightHatY(){
         return rightJoy.getRawAxis(6);
     }
     
     /**
      * @return The value of the right joystick Z axis.
      * @author Kyle Deane
      */
     public double getRightZ() {
         return rightJoy.getRawAxis(4);
     }
    
     public int getDelay(){
        double yaled = driver.getAnalogIn(2);
         if (yaled == 0){
             delay = 0;
         }else if(yaled >= 0.5 && yaled <= 0.7){
             delay = 1;
         }else if(yaled >= 1.2 && yaled <= 1.4){
             delay = 4;
         }else if(yaled >= 1.8 && yaled <= 2.1){
             delay = 6;
         }else if(yaled >= 2.5 && yaled <= 2.8){
             delay = 8;
         }else if(yaled >= 3.0 && yaled <= 4.0){
             delay = 10;
         }else{
             delay = 0;
         }
         System.out.println(delay);
         return delay;
         
     }
     
     public int getAutonMode(){
        double modebox = driver.getAnalogIn(1);
         if (modebox == 0){
             mode = 0;
         }else if(modebox >= 0.5 && modebox <= 0.7){
             mode = 1;
         }else if(modebox >= 1.2 && modebox <= 1.4){
             mode = 2;
         }else if(modebox >= 1.8 && modebox <= 2.1){
             mode = 3;
         }else if(modebox >= 2.5 && modebox <= 2.8){
             mode = 4;
         }else if(modebox >= 3.0 && modebox <= 4.0){
             mode = 5;
         }else{
             mode = 1;
         }
         return mode;
     }
 }
 
