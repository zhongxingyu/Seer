 package no.runsafe.runsafebank.events;
 
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 import no.runsafe.runsafebank.BankHandler;
 
 public class Interact implements IPlayerInteractEvent
 {
 	public Interact(BankHandler bankHandler)
 	{
 		this.bankHandler = bankHandler;
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		RunsafeBlock block = event.getBlock();
 		if (block != null && event.isRightClick() && block.is(Item.Decoration.EnderChest))
 		{
 			RunsafePlayer player = event.getPlayer();
 			if (player.hasPermission("runsafe.bank.use"))
 				this.bankHandler.openBank(player, player);
 			else
 				player.sendColouredMessage("&cYou do not have permissions to use the bank.");
 
			event.setCancelled(true);
 		}
 	}
 
 	private BankHandler bankHandler;
 }
