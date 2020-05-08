 package shadow.mods.metallurgy;
 import java.io.File;
 import org.lwjgl.opengl.GL11;
 
 import shadow.mods.metallurgy.*;
 import shadow.mods.metallurgy.base.FurnaceUpgradeRecipes;
 import shadow.mods.metallurgy.base.MetallurgyBaseMetals;
 //import vazkii.um.common.ModConverter;
 //import vazkii.um.common.UpdateManagerMod;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 
 import java.util.Iterator;
 import java.util.Random;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.Block;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 
 @Mod(modid = "MetallurgyCore", name = "Metallurgy Core", version = "2.0.7.1")
 @NetworkMod(channels = { "MetallurgyCore" }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class )
 public class MetallurgyCore
 {
 	@SidedProxy(clientSide = "shadow.mods.metallurgy.CoreClientProxy", serverSide = "shadow.mods.metallurgy.CoreCommonProxy")
 	public static CoreCommonProxy proxy;
 
 	@Instance( value = "MetallurgyCore" )
 	public static MetallurgyCore instance;
 	
 	public static CoreConfig config;
 	
 	public static boolean hasBase;
 	public static boolean hasNether;
 	public static boolean hasPrecious;
 	public static boolean hasFantasy;
 	public static boolean hasEnder;
 
 	public static Block crusher;
 	public static Block vanillaBricks;
 	
 	public static Block smoke;
 	public static Block smokeInactive;
 	public static Block smokeEater;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		config.init();
 		crusher = new BC_BlockCrusher(CoreConfig.crusherID, false).setHardness(3.5F).setBlockName("Crusher").setCreativeTab(CreativeTabs.tabDeco);
 		vanillaBricks = new MetallurgyBlock(CoreConfig.vanillaBrickID, "/shadow/VanillaBricks.png", 2, 1).setHardness(2F).setResistance(.1F).setBlockName("VanillaBrick");
 		
 		proxy.registerRenderInformation();
 
 		try {
 			Class a = Class.forName("shadow.mods.metallurgy.base.MetallurgyBaseMetals");
 	    	hasBase = true;
     		System.out.println("Metallurgy Core: Base Metals detected, comapatibility added");
     	} catch(ClassNotFoundException e) {
     		System.out.println("Metallurgy Core: Base Metals not detected, reason: " + e);
     	}
 		try {
 			Class a = Class.forName("shadow.mods.metallurgy.nether.MetallurgyNether");
 	    	hasNether = true;
     		System.out.println("Metallurgy Core: Nether detected, comapatibility added");
     	} catch(ClassNotFoundException e) {
     		System.out.println("Metallurgy Core: Nether not detected, reason: " + e);
     	}
 		try {
 			Class a = Class.forName("shadow.mods.metallurgy.precious.MetallurgyPrecious");
 	    	hasPrecious = true;
     		System.out.println("Metallurgy Core: Precious detected, comapatibility added");
     	} catch(ClassNotFoundException e) {
     		System.out.println("Metallurgy Core: Precious not detected, reason: " + e);
     	}
 		try {
 			Class a = Class.forName("shadow.mods.metallurgy.fantasy.MetallurgyFantasy");
 	    	hasFantasy = true;
     		System.out.println("Metallurgy Core: Fantasy detected, comapatibility added");
     	} catch(ClassNotFoundException e) {
     		System.out.println("Metallurgy Core: Fantasy not detected, reason: " + e);
     	}
 		try {
			Class a = Class.forName("shadow.mods.metallurgy.fantasy.MetallurgyEnder");
 	    	hasEnder = true;
     		System.out.println("Metallurgy Core: Ender detected, comapatibility added");
     	} catch(ClassNotFoundException e) {
     		System.out.println("Metallurgy Core: Ender not detected, reason: " + e);
     	}
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) 
 	{
 		
 		mod_Iron.load();
 		mod_Gold.load();
 		
 		GameRegistry.registerBlock(vanillaBricks, MetallurgyItemBlock.class);
 		
 		GameRegistry.registerWorldGenerator(new CoreWorldGen());
 		
 		OreDictionary.registerOre("dustGold", new ItemStack(mod_Gold.GoldDust, 1));
 		OreDictionary.registerOre("dustIron", new ItemStack(mod_Iron.IronDust, 1));
 		
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 
 		proxy.addNames();
 		proxy.registerRenderInformation();
 		proxy.registerTileEntitySpecialRenderer();
 		if(CoreConfig.enableTextureOverrides)
 			proxy.addTextureOverrides();
 		ModLoader.registerBlock(crusher, shadow.mods.metallurgy.BC_BlockCrusherItem.class);
 		ModLoader.registerTileEntity(BC_TileEntityCrusher.class, "crusher");
 		
 		//mod_MetallurgyzAchievements.load();
 		
 	    RecipeHelper.addBrickRecipes(vanillaBricks.blockID, 0, Item.ingotIron);
 	    RecipeHelper.addBrickRecipes(vanillaBricks.blockID, 1, Item.ingotGold);
 	    
 		if(CoreConfig.crushersEnabled)
 			CrusherUpgradeRecipes.load();
 		
 	    BC_CrusherRecipes.smelting().addCrushing(Block.cobblestone.blockID, new ItemStack(Block.sand));
 	    BC_CrusherRecipes.smelting().addCrushing(Block.gravel.blockID, new ItemStack(Item.flint));
 
 		try {
 			Class a = Class.forName("ic2.api.Ic2Recipes");
 			ItemStack circuit = ic2.api.Items.getItem("electronicCircuit");
 			ItemStack macerator = ic2.api.Items.getItem("macerator");
 			GameRegistry.addRecipe(macerator, new Object[] {
 	    			" I ", "IFI", "ICI", Character.valueOf('I'), Item.ingotIron, Character.valueOf('C'), circuit, Character.valueOf('F'), new ItemStack(crusher, 1, 1)
 	    		});
 			GameRegistry.addRecipe(macerator, new Object[] {
 	    			" I ", "IFI", " C ", Character.valueOf('I'), Item.ingotIron, Character.valueOf('C'), circuit, Character.valueOf('F'), new ItemStack(crusher, 1, 2)
 	    		});
 			GameRegistry.addRecipe(macerator, new Object[] {
     			"F", "C", Character.valueOf('C'), circuit, Character.valueOf('F'), new ItemStack(crusher, 1, 3)
     		});
 			GameRegistry.addRecipe(macerator, new Object[] {
 	    		"F", "C", Character.valueOf('C'), circuit, Character.valueOf('F'), new ItemStack(crusher, 1, 4)
     		});
 
 			ItemStack coalDust = ic2.api.Items.getItem("coalDust");
 			if(coalDust != null)
 				BC_CrusherRecipes.smelting().addCrushing(Item.coal.shiftedIndex, 0, coalDust);
 		} catch(Exception e) {}
 		
 	}
 }
