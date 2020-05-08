 /**
  *  This class is the main class of the "World of Zuul" application. 
  *  "World of Zuul" is a very simple, text based adventure game.  Users 
  *  can walk around some scenery. That's all. It should really be extended 
  *  to make it more interesting!
  * 
  *  To play this game, create an instance of this class and call the "play"
  *  method.
  * 
  *  This main class creates and initialises all the others: it creates all
  *  rooms, creates the parser and starts the game.  It also evaluates and
  *  executes the commands that the parser returns.
  * 
  * @author  Michael Kolling and David J. Barnes
  * @version 2006.03.30
  */
 
 public class Game 
 {
     private Parser parser;
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
      * Create all the rooms and link their exits together.
      */
     private void createRooms()
     {
         Room foret, grotte, montagne, plaine, temple, plage;
       
         // create the rooms
         foret = new Room(" dans la forêt au nord-ouest de l'île");
         grotte = new Room("dans la grotte au nord de l'île");
         montagne = new Room("dans les montagnesau nord-est de l'île");
         plaine = new Room("à la plaine à l'ouest de l'île");
         temple = new Room("dans le temple au centre de l'île");
         plage = new Room("à la plage au sud de l'île");
         
         // initialise room exits
         foret.setExit("est", grotte);
         foret.setExit("sud", plaine);
         grotte.setExit("ouest", foret);
         grotte.setExit("est", montagne);
         grotte.setExit("sud", temple);
         montagne.setExit("ouest", grotte);
         plaine.setExit("nord", foret);
         plaine.setExit("est", temple);
         temple.setExit("nord", grotte);
         temple.setExit("sud", plage);
         temple.setExit("ouest", plaine);
         plage.setExit("nord", temple);
 
         currentRoom = temple;  // start game outside
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
         System.out.println("Rooooh tu veux plus jouer déjà? Bon ben bye bye alors");
     }
 
     /**Retourne les informations des sorties possibles
      * @return Les sorties possibles
      */
    private void printLocationInfo()
     {
 //        System.out.println("Tu es " + currentRoom.getDescription());
 //        
 //        System.out.println(currentRoom.getExitString());
     	
     	System.out.println(currentRoom.getLongDescription());
     }
     
     
    /**Redonner les informations de la salle et les sorties disponibles
     */
    private void look()
    {
    	System.out.println(currentRoom.getLongDescription());
    }
    
     /**
      * Print out the opening message for the player.
      */
     private void printWelcome()
     {
         System.out.println();
         System.out.println("Bienvenue à Water Game!");
         System.out.println("Prépare toi à jouer à THE jeu le plus merveilleux de l'univers");
         System.out.println("Tape help si tu as besoin d'aide");
         System.out.println();
                 
         printLocationInfo();
     }
 
     /**
      * Given a command, process (that is: execute) the command.
      * @param command The command to be processed.
      * @return true If the command ends the game, false otherwise.
      */
     private boolean processCommand(Command command) 
     {
         boolean wantToQuit = false;
 
         if(command.isUnknown()) {
             System.out.println("Je ne comprend pas ce que tu veux faire...");
             return false;
         }
 
         String commandWord = command.getCommandWord();
         if (commandWord.equals("help"))
             printHelp();
         else if (commandWord.equals("go"))
             goRoom(command);
         else if (commandWord.equals("quit"))
             wantToQuit = quit(command);
        else if (commandWord.equals("look"))
        	look();
 
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
         System.out.println("Tu es perdu sur l'île, tu es entouré d'ennemis");
         System.out.println("Cherche vite un abris ou de la nourriture");
         System.out.println();
         System.out.println("Les commandes sont:");
         System.out.println("   go, quit, help");
     }
 
     /** 
      * Try to go to one direction. If there is an exit, enter
      * the new room, otherwise print an error message.
      */
     private void goRoom(Command command) 
     {
         if(!command.hasSecondWord()) {
             // if there is no second word, we don't know where to go...
             System.out.println("Aller ou exactement, soit un peu plus précis!");
             return;
         }
 
         String direction = command.getSecondWord();
 
         // Try to leave current room.
         Room nextRoom = currentRoom.getExit(direction);
 
         if (nextRoom == null) {
             System.out.println("Il n'y a rien ici, tu vas tomber dans l'eau si tu continues!");
         }
         else {
             currentRoom = nextRoom;
             printLocationInfo();
         }
     }
 
     /** 
      * "Quit" was entered. Check the rest of the command to see
      * whether we really quit the game.
      * @return true, if this command quits the game, false otherwise.
      */
     private boolean quit(Command command) 
     {
         if(command.hasSecondWord()) {
             System.out.println("Quit quoi? Si tu veux quitter le jeux, écris juste quit");
             return false;
         }
         else {
             return true;  // signal that we want to quit
         }
     }
 }
