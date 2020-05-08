 package com.rakosmanjr.InfTools;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerInteractListener implements Listener
 {
 	int[] allowedIDs;
 	InfTools plugin;
 	
 	public PlayerInteractListener (InfTools plugin)
 	{
 		this.plugin = plugin;
 		
		allowedIDs = new int[28];
 		allowedIDs[0] = 267;//iron sword
 		allowedIDs[1] = 255;//iron shovel
 		allowedIDs[2] = 257;//iron pick
 		allowedIDs[3] = 258;//iron axe
 		allowedIDs[4] = 292;//iron hoe
 		allowedIDs[5] = 268;//wood sword
 		allowedIDs[6] = 269;//wood shovel
 		allowedIDs[7] = 270;//wood pick
 		allowedIDs[8] = 271;//wood axe
 		allowedIDs[9] = 290;//wood hoe
 		allowedIDs[10] = 272;//stone sword
 		allowedIDs[11] = 273;//stone shovel
 		allowedIDs[12] = 274;//stone pick
 		allowedIDs[13] = 275;//stone axe
 		allowedIDs[14] = 291;//stone hoe
 		allowedIDs[15] = 283;//gold sword
 		allowedIDs[16] = 284;//gold shovel
 		allowedIDs[17] = 285;//gold pick
 		allowedIDs[18] = 286;//gold axe
 		allowedIDs[19] = 294;//gold hoe
 		allowedIDs[20] = 276;//diamond sword
 		allowedIDs[21] = 277;//diamond shovel
 		allowedIDs[22] = 278;//diamond pick
 		allowedIDs[23] = 279;//diamond axe
 		allowedIDs[24] = 293;//diamond hoe
 		allowedIDs[25] = 346;//fishing pole
		allowedIDs[26] = 261;//bow
		allowedIDs[27] = 259;//flint & steel
 	}
 	
 	@EventHandler
 	public void onPlayerInteract (PlayerInteractEvent event)
 	{
		try
 		{
 			Player player = event.getPlayer();
 			ItemStack itemStack = event.getItem();
 			Material mat = itemStack.getType();
			
 			if (mat.isBlock() || mat.isEdible() || mat.isRecord())
 			{
 				return;
 			}
			
 			if (plugin.isInfTool.containsKey(player.getName()) ? plugin.isInfTool
 					.get(player.getName()) : false)
 			{
 				
 				for (int i = 0; i < allowedIDs.length; i++)
 				{
 					if (mat.getId() == allowedIDs[i])
 					{
 						itemStack.setDurability((short)0);
 						
 						if (player.getItemInHand().getTypeId() == mat.getId())
 						{
 							player.setItemInHand(itemStack);
 						}
 						
 						break;
 					}
 				}
 			}
 		}
		catch (Exception e)
		{
			
		}
 	}
 }
