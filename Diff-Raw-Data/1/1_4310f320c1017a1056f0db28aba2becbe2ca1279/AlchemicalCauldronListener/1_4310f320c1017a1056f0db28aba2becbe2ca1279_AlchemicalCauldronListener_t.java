 package in.nikitapek.alchemicalcauldron.events;
 
 import com.amshulman.mbapi.MbapiPlugin;
 import in.nikitapek.alchemicalcauldron.util.AlchemicalCauldronConfigurationContext;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 public final class AlchemicalCauldronListener implements Listener {
     private static final float CAULDRON_HORIZONTAL_OFFSET = 0.5f;
     private static final byte CAULDRON_VERTICAL_OFFSET = 1;
     private static final byte ITEMSTACK_DESPAWN_TIME = 3;
 
     private static final float ITEM_VERTICAL_VELOCITY = 0.3f;
     private static final float ITEM_HORIZONTAL_VELOCITY_FRACTION = 0.05f;
 
     private static final byte TICKS_PER_SECOND = 20;
 
     private static final DecimalFormat decimalFormat = new DecimalFormat();
 
     private final MbapiPlugin plugin;
 
     private final Map<Material, Double> inputMaterials;
     private final Map<Material, HashMap<Material, Double>> materialMatches;
 
     public AlchemicalCauldronListener(final AlchemicalCauldronConfigurationContext configurationContext) {
         this.plugin = configurationContext.plugin;
 
         this.inputMaterials = configurationContext.inputMaterials;
         this.materialMatches = configurationContext.materialMatches;
 
         decimalFormat.setMaximumFractionDigits(2);
     }
 
     @EventHandler
     public void onItemDrop(final PlayerDropItemEvent event) {
         // Get the Item being thrown, as well as the relevant ItemStack.
         final Item item = event.getItemDrop();
         final Player player = event.getPlayer();
 
         new BukkitRunnable() {
             // Because the Y value and location only get updated at the end of the run() method, setting firstY to location.getY() right away would result in the task only getting called once.
             // Therefore, firstY and previousLocation are set to Integer.MAX_VALUE to ensure that the task doesn't cancel itself on the first run.
             private double firstY = Integer.MAX_VALUE;
             private Location previousLocation = new Location(item.getLocation().getWorld(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
 
             @Override
             public void run() {
                 // If the item was deleted or picked up or otherwise removed, there is no need to perform further checks on it.
                 if (item == null) {
                     this.cancel();
                    return;
                 }
 
                 final Location location = item.getLocation();
                 final Event itemEvent;
 
                 // If the block has just bounced, then we call the relevant event.
                 if (previousLocation.getY() < location.getY() && firstY > previousLocation.getY()) {
                     // If the item has just bounced, then we call the ItemBounceEvent.
                     itemEvent = new ItemBounceEvent(item, previousLocation, player);
                 } else if (location.getY() == previousLocation.getY()) {
                     // If the item is no longer falling, then we trigger the ItemLandEvent.
                     itemEvent = new ItemLandEvent(item, player);
                 } else {
                     firstY = previousLocation.getY();
                     previousLocation = location;
                     return;
                 }
 
                 Bukkit.getServer().getPluginManager().callEvent(itemEvent);
                 this.cancel();
             }
         }.runTaskTimer(plugin, 1L, 1L);
     }
 
     @EventHandler
     public void onItemBounce(final ItemBounceEvent event) {
         alchemizeItemStack(event.getItem(), event.getLocation().getBlock(), event.getPlayer());
     }
 
     @EventHandler
     public void onItemLand(final ItemLandEvent event) {
         alchemizeItemStack(event.getItem(), event.getBlock(), event.getPlayer());
     }
 
     /**
      *
      * @param item the Item to be alchemized.
      * @param block the cauldron Block.
      * @param player the Player who threw the Item.
      */
     private void alchemizeItemStack(final Item item, final Block block, final Player player) {
         final Location location = block.getLocation();
         final ItemStack itemStack = item.getItemStack();
         final Material material = itemStack.getType();
 
         // If the player does not have permissions to use AlchemicalCauldron, cancels the event.
         if (player == null || !player.hasPermission("alchemicalcauldron.use")) {
             return;
         }
 
         // Checks to make sure the block is a cauldron.
         if (!Material.CAULDRON.equals(block.getType())) {
             return;
         }
 
         // Checks to make sure the ItemStack contains a valid input material.
         if (!inputMaterials.containsKey(material)) {
             return;
         }
 
         // Gets the probability for that input item.
         final double inputProbability = inputMaterials.get(itemStack.getType());
 
         for (int i = 1; i <= itemStack.getAmount(); i++) {
             // If the conversion fails, delete the item.
             if (Math.random() > inputProbability) {
                 continue;
             }
 
             // If the conversion was successful, create a new ItemStack with a randomized (based on ratio) output item.
             // Also create the timer which, when completed, will delete the "input" block and create the "output" block.
             setItemCreationTimer(new Location(item.getWorld(), location.getBlockX() + CAULDRON_HORIZONTAL_OFFSET, location.getBlockY() + CAULDRON_VERTICAL_OFFSET, location.getBlockZ() + CAULDRON_HORIZONTAL_OFFSET),
                     new ItemStack(getObjectByProbability(materialMatches.get(material).entrySet()), 1));
         }
 
         item.remove();
     }
 
     private void setItemCreationTimer(final Location location, final ItemStack itemStack) {
         // Creates an sync task, which when run, creates the new item.
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 // Sets the Item and sets its location to the centre of the CAULDRON.
                 final Item item = location.getWorld().dropItem(location, itemStack);
                 item.setPickupDelay(ITEMSTACK_DESPAWN_TIME);
 
                 // Gives the item a slightly randomized horizontal velocity.
                 item.setVelocity(new Vector(Vector.getRandom().getX() * ITEM_HORIZONTAL_VELOCITY_FRACTION, ITEM_VERTICAL_VELOCITY, Vector.getRandom().getZ() * ITEM_HORIZONTAL_VELOCITY_FRACTION));
             }
         }, ITEMSTACK_DESPAWN_TIME * TICKS_PER_SECOND);
     }
 
     private <K> K getObjectByProbability(final Set<Entry<K, Double>> set) {
         // Selects a randomized output value based on its probability.
         while (true) {
             double probability = Math.random();
 
             for (final Entry<K, Double> entry : set) {
                 if (probability < entry.getValue()) {
                     return entry.getKey();
                 } else {
                     probability -= entry.getValue();
                 }
             }
         }
     }
 }
