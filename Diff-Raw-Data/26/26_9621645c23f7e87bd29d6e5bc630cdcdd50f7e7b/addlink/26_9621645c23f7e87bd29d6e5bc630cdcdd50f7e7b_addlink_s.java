 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCustom.commands;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.dariusmewes.TimoliaCustom.TimoliaCustom;
 
 public class addlink extends TCommand {
 
 	public static Map<Player, String> active = new HashMap<Player, String>();
	public static String coreURL = instance.getConfig().getString("linkURL");
	public static String shorterCore = coreURL.substring(7);
 
 	public addlink(String name) {
 		super(name);
 		setIngame();
 		setMinArgs(1);
 		setMaxArgs(1);
 		setUsage("/addlink <Kuerzel>");
 		setDesc("Fge link-Krzungen hinzu!");
 	}
 
 	public void perform(CommandSender sender, String[] args) {
 		active.put((Player) sender, args[0]);
 		sender.sendMessage(prefix + "Gib nun einfach den Link ein!");
 	}
 
 	public static String writeToDB(Player p, String enteredUrl, String hashWish) throws Exception {
 		URL url = new URL(coreURL + "add.php");
 		URLConnection con = url.openConnection();
 		con.setDoOutput(true);
 		OutputStream out = con.getOutputStream();
 		String data = "user=" + p.getName() + "&url=" + URLEncoder.encode(enteredUrl, "UTF-8");
 		if (hashWish != null) {
 			data += "&hash=" + URLEncoder.encode(hashWish, "UTF-8");
 		}
 
 		out.write(data.getBytes());
 		out.flush();
 		out.close();
 
 		InputStream in = con.getInputStream();
 		InputStreamReader inR = new InputStreamReader(in);
 		BufferedReader reader = new BufferedReader(inR);
 		String line = reader.readLine();
 		in.close();
 		inR.close();
 		reader.close();
 
 		String hash = null;
 		if (line == null) {
 			p.sendMessage(prefix + "Link konnte nicht gekrzt werden. Fehler: Kein web-service");
 			TimoliaCustom.logError("Web-Service unerreichbar");
 			return null;
 		} else {
 			if (line.startsWith("1") || line.startsWith("2")) {
 				hash = line.split(" ")[1];
 			} else if (line.startsWith("3")) {
 				p.sendMessage(prefix + "Das Kuerzel " + hashWish + " ist bereits verwendet!");
 			} else if (line.startsWith("4")) {
 				p.sendMessage(prefix + "Der Link " + shorterCore + "?i=" + hashWish + " wurde hinzugefgt!");
 			} else {
 				p.sendMessage(prefix + "Link konnte nicht gekrzt werden.");
 				TimoliaCustom.logError("LINK-SHORTENING-ERROR: LAST LINE: " + line);
 				return null;
 			}
 		}
 
 		return hash;
 	}
 
 }
