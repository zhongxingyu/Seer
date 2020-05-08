 package no.runsafe.runsafeinventories.events;
 
 import no.runsafe.framework.event.player.IPlayerDeathEvent;
 import no.runsafe.framework.event.player.IPlayerQuitEvent;
 import no.runsafe.framework.server.event.player.RunsafePlayerDeathEvent;
 import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.InventoryHandler;
 
 public class PlayerQuitOrDeath implements IPlayerQuitEvent, IPlayerDeathEvent
 {
 	public PlayerQuitOrDeath(InventoryHandler inventoryHandler)
 	{
 		this.inventoryHandler = inventoryHandler;
 	}
 
 	@Override
 	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
 	{
 		this.SaveInventory(event.getEntity());
 	}
 
 	@Override
 	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
 	{
 		this.SaveInventory(event.getPlayer());
 	}
 
 	private void SaveInventory(RunsafePlayer player)
 	{
 		// Save on logout in-case we need to use the data during maintenance.
 		this.inventoryHandler.saveInventory(player);
 	}
 
 	private InventoryHandler inventoryHandler;
 }
