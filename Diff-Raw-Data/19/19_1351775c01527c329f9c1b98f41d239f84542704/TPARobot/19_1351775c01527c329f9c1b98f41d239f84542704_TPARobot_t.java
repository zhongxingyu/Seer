 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.                                                               */
 /*----------------------------------------------------------------------------*/
 
 package edu.wpi.first.wpilibj.templates;
 
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.Compressor;
 
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
     static final double STOP_VALUE = 0.1;           // Value sent to each motor when the robot is stopping
     static final double CONVEYOR_SPEED = 1.0;       // The speed the conveyor motor will be set to move
     static final double SHOOTING_SPEED_4 = 0.3;     // The speed of the shooter controlled by button 4
     static final double SHOOTING_SPEED_3 = 0.5;     // The speed of the shooter controlled by button 3
     static final double SHOOTING_SPEED_5 = 1.0;     // The speed of the shooter controlled by button 5
     static final double SHOOTING_SPEED_OFF = 0.0;   // Shooting speed, controlled by button 10
     static double theJoystickSpeed = 0;             // Speed of shooters while Joystick controls speed
     static double shoot9ButtonSpeed = 0;            // Speed of shooters assigned to button 9 of shooting joystick
     static boolean joystickRunsShooter = false;     // Use the joystick to control the shooter  at a gradient
     static boolean joystickRanShooter= false;       // True if the joystick used to run the shooter
     static final int theAveragingValue = 10;        
     double theShootingSpeed = 0.0;                  // The actual speed the shooter is running
     static boolean shoot2ButtonPressable = true;    // Flag for pressability of button 6 on the shooting joystick
     static boolean shoot4ButtonPressable = true;    // Flag for pressability of button 4 on the shooting joystick 
     static boolean shoot3ButtonPressable = true;    // Flag for pressability of button 3 on the shooting joystick 
     static boolean shoot5ButtonPressable = true;    // Flag for pressability of button 5 on the shooting joystick
     static boolean shoot6ButtonPressable = true;    // Flag for pressability of button 6 on the shooting joystick
     static boolean shoot7ButtonPressable = true;    // Flag for pressability of button 7 on the shooting joystick
     static boolean shoot8ButtonPressable = true;    // Flag for pressability of button 8 on the shooting joystick
     static boolean left1ButtonPressable = true;     // Flag for pressablity of the trigger on the left joystick
     static boolean flipDriveDirection = false;      // Determines whether the robot is moving forward or backward
     static boolean conveyorMoving = false;          // Determines whether the conveyor is moving
     static boolean theRelayFlag = false;            // Is the relay on?
     static double theAccumulatedDistance;
     static int theDistancesCollected;
     static double theAveragedDistance;
     static double theDistance;                      // The distance returned by the ultrasonic sensor
     Jaguar theConveyorMotor;                        // The motor on the conveyor belt
     Jaguar theTopShootingMotor;                     // The shooting motor on the top
     Jaguar theBottomShootingMotor;                  // The shooting motor on the bottom
     AxisCamera theAxisCamera;                       // The camera
     DriverStationLCD theDriverStationLCD;           // Object representing the driver station   
     public double theMaxSpeed;                      // Multiplier for speed, determined by Z-Axis on left stick
     Solenoid theWedgeUp;                             //This Solenoid moves the wedge up and holds the ball in place
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
     Joystick theShootingStick;                      // The joystick used for all aspects of the shooting system
     TPARobotDriver theRobotDrive;                   // Robot Drive System
     int theDriveMode;                                           // The actual drive mode that is currently selected.
     static final int UNINITIALIZED_DRIVE = 0;                   // Value when no drive mode is selected
     static final int ARCADE_DRIVE = 1;                          // Value when arcade mode is selected 
     static final int TANK_DRIVE = 2;                            // Value when tank drive is selected
     Compressor theCompressor;                       // The air compressor
     TPAUltrasonicAnalogSensor theUltrasonicSensor;  // The ultrasonic sensor
     Relay theRelay;                                 // The Spike Relay
     
     double theSumFrontLeftSpeed =0;
     double theSumFrontRightSpeed =0;
     double theSumRearLeftSpeed =0;
     double theSumRearRightSpeed =0;
     int theNumberCollected=0;
     KinectStick theLeftArm;                         //Your Left Arm
     KinectStick theRightArm;                        //Your Right Arm
     double theHybridDriveRotation;
     double theHybridDriveMagnitude;
     double theHybridDriveDirection;
 
        
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
                
         // Define joysticks being used at USB port #1, USB port #2, and USB port #3 on the Drivers Station
 	theRightStick = new Joystick(1);
 	theLeftStick = new Joystick(2);
         theShootingStick = new Joystick(3);
         if (DEBUG == true){
            System.out.println("The Joysticks constructed successfully"); 
         }
         
         // Define a robot drive object with the front left motor at port 1, rear left at port 2, front right at port 3, and rear right at port 4
         theRobotDrive = new TPARobotDriver(1,2,3,4);
         if (DEBUG == true){
             System.out.println("theRobotDrive constructed successfully");
         }
         //Define Solenoids used to move ball into shooter
         theWedgeUp = new Solenoid(2);
 
         //Defines four E4P Motion Sensors at ports 1,2,3,4,5,6,7, and 8
        /* theFrontLeftEncoder = new Encoder(2,1);
         theFrontLeftEncoder.start();
         theRearLeftEncoder = new Encoder(6,5);
         theRearLeftEncoder.start();
         theFrontRightEncoder = new Encoder(4,3);
         theFrontRightEncoder.start();
         theRearRightEncoder = new Encoder(8,7);
         theRearRightEncoder.start();
         if (DEBUG == true){
             System.out.println("The Encoders constructed successfully");
         } */
         
         // Initialize the Conveyor belt motor at port 5
         theConveyorMotor = new Jaguar(5);
         if (DEBUG == true){
             System.out.println("The Conveyor Motor constructed successfully");
         }
         
         // Intialize the two shooting motors at ports 6 and 7
         theTopShootingMotor = new Jaguar(6);
         theBottomShootingMotor = new Jaguar (7);
         if (DEBUG == true){
             System.out.println("The Shooting Motors constructed successfully");
         }
         
         // Initialize the Ultrasonic sensor at analog port 1 and digital port 14
         theUltrasonicSensor = new TPAUltrasonicAnalogSensor(9,1);
         if (DEBUG == true){
             System.out.println("The ultrasonic sensor constructed successfully");
         }
         
         //Initialize the DriverStationLCD
         theDriverStationLCD = DriverStationLCD.getInstance();
         if (DEBUG == true) {
             System.out.println("DriverStationLCD initialized");
         }
         
         //Initialize the Relay
         theRelay = new Relay(2);
         if (DEBUG == true) {
             System.out.println("Relay initialized");
         }
         
         //Stretches out your arms and gets them ready to work
             theLeftArm = new KinectStick(1);
             theRightArm = new KinectStick(2);
             if (DEBUG == true) {
                 System.out.println("Arms Stretched");
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
         //theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Autonomous Mode Called");
         //theDriverStationLCD.updateLCD();    //Displays a message to DriverStationLCD when entering Autonomous mode
         runShooter(0);
         hybridDrive(theLeftArm, theRightArm);
         if (DEBUG == true) {
             System.out.println("Hybrid Drive Called");
         }
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
         if(DEBUG == true) {
             System.out.println("getCameraImage called");
         }
 
         // Drive the robot with FPS control
         driveRobot();
         if(DEBUG == true){
             System.out.println("driveRobot called");
         }
         
         // Run the conveyor belt
         runConveyor(theShootingStick, CONVEYOR_SPEED);
         if(DEBUG == true){
             System.out.println("runConveyor called");
         }
         
         // Display the speed of each wheel
         /*displaySpeed();
         if (DEBUG == true){
             System.out.println("displaySpeed called");
         } */
         
         // Run the shooter
         runShooter(determineShootingSpeed(theShootingStick));
         //runShooter(1);
         if (DEBUG == true){
             System.out.println("runShooter called");
         }
         //theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "" + theTopShootingMotor.get());
         //theDriverStationLCD.println(DriverStationLCD.Line.kUser2, 1, "" + theBottomShootingMotor.get());
         //theDriverStationLCD.updateLCD();
         
         // Run the Ultrasonic sensor
         /*runUltrasonicSensor(theUltrasonicSensor);
         if (DEBUG == true){
             System.out.println("runUltrasonicSensor called");
         } */
 
         System.out.println("displaySpeed called");
         
         dropBallIntoShooter(theShootingStick);
         
         determineJoystick();
         
         shootWithJoystick(theShootingStick);
 /*
         // Brake the robot if no joysick input.
         brakeOnNeutral();
         if(DEBUG == true) {
             System.out.println("brakeOnNeutral called");
         } */
        
         // Display the distance from ultrasonic sensor (for testing)
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
         // determine if tank or arcade mode, based upon position of "Z" wheel on kit joystick
         if (theRightStick.getZ() <= 0) {    // Logitech Attack3 has z-polarity reversed; up is negative
             // use arcade drive
             if (DEBUG == true){
                 System.out.println("theRightStick.getZ called" );
             }
             if(driveBackwards(theLeftStick)) {
                 theRobotDrive.flipArcadeDrive(theRightStick);	// drive with arcade style (use right stick)
             }
             else {
                 theRobotDrive.arcadeDrive(theRightStick, false);
             }
             if (theDriveMode != ARCADE_DRIVE) {
             // if newly entered arcade drive, print out a message
             System.out.println("Arcade Drive\n");
             theDriveMode = ARCADE_DRIVE;
             }
         } else {
             // use tank drive
             if(driveBackwards(theLeftStick)) {            
                 theRobotDrive.flipTankDrive(theLeftStick, theRightStick);	// drive with tank style
             }
             else {
                 theRobotDrive.tankDrive(theLeftStick, theRightStick);
             }
             if (theDriveMode != TANK_DRIVE) {
                 // if newly entered tank drive, print out a message
                 System.out.println("Tank Drive\n");
                 theDriveMode = TANK_DRIVE;
             }
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
         
         theMaxSpeed = (theLeftStick.getZ() - 1.0)/(-2.0); // z-axis is inverted
         theRobotDrive.setMaxSpeed(theMaxSpeed); // sets the multiplier
     }
     /*--------------------------------------------------------------------------*/
 
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Daniel Hughes
      * Date:    1/29/12
      * Purpose: Display the average speed output from the encoders.
      * Inputs:  None
      * Outputs: None
      */    
     
     /*--------------------------------------------------------------------------*/
     public void displaySpeed(){
     
         double theFrontLeftSpeed = theFrontLeftEncoder.getRate();
         double theRearLeftSpeed = theRearLeftEncoder.getRate();
         double theFrontRightSpeed = theFrontRightEncoder.getRate();
         double theRearRightSpeed = theRearRightEncoder.getRate();
 
         theSumFrontLeftSpeed += theFrontLeftSpeed;
         theSumRearLeftSpeed += theRearLeftSpeed;
         theSumFrontRightSpeed += theFrontRightSpeed;
         theSumRearRightSpeed += theRearRightSpeed;
         theNumberCollected++;
 
         if(theNumberCollected == 100){
             theNumberCollected =0;
             String theAverageFrontLeftSpeed = "FLS: " + theSumFrontLeftSpeed/100;
             String theAverageRearLeftSpeed = "RLS: " + theSumRearLeftSpeed/100;
             String theAverageFrontRightSpeed = "FRS: " + theSumFrontRightSpeed/100;
             String theAverageRearRightSpeed = "RRS: " + theSumRearRightSpeed/100; 
 
             theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1 , theAverageFrontLeftSpeed );
             theDriverStationLCD.println(DriverStationLCD.Line.kUser2, 1 , theAverageRearLeftSpeed );
             theDriverStationLCD.println(DriverStationLCD.Line.kUser3, 1 , theAverageFrontRightSpeed );
             theDriverStationLCD.println(DriverStationLCD.Line.kUser4, 1 , theAverageRearRightSpeed );
             theDriverStationLCD.updateLCD();
 
             theSumFrontLeftSpeed=0;
             theSumRearLeftSpeed=0;
             theSumFrontRightSpeed=0;
             theSumRearRightSpeed=0;
         } 
     }
     /*--------------------------------------------------------------------------*/
 
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author: Andrew Matsumoto
      * Date: 1/26/12   
      * Purpose: decides whether the button to make the direction and rotation 
      * opposite of what they are originally is pressed 
      * Inputs:  
      * Outputs: the direction and the rotation opposite of the original.
      */  
     
     public boolean driveBackwards(Joystick aStick){
         if (left1ButtonPressable  && aStick.getRawButton(1)){
             flipDriveDirection = !flipDriveDirection; // if flip is false, make it true and vice versa.
             left1ButtonPressable = false;
         }
         if (!aStick.getRawButton(1)){
           left1ButtonPressable = true;  
         }
         return flipDriveDirection;
     }
     /*--------------------------------------------------------------------------*/
 
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    1/29/12
      * Purpose: To run a conveyor belt and allow for it to be turned on and off with
      *          a button press
      * Inputs:  Joystick aStick - the joystick that runs the conveyor
      *          double aSpeed - the speed to run the conveyor at
      * Outputs: None
      */    
     
     public void runConveyor(Joystick aStick, double aSpeed){
         if(shoot2ButtonPressable && aStick.getRawButton(2)){ // Toggle conveyor if the button is pressed
             conveyorMoving = !conveyorMoving;
             shoot2ButtonPressable = false;
         }
         if(!aStick.getRawButton(2)){ // On button release, allow it to be pressed again
             shoot2ButtonPressable = true;
         }
         if(conveyorMoving) {
             theConveyorMotor.set(aSpeed);
         }
         else if (!conveyorMoving) {
             theConveyorMotor.set(0);
         }
     }
     /*--------------------------------------------------------------------------*/
 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    2/4/12
      * Purpose: To run a shooter with two motors controlling four wheels. One motor
      *          will run counterclockwise, the other will run clockwise.
      * Inputs:  double aSpeed - the speed the motors will run in
      * Outputs: None
      */    
     
     public void runShooter(double aSpeed){
         theTopShootingMotor.set(aSpeed);
         theBottomShootingMotor.set(-aSpeed);
     }    
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    2/4/12, 2/16/2012 (Gennaro)
      * Purpose: To determine the speed at which the wheels on the shooter will run.
      *          The speeds are controlled by buttons 4, 3, and 5 on the Joystick.
      * Inputs:  Joystick aStick - the joystick which controls the speed
      * Outputs: double theShootingSpeed - the speed the shooter will run at
      */    
     
     public double determineShootingSpeed(Joystick aStick){
         if(aStick.getRawButton(4)){
             theShootingSpeed = SHOOTING_SPEED_4;
             joystickRunsShooter = false;
             joystickRanShooter = false;
         }
         if(aStick.getRawButton(3)){
             theShootingSpeed = SHOOTING_SPEED_3;
             joystickRunsShooter = false;
             joystickRanShooter = false;
         }
         if(aStick.getRawButton(5)){
             theShootingSpeed = SHOOTING_SPEED_5;
             joystickRunsShooter = false;
             joystickRanShooter = false;
         }
         if(aStick.getRawButton(9)) {
             theShootingSpeed = shoot9ButtonSpeed;
             joystickRunsShooter = false;
             joystickRanShooter = false;
         }
         if(aStick.getRawButton(10)) {
             theShootingSpeed = SHOOTING_SPEED_OFF;
             joystickRunsShooter = false;
             joystickRanShooter = false;
         }
         return theShootingSpeed;
     }
     /*--------------------------------------------------------------------------*/
         
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    2/4/2012
      * Purpose: To run the ultrasonic sensor. A press of button 8 toggles it on 
      *          and off.
      * Inputs:  TPAUltrasonicAnalogSensor aSensor - the ultrasonic sensor
      * Outputs: 
      */
 
     public void runUltrasonicSensor(TPAUltrasonicAnalogSensor aSensor){
         // Read in distance and add to an accumulator
         theDistance = aSensor.getDistance();
         theAccumulatedDistance = theAccumulatedDistance + theDistance;
         theDistancesCollected = theDistancesCollected + 1;
         // If enough distances have been collected, print the average value out and restart
         if (theDistancesCollected == theAveragingValue){
             theAveragedDistance = theAccumulatedDistance/theDistancesCollected;
             theDriverStationLCD.println(DriverStationLCD.Line.kUser6,1, "" + theAveragedDistance);
             theDriverStationLCD.updateLCD();
             theAccumulatedDistance = 0;
             theDistancesCollected = 0;
         }
         if(DEBUG == true){
             System.out.println("Sensor Enabled");
         }
     }
     
     /*--------------------------------------------------------------------------*/
     /* 
      * Author:  Sumbhav Sethia
      * Date:    1/29/2012
      * Purpose: To drop one and only one ball into the shooter mechanism
      * Inputs:  Joystick aStick
      * Outputs: None
      */
     
     /*--------------------------------------------------------------------------*/
         
         public void dropBallIntoShooter(Joystick aStick) {
            /* if(aStick.getRawButton(1)) {
                 theRelay.set(Relay.Value.kForward);
             }            
             if(aStick.getRawButton(11)) {
                 theRelay.set(Relay.Value.kOff);
             } */
             if(aStick.getRawButton(1)) {        //Throws ball into shooter
                if(DEBUG == true) {
                    theDriverStationLCD.println(DriverStationLCD.Line.kUser5, 1, "Shooter Fired");
                    theDriverStationLCD.updateLCD();
                }
                if(theRelayFlag) {
                    theRelay.set(Relay.Value.kForward);
                    theRelayFlag = false;
                }            
             } 
             else if(!aStick.getRawButton(1)) {
                 if(!theRelayFlag) {
                     theRelay.set(Relay.Value.kOff);
                     theRelayFlag = true;
                 }
                 theDriverStationLCD.println(DriverStationLCD.Line.kUser5, 1, "               ");
                 theDriverStationLCD.updateLCD();
             } 
         }
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Sumbhav Sethia and Gennaro De Luca
      * Date:    2/11/2012, 2/12/2012
      * Purpose: Hybrid Mode Drive
      * Inputs:  Two Arms
      * Outputs: None
      */    
     
     /*--------------------------------------------------------------------------*/
         public void hybridDrive(KinectStick aLeftArm, KinectStick aRightArm) {
             theHybridDriveRotation = aLeftArm.getY();
             theHybridDriveMagnitude = aRightArm.getY();
             if(aRightArm.getRawButton(3)){
                 theHybridDriveDirection = -90;
             }
             else if(aLeftArm.getRawButton(4)){
                 theHybridDriveDirection = 90;
             }
             else if(!(aLeftArm.getRawButton(4) && aRightArm.getRawButton(3))){
                 theHybridDriveDirection = 0;
             }
             
             if(aRightArm.getRawButton(5)) {
                 if(theRelayFlag) {
                    theRelay.set(Relay.Value.kForward);
                    theRelayFlag = false;
                }
                else if (!theRelayFlag) {
                    theRelay.set(Relay.Value.kOff);
                }   
                 theDriverStationLCD.println(DriverStationLCD.Line.kUser3, 1, "Button 3 pressed");
                 theDriverStationLCD.updateLCD();
             }
             else {
                 theDriverStationLCD.println(DriverStationLCD.Line.kUser3, 1, "                  ");
                 theDriverStationLCD.updateLCD();
                 theRelayFlag = true;
             }
             theRobotDrive.mecanumDrive_Polar(theHybridDriveMagnitude, theHybridDriveDirection, theHybridDriveRotation );
             Timer.delay(.01);   // Delay 10ms to reduce processing load
         }
             
   /*---------------------------------------------------------------------------------------*/
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Gennaro De Luca
      * Date:    2/14/12
      * Purpose: Determine which joystick is which
      * Inputs:  None
      * Outputs: None
      */    
     
     /*--------------------------------------------------------------------------*/
 
         public void determineJoystick() {
             if(theLeftStick.getRawButton(6)) {
                 theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Left       ");
             }
             else if(theRightStick.getRawButton(6)) {
                 theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Right      ");
             }
             else if(theShootingStick.getRawButton(6)) {
                 theDriverStationLCD.println(DriverStationLCD.Line.kMain6, 1, "Shooting");
             }
             theDriverStationLCD.updateLCD();
         }
     
    /*--------------------------------------------------------------------------*/
     /*
      * Author:  Team
      * Date:    2/15/2012, 2/16/2012
      * Purpose: Speed Controlled Shooting
      * Inputs:  aJoystick
      * Outputs: 
      */    
         public void shootWithJoystick(Joystick aStick){
            
             if(aStick.getRawButton(7) && shoot7ButtonPressable) {
                 joystickRunsShooter = !joystickRunsShooter;
                 shoot7ButtonPressable = false;
                 joystickRanShooter = false;
             }
             if(joystickRunsShooter) {
                 theTopShootingMotor.set(aStick.getMagnitude());
                 theBottomShootingMotor.set(-(aStick.getMagnitude()));
                 if(aStick.getRawButton(6) && shoot6ButtonPressable) {
                     theJoystickSpeed = aStick.getMagnitude();
                     joystickRunsShooter = false;
                     joystickRanShooter = true;
                 }
                else if(!aStick.getRawButton(6) && !shoot6ButtonPressable) {
                    shoot6ButtonPressable = true;
                }
                if(aStick.getRawButton(8) && shoot8ButtonPressable) {
                shoot8ButtonPressable = false;
                shoot9ButtonSpeed = aStick.getMagnitude();
                }
                else if(!aStick.getRawButton(8) && !shoot8ButtonPressable) {
                    shoot8ButtonPressable = true;
                }
             }
             else if(!joystickRunsShooter && joystickRanShooter) {
                 theTopShootingMotor.set(theJoystickSpeed);
                 theBottomShootingMotor.set(-theJoystickSpeed);
             }
             if (!aStick.getRawButton(7) && !shoot7ButtonPressable){
                 shoot7ButtonPressable = true;
             }
         }
     /*--------------------------------------------------------------------------*/
                 
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    10/30/2011 (Marissa Beene)
      * Purpose: To use the motors to brake the robot. Takes the speed from the 
      *          each motor and sends the reverse signal back.
      * Inputs:  Double aSpeedRight - the speed of the right motor
      *          Double aSpeedLeft - the speed of the left motor
      * Outputs: None
      */
     
     public void brake(double aSpeedLeft, double aSpeedRight){
         theRobotDrive.tankDrive(-aSpeedLeft, -aSpeedRight); //drive the robot at opposite values
         }
     /*--------------------------------------------------------------------------*/
     
     
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    10/30/11
      * Purpose: To determine if there is no signal. First determines the drive
      *          mode and discards the left stick if in arcade mode. If there is 
      *          no signal to the drive train, it will return true, otherwise it 
      *          will return false
      * Inputs:  Joystick aRightStick  - the right joystick
      *          Joystick aLeftStick - the left joystick
      * Outputs: Boolean - returns true if the drive train is not sent a signal
      */
     
     public boolean isNeutral(Joystick aRightStick, Joystick aLeftStick){
         if (DEBUG == true){
             System.out.println("isNeutral Called");
         }
         if(theDriveMode == ARCADE_DRIVE){ //if arcade drive
             if (DEBUG == true){
                 System.out.println("Arcade Drive Recognized by isNeutral");
             }
             if(aRightStick.getY() == 0 && aRightStick.getX() == 0){ //there is no input
                 return true;
             }
             else{
                 return false;
             }
         }
         else if(theDriveMode == TANK_DRIVE){ //if tank drive
             if (DEBUG == true){
                 System.out.println("Tank Drive Recognized by isNeutral");
             }
             if(aRightStick.getY() == 0 && aLeftStick.getY() == 0){
                 return true;
             }
             else{
                 return false;
             }
         }
         else{
             return false;
         } 
     }
     /*--------------------------------------------------------------------------*/
     
         
     /*--------------------------------------------------------------------------*/
     /*
      * Author:  Marissa Beene
      * Date:    11/5/11
      * Purpose: To brake the robot if there is no signal in arcade mode. If the 
      *          wheel is not considered stopped, it will read the direction that the wheel
      *          is turning and send it the stop value in the other direction.
      * Inputs:  None
      * Outputs: None
      */
     
     public void brakeOnNeutral(){        
         double theFrontLeftSpeedOutput = 0; // value the left motor will be sent
         double theFrontRightSpeedOutput = 0;
         double theRearLeftSpeedOutput = 0;
         double theRearRightSpeedOutput = 0; // value the right motor will be sent
         double theRightSpeedOutput = 0;
         double theLeftSpeedOutput = 0;
         
         if (DEBUG == true){
             System.out.println("brakeOnNeutral called");
         }
         
         if(isNeutral(theRightStick, theLeftStick)){ // if no signal is sent to the robot
             
             // get the direction of the left motor and store the stop value vector to theLeftSpeedOutput
             if(!theFrontLeftEncoder.getStopped()){
                 if(theFrontLeftEncoder.getDirection()){
                     theFrontLeftSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theFrontLeftSpeedOutput = -STOP_VALUE;
                 }
             }
             
             // get the direction of the right motor and store a stop value vector to theRightSpeedOutput
             if(!theFrontRightEncoder.getStopped()){
                 if(theFrontRightEncoder.getDirection()){
                     theFrontRightSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theFrontRightSpeedOutput = -STOP_VALUE;
                 }
             }
             
             if(!theRearRightEncoder.getStopped()){
                 if(theRearRightEncoder.getDirection()){
                     theRearRightSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theRearRightSpeedOutput = -STOP_VALUE;
                 }
             }
             if(!theRearRightEncoder.getStopped()){
                 if(theRearRightEncoder.getDirection()){
                     theRearRightSpeedOutput = STOP_VALUE;
                 }
                 else{
                     theRearRightSpeedOutput = -STOP_VALUE;
                 }
             }
             theLeftSpeedOutput = (theRearLeftSpeedOutput + theFrontLeftSpeedOutput)/2;
             theRightSpeedOutput = (theRearRightSpeedOutput + theFrontRightSpeedOutput)/2;
         // brake the robot at the value of the stop value
         brake(theLeftSpeedOutput, theRightSpeedOutput);
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
     
