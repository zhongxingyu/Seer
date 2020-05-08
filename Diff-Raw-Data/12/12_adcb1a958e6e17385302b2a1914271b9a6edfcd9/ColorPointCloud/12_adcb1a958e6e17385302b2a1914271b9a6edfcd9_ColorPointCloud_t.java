 package kinect;
 
 import java.util.*;
 
 import april.jmat.*;
 import april.vis.*;
 import kinect.Kinect.Frame;
 
 public class ColorPointCloud {
 
     public ArrayList<double[]> points = new ArrayList<double[]>(307200);
     public ArrayList<Integer> colors = new ArrayList<Integer>(307200);
     public VisColorData vcd = new VisColorData();
     // hash map storing mapping between rgb image and depth
     //HashMap<Pixel, Double> rgbDmap = new HashMap<Pixel, Double>();
     int[] pointmap = new int[Constants.HEIGHT * Constants.WIDTH];
 
     public ColorPointCloud(Kinect.Frame frame) {
         assert (frame != null);
 
         StopWatch outertimer = new StopWatch();
         long depthlookup = 0;
         long projection = 0;
         long backprojection = 0;
         long hashmapconstruct = 0;
 
         for (int y = 0; y < Constants.HEIGHT; y++) {
             for (int x = 0; x < Constants.WIDTH; x++) {
                 long[] values = processPoint(frame, x, y);
                 depthlookup += values[0];
                 projection += values[1];
                 backprojection += values[2];
                 hashmapconstruct += values[3];
             }
         }
         outertimer.addTask(new StopWatch.TaskInfo("depthlookup", depthlookup));
         outertimer.addTask(new StopWatch.TaskInfo("projection", projection));
         outertimer.addTask(new StopWatch.TaskInfo("backprojection", backprojection));
         outertimer.addTask(new StopWatch.TaskInfo("hash map construction", hashmapconstruct));
 
         System.out.println(outertimer.prettyPrint());
     }
 
     // alternate constructor for making a decimated point cloud
     // 1 is full resolution, 10 would be every 10 pixels
     // XXX fix for aliasing!
     public ColorPointCloud(Kinect.Frame frame, int Dfactor) {
         StopWatch outertimer = new StopWatch();
         long depthlookup = 0;
         long projection = 0;
         long backprojection = 0;
         long hashmapconstruct = 0;
         for (int y = 0; y < Constants.HEIGHT; y = y + Dfactor) {
             for (int x = 0; x < Constants.WIDTH; x = x + Dfactor) {
                 long[] values = processPoint(frame, x, y);
                 depthlookup += values[0];
                 projection += values[1];
                 backprojection += values[2];
                 hashmapconstruct += values[3];
             }
         }
         outertimer.addTask(new StopWatch.TaskInfo("depthlookup", depthlookup));
         outertimer.addTask(new StopWatch.TaskInfo("projection", projection));
         outertimer.addTask(new StopWatch.TaskInfo("backprojection", backprojection));
         outertimer.addTask(new StopWatch.TaskInfo("hash map construction", hashmapconstruct));
 
         System.out.println(outertimer.prettyPrint());
     }
 
     protected final long[] processPoint(Frame frame, int x, int y) {
         // Calculate point place in world
         long[] values = new long[4];
         StopWatch timer = new StopWatch();
         timer.start("depth lookup");
         double m = frame.depthToMeters(frame.depth[y * Constants.WIDTH + x]);
         if (m < 0) {
             return new long[]{0, 0, 0, 0};
         }
         timer.stop();
         values[0] = timer.getLastTaskTimeMillis();
 
         // points in 3D
         timer.start("projection");
         double px = (x - Constants.Cirx) * m / Constants.Firx;
         double py = (y - Constants.Ciry) * m / Constants.Firy;
         double pz = m;
         timer.stop();
         values[1] = timer.getLastTaskTimeMillis();
         // Calculate color of point
         int cx = 0, cy = 0;
 
         timer.start("backprojection");
         // rotation transformation to transform from IR frame to RGB frame
         double[] xyz = new double[]{px, py, pz};
         double[] cxyz = LinAlg.transform(Constants.Transirtorgb, xyz);
         /*
         double[] cxyz = LinAlg.transform(Constants.Rirtorgb, xyz);
         cxyz = LinAlg.transform(Constants.Tirtorgb, cxyz);
          */
         // project 3D point into rgb image frame
         cx = (int) ((cxyz[0] * Constants.Frgbx / cxyz[2]) + Constants.Crgbx);
         cy = (int) ((cxyz[1] * Constants.Frgby / cxyz[2]) + Constants.Crgby);
         assert (!(cx < 0 || cx > Constants.WIDTH));
         assert (!(cy < 0 || cy > Constants.HEIGHT));
         points.add(new double[]{px, py, pz});
         int argb = frame.argb[cy * Constants.WIDTH + cx]; // get the rgb data for the calculated pixel location
         timer.stop();
         values[2] = timer.getLastTaskTimeMillis();
 
         // add to hashmap cx cy which maps to m
         timer.start("hashmap");
         //rgbDmap.put(new Pixel(cx,cy), m);
        pointmap[cy * Constants.WIDTH + cx] = points.size()-1;
 
         timer.stop();
         values[3] = timer.getLastTaskTimeMillis();
         
         colors.add(argb);
         int abgr = (argb & 0xff000000) | ((argb & 0xff) << 16) | (argb & 0xff00) | ((argb >> 16) & 0xff);
         vcd.add(abgr);
         return values;
     }
 
     // projects image coordinates in rgb image into 3D space
     // XXX if ever make change to way color point cloud is projected into 3D
     // need to make that fix below
     public double[] Project(double[] Xrgb) {
         assert (Xrgb.length == 2);
 
         int index = pointmap[(int) (Xrgb[1] * Constants.WIDTH + Xrgb[0])];
         
         double[] P = this.points.get(index);
 
         return P;
     }
     
 public ArrayList<double[]> Project(List<double[]> XRGB) {
         
         ArrayList<double[]> P = new ArrayList<double[]>();
         
         for (double[] Xrgb: XRGB) {
             double[] p = Project(Xrgb);
             P.add(p);
         }
         return P;
     } 
 
     public int numPoints()
     {
         return points.size();
     }
 }
