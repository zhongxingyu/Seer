 package nachos.threads;
 
 import nachos.ag.BoatGrader;
 
 public class Boat {
 	static BoatGrader bg;
 	static int numChildrenOnOahu;
 	static int numAdultsOnOahu;
 	static int numChildrenOnMolokai;
 	static int numAdultsOnMolokai;
 	static int boatLocation;
 	static int numPassangers;
 	static Lock pilotLock;
 	static Lock gameEnd;
 	static Condition readyAtOahu;
 	static Condition readyAtMolokai;
 	static Condition passangerChildNeeded;
 	static Condition end;
 	static Communicator communicator;
 	static boolean hasChildPilot;
 
 	public static void selfTest() {
 		BoatGrader b = new BoatGrader();
 
 		System.out.println("\n ***Testing Boats with only 2 children***");
 		begin(0, 2, b);
 
		System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		begin(1, 2, b);
 
		//System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//begin(3, 3, b);
 	}
 
 	public static void begin(int adults, int children, BoatGrader b) {
 		// Store the externally generated autograder in a class
 		// variable to be accessible by children.
 		bg = b;
 
 		// Instantiate global variables here
 		boatLocation = 0;
 		numChildrenOnOahu = 0;
 		numChildrenOnMolokai = 0;
 		numAdultsOnOahu = 0;
 		numAdultsOnMolokai = 0;
 		numPassangers = 0;
 		hasChildPilot = false;
 		//boolean gameOver = false;
 		pilotLock = new Lock();
 		readyAtOahu = new Condition(pilotLock);
 		readyAtMolokai = new Condition(pilotLock);
 		passangerChildNeeded = new Condition(pilotLock);
 		gameEnd = new Lock();
 		end = new Condition(gameEnd);
 
 
 		// Create threads here. See section 3.4 of the Nachos for Java
 		// Walkthrough linked from the projects page.
 		for (int i = 0; i < children; i++) {
 			KThread child = new KThread(new Runnable() {
 				public void run() {
 					ChildItinerary();
 				}
 			});
 			child.setName("Child");
 			child.fork();
 		}
 
 		for (int i = 0; i < adults; i++) {
 			KThread adult = new KThread(new Runnable() {
 				public void run() {
 					AdultItinerary();
 				}
 			});
 			adult.setName("Adult");
 			adult.fork();
 		}
 
 		while (true) {
 			//numAdultsOnMolokai = communicator.listen();
 			//numChildrenOnMolokai = communicator.listen();
 			/*if (adults == numAdultsOnMolokai && children == numChildrenOnMolokai) {
 				return;
 			}*/
 			if ((numChildrenOnMolokai + numAdultsOnMolokai) == (adults + children)){
 				System.out.println("Game over. FUCK YEAH!");
 				break;
 			}
 			else {
 				System.out.println("Game is not over yet.");
 				pilotLock.acquire();
 				readyAtMolokai.wake();
 				pilotLock.release();
 				gameEnd.acquire();
 				end.sleep();
 
 			}
 		}
 
 	}
 
 	static void printState() {
 		System.out.println("Oahu:[" + numAdultsOnOahu + ","
 				+ numChildrenOnOahu + "]" + " Molokai:[" + numAdultsOnMolokai + ","
 				+ numChildrenOnMolokai + "]");
 	}
 
 	static void AdultItinerary()
 	{
 		/* This is where you should put your solutions. Make calls
 	   to the BoatGrader to show that it is synchronized. For
 	   example:
 	       bg.AdultRowToMolokai();
 	   indicates that an adult has rowed the boat across to Molokai
 		 */
 		pilotLock.acquire(); //do you need this lock?
 		int currentLocation = 0; //current location set to Oahu (should we set it?)
 		numAdultsOnOahu++;
 		printState();
 		pilotLock.release(); 
 
 		pilotLock.acquire();
 		while (true) {
			System.out.println("______In AdultItinerary______");
 			if (boatLocation == 1) { // if the boat is at Molokai, no adult can cross to Molokai
				System.out.println("______Boat is at MOLOKAI______");
 				return;
 			}
 			if (boatLocation !=  currentLocation){ //if boat is not at the current location
 				readyAtOahu.sleep();
 			}
 			if (numChildrenOnOahu >= 2) { //if Oahu has >= 2 children, dont send adults (you will need to send children)
 				readyAtOahu.sleep();
 			}
 			if (hasChildPilot){ //adult cannot be on a boat with a child
 				readyAtOahu.sleep();
 			}
 			else { //row over to Molokai and update all values
 				bg.AdultRowToMolokai();
 				numAdultsOnOahu--;
 				numAdultsOnMolokai++;
 				boatLocation = 1;
 				if (numChildrenOnMolokai == 0) { // if there are no children to row the boat back to Oahu
 					bg.AdultRowToOahu();
 					numAdultsOnOahu++;
 					numAdultsOnMolokai--;
 					boatLocation = 0;
 					readyAtOahu.sleep();
 				}
 				else {
 					readyAtMolokai.wake();
 					pilotLock.release();
 					break;
 				}
 			}
 		}		
 		pilotLock.release(); 
 		return;
 	}
 
 	static void ChildItinerary() {
 		Boolean readyToTerminate = false;
 		pilotLock.acquire();
 		int currentLocation = 0; //current location set to Oahu
 		numChildrenOnOahu++;
 		printState();
 		pilotLock.release(); 
 
 		pilotLock.acquire();
 		while (true) {
			System.out.println("______In ChildItinerary______");
 			if (currentLocation == 0) { //in Oahu
 				if (boatLocation == 0){
 					if (numChildrenOnOahu == 1 && !hasChildPilot) {
						//readyAtOahu.wake();
						//hasChildPilot = true;
 						readyAtOahu.sleep();
 					}
 
 					else if (hasChildPilot && numPassangers == 0){ //one child on the boat
 						if (numChildrenOnOahu == 2 && numAdultsOnOahu == 0) {  //last 2 children are ready to leave
 							readyToTerminate = true;
 						}
 						//passangerChildNeeded.wake();
 						numPassangers++;
 						//passangerChildNeeded.sleep();
 						numChildrenOnOahu--;
 						numChildrenOnOahu--;
 						bg.ChildRowToMolokai();
 						numChildrenOnMolokai++;
 						numChildrenOnMolokai++;
 						currentLocation = 1;
 						boatLocation = 1;
 						hasChildPilot = false;
 						printState();
 						if (readyToTerminate) {
 							//communicator.speak(numAdultsOnMolokai);
 							//communicator.speak(numChildrenOnMolokai);
 							gameEnd.acquire();
 							end.wake();
 							gameEnd.release();
 						}
 						
 						else {
 							readyAtMolokai.wake();
 						}
 						readyAtMolokai.sleep();
 					}
 					else if (!hasChildPilot) {
 						hasChildPilot = true; //but a child on the boat to be pilot
 						readyAtOahu.wake();
 						passangerChildNeeded.sleep();
 						numChildrenOnOahu--;
 						bg.ChildRowToMolokai();
 						currentLocation = 1;
 						passangerChildNeeded.wake();
 						boatLocation = 1;
 						readyAtOahu.sleep();
 					}
 					else {
 						readyAtOahu.sleep();
 					}
 				}
 			else { //if current location is at Molokai, send a child as the pilot
 					if (boatLocation == 1) {
 						boatLocation = 0;
 						currentLocation = 0;
 						numChildrenOnMolokai--;
 						bg.ChildRowToOahu();
 						numChildrenOnOahu++;
 						readyAtOahu.wake();
 						readyAtOahu.sleep();
 					} 
 					else {
						readyAtMolokai.sleep();
 					}
 				}
 			}
 			KThread.yield();
 		}
 	
 		//pilotLock.release(); 
 		//return;
 
 	}
 
 	static void SampleItinerary() {
 		// Please note that this isn't a valid solution (you can't fit
 		// all of them on the boat). Please also note that you may not
 		// have a single thread calculate a solution and then just play
 		// it back at the autograder -- you will be caught.
 		System.out
 		.println("\n ***Everyone piles on the boat and goes to Molokai***");
 		bg.AdultRowToMolokai();
 		bg.ChildRideToMolokai();
 		bg.AdultRideToMolokai();
 		bg.ChildRideToMolokai();
 	}
 
 }
