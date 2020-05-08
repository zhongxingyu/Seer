 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package com.edinarobotics.zephyr;
 
 
 import com.edinarobotics.utils.gamepad.Gamepad;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Relay.Value;
 import edu.wpi.first.wpilibj.SimpleRobot;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Zephyr extends SimpleRobot {
     private double leftDrive = 0;
     private double rightDrive = 0;
     private double shooterSpeed = 0;
     private boolean ballLoaderUp = false;
     
     private final double SHOOTER_SPEED_STEP = 0.0005;
     
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
     public void autonomous() {
         
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() {
         Gamepad gamepad1 = new Gamepad(1);
         Gamepad gamepad2 = new Gamepad(2);
         Components components = Components.getInstance();
         while(this.isOperatorControl()&&this.isEnabled()){
            leftDrive = gamepad1.getLeftY();
            rightDrive = gamepad1.getRightY();
            if(gamepad1.getRawButton(Gamepad.RIGHT_BUMPER)){
                //Step speed of shooter up.
                shooterSpeed -= SHOOTER_SPEED_STEP;
                if(shooterSpeed<=-1){
                    shooterSpeed = -1;//Max speed is reverse 1 (-1).
                }
            }
            else if(gamepad1.getRawButton(Gamepad.LEFT_BUMPER)){
                //Step speed of shooter down.
                shooterSpeed += SHOOTER_SPEED_STEP;
                if(shooterSpeed>=0){
                    shooterSpeed = 0;
                }
            }
            if(gamepad1.getRawButton(Gamepad.BUTTON_1)){
                //Jump shooter speed to max.
                shooterSpeed = -1; //Max is -1
            }
            else if(gamepad1.getRawButton(Gamepad.BUTTON_2)){
                //Jump shooter speed to min.
                shooterSpeed = 0;
            }
            ballLoaderUp = gamepad1.getRawButton(Gamepad.RIGHT_TRIGGER);
            mechanismSet();
         }
         stop();
     }
     
     private void mechanismSet(){
         Components robotParts = Components.getInstance();
         robotParts.driveControl.tankDrive(leftDrive, rightDrive);
         robotParts.shooterJaguar.set(shooterSpeed);
         robotParts.ballLoadPiston.set((ballLoaderUp ? Relay.Value.kReverse :
                                                       Relay.Value.kForward));
         robotParts.textOutput.println(DriverStationLCD.Line.kUser2, 1, "Shooter Val:");
         robotParts.textOutput.println(DriverStationLCD.Line.kUser3, 1, Double.toString(shooterSpeed));
        robotParts.textOutput.updateLCD();
     }
     
     private void stop(){
         leftDrive = 0;
         rightDrive = 0;
         shooterSpeed = 0;
         ballLoaderUp = false;
         mechanismSet();
     }
 }
