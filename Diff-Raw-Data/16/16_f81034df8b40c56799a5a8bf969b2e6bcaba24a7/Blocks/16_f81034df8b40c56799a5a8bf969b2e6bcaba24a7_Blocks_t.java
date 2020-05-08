 package assets.tacotek.blocks;
 
 import assets.tacotek.common.tacotek;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Blocks {
 
 	public Blocks()
 	{
 		super();
 	}
 	
 	public static Block blockTaco;
 	
 	public static void addBlocks()
 	{
		blockTaco = new BlockTaco(834).setUnlocalizedName("blocktaco").setCreativeTab(tacotek.tacotekTab);
 		registerBlock(blockTaco, "Taco Block", blockTaco.getUnlocalizedName() );
 	}
 	
 	public static void registerBlock(Block block, String name, String unlocalizedName)
 	{
 		GameRegistry.registerBlock(block, tacotek.modID + unlocalizedName);
 		LanguageRegistry.addName(block, name);
 	}
 	
 }
