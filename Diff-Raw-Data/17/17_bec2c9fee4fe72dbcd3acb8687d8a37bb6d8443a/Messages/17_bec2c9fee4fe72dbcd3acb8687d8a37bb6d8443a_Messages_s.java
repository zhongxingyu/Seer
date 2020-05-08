 package digi.recipeManager;
 
 import java.io.File;
 
 import org.bukkit.*;
 import org.bukkit.command.*;
 import org.bukkit.configuration.file.*;
 
 public enum Messages
 {
 	CRAFT_CANTCRAFTANTHING("craft.cantcraftanything", "<dark_red>You don't have permission to use <red>ANY recipes<dark_red>!"),
 	CRAFT_NOREPAIR("craft.norepair", "<dark_red>The repairing recipes are disabled!"),
 	CRAFT_FAILURE("craft.failure", "<dark_red>Recipe failed! <gray>(<white>{chance} chance<gray>)"),
 	CRAFT_DROPPED("craft.dropped", "<dark_green>Some items couldn't fit in your inventory, they were dropped on the floor!"),
 	CRAFT_NOSMELT("craft.nosmelt", "<dark_red>Furnace at <gold>{location}<dark_red> lacks requirements to smelt recipe!"),
 	CRAFT_NOFUEL("craft.nofuel", "<dark_red>Furnace at <gold>{location}<dark_red> lacks requirements for fuel!"),
 	
 	CRAFT_NOPERMISSION("craft.nopermission", "<dark_red>You don't have the required permission for this recipe!"),
 	CRAFT_NOGROUP("craft.nogroup", "<dark_red>You're not in the required group to use this recipe!"),
 	CRAFT_NOWORLD("craft.noworld", "<dark_red>You can't use this recipe in this world!"),
 	
 	CRAFT_NOPROXIMITY("craft.noproximity", "<dark_red>Furnace at <gold>{location}<dark_red> uses a recipe that requires you to be at most {distance} blocks away!"),
 	CRAFT_WARNDISTANCE("craft.warndistance", "<dark_green>Recipe will only work as long as you're within {distance} blocks from the furnace!"),
 	CRAFT_WARNONLINE("craft.warnonline", "<dark_green>Recipe will only work as long as you're online in the server!"),
 	
 	CRAFT_EXPLODE("craft.explode", "<dark_green>Recipe exploded! <gray>(<white>{chance} chance of explosion<gray>)"),
 	CRAFT_EXPLODEONSUCCESS("craft.explodeonsuccess", "<dark_green>Recipe exploded! <gray>(<white>{chance} chance of explosion when recipe works<gray>)"),
 	CRAFT_EXPLODEONFAILURE("craft.explodeonfailure", "<dark_red>Recipe exploded! <gray>(<white>{chance} chance of explosion when recipe fails<gray>)"),
 	
 	CRAFT_GIVEEXP("craft.giveexp", "<green>Got {amount} experience<dark_green> for crafting recipe."),
 	CRAFT_TAKEEXP("craft.takeexp", "<gold>Lost {amount} experience<dark_red> for crafting recipe."),
 	CRAFT_COSTEXP("craft.costexp", "<dark_red>Recipe <gold>costs {amount} experience<dark_red> to craft it!"),
 	CRAFT_MINEXP("craft.minexp", "<dark_red>Recipe usable if you have <gold>at least {amount} experience<dark_red>!"),
 	CRAFT_MAXEXP("craft.maxexp", "<dark_red>Recipe usable if you have <gold>less than {amount} experience<dark_red>!"),
 	
 	CRAFT_GIVELEVEL("craft.givelevel", "<green>Got {amount} level(s)<dark_green> for crafting recipe."),
 	CRAFT_TAKELEVEL("craft.takelevel", "<gold>Lost {amount} level(s)<dark_red> for crafting recipe."),
 	CRAFT_COSTLEVEL("craft.costlevel", "<dark_red>Recipe <gold>costs {amount} level(s)<dark_red> to craft it!"),
 	CRAFT_MINLEVEL("craft.minlevel", "<dark_red>Recipe usable if you have <gold>at least {amount} level(s)<dark_red>!"),
 	CRAFT_MAXLEVEL("craft.maxlevel", "<dark_red>Recipe usable if you have <gold>less than {amount} level(s)<dark_red>!"),
 	
	CRAFT_GIVEMONEY("craft.givemoney", "<green>Got {amount} {money}<dark_green> for crafting recipe."),
	CRAFT_TAKEMONEY("craft.takemoney", "<gold>Lost {amount} {money}<dark_red> for crafting recipe."),
	CRAFT_COSTMONEY("craft.costmoney", "<dark_red>Recipe <gold>costs {amount} {money}<dark_red> to craft it!"),
	CRAFT_MINMONEY("craft.minmoney", "<dark_red>Recipe usable if you have <gold>at least {amount} {money}<dark_red>!"),
	CRAFT_MAXMONEY("craft.maxmoney", "<dark_red>Recipe usable if you have <gold>less than {amount} {money}<dark_red>!"),
 	
 	NOSHIFTCLICK_MULTIPLERESULTS("noshiftclick.multipleresults", "<dark_red>Can't Shift+Click recipes with multiple results <underline>for now<reset><dark_red>, sorry."),
 	NOSHIFTCLICK_REWARDS("noshiftclick.rewards", "<dark_red>Can't Shift+Click recipes that give rewards <underline>for now<reset><dark_red>, sorry."),
 	NOSHIFTCLICK_FURNACEINVENTORY("noshiftclick.furnaceinventory", "<dark_red>Can't Shift+Click in furnace interfaces <underline>for now<reset><dark_red>, sorry."),
 	
 	GENERAL_RECIPE("general.recipe", "recipe"),
 	GENERAL_RECIPES("general.recipes", "recipes"),
 	
 	COMMAND_RMRECIPES_USAGE("command.rmrecipes.usage", "<white>Type <green>{command} <blue>item <gray>[i]<white> to search for recipes. Use this as item to search for held item. Specify i to search in ingredients instead of results."),
 	COMMAND_RMRECIPES_WORKBENCHRECIPES("command.rmrecipes.workbenchrecipes", "<white>Workbench has <gold>{craftrecipes} shaped <white> and <gold>{combinerecipes} shapeless<white> recipes."),
 	COMMAND_RMRECIPES_FURNACERECIPES("command.rmrecipes.furnacerecipes", "<white>Furnace has <gold>{smeltrecipes} recipes<white> and <gold>{fuels} fuels<white>."),
 	COMMAND_RMRECIPES_INVALIDITEM("command.rmrecipes.invaliditem", "<red>Invalid item: <gray>{item}<red>!"),
 	COMMAND_RMRECIPES_INVALIDHELDITEM("command.rmrecipes.invalidhelditem", "<red>You must hold an item to use this command like this."),
 	COMMAND_RMRECIPES_NOINGREDIENT("command.rmrecipes.noingredient", "<yellow>No recipes that have <blue>{item}<yellow> as ingredient."),
 	COMMAND_RMRECIPES_NORESULT("command.rmrecipes.noresult", "<yellow>No recipes that make <blue>{item}<yellow>."),
 	COMMAND_RMRECIPES_LISTINGREDIENT("command.rmrecipes.listingredient", "<light_purple>The <blue>{item}<light_purple> item can be used as an ingredient in <white>{recipes}<light_purple>:"),
 	COMMAND_RMRECIPES_LISTRESULT("command.rmrecipes.listresult", "<light_purple>The <blue>{item}<light_purple> item can be created from <white>{recipes}<light_purple>:"),
 	COMMAND_RMRECIPES_PAGEOFPAGES("command.rmrecipes.pageofpages", "<gray>(Page <white>{page}<gray> of <white>{pages}<gray>)"),
 	COMMAND_RMRECIPES_NEXTAVAILABLE("command.rmrecipes.nextavailable", "<yellow>End of page <white>{page}<yellow> of <white>{pages}<yellow>. Type <green>{command}<yellow> for next page."),
 	COMMAND_RMRECIPES_PREVAVAILABLE("command.rmrecipes.prevavailable", "<yellow>End of page <white>{page}<yellow> of <white>{pages}<yellow>. Type <green>{command}<yellow> for previous page."),
 	
 	COMMAND_RMCHECK_CHECKING("command.rmcheck.checking", "<white>Checking all files inside the '{folder}' folder..."),
 	COMMAND_RMCHECK_VALID("command.rmcheck.valid", "<green>All recipes are valid, no errors reported."),
 	COMMAND_RMCHECK_ERRORS("command.rmcheck.errors", "<red>There were errors processing the files, check server log!"),
 	
 	COMMAND_RMRELOAD_RELOADING("command.rmreload.reloading", "<white>Reloading all settings, recipes and language file..."),
 	COMMAND_RMRELOAD_DONE("command.rmreload.done", "<green>Everything reloaded succesfully, now there are {recipes} recipes."),
 	COMMAND_RMRELOAD_ERRORS("command.rmreload.errors", "<red>There were errors processing the files, check server log!");
 	
 	private String						path;
 	private String						message;
 	private static FileConfiguration	messages;
 	
 	private Messages(String path, String message)
 	{
 		this.path = path;
 		this.message = message;
 	}
 	
 	private void asign()
 	{
 		message = messages.getString(path, message);
 		
 		if(message.equals("false"))
 			message = null;
 	}
 	
 	/**
 	 * Gets the message for the selected enum
 	 * 
 	 * @return
 	 */
 	public String get()
 	{
 		return message;
 	}
 	
 	/**
 	 * Send the selected enum message to a player or console. <br>
 	 * Will not be displayed if the message is set to "false" in the messages.yml.
 	 * 
 	 * @param sender
 	 *            player or console
 	 */
 	public void print(CommandSender sender)
 	{
 		if(message != null)
 			printMessage(sender, message);
 	}
 	
 	/**
 	 * Send the selected enum message to a player or console with an overwriteable message from a recipe. <br>
 	 * The recipeMessage has priority if it's not null. <br>
 	 * If the priority message is "false" it will not be displayed.
 	 * 
 	 * @param sender
 	 *            player or console
 	 * @param recipeMessage
 	 *            overwrite message, ignored if null, don't display if "false"
 	 */
 	public void print(CommandSender sender, String recipeMessage)
 	{
 		if(recipeMessage != null) // recipe has custom message ?
 		{
 			if(!recipeMessage.equals("false")) // if it's not "false" send it, otherwise don't.
 				printMessage(sender, recipeMessage);
 		}
 		else if(message != null) // message not set to "false" in messages.yml (replaced with null to save memory)
 			printMessage(sender, message);
 	}
 	
 	/**
 	 * Send the selected enum message to a player or console with an overwriteable message from a recipe. <br>
 	 * The recipeMessage has priority if it's not null. <br>
 	 * If the priority message is "false" it will not be displayed. <br>
 	 * Additionally you can specify variables to replace in the message. <br>
 	 * The variable param must be a 2D String array that has pairs of 2 strings, variable and replacement value.
 	 * 
 	 * @param sender
 	 *            player or console
 	 * @param recipeMessage
 	 *            overwrite message, ignored if null, don't display if "false"
 	 * @param variables
 	 *            the variables array
 	 */
 	public void print(CommandSender sender, String recipeMessage, String[][] variables)
 	{
 		String msg = message;
 		
 		if(recipeMessage != null) // recipe has custom message
 		{
 			if(recipeMessage.equals("false")) // if recipe message is set to "false" then don't show the message
 				return;
 			
 			msg = recipeMessage;
 		}
 		else if(msg == null) // message from messages.yml is "false", don't show the message
 			return;
 		
 		if(variables != null && variables.length > 0)
 		{
 			for(String[] replace : variables)
 			{
 				msg = msg.replace(replace[0], replace[1]);
 			}
 		}
 		
 		printMessage(sender, msg);
 	}
 	
 	/**
 	 * Sends an array of messages to a player or console. <br>
 	 * Message supports &lt;color&gt; codes.
 	 * 
 	 * @param sender
 	 * @param messages
 	 */
 	public static void printMessage(CommandSender sender, String[] messages)
 	{
 		boolean noColors = (!RecipeManager.settings.COLOR_CONSOLE && sender instanceof ConsoleCommandSender);
 		
 		for(String message : messages)
 		{
 			message = replaceColorCodes(message, noColors);
 		}
 		
 		sender.sendMessage(messages);
 	}
 	
 	/**
 	 * Sends a message to a player or console. <br>
 	 * Message supports &lt;color&gt; codes.
 	 * 
 	 * @param sender
 	 * @param message
 	 */
 	public static void printMessage(CommandSender sender, String message)
 	{
 		sender.sendMessage(replaceColorCodes((sender instanceof ConsoleCommandSender ? "[RecipeManager] " + message : message), (!RecipeManager.settings.COLOR_CONSOLE && sender instanceof ConsoleCommandSender)));
 	}
 	
 	private static String replaceColorCodes(String message, boolean noColors)
 	{
 		for(ChatColor color : ChatColor.values())
 		{
 			message = message.replaceAll("(?i)<" + color.name() + ">", (noColors ? "" : "" + color));
 		}
 		
 		return message;
 	}
 	
 	/**
 	 * (Re)Loads all messages from messages.yml
 	 */
 	public static void loadMessages()
 	{
 		File file = new File(RecipeManager.plugin.getDataFolder() + File.separator + "messages.yml");
 		
 		if(!file.exists())
 		{
 			RecipeManager.plugin.saveResource("messages.yml", false);
 			log("messages.yml file created.");
 		}
 		
 		messages = YamlConfiguration.loadConfiguration(file);
 		
 		try
 		{
 			String version = messages.getString("lastchanged", null);
 			
 			if(version == null || !version.equals(RecipeManager.LAST_CHANGED_MESSAGES))
 				log("<yellow>messages.yml has changed! You should delete it, use rmreload to re-generate it and then re-configure it, and then rmreload again.");
 		}
 		catch(Exception e)
 		{
 			log("<yellow>Error reading messages.yml's version! You should delete it to allow it to re-generate the newest version!");
 		}
 		
 		for(Messages msg : values())
 		{
 			msg.asign();
 		}
 	}
 	
 	/**
 	 * Used by plugin to log messages, shouldn't be used by other plugins unless really needed to send e message tagged by RecipeManager
 	 * 
 	 * @param message
 	 */
 	public static void log(String message)
 	{
 		printMessage(Bukkit.getConsoleSender(), message);
 	}
 }
