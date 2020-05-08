 /**
  * The Permissions project.
  * Copyright (C) 2012 Walker Crouse
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package net.windwaker.permissions.data.sql;
 
 import net.windwaker.permissions.api.GroupManager;
 import net.windwaker.permissions.api.permissible.Group;
 
 import java.util.Set;
 
 public class SQLGroupManager implements GroupManager {
	private final SQLConnection connection = SQLConnection.getConnection();
 
 	@Override
 	public void load() {
		connection.update("INSERT INTO permissions_groups (name, default, per_world) VALUES (admin, false, false)");
 	}
 	
 	private void loadPermissions(Group group) {
 	}
 	
 	private void loadWorlds(Group group) {
 	}
 	
 	private void loadData(Group group) {
 	}
 
 	@Override
 	public void addGroup(String name) {
 	}
 
 	@Override
 	public void removeGroup(String name) {
 	}
 
 	@Override
 	public Group getGroup(String name) {
 		return null;
 	}
 
 	@Override
 	public Set<Group> getGroups() {
 		return null;
 	}
 
 	@Override
 	public void saveGroup(Group group) {
 	}
 }
