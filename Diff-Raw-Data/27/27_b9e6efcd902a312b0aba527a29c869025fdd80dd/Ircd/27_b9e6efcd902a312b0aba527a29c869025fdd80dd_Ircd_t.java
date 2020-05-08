 package de.johannes13.minecraft.bukkit.chat.inspircd;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.util.config.ConfigurationNode;
 
 import de.johannes13.minecraft.bukkit.chat.inspircd.utils.UidGenerator;
 import de.johannes13.minecraft.bukkit.chat.plugin.ChannelMetadata;
 import de.johannes13.minecraft.bukkit.chat.plugin.ChatPlugin;
 import de.johannes13.minecraft.bukkit.chat.plugin.PlayerMetadata;
 
 public class Ircd extends Thread {
 
 	public static enum State {
 		DISCONNECTED, BURSTNOUID, BURST, CONNECTED
 	}
 
 	final ConfigurationNode config;
 	final String sid;
 	final UidGenerator uidgen;
 	final ChatPlugin plugin;
 	String rid;
 	// 0 = not connected, 1 = burst, 2 = connected
 	State state = State.DISCONNECTED;
         Boolean printdbg = false;
 	// key: uid, value: nick
 	final Hashtable<String, String> uidmap = new Hashtable<String, String>();
 	final Hashtable<String, ChannelMetadata> chmap = new Hashtable<String, ChannelMetadata>();
 	final Hashtable<String, String> nick2uid = new Hashtable<String, String>();
 	final Hashtable<String, IrcUser> uid2meta = new Hashtable<String, IrcUser>();
 	final Hashtable<String, PlayerMetadata> uid2player = new Hashtable<String, PlayerMetadata>();
 
 	Socket socket;
 	private boolean stopped;
 
 	BufferedWriter w;
 	private final ArrayList<Player> pending = new ArrayList<Player>();
 
 	public Ircd(ConfigurationNode config, ChatPlugin plugin) {
 		super("ChatPlugin.IrcD");
 		this.config = config;
 		sid = config.getString("serverid");
 		uidgen = new UidGenerator();
 		this.plugin = plugin;
 	}
 
         @Override
 	public void run() {
                 // String for later use.
                 String line;
 		// cache the sid value, it is used very often. (see java bytecode for
 		// reason)
 		String sid = this.sid;
 		String pre = ":" + sid;
 		while (!stopped)
 			try {
 				Socket s = new Socket("localhost", 7002);
 				socket = s;
                                 if (!s.isConnected()) {
                                     throw new IOException("Connection Failed");
                                 }
 				w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
 				BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
                                 // We skip capab, InspIRCd feels fine.
                                 line = r.readLine();
                                 this.println("SERVER " + config.getString("serverhost") + " " + config.getString("password") + " 0 " + sid + " :" + config.getString("servername") + "\r\n");
                                 state = State.BURSTNOUID;
                                 // Wait for CAPAB END.
                                 while (!(line = r.readLine()).equals("CAPAB END")) {
                                 }
 				this.println(pre + " BURST " + getTS() + "\r\n");
 				this.println(pre + " VERSION : Chat Plugin for Bukkit by Johannes13\r\n"); // DO NOT CHANGE!!!
 				// Intoduce all users
 				boolean ips = config.getBoolean("propagate-ip", true);
 				String defhost = config.getString("userhost", "<host>");
 				String ident = config.getString("userident", "<nick>");
 				Player[] players;
 				synchronized (pending) {
 					players = plugin.getServer().getOnlinePlayers();
 					state = State.BURST;
 				}
 				for (Player p : players) {
 					String uid = sid + uidgen.generateUID();
 					PlayerMetadata meta = plugin.getMetadata(p);
 					meta.setUid(uid);
 					uid2player.put(uid, meta);
 					String ip, hostname, realhost;
 					if (ips) {
 						InetAddress addr = p.getAddress().getAddress();
 						ip = addr.getHostAddress();
 						realhost = addr.getCanonicalHostName();
 						if (defhost.contains("<host>"))
 							hostname = defhost.replaceAll("<host>", realhost);
 						else
 							hostname = defhost;
 					} else {
 						ip = "127.0.0.1";
 						realhost = "localhost";
 						if (defhost.contains("<host>"))
 							hostname = "minecraft";
 						else
 							hostname = defhost;
 					}
 					String id = ident;
 					if (ident.contains("<nick>"))
 						id = ident.replaceAll("<nick>", p.getName());
 					this.println(pre + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + " +" + (p.isOp() ? "or" : "r") + " :Minecraft Player");
 					if (p.isOp())
 						this.println(":" + uid + " OPERTYPE Ops\r\n");
 					// log the user in to services (might not work, but might
 					// work)
 					this.println(":" + sid + " METADATA " + uid + " accountname " + p.getName());
 					this.println(":" + sid + " METADATA " + uid + " swhois :is playing Minecraft");
 				}
 				for (ChannelMetadata ch : plugin.getChannels()) {
 					String rel = ch.getIrcRelay();
 					if (rel == null || rel.length() == 0 || rel.charAt(0) != '#')
 						continue;
 					chmap.put(ch.getIrcRelay(), ch);
 				}
 				this.println(pre + " ENDBURST");
 				line = r.readLine();
                                 if (this.printdbg)
                                         System.out.println("IRC COMMAND RECEIVED => " + line + " <=");
 				assert (line.contains("SERVER"));
 				if (line.contains("SERVER")) {
                                     rid = split(line)[5];
                             } else {
                                     rid = "00A";
                             }
 				line = r.readLine();
 				assert (line.contains("BURST"));
 				line = r.readLine();
 				assert (line.contains("VERSION"));
 				// ok, we have now to read the remote users and channels
 				while (!stopped) {
 					parseCommands(r.readLine());
 				}
 			} catch (IOException e) {
 				if (!stopped)
 					plugin.getServer().getLogger().log(Level.SEVERE, "[IRCD] Exception in the IrcD thread", e);
 			}
 	}
 
 	private void parseCommands(String line) throws IOException {
                 if (this.printdbg)
                         System.out.println("IRC COMMAND RECEIVED => " + line + " <=");
 		line = line.trim();
 		if (line.equals(""))
 			return;
 		String pre = ":" + sid;
 		String[] data = split(line);
 		try {
 			boolean force = false;
 			IrcCommands cmd = IrcCommands.valueOf(data[1]);
 			switch (cmd) {
                         case AWAY:
                                 // For now announce we are away.
                                 // TODO: Allow setting away in game.
                                 plugin.ircAway(uid2meta.get(data[0]));
                                 break;
 			case PING:
 				this.println(pre + " PONG " + sid + " " + data[0] + "\r\n");
 				break;
 			case ENDBURST:
 				state = State.CONNECTED;
 				if (data[0].equals(rid))
 					joinchannels();
 				break;
 			case FJOIN:
 				ChannelMetadata cm = chmap.get(data[2]);
 				if (cm == null)
 					break;
 				cm.setTs(data[3]);
 				String[] usr = data[5].split(" ");
 				for (String uid : usr) {
 					uid = uid.split(",", 2)[1];
 					IrcUser u = uid2meta.get(uid);
 					plugin.ircJoin(u, cm, state == State.CONNECTED);
 				}
 				break;
 			case IDLE:
 				// Always respond with 0 0? would be a good idea :)
 				// maybe change it later to the real signon time... TODO
 				this.println(":" + data[2] + " IDLE " + data[0] + "0 0\r\n");
 				break;
 			case NICK:
 				IrcUser u = uid2meta.get(data[0]);
 				String oldnick = u.nick;
 				u.nick = data[2];
 				plugin.sendIrcNickchange(u, oldnick);
 				break;
 			case SQUIT:
 				String rsid = data[2];
 				List<String> uids = new ArrayList<String>();
 				uids.addAll(uid2meta.keySet()); // We need a copy of the keys
 				// because the they will be
 				// modified
 				for (String uid : uids) {
 					if (!uid.startsWith(rsid))
 						continue;
 					plugin.playerQuit(uid2meta.get(uid));
 				}
 				break;
 			case SAJOIN:
 				force = true;
 			case SVSJOIN:
 				// forced join.
 				plugin.forceJoin(uid2player.get(data[2]), chmap.get(data[3]), force);
 				break;
 			case SVSPART:
 			case SAPART:
 				plugin.forcePart(uid2player.get(data[2]), chmap.get(data[3]));
 				break;
 			case TIME:
 				this.println(line + " " + getTS() + "\r\n");
 				break;
 			case UID:
 				IrcUser iu = new IrcUser(this);
 				iu.uid = data[2];
 				iu.nick = data[4];
 				uid2meta.put(data[2], iu);
 				break;
 			case KICK:
                                 if (this.printdbg)
                                         System.out.println("IRC KICK UID: " + data[3]);
 				plugin.forcePart(uid2player.get(data[3]), chmap.get(data[3]));
 				break;
 			case PRIVMSG:
 				if (data[3].startsWith("\u0001")) {
 					// CTCP - we only support ACTION
 					String ctcpdata = data[3].substring(1);
 					String[] split = ctcpdata.split(" ", 2);
 					String ctcpcmd = split[0];
 					if (ctcpcmd.equalsIgnoreCase("ACTION")) {
 						if (split.length > 1) {
 							String msg = split[1];
 							if (data[2].startsWith("#")) {
 								// do something stupid, relay it
 								ChannelMetadata ch = chmap.get(data[2]);
 								if (ch != null)
 									ch.sendAction(uid2meta.get(data[0]).nick, msg);
 							} else {
 								PlayerMetadata pm = uid2player.get(data[2]);
 								if (pm != null)
 									plugin.sendIrcAction(uid2meta.get(data[0]).nick, pm, msg);
 								// ignore it :/
 							}
 						}
 					}
 				} else if (data[2].startsWith("#")) {
                                     ChannelMetadata ch = chmap.get(data[2]);
                                     if (ch != null)
                                             ch.sendMessage(uid2meta.get(data[0]).nick, data[3]);
 				} else {
 					PlayerMetadata pm = uid2player.get(data[2]);
 					if (pm != null)
 						plugin.sendIrcMessage(uid2meta.get(data[0]).nick, pm, data[3]);
 					// ignore it :/
 				}
 				break;
 			case PART:
 				ChannelMetadata ch = chmap.get(data[2]);
 				if (ch == null)
 					break;
 				plugin.sendIrcPart(uid2meta.get(data[0]), chmap.get(data[2]));
 				break;
 			case QUIT:
 				plugin.playerQuit(uid2meta.get(data[0]));
 				break;
 			case KILL:
 				// wtf should I do now? the user is no longer on IRC..
 				// reconnect him :)
 				// maybe we could kill him later (kick from server) but I'm not
 				// sure if admins want this
 				boolean ips = config.getBoolean("propagate-ip", true);
 				String defhost = config.getString("userhost", "<host>");
 				String ident = config.getString("userident", "<nick>");
 				String uid = data[2];
 				String ip,
 				hostname,
 				realhost;
 				PlayerMetadata meta = uid2player.get(uid);
 				Player p = meta.getPlayer();
 				if (ips) {
 					InetAddress addr = p.getAddress().getAddress();
 					ip = addr.getHostAddress();
 					realhost = addr.getCanonicalHostName();
 					if (defhost.contains("<host>"))
 						hostname = defhost.replaceAll("<host>", realhost);
 					else
 						hostname = defhost;
 				} else {
 					ip = "127.0.0.1";
 					realhost = "localhost";
 					if (defhost.contains("<host>"))
 						hostname = "minecraft";
 					else
 						hostname = defhost;
 				}
 				String id = ident;
 				if (ident.contains("<nick>"))
 					id = ident.replaceAll("<nick>", p.getName());
 				this.println(pre + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + " +" + (p.isOp() ? "or" : "r") + " :Minecraft Player\r\n");
 				if (p.isOp())
 					this.println(":" + uid + " OPERTYPE Ops\r\n");
 				// log the user in to services (might not work, but might work)
 				this.println(":" + uid + " METADATA accountname " + p.getName() + "\r\n");
 				this.println(":" + uid + " METADATA swhois :is playing Minecraft\r\n");
 				for (String s : meta.getChannels()) {
 					cm = plugin.getChannel(s);
 					if (!chmap.contains(cm))
 						continue;
 					this.println(":" + sid + " FJOIN " + cm.getIrcRelay() + " " + cm.getTs() + " + :," + uid);
 				}
 				break;
 			default:
 				// ignore
 			}
 
 		} catch (IllegalArgumentException e) {
 			plugin.getServer().getLogger().log(Level.INFO, "[IRCD] Got unknown command: " + data[1], e);
 		}
 	}
 
 	private void joinchannels() throws IOException {
 		assert (state == State.CONNECTED);
 		synchronized (pending) {
 			for (Player p : pending) {
 				addPlayer(p);
 			}
 		}
 		pending.clear();
 		for (ChannelMetadata ch : chmap.values()) {
 			String modes = "+";
 			String ts = ch.getTs();
 			if (ts == null) {
 				ch.setTs(ts = getTS());
 				if (ch.isHidden())
 					modes = "+s";
 			}
 			String usrStr = "";
 			String lim = "";
 			for (PlayerMetadata m : ch.getPlayers()) {
 				usrStr += lim;
 				usrStr += ",";
 				usrStr += m.getUid();
 				lim = " ";
 			}
 
 			this.println(":" + sid + " FJOIN " + ch.getIrcRelay() + " " + ts + " " + modes + " " + " :" + usrStr);
 		}
 	}
 
 	public static String getTS() {
 		return String.valueOf(System.currentTimeMillis() / 1000);
 	}
 
 	public static String[] split(String line) {
 		String[] sp1 = line.split(" :", 2);
 		String[] sp2 = sp1[0].split(" ");
 		String[] res;
 		if (!sp2[0].startsWith(":")) {
 			res = new String[sp1.length + sp2.length];
 			System.arraycopy(sp2, 0, res, 1, sp2.length);
 		} else {
 			res = new String[sp1.length + sp2.length - 1];
 			System.arraycopy(sp2, 0, res, 0, sp2.length);
 			res[0] = res[0].substring(1);
 		}
 		if (sp1.length == 2)
 			res[res.length - 1] = sp1[1];
 		return res;
 	}
 
 	public void exit() {
 		stopped = true;
 		this.println(":" + sid + " SQUIT " + sid + " :Unloading Plugin\r\n");
 		try {
 			socket.close();
 		} catch (IOException e) {
 			// happens
 		}
 	}
 
 	public void sendMessage(PlayerMetadata metadata, String channel, String message) {
 		this.println(":" + metadata.getUid() + " PRIVMSG " + channel + " :" + message);
 	}
 
         public void sendMessage(String nick, String channel, String message) {
 //                ListIterator<PlayerMetadata> pmIt = plugin.getChannel(channel).getPlayers().listIterator();
 //                PlayerMetadata metadata = pmIt.next();
 //                while (pmIt.hasNext() && (!metadata.getPlayer().getName().equalsIgnoreCase(nick))) {
 //                        metadata = pmIt.next();
 //                        //rest of the code block removed
 //                }
                 PlayerMetadata metadata = plugin.getMetadata(plugin.getServer().getPlayer(nick));
 		this.println(":" + metadata.getUid() + " PRIVMSG " + channel + " :" + message);
 	}
 
 	public void addPlayer(Player player) {
 		synchronized (pending) {
 			switch (state) {
 			case DISCONNECTED:
 			case BURSTNOUID:
 				return;
 			case BURST:
 				pending.add(player);
 				return;
 			case CONNECTED:
 				break;
 			}
 		}
 		boolean ips = config.getBoolean("propagate-ip", true);
 		String defhost = config.getString("userhost", "<host>");
 		String ident = config.getString("userident", "<nick>");
 		String uid = sid + uidgen.generateUID();
 		String ip, hostname, realhost;
 		PlayerMetadata meta = plugin.getMetadata(player);
 		meta.setUid(uid);
 		uid2player.put(uid, meta);
 		Player p = player;
 		if (ips) {
 			InetAddress addr = p.getAddress().getAddress();
 			ip = addr.getHostAddress();
 			realhost = addr.getCanonicalHostName();
 			if (defhost.contains("<host>"))
 				hostname = defhost.replaceAll("<host>", realhost);
 			else
 				hostname = defhost;
 		} else {
 			ip = "127.0.0.1";
 			realhost = "localhost";
 			if (defhost.contains("<host>"))
 				hostname = "minecraft";
 			else
 				hostname = defhost;
 		}
 		String id = ident;
 		if (ident.contains("<nick>"))
 			id = ident.replaceAll("<nick>", p.getName());
 		this.println(":" + sid + " UID " + uid + " 2 " + p.getName() + " " + realhost + " " + hostname + " " + id + " " + ip + " " + meta.getSignon() + " +" + (p.isOp() ? "or" : "r") + " :Minecraft Player\r\n");
 		if (p.isOp())
 			this.println(":" + uid + " OPERTYPE Ops");
 		// log the user in to services (might not work, but might work)
 		this.println(":" + sid + " METADATA " + uid + " accountname " + p.getName());
 		this.println(":" + sid + " METADATA " + uid + " swhois :is playing Minecraft");
 		for (String s : meta.getChannels()) {
 			ChannelMetadata cm = plugin.getChannel(s);
 			if (!chmap.contains(cm))
 				continue;
 			this.println(":" + sid + " FJOIN " + cm.getIrcRelay() + " " + cm.getTs() + " + :," + uid + "");
 		}
 	}
 
        public void partPlayer(Player player, ChannelMetadata channel, String message) {
            this.println(":" + plugin.getMetadata(player).getUid() + " PART " + channel.getIrcRelay() + " :" + message);
        }

        public void kickPlayer(Player player, ChannelMetadata channel, String message) {

        }

 	public void removePlayer(Player player, String string) {
 		this.println(":" + plugin.getMetadata(player).getUid() + " QUIT :Quit: " + string);
 	}
 
 	public void removePlayer(Player player, String string, String string2) {
 		this.println(":" + sid + " OPERQUIT " + plugin.getMetadata(player).getUid() + " :Kicked: " + string2);
 		this.println(":" + plugin.getMetadata(player).getUid() + " QUIT :Quit: " + string);
 	}
 
 	public boolean isValid(String channel) {
 		if (channel == null)
 			return false;
 		if (channel.equals(""))
 			return false;
 		if (channel.charAt(0) != '#')
 			return false;
 		return true;
 	}
 
 	public void sendAction(PlayerMetadata src, String channel, String message) {
 		this.println(":" + src.getUid() + " PRIVMSG " + channel + " :\u0001ACTION " + message + "\u0001");
 	}
 
 	public Collection<IrcUser> getUser() {
 		return uid2meta.values();
 	}
 
 	protected void println(String line) {
 		try {
                         if (this.printdbg)
                                 System.out.println(line);
 			w.write(line);
 			w.newLine();
 			w.flush();
 		} catch (IOException e) {
 			// ok, something goes wrong
 			plugin.getServer().getLogger().log(Level.SEVERE, "IOException while writing data ", e);
 			plugin.restartIrcd();
 			// throw a thread death to exit the current thread
 			if (Thread.currentThread() == this)
 				throw new ThreadDeath();
 		}
 	}
 }
