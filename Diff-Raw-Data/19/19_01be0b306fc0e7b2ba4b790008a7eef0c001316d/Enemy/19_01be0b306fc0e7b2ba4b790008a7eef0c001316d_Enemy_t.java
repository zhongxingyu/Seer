 package objects;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: brad is weird
  * Date: 4/4/13
  * Time: 6:45 PM
  */
 public class Enemy extends Entity {
 
     private ArrayList<Point> ClosedSet;
     public Bomb bomb;
 
     public Enemy(int x, int y) {
         super(Thing.enemyImg);
         this.x = x;
         this.y = y;
     }
 
     private int getHScore(Point current, Point goal){
         return Math.abs(goal.x - current.x) + Math.abs(goal.y - current.y);
     }
 
     private Point findLowestFScore(ArrayList<Point> openSet, int[][] fscore){
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
 
     private Point reconstructPath(Point[][] cameFrom, Point currentPos){
         Point previous = cameFrom[currentPos.y][currentPos.x];
         if(previous != null && cameFrom[previous.y][previous.x] != null){
             return reconstructPath(cameFrom, previous);
         }
         else{
             return currentPos;
         }
     }
 
     private int findDirection(Point start, Point step){
         if((step.y-1) == start.y) return 4;
         if((step.y+1) == start.y) return 2;
         if((step.x-1) == start.x) return 3;
         if((step.x+1) == start.x) return 1;
         else return 0;
     }
 
     private void initializeValues(int[][] map, boolean hasBomb){
         for(int y = 0; y < map.length; y++){
             for(int x= 0; x < map[0].length; x++){
                 if(!hasBomb && map[y][x] > 0)
                     ClosedSet.add(new Point(x, y));
                 if(map[y][x] == 6)
                     addClosedBlock(x, y);
             }
         }
     }
 
     private void addClosedBlock(int x, int y) {
         ClosedSet.add(new Point(x + 1, y));
         ClosedSet.add(new Point(x - 1, y));
         ClosedSet.add(new Point(x, y + 1));
         ClosedSet.add(new Point(x, y - 1));
     }
 
     public int calcPath(int pX, int pY, int[][] map) {
         ClosedSet               = new ArrayList<Point>();
         ArrayList<Point> openSet = new ArrayList<Point>();
         int[][] GScore          = new int[map.length][map[0].length];
         int[][] FScore          = new int[map.length][map[0].length];
         Point[][] CameFrom      = new Point[map.length][map[0].length];
         Point start = new Point(this.x, this.y);
         Point goal = new Point(pX, pY);
         Point current = start;
 
         if(bomb != null) {
             if(bomb.isDestroyed()) {
                 bomb = null;
             }
         }
 
         initializeValues(map, bomb == null);
 
         openSet.add(start);
         int currentGScore = 0;
        FScore[start.y][start.x] = getHScore(start, goal);
 
         while(!openSet.isEmpty())
         {
             current = findLowestFScore(openSet, FScore);
 
             if(current.equals(goal)){
                 return findDirection(start, reconstructPath(CameFrom, current));
             }
             openSet.remove(openSet.indexOf(current));       // Removes the current point
             ClosedSet.add(new Point(current));
 
             Point[] neighbours = {new Point(current.x-1, current.y), new Point(current.x, current.y - 1)
                     , new Point(current.x + 1, current.y), new Point(current.x, current.y+1)};
             for (Point neighbour : neighbours) {
                 if ((neighbour.x >= 0 && neighbour.x < map[0].length)
                         && (neighbour.y >= 0 && neighbour.y < map.length)) {
                     // Set distance between to +7 in case of wall, else 1.
                     int distance = map[neighbour.y][neighbour.x] > 0 ? 7 : 1;
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
 
     @Override
     public Bomb setBomb() {
         bomb = new Bomb(1000, x, y);
         return bomb;
     }
 }
