 package in.nikitapek.alchemicalcauldron.events;
 
 import in.nikitapek.alchemicalcauldron.util.AlchemicalCauldronConfigurationContext;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
import java.util.Map.Entry;
import java.util.Set;

 public final class AlchemicalCauldronListener implements Listener {
     private static final float CAULDRON_HORIZONTAL_OFFSET = 0.5f;
     private static final byte CAULDRON_VERTICAL_OFFSET = 1;
     private static final byte ITEMSTACK_DESPAWN_TIME = 3;
 
     private static final float ITEM_VERTICAL_VELOCITY = 0.3f;
     private static final float ITEM_HORIZONTAL_VELOCITY_FRACTION = 0.05f;
 
     private static final byte TICKS_PER_SECOND = 20;
 
     private final AlchemicalCauldronConfigurationContext configurationContext;
 
     public AlchemicalCauldronListener(final AlchemicalCauldronConfigurationContext configurationContext) {
         this.configurationContext = configurationContext;
     }
 
     @EventHandler
     public void onItemDrop(final PlayerDropItemEvent event) {
         // Gets the block targeted by the player.
         final Block targetBlock = event.getPlayer().getTargetBlock(null, 1);
 
         // Checks to make sure the block is a CAULDRON.
         try {
             if (targetBlock.getType() != Material.CAULDRON) {
                 return;
             }
         } catch (final NullPointerException ex) {
             return;
         }
 
         // If the player does not have permissions to use AlchemicalCauldron, cancels the event.
         if (!event.getPlayer().hasPermission("alchemicalcauldron.use")) {
             return;
         }
 
         // Gets the thrown item and converts it into a usable ItemStack.
         final ItemStack thrownItemStack = event.getItemDrop().getItemStack();
 
         // Checks to make sure the ItemStack contains a valid input material.
         if (!configurationContext.getInputMaterials().containsKey(thrownItemStack.getType())) {
             return;
         }
 
         // Gets the probability for that input item.
         final double inputProbability = configurationContext.getInputMaterials().get(thrownItemStack.getType());
 
         for (int i = 1; i <= thrownItemStack.getAmount(); i++) {
             // If the conversion fails, delete the ItemStack.
             if (Math.random() > inputProbability) {
                 // Sets a timer to despawn the "used up" item.
                 setItemDespawnTimer(event.getItemDrop(), ITEMSTACK_DESPAWN_TIME);
                 continue;
             }
 
             // If the conversion was successful, makes a new ItemStack with a randomized (based on ratio) output item.
             final ItemStack newItemStack = new ItemStack(getObjectByProbability(configurationContext.getMaterialMatches().get(thrownItemStack.getType()).entrySet()), 1);
 
             // Possibly unnecessary double-check to make sure the material is not AIR?
             if (newItemStack.getType() == Material.AIR) {
                 continue;
             }
 
             // Creates the timer which, when completed, will delete the "input" block and create the "output" block.
             setItemCreationTimer(event.getItemDrop(),
                     new Location(event.getItemDrop().getWorld(), targetBlock.getX() + CAULDRON_HORIZONTAL_OFFSET, targetBlock.getY() + CAULDRON_VERTICAL_OFFSET, targetBlock.getZ() + CAULDRON_HORIZONTAL_OFFSET),
                    newItemStack);
         }
     }
 
    private void setItemCreationTimer(final Item previousItem, final Location loc, final ItemStack itemStack) {
         // Sets a timer to despawn the "used up" item.
        setItemDespawnTimer(previousItem, ITEMSTACK_DESPAWN_TIME);
 
         // Creates an sync task, which when run, creates the new item.
         Bukkit.getScheduler().scheduleSyncDelayedTask(configurationContext.plugin, new Runnable() {
             @Override
             public void run() {
                 // Sets the Item and sets its location to the centre of the CAULDRON.
                 final Item item = previousItem.getWorld().dropItem(loc, itemStack);
                item.setPickupDelay(ITEMSTACK_DESPAWN_TIME);
 
                 // Gives the item a slightly randomized vertical velocity.
                 final Vector zero = new Vector();
                 zero.setY(ITEM_VERTICAL_VELOCITY);
                 zero.setX(Vector.getRandom().getX() * ITEM_HORIZONTAL_VELOCITY_FRACTION);
                 zero.setZ(Vector.getRandom().getZ() * ITEM_HORIZONTAL_VELOCITY_FRACTION);
                 item.setVelocity(zero);
             }
        }, ITEMSTACK_DESPAWN_TIME * TICKS_PER_SECOND);
     }
 
     private void setItemDespawnTimer(final Item item, final int seconds) {
         // Set the item pickup delay to a the requested value so that it can not be picked up in the meantime.
         item.setPickupDelay((seconds) * TICKS_PER_SECOND);
 
         // Creates an sync task, which when run, deletes the item.
         Bukkit.getScheduler().scheduleSyncDelayedTask(configurationContext.plugin, new Runnable() {
             @Override
             public void run() {
                 item.remove();
             }
         }, seconds * TICKS_PER_SECOND);
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
