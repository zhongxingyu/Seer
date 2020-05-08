 package com.kolinkrewinkel.BitLimitNPCs;
 
 import de.kumpelblase2.remoteentities.RemoteEntities;
 import de.kumpelblase2.remoteentities.api.RemoteEntity;
 import de.kumpelblase2.remoteentities.api.thinking.Behavior;
 import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;
 import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookAtNearest;
 import de.kumpelblase2.remoteentities.api.thinking.goals.DesireLookRandomly;
 import de.kumpelblase2.remoteentities.entities.RemotePlayer;
 import de.kumpelblase2.remoteentities.persistence.ParameterData;
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 public class BlacksmithInteractBehavior extends InteractBehavior {
     private Plugin plugin;
 
     private static Material defaultItem = Material.DIAMOND_AXE;
 
     public BlacksmithInteractBehavior(RemoteEntity inEntity) {
         super(inEntity);
         this.plugin = inEntity.getManager().getPlugin();
 
         this.onEntityUpdate();
     }
 
     public void onEntityUpdate()
     {
         if (this.m_entity.getBukkitEntity() == null)
             return;
 
         this.m_entity.setPushable(false);
 
         Player npc = (Player)this.m_entity.getBukkitEntity();
         npc.setCanPickupItems(false);
         npc.setRemoveWhenFarAway(false);
 
        // Set tools + armor

         // Axe
         ItemStack axe = new ItemStack(defaultItem);
         axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
         npc.setItemInHand(axe);
 
         // Armor
         ItemStack[] armor = new ItemStack[4];
         armor[3] = new ItemStack(Material.LEATHER_HELMET);
         armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
         armor[1] = new ItemStack(Material.IRON_LEGGINGS);
         armor[0] = new ItemStack(Material.LEATHER_BOOTS);
         // Set it backwards just for our own sanity.
 
         for (ItemStack itemStack : armor) {
             itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10); // Give it that glowing feeling.
         }
 
         npc.getInventory().setArmorContents(armor); // Set it.
     }
 
     public void onInteract(Player inPlayer) {
         RemotePlayer behaviorEntity = (RemotePlayer) this.getRemoteEntity();
         Player npc = (Player) behaviorEntity.getBukkitEntity();
         Location npcLocation = npc.getLocation();
 
         ItemStack repairItem = inPlayer.getItemInHand();
 
         if (npc.getItemInHand().getType().getId() != defaultItem.getId()) {
             inPlayer.sendMessage(ChatColor.AQUA + npc.getDisplayName() + ChatColor.RED + " is busy!");
             return;
         } else if (repairItem.getMaxStackSize() != 1) {
             return;
         } else if (repairItem.getDurability() == 0) {
             inPlayer.sendMessage(ChatColor.RED + "Your item is fully repaired.");
             return;
         }
 
 
         npc.setItemInHand(repairItem);
 
         int indexOfPreviouslyHeldItem = inPlayer.getInventory().getHeldItemSlot();
         inPlayer.setItemInHand(new ItemStack(Material.AIR));
         //Update the equipment on the entity.
 
 
         Block closest = closestBlock(npcLocation, Material.ANVIL.getId());
 
         double distanceAway = npcLocation.distance(closest.getLocation());
         behaviorEntity.move(closest.getLocation());
 
         class FinishRepairTrip implements Runnable {
             private final Player npc;
             private final Location gaze;
             private final Player toLookAt;
 
             public FinishRepairTrip(Player npc, Location gaze, Player toLookAt) {
                 this.npc = npc;
                 this.gaze = gaze;
                 this.toLookAt = toLookAt;
             }
 
             public void run() {
                 RemoteEntity entity = RemoteEntities.getRemoteEntityFromEntity(this.npc);
                 npc.setSneaking(true);
 
                 if (this.gaze != this.npc.getLocation())
                     entity.lookAt(this.gaze);
 
                 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     public void run() {
                         npc.setSneaking(false);
 
                         npc.getWorld().playSound(npc.getLocation(), Sound.ANVIL_USE, 1F, 1F);
                         RemoteEntity entity = RemoteEntities.getRemoteEntityFromEntity(npc);
 //
 //                        DesireLookAtNearest desireLookAtNearest = entity.getMind().getMovementDesire(DesireLookAtNearest.class);
 //                        DesireLookRandomly desireLookRandomly = entity.getMind().getMovementDesire(DesireLookRandomly.class);
 //
 //                        desireLookAtNearest.startExecuting();
 //                        desireLookRandomly.startExecuting();
                         entity.lookAt(toLookAt);
                     }
                 }, 20L);
             }
         }
 
         Long wait = Math.round((distanceAway / 4) * 20);
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new FinishRepairTrip(npc, closest.getLocation(), inPlayer), wait);
 
         short zero = 0;
         repairItem.setDurability(zero);
 
         class ReturnNPC implements Runnable {
             private final Location returnLocation;
             private final RemotePlayer entity;
 
             public ReturnNPC(Location returnLocation, RemotePlayer entity) {
                 this.returnLocation = returnLocation;
                 this.entity = entity;
             }
 
             public void run() {
                 this.entity.move(returnLocation);
             }
         }
 
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnNPC(npcLocation, behaviorEntity), wait + 20L);
 
 
         class ReturnItem implements Runnable {
             private final Player player;
             private final ItemStack item;
             private final Player npc;
             private final int attemptSlot;
 
             public ReturnItem(Player player, ItemStack item, Player npc, int attemptSlot) {
                 this.player = player;
                 this.item = item;
                 this.npc = npc;
                 this.attemptSlot = attemptSlot;
             }
 
             public void run() {
                 if (this.player.getInventory().getItem(this.attemptSlot) == null && this.player.isOnline()) {
                     this.player.getInventory().setItem(this.attemptSlot, this.item);
                 } else if (this.player.getInventory().firstEmpty() != -1 && this.player.isOnline()) {
                     this.player.getInventory().setItem(this.player.getInventory().firstEmpty(), this.item);
                 } else {
                     Item droppedItem = npc.getLocation().getWorld().dropItem(npc.getLocation(), this.item);
                     Location playerLoc = this.player.getLocation();
                     droppedItem.setVelocity(new Vector(0, 0, 0));
                 }
 

                 if (item.getItemMeta().hasDisplayName()) {
                     this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired " + ChatColor.YELLOW + item.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                 } else {
                     this.player.sendMessage(ChatColor.AQUA + this.npc.getDisplayName() + ChatColor.GREEN + " repaired your " + ChatColor.YELLOW + item.getType().name().replace("_", " ").toLowerCase() + ChatColor.GREEN + ".");
                 }
 
                 ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
                 axe.addEnchantment(Enchantment.SILK_TOUCH, 1);
                 npc.setItemInHand(axe);
             }
         }
 
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ReturnItem(inPlayer, repairItem, npc, indexOfPreviouslyHeldItem), (wait * 2) + 20L);
     }
 
     private String capitalizedString(String string) {
         return Character.toUpperCase(string.charAt(0)) + string.substring(1);
     }
 
     private Block closestBlock(Location origin, Integer type) {
         World world = origin.getWorld();
 
         for (int x = origin.getBlockX() - 4; x <= origin.getBlockX() + 8; x = x + 1) {
             for (int z = origin.getBlockZ() - 4; z <= origin.getBlockZ() + 8; z = z + 1) {
                 for (int y = origin.getBlockY() - 4; y <= origin.getBlockY() + 8; y = y + 1) {
                     Block block = world.getBlockAt(x, y, z);
                     if (block.getType() == Material.ANVIL) {
                         return block;
                     }
                 }
             }
         }
 
         return world.getBlockAt(origin);
     }

    public Object[] getConstructionals()
    {
        Object[] constructionals = new Object[2];
        constructionals[0] = this.m_entity;
        constructionals[1] = this.plugin;

        return constructionals;
    }
 }
