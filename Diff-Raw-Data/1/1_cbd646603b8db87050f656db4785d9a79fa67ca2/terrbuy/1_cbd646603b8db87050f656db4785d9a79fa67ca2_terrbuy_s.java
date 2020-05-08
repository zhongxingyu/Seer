 package org.gamegen;
 
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldguard.LocalPlayer;
 import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public class terrbuy extends JavaPlugin {
         protected FileConfiguration config;
     @SuppressWarnings("NonConstantLogger")
 	Logger log;
 	WorldGuardPlugin worldGuard;
 	WorldEditPlugin worldEdit;
 	ProtectedRegion region;
 	
     @Override
     @SuppressWarnings("CallToThreadDumpStack")
 	public void onEnable(){
 		log = this.getLogger();
                 config = getConfig();
                 config.options().copyDefaults(true);
                 saveConfig();
 		worldGuard = getWorldGuard();
 		try {
 			worldEdit = worldGuard.getWorldEdit();
 		} catch (CommandException e) {
 			e.printStackTrace();
 		}
 	}
 	
     @Override
 	public void onDisable() {
 	}
 	
 	private WorldGuardPlugin getWorldGuard() {
 	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
 	 
 	    // WorldGuard may not be loaded
 	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
 	        return null; // Maybe you want throw an exception instead
 	    }
 	 
 	    return (WorldGuardPlugin) plugin;
 	}
 	
     @Override
     @SuppressWarnings("CallToThreadDumpStack")
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		Inventory invetory = player.getInventory();
 		Location location = player.getLocation();
 		World world = location.getWorld();
 		LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
 		RegionManager regionManager = worldGuard.getRegionManager(world);
 		DefaultDomain owners = new DefaultDomain();
                 PermissionManager permissionManager = PermissionsEx.getPermissionManager();
 		if(cmd.getName().equalsIgnoreCase("terrbuy")){
 			if(permissionManager.has(player, "terrbuy.buy")){
 				Material material = Material.getMaterial(config.getInt("id"));
 				Block block = player.getLocation().getBlock().getRelative(0, -1, 0);
 				Vector pt = toVector(block);
 				ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
 				for (ProtectedRegion each : set) {
 					if(!each.isOwner(localPlayer)){
 						if((set.getFlag(DefaultFlag.BUYABLE) == true) && (set.getFlag(DefaultFlag.PRICE) != null)){
 							int cost = set.getFlag(DefaultFlag.PRICE).intValue();
 							ItemStack money = new ItemStack(material, cost);
 							if(invetory.contains(material, cost)){
 								if(invetory.removeItem(money) != null){
 									owners.addPlayer(player.getName());
 									each.setOwners(owners);
 									if(each.isOwner(localPlayer)){
 										each.setFlag(DefaultFlag.BUYABLE, false);
                                                                                 log.info(player.getDisplayName()+" is now owner of "+each.getId()+" region!");
 										//log.log(Level.INFO, "{0} is now owner of {1} region!", new Object[]{player.getDisplayName(), each.getId()});
 										//not working, i'm going sleep
                                                                                 player.sendMessage("You are now owner of "+each.getId()+" region!");
 									} else player.sendMessage("An error has occurred!");
 								} else player.sendMessage("Can't get "+cost+" money from your inventory!");
 							} else player.sendMessage("You have not "+cost+" money!");
 						}  else player.sendMessage("You can't buy this region!");
 					} else player.sendMessage("You already buyed this region!");
 				}
 			} else player.sendMessage("You not permited to use this command");
 			return true;
 		}
 		if(cmd.getName().equalsIgnoreCase("terrclaim") && args.length > 0){
 			
 			if(permissionManager.has(player, "terrbuy.claim")){
 				Selection sel = worldEdit.getSelection(player);
 				owners.addPlayer(player.getName());
 				if (sel instanceof Polygonal2DSelection) {
 					Polygonal2DSelection polySel = (Polygonal2DSelection) sel;
 					int minY = polySel.getNativeMinimumPoint().getBlockY();
 					int maxY = polySel.getNativeMaximumPoint().getBlockY();
 					region = new ProtectedPolygonalRegion(args[0], polySel.getNativePoints(), minY, maxY);
 				} else if (sel instanceof CuboidSelection) {
 					BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
 					BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
 					region = new ProtectedCuboidRegion(args[0], min, max);
 				}
 				region.setOwners(owners);
 				if(args.length == 2 && args[1] != null){
 					region.setFlag(DefaultFlag.BUYABLE, true);
 					region.setFlag(DefaultFlag.PRICE, Double.valueOf(args[1]));
 					player.sendMessage("Region "+args[0]+" was create and set buyable with price = "+args[1]+" !");
 				} else player.sendMessage("Region "+args[0]+" was create!");
 				regionManager.addRegion(region);
 				try {
 					regionManager.save();
 				} catch (ProtectionDatabaseException e) {
 					e.printStackTrace();
 				}
 			} else player.sendMessage("You not permited to use this command");
 			return true;
 			}
 		return false; 
 	}
 	
 }
