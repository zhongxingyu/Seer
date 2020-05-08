 package com.edinarobotics.zephyr.autonomous;
 
 import com.edinarobotics.utils.autonomous.AutonomousStep;
 import com.edinarobotics.zephyr.Zephyr;
 import edu.wpi.first.wpilibj.Timer;
 
 /**
 * An autonomous step that warms up the shooter to a specific speed and then
 * fires a ball.
  */
 public class FireShooterStep extends AutonomousStep{
     private static final double SHOOTER_WARMUP_DELAY = 3;
     private static final double SHOOTER_PISTON_UP_TIME = 0.5;
     
     double shooterSpeed;
     Zephyr robot;
     Timer time;
     boolean isFinished;
    
    /**
     * Constructs a FireShooterStep that fires the shooter at the specified
     * speed.
     * @param shooterSpeed the speed for the shooter (a positive value).
     * @param robot {@link Zephyr}.
     */
     public FireShooterStep(double shooterSpeed,Zephyr robot)
     {
         this.shooterSpeed = shooterSpeed;
         this.robot = robot;
         time = new Timer();
         isFinished = false;
     }
     
     public void start()
     {
         robot.shooterSpeed = -shooterSpeed;
         robot.mechanismSet();
         time.start();
     }
     
     public void run()
     {
         if(time.get() >= SHOOTER_WARMUP_DELAY+SHOOTER_PISTON_UP_TIME){
             robot.ballLoaderUp = false;
             robot.mechanismSet();
             isFinished = true;
         }
         else if(time.get() >= SHOOTER_WARMUP_DELAY){
             robot.ballLoaderUp = true;
             robot.mechanismSet();
         }
         else{
             robot.mechanismSet();
         }
     }
     
     public boolean isFinished()
     {
         return isFinished;
     }
     
     public void stop(){
         robot.shooterSpeed = 0;
         robot.mechanismSet();
     }
 }
