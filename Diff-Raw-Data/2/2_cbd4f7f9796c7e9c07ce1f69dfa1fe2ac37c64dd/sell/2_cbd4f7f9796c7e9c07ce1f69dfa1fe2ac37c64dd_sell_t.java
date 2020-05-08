 package nl.giantit.minecraft.GiantShop.core.Commands;
 
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.API.GiantShopAPI;
 import nl.giantit.minecraft.GiantShop.API.stock.ItemNotFoundException;
 import nl.giantit.minecraft.GiantShop.API.stock.Events.StockUpdateEvent;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perm;
 import nl.giantit.minecraft.GiantShop.core.Database.db;
 import nl.giantit.minecraft.GiantShop.core.Items.*;
 import nl.giantit.minecraft.GiantShop.core.Logger.*;
 import nl.giantit.minecraft.GiantShop.core.Tools.InventoryHandler;
 import nl.giantit.minecraft.GiantShop.core.Eco.iEco;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.logging.Level;
 
 /**
  *
  * @author Giant
  */
 public class sell {
 	
 	private static config conf = config.Obtain();
 	private static db DB = db.Obtain();
 	private static perm perms = perm.Obtain();
 	private static Messages mH = GiantShop.getPlugin().getMsgHandler();
 	private static Items iH = GiantShop.getPlugin().getItemHandler();
 	private static iEco eH = GiantShop.getPlugin().getEcoHandler().getEngine();
 	
 	public static void sell(Player player, String[] args) {
 		Heraut.savePlayer(player);
 		if(perms.has(player, "giantshop.shop.sell")) {
 			if(args.length >= 2) {
 				int itemID;
 				Integer itemType = -1;
 				int quantity;
 
 				if(!args[1].matches("[0-9]+:[0-9]+")) {
 					try {
 						itemID = Integer.parseInt(args[1]);
 						itemType = -1;
 					}catch(NumberFormatException e) {
 						ItemID key = iH.getItemIDByName(args[1]);
 						if(key != null) {
 							itemID = key.getId();
 							itemType = key.getType();
 						}else{
 							Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "itemNotFound"));
 							return;
 						}
 					}catch(Exception e) {
 						if(conf.getBoolean("GiantShop.global.debug") == true) {
 							GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 							GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 						}
 
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "unknown"));
 						return;
 					}
 				}else{
 					try {
 						String[] data = args[1].split(":");
 						itemID = Integer.parseInt(data[0]);
 						itemType = Integer.parseInt(data[1]);
 					}catch(NumberFormatException e) {
 						HashMap<String, String> data = new HashMap<String, String>();
 						data.put("command", "sell");
 
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 						return;
 					}catch(Exception e) {
 						if(conf.getBoolean("GiantShop.global.debug") == true) {
 							GiantShop.log.log(Level.SEVERE, "GiantShop Error: " + e.getMessage());
 							GiantShop.log.log(Level.INFO, "Stacktrace: " + e.getStackTrace());
 						}
 
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "unknown"));
 						return;
 					}
 				}
 				
 				Integer iT = ((itemType == null || itemType == -1 || itemType == 0) ? null : itemType);
 				
 				if(args.length >= 3) {
 					try {
 						quantity = Integer.parseInt(args[2]);
 						quantity = (quantity > 0) ? quantity : 1;
 					}catch(NumberFormatException e) {
 						//Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "invQuantity"));
 						Heraut.say("As you did not specify a normal quantity, we'll just use 1 ok? :)");
 						quantity = 1;
 					}
 				}else
 					quantity = 1;
 				
 				if(iH.isValidItem(itemID, iT)) {
 					ArrayList<String> fields = new ArrayList<String>();
 					fields.add("perStack");
 					fields.add("buyFor");
 					fields.add("stock");
 					fields.add("maxStock");
 					fields.add("shops");
 
 					HashMap<String, String> where = new HashMap<String, String>();
 					where.put("itemID", String.valueOf(itemID));
 					where.put("type", String.valueOf((itemType == null || itemType == 0) ? -1 : itemType));
 
 					ArrayList<HashMap<String, String>> resSet = DB.select(fields).from("#__items").where(where).execQuery();
 					if(resSet.size() == 1) {
 						HashMap<String, String> res = resSet.get(0);
 						if(!res.get("buyFor").equals("-1.0")) {
 							String name = iH.getItemNameByID(itemID, iT);
 
 							int perStack = Integer.parseInt(res.get("perStack"));
 							int stock = Integer.parseInt(res.get("stock"));
 							int maxStock = Integer.parseInt(res.get("maxStock"));
 							double buyFor = Double.parseDouble(res.get("buyFor"));
 
 							double cost = buyFor * (double) quantity;
 							int amount = perStack * quantity;
 							
 							if(!conf.getBoolean("GiantShop.stock.useStock") || stock == -1 || maxStock == -1 || (stock + amount <= maxStock || conf.getBoolean("GiantShop.stock.allowOverStock"))) {
 								if(conf.getBoolean("GiantShop.stock.useStock") && conf.getBoolean("GiantShop.stock.stockDefinesCost") && maxStock != -1 && stock != -1) {
 									double maxInfl = conf.getDouble("GiantShop.stock.maxInflation");
 									double maxDefl = conf.getDouble("GiantShop.stock.maxDeflation");
 									int atmi = conf.getInt("GiantShop.stock.amountTillMaxInflation");
 									int atmd = conf.getInt("GiantShop.stock.amountTillMaxDeflation");
 									double split = Math.round((atmi + atmd) / 2);
 									if(maxStock <= atmi + atmd); {
 										split = maxStock / 2;
 										atmi = 0;
 										atmd = maxStock;
 									}
 									
 									if(stock >= atmd) {
 										cost = (buyFor * (1.0 - maxDefl / 100.0)) * (double) quantity; 
 									}else if(stock <= atmi) {
 										cost = (buyFor * (1.0 + maxInfl / 100.0)) * (double) quantity; 
 									}else{
 										if(stock < split) {
 											cost = (double)Math.round(((buyFor * (1.0 + (maxInfl / stock) / 100)) * (double) quantity) * 100.0) / 100.0;
 										}else if(stock > split) {
 											cost = 2.0 + (double)Math.round(((buyFor / (maxDefl * stock / 100)) * (double) quantity) * 100.0) / 100.0;
 										}
 									}
 								}
 								ItemStack iStack;
 								Inventory inv = player.getInventory();
 	
 								if(itemType != null && itemType != -1) {
 									if(itemID != 373)
 										iStack = new MaterialData(itemID, (byte) ((int) itemType)).toItemStack(amount);
 									else
 										iStack = new ItemStack(itemID, amount, (short) ((int) itemType));
 								}else{
 									iStack = new ItemStack(itemID, amount);
 								}
 								
 								int stackAmt = InventoryHandler.hasAmount(inv, iStack);
 								if(stackAmt >= amount) {
 									if(conf.getBoolean("GiantShop.global.broadcastSell"))
 										Heraut.broadcast(player.getName() + " sold some " + name);
 	
 									eH.deposit(player, cost);
 	
 									Heraut.say("You have just sold " + amount + " of " + name + " for " + cost);
 									Heraut.say("Your new balance is: " + eH.getBalance(player));
 									Logger.Log(LoggerType.SELL,
 												player, 
 												"{id: " + String.valueOf(itemID) + "; " +
 												"type:" + String.valueOf((itemType == null || itemType <= 0) ? -1 : itemType) + "; " +
 												"oS:" + String.valueOf(stock) + "; " +
												"nS:" + String.valueOf((stock != -1 ? stock + amount : stock)) + "; " +
 												"amount:" + String.valueOf(amount) + ";" +
 												"total:" + String.valueOf(cost) + ";}");
 									
 									InventoryHandler.removeItem(inv, iStack);
 									
 									if(conf.getBoolean("GiantShop.stock.useStock") && stock != -1) {
 										HashMap<String, String> t = new HashMap<String, String>();
 										t.put("stock", String.valueOf((stock + amount)));
 	
 										DB.update("#__items").set(t).where(where).updateQuery();
 
 										try {
 											StockUpdateEvent event = new StockUpdateEvent(player, GiantShopAPI.Obtain().getStockAPI().getItemStock(itemID, itemType), StockUpdateEvent.StockUpdateType.INCREASE);
 											GiantShop.getPlugin().getSrvr().getPluginManager().callEvent(event);
 										}catch(ItemNotFoundException e) {}
 									}
 								}else{
 									HashMap<String, String> data = new HashMap<String, String>();
 									data.put("needed", String.valueOf(amount));
 									data.put("have", String.valueOf(stackAmt));
 	
 									Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "insufItems", data));
 								}
 							}else{
 								Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "stockExeedsMaxStock"));
 							}
 						}else{
 							Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noReturns"));
 						}
 					}else{
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noneOrMoreResults"));
 					}
 				}else{
 					Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "itemNotFound"));
 				}
 			}else{
 				HashMap<String, String> data = new HashMap<String, String>();
 				data.put("command", "sell");
 
 				Heraut.say(mH.getMsg(Messages.msgType.ERROR, "syntaxError", data));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "sell");
 
 			Heraut.say(mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 	
 }
