 package com.censoredsoftware.Demigods.Engine.Object.Player;
 
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Utility.DataUtility;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.google.common.collect.Sets;
 
 @Model
 public class PlayerWrapper
 {
 	@Id
 	private Long id;
 	@Attribute
 	@Indexed
 	private String player;
 	@Attribute
 	private long lastLoginTime;
 	@Attribute
 	private long current;
 	@Attribute
 	private long previous;
 
 	void setPlayer(String player)
 	{
 		this.player = player;
 		PlayerWrapper.save(this);
 	}
 
 	public static PlayerWrapper create(OfflinePlayer player)
 	{
 		PlayerWrapper trackedPlayer = new PlayerWrapper();
 		trackedPlayer.setPlayer(player.getName());
 		trackedPlayer.setLastLoginTime(player.getLastPlayed());
 		PlayerWrapper.save(trackedPlayer);
 		return trackedPlayer;
 	}
 
 	public static void save(PlayerWrapper trackedPlayer)
 	{
 		JOhm.save(trackedPlayer);
 	}
 
 	public static PlayerWrapper load(Long id)
 	{
 		return JOhm.get(PlayerWrapper.class, id);
 	}
 
 	public static Set<PlayerWrapper> loadAll()
 	{
 		try
 		{
 			return JOhm.getAll(PlayerWrapper.class);
 		}
 		catch(Exception e)
 		{
 			return Sets.newHashSet();
 		}
 	}
 
 	public static PlayerWrapper getPlayer(OfflinePlayer player)
 	{
 		try
 		{
 			List<PlayerWrapper> tracking = JOhm.find(PlayerWrapper.class, "player", player.getName());
 			return tracking.get(0);
 		}
 		catch(Exception ignored)
 		{}
 		return create(player);
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(this.player);
 	}
 
 	public void setLastLoginTime(Long time)
 	{
 		this.lastLoginTime = time;
 		PlayerWrapper.save(this);
 	}
 
 	public Long getLastLoginTime()
 	{
 		return this.lastLoginTime;
 	}
 
 	public void switchCharacter(PlayerCharacter newChar)
 	{
 		Player player = getOfflinePlayer().getPlayer();
 
 		if(!newChar.getOfflinePlayer().equals(getOfflinePlayer()))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that.");
 			return;
 		}
 
 		// Update the current character
 		PlayerCharacter currChar = getCurrent();
 		if(currChar != null)
 		{
 			// Set the values
 			currChar.setHealth(player.getHealth());
 			currChar.setHunger(player.getFoodLevel());
 			currChar.setLevel(player.getLevel());
 			currChar.setExperience(player.getExp());
 			currChar.setLocation(player.getLocation());
 			currChar.saveInventory();
 
 			// Set to inactive and update previous
 			currChar.setActive(false);
 			this.previous = currChar.getId();
 
 			// Save it
 			PlayerCharacter.save(currChar);
 		}
 
 		// Update their inventory
 		if(getChars(player).size() == 1) newChar.saveInventory();
 		newChar.getInventory().setToPlayer(player);
 
 		// Update health and experience
 		player.setHealth(newChar.getHealth());
 		player.setFoodLevel(newChar.getHunger());
 		player.setExp(newChar.getExperience());
 		player.setLevel(newChar.getLevel());
 
 		// Set new character to active
 		newChar.setActive(true);
 		this.current = newChar.getId();
 
 		// Disable prayer, re-enabled movement, etc. just to be safe
 		togglePraying(player, false);
 
 		// Teleport them
 		try
 		{
 			player.teleport(newChar.getLocation());
 		}
 		catch(Exception e)
 		{
 			Demigods.message.severe("There was a problem while teleporting a player to their character.");
 		}
 
 		// Save instances
 		PlayerWrapper.save(this);
 		PlayerCharacter.save(newChar);
 	}
 
 	public PlayerCharacter getCurrent()
 	{
 		return PlayerCharacter.load(this.current);
 	}
 
 	public PlayerCharacter getPrevious()
 	{
 		return PlayerCharacter.load(this.previous);
 	}
 
 	public List<PlayerCharacter> getCharacters()
 	{
 		return JOhm.find(PlayerCharacter.class, "player", this.player);
 	}
 
 	/**
 	 * Returns the current alliance for <code>player</code>.
 	 * 
 	 * @param player the player to check.
 	 * @return String
 	 */
 	public static String getCurrentAlliance(OfflinePlayer player)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		if(character == null || !character.isImmortal()) return "Mortal";
 		return character.getAlliance();
 	}
 
 	/**
 	 * Returns a List of all of <code>player</code>'s characters.
 	 * 
 	 * @param player the player to check.
 	 * @return List the list of all character IDs.
 	 */
 	public static List<PlayerCharacter> getChars(OfflinePlayer player)
 	{
 		return PlayerWrapper.getPlayer(player).getCharacters();
 	}
 
 	/**
 	 * Returns true if the <code>player</code> is currently immortal.
 	 * 
 	 * @param player the player to check.
 	 * @return boolean
 	 */
 	public static boolean isImmortal(OfflinePlayer player)
 	{
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 		return character != null && character.isImmortal();
 	}
 
 	/**
 	 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
 	 * 
 	 * @param player the player to check.
 	 * @param charName the charName to check with.
 	 * @return boolean
 	 */
 	public static boolean hasCharName(OfflinePlayer player, String charName)
 	{
 		final List<PlayerCharacter> characters = getChars(player);
 
 		for(PlayerCharacter character : characters)
 		{
 			if(character == null) continue;
 			if(character.getName().equalsIgnoreCase(charName)) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the <code>player</code> is currently praying.
 	 * 
 	 * @param player the player to check.
 	 * @return boolean
 	 */
 	public static boolean isPraying(OfflinePlayer player)
 	{
 		try
 		{
 			return Boolean.parseBoolean(DataUtility.getValueTemp(player.getName(), "praying").toString());
 		}
 		catch(Exception ignored)
 		{}
 		return false;
 	}
 
 	/**
 	 * Changes prayer status for <code>player</code> to <code>option</code> and tells them.
 	 * 
 	 * @param player the player the manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePraying(Player player, boolean option)
 	{
 		if(option)
 		{
 			togglePlayerChat(player, false);
 			DataUtility.saveTemp(player.getName(), "praying", option);
 			DataUtility.saveTemp(player.getName(), "praying_location", player.getLocation());
 		}
 		else
 		{
 			MiscUtility.clearChat(player);
 			player.sendMessage(ChatColor.AQUA + "You are no longer praying.");
 			player.sendMessage(ChatColor.GRAY + "Your chat has been re-enabled.");
 			togglePlayerChat(player, true);
 			DataUtility.removeTemp(player.getName(), "praying");
 			DataUtility.removeTemp(player.getName(), "praying_location");
 		}
 	}
 
 	/**
 	 * Changes prayer status for <code>player</code> to <code>option</code> silently.
 	 * 
 	 * @param player the player the manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePrayingSilent(Player player, boolean option)
 	{
 		if(option)
 		{
 			togglePlayerChat(player, false);
 			DataUtility.saveTemp(player.getName(), "praying", option);
 			DataUtility.saveTemp(player.getName(), "praying_location", player.getLocation());
 		}
 		else
 		{
 			togglePlayerChat(player, true);
 			DataUtility.removeTemp(player.getName(), "praying");
 			DataUtility.removeTemp(player.getName(), "praying_location");
 		}
 	}
 
 	/**
 	 * Enables or disables player movement for <code>player</code> based on <code>option</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePlayerMovement(OfflinePlayer player, boolean option)
 	{
		if(DataUtility.hasKeyTemp(player.getName(), "player_hold") && option) DataUtility.removeTemp(player.getName(), "temp_player_hold");
 		else DataUtility.saveTemp(player.getName(), "player_hold", true);
 	}
 
 	/**
 	 * Enables or disables player chat for <code>player</code> based on <code>option</code>.
 	 * 
 	 * @param player the player to manipulate.
 	 * @param option the boolean to set to.
 	 */
 	public static void togglePlayerChat(OfflinePlayer player, boolean option)
 	{
		if(DataUtility.hasKeyTemp(player.getName(), "no_chat") && option) DataUtility.removeTemp(player.getName(), "temp_no_chat");
 		else DataUtility.saveTemp(player.getName(), "no_chat", true);
 	}
 }
