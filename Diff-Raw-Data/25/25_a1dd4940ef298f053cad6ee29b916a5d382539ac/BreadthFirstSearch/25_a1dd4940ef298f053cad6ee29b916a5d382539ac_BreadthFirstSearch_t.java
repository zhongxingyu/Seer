 package com.cafeform.algorithm;
 
 import java.util.*;
 import java.util.concurrent.LinkedBlockingDeque;
 
 /**
 * Implementation of breadth first search algorithm.
 * This may not be good implementation.
 * 
  * @author kaizawa
  */
 class BreadthFirstSearch extends SearchAlgorithm {
    
    LinkedBlockingDeque<LinkedList> foundNodeQueue = new LinkedBlockingDeque<>();
     Set<Node> foundNodeSet = new HashSet<>(); // Found, but has not children search 
     List<Node> shortestPath = Collections.emptyList();
 
     public BreadthFirstSearch(List<Node> nodeList, Node startNode, Node goalNode) {
         super(nodeList, startNode, goalNode);
     }
 
     @Override
     public SearchResult doSearch() {
         // List of Node which represents path.
        LinkedList<Node> currentPath = new LinkedList<>();
         currentPath.add(startNode);
         foundNodeQueue.add(currentPath);
 
         while(false == foundNodeQueue.isEmpty()){
            currentPath = foundNodeQueue.poll();            
            Node currentNode = currentPath.getLast();
 
             if (currentNode == goalNode) {
                 shortestPath = currentPath;
                 break;
             } else {
                 for (Node child : (Set<Node>) currentNode.getChildrenMap().keySet()) {
                    time.incrementAndGet();
                     //Check if it has already been found
                     if (foundNodeSet.contains(child)) {
                         // This node has ever been found.
                         continue;
                     }
                    LinkedList childPath = (LinkedList) currentPath.clone();
                     childPath.add(child);
 
                     // Put child path to the gray queue.
                     foundNodeQueue.add(childPath);
                     foundNodeSet.add(child);
                 }
             }
         } 
         // Return path which found first        
        return new SearchResult(shortestPath, "BreadthFirstSearch", time.get());
     }    
 }
