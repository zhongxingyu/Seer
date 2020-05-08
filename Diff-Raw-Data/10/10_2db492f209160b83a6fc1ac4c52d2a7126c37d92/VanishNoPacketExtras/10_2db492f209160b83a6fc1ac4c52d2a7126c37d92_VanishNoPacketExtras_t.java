 package se.DMarby.VanishNoPacketExtras;
 
 import com.google.common.base.Splitter;
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.event.VanishStatusChangeEvent;
 
 public class VanishNoPacketExtras extends JavaPlugin implements Listener {
 
     private static VanishNoPacketExtrasConfig config = new VanishNoPacketExtrasConfig();
     private Map<String, ItemStack[]> inventories = new HashMap();
     private Map<String, ItemStack[]> armor = new HashMap();
     private Map<String, Location> cordinates = new HashMap();
     private ItemStack[] inventory;
 
     private void log(String message) {
         getLogger().info(message);
     }
 
     // Inspired by fullwall
     private boolean addStackToInventory(ItemStack[] inventory, ItemStack add) {
         for (int i = 0; i < inventory.length; ++i) {
             ItemStack in = inventory[i];
             if (in == null) {
                 inventory[i] = add;
                 return true;
             } else if (in.getTypeId() == add.getTypeId()) {
                 int maxStackSize = in.getMaxStackSize();
                 if (in.getAmount() + add.getAmount() <= maxStackSize) {
                     in.setAmount(in.getAmount() + add.getAmount());
                     return true;
                 }
             }
         }
         return false;
     }
 
     // Inspired by fullwall
     private ItemStack[] loadInventory() {
        ItemStack[] new_inventory = new ItemStack[36];
         String load = config.items;
         for (String whole : Splitter.on('|').split(load)) {
             String[] parts = whole.split("x");
             int id, amount;
             try {
                 id = Integer.parseInt(parts[0]);
                 amount = Integer.parseInt(parts[1]);
             } catch (NumberFormatException ex) {
                 log("Incorrect item format " + whole + "; skipping.");
                 continue;
             }
             ItemStack add = new ItemStack(id, amount);
            boolean success = addStackToInventory(new_inventory, add);
             if (!success) {
                 log("Failed to put " + whole + " into the inventory - not enough space.");
             }
         }
        return new_inventory;
     }
 
     private void unVanish(Player player) {
         if (config.teleport) {
             player.teleport(cordinates.get(player.getName()));
             cordinates.remove(player.getName());
         }
         if (config.inventory) {
             player.getInventory().setContents(inventories.get(player.getName()));
             player.getInventory().setArmorContents(armor.get(player.getName()));
             inventories.remove(player.getName());
             armor.remove(player.getName());
         }
     }
 
     @Override
     public void onDisable() {
         for (String p : inventories.keySet()) {
             Player player = getServer().getPlayer(p);
             if (config.teleport) {
                 player.teleport(cordinates.get(player.getName()));
             }
             if (config.inventory) {
                 player.getInventory().setContents(inventories.get(player.getName()));
                 player.getInventory().setArmorContents(armor.get(player.getName()));
             }
             if (config.fly) {
                 player.setAllowFlight(false);
                 player.setFlying(false);
             }
         }
         log("v" + getDescription().getVersion() + " disabled.");
     }
 
     @Override
     public void onEnable() {
         config.setFile(this);
         config.load();
        inventory = loadInventory();
 
         getServer().getPluginManager().registerEvents(this, this);
         log("v" + getDescription().getVersion() + " enabled.");
     }
 
     @EventHandler
     public void onVanishStatusChange(VanishStatusChangeEvent event) {
         Player player = event.getPlayer();
         if (config.fly) {
             player.setAllowFlight(event.isVanishing());
             player.setFlying(event.isVanishing());
         }
         if (event.isVanishing()) {
             if (config.teleport) {
                 cordinates.put(player.getName(), player.getLocation());
             }
             if (config.inventory) {
                 inventories.put(player.getName(), player.getInventory().getContents());
                 armor.put(player.getName(), player.getInventory().getArmorContents());
                 player.getInventory().setArmorContents(null);
                 player.getInventory().setContents(inventory);
             }
         } else {
             unVanish(event.getPlayer());
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (inventories.containsKey(event.getPlayer().getName())) {
             event.getPlayer().setAllowFlight(false);
             event.getPlayer().setFlying(false);
             unVanish(event.getPlayer());
         }
     }
 }
