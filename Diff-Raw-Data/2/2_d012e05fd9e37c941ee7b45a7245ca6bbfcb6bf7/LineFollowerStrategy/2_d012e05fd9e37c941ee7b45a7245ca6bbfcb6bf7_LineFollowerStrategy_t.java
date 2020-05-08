 package strategies;
 
 import static robot.Platform.ENGINE;
 import static robot.Platform.LIGHT_SENSOR;
 import utils.Utils;
 
 public class LineFollowerStrategy extends Strategy {
 
     private int speed = 400;
     private static final double EXP_FACTOR = 1.0 / 60000;
     private static final double LINEAR_FACTOR = 0.6;
 
    public LineFollowerStrategy(final int motorSpeed) {
         if (motorSpeed < 0 || motorSpeed > 1000) {
             throw new IllegalArgumentException("motorSpeed out of range");
         }
         
         this.speed = motorSpeed;
     }
     
     protected void doInit() {
         LIGHT_SENSOR.setFloodlight(true);
     }
 
     protected void doRun() {
         final int error = 500 - LIGHT_SENSOR.getValue();
 
         final int linear = (int) (LINEAR_FACTOR * error);
         final int exponential = (int) (error * error * error * EXP_FACTOR);
         final int out = Utils.clamp(linear + exponential, -1000, 1000);
 
         System.out.println("err: " + error + " lin: " + linear + " exp: "
                 + exponential + " out: " + out);
 
         ENGINE.move(speed, out);
     }
     
     public int getSpeed() {
         return speed;
     }
     
     public void setSpeed(int motorSpeed) {
         if (motorSpeed < 0 || motorSpeed > 1000) {
             throw new IllegalArgumentException("motorSpeed out of range");
         }
         
         speed = motorSpeed;
     }
 }
