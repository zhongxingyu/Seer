 package com.censoredsoftware.demigods.engine.player;
 
 import com.censoredsoftware.censoredlib.data.location.Region;
 import com.censoredsoftware.censoredlib.exception.MojangIdNotFoundException;
 import com.censoredsoftware.censoredlib.helper.ColoredStringBuilder;
 import com.censoredsoftware.censoredlib.helper.MojangIdGrabber;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.battle.Battle;
 import com.censoredsoftware.demigods.engine.conversation.Prayer;
 import com.censoredsoftware.demigods.engine.data.DataManager;
 import com.censoredsoftware.demigods.engine.language.Translation;
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.util.Configs;
 import com.censoredsoftware.demigods.engine.util.Messages;
 import com.censoredsoftware.demigods.engine.util.Zones;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.*;
 
 public class DPlayer implements ConfigurationSerializable
 {
 	private String mojangAccount;
 	private String playerName;
 	private String mortalName, mortalListName;
 	private boolean canPvp;
 	private long lastLoginTime, lastLogoutTime;
 	private String currentDeityName;
 	private int characterSlots;
 	private UUID current;
 	private UUID previous;
 	private UUID mortalInventory;
 	private ChatRecorder chatRecording;
 
 	public DPlayer()
 	{
 		characterSlots = Configs.getSettingInt("character.default_character_slots");
 	}
 
 	public DPlayer(String mojangAccount, ConfigurationSection conf)
 	{
 		this.mojangAccount = mojangAccount;
 		this.playerName = conf.getString("playerName");
 		if(conf.isString("mortalName")) this.mortalName = conf.getString("mortalName");
 		if(conf.isString("mortalListName")) this.mortalListName = conf.getString("mortalListName");
 		if(conf.isBoolean("canPvp")) canPvp = conf.getBoolean("canPvp");
 		if(conf.isLong("lastLoginTime")) lastLoginTime = conf.getLong("lastLoginTime");
 		else lastLoginTime = -1;
 		if(conf.isLong("lastLogoutTime")) lastLogoutTime = conf.getLong("lastLogoutTime");
 		else lastLogoutTime = -1;
 		if(conf.getString("currentDeityName") != null) currentDeityName = conf.getString("currentDeityName");
 		if(conf.isInt("characterSlots")) characterSlots = conf.getInt("characterSlots");
 		else characterSlots = Configs.getSettingInt("character.default_character_slots");
 		if(conf.getString("current") != null) current = UUID.fromString(conf.getString("current"));
 		if(conf.getString("previous") != null) previous = UUID.fromString(conf.getString("previous"));
 		if(conf.getString("mortalInventory") != null) mortalInventory = UUID.fromString(conf.getString("mortalInventory"));
 	}
 
 	@Override
 	public Map<String, Object> serialize()
 	{
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("playerName", playerName);
 		map.put("characterSlots", characterSlots);
 		try
 		{
 			map.put("canPvp", canPvp);
 			map.put("lastLoginTime", lastLoginTime);
 			map.put("lastLogoutTime", lastLogoutTime);
 		}
 		catch(Throwable ignored)
 		{}
 		if(mortalName != null) map.put("mortalName", mortalName);
 		if(mortalListName != null) map.put("mortalListName", mortalListName);
 		if(currentDeityName != null) map.put("currentDeityName", currentDeityName);
 		if(current != null) map.put("current", current.toString());
 		if(previous != null) map.put("previous", previous.toString());
 		if(mortalInventory != null) map.put("mortalInventory", mortalInventory.toString());
 		return map;
 	}
 
 	public void setPlayerName(String player)
 	{
 		this.playerName = player;
 	}
 
 	void setMojangAccount(String account)
 	{
 		this.mojangAccount = account;
 	}
 
 	public void setMortalName(String name)
 	{
 		mortalName = name;
 	}
 
 	public String getMortalName()
 	{
 		return mortalName != null ? mortalName : playerName;
 	}
 
 	public void setMortalListName(String name)
 	{
 		mortalListName = name;
 	}
 
 	public String getMortalListName()
 	{
 		return mortalListName != null ? mortalListName : playerName;
 	}
 
 	public void resetCurrent()
 	{
 		this.current = null;
 		this.currentDeityName = null;
 
 		if(getOfflinePlayer().isOnline())
 		{
 			getOfflinePlayer().getPlayer().setDisplayName(getOfflinePlayer().getName());
 			getOfflinePlayer().getPlayer().setPlayerListName(getOfflinePlayer().getName());
 			getOfflinePlayer().getPlayer().setMaxHealth(20.0);
 		}
 	}
 
 	public void setCanPvp(boolean pvp)
 	{
 		this.canPvp = pvp;
 		Util.save(this);
 	}
 
 	public void updateCanPvp()
 	{
 		if(!getOfflinePlayer().isOnline()) return;
 
 		// Define variables
 		final Player player = getOfflinePlayer().getPlayer();
 		final boolean inNoPvpZone = Zones.inNoPvpZone(player.getLocation());
 
 		if(getCurrent() != null && Battle.Util.isInBattle(getCurrent())) return;
 
 		if(!canPvp() && !inNoPvpZone)
 		{
 			setCanPvp(true);
 			player.sendMessage(ChatColor.GRAY + Demigods.LANGUAGE.getText(Translation.Text.UNSAFE_FROM_PVP));
 		}
 		else if(!inNoPvpZone)
 		{
 			setCanPvp(true);
 			DataManager.removeTimed(player.getName(), "pvp_cooldown");
 		}
 		else if(canPvp() && !DataManager.hasTimed(player.getName(), "pvp_cooldown"))
 		{
 			int delay = Configs.getSettingInt("zones.pvp_area_delay_time");
 			DataManager.saveTimed(player.getName(), "pvp_cooldown", true, delay);
 
 			Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					if(Structure.Util.isInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_PVP))
 					{
 						if(getCurrent() != null && Battle.Util.isInBattle(getCurrent())) return;
 						setCanPvp(false);
 						player.sendMessage(ChatColor.GRAY + Demigods.LANGUAGE.getText(Translation.Text.SAFE_FROM_PVP));
 					}
 				}
 			}, (delay * 20));
 		}
 	}
 
 	public OfflinePlayer getOfflinePlayer()
 	{
 		return Bukkit.getOfflinePlayer(playerName);
 	}
 
 	public void setLastLoginTime(Long time)
 	{
 		this.lastLoginTime = time;
 		Util.save(this);
 	}
 
 	public long getLastLoginTime()
 	{
 		return this.lastLoginTime;
 	}
 
 	public void setLastLogoutTime(long time)
 	{
 		this.lastLogoutTime = time;
 		Util.save(this);
 	}
 
 	public long getLastLogoutTime()
 	{
 		return this.lastLogoutTime;
 	}
 
 	public void setCharacterSlots(int slots)
 	{
 		characterSlots = slots;
 	}
 
 	public void addCharacterSlot()
 	{
 		characterSlots += 1;
 	}
 
 	public void removeCharacterSlot()
 	{
 		characterSlots -= 1;
 	}
 
 	public int getCharacterSlots()
 	{
 		return characterSlots;
 	}
 
 	public void setToMortal()
 	{
 		Player player = getOfflinePlayer().getPlayer();
 		saveCurrentCharacter();
 		player.setMaxHealth(20.0);
 		player.setHealth(20.0);
 		player.setFoodLevel(20);
 		player.setExp(0);
 		player.setLevel(0);
 		player.setGameMode(GameMode.SURVIVAL);
 		for(PotionEffect potion : player.getActivePotionEffects())
 			player.removePotionEffect(potion.getType());
 		player.setDisplayName(getMortalName());
 		player.setPlayerListName(getMortalListName());
 		setMortalName(null);
 		setMortalListName(null);
 		applyMortalInventory();
 		Demigods.BOARD.getTeam("Mortal").addPlayer(getOfflinePlayer());
 	}
 
 	public void saveMortalInventory(PlayerInventory inventory)
 	{
 		DCharacter.Inventory mortalInventory = new DCharacter.Inventory();
 		mortalInventory.generateId();
 		if(inventory.getHelmet() != null) mortalInventory.setHelmet(inventory.getHelmet());
 		if(inventory.getChestplate() != null) mortalInventory.setChestplate(inventory.getChestplate());
 		if(inventory.getLeggings() != null) mortalInventory.setLeggings(inventory.getLeggings());
 		if(inventory.getBoots() != null) mortalInventory.setBoots(inventory.getBoots());
 		mortalInventory.setItems(inventory);
 		DCharacter.Util.saveInventory(mortalInventory);
 		this.mortalInventory = mortalInventory.getId();
 		Util.save(this);
 	}
 
 	public void saveCurrentCharacter()
 	{
 		// Update the current character
 		final Player player = getOfflinePlayer().getPlayer();
 		final DCharacter character = getCurrent();
 
 		if(character != null)
 		{
 			// Set to inactive and update previous
 			character.setActive(false);
 			this.previous = character.getId();
 
 			// Set the values
 			character.setHealth(player.getHealth() >= character.getMaxHealth() ? character.getMaxHealth() : player.getHealth());
 			character.setHunger(player.getFoodLevel());
 			character.setLevel(player.getLevel());
 			character.setExperience(player.getExp());
 			character.setLocation(player.getLocation());
 			Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
 			{
 				@Override
 				public void run()
 				{
 					if(player.getBedSpawnLocation() != null) character.setBedSpawn(player.getBedSpawnLocation());
 				}
 			}, 1);
 			character.setGameMode(player.getGameMode());
 			character.setPotionEffects(player.getActivePotionEffects());
 			character.saveInventory();
 
 			// Disown pets
 			DPet.Util.disownPets(character.getName());
 
 			// Remove from their team
 			Demigods.BOARD.getTeam(getCurrent().getAlliance().getName()).removePlayer(getOfflinePlayer());
 
 			// Save it
 			DCharacter.Util.save(character);
 		}
 	}
 
 	public void switchCharacter(final DCharacter newChar)
 	{
 		final Player player = getOfflinePlayer().getPlayer();
 
 		if(!newChar.getPlayerName().equals(this.playerName))
 		{
 			player.sendMessage(ChatColor.RED + "You can't do that.");
 			return;
 		}
 
 		// Save the current character
 		saveCurrentCharacter();
 
 		// Set new character to active and other info
 		this.current = newChar.getId();
 		currentDeityName = newChar.getDeity().getName();
 
 		// Apply the new character
 		newChar.applyToPlayer(player);
 
 		// Teleport them
 		try
 		{
 			player.teleport(newChar.getLocation());
 		}
 		catch(Exception e)
 		{
 			Messages.warning("There was a problem while teleporting a player to their character.");
 		}
 
 		// Save instances
 		Util.save(this);
 		DCharacter.Util.save(newChar);
 	}
 
 	public boolean canPvp()
 	{
 		return this.canPvp;
 	}
 
 	public String getPlayerName()
 	{
 		return playerName;
 	}
 
 	public String getMojangAccount()
 	{
 		return mojangAccount;
 	}
 
 	public String getCurrentDeityName()
 	{
 		return currentDeityName;
 	}
 
 	public Region getRegion()
 	{
 		if(getOfflinePlayer().isOnline()) return Region.Util.getRegion(getOfflinePlayer().getPlayer().getLocation());
 		return Region.Util.getRegion(getCurrent().getLocation());
 	}
 
 	public boolean hasCurrent()
 	{
 		return getCurrent() != null;
 	}
 
 	public DCharacter getCurrent()
 	{
 		if(this.current == null) return null;
 		DCharacter character = DCharacter.Util.load(this.current);
 		if(character != null && character.isUsable()) return character;
 		return null;
 	}
 
 	public DCharacter getPrevious()
 	{
 		if(this.previous == null) return null;
 		return DCharacter.Util.load(this.previous);
 	}
 
 	public Set<DCharacter> getCharacters()
 	{
 		return Sets.newHashSet(Collections2.filter(DCharacter.Util.loadAll(), new Predicate<DCharacter>()
 		{
 			@Override
 			public boolean apply(DCharacter character)
 			{
 				return character != null && character.getMojangAccount().equals(mojangAccount) && character.isUsable();
 			}
 		}));
 	}
 
 	public Set<DCharacter> getUsableCharacters()
 	{
 		return Sets.filter(getCharacters(), new Predicate<DCharacter>()
 		{
 			@Override
 			public boolean apply(DCharacter character)
 			{
 				return character.isUsable();
 			}
 		});
 	}
 
 	public DCharacter.Inventory getMortalInventory()
 	{
 		return DCharacter.Util.getInventory(mortalInventory);
 	}
 
 	public void applyMortalInventory()
 	{
 		if(getMortalInventory() == null) mortalInventory = DCharacter.Util.createEmptyInventory().getId();
 		getMortalInventory().setToPlayer(getOfflinePlayer().getPlayer());
 		mortalInventory = null;
 	}
 
 	public boolean canMakeCharacter()
 	{
 		return getUsableCharacters().size() < getCharacterSlots();
 	}
 
 	public boolean canUseCurrent()
 	{
 		if(getCurrent() == null || !getCurrent().isUsable())
 		{
 			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to load!");
 			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
 			return false;
 		}
 		else return getOfflinePlayer().isOnline();
 	}
 
 	public void remove()
 	{
 		// First we need to kick the player if they're online
 		if(getOfflinePlayer().isOnline()) getOfflinePlayer().getPlayer().kickPlayer(ChatColor.RED + "Your player save has been cleared.");
 
 		// Remove characters
 		for(DCharacter character : getCharacters())
 			character.remove();
 
 		// Now we clear the DPlayer save itself
 		Util.delete(getMojangAccount());
 	}
 
 	/**
 	 * Starts recording recording the <code>player</code>'s chat.
 	 */
 	public void startRecording()
 	{
 		chatRecording = ChatRecorder.Util.startRecording(getOfflinePlayer().getPlayer());
 	}
 
 	/**
 	 * Stops recording and sends all messages that have been recorded thus far to the player.
 	 * 
 	 * @param display if true, the chat will be sent to the player
 	 */
 	public List<String> stopRecording(boolean display)
 	{
 		Player player = getOfflinePlayer().getPlayer();
 		// Handle recorded chat
 		if(chatRecording != null && chatRecording.isRecording())
 		{
 			// Send held back chat
 			List<String> messages = chatRecording.stop();
 			if(messages.size() > 0 && display)
 			{
 				player.sendMessage(" ");
 				player.sendMessage(new ColoredStringBuilder().italic().gray(Demigods.LANGUAGE.getText(Translation.Text.HELD_BACK_CHAT).replace("{size}", "" + messages.size())).build());
 				for(String message : messages)
 					player.sendMessage(message);
 			}
 
 			return messages;
 		}
 		return null;
 	}
 
 	public static class Util
 	{
 		public static DPlayer create(Player player)
 		{
 			DPlayer playerSave = new DPlayer();
 			playerSave.setMojangAccount(MojangIdGrabber.getUUID(player));
 			playerSave.setPlayerName(player.getName());
 			playerSave.setLastLoginTime(player.getLastPlayed());
 			playerSave.setCanPvp(true);
 			Util.save(playerSave);
 			return playerSave;
 		}
 
 		public static void save(DPlayer player)
 		{
 			DataManager.players.put(player.getMojangAccount(), player);
 		}
 
 		public static void delete(String mojangAccount)
 		{
 			DataManager.players.remove(mojangAccount);
 		}
 
 		public static DPlayer getPlayer(OfflinePlayer player)
 		{
 			String id = MojangIdGrabber.getUUID(player);
 			if(id == null) throw new MojangIdNotFoundException(player.getName());
 			DPlayer found = getPlayer(id);
 			if(found == null) return create(player.getPlayer());
 			return found;
 		}
 
 		public static DPlayer getPlayerFromName(final String playerName)
 		{
 			try
 			{
 				return Iterables.find(DataManager.players.values(), new Predicate<DPlayer>()
 				{
 					@Override
 					public boolean apply(DPlayer dPlayer)
 					{
 						return dPlayer.getPlayerName().equals(playerName);
 					}
 				});
 			}
 			catch(NoSuchElementException ignored)
 			{}
 			throw new NullPointerException("No such player exists currently in the Demigods data.");
 		}
 
 		public static DPlayer getPlayer(String mojangAccount)
 		{
 			if(DataManager.players.containsKey(mojangAccount)) return DataManager.players.get(mojangAccount);
 			return null;
 		}
 
 		/**
 		 * Returns true if the <code>player</code> is currently immortal.
 		 * 
 		 * @param player the player to check.
 		 * @return boolean
 		 */
 		public static boolean isImmortal(Player player)
 		{
 			DCharacter character = getPlayer(player).getCurrent();
 			return character != null && character.isUsable() && character.isActive();
 		}
 
 		public static Collection<OfflinePlayer> getMortals()
 		{
 			return Collections2.transform(Collections2.filter(DataManager.players.values(), new Predicate<DPlayer>()
 			{
 				@Override
 				public boolean apply(DPlayer player)
 				{
 					DCharacter character = player.getCurrent();
					return character == null || !character.isUsable() || !character.isActive();
 				}
 			}), new Function<DPlayer, OfflinePlayer>()
 			{
 				@Override
 				public OfflinePlayer apply(DPlayer player)
 				{
 					return player.getOfflinePlayer();
 				}
 			});
 		}
 
 		public static Set<Player> getOnlineMortals()
 		{
 			return Sets.filter(Sets.newHashSet(Bukkit.getOnlinePlayers()), new Predicate<Player>()
 			{
 				@Override
 				public boolean apply(Player player)
 				{
 					DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
 					return character == null || !character.isUsable() || !character.isActive();
 				}
 			});
 		}
 
 		/**
 		 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
 		 * 
 		 * @param player the player to check.
 		 * @param charName the charName to check with.
 		 * @return boolean
 		 */
 		public static boolean hasCharName(Player player, String charName)
 		{
 			for(DCharacter character : getPlayer(player).getCharacters())
 				if(character.getName().equalsIgnoreCase(charName)) return true;
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
 				return DataManager.hasKeyTemp(player.getName(), "prayer_conversation");
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
 		public static void clearPrayerSession(OfflinePlayer player)
 		{
 			DataManager.removeTemp(player.getName(), "prayer_conversation");
 			DataManager.removeTemp(player.getName(), "prayer_context");
 			DataManager.removeTemp(player.getName(), "prayer_location");
 			DataManager.removeTimed(player.getName(), "currently_creating");
 			DataManager.removeTimed(player.getName(), "currently_forsaking");
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
 			return (ConversationContext) DataManager.getValueTemp(player.getName(), "prayer_context");
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
 				togglePrayingSilent(player, true, true);
 			}
 			else
 			{
 				// Message them
 				Messages.clearRawChat(player);
 				for(String message : Demigods.LANGUAGE.getTextBlock(Translation.Text.PRAYER_ENDED))
 					player.sendRawMessage(message);
 
 				// Toggle off
 				togglePrayingSilent(player, false, true);
 			}
 		}
 
 		/**
 		 * Changes prayer status for <code>player</code> to <code>option</code> silently.
 		 * 
 		 * @param player the player the manipulate.
 		 * @param option the boolean to set to.
 		 * @param recordChat whether or not the chat should be recorded.
 		 */
 		public static void togglePrayingSilent(Player player, boolean option, boolean recordChat)
 		{
 			if(option)
 			{
 				// Create the conversation and save it
 				Conversation prayer = Prayer.startPrayer(player);
 				DataManager.saveTemp(player.getName(), "prayer_conversation", prayer);
 				DataManager.saveTemp(player.getName(), "prayer_location", player.getLocation());
 				player.setSneaking(true);
 
 				// Record chat if enabled
 				if(recordChat) DPlayer.Util.getPlayer(player).startRecording();
 			}
 			else
 			{
 				// Save context and abandon the conversation
 				if(DataManager.hasKeyTemp(player.getName(), "prayer_conversation"))
 				{
 					Conversation prayer = (Conversation) DataManager.getValueTemp(player.getName(), "prayer_conversation");
 					DataManager.saveTemp(player.getName(), "prayer_context", prayer.getContext());
 					prayer.abandon();
 				}
 
 				// Remove the data
 				DataManager.removeTemp(player.getName(), "prayer_conversation");
 				DataManager.removeTemp(player.getName(), "prayer_location");
 				player.setSneaking(false);
 
 				// Handle recorded chat
 				DPlayer.Util.getPlayer(player).stopRecording(recordChat);
 			}
 		}
 	}
 }
