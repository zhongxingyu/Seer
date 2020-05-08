 package com.legit2.Demigods.Utilities;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.google.common.base.Joiner;
 import com.legit2.Demigods.DConfig;
 import com.legit2.Demigods.Database.DDatabase;
 
 public class DCharUtil
 {
 	/*
 	 *  createChar() : Creates the character according to the values passed in. Returns true on success,
 	 *  false on fail.
 	 */
 	public static boolean createChar(Player player, String charName, String charDeity)
 	{
 		if(!DPlayerUtil.hasCharName(player, charName))
 		{
 			// Define variables
 			int playerID = DPlayerUtil.getPlayerID(player);
 			int charID = DObjUtil.generateInt(5);
 			String charAlliance = DDeityUtil.getDeityAlliance(charDeity);
 			int charHP = player.getHealth();
 			float charExp = player.getExp();
 			double charX = player.getLocation().getX();
 			double charY = player.getLocation().getY();
 			double charZ = player.getLocation().getZ();
 			String charW = player.getLocation().getWorld().getName();
 			int charFavor = DConfig.getSettingInt("default_char_favor");
 			int charMaxFavor = DConfig.getSettingInt("default_max_favor");
 			int charDevotion = DConfig.getSettingInt("default_devotion");
 			int charAscensions = DConfig.getSettingInt("default_ascensions");
 			
 			DDataUtil.addChar(charID);
 			DDataUtil.savePlayerData(player, "current_char", charID);
 			DDataUtil.saveCharData(charID, "char_owner", playerID);
 			DDataUtil.saveCharData(charID, "char_active", true);
 			DDataUtil.saveCharData(charID, "char_name", charName);
 			DDataUtil.saveCharData(charID, "char_alliance", charAlliance);
 			DDataUtil.saveCharData(charID, "char_deity", charDeity);
 			DDataUtil.saveCharData(charID, "char_immortal", true);
 			DDataUtil.saveCharData(charID, "char_hp", charHP);
 			DDataUtil.saveCharData(charID, "char_exp", charExp);
 			DDataUtil.saveCharData(charID, "char_lastX", charX);
 			DDataUtil.saveCharData(charID, "char_lastY", charY);
 			DDataUtil.saveCharData(charID, "char_lastZ", charZ);
 			DDataUtil.saveCharData(charID, "char_lastW", charW);
 			DDataUtil.saveCharData(charID, "char_favor", charFavor);
 			DDataUtil.saveCharData(charID, "char_max_favor", charMaxFavor);
 			DDataUtil.saveCharData(charID, "char_devotion", charDevotion);
 			DDataUtil.saveCharData(charID, "char_ascensions", charAscensions);
 			
 			// Add character to player's character list
 			String chars;
 			if(DPlayerUtil.getChars(player) != null && !DPlayerUtil.getChars(player).contains("null"))
 			{
 				ArrayList<String> charsTemp = DPlayerUtil.getChars(player);
 				charsTemp.add("" + charID);
 				chars = Joiner.on(",").join(charsTemp);
 			}
 			else chars = "" + charID;
 			
 			DDataUtil.savePlayerData(player, "player_characters", chars);
 
 			try
 			{
 				DDatabase.addCharToDB(player, charID);
 				DDatabase.savePlayer(player);
 			}
 			catch(SQLException e)
 			{
 				// Error with saving character... send them an error code				
 				player.sendMessage(ChatColor.RED + "There was a problem with saving your character.");
 				player.sendMessage(ChatColor.RED + "Please give this error code to an administrator: " + ChatColor.RESET + ChatColor.ITALIC + ChatColor.RED + "2002");
 			}
 			
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  removeChar() : Removes the character with (int)id. Returns true on success, false on fail.
 	 */
 	public static boolean removeChar(OfflinePlayer player, int charID)
 	{
 		if(DDataUtil.removeChar(charID))
 		{
 			// Remove from player_characters
 			ArrayList<String> charsTemp = DPlayerUtil.getChars(player);
 			charsTemp.remove(charID);
 			String chars = Joiner.on(",").join(charsTemp);
 			DDataUtil.savePlayerData(player, "player_characters", chars);
 
 			DDatabase.savePlayer(player);
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 *  getCharByID() : Returns the complete character info for the character with (int)id.
 	 */
 	public static HashMap<String, Object> getInfo(int charID)
 	{
 		return DDataUtil.getAllCharData(charID);
 	}
 	
 	/*
 	 *  getWhereDeity() : Returns the (int)charID for (Player)player's (String)deity.
 	 */
 	public static int getCharWhereDeity(Player player, String deity)
 	{		
 		for(Entry<Integer, HashMap<String, Object>> character : DDataUtil.getAllPlayerChars(player).entrySet())
 		{
 			int charID = character.getKey();
 			HashMap<String, Object> charData = character.getValue();
 			
 			for(Entry<String, Object> charDataEntry : charData.entrySet())
 			{
 				if(charDataEntry.getKey().equalsIgnoreCase("deity") && ((String) charDataEntry.getValue()).equalsIgnoreCase(deity)) return charID;
 				else return -1;
 			}
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getCharOwner() : Returns the (OfflinePlayer)player who owns (int)charID.
 	 */
 	public static OfflinePlayer getOwner(int charID)
 	{
 		OfflinePlayer charOwner = null;
 		for(Entry<Integer, HashMap<String, Object>> character : DDataUtil.getAllChars().entrySet())
 		{
 			if(character.getValue().containsKey("char_owner"))
 			{
				charOwner = Bukkit.getOfflinePlayer((String) character.getValue().get("char_owner"));
 			}
 		}
 		return charOwner;
 	}
 	
 	/*
 	 *  isImmortal() : Gets if the player is immortal or not.
 	 */
 	public static Boolean isImmortal(OfflinePlayer player)
 	{
 		if(DPlayerUtil.getCurrentChar(player) == -1) return false;
 		if(DDataUtil.getCharData(DPlayerUtil.getCurrentChar(player), "char_immortal") == null) return false;
 		else return DObjUtil.toBoolean(DDataUtil.getCharData(DPlayerUtil.getCurrentChar(player), "char_immortal"));
 	}
 	
 	/*
 	 *  setImmortal() : Sets the player's immortal boolean.
 	 */
 	public static void setImmortal(OfflinePlayer player, boolean option)
 	{			
 		DDataUtil.savePlayerData(player, "immortal", option);
 	}
 
 	/*
 	 *  hasDeity() : Returns boolean for if the character has the deity.
 	 */
 	public static boolean hasDeity(int charID, String deity)
 	{
 		if(charID == -1) return false;
 
 		if(getDeity(charID).equalsIgnoreCase(deity)) return true;
 		else return false;
 	}	
 	
 	/*
 	 *  getPlayer() : Returns the (int)playerID of the player owning (int)charID.
 	 */
 	public static int getPlayer(int charID)
 	{
 		return -1;
 	}
 	
 	/*
 	 *  getCharID() : Returns the (int)charID for the character with the name (String)charName.
 	 */
 	public static int getID(String charName)
 	{
 		HashMap<Integer, HashMap<String, Object>> characters = DDataUtil.getAllChars();
 		for(Entry<Integer, HashMap<String, Object>> playerChar : characters.entrySet())
 		{
 			// Define character-specific variables
 			int charID = playerChar.getKey();
 			
 			if(((String) characters.get(charID).get("char_name")).equalsIgnoreCase(charName)) return charID;
 		}
 		return -1;
 	}
 	
 	/*
 	 *  getName() : Returns the (String)charName of the character with (int)charID.
 	 */
 	public static String getName(int charID)
 	{
 		if(DDataUtil.charExistsByID(charID)) return (String) DDataUtil.getCharData(charID, "char_name");
 		else return null;
 	}
 	
 	/*
 	 *  getDeity() : Returns the (String)deity for (int)charID.
 	 */
 	public static String getDeity(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_deity")) return (String) DDataUtil.getCharData(charID, "char_deity");
 		else return null;
 	}
 
 	/*
 	 *  getAlliance() : Returns the (String)alliance for (int)charID.
 	 */
 	public static String getAlliance(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_alliance")) return (String) DDataUtil.getCharData(charID, "char_alliance");
 		else return null;
 	}
 	
 	/*
 	 *  getImmortal() : Returns the (Boolean)immortal for (int)charID.
 	 */
 	public static boolean getImmortal(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_immortal")) return DObjUtil.toBoolean(DDataUtil.getCharData(charID, "char_immortal"));
 		else return false;
 	}
 	
 	/*
 	 *  getFavor() : Returns the (int)favor for (int)charID.
 	 */
 	public static int getFavor(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_favor")) return DObjUtil.toInteger(DDataUtil.getCharData(charID, "char_favor"));
 		else return -1;
 	}
 	
 	/*
 	 *  getMaxFavor() : Returns the (int)maxFavor for (int)charID.
 	 */
 	public static int getMaxFavor(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_max_favor")) return DObjUtil.toInteger(DDataUtil.getCharData(charID, "char_max_favor"));
 		else return -1;
 	}
 	
 	/*
 	 *  getHP() : Returns the (int)hp for (int)charID.
 	 */
 	public static int getHP(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_hp")) return DObjUtil.toInteger(DDataUtil.getCharData(charID, "char_hp"));
 		else return -1;
 	}
 	
 	/*
 	 *  getExp() : Returns the (int)favor for (int)charID.
 	 */
 	public static float getExp(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_exp")) return DObjUtil.toFloat(DDataUtil.getCharData(charID, "char_exp"));
 		else return -1;
 	}
 	
 	/*
 	 *  getDevotion() : Returns the (int)devotion for (int)charID.
 	 */
 	public static int getDevotion(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_devotion")) return DObjUtil.toInteger(DDataUtil.getCharData(charID, "char_devotion"));
 		else return -1;
 	}
 	
 	/*
 	 *  getAscensions() : Returns the (int)ascensions for (int)charID.
 	 */
 	public static int getAscensions(int charID)
 	{
 		if(DDataUtil.hasCharData(charID, "char_ascensions")) return DObjUtil.toInteger(DDataUtil.getCharData(charID, "char_ascensions"));
 		else return -1;
 	}
 	
 	/*
 	 *  setFavor() : Sets the (String)username's favor to (int)amount.
 	 */
 	public static void setFavor(int charID, int amount)
 	{
 		DDataUtil.saveCharData(charID, "char_favor", amount);
 	}
 	
 	/*
 	 *  subtractFavor() : Subtracts (int)amount from the (String)username's favor.
 	 */
 	public static void subtractFavor(int charID, int amount)
 	{
 		setFavor(charID, getFavor(charID) - amount);
 	}
 	
 	/*
 	 *  giveFavor() : Gives (int)amount favor to (int)charID.
 	 */
 	public static void giveFavor(int charID, int amount)
 	{
 		// Define variables
 		int favor;
 
 		// Perform favor cap check
 		if((getFavor(charID) + amount) > DConfig.getSettingInt("global_max_favor"))
 		{
 			favor = DConfig.getSettingInt("global_max_favor");
 		}
 		else favor = getFavor(charID) + amount;
 		
 		setFavor(charID, favor);
 	}
 	
 	/*
 	 *  setAscensions() : Sets the (String)username's ascensions to (int)amount.
 	 */
 	public static void setAscensions(int charID, int amount)
 	{
 		DDataUtil.saveCharData(charID, "char_ascensions", amount);
 	}
 
 	/*
 	 *  subtractAscensions() : Subtracts (int)amount from the (String)username's ascensions.
 	 */
 	public static void subtractAscensions(int charID, int amount)
 	{
 		if(getAscensions(charID) - amount < 0)
 		{
 			setAscensions(charID, 0);
 		}
 		else setAscensions(charID, getAscensions(charID) - amount);
 	}
 	
 	/*
 	 *  giveAscensions() : Gives (int)amount ascensions to (String)username.
 	 */
 	public static void giveAscensions(int charID, int amount)
 	{
 		DDataUtil.saveCharData(charID, "char_ascensions", getAscensions(charID) + amount);
 	}
 
 	/*
 	 *  setDevotion() : Sets the (String)username's devotion to (int)amount for (String)deity.
 	 */
 	public static void setDevotion(int charID, int amount)
 	{
 		DDataUtil.saveCharData(charID, "char_devotion", amount);
 	}
 
 	/*
 	 *  subtractDevotion() : Subtracts (int)amount from the (String)username's (String)deity devotion.
 	 */
 	public static void subtractDevotion(int charID, int amount)
 	{
 		setDevotion(charID, getDevotion(charID) - amount);
 	}
 	
 	/*
 	 *  giveDevotion() : Gives (int)amount devotion to (String)username for (String)deity.
 	 */
 	public static void giveDevotion(int charID, int amount)
 	{
 		setDevotion(charID, getDevotion(charID) + amount);
 	}
 	
 	/*
 	 *  setAlliance() : Sets the (int)charID's alliance to (String)alliance.
 	 */
 	public static void setAlliance(int charID, String alliance)
 	{
 		DDataUtil.saveCharData(charID, "char_alliance", alliance);
 	}
 	
 	/*
 	 *  isActive() : Returns a boolean for if the (int)charID is active.
 	 */
 	public static boolean isActive(int charID)
 	{
 		if(DDataUtil.getCharData(charID, "char_active") == null) return false;
 		else return DObjUtil.toBoolean(DDataUtil.getCharData(charID, "char_active"));
 	}
 	
 	/*
 	 *  isEnabledAbility() : Returns a boolean for if (String)ability is enabled for (String)username.
 	 */
 	public static boolean isEnabledAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DDataUtil.getCharData(charID, "boolean_" + ability.toLowerCase()) != null)
 		{
 			return DObjUtil.toBoolean(DDataUtil.getCharData(charID, "boolean_" + ability.toLowerCase()));
 		}
 		else return false;
 	}
 	
 	/*
 	 *  enableAbility() : Enables (String)ability for (String)player.
 	 */
 	public static void enableAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(!isEnabledAbility(player, ability))
 		{
 			DDataUtil.saveCharData(charID,  "boolean_" + ability.toLowerCase(), true);
 		}
 	}
 	
 	/*
 	 *  disableAbility() : Disables (String)ability for (String)player.
 	 */
 	public static void disableAbility(Player player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(isEnabledAbility(player, ability))
 		{
 			DDataUtil.saveCharData(charID,  "boolean_" + ability.toLowerCase(), false);
 		}
 	}
 	
 	/*
 	 *  isCooledDown() : Returns a boolean for is (String)ability is cooled down.
 	 */
 	public static boolean isCooledDown(Player player, String ability, long ability_time, boolean sendMsg)
 	{
 		if(ability_time > System.currentTimeMillis())
 		{
 			if(sendMsg) player.sendMessage(ChatColor.RED + ability + " has not cooled down!");
 			return false;
 		}
 		else return true;
 	}
 	
 	/*
 	 *  getBind() : Returns the bind for (String)username's (String)ability.
 	 */
 	public static Material getBind(OfflinePlayer player, String ability)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DDataUtil.getCharData(charID, ability + "_bind") != null)
 		{
 			Material material = (Material) DDataUtil.getCharData(charID, ability + "_bind");
 			return material;
 		}
 		else return null;
 	}
 	
 	/*
 	 *  getBindings() : Returns all bindings for (Player)player.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Material> getBindings(OfflinePlayer player)
 	{		
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		if(DPlayerUtil.hasCharID(player, charID))
 		{
 			return (ArrayList<Material>) DDataUtil.getCharData(charID, "bindings");
 		}
 		else return new ArrayList<Material>();
 	}
 	
 	/*
 	 *  setBound() : Sets (Material)material to be bound for (Player)player.
 	 */
 	public static boolean setBound(OfflinePlayer player, String ability, Material material)
 	{	
 		int charID = DPlayerUtil.getCurrentChar(player);
 		
 		if(DDataUtil.getCharData(charID, ability + "_bind") == null)
 		{
 			if(((Player) player).getItemInHand().getType() == Material.AIR)
 			{
 				((Player) player).sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 			}
 			else
 			{
 				if(isBound(player, material))
 				{
 					((Player) player).sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
 					return false;
 				}
 				else if(material == Material.AIR)
 				{
 					((Player) player).sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
 					return false;
 				}
 				else
 				{			
 					if(DDataUtil.hasCharData(charID, "bindings"))
 					{
 						ArrayList<Material> bindings = getBindings(player);
 						
 						if(!bindings.contains(material)) bindings.add(material);
 						
 						DDataUtil.saveCharData(charID, "bindings", bindings);
 					}
 					else
 					{
 						ArrayList<Material> bindings = new ArrayList<Material>();
 						
 						bindings.add(material);
 						DDataUtil.saveCharData(charID, "bindings", bindings);
 					}
 					
 					DDataUtil.saveCharData(charID, ability + "_bind", material);
 					((Player) player).sendMessage(ChatColor.YELLOW + ability + " is now bound to: " + material.name().toUpperCase());
 					return true;
 				}
 			}
 		}
 		else
 		{
 			removeBind(player, ability, ((Material) DDataUtil.getCharData(charID, ability + "_bind")));
 			((Player) player).sendMessage(ChatColor.YELLOW + ability + "'s bind has been removed.");
 		}
 		return false;
 	}
 	
 	/*
 	 *  isBound() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean isBound(OfflinePlayer player, Material material)
 	{
 		if(getBindings(player) != null && getBindings(player).contains(material)) return true;
 		else return false;
 	}
 	
 	/*
 	 *  removeBind() : Checks if (Material)material is bound for (Player)player.
 	 */
 	public static boolean removeBind(OfflinePlayer player, String ability, Material material)
 	{
 		int charID = DPlayerUtil.getCurrentChar(player);
 
 		ArrayList<Material> bindings = null;
 
 		if(DDataUtil.hasCharData(charID, "bindings"))
 		{
 			bindings = getBindings(player);
 			
 			if(bindings != null && bindings.contains(material)) bindings.remove(material);
 		}
 		
 		DDataUtil.saveCharData(charID, "bindings", bindings);
 		DDataUtil.removeCharData(charID, ability + "_bind");
 
 		return true;
 	}
 }
