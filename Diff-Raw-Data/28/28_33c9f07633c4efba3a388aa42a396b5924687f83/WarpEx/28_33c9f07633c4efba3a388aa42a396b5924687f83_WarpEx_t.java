 import java.util.*;
 
 public class WarpEx extends PluginEx {
 
 	public static final String WARPS_FILE_KEY = "warp-file";
 	public static final String WARPS_FILE_DEFAULT = "data.txt";
 
 	public static final String DEFAULT_NAMESPACE_KEY = "default-namespace";
 	public static final String DEFAULT_NAMESPACE_DEFAULT = "Personal";
 
 	public static final String HIDDEN_PREFIX_KEY = "hidden-prefix";
 	public static final String HIDDEN_PREFIX_DEFAULT = "@";
 
 	public static final String WARP_ALIAS_KEY = "warp-alias";
 	public static final String WARP_ALIAS_DEFAULT = "/go";
 
 	public static final String SETWARP_ALIAS_KEY = "setwarp-alias";
 	public static final String SETWARP_ALIAS_DEFAULT = "/sw";
 
 	public static final String REMOVEWARP_ALIAS_KEY = "removewarp-alias";
 	public static final String REMOVEWARP_ALIAS_DEFAULT = "/rw";
 
 	public static final String LISTWARPS_ALIAS_KEY = "listwarps-alias";
 	public static final String LISTWARPS_ALIAS_DEFAULT = "/lw";
 
 	public static final String LISTNS_ALIAS_KEY = "listns-alias";
 	public static final String LISTNS_ALIAS_DEFAULT = "/ln";
 
 	private Map<String, Location> warps;
 	private Namespace defaultNamespace;
 	private String hiddenPrefix;
 	private String[] warpAlias, setwarpAlias, removewarpAlias, listwarpsAlias,
 			listnsAlias;
 	private Command warp, setwarp, removewarp, listwarps, listns;
 
 	public WarpEx() {
 		super("WarpEx");
 		this.warps = new TreeMap<String, Location>();
 		addHook(PluginLoader.Hook.COMMAND, PluginListener.Priority.LOW);
 	}
 
 	public Pair<String, String> normalizeKey(Player player, String key) {
 		String[] s = key.split(":", 2);
 		String ns = s.length > 1 ? s[0] : defaultNamespace.get(player);
 		String warp = s.length > 1 ? s[1] : s[0];
		if (!warp.matches("[^ ,:]+")
 			|| ((ns.equalsIgnoreCase(Namespace.Global.get(player)) || ns
 				.equalsIgnoreCase(Namespace.Secret.get(player))) && (warp
				.startsWith(hiddenPrefix)))) {
			Chat.toPlayer(player, "normalizeKey: ns = %s, warp = %s", ns, warp);
 			return null;
		}
 		return new Pair<String, String>(ns, warp);
 	}
 
 	public Set<Pair<String, String>> getAllWarps(Player player) {
 		Set<Pair<String, String>> set = new TreeSet<Pair<String, String>>(
 			new Comparator<Pair<String, String>>() {
 				public int compare(Pair<String, String> o1, Pair<String, String> o2) {
 					int c1 = o1.first.compareTo(o2.first);
 					return c1 != 0 ? c1 : o1.second.compareTo(o2.second);
 				}
 			});
 		for (Map.Entry<String, Location> entry : warps.entrySet()) {
 			String key = entry.getKey();
 			String[] s = key.split(":", 2);
 			if (checkPermission(false, player, s[0], s[1])) {
 				set.add(Pair.create(s[0], s[1]));
 			}
 		}
 		return set;
 	}
 
 	public Location getWarp(Player player, String key) {
 		Pair<String, String> p = normalizeKey(player, key);
 		if (p == null)
 			return null;
 		key = p.first + ":" + p.second;
 		return checkPermission(false, player, p.first, p.second)
 			&& warps.containsKey(key) ? warps.get(key) : null;
 	}
 
 	public void setWarp(Player player, String key) {
 		warps.put(key, player.getLocation());
 		saveWarps();
 	}
 
 	public void removeWarp(Player player, String key) {
 		warps.remove(key);
 		saveWarps();
 	}
 
 	public boolean checkPermission(boolean modify, Player player, String ns,
 		String warp) {
 		String personal = Namespace.Personal.get(player);
 		String global = Namespace.Global.get(player);
 		String secret = Namespace.Secret.get(player);
 		return (ns.equalsIgnoreCase(personal))
 			|| (ns.equalsIgnoreCase(global) && (player
 				.canUseCommand("/warpex-modify-" + global) || (!modify && player
 				.canUseCommand("/warpex-" + global))))
 			|| (ns.equalsIgnoreCase(secret) && (player
 				.canUseCommand("/warpex-modify-" + secret) || (!modify && player
 				.canUseCommand("/warpex-" + secret))))
 			|| (!warp.startsWith(hiddenPrefix) && !modify
 				&& !ns.equalsIgnoreCase(global) && !ns.equalsIgnoreCase(secret));
 	}
 
 	protected void onEnable() {
 		defaultNamespace = Enum.valueOf(Namespace.class,
 			Tools.Capitalize(getProperty(DEFAULT_NAMESPACE_KEY,
 				DEFAULT_NAMESPACE_DEFAULT)));
 		hiddenPrefix = getProperty(HIDDEN_PREFIX_KEY, HIDDEN_PREFIX_DEFAULT);
 		warpAlias = Tools.split(getProperty(WARP_ALIAS_KEY, WARP_ALIAS_DEFAULT),
 			",");
 		setwarpAlias = Tools.split(
 			getProperty(SETWARP_ALIAS_KEY, SETWARP_ALIAS_DEFAULT), ",");
 		removewarpAlias = Tools.split(
 			getProperty(REMOVEWARP_ALIAS_KEY, REMOVEWARP_ALIAS_DEFAULT), ",");
 		listwarpsAlias = Tools.split(
 			getProperty(LISTWARPS_ALIAS_KEY, LISTWARPS_ALIAS_DEFAULT), ",");
 		listnsAlias = Tools.split(
 			getProperty(LISTNS_ALIAS_KEY, LISTNS_ALIAS_DEFAULT), ",");
 
 		addCommand(warp = new WarpCommand(this, warpAlias),
 			setwarp = new SetWarpCommand(this, setwarpAlias),
 			removewarp = new RemoveWarpCommand(this, removewarpAlias),
 			listwarps = new ListWarpsCommand(this, listwarpsAlias),
 			listns = new ListNsCommand(this, listnsAlias));
 
 		loadWarps();
 	}
 
 	protected void onDisable() {
 		removeCommand(warp, setwarp, removewarp, listwarps, listns);
 	}
 
 	private void loadWarps() {
 		try {
 			String fileName = getProperty(WARPS_FILE_KEY, WARPS_FILE_DEFAULT);
 			warps = loadMap(fileName,
 				new Converter<String, Pair<String, Location>>() {
 					public Pair<String, Location> convertTo(String value) {
 						String[] s = value.split(",");
 						if (s.length < 4 || s[0].isEmpty())
 							return null;
 						Location location = new Location(Double.valueOf(s[1]), Double
 							.valueOf(s[2]), Double.valueOf(s[3]), s.length >= 5 ? Float
 							.valueOf(s[4]) : 0.0f, s.length >= 6 ? Float.valueOf(s[5]) : 0.0f);
 						return new Pair<String, Location>(s[0], location);
 					}
 				});
 		} catch (Exception e) {
 			saveWarps();
 		}
 	}
 
 	private void saveWarps() {
 		try {
 			String fileName = getProperty(WARPS_FILE_KEY, WARPS_FILE_DEFAULT);
 			saveMap(warps, fileName, new Converter<Pair<String, Location>, String>() {
 				public String convertTo(Pair<String, Location> value) {
 					return String.format("%s,%f,%f,%f,%f,%f", value.first,
 						value.second.x, value.second.y, value.second.z, value.second.rotX,
 						value.second.rotY);
 				}
 			});
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 }
