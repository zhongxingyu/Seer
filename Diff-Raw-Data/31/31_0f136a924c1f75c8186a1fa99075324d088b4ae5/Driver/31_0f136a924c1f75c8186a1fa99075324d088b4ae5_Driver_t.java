 package presenter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Scanner;
 
 import main.Ship;
 import model.Player;
 
 public class Driver {
 	Player playerOne;
 	static int currCurrency = 1000;
 	static ArrayList<Integer> totalSkills;
 	ArrayList<Integer> partySkills;
 	private static ArrayList<Integer> X;
 	private static ArrayList<Integer> Y;
 
 	public Driver() {
 		totalSkills = new ArrayList<Integer>();
 		partySkills = new ArrayList<Integer>();
 	}
 
 	/**
 	 * Creates the universe that the game will be played in.
 	 */
 	public void generateUniverse() {
 
 	}
 
 	/**
 	 * Creates list of usable X and Y coordinates for solar systems. That are at
 	 * least 10 units away from any other coordinate.
 	 */
 	public void generateCoordinates() {
 
 		int amtOfCoordinates = 150; //Amount of coordinates to generate
 		ArrayList<Integer> holdX = new ArrayList<Integer>(); //List to hold X coordinates
 		ArrayList<Integer> holdY = new ArrayList<Integer>(); //Lost to hold Y coordinates
 
 		
 		while (amtOfCoordinates > 0) { //Will loop until proper amt of coordinates have been generated.
 			boolean goodCoords = false;
 			int arrayLength = holdX.size(); //Updates the length of the ArrayList
 
 			while (goodCoords != true) { //Will loop until good coordinates have been found.
 				int trueCounter = 0;
 				//Randomly picks two integers for X and Y.
 				int testX = (int) (Math.random() * 201);
 				int testY = (int) (Math.random() * 201);
 					//Goes through current array to make sure randomly picked X and Y are 
 					// at a distance of 10 or more then all other coordinates.
 				for (int i = 0; i < arrayLength; i++) {
 					int tempX = holdX.get(i);
 					int tempY = holdY.get(i);
 
 					int distance = (int) ((Math.pow(tempX - testX, 2)) + (Math
 							.pow(tempY - testY, 2)));
 					distance = (int) (Math.sqrt(distance));
 					//The randomly picked numbers are >= to coordinates at index i then add to
 					//the trueCounter. Else break and generate new coordinates.
 					if (distance >= 10) {
 						trueCounter++;
 					}
 					else{
 						break;
 					}
 				}
 				if (trueCounter == arrayLength) {
 					holdX.add(testX);
 					holdY.add(testY);
 					goodCoords = true;
 				}
 			}
 			amtOfCoordinates--;
 		}
 
 		X = holdX; 
 		Y = holdY;
 
 	}
 
 	/**
 	 * Creates the player
 	 * 
 	 * @param name
 	 *            - the name of the player
 	 * @param traderUsed
 	 *            - points assigned for trader
 	 * @param engineerUsed
 	 *            - points assigned for engineer
 	 * @param pilotUsed
 	 *            - points assigned for pilot
 	 * @param fighterUsed
 	 *            - points assigned for fighter
 	 */
 	public void createPlayer(String name, int traderUsed, int engineerUsed,
 			int pilotUsed, int fighterUsed) {
 		playerOne = new Player(currCurrency, Ship.ShipType.GNAT, name,
 				pilotUsed, fighterUsed, traderUsed, engineerUsed);
 		totalSkills.add(traderUsed);
 		totalSkills.add(engineerUsed);
 		totalSkills.add(pilotUsed);
 		totalSkills.add(fighterUsed);
 	}
 
 	/**
 	 * Returns the current player's money.
 	 * 
 	 * @return an int that contains the player's money.
 	 */
 	public static int getCurrency() {
 		return currCurrency;
 	}
 
 	/**
 	 * Gets the entire party's skills.
 	 * 
 	 * @return an ArrayList<Integer> that contains all the skills.
 	 */
 	public static ArrayList<Integer> getSkills() {
 		return totalSkills;
 	}
 
 	/**
 	 * This method is to boost the player's statistics in case he/she purchases
 	 * a mercenary.
 	 * 
 	 * @param tr
 	 *            number of trader points.
 	 * @param en
 	 *            number of engineer points.
 	 * @param pi
 	 *            number of pilot points.
 	 * @param fi
 	 *            number of fighter points.
 	 */
 	public void boostSkillPoints(int tr, int en, int pi, int fi) {
 		int[] boostPoints = { tr, en, pi, fi };
 		ArrayList<Integer> newTotalSkills = new ArrayList<Integer>();
 		for (int i = 0; i < boostPoints.length; i++) {
 			newTotalSkills.add(totalSkills.get(i) + boostPoints[i]);
 		}
 		totalSkills = newTotalSkills;
 	}
 	
	public static void generateNames() throws IOException {
		Scanner s = new Scanner(new File("PlanetNames.txt"));
		ArrayList<String> planetNames = new ArrayList<String>();
		while (s.hasNextLine()) {
 			String temp = s.nextLine();
			planetNames.add(temp);
 		}
		ArrayList<String> chosenNames = new ArrayList<String>();
		int amtOfStrings = 150;
		for(int i=0; i<amtOfStrings; i++){
			boolean check = true;
			while(check == true){
				int random = (int) (Math.random() * 450);
				String hold = planetNames.get(random);
				if(chosenNames.contains(hold)==false){
					chosenNames.add(hold);
					System.out.println(hold);
					System.out.println(chosenNames.size());
					check = false;
				}
			}
		}

 	}
 }
