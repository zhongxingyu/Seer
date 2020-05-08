 package com.gmail.zariust.otherbounds.parameters.actions;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.gmail.zariust.otherbounds.Log;
 import com.gmail.zariust.otherbounds.OtherBounds;
 
 
 public class DamageAction extends Action {
     // "potioneffect: "
     // message.player, message.radius@<r>, message.world, message.server
     public enum DamageActionType {
         ATTACKER, VICTIM, RADIUS, WORLD, SERVER, TOOL
     }
 
     public enum DamageType {
         NORMAL, FIRE, LIGHTNING
     }
 
     static Map<String, DamageActionType>    matches = new HashMap<String, DamageActionType>();
     static {
        matches.put("damage", DamageActionType.ATTACKER);
         matches.put("damageattacker", DamageActionType.ATTACKER);
         matches.put("damage.attacker", DamageActionType.ATTACKER);
         matches.put("damage.victim", DamageActionType.VICTIM);
         matches.put("damage.server", DamageActionType.SERVER);
         matches.put("damage.world", DamageActionType.WORLD);
         matches.put("damage.global", DamageActionType.SERVER);
         matches.put("damage.all", DamageActionType.SERVER);
         matches.put("damage.radius", DamageActionType.RADIUS);
 
         // Can't do tooldamage yet - need a way to damage tools by "1" if a
         // block break
         // event and this condition hasn't run.
 
         // matches.put("damage.tool", DamageActionType.TOOL);
         // matches.put("damagetool", DamageActionType.TOOL);
     }
 
     protected DamageActionType              damageActionType;
     protected double                        radius  = 10;
     private final Map<IntRange, DamageType> damages;                                          // this
                                                                                                // can
                                                                                                // contain
                                                                                                // variables,
                                                                                                // parse
                                                                                                // at
                                                                                                // runtime
 
     public DamageAction(Object object, DamageActionType damageEffectType2) {
         damageActionType = damageEffectType2;
         damages = new HashMap<IntRange, DamageType>();
 
         if (object instanceof List) {
             // TODO: support lists?
             @SuppressWarnings("unchecked")
             List<Object> stringList = (List<Object>) object;
             for (Object sub : stringList) {
                 if (sub instanceof String)
                     parseDamage((String) sub);
                 else if (sub instanceof Integer)
                     parseDamage(String.valueOf(sub));
             }
         } else if (object instanceof String) {
             parseDamage((String) object);
         } else if (object instanceof Integer) {
             parseDamage(String.valueOf(object));
         }
     }
 
     private void parseDamage(String sub) {
         IntRange value = IntRange.parse("0");
         DamageType type = DamageType.NORMAL;
 
         if (sub.matches("(?i)fire.*")) {
             type = DamageType.FIRE;
             String[] split = sub.split("@");
             if (split.length > 1)
                 value = IntRange.parse(split[1]);
             else
                 value = IntRange.parse("60"); // default to 60 ticks (3 seconds)
         } else if (sub.matches("(?i)lightning.*")) {
             type = DamageType.LIGHTNING;
             String[] split = sub.split("@");
             if (split.length > 1)
                 value = IntRange.parse(split[1]);
             // default of 0 (harmless lightning) is ok.
         } else
             value = IntRange.parse(sub);
 
         damages.put(value, type);
     }
 
     @Override
     public boolean act(Occurrence occurrence) {
         if (damages != null) {
             for (IntRange key : damages.keySet()) {
                 processDamage(occurrence, key, damages.get(key));
             }
         }
 
         return false;
     }
 
     private void processDamage(Occurrence occurence, IntRange damageRange, DamageType damageType) {
 
         switch (damageActionType) {
         case ATTACKER:
             if (occurence.getAttacker() != null) {
                 damage(occurence.getAttacker(), damageRange, damageType, null);
             }
             break;
         case VICTIM:
             if (occurence.getVictim() != null) {
                 damage(occurence.getVictim(), damageRange, damageType, occurence.getAttacker());
             }
 
             break;
         case RADIUS:
             // occurence.getLocation().getRadiusPlayers()? - how do we get
             // players around radius without an entity?
             Location loc = occurence.getLocation();
             for (Player player : loc.getWorld().getPlayers()) {
                 if (player.getLocation().getX() > (loc.getX() - radius)
                         || player.getLocation().getX() < (loc.getX() + radius))
                     if (player.getLocation().getY() > (loc.getY() - radius)
                             || player.getLocation().getY() < (loc.getY() + radius))
                         if (player.getLocation().getZ() > (loc.getZ() - radius)
                                 || player.getLocation().getZ() < (loc.getZ() + radius))
                             damage(player, damageRange, damageType, occurence.getAttacker());
             }
 
             break;
         case SERVER:
             for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                 damage(player, damageRange, damageType, occurence.getAttacker());
             }
             break;
         case WORLD:
             for (Player player : occurence.getLocation().getWorld()
                     .getPlayers()) {
                 damage(player, damageRange, damageType, occurence.getAttacker());
             }
             break;
         case TOOL:
             // not yet supported, as default damage of 1 needs to be done in the
             // main DropRunner.run() method
             break;
         default:
             break;
         }
 
     }
 
     private void damage(LivingEntity ent, IntRange damageRange, DamageType damageType, LivingEntity attacker) {
         int damageVal = damageRange.getRandomIn(OtherBounds.rng);
         Log.highest("Damaging entity: " + ent.toString() + " range="
                 + damageRange.toString() + " value=" + damageVal + " ("
                 + damageType.toString() + ")");
         switch (damageType) {
         case NORMAL:
             if (damageVal < 0) {
                 int newHealth = ent.getHealth() + (damageVal * -1);
                 if (newHealth > ent.getMaxHealth())
                     newHealth = ent.getMaxHealth();
                 ent.setHealth(newHealth);
             } else if (damageVal > 0) {
                 if (attacker != null) {
                     Log.high("Attacker found, " + attacker.toString());
                     ent.damage(damageVal, attacker);
                 } else {
                     ent.damage(damageVal);
                 }
             }
             break;
         case FIRE:
             ent.setFireTicks(damageVal);
             break;
         case LIGHTNING:
             Location location = ent.getLocation().clone();
             
             // TODO: support randomised locations? 
             //location = getRandomisedLocation(location);
             World world = location.getWorld();
 
             if (damageVal == 0)
                 world.strikeLightningEffect(location);
             else
                 world.strikeLightning(location);
 
             break;
         }
 
     }
 
     // @Override
     @Override
     public List<Action> parse(ConfigurationSection parseMe) {
         List<Action> actions = new ArrayList<Action>();
 
         for (String key : matches.keySet()) {
             if (parseMe.get(key) != null)
                 actions.add(new DamageAction(parseMe.get(key), matches.get(key)));
         }
 
         return actions;
     }
 
 }
