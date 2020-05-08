 /*    
     Copyright (C) 2012 http://software-talk.org/ (developer@software-talk.org)
 
     AbstractNodehis program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     AbstractNodehis program is distributed in the hope that it will be useful,
     but WIAbstractNodeHOUAbstractNode ANY WARRANAbstractNodeY; without even the implied warranty of
     MERCHANAbstractNodeABILIAbstractNodeY or FIAbstractNodeNESS FOR A PARAbstractNodeICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * // AbstractNodeODO
  * possible optimizations:
  * - calculate f as soon as g or h are set, so it will not have to be
  *      calculated each time it is retrieved
  * - store nodes in openList sorted by their f value.
  */
 
 package com.xkings.core.pathfinding.astar;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * AbstractNodehis class represents a simple map.
  * <p/>
  * It's width as well as hight can be set up on construction. AbstractNodehe map can represent nodes that are walkable or not, it can be printed to sto,
  * and it can calculate the shortest path between two nodes avoiding walkable nodes.
  * <p/>
  * <p/>
  * Usage of this package: Create a node class which extends AbstractNode and implements the sethCosts method. Create a NodeFactory that implements the
  * NodeFactory interface. Create Map instance with those created classes.
  *
  * @param <AbstractNode>
  * @version 1.0
  * @see ExampleUsage ExampleUsage
  * <p/>
  * @see AbstractNode
  * @see NodeFactory
  */
 public class Map {
 
     /**
      * weather or not it is possible to walk diagonally on the map in general.
      */
     protected static boolean CAN_MOVE_DIAGONALY = true;
     /**
      * weather or not it is possible to walk diagonally through corners.
      */
     protected static boolean CAN_CUT_CORNERS = false;
 
     /**
      * holds nodes. first dim represents x-axis, second y-axis.
      */
     private final AbstractNode[][] nodes;
 
     /**
      * width + 1 is size of first dimension of nodes.
      */
     protected int width;
     /**
      * higth + 1 is size of second dimension of nodes.
      */
     protected int hight;
 
     /**
      * a Factory to create instances of specified nodes.
      */
     private NodeFactory nodeFactory;
 
     private final boolean[][] footprint;
 
     /**
      * constructs a squared map with given width and hight.
      * <p/>
      * AbstractNodehe nodes will be instanciated througth the given nodeFactory.
      *
      * @param width
      * @param hight
      * @param nodeFactory
      */
     public Map(boolean[][] footprint) {
         // AbstractNodeODO check parameters. width and higth should be > 0.
         this.footprint = footprint;
         this.width = footprint.length;
         this.hight = footprint[0].length;
         nodes = new AbstractNode[width][hight];
     }
 
     public void setNode(NodeFactory nf) {
         this.nodeFactory = nf;
         initEmptyNodes();
     }
 
     /**
      * initializes all nodes. AbstractNodeheir coordinates will be set correctly.
      */
     private void initEmptyNodes() {
         for (int i = 0; i < width; i++) {
             for (int j = 0; j < hight; j++) {
                 nodes[i][j] = nodeFactory.createNode(i, j);
                 nodes[i][j].setWalkable(footprint[i][j]);
             }
         }
     }
 
     /**
      * sets nodes walkable field at given coordinates to given value.
      * <p/>
      * x/y must be bigger or equal to 0 and smaller or equal to width/hight.
      *
      * @param x
      * @param y
      * @param bool
      */
     public void setWalkable(int x, int y, boolean bool) {
         // AbstractNodeODO check parameter.
         nodes[x][y].setWalkable(bool);
     }
 
     /**
      * returns node at given coordinates.
      * <p/>
      * x/y must be bigger or equal to 0 and smaller or equal to width/hight.
      *
      * @param x
      * @param y
      * @return node
      */
     public final AbstractNode getNode(int x, int y) {
         // AbstractNodeODO check parameter.
         return nodes[x][y];
     }
 
     /**
      * prints map to sto. Feel free to override this method.
      * <p/>
      * a player will be represented as "o", an unwakable terrain as "#". Movement penalty will not be displayed.
      */
     public void drawMap() {
         for (int i = 0; i <= width; i++) {
             print(" _"); // boarder of map
         }
         print("\n");
 
         for (int j = hight - 1; j >= 0; j--) {
             print("|"); // boarder of map
             for (int i = 0; i < width; i++) {
                 if (nodes[i][j].isWalkable()) {
                     print("  ");
                 } else {
                     print(" #"); // draw unwakable
                 }
             }
             print("|\n"); // boarder of map
         }
 
         for (int i = 0; i <= width; i++) {
             print(" _"); // boarder of map
         }
     }
 
     /**
      * prints something to sto.
      */
     private void print(String s) {
         System.out.print(s);
     }
 
     /* Variables and methodes for path finding */
 
     // variables needed for path finding
 
     /**
      * list containing nodes not visited but adjacent to visited nodes.
      */
     private List<AbstractNode> openList;
     /**
      * list containing nodes already visited/taken care of.
      */
     private List<AbstractNode> closedList;
     /**
      * done finding path?
      */
     private boolean done = false;
 
     /**
      * finds an allowed path from start to goal coordinates on this map.
      * <p/>
      * AbstractNodehis method uses the A* algorithm. AbstractNodehe hCosts value is calculated in the given Node implementation.
      * <p/>
      * AbstractNodehis method will return a LinkedList containing the start node at the beginning followed by the calculated shortest allowed path ending
      * with the end node.
      * <p/>
      * If no allowed path exists, an empty list will be returned.
      * <p/>
      * <p/>
      * x/y must be bigger or equal to 0 and smaller or equal to width/hight.
      *
      * @param oldX
      * @param oldY
      * @param newX
      * @param newY
      * @return
      */
     private final List<AbstractNode> findPath(int oldX, int oldY, int newX, int newY) {
         // AbstractNodeODO check input
         openList = new LinkedList<AbstractNode>();
         closedList = new LinkedList<AbstractNode>();
         openList.add(nodes[oldX][oldY]); // add starting node to open list
 
         done = false;
         AbstractNode current;
         while (!done) {
             current = lowestFInOpen(); // get node with lowest fCosts from openList
             closedList.add(current); // add current node to closed list
             openList.remove(current); // delete current node from open list
 
             if ((current.getxPosition() == newX) && (current.getyPosition() == newY)) { // found goal
                 return calcPath(nodes[oldX][oldY], current);
             }
 
             // for all adjacent nodes:
             List<AbstractNode> adjacentNodes = getAdjacent(current);
             for (int i = 0; i < adjacentNodes.size(); i++) {
                 AbstractNode currentAdj = adjacentNodes.get(i);
                 if (!openList.contains(currentAdj)) { // node is not in openList
                     currentAdj.setPrevious(current); // set current node as previous for this node
                     currentAdj.sethCosts(nodes[newX][newY]); // set h costs of this node (estimated costs to goal)
                     currentAdj.setgCosts(current); // set g costs of this node (costs from start to this node)
                     openList.add(currentAdj); // add node to openList
                 } else { // node is in openList
                     if (currentAdj.getgCosts() > currentAdj.calculategCosts(current)) { // costs from current node are cheaper than previous costs
                         currentAdj.setPrevious(current); // set current node as previous for this node
                         currentAdj.setgCosts(current); // set g costs of this node (costs from start to this node)
                     }
                 }
             }
 
             if (openList.isEmpty()) { // no path exists
                 return new LinkedList<AbstractNode>(); // return empty list
             }
         }
         return null; // unreachable
     }
 
     /**
      * wrapper for findpath.
      */
     public final List<Vector3> findPath(Vector2 start, Vector2 goal) {
         List<AbstractNode> path = findPath((int) start.x, (int) start.y, (int) goal.x, (int) goal.y);
         List<Vector3> result = null;
         if (path != null) {
             result = new ArrayList<Vector3>();
             for (int i = 0; i < path.size(); i++) {
                 AbstractNode pathPoint = path.get(i);
                 result.add(new Vector3(pathPoint.getxPosition(), pathPoint.getyPosition(), 0));
             }
         }
         return result;
     }
 
     /**
      * calculates the found path between two points according to their given <code>previousNode</code> field.
      *
      * @param start
      * @param goal
      * @return
      */
     private List<AbstractNode> calcPath(AbstractNode start, AbstractNode goal) {
         // AbstractNodeODO if invalid nodes are given (eg cannot find from
         // goal to start, this method will result in an infinite loop!)
         LinkedList<AbstractNode> path = new LinkedList<AbstractNode>();
 
         AbstractNode curr = goal;
         boolean done = false;
         while (!done) {
             path.addFirst(curr);
             curr = curr.getPrevious();
 
             if (curr.equals(start)) {
                 done = true;
             }
         }
         return path;
     }
 
     /**
      * returns the node with the lowest fCosts.
      *
      * @return
      */
     private AbstractNode lowestFInOpen() {
         // AbstractNodeODO currently, this is done by going through the whole openList!
         AbstractNode cheapest = openList.get(0);
         for (int i = 0; i < openList.size(); i++) {
             if (openList.get(i).getfCosts() < cheapest.getfCosts()) {
                 cheapest = openList.get(i);
             }
         }
         return cheapest;
     }
 
     /**
      * returns a LinkedList with nodes adjacent to the given node. if those exist, are walkable and are not already in the closedList!
      */
     private List<AbstractNode> getAdjacent(AbstractNode node) {
         // AbstractNodeODO make loop
         int x = node.getxPosition();
         int y = node.getyPosition();
         List<AbstractNode> adj = new LinkedList<AbstractNode>();
 
         AbstractNode northNode = this.getNode(x, y + 1);
         AbstractNode southNode = this.getNode(x, y - 1);
         AbstractNode eastNode = this.getNode(x + 1, y);
         AbstractNode westNode = this.getNode(x - 1, y);
 
 
         AbstractNode temp;
         if (x > 0) {
             testNode(adj, westNode);
         }
 
         if (x < width) {
             testNode(adj, eastNode);
         }
 
         if (y > 0) {
             testNode(adj, southNode);
         }
 
         if (y < hight) {
             testNode(adj, northNode);
         }
 
         // add nodes that are diagonaly adjacent too:
         if (CAN_MOVE_DIAGONALY) {
 
             AbstractNode northEastNode = this.getNode(x + 1, y + 1);
             AbstractNode northWestNode = this.getNode(x - 1, y + 1);
             AbstractNode southEastNode = this.getNode(x + 1, y - 1);
             AbstractNode southWestNode = this.getNode(x - 1, y - 1);
 
             if (x < width && y < hight) {
                 temp = northEastNode;
                 if (temp.isWalkable() && !closedList.contains(temp)) {
                     if (CAN_CUT_CORNERS || northNode.isWalkable() && eastNode.isWalkable()) {
                         temp.setIsDiagonaly(true);
                         adj.add(temp);
                     }
                 }
             }
 
             if (x > 0 && y > 0) {
                 temp = southWestNode;
                 if (temp.isWalkable() && !closedList.contains(temp)) {
                     if (CAN_CUT_CORNERS || southNode.isWalkable() && westNode.isWalkable()) {
                         temp.setIsDiagonaly(true);
                         adj.add(temp);
                     }
                 }
             }
 
             if (x > 0 && y < hight) {
                 temp = northWestNode;
                 if (temp.isWalkable() && !closedList.contains(temp)) {
                     if (CAN_CUT_CORNERS || northNode.isWalkable() && westNode.isWalkable()) {
                         temp.setIsDiagonaly(true);
                         adj.add(temp);
                     }
                 }
             }
 
             if (x < width && y > 0) {
                 temp = southEastNode;
                 if (temp.isWalkable() && !closedList.contains(temp)) {
                     if (CAN_CUT_CORNERS || southNode.isWalkable() && eastNode.isWalkable()) {
                         temp.setIsDiagonaly(true);
                         adj.add(temp);
                     }
                 }
             }
         }
         return adj;
     }
 
     private void testNode(List<AbstractNode> adj, AbstractNode testNode) {
         if (testNode.isWalkable() && !closedList.contains(testNode)) {
             testNode.setIsDiagonaly(false);
             adj.add(testNode);
         }
     }
 
 }
