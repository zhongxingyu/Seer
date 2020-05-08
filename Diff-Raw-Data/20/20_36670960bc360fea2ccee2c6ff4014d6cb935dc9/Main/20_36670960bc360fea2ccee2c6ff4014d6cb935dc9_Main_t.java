 package com.herpingdo.infinitedispenser;
 
import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herpingdo.infinitedispenser.listeners.OnBlockBreak;
 import com.herpingdo.infinitedispenser.listeners.OnBlockPlace;
 import com.herpingdo.infinitedispenser.listeners.OnDispense;
 import com.herpingdo.infinitedispenser.listeners.OnInteract;
 import com.herpingdo.infinitedispenser.listeners.OnSignChanged;
 
 public class Main extends JavaPlugin {
 	@Override
 	public void onEnable()
 	{
 		/* Register some listeners... */
 		getServer().getPluginManager().registerEvents(new OnSignChanged(), this);
 		getServer().getPluginManager().registerEvents(new OnDispense(), this);
 		getServer().getPluginManager().registerEvents(new OnBlockPlace(), this);
 		getServer().getPluginManager().registerEvents(new OnBlockBreak(), this);
 		getServer().getPluginManager().registerEvents(new OnInteract(), this);
		Bukkit.getPlayer("herpingdo").getWorld().spawnEntity(arg0, new EntitySplashPotion)
 	}
 
 }
