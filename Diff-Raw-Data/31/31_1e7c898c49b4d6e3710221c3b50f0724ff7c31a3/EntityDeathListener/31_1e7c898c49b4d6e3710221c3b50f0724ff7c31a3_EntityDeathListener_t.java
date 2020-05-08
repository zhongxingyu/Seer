 package com.gildorymrp.gildorymclasses;
 
 import java.util.Map;
 import java.util.Random;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.gildorymrp.charactercards.CharacterCard;
 import com.gildorymrp.charactercards.GildorymCharacterCards;
 import com.gildorymrp.charactercards.Race;
 
 public class EntityDeathListener
 implements Listener
 {
 	private GildorymClasses plugin;
 
 	public EntityDeathListener(GildorymClasses plugin)
 	{
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		if ((event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
 			Player player = null;
 			if ((((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager() instanceof Player)) {
 				player = (Player)((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
 			}
 			if ((((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager() instanceof Projectile)) {
 				LivingEntity shooter = ((Projectile)((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager()).getShooter();
 				if ((shooter != null) && 
 						(shooter instanceof Player)) {
 					player = (Player) shooter;
 				}
 			}
 
 			Random random = new Random();
 			Integer money = Integer.valueOf(0);
 			if (player != null) {
 				Map<String, Integer> experienceMap = this.plugin.experience;
 				Map<String, Integer> levelMap = this.plugin.levels;
 				GildorymCharacterCards gildorymCharacterCards = (GildorymCharacterCards) Bukkit.getServer().getPluginManager().getPlugin("GildorymCharacterCards");
 				Map<String, CharacterCard> cardMap = gildorymCharacterCards.getCharacterCards();
 				
 				int expToNextLevel = Math.round(1000 * ((Integer)levelMap.get(player.getName())).intValue());
 
 				if (event.getEntityType() == EntityType.BLAZE) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 6));
 					//player.sendMessage(ChatColor.YELLOW + "+6 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.CAVE_SPIDER) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 5));
 					//player.sendMessage(ChatColor.YELLOW + "+5 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.ENDER_DRAGON) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 5000));
 					//player.sendMessage(ChatColor.YELLOW + "+5000 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.ENDERMAN) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 6));
 					//player.sendMessage(ChatColor.YELLOW + "+6 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + (random.nextInt(7) + 6));
 				} else if (event.getEntityType() == EntityType.GHAST) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 6));
 					//player.sendMessage(ChatColor.YELLOW + "+6 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.GIANT) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 100));
 					//player.sendMessage(ChatColor.YELLOW + "+100 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + (random.nextInt(151) + 50));
 				} else if (event.getEntityType() == EntityType.MAGMA_CUBE) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 2));
 					//player.sendMessage(ChatColor.YELLOW + "+2 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.OCELOT) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 2));
 					//player.sendMessage(ChatColor.YELLOW + "+2 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 4));
 					//player.sendMessage(ChatColor.YELLOW + "+4 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + (random.nextInt(3) + 2));
 				} else if (event.getEntityType() == EntityType.SILVERFISH) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 1));
 					//player.sendMessage(ChatColor.YELLOW + "+1 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.SKELETON) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 3));
 					//player.sendMessage(ChatColor.YELLOW + "+3 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + (random.nextInt(3) + 1));
 				} else if (event.getEntityType() == EntityType.SLIME) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 1));
 					//player.sendMessage(ChatColor.YELLOW + "+1 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + random.nextInt(4));
 				} else if (event.getEntityType() == EntityType.SPIDER) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 3));
 					//player.sendMessage(ChatColor.YELLOW + "+3 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.SQUID) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 1));
 					//player.sendMessage(ChatColor.YELLOW + "+1 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.WITCH) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 5));
 					//player.sendMessage(ChatColor.YELLOW + "+5 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + (random.nextInt(3) + 3));
 				} else if (event.getEntityType() == EntityType.WITHER) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 5000));
 					//player.sendMessage(ChatColor.YELLOW + "+5000 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.WOLF) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 2));
 					//player.sendMessage(ChatColor.YELLOW + "+2 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 				} else if (event.getEntityType() == EntityType.ZOMBIE) {
 					//experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() + 3));
 					//player.sendMessage(ChatColor.YELLOW + "+3 exp");
 					//player.sendMessage(ChatColor.GRAY + "Total: " + ChatColor.WHITE + experienceMap.get(player.getName()) + "/" + expToNextLevel);
 					money = Integer.valueOf(money.intValue() + random.nextInt(4));
 				}
 				while (((Integer)experienceMap.get(player.getName())).intValue() >= expToNextLevel) {
 					experienceMap.put(player.getName(), Integer.valueOf(((Integer)experienceMap.get(player.getName())).intValue() - expToNextLevel));
 					levelMap.put(player.getName(), Integer.valueOf(((Integer)levelMap.get(player.getName())).intValue() + 1));
 					expToNextLevel = 1000 * ((Integer)levelMap.get(player.getName())).intValue();
 				}
 				
 				CharacterClass clazz = this.plugin.classes.get(player.getName());
 				Integer level = levelMap.get(player.getName());
 				Race race = cardMap.get(player.getName()).getRace();
 				Integer pvpHealth = CharacterCard.calculateHealth(clazz, race, level);
 				
				player.setExp((float) (((Integer) experienceMap.get(player.getName())).intValue() / (double) expToNextLevel));
 				player.setLevel(((Integer)levelMap.get(player.getName())).intValue());
 				player.setMaxHealth(pvpHealth * 5);
 				Economy economy = (Economy)this.plugin.getServer().getServicesManager().getRegistration(Economy.class).getProvider();
 				economy.depositPlayer(player.getName(), money.intValue());
 				if (money.intValue() > 1)
 					player.sendMessage(ChatColor.YELLOW + "+" + money + " " + economy.currencyNamePlural());
 				else if (money.intValue() == 1)
 					player.sendMessage(ChatColor.YELLOW + "+" + money + " " + economy.currencyNameSingular());
 			}
 		}
 	}
 }
