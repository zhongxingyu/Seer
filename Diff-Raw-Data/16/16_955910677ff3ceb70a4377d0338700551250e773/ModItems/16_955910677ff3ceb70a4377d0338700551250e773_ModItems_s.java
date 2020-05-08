 package silentAbyss.item;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import silentAbyss.Abyss;
 import silentAbyss.lib.ItemIds;
 import silentAbyss.lib.Reference;
 import silentAbyss.lib.Strings;
 import silentAbyss.recipe.SigilScepterRecipe;
 import silentAbyss.tool.*;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.oredict.OreDictionary;
 
 public class ModItems {
 
 	public static Item abyssGem;
 	public static Item abyssShard;
 	public static Item potatoStick;
 	public static Item craftingMaterial;
 	public static Item teleporterLinker;
 	public static Item abyssSigil;
 	public static Item sigilStone;
 	public static Item sigilScepter;
 	
 	public static Item swordAbyssRuby;
 	public static Item swordAbyssEmerald;
 	public static Item swordAbyssSapphire;
 	public static Item swordAbyssTopaz;
 	public static Item pickaxeAbyssRuby;
 	public static Item pickaxeAbyssEmerald;
 	public static Item pickaxeAbyssSapphire;
 	public static Item pickaxeAbyssTopaz;
 	public static Item shovelAbyssRuby;
 	public static Item shovelAbyssEmerald;
 	public static Item shovelAbyssSapphire;
 	public static Item shovelAbyssTopaz;
 	public static Item axeAbyssRuby;
 	public static Item axeAbyssEmerald;
 	public static Item axeAbyssSapphire;
 	public static Item axeAbyssTopaz;
 	public static Item hoeAbyssRuby;
 	public static Item hoeAbyssEmerald;
 	public static Item hoeAbyssSapphire;
 	public static Item hoeAbyssTopaz;
 	
 	public static Item swordAbyssRubyPlus;
 	public static Item swordAbyssEmeraldPlus;
 	public static Item swordAbyssSapphirePlus;
 	public static Item swordAbyssTopazPlus;
 	public static Item pickaxeAbyssRubyPlus;
 	public static Item pickaxeAbyssEmeraldPlus;
 	public static Item pickaxeAbyssSapphirePlus;
 	public static Item pickaxeAbyssTopazPlus;
 	public static Item shovelAbyssRubyPlus;
 	public static Item shovelAbyssEmeraldPlus;
 	public static Item shovelAbyssSapphirePlus;
 	public static Item shovelAbyssTopazPlus;
 	public static Item axeAbyssRubyPlus;
 	public static Item axeAbyssEmeraldPlus;
 	public static Item axeAbyssSapphirePlus;
 	public static Item axeAbyssTopazPlus;
 	public static Item hoeAbyssRubyPlus;
 	public static Item hoeAbyssEmeraldPlus;
 	public static Item hoeAbyssSapphirePlus;
 	public static Item hoeAbyssTopazPlus;
 	
 	public static void init() {
 		
 		/*
 		 * Initialize item variables
 		 */
 		abyssGem = new AbyssGem(ItemIds.ABYSS_GEM);
 		abyssShard = new GemShard(ItemIds.ABYSS_SHARD);
 		potatoStick = new AbyssFood(ItemIds.POTATO_STICK, 8, 0.8f, false, 0);
 		craftingMaterial = new CraftingMaterial(ItemIds.CRAFTING_MATERIAL);
 		teleporterLinker = new TeleporterLinker(ItemIds.TELEPORTER_LINKER);
 		abyssSigil = new AbyssSigil(ItemIds.ABYSS_SIGIL);
 		sigilStone = new SigilStone(ItemIds.SIGIL_STONE);
 		sigilScepter = new SigilScepter(ItemIds.SIGIL_SCEPTER);
 		
 		swordAbyssRuby = new AbyssSword(ItemIds.TOOL_START + 0, Abyss.materialRegularAbyssGem, 0);
 		pickaxeAbyssRuby = new AbyssPickaxe(ItemIds.TOOL_START + 1, Abyss.materialRegularAbyssGem, 0);
 		shovelAbyssRuby = new AbyssShovel(ItemIds.TOOL_START + 2, Abyss.materialRegularAbyssGem, 0);
 		axeAbyssRuby = new AbyssAxe(ItemIds.TOOL_START + 3, Abyss.materialRegularAbyssGem, 0);
 		hoeAbyssRuby = new AbyssHoe(ItemIds.TOOL_START + 4, Abyss.materialRegularAbyssGem, 0);
 		swordAbyssEmerald = new AbyssSword(ItemIds.TOOL_START + 5, Abyss.materialRegularAbyssGem, 1);
 		pickaxeAbyssEmerald = new AbyssPickaxe(ItemIds.TOOL_START + 6, Abyss.materialRegularAbyssGem, 1);
 		shovelAbyssEmerald = new AbyssShovel(ItemIds.TOOL_START + 7, Abyss.materialRegularAbyssGem, 1);
 		axeAbyssEmerald = new AbyssAxe(ItemIds.TOOL_START + 8, Abyss.materialRegularAbyssGem, 1);
 		hoeAbyssEmerald = new AbyssHoe(ItemIds.TOOL_START + 9, Abyss.materialRegularAbyssGem, 1);
 		swordAbyssSapphire = new AbyssSword(ItemIds.TOOL_START + 10, Abyss.materialRegularAbyssGem, 2);
 		pickaxeAbyssSapphire = new AbyssPickaxe(ItemIds.TOOL_START + 11, Abyss.materialRegularAbyssGem, 2);
 		shovelAbyssSapphire = new AbyssShovel(ItemIds.TOOL_START + 12, Abyss.materialRegularAbyssGem, 2);
 		axeAbyssSapphire = new AbyssAxe(ItemIds.TOOL_START + 13, Abyss.materialRegularAbyssGem, 2);
 		hoeAbyssSapphire = new AbyssHoe(ItemIds.TOOL_START + 14, Abyss.materialRegularAbyssGem, 2);
 		swordAbyssTopaz = new AbyssSword(ItemIds.TOOL_START + 15, Abyss.materialRegularAbyssGem, 3);
 		pickaxeAbyssTopaz = new AbyssPickaxe(ItemIds.TOOL_START + 16, Abyss.materialRegularAbyssGem, 3);
 		shovelAbyssTopaz = new AbyssShovel(ItemIds.TOOL_START + 17, Abyss.materialRegularAbyssGem, 3);
 		axeAbyssTopaz = new AbyssAxe(ItemIds.TOOL_START + 18, Abyss.materialRegularAbyssGem, 3);
 		hoeAbyssTopaz = new AbyssHoe(ItemIds.TOOL_START + 19, Abyss.materialRegularAbyssGem, 3);
 		swordAbyssRubyPlus = new AbyssSword(ItemIds.TOOL_START + 20, Abyss.materialEnergizedAbyssGem, 0);
 		pickaxeAbyssRubyPlus = new AbyssPickaxe(ItemIds.TOOL_START + 21, Abyss.materialEnergizedAbyssGem, 0);
 		shovelAbyssRubyPlus = new AbyssShovel(ItemIds.TOOL_START + 22, Abyss.materialEnergizedAbyssGem, 0);
 		axeAbyssRubyPlus = new AbyssAxe(ItemIds.TOOL_START + 23, Abyss.materialEnergizedAbyssGem, 0);
 		hoeAbyssRubyPlus = new AbyssHoe(ItemIds.TOOL_START + 24, Abyss.materialEnergizedAbyssGem, 0);
 		swordAbyssEmeraldPlus = new AbyssSword(ItemIds.TOOL_START + 25, Abyss.materialEnergizedAbyssGem, 1);
 		pickaxeAbyssEmeraldPlus = new AbyssPickaxe(ItemIds.TOOL_START + 26, Abyss.materialEnergizedAbyssGem, 1);
 		shovelAbyssEmeraldPlus = new AbyssShovel(ItemIds.TOOL_START + 27, Abyss.materialEnergizedAbyssGem, 1);
 		axeAbyssEmeraldPlus = new AbyssAxe(ItemIds.TOOL_START + 28, Abyss.materialEnergizedAbyssGem, 1);
 		hoeAbyssEmeraldPlus = new AbyssHoe(ItemIds.TOOL_START + 29, Abyss.materialEnergizedAbyssGem, 1);
 		swordAbyssSapphirePlus = new AbyssSword(ItemIds.TOOL_START + 30, Abyss.materialEnergizedAbyssGem, 2);
 		pickaxeAbyssSapphirePlus = new AbyssPickaxe(ItemIds.TOOL_START + 31, Abyss.materialEnergizedAbyssGem, 2);
 		shovelAbyssSapphirePlus = new AbyssShovel(ItemIds.TOOL_START + 32, Abyss.materialEnergizedAbyssGem, 2);
 		axeAbyssSapphirePlus = new AbyssAxe(ItemIds.TOOL_START + 33, Abyss.materialEnergizedAbyssGem, 2);
 		hoeAbyssSapphirePlus = new AbyssHoe(ItemIds.TOOL_START + 34, Abyss.materialEnergizedAbyssGem, 2);
 		swordAbyssTopazPlus = new AbyssSword(ItemIds.TOOL_START + 35, Abyss.materialEnergizedAbyssGem, 3);
 		pickaxeAbyssTopazPlus = new AbyssPickaxe(ItemIds.TOOL_START + 36, Abyss.materialEnergizedAbyssGem, 3);
 		shovelAbyssTopazPlus = new AbyssShovel(ItemIds.TOOL_START + 37, Abyss.materialEnergizedAbyssGem, 3);
 		axeAbyssTopazPlus = new AbyssAxe(ItemIds.TOOL_START + 38, Abyss.materialEnergizedAbyssGem, 3);
 		hoeAbyssTopazPlus = new AbyssHoe(ItemIds.TOOL_START + 39, Abyss.materialEnergizedAbyssGem, 3);
 
 
 		// Ore dictionary
 		OreDictionary.registerOre("gemRuby", new ItemStack(abyssGem, 1, 0));
 		OreDictionary.registerOre("gemEmerald", new ItemStack(abyssGem, 1, 1));
 		OreDictionary.registerOre("gemSapphire", new ItemStack(abyssGem, 1, 2));
 		OreDictionary.registerOre("gemTopaz", new ItemStack(abyssGem, 1, 3));
 		OreDictionary.registerOre("gemAbyssite", AbyssGem.getGem(Reference.INDEX_ABYSSITE));
 		OreDictionary.registerOre("gemPurite", AbyssGem.getGem(Reference.INDEX_PURITE));
 		
 		// Other stuff
 		addRandomChestGenLoot();
 		
 	}
 	
 	public static void initItemRecipes() {
 	    
 	    AbyssGem.addRecipes();
         GemShard.addRecipes();
         CraftingMaterial.addRecipes();
         AbyssFood.addRecipes();
         SigilStone.addRecipes();
         SigilScepter.addRecipes();
 	    TeleporterLinker.addRecipes();
 		
		ItemStack rAbyssRuby 			= new ItemStack(abyssGem, 1, 0);
		ItemStack rAbyssEmerald 		= new ItemStack(abyssGem, 1, 1);
		ItemStack rAbyssSapphire 		= new ItemStack(abyssGem, 1, 2);
		ItemStack rAbyssTopaz 			= new ItemStack(abyssGem, 1, 3);
		ItemStack rAbyssRubyPlus		= new ItemStack(abyssGem, 1, 4);
		ItemStack rAbyssEmeraldPlus		= new ItemStack(abyssGem, 1, 5);
		ItemStack rAbyssSapphirePlus	= new ItemStack(abyssGem, 1, 6);
		ItemStack rAbyssTopazPlus		= new ItemStack(abyssGem, 1, 7);
 		
 		// Abyss tools
 		AbyssSword.addRecipe(new ItemStack(swordAbyssRuby), rAbyssRuby, false);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssEmerald), rAbyssEmerald, false);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssSapphire), rAbyssSapphire, false);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssTopaz), rAbyssTopaz, false);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssRubyPlus), rAbyssRubyPlus, true);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssEmeraldPlus), rAbyssEmeraldPlus, true);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssSapphirePlus), rAbyssSapphirePlus, true);
 		AbyssSword.addRecipe(new ItemStack(swordAbyssTopazPlus), rAbyssTopazPlus, true);
 		
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssRuby), rAbyssRuby, false);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssEmerald), rAbyssEmerald, false);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssSapphire), rAbyssSapphire, false);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssTopaz), rAbyssTopaz, false);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssRubyPlus), rAbyssRubyPlus, true);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssEmeraldPlus), rAbyssEmeraldPlus, true);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssSapphirePlus), rAbyssSapphirePlus, true);
 		AbyssPickaxe.addRecipe(new ItemStack(pickaxeAbyssTopazPlus), rAbyssTopazPlus, true);
 		
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssRuby), rAbyssRuby, false);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssEmerald), rAbyssEmerald, false);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssSapphire), rAbyssSapphire, false);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssTopaz), rAbyssTopaz, false);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssRubyPlus), rAbyssRubyPlus, true);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssEmeraldPlus), rAbyssEmeraldPlus, true);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssSapphirePlus), rAbyssSapphirePlus, true);
 		AbyssShovel.addRecipe(new ItemStack(shovelAbyssTopazPlus), rAbyssTopazPlus, true);
 		
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssRuby), rAbyssRuby, false);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssEmerald), rAbyssEmerald, false);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssSapphire), rAbyssSapphire, false);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssTopaz), rAbyssTopaz, false);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssRubyPlus), rAbyssRubyPlus, true);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssEmeraldPlus), rAbyssEmeraldPlus, true);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssSapphirePlus), rAbyssSapphirePlus, true);
 		AbyssAxe.addRecipe(new ItemStack(axeAbyssTopazPlus), rAbyssTopazPlus, true);
 		
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssRuby), rAbyssRuby, false);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssEmerald), rAbyssEmerald, false);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssSapphire), rAbyssSapphire, false);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssTopaz), rAbyssTopaz, false);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssRubyPlus), rAbyssRubyPlus, true);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssEmeraldPlus), rAbyssEmeraldPlus, true);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssSapphirePlus), rAbyssSapphirePlus, true);
 		AbyssHoe.addRecipe(new ItemStack(hoeAbyssTopazPlus), rAbyssTopazPlus, true);
 	}
 	
 	private static void addRandomChestGenLoot() {
 		
 		// Abyssite - can spawn in most chests.
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 20));
 		
 		// Abyss gems - rarely in bonus chest, infrequently in pyramids
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 0), 1, 4, 5));
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 1), 1, 4, 5));
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 2), 1, 4, 5));
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 3), 1, 4, 5));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 0), 1, 4, 50));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 1), 1, 4, 50));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 2), 1, 4, 50));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 3), 1, 4, 50));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 0), 1, 4, 35));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 1), 1, 4, 35));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 2), 1, 4, 35));
 		ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 3), 1, 4, 35));
 		
 		// Taters on sticks! - fairly common
 		ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.potatoStick), 1, 4, 11));
 		ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.potatoStick), 1, 4, 44));
 		ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.potatoStick), 1, 4, 44));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.potatoStick), 1, 4, 33));
 		ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.potatoStick), 1, 4, 33));
 		
 		// Shrine chests
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(AbyssGem.getGem(Reference.INDEX_ABYSSITE), 1, 4, 30));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 0), 1, 4, 50));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 1), 1, 4, 50));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 2), 1, 4, 50));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.abyssGem, 1, 3), 1, 4, 50));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.craftingMaterial, 1, 0), 2, 4, 30));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(ModItems.teleporterLinker), 1, 1, 1));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.enderPearl), 1, 4, 15));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.ingotIron), 4, 10, 20));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.ingotGold), 2, 6, 5));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.diamond), 1, 4, 2));
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.book), 6, 12, 20));
 		
 		// Some configuring...
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).setMin(8);
 		ChestGenHooks.getInfo(Strings.SHRINE_CHEST).setMax(12);
 	}
 
 }
