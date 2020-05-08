 package game.vhat;
 /*
  * Programmed By:  BA Media Club
  * Programmed For:  BA Media Club
  * Program Description:  A game... expanding on this later
  */
 
 //TODO:  checkpoints
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.gui.*;
 import java.awt.Font;
 /*
  * TODO: make it with states so that you can have a menu screen and stuff
  * 		 also, other stuff
  */
 public class vhat extends BasicGame{
 	
 	player henry;						// Reference the player class to create a "henry" player object
 	girl girl;							// Makes henry's companion
 	TextField girlTipOne;				// This is where the user will be able to hear what henry's companion has to say
 	TextField girlTipTwo;				//		There are three because a text field does not support multi-line text
 	TextField girlTipThree;
 	mapManager entrance;				// First map
 	mapManager zim;						// Second map
 	mapManager hallway;					
 	mapManager wombo;
 	TrueTypeFont f;
 	
 	// Keeps track of all the locations that henry has been
 	boolean checkPoints[] = {true, false, false, false};
 	
 	int speed = 1;						// How many pixels henry moves 
 	static int screen_width = 960;	
 	static int screen_height = 705;
 	
 	// Construct calls construct from BasicGame 
 	public vhat(){
 		super("vhat");
 	}
 	
 	// Initializes everything needed
 	public void init(GameContainer gc) throws SlickException {
 			
 		f = new TrueTypeFont(new Font("", java.awt.Font.PLAIN, 14), true);			// Creates a font object
 		
 		gc.setShowFPS(false);
 		
 		// mapFirst is 30x20 tiles (tiles are 32x32 pixels)
 		entrance = new mapManager("res/entrance.tmx", location.entrance, 32*20,32*20-64); 		// Creates map that can be used
 		zim = new mapManager("res/zim.tmx", location.zim, 32*5,0);				//		Same as above
 		hallway = new mapManager("res/hallway.tmx", location.hallway, 32*2, 0);
 		wombo = new mapManager("res/wombo.tmx", location.wombo, 32*5, 32*7);
 		henry = new player(entrance.get_xSpawn(), entrance.get_ySpawn(), 
 				"res/henryTwoPointO.png", location.entrance);								// Creates a new "henry" object from the player class
 		
 		girl = new girl(0,0,"res/ninja.png");										// Makes henry's companion
 		girlTipOne = new TextField(gc, f, girl.get_width(), 640, 500, 20);			// Allows for output from henry's companion
 		girlTipOne.setAcceptingInput(false);										// So the user can't input anything
 		girlTipTwo = new TextField(gc, f, girl.get_width(), 660, 500, 20);			
 		girlTipTwo.setAcceptingInput(false);
 		girlTipThree = new TextField(gc, f, girl.get_width(), 680, 500, 20);			
 		girlTipThree.setAcceptingInput(false);
 		
 		updateGirlSpeak();
 	}
 		
 	// Updates variables based on the user input	
 	public void update(GameContainer gc, int delta) throws SlickException {
 		Input input = gc.getInput();	// Creates an input object
 		
 		if (henry.get_loc() == location.entrance){				// So the computer can keep track which map henry is on
 			makeInBound(entrance);		//		and make henry inBound in that map.
 		}else if (henry.get_loc() == location.zim){
 			makeInBound(zim);
 		}else if (henry.get_loc() == location.hallway){
 			makeInBound(hallway);
 		}else if (henry.get_loc() == location.wombo){
 			makeInBound(wombo);
 		}
 		
 		// Determines the new position on henry depending on the arrow key that is pressed
 		// Keep in mind that the map is the thing that is actually moving - henry is always stationary
 		if (input.isKeyDown(Input.KEY_UP)){				
 			henry.ch_y(-speed);
 		}else if (input.isKeyDown(Input.KEY_DOWN)){
 			henry.ch_y(speed);
 		}else if (input.isKeyDown(Input.KEY_RIGHT)){
 			henry.ch_x(speed);
 		}else if (input.isKeyDown(Input.KEY_LEFT)){
 			henry.ch_x(-speed);
 		}
 		
 		if (input.isKeyPressed(Input.KEY_A)){
 			henry.interact(girl);	
 		}
 	}
 
 	// Renders things to the screen based on the variables that were modified in the update method
 	public void render(GameContainer gc, Graphics g) throws SlickException { 
 		if (henry.get_loc() == location.entrance){				// Determines which map to render based on location
 			entrance.render(0, 0);		//	(which map henry is on)
 		}else if (henry.get_loc() == location.zim){
 			zim.render(0, 0);
 		}else if (henry.get_loc() == location.hallway){
 			hallway.render(0, 0);
 		}else if (henry.get_loc() == location.wombo){
 			wombo.render(0, 0);
 		}
 		
 		henry.draw(henry.get_x(),henry.get_y(), (float)1);		// Draws henry at his x and y position		
 		girl.draw(0,screen_height-girl.get_height(),1);			// Draws henry's companion
 		girlTipOne.render(gc, g);								// Draws what the companion has to say
 		girlTipTwo.render(gc, g);
 		girlTipThree.render(gc, g);
 	}
 	
 	/*
 	 * A method that determines when the girl will speak and what she will say
 	 */
 	public void updateGirlSpeak() throws SlickException{
 		girlTipOne.setText(girl.get_words(0));
 		girlTipTwo.setText(girl.get_words(1));
 		girlTipThree.setText(girl.get_words(2));
 	}
 	
 	/* 
 	 * Makes sure that henry is in bounds.  If henry is not in bounds, he is moved back.
 	 * This method includes keeping henry on the screen and making sure he stays on the path.
 	 */ 
 	public void makeInBound(mapManager map) throws SlickException{
 		//changeLocation();
 		      
 		// For keeping henry in the screen
 		if (henry.get_y() < 0){												// Upper boundary
 			henry.ch_y(speed);}
 		if (henry.get_y() > (map.getHeight()*map.getTileHeight() - 32)){	// Lower boundary
 			henry.ch_y(-speed);}
 		if (henry.get_x() < 0){												// Left-most boundary
 			henry.ch_x(speed);}
 		if (henry.get_x() > (map.getWidth()*map.getTileWidth() - 32)){		// Right-most boundary
 			henry.ch_x(-speed);}
 		
 		// Stupid thing for keeping henry in the path 
 		int top = (int)henry.get_y()+24;						//|
 		int bottom = (int)henry.get_y()+henry.get_height();		//| Defines henry's 
 		int left = (int)henry.get_x()+4;						//| hit box
 		int right = (int)henry.get_x()+henry.get_width()-4;		//|
 		
 		if (left>=map.getForwardX() && right<=map.getForwardX()+64 && top>=map.getForwardY() && bottom<=map.getForwardY()+32){
 			updateLocationOne();
 		}
 		
 		if (left>=map.getBackwardX() && right<=map.getBackwardX()+64 && top>=map.getBackwardY() && bottom<=map.getBackwardY()+32){
 			updateLocationTwo();
 		}
 		
 		if (henry.get_loc() == location.hallway){
 			return;
 		}
 		
 		int t = map.getTileId(((int)((left+right)/2)/32),(int)(((top+bottom)/2)/32), 0);	// Tiled id at center of hit box
 		try{
 			if (t >= 3 && t <= 10){		// If the center of the hit box has a tile id that corresponds to a wall tile, adjust
 				if (map.getTileId(((int)((left+right)/2)/32), (int)(top/32), 0) <= 2 ||				// Top tile is not wall
 						map.getTileId(((int)((left+right)/2)/32), (int)(top/32), 0) >= 11){
 					henry.ch_y(-2);
 				}else if (map.getTileId(((int)((left+right)/2)/32), (int)(bottom/32), 0) <=2 ||		// Bottom tile is not a wall
 						map.getTileId(((int)((left+right)/2)/32), (int)(bottom/32), 0) >= 11){
 					henry.ch_y(2);
 				}else if (map.getTileId((int)(left/32), (int)((top+bottom)/2)/32, 0) <= 2 ||		// Leftmost tile " " " "
 						map.getTileId((int)(left/32), (int)((top+bottom)/2)/32, 0) >= 11){
 					henry.ch_x(-2);
 				}else if (map.getTileId((int)(right/32), (int)((top+bottom)/2)/32, 0) <= 2 ||		// Rightmost tile " " " "
 						map.getTileId((int)(right/32), (int)((top+bottom)/2)/32, 0) >= 11){
 					henry.ch_x(2);
 				}
 			}
 		}catch (ArrayIndexOutOfBoundsException e){				// So that it does not goof up
 			e.printStackTrace();
 		}catch (IndexOutOfBoundsException e){
 			e.printStackTrace();
 		}
 	}
 	
 	// First algorithm to choose which random place to put henry
 	public void updateLocationOne() throws SlickException{
 		int r = (int)(Math.random() * 4);
 		henry.set_loc(location.values()[r]);
		putOnSpawn(henry.get_loc());
 	}
 	
 	// Second random algorithm to choose which random place to put henry
 	public void updateLocationTwo() throws SlickException{
 		int r = (int)(Math.random() * 4);
 		henry.set_loc(location.values()[r]);
		putOnSpawn(henry.get_loc());
	}
	
	public void putOnSpawn(location l) throws SlickException{
		if (henry.get_loc() == location.entrance){				
			henry.new_y(entrance.get_ySpawn());	
			henry.new_x(entrance.get_xSpawn());		
		}else if (henry.get_loc() == location.zim){
			henry.new_y(zim.get_ySpawn());	
			henry.new_x(zim.get_xSpawn());
		}else if (henry.get_loc() == location.hallway){
			henry.new_y(hallway.get_ySpawn());	
			henry.new_x(hallway.get_xSpawn());
		}else if (henry.get_loc() == location.wombo){
			henry.new_y(wombo.get_ySpawn());	
			henry.new_x(wombo.get_xSpawn());
		}
 	}
 	
 	public static void main(String[] args) throws SlickException{
 		AppGameContainer app = new AppGameContainer(new vhat());	// Creates new "app" object
 		app.setDisplayMode(screen_width, screen_height, false);		// Sets the display mode
 		app.start();												// Runs the app.
 	}
 
 }
