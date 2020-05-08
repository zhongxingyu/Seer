 package org.first.team2620.subsystems;
 
 import edu.wpi.first.wpilibj.Timer;
 import org.first.team2620.RobotMap;
 
 /**
  *
  * @author frc2620
  */
 public class Climber {
     
     private int LevelCount_ = 0;
     private int StopClimbLevel_ = 3;
     private double LHClimbPower_ = RobotMap.ClimbPower;
     private double RHClimbPower_ = -RobotMap.ClimbPower;
     private double LegPower_ = RobotMap.LegPower;
     private boolean LHClimb_ = true;
     private boolean RHClimb_ = true;
     
     private Thread ConveyorsThread_ = null;
     private Thread LegThread_ = null;
     
     private boolean End_ = false;
     
     private boolean Climbing_ = false;
 
     public void climb()
     {
         if(Climbing_ == false)
         {
             Climbing_ = true;
             
             // Conveyors Movement Thread
             ConveyorsThread_ = new Thread(new Runnable() {
 
                 public void run() {
 
                     RobotMap.LHConveyor.set(0.5);
                     RobotMap.RHConveyor.set(0.5);
                     
                     Timer.delay(0.2);
                     
                     RobotMap.LHConveyor.set(0);
                     RobotMap.RHConveyor.set(0);
                     
                    while(!RobotMap.Joystick1.getRawButton(8) && End_ == false) {
                         Timer.delay(0.1);
                     }
                     
                     while(LevelCount_ <= StopClimbLevel_ && End_ == false)
                     {
                         if(RobotMap.LHTop.get() && RobotMap.RHTop.get()) // Both are on top hook
                         {
                             LHClimb_ = true;
                             RHClimb_ = true;
 
                             LHClimbPower_ *= -1;
                             RHClimbPower_ *= -1;
                             LevelCount_ += 1;
                         }
                         else
                         {
                             if(RobotMap.LHMiddle.get() && RobotMap.RHMiddle.get()) // Both holding on middle
                             {
                                 LHClimb_ = true;
                                 RHClimb_ = true;
 
                                 if(LevelCount_ == StopClimbLevel_)
                                 {
                                     LHClimb_ = false;
                                     RHClimb_ = false;
                                 } 
                                 else
                                 {
                                     LHClimbPower_ *= -1;
                                     RHClimbPower_ *= -1;
                                 }
                             }
                             else // Middle of climbing, lets keep this bad boy level
                             {
                                 // Level each side out at top
                                 if(RobotMap.LHTop.get() && !RobotMap.RHTop.get()) // Left hand is ready to stop, right hand keep climbing to top
                                 {
                                     LHClimb_ = false;
                                 } 
                                 else if(RobotMap.RHTop.get() && !RobotMap.LHTop.get()) // Right hand is ready to stop, left hand keep climbing to top
                                 {
                                     RHClimb_ = false;
                                 }
 
                                 // Level each side out at middle
                                 if(RobotMap.LHMiddle.get() && !RobotMap.RHMiddle.get()) // Left hand is ready to stop, right hand keep climbing to top
                                 {
                                     LHClimb_ = false;
                                 } 
                                 else if(RobotMap.RHMiddle.get() && !RobotMap.LHMiddle.get()) // Right hand is ready to stop, left hand keep climbing to top
                                 {
                                     RHClimb_ = false;
                                 }
                             }
                         }
 
                         if(LHClimb_) {
                             RobotMap.LHConveyor.set(LHClimbPower_);
                         } else {
                             RobotMap.LHConveyor.set(0);
                         }
 
                         if(RHClimb_) {
                             RobotMap.RHConveyor.set(RHClimbPower_);
                         } else {
                             RobotMap.RHConveyor.set(0);
                         }
                         
                         
                         Timer.delay(0.05);
                     }
 
                     // Just to make sure we stop climbing
                     RobotMap.LHConveyor.set(0);
                     RobotMap.RHConveyor.set(0);
                 }
 
             });
 
             // Leg Movement Thread
             LegThread_ = new Thread(new Runnable() {
 
                 public void run() {
 
                     boolean bringDown = false;
                     boolean bringUp = false;
 
                     while(LevelCount_ < StopClimbLevel_ && End_ == false)
                     {
                         if(RobotMap.LHMiddle.get() && RobotMap.RHMiddle.get()) // Both holding on middle, bring leg down
                         {
                             bringDown = true;
                         }
 
                         if(RobotMap.LHTop.get() && RobotMap.RHTop.get()) // Both are set on top, bring leg up to get out of corners way
                         {
                             bringUp = true;
                         }
 
                         if(bringDown)
                         {
                             if(!RobotMap.LegDown.get() == false)
                             {
                                 RobotMap.Leg.set(LegPower_);
                             }
                             else
                             {
                                 RobotMap.Leg.set(0);
                                 bringDown = false;
                             }
                         }
 
                         if(bringUp)
                         {
                             if(!RobotMap.LegUp.get() == false)
                             {
                                 RobotMap.Leg.set(-LegPower_);
                             }
                             else
                             {
                                 RobotMap.Leg.set(0);
                                 bringDown = false;
                             }
                         }
                         
                         
                         Timer.delay(0.05);
                     }
 
                     // Bring leg all the way up after it climbs all the way
                     while(!RobotMap.LegUp.get() == false && End_ == false)
                     {
                         RobotMap.Leg.set(-LegPower_);
                         Timer.delay(0.05);
                     }
                     RobotMap.Leg.set(0);
                 }
             });
 
 
             ConveyorsThread_.start();
             LegThread_.start();
         }
     }
     
     public void overRideClimb()
     {
         End_ = true;
     }
     
     
     public void newMatch()
     {
         LevelCount_ = 0;
         StopClimbLevel_ = 3;
         LHClimbPower_ = RobotMap.ClimbPower;
         RHClimbPower_ = -RobotMap.ClimbPower;
         LegPower_ = RobotMap.LegPower;
         LHClimb_ = true;
         RHClimb_ = true;
 
         ConveyorsThread_ = null;
         LegThread_ = null;
 
         End_ = false;
 
         Climbing_ = false;
 
     }
 
 }
