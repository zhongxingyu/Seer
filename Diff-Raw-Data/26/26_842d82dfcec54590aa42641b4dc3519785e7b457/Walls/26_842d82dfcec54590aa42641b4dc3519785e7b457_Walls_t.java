 package dev.simple;
 
import pcm.util.V;
 import pcm.util.Vector;
 
 public class Walls extends Surface {
 
   @Override
   public double collisionDistance(Photon p) {
     double d = Double.POSITIVE_INFINITY;
     double c = -p.r.x / p.n.x;
     if (c > 0 && c < d && p.n.x < 0)
       d = c;
     c = -p.r.y / p.n.y;
     if (c > 0 && c < d && p.n.y < 0)
       d = c;
     c = (Photon.X - p.r.x) / p.n.x;
     if (c > 0 && c < d && p.n.x > 0)
       d = c;
     c = (Photon.Y - p.r.y) / p.n.y;
     if (c > 0 && c < d && p.n.y > 0)
       d = c;
     return d;
   }
 
   @Override
   public boolean collision(Photon p) {
     double d = collisionDistance(p);
     p.move(d);
     p.stat.addPath(p.r);
    if (p.r.x < V.EPS || (Photon.X - p.r.x) < V.EPS)
       p.move(new Vector(Photon.X - p.r.x, p.r.y, p.r.z));
    if (p.r.y < V.EPS || (Photon.Y - p.r.y) < V.EPS)
       p.move(new Vector(p.r.x, Photon.Y - p.r.y, p.r.z));
    //    if (p.r.x < Photon.X / 2 ^ p.n.x > 0)
    //      p.move(new Vector(Photon.X - p.r.x, p.r.y, p.r.z));
    //    if (p.r.y < Photon.Y / 2 ^ p.n.y > 0)
    //      p.move(new Vector(p.r.x, Photon.Y - p.r.y, p.r.z));
     p.stat.newBranch(p.r);
     return false;
   }
 
 }
