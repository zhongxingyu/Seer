 package robot;
 
 
 /**
  * Write a description of class MotorPart here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class MotorPart extends Part
 {
     private int maxSpeed = 120;   // in meters/sec.
     private int curSpeed;
     
     /**
      * Constructor for objects of class MotorPart
      */
     public MotorPart()
     {
         this.curSpeed = 0;
     }
     
     public int getMaxSpeed()
     {
         return this.maxSpeed;
     }
 
     public MotorPart setMaxSpeed(int maxSpeed)
     {
         this.maxSpeed = maxSpeed;
         return this;
     }
     
     public byte TxRx(byte data)
     {
        System.out.println(data);
         this.curSpeed = (0x00FF & (int)data);
         this.robot.setSpeed(this.curSpeed*this.maxSpeed/255);
         return 0;
     }
 }
