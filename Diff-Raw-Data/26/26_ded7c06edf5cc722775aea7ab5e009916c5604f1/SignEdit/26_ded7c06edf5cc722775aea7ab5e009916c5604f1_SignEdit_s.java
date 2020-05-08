 package me.carl230690.signedit;
 
import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.carl230690.signedit.commands.CommandSignEdit;
 import me.carl230690.signedit.data.SignEditDataPackage;
 import me.carl230690.signedit.localization.SignEditLocalization;
 import me.carl230690.signedit.utils.SignEditLogger;
 import me.carl230690.signedit.utils.SignEditUtils;
 
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.cyprias.YML;
 
 public class SignEdit extends JavaPlugin {	
 	public String chatPrefix = ChatColor.RESET + "[" + ChatColor.AQUA + "SignEdit" + ChatColor.WHITE + "] " + ChatColor.RESET;
 	public PluginManager pluginMan;
 
 	public Map<String, SignEditDataPackage> playerData = new HashMap<String, SignEditDataPackage>();
 	public Map<String, Integer> pasteAmounts = new HashMap<String, Integer>();
 
 	private SignEditPlayerListener listener;
 	public SignEditLogger log;
 	public SignEditUtils utils;
 	public SignEditLocalization localization;
 	public Config config;
 	public YML yml;
 
 	@Override
 	public void onEnable() {
 		config = new Config(this);
 		yml = new YML(this);
 		localization = new SignEditLocalization(this);		
 		
 		utils = new SignEditUtils(this);
 		log = new SignEditLogger(this);
 
 		listener = new SignEditPlayerListener(this);
 		
 		try {
 		log.info("Localized for locale: " + Config.getLocale());
 		
 		pluginMan = getServer().getPluginManager();
 
 		pluginMan.registerEvents(listener, this);
 
 		getCommand("signedit").setExecutor(new CommandSignEdit(this));
 	}
 
 	@Override
 	public void onDisable() {}
 }
