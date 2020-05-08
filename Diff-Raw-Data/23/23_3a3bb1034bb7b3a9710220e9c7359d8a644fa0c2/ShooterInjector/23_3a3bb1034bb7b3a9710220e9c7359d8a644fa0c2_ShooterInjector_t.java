 package org.usfirst.frc2022.subsystems;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.command.PIDSubsystem;
 import org.usfirst.frc2022.RobotMap;
 import org.usfirst.frc2022.Utils;
import org.usfirst.frc2022.commands.ShooterCommand;
 
 
 /**
  * This code is used to control the speed of Jaguars controlling the shooter for
  * picking up Frisbees by taking input as the rotation speed of the encoder And
  * for output giving ability to modify speed. Note: names with "_" are from the
  * program, no "_" are from RobotMap
  *
  * @author Malachi Loviska
  */
 public class ShooterInjector extends PIDSubsystem {
 
     Encoder shooter_Endcoder;
     Jaguar shooter_Jaguar;
     Solenoid shooter_Solenoid;
     /*
      * Initialize Jaguar, Encoder and Solenoid
      */
     private static final double Kp = 0.1;
     private static final double Ki = 0.0;
     private static final double Kd = 0.0;
 
     /**
      * Constructs the Jaguar, Encoder, and Solenoid and assigns ports from robot
      * map. It also starts the Encoder
      */
     public ShooterInjector() {
         super("ShooterJaguar", Kp, Ki, Kd);
 
         shooter_Solenoid = new Solenoid(RobotMap.shooterSolenoid);
         shooter_Jaguar = new Jaguar(RobotMap.shooterJaguar);
         shooter_Endcoder = new Encoder(RobotMap.shooterEndcoder[0],
                 RobotMap.shooterEndcoder[1]);
         
         //TODO: Set DistancePerPulse() for the Encoder!!!
         
         shooter_Endcoder.start();
     }
     
     public double getJagSpeed(){
         return shooter_Jaguar.get(); 
    }
     
     ///////////////////// Injection Functions /////////////////////////
     /*
      * Activate Solenoid if the Jaguar is on
      * 
      */
     public void activate() {
         if (shooter_Jaguar.get() != 0);
         {
             shooter_Solenoid.set(true);
         }
     }
 
     /*
      * Deactivate Solenoid if Jaguar is off.
      * 
      */
     public void deactivate() {
         shooter_Solenoid.set(false);
     }
 
     ///////////////////// PID Functions /////////////////////////
     
     
     /*
      * Get the value to be used for the PID Input
      * 
      * @return the rotation speed of encoder for use of Jaguar
      */
     protected double returnPIDInput() {
         return shooter_Endcoder.getRate();
 
     }
 
     /**
      *
      * Using the output from encoder to give to change the acceleration of the
      * Jaguar
      *
      * @retun encoder values to jaguar between 1 and -1
      */
     protected void usePIDOutput(double output) {
 
         double shoot = Utils.clamp(shooter_Jaguar.get(), 1, -1);
         shooter_Jaguar.set(output + shoot);
 
     }
     
     /**
      * Set state of PID Loop
      * 
      * @param pid Whether to enable PID loop or not
      */
     
     public void usePID(boolean pid) {
         if(pid) {
             this.enable();
         }
         else {
             this.disable();
         }
     }
     
     ///////////////////// Manual Functions /////////////////////////
     
     /**
      * 
      * Used for Manual control of the shooter.
      * Also turns of PID loop if running.
      * 
      * @param percent Value -1 to 1 for speed of the shooter.
      */
     public void setShooter(double percent) {
         this.usePID(false);
         shooter_Jaguar.set(Utils.clamp(percent, 1, -1));
     }
 
     public void initDefaultCommand() {
        setDefaultCommand(new ShooterCommand());
     }   
 
 }
