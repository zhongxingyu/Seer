 package oneway.g4;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import oneway.sim.MovingCar;
 import oneway.sim.Parking;
 
 public class Player extends oneway.sim.Player
 {
     private int currentTime = -1;
 
     public Player() {}
 
     public void init(int nsegments, int[] nblocks, int[] capacity)
     {
       this.nsegments = nsegments;
       this.nblocks = nblocks;
       this.capacity = capacity.clone();
     }
 
 
     public void setLights(MovingCar[] movingCars,
                           Parking[] left,
                           Parking[] right,
                           boolean[] llights,
                           boolean[] rlights)
     {
       currentTime++;
       Node node = new Node(currentTime, nsegments, nblocks, movingCars, 
           left, right, capacity, llights, rlights);
 
 //      List<Node> children = node.successors();
 //      Collections.sort(children);
 //      if (children.size() == 0) return;
 //      Node choice = children.get(0);
 
       // Strategy 0: On the first turn, just let cars come in since search will
       // instantly terminate
       boolean moreThanOneRoad = left.length > 2;
      if (moreThanOneRoad && movingCars.length == 0 && !anyParkedCars(left, right)){
         System.out.println("First turn with 2+ segments, so lettings cars in.");
         llights[llights.length-1] = true;
         rlights[0] = true;
         return;
       }
 
       Node choice = new Searcher().best(node);
 
       boolean[] newLLights = choice.getLLights();
       boolean[] newRLights = choice.getRLights();
       for(int i = 0; i < nsegments; i++) {
         llights[i] = newLLights[i];
         rlights[i] = newRLights[i];
       }
     }
 
     private boolean anyParkedCars(Parking[] left, Parking[] right){
       for (int i = 0; i < left.length; i++){
         if (left[i] != null && left[i].size() != 0) return true;
         if (right[i] != null && right[i].size() != 0) return true;
       }
       return false;
     }
 
 
     private int nsegments;
     private int[] nblocks;
     private int[] capacity;
 }
