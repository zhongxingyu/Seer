 package linewars.gamestate;
 
 import java.awt.geom.Dimension2D;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 
 import linewars.display.layers.MapItemLayer.MapItemType;
 import linewars.gameLogic.GameTimeManager;
 import linewars.gamestate.mapItems.*;
 import linewars.parser.ConfigFile;
 import linewars.parser.Parser;
 import linewars.parser.Parser.InvalidConfigFileException;
 import linewars.parser.Parser.NoSuchKeyException;
 
 public class GameState
 {
 	// TODO finish implementation!
 	
 	private static final int STARTING_STUFF = 100;
 	
 	private Map map;
 	private HashMap<Integer, Player> players;
 	private int numPlayers;
 	private ArrayList<Race> races;
 	
 	public int getNumPlayers()
 	{
 		return numPlayers;
 	}
 	
 	public Player getPlayer(int playerID)
 	{
 		return players.get(playerID);
 	}
 	
 	/**
 	 * This constructor constructs the game state. It takes in the parser for the map,
 	 * the number of players, and the list of race URIs, in order for each player
 	 * (eg the 1st spot in the list is the race for the 1st player and so on).
 	 * 
 	 * @param mapParser		the parser for the map	
 	 * @param numPlayers	the number of players
 	 * @param raceURIs		the URI's of the races
 	 * @throws FileNotFoundException
 	 * @throws InvalidConfigFileException
 	 */
 	public GameState(String mapURI, int numPlayers, List<String> raceURIs) throws FileNotFoundException, InvalidConfigFileException
 	{
 		Parser mapParser = new Parser(new ConfigFile(mapURI));
 		map = new Map(mapParser, null, null);
 		players = new HashMap<Integer, Player>();
 		this.numPlayers = numPlayers;
 		
 		races = new ArrayList<Race>();
 		for(int i = 0; i < raceURIs.size(); i++)
 		{
 			Race r = new Race(new Parser(new ConfigFile(raceURIs.get(i))));
 			if(!races.contains(r))
 				races.add(r);
 			Node[] startNode = { map.getStartNode(i) };
 			Player p = new Player(STARTING_STUFF, startNode, r);
 		}
 	}
 	
 	public Dimension2D getMapSize()
 	{
 		return map.getDimensions();
 	}
 	
 	public String getMap()
 	{
 		return map.getMapURI();
 	}
 	
 	public List<Player> getPlayers()
 	{
 		List<Player> players = new ArrayList<Player>();
 		for(int i = 0; i < numPlayers; i++)
 			players.add(this.players.get(i));
 		
 		return players;
 	}
 	
 	public long getTime()
 	{
 		return GameTimeManager.currentTimeMillis();
 	}
 	
	public List<? extends MapItem> getMapItemsOfType(MapItemType type)
 	{
 		switch (type)
 		{
 		case UNIT:
 			return getUnits();
 		case PROJECTILE:
 			return getProjectiles();
 		case BUILDING:
 			return getBuildings();
 		default:
 			return new ArrayList<MapItem>(0);
 		}
 	}
 	
 	public List<Unit> getUnits()
 	{
 		List<Unit> units = new ArrayList<Unit>();
 		Lane[] lanes = map.getLanes();
 		for(Lane l : lanes)
 		{
 			Wave[] waves = l.getWaves();
 			for(Wave w : waves)
 			{
 				Unit[] us = w.getUnits();
 				for(Unit u : us)
 					units.add(u);
 			}
 		}
 		
 		return units;
 	}
 	
 	public List<Building> getBuildings()
 	{
 		List<Building> buildings = new ArrayList<Building>();
 		Node[] nodes = map.getNodes();
 		for(Node n : nodes)
 		{
 			Building[] bs = n.getContainedBuildings();
 			for(Building b : bs)
 				buildings.add(b);
 		}
 		
 		return buildings;
 	}
 	
 	public List<Projectile> getProjectiles()
 	{
 		List<Projectile> projectiles = new ArrayList<Projectile>();
 		Lane[] lanes = map.getLanes();
 		for(Lane l : lanes)
 		{
 			Projectile[] ps = l.getProjectiles();
 			for(Projectile p : ps)
 				projectiles.add(p);
 		}
 		
 		return projectiles;
 	}
 	
 	public List<CommandCenter> getCommandCenters()
 	{
 		ArrayList<CommandCenter> ccs = new ArrayList<CommandCenter>();
 		Node[] nodes = map.getNodes();
 		for(Node n : nodes)
 			ccs.add(n.getCommandCenter());
 		return ccs;
 	}
 }
