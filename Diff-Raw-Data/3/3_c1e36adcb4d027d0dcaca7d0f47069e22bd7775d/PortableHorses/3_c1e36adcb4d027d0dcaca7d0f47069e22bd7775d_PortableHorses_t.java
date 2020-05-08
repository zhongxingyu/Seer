 package com.norcode.bukkit.portablehorses;
 
 import net.h31ix.updater.Updater;
 import net.minecraft.server.v1_6_R2.EntityHorse;
 import net.minecraft.server.v1_6_R2.NBTCompressedStreamTools;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 import net.minecraft.v1_6_R2.org.bouncycastle.util.encoders.Base64;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftHorse;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.HorseInventory;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.*;
 
 public class PortableHorses extends JavaPlugin implements Listener {
 
     public static final String DISPLAY_NAME = "Portable Horse";
     public static final String LORE_PREFIX = ChatColor.DARK_GREEN + "" + ChatColor.DARK_PURPLE + "" + ChatColor.GRAY;
     private static final EnumSet<Material> INTERACTIVE_BLOCKS = EnumSet.of(Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.FENCE_GATE, Material.WORKBENCH,
                         Material.ENCHANTMENT_TABLE, Material.ENDER_CHEST, Material.ENDER_PORTAL_FRAME, Material.CHEST, Material.TRAPPED_CHEST, Material.REDSTONE_COMPARATOR_OFF,
                         Material.REDSTONE_COMPARATOR_ON, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.BEACON, Material.TRAP_DOOR, Material.NOTE_BLOCK, Material.JUKEBOX,
                         Material.BREWING_STAND, Material.ANVIL, Material.BED_BLOCK, Material.FURNACE, Material.BURNING_FURNACE);
 
     private Updater updater;
     private PacketListener packetListener;
     private boolean debugMode = false;
     private boolean usePermissions = true;
     private boolean storeArmor = true;
     private boolean storeInventory = true;
     private boolean allowNestedSaddles = false;
     private boolean requireSpecialSaddle = false;
     private boolean craftSpecialSaddle = false;
     private Random random = new Random();
     private HashMap<String, HashMap<Long, List<String>>> loreStorage = new HashMap<String, HashMap<Long, List<String>>>();
     private ShapedRecipe specialSaddleRecipe;
 
 
     public static LinkedList<String> nbtToLore(NBTTagCompound tag) {
         byte[] tagdata = NBTCompressedStreamTools.a(tag);
         LinkedList<String> lines = new LinkedList<String>();
         String encoded = new String(Base64.encode(tagdata));
         while (encoded.length() > 32760) {
             lines.add(ChatColor.BLACK + encoded.substring(0, 32760));
             encoded = encoded.substring(32760);
         }
         if (encoded.length() > 0) {
             lines.add(ChatColor.BLACK + encoded);
         }
         return lines;
     }
 
     public NBTTagCompound nbtFromLore(List<String> lore) {
         String data = "";
         for (int i=0;i<lore.size();i++) {
             if (lore.get(i).startsWith(ChatColor.BLACK.toString())) {
                 data += lore.get(i).substring(2);
             }
         }
         byte[] decoded = null;
         try {
             decoded = Base64.decode(data);
         } catch (Exception e) {
             e.printStackTrace();
             debug(data.toString());
         }
         NBTTagCompound tag = NBTCompressedStreamTools.a(decoded);
         return tag;
     }
 
     public ItemStack saveToSaddle(Horse horse, ItemStack saddle) {
         NBTTagCompound tag = new NBTTagCompound();
         EntityHorse eh = ((CraftHorse) horse).getHandle();
         eh.b(tag);
 
         ItemMeta meta = saddle.getItemMeta();
         if (meta == null) {
             meta = getServer().getItemFactory().getItemMeta(Material.SADDLE);
         }
         meta.setDisplayName(DISPLAY_NAME);
         if (horse.getCustomName() != null) {
             meta.setDisplayName(horse.getCustomName());
         }
         LinkedList<String> lore = nbtToLore(tag);
         lore.addFirst(LORE_PREFIX + horse.getVariant().name() + "/" + horse.getColor().name());
         meta.setLore(lore);
         saddle.setItemMeta(meta);
         return saddle;
     }
 
     public void restoreHorseFromSaddle(ItemStack stack, Horse horse) {
         EntityHorse eh = ((CraftHorse) horse).getHandle();
         if (stack.hasItemMeta()) {
             List<String> lore = stack.getItemMeta().getLore();
             if (lore != null) {
                 NBTTagCompound tag = nbtFromLore(lore);
                 debug("Restoring Horse: " + tag.toString());
                 eh.a(tag);
             }
         }
     }
 
     private Recipe getSpecialSaddleRecipe() {
         if (this.specialSaddleRecipe == null) {
             ItemStack result = getEmptyPortableHorseSaddle();
             this.specialSaddleRecipe = new ShapedRecipe(result);
             this.specialSaddleRecipe.shape("PPP", "PSP", "PPP");
             this.specialSaddleRecipe.setIngredient('P', getCraftingSupplement());
             this.specialSaddleRecipe.setIngredient('S', Material.SADDLE);
         }
         return this.specialSaddleRecipe;
     }
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
         this.packetListener = new PacketListener(this);
         doUpdater();
         getServer().getPluginManager().registerEvents(this, this);
 
     }
 
     @Override
     public void reloadConfig() {
         super.reloadConfig();
         this.usePermissions = getConfig().getBoolean("use-permissions", true);
         this.debugMode = getConfig().getBoolean("debug", false);
         this.storeArmor = getConfig().getBoolean("store-armor", true);
         this.storeInventory = getConfig().getBoolean("store-inventory", true);
         this.allowNestedSaddles = getConfig().getBoolean("allow-nested-saddles", false);
         this.requireSpecialSaddle = getConfig().getBoolean("require-special-saddle", false);
         this.craftSpecialSaddle = getConfig().getBoolean("craft-special-saddle", false);
         // Add or remove the crafting recipe for the special saddle as necessary.
         boolean found = false;
         Iterator<Recipe> it = getServer().recipeIterator();
         while (it.hasNext()) {
             Recipe r = it.next();
             if (r.equals(this.specialSaddleRecipe)) {
                 if (!craftSpecialSaddle) {
                     it.remove();
                     break;
                 } else {
                     found = true;
                 }
             }
         }
         if (craftSpecialSaddle && !found) {
             getServer().addRecipe(this.getSpecialSaddleRecipe());
         }
 
     }
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, "portable-horses", this.getFile(), Updater.UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "portable-horses", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 
     @EventHandler(priority= EventPriority.NORMAL, ignoreCancelled = true)
     public void onSaddleEvent(final InventoryClickEvent event) {
         if (!(event.getInventory() instanceof HorseInventory)) return;
         Horse horse = ((Horse) event.getInventory().getHolder());
         if (event.isShiftClick()) {
             if (event.getRawSlot() != 0) {
                 if (isPortableHorseSaddle(event.getCurrentItem())) {
                     event.setCancelled(true);
                 } else if (isEmptyPortableHorseSaddle(event.getCurrentItem()) && ((HorseInventory) event.getInventory()).getSaddle() == null) {
                     onSaddled(event, horse, event.getCurrentItem());
                 }
             } else if (event.getRawSlot() == 0 && event.getWhoClicked().getInventory().firstEmpty() != -1 && isPortableHorseSaddle(event.getCurrentItem())) {
                 // Removing a saddle by shift-click.
                 onUnsaddled(event, horse, event.getCurrentItem());
             }
         } else if (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE) {
             if (event.getRawSlot() == 0 && event.getCurrentItem().getType() == Material.AIR) {
                 if (isPortableHorseSaddle(event.getCursor())) {
                     event.setCancelled(true);
                 } else if (isEmptyPortableHorseSaddle(event.getCursor())) {
                     debug("Saddling!");
                     onSaddled(event, horse, event.getCursor());
                 }
             }
         } else if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_HALF) {
             if (event.getRawSlot() == 0 && isPortableHorseSaddle(event.getCurrentItem())) {
                 // removed a saddle.
                 onUnsaddled(event, horse, event.getCurrentItem());
             }
        } else if (event.getAction() == InventoryAction.HOTBAR_SWAP && event.getRawSlot() == 0 && isPortableHorseSaddle(event.getCurrentItem())) {
            onUnsaddled(event, horse, event.getCurrentItem());

         }
     }
 
     @EventHandler(priority=EventPriority.HIGH)
     public void onInventoryClick(final InventoryClickEvent event) {
         if (!(event.getInventory() instanceof HorseInventory)) return;
         if (allowNestedSaddles) {
             return;
         }
         Horse horse = ((Horse) event.getInventory().getHolder());
         if (!horse.isCarryingChest()) {
             return;
         }
         getServer().getScheduler().runTaskLater(this, new Runnable() {
             @Override
             public void run() {
                 for (int i=1;i<event.getInventory().getSize();i++) {
                     ItemStack s = ((HorseInventory) event.getInventory()).getItem(i);
                     if (s != null && isPortableHorseSaddle(s)) {
                         event.getInventory().setItem(i, null);
                         event.getWhoClicked().getInventory().addItem(s);
                         ((Player) event.getWhoClicked()).updateInventory();
                     }
                 }
             }
         }, 0);
     }
     public void onSaddled(InventoryClickEvent event, Horse horse, ItemStack saddle) {
         debug(horse + "Saddled.");
         if (!usePermissions || event.getWhoClicked().hasPermission("portablehorses.saddle")) {
             saveToSaddle(horse, saddle);
             horse.getInventory().setSaddle(saddle);
             event.setCurrentItem(null);
         }
     }
 
     public void onUnsaddled(InventoryClickEvent event, Horse horse, ItemStack saddle) {
         debug(horse + "Unsaddled.");
         if (!usePermissions || event.getWhoClicked().hasPermission("portablehorses.unsaddle")) {
             if (!storeArmor) {
                 if (horse.getInventory().getArmor() != null && horse.getInventory().getArmor().getType() != Material.AIR) {
                     horse.getWorld().dropItem(horse.getLocation(), horse.getInventory().getArmor());
                     horse.getInventory().setArmor(null);
                 }
             }
             if (!storeInventory && horse.isCarryingChest()) {
                 ItemStack toDrop;
                 for (int i=2;i<horse.getInventory().getContents().length;i++) {
                     toDrop = horse.getInventory().getItem(i);
                     horse.getWorld().dropItem(horse.getLocation(), toDrop);
                     horse.getInventory().setItem(i, null);
                 }
             }
             saveToSaddle(horse, saddle);
             horse.remove();
         }
     }
 
     public ItemStack getEmptyPortableHorseSaddle() {
         PlayerPickupItemEvent e;
 
         if (requireSpecialSaddle) {
             ItemStack s = new ItemStack(Material.SADDLE);
             ItemMeta meta = getServer().getItemFactory().getItemMeta(Material.SADDLE);
             meta.setDisplayName(DISPLAY_NAME);
             List<String> lore = new LinkedList<String>();
             lore.add("empty");
             meta.setLore(lore);
             s.setItemMeta(meta);
             return s;
         } else {
             return new ItemStack(Material.SADDLE);
         }
     }
 
     private boolean isEmptyPortableHorseSaddle(ItemStack currentItem) {
         if (requireSpecialSaddle) {
             if (!currentItem.hasItemMeta()) {
                 return false;
             }
             if (!DISPLAY_NAME.equals(currentItem.getItemMeta().getDisplayName())) {
                 return false;
             }
             return currentItem.getItemMeta().hasLore() && "empty".equals(currentItem.getItemMeta().getLore().get(0));
         } else {
             return !isPortableHorseSaddle(currentItem);
         }
     }
 
     private boolean isPortableHorseSaddle(ItemStack currentItem) {
         if (currentItem != null && currentItem.getType().equals(Material.SADDLE)) {
             if (currentItem.hasItemMeta()) {
                 if (currentItem.getItemMeta().hasLore()) {
                     List<String> lore = currentItem.getItemMeta().getLore();
                     if (lore.size() >= 1 && lore.get(0).startsWith(LORE_PREFIX) && lore.get(0).length() > LORE_PREFIX.length()) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event) {
         if (event.getEntity() instanceof Horse) {
             Horse h = (Horse) event.getEntity();
             if (isPortableHorseSaddle(h.getInventory().getSaddle())) {
                 h.getInventory().setSaddle(getEmptyPortableHorseSaddle());
             }
         }
     }
 
     @EventHandler
     public void onClickSaddle(PlayerInteractEvent event) {
         if (event.getItem() != null && event.getItem().getType().equals(Material.SADDLE)) {
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isPortableHorseSaddle(event.getItem())) {
                 if (INTERACTIVE_BLOCKS.contains(event.getClickedBlock().getType())) {
                     return;
                 }
                 if (event.getPlayer().hasPermission("portablehorses.spawn")) {
                     Location spawnLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                     Horse horse = (Horse) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.HORSE);
                     restoreHorseFromSaddle(event.getItem(), horse);
                     horse.getInventory().setSaddle(event.getItem());
                     event.getPlayer().setItemInHand(null);
                 } else {
                     event.getPlayer().sendMessage("Sorry, you don't have permission to spawn your horse here.");
                 }
             }
         }
     }
 
 
     public MaterialData getCraftingSupplement() {
         String mat = getConfig().getString("recipe-extra-item", "ENDER_PEARL");
         int data = -1;
         if (mat.contains(":")) {
             String[] parts = mat.split(":");
             mat = parts[0];
             data = Integer.parseInt(parts[1]);
         }
         Material material = null;
         try {
             material = Material.getMaterial(Integer.parseInt(mat));
         } catch (IllegalArgumentException ex) {
             material = Material.getMaterial(mat);
         }
         MaterialData md = new MaterialData(material);
         if (data != -1) {
             md.setData((byte) data);
         }
         return md;
     }
 }
