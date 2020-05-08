 package littlegruz.arpeegee.listeners;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntitySmallFireball;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 public class PlayerInteract implements Listener{
    private ArpeegeeMain plugin;
    
    public PlayerInteract(ArpeegeeMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
       Player playa = event.getPlayer();
 
       // Casting weapon for "Flash"
       if(playa.getItemInHand().getData().toString().contains("MAGENTA DYE")
             && event.getAction().toString().contains("RIGHT_CLICK")
             && plugin.getMagicPlayerMap().get(playa.getName()) != null){
          event.setCancelled(true);
          
          if(!plugin.getMagicPlayerMap().get(playa.getName()).isTeleportReady()){
             playa.sendMessage("Teleport is still on cooldown");
             return;
          }
          else{
             ItemStack is = new ItemStack(351,1);
             is.setDurability((short)13);
             playa.getInventory().remove(is);
             plugin.giveCooldown(playa, "tele", 10);
             plugin.getMagicPlayerMap().get(playa.getName()).setTeleportReadiness(false);
          }
          
          HashSet<Byte> hs = new HashSet<Byte>();
          int spell;
          Block block;
          Location loc;
          
          spell = (int) plugin.getMagicPlayerMap().get(playa.getName()).getSubClassObject().getSpell();
    
          hs.add((byte)0); //Air
          hs.add((byte)8); //Flowing water
          hs.add((byte)9); //Stationary water
          hs.add((byte)20); //Glass
          hs.add((byte)101); //Iron bar
          hs.add((byte)102); //Glass pane
          
          block = playa.getTargetBlock(hs, 3 * spell);
          loc = block.getLocation();
          
          //playa.sendMessage(block.getType().toString());
          
          if(block.getType().compareTo(Material.AIR) != 0
                && block.getType().compareTo(Material.WATER) != 0
                && block.getType().compareTo(Material.STATIONARY_WATER) != 0
                && block.getType().compareTo(Material.GLASS) != 0
                && block.getType().compareTo(Material.THIN_GLASS) != 0
                && block.getType().compareTo(Material.IRON_FENCE) != 0){
             loc.setY(loc.getY() + 1.5);
             
             if(loc.getBlock().getType().compareTo(Material.WATER) == 0
                   || loc.getBlock().getType().compareTo(Material.STATIONARY_WATER) == 0
                   || loc.getBlock().getType().compareTo(Material.AIR) == 0){
                playa.teleport(new Location(loc.getWorld(), loc.getX(),
                      loc.getY(), loc.getZ(), playa.getLocation().getYaw(),
                      playa.getLocation().getPitch()));
                playa.sendMessage("*Zoom*");
             }
             else
                playa.sendMessage("You can not flash to there");
          }
          else
             playa.sendMessage("You can not flash that far!");
       }
       // Lightning (single target) spell
       else if(playa.getItemInHand().getData().toString().contains("YELLOW DYE")
             && plugin.getMagicPlayerMap().get(playa.getName()) != null){
          event.setCancelled(true);
          playa.setLevel(playa.getLevel() - 1);
         plugin.getMagicPlayerMap().get(playa.getName()).setLevel(playa.getLevel());
          
          if(!plugin.getMagicPlayerMap().get(playa.getName()).isLightningReady()){
             playa.sendMessage("Lightning is still on cooldown");
             return;
          }
          else{
             ItemStack is = new ItemStack(351,1);
             is.setDurability((short)11);
             playa.getInventory().remove(is);
             plugin.giveCooldown(playa, "light", 1.5);
             plugin.getMagicPlayerMap().get(playa.getName()).setLightningReadiness(false);
          }
          
          callThor(playa, false);
       }
       // Lightning (area) spell
       else if(playa.getItemInHand().getType().compareTo(Material.BLAZE_ROD) == 0
             && plugin.getMagicPlayerMap().get(playa.getName()) != null){
          event.setCancelled(true);
         playa.setLevel(playa.getLevel() + 1);
         plugin.getMagicPlayerMap().get(playa.getName()).setLevel(playa.getLevel());
          
          if(!plugin.getMagicPlayerMap().get(playa.getName()).isAdvLightningReady()){
             playa.sendMessage("Advanced lightning is still on cooldown");
             return;
          }
          else{
             playa.getInventory().remove(Material.BLAZE_ROD);
             plugin.giveCooldown(playa, "advLight", 4);
             plugin.getMagicPlayerMap().get(playa.getName()).setAdvLightningReadiness(false);
          }
          
          callThor(playa, true);
       }
       // Melancholy. Spawns sheep around mage.
       else if(playa.getItemInHand().getType().compareTo(Material.WHEAT) == 0
             && event.getAction().toString().contains("RIGHT_CLICK")
             && plugin.getMagicPlayerMap().get(playa.getName()) != null){
          event.setCancelled(true);
          
          if(!plugin.getMagicPlayerMap().get(playa.getName()).isSheepReady()){
             playa.sendMessage("Sheep summon is still on cooldown");
             return;
          }
          else{
             playa.getInventory().remove(Material.WHEAT);
             plugin.giveCooldown(playa, "baaa", 15);
             plugin.getMagicPlayerMap().get(playa.getName()).setSheepReadiness(false);
          }
          
          int level;
          Location loc = event.getPlayer().getLocation();
          
          level = (int) plugin.getMagicPlayerMap().get(playa.getName()).getLevel();
          if(level >= 10){
             loc.setY(loc.getY() + 1.5);
             loc.setX(loc.getX() + 1);
             loc.getWorld().spawnCreature(loc, EntityType.SHEEP);
             loc.setX(loc.getX() - 2);
             loc.getWorld().spawnCreature(loc, EntityType.SHEEP);
             loc.setX(loc.getX() + 1);
             loc.setZ(loc.getZ() + 1);
             loc.getWorld().spawnCreature(loc, EntityType.SHEEP);
             loc.setZ(loc.getZ() - 2);
             loc.getWorld().spawnCreature(loc, EntityType.SHEEP);
          }
       }
       // This fireball creation code is based off MadMatt199's code (https://github.com/madmatt199/GhastBlast)
       // Casting weapon to launch a fireball
       else if(playa.getItemInHand().getData().toString().contains("RED DYE")
             && event.getAction().toString().contains("RIGHT_CLICK")
             && plugin.getMagicPlayerMap().get(playa.getName()) != null){
          event.setCancelled(true);
          
          if(!plugin.getMagicPlayerMap().get(playa.getName()).isFireballReady()){
             playa.sendMessage("Fireball is still on cooldown");
             return;
          }
          else{
             ItemStack is = new ItemStack(351,1);
             is.setDurability((short)1);
             playa.getInventory().remove(is);
             plugin.giveCooldown(playa, "fire", 5);
             plugin.getMagicPlayerMap().get(playa.getName()).setFireballReadiness(false);
          }
          
          Vector dir = playa.getLocation().getDirection().multiply(10);
          Location loc = playa.getLocation();
          
          EntityLiving entityPlaya = ((CraftPlayer) playa).getHandle();
          EntitySmallFireball fireball = new EntitySmallFireball(
                ((CraftWorld) playa.getWorld()).getHandle(), entityPlaya,
                dir.getX(), dir.getY(), dir.getZ());
          
          // Spawn the fireball a bit up and away from the player
          fireball.locX = loc.getX() + (dir.getX()/5.0);
          fireball.locY = loc.getY() + (playa.getEyeHeight()/2.0) + 0.5;
          fireball.locZ = loc.getZ() + (dir.getZ()/5.0);
          dir = dir.multiply(10);
          
          ((CraftWorld) playa.getWorld()).getHandle().addEntity(fireball);
 
          playa.sendMessage("*Fwoosh*");
       }
       // Active berserk mode if player has gained enough rage
       else if(event.getAction().toString().contains("RIGHT_CLICK")
             && plugin.getMeleePlayerMap().get(playa.getName()) != null
             && (playa.getItemInHand().getType().compareTo(Material.IRON_SWORD) == 0
                   || playa.getItemInHand().getType().compareTo(Material.DIAMOND_SWORD) == 0)){
          final String pName = playa.getName();
          
          if(plugin.getMeleePlayerMap().get(pName) != null){
             if(plugin.getMeleePlayerMap().get(pName).getRage() == 100){
                playa.sendMessage("RAAAAGE (Berserker mode activated)");
                plugin.getMeleePlayerMap().get(pName).setRage(0);
                plugin.getBerserkMap().put(pName, pName);
                
                // 10 seconds of Berserker mode (increased damage and sword bonuses)
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                   public void run() {
                      plugin.getBerserkMap().remove(pName);
                      plugin.getServer().getPlayer(pName).sendMessage("Berserker mode deactivated");
                   }
               }, 200L);
             }
             else
                playa.sendMessage("Not enough rage. Current rage: " + Integer.toString(plugin.getMeleePlayerMap().get(pName).getRage()));
          }
          else
             playa.sendMessage("Your class does not use rage...why do you have this weapon?");
       }
       else if(playa.getItemInHand().getType().compareTo(Material.IRON_SWORD) == 0
             && plugin.getMeleePlayerMap().get(playa.getName()) != null)
          event.getPlayer().getInventory().setItem(playa.getInventory().getHeldItemSlot(), new ItemStack(Material.IRON_SWORD,1));
       else if(playa.getItemInHand().getType().compareTo(Material.DIAMOND_SWORD) == 0
             && plugin.getMeleePlayerMap().get(playa.getName()) != null)
          event.getPlayer().getInventory().setItem(playa.getInventory().getHeldItemSlot(), new ItemStack(Material.DIAMOND_SWORD,1));
       else if(playa.getItemInHand().getType().compareTo(Material.BOW) == 0
             && plugin.getRangedPlayerMap().get(playa.getName()) != null)
          event.getPlayer().getInventory().setItem(playa.getInventory().getHeldItemSlot(), new ItemStack(Material.BOW,1));
       else if(playa.getItemInHand().getType().compareTo(Material.EGG) == 0
             && plugin.getRangedPlayerMap().get(playa.getName()) != null){
          if(!plugin.getRangedPlayerMap().get(playa.getName()).isEggReady())
             playa.sendMessage("Egg is still on cooldown");
          else{
             plugin.giveCooldown(playa, 5);
             plugin.getRangedPlayerMap().get(playa.getName()).setEggReadiness(false);
          }
       }
       
    }
    
    /* The ranged entity seeking code is borrowed from code listed by
     * DirtyStarfish on the bukkit.org forums (with modifications)*/
    private void callThor(Player playa, boolean area){
       Location loc;
       Block block;
       int bx, by, bz, range;
       final int spell;
       double ex, ey, ez;
       BlockIterator bItr;
       ArrayList<LivingEntity> enemies = new ArrayList<LivingEntity>();
       
       // Base range is 10 blocks plus the casters spell ability
       spell = (int) plugin.getMagicPlayerMap().get(playa.getName()).getSubClassObject().getSpell();
       range = 10 + spell;
       
       for(Entity e : playa.getNearbyEntities(range, range, range)) {
          if (plugin.isEnemy(e)) {
             enemies.add((LivingEntity)e);
          }
       }
       
       bItr = new BlockIterator(playa.getLocation(), 0, range);
       
       while (bItr.hasNext()) {
          block = bItr.next();
          bx = block.getX();
          by = block.getY();
          bz = block.getZ();
          for (LivingEntity e : enemies) {
             loc = e.getLocation();
             ex = loc.getX();
             ey = loc.getY();
             ez = loc.getZ();
             // If entity is within the boundaries then it is the one being looked at
             if ((bx - 0.75 <= ex && ex <= bx + 0.75) && (bz - 0.75 <= ez && ez <= bz + 0.75) && (by - 1 <= ey && ey <= by + 1)){
                loc.setY(loc.getY() + 1);
                loc.getWorld().strikeLightningEffect(loc);
                
                e.damage(spell);
                if(!area){
                   playa.sendMessage("*Zap*");
                }
                else{
                   final ArrayList<LivingEntity> nearEnemies = new ArrayList<LivingEntity>();
                   
                   playa.sendMessage("*Zap zap zap*");
 
                   nearEnemies.add(e);
                   for(Entity victims : e.getNearbyEntities(5, 5, 5)) {
                      if (plugin.isEnemy(victims)) {
                         nearEnemies.add((LivingEntity) victims);
                      }
                   }
                   
                   plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                      public void run() {
                         for(LivingEntity e : nearEnemies) {
                            Location enemyLoc = e.getLocation();
                            enemyLoc.setY(enemyLoc.getY() + 1);
                            enemyLoc.getWorld().strikeLightningEffect(enemyLoc);
                            e.damage(spell/2);
                         }
                      }
                  }, 20L);
                }
                return;
             }
          }
       }
    }
 }
