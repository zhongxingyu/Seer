 /*
 Copyright (c) 2012 Wolf480pl (wolf480@interia.pl)
 
 This software is provided 'as-is', without any express or implied
 warranty. In no event will the authors be held liable for any damages
 arising from the use of this software.
 
 Permission is granted to anyone to use this software for any purpose,
 including commercial applications, and to alter it and redistribute it
 freely, subject to the following restrictions:
 
 1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
 
 2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
 
 3. This notice may not be removed or altered from any source
 distribution.
  */
 
 package com.github.wolf480pl.PaidSwitch;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 //import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityInteractEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.vehicle.VehicleMoveEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PaidSwitch extends JavaPlugin implements Listener {
 	private Logger log;
 	private Economy eco;
 	public void onEnable(){
 		log = getLogger();
 		Payment.log = log;
 		getServer().getPluginManager().registerEvents(this, this);
 		if(SetupEco()) {
 			log.info("Vault economy found.");
 		} else {
 			log.info("Vault economy not found yet.");
 		}
 		parseConfig();
 	}
 	public void onDisable(){
 	}
 	
 	private void parseConfig() {
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		String lvl = getConfig().getString("log-level");
 		Level loglvl;
 		try {
 			loglvl = Level.parse(lvl);
 		} catch (Exception e) {
 			e.printStackTrace();
 			loglvl = Level.INFO;
 		}
 		log.setLevel(loglvl);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("paidswitch")){
 			if(args.length > 0){
 				reloadConfig();
 				parseConfig();
 				sender.sendMessage("[PaidSwitch] Config reloaded.");
 			} else
 				sender.sendMessage(cmd.getUsage().split("\n"));
 			return true;
 		}
 		return false;
 	}
 	@EventHandler
 	public void onEntityInteract(EntityInteractEvent event){
 		try{
 			if(event.isCancelled()) return;
 //			String msg = event.getEntity().getType().name() + " uzyl  " + event.getBlock().getType().name() + " !";
 //			getServer().broadcastMessage(msg);
 			if(isSwitch(event.getBlock(), true, true)){
 				Payment paid = findSign(event.getBlock());
 				if((paid != null) && paid.isValid()){
 					if(event.getEntity() instanceof Vehicle && event.getEntity().getPassenger() !=  null && event.getEntity().getPassenger() instanceof Player && getConfig().getBoolean("vehicle-support"))
 						event.setCancelled(!processPayment(paid, (Player) event.getEntity().getPassenger()));
 					else
 						event.setCancelled(true);
 				}
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getClickedBlock() == null) return;
 		if(event.isCancelled()) return;
 //		String msg = event.getPlayer().getName() + " uzyl  " + event.getAction().name() + " na " + event.getClickedBlock().getType().name() + " !";
 //		getServer().broadcastMessage(msg);
 		if(isInteractSwitch(event.getClickedBlock())){
 			Payment paid = findSign(event.getClickedBlock());
 			if(event.getAction() == Action.PHYSICAL && paid != null && paid.isValid() && event.getPlayer().getVehicle() != null){
 				event.setCancelled(true);
 				return;
 			}
 			event.setCancelled(!processPayment(paid, event.getPlayer()));
 		}
 	}
 	@EventHandler
 	public void onSignChange(SignChangeEvent event){
 		if (event.isCancelled()) return;
 		if(!event.getLine(0).equalsIgnoreCase("[PaidSw]")){
 //			getServer().broadcastMessage(String.format("%s doesn't match [PaidSw]",event.getLine(0)));
 			return;
 		}
 		log.fine("Create [PaidSw]");
 		if(!event.getPlayer().hasPermission("paidswitch.create")){
 			log.fine("NoPerm");
 			event.getPlayer().sendMessage(getConfig().getString("messages.create-noperm").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 			event.setCancelled(true);
 			getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 			event.getBlock().breakNaturally();
 			return;
 		}
 		Block sw = findSwitch(event.getBlock(), true, true);
 		if(sw == null){
 			event.getPlayer().sendMessage(getConfig().getString("messages.create-noswitch").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 			log.fine("NoSwitch\n");
 			event.setCancelled(true);
 			getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 			event.getBlock().breakNaturally();
 			return;
 		}
 		log.finer(sw.toString());
 		if (!canCreate(event.getPlayer(), sw.getType())) {
 			log.fine("NoPerm-Type");
 			event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-noperm-type"), sw.getType().toString()).replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 			event.setCancelled(true);
 			getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 			event.getBlock().breakNaturally();
 			return;
 		}
 		Payment pay = findSign(sw);
 		log.finer(String.valueOf(pay));
 		if(pay != null && pay.isValid(eco)) {
 			log.fine("Another payment");
 			log.finer(pay.toString());
 			if(!pay.Account.equals(event.getPlayer().getName())) {
 				log.fine("Different name");
 				if(!event.getPlayer().hasPermission("paidswitch.create.duplicate")){
 					event.getPlayer().sendMessage(getConfig().getString("messages.create-duplicate").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 					log.fine("Duplicate NoPerm");
 					event.setCancelled(true);
 					getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 					event.getBlock().breakNaturally();
 					return;
 				}
 			}
 		}
 		if(!event.getLine(1).isEmpty()){
 			log.fine("Specified Account");
 			log.finer(event.getLine(1));
 			if(!event.getPlayer().hasPermission("paidswitch.create.others")){
 				event.getPlayer().sendMessage(getConfig().getString("messages.create-others").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 				log.fine("Others Noperm");
 				event.setCancelled(true);
 				getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 				event.getBlock().breakNaturally();
 				return;					
 			}
 			boolean bank = false;
 			if (event.getLine(1).length() >= 2) {
 				bank = event.getLine(1).substring(0, 2).equalsIgnoreCase("b:");
 			}
 			if (bank) {
 				log.fine("bank");
 				try {
 					eco.getBanks();
 				} catch (UnsupportedOperationException e) {
 					event.getPlayer().sendMessage(getConfig().getString("messages.create-nobank").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 					log.fine("Bank unsupported");
 					bank = false;
 					event.setLine(1, event.getLine(1).substring(2));
 				}
 			}
 			if(!event.getLine(1).equalsIgnoreCase("none") && !(bank ? eco.getBanks().contains(event.getLine(1).substring(2)) : eco.hasAccount(event.getLine(1)) )){
 				event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-noaccount"),( bank ? event.getLine(1).substring(2) + " (bank)" : event.getLine(1) + " (player)") ).replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 				log.fine("NoAccount");
 				event.setCancelled(true);
 				getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 				event.getBlock().breakNaturally();
 				return;										
 			}
 		} else {
 			event.setLine(1, event.getPlayer().getName());
 			log.fine("Setting player name");
 		}
 		log.finer(event.getLine(2));
 		try{
 			double price = Double.parseDouble(event.getLine(2));
 			if (!checkLimit(event.getPlayer(), price)) {
 				event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-exceeded-price"), eco.format(getLimit(event.getPlayer()))).replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 				log.fine("PriceLimit");
 				event.setCancelled(true);
 				getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(), event.getPlayer()));
 				event.getBlock().breakNaturally();
 				return;
 			}
 		} catch (NumberFormatException ex) {
 			event.getPlayer().sendMessage(String.format(getConfig().getString("messages.create-noprice"),(event.getLine(2))).replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 			log.fine("Noprice");
 			event.setCancelled(true);
 			getServer().getPluginManager().callEvent(new BlockBreakEvent(event.getBlock(),event.getPlayer()));
 			event.getBlock().breakNaturally();
 			return;
 		}
 		event.getPlayer().sendMessage(getConfig().getString("messages.create-ok").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 		log.fine("Created ok");
 	}
 	@EventHandler()
 	public void onVehicleMove(VehicleMoveEvent event){
 		if(!getConfig().getBoolean("detector-rail")) return;
 		if(event.getVehicle().getType() != EntityType.MINECART) return;
 		List<MetadataValue> data = event.getTo().getBlock().getMetadata("paidswitch.allow");
 		if(!data.isEmpty() && data.get(0).asBoolean()) return;
 		if(isRailSwitch(event.getFrom().getBlock())){
 			Payment payment = findSign(event.getFrom().getBlock());
 			if(payment != null && payment.isValid()){
 				event.getFrom().getBlock().setMetadata("paidswitch.allow", new FixedMetadataValue(this, false));
 			}
 		}
 		if(isRailSwitch(event.getTo().getBlock())){
 			Entity passanger = event.getVehicle().getPassenger();
 			if(passanger == null || !(passanger instanceof Player)) {
 				event.getTo().getBlock().setMetadata("paidswitch.allow", new FixedMetadataValue(this, false));
 			} else {
 				if(data.isEmpty() || (!data.get(0).asBoolean())){
 					Payment payment = findSign(event.getTo().getBlock());
 					boolean can = processPayment(payment, (Player)passanger);
 					event.getTo().getBlock().setMetadata("paidswitch.allow", new FixedMetadataValue(this, can));
 				}
 			}
 //			getServer().broadcastMessage(((passanger != null) ? passanger.getType().getName() + " in " : "") + event.getVehicle().getType().getName() + " moved onto " + event.getTo().getBlock().getType().name());
 		}
 	}
 	@EventHandler()
 	public void onRedstoneChange(BlockRedstoneEvent event){
 		if(!getConfig().getBoolean("detector-rail")) return;
 		if(!isRailSwitch(event.getBlock())) return;
 		List<MetadataValue> data = event.getBlock().getMetadata("paidswitch.allow");
 //		log.info(String.valueOf(event.getNewCurrent()) + " | " + data.toString());
 		Payment payment = findSign(event.getBlock());
 		if(data.isEmpty() || (payment != null && payment.isValid() && (!data.get(0).asBoolean())))
 			event.setNewCurrent(0);
 		
 	}
 	private boolean processPayment(Payment paid, Player player){
 		if((paid != null) && paid.isValid()){
 			if(!player.hasPermission("paidswitch.use")){
 				player.sendMessage(getConfig().getString("messages.use-noperm").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 				return false;
 			}
 //			getServer().broadcastMessage(paid.Amount + " for " + paid.Account);
 			if(player.hasPermission("paidswitch.use.free")){
 				player.sendMessage(getConfig().getString("messages.use-free").replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 				if(getConfig().getBoolean("earn-for-free")) {
 					if(eco == null && !SetupEco()) {
 						log.log(Level.SEVERE,"No economy plugin found!");
 					} else {
 						paid.execute(eco);
 						notifyOwner(paid, player, false);
 					}
 				} else {
 					notifyOwner(paid, player, true);
 				}
 				return true;
 			}
 			if((eco == null) && !SetupEco()){
 				log.log(Level.SEVERE,"No economy plugin found!");
 				if(!player.getName().equalsIgnoreCase(paid.Account))
 					return false;
 			} else {
 				if(eco.has(player.getName(), paid.Amount)){
 					EconomyResponse response = eco.withdrawPlayer(player.getName(),paid.Amount);
 					paid.execute(eco);
 					player.sendMessage(String.format(getConfig().getString("messages.use-paid"),eco.format(paid.Amount),eco.format(response.balance)).replaceAll("/n", "\n").replaceAll("&([0-9a-fA-F])", "\u00A7$1").split("\n"));
 					notifyOwner(paid, player, false);
 				} else {
 					player.sendMessage(String.format(getConfig().getString("messages.use-need"),eco.format(paid.Amount)).replaceAll("&([0-9a-fA-F])", "\u00A7$1"));
 					return false;
 				}
 			}
 		}
 		return true;
 
 	}
 	private Payment findSign(Block block){
 		Payment paid = checkSign(block, BlockFace.UP);
 		if(paid == null) paid = checkSign(block, BlockFace.NORTH);
 		if(paid == null) paid = checkSign(block, BlockFace.EAST);
 		if(paid == null) paid = checkSign(block, BlockFace.SOUTH);
 		if(paid == null) paid = checkSign(block, BlockFace.WEST);
 		if(paid == null) paid = checkSign(block, BlockFace.DOWN);
 //		log.info(paid.toString());
 		return paid;
 	}
 	
 	private Payment checkSign(Block block, BlockFace face){
 		BlockState bl = block.getRelative(face).getState();
 		if(bl instanceof Sign){
 			log.fine("Sign found at " + face.name());
 			log.finer(((Sign) bl).getLine(0));
 			if(((Sign) bl).getLine(0).equalsIgnoreCase("[PaidSw]")) {
 				log.fine("Good sign");
 				return new Payment(((Sign) bl).getLine(1),((Sign) bl).getLine(2), ((Sign) bl).getLine(3), block.getType());
 			}
 		}
 		log.finer("No good at " + face.name());
 		return null;
 	}
 	private boolean SetupEco(){
 		RegisteredServiceProvider<Economy> ecoProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if( ecoProvider != null )
 			eco = ecoProvider.getProvider();
 		return (eco != null);
 	}
 	private boolean isSwitch(Block block, boolean interact, boolean rail){
 		return (interact && isInteractSwitch(block)) ||
 				(rail && isRailSwitch(block));
 	}
 	private boolean isInteractSwitch(Block block) {
 		return (block.getType().equals(Material.WOOD_PLATE) && getConfig().getBoolean("allowed-switches.wood-plate")) ||
 				(block.getType().equals(Material.STONE_PLATE) && getConfig().getBoolean("allowed-switches.stone-plate")) ||
 				(block.getType().equals(Material.STONE_BUTTON) && getConfig().getBoolean("allowed-switches.button")) ||
 				(block.getType().equals(Material.LEVER)  && getConfig().getBoolean("allowed-switches.lever"));
 	}
 	private boolean isRailSwitch(Block  block){
 		return ((block.getType() == Material.DETECTOR_RAIL) && getConfig().getBoolean("allowed-switches.detector-rail"));
 	}
 	private Block findSwitch(Block block, boolean interact, boolean rail){
 		if(isSwitch(block.getRelative(BlockFace.DOWN), interact, rail)) return block.getRelative(BlockFace.DOWN);  
 		if(isSwitch(block.getRelative(BlockFace.SOUTH), interact, rail)) return block.getRelative(BlockFace.SOUTH); 
 		if(isSwitch(block.getRelative(BlockFace.WEST), interact, rail)) return block.getRelative(BlockFace.WEST); 
 		if(isSwitch(block.getRelative(BlockFace.NORTH), interact, rail)) return block.getRelative(BlockFace.NORTH);
 		if(isSwitch(block.getRelative(BlockFace.EAST), interact, rail)) return block.getRelative(BlockFace.EAST);
 		if(isSwitch(block.getRelative(BlockFace.UP), interact, rail)) return block.getRelative(BlockFace.UP);
 		return null;
 	}
 	private boolean canCreate(Player player, Material type) {
 		if (!getConfig().getBoolean("switch-type-permission-needed")) {
 			return true;
 		}
 		if (type.equals(Material.STONE_PLATE)) {
 			return player.hasPermission("paidswitch.create.type.stone-plate");
 		} else if (type.equals(Material.WOOD_PLATE)) {
 			return player.hasPermission("paidswitch.create.type.wood-plate");
 		} else if (type.equals(Material.STONE_BUTTON)) {
 			return player.hasPermission("paidswitch.create.type.button");
 		} else if (type.equals(Material.LEVER)) {
 			return player.hasPermission("paidswitch.create.type.lever");
 		} else if (type.equals(Material.DETECTOR_RAIL)) {
 			return player.hasPermission("paidswitch.create.type.detector-rail");
 		}
 		return false;
 	}
 	
 	// 0 means unlimited
 	private double getLimit(Player player) {
 		ConfigurationSection priceLimits = getConfig().getConfigurationSection("limits.price");
 		Set<String> limits = priceLimits.getKeys(false);
 		Iterator<String> i = limits.iterator();
 		if (player.hasPermission("paidswitch.limit.price.none")) {
 			return 0;
 		}
 		while (i.hasNext()) {
 			String name = i.next();
 			if (player.hasPermission("paidswitch.limit.price." + name)) {
 				return priceLimits.getDouble(name);
 			}
 		}
 		return priceLimits.getDouble("default");
 	}
 	
 	private boolean checkLimit(Player player, double price) {
 		double limit = getLimit(player); 
 		return (limit == 0) || (limit >= price);
 	}
 	
 	private void notifyOwner(Payment pay, Player user, boolean free) {
		if (pay.bank || pay.none) return;
 		if (!getConfig().getBoolean("notify-owner" + (free ? "-free" : ""))) {
 			return;
 		}
 		Player owner = getServer().getPlayer(pay.Account);
 		if (owner == null) {
 			return;
 		}
 		owner.sendMessage(String.format(getConfig().getString("messages.use-notify"), user.getName(), getConfig().getString("messages.switches." + pay.type.toString()), pay.description, free ? getConfig().getString("messages.use-notify-free") : eco.format(pay.Amount)).replaceAll("/n", "\n").replaceAll("&([0-9a-fA-F])", "\u00A7$1").split("\n"));
 	}
 }
