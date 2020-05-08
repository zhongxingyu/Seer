 package no.runsafe.itemflangerorimega.tools.enchants;
 
 import no.runsafe.framework.api.ILocation;
 import no.runsafe.framework.api.block.IBlock;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.Item;
 import no.runsafe.itemflangerorimega.tools.CustomToolEnchant;
 
 public class Lumberjacking extends CustomToolEnchant
 {
 	@Override
 	public String getEnchantText()
 	{
 		return "Lumberjacking I";
 	}
 
 	@Override
 	public String getSimpleName()
 	{
 		return "lumberjack";
 	}
 
 	@Override
 	public boolean onBlockBreak(IPlayer player, IBlock block)
 	{
 		if (player.isSurvivalist())
 			breakBlock(block, 0);
 
 		return false;
 	}
 
 	private void breakBlock(IBlock block, int depth)
 	{
 		if (block.is(Item.BuildingBlock.Wood.Any))
 		{
 			block.breakNaturally();
 			ILocation location = block.getLocation();
 
 			depth += 1;
 
			if (depth < 100)
 			{
 				check(location, 1, 0, 0, depth);
 				check(location, -1, 0, 0, depth);
 				check(location, 0, 0, 1, depth);
 				check(location, 0, 0, -1, depth);
 				check(location, 0, 1, 0, depth);
 				check(location, 0, -1, 0, depth);
 			}
 		}
 	}
 
 	private void check(ILocation location, int offsetX, int offsetY, int offsetZ, int depth)
 	{
 		location = location.clone();
 		location.offset(offsetX, offsetY, offsetZ);
 		IBlock block = location.getBlock();
 
 		if (block.is(Item.Decoration.Leaves.Any) || block.is(Item.BuildingBlock.Wood.Any))
 			breakBlock(block, depth);
 	}
 }
