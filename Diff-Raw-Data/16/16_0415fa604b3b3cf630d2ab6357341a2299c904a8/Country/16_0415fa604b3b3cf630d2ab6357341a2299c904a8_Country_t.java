 package game;
 
 import java.util.List;
 import java.util.Vector;
 import java.util.UUID;
 
 import map.Hexagon;
 
 public class Country
 {
 	private UUID id;
 
 	private Player player;
 
 	private int dice;
 
 	public Vector<Country> neighbours;
 
 	public Vector<Hexagon> hexagons;
 
 	public int countID;
 
 	// generate country without specified player		
 	public Country(int dice, int countID)
 	{
 		this.id = UUID.randomUUID();
 
 		this.neighbours = new Vector<Country>();
 		
 		this.hexagons = new Vector<Hexagon>();
 
 		setDice(dice);
 		
 		this.countID = countID;
 	}
 
 	// generate empty country		
 	public Country(Player player, int dice)
 	{
 		this.id = UUID.randomUUID();
 
 		setPlayer(player);
 
 		setDice(dice);
 
 		this.neighbours = new Vector<Country>();
 
 		this.hexagons = new Vector<Hexagon>();
 	}
 
 	public Country(Country other)
 	{
 		// Copy constructor!
 		id = other.id;
 		player = other.player;
 		neighbours = new Vector<Country>(other.neighbours);
 		this.hexagons = other.hexagons;
 		dice = other.dice;
 	}
 
 	public Player getPlayer()
 	{
 		return player;
 	}
 
 	public void setPlayer(Player player)
 	{
 		this.player = player;
 	}
 
 	public List<Hexagon> getHexagons()
 	{
 		return hexagons;
 	}
 
 	public List<Country> getNeighbours()
 	{
 		return neighbours;
 	}
 
 	public int getDice()
 	{
 		return dice;
 	}
 
 	public void setDice(int dice)
 	{
 		if (dice > getMaximumDice())
 			throw new RuntimeException("Trying to assign more dice than the maximum number of dice to " + this);
 
 		this.dice = dice;
 	}
 
 	public int getMaximumDice()
 	{
 		return 7;
 	}
 
 	public List<Country> enemyNeighbours()
 	{
 		Vector<Country> enemies = new Vector<Country>();
 
 		for (Country neighbour : neighbours)
 			if (!neighbour.getPlayer().equals(player))
 				enemies.add(neighbour);
 
 		return enemies;
 	}
 
	public int hashCode()
	{
		return id.hashCode();
	}

 	public boolean equals(Object other)
 	{
 		if (this == other)
 			return true;
 
 		if (!(other instanceof Country))
 			return false;
 
 		return id == ((Country) other).id;
 	}
 
 	public String toString()
 	{
 		return "[Country " + id + " player:" + player + " dice:" + dice + "]";
 	}
 }
