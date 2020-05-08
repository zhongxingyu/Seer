 package ml.boxes;
 
 import ml.boxes.block.MetaType;
 import ml.boxes.item.ItemType;
 import ml.boxes.network.packets.PacketDescribeCrate;
 import ml.boxes.network.packets.PacketDescribeDisplay;
 import ml.boxes.network.packets.PacketDescribeSafe;
 import ml.boxes.network.packets.PacketTipClick;
 import ml.boxes.network.packets.PacketUpdateData;
 import ml.boxes.recipe.RecipeBox;
 import ml.boxes.recipe.RecipeComboMech;
 import ml.boxes.recipe.RecipeKey;
 import ml.boxes.recipe.RecipeSafe;
 import ml.boxes.tile.TileEntityBox;
 import ml.boxes.tile.TileEntityCrate;
 import ml.boxes.tile.TileEntityDisplayCase;
 import ml.boxes.tile.TileEntitySafe;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
@Mod(modid="Boxes", name="Boxes", dependencies="required-after:Forge@[6.5,)")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false) //, channels={PacketHandler.defChan}, packetHandler=PacketHandler.class)
 public class Boxes {
 	
 	@SidedProxy(serverSide="ml.boxes.CommonProxy", clientSide="ml.boxes.client.ClientProxy")
 	public static CommonProxy proxy;
 	
 	@Instance("Boxes")
 	public static Boxes instance;
 	public static Boolean neiInstalled = false;
 	
 	public static CreativeTabs BoxTab = new BoxesCreativeTab("boxes");
 	
 	public static BoxesConfig config;
 
 	@EventHandler
 	public void PreInit(FMLPreInitializationEvent evt){
 		Configuration cfg = new Configuration(evt.getSuggestedConfigurationFile());
 		config = (BoxesConfig)new BoxesConfig(cfg).load();
 	}
 	
 	@EventHandler
 	public void Init(FMLInitializationEvent evt){
 		GameRegistry.registerTileEntity(TileEntityBox.class, "box");
 		GameRegistry.registerTileEntity(TileEntityCrate.class, "crate");
 		GameRegistry.registerTileEntity(TileEntitySafe.class, "safe");
 		GameRegistry.registerTileEntity(TileEntityDisplayCase.class, "disp_case");
 		
 		Registry.registerBlocks();
 		
 		Registry.registerItems();
 		
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		
 		ml.core.network.PacketHandler pkh = new ml.core.network.PacketHandler();
 			pkh.addHandler(PacketUpdateData.class);
 			pkh.addHandler(PacketTipClick.class);
 			
 			pkh.addHandler(PacketDescribeCrate.class);
 			pkh.addHandler(PacketDescribeSafe.class);
 			pkh.addHandler(PacketDescribeSafe.PacketLockSafe.class);
 			pkh.addHandler(PacketDescribeDisplay.class);
 		
 		NetworkRegistry.instance().registerChannel(pkh, "Boxes");
 		
 		GameRegistry.addRecipe(new ItemStack(Registry.ItemResources, 1, 0), "ppp", "sws", "ppp", 'p', Item.paper, 's', Item.silk, 'w', Item.bucketWater);
 		GameRegistry.addRecipe(ItemType.ISFromType(ItemType.Label, 3), "ppp", " s ", 'p', Item.paper, 's', Item.slimeBall);
 		
 		GameRegistry.addRecipe(new RecipeBox());
 		
 		if (config.crate_allowCrafting) {
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Registry.BlockMeta, 1, MetaType.Crate.ordinal()), "wsw", "scs", "wsw", 'w', "logWood", 's', "plankWood", 'c', Block.chest));
 		}
 		
 		if (config.lockbox_allowCrafting) {
 			GameRegistry.addRecipe(new RecipeSafe());
 			GameRegistry.addRecipe(new RecipeKey(" m", "nm", "nm", 'n', Item.goldNugget, 'm', Item.ingotGold));
 			GameRegistry.addRecipe(new RecipeComboMech());
 		}
 		
 		initDungeonLoot();
 		
 		proxy.load();
 	}
 	
 	@EventHandler
 	public void PostInit(FMLPostInitializationEvent e) {
 
 	}
 	
 	public void initDungeonLoot(){
 		if (config.allowCardboardDungeonSpawn)
 			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(Registry.ItemResources), 1, 3, 100));
 		if (config.allowCardboardBlackSmithSpawn)
 			ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, new WeightedRandomChestContent(new ItemStack(Registry.ItemResources), 1, 5, 12));
 		
 		ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(new ItemStack(Registry.ItemResources), 1, 4, 7));
 	}
 	
 }
