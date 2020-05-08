 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class CustomBot extends AbstractHiveMind {
 
     public static enum Status {
         SAFE, KILL, DIE
     }
 
     private static boolean LOGGING_ENABLED = false;
 
     private static boolean LOGGING_VIS_ENABLED = false;
     private static boolean LOGGING_VIS_MAIN_INFLUENCE = false;
     private static boolean LOGGING_VIS_ANT_INFLUENCE = false;
 
     private static int INFLUENCE_MAX = Integer.MAX_VALUE;
     private static int INFLUENCE_FOOD = INFLUENCE_MAX;
     private static int INFLUENCE_ENEMY_HILL = INFLUENCE_MAX;
     private static int INFLUENCE_UNEXPLORED = INFLUENCE_MAX / 10;
     private static int INFLUENCE_UNSEEN = INFLUENCE_MAX / 20;
     private static int INFLUENCE_MY_HILL_DEFEND = INFLUENCE_MAX;
     private static int INFLUENCE_MY_ANT = 0;
 
     private static String ANT_COLOR_ENEMY = "255 0 0";
     private static String ANT_COLOR_MY = "0 255 0";
 
     private static Map<Cell,Integer> lastSeen = new HashMap<Cell, Integer>();
 
     @Override
     protected void doTurn(IField field) {
         Utils.tic();
 
         //log_err("Current turn: " + field.getTurnNumber());
 
         //Set<Cell> myAnts = field.getMyAntPositions();
         //Set<Cell> orderedAnts = new HashSet<Cell>();
 
         //collectFood(field, myAnts, orderedAnts);
 
         //updateLastSeen(field);
 
         diffuse(field);
 
         //attack(field, myAnts, orderedAnts);
 
         //explore(field, myAnts, orderedAnts);
 
         //randomMoves(field, myAnts, orderedAnts);
 
         log_err("Turn: " + field.getTurnNumber() + "    Time: " + Utils.toc());
     }
 
     private void diffuse(IField field) {
         double[][] diffExp = new double[info.rows][info.cols];
         boolean[][] diffusedExp = new boolean[info.rows][info.cols];
         Set<Cell> myHills = field.getMyHills();
 
         // seed the main influence map
         for(int row = 0; row < info.rows; row ++) {
             for(int col = 0; col < info.cols; col ++) {
                 Cell cell = new Cell(row, col);
                 Owned o = field.get(row, col);
 
                 if (o.type.equals(Cell.Type.HILL) && o.isEnemys()) {
                     diffExp[row][col] = INFLUENCE_ENEMY_HILL;
                     diffusedExp[row][col] = true;
                 }
                 else if (o.type.equals(Cell.Type.FOOD) ) {
                     diffExp[row][col] = INFLUENCE_FOOD;
                     diffusedExp[row][col] = true;
                 }
                 else if (o.type.equals(Cell.Type.ANT) && o.isEnemys() && isCloseToMyHill(field, myHills, cell)) {
                     diffExp[row][col] = INFLUENCE_MY_HILL_DEFEND;
                     diffusedExp[row][col] = true;
                 }
                 else if (o.type.equals(Cell.Type.ANT) && o.isMine()) {
                     diffExp[row][col] = INFLUENCE_MY_ANT;
                     diffusedExp[row][col] = true;
                 }
                 else if (o.type.equals(Cell.Type.WATER) || (o.type.equals(Cell.Type.HILL) && o.isMine())) {
                     diffExp[row][col] = 0;
                     diffusedExp[row][col] = true;
                 }
                 else if (!o.explored) {
                     diffExp[row][col] = INFLUENCE_UNEXPLORED;
                     diffusedExp[row][col] = true;
                 }
 //                else if (o.explored && !field.isSeen(cell)) {
 //                    diffExp[row][col] = INFLUENCE_UNSEEN;
 //                    diffusedExp[row][col] = true;
 //                }
                 else {
                     // lastSeen or unexplored
                     //Integer seenTurn = lastSeen.get(cell);
                     //log_err("Last seen " + cell + ":  " + seenTurn);
                     //int seenTurnsAgo = field.getTurnNumber() - seenTurn;
                     //diffExp[row][col] = INFLUENCE_MAX / 2  - ((200 - seenTurnsAgo) * (INFLUENCE_MAX / 200 / 2));
                 }
 
             }
         }
 
         // iterate to diffuse the influence
         int iterations = 100;
         for (int i=0; i<iterations; i++) {
             for(int row = 0; row < info.rows; row ++) {
                 for(int col = 0; col < info.cols; col ++) {
                     if (diffusedExp[row][col])
                         continue;
                     double up = diffExp[get_dest(row, -1, info.rows)][col];
                     double down = diffExp[get_dest(row, 1, info.rows)][col];
                     double left = diffExp[row][get_dest(col, -1, info.cols)];
                     double right = diffExp[row][get_dest(col, 1, info.cols)];
 
                     double divider = 4.0;
                     diffExp[row][col] = up/divider + down/divider + left/divider + right/divider;
                 }
             }
         }
 
         // calculate my/enemy influence map
         int influenceRadius = (int) Math.ceil(Math.sqrt(info.attackRadiusSquared));
 
         Set<Cell> enemyAnts = field.getEnemyAnts();
         int enemyPlayers = 0;
         for (Cell c : enemyAnts) {
             enemyPlayers = Math.max(enemyPlayers, field.get(c).owner);
         }
         int playerCount = enemyPlayers+1;
 
         int[][][] playerInfluence = new int[playerCount][info.rows][info.cols];
         int[][] totalInfluence = new int[info.rows][info.cols];
         Set<Cell> ants = field.getAnts();
         for (Cell ant : ants) {
             for (int row = -influenceRadius; row<=influenceRadius; row++) {
                 for (int col = -influenceRadius; col<=influenceRadius; col++) {
                     int crow = get_dest(ant.row, row, info.rows);
                     int ccol = get_dest(ant.col, col, info.cols);
                     int minDistSquared = getMinDist(field, getNeighbours(field, ant), Cell.of(crow, ccol));
                     if (minDistSquared <= info.attackRadiusSquared) {
                         totalInfluence[crow][ccol] += 1;
                         playerInfluence[field.get(ant).owner][crow][ccol] += 1;
                     }
                 }
             }
         }
 
         // calculate fighting
         int[][][] fighting = new int[playerCount][info.rows][info.cols];
         for(int p = 0; p<playerCount; p++) {
             for(int row = 0; row < info.rows; row ++) {
                 for(int col = 0; col < info.cols; col ++) {
                     fighting[p][row][col] = Integer.MAX_VALUE;
                 }
             }
         }
         for(Cell ant : ants) {
            for(Cell position : getNeighboursIncludingSelf(field, ant)) {
                int antsFighting = totalInfluence[position.row][position.col] - playerInfluence[field.get(ant).owner][position.row][position.col];
                // now store this in each tile in the fighting array within the combat zone of that tile if it is less than current value
                for (int row = -influenceRadius; row<=influenceRadius; row++) {
                    for (int col = -influenceRadius; col<=influenceRadius; col++) {
                        int crow = get_dest(position.row, row, info.rows);
                        int ccol = get_dest(position.col, col, info.cols);
                        int minDistSquared = field.getDistance(position, Cell.of(crow, ccol));
                        if (minDistSquared <= info.attackRadiusSquared) {
                            fighting[field.get(ant).owner][crow][ccol] = Math.min(fighting[field.get(ant).owner][crow][ccol], antsFighting);
                        }
                    }
                }
            }
         }
 
         // now let's calculate SKD = Safe, Kill, Die
         Status[][] status = new Status[info.rows][info.cols];
         int[][] bestArr = new int[info.rows][info.cols];
         for (Cell myAnt : field.getMyAntPositions()) {
             for(Cell position : getNeighboursIncludingSelf(field, myAnt)) {
                 int enemy_count = totalInfluence[position.row][position.col] - playerInfluence[0][position.row][position.col];
                 int best = Integer.MAX_VALUE;
                 for(int p = 1; p<playerCount; p++) {
                     best = Math.min(best, fighting[p][position.row][position.col]);
                 }
                 bestArr[position.row][position.col] = best;
                 if (best < enemy_count) {
                     status[position.row][position.col] = Status.DIE;
                 }
                 else if (best == enemy_count && enemy_count > 0) {
                     status[position.row][position.col] = Status.KILL;
                 }
                 else {
                     status[position.row][position.col] = Status.SAFE;
                 }
             }
         }
 
         // now make decisions where free ants can go
         for (Cell antLoc : field.getMyAntPositions()) {
             Direction direction = getDirectionHighestDiff(field, antLoc, diffExp, status);
             if (direction != null) {
                 issueOrder(antLoc, direction);
                 log_err("Sent ant: " + antLoc + "  to " + direction);
             }
             else {
                 log_err("Ant stuck: " + antLoc);
             }
         }
 
         // visualize ant influence map
         if (LOGGING_VIS_ENABLED && LOGGING_VIS_ANT_INFLUENCE)
             for(int row = 0; row < info.rows; row ++) {
                 for(int col = 0; col < info.cols; col ++) {
                     if (playerInfluence[0][row][col] > 0) {
                         log_out("i " + row + " " + col + " my influence: " + playerInfluence[0][row][col]);
                         log_out("i " + row + " " + col + " total influence: " + totalInfluence[row][col]);
                         if (status[row][col] != null) {
                             log_out("i " + row + " " + col + " status: " + status[row][col].toString());
                             log_out("i " + row + " " + col + " fighting: " + fighting[0][row][col]);
                             int enemy_count = totalInfluence[row][col] - playerInfluence[0][row][col];
                             log_out("i " + row + " " + col + " enemy_count: " + enemy_count);
                             log_out("i " + row + " " + col + " best: " + bestArr[row][col]);
                         }
                     }
                 }
             }
 
         // visualize main influence map
         if (LOGGING_VIS_ENABLED && LOGGING_VIS_MAIN_INFLUENCE)
             for(int row = 0; row < info.rows; row ++) {
                 for(int col = 0; col < info.cols; col ++) {
                     log_out("v setFillColor 255 0 0 " + diffExp[row][col]/ INFLUENCE_MAX);
                     log_out("v tile " + row + " " + col);
                     log_out("i " + row + " " + col + " " + diffExp[row][col]/ INFLUENCE_MAX);
                 }
             }
     }
 
     private Set<Cell> getNeighboursIncludingSelf(IField field, Cell cell) {
         Set<Cell> r = getNeighbours(field, cell);
         r.add(cell);
         return r;
     }
 
     private Set<Cell> getNeighbours(IField field, Cell cell) {
         Set<Cell> r = new HashSet<Cell>();
         Cell north = field.getDestination(cell, Direction.NORTH); Owned o = field.get(north.row, north.col); if (!o.type.equals(Cell.Type.WATER)) r.add(north);
         Cell south = field.getDestination(cell, Direction.SOUTH); o = field.get(south.row, south.col); if (!o.type.equals(Cell.Type.WATER)) r.add(south);
         Cell west = field.getDestination(cell, Direction.WEST); o = field.get(west.row, west.col); if (!o.type.equals(Cell.Type.WATER)) r.add(west);
         Cell east = field.getDestination(cell, Direction.EAST); o = field.get(east.row, east.col); if (!o.type.equals(Cell.Type.WATER)) r.add(east);
         return r;
     }
 
     private int getMinDist(IField field, Set<Cell> neighbours, Cell cell) {
         int r = Integer.MAX_VALUE;
         for (Cell n : neighbours) {
             r = Math.min(r, field.getDistance(n, cell));
         }
         return r;
     }
 
     private boolean isCloseToMyHill(IField field, Set<Cell> myHills, Cell cell) {
         if (myHills.size() == 0)
             return false;
         int min_distance = Integer.MAX_VALUE;
         for (Cell hill : myHills) {
             int distance = field.getDistance(cell, hill);
             min_distance = Math.min(min_distance, distance);
         }
 
         return (min_distance <= info.viewRadiusSquared);
     }
 
     private Direction getDirectionHighestDiff(IField field, Cell cell, double[][] diffExp, Status[][] status) {
         Cell north = field.getDestination(cell, Direction.NORTH);
         Cell south = field.getDestination(cell, Direction.SOUTH);
         Cell west = field.getDestination(cell, Direction.WEST);
         Cell east = field.getDestination(cell, Direction.EAST);
 
         double diffNorth = field.get(north).type.isPassable()  && (status[north.row][north.col] == Status.SAFE)  ? diffExp[north.row][north.col] : 0;
         double diffSouth = field.get(south).type.isPassable()  && (status[south.row][south.col] == Status.SAFE)  ? diffExp[south.row][south.col] : 0;
         double diffWest = field.get(west).type.isPassable()  && (status[west.row][west.col] == Status.SAFE)  ? diffExp[west.row][west.col] : 0;
         double diffEast = field.get(east).type.isPassable()  && (status[east.row][east.col] == Status.SAFE)  ? diffExp[east.row][east.col] : 0;
 
         double maxDiff = Math.max(Math.max(Math.max(diffNorth, diffSouth), diffWest), diffEast);
 
        //log_err("Diffusion for " + cell + ":  " + diffNorth + " " + diffEast + " " + diffSouth + " " + diffWest);
        //System.out.println("i " + cell.row + " " + cell.col + " N: " + diffNorth + "  E: " + diffEast + "  S: " + diffSouth + "   W: " + diffWest );
        if (maxDiff == 0.0) {
             return null;
         }
        if (diffNorth == maxDiff) {
             return Direction.NORTH;
         }
        else if (diffSouth == maxDiff) {
             return Direction.SOUTH;
         }
        else if (diffWest == maxDiff) {
             return Direction.WEST;
         }
        return Direction.EAST;
     }
 
     private void print(int[][] diffExp) {
         for(int row = 0; row < diffExp.length; row ++) {
             for(int col = 0; col < diffExp[0].length; col ++) {
                 System.err.print(diffExp[row][col] + " ");
             }
             log_err("");
         }
     }
 
     private int get_dest(int row, int direction, int rows) {
         int target_row = (row + direction) % rows;
         if (target_row < 0) {
             target_row += rows;
         }
         return target_row;
     }
 
     private void explore(IField field, Set<Cell> myAnts, Set<Cell> orderedAnts) {
         int remainingAnts = myAnts.size() - orderedAnts.size();
         if (remainingAnts <= 0) {
             return;
         }
 
         Set<Cell> unexplored = field.getUnexplored();
         log_err("exploration phase... remaining myAnts: " + remainingAnts);
         Set<Cell> exploredEdge = new HashSet<Cell>();
         for (Cell cell : unexplored) {
             for (Direction direction: Direction.values()) {
                 Cell neighbour = field.getDestination(cell, direction);
                 if (!unexplored.contains(neighbour) && field.get(neighbour).type.isPassable()) {
                     exploredEdge.add(neighbour);
                     break;
                 }
             }
         }
         //log_err("explored edge: " + exploredEdge.size());
 
 
         List<Route> unseenRoutes = new ArrayList<Route>();
         for (Cell antLoc : myAnts) {
             if (!orderedAnts.contains(antLoc)) {
                 for (Cell unseenLoc : exploredEdge) {
                     int distance = Math.abs(unseenLoc.row - antLoc.row) + Math.abs(unseenLoc.col - antLoc.col);
                     Route route = new Route(antLoc, unseenLoc, distance, null);
                     unseenRoutes.add(route);
                 }
             }
         }
         Collections.sort(unseenRoutes);
 
         for (Route route : unseenRoutes) {
             List<PathNode> path = field.getPathFinder().computePath(route.getStart(), route.getEnd());
             if (path != null && path.get(0).direction != null && !orderedAnts.contains(route.getStart())) {
                 issueOrder(route.getStart(), path.get(0).direction);
                 orderedAnts.add(route.getStart());
                 log_err("Sent ant to explore: " + route.getStart() + "  to " + route.getEnd());
                 remainingAnts--;
             }
             if (remainingAnts <= 0) {
                 break;
             }
         }
     }
 
     private void attack(IField field, Set<Cell> myAnts, Set<Cell> orderedAnts) {
         int remainingAnts = myAnts.size() - orderedAnts.size();
         if (remainingAnts <= 0) {
             return;
         }
 
         if (remainingAnts > (field.getEnemyAnts().size() * 3) /*&& remainingAnts >= 100*/) {
             log_err("attack phase... remaining myAnts: " + remainingAnts);
             List<Route> hillRoutes = new ArrayList<Route>();
             for (Cell hillLoc : field.getEnemyHills()) {
                 for (Cell antLoc : myAnts) {
                     if (!orderedAnts.contains(antLoc)) {
                         int distance = Math.abs(hillLoc.row - antLoc.row) + Math.abs(hillLoc.col - antLoc.col);
                         Route route = new Route(antLoc, hillLoc, distance, null);
                         hillRoutes.add(route);
                     }
                 }
             }
             Collections.sort(hillRoutes);
             for (Route route : hillRoutes) {
                 List<PathNode> path = field.getPathFinder().computePath(route.getStart(), route.getEnd());
                 if (path != null) {
                     issueOrder(route.getStart(), path.get(0).direction);
                     orderedAnts.add(route.getStart());
                     log_err("Sent ant after hill: " + route.getStart() + "  to " + route.getEnd());
                     remainingAnts--;
                     if (remainingAnts <= 0) {
                         break;
                     }
                 }
             }
         }
     }
 
     private void collectFood(IField field, Set<Cell> ants, Set<Cell> orderedAnts) {
         log_err("food phase...");
 
         Set<Cell> foods = field.getSeenFood();
         Map<Cell, Cell> foodTargets = new HashMap<Cell, Cell>();
         Map<Cell, Direction> foodDirections = new HashMap<Cell, Direction>();
         Map<Cell, Cell> foodDestinations = new HashMap<Cell, Cell>();
 
         List<Route> foodRoutes = new ArrayList<Route>();
         TreeSet<Cell> sortedFood = new TreeSet<Cell>(foods);
         TreeSet<Cell> sortedAnts = new TreeSet<Cell>(ants);
         for (Cell foodLoc : sortedFood) {
             for (Cell antLoc : sortedAnts) {
                 List<PathNode> path = field.getPathFinder().computePath(antLoc, foodLoc);
                 if (path != null) {
                     int distance = path.size();
                     Route route = new Route(antLoc, foodLoc, distance, path.get(0).direction);
                     foodRoutes.add(route);
                 }
             }
         }
 
         Collections.sort(foodRoutes);
 
         for (Route route : foodRoutes) {
             if (!foodTargets.containsKey(route.getEnd())
                     && !foodTargets.containsValue(route.getStart())) {
                 foodTargets.put(route.getEnd(), route.getStart());
                 foodDirections.put(route.getStart(), route.getDirection());
                 foodDestinations.put(route.getStart(), route.getEnd());
             }
         }
 
         for (Cell myAnt : foodTargets.values()) {
             Direction direction = foodDirections.get(myAnt);
             if (direction == null) {
                 continue;
             }
             Cell dest = field.getDestination(myAnt, direction);
             if (field.get(dest).type.isPassable() && !isMyHill(field, dest)) {
                 issueOrder(myAnt, direction);
                 log_err("Sent ant after food: " + myAnt + "  to " + foodDestinations.get(myAnt));
                 orderedAnts.add(myAnt);
             }
         }
     }
 
     private void randomMoves(IField field, Set<Cell> ants, Set<Cell> orderedAnts) {
         int remainingAnts;
         remainingAnts = ants.size() - orderedAnts.size();
         if (remainingAnts <= 0) {
             return;
         }
         log_err("random phase...");
         for (Cell ant : ants) {
             if (!orderedAnts.contains(ant)) {
                 if (antHasOptions(field, ant)) {
                     Direction direction = getRandomDirection(field, ant);
                     if (direction != null) {
                         issueOrder(ant, direction);
                         orderedAnts.add(ant);
                         log_err("Sent ant for random walk: " + ant + "  to " + field.getDestination(ant, direction));
                     }
                 }
             }
         }
     }
 
     private Direction getRandomDirection(IField field, Cell ant) {
         boolean passable = false;
         while (!passable) {
             Direction direction = Direction.getRandom();
             Cell dest = field.getDestination(ant, direction);
             if (field.get(dest).type.isPassable() && !isMyHill(field, dest)) {
                 return direction;
             }
             passable = true;
         }
         return null;
     }
 
     private boolean isPassable(IField field, Cell cell) {
         return field.get(cell).type.isPassable() && !isMyHill(field, cell);
 
     }
     private boolean antHasOptions(IField field, Cell ant) {
         return isPassable(field, field.getDestination(ant, Direction.WEST)) || isPassable(field, field.getDestination(ant, Direction.NORTH))
                 || isPassable(field, field.getDestination(ant, Direction.EAST)) || isPassable(field, field.getDestination(ant, Direction.SOUTH));
     }
 
     private boolean isMyHill(IField field, Cell cell) {
         Owned p = field.get(cell);
         return p.type == Cell.Type.HILL && p.isMine();
     }
 
     private Set<Cell> getAllSeen(IField field) {
         Set<Cell> result = new HashSet<Cell>();
 
         for(int row = 0; row < info.rows; row ++) {
             for(int col = 0; col < info.cols; col ++) {
                 Cell c = Cell.of(row, col);
                 if (field.isSeen(c))
                     result.add(c);
             }
         }
         return result;
     }
 
     private void updateLastSeen(IField field) {
         Set<Cell> currentSeen = getAllSeen(field);
         for(Cell c : currentSeen) {
             lastSeen.put(c, field.getTurnNumber());
         }
     }
 
     private void log_err(String s) {
         if (LOGGING_ENABLED)
             System.err.println(s);
     }
 
     private void log_out(String s) {
         if (LOGGING_VIS_ENABLED)
             System.out.println(s);
     }
 
 
 }
