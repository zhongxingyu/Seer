 package ca.celticminstrel.cookbook;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import static java.lang.Math.min;
 import static java.lang.Math.max;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Furnace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.minecraft.server.Item;
 
 public class Cookbook extends JavaPlugin implements Listener {
 	enum InitMethod {COMPOUND, RESET, CLEAN};
 	private static Logger log;
 	private static FileConfiguration config;
 	private LinkedHashMap<String,Recipe> newRecipes = new LinkedHashMap<String,Recipe>();
 	private Pattern stripComments = Pattern.compile("([^#]*)#.*");
 	private Pattern furnacePat = Pattern.compile("\\s*([a-zA-Z0-9_-]+)\\s+->\\s+([0-9]+)[x\\s]\\s*([a-zA-Z0-9_/-]+)\\s*(.*)");
 	private Pattern resultPat = Pattern.compile("\\s*->\\s*([0-9]+)[x\\s]\\s*([a-zA-Z0-9_/-]+)\\s*(.*)");
 	private static Cookbook plugin;
 	
 	@Override
 	public void onDisable() {
 		info(getDescription().getFullName() + " disabled.");
 	}
 	
 	@Override
 	public void onEnable() {
 		log = getLogger();
 		info(getDescription().getFullName() + " enabled.");
 		config = getConfig();
 		File yml = new File(getDataFolder(), "config.yml");
 		if(!yml.exists()) {
 			// TODO: Generate defaults
 			try {
 				yml.createNewFile();
 			} catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 		Option.setConfiguration(config);
 		loadRecipes();
 		// TODO: Uncomment this line
 		//if(plugin == null) getServer().getPluginManager().registerEvents(new WindowListener(this), this);
 		if(Option.FIX_LAVA_BUCKET.get()) {
 			getServer().getPluginManager().registerEvents(this, this);
 			info("Lava bucket fix enabled!");
 		}
 		if(Option.FIX_SOUP_BOWL.get()) {
 			Item.MUSHROOM_SOUP.a(Item.BOWL);
 			info("Soup bowl fix enabled!");
 		}
 		plugin = this;
 		debug("Finished loading!");
 	}
 	
 	@EventHandler
 	public void onFurnaceBurn(FurnaceBurnEvent evt) {
 		debug("Furnace burning with fuel " + evt.getFuel());
 		final Furnace furnace = (Furnace)evt.getFurnace().getState();
 		if(evt.getFuel().getType() == Material.LAVA_BUCKET)
 			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 				@Override public void run() {
 					furnace.getInventory().setItem(1, new ItemStack(Material.BUCKET, 1));
 				}
 			});
 	}
 
 	public static void info(String string) {
 		log.info(string);
 	}
 
 	public static void warning(String string) {
 		log.warning(string);
 	}
 
 	public static void debug(String string) {
 		log.info("[DEBUG] " + string);
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
 		for(Recipe recipe : newRecipes.values()) {
 			StringBuilder show = new StringBuilder();
 			show.append(recipe.getClass().getSimpleName() + "(");
 			if(recipe instanceof FurnaceRecipe) {
 				show.append(((FurnaceRecipe)recipe).getInput());
 			} else if(recipe instanceof ShapelessRecipe) {
 				show.append(((ShapelessRecipe)recipe).getIngredientList());
 			} else if(recipe instanceof ShapedRecipe) {
 				ShapedRecipe shaped = (ShapedRecipe)recipe;
 				show.append(Arrays.asList(shaped.getShape()) + " -- " + shaped.getIngredientMap());
 			}
 			ItemStack result = recipe.getResult();
 			show.append(" -> " + result);
 			if(!result.getEnchantments().isEmpty())
 				show.append(" -- " + result.getEnchantments());
 			show.append(')');
 			debug("Loaded " + show);
 		}
 	}
 	
 	private void loadShaped(ListIterator<String> iter, String prefix, String name) {
 		if(!iter.hasNext()) {
 			warning(prefix + "Expected shapeless recipe on line " + iter.nextIndex() + " but found end-of-file.");
 			return;
 		}
 		int width = 0;
 		String ingred;
 		ItemStack[] line1, line2, line3;
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
 
 	private void addShapedRecipe(int w, Matcher m, int lineno, String prefix, String name, ItemStack[]... lines) {
 		debug("Result pat subpattern matches: " + m.group(1) + ", " + m.group(2) + ", " + m.group(3));
 		ItemStack stack = parseResult(m.group(1), m.group(2), m.group(3), lineno, prefix);
 		debug("Have enchanted result? " + stack.getEnchantments());
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
 			ItemStack[] line = lines[i];
 			for(int j = 0; j < w; j++) {
				if(j < line.length && line[j] != null && line[j].getType() != Material.AIR)
 					recipe.setIngredient(c, line[j].getType(), line[j].getDurability());
 				c++;
 			}
 		}
 		addRecipe(recipe, name);
 	}
 
 	private ItemStack[] parseShapedLine(String line, int lineno, String prefix) {
 		String[] split = line.split("\\s+");
 		if(split.length > 3) {
 			warning(prefix + "Shape cannot be " + split.length + " wide on line " + lineno + ".");
 			return null;
 		}
 		ItemStack[] parsed = new ItemStack[split.length];
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
 		debug("Result pat subpattern matches: " + m.group(1) + ", " + m.group(2) + ", " + m.group(3));
 		ItemStack result = parseResult(m.group(1), m.group(2), m.group(3), iter.nextIndex(), prefix);
 		debug("Have enchanted result? " + result.getEnchantments());
 		ShapelessRecipe shapeless = new ShapelessRecipe(result);
 		for(int i = 0; i < min(split.length, 9); i++) {
 			String[] mat = split[i].split("/");
 			String data = mat.length > 1 ? mat[1] : "";
 			ItemStack ingred = parseMaterial(mat[0], data, iter.nextIndex(), prefix);
 			if(ingred == null) return;
 			shapeless.addIngredient(ingred.getType(), ingred.getDurability());
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
 		debug("Furnace pat subpattern matches: " + m.group(1) + ", " + m.group(2) + ", " + m.group(3) + ", " + m.group(4));
 		ItemStack result = parseResult(m.group(2), m.group(3), m.group(4), iter.nextIndex(), prefix);
 		if(result == null) return;
 		debug("Have enchanted result? " + result.getEnchantments());
 		FurnaceRecipe furnace = new FurnaceRecipe(result, smelt);
 		addRecipe(furnace, name);
 	}
 
 	private ItemStack parseResult(String amount, String item, String ench, int lineno, String prefix) {
 		if(!amount.matches("[0-9]+x?")) {
 			warning(prefix + "Invalid amount " + amount + " on line " + lineno + "; defaulting to 1.");
 			amount = "1";
 		}
 		amount = amount.replace("x", "");
 		String[] split = item.split("/");
 		Material material = parseMaterial(split[0], lineno, prefix);
 		if(material == null) return null;
 		short data = 0;
 		if(split.length > 1) {
 			if(!split[1].matches("[0-9]+")) {
 				warning(prefix + "Invalid data " + split[1] + " on line " + lineno + "; defaulting to 0.");
 				split[1] = "0";
 			}
 			data = Short.parseShort(split[1]);
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
 		}
 		ItemStack result = new ItemStack(material, Integer.parseInt(amount), data);
 		ench = ench.trim();
 		debug("Parsing enchantments: " + ench);
 		if(ench.isEmpty()) return result;
 		for(String magic : ench.split("\\s")) {
 			split = magic.split("=");
 			Enchantment enchantment = magic.matches("[0-9]+") ? Enchantment.getById(Integer.parseInt(split[0]))
 				: Enchantment.getByName(split[0]);
 			if(enchantment == null) {
 				warning(prefix + "Unknown enchantment " + split[0] + " on line " + lineno + "; skipping.");
 				continue;
 			}
 			if(!enchantment.canEnchantItem(result)) {
 				warning(prefix + "Invalid or conflicting enchantment " + split[0] + " for material " + material +
 					" on line " + lineno + "; skipping.");
 				continue;
 			}
 			int level = 1;
 			if(split.length > 1) {
 				if(split[1].matches("[0-9]+"))
 					level = Integer.parseInt(split[1]);
 				else warning(prefix + "Invalid enchantment level " + split[1] + " on line " + lineno +
 					"; defaulting to 1.");
 			}
 			if(level > enchantment.getMaxLevel())
 				warning(prefix + "Enchantment level " + level + " too high for enchantment " + split[0] +
 					" on line " + lineno + "; defaulting to maximum (" + (level = enchantment.getMaxLevel()) + ").");
 			result.addEnchantment(enchantment, level);
 		}
 		debug("Have enchanted result? " + result.getEnchantments());
 		return result;
 	}
 	
 	private Material parseMaterial(String name, int lineno, String prefix) {
 		name = name.replace('-', '_').toUpperCase();
 		Material mat = Material.getMaterial(name);
 		if(mat == null && name.matches("\\d+")) mat = Material.getMaterial(Integer.parseInt(name));
 		if(mat == null) warning(prefix + "Invalid material " + name + " on line " + lineno + ".");
 		return mat;
 	}
 	
 	private ItemStack parseMaterial(String name, String data, int lineno, String prefix) {
 		Material mat = parseMaterial(name, lineno, prefix);
 		if(mat == null) return null;
 		if(data.isEmpty()) return new ItemStack(mat, 0, (short)0);
 		if(!data.matches("-?[0-9]+")) {
 			if(data.equals("*")) {
 				data = "-1";
 			} else {
 				warning(prefix + "Invalid data " + data + " on line " + lineno + "; defaulting to 0.");
 				data = "0";
 			}
 		}
 		return new ItemStack(mat, Short.parseShort(data));
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
 		InitMethod init = InitMethod.RESET;
 		try {
 			init = InitMethod.valueOf(Option.STARTUP.get().toUpperCase());
 		} catch(Exception x) {
 			warning("An exception occurred which is probably innocuous.");
 			x.printStackTrace();
 		}
 		if(init == InitMethod.CLEAN) {
 			Bukkit.getServer().clearRecipes();
 		} else if(init == InitMethod.RESET) {
 			Bukkit.getServer().resetRecipes();
 		}
 	}
 	
 	public int numRecipes() {
 		return newRecipes.size();
 	}
 	
 	public Recipe getRecipe(int num) {
 		if(num < 0 || num >= numRecipes()) return null;
 		return newRecipes.get(num);
 	}
 }
