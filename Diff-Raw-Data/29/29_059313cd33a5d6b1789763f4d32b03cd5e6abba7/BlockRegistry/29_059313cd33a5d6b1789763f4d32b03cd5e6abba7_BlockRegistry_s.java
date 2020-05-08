 package MMC.neocraft.registry;
 
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import MMC.neocraft.block.*;
 import MMC.neocraft.lib.Reference;
 import MMC.neocraft.tileentity.TileEntitySteeper;
 
 public class BlockRegistry
 {
 	
 	public static void registerBlocks()
 	{
 		GameRegistry.registerBlock(NCblock.orangeWood, Reference.MOD_ID + "@" + NCblock.orangeWood.getUnlocalizedName().substring(5));
 		GameRegistry.registerBlock(NCblock.orangeLeaves, ItemBlockOrangeLeaves.class, Reference.MOD_ID + "@" + NCblock.orangeLeaves.getUnlocalizedName().substring(5));
 		GameRegistry.registerBlock(NCblock.blockTest, Reference.MOD_ID + "@" + NCblock.blockTest.getUnlocalizedName().substring(5));
 		GameRegistry.registerBlock(NCblock.saplingOrange, Reference.MOD_ID + "@" + NCblock.saplingOrange.getUnlocalizedName().substring(5));
 		GameRegistry.registerBlock(NCblock.teaSteeper, Reference.MOD_ID + "@" + NCblock.teaSteeper.getUnlocalizedName().substring(5));
<<<<<<< HEAD
 		GameRegistry.registerBlock(NCblock.kilnCore, Reference.MOD_ID + "@" + NCblock.kilnCore.getUnlocalizedName().substring(5));
=======
 		GameRegistry.registerBlock(NCblock.magicSteeper, Reference.MOD_ID + "@" + NCblock.magicSteeper.getUnlocalizedName().substring(5));
>>>>>>> 1c6f3f25b592ba7010d20a775c1e1964f41a5477
 	}
 	public static void registerTileEntities()
 	{
 		GameRegistry.registerTileEntity(TileEntitySteeper.class, Reference.MOD_ID + "@" + "tileentity.teaSteeper");
 	}
 	public static void registerNames()
 	{
 		LanguageRegistry.addName(NCblock.orangeWood, "Orange Wood");
 		LanguageRegistry.addName(new ItemStack(NCblock.orangeLeaves, 1, 0), "Orange Leaves");
 		LanguageRegistry.addName(new ItemStack(NCblock.orangeLeaves, 1, 1), "Orange Leaves");
 		LanguageRegistry.addName(new ItemStack(NCblock.blockTest, 1, 0), "Test Block");
 		LanguageRegistry.addName(NCblock.saplingOrange, "Orange Tree Sapling");
 		LanguageRegistry.addName(NCblock.teaSteeper, "Tea Steeper");
<<<<<<< HEAD
 		LanguageRegistry.addName(NCblock.kilnCore, "Kiln Core");
=======
 		LanguageRegistry.addName(NCblock.magicSteeper, "Magic Steeper");
>>>>>>> 1c6f3f25b592ba7010d20a775c1e1964f41a5477
 	}
 }
