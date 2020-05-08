 package Lihad.Conflict.Command;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import Lihad.Conflict.Conflict;
 import Lihad.Conflict.City;
 import Lihad.Conflict.Util.BeyondUtil;
 import Lihad.Conflict.Information.BeyondInfo;
 
 public class CommandHandler implements CommandExecutor {
 	public static Conflict plugin;
 	public ItemStack post;
 	public CommandHandler(Conflict instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String string, String[] arg) {
 		// if (!(sender instanceof Player) {
 		// // Console can't send Conflict commands.  Sorry.
 		// return false;
 		// }
 		if(cmd.getName().equalsIgnoreCase("point")) {
 			return handlePoint(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("cwho")) {
 			return handleCWho(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("rarity")) {
 			return handleRarity(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("myst")) {
 			return handleMyst(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("protectcity")) {
 			return handleProtectCity(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("cca")) {
 			return handleCCA(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("cc")) {
 			return handleCC(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("post")) {
 			return handlePost(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("look")) {
 			return handleLook(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("gear")) {
 			return handleGear(sender, arg);
 		}else if(Conflict.getCity(cmd.getName()) != null) {
 			return handleCity(sender, cmd.getName(), arg);
 		}else if(cmd.getName().equalsIgnoreCase("join")) {
 			return handleJoinCity(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("spawn")) {
 			return handleSpawn(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("nulls")) {
 			return handleNulls(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("setcityspawn")) {
 			return handleSetCitySpawn(sender, arg);
 		}else if(cmd.getName().equalsIgnoreCase("purchase")) {
 			return handlePurchase(sender, arg);
 		}
 		else if(cmd.getName().equalsIgnoreCase("perks")) {
 			return handlePerks(sender, arg);
 		}
 		else if (cmd.getName().equalsIgnoreCase("ccd")) {
 			return handleCCD(sender, arg);
 		}
 		return false;
 	}
 
 	private boolean handleCCD(CommandSender sender, String[] arg) {
         if (sender.isOp() || (sender instanceof Player && Conflict.handler.has((Player)sender, "conflict.debug"))) {
             if (arg.length > 0) {
                 // Random debug info for running Conflict instance
                 if (arg[0].equalsIgnoreCase("version")) {
                     sender.sendMessage("Conflict version " + org.bukkit.Bukkit.getPluginManager().getPlugin("Conflict").getDescription().getVersion());
                     return true;
                 }
                 if (arg[0].equalsIgnoreCase("nerfme")) {
                     if (sender instanceof Player) { 
                         BeyondUtil.nerfOverenchantedPlayerInventory((Player)sender);
                     }
                     else {
                         sender.sendMessage("Command not available from console.");
                     }
                     return true;
                 }
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /nulls command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleNulls(CommandSender sender, String[] arg) {
 		if (sender.isOp() && sender instanceof Player) {
 			Player player = (Player) sender;
 			player.teleport(player.getWorld().getSpawnLocation());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /[CITYNAME]  command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleCity(CommandSender sender, String name, String[] arg) {
 		City city = Conflict.getCity(name);
 		if (arg.length > 0 && arg[0].equalsIgnoreCase("list")) {
 			sender.sendMessage(city.getInfo(true));
 		} else {
 			sender.sendMessage(city.getInfo(false));
 		}
 		return true;
 	}
 
 	/**
 	 * Processes a /[CITYNAME] join confirm command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleJoinCity(CommandSender sender, String[] arg) {
 		if (sender instanceof Player) {
 			if (arg.length >= 1) {
 				City city = Conflict.getCity(arg[0]);
 				if (city == null) {
 					sender.sendMessage(Conflict.ERRORCOLOR + arg[0] + Conflict.TEXTCOLOR
 							+ "is an invalid city, please try one of: "
 							+ Conflict.CITYCOLOR + Conflict.cities);
 				}
 				else if (arg.length == 2) {
 					if (arg[1].equalsIgnoreCase("confirm")) {
 						plugin.joinCity(sender, sender.getName(), city.getName(), false);						
 					}
 				} else {
 					sender.sendMessage(Conflict.TEXTCOLOR + "Please confirm that you want to join "
 							+ Conflict.CITYCOLOR + city.getName() + " by typing /join " + city.getName() + " confirm");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /setcityspawn command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleSetCitySpawn(CommandSender sender, String[] arg) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			City city = Conflict.getPlayerCity(player.getName());
 			if (city != null && city.getMayors().contains(player.getName())) {
 				city.setSpawn(player.getLocation());
 				return true;
 			}
 		}
 		return false;			
 	}
 
 	/**
 	 * Processes a /spawn command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleSpawn(CommandSender sender, String[] arg) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			City city = Conflict.getPlayerCity(player.getName());
 			if (city != null && city.getSpawn() != null)
 				player.teleport(city.getSpawn());
 			else
 				player.teleport(player.getWorld().getSpawnLocation());
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /perks command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handlePerks(CommandSender sender, String[] arg) {
 		City city = null;
 		if (arg.length == 0) {
 			city = Conflict.getPlayerCity(sender.getName());
 		} else if (arg.length == 1 && sender.isOp()) {
 			for (City c : Conflict.cities) {
 				if (c.getName().equals(arg[0])) {
 					city = c;
 					break;
 				}
 			}
 		}
 
 		if (city == null) {
 			return false;
 		}
 		sender.sendMessage(Conflict.CITYCOLOR + city.getName() + Conflict.TEXTCOLOR
 				+ " mini-perks: " + Conflict.PERKCOLOR + city.getPerks());
 		sender.sendMessage(Conflict.CITYCOLOR + city.getName() + Conflict.TEXTCOLOR
 				+ " nodes: " + Conflict.TRADECOLOR + city.getTrades());
 		return true;
 	}
 
 	/**
 	 * Processes a /purchase command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 
     private boolean handlePurchase(CommandSender sender, String[] arg) {
         if (arg.length > 0) {
             City city = Conflict.getPlayerCity(sender.getName());
             if(city != null && city.getMayors().contains(sender.getName())){
                 if(!city.getPerks().contains(arg[0].toLowerCase())){
                     if(arg[0].equalsIgnoreCase("weapondrops")){
                         city.addPerk("weapondrops");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("armordrops")){
                         city.addPerk("armordrops");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("potiondrops")){
                         city.addPerk("potiondrops");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("tooldrops")){
                         city.addPerk("tooldrops");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("bowdrops")){
                         city.addPerk("bowdrops");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("shield")){
                         city.addPerk("shield");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("strike")){
                         city.addPerk("strike");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("endergrenade")){
                         city.addPerk("endergrenade");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("enchantup")){
                         city.addPerk("enchantup");
                         city.subtractMoney(500);
                     }else if(arg[0].equalsIgnoreCase("golddrops")){
                         city.addPerk("golddrops");
                         city.subtractMoney(500);
                     }else
                         sender.sendMessage(Conflict.ERRORCOLOR + "Invalid perk.  Possible perks: " + Conflict.PERKCOLOR + "weapondrops, armordrops, potiondrops, tooldrops, bowdrops, shield, strike, endergrenade, enchantup, golddrops");
                 }else
                     sender.sendMessage(Conflict.CITYCOLOR + city.getName() + Conflict.ERRORCOLOR + " currently owns perk: " + Conflict.PERKCOLOR + arg[0]);
             }else
                 sender.sendMessage(Conflict.ERRORCOLOR + "Unable to use this command");
         }else
             sender.sendMessage(Conflict.TEXTCOLOR + "Possible perks: " + Conflict.PERKCOLOR + "weapondrops, armordrops, potiondrops, tooldrops, bowdrops, shield, strike, endergrenade, enchantup, golddrops");
         return true;
     }
 
 	/**
 	 * Calculates the gear rating of the specified player.
 	 * @param player - The player whose gear should be rated.
 	 * @return double - The rating of the player's gear.
 	 */
 	private double getGearRating(Player player) {
 		return (BeyondUtil.rarity(player.getInventory().getHelmet())
 				+ BeyondUtil.rarity(player.getInventory().getLeggings())
 				+ BeyondUtil.rarity(player.getInventory().getBoots())
 				+ BeyondUtil.rarity(player.getInventory().getChestplate())
 				+ BeyondUtil.rarity(player.getInventory().getItemInHand())
 				)/5.00;
 	}
 
 	/**
 	 * Processes a /gear command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleGear(CommandSender sender, String[] arg) {
 		if (arg.length == 0 && sender instanceof Player) {
 			Player player = (Player) sender;
 			double total = getGearRating(player);
 			sender.sendMessage(BeyondUtil.getColorOfRarity(total) + Conflict.TEXTCOLOR + "Your Gear Rating is : " + total);
 			return true;
 		}
 		else if(arg.length > 0) {
 			if(plugin.getServer().getPlayer(arg[1]) != null) {
 				Player target = plugin.getServer().getPlayer(arg[1]);
 				double total = getGearRating(target);
 				sender.sendMessage(Conflict.PLAYERCOLOR + target.getName() + Conflict.TEXTCOLOR + " has a Gear Rating of "
 						+ BeyondUtil.getColorOfRarity(total) + total);
 			}else sender.sendMessage(Conflict.TEXTCOLOR + "Player " + Conflict.PLAYERCOLOR + arg[1] + Conflict.TEXTCOLOR + " either doesn't exist, or isn't online");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /look command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleLook(CommandSender sender, String[] arg) {
 		if(post != null){
 			sender.sendMessage(Conflict.HEADERCOLOR + " -------------------------------- ");
 			sender.sendMessage(BeyondUtil.getColorOfRarity(BeyondUtil.rarity(post))
 					+ "[" + post.getType().name() + "] Rarity Index : " + BeyondUtil.rarity(post));
 			for(int i = 0; i<post.getEnchantments().keySet().size(); i++){
 				sender.sendMessage(" -- " + ChatColor.BLUE
 						+ ((Enchantment)(post.getEnchantments().keySet().toArray()[i])).getName()
 						+ ChatColor.WHITE + " LVL"
 						+ BeyondUtil.getColorOfLevel(post.getEnchantmentLevel(((Enchantment)(post.getEnchantments().keySet().toArray()[i]))))
 						+ post.getEnchantmentLevel(((Enchantment)(post.getEnchantments().keySet().toArray()[i]))));
 			}
 			if(post.getEnchantments().keySet().size() <= 0)
 				sender.sendMessage(ChatColor.WHITE + " -- This Item Has No Enchants");
 			sender.sendMessage(Conflict.HEADERCOLOR + " -------------------------------- ");
 		}else sender.sendMessage("There is no item to look at");
 		return true;
 	}
 
 	/**
 	 * Processes a /post command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handlePost(CommandSender sender, String[] arg) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if(BeyondUtil.rarity(player.getItemInHand()) >= 60) {
 				player.chat(BeyondUtil.getColorOfRarity(BeyondUtil.rarity(player.getItemInHand()))
 						+ "[" + player.getItemInHand().getType().name() + "] Rarity Index : "
 						+ BeyondUtil.rarity(player.getItemInHand()));
 				post = player.getItemInHand();
 			}
 			else (player).sendMessage(Conflict.ERRORCOLOR + "This item has no Rarity Index so it can't be posted to chat");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /cc command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleCC(CommandSender sender, String[] arg) {
 		plugin.joinChat(sender.getName());
 		return true;
 	}
 
 	/**
 	 * Processes a /cca command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleCCA(CommandSender sender, String[] arg) {
 		if (sender.isOp()){
 			if(arg.length == 1 && arg[0].equalsIgnoreCase("count")){
 				sender.sendMessage("Count:");
 				for (int i=0; i<Conflict.cities.length; i++) {
 					sender.sendMessage(Conflict.cities[i].getName() + " - " + Conflict.cities[i].getPopulation());
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("trade")){
 				for (int i=0; i<Conflict.cities.length; i++) {
 					sender.sendMessage(Conflict.cities[i].getName() + " - " + Conflict.cities[i].getTrades());
 				}
 			}else if(arg.length == 3 && arg[0].equalsIgnoreCase("cmove")){
 				plugin.joinCity(sender, arg[2], arg[1], true);
 				return true;
 			}else if(arg.length == 3 && arg[0].equalsIgnoreCase("massign")){
 				String playerName = plugin.getFormattedPlayerName(arg[2]);
 				if(playerName != null) {
 					City city = Conflict.getCity(arg[1]);
 					City oldCity = Conflict.getPlayerCity(playerName);
 					if (city != null) {
 						if (!city.equals(oldCity))
 						{
 							boolean worked = plugin.joinCity(sender, playerName, arg[1], true);
 							if (!worked) {
 								return true;
 							}
 							if (oldCity != null) {
 								if (oldCity.getMayors().contains(playerName)) {
 									oldCity.removeMayor(playerName);
 									plugin.getServer().broadcastMessage(Conflict.TEXTCOLOR + "Player " + Conflict.PLAYERCOLOR
 											+ playerName + Conflict.TEXTCOLOR + " is no longer one of " + Conflict.CITYCOLOR
 											+ city.getName() + Conflict.TEXTCOLOR + "'s mayors :(");
 								}
 							}
 						}
 						city.getMayors().add(playerName);
 						Conflict.ex.getUser(playerName).setPrefix(ChatColor.WHITE + "["
 								+ ChatColor.LIGHT_PURPLE + city.getName().substring(0, 2).toUpperCase()
 								+ "-Mayor" + ChatColor.WHITE + "]", null);
 						plugin.getServer().broadcastMessage(Conflict.TEXTCOLOR + "Player " + Conflict.PLAYERCOLOR + playerName
 								+ Conflict.TEXTCOLOR + " is now one of " + Conflict.CITYCOLOR + city.getName()
 								+ Conflict.TEXTCOLOR + "'s mayors!");
 					} else {
 						sender.sendMessage(Conflict.ERRORCOLOR + "Invalid city.  Try one of: " + Conflict.CITYCOLOR + Conflict.cities);
 					}
 				}else{
 					sender.sendMessage(Conflict.ERRORCOLOR + "Player has not logged in before.  Please wait until they have at least played here.");
 				}
 			}else if(arg.length == 3 && arg[0].equalsIgnoreCase("mremove")){
 				String playerName = plugin.getFormattedPlayerName(arg[2]);
 				if(playerName != null) {
 					City city = Conflict.getCity(arg[1]);
 					if (city != null) {
 						if (city.getMayors().contains(playerName)) {
 							Conflict.ex.getUser(playerName).setPrefix("", null);
 							city.removeMayor(playerName);
 							plugin.getServer().broadcastMessage(Conflict.TEXTCOLOR + "Player " + Conflict.PLAYERCOLOR
 									+ playerName + " is no longer one of " + Conflict.CITYCOLOR
 									+ city.getName() + Conflict.TEXTCOLOR + "'s mayors :(");
 						} else {
 							sender.sendMessage(Conflict.ERRORCOLOR + "Player " + Conflict.PLAYERCOLOR
 									+ playerName + Conflict.ERRORCOLOR + " wasn't one of " 
 									+ Conflict.CITYCOLOR + city.getName() + Conflict.ERRORCOLOR
 									+ "'s mayors to begin with.");
 						}
 					}else{
 						sender.sendMessage(Conflict.ERRORCOLOR + "Invalid city.  Try one of: " + Conflict.CITYCOLOR + Conflict.cities);
 					}
 				}else{
 					sender.sendMessage(Conflict.ERRORCOLOR + "Player has not logged in before.  Please wait until they have at least played here.");
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("worth")){
 				sender.sendMessage("Worth:");
 				for (int i=0; i<Conflict.cities.length; i++) {
 					sender.sendMessage(Conflict.cities[i].getName() + " - " + Conflict.cities[i].getMoney());
 				}
 			}else if(arg.length == 4 && arg[0].equalsIgnoreCase("worth") && arg[1].equalsIgnoreCase("modify")){
 				City city = Conflict.getCity(arg[2]);
 				if (city != null) {
 					city.addMoney(Integer.parseInt(arg[3]));
 					sender.sendMessage(city.getName() + " worth = " + city.getMoney());
 				}else{
 					sender.sendMessage("Invalid city.  Try one of: " + Conflict.cities);
 				}
 			}else if(arg.length == 4 && arg[0].equalsIgnoreCase("worth") && arg[1].equalsIgnoreCase("set")){
 				City city = Conflict.getCity(arg[2]);
 				if (city != null) {
 					city.setMoney(Integer.parseInt(arg[3]));
 					sender.sendMessage(city.getName() + " worth = " + city.getMoney());
 				}else{
 					sender.sendMessage(Conflict.ERRORCOLOR + arg[2] + Conflict.TEXTCOLOR
 							+ " is an invalid city.  Try one of: " + Conflict.CITYCOLOR + Conflict.cities);
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("mayors")){
 				for (int i=0; i<Conflict.cities.length; i++) {
 					sender.sendMessage(Conflict.CITYCOLOR + Conflict.cities[i].getName() + Conflict.TEXTCOLOR + " - "
 							+ Conflict.MAYORCOLOR + Conflict.cities[i].getMayors());
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("save")){
 				Conflict.saveInfoFile();
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("reload")){
 				Conflict.loadInfoFile(Conflict.information, Conflict.infoFile);
 				BeyondInfo.loader();
 			}else if(arg.length == 2 && arg[0].equalsIgnoreCase("reset")){
 				plugin.reset(sender, arg[1]);
 				return true;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /protectcity command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleProtectCity(CommandSender sender, String[] arg) {
 		if (arg.length > 0) {
 			City city = Conflict.getPlayerCity(sender.getName());
 			if (city != null && city.getMayors().contains(sender.getName())) {
 				if(Integer.parseInt(arg[0]) <= 500 && Integer.parseInt(arg[0]) > -1)
 				{
 					city.setProtectionRadius(Integer.parseInt(arg[0]));
 					sender.sendMessage(Conflict.CITYCOLOR + city.getName() + Conflict.TEXTCOLOR + "'s protection radius is now " + arg[0]);
 				}
 				else
 					sender.sendMessage(Conflict.ERRORCOLOR + "Invalid number; must be within 0-500");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /myst command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleMyst(CommandSender sender, String[] arg) {
 		if (arg.length == 3 && sender instanceof Player){
 			Player player = (Player) sender;
 			if(player.getWorld().getName().equals("mystworld")) {
 				if(Math.abs(Integer.parseInt(arg[0])) < 100000
 						&& Integer.parseInt(arg[1]) < 255 && Integer.parseInt(arg[1]) > 0
 						&& Math.abs(Integer.parseInt(arg[2])) < 100000){
 					Location loc = new Location(plugin.getServer().getWorld("survival"), Integer.parseInt(arg[0]), Integer.parseInt(arg[1]), Integer.parseInt(arg[2]));
                     for (City city : Conflict.cities) {
                         if (city.isInRadius(loc)) {
                             player.sendMessage(Conflict.ERRORCOLOR + "The awesomeness of the capitol city prevents you.");
                             return true;
                         }
                     }
                     player.teleport(loc);
 				}
 			} else player.sendMessage(Conflict.ERRORCOLOR + "You are not in the correct world to use this command");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /point command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handlePoint(CommandSender sender, String[] arg) {
 		if (sender instanceof Player && sender.isOp() && arg.length == 2 && arg[0].equalsIgnoreCase("set")) {
 			if(!Conflict.PLAYER_SET_SELECT.isEmpty() && Conflict.PLAYER_SET_SELECT.containsKey(sender.getName())){
 				sender.sendMessage(Conflict.PROMPTCOLOR + "Selection turned off");
 				Conflict.PLAYER_SET_SELECT.remove(sender.getName());
 			}else if(Conflict.getCity(arg[1]) != null
 					|| arg[1].equalsIgnoreCase("blacksmith") || arg[1].equalsIgnoreCase("potions") || arg[1].equalsIgnoreCase("enchantments")
 					|| arg[1].equalsIgnoreCase("richportal") || arg[1].equalsIgnoreCase("mystportal")
 					|| (arg[1].length() > 7 
 							&& arg[1].substring(0, 7).equalsIgnoreCase("drifter") 
 							&& Conflict.getCity(arg[1].substring(7)) != null)) {
 				sender.sendMessage(Conflict.PROMPTCOLOR + "Please select a position");
 				Conflict.PLAYER_SET_SELECT.put(sender.getName(), arg[1]);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /rarity command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleRarity(CommandSender sender, String[] arg) {
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 			if(BeyondUtil.rarity(player.getItemInHand()) >= 60)
 				player.sendMessage(Conflict.TEXTCOLOR + "The Rarity Index of your "
 						+ Conflict.ITEMCOLOR + player.getItemInHand().getType().name()
 						+ " is " + BeyondUtil.getColorOfRarity(BeyondUtil.rarity(player.getItemInHand()))
 						+ BeyondUtil.rarity(player.getItemInHand()));
 			else
 				player.sendMessage(Conflict.TEXTCOLOR + "This item has no Rarity Index");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Processes a /cwho command.
 	 * @param sender - The sender of the command.
 	 * @param arg - The arguments.
 	 * @return boolean - True if responded to, false if not.
 	 */
 	private boolean handleCWho(CommandSender sender, String[] arg) {
 		if(arg.length > 0){
 			City city = Conflict.getCity(arg[0]);
 			if (city != null) {
 				List<Player> players = Arrays.asList(plugin.getServer().getOnlinePlayers());
 				String message = Conflict.CITYCOLOR + city.getName() + Conflict.PLAYERCOLOR + ": ";
 				boolean firstOne = true;				
 				for(int i = 0;i<players.size();i++){
 					if (firstOne)
 						firstOne = false;
 					else
 						message += ", ";
 					if(city.hasPlayer(players.get(i).getName()))
 						message = message.concat(Conflict.PLAYERCOLOR + players.get(i).getName() + " ");
 				}
 				sender.sendMessage(message);
 			}else if(plugin.getFormattedPlayerName(arg[0]) != null){
 				String message = Conflict.TEXTCOLOR + "Player " + Conflict.PLAYERCOLOR + arg[0]
 						+ Conflict.TEXTCOLOR + " is ";
 				if (plugin.getServer().getPlayer(arg[0]) != null) 
 					message += Conflict.YESCOLOR + "ONLINE";
 				else
 					message += Conflict.NOCOLOR + "OFFLINE";
 				String cityName = Conflict.NOCOLOR + "<None>";
 				city = Conflict.getPlayerCity(arg[0]);
 				if (city != null)
 					cityName = Conflict.CITYCOLOR + city.getName();
 				message += Conflict.TEXTCOLOR + " - " + cityName;
 				sender.sendMessage(message);
 			}else sender.sendMessage(Conflict.TEXTCOLOR + "Player " + Conflict.ERRORCOLOR + arg[0]
 					+ Conflict.TEXTCOLOR + " has not played here before (or this plugin's just dumb, whichever)");
 		}else sender.sendMessage(Conflict.TEXTCOLOR + "try '/cwho <playername>|<cityname>");
 		return true;
 	}
 }
