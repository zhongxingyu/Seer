 package oneway.g4;
 import oneway.sim.MovingCar;
 import oneway.sim.Parking;
 
 import java.util.*;
 
 /*
  * A class representing one state of the game - meaning some combination of
  * car positions and light settings. This class should handle interfacing between
  * oneway.g4 package representation of the states, and oneway.sim representation
  * of game states.
 */
 
 public class Node implements Comparable<Node> {
   private Segment[] segments;
   private ParkingLot[] lots;
   public ArrayList<Car> allCars;
   public Node parent = null;
   private int currentTime;
   private int m = 0;
   public int carsFinished = 0;
 
   // Initialize a Node from Simulator information
   public Node(int time, int nSegments, int[] lengths, MovingCar[] movingCars,
     Parking[] left, Parking[] right, int[] capacities, boolean[] llights,
     boolean[] rlights){
 
     currentTime = time;
 
     // Initialize the allCars array
     allCars = new ArrayList<Car>();
 
     // Create proper number of parking lots and fill with cars
     lots = new ParkingLot[nSegments+1];
     for (int i = 0; i <= nSegments; i++) {
       lots[i] = new ParkingLot(capacities[i]);
       addCarsToParkingLot(left[i], lots[i], Direction.LEFT);
       addCarsToParkingLot(right[i], lots[i], Direction.RIGHT);
     }
 
     // Create proper number of segments
     segments = new Segment[nSegments];
     for (int i = 0; i < nSegments; i++) {
       segments[i] = new Segment(lengths[i], llights[i], rlights[i]);
       m += lengths[i];
     }
     // Decrement m by 1 because if a car is born facing a green light it starts
     // on that road segment instead of in the parking lot.
     m--;
 
     // Place the movingCars on the segments
     for(MovingCar movingCar : movingCars) {
       Direction dir = movingCar.dir > 0 ? Direction.RIGHT : Direction.LEFT;
       Segment segment = segments[movingCar.segment];
       Car car = new Car(movingCar.startTime, dir);
 
       allCars.add(car);
       segment.addCarAtPosition(car, movingCar.block);
     }
   }
 
   private Node(Node node) {
     currentTime = node.currentTime;
     m = node.m;
     allCars = new ArrayList<Car>();
 
     // Copy the parking lots and add their cars the arrays
     lots = new ParkingLot[node.lots.length];
     for (int i = 0; i < lots.length; i++) {
       lots[i] = node.lots[i].copy();
       List<Car> cars = lots[i].getCars();
       for (Car c : cars) {
         allCars.add(c);
       }
     }
 
     segments = new Segment[node.segments.length];
     for (int i = 0; i < segments.length; i++) {
       segments[i] = node.segments[i].copy();
       List<Car> cars = segments[i].getCars();
       for (Car c : cars) {
         allCars.add(c);
       }
     }
 
     for (Car c : node.allCars) {
       if (c.isComplete()) {
         allCars.add(c);
       }
     }
   }
 
   private void addCarsToParkingLot(List<Integer> cars, ParkingLot lot, Direction dir) {
     if (cars == null) { return; }
     for (Integer carStartTime : cars) {
       Car car = new Car(carStartTime, dir);
       allCars.add(car);
       lot.add(car);
     }
   }
   public ArrayList<Node> successors() {
     ArrayList<Node> children = new ArrayList<Node>();
 
     // max is the maximum number of light permutations
     boolean keepEndsRed = false;
     boolean carsExitingLeft  = segments[0].anyCarsInDir(Direction.LEFT);
     boolean carsExitingRight = segments[segments.length-1].anyCarsInDir(Direction.RIGHT);
     int numLights = segments.length * 2;
 
     int start = 0;
     int end = numLights - 1;
 
     if (carsExitingLeft){
       numLights--;
       start++;
     }
     if (carsExitingRight){
       numLights--;
       end--;
     }
 
     int max = (int) Math.pow(2, numLights);
 
     for(int i = 0; i < max; i++) {
       // Use a bit vector to find different permutations of lights
       int binaryLightRepresentation = i;
       boolean[] lights = new boolean[segments.length * 2];
       int firstLight = 0;
       int lastLight  = lights.length - 1;
 
       if (carsExitingLeft) firstLight++;
       if (carsExitingRight) lastLight--;
 
       for (int j = firstLight; j <= lastLight; j++) {
         lights[j] = binaryLightRepresentation % 2 == 0;
         binaryLightRepresentation = binaryLightRepresentation >> 1;
       }
 
       //Create the child, test it out, and keep it if its good
       Node child = new Node(this);
       child.setLights(lights);
       child.parent = this;
       if(child.playTurn() == false
           && child.noFutureCrashes()
           && child.noFutureOverflows()) {
         child.parent = this;
         children.add(child);
       }
     }
     return children;
   }
 
   private void setLights(boolean[] lights) {
     int nSegments = segments.length;
     for(int i = 0; i < nSegments; i++) {
       segments[i].setLight(Direction.LEFT, lights[i]);
       segments[i].setLight(Direction.RIGHT, lights[i+nSegments]);
     }
   }
 
   // return false if overflows are guaranteed in the future
   private boolean noFutureOverflows() {
 
     for(int i = 1; i<lots.length-1; i++)
     {
       int lcount = 0,rcount = 0;
       Car[] cl = new Car[segments[i-1].getLength()];
       Car[] cr = new Car[segments[i].getLength()];
 
       cl = segments[i-1].getCarsByLocation();
       cr = segments[i].getCarsByLocation();
 
       for(int j =0; j < cl.length; j++) {
         if(cl[j]!=null && cl[j].isRightbound()) {
           lcount++;
         }
       }
 
       for(int k =0; k < cr.length; k++) {
         if(cr[k]!=null && cr[k].isLeftbound()) {
           rcount++;
         }
       }
 
       if(lots[i].getLeftCarCount() + lots[i].getRightCarCount() + lcount + rcount > lots[i].getCapacity()) {
         return false;
       }
     }    
 
     return true;
   }
 
   // return false if crashes are guaranteed in the future
   private boolean noFutureCrashes() {
     for(int i = 0;i < segments.length; i++) {
       boolean left = false, right = false;
       Car[] c = new Car[segments[i].getLength()];
       c = segments[i].getCarsByLocation();
 
       if(segments[i].isLeftGreen() && segments[i].isRightGreen()) {
         return false;
       }
       for(int j =0; j < c.length; j++) {
         if(c[j] != null && c[j].isRightbound()) {
             right = true;
         }
       }
       for(int k =0; k < c.length; k++) {
         if(c[k] != null && c[k].isLeftbound()) { 
             left = true;
         }
       }
 
 
       if(right && left)
         return false;
 
     }
     return true;
 
   }
 
   /**
    * Plays out a single turn on the current node.
    * @return Whether or not the result of playing the turn results in overflow or crash
    */
   private boolean playTurn() {
     currentTime += 1;
    for (Car c : allCars) {
      c.updateLatency(currentTime);
    }

     boolean fail = false;
     for (int i = 0; i < segments.length; i++) {
       fail = fail || segments[i].moveCarsForward(lots[i], lots[i+1]);
     }
     for (int i = 0; i < lots.length; i++) {
       Segment leftSegment = i > 0 ? segments[i-1] : null;
       Segment rightSegment = i < segments.length ? segments[i] : null;
       fail = fail || lots[i].unparkCars(leftSegment, rightSegment);
     }
     lots[0].removeCars(Direction.LEFT, currentTime, this);
     lots[lots.length-1].removeCars(Direction.RIGHT, currentTime, this);
     return fail;
   }
 
   public void printNode() {
     System.out.println("{F: " + f() + "}");
     for(int i = 0; i < lots.length; i++) {
       System.out.println("{lots[" + i +"]:\n\tLeftbound: " + lots[i].getLeftCarCount() + "\n\tRightbound: " + lots[i].getRightCarCount() + "}");
       if (i == lots.length - 1) { break; }
       System.out.println("{segments[" + i + "]:\n\t" + Arrays.toString(segments[i].getCarsByLocation()) + "}");
     }
   }
 
   public double f() {
    double g = g();
    double h = h();
    System.out.println("g: " + g + ", h: " + h);
    return g + h;
   }
 
   // Path cost until this point
   public double g() {
     double cost = 0.0;
     // Sum cost of each car
     for (Car car : allCars) {
      int latency = car.getLatency();
      System.out.println(latency);
      cost += cost(latency);
     }
     return cost;
   }
 
   // Estimate cost from here to the end
   // Maybe use a feature vector of different heuristics and later train
   // to get optimal weights
   private double h() {
     double totalCost = 0;
     int totalDistance = m + 1;
     int partDistance = 0;
 
     for (int i = 0; i < lots.length; i++) {
       // Calculate the expected cost of the segment
       // Skip segment calculation if at index 0.
      if (i != 0) {
         Segment s = segments[i-1];
         Car[] cars = s.getCarsByLocation();
         for (int segDistance = 0; segDistance < cars.length; segDistance++) {
           if (cars[segDistance] != null) {
             int expectedFinish;
             if (cars[segDistance].dir == Direction.LEFT) {
               expectedFinish = currentTime + segDistance + partDistance;
             }
             else {
               expectedFinish = currentTime + (totalDistance - partDistance - (segDistance + 1));
             }
             totalCost += cost(expectedFinish - cars[segDistance].startTime);
           }
         }
         
         partDistance += s.getLength();
       }
       
       // Calculate the expected cost of the lot
       ParkingLot l = lots[i];
       for (Car c : l.getCars()) {
         int expectedFinish;
         if (c.dir == Direction.LEFT) {
           expectedFinish = currentTime + partDistance;
         }
         else {
           expectedFinish = currentTime + (totalDistance - partDistance);
         }
         totalCost += cost(expectedFinish - c.startTime);
       }
     }
 
     return totalCost;
   }
 
   private double cost(int latency) {
     return (latency * Math.log10(latency)) - (((double) m) * Math.log10(m));
   }
 
   @Override
   public int compareTo(Node other) {
     return (int) Math.signum(this.f() - other.f());
   }
 
   public boolean[] getLLights() {
     boolean[] llights = new boolean[segments.length];
     for(int i = 0; i < llights.length; i++) {
       llights[i] = segments[i].isLeftGreen();
     }
     return llights;
   }
 
   public boolean[] getRLights() {
     boolean[] rlights = new boolean[segments.length];
     for(int i = 0; i < rlights.length; i++) {
       rlights[i] = segments[i].isRightGreen();
     }
     return rlights;
   }
 }
