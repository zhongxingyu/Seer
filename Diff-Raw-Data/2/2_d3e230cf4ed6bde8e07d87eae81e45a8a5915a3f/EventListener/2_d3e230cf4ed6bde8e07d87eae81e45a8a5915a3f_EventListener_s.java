 package de.blablubbabc.billboards;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class EventListener implements Listener {
 	
 	private Map<String, SignEdit> edit = new HashMap<String, SignEdit>();
 
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		// only allow breaking if has permission and is sneaking
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		final AdSign adsign = Billboards.instance.getAdSign(block.getLocation());
 		if (adsign != null && Billboards.instance.refreshSign(adsign)) {
 			if (player.isSneaking() && player.hasPermission(Billboards.PERMISSION_ADMIN)) {
 				// remove adsign:
 				Billboards.instance.removeAdSign(adsign);
 				player.sendMessage(Messages.getMessage(Message.SIGN_REMOVED));
 			} else {
 				if (!player.hasPermission(Billboards.PERMISSION_ADMIN))player.sendMessage(Messages.getMessage(Message.NO_PERMISSION));
 				else player.sendMessage(Messages.getMessage(Message.YOU_HAVE_TO_SNEAK));
 				event.setCancelled(true);
 				Billboards.instance.getServer().getScheduler().runTaskLater(Billboards.instance, new Runnable() {
 					
 					@Override
 					public void run() {
 						// refresh sign to display text:
 						Billboards.instance.refreshSign(adsign);
 					}
 				}, 1L);
 			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
 	public void onInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getClickedBlock();
 		if (block != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			
 			AdSign adSignC = Billboards.instance.customers.get(playerName);
 			Billboards.instance.customers.remove(playerName);
 			
 			if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
 				AdSign adsign = Billboards.instance.getAdSign(block.getLocation());
 				if (adsign != null && Billboards.instance.refreshSign(adsign)) {
 					if (adSignC != null && adSignC == adsign) {
 						// check if it's still available:
 						if (!adsign.hasOwner()) {
 							// check if player has enough money:
 							if (Billboards.economy.has(playerName, adsign.getPrice())) {
 								// rent:
 								// take money:
 								EconomyResponse response = Billboards.economy.withdrawPlayer(playerName, adsign.getPrice());
 								// transaction successfull ?
 								if (response.transactionSuccess()) {
 									player.updateInventory();
 									// set new owner:
 									adsign.setOwner(playerName);
 									adsign.setStartTime(System.currentTimeMillis());
 									Billboards.instance.saveCurrentConfig();
 									player.sendMessage(Messages.getMessage(Message.YOU_HAVE_RENT_A_SIGN, String.valueOf(adsign.getPrice()), String.valueOf(adsign.getDurationInDays())));
 								} else {
 									// something went wrong
 									player.sendMessage(Messages.getMessage(Message.TRANSACTION_FAILURE, response.errorMessage));
 								}
 							} else {
 								// not enough money:
 								player.sendMessage(Messages.getMessage(Message.NOT_ENOUGH_MONEY, String.valueOf(adsign.getPrice()), String.valueOf(Billboards.economy.getBalance(playerName))));
 							}
 						} else {
 							// no longer available:
 							player.sendMessage(Messages.getMessage(Message.NO_LONGER_AVAILABLE));
 						}
 					} else {
 						// can rent?
 						if (player.hasPermission(Billboards.PERMISSION_PLAYER)) {
 							// check if available:
 							if (!adsign.hasOwner()) {
 								// check if player has enough money:
 								if (Billboards.economy.has(playerName, adsign.getPrice())) {
 									// click again to rent:
 									player.sendMessage(Messages.getMessage(Message.CLICK_TO_RENT, String.valueOf(adsign.getPrice()), String.valueOf(adsign.getDurationInDays())));
 									Billboards.instance.customers.put(playerName, adsign);
 								} else {
 									// no enough money:
 									player.sendMessage(Messages.getMessage(Message.NOT_ENOUGH_MONEY, String.valueOf(adsign.getPrice()), String.valueOf(Billboards.economy.getBalance(playerName))));
 								}
 							} else {
 								// is owner -> edit
 								if (player.getItemInHand().getType() == Material.SIGN && adsign.hasOwner() && (adsign.getOwner().equals(playerName) || player.hasPermission(Billboards.PERMISSION_ADMIN))) {
 									// do not cancel, so that the place event is called:
 									if (event.isCancelled()) event.setCancelled(false);
 									edit.put(playerName, new SignEdit(block.getRelative(event.getBlockFace()).getLocation(), adsign));
 								} else {
 									// print information of sign:
 									player.sendMessage(Messages.getMessage(Message.INFO_HEADER));
 									player.sendMessage(Messages.getMessage(Message.INFO_OWNER, adsign.getOwner()));
 									player.sendMessage(Messages.getMessage(Message.INFO_PRICE, String.valueOf(adsign.getPrice())));
 									player.sendMessage(Messages.getMessage(Message.INFO_DURATION, String.valueOf(adsign.getDurationInDays())));
 									player.sendMessage(Messages.getMessage(Message.INFO_RENT_SINCE, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(adsign.getStartTime()))));
 									
 									long endTime = adsign.getEndTime();
 									player.sendMessage(Messages.getMessage(Message.INFO_RENT_UNTIL, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(endTime))));
 									
 									long left = endTime - System.currentTimeMillis();
 									long days = TimeUnit.MILLISECONDS.toDays(left);
 									long hours = TimeUnit.MILLISECONDS.toHours(left) - TimeUnit.DAYS.toHours(days);
 									long minutes = TimeUnit.MILLISECONDS.toMinutes(left) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
									String timeLeft = String.format("%d Tage %d h %d min", days, hours, minutes);
 									
 									player.sendMessage(Messages.getMessage(Message.INFO_TIME_LEFT, timeLeft));
 								}
 							}
 						} else {
 							player.sendMessage(Messages.getMessage(Message.NO_PERMISSION));
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		Block block = event.getBlockPlaced();
 		Block against = event.getBlockAgainst();
 		if (!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) || !(against.getType() == Material.WALL_SIGN || against.getType() == Material.SIGN_POST)) return;
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		
 		if (edit.containsKey(playerName)) {
 			if (event.isCancelled()) event.setCancelled(false);
 			/*Sign update = (Sign) block.getState();
 			Sign editing = (Sign) event.getBlockAgainst().getState();
 			int i = 0;
 			for (String line : editing.getLines())
 				update.setLine(i++, line.replace("&", "&&").replace('', '&'));
 			update.update();*/
 		} else {
 			AdSign adsign = Billboards.instance.getAdSign(against.getLocation());
 			if (adsign != null) {
 				// no sign placing against an adsign, if not in edit mode:
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onSignEdit(SignChangeEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		
 		SignEdit signEdit = edit.get(playerName);
 		if (signEdit != null) {
 			if (Billboards.instance.refreshSign(signEdit.adsign)) {
 				// still owner and has still the permission?
 				if (signEdit.adsign.hasOwner() && (signEdit.adsign.getOwner().equals(playerName) || player.hasPermission(Billboards.PERMISSION_ADMIN)) && player.hasPermission(Billboards.PERMISSION_PLAYER)) {
 					Sign target = (Sign) signEdit.adsign.getLocation().getBukkitLocation(Billboards.instance).getBlock().getState();
 					for (int i = 0; i < 4; i++) {
 						target.setLine(i, event.getLine(i));
 					}
 					target.update();
 				}
 			}
 			// cancle and give sign back:
 			edit.remove(playerName);
 			event.setCancelled(true);
 			signEdit.source.getBlock().setType(Material.AIR);
 			if (player.getGameMode() != GameMode.CREATIVE) {
 				ItemStack inHand = player.getItemInHand();
 				if (inHand == null || inHand.getType() == Material.AIR) {
 					player.setItemInHand(new ItemStack(Material.SIGN, 1));
 				} else if (inHand.getType() == Material.SIGN) {
 					inHand.setAmount(inHand.getAmount() + 1);
 				}
 				player.updateInventory();
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onQuit(PlayerQuitEvent event) {
 		Billboards.instance.customers.remove(event.getPlayer().getName());
 	}
 	
 }
