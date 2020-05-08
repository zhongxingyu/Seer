 package no.runsafe.runsafeinventories.events;
 
import no.runsafe.framework.event.player.IPlayerPortalEvent;
import no.runsafe.framework.event.player.IPlayerTeleportEvent;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.event.player.RunsafePlayerPortalEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerTeleportEvent;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.InventoryHandler;
 
public class PlayerTeleport implements IPlayerTeleportEvent, IPlayerPortalEvent
 {
 	public PlayerTeleport(InventoryHandler inventoryHandler, IOutput output)
 	{
 		this.inventoryHandler = inventoryHandler;
 		this.output = output;
 	}
 
 	@Override
	public void OnPlayerTeleport(RunsafePlayerTeleportEvent event)
 	{
		RunsafePlayer player = event.getPlayer();
 		this.output.fine("Detected teleport event: " + player.getName());
		this.checkTeleportEvent(event.getTo().getWorld(), event.getFrom().getWorld(), player);
 	}
 
 	@Override
	public void OnPlayerPortalEvent(RunsafePlayerPortalEvent event)
 	{
		RunsafePlayer player = event.getPlayer();
 		this.output.fine("Detected portal event: " + player.getName());
		this.checkTeleportEvent(event.getTo().getWorld(), event.getFrom().getWorld(), player);
 	}
 
 	private void checkTeleportEvent(RunsafeWorld to, RunsafeWorld from, RunsafePlayer player)
 	{
 		if (!to.getName().equals(from.getName()))
 			this.inventoryHandler.handlePreWorldChange(player);
 	}
 
 	private InventoryHandler inventoryHandler;
 	private IOutput output;
 }
