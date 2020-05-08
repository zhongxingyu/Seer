 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import edu.stuy.Constants;
 import edu.stuy.util.Gamepad;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 
 /**
  *
  * @author R4D4, eric
  */
 public class Shooter {
 
     private static Shooter instance;
     private Victor shooter;
     private DigitalInput hopperSensor;
     
     /**
      * Sets the piston to the "reset" state.
      */
     private Solenoid hopperInSolenoid;
     
     /**
      * Sets the piston to the "launched" state.
      */
     private Solenoid hopperOutSolenoid;
     
     private double lastOutTime = 0.0;
     private double lastInTime = 0.0;
     private boolean firstShot;
     private boolean isShooting;
     private boolean lastSemiAutoShootButtonState;
     private boolean pistonExtended;
     private boolean lastShooterToggleButtonState;
     
     // Time in seconds to allow launcher to shoot and reset before changing its state again
     public static final double HOPPER_OUT_TIME = 1;
     public static final double HOPPER_IN_TIME = 1;
     
     private Shooter() {
         shooter = new Victor(Constants.SHOOTER_CHANNEL);
         hopperSensor = new DigitalInput(Constants.HOPPER_SENSOR_CHANNEL);
         hopperInSolenoid = new Solenoid(Constants.HOPPER_IN_CHANNEL);
         hopperOutSolenoid = new Solenoid(Constants.HOPPER_OUT_CHANNEL);
         firstShot = true;
         isShooting = false;
         pistonExtended = false;
         lastSemiAutoShootButtonState = false;
     }
     
     public static Shooter getInstance() {
         if (instance == null) {
             instance = new Shooter();
         }
         return instance;
     }
     
     public void setSpeed(double speed) {
         shooter.set(speed);
     }
     
     public void runShooterOut() {
         shooter.set(1);
     }
     
     public void runShooterIn() {
         shooter.set(-1);
     }
     
     public void stop() {
         shooter.set(0);
     }
     
     public boolean isShooterRunning() {
         return isShooting;
     }
     
     public boolean isHopperNotEmpty() {
         return hopperSensor.get();
     }
     
     /**
      * Retracts the piston to launch the disc.
      */
     private void pistonLaunch() {
         hopperInSolenoid.set(false);
         hopperOutSolenoid.set(true);
     }
     
     /**
      * Extends the piston to reset.
      */
     public void pistonReset() {
         hopperOutSolenoid.set(false);
         hopperInSolenoid.set(true);
     }
     
     /**
      * Runs continuously to update the state of the piston.
      */
     public void runPistonLogic() {
         SmartDashboard.putBoolean("Has piston finished resetting", hasPistonFinishedResetting());
         SmartDashboard.putBoolean("Piston extended", pistonExtended);
         SmartDashboard.putNumber("lastOutTime", lastOutTime);
         SmartDashboard.putNumber("lastInTime", lastInTime);
         double time = Timer.getFPGATimestamp();
         if (!hasPistonFinishedResetting() && !pistonExtended) { // Piston is still retracting; let it finish
             pistonReset();
         }
         else if (time - lastOutTime < HOPPER_OUT_TIME) { // Piston recently extended or is about to be extended; stay extended until PISTON_EXTEND_TIME has passed
             firstShot = false;
             pistonLaunch();
         }
         else if (pistonExtended) { // Has been extended for more than PISTON_EXTEND_TIME; start retracting
             lastInTime = time;
             pistonReset();
             pistonExtended = false;
         }
         else { // Nothing has happened for a while; stay retracted
             pistonReset();
         }
     }
     
     /**
      * Returns whether the piston has had ample time to retract since the last shot.
      * @return Whether the piston is ready to fire
      */
     private boolean hasPistonFinishedResetting() {
         return Timer.getFPGATimestamp() - lastInTime >= HOPPER_IN_TIME && (lastInTime > lastOutTime || firstShot);
     }
     
     /**
      * Start extending the piston for PISTON_EXTEND_TIME seconds.
      * runPistonLogic() must be running in teleopPeriodic() for this to actually
      * extend the piston.
      */
     public void firePiston() {
         pistonExtended = true;
         lastOutTime = Timer.getFPGATimestamp();
     }
     
     public void fireAutoUntilEmpty() {
        if (hasPistonFinishedResetting() && isShooting) {
             firePiston();
         }
     }
     
     public void manualShooterControl(Gamepad gamepad) {
         /* Toggle shooter on/off */
         if (gamepad.getDPadRight() && !lastShooterToggleButtonState) {
             isShooting = !isShooting;
         }
         lastShooterToggleButtonState = gamepad.getDPadRight();
         
         if (gamepad.getDPadDown()) { // Reverse only when button is held
             runShooterIn();
         } else if (isShooting) {
             runShooterOut();
         } else {
             stop();
         }//
         
         /* Full auto shooting */
        if (gamepad.getTopButton() && hasPistonFinishedResetting() && isShooting) {
             firePiston();
         }
         
         /* Semi-automatic shooting */
        if (gamepad.getRightBumper() && !lastSemiAutoShootButtonState && hasPistonFinishedResetting() && isShooting) { //semi-auto
             firePiston();
         }
         lastSemiAutoShootButtonState = gamepad.getRightBumper();
     }
 }
