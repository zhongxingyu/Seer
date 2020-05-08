 package com.untamedears.xppylons;
 
 import java.util.ArrayList;
 import rtree.AABB;
 import rtree.BoundedObject;
 
 public class Pylon implements BoundedObject {
     private PylonSet cluster;
     private int x, y, z;
     private double radius;
     private int height;
     private Pylon.EffectBounds influence;
     
     public Pylon(PylonSet cluster, int x, int y, int z, int height) {
         this.cluster = cluster;
         this.x = x;
         this.y = y;
         this.z = z;
         this.height = height;
         this.influence = new Pylon.EffectBounds(this);
         
         double maxHeight = cluster.getConfig().getMaxPylonHeight();
         double maxRadius = cluster.getConfig().getMaximumRadius();
         this.radius = Math.sqrt(height / maxHeight) * maxRadius;
     }
     
     public AABB getBounds() {
         AABB boundingBox = new AABB();
         boundingBox.setMinCorner((double) x - 2, (double) y - 1, (double) z - 2);
         boundingBox.setMaxCorner((double) x + 2, (double) y + 2 + height, (double) z + 2);
         return boundingBox;
     }
     
     public int getX() {
         return x;
     }
     
     public int getY() {
         return y;
     }
     
     public int getZ() {
         return z;
     }
     
     public int getHeight() {
         return height;
     }
     
     public PylonSet getCluster() {
         return cluster;
     }
     
     public double getRadiusOfEffect() {
         return radius;
     }
     
     public Pylon.EffectBounds getInfluence() {
         return influence;
     }
     
     public class EffectBounds implements BoundedObject {
         private Pylon pylon;
         
         public EffectBounds(Pylon pylon) {
             this.pylon = pylon;
         }
         
         public Pylon getPylon() {
             return pylon;
         }
         
         public boolean affects(double x, double z) {
             return getStrengthAt(x, z) > 0;
         }
         
         public double getStrengthAt(double x, double z) {
             double radius = pylon.getRadiusOfEffect();
             double radiusSq = radius * radius;
             double dx = pylon.getX() - x;
             double dz = pylon.getZ() - z;
             double distSq = dx * dx + dz * dz;
             if (distSq > radiusSq) {
                 return 0;
             } else {
                 double dist = Math.sqrt(radius);
                 double depletionScale = pylon.getCluster().getConfig().getPylonDepletion();
                 double strength = depletionScale * Math.sqrt(dist / radius);
                 return strength;
             }
         }
         
         public double getShareAt(double x, double z) {
             double totalStrength = 0.0;
             double residual = 1.0;
             
             for (Pylon other : pylon.getCluster().pylonsInfluencing(x, z)) {
                 double strengthAtPoint = other.getInfluence().getStrengthAt(x, z);
                 totalStrength += strengthAtPoint;
                 residual = residual * (1.0 - strengthAtPoint);
             }
             
             if (totalStrength <= 0.0 || residual <= 0.0 || residual > 1.0) {
                 return 0.0;
             }
             
             double draw = 1.0 - residual;
            double share = (getStrengthAt(x, z) / totalStrength) * draw;
             return share;
         }
         
         public AABB getBounds() {
             double radius = pylon.getRadiusOfEffect();
             AABB boundingBox = new AABB();
             boundingBox.setMinCorner(x - radius, 0, z - radius);
             boundingBox.setMaxCorner(x + radius, 256, z + radius);
             return boundingBox;
         }
     }
 }
