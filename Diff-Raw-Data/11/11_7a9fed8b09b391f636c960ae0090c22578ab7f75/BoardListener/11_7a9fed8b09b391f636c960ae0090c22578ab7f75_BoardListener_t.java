 package de.bdh.kb2;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 import de.bdh.board.BoardHelper;
 import de.bdh.kb.util.configManager;
 
 public class BoardListener implements Listener 
 {
 	class removeBoard implements Runnable
 	{
 		BoardListener l;
 		Player p;
 		String id;
 		public removeBoard(BoardListener l, Player p, String id)
 		{
 			this.l = l;
 			this.p = p;
 			this.id = id;
 		}
 		public void run() 
 		{
 			this.l.bh.removeBoardFromPlayer(p, this.id);
 		}
 		
 	}
 	
 	Main m;
 	KBHelper helper;
 	BoardHelper bh;
 	public BoardListener(Main main) 
 	{
 		this.m = main;
 		this.helper = Main.helper;
 		this.bh = (BoardHelper) main.BoardHelper;
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerInteractEvent(PlayerInteractEvent event)
     {
 		Block b = event.getClickedBlock();
 		if(b == null || !(event.getClickedBlock() instanceof Block) || !(event.getPlayer() instanceof Player))
 			return;
 		
 		if(b.getTypeId() == configManager.interactBlock && !event.getPlayer().isSneaking())
 		{
 			int id = this.helper.getIDbyBlock(b);
     		if(id != 0)
     		{
     			KBArea a = this.helper.getArea(id);
     			if(a != null)
     			{
     				Scoreboard bd = this.bh.generateNewBoard();
     				Objective o = bd.registerNewObjective("infos", "dummy");
     					
     				
     				o.setDisplaySlot(DisplaySlot.SIDEBAR);
     				
     				int miet = 0, preis = 0;
     				if(a.miet > 0)
 					{
 						if(a.miet == 1) miet = a.price; else miet = a.miet;
 					}
     				
     				if(a.sold == 1) preis = a.paid; else preis = a.price;
     				
     				if(configManager.lang.equalsIgnoreCase("de"))
     				{
     					if(a.sold == 0 && a.nobuy == 0)
     						o.setDisplayName("GS Zum Verkauf");
     					else if(a.sold == 0 && a.nobuy == 1)
     						o.setDisplayName("GS Unverkaueflich");
     					else if(a.sold == 1)
    						o.setDisplayName(a.gruppe+" von "+a.owner);
     					
     					o.getScore(Bukkit.getOfflinePlayer("Hoehe:")).setScore(a.height);
     					o.getScore(Bukkit.getOfflinePlayer("Keller:")).setScore(a.deep);
    					o.getScore(Bukkit.getOfflinePlayer("Preis:")).setScore(preis);
    					o.getScore(Bukkit.getOfflinePlayer("Level:")).setScore(a.level);
     					if(miet != 0) o.getScore(Bukkit.getOfflinePlayer("Miete:")).setScore(miet);
     				} else
     				{
     					if(a.sold == 0 && a.nobuy == 0)
     						o.setDisplayName("Lot for sale");
     					else if(a.sold == 0 && a.nobuy == 1)
     						o.setDisplayName("Unbuyable lot");
     					else if(a.sold == 1)
    						o.setDisplayName(a.gruppe+" of "+a.owner);
     					
     					o.getScore(Bukkit.getOfflinePlayer("Height:")).setScore(a.height);
     					o.getScore(Bukkit.getOfflinePlayer("Basement:")).setScore(a.deep);
     					o.getScore(Bukkit.getOfflinePlayer("Value:")).setScore(preis);
    					o.getScore(Bukkit.getOfflinePlayer("Level:")).setScore(a.level);
     					if(miet != 0) o.getScore(Bukkit.getOfflinePlayer("Rent:")).setScore(miet);
     				}
     				
     				this.bh.registerPrivateBoard(event.getPlayer(), "krimbuy_detail_"+id, bd);
     				this.bh.showBoardToPlayer(event.getPlayer(), "krimbuy_detail_"+id);
     				Bukkit.getServer().getScheduler().runTaskLater(this.m, new removeBoard(this,event.getPlayer(),"krimbuy_detail_"+id), 10*20);
     			}
     		}
 		}
     }
 }
