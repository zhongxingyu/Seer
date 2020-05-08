 package com.pvpkillz.plugins.Abilities;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 
 import utilities.BGKit;
 
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Animals;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class ABListener extends JavaPlugin implements Listener{
 	
 	Logger log = Logger.getLogger("Minecraft");
 
 	private Main plugin;
 
 public ArrayList<Player> HitterList = new ArrayList<Player>();
 public ArrayList<Player> MilkManList = new ArrayList<Player>();
 
 public ABListener(Main mainclass) {
 
 plugin = mainclass;
 
 mainclass.getServer().getPluginManager().registerEvents(this, mainclass);
 }
 @EventHandler
 public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 	
     if(event.getDamager() instanceof Egg){
         Egg egg = (Egg) event.getDamager();
          
         if(egg.getShooter() instanceof Player){
 	        Player shooter = (Player) egg.getShooter();
         	Animals mob = (Animals) event.getEntity();
 	        if(event.getEntity() instanceof Animals){
 		    	if (BGKit.hasAbility(shooter, 102)) {
 		        if(mob.getType() == EntityType.COW){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 92);
 		        	inv.addItem(item);
 	        	}
 		        if(mob.getType() == EntityType.SHEEP){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 91);
 		        	inv.addItem(item);
 	        	}
 		        if(mob.getType() == EntityType.MUSHROOM_COW){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 96);
 		        	inv.addItem(item);
 	        	}
 		        if(mob.getType() == EntityType.PIG){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 90);
 		        	inv.addItem(item);
 	        	}
 		        if(mob.getType() == EntityType.WOLF){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 95);
 		        	inv.addItem(item);
 	        	}
 		        if(mob.getType() == EntityType.CHICKEN){
 		        	mob.remove();
 		        	PlayerInventory inv = shooter.getInventory();
 		        	ItemStack item = new ItemStack(383, 1, (short) 93);
 		        	inv.addItem(item);
 	        	}
 	        }
 		  }
         }
         }
     }
         
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 	    Player breaker = event.getPlayer();
 	    Block broken = event.getBlock();
 		////////////////////////////////////////////////////////////////////////////////////////
 		////////////////////////////////////////////////////////////////////////////////////////
 		////////////////////////////////////////////////////////////////////////////////////////
     	if (BGKit.hasAbility(breaker, 100)) {
     		Block block = event.getBlock();
     		Location centerOfBlock = block.getLocation();
     		Random r = new Random();
     		int Chance = plugin.config.readInt("Abilities.100.Chance");
     		int Amount = plugin.config.readInt("Abilities.100.Amount");
     		double randomValue = 0 + (100 - 0) * r.nextDouble();
    		if ((Chance <= randomValue) && (broken.getType() == Material.IRON_ORE)){
     		block.getWorld().dropItemNaturally(centerOfBlock, new ItemStack(Material.IRON_INGOT, Amount));
     		}
     		else {
     			breaker.sendMessage(ChatColor.YELLOW+"Better Luck next time :P");
     		}
     	}
     }
 	@EventHandler
 	public void onPlayerMove(final PlayerMoveEvent event){
 	      final Player player = event.getPlayer();
 	      int Level = plugin.config.readInt("Abilities.101.Level")-1;
 		if (BGKit.hasAbility(player, 101)) {
 			if ((player.getLocation().getBlock().getType() == Material.WATER) || (player.getLocation().getBlock().getType() == Material.STATIONARY_WATER)) // If player is in water execute this code...
 			{
 				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1000000, Level)); // Give Strength Potion for abt 15 mins.
 				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1000000, 0)); // Give Aqua-Affinity Potion for abt 15 mins.
 		      }
 			else {
 				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 0, 0), true); // Remove Strengh Potion
 				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 0, 0), true); // Remove Aqua-Affinity Potion
 			}
 			}
 		}
 } // End of the Code
