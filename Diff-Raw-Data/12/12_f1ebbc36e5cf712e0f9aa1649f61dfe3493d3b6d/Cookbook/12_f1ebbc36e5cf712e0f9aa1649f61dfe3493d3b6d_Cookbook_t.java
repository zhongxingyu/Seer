 package ca.celticminstrel.cookbook;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import static java.lang.Math.min;
 import static java.lang.Math.max;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Material;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.ItemManager;
 
 import net.minecraft.server.CraftingManager;
 import net.minecraft.server.FurnaceRecipes;
 
 public class Cookbook extends JavaPlugin {
 	private static Logger log = Logger.getLogger("Minecraft.Cookbook");
 	private static Configuration config;
 	private LinkedHashMap<String,Recipe> newRecipes = new LinkedHashMap<String,Recipe>();
 	private Pattern stripComments = Pattern.compile("([^#]*)#.*");
 	private Pattern furnacePat = Pattern.compile("\\s*([a-zA-Z0-9_-]+)\\s+->\\s+([0-9]+)[x\\s]\\s*([a-zA-Z0-9_/-]+)\\s*");
 	private Pattern resultPat = Pattern.compile("\\s*->\\s*([0-9]+)[x\\s]\\s*([a-zA-Z0-9_/-]+)\\s*");
 	private static boolean haveSpout = false;
 	private static Cookbook plugin;
 	
 	@Override
 	public void onDisable() {
 		info(getDescription().getFullName() + " disabled.");
 	}
 	
 	@Override
 	public void onEnable() {
 		info(getDescription().getFullName() + " enabled.");
 		config = getConfiguration();
 		File yml = new File(getDataFolder(), "config.yml");
 		if(!yml.exists()) {
 			// TODO: Generate defaults
 			config.save();
 		}
 		Option.setConfiguration(config);
		if(Option.TRY_SPOUT.get()) {
			if(getServer().getPluginManager().getPlugin("Spout") != null) haveSpout = true;
			else haveSpout = false;
		}
 		loadRecipes();
 		if(plugin == null) registerListeners();
 		plugin = this;
 		debug("Finished loading!");
 	}
 
 	public static void info(String string) {
 		log.info("[Cookbook] " + string);
 	}
 
 	public static void warning(String string) {
 		log.warning("[Cookbook] " + string);
 	}
 
 	public static void debug(String string) {
 		log.info("[DEBUG] [Cookbook] " + string);
 	}
 	
 	private void registerListeners() {
 		// TODO
 		PluginManager pm = getServer().getPluginManager();
 		debug("haveSpout = " + haveSpout);
 		if(haveSpout) {
 			info("Spout detected; enabling Spout features.");
 			pm.registerEvent(Type.CUSTOM_EVENT, new WindowListener(this), new EventExecutor() {
 				@Override@SuppressWarnings("incomplete-switch")
 				public void execute(Listener listener, Event event) {
 					switch(event.getType()) {
 					case CUSTOM_EVENT:
 						((WindowListener)listener).onCustomEvent(event);
 						break;
 					case FURNACE_BURN:
 						((WindowListener)listener).onFurnaceBurn((FurnaceBurnEvent)event);
 						break;
 					case FURNACE_SMELT:
 						((WindowListener)listener).onFurnaceSmelt((FurnaceSmeltEvent)event);
 						break;
 					}
 				}
 			}, Priority.Normal, this);
 			pm.registerEvent(Type.PLAYER_INTERACT, new ClickListener(this), Priority.Normal, this);
 			// Set item names
 			ItemManager items = SpoutManager.getItemManager();
 			items.setItemName(Material.DOUBLE_STEP, "Double Stone Slab");
 			items.setItemName(Material.LONG_GRASS, "Long Grass");
 			items.setItemName(Material.DEAD_BUSH, "Dead Shrub");
 			// Toolbar
 			short toolbar = 127;
 			items.setItemName(Material.COMPASS, toolbar, "Navigate");
 			items.setItemName(Material.STONE, toolbar, "Basic Blocks");
 			items.setItemName(Material.CHEST, toolbar, "Containers");
 			items.setItemName(Material.SAPLING, toolbar, "Plants");
 			items.setItemName(Material.RAILS, toolbar, "Mechanisms");
 			items.setItemName(Material.WOOD_PICKAXE, toolbar, "Tools");
 			items.setItemName(Material.LEATHER_HELMET, toolbar, "Armour and Weapons");
 			items.setItemName(Material.APPLE, toolbar, "Food");
 			items.setItemName(Material.GREEN_RECORD, toolbar, "Misc");
 		}
 	}
 	
 	private void loadRecipes() {
 		resetOrClear();
 		File recipes = new File(getDataFolder(),config.getString("recipefile", "recipes.cb"));
 		String prefix = "Loading " + recipes.getName() + ": ";
 		try {
 			Scanner in = new Scanner(recipes);
 			List<String> file = new ArrayList<String>();
 			while(in.hasNextLine()) {
 				String line = in.nextLine();
 				Matcher m = stripComments.matcher(line);
 				if(m.matches()) line = m.group(1).trim();
 				else line = line.trim();
 				file.add(line);
 			}
 			ListIterator<String> iter = file.listIterator();
 			while(iter.hasNext()) {
 				String directive = iter.next();
 				if(directive.isEmpty()) continue;
 				if(!directive.startsWith("@")) {
 					warning(prefix + "Unexpected data on line " + iter.nextIndex() + "; skipping.");
 					continue;
 				}
 				String name = "";
 				if(directive.contains(" ")) {
 					int i = directive.indexOf(' ');
 					name = directive.substring(i + 1);
 					directive = directive.substring(0, i);
 				}
 				if(directive.equalsIgnoreCase("@Smelt")) loadSmelting(iter, prefix, name);
 				else if(directive.equalsIgnoreCase("@Shaped")) loadShaped(iter, prefix, name);
 				else if(directive.equalsIgnoreCase("@Shapeless")) loadShapeless(iter, prefix, name);
 				else warning(prefix + "Invalid directive " + directive + " on line " + iter.nextIndex() + ".");
 			}
 		} catch(FileNotFoundException e) {}
 		info("Loaded " + newRecipes.size() + " custom recipes.");
 		List<String> show = new ArrayList<String>();
 		for(Recipe recipe : newRecipes.values()) {
 			String display = recipe.getClass().getSimpleName() + "(";
 			if(recipe instanceof FurnaceRecipe) {
 				display += ((FurnaceRecipe)recipe).getInput();
 			} else if(recipe instanceof ShapelessRecipe) {
 				display += ((ShapelessRecipe)recipe).getIngredientList();
 			} else if(recipe instanceof ShapedRecipe) {
 				ShapedRecipe shaped = (ShapedRecipe)recipe;
 				display += Arrays.asList(shaped.getShape()) + " -- " + shaped.getIngredientMap();
 			}
 			display += " -> " + recipe.getResult() + ")";
 			show.add(display);
 		}
 		debug("Loaded " + show);
 	}
 	
 	private void loadShaped(ListIterator<String> iter, String prefix, String name) {
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected shapeless recipe on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		int width = 0;
 		String ingred;
 		MaterialData[] line1, line2, line3;
 		Matcher m;
 		// First line
 		ingred = iter.next();
 		m = resultPat.matcher(ingred);
 		if(m.matches()) {
 			warning(prefix + "Shaped recipe on line " + iter.nextIndex() + " is missing shape.");
 			return;
 		}
 		line1 = parseShapedLine(ingred, iter.nextIndex(), prefix);
 		if(line1 == null) return;
 		width = max(width, line1.length);
 		// Second line
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected shaped recipe (shape or result) on line " + iter.nextIndex() +
 				" but found end-of-file.");
 			return;
 		}
 		ingred = iter.next();
 		m = resultPat.matcher(ingred);
 		if(m.matches()) {
 			addShapedRecipe(width, m, iter.nextIndex(), prefix, name, line1);
 			return;
 		}
 		line2 = parseShapedLine(ingred, iter.nextIndex(), prefix);
 		if(line2 == null) return;
 		width = max(width, line2.length);
 		// Third line
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected shaped recipe (shape or result) on line " + iter.nextIndex() +
 				" but found end-of-file.");
 			return;
 		}
 		ingred = iter.next();
 		m = resultPat.matcher(ingred);
 		if(m.matches()) {
 			addShapedRecipe(width, m, iter.nextIndex(), prefix, name, line1, line2);
 			return;
 		}
 		line3 = parseShapedLine(ingred, iter.nextIndex(), prefix);
 		if(line3 == null) return;
 		width = max(width, line3.length);
 		// Result
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected recipe result on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		ingred = iter.next();
 		m = resultPat.matcher(ingred);
 		if(m.matches()) {
 			addShapedRecipe(width, m, iter.nextIndex(), prefix, name, line1, line2, line3);
 			return;
 		}
 		warning(prefix + "Missing recipe result on line " + iter.nextIndex() + ".");
 		iter.previous(); // Back up in case the "unknown data" is a directive starting the next recipe
 	}
 
 	private void addShapedRecipe(int w, Matcher m, int lineno, String prefix, String name, MaterialData[]... lines) {
 		ItemStack stack = parseResult(m.group(1), m.group(2), lineno, prefix);
 		ShapedRecipe recipe = new ShapedRecipe(stack);
 		int h = lines.length;
 		switch(h) {
 		case 1:
 			switch(w) {
 			case 1: recipe.shape("a"); break;
 			case 2: recipe.shape("ab"); break;
 			case 3: recipe.shape("abc"); break;
 			}
 			break;
 		case 2:
 			switch(w) {
 			case 1: recipe.shape("a","b"); break;
 			case 2: recipe.shape("ab","cd"); break;
 			case 3: recipe.shape("abc","def"); break;
 			}
 			break;
 		case 3:
 			switch(w) {
 			case 1: recipe.shape("a","b","c"); break;
 			case 2: recipe.shape("ab","cd","ef"); break;
 			case 3: recipe.shape("abc","def","ghi"); break;
 			}
 			break;
 		}
 		char c = 'a';
 		for(int i = 0; i < h; i++) {
 			MaterialData[] line = lines[i];
 			for(int j = 0; j < w; j++) {
 				if(j < line.length && line[j].getItemType() != Material.AIR)
 					recipe.setIngredient(c, line[j]);
 				c++;
 			}
 		}
 		addRecipe(recipe, name);
 	}
 
 	private MaterialData[] parseShapedLine(String line, int lineno, String prefix) {
 		String[] split = line.split("\\s+");
 		if(split.length > 3) {
 			warning(prefix + "Shape cannot be " + split.length + " wide on line " + lineno + ".");
 			return null;
 		}
 		MaterialData[] parsed = new MaterialData[split.length];
 		for(int i = 0; i < split.length; i++) {
 			String[] item = split[i].split("/");
 			parsed[i] = parseMaterial(item[0], item.length > 1 ? item[1] : "", lineno, prefix);
 		}
 		return parsed;
 	}
 
 	private void loadShapeless(ListIterator<String> iter, String prefix, String name) {
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected shapeless recipe on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		String recipe = iter.next();
 		String[] split = recipe.split("\\s*,\\s*");
 		if(split.length > 9) {
 			warning(prefix + "Too many ingredients for shapeless recipe on line " + iter.nextIndex() +
 				"; skipping excess.");
 		}
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected recipe result on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		Matcher m = resultPat.matcher(iter.next());
 		if(!m.matches()) {
 			warning(prefix + "Missing recipe result on line " + iter.nextIndex() + ".");
 			iter.previous(); // Back up in case the "unknown data" is a directive starting the next recipe
 			return;
 		}
 		ItemStack result = parseResult(m.group(1), m.group(2), iter.nextIndex(), prefix);
 		ShapelessRecipe shapeless = new ShapelessRecipe(result);
 		for(int i = 0; i < min(split.length, 9); i++) {
 			String[] mat = split[i].split("/");
 			String data = mat.length > 1 ? mat[1] : "";
 			MaterialData ingred = parseMaterial(mat[0], data, iter.nextIndex(), prefix);
 			if(ingred == null) return;
 			shapeless.addIngredient(ingred);
 		}
 		addRecipe(shapeless, name);
 	}
 
 	private void loadSmelting(ListIterator<String> iter, String prefix, String name) {
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected furnace recipe on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		String recipe = iter.next();
 		Matcher m = furnacePat.matcher(recipe);
 		if(!m.matches()) {
 			warning(prefix + "Missing furnace recipe on line " + iter.nextIndex() + ".");
 			iter.previous(); // Back up in case the "unknown data" is a directive starting the next recipe
 			return;
 		}
 		Material smelt = parseMaterial(m.group(1), iter.nextIndex(), prefix);
 		if(smelt == null) return;
 		ItemStack result = parseResult(m.group(2), m.group(3), iter.nextIndex(), prefix);
 		if(result == null) return;
 		FurnaceRecipe furnace = new FurnaceRecipe(result, smelt);
 		addRecipe(furnace, name);
 	}
 
 	private ItemStack parseResult(String amount, String item, int lineno, String prefix) {
 		if(!amount.matches("[0-9]+x?")) {
 			warning(prefix + "Invalid amount " + amount + " on line " + lineno + "; defaulting to 1.");
 			amount = "1";
 		}
 		amount = amount.replace("x", "");
 		String[] split = item.split("/");
 		Material material = parseMaterial(split[0], lineno, prefix);
 		if(material == null) return null;
 		if(split.length == 1) return new ItemStack(material, Integer.parseInt(amount));
 		if(!split[1].matches("[0-9]+")) {
 			warning(prefix + "Invalid data " + split[1] + " on line " + lineno + "; defaulting to 0.");
 			split[1] = "0";
 		}
 		short data = Short.parseShort(split[1]);
 		if(material.getMaxDurability() > 0 && data > material.getMaxDurability()) {
 			warning(prefix + "Invalid data " + data + " for material " + material + " on line " + lineno +
 				"; continuing anyway.");
 		} else if(material == Material.MAP && getServer().getMap(data) == null) {
 			warning(prefix + "Invalid data for material MAP on line " + lineno + "; map ID " + data +
 				" does not exist. Continuing anyway.");
 		} else if(material.getMaxDurability() == -1 && data >= 16) {
 			warning(prefix + "Invalid data " + data + " for material " + material + " on line " + lineno +
 				"; continuing anyway.");
 		}
 		return new ItemStack(material, Integer.parseInt(amount), data);
 	}
 	
 	private Material parseMaterial(String name, int lineno, String prefix) {
 		name = name.replace('-', '_').toUpperCase();
 		Material mat = Material.getMaterial(name);
 		if(mat == null && name.matches("\\d+")) mat = Material.getMaterial(Integer.parseInt(name));
 		if(mat == null) warning(prefix + "Invalid material " + name + " on line " + lineno + ".");
 		return mat;
 	}
 	
 	private MaterialData parseMaterial(String name, String data, int lineno, String prefix) {
 		Material mat = parseMaterial(name, lineno, prefix);
 		if(mat == null) return null;
 		if(data.isEmpty()) return new MaterialData(mat, (byte)0);
 		if(!data.matches("[0-9]+")) {
 			warning(prefix + "Invalid data " + data + " on line " + lineno + "; defaulting to 0.");
 			data = "0";
 		}
 		return new MaterialData(mat, Byte.parseByte(data));
 	}
 
 	private Random nameGen = new Random();
 	private void addRecipe(Recipe recipe, String name) {
 		getServer().addRecipe(recipe);
 		if(newRecipes.containsKey(name)) {
 			warning("Duplicate recipe name " + name + "; appending _ to make it unique.");
 			do {
 				name += '_';
 			} while(newRecipes.containsKey(name));
 		} else if(name.isEmpty()) {
 			do {
 				name = Integer.toHexString(nameGen.nextInt());
 			} while(newRecipes.containsKey(name));
 		}
 		newRecipes.put(name, recipe);
 	}
 
 	private void resetOrClear() {
 		newRecipes.clear();
 		boolean cleanInstall = config.getBoolean("clean-install", false);
 		CraftingManager cm = CraftingManager.getInstance();
 		FurnaceRecipes fm = FurnaceRecipes.getInstance();
 		try {
 			Field recipes = CraftingManager.class.getDeclaredField("b");
 			recipes.setAccessible(true);
 			if(cleanInstall) {
 				((List<?>)recipes.get(cm)).clear();
 			} else {
 				Constructor<CraftingManager> ctor = CraftingManager.class.getDeclaredConstructor();
 				ctor.setAccessible(true);
 				CraftingManager temp = ctor.newInstance();
 				recipes.set(cm, recipes.get(temp));
 			}
 			Field smelting = FurnaceRecipes.class.getDeclaredField("b");
 			smelting.setAccessible(true);
 			if(cleanInstall) {
 				((Map<?,?>)smelting.get(fm)).clear();
 			} else {
 				Constructor<FurnaceRecipes> ctor = FurnaceRecipes.class.getDeclaredConstructor();
 				ctor.setAccessible(true);
 				FurnaceRecipes temp = ctor.newInstance();
 				smelting.set(fm, smelting.get(temp));
 			}
 		} catch(SecurityException e) {}
 		catch(IllegalArgumentException e) {}
 		catch(NoSuchFieldException e) {}
 		catch(IllegalAccessException e) {}
 		catch(NoSuchMethodException e) {}
 		catch(InstantiationException e) {}
 		catch(InvocationTargetException e) {}
 	}
 	
 	public int numRecipes() {
 		return newRecipes.size();
 	}
 	
 	public Recipe getRecipe(int num) {
 		if(num < 0 || num >= numRecipes()) return null;
 		return newRecipes.get(num);
 	}
 }
