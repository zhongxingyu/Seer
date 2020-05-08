 package com.badrobots.y2012.technetium.commands;
 
 import com.badrobots.y2012.technetium.OI;
 import com.badrobots.y2012.technetium.subsystems.Helios;
 import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
 import edu.wpi.first.wpilibj.Relay;
 
 /*
  * @author 1014 Programming Team
  */
 public class GatherBallsAndManualShoot extends CommandBase //We need to rename this. Just maybe
 {    
     private static boolean bottomWasBlocked = false;  //was the top garage sensor blocked?
     private static boolean topWasBlocked = false;
     private static boolean done = true;
     
     public GatherBallsAndManualShoot() 
     {
         requires(ballGatherer);
         requires(shooter);
     }
 
     protected void initialize() 
     {
         //if (Helios.getInstance().topChannelBlocked())
             //topBlocked = true;
     }
 
     /*
      * Gathers Balls -- if the garage door sensor is blocked, then a ball is in the
      * gatherer, and the conveyor system runs until the sensor is no longer obsscured.
      * 
      * If the user wants to shoot, he/she first hits the secondary trigger,
      * revving the shooter. Then, while the shooter is revved, if the primary trigger is
      * pressed, the conveyors roll, decreasing the tracked ball count
      *
      * LUCAS WARNING: Uncommon circumstances could throw off counts!!
      * There needs to be greater self correcting added after the collector is built.
      */
 
     int spaceUp = 0;
     protected void execute() 
     {
         boolean conveyorUp = false;
         boolean conveyorDown = false;
         boolean rollerIn = false;
         boolean rollerOut = false;
 
 
         //Auto collect balls with counting. Does not space
         if(OI.secondXboxLeftJoyClick())
         {
             //Does not account for ball shooting yet
             if(sensors.getNumBalls() < 3)
             {
                 rollerIn = true;
                 conveyorUp = true;
             }
              else
             {
                  System.out.println("Balls Full");
                  rollerIn = false;
                  conveyorUp = false;
             }
         }
 
         //Code for shooting
         if (OI.getSecondaryTrigger())   //warm up the shooter -- think gatling gun
         {                           
             shooter.run(1);
 
             if (OI.getPrimaryTrigger()) // push balls into shooter
                 conveyorUp = true;
         }
         else
             shooter.run(0);
 
         //Ball Spacing
         if(OI.secondXboxA())
         {
             conveyorUp = false;
             conveyorDown = false;
             rollerIn = false;
             rollerOut = false;
 
             rollerIn = true;
             conveyorUp = false;
            if(sensors.bottomChannelBlocked())
             {
                 spaceUp = 100;
                 conveyorUp = true;
             }
         }
 
         if(spaceUp > 0)
         {
             spaceUp--;
             conveyorUp = true;
         }
 
         //Manual controlls here
         if(OI.secondXboxX())
         {
             conveyorUp = true;
             spaceUp = 0;
         }
         if(OI.secondXboxB())
         {
             conveyorUp = false;
             conveyorDown = true;
             spaceUp = 0;
         }
         if(OI.secondXboxY())
         {
             rollerIn = false;
             rollerOut = true;
         }
         if(OI.secondXboxLeftJoyClick())//this needs to be something else
         {
             rollerIn = true;
             rollerOut = false;
         }
 
         ballGatherer.runBottomRoller(rollerIn, rollerOut);
         ballGatherer.runConveyor(conveyorUp, conveyorDown);
 
     }
 
     // Make this return true when this Command no longer needs to run execute()
     protected boolean isFinished() 
     {
         return done;
     }
 
     // Called once after isFinished returns true
     protected void end() 
     {
         //ballGatherer.runBottomRoller(false, false);
         //ballGatherer.runConveyor(false, false);
         //shooter.run(0);
     }
 
     // Called when another command which requires one or more of the same
     // subsystems is scheduled to run
     protected void interrupted() {
     }
 }
 //2
 
 /*if (Helios.getInstance().topChannelBlocked())   // ball enters loading zone
                     topBlocked = true;
 
                 if (topBlocked && !Helios.getInstance().topChannelBlocked()) //ball leaves loading zone
                 {
                     ballGatherer.notifyBallShot();
                     topBlocked = false;
                 }*/
