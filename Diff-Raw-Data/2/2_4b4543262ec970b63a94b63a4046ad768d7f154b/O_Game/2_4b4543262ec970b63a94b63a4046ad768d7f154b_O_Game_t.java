 package hhu.propra_2013.gruppe_13;
 
 import java.awt.KeyboardFocusManager;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import java.io.*;
 
 class O_Game {
 	// Frame, graphic, logic and a figure for the actual game
 	JFrame 		gameWindow;
 	Logic 		logic;
 	GameDrawer 	graphics;
 	Figure 		figure;
 		
 	
 	// Build two lists, the graphics component will also receive the figure, which has a special function in the logic class
 	ArrayList<ArrayList<GameObjects>> rooms;
 	
 	// Initialize method for the actual game
 	O_Game(JFrame inFrame) {
 		// Initiate object variables
 		gameWindow 		= inFrame;
 		rooms 			= new ArrayList<ArrayList<GameObjects>>();
 		figure 			= new Figure(0, 0, gameWindow);
 		int element, line, column, dest; //for room generation, saves the current char (as int),the line its from, and the column its in
 		
 		
 		// iterate over all objects and rooms within the level, all objects run within [0...800)x[0...600)
 		// TODO: make that shit better!!, implement the current level
 		for (int i=0; i<3; i++) {
 			ArrayList<GameObjects> temp = new ArrayList<GameObjects>();
 			temp.add(figure);
 			
 			try {
 				InputStream roomStream = new FileInputStream("Level/raum"+i+".txt");
 				Reader roomReader = new InputStreamReader (roomStream);
 				
 				element = 0;
 				column = 0; 
 				line = 0;	
 				dest = 0;
 				while ((element = roomReader.read()) != -1){ //Goes trough the whole raumX.txt, and spawns Objects at their Positions
 					
 					switch (element) { 	//ASCII: W=87 D=68 E=69
 					case 87:			//In order of probability
 						temp.add(new Wall(column-1, line-1, 0.5, 1)); 	//-1 because the top left corner seems to have
 						break;											//the coordinates 1:1
 					case 69:
 						temp.add(new Enemy(column-1, line-1, 0.5, gameWindow));
 						break;
 					case 68: //looks where the door is, then sets destination accordingly
 						
 						if (line-1 == 0) 	{dest = 1;} //Door is on the upper edge of the field, door should lead up
 						if (line-1 == 14) 	{dest = 3;} //Door is on the bottom edge of the field, door should lead down
 						if (column-1==24)	{dest = 2;} //Door is on the right edge of the field, door should lead right
 						if (column-1==0) 	{dest = 4;} //Door is on the left edge of the field, door should lead left
 						
 						temp.add(new Door(column-1, line-1, 0.5, true, true, dest)); //creating door with correct destination
 						break;	
 					}
 					column++; //sets column up for the next cycle of the switch-case
 						if (column==25){ //since we use 0-24 chars per line, the 25th should trigger the next line
 							column = 0;
 							line++;
 						}
 					}

 				} catch (IOException e) {
 				System.out.println("File not found, system exiting.");
 				System.exit(1);
 			}
 			
 			rooms.add(i, temp);
 		}
 
 		// Initialize Logic and Graphics
 		graphics 	= new GameDrawer(rooms, gameWindow);
 		logic 		= new Logic(rooms, figure, this);
 		
 		// set contentPane to JPanel returned by GameDrawer, set GameIO as keyboard manager
 		gameWindow.setContentPane(graphics.init(logic));
 		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         manager.addKeyEventDispatcher(new Game_IO(logic));
 	}
 	
 	// Setter method so the graphic will know which room to paint
 	void setRoom(int inRoom) {
 		graphics.setRoom(inRoom);
 	}
 	
 	void start() {
 		// Build two new threads, one for logic and one for graphics
 		Thread logicThread = new Thread(logic);
 		Thread graphicThread = new Thread(graphics);
 
 		logicThread.start();
 		graphicThread.start();
 	}
 }
