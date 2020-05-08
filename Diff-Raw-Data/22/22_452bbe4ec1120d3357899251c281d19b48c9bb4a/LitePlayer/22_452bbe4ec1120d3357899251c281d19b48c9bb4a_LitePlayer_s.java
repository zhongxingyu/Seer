 package gmail.theultimatebudgie.liteperms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 
 public class LitePlayer {
 	private static final ArrayList<LitePlayer> PLAYERS;
 	
 	static {
 		PLAYERS = new ArrayList<LitePlayer>();
 	}
 	
 	public static List<LitePlayer> getAllPlayers() {
 		return PLAYERS;
 	}
 	
 	public static LitePlayer getPlayerByName(String name) {
 		for (LitePlayer p : PLAYERS) {
 			if (p.getName().equalsIgnoreCase(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 	
 	public static void addPlayer (LitePlayer player) {
 		PLAYERS.add(player);
 	}
 	
 	//#######################################################
 	
 	private final String name;
 	private final PermissionAttachment attachment;
 	
 	private Group group;
 	private List<String> extraPermissions;
 	private String chatPrefix;
 	
 	public LitePlayer (String name, PermissionAttachment pa) {
 		this.name = name;
 		this.attachment = pa;
 	}
 	
 	public void remove(Player p) {
 		PLAYERS.remove(this);
 		p.removeAttachment(attachment);
 	}
 	
 	public void updatePermissions() {
 		//Remove all current permissions
 		for (String p : attachment.getPermissions().keySet()){
 			attachment.unsetPermission(p);
 		}
 		
 		//Go through every parent group!
 		List<Group> parents = group.getAllParents();
 		for (Group p : parents) {
 			List<String> parentPerms = getAllGroupPermissions(p);
 			for (String s : parentPerms) {
 				addPermission(s);
 			}
 		}
 		
 		//Check for allPerms/Op
 		if (group.isAllPerms()) {
 			for (Permission p : Bukkit.getPluginManager().getPermissions()){
 				attachment.setPermission(p, true);
 			}
 		}
 		
 		//Finally, give group perms (highest priority)
 		List<String> groupPerms = getAllGroupPermissions(group);
 		for (String s : groupPerms) {
 			addPermission(s);
 		}
 		
 		//More Finally, give extra permissions:
 		for (String s : extraPermissions) {
 			addPermission(s);
 		}
 	}
 	
 	private List<String> getAllGroupPermissions (Group g) {
 		ArrayList<String> perms = new ArrayList<>();
 		perms.addAll(g.getPermissions());
 		for (Group inherits : g.getInherits()) {
 			perms.addAll(getAllGroupPermissions(inherits));
 		}
 		return perms;
 	}
 	
 	private void addPermission(String perm) {	
 		perm = perm.trim();
 		boolean give;
 		if (perm.startsWith("-")) {
 			give = false;
 			perm = perm.substring(1);
 		} else {
 			give = true;
 		}
 		
 		if (perm.contains(".*")) {
 			int index = perm.lastIndexOf(".*");
 			String firstPart = perm.substring(0,index-1);
 			for (Permission p : Bukkit.getPluginManager().getPermissions()){
 				if (p.getName().contains(firstPart)){
 					attachment.setPermission(p, give);
 				}
 			}
 		} else {
 			attachment.setPermission(perm, give);
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Group getGroup() {
 		return group;
 	}
 
 	public void setGroup(Group group) {
 		this.group = group;
 	}
 
 	public List<String> getExtraPermissions() {
 		return extraPermissions;
 	}
 	
 	public void addExtraPermission(String perm) {
 		extraPermissions.add(perm);
 	}
 
 	public void setExtraPermissions(List<String> extraPermissions) {
 		this.extraPermissions = extraPermissions;
 	}
 
 	public String getChatPrefix() {
 		if (chatPrefix == null) {
 			return group.getChatPrefix();
 		}
 		return chatPrefix;
 	}
 	
 	public void removeChatPrefix() {
 		chatPrefix = null;
 	}
 
 	public void setChatPrefix(String chatPrefix) {
 		if (chatPrefix == null) {
 			removeChatPrefix();
 			return;
 		}
 		this.chatPrefix = ChatColor.translateAlternateColorCodes('&', chatPrefix);
 	}
 
 }
