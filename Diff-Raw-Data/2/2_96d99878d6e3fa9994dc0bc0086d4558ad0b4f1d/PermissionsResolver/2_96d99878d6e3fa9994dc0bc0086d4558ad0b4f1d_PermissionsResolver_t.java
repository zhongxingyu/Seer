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
 
 package at.co.hohl.myresidence;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 /**
  * Used to retrieve information about what a player is allowed to do and what not.
  *
  * @author Michael Hohl
  */
 public interface PermissionsResolver {
 
   /**
    * Checks if the player has the passed permission.
    *
    * @param player     the player to check.
    * @param permission the permission to check.
   * @return true if the player has the required permission.
    */
   boolean hasPermission(Player player, String permission);
 
   /**
    * Checks if the session is a session of an Administrator.
    *
    * @param player player to check the permissions for.
    * @return true, if the player owns the administrator permission.
    */
   boolean isAdmin(Player player);
 
   /**
    * Checks if the session is a session of an Administrator.
    *
    * @param player player to check the permissions for.
    * @return true, if the player owns the trusted permission.
    */
   boolean isTrustedPlayer(Player player);
 
   /**
    * Checks if the player is
    *
    * @param player      player to check the permissions for.
    * @param blockPlaced the placed block.
    * @return true, if the player is allowed to build here.
    */
   public boolean isAllowedToPlaceBlockAt(Player player, Block blockPlaced);
 
   /**
    * Checks if the player is
    *
    * @param player         player to check the permissions for.
    * @param blockDestroyed the block which gets destroyed.
    * @return true, if the player is allowed to build here.
    */
   public boolean isAllowedToDestroyBlockAt(Player player, Block blockDestroyed);
 
   /**
    * Checks if the player is allowed to interact with the passed block.
    *
    * @param player          player to check
    * @param blockToInteract the block to interact
    * @return true, if the player is allowed to interact
    */
   public boolean isAllowedToInteractWithBlock(Player player, Block blockToInteract);
 
 }
