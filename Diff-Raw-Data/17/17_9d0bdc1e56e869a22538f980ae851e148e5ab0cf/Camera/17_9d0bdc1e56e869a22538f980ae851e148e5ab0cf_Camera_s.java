 package cabbagegl;
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.Random;
 
 public class Camera {
     // Modelview
     private Vector3 eye;
     private Vector3 view;
     private Vector3 up;
 
     // Projection
     private double field_of_view;
     private double aspect_ratio;
     private double near;
     private double far;
 
     // Defaults
     private static final Vector3 DEFAULT_EYE = Vector3.UNIT_X;
     private static final Vector3 DEFAULT_VIEW = Vector3.ZERO;
     private static final Vector3 DEFAULT_UP = Vector3.UNIT_Y;
 
     private static final double DEFAULT_FOV = 45.0;
     private static final double DEFAULT_ASPECT = 1.0;
     private static final double DEFAULT_ZNEAR = 0.1;
     private static final double DEFAULT_ZFAR = 20.0;
 
     public boolean cel_shaded; // wat
 
     private AtomicInteger pixelsRendered;
 
     public Camera() {
         lookAt(DEFAULT_EYE, DEFAULT_VIEW, DEFAULT_UP);
         perspective(DEFAULT_FOV, DEFAULT_ASPECT, DEFAULT_ZNEAR, DEFAULT_ZFAR);
         cel_shaded = false;
     }
 
 
     public void lookAt(Vector3 ieye, Vector3 iview, Vector3 iup) {
         eye = ieye;
         view = iview;
         up = iup;
     }
 
     public void perspective(double fov, double aspect, double znear,
             double zfar) {
         field_of_view = fov;
         aspect_ratio = aspect;
         near = znear;
         far = zfar;
     }
 
     public BufferedImage renderScene(Scene scene, RenderOptions roptions) {
         BufferedImage img = new BufferedImage(roptions.width, roptions.height,
             BufferedImage.TYPE_INT_RGB);
         // Generate several threads that render seperate parts of the scene
         int nthreads = 10;
         Thread threads[] = new Thread[nthreads];
         pixelsRendered = new AtomicInteger(0);
         for (int i = 0; i < nthreads; i++) {
            // Calculate the current thread's range to render
            int low = i * (roptions.width / nthreads);
            int high;
            if (i == nthreads) {
               high = roptions.width;
            } else {
               high = low + (roptions.width / nthreads);
            }
            RenderThread rt = new RenderThread(scene, roptions, low, high, img);
            Thread toRun = new Thread(rt);
            toRun.start();
            threads[i] = toRun;
         }
         for (int i = 0; i < nthreads; i++) {
            try {
               threads[i].join();
            } catch(Exception e) {}
         }
 
         return img;
     }
 
     public class RenderThread implements Runnable {
        private Scene scene;
        private RenderOptions roptions;
        private int low, high;
        private BufferedImage image;
        public RenderThread(Scene s, RenderOptions ro, int l, int h,
             BufferedImage img) {
           scene = s;
           roptions = ro;
           low = l;
           high = h;
           image = img;
        }
        public void run() {
           renderInRange(scene, roptions, low, high, image);
        }
     }
 
     private void renderInRange(Scene s, RenderOptions o, int il, int ih,
          BufferedImage img) {
        for (int i = il; i < ih; i++) {
           for (int j = 0; j < o.height; j++) {
              Color pixCol = renderPixel(i, j, s, o);
              img.setRGB(i, j, pixCol.getRGB());
           }
        }
     }
 
     private Color renderPixel(int i, int j, Scene s, RenderOptions roptions) {
         // Convert i,j to ix, jy where projection plane center is 0
         double ix = (double) i - roptions.width  / 2.0;
         double jy = (double) -j + roptions.height / 2.0;
 
         // Construct a ray from the eye to the proper viewpoint
         // We need a base vector and a direction vector for a ray to trace
         Vector3 base = eye;
         Vector3 dir = view.diff(eye).normalize();
         Vector3 left = up.normalize().cross(dir).normalize();
         Vector3 y = dir.cross(left).normalize();
 
         // d used to be the near plane
         // but now we're worried about the focal plane
         double d = roptions.focal_plane_dist;
         double phi = field_of_view / 2.0;
 
         // Get the physical dimensions of projection plane
         // These are actually the physical dimensions of the focal plane
         double yhgt = 2 * d * Math.tan(Math.toRadians(phi));
         double xwid = yhgt * aspect_ratio;
         double theta = Math.toDegrees(Math.atan((xwid/2.0)/d));
 
         // Now we have phi and theta, as well as camera orthonormal coord sys
         // coord sys is left, y, dir
         // positions on projection plane are linear combination of u and v
         Vector3 u = left.scale(d * Math.tan(Math.toRadians(theta)));
         Vector3 v = y.scale(d * Math.tan(Math.toRadians(phi)));
         // get normalized u and v for eye jitter in depth of field
         Vector3 nu = u.normalize(); Vector3 nv = v.normalize();
 
         // Alpha and Beta are coefficients
         double alpha = 2.0*(ix+0.5) / roptions.width;
         double beta  = 2.0*(jy+0.5) / roptions.height;
 
         // Get center of focal plane
         Vector3 fc = eye.sum(view.diff(eye).normalize().scale(d));
 
         // Get position of pixel in world coords
         // used to end in .sum(view) should now be .sum(focal center)
         Vector3 P = u.scale(alpha).sum(v.scale(beta)).sum(fc);
 
         // Now get a vector pointing to the pixel from the cam
         Vector3 pixelDir = P.diff(eye).normalize();
         double mindist = near;
         double maxdist = far;
 
         double pixWid = yhgt / roptions.height;
         double pixHgt = xwid / roptions.width;
 
         Vector3 nPixelDir = pixelDir;
         Vector3 neye = eye;
 
         Vector3 toUse = Vector3.ZERO;
         Vector3 curr = Vector3.ZERO;
         // trace our new ray through the scene to get the color of this pixel
         for (int dofitr = 0; dofitr < roptions.dof_rays; dofitr++) {
            for (int aaitr = 0; aaitr < roptions.AA_samples; aaitr++) {
               Ray toTrace = new Ray(neye, nPixelDir);
              curr = curr.sum(toTrace.trace(s, mindist, maxdist, roptions.max_recurse));
 
               // Jitter the viewpoint
               double xJit = doubleBetween(-pixWid / 4.0, pixWid / 4.0);
               double yJit = doubleBetween(-pixHgt / 4.0, pixHgt / 4.0);
               Vector3 nP = u.scale(alpha+xJit).sum(v.scale(beta+yJit)).sum(fc);
               nPixelDir = nP.diff(neye).normalize();
            }
            toUse = toUse.sum(curr.scale(1.0/roptions.AA_samples));
            curr = Vector3.ZERO;
 
            // Jitter the eye point
            double randAngle = doubleBetween(0,2*Math.PI);
            double lensDist = doubleBetween(0, roptions.lens_aperture_radius);
            Vector3 utrans = nu.scale(Math.cos(randAngle));
            Vector3 vtrans = nv.scale(Math.sin(randAngle));
            Vector3 eyeJit = utrans.sum(vtrans).normalize().scale(lensDist);
            neye = eye.sum(eyeJit);
 
            nPixelDir = P.diff(neye).normalize();
         }
         toUse = toUse.scale(1.0/roptions.dof_rays);
 
         toUse = toUse.clamp(0.0, 1.0);
         int rendered = pixelsRendered.incrementAndGet();
         if (rendered % 100 == 0)
            System.out.println((((double)rendered) / (roptions.width*roptions.height)) + " complete.");
         return vectorToColor(toUse);
     }
 
     private static Random randGen = new Random();
     private double doubleBetween(double low, double high) {
        return randGen.nextDouble() * (high-low) + low;
     }
 
     private static Color vectorToColor(Vector3 in) {
        int r = (int) (in.X() * 255.0);
        int g = (int) (in.Y() * 255.0);
        int b = (int) (in.Z() * 255.0);
        return new Color(r, g, b);
     }
 }
