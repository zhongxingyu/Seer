 package edu.cmu.pandaa.header;
 
 import java.io.Serializable;
 
 public class GeometryHeader extends StreamHeader implements Serializable {
   public String[] deviceIds;
   public int rows, cols;
   private double[] prevX;
 
 
   public GeometryHeader(String[] deviceIds, long startTime, int frameTime, int rows, int cols) {
     super(makeId(deviceIds), startTime, frameTime);
     this.deviceIds = deviceIds;
     this.rows = rows;
     this.cols = cols;
     if (deviceIds.length != rows)
       throw new IllegalArgumentException("Mismatched array dimensions");
   }
 
   public GeometryHeader(String id, long startTime, int frameTime, int rows, int cols) {
     super(id, startTime, frameTime);
     this.deviceIds = getIds(id);
     this.rows = rows;
     this.cols = cols;
     if (deviceIds.length != rows)
       throw new IllegalArgumentException("Mismatched array dimensions");
   }
 
   private static String makeId(String[] DeviceIds) {
     StringBuilder ids = new StringBuilder();
     for (int i=0; i<DeviceIds.length;i++) {
       ids.append(',');
       ids.append(DeviceIds[i]);
     }
     return ids.substring(1); // return a single string(pseudo-master ID)
   }
 
   private static String[] getIds(String id) {
     return id.split(",");
   }
 
   public class GeometryFrame extends StreamFrame implements Serializable {
     public double[][] geometry;
 
     public GeometryFrame() {
     }
 
     public GeometryFrame(int seq, double[][] geometry) {
       super(seq);
       init(geometry);
     }
 
     public GeometryFrame(double[][] geometry) {
       init(geometry);
     }
 
     private void init(double[][] geometry) {
       if (geometry.length != cols)
         throw new IllegalArgumentException("Gemoetry cols does not match");
       if (geometry.length > 0 && geometry[0].length != rows)
         throw new IllegalArgumentException("Geometry rows does not match");
       this.geometry = geometry;
     }
 
     public void adjustAxes()
     {
       if (cols != 2)
         throw new IllegalArgumentException("should be 2 dimensions!");
 
       // check for valid data
       for (int i = 0;i < cols; i++)
         for (int j = 0;j < rows; j++)
           if (Double.isNaN(geometry[i][j]))
             return;
 
       // translate coordinates to the centroid
       for (int i = 0;i < cols; i++) {
         double sum = 0;
         for (int j = 0;j < rows; j++)
           sum += geometry[i][j];
         sum = sum / rows;
         for (int j = 0;j < rows; j++)
           geometry[i][j] -= sum;
       }
 
       // rotate device 0 so it's at the bottom center
       double ang = Math.atan2(-geometry[0][0], -geometry[1][0]);
       double sin = Math.sin(ang);
       double cos = Math.cos(ang);
       for (int j = 0;j < rows; j++) {
         double nx = geometry[0][j]*cos - geometry[1][j]*sin;
         double ny = geometry[0][j]*sin + geometry[1][j]*cos;
         geometry[0][j] = nx;
         geometry[1][j] = ny;
       }
 
       // first time through, we don't really know left from right
       // arbitrarily choose it so that device[1] is x>0
       if (prevX == null) {
         prevX = new double[rows];
         double mult = geometry[0][1] > 0 ? 1 : -1;
         for (int j = 0;j < rows; j++) {
           prevX[j] = geometry[0][j]*mult;
         }
       }
 
       // go through and see if we should flip or not flip
       double flip = 0, noflip = 0;
       for (int j = 0;j < rows; j++) {
         double dx = geometry[0][j]- prevX[j];
         noflip += dx*dx;
         dx = geometry[0][j]+ prevX[j];
         flip += dx*dx;
       }
 
       // flip the x axis if it minimizes difference
       if (flip < noflip) {
         for (int j = 0;j < rows; j++) {
           geometry[0][j] = -geometry[0][j];
         }
       }
 
       // save the X values for flipping later frames
       for (int j = 0;j < rows; j++) {
         prevX[j] = geometry[0][j];
       }
     }
   }
 
   public GeometryFrame makeFrame() {
     return new GeometryFrame();
   }
 
   public GeometryFrame makeFrame(double[][] geometry) {
     return new GeometryFrame(geometry);
   }
 
   public GeometryFrame makeFrame(int seq, double[][] geometry) {
     return new GeometryFrame(seq, geometry);
   }
 }
