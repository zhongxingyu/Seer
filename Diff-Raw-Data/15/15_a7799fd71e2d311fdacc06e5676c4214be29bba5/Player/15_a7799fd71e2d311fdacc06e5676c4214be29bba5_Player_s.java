 package com.evervoid.state.player;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.Color;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.data.RaceData;
 
 public class Player implements Jsonable
 {
 	private Color aColor;
 	private final String aColorName;
 	/**
 	 * UI-visible player name
 	 */
 	private String aFriendlyName = "";
 	/**
 	 * Player's home solar system ID; assigned by the game state on creation
 	 */
 	private int aHomeSolarSystem = -1;
 	/**
 	 * Internal player name, used to store associations
 	 */
 	private final String aName;
 	private RaceData aRaceData;
 	private final String aRaceName;
 	private Research aResearch;
	private final ResourceAmount aResources;
 	private EVGameState aState;
 
 	public Player(final Json j, final EVGameState state)
 	{
 		this(j.getStringAttribute("name"), j.getStringAttribute("race"), j.getStringAttribute("color"), state);
 		aFriendlyName = j.getStringAttribute("friendlyname");
 		aResearch = Research.fromJson(j.getAttribute("research"));
 		aHomeSolarSystem = j.getIntAttribute("home");
 	}
 
 	/**
 	 * Player constructor. Warning: The state argument may be null during lobby initialization; do NOT rely on the state in this
 	 * method. Rely on it in the setState method instead.
 	 * 
 	 * @param name
 	 *            Player name
 	 * @param race
 	 *            Player race name
 	 * @param color
 	 *            Player color name
 	 * @param state
 	 *            Pointer to state; MAY BE NULL, see setState()
 	 */
 	public Player(final String name, final String race, final String color, final EVGameState state)
 	{
 		aName = name;
 		aRaceName = race;
 		aColorName = color;
 		aResearch = new Research();
 		aFriendlyName = aName; // Can be set by the player later
 		setState(state); // Will populate the rest
		aResources = new ResourceAmount(state.getData(), aRaceData);
 	}
 
 	public Color getColor()
 	{
 		return aColor;
 	}
 
 	public SolarSystem getHomeSolarSystem()
 	{
 		return aState.getSolarSystem(aHomeSolarSystem);
 	}
 
 	public String getName()
 	{
 		return aName;
 	}
 
 	public String getNickname()
 	{
 		return aFriendlyName;
 	}
 
 	public RaceData getRaceData()
 	{
 		return aRaceData;
 	}
 
 	public Research getResearch()
 	{
 		return aResearch;
 	}
 
 	public Player setColor(final Color color)
 	{
 		aColor = color;
 		return this;
 	}
 
 	public void setFriendlyName(final String name)
 	{
 		aFriendlyName = name;
 	}
 
 	public void setHomeSolarSystem(final SolarSystem home)
 	{
 		aHomeSolarSystem = home.getID();
 	}
 
 	public Player setRace(final RaceData race)
 	{
 		aRaceData = race;
 		return this;
 	}
 
 	/**
 	 * Set this player's EVGameState link, and update Player information based on it
 	 * 
 	 * @param state
 	 *            The EVGameState to set to
 	 */
 	public void setState(final EVGameState state)
 	{
 		aState = state;
 		if (aState != null) {
 			aRaceData = aState.getRaceData(aRaceName);
 			aColor = aState.getPlayerColor(aColorName);
 		}
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setStringAttribute("name", aName).setStringAttribute("race", aRaceData.getType())
 				.setStringAttribute("color", aColorName).setAttribute("research", aResearch)
 				.setStringAttribute("friendlyname", aFriendlyName).setIntAttribute("home", aHomeSolarSystem);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Player " + aName + " (Nicknamed \"" + aFriendlyName + "\") of race " + aRaceData.getType();
 	}
 }
