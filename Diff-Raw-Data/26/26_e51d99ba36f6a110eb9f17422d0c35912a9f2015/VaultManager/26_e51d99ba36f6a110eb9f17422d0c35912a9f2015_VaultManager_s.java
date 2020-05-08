 package Mike724.minecraft.WebMarket.util;
 
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 public class VaultManager {
 	public static Permission permission = null;
 	public static Economy economy = null;
 
 	public static boolean setupEconomy(JavaPlugin plugin) {
 		if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    public static boolean setupPermissions(JavaPlugin plugin) {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        permission = rsp.getProvider();
        return permission != null;
    }
 }
