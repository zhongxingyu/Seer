 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tira.harjoitustyo.solvers;
 
 import tira.harjoitustyo.util.MapTools;
 import tira.harjoitustyo.util.Point;
 import tira.harjoitustyo.util.PriQueue;
 
 /**
  *
  * @author tuure
  */
 public class AstarSolver implements Solver {
 
     private int nodesChecked;
     private int solutionLength;
     private boolean solved;
     
     @Override
     public char[][] solve(char[][] origMap) {
        //initial values
        nodesChecked = 0;
        solutionLength = 0;
        solved = false;
        
         char[][] map = MapTools.copy(origMap);
         if (map == null) return null;
         
         //an array for the visited nodes
         boolean[][] visited = new boolean[map.length][map[0].length];
         //a queue for the open nodes
         PriQueue<Point> open = new PriQueue();
         
         //search the starting point
         Integer startX = null;
         Integer startY = null;
         int x = 0;
         int y = 0;
         
         for (char[] row:map){
             for (char c:row){
                 if (c=='s'){
                     startX = x;
                     startY = y;
                     break;
                 }
                 x++;
             }
             if (startX != null) break;
             x=0;
             y++;
         }
         
         if (startX == null){
             startX = 0;
             startY = 0;
         }
         
         //add the first node to the open nodes
         open.add(new Point(startX, startY));
         
         //go through the open nodes
         while (!open.isEmpty()){
             Point p = open.poll();
             
             //check if we've reached the finish
             if (map[p.x][p.y] == 'f'){
                 //mark the map sovled
                 solved = true;
                 
                 //draw the route on the map
                 while (p.parent != null){
                     p = p.parent;
                     if (map[p.x][p.y] == ' '){
                         map[p.x][p.y] = '+';
                         solutionLength++;
                     }
                 }
                 
                 return map;
             }
             
             addIfValid(new Point(p.x+1, p.y, p, p.distance+1), open, visited, map);
             addIfValid(new Point(p.x-1, p.y, p, p.distance+100), open, visited, map);
             addIfValid(new Point(p.x, p.y+1, p, p.distance+1), open, visited, map);
             addIfValid(new Point(p.x, p.y-1, p, p.distance+100), open, visited, map);
         }
         
         return null;
     }
 
     @Override
     public String getName() {
         return "A*";
     }
     
     private void addIfValid(Point p, PriQueue<Point> open, boolean[][] visited, char[][] map){
         //check that the suggested point is within the map
         if (p.x >= 0 && p.y >= 0 && p.x < map.length && p.y < map[0].length){
             //check that the suggested point is not visited
             if (!visited[p.x][p.y]){
                 //check that the suggested point is not a wall
                 if (map[p.x][p.y] != 'X'){
                     //add the new point to the open nodes
                     open.add(p);
                     //mark the point visited
                     visited[p.x][p.y] = true;
                     //add the checked count
                     nodesChecked++;
                 }
             }
         }
     }
 
     @Override
     public int getNodesChecked() {
         return nodesChecked;
     }
 
     @Override
     public int getSolutionLength() {
         return solutionLength;
     }
 
     @Override
     public boolean isSolved() {
         return solved;
     }
     
 }
