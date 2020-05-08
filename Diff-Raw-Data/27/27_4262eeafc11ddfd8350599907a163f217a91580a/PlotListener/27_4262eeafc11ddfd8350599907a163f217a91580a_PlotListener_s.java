 package com.mtihc.regionselfservice.v2.plots;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.util.BlockVector;
 
 import com.mtihc.regionselfservice.v2.plots.exceptions.SignException;
 import com.mtihc.regionselfservice.v2.plots.signs.ForRentSign;
 import com.mtihc.regionselfservice.v2.plots.signs.ForSaleSign;
 import com.mtihc.regionselfservice.v2.plots.signs.PlotSignText;
 import com.mtihc.regionselfservice.v2.plots.signs.PlotSignText.ForRentSignText;
 import com.mtihc.regionselfservice.v2.plots.signs.PlotSignText.ForSaleSignText;
 import com.mtihc.regionselfservice.v2.plots.signs.PlotSignType;
 import com.mtihc.regionselfservice.v2.plots.util.TimeStringConverter;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 class PlotListener implements Listener {
 
 	private PlotManager mgr;
 	
 	PlotListener(PlotManager mgr) {
 		this.mgr = mgr;
 	}
 	
 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 		if(event.isCancelled()) {
 			return;// event was cancelled
 		}
 		if(!(event.getBlock().getState() instanceof Sign)) {
 			return;
 		}
 		
 		Sign sign = (Sign) event.getBlock().getState();
 		PlotSignType type = PlotSignType.getPlotSignType(event.getLines());
 		if(type == null) {
 			return;// not a plot-sign
 		}
 		
 		// player
 		Player player = event.getPlayer();
 		
 		// plot world
 		World world = sign.getWorld();
 		PlotWorld plotWorld = mgr.getPlotWorld(world.getName());
 		
 		
 
 		Plot plot;
 		PlotSignText<?> signText;
 		IPlotSign plotSign;
 		
 		try {
 			// try to read the sign
 			signText = PlotSignText.createText(plotWorld, event.getLines());
 			
 			// get plot data
  			plot = plotWorld.getPlot(signText.getRegionId());
 			
 			// create sign data... later
 			// add sign to plot... later
 			
 		} catch (SignException e) {
 			// invalid sign
 			player.sendMessage(ChatColor.RED + "Failed to create " + type.name() + " sign: ");
 			player.sendMessage(ChatColor.RED + e.getMessage());
 			// break the sign
 			event.setCancelled(true);
 			sign.getBlock().breakNaturally();
 			return;
 		}
 		
 		ProtectedRegion region = plot.getRegion();
 		if(region == null) {
 			player.sendMessage(ChatColor.RED + "Failed to create " + type.name() + " sign. ");
 			player.sendMessage(ChatColor.RED + "Region \"" + plot.getRegionId() + "\" doesn't exist.");
 			event.setCancelled(true);
 			sign.getBlock().breakNaturally();
 			return;
 		}
 		
 		boolean isOwner = region.isOwner(player.getName());
 		boolean isInside = region.contains(sign.getX(), sign.getY(), sign.getZ());
 		
 		IPlotWorldConfig config = plot.getPlotWorld().getConfig();
 
 		if(type == PlotSignType.FOR_RENT) {
 			
 			// check permission to rent out
 			if(!player.hasPermission(Permission.RENTOUT)) {
 				player.sendMessage(ChatColor.RED + "You don't have permission to rent out regions.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			// check permission to rent out, unowned regions
 			if(!isOwner && !player.hasPermission(Permission.RENTOUT_ANYREGION)) {
 				player.sendMessage(ChatColor.RED + "You can't rent out regions that you don't own.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			// check permission to rent out, outside the region
 			if(!isInside && !player.hasPermission(Permission.RENTOUT_ANYWHERE)) {
 				player.sendMessage(ChatColor.RED + "You can't place this sign outside the region itself.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			
 			ForRentSignText rentText = (ForRentSignText) signText;
 			// check if is rented out, then player typed a name on the sign instead of cost
 			if(rentText.isRentedOut()) {
 				player.sendMessage(ChatColor.RED + "Invalid sign text. Expected rent-cost and rent-time.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			double rentCostOld = plot.getRentCost();
 			double rentCost = rentText.getRentCost();
 			long rentTimeOld = plot.getRentTime();
 			long rentTime = rentText.getRentTime();
 			
 			rentText.applyToSign(event);
 			
 			// check min/max cost
 			double minRentCost = plot.getWorth(config.getOnRentMinBlockCost());
 			double maxRentCost = plot.getWorth(config.getOnRentMaxBlockCost());
			if(rentCost < minRentCost) {
 				player.sendMessage(ChatColor.RED + "The price is too low.");
				// TODO The rent-price must be between minRentCost per minRentTime and maxRentCost per maxRentTime.
				// So we need to add time to the config. Orrrr interpret the min/max rent cost as "rent cost per hour"
				player.sendMessage(ChatColor.RED + "The rent-price must be between " + mgr.getEconomy().format(minRentCost) + " and " + mgr.getEconomy().format(maxRentCost) + ".");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
			else if(rentCost > maxRentCost) {
 				player.sendMessage(ChatColor.RED + "The price is too high.");
				// TODO The rent-price must be between minRentCost per minRentTime and maxRentCost per maxRentTime.
				// So we need to add time to the config. Orrrr interpret the min/max rent cost as "rent cost per hour"
				player.sendMessage(ChatColor.RED + "The rent-price must be between " + mgr.getEconomy().format(minRentCost) + " and " + mgr.getEconomy().format(maxRentCost) + ".");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			// check permission to rent out, for free
 			if(rentCost == 0) {
 				if(!player.hasPermission(Permission.RENTOUT_FREE)) {
 					player.sendMessage(ChatColor.RED + "You don't have permission to rent out regions for free.");
 					event.setCancelled(true);
 					sign.getBlock().breakNaturally();
 					return;
 				}
 			}
 			
 			if(rentCostOld != rentCost || rentTimeOld != rentTime) {
 				plot.setRentCost(rentCost, rentTime);
 			}
 			plotSign = new ForRentSign(plot, sign.getLocation().toVector().toBlockVector());
 			// no need to set extra data at this point
 			
 			mgr.messages.upForRent(player, 
 					region.getOwners().getPlayers(), 
 					region.getMembers().getPlayers(), 
					region.getId(), rentCost, new TimeStringConverter().convert(rentTime));
 		}
 		else if(type == PlotSignType.FOR_SALE) {
 			
 			// check permission to sell
 			if(!player.hasPermission(Permission.SELL)) {
 				player.sendMessage(ChatColor.RED + "You don't have permission to sell regions.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			// check permission to sell, unowned regions
 			if(!isOwner && !player.hasPermission(Permission.SELL_ANYREGION)) {
 				player.sendMessage(ChatColor.RED + "You can't sell regions that you don't own.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 
 			
 			// You can't sell a player's last region, 
 			// because players would be able to work together, to mess up your server
 			if(plotWorld.getConfig().isReserveFreeRegionsEnabled()) {
 				Set<String> owners = region.getOwners().getPlayers();
 				Set<String> homeless = mgr.getControl().getPotentialHomeless(world, owners);
 				if(!homeless.isEmpty()) {
 					String homelessString = "";
 					for (String string : homeless) {
 						homelessString += ", " + string;
 					}
 					homelessString = homelessString.substring(2);
 					player.sendMessage(ChatColor.RED + "Sorry, you can't sell this region. The following players would become homeless: " + homelessString);
 				}
 				
 				
 			}
 			
 			// check permission to sell, outside the region
 			if(!isInside && !player.hasPermission(Permission.SELL_ANYWHERE)) {
 				player.sendMessage(ChatColor.RED + "You can't place this sign outside the region itself.");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 
 			ForSaleSignText saleText = (ForSaleSignText) signText;
 			double sellCostOld = plot.getSellCost();
 			double sellCost = saleText.getSellCost();
 			signText.applyToSign(event.getLines());
 			
 			// check min/max cost
 			double minSellCost = plot.getWorth(config.getOnSellMinBlockCost());
 			double maxSellCost = plot.getWorth(config.getOnSellMaxBlockCost());
 			
 			if(sellCost < minSellCost) {
 				player.sendMessage(ChatColor.RED + "The price is too low.");
 				player.sendMessage(ChatColor.RED + "The sell-cost must be between " + mgr.getEconomy().format(minSellCost) + " and " + mgr.getEconomy().format(maxSellCost) + ".");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			else if(sellCost > maxSellCost) {
 				player.sendMessage(ChatColor.RED + "The price is high.");
 				player.sendMessage(ChatColor.RED + "The sell-cost must be between " + mgr.getEconomy().format(minSellCost) + " and " + mgr.getEconomy().format(maxSellCost) + ".");
 				event.setCancelled(true);
 				sign.getBlock().breakNaturally();
 				return;
 			}
 			
 			// check permission to sell, for free
 			if(sellCost == 0) {
 				if(!player.hasPermission(Permission.SELL_FREE)) {
 					player.sendMessage(ChatColor.RED + "You don't have permission to sell regions for free.");
 					event.setCancelled(true);
 					sign.getBlock().breakNaturally();
 					return;
 				}
 			}
 			
 			if(sellCostOld != sellCost) {
 				plot.setSellCost(sellCost);
 			}
 			plotSign = new ForSaleSign(plot, sign.getLocation().toVector().toBlockVector());
 			// no need to set extra data at this point
 			
 			mgr.messages.upForSale(player, 
 					region.getOwners().getPlayers(), 
 					region.getMembers().getPlayers(), 
 					region.getId(), sellCost);
 			
 		}
 		else {
 			player.sendMessage(ChatColor.RED + "Unknown sign type: \"" + type.name() + "\"");
 			return;
 		}
 		
 		// save new sign data
 		plot.setSign(plotSign);
 		plot.save();
 		
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(event.isCancelled()) {
 			return;// event was cancelled
 		}
 		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 			return;// didn't right-click (or left-click)
 			
 			// added left-click because 
 			// right-click doesn't work when you're standing 
 			// too close with a block in your hand.
 		}
 		if(!(event.getClickedBlock().getState() instanceof Sign)) {
 			return;// not a sign
 		}
 		Sign sign = (Sign) event.getClickedBlock().getState();
 		PlotSignType type = PlotSignType.getPlotSignType(sign.getLines());
 		if(type == null) {
 			return;// not a plot-sign
 		}
 		
 		// plot world
 		PlotWorld plotWorld = mgr.getPlotWorld(sign.getWorld().getName());
 		
 		// get plot information using the sign
 		Plot plot;
 		try {
 			plot = plotWorld.getPlot(sign);
 		} catch (SignException e) {
 			// not a valid sign
 			event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
 			event.getClickedBlock().breakNaturally();
 			return;
 		}
 		
 		if(plot == null) {
 			// didn't find the plot information
 			event.getPlayer().sendMessage(ChatColor.RED + "Sorry, this sign is invalid. Couldn't find the plot information.");
 			event.getClickedBlock().breakNaturally();
 			return;
 		}
 		
 		if(plot.getRegion() == null) {
 			// protected region doesn't exist
 			event.getPlayer().sendMessage(ChatColor.RED + "Sorry, region '" + plot.getRegionId() + "' doesn't exist anymore.");
 			event.getClickedBlock().breakNaturally();
 			plot.delete();
 			return;
 		}
 		
 
 		// send plot info
 		plot.sendInfo(event.getPlayer());
 		
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		onBlockProtect(event.getBlock(), event);
 	}
 	
 	@EventHandler
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		onBlockProtect(event.getBlock(), event);
 	}
 	
 	private void onBlockProtect(Block block, Cancellable event) {
 		onBlockProtect(block, event, null);
 	}
 	
 	private boolean areLocationsEqual(Block block1, Block block2) {
 		if(block1 == null || block2 == null) return false;
 		return Location.locToBlock(block1.getX()) == Location.locToBlock(block2.getX()) 
 				&& Location.locToBlock(block1.getZ()) == Location.locToBlock(block2.getZ())
 				&& Location.locToBlock(block1.getY()) == Location.locToBlock(block2.getY());
 	}
 	
 	private void onBlockProtect(Block block, Cancellable event, Block originalBlock) {
 		if(event.isCancelled()) {
 			return;// event cancelled
 		}
 		if(!(block.getState() instanceof Sign)) {
 			if(originalBlock == null) {
 				// check if there's a sign attached to this block
 				onBlockProtect(block.getRelative(BlockFace.UP), event, block);
 				onBlockProtect(block.getRelative(BlockFace.EAST), event, block);
 				onBlockProtect(block.getRelative(BlockFace.SOUTH), event, block);
 				onBlockProtect(block.getRelative(BlockFace.WEST), event, block);
 				onBlockProtect(block.getRelative(BlockFace.NORTH), event, block);
 			}
 			return;// not a sign
 		}
 		Sign sign = (Sign) block.getState();
 		Block attached = block.getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
 		if(originalBlock != null && !areLocationsEqual(attached, originalBlock)) {
 			// broke a block next to a sign.
 			// but the sign was not attached to it
 			return;
 		}
 		// broke a sign. or a block with a sign attached to it.
 		
 		PlotSignType type = PlotSignType.getPlotSignType(sign.getLines());
 		if(type == null) {
 			return;// not a plot-sign
 		}
 		
 		String regionId = PlotSignText.getRegionId(sign.getLines());
 		
 		PlotWorld plotWorld = mgr.getPlotWorld(sign.getWorld().getName());
 		
 		Plot plot = plotWorld.getPlot(regionId);
 		
 		if(plot == null) {
 			// not a saved sign, let it break
 			return;
 		}
 		
 		ProtectedRegion region = plot.getRegion();
 		if(region == null) {
 			// region doesn't exist anymore
 			// let it break
 			return;
 		}
 		
 		BlockVector coords = sign.getLocation().toVector().toBlockVector();
 		IPlotSignData plotSign = plot.getSign(coords);
 		if(plotSign == null) {
 			// sign data doesn't exist
 			// let it break
 			return;
 		}
 		type = plotSign.getType();
 		
 		if(event instanceof BlockBreakEvent) {
 			BlockBreakEvent e = (BlockBreakEvent) event;
 			Player player = e.getPlayer();
 			
 			// check region ownership || permission break-any
 			boolean isOwner = region.isOwner(player.getName());
 			if(!isOwner && !player.hasPermission(Permission.BREAK_ANY_SIGN)) {
 				// not an owner, and no special permission
 				player.sendMessage(ChatColor.RED + "You don't own this region.");
 				// protect the sign
 				event.setCancelled(true);
 				return;
 			}
 			else {
 				
 				plot.removeSign(coords);
 				plot.save();
 				Collection<IPlotSignData> signs = plot.getSigns(type);
 				if(signs == null || signs.isEmpty()) {
 					player.sendMessage(ChatColor.GREEN + "You broke the last " + type.name() + " sign of region \"" + plot.getRegionId() + "\".");
 				}
 				else {
 					player.sendMessage(ChatColor.GREEN + "You broke a " + type.name() + " sign of region \"" +  plot.getRegionId() + "\". There are " + signs.size() + " " + type.name() + " signs left.");
 				}
 			}
 		}
 		else {
 			// protect the sign
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event) {
 		Iterator<Block> blocks = event.blockList().iterator();
 		while(blocks.hasNext()) {
 			Block block = blocks.next();
 			onBlockProtect(block, event);
 			if(event.isCancelled()) {
 				break;
 			}
 		}
 	}
 
 }
