 /*
  * The MIT License
  *
  * Copyright (c) 2011 Andrew Williams (Nik_Doof/Matalok)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.pleaseignore.BukkitXMPP;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.muc.DiscussionHistory;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 /**
  * XMPP Plugin for Bukkit
  *
  * @author Matalok
  */
 public class BukkitXMPP extends JavaPlugin implements PacketListener {
     private final BukkitXMPPPlayerListener playerListener = new BukkitXMPPPlayerListener(this);
     private final BukkitParticipantStatusListener participantListener = new BukkitParticipantStatusListener(this);
 
     private Logger log;
 
     public XMPPConnection xmppconn;
     public MultiUserChat muc;
 
     private String nickname;
     private Configuration conf;
 
     /**
      * Loads a configuration file
      *
      * @param yamlfile File to parse
      */
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
             conf.setProperty("commands.prefix", "!");
             conf.save();
         }
     }
 
     /**
      * Connects to a XMPP server and joins the configured MUC rooms.
      */
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
 
                 if (!xmppconn.isConnected()) {
                     log.warning("Unable to connect to the XMPP server, please check your config!");
                     return;
                 }
 
                 xmppconn.login(username, password);
 
                 log.info("Joining Room");
                 muc = new MultiUserChat(xmppconn, channel);
 
                 DiscussionHistory history = new DiscussionHistory();
                 history.setMaxStanzas(0);
 
                 muc.join(nickname, "", history, 2000);
                 muc.addMessageListener(this);
                 muc.addParticipantStatusListener(participantListener);
             } catch (Exception e) {
                log.warning("Error connecting to XMPP server " + server + ": " + e.toString());
             }
         } else {
             log.warning("Incomplete configration for XMPP server");
         }
     }
 
     /**
      * Leave any active MUC rooms and disconnect from the XMPP server
      */
     private void disconnect() {
 
         if (muc instanceof MultiUserChat && muc.isJoined()) {
             muc.leave();
         }
 
         if (xmppconn instanceof XMPPConnection && xmppconn.isConnected()) {
             xmppconn.disconnect();
         }
 
     }
 
     /**
      * Enables the XMPP plugin
      */
     public void onEnable() {
 
         log = getServer().getLogger();
 
         final File yml = new File(getDataFolder(), "BukkitXMPP.yml");
         log.info("Path to BukkitXMPP.yml: " + yml.getPath());
         loadConfig(yml);
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
 
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info( pdfFile.getName() + " v" + pdfFile.getVersion() + " loaded" );
 
         // Connect to XMPP
         if (conf.getBoolean("general.autoconnect", true))
             connectAndJoin();
     }
 
     /**
      * Disables the XMPP plugin.
      */
     public void onDisable() {
         disconnect();
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled");
     }
 
     /**
      * Get a list of current players on the Server
      */
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
 
     /**
      * Send a message to the configured MUC room.
      *
      * @param msg Message to send
      */
     public void sendMUCMessage(String msg) {
         if (xmppconn.isConnected() && muc.isJoined()) {
             try {
                 muc.sendMessage(msg);
             } catch (Exception e) {
                 log.warning("Error sending MUC message");
             }
 
         }
     }
 
     /**
      * Send a message to the Minecraft chat.
      *
      * @param msg Message to send
      */
     public void sendMCMessage(String msg) {
         for(Player p: getListeners()) {
             p.sendMessage(msg);
         }
     }
 
     /**
      * Process a incoming XMPP packet
      *
      * @param p the (@link Packet) to process
      */
     public void processPacket(Packet p)
     {
         if (p instanceof Message) {
         	final Message message = (Message) p;
         	if(message.getType() == Message.Type.groupchat) {
         		if(!StringUtils.parseResource(message.getFrom()).equalsIgnoreCase(conf.getString("connection.nickname", "MinecraftBot"))) {
         			if (message.getBody().startsWith(conf.getString("commands.prefix", "!"))) {
         				if (message.getBody().substring(1).startsWith("players")) {
         					StringBuffer buffer = new StringBuffer();
         					for(Player x: getListeners()) {
         						buffer.append(" ").append(x.getName());
         					}
         					try {
         						sendMUCMessage("Online Players:" + buffer);
         					} catch (Exception e) {
 							// TODO: Error handling
 							e.printStackTrace();
         					}
         				}
 
         			} else {
         				String outmsg = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "XMPP" + ChatColor.GRAY + "] " + ChatColor.WHITE + StringUtils.parseResource(message.getFrom()) + ": " + message.getBody();
         				sendMCMessage(outmsg);
         				log.info(outmsg);
         			}
         		}
         	}
         }
     }
 
 }
 
