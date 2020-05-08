 package com.vildaberper.DefaultCommands.Listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import com.vildaberper.DefaultCommands.L;
 import com.vildaberper.DefaultCommands.Misc;
 import com.vildaberper.DefaultCommands.Perm;
 import com.vildaberper.DefaultCommands.Util;
 import com.vildaberper.DefaultCommands.V;
 import com.vildaberper.DefaultCommands.Class.DCAfkPlayer;
 import com.vildaberper.DefaultCommands.Class.DCPortal;
 import com.vildaberper.DefaultCommands.Runnable.BedCheck;
 
 public class DCPlayerListener extends PlayerListener{
 	@Override
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(!Perm.hasPermissionSilent(event.getPlayer(), "dc.do." + event.getPlayer().getWorld().getName())){
 			event.setCancelled(true);
 			return;
 		}
 		if(event.getRightClicked().getPassenger() != null && event.getRightClicked().getPassenger().equals(event.getPlayer())){
 			event.setCancelled(true);
 			event.getRightClicked().eject();
 			return;
 		}
 		if(Misc.isRideAble(event.getRightClicked())){
 			if(Perm.hasPermissionSilent(event.getPlayer(), "dc.mount.animal." + event.getPlayer().getWorld().getName())){
 				if(event.getPlayer().getItemInHand().getType().equals(Material.SADDLE) && Misc.isAnimal(event.getRightClicked())){
 					if(!(event.getRightClicked() instanceof Wolf) || event.getRightClicked() instanceof Wolf && ((Wolf) event.getRightClicked()).isTamed() && ((CraftPlayer) ((Wolf) event.getRightClicked()).getOwner()).getName().equals(event.getPlayer().getName())){
 						event.setCancelled(true);
 						event.getRightClicked().setPassenger(event.getPlayer());
 						return;
 					}
 				}
 			}
 			if(Perm.hasPermissionSilent(event.getPlayer(), "dc.mount.monster." + event.getPlayer().getWorld().getName())){
 				if(event.getPlayer().getItemInHand().getType().equals(Material.SADDLE) && Misc.isMonster(event.getRightClicked()) && (!(event.getRightClicked() instanceof Wolf) || !((Wolf) event.getRightClicked()).isTamed())){
 					event.setCancelled(true);
 					event.getRightClicked().setPassenger(event.getPlayer());
 					return;
 				}
 			}
 			if(Perm.hasPermissionSilent(event.getPlayer(), "dc.mount.player." + event.getPlayer().getWorld().getName())){
 				if(event.getPlayer().getItemInHand().getType().equals(Material.SADDLE) && event.getRightClicked() instanceof Player){
 					event.setCancelled(true);
 					event.getRightClicked().setPassenger(event.getPlayer());
 					return;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerBedEnter(PlayerBedEnterEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(!Perm.hasPermissionSilent(event.getPlayer(), "dc.do." + event.getPlayer().getWorld().getName())){
 			event.setCancelled(true);
 			return;
 		}
 		V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(V.plugin, new BedCheck(), 80);
 	}
 
 	@Override
 	public void onPlayerMove(final PlayerMoveEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(Misc.isFreeze(event.getPlayer().getName())){
 			event.getPlayer().teleport(event.getFrom());
 			event.setCancelled(true);
 		}
 		if(Misc.isAfk(event.getPlayer().getEntityId()) && (Util.getDistanceIgnoreY(event.getFrom(), event.getTo()) >= V.afk_move_min || Misc.getAfkPlayer(event.getPlayer().getEntityId()).getLocation().getYaw() != event.getPlayer().getLocation().getYaw() || Misc.getAfkPlayer(event.getPlayer().getEntityId()).getLocation().getPitch() != event.getPlayer().getLocation().getPitch()) && Perm.hasPermissionSilent(event.getPlayer(), "dc.afk.self")){
 			Misc.setAfk(event.getPlayer().getEntityId(), false);
 			Misc.getAfkPlayer(event.getPlayer().getEntityId()).resetTime();
 			L.log(Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 			Misc.sendMessage(event.getPlayer(), Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 		}
 		if(!event.getPlayer().isInsideVehicle() && event.getFrom().getBlock() != event.getTo().getBlock()){
 			final DCPortal dcportal = Misc.getPortal(event.getTo().getBlock().getLocation());
 
 			if(dcportal != null && dcportal.getTarget(event.getPlayer()) != null){
 				final Player player = event.getPlayer();
 
 				event.setCancelled(true);
 				V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(
 						V.plugin,
 						new Runnable(){
 							@Override
 							public void run(){
 								boolean god = Misc.isGod(player.getEntityId());
 
 								if(!god){
 									Misc.setGod(player.getEntityId(), true);
 								}
 								player.teleport(dcportal.getTarget(event.getPlayer()));
 								if(!god){
 									V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(
 											V.plugin,
 											new Runnable(){
 												@Override
 												public void run(){
 													Misc.setGod(player.getEntityId(), false);
 												}
 											},
 											20
 									);
 								}
 							}
 						}
 				);
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		if(Misc.getConfig(event.getPlayer()).getBoolean("home_on_death") && Misc.getHome(event.getPlayer()) != null){
 			event.setRespawnLocation(Util.getSafeLocationAt(Misc.getHome(event.getPlayer()).getLocation()));
 		}else{
 			event.setRespawnLocation(Util.getSafeLocationAt(Misc.getConfig(event.getPlayer()).getSpawn()));
 		}
 		if(!event.getPlayer().getWorld().equals(event.getRespawnLocation().getWorld())){
 			Misc.setInventory(event.getPlayer().getName(), event.getPlayer().getInventory().getContents(), event.getPlayer().getWorld().getName());
 			if(Misc.getInventory(event.getPlayer().getName(), event.getRespawnLocation().getWorld().getName()) != null){
 				event.getPlayer().getInventory().setContents(Util.convertItemStack(Misc.getInventory(event.getPlayer().getName(), event.getRespawnLocation().getWorld().getName()).getContents()));
 			}else{
 				event.getPlayer().getInventory().clear();
 			}
 			Misc.setArmor(event.getPlayer().getName(), event.getPlayer().getInventory().getArmorContents(), event.getPlayer().getWorld().getName());
 			if(Misc.getArmor(event.getPlayer().getName(), event.getRespawnLocation().getWorld().getName()) != null){
 				event.getPlayer().getInventory().setArmorContents(Misc.getArmor(event.getPlayer().getName(), event.getRespawnLocation().getWorld().getName()).getArmor());
 			}else{
 				event.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
 			}
 			Misc.broadcastJoin(event.getPlayer(), event.getRespawnLocation().getWorld().getName(), false);
 		}
 		Misc.updateHide();
 	}
 
 	@Override
 	public void onPlayerPickupItem(PlayerPickupItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
		if(!Perm.hasPermissionSilent(event.getPlayer(), "dc.do." + event.getPlayer().getWorld().getName())){
			event.setCancelled(true);
			return;
		}
		if(V.nopickups.contains(event.getItem())){
			event.setCancelled(true);
			return;
		}
		if(Misc.isNopickup(event.getPlayer().getEntityId())){
 			event.setCancelled(true);
 			return;
 		}
 		if(Misc.getConfig(event.getPlayer()).getBoolean("creative") || Misc.isCreative(event.getPlayer().getEntityId())){
 			event.setCancelled(true);
 			Util.addIfNotInInventory(event.getPlayer(), event.getItem().getItemStack());
 			event.getItem().remove();
 		}
 	}
 
 	@Override
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(Misc.getConfig(event.getPlayer()).getBoolean("creative") || Misc.isCreative(event.getPlayer().getEntityId())){
 			event.getItemDrop().remove();
 		}
 	}
 
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getAction().equals(Action.LEFT_CLICK_BLOCK) && Util.isLeftClickInteractAble(event.getClickedBlock()) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Util.isRightClickInteractAble(event.getClickedBlock())){
 			if(!Perm.hasPermissionSilent(event.getPlayer(), "dc.do." + event.getPlayer().getWorld().getName())){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		if(Misc.isFly(event.getPlayer().getEntityId()) && event.getPlayer().getItemInHand().getType().equals(Material.FEATHER) && !Util.isLeftClickInteractAble(event.getClickedBlock()) && !Util.isRightClickInteractAble(event.getClickedBlock())){
 			if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 				event.getPlayer().setVelocity(
 						new Vector(
 								event.getPlayer().getLocation().getDirection().getX() * 2,
 								0.75,
 								event.getPlayer().getLocation().getDirection().getZ() * 2
 						)
 				);
 				return;
 			}else if(event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
 				event.getPlayer().setVelocity(
 						new Vector(
 								event.getPlayer().getLocation().getDirection().getX() * 5,
 								event.getPlayer().getLocation().getDirection().getY() * 3.5 + 1,
 								event.getPlayer().getLocation().getDirection().getZ() * 5
 						)
 				);
 				return;
 			}
 		}
 		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType().equals(Material.FLINT_AND_STEEL) && !Util.isRightClickInteractAble(event.getClickedBlock()) && !Perm.hasPermissionSilent(event.getPlayer(), "dc.flint_and_steel." + event.getPlayer().getWorld().getName())){
 			if(Misc.getConfig(event.getPlayer()).getBoolean("block_fire_flint_and_steel")){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		if(event.getPlayer().getItemInHand().getTypeId() == V.selection_tool && Perm.hasPermissionSilent(event.getPlayer(), "dc.select")){
 			if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
 				event.setCancelled(true);
 				Misc.setSelection(event.getPlayer().getName(), null, event.getClickedBlock());
 				event.getPlayer().sendMessage("Block 2 marked.");
 			}else if(event.getAction().equals(Action.LEFT_CLICK_BLOCK)){
 				event.setCancelled(true);
 				Misc.setSelection(event.getPlayer().getName(), event.getClickedBlock(), null);
 				event.getPlayer().sendMessage("Block 1 marked.");
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerQuit(PlayerQuitEvent event){
 		event.setQuitMessage(null);
 		Misc.setInventory(event.getPlayer().getName(), event.getPlayer().getInventory().getContents(), event.getPlayer().getWorld().getName());
 		Misc.setAfkPlayer(event.getPlayer().getEntityId(), null);
 		Misc.broadcastDisconnect(event.getPlayer(), false);
 	}
 
 	@Override
 	public void onPlayerLogin(PlayerLoginEvent event){
 		if(V.whitelist && V.whitelist_kick && !Misc.isWhitelist(event.getPlayer().getName())){
 			event.disallow(Result.KICK_WHITELIST, Misc.getColoredString("whitelist"));
 		}
 		if(Misc.getBan(event.getPlayer().getName()) != null){
 			event.disallow(Result.KICK_BANNED, Util.replaceColor(Misc.getBan(event.getPlayer().getName()).getMessage()));
 		}
 	}
 
 	@Override
 	public void onPlayerJoin(PlayerJoinEvent event){
 		event.setJoinMessage(null);
 		if(Misc.getInventory(event.getPlayer().getName(), event.getPlayer().getWorld().getName()) != null){
 			event.getPlayer().getInventory().setContents(Util.convertItemStack(Misc.getInventory(event.getPlayer().getName(), event.getPlayer().getWorld().getName()).getContents()));
 		}
 		if(Misc.getArmor(event.getPlayer().getName(), event.getPlayer().getWorld().getName()) != null){
 			event.getPlayer().getInventory().setArmorContents(Misc.getArmor(event.getPlayer().getName(), event.getPlayer().getWorld().getName()).getArmor());
 		}
 		Misc.setAfkPlayer(event.getPlayer().getEntityId(), new DCAfkPlayer(event.getPlayer().getEntityId(), event.getPlayer().getLocation()));
 		Misc.broadcastConnect(event.getPlayer(), false);
 		Misc.updateHide();
 		if(Perm.hasPermissionSilent(event.getPlayer(), "dc.motd")){
 			event.getPlayer().chat("/dcmotd");
 		}
 		if(V.whitelist && !Misc.isWhitelist(event.getPlayer().getName())){
 			Misc.sendString(event.getPlayer(), "whitelist");
 		}
 	}
 
 	@Override
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
 			Misc.setInventory(event.getPlayer().getName(), event.getPlayer().getInventory().getContents(), event.getFrom().getWorld().getName());
 			if(Misc.getInventory(event.getPlayer().getName(), event.getTo().getWorld().getName()) != null){
 				event.getPlayer().getInventory().setContents(Util.convertItemStack(Misc.getInventory(event.getPlayer().getName(), event.getTo().getWorld().getName()).getContents()));
 			}else{
 				event.getPlayer().getInventory().clear();
 			}
 			Misc.setArmor(event.getPlayer().getName(), event.getPlayer().getInventory().getArmorContents(), event.getFrom().getWorld().getName());
 			if(Misc.getArmor(event.getPlayer().getName(), event.getTo().getWorld().getName()) != null){
 				event.getPlayer().getInventory().setArmorContents(Misc.getArmor(event.getPlayer().getName(), event.getTo().getWorld().getName()).getArmor());
 			}else{
 				event.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
 			}
 			Misc.broadcastJoin(event.getPlayer(), event.getTo().getWorld().getName(), false);
 		}
 		Misc.updateHide();
 		V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(
 				V.plugin,
 				new Runnable(){
 					@Override
 					public void run(){
 						Misc.updateHide();
 					}
 				},
 				2
 		);
 		if(V.show_teleport_smoke){
 			for(Player player : event.getFrom().getWorld().getPlayers()){
 				if(Misc.getPlayers(player, V.all).contains(event.getPlayer())){
 					player.playEffect(event.getFrom(), Effect.SMOKE, 4);
 				}
 			}
 			for(Player player : event.getTo().getWorld().getPlayers()){
 				if(Misc.getPlayers(player, V.all).contains(event.getPlayer())){
 					player.playEffect(event.getTo(), Effect.SMOKE, 4);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(Misc.isAfk(event.getPlayer().getEntityId()) && Perm.hasPermissionSilent(event.getPlayer(), "dc.afk.self")){
 			Misc.setAfk(event.getPlayer().getEntityId(), false);
 			Misc.getAfkPlayer(event.getPlayer().getEntityId()).resetTime();
 			L.log(Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 			Misc.sendMessage(event.getPlayer(), Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 		}
 		if(!event.isCancelled()){
 			String command = "", newm = "";
 			boolean lastcommand = true;
 
 			for(String s : Misc.getAlias("dcrepeat")){
 				if(event.getMessage().substring(1).split(" ")[0].equalsIgnoreCase(s)){
 					lastcommand = false;
 				}
 			}
 			if(event.getMessage().substring(1).split(" ")[0].equalsIgnoreCase("dcrepeat")){
 				lastcommand = false;
 			}
 			for(int i = event.getMessage().substring(1).split(" ").length - 1; i >= 0; i--){
 				command = "";
 				newm = "";
 				for(int u = i + 1; u < event.getMessage().substring(1).split(" ").length; u++){
 					newm += " " + event.getMessage().substring(1).split(" ")[u];
 				}
 				for(int u = 0; u <= i; u++){
 					if(u != 0){
 						command += " ";
 					}
 					command += event.getMessage().substring(1).split(" ")[u];
 				}
 				if(Misc.getCommand(command) != null){
 					for(String c : Misc.getCommand(command).replace("<player>", event.getPlayer().getName()).split(";")){
 						if(lastcommand){
 							Misc.setLastCommand(event.getPlayer().getName(), c + newm);
 						}
 						event.getPlayer().chat("/" + c + newm);
 					}
 					event.setCancelled(true);
 					return;
 				}
 				if(lastcommand){
 					Misc.setLastCommand(event.getPlayer().getName(), event.getMessage().substring(1));
 				}
 			}
 			if(V.unknown_command && V.plugin.getServer().getPluginCommand(event.getMessage().split(" ")[0].substring(1)) == null){
 				if(
 						":reload:plugins:help:?:kick:ban:pardon:ban-ip:pardon-ip:op:deop:tp:give:tell:stop:save-all:save-off:save-on:list:say:time:".indexOf(":" + event.getMessage().split(" ")[0].substring(1) + ":") == -1
 						&& (V.plugin.getServer().getPluginManager().getPlugin("Towny") == null || V.plugin.getServer().getPluginManager().getPlugin("Towny").isEnabled() && ":tc:nc:resident:town:plot:nation:towny:townyadmin:".indexOf(":" + event.getMessage().split(" ")[0].substring(1) + ":") == -1)
 				){
 					Misc.sendMessage(event.getPlayer(), Misc.getColoredString("unknown_command"));
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onPlayerChat(PlayerChatEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		if(Misc.isAfk(event.getPlayer().getEntityId()) && Perm.hasPermissionSilent(event.getPlayer(), "dc.afk.self")){
 			Misc.setAfk(event.getPlayer().getEntityId(), false);
 			Misc.getAfkPlayer(event.getPlayer().getEntityId()).resetTime();
 			L.log(Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 			Misc.sendMessage(event.getPlayer(), Misc.getSenderCmdMsg("c_afk", event.getPlayer(), Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()), Misc.isAfk(Misc.getPlayers(event.getPlayer(), event.getPlayer().getName()).get(0).getEntityId())));
 		}
 		if(event.isCancelled()){
 			return;
 		}
 		if(Misc.isMute(event.getPlayer().getName())){
 			Misc.sendString(event.getPlayer(), "muted");
 			event.setCancelled(true);
 			return;
 		}
 		if(V.better_chat){
 			String hp = "";
 
 			for(int i = 0; i < event.getPlayer().getHealth(); i++){
 				hp += "|";
 			}
 			hp += ChatColor.DARK_GRAY;
 			for(int i = 0; i < 20 - event.getPlayer().getHealth(); i++){
 				hp += "|";
 			}
 			if(event.getPlayer().getHealth() > 10){
 				hp = ChatColor.GREEN + hp;
 			}else if(event.getPlayer().getHealth() <= 10 && event.getPlayer().getHealth() > 5){
 				hp = ChatColor.GOLD + hp;
 			}else if(event.getPlayer().getHealth() <= 5){
 				hp = ChatColor.RED + hp;
 			}
 			hp += ChatColor.WHITE;
 			event.setMessage(Util.replaceColor(event.getMessage()));
 			if(Perm.PermissionsHandler != null){
 				event.setFormat(
 						Util.replaceColor(
 								V.chat
 								.replace("<prefix>", Perm.PermissionsHandler.getGroupPrefix(event.getPlayer().getWorld().getName(), Perm.PermissionsHandler.getGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName())))
 								.replace("<suffix>", Perm.PermissionsHandler.getGroupSuffix(event.getPlayer().getWorld().getName(), Perm.PermissionsHandler.getGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName())))
 								.replace("<player>", event.getPlayer().getDisplayName())
 								.replace("<message>", event.getMessage())
 								.replace("<hp>", hp)
 								.replace("<world>", event.getPlayer().getWorld().getName())
 								.replace("<group>", Perm.PermissionsHandler.getGroup(event.getPlayer().getWorld().getName(), event.getPlayer().getName()))
 								.replace("<x>", Double.toString(Math.round(event.getPlayer().getLocation().getX())))
 								.replace("<y>", Double.toString(Math.round(event.getPlayer().getLocation().getY())))
 								.replace("<z>", Double.toString(Math.round(event.getPlayer().getLocation().getZ())))
 						)
 				);
 			}else{
 				event.setFormat(
 						Util.replaceColor(
 								V.chat
 								.replace("<prefix>", "")
 								.replace("<suffix>", "")
 								.replace("<player>", event.getPlayer().getDisplayName())
 								.replace("<message>", event.getMessage())
 								.replace("<hp>", hp)
 								.replace("<world>", event.getPlayer().getWorld().getName())
 								.replace("<group>", "")
 								.replace("<x>", Double.toString(Math.round(event.getPlayer().getLocation().getX())))
 								.replace("<y>", Double.toString(Math.round(event.getPlayer().getLocation().getY())))
 								.replace("<z>", Double.toString(Math.round(event.getPlayer().getLocation().getZ())))
 						)
 				);
 			}
 		}
 		L.log(event.getPlayer().getName() + ": " + event.getMessage());
 	}
 }
