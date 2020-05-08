 package com.pleaseignore.BukkitXMPP;
 
 import java.io.File;
 import java.util.logging.Logger;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.World;
 import org.bukkit.Server;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.ChatColor;
 import org.bukkit.util.config.Configuration;
 
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.muc.DiscussionHistory;
 
 /**
  * BukkitXMPP for Bukkit
  *
  * @author Matalok
  */
 public class BukkitXMPP extends JavaPlugin implements PacketListener {
     private final BukkitXMPPPlayerListener playerListener = new BukkitXMPPPlayerListener(this);
 
     private Logger log;
 
     public XMPPConnection xmppconn;
     public MultiUserChat muc;
 
     private String nickname;
     private Configuration conf;
 
     private void loadConfig(File yamlfile) {
         conf = new Configuration(yamlfile);
         if(yamlfile.exists()) {
             conf.load();
         } else {
             conf.setProperty("connection.server", "example.org");
             conf.setProperty("connection.username", "exampleuser");
             conf.setProperty("connection.password", "examplepass");
             conf.setProperty("connection.channel", "examplechannel@talk.example.org");
             conf.setProperty("connection.nickname", "MinecraftBot");
             conf.getBoolean("general.autoconnect", false);
             conf.save();
         }
     }
 
     private void connectAndJoin() {
 
         String server = conf.getString("connection.server", "");
         String username = conf.getString("connection.username", "");
         String password = conf.getString("connection.password", "");
         String channel = conf.getString("connection.channel", "");
         String nickname = conf.getString("connection.nickname", "MinecraftBot");
 
         if (!server.equals("") && !username.equals("") && !password.equals("") && !channel.equals("")) {
             try {
                 log.info("Connecting to XMPP");
                 xmppconn = new XMPPConnection(server);
                 xmppconn.connect();
                 xmppconn.login(username, password);
 
                 log.info("Joining Room");
                 muc = new MultiUserChat(xmppconn, channel);
 
                 DiscussionHistory history = new DiscussionHistory();
                 history.setMaxStanzas(0);
 
                 muc.join(nickname, "", history, 2000);
                 muc.addMessageListener(this);
             } catch (Exception e) {
                 log.warning("Error connecting to XMPP server " + server);
             }
         } else {
             log.warning("Incomplete configration for XMPP server");
         }
     }
 
     private void disconnect() {
 
         if (muc instanceof MultiUserChat) {
             muc.leave();
             muc = null;
         }
 
         if (xmppconn instanceof XMPPConnection) {
             xmppconn.disconnect();
             xmppconn = null;
         }
 
     }
 
     public void onEnable() {
 
         log = getServer().getLogger();
 
         final File yml = new File(getDataFolder(), "BukkitXMPP.yml");
         log.info("Path to BukkitXMLPP.yml: " + yml.getPath());
         loadConfig(yml);
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Normal, this);
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info( pdfFile.getName() + " v" + pdfFile.getVersion() + " loaded" );
 
         // Connect to XMPP
         if (conf.getBoolean("general.autoconnect", true))
             connectAndJoin();
     }
 
     public void onDisable() {
         disconnect();
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled");
     }
 
     public List<Player> getListeners() {
         List<Player> list = null;
         for(World world: getServer().getWorlds()) {
             if (list == null) {
                 list = world.getPlayers();
             } else {
                 list.addAll(world.getPlayers());
             }
         }
 
         return list;
     }
 
     public void sendMCMessage(String msg) {
         for(Player p: getListeners()) {
             p.sendMessage(msg);
         }
     }
 
     public void processPacket(Packet p)
     {
         if (p instanceof Message) {
             final Message message = (Message) p;
             if(message.getType() == Message.Type.groupchat) {
                if(!StringUtils.parseResource(message.getFrom()).equalsIgnoreCase(conf.getString("connection.nickname", "MinecraftBot"))) {
                     String outmsg = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "XMPP" + ChatColor.GRAY + "] " + ChatColor.WHITE + StringUtils.parseResource(message.getFrom()) + ": " + message.getBody();
                     sendMCMessage(outmsg);
                     log.info(outmsg);
                 }
             }
         }
     }
 
 }
 
