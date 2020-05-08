 package me.thegeekyguy101.TurtleMod;
 
 import me.thegeekyguy101.TurtleMod.entity.EntityDonatello;
 import me.thegeekyguy101.TurtleMod.entity.EntityHelloGuy;
 import me.thegeekyguy101.TurtleMod.entity.EntityLeonardo;
 import me.thegeekyguy101.TurtleMod.entity.EntityMichelangelo;
 import me.thegeekyguy101.TurtleMod.entity.EntityMineTurtle;
 import me.thegeekyguy101.TurtleMod.entity.EntityRaphael;
 import me.thegeekyguy101.TurtleMod.entity.EntityTurtle;
 import me.thegeekyguy101.TurtleMod.entity.EntityZombieTurtle;
import me.thegeekyguy101.TurtleMod.entity.customEntityList;
 import me.thegeekyguy101.TurtleMod.item.ItemTMNTrecord;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleBoots;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleChestplate;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleHelmet;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleLeather;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleLeggings;
 import me.thegeekyguy101.TurtleMod.item.ItemTurtleShell;
 import me.thegeekyguy101.TurtleMod.item.customSpawnEgg;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityEggInfo;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.EnumHelper;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "DJCTM", name = "DeathJustys Crazy Turtle Mod", version = "Alpha 4.0")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class TurtleMod {
 
 	// Proxy stuff
 	@SidedProxy(clientSide = "me.thegeekyguy101.TurtleMod.ClientProxy", serverSide = "me.thegeekyguy101.TurtleMod.ServerProxy")
 	public static ServerProxy proxy;
 
 	// Items
 	public static Item Turtleleather;
 	public static Item turtleShell;
 
 	public static Item turtleHelmet;
 	public static Item turtleChestplate;
 	public static Item turtleLeggings;
 	public static Item turtleBoots;
 
 	public static Item customSpawnEgg;
 
 	public static Item recordTMNT80;
 	public static Item recordTMNT2003;
 	public static Item recordTMNT2012;
 	public static Item recordMineturtle;
 
 	public static EnumArmorMaterial turtleArmor = EnumHelper.addArmorMaterial(
 			"Turtle", 5, new int[] { 1, 3, 2, 1 }, 15);
 
 	// creative tabs
 	public static CreativeTabs turtleTab;
 
 	// Mob stuff
 	static int startEntityId = 350;
 
 	public static int getUniqueEntityId() {
 		do {
 			startEntityId++;
		} while (customEntityList.getStringFromID(startEntityId) != null);
 		return startEntityId++;
 	}
 
 	public static void registerEntityEgg(Class<? extends Entity> entity,
 			int primaryColor, int secondaryColor) {
 		int id = getUniqueEntityId();
		customEntityList.IDtoClassMapping.put(id, entity);
		customEntityList.entityEggs.put(id, new EntityEggInfo(id, primaryColor,
 				secondaryColor));
 	}
 
 	@EventHandler
 	public void Load(FMLPreInitializationEvent Event) {
 
 		// more Proxy stuff
 		proxy.registerRenderThings();
 		proxy.registerServerTickHandler();
 		proxy.registerSound();
 
 		// custom creative tab
 		turtleTab = new CreativeTabs("turtleTab") {
 			public ItemStack getIconItemStack() {
 				return new ItemStack(turtleShell, 1, 0);
 			}
 		};
 		LanguageRegistry.instance()
 				.addStringLocalization("itemGroup.turtleTab", "en_US",
 						"DeathJusty's Crazy Turtle Mod");
 
 		// Armor stuff
 		int renderTurtleArmor = proxy.addArmor("turtle");
 		int renderShell = proxy.addArmor("shell");
 
 		recordTMNT80 = new ItemTMNTrecord(1006, "turtlemod:TMNT80", "TMNT80")
 				.setUnlocalizedName("TMNT80").func_111206_d("turtlemod:TMNT80");
 		recordTMNT2003 = new ItemTMNTrecord(1007, "turtlemod:TMNT2003",
 				"TMNT2003").setUnlocalizedName("TMNT2003").func_111206_d(
 				"turtlemod:TMNT2003");
 		recordTMNT2012 = new ItemTMNTrecord(1008, "turtlemod:TMNT2012",
 				"TMNT2012").setUnlocalizedName("TMNT2012").func_111206_d(
 				"turtlemod:TMNT2012");
 		recordMineturtle = new ItemTMNTrecord(1009, "turtlemod:mineturtle",
 				"mineturtle").setUnlocalizedName("mineturtle").func_111206_d(
 				"turtlemod:mineturtle");
 
 		LanguageRegistry.addName(recordTMNT80, "TMNT80");
 		LanguageRegistry.addName(recordTMNT2003, "TMNT2003");
 		LanguageRegistry.addName(recordTMNT2012, "TMNT2012");
 		LanguageRegistry.addName(recordMineturtle, "Mine Turtle");
 
 		// Turtle Leather
 		Turtleleather = new ItemTurtleLeather(1000)
 				.setUnlocalizedName("turtlemod:turtleLeather");
 		LanguageRegistry.addName(Turtleleather, "Turtle Leather");
 
 		// Turtle Shell
 		turtleShell = new ItemTurtleShell(1001, turtleArmor, renderShell, 1)
 				.setUnlocalizedName("turtleShell").setCreativeTab(
 						this.turtleTab);
 		GameRegistry.registerItem(turtleShell, "turtleShell");
 		LanguageRegistry.addName(turtleShell, "Turtle Shell");
 
 		// Armor
 		turtleHelmet = new ItemTurtleHelmet(1002, turtleArmor,
 				renderTurtleArmor, 0).setUnlocalizedName("turtleHelmet")
 				.setCreativeTab(this.turtleTab);
 		turtleChestplate = new ItemTurtleChestplate(1003, turtleArmor,
 				renderTurtleArmor, 1).setUnlocalizedName("turtleChestplate")
 				.setCreativeTab(this.turtleTab);
 		turtleLeggings = new ItemTurtleLeggings(1004, turtleArmor,
 				renderTurtleArmor, 2).setUnlocalizedName("turtleLeggings")
 				.setCreativeTab(this.turtleTab);
 		turtleBoots = new ItemTurtleBoots(1005, turtleArmor, renderTurtleArmor,
 				3).setUnlocalizedName("turtleBoots").setCreativeTab(
 				this.turtleTab);
 		GameRegistry.registerItem(turtleHelmet, "turtleHelmet");
 		GameRegistry.registerItem(turtleChestplate, "turtleChestplate");
 		GameRegistry.registerItem(turtleLeggings, "turtleLeggings");
 		GameRegistry.registerItem(turtleBoots, "turtleBoots");
 		LanguageRegistry.addName(turtleHelmet, "Turtle Helmet");
 		LanguageRegistry.addName(turtleChestplate, "Turtle Chestplate");
 		LanguageRegistry.addName(turtleLeggings, "Turtle Leggings");
 		LanguageRegistry.addName(turtleBoots, "Turtle Boots");
 		GameRegistry.addRecipe(new ItemStack(turtleHelmet), new Object[] {
 				"TST", "T T", 'T', Turtleleather, 'S', turtleShell });
 		GameRegistry.addRecipe(new ItemStack(turtleChestplate), new Object[] {
 				"T T", "TST", "TTT", 'T', Turtleleather, 'S', turtleShell });
 		GameRegistry.addRecipe(new ItemStack(turtleLeggings), new Object[] {
 				"TST", "T T", "T T", 'T', Turtleleather, 'S', turtleShell });
 		GameRegistry.addRecipe(new ItemStack(turtleBoots), new Object[] {
 				"T T", "T T", 'T', Turtleleather });
 
 		// Custom spawn egg for custom creative tab
 		customSpawnEgg = new customSpawnEgg(1020)
 				.setUnlocalizedName("customSpawnEgg")
 				.func_111206_d("turtlemod:customSpawnEgg")
 				.setCreativeTab(this.turtleTab);
 		LanguageRegistry.addName(customSpawnEgg, "Spawn");
 
 		EntityRegistry.findGlobalUniqueEntityId();
 
 		// Turtle
 		EntityRegistry.registerGlobalEntityID(EntityTurtle.class, "Turtle", 1);
 		EntityRegistry.registerGlobalEntityID(EntityZombieTurtle.class,
 				"ZombieTurtle", 2);
 		EntityRegistry.registerGlobalEntityID(EntityMineTurtle.class,
 				"MineTurtle", 3);
 		EntityRegistry.registerGlobalEntityID(EntityHelloGuy.class, "HelloGuy", 4);
 		EntityRegistry.registerGlobalEntityID(EntityLeonardo.class, "Leonardo", 5);
 		EntityRegistry.registerGlobalEntityID(EntityRaphael.class, "Raphael", 6);
 		EntityRegistry.registerGlobalEntityID(EntityDonatello.class, "Donatello", 7);
 		EntityRegistry.registerGlobalEntityID(EntityMichelangelo.class, "Michelangelo", 8);
 		EntityRegistry.addSpawn(EntityTurtle.class, 10, 2, 4,
 				EnumCreatureType.creature);
 		EntityRegistry.addSpawn(EntityZombieTurtle.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		EntityRegistry.addSpawn(EntityMineTurtle.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		EntityRegistry.addSpawn(EntityHelloGuy.class, 10, 2, 4, EnumCreatureType.creature);
 		EntityRegistry.addSpawn(EntityLeonardo.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		EntityRegistry.addSpawn(EntityRaphael.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		EntityRegistry.addSpawn(EntityDonatello.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		EntityRegistry.addSpawn(EntityMichelangelo.class, 10, 2, 4,
 				EnumCreatureType.monster);
 		// registerEntityEgg(EntityTurtle.class, 0x6F9DD1, 0x7AD16F);
 		LanguageRegistry.instance().addStringLocalization("entity.Turtle.name",
 				"Turtle");
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.ZombieTurtle.name", "Zombie Turtle");
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.MineTurtle.name", "Mine Turtle");
 		LanguageRegistry.instance().addStringLocalization(
 				"entity.HelloGuy.name", "Hello Guy");
 		LanguageRegistry.instance().addStringLocalization("entity.Leonardo.name", "Leonardo");
 		LanguageRegistry.instance().addStringLocalization("entity.Raphael.name", "Raphael");
 		LanguageRegistry.instance().addStringLocalization("entity.Donatello.name", "Donatello");
 		LanguageRegistry.instance().addStringLocalization("entity.Michelangelo.name", "Michelangelo");
 
 	}
 }
