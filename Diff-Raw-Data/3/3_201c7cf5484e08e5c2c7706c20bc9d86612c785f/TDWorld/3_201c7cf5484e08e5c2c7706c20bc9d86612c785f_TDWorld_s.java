 /*
  * TDWorld.java
  * The TDWorld class creates a grid and manages actors, lives, and attacks. 
  * This is a tower defense game in which the user places barricades and towers to try and stop minions from reaching the end square.
  * @author Edward Yu, Ronbo Fan
  * Period: 6
  * Date: 5/19/13
  * 
  */
 package td;
 
 import info.gridworld.grid.*;
 import info.gridworld.world.*;
 import info.gridworld.actor.*;
 
 import java.awt.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 
 public class TDWorld extends World<Actor>
 {    
     private static String DEFAULT_MESSAGE = "Welcome to Super TD!";
     
     public static final boolean DEBUG = false;
     
     public static boolean cheats = false;
     
     public final Location startLoc;
     public final Location endLoc;
     
     public static String lastAdded;
     
     public int hp;
     public int gold;
     
     public Actor nextToAdd;
     public boolean gameOver;
     
     private boolean gameStarted;
     
     private int timer;
     private int level = 1;
     private int minionsAdded = 0;
     
     public Graphics2D g2;
     
     private HashSet<Location> open; //list of possible locations to check 
     private HashSet<Location> closed; //list of checked locations
     private Map<Location, Integer> fcosts; //overall distance from beginning to end
     private Map<Location, Integer> gcosts; //distance from beginning to location
     private Map<Location, Integer> hcosts; //distance from end to location
     private Map<Location, Location> parents;
     
     /*
      * Easier way to print than System.out.println()
      * @param o the object to print out.
      */
     public void db(Object o) {
     	System.out.println(o.toString());
     }
     
     /*
      * Constructs a TDWorld and sets the default start locations and end locations
      * Start is bottom left corner, end is top right corner
      */
     public TDWorld()
     {
         startLoc = new Location(getGrid().getNumRows() - 1, 0);
         endLoc = new Location(0, getGrid().getNumCols() - 1);
         load();
         
         open = new HashSet<Location>();
         closed = new HashSet<Location>();
         fcosts = new HashMap<Location, Integer>();
         gcosts = new HashMap<Location, Integer>();
         hcosts = new HashMap<Location, Integer>();
         parents = new HashMap<Location, Location>();
     }
     
     /*
      * Constructs a TDWorld 
      * @param loc1 the start location
      * @param loc2 the end location
      */
     public TDWorld(Location loc1, Location loc2) {
     	startLoc = loc1;
     	endLoc = loc2;
     	load();
         
         open = new HashSet<Location>();
         closed = new HashSet<Location>();
         fcosts = new HashMap<Location, Integer>();
         gcosts = new HashMap<Location, Integer>();
         hcosts = new HashMap<Location, Integer>();
         parents = new HashMap<Location, Location>();
     } 
 
     /*
      * Constructs a TDWorld
      * @param grid a grid that the game will be played on
      */
     public TDWorld(Grid<Actor> grid)
     {
         super(grid);
         startLoc = new Location(getGrid().getNumRows() - 1, 0);
         endLoc = new Location(0, getGrid().getNumCols() - 1);
         load();
         
         open = new HashSet<Location>();
         closed = new HashSet<Location>();
         fcosts = new HashMap<Location, Integer>();
         gcosts = new HashMap<Location, Integer>();
         hcosts = new HashMap<Location, Integer>();
         parents = new HashMap<Location, Location>();
     }
     
     /*
      * Loads the default settings for a game.
      */
     public void load() {
     	gameStarted = false;
     	timer = 10; //game starts in 10 steps
         level = 1;
     	gameOver = false;
     	gold = 50;
     	hp = 20;
     	add(startLoc, new Shade(this));
     	add(endLoc, new Shade(this));
     }
     
     /*
      * Enables cheat mode, where objects can be added to the grid without retyping the type every time. 
      */
     public void cheater() {
     	cheats = !cheats;
     }
     
     /*
      * Subtracts gold from the stockpile
      * @param toTake the amount to subtract
      */
     public void takeGold(int toTake) {
     	gold -= toTake;
     }
     
     /*
      * Adds gold the stockpile
      * @param toAdd the amount to add
      */
     public void addGold(int toAdd) {
     	gold += toAdd;
     }
     
     /*
      * Gets the amount of gold in the stockpile
      * @return the amount of gold 
      */
     public int getGold() {
     	return gold;
         
     }
     
     /*
      * prints the amount of gold the user has
      */
     public void printGold()
     {
         System.out.println("You currently have " + gold + " gold.");
     }
     
     /*
      * Takes a life away from the player.
      * Stops the game if the players has no lives left.
      */
     public void loseLife() {
     	hp--;
     	System.out.println("You have lost a life! You now have " + hp + " lives.");
     	if(hp == 0) {
     		gameOver = true;
     		System.out.println("GAME OVER! You lost!");
     	}
     }
     
     /*
      * Gets the HP that each minion should have, based on the level
      * @return recommened HP for minion
      */
     public int getHP()
     {
         return level * level * 5;
     }
     
     /*
      * The number of minions in a level
      * @return number of minions, dependant on level
      */
     public int getNumMinions()
     {
         return 2 * level;
     }
     
     /*
      * The gold bonus for completing a level
      * @return gold bonus, dependent on level
      */
     public int getGoldBonus()
     {
         return 5 * level;
     }
     
     /*
      * Takes the users input and stores it so the object can be added.
      * @param s the name of the object to the added (barricade, basictower, etc.)
      */
     public void nextType(String s) {
     	switch(s) {
     		case "barricade":
     			nextToAdd = new Barricade(this);
     		break;
     		case "firetower":
     			nextToAdd = new FireTower(this);
     		break;
     		case "watertower":
     			nextToAdd = new WaterTower(this);
     		break;
     		case "magetower":
     			nextToAdd = new MageTower(this);
     		break;
     		case "moneyhut":
     			nextToAdd = new MoneyHut(this);
     		break;
     		case "basictower":
     			nextToAdd = new BasicTower(this);
     		break;
     		case "minion":
     			nextToAdd = new Minion(startLoc, endLoc, this);
     		break;
     		default:
     			System.out.println("error 1");
     		break;
     	}
     	lastAdded = s;
     }
 
     /*
      * Displays the world and default message
      */
     public void show()
     {
         if (getMessage() == null)
             setMessage(DEFAULT_MESSAGE);
         super.show();
     }    
     	
     /**
      * This method is called when the user clicks on a location in the
      * WorldFrame.
      * It adds an object to the world if the user has specified one.
      * @param loc the grid location that the user selected
      * @return true if the world consumes the click, or false if the GUI should
      * invoke the Location->Edit menu action
      */
     public boolean locationClicked(Location loc)
     {
         if(getGrid().get(loc) instanceof Barricade)
         {
             gold += ((Barricade) getGrid().get(loc)).getCost();
             remove(loc);
             printGold();
         }
         
     	if(nextToAdd == null)
     		return true;
         if(!isValidPlacement(loc))
         {
             System.out.println("Sorry, you can't place objects to completely block the end path.");
             return true;
         }
         
         if(!(nextToAdd instanceof Minion) && ((Barricade)nextToAdd).getCost() <= gold) 
         {
         	gold -= ((Barricade)nextToAdd).getCost();
 	        System.out.println("You now have " + gold + " gold."); 
 	    	add(loc, nextToAdd);
         } 
         else if (nextToAdd instanceof Minion)
         {
                 if(cheats)
                     add(loc, nextToAdd);
     	} 
         else 
         {
         	System.out.println("Sorry, but you must have " + ((Barricade)nextToAdd).getCost() + " gold to build this structure!");
         }
         //nextToAdd.putSelfInGrid(getGrid(), loc);
         
     	if(!cheats) {
     		nextToAdd = null;
     	} else {
     		nextType(lastAdded);
     	}
         
     	return true;
         //return false;
     }
 
 	/*
 	 * Prints the instructions for playing the game
 	 */
     public void instructions() 
     {
        System.out.println("Welcome to GridDefense!");
        System.out.println("Wave 1 will begin when you press Run.");
        System.out.println("The speed of each step may be adjusted in the Run Speed slider below.");
        printGold();
     }
 
     /*
      * Makes each actor in the world act. Towers and barricades are processed first, then minions.
      */
     public void step()
     {
     	if(!gameStarted) {
         	gameStarted = true;
     	}
     	if(timer > 0) {
     		timer--;
     		return;
     	}
     	if(gameOver) {
     		System.out.println("Game over. Please reload Super TD to start a new game.");
     		return;
     	}
         Grid<Actor> gr = getGrid();
         ArrayList<Actor> minions = new ArrayList<Actor>();
         for (Location loc : gr.getOccupiedLocations())
             minions.add(gr.get(loc));
 		
 		ArrayList<Actor> towers = (ArrayList<Actor>)minions.clone();
         for (Actor a : towers)
         {
             if (a.getGrid() == gr && a instanceof Barricade)
                 a.act();
         }
         		
         for (Actor a : minions)
         {
             if (a.getGrid() == gr && a instanceof Actor)
                 a.act();
         }
         if(gr.get(startLoc) == null)
     		add(startLoc, new Shade(this));
         if(gr.get(endLoc) == null)
     		add(endLoc, new Shade(this));
     	/*if not all minions have been spawned*/
         if(minionsAdded < getNumMinions()) {
                 Minion m =  new Minion(startLoc, endLoc, this);
                 m.setHP(getHP());
         	add(startLoc, m);
         	minionsAdded++;
         }
         boolean allDead = true;
         for(Location loc : gr.getOccupiedLocations()) {
         	if(gr.get(loc) instanceof Minion)
         		allDead = false;
         }
         if(allDead) {
         	gold += getGoldBonus();
         	timer = 10;
         	level++;
         	minionsAdded = 0;
                
         	System.out.println("Congratulations! Wave " + (level) + " will commence when you press Run.");
                 System.out.println("Minions: " + 2 * level);
                 System.out.println("Minion health: " + level * level * 2);
                 
                 printGold();
         }
     }
 
     /*
      * Adds an actor to the grid
      * @param loc the location which the actor will be added to
      * @param occupant the actor to add
      *
      */
     public void add(Location loc, Actor occupant)
     {
         occupant.putSelfInGrid(getGrid(), loc);
     }
 
     /*
      * Adds an actor to a random location
      * @param occupant the actor to add
      */
     public void add(Actor occupant)
     {
         Location loc = getRandomEmptyLocation();
         if (loc != null)
             add(loc, occupant);
     }
 
     /*
      * Remove an actor from the grid
      * @param loc the location from which to remove the actor
      * @return the actor which was removed
      */
     public Actor remove(Location loc)
     {
         Actor occupant = getGrid().get(loc);
         if (occupant == null)
             return null;
         occupant.removeSelfFromGrid();
         return occupant;
     }
     
     /*
      * Don't allow the user to place an object so that the path from start to end is completely blocked.
      * @param test the location to check for validity
      * @return true if it doesn't block the path, false otherwise
      */
     public boolean isValidPlacement(Location test)
     {
         //make sure all hashmaps and hashsets are empty
         open = new HashSet<Location>();
         closed = new HashSet<Location>();
         fcosts = new HashMap<Location, Integer>();
         gcosts = new HashMap<Location, Integer>();
         hcosts = new HashMap<Location, Integer>();
         parents = new HashMap<Location, Location>();
         
         
         open.add(startLoc);
         parents.put(startLoc, startLoc);
         hcosts.put(startLoc, getHcost(startLoc));
         gcosts.put(startLoc, getGcost(startLoc));
         fcosts.put(startLoc, getFcost(startLoc));
         
         
         
         
         while(!closed.contains(endLoc) || !open.isEmpty())
         {
             Location current = getMinLocation();
             open.remove(current);
             closed.add(current);
             ArrayList<Location> theoreticalLocs = getWalkableLocs(current);
             theoreticalLocs.remove(test);
             for(Location loc : theoreticalLocs)
             {
                 if(!closed.contains(loc))
                 {
                     if(!open.contains(loc))
                     {
                         open.add(loc);
                         parents.put(loc, current);
                         fcosts.put(loc, getFcost(loc));
                         gcosts.put(loc, getGcost(loc));
                         hcosts.put(loc, getHcost(loc));
                     }
                     else if(getGcost(loc) < gcosts.get(loc))
                     {
                         parents.put(loc, current);
                         fcosts.put(loc, getFcost(loc));
                         gcosts.put(loc, getGcost(loc));
                     }
                     
 
                 }
             }
             
             if(open.isEmpty())
                 break;
         }
         
         if(closed.contains(endLoc))
             return true;
         else
             return false;
             
     
     }
     
     /*
      * Gets all the locations a minion can move to. A minion cannot eat Barricades or other Minions, but it can eat other Actors.
      * @param loc the location around which to check for walkable locations.
      * @return an ArrayList of walkable locations that are adjacent to loc
      */
     public ArrayList<Location> getWalkableLocs(Location loc)
     {
         ArrayList<Location> adjacentLocs = getGrid().getValidAdjacentLocations(loc);
         for(int i = adjacentLocs.size() - 1; i >= 0; i--)
         {
             //remove barricades from walkable locations
             if(getGrid().get(adjacentLocs.get(i)) instanceof Barricade)
                 adjacentLocs.remove(i);
         }
         
         return adjacentLocs;
     }
     
     /*
      * Estimates the distance from loc to the endpoint using the Manhattan method, which ignores barriers.
      * The distance is simply the number of rows + the number of columns it takes to get to end.
      * @param loc the location from which to calculate
      * @return the estimated distance to end
      */
     public int getHcost(Location loc)
     {
         //manhattan method for estimating distance from end.
         int x1 = loc.getRow();
         int x2 = endLoc.getRow();
         
         int y1 = loc.getCol();
         int y2 = endLoc.getCol();
         
         return 10 * (int) (Math.abs(x1 - x2) + Math.abs(y1 - y2));
     }
     
     /*
      * Recursively estimates the distance from start to loc. Moving horizontally 
      * or vertically costs 10, but moving diagonally costs 14.
      * @param loc the location to which to calculate.
      * @return the estimated distance from start to loc.
      */
     public int getGcost(Location loc)
     {
         int gcost;
         Location parent = parents.get(loc);
         //if loc is directly to the side of parent, the cost of moving there is 10
         //System.out.println("Parent: " + parent.toString());
         if(loc.getDirectionToward(parent) % 90 == 0)
             gcost = 10;
         //if loc is diagonal from parent, then the cost of moving there is 10 * sqrt(2), or approximately 14
         else
             gcost = 14;
         
         
         if(parent.equals(startLoc))
             return 0;
         else
             return gcost + getGcost(parent);        
     }
     
     /*
      * The estimated distance from start to end, assuming we go through loc.
      * @param loc the location we must go through
      * @return the estimated distance from start to end (lower is better)
      */
     public int getFcost(Location loc)
     {
         //a bit wasteful 
         return getGcost(loc) + getHcost(loc);
     }
     
     /*
      * Out of all the locations to be checked (in the HashSet open), return the 
      * location with the lowest estimated distance from beginning to end (fcost).
      * @return the Location with the lowest fcost (the Location with the lowest estimated distance from beginning to end).
      */
     public Location getMinLocation()
     {
         
         Object[] loc2 = open.toArray();
     	Location minLoc = (Location) loc2[0];
     	//if(!fcosts.containsKey(minLoc))
     	//	return null;
     	//System.out.println("minloc: " + minLoc);
         //System.out.println("Fcosts: " + fcosts.toString());
         int minFcost = fcosts.get(minLoc);
         for(Location loc : open)
         {
             int fcost = getFcost(loc);
             if(fcost <= minFcost)
             {
                 minFcost = fcost;
                 minLoc = loc;
             }
         }
         
         return minLoc;
     }
 }
