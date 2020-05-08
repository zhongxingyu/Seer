 package net.esper.tacos.ircbot;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.UtilSSLSocketFactory;
 import org.pircbotx.cap.SASLCapHandler;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.InviteEvent;
 import org.pircbotx.hooks.events.KickEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 import org.yaml.snakeyaml.Yaml;
 
 @SuppressWarnings("rawtypes")
 public class TacoBot extends ListenerAdapter implements Listener {
 
 	public static String CHAN = "#tacos";
 	public static String NICK = "TacoBot";
 	public static Channel CHAN_OBJ;
 	public static final String PREFIX = ".";
 	public static final PircBotX bot = new PircBotX();
 	static volatile boolean keepRunning = true;
 	public static List<String> blacklist = Collections
 			.synchronizedList(new ArrayList<String>());
 	public static List<String> trollmessages = Collections
 			.synchronizedList(new ArrayList<String>());
 
 
 	public static void main(String[] args) throws Exception {
 		// Setup
 		CommandProcessor.init();
 		// bot.setVerbose(true);
 		bot.setVersion("TacoBot v1.2 - For #tacos only!");
 		bot.getListenerManager().addListener(new TacoBot());
 
 		// Now connect the magic
 		try {
 			InputStream input = new FileInputStream(new File("config.yml"));
 			Yaml yaml = new Yaml();
 			Map config = (Map) yaml.load(input);
 			boolean ssl = false;
 			boolean invite = false;
 			NICK = (String) config.get("name");
 			System.out.println("Name: " + NICK);
 			bot.setName(NICK);
 			String server = (String) config.get("server");
 			System.out.println("Server: " + server);
 			int port = Integer.parseInt((String) config.get("port").toString());
 			System.out.println("Port: " + port);
 			String password = (String) config.get("password");
 			if (password == null) {
 				password = "";
 			}
 			System.out.println("Password: " + password);
 			String nickserv = (String) config.get("nickserv");
 			System.out.println("Nickserv: " + nickserv);
 			String channel = (String) config.get("channel");
 			System.out.println("Channel: " + channel);
 			ssl = (Boolean) config.get("ssl");
 			System.out.println("SSL: " + ssl);
 			invite = (Boolean) config.get("invite");
 			System.out.println("Invite: " + invite);
 			blacklist = (List<String>) config.get("blacklist");
 			System.out.println("Blacklist:");
 			for (String u : blacklist) {
 				System.out.println(u);
 			}
 			trollmessages = (List<String>) config.get("trollmessages");
 			System.out.println("Troll Messages:");
 			for (String u : trollmessages) {
 				System.out.println(u);
 			}
 
 			if (ssl) {
 				// TODO: Figure out Java Keystore to only trust bouncer
 				// certificate.
 				bot.connect(server, port, password,
 						new UtilSSLSocketFactory().trustAllCertificates());
 			} else {
 				bot.connect(server, port, password);
 			}
 			bot.setName(NICK);
 			if (nickserv != null) {
 				bot.identify(nickserv);
 			}
 			CHAN = channel;
 			CHAN_OBJ = bot.getChannel(channel);
 			input.close();
 			if (invite) {
 				Thread.sleep(1000);
 				bot.sendRawLine("/msg chanserv invite " + channel);
 				bot.sendInvite(bot.getNick(), channel);
 				System.out.println("attempting invite");
 				Thread.sleep(1000);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			bot.joinChannel(CHAN);
 			System.out.println("Connected to " + CHAN + "!");
 			bot.sendMessage(CHAN, "420 blaze it faggots");
 			final Thread mainThread = Thread.currentThread();
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				public void run() {
 					keepRunning = false;
 					bot.disconnect();
 					System.out.println("Shutting Down");
					System.exit(0);
 				}
 			});
 		}
 	}
 
 	private static List<String> msgs = new CopyOnWriteArrayList<String>();
 
 	public static void addMsg(String msg) {
 		msgs.add(msg);
 		if (msgs.size() == 6) {
 			msgs.remove(0);
 		}
 	}
 
 	public static String[] getMsgs() {
 		return msgs.toArray(new String[0]);
 	}
 
 	public static void sendMessage(User user, String message) {
 		sendMessage(user.getNick(), message);
 	}
 
 	public static void sendMessage(String user, String message) {
 		sendMessage("(" + user + ") " + message);
 	}
 
 	public static String[] getOps() {
 		String users = "";
 		for (Channel c : bot.getChannels()) {
 			for (User u : c.getOps()) {
 				users = users + (u.getNick() + ",");
 			}
 		}
 		return users.substring(0, users.length() - 1).split(",");
 	}
 
 	public static void sendMessage(String message) {
 		bot.sendMessage(CHAN, message);
 	}
 
 	@Override
 	public void onMessage(MessageEvent event) throws Exception {
 		if (!event.getChannel().getName().equals(CHAN)) {
 			return;
 		}
 		addMsg("<" + event.getUser().getNick() + "> " + event.getMessage());
 		CommandProcessor.process(event);
 	}
 
 	@Override
 	public void onInvite(InviteEvent event) throws Exception {
 		if (!event.getChannel().equals(CHAN)) {
 			bot.joinChannel(CHAN);
 			System.out.println("Connected to " + CHAN + "!");
 		}
 	}
 
 	@Override
 	public void onPrivateMessage(PrivateMessageEvent event) {
 		if (!event.getUser().getChannelsOpIn().contains(CHAN_OBJ)) {
 			event.respond("Sorry, but this bot does not support private messaging capabilities.");
 			return;
 		}
 		System.out.println(event.getMessage());
 		CommandProcessor.process(event);
 	}
 
 	public void onChannelKick(KickEvent event) {
 		if (event.getRecipient().getNick().toLowerCase()
 				.equals(NICK.toLowerCase())) {
 			bot.joinChannel(CHAN);
 		}
 	}
 }
