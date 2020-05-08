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
        _target.damage(_target.getHealth());
     }
 }
