 package Nanashi.AdvancedTools;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid="AdvancedTools", name="AdvancedTools", version="2.0p-Unofficial")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels="AT|Tool", packetHandler=PacketHandler.class)
 
 public class AdvancedTools
 {
 	public static int ItemID_INDEX = 3000;
 	public static int UGTools_DestroyRangeLV;
 	public static int UGTools_SaftyCounter;
 	public static boolean spawnHiGradeMob;
 
 	public static Item RedEnhancer;
 	public static Item BlueEnhancer;
 	public static Item UGWoodShovel;
 	public static Item UGStoneShovel;
 	public static Item UGIronShovel;
 	public static Item UGDiamondShovel;
 	public static Item UGGoldShovel;
 	public static Item UGWoodPickaxe;
 	public static Item UGStonePickaxe;
 	public static Item UGIronPickaxe;
 	public static Item UGDiamondPickaxe;
 	public static Item UGGoldPickaxe;
 	public static Item UGWoodAxe;
 	public static Item UGStoneAxe;
 	public static Item UGIronAxe;
 	public static Item UGDiamondAxe;
 	public static Item UGGoldAxe;
 	public static Item BlazeBlade;
 	public static Item IceHold;
 	public static Item AsmoSlasher;
 	public static Item PlanetGuardian;
 	public static Item StormBringer;
 	public static Item NEGI;
 	public static Item LuckLuck;
 	public static Item SmashBat;
 	public static Item DevilSword;
 	public static Item HolySaber;
 	public static Item ThrowingKnife;
 	public static Item PoisonKnife;
 	public static Item CrossBow;
 	public static Item InfiniteSword;
 	public static Item InfinitePickaxe;
 	public static Item InfiniteAxe;
 	public static Item InfiniteShovel;
 	public static Item InfiniteHoe;
 	public static Item GenocideBlade;
 //	public static Item ExtraSpawner;
 
 //	public static String textureDomain = "Nanashi/AdvancedTools:";
 	public static String itemTexture = "/Nanashi/AdvancedTools/textures/items.png";
 	public static String mobTexture = "/Nanashi/AdvancedTools/textures/mob/";
 
 	@Instance("AdvancedTools")
 	public static AdvancedTools instance;
 	@SidedProxy(clientSide = "Nanashi.AdvancedTools.client.ClientProxy", serverSide = "Nanashi.AdvancedTools.CommonProxy")
 	public static CommonProxy proxy;
 	public static final CreativeTabs tabsAT = new CreativeTabAT("AdvancedTools");
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		ItemID_INDEX = config.get(Configuration.CATEGORY_ITEM, "Item ID", 3000).getInt();
 		UGTools_DestroyRangeLV = config.get(Configuration.CATEGORY_GENERAL, "Destroy Range Lv", 1).getInt();
 		UGTools_SaftyCounter = config.get(Configuration.CATEGORY_GENERAL, "Safty Counter", 100).getInt();
 		spawnHiGradeMob = config.get(Configuration.CATEGORY_GENERAL, "Spawn Hi-Grade Mobs", true).getBoolean(true);
 		config.save();
 	}
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		this.itemSetup();
 		this.addRecipe();
 		this.setItemIcon();
 		this.entitySetup();
		this.setName();

 		proxy.registerRenderInformation();
 	}
 
 	public void entitySetup()
 	{
 //		EntityRegistry.registerGlobalEntityID(Entity_HighSkeleton.class, "HighSkeleton", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 //		EntityRegistry.registerGlobalEntityID(Entity_SkeletonSniper.class, "SkeletonSniper", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 //		EntityRegistry.registerGlobalEntityID(Entity_ZombieWarrior.class, "ZombieWarrior", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 //		EntityRegistry.registerGlobalEntityID(Entity_FireZombie.class, "FireZombie", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 //		EntityRegistry.registerGlobalEntityID(Entity_HighSpeedCreeper.class, "HighSpeedCreeper", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 //		EntityRegistry.registerGlobalEntityID(Entity_GoldCreeper.class, "GoldCreeper", EntityRegistry.findGlobalUniqueEntityId(), 0, 0);
 
 		EntityRegistry.registerModEntity(Entity_ThrowingKnife.class, "ThrowingKnife", 0, this, 250, 20, true);
 		EntityRegistry.registerModEntity(Entity_HighSkeleton.class, "HighSkeleton", 1, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_SkeletonSniper.class, "SkeletonSniper", 2, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_ZombieWarrior.class, "ZombieWarrior", 3, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_FireZombie.class, "FireZombie", 4, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_HighSpeedCreeper.class, "HighSpeedCreeper", 5, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_GoldCreeper.class, "GoldCreeper", 6, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_BBFireBall.class, "BBFireBall", 7, this, 250, 5, true);
 		EntityRegistry.registerModEntity(Entity_IHFrozenMob.class, "IHFrozenMob", 8, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_PGPowerBomb.class, "PGPowerBomb", 9, this, 250, 1, true);
 		EntityRegistry.registerModEntity(Entity_SBWindEdge.class, "SBWindEdge", 10, this, 250, 1, true);
 		if (spawnHiGradeMob)
 		{
 			for(int i = 0; i <BiomeGenBase.biomeList.length;i++)
 			{
				if(BiomeGenBase.biomeList[i] != null
						&& BiomeGenBase.biomeList[i] != BiomeGenBase.hell
						&& BiomeGenBase.biomeList[i] != BiomeGenBase.mushroomIsland
						&& BiomeGenBase.biomeList[i] != BiomeGenBase.mushroomIslandShore
						&& BiomeGenBase.biomeList[i] != BiomeGenBase.sky
						&& BiomeGenBase.biomeList[i].getSpawnableList(EnumCreatureType.monster).size() >= 5)
 				{
 					EntityRegistry.addSpawn(Entity_HighSkeleton.class, 2, 1, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 					EntityRegistry.addSpawn(Entity_SkeletonSniper.class, 3, 1, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 					EntityRegistry.addSpawn(Entity_ZombieWarrior.class, 2, 1, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 					EntityRegistry.addSpawn(Entity_FireZombie.class, 3, 1, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 					EntityRegistry.addSpawn(Entity_HighSpeedCreeper.class, 3, 4, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 					EntityRegistry.addSpawn(Entity_GoldCreeper.class, 1, 1, 4, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 
 //					EntityRegistry.removeSpawn(EntitySkeleton.class, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 //					EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 //					EntityRegistry.removeSpawn(EntitySpider.class, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 //					EntityRegistry.removeSpawn(EntityCreeper.class, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 //					EntityRegistry.removeSpawn(EntityEnderman.class, EnumCreatureType.monster, BiomeGenBase.biomeList[i]);
 				}
 			}
 
 		}
 
 	}
 
 	public void itemSetup()
 	{
 		RedEnhancer = (new ItemEnhancer(ItemID_INDEX + 0)).setItemName("RedEnhancer").setCreativeTab(tabsAT);
 		BlueEnhancer = (new ItemEnhancer(ItemID_INDEX + 1)).setItemName("BlueEnhancer").setCreativeTab(tabsAT);
 		UGWoodShovel = (new ItemUGShovel(ItemID_INDEX + 2, EnumToolMaterial.WOOD)).setItemName("UpgradedWoodenShovel").setCreativeTab(tabsAT);
 		UGStoneShovel = (new ItemUGShovel(ItemID_INDEX + 3, EnumToolMaterial.STONE, 1.5F)).setItemName("UpgradedStoneShovel").setCreativeTab(tabsAT);
 		UGIronShovel = (new ItemUGShovel(ItemID_INDEX + 4, EnumToolMaterial.IRON, 2.0F)).setItemName("UpgradedIronShovel").setCreativeTab(tabsAT);
 		UGDiamondShovel = (new ItemUGShovel(ItemID_INDEX + 5, EnumToolMaterial.EMERALD, 3.0F)).setItemName("UpgradedDiamondShovel").setCreativeTab(tabsAT);
 		UGGoldShovel = (new ItemUGShovel(ItemID_INDEX + 6, EnumToolMaterial.GOLD, 2.5F)).setItemName("UpgradedGoldenShovel").setCreativeTab(tabsAT);
 		UGWoodPickaxe = (new ItemUGPickaxe(ItemID_INDEX + 7, EnumToolMaterial.WOOD)).setItemName("UpgradedWoodenPickaxe").setCreativeTab(tabsAT);
 		UGStonePickaxe = (new ItemUGPickaxe(ItemID_INDEX + 8, EnumToolMaterial.STONE, 1.5F)).setItemName("UpgradedStonePickaxe").setCreativeTab(tabsAT);
 		UGIronPickaxe = (new ItemUGPickaxe(ItemID_INDEX + 9, EnumToolMaterial.IRON, 2.0F)).setItemName("UpgradedIronPickaxe").setCreativeTab(tabsAT);
 		UGDiamondPickaxe = (new ItemUGPickaxe(ItemID_INDEX + 10, EnumToolMaterial.EMERALD, 3.0F)).setItemName("UpgradedDiamondPickaxe").setCreativeTab(tabsAT);
 		UGGoldPickaxe = (new ItemUGPickaxe(ItemID_INDEX + 11, EnumToolMaterial.GOLD, 2.5F)).setItemName("UpgradedGoldenPickaxe").setCreativeTab(tabsAT);
 		UGWoodAxe = (new ItemUGAxe(ItemID_INDEX + 12, EnumToolMaterial.WOOD)).setItemName("UpgradedWoodenAxe").setCreativeTab(tabsAT);
 		UGStoneAxe = (new ItemUGAxe(ItemID_INDEX + 13, EnumToolMaterial.STONE, 1.5F)).setItemName("UpgradedStoneAxe").setCreativeTab(tabsAT);
 		UGIronAxe = (new ItemUGAxe(ItemID_INDEX + 14, EnumToolMaterial.IRON, 2.0F)).setItemName("UpgradedIronAxe").setCreativeTab(tabsAT);
 		UGDiamondAxe = (new ItemUGAxe(ItemID_INDEX + 15, EnumToolMaterial.EMERALD, 3.0F)).setItemName("UpgradedDiamondAxe").setCreativeTab(tabsAT);
 		UGGoldAxe = (new ItemUGAxe(ItemID_INDEX + 16, EnumToolMaterial.GOLD, 2.5F)).setItemName("UpgradedGoldenAxe").setCreativeTab(tabsAT);
 		BlazeBlade = (new ItemUQBlazeBlade(ItemID_INDEX + 17, EnumToolMaterial.EMERALD, 4)).setItemName("BlazeBlade").setMaxDamage(1200).setCreativeTab(tabsAT);
 		IceHold = (new ItemUQIceHold(ItemID_INDEX + 18, EnumToolMaterial.EMERALD, 4)).setItemName("FreezeHold").setMaxDamage(1200).setCreativeTab(tabsAT);
 		AsmoSlasher = (new ItemUQAsmoSlasher(ItemID_INDEX + 19, EnumToolMaterial.EMERALD, 4)).setItemName("AsmoSlasher").setMaxDamage(1200).setCreativeTab(tabsAT);
 		PlanetGuardian = (new ItemUQPlanetGuardian(ItemID_INDEX + 20, EnumToolMaterial.EMERALD, 4)).setItemName("Planet Guardian").setMaxDamage(1200).setCreativeTab(tabsAT);
 		StormBringer = (new ItemUQStormBringer(ItemID_INDEX + 21, EnumToolMaterial.EMERALD, 4)).setItemName("StormBringer").setMaxDamage(1200).setCreativeTab(tabsAT);
 		NEGI = (new Item_Negi(ItemID_INDEX + 22, 1, 0.1F, false)).setItemName("NEGI").setCreativeTab(tabsAT);
 		LuckLuck = (new ItemUQLuckies(ItemID_INDEX + 23, EnumToolMaterial.GOLD, 2)).setItemName("Lucky&Lucky").setMaxDamage(77).setCreativeTab(tabsAT);
 		SmashBat = (new ItemUniqueArms(ItemID_INDEX + 24, EnumToolMaterial.WOOD, 1)).setItemName("SmashBat").setMaxDamage(95).setCreativeTab(tabsAT);
 		DevilSword = (new ItemUQDevilSword(ItemID_INDEX + 25, EnumToolMaterial.EMERALD, 1)).setItemName("DevilSword").setMaxDamage(427).setCreativeTab(tabsAT);
 		HolySaber = (new ItemUQHolySaber(ItemID_INDEX + 26, EnumToolMaterial.GOLD, 5)).setItemName("HolySaber").setMaxDamage(280).setCreativeTab(tabsAT);
 		ThrowingKnife = (new Item_ThrowingKnife(ItemID_INDEX + 27, false)).setItemName("ThrowingKnife").setCreativeTab(tabsAT);
 		PoisonKnife = (new Item_ThrowingKnife(ItemID_INDEX + 28, true)).setItemName("PoisonKnife").setCreativeTab(tabsAT);
 		CrossBow = (new Item_CrossBow(ItemID_INDEX + 29)).setItemName("A_CrossBow").setCreativeTab(tabsAT);
 		InfiniteSword = (new ItemUniqueArms(ItemID_INDEX + 30, EnumToolMaterial.GOLD, 8)).setItemName("InfiniteSword").setMaxDamage(0).setCreativeTab(tabsAT);
 		InfinitePickaxe = (new ItemUGPickaxe(ItemID_INDEX + 31, EnumToolMaterial.EMERALD)).setItemName("InfinityPickaxe").setMaxDamage(0).setCreativeTab(tabsAT);
 		InfiniteAxe = (new ItemUGAxe(ItemID_INDEX + 32, EnumToolMaterial.GOLD)).setItemName("InfinityAxe").setMaxDamage(0).setCreativeTab(tabsAT);
 		InfiniteShovel = (new ItemUGShovel(ItemID_INDEX + 33, EnumToolMaterial.GOLD)).setItemName("InfinityShovel").setMaxDamage(0).setCreativeTab(tabsAT);
 		InfiniteHoe = (new ItemInfHoe(ItemID_INDEX + 34, EnumToolMaterial.GOLD)).setItemName("InfinityHoe").setMaxDamage(0).setCreativeTab(tabsAT);
 		GenocideBlade = (new ItemUniqueArms(ItemID_INDEX + 36, EnumToolMaterial.EMERALD, 10000)).setItemName("GenocideBlade").setMaxDamage(0).setCreativeTab(tabsAT);
 //		ExtraSpawner = (new ItemExtraMobSpawner(ItemID_INDEX + 37)).setItemName("Extra Spawner").setCreativeTab(tabsAT);
 	}
 
 	public void addRecipe()
 	{
 		Item[][] var1 = new Item[][] {{UGWoodShovel, UGStoneShovel, UGIronShovel, UGDiamondShovel, UGGoldShovel, InfiniteShovel}, {UGWoodPickaxe, UGStonePickaxe, UGIronPickaxe, UGDiamondPickaxe, UGGoldPickaxe, InfinitePickaxe}, {UGWoodAxe, UGStoneAxe, UGIronAxe, UGDiamondAxe, UGGoldAxe, InfiniteAxe}};
 		Item[] var2 = new Item[] {Item.stick, RedEnhancer, RedEnhancer, BlueEnhancer, BlueEnhancer, Item.eyeOfEnder};
 		Object[] var3 = new Object[] {Block.wood, Block.cobblestone, Item.ingotIron, Item.diamond, Item.ingotGold, Block.whiteStone};
 		String[][] var4 = new String[][] {{"X", "#", "Z"}, {"XXX", " # ", " Z "}, {"XX", "X#", " Z"}};
 		GameRegistry.addRecipe(new ItemStack(InfiniteSword, 1), new Object[] {" #X", "XY#", "ZX ", 'X', Block.whiteStone, 'Y', Item.eyeOfEnder, 'Z', Item.stick, '#', Block.glowStone});
 		GameRegistry.addRecipe(new ItemStack(InfiniteHoe, 1), new Object[] {"XX", " Y", " Z", 'X', Block.whiteStone, 'Y', Item.eyeOfEnder, 'Z', Item.stick});
 		GameRegistry.addRecipe(new ItemStack(RedEnhancer, 2), new Object[] {" X ", "XZX", " X ", 'X', Item.goldNugget, 'Z', Item.redstone});
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.redstone), new Object[] {RedEnhancer, RedEnhancer});
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.ingotGold), new Object[] {RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer, RedEnhancer});
 		GameRegistry.addRecipe(new ItemStack(BlueEnhancer, 2), new Object[] {"RXR", "XZX", "RXR", 'X', Item.ingotGold, 'Z', Item.diamond, 'R', Item.redstone});
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.ingotGold), new Object[] {BlueEnhancer, BlueEnhancer});
 		GameRegistry.addShapelessRecipe(new ItemStack(Item.diamond), new Object[] {BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer, BlueEnhancer});
 
 		for (int var5 = 0; var5 < var1[0].length; ++var5)
 		{
 			for (int var6 = 0; var6 < var1.length; ++var6)
 			{
 				GameRegistry.addRecipe(new ItemStack(var1[var6][var5]), new Object[] {var4[var6], 'X', var3[var5], '#', var2[var5], 'Z', Item.stick});
 			}
 		}
 
 		GameRegistry.addRecipe(new ItemStack(BlazeBlade), new Object[] {" P#", "XEP", "IX ", 'I', Item.blazeRod, 'X', Item.diamond, '#', Item.ingotIron, 'E', BlueEnhancer, 'P', Item.blazePowder});
 		GameRegistry.addRecipe(new ItemStack(IceHold), new Object[] {" P#", "XEP", "IX ", 'I', Item.stick, 'X', Item.ingotIron, '#', Item.bucketWater, 'E', BlueEnhancer, 'P', Block.blockSnow});
 		GameRegistry.addRecipe(new ItemStack(AsmoSlasher), new Object[] {" P#", "XEP", "IX ", 'I', Item.stick, 'X', Item.ingotGold, '#', Item.ingotIron, 'E', BlueEnhancer, 'P', Item.redstone});
 		GameRegistry.addRecipe(new ItemStack(PlanetGuardian), new Object[] {" ##", "#E#", "I# ", 'I', Item.stick, '#', Item.ingotIron, 'E', BlueEnhancer});
 		GameRegistry.addRecipe(new ItemStack(StormBringer), new Object[] {"  #", "XE ", "IX ", 'I', Item.stick, 'X', Item.feather, '#', Item.ingotIron, 'E', BlueEnhancer});
 		GameRegistry.addRecipe(new ItemStack(LuckLuck), new Object[] {"  #", "#E ", "I# ", 'I', Item.stick, '#', Item.ingotGold, 'E', RedEnhancer});
 		GameRegistry.addRecipe(new ItemStack(SmashBat), new Object[] {"#", "#", "#", '#', Block.wood});
 		GameRegistry.addShapelessRecipe(new ItemStack(NEGI), new Object[] {Item.bucketWater, Item.seeds, Block.dirt});
 		GameRegistry.addRecipe(new ItemStack(HolySaber), new Object[] {"XBX", "EAC", "XDX", 'A', BlazeBlade, 'B', IceHold, 'C', AsmoSlasher, 'D', PlanetGuardian, 'E', StormBringer, 'X', Block.glowStone});
 		GameRegistry.addRecipe(new ItemStack(ThrowingKnife, 16), new Object[] {" X", "# ", 'X', Item.ingotIron, '#', Item.stick});
 		GameRegistry.addShapelessRecipe(new ItemStack(PoisonKnife), new Object[] {ThrowingKnife, Item.spiderEye});
 	}
 
 	public void setItemIcon()
 	{
 		RedEnhancer.setIconIndex(0);
 		BlueEnhancer.setIconIndex(1);
 		UGWoodPickaxe.setIconIndex(2);
 		UGWoodShovel.setIconIndex(3);
 		UGWoodAxe.setIconIndex(4);
 		UGStonePickaxe.setIconIndex(5);
 		UGStoneShovel.setIconIndex(6);
 		UGStoneAxe.setIconIndex(7);
 		UGIronPickaxe.setIconIndex(8);
 		UGIronShovel.setIconIndex(9);
 		UGIronAxe.setIconIndex(10);
 		UGDiamondPickaxe.setIconIndex(11);
 		UGDiamondShovel.setIconIndex(12);
 		UGDiamondAxe.setIconIndex(13);
 		UGGoldPickaxe.setIconIndex(14);
 		UGGoldShovel.setIconIndex(15);
 		UGGoldAxe.setIconIndex(16);
 		BlazeBlade.setIconIndex(17);
 		IceHold.setIconIndex(18);
 		AsmoSlasher.setIconIndex(19);
 		PlanetGuardian.setIconIndex(20);
 		StormBringer.setIconIndex(21);
 		NEGI.setIconIndex(22);
 		LuckLuck.setIconIndex(23);
 		SmashBat.setIconIndex(24);
 		HolySaber.setIconIndex(25);
 		DevilSword.setIconIndex(26);
 		InfiniteSword.setIconIndex(27);
 		InfinitePickaxe.setIconIndex(28);
 		InfiniteAxe.setIconIndex(29);
 		InfiniteShovel.setIconIndex(30);
 		InfiniteHoe.setIconIndex(31);
 		CrossBow.setIconIndex(32);
 		ThrowingKnife.setIconIndex(33);
 		PoisonKnife.setIconIndex(34);
 		GenocideBlade.setIconIndex(26);
 	}
 
 	public void setName()
 	{
 		LanguageRegistry.addName(RedEnhancer, "RedEnhancer");
 		LanguageRegistry.addName(BlueEnhancer, "BlueEnhancer");
 		LanguageRegistry.addName(InfiniteSword, "Infinite Possibility");
 		LanguageRegistry.addName(InfinitePickaxe, "Infinity Pickaxe");
 		LanguageRegistry.addName(InfiniteAxe, "Infinity Axe");
 		LanguageRegistry.addName(InfiniteShovel, "Infinity Shovel");
 		LanguageRegistry.addName(InfiniteHoe, "Infinity Hoe");
 		Item[][] var1 = new Item[][] {{UGWoodShovel, UGStoneShovel, UGIronShovel, UGDiamondShovel, UGGoldShovel, InfiniteShovel}, {UGWoodPickaxe, UGStonePickaxe, UGIronPickaxe, UGDiamondPickaxe, UGGoldPickaxe, InfinitePickaxe}, {UGWoodAxe, UGStoneAxe, UGIronAxe, UGDiamondAxe, UGGoldAxe, InfiniteAxe}};
 
 		for (int var2 = 0; var2 < var1.length; ++var2)
 		{
 			for (int var3 = 0; var3 < var1[0].length; ++var3)
 			{
 				String var4 = "item." + ((ItemUGTool)var1[var2][var3]).BaseName + ".name";
 				LanguageRegistry.instance().addStringLocalization(var4, ((ItemUGTool)var1[var2][var3]).BaseName);
 			}
 		}
 
 		LanguageRegistry.addName(BlazeBlade, "Blaze Blade");
 		LanguageRegistry.addName(IceHold, "Freeze Hold");
 		LanguageRegistry.addName(AsmoSlasher, "Asmo Slasher");
 		LanguageRegistry.addName(PlanetGuardian, "Planet Guardian");
 		LanguageRegistry.addName(StormBringer, "Storm Bringer");
 		LanguageRegistry.addName(NEGI, "N E G I");
 		LanguageRegistry.addName(LuckLuck, "Lucky&Lucky");
 		LanguageRegistry.addName(SmashBat, "Smash Bat");
 		LanguageRegistry.addName(HolySaber, "Holy Saber");
 		LanguageRegistry.addName(CrossBow, "Automatic Crossbow");
 		LanguageRegistry.addName(ThrowingKnife, "Throwing Knife");
 		LanguageRegistry.addName(PoisonKnife, "Poison Knife");
 		LanguageRegistry.addName(DevilSword, "Devil Sword");
 		LanguageRegistry.addName(GenocideBlade, "Genocide Blade");
 //		LanguageRegistry.addName(ExtraSpawner, "Extra Mob Spawner");
		
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.HighSkeleton.name", "HighSkeleton");
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.SkeletonSniper.name", "SkeletonSniper");
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.ZombieWarrior.name", "ZombieWarrior");
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.FireZombie.name", "FireZombie");
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.HighSpeedCreeper.name", "HighSpeedCreeper");
		LanguageRegistry.instance().addStringLocalization("entity.AdvancedTools.GoldCreeper.name", "GoldCreeper");
		
 	}
 	public static MovingObjectPosition setMousePoint(World world, EntityPlayer entityplayer)
     {
     	float var1=1F;
     	double Dislimit = 150.0D;
     	double viewX = entityplayer.getLookVec().xCoord;
 		double viewY = entityplayer.getLookVec().yCoord;
 		double viewZ = entityplayer.getLookVec().zCoord;
     	double PlayerposX = entityplayer.prevPosX + (entityplayer.posX - entityplayer.prevPosX) * (double)var1;
         double PlayerposY = entityplayer.prevPosY + (entityplayer.posY - entityplayer.prevPosY) * (double)var1 + 1.62D - (double)entityplayer.yOffset;
         double PlayerposZ = entityplayer.prevPosZ + (entityplayer.posZ - entityplayer.prevPosZ) * (double)var1;
         Vec3 PlayerPosition = Vec3.createVectorHelper(PlayerposX, PlayerposY, PlayerposZ);
         Vec3 PlayerLookVec = PlayerPosition.addVector(viewX*Dislimit, viewY*Dislimit, viewZ*Dislimit);
         MovingObjectPosition MOP = world.rayTraceBlocks_do(PlayerPosition, PlayerLookVec, true);
         return MOP;
     }
 }
