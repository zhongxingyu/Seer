 package no.runsafe.ItemControl;
 
 import no.runsafe.framework.api.IOutput;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.block.IBlockBreakEvent;
 import no.runsafe.framework.api.event.block.IBlockDispense;
 import no.runsafe.framework.api.event.block.IItemDispensed;
 import no.runsafe.framework.api.minecraft.RunsafeEntityType;
 import no.runsafe.framework.minecraft.Enchant;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.framework.minecraft.RunsafeLocation;
 import no.runsafe.framework.minecraft.RunsafeWorld;
 import no.runsafe.framework.minecraft.block.RunsafeBlock;
 import no.runsafe.framework.minecraft.block.RunsafeSpawner;
 import no.runsafe.framework.minecraft.event.block.RunsafeBlockBreakEvent;
 import no.runsafe.framework.minecraft.item.RunsafeItemStack;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.minecraft.player.RunsafePlayer;
 
 import java.util.logging.Level;
 
 public class BlockListener implements IBlockBreakEvent, IItemDispensed
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
