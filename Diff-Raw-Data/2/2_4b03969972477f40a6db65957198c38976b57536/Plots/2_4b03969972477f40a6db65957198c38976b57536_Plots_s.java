 package us.fitzpatricksr.cownet;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.BukkitPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import cosine.boseconomy.BOSEconomy;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import us.fitzpatricksr.cownet.plots.BlockUtils;
 import us.fitzpatricksr.cownet.plots.InfinitePlotClaim;
 import us.fitzpatricksr.cownet.plots.PlayerCenteredClaim;
 import us.fitzpatricksr.cownet.plots.PlotsChunkGenerator;
 import us.fitzpatricksr.cownet.utils.CowNetThingy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class Plots extends CowNetThingy {
     private WorldGuardPlugin worldGuard;
     private BOSEconomy economy;
     private NoSwearing noSwearingMod;
     private PlayerCenteredClaim pcc;
     private InfinitePlotClaim ipc;
     private int maxPlots = 3;
 
     private int plotCost = 100;
     private int plotSize = 64;
     private int plotHeight = 20;
     private Material plotBase = Material.STONE;
     private Material plotSurface = Material.GRASS;
     private Material plotPath = Material.DOUBLE_STEP;
 
     /**
      * Interface for different type of claim shapes and decorations
      */
     public interface AbstractClaim {
         //define the region of the claim
         public ProtectedRegion defineClaim(Player p, String name);
 
         //after it has been claimed, we can build it out a bit and make it look nice.
         public void decorateClaim(Player p, ProtectedRegion region);
 
         public void dedecorateClaim(Player p, ProtectedRegion region);
     }
 
     public Plots(JavaPlugin plugin, String permissionRoot, String trigger, NoSwearing noSwearingMod) {
         super(plugin, permissionRoot, trigger);
         if (isEnabled()) {
             //get WorldGuard and WorldEdit plugins
             Plugin worldPlugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
             if (worldPlugin == null || !(worldPlugin instanceof WorldGuardPlugin)) {
                 throw new RuntimeException("WorldGuard must be loaded first");
             }
             worldGuard = (WorldGuardPlugin) worldPlugin;
             Plugin econ = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
             if (econ instanceof BOSEconomy) {
                 this.economy = (BOSEconomy) econ;
                 logInfo("Found BOSEconomy.  Plot economy enable.");
             } else {
                 logInfo("Could not find BOSEconomy.  Plot economy disabled.");
             }
 
             this.noSwearingMod = noSwearingMod;
             this.pcc = new PlayerCenteredClaim(this);
 
             reload();
         }
     }
 
     @Override
     protected void reload() {
         this.plotCost = getConfigInt("plotCost", plotCost);
         this.maxPlots = getConfigInt("maxPlots", maxPlots);
         this.plotSize = getConfigInt("plotSize", plotSize);
         this.plotHeight = getConfigInt("plotHeight", plotHeight);
         this.plotBase = Material.valueOf(getConfigString("plotBase", plotBase.toString()));
         this.plotSurface = Material.valueOf(getConfigString("plotSurface", plotSurface.toString()));
         this.plotPath = Material.valueOf(getConfigString("plotPath", plotPath.toString()));
 
         this.ipc = new InfinitePlotClaim(plotSize);
     }
 
     @Override
     protected String getHelpString(CommandSender sender) {
         return "usage: plot [ claim <plotName> | release | share <player> | unshare <player> | " +
                 "info | list [player] | giveto <player> | tp <plotName> ]";
     }
 
     @Override
     protected boolean handleCommand(Player player, Command cmd, String[] args) {
         if (args.length < 1) {
             //just return the default help string
             return false;
         } else if (hasPermissions(player)) {
             String subCmd = args[0].toLowerCase();
             if ("claim".equalsIgnoreCase(subCmd)) {
                 return claim(player, args);
             } else if ("release".equalsIgnoreCase(subCmd)) {
                 return release(player, args);
             } else if ("share".equalsIgnoreCase(subCmd)) {
                 return share(player, args);
             } else if ("unshare".equalsIgnoreCase(subCmd)) {
                 return unshare(player, args);
             } else if ("info".equalsIgnoreCase(subCmd)) {
                 return info(player, args);
             } else if ("list".equalsIgnoreCase(subCmd)) {
                 return list(player, args);
             } else if ("tp".equalsIgnoreCase(subCmd)) {
                 return tp(player, args);
             } else if ("giveto".equalsIgnoreCase(subCmd)) {
                 return giveTo(player, args);
             } else {
                 return false;
             }
         } else {
             player.sendMessage("You don't have permission to claim land.");
             return true;
         }
     }
 
     private boolean share(Player player, String[] args) {
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         if ((args.length != 2)) {
             player.sendMessage("You must specify a player to share with.");
             return true;
         }
 
         String playerName = args[1];
 
         ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
         if (regions.size() == 0) {
             player.sendMessage("Nothing to share.");
         } else {
             for (ProtectedRegion region : regions) {
                 if (region.isOwner(wgPlayer)) {
                     region.getMembers().addPlayer(playerName);
                     if (saveRegions(regionManager)) {
                         player.sendMessage("Sharing " + region.getId() + " with " + playerName);
                     } else {
                         player.sendMessage("Could not share region for unknown reasons.");
                     }
                 } else {
                     player.sendMessage("Could not share region " + region.getId() + " because you don't own it.");
                 }
             }
         }
         return true;
     }
 
     private boolean unshare(Player player, String[] args) {
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         if ((args.length != 2)) {
             player.sendMessage("You must specify a player to unshare with.");
             return true;
         }
 
         String playerName = args[1];
 
         ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
         if (regions.size() == 0) {
             player.sendMessage("Nothing to unshare.");
         } else {
             for (ProtectedRegion region : regions) {
                 if (region.isOwner(wgPlayer)) {
                     region.getMembers().removePlayer(playerName);
                     if (saveRegions(regionManager)) {
                         player.sendMessage("No longer sharing " + region.getId() + " with " + playerName);
                     } else {
                         player.sendMessage("Could not unshare region for unknown reasons.");
                     }
                 } else {
                     player.sendMessage("Could not unshare region " + region.getId() + " because you don't own it.");
                 }
             }
         }
         return true;
     }
 
     private boolean release(Player player, String[] args) {
         if (args.length != 1) {
             return false;
         }
 
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
         if (regions.size() == 0) {
             player.sendMessage("Nothing to release.");
         } else {
             for (ProtectedRegion region : regions) {
                 if (region.isOwner(wgPlayer)) {
                     regionManager.removeRegion(region.getId());
                     if (saveRegions(regionManager)) {
                         player.sendMessage("Releasing region " + region.getId());
                         getClaimType(player).dedecorateClaim(player, region);
                     } else {
                         player.sendMessage("Could not release region for unknown reasons.");
                     }
                 } else {
                     player.sendMessage("Could not release region " + region.getId() + " because you don't own it.");
                 }
             }
         }
         return true;
     }
 
     private boolean info(Player player, String[] args) {
         if (args.length != 1) {
             return false;
         }
 
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
         if (regions.size() == 0) {
            player.sendMessage("This region is free to be claimed for " + getPurchaseFeeString(regionManager, player));
             String moneyInPocket = getAvailableMoneyString(regionManager, player);
             if (moneyInPocket != null) {
                 player.sendMessage("   You have " + moneyInPocket);
             }
         } else {
             for (ProtectedRegion region : regions) {
                 String name = region.getId();
                 String owners = region.getOwners().toPlayersString();
                 player.sendMessage("Plot name: " + name);
                 player.sendMessage("    Owner: " + owners);
                 if (region.getMembers().size() > 0) {
                     String members = region.getMembers().toPlayersString();
                     player.sendMessage("    Shared with: " + members);
                 }
             }
             return true;
         }
         return true;
     }
 
     private boolean list(Player player, String[] args) {
         if (args.length < 1 || args.length > 2) {
             return false;
         }
 
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
         String playerName = (args.length == 1) ? player.getName() : args[1];
         player.sendMessage("Plots claimed by " + playerName);
         for (Map.Entry<String, ProtectedRegion> entry : regionManager.getRegions().entrySet()) {
             for (String owner : entry.getValue().getOwners().getPlayers()) {
                 if (owner.equalsIgnoreCase(playerName)) {
                     player.sendMessage("    " + entry.getKey());
                     break;
                 }
             }
         }
         return true;
     }
 
     private boolean tp(Player player, String[] args) {
         if (args.length != 2) {
             return false;
         }
 
         String plotName = args[1];
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
         ProtectedRegion region = regionManager.getRegion(plotName);
         if (region == null) {
             player.sendMessage("Can't find a plot named " + plotName);
             return true;
         }
 
         Vector middle = region.getMinimumPoint();
         middle = middle.add(region.getMaximumPoint());
         middle = middle.divide(2.0);
 
         Location dropPoint = BlockUtils.getHighestLandLocation(
                 new Location(player.getWorld(), middle.getX() + 0.5, middle.getY(), middle.getZ() + 0.5));
 
         dropPoint.setY(dropPoint.getY() + 1); //above ground.  :-)
         player.sendMessage("Zooooop!   You're in " + plotName + ".");
         player.teleport(dropPoint);
         return true;
     }
 
     private boolean giveTo(Player player, String[] args) {
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         if ((args.length != 2)) {
             player.sendMessage("You must specify a player to give your plot to.");
             return true;
         }
 
         String playerName = args[1];
 
         ApplicableRegionSet regions = regionManager.getApplicableRegions(player.getLocation());
         if (regions.size() == 0) {
             player.sendMessage("Nothing to give.");
         } else {
             for (ProtectedRegion region : regions) {
                 if (region.isOwner(wgPlayer)) {
                     // add designated player to owners and remove from sharing
                     region.getOwners().addPlayer(playerName);
                     region.getMembers().removePlayer(playerName);
                     // remove this player from owners and add to sharing
                     region.getOwners().removePlayer(player.getName());
                     region.getMembers().addPlayer(player.getName());
                     if (saveRegions(regionManager)) {
                         player.sendMessage("Gave " + region.getId() + " to " + playerName + ".  " +
                                 "You are no longer an owner, but it's shared with you.");
                     } else {
                         player.sendMessage("Could not give away region for unknown reasons.");
                     }
                 } else {
                     player.sendMessage("Could not give region " + region.getId() + " away because you don't own it.");
                 }
             }
         }
         return true;
     }
 
     private boolean claim(Player player, String[] args) {
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         RegionManager regionManager = worldGuard.getRegionManager(player.getWorld());
 
         if (!hasPermissions(player, "claim")) {
             player.sendMessage("You don't have permission to claim plots.");
             return true;
         }
 
         if ((args.length != 2) || !ProtectedRegion.isValidId(args[1])) {
             player.sendMessage("You must specify a valid name for the claim: /plot claim <name>");
             return true;
         }
 
         String claimName = args[1];
 
         //make sure nobody uses bad language.
         if (noSwearingMod.scanForBadWords(player, claimName)) {
             return true;
         }
 
         //is there already a claim with that name?
         if (regionManager.hasRegion(claimName)) {
             player.sendMessage("There's already a claim with that name.  Please choose another name.");
             return true;
         }
 
         //does the region on unclaimed land?
         AbstractClaim claim = getClaimType(player);
         ProtectedRegion region = claim.defineClaim(player, claimName);
         List<ProtectedRegion> conflicts = getConflictingRegions(regionManager, region, wgPlayer);
         if (conflicts.size() > 0) {
             for (ProtectedRegion conflict : conflicts) {
                 String owners = conflict.getOwners().toPlayersString();
                 String name = conflict.getId();
                 player.sendMessage("Sorry, this overlaps \"" + name + "\" owned by " + owners);
             }
             return true;
         }
 
         if (!plotsAreFree(player)) {
             if (economy != null) {
                 if (!canPayForClaim(regionManager, player)) {
                     player.sendMessage("You have used you " + maxPlots +
                             " free plots and can't afford another for " + getPurchaseFeeString(regionManager, player));
                     String moneyInPocket = getAvailableMoneyString(regionManager, player);
                     if (moneyInPocket != null) {
                         player.sendMessage("   You have " + moneyInPocket);
                     }
                     return true;
                 }
             } else {
                 if (regionManager.getRegionCountOfPlayer(wgPlayer) >= maxPlots) {
                     player.sendMessage("You've exceeded the maximum of " + maxPlots + " allowed plots");
                     return true;
                 }
             }
         }
 
         // set up owner and flags
         region.getOwners().addPlayer(wgPlayer);
         region.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.CREEPER_EXPLOSION, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.ENDER_BUILD, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.GHAST_FIREBALL, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
         region.setFlag(DefaultFlag.GREET_MESSAGE, "Now entering " + claimName + " owned by " + wgPlayer.getName());
         region.setFlag(DefaultFlag.FAREWELL_MESSAGE, "Now leaving " + claimName);
 
         // looks good, so let's twiddle as needed.
         claim.decorateClaim(player, region);
         String feeString = getPurchaseFeeString(regionManager, player);
         int fee = getPurchaseFee(regionManager, player);
         regionManager.addRegion(region);
         if (saveRegions(regionManager)) {
             if (plotsAreFree(player) || (economy == null)) {
                 player.sendMessage("You now own a plot named " + claimName);
             } else {
                 payForClaim(regionManager, player, fee);
                 player.sendMessage("You now own a plot named " + claimName + " and you paid " + feeString);
             }
         } else {
             player.sendMessage("You're claim was REJECTED by the county land manager.  Bummer.");
         }
         return true;
     }
 
     private boolean saveRegions(RegionManager mgr) {
         try {
             mgr.save();
             return true;
         } catch (ProtectionDatabaseException e) {
             e.printStackTrace();
             return false;
         }
     }
 
     private AbstractClaim getClaimType(Player p) {
         World w = p.getWorld();
         ChunkGenerator cg = w.getGenerator();
         if (cg instanceof PlotsChunkGenerator) {
             // InfinitePlotsClaim
             logInfo("Claiming using InfinitePlotsClaim");
             return ipc;
         } else {
             // player centered claim
             if (cg != null) {
                 logInfo("Claiming using PlayerCenterClaim.  Chunk generator was a " + cg.getClass().getName());
             } else {
                 logInfo("Claiming using PlayerCenterClaim.");
             }
             return pcc;
         }
     }
 
     private List<ProtectedRegion> getConflictingRegions(RegionManager mgr, ProtectedRegion checkRegion, LocalPlayer player) {
         ArrayList<ProtectedRegion> result = new ArrayList<ProtectedRegion>();
         ApplicableRegionSet appRegions = mgr.getApplicableRegions(checkRegion);
         for (ProtectedRegion region : appRegions) {
             if (!region.getOwners().contains(player)) {
                 result.add(region);
             }
         }
         return result;
     }
 
     private int getPurchaseFee(RegionManager regionManager, Player player) {
         if (economy == null) return 0;
         BukkitPlayer wgPlayer = new BukkitPlayer(worldGuard, player);
         int existingPlots = regionManager.getRegionCountOfPlayer(wgPlayer);
         if (existingPlots < maxPlots) {
             return 0;
         } else {
             return (int) Math.pow(2, (existingPlots - maxPlots)) * 100;
         }
     }
 
     private String getPurchaseFeeString(RegionManager regionManager, Player player) {
         if (economy == null) return "Free";
         int fee = getPurchaseFee(regionManager, player);
         if (fee == 0) {
             return "free";
         } else {
             return "" + fee + " " + economy.getMoneyNamePlural();
         }
     }
 
     private boolean plotsAreFree(Player player) {
         return hasPermissions(player, "unlimitedPlots");
     }
 
     private String getAvailableMoneyString(RegionManager regionManager, Player player) {
         if (economy == null) return null;
         return "" + economy.getPlayerMoneyDouble(player.getName()) + " " + economy.getMoneyNamePlural();
     }
 
     private boolean canPayForClaim(RegionManager regionManager, Player player) {
         int fee = getPurchaseFee(regionManager, player);
         if (economy.getPlayerMoneyDouble(player.getName()) < fee) {
             logInfo("canPayForClaim: false has: " + economy.getPlayerMoneyDouble(player.getName()) + " needs: " + fee);
             return false;
         } else {
             logInfo("canPayForClaim: true has: " + economy.getPlayerMoneyDouble(player.getName()) + " needs: " + fee);
             return true;
         }
     }
 
     private void payForClaim(RegionManager regionManager, Player player, int fee) {
         logInfo("payForClaim: true has: " + economy.getPlayerMoneyDouble(player.getName()) + " needs: " + fee);
         economy.addPlayerMoney(player.getName(), (double) -fee, false);
     }
 
     public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
         return new PlotsChunkGenerator(plotSize, plotHeight, plotBase, plotSurface, plotPath);
     }
 }
 
