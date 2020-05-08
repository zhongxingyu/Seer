 package com.censoredsoftware.Demigods.Engine.Object.Player;
 
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.entity.Player;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.Engine.Conversation.Prayer;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
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
 			// TODO: Confirm that this covers all of the bases.
 			currChar.setMaxHealth(player.getMaxHealth());
 			currChar.setHealth(player.getHealth());
 			currChar.setHunger(player.getFoodLevel());
 			currChar.setLevel(player.getLevel());
 			currChar.setExperience(player.getExp());
 			currChar.setLocation(player.getLocation());
 			currChar.saveInventory();
 
 			// Disown pets
 			TameableWrapper.disownPets(currChar.getName());
 
 			// Set to inactive and update previous
 			currChar.setActive(false);
 			this.previous = currChar.getId();
 
 			// Save it
 			PlayerCharacter.save(currChar);
 		}
 
 		// Update their inventory
 		if(getCharacters(player).size() == 1) newChar.saveInventory();
 		newChar.getInventory().setToPlayer(player);
 
 		// Update health and experience
 		// TODO: Confirm that this covers all of the bases too.
 		player.setMaxHealth(newChar.getMaxHealth());
 		player.setHealth(newChar.getHealth());
 		player.setFoodLevel(newChar.getHunger());
 		player.setExp(newChar.getExperience());
 		player.setLevel(newChar.getLevel());
 
 		// Set new character to active
 		newChar.setActive(true);
 		this.current = newChar.getId();
 
 		// Re-own pets
 		TameableWrapper.reownPets(player);
 
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
 	public static List<PlayerCharacter> getCharacters(OfflinePlayer player)
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
 		final List<PlayerCharacter> characters = getCharacters(player);
 
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
 	public static boolean isPraying(Player player)
 	{
 		try
 		{
 			return DataUtility.hasKeyTemp(player.getName(), "prayer_conversation");
 		}
 		catch(Exception ignored)
 		{}
 		return false;
 	}
 
 	/**
 	 * Removes all temp data related to prayer for the <code>player</code>.
 	 * 
 	 * @param player the player to clean.
 	 */
 	public static void clearPrayerSession(Player player)
 	{
		if(DataUtility.hasKeyTemp(player.getName(), "prayer_conversation")) DataUtility.removeTemp(player.getName(), "prayer_conversation");
		if(DataUtility.hasKeyTemp(player.getName(), "prayer_context")) DataUtility.removeTemp(player.getName(), "prayer_context");
		if(DataUtility.hasKeyTemp(player.getName(), "prayer_location")) DataUtility.removeTemp(player.getName(), "prayer_location");
 	}
 
 	/**
 	 * Returns the context for the <code>player</code>'s prayer converstion.
 	 * 
 	 * @param player the player whose context to return.
 	 * @return ConversationContext
 	 */
 	public static ConversationContext getPrayerContext(Player player)
 	{
 		if(!isPraying(player)) return null;
 		return (ConversationContext) DataUtility.getValueTemp(player.getName(), "prayer_context");
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
 			// Toggle on
 			togglePrayingSilent(player, true);
 		}
 		else
 		{
 			// Toggle off
 			togglePrayingSilent(player, false);
 
 			// Message them
 			MiscUtility.clearChat(player);
 			player.sendMessage(ChatColor.AQUA + "You are no longer praying.");
 			player.sendMessage(ChatColor.GRAY + "Your chat has been re-enabled.");
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
 			// Create the conversation and save it
 			Conversation prayer = Prayer.startPrayer(player);
 			DataUtility.saveTemp(player.getName(), "prayer_conversation", prayer);
 			DataUtility.saveTemp(player.getName(), "prayer_location", player.getLocation());
 		}
 		else
 		{
 			// Save context and abandon the conversation
 			if(DataUtility.hasKeyTemp(player.getName(), "prayer_conversation"))
 			{
 				Conversation prayer = (Conversation) DataUtility.getValueTemp(player.getName(), "prayer_conversation");
 				DataUtility.saveTemp(player.getName(), "prayer_context", prayer.getContext());
 				prayer.abandon();
 			}
 
 			// Remove the data
 			DataUtility.removeTemp(player.getName(), "prayer_conversation");
 			DataUtility.removeTemp(player.getName(), "prayer_location");
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
 		if(DataUtility.hasKeyTemp(player.getName(), "player_hold") && option) DataUtility.removeTemp(player.getName(), "player_hold");
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
 		if(DataUtility.hasKeyTemp(player.getName(), "no_chat") && option) DataUtility.removeTemp(player.getName(), "no_chat");
 		else DataUtility.saveTemp(player.getName(), "no_chat", true);
 	}
 }
