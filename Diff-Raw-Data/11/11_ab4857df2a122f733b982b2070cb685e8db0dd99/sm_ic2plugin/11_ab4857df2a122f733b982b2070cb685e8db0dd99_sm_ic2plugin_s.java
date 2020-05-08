 package mods.storemore;
   
 import mods.storemore.blocks.blockitems.ic2blocksIItem;
 import mods.storemore.handlers.SProxy;
 import mods.storemore.handlers.StoreMoreTab;
 import mods.storemore.handlers.sm_config;
 import mods.storemore.handlers.sm_modCompatability;
 import mods.storemore.handlers.sm_naming;
 import mods.storemore.handlers.sm_recipes;
 import mods.storemore.ic2.api.Items;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
   
 @Mod(modid = sm_ic2plugin.modId, name = sm_ic2plugin.modName, version = sm_ic2plugin.version, dependencies = "required-after:StoreMore;after:IC2")
 
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class sm_ic2plugin
 
 
 {
 	
 	public static final String modId = "StoreMoreIC2";
 	public static final String modName = "Store More IC2 Plugin";
 	public static final String version = "0.1";
 	public static final String parent = "storemoreMain";
 
 	@SidedProxy(clientSide = "mods.storemore.client.CProxy", serverSide = "mods.storemore.common.SProxy")
 		public static SProxy proxy;
 
 	private static ItemStack copperBlock;
     private static ItemStack copperBlock2;
     private static ItemStack tinBlock;
     private static ItemStack tinBlock2;
     private static ItemStack uraniumBlock;
     private static ItemStack uraniumBlock2;
     private static ItemStack bronzeBlock;
     private static ItemStack bronzeBlock2;
     
 	public static Block ic2blocksI;
    
 
 	@PreInit()
     public void preInit(FMLPreInitializationEvent event) {
 
 	      sm_config.init();
           
 	        sm_config.initialize(event.getSuggestedConfigurationFile());
 	          
 	        sm_config.save(); 
 		
 	}
 	@Init
 	public void load(FMLInitializationEvent event){
 	proxy.registerRenderers();
     sm_recipes.initRecipes();
     sm_ic2plugin.initIC2Recipes();
     sm_ic2handler.initIC2();
     sm_naming.initIC2Naming();
 	}
     
 	public static CreativeTabs StoreMoreTab = new StoreMoreTab(CreativeTabs.getNextID(), "Store More");
 	
 	{
 
 	ic2blocksI = new mods.storemore.blocks.ic2blocksI(sm_config.ic2blocksIID).setHardness(1.0f).setResistance(50.0f).setCreativeTab(StoreMoreTab).setUnlocalizedName("ic2blocksI");
 	
 	}
 	
 	{
 
 		Item.itemsList[sm_config.ic2blocksIID] = new ic2blocksIItem(sm_config.ic2blocksIID-256).setUnlocalizedName("ic2blocksI");
 
 	}
 
     public static void initIC2Recipes()
     	{
     		
     		
     		
     		if(Loader.isModLoaded("IC2"))
     		{
     			
     			copperBlock = sm_modCompatability.getIC2Item("copperBlock");
     			tinBlock = sm_modCompatability.getIC2Item("tinBlock");
     			uraniumBlock = sm_modCompatability.getIC2Item("uraniumBlock");
     			bronzeBlock = sm_modCompatability.getIC2Item("bronzeBlock");
     	
 
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,0), "XXX", "XXX", "XXX", Character.valueOf('X'), copperBlock);
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,1), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,0));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,2), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,1));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,3), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,2));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,4), "XXX", "XXX", "XXX", Character.valueOf('X'), tinBlock);
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,5), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,4));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,6), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,5));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,7), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,6));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,8), "XXX", "XXX", "XXX", Character.valueOf('X'), uraniumBlock);
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,9), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,8));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,10), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,9));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,11), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,10));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,12), "XXX", "XXX", "XXX", Character.valueOf('X'), bronzeBlock);
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,13), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,12));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,14), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,13));
     		GameRegistry.addRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,1,15), "XXX", "XXX", "XXX", Character.valueOf('X'), new ItemStack(sm_ic2plugin.ic2blocksI,1,14));
     		
    		GameRegistry.addShapelessRecipe(copperBlock2,9,0, new ItemStack(sm_ic2plugin.ic2blocksI,1,0));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,0), new ItemStack(sm_ic2plugin.ic2blocksI,1,1));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,1), new ItemStack(sm_ic2plugin.ic2blocksI,1,2));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,2), new ItemStack(sm_ic2plugin.ic2blocksI,1,3));
    		GameRegistry.addShapelessRecipe(tinBlock2,9,0, new ItemStack(sm_ic2plugin.ic2blocksI,1,4));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,4), new ItemStack(sm_ic2plugin.ic2blocksI,1,5));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,5), new ItemStack(sm_ic2plugin.ic2blocksI,1,6));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,6), new ItemStack(sm_ic2plugin.ic2blocksI,1,7));
    		GameRegistry.addShapelessRecipe(uraniumBlock2,9,0, new ItemStack(sm_ic2plugin.ic2blocksI,1,8));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,8), new ItemStack(sm_ic2plugin.ic2blocksI,1,9));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,9), new ItemStack(sm_ic2plugin.ic2blocksI,1,10));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,10), new ItemStack(sm_ic2plugin.ic2blocksI,1,11));
    		GameRegistry.addShapelessRecipe(bronzeBlock2,9,0, new ItemStack(sm_ic2plugin.ic2blocksI,1,12));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,12), new ItemStack(sm_ic2plugin.ic2blocksI,1,13));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,13), new ItemStack(sm_ic2plugin.ic2blocksI,1,14));
     		GameRegistry.addShapelessRecipe(new ItemStack(sm_ic2plugin.ic2blocksI,9,14), new ItemStack(sm_ic2plugin.ic2blocksI,1,15));
     		
     		
     		}
 
 }
 }
  
