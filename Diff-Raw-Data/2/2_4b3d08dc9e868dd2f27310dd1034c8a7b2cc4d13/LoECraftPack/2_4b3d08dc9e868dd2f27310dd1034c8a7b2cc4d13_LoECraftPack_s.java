 package loecraftpack;
 
 import loecraftpack.common.blocks.BlockAppleBloomLeaves;
 import loecraftpack.common.blocks.BlockAppleBloomSapling;
 import loecraftpack.common.blocks.BlockAppleLog;
 import loecraftpack.common.blocks.BlockBank;
 import loecraftpack.common.blocks.BlockColoredBed;
 import loecraftpack.common.blocks.BlockHiddenOre;
 import loecraftpack.common.blocks.BlockProjectTable;
 import loecraftpack.common.blocks.BlockProtectionMonolith;
 import loecraftpack.common.blocks.BlockZapAppleLeaves;
 import loecraftpack.common.blocks.BlockZapAppleLeavesCharged;
 import loecraftpack.common.blocks.BlockZapAppleSapling;
 import loecraftpack.common.blocks.TileColoredBed;
 import loecraftpack.common.blocks.TileProjectTable;
 import loecraftpack.common.blocks.TileProtectionMonolith;
 import loecraftpack.common.enchantment.EnchantmentBanish;
 import loecraftpack.common.enchantment.EnchantmentElectric;
 import loecraftpack.common.enchantment.EnchantmentFriendship;
 import loecraftpack.common.entity.EntityPedestal;
 import loecraftpack.common.entity.EntityPhantomArrow;
 import loecraftpack.common.entity.EntityTimberWolf;
 import loecraftpack.common.items.ItemBits;
 import loecraftpack.common.items.ItemColoredBed;
 import loecraftpack.common.items.ItemCrystalHeart;
 import loecraftpack.common.items.ItemGemStones;
 import loecraftpack.common.items.ItemHiddenOre;
 import loecraftpack.common.items.ItemIronArrow;
 import loecraftpack.common.items.ItemLeavesAppleBloom;
 import loecraftpack.common.items.ItemMusicDisc;
 import loecraftpack.common.items.ItemNecklace;
 import loecraftpack.common.items.ItemNecklaceOfBling;
 import loecraftpack.common.items.ItemNecklaceOfDreams;
 import loecraftpack.common.items.ItemPedestal;
 import loecraftpack.common.items.ItemPickaxeGem;
 import loecraftpack.common.items.ItemRacial;
 import loecraftpack.common.items.ItemRestorative;
 import loecraftpack.common.items.ItemRing;
 import loecraftpack.common.items.ItemRingLife;
 import loecraftpack.common.items.ItemRingPhantomArrow;
 import loecraftpack.common.items.ItemZapApple;
 import loecraftpack.common.items.ItemZapAppleJam;
 import loecraftpack.common.logic.HandlerColoredBed;
 import loecraftpack.common.logic.HandlerEvent;
 import loecraftpack.common.logic.HandlerGui;
 import loecraftpack.common.logic.HandlerPlayer;
 import loecraftpack.common.logic.HandlerTick;
 import loecraftpack.common.potions.PotionCharged;
 import loecraftpack.common.potions.PotionOreVision;
 import loecraftpack.common.worldgen.BiomeGenEverFreeForest;
 import loecraftpack.common.worldgen.WorldGenCustomAppleTree;
 import loecraftpack.common.worldgen.WorldGenCustomForest;
 import loecraftpack.packet.PacketHandlerClient;
 import loecraftpack.packet.PacketHandlerServer;
 import loecraftpack.ponies.abilities.Ability;
 import loecraftpack.ponies.abilities.ItemAbility;
 import loecraftpack.ponies.stats.CommandStatRace;
 import loecraftpack.ponies.stats.StatHandlerServer;
 import loecraftpack.proxies.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 
 @Mod(modid = "loecraftpack", name = "LoECraft Pack", version = "1.0")
 
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, clientPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = PacketHandlerClient.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = PacketHandlerServer.class))
 public class LoECraftPack
 {
 	/***************************/
 	/**Variable Initialization**/
 	/***************************/
 	
 	//Create a singleton
 	@Instance("loecraftpack")
     public static LoECraftPack instance = new LoECraftPack();
 	
 	//Register proxies
 	@SidedProxy(clientSide = "loecraftpack.proxies.ClientProxy", serverSide = "loecraftpack.proxies.CommonProxy")
     public static CommonProxy proxy;
 	@SidedProxy(clientSide = "loecraftpack.ponies.stats.StatHandlerClient", serverSide = "loecraftpack.ponies.stats.StatHandlerServer")
     public static StatHandlerServer statHandler;
 	
 	//Create our own creative tab
 	public static CreativeTabs LoECraftTab = new CreativeTabs("LoECraftTab")
 	{
 		//Set the icon - Do: CreativeTab - Add new item with custom icon for use here 
         public ItemStack getIconItemStack()
         {
                 return new ItemStack(Item.writableBook, 1, 0);
         }
 	};
 	//IDs
 	public static final int SkylandDimensionID = 8;
 	public static final int SkylandProviderID = 8;
 	
 	//Declare Potions
 	public static final PotionCharged potionCharged = (PotionCharged)(new PotionCharged(21, true, 16776960)).setPotionName("Charged").setIconIndex(0, 0);
 	public static final PotionOreVision potionOreVision = (PotionOreVision)(new PotionOreVision(22, false, 0)).setPotionName("Ore Vision").setIconIndex(0, 0);
 	
 	//Declare immutable Items, Blocks, and Enchantments - Do: Initialize Items/Blocks in PreInit based on config IDs
 	public static final ItemBits bits = new ItemBits(667);
 	public static final ItemColoredBed bedItems = new ItemColoredBed(670);
 	public static final ItemZapApple itemZapApple = (ItemZapApple)(new ItemZapApple(671, 4, 1.2F, true)).setAlwaysEdible().setUnlocalizedName("appleZap");
 	public static final ItemZapAppleJam itemZapAppleJam = (ItemZapAppleJam)(new ItemZapAppleJam(672, 4, 1.2F, false)).setAlwaysEdible().setUnlocalizedName("zapAppleJam");
 	public static final ItemPickaxeGem itemPickaxeGem = (ItemPickaxeGem)(new ItemPickaxeGem(673)).setUnlocalizedName("pickaxeGem");
 	public static final ItemPedestal itemPedestal = (ItemPedestal)(new ItemPedestal(674)).setUnlocalizedName("pedestal");
 	public static final ItemGemStones itemGemStones = (ItemGemStones)(new ItemGemStones(675)).setUnlocalizedName("gemstones");
 	public static final ItemCrystalHeart itemCrystalHeart = (ItemCrystalHeart)(new ItemCrystalHeart(676)).setUnlocalizedName("cyrstalheart");
 	public static final ItemRacial itemRacial = (ItemRacial)(new ItemRacial(677)).setUnlocalizedName("racial");
 	public static final ItemNecklace itemNecklace = (ItemNecklace)(new ItemNecklace(678)).setUnlocalizedName("necklace");
 	public static final ItemNecklaceOfDreams itemNecklaceOfDreams = (ItemNecklaceOfDreams)(new ItemNecklaceOfDreams(679)).setUnlocalizedName("necklacedream");
 	public static final ItemNecklaceOfBling itemNecklaceOfBling = (ItemNecklaceOfBling)(new ItemNecklaceOfBling(680)).setUnlocalizedName("necklacebling");
 	public static final ItemRing itemRing = (ItemRing)(new ItemRing(681)).setUnlocalizedName("ring");
 	public static final ItemRingLife itemRingLife = (ItemRingLife)(new ItemRingLife(682)).setUnlocalizedName("ringlife");
 	public static final ItemRingPhantomArrow itemRingPhantomArrow = (ItemRingPhantomArrow)(new ItemRingPhantomArrow(683)).setUnlocalizedName("ringphantomarrow");
 	public static final ItemIronArrow itemAmmo = (ItemIronArrow)(new ItemIronArrow(684)).setUnlocalizedName("ammo");
 	public static final ItemAbility ability = new ItemAbility(685);
 	public static final ItemRestorative restoratives = new ItemRestorative(686);
 	static
 	{
		restoratives.addSubType("Dispel Gem Major").addRMinorSpells();
 		restoratives.addSubType("Dispel Gem Major").addRMinorSpells().addRMajorSpells();
 		restoratives.addSubType("Cleanse Minor").addRSimpleDebuffs();
 		restoratives.addSubType("Cleanse Major").addRSimpleDebuffs().addRHarshDebuffs();
 	}
 	
 			
 	public static final BlockBank bank = new BlockBank(665);
 	public static final BlockProtectionMonolith monolith = new BlockProtectionMonolith(666);
 	public static final BlockProjectTable table = new BlockProjectTable(667);
 	public static final BlockColoredBed bedBlock = (BlockColoredBed)new BlockColoredBed(670).setHardness(0.2F);
 	public static final BlockAppleBloomSapling blockAppleBloomSapling = (BlockAppleBloomSapling)(new BlockAppleBloomSapling(671)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("saplingBloom");
 	public static final BlockZapAppleSapling blockZapAppleSapling = (BlockZapAppleSapling)(new BlockZapAppleSapling(672)).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setUnlocalizedName("saplingZap");
 	public static final BlockAppleLog blockAppleBloomLog = (BlockAppleLog)(new BlockAppleLog(673, "tree_side", "tree_top")).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logApple");
 	public static final BlockAppleLog blockZapAppleLog = (BlockAppleLog)(new BlockAppleLog(674, "loecraftpack:trees/tree_zapapple", "loecraftpack:trees/tree_zapapple_top" )).setHardness(2.0F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("logZap");
 	public static final BlockAppleBloomLeaves blockAppleBloomLeaves = (BlockAppleBloomLeaves)(new BlockAppleBloomLeaves(675)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	public static final BlockZapAppleLeaves blockZapAppleLeaves = (BlockZapAppleLeaves)(new BlockZapAppleLeaves(676)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	public static final BlockZapAppleLeavesCharged blockZapAppleLeavesCharged = (BlockZapAppleLeavesCharged)(new BlockZapAppleLeavesCharged(677)).setHardness(0.2F).setLightOpacity(1).setStepSound(Block.soundGrassFootstep);
 	public static final BlockHiddenOre blockGemOre = (BlockHiddenOre)(new BlockHiddenOre(678)).setHardness(1.5F).setResistance(10.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreGem");
 	
 	public static final Enchantment electricEnchant = new EnchantmentElectric(100, 2);
 	public static final Enchantment banishEnchant = new EnchantmentBanish(101, 2);
 	public static final Enchantment friendshipEnchant = new EnchantmentFriendship(103, 3);
 
 	
 	//declare Generators
 	public static final BiomeGenEverFreeForest biomeGeneratorEverFreeForest = (BiomeGenEverFreeForest)new BiomeGenEverFreeForest(50).setColor(5).setBiomeName("EverFree").setTemperatureRainfall(0.5f, 0.7f);
 	public static final WorldGenCustomForest worldGeneratorZapAppleForest = new WorldGenCustomForest(false, blockZapAppleSapling, blockZapAppleLog, blockZapAppleLeaves);
 	public static final WorldGenCustomAppleTree worldGeneratorAppleBloom = new WorldGenCustomAppleTree(false, blockAppleBloomSapling, blockAppleBloomLog, blockAppleBloomLeaves, 5);
 	
 	/****************************/
 	/**Forge Pre-Initialization**/
 	/****************************/
 	
 	@PreInit
     public void preLoad(FMLPreInitializationEvent event)
 	{
 			
 		
 		/***************/
 		/**Load Config**/
 		/***************/
 		
 		//Do: PreInit - Load mod config here
 		
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
 		ItemMusicDisc.AddMusicDisc("LoE", "Cloudsdale Race Theme");
 		ItemMusicDisc.AddMusicDisc("MLP:FiM", "What My Cutie Mark Is Telling Me");
 		LanguageRegistry.instance().addStringLocalization("item.appleZap.normal.name", "Zap-Apple");
 		LanguageRegistry.instance().addStringLocalization("item.appleZap.charged.name", "Zap-Apple : Charged");
 		LanguageRegistry.addName(itemZapAppleJam, "Zap-Apple Jam");
 		LanguageRegistry.instance().addStringLocalization("item.pickaxeGem.name", "Gem Pickaxe");
 		LanguageRegistry.addName(itemCrystalHeart, "Crystal Heart Container");
 		LanguageRegistry.addName(itemRacial, "Racial");
 		LanguageRegistry.addName(itemNecklace, "Necklace");
 		LanguageRegistry.addName(itemNecklaceOfDreams, "Necklace Of Dreams");
 		LanguageRegistry.addName(itemNecklaceOfBling, "Necklace Of Bling");
 		LanguageRegistry.addName(itemRing, "Ring");
 		LanguageRegistry.addName(itemRingLife, "Life Ring");
 		LanguageRegistry.addName(itemRingPhantomArrow, "Ring of Phantom Arrows");
 		LanguageRegistry.addName(itemAmmo, "Ammo");
 		LanguageRegistry.addName(itemPedestal, "Pedestal");
 		
 		//Abilities
 		Ability.RegisterAbilities();
 		
 		//Restoratives
 		restoratives.RegisterRestoratives();
 		
 		//Blocks
 		GameRegistry.registerBlock(bank, "Bank");
 		LanguageRegistry.addName(bank, "Bank");
 		GameRegistry.registerBlock(monolith, "ProtectionMonolithBlock");
 		LanguageRegistry.addName(monolith, "Protection Monolith");
 		GameRegistry.registerBlock(table, "ProjectTableBlock");
 		LanguageRegistry.addName(table, "Project Table");
 		GameRegistry.registerBlock(bedBlock, "ColoredBed");
 		GameRegistry.registerBlock(blockAppleBloomSapling,"AppleBloomSapling");
 		LanguageRegistry.addName(blockAppleBloomSapling,"Apple-Bloom Sapling");
 		GameRegistry.registerBlock(blockAppleBloomLog,"AppleBloomlog");
 		LanguageRegistry.addName(blockAppleBloomLog,"Apple-Bloom log");
 		GameRegistry.registerBlock(blockAppleBloomLeaves, ItemLeavesAppleBloom.class, "AppleBloomLeaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesAppleBloom.normal.name", "Apple-Bloom Leaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesAppleBloom.blooming.name", "Apple-Bloom Leaves : Blooming");
 		GameRegistry.registerBlock(blockZapAppleSapling,"ZapAppleSapling");
 		LanguageRegistry.addName(blockZapAppleSapling,"Zap-Apple Sapling");
 		GameRegistry.registerBlock(blockZapAppleLog,"ZapApplelog");
 		LanguageRegistry.addName(blockZapAppleLog,"Zap-Apple log");
 		GameRegistry.registerBlock(blockZapAppleLeaves, ItemLeavesAppleBloom.class, "ZapAppleLeaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZap.normal.name", "Zap-Apple Leaves");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZap.blooming.name", "Zap-Apple Leaves : Blooming");
 		GameRegistry.registerBlock(blockZapAppleLeavesCharged, ItemLeavesAppleBloom.class, "ZapAppleLeavesCharged");
 		LanguageRegistry.instance().addStringLocalization("tile.leavesZapCharged.name", "Zap-Apple Leaves : Charged");
 		GameRegistry.registerBlock(blockGemOre, ItemHiddenOre.class, "HiddenGemBlock");
 		for(int i=0; i<16; i++)
 		{
 			LanguageRegistry.instance().addStringLocalization("tile.oreGem."+i+".name", "Hidden Gem Block : "+itemGemStones.gemDisplayNames[i]);
 			LanguageRegistry.instance().addStringLocalization("item.gemstones."+itemGemStones.gemDataNames[i]+".name", itemGemStones.gemDisplayNames[i]);
 		}
 		
 		//Enchantment names
 		LanguageRegistry.instance().addStringLocalization("enchantment.electric", "Electric");
 		LanguageRegistry.instance().addStringLocalization("enchantment.banish", "Banish");
 		
 		//Tile Entities
 		GameRegistry.registerTileEntity(TileProtectionMonolith.class, "ProtectionMonolithTileEntity");
 		GameRegistry.registerTileEntity(TileColoredBed.class, "ColoredBedTileEntity");
 		GameRegistry.registerTileEntity(TileProjectTable.class, "ProjectTableTileEntity");
 		
 		//Entities
 		EntityRegistry.registerGlobalEntityID(EntityTimberWolf.class, "timberwolf", 100, 12422002, 5651507);
 		LanguageRegistry.instance().addStringLocalization("entity.timberwolf.name", "Timber Wolf");
 		EntityRegistry.registerModEntity(EntityPhantomArrow.class, "phantomarrow", 1, this, 50, 2, true);
 		EntityRegistry.registerModEntity(EntityPedestal.class, "pedestal", 2, this, 50, 2, true);
 		
 		//World Generators/Biomes/Layers/dimensional stuff/ etc.
 		GameRegistry.addBiome(biomeGeneratorEverFreeForest);
 		//DimensionManager.registerProviderType(SkylandProviderID, DimensionSkyland.class, false);
 		//DimensionManager.registerDimension(SkylandDimensionID, SkylandProviderID);
 		
 		
 		//Handlers
 		NetworkRegistry.instance().registerGuiHandler(this, new HandlerGui());
 		GameRegistry.registerPlayerTracker(new HandlerPlayer());
 		MinecraftForge.EVENT_BUS.register(new HandlerEvent());
 		TickRegistry.registerTickHandler(new HandlerTick(), Side.SERVER);
 		TickRegistry.registerTickHandler(new HandlerTick(), Side.CLIENT);
 		
 		
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
     	
 		//load all beds
 		HandlerColoredBed.loadBeds();
     	
     	//Crystal Heart
     	CraftingManager.getInstance().addRecipe(new ItemStack(LoECraftPack.itemCrystalHeart, 1, 0), "XX", "XX",
     											'X', new ItemStack(LoECraftPack.itemGemStones, 1, 9));
 	}
 	
 	/*****************************/
 	/**Forge Post-Initialization**/
 	/*****************************/
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent e)
 	{
 		//Do: Post-load stuff
 		proxy.doProxyStuffPost();
 	}
 	
 	/****************/
 	/**Server Start**/
 	/****************/
 	@ServerStarting
 	public void serverLoad(FMLServerStartingEvent event)
 	{
 		//load server commands
 		event.registerServerCommand(new CommandStatRace());
 	}	
 	
 	public static boolean isSinglePlayer()
 	{
 		return proxy.isSinglePlayer();
 	}
 }
