 package lab2;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import lab2.Elevator;
 
 
 public class Building
 {
     public static int F = 10;
 
     private List<Elevator> myElevators;
     private EventBarrier[] myUpRiders, myDownRiders;
 
 
     public Building(int E) {
         myElevators = new ArrayList<Elevator>(E);
         myUpRiders = new EventBarrier[F];
         myDownRiders = new EventBarrier[F];
         for (int i = 0; i < E; i++)
             myElevators.add(new Elevator(this));
         for (int i = 0; i < F; i++) {
             myUpRiders[i] = new EventBarrier();
             myDownRiders[i] = new EventBarrier();
         }
     }
 
     public int getFloors() {
         return F;
     }
 
     private Elevator findOpenElevator(int floor, boolean up) {
         /*
         for (Elevator elevator : myElevators) {
             if (elevator.isGoingUp() == up &&
                 elevator.currentFloor() == floor)
                 return elevator;
         }
         return null;
         */
         return myElevators.get(0);
     }
 
     public synchronized EventBarrier getUpRiders(int floor) {
         return myUpRiders[floor];
     }
 
     public synchronized EventBarrier getDownRiders(int floor) {
         return myDownRiders[floor];
     }
 
    public synchronized Elevator awaitUp(int floor) {
         myUpRiders[floor].hold();
         return findOpenElevator(floor, true);
     }
 
    public synchronized Elevator awaitDown(int floor) {
         myDownRiders[floor].hold();
        return findOpenElevator(floor, false);
     }
 
     public void startElevators() {
         for (Elevator elevator : myElevators) {
             elevator.start();
         }
     }
 
     public void stopElevators() {
         for (Elevator elevator : myElevators) {
             elevator.interrupt();
         }
     }
 }
