 
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
 public class HMEndiumOreGen extends OreGenReplace
 {
 	public HMEndiumOreGen()
 	{
 		super("Endium Ore", "oreEndium", new ItemStack(HMBlock.ore), 121, 12, 64, 8, 8, "pickaxe", 3);
 		this.ignoreNether = true;
 		this.ignoreSurface = true;
		this.ignoreEnd = false;
 		this.shouldGenerate = HawksMachinery.instance().MANAGER.generateEndium;
 		
 	}
 	
 }
