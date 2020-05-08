 package com.turt2live;
 
 import java.util.HashMap;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class AntiShareListener implements Listener {
 
 	private AntiShare plugin;
 	private HashMap<Player, Long> blockDropTextWarnings = new HashMap<Player, Long>();
 
 	public AntiShareListener(AntiShare p){
 		plugin = p;
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_break", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")){
 						event.setCancelled(true);
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_break", player.getWorld())));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked){
 				event.setCancelled(true);
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_break", player.getWorld())));
 			}
 			//Bedrock check
 			if(!event.isCancelled()
 					&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 					&& !player.hasPermission("AntiShare.bedrock")
 					&& event.getBlock().getType() == Material.BEDROCK){
 				if(plugin.getConfig().getBoolean("other.only_if_creative")){
 					if(player.getGameMode() == GameMode.CREATIVE){
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.bedrock", player.getWorld())));
 						event.setCancelled(true);
 					}
 				}else{
 					player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.bedrock", player.getWorld())));
 					event.setCancelled(true);
 				}
 			}
 			//Creative Mode Blocking
 			if(!event.isCancelled()
 					&& plugin.config().getBoolean("other.track_blocks", player.getWorld())
 					&& !player.hasPermission("AntiShare.blockBypass")){
 				if(player.getGameMode() == GameMode.SURVIVAL){
 					boolean isBlocked = ASBlockRegistry.isBlockCreative(event.getBlock());
 					if(isBlocked){
 						if(!plugin.getConfig().getBoolean("other.blockDrops")){
 							player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.creativeModeBlock", player.getWorld())));
 							event.setCancelled(true);
 						}else{
 							player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.creativeModeBlock", player.getWorld())));
 							ASBlockRegistry.unregisterCreativeBlock(event.getBlock());
 							Block block = event.getBlock();
 							event.setCancelled(true);
 							block.setTypeId(0); // Fakes a break
 						}
 					}
 				}else{
 					ASBlockRegistry.unregisterCreativeBlock(event.getBlock());
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockDamage(BlockDamageEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_break", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")){
 						event.setCancelled(true);
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_break", player.getWorld())));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked){
 				event.setCancelled(true);
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_break", player.getWorld())));
 			}
 		}
 		// Warning message for block drops
 		if(!event.isCancelled()){
 			if(plugin.getConfig().getBoolean("other.blockDrops")
 					&& !player.hasPermission("AntiShare.blockBypass")
 					&& ASBlockRegistry.isBlockCreative(event.getBlock())){
 				long systemTime = System.currentTimeMillis();
 				if(blockDropTextWarnings.containsKey(player)){
 					if((systemTime - blockDropTextWarnings.get(player)) > 1000){
 						player.sendMessage(ASUtils.addColor(plugin.getConfig().getString("messages.noBlockDrop")));
 						blockDropTextWarnings.remove(player);
 						blockDropTextWarnings.put(player, systemTime);
 					}
 				}else{
 					player.sendMessage(ASUtils.addColor(plugin.getConfig().getString("messages.noBlockDrop")));
 					blockDropTextWarnings.put(player, systemTime);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlockPlaced().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_place", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place")){
 						event.setCancelled(true);
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_place", player.getWorld())));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place") && itemIsBlocked){
 				event.setCancelled(true);
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.block_place", player.getWorld())));
 			}
 		}
 		//Bedrock check
 		if(!event.isCancelled()
 				&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 				&& !player.hasPermission("AntiShare.bedrock")
 				&& event.getBlock().getType() == Material.BEDROCK){
 			if(plugin.getConfig().getBoolean("other.only_if_creative")){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.bedrock", player.getWorld())));
 					event.setCancelled(true);
 				}
 			}else{
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.bedrock", player.getWorld())));
 				event.setCancelled(true);
 			}
 		}
 		//Creative Mode Placing
 		if(!event.isCancelled()
 				&& plugin.config().getBoolean("other.track_blocks", player.getWorld())
 				&& player.getGameMode() == GameMode.CREATIVE
 				&& !player.hasPermission("AntiShare.freePlace")){
 			ASBlockRegistry.saveCreativeBlock(event.getBlock());
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityDamage(EntityDamageEvent event){
 		if(!(event instanceof EntityDamageByEntityEvent)
 				|| event.isCancelled()){
 			return;
 		}
 		Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
 		if(damager instanceof Player){
 			Player dealer = (Player) damager;
 			if(dealer.getGameMode() != GameMode.CREATIVE && !plugin.getConfig().getBoolean("other.only_if_creative")){
 				return;
 			}else if(dealer.getGameMode() != GameMode.CREATIVE){
 				return;
 			}
 			//System.out.println("GM: " + dealer.getGameMode().toString());
 			if(event.getEntity() instanceof Player){
 				if(plugin.getConfig().getBoolean("other.pvp")){
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.pvp")){
 					dealer.sendMessage(ASUtils.addColor(plugin.config().getString("messages.pvp", event.getEntity().getWorld())));
 					event.setCancelled(true);
 				}
 			}else{
 				if(plugin.getConfig().getBoolean("other.pvp-mob")){
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.mobpvp")){
 					dealer.sendMessage(ASUtils.addColor(plugin.config().getString("messages.mobpvp", event.getEntity().getWorld())));
 					event.setCancelled(true);
 				}
 			}
 		}else if(damager instanceof Projectile){
 			LivingEntity shooter = ((Projectile) damager).getShooter();
 			if(shooter instanceof Player){
 				Player dealer = ((Player) shooter);
 				if(dealer.getGameMode() != GameMode.CREATIVE && !plugin.getConfig().getBoolean("other.only_if_creative")){
 					return;
 				}else if(dealer.getGameMode() != GameMode.CREATIVE){
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.pvp")){
 					dealer.sendMessage(ASUtils.addColor(plugin.config().getString("messages.pvp", dealer.getWorld())));
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityDeath(EntityDeathEvent event){
 		// System.out.println("onDeath | " + event.getEntity());
 		if(event.getEntity() instanceof Player){
 			Player player = (Player) event.getEntity();
 			if(player != null){
 				for(ItemStack item : event.getDrops()){
 					boolean itemIsBlocked = false;
 					itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.death", player.getWorld()), item.getTypeId());
 					if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 						if(player.getGameMode() == GameMode.CREATIVE){
 							if(player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death")){
 								player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.death", player.getWorld())));
 								item.setAmount(0);
 							}
 						}
 					}else if(player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death") && itemIsBlocked){
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.death", player.getWorld())));
 						item.setAmount(0);
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityTarget(EntityTargetEvent event){
 		if(event.isCancelled())
 			return;
 
 		Entity targetEntity = event.getTarget();
 		if(event.getEntity() instanceof Monster
 				&& targetEntity != null
 				&& targetEntity instanceof Player){
 			Player player = (Player) targetEntity;
 			if(plugin.getConfig().getBoolean("other.only_if_creative")){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(!player.hasPermission("AntiShare.mobpvp")
 							&& !plugin.getConfig().getBoolean("other.mobpvp")){
 						event.setCancelled(true);
 					}
 				}
 			}else{
 				if(!player.hasPermission("AntiShare.mobpvp")
 						&& !plugin.getConfig().getBoolean("other.mobpvp")){
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player sender = event.getPlayer();
 		if(sender.hasPermission("AntiShare.allow.commands")){
 			return;
 		}
 		if(plugin.getConfig().getBoolean("other.only_if_creative")){
 			if(sender.getGameMode() != GameMode.CREATIVE){
 				return;
 			}
 		}
 		String commandsToBlock[] = plugin.getConfig().getString("events.commands").split(" ");
 		String commandSent = event.getMessage();
 		for(String check : commandsToBlock){
 			if(check.equalsIgnoreCase(commandSent)){
 				sender.sendMessage(ASUtils.addColor(plugin.getConfig().getString("messages.illegalCommand")));
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		// System.out.println("onDrop | " + event.getPlayer() + " | " + event.getItemDrop().getItemStack().getTypeId());
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			ItemStack item = event.getItemDrop().getItemStack();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.drop_item", player.getWorld()), item.getTypeId());
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop")){
 						event.setCancelled(true);
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.drop_item", player.getWorld())));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop") && itemIsBlocked){
 				event.setCancelled(true);
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.drop_item", player.getWorld())));
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
 		if(plugin.config().getBoolean("other.inventory_swap", event.getPlayer().getWorld())){
 			Player player = event.getPlayer();
 			if(player != null){
 				if(!player.hasPermission("AntiShare.noswap")){
 					ASInventory.save(player, player.getGameMode());
 					ASInventory.load(player, event.getNewGameMode());
 					player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.inventory_swap", player.getWorld())));
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		// System.out.println("onInteract | " + event.getPlayer() + " | " + event.getClickedBlock().getTypeId());
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled() && event.getClickedBlock() != null){
 			boolean itemIsBlocked = false;
 			int item = event.getClickedBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.interact", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact")){
 						event.setCancelled(true);
 						player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.interact", player.getWorld())));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact") && itemIsBlocked){
 				event.setCancelled(true);
 				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.interact", player.getWorld())));
 			}
 			//Egg check
 			if(plugin.config().getBoolean("other.allow_eggs", player.getWorld()) == false){
 				boolean filter = false;
 				if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && player.getGameMode() == GameMode.CREATIVE){
 					filter = true;
 				}else if(!plugin.config().getBoolean("other.only_if_creative", player.getWorld())){
 					filter = true;
 				}
 				if(player.hasPermission("AntiShare.allow.eggs")){
 					filter = false;
 				}
 				if(filter && (player.hasPermission("AntiShare.eggs"))){
 					ItemStack possibleEgg = event.getItem();
 					if(possibleEgg != null){
 						if(possibleEgg.getTypeId() == 383){
 							event.setCancelled(true);
 							player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.eggs", player.getWorld())));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
 			boolean cancel = !ASMultiWorld.worldSwap(plugin, player, event.getFrom(), event.getTo());
 			if(cancel){
				player.sendMessage(ASUtils.addColor(plugin.config().getString("messages.worldSwap", event.getTo().getWorld())));
 				event.setCancelled(true);
 			}
 		}
 	}
 }
