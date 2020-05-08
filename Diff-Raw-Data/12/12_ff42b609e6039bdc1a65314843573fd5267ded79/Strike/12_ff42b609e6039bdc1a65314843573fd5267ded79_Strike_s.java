 package me.ryall.wrath.execution;
 
 import me.ryall.wrath.Wrath;
 
 import org.bukkit.entity.Player;
 
 public class Strike extends Executioner
 {
     public boolean hasPermission(Player _player)
     {
         return Wrath.get().getPermissions().hasStrikePermission(_player);
     }
 
     public String getMessage()
     {
         return Wrath.get().getConfig().getStrikeMessage();
     }
 
     public void update(Player _target)
     {
         _target.getWorld().strikeLightning(_target.getLocation());
        
        // Make sure damage occurs before death, so that other plugins work properly.
        _target.damage(_target.getHealth() - 1);
        _target.damage(1);
     }
 }
