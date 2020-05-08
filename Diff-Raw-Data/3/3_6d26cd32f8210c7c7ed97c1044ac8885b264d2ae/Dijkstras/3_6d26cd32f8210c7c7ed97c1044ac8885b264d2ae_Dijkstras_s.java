 package RDS;
 
 import java.util.PriorityQueue;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  *The Dijkstras class is responsible for the implementation of all of the back-end
  * algorithm related business. It is called Dijkstras because that is the algorithm
  * that is being implemented. This algorithm works with any kind of routing where
  * finding a shortest path between A and B is important. Here it is used to find
  * routes between buildings on Rowan's campus but the same algorithm could be used
  * for computer networking for example.
  *
  * The reason this algorithm works for this particular problem is because since
  * it is based on a pre-calculated map based off of the actual Rowan Campus map,
  * there will be no negative weights or cycles. If this algorithm were to be ported
  * to a different problem set, Bellman-Ford or Djikstra-Scholten or a different
  * path algorithm might have to be implemented.
  *
  * @author Scott Stevenson
  */
 public class Dijkstras {
 
     private static final double AVG_WALKSPEED = 286;    //feet per minute
 
     /**
      * computeRoutes computes all the routes between buildings beginning with a given
      * current_loc building. It does so by analyzing the adjacent building and correctly
      * setting the 'previous' building as well as the 'minDistance.'
      * It also applies the weights to their corresponding edges to
      * allow for calculating a shortest route later on. This method can be thought
      * of as 'connecting-the-dots' on the given map of buildings.
      *
      * The loop that checks distances is essential to the algorithm because it
      * allows for the shortest route to nxt be checked FIRST. If that was not the
      * case a TON of efficiency would be lost. Removing and re-adding nxt insures
      * that the new minDistance is taken into account when analyzing the next round.
      *
      * @param current_loc Client's current location
      */
     public static void computeRoutes(Building current_loc) {
         //By default all buildings minDistance is infinity so we need to set
         //the current_loc building's minDistance to 0 so it will NEVER have a
         //prev set to it (if it did, it would no longer be the source).
         //***********current_loc.minDistance = 0;
         current_loc.setMinDistance(0);
 
         //PriorityQueue is a queue that automatically orders itself based on
         //the compareTo() method in the Building class (in this case minDistance)
         PriorityQueue<Building> buildingQueue = new PriorityQueue<Building>();
         buildingQueue.add(current_loc);    //Add current location to queue
 
         //While the queue isn't empty keep going
         while (!buildingQueue.isEmpty()) {
             //poll() retrieves and dequeues the head of the queue so we are grabbing
             //the building at the front of the queue to analyze (which will always
             //have the shortest minDistance due to the queue's nature). Therefore
             //we are always analyzing the shortest route first. Polling is what allows
             //this algorithm to stop. It will continue examining adjacencies and then
             //adding each adjacency to the queue until there are no more adjacencies left
             //to check.
             Building cur = buildingQueue.poll();    //cur is now the building @ beginning of queue
 
             //Now we must check all the edges on this building (cur)
             for (Edge e : cur.getAdjacencies()) {
                 Building nxt = e.getTarget();      //the building this edge points to
                 double weight = e.getWeight();     //the weight of this edge
 
                 //Now that we have an edge and that edge's weight, we need to see
                 //if this edge is the best (shortest) option in relation to nxt
                 double curToNxt = cur.getMinDistance() + weight;
                 //If curToNxt is less than nxt's current minDistance then
                 //the distance between cur and nxt is now the new minimum and
                 //the current route to get to nxt is the new 'correct' route.
                 //nxt is now a valid node to check so we must add it to the queue.
                 if (curToNxt < nxt.getMinDistance()) {
                     //Remove nxt from the queue IF it is there, if not it do nothing
                     buildingQueue.remove(nxt);
                     //nxt's minDistance will now be the distance between itself and cur
                     nxt.setMinDistance(curToNxt);
                     //nxt's previous building is now cur. cur and nxt are now fully connected.
                     nxt.setPrevious(cur);
                     //We now add nxt to the queue so we can analyze it's adjacencies
                     buildingQueue.add(nxt);
                 }
             }
         }
     }
 
     /**
      * getShortestRouteTo will find the shortest route between the current_loc building
      * in computeRoutes and the given destination parameter
      *
      * @param destination Client's desired destination
      * @return The route to destination
      */
     public static List<Building> getShortestRouteTo(Building destination) {
         List<Building> route = new ArrayList<Building>();
         //Since we already calculated all the routes, all we need to do now is
         //go backwards through each building (starting with the destination) and
         //add them to the route list at each step until we hit a null (the earlier
         //current_loc building is the ONLY building that doesn't have a 'previous'
         for (Building cur = destination; cur != null; cur = cur.getPrevious()) {
             route.add(cur);   //add current building to the route
         }
 
         //the route is currently destination -> current_loc so in order for it to make
         //sense logically to the client we need to reverse the list so it is now
         //current_loc -> destination
         Collections.reverse(route);  //reverse the route
         return route;    //return the logical route
     }
 
     /**
      * printRoute method takes in a list of buildings (assumedly a
      * current_loc -> destination route) and prints the distance between each building
      * as well as the final route and its distance
      * 
      * @param buildings The current_loc -> destination route to print
      */
     public static void printRoute(List<Building> buildings) {
         double difference = 0;
         double total_distance = 0;
        String routeInfo = "\nTo get to " + buildings.get(0) + " first go ";
 
         for (int i = 1; i < buildings.size(); i++) {
             difference = buildings.get(i - 1).getMinDistance();
             routeInfo += (buildings.get(i).getMinDistance() - difference)
                     + " feet to " + buildings.get(i) + ". \nThen go ";
         }
         total_distance = buildings.get(buildings.size() - 1).getMinDistance();
         routeInfo = routeInfo.substring(0, routeInfo.lastIndexOf(". \nThen go "));
         routeInfo += ". \nYou will now arrive at your destination."
                 + "\nThis trip is a total of " + total_distance
                 + " feet and will take you approximately "
                 + truncateDouble(total_distance / AVG_WALKSPEED, 2)
                 + " minutes to walk.";
         System.out.println(routeInfo);
     }
 
     private static double truncateDouble(double number, int numDigits) {
         String arg = "" + number;
         int i = arg.indexOf('.');
         if (i != -1) {
             if (arg.length() > i + numDigits) {
                 arg = arg.substring(0, i + numDigits + 1);
                 return Double.parseDouble(arg);
             }
         }
         return number;
     }
 }
