 package net.goldenapplemc.LegionChamps;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 
 public class ChampionHealth implements Listener {
 	LegionChamps plugin;
 	public ChampionHealth(LegionChamps plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR)
 	public void stopFoodRegen(EntityRegainHealthEvent e) {
 		if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)
 			e.setCancelled(true);
 	}
 
 	public void removeHealth(Player p, int amount, LivingEntity damager) {
 		Champion c = plugin.getChampion(p.getName());
 		int hp = c.getHp();
 		c.setHp(hp - amount);
		int hearts = (int)(20 / (c.getMaxHp() / c.getHp())); // Calculate percent of hp in hearts where 20 is max (10 hearts = 20 half hearts)
		if(hearts == 0) hearts = 1; // Make sure they don't die unless we want them to
		p.setHealth(hearts); // Set MC health to display percentage of current health
 		if(hp - amount <= 0) {
 			if(damager == null) killPlayer(p, null);
 			else {
 				killPlayer(p, damager); 
 			}
 		}
 	}
 
 	public void killPlayer(Player p, LivingEntity killer) {
 		p.setHealth(0);
 		Champion c = plugin.getChampion(p.getName());
 		c.setTotalDeaths(c.getTotalDeaths() + 1);
 		c.setHp(c.getMaxHp());
 		if(killer == null) c.setDeathsToEnvironment(c.getDeathsToEnvironment() + 1);
 		else if(killer instanceof Player) {
 			Player killerP = (Player) killer;
 			Champion killerC = plugin.getChampion(killerP.getName());
 			killerC.setPlayersKilled(killerC.getPlayersKilled() + 1);
 			c.setDeathsToPlayers(c.getDeathsToPlayers() + 1);
 		}
 		else {
 			c.setDeathsToMonsters(c.getDeathsToMonsters() + 1);
 		}
 	}
 }
