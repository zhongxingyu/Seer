 package dk.stacktrace.risk.game_logic;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 import dk.stacktrace.risk.game_logic.enumerate.TerritoryID;
 
 public class Territory {
 	private String name;
 	private ArrayList<Territory> neighbours;
 	private Player owner;
 	private int armySize;
 	private TerritoryID id;
 	
 
 	public Territory() {
 		neighbours = new ArrayList<Territory>();
 	}
 
 	public Territory(String name, TerritoryID id) {
 		super();
 		neighbours = new ArrayList<Territory>();
 		this.name = name;
 		this.id = id;
 		armySize = 1;
 	}
 
 	public void reinforce(int armySize)
 	{
 		this.armySize += armySize; 
 	}
 
 	public int moveTroops(int armySize)
	{ 
 		if (this.armySize -1 >= armySize)
 		{
 			this.armySize -= armySize;
 			return this.armySize;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 	
 	public void addNeighbour(Territory neighbour)
 	{
 		neighbours.add(neighbour);
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof Territory))
 		{
 			return false;
 		}
 		else if (((Territory) o).getName().equals(name) )
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isNeighbour(Territory other)
 	{
 		for (Territory neighbour : neighbours) {
 			if (neighbour.equals(other))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public Player getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Player owner) {
 		this.owner = owner;
 	}
 	
 	public int getArmySize() {
 		return armySize;
 	}
 	
 	public TerritoryID getId() {
 		return id;
 	}
 	
 	public ArrayList<Territory> getNeighbours() {
 		return neighbours;
 	}
 	
 	public void kill() {
 		--armySize;
 	}
 }
