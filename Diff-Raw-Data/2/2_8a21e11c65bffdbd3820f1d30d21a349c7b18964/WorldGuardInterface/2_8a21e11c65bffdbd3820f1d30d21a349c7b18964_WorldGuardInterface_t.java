 package no.runsafe.worldguardbridge;
 
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import no.runsafe.framework.player.RunsafePlayer;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 public class WorldGuardInterface
 {
     private Server server;
     private WorldGuardPlugin worldGuard;
 
     public WorldGuardInterface(Server server)
     {
         this.server = server;
     }
 
     private WorldGuardPlugin getWorldGuard(Server server)
     {
         Plugin plugin = server.getPluginManager().getPlugin("WorldGuard");
 
        if (plugin == null || !(plugin instanceof WorldGuardPlugin))
             return null;
 
         return (WorldGuardPlugin) plugin;
     }
 
     public boolean serverHasWorldGuard()
     {
         if (this.worldGuard == null)
             this.getWorldGuard(server);
 
         if (this.worldGuard != null)
             return true;
 
         return false;
     }
 
     public boolean isInPvPZone(RunsafePlayer player)
     {
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld().getRaw());
         ApplicableRegionSet set = regionManager.getApplicableRegions(player.getRaw().getLocation());
 
         return set.allows(DefaultFlag.PVP);
     }
 }
