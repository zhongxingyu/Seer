 package de.bdh.kb2;
 
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import de.bdh.kb.util.configManager;
 import de.bdh.kb2.Main;
 import java.util.HashSet;
 public class KBPlayerListener implements Listener
 {
 	Main p;
 	KBHelper helper;
 	public Map<Player,Long> lastclick = new HashMap<Player,Long>();
 	
 	public KBPlayerListener(Main m)
 	{
 		this.p = m;
 		this.helper = Main.helper;
 		
 		for (Player player: Bukkit.getServer().getOnlinePlayers()) 
         {
 	        this.helper.loadPlayerAreas(player);
 	        this.helper.updateLastOnline(player);
         }
 	}
 	
 	@EventHandler
     public void onQuit(PlayerQuitEvent event)
     {
 		this.helper.lastBlock.remove(event.getPlayer());
 		this.helper.pass.remove(event.getPlayer());
 		this.helper.userarea.remove(event.getPlayer());
     }
 	
 	@EventHandler
     public void onJoin(PlayerJoinEvent event)
     {
         Player player = event.getPlayer();
         this.helper.loadPlayerAreas(player);
         this.helper.updateLastOnline(player);
     }
 	
 	//BlockDamage - Anti Guest 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockDamage(BlockDamageEvent event)
     {
     	if(!(event.getPlayer() instanceof Player))
 			return;
     	
         Player player = event.getPlayer();
         if(!player.hasPermission("kab.build"))
         {
             event.setCancelled(true);
             this.helper.blockedEvent.put(event.hashCode(), true);
             
             if(configManager.lang.equalsIgnoreCase("de"))
             	player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast noch keine Berechtigung zum Bauen.").toString());
             else
             	player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to build.").toString());
 
             return;
         }
     }
     
     //GARBAGE COLLECTORS
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockDamageGarbageCollector(BlockDamageEvent event)
     {
     	this.garbageCollector(event);
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
 	public void onDiscGarbageCollector(InventoryClickEvent event)
     {
     	this.garbageCollector(event);
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onBlockPlaceGarbageCollector(BlockPlaceEvent event)
     {
     	this.garbageCollector(event);
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockBreakGarbageCollector(BlockBreakEvent event)
 	{
     	this.garbageCollector(event);
 	}
     
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerInteractEventGarbageCollector(PlayerInteractEvent event)
     {
 		this.garbageCollector(event);
     }
 	
     @EventHandler(priority = EventPriority.MONITOR)
 	public void onClickPlayerGarbageCollector(PlayerInteractEntityEvent event)
     {
     	this.garbageCollector(event);
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
 	public void onPistonGarbageCollector(BlockPistonExtendEvent event)
 	{
     	if(configManager.doPiston == 1)
     		this.garbageCollector(event);
 	}
     
     public void garbageCollector(Event event)
     {
     	if(this.helper.blockedEvent.get(event.hashCode()) != null)
     		this.helper.blockedEvent.remove(event.hashCode());
     }
     
     //Inventory Click - Anti Guest
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onDisc(InventoryClickEvent event)
     {
 		if(event.getWhoClicked() instanceof Player)
 		{
 			Player player = (Player) event.getWhoClicked();
 			if(!player.hasPermission("kab.build"))
 	        {
 	        	event.setCancelled(true);
 	        	this.helper.blockedEvent.put(event.hashCode(), true);
 	        	if(configManager.lang.equalsIgnoreCase("de"))
 	        		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast noch keine Berechtigung.").toString());
 	        	else
 	        		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do that").toString());
 
 	        	return;
 	        }
 		}
     }
     
     //BlockPlace - Anti Guest
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPlaceGuest(BlockPlaceEvent blockplaceevent)
     {
 		if(!(blockplaceevent.getPlayer() instanceof Player))
 			return;
 		
         Player player = blockplaceevent.getPlayer();
         if(!player.hasPermission("kab.build"))
         {
         	blockplaceevent.setCancelled(true);
         	this.helper.blockedEvent.put(blockplaceevent.hashCode(), true);
         	if(configManager.lang.equalsIgnoreCase("de"))
         		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast noch keine Berechtigung zum Bauen.").toString());
         	else
         		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to do build").toString());
 
         	return;
         }
     }
 
     //BlockBreak - Anti Guest
     @EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreakGuest(BlockBreakEvent blockbreakevent)
 	{
 		if(!(blockbreakevent.getPlayer() instanceof Player))
 			return;
 		
         Player player = blockbreakevent.getPlayer();
         if(!player.hasPermission("kab.build"))
         {
         	this.helper.blockedEvent.put(blockbreakevent.hashCode(), true);
         	blockbreakevent.setCancelled(true);
         	if(configManager.lang.equalsIgnoreCase("de"))
         		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast noch keine Berechtigung zum Bauen.").toString());
         	else
         		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to build").toString());
 
         	return;
         }
 	}
     
     //Interact - Anti Guest
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteractEventGuest(PlayerInteractEvent event)
     {
 		if(!(event.getPlayer() instanceof Player))
 			return;
 		
     	Player player = event.getPlayer();
         if(!player.hasPermission("kab.interact"))
         {
             event.setCancelled(true);
             this.helper.blockedEvent.put(event.hashCode(), true);
             if(configManager.lang.equalsIgnoreCase("de"))
             	player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du hast noch keine Berechtigung zum Interagieren.").toString());
             else
         		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You dont have permissions to interact with the world").toString());
 
             return;
         }
     }
     
     //Item Trade
     @EventHandler
 	public void onClickPlayer(PlayerInteractEntityEvent event)
     {
         if(!(event.getRightClicked() instanceof Player))
         	return;
        
         Player giver = event.getPlayer();
         Player reciever = (Player)event.getRightClicked();
         
        if(event.getPlayer().isSneaking() && giver != null && reciever != null)
         {
         	if(!giver.hasPermission("kb.trade") || !reciever.hasPermission("kb.trade"))
         	{
         		if(configManager.lang.equalsIgnoreCase("de"))
 					giver.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du darfst mit diesem Spieler nicht handeln").toString());
 				else
 	        		giver.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You're not allowed to trade with this player").toString());
 
         		return;
         	}
         } 
         
         if(event.getPlayer().isSneaking() && event.isCancelled() == false)
         {
         	Long lc = lastclick.get(giver);
         	if(lc != null && Math.abs(System.currentTimeMillis() - lc) < 500)
         	{
         		this.helper.blockedEvent.put(event.hashCode(), true);
 				event.setCancelled(true);
         	}
 			else
 			{
 				int itemid = giver.getItemInHand().getTypeId();
 				if(reciever.getInventory().firstEmpty() == -1)
 				{
 					if(configManager.lang.equalsIgnoreCase("de"))
 						giver.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Inventar des anderen Spielers ist voll.").toString());
 					else
 		        		giver.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The inventory of the other player is full").toString());
 
 				}
 				else if(giver.getInventory().contains(itemid))
 	            {
 	                reciever.getInventory().addItem(new ItemStack[] {
 	                	event.getPlayer().getItemInHand()
 	                });
 	                giver.getInventory().removeItem(new ItemStack[] {
 		                	event.getPlayer().getItemInHand()
 		            });
 	            }
 	        	event.setCancelled(true);
 	        	this.helper.blockedEvent.put(event.hashCode(), true);
 	        	lastclick.put(giver, System.currentTimeMillis());
 	        	return;
 			}
         }
     }
     
     @EventHandler(priority = EventPriority.LOW)
 	public void onPiston(BlockPistonExtendEvent event)
 	{
     	if(configManager.doPiston == 1)
     	{
     		KBArea a = this.helper.getAreaByLocation(event.getBlock().getLocation());
     		
     		if(a != null)
     		{
 				List<Block> l = event.getBlocks();
 				if(!a.isIn(event.getBlock().getRelative(event.getDirection()).getLocation()))
 				{
 					this.helper.blockedEvent.put(event.hashCode(), true);
 					event.setCancelled(true);
 					return;
 				}
 				
 				for (Block b: l) 
 				{
 					if(!a.isIn(b.getRelative(event.getDirection()).getLocation()))
 					{
 						this.helper.blockedEvent.put(event.hashCode(), true);
 						event.setCancelled(true);
 						return;
 					}
 				}
     		}
     	}
 	}
     
     //DEFAULT Block Place
     @EventHandler(priority = EventPriority.LOW)
 	public void onBlockPlace(BlockPlaceEvent blockplaceevent)
     {
 		if(!(blockplaceevent.getPlayer() instanceof Player))
 			return;
 		
         Player player = blockplaceevent.getPlayer();
         if(!this.helper.canBuildHere(player, blockplaceevent.getBlock(),false))
         {
         	this.helper.blockedEvent.put(blockplaceevent.hashCode(), true);
     		blockplaceevent.setCancelled(true);
         	return;
         }
        
 		int i = blockplaceevent.getBlock().getTypeId();
 		Player p = blockplaceevent.getPlayer();
 		
 		if(p.hasPermission("kb.create") && (i == configManager.interactBlock))
 		{
 			this.helper.lastBlock.put(p, blockplaceevent.getBlock());
 		}
 		
 		if(configManager.doSign == 1 && blockplaceevent.isCancelled() == false && blockplaceevent.getBlockAgainst() != null && blockplaceevent.getBlock() != null)
 		{
 			if(blockplaceevent.getBlockAgainst().getTypeId() == configManager.interactBlock && (blockplaceevent.getBlock().getType() == Material.SIGN_POST || blockplaceevent.getBlock().getType() == Material.WALL_SIGN))
 			{
 				//Info-Sign
 				Main.helper.updateSign(blockplaceevent.getBlock());
 			}
 		}
     }
     
     //DEFAULT Block Break
     @EventHandler(priority = EventPriority.LOW)
 	public void onBlockBreak(BlockBreakEvent blockbreakevent)
 	{
 		if(!(blockbreakevent.getPlayer() instanceof Player))
 			return;
 		
         Player player = blockbreakevent.getPlayer();
       
         
         if(!this.helper.canBuildHere(player, blockbreakevent.getBlock(),false))
         {
         	this.helper.blockedEvent.put(blockbreakevent.hashCode(), true);
     		blockbreakevent.setCancelled(true);
         	return;
         }
         
         if(blockbreakevent.getBlock().getTypeId() == Material.SPONGE.getId() && blockbreakevent.getBlock().getRelative(BlockFace.DOWN).getTypeId() == configManager.interactBlock)
         {
         	this.helper.blockedEvent.put(blockbreakevent.hashCode(), true);
     		blockbreakevent.setCancelled(true);
         	return;
         }
        
         
 		Block b = blockbreakevent.getBlock();
 		int i = b.getTypeId();
 		Player p = blockbreakevent.getPlayer();
 		if(p.hasPermission("kb.create") && (i == configManager.interactBlock))
 		{
 			Long lc = lastclick.get(blockbreakevent.getPlayer());
 			lastclick.put(blockbreakevent.getPlayer(), System.currentTimeMillis());
 			
         	if(lc != null && Math.abs(System.currentTimeMillis() - lc) > 500)
         	{
         		this.helper.blockedEvent.put(blockbreakevent.hashCode(), true);
         		blockbreakevent.setCancelled(true);
         	}
         	else
         	{	
         		int id = this.helper.getIDbyBlock(b);
         		if(id != 0)
         		{
         			this.helper.killGS(id);
         			if(configManager.lang.equalsIgnoreCase("de"))
         				player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstück wurde zerstört").toString());
         			else
     	        		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("The lot has been destroyed").toString());
 
         		}
         	}
 		}
 	}
     
     //PVP
     
     @EventHandler
 	public void splashPot(PotionSplashEvent event)
 	{
 		try
 		{
 			List<Player> players = new ArrayList<Player>();
 			if(event.getPotion().getShooter() instanceof Player)
 			{
 				Player damager = (Player) event.getPotion().getShooter();
 				for(PotionEffect e: event.getPotion().getEffects())
 				{
 					if(e.getType().equals(PotionEffectType.WITHER) || e.getType().equals(PotionEffectType.CONFUSION) || e.getType().equals(PotionEffectType.POISON) || e.getType().equals(PotionEffectType.HARM) || e.getType().equals(PotionEffectType.WEAKNESS))
 					{
 						Player damaged = null;
 						boolean candamage = Main.helper.canPVPHere(damager);
 						for(LivingEntity tmp : event.getAffectedEntities())
 						{
 							if(tmp instanceof Player)
 							{
 								damaged = (Player)tmp;
 								if(damaged.hasPermission("kb.alwayspvp") && !damaged.isOp())
 								{
 									players.add(damaged);
 								}
 								
 
 								if(candamage != false)
 								{
 									if(Main.helper.canPVPHere(damaged) != false)
 						        	{
 										players.add(damaged);
 						        	}
 								}
 							}
 						}
 
 						event.setCancelled(true);
 						
 						for(Player p: players)
 						{
 							p.addPotionEffect(e);
 						}
 					}
 				}
 			}
 		} catch(Exception e)
 		{
 			//nothing
 		}
 	}
     
     @EventHandler
 	public void onAssault(EntityDamageByEntityEvent event)
     {
     	//Betrifft nur Spieler
     	if(!(event.getEntity() instanceof Player))
     		return;
     	
     	Player damaged = (Player)event.getEntity();
     	
     	if(damaged.hasPermission("kb.alwayspvp") && !damaged.isOp())
     		return;
     	
     	Player damager = null;
     	boolean dispensed = false;
     	
     	//Hole Shooterdaten
         if(event.getDamager() instanceof Arrow)
         {
         	Arrow ar = (Arrow)event.getDamager();
         	if(ar.getShooter() instanceof Player)
         		damager = (Player) ar.getShooter();
         	else if(ar.getShooter() == null)
         		dispensed = true;
         } else if(event.getDamager() instanceof Snowball)
         {
         	Snowball ar = (Snowball)event.getDamager();
         	if(ar.getShooter() instanceof Player)
         		damager = (Player) ar.getShooter();
         	else if(ar.getShooter() == null)
         		dispensed = true;
         } else if(event.getDamager() instanceof Fireball)
         {
         	Fireball ar = (Fireball)event.getDamager();
         	if(ar.getShooter() instanceof Player)
         		damager = (Player) ar.getShooter();
         	else if(ar.getShooter() == null)
         		dispensed = true;
         } else if(event.getDamager() instanceof Player)
         	damager = (Player)event.getDamager();
         
         if(dispensed == true)
         {
         	if(Main.helper.canPVPHere(damaged) == false)
         		event.setCancelled(true);
         	
         } else if(damager != null)
         {
         	if(Main.helper.canPVPHere(damaged) == false || Main.helper.canPVPHere(damager) == false)
         	{
         		event.setCancelled(true);
         		if(configManager.lang.equalsIgnoreCase("de"))
     				damager.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Du kannst diesen Spieler nicht angreifen").toString());
     			else
     				damager.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You cannot attack this player here").toString());
         	}
         }
     }
     
     //DEFAULT Interact
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerInteractEvent(PlayerInteractEvent event)
     {
 		if(!(event.getPlayer() instanceof Player))
 			return;
 		
     	Player player = event.getPlayer();
     
         if((event.getClickedBlock() instanceof Block))
 		{
 			Block b = event.getClickedBlock();
 			int gt = b.getTypeId();
 
 			if(b.getType() == Material.AIR)
 				return;
 			
 			if((gt == Material.DISPENSER.getId() || gt == Material.FURNACE.getId() || gt == Material.CHEST.getId()) && player.hasPermission("kb.nochest") && !player.hasPermission("kb.chest"))
 			{
 				if(!this.helper.canBuildHereData(player, b,true))
 				{
 					if(configManager.lang.equalsIgnoreCase("de"))
 						player.sendMessage("Dein Rang verbietet das Öffnen von Truhen / Dispensern / Öfen");
 					else
 		        		player.sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("You're not allowed to open chests, dispensers and furnaces").toString());
 
 					this.helper.blockedEvent.put(event.hashCode(), true);
 					event.setCancelled(true);
 					return;
 				}
 			}
 
 			//Verhindere manipulation an Pipes - BrauTec Mod
 			else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && (gt == 166 || gt == 187 || gt == 215) && !configManager.BrauTec.equalsIgnoreCase("0"))
 	        {
 	        	if(!this.helper.canBuildHere(player, b,true))
 				{
 					player.kickPlayer("Du darfst keine fremden Objekte editieren");
 					event.setCancelled(true);
 				}
 	        }
 	        
 			
 	        //Schilder duerfen immer geklickt werden - sowie minecarts immer auf rails oder wool gesetzt werden d�rfen
 	        else if(event.getAction() == Action.RIGHT_CLICK_BLOCK && ((gt == 63 || gt == 68 || gt == 323) || (player.getItemInHand().getTypeId() == 328 && (gt == Material.WOOL.getId() || gt == 27 || gt == 28 || gt == 66))))
 	        {
 	        	this.helper.blockedEvent.put(event.hashCode(), false);
 	        	event.setCancelled(false);
 	        	
 	        	//Schilder aktualisieren
 	        	if((gt == 63 || gt == 68) && configManager.doSign == 1)
 	        	{
 	        		if(Main.helper.near(event.getClickedBlock(), configManager.interactBlock) != null)
 	        		{
 	        			Main.helper.updateSign(event.getClickedBlock());
 	        		}
 	        	}
 	        	
 	        	//alles OK
 	        //Boote sind erlaubt
 	        } else if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && player.getItemInHand().getTypeId() == Material.BOAT.getId())
 	        {
                         //Blocks to be rendered as "transparent"
                         HashSet<Byte> ignore = new HashSet<Byte>();
                         //Ignore air, torches and signs
                         //NOTE:Add more if you like to
                         ignore.add((byte)0);
                         ignore.add((byte)Material.TORCH.getId());
                         ignore.add((byte)Material.WALL_SIGN.getId());
                         ignore.add((byte)Material.SIGN_POST.getId());
                         
                         Block targetBlock = player.getTargetBlock(ignore, 5);
                         
                         if(targetBlock!=null){
                             if(targetBlock.getType() == Material.WATER || targetBlock.getType() == Material.STATIONARY_WATER){
 
                                 this.helper.blockedEvent.put(event.hashCode(), false);
                                 event.setCancelled(false);
 
                             }
                         }
    	        }
 	        	
 	        //Holzkn�pfe gehen immer genauso wie 225 ( BrauTec ) und Bedrock
 	        else if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && (event.getClickedBlock().getTypeId() != configManager.interactBlock && event.getClickedBlock().getTypeId() != 225 && event.getClickedBlock().getTypeId() != Material.WOOD_BUTTON.getId()))
 			{
 				if(!(this.helper.canBuildHere(player, b.getRelative(BlockFace.UP),true)) && !this.helper.canBuildHere(player, b,true) && !event.getPlayer().hasPermission("kb.interact"))
 				{
 	        		event.setCancelled(true);
 	        		this.helper.blockedEvent.put(event.hashCode(), true);
 	        		if(configManager.interactMessage == 1)
 	        		{
 	        			if(configManager.lang.equalsIgnoreCase("de"))
 	        				player.sendMessage("Du kannst nicht auf X:"+b.getX() +"+Y:"+ b.getY()+" Z:"+b.getZ()+" interagieren");
 	        			else
 	    	        		player.sendMessage("You can't interact on X:"+b.getX() +"+Y:"+ b.getY()+" Z:"+b.getZ());
 
 	        		}
 	        	} else if(!(this.helper.canBuildHere(player, b.getRelative(BlockFace.UP),true)) && !this.helper.canBuildHere(player, b,true) && event.getPlayer().hasPermission("kb.interact") && (gt == Material.DISPENSER.getId() || gt == Material.FURNACE.getId() || gt == Material.CHEST.getId()))
 				{
 					//Truhen / Dispenser / Ofen sind trotz allem verboten (gilt nur mit Vanilla Blocks)
 					event.setCancelled(true);
 					this.helper.blockedEvent.put(event.hashCode(), true);
 					if(configManager.lang.equalsIgnoreCase("de"))
 						player.sendMessage("Du darfst keine fremden Truhen oeffnen");
 					else
 		        		player.sendMessage("You're not allowed to open chests from other players");
 					
 
 				}
 			}
 	        
 	        //Interaktionsblock
 	        else if(b.getTypeId() == configManager.interactBlock && !event.getPlayer().isSneaking())
 			{
 				if(event.getPlayer().hasPermission("kb.create"))
 					this.helper.lastBlock.put(event.getPlayer(), b);
 				
 				this.helper.blockedEvent.put(event.hashCode(), true);
 				event.setCancelled(true);
 				
 				int id = this.helper.getIDbyBlock(b);
         		if(id != 0)
         		{
         			KBArea a = this.helper.getArea(id);
         			if(a != null)
         			{
         				this.helper.lastBlock.put(event.getPlayer(), b);
         				
         				//Region String
         				StringBuilder s = (new StringBuilder());
         				if(a.ruleset.length() > 0)
         				{
         					if(configManager.lang.equalsIgnoreCase("de"))
         						s.append(ChatColor.YELLOW).append("Bauhöhe: ").append(a.height).append(" Blöcke, Keller: ").append(a.deep).append(" Blöcke");
         					else
         						s.append(ChatColor.YELLOW).append("Build height: ").append(a.height).append(" Blocks, basement: ").append(a.deep).append(" Blocks");
 	
         					if(a.miet > 0)
 							{
 								int miete;
 								if(a.miet == 1) miete = a.price; else miete = a.miet;
 								if(configManager.lang.equalsIgnoreCase("de"))
 									s.append(", Miete pro Tag: ").append(miete);
 								else
 									s.append(", rental fee per day: ").append(miete);
 							}
         				}
         				
         				//Typbezeichnung
         				String typ = "";
         				if(a.gruppe.length() > 0)
         					typ = a.gruppe;
         				else if(a.ruleset.length() == 0)
         					typ = "OnlyBlock";
         				else
         					typ = a.ruleset;
         				
         				//GS noch nicht verkauft
         				if(a.sold == 0)
         				{
         					if(a.nobuy == 0)
         					{
 	        					if(configManager.lang.equalsIgnoreCase("de"))
 	        						event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieser Bauplatz vom Typ ").append(typ).append(" steht zum Verkauf").toString());
 	        					else
 	        						event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot of the type  ").append(typ).append(" is for sale").toString());
 	        					if(a.price > 0)
 	        					{
 	        						if(configManager.lang.equalsIgnoreCase("de"))
 	        							event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Der Preis beträgt ").append(a.price).append(this.p.econ.currencyNamePlural()).toString());
 	        						else
 	        							event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("It cost ").append(a.price).append(this.p.econ.currencyNamePlural()).toString());
 	
 	        					}
 	        					else
 	        					{
 	        						if(configManager.lang.equalsIgnoreCase("de"))
 	        							event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstück ist kostenlos").toString());
 	        						else
 	        							event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("It's free").toString());
 	
 	        					}
 								if(s.length() > 1)
 									event.getPlayer().sendMessage(s.toString());
 								
 								if(configManager.lang.equalsIgnoreCase("de"))
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Wenn du dies kaufen willst, gib /buyGS ein").toString());
 								else
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("If you want to buy this - enter /buyGS").toString());
         					} else
         					{
         						if(configManager.lang.equalsIgnoreCase("de"))
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstueck ist unverkaeuflich").toString());
 								else
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is not for sale").toString());
         					}
         				} else if(a.owner.equalsIgnoreCase(event.getPlayer().getName()))
         				{
 							StringBuilder sndm = (new StringBuilder()).append(ChatColor.YELLOW).append("Level: '").append(a.level);
 							if(a.cansell != 0)
 							{
 								if(configManager.lang.equalsIgnoreCase("de"))
 									sndm.append(". Du kannst es mit /sellGS verkaufen");
 								else
 									sndm.append(". You can sell it by entering /sellGS");
 
 							}
 							if(configManager.lang.equalsIgnoreCase("de"))
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstück gehört dir. Es ist vom Typ '").append(typ).toString());
 							else
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot is yours. It's type is '").append(typ).toString());
 	
 							event.getPlayer().sendMessage(sndm.toString());
 							int cu = this.helper.canUpgradeArea(event.getPlayer(), b);
 							if(cu != 0)
 							{
 								if(configManager.lang.equalsIgnoreCase("de"))
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Dieses Grundstück kann erweitert werden für ").append(cu).append(this.p.econ.currencyNamePlural()).append(". Gib dazu /upgradeGS ein").toString());
 								else
 									event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot can be upgraded for ").append(cu).append(this.p.econ.currencyNamePlural()).append(". Just enter /upgradeGS").toString());
 
 							}        				
 						} else
         				{
 							if(configManager.lang.equalsIgnoreCase("de"))
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Das Grundstück mit dem Preis von ").append(a.paid).append(this.p.econ.currencyNamePlural()).append(" des Typs '").append(typ).append("' - Level ").append(a.level).append(" gehört ").append(a.owner).toString());
 							else
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("This lot with the price ").append(a.paid).append(this.p.econ.currencyNamePlural()).append(" and the type '").append(typ).append("' - Level ").append(a.level).append(" owns ").append(a.owner).toString());
 
 							long tmp = 1000l * a.lastonline;
 							String date = DateFormat.getDateInstance().format(tmp);
 							tmp = 1000l * a.kaufzeit;
 							String date2 = DateFormat.getDateInstance().format(tmp);
 							if(configManager.lang.equalsIgnoreCase("de"))
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Letztes mal Online: ").append(date).append(", Kaufzeitpunkt: ").append(date2).toString());
 							else
 								event.getPlayer().sendMessage((new StringBuilder()).append(ChatColor.YELLOW).append("Last online: ").append(date).append(", Bought: ").append(date2).toString());
 
         				}
         			}
         		}
 			} 
 		}
     }
 	   
 }
