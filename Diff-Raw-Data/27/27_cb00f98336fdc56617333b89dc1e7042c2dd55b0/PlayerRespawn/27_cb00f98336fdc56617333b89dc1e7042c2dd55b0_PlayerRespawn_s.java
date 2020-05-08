 package littlegruz.arpeegee.listeners;
 
 import littlegruz.arpeegee.ArpeegeeMain;
 import littlegruz.arpeegee.entities.RPGMagicPlayer;
 import littlegruz.arpeegee.entities.RPGMeleePlayer;
 import littlegruz.arpeegee.entities.RPGPlayer;
 import littlegruz.arpeegee.entities.RPGRangedPlayer;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerRespawn implements Listener{
 private ArpeegeeMain plugin;
    
    public PlayerRespawn(ArpeegeeMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
       // Restore weapons and levels for the melee class
      event.getPlayer().sendMessage(Integer.toString(event.getPlayer().getLevel()));
       if(plugin.getMeleePlayerMap().get(event.getPlayer().getName()) != null){
          RPGMeleePlayer rpgPlaya = plugin.getMeleePlayerMap().get(event.getPlayer().getName());
 
          event.getPlayer().setLevel(newLevel(rpgPlaya, event.getPlayer().getLevel()));
 
          // Give player back their base weapon
          event.getPlayer().getInventory().setItem(0, new ItemStack(Material.IRON_SWORD,1));
          
          if(rpgPlaya.getLevel() >= 5)
             event.getPlayer().getInventory().setItem(1, new ItemStack(Material.DIAMOND_SWORD,1));
       }
       // Restore weapons and levels for the ranged class
       else if(plugin.getRangedPlayerMap().get(event.getPlayer().getName()) != null){
          RPGRangedPlayer rpgPlaya = plugin.getRangedPlayerMap().get(event.getPlayer().getName());
 
          event.getPlayer().setLevel(newLevel(rpgPlaya, event.getPlayer().getLevel()));
 
          // Give player back their base weapon
          event.getPlayer().getInventory().setItem(0, new ItemStack(Material.BOW,1));
          event.getPlayer().getInventory().setItem(9, new ItemStack(Material.ARROW,64));
          event.getPlayer().getInventory().setItem(10, new ItemStack(Material.ARROW,64));
          
          if(rpgPlaya.getLevel() >= 7)
             event.getPlayer().getInventory().setItem(1, new ItemStack(Material.EGG,1));
       }
       // Restore weapons and levels for the magic class
       else if(plugin.getMagicPlayerMap().get(event.getPlayer().getName()) != null){
          RPGMagicPlayer rpgPlaya = plugin.getMagicPlayerMap().get(event.getPlayer().getName());
 
          event.getPlayer().setLevel(newLevel(rpgPlaya, rpgPlaya.getLevel()));
          
          // Create the base dye type first
          ItemStack is = new ItemStack(351,1);
          
          // Lightning
          is.setDurability((short)11);
          event.getPlayer().getInventory().setItem(0, is);
          
          // Heal
          if(rpgPlaya.getLevel() >= 3){
             is.setDurability((short)15);
             event.getPlayer().getInventory().setItem(1, is);
          }
          // Fireball
          if(rpgPlaya.getLevel() >= 5){
             is.setDurability((short)1);
             event.getPlayer().getInventory().setItem(2, is);
          }
          // Teleport
          if(rpgPlaya.getLevel() >= 8){
             is.setDurability((short)13);
             event.getPlayer().getInventory().setItem(3, is);
          }
          // Sheep summon
          if(rpgPlaya.getLevel() >= 10){
             is.setType(Material.WHEAT);
             event.getPlayer().getInventory().setItem(4, is);
          }
          // Advanced heal
          if(rpgPlaya.getLevel() >= 11){
             is.setType(Material.BONE);
             event.getPlayer().getInventory().setItem(5, is);
          }
          // Advanced lightning
          if(rpgPlaya.getLevel() >= 13){
             is.setType(Material.BLAZE_ROD);
             event.getPlayer().getInventory().setItem(6, is);
          }
       }
    }
 
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
       event.setDroppedExp(0);
       event.setKeepLevel(true);
       event.getDrops().removeAll(event.getDrops());
    }
    
    // Determines how many levels the player looses upon respawning
    private int newLevel(RPGPlayer rpgPlaya, int level){
       if(level <= 3){
          rpgPlaya.setLevel(1);
          return 1;
       }
       else if(level <= 7){
          rpgPlaya.setLevel(level - 2);
          return level - 2;
       }
       else{
          rpgPlaya.setLevel(level - 1);
          return level - 1;
       }
    }
 }
