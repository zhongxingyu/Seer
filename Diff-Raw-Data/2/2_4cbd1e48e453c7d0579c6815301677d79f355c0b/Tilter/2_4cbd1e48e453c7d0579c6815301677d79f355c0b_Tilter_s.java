 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.stuy.subsystems;
 
 import com.sun.squawk.util.MathUtils;
 import edu.stuy.Constants;
 import edu.stuy.util.BoundedTalon;
 import edu.stuy.util.Gamepad;
 import edu.stuy.util.NetworkIO;
 import edu.wpi.first.wpilibj.ADXL345_I2C;
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.PIDController;
 import edu.wpi.first.wpilibj.PIDOutput;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 /**
  * 
  * @author kevin, abdullah, eric
  */
 
 public class Tilter {
     
     private static Tilter instance;
     private BoundedTalon leadscrew;
     
     private AnalogChannel pendulum; // potentiometer (angle measuring device)
     /**
      * Mount upside down, with the y-axis positive arrow pointed towards the
      * mouth of the shooter.
      */
     private ADXL345_I2C accel;
     
     private PIDController controller;
     private Encoder enc;
     private double initialLeadLength;
     private static NetworkIO net;
     
     private boolean isCVAiming = false;
     
     private Vector accelMeasurements;
     private Timer updateMeasurements;
     private Timer initialReadingTimer;
     private final int ACCEL_MEASUREMENT_SIZE = 10; //Number of measurements to average
     private final int ACCEL_UPDATE_PERIOD = 10; //Time between measurements. DO NOT USE ANY VALUE LESS THAN 10.
     private final int INITIAL_ANGLE_MEASUREMENT_DELAY = 1000;
     
     public static double UP_VAL = -1;
     public static double DOWN_VAL = 1;
     public static double STOP_VAL = 0;
     
     private Tilter() {
         leadscrew = new BoundedTalon(Constants.TILTER_CHANNEL, Constants.TILTER_UPPER_LIMIT_SWITCH_CHANNEL, Constants.TILTER_LOWER_LIMIT_SWITCH_CHANNEL);
         accel = new ADXL345_I2C(Constants.ACCELEROMETER_CHANNEL, ADXL345_I2C.DataFormat_Range.k2G);
         accelMeasurements = new Vector();
         updateAccel();        
         pendulum = new AnalogChannel(Constants.PENDULUM_CHANNEL);
         enc = new Encoder(Constants.LEADSCREW_ENCODER_A_CHANNEL, Constants.LEADSCREW_ENCODER_B_CHANNEL);
         enc.setDistancePerPulse(Constants.TILTER_DISTANCE_PER_PULSE);
         enc.start();
         Thread t = new Thread(new Runnable() {
             public void run() {
                 Tilter.net = new NetworkIO();
             }
         });
         t.start();
         controller = new PIDController(Constants.PVAL_T, Constants.IVAL_T, Constants.DVAL_T, enc, new PIDOutput() {
             public void pidWrite(double output) {
                 setLeadscrewMotor(output);
             }
         }); 
         controller.setPercentTolerance(0.01);
         controller.disable();
         updatePID();
         initialReadingTimer = new Timer();
         initialReadingTimer.schedule(new TimerTask() {
 
             public void run() {
                 initialLeadLength = calcLeadscrewLength(getAveragedAccelBasedAngle());
             }
         }, INITIAL_ANGLE_MEASUREMENT_DELAY);
     }
     
     public static Tilter getInstance() {
         if (instance == null) {
             instance = new Tilter();
         }
         return instance;
     }
     
     /**
      * Returns if shooter is at a specified angle
      * @param angle
      * @return 0 if shooter is at angle, 1 if above, -1 if below
      */
     public int isAtAngle(double angle) {
         return Math.abs(getLeadscrewBasedAngle() - angle) < 0.1 ? 0 : (getLeadscrewBasedAngle() - angle > 0 ? 1 : -1);
     }
     
     /**
      * Enables tilter PID controller.
      */
     public void enableAngleControl() {
         controller.enable();
     }
     
     /**
      * Disables tilter PID controller.
      */
     public void disableAngleControl() {
         controller.disable();
     }
     
     /**
      * Checks for CV aiming.
      * @return if CV aiming is being used
      */
     public boolean isCVAiming() {
         return isCVAiming;
     }
     
     public boolean isAtUpperBound() {
         return leadscrew.isFwdSwitchTriggered();
     }
     
     public boolean isAtLowerBound() {
         return leadscrew.isRevSwitchTriggered();
     }
     
     /**
      * Uses Smart Dashboard to update PID values.
      */
     public void updatePID() {
         double pVal;
         double iVal;
         double dVal;
         try {
             pVal = SmartDashboard.getNumber("Tilter P");
             iVal = SmartDashboard.getNumber("Tilter I");
             dVal = SmartDashboard.getNumber("Tilter D");
             controller.setPID(pVal, iVal, dVal);
         } catch (TableKeyNotDefinedException e) {
             SmartDashboard.putNumber("Tilter P", 0.0);
             SmartDashboard.putNumber("Tilter I", 0.0);
             SmartDashboard.putNumber("Tilter D", 0.0);
         }
     }
     
     /**
      * Sets angle based on a difference in angle to move.
      * @param deltaAngle relative angle in degrees
      */
     public void setRelativeAngle(double deltaAngle) {
         double initialAngle = getLeadscrewBasedAngle();
         double absoluteAngle = deltaAngle + initialAngle;
         setAbsoluteAngle(absoluteAngle);
     }
     
     /**
      * Sets angle by using the maths and then sets that setpoint distance.
      * @param angle angle in degrees
      */
     public void setAbsoluteAngle(double angle) {
         double leadScrewLength = calcLeadscrewLength(angle);
         double deltaLeadScrewLength = leadScrewLength - getLeadscrewLength();
         controller.setSetpoint(deltaLeadScrewLength + enc.getDistance());
     }
 
     /**
      * Set leadscrew motor speed.
      * @param speed 
      */
     public void setLeadscrewMotor(double speed){
         leadscrew.set(speed);
     }
     
     /**
      * Stops leadscrew motor.
      */
     public void stopLeadscrewMotor() {
         leadscrew.set(0);
     }
     
     public void reset() {
         stopLeadscrewMotor();
         isCVAiming = false;
         resetAccelMeasurements();
     }
     
      /**
      * Starts the update thread for the accelerometer.
      */
     public void updateAccel() {
         accelStop();
         updateMeasurements = new Timer();
         updateMeasurements.schedule(new TimerTask() {
             public void run() {
                 synchronized (Tilter.this) {
                     accelMeasurements.addElement(new Double(getInstantaneousAccelBasedAngle()));
                     if (accelMeasurements.size() > ACCEL_MEASUREMENT_SIZE) {
                         accelMeasurements.removeElementAt(0);
                     }
                 }
             }
         }, 0, ACCEL_UPDATE_PERIOD);
     }
     
     /**
      * Stops the accelerometer update thread.
      */
     public void accelStop() {
         if (updateMeasurements != null) {
             updateMeasurements.cancel();
         }
     }
     
     /**
      * Resets the accelerometer update thread.
      */
     public void resetAccelMeasurements() {
         accelMeasurements.removeAllElements();
     }
 
     /**
      * Gets the relative angle to the target from the Pi.
      * @return angle difference from the target
      */
     public double getCVRelativeAngle () {
         if (Tilter.net == null) {
             return Constants.CV_DEFAULT_VALUE;
         } 
         else {
             double relativeAngle = Tilter.net.getCurrent();
             return relativeAngle;
         }
     }
     
     /**
      * Gets X-axis acceleration of the shooter. This is not used on DESiree, as it is perpendicular to the side of the robot.
      * @return x-axis acceleration
      */
     public double getXAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kX);
     }
     
     /**
      * Gets Y-axis acceleration of the shooter. This is pointing in the direction of the shooter.
      * @return y-axis acceleration
      */
     public double getYAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kY);
     }
     
     /**
      * Gets Z-axis acceleration of the shooter. This is pointing perpendicular to the shooter.
      * @return z-axis acceleration
      */
     public double getZAcceleration() {
         return accel.getAcceleration(ADXL345_I2C.Axes.kZ);
     }
     
     /**
      * Gets the angle from the measurements of the last 10 accelerations.
      */
     public double getAveragedAccelBasedAngle() {
         if (accelMeasurements.isEmpty()) {
             return 0;
         }
         double sum = 0;
         double min = ((Double) accelMeasurements.elementAt(0)).doubleValue();
         double max = min;
         synchronized (this) {
             for (int i = 0; i < accelMeasurements.size(); i++) {
                 double measure = ((Double) accelMeasurements.elementAt(i)).doubleValue();
                 sum += measure;
                 min = (min < measure) ? min : measure;
                 max = (max > measure) ? max : measure;
             }
             if (accelMeasurements.size() >= 3) {
                 return (sum - min - max) / (accelMeasurements.size() - 2); //Removes the max and min values to get rid of any weird fluctuations
             }
             else {
                 return sum / accelMeasurements.size();
             }
         }
     }
     
     /**
     * Gets instantaneous angle of hte tilter directly from the accelerometer measurements.
      * @return the instantaneous angle read from the accelerometer in degrees
      */
     public double getInstantaneousAccelBasedAngle() {
         return MathUtils.atan(getYAcceleration() / -getZAcceleration()) * 180.0 / Math.PI;
     }
     
     /**
      * Uses pendulum potentiometer to find angle.
      * @return angle of the shooter
      */
     public double getPendulumOutput() {
         return 30 + (pendulum.getVoltage() * 6);
     }
     
     /**
      * Gets the leadscrew length by adding the initial to the distance the encoder has read.
      * @return the leadscrew length, from the base to the point of connection to the shooter
      */
     public double getLeadscrewLength() {
         return initialLeadLength + enc.getDistance();
     }
     
     public double getLeadscrewEncoderDistance() {
         return enc.getDistance();
     }
     
     /**
      * Sets the initial leadscrew length for when the shooter is all of the way up.
      */
     public void setInitialLeadscrewLength() {
         initialLeadLength = calcLeadscrewLength(Constants.TILTER_HIGHEST_ANGLE);
     }
     
     /**
      * Runs tilter until it is at angle
      * @param angle 
      */
     public void runTilterToAngle(double angle) {
         if(isAtAngle(angle) > 0) {
             while (!isAtLowerBound() && isAtAngle(angle) != 0) {
                 setLeadscrewMotor(Tilter.DOWN_VAL);
             }
         }
         else if(isAtAngle(angle) < 0) {
             while (!isAtUpperBound() && isAtAngle(angle) != 0) {
                 setLeadscrewMotor(Tilter.UP_VAL);
             }
         }
         stopLeadscrewMotor();
     }
     
     /**
      * Simple math method that squares a number.
      * @param x
      * @return the square of x
      */
     private double square(double x) {
         return x * x;
     }
     
     /**
      * Gets the leadscrew length given a specified angle.
      * ======== v(q) finds length from angle, using backwards from length to angle ========
      * v(q) = sqrt(x^2 + y^2 + z^2 - w^2 - 2sqrt(x^2 + y^2)zcos(q - arctan(y/x)) )
      * w = distance from leadscrew attachment to shooter attachment
      * v = leadscrew length
      * q = angle of shooter
      * z = distance from pivot to where leadscrew hits shooter
      * x = distance from pivot to base of base of leadscrew
      * y = height of the leadscrew
      * @param angle angle in degrees
      * @return the leadscrew length, from the base to the point of connection to the shooter
      */
     public double calcLeadscrewLength(double angle) {
         double heightSquared = square(Constants.LEADSCREW_HEIGHT);
         double baseSquared = square(Constants.DISTANCE_TO_LEADSCREW_BASE);
         double hypSquared = square(Constants.SHOOTER_DISTANCE_TO_LEADSCREW);
         double leadscrewWidthSquared = square(Constants.LEADSCREW_ASSEMBLY_WIDTH);
         angle *= Math.PI / 180;
         return Math.sqrt(baseSquared + heightSquared + hypSquared - leadscrewWidthSquared 
                 - 2 * Math.sqrt(baseSquared + heightSquared) * Constants.SHOOTER_DISTANCE_TO_LEADSCREW 
                 * Math.cos(angle - MathUtils.atan(Constants.LEADSCREW_HEIGHT / Constants.DISTANCE_TO_LEADSCREW_BASE)));
     }
     
     /**
      * Shooter angle as gotten from the leadscrew length.
      * ======== q(v) adds two angles ========
      * q(v) = atan(y/x) + acos( (z^2 + x^2 + y^2 - v^2 - w^2) / (2zsqrt(x^2 + y^2)) )
      * variables are defined above
      * @return shooter angle in degrees
      */
     public double getLeadscrewBasedAngle() {
         double leadscrewLength = getLeadscrewLength();
         double heightSquared = square(Constants.LEADSCREW_HEIGHT);
         double baseSquared = square(Constants.DISTANCE_TO_LEADSCREW_BASE);
         double hypSquared = square(Constants.SHOOTER_DISTANCE_TO_LEADSCREW);
         double leadscrewWidthSquared = square(Constants.LEADSCREW_ASSEMBLY_WIDTH);
         double angleRadians = MathUtils.atan(Constants.LEADSCREW_HEIGHT / Constants.DISTANCE_TO_LEADSCREW_BASE) + 
                MathUtils.acos((hypSquared + baseSquared + heightSquared - square(leadscrewLength) - leadscrewWidthSquared) / 
                (2 * Constants.SHOOTER_DISTANCE_TO_LEADSCREW * Math.sqrt(baseSquared + heightSquared)));
         return angleRadians * 180 / Math.PI; // Returns angle in degrees
     }
     
     /**
      * Manually allows for tilter control and aiming.
      * @param gamepad 
      */
     public void manualTilterControl(Gamepad gamepad) {
         if (gamepad.getBottomButton()) {
             isCVAiming = true;
             if (getCVRelativeAngle() != 694) {
                 runTilterToAngle(getCVRelativeAngle() + getLeadscrewBasedAngle());
             }
             else {
                 stopLeadscrewMotor();
             }
         }
         /*
         else if (gamepad.getTopButton()) {
             isCVAiming = false;
             runTilterToAngle(Constants.FEEDER_STATION_ANGLE);
         }
         * */
         else {
             isCVAiming = false;
             leadscrew.set(gamepad.getRightY());
         }
         printAngle();
     }
     
     /**
      * Prints angles to the SmartDashboard.
      */
     public void printAngle() {
 //        if (getCVRelativeAngle() != 694) {
             SmartDashboard.putNumber("CV Angle", getCVRelativeAngle());   
 //        }
 //        else {
 //            SmartDashboard.putString("CV Angle", "DRIPTO THE ANGLE'S SMOKING!");
 //        }
         SmartDashboard.putNumber("Absolute Angle", getAveragedAccelBasedAngle());
         SmartDashboard.putNumber("LS-Based Angle", getLeadscrewBasedAngle());
         SmartDashboard.putNumber("Instant Angle", getInstantaneousAccelBasedAngle());
     }
 }
