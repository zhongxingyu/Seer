 package mrkirby153.MscHouses.core;
 
 import mrkirby153.MscHouses.lib.Strings;
import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Localization {
 	
 	public static void localize(){
 		//Item names
 		System.out.println(StatCollector.translateToLocal(Strings.ITEM_DEBUG_NAME));
 	//	LanguageRegistry.addName(MscHouses.Debug, StatCollector.translateToLocal(Strings.ITEM_DEBUG_NAME));
 	//	LanguageRegistry.addName(MscHouses.Debug, "test");
 		LanguageRegistry.addName(MscHouses.ingotCopper, StatCollector.translateToLocal(Strings.ITEM_INGOT_COPPER_NAME));
 		LanguageRegistry.addName(MscHouses.HouseTool, StatCollector.translateToLocal(Strings.ITEM_HOUSE_TOOL_NAME));
 		LanguageRegistry.addName(MscHouses.PCB, StatCollector.translateToLocal(Strings.ITEM_PCB_NAME));
 		LanguageRegistry.addName(MscHouses.moduel, StatCollector.translateToLocal(Strings.ITEM_MODUEL_NAME));
 		LanguageRegistry.addName(MscHouses.modifyer, StatCollector.translateToLocal(Strings.ITEM_MATERIAL_MODIFYER_NAME));
 		LanguageRegistry.addName(MscHouses.infiniteDimensions, StatCollector.translateToLocal(Strings.ITEM_INFINITE_DIM_NAME));
 		
 		//Block Names
 		LanguageRegistry.addName(MscHouses.OreCopper, StatCollector.translateToLocal(Strings.BLOCK_ORE_COPPER_NAME));
 		LanguageRegistry.addName(MscHouses.BlockHouseGenerator, StatCollector.translateToLocal(Strings.BLOCK_HOUSE_GEN_NAME));
 		
 		//Localizations
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MscHouses-main", StatCollector.translateToLocal(Strings.TAB_MAIN));
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MscHouses-Moduel", StatCollector.translateToLocal(Strings.TAB_MODUEL));
 		LanguageRegistry.instance().addStringLocalization("itemGroup.MscHouses-Modifyers", StatCollector.translateToLocal(Strings.TAB_MODIFYER));
 		
 	}
 
 }
