 package at.junction.transmission;
 
 import org.bukkit.ChatColor;
 
 import java.util.List;
 
 public class Configuration {
     private Transmission plugin;
     boolean RATE_LIMIT;
     int MESSAGES;
     int TIME;
     List<String> MUTED_PLAYERS;
     String MUTE_MESSAGE;
     String SPAM_MESSAGE;
 
     public Configuration(Transmission instance) {
         plugin = instance;
     }
 
     public void load() {
         RATE_LIMIT = plugin.getConfig().getBoolean("rate-limit", false);
         MESSAGES = plugin.getConfig().getInt("rate-limit-messages", 100);
         TIME = plugin.getConfig().getInt("rate-limit-time", 1);
         MUTED_PLAYERS = plugin.getConfig().getStringList("muted-players");
         MUTE_MESSAGE = plugin.getConfig().getString("mute-message");
         SPAM_MESSAGE = plugin.getConfig().getString("spam-message");
     }
     public void save(){
         plugin.getConfig().set("muted-players", MUTED_PLAYERS);
     }
 }
