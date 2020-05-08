 /*  ______ ______ ______ ______
  * |__    |      |__    |  __  |
  * |__    |_     |    __|__    |
  * |______| |____|______|______|
  */
 package edu.first3729.frc2012;
 
 /**
  * \file FRCRobot.java \brief The main class from which execution starts, as
  * mandated by WPILib
  *
  */
 
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Timer;
 
 import edu.first3729.frc2012.periodic.gamemode.*;
 
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class FRCRobot extends IterativeRobot {
     
     private FRCGameMode _mode = null;
     
     private Timer auto_timer;
     
     private int state = 0;
 
     /**
      * This function is run when the robot is first started up
      */
     public void robotInit() {
         // Initialization timer
         Timer init_timer = new Timer();
         
         // Print banner
         System.out.println(" ______ ______ ______ ______\n|__    |      |__    |  __  |\n|__    |_     |    __|__    |\n|______| |____|______|______|\n");
  
         // For the lulz
         System.out.println("This robot complies with Asimov's Laws of Robotics:");
         System.out.println("\t~> 1. A robot may not injure a human being or,\n\t      through inaction, allow a human being to come to harm.");
         System.out.println("\t~> 2. A robot must obey the orders given to it by human beings,\n\t      except where such orders would conflict with the First Law.");
         System.out.println("\t~> 3. A robot must protect its own existence as long as\n\t      such protection does not conflict with the First or Second Laws.");
     }
 
     public void disabledInit() {
        System.out.println("Going disabled.  Yes, the code is being updated.");
         
         // Disable the watchdog, because we don't need it
         this.getWatchdog().setEnabled(false);
         
         // Go disabled
         this._mode = FRCGameMode.to_disabled(this._mode, this);
     }
 
     public void disabledPeriodic() {
         this._mode.loop_periodic();
     }
     
     public void disabledContinuous() {
         this._mode.loop_continuous();
     }
 
     public void teleopInit() {
         System.out.println("Going teleoperated.");
         
         // WAtchdog expiration
         this.getWatchdog().setExpiration(5);
         
         // Enable the watchdog
         this.getWatchdog().setEnabled(true);
         
         // Go teleoperated
         this._mode = FRCGameMode.to_teleoperated(this._mode, this);
     }
 
     /**
      * This function is called periodically during teleoperated mode
      */
     public void teleopPeriodic() {
         // Run one loop
         this._mode.loop_periodic();
         
         // Feed the watchdog
         this.getWatchdog().feed();
     }
 
     public void teleopContinuous() {
         this._mode.loop_continuous();
     }
     
     public void autonomousInit() {
         System.out.println("Going autonomous.");
 
         auto_timer = new Timer();
         
         // Up watchdog expiration for autonomous
         this.getWatchdog().setExpiration(15);
         
         // Disable the watchdog
         this.getWatchdog().setEnabled(false);
         
         // Initialize autonomous
         this._mode = FRCGameMode.to_autonomous(this._mode, this);
         this._mode.init();
     }
     
     public void autonomousPeriodic() {
         // Run a loop
         this._mode.loop_periodic();
         
         // Feed the watchdog
         //this.getWatchdog().feed();
     }
     
     public void autonomousContinuous() {
         this._mode.loop_continuous();
     }
 }
