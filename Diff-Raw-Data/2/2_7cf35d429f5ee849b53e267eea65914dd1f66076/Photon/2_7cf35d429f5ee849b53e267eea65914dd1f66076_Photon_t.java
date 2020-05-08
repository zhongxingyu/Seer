 package dev.simple;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeMap;
 
 import pcm.PCM3D;
 import pcm.model.geom.V;
 import pcm.model.geom.Vector;
 
 /**
  * This class contains all information for the photon
  * 
  * @author John Stewart
  */
 public class Photon {
 
   public static double X = 1;
   public static double Y = 1;
   public static double Z = 1;
 
   public Vector r = new Vector(), n = new Vector();
   public Vector r0 = new Vector(), n0 = new Vector();
   
   // Temporary orbit interfacing with AppletModel
   public double degrees = -1;
   //
   
   public double w, f, E;
   public static double deltaf = .001, maxf = 3;
   public static TreeMap<Double,Double> blackBody = null;
 
   public Statistic stat = new Statistic();
 
   public Photon() {
     if (blackBody == null) initialize();
   }
   
   public void initialize() {
     blackBody = new TreeMap<Double,Double>();
     List<Double> I = new ArrayList<Double>((int) (maxf / deltaf));
     I.add(1.474e-5 * deltaf * deltaf * deltaf / (Math.exp(6.826 * deltaf) - 1));
     for (double f = 2 * deltaf; f <= maxf; f += deltaf)
       I.add(1.474e-5 * f * f * f / (Math.exp(6.826 * f) - 1) + I.get(I.size() - 1));
     for (int i = 0; i < I.size(); i++)
       I.set(i, I.get(i) / I.get(I.size() - 1));
     for (int i = 0; i < I.size(); i++) 
       blackBody.put(I.get(i), (i + 1) * deltaf);
   }
 
   /**
    * This method randomizes the variables in the photon for a new simulation
    * The photon starts at the top with a downward trajectory
    */
   public void reset() {
     double theta = Math.PI / 2 * (PCM3D.rnd.nextDouble() + 1), phi = Math.PI * 2 * PCM3D.rnd.nextDouble();
     n.x = Math.sin(theta) * Math.cos(phi);
     n.y = Math.sin(theta) * Math.sin(phi);
     n.z = Math.cos(theta);
     n0 = n.clone();
     r.x = X * PCM3D.rnd.nextDouble();
     r.y = Y * PCM3D.rnd.nextDouble();
     r.z = Z;
     r0 = r.clone();
     f = genFreq();
     w = 299792458/f;
     E = 1.986e-25/w;
     stat.newPhoton(r);
   }
 
   public double genFreq() {
 //  f = Math.acos(1-PCM3D.rnd.nextDouble()/.5)/Math.PI*1e15; // rough approximation of black body spectrum, 20% error
 //  if (!(f>0)) {
 //    System.out.println("Invalid Frequency");
 //    f = 1e15;
 //  }
   // P(f) = 1 - (94.35*f^3+34.22*f^2+8.272*f+1)*(2.5548e-4)^f | f = f*1e15
   // p(f) = 1.474e-5*f^3/(e^(6.826*f)-1) | f = f*1e15
     double p = PCM3D.rnd.nextDouble();
     double p1 = blackBody.floorKey(p), p2 = blackBody.ceilingKey(p);
     double f1 = blackBody.get(p1), f2 = blackBody.get(p2);
     double f = f1+(p-p1)*(f2-f1)/(p2-p1); 
     // f = (f1*p2-f1*p1+p*f2-p*f1-p1*f2+p1*f1)/(p2-p1) = (f1*(p2-p)+f2*(p-p1))/(p2-p1) = (f1*p2-f2*p1+p*(f2-f1))/(p2-p1)
    return f*1e15;
   }
 
   /**
    * Moves the photon to the indicated position
    * 
    * @param v The new position
    */
   public void move(Vector v) {
     r.set(v);
   }
 
   public void move(double d) {
     r.add(V.mult(d, n));
   }
 
   /**
    * Reflects the velocity on the vector given and makes a statistical recording
    * Checks if photon is absorbed instead.
    * 
    * @param v The surface normal vector
    * @return Return true if the photon was absorbed, false if not
    */
   public boolean bounce(Vector v) {
     n.add(V.mult(-2 * n.dot(v), v));
     // Statistics here
     if (absorpsionChance()) {
       absorb();
       return true;
     } else {
       stat.addPath(r);
       return false;
     }
   }
 
   /**
    * This method calculates whether the photon is absorbed or not
    * 
    * @return true if absorbed
    */
   public boolean absorpsionChance() {
     return (E > 2.403e-19 && PCM3D.rnd.nextDouble() < .2);
   }
 
   /**
    * This method handles absorbtion of the photon
    */
   public void absorb() {
     stat.addPath(r);
     stat.absorb(r);
   }
 
 }
