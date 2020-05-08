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
 	private SimpleDateFormat dateFormat = new SimpleDateFormat(Messages.getMessage(Message.DATE_FORMAT));
 
 	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		// only allow breaking if has permission and is sneaking
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		final BillboardSign billboard = Billboards.instance.getBillboard(block.getLocation());
 		if (billboard != null && Billboards.instance.refreshSign(billboard)) {
 			boolean breakFailed = false;
 			if (!player.isSneaking()) {
 				breakFailed = true;
 				player.sendMessage(Messages.getMessage(Message.YOU_HAVE_TO_SNEAK));
 			} else if (!((billboard.hasCreator() && billboard.getCreator().equals(player.getName())) || player.hasPermission(Billboards.ADMIN_PERMISSION))) {
 				breakFailed = true;
 				player.sendMessage(Messages.getMessage(Message.NO_PERMISSION));
 			}
 
 			if (breakFailed) {
 				event.setCancelled(true);
 				Billboards.instance.getServer().getScheduler().runTaskLater(Billboards.instance, new Runnable() {
 
 					@Override
 					public void run() {
 						// refresh sign to display text:
 						Billboards.instance.refreshSign(billboard);
 					}
 				}, 1L);
 			} else {
 				// remove billboard:
 				Billboards.instance.removeBillboard(billboard);
 				player.sendMessage(Messages.getMessage(Message.SIGN_REMOVED));
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
 	public void onInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block clickedBlock = event.getClickedBlock();
 
 		if (clickedBlock != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 
 			BillboardSign billboardC = Billboards.instance.customers.remove(playerName);
 
 			if (clickedBlock.getType() == Material.SIGN_POST || clickedBlock.getType() == Material.WALL_SIGN) {
 				BillboardSign billboard = Billboards.instance.getBillboard(clickedBlock.getLocation());
 				if (billboard != null && Billboards.instance.refreshSign(billboard)) {
 
 					// cancle all block-placing against a billboard sign already here:
 					event.setCancelled(true);
 
 					// can rent?
 					if (!player.hasPermission(Billboards.RENT_PERMISSION)) {
 						player.sendMessage(Messages.getMessage(Message.NO_PERMISSION));
 						return;
 					}
 					// own sign?
 					if (billboard.getCreator().equals(playerName)) {
 						player.sendMessage(Messages.getMessage(Message.CANT_RENT_OWN_SIGN));
 						return;
 					}
 
 					if (billboardC != null && billboardC == billboard) {
 						// check if it's still available:
 						if (!billboard.hasOwner()) {
 							// check if player has enough money:
 							if (Billboards.economy.has(playerName, billboard.getPrice())) {
 								// rent:
 								// take money:
 								EconomyResponse withdraw = Billboards.economy.withdrawPlayer(playerName, billboard.getPrice());
 								// transaction successful ?
 								if (!withdraw.transactionSuccess()) {
 									// something went wrong
 									player.sendMessage(Messages.getMessage(Message.TRANSACTION_FAILURE, withdraw.errorMessage));
 									return;
 								}
 
 								if (billboard.hasCreator()) {
 									// give money to the creator:
 									EconomyResponse deposit = Billboards.economy.depositPlayer(billboard.getCreator(), billboard.getPrice());
 									// transaction successful ?
 									if (!deposit.transactionSuccess()) {
 										// something went wrong :(
 										player.sendMessage(Messages.getMessage(Message.TRANSACTION_FAILURE, deposit.errorMessage));
 
 										// try to refund the withdraw
 										EconomyResponse withdrawUndo = Billboards.economy.depositPlayer(playerName, withdraw.amount);
 										if (!withdrawUndo.transactionSuccess()) {
 											// this is really bad:
 											player.sendMessage(Messages.getMessage(Message.TRANSACTION_FAILURE, withdrawUndo.errorMessage));
 										}
 										player.updateInventory();
 										return;
 									}
 								}
 
 								player.updateInventory();
 
 								// set new owner:
 								billboard.setOwner(playerName);
 								billboard.setStartTime(System.currentTimeMillis());
 								Billboards.instance.saveCurrentConfig();
 
 								// initialize new sign text:
 								Sign sign = (Sign) clickedBlock.getState();
 
 								String[] args = new String[] { String.valueOf(billboard.getPrice()), String.valueOf(billboard.getDurationInDays()), billboard.getCreator(), playerName };
 
 								sign.setLine(0, Billboards.trimTo16(Messages.getMessage(Message.RENT_SIGN_LINE_1, args)));
 								sign.setLine(1, Billboards.trimTo16(Messages.getMessage(Message.RENT_SIGN_LINE_2, args)));
 								sign.setLine(2, Billboards.trimTo16(Messages.getMessage(Message.RENT_SIGN_LINE_3, args)));
 								sign.setLine(3, Billboards.trimTo16(Messages.getMessage(Message.RENT_SIGN_LINE_4, args)));
 								sign.update();
 
 								player.sendMessage(Messages.getMessage(Message.YOU_HAVE_RENT_A_SIGN, String.valueOf(billboard.getPrice()), String.valueOf(billboard.getDurationInDays()), billboard.getCreator()));
 							} else {
 								// not enough money:
 								player.sendMessage(Messages.getMessage(Message.NOT_ENOUGH_MONEY, String.valueOf(billboard.getPrice()), String.valueOf(Billboards.economy.getBalance(playerName))));
 							}
 						} else {
 							// no longer available:
 							player.sendMessage(Messages.getMessage(Message.NO_LONGER_AVAILABLE));
 						}
 					} else {
 						// check if available:
 						if (!billboard.hasOwner()) {
 							// check if the player already owns to many billboards:
							if (Billboards.instance.getRentBillboards(playerName).size() >= Billboards.instance.maxRent) {
 								player.sendMessage(Messages.getMessage(Message.MAX_RENT_LIMIT_REACHED, String.valueOf(Billboards.instance.maxRent)));
 							} else if (Billboards.economy.has(playerName, billboard.getPrice())) {
 								// check if player has enough money:
 								// click again to rent:
 								player.sendMessage(Messages.getMessage(Message.CLICK_TO_RENT, String.valueOf(billboard.getPrice()), String.valueOf(billboard.getDurationInDays()), billboard.getCreator()));
 								Billboards.instance.customers.put(playerName, billboard);
 							} else {
 								// no enough money:
 								player.sendMessage(Messages.getMessage(Message.NOT_ENOUGH_MONEY, String.valueOf(billboard.getPrice()), String.valueOf(Billboards.economy.getBalance(playerName))));
 							}
 						} else {
 							// is owner -> edit
 							if (player.getItemInHand().getType() == Material.SIGN && billboard.hasOwner() && (billboard.getOwner().equals(playerName) || player.hasPermission(Billboards.ADMIN_PERMISSION))) {
 								// do not cancel, so that the place event is called:
 								event.setCancelled(false);
 							} else {
 								// print information of sign:
 								player.sendMessage(Messages.getMessage(Message.INFO_HEADER));
 								player.sendMessage(Messages.getMessage(Message.INFO_CREATOR, billboard.getCreator()));
 								player.sendMessage(Messages.getMessage(Message.INFO_OWNER, billboard.getOwner()));
 								player.sendMessage(Messages.getMessage(Message.INFO_PRICE, String.valueOf(billboard.getPrice())));
 								player.sendMessage(Messages.getMessage(Message.INFO_DURATION, String.valueOf(billboard.getDurationInDays())));
 								player.sendMessage(Messages.getMessage(Message.INFO_RENT_SINCE, dateFormat.format(new Date(billboard.getStartTime()))));
 
 								long endTime = billboard.getEndTime();
 								player.sendMessage(Messages.getMessage(Message.INFO_RENT_UNTIL, dateFormat.format(new Date(endTime))));
 
 								long left = endTime - System.currentTimeMillis();
 								long days = TimeUnit.MILLISECONDS.toDays(left);
 								long hours = TimeUnit.MILLISECONDS.toHours(left) - TimeUnit.DAYS.toHours(days);
 								long minutes = TimeUnit.MILLISECONDS.toMinutes(left) - TimeUnit.DAYS.toMinutes(days) - TimeUnit.HOURS.toMinutes(hours);
 								String timeLeft = String.format(Messages.getMessage(Message.TIME_REMAINING_FORMAT), days, hours, minutes);
 
 								player.sendMessage(Messages.getMessage(Message.INFO_TIME_LEFT, timeLeft));
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
 	public void onBlockPlaceEarly(BlockPlaceEvent event) {
 		Block placed = event.getBlockPlaced();
 		Material typePlaced = placed.getType();
 
 		if (!(typePlaced == Material.WALL_SIGN || typePlaced == Material.SIGN_POST)) return;
 
 		Block against = event.getBlockAgainst();
 		Material typeAgainst = against.getType();
 
 		if (!(typeAgainst == Material.WALL_SIGN || typeAgainst == Material.SIGN_POST)) return;
 
 		BillboardSign billboard = Billboards.instance.getBillboard(against.getLocation());
 
 		if (billboard != null) {
 			Player player = event.getPlayer();
 			String playerName = player.getName();
 
 			// cancle event, so other plugins ignore it and don't print messages for cancelling it:
 			event.setCancelled(true);
 
 			if (billboard.hasOwner() && (billboard.getOwner().equals(playerName) || player.hasPermission(Billboards.ADMIN_PERMISSION))) {
 				edit.put(playerName, new SignEdit(placed.getLocation(), billboard));
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
 	public void onBlockPlaceLate(BlockPlaceEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 
 		if (edit.containsKey(playerName)) {
 			// make sure the sign can be placed, so that the sign edit window opens for the player
 			event.setCancelled(false);
 
 			/*
 			 * Sign update = (Sign) block.getState();
 			 * Sign editing = (Sign) event.getBlockAgainst().getState();
 			 * int i = 0;
 			 * for (String line : editing.getLines())
			 * update.setLine(i++, line.replace("&", "&&").replace('', '&'));
 			 * update.update();
 			 */
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@EventHandler
 	public void onSignEdit(SignChangeEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 
 		SignEdit signEdit = edit.remove(playerName);
 		if (signEdit != null) {
 			if (Billboards.instance.refreshSign(signEdit.billboard)) {
 				// still owner and has still the permission?
 				if (signEdit.billboard.hasOwner() && (signEdit.billboard.getOwner().equals(playerName) || player.hasPermission(Billboards.ADMIN_PERMISSION)) && player.hasPermission(Billboards.RENT_PERMISSION)) {
 					Sign target = (Sign) signEdit.billboard.getLocation().getBukkitLocation(Billboards.instance).getBlock().getState();
 					for (int i = 0; i < 4; i++) {
 						target.setLine(i, event.getLine(i));
 					}
 					target.update();
 				}
 			}
 			// cancle and give sign back:
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
