 package robot;
 
 
 /**
  * Write a description of class MotorPart here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class MotorPart extends Part
 {
     private int maxSpeed;   // in meters/sec.
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
        System.out.println("data = " + data);
         this.curSpeed = (int)data;
         this.robot.setSpeed((this.curSpeed*this.maxSpeed)/255);
        System.out.println((this.curSpeed*this.maxSpeed)/255);
         return 0;
     }
 }
