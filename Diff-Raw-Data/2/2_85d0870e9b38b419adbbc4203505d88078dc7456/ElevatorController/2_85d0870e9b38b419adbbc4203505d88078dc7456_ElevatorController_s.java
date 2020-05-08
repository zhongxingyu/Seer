 package com.se350.app;
 
 import java.util.ArrayList;
 
 /**
  * ElevatorController class, it uses Singleton pattern
  * @author Valentin Litvak, Michael Williamson
  */
 public class ElevatorController {
 
     private int numFloors;
     private ArrayList<Elevator> elevators;
     private int desiredElevatorNumber;
 
     public ElevatorController( int numElevatorsIn, int numFloorsIn ) throws IllegalArgumentException {
         if ( numFloorsIn <= 0 || numElevatorsIn <= 0 )
             throw new IllegalArgumentException();
         elevators = new ArrayList<Elevator>();
         int elevatorNumber = 1;
         for ( int i = elevatorNumber; i < numElevatorsIn + 1; i++ )
             elevators.add( new Elevator( i ) );
         setNumFloors( numFloorsIn );
     }
 
     public int getNumElevators() {
         return elevators.size();
     }
 
     /**
      * @return number of floors available in the building.
      */
     private int getNumFloors() {
     	return numFloors;
     }
 
     /**
      * sets numFloors to the parameter numFloorsIn.
      */
     private void setNumFloors( int numFloorsIn ) {
     	numFloors = numFloorsIn;
     }
     
     /**
      * Accepts a request from a person (for now App.java) and stores it in pendingRequests.
      */
     public void addFloorRequest( int requestedFloorNum, int elevatorNumber ) {
        if ( requestedFloorNum > 0 || requestedFloorNum <= numFloors && elevatorNumber > 0 ) {
             sendRequestToElevator( elevatorNumber, requestedFloorNum );
         } else {
             System.out.println( "We're are sorry, but you have requested an invalid floor or an invalid elevator." );
         }
     }
 
     /**
      * sends a request to the specified elevator(elevatorIndex) to the specified floor(requestedFloorNum).
      * @param elevatorIndex The index of the requested elevator within the elevators ArrayList.
      * @param requestedFloorNum The number of the destination floor.
      */
     private void sendRequestToElevator( int elevatorNumber, int requestedFloorNum ) {
         System.out.println( "elevators.size(): " + elevators.size() );
         System.out.println( "The elevator number is " + elevatorNumber );
         int elevatorIndex = elevatorNumber - 1;
         System.out.println( "The elevator index is " + elevatorIndex );
         elevators.get( elevatorIndex ).addDestination( requestedFloorNum );
     }
 
     /**
      * checks to see whether there are any pending destinations that were added to an elevator.
      */
     public int getNumRequests() {
         ArrayList<Elevator> allElevators = getElevators();
         int totalDestinationsCount = 0;
         for( Elevator elevator : allElevators ) {
             totalDestinationsCount += elevator.getNumPendingDestinations();
         }
         return totalDestinationsCount;
     }
 
     /**
      * @return the list of elevators that pertain to this ElevatorController.
      */
     private ArrayList<Elevator> getElevators() {
         return elevators; 
     }
 }
