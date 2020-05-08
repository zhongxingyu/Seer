 package Factoids;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.PluggableBot.plugin.DefaultPlugin;
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 
 public class Factoids extends DefaultPlugin {
 
 	private static final Logger log = Logger.getLogger(Factoids.class.toString());
 	private final ObjectContainer database;
 
 	private static final String COMMAND_ADD = "!addfact";
 	private static final String COMMAND_SET = "!setfact";
 
 	public Factoids() {
		database = Db4o.openFile("Tell.db4o");
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 
 		String[] messageSplit = message.split(" ");
 		if (message.startsWith("?") && message.split(" ").length == 1) {
 			Fact proto = new Fact(message.toLowerCase().substring(1), null,
 					null, null, true);
 			ObjectSet<Fact> facts = database.get(proto);
 			bot.Message(channel, sender + ": " + facts.get(0));
 		} else if (messageSplit.length >= 3
 				&& messageSplit[0].equals(COMMAND_ADD)) {
 			String factString = messageSplit[1];
 			String factMessage = messageSplit[2];
 			String tmpMessage = message.substring(messageSplit[0].length()
 					+ messageSplit[1].length() + 1);
 
 			if (tmpMessage.trim().startsWith("is ")) {
 				factMessage += " " + tmpMessage.trim();
 			} else {
 				factMessage += " is " + tmpMessage.trim();
 			}
 			String factSetBy = sender;
 			Date factDate = new Date();
 			Fact fact = new Fact(factString, factMessage, factSetBy, factDate,
 					true);
 
 			Fact proto = new Fact(factString, null, null, null, true);
 			ObjectSet<Fact> set = database.get(proto);
 			for (Fact old : set) {
 				old.setOld();
 			}
 
 			database.set(fact);
 			database.commit();
 			bot.sendMessage(channel, sender + ": Stored, thanks.");
 
 		} else if (messageSplit.length >= 3
 				&& messageSplit[0].equals(COMMAND_SET)) {
 			String factString = messageSplit[1];
 			String factMessage = messageSplit[2];
 			String tmpMessage = message.substring(messageSplit[0].length()
 					+ messageSplit[1].length() + 1);
 
 			if (tmpMessage.trim().startsWith("is ")) {
 				factMessage += " " + tmpMessage.trim();
 			} else {
 				factMessage += " is " + tmpMessage.trim();
 			}
 			String factSetBy = sender;
 			Date factDate = new Date();
 			Fact fact = new Fact(factString, factMessage, factSetBy, factDate,
 					true);
 
 			Fact proto = new Fact(factString, null, null, null, true);
 			ObjectSet<Fact> set = database.get(proto);
 			for (Fact old : set) {
 				old.setOld();
 			}
 
 			database.set(fact);
 			database.commit();
 			bot.sendMessage(channel, sender + ": Stored, thanks.");
 		}
 
 	}
 
 	@Override
 	public String getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void unload() {
 		database.close();
 	}
 	
 }
