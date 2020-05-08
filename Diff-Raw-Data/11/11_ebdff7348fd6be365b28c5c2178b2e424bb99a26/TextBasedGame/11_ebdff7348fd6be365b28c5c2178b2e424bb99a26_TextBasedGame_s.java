 package com.fastquake.textbasedgame;
 
 
 import java.io.*;
 
 import com.fastquake.textbasedgame.gameobject.GameObject;
 import com.fastquake.textbasedgame.room.Room;
 import com.fastquake.textbasedgame.room.RoomManager;
 
 public class TextBasedGame {
 	public static RoomManager rm;
 	private static boolean describedRoom = false;
 	static Room currentRoom;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		boolean doShutdown = false; //TODO: Allow the player to exit
 		int currentRoomId = 1;
 		rm = new RoomManager();
 		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 		
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
 			}else
 				currentRoom.describe();
 		}else if(command.equals("open")){
 			GameObject openObject;
			if(splitCommand.length<2)
 				System.out.println("Open what?");
			else{
				object = extractObjectName(splitCommand);
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
 		//This loop removes certain words from the command string
 		for(int i=0;i<splitCommand.length-ignoredWords;i++){
 			if(splitCommand[i].equals("at") || splitCommand[i].equals("the")){ //If the word is an ignored word
 				if(!(i+1>=splitCommand.length)){ //If there is a next word
 					splitCommand[i] = null;
 					for(int k=i;k<splitCommand.length-1;k++)
 						swap(splitCommand,k,k+1);
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
 	
 	public static void swap(String array2[], int first, int second) {
 		String hold = array2[first];
 		array2[first] = array2[second];
 		array2[second] = hold;
 	}
 }
