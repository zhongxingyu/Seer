 /* Currently managed by Tarun Sunkaraneni, and Ben Rose
  * This class manages all motor writing and managment. May need to deal with complex Gyro
  * input. 
  */
 package edu.ames.frc.robot;
 
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Victor;
 
 public class MotorControl {
        
     private static Victor A = new Victor(RobotMap.Apin);
     private static Victor B = new Victor(RobotMap.Bpin);
     private static Victor C = new Victor(RobotMap.Cpin);
    private static Relay Col = new Relay(5);
     private static Jaguar shoot = new Jaguar(4);
     static void drive(double[] mv){
         if(mv[0]<-1){
             mv[0] = -1;
         }
         if(mv[0]>1){
             mv[0] = 1;
         }
         if(mv[1]<-1){
             mv[1] = -1;
         }
         if(mv[1]>1){
             mv[1] = 1;
         }
         if(mv[2]<-1){
             mv[2] = -1;
         }
         if(mv[2]>1){
             mv[2] = 1;
         }
         A.set(mv[0]);
         B.set(mv[1]);
         C.set(mv[2]);
     }
     
     public void shooter(double power) {
         if (power < -1) {
             power = -1;
         }
         if (power > 1) {
             power = 1;
         }
         shoot.set(power);
         
     }
 
     /* Make sure the motors don't go full blast all the time */
     double[] setSpeedCap(double[] in) {
         for(int i=0;i<in.length;i++) {
             if(in[i] > RobotMap.speedcap) {
                 in[i] = RobotMap.speedcap;
             }
         }
         return in;
     }
     
     /* This converts the direction we want to go (from 0 to 1, relative to the robot's base)
      * and speed (from 0 to 1) directly to values for the three omni-wheeled motors.
      */
    public void direction(boolean dirc)
     {
        if (dirc)
         {
            Col.set(0.4);
         }
        if (!dirc)
         {
            Col.set(-0.4);
         }
     }
     
     double[] convertHeadingToMotorCommands(double direction, double speed, double pivot) {
         double[] motorvalue = new double[3];
         
         /* so, we'll define the direction we want to go as "forward". There are
          * 3 different points where only two motors will need to run (if the direction
          * is parallel to a motor's axle).
          */
         // 0 is what we define as the "front" motor - what we measure our heading angle from,
         // 1 is the motor one position clockwise from that, and
         // 2 is the motor one position counter-clockwise from 0.
         motorvalue[0] = speed * Math.sin(direction);
         motorvalue[1] = speed * Math.sin(direction - (2 * Math.PI / 3));
         motorvalue[2] = speed * Math.sin(direction + (2 * Math.PI / 3));
        
       if(pivot<0){
             motorvalue[0] =- pivot;
             motorvalue[1] =- pivot;
             motorvalue[2] =- pivot;
         }
         if(pivot>0){
             motorvalue[0] =+ pivot;
             motorvalue[1] =+ pivot;
             motorvalue[2] =+ pivot;
         }
         return motorvalue;
     }
 }
