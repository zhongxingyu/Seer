 package grid;
 
 import item.ChargedIdentityDisk;
 import item.ForceFieldGenerator;
 import item.IdentityDisk;
 import item.Item;
 import item.LightGrenade;
 import item.Teleporter;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 
 import obstacle.Orientation;
 import obstacle.Wall;
 import exception.InvalidMoveException;
 import exception.OutsideTheGridException;
 
 /**
  * A class of random grid builders
  * 
  * @invar 	The grid has proper dimensions
  * 			| gridHasProperDimensions()
  * 
  * @author 	Group 8
  * @version	May 2013
  */
 public abstract class RandomGridBuilder extends GridBuilder {
 	
 	/**
 	 * There is at least 1 wall.
 	 */
 	public final static int MIN_WALL_NUMBER = 1;
 	
 	/**
 	 * The area around a starting position where a light grenade must be placed.
 	 */
 	public final static Dimension LIGHT_GRENADE_AREA = new Dimension(5,5);
 	
 	/**
 	 * The area around a starting position where a identity disk must be placed.
 	 */
 	public final static Dimension IDENTITY_DISK_AREA = new Dimension(7,7);
 		
 	/**
 	 * At most 20% of the squares in the grid, rounded up to an integer value, is covered by a wall.
 	 */
 	public final static double MAX_SQUARES_WALL = 0.2;
 	
 	/**
 	 * The maximum difference in path length for picking up the charged identity disk.
 	 */
 	public final static int CHARGED_DISC_MAX_PATH_DIFFERENCE = 2;	
 	
 	/**
 	 * 2% of the squares contain a light grenade.
 	 */
 	public final static double SQUARES_LIGHT_GRENADE = 0.02;
 	
 	/**
 	 * 2% of the squares contain an identity disk
 	 */
 	public final static double SQUARES_IDENTITY_DISK = 0.02;
 	
 	/**
 	 * 3% of the squares contain a teleporter
 	 */
 	public final static double SQUARES_TELEPORTER = 0.03;
 	
 	/**
 	 * 7% of the squares contain a force field generator
 	 */
 	public final static double SQUARES_FORCE_FIELD_GENERATOR = 0.07;
 	
 	/**
 	 * Returns the maximum horizontal wall length.
 	 * 
 	 * @return 	50% of the horizontal size of the grid, rounded up to an integer value. 
 	 */
 	public int getMaxHorizontalWallLength() {
 		return (int) Math.round(getGrid().getDimension().getX() * Wall.MAX_WALL_LENGTH_PERCENTAGE);
 	}
 	
 	/**
 	 * Returns the maximum vertical wall length.
 	 * 
 	 * @return 	50% of the vertical size of the grid, rounded up to an integer value. 
 	 */
 	public int getMaxVerticalWallLength() {
 		return (int) Math.round(getGrid().getDimension().getY() * Wall.MAX_WALL_LENGTH_PERCENTAGE);
 	}
 	
 	/**
 	 * Return the maximum length that a wall can have for the given orientation.
 	 * 
 	 * @param 	orientation
 	 * 			The orientation of the wall.
 	 * @return 	The maximum length of a wall in the grid based on the orientation of the wall.
 	 */
 	public int getMaxWallLength(Orientation orientation)
 	{
 		if (orientation == Orientation.HORIZONTAL)
 			return getMaxHorizontalWallLength();
 		else
 			return getMaxVerticalWallLength();		
 	}
 	
 	/**
 	 * @return	The minimal wall length.
 	 */
 	public int getMinWallLength() {
 		return Wall.MIN_WALL_LENGTH;
 	}
 	
 	/**
 	 * Return if a wall can be build on a given square in a given orientation.
 	 * 
 	 * @param 	square
 	 * 			The square to be checked if a wall can be build on it.
 	 * @param 	o
 	 * 			The orientation of the potential wall.
 	 * @param	builtWalls
 	 * 			A list of walls that are already built.
 	 * @return	True if a wall can be build on the given square in the given orientation, false if:
 	 * 			- That wall would cover the starting position of a player.
 	 * 			- There already is an obstacle on the given square.
 	 * 			- The wall would touch or intersect with another wall.
 	 */
 	public boolean canHaveWall(Square square, Orientation o, ArrayList<Wall> builtWalls) {
 		// The square cannot have an obstacle
 		if(square.hasObstacle()) return false; 
 		// A wall cannot cover the starting position of a player.
 		
 		if(square instanceof StartingPosition) return false;
 				
 		/* Check for possible touching or intersection of walls.
 		boolean result = false;*/
 		Direction[] directions = Direction.values();
 		// There are at most 8 neighbours.
 		for (Direction d: directions)
 		{
 			try {
 				Iterator<Wall> iterator = builtWalls.iterator();
 				Square neighbour = square.getNeighbour(d);
 				// No walls built so far.
 				if (builtWalls.isEmpty()){
 					continue;
 				}
 				else {
 					while (iterator.hasNext())
 					{
 						Wall wall = iterator.next();
 						if(wall.coversSquare(neighbour)){
 							return false;
 						}
 						
 					}
 				}
 			} catch (OutsideTheGridException e) {
 				continue;
 			}
 		}	
 		return true;
 	}
 
 	/**
 	 * Find and return (if any found) a free square in this grid.
 	 * 
 	 * @param 	x1
 	 * 			Width lower bound.
 	 * @param 	x2
 	 * 			Width upper bound.
 	 * @param 	y1
 	 * 			Height lower bound.
 	 * @param 	y2
 	 * 			Height upper bound.
 	 * 
 	 * @return	A free square in a certain range in this grid used to place items.
 	 */
 	protected Square findFreeSquare(int x1, int x2, int y1, int y2) {
 		Boolean found = false;
 		Random generator = new Random();
 		Square freeSquare = null;
 		
 		while(!found) {
 			int x = generator.nextInt(x2 - x1 + 1) + x1;
 			int y = generator.nextInt(y2 - y1 + 1) + y1;
 			try {
 				freeSquare = getGrid().getSquareAtCoordinate(new Coordinate(x,y));
 				found = true;
 			} catch (OutsideTheGridException e) {
				throw new AssertionError("Continue the search for a free square");
 			}
 		}
 		return freeSquare; // Because of the max 20% boundary there will always be a free square left (not null).	
 	}
 
 	/**
 	 * Return a random square in the area with given width around the given starting position.
 	 * 
 	 * @param 	startingCoordinate 
 	 * 			The coordinate around which to build the area.
 	 * @param 	width 
 	 * 			The width of the area.
 	 * @pre 	The area is smaller than the area of the grid.
 	 * @pre 	The given width is uneven.
 	 * @return 	A random square in an area around the starting position.
 	 */
 	protected Square getRandomSquareInStartingArea(Coordinate startingCoordinate, Dimension area){	
 		int width = area.getX();
 		
 		assert width % 2 == 1;
 		assert width*width <= getGrid().getDimension().getX()*getGrid().getDimension().getY();
 		ArrayList<ArrayList<Square>> rings = new ArrayList<ArrayList<Square>>();
 		int currentRing = 0;
 		rings.add(new ArrayList<Square>());
 
 		Square start = null;		
 		try {
 			start = (Square) getGrid().getSquareAtCoordinate(startingCoordinate);
 			rings.get(currentRing).add(start);
 		} catch (OutsideTheGridException e1) {
 			throw new AssertionError("Starting coordinates are inside the grid, " +
 					"no matter what.");
 		}
 		
 		HashSet<Square> squares = new HashSet<Square>();
 		
 		while(2*currentRing+1 < width){
 			rings.add(new ArrayList<Square>());
 			for(Square s: rings.get(currentRing)){
 				for(Direction d: Direction.values()){
 					try{
 						Square extension = (Square) getGrid().getSquareAtDirection(s, d);
 						if(!squares.contains(extension)){
 							rings.get(currentRing+1).add(extension);
 						}
 						squares.add(extension);
 
 					}
 					catch(OutsideTheGridException e){
 						continue;
 					}
 				}
 			}
 			currentRing++;
 		}
 		
 		Random rg = new Random();
 		
 		squares.remove(start);
 		
 		Square[] squareArray = squares.toArray(new Square[squares.size()]);
 		return squareArray[rg.nextInt(squareArray.length)];	
 	}
 
 	/**
 	 * Place an amount of light grenades on 2% of the squares on this grid.
 	 * The placement of the light grenades is however constrained.
 	 * 
 	 * @post 	2 percent of the squares is covered with a light grenade.
 	 * @post 	The 5x5 area surrounding the starting positions contain at least one light grenade.
 	 */
 	@Override
 	public void placeLightGrenades() {
 		// 2% of the squares contain a light grenade.
 		int numberOfGrenades = (int) Math.round(getGrid().getDimension().getX() * getGrid().getDimension().getY() * SQUARES_LIGHT_GRENADE);	
 		
 		placeItemInRandomArea(LIGHT_GRENADE_AREA, new LightGrenade());
                      
 		numberOfGrenades--;
 		if(numberOfGrenades <= 0) return;
 
 		//place the rest of the light grenades
 		while(numberOfGrenades != 0){
 			Square s = findFreeSquare(1, getGrid().getDimension().getX(), 1, getGrid().getDimension().getY());
 			LightGrenade lg = new LightGrenade();
 			if(lg.canInitializeOnSquare(s)){
 				s.addItem(new LightGrenade());
 				numberOfGrenades--;
 			}
 		}	
 	}
 	
 	/**
 	 * Place an amount of force field generators on 7% of the squares on this grid.
 	 * The placement of the force field generators is however constrained.
 	 * 
 	 * @post 	7 percent of the squares is covered with a light grenade.
 	 */
 	public void placeForceFieldGenerators() {
 		// 7% of the squares contain a force field generator.
 		int numberOfForceFieldGenerators = (int) Math.round(getGrid().getDimension().getX() * getGrid().getDimension().getY() * SQUARES_FORCE_FIELD_GENERATOR);
 		
 		//place the force field generators
 		while(numberOfForceFieldGenerators != 0){
 			Square s = findFreeSquare(1, getGrid().getDimension().getX(), 1, getGrid().getDimension().getY());
 			ForceFieldGenerator ffg = new ForceFieldGenerator();
 			if(ffg.canInitializeOnSquare(s)){
 				s.addItem(ffg);
 				numberOfForceFieldGenerators--;
 			}
 		}
 		
 	}
 	
 	/**
 	 * Place the teleporters on the grid
 	 * 
 	 * @post 	3 percent of the squares is covered with a teleporter
 	 * @post 	Each teleporter has another random teleporter as its destination
 	 */
 	@Override
 	public void placeTeleporters() {	
 		// 3% of the squares contain a light grenade.
 		int numberOfTeleporters = (int) Math.round(getGrid().getDimension().getX() * getGrid().getDimension().getY() * SQUARES_TELEPORTER);
 		
 		//Create the teleporters
 		Random rg = new Random();
 		ArrayList<Teleporter> teleporters = new ArrayList<Teleporter>();
 		for(int i = 1;i<=numberOfTeleporters;i++){
 			Teleporter tp = new Teleporter(null);
 			teleporters.add(tp);
 		}
 		
 		for(Teleporter t: teleporters){
 			Square s = null;
 			do {
 				s = findFreeSquare(1, getGrid().getDimension().getX(), 1, getGrid().getDimension().getY());
 			} while(s == null || !t.canInitializeOnSquare(s));
 			s.addItem(t);
 			
 		}	
 		for(Teleporter t: teleporters){
 			int destinationIndex;	
 			do {
 				destinationIndex = rg. nextInt(numberOfTeleporters);
 			} while(destinationIndex == teleporters.indexOf(t));
 			
 			t.setDestination(teleporters.get(destinationIndex));
 		}
 			
 	}
 
 	/**
 	 * Place the identity disks on the grid.
 	 * 
 	 * @post 	2 percent of the squares is covered with an identity disk.
 	 * @post 	The 5x5 area surrounding the starting positions contain at least one identity disk.
 	 */
 	@Override
 	public void placeIdentityDisks() {
 		// 2% of the squares contain an identity disk.
 		int numberOfIdentityDisks = (int) Math.round(getGrid().getDimension().getX() * getGrid().getDimension().getY() * SQUARES_IDENTITY_DISK);
 		
 		placeItemInRandomArea(IDENTITY_DISK_AREA,new IdentityDisk());
 		numberOfIdentityDisks--;	
 		if(numberOfIdentityDisks <= 0)
 			return;
 				
 		//place the rest of the identity disks
 		while(numberOfIdentityDisks != 0){
 			Square s = findFreeSquare(1, getGrid().getDimension().getX(), 1, getGrid().getDimension().getY());
 			IdentityDisk id = new IdentityDisk();		
 			if(s.canHaveAsItem(id)){
 				s.addItem(new IdentityDisk());
 				numberOfIdentityDisks--;
 			}
 		}
 		
 	}
 	
 	/**
 	 * In the area x area in the grid the given item is placed.
 	 * 
 	 * @param area
 	 * @param item
 	 * @effect	Item placed at random location in area x area.
 	 * 			| s = getRandomSquareInStartingArea(c,area);
 	 * 			| s.addItem(item);
 	 */
 	protected void placeItemInRandomArea(Dimension area, Item item) {
 		Collection<Coordinate> startingCoordinates = getGrid().getStartingPositions().values();
 		for(Coordinate c: startingCoordinates){			
 			Square s = null;	
 			do{
 				s = getRandomSquareInStartingArea(c,area);
 			} while(s == null || !item.canInitializeOnSquare(s));
 			s.addItem(item);
 		}	
 	}
 
 	/**
 	 * Return the potential locations of a charged identity disk.
 	 * 
 	 * @return 	An array list of potential locations (those which are reachable
 	 * 			from any starting position and for which the shortest path
 	 * 			from a starting position does not differ more than 2 squares
 	 * 			from any other starting position)	
 	 */
 	protected ArrayList<Square> getPotentialChargedIdentityDiskLocations() {
 		//a list to keep potential squares
 		ArrayList<Square> potentialSquares = new ArrayList<Square>();
 		
 		Iterator<Square> it = getGrid().iterator();
 		
 		// The difference between the shortest and longest path to a potential square
 		int pathDifference = 0;
 		loop:
 		while(it.hasNext()){
 			Square potentialSquare = it.next();
 			
 			if((new ChargedIdentityDisk()).canInitializeOnSquare(potentialSquare)) continue;			
 			
 			// Keep a list of distances from a starting position to the square s
 			ArrayList<Integer> distances = new ArrayList<Integer>();
 			
 			Collection<Coordinate> startingCoordinates = getGrid().getStartingPositions().values();
 			
 			for(Coordinate c: startingCoordinates){
 				Square startingSquare = null;
 				try {
 					startingSquare = getGrid().getSquareAtCoordinate(c);
 				} catch (OutsideTheGridException e) {
 					throw new AssertionError("Cannot be outside the grid.");
 				}
 				try {
 					int distance = getGrid().getPathDistance(startingSquare, potentialSquare);
 					distances.add(distance);
 				} catch (InvalidMoveException e) {
 					/* 
 					 * some player cannot reach potential square, 
 					 * continue with the next potential square.
 					 */
 					continue loop;
 				}				
 			}
 			
 			pathDifference = Math.abs(Collections.max(distances)-Collections.min(distances));
 			
 			/*
 			 * Add the potential square to the list if the distance between the shortest path
 			 * and the longest path from a starting position differs at most 2 squares
 			 */
 			if(pathDifference <= CHARGED_DISC_MAX_PATH_DIFFERENCE){
 				potentialSquares.add(potentialSquare);
 			}
 		}
 		return potentialSquares;
 	}
 
 	/**
 	 * Place a charged identity disk at a random square.
 	 * @post 	A charged identity disc is placed such that the length of the shortest path
 	 *			from each player to the disc differs by at most 2 squares. If such a location does
 	 *			not exist or the only possible location is a starting square, no disk is placed.
 	 */
 	@Override
 	public void placeChargedIdentityDisk(){
 		ArrayList<Square> potentialSquares = this.getPotentialChargedIdentityDiskLocations();
 		
 		if(potentialSquares.size() == 0) return;
 		
 		Random rg = new Random();
 		int squareIndex = rg.nextInt(potentialSquares.size());
 		Square square = potentialSquares.get(squareIndex);
 		square.addItem(new ChargedIdentityDisk());
 	}
 
 	/**
 	 * Spreads the power failures at random.
 	 * 
 	 * @effect	Set the power power failure spreader of the grid
 	 * 			| getGrid().setPowerFailureSpreader(new PowerFailureSpreader())
 	 * @effect Spread power failures on the grid
 	 * 			| getGrid().getPowerFailureSpreader().spread();
 	 */
 	@Override
 	public void spreadPowerFailures() {
 		getGrid().setPowerFailureSpreader(new PowerFailureSpreader());
 		getGrid().getPowerFailureSpreader().spread();
 	}
 	
 }
