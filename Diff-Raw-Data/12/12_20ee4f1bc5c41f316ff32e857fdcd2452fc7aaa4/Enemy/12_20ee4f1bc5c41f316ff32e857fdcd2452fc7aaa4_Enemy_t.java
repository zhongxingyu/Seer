 package objects;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * The enemy class is where most of the AI is handled.
  * The enemy will recalculate their path every tick and take the appropriate path.
  * The AI will assume certain risks, but it will mostly dodge danger.
  */
 public class Enemy extends Entity {
     private HashSet<Point> ClosedSet;
     private HashMap<Point, Bomb.BombType> DangerPath;
 
     public Enemy(int x, int y) {
         super(Thing.enemyImg);
         this.x = x;
         this.y = y;
     }
 
     /**
      * Gets the heuristic score of the current point to the goal
      *
      * @param current
      * @param goal
      * @return
      */
     private int getHScore(Point current, Point goal) {
         return Math.abs(goal.x - current.x) + Math.abs(goal.y - current.y);
     }
 
     /**
      * Finds the lowest fscore from the current openset
      *
      * @param openSet
      * @param fscore
      * @return
      */
     private Point findLowestFScore(ArrayList<Point> openSet, int[][] fscore) {
         Point result = new Point();
         int lowestScore = Integer.MAX_VALUE;
         for (Point p : openSet) {
             if (fscore[p.y][p.x] < lowestScore) {
                 lowestScore = fscore[p.y][p.x];
                 result = p;
             }
         }
         return result;
     }
 
     /**
      * Reconstructs the optimal path from the end position to the start position, recursively.
      * Returns the first movement only.
      *
      * @param cameFrom
      * @param currentPos
      * @return
      */
     private Point reconstructPath(Point[][] cameFrom, Point currentPos) {
         Point previous = cameFrom[currentPos.y][currentPos.x];
         if (previous != null && cameFrom[previous.y][previous.x] != null) {
             return reconstructPath(cameFrom, previous);
         } else {
             return currentPos;
         }
     }
 
     /**
      * Finds the direction that the enemy should move after calculating the optimal path.
      * @param start
      * @param step
      * @return
      */
     private int findDirection(Point start, Point step) {
         if ((step.y - 1) == start.y) return 4;
         if ((step.y + 1) == start.y) return 2;
         if ((step.x - 1) == start.x) return 3;
         if ((step.x + 1) == start.x) return 1;
         return 0;
     }
 
     /**
      * Initializes the closed set based on where the enemy is and whether they have a bomb or not.
      * @param map
      * @param hasBomb
      * @param inDangerPath
      */
     private void initializeValues(int[][] map, boolean hasBomb, boolean inDangerPath) {
         for (int y = 0; y < map.length; y++) {
             for (int x = 0; x < map[0].length; x++) {
                 if ((!hasBomb || inDangerPath) && map[y][x] > 0)
                     ClosedSet.add(new Point(x, y));
             }
         }
         if (!inDangerPath) {
             Iterator<Map.Entry<Point, Bomb.BombType>> iter = DangerPath.entrySet().iterator();
 
             while (iter.hasNext()) {
                 Map.Entry<Point, Bomb.BombType> e = iter.next();
                 if (e.getValue() == Bomb.BombType.Bomb1)
                     continue;
                 ClosedSet.add(e.getKey());
                 iter.remove();
             }
         }
     }
 
     /**
      * Adds a series of points that should be avoided to a HashMap
      * @param points
      * @param bt
      */
     private void addDangerPath(List<Point> points, Bomb.BombType bt) {
         for (Point p : points) {
             DangerPath.put(p, bt);
         }
     }
 
     /**
      * The modified A* search algorithm.
      * Calculates the optimal path to go with consideration to risks and returns a direction.
      * @param pX
      * @param pY
      * @param bombs
      * @param map
      * @return
      */
     public int calcPath(int pX, int pY, List<Bomb> bombs, int[][] map) {
         ClosedSet = new HashSet<Point>();
         DangerPath = new HashMap<Point, Bomb.BombType>();
         ArrayList<Point> openSet = new ArrayList<Point>();
         int[][] GScore = new int[map.length][map[0].length];
         int[][] FScore = new int[map.length][map[0].length];
         Point[][] CameFrom = new Point[map.length][map[0].length];
         Point start = new Point(this.x, this.y);
        Point goal = new Point(pX, pY);
         Point current = start;
         boolean inDangerPath;
 
         for (Bomb b : bombs) {
             addDangerPath(b.blowUp(map), b.bt);
         }
 
         inDangerPath = DangerPath.containsKey(new Point(x, y));
 
         initializeValues(map, checkBombs(), inDangerPath);
 
         openSet.add(start);
         int currentGScore;
         FScore[start.y][start.x] = getHScore(start, goal);
 
         while (!openSet.isEmpty()) {
             current = findLowestFScore(openSet, FScore);
 
             if (current.equals(goal)) {
                 return findDirection(start, reconstructPath(CameFrom, current));
             }
             openSet.remove(openSet.indexOf(current));       // Removes the current point
             ClosedSet.add(new Point(current));
 
             Point[] neighbours = {new Point(current.x - 1, current.y), new Point(current.x, current.y - 1)
                     , new Point(current.x + 1, current.y), new Point(current.x, current.y + 1)};
             for (Point neighbour : neighbours) {
                 if ((neighbour.x >= 0 && neighbour.x < map[0].length)
                         && (neighbour.y >= 0 && neighbour.y < map.length)) {
                     // Set distance between to +7 if there's a wall that can be blown up,
                     // 50 if it's an explosion path, else 1.
                     int distance;
                     if (DangerPath.containsKey(neighbour)) {
                         distance = 33;
                     } else {
                         distance = map[neighbour.y][neighbour.x] > 0 ? 7 : inDangerPath ? 0 : 1;
                     }
                     currentGScore = GScore[current.y][current.x] + distance;
 
                     if (ClosedSet.contains(neighbour) && currentGScore >= GScore[neighbour.y][neighbour.x]) {
                         continue;
                     }
                     if (!openSet.contains(neighbour) || currentGScore < GScore[neighbour.y][neighbour.x]) {
                         CameFrom[neighbour.y][neighbour.x] = current;
                         GScore[neighbour.y][neighbour.x] = currentGScore;
                         FScore[neighbour.y][neighbour.x] = getHScore(neighbour, goal) + currentGScore;
                         if (!openSet.contains(neighbour)) {
                             openSet.add(new Point(neighbour));
                         }
                     }
                 }
             }
         }
         return findDirection(start, reconstructPath(CameFrom, current));
     }
 
     /**
      * Moves the enemy
      * @param direction
      */
     public void move(int direction) {
         switch (direction) {
             // Left
             case 1:
                 x--;
                 return;
             // Up
             case 2:
                 y--;
                 return;
             // Right
             case 3:
                 x++;
                 return;
             // Down
             case 4:
                 y++;
         }
     }
 
 }
