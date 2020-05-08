 package arpong.logic.primitives;
 
 public class Vector {
     private float x;
     private float y;
 
     public Vector(float x, float y) {
         this.x = x;
         this.y = y;
     }
 
     public float getY() {
         return y;
     }
 
     public float getX() {
         return x;
     }
 
     public Vector plus(Vector diff) {
         return new Vector(getX() + diff.getX(),
                           getY() + diff.getY());
     }
 
     public Vector minus(Vector diff) {
         return new Vector(getX() - diff.getX(),
                           getY() - diff.getY());
     }
 
     public float dot(Vector v) {
        return this.getX() * v.getX() + this.getY() * v.getY();
     }
 
     public static float abs(Vector vector) {
         return vector.dot(vector);
     }
 }
