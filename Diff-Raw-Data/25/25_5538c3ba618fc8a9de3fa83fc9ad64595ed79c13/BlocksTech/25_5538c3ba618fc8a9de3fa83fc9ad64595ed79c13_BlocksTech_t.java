 package mcextreme.tech.block;
 
 import mcextreme.core.MCExtremeCore;
 import mcextreme.core.config.MCExtremeConfig;
 import mcextreme.tech.item.ItemBlockTech;
 import mcextreme.tech.tileentity.TileEntityCompressor;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class BlocksTech
 {
     public static Block blockOre, blockStorage, blockBasicMachine, blockCompressor, blockCompressorActive;
     
     public static void initBlocks()
     {
         blockOre = new BlockOreTech(MCExtremeConfig.blockTechOreID, "ORE", Material.rock).setHardness(3.0F).setResistance(1.5F).setUnlocalizedName("blockTechOre");
         blockStorage = new BlockOreStorageTech(MCExtremeConfig.blockTechStorageID, "STORAGE", Material.iron).setHardness(3.0F).setResistance(1.5F).setUnlocalizedName("blockTechStorage");
         blockBasicMachine = new BlockBasicMachine(MCExtremeConfig.blockBasicMachineID).setHardness(3.5F).setUnlocalizedName("blockBasicMachine").setCreativeTab(MCExtremeCore.tabTech);
         blockCompressor = new BlockCompressor(MCExtremeConfig.blockCompressorID, false).setHardness(3.5F).setCreativeTab(MCExtremeCore.tabTech);
         blockCompressorActive = new BlockCompressor(MCExtremeConfig.blockCompressorActiveID, true).setHardness(3.5F).setLightValue(1.0F);
         
         Item.itemsList[MCExtremeConfig.blockTechOreID] = new ItemBlockTech(MCExtremeConfig.blockTechOreID).setUnlocalizedName("blockOreID");
         Item.itemsList[MCExtremeConfig.blockTechStorageID] = new ItemBlockTech(MCExtremeConfig.blockTechStorageID).setUnlocalizedName("blockStorageID");
         Item.itemsList[MCExtremeConfig.blockBasicMachineID] = new ItemBlockTech(MCExtremeConfig.blockBasicMachineID).setUnlocalizedName("blockBasicMachine");
        
        
        GameRegistry.registerBlock(blockBasicMachine, ItemBlock.class, null);
        GameRegistry.registerBlock(blockCompressor, "Compressor");
        GameRegistry.registerBlock(blockCompressorActive, "You Broke ME");
        
        GameRegistry.registerTileEntity(TileEntityCompressor.class, "tileEntityCompressor");
        
        Item.itemsList[MCExtremeConfig.blockBasicMachineID] = new ItemBlockTech(MCExtremeConfig.blockBasicMachineID).setUnlocalizedName("blockBasicMachine");
        
     }
 }
