 package team197.modules;
 
 import battlecode.common.RobotController;
 import battlecode.common.MapLocation;
 import battlecode.common.Direction;
 
 
 public class MapModule {
     public static final int MAX_WAYPOINTS = 10;
 
     private static final int GROUND_OPEN = 0,
                              GROUND_MINE = 12,
                              GROUND_ENCAMPMENT = 1; //Placeholder no; doesn't matter b/c will be checked against
     private static final Direction[] DIRECTIONS = new Direction[] {Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST,
                                                                    Direction.WEST,                        Direction.EAST,
                                                                    Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};
 
     private int[][] map_weights;
     private int mapwidth,
                 mapheight,
                 gscore,
                 fscore;
     private MapLocation start,
                         end;
 
 
     public MapModule(RobotController rc) {
         mapwidth = rc.getMapWidth();
         mapheight = rc.getMapHeight();
     }
 
     /** Resenses the weights array.
      * Clears the weights array as a side effect by instantializing
      *  a new array.  Java ensures that the newly created array will
      *  be filled by zeros.
      */
     private void recalc_weights(RobotController rc){
         MapLocation[] locs;
         int x, y;
 
         // Clear the map by creating a new array, avoiding the need
         // to walk through it.
         map_weights = new int[mapwidth][mapheight];
 
         // Place all of the mines onto the map
         locs = rc.senseNonAlliedMineLocations(new MapLocation(0,0),mapheight * mapheight + mapwidth * mapwidth);
         for(int i = 0; i < locs.length; i ++)
             map_weights[locs[i].x][locs[i].y] = GROUND_MINE;
 
         // Place all of the encampments
         locs = rc.senseAllEncampmentSquares();
         for(int i = 0; i < locs.length; i++)
             map_weights[locs[i].x][locs[i].y] = GROUND_ENCAMPMENT;
     }
 
     /** Searches for the shortest path between the start and end destinations.
      * This is a computationally intensive function that doesn't return until
      *  the computation is complete.  The returned MapLocation[] is guaranteed
      *  to be at most MAX_WAYPOINTS long, and should be a good approximation of
      *  the shortest path from start to end.
      */
     public MapLocation[] make_path(RobotController rc, MapLocation start, MapLocation end){
         PathNode[][] map_nodes;
         MapLocation[] retval;
         MapLocation curLoc;
         PathNode path,
                  cur,
                  tmp;
         int cost,
             estimate,
             skip;
 
         // First recalculate the weights for the map and initialize the closed set
         recalc_weights(rc);
         map_nodes = new PathNode[mapwidth][mapheight];
         System.out.println(" - Recalculated map weights\n");
 
         // We start with 'start' on the queue.  As long as there's a node
         //  on the queue, grab it and expand it.
         path = new PathNode(0, 0, start.distanceSquaredTo(end), start);
         map_nodes[path.loc.x][path.loc.y] = path;
         do {
             // Pop the current node off the queue.
             cur = path;
             path = path.pop();
 
             // If we've reached the destination, we're ready to read the
             //  resulting path!
             if(cur.loc.equals(end))
                 break;
 
             // Now step through this squares neighbors and add any unvisited
             // locations to the priority queue.
             for(int i = 0; i < DIRECTIONS.length; i++) {
                 // Calculate the new position
                 curLoc = cur.loc.add(DIRECTIONS[i]);
 
                 // Check if the next node is within the map bounds
                 if(curLoc.x < 0 || curLoc.x >= mapwidth ||
                    curLoc.y < 0 || curLoc.y >= mapheight)
                     continue;
 
                 // The new node should have a path cost of 1 + cur's path cost +
                 //  the value in the weights array for this location.
                 cost = cur.pathCost+map_weights[curLoc.x][curLoc.y]+1;
                 estimate = cost+curLoc.distanceSquaredTo(end);
 
                 // Check if this node is already in the queue.  If it is, and
                 //  the new estimate is better than the old one, replace the old
                 //  estimate.
                 if(map_nodes[curLoc.x][curLoc.y] == null) {
                     tmp = new PathNode(cost, cur.pathLength+1, estimate, curLoc);
                     map_nodes[curLoc.x][curLoc.y] = tmp;
                 } else if(estimate < map_nodes[curLoc.x][curLoc.y].estimateCost) {
                     tmp = map_nodes[curLoc.x][curLoc.y];
                     tmp.pathCost = cost;
                     tmp.estimateCost = estimate;
                     tmp.pathLength = cur.pathLength+1;
 
                     // Remove this node from its previous position
                     path = path.remove(tmp);
                 } else {
                    continue;
                 }
 
                 // For this node, the next step in the final path would be the
                 //  node we started with (cur)
                 tmp.nextpath = cur;
 
                 // Now add this node to the priority queue.
                 if(path == null)
                     path = tmp;
                 else
                     path = path.push(tmp);
             }
         } while(path != null);
 
         // cur should now contain end, and should form a path to
         //  start.  If there are less than MAX_WAYPOINTS steps, make
         //  each one a waypoint.  Otherwise, skip steps so that only
         //  MAX_WAYPOINTS steps are returned.
         //
         // When skipping, 'end' should be the last element of the
         //  the array, while it doesn't necessarily start with 'start'
         if(cur.pathLength < MAX_WAYPOINTS) {
             skip = 1;
             retval = new MapLocation[cur.pathLength];
         } else {
             skip = cur.pathLength/MAX_WAYPOINTS;
             retval = new MapLocation[MAX_WAYPOINTS];
         }
 
         // Now actually fill out the return value
         for(int i = retval.length-1; i >= 0; i--) {
             retval[i] = cur.loc;
             System.out.print("(" + retval[i].x + ", " + retval[i].y+")\n");
 
             // Skip 'skip' number of nodes
             for(int k = 0; k < skip; k++)
                 cur = cur.nextpath;
         }
 
         // And we're done
         return retval;
     }
 
     public MapLocation[][] findEncampments(RobotController rc, int numClosest){
         MapLocation[][] retval;
         MapLocation curLoc;
         int[][] map_costs;
         PathNode path,
                  cur,
                  tmp,
                  lastEncampment;
         int encamp_i,
             cost;
         boolean stop;
 
         // First recalculate the weights for the map and initialize map
         recalc_weights(rc);
         map_costs = new int[mapwidth][mapheight];
         retval = new MapLocation[3][];
         retval[0] = new MapLocation[numClosest];
 
         // A square is unvisited if the map_costs value is equal to 0.  To avoid
         //  revisiting the start location, mark it with -1.
         path = new PathNode(0, 0, 0, rc.senseHQLocation());
         map_costs[path.loc.x][path.loc.y] = -1;
 
         // Now do a type of Dijkstra's search for encampments
         lastEncampment = null;
         encamp_i = 0;
         stop = false;
         while(path != null) {
             // Get the next location to expand
             cur = path;
             path = path.pop();
 
             // If this square is marked with a negative value smaller than -1,
             //  we've hit the opposed search.  This means we should stop searching
             //  after we find the next encampment, and stop the opposed search.
             if(map_costs[cur.loc.x][cur.loc.y] < -1)
                 stop = true;
             else if(map_costs[cur.loc.x][cur.loc.y] != 0 ||
                     map_costs[cur.loc.x][cur.loc.y] <= cur.estimateCost)
                 continue;
 
             // If this square is an encampment, we should record it's location
             if(map_weights[cur.loc.x][cur.loc.y] == GROUND_ENCAMPMENT) {
                 // Only record up to numClosest locations
                 if(encamp_i < numClosest)
                     retval[0][encamp_i++] = cur.loc;
 
                 // If we're stopping, then 'cur' and 'lastEncampment' should
                 //  be rotationally symmetrical encampments.  These are the two
                 //  encampments to record paths for.
                 if(stop) {
                     // Record lastEncampment (the closer encampment to us) first
                     retval[1] = new MapLocation[lastEncampment.pathLength];
                     tmp = lastEncampment;
                     for(int i = 0; i < lastEncampment.pathLength; i++, tmp = tmp.nextpath)
                         retval[1][lastEncampment.pathLength-i-1] = tmp.loc;
 
                     // Record cur (the farther encampment to us) next
                     retval[1] = new MapLocation[cur.pathLength];
                     tmp = cur;
                     for(int i = 0; i < cur.pathLength; i++, tmp = tmp.nextpath)
                         retval[1][cur.pathLength-i-1] = tmp.loc;
 
                     break;
                 } else {
                     lastEncampment = cur;
                 }
             }
 
             // Now mark the map and it's rotationally symmetrical counterpart
             map_costs[cur.loc.x][cur.loc.y] = cur.estimateCost;
             map_costs[mapwidth-cur.loc.x][mapheight-cur.loc.y] = -cur.estimateCost;
 
             // Finally, add this location's neighbors to the queue
             for(int i = 0; i < DIRECTIONS.length; i++) {
                 curLoc = cur.loc.add(DIRECTIONS[i]);
 
                 // Add this node to the queue.  Note that there are no
                 //  heuristics for this search.
                 cost = cur.pathCost+map_weights[curLoc.x][curLoc.y]+1;
                 tmp = new PathNode(cost, cur.pathLength+1, cost, curLoc);
                 tmp.nextpath = cur;
                 if(path != null)
                     path = path.enqueue(tmp);
                 else
                     path = tmp;
             }
         }
 
         // Finally, return
         return retval;
     }
 }
 
 
 /** Node for a priority queue of map locations, used to find shortest paths.
  * Each node holds a reference to the next node in the queue as well as
  *  the next node in a possible path.  Once the algorithm is complete, following
  *  'nextpath' should bring you from the start to the end.
  *
  * The double-linked list part of the PathNode, internally, is a circular queue.
  *  This means that the heads of lists know about the tails, so things can be
  *  appended to the end very easily.  It also makes link upkeep much simpler.
  */
 class PathNode {
     PathNode nextpath,
              prev,
              next;
     MapLocation loc;
     int pathCost,
         pathLength,
         estimateCost;
 
     public PathNode(int pathCost, int pathLength, int estimateCost, MapLocation loc) {
         this.pathCost = pathCost;
         this.pathLength = pathLength;
         this.estimateCost = estimateCost;
         this.loc = loc;
 
         prev = this;
         next = this;
     }
 
     /** Adds the given node to the list starting at this node.
      * Returns the head of the new list, which is either 'node'
      *  or this node.
      */
     public PathNode push(PathNode node) {
     	PathNode temp;
 
         // Loop through the queue to find where the node should
         // be inserted, starting from the front.
         temp = this;
         do {
             if(node.estimateCost <= temp.estimateCost) {
                 node.prev = temp.prev;
                 node.next = temp;
                 temp.prev.next = node;
                 temp.prev = node;
 
                 // If temp is the current node, that means
                 // 'node' is the new head of the list
                 if(temp == this)
                     return node;
                 else
                     return this;
             }
 
             temp = temp.next;
         } while(temp != this);
 
         // If we make it here, the node should be placed
         // at the end of the list.
         node.prev = prev;
         node.next = this;
         prev.next = node;
         prev = node;
         return this;
     }
 
     /** Adds the given node to the list starting at the end of
      *   of the list.
      * Returns the head of the new list, which is either 'node'
      *  or this node.
      */
     public PathNode enqueue(PathNode node) {
     	PathNode temp;
 
         // Loop through the queue to find where the node should
         // be inserted, starting from the back;
         temp = this.prev;
         do {
             if(node.estimateCost > temp.estimateCost) {
                 node.prev = temp;
                 node.next = temp.next;
                 temp.next.prev = node;
                 temp.next = node;
                 return this;
             }
 
             temp = temp.prev;
         } while(temp != this);
 
         // If we make it here, the node should be placed
         // at the start of the list.
         node.prev = this;
         node.next = next;
         next.prev = node;
         next = node;
         return node;
     }
 
     /** Pops this node off of the list.
      * Returns the next node in the queue, which may be null.
      */
     public PathNode pop() {
         PathNode tmp;
 
         // If this is the only node in the list, the new list
         //  is null.
         tmp = next;
         if(tmp != this) {
             prev.next = next;
             next.prev = prev;
         } else {
             tmp = null;
         }
 
        prev = this;
        next = this;
         return tmp;
     }
 
     /** Removes the node from this list.
      * Returns the new head of the list.
      */
     public PathNode remove(PathNode node) {
         if(this == node)
             return pop();
 
         node.prev.next = node.next;
         node.next.prev = node.prev;
        node.prev = node;
        node.next = node;
 
         return this;
     }
 }
