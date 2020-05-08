 package dev.simple;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class organizes the surfaces in a collection, in this case Ribbons
  * 
  * @author John Stewart
  */
 public class Tower {
 
   public List<Double> Lx = new ArrayList<Double>();
   public List<Double> Ly = new ArrayList<Double>();
   public List<Ribbon> LS = new ArrayList<Ribbon>();
 
   /**
    * This constructor makes all the ribbons, with the points given in counter-clock wise order
    * 
    * @param Lx X-coordinates of points
    * @param Ly Y-coordinates of points
    */
   public Tower(List<Double> Lx, List<Double> Ly) {
     this.Lx.addAll(Lx);
     this.Ly.addAll(Ly);
     for (int i = 0; i < Lx.size() - 1; i++)
       LS.add(new Ribbon(Lx.get(i), Ly.get(i), Lx.get(i + 1), Ly.get(i + 1)));
     LS.add(new Ribbon(Lx.get(Lx.size() - 1), Ly.get(Lx.size() - 1), Lx.get(0), Ly.get(0)));
   }
 
   /**
    * Returns true if the photon is within the tower
    * 
    * @param p The photon to check
    * @return Returns true if the photon is inside
    */
   public boolean isInside(Photon p) {
     boolean inside = false;
    for (int i = 0; i < LS.size() - 1; i++)
       inside = inside ^ LS.get(i).checkRight(p);
     return inside;
   }
 
 }
