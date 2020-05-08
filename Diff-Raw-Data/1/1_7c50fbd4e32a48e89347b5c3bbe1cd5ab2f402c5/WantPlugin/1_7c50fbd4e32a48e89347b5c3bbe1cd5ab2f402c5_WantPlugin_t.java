 package net.preoccupied.bukkit.want;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.List;
 import java.util.HashMap;
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
 
 
 
 public class WantPlugin extends JavaPlugin {
 
 
     private Map<String,ItemData> items;
 
     private Map<String,PackData> packs;
 
     private Map<Integer,List<ItemData>> items_by_id;
 
 
 
     public void onEnable() {
 	setupCommands();
     }
 
 
     public void onDisable() {
 	;
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
 
 	System.out.println("loaded " + this.items_by_id.size() + " item IDs for Want");
 	System.out.println("loaded " + this.items.size() + " item aliases for Want");
 
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
 
 	System.out.println("loaded " + this.packs.size() + " packs for Want");
     }
 
 
 
     private static String alias_transform(String alias) {
 	alias = alias.replaceAll("\\s", "");
 	alias = alias.toLowerCase();
 	return alias;
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
 		ItemData item = getItem(itemname);
 
 		if(item == null) {
 		    msg(player, "I don't know what that is: " + itemname);
 		    return true;
 		}
 
 		if(! item.permitted(player)) {
 		    msg(player, "You are not permitted to spawn that: " + itemname);
 		    return true;
 		}
 
 		ItemUtils.spawnItem(player, item.id, (short) item.type, item.stack);
 		return true;
 	    }
 	};
 
 	new PermissionCommand(this, "grant") {
 	    public boolean run(Player player, String recipient, String itemname) {
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
 
 		ItemUtils.spawnItem(friend, item.id, (short) item.type, item.stack);
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
 
 		return true;
 	    }
 	};
 
     }
 
 
 }
 
 
 
 /* The end. */
