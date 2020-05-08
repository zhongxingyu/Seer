 package Tell;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 import com.PluggableBot.plugin.DefaultPlugin;
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 
 public class Tell extends DefaultPlugin {
 
 	private static final String command = "tell";
 	Logger log = Logger.getLogger(this.getClass().toString());
 	ObjectContainer database;
 
 	public Tell() {
 		log.info("Tell: loading database");
 		database = Db4o.openFile("Tell.db4o");
 		Db4o.configure().objectClass(Message.class).cascadeOnUpdate(true);
 		log.info("Tell: database open");
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 
 		if (message.toLowerCase().startsWith("!" + command)) {
 
 			message = message.substring(("!" + command).length() + 1);
 			log.info("Tell:Saving new message " + message);
 			String[] split = message.split(" ");
			if (split.length > 2) {
 				String target = split[0];
 				message = message.substring(split[0].length() + 1);
 				Message m = new Message(sender, new Date(), target, message,
 						channel);
 				database.set(m);
 				database.commit();
 				bot.sendMessage(channel, sender + ": Thanks, I will tell "
 						+ target + " that.");
 			}
 		}
 
 		Message proto = new Message(null, null, sender, null, channel);
 
 		ObjectSet<Message> set = database.get(proto);
 		for (Message m : set) {
 			bot.sendMessage(channel, m.target + ": " + m.message + " [sent: "
 					+ m.date.toLocaleString() + ", from: " + m.sender + "]");
 			database.delete(m);
 		}
 		database.commit();
 	}
 
 	@Override
 	public void onNickChange(String oldNick, String login, String hostname,
 			String newNick) {
 		Message proto = new Message(null, null, oldNick, null, null);
 		ObjectSet<Message> set = database.get(proto);
 		for (Message m : set) {
 			log.info("Tell: updating message" + m.message);
 			m.target = newNick;
 		}
 	}
 
 	@Override
 	public void onJoin(String channel, String sender, String login,
 			String hostname) {
 		Message proto = new Message(null, null, sender, null, null);
 		ObjectSet<Message> set = database.get(proto);
 		if (!set.isEmpty()) {
 			bot.Message(channel, sender, "You have " + set.size() + " message"
 					+ (set.size() > 1 ? "s" : "") + "waiting.");
 		}
 	}
 
 	@Override
 	public String getHelp() {
 		return "";
 	}
 
 }
