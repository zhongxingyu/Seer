 package de.engine.math;
 
 public class Vector
 {
     private boolean calcLength;
     private double length;
     
     private double x;
     private double y;
     
     public Vector()
     {
         x = 0.0;
         y = 0.0;
         calcLength = true;
         length = 0.0;
     }
         public Vector(double x, double y)
     {
         this.x = x;
         this.y = y;
         calcLength = false;
     }
     
     public double getX()
     {
         return x;
     }
     
     public double getY()
     {
         return y;
     }
     
     public void setX(double x)
     {
         this.x = x;
         calcLength = false;
         
     }
     
     public void setY(double y)
     {
         this.y = y;
         calcLength = false;
     }
     
     public void setPoint(double x, double y)
     {
         this.x = x;
         this.y = y;
         calcLength = false;
     }
     
     public double getLength()
     {
         if (!calcLength)
         {
             length = Util.distanceToOrigin(this);
             calcLength = true;
         }
         return length;
     }
     
     public Vector add(Vector v)
     {
         this.x += v.x;
         this.y += v.y;
         calcLength = false;
         return this;
     }
     
     public Vector add(double x, double y)
     {
         this.x += x;
         this.y += y;
         calcLength = false;
         return this;
     }
     
     public Vector minus(Vector v)
     {
         this.x -= v.x;
         this.y -= v.y;
         calcLength = false;
         return this;
     }
     
     public Vector minus(double x, double y)
     {
         this.x -= x;
         this.y -= y;
         calcLength = false;
         return this;
     }
     
     public Vector scale(double s)
     {
         x *= s;
         y *= s;
         return this;
     }
     
     public Vector getNormalVector()
     {
         return new Vector(-1 * y, x);
     }
     
     public Vector getUnitVector()
     {
        double scale = 1 / getLength();
         return Util.scale(this, scale);
     }
 }
