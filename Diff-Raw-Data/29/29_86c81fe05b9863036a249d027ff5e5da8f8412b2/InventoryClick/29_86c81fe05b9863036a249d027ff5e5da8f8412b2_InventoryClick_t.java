 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.api.event.inventory.IInventoryClick;
 import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 
 public class InventoryClick implements IInventoryClick
 {
 	@Override
 	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
 	{
 		RunsafeMeta item = event.getCurrentItem();
 		if (item != null)
 		{
 			String name = item.getDisplayName();
 			if (name != null)
 			{
 				if (name.equalsIgnoreCase("Infinite"))
 				{
 					RunsafePlayer player = event.getWhoClicked();
 					if (!player.hasPermission("runsafe.toybox.infinitedispensers"))
 					{
 						player.sendColouredMessage("&cYou do not have permission to make those.");
						event.cancel();
 					}
 				}
 			}
 		}
 	}
 }
