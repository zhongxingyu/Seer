 /**
  * This file is part of Orion source code
  * 
  * Copyright (C) 2012 [Gore]Clan - http://www.goreclan.net
  *
  * Orion is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Lesser Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Orion. If not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * @author      Daniele Pantaleone
  * @version     1.2
  * @copyright   Daniele Pantaleone, 20 October, 2012
  * @package     com.orion.control
  **/
 
 package com.orion.control;
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 
 import com.orion.bot.Orion;
 import com.orion.dao.GroupDao;
 import com.orion.dao.MySqlGroupDao;
 import com.orion.domain.Group;
 
 public class GroupC {
    
     private final Log log;
     private final GroupDao dao;
     private List<Group> groups;
     
     
     /**
      * Object constructor.
      * 
      * @author Daniele Pantaleone
      * @param  orion <tt>Orion</tt> object reference
      **/
     public GroupC(Orion orion) {
         this.log = orion.log;
         this.dao = new MySqlGroupDao(orion.storage);
         this.groups = new LinkedList<Group>();
     }
     
     
     /**
      * Return the <tt>Group</tt> object matching the specified <tt>Group</tt> id.
      * The search is performed at first on the prebuilt group list.
      * If it's empty it will be loaded from the storage level so the search can be performed.
      * 
      * @author Daniele Pantaleone
      * @param  id The <tt>Group</tt> object primary key
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @return A <tt>Group</tt> object matching the given id or <tt>null</tt> if we have no match
      **/
     public Group getById(int id) throws ClassNotFoundException, SQLException {
         
         if (this.groups == null || this.groups.size() == 0) 
             this.groups = this.dao.loadAll();
         
         for (Group group : this.groups)
            if (group.id == id)
                 return group;
         
         return null;
         
     }
     
     
     /**
      * Return the <tt>Group</tt> matching the specified <tt>Group</tt> name.
      * The search is performed at first on the prebuilt group list.
      * If it's empty it will be loaded from the storage level so the search can be performed.
      * 
      * @author Daniele Pantaleone
      * @param  name The <tt>Group</tt> name
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @return A <tt>Group</tt> object matching the given name or <tt>null</tt> if we have no match
      **/
     public Group getByName(String name) throws ClassNotFoundException, SQLException {
         
         if (this.groups == null || this.groups.size() == 0) 
             this.groups = this.dao.loadAll();
         
         for (Group group : this.groups)
            if (group.name.equals(name))
                 return group;
         
         return null;
         
     }
     
     
     /**
      * Return the <tt>Group</tt> matching the specified <tt>Group</tt> keyword.
      * The search is performed at first on the prebuilt group list.
      * If it's empty it will be loaded from the storage level so the search can be performed.
      * 
      * @author Daniele Pantaleone
      * @param  id The <tt>Group</tt> object keyword
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @return A <tt>Group</tt> object matching the given keyword or <tt>null</tt> if we have no match
      **/
     public Group getByKeyword(String keyword) throws ClassNotFoundException, SQLException {
         
         if (this.groups == null || this.groups.size() == 0) 
             this.groups = this.dao.loadAll();
         
         // Lowercasing the keyword.
         keyword = keyword.toLowerCase();
         
         for (Group group : this.groups)
            if (group.keyword.equals(keyword))
                 return group;
         
         return null;
         
     }
     
     
     /**
      * Return the <tt>Group</tt> matching the specified pattern.
      * The search is performed at first on the prebuilt group list.
      * If it's empty it will be loaded from the storage level so the search can be performed.
      * The pattern will be matched with the group <tt>level</tt> or the group <tt>keyword</tt>
      * 
      * @author Daniele Pantaleone
      * @param  id The <tt>Group</tt> object keyword
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @return A <tt>Group</tt> object matching the given keyword or null if we have no match
      **/
     public Group getByMagic(String pattern) throws ClassNotFoundException, SQLException {
         if (!pattern.matches("^\\d+$")) return this.getByKeyword(pattern);
         else return this.getByLevel(Integer.parseInt(pattern));
     }
     
     
     /**
      * Return the <tt>Group</tt> object matching the specified Group level.
      * The search is performed at first on the prebuilt group list.
      * If it's empty it will be loaded from the storage level so the search can be performed.
      * 
      * @author Daniele Pantaleone
      * @param  level The <tt>Group</tt> object level
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      * @return A <tt>Group</tt> object matching the given level or null if we have no match
      **/
     public Group getByLevel(int level) throws ClassNotFoundException, SQLException {
         
         if (this.groups == null || this.groups.size() == 0) 
             this.groups = this.dao.loadAll();
         
         for (Group group : this.groups)
            if (group.level == level)
                 return group;
         
         return null;
         
     }
     
     
     /**
      * Insert a new <tt>Group</tt> object in the database.
      *
      * @author Daniele Pantaleone
      * @param  group The <tt>Group</tt> object to be saved in the database
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      **/
     public void insert(Group group) throws ClassNotFoundException, SQLException {
         this.log.trace("[SQL] INSERT `groups`: " + group.toString());
         this.dao.insert(group);
     }
     
     
     /**
      * Update the <tt>Group</tt> object informations in the database.
      *
      * @author Daniele Pantaleone
      * @param  group The <tt>Group</tt> object to be updated in the database
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      **/
     public void update(Group group) throws ClassNotFoundException, SQLException {
         this.log.trace("[SQL] UPDATE `groups`: " + group.toString());
         this.dao.update(group);
     }
     
     /**
      * Delete the <tt>Group</tt> object from the database.
      *
      * @author Daniele Pantaleone
      * @param  group The <tt>Client</tt> object to be deleted from the database
      * @throws ClassNotFoundException If the JDBC driver fails in being loaded
      * @throws SQLException If the load query fails somehow
      **/
     public void delete(Group group) throws ClassNotFoundException, SQLException {
         this.log.trace("[SQL] DELETE `groups`: " + group.toString());
         this.dao.delete(group);
     }
     
 }
