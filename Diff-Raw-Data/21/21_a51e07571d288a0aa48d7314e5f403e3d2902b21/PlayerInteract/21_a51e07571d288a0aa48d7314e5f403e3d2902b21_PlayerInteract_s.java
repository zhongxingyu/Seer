 package no.runsafe.mailbox.events;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.mailbox.MailHandler;
 import no.runsafe.mailbox.MailboxBlocks;
 
public class PlayerInteract implements IPlayerInteractEvent
 {
 	public PlayerInteract(MailHandler mailHandler, MailboxBlocks blocks)
 	{
 		this.mailHandler = mailHandler;
 		this.blocks = blocks;
 	}
 
 	@Override
 	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
 	{
 		if (event.hasItem())
 		{
 			RunsafeMeta item = event.getItemStack();
 			if (item.is(Item.Decoration.Chest) && item.hasItemMeta())
 			{
 				String displayName = item.getDisplayName();
 				if (displayName != null && displayName.startsWith("Mail Package #"))
 				{
 					IPlayer player = event.getPlayer();
 					String[] split = displayName.split("#");
 					this.mailHandler.openPackage(player, Integer.valueOf(split[1]));
 					player.getInventory().remove(item);
 					event.cancel();
 				}
 			}
 		}
 
		ILocation targetBlock = event.getTargetBlock();
		if (targetBlock != null && targetBlock.getBlock().is(Item.Decoration.Chest) && blocks.isMailboxBlock(targetBlock))
			mailHandler.openMailbox(event.getPlayer());
 	}
 
 	private final MailHandler mailHandler;
 	private final MailboxBlocks blocks;
 }
