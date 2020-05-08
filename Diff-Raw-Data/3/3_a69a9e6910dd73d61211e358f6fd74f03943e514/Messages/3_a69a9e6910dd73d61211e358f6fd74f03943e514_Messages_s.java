 package nl.giantit.minecraft.GiantShop.Misc;
 
import java.io.File;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  *
  * @author Giant
  */
 public class Messages {
 	
 	private GiantShop plugin;
 	private YamlConfiguration config;
 	private double yamlVersion = 0.1;
 	private HashMap<String, String> mainMsgs, adminMsgs, errorMsgs;
 	
 	public enum msgType {
 		ERROR,
 		MAIN,
 		ADMIN
 	}
 	
 	private void loadMain() {
 		this.mainMsgs = new HashMap<String, String>();
 		
 		Set<String> list = config.getConfigurationSection("main").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] There are no main messages specified in the msgTemplates.yml file. This might cause errors!");
 			return;
 		}
 		
 		for(String key : list) {
 			String msg = config.getString("main." + key);
 			this.mainMsgs.put(key, msg);
 		}
 	}
 	
 	private void loadAdmin() {
 		this.adminMsgs = new HashMap<String, String>();
 		
 		Set<String> list = config.getConfigurationSection("admin").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] There are no admin messages specified in the msgTemplates.yml file. This might cause errors!");
 			return;
 		}
 		
 		for(String key : list) {
 			String msg = config.getString("admin." + key);
 			this.adminMsgs.put(key, msg);
 		}
 	}
 	
 	private void loadErrors() {
 		this.errorMsgs = new HashMap<String, String>();
 		
 		Set<String> list = config.getConfigurationSection("errors").getKeys(false);
 		if(list == null) {
 			plugin.getLogger().log(Level.WARNING, "[" + plugin.getName() + "] There are no error messages specified in the msgTemplates.yml file. This might cause errors!");
 			return;
 		}
 		
 		for(String key : list) {
 			String msg = config.getString("errors." + key);
 			this.errorMsgs.put(key, msg);
 		}
 	}
 	
 	private String colourfy(String template) {
 		template = template.replaceAll("C_BLACK", "&0");
 		template = template.replaceAll("C_DARKBLUE", "&1");
 		template = template.replaceAll("C_GREEN", "&2");
 		template = template.replaceAll("C_BLUE", "&3");
 		template = template.replaceAll("C_DARKRED", "&4");
 		template = template.replaceAll("C_MAGENTA", "&5");
 		template = template.replaceAll("C_GOLD", "&6");
 		template = template.replaceAll("C_LIGHTGRAY", "&7");
 		template = template.replaceAll("C_GRAY", "&8");
 		template = template.replaceAll("C_PURPLE", "&9");
 		template = template.replaceAll("C_LIGHTGREEN", "&a");
 		template = template.replaceAll("C_LIGHTBLUE", "&b");
 		template = template.replaceAll("C_RED", "&c");
 		template = template.replaceAll("C_PINK", "&d");
 		template = template.replaceAll("C_YELLOW", "&e");
 		template = template.replaceAll("C_WHITE", "&f");
 		return template;
 	}
 	
 	private String deColourfy(String template) {
 		template = template.replaceAll("C_BLACK", "");
 		template = template.replaceAll("C_DARKBLUE", "");
 		template = template.replaceAll("C_GREEN", "");
 		template = template.replaceAll("C_BLUE", "");
 		template = template.replaceAll("C_DARKRED", "");
 		template = template.replaceAll("C_MAGENTA", "");
 		template = template.replaceAll("C_GOLD", "");
 		template = template.replaceAll("C_LIGHTGRAY", "");
 		template = template.replaceAll("C_GRAY", "");
 		template = template.replaceAll("C_PURPLE", "");
 		template = template.replaceAll("C_LIGHTGREEN", "");
 		template = template.replaceAll("C_LIGHTBLUE", "");
 		template = template.replaceAll("C_RED", "");
 		template = template.replaceAll("C_PINK", "");
 		template = template.replaceAll("C_YELLOW", "");
 		template = template.replaceAll("C_WHITE", "");
 		return template;
 	}
 	
 	public Messages(GiantShop plugin) {
 		this.plugin = plugin;
 		
 		File configFile = new File(plugin.getDir(), "msgTemplate.yml");
 		if(!configFile.exists()) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] Extracting new msgTemplate.yml file...");
 			plugin.extract("msgTemplate.yml");
 		}
 		
 		config = YamlConfiguration.loadConfiguration(configFile);
 		double v = config.getDouble("version");
 		if(v < this.yamlVersion) {
 			plugin.getLogger().log(Level.INFO, "[" + plugin.getName() + "] Your msgTemplate.yml has ran out of date. Updating now!");
 			File oconfigFile = new File(plugin.getDir(), "msgTemplate.yml." + v + ".bak");
 			configFile.renameTo(oconfigFile);
 			plugin.extract("msgTemplate.yml");
 			config = YamlConfiguration.loadConfiguration(configFile);
 		}
 		
 		this.loadMain();
 		this.loadAdmin();
 		this.loadErrors();
 	}
 	
 	public String getMsg(msgType type, String template) {
 		return this.getMsg(type, template, null);
 	}
 	
 	public String getMsg(msgType type, String template, HashMap<String, String> data) {
 		String Template = null;
 		if(data == null)
 			data = new HashMap<String, String>();
 		
 		switch(type) {
 			case MAIN:
 				if(this.mainMsgs.containsKey(template))
 					Template = this.mainMsgs.get(template);
 				break;
 			case ADMIN:
 				if(this.adminMsgs.containsKey(template))
 					Template = this.adminMsgs.get(template);
 				break;
 			case ERROR:
 				if(this.errorMsgs.containsKey(template))
 					Template = this.errorMsgs.get(template);
 				break;
 		}
 		
 		if(Template != null) {
 			Template = Template.replace("&n", plugin.getPubName());
 			
 			for(Map.Entry<String, String> entry : data.entrySet()) {
 				Template = Template.replace("&" + entry.getKey(), entry.getValue());
 			}
 			
 			Template = this.colourfy(Template);
 			return Template;
 		}
 		
 		this.plugin.getLogger().log(Level.SEVERE, "[" + plugin.getName() + "] Template for " + template + " does not exist!");
 		return "&cRequested template does not exist!";
 	}
 	
 	public String getConsoleMsg(msgType type, String template) {
 		return this.getConsoleMsg(type, template, null);
 	}
 	
 	public String getConsoleMsg(msgType type, String template, HashMap<String, String> data) {
 		String Template = null;
 		if(data == null)
 			data = new HashMap<String, String>();
 		
 		switch(type) {
 			case MAIN:
 				if(this.mainMsgs.containsKey(template))
 					Template = this.mainMsgs.get(template);
 				break;
 			case ADMIN:
 				if(this.adminMsgs.containsKey(template))
 					Template = this.adminMsgs.get(template);
 				break;
 			case ERROR:
 				if(this.errorMsgs.containsKey(template))
 					Template = this.errorMsgs.get(template);
 				break;
 		}
 		
 		if(Template != null) {
 			Template = Template.replace("&n", plugin.getPubName());
 			
 			for(Map.Entry<String, String> entry : data.entrySet()) {
 				Template = Template.replace("&" + entry.getKey(), entry.getValue());
 			}
 			
 			Template = this.deColourfy(Template);
 			return Template;
 		}
 		
 		this.plugin.getLogger().log(Level.SEVERE, "[" + plugin.getName() + "] Template for " + template + " does not exist!");
 		return "&cRequested template does not exist!";
 	}
 }
