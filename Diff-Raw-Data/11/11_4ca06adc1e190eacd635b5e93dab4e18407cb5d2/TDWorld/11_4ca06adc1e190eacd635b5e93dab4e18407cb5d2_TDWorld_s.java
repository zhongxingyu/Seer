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
     
     public static final boolean DEBUG = true;
     
     public static boolean cheats = false;
     
     public final Location startLoc;
     public final Location endLoc;
     
     public static String lastAdded;
     
     public int hp;
     public int gold;
     
     public Actor nextToAdd;
     public boolean gameOver;
     
     public Graphics2D g2;
     
     private HashSet<Location> open; //list of possible locations to check 
     private HashSet<Location> closed; //list of checked locations
     private Map<Location, Integer> fcosts; //overall distance from beginning to end
     private Map<Location, Integer> gcosts; //distance from beginning to location
     private Map<Location, Integer> hcosts; //distance from end to location
     private Map<Location, Location> parents;
     
     
     public void db(Object o) {
     	System.out.println(o.toString());
     }
     
     
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
     
     public void load() {
     	gameOver = false;
     	gold = 100;
     	hp = 20;
     	add(startLoc, new Shade(this));
     	add(endLoc, new Shade(this));
     }
     
     public void cheater() {
     	cheats = !cheats;
     }
     
     public void takeGold(int toTake) {
     	gold -= toTake;
     }
     
     public void addGold(int toAdd) {
     	gold += toAdd;
     }
     
     public int getGold() {
     	return gold;
     }
     
     public void loseLife() {
     	hp--;
     	System.out.println("You have lost a life! You now have " + hp + " lives.");
     	if(hp == 0) {
     		gameOver = true;
     		System.out.println("GAME OVER! You lost!");
     	}
     }
     
     public void nextType(String s) {
     	db("startLoc: " + startLoc + " endLoc: " + endLoc);
     	switch(s) {
     		case "barricade":
     			nextToAdd = new Barricade(this);
     		break;
     		case "firetower":
     			nextToAdd = new FireTower(this);
     		break;
     		case "watertower":
     		//	nextToAdd = new WaterTower(this);
     		break;
     		case "magetower":
     		//	nextToAdd = new MageTower(this);
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
 
     public void show()
     {
         if (getMessage() == null)
             setMessage(DEFAULT_MESSAGE);
         super.show();
     }    
     	
     /**
      * This method is called when the user clicks on a location in the
      * WorldFrame.
      * 
      * @param loc the grid location that the user selected
      * @return true if the world consumes the click, or false if the GUI should
      * invoke the Location->Edit menu action
      */
     public boolean locationClicked(Location loc)
     {
     	if(nextToAdd == null)
     		return true;
         if(!isValidPlacement(loc))
         {
             System.out.println("Sorry, you can't place objects to completely block the end path.");
             return true;
         }
             
     	add(loc, nextToAdd);
     	
     	if(!cheats) {
     		nextToAdd = null;
     	} else {
     		nextType(lastAdded);
     	}
         
     	return true;
         //return false;
     }
 
     public void step()
     {
     	if(gameOver) {
     		System.out.println("Game over. Please reload Super TD to start a new game.");
     		return;
     	}
         Grid<Actor> gr = getGrid();
         ArrayList<Actor> actors = new ArrayList<Actor>();
         for (Location loc : gr.getOccupiedLocations())
             actors.add(gr.get(loc));
 
         for (Actor a : actors)
         {
             if (a.getGrid() == gr)
                 a.act();
         }
         if(gr.get(startLoc) == null)
     		add(startLoc, new Shade(this));
         if(gr.get(endLoc) == null)
     		add(endLoc, new Shade(this));
         	
     }
 
     public void add(Location loc, Actor occupant)
     {
         occupant.putSelfInGrid(getGrid(), loc);
     }
 
     public void add(Actor occupant)
     {
         Location loc = getRandomEmptyLocation();
         if (loc != null)
             add(loc, occupant);
     }
 
     public Actor remove(Location loc)
     {
         Actor occupant = getGrid().get(loc);
         if (occupant == null)
             return null;
         occupant.removeSelfFromGrid();
         return occupant;
     }
     
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
            if(theoreticalLocs.contains(test))
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
         }
         
         if(closed.contains(endLoc))
             return true;
         else
             return false;
             
     
     }
     
     public ArrayList<Location> getWalkableLocs(Location loc)
     {
         ArrayList<Location> adjacentLocs = getGrid().getValidAdjacentLocations(loc);
         for(int i = adjacentLocs.size() - 1; i >= 0; i--)
         {
             //remove barricades or minions from walkable locations
             if(getGrid().get(adjacentLocs.get(i)) instanceof Barricade || getGrid().get(adjacentLocs.get(i)) instanceof Minion)
                 adjacentLocs.remove(i);
         }
         
         return adjacentLocs;
     }
     
     public int getHcost(Location loc)
     {
         //manhattan method for estimating distance from end.
         int x1 = loc.getRow();
         int x2 = endLoc.getRow();
         
         int y1 = loc.getCol();
         int y2 = endLoc.getCol();
         
         return 10 * (int) (Math.abs(x1 - x2) + Math.abs(y1 - y2));
     }
     
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
     
     public int getFcost(Location loc)
     {
         //a bit wasteful 
         return getGcost(loc) + getHcost(loc);
     }
     
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
