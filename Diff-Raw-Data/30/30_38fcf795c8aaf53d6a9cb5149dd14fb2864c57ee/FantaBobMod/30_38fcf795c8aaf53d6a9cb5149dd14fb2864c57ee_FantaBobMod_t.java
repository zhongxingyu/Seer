 package fr.outadoc.FantaBobMod.common;
 
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.StepSound;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.stats.Achievement;
 import net.minecraft.stats.AchievementList;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import fr.outadoc.FantaBobMod.client.FTMEventSound;
 
 @Mod(modid = "FantaBobMod", name = "Fanta Bob Mod", version = "2.0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class FantaBobMod
 {
 	@SidedProxy(clientSide = "fr.outadoc.FantaBobMod.client.ClientProxy", serverSide = "fr.outadoc.FantaBobMod.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	// Achievement
 	public static Achievement installModAch;	
 	public static Achievement craftMagabondRecAch;
 	public static Achievement getFantaGlassesAch;
 	public static Achievement killBobAch;
 	public static Achievement killFantaAch;
 	public static Achievement killJeanKevinAch;
 	public static Achievement floodAch;
 	public static Achievement killBotlennonAch;
 	public static Achievement getBouseAch;
 	public static Achievement brushTeethAch;
 	public static Achievement getHairPotionAch;
 	public static Achievement getStampCollectionAch;
 	public static Achievement getMagabondChopAch;
 	public static Achievement getCobbleTieAch;
 	public static Achievement getBambooSwordAch;
 	
 	public static CraftingHandler craftHandler = new CraftingHandler();
 	public static PickupHandler pickupHandler = new PickupHandler();
 	public static PlayerHandler playerHandler = new PlayerHandler();
 	
 	// Block
 	public static Block bouse;
 	
 	// Items
 	public static Item fantaGlasses, cobbleTie, toothBrush, stampCollection, bambooSword, hairPotion, magabondChop, magabondRecord;
 	
 	// Armor
 	static EnumArmorMaterial FBMarmor = EnumHelper.addArmorMaterial("FBMarmor", 5, new int[]{1, 3, 2, 1}, 15); 
 	
 	// Log
 	public static Logger FBMlog = Logger.getLogger("FantaBobMod");
 	
 	// cfg
 	public static boolean fantaSpawn, BoblennonSpawn, JeankevinSpawn, BotlennonSpawn;
 	public static int fantaSpawnRate, BoblennonSpawnRate, JeankevinSpawnRate, BotlennonSpawnRate;
 	
 	public static int bouseID, fantaGlassesID, cobbleTieID, toothBrushID, stampCollectionID, bambooSwordID, hairPotionID, magabondChopID, magabondRecordID;
 	
 	// BobLennon capacities
 	public static boolean canBurnWool, canBurnWood, canBurnTNT, canBurnPlants, canBurnLeaves, isPyromaniac, isImmuneToFire;
 	public static int pyroRate;
 	
 
 	/*
 	 * IDs: items: 8400 ˆ 8407 blocs: 1200 entites: 101 ˆ 104 achievements: 888 ˆ
 	 * 901
 	 */
 
 	@PreInit
 	public void preload(FMLPreInitializationEvent event)
 	{
 		FBMlog.setParent(FMLLog.getLogger());
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
 		if(side == Side.CLIENT)
 		{
 		      MinecraftForge.EVENT_BUS.register(new FTMEventSound());      
 		}
 		
 		//config
 		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
 		cfg.load();
 		fantaSpawn = cfg.get("Mob Spawn", "Fanta", true).getBoolean(true);
 		BoblennonSpawn = cfg.get("Mob Spawn", "Boblennon", true).getBoolean(true);
 		JeankevinSpawn = cfg.get("Mob Spawn", "Jean kevin", true).getBoolean(true);
 		BotlennonSpawn = cfg.get("Mob Spawn", "Botlennon", true).getBoolean(true);
 
 		fantaSpawnRate = cfg.get("Mob Spawn Rate", "Fanta", 9).getInt();
 		BoblennonSpawnRate = cfg.get("Mob Spawn Rate", "Boblennon", 9).getInt();
 		JeankevinSpawnRate = cfg.get("Mob Spawn Rate", "Jean kevin", 10).getInt();
 		BotlennonSpawnRate = cfg.get("Mob Spawn Rate", "Botlennon", 9).getInt();
 
 		isImmuneToFire = cfg.get("BobLennon", "Immunetofire", true).getBoolean(true);
 		isPyromaniac = cfg.get("BobLennon", "Pyromaniac", false).getBoolean(true);
 		pyroRate = cfg.get("BobLennon", "Pyromaniac Rate", 40).getInt();
 		canBurnWood = cfg.get("BobLennon", "Fire Wood", true).getBoolean(true);
 		canBurnWool = cfg.get("BobLennon", "Fire Wool", true).getBoolean(true);
 		canBurnPlants = cfg.get("BobLennon", "Fire Plants", true).getBoolean(true);
 		canBurnTNT = cfg.get("BobLennon", "Fire TNT", true).getBoolean(true);
 		canBurnLeaves = cfg.get("BobLennon", "Fire Leaves", true).getBoolean(true);
 		
 		bouseID = cfg.getBlock("Bouse", 1200).getInt();
 		
         fantaGlassesID = cfg.getItem("Fanta Glasses", 8400).getInt();
 		cobbleTieID = cfg.getItem("Cobble Tie", 8401).getInt();
 		toothBrushID = cfg.getItem("Tooth Brush", 8402).getInt();
 		stampCollectionID = cfg.getItem("Stamp Collection", 8403).getInt();
 		bambooSwordID = cfg.getItem("Bamboo Sword", 8404).getInt();
 		hairPotionID = cfg.getItem("Hair Potion", 8405).getInt();
 		magabondChopID = cfg.getItem("Magabond Chop", 8406).getInt();
 		magabondRecordID = cfg.getItem("Magabond Record", 8407).getInt();
 		
 		cfg.save();
 
 	}
 
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 
 		// enregistrement des mobs
 		EntityRegistry.registerGlobalEntityID(EntityBob.class, "Boblennon", EntityRegistry.findGlobalUniqueEntityId(), 204, 13613421);
 		EntityRegistry.registerGlobalEntityID(EntityFanta.class, "TheFantasio974", EntityRegistry.findGlobalUniqueEntityId(), 0, 16777215);
 		EntityRegistry.registerGlobalEntityID(EntityJeanKevin.class, "JeanKevin", EntityRegistry.findGlobalUniqueEntityId(), 44975, 11762530);
 		EntityRegistry.registerGlobalEntityID(EntityBotlennon.class, "Botlennon", EntityRegistry.findGlobalUniqueEntityId(), 204, 16777215);
 
 		EntityRegistry.registerModEntity(EntityBob.class, "Boblennon", 101, this, 64, 1, true);
 		EntityRegistry.registerModEntity(EntityFanta.class, "TheFantasio974", 102, this, 64, 1, true);
 		EntityRegistry.registerModEntity(EntityJeanKevin.class, "JeanKevin", 103, this, 64, 1, true);
 		EntityRegistry.registerModEntity(EntityBotlennon.class, "Botlennon", 104, this, 64, 1, true);
 		
 		if (BoblennonSpawn)
 			EntityRegistry.addSpawn(EntityBob.class, BoblennonSpawnRate, 1, 2, EnumCreatureType.creature);
 		if (fantaSpawn)
 			EntityRegistry.addSpawn(EntityFanta.class, fantaSpawnRate, 1, 2, EnumCreatureType.creature);
 		if (JeankevinSpawn)
 			EntityRegistry.addSpawn(EntityJeanKevin.class, JeankevinSpawnRate, 2, 3, EnumCreatureType.creature);
 		if (BotlennonSpawn)
 			EntityRegistry.addSpawn(EntityBotlennon.class, BotlennonSpawnRate, 1, 3, EnumCreatureType.monster);
 		
 		//render
 		proxy.addEntityRender();
 
 		//Items
         fantaGlasses = new ItemFBMArmor(fantaGlassesID, FBMarmor, 1, 0).setUnlocalizedName("fantaGlasses").setMaxStackSize(64);
 		cobbleTie = new ItemFBMArmor(cobbleTieID, FBMarmor, 1, 1).setUnlocalizedName("cobbleTie");
 		toothBrush = new ItemObsidianToothBrush(toothBrushID).setUnlocalizedName("toothBrush");
 		stampCollection = new ItemStampCollection(stampCollectionID).setUnlocalizedName("stampCollection");
 		bambooSword = new ItemBambooSword(bambooSwordID, EnumToolMaterial.WOOD).setUnlocalizedName("bambooSword");
 		hairPotion = new ItemFBMArmor(hairPotionID, FBMarmor, 1, 0).setUnlocalizedName("hairPotion").setCreativeTab(CreativeTabs.tabMisc);
 		magabondChop = new ItemFBMFood(magabondChopID, 4, 0.1F, true).setPotionEffect(Potion.confusion.id, 20, 0, 0.8F).setUnlocalizedName("magabondChop");
 		magabondRecord = new ItemRecordFantaBob(magabondRecordID, "Magabond", "ATE BITS").setUnlocalizedName("magabondRecord");
 		
 		//Block
 		bouse = new BlockBouse(bouseID).setHardness(0.6F).setResistance(4.0F).setUnlocalizedName("bouse").setStepSound(new StepSound("bouse", 3.0F, 1.0F));
 
 		// enregistrement des blocs
 		GameRegistry.registerBlock(bouse, "bouse");
 		
 		// ajout des achievements
		installModAch = new Achievement(AchievementList.achievementList.size() + 1, "installModAch", -2, 0, Item.paper, null).registerAchievement();
		killBobAch = new Achievement(AchievementList.achievementList.size() + 1, "killBobAch", -4, 0, Block.cobblestone, AchievementList.openInventory).registerAchievement();
		killFantaAch = new Achievement(AchievementList.achievementList.size() + 1, "killFantaAch", -6, 0, Item.swordWood, AchievementList.openInventory).registerAchievement();
		floodAch = new Achievement(AchievementList.achievementList.size() + 1, "floodAch", -8, 0, Item.bucketLava, AchievementList.openInventory).registerAchievement();
		killJeanKevinAch = new Achievement(AchievementList.achievementList.size() + 1, "killJeanKevinAch", -10, 0, Item.arrow, AchievementList.openInventory).registerAchievement();
		killBotlennonAch = new Achievement(AchievementList.achievementList.size() + 1, "killBotlennonAch", -12, 0, Item.ingotIron, AchievementList.openInventory).registerAchievement();
		getBouseAch = new Achievement(AchievementList.achievementList.size() + 1, "getBouseAch", -14, 0, bouse, AchievementList.openInventory).registerAchievement();
		brushTeethAch = new Achievement(AchievementList.achievementList.size() + 1, "brushTeethAch", -14, 2, toothBrush, AchievementList.openInventory).registerAchievement();
		getHairPotionAch = new Achievement(AchievementList.achievementList.size() + 1, "getHairPotionAch", -12, 2, hairPotion, AchievementList.openInventory).registerAchievement();
		getStampCollectionAch = new Achievement(AchievementList.achievementList.size() + 1, "getStampCollectionAch", -10, 2, stampCollection, AchievementList.openInventory).registerAchievement();
		getCobbleTieAch = new Achievement(AchievementList.achievementList.size() + 1, "getCobbleTieAch", -8, 2, cobbleTie, AchievementList.openInventory).registerAchievement();
		getBambooSwordAch = new Achievement(AchievementList.achievementList.size() + 1, "getBambooSwordAch", -6, 2, bambooSword, AchievementList.openInventory).registerAchievement();
		getMagabondChopAch = new Achievement(AchievementList.achievementList.size() + 1, "getMagabondChopAch", -8, -2, magabondChop, killJeanKevinAch).registerAchievement();
		craftMagabondRecAch = new Achievement(AchievementList.achievementList.size() + 1, "craftMagabondRecAch", -10, -2, magabondRecord, getMagabondChopAch).registerAchievement();
		getFantaGlassesAch = new Achievement(AchievementList.achievementList.size() + 1, "getFantaGlassesAch", -6, -2, fantaGlasses, killFantaAch).registerAchievement();
 		
 		addAchievementLocalizations();
 		
 		//Handler pour les achivement
 		GameRegistry.registerCraftingHandler(craftHandler);
 		GameRegistry.registerPickupHandler(pickupHandler);
 		GameRegistry.registerPlayerTracker(playerHandler);
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		// recettes de craft
 		GameRegistry.addRecipe(new ItemStack(fantaGlasses, 1), new Object[] 
 			{
 				"# #", "# #", "X#X", '#', Item.stick, 'X', Block.glass 
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(cobbleTie, 1), new Object[]
 			{
 				"#", "X", "X", '#', Item.silk, 'X', Block.cobblestone
 			});
 
 		GameRegistry.addRecipe(new ItemStack(toothBrush, 1), new Object[] 
 			{ 
 				"#X", "# ", "# ", '#', Block.obsidian, 'X', Block.cloth
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(stampCollection, 1), new Object[] 
 			{
 				"#XX", "#XX", "#XX", '#', Item.book, 'X', Item.paper 
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(bambooSword, 1), new Object[] 
 			{
 				"#", "#", "X", '#', Item.reed, 'X', Item.stick
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(hairPotion, 1), new Object[] 
 			{ 
 				" # ", "#X#", " # ", '#', Block.glass, 'X', Item.redstone
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(bouse, 3), new Object[] 
 			{ 
 				"X", "#", '#', Block.dirt, 'X', Item.bucketWater
 			});
 		
 		GameRegistry.addRecipe(new ItemStack(magabondRecord, 1), new Object[]
 			{
 				" # ", "#X#", " # ", '#', magabondChop, 'X', Item.redstone
 			});
 
 		//Localization 
 		LanguageRegistry.addName(fantaGlasses, "Lunettes de Fantasio");
 		LanguageRegistry.addName(cobbleTie, "Cravate en cobble");
 		LanguageRegistry.addName(toothBrush, "Brosse à dents en obsidienne");
 		LanguageRegistry.addName(stampCollection, "Collection de timbres");
 		LanguageRegistry.addName(bambooSword, "Epée en bambou");
 		LanguageRegistry.addName(hairPotion, "Potion capillaire de Papy Lennon");
 		LanguageRegistry.addName(bouse, "Bouse");
 		LanguageRegistry.addName(magabondChop, "Côtelette de magabond");
 		LanguageRegistry.addName(magabondRecord, "Disque");
 		LanguageRegistry.instance().addStringLocalization("entity.Boblennon.name", "Boblennon");
 		LanguageRegistry.instance().addStringLocalization("entity.TheFantasio974.name", "TheFantasio974");
 		LanguageRegistry.instance().addStringLocalization("entity.JeanKevin.name", "Jean-Kevin");
 		LanguageRegistry.instance().addStringLocalization("entity.Botlennon.name", "Botlennon");
 	}
 	
     private static void addAchievementLocalizations()
     {
     	addAchievementName("installModAch", "Youtuber");
     	addAchievementDesc("installModAch", "Installer le mod FantaBobShow");
     	addAchievementName("killBobAch", "Bob Lennon Hater");
     	addAchievementDesc("killBobAch", "Tuez Bob Lennon");
     	addAchievementName("killFantaAch", "Spirou");
     	addAchievementDesc("killFantaAch", "Tuez Fantasio");
     	addAchievementName("floodAch", "Flooooood !");
     	addAchievementDesc("floodAch", "Se faire suivre par Jean-Kevin");
     	addAchievementName("killJeanKevinAch", "Sauveur de l'humanité");
     	addAchievementDesc("killJeanKevinAch", "Tuer Jean-Kevin");
     	addAchievementName("killBotlennonAch", "Tas de ferraille");
     	addAchievementDesc("killBotlennonAch", "Tuez Botlennon");
     	addAchievementName("getBouseAch", "Elément naturel");
     	addAchievementDesc("getBouseAch", "Craftez de la bouse");
     	addAchievementName("brushTeethAch", "Hygiène dentaire");
     	addAchievementDesc("brushTeethAch", "Se brosser les dents");
     	addAchievementName("getHairPotionAch", "Crane rasé");
     	addAchievementDesc("getHairPotionAch", "Craftez une potion capillaire");
     	addAchievementName("getStampCollectionAch", "Philatéliste");
     	addAchievementDesc("getStampCollectionAch", "Craftez une collection de timbres");
     	addAchievementName("getCobbleTieAch", "Bonjour patron !");
     	addAchievementDesc("getCobbleTieAch", "Craftez une cravate en cobble");
     	addAchievementName("getBambooSwordAch", "Assassin au naturel");
     	addAchievementDesc("getBambooSwordAch", "Craftez une épée en bambou");
     	addAchievementName("craftMagabondRecAch", "Magabooond");
     	addAchievementDesc("craftMagabondRecAch", "Craftez un magabond remix");
     	addAchievementName("getFantaGlassesAch", "Binoclard");
     	addAchievementDesc("getFantaGlassesAch", "Récuperer les lunettes de Fantasio");
     	addAchievementName("getMagabondChopAch", "Repas avarié");
     	addAchievementDesc("getMagabondChopAch", "Obtenir une côtelette de magabond");
     }
 	
 	private static void addAchievementName(String ach, String name)
 	{
 	        LanguageRegistry.instance().addStringLocalization("achievement." + ach, "en_US", name);
 	}
 
 	private static void addAchievementDesc(String ach, String desc)
 	{
 	        LanguageRegistry.instance().addStringLocalization("achievement." + ach + ".desc", "en_US", desc);
 	}
 }
