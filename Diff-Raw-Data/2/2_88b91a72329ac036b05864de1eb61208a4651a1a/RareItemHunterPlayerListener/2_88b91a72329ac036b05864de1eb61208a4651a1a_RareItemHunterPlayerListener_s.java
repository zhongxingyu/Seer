 package com.ne0nx3r0.rareitemhunter.listener;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import com.ne0nx3r0.rareitemhunter.boss.Boss;
 import com.ne0nx3r0.util.FireworkVisualEffect;
 import java.util.List;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.metadata.Metadatable;
 
 public class RareItemHunterPlayerListener implements Listener
 {
     private final RareItemHunter plugin;
 
     public RareItemHunterPlayerListener(RareItemHunter plugin)
     {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onInteract(PlayerInteractEvent e)
     {        
         if(e.hasBlock())
         { 
             if(e.getClickedBlock().getType() == Material.DRAGON_EGG)
             {
                 Location lClicked = e.getClickedBlock().getLocation();
 
                 if(plugin.bossManager.isBossEgg(e.getClickedBlock()))
                 {
                     e.setCancelled(true);
                     
                     if(e.getPlayer().hasPermission("rareitemhunter.hunter.hatch"))
                     {
                         Boss boss = plugin.bossManager.hatchBoss(lClicked);
                         
                         for(Player p : lClicked.getWorld().getPlayers())
                         {
                            p.sendMessage(ChatColor.DARK_GREEN+"Legendary boss "+ChatColor.WHITE+boss.getName()+ChatColor.DARK_GREEN+" has been awakened by "+ChatColor.WHITE+e.getPlayer()+ChatColor.DARK_GREEN+"!");
                         }
                         
                         lClicked.getWorld().strikeLightningEffect(lClicked);
                     }
                     else
                     {
                         e.getPlayer().sendMessage(ChatColor.RED+"You do not have permission to awaken legendary bosses!");
                     }
                 }
             }
             else if(e.hasItem()
             && plugin.recipeManager.isCompassItem(e.getItem()))
             {
                 if(!e.getPlayer().hasPermission("rareitemhunter.hunter.compass"))
                 {
                     e.getPlayer().sendMessage(ChatColor.RED+"You do not have permission to use a legendary compass!");
                 }
                 else
                 {
                     Location lBossEgg = plugin.bossManager.getNearestBossEggLocation(e.getPlayer().getLocation());
 
                     if(lBossEgg != null)
                     {
                         e.getPlayer().setCompassTarget(lBossEgg);
 
                         e.getPlayer().sendMessage(ChatColor.DARK_GREEN+"The compass glows, then points sharply");
                     }
                     else
                     {
                         e.getPlayer().sendMessage(ChatColor.DARK_GRAY+"The compass glows for a moment, then fades...");
                     }
                 }
                 
                 e.setCancelled(true);
             }
         }
         
         plugin.propertyManager.onInteract(e.getPlayer(), e.getItem(), e);
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerDeath(PlayerDeathEvent e)
     {     
         if(e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
         {
             EntityDamageByEntityEvent edbe  = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
 
             Entity eAttacker = edbe.getDamager();
 
             if((eAttacker instanceof Arrow))
             {
                 eAttacker = ((Arrow) eAttacker).getShooter();
             }       
             if((eAttacker instanceof Fireball))
             {
                 eAttacker = ((Fireball) eAttacker).getShooter();
             }     
 
             Boss bossAttacker = plugin.bossManager.getBoss(eAttacker);
 
             if(bossAttacker != null)
             {
                 e.setDeathMessage(e.getEntity().getName()+ChatColor.DARK_RED+" was defeated by legendary boss "+ChatColor.WHITE+bossAttacker.getName()+ChatColor.DARK_RED+"!");
                 
                 int bossKills = bossAttacker.addKill();
                 
                 if(bossKills >= plugin.getConfig().getInt("bossExpireKills",10))
                 {
                     for(Player p : eAttacker.getWorld().getPlayers())
                     {
                         p.sendMessage(ChatColor.GREEN+"Legendary boss "+bossAttacker.getName()
                                 +" has had its fill of players and has left this world.");
                     }
 
                     try
                     {
                         new FireworkVisualEffect().playFirework(
                             eAttacker.getWorld(), eAttacker.getLocation(),
                             FireworkEffect
                                 .builder()
                                 .with(FireworkEffect.Type.BURST)
                                 .withColor(Color.GREEN)
                                 .build()
                         );
                     }
                     catch (Exception ex)
                     {
                         plugin.getLogger().log(Level.SEVERE, null, ex);
                     }
                     
                     plugin.bossManager.destroyBoss(bossAttacker);
                 }
                 
             }
         }
     }
     
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onCraftItem(PrepareItemCraftEvent e)
     {
         ItemStack isResult = plugin.recipeManager.getRecipeResult(e);
         
         if(isResult != null)
         {
             if(!e.getView().getPlayer().hasPermission("rareitemhunter.hunter.craft"))
             {
                 ((Player) e.getView().getPlayer()).sendMessage(ChatColor.RED+"You do not have permission to craft rare items!");
                 
                 e.getInventory().setResult(new ItemStack(Material.AIR));
             }
             else
             {
                 e.getInventory().setResult(isResult);
             }
         }
     }
     
     private final BlockFace[] bfs = new BlockFace[]{
         BlockFace.NORTH,
         BlockFace.EAST,
         BlockFace.SOUTH,
         BlockFace.WEST
     };
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamagedByEntity(EntityDamageByEntityEvent e)
     {
         if(e.getDamager() instanceof Arrow)
         {
             Arrow arrow = (Arrow) e.getDamager();
             MetadataValue itemCraftMetaData = getItemCraftMetaData(arrow,"bow");
             
             if(itemCraftMetaData != null)
             {
                 ItemStack isBow = (ItemStack) itemCraftMetaData.value();
 
                 if(isBow != null)
                 {
                     Player pShooter = (Player) getItemCraftMetaData(arrow,"shooter").value();
 
                     plugin.propertyManager.onArrowHitEntity(pShooter,isBow,e);
                 }
             }
         }
         else if(e.getDamager() instanceof Player)
         {
             Player attacker = (Player) e.getDamager();
 
             if(attacker.getItemInHand() != null && attacker.getItemInHand().getType() != Material.AIR)
             {
                 plugin.propertyManager.onDamagedOtherEntity(attacker,e);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onEntityShootBow(EntityShootBowEvent e)
     {
         if ((e.getEntity() instanceof Player))
         {
             //Mark which bow shot the arrow
             Arrow arrow = (Arrow) e.getProjectile();
             Player shooter = (Player) e.getEntity();
             
             arrow.setMetadata("shooter", new FixedMetadataValue(plugin, shooter));
             arrow.setMetadata("bow", new FixedMetadataValue(plugin, shooter.getItemInHand()));
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerInteractedWithEntity(PlayerInteractEntityEvent e)
     {
         plugin.propertyManager.onInteractEntity(e);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerInventoryClick(InventoryClickEvent e)
     {
         if(e.getSlotType() == InventoryType.SlotType.ARMOR)
         {
             if(e.getCursor() != null && e.getCursor().getType() != Material.AIR)//equipped item
             {
                 plugin.propertyManager.onEquip(e);
             }
             if(e.getCurrentItem().getType() != Material.AIR)//unequipped item
             { 
                 plugin.propertyManager.onUnequip(e);
             }
         }
     }
     
     public MetadataValue getItemCraftMetaData(Metadatable holder,String key)
     {
         List<MetadataValue> metadata = holder.getMetadata(key);
         
         for(MetadataValue mdv : metadata)
         {
             if(mdv.getOwningPlugin().equals(plugin))
             {
                 return mdv;
             }
         }
         
         return null;
     }
     
     @EventHandler(priority= EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerJoin(PlayerJoinEvent e)
     {
         plugin.propertyManager.onJoin(e.getPlayer());
         
         if(plugin.UPDATE_AVAILABLE)
         {
             if(e.getPlayer().hasPermission("rareitemhunter.admin.notify"))
             {
                 e.getPlayer().sendMessage(ChatColor.GREEN+"An update for RareItemHunter is available: "+ChatColor.RESET+plugin.UPDATE_STRING);
             }
         }
     }
     
     @EventHandler(priority= EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerQuit(PlayerQuitEvent e)
     {
         plugin.propertyManager.revokeAllItemProperties(e.getPlayer());
     }
 }
