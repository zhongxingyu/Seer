 package linewars.gamestate;
 import java.util.*;
 import linewars.gamestate.mapItems.BuildingDefinition;
 import linewars.gamestate.mapItems.MapItem;
 import linewars.gamestate.mapItems.Node;
 import linewars.gamestate.mapItems.UnitDefinition;
 import java.util.*;
 public class Player {
 
 	private double stuffAmount;
 	private ArrayList<Node> ownedNodes;
 	private HashMap<Node, Double> flowDist;
 	private ArrayList<BuildingDefinition> buildingDefs;
 	private ArrayList<UnitDefinition> unitDefs;
 	private ArrayList<Tech> techLevels;
 	private String name;
 	
	public Player(double startingStuff, Node startingNode){
 		stuffAmount = startingStuff;
		ownedNodes = new ArrayList<Node>();
		ownedNodes.add(startingNode);
 		flowDist = new HashMap<Node, Double>();
 		buildingDefs = new ArrayList<BuildingDefinition>();
 		unitDefs = new ArrayList<UnitDefinition>();
 		techLevels = new ArrayList<Tech>();
 	}
 	
 	public double getStuff(){
 		return stuffAmount;
 	}
 	
 	public String getPlayerName(){
 		return name;
 	}
 	
 	public double getFlowDist(Node n){
 		return flowDist.get(n);
 	}
 	
 	public ArrayList<Node> getOwnedNodes(){
 		return ownedNodes;
 	}
 	
 	public UnitDefinition[] getUnitDefinitions(){
		return (UnitDefinition[])unitDefs.toArray();
 	}
 	
 	public BuildingDefinition[] getBuildingDefintions()
 	{
		return (BuildingDefinition[])buildingDefs.toArray();
 	}
 	
 	public Tech[] getTech()
 	{
		return (Tech[])techLevels.toArray();
 	}
 	
 	//TODO implement addMapItem
 	/**
 	 * adds the given mapItem to the player's master list of owned items
 	 * 
 	 * @param m	the mapItem to add
 	 */
 	public void addMapItem(MapItem m)
 	{
 		
 	}
 	
 	
 	/**
 	 * spends the given amount of stuff, throws an exception if there
 	 * isn't enough stuff left to spend
 	 * 
 	 * @param amount	the amount of stuff to spend
 	 */
	public void spendStuff(double amount) throws IllegalArgumentException
 	{
 		if(amount > stuffAmount){
			throw new IllegalArgumentException("Not enough Stuff. " +name
					+" Tried spending " +amount +" Stuff but only has " +stuffAmount);
 		}
		stuffAmount = stuffAmount - amount;
 	}
 	
 }
