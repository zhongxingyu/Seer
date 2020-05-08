 package org.usfirst.frc2022.commands;
 
 import edu.wpi.first.wpilibj.buttons.JoystickButton;
 import edu.wpi.first.wpilibj.command.Command;
 import org.usfirst.frc2022.Joysticks.Xbox360;
 
 public class AutoAimCommand extends CommandBase {
 
     /*
      * The portion of the image that the box can be in
      */
     public final double middle = .01;
     public final double flat = 0.0;
     private double rotation;
     private double pitch;
     private double goal;
     private boolean on;
     private Xbox360 xboz;
 
     public AutoAimCommand() {
         requires(cam);
         requires(shooter);
         requires(shooterPitch);
         requires(shooterRotation);
     }
 
     protected void initialize() {
         shooter.enable();
        shooterPitch.disable();
        shooterRotation.disable();
        shooterPitch.setPitch(0);
        shooterRotation.setRotation(0);
         shooter.setSetpoint(0);
         xboz = oi.getXbawks();
         
     }
 
     protected void execute() {
         process(cam.analyze());
         if(on){
             if((goal == 0.0) || (goal == 1.0) || (goal == 2.0)){
                 shooter.setSetpoint(30);
                shooterPitch.setPitch(pitch);
                shooterRotation.setRotation(rotation);
             } else{
                shooter.setSetpoint(0);
             }
         } else {}
     }
     
     protected void end() {
         shooter.disable();
         shooterPitch.disable();
         shooterRotation.disable();
     }
 
     protected void interrupted() {
         shooter.disable();
         shooterPitch.disable();
         shooterRotation.disable();
     }
 
     protected void toggleState(){
         if(on){on = false;}
         if(!on){on = true;}
     }
 
     private void process(double[] analyze) {
         rotation = analyze[1];
         pitch = analyze[2];
         goal = analyze[0];
     }
 
     protected boolean isFinished() {
         return false;
     }
 }
