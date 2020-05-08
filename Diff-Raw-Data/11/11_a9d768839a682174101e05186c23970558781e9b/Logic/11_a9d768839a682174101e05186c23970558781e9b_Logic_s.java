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
 	
 	// Boolean variables for movement and collision detection, location counter for the room
 	private boolean 	down, up, right, left;								//für die Bewegungsrichtungen
 	private boolean		north, east, south, west, northwest, northeast, southwest, southeast;		//zum schießen in die Himmelsrichtungen
 	
 	private boolean 	freeright, freeup, freedown, freeleft;
 	private double 		distDown, distUp, distRight, distLeft;
 	
 	private int 		location;
 	private GameObjects figure;
 	private O_Game		game;
 	
 	private double 		figX, figY;
 	private double 		figVX, figVY;
 	private boolean		punch, use, bomb;									//für Aktionen
 
 	
 	// List of all Objects within the game
 	private ArrayList<ArrayList<GameObjects>> 	rooms;
 	private ArrayList<GameObjects> 				currentRoom;
 
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
 		rooms 		= objectsInit;
 		figure 		= inFigure;
 		game		= inGame;
 
 		up 			= false;
 		down		= false;
 		right		= false;
 		left		= false;
 		
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
 		
 		freeright	= true;
 		freeleft	= true;
 		freeup		= true;
 		freedown	= true;
 	}
 	
 	private void setRoom(int newLocation) {
 		game.setRoom(newLocation);
 	}
 	
 	private void checkDistance() {
 		
 	}
 	
 	private void moveEnemy() {
 		// TODO Auto-generated method stub, wird erstmal leer bleiben, da wir noch keine KI haben
 	}
 	
 	private void checkCollision() {
 		freeright	= true;
 		freeleft	= true;
 		freeup		= true;
 		freedown	= true;
 		
 		double figR 	= figure.getRad();
 		double tmp;
 		
 		double objX;
 		double objY;
 		double objR;
 		
 		ArrayList<GameObjects> collidable = rooms.get(location);
 		
 		// TODO Auto-generated method stub, hier muss noch genau festgelegt werden, wie wir Kollisionen feststellen wollen
 		// iterate over all objects within the room, excepting the figure, of course
 		for(int i=1; i<collidable.size(); i++) {
 			objX = collidable.get(i).getPosX();
 			objY = collidable.get(i).getPosY();
 			objR = collidable.get(i).getRad();
 			System.out.println(collidable.get(i));
 			
 			// First check whether the objects are close enough to encounter one another within the next couple of moves, use squares, saves a couple of sqrt calls
 			if (((objX-figX)*(objX-figX)+(objY-figY)*(objY-figY)) < ((figR+objR)*(figR+objR))) {
 				
 				/* Reference point for all objects is the top left corner, as drawing exclusively starts here
 				 * First of all we check, whether the object in question is on a collidable course in x or y direction, 
 				 * once that is done, the remaining distance between the two objects will be computed and saved in a 
 				 * corresponding double value.
 				 * 
 				 *  Start with collisions in y-direction */
 				if((tmp=figX-objX) > -1 && tmp < 1) {
 					// check whether the object is to the left or right of the figure and whether the figure could reach it within one step
					if(((tmp=figY-objY) >= 1) && (figVY > tmp)) {
 						// set remaining distance and checking variable
 						distUp = tmp - 1;
 						freeup = false;
					} else if ((tmp <= -1) && (figVY > tmp)) {
 						distDown = -tmp - 1;
 						freedown = false;
 					}
 				} 
 				
 				// this will cover collision detection in x-direction, analogous to above
 				if((tmp=figY-objY) > -1 && tmp < 1) {
					if(((tmp=figX-objX) >= 1) && (figVX > tmp)) {
 						distLeft = tmp - 1;
 						freeleft = false;
					} else if ((tmp <= -1) && (figVX > tmp)) {
 						distRight = -tmp - 1;
 						freeright = false;
 					}
 				} 
 			}
 		}
 	}
 	
 	
 	/* This is the actual movement method, which checks for all directions whether the figure needs to be moved.
 	 * Additionally the method checks whether the figure has reached a boundary and will prevent it from moving out of the gaming area.  */	
 	private void moveFigure() {
 		if(right && !left){
 			if(freeright){
 				if (figX+figVX >= 21) 	figX = 21;
 				else					figX += figVX;
 			} else 						figX += distRight;
 		}
 		
 		if(left && !right){
 			if(freeleft) {				
 				if (figX-figVX <= 0) 	figX = 0;
 				else					figX -= figVX;
 			} else	 					figX -= distLeft;
 		}
 		
 		if(up && !down){
 			if(freeup) {
 				if (figY-figVY <= 0) 	figY = 0;
 				else					figY -= figVY;
 			} else						figY -= distUp;
 		}
 		
 		if(down && !up){
 			if(freedown) {
 				if (figY+figVY >= 12) 	figY = 12;
 				else					figY += figVY;
 			} else						figY += distDown;
 		}
 		
 		figure.setPos(figX, figY);
 	}
 	
 	@Override //Override run method from interface, this will have the game loop
 	public void run() {
 		currentRoom = rooms.get(0);
 		long time;
 		long temp;
 		int count;
 		
 		// game loop
 		while (true) {
 			time = System.currentTimeMillis();
 
 			figX 	= figure.getPosX();
 			figY 	= figure.getPosY();
 			figVX	= figure.getVX();
 			figVY	= figure.getVY();
 			
 			// diagonal velocity is slowed, so that diagonal and straight movement seem to have the same speed.  
 			count = 0;
 			if(up)		count++;
 			if(down)	count++;
 			if(right)	count++;
 			if(left)	count++;
 			
 			// if exactly two key are pressed the figure either doesn't move (opposing key) or it moves diagonal			
 			if(count == 2) {
 				figVX /= SQRT_2;
 				figVY /= SQRT_2;
 			}
 			
 			// do the actual logic in this game
 			this.checkDistance();
 			this.checkCollision();
 			this.moveFigure();
 			this.moveEnemy();
 			
 			// set the thread asleep, we don't need it too often
 			try {
 				if((temp=System.currentTimeMillis()-time) < 16)
 				Thread.sleep(16-temp);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
