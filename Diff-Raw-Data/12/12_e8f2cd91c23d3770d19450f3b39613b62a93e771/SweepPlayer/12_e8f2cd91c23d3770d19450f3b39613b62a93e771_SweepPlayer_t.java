 package mosquito.g0;
 
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 
 import org.apache.log4j.Logger;
 import org.jgrapht.alg.CycleDetector;
 
 public class SweepPlayer extends mosquito.sim.Player {
     
     CycleDetector a;
 
     private int numLights;
     
     private Light[] allLights;
     private Point2D.Double lastLight;
     private Logger log = Logger.getLogger(this.getClass()); // for logging
     private LinkedList<sweepSection> boardSections = new LinkedList<sweepSection>();
     
     @Override
     public String getName() {
         return "bears";
     }
     
     private Point2D.Double getMidpoint(Line2D line) {
         return new Point2D.Double((line.getX1() + line.getX2()) / 2, (line.getY1() + line.getY2()) / 2);
     }
     
     private Set<Light> lights;
     private Set<MoveableLight> mlights;
     private Set<Line2D> walls;
     private int phase = 0;
     private AStar astar;
     private int move = 0;
     
     private HashMap<Integer, Integer> numLightsToSpacingMap = new HashMap<Integer, Integer>();
     private HashMap<MoveableLight, Boolean> movementMap = new HashMap<MoveableLight, Boolean>();
     private HashMap<MoveableLight, Integer> lightsToMovesMap = new HashMap<MoveableLight, Integer>();
     /*
      * This is called when a new game starts. It is passed the set
      * of lines that comprise the different walls, as well as the 
      * maximum number of lights you are allowed to use.
      * 
      * The return value is a set of lines that you would like to have drawn on the screen.
      * These lines don't actually affect gameplay, they're just there so you can have some
      * visual clue as to what's happening in the simulation.
      */
     @Override
     public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
         // vertical lines have undefined slope, so we keep their positions
         ArrayList<Line2D.Double> verticalLines = new ArrayList<Line2D.Double>();
         
         // lines are for sketching
         ArrayList<Line2D> lines = new ArrayList<Line2D>();
         ArrayList<Point2D.Double> midpoints = new ArrayList<Point2D.Double>();
         
         this.numLights = numLights;
         this.walls = walls;
         
         ArrayList<Line2D> extendedLines = new ArrayList<Line2D>();
         
         numLightsToSpacingMap.put(10, 9);
         numLightsToSpacingMap.put(9, 7);
         numLightsToSpacingMap.put(8, 8);
         numLightsToSpacingMap.put(7, 10);
         numLightsToSpacingMap.put(6, 13);
         numLightsToSpacingMap.put(5, 18);
         numLightsToSpacingMap.put(4, 22);
         numLightsToSpacingMap.put(3, 33);
         numLightsToSpacingMap.put(1, 22);
         // creating game borders
         Point2D.Double uR = new Point2D.Double(0, 100.0);
         Point2D.Double uL = new Point2D.Double(0, 0);
         Point2D.Double lL = new Point2D.Double(0, 100.0);
         Point2D.Double lR = new Point2D.Double(100.0, 100.0);
         
         Line2D l = new Line2D.Double(uL, lL);
         Line2D r = new Line2D.Double(uR, lR);
         Line2D u = new Line2D.Double(uL, uR);
         Line2D d = new Line2D.Double(lL, lR);
         
         sweepSection boundary = new sweepSection(l, r, u, d);
         ArrayList<Double> sortedVerticalLines = new ArrayList<Double>();
         boardSections.add(boundary);
         
         for (Line2D w : walls) {
             double x1 = w.getX1();
             double x2 = w.getX2();
             double y1 = w.getY1();
             double y2 = w.getY2();
             
             // dealing with vertical lines first
             if (w.getX1() == w.getX2()) {
                 verticalLines.add(new Line2D.Double(new Point2D.Double(x1, 0), new Point2D.Double(x1, 100)));
                 sortedVerticalLines.add(w.getX1());
                 continue;
             }
             
             Point2D.Double mid = this.getMidpoint(w);
             midpoints.add(mid);
         }
         
         // subdivide by vertical sections
         for(int i = 0; i < verticalLines.size(); i++) {
             // need to segment by vertical section...
         }
         
         return lines;
     }
 
 
     /*
      * This is used to determine the initial placement of the lights.
      * It is called after startNewGame.
      * The board tells you where the mosquitoes are: board[x][y] tells you the
      * number of mosquitoes at coordinate (x, y)
      */
     public Set<Light> getLights(int[][] board) {
         // initializing AStar
         FHeuristic fh = new FHeuristic();
         
         int x = 0; 
         int y = numLightsToSpacingMap.get(numLights);
         
         lights = new HashSet<Light>();
         lastLight = new Point2D.Double(10, 10);
         mlights = new HashSet<MoveableLight>();
         for(int a = 0; a<numLights; a++)
         {
             AreaMap cleanMap = new AreaMap(101,101);
             for(int i = 0; i < board.length; i++) {
                 for(int j = 0; j < board[0].length; j++) {
                     for(Line2D wall: walls) {
                         if(wall.ptSegDist(i, j) < 2.0) {
                             cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
                         }
                     }
                 }
             }
             astar = new AStar(cleanMap, fh);
             lastLight = new Point2D.Double(x, y);
             y += numLightsToSpacingMap.get(numLights);
             MoveableLight l = new MoveableLight(lastLight.getX(), lastLight.getY(), true);
 
             lights.add(l);
             
             lightsToMovesMap.put(l, 0);
             mlights.add(l);
             movementMap.put(l, false);
         }
         allLights = lights.toArray(new Light[numLights]);
         return lights;
     }
     
     /*
      * This is called at the beginning of each step (before the mosquitoes have moved)
      * If your Set contains additional lights, an error will occur. 
      * Also, if a light moves more than one space in any direction, an error will occur.
      * The board tells you where the mosquitoes are: board[x][y] tells you the
      * number of mosquitoes at coordinate (x, y)
      */
     public Set<Light> updateLights(int[][] board) {
         for(MoveableLight ml: mlights) {
             if(ml.getX() == 97 && ml.getY() == 50) { //If you've reached the collector, stay there for 15 moves
                 if(ml.numMovesAtCollector >= 15) { //If you've stayed at the collector for 15 moves then time to move on
                     ml.hasFinishedPhaseOne = true;
                     movementMap.put(ml, false); //This is a hashmap that tells us whether each light is currently on an A* path
                     lightsToMovesMap.put(ml, 0); //This hashmap tells us what move number is the current light in, in its A* path
                 }
                 else {
                     ml.numMovesAtCollector++; //If you haven't stayed for 15 moves yet, stay put and increement your movesAtCollector
                     continue;
                 }
             }
             if(!ml.hasFinishedPhaseOne) { //If we are still sweeping from left to right (i.e Phase 1 of the strategy)
                 if(ml.getX() == 97 && !movementMap.get(ml)) { //If we've reached the right side of the board after sweeping from left to right AND we aren't on an A* path as it is
                     log.error("REACHED THE OTHER SIDE !!!!!!!!");
                     AreaMap cleanMap = new AreaMap(101,101);
                     for(int i = 0; i <= board.length; i++) {
                         for(int j = 0; j <= board[0].length; j++) {
                             for(Line2D wall: walls) {
                                 if(wall.ptSegDist(i, j) < 2.0) { //Create our A* graph by marking something as an obstacle if it's within 2 units of distance
                                     cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
                                 }
                             }
                         }
                     }
                     FHeuristic fh = new FHeuristic();
                     astar = new AStar(cleanMap, fh);
                     astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), 97, 50); //Compute the A* path from here to the collector
                     ml.currDestinationX = 97;
                     ml.currDestinationY = 50;
                     ml.shortestPath = astar.shortestPath;
                     movementMap.put(ml, true);
                     log.error("Current x: "+ml.getX());
                     log.error("Current y: "+ml.getY());
                     if(ml.shortestPath != null) {
                         log.error("Moving to x = "+ml.shortestPath.getX(0));
                         log.error("Moving to y = "+ml.shortestPath.getY(0));
                         ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0)); //Move once in the A* path to initiate the A* movement sequence
                         lightsToMovesMap.put(ml, 1);
                     }
                     // THIS IS A HACK!!!!! 
                     // ANYONE WHO READS THIS MUST KNOW THAT IF YOU DO SUCH THINGS IN REAL WORLD PROJECTS 
                     // KITTENS WILL DIE SPONTANEOUSLY
                     else {
                         ml.turnOff();
                     }
                     continue;
                 }
                 if(ml.hasStoppedAtCorner) { //Code to ensure that we only stop at a corner once to wait for mosquitos to catch up
                     ml.numMovesSinceStopped++;
                     if(ml.numMovesSinceStopped == 25) {
                         ml.numMovesSinceStopped = 0;
                         ml.hasStoppedAtCorner = false;
                     }
                 }
                 boolean hasMovedThisTurn = false; //Boolean variable to ensure that a light can only enter a critical code section, i.e where movement occurs, once.
                 if(ml.numMovesSinceStopped == 0) { //Check to see if we need to stop at a corner assuming we haven't seen a corner in the last 25 moves
                     for(Line2D obstacle: walls) {
                         if(!ml.hasStoppedAtCorner && (obstacle.getP1().distance(ml.getLocation()) < 3 || obstacle.getP2().distance(ml.getLocation()) < 3)) {
                             if(ml.numTurnsAtCorner >= 10) { //Wait for obstacle for 10 moves
                                 ml.numTurnsAtCorner = 0;
                                 ml.hasStoppedAtCorner = true;
                                 ml.numMovesSinceStopped = 1;
                             }
                             else {
                                 ml.numTurnsAtCorner++;
                                 hasMovedThisTurn = true;
                             }
                             break;
                         }
                     }
                 }
                 if(ml.getX() == ml.currDestinationX && ml.getY() == ml.currDestinationY) { //If the light has reached its A* destination in our left to right sweep
                     log.error("4");
                     if(!hasMovedThisTurn) {
                         movementMap.put(ml, false);
                         lightsToMovesMap.put(ml, 0);
                         ml.moveRight();
                         hasMovedThisTurn = true;
                     }
                 }
                 else if(!movementMap.get(ml)) { // If the light isn't currently moving on an A* path
                     boolean hasHitWall = false;
                     for(Line2D obstacle: walls) { //Check for obstacles and see if we are near an obstacle
                         if(obstacle.ptSegDistSq(ml.getX(), ml.getY()) <= 4.0) { //We've detected an obstacle 
                             log.error("ENTERED BAD AREA!!! : "+ml);
                             hasHitWall = true;
                             AreaMap cleanMap = new AreaMap(101,101);
                             for(int i = 0; i <= board.length; i++) {
                                 for(int j = 0; j <= board[0].length; j++) {
                                     for(Line2D wall: walls) {
                                        if(wall.ptSegDist(i, j) < 4.0) {
                                             cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
                                         }
                                     }
                                 }
                             }
                             FHeuristic fh = new FHeuristic();
                             astar = new AStar(cleanMap, fh);
                             int xCoordToMoveTo = 0;
                             for(int i = (int) ml.getX() + 2; i < 101; i++) {
                                 if(!cleanMap.getNodes().get(i).get((int)ml.getY()).isObstacle) {
                                     xCoordToMoveTo = i;
                                     break;
                                 }
                             }
                             astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), xCoordToMoveTo, (int) ml.getY()); //Since we've seen an obstacle we need to move the point 6 units to the right
                             ml.currDestinationX = xCoordToMoveTo;
                             ml.currDestinationY = ml.getY();
                             ml.shortestPath = astar.shortestPath;
                             movementMap.put(ml, true);
                             log.error("Current x: "+ml.getX());
                             log.error("Current y: "+ml.getY());
                             if(ml.shortestPath != null && !hasHitWall) {
                                 log.error("Moving to x = "+ml.shortestPath.getX(0));
                                 log.error("Moving to y = "+ml.shortestPath.getY(0));
                                 ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
                                 lightsToMovesMap.put(ml, 1);
                             }
                             // THIS IS A HACK!!!!! 
                             // ANYONE WHO READS THIS MUST KNOW THAT IF YOU DO SUCH THINGS IN REAL WORLD PROJECTS 
                             // KITTENS WILL DIE SPONTANEOUSLY
                             else if(ml.shortestPath == null){
                                 ml.turnOff();
                             }
                         }
                     }
                     if(!hasHitWall) { //If there were no obstacles just move right
                         log.error("3" + ml);
                         if(!hasMovedThisTurn) {
                             ml.moveRight();
                             hasMovedThisTurn = true;
                         }
                     }
                 }
                 else {
                     if(ml.shortestPath == null) { //If there were no obstacles move right
                         log.error("2");
                         if(!hasMovedThisTurn) {
                             ml.moveRight();
                             hasMovedThisTurn = true;
                         }
                     }
                     else if(lightsToMovesMap.get(ml) >= ml.shortestPath.getLength()) { //If we've come to the end of our A* path then reset the A* moves and mvoe right
                         log.error("1");
                         if(!hasMovedThisTurn) {
                             ml.moveRight();
                             lightsToMovesMap.put(ml, 0);
                             movementMap.put(ml, false);
                             hasMovedThisTurn = true;
                         }
                     }
                     else { //If we are still on our A* path then move to the next position in the A* path
                         if(!hasMovedThisTurn) {
                             int moveNum = lightsToMovesMap.get(ml);
                             log.error("CURR: x = "+ml.getX()+ " y = "+ml.getY());
                             log.error("PATH: x = "+ml.shortestPath.getX(moveNum)+" y = "+ml.shortestPath.getY(moveNum));//Log the place we are moving to
                             ml.moveTo(ml.shortestPath.getX(moveNum), ml.shortestPath.getY(moveNum));
                             log.error(ml +"   distance: x = "+(ml.shortestPath.getX(moveNum) - ml.getX())+" y = "+(ml.shortestPath.getY(moveNum) - ml.getY())); //Log the distance between the current position and the next position.
                             moveNum++;
                             lightsToMovesMap.put(ml, moveNum);
                             hasMovedThisTurn = true;
                         }
                     }
                 }
             }
             else { //If we've finished phase one (i.e reached the collector once) then do this
                 if(!movementMap.get(ml)) { // If we aren't on an A* path
                     List<Point2D.Double> mosquitoLocations = getMosquitoLocationsByDistance(board, ml); //Get locations of all the mosquitos ordered in descending order by distance
                     if(!mosquitoLocations.isEmpty()) {
                         AreaMap cleanMap = new AreaMap(101,101);
                         for(int i = 0; i <= board.length; i++) {
                             for(int j = 0; j <= board[0].length; j++) {
                                 for(Line2D wall: walls) {
                                     if(wall.ptSegDist(i, j) < 2.0) {
                                         cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
                                     }
                                 }
                             }
                         }
                         FHeuristic fh = new FHeuristic();
                         astar = new AStar(cleanMap, fh);
                         astar.calcShortestPath((int)ml.getX(),    //Calculate a* path to the farthest mosquito
                                 (int) ml.getY(), 
                                 (int)mosquitoLocations.get(0).getX(), 
                                 (int)mosquitoLocations.get(0).getY());
                         ml.currDestinationX = mosquitoLocations.get(0).getX(); 
                         ml.currDestinationY = mosquitoLocations.get(0).getY();
                         ml.shortestPath = astar.shortestPath;
                         movementMap.put(ml, true);
                         log.error("Current x: "+ml.getX());
                         log.error("Current y: "+ml.getY());
                         if(ml.shortestPath != null) {
                             log.error("Moving to x = "+ml.shortestPath.getX(0)); //Start moving towards farthest mosquito
                             log.error("Moving to y = "+ml.shortestPath.getY(0));
                             ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
                             lightsToMovesMap.put(ml, 1);
                         }
                     }
                 }
                 else if(ml.getX() == ml.currDestinationX && ml.getY() == ml.currDestinationY) { //Once we've reached the farthest mosquito we now go back to the collector
                     movementMap.put(ml, true);
                     ml.currDestinationX = 0;
                     ml.currDestinationY = 0;
                     lightsToMovesMap.put(ml, 0);
                     AreaMap cleanMap = new AreaMap(101,101);
                     for(int i = 0; i <= board.length; i++) {
                         for(int j = 0; j <= board[0].length; j++) {
                             for(Line2D wall: walls) {
                                 if(wall.ptSegDist(i, j) < 2.0) {
                                     if(i==99 && j==50) {
                                         log.error("REMOVE IMPORTANT NODE!!!");
                                     }
                                     cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
                                 }
                             }
                         }
                     }
                     FHeuristic fh = new FHeuristic();
                     astar = new AStar(cleanMap, fh);
                     astar.calcShortestPath((int)ml.getX(), (int) ml.getY(), 97, 50);
                     ml.currDestinationX = 97;
                     ml.currDestinationY = 50;
                     ml.shortestPath = astar.shortestPath;
                     movementMap.put(ml, true);
                     log.error("Current x: "+ml.getX());
                     log.error("Current y: "+ml.getY());
                     if(ml.shortestPath != null) {
                         log.error("Moving to x = "+ml.shortestPath.getX(0));
                         log.error("Moving to y = "+ml.shortestPath.getY(0));
                         ml.moveTo(ml.shortestPath.getX(0), ml.shortestPath.getY(0));
                         lightsToMovesMap.put(ml, 1);
                     }
                     continue;
                 }
                 else { //If we haven't reached the furthest mosquito then continue moving towards it using the A* path
                     int moveNum = lightsToMovesMap.get(ml);
                     log.error("CURR: x = "+ml.getX()+ " y = "+ml.getY());
                     log.error("PATH: x = "+ml.shortestPath.getX(moveNum)+" y = "+ml.shortestPath.getY(moveNum));//Log the place we are moving to
                     ml.moveTo(ml.shortestPath.getX(moveNum), ml.shortestPath.getY(moveNum));
                     log.error(ml +"   distance: x = "+(ml.shortestPath.getX(moveNum) - ml.getX())+" y = "+(ml.shortestPath.getY(moveNum) - ml.getY())); //Log the distance between the current position and the next position.
                     moveNum++;
                     lightsToMovesMap.put(ml, moveNum);
                 }
             }
         }
         return lights;
     }
     
     public List<Point2D.Double> getMosquitoLocationsByDistance(int[][] board, MoveableLight ml) {
         List<Point2D.Double> result = new ArrayList<Point2D.Double>();
         for(int i = 0; i < 100; i++) {
             for(int j = 0; j < 100; j++) {
                 if(i < 95 && (j < 48 || j > 52) && !isNearAnotherLight(i,j,ml)) { //If the mosquito isn't near the collector and it isn't near another light
                     if(board[i][j] != 0) {
                         result.add(new Point2D.Double(i, j));
                     }
                 }
             }
         }
         Collections.sort(result, new Comparator<Point2D.Double>() {
 
             @Override
             public int compare(Point2D.Double o1, Point2D.Double o2) { //Sort the mosquito's in descending order by distance.
                 Point2D.Double origin = new Point2D.Double(97, 50);
                 double o1Distance = origin.distance(o1);
                 double o2Distance = origin.distance(o2);
                 if(o1Distance > o2Distance) {
                     return -1;
                 }
                 else if(o2Distance > o1Distance) {
                     return 1;
                 }
                 return 0;
             }
         });
         
         return result;
     }
     
     public boolean isNearAnotherLight(int i, int j, MoveableLight ml) { //Returns if the the mosquitos at this point are near another light (to avoid random bugs like one light moving to another light
        for(MoveableLight other: mlights) {                              //unnecessarily because the other light would have already caught the mosquitos).
             if(other.getLocation() != ml.getLocation() && other.isOn()) {
                 Point2D.Double locationOfOther = (Point2D.Double) other.getLocation();
                 Point2D.Double locationOfMosquito = new Point2D.Double(i,j);
                 if (locationOfOther.distance(locationOfMosquito) < 8) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /*
      * Currently this is only called once (after getLights), so you cannot
      * move the Collector.
      */
     @Override
     public Collector getCollector() {
         // this one just places a collector next to the last light that was added
         Collector c = new Collector(97,50);
         return c;
     }
 
 }
