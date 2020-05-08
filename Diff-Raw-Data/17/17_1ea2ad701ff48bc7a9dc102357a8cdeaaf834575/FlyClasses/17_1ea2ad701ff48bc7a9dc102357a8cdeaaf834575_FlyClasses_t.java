 package ru.flydev.kartograf;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.flydev.kartograf.commands.FRpg;
 
 public class FlyClasses extends JavaPlugin {
 		 
 		public void onEnable(){
 			getLogger().info("Enable FlyClasses!");
 			
			getCommand("frpg").setExecutor(new FRpg(this));
 		}
 	 
 		public void onDisable(){
 			getLogger().info("Disable FlyClasses!");
 		}
 }
