 package de.engine.math;
 
 import de.engine.objects.Circle;
 import de.engine.objects.Ground;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.Polygon;
 import de.engine.objects.Square;
 
 public class Util
 {
     // needed for derivation
    private static final Double h = Math.pow(10d, -7d);
    private static final Double u = Math.pow(10d, 12d);
     private static Vector function = new Vector();
     private static double  m = 0;
     private static double  n = 0;
     private static int     sign   = 1;
     public  static double  used_m = 0;
     private static boolean set_slope = false;
     // has to be set before using Newton Iteration
     public static ObjectProperties object;
     public static Ground           ground;
     
     
     public static double distance(Vector p1, Vector p2)
     {
         double x = p2.getX() - p1.getX();
         double y = p2.getY() - p1.getY();
         return Math.hypot(x, y);
     }
     
     public static double distanceToOrigin(Vector p)
     {
         return Math.hypot(p.getX(), p.getY());
     }
     
     public static Vector add(Vector vec1, Vector vec2)
     {
         return new Vector(vec1.getX() + vec2.getX(), vec1.getY() + vec2.getY());
     }
     
     public static Vector minus(Vector vec1, Vector vec2)
     {
         return new Vector(vec1.getX() - vec2.getX(), vec1.getY() - vec2.getY());
     }
     
     public static Vector scale(Vector v, double s)
     {
         return new Vector(v.getX() * s, v.getY() * s);
     }
     
     public static double scalarProduct(Vector vec1, Vector vec2)
     {
         return vec1.getX() * vec2.getX() + vec1.getY() * vec2.getY();
     }
     
     public static double crossProduct(Vector vec1, Vector vec2)
     {
         return vec1.getX() * vec2.getY() - vec1.getY() * vec2.getX();
     }
     
     public static double getAngle(Vector vec1, Vector vec2)
     {
         return Math.atan2(vec2.getY() - vec1.getY(), vec2.getX() - vec1.getX());
     }
     
     
     public static Double newtonIteration()
     {
         // If the object has no velocity in x direction, return the objects x-coordinate.
         if (object.velocity.getX() == 0)
             return object.getPosition().getX();
         
         Double xn = object.getPosition().getX();
         
         if (!set_slope) 
         {
             // m = slope, n = shift in y, linear function
             m = object.velocity.getY() / object.velocity.getX();
             n = object.getPosition().getY() - m*xn;
             
             sign = (object.velocity.getY()<0) ? 1 : -1;
         }
         
         // interpolates intersections for slopes between -0.15..0.15
         if (m<=0.15 && m>=-0.15) 
         {
             double oldm = m;
           
             set_slope = true;
           
             m = 0.16;
             n = object.getPosition().getY() - m*xn;
             sign = object.velocity.getX()>=0 ? -1 :  1;
             double i1 = newtonIteration();
           
             m = -0.16;
             n = object.getPosition().getY() - m*xn;
             sign = object.velocity.getX()>=0 ?  1 : -1;
             double i2 = newtonIteration();
 
             set_slope = false;
           
             return i1 + Math.abs(i1-i2) * ((oldm-0.15)/0.32);
         }
         
         // the ordinary newton iteration
         for (int i = 0; i < 8; i++)
         {
             double fktValue = newFkt( xn );
             xn = xn - fktValue / derive1D( fktValue ); 
         }
         
         used_m = m;
 
         return xn;
     }
     
     /**
      * Calculates the 1st derivation of the function given in <i>functions</i>.
      * 
      * @param x - determines the point of which the derivation is wanted
      * @param object -
      * @param ground
      * @return
      */
     public static Double derive1D( Double x )
     {
         // df(x) = ( -f(x+2h) +8f(x+h) - 8f(x-h) + f(x-2h) ) / 12h
         return sign * ( -newFkt( x+2d*u ) + 8d*newFkt( x+u ) - 8d*newFkt( x-u ) + newFkt( x-2d*u )) / (2d*u);
     }
     
     public static Double derive1Dr( Double x )
     {
         // df(x) = ( f(x+h) - f(x-h) ) / 2h
         return (functions(x+h).getY() - functions(x-h).getY()) / (2d*h);
     }
     
     public static Double newFkt(Double x)
     {
         Vector function = functions(x);
         return function.getY() - function.getX();
     }
     
     public static Vector functions(Double x)
     {
         function.setX( m*x + n );
         function.setY( ground.function(x) );
 
 //        System.out.println( m + "*x "+ (n<0 ? "":"+") + n);
         
         return function;
     }
 
     
     
     
     
     // ************************************************************
 
     /**
      * returns all axis between a Polygon and a Circle for the seperating axis theorem
      */
     public static Vector[] getAxis(Circle c, Polygon p, double time)
     {
         return mergeArrays(getAxis(p, time), getAxisToPoint(p, c.getPosition(time), time));
     }
     
     /**
      * returns all axis between two Polygons  for the seperating axis theorem
      */
     public static Vector[] getAxis(Polygon p1, Polygon p2, double time)
     {
         return mergeArrays(getAxis(p1, time), getAxis(p2, time));
     }
     
     private static Vector[] getAxisToPoint(Polygon p, Vector point, double time)
     {
         Vector[] axis = new Vector[p.points.length];
         for (int i = 0; i < p.points.length; i++)
         {
             axis[i] = Util.minus(p.getWorldPointPos(i, time), point).getUnitVector();
         }
         return axis;
     }
     
     private static Vector[] getAxis(Polygon p, double time)
     {
         Vector[] axis;
         if (p instanceof Square)
         {
             axis = new Vector[2];
             axis[0] = Util.minus(p.getWorldPointPos(1, time), p.getWorldPointPos(0, time)).getNormalVector().getUnitVector();
             axis[1] = Util.minus(p.getWorldPointPos(2, time), p.getWorldPointPos(1, time)).getNormalVector().getUnitVector();
         }
         else
         {
             axis = new Vector[p.points.length];
             for (int i = 0; i < p.points.length; i++)
             {
                 int j = (i == p.points.length - 1) ? 0 : i + 1;
                 axis[i] = Util.minus(p.getWorldPointPos(i, time), p.getWorldPointPos(j, time)).getNormalVector().getUnitVector();
             }
         }
         return axis;
     }
     
     private static Vector[] mergeArrays(Vector[] v1, Vector[] v2)
     {
         Vector[] array;
         array = new Vector[v1.length + v2.length];
         for (int i = 0; i < array.length; i++)
         {
             if (i < v1.length)
                 array[i] = v1[i];
             else
             {
                 array[i] = v2[i - v1.length];
             }
         }
         return array;
     }
     
     public static Vector crossEdges(Vector pos1, Vector edge1, Vector pos2, Vector edge2)
     {
         Vector pos1n, pos2n, edge1n, edge2n;
         if ((edge1.getX() == 0 && edge1.getY() == 0) || (edge2.getX() == 0 && edge2.getY() == 0))
             return null;
         if (edge1.getY() == 0 && edge2.getY() != 0)
         {
             pos1n = pos2;
             pos2n = pos1;
             edge1n = edge2;
             edge2n = edge1;
         }
         else if (edge1.getY() != 0)
         {
             pos1n = pos1;
             pos2n = pos2;
             edge1n = edge1;
             edge2n = edge2;
         }
         else
         {
             return null;
         }
         
         double s2 = ((pos2n.getY() - pos1n.getY()) * (edge1n.getX() / edge1n.getY()) - (pos2n.getX() - pos1n.getX())) / (edge2n.getX() - (edge2n.getY() * edge1n.getX() / edge1n.getY()));
         double s1 = ((edge2n.getY() * s2) + (pos2n.getY() - pos1n.getY())) / edge1n.getY();
         
         if (s2 >= 0 && s2 <= 1 && s1 >= 0 && s1 <= 1)
         {
             return new Vector(pos1n.getX() + s1 * edge1n.getX(), pos1n.getY() + s1 * edge1n.getY());
         }
         
         return null;
     }
 }
