 package digi.recipeManager;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.logging.*;
 
 import net.minecraft.server.*;
 
 import org.bukkit.*;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.*;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.*;
 
 import digi.recipeManager.data.*;
 import digi.recipeManager.data.Item;
 import digi.recipeManager.data.Recipe;
 
 public class Recipes
 {
 	private ItemStack							placeholderItem		= new ItemStack(Material.LONG_GRASS, 0, (short)1337);
 	protected List<Craft>						craftRecipes		= new ArrayList<Craft>();
 	protected List<Combine>						combineRecipes		= new ArrayList<Combine>();
 	protected HashMap<String, Smelt>			smeltRecipes		= new HashMap<String, Smelt>();
 	protected HashMap<String, Fuel>				fuels				= new HashMap<String, Fuel>();
 	
 	private HashMap<String, List<String>>		recipeErrors		= null;
 //	private List<String>					recipeErrors		= null;
 	private String								currentFile			= null;
 	private int									currentFileLine		= 0;
 	
 	private int									craftNum			= 0;
 	private int									combineNum			= 0;
 	private boolean								smeltCustomRecipes	= false;
 	
 	protected HashMap<String, FurnaceData>		furnaceData			= new HashMap<String, FurnaceData>();
 	protected HashMap<String, MutableDouble>	furnaceSmelting		= new HashMap<String, MutableDouble>();
 	private int									furnaceTaskId		= 0;
 	
 	protected boolean							hasExplosive		= false;
 	private HashSet<String>						overriddenRecipes	= new HashSet<String>();
 	
 	private Logger								log;
 	private String								NL					= System.getProperty("line.separator");
 	
 	public Recipes()
 	{
 		log = RecipeManager.plugin.getLogger();
 	}
 	
 	protected void clearData()
 	{
 		reset();
 		
 		placeholderItem = null;
 		craftRecipes = null;
 		combineRecipes = null;
 		smeltRecipes = null;
 		fuels = null;
 		
 		recipeErrors = null;
 		currentFile = null;
 		currentFileLine = 0;
 		
 		craftNum = 0;
 		combineNum = 0;
 		
 		furnaceData = null;
 		furnaceSmelting = null;
 		furnaceTaskId = 0;
 		
 		overriddenRecipes = null;
 		
 		log = null;
 		NL = null;
 	}
 	
 	/**
 	 * Gets all CRAFT recipes (workbench shaped)
 	 * 
 	 * @return
 	 */
 	public List<Craft> getCraftRecipes()
 	{
 		return craftRecipes;
 	}
 	
 	/**
 	 * Gets Craft recipe class from the placeholder result so you can get the real result of the recipe
 	 * 
 	 * @param placeholderResult
 	 *            the ItemStack result placeholder
 	 * @return Can be null if not found
 	 */
 	public Craft getCraftRecipe(ItemStack placeholderResult)
 	{
 		return (!isCustomRecipe(placeholderResult) || craftRecipes.size() >= placeholderResult.getAmount() ? null : craftRecipes.get(placeholderResult.getAmount()));
 	}
 	
 	/**
 	 * Gets all COMBINE recipes (workbench shapeless)
 	 * 
 	 * @return
 	 */
 	public List<Combine> getCombineRecipes()
 	{
 		return combineRecipes;
 	}
 	
 	/**
 	 * Gets Combine recipe class from the placeholder result so you can get the real result of the recipe
 	 * 
 	 * @param placeholderResult
 	 *            the ItemStack result placeholder
 	 * @return Can be null if not found
 	 */
 	public Combine getCombineRecipe(ItemStack placeholderResult)
 	{
 		return (!isCustomRecipe(placeholderResult) || combineRecipes.size() >= placeholderResult.getAmount() ? null : combineRecipes.get(placeholderResult.getAmount()));
 	}
 	
 	/**
 	 * Gets all SMELT recipes (furnace recipes)
 	 * The string in the hashmap is the ID of the recipe
 	 * 
 	 * @return
 	 */
 	public HashMap<String, Smelt> getSmeltRecipes()
 	{
 		return smeltRecipes;
 	}
 	
 	/**
 	 * Gets Smelt recipe class from the ingredient
 	 * 
 	 * @param ingredient
 	 *            the ItemStack ingredient
 	 * @return Can be null if not found
 	 */
 	public Smelt getSmeltRecipe(ItemStack ingredient)
 	{
 		ItemData item = new ItemData(ingredient);
 		Smelt recipe = smeltRecipes.get(item.getType() + "");
 		
 		if(recipe == null)
 			return smeltRecipes.get(item.getType() + ":" + item.getData());
 		
 		return recipe;
 	}
 	
 	/**
 	 * Gets all FUEL recipes (furnace fuels)
 	 * 
 	 * @return
 	 */
 	public HashMap<String, Fuel> getFuels()
 	{
 		return fuels;
 	}
 	
 	/**
 	 * Gets a Fuel recipe class recipe from an ingredient (the fuel itself)
 	 * 
 	 * @param ingredient
 	 *            the ItemStack ingredient
 	 * @return
 	 */
 	public Fuel getFuelRecipe(ItemStack ingredient)
 	{
 		if(ingredient == null)
 			return null;
 		
 		return getFuelRecipe(new ItemData(ingredient));
 	}
 	
 	/**
 	 * Gets a Fuel recipe class recipe from an ingredient (the fuel itself)
 	 * 
 	 * @param ingredient
 	 *            the ItemData ingredient
 	 * @return
 	 */
 	public Fuel getFuelRecipe(ItemData ingredient)
 	{
 		if(ingredient == null)
 			return null;
 		
 		Fuel recipe = fuels.get(ingredient.getType() + "");
 		
 		if(recipe == null)
 			return fuels.get(ingredient.getType() + ":" + ingredient.getData());
 		
 		return recipe;
 	}
 	
 	/**
 	 * Returns a FurnaceData class which has some new serval furnace specific methods.
 	 * If furnace data entry does not exist it will be created and returned, if you don't want this to happen just use the other method with the 2nd argument as false.
 	 * 
 	 * @param location
 	 *            location of the furnace
 	 * @return FurnaceData object
 	 */
 	public FurnaceData getFurnaceData(Location location)
 	{
 		return getFurnaceData(location, true);
 	}
 	
 	/**
 	 * Returns a FurnaceData class which has serval furnace specific methods.<br>
 	 * If 2nd argument is true and the FurnaceData object doesn't exist, it will be created and returned, using false will return null if object doesn't exist.
 	 * 
 	 * @param location
 	 *            location of the furnace
 	 * @return FurnaceData object or null if create is false and object doesn't exist
 	 */
 	public FurnaceData getFurnaceData(Location location, boolean create)
 	{
 		if(create)
 		{
 			String locStr = locationToString(location);
 			
 			if(!furnaceData.containsKey(locStr))
 				furnaceData.put(locStr, new FurnaceData());
 		}
 		
 		return furnaceData.get(locationToString(location));
 	}
 	
 	/**
 	 * Gets the placeholder item that RecipeManager uses for recipe results to track them.
 	 * Only ID and Data values are appliable, the amount is used as the recipe's ID.
 	 * 
 	 * @return The item as ItemStack object
 	 */
 	public ItemStack getPlaceholderItem()
 	{
 		return placeholderItem;
 	}
 	
 	/**
 	 * Does result fit player's inventory on shift+click or player's cursor on click ?
 	 * 
 	 * @param player
 	 *            player for inventory
 	 * @param result
 	 *            result in question
 	 * @param cursor
 	 *            cursor item if not shift+click
 	 * @param shiftClick
 	 *            is shift+click ?
 	 * @return false if inventory or cursor is full, otherwise true.
 	 */
 	public boolean isResultTakeable(Player player, Item result, ItemStack cursor, boolean shiftClick)
 	{
 		if(shiftClick)
 		{
 			for(ItemStack item : player.getInventory().getContents())
 			{
 				if(item == null || item.getTypeId() == 0 || (result.compareItemStack(item) && item.getAmount() < item.getType().getMaxStackSize()))
 					return true;
 			}
 		}
 		else
 		{
 			if(cursor == null || cursor.getTypeId() == 0 || (result.compareItemStack(cursor) && cursor.getAmount() < cursor.getType().getMaxStackSize()))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Check if item is the placeholder item used by RecipeManager to track its recipes
 	 * Works only for WORKBENCH recipes because only those use placeholders.
 	 * 
 	 * @param result
 	 * @return
 	 */
 	public boolean isCustomRecipe(ItemStack result)
 	{
 		return (result != null && result.getTypeId() == placeholderItem.getTypeId() && result.getDurability() == placeholderItem.getDurability());
 	}
 	
 	/**
 	 * Checks if recipe is a custom RecipeManager recipe.
 	 * Works for workbench and furnace recipes as well.
 	 * 
 	 * @param recipe
 	 * @return
 	 */
 	public boolean isCustomRecipe(org.bukkit.inventory.Recipe recipe)
 	{
 		if(recipe instanceof FurnaceRecipe)
 		{
 			Smelt smelt = getSmeltRecipe(((FurnaceRecipe)recipe).getInput());
 			
 			return (smelt != null);
 		}
 		
 		return isCustomRecipe(recipe.getResult());
 	}
 	
 	/**
 	 * Gets all recipes for a single item
 	 * Useful for printing recipes
 	 * 
 	 * @param item
 	 *            item as ItemStack
 	 * @param ingredient
 	 *            item is ingredient (true) or result (false) ?
 	 * @return list of recipes
 	 */
 	public List<Recipe> getRecipesForItem(ItemStack item, boolean ingredient)
 	{
 		return getRecipesForItem(new Item(item), ingredient);
 	}
 	
 	/**
 	 * Gets all recipes for a single item
 	 * Useful for printing recipes
 	 * 
 	 * @param item
 	 *            item as Item
 	 * @param ingredient
 	 *            item is ingredient (true) or result (false) ?
 	 * @return list of recipes
 	 */
 	public List<Recipe> getRecipesForItem(Item item, boolean ingredient)
 	{
 		List<Recipe> recipes = new ArrayList<Recipe>();
 		
 		for(Craft r : craftRecipes)
 		{
 			if(ingredient)
 			{
 				for(ItemData i : r.getIngredients())
 				{
 					if(item.compareItemData(i))
 					{
 						recipes.add(r);
 						break;
 					}
 				}
 			}
 			else
 			{
 				for(Item i : r.getResults())
 				{
 					if(item.compareItemData(i))
 					{
 						recipes.add(r);
 						break;
 					}
 				}
 			}
 		}
 		
 		for(Combine r : combineRecipes)
 		{
 			for(ItemData i : (ingredient ? r.getIngredients() : r.getResults()))
 			{
 				if(item.compareItemData(i))
 				{
 					recipes.add(r);
 					break;
 				}
 			}
 		}
 		
 		for(Smelt r : smeltRecipes.values())
 		{
 			if(item.compareItemData(ingredient ? r.getIngredient() : r.getResult()))
 				recipes.add(r);
 		}
 		
 		if(ingredient)
 		{
 			for(Fuel r : fuels.values())
 			{
 				if(item.compareItemData(r.getFuel()))
 					recipes.add(r);
 			}
 		}
 		
 		return recipes;
 	}
 	
 	/* ====================================================================================== */
 	
 	/**
 	 * Removes all RecipeManager's custom recipes from the server.
 	 */
 	public void removeCustomRecipes()
 	{
 		Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.recipeIterator();
 		org.bukkit.inventory.Recipe recipe;
 		
 		while(recipes.hasNext())
 		{
 			if((recipe = recipes.next()) != null && isCustomRecipe(recipe))
 				recipes.remove();
 		}
 	}
 	
 	/**
 	 * Removes all vanilla Minecraft recipes.<br>
 	 * Note: prints "(num) recipes" in console, triggered by the game, nothing to worry about tough.
 	 */
 	@SuppressWarnings(
 	{
 		"unchecked",
 		"rawtypes"
 	})
 	public void removeDefaultRecipes() // removes only vanilla Minecraft recipes
 	{
 		// remove furnace recipes, easy.
 		RecipesFurnace.getInstance().recipes.keySet().removeAll(new RecipesFurnace().recipes.keySet());
 		
 		Logger logger = Logger.getLogger("Minecraft");
 		Filter oldFilter = logger.getFilter();
 		
 		logger.setFilter(new Filter()
 		{
 			@Override
 			public boolean isLoggable(LogRecord log)
 			{
 				return !log.getMessage().endsWith(" recipes");
 			}
 		});
 		
 		// remove workbench recipes, not so easy...
 		List workbenchRaw = new CraftingManager().recipes;
 		List<String> workbench = new ArrayList<String>();
 		
 		logger.setFilter(oldFilter);
 		
 		for(Object raw : workbenchRaw)
 		{
 			if(raw instanceof ShapedRecipes)
 				workbench.add(shapedToString(((ShapedRecipes)raw).toBukkitRecipe()));
 			
 			else if(raw instanceof ShapelessRecipes)
 				workbench.add(shapelessToString(((ShapelessRecipes)raw).toBukkitRecipe()));
 		}
 		
 		Iterator recipes = CraftingManager.getInstance().getRecipes().iterator();
 		Object obj;
 		
 		while(recipes.hasNext())
 		{
 			obj = recipes.next();
 			
 			if(obj instanceof ShapedRecipes)
 			{
 				if(workbench.contains(shapedToString(((ShapedRecipes)obj).toBukkitRecipe())))
 					recipes.remove();
 			}
 			else if(obj instanceof ShapelessRecipes)
 			{
 				if(workbench.contains(shapelessToString(((ShapelessRecipes)obj).toBukkitRecipe())))
 					recipes.remove();
 			}
 		}
 	}
 	
 	/**
 	 * Re-adds vanilla Minecraft recipes to the game, preserving current recipes.<br>
 	 * NOTE: This is diferent from getServer().resetRecipes(), that removes everything else and leaves only the vanilla recipes.
 	 */
 	@SuppressWarnings("unchecked")
 	public void restoreDefaultRecipes()
 	{
 		Logger logger = Logger.getLogger("Minecraft");
 		Filter oldFilter = logger.getFilter();
 		
 		logger.setFilter(new Filter()
 		{
 			@Override
 			public boolean isLoggable(LogRecord log)
 			{
 				return !log.getMessage().endsWith(" recipes");
 			}
 		});
 		
 		RecipesFurnace.getInstance().recipes.putAll(new RecipesFurnace().recipes);
 		CraftingManager.getInstance().recipes.addAll(new CraftingManager().recipes);
 		
 		logger.setFilter(oldFilter);
 	}
 	
 	protected boolean loadRecipes(boolean simulation)
 	{
 		if(!simulation)
 		{
 			reset();
 			
 			switch(RecipeManager.settings.EXISTING_RECIPES)
 			{
 				case 'r':
 					removeDefaultRecipes();
 					break;
 				
 				case 'c':
 					Bukkit.clearRecipes();
 					break;
 			}
 		}
 		
 		File dir = new File(RecipeManager.plugin.getDataFolder() + File.separator + "recipes");
 		
 		if(!dir.exists())
 			dir.mkdirs();
 		
 		recipeErrors = new HashMap<String, List<String>>();
 		
 		loadDirectory(dir, simulation);
 		
 		boolean hasErrors = !recipeErrors.isEmpty();
 		
 		if(hasErrors)
 		{
 			StringBuilder errors = new StringBuilder();
 			
 			for(Entry<String, List<String>> entry : recipeErrors.entrySet())
 			{
 				errors.append("" + ChatColor.BOLD + ChatColor.BLUE + "File: " + entry.getKey() + NL);
 				
 				for(String error : entry.getValue())
 				{
 					errors.append(ChatColor.WHITE + error + NL);
 				}
 				
 				errors.append(NL);
 			}
 			
 			Messages.log(ChatColor.RED + "There were errors processing the files: " + NL + NL + errors + NL + NL);
 			
 			try
 			{
 				File file = new File(RecipeManager.plugin.getDataFolder() + File.separator + "last recipe errors.log");
 				
 				if(!file.exists())
 					file.createNewFile();
 				
 				BufferedWriter stream = new BufferedWriter(new FileWriter(file, false));
 				
 				stream.write(ChatColor.stripColor(errors.toString()));
 				
 				stream.close();
 				
 				Messages.log(ChatColor.YELLOW + "These errors have been saved in 'server.log' and '" + file.getPath() + "' as well.");
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		recipeErrors = null;
 		currentFile = null;
 		currentFileLine = 0;
 		
 		if(!simulation)
 		{
 			log.fine("Total loaded:");
 			log.fine("  " + craftRecipes.size() + " crafting recipes");
 			log.fine("  " + combineRecipes.size() + " combining recipes");
 			log.fine("  " + smeltRecipes.size() + " smelting recipes");
 			log.fine("  " + fuels.size() + " fuels");
 			
 			// restart furnace task if any custom time smelt recipes exist
 			
 			boolean cancelTask = !RecipeManager.settings.COMPATIBILITY_CHUNKEVENTS;
 			
 			if(!cancelTask)
 			{
 				if(smeltCustomRecipes)
 				{
 					if(furnaceSmelting == null)
 						furnaceSmelting = new HashMap<String, MutableDouble>();
 					
 					if(furnaceTaskId <= 0)
 						furnaceTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(RecipeManager.plugin, new FurnacesTask(RecipeManager.settings.FURNACE_TICKS), 0, RecipeManager.settings.FURNACE_TICKS);
 				}
 				else
 				{
 					cancelTask = true;
 					furnaceSmelting = null;
 				}
 			}
 			
 			if(cancelTask && furnaceTaskId > 0)
 			{
 				Bukkit.getScheduler().cancelTask(furnaceTaskId);
 				furnaceTaskId = -1;
 			}
 		}
 		
 		return !hasErrors;
 	}
 	
 	private void loadDirectory(File dir, boolean simulation)
 	{
 		log.fine("Loading '" + dir.getName() + "' folder...");
 		
 		String fileName;
 		
 		for(File file : dir.listFiles())
 		{
 			fileName = file.getName();
 			
 			if(file.isDirectory())
 			{
 				if(fileName.equalsIgnoreCase("disabled"))
 				{
 					log.fine("Skipped the 'disabled' folder");
 					continue;
 				}
 				
 				if(RecipeManager.settings.EXISTING_RECIPES == 'n' && fileName.equalsIgnoreCase("default"))
 				{
 					log.fine("Skipped the 'default' folder (due to config's 'existing-recipes' set to 'nothing')");
 					continue;
 				}
 				
 				loadDirectory(file, simulation);
 			}
 			else if(fileName.endsWith(".txt"))
 			{
 				try
 				{
 					loadDataFile(file, simulation);
 				}
 				catch(Exception e)
 				{
 					Messages.log(ChatColor.RED + "Failed to load '" + file.toString() + "', error:");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void loadDataFile(File file, boolean simulation) throws Exception
 	{
 		String fileName = file.getPath().replace(RecipeManager.plugin.getDataFolder().getPath() + "\\", "");
 		
 		log.fine("Loading '" + fileName + "' file...");
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
 		Recipe recipeData = new Recipe();
 		String[] error;
 		String line;
 		currentFile = fileName;
 		currentFileLine = -1; // one line behind to be more human friendly
 		
 		while((line = readLine(reader)) != null)
 		{
 			if((line = processLine(line, false)) == null)
 				continue;
 			
 			try
 			{
 				if((line = processRecipeData(line, reader, recipeData)) == null)
 					continue;
 				
 				error = null;
 				
 				if(line.equalsIgnoreCase("CRAFT"))
 					error = craftRecipe(line, reader, new Craft(recipeData), simulation);
 				else if(line.equalsIgnoreCase("COMBINE"))
 					error = combineRecipe(line, reader, new Combine(recipeData), simulation);
 				else if(line.equalsIgnoreCase("SMELT"))
 					error = smeltRecipe(line, reader, new Smelt(recipeData), simulation);
 				else if(line.equalsIgnoreCase("FUEL"))
 					error = fuelRecipe(line, reader, new Fuel(recipeData), simulation);
 				else
 					error = new String[]
 					{
 						"<yellow>This line was skipped: '" + line + "'",
 						"Possible causes: missing recipe type (CRAFT, SMELT, etc); it's part of a recipe and it has empty lines before it"
 					};
 				
 				if(error != null)
 					recipeError(error[0], (error.length > 1 ? error[1] : null));
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		if(currentFileLine == 0)
 			Messages.log("<yellow>Recipe file '" + fileName + "' is empty.");
 		
 		reader.close();
 	}
 	
 	private String readLine(BufferedReader reader) throws Exception
 	{
 		currentFileLine++; // to easily keep track of lines
 		
 		return reader.readLine();
 	}
 	
 	private String processLine(String string, boolean upperCase)
 	{
 		if(string == null || string.isEmpty())
 			return null;
 		
 		String comments[] =
 		{
 			"//",
 			"#"
 		};
 		
 		int index;
 		string = (upperCase ? string.trim().toUpperCase() : string.trim());
 		
 		for(String comm : comments)
 		{
 			index = string.indexOf(comm);
 			
 			if(index == 0)
 				return null;
 			
 			if(index > -1)
 				return string.substring(0, index).trim();
 		}
 		
 		return string;
 	}
 	
 	private void recipeError(String error)
 	{
 		recipeError(error, null);
 	}
 	
 	private void recipeError(String error, String tip)
 	{
 		List<String> errors = recipeErrors.get(currentFile);
 		
 		if(errors == null)
 			errors = new ArrayList<String>();
 		
 		errors.add("line " + String.format("%-5d", currentFileLine) + ChatColor.WHITE + error + (tip != null ? NL + ChatColor.DARK_GREEN + "          TIP: " + ChatColor.GRAY + tip : ""));
 		
 		recipeErrors.put(currentFile, errors);
 	}
 	
 	protected ItemData processItemData(String string, int defaultData, boolean allowData, boolean printRecipeErrors)
 	{
 		Item item = processItem(string, defaultData, allowData, false, false, printRecipeErrors);
 		
 		return (item == null ? null : new ItemData(item));
 	}
 	
 	protected Item processItem(String string, int defaultData, boolean allowData, boolean allowAmount, boolean allowEnchantments, boolean printRecipeErrors)
 	{
 		string = string.trim();
 		
 		if(string.length() == 0)
 			return null;
 		
 		String itemString[] = string.split("\\|");
 		String stringArray[] = itemString[0].trim().split(":");
 		
 		if(stringArray.length <= 0 || stringArray[0].isEmpty())
 			return new Item(0);
 		
 		stringArray[0] = stringArray[0].trim();
 		String alias = RecipeManager.plugin.getAliases().get(stringArray[0]);
 		
 		if(alias != null)
 		{
 			if(stringArray.length > 2 && printRecipeErrors)
 				recipeError("'" + stringArray[0] + "' is an alias with data and amount.", "You can only set amount e.g.: alias:amount.");
 			
 			return processItem(string.replace(stringArray[0], alias), defaultData, allowData, allowAmount, allowEnchantments, printRecipeErrors);
 		}
 		
 		Material mat = Material.matchMaterial(stringArray[0]);
 		
 		if(mat == null)
 		{
 			if(printRecipeErrors)
 				recipeError("Item '" + stringArray[0] + "' does not exist!", "Name could be different, look in readme.txt for links");
 			
 			return null;
 		}
 		
 		int type = mat.getId();
 		
 		if(type <= 0)
 			return new Item(0);
 		
 		int data = defaultData;
 		
 		if(stringArray.length > 1)
 		{
 			if(allowData)
 			{
 				try
 				{
 					stringArray[1] = stringArray[1].trim();
 					
 					if(stringArray[1].charAt(0) != '*')
 						data = Math.max(Integer.valueOf(stringArray[1]), data);
 				}
 				catch(Exception e)
 				{
 					if(printRecipeErrors)
 						recipeError("Item '" + mat + " has data value that is not a number: '" + stringArray[1] + "', defaulting to " + defaultData);
 				}
 			}
 			else if(printRecipeErrors)
 				recipeError("Item '" + mat + "' can't have data value defined in this recipe's slot, data value ignored.");
 		}
 		
 		int amount = 1;
 		
 		if(stringArray.length > 2)
 		{
 			if(allowAmount)
 			{
 				try
 				{
 					amount = Math.max(Integer.valueOf(stringArray[2].trim()), 1);
 				}
 				catch(Exception e)
 				{
 					if(printRecipeErrors)
 						recipeError("Item '" + mat + "' has amount value that is not a number: " + stringArray[2] + ", defaulting to 1");
 				}
 			}
 			else if(printRecipeErrors)
 				recipeError("Item '" + mat + "' can't have amount defined in this recipe's slot, amount ignored.");
 		}
 		
 		Item item = new Item(type, amount, (short)data);
 		
 		if(itemString.length > 1)
 		{
 			if(allowEnchantments)
 			{
 				if(item.getAmount() > 1)
 				{
 					if(printRecipeErrors)
 						recipeError("Item '" + mat + "' has enchantments and more than 1 amount, it can't have both, amount set to 1.");
 					
 					item.setAmount(1);
 				}
 				
 				String[] enchants = itemString[1].split(",");
 				String[] enchData;
 				Enchantment ench;
 				int level;
 				
 				for(String enchant : enchants)
 				{
 					enchant = enchant.trim();
 					enchData = enchant.split(":");
 					
 					if(enchData.length != 2)
 					{
 						if(printRecipeErrors)
 							recipeError("Enchantments have to be 'ENCHANTMENT:LEVEL' format.", "Look in readme.txt for enchantment list link.");
 						
 						continue;
 					}
 					
 					ench = Enchantment.getByName(enchData[0]);
 					
 					if(ench == null)
 					{
 						try
 						{
 							ench = Enchantment.getById(Integer.valueOf(enchData[0]));
 						}
 						catch(Exception e)
 						{
 							ench = null;
 						}
 						
 						if(ench == null)
 						{
 							if(printRecipeErrors)
 								recipeError("Enchantment '" + enchData[0] + "' does not exist!", "Name or ID could be different, look in readme.txt for enchantments list links.");
 							
 							continue;
 						}
 					}
 					
 					if(enchData[1].equals("MAX"))
 						level = ench.getMaxLevel();
 					
 					else
 					{
 						try
 						{
 							level = Integer.valueOf(enchData[1]);
 						}
 						catch(Exception e)
 						{
 							if(printRecipeErrors)
 								recipeError("Invalid enchantment level: '" + enchData[1] + "' must be a valid number, positive, zero or negative.");
 							
 							continue;
 						}
 					}
 					
 					item.addEnchantment(ench, level);
 				}
 			}
 			else if(printRecipeErrors)
 				recipeError("Item '" + mat + "' can't use enchantments in this recipe slot!");
 		}
 		
 		return item;
 	}
 	
 	private String[] craftRecipe(String line, BufferedReader reader, Craft recipe, boolean simulation) throws Exception
 	{
 		line = processLine(readLine(reader), false); // skip recipe header, read next line
 		line = processRecipeData(line, reader, recipe); // check for @flags
 		
 		if(line == null)
 			return new String[]
 			{
 				"Recipe has no ingredients !"
 			};
 		
 		HashMap<ItemData, Character> itemChars = new HashMap<ItemData, Character>();
 		List<String> recipeShape = new ArrayList<String>();
 		ItemData[] ingredients = new ItemData[9];
 //		Item[] leftovers = new Item[9];
 		
 		String split[];
 //		String itemSplit[];
 		String resultRaw = null;
 		ItemData itemData;
 		Character c;
 		int charIndex = 97;
 		int itemIndex = 0;
 		int rows = 0;
 		int largest = 0;
 		boolean errors = false;
 		
 		while(rows < 3)
 		{
 			if(rows > 0)
 				line = processLine(readLine(reader), true);
 			
 			if(line == null)
 				continue;
 			
 			// if we bump into the result prematurely (smaller recipes)
 			
 			if(line.charAt(0) == '=')
 			{
 				resultRaw = line;
 				break;
 			}
 			
 			split = line.split("\\+");
 			
 			int len = Math.min(split.length, 3); // max 3 items per row !
 			StringBuilder recipeChars = new StringBuilder();
 			
 			itemIndex = rows * 3;
 			largest = Math.max(len, largest);
 			
 			for(int i = 0; i < len; i++)
 			{
 				// splitting line by ">" char to get leftover item (if any)
 //				itemSplit = split[i].split(">");
 				itemData = processItemData(split[i] /*itemSplit[0]*/, -1, true, true);
 				
 				if(itemData == null)
 				{
 					errors = true;
 					continue;
 				}
 				
 				if(itemData.getType() > 0)
 				{
 					Character ic = itemChars.get(itemData);
 					
 					if(ic == null)
 					{
 						c = (char)charIndex++;
 						
 						itemChars.put(itemData, c);
 					}
 					else
 						c = ic;
 					
 					ingredients[itemIndex] = itemData; // add non-AIR item
 				}
 				else
 					c = ' ';
 				
 				recipeChars.append(c);
 				
 				// get leftover item (if any)
 				/*
 				if(itemData.type != 0 && itemSplit.length > 1)
 					leftovers[itemIndex] = Main.processItem(itemSplit[1].trim(), 0, true, true);
 				*/
 				
 				itemIndex++;
 			}
 			
 			recipeShape.add(recipeChars.toString());
 			rows++;
 		}
 		
 		if(rows == 0)
 			return new String[]
 			{
 				"Recipe doesn't have ingredients !",
 				"Consult readme.txt for proper recipe syntax."
 			};
 		
 		// If result wasn't found yet, it's got to be the 4th line
 		
 		if(resultRaw == null)
 			resultRaw = processLine(readLine(reader), true);
 		
 		List<Item> results = getResults(resultRaw, reader, false, false);
 		
 		if(results == null)
 			return null;
 		
 		if(errors)
 			return new String[]
 			{
 				"Recipe has some invalid ingredients, fix them!"
 			};
 		
 		// starting the recipe and setting it's result
 		
 		ShapedRecipe bukkitRecipe = new ShapedRecipe(new ItemStack(placeholderItem.getTypeId(), craftNum, placeholderItem.getDurability()));
 		String[] chars = new String[recipeShape.size()];
 		int i = 0;
 		
 		for(String shapeRow : recipeShape)
 		{
 			chars[i++] = (shapeRow.length() < largest ? (shapeRow + "    ").substring(0, largest) : shapeRow);
 		}
 		
 		bukkitRecipe.shape(chars);
 		
 		for(Entry<ItemData, Character> entry : itemChars.entrySet())
 		{
 			bukkitRecipe.setIngredient(entry.getValue(), entry.getKey().getMaterial(), entry.getKey().getData());
 		}
 		
 		if(simulation)
 			return null;
 		
 		String ingredientsString = ingredientsToString(ingredients);
 		
 		for(Craft r : craftRecipes)
 		{
 			if(ingredientsString.equals(ingredientsToString(r.getIngredients())))
 				return new String[]
 				{
 					"Recipe already defined in one of your recipe files.",
 					(recipe.getOverride() ? "You can't override recipes that are already handled by this plugin because you can simply edit them!" : null)
 				};
 		}
 		
 		Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.getServer().recipeIterator();
 		org.bukkit.inventory.Recipe rec;
 		ShapedRecipe r;
 		int width = chars[0].length();
 		int height = chars.length;
 		boolean override = recipe.getOverride();
 		boolean exists = false;
 		
 		while(recipes.hasNext())
 		{
 			rec = recipes.next();
 			
 			if(rec == null || !(rec instanceof ShapedRecipe) || isCustomRecipe(rec.getResult()))
 				continue;
 			
 			r = (ShapedRecipe)rec;
 			
 			if(height != r.getShape().length || width != r.getShape()[0].length())
 				continue;
 			
 			if(compareCraftRecipe(ingredients, r))
 			{
 				exists = true;
 				
 				if(override)
 				{
 					recipes.remove();
 					overriddenRecipes.add(ingredientsString);
 				}
 			}
 		}
 		
 		if(override)
 		{
 			if(!exists && !overriddenRecipes.contains(ingredientsString))
 				recipeError("The @override flag couldn't find the original recipe to override, added new recipe instead.", "Maybe shape is diferent, mirrored perhaps ?");
 		}
 		else
 		{
 			if(exists)
 				return new String[]
 				{
 					"Recipe already exists! It's either vanilla or added by another plugin/mod.",
 					"Add @override flag to the recipe to supercede it."
 				};
 		}
 		
 		// finally add the recipe to the server!
 		
 		if(!Bukkit.addRecipe(bukkitRecipe))
 			return new String[]
 			{
 				"Couldn't add recipe to server, unknown error!"
 			};
 		
 		recipe.setIngredients(ingredients);
 		recipe.setResults(results);
 		
 		craftRecipes.add(craftNum, recipe);
 		craftNum++;
 		
 		return null;
 	}
 	
 	private String ingredientsToString(ItemData[] ingredients)
 	{
 		StringBuilder str = new StringBuilder("craft");
 		
 		for(ItemData ingredient : ingredients)
 		{
 			str.append(',').append(ingredient == null ? "" : ingredient.convertString());
 		}
 		
 		return str.toString();
 	}
 	
 	private String ingredientsToString(List<Item> ingredients)
 	{
 		StringBuilder str = new StringBuilder("combine");
 		
 		for(ItemData ingredient : ingredients)
 		{
 			str.append(',').append(ingredient == null ? "" : ingredient.convertString());
 		}
 		
 		return str.toString();
 	}
 	
 	private boolean compareCraftRecipe(ItemData[] ingredients, ShapedRecipe recipe)
 	{
 		ItemStack[] matrix = new ItemStack[9];
 		Map<Character, ItemStack> items = recipe.getIngredientMap();
 		String[] shape = recipe.getShape();
 		int slot = 0;
 		
 		for(int i = 0; i < shape.length; i++)
 		{
 			for(char col : shape[i].toCharArray())
 			{
 				matrix[slot] = items.get(col);
 				slot++;
 			}
 			
 			slot = ((i + 1) * 3);
 		}
 		
 		for(int i = 0; i < 9; i++)
 		{
 			if(matrix[i] == null && ingredients[i] == null)
 				continue;
 			
 			if(matrix[i] == null || ingredients[i] == null || ingredients[i].getType() != matrix[i].getTypeId() || (ingredients[i].getData() != -1 && ingredients[i].getData() != matrix[i].getDurability()))
 				return false;
 		}
 		
 		return true;
 	}
 	
 	private String[] combineRecipe(String line, BufferedReader reader, Combine recipe, boolean simulation) throws Exception
 	{
 		line = processLine(readLine(reader), false); // skip recipe header, read next line
 		line = processRecipeData(line, reader, recipe); // check for @flags
 		
 		if(line == null)
 			return new String[]
 			{
 				"Recipe has no ingredients !"
 			};
 		
 		String[] ingredientsRaw = line.split("\\+");
 		
 		List<Item> results = getResults(processLine(readLine(reader), true), reader, false, false);
 		
 		if(results == null)
 			return null;
 		
 		ShapelessRecipe bukkitRecipe = new ShapelessRecipe(new ItemStack(placeholderItem.getTypeId(), combineNum, placeholderItem.getDurability()));
 		List<Item> ingredients = new ArrayList<Item>();
 		Item item;
 		int items = 0;
 		
 		for(String itemRaw : ingredientsRaw)
 		{
 			item = processItem(itemRaw, -1, true, true, false, true);
 			
 			if(item == null)
 				return new String[]
 				{
 					"Recipe has some invalid ingredients, fix them!"
 				};
 			
 			if((items += item.getAmount()) > 9)
 				return new String[]
 				{
 					"Combine recipes can't have more than 9 ingredients !"
 				};
 			
 			ingredients.add(item);
 			bukkitRecipe.addIngredient(item.getAmount(), item.getMaterial(), item.getData());
 		}
 		
 		if(simulation)
 			return null;
 		
 		for(Combine r : combineRecipes)
 		{
 			if(compareCombineIngredients(r.getIngredients(), bukkitRecipe.getIngredientList()))
 				return new String[]
 				{
 					"Recipe already defined in one of your recipe files.",
 					(recipe.getOverride() ? "You can't override recipes that are already handled by this plugin because you can simply edit them!" : null)
 				};
 		}
 		
 		Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.getServer().recipeIterator();
 		org.bukkit.inventory.Recipe rec;
 		ShapelessRecipe r;
 		String ingredientsString = ingredientsToString(ingredients);
 		boolean override = recipe.getOverride();
 		boolean exists = false;
 		
 		while(recipes.hasNext())
 		{
 			rec = recipes.next();
 			
 			if(rec == null || !(rec instanceof ShapelessRecipe) || isCustomRecipe(rec.getResult()))
 				continue;
 			
 			r = (ShapelessRecipe)rec;
 			
 			if(compareCombineIngredients(ingredients, r.getIngredientList()))
 			{
 				exists = true;
 				
 				if(override)
 				{
 					recipes.remove();
 					overriddenRecipes.add(ingredientsString);
 				}
 			}
 		}
 		
 		if(override)
 		{
 			if(!exists && !overriddenRecipes.contains(ingredientsString))
 				recipeError("The @override flag couldn't find the original recipe to override, added new recipe instead.");
 		}
 		else
 		{
 			if(exists)
 				return new String[]
 				{
 					"Recipe already exists! It's either vanilla or added by another plugin/mod.",
 					"Add @override flag to the recipe to supercede it."
 				};
 		}
 		
 		if(!Bukkit.addRecipe(bukkitRecipe))
 			return new String[]
 			{
 				"Couldn't add recipe to server, unknown error"
 			};
 		
 		recipe.setIngredients(ingredients);
 		recipe.setResults(results);
 		
 		combineRecipes.add(combineNum, recipe);
 		combineNum++;
 		
 		return null;
 	}
 	
 	private boolean compareCombineIngredients(List<Item> ingredients, List<ItemStack> recipeIngredients)
 	{
 		int size = ingredients.size();
 		
 		if(size != recipeIngredients.size())
 			return false;
 		
 		HashSet<Integer> compared = new HashSet<Integer>();
 		boolean found;
 		
 		for(Item item : ingredients)
 		{
 			found = false;
 			
 			for(int i = 0; i < size; i++)
 			{
 				if(compared.contains(i))
 					continue;
 				
 				if(item.compareItemStack(recipeIngredients.get(i)))
 				{
 					compared.add(i);
 					found = true;
 					break;
 				}
 			}
 			
 			if(!found)
 				return false;
 		}
 		
 		return (compared.size() == size);
 	}
 	
 	private String[] smeltRecipe(String line, BufferedReader reader, Smelt recipe, boolean simulation) throws Exception
 	{
 		line = processLine(readLine(reader), false); // skip recipe header, read next line
 		line = processRecipeData(line, reader, recipe); // check for @flags
 		
 		if(line == null)
 			return new String[]
 			{
 				"Recipe doesn't have an ingredient !"
 			};
 		
 		String[] split = line.split("%");
 		
 		if(split.length == 0)
 			return new String[]
 			{
 				"Recipe doesn't have an ingredient !"
 			};
 		
 		List<Item> results = getResults(processLine(readLine(reader), true), reader, true, true);
 		
 		if(results == null)
 			return null;
 		
 		if(results.size() > 1)
 			recipeError("Can't have more than 1 result in this recipe, the rest were ignored.");
 		
 		Item result = results.get(0);
 		
 		// TODO: revert allowData to true when data values for furnaces have been fixed
 		ItemData ingredient = processItemData(split[0], -1, false, true);
 		
 		if(ingredient == null)
 			return new String[]
 			{
 				"Invalid ingredient '" + split[0] + "'.",
 				"Name could be diferent, look in readme.txt for links."
 			};
 		
 		double minTime = -1.0;
 		double maxTime = -1.0;
 		
 		if(split.length >= 2)
 		{
 			String[] timeSplit = split[1].trim().split("-");
 			
 			if(!timeSplit[0].equals("INSTANT"))
 			{
 				minTime = Double.valueOf(timeSplit[0]);
 				
 				if(timeSplit.length >= 2)
 					maxTime = Double.valueOf(timeSplit[1]);
 			}
 			else
 				minTime = 0.0;
 			
 			if(maxTime > -1.0 && minTime >= maxTime)
 				return new String[]
 				{
 					"Smelting recipe has minimum time less or equal to maximum time!",
 					"Use a single number if you want a fixed value."
 				};
 		}
 		
 		// the recipe
 		
 		if(simulation)
 			return null;
 		
 		if(smeltRecipes.containsKey(ingredient.getType() + (ingredient.getData() == -1 ? "" : ":" + ingredient.getData())))
 			return new String[]
 			{
 				"Recipe for '" + ingredient.printItemData() + "' is already defined in one of your recipe files.",
 				(recipe.getOverride() ? "You can't override recipes that are already handled by this plugin because you can simply edit them!" : null)
 			};
 		
 		Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.getServer().recipeIterator();
 		org.bukkit.inventory.Recipe rec;
 		FurnaceRecipe r;
 		String ingredientsString = "smelt," + ingredient.convertString();
 		boolean override = recipe.getOverride();
 		boolean exists = false;
 		
 		while(recipes.hasNext())
 		{
 			rec = recipes.next();
 			
 			if(rec == null || !(rec instanceof FurnaceRecipe) || isCustomRecipe(rec.getResult()))
 				continue;
 			
 			r = (FurnaceRecipe)rec;
 			
 			if(ingredient.compareItemStack(r.getInput()))
 			{
 				exists = true;
 				
 				if(override)
 				{
 					recipes.remove();
 					overriddenRecipes.add(ingredientsString);
 				}
 			}
 		}
 		
 		if(override)
 		{
 			if(!exists && !overriddenRecipes.contains(ingredientsString))
 				recipeError("The @override flag couldn't find the original recipe to override, added new recipe instead.");
 		}
 		else
 		{
 			if(exists)
 				return new String[]
 				{
 					"Recipe for '" + ingredient.printItemData() + "' already exists! It's either vanilla or added by another plugin/mod.",
 					"Add @override flag to the recipe to supercede it."
 				};
 		}
 		
 		if(!Bukkit.addRecipe(new FurnaceRecipe((result.getType() == 0 ? new ItemStack(placeholderItem.getTypeId()) : result.getItemStack()), ingredient.getMaterial(), ingredient.getData())))
 			return new String[]
 			{
 				"Couldn't add recipe to server, unknown error"
 			};
 		
 		recipe.setIngredient(ingredient);
 		recipe.setResult(result);
 		recipe.setMinTime(minTime);
 		recipe.setMaxTime(maxTime);
 		
 		smeltRecipes.put(ingredient.convertString(), recipe);
 		
 		if(minTime >= 0.0)
 			smeltCustomRecipes = true;
 		
 		return null;
 	}
 	
 	private String[] fuelRecipe(String line, BufferedReader reader, Fuel recipe, boolean simulation) throws Exception
 	{
 		line = processLine(readLine(reader), false); // skip recipe header, read next line
 		line = processRecipeData(line, reader, recipe); // check for @flags
 		
 		if(line == null)
 			return new String[]
 			{
 				"Recipe doesn't have an ingredient !"
 			};
 		
 		String[] split = line.split("%");
 		
 		if(split[1] == null)
 			return new String[]
 			{
 				"Burn time not set !",
 				"It must be set after the ingredient like: ingredient % burntime"
 			};
 		
 		String[] timeSplit = split[1].trim().split("-");
 		int minTime = Math.max(Integer.valueOf(timeSplit[0]), 1);
 		int maxTime = -1;
 		
 		if(timeSplit.length >= 2)
 			maxTime = Math.max(Integer.valueOf(timeSplit[1]), maxTime);
 		
 		ItemData ingredient = processItemData(split[0], -1, true, true);
 		
 		if(ingredient == null || ingredient.getType() == 0)
 			return new String[]
 			{
 				"Invalid item: '" + ingredient + "'"
 			};
 		
 		if(minTime <= 0)
 			return new String[]
 			{
 				"Fuel " + ingredient + " can't burn for negative or zero seconds!"
 			};
 		
 		if(maxTime > -1 && minTime >= maxTime)
 			return new String[]
 			{
 				"Fuel " + ingredient + " has minimum time less or equal to maximum time!",
 				"Use a single number if you want a fixed value"
 			};
 		
 		if(simulation)
 			return null;
 		
 		String ingredientString = ingredient.convertString();
 		
 		if(fuels.containsKey(ingredientString))
 			return new String[]
 			{
 				"Fuel " + ingredient.getMaterial() + ":" + ingredient.getData() + " already exists!",
 				"Search the recipe files from this plugin, it can't be from other plugins or mods."
 			};
 		
 		recipe.setFuel(ingredient);
 		recipe.setMinTime(minTime);
 		recipe.setMaxTime(maxTime);
 		
 		fuels.put(ingredientString, recipe);
 		
 		return null;
 	}
 	
 	private List<Item> getResults(String line, BufferedReader reader, boolean allowAir, boolean oneResult) throws Exception
 	{
 		List<Item> results = new ArrayList<Item>();
 		Item noPercentItem = null;
 		String[] resultRawSplit;
 		Item item;
 		int totalpercentage = 0;
 		
 		while(line != null && !line.isEmpty() && line.charAt(0) == '=')
 		{
 			resultRawSplit = line.substring(1).trim().split("%"); // remove the = character and see if it's got percentage
 			
 			if(resultRawSplit.length >= 2)
 			{
 				item = processItem(resultRawSplit[1].trim(), 0, true, true, true, true);
 				
 				if(item == null || (!allowAir && item.getType() == 0))
 				{
 					recipeError("Invalid result !");
 					return null;
 				}
 				
 				try
 				{
 					item.setChance(Math.min(Math.max(Integer.valueOf(resultRawSplit[0].trim()), 0), 100));
 				}
 				catch(Exception e)
 				{
 					recipeError("Invalid percentage number: " + resultRawSplit[0]);
 					return null;
 				}
 				
 				if((totalpercentage += item.getChance()) > 100)
 				{
 					recipeError("Total result items' chance exceeds 100% !", "Not defining percentage for one item will make its chance fit with the rest until 100%");
 					return null;
 				}
 				
 				results.add(item);
 			}
 			else
 			{
 				if(resultRawSplit[0] == null)
 				{
 					recipeError("Missing result !");
 					return null;
 				}
 				
 				if(noPercentItem != null)
 				{
 					recipeError("Can't have more than 1 item without procentage to fill the rest!");
 					return null;
 				}
 				
 				noPercentItem = processItem(resultRawSplit[0].trim(), 0, true, true, true, true);
 				
 				if(noPercentItem == null || (!allowAir && noPercentItem.getType() == 0))
 				{
 					recipeError("Invalid result !");
 					return null;
 				}
 			}
 			
 			line = processLine(readLine(reader), true);
 		}
 		
 		if(noPercentItem != null)
 		{
 			noPercentItem.setChance(100 - totalpercentage);
 			results.add(noPercentItem);
 		}
 		else if(results.isEmpty())
 		{
 			recipeError("Missing result !");
 			return null;
 		}
 		else if(!oneResult && totalpercentage < 100)
 			results.add(new Item(0, 0, (short)0, (100 - totalpercentage)));
 		
 		return results;
 	}
 	
 	protected void reset()
 	{
 		removeCustomRecipes();
 		
 		if(RecipeManager.settings.EXISTING_RECIPES != 'c')
 		{
 			removeDefaultRecipes();
 			restoreDefaultRecipes();
 		}
 		
 		craftRecipes.clear();
 		combineRecipes.clear();
 		smeltRecipes.clear();
 		fuels.clear();
 		
 		craftNum = 0;
 		combineNum = 0;
 		smeltCustomRecipes = false;
 		hasExplosive = false;
 	}
 	
 	private String processRecipeData(String line, BufferedReader reader, Recipe recipeData) throws Exception
 	{
 		while(line != null && line.charAt(0) == '@')
 		{
 			recipeFlags(line, recipeData);
 			line = processLine(readLine(reader), false);
 		}
 		
 		return (line == null ? null : line.toUpperCase());
 	}
 	
 	private void recipeFlags(String line, Recipe recipe)
 	{
 		String[] split = line.split(":", 2);
 		String flag = split[0].substring(1).trim();
 		
 		if(flag.equalsIgnoreCase("log"))
 		{
 			if(split.length > 1)
 			{
 				try
 				{
 					recipe.setLog(split[1].trim().equalsIgnoreCase("true"));
 				}
 				catch(Exception e)
 				{
 					recipeError("@" + flag + " only accepts true or false! Set to true.");
 					recipe.setLog(true);
 				}
 			}
 			else
 				recipe.setLog(true);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("override"))
 		{
 			if(recipe instanceof Fuel)
 			{
 				recipeError("@" + flag + " doesn't do anything for FUEL recipes, ignored.");
 				return;
 			}
 			
 			if(split.length > 1)
 			{
 				try
 				{
 					recipe.setOverride(split[1].trim().equalsIgnoreCase("true"));
 				}
 				catch(Exception e)
 				{
 					recipeError("@" + flag + " only accepts true or false! Set to true.");
 					recipe.setOverride(true);
 				}
 			}
 			else
 				recipe.setOverride(true);
 			
 			return;
 		}
 		
 		if(split.length < 2)
 		{
 			recipeError("@" + flag + " doesn't have any values!");
 			return;
 		}
 		
 		String value = split[1].trim();
 		
 		if(flag.equalsIgnoreCase("failmessage"))
 		{
 			recipe.setFailMessage(value);
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("permission"))
 		{
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setPermission(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			
 			if(split.length > 1)
 				failMessage = split[1];
 			
 			split = value.split("=");
 			String perm = split[0].trim();
 			
 			if(perm.length() < 3 || !perm.matches("[a-zA-Z0-9.]+"))
 			{
 				recipeError("@" + flag + " has invalid value: '" + perm + "', minimum 3 chars, numbers, letters and dots only!");
 				return;
 			}
 			
 			Permission p = Bukkit.getPluginManager().getPermission(perm);
 			boolean exists = (p != null);
 			
 			if(!exists)
 			{
 				p = new Permission(perm);
 				p.setDescription("RecipeManager's recipes");
 			}
 			
 			if(split.length > 1)
 			{
 				String defValue = split[1].trim();
 				
 				switch(defValue.charAt(0))
 				{
 					case 'T':
 					case 't':
 					{
 						p.setDefault(PermissionDefault.TRUE);
 						break;
 					}
 					
 					case 'F':
 					case 'f':
 					{
 						p.setDefault(PermissionDefault.FALSE);
 						break;
 					}
 					
 					case 'O':
 					case 'o':
 					{
 						p.setDefault(PermissionDefault.OP);
 						break;
 					}
 					
 					case 'N':
 					case 'n':
 					{
 						p.setDefault(PermissionDefault.NOT_OP);
 						break;
 					}
 					
 					default:
 					{
 						recipeError("@" + flag + " has invalid default value '" + defValue + "' for permission node '" + perm + "'", "Valid values: true, false, op, non-op");
 						return;
 					}
 				}
 				
 				log.fine("Set permission " + perm + "'s default value to " + p.getDefault().toString());
 			}
 			
 			recipe.setPermission(new Flag<String>(p.getName(), failMessage));
 			
 			if(!exists)
 				Bukkit.getPluginManager().addPermission(p);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("groups"))
 		{
 			if(!RecipeManager.permissions.isEnabled())
 			{
 				recipeError("@" + flag + " can't work without Vault and a permission plugin that supports groups, ignored.");
 				return;
 			}
 			
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setGroups(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			
 			if(split.length > 1)
 				failMessage = split[1];
 			
 			recipe.setGroups(new Flag<Set<String>>(new HashSet<String>(), failMessage));
 			split = value.split(",");
 			
 			for(String group : split)
 			{
 				group = group.trim();
 				
 				if(group.isEmpty())
 				{
 					recipeError("@" + flag + " has a group that's invalid: '" + group + "'");
 					continue;
 				}
 				
 				recipe.getGroups().getValue().add(group);
 			}
 			
 			if(recipe.getGroups().getValue().isEmpty())
 				recipe.setGroups(null);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("anygroup"))
 		{
 			if(!RecipeManager.permissions.isEnabled())
 			{
 				recipeError("@" + flag + " can't work without Vault and a permission plugin that supports groups, ignored.");
 				return;
 			}
 			
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setAnyGroup(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			
 			if(split.length > 1)
 				failMessage = split[1];
 			
 			recipe.setAnyGroup(new Flag<Set<String>>(new HashSet<String>(), failMessage));
 			split = value.split(",");
 			
 			for(String group : split)
 			{
 				group = group.trim();
 				
 				if(group.isEmpty())
 				{
 					recipeError("@" + flag + " has a group that's invalid: '" + group + "'");
 					continue;
 				}
 				
 				recipe.getAnyGroup().getValue().add(group);
 			}
 			
 			if(recipe.getAnyGroup().getValue().isEmpty())
 				recipe.setAnyGroup(null);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("worlds"))
 		{
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setWorlds(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			
 			if(split.length > 1)
 				failMessage = split[1];
 			
 			recipe.setWorlds(new Flag<Set<String>>(new HashSet<String>(), failMessage));
 			split = value.split(",");
 			
 			for(String world : split)
 			{
 				world = world.trim();
 				
 				if(world.isEmpty() || Bukkit.getWorld(world) == null)
 				{
 					recipeError("@" + flag + " has a world that's invalid: '" + world + "'");
 					continue;
 				}
 				
 				recipe.getWorlds().getValue().add(world);
 			}
 			
 			if(recipe.getWorlds().getValue().isEmpty())
 				recipe.setWorlds(null);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("proximity"))
 		{
 			if(recipe instanceof Craft || recipe instanceof Combine)
 			{
 				recipeError("@" + flag + " doesn't do anything for CRAFT or COMBINE recipes, ignored.");
 				return;
 			}
 			
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setProximity(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			String successMessage = null;
 			
 			if(split.length > 1)
 			{
 				failMessage = split[1].trim();
 				
 				if(split.length > 2)
 					successMessage = split[2].trim();
 			}
 			
 			try
 			{
 				recipe.setProximity(new Flag<Integer>((value.equalsIgnoreCase("online") ? -1 : Integer.valueOf(value)), failMessage, successMessage));
 			}
 			catch(Exception e)
 			{
 				recipeError("@" + flag + " has invalid number value!");
 				return;
 			}
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("explode"))
 		{
 			if(value.equalsIgnoreCase("false"))
 			{
 				recipe.setExplode(null);
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String message = null;
 			
 			if(split.length > 1)
 				message = split[1].trim();
 			
 			split = value.split(" ");
 			
 			if(split.length < 3)
 			{
 				recipeError("@" + flag + " doesn't have enough arguments! Read readme.txt for requirements.");
 				return;
 			}
 			
 			try
 			{
 				char when = Character.toLowerCase(Character.valueOf(split[0].trim().charAt(0)));
 				
 				if(when == 'f' && recipe instanceof Fuel)
 				{
 					recipeError("@" + flag + " can't be set to 'fail' for FUEL recipes because they never fail, ignored.");
 					return;
 				}
 				
 				recipe.setExplode(new Flag<int[]>(new int[]
 				{
 					when,
 					Math.min(Math.max(Integer.valueOf(split[1].trim()), 1), 100),
 					Math.max(Integer.valueOf(split[2].trim()), 0),
 					(split.length > 3 && Boolean.valueOf(split[3].trim()) ? 1 : 0),
 				}, message));
 				
 				hasExplosive = true;
 			}
 			catch(Exception e)
 			{
 				recipeError("@" + flag + " has one or more invalid arguments! Read readme.txt for requirements.");
 				return;
 			}
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("command"))
 		{
 			if(!value.equalsIgnoreCase("false"))
 			{
 				if(recipe.getCommands() == null)
 					recipe.setCommands(new HashSet<String>());
 				
 				recipe.getCommands().add(value);
 			}
 			else
 				recipe.setCommands(null);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("message"))
 		{
 			if(!value.equalsIgnoreCase("false"))
 			{
 				if(recipe.getMessages() == null)
 					recipe.setMessages(new HashSet<String>());
 				
 				recipe.getMessages().add(value);
 			}
 			else
 				recipe.setMessages(null);
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("givexp") || flag.equalsIgnoreCase("giveexp") || flag.equalsIgnoreCase("givelevel") || flag.equalsIgnoreCase("givemoney"))
 		{
			char type = flag.charAt(4);
 			
 			if(type == 'm' && !RecipeManager.economy.isEnabled())
 			{
 				recipeError("@" + flag + " can't work without Vault and an economy plugin, ignored.");
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			String successMessage = null;
 			Double val = null;
 			
 			if(!value.equalsIgnoreCase("false"))
 			{
 				if(split.length > 1)
 				{
 					failMessage = split[1].trim();
 					
 					if(split.length > 2)
 						successMessage = split[2].trim();
 				}
 				
 				try
 				{
 					val = (type == 'm' ? Double.valueOf(value) : Integer.valueOf(value));
 				}
 				catch(Exception e)
 				{
 					recipeError("@" + flag + " has invalid number value!");
 					return;
 				}
 				
 				if(val == 0)
 					val = null;
 			}
 			
 			switch(type)
 			{
 				case 'e': // give[e]xp
 				case 'x': // give[x]p
 				{
 					recipe.setGiveExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage, successMessage));
 					break;
 				}
 				
 				case 'l': // give[l]evel
 				{
 					recipe.setGiveLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage, successMessage));
 					break;
 				}
 				
 				case 'm': // give[m]oney
 				{
 					recipe.setGiveMoney(val == null ? null : new Flag<Double>(val, failMessage, successMessage));
 					break;
 				}
 			}
 			
 			return;
 		}
 		
 		if(flag.equalsIgnoreCase("minxp") || flag.equalsIgnoreCase("minexp") || flag.equalsIgnoreCase("maxxp") || flag.equalsIgnoreCase("maxexp") || flag.equalsIgnoreCase("minlevel") || flag.equalsIgnoreCase("maxlevel") || flag.equalsIgnoreCase("minmoney") || flag.equalsIgnoreCase("maxmoney"))
 		{
			char type = flag.charAt(3);
 			
 			if(type == 'm' && !RecipeManager.economy.isEnabled())
 			{
 				recipeError("@" + flag + " can't work without Vault and an economy plugin, ignored.");
 				return;
 			}
 			
 			split = value.split("\\|");
 			value = split[0].trim();
 			String failMessage = null;
 			Double val = null;
 			
 			if(!value.equalsIgnoreCase("false"))
 			{
 				if(split.length > 1)
 					failMessage = split[1].trim();
 				
 				try
 				{
 					val = (type == 'm' ? Double.valueOf(value) : Integer.valueOf(value));
 				}
 				catch(Exception e)
 				{
 					recipeError("@" + flag + " has invalid number value!");
 					return;
 				}
 				
 				if(val <= 0)
 					val = null;
 			}
 			
 			switch(type)
 			{
 				case 'e': // min/max[e]xp
 				case 'x': // min/max[x]p
 				{
					switch(flag.charAt(2))
 					{
 						case 'n':
 							recipe.setMinExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
 							break;
 						case 'x':
 							recipe.setMaxExp(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
 							break;
 					}
 					
 					break;
 				}
 				
 				case 'l': // min/max[l]evel
 				{
					switch(flag.charAt(2))
 					{
 						case 'n':
 							recipe.setMinLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
 							break;
 						case 'x':
 							recipe.setMaxLevel(val == null ? null : new Flag<Integer>(val.intValue(), failMessage));
 							break;
 					}
 					
 					break;
 				}
 				
 				case 'm': // min/max[m]oney
 				{
					switch(flag.charAt(2))
 					{
 						case 'n':
 							recipe.setMinMoney(val == null ? null : new Flag<Double>(val, failMessage));
 							break;
 						case 'x':
 							recipe.setMaxMoney(val == null ? null : new Flag<Double>(val, failMessage));
 							break;
 					}
 					
 					break;
 				}
 			}
 			
 			return;
 		}
 		
 		log.warning("Unknown flag: '" + line + "' !");
 	}
 	
 	protected static String locationToString(Location location)
 	{
 		return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
 	}
 	
 	protected static Furnace stringToFurnace(String string)
 	{
 		String[] split = string.split(":");
 		World world = Bukkit.getWorld(split[0]);
 		
 		if(world == null) // world doesn't exist
 			return null;
 		
 		Block block = world.getBlockAt(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
 		
 		if(block.getType() != Material.BURNING_FURNACE) // not a running furnace
 			return null;
 		
 		BlockState blockState = block.getState();
 		
 		if(!(blockState instanceof Furnace)) // not really a furnace
 			return null;
 		
 		Furnace furnace = (Furnace)blockState;
 		
 		if(furnace.getBurnTime() <= 0) // furnace is not really running
 			return null;
 		
 		return furnace;
 	}
 	
 	private String shapedToString(ShapedRecipe recipe)
 	{
 		StringBuilder str = new StringBuilder();
 		
 		for(Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet())
 		{
 			if(entry.getKey() != null && entry.getValue() != null)
 				str.append(entry.getKey() + "=" + entry.getValue().getType() + ":" + entry.getValue().getDurability() + ";");
 		}
 		
 		for(String row : recipe.getShape())
 		{
 			str.append(row + ";");
 		}
 		
 		return str.toString();
 	}
 	
 	private String shapelessToString(ShapelessRecipe recipe)
 	{
 		StringBuilder str = new StringBuilder();
 		
 		for(ItemStack ingredient : recipe.getIngredientList())
 		{
 			if(ingredient == null)
 				continue;
 			
 			str.append(ingredient.getType() + ":" + ingredient.getDurability() + ";");
 		}
 		
 		return str.toString();
 	}
 }
