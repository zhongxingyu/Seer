 package lab2;
 
 import java.util.List;
 
 import lab2.EventBarrier;
 import lab2.Building;
 
 
 public class Elevator extends Thread{
 	private int myCurrentFloor;
 	private EventBarrier[] myFloorBarriers;
 	private Building myBuilding;
 	private boolean up=true;
 
 	public Elevator(Building b){
 		myBuilding = b;
         myCurrentFloor = 0;
 		myFloorBarriers=new EventBarrier[myBuilding.getFloors()];
 		for(int i=0; i<myBuilding.getFloors(); i++){
 			myFloorBarriers[i]=new EventBarrier();
 		}
 	}
 
     private synchronized void visitFloor(int floor){
 		myCurrentFloor = floor;
         System.out.println("Visiting floor: " + floor);
         if(up){
             myBuilding.getUpRiders(floor).signal();
         }
         else{
             myBuilding.getDownRiders(floor).signal();
         }
         myFloorBarriers[floor].signal();
 	}
 
 	public boolean isGoingUp(){
 		return up;
 	}
 
     public int currentFloor() {
         return myCurrentFloor;
     }
 
 	public void enter() {
 		if (up) {
 			myBuilding.getUpRiders(myCurrentFloor).complete();
 		}
 		else{
 			myBuilding.getDownRiders(myCurrentFloor).complete();
 		}
 	}
 
     public void requestFloor(int floor) {
         myFloorBarriers[floor].hold();
     }
 
 	public void exit() {
 		myFloorBarriers[myCurrentFloor].complete();
 	}
 
     private int nextFloor() {
         if (up) {
             int next = nextHigherFloor();
             if (next != -1)
                 return next;
             up = false;
             return nextLowerFloor();
         }
         else {
             int next = nextLowerFloor();
             if (next != -1)
                 return next;
             up = true;
             return nextHigherFloor();
         }
     }
 
     private int nextHigherFloor() {
         for (int i = myCurrentFloor + 1; i < myBuilding.getFloors(); i++) {
        	System.out.println("FLOOR " + i);
        	System.out.println(myFloorBarriers[i].waiters());
        	System.out.println(myBuilding.getUpRiders(i).waiters());
        	System.out.println(myBuilding.getDownRiders(i).waiters());
        	if (myFloorBarriers[i].waiters() > 0 ||
                 myBuilding.getUpRiders(i).waiters() > 0 ||
                 myBuilding.getDownRiders(i).waiters() > 0) {
                 return i;
             }
         }
         return -1;
     }
 
     private int nextLowerFloor() {
         for (int i = myCurrentFloor - 1; i >= 0; i--) {
        	System.out.println("FLOOR " + i);
        	System.out.println(myFloorBarriers[i].waiters());
        	System.out.println(myBuilding.getUpRiders(i).waiters());
        	System.out.println(myBuilding.getDownRiders(i).waiters());
             if (myFloorBarriers[i].waiters() > 0 ||
                 myBuilding.getUpRiders(i).waiters() > 0 ||
                 myBuilding.getDownRiders(i).waiters() > 0) {
                 return i;
             }
         }
         return -1;
     }
 
 	@Override
 	public void run() {
         while (true) {
             int next = nextFloor();
             if (next != -1)
                 visitFloor(next);
             if (interrupted())
                 return;
         }
 	}
 }
