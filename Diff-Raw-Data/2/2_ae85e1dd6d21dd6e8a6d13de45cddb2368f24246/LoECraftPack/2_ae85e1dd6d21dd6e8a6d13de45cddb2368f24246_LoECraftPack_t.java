 package loecraftpack;
 
 import loecraftpack.common.blocks.BlockAppleBloomLeaves;
 import loecraftpack.common.blocks.BlockAppleLog;
 import loecraftpack.common.blocks.BlockColoredBed;
 import loecraftpack.common.blocks.BlockProtectionMonolith;
 import loecraftpack.common.blocks.BlockZapAppleLeaves;
 import loecraftpack.common.blocks.BlockZapAppleLeavesCharged;
 import loecraftpack.common.blocks.BlockZapAppleSapling;
 import loecraftpack.common.blocks.TileColoredBed;
 import loecraftpack.common.blocks.TileProtectionMonolith;
 import loecraftpack.common.items.ItemBits;
 import loecraftpack.common.items.ItemColoredBed;
 import loecraftpack.common.items.ItemLeavesAppleBloom;
 import loecraftpack.common.items.ItemMusicDisc;
 import loecraftpack.common.items.ItemZapApple;
 import loecraftpack.common.items.ItemZapAppleJam;
 import loecraftpack.common.logic.HandlerColoredBed;
 import loecraftpack.common.logic.HandlerEvent;
 import loecraftpack.common.logic.HandlerGui;
 import loecraftpack.common.logic.HandlerKey;
 import loecraftpack.common.logic.HandlerPlayer;
 import loecraftpack.enums.Dye;
 import loecraftpack.packet.PacketHandlerClient;
 import loecraftpack.packet.PacketHandlerServer;
 import loecraftpack.ponies.stats.StatHandlerServer;
 import loecraftpack.proxies.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 
 @Mod(modid = "loecraftpack", name = "LoECraft Pack", version = "1.0")
 
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, clientPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = PacketHandlerClient.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = PacketHandlerServer.class))
 public class LoECraftPack
 {
 	/***************************/
 	/**Variable Initialization**/
 	/***************************/
 	
 	//Create a singleton
 	@Instance
     public static LoECraftPack instance = new LoECraftPack();
 	
 	//Register proxies
 	@SidedProxy(clientSide = "loecraftpack.proxies.ClientProxy", serverSide = "loecraftpack.proxies.CommonProxy")
     public static CommonProxy proxy;
	@SidedProxy(clientSide = "loecraftpack.ponies.stats.StatHandlerClient", serverSide = "loecraftpack.ponies.stats.StatHandlerServer")
     public static StatHandlerServer StatHandler;
 	
 	//Create our own creative tab
 	public static CreativeTabs LoECraftTab = new CreativeTabs("LoECraftTab")
 	{
 		//Set the icon - TODO: ADD NEW ITEM WITH CUSTOM ICON FOR USE HERE 
         public ItemStack getIconItemStack()
         {
                 return new ItemStack(Item.writableBook, 1, 0);
         }
 	};
 	
 	//Declare immutable items and blocks - TODO: INITIALIZE THESE IN PREINIT BASED ON CONFIG IDS
 	public static final ItemBits bits = new ItemBits(667);
 	public static final ItemColoredBed bedItems = new ItemColoredBed(670);
 	public static final ItemZapApple itemZapApple = (ItemZapApple)(new ItemZapApple(671, 4, 1.2F, true)).setAlwaysEdible().setUnlocalizedName("appleZap");
 	public static final ItemZapAppleJam itemZapAppleJam = (ItemZapAppleJam)(new ItemZapAppleJam(672, 4, 1.2F, false)).setAlwaysEdible().setUnlocalizedName("zapAppleJam");
 	
 	public static final BlockProtectionMonolith monolith = new BlockProtectionMonolith(666);
 	public static final BlockColoredBed bedBlock = new BlockColoredBed(670);
 	public static final BlockZapAppleSapling blockZapAppleSapling = (BlockZapAppleSapling)(new BlockZapAppleSapling(671)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("saplingZap");
 	public static final BlockAppleLog blockZapAppleLog = (BlockAppleLog)(new BlockAppleLog(672, "loecraftpack:tree_zapapple", "loecraftpack:tree_zapapple_top" )).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logZap");
 	public static final BlockAppleLog blockAppleBloomLog = (BlockAppleLog)(new BlockAppleLog(673, "tree_side", "tree_top")).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logapple");
 	//@Mod.Block(name = "ZapApple Leaves")
 	public static final BlockZapAppleLeaves blockZapAppleLeaves = (BlockZapAppleLeaves)(new BlockZapAppleLeaves(674)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	//@Mod.Block(name = "ZapApple Leaves Charged")
 	public static final BlockZapAppleLeavesCharged blockZapAppleLeavesCharged = (BlockZapAppleLeavesCharged)(new BlockZapAppleLeavesCharged(675)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	
 	//@Mod.Block(name = "AppleBloom Leaves")
 	public static final BlockAppleBloomLeaves blockAppleBloomLeaves = (BlockAppleBloomLeaves)(new BlockAppleBloomLeaves(676)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	
 	/****************************/
 	/**Forge Pre-Initialization**/
 	/****************************/
 	
 	@PreInit
     public void preLoad(FMLPreInitializationEvent event)
 	{
 		/***************/
 		/**Load Config**/
 		/***************/
 		
 		//TODO: LOAD CONFIG HERE
 		
 		/************************/
 		/**Initialize Variables**/
 		/************************/
     }
 	
 	/************************/
 	/**Forge Initialization**/
 	/************************/
 	
 	@Init
 	public void load(FMLInitializationEvent e)
 	{
 		/****************************/
 		/**Register everything else**/
 		/****************************/
 		
 		//Creative tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.LoECraftTab", "LoECraft");
 		
 		//Items
 		for(int i = 0; i < ItemBits.names.length; i++ )
 			LanguageRegistry.instance().addStringLocalization("item.itemBits." + ItemBits.iconNames[i] + ".name", ItemBits.names[i]);
 		ItemMusicDisc.AddMusicDisc("LoE", "Cloudsdale Race Theme"); //This is just a test. I do not yet have permission to use this song publicly.
 		ItemMusicDisc.AddMusicDisc("MLP:FiM", "What My Cutie Mark Is Telling Me");
 		LanguageRegistry.instance().addStringLocalization("item.appleZap.normal.name", "Zap-Apple");
 		LanguageRegistry.instance().addStringLocalization("item.appleZap.charged.name", "Zap-Apple : Charged");
 		LanguageRegistry.addName(itemZapAppleJam, "Zap-Apple Jam");
 		
 		//Blocks
 		GameRegistry.registerBlock(monolith, "ProtectionMonolithBlock");
 		LanguageRegistry.addName(monolith, "Protection Monolith");
 		GameRegistry.registerBlock(bedBlock, "ColoredBed");
 		GameRegistry.registerBlock(blockZapAppleSapling,"ZapAppleSapling");
 		LanguageRegistry.addName(blockZapAppleSapling,"ZapApple Sapling");
 		GameRegistry.registerBlock(blockZapAppleLog,"ZapApplelog");
 		LanguageRegistry.addName(blockZapAppleLog,"ZapApple log");
 		LanguageRegistry.addName(blockAppleBloomLog,"AppleBloom log");
 		GameRegistry.registerBlock(blockZapAppleLeaves, ItemLeavesAppleBloom.class, "ZapAppleLeaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZap.normal.name", "Zap-Apple Leaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZap.blooming.name", "Zap-Apple Leaves : Blooming");
 		GameRegistry.registerBlock(blockZapAppleLeavesCharged, ItemLeavesAppleBloom.class, "ZapAppleLeavesCharged");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZapCharged.name", "Zap-Apple Leaves : Charged");
 		GameRegistry.registerBlock(blockAppleBloomLeaves, ItemLeavesAppleBloom.class, "AppleBloomLeaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesAppleBloom.normal.name", "Apple-Bloom Leaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesAppleBloom.blooming.name", "Apple-Bloom Leaves : Blooming");
 		
 		//Tile Entities
 		GameRegistry.registerTileEntity(TileProtectionMonolith.class, "ProtectionMonolithTileEntity");
 		GameRegistry.registerTileEntity(TileColoredBed.class, "ColoredBedTileEntity");
 		
 		//Handlers
 		NetworkRegistry.instance().registerGuiHandler(this, new HandlerGui());
 		GameRegistry.registerPlayerTracker(new HandlerPlayer());
 		MinecraftForge.EVENT_BUS.register(new HandlerEvent());
 		
 		/******************/
 		/**Do Proxy Stuff**/
 		/******************/
 		
 		//Schtuff
 		proxy.doProxyStuff();
 		
 		/******************/
 		/**Update Recipes**/
 		/******************/
           	
     	//locate and remove old bed recipe
 		HandlerColoredBed.cleanBedRecipe();
     	
     	//Add base-color beds
     	HandlerColoredBed.addCustomBed("Rarity", Dye.White);
     	HandlerColoredBed.addCustomBed("Octavia", Dye.LightGray);
     	HandlerColoredBed.addCustomBed("Derpy", Dye.Gray);
     	HandlerColoredBed.addCustomBed("Discord", Dye.Black);
     	HandlerColoredBed.addCustomBed("Big Mac", Dye.Red);
     	HandlerColoredBed.addCustomBed("Applejack", Dye.Orange);
     	HandlerColoredBed.addCustomBed("Fluttershy", Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Granny Smith", Dye.Lime);
     	HandlerColoredBed.addCustomBed("Spike", Dye.Green);
     	HandlerColoredBed.addCustomBed("Trixie", Dye.Cyan);
     	HandlerColoredBed.addCustomBed("Rainbow Dash", Dye.LightBlue);
     	HandlerColoredBed.addCustomBed("Luna", Dye.Blue);
     	HandlerColoredBed.addCustomBed("Twilight Sparkle", Dye.Purple);
     	HandlerColoredBed.addCustomBed("Cheerilee", Dye.Magenta);
     	HandlerColoredBed.addCustomBed("Pinkie Pie", Dye.Pink);
     	HandlerColoredBed.addCustomBed("Muffin", Dye.Brown);
     	
     	//Add combo-color beds
     	HandlerColoredBed.addCustomBed("Celestia", Dye.Lime, Dye.LightBlue, Dye.Pink);
     	HandlerColoredBed.addCustomBed("Fausticorn", Dye.White, Dye.Red, Dye.White);
     	HandlerColoredBed.addCustomBed("CMC", Dye.Red, Dye.Blue, Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Sweetie Belle", Dye.White, Dye.Pink, Dye.Magenta);
     	HandlerColoredBed.addCustomBed("Scootaloo", Dye.Orange, Dye.Purple, Dye.Orange);
     	HandlerColoredBed.addCustomBed("Babs Seed", Dye.Brown, Dye.Red, Dye.Pink);
     	HandlerColoredBed.addCustomBed("Apple Bloom", Dye.Yellow, Dye.Red, Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Silver Spoon", Dye.Gray, Dye.LightGray, Dye.LightBlue);
     	HandlerColoredBed.addCustomBed("Diamond Tiara", Dye.Pink, Dye.Purple, Dye.White);
     	HandlerColoredBed.addCustomBed("Lyra", Dye.Lime, Dye.Yellow, Dye.Cyan);
     	HandlerColoredBed.addCustomBed("Bon Bon", Dye.Blue, Dye.Pink, Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Spitfire", Dye.Yellow, Dye.Orange, Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Shining Armor", Dye.White, Dye.LightBlue, Dye.Blue);
     	HandlerColoredBed.addCustomBed("Cadence", Dye.Purple, Dye.Magenta, Dye.Yellow);
     	HandlerColoredBed.addCustomBed("Colgate", Dye.White, Dye.Blue, Dye.Cyan);
     	
     	//Register bed pairs
     	HandlerColoredBed.addBedPair("Alicorn Sisters", "Celestia", "Luna");
 	}
 	
 	/*****************************/
 	/**Forge Post-Initialization**/
 	/*****************************/
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent e)
 	{
 		//TODO: POST-LOAD STUFF
 		Minecraft.getMinecraft().gameSettings.keyBindJump = HandlerKey.jump; //KeysHandler overrides the default jump keybind, which disables jumping. This gets around that.
 	}
 }
