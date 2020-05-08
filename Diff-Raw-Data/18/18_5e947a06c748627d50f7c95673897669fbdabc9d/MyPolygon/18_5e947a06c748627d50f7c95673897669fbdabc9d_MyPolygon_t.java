 package src;
 
 import java.awt.*;
 import java.lang.*;
 import java.util.*;
 import java.awt.geom.Line2D;
 
 public class MyPolygon extends Polygon {
 
     public static final long serialVersionUID = 24362462L;
     public Polygon Elevated;
     public boolean elevated = false;
     private double tempX[] = new double[4];
     private double tempY[] = new double[4];
 
     public MyPolygon() {
     }
 
     public MyPolygon(int x[],int y[],int n) {
     }
 
     /////////////////////////////////////////////////////////////
 
     //This is a convenience method for ensuring that tempX and tempY
     //grow in an extensible manner.  If newSize is greater than the 
     //current capacity then tempX and tempY are reallocated to double the 
     //until they reach the required capacity.  This reallocation includes 
     //copying all data to the new location.
     private void resizeTemp(int newSize)
     {
         assert(tempX.length == tempY.length);
         while (newSize > tempX.length)
         {
             //Double the capacity of the array
             //Make more space
             double [] copy_x=new double[tempX.length*2];
             double [] copy_y=new double[tempX.length*2];
             //Copy data across
             System.arraycopy(tempX, 0, copy_x, 0, tempX.length);
             System.arraycopy(tempY, 0, copy_y, 0, tempX.length);
             //Swing the references back to the original object
             tempX=copy_x;
             tempY=copy_y;
            
             //System.out.println("Capacity increased to "+tempX.length);
         }
         assert(tempX.length == tempY.length);
     }
 
     public void elevateOnce() {
       //Input comes in the form of a Java Polygon (points are stored as integers)
       //Output is also in the form of a Java Polygon called Elevated
       //See http://download.oracle.com/javase/1.5.0/docs/api/java/awt/Polygon.html for Polygon details
       //As degree elevation progresses, the working data is kept in
       //double-precision floating point
 
       int np=npoints;
         //Begin new elevated polygon
         Elevated = new Polygon();
         //Make vector(s) of points which are of size npoints+1 (to allow room for elevation).
         resizeTemp(np+1);
         
       /* YOUR CODE GOES HERE */
       System.out.println("First degree elevation...");
      double[] originalX = new double[np];
      double[] originalY = new double[np];
      for (int i=0; i<np; i++) {
        tempX[i] =  xpoints[i]; originalX[i] = xpoints[i];
        tempY[i] =  ypoints[i]; originalY[i] = ypoints[i];
      }
       double n = np;
       for (int i = 1; i < np; i++)
       {
     	  tempX[i] = (i/n)*originalX[i-1] + ((n-i)/n)*originalX[i];
    	  tempY[i] = (i/n)*originalY[i-1] + ((n-i)/n)*originalY[i];
       }
       tempX[np] = originalX[np-1];
       tempY[np] = originalY[np-1];
       
       //Copy all npoints+1 points back into the Elevated polygon
       Elevated.reset();
       for(int i=0;i<=np; i++) {
         Elevated.addPoint((int)(tempX[i]+0.5),
                           (int)(tempY[i]+0.5));
       }
     }
 }
