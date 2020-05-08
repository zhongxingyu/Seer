 package kinect;
 
 import java.util.*;
 import java.awt.image.*;
 import javax.imageio.ImageIO;
 import java.io.*;
 
 import april.jmat.*;
 
 
 public class Kinect
 {
     // Frame buffers
     int[] rgb_buf = null;
     short[] d_buf = null;
     int rgb_cnt = 0;
     int d_cnt = 0;
 
     int rgb_save_cnt = 0; // how many rgb images have we saved so far
     int d_save_cnt = 0;
 
     // Initialize the kinect device, returning a negative
     // error code upon failure
     public native int initKinect();
 
     public native int closeKinect();
 
     public native void startVideo();
 
     public native void startRGBVideo();
 
     public native void startIRVideo();
 
     public native void stopVideo();
 
     public native void startDepth();
 
     public native void stopDepth();
 
     public native int[] getVideoFrame();
 
     public native short[] getDepthFrame();
 
 
     static {
         System.loadLibrary("kinect");
     }
 
     public synchronized int init() {
         return initKinect();
     }
 
     public synchronized int close() {
         return closeKinect();
     }
 
     public synchronized void start() {
         startRGBVideo();
         //startIRVideo();
         startDepth();
     }
 
     // same as default above
     public synchronized void startRGB() {
         startRGBVideo();
         startDepth();
     }
 
     public synchronized void startIR() {
         startIRVideo();
         startDepth();
     }
 
     public synchronized void stop() {
         stopVideo();
         stopDepth();
     }
 
     public synchronized Frame getFrame() {
         int[] argb = getVideoFrame();
         short[] depth = getDepthFrame();
         if (argb != null) {
             rgb_buf = rectifyRGB(argb);
             rgb_cnt++;
         }
         if (depth != null) {
             d_buf = rectifyD(depth);
             d_cnt++;
         }
 
         if (rgb_buf != null && d_buf != null) {
             Frame f = new Frame(rgb_buf, d_buf);
             rgb_buf = null;
             d_buf = null;
 
             return f;
         }
         return null;
     }
 
     // rectifies distorted image from color camera using camera parameters
     private synchronized int[] rectifyRGB(int[] Dargb) {
 
         int[] Rargb = new int[Constants.WIDTH * Constants.HEIGHT]; // recified image
         // for every pixel in Rargb 
 
         for (int xp = 0; xp < Constants.WIDTH; xp++) {
             double x = (xp - Constants.Crgbx) / Constants.Frgbx; // compute normalized point x
             for (int yp = 0; yp < Constants.HEIGHT; yp++) {
                 double y = (yp - Constants.Crgby) / Constants.Frgby; // compute normalized point y
                 double[] XND = compXNDrgb(x, y); // apply distortion model
                 int xdp = (int) Math.floor(Constants.Frgbx * XND[0] + Constants.Crgbx); // compute pixel location
                 int ydp = (int) Math.floor(Constants.Frgby * XND[1] + Constants.Crgby);
 
                 // if we have ended up outside of the image
                 if ((xdp < 0) || (xdp >= Constants.WIDTH) || (ydp < 0) || (ydp >= Constants.HEIGHT)) {
                     Rargb[Constants.WIDTH * yp + xp] = 0xff000000; // set to black
                 } else {
                     Rargb[Constants.WIDTH * yp + xp] = Dargb[Constants.WIDTH * ydp + xdp];
                 }
             }
         }
 
         return Rargb;
     }
 
     // rectifies distored depth image using parameters from IR camera, 
     // parameters were obtained from the 640x480 so should be able to modify this image
     private synchronized short[] rectifyD(short[] Dd) {
 
         short[] Rd = new short[Constants.WIDTH * Constants.HEIGHT]; // rectified image
 
         for (int xp = 0; xp < Constants.WIDTH; xp++) {
             double x = (xp - Constants.Cirx) / Constants.Firx; // compute normalized point x
             for (int yp = 0; yp < Constants.HEIGHT; yp++) {
                 double y = (yp - Constants.Ciry) / Constants.Firy; // compute normalized point y
                 double[] XND = compXNDir(x, y); // apply distortion model
                 int xdp = (int) Math.floor(Constants.Firx * XND[0] + Constants.Cirx); // compute pixel location
                 int ydp = (int) Math.floor(Constants.Firy * XND[1] + Constants.Ciry);
                 // if we have ended up outside of the image
                 if ((xdp < 0) || (xdp >= Constants.WIDTH) || (ydp < 0) || (ydp >= Constants.HEIGHT)) {
                     Rd[Constants.WIDTH * yp + xp] = 2048; // set to no informatiom
                 } else {
                     Rd[Constants.WIDTH * yp + xp] = Dd[Constants.WIDTH * ydp + xdp];
                 }
             }
         }
 
         return Rd;
     }
 
     // given a normalized point x, y computes the normalized distorted location
     private synchronized double[] compXNDrgb(double x, double y) {
         double[] XND = new double[2];
         // simplified expression
         //XND[0] = x;
         //XND[1] = y;
 
         // full expression
         double r2 = x * x + y * y;
         // radial component
         double KR = 1 + Constants.Krgb[0] * r2 + Constants.Krgb[1] * r2 * r2 + Constants.Krgb[4] * r2 * r2 * r2;
         // tangential component
         double dx = 2 * Constants.Krgb[2] * x * y + Constants.Krgb[3] * (r2 + 2 * x * x);
         double dy = Constants.Krgb[2] * (r2 + 2 * y * y) + 2 * Constants.Krgb[3] * x * y;
 
         XND[0] = KR * x + dx;
         XND[1] = KR * y + dy;
 
         return XND;
     }
 
     // given a normalized point x, y computes the normalized distorted location
     private synchronized double[] compXNDir(double x, double y) {
         double[] XND = new double[2];
         // simplified expression
         //XND[0] = x;
         //XND[1] = y;
 
         // full expression
         double r2 = x*x + y*y;
         // radial component
         double KR = 1 + Constants.Kir[0]*r2 + Constants.Kir[1]*r2*r2 + Constants.Kir[4]*r2*r2*r2;
         // tangential component
         double dx = 2*Constants.Kir[2]*x*y + Constants.Kir[3]*(r2+2*x*x);
         double dy = Constants.Kir[2]*(r2+2*y*y) + 2*Constants.Kir[3]*x*y;
         XND[0] = KR*x + dx;
         XND[1] = KR*y + dy;
 
         return XND;
     }
 
     public synchronized void printCount() {
         System.out.printf("rgb: %d depth: %d\n", rgb_cnt, d_cnt);
     }
 
     // saves picture of RGB image to file
 
     public void saveRGB(Frame frame)
     {
         BufferedImage Im = frame.makeRGB();
         try {
             File file = new File("Constants.Krgb" + Integer.toString(rgb_save_cnt) + ".jpg");
             ImageIO.write(Im, "jpg", file);
         } catch (IOException e) {
             System.out.println("Failure to Save RGB!");
         }
         rgb_save_cnt++;
     }
 
     // saves depth image to file
     public void saveD(Frame frame)
     {
         BufferedImage Im = frame.makeDepth();
         try {
             File file = new File("Kdepth" + Integer.toString(d_save_cnt) + ".jpg");
             ImageIO.write(Im, "jpg", file);
         } catch (IOException e) {
             System.out.println("Failure to Save Depth!");
         }
         d_save_cnt++;
     }
 
     // Practical resolution of depth seems to be:
     // 632 x 480
     static public class Frame {
         // Not an ideal location for more constants
 
         public int[] argb;
         public short[] depth;
         static double[] t_gamma = null;
 
         public Frame(int argb[], short[] depth) {
             this.argb = LinAlg.copy(argb);
             this.depth = Arrays.copyOf(depth, depth.length);
 
             if (t_gamma == null) {
                 this.t_gamma = new double[2048];
 
                 // From Daniel Shiffman
                 for (int i = 0; i < 2048; i++) {
                     t_gamma[i] = 1.0 / (i * -0.0030711016 + 3.3309495161);
                 }
 
                 // From Stephane Magnenat
                 //double k1 = 1.1863;
                 //double k2 = 2842.5;
                 //double k3 = 0.1236;
                 //for (int i = 0; i < 2048; i++) {
                 //    t_gamma[i] = k3*Math.tan(i/k2 + k1);
                 //}
 
                 // From Willow Garage
                 //for (i = 0; i < 2048; i++) {
                 //    double frac = i/2048.0;
                 //    frac = 6*Math.pow(frac, 3);
                 //    t_gamma[i] = v*6*256;
                 //}
             }
         }
 
         
 
         public double depthToMeters(short depth) {
             // Throw away extreme values
            if ((int) depth >= 2048) {
                 return -1;
             }
             return t_gamma[depth];
         }
 
         public BufferedImage makeRGB() {
             assert (argb.length == Constants.WIDTH * Constants.HEIGHT);
             BufferedImage im = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
             int[] buf = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();
             for (int i = 0; i < buf.length; i++) {
                 buf[i] = argb[i];
             }
 
             return im;
         }
 
         public BufferedImage makeDepth() {
             assert (depth.length == Constants.WIDTH * Constants.HEIGHT);
             BufferedImage im = new BufferedImage(Constants.WIDTH, Constants.HEIGHT, BufferedImage.TYPE_INT_RGB);
             int[] buf = ((DataBufferInt) (im.getRaster().getDataBuffer())).getData();
             double[] cutoffs = new double[]{1.0, 1.75, 2.5, 3.25, 4.0, 5.0};
             for (int i = 0; i < buf.length; i++) {
                 // XXX Improved color mapping. Optimal range is ~0.8m - 3.5m
                 // white -> close
                 // red
                 // orange
                 // yellow
                 // green
                 // blue
                 // magenta
                 // black -> bad values
                 double m = depthToMeters(depth[i]);
                 if (m < 0) {
                     buf[i] = 0;
                     continue;
                 }
                 int r, g, b;
                 if (m < cutoffs[0]) {
                     r = 0xff;
                     g = 0xff - (int) (0xff * m / cutoffs[0]);
                     b = 0xff - (int) (0xff * m / cutoffs[0]);
                 } else if (m < cutoffs[1]) {
                     r = 0xff;
                     g = 0xff - (int) (0xff * ((cutoffs[1] - m) / (cutoffs[1] - cutoffs[0])));
                     b = 0;
                 } else if (m < cutoffs[2]) {
                     r = (int) (0xff * ((cutoffs[2] - m) / (cutoffs[2] - cutoffs[1])));
                     g = 0xff;
                     b = 0;
                 } else if (m < cutoffs[3]) {
                     r = 0;
                     g = (int) (0xff * ((cutoffs[3] - m) / (cutoffs[3] - cutoffs[2])));
                     b = 0xff - (int) (0xff * ((cutoffs[3] - m) / (cutoffs[3] - cutoffs[2])));
                 } else if (m < cutoffs[4]) {
                     r = 0xff - (int) (0xff * ((cutoffs[4] - m) / (cutoffs[4] - cutoffs[3])));
                     g = 0;
                     b = 0xff;
                 } else if (m < cutoffs[5]) {
                     r = (int) (0xff * ((cutoffs[5] - m) / (cutoffs[5] - cutoffs[4])));
                     g = 0;
                     b = (int) (0xff * ((cutoffs[5] - m) / (cutoffs[5] - cutoffs[4])));
                 } else {
                     r = 0;
                     g = 0;
                     b = 0;
                 }
 
                 buf[i] = 0xff000000 | (r << 16) | (g << 8) | b;
             }
 
             return im;
         }
     }
 }
