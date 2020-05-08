 package ogo.spec.game.model;
 
 import java.util.PriorityQueue;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.HashMap;
 
 public class Map
 {
     protected Tile[][] tiles;
 
     // distance between two tiles
     public final static int DIST = 1000;
 
     /**
      * A node in the path.
      */
     private class Node implements Comparable<Node>
     {
         Tile tile;
         Node prev;
 
         // g and f values
         int g = Integer.MAX_VALUE, f = Integer.MAX_VALUE;
 
         Node(Tile tile)
         {
             this.tile = tile;
         }
 
         /**
          * Get the h(x) value
          */
         int h(Tile x)
         {
             return getDistance(tile, x);
         }
 
         /**
          * Compare.
          */
         public int compareTo(Node t)
         {
             return f - t.f;
         }
     }
 
     /**
      * Calculate a path from a source to a target.
      *
      * This method uses A*
      */
     public List<Tile> calculatePath(Tile source, Tile target, Set<TileType> allowedTypes)
     {
         AyStar(source, target, allowedTypes);
 
         return new LinkedList<Tile>();
     }
 
     /**
      * Get the distance between two tiles.
      */
     public int getDistance(Tile a, Tile b)
     {
         int dx = Math.abs(a.x - b.x);
         int dy = Math.abs(a.y - b.y);
         return (int) (Math.hypot(dx, dy) * 1000);
     }
 
     /**
      * Get the neighbours of a tile.
      */
     public List<Tile> getNeighbours(Tile tile)
     {
        LinkedList<Tile> neighbours = new LinkedList<Tile>();
        for (int x = tile.x - 1; x >= 0 && x < tiles.length; x++) {
            for (int y = tile.y - 1; y >= 0 && y < tiles[0].length; y++) {
                if (x == tile.x && y == tile.y) {
                    continue;
                }
                neighbours.add(tiles[x][y]);
            }
        }
        return neighbours;
     }
 
     /**
      * A* algorithm.
      */
     private void AyStar(Tile source, Tile target, Set<TileType> allowedTypes)
     {
         Set<Tile> done = new HashSet<Tile>();
         HashMap<Tile,Node> open = new HashMap<Tile,Node>();
         PriorityQueue<Node> Q = new PriorityQueue<Node>();
 
         // initialize the source
         Node start = new Node(source);
         start.g = 0;
         start.f = start.g + start.h(target);
 
         Q.add(start);
         open.put(source, start);
 
         Node current;
         while ((current = Q.poll()) != null) {
             if (current.equals(target)) {
                 return;
             }
 
             open.remove(current.tile);
             done.add(current.tile);
 
             // loop through all neighbours, and add them
             for (Tile neighbour : getNeighbours(current.tile)) {
                 if (done.contains(neighbour) || !allowedTypes.contains(neighbour.getType())) {
                     continue;
                 }
 
                 Node neighbourNode;
 
                 // create the node if required
                 if (!open.containsKey(neighbour)) {
                     neighbourNode = new Node(neighbour);
                 } else {
                     neighbourNode = open.get(neighbour);
                 }
 
                 int distanceThroughNeighbour = current.g + getDistance(current.tile, neighbour);
 
                 // get the shortest distance
                 if (distanceThroughNeighbour <= neighbourNode.g) {
                     neighbourNode.g = distanceThroughNeighbour;
                     neighbourNode.f = distanceThroughNeighbour + current.h(neighbour);
                     open.put(neighbour, neighbourNode);
                 }
             }
         }
     }
 }
