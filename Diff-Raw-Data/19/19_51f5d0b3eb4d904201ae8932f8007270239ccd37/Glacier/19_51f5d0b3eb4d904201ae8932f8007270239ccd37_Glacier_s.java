 package at.junction.glacier;
 
 import at.junction.glacier.database.Liquid;
 import at.junction.glacier.database.LiquidTable;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import javax.persistence.PersistenceException;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Glacier extends JavaPlugin {
     
     private final GlacierListener listener = new GlacierListener(this);
     final Configuration config = new Configuration(this);
     private final LiquidTable lt = new LiquidTable(this);
     List<Location> frozenBlocks = new ArrayList<>();
     final List<String> frozenPlayers = new ArrayList<>();
     private WorldGuardPlugin wg;
     
     @Override
     public void onEnable() {
         getServer().getPluginManager().registerEvents(listener, this);
         
         File cfile = new File(getDataFolder(), "config.yml");
 		if(!cfile.exists()) {
 			getConfig().options().copyDefaults(true);
 			saveConfig();
 		}
         
         config.load();
         setupDatabase();
         wg = getWorldGuard();
         frozenBlocks = lt.getFrozen();
     }
     
     @Override
     public void onDisable() {
         
     }
     
    void setupDatabase() {
         try {
             getDatabase().find(Liquid.class).findRowCount();
         } catch(PersistenceException ex) {
             getLogger().log(Level.INFO, "First run, initializing database.");
             installDDL();
         }
     }
     
     @Override
     public ArrayList<Class<?>> getDatabaseClasses() {
         ArrayList<Class<?>> list = new ArrayList<>();
         list.add(Liquid.class);
         return list;
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
         if(command.getName().equalsIgnoreCase("freeze")) {
                 if(frozenPlayers.contains(sender.getName())) {
                     frozenPlayers.remove(sender.getName());
                     sender.sendMessage(ChatColor.AQUA + "You're no longer placing frozen blocks.");
                 } else {
                     frozenPlayers.add(sender.getName());
                     sender.sendMessage(ChatColor.AQUA + "You're now placing frozen blocks.");
                 }
         }
         return true;
     }
     
     private WorldGuardPlugin getWorldGuard() {
         Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 
         if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
             getLogger().log(Level.INFO, "WorldGuard was not detected on the server! Some functions of Glacier may not work.");
             return null;
         }
         return (WorldGuardPlugin) plugin;
     }
     
     public void newFrozen(Block block) {
         frozenBlocks.add(block.getLocation());
         lt.newFrozen(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
     }
     
     public void delFrozen(Location loc) {
         frozenBlocks.remove(loc);
         lt.delFrozen(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
     }
     
     public boolean hasRegion(Block block) {
         return wg.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).size() != 0;
     }
     
     public boolean isBlockRegionMember(Block block, String player) {
         LocalPlayer pl = wg.wrapPlayer(getServer().getPlayer(player));
         return wg.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).isMemberOfAll(pl);
     }
     
     public boolean canFlowInRegion(Block fromBlock, Block toBlock) { // There has *got* to be a better way... Calling all pull requests.
         List<String> fIDs = new ArrayList<>();
         List<String> tIDs = new ArrayList<>();
         ApplicableRegionSet fromRegions = wg.getRegionManager(fromBlock.getWorld()).getApplicableRegions(fromBlock.getLocation());
         ApplicableRegionSet toRegions = wg.getRegionManager(fromBlock.getWorld()).getApplicableRegions(toBlock.getLocation());
         
         for(ProtectedRegion p : fromRegions) {
             fIDs.add(p.getId());
         }
         
         for(ProtectedRegion p : toRegions) {
             tIDs.add(p.getId());
         }
 
         return fIDs.equals(tIDs);
     }
 }
