 package com.nekocraft.skin;
 
 import com.nekocraft.skin.listener.NekoSkinPlayerListener;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NekoSkin extends JavaPlugin {
 	public static NekoSkin plugin;
 	private String skinurl;
 	private String cloakurl;
	protected Configuration config;
 
 
 	@Override
 	public void onEnable() {
 		plugin = this;
 		initConfig();
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new NekoSkinPlayerListener(), this);
 	}
 
 	private void initConfig() {
 		config = getConfig();
 		skinurl = config.getString("setting.skinurl");
 		cloakurl = config.getString("setting.cloakurl");
 	}
 
 	@Override
 	public void onDisable() {
 		plugin = null;
 	}
 
 	public String getSkinurl(String player) {
 		return String.format(skinurl, player);
 	}
 
 	public String getCloakurl(String player) {
 		return String.format(cloakurl, player);
 	}
 }
