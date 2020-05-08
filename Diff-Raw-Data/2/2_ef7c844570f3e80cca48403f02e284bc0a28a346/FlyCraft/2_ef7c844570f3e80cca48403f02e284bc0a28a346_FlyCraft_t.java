 package Fly_Craft;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 
 @Mod(modid="JJMACCA_FlyCraft",name="Fly Craft",version="1.0")
 @NetworkMod(clientSideRequired=true,serverSideRequired=false)
 
 
 public class FlyCraft {
 	
 	//Mod ID
 	public static final String modid = "JJMACCA_FlyCraft";
 	
 	//Items
 	public static Item IngotSteel;
 	public static Item PlaceHolder;
 	public static Item SteelWings;
 	public static Item LiquidSteel;
 	
 	//Config ID's
 	int ItemIngotSteelID;
 	int ItemPlaceHolderID;
 	int ItemSteelWingsID;
 	int ItemLiquidSteelID;
 	
 	//Creative Tab
 	public static CreativeTabs TabFlyCraft = new TabFlyCraft(CreativeTabs.getNextID(), "Fly Craft");
 	
 	//Proxy
 	@SidedProxy(clientSide = "Fly_Craft.FlyCraftClient", serverSide = "Fly_Craft.FlyCraftProxy" )
 	public static FlyCraftProxy proxy;
 	
 	@PreInit
 	public void PreLoad(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		
 		config.load();
 		
 		ItemIngotSteelID = config.get(Configuration.CATEGORY_ITEM, "Steel Ingot ID", 500).getInt();
 		ItemPlaceHolderID = config.get(Configuration.CATEGORY_ITEM, "Place Holder ID", 501).getInt();
 		ItemSteelWingsID = config.get(Configuration.CATEGORY_ITEM, "Steel Wings ID", 502).getInt();
 		ItemLiquidSteelID = config.get(Configuration.CATEGORY_ITEM, "Liquid Steel ID", 503).getInt();
 		
 		config.save();
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		//Texture Functions
 		proxy.registerRenderInformation();
 		
 		//Steel Ingot
 		IngotSteel = new ItemIngotSteel(ItemIngotSteelID).setUnlocalizedName("IngotSteel");
 		LanguageRegistry.addName(IngotSteel, "Steel Ingot");
 		IngotSteel.setCreativeTab(TabFlyCraft);
 		GameRegistry.addRecipe(new ItemStack(IngotSteel, 3), new Object [] { "yyy", "xxx", "yyy", 
 			Character.valueOf('y'), Item.coal,Character.valueOf('x'), Item.ingotIron});
 		
 		
 		//Item Place Holder
 		PlaceHolder = new ItemPlaceHolder(ItemPlaceHolderID).setUnlocalizedName("PlaceHolder");
 				
 	
 		//Steel Wings
 	    SteelWings = new ItemSteelWings(ItemSteelWingsID).setUnlocalizedName("SteelWings");
 	    LanguageRegistry.addName(SteelWings, "Steel Wings");
 	    SteelWings.setCreativeTab(TabFlyCraft);
 	    
 	    //Liquid Steel
 	    LiquidSteel = new ItemLiquidSteel(ItemLiquidSteelID).setUnlocalizedName("LiquidSteel");
 	    LanguageRegistry.addName(LiquidSteel, "Liquid Steel");
 	    LiquidSteel.setCreativeTab(TabFlyCraft);
	    GameRegistry.addSmelting(IngotSteel.itemID, new ItemStack(LiquidSteel), 10F);
 	}
 
 	}
 
