 package kinect;
 
 import java.util.*;
 
 import april.jmat.*;
 import april.vis.*;
 
 public class ColorPointCloud
 {
     public ArrayList<double[]> points = new ArrayList<double[]>();
    public ArrayList<Integer> colors = new ArrayList<Integer>();
     public VisColorData vcd = new VisColorData();
 
     // Camera calibration numbers courtesy of Nicolas Burrus
     static double fx_rgb = 5.2921508098293293e2;
     static double fy_rgb = 5.2556393630057437e2;
     static double cx_rgb = 3.2894272028759258e2;
     static double cy_rgb = 2.6748068171871557e2;
     static double k1_rgb = 2.6451622333009589e-1;
     static double k2_rgb = -8.3990749424620825e-1;
     static double p1_rgb = -1.9922302173693159e-3;
     static double p2_rgb = 1.4371995932897616e-3;
     static double k3_rgb = 9.1192465078713847e-1;
 
     static double fx_d = 5.9421434211923247e2;
     static double fy_d = 5.9104053696870778e2;
     static double cx_d = 3.3930780975300314e2;
     static double cy_d = 2.4273913761751615e2;
     static double k1_d = -2.6386489753128833e-1;
     static double k2_d = 9.9966832163729757e-1;
     static double p1_d = -7.6275862143610667e-4;
     static double p2_d = 5.0350940090814270e-3;
     static double k3_d = -1.3053628089976321;
 
     /*
     double[][] transform = new double[][] {
 [ 9.9984628826577793e-01, 1.2635359098409581e-03,
     -1.7487233004436643e-02, -1.4779096108364480e-03,
     9.9992385683542895e-01, -1.2251380107679535e-02,
     1.7470421412464927e-02, 1.2275341476520762e-02,
     9.9977202419716948e-01 ]
     */
     static double[][] rotate = new double[][] {{9.9984628826577793e-1, 1.2635359098409581e-3, -1.7487233004436643e-2, 0},
                                         {-1.4779096108364480e-3, 9.9992385683542895e-1, -1.2251380107679535e-2, 0},
                                         {1.7470421412464927e-2, 1.2275341476520762e-2, 9.9977202419716948e-1, 0},
                                         {0,0,0,1}};
     static double[][] trans = LinAlg.translate(new double[] {1.9985242312092553e-2,
                                                       -7.4423738761617583e-4,
                                                       -1.0916736334336222e-2});
 
     public ColorPointCloud(Kinect.Frame frame)
     {
         // XXX Undistort the image!
 
         for (int y = 0; y < frame.depthHeight; y++) {
             for (int x = 0; x < frame.depthWidth; x++) {
                 // Calculate point place in world
                 double m = frame.depthToMeters(frame.depth[y*frame.depthWidth + x]);
                 if (m < 0)
                     continue;
                 double px = (x - cx_d) * m/fx_d;
                 double py = (y - cy_d) * m/fy_d;
                 double pz = m;
 
                 // Calculate color of point
                 int cx = 0, cy = 0;
                 double[] xyz = new double[] {px, py, pz};
                 double[] cxyz = LinAlg.transform(rotate, xyz);
                 cxyz = LinAlg.transform(trans, cxyz);
 
                 cx = (int) ((cxyz[0] * fx_rgb / cxyz[2]) + cx_rgb);
                 cy = (int) ((cxyz[1] * fy_rgb / cxyz[2]) + cy_rgb);
 
                 assert (!(cx < 0 || cx > frame.rgbWidth));
                 assert (!(cy < 0 || cy > frame.rgbHeight));
 
                points.add(new double[] {px, py, pz});
                 int argb = frame.argb[cy*frame.rgbWidth + cx];
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
 
