 package robot;
 
 
 /**
  * Write a description of class LauncherPart here.
  * 
  * @author Sam Weiss
  * @version 0.1.0
  */
 public class LauncherPart extends Part
 {
     private int kind;       // projectile type
     private int rounds;     // number of rounds of ammunition
     private double radius;  // radius of projectile in cm
     private double mass;    // mass of projectile in kg
     private double speed;   // launch speed in meters/sec.
     
     public LauncherPart()
     {
         this.rounds = 0;
         this.radius = 0;
         this.mass = 0;
         this.speed = 0;
     }
     
     public LauncherPart setProjectileInfo(int kind, int rounds, double radius, double mass, double speed)
     {
         this.kind = kind;
         this.rounds = rounds;
         this.radius = radius;
         this.mass = mass;
         this.speed = speed;
         return this;
     }
     
     public double getRadius()
     {
         return this.radius;
     }
     
     public double getMass()
     {
         return this.mass;
     }
     
     public double getSpeed()
     {
         return this.speed;
     }
     
     public byte TxRx(byte data)
     {
         if (this.rounds > 0 && data != 0)
         {
             --this.rounds;
             this.robot.launchProjectile(this.kind, this.radius, this.mass, this.speed);
         }
         return 0;
     }
 }
