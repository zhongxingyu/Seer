 //------------------------------------------------------
 //
 //   Greg's Lighting - Common
 //
 //------------------------------------------------------
 
 package gcewing.lighting;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Hashtable;
 
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.network.*;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.*;
 import net.minecraftforge.common.*;
 import net.minecraftforge.oredict.*;
 
 import net.minecraft.block.*;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.*;
 import net.minecraft.entity.player.*;
 import net.minecraft.item.*;
 import net.minecraft.item.crafting.*;
 import net.minecraft.tileentity.*;
 import net.minecraft.world.*;
 
 @Mod(modid = "GregsLighting", name = "Greg's Lighting", version = GregsLighting.version)
@NetworkMod(clientSideRequired = true, serverSideRequired = true, versionBounds = "[1.8,1.9)")
 public class GregsLighting {
 
 	public final static String modName = "GregsLighting";
 
 	public static BlockFloodlight floodlight;
 	public static BlockFloodlightBeam floodlightBeam;
 	public static final String version = "1.5.1R1.8.1";
 
 	public static Item glowingIngot;
 
 	static String configName = modName + ".cfg";
 	static File cfgfile;
 	static OrderedProperties config;
 	static Map<Integer, String> idToName = new Hashtable<Integer, String>();
 	static int nextBlockID = 1;
 	static int nextItemID = 1;
 	static boolean autoAssign = true;
 
 	public static GregsLighting mod;
 	public static CreativeTabs itemTab = CreativeTabs.tabMisc;
 	
 	@SidedProxy(
 			clientSide = "gcewing.lighting.GregsLightingClient",
 			serverSide = "gcewing.lighting.GregsLightingServer")
 	public static GregsLightingBase proxy;
 
 	public GregsLighting() {
 		mod = this;
 	}
 
 	@Mod.PreInit
 	public void init(FMLPreInitializationEvent e) {
 		cfgfile = e.getSuggestedConfigurationFile();
 		load();
 	}
 
 	public void load() {
 		loadConfig();
 		registerBlocks();
 		registerTileEntities();
 		registerItems();
 		addRecipes();
 		saveConfig();
 		proxy.load();
 	}
 
 	void loadConfig() {
 		config = new OrderedProperties();
 		try {
 			config.load(new FileInputStream(cfgfile));
 		} catch (FileNotFoundException e) {
 			System.out.printf("%s: No existing config file\n", modName);
 		} catch (IOException e) {
 			System.out.printf("%s: Failed to read %s\n%s\n", modName,
 					cfgfile.getPath(), e);
 		}
 		config.extended = false;
 		Floodlight.init(config);
 	}
 
 	void saveConfig() {
 		try {
 			if (config.extended) {
 				System.out.printf("%s: Writing config file\n", modName);
 				config.store(new FileOutputStream(cfgfile), modName
 						+ " Configuration File");
 			}
 		} catch (IOException e) {
 			System.out.printf("%s: Failed to %s\n%s\n", modName,
 					cfgfile.getPath(), e);
 		}
 	}
 
 	void registerBlocks() {
 		floodlightBeam = new BlockFloodlightBeam(getBlockID(/* 254 */1031,
 				"floodlightBeam"));
 		addBlock("Floodlight Beam", floodlightBeam);
 		floodlight = new BlockFloodlight(
 				getBlockID(/* 255 */1030, "floodlight"));
 		floodlight.setHardness(1.5F).setCreativeTab(CreativeTabs.tabMisc);
 		addBlock("Floodlight", floodlight);
 	}
 
 	void registerTileEntities() {
 		GameRegistry.registerTileEntity(TEFloodlightBeam.class,
 				"gcewing.FloodlightBeam");
 	}
 
 	void registerItems() {
 		glowingIngot = addItem("Glowing Alloy Ingot",
 				new Item(getItemID(10305, "glowingAlloy")));
 	}
 
 	void addRecipes() {
 		if (config.getBoolean("enableSimpleFloodlight", true)) {
 			addRecipe(floodlight, 1, "IrI", "IgI", "GGG", 'I', Item.ingotIron,
 					'r', Item.redstone, 'g', glowingIngot, 'G', Block.glass);
 			addRecipe(glowingIngot, 1, "GiG", "igi", "GiG", 'G',
 					Block.glowStone, 'g', Item.goldNugget, 'i', Item.ingotIron);
 		}
 	}
 
 	public static void addRecipe(Item product, int qty, Object... params) {
 		GameRegistry.addRecipe(new ItemStack(product, qty), params);
 	}
 
 	public static void addRecipe(Block product, int qty, Object... params) {
 		GameRegistry.addRecipe(new ItemStack(product, qty), params);
 	}
 
 	public static void addOreRecipe(Item product, int qty, Object... params) {
 		IRecipe recipe = new ShapedOreRecipe(new ItemStack(product, qty),
 				params);
 		CraftingManager.getInstance().getRecipeList().add(recipe);
 	}
 
 	public static void addShapelessRecipe(Item product, int qty,
 			Object... params) {
 		GameRegistry.addShapelessRecipe(new ItemStack(product, qty), params);
 	}
 
 	public static void addSmeltingRecipe(Item product, int qty, Item input) {
 		GameRegistry.addSmelting(input.itemID, new ItemStack(product, qty), 0);
 	}
 
 	public static void addSmeltingRecipe(Item product, int qty, Block input) {
 		GameRegistry.addSmelting(input.blockID, new ItemStack(product, qty), 0);
 	}
 
 	public static int getBlockID(int defaultID, String name) {
 		if (autoAssign)
 			defaultID = nextUnusedBlockID();
 		return getBlockOrItemID(defaultID, "block." + name, 0);
 	}
 
 	public static int getItemID(int defaultID, String name) {
 		if (autoAssign)
 			defaultID = nextUnusedItemID();
 		return getBlockOrItemID(defaultID, "item." + name, 256);
 	}
 
 	public static int getBlockOrItemID(int defaultID, String name, int offset) {
 		String key = name + ".id";
 		int id;
 		if (config.containsKey(key)) {
 			String value = (String) config.get(key);
 			id = Integer.parseInt(value);
 		} else {
 			config.put(key, Integer.toString(defaultID));
 			id = defaultID;
 		}
 		idToName.put(Integer.valueOf(offset + id), name);
 		return id;
 	}
 
 
 	// int iconOverride(String name) {
 	// return ModLoader.addOverride("/gui/items.png",
 	// "/gcewing/prospecting/resources/" + name);
 	// }
 
 	public static Block addBlock(String name, Block block) {
 		// System.out.printf("%s: Adding block %s id %s\n", modName, name,
 		// block.blockID);
 		block.setUnlocalizedName("gcewing." + idToName.get(block.blockID));
 		GameRegistry.registerBlock(block);
 		LanguageRegistry.addName(block, name);
 		return block;
 	}
 
 	public static Item addItem(String name, Item item) {
 		item.setUnlocalizedName("gcewing." + idToName.get(item.itemID));
 		LanguageRegistry.addName(item, name);
 		item.setCreativeTab(itemTab);
 		return item;
 	}
 
 	public boolean shiftKeyDown() {
 		return false;
 	}
 
 	static int nextUnusedBlockID() {
 		// System.out.printf("%s: nextUnusedBlockID\n", modName);
 		while (nextBlockID < 4096) {
 			if (Block.blocksList[nextBlockID] == null)
 				return nextBlockID;
 			nextBlockID += 1;
 		}
 		throw new RuntimeException(modName + ": Out of block IDs");
 	}
 
 	static int nextUnusedItemID() {
 		// System.out.printf("%s: nextUnusedItemID\n", modName);
 		while (nextItemID < 32768) {
 			if (Item.itemsList[nextItemID + 256] == null)
 				return nextItemID;
 			nextItemID += 1;
 		}
 		throw new RuntimeException(modName + ": Out of item IDs");
 	}
 
 	public static boolean explodeMachineAt(World world, int x, int y, int z) {
 		try {
 			Class<?> mainIC2Class = Class.forName("ic2.common.IC2");
 			mainIC2Class.getMethod("explodeMachineAt", World.class,
 					Integer.TYPE, Integer.TYPE, Integer.TYPE).invoke(null,
 					world, x, y, z);
 			return true;
 		} catch (Exception e) {
 			System.out.printf("GregsLighting.explodeMachineAt: %s\n", e);
 			return false;
 		}
 	}
 
 }
