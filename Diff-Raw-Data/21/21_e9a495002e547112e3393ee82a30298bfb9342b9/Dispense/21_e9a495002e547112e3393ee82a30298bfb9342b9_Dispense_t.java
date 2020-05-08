 package no.runsafe.toybox.events;
 
 import no.runsafe.framework.event.block.IBlockDispense;
 import no.runsafe.framework.server.block.RunsafeBlock;
 import no.runsafe.framework.server.block.RunsafeBlockState;
 import no.runsafe.framework.server.block.RunsafeDispenser;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
import no.runsafe.framework.server.item.meta.RunsafeMeta;
 import org.bukkit.block.Dropper;
 
 public class Dispense implements IBlockDispense
 {
 	@Override
	public boolean OnBlockDispense(RunsafeBlock block, RunsafeMeta item)
 	{
 		RunsafeBlockState blockState = block.getBlockState();
 		if (blockState instanceof RunsafeDispenser)
 		{
 			RunsafeDispenser dispenser = (RunsafeDispenser) blockState;
 			RunsafeInventory inventory = dispenser.getInventory();
 			String title = inventory.getTitle();
 
 			if (title.equalsIgnoreCase("Infinite"))
 				inventory.addItems(item.clone());
 		}
 		else if (blockState.getRaw() instanceof Dropper)
 		{
 			Dropper dropper = (Dropper) blockState.getRaw();
 			RunsafeInventory inventory = new RunsafeInventory(dropper.getInventory());
 			String title = inventory.getTitle();
 
 			if (title.equalsIgnoreCase("Infinite"))
 				inventory.addItems(item.clone());
 		}
 		return true;
 	}
 }
