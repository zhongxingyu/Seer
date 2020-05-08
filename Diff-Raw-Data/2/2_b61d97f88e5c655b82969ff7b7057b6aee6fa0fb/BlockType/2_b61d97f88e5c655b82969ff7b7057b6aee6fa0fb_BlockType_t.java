 package ip.industrialProcessing.utils.registry;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public enum BlockType {
     Block, Machine, Tier0, Tier1, Power, Refinary, Smelting, Ore_Processing, assemble, Dummy, Transport, Storage, fluid, structure, decoration, logic;
 
     public static void registerNames() {
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + Ore_Processing.toString(), "en_US", "Ore processing");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + Smelting.toString(), "en_US", "Smelting");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + Power.toString(), "en_US", "Power");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + Refinary.toString(), "en_US", "Refinary");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + assemble.toString(), "en_US", "Assemble");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + fluid.toString(), "en_US", "Fluids");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + structure.toString(), "en_US", "Structure");
 	LanguageRegistry.instance().addStringLocalization("IP.BlockType." + decoration.toString(), "en_US", "Decoration");
     }
 
     public String getDisplayName() {
	return LanguageRegistry.instance().getStringLocalization("IP.BlockType." + this.toString(),"en_US");
     }
 }
