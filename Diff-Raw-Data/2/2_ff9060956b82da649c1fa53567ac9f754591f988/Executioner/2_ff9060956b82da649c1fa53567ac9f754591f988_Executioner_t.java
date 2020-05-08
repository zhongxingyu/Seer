 package edgruberman.bukkit.guillotine;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Skull;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.SkullMeta;
 
 /** makes heads appear from dead bodies **/
 public final class Executioner implements Listener {
 
     private final Random rng = new Random();
 
     /** (victim, (killer, rate)) */
     private final Map<EntityType, Map<EntityType, Double>> instructions = new HashMap<EntityType, Map<EntityType, Double>>();
 
     public void putInstruction(final EntityType type, final Map<EntityType, Double> rates) {
         this.instructions.put(type, rates);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDeathByEntity(final EntityDeathEvent death) {
         // ignore events when no applicable instruction exists for victim
         Map<EntityType, Double> rates = this.instructions.get(death.getEntityType());
         if (rates == null) rates = this.instructions.get(null);
         if (rates == null) return;
         final EntityDamageEvent last = death.getEntity().getLastDamageCause();
         if (!(last instanceof EntityDamageByEntityEvent)) return;
 
         // randomize creation of head according to rates for killer
         final EntityDamageByEntityEvent cause = (EntityDamageByEntityEvent) last;
         final Double rate = rates.get(cause.getDamager().getType());
        if (rate == null || (rate < 1 && rate <= this.rng.nextDouble())) return;
 
         // create head
         final ItemStack skull = SkullType.of(death.getEntity()).toItemStack();
         final SkullMeta meta = (SkullMeta) skull.getItemMeta();
         if (SkullType.HUMAN.matches(skull)) {
             final String victim = ( death.getEntity() instanceof Player ? ((Player) death.getEntity()).getName() : null );
             meta.setOwner(victim);
         }
         skull.setItemMeta(meta);
 
         // drop head
         final Location drop = death.getEntity().getLocation();
         drop.getWorld().dropItemNaturally(drop, skull);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerSkullBlockRightClick(final PlayerInteractEvent interact) {
         if (interact.getAction() != Action.RIGHT_CLICK_BLOCK) return;
         if (interact.getClickedBlock().getTypeId() != Material.SKULL.getId()) return;
         final Skull state = (Skull) interact.getClickedBlock().getState();
         if (state.getSkullType() != org.bukkit.SkullType.PLAYER) return;
 
         Main.courier.send(interact.getPlayer(), "describe", state.getOwner());
     }
 
 }
