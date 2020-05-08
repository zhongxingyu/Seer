 package WWCmod;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid="WWCmod",name="Wonder World Craft Mod",version="v1")
 @NetworkMod(clientSideRequired=true,serverSideRequired=false)
 
 public class WWCMod {
 	//creative tab
 	public static CreativeTabs WWCTab = new TabWWCMod(CreativeTabs.getNextID(), "Wonder World Craft Mod");
 	//toolMaterial
 	public static EnumToolMaterial EnumToolMaterialMithril= EnumHelper.addToolMaterial("LowPower", 2, 250, 6.0F, 2, 15);
 	public static EnumToolMaterial EnumToolMaterialOrichalcum= EnumHelper.addToolMaterial("HighPower", 3, 1561, 20.0F, 3, 15);
 	//blocks
 	public static Block copperore;
 	public static Block tinore;
 	public static Block silverore;
 	public static Block platinumore;
 	public static Block mithrilore;
 	public static Block orichalcumore;
 	//items
 	public static Item copper;
 	public static Item tin;
 	public static Item silver;
 	public static Item platinum;
 	public static Item mithril;
 	public static Item orichalcum;
 	public static Item bronze;
 	public static Item steel;
 	public static Item titanium;
 	//tools
 	public static Item MithrilAxe;
 	public static Item MithrilShovel;
 	public static Item MithrilSword;
 	public static Item MithrilPickaxe;
 	public static Item MithrilHoe;
 	public static Item OrichalcumAxe;
 	public static Item OrichalcumShovel;
 	public static Item OrichalcumSword;
 	public static Item OrichalcumPickaxe;
 	public static Item OrichalcumHoe;
 
@Init
public void load(FMLInitializationEvent event){
 	// world generation
 		GameRegistry.registerWorldGenerator(new WorldGeneratorWWCMod());
 	// ore blocks
 	    copperore= new CopperOre(2501, "copperore").setUnlocalizedName("copperore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(copperore, "copperore");
 		LanguageRegistry.addName(copperore, "Copper Ore");
 
 		tinore= new TinOre(2502, "tinore").setUnlocalizedName("tinore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(tinore, "tinore");
 		LanguageRegistry.addName(tinore, "Tin Ore");
 		
 		silverore= new SilverOre(2503, "silverore").setUnlocalizedName("silverore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(silverore, "silverore");
 		LanguageRegistry.addName(silverore, "Silver Ore");
 		
 		platinumore= new PlatinumOre(2504, "platinumore").setUnlocalizedName("platinumore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(platinumore, "platinumore");
 		LanguageRegistry.addName(platinumore, "Platinum Ore");
 		
 		mithrilore= new MithrilOre(2505, "mithrilore").setUnlocalizedName("mithrilore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(mithrilore, "mithrilore");
 		LanguageRegistry.addName(mithrilore, "Mithril Ore");
 
 		orichalcumore= new OrichalcumOre(2506, "orichalcumore").setUnlocalizedName("orichalcumore").setHardness(2.0F).setStepSound(Block.soundMetalFootstep).setResistance(10.0F);
 		GameRegistry.registerBlock(orichalcumore, "orichalcumore");
 		LanguageRegistry.addName(orichalcumore, "Orichalcum Ore");
 		
 	// ingots items
 		copper = new Ingot(2507).setUnlocalizedName("copper");
 		LanguageRegistry.addName(copper, "Copper Ingot");
 		
 		tin = new Ingot(2508).setUnlocalizedName("tin");
 		LanguageRegistry.addName(tin, "Tin Ingot");
 		
 		silver = new Ingot(2509).setUnlocalizedName("silver");
 		LanguageRegistry.addName(silver, "Silver Ingot");
 		
 		platinum = new Ingot(2510).setUnlocalizedName("platinum");
 		LanguageRegistry.addName(platinum, "Platinum Ingot");
 		
 		mithril = new Ingot(2511).setUnlocalizedName("mithril");
 		LanguageRegistry.addName(mithril, "Mithril Ingot");
 		
 		orichalcum = new Ingot(2512).setUnlocalizedName("orichalcum");
 		LanguageRegistry.addName(orichalcum, "Orichalcum Ingot");
 		
 		bronze = new Ingot(2513).setUnlocalizedName("bronze");
 		LanguageRegistry.addName(bronze, "Bronze Ingot");
 		
 		steel = new Ingot(2514).setUnlocalizedName("steel");
 		LanguageRegistry.addName(steel, "Steel Ingot");
 		
 		titanium = new Ingot(2515).setUnlocalizedName("titanium");
 		LanguageRegistry.addName(titanium, "Titanium Ingot");
 		
 	//tools
 		MithrilAxe = new WWCModAxe(2516, EnumToolMaterialMithril).setUnlocalizedName("mithrilaxe");
 		MithrilShovel = new WWCModShovel(2517, EnumToolMaterialMithril).setUnlocalizedName("mithrilshovel");
 		MithrilPickaxe = new WWCModPickaxe(2518, EnumToolMaterialMithril).setUnlocalizedName("mithrilpick");
 		MithrilHoe = new WWCModHoe(2519, EnumToolMaterialMithril).setUnlocalizedName("mithrilhoe");
 		MithrilSword = new WWCModSword(2520, EnumToolMaterialMithril).setUnlocalizedName("mithrilsword");
 		LanguageRegistry.addName(MithrilAxe, "Mithril Axe");
 		LanguageRegistry.addName(MithrilShovel, "Mithril Spade");
 		LanguageRegistry.addName(MithrilPickaxe, "Mithril Pickaxe");
 		LanguageRegistry.addName(MithrilSword, "Mithril Sword");
 		LanguageRegistry.addName(MithrilHoe, "Mithril Hoe");
 		
 		OrichalcumAxe = new WWCModAxe(2521, EnumToolMaterialOrichalcum).setUnlocalizedName("orichalcumaxe");
 		OrichalcumShovel = new WWCModShovel(2522, EnumToolMaterialOrichalcum).setUnlocalizedName("orichalcumshovel");
 		OrichalcumPickaxe = new WWCModPickaxe(2523, EnumToolMaterialOrichalcum).setUnlocalizedName("orichalcumpick");
 		OrichalcumHoe = new WWCModHoe(2524, EnumToolMaterialOrichalcum).setUnlocalizedName("orichalcumhoe");
 		OrichalcumSword = new WWCModSword(2525, EnumToolMaterialOrichalcum).setUnlocalizedName("orichalcumsword");
 		LanguageRegistry.addName(OrichalcumAxe, "Orichalcum Axe");
 		LanguageRegistry.addName(OrichalcumShovel, "Orichalcum Spade");
 		LanguageRegistry.addName(OrichalcumPickaxe, "Orichalcum Pickaxe");
 		LanguageRegistry.addName(OrichalcumSword, "Orichalcum Sword");
 		LanguageRegistry.addName(OrichalcumHoe, "Orichalcum Hoe");
 		
 	//crafting
 		GameRegistry.addRecipe(new ItemStack(MithrilAxe,1), new Object[]{
 			"MM ","MS "," S ",'M',mithril, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(MithrilShovel,1), new Object[]{
 			" M "," S "," S ",'M',mithril, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(MithrilPickaxe,1), new Object[]{
 			"MMM"," S "," S ",'M',mithril, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(MithrilHoe,1), new Object[]{
 			"MM "," S "," S ",'M',mithril, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(MithrilSword,1), new Object[]{
 			" M "," M "," S ",'M',mithril, 'S',Item.stick,});
 		
 		GameRegistry.addRecipe(new ItemStack(OrichalcumAxe,1), new Object[]{
 			"OO ","OS "," S ",'O',orichalcum, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(OrichalcumShovel,1), new Object[]{
 			" O "," S "," S ",'O',orichalcum, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(OrichalcumPickaxe,1), new Object[]{
 			"OOO"," S "," S ",'O',orichalcum, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(OrichalcumHoe,1), new Object[]{
 			"OO "," S "," S ",'O',orichalcum, 'S',Item.stick,});
 		GameRegistry.addRecipe(new ItemStack(OrichalcumSword,1), new Object[]{
 			" O "," O "," S ",'O',orichalcum, 'S',Item.stick,});
 	//smelting
 		GameRegistry.addSmelting(copperore.blockID, new ItemStack(copper,1), 2.0F);
 		GameRegistry.addSmelting(tinore.blockID, new ItemStack(tin,1), 2.0F);
 		GameRegistry.addSmelting(silverore.blockID, new ItemStack(silver,1), 2.0F);
 		GameRegistry.addSmelting(platinumore.blockID, new ItemStack(platinum,1), 2.0F);
 		GameRegistry.addSmelting(mithrilore.blockID, new ItemStack(mithril,1), 2.0F);
 		GameRegistry.addSmelting(orichalcumore.blockID, new ItemStack(orichalcum,1), 2.0F);
 }	
 
 }
 
 
