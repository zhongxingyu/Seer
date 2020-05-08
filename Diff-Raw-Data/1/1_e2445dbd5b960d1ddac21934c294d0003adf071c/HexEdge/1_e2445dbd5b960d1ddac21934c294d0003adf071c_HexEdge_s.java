 package edu.uwm.cs552;
 
 import java.awt.Point;
 
 import edu.uwm.cs552.util.Pair;
 
 public class HexEdge {
 
   private HexCoordinate coor;
   private HexDirection dir;
 
   public HexEdge(HexCoordinate h, HexDirection d) {
     coor = h;
     dir = d;
   }
 
   public double a() {
     HexCoordinate h = coor.move(dir);
     double hp = h.a();
     double cp = coor.a();
     return (hp + cp) / 2;
   }
 
   public double b() {
     HexCoordinate h = coor.move(dir);
     double hp = h.b();
     double cp = coor.b();
     return (hp + cp) / 2;
   }
 
   public static HexEdge fromString(String string) {
     String[] s = string.split("@");
     return new HexEdge(HexCoordinate.fromString(s[0]),
         HexDirection.valueOf(s[1]));
   }
 
   public Pair<Point, Point> toLineSegment(double s) {
     HexCoordinate h = coor.move(dir);
     Point hp = h.toPoint(s);
     Point cp = coor.toPoint(s);
 
     Point mid = new Point((int) Math.round((hp.getX() + cp.getX()) / 2),
         (int) Math.round((hp.getY() + cp.getY()) / 2));
 
     Pair<Point, Point> p = new Pair<Point, Point>(new Point(mid.x,
         (int) Math.round(mid.getY() + (s * SQRT32) / 3)), new Point(mid.x,
         (int) Math.round(mid.getY() - (s * SQRT32) / 3)));
 
     switch (dir) {
     case NORTHEAST:
     case SOUTHWEST:
       p = rotateLineSegment(p, mid, -120);
       break;
     case EAST:
     case WEST:
       break;
     case SOUTHEAST:
     case NORTHWEST:
       p = rotateLineSegment(p, mid, 120);
       break;
     }
     return p;
   }
 
   public static HexEdge fromPoint(Point p, double scale) {
     HexCoordinate on = HexCoordinate.fromPoint(p, scale);
     double distance = Double.MAX_VALUE;
 
     HexDirection direction = null;
 
     for (HexDirection d : HexDirection.values()) {
       Point n = on.move(d).toPoint(scale);
 
       if (p.distance(n.x, n.y) < distance) {
         distance = p.distance(n.x, n.y);
         direction = d;
       }
     }
 
     return new HexEdge(on, direction);
   }
 
   @Override
   public String toString() {
     return coor.toString() + "@" + dir.name();
   }
 
   public boolean equals(Object o) {
     if (!(o instanceof HexEdge))
       return false;
 
     HexEdge h = (HexEdge) o;
     HexCoordinate c1 = coor, c2 = h.coor;
     HexDirection d1 = dir, d2 = h.dir;
 
     if (dir == HexDirection.WEST || dir == HexDirection.SOUTHWEST
         || dir == HexDirection.NORTHWEST) {
       c1 = c1.move(dir);
       d1 = d1.reverse();
     }
     if (h.dir == HexDirection.WEST || h.dir == HexDirection.SOUTHWEST
         || h.dir == HexDirection.NORTHWEST) {
       c2 = c2.move(h.dir);
       d2 = d2.reverse();
     }
 
     return c1.equals(c2) && d1 == d2;
   }
 
   public int hashCode() {
     HexDirection d = dir;
     HexCoordinate c = coor;
 
     if (dir == HexDirection.WEST || dir == HexDirection.SOUTHWEST
         || dir == HexDirection.NORTHWEST) {
       c = c.move(dir);
       d = d.reverse();
     }
 
     return c.hashCode() + (d.ordinal() * 1001);
   }
 
   private Pair<Point, Point> rotateLineSegment(Pair<Point, Point> points,
       Point mid, double angle) {
     angle = Math.toRadians(angle);
     double sin = Math.sin(angle);
     double cos = Math.cos(angle);
 
     int nx1 = (int) Math.round(mid.x + (points.fst.x - mid.x) * cos
         - (points.fst.y - mid.y) * sin);
     int ny1 = (int) Math.round(mid.y + (points.fst.x - mid.x) * sin
         - (points.fst.y - mid.y) * cos);
     int nx2 = (int) Math.round(mid.x + (points.snd.x - mid.x) * cos
         - (points.snd.y - mid.y) * sin);
     int ny2 = (int) Math.round(mid.y + (points.snd.x - mid.x) * sin
         - (points.snd.y - mid.y) * cos);
     return new Pair<Point, Point>(new Point(nx1, ny1), new Point(nx2, ny2));
   }
 
   private static final double SQRT32 = Math.sqrt(3.0) / 2.0;
 }
