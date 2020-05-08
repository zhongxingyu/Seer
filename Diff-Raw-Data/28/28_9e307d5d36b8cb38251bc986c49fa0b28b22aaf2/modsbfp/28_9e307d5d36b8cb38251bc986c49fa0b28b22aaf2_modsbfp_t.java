 package sbfp;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import sbfp.chemistry.ItemDye;
 import sbfp.machines.BlockMachines;
 import sbfp.machines.ItemBlockMachines;
 import sbfp.machines.ItemRedflux;
 import sbfp.world.BlockOre;
 import sbfp.world.ItemBlockOre;
 import sbfp.world.WorldGenOres;
 import sbfp.world.WorldGenerator;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = modsbfp.modid, name = modsbfp.shortname, version = modsbfp.version)
 @NetworkMod(channels = {modsbfp.modid}, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
 public class modsbfp{
 
 	// Logger
 	public static final Logger logger = Logger.getLogger("Minecraft");
 	// name constants
 	public static final String modid = "sbfp";
 	public static final String shortname = "SBFP Tech";
 	public static final String version = "Aleph NEGATIVE TEN MILLION";
 
 	// mechanics constants
 	@Instance(modid)
 	private static modsbfp instance;
 	private WorldGenerator wGen;
 	private static final Configuration config = new Configuration(new File("config/SBFP/SBFP.cfg"));
 	
 	@SidedProxy(clientSide = "sbfp.client.SBClientProxy", serverSide = "sbfp.SBCommonProxy")
 	public static SBCommonProxy proxy;
 
 	//block and item name
 	public static final String[][] redFluxNames = new String[][]{{"redFluxAmp","redFluxAbsorber", "redFluxStablizer", "chargedRedstone"},{"Redstone Flux Amplifier", "Redstone Flux Absorber", "Redstone Flux Stablilizer", "Charged Redstone"}};
	public static final String[][] oreNames = new String[][]{{"oreThorium","oreFluorite","oreMoS2","oreRutile"}, {"Monazite Sand", "Fluorite", "Molybdenite","Rutile"}};
 	public static final String[][] dyeNames = new String[][]{{"dye1", "dye2"}, {"color", "moarcolor"}};
 	public static final String[][] machineNames = new String[][]{{"solarCharger"},{"Sunlight Collector"}};
 	// blocks and items
 	public static final BlockOre blockOre = new BlockOre(getBlockID("blockOreID",0x4c0));
	public static final BlockMachines blockMachines = new BlockMachines(getBlockID("blockMachinesID", 0x4c3));
 	public static final ItemRedflux itemRedflux = new ItemRedflux(getItemID("itemRedfluxID",0x4c1));
	//public static final ItemDye itemDye = new ItemDye(getItemID("itemDyeID", 0x4c2));
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event){
 		instance = this;
 		wGen = new WorldGenerator();
 	}
 	
 	@Init
 	public void init(FMLInitializationEvent event){
 		GameRegistry.registerBlock(blockOre,ItemBlockOre.class,"blockOre");
 		GameRegistry.registerBlock(blockMachines, ItemBlockMachines.class, "blockMachines");
 		GameRegistry.registerItem(itemRedflux,"itemRedflux");
 		for(int i = 0; i<blockOre.names.length; i++){
 			LanguageRegistry.addName(new ItemStack(blockOre.blockID,1,i),oreNames[1][i]);
 		}
 		for(int i = 0; i<itemRedflux.names.length; i++){
 			LanguageRegistry.addName(new ItemStack(itemRedflux.itemID,1,i),redFluxNames[1][i]);
 		}
		for(int i = 0; i<blockMachines.names.length; i++){
			LanguageRegistry.addName(new ItemStack(blockMachines.blockID,1,i),machineNames[1][i]);
		}
 		this.addWorldGeneration();
 		GameRegistry.registerWorldGenerator(this.wGen);
 		System.out.println("AddedWGen");
 		
 	}
 
 	private void addWorldGeneration(){
 		this.wGen.addOre(new WorldGenOres(blockOre.blockID, 0, 12, 40, 100, 100, Block.stone.blockID));
 		this.wGen.addOre(new WorldGenOres(blockOre.blockID, 1, 12, 40, 14, 4, Block.stone.blockID));
 		this.wGen.addOre(new WorldGenOres(blockOre.blockID, 2, 12, 40, 5, 2, Block.stone.blockID));
 		this.wGen.addOre(new WorldGenOres(blockOre.blockID, 3, 12, 40, 14, 6, Block.stone.blockID));
 		
 	}
 	
 	private void addRecipes(){
 		
 	}
 
 	private static int getBlockID(String name, int defaultid){
 		config.load();
 		Property q = config.get(Configuration.CATEGORY_BLOCK,name,defaultid);
 		return q.getInt(defaultid);
 	}
 
 	private static int getItemID(String name, int defaultid){
 		config.load();
 		Property q = config.get(Configuration.CATEGORY_ITEM,name,defaultid);
 		return q.getInt(defaultid);
 	}
 	
 	public static modsbfp getInstance(){
 		return instance;
 	}
 }
