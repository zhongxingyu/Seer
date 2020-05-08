 package Identitaet;
 
 public class Particle3D {
     public float x;
     public float y;
     public float z;
     public float velX;
     public float velY;
     public float velZ;
    public float dummy1;
    public float dummy2;
 
     public Particle3D(float x, float y, float z, float velX, float velY, float velZ) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.velX = velX;
         this.velY = velY;
         this.velZ = velZ;
         this.dummy1 = 0;
         this.dummy2 = 0;
     }
 }
