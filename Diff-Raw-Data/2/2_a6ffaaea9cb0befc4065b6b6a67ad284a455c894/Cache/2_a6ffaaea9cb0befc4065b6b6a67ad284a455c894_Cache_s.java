 import java.util.*;
 
 public class Cache {
     private Func f;
 
     private int xBegin =   0;
     private int xEnd   = +10;
     //======================|
     private int zBegin =   0;
     private int zEnd   = +10;
 
     private float[][]   values;
     private float[][][] normals;
     private float[][][] colors;
 
     public Cache(Func f) {
         this.f = f;
 
         int xSize = Math.abs(xBegin - xEnd) + 1;
         int zSize = Math.abs(zBegin - zEnd) + 1;
 
         Util.ensure(xEnd > xBegin);
         Util.ensure(zEnd > zBegin);
 
         values  = new float[xSize][zSize];
         normals = new float[xSize][zSize][];
         colors  = new float[xSize][zSize][];
 
         fillInValues();
 
         Debug.println(Arrays.deepToString(values));
     }
 
     private void fillInValues() {
         for (int x=xBegin; x <= xEnd; ++x) {
             for (int z=zBegin; z <= zEnd; ++z) {
                 int xIndex = mapIntoZeroBasedRange(x, xBegin);
                 int zIndex = mapIntoZeroBasedRange(z, zBegin);
 
                 float   thisValue  = f. valueAt(x, z);
                 float[] thisNormal = f.normalAt(x, z);
                 float[] thisColor  = colorAt(thisValue);
 
                 values [xIndex][zIndex] = thisValue;
                 normals[xIndex][zIndex] = thisNormal;
                 colors [xIndex][zIndex] = thisColor;
             }
         }
     }
 
    public float[] colorAt(int y) {
         return new float[] {1, 1, 1, 1};
     }
 
     int mapIntoZeroBasedRange(int n, int begin) {
         return n - begin;
     }
 }
