 /*
  * Created on Jan 23, 2005
  */
 package org.spacebar.escape.j2se;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 import org.spacebar.escape.common.BitInputStream;
 import org.spacebar.escape.common.Entity;
 
 /**
  * @author adam
  */
 public class AStarSearch implements Runnable {
     PriorityQueue<AStarNode> open = new PriorityQueue<AStarNode>(11,
             new AStarPQComparator());
 
     Map<Level, AStarNode> openMap = new HashMap<Level, AStarNode>();
 
    Set<Level> closed = new HashSet<Level>();
 
     final int moveLimit;
 
     public AStarSearch(Level l, int moveLimit) {
         this.moveLimit = moveLimit;
 
         // construct initial node
         AStarNode start = new AStarNode(null, Entity.DIR_NONE, l, 0);
 
         // add to open list
         updateOpen(start);
     }
 
     private void updateOpen(AStarNode a) {
         if (openMap.containsKey(a.level)) {
             open.remove(a);
         }
 
         open.add(a);
         openMap.put(a.level, a);
     }
 
     private AStarNode getFromOpen(AStarNode a) {
         return openMap.get(a.level);
     }
 
     private AStarNode removeFromOpen() {
         AStarNode a = open.remove();
         openMap.remove(a.level);
         return a;
     }
 
     int h(AStarNode node) {
         // default heuristic -- override
         Level l = node.level;
        return manhattan(l);
    }

    private int manhattan(Level l) {
        
         return 0;
     }
 
     List<AStarNode> generateChildren(AStarNode node) {
         // default children generation -- override
         List<AStarNode> l = new ArrayList<AStarNode>();
         Level level = node.level;
 
         if (!level.isDead() && !level.isWon() || node.g == moveLimit) {
             for (int i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
                 Level lev = new Level(level);
                 testChild(node, l, i, lev);
             }
         }
 
         return l;
 
     }
 
     protected void testChild(AStarNode node, List<AStarNode> l, int i, Level lev) {
         if (lev.move(i)) {
             // System.out.println(" child");
            if (!closed.contains(lev)) {
                l.add(new AStarNode(node, i, lev, node.g + 1)); // cost
                // of
                // move is 1
            }
         }
     }
 
     class AStarNode {
         final AStarNode parent;
 
         final int dirToGetHere;
 
         final Level level;
 
         final int f;
 
         final int g;
 
         @Override
         public int hashCode() {
             return level.hashCode();
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj instanceof AStarNode) {
                 AStarNode item = (AStarNode) obj;
                 return level.equals(item.level);
             }
             return false;
         }
 
         AStarNode(AStarNode parent, int dir, Level l, int g) {
             this.parent = parent;
             dirToGetHere = dir;
             this.level = l;
             this.g = g;
             f = g + h(this);
         }
 
         boolean isGoal() {
             return !level.isDead() && level.isWon();
         }
 
     }
 
     private class AStarPQComparator implements Comparator<AStarNode> {
         public int compare(AStarNode o1, AStarNode o2) {
             if (o1.f < o2.f) {
                 return -1;
             } else if (o1.f > o2.f) {
                 return 1;
             } else {
                 return 0;
             }
         }
     }
 
     public void run() {
         long time = System.currentTimeMillis();
         while (!open.isEmpty()) {
             if (System.currentTimeMillis() - time > 1000) {
                 System.out.println("Open nodes: " + open.size()
                         + ", closed nodes: " + closed.size());
                 System.out.println("minimum spheres: " + minSpheres);
                 time = System.currentTimeMillis();
             }
             AStarNode a = removeFromOpen();
             // System.out.println("getting node from open list, f: " + a.f);
             // a.level.print(System.out);
             // System.out.println();
             if (a.isGoal()) {
                 // GOAL
                 constructSolution(a);
                 return;
             } else {
                 // System.out.println("adding to closed list");
                closed.add(a.level);
                 List<AStarNode> children = generateChildren(a);
                 for (AStarNode node : children) {
                     AStarNode oldNode = getFromOpen(node);
                     if (oldNode == null) {
                         updateOpen(node);
                     } else {
                         // better cost from here?
                         if (node.g < oldNode.g) {
                             updateOpen(node);
                         }
                     }
                 }
             }
         }
         System.out.println("No goal found.");
     }
 
     private void constructSolution(AStarNode a) {
         System.out.println("moves: " + a.g);
         List<Integer> moves = new ArrayList<Integer>();
         while (a != null) {
             moves.add(a.dirToGetHere);
             a = a.parent;
         }
         Collections.reverse(moves);
         moves.remove(0);
 
         int lastMove = Entity.DIR_NONE;
         int moveCount = 0;
         for (Integer move : moves) {
             if (move != lastMove) {
                 if (lastMove != Entity.DIR_NONE) {
                     System.out.print(moveCount
                             + Entity.directionToString(lastMove) + " ");
                 }
                 lastMove = move;
                 moveCount = 1;
             } else {
                 moveCount++;
             }
         }
         System.out.println(moveCount + Entity.directionToString(lastMove));
     }
 
    int minSpheres = Integer.MAX_VALUE;
 
    int countSpheres(Level l) {
         // number of spheres
         int count = 0;
         for (int i = 0; i < l.getWidth() * l.getHeight(); i++) {
             if (l.tileAt(i) == Level.T_BSPHERE) {
                 count++;
             }
         }
         if (count < minSpheres) {
             minSpheres = count;
         }
         return count;
     }
 
     public static void main(String[] args) {
         try {
             Level l = new Level(
                     new BitInputStream(new FileInputStream(args[0])));
 
             AStarSearch search = new AStarSearch(l, 200) {
                 @Override
                 public int h(AStarNode node) {
                     return countSpheres(node.level);
                 }
 
                 @Override
                 public List<AStarNode> generateChildren(AStarNode node) {
                     List<AStarNode> l = new ArrayList<AStarNode>();
                     Level level = node.level;
 
                     if (!level.isDead() && !level.isWon()
                             || node.g == moveLimit) {
                         if (countSpheres(level) == 0) {
                             for (int i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
                                 Level lev = new Level(level);
                                 testChild(node, l, i, lev);
                             }
                         } else {
                             if (level.getPlayerY() == 3) { // sphererow
                                 Level lev = new Level(level);
                                 testChild(node, l, Entity.DIR_UP, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_RIGHT, lev);
                             } else {
                                 Level lev = new Level(level);
                                 testChild(node, l, Entity.DIR_DOWN, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_LEFT, lev);
                                 lev = new Level(level);
                                 testChild(node, l, Entity.DIR_RIGHT, lev);
                             }
                         }
                     }
 
                     return l;
                 }
             };
 
             search.run();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
