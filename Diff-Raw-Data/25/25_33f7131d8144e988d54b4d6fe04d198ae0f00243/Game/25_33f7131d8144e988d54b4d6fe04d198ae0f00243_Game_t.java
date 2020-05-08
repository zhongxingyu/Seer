 /**
  * 
  *  To play this game, create an instance of this class and call the "play"
  *  method.
  *  
  *  NEW CHANGES - 1/17/13 Evan O'Connell:
  *  Added workshop, basement, hallway and vault. Workshop seems like just 
  *  another room, until you remember that there's always been that basement
  *  used for storage there. When you look in the basement, you see a hallway
  *  you've never seen before. The dark hallway leads to the vault full of
  *  old pieces of technology and relics of the past the school has saved.
  *  
  * @author Evan O'Connell
  * @version 2.0 (1/28/13)
  */
 
 class Game 
 {
     private final Parser parser;
     private Room currentRoom;
         
     /**
      * Create the game and initialise its internal map.
      */
     public Game() 
     {
         createRooms();
         parser = new Parser();
     }
     
     /**
      * This is used for testing in jar form or eclipse.
      */
     public static void main(String[] args){
     	Game game = new Game();
     	game.play();
     }
 
     /**
      * Create all the rooms and link their exits together.
      */
     private void createRooms()
     {
         Room outside, theatre, pub, lab, office, workshop, basement, hallway, vault;
       
         // create the rooms
         outside = new Room("outside the main entrance of the university");
         theatre = new Room("in a lecture theatre");
         pub = new Room("in the campus pub");
         lab = new Room("in a computing lab");
         office = new Room("in the computing admin office");
         workshop = new Room("in the craft/wood workshop");
         basement = new Room("in the workshop's stoarge Basement");
         hallway = new Room("in a dark hallway out of the basement you've never seen before...");
         vault = new Room("in a wonderful vault filled with old equipment, materials, and technological relics from years ago.");
         
         /* nulls should be okay since they're caught in Room.setExit()
          * right before they reach the hashmap.
          */
         outside.setExits(null, theatre, lab, pub, null, null);
         theatre.setExits(null, null, null, outside, null, null);
         pub.setExits(null, outside, null, null, null, null);
         lab.setExits(outside, office, null, null, null, null);
         office.setExits(null, null, workshop, lab, null, null);
         workshop.setExits(office, null, null, null, null, basement);
         basement.setExits(hallway, null, null, null, workshop, null);
         hallway.setExits(vault, null, basement, null, null, null);
         vault.setExits(null, null, hallway, null, null, null);
         
         currentRoom = outside;
     }
     
     /**
      *  Main play routine.  Loops until end of play.
      */
     public void play() 
     {            
         printWelcome();
 
         // Enter the main command loop.  Here we repeatedly read commands and
         // execute them until the game is over.
                 
         boolean finished = false;
         while (! finished) {
             Command command = parser.getCommand();
             finished = processCommand(command);
         }
         System.out.println("Thank you for playing.  Good bye.");
     }
 
     /**
      * Print out the opening message for the player.
      */
     private void printWelcome()
     {
         System.out.println("\nWelcome to Adventure!" +
     		"\nAdventure is a new, incredibly boring adventure game." +
     		"\nType 'help' if you need help.\n"
 		);
         printRoomDescription();
     }
     
    /**
     * Retrieves and prints description info from currentRoom object. 
     * 
     * Replaces printExits()
     */
     private void printRoomDescription(){
     	System.out.println(currentRoom.getLongDescription());
     }
     
     /**
      * Prints out all possible exits from your location.
      * 
      * AKA printLocationInfo();
      */
 //    private void printExits(){
 //    	System.out.println("You are " + currentRoom.getDescription());
 //        System.out.print("Exits: \t"+
 //        		(currentRoom.hasExit(Room.Direction.NORTH) ? "north " : "")+
 //				(currentRoom.hasExit(Room.Direction.EAST) ? "east " : "")+
 //				(currentRoom.hasExit(Room.Direction.SOUTH) ? "south " : "")+
 //				(currentRoom.hasExit(Room.Direction.WEST) ? "west " : "")+
 //				(currentRoom.hasExit(Room.Direction.UP) ? "upstairs " : "")+
 //				(currentRoom.hasExit(Room.Direction.DOWN) ? "downstairs " : "")+
 //				"\n"
 //    		);
 //    }
 
     /**
      * Given a command, process (that is: execute) the command.
      * If this command ends the game, true is returned, otherwise false is
      * returned.
      */
     private boolean processCommand(Command command) 
     {
         boolean wantToQuit = false;
 
         if(command.isUnknown()) {
             System.out.println("I don't know what you mean...");
             return false;
         }
         
         
         String commandWord = command.getCommandWord();
         if (commandWord.equalsIgnoreCase("help"))
             printHelp();
         else if (commandWord.equalsIgnoreCase("go"))
             goRoom(command);
         else if (commandWord.equalsIgnoreCase("quit"))
             wantToQuit = quit(command);
 
         return wantToQuit;
     }
 
     // implementations of user commands:
 
     /**
      * Print out some help information.
      * Here we print some stupid, cryptic message and a list of the 
      * command words.
      */
     private void printHelp() 
     {
         System.out.println("You are lost. You are alone. You wander" +
         		"\naround at the university.\n" +
         		"\nYour command words are:\n"
     		);
         System.out.println("\tgo\t|\tquit\t|\thelp");
     }
 
     /** 
      * Try to go to one direction. If there is an exit, enter
      * the new room, otherwise print an error message.
      */
     private void goRoom(Command command) 
     {
         if(!command.hasSecondWord()) {
             // if there is no second word, we don't know where to go...
             System.out.println("Go where?");
             return;
         }
 
         String direction = command.getSecondWord();
 
         // Try to leave current room.
         // TODO: improve by integrating the string directions with the enum directions
         Room nextRoom = null;
         if(direction.equals("north"))
             nextRoom = currentRoom.getExit(Room.Direction.NORTH);
         if(direction.equals("east"))
             nextRoom = currentRoom.getExit(Room.Direction.EAST);
         if(direction.equals("south"))
             nextRoom = currentRoom.getExit(Room.Direction.SOUTH);
         if(direction.equals("west"))
             nextRoom = currentRoom.getExit(Room.Direction.WEST);
         if(direction.equals("up"))
             nextRoom = currentRoom.getExit(Room.Direction.UP);
         if(direction.equals("down"))
             nextRoom = currentRoom.getExit(Room.Direction.DOWN);
 
         if (nextRoom == null || nextRoom.isEmpty())
             System.out.println("There is no exit that way!");
         else {
             currentRoom = nextRoom;
             printRoomDescription();
         }
     }
 
     /** 
      * "Quit" was entered. Check the rest of the command to see
      * whether we really quit the game. Return true, if this command
      * quits the game, false otherwise.
      */
     private boolean quit(Command command) 
     {
         if(command.hasSecondWord()){
             System.out.println("Quit what?");
             return false;
         }
         return true;  // signal that we want to quit
     }
 }
