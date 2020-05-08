 package blakesteel.MusicService;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.Faction;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @author Palisade
  */
 public class SupportFactions {
     static public Plugin getPlugin(Server server) throws PluginUnavailableException {
         Plugin plugin = server.getPluginManager().getPlugin("Factions");
         
         if (plugin == null) {
             throw new PluginUnavailableException();
         }
         
         return plugin;
     }
     
     static public Faction getFactionAtLocation(Server server, Player player)
             throws PluginUnavailableException
     {
         Plugin factions = getPlugin(server);
         
         if (factions != null) {
             // Get a faction located at the player's position.
             return Board.getFactionAt(new FLocation(player));
         }
         
         return null;
     }
     
     static public Faction getFactionAtLocation(Server server, Location location)
             throws PluginUnavailableException {
         Plugin factions = getPlugin(server);
 
         if (factions != null) {
             // Get a faction located at the player's position.
             return Board.getFactionAt(new FLocation(location));
         }
         
         return null;
     }
 
     static public boolean isPlayerFactionAdmin(Server server, Faction faction, Player player)
             throws PluginUnavailableException {
         Plugin factions = getPlugin(server);
         
         if (factions != null) {
             // Faction exists?
             if (faction != null) {
                 // Get the faction's admin.
                 FPlayer factionAdmin = faction.getFPlayerAdmin();
 
                if (factionAdmin != null && factionAdmin.getPlayer().equals(player)) {
                    return true;
                 }
             }
         }
         
         return false;
     }
 }
