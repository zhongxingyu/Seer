 package com.edinarobotics.zed.subsystems;
 
 import com.edinarobotics.utils.commands.MaintainStateCommand;
 import com.edinarobotics.utils.subsystems.Subsystem1816;
 import edu.wpi.first.wpilibj.command.Command;
 
 public class DrivetrainStrafe extends Subsystem1816 {
     private double magnitude;
     private double direction;
     private Drivetrain drivetrain;
     
     public DrivetrainStrafe(Drivetrain drivetrain) {
         super("DrivetrainStrafe");
         this.drivetrain = drivetrain;
     }
     
     public void mecanumPolarStrafe(double magnitude, double direction) {
         this.magnitude = magnitude;
         this.direction = direction;
        update();
     }
     
     public void update() {
         drivetrain.mecanumPolarStrafe(magnitude, direction);
     }
 
     public void initDefaultCommand(){
         setDefaultCommand(new MaintainStateCommand(this));
     }
     
     
     public void setDefaultCommand(Command command){
        if(getDefaultCommand() != null){
            super.getDefaultCommand().cancel();
        }
         super.setDefaultCommand(command);
     }
 }
