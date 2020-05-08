 package gameWorld;
 
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 import main.Client;
 import main.NetworkThread;
 import main.NetworkType;
 import main.Server;
 import userInterface.Message;
 
 /** A class to keep track of (and update) the status of the game (Players, Enemies, Items and Progress). **/
 public class GameState {
 
     public static final int NUMBER_OF_ROOMS = 6;
     public static Room[] ROOMS = new Room[NUMBER_OF_ROOMS];
     public static boolean[] LOCKS = new boolean[NUMBER_OF_ROOMS];
     private static NetworkThread network;
     private Player player = new Player("Player", new ArrayList<Item>(), null, null );
 
     static {
         // unlock initial doors
         LOCKS[0] = true;
         LOCKS[1] = true;
         LOCKS[2] = true;
     }
 
     public static void createNetworkThread(NetworkType type) {
 
         switch (type) {
             case SERVER:
                 network = new Server() {
 
                     public void read(int data) {
                         readFromNetwork(data);
                     }
                 };
                 break;
             case CLIENT:
                 network = new Client() {
 
                     public void read(int data) {
                         readFromNetwork(data);
                     }
                 };
                 break;
         }
         network.start();
     }
 
     public static void readFromNetwork(int data) {
         System.out.println("Read from network: " + data);
     }
 
     public static void sendToNetwork(int data) {
         if (network != null) network.send(data);
     }
 
     public static void setNetworkThread(NetworkThread network) {
     }
 
     public GameState() {
         initializeGame();
     }
 
     public void initializeGame() {
 
         // Creating the Rooms //
         createRooms();
 
         // Creating the Rooms //
         fillRooms();
 
         /**
          * Display the game window starting from Room 1, render the items in the room and listen to mouse clicks *
          */
     }
 
     private void createRooms() {
 
         // Create Room1 //
         ROOMS[0] = new Room(1);
 
         // Create Room2 //
         ROOMS[1] = new Room(0, 2, 5);
 
         // Create Room3 //
         ROOMS[2] = new Room(1, 3);
 
         // Create Room4 //
         ROOMS[3] = new Room(2, 4);
 
         // Create Room5 //
         ROOMS[4] = new Room(3);
 
         // Create Room6 //
         ROOMS[5] = new Room(1, 6);
    
     }
     
     private void fillRooms() {
         
         Chest bronzeChest = new Chest(0.20, 0.8, 0.20, 0.20, "images/safe.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
                 System.err.println("Bronze chest was clicked");
                if(this.getContents().size()==0){
                 	Message.show("This chest has already been opened and contains no items");
                 }else{
 	                String input = JOptionPane.showInputDialog("What is the combination?");
 	                int c = 0;
 	                try {
 	                    c = Integer.parseInt(input);
 	                } catch (Exception ex) {
 	                }
 	                if (c == 1234) {
 	                    Message.show("Correct! You now have the bronze key");
 	                    player.addItems(this.getContents());
 	                    this.removeItems();
 	                    LOCKS[3] = true;
 	                } else {
 	                    Message.show("Sorry that was incorrect.");
 	                }
                 }
             }
         };
 
         Item bronzeKey = new Item(0.7, 0.8, 0.2, 0.2, "images/key_bronze.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
                 System.err.println("Bronze key was clicked");
             }
         };
 
         Item silverKey = new Item(0.81, 0.79, 0.3, 0.3, "images/silverkey.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
             	ROOMS[0].removeItem(this);
             	LOCKS[5] = true;
             	Message.show("You found a silver key!");
             }
         };
 
         Item goldKey = new Item(0.5, 0.5, 0.1, 0.1, "images/goldkey.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
             }
         };
         
         String message3 = "Page 3: ";
         Note note3 = new Note(0.8, 0.8, 0.7, 0.7, "images/note.png", message3) {
 
             @Override
             public void onMouseClick(MouseEvent e) {
             	ROOMS[0].removeItem(this);
             	Message.show("You picked up a note! It says:\n" + this.getMessage());
             }
         };
 
         Item baseballBat = new Item(0.7, 0.85, 0.2, 0.2, "images/baseballbat.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
             	player.setWeapon(new Weapon(20));
             	ROOMS[1].removeItem(this);
            	Message.show("You have found a baseball bat. This could help defending against enemies");
             }
         };
         
         // Ehab - Trying Zombies //
         Zombie zombie1 = new Zombie(0.5, 0.5, 0.1, 0.1, "images/zombie1.png"){
         	
         	 @Override
              public void onMouseClick(MouseEvent e) {
              	Message.show("Zombie1 Clicked!");
              }
         };
         
         Item gun = new Item(0.2, 0.85, 0.2, 0.2, "images/gun.png") {
 
             @Override
             public void onMouseClick(MouseEvent e) {
             	player.setWeapon(new Weapon(50));
             	ROOMS[5].removeItem(this);
             	Message.show("You have found a gun. You feel safer.");
             }
         };
         
         bronzeChest.addItem(bronzeKey);
 
         ROOMS[0].addItem(bronzeChest);
         ROOMS[0].addItem(note3);
         ROOMS[0].addItem(silverKey);
         ROOMS[1].addItem(baseballBat);
         ROOMS[5].addItem(gun);
         		//ROOMS[0].addItem(bronzeKey);
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 //                // Fill Room 1 //
 //		ArrayList<Item> items = new ArrayList<Item>();
 //		// Create a box and the Bronze Key in Room 1. Put the key "inside" the box! //
 //		Item box = new Item("Box", "You need a 4 digit pin to open this box!", ROOMS[0], tempImage);
 //		box.setIsBox(true); // Declaring this Item as a box that contains other items //
 //		Item bronzeKey = new Item ("Bronze Key", "Bronze Key!", ROOMS[0], tempImage);
 //		box.getBoxItems().add(bronzeKey);
 //		box.boxSetLocked(true); // Lock the box! //
 //		// Create the silver Key and Note //
 //		Item silverKey = new Item("Silver Key", "Silver Key!", ROOMS[0], tempImage);
 //		silverKey.setHidden(true);
 //		Item noteOfRoom1 = new Item("note1", "Tell the Game Story to Player", ROOMS[0], tempImage);
 //		// Add all items to Room 1 //
 //		ROOMS[0].getItemsOfTheRoom().addAll(items);
 //		
 //		// Fill Room 2 //
 //		Item gun = new Item("Gun", "A Gun! Could be used to kill avarage sized zombies!", ROOMS[1], tempImage);
 //		
 //		// Fill Room 3 //
 //		Item room1BoxCodeNumber = new Item("Code of Box in Room 1", "1234!",  ROOMS[2], tempImage);
 //		Zombie room3Zombie = new Zombie("Room3 Zombie", ROOMS[2], tempImage);
 //		room3Zombie.setItemCarriedByZombie(room1BoxCodeNumber);
 //		
 //		// Fill Room 4 //
 //		Item silverKeyNote = new Item("Silver Key Note", "Silver Key is Hidden in Room 1 behind the brick!", ROOMS[3], tempImage);
 //		Zombie room4Zombie = new Zombie("Room4 Zombie", ROOMS[3], tempImage);
 //		room4Zombie.setItemCarriedByZombie(silverKeyNote);
 //		
 //		// Fill Room 5 //
 //		Item goldenKey = new Item("Golden Key", "Golden Key!", ROOMS[4], tempImage);
 //		Zombie room5Zombie = new Zombie("Room5 Zombie", ROOMS[4], tempImage);
 //		room5Zombie.setItemCarriedByZombie(goldenKey);
 //		
 //		// Fill Room 6 //
 //		Item bazooka = new Item("Bazooka", "Bazooka!!", ROOMS[5], tempImage);
 
     }
 
    
 }
