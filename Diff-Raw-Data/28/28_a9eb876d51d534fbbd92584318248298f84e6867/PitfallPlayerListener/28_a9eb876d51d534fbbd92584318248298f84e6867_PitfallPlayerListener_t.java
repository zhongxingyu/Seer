 package com.bukkit.jason.pitfall;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  * Handle events for all Player related events
  * 
  * @author <Your Name>
  */
 public class PitfallPlayerListener extends PlayerListener
 {
 	private final Pitfall plugin;
 
 	public PitfallPlayerListener(final Pitfall instance)
 	{
 		plugin = instance;
 	}
 
 	public void onPlayerMove(PlayerMoveEvent event)
 	{
 		final Player player = event.getPlayer();
 		final Block b = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 2, player.getLocation().getBlockZ());
 		int botMaterial = b.getTypeId();
 		if (botMaterial == PitfallSettings.pitItem)
 		{
 			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
 			{
 				public void run()
 				{
 					try
 					{
 						destroy(player, b);
 					}
 					catch (Exception e)
 					{
 					}
 				}
 			}, 2l);
 		}
 	}
	//TODO Use better method, exception driven testing is BAAAAD
    private boolean isInteger( String input )  
    {  
       try  
       {  
          Integer.parseInt( input );  
          return true;  
       }  
       catch( Exception e)  
       {  
          return false;  
       }  
    }  
 	private boolean inBlackList(int typeId)
 	{
 		for(int i=0;i<PitfallSettings.blackList.length;i++)
 		{
			if(isInteger(PitfallSettings.blackList[i])&&Integer.parseInt(PitfallSettings.blackList[i])==typeId)
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	public void destroy(Player player, final Block b)
 	{
 		if (b.getTypeId() != PitfallSettings.pitItem)
 			return;
 		final Block h = player.getWorld().getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() + 1, b.getLocation().getBlockZ());
 		final int type = h.getTypeId();
		if (!inBlackList(h.getTypeId()) )
 		{
 			h.setTypeId(0);
 			b.setTypeId(0);
 
 			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
 			{
 				public void run()
 				{
 					try
 					{
 						h.setTypeId(type);
 						b.setTypeId(PitfallSettings.pitItem);
 					}
 					catch (Exception e)
 					{
 					}
 				}
 			}, 60l);
 		}
 
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() - 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ() - 1));
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ() - 1));
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() + 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ() - 1));
 
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() - 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ()));
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() + 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ()));
 
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() - 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ() + 1));
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY(), b.getLocation().getBlockZ() + 1));
 		destroy(player, player.getWorld().getBlockAt(b.getLocation().getBlockX() + 1, b.getLocation().getBlockY(), b.getLocation().getBlockZ() + 1));
 	}
 }
