 /*
  * KillPlugin.java
  *
  * Created on 10 October 2007, 13:18
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 import java.util.List;
 import java.util.Random;
 
 import Kill.*;
 
 import AndrewCassidy.PluggableBot.PluggableBot;
 import AndrewCassidy.PluggableBot.Plugin;
 
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 
 /**
  * 
  * @author AndyC
  */
 public class Kill implements Plugin {
 
 	private ObjectContainer database;
 	private String def = "stabs %NAME with a elongated frozen eel";
 	private Random rng = new Random();
 
 	/** Creates a new instance of KillPlugin */
 	public Kill() {
 		load();
 	}
 
 	public void onAction(String sender, String login, String hostname,
 			String target, String action) {
 	}
 
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 	}
 
 	public void onKick(String channel, String kickerNick, String kickerLogin,
 			String kickerHostname, String recipientNick, String reason) {
 	}
 
 	private KillLists getKillList(String sender) {
 		KillLists proto = new KillLists(sender);
 		ObjectSet<KillLists> killsContainer = database.get(proto);
 		if (killsContainer.size() == 1) {
 			return killsContainer.get(0);
 		} else {
 			return proto;
 		}
 	}
 
 	private void kill(String sender, String message, String channel) {
 		String killString = def;
 		List<String> listOfUserKills = getKillList(sender).getKills();
 		if (listOfUserKills.size() > 0) {
			String killList = listOfUserKills.get(rng
 					.nextInt(listOfUserKills.size()));
 		}
 		String target = message.substring(6).trim();
 		if (target.toLowerCase().equals(PluggableBot.Nick().toLowerCase()))
 			target = sender;
 		PluggableBot
 				.Action(channel, killString.replaceAll("%NAME", target));
 	}
 	
 	private void addKill(String sender, String message) {
 		KillLists killer = getKillList(sender);
 		List<String> listOfUserKills = killer.getKills();
 		if (!listOfUserKills.contains(message.substring(9))) {
 			listOfUserKills.add(message.substring(9));
 			database.set(killer);
 			database.commit();
 			PluggableBot.Message(sender, "Added kill: "
 					+ message.substring(9));
 		} else {
 			PluggableBot.Message(sender, "Kill '" + message.substring(9)
 					+ "' already exosists");
 		}
 	}
 	
 	private void listKills(String sender) {
 		KillLists killer = getKillList(sender);
 			List<String> listOfUserKills = killer.getKills();
 			PluggableBot.Message(sender, "You kills are :");
 			for (int i = 0; i < listOfUserKills.size(); ++i) {
 				PluggableBot.Message(sender, i + ": "
 						+ listOfUserKills.get(i));
 			}
 	}
 	
 	private void removeKill(String sender, String message) {
 		try {
 			int remove = Integer.parseInt(message.substring(11).trim());
 			KillLists proto = new KillLists(sender);
 			ObjectSet<KillLists> killsContainer = database.get(proto);
 			if (killsContainer.size() == 1) {
 				KillLists killer = killsContainer.get(0);
 				List<String> listOfUserKills = killer.getKills();
 				if (listOfUserKills.size() > remove) {
 					String removed = listOfUserKills.remove(remove);
 					PluggableBot.Message(sender, "kill '" + removed
 							+ "' was removed");
 					database.set(killer);
 					database.commit();
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 	}
 	
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.startsWith("!kill")) {
 			kill(sender, message, channel);
 		} else if (message.startsWith("!addkill")) {
 			addKill(sender, message);
 		} else if (message.startsWith("!listkills")) {
 			listKills(sender);
 		} else if (message.startsWith("!removekill")) {
 			removeKill(sender, message);
 		}
 	}
 
 	public void onPart(String channel, String sender, String login,
 			String hostname) {
 	}
 
 	public void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 	}
 
 	public String getHelp() {
 		return "This allows users to order me to kill other users, by using !kill <username>. To customise your kill message, use !addkill, followed by the attack. use %NAME as a placeholder for a user's nick. !listkills, !removekill <number>";
 	}
 
 	private void load() {
 		database = Db4o.openFile("Kill.db4o");
 	}
 
 	public void onPrivateMessage(String sender, String login, String hostname,
 			String message) {
 		if (message.startsWith("!addkill")) {
 			addKill(sender, message);
 		} else if (message.startsWith("!listkills")) {
 			listKills(sender);
 		} else if (message.startsWith("!removekill")) {
 			removeKill(sender, message);
 		}
 	}
 
 	public void unload() {
 		database.close();
 	}
 }
