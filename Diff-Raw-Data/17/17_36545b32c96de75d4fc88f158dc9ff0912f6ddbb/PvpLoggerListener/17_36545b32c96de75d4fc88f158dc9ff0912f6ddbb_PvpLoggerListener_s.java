 package mccity.plugins.pvprealm.listeners;
 
 import com.google.common.base.Function;
 import com.herocraftonline.heroes.api.events.HeroLeaveCombatEvent;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import com.herocraftonline.heroes.characters.effects.CombatEffect;
 import mccity.plugins.pvprealm.PvpRealm;
 import mccity.plugins.pvprealm.Settings;
 import mccity.plugins.pvprealm.object.ObjectManager;
 import mccity.plugins.pvprealm.object.PvpPlayer;
 import me.galaran.bukkitutils.pvprealm.text.Messaging;
 import me.galaran.bukkitutils.pvprealm.text.StringUtils;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 public class PvpLoggerListener implements Listener {
 
     private final PvpRealm plugin;
 
     public PvpLoggerListener(PvpRealm pvpRealm) {
         plugin = pvpRealm;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onHeroLeaveCombat(HeroLeaveCombatEvent event) {
         if (event.getReason() != CombatEffect.LeaveCombatReason.LOGOUT) return;
         if (!Settings.pvpLogger) return;
         Hero hero = event.getHero();
         if (!Settings.pvpLoggerGlobal && !hero.getPlayer().getLocation().getWorld().equals(Settings.pvpWorld)) return;
 
         CombatEffect combat = (CombatEffect) hero.getEffect("Combat");
         if (combat == null) return;
 
         Set<Player> combatPlayers = new HashSet<Player>();
         Map<LivingEntity, CombatEffect.CombatReason> combatants = combat.getCombatants();
         for (LivingEntity living : combatants.keySet()) {
             if (living instanceof Player) {
                 combatPlayers.add((Player) living);
             }
         }
         combatPlayers.remove(hero.getPlayer()); // remove self
 
         if (!combatPlayers.isEmpty()) {
             onPvpLogout(ObjectManager.instance.getPvpPlayer(hero.getPlayer()), combatPlayers);
         }
     }
 
     public void onPvpLogout(PvpPlayer pvpPlayer, Set<Player> combatWith) {
         Player player = pvpPlayer.getPlayer();
         if (!Settings.pvpLoggerOp && player.isOp()) return;
 
         if (Settings.pvpLoggerBypassFriendly) {
             if (Settings.debug) {
                 Messaging.log("$1 logged out of pvp. Before friend check: $2", player.getName(), combatWith.toString());
             }
             Iterator<Player> itr = combatWith.iterator();
             while (itr.hasNext()) {
                 PvpPlayer curCombatPlayer = ObjectManager.instance.getPvpPlayer(itr.next());
                 if (curCombatPlayer.hasFriend(player)) {
                     itr.remove();
                     if (Settings.debug) {
                         Messaging.log("$1 has friend $2", curCombatPlayer.getName(), player.getName());
                     }
                 }
             }
 
             if (combatWith.isEmpty()) return;
         }
 
         String playerList = StringUtils.join(combatWith, ", ", new Function<Player, String>() {
             @Override
             public String apply(Player player) {
                 return player.getName();
             }
         });
 
        String pvpLogMessage = Messaging.getDecoratedTranslation("logger.message", pvpPlayer.getName(), playerList);
        Messaging.log(pvpLogMessage);
         if (Settings.pvpLoggerMessage) {
            Messaging.broadcastServerNoPrefix(pvpLogMessage);
         }
 
         Hero hero = plugin.getHero(pvpPlayer);
         if (Settings.pvpLoggerExpPenalty > 0) {
             hero.gainExp(-Settings.pvpLoggerExpPenalty, HeroClass.ExperienceType.EXTERNAL, player.getLocation());
         }
         if (Settings.pvpLoggerKill) {
             hero.setHealth(0);
             hero.syncHealth();
         }
     }
 }
