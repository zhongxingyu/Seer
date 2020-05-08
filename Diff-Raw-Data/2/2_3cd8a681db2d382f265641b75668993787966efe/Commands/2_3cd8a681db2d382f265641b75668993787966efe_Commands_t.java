 package eu.janinko.botninko;
 
 
 import eu.janinko.botninko.connections.RoomImpl;
 import eu.janinko.botninko.api.Presence;
 import eu.janinko.botninko.api.plugin.Command;
 import eu.janinko.botninko.api.plugin.MessageHandler;
 import eu.janinko.botninko.api.plugin.PresenceHandler;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import org.apache.log4j.Logger;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 public class Commands {
 	private RoomImpl conn;
 	private Plugins plugins;
 
 	private Map<String,Integer> privs = new HashMap<>();
 	private Timer timer = new Timer();
 	
 	private String prefix = ".";
 	
 	private static Logger logger = Logger.getLogger(Commands.class);
 	private String configDir;
 	
 	Commands(Plugins plugins, RoomImpl connection){
 		this.plugins = plugins;
 		this.conn = connection;
 	}
 
 	void init(){
 		plugins.setCommands(this);
 		conn.setCommands(this);
 		plugins.startPlugins();
 	}
 
     /**
 	 * Returns current command prefix.
 	 *
 	 * @return Command prefix.
 	 */
 	public String getPrefix() {
 		return prefix;
 	}
 
     /**
 	 * Sets command prefix. Default is '.' (dot).
 	 *
 	 * @param prefix Command prefix.
 	 */
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 
 	public void disconnected(){
 		plugins.disconnected();
 	}
 
 	void connected(){
 		plugins.connected();
 	}
 
 	public void handlePresence(Presence presence) {
 		if(!conn.isConnected()) return;
 
 		for(CommandWrapper cw : plugins.getPresencePlugins()){
 			PresenceHandler command = (PresenceHandler) cw.getPlugin();
                         if(logger.isTraceEnabled()){logger.trace("Handling presence with: " + command);}
 			command.handlePresence(presence);
 		}
 	}
 
 	public void handleStatus(StatusImpl status) {
 		if(!conn.isConnected()) return;
 
 		for(CommandWrapper cw : plugins.getPresencePlugins()){
 			PresenceHandler command = (PresenceHandler) cw.getPlugin();
                         if(logger.isTraceEnabled()){logger.trace("Handling status with: " + command);}
 			command.handleStatus(status);
 		}
 	}
 
 	public void handleMessage(Message message) {
 		if(!conn.isConnected()) return;
 		
 		if(message.getBody().startsWith(prefix) || message.getBody().startsWith(conn.getNick())){
 			handleCommand(message);
 			return;
 		}
 		for(CommandWrapper cw : plugins.getMessagePlugins()){
 			MessageHandler command = (MessageHandler) cw.getPlugin();
                         if(logger.isTraceEnabled()){logger.trace("Handling message with: " + command);}
 			command.handleMessage(new MessageImpl(message,this));
 		}
 	}
 
 	public void handleCommand(Message message) {
 		if(!conn.isConnected()) return;
 
 		String body = message.getBody();
 		if(body.startsWith(prefix)){
 			body = body.substring(prefix.length());
 		}else{ //(message.getBody().startsWith(connection.getNick()){
 			body = body.substring(conn.getNick().length());
 			if(body.matches("[:>]? .*")){
 				body = body.substring(1).trim();
 			}else{
 				return;
 			}
 		}
 
 		String[] command = body.split(" +");
 		if(logger.isTraceEnabled()){logger.trace("Handling command: " + Arrays.toString(command));}
 		
 		String from = message.getFrom();
 		int priv = getPrivLevel(from);
 		switch (command[0]) {
 			case "commands":
 				printCommands();
 				break;
 			case "help":
 				printHelp(command[1]);
 				break;
 			default:
 				CommandWrapper cw = plugins.getCommand(command[0]);
 				if(cw == null) return;
 				Command c = (Command) cw.getPlugin();
 				if(c.getPrivLevel() <= priv){
					c.handleCommand(new CommandMessageImpl(message, this, body.substring(command[0].length())));
 				}else if(logger.isInfoEnabled()){
 					logger.info("User " + from + " (priv " + priv + ") tried to do '" + message.getBody() + "' (priv " + c.getPrivLevel() + ")");
 				}
 				break;
 		}
 	}
 	
 	private void printHelp(String what){
 		if(!conn.isConnected()) return;
 
 		String help = null;
 		switch (what) {
 			case "help":
 				help="Si ze mě děláš srandu?";
 				break;
 			case "commands":
 				help="Proč to prostě nezkusíš? Jen to vypíše dostupné příkazy.";
 				break;
 			default:
 				CommandWrapper cw = plugins.getCommand(what);
 				if(cw != null){
 					help = ((Command) cw.getPlugin()).help(prefix);
 				}
 				break;
 		}
 		if(help == null){
 			help = "O tomhle ale vůbec nic nevím!";
 		}
 		conn.sendMessage(help);
 	}
 
 	private void printCommands(){
 		if(!conn.isConnected()) return;
 		
 		StringBuilder sb = new StringBuilder("Příkazy jsou: ");
 		sb.append(prefix);
 		sb.append("commands, ");
 		sb.append(prefix);
 		sb.append("help");
 		
 		for(CommandWrapper cw : plugins.getCommandPlugins()){
 			sb.append(", ");
 			sb.append(prefix);
 			Command c = (Command) cw.getPlugin();
 			sb.append(c.getCommand());
 		}
 		conn.sendMessage(sb.toString());
 	}
 
 	private int getPrivLevel(String usser){
 		MultiUserChat muc = conn.getMuc();
 		if(muc == null) return 0; // we are not connected
 		String jid = muc.getOccupant(usser).getJid();
 		if(jid == null) return 0; // we do not have right to read jid
 		jid = jid.split("/")[0].toLowerCase(); // normalize jid
 		Integer priv = privs.get(jid);
 		if(priv == null) return 0;
 		return priv;
 	}
 
     /**
 	 * Returns Room.
 	 *
 	 * @return Room that handle connection to XMPP server and MUC.
 	 */
 	public RoomImpl getRoom() {
 		return conn;
 	}
 
 	public Plugins getPlugins() {
 		return plugins;
 	}
 
 	public void privSet(String userJid, Integer decode) {
 		privs.put(userJid.toLowerCase(), decode);		
 	}
 
 	public Timer getTimer() {
 		return timer;
 	}
 
 	void stop() {
 		timer.cancel();
 		timer = new Timer();
 	}
 
 	void setConfiDir(String configDir) {
 		this.configDir = configDir;
 	}
 
 	String getConfigDir() {
 		return configDir;
 	}
 }
