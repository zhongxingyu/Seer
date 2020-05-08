 package com.lala.wordrank.misc;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import ru.tehkode.permissions.PermissionGroup;
 
 import com.lala.wordrank.WordRank;
 
 import de.bananaco.permissions.interfaces.PermissionSet;
 
 public class PermHandle {
 
 	private WordRank plugin;
 	private Perms perms;
 	private Player player;
 	
 	public PermHandle(WordRank plugin, Perms perms, Player player){
 		this.plugin = plugin;
 		this.perms = perms;
 		this.player = player;
 	}
 	
 	public void setGroup(String groupname){
 		if (perms.equals(Perms.bPermissions)){
 			List<PermissionSet> ps = plugin.bperm.getPermissionSets();
 			for (int i = 0; i < ps.size(); i++){
 				ps.get(i).setGroup(player, groupname);
 			}
 			return;
 		}
 		
 		if (perms.equals(Perms.PEX)){
 			String[] groups = new String[1];
 			groups[0] = groupname;
 			plugin.pex.getUser(player).setGroups(groups);
 			return;
 		}
 	}
 	
 	public ArrayList<String> getPlayerGroups(){
 		if (perms.equals(Perms.bPermissions)){
 			List<String> gl = plugin.bperm.getPermissionSet(player.getWorld()).getGroups(player.getName());
 			ArrayList<String> g = new ArrayList<String>();
 			g.addAll(gl);
 			return g;
 		}
 		
 		if (perms.equals(Perms.PEX)){
 			PermissionGroup[] gl = plugin.pex.getUser(player).getGroups();
 			ArrayList<String> g = new ArrayList<String>();
			for (int i = 0; i < gl.length; i++){
				g.add(gl[i].getName());
 			}
 			return g;
 		}
 		return null;
 	}
 }
