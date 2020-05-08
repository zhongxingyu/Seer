 package me.DDoS.Quicksign.handler;
 
 import me.DDoS.Quicksign.session.StandardEditSession;
 import me.DDoS.Quicksign.session.EditSession;
 import me.DDoS.Quicksign.util.QSUtil;
 import me.DDoS.Quicksign.permission.Permission;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
 
 import com.bekvon.bukkit.residence.Residence;
 import com.bekvon.bukkit.residence.protection.ClaimedResidence;
 import com.bekvon.bukkit.residence.protection.FlagPermissions;
 import com.bekvon.bukkit.residence.protection.ResidencePermissions;
 
 import com.griefcraft.lwc.LWC;
 import com.griefcraft.model.Protection;
 
 import com.palmergames.bukkit.towny.NotRegisteredException;
 import com.palmergames.bukkit.towny.PlayerCache;
 import com.palmergames.bukkit.towny.PlayerCache.TownBlockStatus;
 import com.palmergames.bukkit.towny.Towny;
 import com.palmergames.bukkit.towny.TownyException;
 import com.palmergames.bukkit.towny.object.Coord;
 import com.palmergames.bukkit.towny.object.TownBlockType;
 import com.palmergames.bukkit.towny.object.TownyPermission;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 import com.palmergames.bukkit.towny.object.TownyWorld;
 import com.palmergames.bukkit.towny.object.WorldCoord;
 import com.palmergames.bukkit.townywar.TownyWar;
 import com.palmergames.bukkit.townywar.TownyWarConfig;
 import com.sk89q.worldedit.Vector;
 
 import couk.Adamki11s.Regios.API.RegiosAPI;
 import couk.Adamki11s.Regios.Regions.Region;
 import me.DDoS.Quicksign.QuickSign;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.yi.acru.bukkit.Lockette.Lockette;
 
 /**
  *
  * @author DDoS
  */
 @SuppressWarnings("unchecked")
 public class SelectionHandler {
 
     private final QuickSign plugin;
     //
     private WorldGuardPlugin wg = null;
     private RegiosAPI regiosAPI = null;
     private Towny towny = null;
     private LWC lwc = null;
     private boolean residence = false;
     private boolean lockette = false;
 
     public SelectionHandler(QuickSign instance) {
 
         plugin = instance;
 
     }
 
     public void setWG(WorldGuardPlugin wg) {
 
         this.wg = wg;
 
     }
 
     public void setRegiosAPI(RegiosAPI regiosAPI) {
 
         this.regiosAPI = regiosAPI;
 
     }
 
     public void setTowny(Towny towny) {
 
         this.towny = towny;
 
     }
 
     public void setLWC(LWC lwc) {
 
         this.lwc = lwc;
 
     }
 
     public void setResidence(boolean residence) {
 
         this.residence = residence;
 
     }
 
     public void setLockette(boolean lockette) {
 
         this.lockette = lockette;
 
     }
 
     public void handleSignSelection(PlayerInteractEvent event, Sign sign, Player player) {
 
         if (!plugin.getBlackList().allows(sign, player)) {
 
             QSUtil.tell(player, "You cannot select this sign.");
             return;
 
         }
 
         if (checkForSelectionRights(player, sign.getBlock().getLocation())) {
 
             if (event != null) {
 
                 event.setCancelled(true);
 
             }
 
             Player owner = getOwner(sign);
 
             if (owner != null && !owner.equals(player)) {
 
                 QSUtil.tell(player, "This sign is already selected.");
                 return;
 
             }
 
             StandardEditSession session = (StandardEditSession) plugin.getSession(player);
 
             if (session.addSign(sign)) {
 
                 QSUtil.tell(player, "Sign " + ChatColor.GREEN + "added " + ChatColor.GRAY + "to selection, "
                         + ChatColor.WHITE + session.getNumberOfSigns() + ChatColor.GRAY + " total.");
                 return;
 
             } else {
 
                 session.removeSign(sign);
                 QSUtil.tell(player, "Sign " + ChatColor.RED + "removed " + ChatColor.GRAY + "from selection, "
                         + ChatColor.WHITE + session.getNumberOfSigns() + ChatColor.GRAY + " total.");
                 return;
 
             }
 
         } else {
 
             QSUtil.tell(player, "You don't own this sign.");
 
         }
     }
 
     private boolean checkForWGMembership(Player player, Location location, World world) {
 
         Vector pt = toVector(location);
         LocalPlayer localPlayer = wg.wrapPlayer(player);
         RegionManager regionManager = wg.getRegionManager(world);
         ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
 
         if (set.size() != 0) {
 
             return set.isMemberOfAll(localPlayer);
 
         }
 
         return false;
 
     }
 
     private boolean checkForWGOwnership(Player player, Location location, World world) {
 
         Vector pt = toVector(location);
         LocalPlayer localPlayer = wg.wrapPlayer(player);
         RegionManager regionManager = wg.getRegionManager(world);
         ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
 
         if (set.size() != 0) {
 
             return set.isOwnerOfAll(localPlayer);
 
         }
 
         return false;
 
     }
 
     private boolean checkForWGBuildPermissions(Player player, Location location, World world) {
 
         Vector pt = toVector(location);
         LocalPlayer localPlayer = wg.wrapPlayer(player);
         RegionManager regionManager = wg.getRegionManager(world);
         ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
         return set.canBuild(localPlayer);
 
     }
 
     private boolean checkForResidencePerms(World world, Location location, Player player, boolean forceRegion) {
 
         FlagPermissions.addFlag("build");
         ClaimedResidence res = Residence.getResidenceManager().getByLoc(location);
 
         if (res == null && !forceRegion) {
 
             return true;
 
         }
 
         if (res == null && forceRegion) {
 
             return false;
 
         }
 
         if (res != null) {
 
             ResidencePermissions perms = res.getPermissions();
             return perms.playerHas(player.getName(), world.getName(), "build", true);
 
         }
 
         return false;
 
     }
 
     private boolean checkForRegiosPerms(Player player, boolean forceRegion) {
 
         Region region = regiosAPI.getRegion(player);
 
         if (region == null && !forceRegion) {
 
             return true;
 
         }
 
         if (region == null && forceRegion) {
 
             return false;
 
         }
 
         if (region != null) {
 
             return region.canBuild(player);
 
         }
 
         return false;
 
     }
 
    private boolean checkForTownyPerms(Player player, Location loc, boolean forceRegion) {
 
         boolean canBuild = true;
 
         if (towny.isError()) {
 
             return false;
 
         }
 
         Block block = loc.getBlock();
         TownyWorld world;
 
         try {
 
             world = TownyUniverse.getDataSource().getWorld(block.getWorld().getName());
 
         } catch (NotRegisteredException nre) {
 
             return false;
 
         }
 
         WorldCoord worldCoord = new WorldCoord(world, Coord.parseCoord(block));
         PlayerCache cache = towny.getCache(player);
         TownBlockStatus status = cache.getStatus();
         boolean bBuild = TownyUniverse.getCachePermissions().
                 getCachePermission(player, block.getLocation(), TownyPermission.ActionType.BUILD);
         boolean wildOverride = TownyUniverse.getPermissionSource().
                 hasWildOverride(worldCoord.getWorld(), player, block.getTypeId(), TownyPermission.ActionType.BUILD);
 
         if (((status == TownBlockStatus.UNCLAIMED_ZONE)
                 && (wildOverride))
                 || ((status == TownBlockStatus.TOWN_RESIDENT)
                 && (towny.getTownyUniverse().getTownBlock(block.getLocation()).getType() == TownBlockType.WILDS)
                 && (wildOverride))) {
 
             return true;
 
         }
 
 
         if (((status == TownBlockStatus.TOWN_RESIDENT)
                 && (TownyUniverse.getPermissionSource().hasOwnTownOverride(player, block.getTypeId(), TownyPermission.ActionType.BUILD)))
                 || (((status == TownBlockStatus.OUTSIDER)
                 || (status == TownBlockStatus.TOWN_ALLY)
                 || (status == TownBlockStatus.ENEMY))
                 && (TownyUniverse.getPermissionSource().hasAllTownOverride(player, block.getTypeId(), TownyPermission.ActionType.BUILD)))) {
 
             return true;
 
         }
 
         if (((status == TownBlockStatus.ENEMY)
                 && TownyWarConfig.isAllowingAttacks())
                 && (block.getType() == TownyWarConfig.getFlagBaseMaterial())) {
 
             try {
 
                 if (TownyWar.callAttackCellEvent(towny, player, block, worldCoord)) {
 
                     return true;
 
                 }
 
             } catch (TownyException e) {
             }
 
             canBuild = false;
 
         } else if (status == TownBlockStatus.WARZONE) {
 
             if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
 
                 canBuild = false;
 
             }
 
             return canBuild;
 
         } else if (((status == TownBlockStatus.UNCLAIMED_ZONE)
                 && (!wildOverride))
                 || ((!bBuild)
                 && (status != TownBlockStatus.UNCLAIMED_ZONE))) {
 
             canBuild = false;
 
         }
 
         return canBuild;
 
     }
 
     private boolean checkForLWCPerms(Player player, Location loc, boolean forceProtection) {
 
         Protection protection = lwc.findProtection(loc.getBlock());
 
         if (protection == null && !forceProtection) {
 
             return true;
 
         }
 
         if (protection == null && forceProtection) {
 
             return false;
 
         }
 
         if (protection != null) {
 
             return lwc.canAccessProtection(player, protection);
 
         }
 
         return false;
 
     }
 
     private boolean checkForLockettePerms(Player player, Location loc, boolean forceProtection) {
 
         boolean isProtected = Lockette.isProtected(loc.getBlock());
 
         if (!isProtected && !forceProtection) {
 
             return true;
 
         }
 
         if (!isProtected && forceProtection) {
 
             return false;
 
         }
 
         if (isProtected) {
 
             return Lockette.isOwner(loc.getBlock(), player.getName());
 
         }
 
         return false;
 
     }
 
     public boolean checkForSelectionRights(Player player, Location location) {
 
         World world = location.getWorld();
 
         if (wg == null && !residence && !lockette && regiosAPI == null && towny == null && lwc == null) {
 
             return plugin.hasPermissions(player, Permission.USE);
 
         }
 
         if (plugin.hasPermissions(player, Permission.FREE_USE)) {
 
             return true;
 
         }
 
         if (wg != null) {
 
             if (plugin.hasPermissions(player, Permission.WG_MEMBER)
                     && checkForWGMembership(player, location, world)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.WG_OWNER)
                     && checkForWGOwnership(player, location, world)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.WG_CAN_BUILD)
                     && checkForWGBuildPermissions(player, location, world)) {
 
                 return true;
 
             }
         }
 
         if (residence) {
 
             if (plugin.hasPermissions(player, Permission.RS_CAN_BUILD_FP)
                     && checkForResidencePerms(world, location, player, true)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.RS_CAN_BUILD)
                     && checkForResidencePerms(world, location, player, false)) {
 
                 return true;
 
             }
         }
 
         if (regiosAPI != null) {
 
             if (plugin.hasPermissions(player, Permission.RE_CAN_BUILD_FP)
                     && checkForRegiosPerms(player, true)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.RE_CAN_BUILD)
                     && checkForRegiosPerms(player, false)) {
 
                 return true;
 
             }
         }
 
         if (towny != null) {
 
             if (plugin.hasPermissions(player, Permission.TW_CAN_BUILD)
                    && checkForTownyPerms(player, location, true)) {
 
                 return true;
 
             }
         }
 
         if (lwc != null) {
 
             if (plugin.hasPermissions(player, Permission.LWC_CAN_ACCESS_FP)
                     && checkForLWCPerms(player, location, true)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.LWC_CAN_ACCESS)
                     && checkForLWCPerms(player, location, false)) {
 
                 return true;
 
             }
         }
 
         if (lockette) {
 
             if (plugin.hasPermissions(player, Permission.LOCKETTE_IS_OWNER_FP)
                     && checkForLockettePerms(player, location, true)) {
 
                 return true;
 
             }
 
             if (plugin.hasPermissions(player, Permission.LOCKETTE_IS_OWNER)
                     && checkForLockettePerms(player, location, false)) {
 
                 return true;
 
             }
         }
 
         return false;
 
     }
 
     private Player getOwner(Sign sign) {
 
         for (Entry<Player, EditSession> entry : plugin.getSessions()) {
 
             if (entry.getValue().checkIfSelected(sign)) {
 
                 return entry.getKey();
 
             }
         }
 
         return null;
 
     }
 }
