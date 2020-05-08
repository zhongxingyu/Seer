 package kinect;
 
 import java.util.*;
 import java.awt.image.*;
 import javax.imageio.ImageIO;
 import java.io.*;
 
 class Kinect
 {
     static final int RGB_WIDTH = 640;
     static final int RGB_HEIGHT = 480;
     static final int DEPTH_WIDTH = 632;
     static final int DEPTH_HEIGHT = 480;
 
     // RGB Intrinsic Camera Parameters
     static final double Frgbx = 521.67090; // focal lengths
     static final double Frgby = 521.23461;
     static final double Crgbx = 312.82654; // optial axis
     static final double Crgby = 258.60812;
     // assume 0 skew
     // distortion parameters 1 2 and 5 are radial terms, 3 and 4 are tangential
     static final double[] Krgb = {0.18993, -0.52470, 0.00083, 0.00480, 0};
 
     // IR Intrinsic Camera Parameters
     static final double Firx = 583.56911; // focal lengths
     static final double Firy = 582.28721; 
     static final double Cirx = 317.73984; // optical axis
     static final double Ciry = 248.91467;
     // assume 0 skew
     // distortion parameters 1 2 and 5 are radial terms, 3 and 4 are tangential
     static final double[] Kir = {-0.09234, 0.31571, 0.00037, -0.00425, 0};
     
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
 
     static
     {
         System.loadLibrary("kinect");      
     }
 
     public synchronized int init()
     {
         return initKinect();
     }
 
     public synchronized int close()
     {
         return closeKinect();
     }
 
     public synchronized void start()
     {
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
 
     public synchronized void stop()
     {
         stopVideo();
         stopDepth();
     }
 
     public synchronized Frame getFrame()
     {
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
     public synchronized int[] rectifyRGB(int[] Dargb) {
         
         int[] Rargb = new int[RGB_WIDTH*RGB_HEIGHT]; // recified image
         // for every pixel in Rargb 
         for (int xp = 0; xp < RGB_WIDTH; xp++) {
             double x = (xp-Crgbx)/Frgbx; // compute normalized point x
             for (int yp = 0; yp < RGB_HEIGHT; yp++) {
                 double y = (yp - Crgby)/Frgby; // compute normalized point y
                 double[] XND = compXNDrgb(x,y); // apply distortion model
                 int xdp = (int) Math.floor(Frgbx*XND[0] + Crgbx); // compute pixel location
                 int ydp = (int) Math.floor(Frgby*XND[1] + Crgby); 
                 // if we have ended up outside of the image
                 if ((xdp < 0) || (xdp >= RGB_WIDTH) || (ydp < 0) || (ydp >= RGB_HEIGHT)) {
                     Rargb[RGB_WIDTH*yp+xp] = 0xff000000; // set to black
                 } else {
                     Rargb[RGB_WIDTH*yp+xp] = Dargb[RGB_WIDTH*ydp+xdp];
                 }
             }
         }
         
         return Rargb;  
     }
     
     // rectifies distored depth image using parameters from IR camera, 
     // parameters were obtained from the 640x480 so should be able to modify this image
     private synchronized short[] rectifyD(short[] Dd) {
         
         short[] Rd = new short[RGB_WIDTH*RGB_HEIGHT]; // rectified image
         for (int xp = 0; xp < RGB_WIDTH; xp++) {
             double x = (xp-Cirx)/Firx; // compute normalized point x
             for (int yp = 0; yp < RGB_HEIGHT; yp++) {
                 double y = (yp - Ciry)/Firy; // compute normalized point y
                 double[] XND = compXNDrgb(x,y); // apply distortion model
                 int xdp = (int) Math.floor(Firx*XND[0] + Cirx); // compute pixel location
                 int ydp = (int) Math.floor(Firy*XND[1] + Ciry); 
                 // if we have ended up outside of the image
                 if ((xdp < 0) || (xdp >= RGB_WIDTH) || (ydp < 0) || (ydp >= RGB_HEIGHT)) {
                     Rd[RGB_WIDTH*yp+xp] = 2047; // set to no information
                 } else {
                     Rd[RGB_WIDTH*yp+xp] = Dd[RGB_WIDTH*ydp+xdp];
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
         
         double r2 = x*x + y*y;
         // radial component
         double KR = 1 + Krgb[0]*r2 + Krgb[1]*r2*r2 + Krgb[4]*r2*r2*r2;
         // tangential component
         double dx = 2*Krgb[2]*x*y + Krgb[3]*(r2+2*x*x);
         double dy = Krgb[2]*(r2+2*y*y) + 2*Krgb[3]*x*y;
           
         XND[0] = KR*x + dx;
         XND[1] = KR*y + dy;
         
         return XND;
     }
     
     // given a normalized point x, y computes the normalized distorted location
     private synchronized double[] compXNDdepth(double x, double y) {
         double[] XND = new double[2];
         // simplified expression
         XND[0] = x;
         XND[1] = y;
         
         // full expression
         /*
         double r2 = x*x + y*y;
         // radial component
         double KR = 1 + Kir[0]*r2 + Kir[1]*r2*r2 + Kir[4]*r2*r2*r2;
         // tangential component
         double dx = 2*Kir[2]*x*y + Kir[3]*(r2+2*x*x);
         double dy = Kir[2]*(r2+2*y*y) + 2*Kir[3]*x*y;
           
         XND[0] = KR*x + dx;
         XND[1] = KR*y + dy;
          */
         
         return XND;
     }
     
     // compute the jacobian for the equations Xd = F(x,y) probably not needed
     private synchronized double[][] compJXNDrgb(double x, double y) {
         double[][] JXND = new double[2][2];
         // simplified expression
         // Dxd/Dx
         double DxdDx = 1;
         // Dxd/Dy
         double DxdDy = 0;
         // Dyd/Dx
         double DydDx = 0;
         // Dyd/Dy
         double DydDy = 1;
         
         /* full expression neglects the 6th order radial component Krgb[5]
          * // Dxd/Dx
          * double DxdDx = 1 + 3*Krgb[1]*x*x + Krgb[1]*y*y + 5*Krgb[2]*Math.pow(x,4) + 6*Krgb[2]*y*y*x*x + Krgb[2]*Math.pow(y,4) +
          * 2*Krgb[3]*y + 2*Krgb[4]*x + 4*Krgb[4]*x;
          * // Dxd/Dy
          * double DxdDy = 2*Krgb[1]*x*y + 4*Krgb[2]*Math.pow(x,3)*y + 4*Krgb[2]*x*Math.pow(y,3) + 2*Krgb[3]*x + 2*Krgb[4]*y;
          * // Dyd/Dx
          * double DydDx = 2*Krgb[1]*x*y + 4*Krgb[2]*Math.pow(x,3)*y + 4*Krgb[2]*x*Math.pow(y,3) + 2*Krgb[3]*x + 2*Krgb[4]*y
          * // Dyd/Dy
          * double DydDy = 1 + Krgb[1]*x*x + 3*Krgb[1]*y*y + Krgb[2]*Math.pow(x,4) + 6*Krgb[2]*y*y*x*x + 5*Krgb[2]*Math.pow(y,4) + 
          * 2*Krgb[3]*y + 4*Krgb[3]*y + 2*Krgb[4]*x;
          */
         
         JXND[0][0] = DxdDx;
         JXND[0][1] = DxdDy;
         JXND[1][0] = DydDx;
         JXND[1][1] = DydDy;
         
         return JXND;
     }
 
     public synchronized void printCount()
     {
         System.out.printf("rgb: %d depth: %d\n", rgb_cnt, d_cnt);
     }
     
     // saves picture of RGB image to file
     public void saveRGB(Frame frame) 
     {
         BufferedImage Im = frame.makeRGB();
         try {
             File file = new File("Krgb" + Integer.toString(rgb_save_cnt) + ".jpg");
             ImageIO.write(Im , "jpg", file);
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
             ImageIO.write(Im , "jpg", file);
         } catch (IOException e) {
             System.out.println("Failure to Save Depth!");  
         }
         d_save_cnt++;
     }
     
     // Practical resolution of depth seems to be:
     // 632 x 480
     static public class Frame
     {
         // Not an ideal location for more constants
         static final public int rgbWidth = 640;
         static final public int rgbHeight = 480;
         static final public int depthWidth = 640;
         static final public int depthHeight = 480;
 
         public int[] argb;
         public short[] depth;
 
         static double[] t_gamma = null;
         public Frame(int argb[], short[] depth)
         {
             this.argb = argb;
             this.depth = depth;
 
             if (t_gamma == null) {
                 this.t_gamma = new double[2048];
 
                 // From Stephane Magnenat
                 double k1 = 1.1863;
                 double k2 = 2842.5;
                 double k3 = 0.1236;
                 for (int i = 0; i < 2048; i++) {
                     t_gamma[i] = k3*Math.tan(i/k2 + k1);
                 }
 
                 // From Willow Garage
                 //for (i = 0; i < 2048; i++) {
                 //    double frac = i/2048.0;
                 //    frac = 6*Math.pow(frac, 3);
                 //    t_gamma[i] = v*6*256;
                 //}
             }
         }
 
         public double depthToMeters(short depth)
         {
             // Throw away extreme values
             if ((int) depth == 2048)
                 return -1;
             return t_gamma[depth];
         }
 
         public BufferedImage makeRGB()
         {
             assert (argb.length == rgbHeight*rgbWidth);
             BufferedImage im = new BufferedImage(rgbWidth, rgbHeight, BufferedImage.TYPE_INT_RGB);
             int[] buf = ((DataBufferInt)(im.getRaster().getDataBuffer())).getData();
             for (int i = 0; i < buf.length; i++) {
                 buf[i] = argb[i];
             }
 
             return im;
         }
 
         public BufferedImage makeDepth()
         {
             assert (depth.length == depthHeight*depthWidth);
             BufferedImage im = new BufferedImage(depthWidth, depthHeight, BufferedImage.TYPE_INT_RGB);
             int[] buf = ((DataBufferInt)(im.getRaster().getDataBuffer())).getData();
             double[] cutoffs = new double[] {1.0, 1.75, 2.5, 3.25, 4.0, 5.0};
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
                 int r,g,b;
                 if (m < cutoffs[0]) {
                     r = 0xff;
                     g = 0xff - (int) (0xff * m/cutoffs[0]);
                     b = 0xff - (int) (0xff * m/cutoffs[0]);
                 } else if (m < cutoffs[1]) {
                     r = 0xff;
                     g = 0xff - (int) (0xff * ((cutoffs[1] - m)/(cutoffs[1]-cutoffs[0])));
                     b = 0;
                 } else if (m < cutoffs[2]) {
                     r = (int) (0xff * ((cutoffs[2] - m)/(cutoffs[2]-cutoffs[1])));
                     g = 0xff;
                     b = 0;
                 } else if (m < cutoffs[3]) {
                     r = 0;
                     g = (int) (0xff * ((cutoffs[3] - m)/(cutoffs[3]-cutoffs[2])));
                     b = 0xff - (int) (0xff * ((cutoffs[3] - m)/(cutoffs[3]-cutoffs[2])));
                 } else if (m < cutoffs[4]) {
                     r = 0xff - (int) (0xff * ((cutoffs[4] - m)/(cutoffs[4]-cutoffs[3])));
                     g = 0;
                     b = 0xff;
                 } else if (m < cutoffs[5]) {
                     r = (int) (0xff * ((cutoffs[5] - m)/(cutoffs[5]-cutoffs[4])));
                     g = 0;
                     b = (int) (0xff * ((cutoffs[5] - m)/(cutoffs[5]-cutoffs[4])));
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
