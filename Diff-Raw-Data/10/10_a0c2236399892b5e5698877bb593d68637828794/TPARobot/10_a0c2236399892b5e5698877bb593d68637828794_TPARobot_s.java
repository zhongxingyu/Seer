 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class TPARobot extends IterativeRobot {
     static final boolean DEBUG = true;              // Debug Trigger
     static final boolean CAMERA = false;            // Camera Trigger
     AxisCamera theAxisCamera;                       // The camera
     DriverStationLCD theDriverStationLCD;           // Object representing the driver station   
     // Drive mode selection
     int theDriveMode;                               // The actual drive mode that is currently selected.
     static final int UNINITIALIZED_DRIVE = 0;       // Value when no drive mode is selected
     static final int ARCADE_DRIVE = 1;              // Value when arcade mode is selected 
     static final int TANK_DRIVE = 2;                // Value when tank drive is selected
     public double theMaxSpeed;                      // Multiplier for speed, determined by Z-Axis on left stick
     static final double STOP_VALUE = 0.1;           // Value sent to each motor when the robot is stopping
    Solenoid theWedgeUp;                             //This Solenoid moves the wedge up and holds the ball in place
     Solenoid theWedgeDown;                           //This Solenoid moves the wedge down and lets balls fall into the shooter
     Encoder theFrontLeftEncoder;                    // The front left E4P
     Encoder theRearLeftEncoder;                     // The rear left E4P
     Encoder theFrontRightEncoder;                   // The front right E4P
     Encoder theRearRightEncoder;                    // The rear right E4P
     double theFrontLeftOutput;                      // The output sent to the front left motor
     double theRearLeftOutput;                       // The output sent to the rear left motor
     double theFrontRightOutput;                     // The output sent to the front right motor
     double theRearRightOutput;                      // The output sent to the rear right motor
     Joystick theRightStick;                         // Right joystick
     Joystick theLeftStick;                          // Left joystick
     RobotDrive theRobotDrive;                   // Robot Drive System
     double theDriveDirection;                       // Direction the robot will move
     double theDriveMagnitude;                       // Speed the robot will move at
     double theDriveRotation;                        // Value the robot will rotate
 
     double afls =0;
     double afrs =0;
     double arls =0;
     double arrs =0;
     int numberCollected=0;
 
 
    
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/13/2012
      * Purpose: Robot Initialization Function. This function is run once when the
      *          robot is first started up and should be used for any initialization
      *          code.
      * Inputs:  None
      * Outputs: None
      */
     public void robotInit() {
                
         // Define joysticks being used at USB port #1 and USB port #2 on the Drivers Station
 	theRightStick = new Joystick(1);
 	theLeftStick = new Joystick(2);
         if (DEBUG == true){
            System.out.println("The Joysticks constructed successfully"); 
         }
         
         // Define a robot drive object with the front left motor at port 1, rear left at port 2, front right at port 3, and rear right at port 4
         theRobotDrive = new TPARobotDriver(1,2,3,4);
         if (DEBUG == true){
             System.out.println("theRobotDrive constructed successfully");
         }
         //Define Solenoids used to move ball into shooter
         theWedgeUp = new Solenoid(1);
         theWedgeDown = new Solenoid(2);
 
         //Defines four E4P Motion Sensors at ports 1,2,3,4,5,6,7, and 8
         theFrontLeftEncoder = new Encoder(2,1);
         theFrontLeftEncoder.start();
         theRearLeftEncoder = new Encoder(6,5);
         theRearLeftEncoder.start();
         theFrontRightEncoder = new Encoder(4,3);
         theFrontRightEncoder.start();
         theRearRightEncoder = new Encoder(8,7);
         theRearRightEncoder.start();
         if (DEBUG == true){
             System.out.println("The Encoders constructed successfully");
         }
 
 
         //Initialize the DriverStationLCD
         theDriverStationLCD = DriverStationLCD.getInstance();
         if (DEBUG == true) {
             System.out.println("DriverStationLCD initialized");
         }
         
         //Initialize the AxisCamera
         if (CAMERA == true){
             theAxisCamera = AxisCamera.getInstance(); 
             theAxisCamera.writeResolution(AxisCamera.ResolutionT.k320x240);        
             theAxisCamera.writeBrightness(50);
             if (DEBUG == true) {
                 System.out.println("AxisCamera initialized");
             }
         }
         else {
             if (DEBUG == true){
                 System.out.println("CAMERA set to false");
             }
         }
      
         // Initialize the Drive Mode to Uninitialized
         theDriveMode = UNINITIALIZED_DRIVE;
         
         // Default the robot to not move
         theDriveDirection = 0;
         theDriveMagnitude = 0;
         theDriveRotation = 0;
         if (DEBUG == true){
             System.out.println("The robot set to not move");
         }
         
         if (DEBUG == true){
         System.out.println("RobotInit() completed.\n");
         }
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/13/2012
      * Purpose: TPARobot Constructor
      * Inputs:  None
      * Outputs: None
      */
     public void TPARobot(){
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
 
     public void autonomousPeriodic() {
         
         Watchdog.getInstance().feed();
         theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Autonomous Mode Called");
         theDriverStationLCD.updateLCD();    //Displays a message to DriverStationLCD when entering Autonomous mode
     }
     /*--------------------------------------------------------------------------*/
     
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    1/13/2012
      * Purpose: This function is called periodically during autonomous mode. Allows
      *          for autonomous operation of the robot.
      * Inputs:  None
      * Outputs: None
      */
     public void teleopPeriodic() {
        
         // Feed the user watchdog at every period when in autonomous
         Watchdog.getInstance().feed();
         if (DEBUG == true){
             System.out.println("Teleop Periodic Watchdog Fed");
         }
         
         //Set the multiplier for max speed
         setMaxSpeed();
         if (DEBUG == true) {
             System.out.println("setMaxSpeed called");
         }
         
         //Get the image from the Axis Camera
         DriverStationLCD.getInstance().updateLCD();
         if(DEBUG) {
             System.out.println("getCameraImage called");
         }
 
         // Drive the robot with FPS control
         driveRobot();
         if(DEBUG == true){
             System.out.println("driveRobot called");
         }
         displaySpeed();
         System.out.println("displaySPeed called");
         
         DropBallIntoShooter(theRightStick);
 /*
         // Brake the robot if no joysick input.
         brakeOnNeutral();
         if(DEBUG == true) {
             System.out.println("brakeOnNeutral called");
         }
 */        
     }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/14/2012
      * Purpose: To drive the robot with mecanum wheels with a FPS control system.
      *          The left stick will control translational movement and the right
      *          stick will control rotation.
      * Inputs:  None
      * Outputs: None
      */    
     public void driveRobot() {
         theDriveDirection = theLeftStick.getDirectionDegrees(); // Set the direction to the value of the left stick
         theDriveMagnitude = theLeftStick.getMagnitude();    // Set the magnitude to the value of the left stick
         theDriveRotation = (theRightStick.getX()); // Set the rotation to the value of the right stick
         theRobotDrive.mecanumDrive_Polar(theDriveMagnitude, theDriveDirection, theDriveRotation );
         //theRobotDrive.mecanumDrive_Polar(.3, 180, 0);
         if (DEBUG == true){ 
         System.out.println("The drive rotation in degrees" + theDriveRotation);
         System.out.println("The drive magnitude is" + theDriveMagnitude);
         System.out.println("The drive direction is" + theDriveDirection);
         }
     }   
 
     /*--------------------------------------------------------------------------*/
 
                     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Gennaro De Luca, Marissa Beene
      * Date:    11/26/2011 (Gennaro De Luca)
      * Purpose: To determine the speed multiplier based on the "Z" wheel on 
      *          the left joystick. Responds as a gradient. If the "Z" wheel on the
      *          joystick gets a higher value, the robot will move faster.
      * Inputs:  None
      * Outputs: None
      */    
     public void setMaxSpeed(){
         
         theMaxSpeed = (theLeftStick.getZ() + 1.0)/2.0;
        // theRobotDrive.setMaxSpeed(theMaxSpeed); // sets the multiplier
     }
     /*--------------------------------------------------------------------------*/
 
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  
      * Date:    
      * Purpose: 
      * Inputs:  
      * Outputs: 
      */    
     
     /*--------------------------------------------------------------------------*/
 public void displaySpeed(){
     
     double tfl = theFrontLeftEncoder.getRate();
     double trl = theRearLeftEncoder.getRate();
     double tfr = theFrontRightEncoder.getRate();
     double trr = theRearRightEncoder.getRate();
     
     afls += tfl;
     arls += trl;
     afrs += tfr;
     arrs += trr;
     numberCollected++;
     
     if(numberCollected == 100){
         numberCollected =0;
         String print1 = "FLS: " + afls/100;
         String print2 = "RLS: " + arls/100;
         String print3 = "FRS: " + afrs/100;
         String print4 = "RRS: " + arrs/100;
     
         theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1 , print1 );
         theDriverStationLCD.println(DriverStationLCD.Line.kUser2, 1 , print2 );
         theDriverStationLCD.println(DriverStationLCD.Line.kUser3, 1 , print3 );
         theDriverStationLCD.println(DriverStationLCD.Line.kUser4, 1 , print4 );
         theDriverStationLCD.updateLCD();
         
         afls=0;
         arls=0;
         afrs=0;
         arrs=0;
     }
    
     
     
 }
     
     /*--------------------------------------------------------------------------*/
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Sumbhav Sethia
      * Date:    1/29/2012
      * Purpose: To drop one and only one ball into the shooter mechanism
      * Inputs:  Joystick aStick
      * Outputs: None
      */
         public void DropBallIntoShooter(Joystick aStick) {
            if(aStick.getRawButton(3)) {
               theWedgeUp.set(false);
               theWedgeDown.set(true);
               Thread.yield();
               Timer.delay(2.0);
               Thread.yield();
                theWedgeUp.set(true);
                theWedgeDown.set(false);
             }
         }
     
     /*--------------------------------------------------------------------------*/
 
  /*--------------------------------------------------------------------------*/
     /*
      * Author:  
      * Date:    
      * Purpose: 
      * Inputs:  
      * Outputs: 
      */    
     
     /*--------------------------------------------------------------------------*/
 
 
 }
