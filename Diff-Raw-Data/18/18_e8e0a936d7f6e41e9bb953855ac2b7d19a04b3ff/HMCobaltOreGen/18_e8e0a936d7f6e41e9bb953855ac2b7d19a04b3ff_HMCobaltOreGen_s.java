 
 package hawksmachinery.common;
 
 import hawksmachinery.common.block.HMBlock;
 import net.minecraft.item.ItemStack;
 import universalelectricity.prefab.ore.OreGenReplace;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMCobaltOreGen extends OreGenReplace
 {
 	public HMCobaltOreGen()
 	{
 		super("Cobalt Ore", "oreCobalt", new ItemStack(HMBlock.ore, 1, 1), 1, 12, 30, 32, 1, "pickaxe", 2);
		this.ignoreNether = true;
		this.ignoreEnd = true;
 		this.shouldGenerate = HawksMachinery.instance().MANAGER.generateCobalt;
 		
 	}
 	
 }
