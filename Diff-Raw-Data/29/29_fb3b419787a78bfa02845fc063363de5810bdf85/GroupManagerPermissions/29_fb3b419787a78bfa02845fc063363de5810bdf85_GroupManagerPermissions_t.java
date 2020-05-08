 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 
 package com.iminurnetz.bukkit.permissions;
 
import java.util.List;

 import org.anjocaido.groupmanager.data.Group;
 import org.anjocaido.groupmanager.data.User;
 import org.anjocaido.groupmanager.dataholder.WorldDataHolder;
 import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
 import org.bukkit.entity.Player;
 
 public class GroupManagerPermissions implements PermissionHandler {
 
 	private WorldsHolder worldHolder;
 	
 	public GroupManagerPermissions(WorldsHolder wd) {
 		this.worldHolder = wd;
 	}
 
 	@Override
 	public boolean hasPermission(Player player, String permission) {
 		return worldHolder.getWorldPermissions(player).permission(player, permission);
 	}
 	
 	public boolean setGroup(String name, String newGroup) {
 	    WorldDataHolder holder = worldHolder.getWorldDataByPlayerName(name);
 	    if (holder != null) {
 	        return setGroup(holder.getUser(name), newGroup, holder.getName());
 	    }
 	    
 	    return false;
 	}
 	
 	public boolean setGroup(Player player, String newGroup) {
 	    User u = worldHolder.getWorldData(player).getUser(player.getName());
 	    return setGroup(u, newGroup, player.getWorld().getName());
 	}
 	
 	public boolean setGroup(User u, String newGroup, String world) {
 	    Group group = worldHolder.getWorldData(world).getGroup(newGroup);
  	    if (group != null) {
 	        u.setGroup(group);
 	        u.flagAsChanged();
 	        return true;
 	    }
 	    
 	    return false;
 	}
 
     @Override
     public String getGroup(Player player) {
         User u = worldHolder.getWorldData(player).getUser(player.getName());
         return u.getGroup().getName();
     }
 
     @Override
     public boolean parentGroupsInclude(Player player, String group) {
         Group g = worldHolder.getWorldData(player).getUser(player.getName()).getGroup();
        return g.getName().equalsIgnoreCase(group) || inheritsInclude(player, g.getName(), group);
        
    }
    
    private boolean inheritsInclude(Player player, String group, String name) {
        Group theGroup = worldHolder.getWorldData(player).getGroup(group);
        
        for (String g : theGroup.getInherits()) {
            if (g.equalsIgnoreCase(name) || inheritsInclude(player, g, name)) {
                return true;
            }
        }
        
        return false;
     }
 }
