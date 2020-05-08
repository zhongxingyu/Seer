 package me.p000ison.MobSpawnerEggChanger;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class SECPlayerListener implements Listener {
 
     private MobSpawnerEggChanger plugin;
 
     public SECPlayerListener(MobSpawnerEggChanger plugin)
     {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event)
     {
         Player player = event.getPlayer();
         ItemStack iih = event.getItem();
         Block block = event.getClickedBlock();
 
         if (iih != null && block != null && event.getAction() != null) {
             if (player.isSneaking()) {
                 if (player.hasPermission("sec.spawnerchange")) {
                     if (block.getType() == Material.MOB_SPAWNER && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                         try {
                             String type = EntityType.fromId(iih.getDurability()).getName().toLowerCase();
 
                             if (plugin.isPerMobPermissions()) {
                                 if (player.hasPermission("sec.mob." + type)) {
                                     changeSpawner(block, player, iih);
                                 } else {
                                     player.sendMessage("You don't have permission to do that!");
                                 }
                             } else {
                                 changeSpawner(block, player, iih);
                             }
 
                         } catch (RuntimeException ex) {
                             player.sendMessage(color(plugin.getFailedSpawnerEggs(), (short) 0));
                         }
 
                         event.setCancelled(true);
                     }
                 }
             }
         }
     }
 
     @SuppressWarnings("deprecation")
     public void changeSpawner(Block block, Player player, ItemStack iih)
     {
         Short dur = iih.getDurability();
         EntityType type = EntityType.fromId(dur);
         String typename = type.getName().toLowerCase();
        boolean bypass = player.hasPermission("sec.bypass." + typename) || player.hasPermission("sec.bypass.*");
 
         if (dur != 0) {
             if (iih.getType() == Material.MONSTER_EGG) {
                 if (bypass || iih.getAmount() >= plugin.getSpawnerEggs(typename)) {
                     ((CreatureSpawner) block.getState()).setSpawnedType(type);
                     player.sendMessage(color(plugin.getSpawnerMesssage(), dur).replace("%entity", typename));
 
                     if (!bypass) {
                         remove(player.getInventory(), new ItemStack(Material.MONSTER_EGG), plugin.getSpawnerEggs(typename), dur);
                         player.updateInventory();
                     }
                 } else {
                     player.sendMessage(color(plugin.getNotEnoughSpawnerEggs(), iih.getDurability()).replace("%entity", EntityType.fromId(dur).toString()));
                 }
             }
         }
     }
 
     public String color(String text, short dur)
     {
         return text.replaceAll("&(?=[0-9a-fA-FkK])", "\u00a7");
     }
 
     public static int remove(Inventory inv, ItemStack item, int amount, short durability)
     {
         amount = (amount > 0 ? amount : 1);
         Material itemMaterial = item.getType();
 
         int first = inv.first(itemMaterial);
         if (first == -1) {
             return amount;
         }
 
         for (int slot = first; slot < inv.getSize(); slot++) {
             if (amount <= 0) {
                 return 0;
             }
 
             ItemStack currentItem = inv.getItem(slot);
             if (currentItem == null || currentItem.getType() == Material.AIR) {
                 continue;
             }
 
             if (equals(currentItem, item, durability)) {
                 int currentAmount = currentItem.getAmount();
                 if (amount == currentAmount) {
                     currentItem = null;
                     amount = 0;
                 } else if (amount < currentAmount) {
                     currentItem.setAmount(currentAmount - amount);
                     amount = 0;
                 } else {
                     currentItem = null;
                     amount -= currentAmount;
                 }
                 inv.setItem(slot, currentItem);
             }
         }
         return amount;
     }
 
     private static boolean equals(ItemStack i, ItemStack item, short durability)
     {
         return i != null
                 && i.getType() == item.getType()
                 && (durability == -1 || i.getDurability() == durability);
     }
 }
