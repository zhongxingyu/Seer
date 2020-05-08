 import java.util.*;
 
 public class ListWarpsCommand extends Command {
 
 	public static final String COMMAND = "/listwarps";
 	private WarpEx plugin;
 
 	public ListWarpsCommand(WarpEx plugin, String[] alias) {
 		super(COMMAND, alias, "<namespace>", "Show warps", COMMAND);
 		this.plugin = plugin;
 	}
 
 	public boolean call(Player player, String command, List<String> args) {
		String defns = plugin.normalizeKey(player, "foo").first;
		String ns = args.isEmpty() ? defns : args.get(0);
 		Set<String> set = new TreeSet<String>();
		for (Pair<String, String> w : plugin.getAllWarps(player)) {
			if (w.first.equalsIgnoreCase(ns))
				set.add(w.second);
 		}
 		if (set.isEmpty()) {
 			Chat.toPlayer(player, (Colors.Rose + "No warps avairable"));
 			return true;
 		}
 		StringBuilder sb = new StringBuilder();
 		for (String s : set) {
 			if (sb.length() > 0)
 				sb.append(" ");
 			sb.append(s);
 		}
 		String ns2 = ns + "\'s";
 		if (ns.equals(Namespace.Global.get(player)))
 			ns2 = "Global";
 		if (ns.equals(Namespace.Secret.get(player)))
 			ns2 = "Secret";
 		Chat
 			.toPlayer(player, (Colors.LightGreen + ns2)
 				+ (Colors.LightGray + " warps: ")
 				+ (Colors.White + sb.toString().trim()));
 		Chat.toPlayer(player, (Colors.LightGray + "Type ")
 			+ (Colors.LightPurple + WarpCommand.COMMAND + (ns.equals(defns)
 				? " [name]" : " " + ns + ":[name]")) + (Colors.LightGray + " to use"));
 		return true;
 	}
 
 }
