 package com.evervoid.state;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.graphics.geometry.MathUtils;
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.action.Action;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.RaceData;
 import com.evervoid.state.data.StarData;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Star;
 
 public class EVGameState implements Jsonable
 {
 	public static void main(final String[] args)
 	{
 		System.out.println("Creating test game state...");
 		final EVGameState testState = new EVGameState();
 		System.out.println("Creating test game state created, printing.");
 		final Json testJ = testState.toJson();
 		System.out.println(testJ.toPrettyString());
 		System.out.println("Deserializing...");
 		final EVGameState testState2 = new EVGameState(testJ);
 		System.out.println("Re-printing...");
 		final Json testJ2 = testState2.toJson();
 		System.out.println(testJ2.toPrettyString());
 		System.out.println("Hash 1: " + testJ.getHash());
 		System.out.println("Hash 2: " + testJ2.getHash());
 		System.out.println("Matches: " + testJ.equals(testJ));
 	}
 
 	private final Map<Integer, Prop> aAllProps = new HashMap<Integer, Prop>();
 	private final Galaxy aGalaxy;
 	private final GameData aGameData;
 	private boolean aGameStarted = false;
 	private final Player aNullPlayer;
 	private final List<Player> aPlayerList;
 	private final String neutralPlayerName = "NullPlayer";
 
 	/**
 	 * Default constructor, creates a fully randomized galaxy with solar systems and planets in it.
 	 */
 	public EVGameState()
 	{
 		aGameData = new GameData(); // Game data must always be loaded first
 		aPlayerList = new ArrayList<Player>();
		aNullPlayer = new Player(neutralPlayerName, this);
 		aPlayerList.add(aNullPlayer);
		aPlayerList.add(new Player("Player1", this));
		aPlayerList.add(new Player("Player2", this));
 		aGalaxy = new Galaxy(this);
 		aGalaxy.populateRandomly();
 	}
 
 	/**
 	 * Restore a game state from a serialized state
 	 * 
 	 * @param json
 	 *            The Json representation of the game state
 	 */
 	public EVGameState(final Json json)
 	{
 		aGameStarted = json.getBooleanAttribute("gamestarted");
 		aGameData = new GameData(json.getAttribute("gamedata"));
 		final Json players = json.getAttribute("players");
 		aPlayerList = new ArrayList<Player>(players.size());
 		for (final Json p : players) {
 			aPlayerList.add(new Player(p, this));
 		}
 		if (getPlayerByName(neutralPlayerName) != null) {
 			aNullPlayer = getPlayerByName(neutralPlayerName);
 		}
 		else {
			aNullPlayer = new Player(neutralPlayerName, this);
 			aPlayerList.add(aNullPlayer);
 		}
 		// It is necessary to create an empty galaxy first and then to populate it.
 		// This is because certain stuff (props) need to look up stuff from the game state when constructing themselves
 		// Thus aGalaxy must be defined in the game state before they can look that up
 		aGalaxy = new Galaxy(this);
 		aGalaxy.populate(json.getAttribute("galaxy"));
 	}
 
 	/**
 	 * Overloaded constructor using specified playerList and galaxy.
 	 * 
 	 * @param playerList
 	 *            A list containing all the players.
 	 * @param galaxy
 	 *            A galaxy to generate the game state upon.
 	 */
 	public EVGameState(final List<Player> playerList, final Galaxy galaxy)
 	{
 		aGameData = new GameData(); // Game data must always be loaded first
		aNullPlayer = new Player(neutralPlayerName, this);
 		aPlayerList = new ArrayList<Player>(playerList);
 		aPlayerList.add(aNullPlayer);
 		aGalaxy = galaxy;
 	}
 
 	@Override
 	public EVGameState clone()
 	{
 		return new EVGameState(toJson());
 	}
 
 	public boolean commitAction(final Action action)
 	{
 		if (!action.isValid()) {
 			return false;
 		}
 		action.execute();
 		return true;
 	}
 
 	public void commitTurn(final Turn turn)
 	{
 		for (final Action action : turn.getActions()) {
 			commitAction(action);
 		}
 	}
 
 	public Galaxy getGalaxy()
 	{
 		return aGalaxy;
 	}
 
 	/**
 	 * @return A new, unused prop ID
 	 */
 	public int getNextPropID()
 	{
 		// If we have no prop, then ID 0 is not taken
 		if (aAllProps.isEmpty()) {
 			return 0;
 		}
 		// If we have props, iterate over them, get the max, and return max+1
 		// because that ID is certainly not taken
 		int maxId = Integer.MIN_VALUE;
 		for (final Integer id : aAllProps.keySet()) {
 			maxId = Math.max(maxId, id);
 		}
 		return maxId + 1;
 	}
 
 	/**
 	 * @return A new, unused solar system ID
 	 */
 	public int getNextSolarID()
 	{
 		return aGalaxy.getNextSolarID();
 	}
 
 	/**
 	 * @return The neutral (null) player
 	 */
 	public Player getNullPlayer()
 	{
 		return aNullPlayer;
 	}
 
 	/**
 	 * @param planetType
 	 *            The type of planet
 	 * @return The PlanetData object corresponding to that planet type
 	 */
 	public PlanetData getPlanetData(final String planetType)
 	{
 		return aGameData.getPlanetData(planetType);
 	}
 
 	/**
 	 * @return Available planet types
 	 */
 	public Set<String> getPlanetTypes()
 	{
 		return aGameData.getPlanetTypes();
 	}
 
 	/**
 	 * Returns a Player by his/her name
 	 * 
 	 * @param name
 	 *            The name to search for
 	 * @return The player object
 	 */
 	public Player getPlayerByName(final String name)
 	{
 		for (final Player p : aPlayerList) {
 			if (p.getName().equalsIgnoreCase(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return The list of players
 	 */
 	public List<Player> getPlayers()
 	{
 		return aPlayerList;
 	}
 
 	public Prop getPropFromID(final int id)
 	{
 		return aAllProps.get(id);
 	}
 
 	/**
 	 * @param raceType
 	 *            The type of race
 	 * @return The RaceData object corresponding to that race type
 	 */
 	public RaceData getRaceData(final String raceType)
 	{
 		return aGameData.getRaceData(raceType);
 	}
 
 	/**
 	 * @return A randomly-selected player
 	 */
 	Player getRandomPlayer()
 	{
 		return (Player) MathUtils.getRandomElement(aPlayerList);
 	}
 
 	public Star getRandomStar(final Dimension dim)
 	{
 		return Star.randomStar(dim, this);
 	}
 
 	/**
 	 * @param id
 	 *            The ID of the solar system
 	 * @return The solar system of the specified ID.
 	 */
 	public SolarSystem getSolarSystem(final int id)
 	{
 		return aGalaxy.getSolarSystem(id);
 	}
 
 	/**
 	 * @return The list of solar systems in the galaxy
 	 */
 	public Collection<SolarSystem> getSolarSystems()
 	{
 		return aGalaxy.getSolarSystems();
 	}
 
 	/**
 	 * @param starType
 	 *            The type of star
 	 * @return The StarData object corresponding to that star type
 	 */
 	public StarData getStarData(final String starType)
 	{
 		return aGameData.getStarData(starType);
 	}
 
 	/**
 	 * @return Available star types
 	 */
 	public Set<String> getStarTypes()
 	{
 		return aGameData.getStarTypes();
 	}
 
 	/**
 	 * @return A temporary solar system. (Used for development).
 	 */
 	public SolarSystem getTempSolarSystem()
 	{
 		return aGalaxy.getTempSolarSystem();
 	}
 
 	/**
 	 * @return Whether the game has started or not (in lobby)
 	 */
 	public boolean isStarted()
 	{
 		return aGameStarted;
 	}
 
 	/**
 	 * @return Whether the game is ready to be started (all players ready, all slots filled)
 	 */
 	public boolean readyToStart()
 	{
 		// TODO: Check if all players are ready
 		return true;
 	}
 
 	/**
 	 * Adds a prop to the game state's list of props
 	 * 
 	 * @param prop
 	 *            The prop to add
 	 */
 	public void registerProp(final Prop prop)
 	{
 		aAllProps.put(prop.getID(), prop);
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setBooleanAttribute("gamestarted", aGameStarted).setAttribute("gamedata", aGameData)
 				.setAttribute("galaxy", aGalaxy).setListAttribute("players", aPlayerList);
 	}
 }
