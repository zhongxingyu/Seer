 package ramirez57.CLIShop;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	Server mc;
 	PluginManager pluginmgr;
 	Logger log;
 	Economy economy;
 	Player player;
 	String string;
 	File salesFile;
     FileConfiguration sales;
     File configFile;
     FileConfiguration config;
     Material material;
     ItemStack itemstack;
     List<String> stringlist;
 	
 	public void onEnable() {
 		mc = this.getServer();
 		log = this.getLogger();
 		pluginmgr = this.getServer().getPluginManager();
 		if(!setupEcon()) {
 			log.info("Problem depending vault. Disabling...");
 			pluginmgr.disablePlugin(this);
 			return;
 		}
 		salesFile = new File(this.getDataFolder(), "SALES");
 		sales = YamlConfiguration.loadConfiguration(salesFile);
 		configFile = new File(this.getDataFolder(), "config.yml");
 		config = this.getConfig();
 		config.options().copyDefaults(true);
 		savesales();
 		saveconfig();
 	}
 	
 	public void onDisable() {
 	}
 	
 	public void reload() {
 		config = YamlConfiguration.loadConfiguration(configFile);
 		sales = YamlConfiguration.loadConfiguration(salesFile);
 		return;
 	}
 	
 	public void savesales() {
 		try {
 			sales.save(salesFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return;
 	}
 	
 	public void saveconfig() {
 		try {
 			config.save(configFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return;
 	}
 	
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 	    if(cmd.getName().equalsIgnoreCase("cshop")){
 	    	if(isplayer(sender)) {
 	    		player = (Player)sender;
 	    		if(args.length > 0) {
 	    			if(args[0].equalsIgnoreCase("help")) {
 	    				sender.sendMessage(ChatColor.GOLD + "/cshop help" + ChatColor.GRAY + " - Display help information");
 	    				sender.sendMessage(ChatColor.GOLD + "/cshop sell " + ChatColor.GRAY + "- Sell amount of item for cost per one");
 	    				sender.sendMessage(ChatColor.GOLD + "/cshop sell hand " + ChatColor.GRAY + "- Sell amount of what is in your hand for cost per one");
 	    				sender.sendMessage(ChatColor.GOLD + "/cshop buy " + ChatColor.GRAY + "- Buy the amount of item from player");
 	    				sender.sendMessage(ChatColor.GOLD + "/cshop list <page> <item> " + ChatColor.GRAY + "- Show the CLIShop market");
 	    				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Type a command for further help on syntax.");
 	    				return true;
 	    			}
 	    			if(args[0].equalsIgnoreCase("sell")) {
 	    				if(args.length > 3) {
 	    					try {Integer.parseInt(args[2]);} catch(NumberFormatException e) {player.sendMessage("/cshop sell [item] [amount] [cost_per_one]");return true;}
 	    					try {Double.parseDouble(args[3]);} catch(NumberFormatException e) {player.sendMessage("/cshop sell [item] [amount] [cost_per_one]");return true;}
 	    					if(Double.parseDouble(args[3]) > config.getDouble("price_limit")) {
 	    						player.sendMessage(ChatColor.RED + "You may not set a price that high.");
 	    						player.sendMessage(ChatColor.RED + "Price limit is " + String.valueOf(config.getDouble("price_limit")));
 	    						return true;
 	    					}
 	    					if(Double.parseDouble(args[3]) < 0) {
 	    						player.sendMessage(ChatColor.RED + "You must set a price 0 or higher.");
 	    						return true;
 	    					}
 	    					if(!args[1].equalsIgnoreCase("hand")) {
 		    					if(Material.matchMaterial(args[1]) == null) {
 		    						sender.sendMessage("What is " + args[1] + "?");
 		    						return true;
 		    					}
 	    					} else {
 	    						args[1] = player.getItemInHand().getType().toString();
 	    					}
 		    				material = Material.matchMaterial(args[1]);
 	    					if(Integer.parseInt(args[2]) <= 0 || material == Material.AIR) {
 	    						player.sendMessage(ChatColor.RED + "Stop trying to sell nothing.");
 	    						return true;
 	    					}
 	    					if(!player.getInventory().contains(material, Integer.parseInt(args[2]))) {
 		    					sender.sendMessage("You do not have at least " + args[2] + " of " + args[1]);
 		    					return true;
 		    				} else {
 		    					int eax,ebx;
 		    					eax = Integer.parseInt(args[2]);
 		    					while(eax > 0) {
 		    						ebx = player.getInventory().first(material);
 		    						itemstack = player.getInventory().getItem(ebx);
 		    						if(itemstack.getDurability() > 0 && !config.getBoolean("sell_damaged_items")) {
 	    								player.sendMessage(ChatColor.RED + "You cannot sell damaged items.");
 	    								return true;
 	    							}
 		    						if(itemstack.getAmount() <= eax) {
 		    							eax -= itemstack.getAmount();
 		    							itemstack.setAmount(0);
 		    							player.getInventory().setItem(ebx, itemstack);
 		    						} else {
 		    							itemstack.setAmount(itemstack.getAmount()-eax);
 		    							player.getInventory().setItem(ebx, itemstack);
 		    							eax = 0;
 		    						}
 		    					}
 		    					string = material.toString() +"."+player.getName().toUpperCase() + ".";
 		    					sales.set(string + "amount", sales.getInt(string + "amount", 0) + Integer.parseInt(args[2]));
 		    					sales.set(string + "durability", itemstack.getDurability());
 		    					sales.set(string + "cost", Double.parseDouble(args[3]));
 		    					stringlist = sales.getStringList(material.toString() + ".sellers");if(!stringlist.contains(player.getName().toUpperCase())) stringlist.add(player.getName().toUpperCase());
 		    					sales.set(material.toString() + ".sellers", stringlist);
 		    					savesales();
 		    					player.sendMessage(ChatColor.GREEN + "Put item (" + args[1] + " x " + args[2] + ") on the market for " + args[3] + " " + economy.currencyNamePlural() + " per one.");
 		    					mc.broadcastMessage(ChatColor.BLUE + player.getName() + ChatColor.GREEN +" is now selling " + ChatColor.YELLOW + args[2] + " " + Material.matchMaterial(args[1]).toString() + ChatColor.GREEN + " for " + ChatColor.RED + args[3] + " " + economy.currencyNamePlural() + " per one.");
 		    					return true;
 		    				}
 	    				} else {
 	    					player.sendMessage("/cshop sell [item] [amount] [cost_per_one]");
 	    					return true;
 	    				}
 	    			}
 	    			if(args[0].equalsIgnoreCase("buy")) {
 	    				if(args.length > 3) {
 	    					try{Integer.parseInt(args[3]);} catch(NumberFormatException e) {player.sendMessage("/cshop buy [player] [item] [amount]");return true;}
 	    					material = Material.matchMaterial(args[2]);
 	    					if(material == null) {
 	    						player.sendMessage("What is " + args[2] + "?");
 	    						return true;
 	    					}
 	    					String szsale = material.toString() + "." + args[1].toUpperCase() + ".";
 	    					int eax = Integer.parseInt(args[3]);
 	    					if(sales.getInt(szsale + "amount",0) > 0) {
 	    						if(sales.getInt(szsale + "amount",0) >= eax) {
 	    							double cost = sales.getDouble(szsale + "cost") * Integer.parseInt(args[3]);
 	    							String name = player.getName();
 	    							if(economy.getBalance(name) >= cost) {
 	    								economy.withdrawPlayer(name, cost);
 	    								economy.depositPlayer(args[1], cost);
	    								if(mc.getPlayer(args[1]) != null) {
	    									if(mc.getPlayer(args[1]).isOnline()) {
	    										mc.getPlayer(args[1]).sendMessage(ChatColor.GREEN + name + " has bought " + args[3] + " of your " + args[2] + "(" + cost + " " + economy.currencyNamePlural() + ")");
	    									}
 	    								}
 	    							} else if(name.equalsIgnoreCase(args[1])) {
 	    								player.sendMessage(ChatColor.GREEN + "Taking item off the market...");
 	    							} else {
 	    								player.sendMessage(ChatColor.RED + "You do not have enough " + economy.currencyNamePlural());
 	    								player.sendMessage(ChatColor.RED + String.valueOf(cost) + " " + economy.currencyNamePlural() + " is required.");
 	    								return true;
 	    							}
 	    							itemstack = new ItemStack(material,Integer.parseInt(args[3]),Short.parseShort(sales.getString(szsale + "durability","0")));
 	    							Iterator<ItemStack> i_stacks = player.getInventory().addItem(itemstack).values().iterator();
 	    							while(i_stacks.hasNext()) {
 	    								player.getWorld().dropItem(player.getLocation(), i_stacks.next()); //drop any items that couldn't fit
 	    								player.sendMessage(ChatColor.YELLOW + "Some items could not fit. They were dropped at your location.");
 	    							}
 	    							if(sales.getInt(szsale + "amount",0)-Integer.parseInt(args[3]) <= 0) {
 	    								sales.set(szsale + "amount", null);
 	    								sales.set(szsale + "durability", null); //sold out
 	    								sales.set(szsale + "cost", null);
 	    								sales.set(material.toString() + "." + args[1].toUpperCase(), null);
 	    								stringlist = sales.getStringList(material.toString() + ".sellers");stringlist.remove(args[1].toUpperCase());
 	    								sales.set(material.toString() + ".sellers", stringlist);
 	    							} else {
 	    								sales.set(szsale + "amount", sales.getInt(szsale + "amount",0)-Integer.parseInt(args[3]));
 	    							}
 	    							savesales();
 	    							player.sendMessage(ChatColor.GREEN + "Received item.");
 	    							return true;
 	    						} else {
 	    							player.sendMessage(ChatColor.RED + "Player " + args[1] + " is not selling that many " + args[2]);
 	    							player.sendMessage(ChatColor.RED + args[1] + " is selling " + String.valueOf(sales.getInt(string + "amount",0)) + " " + args[2]);
 	    							return true;
 	    						}
 	    					} else {
 	    						player.sendMessage(ChatColor.RED + "Player " + args[1] + " is not selling any " + args[2]);
 	    						return true;
 	    					}
 	    				} else {
 	    					player.sendMessage("/cshop buy [player] [item] [amount]");
 	    					return true;
 	    				}
 	    			}
 	    			if(args[0].equalsIgnoreCase("list")) {
 	    				int page = 1;
 	    				String filter;
 	    				if(args.length >= 2) {
 	    					try{page=Integer.valueOf(args[1]);} catch(NumberFormatException e) {player.sendMessage(ChatColor.RED + "That page number is invalid.");return true;}
 	    				}
 	    				if(args.length >= 3) filter = Material.matchMaterial(args[2]).toString(); else filter = "NONE";
 	    				int lineend = 11 * page;
 	    				int linestart = lineend-10;
 	    				int lines = 1;
 	    				player.sendMessage(ChatColor.RED + "|-----Page " + String.valueOf(page) + "-----|");
 	    				if(config.getBoolean("shorthand")) { //The check is done first so it can process faster later.
 		    				for(Material mat : Material.values()) {
 		    					if(mat.toString().equals(filter) || filter.equals("NONE")) {
 			    					List<String> sellers = sales.getStringList(mat.toString() + "." + "sellers");
 			    					for(Object seller : sellers.toArray()) {
 			    						if(lines >= linestart && lines <= lineend) {
			    							player.sendMessage(ChatColor.BLUE + String.valueOf(seller) + ChatColor.RED + ": " + ChatColor.YELLOW + String.valueOf(sales.getInt(mat.toString() + "." + seller + ".amount")) + " " + mat.toString() + ChatColor.RED + " for " + ChatColor.GREEN + String.valueOf(sales.getDouble(mat.toString() + "." + seller + ".cost")) + " " + economy.currencyNamePlural() + " each.");
 			    						}
 			    						lines++;
 			    					}
 		    					}
 		    				}
 	    				} else {
 	    					for(Material mat : Material.values()) {
 		    					if(mat.toString().equals(filter) || filter.equals("NONE")) {
 			    					List<String> sellers = sales.getStringList(mat.toString() + "." + "sellers");
 			    					for(Object seller : sellers.toArray()) {
 			    						if(lines >= linestart && lines <= lineend) {
			    								player.sendMessage(ChatColor.BLUE + String.valueOf(seller) + ChatColor.RED + " is selling " + ChatColor.YELLOW + String.valueOf(sales.getInt(mat.toString() + "." + seller + ".amount")) + " " + mat.toString() + ChatColor.RED + " for " + ChatColor.GREEN + String.valueOf(sales.getDouble(mat.toString() + "." + seller + ".cost")) + " " + economy.currencyNamePlural() + " per one.");
 			    						}
 			    						lines++;
 			    					}
 		    					}
 		    				}
 	    				}
 	    				player.sendMessage(ChatColor.RED + "Type " + ChatColor.YELLOW + "/cshop list " + String.valueOf(page+1) + ChatColor.RED +  " to see the next page");
 	    				return true;
 	    			}
 	    		} else {
 	    			return false;
 	    		}
 	    	} else {
 	    		sender.sendMessage("Only players can use this command.");
 	    		return true;
 	    	}
 	    }
 	    if(cmd.getName().equalsIgnoreCase("cshopadmin")) {
 	    	if(args.length > 0) {
 	    		if(args[0].equalsIgnoreCase("reload")) {
 	    			reload();
 	    			sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
 	    			return true;
 	    		}
 	    	}
 	    }
     	return false; 
     }
     
     public boolean isplayer(CommandSender _sender) {
     	return _sender instanceof Player;
     }
 	
 	public boolean setupEcon() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
 
         return (economy != null);
 	}
 	
 	public boolean sell(String playername, ItemStack item, double cost) {
 		return true;
 	}
 }
