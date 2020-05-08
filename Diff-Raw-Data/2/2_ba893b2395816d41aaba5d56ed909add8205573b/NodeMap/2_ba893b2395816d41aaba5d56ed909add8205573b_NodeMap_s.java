 package vooga.rts.map;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import vooga.rts.gamedesign.sprite.gamesprites.GameSprite;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.util.Location;
 import vooga.rts.util.Location3D;
 
 
 /**
  * This class stores all the nodes that will be used for pathfinding.
  * 
  * @author Challen Herzberg-Brovold
  * @author Jonathan Schmidt
  * 
  */
 public class NodeMap implements Observer {
 
     private int myWidth;
     private int myHeight;
     private Node[][] myMap;
 
     private Map<GameSprite, Node> myLookupMap;
 
     public NodeMap (int width, int height) {
         myMap = new Node[width][height];
         myWidth = width;
         myHeight = height;
         myLookupMap = new HashMap<GameSprite, Node>();
     }
 
     /**
      * Returns all the neighboring nodes of a specified node.
      * 
      * @param current the node for which we want the neighbors
      * @return a list of neighbors of the node
      */
     public List<Node> getNeighbors (Node current) {
         List<Node> neighbors = new ArrayList<Node>();
         int x = current.getX();
         int y = current.getY();
         for (int i = -1; i < 2; i += 2) {
             neighbors.add(get(x + i, y));
             neighbors.add(get(x, y + i));
         }
         return neighbors;
     }
 
     /**
      * Returns the node at the specified coordinates
      * 
      * @param x-coordinate
      * @param y-coordinate
      * @return node at the coordinates
      */
     public Node get (int x, int y) {
        if (x > 0 && y > 0 && x < myMap.length && y < myMap[0].length) {
             return myMap[x][y];
         }
         return null;
     }
 
     /**
      * 
      * @return width of the map in nodes
      */
     public int getWidth () {
         return myWidth;
     }
 
     /**
      * 
      * @return height of the map in nodes.
      */
     public int getHeight () {
         return myHeight;
     }
 
     public void put (Node node, int x, int y) {
         myMap[x][y] = node;
     }
 
     public void paint (Graphics2D pen) {
         // Paint everything in the game!
     }
 
     /**
      * Adds a Sprite to a Node.
      * 
      * @param sprite The sprite to be added to the node.
      * @param node The node to add the sprite to.
      */
     private void addToNode (GameSprite sprite, Node node) {
         myLookupMap.put(sprite, node);
         node.addSprite(sprite);
     }
 
     /**
      * Removes a Sprite from its current Node.
      * 
      * @param sprite The sprite to be added to the node.
      * @param node The node to add the sprite to.
      */
     private void removeFromNode (GameSprite sprite) {
         Node node = myLookupMap.get(sprite);
         node.removeSprite(sprite);
     }
 
     /**
      * Finds a node that a location should correspond to.
      * 
      * @param world The world location.
      * @return The node that the location is inside.
      */
     private Node findContainingNode (Location3D world) {
         // This should be the node for this location.
         Node potential = getNode(world);
         if (potential != null && potential.contains(world)) {
             return potential;
         }
         return null;
     }
 
     /**
      * Returns a list of nodes less than a certain distance of the center.
      * 
      * @param center The center
      * @param radius The distance to search from the center
      * @return
      */
     public List<Node> getNodesinArea (Location3D center, double radius) {
         // generate the square
         int numTiles = (int) Math.ceil(radius / Node.NODE_SIZE);
         int nodeX = (int) Math.floor(center.getX() / Node.NODE_SIZE);
         int nodeY = (int) Math.floor(center.getY() / Node.NODE_SIZE);
 
         List<Node> nodeList = new ArrayList<Node>();
         for (int x = nodeX - numTiles; x < nodeX + numTiles; x++) {
             for (int y = nodeY - numTiles; x < nodeY + numTiles; y++) {
                 Node cur = get(x, y);
                 if (cur != null) {
                     if (cur.contains(new Location3D(x * Node.NODE_SIZE, y * Node.NODE_SIZE, center
                             .getZ()))) {
                         nodeList.add(cur);
                     }
                 }
             }
         }
         return nodeList;
     }
 
     /**
      * Returns a node that corresponds to a provided world location.
      * 
      * @param location The location of
      * @return
      */
     public Node getNode (Location3D location) {
         int x = (int) Math.floor(location.getX() / Node.NODE_SIZE);
         int y = (int) Math.floor(location.getY() / Node.NODE_SIZE);
         return get(x, y);
     }
 
     @Override
     public void update (Observable arg0, Object arg1) {
         // Map only worries about Game Sprite observables
         if (!(arg0 instanceof GameSprite)) {
             return;
         }
         GameSprite item = (GameSprite) arg0;
         Node cur = myLookupMap.get(item);
         // If the map doesn't know about it yet
         if (cur == null) {
             Node newNode = findContainingNode(item.getWorldLocation());
             if (newNode != null) {
                 addToNode(item, newNode);
                 cur = newNode;
             }
         }
 
         // if it's updating with its new location
         if (arg1 instanceof Location3D) {
             System.out.println("Updating poition");
             // hasn't moved outside of the current node
             if (cur.contains(item.getWorldLocation())) {
                 return;
             }
             else {
                 Node newNode = findContainingNode(item.getWorldLocation());
                 if (newNode != null) {
                     removeFromNode(item);
                     addToNode(item, newNode);
                 }
             }
 
         }
         if (item instanceof InteractiveEntity) {
             InteractiveEntity ie = (InteractiveEntity) item;
             if (ie.isDead()) {                
                 if (cur != null) {
                     removeFromNode(item);
                 }
             }
         }
     }
 }
