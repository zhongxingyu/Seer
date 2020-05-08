 package serial;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 
 import plugin.Stalemate;
 
 import defense.HolyBlock;
 import defense.Team;
 public class RoundConfiguration {
 
 	private Map<Location, HolyBlock> holyBlocks = new ConcurrentHashMap<Location, HolyBlock>();
 	private List<Block> blocks = new Vector<Block>();
 	private Set<Team> teams = new HashSet<Team>();
 	private MapArea area;
 	public static enum RoundState {
 		IDLE, JOIN, BUILD, FIGHT, FINISH
 	}
 	public RoundConfiguration.RoundState state = RoundState.IDLE;
 	/**
 	 * Takes a List<Team> containing all participating teams and creates a round with the center at Location center
 	 * @param center - Center of the round
 	 * @param list - List of the teams in the round
 	 */
 	public RoundConfiguration(Location center, List<Team> list) {
 		// Auto-assign positions
		String radiusStr = Stalemate.getInstance().getSetting("playradius", "10");
 		int radius;
 		try {
 			radius = Integer.parseInt(radiusStr);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Please set playradius in the configuration properly.");
 		}
		MapArea a = new MapArea(center.clone().subtract(new Location(center.getWorld(), radius, radius, radius)), center.clone().add(new Location(center.getWorld(), radius, radius, radius)));
 		double currentAngle = 0;
 		double ang = 2*Math.PI/list.size();
 		Map<Location, Team> t = new HashMap<Location, Team>();
 		for (int i = 0; i < list.size(); i++)
 		{
 			int posX = (int) Math.round(Math.cos(currentAngle));
 			int posZ = (int) Math.round(Math.sin(currentAngle));
 			Location blockLoc = new Location(center.getWorld(), center.getBlockX()+posX, center.getBlockY(), center.getBlockZ()+posZ);
 			t.put(blockLoc, list.get(i));
 			currentAngle += ang;
 		}
 		init(a, t);
 	}
 	
 	/**
 	 * Constructs a round from an area of the map (MapArea), and a Map of team Locations (Map<Location, Team>)
 	 * @param area - A MapArea that defines where the round is played
 	 * @param teams - A Map<Location, Team> that contains the teams at a given Location
 	 */
 	public RoundConfiguration(MapArea area, Map<Location, Team> teams) {
 		init(area, teams);
 	}
 	
 
 	private void init(MapArea area, Map<Location, Team> teams) {
 		this.area = area;
 		for (Location loc : teams.keySet())
 		{  
 			HolyBlock hb = new HolyBlock(loc, teams.get(loc));
 			holyBlocks.put(loc, hb);
 			hb.place();
 			blocks.add(loc.getWorld().getBlockAt(loc));
 		}
 	}
 	
 	/**
 	 * Gets the HolyBlock at a location
 	 * @param loc - The location
 	 * @return block - The HolyBlock
 	 */
 	public HolyBlock holyBlockAt(Location loc)
 	{
 		return holyBlocks.get(loc);
 	}
 	
 	/**
 	 * Gets the map area in which the round is taking place
 	 * @return area - A MapArea of where the area where the round takes place
 	 */
 	public MapArea getArea()
 	{
 		return area;
 	}
 	
 	/**
 	 * Adds a team to the round
 	 * @param t - The Team to be added
 	 */
 	public void addTeam(Team t)
 	{
 		teams.add(t);
 	}
 	
 	/**
 	 * Removes a team from the round
 	 * @param t - The Team to be removed
 	 */
 	public void removeTeam(Team t)
 	{
 		teams.remove(t);
 	}
 	
 	/**
 	 * Gets the players in the round, names capitalized.
 	 * @return players - A List<String> containing the players in the round
 	 */
 	public List<String> getParticipatingPlayers()
 	{
 		List<String> players = new Vector<String>();
 		for (Team t : getParticipatingTeams())
 		{
 			for (String s : t.getPlayers())
 				players.add(s);
 		}
 		return players;
 	}
 	
 	/**
 	 * Gets the teams in the round
 	 * @return teams - A List<Team> containing the participating teams
 	 */
 	public List<Team> getParticipatingTeams()
 	{
 		return new Vector<Team>(teams);
 	}
 	
 	/**
 	 * Returns all of the Holy Blocks in a round
 	 * @return blocks - A List<Block> containing all of the Holy Blocks
 	 */
 	public List<Block> getHolyBlocks()
 	{
 		return new Vector<Block>(blocks);
 	}
 
     /**
      * Returns the number of teams participating in a round
      * @return number - The number of teams in a round, expressed as an int
      */
 	public int numTeams() {
 		return teams.size();
 	}
 	
 
 }
