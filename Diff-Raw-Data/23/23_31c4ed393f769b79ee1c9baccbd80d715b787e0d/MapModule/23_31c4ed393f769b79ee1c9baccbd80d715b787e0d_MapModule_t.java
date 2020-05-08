 package team197.modules;
 
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.RobotController;
 import battlecode.common.MapLocation;
 
 
 public class MapModule {
     private static final int SEARCH_SCALE = 3,
                              SEARCH_MID = 1,
                              GROUND_OPEN = 0,
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
 
 
     public MapModule(RobotController rc) {
         mapwidth = rc.getMapWidth();
         mapheight = rc.getMapHeight();
     }
 
     /** Resenses the weights array in a limited area.
      * Clears the weights array as a side effect by instantializing
      *  a new array.  Java ensures that the newly created array will
      *  be filled by zeros.
      */
     private void recalc_weights(RobotController rc, MapLocation center, int width, int height, int scale) throws GameActionException {
         MapLocation[] locs;
         int x, y, rsquare;
 
         // Clear the map by creating a new array, avoiding the need
         // to walk through it.
         rsquare = width*width+height*height;
         map_weights = new int[mapwidth][mapheight];
 
         // Place all of the mines onto the map
         locs = rc.senseNonAlliedMineLocations(center, rsquare);
         for(int i = 0; i < locs.length; i ++)
             map_weights[locs[i].x/scale][locs[i].y/scale] += GROUND_MINE;
 
         // Mark sections of the map as containing encampments by
         //  making the weights array odd
         locs = rc.senseEncampmentSquares(center, rsquare, null);
         for(int i = 0; i < locs.length; i++)
             map_weights[locs[i].x/scale][locs[i].y/scale] |= 1;
     }
 
     /** Searches for the shortest path between the start and end destinations.
      * This is a computationally intensive function that doesn't return until
      *  the computation is complete.  The returned MapLocation[] is a step-by-step
      *  path through the map, which may be approximate.
      */
     public MapLocation[] findPath(RobotController rc, MapLocation start, MapLocation end, int searchScale) throws GameActionException {
         PathNode[][] map_nodes;
         MapLocation[] retval;
         MapLocation curLoc,
                     center;
         PathNode path,
                  cur,
                  tmp;
         int cost,
             estimate,
            cx,
            cy,
             minw,
             maxw,
             minh,
             maxh;
 
         // Scale the start and end locations
         start = new MapLocation(start.x/searchScale, start.y/searchScale);
         end = new MapLocation(end.x/searchScale, end.y/searchScale);
 
         // First recalculate the weights for the map and initialize the closed set.
         //  Only search in a rectangular area around the start and end location. Search
         //  a larger area when working with a larger scale, since it takes shorter.
        cx = (end.x+start.x)/2;
        cy = (end.y+start.y)/2;
        if((maxw = Math.abs(end.x-start.x)/2) > (maxh = Math.abs(end.y-start.y)/2)) {
            minh = cy-maxw-searchScale;
            maxh = cy+maxw-searchScale;
            minw = cx-maxw-searchScale;
            maxw = cx+maxw-searchScale;
        } else {
            minw = cx-maxh-searchScale;
            maxw = cx+maxh-searchScale;
            minh = cy-maxh-searchScale;
            maxh = cy+maxh-searchScale;
        }
         if(minw < 0) minw = 0;
         if(maxw >= mapwidth/searchScale) maxw = mapwidth/searchScale-1;
         if(minh < 0) minh = 0;
         if(maxh >= mapheight/searchScale) maxh = mapheight/searchScale-1;
         center = new MapLocation((maxw+minw)/2, (maxh+minh)/2);
         recalc_weights(rc, center, maxw-minw+1, maxh-minh+1, searchScale);
         map_nodes = new PathNode[mapwidth][mapheight];
         System.out.println(" - Recalculated map weights\n");
 
         // We start with 'start' on the queue.  As long as there's a node
         //  on the queue, grab it and expand it.
         path = new PathNode(0, 0, (int)Math.sqrt(start.distanceSquaredTo(end)), start);
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
 
                 // Check if the next node is within the search bounds
                 if(curLoc.x < minw || curLoc.x > maxw ||
                    curLoc.y < minh || curLoc.y > maxh)
                     continue;
 
                 // The new node should have a path cost of 1 + cur's path cost +
                 //  the value in the weights array for this location.
                 cost = cur.pathCost+map_weights[curLoc.x][curLoc.y]+1;
                 estimate = cost+(int)Math.sqrt(curLoc.distanceSquaredTo(end));
 
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
         //  start.  Fill out retval with points corresponding to the
         //  centers of each map segment.
         //
         // TODO: ensure this doesn't return locations outside of the map bounds
         retval = new MapLocation[cur.pathLength];
         for(int i = retval.length-1; i >= 0; i--) {
             retval[i] = new MapLocation(cur.loc.x*searchScale+searchScale/2,
                                         cur.loc.y*searchScale+searchScale/2);
             cur = cur.nextpath;
             System.out.print("PATH: (" + retval[i].x + ", " + retval[i].y + ")\n");
         }
 
         // And we're done
         return retval;
     }
 
     public MapLocation[][] findEncampments(RobotController rc, int numClosest, int searchScale) throws GameActionException {
         PathNode[][] map_nodes;
         MapLocation[][] retval;
         MapLocation curLoc,
                     enemyHQ;
         int[][] map_costs;
         PathNode path,
                  cur,
                  tmp,
                  lastEncampment;
         int width,
             height,
             encamp_i,
             cost,
             estimate,
             dist,
             lastdist,
             lastRotX,
             lastRotY;
         boolean stop;
 
         // First recalculate the weights for the map and initialize map
         width = mapwidth/searchScale;
         height = mapheight/searchScale;
         enemyHQ = rc.senseEnemyHQLocation();
         enemyHQ = new MapLocation(enemyHQ.x/searchScale, enemyHQ.y/searchScale);
         recalc_weights(rc, rc.senseHQLocation(), mapwidth, mapheight, searchScale);
         map_costs = new int[mapwidth][mapheight];
         map_nodes = new PathNode[mapwidth][mapheight];
         retval = new MapLocation[3][];
         retval[0] = new MapLocation[numClosest];
 
         // Squares are visited only if the new estimate for distance is
         //  smaller than the old.  So put a dummy value for a path to
         //  the HQ.  This allows the first node to be expanded properly.
         curLoc = rc.senseHQLocation();
         path = new PathNode(0, 0, 0, new MapLocation(curLoc.x/searchScale, curLoc.y/searchScale));
         map_costs[path.loc.x][path.loc.y] = 1;
 
         // Now do a type of Dijkstra's search for encampments
         lastEncampment = null;
         lastdist = 4900;
         lastRotX = 0;
         lastRotY = 0;
         encamp_i = 0;
         stop = false;
 int count = 0;
         while(path != null) {
 count++;
             // Get the next location to expand
             cur = path;
             path = path.pop();
 
             // If this square is marked with a negative value, we've hit the
             //  opposed search.  This means we should stop searching after
             //  we find the next encampment, and stop the opposed search.
             if(map_costs[cur.loc.x][cur.loc.y] < 0)
                 stop = true;
             else if(map_costs[cur.loc.x][cur.loc.y] != 0 &&
                     map_costs[cur.loc.x][cur.loc.y] <= cur.estimateCost)
                 continue;
 
             // If this square is an encampment, we should record it's location
             if((map_weights[cur.loc.x][cur.loc.y]&1) == 1) {
                 // Only record up to numClosest locations
                 if(encamp_i < numClosest)
                     retval[0][encamp_i++] = new MapLocation(cur.loc.x*searchScale+searchScale/2,
                                                             cur.loc.y*searchScale+searchScale/2);
 
                 // If we're stopping, then 'cur' and 'lastEncampment' should
                 //  be rotationally symmetrical encampments.  These are the two
                 //  encampments to record paths for.
                 if(stop) {
                     if(Math.abs(lastRotX-cur.loc.x) <= 1 &&
                        Math.abs(lastRotY-cur.loc.y) <= 1) {
                         // Record lastEncampment (the closer encampment to us) first
                         retval[1] = new MapLocation[lastEncampment.pathLength];
                         tmp = lastEncampment;
                         for(int i = 0; i < lastEncampment.pathLength; i++, tmp = tmp.nextpath)
                             retval[1][lastEncampment.pathLength-i-1] = new MapLocation(tmp.loc.x*searchScale+searchScale/2,
                                                                                        tmp.loc.y*searchScale+searchScale/2);
 
                         // Record cur (the farther encampment to us) next
                         retval[2] = new MapLocation[cur.pathLength];
                         tmp = cur;
                         for(int i = 0; i < cur.pathLength; i++, tmp = tmp.nextpath) {
                             retval[2][cur.pathLength-i-1] = new MapLocation(tmp.loc.x*searchScale+searchScale/2,
                                                                             tmp.loc.y*searchScale+searchScale/2);
 System.out.println("\t\t(" + retval[2][cur.pathLength-i-1].x + ", " + retval[2][cur.pathLength-i-1].y + ")");
                         }
 
                         break;
                     }
                 } else {
                     // We only want to store encampments if they are closer
                     // to the enemy encampment.
                     dist = cur.loc.distanceSquaredTo(enemyHQ);
                     if(dist < lastdist) {
 System.out.println("-- found an encampment at (" + (cur.loc.x*searchScale+searchScale/2) + ", " + (cur.loc.y*searchScale+searchScale/2) + ")");
                         lastEncampment = cur;
                         lastdist = dist;
                         lastRotX = width-1-cur.loc.x;
                         lastRotY = height-1-cur.loc.y;
                     }
                 }
             }
 
             // Now mark the map and its rotationally symmetrical counterpart
             // Only mark the symmetrical part if we haven't located the
             //  stopping point yet.
             map_costs[cur.loc.x][cur.loc.y] = cur.estimateCost;
             if(!stop)
                 map_costs[width-cur.loc.x-1][height-cur.loc.y-1] = -cur.estimateCost;
 
             // Finally, add this location's neighbors to the queue
             for(int i = 0; i < DIRECTIONS.length; i++) {
                 curLoc = cur.loc.add(DIRECTIONS[i]);
 
                 // Don't expand if the new location is outside the map
                 if(curLoc.x < 0 || curLoc.x >= width || curLoc.y < 0 || curLoc.y >= height)
                     continue;
 
                 // Add this node to the queue.
                 cost = cur.pathCost+map_weights[curLoc.x][curLoc.y]+1;
                 estimate = cost;
                 if(map_costs[curLoc.x][curLoc.y] <= 0 ||
                    map_costs[curLoc.x][curLoc.y] > cost) {
                     // We want to expand this node only if it is the best
                     // path found so far.
                     if(map_nodes[curLoc.x][curLoc.y] != null) {
                         if(map_nodes[curLoc.x][curLoc.y].estimateCost <= cost)
                             continue;
                         else
                             path = path.remove(map_nodes[curLoc.x][curLoc.y]);
                     }
 
                     tmp = new PathNode(cost, cur.pathLength+1, cost, curLoc);
                     map_nodes[curLoc.x][curLoc.y] = tmp;
                     tmp.nextpath = cur;
                     if(path != null)
                         path = path.enqueue(tmp);
                     else
                         path = tmp;
                 }
             }
         }
 
         // Finally, return
 System.out.println("Expanded " + count + "  nodes before returning.");
 System.out.println("Path lengths are " + retval[1].length + " and " + retval[2].length);
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
