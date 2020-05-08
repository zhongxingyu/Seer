 package Mike724.minecraft.WebMarket.util;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Settings extends JavaPlugin {
	public static String SECRETKEY;
	public static int HTTPPORT;
	public static void setSK(String sk) {
		SECRETKEY = sk;
	}
	public static void setPort(int p) {
		HTTPPORT = p;
	}
 }
