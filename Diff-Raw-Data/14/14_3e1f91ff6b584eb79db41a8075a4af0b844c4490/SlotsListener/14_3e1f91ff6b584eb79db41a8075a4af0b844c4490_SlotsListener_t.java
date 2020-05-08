 package me.sirtyler.VaultSlots;
 
 import java.util.Random;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 @SuppressWarnings("deprecation")
 public class SlotsListener implements Listener{
 	public static VaultSlots plugin;
 	private FileConfiguration config; 
 	public Permission perm = null;
     public Economy eco = null;
 	Random rand = new Random();
 	private boolean useDebug = false;
 	private static Log log;
 	
 	public SlotsListener(VaultSlots instance) {
 		plugin = instance;
 		log = plugin.log;
 	}
 	@EventHandler
 	public void signChange(SignChangeEvent event) {
 		perm = plugin.permission;
 		Player player = event.getPlayer();
 		if(useDebug) log.sendDebugInfo("Sign Maker:" + player.getName() + " Checking Permission");
 		if(perm.playerHas(player, "vaultslots.build")) {
 			if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 			if(event.getLine(0).equalsIgnoreCase("[Slots]")) {
 				if(!event.getLine(1).isEmpty()){
 					String slotName = event.getLine(1);
 					event.setLine(0, ChatColor.GREEN + "[Slots]");
 					event.setLine(1, ChatColor.GREEN + slotName);
 					event.setLine(2, ChatColor.BLUE + "-|-|-");
 					if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Created Slot Machine Type:" + slotName);
 					player.sendMessage(ChatColor.GOLD + "Slot Machine Made.");
 				} else {
 					if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Did not Create Slot Machine: Missing Type");
 					player.sendMessage(ChatColor.GREEN + "[VaultSlots]: Missing Slot Machine Type.");
 					player.sendMessage(ChatColor.GREEN + "[VaultSlots]: Use /slots help to see Commands and Sign Format.");
 				}
 			}
 		} else {
 			if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 			player.sendMessage(ChatColor.RED + "You Do Not Have Permission for That");
 		}
 	}
 	
 	@EventHandler
     public void playerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         useDebug = plugin.inDebug;
 		config = plugin.getConfig();
 		Action action = event.getAction();
 		Block block = event.getClickedBlock();
 		perm = plugin.permission;
 		eco = plugin.economy;
 		if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
 		if(block.getType() == Material.SIGN || (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
 			if(action == Action.RIGHT_CLICK_BLOCK && (block.getType() == Material.SIGN || (block.getType() == Material.SIGN_POST  || block.getType() == Material.WALL_SIGN))) {
 				if (block.getState() instanceof Sign) {
 					Sign sign = (Sign) block.getState();
 					String name = ChatColor.stripColor(sign.getLine(0));
 					if(name.equalsIgnoreCase("[Slots]")) {
 						if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking Permission");
 						if(perm.playerHas(player, "vaultslots.access")) {
 							if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 							String type = ChatColor.stripColor(sign.getLine(1));
 							if(config.contains("type." + type)){
 								String label = config.getString("type." + type + ".labels");
 								String[] labels = label.split(":");
 								String jackpotLID = config.getString("type." + type + ".jackpotLabel");
 								String payoutID = config.getString("type." + type + ".payout");
 								String[] payout = null;
 								int payout2 = 0;
 								int payoutItm = 0;
 								int payoutNum = 0;
 								short payoutDur = 0;
 								boolean Pecon = false;
 								if(payoutID.contains("$")) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:payout SlotType:" + type + "Econ:true");
 									Pecon = true;
 									payout2 = Integer.parseInt(payoutID.substring(1));
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:payout SlotType:" + type + "Config Got:" + payout2);
 								} else {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:payout SlotType:" + type + "Econ:false");
 									Pecon = false;
 									payout = payoutID.split(":");
 									if(payout[0].contains(">")) {
 										String value = payout[0];
 										String[] newData = value.split(">");
 										payoutItm = Integer.parseInt(newData[0]);
 										payoutDur = Short.valueOf(newData[1]);
 									} else {
 										payoutItm = Integer.parseInt(payout[0]);
 									}
 									payoutNum = Integer.parseInt(payout[1]);
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:payout SlotType:" + type + "Data Got:" + payoutItm +">" + payoutDur + ":" + payoutNum);
 								}
 								String jackpotID = config.getString("type." + type + ".jackpot");
 								String[] jackpot = null;
 								int jackpot2 = 0;
 								int jackpotItm = 0;
 								int jackpotNum = 0;
 								short jackpotDur = 0;
 								boolean Jecon = false;
 								if(jackpotID.contains("$")) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:jackpot SlotType:" + type + "Econ:true");
 									Jecon = true;
 									jackpot2 = Integer.parseInt(jackpotID.substring(1));
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:jackpot SlotType:" + type + "Config Got:" + jackpot2);
 								} else {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:jackpot SlotType:" + type + "Econ:false");
 									Jecon = false;
 									jackpot = jackpotID.split(":");
 									if(jackpot[0].contains(">")) {
 										String value = payout[0];
 										String[] newData = value.split(">");
 										jackpotItm = Integer.parseInt(newData[0]);
 										jackpotDur = Short.valueOf(newData[1]);
 									} else {
 										jackpotItm = Integer.parseInt(jackpot[0]);
 									}
 									jackpotNum = Integer.parseInt(jackpot[1]);
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:jackpot SlotType:" + type + "Data Got:" + jackpotItm +">" + jackpotDur + ":" + jackpotNum);
 								}
 								String costId = config.getString("type." + type + ".cost");
 								String[] cost = null;
 								int cost2 = 0;
 								int costItm = 0;
 								int costNum = 0;
 								short costDur = 0;
 								boolean Cecon = false;
 								if(costId.contains("$")) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:cost SlotType:" + type + "Econ:true");
 									Cecon = true;
 									cost2 = Integer.parseInt(costId.substring(1));
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:cost SlotType:" + type + "Config Got:" + cost2);
 								} else {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:cost SlotType:" + type + "Econ:false");
 									Cecon = false;
 									cost = costId.split(":");
 									if(cost[0].contains(">")) {
 										String value = payout[0];
 										String[] newData = value.split(">");
 										costItm = Integer.parseInt(newData[0]);
 										costDur = Short.valueOf(newData[1]);
 									} else {
 										costItm = Integer.parseInt(cost[0]);
 									}
 									costNum = Integer.parseInt(cost[1]);
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking:cost SlotType:" + type + "Data Got:" + costItm +">" + costDur + ":" + costNum);
 								}
 								boolean econ = false;
 								if((Pecon == true && Jecon == true) && (Cecon == true)) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Use econ?:true");
 									econ = true;
 								}
 								if(econ == false) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Use econ?:false");
 									ItemStack costing = new ItemStack(new ItemStack(costItm).getType(),costNum);
 									costing.setDurability(costDur);
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking Inventory for:" + costing);
 									if(checkInventory(player, costing)) {
 									//if(player.getInventory().contains(costing, costNum)) {
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Removing:" + costing);
 										player.getInventory().removeItem(costing);
 										player.sendMessage(ChatColor.BLUE + "You payed " + costing.getAmount() + " of " + costing.getType());
 										int rNum1 = rand.nextInt(labels.length) + 1;
 										int rNum2 = rand.nextInt(labels.length) + 1;
 										int rNum3 = rand.nextInt(labels.length) + 1;
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Random Numbers Generated:" + rNum1 + ":" + rNum2 + ":" + rNum3);
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking For Winning");
 										if(rNum1 == rNum2 && rNum2 == rNum3) {
 											if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 											String rNum = labels[(rNum1-1)];
 											try {
 												Inventory i = player.getInventory();
 												if(rNum.equals(jackpotLID)) {
 													ItemStack give = new ItemStack(jackpotItm,jackpotNum);
 													give.setDurability(jackpotDur);
 													if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Giving:" + give);
 													player.sendMessage(ChatColor.GOLD + "You Won the Jackpot of " + give.getAmount() + " of " + give.getType());
 													i.addItem(give);
 												} else {
 													ItemStack give = new ItemStack(payoutItm,payoutNum);
 													give.setDurability(payoutDur);
 													if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Giving:" + give);
 													player.sendMessage(ChatColor.GOLD + "You Won " + give.getAmount() + " of " + give.getType());
 													i.addItem(give);
 												}
 											} catch (Exception e) {
 												if(log.sendExceptionInfo(e)) return;
 												plugin.log.sendInfo("[VaultSlots] Exception: " + e);
 											}
 										} else {
 											if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 											player.sendMessage(ChatColor.GRAY + "Aw, you Lost.");
 										}
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Setting Sign");
 										String rNumA = labels[(rNum1-1)];
 										String rNumB = labels[(rNum2-1)];
 										String rNumC = labels[(rNum3-1)];
 										sign.setLine(2, ChatColor.BLUE + "" + rNumA + "|" + rNumB + "|" + rNumC);
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Sign Updated & Inventory Updated");
 										sign.update();
 										player.updateInventory();
 									} else {
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 										player.sendMessage(ChatColor.GRAY + "Insufficient Funds");
 									}
 								} else if (econ == true) {
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Use econ?:true");
 									if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking Account for:" + cost2);
 									if(eco.has(player.getName(), cost2)) {
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Removing from Account:" + cost2);
 										eco.withdrawPlayer(player.getName(), cost2);
 										player.sendMessage(ChatColor.BLUE + "You payed $" + cost2 + " and pulled the lever...");
 										int rNum1 = rand.nextInt(labels.length) + 1;
 										int rNum2 = rand.nextInt(labels.length) + 1;
 										int rNum3 = rand.nextInt(labels.length) + 1;
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Random Numbers Generated:" + rNum1 + ":" + rNum2 + ":" + rNum3);
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Checking For Winning");
 										if((rNum1 == rNum2) && (rNum2 == rNum3)) {
 											if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check true");
 											String rNum = labels[(rNum1-1)];
 											try {
 												if(rNum.equals(jackpotLID)) {
 													if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Giving to Account:" + jackpot2);
 													eco.depositPlayer(player.getName(), jackpot2);
 													player.sendMessage(ChatColor.GOLD + "You Won the Jackpot of $" + jackpot2);
 												} else {
 													if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Giving to Acconut:" + payout2);
 													eco.depositPlayer(player.getName(), payout2);
 													player.sendMessage(ChatColor.GOLD + "You Won $" + payout2);
 												}
 											} catch (Exception e) {
 												if(log.sendExceptionInfo(e)) return;
 												plugin.log.sendInfo("[VaultSlots] Execption: " + e);
 											}
 										} else {
 											if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 											player.sendMessage(ChatColor.GRAY + "Aw, you Lost.");
 										}
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Setting Sign");
 										String rNumA = labels[(rNum1-1)];
 										String rNumB = labels[(rNum2-1)];
 										String rNumC = labels[(rNum3-1)];
 										sign.setLine(2, ChatColor.BLUE + "" + rNumA + "|" + rNumB + "|" + rNumC);
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Sign Updated");
 										sign.update();
 									} else {
 										if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 										player.sendMessage(ChatColor.GRAY + "Insufficient Funds");
 									}
 								} else {
 									if(useDebug) log.sendErrorInfo("Config Error, Could not distinguish between Econ and Item");
 									player.sendMessage(ChatColor.RED + "Error, contact Server Admin.");
 								}
 							} else {
 								if(useDebug) log.sendErrorInfo("Config/Sign Erro, Could not retrive sign slot type");
 								player.sendMessage(ChatColor.RED + "Slots Type Unavaible, Contact Server Admin.");
 							}
 						} else {
 							if(useDebug) log.sendDebugInfo("Sign User:" + player.getName() + " Check false");
 							player.sendMessage(ChatColor.RED + "You Do Not Have Permission for That");
 						}
 					}
 				}
 			}
 		}
 	}
 	private boolean checkInventory(Player player, ItemStack stack) {
 		if(useDebug) log.sendDebugInfo("Custom Inventory Checking");
 		Inventory inv = player.getInventory();
 		ItemStack[] list = inv.getContents();
 		for(int i = 1; i <= list.length; i++) {
 			if(list[i-1] != null) {
 				if(useDebug) log.sendDebugInfo("? Got" + list[i-1]);
 				if(useDebug) log.sendDebugInfo("? Costing Stack:" + stack + "Costing Durability:" + stack.getDurability());
				if(list[i-1].getType() == stack.getType()) {
					if(list[i-1].getAmount() >= stack.getAmount()) {
						if(list[i-1].getDurability() == stack.getDurability()) {
							if(useDebug) log.sendDebugInfo("? Checking Match = True");
							return true;
						} else {
							if(useDebug) log.sendDebugInfo("? Checking Match = False");
						}
					}
 				}
 			}
 		}
 		return false;
 	}
 }
