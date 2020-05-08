 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.event.inventory.IInventoryClick;
 import no.runsafe.framework.server.event.inventory.RunsafeInventoryClickEvent;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.RunsafeItemMeta;
 import no.runsafe.framework.server.player.RunsafePlayer;
 
 public class InventoryClick implements IInventoryClick
 {
 	@Override
 	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
 	{
 		RunsafeItemStack item = event.getCurrentItem();
 		if (item != null)
 		{
 			RunsafeItemMeta meta = item.getItemMeta();
 			if (meta != null)
 			{
				if (meta.getDisplayName().startsWith("Infinite:"))
 				{
					RunsafePlayer player = event.getWhoClicked();
					if (!player.hasPermission("runsafe.toybox.infinitedispensers"))
 					{
						player.sendColouredMessage("&cYou do not have permission to make those.");
						event.setCancelled(true);
 					}
 				}
 			}
 		}
 	}
 }
