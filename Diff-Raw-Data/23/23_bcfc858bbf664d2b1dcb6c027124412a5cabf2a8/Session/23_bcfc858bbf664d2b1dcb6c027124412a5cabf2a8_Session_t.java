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
 
 package at.co.hohl.myresidence.storage;
 
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.exceptions.NoResidenceSelectedException;
 import at.co.hohl.myresidence.exceptions.NoTownSelectedException;
 import at.co.hohl.myresidence.storage.persistent.Major;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import java.util.List;
 
 /**
  * Contains information about a players session.
  *
  * @author Michael Hohl
  */
 public class Session {
     // Duration, how long a selection should be stored.
     private static final long SELECTION_DURATION = 45 * 1000;
 
     /** Activator for tasks. */
     public enum Activator {
         CONFIRM_COMMAND,
         SELECT_SIGN
     }
 
     /** Player who owns the session. */
     private final Player player;
 
     /** The plugin which holds the session. */
     private final MyResidence plugin;
 
     /** The nation which contains all the towns and residences. */
     private final Nation nation;
 
     /** Is flag set, player want to get debug information. */
     private boolean debugger;
 
     /** Last clicked sign. */
     private Sign selectedSign;
 
     /** Time, when sign is selected. */
     private long signSelectedAt;
 
     /** Selected town. */
     private int selectedTownId = -1;
 
     /** Activator for task. */
     private Activator taskActivator;
 
     /** Task to do. */
     private Runnable task;
 
     /**
      * Creates a new Session for the passed player.
      *
      * @param plugin the plugin which holds the session.
      * @param nation the nation which contains all the towns and residences.
      * @param player the player who should own the session.
      */
     public Session(MyResidence plugin, Nation nation, Player player) {
         this.plugin = plugin;
         this.nation = nation;
         this.player = player;
     }
 
     /** @return id of the player. */
     public int getPlayerId() {
         return nation.getInhabitant(player.getName()).getId();
     }
 
     /**
      * Checks if the player has permission to do that.
      *
      * @param permission the permission to check.
      * @return true, if the player has the permission.
      */
     public boolean hasPermission(String permission) {
         return plugin.getPermissionsResolver().hasPermission(player.getName(), permission);
     }
 
     /**
      * Checks if the session has major rights in the town.
      *
      * @param town the town to check.
      * @return true, if the session has enough rights.
      */
     public boolean hasMajorRights(Town town) {
         return nation.isMajor(town, nation.getInhabitant(getPlayerId())) || hasPermission("myresidence.admin");
     }
 
     /**
      * Checks if the session has owner rights for the residence.
      *
      * @param residence the residence to check.
      * @return true, if the session has enough rights.
      */
     public boolean hasResidenceOwnerRights(Residence residence) {
         return getPlayerId() == residence.getOwnerId() || hasPermission("myresidence.admin");
     }
 
     /**
      * Returns the current selected Residence.
      *
      * @return the current selection of the player.
      *
      * @throws at.co.hohl.myresidence.exceptions.NoResidenceSelectedException
      *          thrown when the player don't have a selection.
      */
     public Residence getSelectedResidence() throws NoResidenceSelectedException {
         Residence residence;
 
        if (selectedSign != null && signSelectedAt + SELECTION_DURATION > System.currentTimeMillis()) {
             signSelectedAt = System.currentTimeMillis();
 
             residence = nation.getResidence(getSelectedSign());
         } else {
             residence = nation.getResidence(player.getLocation());
         }
 
         if (residence == null) {
             throw new NoResidenceSelectedException();
         }
 
         return residence;
 
     }
 
     /**
      * @return selected town.
      *
      * @throws NoTownSelectedException thrown when no town is selected.
      */
     public Town getSelectedTown() throws NoTownSelectedException {
         if (selectedTownId == -1) {
             List<Major> majorities = nation.getDatabase().find(Major.class).where()
                     .eq("inhabitantId", getPlayerId())
                     .findList();
 
             if (majorities.size() == 1) {
                 selectedTownId = majorities.get(0).getTownId();
                 return nation.getTown(majorities.get(0).getTownId());
             }
 
             throw new NoTownSelectedException();
         } else {
             return nation.getTown(selectedTownId);
         }
     }
 
     /**
      * Sets the selected Town.
      *
      * @param town the town to set selected.
      */
     public void setSelectedTown(Town town) {
         selectedTownId = town.getId();
     }
 
     /**
      * Sets the last selected sign.
      *
      * @param selectedSign the selected sign.
      */
     public void setSelectedSign(Sign selectedSign) {
         this.selectedSign = selectedSign;
 
         signSelectedAt = System.currentTimeMillis();
     }
 
     public boolean isDebugger() {
         return debugger;
     }
 
     public void setDebugger(boolean debugger) {
         this.debugger = debugger;
     }
 
     public Sign getSelectedSign() {
         return selectedSign;
     }
 
     public Runnable getTask() {
         return task;
     }
 
     public void setTask(Runnable task) {
         this.task = task;
     }
 
     public Activator getTaskActivator() {
         return taskActivator;
     }
 
     public void setTaskActivator(Activator taskActivator) {
         this.taskActivator = taskActivator;
     }
 }
