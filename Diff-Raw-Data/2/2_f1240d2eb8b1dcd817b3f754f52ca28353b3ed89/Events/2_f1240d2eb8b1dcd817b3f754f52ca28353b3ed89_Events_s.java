 package net.downwithdestruction.dwdshop;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class Events implements Listener {
 
 	public static Set<String> repairMode = new HashSet<String>();
 	public static Set<String> createMode = new HashSet<String>();
 
 	public Events(DwDShopPlugin plugin) {
 
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.getPlayer().isSneaking()) return;
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			Location location = event.getClickedBlock().getLocation();
 			Block block = location.getBlock();
 			if (block.getType() == Material.SIGN
 					|| block.getType() == Material.WALL_SIGN
 					|| block.getType() == Material.SIGN_POST) {
 				if (repairMode.contains(event.getPlayer().getName())) {
 					// Convert from old format
 					Sign sign = (Sign) block.getState();
 					DwDShopPlugin.debug("Line 0:" + sign.getLine(0));
 					if ((sign.getLine(0).toLowerCase().contains("admin") && sign
 							.getLine(0).toLowerCase().contains("shop"))
 							|| Shops.isShop(location)) {
 						int amount = Integer.parseInt(sign.getLine(1));
 
 						int x, y, z;
 						x = location.getBlockX();
 						y = location.getBlockY();
 						z = location.getBlockZ();
 
 						Entity[] entities = location.getChunk().getEntities();
 						for (Entity e : entities) {
 							Location eLoc = e.getLocation();
 							if ((eLoc.getBlockX() == x)
 									&& (eLoc.getBlockZ()) == z
 									&& (eLoc.getBlockY() > y)
 									&& (eLoc.getBlockY() <= (y + 2))
 									&& (e.getType() == EntityType.ITEM_FRAME)) {
 								// Found the entity
 
 								ItemFrame frame = (ItemFrame) e;
 								int item = frame.getItem().getTypeId();
 								short damage = frame.getItem().getDurability();
 
 								String itemID = (damage == 0) ? "" + item
 										: item + ":" + damage;
 
 								// Get Prices
 								try {
 									DwDShopPlugin.debug("Query: SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 													+ itemID + "' LIMIT 1");
 									ResultSet results = DwDShopPlugin.db
 											.query("SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 													+ itemID + "' LIMIT 1");
 									if (results.first()) {
 										DwDShopPlugin.debug("Found price");
 										double buy, sell;
 										buy = results.getDouble("buy");
 										sell = results.getDouble("sell");
 
 										// Add the local storage
 										Shop shop = Shops.createShop(location, item,
 												damage, amount, buy, sell);
 
 										shop.update();
 										event.getPlayer()
 												.sendMessage(
 														DwDShopPlugin.lang
 																.get("signs.shopUpdated"));
 									} else {
 										// Item not found
 										event.getPlayer()
 												.sendMessage(
 														DwDShopPlugin.lang
 																.get("exceptions.itemNotFound"));
 									}
 								} catch (SQLException e1) {
 									e1.printStackTrace();
 								}
 
 								break;
 							}
 						}
 					} else {
 						event.getPlayer().sendMessage(
 								DwDShopPlugin.lang
 										.get("exceptions.notAdminShop"));
 					}
 					event.setUseItemInHand(Event.Result.DENY);
 					event.setUseInteractedBlock(Event.Result.DENY);
 				} else {
 					if (Shops.isShop(location)) {
 						// Sell
 						Player player = event.getPlayer();
 						Shop shop = Shops.getShop(location);
 						
 						DwDShopPlugin.debug("Sell Request "+shop.getAmount()+" of "+shop.getItemID()+":"+shop.getItemDamage()+" for "+shop.getBuy());
 
 						String itemID = ""+shop.getItemID();
 						// Check inventory
 						if (player.getInventory().contains(
 								Material.getMaterial(shop.getItemID()), shop.getAmount())) {
 							double price = shop.getSell() * shop.getAmount();
 							short damage = (short) shop.getItemDamage();
 							
 							if(damage > 0) {
 								itemID = itemID+":"+damage;
 							}
 
 							ResultSet results;
 							try {
 								DwDShopPlugin.debug("Query: SELECT `itemName` FROM `Items` WHERE `itemID`='"
 										+ itemID + "' LIMIT 1");
 								results = DwDShopPlugin.db
 										.query("SELECT `itemName` FROM `Items` WHERE `itemID`='"
 												+ itemID + "' LIMIT 1");
 								if (results.first()) {
 									// Take ze items & ze moneh
 									DwDShopPlugin.economy.depositPlayer(
 											player.getName(), price);
 									ItemStack items = new ItemStack(
 											shop.getItemID(), shop.getAmount(), damage);
 									player.getInventory().removeItem(items);
 									player.updateInventory();
 									// Done \o/
 									String itemName = results
 											.getString("itemName");
 									player.sendMessage(DwDShopPlugin.lang
 											.get("signs.sellComplete")
 											.replaceAll("%I", itemName)
 											.replaceAll("%P", "" + price)
 											.replaceAll("%A", "" + shop.getAmount()));
 								}
 							} catch (SQLException e) {
 								player.sendMessage(DwDShopPlugin.lang
 										.get("exceptions.itemNotFound"));
 							}
 						} else {
 							// Not enough items
 							player.sendMessage(DwDShopPlugin.lang
 									.get("exceptions.notEnoughItems"));
 						}
 						event.setUseItemInHand(Event.Result.DENY);
 						event.setUseInteractedBlock(Event.Result.DENY);
 					}
 				}
 			}
 		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
 			Location location = event.getClickedBlock().getLocation();
 			if (Shops.isShop(location)) {
 				// Buy
 				Player player = event.getPlayer();
 				Shop shop = Shops.getShop(location);
 				
 				DwDShopPlugin.debug("Buy Request "+shop.getAmount()+" of "+shop.getItemID()+":"+shop.getItemDamage()+" for "+shop.getBuy());
 
 				String itemID = ""+shop.getItemID();
 				short damage = (short) shop.getItemDamage();
 				double price = shop.getBuy() * shop.getAmount();
 				
 				if(damage > 0) {
 					itemID = itemID+":"+damage;
 				}
 				
 				// Check inventory
 				if (DwDShopPlugin.economy.has(player.getName(), price)) {
 
					if(player.getInventory().firstEmpty() > 0) {
 						ResultSet results;
 					
 					try {
 						DwDShopPlugin.debug("Query: SELECT `itemName` FROM `Items` WHERE `itemID`='"
 								+ itemID + "' LIMIT 1");
 						results = DwDShopPlugin.db
 								.query("SELECT `itemName` FROM `Items` WHERE `itemID`='"
 										+ itemID + "' LIMIT 1");
 						if (results.first()) {
 							// Take ze items & ze moneh
 							DwDShopPlugin.economy.withdrawPlayer(
 									player.getName(), price);
 							ItemStack items = new ItemStack(
 									shop.getItemID(), shop.getAmount(), damage);
 							player.getInventory().addItem(items);
 							// Done \o/
 							String itemName = results
 									.getString("itemName");
 							player.sendMessage(DwDShopPlugin.lang
 									.get("signs.buyComplete")
 									.replaceAll("%I", itemName)
 									.replaceAll("%P", "" + price)
 									.replaceAll("%A", "" + shop.getAmount()));
 						}
 					} catch (SQLException e) {
 						player.sendMessage(DwDShopPlugin.lang
 								.get("exceptions.itemNotFound"));
 					}
 					}
 					else {
 						player.sendMessage(DwDShopPlugin.lang
 								.get("exceptions.notEnoughItemSpace"));
 					}
 				} else {
 					// Not enough items
 					player.sendMessage(DwDShopPlugin.lang
 							.get("exceptions.notEnoughFunds"));
 				}
 				event.setUseItemInHand(Event.Result.DENY);
 				event.setUseInteractedBlock(Event.Result.DENY);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 		DwDShopPlugin.debug("SignChangeEvent triggered");
 		if (createMode.contains(event.getPlayer().getName())) {
 			DwDShopPlugin.debug("Player in create mode");
 			Block block = event.getBlock();
 			Location location = block.getLocation();
 			if (block.getType().equals(Material.SIGN)
 					|| block.getType().equals(Material.WALL_SIGN)
 					|| block.getType().equals(Material.SIGN_POST)) {
 				DwDShopPlugin.debug("Block is a sign");
 
 				if (event.getLine(1).toLowerCase().contains("[shop]")) {
 					DwDShopPlugin.debug("Sign contains [shop]");
 					int amount = 1;
 
 					int x, y, z;
 					x = location.getBlockX();
 					y = location.getBlockY();
 					z = location.getBlockZ();
 
 					Entity[] entities = location.getChunk().getEntities();
 					for (Entity e : entities) {
 						Location eLoc = e.getLocation();
 						if ((eLoc.getBlockX() == x) && (eLoc.getBlockZ()) == z
 								&& (eLoc.getBlockY() > y)
 								&& (eLoc.getBlockY() <= (y + 2))
 								&& (e.getType() == EntityType.ITEM_FRAME)) {
 							// Found the entity
 
 							// Work out amount (1 or 64)
 							if ((eLoc.getBlockY() - location.getBlockY()) > 1) {
 								amount = 64;
 							}
 
 							ItemFrame frame = (ItemFrame) e;
 							int item = frame.getItem().getTypeId();
 							short damage = frame.getItem().getDurability();
 
 							String itemID = (damage == 0) ? "" + item : item
 									+ ":" + damage;
 
 							// Get Prices
 							try {
 								DwDShopPlugin
 										.debug("Query: SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 												+ itemID + "' LIMIT 1");
 								ResultSet results = DwDShopPlugin.db
 										.query("SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 												+ itemID + "' LIMIT 1");
 								if (results.first()) {
 									DwDShopPlugin.debug("Found price");
 									double buy, sell;
 									buy = results.getDouble("buy");
 									sell = results.getDouble("sell");
 
 									// Add the local storage
 									Shop shop = Shops.createShop(location,
 											item, damage, amount, buy, sell);
 
 									String[] signText = shop.update();
 
 									event.setLine(0, signText[0]);
 									event.setLine(1, signText[1]);
 									event.setLine(2, signText[2]);
 									event.setLine(3, signText[3]);
 
 									event.getPlayer().sendMessage(
 											DwDShopPlugin.lang
 													.get("signs.shopCreated"));
 								} else {
 									// Item not found
 									event.getPlayer()
 											.sendMessage(
 													DwDShopPlugin.lang
 															.get("exceptions.itemNotFound"));
 								}
 							} catch (SQLException e1) {
 								e1.printStackTrace();
 							}
 
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		Location loc = event.getBlock().getLocation();
 		if (Shops.isShop(loc)) {
 			if (event.getPlayer().hasPermission("dwdshop.shop.delete")) {
 				Shops.deleteShop(loc);
 				event.getPlayer().sendMessage(
 						DwDShopPlugin.lang.get("signs.shopDeleted"));
 			} else {
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(
 						DwDShopPlugin.lang.get("exceptions.noPermission"));
 			}
 		}
 	}
 }
