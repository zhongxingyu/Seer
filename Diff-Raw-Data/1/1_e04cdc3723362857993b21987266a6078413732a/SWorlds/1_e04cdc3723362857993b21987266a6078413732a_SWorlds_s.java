 package deepcraft.core;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import deepcraft.world.*;
 import deepcraft.world.deepnether.*;
 import deepcraft.world.endurai.*;
 import deepcraft.world.xirk.*;
 import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.DimensionManager;
 
 public class SWorlds {
 	
 	public static BiomeGenBase biomeEndurai = new BiomeEndurai(90).setBiomeName("Endurai");
 	public static BiomeGenBase biomeXirk = new BiomeXirk(91).setBiomeName("Xirk");
 	public static BiomeGenBase biomeDeepNether = new BiomeDeepNether(92).setBiomeName("Deep Nether");
 	
 	public static int enduraiID = 40;
 	public static int xirkID = 41;
 	public static int deepNetherID = 42;
 	public static int valaID = 43;
 	public static int sciID = 44;
 	public static int godusID = 45;
 	public static int limboID = 46;
 	
 	public static void SetupBiomes() {
 		//GameRegistry.addBiome(biomeEndurai);
 	}
 	
 	public static void SetupWorlds() {
 		
 		DimensionManager.registerProviderType(enduraiID, WorldProviderEndurai.class, false);
 		DimensionManager.registerDimension(enduraiID, enduraiID);
 		DimensionManager.registerProviderType(xirkID, WorldProviderXirk.class, false);
 		DimensionManager.registerDimension(xirkID, xirkID);
 		DimensionManager.registerProviderType(deepNetherID, WorldProviderDeepNether.class, false);
 		DimensionManager.registerDimension(deepNetherID, deepNetherID);
 		
 	}
 	
 	public static void SetupOres() {
 		WorldsGeneratorSensMod WGSM = new WorldsGeneratorSensMod();
 		
 		WGSM.SetupStandardSurfOre(SBlocks.oreEndurum.blockID, 2, 4, 8, 32);
 		WGSM.SetupStandardSurfOre(SBlocks.oreXircium.blockID, 1, 4, 8, 24);
 		WGSM.SetupStandardSurfOre(SBlocks.oreNetherite.blockID, 1, 4, 4, 16);
 		WGSM.SetupStandardSurfOre(SBlocks.oreValarium.blockID, 1, 4, 6, 8);
 		WGSM.SetupStandardSurfOre(SBlocks.oreScinite.blockID, 2, 4, 4, 6);
 		WGSM.SetupStandardSurfOre(SBlocks.oreGodum.blockID, 1, 4, 4, 6);
 		WGSM.SetupStandardSurfOre(Block.lavaMoving.blockID, 2, 4, 16, 48);
 
 		WGSM.SetupHellOre(SBlocks.oreGodumNether.blockID, 4, 4, 4, 31);
 		WGSM.SetupHellOre(SBlocks.oreSciniteNether.blockID, 8, 4, 4, 31);
 		WGSM.SetupHellOre(SBlocks.oreValariumNether.blockID, 1, 4, 24, 47);
 		WGSM.SetupHellOre(SBlocks.oreNetheriteNether.blockID, 32, 2, 16, 127);
 		WGSM.SetupHellOre(SBlocks.oreXirciumNether.blockID, 8, 4, 8, 127);
 		WGSM.SetupHellOre(SBlocks.oreEndurumNether.blockID, 2, 8, 64, 127);
 		WGSM.SetupHellOre(SBlocks.oreCoalNether.blockID, 40, 4, 16, 127);
 		WGSM.SetupHellOre(SBlocks.oreIronNether.blockID, 40, 4, 8, 63);
 		WGSM.SetupHellOre(SBlocks.oreGoldNether.blockID, 4, 4, 16, 47);
 		WGSM.SetupHellOre(SBlocks.oreDiamondNether.blockID, 8, 4, 7, 31);
 		WGSM.SetupHellOre(SBlocks.oreRedstoneNether.blockID, 32, 8, 16, 127);
 		WGSM.SetupHellOre(SBlocks.oreLapisNether.blockID, 4, 4, 6, 47);
 		WGSM.SetupHellOre(SBlocks.oreEmeraldNether.blockID, 8, 4, 7, 31);
 		WGSM.SetupHellOre(Block.lavaMoving.blockID, 8, 8, 48, 127);
 		
 		WGSM.SetupEnduraiOre(SBlocks.oreEndurum.blockID, 128, 16, 64, 127);
 		
 		WGSM.SetupXirkOre(SBlocks.oreXirciumXirk.blockID, 3, 64, 128, 56);
 		
 		WGSM.SetupDeepNetherOre(SBlocks.oreNetheriteDeepNether.blockID, 128, 8, 16, 127);
 		WGSM.SetupDeepNetherOre(SBlocks.lavaBlueMoving.blockID, 4, 16, 64, 127);
 		
 		GameRegistry.registerWorldGenerator(WGSM);
 	}
 
 }
