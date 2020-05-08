 package mcextreme.tech.block;
 
 import mcextreme.core.MCExtremeCore;
 import mcextreme.core.config.MCExtremeConfig;
 import mcextreme.core.utils.MCExtremeUtils;
 import mcextreme.tech.item.ItemBlockTech;
 import mcextreme.tech.tileentity.TileEntityCompressor;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockGlass;
 import net.minecraft.block.BlockPane;
 import net.minecraft.block.BlockWall;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraftforge.fluids.Fluid;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class BlocksTech
 {	
     public static Block blockOre, blockStorage, blockBreaker, blockPlacer, blockCompressor, blockCompressorActive, blockRebar;
     
     public static Block blockLiquidConcrete, blockSolidConcrete;
     
     public static Fluid fluidConcrete;
     
     public static void initBlocks()
     {	
     	fluidConcrete = new FluidConcrete().setBlockID(MCExtremeConfig.blockLiquidConcreteID);
     	
         blockOre = new BlockOreTech(MCExtremeConfig.blockTechOreID, "ORE", Material.rock).setHardness(3.0F).setResistance(1.5F).setUnlocalizedName("blockTechOre");
         blockStorage = new BlockOreStorageTech(MCExtremeConfig.blockTechStorageID, "STORAGE", Material.iron).setHardness(3.0F).setResistance(1.5F).setUnlocalizedName("blockTechStorage");
         blockBreaker = new BlockBasicMachine(MCExtremeConfig.blockBreakerID, "BREAKER").setHardness(3.5F).setUnlocalizedName("blockBreaker").setCreativeTab(MCExtremeCore.tabTech);
         blockPlacer = new BlockBasicMachine(MCExtremeConfig.blockPlacerID, "PLACER").setHardness(3.5F).setUnlocalizedName("blockPlacer").setCreativeTab(MCExtremeCore.tabTech);
         blockCompressor = new BlockCompressor(MCExtremeConfig.blockCompressorID, false).setHardness(3.5F).setCreativeTab(MCExtremeCore.tabTech);
         blockCompressorActive = new BlockCompressor(MCExtremeConfig.blockCompressorActiveID, true).setHardness(3.5F).setLightValue(1.0F);
         blockLiquidConcrete = new BlockLiquidConcrete(MCExtremeConfig.blockLiquidConcreteID).setUnlocalizedName("liquidConcrete").setCreativeTab(MCExtremeCore.tabTech);
         blockRebar = new BlockWall(MCExtremeConfig.blockRebarID, Block.anvil).setUnlocalizedName("rebar").setCreativeTab(MCExtremeCore.tabTech);
         blockSolidConcrete = new Block(MCExtremeConfig.blockSolidConcreteID, Material.rock).setUnlocalizedName("solidconcrete").setCreativeTab(MCExtremeCore.tabTech);
         
         Item.itemsList[MCExtremeConfig.blockTechOreID] = new ItemBlockTech(MCExtremeConfig.blockTechOreID).setUnlocalizedName("blockOreID");
         Item.itemsList[MCExtremeConfig.blockTechStorageID] = new ItemBlockTech(MCExtremeConfig.blockTechStorageID).setUnlocalizedName("blockStorageID");
         
        MCExtremeUtils.registerDefaultBlocks(blockBreaker, blockPlacer, blockCompressor, blockCompressorActive, blockRebar, blockLiquidConcrete);
         
         GameRegistry.registerTileEntity(TileEntityCompressor.class, "tileEntityCompressor");
        MCExtremeUtils.registerDefaultBlock(blockSolidConcrete);
     }
 }
