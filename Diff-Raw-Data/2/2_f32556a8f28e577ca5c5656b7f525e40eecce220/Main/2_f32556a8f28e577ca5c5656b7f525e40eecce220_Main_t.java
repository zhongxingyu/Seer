 package edgruberman.bukkit.silkpoke;
 
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class Main extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         if (!this.getConfig().getBoolean("nether.ice")) new Sublimator(this);
         if (!this.getConfig().getBoolean("nether.water")) new Vaporizer(this);
         this.getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
         if (!event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
 
         if (!event.getPlayer().hasPermission("silkpoke.material." + event.getBlock().getType().name())) return;
 
         final SilkTouchBlockDrop custom = new SilkTouchBlockDrop(event.getBlock(), event.getPlayer());
         this.getServer().getPluginManager().callEvent(custom);
         if (custom.isCancelled()) return;
 
         event.setCancelled(true);
         final int id = event.getBlock().getTypeId();
        final byte damage = event.getBlock().getState().getRawData();
         final ItemStack item = new ItemStack(id, 1, damage, damage);
         event.getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
         event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
     }
 
 }
