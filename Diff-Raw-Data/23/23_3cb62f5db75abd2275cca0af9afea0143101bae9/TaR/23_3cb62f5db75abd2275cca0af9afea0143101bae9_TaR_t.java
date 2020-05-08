 package me.FluffyWolfers.TaR;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TaR extends JavaPlugin{

 	public static TaR t;

 	public void onEnable(){

 		Bukkit.getLogger().info("[TakeARide] Starting up...");

 		t = this;

 		this.getServer().getPluginManager().registerEvents(new TaRListener(), this);

 	}

}
