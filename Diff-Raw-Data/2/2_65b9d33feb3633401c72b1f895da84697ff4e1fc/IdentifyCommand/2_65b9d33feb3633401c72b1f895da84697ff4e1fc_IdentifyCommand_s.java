 package com.modcrafting.identify.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.modcrafting.diablodrops.items.Socket;
 import com.modcrafting.identify.Identify;
 /*
  * 
  */
 public class IdentifyCommand implements CommandExecutor {
 	Identify plugin;
 	public IdentifyCommand(Identify identify) {
 		this.plugin = identify;
 		
 	}
 	@SuppressWarnings("deprecation")
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (!hasPerms(sender,command.getPermission()))
 			return true;
 		Player player = null;
 		if (sender instanceof Player){
 			player = (Player)sender;
 		}
 		if(args.length<1) return false;
 		if(args[0].equalsIgnoreCase("list")&&hasPerms(sender,"identify.list")){
 
 			if(args.length < 2){
 				showList(player);
 				return true;
 			}
 			if(args[1].equalsIgnoreCase("dd")&&plugin.getDiabloDrops()!=null){
 				//TODO: DD daily list.
 				//TODO: DD Tier list.
 				//TODO: DD Custom buy.
 				return true;
 			}
 			return true;
 		}
 		if(args[0].equalsIgnoreCase("buy")){
 			if (!hasPerms(sender,"identify.buy"))
 				return true;
 			double bal = plugin.economy.getBalance(sender.getName());
 			if(args.length<2){
 				if(plugin.getDiabloDrops()!=null){
 					sender.sendMessage(ChatColor.GRAY+" /identify buy <dd|tier|tome|gem|name|enchant|lore|random>");
 					return true;
 				}
 				return false;				
 			}
 			if((args[1].equalsIgnoreCase("diablodrop")||args[1].equalsIgnoreCase("dd"))
 					&&plugin.getDiabloDrops()!=null){
 				if(!plugin.ddConfig.getBoolean("DiabloDrop.Random.Enabled",true)) return true;
 				double price = plugin.ddConfig.getDouble("DiabloDrop.Random.Price", 1000);
 				if(price > bal){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 					return true;
 				}else{
 					ItemStack item = plugin.getDiabloDrops().getDropAPI().getItem();
 					while(item==null)
 						item = plugin.getDiabloDrops().getDropAPI().getItem();
 					String name = null;
 					if(item.hasItemMeta()){
 						name = item.getItemMeta().getDisplayName();
 					}
 					player.getInventory().addItem(item);
 					player.updateInventory();
 					plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 					sender.sendMessage(ChatColor.DARK_AQUA + "You were charged: " + ChatColor.BLUE + String.valueOf(price));
 					sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"
 							+ChatColor.GRAY + "For: " + name);
 					return true;
 				}
 			}
 			if(args[1].equalsIgnoreCase("tier")&&plugin.getDiabloDrops()!=null){
 				for(com.modcrafting.diablodrops.tier.Tier tier:plugin.getDiabloDrops().tiers){
 					if(args[2].equalsIgnoreCase(tier.getName())&&plugin.ddConfig.getBoolean("DiabloDrop.Tier.Enabled",true)){
						double price = plugin.ddConfig.getDouble("DiabloDrop."+tier.getName()+".Price", 1000);
 						if(price > bal){
 							sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 							return true;
 						}
 						ItemStack item = plugin.getDiabloDrops().getDropAPI().getItem();
 						while(item==null)
 							item = plugin.getDiabloDrops().getDropAPI().getItem();
 						String name = null;
 						if(item.hasItemMeta()){
 							name = item.getItemMeta().getDisplayName();
 						}
 						player.getInventory().addItem(item);
 						player.updateInventory();
 						plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 						sender.sendMessage(ChatColor.DARK_AQUA + "You were charged: " + ChatColor.BLUE + String.valueOf(price));
 						sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"
 								+ChatColor.GRAY + "For: " + name);
 						return true;								
 					}						
 				}
 				sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"
 						+ChatColor.GRAY+" Tier: "+args[2]+" not found.");
 				return true;
 			}
 			if(args[1].equalsIgnoreCase("tome")&&plugin.getDiabloDrops()!=null){
 				if(!plugin.ddConfig.getBoolean("DiabloDrop.Tome.Enabled",true)) return true;
 				double price = plugin.ddConfig.getDouble("DiabloDrop.Tome.Price", 1000);
 				if(price > bal){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 					return true;
 				}
 				com.modcrafting.diablodrops.items.IdentifyTome tome = new com.modcrafting.diablodrops.items.IdentifyTome();
 				player.getInventory().addItem(tome);
 				player.updateInventory();
 				plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				sender.sendMessage(ChatColor.DARK_AQUA + "You were charged: " + ChatColor.BLUE + String.valueOf(price));
 				sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"
 						+ChatColor.GRAY + "For An Identification Tome. ");
 				return true;								
 				
 			}
 			if(args[1].equalsIgnoreCase("gem")&&plugin.getDiabloDrops()!=null){
 				if(!plugin.ddConfig.getBoolean("DiabloDrop.SocketGem.Enabled",true)) return true;
 				double price = plugin.ddConfig.getDouble("DiabloDrop.SocketGem.Price", 1000);
 				if(price > bal){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 					return true;
 				}
 	            List<String> l = plugin.getDiabloDrops().getConfig().getStringList("SocketItem.Items");
 	            com.modcrafting.diablodrops.items.Socket tome = new Socket(Material.valueOf(l.get(
 	            		plugin.getDiabloDrops().getSingleRandom().nextInt(l.size())).toUpperCase()));
 				player.getInventory().addItem(tome);
 				player.updateInventory();
 				plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				sender.sendMessage(ChatColor.DARK_AQUA + "You were charged: " + ChatColor.BLUE + String.valueOf(price));
 				sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"
 						+ChatColor.GRAY + "For An Socket Gem. ");
 				return true;								
 				
 			}
 			if(args[1].equalsIgnoreCase("name")
 					&&plugin.getConfig().getBoolean("Name.Enabled",true)
 					&&hasPerms(sender,"identify.buy.name")){
 				double price = 1000;
 				String name = combineSplit(2, args, " ");
 				name = ChatColor.translateAlternateColorCodes('&', name);
 				ItemStack item = player.getItemInHand();
 				if (item==null||item.getType() == Material.AIR){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your Not Holding Anything.");
 					return true;
 				}
 				if(plugin.getConfig().getBoolean("Name.Flat",true)){
 					price = plugin.getConfig().getDouble("Name.FlatRate", 1000);
 					if(price > bal){
 						sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 						sender.sendMessage(ChatColor.BLUE + "Name Cost: "+String.valueOf(price));
 						return true;
 					}
 					plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				}else{
 					price = plugin.getConfig().getDouble("Name.PerLetter", 10);
 					price = price*name.length();
 					if(price > bal){
 						sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 						sender.sendMessage(ChatColor.BLUE + "Name Cost: "+String.valueOf(price));
 						return true;
 					}
 					plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				}
 				ItemMeta meta = item.getItemMeta();
 				meta.setDisplayName(name);
 				item.setItemMeta(meta);
 				sender.sendMessage(ChatColor.GRAY+" Name: "+name+ChatColor.GRAY+" was set for "+ChatColor.GOLD+String.valueOf(price));
 				return true;
 			}
 			if(args[1].equalsIgnoreCase("lore")
 					&&plugin.getConfig().getBoolean("Lore.Enabled",true)
 					&&hasPerms(sender,"identify.buy.lore")){
 				if(plugin.getDiabloDrops()!=null&&!sender.hasPermission("identify.override.lore")){
 					sender.sendMessage(ChatColor.GOLD+"[DiabloDrops]"+ChatColor.GRAY+"Option disabled.");
 					return true;
 				}
 				double price = 1000;
 				
 				String lore = combineSplit(2, args, " ");
                 lore = ChatColor.translateAlternateColorCodes(
                         "&".toCharArray()[0], lore);
 				if(!plugin.getConfig().getBoolean("Lore.PerLetterLine",false)){
 					price = plugin.getConfig().getDouble("Lore.FlatRatePerLine", 1000);
 					price = lore.split(",").length*price;
 					if(price > bal){
 						sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 						sender.sendMessage(ChatColor.BLUE + "Lore Cost: "+String.valueOf(price));
 						return true;
 					}
 					plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				}else{
 					price = plugin.getConfig().getDouble("Lore.PerRate", 10);
 					price = lore.length()*price;
 					if(price > bal){
 						sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 						sender.sendMessage(ChatColor.BLUE + "Lore Cost: "+String.valueOf(price));
 						return true;
 					}
 					plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 				}
 				ItemStack item = player.getItemInHand();
 				List<String> list = new ArrayList<String>();
                 for (String s : lore.split(","))
                 {
                     if (s.length() > 0)
                         list.add(s);
                 }
                 ItemMeta m = item.getItemMeta();
                 m.setLore(list);
                 item.setItemMeta(m);
                 sender.sendMessage(ChatColor.DARK_AQUA + "You were charged: " + ChatColor.BLUE + String.valueOf(price));
 				for(String s:list){
                     if (s.length() > 0)
                         sender.sendMessage(s);
 				}
                 sender.sendMessage(ChatColor.DARK_AQUA + "Lore Set.");
 				return true;
 			}
 			if(args[1].equalsIgnoreCase("random")&&hasPerms(sender,"identify.buy.random")){
 				ItemStack item = player.getItemInHand();
 				if(item.hasItemMeta()){
 					ItemMeta meta = item.getItemMeta();
 					if(meta.getDisplayName().contains(new Character((char) 167).toString())
 							|| item.getEnchantments().size()>0){
 						sender.sendMessage(ChatColor.AQUA+"You are unable to enchant this.");
 						return true;
 					}
 				}
 				plugin.buy.buyRandom(player);
 				return true;						
 			}
 			if(args[1].equalsIgnoreCase("enchant")&&args.length>2&&hasPerms(sender,"identify.buy.enchant")){
 				ItemStack item = player.getItemInHand();
 				if(item.hasItemMeta()){
 					ItemMeta meta = item.getItemMeta();
 					if(meta.getDisplayName()!=null
 							&&meta.getDisplayName().contains(new Character((char) 167).toString())){
 						sender.sendMessage(ChatColor.AQUA+"You are unable to enchant this.");
 						return true;
 					}
 				}
 				plugin.buy.buyList(player, args); 
 				return true;
 			}
 		}
 
 		if(args[0].equalsIgnoreCase("repair")
 				&&plugin.getConfig().getBoolean("Repair.Enabled",true)
 				&&hasPerms(sender,"identify.repair")){
 			double bal = plugin.economy.getBalance(sender.getName());
 			double price = 1000;
 			ItemStack item = player.getItemInHand();
 			if (item==null||item.getType() == Material.AIR){
 				sender.sendMessage(ChatColor.DARK_AQUA + "Your Not Holding Anything.");
 				return true;
 			}
 			if(plugin.getConfig().getBoolean("Repair.Flat",true)){
 				price = plugin.getConfig().getDouble("Repair.FlatRate", 1000);
 				if(price > bal){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 					sender.sendMessage(ChatColor.BLUE + "Repair Cost: "+String.valueOf(price));
 					return true;
 				}
 				plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 			}else{
 				price = plugin.getConfig().getDouble("Repair.PriceVsDamage", 10);
 				price = price*item.getDurability();
 				if(price > bal){
 					sender.sendMessage(ChatColor.DARK_AQUA + "Your don't have enough money!");
 					sender.sendMessage(ChatColor.BLUE + "Repair Cost: "+String.valueOf(price));
 					return true;
 				}
 				plugin.economy.withdrawPlayer(sender.getName(), Math.abs(price));
 			}
 			item.setDurability((short)0);
 			sender.sendMessage(ChatColor.GRAY+" Your "+item.getType().toString()+ChatColor.GRAY+" was repaired for "+ChatColor.GOLD+String.valueOf(price));
 			return true;
 		}
 		if(args[0].equalsIgnoreCase("help")){
 			showHelp(player);
 			return true;
 		}
 		if(args[0].equalsIgnoreCase("reload")&&hasPerms(sender,"identify.reload")){
 			plugin.getServer().getPluginManager().disablePlugin(plugin);
 			plugin.getServer().getPluginManager().enablePlugin(plugin);
 			plugin.reloadConfig();
 			sender.sendMessage(ChatColor.GREEN+"Reloaded Identify.");
 			return true;
 		}
 		if(args[0].equalsIgnoreCase("clear")&&hasPerms(sender,"identify.clear")){
 			ItemStack it = player.getItemInHand();
 			Material mat = it.getType();
 			short dam = it.getDurability();
 			ItemStack its = new ItemStack(mat,1,dam);
 			player.setItemInHand(its);
 			sender.sendMessage(ChatColor.DARK_AQUA+"Item cleared");
 			return true;
 		}
 		return false;
 	}
 
 	public boolean showHelp(Player sender) {
 		sender.sendMessage(ChatColor.DARK_AQUA + "Identify Help");
 		sender.sendMessage(ChatColor.DARK_AQUA + "-----------------------------------------");
 		sender.sendMessage(ChatColor.DARK_AQUA + "/identify <Buy/List/Reload/Clear/Help>");
 		if(plugin.getDiabloDrops()!=null){
 			sender.sendMessage(ChatColor.DARK_AQUA + "/identify buy <DD/Tier/Name/Enchant/Lore/Random>");
 		}else{
 			sender.sendMessage(ChatColor.DARK_AQUA + "/identify buy <Name/Enchant/Lore/Random>");			
 		}
 		sender.sendMessage(ChatColor.DARK_AQUA + "/Identify buy enchant {ID#/Name} (level/MAX)");
 		sender.sendMessage(ChatColor.DARK_AQUA + "/Identify buy name &7AwezomeSword");
 		sender.sendMessage(ChatColor.DARK_AQUA + "/Identify buy lore &7Enter text here,&bseperated by comma");
 		sender.sendMessage(ChatColor.DARK_AQUA + "/identify list");
 		sender.sendMessage(ChatColor.DARK_AQUA + "/identify reload");
 		return true;
 	}
 
 	public void showList(Player sender){
 		ItemStack item = sender.getItemInHand();
 		String itemName = item.getType().toString();
 		sender.sendMessage(ChatColor.DARK_AQUA + "Identify Shop  {Current Item: " + itemName + " }");
 		sender.sendMessage(ChatColor.DARK_AQUA + "-----------------------------------------");
 		sender.sendMessage(ChatColor.GOLD + "For a random enchantment type /identify buy random");
 		sender.sendMessage(ChatColor.GOLD + "Use /identify buy enchant [id# or name] [lvl]");
 		for(int i=0;i<Enchantment.values().length;i++){
 			Enchantment e = Enchantment.values()[i];
 			if(e.canEnchantItem(item)){
 				sender.sendMessage(ChatColor.BLUE + "ID#"+String.valueOf(EnchantUtil.getID(e))+" "+e.getName()+" +1");
 			}
 		}
 	}
 	public boolean hasPerms(CommandSender sender,String string){
 		if (!sender.hasPermission(string)){
 			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
 			return false;
 		}
 		return true;
 	}
 	
     public String combineSplit(int startIndex, String[] string, String seperator)
     {
         StringBuilder builder = new StringBuilder();
         if (string.length >= 1)
         {
             for (int i = startIndex; i < string.length; i++)
             {
                 builder.append(string[i]);
                 builder.append(seperator);
             }
             if (builder.length() > seperator.length())
             {
                 builder.deleteCharAt(builder.length() - seperator.length()); // remove
                 return builder.toString();
             }
         }
         return "";
     }
 }
