 package kinect;
 
 import java.util.*;
 import java.lang.Math; // for undistortion
 
 import april.jmat.*;
 import april.vis.*;
 
 public class ColorPointCloud
 {
     public ArrayList<double[]> points = new ArrayList<double[]>();
     public ArrayList<Integer> colors = new ArrayList<Integer>();
     public VisColorData vcd = new VisColorData();
     
     // John's Calibration Data
     /*
     // RGB Intrinsic Camera Parameters
     static final double Frgbx = 521.67090; // focal lengths
     static final double Frgby = 521.23461;
     static final double Crgbx = 300; // optial axis 312.82654
     static final double Crgby = 272; //258.60812 
     // IR Intrinsic Camera Parameters
     static final double Firx = 583.56911; // focal lengths
     static final double Firy = 582.28721; 
     static final double Cirx = 317.73984; // optical axis
     static final double Ciry = 248.91467;
     */
        
     // Camera calibration numbers courtesy of Nicolas Burrus
     // parameters for rgb color camera
     static double Frgbx = 5.2921508098293293e2; // focal length
     static double Frgby = 5.2556393630057437e2; 
     static double Crgbx = 3.2894272028759258e2; // camera center in pixels
     static double Crgby = 2.6748068171871557e2;
     // parameters for IR depth camera
     static double Firx = 5.9421434211923247e2; // focal length
     static double Firy = 5.9104053696870778e2;
     static double Cirx = 3.3930780975300314e2; // camera center in pixels
     static double Ciry = 2.4273913761751615e2;
   
     /*
     // parameters for IR depth camera
     static double fx_d = 5.9421434211923247e2; // focal length
     static double fy_d = 5.9104053696870778e2;
     static double cx_d = 3.3930780975300314e2; // camera center in pixels
     static double cy_d = 2.4273913761751615e2;
     // the following may be terms in the Brown's Distortion model
     static double k1_d = -2.6386489753128833e-1; // radial distortion coefficient
     static double k2_d = 9.9966832163729757e-1;
     static double p1_d = -7.6275862143610667e-4; // tangential distortion coefficient
     static double p2_d = 5.0350940090814270e-3;
     static double k3_d = -1.3053628089976321;
     */
 
     // rotation transformation between IR depth camera and RGB color camera
     static double[][] rotate = new double[][] {{9.9984628826577793e-1, 1.2635359098409581e-3, -1.7487233004436643e-2, 0},
                                         {-1.4779096108364480e-3, 9.9992385683542895e-1, -1.2251380107679535e-2, 0},
                                         {1.7470421412464927e-2, 1.2275341476520762e-2, 9.9977202419716948e-1, 0},
                                         {0,0,0,1}};
     static double[][] trans = LinAlg.translate(new double[] {1.9985242312092553e-2,
                                                       -7.4423738761617583e-4,
                                                       -1.0916736334336222e-2});   
      // Lauren's Code
     /*
     static final int WIDTH = Kinect.WIDTH;
     static final int HEIGHT = Kinect.HEIGHT;
 
     static double dfx = 5.8e+02;
     static double dfy = 5.8e+02;
     //  static double dcx = 3.1553578317293898e+02; \\Lauren's    
     //  static double dcy = 2.4608755771403534e+02; \\Lauren's
     static double dcx = 3.2353578317293898e+02;    
     static double dcy = 2.608755771403534e+02;
     double rfx = 5.25e+02;
     double rfy = 5.25e+02;
     double rcx = 3.1924870232372928e+02;                                                                                                       
     double rcy = 2.6345521395833958e+02;
 
     static double[] t = new double[]{-1.5e-02, 2.5073334719943473e-03,-1.2922411623995907e-02};
     */
 
     public ColorPointCloud(Kinect.Frame frame)
     {       
         for (int y = 0; y < frame.depthHeight; y++) {
             for (int x = 0; x < frame.depthWidth; x++) {
         /*
         for (int y = 0; y < HEIGHT; y++) {
             for (int x = 0; x < WIDTH; x++) { */
 
 
                 // Calculate point place in world
                 double m = frame.depthToMeters(frame.depth[y*frame.depthWidth + x]);
                 if (m < 0)
                     continue;		
                 
                 // points in 3D
                 double px = (x - Cirx) * m/Firx;
                 double py = (y - Ciry) * m/Firy;
                 double pz = m;
 		
                 
                 /*double px = (x - cx_d) * m/fx_d;
                 double py = (y - cy_d) * m/fy_d;
                 double pz = m; */
                 
 
                 // Calculate color of point
                 int cx = 0, cy = 0;
                 // rotation transformation to transform from IR frame to RGB frame
                 double[] xyz = new double[] {px, py, pz};
                 double[] cxyz = LinAlg.transform(rotate, xyz);
                 cxyz = LinAlg.transform(trans, cxyz);
 
                 // project 3D point into rgb image frame
                 cx = (int) ((cxyz[0] * Frgbx / cxyz[2]) + Crgbx);
                 cy = (int) ((cxyz[1] * Frgby / cxyz[2]) + Crgby);
   
                 assert (!(cx < 0 || cx > frame.rgbWidth));
                 assert (!(cy < 0 || cy > frame.rgbHeight));
 
                 points.add(new double[] {px, py, pz});
                 int argb = frame.argb[cy*frame.rgbWidth + cx]; // get the rgb data for the calculated pixel location
 
                 // Lauren's Code
                 /*
                 double px = ((x - dcx) * m/dfx)  + t[0];
                 double py = ((y - dcy) * m/dfy) + t[1];
                 double pz = m + t[2];
                 points.add(new double[] {pz, -px, -py});     
 
                 // Calculate color of point
                 int modx = ((int)(px * rfx/pz + rcx));
                 int mody = ((int)(py * rfy/pz + rcy));
 
                 int argb;
                 if (modx < 0 || modx >= WIDTH || mody < 0 || mody >= HEIGHT){
                     argb = 0xff000000;
                 }
                 else{
                     argb = frame.argb[mody*WIDTH + modx];
                 }
                 */
                 colors.add(argb);
                 int abgr = (argb & 0xff000000) | ((argb & 0xff) << 16) | (argb & 0xff00) | ((argb >> 16) & 0xff);
                 vcd.add(abgr);
             }
         }
     }
     
     // alternate constructor for making a decimated point cloud
     // 1 is full resolution, 10 would be every 10 pixels
    // XXX fix for aliasing!
     public ColorPointCloud(Kinect.Frame frame, int Dfactor) {
         
         for (int y = 0; y < frame.depthHeight; y = y + Dfactor) {
             for (int x = 0; x < frame.depthWidth; x = x + Dfactor) {
                 
                 // Calculate point place in world
                 double m = frame.depthToMeters(frame.depth[y*frame.depthWidth + x]);
                 if (m < 0)
                     continue;		
                 
                 // points in 3D
                 double px = (x - Cirx) * m/Firx;
                 double py = (y - Ciry) * m/Firy;
                 double pz = m;
 
                 // Calculate color of point
                 int cx = 0, cy = 0;
                 // rotation transformation to transform from IR frame to RGB frame
                 double[] xyz = new double[] {px, py, pz};
                 double[] cxyz = LinAlg.transform(rotate, xyz);
                 cxyz = LinAlg.transform(trans, cxyz);
 
                 // project 3D point into rgb image frame
                 cx = (int) ((cxyz[0] * Frgbx / cxyz[2]) + Crgbx);
                 cy = (int) ((cxyz[1] * Frgby / cxyz[2]) + Crgby);
 
                 assert (!(cx < 0 || cx > frame.rgbWidth));
                 assert (!(cy < 0 || cy > frame.rgbHeight));
 
                 points.add(new double[] {px, py, pz});
                 int argb = frame.argb[cy*frame.rgbWidth + cx]; // get the rgb data for the calculated pixel location
 
                 colors.add(argb);
                 int abgr = (argb & 0xff000000) | ((argb & 0xff) << 16) | (argb & 0xff00) | ((argb >> 16) & 0xff);
                 vcd.add(abgr);           
             } 
         }  
     }
 
     public int numPoints()
     {
         return points.size();
     }
 }
