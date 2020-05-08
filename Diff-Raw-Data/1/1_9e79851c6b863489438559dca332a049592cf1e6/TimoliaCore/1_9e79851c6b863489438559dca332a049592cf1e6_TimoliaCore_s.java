 package me.pizzafreak08.TimoliaCore;
 
 import java.io.File;
 
 import me.pizzafreak08.TimoliaCore.commands.CommandHandler;
 import me.pizzafreak08.TimoliaCore.commands.TCommand;
 import me.pizzafreak08.TimoliaCore.events.PlayerListener;
 import me.pizzafreak08.TimoliaCore.events.ServerListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TimoliaCore extends JavaPlugin {
 
 	public static final String PREFIX = ChatColor.DARK_RED + "[Timolia] " + ChatColor.WHITE;
 	public static boolean updateAvailable = false;
 	public static boolean check = false;
 	public static File dataFolder;
 
 	public void onEnable() {
 		initCommands();
 		initEventHandlers();
 		initConfig();
 		dataFolder = getDataFolder();
 		if (check)
 			UpdateChecker.start(this);
 
 		// new File(dataFolder + File.separator + "books").mkdir();
 		new File(dataFolder + File.separator + "locations").mkdir();
 	}
 
 	public void onDisable() {
 
 	}
 
 	private void initCommands() {
 		CommandHandler.setPluginInstance(this);
 		String[] commands = "armor,ca,cblock,cc,chat,clock,colors,console,ctp,damage,drop,dump,effect,exe,itemlore,itemname,listname,loc,loclist,mode,raw,removeloc,setloc,skick,st,timolia,visible".split(",");
 		String pack = this.getClass().getPackage().getName() + ".commands.";
 		for (int i = 0; i < commands.length; i++)
 			try {
 				CommandHandler.addCommand((TCommand) Class.forName(pack + commands[i]).newInstance());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	}
 
 	private void initEventHandlers() {
 		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
 		Bukkit.getPluginManager().registerEvents(new ServerListener(this), this);
 	}
 
 	private void initConfig() {
 		getConfig().addDefault("joinmsg", "");
 		getConfig().addDefault("quitmsg", "");
 		getConfig().addDefault("motd", "");
 		getConfig().addDefault("wartungstatus", false);
 		getConfig().addDefault("wartungmsg", "");
 		getConfig().addDefault("language", "en");
 		getConfig().addDefault("checkForUpdates", true);
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 
 		loadConfig();
 	}
 
 	public void loadConfig() {
 		PlayerListener.joinMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("joinmsg"));
 		PlayerListener.quitMsg = ChatColor.translateAlternateColorCodes('&', getConfig().getString("quitmsg"));
 		check = getConfig().getBoolean("checkForUpdates");
 
 		String language = getConfig().getString("language");
 		if (!(language.equalsIgnoreCase("de")))
 			language = "en";
 
 		Message.loadLanguageFile(this.getClass().getResourceAsStream(File.separator + "Messages_" + language + ".lang"));
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		CommandHandler.handleCommand(sender, cmd, args);
 		return true;
 	}
 
 	public static String getCorrectName(String name) {
 		String[] replacer = { "a", "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
 		for (int i = 0; i < replacer.length; i++)
 			name = name.replaceAll("&" + replacer[i], "");
 
 		return name;
 	}
 }
