 package me.ellbristow.ChestBank;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.minecraft.server.*;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
         
 public class ChestBank extends JavaPlugin {
 	
 	public static ChestBank plugin;
 	public static Logger logger;
 	public FileConfiguration banksConfig;
         public FileConfiguration config;
 	private File bankFile = null;
 	public HashMap<String, InventoryLargeChest> chestBanks;
         public int[] limits = {10,25,35};
         public final ChestBlockListener blockListener = new ChestBlockListener(this);
         public final ChestPlayerListener playerListener = new ChestPlayerListener(this);
         public final ChestBankInvListener invListener = new ChestBankInvListener(this);
         public HashSet<String> openInvs = new HashSet<String>();
 
 	@Override
 	public void onDisable () {
 		setChests(chestBanks);
 	}
 
 	@Override
 	public void onEnable () {
 		PluginManager pm = getServer().getPluginManager();
                 logger = getLogger();
                 config = getConfig();
                 limits[0] = 10;
                 limits[0] = config.getInt("normal_limit");
                 config.set("normal_limit", limits[0]);
                 limits[1] = 25;
                 limits[1] = config.getInt("elevated_limit");
                 config.set("elevated_limit", limits[1]);
                 limits[2] = 25;
                 limits[2] = config.getInt("vip_limit");
                 config.set("vip_limit", limits[2]);
                 saveConfig();
 		pm.registerEvents(blockListener, this);
 		pm.registerEvents(playerListener, this);
                 pm.registerEvents(invListener, this);
 		banksConfig = getChestBanks();
 		saveChestBanks();
                 bankTidy();
 		chestBanks = getChests();
 	}
 	
         @Override
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Sorry! The console can't use this command!");
 			return true;
 		}
 		Player player = (Player) sender;
 		if (args.length == 0) {
 			// Command list requested
 			PluginDescriptionFile pdfFile = this.getDescription();
 			player.sendMessage(ChatColor.GOLD + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
                         boolean found = false;
 			if (player.hasPermission("chestbank.info")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank info " + ChatColor.GRAY + ": Get targetted ChestBank's info.");
 			}
                         if (player.hasPermission("chestbank.list")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank list " + ChatColor.GRAY + ": List all existing ChestBank networks.");
 			}
                         if (player.hasPermission("chestbank.create")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank create " + ChatColor.GRAY + ": Make targetted chest a ChestBank.");
 			}
                         if (player.hasPermission("chestbank.create.networks")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank create {network}" + ChatColor.GRAY + ": Create a Chestbank on the name network.");
 			}
 			if (player.hasPermission("chestbank.remove")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank remove " + ChatColor.GRAY + ": Make targetted ChestBank a chest.");
 			}
 			if (player.hasPermission("chestbank.see")) {
                             player.sendMessage(ChatColor.GOLD + "  /chestbank see [player] " + ChatColor.GRAY + ": View player's ChestBank account.");
 			}
                         if (!found) {
                             player.sendMessage(ChatColor.GOLD + "There are no ChestBank commands you can use!");
                         }
 			return true;
 		}
 		else if (args.length == 1 || (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("remove")))) {
 			if (args[0].equalsIgnoreCase("see")) {
 				player.sendMessage(ChatColor.RED + "Please specify a player!");
 				return false;
 			}
                         // Create, Remove, List, Info
 			else if (args[0].equalsIgnoreCase("create")) {
 				if (!player.hasPermission("chestbank.create")) {
 					player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank!");
 					return true;
 				}
                                 if (args.length == 2 && !player.hasPermission("chestbank.create.networks")) {
 					player.sendMessage(ChatColor.RED + "You do not have permission to create a ChestBank on an alternate network!");
 					return true;
 				}
 				Block block = player.getTargetBlock(null, 4);
 				if (block.getTypeId() != 54) {
 					player.sendMessage(ChatColor.RED + "You're not looking at a chest!");
 					return true;
 				}
 				if (isBankBlock(block)) {
 					player.sendMessage(ChatColor.RED + "That is already a ChestBank!");
 					return true;
 				}
                                 if (args.length == 2) {
                                     // Network specified
                                     String network = args[1];
                                     String bankNames = banksConfig.getString("networks.names", "");
                                     if (bankNames.equals("")) {
                                         bankNames = args[1];
                                     } else {
                                         String[] bankNamesArray = bankNames.split(":");
                                         boolean exists = false;
                                         for (String bankName : bankNamesArray) {
                                             if (bankName.equals(args[1])) {
                                                 exists = true;
                                             }
                                         }
                                         if (!exists) {
                                             bankNames += ":" + args[1];
                                         }
                                     }
                                     banksConfig.set("networks.names", bankNames);
                                     ConfigurationSection networkBank = banksConfig.getConfigurationSection("networks." + args[1]);
                                     String locsList = "";
                                     if (networkBank != null) {
                                         locsList = networkBank.getString("locations", "");
                                         if (!locsList.equals("")) {
                                             locsList += ";";
                                         }
                                     }
                                     locsList += block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
                                     Block doubleChest = getDoubleChest(block);
                                     if (doubleChest != null) {
                                             locsList += ":" + doubleChest.getX() + ":" + doubleChest.getY() + ":" + doubleChest.getZ();
                                     }
                                     banksConfig.set("networks." + args[1] + ".locations", locsList);
                                     saveChestBanks();
                                     player.sendMessage(ChatColor.GOLD + "ChestBank created on " + ChatColor.WHITE + network + ChatColor.GOLD + " Network!");
                                     return true;
                                 }
 				String bankList = banksConfig.getString("banks", "");
 				if (bankList.equals("")) {
 					bankList += block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
 				}
 				else {
 					bankList += ";" + block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
 				}
 				Block doubleChest = getDoubleChest(block);
 				if (doubleChest != null) {
 					bankList += ":" + doubleChest.getX() + ":" + doubleChest.getY() + ":" + doubleChest.getZ();
 				}
 				banksConfig.set("banks", bankList);
 				saveChestBanks();
 				player.sendMessage(ChatColor.GOLD + "ChestBank created!");
 				return true;
 			}
 			else if (args[0].equalsIgnoreCase("remove")) {
 				if (!player.hasPermission("chestbank.remove")) {
 					player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank!");
 					return true;
 				}
 				Block block = player.getTargetBlock(null, 4);
                                 if (isNetworkBank(block) && !player.hasPermission("chestbank.remove.networks")) {
 					player.sendMessage(ChatColor.RED + "You do not have permission to remove a ChestBank on an alternate network!");
 					return true;
 				}
 				if (!isBankBlock(block)) {
 					player.sendMessage(ChatColor.RED + "You're not looking at a ChestBank!");
 					return true;
 				}
                                 if (isNetworkBank(block)) {
                                     String networkNames = banksConfig.getString("networks.names");
                                     String[] networkNamesArray = networkNames.split(":");
                                     String networkName = getNetwork(block);
                                     String networkLocs = banksConfig.getString("networks." + networkName + ".locations", "");
                                     String newNetworkLocs = "";
                                     for (String location : networkLocs.split(";")) {
                                         String[] loc = location.split(":");
                                         if (loc.length == 4) {
                                             String bankWorld = loc[0];
                                             int bankX = Integer.parseInt(loc[1]);
                                             int bankY = Integer.parseInt(loc[2]);
                                             int bankZ = Integer.parseInt(loc[3]);
                                             if (!bankWorld.equals(block.getWorld().getName()) || bankX != block.getX() || bankY != block.getY() || bankZ != block.getZ()) {
                                                 if (!newNetworkLocs.equals("")) {
                                                     newNetworkLocs += ";";
                                                 }
                                                 newNetworkLocs += bankWorld + ":" + bankX + ":" + bankY + ":" + bankZ;
                                             }
                                         } else if (loc.length == 7) {
                                             String bankWorld = loc[0];
                                             int bankX = Integer.parseInt(loc[1]);
                                             int bankY = Integer.parseInt(loc[2]);
                                             int bankZ = Integer.parseInt(loc[3]);
                                             int bankA = Integer.parseInt(loc[4]);
                                             int bankB = Integer.parseInt(loc[5]);
                                             int bankC = Integer.parseInt(loc[6]);
                                             if (!bankWorld.equals(block.getWorld().getName()) || (!(bankX != block.getX() && bankY != block.getY() && bankZ != block.getZ()) && !(bankA != block.getX() && bankB != block.getY() && bankC != block.getZ()) )) {
                                                 if (!newNetworkLocs.equals("")) {
                                                     newNetworkLocs += ";";
                                                 }
                                                 newNetworkLocs += bankWorld + ":" + bankX + ":" + bankY + ":" + bankZ + ":" + bankA + ":" + bankB + ":" + bankC;
                                             }
                                         }
                                     }
                                     banksConfig.set("networks." + networkName + ".locations", newNetworkLocs);
                                     saveChestBanks();
                                     player.sendMessage(ChatColor.GOLD + "ChestBank removed from " + ChatColor.WHITE + networkName + " Network!");
                                     return true;
                                 }
 				String bankList = banksConfig.getString("banks");
 				String[] bankSplit = bankList.split(";");
 				if (bankSplit.length == 0 || bankSplit.length == 1) {
 					banksConfig.set("banks", "");
 				}
 				else {
 					String newBankList = "";
 					for (String chestBank : bankSplit) {
                                                 String[] bankLoc = {null,null,null,null,null,null,null};
 						String[] bankLocA = chestBank.split(":");
 						if (bankLocA.length <= 4) {
                                                     if (bankLocA.length == 3) {
                                                         bankLoc[3] = bankLocA[2];
                                                         bankLoc[2] = bankLocA[1];
                                                         bankLoc[1] = bankLocA[0];
                                                         bankLoc[0] = block.getWorld().getName();
                                                     } else {
                                                         bankLoc[3] = bankLocA[3];
                                                         bankLoc[2] = bankLocA[2];
                                                         bankLoc[1] = bankLocA[1];
                                                         bankLoc[0] = bankLocA[0];
                                                     }
                                                     String blockWorld = bankLoc[0];
                                                     int blockX = Integer.parseInt(bankLoc[1]);
                                                     int blockY = Integer.parseInt(bankLoc[2]);
                                                     int blockZ = Integer.parseInt(bankLoc[3]);
                                                     if (!blockWorld.equals(block.getWorld().getName()) || blockX != block.getX() || blockY != block.getY() || blockZ != block.getZ()) {
                                                             if (!newBankList.equals("")) {
                                                                     newBankList += ";";
                                                             }
                                                             newBankList += blockWorld + ":" + blockX + ":" + blockY + ":" + blockZ;
                                                     }
 						}
 						else {
                                                         if (bankLocA.length == 6) {
                                                             bankLoc[6] = bankLocA[5];
                                                             bankLoc[5] = bankLocA[4];
                                                             bankLoc[4] = bankLocA[3];
                                                             bankLoc[3] = bankLocA[2];
                                                             bankLoc[2] = bankLocA[1];
                                                             bankLoc[1] = bankLocA[0];
                                                             bankLoc[0] = player.getWorld().getName();
                                                         } else {
                                                             bankLoc[6] = bankLocA[6];
                                                             bankLoc[5] = bankLocA[5];
                                                             bankLoc[4] = bankLocA[4];
                                                             bankLoc[3] = bankLocA[3];
                                                             bankLoc[2] = bankLocA[2];
                                                             bankLoc[1] = bankLocA[1];
                                                             bankLoc[0] = bankLocA[0];
                                                         }
                                                         String blockWorld = bankLoc[0];
 							int blockX = Integer.parseInt(bankLoc[1]);
 							int blockY = Integer.parseInt(bankLoc[2]);
 							int blockZ = Integer.parseInt(bankLoc[3]);
 							int blockA = Integer.parseInt(bankLoc[4]);
 							int blockB = Integer.parseInt(bankLoc[5]);
 							int blockC = Integer.parseInt(bankLoc[6]);
 							if (!(blockX == block.getX() && blockY == block.getY() && blockZ == block.getZ()) && !(blockA == block.getX() && blockB == block.getY() && blockC == block.getZ())) {
 								if (!newBankList.equals("")) {
 									newBankList += ";";
 								}
 								newBankList += blockWorld + ":" + blockX + ":" + blockY + ":" + blockZ + ":" + blockA + ":" + blockB + ":" + blockC;
 							}
 						}
 					}
 					banksConfig.set("banks", newBankList);
 				}
 				saveChestBanks();
 				player.sendMessage(ChatColor.GOLD + "ChestBank removed!");
 				return true;
 			} else if (args[0].equalsIgnoreCase("info")) {
                             if (!player.hasPermission("chestbank.info")) {
                                 player.sendMessage(ChatColor.RED + "You do not have permission to get ChestBank info!");
                                 return true;
                             }
                             Block block = player.getTargetBlock(null, 4);
                             if (!isBankBlock(block)) {
                                 player.sendMessage(ChatColor.RED + "This block is not a ChestBank!");
                                 return true;
                             } else {
                                 if (!isNetworkBank(block)) {
                                     player.sendMessage(ChatColor.GOLD + "This ChestBank is on the main network!");
                                     return true;
                                 } else {
                                     String network = getNetwork(block);
                                     player.sendMessage(ChatColor.GOLD + "This ChestBank is on the " + ChatColor.WHITE + network + ChatColor.GOLD + "network!");
                                     return true;
                                 }
                             }
                         } else if (args[0].equalsIgnoreCase("list")) {
                             String bankLocs = banksConfig.getString("banks", "");
                             player.sendMessage(ChatColor.GOLD + "ChestBank Networks:");
                             int banks = 0;
                             if (!bankLocs.equals("")) {
                                 banks = bankLocs.split(";").length;
                             }
                             player.sendMessage(ChatColor.GOLD + "  Main Network: " + ChatColor.WHITE + banks + " Location(s)");
                             String networkNames = banksConfig.getString("networks.names", "");
                             if (!networkNames.equals("")) {
                                 String[] networks = networkNames.split(":");
                                 for (String network : networks) {
                                     bankLocs = banksConfig.getString("networks." + network + ".locations", "");
                                     banks = 0;
                                     if (!bankLocs.equals("")) {
                                         banks = bankLocs.split(";").length;
                                     }
                                     player.sendMessage(ChatColor.GOLD + "  " + network + " Network: " + ChatColor.WHITE + banks + " Location(s)");
                                 }
                             }
                             return true;
                         }
 		}
 		else if (args.length == 2) {
 			if (!args[0].equalsIgnoreCase("see")) {
 				return false;
 			}
 			if (!player.hasPermission("chestbank.see")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to access other players' accounts!");
 				return true;
 			}
 			Block block = player.getTargetBlock(null, 4);
                         if (isNetworkBank(block) && !player.hasPermission("chestbank.see.networks")) {
 				player.sendMessage(ChatColor.RED + "You do not have permission to access other players' " + ChatColor.WHITE + getNetwork(block) + ChatColor.GOLD + " accounts!");
 				return true;
 			}
 			if (!isBankBlock(block)) {
 				player.sendMessage(ChatColor.RED + "You're not looking at a ChestBank!");
 				return true;
 			}
                         OfflinePlayer target = getServer().getOfflinePlayer(args[1]);
                         String account = "";
                         if (isNetworkBank(block)) {
                             account = getNetwork(block) + ">>" + target.getName();
                         } else {
                             account = target.getName();
                         }
 			if (chestBanks.containsKey(account)) {
 				EntityPlayer ePlayer;
 				ePlayer = ((CraftPlayer) player).getHandle();
 				InventoryLargeChest lc = chestBanks.get(account);
 				ePlayer.a(lc);
 			}
 			else {
 				player.sendMessage(ChatColor.RED + target.getName() + " does not have a ChestBank account here!");
 			}
                         return true;
 		}
 		return false;
 	}
 	
         public void bankTidy() {
             String banks = banksConfig.getString("banks", "");
             if (!banks.equals("")) {
                 String newBankList = "";
                 int dropped = 0;
                 for (String bank : banks.split(";")) {
                     if (!newBankList.equals("")) {
                         newBankList += ";";
                     }
                     String[] loc = bank.split(":");
                     boolean hasWorld = false;
                     try {
                         Integer.parseInt(loc[0]);
                     } catch (NumberFormatException nfe) {
                         hasWorld = true;
                     }
                     int bankX = 0;
                     int bankY = 0;
                     int bankZ = 0;
                     if (!hasWorld) {
                         bankX = Integer.parseInt(loc[0]);
                         bankY = Integer.parseInt(loc[1]);
                         bankZ = Integer.parseInt(loc[2]);
                        if (getServer().getWorlds().get(0).getBlockAt(bankX, bankY, bankZ).getTypeId() == 54) {
                            newBankList += bankX + ":" + bankY + ":" + bankZ;
                            if (loc.length > 3) {
                                newBankList += loc[3] + ":" + loc[4] + ":" + loc[5];
                            }
                        } else {
                            dropped++;
                        }
                     } else {
                         bankX = Integer.parseInt(loc[1]);
                         bankY = Integer.parseInt(loc[2]);
                         bankZ = Integer.parseInt(loc[3]);
                        if (getServer().getWorld(loc[0]).getBlockAt(bankX, bankY, bankZ).getTypeId() == 54) {
                            newBankList += bankX + ":" + bankY + ":" + bankZ;
                            if (loc.length > 4) {
                                newBankList += loc[3] + ":" + loc[4] + ":" + loc[5];
                            }
                        } else {
                            dropped++;
                        }
                     }
                    
                 }
                 if (!newBankList.equals("")) {
                     banksConfig.set("bank", newBankList);
                 }
                 if (dropped != 0) {
                     logger.info(dropped + " orphaned ChestBanks removed!");
                     saveChestBanks();
                 }
             }
         }
         
 	public boolean isBankBlock (Block block) {
 		// Check if the block is a ChestBank
 		String bankList = banksConfig.getString("banks", "");
 		if (!bankList.equals("")) {
 			String[] bankSplit = bankList.split(";");
 			for (String bank : bankSplit) {
                             if (!bank.isEmpty() && !bank.equals("")) {
                                 String[] bankCoords = {null,null,null,null,null,null,null};
 				String[] bankCoordsA = bank.split(":");
                                 boolean hasWorld = false;
                                 try {
                                     Integer.parseInt(bankCoordsA[0]);
                                 } catch (NumberFormatException nfe) {
                                     hasWorld = true;
                                 } finally {
                                     if (!hasWorld) {
                                         if (bankCoordsA.length == 6) {
                                             bankCoords[6] = bankCoordsA[5];
                                             bankCoords[5] = bankCoordsA[4];
                                             bankCoords[4] = bankCoordsA[3];
                                         }
                                         bankCoords[3] = bankCoordsA[2];
                                         bankCoords[2] = bankCoordsA[1];
                                         bankCoords[1] = bankCoordsA[0];
                                         bankCoords[0] = block.getWorld().getName();
                                     } else {
                                         bankCoords = bankCoordsA;
                                     }
                                 }
                                 String blockWorld = bankCoords[0];
 				int blockX = Integer.parseInt(bankCoords[1]);
 				int blockY = Integer.parseInt(bankCoords[2]);
 				int blockZ = Integer.parseInt(bankCoords[3]);
 				if (block.getWorld().getName().equals(blockWorld) && block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
 					return true;
 				}
 				if (bankCoordsA.length > 4) {
 					blockX = Integer.parseInt(bankCoords[4]);
 					blockY = Integer.parseInt(bankCoords[5]);
 					blockZ = Integer.parseInt(bankCoords[6]);
 					if (block.getX() == blockX && block.getY() == blockY && block.getZ() == blockZ) {
 						return true;
 					}
 				}
                             }
 			}
 		}
                 if (isNetworkBank(block)) {
                     return true;
                 }
 		return false;
 	}
         
         public boolean isNetworkBank(Block block) {
             ConfigurationSection bankList = banksConfig.getConfigurationSection("networks");
             if (bankList != null) {
                 String bankNames = bankList.getString("names", "");
                 if (!bankNames.equals("")) {
                     String[] bankNamesArray = bankNames.split(":");
                     for (String bankName : bankNamesArray) {
                         String bankLocs = bankList.getString(bankName + ".locations", "");
                         if (!bankLocs.equals("")) {
                             String[] bankLocations = bankLocs.split(";");
                             for (String bankLoc : bankLocations) {
                                 String[] bankCoords = bankLoc.split(":");
                                 String bankWorld = bankCoords[0];
                                 int bankX = Integer.parseInt(bankCoords[1]);
                                 int bankY = Integer.parseInt(bankCoords[2]);
                                 int bankZ = Integer.parseInt(bankCoords[3]);
                                 if (block.getWorld().getName().equals(bankWorld) && block.getX()== bankX && block.getY() == bankY && block.getZ() == bankZ) {
                                     return true;
                                 }
                                 if (bankCoords.length == 7) {
                                     bankX = Integer.parseInt(bankCoords[4]);
                                     bankY = Integer.parseInt(bankCoords[5]);
                                     bankZ = Integer.parseInt(bankCoords[6]);
                                     if (block.getWorld().getName().equals(bankWorld) && block.getX()== bankX && block.getY() == bankY && block.getZ() == bankZ) {
                                         return true;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             return false;
         }
         
         public String getNetwork(Block block) {
             String network = "";
             String networkNames = banksConfig.getString("networks.names");
             String[] networkNamesArray = networkNames.split(":");
             for (String networkName : networkNamesArray) {
                 String networkLocs = banksConfig.getString("networks." + networkName + ".locations", "");
                 for (String location : networkLocs.split(";")) {
                     String[] loc = location.split(":");
                     if (loc.length == 4) {
                         String bankWorld = loc[0];
                         int bankX = Integer.parseInt(loc[1]);
                         int bankY = Integer.parseInt(loc[2]);
                         int bankZ = Integer.parseInt(loc[3]);
                         if (bankWorld.equals(block.getWorld().getName()) && bankX == block.getX() && bankY == block.getY() && bankZ == block.getZ()) {
                             network = networkName;
                         }
                     } else if (loc.length == 7) {
                         String bankWorld = loc[0];
                         int bankX = Integer.parseInt(loc[1]);
                         int bankY = Integer.parseInt(loc[2]);
                         int bankZ = Integer.parseInt(loc[3]);
                         int bankA = Integer.parseInt(loc[4]);
                         int bankB = Integer.parseInt(loc[5]);
                         int bankC = Integer.parseInt(loc[6]);
                         if (bankWorld.equals(block.getWorld().getName()) && ((bankX == block.getX() && bankY == block.getY() && bankZ == block.getZ()) || (bankA == block.getX() && bankB == block.getY() && bankC == block.getZ()))) {
                             network = networkName;
                         }
                     }
                 }
             }
             return network;
         }
 	
 	public Block getDoubleChest(Block block) {
 		int blockX = block.getX();
 		int blockY = block.getY();
 		int blockZ = block.getZ();
 		if (block.getWorld().getBlockAt(blockX + 1, blockY, blockZ).getTypeId() == 54) {
 			return block.getWorld().getBlockAt(blockX + 1, blockY, blockZ);
 		}
 		if (block.getWorld().getBlockAt(blockX - 1, blockY, blockZ).getTypeId() == 54) {
 			return block.getWorld().getBlockAt(blockX - 1, blockY, blockZ);
 		}
 		if (block.getWorld().getBlockAt(blockX , blockY, blockZ + 1).getTypeId() == 54) {
 			return block.getWorld().getBlockAt(blockX, blockY, blockZ + 1);
 		}
 		if (block.getWorld().getBlockAt(blockX , blockY, blockZ - 1).getTypeId() == 54) {
 			return block.getWorld().getBlockAt(blockX, blockY, blockZ - 1);
 		}
 		return null;
 	}
 	
 	public HashMap<String, InventoryLargeChest> getChests() {
 		HashMap<String, InventoryLargeChest> chests = new HashMap<String, InventoryLargeChest>();
 		ConfigurationSection chestSection = banksConfig.getConfigurationSection("accounts");
 		if (chestSection != null) {
 			Set<String> fileChests = chestSection.getKeys(false);
 			if (fileChests != null) {
 				for (String playerName : fileChests) {
                                         String account = "";
                                         if (playerName.contains(">>")) {
                                             account = playerName.split(">>")[1];
                                         } else {
                                             account = playerName;
                                         }
 					InventoryLargeChest returnInv = new InventoryLargeChest(account, new TileEntityChest(), new TileEntityChest());
 					String[] chestInv = banksConfig.getString("accounts." + playerName).split(";");
 					int i = 0;
 					for (String items : chestInv) {
 						String[] item = items.split(":");
 						int i0 = Integer.parseInt(item[0]);
 						int i1 = Integer.parseInt(item[1]);
 						short i2 = Short.parseShort(item[2]);
 						if(i0 != 0) {
 							ItemStack stack = new ItemStack(i0, i1, i2);
 							if (item.length == 4) {
 								String[] enchArray = item[3].split(",");
 								for (String ench : enchArray) {
 									String[] bits = ench.split("~");
 									int enchId = Integer.parseInt(bits[0]);
 									int enchLvl = Integer.parseInt(bits[1]);
 									stack.addEnchantment(Enchantment.byId[enchId], enchLvl);
 								}
 							}
 							returnInv.setItem(i, stack);
 						}
 						i++;
 					}
 					chests.put(playerName, returnInv);
 				}
 			}
 		}
 		return chests;
 	}
 	
 	public void setChests(HashMap<String, InventoryLargeChest> chests) {
 		Set<String> chestKeys = chests.keySet();
 		for (String key : chestKeys) {
 			InventoryLargeChest chest = chests.get(key);
 			String chestInv = "";
 			for (ItemStack item : chest.getContents()) {
 				chestInv += ";";
 				if (item != null) {
 					int itemID = item.id;
 					int itemCount = item.count;
 					int itemDamage = item.getData();
 					chestInv += itemID + ":" + itemCount + ":" + itemDamage;
 					if (item.hasEnchantments()) {
 						NBTTagList itemEnch = item.getEnchantments();
 						chestInv += ":";
 						String enchList = "";
 						for (int i = 0; i < itemEnch.size(); i++) {
 							NBTTagCompound ench = (NBTTagCompound) itemEnch.get(i);
 							short enchId = ench.getShort("id");
 							short enchLvl = ench.getShort("lvl");
 							enchList += "," + enchId + "~" + enchLvl;
 						}
 						chestInv += enchList.replaceFirst(",", "");
 					}
 				}
 				else {
 					chestInv += "0:0:0";
 				}
 			}
 			banksConfig.set("accounts." + key, chestInv.replaceFirst(";", ""));
 		}
 		saveChestBanks();
 	}
 	
 	public void loadChestBanks() {
 		if (bankFile == null) {
 			bankFile = new File(getDataFolder(),"chests.yml");
 		}
 		banksConfig = YamlConfiguration.loadConfiguration(bankFile);
 	}
 	
 	public FileConfiguration getChestBanks() {
 		if (banksConfig == null) {
 			loadChestBanks();
 		}
 		return banksConfig;
 	}
 	
 	public void saveChestBanks() {
 		if (banksConfig == null || bankFile == null) {
 			return;
 		}
 		try {
 			banksConfig.save(bankFile);
 		} catch (IOException ex) {
 			logger.log(Level.SEVERE, "Could not save " + bankFile, ex );
 		}
 	}
 }
