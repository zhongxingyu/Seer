 package Mike724.minecraft.WebMarket.util;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Settings extends JavaPlugin {
	public final String SECRETKEY = getConfig().getString("secret-key");
	public final int HTTPPORT = getConfig().getInt("http-port");
 }
