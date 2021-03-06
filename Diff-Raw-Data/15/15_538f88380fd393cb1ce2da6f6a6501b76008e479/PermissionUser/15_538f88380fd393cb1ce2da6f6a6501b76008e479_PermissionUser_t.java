 /*
  * PermissionsEx - Permissions plugin for Bukkit
  * Copyright (C) 2011 t3hk0d3 http://www.tehkode.ru
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package ru.tehkode.permissions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import ru.tehkode.permissions.events.PermissionEntityEvent;
 import ru.tehkode.permissions.exceptions.RankingException;
 
 /**
  *
  * @author code
  */
 public abstract class PermissionUser extends PermissionEntity {
 
     protected HashMap<String, List<PermissionGroup>> cachedGroups = new HashMap<String, List<PermissionGroup>>();
     protected HashMap<String, String[]> cachedPermissions = new HashMap<String, String[]>();
     protected String cachedPrefix = null;
     protected String cachedSuffix = null;
     protected HashMap<String, String> cachedAnwsers = new HashMap<String, String>();
 
     public PermissionUser(String playerName, PermissionManager manager) {
         super(playerName, manager);
     }
 
     /**
      * Return non-inherited user prefix.
      * This means if a user don't have has own prefix
      * then empty string or null would be returned
      * 
      * @return prefix as string
      */
     public String getOwnPrefix() {
         return this.getOwnPrefix(null);
     }
 
     public abstract String getOwnPrefix(String worldName);
 
     /**
      * Return non-inherited suffix prefix.
      * This means if a user don't has own suffix
      * then empty string or null would be returned
      * 
      * @return suffix as string
      */
     public final String getOwnSuffix() {
         return this.getOwnSuffix(null);
     }
 
     public abstract String getOwnSuffix(String worldName);
 
     /**
      * Return non-inherited permissions of a user in world
      * 
      * @param world world's name
      * @return String array of owned Permissions
      */
     public abstract String[] getOwnPermissions(String world);
 
     /**
      * Return non-inherited value of specified option for user in world
      * 
      * @param option option string
      * @param world world's name
      * @param defaultValue default value
      * @return option value or defaultValue if option is not set
      */
     public abstract String getOwnOption(String option, String world, String defaultValue);
 
     /**
      * Return non-inherited value of specified option in common space (all worlds).
      * 
      * @param option
      * @return option value or empty string if option is not set
      */
     public String getOwnOption(String option) {
         return this.getOwnOption(option, "", "");
     }
 
     public String getOwnOption(String option, String world) {
         return this.getOwnOption(option, world, "");
     }
 
     public int getOwnOptionInteger(String optionName, String world, int defaultValue) {
         String option = this.getOwnOption(optionName, world, Integer.toString(defaultValue));
 
         try {
             return Integer.parseInt(option);
         } catch (NumberFormatException e) {
         }
 
         return defaultValue;
     }
 
     public boolean getOwnOptionBoolean(String optionName, String world, boolean defaultValue) {
         String option = this.getOwnOption(optionName, world, Boolean.toString(defaultValue));
 
         if ("false".equalsIgnoreCase(option)) {
             return false;
         } else if ("true".equalsIgnoreCase(option)) {
             return true;
         }
 
         return defaultValue;
     }
 
     public double getOwnOptionDouble(String optionName, String world, double defaultValue) {
         String option = this.getOwnOption(optionName, world, Double.toString(defaultValue));
 
         try {
             return Double.parseDouble(option);
         } catch (NumberFormatException e) {
         }
 
         return defaultValue;
     }
 
     protected abstract String[] getGroupsNamesImpl(String worldName);
 
     /**
      * Get group for this user, global inheritance only
      * 
      * @return 
      */
     public PermissionGroup[] getGroups() {
         return this.getGroups(null);
     }
 
     /**
      * Get groups for this user for specified world
      * 
      * @param worldName Name of world
      * @return PermissionGroup groups
      */
     public PermissionGroup[] getGroups(String worldName) {
         String worldIndex = worldName;
 
         if (worldName == null || worldName.isEmpty()) {
             worldIndex = "@common";
         }
 
         if (!this.cachedGroups.containsKey(worldIndex)) {
             List<PermissionGroup> groups = new LinkedList<PermissionGroup>();
 
             for (String groupName : this.getGroupsNamesImpl(worldName)) {
                 if (groupName == null || groupName.isEmpty()) {
                     continue;
                 }
 
                 PermissionGroup group = this.manager.getGroup(groupName);
 
                 if (!groups.contains(group)) {
                     groups.add(group);
                 }
             }
 
             if (worldName != null) { // check world-inheritance also
                 // world inheritance
                 for (String world : this.manager.getWorldInheritance(worldName)) {
                     groups.addAll(Arrays.asList(this.getGroups(world)));
                 }
 
                 // common groups
                 groups.addAll(Arrays.asList(this.getGroups(null)));
             }
 
             if (groups.isEmpty()) {
                 groups.add(this.manager.getDefaultGroup());
             }
 
             Collections.sort(groups);
 
             this.cachedGroups.put(worldIndex, groups);
         }
 
         return this.cachedGroups.get(worldIndex).toArray(new PermissionGroup[0]);
     }
 
     public Map<String, PermissionGroup[]> getAllGroups() {
         Map<String, PermissionGroup[]> allGroups = new HashMap<String, PermissionGroup[]>();
 
         for (String worldName : this.getWorlds()) {
             List<PermissionGroup> groups = new LinkedList<PermissionGroup>();
 
             for (String groupName : this.getGroupsNamesImpl(worldName)) {
                 if (groupName == null || groupName.isEmpty()) {
                     continue;
                 }
 
                 PermissionGroup group = this.manager.getGroup(groupName);
 
                 if (!groups.contains(group)) {
                     groups.add(group);
                 }
             }
 
             Collections.sort(groups);
 
             if (groups.size() > 0) {
                 allGroups.put(worldName, groups.toArray(new PermissionGroup[0]));
             }
         }
 
         return allGroups;
     }
 
     /**
      * Get group names, common space only
      * 
      * @return 
      */
     public String[] getGroupsNames() {
         return this.getGroupsNames(null);
     }
 
     /**
      * Get group names in specified world
      * 
      * @return String array of user's group names
      */
     public String[] getGroupsNames(String worldName) {
         List<String> groups = new LinkedList<String>();
         for (PermissionGroup group : this.getGroups(worldName)) {
             if (group != null) {
                 groups.add(group.getName());
             }
         }
 
         return groups.toArray(new String[0]);
     }
 
     /**
      * Set parent groups for user
      * 
      * @param groups array of parent group names
      */
     public abstract void setGroups(String[] groups, String worldName);
 
     public void setGroups(String[] groups) {
         this.setGroups(groups, null);
     }
 
     /**
      * Set parent groups for user
      * 
      * @param groups array of parent group objects
      */
     public void setGroups(PermissionGroup[] parentGroups, String worldName) {
         List<String> groups = new LinkedList<String>();
 
         for (PermissionGroup group : parentGroups) {
             groups.add(group.getName());
         }
 
         this.setGroups(groups.toArray(new String[0]), worldName);
     }
 
     public void setGroups(PermissionGroup[] parentGroups) {
         this.setGroups(parentGroups, null);
     }
 
     /**
      * Add user to group
      * 
      * @param groupName group's name as String
      */
     public void addGroup(String groupName, String worldName) {
         if (groupName == null || groupName.isEmpty()) {
             return;
         }
 
         this.addGroup(this.manager.getGroup(groupName), worldName);
     }
 
     public void addGroup(String groupName) {
         this.addGroup(groupName, null);
     }
 
     /**
      * Add user to group
      * 
      * @param group as PermissionGroup object
      */
     public void addGroup(PermissionGroup group, String worldName) {
         if (group == null) {
             return;
         }
 
         List<PermissionGroup> groups = new LinkedList<PermissionGroup>(Arrays.asList(this.getGroups(worldName)));
 
         if (this.getGroupsNamesImpl(worldName).length == 0 && groups.size() == 1 && groups.contains(this.manager.getDefaultGroup())) {
             groups.clear(); // clean out default group
         }
 
         if (group.isVirtual()) {
             group.save();
         }
 
         if (!groups.contains(group)) {
             groups.add(group);
             this.clearCache();
             this.setGroups(groups.toArray(new PermissionGroup[0]), worldName);
         }
     }
 
     public void addGroup(PermissionGroup group) {
         this.addGroup(group, null);
     }
 
     /**
      * Remove user from group
      * 
      * @param groupName group's name as String
      */
     public void removeGroup(String groupName, String worldName) {
         if (groupName == null || groupName.isEmpty()) {
             return;
         }
 
         this.removeGroup(this.manager.getGroup(groupName), worldName);
     }
 
     public void removeGroup(String groupName) {
         this.removeGroup(this.manager.getGroup(groupName));
     }
 
     /**
      * Remove user from group
      * 
      * @param group group as PermissionGroup object
      */
     public void removeGroup(PermissionGroup group, String worldName) {
         if (group == null) {
             return;
         }
 
         List<PermissionGroup> groups = new LinkedList<PermissionGroup>(Arrays.asList(this.getGroups(worldName)));
 
         if (groups.contains(group)) {
             groups.remove(group);
             this.clearCache();
             this.setGroups(groups.toArray(new PermissionGroup[]{}), worldName);
         }
 
         this.clearCache();
     }
 
     public void removeGroup(PermissionGroup group) {
         for (String worldName : this.getWorlds()) {
             this.removeGroup(group, worldName);
         }
 
         this.removeGroup(group, null);
     }
 
     /**
      * Check if this user is member of group or one of its descendant groups (optionally)
      * 
      * @param group group as PermissionGroup object
      * @param worldName 
      * @param checkInheritance if true then descendant groups of the given group would be checked too 
      * @return true on success, false otherwise
      */
     public boolean inGroup(PermissionGroup group, String worldName, boolean checkInheritance) {
         for (PermissionGroup parentGroup : this.getGroups(worldName)) {
             if (parentGroup.equals(group)) {
                 return true;
             }
 
             if (checkInheritance && parentGroup.isChildOf(group, worldName, true)) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean inGroup(PermissionGroup group, boolean checkInheritance) {
         for (String worldName : this.getWorlds()) {
             if (this.inGroup(group, worldName, checkInheritance)) {
                 return true;
             }
         }
 
         return this.inGroup(group, null, checkInheritance);
     }
 
     /**
      * Check if this user is member of group or one of its descendant groups (optionally)
      * 
      * @param groupName group's name to check
      * @param worldName 
      * @param checkInheritance if true than descendant groups of specified group would be checked too 
      * @return true on success, false otherwise
      */
     public boolean inGroup(String groupName, String worldName, boolean checkInheritance) {
         return this.inGroup(this.manager.getGroup(groupName), worldName, checkInheritance);
     }
 
     public boolean inGroup(String groupName, boolean checkInheritance) {
         return this.inGroup(this.manager.getGroup(groupName), checkInheritance);
     }
 
     /**
      * Check if this user is member of group or one of its descendant groups
      * 
      * @param group
      * @param worldName 
      * @return true on success, false otherwise
      */
     public boolean inGroup(PermissionGroup group, String worldName) {
         return this.inGroup(group, worldName, true);
     }
 
     public boolean inGroup(PermissionGroup group) {
         return this.inGroup(group, true);
     }
 
     /**
      * Checks if this user is member of specified group or one of its descendant groups
      * 
      * @param group group's name
      * @return true on success, false otherwise
      */
     public boolean inGroup(String groupName, String worldName) {
         return this.inGroup(this.manager.getGroup(groupName), worldName, true);
     }
 
     public boolean inGroup(String groupName) {
         return this.inGroup(groupName, true);
     }
 
     /**
      * Promotes user in specified ladder.
      * If user is not member of the ladder RankingException will be thrown
      * If promoter is not null and he is member of the ladder and 
      * his rank is lower then user's RankingException will be thrown too.
      * If there is no group to promote the user to RankingException would be thrown
      * 
      * 
      * @param promoter null if action is performed from console or by a plugin
      * @param ladderName Ladder name
      * @throws RankingException 
      */
     public PermissionGroup promote(PermissionUser promoter, String ladderName) throws RankingException {
         if (ladderName == null || ladderName.isEmpty()) {
             ladderName = "default";
         }
 
         int promoterRank = getPromoterRankAndCheck(promoter, ladderName);
         int rank = this.getRank(ladderName);
 
         PermissionGroup sourceGroup = this.getRankLadders().get(ladderName);
         PermissionGroup targetGroup = null;
 
         for (Map.Entry<Integer, PermissionGroup> entry : this.manager.getRankLadder(ladderName).entrySet()) {
             int groupRank = entry.getValue().getRank();
             if (groupRank >= rank) { // group have equal or lower than current rank
                 continue;
             }
 
             if (groupRank <= promoterRank) { // group have higher rank than promoter
                 continue;
             }
 
             if (targetGroup != null && groupRank <= targetGroup.getRank()) { // group have higher rank than target group
                 continue;
             }
 
             targetGroup = entry.getValue();
         }
 
         if (targetGroup == null) {
             throw new RankingException("User are not promoteable", this, promoter);
         }
 
         this.swapGroups(sourceGroup, targetGroup);
 
         this.callEvent(PermissionEntityEvent.Action.RANK_CHANGED);
 
         return targetGroup;
     }
 
     /**
      * Demotes user in specified ladder.
      * If user is not member of the ladder RankingException will be thrown
      * If demoter is not null and he is member of the ladder and 
      * his rank is lower then user's RankingException will be thrown too.
      * If there is no group to demote the user to RankingException would be thrown
      * 
      * @param promoter Specify null if action performed from console or by plugin
      * @param ladderName
      * @throws RankingException 
      */
     public PermissionGroup demote(PermissionUser demoter, String ladderName) throws RankingException {
         if (ladderName == null || ladderName.isEmpty()) {
             ladderName = "default";
         }
 
         int promoterRank = getPromoterRankAndCheck(demoter, ladderName);
         int rank = this.getRank(ladderName);
 
         PermissionGroup sourceGroup = this.getRankLadders().get(ladderName);
         PermissionGroup targetGroup = null;
 
         for (Map.Entry<Integer, PermissionGroup> entry : this.manager.getRankLadder(ladderName).entrySet()) {
             int groupRank = entry.getValue().getRank();
             if (groupRank <= rank) { // group have equal or higher than current rank
                 continue;
             }
 
             if (groupRank <= promoterRank) { // group have higher rank than promoter
                 continue;
             }
 
             if (targetGroup != null && groupRank >= targetGroup.getRank()) { // group have lower rank than target group
                 continue;
             }
 
             targetGroup = entry.getValue();
         }
 
         if (targetGroup == null) {
             throw new RankingException("User are not demoteable", this, demoter);
         }
 
         this.swapGroups(sourceGroup, targetGroup);
 
         this.callEvent(PermissionEntityEvent.Action.RANK_CHANGED);
 
         return targetGroup;
     }
 
     /**
      * Check if the user is in the specified ladder
      * 
      * @param ladder Ladder name
      * @return true on success, false otherwise
      */
     public boolean isRanked(String ladder) {
         return (this.getRank(ladder) > 0);
     }
 
     /**
      * Return user rank in specified ladder
      * 
      * @param ladder Ladder name
      * @return rank as int
      */
     public int getRank(String ladder) {
         Map<String, PermissionGroup> ladders = this.getRankLadders();
 
         if (ladders.containsKey(ladder)) {
             return ladders.get(ladder).getRank();
         }
 
         return 0;
     }
 
     /**
      * Return user's group in specified ladder 
      * 
      * @param ladder Ladder name
      * @return PermissionGroup object of ranked ladder group
      */
     public PermissionGroup getRankLadderGroup(String ladder) {
         if (ladder == null || ladder.isEmpty()) {
             ladder = "default";
         }
 
         return this.getRankLadders().get(ladder);
     }
 
     /**
      * Return all ladders the user is participating in
      * 
      * @return Map, key - name of ladder, group - corresponding group of that ladder
      */
     public Map<String, PermissionGroup> getRankLadders() {
         Map<String, PermissionGroup> ladders = new HashMap<String, PermissionGroup>();
 
         for (PermissionGroup group : this.getGroups()) {
             if (!group.isRanked()) {
                 continue;
             }
 
             ladders.put(group.getRankLadder(), group);
         }
 
         return ladders;
     }
 
     @Override
     public String[] getPermissions(String world) {
         if (!this.cachedPermissions.containsKey(world)) {
             List<String> permissions = new LinkedList<String>();
             this.getInheritedPermissions(world, permissions, true);
 
             this.cachedPermissions.put(world, permissions.toArray(new String[0]));
         }
 
         return this.cachedPermissions.get(world);
     }
 
     protected void getInheritedPermissions(String worldName, List<String> permissions, boolean groupInheritance) {
         permissions.addAll(Arrays.asList(this.getTimedPermissions(worldName)));
         permissions.addAll(Arrays.asList(this.getOwnPermissions(worldName)));
 
         if (worldName != null) {
             // World inheritance
             for (String parentWorld : this.manager.getWorldInheritance(worldName)) {
                 getInheritedPermissions(parentWorld, permissions, false);
             }
 
             // Common permissions
             getInheritedPermissions(null, permissions, false);
         }
 
         // Group inhertance
         if (groupInheritance) {
             for (PermissionGroup group : this.getGroups(worldName)) {
                 group.getInheritedPermissions(worldName, permissions, true);
             }
         }
     }
 
     @Override
     public void addTimedPermission(String permission, String world, int lifeTime) {
         super.addTimedPermission(permission, world, lifeTime);
 
         this.clearCache();
     }
 
     @Override
     public void removeTimedPermission(String permission, String world) {
         super.removeTimedPermission(permission, world);
 
         this.clearCache();
     }
 
     protected int getPromoterRankAndCheck(PermissionUser promoter, String ladderName) throws RankingException {
         if (!this.isRanked(ladderName)) { // not ranked
             throw new RankingException("User are not in this ladder", this, promoter);
         }
 
         int rank = this.getRank(ladderName);
         int promoterRank = 0;
 
         if (promoter != null && promoter.isRanked(ladderName)) {
             promoterRank = promoter.getRank(ladderName);
 
             if (promoterRank >= rank) {
                 throw new RankingException("Promoter don't have high enough rank to change " + this.getName() + "'s rank", this, promoter);
             }
         }
 
         return promoterRank;
     }
 
     protected void swapGroups(PermissionGroup src, PermissionGroup dst) {
         List<PermissionGroup> groups = new ArrayList<PermissionGroup>(Arrays.asList(this.getGroups()));
 
         groups.remove(src);
         groups.add(dst);
 
         this.setGroups(groups.toArray(new PermissionGroup[0]));
     }
 
     @Override
     public String getPrefix(String worldName) {
         if (this.cachedPrefix == null) {
            String localPrefix = this.getOwnPrefix(worldName);
             if (localPrefix == null || localPrefix.isEmpty()) {
                 for (PermissionGroup group : this.getGroups(worldName)) {
                     localPrefix = group.getPrefix();
                     if (localPrefix != null && !localPrefix.isEmpty()) {
                         break;
                     }
                 }
             }
 
             if (localPrefix == null) { // just for NPE safety
                 localPrefix = "";
             }
 
             this.cachedPrefix = localPrefix;
         }
 
         return this.cachedPrefix;
     }
 
     @Override
     public String getSuffix(String worldName) {
         if (this.cachedSuffix == null) {
            String localSuffix = this.getOwnSuffix(worldName);
             if (localSuffix == null || localSuffix.isEmpty()) {
                 for (PermissionGroup group : this.getGroups(worldName)) {
                     localSuffix = group.getSuffix();
                     if (localSuffix != null && !localSuffix.isEmpty()) {
                         break;
                     }
                 }
             }
 
             if (localSuffix == null) { // just for NPE safety
                 localSuffix = "";
             }
             this.cachedSuffix = localSuffix;
         }
 
         return this.cachedSuffix;
     }
 
     @Override
     protected String getMatchingExpression(String permission, String world) {
         String cacheId = world + ":" + permission;
         if (!this.cachedAnwsers.containsKey(cacheId)) {
             this.cachedAnwsers.put(cacheId, super.getMatchingExpression(permission, world));
         }
 
         return this.cachedAnwsers.get(cacheId);
     }
 
     protected void clearCache() {
         this.cachedPrefix = null;
         this.cachedSuffix = null;
 
         this.cachedGroups.clear();
         this.cachedPermissions.clear();
         this.cachedAnwsers.clear();
     }
 
     @Override
     public void setPrefix(String prefix, String worldName) {
         this.clearCache();
     }
 
     @Override
     public void setSuffix(String postfix, String worldName) {
         this.clearCache();
     }
 
     @Override
     public void remove() {
         this.clearCache();
 
         this.callEvent(PermissionEntityEvent.Action.REMOVED);
     }
 
     @Override
     public void save() {
         this.clearCache();
 
         this.callEvent(PermissionEntityEvent.Action.SAVED);
     }
 }
