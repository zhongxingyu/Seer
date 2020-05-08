 package no.runsafe.runsafeinventories.events;
 
import no.runsafe.framework.event.player.IPlayerPortal;
import no.runsafe.framework.event.player.IPlayerTeleport;
 import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.runsafeinventories.InventoryHandler;
 
public class PlayerTeleport implements IPlayerTeleport, IPlayerPortal
 {
 	public PlayerTeleport(InventoryHandler inventoryHandler, IOutput output)
 	{
 		this.inventoryHandler = inventoryHandler;
 		this.output = output;
 	}
 
 	@Override
	public void OnPlayerTeleport(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
 	{
 		this.output.fine("Detected teleport event: " + player.getName());
		this.checkTeleportEvent(to == null ? null : to.getWorld(), from == null ? null : from.getWorld(), player);
 	}
 
 	@Override
	public void OnPlayerPortal(RunsafePlayer player, RunsafeLocation from, RunsafeLocation to)
 	{
 		this.output.fine("Detected portal event: " + player.getName());
		this.checkTeleportEvent(to == null ? null : to.getWorld(), from == null ? null : from.getWorld(), player);
 	}
 
 	private void checkTeleportEvent(RunsafeWorld to, RunsafeWorld from, RunsafePlayer player)
 	{
 		if (!to.getName().equals(from.getName()))
 			this.inventoryHandler.handlePreWorldChange(player);
 	}
 
 	private InventoryHandler inventoryHandler;
 	private IOutput output;
 }
