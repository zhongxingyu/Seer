 package com.github.adut.ExplodingSheep;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Sheep;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ExplodingSheep extends JavaPlugin implements Listener {
		
 	@Override
 	public void onEnable() {
 		
 		
 		getLogger().info("Exploding Sheep enabled");
 		getServer().getPluginManager().registerEvents(this, this);
 		this.saveDefaultConfig(); // Set up the default plugin if it's not there
 		
 	}
 	
 	@Override
 	public void onDisable() {
 		getLogger().info("Exploding Sheep disabled");
 		this.saveDefaultConfig(); // Set up the default plugin if it's not there
 	}
 	
 	@EventHandler
 	public void onShear(PlayerShearEntityEvent event) {
 		Entity sheared = event.getEntity();
 		if (sheared instanceof Sheep)
 		{
 			Sheep sheep = (Sheep) sheared;
 			/* If the sheep isn't already sheared check wool color */
 			if(!sheep.isSheared()) {
 				int expStrength = this.getConfig().getInt("Global.xsexp");
 				String excolor = this.getConfig().getString("Global.xscolor");
 				if(excolor.equals("ALL") || sheep.getColor().name().equals(excolor)) {					
 					Location sheepLoc = sheared.getLocation();
 					sheared.getWorld().createExplosion(sheepLoc,expStrength);
 				}
 				
 			}
 		}
 		
 	}
 
 
 	
 	/*
 	 * On command check for the following
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		String cmdName = cmd.getName();
 		
 		if(cmdName.equals("xsexp")) {
 			this.getConfig().set("xsexp", args[0]);
 		} else if(cmdName.equals("xscolor")) {
 			this.getConfig().set("xscolor", args[0]);
 		}
 	        // If this hasn't happened the a value of false will be returned.
 		return false; 
 	}
 	
 	
 }
