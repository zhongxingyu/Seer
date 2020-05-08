 package assets.tacotek.blocks;
 
 import assets.tacotek.common.IDsHelper;
 import assets.tacotek.common.tacotek;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class BlocksHelper {
 	//Blocks
 	public static Block tacoBox;
 	
 	public static void setupBlocks() {
 		//Normal Blocks
 		tacoBox = new TacoBox(IDsHelper.tacoBoxID, "tacobox").setUnlocalizedName("tacobox").setCreativeTab(tacotek.tacotekTab);
 		//siliconOre = new BlockOre(IDsHelper.siliconOreID).setHardness(10F).setResistance(0.2F)
         //			   .setUnlocalizedName("SiliconOre").setCreativeTab(tacotek.tacotekTab);
 		
 		//Machines
 		
 		//Liquids
 		
 		//Other
 		
 		gameRegisters();
 		languageRegistry();
         oreDictionary();
 	}
 	
 	private static void gameRegisters() {
         // Block Registry
 		GameRegistry.registerBlock(tacoBox, tacotek.modID + tacoBox.getUnlocalizedName() );
 	}
 	
	public static void languageRegistry() {
 		//Block Name Registry
 		LanguageRegistry.addName(tacoBox, "Taco Box");
 	}
 	
 	private static void oreDictionary() {
	     //OreDictionary.registerOre("oreSilicon", siliconOre);
 	}
 }
