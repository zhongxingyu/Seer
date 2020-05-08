 package org.glowacki.core.dungen;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.glowacki.core.astar.INode;
 import org.glowacki.core.astar.PathFinder;
 
 /**
  * Find a path on the specified map.
  */
 public class RoomFinder
     extends PathFinder
 {
     private MapNode[][] nodes;
 
     /**
      * Create a path finder for the specified map
      *
      * @param map map
      */
     public RoomFinder(MapNode[][] map)
     {
         nodes = map;
     }
 
     /**
      * Create a temporary node
      *
      * @param x X coordinate
      * @param y Y coordinate
      *
      * @return new temporary node
      */
     public INode createTempNode(INode node)
     {
        return new MapNode(node.getX(), node.getY());
     }
 
     /**
      * Find the best path from <tt>startPt</tt> to <tt>endPt</tt>.
      *
      * @param startPt starting point
      * @param endPt ending point
      *
      * @return list of points in the path
      *
      * @throws GeneratorException if the start or end point is bad
      */
     public List<MapNode> findBestPath(MapNode startPt, MapNode endPt)
         throws GeneratorException
     {
         if (startPt.getX() < 0 || startPt.getX() >= nodes.length ||
             startPt.getY() < 0 || startPt.getY() >= nodes[0].length)
         {
             final String msg =
                 String.format("Bad start point [%d,%d]", startPt.getX(),
                               startPt.getY());
             throw new GeneratorException(msg);
         }
 
         if (endPt.getX() < 0 || endPt.getX() >= nodes.length ||
             endPt.getY() < 0 || endPt.getY() >= nodes[0].length)
         {
             final String msg =
                 String.format("Bad end point [%d,%d]", endPt.getX(),
                               endPt.getY());
             throw new GeneratorException(msg);
         }
 
         for (int x = 0; x < nodes.length; x++) {
             for (int y = 0; y < nodes[x].length; y++) {
                 nodes[x][y].clear();
             }
         }
 
         MapNode start = nodes[startPt.getX()][startPt.getY()];
         MapNode end = nodes[endPt.getX()][endPt.getY()];
 
         start.setEndPoint(MapNode.EndPoint.START);
         end.setEndPoint(MapNode.EndPoint.END);
 
         List<INode> list;
         try {
             list = super.findBestPath(start, end);
         } finally {
             start.clear();
             end.clear();
         }
 
         if (list == null) {
             return null;
         }
 
         List<MapNode> bestList = new ArrayList<MapNode>();
         for (INode node : list) {
             if (!(node instanceof MapNode)) {
                 throw new Error("Found non-MapEntry node " + node + "<" +
                                 node.getClass().getName() + ">");
             }
             bestList.add((MapNode) node);
         }
 
         return bestList;
     }
 
     /**
      * Get movable nodes adjacent to the specified node.
      *
      * @param node center node
      *
      * @return set of adjacent nodes
      */
     public Set<INode> getAdjacencies(INode node)
     {
 //try{drawCosts(node);}catch(Throwable thr){thr.printStackTrace();}
         if (!(node instanceof MapNode)) {
             throw new Error("Unexpected node " + node.getClass().getName());
         }
 
         Set<INode> sorted = new TreeSet<INode>();
         if (node.getX() > 0) {
             sorted.add(nodes[node.getX() - 1][node.getY()]);
         }
         if (node.getX() < nodes.length - 1) {
             sorted.add(nodes[node.getX() + 1][node.getY()]);
         }
         if (node.getY() > 0) {
             sorted.add(nodes[node.getX()][node.getY() - 1]);
         }
         if (node.getY() < nodes[0].length - 1) {
             sorted.add(nodes[node.getX()][node.getY() + 1]);
         }
 
         return sorted;
     }
 private INode prevNode = null;
 private void drawCosts(INode node)
 {
     StringBuilder buf = new StringBuilder();
     for (int x = 0; x < nodes.length; x++) {
         buf.append(" ---");
     }
 
     final String sep = buf.toString();
 
     final int px, py;
     if (prevNode == null) {
         px = node.getX();
         py = node.getY();
     } else {
         px = prevNode.getX();
         py = prevNode.getY();
     }
     prevNode = node;
 
     System.out.format("** Costs for %d,%d **\n", px, py);
     for (int y = 0; y < nodes[0].length; y++) {
         System.out.println(sep);
         for (int x = 0; x < nodes.length; x++) {
             final double cost = nodes[x][y].getPassThrough(node);
             if (cost < 1000.0) {
                 System.out.format("|%3.0f", cost);
             } else {
                 System.out.format("| %c ", nodes[x][y].getChar());
             }
         }
         System.out.println("|");
     }
     System.out.println();
 }
 }
