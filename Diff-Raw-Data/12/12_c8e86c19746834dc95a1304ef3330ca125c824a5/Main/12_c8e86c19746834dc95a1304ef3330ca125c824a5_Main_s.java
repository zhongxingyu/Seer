 package edgruberman.bukkit.silkpoke;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.silkpoke.commands.Reload;
 import edgruberman.bukkit.silkpoke.messaging.ConfigurationCourier;
 import edgruberman.bukkit.silkpoke.messaging.Courier;
 import edgruberman.bukkit.silkpoke.util.CustomPlugin;
 
 public final class Main extends CustomPlugin implements Listener {
 
     public static Courier courier;
 
     private final List<Permission> permissions = new ArrayList<Permission>();
 
     @Override
    public void onLoad() { this.putConfigMinimum("config.yml", "2.2.0");  }
 
     @Override
     public void onEnable() {
         this.setPathSeparator('|').reloadConfig();
         Main.courier = new ConfigurationCourier(this);
 
         this.loadPermissions(this.getConfig().getConfigurationSection("permissions"));
 
         if (this.getConfig().isList("pickupData")) {
             final DataPicker picker = new DataPicker(this, this.getConfig().getStringList("pickupData"));
             Bukkit.getPluginManager().registerEvents(picker, this);
         }
 
         if (this.getConfig().getBoolean("vaporizer"))
             Bukkit.getPluginManager().registerEvents(new Vaporizer(), this);
 
         Bukkit.getPluginManager().registerEvents(this, this);
 
         this.getCommand("silkpoke:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll((Plugin) this);
 
         for (final Permission permission : this.permissions)
             Bukkit.getPluginManager().removePermission(permission);
 
         Main.courier = null;
     }
 
     private void loadPermissions(final ConfigurationSection permissions) {
         if (permissions == null) return;
 
         for (final String name : permissions.getKeys(false)) {
             final ConfigurationSection permission = permissions.getConfigurationSection(name);
             final String description = permission.getString("description");
             final PermissionDefault permDefault = PermissionDefault.getByName(permission.getString("default", Permission.DEFAULT_PERMISSION.name()));
             final Map<String, Boolean> children = new HashMap<String, Boolean>();
             for (final String child : permission.getConfigurationSection("children").getKeys(false))
                 children.put(child, permission.getConfigurationSection("children").getBoolean(child));
 
             final Permission created = new Permission(name, description, permDefault, children);
             this.permissions.add(created);
             Bukkit.getPluginManager().addPermission(created);
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
         if (!event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;
 
         if (!event.getPlayer().hasPermission("silkpoke.material." + event.getBlock().getType().name())) return;
 
         event.setCancelled(true);
         final int id = event.getBlock().getTypeId();
         final byte damage = event.getBlock().getState().getRawData();
         final ItemStack item = new ItemStack(id, 1, damage);
         event.getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
         event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
 
         this.getLogger().log(Level.FINEST, "Dropped {0}:{1} for {2} using {3} with {4}"
                 , new Object[] { item.getType().name(), (int) item.getDurability()
                 , event.getPlayer().getName(), event.getPlayer().getItemInHand().getType().name()
                 , Enchantment.SILK_TOUCH.getName() });
     }
 
 }
