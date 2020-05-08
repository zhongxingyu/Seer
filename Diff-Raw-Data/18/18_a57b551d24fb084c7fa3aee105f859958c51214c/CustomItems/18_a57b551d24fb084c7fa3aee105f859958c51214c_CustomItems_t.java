 package me.meiamsome.recipelookup; //Change the package to be inside your own package.
 
 /* CustomItems by meiamsome
  * 
  * CustomItems must be initialised before use with:
  * new CustomItems(Plugin);
  * There is no need to keep the instance, all other methods are static
  * 
  * Public Static Methods :
  * ItemStack getItemStack(String name)						Returns the ItemStack matching name.  TODO: Enchantments & Specified quantities
  * ItemStack getItemStack(String name, boolean aprox)		Returns null if none could be found.
  * MaterialData getMaterial(String name)					Returns the ItemStack matching name.
  * MaterialData getMaterial(String name, boolan aprox)		Returns null if none could be found.
  * If aprox is omitted, it is taken to be false.
  * 
  * String getItemName(ItemStack)							Returns the preferred name for the ItemStack's MaterialData along with numerics
  * String getMaterialName(MaterialData) 					Returns the preferred name for the MaterialData specified
  * String getMaterialNames(MaterialData) 					Returns all possible names for the specified MaterialData
  */
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandMap;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.SimplePluginManager;
 
 
 public class CustomItems implements CommandExecutor {
 	private static Server server;
 	private static List<Object> others = null;
 	private static CustomItems self;
 	private static boolean allErrors, loading = false;
 	private static final boolean debug = false;
	private static FileConfiguration config, items;
 	private static Plugin plugin;
 	private static String dataSplit;
 	private static String capRegex;
 	private static char dataSplitChar;
 	private static HashMap<MaterialData, String> preferredNames = new HashMap<MaterialData, String>();
 	private static HashMap<MaterialData, List<String>> names = new HashMap<MaterialData, List<String>>();
 	private static HashMap<String, MaterialData> ids = new HashMap<String, MaterialData>();
 	public static final int version = 0;
 	
 
 	public static ItemStack getItemStack(String name) {return getItemStack(name, false);}
 	public static ItemStack getItemStack(String name, boolean aprox) {
 		MaterialData m = getMaterial(name, aprox);
 		if(m == null) return null;
 		ItemStack out = m.toItemStack();
 		return out;
 	}
 	
 	public static MaterialData getMaterial(String name)  {return getMaterial(name, false);}
 	public static MaterialData getMaterial(String name, boolean aprox) {
 		String in = formatName(name);
 		String input[] = in.split("["+dataSplit+"]");
 		if(input.length == 3) return null;
 		Byte dataVal = null;
 		if(input.length == 2)
 			dataVal = Byte.parseByte(input[1]);
 		if(dataVal != null) 
 			in = input[0] + dataSplitChar + "" + input[1];
 		if(ids.containsKey(in)) return ids.get(in);
 		if(input[0].matches("[0-9]*")) return new MaterialData(Integer.parseInt(input[0]), dataVal == null? -1 : dataVal);
 		while(input[0].length() > 0) {
 			if(checkIds(input[0])!=null) {
 				String a = checkIds(input[0]);
 				if(ids.containsKey(a + dataSplitChar + "" + dataVal)) return ids.get(a + dataSplitChar + "" + dataVal).clone();
 				MaterialData md = ids.get(a).clone();
 				if(dataVal!=null) md.setData(dataVal);
 				return md;
 			}
 			if(checkMaterial(input[0])!=null) return Material.getMaterial(checkMaterial(input[0])).getNewData(dataVal == null ? -1 : dataVal);
 			if(!aprox) return null;
 			input[0] = input[0].substring(0, input[0].length()-1);
 		}
 		return null;
 	}
 	public static String getItemName(ItemStack is) {
 		return is.getAmount()+" "+getMaterialName(is.getData());
 	}
 	public static String getMaterialName(MaterialData md) {
 		if(md == null) return null;
 		if(preferredNames.containsKey(md)) return preferredNames.get(md);
 		MaterialData md2 = md.clone();
 		md2.setData((byte) -1);
 		if(preferredNames.containsKey(md2)) return preferredNames.get(md2) + (md.getData() == -1 ? "" : dataSplitChar + "" + md.getData());
 		return reverseName(md.getItemType().name()) + (md.getData() == -1 ? "" : dataSplitChar + "" + md.getData());
 	}
 	
 	public static List<String> getMaterialNames(MaterialData md) {
 		ArrayList<String> ret = new ArrayList<String>();
 		if(names.containsKey(md)) {
 			List<String> l = names.get(md);
 			for(String a: l) ret.add(a);
 		}
 		MaterialData md2 = md.clone();
 		md2.setData((byte)-1);
 		if(names.containsKey(md2)) {
 			List<String> l = names.get(md2);
 			for(String a: l) ret.add((a + dataSplitChar) + md.getData());
 		}
 		ret.add((reverseName(md.getItemType().name()) + dataSplitChar) + md.getData());
 		ret.add("" + md.getItemTypeId() + "" + dataSplitChar + "" + md.getData());
 		if(md.getData() == -1) {
 			ret.add(reverseName(md.getItemType().name()));
 			ret.add(""+md.getItemTypeId());
 		}
 		return ret;
 	}
 	public static void addName(MaterialData md, String name, boolean preferred) {
 		if(!preferredNames.containsKey(md)) preferred = true;
 		ids.put(formatName(name), md);
 		if(!names.containsKey(md)) names.put(md, new ArrayList<String>());
 		if(!names.get(md).contains(name)) names.get(md).add(name);
 		if(preferred) {
 			if(preferredNames.containsKey(md) && !names.get(md).contains(preferredNames.get(md))) 
 				names.get(md).add(preferredNames.get(md));
 			preferredNames.put(md, name);
 			if(debug) System.out.println("Put `"+name+"` as `"+formatName(name)+"` for item "+md.getItemTypeId()+":"+md.getData());
 		} else if(debug) System.out.println("Put `"+name+"` as `"+formatName(name)+"` for item "+md.getItemTypeId()+":"+md.getData());
 		if(loading) return;
 		//Complex config shtuff
 		List<String> matNames = getMaterialNames(md);
 		ConfigurationSection cs = null;
 		for(String n: matNames) {
 			if(cs != null) break;
			cs = items.getConfigurationSection("Items."+n);
 		}
		if(cs == null) cs = items.createSection("Items."+md.getItemTypeId() +(md.getData() == -1?  "" : dataSplitChar + "" + md.getData()));
 		cs.set("Names", names.get(md));
 		if(preferred) {
 			cs.set("PreferredName", name);
 		}
 		save();
 	}
 	public static void removeName(MaterialData md, String name) {
 		if(names.containsKey(md)) {
 			names.get(md).remove(name);
 			if(names.get(md).size() == 0) names.remove(md);
 		}
 		if(preferredNames.containsKey(md) && preferredNames.get(md).equals(name)) {
 			preferredNames.remove(md);
 			if(names.containsKey(md)) addName(md, names.get(md).get(0), true);
 		}
 		if(loading) return;
 		//Complex config shtuff
 		List<String> matNames = getMaterialNames(md);
 		ConfigurationSection cs = null;
 		for(String n: matNames) {
 			if(cs != null) break;
			cs = items.getConfigurationSection("Items."+n);
 		}
		if(cs == null) cs = items.createSection("Items."+md.getItemTypeId() +(md.getData() == -1?  "" : dataSplitChar + "" + md.getData()));
 		if(preferredNames.containsKey(md))
 			cs.set("PreferredName", preferredNames.get(md));
 		else cs.set("PreferredName", null);
 		if(names.containsKey(md))
 			cs.set("Names", names.get(md));
 		else cs.set("Names", null);
 		save();
 	}
 	
 	private static String checkIds(String in) {
 		ArrayList<String> res = new ArrayList<String>();
 		for(String s: ids.keySet()) {
 			if(s.startsWith(in)) res.add(s);
 		}
 		if(res.size() == 0) return null;
 		while(res.size()>1) res.remove(res.get(0).length() < res.get(1).length()? 1 : 0);
 		return res.get(0);
 	}
 	private static String checkMaterial(String in) {
 		ArrayList<String> res = new ArrayList<String>();
 		for(Material m: Material.values()) if(formatName(m.name()).startsWith(in)) res.add(m.name());
 		if(res.size() == 0) return null;
 		while(res.size()>1) res.remove(res.get(0).length() < res.get(1).length()? 1 : 0);
 		return res.get(0);
 		
 	}
 	
 	
 	
 	private static String reverseName(String name) {
 		String ret = name.replaceAll("_([A-Z])", " $1").toLowerCase();
 		if(ret == null) return ret;
 		String[] split = ret.split(capRegex);
 		ret = "";
 		for(String a: split) {
 			if(a.length() > 0) ret += a.substring(0,1).toUpperCase();
 			if(a.length() > 1) ret += a.substring(1);
 		}
 		return ret;
 	}
 	
 	private static String formatName(String name) {
 		if(dataSplit.length() <= 1) return name.toLowerCase().replaceAll("[^A-Za-z0-9"+dataSplit+"]", "");
 		return name.toLowerCase().replaceAll("[^A-Za-z0-9"+dataSplit+"]", "").replaceAll("["+dataSplit.substring(1)+"]", Character.toString(dataSplitChar));
 	}
 	
 	
 	/* Handle the command setup */
 	public static void addOther(Object o) {//Called via reflection
 		others.add(o);
 		if(debug) System.out.println("Adding "+o.getClass().getCanonicalName()+" to list.");
 	}
 	
 	public static void transfer(Object o) throws Exception {//Called via reflection to transfer owner
 		Method meth = o.getClass().getMethod("addOther", Object.class);
 		if(debug) System.out.println("Transfer.");
 		for(Object other: others)
 			meth.invoke(o, other);
 		meth.invoke(o, self);
 		others = null;
 	}
 	
 	private static void getServer() throws Exception {
 		if(server != null) return;
 		if(self == null) throw new Exception("CustomItems was not initialised correctly!");
 		server = Bukkit.getServer();
 		if(server == null) return;
 		CommandExecutor c = getCommand(false);
 		if(c != null && !c.equals(self)) {
 			try {
 				Field f = c.getClass().getDeclaredField("version");
 				if(version > (int) f.get(c)) {
 					if(debug) System.out.print("CustomItems: Stealing ownership of cluster!");
 					if(getCommand(true) != null) {//Cause the theft to occur!
 						Method meth = c.getClass().getMethod("transfer", Object.class);
 						meth.invoke(c, self);
 						return;
 					} else if(debug) System.out.print("CustomItems: Failed to steal ownership!");
 				}
 				Method meth = c.getClass().getMethod("addOther", Object.class);
 				meth.invoke(c, self);
 			} catch(Exception e) {
 				if(allErrors) throw e;
 			}
 		}
 	}
 	
 	public static CommandExecutor getCommand(boolean steal) throws Exception {
 		try {
 			Field f = SimplePluginManager.class.getDeclaredField("commandMap");
 			f.setAccessible(true);
 			CommandMap cm = (CommandMap) f.get(server.getPluginManager());
 			Command ret = cm.getCommand(config.getString("Command.Name"));
 			if(ret != null) {
 				if(!steal) return getExecutor(ret);
 				Method m = ret.getClass().getDeclaredMethod("setExecutor", CommandExecutor.class);
 				m.invoke(ret, self);
 			} else {
 				cm.register("_", self.new CustomItemsCommand());
 			}
 			others = new ArrayList<Object>();
 			return getExecutor(ret == null ? cm.getCommand(config.getString("Command.Name")) : ret);
 		} catch(Exception e) {
 			if(!allErrors) return null;	//Can't set the command up :(
 			throw e; //Pass the exception up.
 		}
 	}
 	private static CommandExecutor getExecutor(Command c) throws Exception {
 		Method m = c.getClass().getDeclaredMethod("getExecutor");
 		return (CommandExecutor) m.invoke(c);
 	}
 	
 	//Configuration
 	private static FileConfiguration loadConfig() {
 		return loadConfig(plugin.getDataFolder().getParentFile());
 	}
 	private static FileConfiguration loadConfig(File base) {
 		System.out.print("CustomItems: Loading configuration");
 		loading = true;
 		File actual = new File(new File(base, "CustomItems"), "config.yml");
 		config = YamlConfiguration.loadConfiguration(actual);
 		config.addDefault("Command.Name", "CustomItems");
 		List<String> alts = new ArrayList<String>();
 		alts.add("ItemNames");
 		config.addDefault("Command.AlternateNames", alts);
 		config.addDefault("Command.AdminPermission", "CustomItems.admin");
 		config.addDefault("Command.BasePermission", "CustomItems.base");
 		config.addDefault("AllErrors", false);
 		config.addDefault("Data Value Split Characters", ":");
 		config.addDefault("Capitalize Regex", "\\b");
 		config.options().copyDefaults(true);
 		try {
 			config.save(actual);
 		} catch (IOException e) {}
 		File items = new File(new File(base, "CustomItems"), "items.yml");
 		if(!items.exists()) {
 			System.out.println("Downloading base item list.");
 			BufferedInputStream fi = null;
 			FileOutputStream fo = null;
 			try {
 				fi = new BufferedInputStream(new URL("https://raw.github.com/meiamsome/CustomItems/master/Items.yml").openStream());
 				fo = new FileOutputStream(items);
 				int a = fi.read();
 				while(a != -1) {
 					fo.write(a);
 					byte[] b = new byte[fi.available()];
 					fi.read(b);
 					fo.write(b);
 					a = fi.read();
 				}
 			} catch (Exception e) {
 				System.out.println("Failed to download items:");
 				e.printStackTrace();
 			} finally {
 				if(fi != null)
 					try {
 						fi.close();
 					} catch (IOException e1) {}
 				if(fo != null)
 					try {
 						fo.close();
 					} catch (IOException e) {}
 			}
 		}
 		dataSplitChar = config.getString("Data Value Split Characters").charAt(0);
 		dataSplit = config.getString("Data Value Split Characters").replaceAll("([\\\\\\[\\]])", "\\$1");
 		capRegex = config.getString("Capitalize Regex");
 		setupHashMaps(items);
 		allErrors = config.getBoolean("AllErrors");
 		getPermission(config.getString("Command.BasePermission")).setDefault(PermissionDefault.TRUE);
 		getPermission(config.getString("Command.AdminPermission")).setDefault(PermissionDefault.OP);
 		loading = false;
 		return config;
 	}
 	
 	private static Permission getPermission(String permission) {
 		Permission p = Bukkit.getPluginManager().getPermission(permission);
 		if(p != null) return p;
 		p = new Permission(permission);
 		Bukkit.getPluginManager().addPermission(p);
 		return p;
 	}
 	
 	private static void save() {
 		save(plugin.getDataFolder().getParentFile());
 	}
 	private static void save(File base) {
 		System.out.print("CustomItems: Saving configuration");
 		File actual = new File(new File(base, "CustomItems"), "config.yml");
		File itemfile = new File(new File(base, "CustomItems"), "items.yml");
 		try {
 			config.save(actual);
			items.save(itemfile);
 		} catch (IOException e) {
 			System.out.print("CustomItems: Failed to save configuration");
 			e.printStackTrace();
 		}
 	}
 	
 	private static void setupHashMaps(File file) {
 		preferredNames.clear();
 		names.clear();
 		ids.clear();
 		if(!file.exists()) return;
 		ConfigurationSection items = YamlConfiguration.loadConfiguration(file).getConfigurationSection("Items");
 		if(items == null) return;
 		Set<String> keys = items.getKeys(false);
 		int quant = 0;
 		for(String key: keys) {
 			try {
 				ConfigurationSection cs = items.getConfigurationSection(key);
 				MaterialData md = getMaterial(key, true);
 				if(md == null) {
 					System.out.println("CustomItem error: could not get material for item '"+key+"'");
 					continue;
 				}
 				if(preferredNames.containsKey(md) || names.containsKey(md)) {
 					System.out.println("CustomItem error: Duplicate key for item '"+key+"' ("+getMaterialName(md)+")");
 					continue;
 				}
 				String s = cs.getString("PreferredName", null);
 				if(s != null) {
 					addName(md, s, true);
 					quant++;
 				}
 				List<String> list =  cs.getStringList("Names");
 				if(list != null && list.size() > 0) {
 					for(String str: list) {
 						addName(md, str, false);
 						quant++;
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("CustomItems: Configuration file error: ");
 				e.printStackTrace();
 			}
 		}
 		System.out.println("CustomItems: added "+quant+" alternate names.");
 	}
 	
 	//Non-Static Methods
 	
 	public CustomItems(final Plugin plug) {
 		plugin = plug;
 		self = this;
 		loadConfig();
 		try {
 			getServer();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public void update() throws Exception {
 		loadConfig();
 		Exception e1 = null;
 		if(this.equals(getCommand(false))) for(Object o: others) {
 			try {
 				Method m = o.getClass().getDeclaredMethod("update");
 				if(m == null) continue;
 				m.invoke(o);
 			} catch (Exception e) {
 				e.printStackTrace();
 				e1 = e;
 			}
 		}
 		if(e1!=null) throw e1;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender,  Command command, String name, String[] args) {
 		if((args.length == 0 && (!(sender instanceof Player) || ((Player)sender).getItemInHand() == null))) {
 			sender.sendMessage("CustomItems version "+version+" help menu: ");
 			if(sender.hasPermission(config.getString("Command.AdminPermission")))
 				sender.sendMessage("IMPORTANT: <item> cannot contain spaces. <name>, however, can.");
 			sender.sendMessage("/"+name+" <item> <page> views alternate names for item.");
 			if(sender.hasPermission(config.getString("Command.AdminPermission"))) {
 				sender.sendMessage("/"+name+" add <item> <name> Adds the pseudonym name to item.");//DONE
 				sender.sendMessage("/"+name+" set <item> <name> Sets the preferred pseudonym name of item.");//DONE
 				sender.sendMessage("/"+name+" remove <item> <name> Removes the pseudonym name to item.");//DONE
 				sender.sendMessage("/"+name+" reload Reloads the config file.");//DONE
 			}
 			return true;
 		}
 		if(args.length == 0) {
 			Player p = (Player) sender;
 			ItemStack is = p.getItemInHand();
 			p.sendMessage("You are holding "+getItemName(is));
 		}
 		if(args[0].equalsIgnoreCase("reload") && sender.hasPermission(config.getString("Command.AdminPermission"))) {
 			try {
 				update();
 				sender.sendMessage("CustomItems: Reloaded " + (1 + others.size()) + " implementation(s)");
 			} catch (Exception e) {
 				sender.sendMessage("Failed to reload all CustomItems implementations. See server log for details");
 			}
 			return true;
 		} else if(args[0].equalsIgnoreCase("add") && sender.hasPermission(config.getString("Command.AdminPermission"))) {
 			if(args.length < 3) {
 				sender.sendMessage("Incorrect quantity of paramaters suplied.");
 				return true;
 			}
 			String matName = "";
 			for(int i = 2; i < args.length; i++) matName += args[i] + " ";
 			matName = matName.trim();
 			MaterialData md = getMaterial(args[1]);
 			if(md == null) {
 				sender.sendMessage("No item '"+args[1]+"' exists to add psuedonym.");
 				return true;
 			}
 			addName(md, matName, false);
 			sender.sendMessage("Added "+matName+" as a name for "+getMaterialName(md));
 			return true;
 		} else if(args[0].equalsIgnoreCase("set") && sender.hasPermission(config.getString("Command.AdminPermission"))) {
 			if(args.length < 3) {
 				sender.sendMessage("Incorrect quantity of paramaters suplied.");
 				return true;
 			}
 			String matName = "";
 			for(int i = 2; i < args.length; i++) matName += args[i] + " ";
 			matName = matName.trim();
 			MaterialData md = getMaterial(args[1]);
 			if(md == null) {
 				sender.sendMessage("No item '"+args[1]+"' exists to set psuedonym.");
 				return true;
 			}
 			String orig = getMaterialName(md);
 			addName(md, matName, true);
 			sender.sendMessage("Replaced "+orig+" with "+matName);
 			return true;
 		} else if(args[0].equalsIgnoreCase("remove") && sender.hasPermission(config.getString("Command.AdminPermission"))) {
 			if(args.length < 3) {
 				sender.sendMessage("Incorrect quantity of paramaters suplied.");
 				return true;
 			}
 			String matName = "";
 			for(int i = 2; i < args.length; i++) matName += args[i] + " ";
 			matName = matName.trim();
 			MaterialData md = getMaterial(args[1]);
 			if(md == null) {
 				sender.sendMessage("No item '"+args[1]+"' exists to remove psuedonym.");
 				return true;
 			}
 			removeName(md, matName);
 			sender.sendMessage("Removed "+matName+" as a name for "+getMaterialName(md));
 			return true;
 		}
 		return false;
 	}
 	
 	public class CustomItemsCommand extends Command {
 		CommandExecutor exec = self;
 		CustomItemsCommand() {
 			super(config.getString("Command.Name"));
 		}
 		public void setExecutor(CommandExecutor e) {
 			exec = e;
 		}
 		public CommandExecutor getExecutor() {
 			return exec;
 		}
 		@Override
 		public String getPermission() {
 			return config.getString("Command.BasePermission");
 		}
 		@Override
 		public String getPermissionMessage() {
 			return ChatColor.RED + "You lack the required permission";
 		}
 		@Override
 		public boolean execute(CommandSender sender, String name, String[] args) {
 			return exec.onCommand(sender, this, name, args);
 		}
 	}
 }
