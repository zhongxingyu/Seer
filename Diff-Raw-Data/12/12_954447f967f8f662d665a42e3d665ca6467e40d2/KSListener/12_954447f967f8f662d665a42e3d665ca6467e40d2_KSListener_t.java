 package de.bdh.ks;
 
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 
 public class KSListener implements Listener 
 {
 	public Main m;
 	public KSListener(Main main) 
 	{
 		this.m = main;
 	}
 
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
     public void onJoin(PlayerJoinEvent event)
     {
 		Main.helper.hasDelivery(event.getPlayer()); 
 		Main.lng.msg(event.getPlayer(), "welcome");
     }
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
     public void onQuit(PlayerQuitEvent event)
     {
 		this.m.poffer.remove(event.getPlayer());
     }
 	
 	/*
 	//Idee: Verkaufe automatisch alle Items, die man in eine Kiste legt
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event)
 	{
 		if(event.getPlayer() instanceof Player)
 		{
 			//Player p = (Player) event.getPlayer();
 			if(event.getInventory().getHolder() instanceof Chest)
 			{
 				for(ItemStack i : event.getInventory().getContents())
 				{
 					if(i != null)
 					{
 						//Items aus Truhe werden ausgegeben
 					}
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent event)
 	{
 		if(event.getPlayer() instanceof Player)
 		{
 			
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event)
 	{	
 		if(event.getWhoClicked() instanceof Player)
 		{
 			
 		}
 	}*/
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event)
 	{
 		if(event.isCancelled() == true)
 			return;
 		
 		if(event.getBlock() != null)
 		{
			if(event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST)
 			{
 				if(this.m.poffer.get(event.getPlayer()) != null)
 				{
 					Main.helper.setSign(this.m.poffer.get(event.getPlayer()), event.getBlock());
 					Boolean offer = false;
 					if(this.m.poffer.get(event.getPlayer()).type == 1)
 						offer = true;
 					Main.helper.updateSign(this.m.poffer.get(event.getPlayer()).id, false, offer);
 					
 					this.m.poffer.remove(event.getPlayer());
 					Main.lng.msg(event.getPlayer(), "suc_sign_com");
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event)
     {
 		if(event.getClickedBlock() != null) 
 		{  
			if(event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
 			{
 				//Signshop Implementation
 				if(event.getClickedBlock().getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK)
     			{
 					if(Main.helper.serveOfferBySign(event.getPlayer(), event.getClickedBlock()))
 						event.setCancelled(true);
     			}
 			}
 			
 			if(configManager.ender == 0)  
 				return;  
 			
 			if(event.getClickedBlock().getTypeId() == configManager.interactBlock && !event.getPlayer().isSneaking())  
 	 		{  
 	 			if(configManager.interactBlockSub != 0)
 	 			{
 	 				if(event.getClickedBlock().getData() != configManager.interactBlockSub)
 	 					return;
 	 			}
 	 			
 	 			if(event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR)  
 	 			{  
 	 				//Zeige Infos über den Block  
 	 				Main.helper.sendInfos(event.getPlayer(), event.getPlayer().getItemInHand());  
 	 				  
 	 			}  
 	 			else if(event.getAction() == Action.RIGHT_CLICK_BLOCK)  
 	 				Main.helper.getDelivery(event.getPlayer());  
 	 			  
 	 			event.setCancelled(true);  
 	 		}  
 	 	} 
     }
 	
 }
