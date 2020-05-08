 package com.censoredsoftware.demigods.engine.language;
 
 import java.util.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 
 import com.censoredsoftware.core.bukkit.ConfigFile;
 import com.censoredsoftware.demigods.engine.Demigods;
 import com.censoredsoftware.demigods.engine.element.Deity;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class Translation
 {
 	private static File translationYAML;
 	private static Map<String, Object> translation;
 
 	public Translation()
 	{
 		String language = Demigods.config.getSettingString("language").toLowerCase();
 		if(!Demigods.config.getSettingString("language").equals("english")) try
 		{
 			translationYAML = new File(language);
 			translation = translationYAML.loadFromFile();
 			return;
 		}
 		catch(Throwable ignored)
 		{}
 		translation = Maps.newHashMap();
 	}
 
 	/**
 	 * Gets a specific line of <code>language</code>.
 	 * 
 	 * @param text The line we want.
 	 * @return The String value of the Text.
 	 */
 	public String getText(Text text)
 	{
 		if(translation.containsKey(text.name())) return ChatColor.translateAlternateColorCodes('&', translation.get(text.name()).toString());
 		return text.english();
 	}
 
 	/**
 	 * Gets a List for the given <code>language</code>.
 	 * 
 	 * @param text The block of language we want.
 	 * @return The ArrayList for the block of language.
 	 */
 	public List<String> getTextBlock(final Text text)
 	{
 		if(translation.containsKey(text.name()) && translation.get(text.name()) instanceof List) return new ArrayList<String>()
 		{
 			{
 				for(String line : (List<String>) translation.get(text.name()))
 					add(ChatColor.translateAlternateColorCodes('&', line));
 			}
 		};
 		return text.englishBlock();
 	}
 
 	public Set<String> getBlackList()
 	{
 		return new HashSet<String>()
 		{
 			{
 				// Manual Blacklist
 				add("Fuck");
 				add("Shit");
 				add("Ass");
 				add("Dick");
 				add("Penis");
 				add("Vagina");
 				add("Cunt");
 				add("Bitch");
 				add("Nigger");
 				add("Phil");
 				add("Staff");
 				add("Server");
 				add("Console");
 				add("Disowned");
 
 				// YAML Blacklist
 				if(translation.containsKey("BLACKLIST") && translation.get("BLACKLIST") instanceof List) addAll((List<String>) translation.get("BLACKLIST"));
 
 				// Deities
 				for(Deity deity : Demigods.getLoadedDeities().values())
 				{
 					add(deity.getInfo().getName());
 					add(deity.getInfo().getAlliance());
 				}
 			}
 		};
 	}
 
 	public static class File extends ConfigFile
 	{
 		private static String LANGUAGE_PATH;
 		private static String TRANSLATION_FILE;
 
 		public File(String translation)
 		{
 			super(Demigods.plugin);
 			TRANSLATION_FILE = translation + ".lang";
			LANGUAGE_PATH = Demigods.plugin.getDataFolder() + "/lang/";
 		}
 
 		@Override
 		public Map<String, Object> loadFromFile()
 		{
 			final FileConfiguration data = getData(LANGUAGE_PATH, TRANSLATION_FILE);
 			return new HashMap<String, Object>()
 			{
 				{
 					for(String stringId : data.getKeys(false))
 						put(stringId, data.get(stringId));
 				}
 			};
 		}
 
 		@Override
 		public boolean saveToFile()
 		{
 			return true;
 		}
 	}
 
 	public static enum Text
 	{
 		NOTIFICATIONS_PRAYER_FOOTER(new ArrayList<String>()
 		{
 			{
 				add(ChatColor.GRAY + "  Type " + ChatColor.RED + "clear" + ChatColor.GRAY + " to remove all notifications or type " + ChatColor.YELLOW + "menu");
 				add(ChatColor.GRAY + "  to return to the main menu.");
 			}
 		}), NOTIFICATION_RECEIVED(new ArrayList<String>()
 		{
 			{
 				add(ChatColor.GREEN + "You have a new notification!");
 				add(ChatColor.GRAY + "Find an Altar to view your notifications.");
 			}
 		}), PRAYER_ENDED(new ArrayList<String>()
 		{
 			{
 				add(ChatColor.AQUA + "You are no longer praying.");
 				add(ChatColor.GRAY + "Your chat has been re-enabled.");
 			}
 		}), PRAYER_INTRO(new ArrayList<String>()
 		{
 			{
 				add(ChatColor.GRAY + " While praying you are unable chat with players.");
 				add(ChatColor.GRAY + " You can return to the main menu at anytime by typing " + ChatColor.YELLOW + "menu" + ChatColor.GRAY + ".");
 				add(ChatColor.GRAY + " Walk away to stop praying.");
 			}
 		}), PVP_NO_PRAYER(new ArrayList<String>()
 		{
 			{
 				add(ChatColor.GRAY + "You cannot pray when PvP is still possible.");
 				add(ChatColor.GRAY + "Wait a few moments and then try again when it's safe.");
 			}
 		}), KNELT_FOR_PRAYER("{player} has knelt to begin prayer."), UNSAFE_FROM_PVP("You can now PvP!"), SAFE_FROM_PVP("You are now safe from PvP!"), ERROR_BIND_WEAPON_REQUIRED("A {weapon} is required to bind {ability}!"), DATA_RESET_KICK("All Demigods data has been reset, you may now rejoin!"), ADMIN_CLEAR_DATA_STARTING("Clearing all Demigods data..."), ADMIN_CLEAR_DATA_FINISHED("All Demigods data cleared successfully!"), SUCCESS_ABILITY_BOUND("{ability} has been bound to slot {slot}!"), SUCCESS_ABILITY_UNBOUND("{ability} has been unbound."), ERROR_BIND_TO_SLOT("You can only bind to an empty slot."), NOTIFICATION_WARP_CREATED(ChatColor.GREEN + "Warp created!"), NOTIFICATION_WARP_DELETED(ChatColor.RED + "Warp deleted!"), NOTIFICATION_INVITE_SENT(ChatColor.GREEN + "Invite sent!"), ERROR_NAME_LENGTH("Your name should be between 4 and 13 characters."), ERROR_CHAR_EXISTS("A character with that name already exists."), ERROR_ALPHA_NUMERIC("Only alpha-numeric characters are allowed."), ERROR_MAX_CAPS("Please use no more than {maxCaps} capital letters."), CREATE_OBELISK("You created an Obelisk!"), ALTAR_SPAWNED_NEAR("An Altar has spawned near you..."), PROTECTED_BLOCK("That block is protected by a Deity!"), ADMIN_WAND_GENERATE_ALTAR("Generating new Altar..."), ADMIN_WAND_GENERATE_ALTAR_COMPLETE("Altar created!"), ADMIN_WAND_REMOVE_ALTAR("Right-click this Altar again to remove it."), ADMIN_WAND_REMOVE_ALTAR_COMPLETE("Altar removed!"), CREATE_SHRINE_1("The {alliance} are pleased..."), CREATE_SHRINE_2("You have created a Shrine in the name of {deity}!"), ADMIN_WAND_REMOVE_SHRINE("Right-click this Shrine again to remove it."), ADMIN_WAND_REMOVE_SHRINE_COMPLETE("Shrine removed!"), NO_WARP_ALTAR("You've never set a warp at this Altar."), CHARACTER_CREATE_COMPLETE("You have been accepted into the lineage of {deity}!"), KILLSTREAK("{character} is on a killstreak of {kills} kills."), MORTAL_SLAIN_1(ChatColor.YELLOW + "A mortal" + ChatColor.GRAY + " was slain by " + ChatColor.YELLOW + "another mortal" + ChatColor.GRAY + "."), MORTAL_SLAIN_2(ChatColor.YELLOW + "A mortal" + ChatColor.GRAY + " was slain by {attacker} of the {attackerAlliance} alliance."), DEMI_SLAIN_1("{killed} of the {killedAlliance} was slain by " + ChatColor.YELLOW + "a mortal" + ChatColor.GRAY + "."), DEMI_SLAIN_2("{killed} of the {killedAlliance} was slain by {attacker} of the {attackerAlliance} alliance."), DEMI_BETRAY("{killed} was betrayed by {attacker} of the {alliance} alliance."), MORTAL_BETRAY("A mortal was killed by another worthless mortal."), COMMAND_BLOCKED_BATTLE("That command is blocked during a battle."), NO_PVP_ZONE("No-PVP in this zone."), WEAKER_THAN_YOU("One weaker than you has been slain by your hand."), YOU_FAILED_DEITY("You have failed {deity}!");
 
 		private String english;
 		private List<String> englishBlock;
 
 		private Text(String english)
 		{
 			this.english = english;
 			this.englishBlock = Lists.newArrayList();
 		}
 
 		private Text(List<String> englishBlock)
 		{
 			this.english = "";
 			this.englishBlock = englishBlock;
 		}
 
 		public String english()
 		{
 			return english;
 		}
 
 		public List<String> englishBlock()
 		{
 			return englishBlock;
 		}
 	}
 }
