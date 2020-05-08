 package pcm.dev.satayev.raytracer;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import pcm.geom.Vector;
 import pcm.model.Photon;
 import pcm.model.shape.Polygon;
 import pcm.model.shape.Sphere;
 import pcm.model.shape.Surface;
 import pcm.util.V;
 
 /**
  * Simple ray tracer, takes into account reflections, refractions, and transparency.
  * 
  * @author Satayev
  */
 @SuppressWarnings("serial")
 public class Raytracer extends JPanel {
 
   /////////////////////////////////////////////////////////////////////////////
   // Constants to determine a color of a pixel
   /////////////////////////////////////////////////////////////////////////////
 
   private double lambert = 0.4, phong = 0.6, phongpower = 7, refractionCoef = 1;
   private double reflectionCoef = 0.15, transparencyCoef = 0.15;
   private double surfaceColorCoef = 1 - reflectionCoef - transparencyCoef;
 
   /////////////////////////////////////////////////////////////////////////////
   // Create GUI and add objects into a scene
   /////////////////////////////////////////////////////////////////////////////
 
   // eye is on the following plane
   private final static double eyeZ = 1000;
   // size of the square canvas
   private final static int size = 500;
   // used to color surfaces
   private final static Color[] colors = { Color.red, Color.blue, Color.green, Color.yellow, Color.cyan, Color.magenta,
       Color.pink, Color.white };
 
   public static void main(String[] args) {
     Raytracer rt = new Raytracer();
 
     // add lights
     rt.lights.add(new Vector(eyeZ, eyeZ, eyeZ));
     rt.lights.add(new Vector(-eyeZ / 5, 0, 0));
     rt.lights.add(new Vector(0, eyeZ, eyeZ));
 
     // add surfaces
     rt.surfaces.add(new Sphere(new Vector(100, 55, 0), 25));
     rt.surfaces.add(new Sphere(new Vector(100, 100, 100), 50));
     rt.surfaces.add(new Sphere(new Vector(175, 100, 100), 25));
     rt.surfaces.add(new Sphere(new Vector(0, 0, 0), 100));
 
     // add a rectangle
     Polygon rect = new Polygon(new Vector[] {
         new Vector(-200, 100, 0), new Vector(200, 100, 0),
         new Vector(200, -150, 0), new Vector(-200, -150, 0)
     });
     rt.surfaces.add(rect);
 
     // assign distinct colors to surfaces
     for (int i = 0; i < rt.surfaces.size(); i++) {
       Color c = colors[i];
       Vector v = new Vector(c.getRed(), c.getGreen(), c.getBlue());
       v.normalize();
       rt.surfaceColors.put(rt.surfaces.get(i), v);
     }
 
     JFrame frame = new JFrame("Render");
     frame.setContentPane(rt);
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.setSize(size, size);
     frame.setVisible(true);
   }
 
   /** Draws a scene with an orthogonal camera. */
   public void paintComponent(Graphics g) {
     BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
     for (int x = 0; x < size; x++)
       for (int y = 0; y < size; y++) {
         Photon photon = new Photon(new Vector(x - size / 2, y - size / 2, eyeZ), new Vector(0, 0, -1));
         Vector clr = this.color(photon);
         int rgb = new Color((float) clr.x, (float) clr.y, (float) clr.z).getRGB();
         // swap y-coordinate to match OpenGL
         img.setRGB(x, size - 1 - y, rgb);
       }
     g.drawImage(img, 0, 0, size, size, null);
   }
 
   /////////////////////////////////////////////////////////////////////////////
   // Raytracer code
   /////////////////////////////////////////////////////////////////////////////
 
   // point lights of the same power
   private List<Vector> lights = new ArrayList<Vector>();
   // different objects in the scene
   private List<Surface> surfaces = new ArrayList<Surface>();
   // surface colors
   private Map<Surface, Vector> surfaceColors = new HashMap<Surface, Vector>();
 
   private class Hit {
     Surface surface;
     double time;
 
     Hit(Surface surface, double time) {
       this.surface = surface;
       this.time = time;
     }
   }
 
   /* Given a maximum distance photon can travel, determine the closest surface that it collides with. */
   private Hit trace(Photon photon, double maxDistance) {
     double time = Double.POSITIVE_INFINITY;
     Surface surface = null;
     for (Surface s : surfaces) {
       double t = s.travelTime(photon);
       if (t < time && t < maxDistance && t > 0) {
         time = t;
         surface = s;
       }
     }
     return new Hit(surface, time);
   }
 
   /* Determine color for a pixel defined by a photon */
   private Vector color(Photon photon) {
     Hit hit = trace(photon, Double.POSITIVE_INFINITY);
     if (hit.surface == null || hit.time == Double.POSITIVE_INFINITY)
       // return black
       return new Vector();
 
     Vector surfcol = diffuse(photon.clone(), hit);
     Vector reflcol = reflection(photon.clone(), hit);
     Vector refrcol = refraction(photon.clone(), hit);
 
     if (surfcol.length() > 1)
       surfcol.normalize();
 
     double r = (surfaceColorCoef * surfcol.x + reflectionCoef * reflcol.x + transparencyCoef * refrcol.x);
     double g = (surfaceColorCoef * surfcol.y + reflectionCoef * reflcol.y + transparencyCoef * refrcol.y);
     double b = (surfaceColorCoef * surfcol.z + reflectionCoef * reflcol.z + transparencyCoef * refrcol.z);
 
     return new Vector(r, g, b);
   }
 
   private Vector refraction(Photon photon, Hit hit) {
     // refract once for simplicity
    if (photon. > 1)
       return new Vector();
     photon.travel(hit.time);
     photon.v.refract(hit.surface.normal(photon.p), refractionCoef);
     if (photon.v.x == Double.NaN)
       return new Vector();
    photon.++;
     return color(photon);
   }
 
   private Vector reflection(Photon photon, Hit hit) {
     // reflect once for simplicity
    if (photon. > 1)
       return new Vector();
     photon.travel(hit.time);
     photon.v.reflect(hit.surface.normal(photon.p));
    photon.++;
     return color(photon);
   }
 
   private Vector diffuse(Photon photon, Hit hit) {
     double sunlight = 0;
     photon.travel(hit.time);
 
     for (Vector light : lights) {
       Vector dir = V.sub(light, photon.p);
       double dist = dir.length();
       dir.normalize();
       Photon p = new Photon(photon.p, dir);
       Hit test = trace(p, dist - V.EPS);
       if (test.surface == null || test.time == Double.POSITIVE_INFINITY) {
         Vector normal = hit.surface.normal(photon.p);
         // point is not in the shadow
         if (phong > 0) {
           Vector lr = V.reflect(dir, normal);
           double cos = V.dot(lr, photon.v);
           if (cos > 0)
             sunlight += phong * Math.pow(cos, phongpower);
         }
         if (lambert > 0) {
           double cos = V.dot(dir, normal);
           if (cos > 0)
             sunlight += lambert * cos;
         }
       }
     }
     return V.mul(sunlight, surfaceColors.get(hit.surface));
   }
 }
