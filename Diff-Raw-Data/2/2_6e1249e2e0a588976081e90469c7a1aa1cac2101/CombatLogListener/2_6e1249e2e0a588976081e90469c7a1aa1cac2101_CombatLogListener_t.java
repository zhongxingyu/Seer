 package net.kingdomsofarden.andrew2060.anticombatlog.listeners;
 
 import net.kingdomsofarden.andrew2060.anticombatlog.AntiCombatLogPlugin;
 import net.kingdomsofarden.andrew2060.anticombatlog.CombatInformation;
 import net.kingdomsofarden.andrew2060.anticombatlog.ConfigManager;
 import net.kingdomsofarden.andrew2060.anticombatlog.Util;
 import net.kingdomsofarden.andrew2060.anticombatlog.events.CombatLogEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import com.earth2me.essentials.Essentials;
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.effects.CombatEffect.LeaveCombatReason;
 
 public class CombatLogListener implements Listener{
 	AntiCombatLogPlugin plugin;
 	private Essentials essentials;
 	private Heroes heroes;
 	private ConfigManager config;
 	public CombatLogListener(Heroes heroes, AntiCombatLogPlugin plugin) {
 		this.heroes = heroes;
 		this.plugin = plugin;
 		this.config = plugin.getConfigManager();
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPlayerKick(PlayerKickEvent event) {
 		Hero h = heroes.getCharacterManager().getHero(event.getPlayer());
 		if(h.isInCombat() == true) {
 			h.leaveCombat(LeaveCombatReason.LOGOUT);
 			return;
 		} 
 	}
 	@EventHandler(priority=EventPriority.HIGHEST)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Hero h = heroes.getCharacterManager().getHero(event.getEntity());
 		if(h.isInCombat()) {
 			h.leaveCombat(LeaveCombatReason.DEATH);
 			return;
 		} 
 	}
 
 	@EventHandler(priority=EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player p = event.getPlayer();
 		Hero h = heroes.getCharacterManager().getHero(event.getPlayer());
 		CombatInformation cI = Util.isInCombatWithPlayer(h);
 		if(!cI.isInCombat()) {
 			return;
 		}
 		
 		if(p.getHealth() != 0) {
 			String target = cI.getLastCombatant().getName();
 			if(cI.getLastCombatant() == p) {
 				h.leaveCombat(LeaveCombatReason.LOGOUT);
 				return;
 			}
 			if(AntiCombatLogPlugin.permission.has(p, "combatlog.bypass.logoff")) {
 				return;
 			}
 			CombatLogEvent cLEvent = new CombatLogEvent(cI.getLastCombatant(),p);
 			Bukkit.getPluginManager().callEvent(cLEvent);
 			if(cLEvent.isCancelled()) {
 				return;
 			} else {
				p.setHealth(0D);
 				Bukkit.getServer().broadcastMessage(ChatColor.AQUA + "[" + ChatColor.RED + "NOTICE" + ChatColor.AQUA + "]: " + p.getName() + " just CombatLogged against " + target + " and dropped their items!");
 				if(config.essentialsIntegration == true) {
 					essentials.getUser(p).addMail(config.essIntegrationMessage.replace("%player%", target));
 				}
 				if(config.economyEnabled) {
 					double bal = AntiCombatLogPlugin.economy.getBalance(p.getName());
 					AntiCombatLogPlugin.economy.withdrawPlayer(p.getName(), config.economyFlatValue);
 					AntiCombatLogPlugin.economy.withdrawPlayer(p.getName(), config.economyPercentValue*bal);
 				}
 				return;
 			}
 		}
 		h.leaveCombat(LeaveCombatReason.LOGOUT);
 	}
 }
 
