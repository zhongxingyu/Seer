 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import java.util.Set;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Ability.AbilityBind;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
 import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
 import com.google.common.collect.DiscreteDomains;
 import com.google.common.collect.Ranges;
 
 public class InventoryListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	private void onInventoryClickEvent(InventoryClickEvent event)
 	{
 		Player player = (Player) event.getWhoClicked();
 		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
 
 		if(event.getClickedInventory() instanceof PlayerInventory)
 		{
 			PlayerInventory inventory = (PlayerInventory) event.getClickedInventory();
 			Demigods.message.broadcast("Clicked slot: " + event.getSlot() + "/" + event.getRawSlot());
 			Demigods.message.broadcast("Item: " + inventory.getItem(event.getSlot()));
 			Demigods.message.broadcast("Size: " + inventory.getSize());
 		}
 
 		Set<Integer> hotBar = Ranges.closed(event.getClickedInventory().getSize() - 9, event.getClickedInventory().getSize()).asSet(DiscreteDomains.integers());
 
 		// Return if no character exists
 		if(character == null) return;
 
 		for(AbilityBind bind : character.getMeta().getBinds())
 		{
 			if(MiscUtility.getIndex(hotBar, event.getRawSlot()) == bind.getSlot()) event.setCancelled(true);
 		}
 	}
 }
