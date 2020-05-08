 package net.preoccupied.bukkit.want;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 import net.preoccupied.bukkit.ItemUtils;
 import net.preoccupied.bukkit.PluginConfiguration;
 import net.preoccupied.bukkit.permissions.PermissionCheck;
 import net.preoccupied.bukkit.permissions.PermissionCommand;
 
 
 /**
    @author Christopher O'Brien <obriencj@gmail.com>
  */
 public class WantPlugin extends JavaPlugin {
 
 
     private Map<String,ItemData> items;
 
     private Map<String,PackData> packs;
 
     private Map<Integer,List<ItemData>> items_by_id;
 
 
 
     public void onEnable() {
 	setupCommands();
	getServer().getLogger().info(this + " is enabled");
     }
 
 
     public void onDisable() {
	getServer().getLogger().info(this + " is disabled");
     }
 
 
     public void onLoad() {
 
 	/* item data */
 
 	this.items = new HashMap<String,ItemData>();
 	this.items_by_id = new TreeMap<Integer,List<ItemData>>();
 	
 	Configuration conf = null;
 
 	try {
 	    conf = PluginConfiguration.load(this, this.getFile(), "items.yml");
 	} catch(IOException ioe) {
 	    System.out.println(ioe);
 	    ioe.printStackTrace();
 	    return;
 	}
 
 	for(ConfigurationNode node : conf.getNodeList("items", null)) {
 	    int id = node.getInt("id", 0);
 
 	    List<ItemData> idata = loadItemData(id, node);
 	    this.items_by_id.put(id, idata);
 
 	    for(ItemData item : idata) {
 		for(String alias : item.aliases) {
 		    alias = alias_transform(alias);
 		    this.items.put(alias, item);
 		}
 	    }
 	}
 
 	/* we aren't going to bother storing groups, we'll just attach
 	   it as auxillary data to items in their membership */
 
 	for(ConfigurationNode node: conf.getNodeList("groups", null)) {
 	    String name = node.getString("name", "undefined");
 	    int stack = node.getInt("stack", 1);
 	    
 	    for(int id : node.getIntList("items", null)) {
 		List<ItemData> found = items_by_id.get(id);
 		if(found == null)
 		    continue;
 
 		for(ItemData i : found) {
 		    i.group = name;
 		    i.stack = stack;
 		}
 	    }	    
 	}
 
	getServer().getLogger().info("loaded " + this.items_by_id.size() + " item IDs");
	getServer().getLogger().info("loaded " + this.items.size() + " item aliases");
 
 	/* pack data */
 
 	this.packs = new HashMap<String,PackData>();
 
 	try {
 	    conf = PluginConfiguration.load(this, this.getFile(), "packs.yml");
 	} catch(IOException ioe) {
 	    System.out.println(ioe);
 	    return;
 	}
 
 	for(ConfigurationNode node : conf.getNodeList("packs", null)) {
 	    String name = node.getString("name", null);
 
 	    PackData pack = new PackData(name);
 	    pack.title = node.getString("title", name);
 	    pack.message = node.getString("message", null);
 
 	    for(ConfigurationNode itemnode : node.getNodeList("items", null)) {
 		int id = itemnode.getInt("id", 0);
 		int type = itemnode.getInt("type", 0);
 		int count = itemnode.getInt("count", 1);
 
 		if(id > 0 && count > 0) {
 		    pack.addItem(id, type, count);
 		}
 	    }
 	    
 	    this.packs.put(name, pack);
 	}
 
	getServer().getLogger().info("loaded " + this.packs.size() + " packs");
     }
 
 
 
     private static String alias_transform(String alias) {
 	alias = alias.replaceAll("\\s", "");
 	alias = alias.toLowerCase();
 	return alias;
     }
 
 
 
     private static final String globconvert(String pattern) {
         pattern = pattern.replace("\\","\\\\");
         pattern = pattern.replace(".", "\\.");
         pattern = pattern.replace("?", ".");
         pattern = pattern.replace("*", ".*");
         return pattern;
     }
 
 
 
     private static final String safestr(Object o) {
 	return (o != null)? o.toString(): "[null]";
     }
 
 
 
     private static List<ItemData> loadItemData(int id, ConfigurationNode node) {
 	List<String> names = node.getStringList("name", null);
 	
 	if(id == 0 || names == null || names.isEmpty()) {
 	    return Collections.emptyList();
 	}
 
 	List<ItemData> items = new ArrayList<ItemData>(1);
 
 	ItemData zerotype = new ItemData(id, names);
 	items.add(zerotype);
 
 	for(ConfigurationNode typenode : node.getNodeList("types", null)) {
 	    int typeid = typenode.getInt("type", 0);
 	    names = typenode.getStringList("name", null);
 	    
 	    if(names == null || names.isEmpty())
 		continue;
 
 	    if(typeid == 0) {
 		zerotype.aliases.addAll(names);
 
 	    } else {
 		ItemData ti = new ItemData(id, names);
 		ti.type = typeid;
 		items.add(ti);
 	    }
 	}
 	
 	return items;
     }
 
 
     
     public ItemData getItem(String by_name) {
 	if(by_name == null) return null;
 	return this.items.get(alias_transform(by_name));
     }
 
 
 
     public ItemData getItem(int by_id, int and_type) {
 	List<ItemData> items = this.items_by_id.get(by_id);
 	if(items == null) return null;
 
 	for(ItemData i : items) {
 	    if(i.type == and_type) {
 		return i;
 	    }
 	}
 
 	return null;
     }
 
 
 
     public ItemData getItem(int by_id) {
 	return getItem(by_id, 0);
     }
 
 
 
     public PackData getPack(String by_name) {
 	return packs.get(by_name);
     }
 
 
 
     private void setupCommands() {
 	
 	new PermissionCommand(this, "want") {
 	    public boolean run(Player player, String itemname) {
 		return run(player, itemname, null);
 	    }
 
 	    public boolean run(Player player, String itemname, String numstr) {
 		ItemData item = getItem(itemname);
 
 		if(item == null) {
 		    msg(player, "I don't know what that is: " + itemname);
 		    return true;
 		}
 
 		if(! item.permitted(player)) {
 		    msg(player, "You are not permitted to spawn that: " + itemname);
 		    return true;
 		}
 
 		int count = item.stack;
 		if(numstr != null) {
 		    count = Integer.parseInt(numstr);
 		}
 
 		ItemUtils.spawnItem(player, item.id, (short) item.type, count);
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "grant") {
 	    public boolean run(Player player, String recipient, String itemname) {
 		return run(player, recipient, itemname, null);
 	    }
 
 	    public boolean run(Player player, String recipient, String itemname, String numstr) {
 		ItemData item = getItem(itemname);
 
 		if(item == null) {
 		    msg(player, "Unknown item: " + itemname);
 		    return true;
 		}
 
 		if(! item.permitted(player)) {
 		    msg(player, "You are not permitted to spawn that: " + itemname);
 		    return true;
 		}
 
 		Player friend = Bukkit.getServer().getPlayer(recipient);
 		if(friend == null || ! friend.isOnline()) {
 		    msg(player, "Your friend is not online: " + recipient);
 		    return true;
 		}
 
 		int count = item.stack;
 		if(numstr != null) {
 		    count = Integer.parseInt(numstr);
 		}
 
 		ItemUtils.spawnItem(friend, item.id, (short) item.type, count);
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "pack") {
 	    public boolean run(Player player, String recipient, String packname) {
 		PackData pack = getPack(packname);
 
 		if(pack == null) {
 		    msg(player, "Unknown pack: " + packname);
 		    return true;
 		}
 
 		Player friend;
 		if(recipient.equals("me")) {
 		    friend = player;
 		} else {
 		    friend = Bukkit.getServer().getPlayer(recipient);
 		}
 		
 		if(friend == null || ! friend.isOnline()) {
 		    msg(player, "Your friend is not online: " + recipient);
 		    return true;
 		}
 
 		for(PackData.PackItem i : pack.items) {
 		    ItemUtils.spawnItem(friend, i.id, (short) i.type, i.count);
 		}
 
 		if(pack.message != null) {
 		    msg(friend, pack.message);
 		} else {
 		    msg(friend, "You've received " + pack.title);
 		}
 
		if(friend != player) {
		    msg(player, "You've given " + recipient + " " + pack.title);
		}

 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "item-search") {
 	    public boolean run(Player player, String glob) {
 		Set<ItemData> found = new HashSet<ItemData>();
 
 		String pattern = globconvert(glob);
 		for(Map.Entry<String,ItemData> entry : items.entrySet()) {
 		    if(entry.getKey().matches(pattern)) {
 			found.add(entry.getValue());
 		    }
 		}
 
 		if(found.isEmpty()) {
 		    msg(player, "No items found matching: " + glob);
 
 		} else {
 		    msg(player, "Found items:");
 		    for(ItemData id : found) {
 			msg(player, " " + id.getName());
 		    }
 		}
 
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "pack-list") {
 	    public boolean run(Player player) {
 		msg(player, "Pack names:");
 		for(PackData p : packs.values()) {
 		    msg(player, " " + p.name);
 		}
 		return true;
 	    }
 
 	    public boolean run(Player player, String glob) {
 		msg(player, "Pack names:");
 		String pattern = globconvert(glob);
 		for(PackData p : packs.values()) {
 		    if(p.name.matches(pattern)) {
 			if(p.title != null) {
 			    msg(player, " " + p.name + ": " + p.title);
 			} else {
 			    msg(player, " " + p.name);
 			}
 		    }
 		}
 		return true;
 	    }
 	};
 
 
 	new PermissionCommand(this, "pack-info") {
 	    public boolean run(Player player, String packname) {
 		PackData pack = getPack(packname);
 
 		if(pack == null) {
 		    msg(player, "Unknown pack: " + packname);
 
 		} else {
 		    msg(player, "Information for Pack: " + packname);
 		    msg(player, "  Title: " + safestr(pack.title));
 		    msg(player, "  Message: " + safestr(pack.message));
 		    msg(player, "  Items:");
 		    
 		    for(PackData.PackItem pi : pack.items) {
 			ItemData item = getItem(pi.id, pi.type);
 			if(item != null) {
 			    msg(player, "    " + pi.count + " x " + item.getName());
 			}
 		    }
 		}
 
 		return true;
 	    }
 	};
 
     }
 
 
 }
 
 
 
 /* The end. */
