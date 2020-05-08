 package de.whichdesign.selfmaderegion.commands;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.item.Items;
 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 
 import de.whichdesign.selfmaderegion.Configuration;
 import de.whichdesign.selfmaderegion.Constants;
 import de.whichdesign.selfmaderegion.Helper;
 import de.whichdesign.selfmaderegion.Region;
 import de.whichdesign.selfmaderegion.RegionSize;
 import de.whichdesign.selfmaderegion.plugins.Vault;
 import de.whichdesign.selfmaderegion.plugins.WorldGuard;
 
 /**
  * 
  * @author Blockhaus2000
  */
 public class Plot implements CommandExecutor {
     private Plugin plugin;
 
     public Plot(Plugin plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Executes the command "/plot <start|ready|leave>"
      * 
      */
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (!Helper.isPlayer(sender)) {
             return true;
         }
 
         if (!Helper.isLengthEquals(args, 1, sender)) {
             return true;
         }
 
         Player p = (Player) sender;
         String path = p.getName().toLowerCase() + "|" + p.getWorld().getName().toLowerCase();
 
         // plot start
         if (args[0].equalsIgnoreCase("start")) {
             if (!Helper.hasPermission(sender, "selfmaderegion.plot.start")) {
                 return true;
             }
 
             plotStart(sender, p, args, path);
             return true;
         }
 
         // plot ready
         else if (args[0].equalsIgnoreCase("ready")) {
             if (!Helper.hasPermission(sender, "selfmaderegion.plot.ready")) {
                 return true;
             }
 
             plotReady(sender, p, args, path);
             return true;
         }
 
         // plot leave
         else if (args[0].equalsIgnoreCase("leave")) {
             if (!Helper.hasPermission(sender, "selfmaderegion.plot.leave")) {
                 return true;
             }
 
             plotLeave(sender, p, path);
             return true;
         }
 
         Helper.sendMessage(sender, Constants.UnknownArguments);
         return true;
     }
 
     /**
      * Executes the command "/plot start"
      * 
      * @param sender
      * @param args
      * @param path
      */
     private void plotStart(CommandSender sender, Player p, String[] args, String path) {
         // Copy inventory
         ItemStack[] inv = p.getInventory().getContents();
 
         // Clear Inventory of 'p' and gives add the Wand-Item
         p.getInventory().clear();
         p.getInventory().setItemInHand(new ItemStack(Items.itemById(Configuration.Wand).getType()));
 
         // Information messages
         Helper.sendMessage(sender, Constants.InfoSelectPositions);
         Helper.sendMessage(sender, Constants.InfoPlotLeave);
         Helper.sendMessage(sender, Constants.InfoFly);
 
         // Allows player to fly
        if (Configuration.PlayerIsAllowedToFlyWhileInSelectionMode
                .get(Vault.permission.getPrimaryGroup(p.getWorld(), p.getName()))) {
             p.setAllowFlight(true);
         }
 
         // Saves inventory
         Configuration.inventorys.put(path, inv);
         plugin.saveConfig();
 
         // Add player
         Configuration.players.add(path);
         return;
     }
 
     /**
      * Executes the command "/plot ready"
      * 
      * @param sender
      * @param args
      * @param path
      */
     private void plotReady(CommandSender sender, Player p, String[] args, String path) {
         Region region = Configuration.points.get(path);
 
         // Return cases
         if (Configuration.players.contains(path) && Configuration.inventorys.containsKey(path)) {
             if (!Configuration.points.containsKey(path) || !region.isSet()) {
                 Helper.sendMessage(sender, Constants.MarkTwoPoints);
                 return;
             }
         } else {
             Helper.sendMessage(sender, Constants.ExecutePlotStartCommandFirst);
             return;
         }
 
         Player player = plugin.getServer().getPlayer(p.getName());
         World world = plugin.getServer().getWorld(player.getWorld().getName());
 
         String playerName = player.getName().toLowerCase();
         String worldName = world.getName().toLowerCase();
         String primaryGroup = Vault.permission.getPrimaryGroup(world, playerName).toLowerCase();
 
         // Check size
         RegionSize regionsize = Configuration.RegionSize.get(primaryGroup).get(worldName);
 
         if (region.getLength() > regionsize.getMaxLength() || region.getWidth() > regionsize.getMaxWidth()) {
             Helper.sendMessage(sender, Constants.RegionTooBig);
             Helper.sendMessage(sender, Constants.MarkOtherRegion);
             return;
         } else if (region.getLength() < regionsize.getMinLength() || region.getWidth() < regionsize.getMinWidth()) {
             Helper.sendMessage(sender, Constants.RegionTooSmall);
             Helper.sendMessage(sender, Constants.MarkOtherRegion);
             return;
         }
 
         // Load region counter
         int regions = Helper.loadRegions(path, plugin);
 
         // Check max regions
         int maxRegions = Configuration.MaxRegions.get(primaryGroup).get(worldName);
         if (regions >= maxRegions && maxRegions != -1) {
             Helper.sendMessage(sender, Constants.MaxRegionsReached.replace("%world%", world.getName()));
             Helper.removeUnusedPath(path);
             Helper.disableFlying(player);
             return;
         }
 
         // Load WorldGuard
         WorldGuardPlugin worldguard = new WorldGuard(plugin).getWorldGuard();
 
         if (worldguard == null) {
             return;
         }
 
         RegionManager regionmanager = worldguard.getRegionManager(world);
 
         // Create Region Configuration
         BlockVector pt1 = new BlockVector(region.getMinX(), region.getMinY(), region.getMinZ());
         BlockVector pt2 = new BlockVector(region.getMaxX(), region.getMaxY(), region.getMaxZ());
         ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(Constants.PluginName + "_" + p.getName() + "_"
                 + (Configuration.regions.get(path) + 1), pt1, pt2);
 
         // Economy
         double pricePerSquareBlock = Configuration.PricePerSquareBlock.get(primaryGroup).get(worldName);
         if (pricePerSquareBlock != 0) {
             double price = Double.parseDouble(Integer.toString(region.area())) * pricePerSquareBlock;
             Economy economy = Vault.economy;
 
             if (!economy.hasAccount(playerName, worldName)) {
                 Helper.sendMessage(sender, Constants.NoEconomyAccount);
                 Helper.sendMessage(sender, Constants.NotAbleToBuy);
                 return;
             }
 
             if (!economy.has(playerName, worldName, price)) {
                 Helper.sendMessage(sender, Constants.NotEnoughMoney);
                 return;
             }
 
             economy.withdrawPlayer(playerName, worldName, price);
         }
 
         // Save Region and edit basic options
         protectedRegion.getOwners().addPlayer(p.getName());
         regionmanager.addRegion(protectedRegion);
         try {
             regionmanager.save();
         } catch (ProtectionDatabaseException ex) {
             Helper.error(ex, plugin, sender, false);
             Helper.sendMessage(sender, "Cannot creat region");
             return;
         }
 
         // Feedback
         Helper.sendMessage(sender, Constants.RegionCreated);
 
         // Denys the player to fly
         Helper.disableFlying(player);
 
         // Adds the old Inventory (from Config)
         p.getInventory().setContents(Configuration.inventorys.get(path));
 
         // Increments region counter
         Configuration.regions.put(path, regions + 1);
 
         // Removes unused entrys
         Helper.removeUnusedPath(path);
         return;
     }
 
     /**
      * Executes the command "/plot leave"
      * 
      * @param sender
      * @param path
      */
     private void plotLeave(CommandSender sender, Player p, String path) {
         Helper.sendMessage(sender, Constants.InfoSelectionModeLeft);
         Helper.disableFlying(p);
         Helper.removeUnusedPath(path);
     }
 }
