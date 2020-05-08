 package robot.subsystems;
 
 import edu.wpi.first.wpilibj.DoubleSolenoid;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import robot.Constants;
 import robot.ParsableDouble;
 import robot.Pneumatic;
 import robot.commands.HopperCommand;
 
 public class HopperSubsystem extends Subsystem {
     
     Pneumatic stackDropper = new Pneumatic(new DoubleSolenoid(Constants.SOLENOID_STACK_DROPPER_ONE, Constants.SOLENOID_STACK_DROPPER_TWO));
     Pneumatic stackHolder = new Pneumatic(new Solenoid(Constants.SOLENOID_STACK_HOLDER));
     Pneumatic shooterLoader = new Pneumatic(new DoubleSolenoid(Constants.SOLENOID_SHOOTER_LOADER_ONE, Constants.SOLENOID_SHOOTER_LOADER_TWO));
 
     public static ParsableDouble RELEASE_DELAY = new ParsableDouble("release_delay", 1);
     public static ParsableDouble STACK_HOLD_DELAY = new ParsableDouble("stack_hold_delay", 0.5);
     public static ParsableDouble STACK_RELEASE_DELAY = new ParsableDouble("stack_release_delay", 0.5);
     public static ParsableDouble FINISH_DELAY = new ParsableDouble("finish_delay", 0.5);
     
     boolean requestRelease = false;
     
     double lastReleaseTime = 0.0;
     double stackHoldTime = 0.0;
     double stackReleaseTime = 0.0;
     double finishTime = 0.0;
     
     public void initDefaultCommand() {
         setDefaultCommand(new HopperCommand());
     }
     
     public void reset() {
         stackDropper.set(true);
         stackHolder.set(false);
         shooterLoader.set(false);
     }
     
    public void update(boolean requestShot) {
         double now = Timer.getFPGATimestamp();
         
        if(requestShot && now - lastReleaseTime > RELEASE_DELAY.get()) {
            requestRelease = !requestRelease ? true : requestRelease;
         }
         
         if(requestRelease) {
             //Record the time we start holding the stack
             stackHoldTime = !stackHolder.get() ? now : stackHoldTime;
 
             //Hold the stack
             stackHolder.set(true);
             
             //If we've waited long enough after we start holding the stack
             if(now - stackHoldTime > STACK_HOLD_DELAY.get()) {
                 //Drop the stack and record the time
                 stackReleaseTime = now;
                 stackDropper.set(false);
             }
             
             if(now - stackReleaseTime > STACK_RELEASE_DELAY.get()) {
                 //When we've left enough time for the stack to drop, reset the dropper and load the shooter
                 finishTime = now;
                stackDropper.set(true);
                 shooterLoader.set(true);
             }
             
             if(now - finishTime > FINISH_DELAY.get()) {
                 lastReleaseTime = now;
                 //If we've waited long enough for the dropper and shooterLoader to do their stuff, then finish the release sequence
                 requestRelease = false;
                reset();
             }
         } else {
             //Reset all pneumatics
             reset();
         }
     }
 }
