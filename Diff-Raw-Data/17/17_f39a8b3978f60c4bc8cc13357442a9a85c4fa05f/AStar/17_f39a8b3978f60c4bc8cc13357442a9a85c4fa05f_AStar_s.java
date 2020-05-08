 package Algorithm;
 
 public class AStar {
 
     private LinkedList open, closed;
     private Node start, goal;
 
     public AStar(LinkedList open, LinkedList closed) {
         this.open = open;
         this.closed = closed;
     }
 
     public AStar(LinkedList open) {
         this.open = open;
         this.closed = null;
     }
 
     protected LinkedList constructPath(Node node) {
         LinkedList path = new LinkedList();
 
        while (node.getParent() != null) {
             path.addFirst(node);
             node = node.getParent();
         }
         return path;
     }
 
     public void reset() {
         resetNodes();
     }
 
     protected void resetNodes() {
         if (goal != null) {
             goal.reset();
             System.out.println("Reset Goal");
         }
         if (start != null) {
             start.reset();
             System.out.println("Reset Start");
         }
         if (open != null) {
             System.out.println("Reset OPEN");
             open.clear();
         }
         if (closed != null) {
             System.out.println("Reset CLOSED");
             closed.clear();
         }
     }
 
     public LinkedList findPath(Node start, Node goalNode) {
         this.start = start;
        this.goal = new Node(new Cell(goalNode.data.getX(),goalNode.data.getY()));
         start.setParent(null);
         start.estimatedCostToGoal = start.getEstimatedCostTo(goal);
         start.costFromStart = 0;
         open.add(start);
         while (!open.isEmpty()) {
             Node node = open.removeFirst();
             node.setVisited(true);
 
             if (node.compareTo(goal) == 0) {
                 return constructPath(node);
             } else {
                 for (int i = 0; i < node.getNeighbours().length; i++) {
                     Node neighbour = node.getNeighbours()[i];
                     if (neighbour != null && !neighbour.isBlocked()) {
                         int costFromStart = node.costFromStart + node.getCost();
                         boolean inClosed = closed.contains(neighbour);
                         boolean inOpen = open.contains(neighbour);
                         if ((!inOpen && !inClosed) || costFromStart < neighbour.costFromStart) {
                             if (!neighbour.visited) {
                                 neighbour.setParent(node);
                             }
                             neighbour.costFromStart = costFromStart;
                             neighbour.estimatedCostToGoal = neighbour.getEstimatedCostTo(goalNode);
                             if (inClosed) {
                                 closed.remove(neighbour);
                             }
                             if (!inOpen) {
                                 open.add(neighbour);
                             }
                         }
                     }
                 }
                 closed.add(node);
             }
         }
         return null;
     }

 //    protected LinkedList startSearch(Node start, Node goal) {
 //        start.setParent(null);
 //        start.estimatedCostToGoal = start.getEstimatedCostTo(goal);
 //        start.costFromStart = 0;
 //        open.add(start);
 //        while (!open.isEmpty()) {
 //            Node node = open.removeFirst();
 //            node.setVisited(true);
 //
 //            if (node.compareTo(goal) == 0) {
 //                return constructPath(node);
 //            } else {
 //                for (int i = 0; i < node.getNeighbours().length; i++) {
 //                    Node neighbour = node.getNeighbours()[i];
 //                    if (neighbour != null && !neighbour.isBlocked()) {
 //                        int costFromStart = node.costFromStart + node.getCost();
 //                        boolean inClosed = closed.contains(neighbour);
 //                        boolean inOpen = open.contains(neighbour);
 //                        if ((!inOpen && !inClosed) || costFromStart < neighbour.costFromStart) {
 //                            if (!neighbour.visited) {
 //                                neighbour.setParent(node);
 //                            }
 //                            neighbour.costFromStart = costFromStart;
 //                            neighbour.estimatedCostToGoal = neighbour.getEstimatedCostTo(goal);
 //                            if (inClosed) {
 //                                closed.remove(neighbour);
 //                            }
 //                            if (!inOpen) {
 //                                open.add(neighbour);
 //                            }
 //                        }
 //                    }
 //                }
 //                closed.add(node);
 //            }
 //        }
 //        return null;
 //    }
 }
