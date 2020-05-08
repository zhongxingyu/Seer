 
 package TSP;
 
 /**
  *
  * Drew McDermott
  * adm75@pitt.edu
  */
 import java.util.ArrayList;
 import java.awt.*;
 
 public class GraphVertex 
 {
     private Point p;
     private ArrayList<GraphVertex> conn;
     public boolean Marked = false;
     
     public GraphVertex(int x, int y)
     {
         p = new Point(x,y);
     }
     
     public void connectTo(GraphVertex gv)
     {
         gv.conn.add(this);
         conn.add(gv);
     }
     
     public double distanceTo(GraphVertex gv)
     {
         return p.distance(gv.p);
     }
     
     public Point getLocation()
     {
         return p;
     }
     public  ArrayList<GraphVertex> getConnected()
     {
         return conn;
     }
     
 
 }
