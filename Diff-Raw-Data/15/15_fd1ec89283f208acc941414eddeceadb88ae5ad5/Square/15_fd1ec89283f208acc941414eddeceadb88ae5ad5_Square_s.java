 package com.bradleybuda;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class Square {
     private enum Direction {
         EAST('e'), WEST('w'), SOUTH('s'), NORTH('n');
         
         private final char symbol;
         private Direction(final char symbol) {
             this.symbol = symbol;
         }
         
         private Offset offset() {
             switch (this) {
             case EAST:
                 return new Offset(0, 1);
             case WEST:
                 return new Offset(0, -1);
             case SOUTH:
                 return new Offset(1, 0);
             case NORTH:
                 return new Offset(-1, 0);
             } 
         }
     }
     
     private static class Offset {
         int row;
         int col;
 
         Offset(int row, int col) {
             this.row = row;
             this.col = col;
         }
     }
     
     private static int ROWS;
     private static int COLS;
     private static int VIEW_RADIUS_2;
     private static Square[][] INDEX;
     public static final Set<Square> OBSERVED = new HashSet<Square>();
     private static final Set<Offset> VISIBLITY_MASK = new HashSet<Offset>();
     
     public static String dumpMap(final Square from, final Square to) {
         final StringBuilder map = new StringBuilder();
         
         for (int row = 0; row < ROWS; ++row) {
             for (int col = 0; col < COLS; ++col) {
                 final Square square = INDEX[row][col];
                 
                 char symbol;
                 if (square == null)
                     symbol = 'X';
                 else if (square == from)
                     symbol = '*';
                 else if (square == to)
                     symbol = '$';
                 else if (square.isObserved())
                     symbol = '_';
                 else
                     symbol = ' ';
                 map.append(symbol);
             }
         }
         
         return map.toString();
     }
     
     public static void createSquares(int rows, int cols, int viewRadius2) {
         Square.VIEW_RADIUS_2 = viewRadius2;
         Square.ROWS = rows;
         Square.COLS = cols;
      
         int viewRadius = (int)Math.ceil(Math.sqrt(VIEW_RADIUS_2));
         for (int rowOffset = -1 * viewRadius; rowOffset <= viewRadius; ++rowOffset)
             for (int colOffset = -1 * viewRadius; rowOffset <= viewRadius; ++rowOffset)
                 if (Square.distance2(0, 0, rowOffset, colOffset) < VIEW_RADIUS_2)
                     VISIBLITY_MASK.add(new Offset(rowOffset, colOffset));
         
         INDEX = new Square[ROWS][COLS];
         for (int row = 0; row < ROWS; ++row)
             for (int col = 0; col < COLS; ++col)
                 INDEX[row][col] = new Square(row, col);
     }
     
     public static Set<Square> all() {
         final Set<Square> all = new HashSet<Square>();
         for (int row = 0; row < ROWS; ++row)
             for (int col = 0; col < COLS; ++col)
                 if (INDEX[row][col] != null)
                     all.add(INDEX[row][col]);
         
         return all;
     }
     
     public static void resetAll() {
         for (int row = 0; row < ROWS; ++row)
             for (int col = 0; col < COLS; ++col)
                 if (INDEX[row][col] != null)
                     INDEX[row][col].reset();        
     }
     
     public static Square at(int row, int col) {
         return INDEX[normalizeRow(row)][normalizeCol(col)];
     }
     
     private static int normalizeRow(int row) {
         return row % ROWS;
     }
     
     private static int normalizeCol(int col) {
         return col % COLS;
     }
     
     private final int row;
     private final int col;
     private boolean observed = false;
     private boolean visited = false;
     private Item item = null;
     private final Map<Goal, Route> goals = new HashMap<Goal, Route>();
     private final Map<Direction, Square> neighbors = new HashMap<Direction, Square>(4);
     private Set<Square> visibleSquares;
     private Ant ant;
     private Ant nextAnt;    
     
     public Square(int row, int col) {
         this.row = row;
         this.col = col;
     }
     
     public Set<Square> neighbors() {
         if (neighbors == null)
             makeNeighbors();
         return new HashSet<Square>(neighbors.values());
     }
     
     public Map<Direction, Square> directionToNeighbors() {
         if (neighbors == null)
             makeNeighbors();
         return neighbors;
     }
     
     private void makeNeighbors() {
         for (final Direction d : Direction.values()) {
             final Offset o = d.offset();
             final int neighborRow = row + o.row;
             final int neighborCol = col + o.col;
             final Square neighbor = Square.at(neighborRow, neighborCol);
             if (neighbor != null)
             neighbors.put(d, neighbor);
         }
     }
     
     public Direction directionTo(final Square neighbor) {
         for (final Map.Entry<Direction, Square> entry : neighbors.entrySet()) {
             if (entry.getValue().equals(neighbor)) {
                 return entry.getKey();
             }
         }
     }
     
     public boolean isVisited() {
         return visited;
     }
     
     public boolean isObserved() {
         return observed;
     }
     
     public void observe() {
         observed = true;
         OBSERVED.add(this);
     }
     
     public Set<Square> visibleSquares() {
         // initial memoized list
         if (visibleSquares == null) {
             visibleSquares = new HashSet<Square>(VISIBLITY_MASK.size());
             for (final Offset o : VISIBLITY_MASK) {
                 final Square s = Square.at(row + o.row, col + o.col);
                 if (s != null) {
                     visibleSquares.add(s);
                 }
             }
         }
         
         // double-check memoized against ones that have disappeared
         final Set<Square> retVal = new HashSet<Square>(visibleSquares.size());
         for (final Square s : visibleSquares) {
             final Square stillThere = Square.at(s.row, s.col);
             if (stillThere != null)
                 retVal.add(stillThere);
         }
         
         return retVal;
     }
     
     public boolean visible(Square square) {
         return distance2(square) < VIEW_RADIUS_2; 
     }
     
     public boolean isFrontier() {
         if (!isObserved()) {
             return false;
         } else {
           for (Square s : neighbors())
               if (!s.isObserved())
                   return true; // observed but has unobserved neighbor
           return false; // observed and all neighbors observed
         }
     }
     
     public boolean hasFood() {
         return (item != null) && (item instanceof Food);
     }
     
     public boolean hasHill() {
         return (item != null) && (item instanceof Hill);
     }
 
     public boolean hasEnemyAnt() {
         return (item != null) && (item instanceof EnemyAnt);
     }
     
     public void destroy() {
         for (final Square neighbor : neighbors())
             neighbor.removeDeadNeighbor(this);
         INDEX[row][col] = null;
         OBSERVED.remove(this);
     }
     
     private void removeDeadNeighbor(Square deadNeighbor) {
         final Direction direction = directionTo(deadNeighbor);
         neighbors.remove(direction);
     }
     
     public static int distance2(int r1, int c1, int r2, int c2) {
         final int rdelt = Math.abs(r1 - r2);
         final int cdelt = Math.abs(c1 - c2);
         final int dr = Math.min(rdelt, ROWS - rdelt);
         final int dc = Math.min(cdelt, COLS - cdelt);
         return (dr ^ 2) + (dc ^ 2);
     }
     
     public int distance2(Square other) {
         return Square.distance2(row, col, other.row, other.col);
     }
     
     @Override
     public String toString() {
         return String.format("[%d, %d]", row, col);
     }
     
     public Set<Square> blacklist() {
         final Set<Square> blacklist = new HashSet<Square>();
         for (final Square neighbor : neighbors()) {
             if (neighbor.nextAnt != null)
                 blacklist.add(neighbor);
             else if (neighbor.hasFood())
                 blacklist.add(neighbor);
             else if (neighbor.hasHill() && neighbor.item.isMine())
                 blacklist.add(neighbor);
         }
     }
     
     public void reset() {
         nextAnt = ant = null;
     }
     
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(row).append(col).hashCode();
     }
     
     @Override
     public boolean equals(Object obj) {
         final Square other = (Square)obj;
         return new EqualsBuilder().append(row, other.row).append(col, other.col).isEquals();
     }
 }
