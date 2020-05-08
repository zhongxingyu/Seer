 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 public class GlobalState {
 
     // Is driving enabled?
     private ToggleButtonState drive = new ToggleButtonState(ToggleButtonState.OFF);
     // Are we driving backwards
     private ToggleButtonState driveBackwards = new ToggleButtonState(ToggleButtonState.OFF);
     // Is balance mode enabled
     private ToggleButtonState balanceMode = new ToggleButtonState(ToggleButtonState.OFF);
     // Is ball handling enabled
     private ToggleButtonState ballHandling = new ToggleButtonState(ToggleButtonState.OFF);
     // Are we shooting or loading
     private ToggleButtonState shootMode = new ToggleButtonState(ToggleButtonState.OFF);
     // Are we running the autoshoot sequence?
     private boolean autoshoot = false;
     // Are we running the autoRampKnockdown sequence?
     private boolean autoknockdown = false;
 
     public boolean isAutoknockdown() {
         return autoknockdown;
     }
 
     public void setAutoknockdown(boolean autoknockdown) {
         this.autoknockdown = autoknockdown;
     }
     // Do we have the target in sight?
     private boolean targetVisible = false;
     // Track loaded balls
     private int ballsInControl = 0;
     private int ballsInQueue = 0;
     // Is there a ball at the ready-to-shoot (elevator top) switch?
     private boolean readyToShoot = false;
     // Is there a ball at the the ready-to-queue (elevator bottom) switch?
     private boolean readyToQueue = false;
     // Is there a ball at the ready-to-dequeue (elevator middle) switch?
     private boolean readyToDequeue = false;
     // Which way is down?
     private double gravity = 0;
     // Which way is north?
     private double heading = 0;
     // Reference voltage for analog sensors
     private double vin = 0;
     // Hood angle (as voltage ratio)
     private double hoodAngle = 0;
     // Rangefinder reading (in inches)
     private int distance = 0;
     // Current rotational rate of the shooter wheel (in blips/second)
     private double shooterRate = 0;
     private double azimuth = 0;
     private double cameraDistance = 0;
     private double targetAngle = 0;
 
     public double getTargetAngle() {
         return targetAngle;
     }
 
     public void setTargetAngle(double targetAngle) {
         this.targetAngle = targetAngle;
     }
 
     public double getAzimuth() {
         return azimuth;
     }
 
     public void setAzimuth(double azimuth) {
         this.azimuth = azimuth;
     }
 
     public double getCameraDistance() {
         return cameraDistance;
     }
 
     public void setCameraDistance(double cameraDistance) {
         this.cameraDistance = cameraDistance;
     }
     private double accelX = 0;
 
     public double getAccelX() {
         return accelX;
     }
 
     public void setAccelX(double accelX) {
         this.accelX = accelX;
     }
 
     public double getAccelY() {
         return accelY;
     }
 
     public void setAccelY(double accelY) {
         this.accelY = accelY;
     }
     private double accelY = 0;
 
     public double getHoodAngle() {
         return hoodAngle;
     }
 
     public void setHoodAngle(double hoodAngle) {
         this.hoodAngle = hoodAngle;
     }
 
     public int getBallsInQueue() {
         return ballsInQueue;
     }
 
     public void queuedBall() {
         ballsInQueue++;
     }
 
     public void dequeuedBall() {
         ballsInQueue--;
     }
 
     public void setDriveEnabled(boolean enabled) {
         if (enabled) {
             drive.set(ToggleButtonState.ON);
         } else {
             drive.set(ToggleButtonState.OFF);
         }
     }
 
     public void updateDriveEnabled(boolean pressed) {
         drive.update(pressed);
     }
 
     public boolean isDriveEnabled() {
         return drive.isOn();
     }
 
     public void updateDriveBackwards(boolean pressed) {
         driveBackwards.update(pressed);
     }
 
     public boolean isDriveBackwards() {
         return driveBackwards.isOn();
     }
 
    public void setBallHandlingEnabled(boolean enable) {
        if (enable) {
            ballHandling.set(ToggleButtonState.ON);
        } else {
            ballHandling.set(ToggleButtonState.OFF);
        }
    }

     public void updateBallHandlingEnabled(boolean pressed) {
         ballHandling.update(pressed);
     }
 
     public boolean isBallHandlingEnabled() {
         return ballHandling.isOn();
     }
 
     public void updateShootMode(boolean pressed) {
         shootMode.update(pressed);
     }
 
     public void setShootMode(boolean enable) {
         if (enable) {
             shootMode.set(ToggleButtonState.ON);
         } else {
             shootMode.set(ToggleButtonState.OFF);
         }
     }
 
     public boolean isShootMode() {
         return shootMode.isOn();
     }
 
     public void updateBalanceEnabled(boolean pressed) {
         balanceMode.update(pressed);
     }
 
     public void setBalanceEnabled(boolean enable) {
         if (enable) {
             balanceMode.set(ToggleButtonState.ON);
         } else {
             balanceMode.set(ToggleButtonState.OFF);
         }
 
     }
 
     public boolean isBalanceEnabled() {
         return balanceMode.isOn();
     }
 
     public void setAutoshoot(boolean enable) {
         autoshoot = enable;
         SmartDashboard.putBoolean("Autoshoot Enabled", isAutoshootEnabled());
     }
 
     public boolean isAutoshootEnabled() {
         return autoshoot;
     }
 
     public void setTargetVisible(boolean visible) {
         this.targetVisible = visible;
     }
 
     public boolean targetVisible() {
         return this.targetVisible;
     }
 
     public boolean readyToShoot() {
         return this.readyToShoot;
     }
 
     public boolean readyToQueue() {
         return this.readyToQueue;
     }
 
     public boolean readyToDequeue() {
         return this.readyToDequeue;
     }
 
     public int ballsInControl() {
         return this.ballsInControl;
     }
 
     public boolean canLoadMoreBalls() {
         if (ballsInControl() < RobotMap.MAX_BALLS) {
             return true;
         }
         return false;
     }
 
     public void loadedBall() {
         this.ballsInControl++;
         SmartDashboard.putInt("Balls in control", ballsInControl);
     }
 
     public void unloadedBall() {
         this.ballsInControl--;
         SmartDashboard.putInt("Balls in control", ballsInControl);
     }
 
     public void setGravity(double gravity) {
         this.gravity = gravity;
     }
 
     public double getGravity() {
         return gravity;
     }
 
     public void setHeading(double heading) {
         this.heading = heading;
     }
 
     public double getHeading() {
         return heading;
     }
 
     public void setReadyToQueue(boolean ready) {
         this.readyToQueue = ready;
         SmartDashboard.putBoolean("Ready to Queue", readyToQueue);
     }
 
     public void setReadyToDequeue(boolean ready) {
         this.readyToDequeue = ready;
         SmartDashboard.putBoolean("Ready to Dequeue", readyToDequeue);
     }
 
     public void setReadyToShoot(boolean ready) {
         this.readyToShoot = ready;
         SmartDashboard.putBoolean("Ready to Shoot", readyToShoot);
     }
 
     public double getVin() {
         return vin;
     }
 
     public void setVin(double vin) {
         this.vin = vin;
     }
 
     public double getDistance() {
         return distance;
     }
 
     public void setDistance(int distance) {
         this.distance = distance;
     }
 
     public double getShooterRate() {
         return shooterRate;
     }
 
     public void setShooterRate(double rate) {
         this.shooterRate = rate;
     }
 
     public void incrementBallCount() {
         ballsInControl++;
     }
 
     public void deincrementBallCount() {
         ballsInControl--;
     }
 }
