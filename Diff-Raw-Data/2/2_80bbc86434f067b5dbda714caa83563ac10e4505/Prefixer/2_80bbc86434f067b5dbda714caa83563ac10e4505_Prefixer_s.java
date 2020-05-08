 package net.gamesketch.bukkit.gsgeneral.PREFIX;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.gamesketch.bukkit.gsgeneral.Core;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class Prefixer {
 	public static List<PlayerPrefix> prefixes;
 	
 	static File file = new File("plugins/GSGeneral/prefixer.prefix");
 	static File folder = new File("plugins/GSGeneral/");
 	
 	public static void Load() {
 		
 		if (!file.exists()) {
 			try {
 				folder.mkdirs();
 				file.createNewFile();
 			} catch (IOException e) { return; }
 			return;
 		}
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(file));
 			String s;
 			while ((s = in.readLine()) != null) {
				if (!s.matches("[a-zA-Z0-9_]+:[a-zA-Z0-9]+:[0-9]+")) {
 					System.out.println("[GSGeneral-prefix] Error parsing the string " + s);
 					continue;
 				}
 				String prefix = s.split(":")[1];
 				int color = Integer.parseInt(s.split(":")[2]);
 				String p = s.split(":")[0].toLowerCase();
 				if (getPrefixByName(p) != null) { continue; } //to prevent old dupes
 				prefixes.add(new PlayerPrefix(p,prefix,color));
 				continue;
 			}
 		} catch (IOException e) { }
 	}
 	public static void init() { 
 		prefixes = new LinkedList<PlayerPrefix>(); 
 		Load();
 		for (Player p : Core.server.getOnlinePlayers()) {
 			if (getPrefix(p) == null) { prefixes.add(new PlayerPrefix(p.getName())); }
 		}
 	}
 	public static void Save() {
 		if (!file.exists()) {
 			try {
 				folder.mkdirs();
 				file.createNewFile();
 			} catch (IOException e) { return; }
 		}
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(file));
 			for (PlayerPrefix p : prefixes) {
 				out.write(p.getPlayerName() + ":" + p.getRawPrefix() + ":" + p.getColor());
 				out.newLine();
 			}
 			out.close();
 		} catch (IOException e) { return; }
 	}
 	public static void chatEvent(PlayerChatEvent event) {
     	if (event.isCancelled()) { return; }
     	
     	String prefix = getPrefix(event.getPlayer()).getPrefix();
     	if (prefix == null || prefix.length() < 1) { return; }
     	event.setFormat(prefix + ' ' + event.getFormat());
 	}
 	
 	
 	
 	
 	
 	
 	public static PlayerPrefix getPrefix(Player p) {
 		for (PlayerPrefix prefix : prefixes) {
 			if (prefix.getPlayerName().equalsIgnoreCase(p.getName())) { return prefix; }
 		}
 		return null;
 	}
 	public static PlayerPrefix getPrefixByName(String name) {
 		for (PlayerPrefix prefix : prefixes) {
 			if (name.equalsIgnoreCase(prefix.getPlayerName())) { return prefix; }
 		}
 		return null;
 	}
 	public static int toInt(String s) {
 		s = s.toLowerCase();
 		if (s.equals("black")) { return 0; } 
 		if (s.equals("darkblue")) { return 1; } 
 		if (s.equals("darkgreen")) { return  2; } 
 		if (s.equals("darkaqua")) { return  3; } 
 		if (s.equals("darkred")) { return  4; } 
 		if (s.equals("darkpurple")) { return  5; } 
 		if (s.equals("gold")) { return  6; } 
 		if (s.equals("gray")) { return  7; } 
 		if (s.equals("darkgray")) { return  8; } 
 		if (s.equals("blue")) { return  9; } 
 		if (s.equals("green")) { return  10; } 
 		if (s.equals("aqua")) { return  11; } 
 		if (s.equals("red")) { return  12; } 
 		if (s.equals("pink")) { return  13; }
 		if (s.equals("yellow")) { return  14; }
 		if (s.equals("white")) { return  15; }
 		return 15;
 		
 	}
 	
 }
