 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package search;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 import pathfinding.BoardAction;
 import board.Board;
 
 
 /**
  * An A* implementation. Some assumptions are made:
  * The expand function of T should be such that the path cost of each successor is
  * initialised with the cost of the parent plus the cost to get from the parent
  * to the successor
  * @author michal
  * @param <T> A type which has some function for generating successors
  * @param <U> A type (usually an enum) which specifies the actions that can be
  * applied to the T type
  */
 public class AStar extends SearchMethod {
 
     Heuristic<Board> h;
     
     /**
      * Basic constructor
      * @param h The heuristic to use for state evaluation
      */
     public AStar(Heuristic<Board> h){
         this.h = h;
     }
     
     @Override
     public ArrayList<BoardAction> findPath(Board start, Board goal, boolean boardSpace) {
         // Store already visited nodes
         HashSet<SearchNode> closed = new HashSet<>();
         // Store as yet unvisited nodes in a priority queue - we expand from the best
         Queue<SearchNode> open = new PriorityQueue<>();
         
         // Add the start state as a node with zero path cost
         open.add(new SearchNode(start, null, null, 0, (int) h.utility(start, goal), boardSpace));
         SearchNode goalNode = new SearchNode(goal, null, null, boardSpace);
         while(!open.isEmpty()){
 //            System.out.println("open size: " + open.size() + " closed size: " + closed.size() + " discarded locks: " +Board.lockedStatesIgnored);
 //            System.out.println("OPEN LIST =============");
 //            for (SearchNode searchNode : open) {
 //                System.out.println(searchNode);
 //            }
 //            System.out.println("CLOSED LIST ===========");
 //            for (SearchNode searchNode : closed) {
 //                System.out.println(searchNode);
 //            }
 //            System.out.println("OPEN LIST COSTS ============");
 //            for (SearchNode searchNode : open) {
 //                System.out.print(searchNode.estimatedCost + " ");
 //            }
 //            System.out.println("");
             SearchNode front = open.remove(); // The best node in the queue
 //            System.out.println("Checking if closed contains the front node");
 //            System.out.println(front);
             // If front is the goal, return the action sequence.
 //            System.out.println("Checking goal state");
             if (front.equals(goalNode)){
 //                System.out.println("Found the goal!");
 //                System.out.println("Front:");
 //                System.out.println(front);
 //                System.out.println("Goal:");
 //                System.out.println(goalNode);
                 return front.actionUnwind();
             }
             
 
             // Add the parent to the closed list - we do not need to expand it more than once
             if (!closed.contains(front)){ // #TODO Is this check really necessary?
 //                System.out.println("closed does not contain the front node. adding.");
                 closed.add(front);
             }
             
             ArrayList<SearchNode> successors = front.expand();
 //            System.out.println("Number of successor states: " + successors.size());
             for (SearchNode successor : successors) {
 //                System.out.println("Examining successor of front node");
 //                System.out.println(successor);
                 // Look through the open list to see if the successor is
                 // already present
                 Iterator<SearchNode> it = open.iterator();
 //                System.out.println("Checking open list to see if it contains the successor.");
                 boolean inOpen = false;
                 while(it.hasNext()){
                     SearchNode element = it.next();
 //                    try {
 //                        Thread.sleep(1000);
 //                    } catch (InterruptedException ex) {
 //                        
 //                    }
                     if (element.equals(successor)){
                         if (element.pathCost > successor.pathCost){
                             // If the current state is in the open list and the path cost to that
                             // state node is greater than that of the path cost to this node,
                             // remove it from the open list - the path via this node is better
                             //System.out.println("open list already contained successor, but this path is better.");
                             it.remove();
                             break;
                         }
                         // we set this if the successor is in the open list, but does
                         // not have a better path cost so that we don't have to check again.
                         // do this after the above check instead of before so that we don't
                         // say that it's present and then remove it.
                         inOpen = true;
                     }
                 }
 //                System.out.println("Finished checking open list - successor in open? " + inOpen);
                 // If neither of the open or closed lists contains the successor, then
                 // we compute the estimated cost to get to the goal from this node state,
                 // add it to the path cost and then add the node to the open list
 //                System.out.println("Checking if the successor is in the closed list");
                 boolean inClosed = closed.contains(successor);
 //                System.out.println("successor in closed? " + inClosed);
                 if (!inOpen && !inClosed){
 //                    System.out.println("successor path cost " + successor.pathCost + " successor utility " + h.utility(successor.nodeState, goal));
//                    System.out.println("Successor not in either list - Adding the successor to the open list");
                     successor.estimatedCost = successor.pathCost + (int) h.utility(successor.nodeState, goal);
 //                    System.out.println("successor estimated cost: " + successor.estimatedCost);
                     open.add(successor);
                 }
             }
         }
         
         // Went through all nodes without finding the goal - there is no path.
         return null;
     }
 
 }
