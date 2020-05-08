 package gmail.theultimatebudgie.liteperms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 
 import com.google.common.collect.Lists;
 
 public class Group {
 	
 	private static final ArrayList<Group> GROUPS;
 	private static Group DEFAULT;
 	
 	static {
 		GROUPS = new ArrayList<Group>();
 	}
 	
 	public static void setGroups(ArrayList<Group> groups) {
 		GROUPS.clear();
 		for (Group g : groups) {
 			GROUPS.add(g);
 		}
 	}
 	
 	public static ArrayList<Group> getGroups() {
 		return GROUPS;
 	}
 	
 	public static Group getGroupByName(String name) {
     	for (Group g : GROUPS){
     		if (g.getName().equalsIgnoreCase(name)) {
     			return g;
     		}
     	}
     	return null;
     }
 	
 	public static void setDefaultGroup(Group group){
 		if (DEFAULT == null) {
 			DEFAULT = group;
 		}
     }
 	
 	public static Group getDefaultGroup() {
 		return DEFAULT;
 	}
 	
 	//###############################################
 	
 	private final String name;
 	
 	private Group parent;
 	private Group child;
 	private List<Group> inherits;
 	
 	private boolean allPerms;
 	private String chatPrefix;
 	
 	private List<String> permissions;
 	
 	public Group (String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public List<Group> getAllParents() {
 		ArrayList<Group> parents = new ArrayList<>();
 		
 		Group current = this;
 		boolean moreParents = true;
 		while (moreParents) {
 			if (current.getParent() == null || parents.contains(current.getParent())) {
 				moreParents = false;
 			} else {
 				parents.add(current.getParent());
 				current = current.getParent();
 			}
 		}
 		
 		return Lists.reverse(parents);
 	}
 	
 	public Group getParent() {
 		return parent;
 	}
 
 	public void setParent(Group parent) {
 		this.parent = parent;
 	}
 
 	public Group getChild() {
 		return child;
 	}
 
 	public void setChild(Group child) {
 		this.child = child;
 	}
 
 	public List<Group> getInherits() {
 		return inherits;
 	}
 
 	public void setInherits(List<Group> inherits) {
 		this.inherits = inherits;
 	}
 
 	public List<String> getPermissions() {
 		return permissions;
 	}
 	
 	public void addPermission(String perm) {
 		permissions.add(perm);
 	}
 	
 	public void setPermissions(List<String> permissions) {
 		this.permissions = permissions;
 	}
 
 	public boolean isAllPerms() {
 		return allPerms;
 	}
 
 	public void setAllPerms(boolean op) {
 		this.allPerms = op;
 	}
 
 	public String getChatPrefix() {
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
