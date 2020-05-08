 package no.runsafe.runsafebank.events;
 
 import no.runsafe.framework.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
 
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.runsafebank.BankHandler;
 import org.bukkit.Material;
 
 public class Interact implements IPlayerInteractEvent
 {
 	public Interact(IOutput output, BankHandler bankHandler)
 	{
 		this.output = output;
 		this.bankHandler = bankHandler;
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
		try
 		{
			if (event.getBlock().getTypeId() == Material.ENDER_CHEST.getId())
 			{
 				RunsafePlayer player = event.getPlayer();
 				if (player.hasPermission("runsafe.bank.use"))
 					this.bankHandler.openPlayerBank(player, player);
 				else
					player.sendColouredMessage("&cYou do not have permission to use the bank.");
 
 				event.setCancelled(true);
 			}
 		}
		catch (NullPointerException e)
		{
			// Was not an event we care for.
		}
 	}
 
 	private IOutput output;
 	private BankHandler bankHandler;
 }
