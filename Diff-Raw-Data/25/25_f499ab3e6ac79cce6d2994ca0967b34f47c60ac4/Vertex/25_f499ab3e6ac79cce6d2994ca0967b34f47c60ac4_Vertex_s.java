 package com.chromabits.ugaacm.WarpDrive.render;
 
 /**
  * Created by Eduardo Trujillo <ed@chromabits.com> on 10/19/13.
  */
 public class Vertex {
 
     public final static int COORDS_PER_VERTEX = 3;
 
     private float x,y,z;
 
     public float[] getFloatArray(){
             float values[] = new float[3];
 
             values[0] = x;
             values[1] = y;
             values[2] = z;
 
             return values;
     }
 
     public Vertex(float x, float y, float z){
         this.x = x;
         this.y = y;
         this.z = z;
     }
 
     public float getX(){
         return x;
     }
 
     public float getY(){
         return y;
     }
 
     public float getZ(){
         return z;
     }
 
     public int getArraySize(){
         return COORDS_PER_VERTEX;
     }
 
    public static int getVertexStride(){
        return Float.SIZE * COORDS_PER_VERTEX;
     }
 }
