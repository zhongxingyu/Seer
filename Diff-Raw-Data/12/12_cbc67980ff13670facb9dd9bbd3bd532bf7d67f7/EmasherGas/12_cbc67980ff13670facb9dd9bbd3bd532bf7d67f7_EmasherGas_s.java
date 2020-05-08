 package emasher.gas;
 
 import java.awt.event.ItemEvent;
 
 import net.minecraftforge.liquids.*;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import buildcraft.api.fuels.IronEngineFuel;
 import buildcraft.api.recipes.*;
 import mods.railcraft.api.fuel.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLInterModComms;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraftforge.client.event.TextureStitchEvent;
 import net.minecraftforge.common.*;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.world.biome.*;
 import net.minecraft.world.*;
 import net.minecraft.world.chunk.*;
 import net.minecraft.block.*;
 import net.minecraft.item.*;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.tileentity.*;
 import net.minecraft.block.material.*;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.*;
 import net.minecraft.entity.player.*;
 import net.minecraft.entity.*;
 import net.minecraft.util.*;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.potion.*;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.fluids.FluidStack;
 import emasher.api.*;
 import emasher.core.*;
 import emasher.core.item.ItemEmasherGeneric;
 import emasher.gas.block.*;
 import emasher.gas.fluids.FluidGas;
 import emasher.gas.item.*;
 import emasher.gas.tileentity.TileDuct;
 import emasher.gas.tileentity.TileGas;
 import emasher.gas.tileentity.TileShaleResource;
 import emasher.gas.worldgen.WorldGenGas;
 import emasher.gas.worldgen.WorldGenGasVent;
 import emasher.gas.worldgen.WorldGenerationUpdater;
 import emasher.sockets.items.ItemBlockSocket;
 import emasher.sockets.SocketsMod;
 import emasher.sockets.items.ItemDusts;
 import buildcraft.BuildCraftEnergy;
 
 
@Mod(modid="gascraft", name="GasCraft", version="2.0.4.0", dependencies = "required-after:eng_toolbox")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, 
 clientPacketHandlerSpec =
 @SidedPacketHandler(channels = {"GasCraft" }, packetHandler = PacketHandler.class),
 serverPacketHandlerSpec =
 @SidedPacketHandler(channels = {"GasCraft" }, packetHandler = PacketHandler.class))
 
 public class EmasherGas
 {
 	@Instance("gascraft")
 	public static EmasherGas instance;
 	
 	@SidedProxy(clientSide="emasher.gas.client.ClientProxy", serverSide="emasher.gas.CommonProxy")
 	public static CommonProxy proxy;
 	
 	//Blocks
 	
 	public static BlockGasGeneric naturalGas;
 	public static BlockGasGeneric propellent;
 	public static BlockGasGeneric hydrogen;
 	public static BlockGasGeneric smoke;
 	public static BlockGasGeneric toxicGas;
 	public static BlockGasGeneric neurotoxin;
 	public static BlockGasGeneric corrosiveGas;
 	public static BlockGasGeneric plasma;
 	
 	public static Block shaleResource;
 	public static Block chimney;
 	
 	public static Block gasPocket;
 	public static Block plasmaPocket;
 	
 	//Items
 	
 	public static Item vial;
 	public static Item vialFilled;
 	public static Item gasMask;
 	public static Item smokeGrenade;
 	public static Item ash;
 	
 	//Fluids
 	
 	public static Fluid fluidNaturalGas;
 	public static Fluid fluidPropellent;
 	public static Fluid fluidHydrogen;
 	public static Fluid fluidSmoke;
 	public static Fluid fluidToxicGas;
 	public static Fluid fluidNeurotoxin;
 	public static Fluid fluidCorrosiveGas;
 	public static Fluid fluidPlasma;
 	
 	public static WorldGenGas gasGenerator = new WorldGenGas();
 	public static WorldGenGasVent gasVentGenerator = new WorldGenGasVent();
 	public static boolean smeltSand;
 	public static boolean spawnMineGas;
 	public static boolean flatBedrock;
 	public static int flatBedrockTop;
 	
 	public int naturalGasID;
 	public int propellentID;
 	public int hydrogenID;
 	public int smokeID;
 	public int toxicGasID;
 	public int neurotoxinID;
 	public int corrosiveGasID;
 	public int plasmaID;
 	
 	public int vialID;
 	public int filledVialID;
 	
 	
 	public int shaleID;
 	public int gasPocketID;
 	public int plasmaPocketID;
 	
 	public int gasMaskID;
 	public int smokeGrenadeID;
 	public int ashID;
 	
 	public int chimneyID;
 	
 	
 	public static int maxGasInVent;
 	public static int minGasInVent;
 	public static boolean infiniteGasInVent;
 	
 	public static CreativeTabs tabGasCraft = new CreativeTabs("tabGasCraft")
 	{
 		public ItemStack getIconItemStack()
 		{
 			return new ItemStack(vialFilled, 1, 0);
 		}
 	};
 	
 	public static String[] dyes =
         {
             "dyeBlack",
             "dyeRed",
             "dyeGreen",
             "dyeBrown",
             "dyeBlue",
             "dyePurple",
             "dyeCyan",
             "dyeLightGray",
             "dyeGray",
             "dyePink",
             "dyeLime",
             "dyeYellow",
             "dyeLightBlue",
             "dyeMagenta",
             "dyeOrange",
             "dyeWhite"
         };
 	
 	static EnumArmorMaterial enumArmorMaterialGas = EnumHelper.addArmorMaterial("Gas", 5, new int[]{1, 3, 2, 1}, 15);
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) 
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		
 		config.load();
 		
 		naturalGasID = config.get(Configuration.CATEGORY_BLOCK,  "Gas Block ID", 2039).getInt();
 		propellentID = config.get(Configuration.CATEGORY_BLOCK, "Propellent ID", 2065).getInt();
 		hydrogenID = config.get(Configuration.CATEGORY_BLOCK, "Hydrogen Block ID", 2097).getInt();
 		smokeID = config.get(Configuration.CATEGORY_BLOCK, "Smoke ID", 2098).getInt();
 		toxicGasID = config.get(Configuration.CATEGORY_BLOCK, "Weaponized Gas Block ID", 2099).getInt();
 		neurotoxinID = config.get(Configuration.CATEGORY_BLOCK, "Neurotoxin ID", 2100).getInt();
 		corrosiveGasID = config.get(Configuration.CATEGORY_BLOCK, "Corrosive Gas ID", 2101).getInt();
 		plasmaID = config.get(Configuration.CATEGORY_BLOCK, "Plasma ID", 2102).getInt();
 		
 		vialID = config.get(Configuration.CATEGORY_ITEM, "Vial ID", 4341).getInt();
 		filledVialID = config.get(Configuration.CATEGORY_GENERAL, "Filled Vial ID", 4342).getInt();
 		
 		shaleID = config.get(Configuration.CATEGORY_BLOCK, "Shale Resource ID", 2040).getInt();
 		gasPocketID = config.get(Configuration.CATEGORY_BLOCK, "Gas Pocket ID", 2112).getInt();
 		plasmaPocketID = config.get(Configuration.CATEGORY_BLOCK, "Plasma Pocket ID", 2113).getInt();
 		
 		gasMaskID = config.get(Configuration.CATEGORY_ITEM, "Gas Mask ID", 4243).getInt();
 		smokeGrenadeID = config.get(Configuration.CATEGORY_ITEM, "Smoke Grenade ID", 4205).getInt();
 		ashID = config.get(Configuration.CATEGORY_ITEM, "Ash ID", 4206).getInt();
 		
 		chimneyID = config.get(Configuration.CATEGORY_BLOCK, "Chimney ID", 2108).getInt();
 		
 		maxGasInVent = config.get(Configuration.CATEGORY_GENERAL, "Max Fluid In Shale Resources (In Buckets)", 50000).getInt();
 		minGasInVent = config.get(Configuration.CATEGORY_GENERAL, "Min Fluid In Shale Resources (In Buckets)", 5000).getInt();
 		infiniteGasInVent = config.get(Configuration.CATEGORY_GENERAL, "Infinite Gas In Vents", false).getBoolean(false);
 		spawnMineGas = config.get(Configuration.CATEGORY_GENERAL, "Spawn Gas Pockets in the world", false).getBoolean(false);
 		flatBedrock = config.get(Configuration.CATEGORY_GENERAL, "Flat Bedrock Compatibility Mode", false).getBoolean(false);
 		flatBedrockTop = config.get(Configuration.CATEGORY_GENERAL, "Flat Bedrock Top Layer", 0).getInt();
 		
 		config.save();
 		
 		ModuleRegistry.addModuleRegistrationManager(new GasModuleRegistrationManager());
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event) 
 	{
 		registerItems();
 		registerBlocks();
 		registerFluids();
 		registerRecipes();
 		registerTileEntities();
 		registerEvents();
 		registerNames();
 		registerWorldGen();
 		
 		LanguageRegistry.instance().addStringLocalization("itemGroup.tabGasCraft", "en_US", "GasCraft");
 		
 		proxy.registerRenderers();
 		
 		registerInRegistry();
 		
 	}
 	
 	private void registerTileEntities()
 	{
 		GameRegistry.registerTileEntity(TileShaleResource.class, "shaleResource");
 		GameRegistry.registerTileEntity(TileGas.class, "gas");
 		GameRegistry.registerTileEntity(TileDuct.class, "chimney");
 		
 		EntityRegistry.registerModEntity(EntitySmokeBomb.class, "smokeGrenade", 1, this, 80, 3, true);
 	}
 
 	
 	private void registerBlocks()
 	{
 		gasPocket = (new BlockMineGas(gasPocketID)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("gasPocket");
		plasmaPocket = (new BlockNetherGas(plasmaPocketID)).setHardness(0.4F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("plasmaPocket");
 		
 		naturalGas = (BlockGasGeneric)new BlockNaturalGas(naturalGasID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("naturalGas").setResistance(0.0F);
 		propellent = (BlockGasGeneric)new BlockPropellent(propellentID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("propellent").setResistance(0.0F);
 		hydrogen = (BlockGasGeneric)new BlockHydrogen(hydrogenID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("hydrogen").setResistance(0.0F);
 		smoke = (BlockGasGeneric)new BlockSmoke(smokeID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("smoke").setResistance(0.0F);
 		toxicGas = (BlockGasGeneric)new BlockWeaponizedGas(toxicGasID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("toxicGas").setResistance(0.0F);
 		neurotoxin = (BlockGasGeneric)new BlockNeurotoxin(neurotoxinID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("neurotoxin").setResistance(0.0F);
 		corrosiveGas = (BlockGasGeneric) new BlockCorrosiveGas(corrosiveGasID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("corrosiveGas").setResistance(0.0F);
 		plasma = (BlockGasGeneric) new BlockPlasma(plasmaID).setBlockUnbreakable().setLightValue(0.0F).setUnlocalizedName("plasma").setResistance(0.0F);
 		
 		shaleResource = new BlockShaleResource(shaleID);
 		Item.itemsList[shaleResource.blockID] = new ItemBlockShaleResource(shaleResource.blockID - 256);
 		LanguageRegistry.instance().addStringLocalization("tile.shaleResource.gas.name", "Shale Gas");
 		LanguageRegistry.instance().addStringLocalization("tile.shaleResource.oil.name", "Shale Oil");
 		LanguageRegistry.instance().addStringLocalization("tile.shaleResource.plasma.name", "Shale Plasma");
 		
 		chimney = new BlockDuct(chimneyID).setResistance(5.0F).setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("chimney");
 		GameRegistry.registerBlock(chimney, "chimney");
 		
 		
 		GameRegistry.registerBlock(naturalGas, "naturalGas");
 		GameRegistry.registerBlock(propellent, "propellent");
 		GameRegistry.registerBlock(hydrogen, "hydrogen");
 		GameRegistry.registerBlock(smoke, "smoke");
 		GameRegistry.registerBlock(toxicGas, "weaponizedGas");
 		GameRegistry.registerBlock(neurotoxin, "neurotoxin");
 		GameRegistry.registerBlock(corrosiveGas, "corrosiveGas");
 		GameRegistry.registerBlock(gasPocket, "gasPocket");
 		GameRegistry.registerBlock(plasma, "plasma");
 		
 		
 		
 	}
 	
 	private void registerFluids()
 	{
 		fluidNaturalGas = new FluidGas("gasCraft_naturalGas", naturalGas, 0);
 		fluidPropellent = new FluidGas("gasCraft_propellent", propellent, 1);
 		fluidHydrogen = new FluidGas("gasCraft_hydrogen", hydrogen, 2);
 		fluidSmoke = new FluidGas("gasCraft_smoke", smoke, 3);
 		fluidToxicGas = new FluidGas("gasCraft_toxicGas", toxicGas, 4);
 		fluidNeurotoxin = new FluidGas("gasCraft_neurotoxin", neurotoxin, 5);
 		fluidCorrosiveGas = new FluidGas("gasCraft_corrosiveGas", corrosiveGas, 6);
 		fluidPlasma = new FluidGas("gasCraft_plasma", plasma, 7);
 		
 		FluidRegistry.registerFluid(fluidNaturalGas);
 		FluidRegistry.registerFluid(fluidPropellent);
 		FluidRegistry.registerFluid(fluidHydrogen);
 		FluidRegistry.registerFluid(fluidSmoke);
 		FluidRegistry.registerFluid(fluidToxicGas);
 		FluidRegistry.registerFluid(fluidNeurotoxin);
 		FluidRegistry.registerFluid(fluidCorrosiveGas);
 		FluidRegistry.registerFluid(fluidPlasma);
 
 		naturalGas.blocksFluid = fluidNaturalGas;
 		propellent.blocksFluid = fluidPropellent;
 		hydrogen.blocksFluid = fluidHydrogen;
 		smoke.blocksFluid = fluidSmoke;
 		toxicGas.blocksFluid = fluidToxicGas;
 		neurotoxin.blocksFluid = fluidNeurotoxin;
 		corrosiveGas.blocksFluid = fluidCorrosiveGas;
 		plasma.blocksFluid = fluidPlasma;
 		
 		registerFluidContainers();
 	}
 	
 	private void registerFluidContainers()
 	{
 		vial = new ItemGasVial(vialID);
 		vialFilled = new ItemGasVialFilled(filledVialID);
 		
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidNaturalGas, 4000), new ItemStack(vialFilled, 1, 0), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidPropellent, 4000), new ItemStack(vialFilled, 1, 1), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidHydrogen, 4000), new ItemStack(vialFilled, 1, 2), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidSmoke, 4000), new ItemStack(vialFilled, 1, 3), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidToxicGas, 4000), new ItemStack(vialFilled, 1, 4), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidNeurotoxin, 4000), new ItemStack(vialFilled, 1, 5), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidCorrosiveGas, 4000), new ItemStack(vialFilled, 1, 6), new ItemStack(vial));
 		FluidContainerRegistry.registerFluidContainer(new FluidStack(fluidPlasma, 4000), new ItemStack(vialFilled, 1, 7), new ItemStack(vial));
 		
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.naturalGas.name", "Natural Gas Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.propellent.name", "Propellent Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.hydrogen.name", "Hydrogen Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.smoke.name", "Smoke Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.toxicGas.name", "Toxic Gas Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.neurotoxin.name", "Neurotoxin Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.corrosiveGas.name", "Corrosive Gas Vial");
 		LanguageRegistry.instance().addStringLocalization("item.gasVialFilled.plasma.name", "Nether Plasma Vial");
 	}
 	
 	private void registerItems()
 	{
 		gasMask = new ItemGasMask(gasMaskID, enumArmorMaterialGas, CommonProxy.ARMOR_GAS, 0);
 		smokeGrenade = new ItemSmokeGrenade(smokeGrenadeID);
 		ash = new ItemEmasherGeneric(ashID, "gascraft:ash", "ash");
 		OreDictionary.registerOre("dustAsh", ash);
 	}
 	
 	private void registerNames()
 	{
 		LanguageRegistry.addName(naturalGas, "naturalGas");
 		LanguageRegistry.addName(propellent, "Propellent");
 		LanguageRegistry.addName(hydrogen, "Hydrogen");
 		LanguageRegistry.addName(smoke, "Smoke");
 		LanguageRegistry.addName(toxicGas, "Weaponized Gas");
 		LanguageRegistry.addName(neurotoxin, "Deadly Neurotoxin");
 		LanguageRegistry.addName(corrosiveGas, "Corrosive Gas");
 		LanguageRegistry.addName(vial, "Empty Gas Vial");
 		LanguageRegistry.addName(gasMask, "Gas Mask");
 		LanguageRegistry.addName(chimney, "Chimney");
 		LanguageRegistry.addName(smokeGrenade, "Smoke Grenade");
 		LanguageRegistry.addName(gasPocket, "Gas Pocket");
 		LanguageRegistry.addName(plasmaPocket, "Nether Plasma Pocket");
 		LanguageRegistry.addName(ash, "Ash");
 	}
 	
 	private void registerRecipes()
 	{
 		PhotobioReactorRecipeRegistry.registerRecipe(new ItemStack(EmasherCore.pondScum), new FluidStack(FluidRegistry.WATER, 1000), new FluidStack(fluidHydrogen, 1000));
 		PhotobioReactorRecipeRegistry.registerRecipe(new ItemStack(EmasherCore.pondScum), new FluidStack(fluidToxicGas, 1000), new FluidStack(fluidNeurotoxin, 500));
 		
 		MixerRecipeRegistry.registerRecipe(new ItemStack(Item.gunpowder), new FluidStack(fluidPropellent, 1000), new FluidStack(fluidToxicGas, 500));
 		MixerRecipeRegistry.registerRecipe(new ItemStack(SocketsMod.dusts, 1, ItemDusts.Const.lime.ordinal()), new FluidStack(fluidPropellent, 100), new FluidStack(fluidCorrosiveGas, 100));
 		
 		FuelManager.addBoilerFuel(fluidNaturalGas, 20000);
 		FuelManager.addBoilerFuel(fluidHydrogen, 20000);
 		FuelManager.addBoilerFuel(fluidPlasma, 100000);
 		
 		IronEngineFuel.addFuel(fluidNaturalGas, 5, 40000);
 		IronEngineFuel.addFuel(fluidHydrogen, 5, 40000);
 		
 		GeneratorFuelRegistry.registerFuel(new FluidStack(fluidNaturalGas, 1000), 500, 60, true);
 		GeneratorFuelRegistry.registerFuel(new FluidStack(fluidHydrogen, 1000), 500, 60, false);
 		
 		if(Loader.isModLoaded("BuildCraft|Core"))
 		{
 			GeneratorFuelRegistry.registerFuel(new FluidStack(BuildCraftEnergy.fluidFuel, 1000), 1000, 60, true);
 		}
 		
 		//TE
 		//Natural Gas
 		NBTTagCompound toSend = new NBTTagCompound();
 		toSend.setString("fluidName", "gasCraft_naturalGas");
 		toSend.setInteger("energy", 30000);
 		FMLInterModComms.sendMessage("ThermalExpansion", "CompressionFuel", toSend);
 		//Natural Gas
 		toSend = new NBTTagCompound();
 		toSend.setString("fluidName", "gasCraft_hydrogen");
 		toSend.setInteger("energy", 30000);
 		FMLInterModComms.sendMessage("ThermalExpansion", "CompressionFuel", toSend);
 		
 		RefineryRecipes.addRecipe(new FluidStack(fluidNaturalGas, 2), new FluidStack(fluidPropellent, 1), 1, 1);
 		
 		FurnaceRecipes.smelting().addSmelting(this.vialFilled.itemID, 0, new ItemStack(vialFilled, 1, 1), 1.0F);
 		
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(vial, 8), "s", "g", "g", Character.valueOf('g'), Block.glass, Character.valueOf('s'), Item.silk));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(gasMask), "lll", "lgl", "vwv", Character.valueOf('g'), Block.glass, Character.valueOf('l'), Item.leather,
 				Character.valueOf('v'), vial, Character.valueOf('w'), Block.cloth));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(smokeGrenade), " i ", "isi", " i ", Character.valueOf('i'), Item.ingotIron, Character.valueOf('s'), new ItemStack(vialFilled, 1, 3)));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(smokeGrenade), " i ", "isi", " i ", Character.valueOf('i'), "ingotAluminum", Character.valueOf('s'), new ItemStack(vialFilled, 1, 3)));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(smokeGrenade), " i ", "isi", " i ", Character.valueOf('i'), "ingotTin", Character.valueOf('s'), new ItemStack(vialFilled, 1, 3)));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(chimney), " i ", "i i", " i ", Character.valueOf('i'), Item.brick));
 		
 		for(int i = 0; i < 16; i++)
 		{
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(SocketsMod.paintCans[i], 1),
 						"i", "p", "d", Character.valueOf('i'), Block.stoneButton, Character.valueOf('p'), new ItemStack(vialFilled, 1, 1), Character.valueOf('d'), dyes[i])
 					);
 		}
 	}
 	
 	
 	private void registerEvents()
 	{
 		MinecraftForge.EVENT_BUS.register(new WorldGenerationUpdater());
 	}
 	
 	private void registerWorldGen()
 	{
 		if(this.spawnMineGas)GameRegistry.registerWorldGenerator(gasGenerator);
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		FluidStack ethanol = FluidRegistry.getFluidStack("bioethanol", 1000);
 		if(ethanol != null) GeneratorFuelRegistry.registerFuel(ethanol, 750, 40, false);
 	}
 	
 	private void registerInRegistry()
 	{
 		Registry.addBlock("shaleResource", this.shaleResource);
 		Registry.addBlock("chimney", this.chimney);
 		Registry.addBlock("naturalGas", this.naturalGas);
 		Registry.addBlock("propellent", this.propellent);
 		Registry.addBlock("hydrogen", this.hydrogen);
 		Registry.addBlock("smoke", this.smoke);
 		Registry.addBlock("toxicGas", this.toxicGas);
 		Registry.addBlock("neurotoxin", this.neurotoxin);
 		Registry.addBlock("plasma", this.plasma);
 		
 		Registry.addItem("vialEmpty", this.vial);
 		Registry.addItem("vialFilled", this.vialFilled);
 		Registry.addItem("gasMask", this.gasMask);
 		Registry.addItem("smokeGrenade", this.smokeGrenade);
 		Registry.addItem("ash", this.ash);
 		
 		
 		
 	}
 	
 	
 	
 }
