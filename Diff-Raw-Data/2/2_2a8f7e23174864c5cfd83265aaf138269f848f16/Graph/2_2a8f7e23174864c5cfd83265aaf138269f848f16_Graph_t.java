 package graph;
 
 import java.util.Iterator;
 
 import controller.Controller;
 import dataStructure.Connection;
 import dataStructure.DynArray;
 import dataStructure.Point;
 
 /**Graph handler class for points. 
  * This works on a graph. 
  * This is used for finding shortest path between points in map.
  * 
  * @author Claus L. Henriksen - clih@itu.dk
  * @see EdgeWeightedDigraph
  * @see DirectedEdge
  */
 public class Graph {
   private EdgeWeightedDigraph g;
   private Connection[] connections;
   private double xMin = 70000, xMax = 0, yMin = 70000, yMax = 0;
   
   public Graph(){
     //Get points 
     Point[] points = Controller.getInstance().getPoints();
     
     //Get roads
     connections = Controller.getInstance().getConnections();
 
     //Create graph
     g = new EdgeWeightedDigraph(points.length+1); //vertices
     
     //add edges
     for(Connection c : connections){
       if(c != null){
         //from, to, connection ID, weight
         g.addEdge(new DirectedEdge(c.getLeft().getID(), c.getRight().getID(), c.getID(), c.getWeight()));
         g.addEdge(new DirectedEdge(c.getRight().getID(), c.getLeft().getID(), c.getID(), c.getWeight()));
       }
     }
   }
   
   /**
    * Find shortest path between two points.
    * @param from
    * @param to
    * @return Path as array of connections
    * @throws RuntimeException if there is no path between points
    * @see Connection
    * @see DijkstraSP
    */
   public Connection[] shortestPath(Point from, Point to) throws RuntimeException{
    //Reset values
    xMin = 70000; xMax = 0; yMin = 70000; yMax = 0;
     //Create Dijkstra
     DijkstraSP dijk = new DijkstraSP(g, from.getID());
     //If there is no path between points
     if(!dijk.hasPathTo(to.getID())) throw new RuntimeException("No path");
     
     //Iterate points on path and get Connection IDs
     DynArray<Integer> cs = new DynArray<Integer>(Integer[].class);
     Iterator<DirectedEdge> it = dijk.pathTo(to.getID()).iterator();
     while(it.hasNext()){
       DirectedEdge edge = it.next();
       cs.add(edge.id());
     }
     
     //Convert IDs to actual connections and return them
     Connection[] path = new Connection[cs.size()];
     int index = 0;
     for(Integer i : cs){
       Connection con = connections[i];
       path[index++] = con;
       
       //Set xMin, xMax, yMin, yMax. These are used for zooming in on route
       double xLow = con.getLeft().getX();
       double xHigh = con.getRight().getX();
       double yLow, yHigh;
       if(con.getY1() < con.getY2()){
         yLow = con.getY1();
         yHigh = con.getY2();
       }else{
         yLow = con.getY2();
         yHigh = con.getY1();
       }
       
       if(xLow  < xMin) xMin = xLow;
       if(yLow  < yMin) yMin = yLow;
       if(xHigh > xMax) xMax = xHigh;
       if(yHigh > yMax) yMax = yHigh;
     }
     return path;
   }
   
   /**
    * Getter for path boundary limit value.
    * Used by MapComponent to zoom in on route.
    * @return Smallest x-value on route
    */
   public double getXmin(){
     return xMin;
   }
   
   /**
    * Getter for path boundary limit value.
    * Used by MapComponent to zoom in on route.
    * @return Biggest x-value on route
    */
   public double getXmax(){
     return xMax;
   }
   
   /**
    * Getter for path boundary limit value.
    * Used by MapComponent to zoom in on route.
    * @return Smallest y-value on route
    */
   public double getYmin(){
     return yMin;
   }
   
   /**
    * Getter for path boundary limit value.
    * Used by MapComponent to zoom in on route.
    * @return Biggest y-value on route
    */
   public double getYmax(){
     return yMax;
   }
 }
