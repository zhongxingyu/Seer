 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm2013.commands.LEDcommands;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.command.Command;
 import storm2013.Robot;
 
 /**
  *
  * @author evan1026
  */
 public class SetModeMoving extends Command {
     private static final double PERIOD_SCALAR = 0.05;
     
     private Timer _timer = new Timer();
     private double _val = 0;
     
     public SetModeMoving(){
         requires(Robot.ledStrip);
     }
     
     public void execute(){
         double rate = 2*Math.PI*(Math.abs(Robot.driveTrain.getLeftSpeed()) + Math.abs(Robot.driveTrain.getRightSpeed())) / 2;
         if(rate == 0) {
             _val = Math.PI/2;
         } else {
            _val += rate*_timer.get()/PERIOD_SCALAR;
            _timer.reset();
             _val %= Math.PI*2;
         }
         double scalar = (Math.sin(_val)+1)/2;
         if(scalar <= 0.25) {
             scalar = 0;
         }
         int color = (int)(scalar*255);
         Robot.ledStrip.setColor(color,color,color);
     }
 
     protected void initialize() {
         _timer.reset();
         _timer.start();
     }
 
     protected boolean isFinished() {
         return false;
     }
 
     protected void end() {
         _timer.stop();
     }
 
     protected void interrupted() {}
 }
