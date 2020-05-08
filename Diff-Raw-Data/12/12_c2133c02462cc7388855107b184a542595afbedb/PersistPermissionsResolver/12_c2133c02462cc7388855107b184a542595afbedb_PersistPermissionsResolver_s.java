 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit.persistent;
 
 import at.co.hohl.myresidence.*;
 import at.co.hohl.myresidence.storage.persistent.*;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 /**
  * Implementention for PermissionsResolver which bases on persistence.
  *
  * @author Michael Hohl
  */
 public class PersistPermissionsResolver implements PermissionsResolver {
   // Permission describes that player is an admin.
   private static final String ADMIN_PERMISSION = "myresidence.admin";
 
   // Permission describes that player is trusted.
   private static final String TRUSTED_PERMISSION = "myresidence.trust";
 
   private final MyResidence plugin;
   private final Nation nation;
 
   /**
    * Checks if the player has the passed permission.
    *
    * @param player     the player to check.
    * @param permission the permission to check.
    * @return true if the player has the requiered permission.
    */
   public boolean hasPermission(Player player, String permission) {
     if (plugin.getWorldEdit() != null) {
       return plugin.getWorldEdit().getPermissionsResolver().hasPermission(player.getName(), permission);
     } else {
       return player.hasPermission(permission) || player.isOp();
     }
   }
 
   /**
    * Creates a new, persistence based PermissionsResolver.
    *
    * @param plugin the myresidence which holds the nation.
    * @param nation the nation to manage.
    */
   public PersistPermissionsResolver(MyResidence plugin, Nation nation) {
     this.nation = nation;
     this.plugin = plugin;
   }
 
   /**
    * Checks if the session is a session of an Administrator.
    *
    * @param player player to check the permissions for.
    * @return true, if the player owns the administrator permission.
    */
   public boolean isAdmin(Player player) {
     return hasPermission(player, ADMIN_PERMISSION);
   }
 
   /**
    * Checks if the session is a session of an Administrator.
    *
    * @param player player to check the permissions for.
    * @return true, if the player owns the trusted permission.
    */
   public boolean isTrustedPlayer(Player player) {
     return hasPermission(player, TRUSTED_PERMISSION);
   }
 
   /**
    * Checks if the player is
    *
    * @param player      player to check the permissions for.
    * @param blockPlaced the placed block.
    * @return true, if the player is allowed to build here.
    */
   public boolean isAllowedToPlaceBlockAt(Player player, Block blockPlaced) {
     if (isTrustedPlayer(player) || isAdmin(player)) {
       return true;
     }
 
     Location blockLocation = blockPlaced.getLocation();
     Inhabitant inhabitant = nation.getInhabitant(player.getName());
 
     // On Residence?
     List<Residence> residencesAtLocation = nation.findResidencesNearTo(blockLocation,
             plugin.getConfiguration(player.getWorld()).getResidenceOverlay());
     if (residencesAtLocation != null && residencesAtLocation.size() > 0) {
       for (Residence residence : residencesAtLocation) {
         if (!canBuildAndDestroy(residence, inhabitant)) {
           return false;
         }
       }
       return true;
     }
 
     // Inside Town?
     Town town = nation.getTown(blockLocation);
     if (town != null) {
       return canBuildAndDestroy(town, inhabitant) || plugin.getConfiguration(player.getWorld())
               .getAllowedToBuildInTown().contains(blockPlaced.getTypeId());
     }
 
     // In wildness?
     return plugin.getConfiguration(player.getWorld()).getAllowedToBuildInWildness().contains(blockPlaced.getTypeId());
   }
 
   /**
    * Checks if the player is
    *
    * @param player         player to check the permissions for.
    * @param blockDestroyed the block which gets destroyed.
    * @return true, if the player is allowed to build here.
    */
   public boolean isAllowedToDestroyBlockAt(Player player, Block blockDestroyed) {
     if (isTrustedPlayer(player) || isAdmin(player)) {
       return true;
     }
 
     Location blockLocation = blockDestroyed.getLocation();
     Inhabitant inhabitant = nation.getInhabitant(player.getName());
 
     List<Residence> residencesAtLocation = nation.findResidencesNearTo(blockLocation,
             plugin.getConfiguration(player.getWorld()).getResidenceOverlay());
     if (residencesAtLocation != null && residencesAtLocation.size() > 0) {
       for (Residence residence : residencesAtLocation) {
         if (!canBuildAndDestroy(residence, inhabitant)) {
           return false;
         }
       }
       return true;
     }
 
     Town town = nation.getTown(blockLocation);
     // Town not null? You must be inside a town.
     if (town != null) {
       return canBuildAndDestroy(town, inhabitant) ||
               plugin.getConfiguration(player.getWorld()).getAllowedToDestroyInTown()
                       .contains(blockDestroyed.getTypeId());
     }
 
     // In wildness?
     return plugin.getConfiguration(player.getWorld()).getAllowedToDestroyInWildness()
             .contains(blockDestroyed.getTypeId());
   }
 
   /**
    * Checks if the player is allowed to interact with the passed block.
    *
    * @param player          player to check
    * @param blockToInteract the block to interact
    * @return true, if the player is allowed to interact
    */
   public boolean isAllowedToInteractWithBlock(Player player, Block blockToInteract) {
     if (isTrustedPlayer(player) || isAdmin(player)) {
       return true;
     }
 
     Inhabitant inhabitant = nation.getInhabitant(player.getName());
     Residence residencesAtLocation = nation.getResidence(blockToInteract.getLocation());
    return residencesAtLocation != null && canBuildAndDestroy(residencesAtLocation, inhabitant);
   }
 
   /**
    * Return true, if the player can build and destroy on the passed residence.
    *
    * @param residence  the residence to check.
    * @param inhabitant the inhabitant to check.
    * @return true, if the player can build and destroy.
    */
   private boolean canBuildAndDestroy(Residence residence, Inhabitant inhabitant) {
     ResidenceManager residenceManager = nation.getResidenceManager(residence);
 
     if (residenceManager.hasFlag(ResidenceFlag.Type.PUBLIC) || residenceManager.isMember(inhabitant)) {
       return true;
     }
 
     if (residenceManager.hasFlag(ResidenceFlag.Type.LOCAL) && residence.getTownId() != 0) {
       Town town = nation.getTown(residence.getTownId());
       TownManager townManager = nation.getTownManager(town);
       return townManager.isInhabitant(inhabitant);
     }
 
     return false;
   }
 
   /**
    * Check if the player can build and destroy in the passed town.
    *
    * @param town       the town to check.
    * @param inhabitant the inhabitant to check.
    * @return true, if the player can build and destroy.
    */
   private boolean canBuildAndDestroy(Town town, Inhabitant inhabitant) {
     TownManager townManager = nation.getTownManager(town);
     return townManager.hasFlag(TownFlag.Type.INHABITANTS_CAN_BUILD) || townManager.isMajor(inhabitant);
   }
 }
