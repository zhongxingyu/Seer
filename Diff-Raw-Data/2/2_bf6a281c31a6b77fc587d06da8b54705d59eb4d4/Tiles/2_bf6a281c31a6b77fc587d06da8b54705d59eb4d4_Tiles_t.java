 package LightProcessing.common.lib;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import LightProcessing.common.lightProcessing.tile.*;
 
 public class Tiles {
 	public static void registerTiles() {
 		GameRegistry.registerTileEntity(TileEntityHarvester.class, "Harvester");
 		GameRegistry.registerTileEntity(TileEntityCompressor.class, "Abs Comp");
 		GameRegistry.registerTileEntity(TileEntityDarkGen.class, "Dark Gen");
		GameRegistry.registerTileEntity(TileEntityMachineCore.class, "Machine Core");
		GameRegistry.registerTileEntity(TileEntityExtractor.class, "Extractor");
 	}
 }
