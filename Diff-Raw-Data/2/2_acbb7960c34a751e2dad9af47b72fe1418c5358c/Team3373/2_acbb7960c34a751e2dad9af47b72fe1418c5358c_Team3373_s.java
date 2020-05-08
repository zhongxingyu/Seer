 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.wpi.first.wpilibj.templates.*;
 //import edu.wpi.first.wpilibj.RobotDrive;
 //import edu.wpi.first.wpilibj.SimpleRobot;
 //import edu.wpi.first.wpilibj.templates.Shooter;
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the SimpleRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory. 
  */
 public class Team3373 extends SimpleRobot{
     /**
      * This function is called once each time the robot enters autonomous mode.
      */
    Servo frontCameraServo = new Servo(6);//camera class?
    
    
    
    // what is this used for?
    
    DriverStationLCD LCD = DriverStationLCD.getInstance();
    //SmartDashboard smartDashboard;
    SuperJoystick driveStick = new SuperJoystick(1); 
    SuperJoystick shooterController = new SuperJoystick(2);
    Shooter objShooter = new Shooter();
    //Deadband objDeadband = new Deadband();
    Timer robotTimer = new Timer();
   PickupArm arm = new PickupArm(this);
    //Camera camera = new Camera();
 
    double rotateLimitMaximum = 4.8;//are these used?
    double rotateLimitMinimum = 0.2;//are these used?
    //drive Drive = new drive(this);
 
    boolean test;
    boolean solenidFlag=false;
    
   /*********************************
    * Math/Shooter Action Variables *
    *********************************/
    
    TableLookUp objTableLookUp = new TableLookUp();
    
    double ShooterSpeedStage2 = 0;//was StageTwoTalon.get()
    double percentageScaler = 0.75;
    double ShooterSpeedStage1 = ShooterSpeedStage2 * percentageScaler;//was StageOneTalon.get()
    
    double ShooterSpeedMax = 5300.0;
    double ShooterSpeedAccel = 250;
    double stageOneScaler = .5; //What stage one is multiplied by in order to make it a pecentage of stage 2
    double PWMMax = 1; //maximum voltage sent to motor
    double MaxScaler = PWMMax/5300;
    double ShooterSpeedScale = MaxScaler * ShooterSpeedMax; //Scaler for voltage to RPM. Highly experimental!!
    double target;
    double RPMModifier = 250;
    double idle = 1 * ShooterSpeedScale;
    double off = 0;
    double change;
    
    double startTime = 9000000;
    double backTime = 90000000;
    double aTime = 900000000;
    double bTime = 900000000;
    double targetRotatePosition;
    boolean manualToggle;
    double manualStatus;
    boolean armTestFlag;
    int LX = 1;
    int LY = 2;
    int Triggers = 3;
    int RX = 4;
    int RY = 5;
    int DP = 6;
    
    //public Team3373(){
       // camera.robotInit();
     //}
     
     public void autonomous() {
         for (int i = 0; i < 4; i++)  {
 
             }
     }
 
     /**
      * This function is called once each time the robot enters operator control.
      */
     public void operatorControl() {
         robotTimer.start();
         while (isOperatorControl() & isDisabled()){ 
             manualToggle = false;
             armTestFlag = false;
             targetRotatePosition = arm.pot1.getVoltage();
         }
    while (isOperatorControl() & isEnabled()){
    objTableLookUp.test();
    /****************
    **Shooter Code***
    ****************/
    //Resets the internal toggle flags when a previously pushed button has been released
        shooterController.clearButtons();
        
        LCD.println(Line.kUser2, 1, "running");
        if(shooterController.isStartPushed()){
            LCD.println(Line.kUser5, 1, "Inside");//TODO
            //camera.imageAnalysis();
            System.out.println("Inside");
            //objShooter.start();
            //arm.armUp();
        }
        /**********************
         * Shooter Algorithms *
         **********************/
        
        if(shooterController.isAPushed() && !armTestFlag){
             objShooter.increaseSpeed();
        }
        if(shooterController.isBPushed() && !armTestFlag){
            objShooter.decreaseSpeed();
        }
        if(shooterController.isXPushed() && !armTestFlag){
            objShooter.increasePercentage();
        }
        if(shooterController.isYPushed() && !armTestFlag){
            objShooter.decreasePercentage();
        }
        if(shooterController.isBackPushed() && !armTestFlag){
            arm.armDown();
            //objShooter.stop();
        }
        if(shooterController.isLStickPushed() && !armTestFlag){
            LCD.println(Line.kUser5, 1, "Inside");
            //camera.imageAnalysis();
            System.out.println("Inside");
        }
        //arm.rotate(targetRotatePosition);
        objShooter.printLCD(LCD);
        //Arm.rotate(targetPosition);
        //objShooter.elevator();
        //Arm.grabFrisbee();
        //Arm.armUp();
        //Arm.armDown();
        //Arm.goToPosition(2.5);
        /*
        //try {Thread.sleep(1000);} catch(Exception e){}
        
        /*******************
         * Servo Test Code *
         ******************/
         
         /******************
          * Demo/Test Code *
          ******************/
        if  (!armTestFlag) {
        arm.rotateTalon.set(shooterController.getRawAxis(LX));
        }
         if (shooterController.isAHeld() && shooterController.isXHeld() && shooterController.isYHeld() && !armTestFlag){ //allows the test mode for the arm assembly to start
             armTestFlag = true;
         } else if (shooterController.isAHeld() && shooterController.isXHeld() && shooterController.isYHeld() && armTestFlag){ //turns the test mode for arm off
             armTestFlag = false;
         }
         if (armTestFlag){
 
             
             if (shooterController.isLStickPushed() && manualToggle){
                 manualToggle = false;
             } else if (shooterController.isRStickPushed() && !manualToggle){
                 manualToggle = true;
             }
             //switch (manualStatus){ //controls whether automatic or manual control
                 //case 0: //manual rotation control
               if (manualToggle){      
                     if (shooterController.isRBHeld() && !shooterController.isLBHeld()){
                         arm.rotateTalon.set(.5);
                     } else if (shooterController.isLBHeld() && !shooterController.isRBHeld()){
                         arm.rotateTalon.set(-.5);
                     } else{
                         arm.rotateTalon.set(0);
                     }
                     if (shooterController.isStartPushed()){
                         manualStatus = 1;
                     }
               } else if (!manualToggle) {
                 //case 1: //automatic targeting
                     if (shooterController.isLBPushed()){
                         targetRotatePosition = 2.7;
                     } 
                     if(shooterController.isRBPushed()){
                         targetRotatePosition = 2.3;
                     }
                     arm.rotate(targetRotatePosition);
                     if (shooterController.isBackPushed()){
                         manualStatus = 0;
                     }
               }
             //}
 
             shooterController.clearButtons();
             if (shooterController.isAPushed()) arm.armUp();
             if (shooterController.isBPushed()) arm.armDown();
             //Arm.grabFrisbee(shooterController.isStartPushed());
 
 
             LiveWindow.run();
         }
         SmartDashboard.putNumber("Target: ", targetRotatePosition);
         //SmartDashboard.putNumber("manualStatus: ", manualStatus);
         SmartDashboard.putBoolean("ManualToggle: ", manualToggle);
         SmartDashboard.putBoolean ("Test Status: ", armTestFlag);
        
         String currentTime = Double.toString(robotTimer.get());
         LCD.println(Line.kUser6, 1, currentTime);
         
         //String potString = Double.toString(pot1.getVoltage());
         //LCD.println(Line.kUser2, 1, potString);
         LCD.updateLCD();
     
         
         }
     }
 }
 
