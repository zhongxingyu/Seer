 package model;
 
 import java.util.*;
 
 import javax.swing.JOptionPane;
 
 import controller.FPKeyListener;
 import controller.FPMouseListener;
 
 import model.command.Command;
 import model.command.CommandStack;
 
 import model.object.*;
 
 import view.*;
 /**
  *  This class is the main class of the "World of Zuul" application. 
  *  "World of Zuul" is a very simple, text based adventure game.  Users 
  *  can walk around some scenery. That's all. It should really be extended 
  *  to make it more interesting!
  * 
  *  To play this game, create an instance of this class and call the "play"
  *  method.
  * 
  *  This main class creates and initializes all the others: it creates all
  *  rooms, creates the parser and starts the game.  It also evaluates and
  *  executes the commands that the parser returns.
  * 
  */
 
 public class Game extends Observable implements Observer
 {
     private static final String GAME_OVER = "GAME OVER";
 	private static final int STARTING_HEALTH = 20;
 	private final static String PLAYER_DESCRIPTION = "Me";
     private final static int MAX_WEIGHT = 10;
     private final static String DEFAULT_START_ROOM = "entrance";
     
 	private static final String LEFT = "left";
 	private static final String RIGHT = "right";
 	
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 
 	private Parser parser;
     private Player player1;
     private HashMap<String,Room> rooms;
     //private HashMap<String,Monster> monsters;
     private CommandStack redoStack;
     private CommandStack undoStack;
     private FPMouseListener mouseListener;
     private static FPKeyListener keyListener;
     //private String commandFrom;
 
     
     /**
      * Create the game and initialize its internal map.
      */
     public Game() 
     {
         parser = new Parser();
         rooms = new HashMap<String,Room>();
         //monsters = new HashMap<String,Monster>();
         
         mouseListener = new FPMouseListener();
         mouseListener.addObserver(this);
         
         keyListener = new FPKeyListener();
         keyListener.addObserver(this);
         
         undoStack = new CommandStack();
         redoStack = new CommandStack();
         
         initializeGame();        
     }
 
     /**
      * Create all the rooms and link their exits together.
      */
     private void initializeGame()
     {
         Room gallery,waitingroom, workshop, lobby, entrance, dinningroom,studio,theater, dressingroom,technician;
         
         // create the rooms
         rooms.put("gallary",gallery = new FirstPersonRoom("Gallery", mouseListener));
         rooms.put("workshop",workshop = new FirstPersonRoom("Workshop", mouseListener));
         rooms.put("lobby",lobby = new FirstPersonRoom("Lobby", mouseListener));
         rooms.put("entrance",entrance = new FirstPersonRoom("Entrance", mouseListener));
         rooms.put("dinning room",dinningroom = new FirstPersonRoom("Dinning Room", mouseListener));
         rooms.put("studio",studio = new FirstPersonRoom("Studio", mouseListener));
         rooms.put("theater",theater = new FirstPersonRoom("Theater", mouseListener));
         rooms.put("dressing room",dressingroom = new FirstPersonRoom("Dressing Room", mouseListener));
         rooms.put("technician room",technician = new FirstPersonRoom("Technician Room", mouseListener));
         rooms.put("waiting room",waitingroom = new FirstPersonRoom("Waiting Room", mouseListener));
 
         // Initialize room exits
         gallery.setExits(SOUTH,workshop);
         
         workshop.setExits(NORTH,gallery);
         workshop.setExits(EAST,dressingroom);
         
         dressingroom.setExits(WEST,workshop);
         dressingroom.setExits(EAST, technician);
         
         technician.setExits(WEST,dressingroom);
         technician.setExits(NORTH,studio);
         
         studio.setExits(SOUTH,technician);
         studio.setExits(WEST,theater);
         studio.setExits(NORTH,dinningroom);
         
         dinningroom.setExits(SOUTH, studio);
         dinningroom.setExits(WEST, lobby);
         
         lobby.setExits(EAST,dinningroom);
         lobby.setExits(SOUTH,theater);
         lobby.setExits(WEST,waitingroom);
         lobby.setExits(NORTH,entrance);
         
         waitingroom.setExits(EAST,lobby);
         
         theater.setExits(NORTH,lobby);
         theater.setExits(EAST,studio);
         entrance.setExits(SOUTH,lobby);
         
         //create the items
         Item plant = new FirstPersonItem("Plant",2.0,"Plant1.png");
         Item sword = new FirstPersonItem("Sword", 7.0, "Sword1.png");
         Item pogoStick = new FirstPersonItem("PogoStix", 5.0,"PogoStick1.png");
         
         //Add Items
         entrance.addItem(plant,"north");
         workshop.addItem(sword,"south");
         dressingroom.addItem(pogoStick,"east");
         
         //Create monsters
         Monster kracken = new FirstPersonMonster("Kracken",5,"Kracken.png" );
         //monsters.put("Kracken", kracken);
         Monster grendel = new FirstPersonMonster("Grendel", 8,"Grendle.png");
         //monsters.put("Grendel", grendel);
         Monster goblin = new FirstPersonMonster("Goblin",3,"TrollBig.png");
         //monsters.put("Goblin", goblin);
         
         
         //Add Monsters to room
         entrance.addMonster(kracken,"south");
         //kracken.setCurrentRoom(entrance);
         
         workshop.addMonster(grendel,"north");
         //grendel.setCurrentRoom(workshop);
         
         dinningroom.addMonster(goblin,"south");
         //goblin.setCurrentRoom(dinningroom);
         
         String playerName = JOptionPane.showInputDialog("Please enter your name:");
         player1 = new Player(playerName,PLAYER_DESCRIPTION,MAX_WEIGHT,STARTING_HEALTH);
         
         rooms.get(DEFAULT_START_ROOM).visit();
         player1.setCurrentRoom(rooms.get(DEFAULT_START_ROOM));  // start game outside
 
     }
 
     /**
      *  Main play routine.  Loops until end of play.
      * @throws CloneNotSupportedException 
      */
     public void play()
     {            
         printWelcome();
 
         //Refresh the View
         setChanged();
         notifyObservers(player1);
         
         // Enter the main command loop.  Here we repeatedly read commands and
         // execute them until the game is over.
 
         boolean finished = false;
         while (! finished) {
             Command command = parser.getCommand();
             //commandFrom = "player";
             finished = processCommand(command, true);
         }
         
         //Notify observers that the game is over
         setChanged();
         notifyObservers(GAME_OVER);
         
         System.out.println("Game over! Thank you for playing.  Good bye.");
     }
 
     /**
      * Print out the opening message for the player.
      */
     private void printWelcome()
     {
         System.out.println();
         System.out.println("Welcome to the World of Zuul!");
         System.out.println("World of Zuul is a new, incredibly boring adventure game.");
         System.out.println("Type 'help' if you need help.");
         System.out.println();
         printLocationInfo(player1);
     }
 
     private void printLocationInfo(Player player){
         System.out.println(player.getCurrentPlayerRoom().getLongDescription());
         System.out.println(player1.getPlayerName() + "'s health :" + player1.getHealth());
     }
 
     /**
      * Given a command, process (that is: execute) the command.
      * @param command The command to be processed.
      * @return true If the command ends the game, false otherwise.
      * @throws CloneNotSupportedException 
      */
     private boolean processCommand(Command command, boolean addToStack) 
     {
     	
         boolean quit = false;
 
         if(command==null || command.getCommandWord()==null) {
             System.out.println("I don't know what you mean...");
             return false;
         }
 
         if (addToStack) {
         	undoStack.add(command);
         }
         
         //if(parser.isReversible(command.getCommandWord()))
         //{
         //	redoStack.empty();
         //}
 
         String commandWord = command.getCommandWord();
         if (commandWord.equals("help")) {
             printHelp();
         }
         else if (commandWord.equals("go")) {
             goRoom(command);
         }
         else if (commandWord.equals("quit")) {
             quit = true;
         }
         else if (commandWord.equals("look")){
             look();
         }
         else if (commandWord.equals("undo")){
             undo();
         }
         else if (commandWord.equals("redo")){
             redo();
         }
         else if (commandWord.equals("pick")){
             pick(command);
             //checkMonsterAttack();
         }
         else if (commandWord.equals("drop")){
             drop(command);
             //checkMonsterAttack();
         } 
         else if (commandWord.equals("attack")) {
         	attack(command);
         	ArrayList<Monster> m = player1.getCurrentPlayerRoom().getMonsters();
         	for(Monster monster: m)
         	{
         		monster.attack(player1);
         	}
         	//checkMonsterAttack();
         }        
         else if (commandWord.equals("heal")) {
         	ArrayList<Monster> m = player1.getCurrentPlayerRoom().getMonsters();
         	for(Monster monster: m)
         	{
         		monster.heal(player1);
         	}
         	heal(command);
         	//checkMonsterAttack();
         }
         else if (commandWord.equals("turn")) {
         	turn(command);
         }
         else if(commandWord.equals("straight"))
         {
         	Command temp = new Command("go",player1.getLookingDirection());
         	undoStack.add(temp);
         	goRoom(temp);
         }
         
         
     	
         //Notify observers (must notify AFTER monster attacks)
         setChanged();
         notifyObservers(player1);
         
     	//Check to see if the player is still alive, if not, quit
         if (!quit) {
         	quit = player1.getHealth() <= 0;
         }
     	
         return quit;
     }
 
     private void turn(Command command) {
         if(!command.hasSecondWord()) {
             System.out.println("Turn where?");
             return;
         }
         
         String direction = command.getSecondWord();
         
         if (direction.equals(LEFT)) {
 			if (player1.getLookingDirection().equals(NORTH)) {
 				player1.setLookingDirection(WEST);
 			} else if (player1.getLookingDirection().equals(SOUTH)) {
 				player1.setLookingDirection(EAST);
 			} else if (player1.getLookingDirection().equals(EAST)) {
 				player1.setLookingDirection(NORTH);
 			} else if (player1.getLookingDirection().equals(WEST)) {
 				player1.setLookingDirection(SOUTH);
 			}
 		} else if (direction.equals(RIGHT)) {
 			if (player1.getLookingDirection().equals(NORTH)) {
 				player1.setLookingDirection(EAST);
 			} else if (player1.getLookingDirection().equals(SOUTH)) {
 				player1.setLookingDirection(WEST);
 			} else if (player1.getLookingDirection().equals(EAST)) {
 				player1.setLookingDirection(SOUTH);
 			} else if (player1.getLookingDirection().equals(WEST)) {
 				player1.setLookingDirection(NORTH);
 			}
 		}
 	}
 
 	private void undo(){
         Command temp = undoStack.pop();
         if(temp!=null)
         {
         	redoStack.add(temp);
         	//commandFrom = "undo";
         	processCommand(temp, false);
         	
         }
     }
     
 
     private void redo(){
     	Command temp = redoStack.pop();
     	if(temp!=null)
     	{
     		undoStack.add(temp);
     		//commandFrom = "player";
     		processCommand(temp, false);
     	}
     }
         
     /**
      * Attack a monster that is in the room
      * @param command
      */
     private void attack(Command command) {
         if(!command.hasSecondWord()) {
             // if there is no second word, we don't who to attack
             System.out.println("Attack what?");
             return;
         }
         
         Room currentRoom = player1.getCurrentPlayerRoom();
         Monster monster = currentRoom.getMonster(command.getSecondWord());
         
         if (monster == null) {
             // There is no monster by that name in the room
             System.out.println("There is no monster called " + command.getSecondWord() + "!");
             return;
         }
         
         //Decrease the monster's health
         
         monster.decreaseHealth();
         
         if (!monster.isAlive()) {
         	//currentRoom.removeMonster(command.getSecondWord());
         	System.out.println("Good job! You've killed " + command.getSecondWord());
         	return;
         } else {
         	System.out.println(command.getSecondWord() + " health decreased to " + monster.getHealth());
         	//player1.pushLastMonsterAttacked(monster.getName());
         }
 
 	}
     
     /**
      * Un-attack monster
      * @param command
      */
     private void heal(Command command) {
         Room currentRoom = player1.getCurrentPlayerRoom();
         Monster monster = currentRoom.getMonster(command.getSecondWord());
         
         if (monster == null) {
             // There is no monster by that name in the room
             System.out.println("There is no monster called " + command.getSecondWord() + "!");
             return;
         }
         //monsters.get(player1.getLastMonsterAttacked()).increaseHealth();
        //monster.increaseHealth();
     }
 
     
     private void drop(Command command){
     	Item item = player1.drop(command.getSecondWord());
     	if (item != null) {
     		if(player1.getCurrentPlayerRoom().getWall(player1.getLookingDirection()).getItem()!=null)
     		{
     			System.out.println("Cannot place item onto of another item.  Please drop somewhere else.");
     			return;
     		}
     		System.out.println(item.getItemName() + " has been dropped by " + player1.getPlayerName());
 		    player1.getCurrentPlayerRoom().addItem(item,player1.getLookingDirection());
 		    player1.printItemsAndWeight();
     	} else {
     		System.out.println("You cannot drop an item you're not carrying!");
     	}
     }
 
     private void look(){
         System.out.println(player1.getCurrentPlayerRoom().getLongDescription());
         System.out.println(player1.getPlayerName() + "'s health :" + player1.getHealth());
     }
 
     // implementations of user commands:
 
     /**
      * Print out some help information.
      * Here we print some stupid, cryptic messagego and a list of the 
      * command words.
      */
     private void printHelp() 
     {
         System.out.println("You are lost. You are alone. You wander");
         System.out.println("around at the university.");
         System.out.println();
         System.out.println("Your command words are:");
         System.out.println(parser.showCommands());
     }
 
 
     private void pick(Command command) {
 
    
         if(!command.hasSecondWord()) {
             // if there is no second word, we don't know where to go...
             System.out.println("Pick what?");
             return;
         }
 
         String itemName = command.getSecondWord();
         Item item = player1.getCurrentPlayerRoom().getItem(itemName);
 
         // Try to pick up the item.
         
         if(player1.getCurrentPlayerRoom().containsItem(itemName)&&player1.pick(itemName,item)){
             System.out.println(item.getItemName() + " has been picked by " + player1.getPlayerName());
             player1.getCurrentPlayerRoom().removeItem(itemName);
             player1.printItemsAndWeight();
         }else{
             System.out.println("item could not be picked ");
         }
         System.out.println();//
     }
 
     /** 
      * Try to go in one direction. If there is an exit, enter
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
         
         if (direction.equals("straight")) {
         	direction = player1.getLookingDirection();
         }
         
         Room nextRoom = player1.getCurrentPlayerRoom().getExit(direction);
 
         if (nextRoom == null) {
             System.out.println("There is no door!");
         } else if (player1.getCurrentPlayerRoom().getWall(direction).getMonster() != null && player1.getCurrentPlayerRoom().getWall(direction).getMonster().isAlive()) {
         	System.out.println("Cannot go through that door! There is a monster in the way");
         } else {
             // Try to leave current room.
             //player1.setPreviousRoom(player1.getCurrentPlayerRoom());
             player1.setCurrentRoom(nextRoom);
             //monsterMove();
             printLocationInfo(player1);
             nextRoom.visit();
         }
     }
     
     public static void main(String args[]) {    
     	//Create a new game
     	Game game = new Game();
     	
     	//Create a 3D First Person View
     	FirstPersonView view = new FirstPersonView("World of Zuul", keyListener);
     	
     	game.addObserver(view);
     	view.addObserver(game);
 
     	view.show();
 		game.play();
     }
 
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof Command) {
 			Command command = (Command)arg1;
 			if (processCommand(command, true)) {
 		        //Notify observers that the game is over
 		        setChanged();
 		        notifyObservers(GAME_OVER);
 			}
 		}
 	}
 	/*public void monsterMove(){		
 		for(Monster m : monsters.values()){
 			while(true){
 				String monsterExit = m.randomMove();
 				//System.out.println(monsterExit);
 				if(m.getCurrentRoom().getExitString().contains(monsterExit)){
 			
 					m.getCurrentRoom().removeMonster(m.getName());
 					m.getCurrentRoom().getExit(monsterExit).addMonster(m);
 					m.setCurrentRoom(m.getCurrentRoom().getExit(monsterExit));
 					break;
 				}
 			}
 		}
 		
 	}
 	public void monsterAttack(){
 		for(Monster m : player1.getCurrentPlayerRoom().getMonsterList().values()){
 			if (m.isAlive()) {
 				player1.attacked(m.getName());
 			}
 		}
 		player1.addHealthLoss(player1.getCurrentPlayerRoom().getMonsterList().size());
 		
 	}
 	public void monsterUnAttack(){
 		player1.unAttacked();
 	}
 	public void checkMonsterAttack(){
 		if(commandFrom.equals("player")){
 			monsterAttack();
 		}
 		else if(commandFrom.equals("undo")){
 			monsterUnAttack();
 		}
 	}*/
 }
