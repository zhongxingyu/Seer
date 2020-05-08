 package me.shock.grenades;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class LaunchListener implements Listener
 {
 
 	
   private ArrayList<Location> explodes = new ArrayList<Location>();
 	
 	public Main plugin;
 	
 	public LaunchListener(Main instance)
 	{
 		this.plugin = instance;
 	}
 	
 	@EventHandler
 	public void onHit(ProjectileHitEvent event)
 	{
 		// Get the shooter.
 		LivingEntity shooter = event.getEntity().getShooter();
 		
 		
 		// Get the entity shot.
 		EntityType entity = event.getEntityType();
 		
 		// Check if it's a player.
 		if(shooter instanceof Player)
 		{
 			// Get the Player and inventory.
 			Player player = (Player) shooter;
 			Inventory inv = player.getInventory();
 			boolean hasTnt = inv.contains(Material.TNT, 1);
 			
 			
 			
 			// Snowball
 			if(entity.equals(EntityType.SNOWBALL))
 			{
 				// Flash grenade.
 				if(player.hasPermission("simplegrenades.flash"))
 				{
 					Location loc = event.getEntity().getLocation();
 					double flashRadius = plugin.getConfig().getDouble("Grenades.Flash.radius");
 					List<Entity> entities = event.getEntity().getNearbyEntities(flashRadius, flashRadius, flashRadius);
 					if (plugin.getConfig().getBoolean("Grenades.Flash.useTnT") == true)
 					{
 						if (hasTnt == true && !(player.hasPermission("simplegrenades.flash.notnt")))
 						{
 						    ItemStack contents = player.getInventory().getItem(player.getInventory().first(Material.TNT));
 				    	    if(contents.getAmount() > 1)
 					        {
 					            contents.setAmount(contents.getAmount() - 1);
 					        }
 					        else
 					        {
 						        player.getInventory().remove(new ItemStack(Material.TNT, 1));
 					        }
 				    	    
 				    	 // Simulates explosion
 							explodes.add(loc);
 							loc.getWorld().createExplosion(loc, .1F);
 							
 							// Blind and slow the nearby players.
 							for (Entity ents : entities)
 							{
 								if (ents instanceof Player) 
 								{
 									int duration = 20 * plugin.getConfig().getInt("Grenades.Flash.effectDuration");
 									int blindness = plugin.getConfig().getInt("Grenades.Flash.blindness");
 									int slowness = plugin.getConfig().getInt("Grenades.Flash.slowness");
 									
 									
 									Player victim = (Player) ents;
 									victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, blindness));
 									victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, slowness));
 								}
 							}
 				    	    
 						}
 						if (hasTnt == false && !(player.hasPermission("simplegrenades.flash.notnt")))
 						{
 							player.sendMessage(ChatColor.RED + "You need TNT to throw a flash grenade.");
 						}
 						if (player.hasPermission("simplegrenades.flash.notnt"))
 						{
 							
 							// Simulates explosion
 							explodes.add(loc);
 							loc.getWorld().createExplosion(loc, .1F);
 							
 							// Blind and slow the nearby players.
 							for (Entity ents : entities)
 							{
 								if (ents instanceof Player) 
 								{
 									int duration = 20 * plugin.getConfig().getInt("Grenades.Flash.effectDuration");
 									int blindness = plugin.getConfig().getInt("Grenades.Flash.blindness");
 									int slowness = plugin.getConfig().getInt("Grenades.Flash.slowness");
 									
 									
 									Player victim = (Player) ents;
 									victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, blindness));
 									victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, slowness));
 								}
 							}
 						}
 					}
 				}
 				
 
 				/* 
 				 * Concussion grenade.
 				 * 3F is too large of explosion. Try .5
 				 */
 				if(player.hasPermission("simplegrenades.concussion"))
 				{
 					Location loc = event.getEntity().getLocation();
 					double concussionSize = plugin.getConfig().getDouble("Grenades.Concussion.size");
 					List<Entity> entities = event.getEntity().getNearbyEntities(concussionSize, concussionSize, concussionSize);
 					
 					// Simulates explosion
 					explodes.add(loc);
 					loc.getWorld().createExplosion(loc, .5F);
 					
 					// Daze and slow nearby players.
 					for (Entity ents : entities)
 					{
 						if(ents instanceof Player)
 						{
 							int effectDuration = 20 * plugin.getConfig().getInt("Grenades.Concussion.effectDuration");
 							int confusion = plugin.getConfig().getInt("Grenades.Concussion.confusion");
 							int slowness = plugin.getConfig().getInt("Grenades.Concussion.slowness");
 							Player victim = (Player) ents;
 							victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, effectDuration, slowness));
 							victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, effectDuration, confusion));
 						}
 					}
 				}
 				
 				// Frag grenade
 				// Perfect :D
 				if(player.hasPermission("simplegrenades.frag"))
 				{
 					int fragSize = plugin.getConfig().getInt("Grenades.Frag.size");
 					Location loc = event.getEntity().getLocation();
 					if (plugin.getConfig().getBoolean("Grenades.Frag.useTnT") == true)
 					{
 						if (hasTnt == true && !(player.hasPermission("simplegrenades.frag.notnt")))
 						{
 						    ItemStack contents = player.getInventory().getItem(player.getInventory().first(Material.TNT));
 				    	    if(contents.getAmount() > 1)
 					        {
 					            contents.setAmount(contents.getAmount() - 1);
 					        }
 					        else
 					        {
 						        player.getInventory().remove(new ItemStack(Material.TNT, 1));
 					        }
 				    	    
 							loc.getWorld().createExplosion(loc, fragSize, false);
 				    	    
 						}
 						if (hasTnt == false && !(player.hasPermission("simplegrenades.frag.notnt")))
 						{
 							player.sendMessage(ChatColor.RED + "You need TNT to throw a frag grenade.");
 						}
 						if (player.hasPermission("simplegrenades.frag.notnt"))
 						{
 							loc.getWorld().createExplosion(loc, fragSize, false);
 						}
 					}
 				}
 				
 				// Smoke grenade
 				// Doesn't work
 				if(player.hasPermission("simplegrenades.smoke"));
	
	// Made this while at school
	// Hope it works
	
	{
	int radius = plugin.getConfig().getInt("Grenades.Smoke.radius");
        int x = (int)Math.floor(event.getEntity().getLocation().getX()/1);
        int z = (int)Math.floor(event.getEntity().getLocation().getZ()/1);
        int y = (int)Math.floor(event.getEntity().getLocation().getY()/1);
        for(int a = (x-radius); a <= (x+radius); a++){
            for(int b = (y-radius); b <= (y+radius); b++){
                for(int c = (z - radius); c <= (z+radius); c++){
                   World world  = player.getWorld();
               event.getEntity().getWorld.playEffect(new Location(world,a,b,c), Effect.SMOKE, 4);
           
        }
    }
}
}

				
 				
 				// Decoy grenade
 				if(player.hasPermission("simplegrenades.decoy"))
 				{
 				    Location loc = event.getEntity().getLocation();
 				    loc.getWorld().playEffect(loc, Effect.GHAST_SHOOT, 100);
 				}
 				
 				// Lightning
 				if(player.hasPermission("simplegrenades.lightning"))
 				{
 					Location loc = event.getEntity().getLocation();
 					loc.getWorld().strikeLightningEffect(loc);
 				}
 			}
 		}
 		
 	}
}
