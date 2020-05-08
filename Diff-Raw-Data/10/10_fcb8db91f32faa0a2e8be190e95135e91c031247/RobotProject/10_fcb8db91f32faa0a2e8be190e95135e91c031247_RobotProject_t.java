 /*----------------------------------------------------------------------------*/
 /* Copyright (c) FIRST 2008. All Rights Reserved.                             */
 /* Open Source Software - may be modified and shared by FRC teams. The code   */
 /* must be accompanied by the FIRST BSD license file in the root directory of */
 /* the project.      
  /*----------------------------------------------------------------------------*/
 package edu.ames.frc.robot;
 //___
 // | |_  o  _     o  _    _|_|_  _    __  _  o __     _  |  _  _  _
 // | | | | _>     | _>     |_| |(/_   |||(_| | | |   (_  | (_|_> _>
 //The main class is under control of Kolton Yager and Danial Ebling. DO NOT EDIT
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Watchdog;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class RobotProject extends IterativeRobot {
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     protected static MotorControl MC = new MotorControl();
     protected static InputManager IM = new InputManager();
     protected static ImageProcessor IP = new ImageProcessor();
     protected static FrisbeeSimulator FS = new FrisbeeSimulator();
     protected static Communication Com = new Communication();
     protected static SensorInput SI = new SensorInput();
     protected static RobotMap RM = new RobotMap();
     protected static Watchdog wd;
     double pivotval;
     double[] drivemotorvalues;
     double[] joystickangleandspeed;
     double climbval;
     double auxjoystick;
    boolean feederthreadrunning = false;
     protected static byte autonomfcount;
 
     // this runs the feeder - we only need to make it dart in and out
     // putting this function in a thread also allows us to keep driving the robot
     class FeederThread extends Thread {
         public void run() {
             try {
                 System.out.println("fed!");
                 wd.feed();
                 while(!SI.getFeederSwitch()) {
                     System.out.println("going...");
                     wd.feed();
                    feederthreadrunning = true;
                     Thread.sleep(30);
                     MC.pusher(1);
                 }
                 MC.pusher(0);
                 Thread.sleep(50);
                 wd.feed();
                 for(int i=0;i<7;i++) {
                     wd.feed();
                    feederthreadrunning = true;
                     MC.pusher(-1);
                     Thread.sleep(30);
                 }
                 wd.feed();
                 MC.pusher(0);
                 wd.feed();
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
            feederthreadrunning = false;
         }
     }
         
     public void robotInit() {
         //Com.ps = new Communication.PISocket(true);
         
         try{
             Com.ps.init(true);
             Com.ps.GetData();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         wd = Watchdog.getInstance();
         wd.setExpiration(0.5);
         SI.init();
         IM.init();
         MC.init();
         wd.feed();
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
         autonomfcount = 3;
         while(isAutonomous()){
             FeederThread aft = new FeederThread();
             wd.feed();
             if (autonomfcount == 3) {
                 for(double it = 0; it <= 10; it++){
                    //MC.shooter(it);
                    MC.shootertilt(1 - (it * .1));
                    System.out.println("decrement: " + (1 - (it * .1)));
                    wd.feed();
                    Timer.delay(.15);
                 }
                 MC.shooter(0);//Justin Case Attorney at law!!
                 wd.feed();
                 MC.shooter(.7);
                 System.out.println("Autonomous Shooting=3");
                 
                 for (int ti = 0; ti <= 2; ti++) {
                     Timer.delay(.2);
                     wd.feed();
                 }
                 
                 Timer.delay(.1);
                 System.out.println("$#####");
                 wd.feed();
             }
             if (autonomfcount > 0) {
                 System.out.println("In shooter loop");
                 new Thread(aft).start();
                 autonomfcount--;
                 wd.feed();
                 for (int ti = 0; ti <= 10; ti++) {
                     Timer.delay(.2);
                     wd.feed();
                 }
                 System.out.println("Autonomous Shooting >0");
             } else if (autonomfcount == 0) {
                 MC.shooter(0);
                 System.out.println("Autonomous Shooting finished");
                 autonomfcount = -1;
             }
         }
         
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         while (isOperatorControl() && isEnabled()) {
             wd.feed();
             IM.updateAllButtons();
             
             joystickangleandspeed = IM.getPureAxis();
             pivotval = IM.getPivot() * 0.5;
             Com.RobotDirection(joystickangleandspeed[0]);
             Com.RobotSpeed(joystickangleandspeed[1]);            
             
             drivemotorvalues = MC.convertHeadingToMotorCommands(joystickangleandspeed[0], joystickangleandspeed[1]);
             drivemotorvalues = MC.setSpeedCap(drivemotorvalues, IM.speedBoost.getState(), IM.speedUnboost.getState());
             drivemotorvalues = MC.addPivot(drivemotorvalues, pivotval);
             wd.feed();
             
             System.out.println("angle: " + joystickangleandspeed[0] + 
                     "\tmotors: " + drivemotorvalues[0] + ",\t" + 
                     drivemotorvalues[1] + ",\t" + drivemotorvalues[2]);
             MC.drive(drivemotorvalues);
                 
             auxjoystick = IM.getSecondaryAxis(IM.tiltslow.getState());
            if (IM.tilttoggle.getState()) {
                 MC.shootertilt(IM.getThrottleAxis());
                 MC.climb(MC.Climblimit(auxjoystick));
             } else {
                 MC.shootertilt(auxjoystick);
                 MC.climb(0);
             }
 
             if (IM.fireButton.getState() && !IM.slowfireButton.getState()) {
                 MC.shooter(1.0);
             } else if(!IM.fireButton.getState() && IM.slowfireButton.getState()) {
                 MC.shooter(0.75);
             } else if (!IM.fireButton.getState() && !IM.slowfireButton.getState()) {
                 MC.shooter(0);
             }
             
             if(IM.feedforward.getState() && !IM.feedback.getState()) {
                 MC.pusher(1);
             } else if(!IM.feedforward.getState() && IM.feedback.getState()) {
                 MC.pusher(-1);
            } else if(!IM.feedforward.getState() && !IM.feedback.getState() && !feederthreadrunning) {
                 MC.pusher(0);
             }
             
             if(IM.feedbutton.getState()) {
                 FeederThread ft = new FeederThread();
                 new Thread(ft).start();
             }
             
             if(SI.getFeederSwitch()) {
                 System.out.println("pressed");
             }
             
             // vision code
             try {
                 Com.ps.GetData();
                 System.out.println("distance: " + Com.ps.distanceInt);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
             
         }
     }
 }
