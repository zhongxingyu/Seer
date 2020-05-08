 package storm2013.commands;
 
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.command.Command;
 import storm2013.Robot;
 import storm2013.commands.LEDcommands.SetModeSpinningUp;
 
 /**
  * Spins up the firing wheel.
  * @author evan1026
  */
 public class SpinUp extends Command {
     public static final double SPEED_NORMAL    = 3400,
                                SPEED_FULLCOURT = 4000; // tentative
     
     private final Timer _onTargetTimer = new Timer();
     private final Command lightCommand = new SetModeSpinningUp();
     private final double _speed;
 
     /**
      * Creates a new instance blahdy blahdy blah.
      */
     public SpinUp(double speed){
         requires(Robot.shooter);
         _speed = speed;
     }
     
     /**
      * 
      */
     public SpinUp() {
         this(SPEED_NORMAL);
     }
     
     /**
      * Initializes {@link Command}
      */
     protected void initialize() {
         Robot.shooter.getPIDController().setSetpoint(_speed);
        Robot.shooter.enable();
         _onTargetTimer.start();
         _onTargetTimer.reset();
         lightCommand.start();
     }
 
     /**
      * Executes over and over and such
      */
     protected void execute() {
         if(!Robot.shooter.onTarget()) {
             _onTargetTimer.reset();
         }
     }
 
     /**
      * Returns true when the shooter is on target
      */
     protected boolean isFinished() {
         return _onTargetTimer.get() > 0.3;
     }
 
     /**
      * Called when {@link Command} is done
      */
     protected void end() {
         lightCommand.cancel();
     }
 
     /**
      * Called when {@link Command} is interrupted
      */
     protected void interrupted() {
         end();
     }
 }
