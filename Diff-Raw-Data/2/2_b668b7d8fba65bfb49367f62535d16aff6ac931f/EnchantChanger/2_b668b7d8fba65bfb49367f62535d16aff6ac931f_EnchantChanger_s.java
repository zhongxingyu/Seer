 package ak.EnchantChanger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnchantmentHelper;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.Loader;
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
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="EnchantChanger", name="EnchantChanger", version="1.6m-universal")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels={"EC|Levi","EC|CSC","EC|CS","EC|Sw"}, packetHandler=Packet_EnchantChanger.class)
 public class EnchantChanger //extends BaseMod
 {
 	public static int ExExpBottleID ;
 	public static  Item ItemExExpBottle ;
 	public static int MateriaID;
 	public static  Item ItemMat ;
 	public static int ZackSwordItemID ;
 	public static  Item ItemZackSword ;
 	public static int CloudSwordItemID ;
 	public static  Item ItemCloudSword ;
 	public static int FirstSwordItemID;
 	public static  Item ItemCloudSwordCore ;
 	public static int SephirothSwordItemID ;
 	public static  Item ItemSephirothSword ;
 	public static int UltimateWeaponItemID ;
 	public static  Item ItemUltimateWeapon ;
 	public static int PortableEnchantChangerID ;
 	public static  Item ItemPortableEnchantChanger ;
 	public static int PortableEnchantmentTableID ;
 	public static  Item ItemPortableEnchantmentTable ;
 	public static int MasterMateriaID;
 	public static  Item MasterMateria ;
 	public static int ImitateSephSwordID;
 	public static Item ItemImitateSephirothSword;
 	public static int EnchantChangerID;
 	public static Block BlockMat;
 	public static int HugeMateriaID;
 	public static Block HugeMateria;
 	public static Item ItemHugeMateria;
 	public static boolean LevelCap;
 	public static boolean Debug;
 	public static float MeteoPower;
 	public static float MeteoSize;
 	public static String SwordIds = "";
 	public static ArrayList<Integer> SwordIdArray = new ArrayList<Integer>();
 	public static String ToolIds = "";
 	public static ArrayList<Integer> ToolIdArray = new ArrayList<Integer>();
 	public static String BowIds = "";
 	public static ArrayList<Integer> BowIdArray = new ArrayList<Integer>();
 	public static String ArmorIds = "";
 	public static ArrayList<Integer> ArmorIdArray = new ArrayList<Integer>();
 
 	public static boolean DecMateriaLv;
 	public static boolean YouAreTera;
 	public static int MateriaPotionMinutes;
 	public static int Difficulty;
 	public static double AbsorpBoxSize = 5D;
 	public static int MaxLv = 127;
 	public static boolean enableAPSystem;
 	public static boolean enableDungeonLoot;
 	public static int aPBasePoint;
 
 	public static int EnchantmentMeteoId;
 	public static Enchantment Meteo;
 	public static int EndhantmentHolyId;
 	public static Enchantment Holy;
 	public static int EnchantmentTelepoId;
 	public static Enchantment Telepo;
 	public static int EnchantmentFloatId;
 	public static Enchantment Float;
 	public static int EnchantmentThunderId;
 	public static Enchantment Thunder;
 	
 	public static String EcMeteoPNG = "/mods/ak/EnchantChanger/textures/items/Meteo.png";
 	public static String EcExpBottlePNG = "/mods/ak/EnchantChanger/textures/items/ExExpBottle.png";
 	public static String EcZackSwordPNG ="/ak/EnchantChanger/textures/item/ZackSword.png";
 	public static String EcSephirothSwordPNG ="/ak/EnchantChanger/textures/item/SephirothSword.png";
 	public static String EcCloudSwordPNG ="/ak/EnchantChanger/textures/item/CloudSword.png";
 	public static String EcCloudSword2PNG ="/ak/EnchantChanger/textures/item/CloudSword-3Dtrue.png";
 	public static String EcCloudSwordCorePNG ="/ak/EnchantChanger/textures/item/CloudSwordCore.png";
 	public static String EcCloudSwordCore2PNG ="/ak/EnchantChanger/textures/item/CloudSwordCore-3Dtrue.png";
 	public static String EcUltimateWeaponPNG ="/ak/EnchantChanger/textures/item/UltimaWeapon.png";
 	public static String EcGuiMaterializer ="/ak/EnchantChanger/textures/gui/materializer.png";
 	public static String EcGuiHuge = "/ak/EnchantChanger/textures/gui/HugeMateriaContainer.png";
 	public static String EcHugetex ="/ak/EnchantChanger/textures/item/hugemateriatex.png";
 	public static String EcTextureDomain = "ak/EnchantChanger:";
 
 	public static boolean incompatible = false;
 	public static boolean loadMTH = false;
 	@Instance("EnchantChanger")
 	public static EnchantChanger instance;
 	@SidedProxy(clientSide = "ak.EnchantChanger.Client.ClientProxy", serverSide = "ak.EnchantChanger.CommonProxy")
 	public static CommonProxy proxy;
 	public static int guiIdMaterializer = 0;
 	public static int guiIdPortableEnchantmentTable = 1;
 	public static int guiIdHugeMateria=2;
 	public static HashMap<Integer, Integer> apLimit = new HashMap();
 	public static HashSet<Integer> magicEnchantment = new HashSet();
 	public static final CreativeTabs tabsEChanger = new CreativeTabEC("EnchantChanger");
 	public static LivingEventHooks livingeventhooks;
 
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 
 		EnchantChangerID = config.get(Configuration.CATEGORY_BLOCK, "EnchantChanger Id",2000).getInt();
 		HugeMateriaID = config.get(Configuration.CATEGORY_BLOCK, "Huge Materia Id",2001).getInt();
 		ExExpBottleID = config.get(Configuration.CATEGORY_ITEM, "ExExpBottle Id", 4999).getInt();
 		MateriaID = config.get(Configuration.CATEGORY_ITEM, "Materia Id", 5000).getInt();
 		ZackSwordItemID = config.get(Configuration.CATEGORY_ITEM, "BasterSword Id", 5001).getInt();
 		CloudSwordItemID = config.get(Configuration.CATEGORY_ITEM, "UnionSword Id", 5002).getInt();
 		FirstSwordItemID = config.get(Configuration.CATEGORY_ITEM, "FirstSword Id", 5003).getInt();
 		SephirothSwordItemID = config.get(Configuration.CATEGORY_ITEM, "Masamune Blade Id", 5004).getInt();
 		UltimateWeaponItemID = config.get(Configuration.CATEGORY_ITEM, "Ultimate Weapon Id", 5005).getInt();
 		PortableEnchantChangerID = config.get(Configuration.CATEGORY_ITEM, "Portable Enchant Changer Id", 5006).getInt();
 		PortableEnchantmentTableID = config.get(Configuration.CATEGORY_ITEM, "Portable Enchantment Table Id", 5007).getInt();
 		MasterMateriaID = config.get(Configuration.CATEGORY_ITEM, "Master Materia Id", 5008).getInt();
 		ImitateSephSwordID = config.get(Configuration.CATEGORY_ITEM, "Imitate Masamune Blade Id", 5009).getInt();
 		Property LevelCapProp = config.get(Configuration.CATEGORY_GENERAL, "LevelCap", false);
 		LevelCapProp.comment="TRUE:You cannot change a Materia to a enchantment over max level of the enchantment.";
 		LevelCap = LevelCapProp.getBoolean(false);
 		Property DebugProp = config.get(Configuration.CATEGORY_GENERAL, "Debug mode", false);
 		DebugProp.comment="For Debugger";
 		Debug = DebugProp.getBoolean(false);
 		enableAPSystem = config.get(Configuration.CATEGORY_GENERAL, "enableAPSystem", true).getBoolean(true);
 		enableDungeonLoot = config.get(Configuration.CATEGORY_GENERAL, "enableDungeonLoot", true).getBoolean(true);
		aPBasePoint = config.get(Configuration.CATEGORY_GENERAL, "APBAsePoint", 100).getInt();
 		Property SwordIdsProp = config.get(Configuration.CATEGORY_GENERAL, "Extra SwordIds", "267");
 		SwordIdsProp.comment="Put Ids which you want to operate as  swords. Usage: 1,2,3";
 		SwordIds= SwordIdsProp.getString();
 		Property ToolIdsProp = config.get(Configuration.CATEGORY_GENERAL, "Extra ToolIds", "257");
 		ToolIdsProp.comment="Put Ids which you want to operate as  swords. Usage: 1,2,3";
 		ToolIds = ToolIdsProp.getString();
 		Property BowIdsProp = config.get(Configuration.CATEGORY_GENERAL, "Extra BowIds", "261");
 		BowIdsProp.comment="Put Ids which you want to operate as  bows. Usage: 1,2,3";
 		BowIds = BowIdsProp.getString();
 		Property ArmorIdsProp = config.get(Configuration.CATEGORY_GENERAL, "Extra ArmorIds", "298");
 		ArmorIdsProp.comment="Put Ids which you want to operate as  armors. Usage: 1,2,3";
 		ArmorIds = ArmorIdsProp.getString();
 
 		Property DecMateriaLvProp = config.get(Configuration.CATEGORY_GENERAL, "DecMateriaLv", false);
 		DecMateriaLvProp.comment= "TRUE:The level of extracted Materia is decreased by the item damage";
 		DecMateriaLv = DecMateriaLvProp.getBoolean(false);
 		Property YouAreTeraProp = config.get(Configuration.CATEGORY_GENERAL, "YouAreTera", false);
 		YouAreTeraProp.comment="TRUE:You become Tera in FF4. It means that you can use Magic Materia when your MP is exhausted";
 		YouAreTera = YouAreTeraProp.getBoolean(false);
 		Property MeteoPowerProp =config.get(Configuration.CATEGORY_GENERAL, "METEO POWER", 10);
 		MeteoPowerProp.comment="This is a power of Meteo";
 		MeteoPower = (float)MeteoPowerProp.getInt();
 		Property MeteoSizeProp =config.get(Configuration.CATEGORY_GENERAL, "Meteo Size", 10);
 		MeteoSizeProp.comment="This is a Size of Meteo";
 		MeteoSize = (float)MeteoSizeProp.getInt();
 		Property MateriaPotionMinutesProp = config.get(Configuration.CATEGORY_GENERAL, "Materia Potion Minutes", 10);
 		MateriaPotionMinutesProp.comment = "How long minutes Materia put potion effect to MOB or ANIMAL";
 		MateriaPotionMinutes = MateriaPotionMinutesProp.getInt();
 
 		Property DifficultyProp = config.get(Configuration.CATEGORY_GENERAL, "Difficulty", 1);
 		DifficultyProp.comment="Difficulty of this MOD. 0 = Easy, 1 = Normal, 2 = Hard";
 		Difficulty = DifficultyProp.getInt();
 		EnchantmentMeteoId = config.get(Configuration.CATEGORY_GENERAL, "EnchantmentMeteoId", 240).getInt();
 		EndhantmentHolyId = config.get(Configuration.CATEGORY_GENERAL, "EndhantmentHolyId", 241).getInt();
 		EnchantmentTelepoId = config.get(Configuration.CATEGORY_GENERAL, "EnchantmentTelepoId", 242).getInt();
 		EnchantmentFloatId = config.get(Configuration.CATEGORY_GENERAL, "EnchantmentFloatId", 243).getInt();
 		EnchantmentThunderId = config.get(Configuration.CATEGORY_GENERAL, "EnchantmentThunderId", 244).getInt();
 
 		config.save();
 	}
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		this.initMaps();
 		ItemMat = (new EcItemMateria(MateriaID-256)).setUnlocalizedName(this.EcTextureDomain + "Materia").setCreativeTab(tabsEChanger);
 		ItemExExpBottle =new EcItemExExpBottle(ExExpBottleID-256).setUnlocalizedName(this.EcTextureDomain + "ExExpBottle").setCreativeTab(tabsEChanger);
 		ItemZackSword = (new EcItemZackSword(ZackSwordItemID-256)).setUnlocalizedName(this.EcTextureDomain + "ZackSword").setCreativeTab(tabsEChanger);
 		ItemCloudSwordCore = (new EcItemCloudSwordCore(FirstSwordItemID -256)).setUnlocalizedName(this.EcTextureDomain + "CloudSwordCore").setCreativeTab(tabsEChanger);
 		ItemCloudSword = (new EcItemCloudSword(CloudSwordItemID-256)).setUnlocalizedName(this.EcTextureDomain + "CloudSword").setCreativeTab(null);
 		ItemSephirothSword = (new EcItemSephirothSword(SephirothSwordItemID-256)).setUnlocalizedName(this.EcTextureDomain + "MasamuneBlade").setCreativeTab(tabsEChanger);
 		ItemUltimateWeapon = (new EcItemUltimateWeapon(UltimateWeaponItemID - 256)).setUnlocalizedName(this.EcTextureDomain + "UltimateWeapon").setCreativeTab(tabsEChanger);
 		ItemPortableEnchantChanger = (new EcItemMaterializer(PortableEnchantChangerID - 256)).setUnlocalizedName(this.EcTextureDomain + "PortableEnchantChanger").setCreativeTab(tabsEChanger);
 		ItemPortableEnchantmentTable = (new EcItemEnchantmentTable(PortableEnchantmentTableID - 256)).setUnlocalizedName(this.EcTextureDomain + "PortableEnchantmentTable").setCreativeTab(tabsEChanger);
 		MasterMateria = new EcItemMasterMateria(MasterMateriaID - 256).setUnlocalizedName(this.EcTextureDomain + "MasterMateria").setCreativeTab(tabsEChanger);
 		ItemImitateSephirothSword = (new EcItemSephirothSwordImit(ImitateSephSwordID-256)).setUnlocalizedName(this.EcTextureDomain + "ImitateMasamuneBlade").setCreativeTab(tabsEChanger);
 		BlockMat = (new EcBlockMaterialize(EnchantChangerID)).setCreativeTab(tabsEChanger).setUnlocalizedName("EnchantChanger");
 		HugeMateria = new EcBlockHugeMateria(HugeMateriaID).setUnlocalizedName("HugeMateria");
 		ItemHugeMateria = new EcItemHugeMateria(HugeMateria.blockID - 256).setUnlocalizedName(this.EcTextureDomain + "HugeMateria").setCreativeTab(tabsEChanger);
 
 		Meteo = new EcEnchantmentMeteo(this.EnchantmentMeteoId,0);
 		Holy = new EcEnchantmentHoly(this.EndhantmentHolyId,0);
 		Telepo = new EcEnchantmentTeleport(this.EnchantmentTelepoId,0);
 		Float = new EcEnchantmentFloat(this.EnchantmentFloatId,0);
 		Thunder = new EcEnchantmentThunder(this.EnchantmentThunderId,0);
 
 		livingeventhooks = new LivingEventHooks();
 		MinecraftForge.EVENT_BUS.register(livingeventhooks);
 		GameRegistry.registerBlock(BlockMat,"EnchantChanger");
 		GameRegistry.registerTileEntity(EcTileEntityMaterializer.class, "container.materializer");
 		GameRegistry.registerTileEntity(EcTileEntityHugeMateria.class, "container.hugeMateria");
 
 		EntityRegistry.registerModEntity(EcEntityExExpBottle.class, "ItemExExpBottle", 0, this, 250, 5, true);
 		EntityRegistry.registerModEntity(EcEntityMeteo.class, "Meteo", 1, this, 250, 5, true);
 		EntityRegistry.registerModEntity(EcEntityApOrb.class, "apOrb", 2, this, 64, 1, false);
 
 		//"this" is an instance of the mod class
 		NetworkRegistry.instance().registerGuiHandler(this, proxy);
 		proxy.registerRenderInformation();
 		proxy.registerTileEntitySpecialRenderer();
 		MinecraftForge.setToolClass(ItemSephirothSword, "FF7", 0);
 		Block[] pickeff =
 			{
 				Block.cobblestone,Block.stone,
 				Block.sandStone,   Block.cobblestoneMossy,
 				Block.oreCoal,     Block.ice,
 				Block.netherrack,  Block.oreLapis,
 				Block.blockLapis,  Block.oreRedstone,
 				Block.obsidian,    Block.oreRedstoneGlowing
 			};
 		for (Block block : pickeff)
 		{
 			MinecraftForge.removeBlockEffectiveness(block, "FF7");
 			MinecraftForge.setBlockHarvestLevel(block, "FF7", 0);
 		}
 
 		StringtoInt(SwordIds,SwordIdArray);
 		StringtoInt(ToolIds,ToolIdArray);
 		StringtoInt(BowIds,BowIdArray);
 		StringtoInt(ArmorIds,ArmorIdArray);
 
 		if(this.Difficulty < 2)
 			GameRegistry.addRecipe(new EcMateriaRecipe());
 		GameRegistry.addRecipe(new EcMasterMateriaRecipe());
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemMat,1, 0), new Object[]{new ItemStack(Item.diamond, 1), new ItemStack(Item.enderPearl, 1)});
 		GameRegistry.addRecipe(new ItemStack(ItemZackSword, 1), new Object[]{" X","XX"," Y", Character.valueOf('X'),Block.blockIron, Character.valueOf('Y'),Item.ingotIron});
 		GameRegistry.addRecipe(new ItemStack(ItemCloudSwordCore, 1), new Object[]{" X ","XYX"," Z ", Character.valueOf('X'), Block.blockIron, Character.valueOf('Y'), new ItemStack(ItemMat, 1,0), Character.valueOf('Z'),Item.ingotIron});
 		GameRegistry.addRecipe(new ItemStack(ItemSephirothSword, 1), new Object[]{"  A"," B ","C  ",Character.valueOf('A'),Item.ingotIron, Character.valueOf('B'),new ItemStack(Item.swordDiamond, 1, 0), Character.valueOf('C'),new ItemStack(ItemMat, 1, 1)});
 		GameRegistry.addRecipe(new ItemStack(ItemUltimateWeapon, 1), new Object[]{" A ","ABA"," C ", Character.valueOf('A'),Block.blockDiamond, Character.valueOf('B'), new ItemStack(MasterMateria, 1,OreDictionary.WILDCARD_VALUE), Character.valueOf('C'),Item.stick});
 		GameRegistry.addRecipe(new ItemStack(ItemImitateSephirothSword), "  A"," A ", "B  ", 'A', Item.ingotIron, 'B', Item.swordIron);
 		GameRegistry.addRecipe(new ItemStack(BlockMat, 1), new Object[]{"XYX","ZZZ", Character.valueOf('X'),Item.diamond, Character.valueOf('Y'),Block.blockGold, Character.valueOf('Z'),Block.obsidian});
 		GameRegistry.addRecipe(new ItemStack(HugeMateria), new Object[]{" A ","ABA"," A ",'A',Block.blockDiamond,'B',Item.netherStar});
 		GameRegistry.addRecipe(new ItemStack(HugeMateria), new Object[]{" A ","ABA"," A ",'A',Block.blockDiamond,'B',new ItemStack(MasterMateria,1,OreDictionary.WILDCARD_VALUE)});
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemPortableEnchantChanger,1),  new Object[]{BlockMat});
 		GameRegistry.addShapelessRecipe(new ItemStack(ItemPortableEnchantmentTable,1),  new Object[]{Block.enchantmentTable});
 		GameRegistry.addShapelessRecipe(new ItemStack(MasterMateria,1,0), new Object[]{new ItemStack(MasterMateria,1,1), new ItemStack(MasterMateria,1,2), new ItemStack(MasterMateria,1,3), new ItemStack(MasterMateria,1,4), new ItemStack(MasterMateria,1,5)});
 		if(this.Difficulty == 0)
 			GameRegistry.addRecipe(new ItemStack(Item.expBottle, 8), new Object[]{"XXX","XYX","XXX", Character.valueOf('X'),new ItemStack(Item.potion, 1, 0), Character.valueOf('Y'), new ItemStack(Item.diamond, 1)});
 		GameRegistry.addRecipe(new ItemStack(ItemExExpBottle, 8), new Object[]{"XXX","XYX","XXX", Character.valueOf('X'),new ItemStack(Item.expBottle, 1, 0), Character.valueOf('Y'), new ItemStack(Block.blockDiamond, 1)});
 		GameRegistry.addRecipe(new ItemStack(Block.dragonEgg,1), new Object[]{"XXX","XYX","XXX",Character.valueOf('X'), Item.eyeOfEnder, Character.valueOf('Y'), new ItemStack(MasterMateria,1,OreDictionary.WILDCARD_VALUE)});
 		if(this.enableDungeonLoot)
 			this.DungeonLootItemResist();
 		if(this.Debug)
 			DebugSystem();
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		this.loadMTH = Loader.isModLoaded("MultiToolHolders");
 		AddLocalization();
 		this.incompatible = this.checkCompatibility();
 	}
 	public void AddLocalization()
 	{
 		LanguageRegistry.addName(ItemExExpBottle, "Ex Exp Bottle");
 		LanguageRegistry.addName(BlockMat, "Enchant Changer");
 		LanguageRegistry.addName(ItemMat,  "Materia");
 		LanguageRegistry.addName(HugeMateria, "HugeMateria");
 		LanguageRegistry.addName(ItemHugeMateria, "HugeMateria");
 		LanguageRegistry.addName(ItemZackSword, "Buster Sword");
 		LanguageRegistry.addName(ItemCloudSword, "Union Sword");
 		LanguageRegistry.addName(ItemCloudSwordCore, "First Sword");
 		LanguageRegistry.addName(ItemSephirothSword, "Masamune Blade");
 		LanguageRegistry.addName(ItemUltimateWeapon, "Ultimate Weapon");
 		LanguageRegistry.addName(ItemPortableEnchantChanger, "Portable Enchant Changer");
 		LanguageRegistry.addName(ItemPortableEnchantmentTable,"Portable Enchantment Table");
 		LanguageRegistry.addName(ItemImitateSephirothSword, "1/1 Masamune Blade(Imitation)");
 		LanguageRegistry.instance().addStringLocalization("enchantment.Meteo", "Meteo");
 		LanguageRegistry.instance().addStringLocalization("enchantment.Holy", "Holy");
 		LanguageRegistry.instance().addStringLocalization("enchantment.Teleport", "Teleport");
 		LanguageRegistry.instance().addStringLocalization("enchantment.Floating", "Floating");
 		LanguageRegistry.instance().addStringLocalization("enchantment.Thunder", "Thunder");
 		LanguageRegistry.instance().addNameForObject(BlockMat, "ja_JP","エンチャントチェンジャー");
 		LanguageRegistry.instance().addNameForObject(ItemMat, "ja_JP", "マテリア");
 		LanguageRegistry.instance().addNameForObject(HugeMateria, "ja_JP","ヒュージマテリア");
 		LanguageRegistry.instance().addNameForObject(ItemHugeMateria, "ja_JP","ヒュージマテリア");
 		LanguageRegistry.instance().addNameForObject(ItemExExpBottle, "ja_JP", "エンチャントの瓶EX");
 		LanguageRegistry.instance().addNameForObject(ItemZackSword, "ja_JP","バスターソード");
 		LanguageRegistry.instance().addNameForObject(ItemCloudSword, "ja_JP","合体剣");
 		LanguageRegistry.instance().addNameForObject(ItemCloudSwordCore,"ja_JP" ,"ファースト剣");
 		LanguageRegistry.instance().addNameForObject(ItemSephirothSword,"ja_JP","正宗");
 		LanguageRegistry.instance().addNameForObject(ItemUltimateWeapon,"ja_JP","究極剣");
 		LanguageRegistry.instance().addNameForObject(ItemPortableEnchantChanger, "ja_JP","携帯エンチャントチェンジャー");
 		LanguageRegistry.instance().addNameForObject(ItemPortableEnchantmentTable, "ja_JP","携帯エンチャントテーブル");
 		LanguageRegistry.instance().addNameForObject(ItemImitateSephirothSword, "ja_JP","1/1 マサムネブレード");
 		LanguageRegistry.instance().addStringLocalization("ItemMateria.Base.name", "Inactive Materia");
 		LanguageRegistry.instance().addStringLocalization("ItemMateria.Base.name", "ja_JP","不活性マテリア");
 		LanguageRegistry.instance().addStringLocalization("ItemMateria.name", "Materia");
 		LanguageRegistry.instance().addStringLocalization("ItemMateria.name", "ja_JP","マテリア");
 		LanguageRegistry.instance().addStringLocalization("container.materializer", "Enchant Changer");
 		LanguageRegistry.instance().addStringLocalization("container.materializer", "ja_JP", "エンチャントチェンジャー");
 		LanguageRegistry.instance().addStringLocalization("container.hugeMateria", "HugeMateria");
 		LanguageRegistry.instance().addStringLocalization("container.hugeMateria", "ja_JP", "ヒュージマテリア");
 		LanguageRegistry.instance().addStringLocalization("Key.EcMagic", "Magic Key");
 		LanguageRegistry.instance().addStringLocalization("Key.EcMagic", "ja_JP", "魔法キー");
 
 		for(int i=0;i < EcItemMateria.MagicMateriaNum;i++)
 		{
 			LanguageRegistry.instance().addStringLocalization("ItemMateria." + EcItemMateria.MateriaMagicNames[i]+".name", EcItemMateria.MateriaMagicNames[i]+" Materia");
 			LanguageRegistry.instance().addStringLocalization("ItemMateria." + EcItemMateria.MateriaMagicNames[i]+".name", "ja_JP",EcItemMateria.MateriaMagicJPNames[i]+"マテリア");
 		}
 
 		for(int i = 0;i< EcItemMasterMateria.MasterMateriaNum;i++)
 		{
 			LanguageRegistry.instance().addStringLocalization("ItemMasterMateria." + i + ".name", "Master Materia of " + EcItemMasterMateria.MasterMateriaNames[i]);
 			LanguageRegistry.instance().addStringLocalization("ItemMasterMateria." + i + ".name","ja_JP", EcItemMasterMateria.MasterMateriaJPNames[i] + "のマスターマテリア");
 		}
 		for(int i = 11;i<this.MaxLv + 1;i++)
 		{
 			LanguageRegistry.instance().addStringLocalization("enchantment.level."+i, i+"");
 		}
 	}
 	private void initMaps()
 	{
 		this.magicEnchantment.add(this.EnchantmentMeteoId);
 		this.magicEnchantment.add(this.EnchantmentFloatId);
 		this.magicEnchantment.add(this.EndhantmentHolyId);
 		this.magicEnchantment.add(this.EnchantmentTelepoId);
 		this.magicEnchantment.add(this.EnchantmentThunderId);
 		this.apLimit.put(0, 2*aPBasePoint);
 		this.apLimit.put(1, 1*aPBasePoint);
 		this.apLimit.put(2, 1*aPBasePoint);
 		this.apLimit.put(3, 1*aPBasePoint);
 		this.apLimit.put(4, 1*aPBasePoint);
 		this.apLimit.put(5, 1*aPBasePoint);
 		this.apLimit.put(6, 1*aPBasePoint);
 		this.apLimit.put(7, 1*aPBasePoint);
 		this.apLimit.put(16, 2*aPBasePoint);
 		this.apLimit.put(17, 1*aPBasePoint);
 		this.apLimit.put(18, 1*aPBasePoint);
 		this.apLimit.put(19, 1*aPBasePoint);
 		this.apLimit.put(20, 1*aPBasePoint);
 		this.apLimit.put(21, 3*aPBasePoint);
 		this.apLimit.put(32, 1*aPBasePoint);
 		this.apLimit.put(33, 1*aPBasePoint);
 		this.apLimit.put(34, 1*aPBasePoint);
 		this.apLimit.put(35, 2*aPBasePoint);
 		this.apLimit.put(48, 2*aPBasePoint);
 		this.apLimit.put(49, 1*aPBasePoint);
 		this.apLimit.put(50, 1*aPBasePoint);
 		this.apLimit.put(51, 1*aPBasePoint);
 	}
 	public static boolean isApLimit(int Id, int Lv, int ap)
 	{
 		if(EnchantChanger.getApLimit(Id, Lv) < ap)
 			return true;
 		else
 			return false;
 	}
 	public static int getApLimit(int Id, int Lv)
 	{
 		if(EnchantChanger.apLimit.containsKey(Id))
 		{
 			return ((int)EnchantChanger.apLimit.get(Id)) * (Lv / 5 + 1);
 		}
 		else
 			return 150*(Lv / 5 + 1);
 	}
 	public static void addEnchantmentToItem(ItemStack item, Enchantment enchantment, int Lv)
 	{
 		if (item.stackTagCompound == null)
 		{
 			item.setTagCompound(new NBTTagCompound());
 		}
 
 		if (!item.stackTagCompound.hasKey("ench"))
 		{
 			item.stackTagCompound.setTag("ench", new NBTTagList("ench"));
 		}
 
 		NBTTagList var3 = (NBTTagList)item.stackTagCompound.getTag("ench");
 		NBTTagCompound var4 = new NBTTagCompound();
 		var4.setShort("id", (short)enchantment.effectId);
 		var4.setShort("lvl", (short)(Lv));
 		var3.appendTag(var4);
 	}
 	public static int getMateriaEnchKind(ItemStack item)
 	{
 		int EnchantmentKind = 256;
 		for(int i = 0; i < Enchantment.enchantmentsList.length; i++)
 		{
 			if(EnchantmentHelper.getEnchantmentLevel(i, item) > 0)
 			{
 				EnchantmentKind = i;
 				break;
 			}
 		}
 		return EnchantmentKind;
 	}
 	public static int getMateriaEnchLv(ItemStack item)
 	{
 		int Lv = 0;
 		for(int i = 0; i < Enchantment.enchantmentsList.length; i++)
 		{
 			if(EnchantmentHelper.getEnchantmentLevel(i, item) > 0)
 			{
 				Lv = EnchantmentHelper.getEnchantmentLevel(i, item);
 				break;
 			}
 		}
 		return Lv;
 	}
 	public void StringtoInt(String ExtraIDs, ArrayList<Integer> IDList)
 	{
 		if(!ExtraIDs.isEmpty())
 		{
 			StringBuffer IDNum=new StringBuffer();
 			int shift=0;
 			for(int i=0; i < ExtraIDs.length(); i++)
 			{
 				if(ExtraIDs.charAt(i) == ',')
 				{
 					for(int j=shift; j < i;j++)
 					{
 						IDNum.append(ExtraIDs.charAt(j));
 					}
 					System.out.println(IDNum);
 					IDList.add(Integer.valueOf(IDNum.toString())-256);
 					shift = i+1;
 					IDNum = new StringBuffer();
 				}
 			}
 			for(int j=shift; j < ExtraIDs.length();j++)
 			{
 				IDNum.append(ExtraIDs.charAt(j));
 			}
 			System.out.println(IDNum);
 			IDList.add(Integer.valueOf(IDNum.toString()));
 		}
 		else
 		{
 			IDList.add(Integer.valueOf(0));
 		}
 	}
 	public void StringtoArray(String ExtraNames, ArrayList<String> NameList)
 	{
 		if(!ExtraNames.isEmpty())
 		{
 			int shift=0;
 			for(int i=0; i < ExtraNames.length(); i++)
 			{
 				if(ExtraNames.charAt(i) == ',')
 				{
 					NameList.add(ExtraNames.substring(shift,i));
 					shift = i+1;
 				}
 			}
 			NameList.add(ExtraNames.substring(shift));
 		}
 		else
 		{
 			NameList.add("");
 		}
 	}
 	public void DungeonLootItemResist()
 	{
 		WeightedRandomChestContent materiaInChest;
 		for(int i =0;i< 8;i++)
 		{
 			materiaInChest = ((EcItemMateria)ItemMat).addMateriaInChest(i,1,1,1);
 			ChestGenHooks.addItem(ChestGenHooks.MINESHAFT_CORRIDOR, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_DESERT_CHEST, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_CHEST, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CORRIDOR, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_LIBRARY, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.STRONGHOLD_CROSSING, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, materiaInChest);
 			ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, materiaInChest);
 		}
 	}
 	public void DebugSystem()
 	{
 
 	}
 	public boolean checkCompatibility()
 	{
 		return Loader.isModLoaded("GraviSuite") || Loader.isModLoaded("ic2ca");
 	}
 }
