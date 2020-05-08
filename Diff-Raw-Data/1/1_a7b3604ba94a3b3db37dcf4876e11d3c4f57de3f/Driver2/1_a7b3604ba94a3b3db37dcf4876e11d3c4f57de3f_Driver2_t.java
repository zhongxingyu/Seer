 /*
  * ALGORITHM FOR MIN SPANNING TREE
  * 1. eliminate paths to rooms that have already been prcessed
  * 2. check if avail is empty
  * 3. find cheapest in avail
  * 4. add cheapest to processed and add adjacent doors to avail
  */
 
 /**
  * @author Matt Newbill
  * @author Matt Hamersky
  * Comp 282 
  * Project 2
  * Spring 2013
  * Driver2 Class
  */
 import java.util.*;
 import java.io.*;
 import java.util.*;
 public class Driver2 {
 	/* Global fields */
 	private static Scanner screen; 
 	private static Room theRooms[];
 	private static ArrayList<Integer> processedRooms;
 	private static int k;
 	
 	/**
 	 * 
 	 * @param args
 	 * @throws FileNotFoundException
 	 */
 	public static void main(String[] args) throws FileNotFoundException{
 		System.out.println("Welcome to castle helper.\n");
 		File inputFile = new File("sample.txt");
 		screen = new Scanner(inputFile);
 		initializeSpookiness();
 		blockedDoors();
 		calculateReachableRooms();
 		calculateMinWorkToOpenAllRooms();
 		calculateMinWorkBetweenTwoRooms();
 		calculateTotalSpookiness();
 		calculateMaxSpookiness();
 	}
 
 	/**
 	 * Read (from the data file) and set the spookiness for each room
 	 */
 	private static void initializeSpookiness() {
 		String tempSize = screen.nextLine(); //pull out K from file
 		tempSize = tempSize.trim();
 		k = Integer.parseInt(tempSize);
 		
 		theRooms = new Room[k * k];  //Intialize array of Room
 		for(int i = 0; i < theRooms.length; i++) {
 			String tempSpookiness = screen.nextLine();
 			tempSpookiness = tempSpookiness.trim();
 			int spookiness = Integer.parseInt(tempSpookiness);
 			theRooms[i] = new Room(spookiness, i, k);
 		}
 	}
 	
 	/**
 	 * Remove the blocked off doorways
 	 */
 	private static void blockedDoors() {
 		while(screen.hasNext()) {
 			String tempBlockedDoor = screen.nextLine();
 			String[] temp = tempBlockedDoor.split("\\s+");
 			int firstDoor = Integer.parseInt(temp[0]);
 			int secondDoor = Integer.parseInt(temp[1]);
 			theRooms[firstDoor].removeDoor(secondDoor);
 			theRooms[secondDoor].removeDoor(firstDoor);
 		}
 	}
 	
 	/**
 	 * Finds all the rooms that can be reached
 	 */
 	private static void calculateReachableRooms() {
 		ArrayDeque<Integer> visited = new ArrayDeque<Integer>();
 		ArrayList<Integer> processed = new ArrayList<Integer>();
 		ArrayList<Integer> remaining = new ArrayList<Integer>();
 		visited.add(0); //Starts at the entrance which is index 0
 		initializeRemainingRooms(remaining);
 		
 		System.out.print("The reachable rooms are: ");
 		
 		while(!visited.isEmpty()) { //while there are items still to be visited
 			//System.out.print("here!");
 			ArrayList<Integer> adjacentRooms = theRooms[visited.peekLast()].getValidDoors();
 			ArrayList<Integer> adjacentRooms2 = removeAdjacent(adjacentRooms, remaining); 
 			addAdjacent(adjacentRooms2, visited);
 			processed.add(visited.peekLast()); //the first visited room is now finished
 			visited.removeLast(); //delete the first visited from "visited"
 		}
 		printProcessedRooms(processed);
 	}
 	
 	/**
 	 * Fills the "remaining" array list with all the nodes except the entrance.
 	 * @param remaining
 	 */
 	private static void initializeRemainingRooms(ArrayList<Integer> remaining) {
 		for(int i = 1; i < (k * k); i++) {
 			remaining.add(i);
 		}
 	}
 	
 	/**
 	 * Adds the array of adjacent rooms into the "visited" queue
 	 * @param adjacent
 	 * @param visited
 	 */
 	private static void addAdjacent(ArrayList<Integer> adjacent, 
 									ArrayDeque<Integer> visited) {
 		for(int i = 0; i < adjacent.size(); i++) {
 			visited.push(adjacent.get(i));
 		}
 	}
 	
 	/**
 	 * Removes the nodes that are adjacent from the remaining nodes
 	 * @param adjacent
 	 * @param remaining
 	 * @return
 	 */
 	private static ArrayList<Integer> removeAdjacent(ArrayList<Integer> adjacent, ArrayList<Integer> remaining) {
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		for(int i = 0; i < adjacent.size(); i++) {
 			for(int j = 0; j < remaining.size(); j++) {
 				if(adjacent.get(i) == remaining.get(j)) {
 					temp.add(adjacent.get(i));
 					remaining.remove(j);
 					break;
 				}
 			}
 		}
 		return temp;
 	}
 	
 	/**
 	 * Indexes through the "processed" array and makes sure there are no
 	 * duplicates.  Then it prints them.
 	 * @param processed
 	 */
 	private static void printProcessedRooms(ArrayList<Integer> processed) {
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		for(int i = 0; i < processed.size(); i++) {
 			for(int j = 0; j < processed.size(); j++) { //make sure there are no duplicates
 				if((i != j) && (processed.get(i) != processed.get(j))) {
 					temp.add(processed.get(i));
 					break;
 				}
 			}
 		}
 		Collections.sort(temp); //sort the data numerically
 		for(int i = 0; i < temp.size() - 1; i++) {
 			System.out.print(temp.get(i) + ", ");
 		}
 		System.out.println(temp.get(temp.size() - 1) + ".");
 	}
 	
 	private static void calculateMinWorkToOpenAllRooms() {
 	    int n = 0;
 	    PriorityQueue<Door> couldVisit = new PriorityQueue<Door>();
 	    ArrayList<Room> alreadyVisited = new ArrayList<Room>();
 	    ArrayList<Door> firstRoomsDoors = theRooms[0].getValidDoorsObj();
 	    for(int i=0; i<firstRoomsDoors.size();i++)//adds first rooms doors to could visit list
 		couldVisit.add(firstRoomsDoors.get(i));
	    alreadyVisited.add(theRooms[0]);
 	    while(!couldVisit.isEmpty()){
 		Door door = couldVisit.poll();
 		if(!alreadyVisited.contains(theRooms[door.getTo()])){
 		    n+= door.getWeight();
 		    alreadyVisited.add(theRooms[door.getTo()]);
 		    Room newRoom = theRooms[door.getTo()];
 		    ArrayList<Door> newDoors = newRoom.getValidDoorsObj();
 		    couldVisit.addAll(newDoors);
 		}		    
 	    }		
 	    System.out.println("\nThe minimum amount of work necessary to open " +
 						   "doors so that all rooms are accessable is: " + n);
 		
 	}
 	
 	private static void calculateMinWorkBetweenTwoRooms() {
 		int[][] result = new int[k*k][k*k];
 		for(int col = 0; col<k*k; col++)
 		    for(int row = 0; row<k*k; row++)
 			result[row][col] = calculateMinWork(row,col);
 		System.out.println("\nMinimum amount of work to move between " +
 				"a pair of rooms:");
 		printTable(result);
 	}
 
 	private static int calculateMinWork(int from, int to){
 	    if(from %2 == 0)//for testing print purposes
 	    return 0;
 	    return 11;
 	}
 	
 	private static void calculateTotalSpookiness() {
 	    int[][] result = new int[k*k][k*k];
 		for(int col = 0; col<k*k; col++)
 		    for(int row = 0; row<k*k; row++)
 			result[row][col] = calculateTotalSpookiness(row,col);
 		System.out.println("\nTotal spookiness to move between" +
 				" a pair of rooms:");
 		printTable(result);
 	}
 	
 	private static int calculateTotalSpookiness(int from, int to){
 	    return 0;
 	}
 
 	private static void calculateMaxSpookiness() {
 	    int[][] result = new int[k*k][k*k];
 		for(int col = 0; col<k*k; col++)
 		    for(int row = 0; row<k*k; row++)
 			result[row][col] = calculateMaxSpookiness(row,col);
 		System.out.println("\nMax spookiness to move between a " +
 				"pair of rooms:");
 		printTable(result);
 	}
 	
 	private static int calculateMaxSpookiness(int from,int to){
 	    return 0;
 	}
 	
 	private static void printTable(int[][] result) {
 	    for(int row = 0; row<k*k; row++){
 		for(int col = 0; col<k*k; col++){
 		    if(result[row][col] == -1)
 			System.out.printf("%2s ","XX");
 		    else
 			System.out.printf("%2d ",result[row][col]);
 		}
 	    System.out.println();
 	    }
 		    
 	    
 	}
 
 }
