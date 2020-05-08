 package hhu.propra_2013.gruppe_13;
 
 import java.util.ArrayList;
 
 
 /*
  * Ablauf: 	Logik bekommt die Bewegungsbefehle von der IO
  * 			Logik kennt die (Liste) der Objekte
  * 			(Gegner werden berechnet)
  * 			Kollisionsabfrage
  * 			Logik gibt den Objekten ihre neuen Positionen
  */
 
 
 class Logic implements Runnable {
 	
 	private static final double SQRT_2 = 	1.41421356237309504880168872420969807856967187537694807317667973799;	// http://en.wikipedia.org/wiki/Square_root_of_2
 	private boolean gameRunning;
 	// Boolean variables for movement and collision detection, location counter for the room
 	private boolean 	down, up, right, left, upLeft, upRight , downLeft, downRight;				//für die Bewegungsrichtungen
 	private boolean		north, east, south, west, northwest, northeast, southwest, southeast;		//zum schießen in die Himmelsrichtungen
 	
 	private boolean 	freeRight, freeUp, freeDown, freeLeft;
 	private double 		distDown, distUp, distRight, distLeft;
 	
 	private int 		location = 0;
 	private GameObjects figure;
 	private O_Game		game;
 	
 	private double 		figX, figY;
 	private double 		figVX, figVY;
 	private boolean		punch, use, bomb;									//für Aktionen
 	private int			figHP;
 	
 	// List of all Objects within the game
 	private ArrayList<ArrayList<GameObjects>> 	rooms;
 	private ArrayList<GameObjects> 				currentRoom;
 
 	void setGameRunning(boolean boolIn){
 		gameRunning = boolIn;
 		System.out.println("maria wars");
 	}
 
 	// Setter methods to determine whether a movement shall be initiated 
 	void setDown(boolean in){
 		down = in;
 	}
 	
 	void setUp(boolean in){
 		up = in;
 	}
 
 	void setRight(boolean in){
 		right = in;
 	}
 	
 	void setLeft(boolean in){
 		left = in;
 	}
 
 	void setUpRight(boolean in) {
 		upRight = in;
 	}
 
 	void setUpLeft(boolean in) {
 		upLeft = in;
 	}
 
 	void setDownRight(boolean in) {
 		downRight = in;		
 	}
 
 	void setDownLeft(boolean in) {
 		downLeft = in;	
 	}
 	
 	void setPunch(boolean in) {
 		punch = in;		
 	}
 	
 	void setUse(boolean in) {
 		use = in;		
 	}
 	
 	void setBomb(boolean in) {
 		bomb = in;		
 	}
 	
 	void setNorth(boolean in) {
 		north = in;		
 	}
 	
 	void setEast(boolean in) {
 		east = in;		
 	}
 	
 	void setSouth(boolean in) {
 		south = in;		
 	}
 	
 	void setWest(boolean in) {
 		west = in;		
 	}
 	
 	void setNorthwest(boolean in) {
 		northwest = in;		
 	}
 	
 	void setNortheast(boolean in) {
 		northeast = in;		
 	}
 	
 	void setSouthwest(boolean in) {
 		southwest = in;		
 	}
 	
 	void setSoutheast(boolean in) {
 		southeast = in;		
 	}
 
 	// Initiate the current objects variables
 	Logic(ArrayList<ArrayList<GameObjects>> objectsInit, Figure inFigure, O_Game inGame) {
 		gameRunning = true;
 		rooms 		= objectsInit;
 		figure 		= inFigure;
 		game		= inGame;
 
 		up 			= false;
 		down		= false;
 		right		= false;
 		left		= false;
 		
 		upLeft		= false;
 		upRight 	= false;
 		downLeft	= false;
 		downRight	= false;
 		
 		punch		= false;
 		use			= false;
 		bomb		= false;
 		
 		north		= false;
 		east		= false;
 		south		= false;
 		west		= false;
 		
 		northwest	= false;
 		northeast	= false;
 		southwest	= false;
 		southeast	= false;
 		
 		freeRight	= true;
 		freeLeft	= true;
 		freeUp		= true;
 		freeDown	= true;
 	}
 	
 	private void setRoom(int newLocation) {
 		game.setRoom(newLocation);
 		location = newLocation;
 	}
 	
 	private void checkDistance() {
 		
 	}
 	
 	private void moveEnemy() {
 		// TODO Auto-generated method stub, wird erstmal leer bleiben, da wir noch keine KI haben
 	}
 	
 	private void checkCollision() {
 		// reset collision values
 		freeRight		= true;
 		freeLeft		= true;
 		freeUp			= true;
 		freeDown		= true;
 		
 		// reset distance values
 		distUp			= 50;
 		distDown		= 50;
 		distLeft		= 50;
 		distRight		= 50;
 		
 		// method variables for 
 		double figR 		= figure.getRad();
 		double figWidth 	= figure.getWidth();
 		double figHeight 	= figure.getHeight();
 		double tmpX, tmpY;
 		
 		double objX, objY;
 		double objR;
 		double objWidth, objHeight;
 		
 		ArrayList<GameObjects> collidable = rooms.get(location);
 		
 		// iterate over all objects within the room, excepting the figure, of course
 		for(int i=1; i<collidable.size(); i++) {
 			objX 		= collidable.get(i).getPosX();
 			objY 		= collidable.get(i).getPosY();
 			objR 		= collidable.get(i).getRad();
 			
 			objWidth 	= collidable.get(i).getWidth();
 			objHeight 	= collidable.get(i).getHeight();
 			
 			// First check whether the objects are close enough to encounter one another within the next couple of moves, use squares, saves a couple of sqrt calls
 			if (((objX-figX)*(objX-figX)+(objY-figY)*(objY-figY)) < ((figR+objR)*(figR+objR))) {
 				tmpX = figX-objX;
 				tmpY = figY-objY;
 				
 				/* Reference point for all objects is the top left corner, as drawing exclusively starts here
 				 * First of all we check, whether the object in question is on a collidable course in x or y direction, 
 				 * once that is done, the remaining distance between the two objects will be computed and saved in a 
 				 * corresponding double value.
 				 * 
 				 *  Start with collisions in y-direction */
 				if(Math.abs(tmpX) < (figWidth+objWidth)/2.) {
 					// check whether the object is to the left or right of the figure and whether the figure could reach it within one step
 					if(((tmpY) > 0) && (figVY > (tmpY-(figHeight+objHeight)/2.))) {
 						// set remaining distance and checking variable
 						distUp = Math.min(distUp, tmpY - (figHeight+objHeight)/2.);
 						freeUp = false;
 					} else if ((tmpY < 0) && (figVY > (-tmpY-(figHeight+objHeight)/2.))) {
 						distDown = Math.min(distDown, -tmpY - (figHeight+objHeight)/2.);
 						freeDown = false;
 					}
 				}
 				
 				// this will cover collision detection in x-direction, analogous to above
 				if(Math.abs(tmpY) < (figHeight+objHeight)/2.) {
 					if((tmpX > 0) && (figVX > tmpX-(figWidth+objWidth)/2.)) {
 						distLeft = Math.min(distLeft, tmpX - (figWidth+objWidth)/2.);
 						freeLeft = false;
 					} else if ((tmpX < 0) && (figVX > -tmpX-(figWidth+objWidth)/2.)) {
 						distRight = Math.min(distRight, -tmpX - (figWidth+objWidth)/2.);
 						freeRight = false;
 					}
 				}
 			}
 		}
 	}
 	
 	private void setDirection() {
 		
 	}
 	
 	/* This is the actual movement method, which checks for all directions whether the figure needs to be moved.
 	 * Additionally the method checks whether the figure has reached a boundary and will prevent it from moving out of the gaming area.  */	
 	private void moveFigure() {
 		// convert diagonal movement into two horizontal components, diagonal movement is slowed,
 		// as it will take place into two directions
 		if (upLeft) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			left 	= true;
 			up		= true;
 		}
 		
 		if (upRight) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			right 	= true;
 			up		= true;
 		}
 		
 		if (downLeft) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			left 	= true;
 			down	= true;
 		}
 		
 		if (downRight) {
 			figVX	/= SQRT_2;
 			figVY 	/= SQRT_2;
 			right 	= true;
 			down	= true;
 		}
 		
 		// horizontal and vertical movement handlers, these also try to find out, whether there is anything
 		// within a diagonal direction inhibiting movement. 
 		if(right) {
 			if(freeRight){
 				if (figX+figVX >= 21.5)		figX  = 21.5;
 				else						figX += figVX;
 			} else 							figX += distRight;
 		}
 		
 		if(left) {
 			if(freeLeft) {
 				if (figX-figVX <= 0.5) 		figX  = 0.5;
 				else						figX -= figVX;
 			} else							figX -= distLeft;
 		}
 
 		if(up) {
 			if(freeUp) {
 				if (figY-figVY <= 0.5) 		figY  = 0.5;
 				else						figY -= figVY;
 			} else							figY -= distUp;
 		}
 		
 		if(down) {
 			if(freeDown) {
 				if (figY+figVY >= 12.5) 		figY  = 12.5;
 				else							figY += figVY;
 			} else								figY += distDown;
 		}
 		
 		// finally set the position of the figure
 		figure.setPos(figX, figY);
 	}
 	private void checkFigure(){
 		if(figHP <= 0){
 			game.end();
 		}
 	}
 	
 	private void switchRoom(double inX,double inY,int inlocation){ 
 	//wechselt den Raum, falls die Figur an einer Stelle steht an der im aktuellen Raum eine Tür ist
		System.out.println(location);
 		switch (inlocation){ //prüft in welchem Raum die Figur ist (bisher 0-2 für die 3 Räume)
 
 		case(0)://erster Raum: eine Tür rechts mittig
 			if (inX == 21 && (int)inY == 6){
 				location++; //einen Raum nach rechts
 				figX = 1; //Figur (fast) an rechten Rand, ohne die Tür nach links im nächsten Raum auszulösen
 				this.setRoom(location);//neuen Raum festlegen
 			}
 		break; //vorgehen für alle Fälle analog
 		
 		case(1): //zweiter Raum: je rechts und links mittig eine Tür
 			if (inX == 21 && (int)inY == 6){
 				location++;
 				figX = 1;
 				this.setRoom(location);
 			}
 		
 			if (inX == 0 && (int)inY == 6){
 				location--;
 				figX = 20;
 				this.setRoom(location);
 			}
 		case(2): //dritter Raum: eine Tür rechts mittig
 			if (inX == 0 && (int)inY == 6){
 				location--;
 				figX = 20;
 				this.setRoom(location);
 			}
 	
 	}
 
 	
 	
 	}
 	@Override //Override run method from interface, this will have the game loop
 	public void run() {
 		currentRoom = rooms.get(0);
 		long time;
 		long temp;
 		
 		// game loop
 		while (gameRunning) {
 			time = System.currentTimeMillis();

 			// get current figure positions and velocities
 			figX 	= figure.getPosX();
 			figY 	= figure.getPosY();
 			figVX	= figure.getVX();
 			figVY	= figure.getVY();
 			figHP	= figure.getHP();
 			
 			this.switchRoom(figX, figY, location);
 			
 			// do the actual logic in this game
 			this.checkDistance();
 			this.checkCollision();
 			this.moveFigure();
 			this.moveEnemy();
 			this.checkFigure();
 			
 			// set the thread asleep, we don't need it too often
 			try {
 				if((temp=System.currentTimeMillis()-time) < 16)
 				Thread.sleep(16-temp);
 			} catch (InterruptedException e) {
 				// don't care if the thread is interrupted
 			}
 		}
 	}
 }
