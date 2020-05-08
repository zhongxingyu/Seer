 package com.crystalcraftmc.darkhorse;
 
 import java.io.IOException;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Horse.Variant;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Main extends JavaPlugin
 {
 	@Override
     public void onEnable()
 	{
         // TODO Insert logic to be performed when the plugin is enabled
 		getLogger().info("DarkHorse has been enabled!");
 		
 		// ...link plugin with online stats.
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e){
 			// Failed to submit the stats :-(
 		}
 		
 		// Generate the config.yml file...
 		this.saveDefaultConfig();
 		
 		// ...load the configuration file and copy the defaults into the plugin...
 		this.getConfig().options().copyDefaults(true);
 		
 		// ...and save the configuration file.
         this.saveConfig();
         
         // ...see if the config file allows auto-updating...
         if (this.getConfig().getBoolean("auto-update"))
         {
         	// ...and if so, run the auto-update class.
        	@SuppressWarnings({ "unused" })
 			Updater updater = new Updater(this, "darkhorse", this.getFile(), Updater.UpdateType.DEFAULT, true);
         }
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
         if (!(sender instanceof Player))
         {
             sender.sendMessage("This command can only be run by a player.");
             return false;
         }
 
     	// Make the letter 'p' a variable for the command sender (or the player).
 		Player p = (Player) sender;
 		
     	if (cmd.getName().equalsIgnoreCase("dh"))
     	{
             if (args.length == 0)
             {
                 sender.sendMessage("Not enough arguments!");
                 return false;
             }
     		
     		else if(args[0].equalsIgnoreCase("horse"))
             {
                 if (args.length == 2 && args[1].equalsIgnoreCase("tamed"))
                 {
                     if (p.hasPermission("darkhorse.horse.tamed"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a normal horse...
                         horse.setVariant(Variant.HORSE);
 
                         // ...set the horse to the tamed state...
                         horse.setTamed(true);
 
                         // ...and set thw owner to be the player who ran the command.
                         horse.setOwner(p);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A tamed horse has been spawned.");
 
                         // If this has happened, the function will returns true.
                         return true;
                     }
                 }
 
                 else if (p.hasPermission("darkhorse.horse"))
                 {
                     // ...create a variable to find the player's location...
                     Location location = p.getLocation();
 
                     // ...then spawn a horse at the player's current location...
                     Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                     // ...and change the type of horse to a normal horse.
                     horse.setVariant(Variant.HORSE);
 
                     // Then, notify the player that the entity has been spawned.
                     p.sendMessage(ChatColor.GOLD + "A horse has been spawned.");
 
                     // If this has happened, the function will return true.
                     return true;
                 }
     		}
     		
     		else if(args[0].equalsIgnoreCase("donkey"))
     		{
                 if (args.length == 2 && args[1].equalsIgnoreCase("tamed"))
                 {
                     if (p.hasPermission("darkhorse.donkey.tamed"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a normal horse...
                         horse.setVariant(Variant.DONKEY);
 
                         // ...set the horse to the tamed state...
                         horse.setTamed(true);
 
                         // ...and set thw owner to be the player who ran the command.
                         horse.setOwner(p);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A tamed donkey has been spawned.");
 
                         // If this has happened, the function will returns true.
                         return true;
                     }
                 }
 
                 else
                 {
                     if (p.hasPermission("darkhorse.donkey"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a donkey.
                         horse.setVariant(Variant.DONKEY);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A donkey has been spawned.");
 
                         // If this has happened, the function will return true.
                         return true;
                     }
                 }
     		}
     		
     		else if(args[0].equalsIgnoreCase("mule"))
     		{
                 if (args.length == 2 && args[1].equalsIgnoreCase("tamed"))
                 {
                     if (p.hasPermission("darkhorse.mule.tamed"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a normal horse...
                         horse.setVariant(Variant.MULE);
 
                         // ...set the horse to the tamed state...
                         horse.setTamed(true);
 
                         // ...and set thw owner to be the player who ran the command.
                         horse.setOwner(p);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A tamed mule has been spawned.");
 
                         // If this has happened, the function will returns true.
                         return true;
                     }
                 }
                 else
                 {
                     if (p.hasPermission("darkhorse.mule"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a donkey.
                         horse.setVariant(Variant.MULE);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A mule has been spawned.");
 
                         // If this has happened, the function will return true.
                         return true;
                     }
                 }
     		}
     		
     		else if(args[0].equalsIgnoreCase("skeleton"))
     		{
                 if (args.length == 2 && args[1].equalsIgnoreCase("tamed"))
                 {
                     if (p.hasPermission("darkhorse.skeleton.tamed"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a normal horse...
                         horse.setVariant(Variant.SKELETON_HORSE);
 
                         // ...set the horse to the tamed state...
                         horse.setTamed(true);
 
                         // ...and set thw owner to be the player who ran the command.
                         horse.setOwner(p);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A tamed skeleton horse has been spawned.");
 
                         // If this has happened, the function will returns true.
                         return true;
                     }
                 }
                 else
                 {
                     if (p.hasPermission("darkhorse.skeleton"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a skeleton horse.
                         horse.setVariant(Variant.SKELETON_HORSE);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A skeleton horse has been spawned.");
 
                         // If this has happened, the function will return true.
                         return true;
                     }
                 }
     		}
     		
     		else if(args[0].equalsIgnoreCase("zombie"))
     		{
                 if (args.length == 2 && args[1].equalsIgnoreCase("tamed"))
                 {
                     if (p.hasPermission("darkhorse.zombie.tamed"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a normal horse...
                         horse.setVariant(Variant.UNDEAD_HORSE);
 
                         // ...set the horse to the tamed state...
                         horse.setTamed(true);
 
                         // ...and set thw owner to be the player who ran the command.
                         horse.setOwner(p);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A tamed zombie horse has been spawned.");
 
                         // If this has happened, the function will returns true.
                         return true;
                     }
                 }
                 else
                 {
                     if (p.hasPermission("darkhorse.zombie"))
                     {
                         // ...create a variable to find the player's location...
                         Location location = p.getLocation();
 
                         // ...then spawn a horse at the player's current location...
                         Horse horse = (Horse) location.getWorld().spawnEntity(location, EntityType.HORSE);
 
                         // ...and change the type of horse to a zombie horse.
                         horse.setVariant(Variant.UNDEAD_HORSE);
 
                         // Then, notify the player that the entity has been spawned.
                         p.sendMessage(ChatColor.GOLD + "A zombie horse has been spawned.");
 
                         // If this has happened, the function will return true.
                         return true;
                     }
                 }
     		}
         }
         // If this hasn't happened, a value of false will be returned.
         return false;
     }
 	
     @Override
     public void onDisable()
     {
         // TODO Insert logic to be performed when the plugin is disabled
     	getLogger().info("DarkHorse has been disabled!");
     }
 }
