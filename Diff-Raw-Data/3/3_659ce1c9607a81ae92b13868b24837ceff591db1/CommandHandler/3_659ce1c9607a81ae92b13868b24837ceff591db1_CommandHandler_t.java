 package Lihad.Conflict.Command;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
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
		Player player = null;
		if(sender instanceof Player)player = (Player)sender;
 
 		if(cmd.getName().equalsIgnoreCase("point") && arg.length == 2 && arg[0].equalsIgnoreCase("set") && sender instanceof Player && (player).isOp()){
 			if(!Conflict.PLAYER_SET_SELECT.isEmpty() && Conflict.PLAYER_SET_SELECT.containsKey((player).getName())){
 				(player).sendMessage(ChatColor.LIGHT_PURPLE.toString()+"Selection turned off");
 				Conflict.PLAYER_SET_SELECT.remove((player).getName());
 			}else if(Conflict.getCity(arg[1]) != null
 					|| arg[1].equalsIgnoreCase("blacksmith") || arg[1].equalsIgnoreCase("potions") || arg[1].equalsIgnoreCase("enchantments")
 					|| arg[1].equalsIgnoreCase("richportal") || arg[1].equalsIgnoreCase("mystportal")
 					|| (arg[1].length() > 7 
 						&& arg[1].substring(0, 7).equalsIgnoreCase("drifter") 
 						&& Conflict.getCity(arg[1].substring(7)) != null)){
 				(player).sendMessage(ChatColor.LIGHT_PURPLE.toString()+"Please select a position");
 				Conflict.PLAYER_SET_SELECT.put((player).getName(), arg[1]);
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("cwho")){
 			if(arg.length == 1){
 				City city = Conflict.getCity(arg[1]);
 				if (city != null) {
 					List<Player> players = Arrays.asList(plugin.getServer().getOnlinePlayers());
 					String message = "";
 					for(int i = 0;i<players.size();i++){
 						if(city.hasPlayer(players.get(i).getName()))
 							message = message.concat(players.get(i).getName() + " ");
 					}
 					(player).sendMessage(message);
 				}else if(plugin.getServer().getPlayer(arg[0]) != null){
 					city = Conflict.getPlayerCity(arg[0]);
 					if (city != null)
 						player.sendMessage( arg[0] + " - " + city.getName());
 					else
 						player.sendMessage( arg[0] + " - <None>");
 				}else (player).sendMessage("Player is not online");
 
 			}else (player).sendMessage("try '/cwho <playername>|<capname>");
 			return true;
 
 		}else if(cmd.getName().equalsIgnoreCase("rarity") && arg.length == 0){
 			if(BeyondUtil.rarity((player).getItemInHand()) >= 60)(player).sendMessage("The Rarity Index of your "+ChatColor.BLUE.toString()+(player).getItemInHand().getType().name()+" is "+BeyondUtil.getColorOfRarity(BeyondUtil.rarity((player).getItemInHand()))+BeyondUtil.rarity((player).getItemInHand()));
 			else (player).sendMessage("This item has no Rarity Index");
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("myst") && arg.length == 3){
 			if((player).getWorld().getName().equals("mystworld")){	
 				if(Integer.parseInt(arg[0]) < 100000 && Integer.parseInt(arg[0]) > -100000 
 						&& Integer.parseInt(arg[1]) < 255 && Integer.parseInt(arg[1]) > 0
 						&& Integer.parseInt(arg[2]) < 100000 && Integer.parseInt(arg[2]) > -100000){
 					(player).teleport(new Location(plugin.getServer().getWorld("survival"),Integer.parseInt(arg[0]),Integer.parseInt(arg[1]),Integer.parseInt(arg[2])));
 				}
 			}else (player).sendMessage("You are not in the correct world to use this command");
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("protectcity") && arg.length == 1){
 			if(Integer.parseInt(arg[0]) <= 500 && Integer.parseInt(arg[0]) > -1){
 				City city = Conflict.getPlayerCity(player.getName());
 				if (city.getGenerals().contains(player.getName()))
 					city.setProtectionRadius(Integer.parseInt(arg[0]));
 			}else{
 				(player).sendMessage("Invalid number. 0-500");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("cca") && ((sender instanceof Player && (player).isOp()) || sender instanceof ConsoleCommandSender)){
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
 				String playerName = plugin.getFormattedPlayerName(arg[2]);
 				if(playerName != null) {
 					City city = Conflict.getCity(arg[1]);
 					City oldCity = Conflict.getPlayerCity(playerName);
 
 					if (city != null && oldCity != null && !city.equals(oldCity)) {
 						oldCity.removePlayer(playerName);
 						city.addPlayer(playerName);
 						sender.sendMessage("Player " + playerName + " is now a member of " + city.getName());
 					}else{
 						sender.sendMessage("Invalid city.  Try one of: " + Conflict.cities.toString());
 					}
 				}else{
 					sender.sendMessage("Player has not logged in before.  Please wait until they have at least played here.");
 				}
 			}else if(arg.length == 3 && arg[0].equalsIgnoreCase("gassign")){
 				String playerName = plugin.getFormattedPlayerName(arg[2]);
 				if(playerName != null) {
 					City city = Conflict.getCity(arg[1]);
 					City oldCity = Conflict.getPlayerCity(playerName);
 					if (city != null && oldCity != null && city.equals(oldCity)) { //player is a member of the town you are assigning them as general to
 						city.getGenerals().add(playerName);
 						Conflict.ex.getUser(playerName).setPrefix(ChatColor.WHITE.toString() + "["
 								+ ChatColor.LIGHT_PURPLE.toString() + city.getName().substring(0, 2).toUpperCase()
 								+ "-General" + ChatColor.WHITE.toString() + "]", null);
 						sender.sendMessage("Player " + playerName + " is now one of " + city.getName() + "'s Generals");
 					}else{
 						sender.sendMessage("Player " + playerName + " is not a member of " + arg[1]);
 					}
 				}else{
 					sender.sendMessage("Player has not logged in before.  Please wait until they have at least played here.");
 				}
 			}else if(arg.length == 3 && arg[0].equalsIgnoreCase("gremove")){
 				String playerName = plugin.getFormattedPlayerName(arg[2]);
 				if(playerName != null) {
 					City city = Conflict.getCity(arg[1]);
 					City oldCity = Conflict.getPlayerCity(playerName);
 					if (city != null && oldCity != null && city.equals(oldCity)) { //player is a member of the town you are assigning them as general to
 						if (city.getGenerals().contains(playerName)) {
 							Conflict.ex.getUser(playerName).setPrefix("", null);
 							sender.sendMessage("Player " + playerName + " is no longer one of " + city.getName() + "'s Generals");
 						} else {
 							sender.sendMessage("Player " + playerName + " was already not a general for " + city.getName());
 						}
 					}else{
 						sender.sendMessage("Player " + playerName + " is not a member of " + arg[1]);
 					}
 				}else{
 					sender.sendMessage("Player has not logged in before.  Please wait until they have at least played here.");
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
 					sender.sendMessage("Invalid city.  Try one of: " + Conflict.cities.toString());
 				}
 			}else if(arg.length == 4 && arg[0].equalsIgnoreCase("worth") && arg[1].equalsIgnoreCase("set")){
 				City city = Conflict.getCity(arg[2]);
 				if (city != null) {
 					city.setMoney(Integer.parseInt(arg[3]));
 					sender.sendMessage(city.getName() + " worth = " + city.getMoney());
 				}else{
 					sender.sendMessage("Invalid city.  Try one of: " + Conflict.cities.toString());
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("generals")){
 				for (int i=0; i<Conflict.cities.length; i++) {
 					sender.sendMessage(Conflict.cities[i].getName() + " - " + Conflict.cities[i].getGenerals());
 				}
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("save")){
 				Conflict.saveInfoFile();
 			}else if(arg.length == 1 && arg[0].equalsIgnoreCase("reload")){
 				Conflict.loadInfoFile(Conflict.information, Conflict.infoFile);
 				BeyondInfo.loader();
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("cc") && arg.length == 0){
 			City city = Conflict.getPlayerCity(player.getName());
 			if (city != null)
 				player.performCommand("ch " + city.getName());
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("post") && arg.length == 0){
 			if(BeyondUtil.rarity((player).getItemInHand()) >= 60){
 				(player).chat(BeyondUtil.getColorOfRarity(BeyondUtil.rarity((player).getItemInHand()))+"["+(player).getItemInHand().getType().name()+"] Rarity Index : "+BeyondUtil.rarity((player).getItemInHand()));
 				post = (player).getItemInHand();
 			}
 			else (player).sendMessage("This item has no Rarity Index so it can't be posted to chat");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("look") && arg.length == 0){
 			if(post != null){
 				player.sendMessage(ChatColor.YELLOW.toString()+" -------------------------------- ");
 				player.sendMessage(BeyondUtil.getColorOfRarity(BeyondUtil.rarity(post))+"["+post.getType().name()+"] Rarity Index : "+BeyondUtil.rarity(post));
 				for(int i = 0; i<post.getEnchantments().keySet().size(); i++){
 					player.sendMessage(" -- "+ChatColor.BLUE.toString()+((Enchantment)(post.getEnchantments().keySet().toArray()[i])).getName()+ChatColor.WHITE.toString()+" LVL"+BeyondUtil.getColorOfLevel(post.getEnchantmentLevel(((Enchantment)(post.getEnchantments().keySet().toArray()[i]))))+post.getEnchantmentLevel(((Enchantment)(post.getEnchantments().keySet().toArray()[i]))));
 				}
 				if(post.getEnchantments().keySet().size() <= 0)player.sendMessage(ChatColor.WHITE.toString()+" -- This Item Has No Enchants");
 				player.sendMessage(ChatColor.YELLOW.toString()+" -------------------------------- ");
 			}else (player).sendMessage("There is no item to look at");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("gear") && arg.length == 0){
 			double total = (BeyondUtil.rarity((player).getInventory().getHelmet())+BeyondUtil.rarity((player).getInventory().getLeggings())+BeyondUtil.rarity((player).getInventory().getBoots())+BeyondUtil.rarity((player).getInventory().getChestplate())+BeyondUtil.rarity((player).getInventory().getItemInHand()))/5.00;
 			(player).sendMessage(BeyondUtil.getColorOfRarity(total)+"Your Gear Rating is : "+total);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("gear") && arg.length == 1){
 			if(plugin.getServer().getPlayer(arg[1]) != null){
 				Player target = plugin.getServer().getPlayer(arg[1]);
 				org.bukkit.inventory.PlayerInventory inv = target.getInventory();
 				double total = (BeyondUtil.rarity(inv.getHelmet())+BeyondUtil.rarity(inv.getLeggings())+BeyondUtil.rarity(inv.getBoots())+BeyondUtil.rarity(inv.getChestplate())+BeyondUtil.rarity(inv.getItemInHand()))/5.00;
 				player.sendMessage(target.getName()+" has a Gear Rating of "+BeyondUtil.getColorOfRarity(total)+total);
 			}else player.sendMessage("This player either doesn't exist, or isn't online");
 			return true;
 		}else if(Conflict.getCity(cmd.getName()) != null && arg.length == 0 && Conflict.UNASSIGNED_PLAYERS.contains((player).getName())){
 			City city = Conflict.getCity(cmd.getName());
 			int least = Integer.MAX_VALUE;
 			for (int i=0; i<Conflict.cities.length; i++) {
 				if (Conflict.cities[i].getPopulation() < least)
 					least = Conflict.cities[i].getPopulation();
 			}
 			if (least < (city.getPopulation() - 10))
 				(player).sendMessage(ChatColor.BLUE.toString() + city.getName() + " is over capacity!  Try joining one of the others, or wait and try later.");
 			else{
 				city.addPlayer(player.getName());
 				Conflict.UNASSIGNED_PLAYERS.remove(player.getName());
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("spawn") && arg.length == 0){
 			City city = Conflict.getPlayerCity(player.getName());
 			if (city != null && city.getSpawn() != null)
 				player.teleport(city.getSpawn());
 			else
 				player.teleport(player.getWorld().getSpawnLocation());
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("nulls") && arg.length == 0 && (player).isOp()){
 			(player).teleport((player).getWorld().getSpawnLocation());
 		}else if(cmd.getName().equalsIgnoreCase("setcityspawn") && arg.length == 0){
 			City city = Conflict.getPlayerCity(player.getName());
 			if (city != null && city.getGenerals().contains(player.getName()))
 				city.setSpawn(player.getLocation());
 			else
 				player.sendMessage(ChatColor.LIGHT_PURPLE.toString()+"Unable to use this command");
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("purchase") && arg.length == 0){
 			sender.sendMessage("Possible Perks: weapondrops, armordrops, potiondrops, tooldrops, bowdrops, shield, strike, endergrenade, enchantup, golddrops");
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("purchase") && arg.length == 1){
 			City city = Conflict.getPlayerCity(player.getName());
 			if(city != null && city.getGenerals().contains(player.getName())){
 				if(city.getMoney() >= 500){
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
 							(player).sendMessage("Invalid perk");
 					}else
 						(player).sendMessage(city.getName() + " currently owns perk: " + arg[0]);
 				}else
 					(player).sendMessage(city.getName() + " does not have enough gold to purchase the ability");
 			}else
 				player.sendMessage(ChatColor.LIGHT_PURPLE.toString()+"Unable to use this command");
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("warstats") && arg.length == 0){
 			if (Conflict.war != null) {
 				Conflict.war.postWarAutoList(sender);
 			}
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("perks")) {
 			City city = null;
 			if (arg.length == 0 && sender instanceof Player) {
 				city = Conflict.getPlayerCity((player).getName());
 			}
 			if (arg.length == 1 && (player).isOp()) {
 				for (City c : Conflict.cities) {
 					if (c.getName().equals(arg[0])) {
 						city = c;
 						break;
 					}
 				}
 			}
 
 			if (city == null) { return false; }
 
 			sender.sendMessage("" + city + " mini-perks: " + city.getPerks().toString());
 
 			// TODO: Give nodes in this message as well.
 			sender.sendMessage("" + city + " nodes: " + city.getTrades().toString());
 
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("bnn") && Conflict.war != null && arg.length == 2 && arg[0].equalsIgnoreCase("reporter") && arg[1].equalsIgnoreCase("enable") && (sender instanceof Player)) {
 			Conflict.war.reporters.add(player);                
 		}
 		return false;
 	}
 }
