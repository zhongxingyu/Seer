 package loecraftpack;
 
 import loecraftpack.blocks.BlockZapAppleLeaves;
 import loecraftpack.blocks.BlockZapAppleLog;
 import loecraftpack.blocks.BlockZapAppleSapling;
 import loecraftpack.blocks.ColoredBedBlock;
 import loecraftpack.blocks.ProtectionMonolithBlock;
 import loecraftpack.blocks.te.ColoredBedTileEntity;
 import loecraftpack.blocks.te.ProtectionMonolithTileEntity;
 import loecraftpack.enums.Dye;
 import loecraftpack.items.Bits;
 import loecraftpack.items.ColoredBedItem;
 import loecraftpack.items.ItemZapApple;
 import loecraftpack.items.ItemZapAppleJam;
 import loecraftpack.items.musicdiscs.LoEMusicDisc;
 import loecraftpack.logic.handlers.ColoredBedHandler;
 import loecraftpack.logic.handlers.EventHandler;
 import loecraftpack.logic.handlers.GuiHandler;
 import loecraftpack.logic.handlers.KeysHandler;
 import loecraftpack.logic.handlers.PlayerHandler;
 import loecraftpack.packethandling.ClientPacketHandler;
 import loecraftpack.packethandling.ServerPacketHandler;
 import loecraftpack.ponies.stats.ServerStatHandler;
 import loecraftpack.proxies.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
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
 
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, clientPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = ClientPacketHandler.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = ServerPacketHandler.class))
 public class LoECraftPack
 {
 	/***************************/
 	/**Variable Initialization**/
 	/***************************/
 	
 	//Create a singleton
 	@Instance
     public static LoECraftPack instance = new LoECraftPack();
 	
	//Instantiate bed item variables
	
	
	
 	//Register proxies
 	@SidedProxy(clientSide = "loecraftpack.proxies.ClientProxy", serverSide = "loecraftpack.proxies.CommonProxy")
     public static CommonProxy proxy;
 	@SidedProxy(clientSide = "loecraftpack.ponies.stats.ClientStatHandler", serverSide = "loecraftpack.ponies.stats.ServerStatHandler")
     public static ServerStatHandler StatHandler;
 	
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
 	public static final Bits bits = new Bits(667);
 	public static final ColoredBedItem bedItems = new ColoredBedItem(670);
 	public static final ItemZapApple itemZapApple = (ItemZapApple)(new ItemZapApple(671, 4, 1.2F, true)).setAlwaysEdible().setPotionEffect(Potion.moveSpeed.id, 30, 0, 1.0F).setUnlocalizedName("appleZap");
 	public static final ItemZapAppleJam itemZapAppleJam = (ItemZapAppleJam)(new ItemZapAppleJam(672, 4, 1.2F, false)).setAlwaysEdible().setUnlocalizedName("zapAppleJam");
 	
 	public static final ProtectionMonolithBlock monolith = new ProtectionMonolithBlock(666);
 	public static final ColoredBedBlock bedBlock = new ColoredBedBlock(670);
 	public static final BlockZapAppleSapling blockZapAppleSapling = (BlockZapAppleSapling)(new BlockZapAppleSapling(671)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("saplingZap");
 	public static final BlockZapAppleLog blockZapApplelog = (BlockZapAppleLog)(new BlockZapAppleLog(672)).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logZap");
 	public static final BlockZapAppleLeaves blockZapAppleLeaves = (BlockZapAppleLeaves)(new BlockZapAppleLeaves(673)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("leavesZap");
 	
 	
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
 		for(int i = 0; i < Bits.names.length; i++ )
 			LanguageRegistry.instance().addStringLocalization("item.itemBits." + Bits.iconNames[i] + ".name", Bits.names[i]);
 		LoEMusicDisc.AddMusicDisc("LoE", "Cloudsdale Race Theme"); //This is just a test. I do not yet have permission to use this song publicly.
 		LoEMusicDisc.AddMusicDisc("MLP:FiM", "What My Cutie Mark Is Telling Me");
 		LanguageRegistry.addName(itemZapAppleJam, "Zap-Apple Jam");
 		
 		//Blocks
 		GameRegistry.registerBlock(monolith, "ProtectionMonolithBlock");
 		LanguageRegistry.addName(monolith, "Protection Monolith");
 		GameRegistry.registerBlock(bedBlock, "ColoredBed");
 		GameRegistry.registerBlock(blockZapAppleSapling,"ZapAppleSapling");
 		LanguageRegistry.addName(blockZapAppleSapling,"ZapApple Sapling");
 		GameRegistry.registerBlock(blockZapApplelog,"ZapApplelog");
 		LanguageRegistry.addName(blockZapApplelog,"ZapApple log");
 		GameRegistry.registerBlock(blockZapAppleLeaves,"ZapAppleLeaves");
 		LanguageRegistry.addName(blockZapAppleLeaves,"ZapApple Leaves"); 
 		
 		//Tile Entities
 		GameRegistry.registerTileEntity(ProtectionMonolithTileEntity.class, "ProtectionMonolithTileEntity");
 		GameRegistry.registerTileEntity(ColoredBedTileEntity.class, "ColoredBedTileEntity");
 		
 		//Handlers
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		GameRegistry.registerPlayerTracker(new PlayerHandler());
 		MinecraftForge.EVENT_BUS.register(new EventHandler());
 		
 		/******************/
 		/**Do Proxy Stuff**/
 		/******************/
 		
 		//Schtuff
 		proxy.doProxyStuff();
 		
 		/******************/
 		/**Update Recipes**/
 		/******************/
           	
     	//locate and remove old bed recipe
 		ColoredBedHandler.cleanBedRecipe();
     	
     	//Add base-color beds
     	ColoredBedHandler.addCustomBed("Rarity", Dye.White);
     	ColoredBedHandler.addCustomBed("Octavia", Dye.LightGray);
     	ColoredBedHandler.addCustomBed("Derpy", Dye.Gray);
     	ColoredBedHandler.addCustomBed("Discord", Dye.Black);
     	ColoredBedHandler.addCustomBed("Big Mac", Dye.Red);
     	ColoredBedHandler.addCustomBed("Applejack", Dye.Orange);
     	ColoredBedHandler.addCustomBed("Fluttershy", Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Granny Smith", Dye.Lime);
     	ColoredBedHandler.addCustomBed("Spike", Dye.Green);
     	ColoredBedHandler.addCustomBed("Trixie", Dye.Cyan);
     	ColoredBedHandler.addCustomBed("Rainbow Dash", Dye.LightBlue);
     	ColoredBedHandler.addCustomBed("Luna", Dye.Blue);
     	ColoredBedHandler.addCustomBed("Twilight Sparkle", Dye.Purple);
     	ColoredBedHandler.addCustomBed("Cheerilee", Dye.Magenta);
     	ColoredBedHandler.addCustomBed("Pinkie Pie", Dye.Pink);
     	ColoredBedHandler.addCustomBed("Muffin", Dye.Brown);
     	
     	//Add combo-color beds
     	ColoredBedHandler.addCustomBed("Celestia", Dye.Lime, Dye.LightBlue, Dye.Pink);
     	ColoredBedHandler.addCustomBed("Fausticorn", Dye.White, Dye.Red, Dye.White);
     	ColoredBedHandler.addCustomBed("CMC", Dye.Red, Dye.Blue, Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Sweetie Belle", Dye.White, Dye.Pink, Dye.Magenta);
     	ColoredBedHandler.addCustomBed("Scootaloo", Dye.Orange, Dye.Purple, Dye.Orange);
     	ColoredBedHandler.addCustomBed("Babs Seed", Dye.Brown, Dye.Red, Dye.Pink);
     	ColoredBedHandler.addCustomBed("Apple Bloom", Dye.Yellow, Dye.Red, Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Silver Spoon", Dye.Gray, Dye.LightGray, Dye.LightBlue);
     	ColoredBedHandler.addCustomBed("Diamond Tiara", Dye.Pink, Dye.Purple, Dye.White);
     	ColoredBedHandler.addCustomBed("Lyra", Dye.Lime, Dye.Yellow, Dye.Cyan);
     	ColoredBedHandler.addCustomBed("Bon Bon", Dye.Blue, Dye.Pink, Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Spitfire", Dye.Yellow, Dye.Orange, Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Shining Armor", Dye.White, Dye.LightBlue, Dye.Blue);
     	ColoredBedHandler.addCustomBed("Cadence", Dye.Purple, Dye.Magenta, Dye.Yellow);
     	ColoredBedHandler.addCustomBed("Colgate", Dye.White, Dye.Blue, Dye.Cyan);
     	
     	//Register bed pairs
     	ColoredBedHandler.addBedPair("Alicorn Sisters", "Celestia", "Luna");
 	}
 	
 	/*****************************/
 	/**Forge Post-Initialization**/
 	/*****************************/
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent e)
 	{
 		//TODO: POST-LOAD STUFF
 		Minecraft.getMinecraft().gameSettings.keyBindJump = KeysHandler.jump; //KeysHandler overrides the default jump keybind, which disables jumping. This gets around that.
 	}
 }
