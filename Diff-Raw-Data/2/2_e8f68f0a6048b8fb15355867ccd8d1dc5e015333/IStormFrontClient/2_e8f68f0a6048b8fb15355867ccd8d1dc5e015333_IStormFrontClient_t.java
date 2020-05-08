 /*
  * Created on Mar 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.core.stormfront.client;
 
 import java.util.List;
 
 import cc.warlock.core.client.ICharacterStatus;
 import cc.warlock.core.client.ICompass;
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.script.IScript;
 import cc.warlock.core.script.IScriptListener;
 import cc.warlock.core.stormfront.serversettings.server.ServerSettings;
 import cc.warlock.core.stormfront.serversettings.skin.IStormFrontSkin;
 
 /**
  * @author Marshall
  */
 public interface IStormFrontClient extends IWarlockClient {
 	
 	public static final String DEATH_STREAM_NAME = "death";
 	public static final String INVENTORY_STREAM_NAME = "inv";
 	public static final String THOUGHTS_STREAM_NAME = "thoughts";
 	public static final String ROOM_STREAM_NAME = "room";
 	public static final String FAMILIAR_STREAM_NAME = "familiar";
 	
 	public static final String COMPONENT_ROOM_EXITS = "room exits";
 	public static final String COMPONENT_ROOM_DESCRIPTION = "room desc";
 	
 	public static enum GameMode {
 		Game, CharacterManager
 	};
 	
 	/**
 	 * The server settings for this client
 	 * @return
 	 */
 	public ServerSettings getServerSettings();
 	
 	/**
 	 * @return The player ID of the current player
 	 */
 	public IProperty<String> getPlayerId();
 	
 	/**
 	 * @return The roundtime property
 	 */
 	public IProperty<Integer> getRoundtime();
 	
 	/**
 	 * Start a new roundtime countdown based on the seconds argument.
 	 * @param seconds The number of seconds to count down in the Roundtime bar.
 	 * @param label The label to show in the roundtime bar.
 	 */
 	public void startRoundtime(int seconds);
 	
 	public void updateRoundtime(int secondsLeft);
 	
 	/**
 	 * @return The health property
 	 */
 	public IProperty<Integer> getHealth();	
 	
 	/**
 	 * @return The amount of mana in the mana bar.
 	 */
	public IProperty<Integer> getMana();
 	
 	/**
 	 * @return The amount of fatigue in the fatigue bar.
 	 */
 	public IProperty<Integer> getFatigue();
 	
 	/**
 	 * @return The amount of spirit in the spirit bar.
 	 */
 	public IProperty<Integer> getSpirit();
 	
 	/**
 	 * @return The client's compass.
 	 */
 	public ICompass getCompass();
 
 	/**
 	 * @return The left hand property
 	 */
 	public IProperty<String> getLeftHand();
 	
 	/**
 	 * @return The right hand property
 	 */
 	public IProperty<String> getRightHand();
 	
 	/**
 	 * @return The current spell property
 	 */
 	public IProperty<String> getCurrentSpell();
 	
 	/**
 	 * @return The character status
 	 */
 	public ICharacterStatus getCharacterStatus();
 	
 	/**
 	 * @return The name of the character associated with this client.
 	 */
 	public IProperty<String> getCharacterName();
 	
 	/**
 	 * @return A list of currently running scripts
 	 */
 	public List<IScript> getRunningScripts();
 	
 	/**
 	 * Add a script listener
 	 * @param listener
 	 */
 	public void addScriptListener (IScriptListener listener);
 	
 	public void removeScriptListener (IScriptListener listener);
 	
 	/**
 	 * @return The stormfront skin
 	 */
 	public IStormFrontSkin getStormFrontSkin();
 	
 	/**
 	 * @return The stream for thoughts
 	 */
 	public IStream getThoughtsStream();
 	
 	/**
 	 * @return The stream for deaths
 	 */
 	public IStream getDeathsStream();
 	
 	/**
 	 * @return The stream for inventory
 	 */
 	public IStream getInventoryStream();
 	
 	/**
 	 * @return The stream for room description/exits/etc.
 	 */
 	public IStream getRoomStream();
 	
 	/**
 	 * @return The stream for familiars / wounds
 	 */
 	public IStream getFamiliarStream();
 	
 	/**
 	 * @return The description of the current room
 	 */
 	public IProperty<String> getRoomDescription();
 	
 	/**
 	 * @param componentName
 	 * @return The component with the passed in name
 	 */
 	public IProperty<String> getComponent(String componentName);
 	
 	/** 
 	 * @return The current game mode
 	 */
 	public IProperty<GameMode> getGameMode();
 }
