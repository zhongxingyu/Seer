 package org.kornicameister.sise.puzzle.builder;
 
 import com.rits.cloning.Cloner;
 import org.kornicameister.sise.core.graph.GraphNode;
 import org.kornicameister.sise.puzzle.core.PuzzleNodeBuilder;
 import org.kornicameister.sise.puzzle.node.PuzzleNode;
 
 import java.util.*;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class PuzzleNeighborsBuilder implements PuzzleNodeBuilder {
     private static final String RANDOM_ORDER = "R";
     private static final Character[] MOVES = {'L', 'P', 'G', 'D'};
     private static Integer ID = 0;
     private List<GraphNode> generatedNodes = new ArrayList<>();
 
    public PuzzleNeighborsBuilder(List<GraphNode> originalNodes) {
        this.generatedNodes.addAll(originalNodes);
    }

     @Override
     public Map<Character, GraphNode> getPossibleNeighbours(GraphNode node) {
         Map<Character, GraphNode> puzzleNodeList = new HashMap<>();
         PuzzleNode puzzleNode = (PuzzleNode) node;
         Integer[][] puzzle = puzzleNode.getPuzzle();
         Point fromCords = this.findBlankField(puzzleNode);
         List<Character> moves = new ArrayList<>();
         final String order = puzzleNode.getOrder();
 
         if (order.equals(RANDOM_ORDER)) {
             Character[] movesRandom = MOVES.clone();
             Collections.shuffle(Arrays.asList(movesRandom));
             moves.addAll(Arrays.asList(MOVES).subList(0, 4));
         } else {
             moves.add(order.charAt(0));
             moves.add(order.charAt(1));
             moves.add(order.charAt(2));
             moves.add(order.charAt(3));
         }
 
         for (Character direction : moves) {
             switch (direction) {
                 case 'L': {    //move to the left
                     final Point toCords = fromCords.newPointByOffset(0, -1);
                     if (toCords.getY() >= 0) {
 
                         Integer[][] puzzleCopy = this.swapByIndex(
                                 new Cloner().deepClone(puzzleNode).getPuzzle(),
                                 fromCords,
                                 toCords
                         );
 
                         final PuzzleNode newNode = new PuzzleNode(String.format("PuzzleNode-gen-%d", ID++), order, puzzleCopy);
 
                         if (this.generatedNodes.contains(newNode)) {
                             puzzleNodeList.put(direction, this.generatedNodes.get(this.generatedNodes.indexOf(newNode)));
                         } else {
                             this.generatedNodes.add(newNode);
                             puzzleNodeList.put(direction, newNode);
                         }
 
                     }
                 }
                 break;
                 case 'P': {   //move to the right
                     final Point toCords = fromCords.newPointByOffset(0, 1);
                     if (toCords.getY() < puzzle[0].length) {
 
                         Integer[][] puzzleCopy = this.swapByIndex(
                                 new Cloner().deepClone(puzzleNode).getPuzzle(),
                                 fromCords,
                                 toCords
                         );
 
                         final PuzzleNode newNode = new PuzzleNode(String.format("PuzzleNode-gen-%d", ID++), order, puzzleCopy);
 
                         if (this.generatedNodes.contains(newNode)) {
                             puzzleNodeList.put(direction, this.generatedNodes.get(this.generatedNodes.indexOf(newNode)));
                         } else {
                             this.generatedNodes.add(newNode);
                             puzzleNodeList.put(direction, newNode);
                         }
 
                     }
                 }
                 break;
                 case 'G': {    //move up
                     final Point toCords = fromCords.newPointByOffset(-1, 0);
                     if (toCords.getX() >= 0) {
 
                         Integer[][] puzzleCopy = this.swapByIndex(
                                 new Cloner().deepClone(puzzleNode).getPuzzle(),
                                 fromCords,
                                 toCords
                         );
 
                         final PuzzleNode newNode = new PuzzleNode(String.format("PuzzleNode-gen-%d", ID++), order, puzzleCopy);
 
                         if (this.generatedNodes.contains(newNode)) {
                             puzzleNodeList.put(direction, this.generatedNodes.get(this.generatedNodes.indexOf(newNode)));
                         } else {
                             this.generatedNodes.add(newNode);
                             puzzleNodeList.put(direction, newNode);
                         }
 
                     }
                 }
                 break;
                 case 'D': {  //move down
                     final Point toCords = fromCords.newPointByOffset(1, 0);
                     if (toCords.getX() < puzzle.length) {
 
                         Integer[][] puzzleCopy = this.swapByIndex(
                                 new Cloner().deepClone(puzzleNode).getPuzzle(),
                                 fromCords,
                                 toCords
                         );
 
                         final PuzzleNode newNode = new PuzzleNode(String.format("PuzzleNode-gen-%d", ID++), order, puzzleCopy);
 
                         if (this.generatedNodes.contains(newNode)) {
                             puzzleNodeList.put(direction, this.generatedNodes.get(this.generatedNodes.indexOf(newNode)));
                         } else {
                             this.generatedNodes.add(newNode);
                             puzzleNodeList.put(direction, newNode);
                         }
 
                     }
                 }
                 break;
             }
         }
 
         return puzzleNodeList;
     }
 
     public int getGeneratedNodes() {
         return generatedNodes.size();
     }
 
     private Integer[][] swapByIndex(Integer[][] target, Point from, Point to) {
         int fX = from.getX(),
                 fY = from.getY(),
                 tX = to.getX(),
                 tY = to.getY();
 
         Integer temp = target[fX][fY];
         target[fX][fY] = target[tX][tY];
         target[tX][tY] = temp;
 
         return target;
     }
 
     private Point findBlankField(PuzzleNode node) {
         Integer[][] puzzle = node.getPuzzle();
         for (int i = 0; i < puzzle.length; i++) {
             for (int j = 0; j < puzzle[0].length; j++) {
                 if (puzzle[i][j].equals(0)) {
                     return new Point(i, j);
                 }
             }
         }
         return null;
     }
 
     private class Point {
         private int x;
         private int y;
 
         public Point(int x, int y) {
             this.x = x;
             this.y = y;
         }
 
         public Point newPointByOffset(int x, int y) {
             return new Point(this.x + x, this.y + y);
         }
 
         public int getX() {
             return x;
         }
 
         public int getY() {
             return y;
         }
 
         @Override
         public String toString() {
             final StringBuilder sb = new StringBuilder();
             sb.append("Point");
             sb.append("{x=").append(x);
             sb.append(", y=").append(y);
             sb.append('}');
             return sb.toString();
         }
     }
 }
