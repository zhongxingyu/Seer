 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.model.logic;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.dao.UserDao;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.pages.Pagination;
 
 import java.util.List;
 
 /**
  * Class for working with users banning
  *
  * @author stanislav bashkirtsev
  * @author maxim reshetov
  */
 public class UserBanner {
     public static final String BANNED_USERS_GROUP_NAME = "Banned Users";
     private final GroupDao groupDao;
     private final UserDao userDao;
 
     public UserBanner(GroupDao groupDao, UserDao userDao) {
         this.groupDao = groupDao;
         this.userDao = userDao;
     }
 
     /**
      * Gets a {@link UserList} of banned users from banned users group.
      *
      * @return the List of {@link PoulpeUser} with banned users.
      */
     public List<PoulpeUser> getAllBannedUsers() {
         List<Group> bannedUserGroups = getBannedUsersGroups();
         return userDao.getUsersInGroups(bannedUserGroups);
     }
 
     /**
      * Gets List of {@link PoulpeUser} unbanned users
      *
      * @param availableFilterText Filter (like '%%') to username
      * @param pagination          Params to limit
      * @return List of {@link PoulpeUser}
      *         //
      */
     public List<PoulpeUser> getNonBannedUsersByUsername(String availableFilterText, Pagination pagination) {
         List<Group> bannedUserGroups = getBannedUsersGroups();
         return userDao.findUsersNotInGroups(availableFilterText, bannedUserGroups, pagination);
     }
 
     /**
      * Adds users to banned users group.
      *
      * @param usersToBan {@link UserList} with users to ban
      */
     public void banUsers(UserList usersToBan) {
         Group bannedUserGroup = getBannedUsersGroups().get(0);
         bannedUserGroup.getUsers().addAll(usersToBan.getUsers());
         groupDao.saveOrUpdate(bannedUserGroup);
     }
 
     /**
      * Revokes ban from users, deleting them from banned users group.
      *
      * @param usersToRevoke {@link UserList} with users to revoke ban.
      */
     public void revokeBan(UserList usersToRevoke) {
         Group bannedUserGroup = getBannedUsersGroups().get(0);
         bannedUserGroup.getUsers().removeAll(usersToRevoke.getUsers());
         groupDao.saveOrUpdate(bannedUserGroup);
     }
 
 
     /**
      * Create group to ban
      *
      * @return {@link Group} of ban
      */
     private Group createBannedUserGroup() {
         Group bannedUsersGroup = new Group(BANNED_USERS_GROUP_NAME, "Banned Users");
         groupDao.saveOrUpdate(bannedUsersGroup);
         return bannedUsersGroup;
     }
 
     /**
      * Search and return list of banned groups.  If groups wasn't found in database, then creates new one.Note, that
      * creating of this group is a temporal solution until we implement Permission Schemas.
      *
      * @return List of banned groups
      */
     public List<Group> getBannedUsersGroups() {
         List<Group> bannedUserGroups = groupDao.getByName(BANNED_USERS_GROUP_NAME);
         if (bannedUserGroups.isEmpty()) {
             bannedUserGroups.add(createBannedUserGroup());
         }
 
         return bannedUserGroups;
     }
 }
