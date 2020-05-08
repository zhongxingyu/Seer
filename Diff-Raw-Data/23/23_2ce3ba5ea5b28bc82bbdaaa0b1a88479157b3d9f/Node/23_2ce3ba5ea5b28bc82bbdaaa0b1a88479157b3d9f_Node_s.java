 package linewars.gamestate.mapItems;
 
 import linewars.gamestate.Player;
 import linewars.gamestate.Transformation;
import linewars.gamestate.mapItems.Lane
import linewars.gamestate.mapItems.Building
import linewars.gamestate.mapItems.Unit
 
 public class Node {
 	private Player owner;
 	private Player invader;
 	private long occupationTime;
 	private ArrayList<Lane> attachedLanes;
 	private ArrayList<Building> containedBuildings;
 	private CommandCenter center;
 	private ArrayList<Unit> containedUnits;
 	private Transformation[] buildingSpots;
 	private int numBuildings;
 	
 	public Node(Player owner, Lane[] lanes, CommandCenter center, Transformation[] buildingSpots)
 	{
 		this.owner = owner;
 		invader = null; //Maybe have a special value to set this to in order to avoid null?
 		occupationTime = 0;
		attachedLanes = new ArrayList<lane>();
		for(int i = 0; i < lanes.length(); i++)
 		{
 			attachedLanes.add(lanes[i]);
 		}
 		this.center = center;
 		containedUnits = new ArrayList<Unit>();
 		this.buildingSpots = buildingSpots;
 	}
 	
 	public Player getOwner()
 	{
 		return owner;
 	}
 	
 	public boolean isContested()
 	{
 		return occupationTime != 0;
 	}
 	
 	public Player getInvader()
 	{
 		return invader;
 	}
 	
 	public long getOccupationTime()
 	{
 		return occupationTime;
 	}
 	
 	public Building[] getContainedBuildings()
 	{
		return containedBuildings.toArray();
 	}
 	
 	public Lane[] getAttachedLanes()
 	{
		return attachedLanes.toArray();
 	}
 	
 	public Unit[] getContainedUnits()
 	{
		return containedUnits.toArray();
 	}
 	
 	public int getNumBuildings()
 	{
 		return numBuildings;
 	}
 	
 	void spawnWaves()
 	{
 		if(containedUnits.size() != 0)
 		{
 			
 		}
 	}
 	
 	
 	/**
 	 * This method gets the next available position and rotation to build a
 	 * building at. Transformation includes a position and
 	 * rotation. Returns null if there isn't a spot left to
 	 * build any buildings at.
 	 * 
 	 * @return the next available spot or null
 	 */
 	public Transformation getNextAvailableBuildingSpot()
 	{
 		if(numBuildings >= buildingSpots.length)
 		{
 			return null;
 		}
 		return buildingSpots[numBuildings];
 	}
 	
 	/**
 	 * This method attempts to put the input building into the spot
 	 * that the building says its in. If that spot is available to
 	 * place a building, then it is added and this method returns
 	 * true. Otherwise this method returns false.
 	 * 
 	 * @param b		the building to add to the node
 	 * @return		if the building was successfully added
 	 */
 	public boolean addBuilding(Building b)
 	{
 		for(int i = 0; i < containedBuildings.size(); i++)
 		{
 			if(b.getTransformation().equals(containedBuildings.get(i).getTransformation()))
 			{
 				return false;
 			}
 		}
 		if(containedBuildings.add(b))
 		{
 			numBuildings++;
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Adds a unit to the list of contained units in the node
 	 * 
 	 * @param u		the contained units in the node
 	 */
 	public void addUnit(Unit u)
 	{
 		containedUnits.add(u);
 	}
 }
