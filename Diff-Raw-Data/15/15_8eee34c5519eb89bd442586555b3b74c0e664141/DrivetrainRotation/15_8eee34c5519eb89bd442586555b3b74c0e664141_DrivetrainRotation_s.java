 package com.edinarobotics.zed.subsystems;
 
 import com.edinarobotics.utils.commands.MaintainStateCommand;
 import com.edinarobotics.utils.subsystems.Subsystem1816;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class DrivetrainRotation extends Subsystem1816 {
     private double rotation;
     private Drivetrain drivetrain;
     
     public DrivetrainRotation(Drivetrain drivetrain) {
         super("DrivetrainRotation");
         this.drivetrain = drivetrain;
     }
     
     public void mecanumPolarRotate(double rotation) {
         this.rotation = rotation;
     }
     
     public void update() {
         drivetrain.mecanumPolarRotation(rotation);
     }
 
     public void initDefaultCommand(){
         setDefaultCommand(new MaintainStateCommand(this));
     }
     
     
     public void setDefaultCommand(Command command){
         super.setDefaultCommand(command);
     }
     
 }
