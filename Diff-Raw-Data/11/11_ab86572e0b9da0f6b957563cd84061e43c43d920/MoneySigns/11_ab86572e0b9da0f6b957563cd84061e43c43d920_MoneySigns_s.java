 package uk.co.drnaylor.moneysigns;
 
 import java.util.Map;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MoneySigns extends JavaPlugin
 {
     public static Map<Player, MoneyUser> users;
     public static MoneySigns plugin;
     
     @Override
     public void onEnable() {
         MoneySigns.plugin = this;
     }
     
     
 }
