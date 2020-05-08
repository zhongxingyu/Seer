 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Watchdog;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 public class RobotWizards extends SimpleRobot {
     
     public static final String ROTATTION_KEY = "Rotate State: ";
     public static final String LIFTING_KEY = "Lift State: ";
     
     private final WizardArmController armController;
     private final RobotDrive robotDrive;
     private final Joystick joystick1;
     private final Joystick joystick2;
     private final Joystick joystick3;
 
     public RobotWizards() {
         this.armController = new WizardArmController(RobotMap.RAISE_ARM_RELAY, 
                 RobotMap.ROTATE_ARM_RELAY);
         this.robotDrive = new RobotDrive(RobotMap.MOTOR_ONE, RobotMap.MOTOR_TWO);
         joystick1 = new Joystick(1);
         joystick2 = new Joystick(2);
         joystick3 = new Joystick(3);
     } 
     
     public void autonomous() {
         Watchdog.getInstance().setEnabled(false);
         
         //Autonomous cod here
         
         Watchdog.getInstance().setEnabled(true);
         Watchdog.getInstance().feed();
     }
 
     public void operatorControl() {
         while(isOperatorControl() && isEnabled()){
             Watchdog.getInstance().setEnabled(true);
             Watchdog.getInstance().feed();
             robotDrive.tankDrive(joystick1, joystick2);
             checkRotateJoystick();
             checkClimbButtons();
             checkAutoClimbButtons();
             
             Timer.delay(0.01);
         }
     }
     
     private void checkRotateJoystick(){
         if(joystick3.getY() > UIMap.JOYSTICK_DEAD_ZONE){
             armController.rotateArmsForward();
             SmartDashboard.putString(ROTATTION_KEY, "Forwards");
         }
        else if(joystick3.getY() < UIMap.JOYSTICK_DEAD_ZONE){
             armController.rotateArmsBackward();
             SmartDashboard.putString(ROTATTION_KEY, "Backwards");
         }
         else{
             armController.stopArmRotation();
             SmartDashboard.putString(ROTATTION_KEY, "Stopped");
         }
         SmartDashboard.putNumber("Rotatin:", joystick3.getY());
     }
     
     private void checkClimbButtons(){
         if(joystick3.getRawButton(UIMap.RAISE_ARM_BUTTON)){
             armController.lowerClimbArms();
             SmartDashboard.putString(LIFTING_KEY, "Lifting");
         }
         else if(joystick3.getRawButton(UIMap.LOWER_ARM_BUTTON)){
             armController.raiseClimbArms();
             SmartDashboard.putString(LIFTING_KEY, "Lowering");
         }
         else{
             armController.stopClimbArms();
             SmartDashboard.putString(LIFTING_KEY, "Stopped");
         }
     }
     
     private void checkAutoClimbButtons(){
         //To-Do
     }
     
     public void test() {
         SmartDashboard.putNumber("Test Axis", joystick3.getY());
     }
 }
