 package shadow.mods.metallurgy;
 import java.io.File;
 import java.util.Random;
 
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import shadow.mods.metallurgy.*;
 import shadow.mods.metallurgy.base.BaseConfig;
 import shadow.mods.metallurgy.base.mod_MetallurgyBaseMetals;
 import shadow.mods.metallurgy.fantasy.FF_EssenceRecipes;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.BaseMod;
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.IChunkProvider;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.World;
 import net.minecraftforge.oredict.OreDictionary;
 
 public class mod_Gold
 {
 	
 	public static final int meta = 4;
 		
	public static Item GoldDust = (new MetallurgyItem(CoreConfig.ItemGoldDustID, "/shadow/MetallurgyBaseMetals.png")).setIconCoord(4,3).setItemName("GoldDust").setTabToDisplayOn(CreativeTabs.tabMaterials);
 	
 	public static void load()
 	{
 		//Smelting
 		ModLoader.addSmelting(GoldDust.shiftedIndex, new ItemStack(Item.ingotGold, 1));
 			
 		//Crusher
 		BC_CrusherRecipes.smelting().addCrushing(Block.oreGold.blockID, 0, new ItemStack(GoldDust, 2));
 		BC_CrusherRecipes.smelting().addCrushing(Item.ingotGold.shiftedIndex, new ItemStack(GoldDust, 1));
 
 		if(mod_MetallurgyCore.hasFantasy)
 			FF_EssenceRecipes.essence().addEssenceAmount(Item.ingotGold.shiftedIndex, 6);
 		 
 		//RecipeHelper.addBrickRecipes(mod_MetallurgyBaseMetals.BaseMetalsBrick.blockID, meta, Item.ingotGold);
 	}
 }
