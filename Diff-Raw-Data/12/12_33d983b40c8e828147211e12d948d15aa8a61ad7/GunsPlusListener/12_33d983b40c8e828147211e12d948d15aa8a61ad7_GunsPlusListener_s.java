 package team.GunsPlus;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.event.input.KeyPressedEvent;
 import org.getspout.spoutapi.event.input.KeyReleasedEvent;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.ScreenType;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.keyboard.Keyboard;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import team.GunsPlus.Block.TripodData;
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Gui.HUD;
 import team.GunsPlus.Gui.TripodPopup;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Manager.TripodDataHandler;
 import team.GunsPlus.Util.GunUtils;
 import team.GunsPlus.Util.PlayerUtils;
 import team.GunsPlus.Util.Util;
 
 public class GunsPlusListener implements Listener {
 
 	public GunsPlus plugin;
 	public static String credit;
 
 	public GunsPlusListener(GunsPlus instance) {
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onButtonInteract(ButtonClickEvent e){
 		Button  b = e.getButton();
 		SpoutPlayer sp = e.getPlayer();
 		if(b.getPlugin().equals(plugin)&&sp.getMainScreen().getActivePopup() instanceof TripodPopup){
 			TripodPopup tpp = (TripodPopup) sp.getMainScreen().getActivePopup();
 			if(tpp.getId("MANU").equals(b.getId())){
 				tpp.setChooserDisabled();
 			}else if(tpp.getId("AUTO").equals(b.getId())){
 				tpp.setChooserEnabled();
 			}else if (tpp.getId("APPLY").equals(b.getId())){
 				tpp.applyData();
 				tpp.close(sp);
 			}else if (tpp.getId("ADD").equals(b.getId())&&tpp.getMode()==-1){
 				tpp.setAddMode();
 				tpp.setTargetChooser();
 			}else if(tpp.getId("OK").equals(b.getId())&&tpp.getMode()!=-1){
 				tpp.removeTargetChooser();
 				tpp.removePlayerChooser();
 				tpp.performListAction();
 			}else if(tpp.getId("DEL").equals(b.getId())&&tpp.getMode()==-1){
 				tpp.setDelMode();
 				tpp.setTargetChooser();
 			}else if(tpp.getId("EDIT").equals(b.getId())&&tpp.getMode()==-1){
 				tpp.setEditMode();
 				tpp.setTargetChooser();
 			}
 		}
 	}
 	
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		Player p = e.getPlayer();
 		Action a = e.getAction();
 		if (GunsPlus.lwc != null)
 			GunsPlus.lwc.wrapPlayer(p);
 		if (!PlayerUtils.hasSpoutcraft(p))return;
 		SpoutPlayer sp = (SpoutPlayer) p;
 		GunsPlusPlayer gp = PlayerUtils.getPlayerBySpoutPlayer(sp);
 		if(gp==null) return;
 		Gun g = null;
 		if (GunUtils.holdsGun(sp))
 			g = GunUtils.getGunInHand(sp);
 		switch (a) {
 			case RIGHT_CLICK_AIR:
 				if(g!=null){
 					if (plugin.zoomKey.equals(KeyType.RIGHT))
 						gp.zoom(g);
 					if (plugin.reloadKey.equals(KeyType.RIGHT))
 						gp.reload(g);
 					if (plugin.fireKey.equals(KeyType.RIGHT))
 						gp.fire(g);
 					if (sp.isSneaking() && plugin.zoomKey.equals(KeyType.RIGHTSHIFT))
 						gp.zoom(g);
 					if (sp.isSneaking() && plugin.reloadKey.equals(KeyType.RIGHTSHIFT))
 						gp.reload(g);
 					if (sp.isSneaking() && plugin.fireKey.equals(KeyType.RIGHTSHIFT))
 						gp.fire(g);
 				}
 				break;
 			case RIGHT_CLICK_BLOCK:
 				if(g!=null){
 					if (plugin.zoomKey.equals(KeyType.RIGHT))
 						gp.zoom(g);
 					if (plugin.reloadKey.equals(KeyType.RIGHT))
 						gp.reload(g);
 					if (plugin.fireKey.equals(KeyType.RIGHT))
 						gp.fire(g);
 					if (sp.isSneaking() && plugin.zoomKey.equals(KeyType.RIGHTSHIFT))
 						gp.zoom(g);
 					if (sp.isSneaking() && plugin.reloadKey.equals(KeyType.RIGHTSHIFT))
 						gp.reload(g);
 					if (sp.isSneaking() && plugin.fireKey.equals(KeyType.RIGHTSHIFT))
 						gp.fire(g);
 				}
 				if(Util.isTripod(e.getClickedBlock())){
 					TripodData td = Util.loadTripodData(e.getClickedBlock());
 					if(td.getOwner()!=null&&td.getOwner().equals(gp)){
 						if(td.getGun()==null&&sp.isSneaking()&&GunUtils.holdsGun(sp)){
 							Gun tg = GunUtils.getGunInHand(sp);
 							if(GunUtils.isMountable(tg)){
 								td.setGun(tg);
 								GunUtils.removeGunInHand(sp);
 							}else{
 								if(GunsPlus.notifications)
 									sp.sendNotification("You can't mount a", GunUtils.getGunNameWITHOUTAdditions(tg), new SpoutItemStack(tg), 2000);
 							}
 						}else if(td.getGun()!=null&&sp.isSneaking()){
 							TripodPopup tpp = new TripodPopup(plugin, td);
 							tpp.attach(sp);
 						}else if(td.getGun()!=null){
 							sp.openInventory(td.getInventory());
 						}
 				}
 				}
 				break;
 			case LEFT_CLICK_AIR:
 				if(g!=null){
 					if (plugin.zoomKey.equals(KeyType.LEFT))
 						gp.zoom(g);
 					if (plugin.reloadKey.equals(KeyType.LEFT))
 						gp.reload(g);
 					if (plugin.fireKey.equals(KeyType.LEFT))
 						gp.fire(g);
 					if (sp.isSneaking() && plugin.zoomKey.equals(KeyType.LEFTSHIFT))
 						gp.zoom(g);
 					if (sp.isSneaking() && plugin.reloadKey.equals(KeyType.LEFTSHIFT))
 						gp.reload(g);
 					if (sp.isSneaking() && plugin.fireKey.equals(KeyType.LEFTSHIFT))
 						gp.fire(g);
 				}
 				break;
 			case LEFT_CLICK_BLOCK:
 				if(g!=null){
 					if (plugin.zoomKey.equals(KeyType.LEFT))
 						gp.zoom(g);
 					if (plugin.reloadKey.equals(KeyType.LEFT))
 						gp.reload(g);
 					if (plugin.fireKey.equals(KeyType.LEFT))
 						gp.fire(g);
 					if (sp.isSneaking() && plugin.zoomKey.equals(KeyType.LEFTSHIFT))
 						gp.zoom(g);
 					if (sp.isSneaking() && plugin.reloadKey.equals(KeyType.LEFTSHIFT))
 						gp.reload(g);
 					if (sp.isSneaking() && plugin.fireKey.equals(KeyType.LEFTSHIFT))
 						gp.fire(g);
 				}
 				if(Util.isTripod(e.getClickedBlock())){
 					TripodData td = Util.loadTripodData(e.getClickedBlock().getLocation());
 					if(td.getOwner().equals(gp)&&td.getGun()!=null){
 						if(!td.isAutomatic()&&!td.isEntered()){
 							td.setEntered(true);
 						}else if(!td.isAutomatic()&&td.isEntered()){
 							td.setEntered(false);
 						}else if(td.isAutomatic()&&!td.isWorking()){
 							td.setWorking(true);
 						}else if(td.isAutomatic()&&td.isWorking()){
 							td.setWorking(false);
 						}
 					}
 				}
 				break;
 		}
 	}
 
 	@EventHandler
 	public void onHeldItemChange(PlayerItemHeldEvent e) {
 		Player p = e.getPlayer();
 		if (!PlayerUtils.hasSpoutcraft(p))
 			return;
 		SpoutPlayer sp = (SpoutPlayer) p;
 		ItemStack preItem = p.getInventory().getItem(e.getPreviousSlot());
 		ItemStack nextItem = p.getInventory().getItem(e.getNewSlot());
 		if(Util.enteredTripod(sp)){
 			sp.getInventory().setItem(e.getNewSlot(), preItem);
 			sp.getInventory().setItem(e.getPreviousSlot(), null);
 			return;
 		}else if(PlayerUtils.isZooming(sp)) {
 			PlayerUtils.setZooming(sp, false);
 			GunUtils.zoomOut(PlayerUtils.getPlayerBySpoutPlayer(sp));
 		}
 		
 		if (GunUtils.isGun(preItem)) {
 			sp.setWalkingMultiplier(1);
 		}
 		if (GunUtils.isGun(nextItem)) {
 			Gun g = GunUtils.getGun(nextItem);
 			sp.setWalkingMultiplier(1 - (g.getValue("WEIGHT")/100));
 		}
 	}
 	
 	//make guns unstackable
 	@EventHandler(ignoreCancelled=true)
     public void onInventoryClick(InventoryClickEvent event) {
             ItemStack clicked = event.getCurrentItem();
             ItemStack cursor = event.getCursor();
             if (clicked != null && GunUtils.isGun(clicked)) {
                     if (cursor != null && GunUtils.isGun(cursor) && event.isLeftClick() && !event.isShiftClick() && clicked.getDurability() == cursor.getDurability()) {
                     		event.setCancelled(true);
                     } else if (cursor !=null && GunUtils.isGun(cursor) && event.isRightClick() && !event.isShiftClick() && clicked.getDurability() == cursor.getDurability()){
                     		event.setCancelled(true);
                     } else if (event.isShiftClick()) {
                             event.setCancelled(true);
                             Inventory main = event.getView().getBottomInventory();
                             Inventory top = event.getView().getTopInventory();
                             if (top.getType() == InventoryType.CHEST) {
                                     if (event.getRawSlot() < top.getSize()) {
                                             int slot = main.firstEmpty();
                                             if (slot >= 0) {
                                                     main.setItem(slot, event.getCurrentItem().clone());
                                                     top.setItem(event.getSlot(), null);
                                             }
                                     } else {
                                             int slot = top.firstEmpty();
                                             if (slot >= 0) {
                                                     top.setItem(slot, event.getCurrentItem().clone());
                                                     main.setItem(event.getSlot(), null);
                                             }
                                     }
                             }
                     }
             }
     }
 	
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent e){
 		Player p = e.getPlayer();
 		ItemStack i = e.getItemDrop().getItemStack();
 		if(PlayerUtils.hasSpoutcraft(p)){
 			SpoutPlayer sp = (SpoutPlayer)p;
 			if(Util.enteredTripod(sp)){
 				e.setCancelled(true);
 				sp.setItemInHand(i);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPickupItem(PlayerPickupItemEvent e){
 		Player p = e.getPlayer();
 		Item i = e.getItem();
 		ItemStack itemstack = i.getItemStack();
 		if(!PlayerUtils.hasSpoutcraft(p)) return;
 		SpoutPlayer sp = (SpoutPlayer) p ;
 		//if the player entered a tripod put the stack in his normal inventory (stored in the tripoddata)
 		if(Util.enteredTripod(sp)){
 			e.setCancelled(true);
 			i.remove();
 			TripodData td = Util.getTripodDataOfEntered(sp);
 			td.getOwnerInv().setItem(td.getOwnerInv().firstEmpty(), itemstack);
 		}
 		//make sure that the guns will not be stacked on pickup
 		if(GunUtils.isGun(itemstack) && GunUtils.checkInvForGun(sp.getInventory(), GunUtils.getGun(itemstack))){
 			e.setCancelled(true);
 			i.remove();
 			sp.getInventory().setItem(sp.getInventory().firstEmpty(), itemstack);
 		}
 	}
 	
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent e){
 		if(e.getEntityType().equals(EntityType.FIREBALL)&&((Fireball)e.getEntity()).getShooter()==null){
 			Fireball fireball = (Fireball) e.getEntity();
 			fireball.setIsIncendiary(false);
 			e.setCancelled(true);
 			e.setYield(0);
 		}
 		for(Block b: e.blockList()){
 			if(Util.isTripod(b)){
 				Location l = b.getLocation();
 				TripodData td = Util.loadTripodData(l);
 				td.resetDroppedGun();
 				if(td.isEntered())
 					td.setEntered(false);
 				if(td.isWorking())
 					td.setWorking(false);
 				if(td.getGun()!=null)
 					l.getWorld().dropItemNaturally(l, new SpoutItemStack(td.getGun(), 1));
 				for(ItemStack a : td.getInventory().getContents()) if(a!=null)l.getWorld().dropItemNaturally(l, a);
 				TripodDataHandler.removeId(TripodDataHandler.getId(td.getLocation()));                                                                                                    
 				GunsPlus.allTripodBlocks.remove(td);
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onBlockDestroyed(BlockBreakEvent e){
 		if(!Util.isTripod(e.getBlock())) return;
 		Location l = e.getBlock().getLocation();
 		TripodData td = Util.loadTripodData(l);
 		td.resetDroppedGun();
 		if(td.isEntered())
 			td.setEntered(false);
 		if(td.isWorking())
 			td.setWorking(false);
 		if(td.getGun()!=null)
 			l.getWorld().dropItemNaturally(l, new SpoutItemStack(td.getGun(), 1));
 		for(ItemStack a : td.getInventory().getContents()) if(a!=null)l.getWorld().dropItemNaturally(l, a);
 		TripodDataHandler.removeId(TripodDataHandler.getId(td.getLocation()));                                                                                                    
 		GunsPlus.allTripodBlocks.remove(td);
 	}
 	
 	@EventHandler
 	public void onKeyPressed(KeyPressedEvent e) {
 		SpoutPlayer sp = e.getPlayer();
 		if (!GunUtils.holdsGun(sp))return;
 		Gun g = GunUtils.getGunInHand(sp);
 		if(g==null) return;
 		GunsPlusPlayer gp = PlayerUtils.getPlayerBySpoutPlayer(sp);
 		if(gp==null) return;
 		Keyboard key = e.getKey();
 		String keyString = key.toString().split("_")[1].toLowerCase();
 		ScreenType st = e.getScreenType();
 		if ((plugin.zoomKey.equals(KeyType.LETTER)
 				|| plugin.zoomKey.equals(KeyType.NUMBER)
 				|| plugin.zoomKey.equals(KeyType.HOLDNUMBER) || plugin.zoomKey
 					.equals(KeyType.HOLDLETTER))
 				&& plugin.zoomKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			gp.zoom(g);
 		} else if ((plugin.reloadKey.equals(KeyType.LETTER)
 				|| plugin.reloadKey.equals(KeyType.NUMBER)
 				|| plugin.reloadKey.equals(KeyType.HOLDNUMBER) || plugin.reloadKey
 					.equals(KeyType.HOLDLETTER))
 				&& plugin.reloadKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			gp.reload(g);
 		} else if ((plugin.fireKey.equals(KeyType.LETTER)
 				|| plugin.fireKey.equals(KeyType.NUMBER)
 				|| plugin.fireKey.equals(KeyType.HOLDNUMBER) || plugin.fireKey
 					.equals(KeyType.HOLDLETTER))
 				&& plugin.fireKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			gp.fire(g);
 		}
 	}
 
 	@EventHandler
 	public void onKeyReleased(KeyReleasedEvent e) {
 		SpoutPlayer sp = e.getPlayer();
 		if (!GunUtils.holdsGun(sp)) return;
 		Gun g = GunUtils.getGunInHand(sp);
 		GunsPlusPlayer gp = PlayerUtils.getPlayerBySpoutPlayer(sp);
 		Keyboard key = e.getKey();
 		String keyString = key.toString().split("_")[1].toLowerCase();
 		ScreenType st = e.getScreenType();
 		if ((plugin.zoomKey.equals(KeyType.HOLDNUMBER) || plugin.zoomKey
 				.equals(KeyType.HOLDLETTER))
 				&& plugin.zoomKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			gp.zoom(g);
 		} else if ((plugin.reloadKey.equals(KeyType.HOLDNUMBER) || plugin.reloadKey
 				.equals(KeyType.HOLDLETTER))
 				&& plugin.reloadKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			// stop reloading ?
 		} else if ((plugin.fireKey.equals(KeyType.HOLDNUMBER) || plugin.fireKey
 				.equals(KeyType.HOLDLETTER))
 				&& plugin.fireKey.getData().equalsIgnoreCase(keyString)
 				&& st.toString().equalsIgnoreCase("GAME_SCREEN")) {
 			// stop fireing ?
 		}
 	}
 
 	@EventHandler
 	public void onSpoutCraftEnable(SpoutCraftEnableEvent e) {
 		SpoutPlayer sp = e.getPlayer();
 		HUD hud = new HUD(plugin, GunsPlus.hudX, GunsPlus.hudY, GunsPlus.hudBackground);
 		GunsPlusPlayer gp = new GunsPlusPlayer(sp, hud);
 		GunsPlus.GunsPlusPlayers.add(gp);
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e){
 		if(PlayerUtils.hasSpoutcraft(e.getPlayer())&&PlayerUtils.getPlayerBySpoutPlayer((SpoutPlayer)e.getPlayer())!=null){
 			GunsPlusPlayer gp = PlayerUtils.getPlayerBySpoutPlayer((SpoutPlayer)e.getPlayer());
 			if(Util.enteredTripod(gp.getPlayer())){
 				Util.getTripodDataOfEntered(gp.getPlayer()).setEntered(false);
 			}
 			for(TripodData td : GunsPlus.allTripodBlocks){
				if(td.getOwner().equals(gp)){
					TripodDataHandler.save(td);
 				}
 			}
 			GunsPlus.GunsPlusPlayers.remove(gp);
 		}
 		
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		SpoutPlayer sp = (SpoutPlayer) event.getPlayer();
 		if(GunsPlus.notifications) {
 			creditsDelayed(sp);
 		}
 	}
 	
 	public void credits(SpoutPlayer sp) {
 		credit = ("This server is running " + ChatColor.GOLD + "Guns+" + ChatColor.DARK_GREEN + " By:" + plugin.getDescription().getAuthors());
 		if(sp.isSpoutCraftEnabled()) sp.sendNotification(ChatColor.GRAY + "Guns+", ChatColor.DARK_GREEN + "By " + plugin.getDescription().getAuthors(), Material.SULPHUR);
 		else sp.sendMessage(credit);
 	}
 	
 	private void creditsDelayed(final SpoutPlayer sp) {
 		Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
 			public void run() {
 				credits(sp);
 			}
 		}, 100L);
 	}
 }
