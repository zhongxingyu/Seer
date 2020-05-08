 /*
  * Created on Jan 23, 2005
  */
 package org.spacebar.escape.solver;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 import javax.swing.JOptionPane;
 
 import org.spacebar.escape.common.BitInputStream;
 import org.spacebar.escape.common.Entity;
 import org.spacebar.escape.common.EquateableLevel;
 import org.spacebar.escape.common.LevelManip;
 import org.spacebar.escape.common.Level.HeuristicData;
 
 /**
  * @author adam
  */
 public class AStarSearch implements Runnable {
     PriorityQueue<AStarNode> open = new PriorityQueue<AStarNode>(11,
             new AStarPQComparator());
 
     Map<SoftLevel, AStarNode> openMap = new HashMap<SoftLevel, AStarNode>();
 
     // Set<Long> closed = new HashSet<Long>();
     Set<SoftLevel> closed = new HashSet<SoftLevel>();
 
     // Set<Level> closed = new HashSet<Level>();
 
     int moveLimit = Integer.MAX_VALUE;
 
     final private AStarNode start;
 
     public void setMoveLimit(int moves) {
         moveLimit = moves;
     }
 
     final int heuristicMap[][];
 
     int greatestG;
 
     private int died;
 
     public AStarSearch(EquateableLevel l) {
         // construct initial node
         System.out.println("Initial Level");
         l.print(System.out);
 
         LevelManip lm = new LevelManip(l);
         lm.optimize();
         EquateableLevel l2 = new EquateableLevel(lm);
         if (!l.equals(l2)) {
             l = l2;
             System.out.println("Shrunk Level");
             l.print(System.out);
         }
 
         heuristicMap = l.computeHeuristicMap().map;
         HeuristicData.printHmap(heuristicMap);
         start = new AStarNode(null, new SoftLevel(l), 0);
     }
 
     private void updateOpen(AStarNode a) {
         SoftLevel level = a.level;
         assert sanityCheck();
         assert a.g + h(level) <= a.f : a.f + " not <= " + a.g + " + "
                 + h(level);
         // if (openMap.containsKey(level)) {
         // open.removeAll(Collections.singleton(a));
         // // open.remove(a);
         // assert !open.contains(a);
         // openMap.remove(level);
         // // System.out.println("old node: " + node);
         // // System.out.println("new node: " + a);
         // }
         // assert sanityCheck();
 
 //        level.flush();
         open.add(a);
         openMap.put(level, a);
         assert open.size() >= openMap.size();
         // assert openMap.containsKey(new Level(level));
         assert sanityCheck();
     }
 
     private AStarNode getFromOpen(AStarNode a) {
         SoftLevel level = a.level;
         assert a.g + h(level) <= a.f : a.f + " not <= " + a.g + " + "
                 + h(level);
         assert sanityCheck();
         AStarNode node = openMap.get(level);
         assert sanityCheck();
         return node;
     }
 
     private AStarNode removeFromOpen() {
         assert sanityCheck();
         // System.out.println("AStarSearch.removeFromOpen()");
         assert open.size() >= openMap.size();
         AStarNode a;
         do {
             a = open.remove();
         } while (!openMap.containsKey(a.level));
 
         SoftLevel aLevel = a.level;
         assert openMap.containsValue(a);
         AStarNode result = openMap.remove(aLevel);
         SoftLevel resLevel = result.level;
         assert !openMap.containsValue(a);
         assert aLevel.equals(resLevel);
         assert open.size() >= openMap.size();
         assert sanityCheck();
         assert a.g + h(aLevel) <= a.f : a.f + " not <= " + a.g + " + "
                 + h(aLevel);
         return a;
     }
 
     private boolean sanityCheck() {
         // List<AStarNode> extraOpenItems = new ArrayList<AStarNode>();
         List<AStarNode> extraOpenMapItems = new ArrayList<AStarNode>();
 
         boolean bad = false;
 
         AStarNode head = open.peek();
         int headF = head == null ? 0 : head.f;
         // System.out.println("head node f: " + headF);
 
         for (AStarNode node : open) {
             // if (!openMap.containsValue(node)) {
             // extraOpenItems.add(node);
             // }
             if (headF > node.f) {
                 System.out.println("head node has non-best f: " + headF
                         + " (better f: " + node.f + ")");
                 bad = true;
             }
         }
 
         for (AStarNode node : openMap.values()) {
             if (!open.contains(node)) {
                 extraOpenMapItems.add(node);
             }
         }
 
         // if (!extraOpenItems.isEmpty()) {
         // System.out.println("extra items in open: " + extraOpenItems);
         // bad = true;
         // }
         if (!extraOpenMapItems.isEmpty()) {
             System.out.println("extra items in openMap: " + extraOpenMapItems);
             bad = true;
         }
 
         return !bad;
     }
 
     int h(SoftLevel l) {
         // default heuristic -- override
         int m = pathHeuristic(l);
 
         return m;
     }
 
     private int pathHeuristic(SoftLevel l) {
         return heuristicMap[l.getPlayerX()][l.getPlayerY()];
     }
 
     private final AStarNode tmpChildren[] = new AStarNode[4];
 
     private int tmpChildCount;
 
     void generateChildren(AStarNode node) {
         tmpChildCount = 0;
         SoftLevel level = node.level;
         // default children generation -- override
 
         boolean isDead;
         if (!level.isDead()) {
             isDead = false;
         } else {
             died++;
             isDead = true;
         }
         if (node.g == 0 || (!isDead && !level.isWon()) && node.g < moveLimit) {
             for (byte i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
                 testChild(node, i);
             }
             // System.out.println("----------");
         }
         // System.out.println("number of children: " + l.size());
     }
 
     protected void testChild(AStarNode node, byte i) {
         SoftLevel lev = node.level.move(i);
         if (lev != null) {
             // System.out.println(" child");
             // if (!closed.contains(lev.quickHash())) {
             if (!closed.contains(lev)) {
                 // System.out.println("***adding " + lev);
                 tmpChildren[tmpChildCount++] = new AStarNode(node, lev, 1);
                 // lev.print(System.out);
             }
         }
     }
 
     class AStarNode {
         public final SoftLevel level;
 
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
 
         @Override
         public String toString() {
             return "(f: " + f + ", g: " + g + ", h: " + h(level) + ", "
                     + level.toString() + ")";
         }
 
         AStarNode(AStarNode parent, SoftLevel l, int cost) {
             level = l;
 
             final int parentF;
             if (parent == null) {
                 this.g = cost;
                 parentF = 0;
             } else {
                 this.g = parent.g + cost;
                 parentF = parent.f;
             }
 
             f = g + h(l);
 
             // TODO: lev1464.esx
             if (f < parentF) {
                 // so important, that we never want to disable
                 printSolution(constructSolution(this));
                 throw new AssertionError("pathmax active! " + parent + " "
                         + this);
             }
             if (g > greatestG) {
                 System.out.println("greatest g: " + greatestG);
                 greatestG = g;
             }
         }
 
         boolean isGoal() {
             boolean result = !level.isDead() && level.isWon() && g > 0;
             if (result) {
                 System.out.println("GOAL");
             }
             return result;
         }
 
     }
 
     static class AStarPQComparator implements Comparator<AStarNode>, Serializable {
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
 
     private List<Byte> solution;
 
     class TimingPrinter implements Runnable {
         volatile boolean done;
 
         public void run() {
             int prevOpen = 0;
             int prevClosed = 0;
 
             while (!done) {
                 int os = openMap.size();
                 int cs = closed.size();
                 System.out.print("Open nodes: " + os + ", closed nodes: " + cs
                         + "   (deltas: " + (os - prevOpen) + ", "
                         + (cs - prevClosed) + ")");
                 if (SoftLevel.regenCount > 0) {
                     System.out.print("  regenerated levels: "
                             + SoftLevel.regenCount);
                     SoftLevel.regenCount = 0;
                 }
                 System.out.println();
                 // System.out.println("best h: " + bestH);
                 prevOpen = os;
                 prevClosed = cs;
 
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public void run() {
         TimingPrinter tp = new TimingPrinter();
         new Thread(tp).start();
         
         try {
             while (!open.isEmpty()) {
                 // System.out.println(" ***** closed: " + closed);
                 AStarNode a = removeFromOpen();
                 // System.out.println("getting node from open list, f: " + a.f);
                 // System.out.println(a);
                 // a.level.print(System.out);
                 // System.out.println();
                 if (a.isGoal()) {
                     // GOAL
                     solution = constructSolution(a);
                     return;
                 } else {
                     // System.out.println("adding to closed list");
                     SoftLevel level = a.level;
                     // closed.add(level.quickHash());
                     // System.out.println(" ***** closed: " + closed);
                     closed.add(level);
                     // System.out.println(" ***** closed: " + closed);
                     generateChildren(a);
                     // System.out.println(" ***** closed: " + closed);
 //                    level.flush();
 
                     // Collections.shuffle(children);
                     for (int i = 0; i < tmpChildCount; i++) {
                         AStarNode node = tmpChildren[i];
                         AStarNode oldNode = getFromOpen(node);
                         if (oldNode == null) {
                             updateOpen(node);
                         } else if (node.g < oldNode.g) {
                             assert node.equals(oldNode);
                             updateOpen(node);
                         }
                     }
                 }
             }
             System.out.println("closed nodes: " + closed.size());
             solution = null;
         } finally {
             tp.done = true;
         }
     }
 
     public void initialize() {
         assert sanityCheck();
         // init lists
         open.clear();
         openMap.clear();
         closed.clear();
         assert sanityCheck();
 
         // add to open list
         updateOpen(start);
         SoftLevel level = start.level;
         assert start.g + h(level) == start.f : start.f + " != " + start.g
                 + " + " + h(level);
 
         greatestG = 0;
         died = 0;
     }
 
     List<Byte> constructSolution(AStarNode a) {
         List<Byte> moves = new ArrayList<Byte>();
         SoftLevel l = a.level;
         while (l != null) {
             moves.add(l.getDirToHere());
             l = l.parent;
         }
         Collections.reverse(moves);
         moves.remove(0);
 
         return moves;
     }
 
     public boolean solutionFound() {
         return solution != null;
     }
 
     public void printSolution() {
         if (solution == null) {
             System.out.println("No solution found");
             return;
         }
 
         printSolution(solution);
     }
 
     static void printSolution(List<Byte> solution) {
         System.out.println("moves: " + solution.size());
         byte lastMove = Entity.DIR_NONE;
         int moveCount = 0;
         for (Byte move : solution) {
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
 
     int countSpheres(SoftLevel l) {
         // number of spheres
         int count = 0;
         for (int i = 0; i < l.getWidth() * l.getHeight(); i++) {
             if (l.tileAt(i) == EquateableLevel.T_BSPHERE) {
                 count++;
             }
         }
         return count;
     }
 
     public static void main(String[] args) {
        // dummy
        try {
            new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
        }
 
 
         try {
             EquateableLevel l = new EquateableLevel(new BitInputStream(
                     new FileInputStream(args[0])));
 
             int startingMoves = 60;
             if (args.length > 1) {
                 startingMoves = Integer.parseInt(args[1]);
             }
 
             // AStarSearch search2 = new AStarSearch(l) {
             // @Override
             // public int h(SoftLevel l) {
             // return countSpheres(l);
             // }
             //
             // @Override
             // public List<AStarNode> generateChildren(AStarNode node) {
             // List<AStarNode> l = new ArrayList<AStarNode>();
             // SoftLevel level = node.level;
             //
             // if (!level.isDead() && !level.isWon() && node.g < moveLimit) {
             // if (countSpheres(level) == 0) {
             // for (byte i = Entity.FIRST_DIR; i <= Entity.LAST_DIR; i++) {
             // SoftLevel lev = new SoftLevel(level);
             // testChild(node, l, i, lev);
             // }
             // } else {
             // if (level.getPlayerY() == 3) { // sphererow
             // SoftLevel lev = new SoftLevel(level);
             // testChild(node, l, Entity.DIR_UP, lev);
             // lev = new SoftLevel(level);
             // testChild(node, l, Entity.DIR_RIGHT, lev);
             // } else {
             // SoftLevel lev = new SoftLevel(level);
             // testChild(node, l, Entity.DIR_DOWN, lev);
             // lev = new SoftLevel(level);
             // testChild(node, l, Entity.DIR_LEFT, lev);
             // lev = new SoftLevel(level);
             // testChild(node, l, Entity.DIR_RIGHT, lev);
             // }
             // }
             // }
             //
             // return l;
             // }
             // };
 
             AStarSearch search = new AStarSearch(l);
             for (int i = startingMoves; i <= 10000; i *= 2) {
                 System.out.print("trying in " + i + " moves, ");
                 search.initialize();
                 search.setMoveLimit(i);
 
                 search.run();
 
                 if (search.solutionFound()) {
                     System.out.println(l);
                     search.printDeaths();
                     search.printSolution();
                     search.initialize();
                     robot(search.solution);
                     break;
                 }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void printDeaths() {
         System.out.println("Died " + died + " times");
     }
 
     private static void robot(List<Byte> s) {
         int time = 2;
         String options[] = { "Type The Solution", "Exit" };
 
         int result = JOptionPane.showOptionDialog(null,
                 "Do you want to run the solution?", "Solution Found",
                 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                 options, options[0]);
 
 //        System.out.println("result = " + result);
         
         if (result == JOptionPane.YES_OPTION) {
             Robot r = null;
             try {
 //                System.out.println("Initializing robot...");
                 r = new Robot();
             } catch (AWTException e) {
                 e.printStackTrace();
                 return;
             }
 
             r.setAutoDelay(40);
 
             System.out.print("Running in " + time + " seconds...");
             System.out.flush();
             try {
                 Thread.sleep(time * 1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             System.out.println();
 
             for (byte dir : s) {
                 System.out.print(Entity.directionToString(dir) + " ");
                 System.out.flush();
 
                 int k = directionToKey(dir);
                 r.keyPress(k);
                 r.keyRelease(k);
             }
             System.out.println();
         }
     }
 
     private static int directionToKey(int dir) {
         switch (dir) {
         case Entity.DIR_UP:
             return KeyEvent.VK_UP;
         case Entity.DIR_DOWN:
             return KeyEvent.VK_DOWN;
         case Entity.DIR_LEFT:
             return KeyEvent.VK_LEFT;
         case Entity.DIR_RIGHT:
             return KeyEvent.VK_RIGHT;
 
         default:
             assert false;
             return 0;
         }
     }
 }
