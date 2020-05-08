 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storm2013.commands.LEDcommands;
 
 import storm2013.Robot;
 
 /**
  *
  * @author evan1026
  */
 public class SetModeSpinningUp extends FlashOnAndOff {
     public SetModeSpinningUp(){
         super(Color.SPINNING_UP);
     }
     
     private static final double PERIOD_SCALAR = 1.0/2500;
 
     public void execute() {
        setPeriod(Robot.shooter.getPIDController().getError()*PERIOD_SCALAR);
         super.execute();
     }
     
     
 }
