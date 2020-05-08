 package clashsoft.mods.moretools;
 
 import clashsoft.clashsoftapi.util.CSCrafting;
 import clashsoft.clashsoftapi.util.CSItems;
 import clashsoft.mods.moretools.item.ItemArmorMoreTools;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.*;
 import net.minecraft.src.ModLoader;
 import net.minecraftforge.common.EnumHelper;
 
 public class MoreToolsMod_Armor
 {	
 	public static MoreToolsMod_Armor instance = new MoreToolsMod_Armor();
 
 	//Armor Materials.
 
 	private static EnumArmorMaterial OBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", 2048, new int[] {10, 15, 13, 6}, 7);
 	private static EnumArmorMaterial REDSTONE = EnumHelper.addArmorMaterial("REDSTONE", 100, new int[] {1, 4, 3, 2}, 10);
 	private static EnumArmorMaterial COAL = EnumHelper.addArmorMaterial("COAL", 128, new int[] {1, 5, 3, 2}, 10);
 	private static EnumArmorMaterial LAPIS = EnumHelper.addArmorMaterial("LAPIS", 128, new int[] {1, 5, 3, 2}, 10);
 	private static EnumArmorMaterial END = EnumHelper.addArmorMaterial("END", 1024, new int[] {3, 8, 7, 6}, 10);
 	private static EnumArmorMaterial SPACE = EnumHelper.addArmorMaterial("SPACE", 700, new int[] {7, 10, 9, 8}, 15);
 	private static EnumArmorMaterial BONE = EnumHelper.addArmorMaterial("BONE", 24, new int[] {1, 5, 3, 2}, 10);
 	private static EnumArmorMaterial REED = EnumHelper.addArmorMaterial("REED", 16, new int[] {1, 5, 3, 2}, 10);
 	private static EnumArmorMaterial BLAZEROD = EnumHelper.addArmorMaterial("BLAZEROD", 64, new int[] {4, 10, 7, 3}, 10);
 	private static EnumArmorMaterial DIRT = EnumHelper.addArmorMaterial("DIRT", 16, new int[] {1, 1, 1, 1}, 10);
 	private static EnumArmorMaterial GLASS = EnumHelper.addArmorMaterial("GLASS", 16, new int[] {1, 1, 1, 1}, 10);
 	private static EnumArmorMaterial SANDSTONE = EnumHelper.addArmorMaterial("SANDSTONE", 64, new int[] {2, 4, 3, 2}, 10);
 	private static EnumArmorMaterial BRICK = EnumHelper.addArmorMaterial("BRICK", 32, new int[] {2, 4, 3, 2}, 10);
 	private static EnumArmorMaterial ENDSTONE = EnumHelper.addArmorMaterial("ENDSTONE", 100, new int[] {3, 5, 4, 2}, 10);
 	private static EnumArmorMaterial EPIC = EnumHelper.addArmorMaterial("EPIC", 4096, new int[] {13, 18, 15, 10}, 1);
 	public static EnumArmorMaterial INVISIBILITY = EnumHelper.addArmorMaterial("INVISIBILITY", 24, new int[] {2, 7, 5, 2}, 10);
 	private static EnumArmorMaterial HALLOWED = EnumHelper.addArmorMaterial("HALLOWED", 512, new int[] {8, 10, 9, 5}, 7);
 	private static EnumArmorMaterial GODDAWN = EnumHelper.addArmorMaterial("GODDAWN", 512, new int[] {8, 10, 9, 5}, 7);
 	private static EnumArmorMaterial EMERALD = EnumHelper.addArmorMaterial("EMERALD", 512, new int[] {3, 8, 6, 3}, 13);
 	private static EnumArmorMaterial NETHERSTAR = EnumHelper.addArmorMaterial("NETHERSTAR", 512, new int[] {11, 16, 14, 7}, 20);
 	private static EnumArmorMaterial POTATO = EnumHelper.addArmorMaterial("POTATO", 16, new int[] {1, 3, 3, 2}, 7);
 	private static EnumArmorMaterial CARROT = POTATO;
 	private static EnumArmorMaterial FISH = EnumHelper.addArmorMaterial("FISH", 16, new int[] {1, 3, 2, 1}, 7);
 	private static EnumArmorMaterial BACON = FISH;
 	private static EnumArmorMaterial GLOWSTONE = EnumHelper.addArmorMaterial("GLOWSTONE", 64, new int[] {1, 1, 1, 1}, 10);
 	private static EnumArmorMaterial SLIME = EnumHelper.addArmorMaterial("SLIME", 80, new int[] {1, 3, 2, 1}, 7);
 
 	//Items.
 
 	public static final Item obsidianHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 01, OBSIDIAN, ClientProxy.obsidian, 0)).setUnlocalizedName("obsidianHelmet");
 	public static final Item obsidianChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 02, OBSIDIAN, ClientProxy.obsidian, 1)).setUnlocalizedName("obsidianChestplate");
 	public static final Item obsidianLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 03, OBSIDIAN, ClientProxy.obsidian, 2)).setUnlocalizedName("obsidianLeggings");
 	public static final Item obsidianBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 04, OBSIDIAN, ClientProxy.obsidian, 3)).setUnlocalizedName("obsidianBoots");
 
 	public static final Item redstoneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 05, REDSTONE, ClientProxy.redstone, 0)).setUnlocalizedName("redstoneHelmet");
 	public static final Item redstoneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 06, REDSTONE, ClientProxy.redstone, 1)).setUnlocalizedName("redstoneChestplate");
 	public static final Item redstoneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 07, REDSTONE, ClientProxy.redstone, 2)).setUnlocalizedName("redstoneLeggings");
 	public static final Item redstoneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 8, REDSTONE, ClientProxy.redstone, 3)).setUnlocalizedName("redstoneBoots");
 
 	public static final Item coalHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 9, COAL, ClientProxy.coal, 0)).setUnlocalizedName("coalHelmet");
 	public static final Item coalChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 10, COAL, ClientProxy.coal, 1)).setUnlocalizedName("coalChestplate");
 	public static final Item coalLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 11, COAL, ClientProxy.coal, 2)).setUnlocalizedName("coalLeggings");
 	public static final Item coalBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 12, COAL, ClientProxy.coal, 3)).setUnlocalizedName("coalBoots");
 
 	public static final Item lapisHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 13, LAPIS, ClientProxy.lapis, 0)).setUnlocalizedName("lapisHelmet");
 	public static final Item lapisChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 14, LAPIS, ClientProxy.lapis, 1)).setUnlocalizedName("lapisChestplate");
 	public static final Item lapisLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 15, LAPIS, ClientProxy.lapis, 2)).setUnlocalizedName("lapisLeggings");
 	public static final Item lapisBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 16, LAPIS, ClientProxy.lapis, 3)).setUnlocalizedName("lapisBoots");
 
 	public static final Item enderHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 17, END, ClientProxy.ender, 0)).setUnlocalizedName("enderHelmet");
 	public static final Item enderChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 18, END, ClientProxy.ender, 1)).setUnlocalizedName("enderChestplate");
 	public static final Item enderLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 19, END, ClientProxy.ender, 2)).setUnlocalizedName("enderLeggings");
 	public static final Item enderBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 20, END, ClientProxy.ender, 3)).setUnlocalizedName("enderBoots");
 
 	public static final Item spaceHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 21, SPACE, ClientProxy.space, 0)).setUnlocalizedName("spaceHelmet");
 	public static final Item spaceChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 22, SPACE, ClientProxy.space, 1)).setUnlocalizedName("spaceChestplate");
 	public static final Item spaceLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 23, SPACE, ClientProxy.space, 2)).setUnlocalizedName("spaceLeggings");
 	public static final Item spaceBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 24, SPACE, ClientProxy.space, 3)).setUnlocalizedName("spaceBoots");
 
 	public static final Item boneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 25, BONE, ClientProxy.bone, 0)).setUnlocalizedName("boneHelmet");
 	public static final Item boneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 26, BONE, ClientProxy.bone, 1)).setUnlocalizedName("boneChestplate");
 	public static final Item boneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 27, BONE, ClientProxy.bone, 2)).setUnlocalizedName("boneLeggings");
 	public static final Item boneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 28, BONE, ClientProxy.bone, 3)).setUnlocalizedName("boneBoots");
 
 	public static final Item sugarcaneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 29, REED, ClientProxy.sugarcane, 0)).setUnlocalizedName("sugarcaneHelmet");
 	public static final Item sugarcaneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 30, REED, ClientProxy.sugarcane, 1)).setUnlocalizedName("sugarcaneChestplate");
 	public static final Item sugarcaneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 31, REED, ClientProxy.sugarcane, 2)).setUnlocalizedName("sugarcaneLeggings");
 	public static final Item sugarcaneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 32, REED, ClientProxy.sugarcane, 3)).setUnlocalizedName("sugarcaneBoots");
 
 	public static final Item blazerodHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 33, BLAZEROD, ClientProxy.blazerod, 0)).setUnlocalizedName("blazeHelmet");
 	public static final Item blazerodChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 34, BLAZEROD, ClientProxy.blazerod, 1)).setUnlocalizedName("blazeChestplate");
 	public static final Item blazerodLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 35, BLAZEROD, ClientProxy.blazerod, 2)).setUnlocalizedName("blazeLeggings");
 	public static final Item blazerodBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 36, BLAZEROD, ClientProxy.blazerod, 3)).setUnlocalizedName("blazeBoots");
 
 	public static final Item dirtHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 37, DIRT, ClientProxy.dirt, 0)).setUnlocalizedName("dirtHelmet");
 	public static final Item dirtChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 38, DIRT, ClientProxy.dirt, 1)).setUnlocalizedName("dirtChestplate");
 	public static final Item dirtLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 39, DIRT, ClientProxy.dirt, 2)).setUnlocalizedName("dirtLeggings");
 	public static final Item dirtBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 40, DIRT, ClientProxy.dirt, 3)).setUnlocalizedName("dirtBoots");
 
 	public static final Item glassHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 41, GLASS, ClientProxy.glass, 0)).setUnlocalizedName("glassHelmet");
 	public static final Item glassChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 42, GLASS, ClientProxy.glass, 1)).setUnlocalizedName("glassChestplate");
 	public static final Item glassLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 43, GLASS, ClientProxy.glass, 2)).setUnlocalizedName("glassLeggings");
 	public static final Item glassBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 44, GLASS, ClientProxy.glass, 3)).setUnlocalizedName("glassBoots");
 
 	public static final Item sandstoneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 45, SANDSTONE, ClientProxy.sandstone, 0)).setUnlocalizedName("sandstoneHelmet");
 	public static final Item sandstoneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 46, SANDSTONE, ClientProxy.sandstone, 1)).setUnlocalizedName("sandstoneChestplate");
 	public static final Item sandstoneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 47, SANDSTONE, ClientProxy.sandstone, 2)).setUnlocalizedName("sandstoneLeggings");
 	public static final Item sandstoneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 48, SANDSTONE, ClientProxy.sandstone, 3)).setUnlocalizedName("sandstoneBoots");
 
 	public static final Item brickHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 49, BRICK, ClientProxy.brick, 0)).setUnlocalizedName("brickHelmet");
 	public static final Item brickChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 50, BRICK, ClientProxy.brick, 1)).setUnlocalizedName("brickChestplate");
 	public static final Item brickLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 51, BRICK, ClientProxy.brick, 2)).setUnlocalizedName("brickLeggings");
 	public static final Item brickBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 52, BRICK, ClientProxy.brick, 3)).setUnlocalizedName("brickBoots");
 
 	public static final Item endstoneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 53, ENDSTONE, ClientProxy.endstone, 0)).setUnlocalizedName("endstoneHelmet");
 	public static final Item endstoneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 54, ENDSTONE, ClientProxy.endstone, 1)).setUnlocalizedName("endstoneChestplate");
 	public static final Item endstoneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 55, ENDSTONE, ClientProxy.endstone, 2)).setUnlocalizedName("endstoneLeggings");
 	public static final Item endstoneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 56, ENDSTONE, ClientProxy.endstone, 3)).setUnlocalizedName("endstoneBoots");
 
 	public static final Item hallowedHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 57, HALLOWED, ClientProxy.hallowed, 0)).setUnlocalizedName("godHelmet");
 	public static final Item hallowedChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 58, HALLOWED, ClientProxy.hallowed, 1)).setUnlocalizedName("godChestplate");
 	public static final Item hallowedLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 59, HALLOWED, ClientProxy.hallowed, 2)).setUnlocalizedName("godLeggings");
 	public static final Item hallowedBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 60, HALLOWED, ClientProxy.hallowed, 3)).setUnlocalizedName("godBoots");
 
 	public static final Item goddawnHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 61, GODDAWN, ClientProxy.goddawn, 0)).setUnlocalizedName("luziferHelmet");
 	public static final Item goddawnChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 62, GODDAWN, ClientProxy.goddawn, 1)).setUnlocalizedName("luziferChestplate");
 	public static final Item goddawnLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 63, GODDAWN, ClientProxy.goddawn, 2)).setUnlocalizedName("luziferLeggings");
 	public static final Item goddawnBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 64, GODDAWN, ClientProxy.goddawn, 3)).setUnlocalizedName("luziferBoots");
 
 	//2565, 66, 67, 68
 	
 	public static final Item invisibilityHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 69, INVISIBILITY, ClientProxy.invisibility, 0)).setUnlocalizedName("invisibilityHelmet");
 	public static final Item invisibilityChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 70, INVISIBILITY, ClientProxy.invisibility, 1)).setUnlocalizedName("invisibilityChestplate");
 	public static final Item invisibilityLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 71, INVISIBILITY, ClientProxy.invisibility, 2)).setUnlocalizedName("invisibilityLeggings");
 	public static final Item invisibilityBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 72, INVISIBILITY, ClientProxy.invisibility, 3)).setUnlocalizedName("invisibilityBoots");
 
 	public static final Item emeraldHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 73, EMERALD, ClientProxy.emerald, 0)).setUnlocalizedName("emeraldHelmet");
 	public static final Item emeraldChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 74, EMERALD, ClientProxy.emerald, 1)).setUnlocalizedName("emeraldChestplate");
 	public static final Item emeraldLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 75, EMERALD, ClientProxy.emerald, 2)).setUnlocalizedName("emeraldLeggings");
 	public static final Item emeraldBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 76, EMERALD, ClientProxy.emerald, 3)).setUnlocalizedName("emeraldBoots");
 
 	public static final Item netherstarHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 77, NETHERSTAR, ClientProxy.netherstar, 0)).setUnlocalizedName("netherstarHelmet");
 	public static final Item netherstarChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 78, NETHERSTAR, ClientProxy.netherstar, 1)).setUnlocalizedName("netherstarChestplate");
 	public static final Item netherstarLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 79, NETHERSTAR, ClientProxy.netherstar, 2)).setUnlocalizedName("netherstarLeggings");
 	public static final Item netherstarBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 80, NETHERSTAR, ClientProxy.netherstar, 3)).setUnlocalizedName("netherstarBoots");
 
 	public static final Item potatoHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 81, POTATO, ClientProxy.potato, 0)).setUnlocalizedName("potatoHelmet");
 	public static final Item potatoChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 82, POTATO, ClientProxy.potato, 1)).setUnlocalizedName("potatoChestplate");
 	public static final Item potatoLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 83, POTATO, ClientProxy.potato, 2)).setUnlocalizedName("potatoLeggings");
 	public static final Item potatoBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 84, POTATO, ClientProxy.potato, 3)).setUnlocalizedName("potatoBoots");
 
 	public static final Item carrotHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 85, CARROT, ClientProxy.carrot, 0)).setUnlocalizedName("carrotHelmet");
 	public static final Item carrotChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 86, CARROT, ClientProxy.carrot, 1)).setUnlocalizedName("carrotChestplate");
 	public static final Item carrotLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 87, CARROT, ClientProxy.carrot, 2)).setUnlocalizedName("carrotLeggings");
 	public static final Item carrotBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 88, CARROT, ClientProxy.carrot, 3)).setUnlocalizedName("carrotBoots");
 
 	public static final Item fishHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 89, FISH, ClientProxy.fish, 0)).setUnlocalizedName("fishHelmet");
 	public static final Item fishChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 90, FISH, ClientProxy.fish, 1)).setUnlocalizedName("fishChestplate");
 	public static final Item fishLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 91, FISH, ClientProxy.fish, 2)).setUnlocalizedName("fishLeggings");
 	public static final Item fishBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 92, FISH, ClientProxy.fish, 3)).setUnlocalizedName("fishBoots");
 
 	public static final Item baconHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 93, BACON, ClientProxy.bacon, 0)).setUnlocalizedName("baconHelmet");
 	public static final Item baconChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 94, BACON, ClientProxy.bacon, 1)).setUnlocalizedName("baconChestplate");
 	public static final Item baconLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 95, BACON, ClientProxy.bacon, 2)).setUnlocalizedName("baconLeggings");
 	public static final Item baconBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 96, BACON, ClientProxy.bacon, 3)).setUnlocalizedName("baconBoots");
 
 	public static final Item glowstoneHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 97, GLOWSTONE, ClientProxy.glowstone, 0)).setUnlocalizedName("glowstoneHelmet");
 	public static final Item glowstoneChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 98, GLOWSTONE, ClientProxy.glowstone, 1)).setUnlocalizedName("glowstoneChestplate");
 	public static final Item glowstoneLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 99, GLOWSTONE, ClientProxy.glowstone, 2)).setUnlocalizedName("glowstoneLeggings");
 	public static final Item glowstoneBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 100 + 00, GLOWSTONE, ClientProxy.glowstone, 3)).setUnlocalizedName("glowstoneBoots");
 
 	public static final Item slimeHelmet = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 100 + 01, SLIME, ClientProxy.slime, 0)).setUnlocalizedName("slimeHelmet");
 	public static final Item slimeChestplate = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 100 + 02, SLIME, ClientProxy.slime, 1)).setUnlocalizedName("slimeChestplate");
 	public static final Item slimeLeggings = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 100 + 03, SLIME, ClientProxy.slime, 2)).setUnlocalizedName("slimeLeggings");
 	public static final Item slimeBoots = (new ItemArmorMoreTools(MoreToolsMod.ARMORY_ID + 100 + 04, SLIME, ClientProxy.slime, 3)).setUnlocalizedName("slimeBoots");
 
 	//	@Init
 	public void load(FMLInitializationEvent event)
 	{
 
 		//Obsidian Armor.
 
 		addArmorItem(obsidianHelmet, new ItemStack(Block.obsidian), "Obsidian Helmet", 0);
 		addArmorItem(obsidianChestplate, new ItemStack(Block.obsidian), "Obsidian Chestplate", 1);
 		addArmorItem(obsidianLeggings, new ItemStack(Block.obsidian), "Obsidian Leggings", 2);
 		addArmorItem(obsidianBoots, new ItemStack(Block.obsidian), "Obsidian Boots", 3);
 
 		//Redstone Armor.
 
 		addArmorItem(redstoneHelmet, new ItemStack(Item.redstone), "Redstone Helmet", 0);
 		addArmorItem(redstoneChestplate, new ItemStack(Item.redstone), "Redstone Chestplate", 1);
 		addArmorItem(redstoneLeggings, new ItemStack(Item.redstone), "Redstone Leggings", 2);
 		addArmorItem(redstoneBoots, new ItemStack(Item.redstone), "Redstone Boots", 3);
 
 		//Coal Armor.
 
 		addArmorItem(coalHelmet, new ItemStack(Item.coal), "Coal Helmet", 0);
 		addArmorItem(coalChestplate, new ItemStack(Item.coal), "Coal Chestplate", 1);
 		addArmorItem(coalLeggings, new ItemStack(Item.coal), "Coal Leggings", 2);
 		addArmorItem(coalBoots, new ItemStack(Item.coal), "Coal Boots", 3);
 
 		//Lapis Armor.
 
 		addArmorItem(lapisHelmet, new ItemStack(Block.blockLapis), "Lapis Helmet", 0);
 		addArmorItem(lapisChestplate, new ItemStack(Block.blockLapis), "Lapis Chestplate", 1);
 		addArmorItem(lapisLeggings, new ItemStack(Block.blockLapis), "Lapis Leggings", 2);
 		addArmorItem(lapisBoots, new ItemStack(Block.blockLapis), "Lapis Boots", 3);
 
 		//Ender Armor.
 
 		addArmorItem(enderHelmet, new ItemStack(Item.enderPearl), "Ender Helmet", 0);
 		addArmorItem(enderChestplate, new ItemStack(Item.enderPearl), "Ender Chestplate", 1);
 		addArmorItem(enderLeggings, new ItemStack(Item.enderPearl), "Ender Leggings", 2);
 		addArmorItem(enderBoots, new ItemStack(Item.enderPearl), "Ender Boots", 3);
 
 		//Space Armor.
 
 		addArmorItem(spaceHelmet, new ItemStack(MoreToolsMod_Tools.spaceIngot), "Space Helmet", 0);
 		addArmorItem(spaceChestplate, new ItemStack(MoreToolsMod_Tools.spaceIngot), "Space Chestplate", 1);
 		addArmorItem(spaceLeggings, new ItemStack(MoreToolsMod_Tools.spaceIngot), "Space Leggings", 2);
 		addArmorItem(spaceBoots, new ItemStack(MoreToolsMod_Tools.spaceIngot), "Space Boots", 3);
 
 		//Bone Armor.
 
 		addArmorItem(boneHelmet, new ItemStack(Item.bone), "Bone Helmet", 0);
 		addArmorItem(boneChestplate, new ItemStack(Item.bone), "Bone Chestplate", 1);
 		addArmorItem(boneLeggings, new ItemStack(Item.bone), "Bone Leggings", 2);
 		addArmorItem(boneBoots, new ItemStack(Item.bone), "Bone Boots", 3);
 
 		//Sugarcane Armor.
 
 		addArmorItem(sugarcaneHelmet, new ItemStack(Item.reed), "Sugarcane Helmet", 0);
 		addArmorItem(sugarcaneChestplate, new ItemStack(Item.reed), "Sugarcane Chestplate", 1);
 		addArmorItem(sugarcaneLeggings, new ItemStack(Item.reed), "Sugarcane Leggings", 2);
 		addArmorItem(sugarcaneBoots, new ItemStack(Item.reed), "Sugarcane Boots", 3);
 
 		//Blaze Armor.
 
 		addArmorItem(blazerodHelmet, new ItemStack(Item.blazeRod), "Blaze Helmet", 0);
 		addArmorItem(blazerodChestplate, new ItemStack(Item.blazeRod), "Blaze Chestplate", 1);
 		addArmorItem(blazerodLeggings, new ItemStack(Item.blazeRod), "Blaze Leggings", 2);
 		addArmorItem(blazerodBoots, new ItemStack(Item.blazeRod), "Blaze Boots", 3);
 
 		//Dirt Armor.
 
 		addArmorItem(dirtHelmet, new ItemStack(Block.dirt), "Dirt Helmet", 0);
 		addArmorItem(dirtChestplate, new ItemStack(Block.dirt), "Dirt Chestplate", 1);
 		addArmorItem(dirtLeggings, new ItemStack(Block.dirt), "Dirt Leggings", 2);
 		addArmorItem(dirtBoots, new ItemStack(Block.dirt), "Dirt Boots", 3);
 
 		//Glass Armor.
 
 		addArmorItem(glassHelmet, new ItemStack(Block.glass), "Glass Helmet", 0);
 		addArmorItem(glassChestplate, new ItemStack(Block.glass), "Glass Chestplate", 1);
 		addArmorItem(glassLeggings, new ItemStack(Block.glass), "Glass Leggings", 2);
 		addArmorItem(glassBoots, new ItemStack(Block.glass), "Glass Boots", 3);
 
 		//Sandstone Armor.
 
 		addArmorItem(sandstoneHelmet, new ItemStack(Block.sandStone), "Sandstone Helmet", 0);
 		addArmorItem(sandstoneChestplate, new ItemStack(Block.sandStone), "Sandstone Chestplate", 1);
 		addArmorItem(sandstoneLeggings, new ItemStack(Block.sandStone), "Sandstone Leggings", 2);
 		addArmorItem(sandstoneBoots, new ItemStack(Block.sandStone), "Sandstone Boots", 3);
 
 		//Brick Armor.
 
 		addArmorItem(brickHelmet, new ItemStack(Item.brick), "Brick Helmet", 0);
 		addArmorItem(brickChestplate, new ItemStack(Item.brick), "Brick Chestplate", 1);
 		addArmorItem(brickLeggings, new ItemStack(Item.brick), "Brick Leggings", 2);
 		addArmorItem(brickBoots, new ItemStack(Item.brick), "Brick Boots", 3);
 
 		//Endstone Armor.
 
 		addArmorItem(endstoneHelmet, new ItemStack(Block.whiteStone), "Endstone Helmet", 0);
 		addArmorItem(endstoneChestplate, new ItemStack(Block.whiteStone), "Endstone Chestplate", 1);
 		addArmorItem(endstoneLeggings, new ItemStack(Block.whiteStone), "Endstone Leggings", 2);
 		addArmorItem(endstoneBoots, new ItemStack(Block.whiteStone), "Endstone Boots", 3);
 
 		//Hallowed Armor.
 
 		addName(hallowedHelmet, "God's Helmet");
 		addName(hallowedChestplate, "God's Chestplate");
 		addName(hallowedLeggings, "God's Leggings");
 		addName(hallowedBoots, "God's Boots");
 		ModLoader.addRecipe(new ItemStack(hallowedHelmet, 1), new Object[]{
 			"XXX",
 			"XxX",
			Character.valueOf('x'), Block.blockGold,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(hallowedChestplate, 1), new Object[]{
 			"XxX",
 			"XXX",
 			"XXX",
			Character.valueOf('x'), Block.blockGold,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(hallowedLeggings, 1), new Object[]{
 			"XXX",
 			"XxX",
 			"XxX",
			Character.valueOf('x'), Block.blockGold,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(hallowedHelmet, 1), new Object[]{
 			"x x",
 			"XxX",
 			"XxX",
			Character.valueOf('x'), Block.blockGold,
 			Character.valueOf('x'), Item.redstone
 		});
 
 		//Goddawn Armor.
 
 		addName(goddawnHelmet, "Luzifer's Helmet");
 		addName(goddawnChestplate, "Luzifer's Chestplate");
 		addName(goddawnLeggings, "Luzifer's Leggings");
 		addName(goddawnBoots, "Luzifer's Boots");
 		ModLoader.addRecipe(new ItemStack(goddawnHelmet, 1), new Object[]{
 			"XXX",
 			"XxX",
			Character.valueOf('x'), Block.netherrack,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(goddawnChestplate, 1), new Object[]{
 			"XxX",
 			"XXX",
 			"XXX",
			Character.valueOf('x'), Block.netherrack,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(goddawnLeggings, 1), new Object[]{
 			"XXX",
 			"XxX",
 			"XxX",
			Character.valueOf('x'), Block.netherrack,
 			Character.valueOf('x'), Item.redstone
 		});
 		ModLoader.addRecipe(new ItemStack(goddawnHelmet, 1), new Object[]{
 			"x x",
 			"XxX",
 			"XxX",
 			Character.valueOf('x'), Block.netherrack,
 			Character.valueOf('x'), Item.redstone
 		});
 		
 		//Invisibility Armor.
 
 		CSItems.addItemWithRecipe(invisibilityHelmet, 0, "Invisibility Helmet", 1, new Object[] { "XxX", "X X", 'X', Item.ingotIron, 'x', Item.diamond });
 		CSItems.addItemWithRecipe(invisibilityChestplate, 0, "Invisibility Chestplate", 1, new Object[] { "X X", "XxX", "XXX", 'X', Item.ingotIron, 'x', Item.diamond });
 		CSItems.addItemWithRecipe(invisibilityLeggings, 0, "Invisibility Leggings", 1, new Object[] { "XxX", "X X", "X X", 'X', Item.ingotIron, 'x', Item.diamond });
 		CSItems.addItemWithRecipe(invisibilityBoots, 0, "Invisibility Boots", 1, new Object[] { "x x", "X X", 'X', Item.ingotIron, 'x', Item.diamond });
 
 		//Emerald Armor.
 
 		addArmorItem(emeraldHelmet, new ItemStack(Item.emerald), "Emerald Helmet", 0);
 		addArmorItem(emeraldChestplate, new ItemStack(Item.emerald), "Emerald Chestplate", 1);
 		addArmorItem(emeraldLeggings, new ItemStack(Item.emerald), "Emerald Leggings", 2);
 		addArmorItem(emeraldBoots, new ItemStack(Item.emerald), "Emerald Boots", 3);
 
 		//Netherstar Armor.
 
 		addArmorItem(netherstarHelmet, new ItemStack(Item.netherStar), "Nether Star Helmet", 0);
 		addArmorItem(netherstarChestplate, new ItemStack(Item.netherStar), "Nether Star Chestplate", 1);
 		addArmorItem(netherstarLeggings, new ItemStack(Item.netherStar), "Nether Star Leggings", 2);
 		addArmorItem(netherstarBoots, new ItemStack(Item.netherStar), "Nether Star Boots", 3);
 
 		//Potato Armor.
 
 		addArmorItem(potatoHelmet, new ItemStack(Item.potato), "Potato Helmet", 0);
 		addArmorItem(potatoChestplate, new ItemStack(Item.potato), "Potato Chestplate", 1);
 		addArmorItem(potatoLeggings, new ItemStack(Item.potato), "Potato Leggings", 2);
 		addArmorItem(potatoBoots, new ItemStack(Item.potato), "Potato Boots", 3);
 
 		//Carrot Armor.
 
 		addArmorItem(carrotHelmet, new ItemStack(Item.carrot), "Carrot Helmet", 0);
 		addArmorItem(carrotChestplate, new ItemStack(Item.carrot), "Carrot Chestplate", 1);
 		addArmorItem(carrotLeggings, new ItemStack(Item.carrot), "Carrot Leggings", 2);
 		addArmorItem(carrotBoots, new ItemStack(Item.carrot), "Carrot Boots", 3);
 
 		//Fish-Scale Armor.
 
 		addArmorItem(fishHelmet, new ItemStack(Item.fishRaw), "Fishscale Helmet", 0);
 		addArmorItem(fishChestplate, new ItemStack(Item.fishRaw), "Fishscale Chestplate", 1);
 		addArmorItem(fishLeggings, new ItemStack(Item.fishRaw), "Fishscale Leggings", 2);
 		addArmorItem(fishBoots, new ItemStack(Item.fishRaw), "Fishscale Boots", 3);
 
 		//Bacon Armor.
 
 		addArmorItem(baconHelmet, new ItemStack(Item.porkRaw), "Bacon Helmet", 0);
 		addArmorItem(baconChestplate, new ItemStack(Item.porkRaw), "Bacon Chestplate", 1);
 		addArmorItem(baconLeggings, new ItemStack(Item.porkRaw), "Bacon Leggings", 2);
 		addArmorItem(baconBoots, new ItemStack(Item.porkRaw), "Bacon Boots", 3);
 
 		//Glowstone Armor
 
 		addArmorItem(glowstoneHelmet, new ItemStack(Block.glowStone), "Glowstone Helmet", 0);
 		addArmorItem(glowstoneChestplate, new ItemStack(Block.glowStone), "Glowstone Chestplate", 1);
 		addArmorItem(glowstoneLeggings, new ItemStack(Block.glowStone), "Glowstone Leggings", 2);
 		addArmorItem(glowstoneBoots, new ItemStack(Block.glowStone), "Glowstone Boots", 3);
 
 		//Slime Armor
 
 		addArmorItem(slimeHelmet, new ItemStack(Item.slimeBall), "Slime Helmet", 0);
 		addArmorItem(slimeChestplate, new ItemStack(Item.slimeBall), "Slime Chestplate", 1);
 		addArmorItem(slimeLeggings, new ItemStack(Item.slimeBall), "Slime Leggings", 2);
 		addArmorItem(slimeBoots, new ItemStack(Item.slimeBall), "Slime Boots", 3);
 	}
 
 	/**
 	 * Adds an ArmorItem with all things.
 	 * @param item
 	 * @param input
 	 * @param name
 	 * @param iconIndex
 	 * @param part
 	 */
 	public void addArmorItem(Item item, ItemStack input, String name, int part)
 	{
 		addName(item, name);
 		CSCrafting.addArmorRecipe(new ItemStack(item), input, part);
 	}
 
 
 	public void addName(Object o, String name)
 	{
 		LanguageRegistry.instance().addNameForObject(o, "en_US", name);
 	}
 }
