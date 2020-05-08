 package com.fastquake.textbasedgame;
 
 
 import java.io.*;
 
 import com.fastquake.textbasedgame.gameobject.GameObject;
 import com.fastquake.textbasedgame.room.Room;
 import com.fastquake.textbasedgame.room.RoomManager;
 
 public class TextBasedGame {
 	public static RoomManager rm;
 	public static int score;
 	private static boolean describedRoom = false;
 	static Room currentRoom;
 	private static BufferedReader input;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		score = 0;
 		boolean doShutdown = false; //TODO: Allow the player to exit
 		int currentRoomId = 1;
 		rm = new RoomManager();
 		input = new BufferedReader(new InputStreamReader(System.in));
 		
 		loadScores(1);
 		
 		File scoresFile = new File("scores.txt");
 		if(!scoresFile.exists()) //If the file doesn't exist
 			scoresFile.createNewFile(); //Create it
 		
 		System.out.println("Welcome to TextBasedGame.");
 		//System.out.println("Type \"play\" to start a new game, or \"load\" to load a previous game.");
 		System.out.println();
 
 		while(!doShutdown){
 			currentRoom = rm.getRoomById(currentRoomId);
 			String command = "";
 			
 			if(!describedRoom){ //We don't want to describe the room every time the player
 								//uses a command
 				currentRoom.describe();
 				describedRoom = true;
 			}
 			System.out.println();
 			System.out.print("> ");
 			command = input.readLine();
 			handleCommand(command);
 		}
 	}
 	
 	private static void handleCommand(String inputCommand){
 		inputCommand = inputCommand.toLowerCase(); //Change it to lowercase so that there is no confusion due to case
 		String[] splitCommand = inputCommand.split(" ");
 		String command = splitCommand[0];
 		String object = "";
 		
 		if(command.equals("north") || command.equals("n")){
 			move(Direction.NORTH);
 		}else if(command.equals("east") || command.equals("e")){
 			move(Direction.EAST);
 		}else if(command.equals("south") || command.equals("s")){
 			move(Direction.SOUTH);
 		}else if(command.equals("west") || command.equals("w")){
 			move(Direction.WEST);
 		}else if(command.equals("look") || command.equals("examine") || command.equals("l")){
 			if(splitCommand.length>1){ //If the user entered more than just "look"
 				object = extractObjectName(splitCommand);
 				GameObject lookObject = currentRoom.getObjectByName(object);
 				if(lookObject != null)
 					lookObject.describe();
 				else
 					System.out.println("There is no " + object + " here");
 			}else
 				currentRoom.describe();
 		}else if(command.equals("open")){
 			GameObject openObject;
 			object = extractObjectName(splitCommand);
 			if(object == null){
 				System.out.println("Open what?");
 			}else{
 				openObject = currentRoom.getObjectByName(object);
 				if(openObject != null){
 					if(openObject.isOpenable()){
 						openObject.open();
 					}else{
 						System.out.println("You can't open the " + object);
 					}
 				}else{
 					System.out.println("There is no " + object + " here.");
 				}
 			}
 		}else if(command.startsWith("help")){
 			printHelp();
 		}else if(command.equals("exit") || command.equals("quit")){
 			System.exit(0);
 		}
 	}
 	
 	private static void move(Direction direction){
 		if(currentRoom.getDoorTarget(direction, rm) != null){ //If there is a door in that direction
 			currentRoom = currentRoom.getDoorTarget(direction, rm);
 			describedRoom = false;
 		}else
 			System.out.println("You cannot go that way.");
 	}
 	
 	private static void printHelp(){
 		System.out.println("This is a text-based game, which means that you play it by entering text commands.\n" +
 							"Available commands include: \n" +
 							"help - Displays this text\n"+
 							"open - Opens a door or other object\n"+
 							"n,e,s,w - Moves you north, east, south or west if possible\n"+
 							"exit - Terminates the program");
 	}
 	
 	/**
 	 * Extracts the object name from an array representing the words of a command
 	 * @param ignore A word to ignore, such at "at" (ex. "look at")
 	 * @param splitCommand Array containing the command split into words
 	 * @return The name of an object that the user probably entered.
 	 */
 	private static String extractObjectName(String[] splitCommand){
 		String objectStr = "";
 		int ignoredWords = 0;
 		if(splitCommand.length<2) 	//If there are less than 2 words in the command string, 
 									//then the user did not specify an object
 			return null;
 		//This loop removes certain words from the command string
 		for(int i=0;i<splitCommand.length-ignoredWords;i++){
 			if(splitCommand[i].equals("at") || splitCommand[i].equals("the")){ //If the word is an ignored word
 				if(!(i+1>=splitCommand.length)){ //If there is a next word
 					splitCommand[i] = null;
 					for(int k=i;k<splitCommand.length-1;k++)
 						arraySwap(splitCommand,k,k+1);
 					ignoredWords++;
 					i--;
 				}else
 					return null;
 			}
 		}
 		//Now that those words have been removed, everything after the 
 		//first word will be considered part of the object name
 		for(int i=1;i<splitCommand.length-ignoredWords;i++){
 			objectStr += splitCommand[i];
 			if(!(i+1 >= splitCommand.length-ignoredWords))
 				objectStr += " ";
 		}
 		return objectStr;
 	}
 	
 	public static void arraySwap(String array[], int first, int second) {
 		String hold = array[first];
 		array[first] = array[second];
 		array[second] = hold;
 	}
 	
 	public static void playerDie(){
 		System.out.println("You are dead. Game over.");
 		System.out.println();
 		System.out.println("Your final score is " + score + ".");
 		saveScore();
 		System.out.print("Enter 1 to play again, 2 to view highscores, or anything else to exit: ");
 		try{
 			String response = input.readLine();
 			if(response.equals("1")){
 				main(null);
 			}else if(response.equals("2")){
 				System.out.println("Would you like to sort them by time (most-to-least recent) or score (highest to lowest)?");
 				while(true){ //Input verification loop
 					System.out.print("Enter \"score\" or \"time\": ");
 					response = input.readLine().toLowerCase();
 					if(response.equals("time")){
 						loadScores(1);
 						break;
 					}
 					if(response.equals("score")){
 						loadScores(2);
 						break;
 					}
 					else{
 						System.out.println("Invalid selection. Please try again.");
 						continue;
 					}
 				}
 			}else
 				System.exit(0);
 		}catch(IOException e){ 	//Catch this so everything that calls it won't break. It'll never happen anyway.
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Writes a line to the scores file in the format of:
 	 * [current-unix-time]:[score]
 	 * @throws IOException
 	 */
 	public static void saveScore(){
 		PrintWriter fileOut = null;
 		try{
 			fileOut = new PrintWriter(new FileWriter("scores.txt",true));
 		}catch(IOException e){
 			e.printStackTrace();
 			System.exit(1);
 		}
 		long curTime = System.currentTimeMillis() / 1000L;
 		fileOut.println(curTime + ":" + score);
 	//	System.out.println("Your score has been saved.");
 		fileOut.close();
 	}
 	
 	/**
 	 * Loads and displays scores sorted in a format specified by the user
 	 * @param sortField 1 to sort by time, 2 to sort by score
 	 * @throws IOException
 	 */
 	public static void loadScores(int sortField) throws IOException{
 		BufferedReader fileIn;
 		Score[] scores;
 		int lineCount = 0;
 		
 		try{
 			fileIn = new BufferedReader(new FileReader("scores.txt"));
 		}catch(FileNotFoundException e){	//This shouldn't happen unless the user was 
 											//derping around with the files while the game is running
 			File newFile = new File("scores.txt");
 			newFile.createNewFile();
 			fileIn = new BufferedReader(new FileReader("scores.txt"));
 		}
 		while(fileIn.ready()){
 			fileIn.readLine();
 			lineCount++;
 		}
 		scores = new Score[lineCount];
 		
 		fileIn.close();
 		//The file needs to be reopened so that it can be read again.
 		fileIn = new BufferedReader(new FileReader("scores.txt"));
 		
 		for(int i=0;i<lineCount;i++){
 			String curLine = fileIn.readLine();
 			String[] curLineArray = curLine.split(":");
 			//Now we have an array containing the score and time as separate elements 
 			//Create a score object out of these
 			scores[i] = new Score(Long.parseLong(curLineArray[0]),Integer.parseInt(curLineArray[1]));
 		}
 		
		
 	}
 	
 	/**
 	 * Sorts a score array from highest to lowest score
 	 * @param unsorted Unsorted Score array
 	 * @return Sorted-by-score Score array
 	 */
 	private static Score[] sortByScore(Score[] unsorted){
 		Score[] sorted = new Score[unsorted.length];
 		
 		for(int i=0,j=0;i<unsorted.length;i++){
			Score largest = unsorted[i];
 			largest.score = Integer.MIN_VALUE;
 			int lastLargestIndex = 0;
 			for(int k=0;k<unsorted.length;k++){
				if(unsorted[k].time > largest.time && unsorted[k].time != -1){
 					largest = unsorted[k];
 					lastLargestIndex = k;
 				}
 			}
 			unsorted[lastLargestIndex].score = -1;
			sorted[j] = largest;
 			j++;
 		}
 		return sorted;
 	}
 	
 	/**
 	 * Sorts a score array from most recent to most distant time
 	 * @param unsorted Unsorted Score array
 	 * @return Sorted-by-time Score array
 	 */
 	private static Score[] sortByTime(Score[] unsorted){
 		Score[] sorted = new Score[unsorted.length];
 		
 		for(int i=0,j=0;i<unsorted.length;i++){
			Score largest = unsorted[i];
 			largest.time = Long.MIN_VALUE;
 			int lastLargestIndex = 0;
 			for(int k=0;k<unsorted.length;k++){
 				if(unsorted[k].time > largest.time && unsorted[k].time != -1){
 					largest = unsorted[k];
 					lastLargestIndex = k;
 				}
 			}
 			unsorted[lastLargestIndex].time = -1;
			sorted[j] = largest;
 			j++;
 		}
 		return sorted;
 	}
 }
