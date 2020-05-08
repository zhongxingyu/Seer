 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.enchant.Enchant;
 import no.runsafe.framework.event.block.IBlockBreakEvent;
 import no.runsafe.framework.event.block.IBlockDispense;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.RunsafeWorld;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.block.RunsafeSpawner;
 import no.runsafe.framework.server.entity.RunsafeEntityType;
 import no.runsafe.framework.server.event.block.RunsafeBlockBreakEvent;
 import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.item.meta.RunsafeMeta;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.IScheduler;
 
 import java.util.logging.Level;
 
 public class BlockListener implements IBlockBreakEvent, IBlockDispense
 {
 	public BlockListener(Globals globals, IScheduler scheduler, IOutput output)
 	{
 		this.output = output;
 		this.globals = globals;
 		this.scheduler = scheduler;
 	}
 
 	@Override
 	public void OnBlockBreakEvent(RunsafeBlockBreakEvent event)
 	{
 		if (event.getCancelled())
 			return;
 
 		final RunsafeBlockBreakEvent blockBreakEvent = event;
 
 		RunsafePlayer thePlayer = event.getPlayer();
 		RunsafeItemStack heldItem = thePlayer.getItemInHand();
 		final RunsafeBlock theBlock = event.getBlock();
 
 		if (this.globals.blockShouldDrop(thePlayer.getWorld(), theBlock.getTypeId()) && heldItem.enchanted(Enchant.SilkTouch))
 		{
 			try
 			{
 				RunsafeSpawner spawner = (RunsafeSpawner) theBlock;
 				final RunsafeEntityType creature = spawner.getCreature();
 				if (!globals.spawnerTypeValid(creature, thePlayer))
 				{
 					output.outputToConsole(
 						String.format(
 							"%s tried harvesting an invalid %s spawner!",
 							thePlayer.getName(),
 							creature.getName()
 						),
 						Level.WARNING
 					);
 					return;
 				}
 				scheduler.createSyncTimer(
 					new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							if (blockBreakEvent.getCancelled())
 								return;
 							Item.Miscellaneous.MonsterEgg.Get(creature).Drop(theBlock.getLocation(), 1);
 						}
 					},
 					10L
 				);
 				blockBreakEvent.setXP(0);
 			}
 			catch (Exception e)
 			{
 				output.logException(e);
 			}
 		}
 	}
 
 	@Override
	public boolean OnBlockDispense(RunsafeBlock block, RunsafeMeta itemStack)
 	{
 		RunsafeWorld blockWorld = block.getWorld();
 		RunsafeLocation blockLocation = block.getLocation();
 		if (this.globals.itemIsDisabled(block.getWorld(), itemStack.getItemId()))
 		{
 			blockWorld.createExplosion(blockLocation, 0, true);
 			block.breakNaturally();
 			return false;
 		}
 		return true;
 	}
 
 	private final IOutput output;
 	private final Globals globals;
 	private final IScheduler scheduler;
 }
