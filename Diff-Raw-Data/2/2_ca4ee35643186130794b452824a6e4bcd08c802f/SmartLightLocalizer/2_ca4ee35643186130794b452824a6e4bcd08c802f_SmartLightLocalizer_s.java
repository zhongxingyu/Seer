 package ca.mcgill.dpm.winter2013.group6.localization;
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.Sound;
 import ca.mcgill.dpm.winter2013.group6.navigator.Navigator;
 import ca.mcgill.dpm.winter2013.group6.odometer.Odometer;
 import ca.mcgill.dpm.winter2013.group6.util.Coordinate;
 
 /**
  * A {@link Localizer} implementation that uses a {@link LightSensor} for
  * performing its localization.
  * 
  * @author Arthur Kam
  * 
  */
 public class SmartLightLocalizer extends LightLocalizer {
   private final double LIGHT_SENSOR_DISTANCE = 11.8;
   private Coordinate coordinates;
   private int count = 0;
 
   /**
    * The constructor the smart light localizer class.
    * 
    * @param odometer
    *          The {@link Odometer} we'll be reading from.
    * @param navigator
    *          The navigator class that will be used by the localizer.
    * @param lightSensor
    *          The {@link LightSensor} object.
    * @param coordinates
    *          The coordinates that the localizer will think it is localizing in
    *          reference to (must be an intersetction)
    */
   public SmartLightLocalizer(Odometer odometer, Navigator navigator, LightSensor lightSensor,
       Coordinate coordinates) {
     super(odometer, navigator, lightSensor, 1);
     this.coordinates = coordinates;
   }
 
   public SmartLightLocalizer(Odometer odometer, Navigator navigator, LightSensor lightSensor) {
     super(odometer, navigator, lightSensor, 1);
     this.coordinates = new Coordinate(0, 0);
   }
 
   public void setCoordinates(Coordinate coordinates) {
     if (coordinates != null) {
 
       this.coordinates = coordinates;
     }
   }
 
   @Override
   public void localize() {
     if (3 <= count) {
       Sound.beepSequence();
       count = 0;
       return;
     }
     navigator.face(45);
     odometer.setPosition(new double[] { 0, 0, 0 }, new boolean[] { true, true, true });
     lightSensor.setFloodlight(true);
     int lineCounter = 0;
 
     double[] raw = new double[4];
 
     // Filter the light sensor
     try {
       Thread.sleep(300);
     }
     catch (InterruptedException e) {
     }
     // Rotate and clock the 4 grid lines
     calibrateSensorAverage();
 
     navigator.setMotorRotateSpeed(-robot.getRotateSpeed() - 150);
 
     // Detect the four lines
     double[] heading = new double[2];
     odometer.getDisplacementAndHeading(heading);
     double startingAngle = heading[1];
     double currAngle = startingAngle;
     boolean error = false;
 
     while (lineCounter < 4) {
 
       if (blackLineDetected()) {
         Sound.beep();
         raw[lineCounter] = odometer.getTheta();
         odometer.getDisplacementAndHeading(heading);
         currAngle += heading[1];
         // its negative since we are rotating the negative direction
        if (currAngle + 360 < startingAngle) {
           Sound.buzz();
           error = true;
           break;
         }
         lineCounter++;
         try {
           // sleeping to avoid counting the same line twice
           Thread.sleep(150);
         }
         catch (InterruptedException e) {
         }
       }
     }
     // Stop the robot
     navigator.stop();
 
     // write code to handle situations where it wouldn't work
     // which means it has rotated for 360 degrees but havent scanned all 4 lines
     // yet
 
     if (error) {
 
       // seriously i cant fix it at this stage...gg
       if (lineCounter == 0) {
         navigator.face(45);
         navigator.travelStraight(15);
         count++;
         localize();
         return;
       }
       // not very likely but here is an attempt to fix it
       else if (lineCounter == 1) {
         // move to the line
         raw[0] += 180;
         navigator.turnTo(raw[0]);
         navigator.travelStraight(LIGHT_SENSOR_DISTANCE);
         count++;
         // calling it again here to try to localize
         localize();
       }
       // two causes in this case
       // one is one axis is off
       // the other is both axis is off
       else if (lineCounter == 2) {
         // if the lines are opposite of each other
         if (Math.abs(raw[1] - raw[0]) > 175) {
           navigator.face(raw[0]);
           navigator.travelStraight(10);
         }
         else {
           navigator.face(raw[1] - raw[0] + 180 + 45);
           navigator.travelStraight(10);
         }
         count++;
         localize();
       }
       else if (lineCounter == 3) {
         return;
       }
       return;
     }
     // happens if it is displaced too far right from the y axis
     if (raw[0] > 60) {
       double swap = raw[1];
       double swap2 = raw[2];
       raw[1] = raw[0];
       raw[2] = swap;
       swap = raw[3];
       raw[3] = swap2;
       raw[0] = swap;
 
     }
     // formula modified from the tutorial slides
     double thetaX = (raw[3] - raw[1]) / 2;
     double thetaY = (raw[2] - raw[0]) / 2;
     double newX = -LIGHT_SENSOR_DISTANCE * Math.cos(Math.toRadians(thetaY));
     double newY = -LIGHT_SENSOR_DISTANCE * Math.cos(Math.toRadians(thetaX));
     double newTheta = 180 + thetaX - raw[3];
     newTheta += odometer.getTheta();
 
     odometer.setPosition(new double[] {
         newX + coordinates.getX(),
         newY + coordinates.getY(),
         newTheta }, new boolean[] { true, true, true });
     count = 0;
 
     // travel to the coordinate
     navigator.face(0);
     navigator.travelStraight(-newY);
     navigator.face(90);
     navigator.travelStraight(-newX);
 
     Sound.twoBeeps();
 
     return;
   }
 
   @Override
   public void run() {
     localize();
   }
 }
