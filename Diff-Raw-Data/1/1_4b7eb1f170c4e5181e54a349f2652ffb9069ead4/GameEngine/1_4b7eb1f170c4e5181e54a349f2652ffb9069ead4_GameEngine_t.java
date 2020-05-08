 /*
  *  This class is part of the "World of Zuul" application. 
  *  "World of Zuul" is a very simple, text based adventure game.
  * 
  *  This class creates all rooms, creates the parser and starts
  *  the game.  It also evaluates and executes the commands that 
  *  the parser returns.
  * 
  * @author  Michael Kolling and David J. Barnes
  * @version 1.0 (Jan 2003)
  */
 import java.util.HashMap;
 import java.util.Stack;
 import java.io.*;
 import java.util.*;
 
 public class GameEngine
 {
     private Parser parser;
     private Room currentRoom;
     private Stack<Room> backRoom;
     private UserInterface gui;
     private HashMap <String, Room> salle;
     private Player heros;
 
 
     /**
      * Constructor for objects of class GameEngine
      */
     public GameEngine()
     {
         parser = new Parser();
         salle = new HashMap<String, Room>();
         backRoom = new Stack<Room>();
         createRooms();
 	heros = new Player("Bitch");
     }
 
     public void setGUI(UserInterface userInterface)
     {
         gui = userInterface;
         printWelcome();
     }
 
     /**
      * Print out the opening message for the player.
      */
     private void printWelcome()
     {
         gui.print("\n");
         gui.println("Welcome to the World of Zuul!");
         gui.println("World of Zuul is a new, incredibly boring adventure game.");
         gui.println("Type 'help' if you need help.");
         gui.print("\n");
         gui.println(currentRoom.getLongDescription());
         gui.showImage(currentRoom.getImageName());
     }
 
     /**
      * Create all the rooms and link their exits together.
      */
     private void createRooms()
     {
         Room porteVille, entreeVille, marchand, place2, place3;
         Item bouteille, cadavre_moisie;
 
         // create the rooms
         porteVille  = new Room("Porte de la ville", "images/img_840x525/entree.jpg");
         entreeVille = new Room("Entree de la ville", "images/img_840x525/place1.jpg");
         marchand    = new Room("Marchand", "images/img_840x525/maison1.jpg");
         place2      = new Room("Place 1", "images/img_840x525/place2.jpg");
         place3      = new Room("Place 2", "images/img_840x525/place3.jpg");
         
         // initialise room exits
         porteVille.setExit("ville", entreeVille);
         
         entreeVille.setExit("porte_de_la_ville", porteVille);
         entreeVille.setExit("marchand", marchand);
         entreeVille.setExit("place2", place2);
         entreeVille.setExit("place3", place3);
         
         marchand.setExit("entree_de_la_ville", entreeVille);
 
         place2.setExit("entree_de_la_ville", entreeVille);
         place2.setExit("place3", place3);
 
         place3.setExit("entree_de_la_ville", entreeVille);
         place3.setExit("place2",place2);
       
         salle.put("porte_de_la_ville", porteVille);
         salle.put("entree_de_la_ville", entreeVille);
         salle.put("marchand", marchand);
         salle.put("place2", place2);
         salle.put("place3", place2);
 
 	bouteille = new Item("bouteille", 1, 1);
 	cadavre_moisie = new Item("cadavre", 0, 50);
 
 	marchand.setItem("bouteille", bouteille);
 	marchand.setItem("cadavre_moisie", cadavre_moisie);
 	marchand.setItem("pomme", cadavre_moisie);
 
 
 		
         currentRoom = porteVille;  // start game outside
     }
 
     /**
      * Given a command, process (that is: execute) the command.
      * If this command ends the game, true is returned, otherwise false is
      * returned.
      */
     public void interpretCommand(String commandLine) 
     {
         gui.println(commandLine);
         Command command = parser.getCommand(commandLine);
 
         if(command.isUnknown()) {
             gui.println("I don't know what you mean...");
             return;
         }
 
         String commandWord = command.getCommandWord();
         if (commandWord.equals("help"))
             printHelp();
         else if (commandWord.equals("go")){
 		if (heros.get_ventre() >= 5){
             		goRoom(command);
 			heros.add_ventre(-5);}
 		else {
 			gui.println("You need to eat something");
 			}
 		}
         else if (commandWord.equals("quit")) {
             if(command.hasSecondWord())
                 gui.println("Quit what?");
             else
                 endGame();}
 	else if (commandWord.equals("look")){
 		if (command.hasSecondWord()){
 			if ((command.getSecondWord()).equals("sac")){
 				gui.println("In the bag : ");
 				gui.println(heros.getSacString());
 			}
 		}else{
         		gui.println(currentRoom.getLongDescription());}
 			
 	}
 				
 	else if (commandWord.equals("eat"))
 		heros.add_ventre(5);
 	else if (commandWord.equals("status"))
 		gui.println("Etat : "+ heros.get_ventre());
 	else if (commandWord.equals("back"))
     		goBack();
 	else if (commandWord.equals("execute"))
 		execute(command);
 	else if (commandWord.equals("take"))
 		take(command);
 	else if (commandWord.equals("drop"))
 		drop(command);
 		
 	
     
 }
 
     // implementations of user commands:
 
     /**
      * Print out some help information.
      * Here we print some stupid, cryptic message and a list of the 
      * command words.
      */
     private void printHelp() 
     {
         gui.println("You are lost. You are alone. You wander");
         gui.println("around at Monash Uni, Peninsula Campus." + "\n");
         gui.print("Your command words are: " + parser.showCommands());
     }
 
     /** 
      * Try to go to one direction. If there is an exit, enter the new
      * room, otherwise print an error message.
      */
     private void goRoom(Command command) 
     {
         if(!command.hasSecondWord()) {
             // if there is no second word, we don't know where to go...
             gui.println("Go where?");
             return;
         }
 
         String direction = command.getSecondWord();
 
         // Try to leave current room.
         Room nextRoom = currentRoom.getExit(direction);
         
         if (nextRoom == null)
             gui.println("There is no door!");
         else {
         	backRoom.push(currentRoom);
             currentRoom = nextRoom;
             gui.println(currentRoom.getLongDescription());
             gui.println(currentRoom.getItemString());
             if(currentRoom.getImageName() != null)
                 gui.showImage(currentRoom.getImageName());
         }
     }
     
 
     private void execute(Command command) 
     {
 	File fichier;
 	Scanner it;
 
 	if(!command.hasSecondWord()) {
 		gui.println("I see what ? What ?");
 		return;}
 	else{
 		try {
 			fichier = new File(command.getSecondWord());
 			it = new Scanner(fichier);
 			while(it.hasNextLine())
     				interpretCommand(it.nextLine());
 
 		} catch (FileNotFoundException e){
 				gui.println("Pas de fichier...");
 		}
 	}
 }
 
     private void drop(Command command) {
     	String item;
 
     	if(!command.hasSecondWord()) { 
 		gui.println("Wath da fuck... ?");
 		return;}
 	else {
 		item = command.getSecondWord();
 		if (heros.hasItem(item))
 			currentRoom.addItem(heros.drop_item(item));
 		else
 				gui.println("This item doesn't exist");
 	}
     }
     private void take(Command command) {
     	String item;
 
     	if(!command.hasSecondWord()) { 
 		gui.println("Wath da fuck... ?");
 		return;}
 	else {
 		item = command.getSecondWord();
 		if (currentRoom.hasItem(item))
 			heros.add_item(currentRoom.removeItem(item));
 		else
 				gui.println("This item doesn't exist");
 	}
     }
 
 		
 			
 
     private void goBack(){
     	if(backRoom.empty())
     		gui.println("Pas de back Room");
     	else{
     		currentRoom = backRoom.pop();
     		gui.println(currentRoom.getLongDescription());
     		gui.println(currentRoom.getItemString());
             if(currentRoom.getImageName() != null)
                 gui.showImage(currentRoom.getImageName());
         }
     	
     }
     private void endGame()
     {
         gui.println("Thank you for playing.  Good bye.");
         gui.enable(false);
     }
 
 }
