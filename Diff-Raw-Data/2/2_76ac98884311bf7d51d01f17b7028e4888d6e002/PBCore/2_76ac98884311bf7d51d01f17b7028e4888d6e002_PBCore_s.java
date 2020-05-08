 package slimevoid.projectbench.core;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import slimevoid.projectbench.blocks.BlockProjectBase;
 import slimevoid.projectbench.core.lib.BlockLib;
 import slimevoid.projectbench.core.lib.ConfigurationLib;
 import slimevoid.projectbench.core.lib.IconLib;
 import slimevoid.projectbench.core.lib.ItemLib;
 import slimevoid.projectbench.core.lib.LocaleLib;
 import slimevoid.projectbench.items.ItemBase;
 import slimevoid.projectbench.items.ItemPlan;
 import slimevoid.projectbench.tileentity.TileEntityProjectBench;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class PBCore {
 	public static void registerNames() {
 		LocaleLib.registerLanguages();
 	}
 	
 	public static void registerBlocks() {
 		ConfigurationLib.blockProjectBase = new BlockProjectBase(ConfigurationLib.blockProjectBaseID);
 		GameRegistry.registerBlock(ConfigurationLib.blockProjectBase, ItemBase.class, BlockLib.BLOCK_PROJECT_BASE);
 		GameRegistry.registerTileEntity(TileEntityProjectBench.class, BlockLib.BLOCK_PROJECT_BENCH);
 		ConfigurationLib.blockProjectBase.addTileEntityMapping(BlockLib.BLOCK_PROJECT_BENCH_ID, TileEntityProjectBench.class);
 		ConfigurationLib.blockProjectBase.setItemName(BlockLib.BLOCK_PROJECT_BENCH_ID, BlockLib.BLOCK_PROJECT_BENCH);
 		GameRegistry.addRecipe(new ItemStack(ConfigurationLib.blockProjectBase, 1, BlockLib.BLOCK_PROJECT_BENCH_ID),
 				new Object[] {
 					"SBS",
 					"SCS",
					"WSW",
 					Character.valueOf('S'), Block.cobblestone,
 					Character.valueOf('B'), Block.workbench,
 					Character.valueOf('C'), Block.chest,
 					Character.valueOf('W'), Block.woodSingleSlab
 		});
 	}
 	
 	public static void registerItems() {
 		ConfigurationLib.itemPlanBlank = new Item(ConfigurationLib.itemPlanBlankID).func_111206_d(IconLib.PROJECT_PLAN_BLANK).setUnlocalizedName(ItemLib.PROJECT_PLAN_BLANK);
 		ConfigurationLib.itemPlanBlank.setCreativeTab(CreativeTabs.tabMisc);
 		ConfigurationLib.itemPlanFull = new ItemPlan(ConfigurationLib.itemPlanFullID);
 		ConfigurationLib.itemPlanFull.setCreativeTab(CreativeTabs.tabMisc);
 	}
 }
