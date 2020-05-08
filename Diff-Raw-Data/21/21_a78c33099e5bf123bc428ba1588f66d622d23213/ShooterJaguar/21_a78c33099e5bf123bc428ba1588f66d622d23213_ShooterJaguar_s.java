 
 package org.usfirst.frc2022.subsystems;
 
 import edu.wpi.first.wpilibj.SolenoidBase;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.SensorBase;
 import edu.wpi.first.wpilibj.command.PIDSubsystem;
 import edu.wpi.first.wpilibj.Encoder.PIDSourceParameter;
 import org.usfirst.frc2022.RobotMap;
 import com.sun.squawk.util.Arrays;
 import edu.wpi.first.wpilibj.*;
 import org.usfirst.frc2022.Utils;
 
 /**
  *This code is used to control the speed of Jaguars controlling the shooter for
  * picking up Frisbees by taking input as the rotation speed of the encoder
  * And for output giving ability to modify speed.
  * Note: names with "_" are from the program, no "_" are from RobotMap
  * @author Malachi
  */
 public class ShooterJaguar extends PIDSubsystem {  
    
     Encoder shooter_Endcoder;
     Jaguar shooter_Jaguar;
     Solenoid shooter_Solenoid;
      /*
      * Initialize Jaguar and Encoder
      */
     private static final double Kp = 0.1;
     private static final double Ki = 0.0;
     private static final double Kd = 0.0; 
     
     public ShooterJaguar () {
         super("ShooterJaguar", Kp, Ki, Kd);
         
         shooter_Solenoid = new Solenoid(RobotMap.shooterSolenoid );
         shooter_Jaguar = new Jaguar(RobotMap.shooterJaguar);
         shooter_Endcoder = new Encoder(RobotMap.shooterEndcoder[0],
                RobotMap.shooterEndcoder[1]);
         shooter_Endcoder.start(); 
        
        /*
        * Constructs the Jaguar and Encoder and assigns ports from
        * robot map. It also starts the Encoder
        */
     }
     
    
     public void activate(){
      
    if  (shooter_Jaguar.get()!= 0);{
         shooter_Solenoid.set(true);
     }
 
 }
     public void deactivate(){
        shooter_Solenoid.set(false); 
     }
     
     public void initDefaultCommand() {
         
     }
     
     protected double returnPIDInput() {
        return shooter_Endcoder.getRate();
         /*
          * returns the rotation speed of encoder for use of Jaguar
          */
     }
     protected void usePIDOutput(double output) {
        
         double shoot = Utils.clamp(shooter_Jaguar.get(),1,-1);
       shooter_Jaguar.set(output+shoot);
   
         /**
          * 
           * Using the output from encoder to give to change 
           * the acceleration of the Jaguar
           *  @retun encoder values to jaguar  between 1 and -1
           */
       
     }
 }
