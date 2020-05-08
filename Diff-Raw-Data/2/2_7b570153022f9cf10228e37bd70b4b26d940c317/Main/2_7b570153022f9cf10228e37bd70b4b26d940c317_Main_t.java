 package mmode;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.comphenix.protocol.ProtocolLibrary;
 import com.comphenix.protocol.ProtocolManager;
 import com.comphenix.protocol.events.ConnectionSide;
 import com.comphenix.protocol.events.ListenerPriority;
 import com.comphenix.protocol.events.PacketAdapter;
 import com.comphenix.protocol.events.PacketEvent;
 import com.comphenix.protocol.injector.GamePhase;
 import com.comphenix.protocol.reflect.StructureModifier;
 
 public class Main extends JavaPlugin implements Listener {
 
 	public ProtocolManager protocolManager = null;
 	private Config config;
 	private Commands commands;
 	@Override
 	public void onLoad() 
 	{
 	    protocolManager = ProtocolLibrary.getProtocolManager();
 	}
 	
 	@Override
 	public void onEnable()
 	{
 		config = new Config();
 		config.loadConfig();
 		commands = new Commands(config);
 		getCommand("mmode").setExecutor(commands);
 		
 		protocolManager.addPacketListener(
 	    		new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, GamePhase.LOGIN, new Integer[] { Integer.valueOf(255) })
 	    		{
 	    			public void onPacketSending(PacketEvent event) {
 	    				try {
 	    					if (!config.mmodeEnabled) {return;}
 	    					
 	    					StructureModifier<String> packetStr = event.getPacket().getSpecificModifier(String.class);
 	    					String p = (String)packetStr.read(0);
 	    					String prep = p.substring(0, 3);
	    					String motd = config.mmodeMOTD.replace("{motd}", p.split("\u0000")[3]);
 	    					packetStr.write(0, 
 	    							prep  //first 3 bytes
 	    							+ -1  //protocol vesion
 	    							+ "\u0000" 
 	    							+ config.mmodeMessage //message near ping
 	    							+ "\u0000" 
 	    							+ motd //motd
 	    							+ "\u0000" 
 	    							+ getServer().getOnlinePlayers().length  //online players count
 	    							+ "\u0000" 
 	    							+ getServer().getMaxPlayers() // max players
 	    							);
 	    				}
 	    				catch (Exception e) {}
 	    			}
 	    		}
 	    );
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		 protocolManager.removePacketListeners(this);
 		 protocolManager = null;
 		 commands = null;
 		 config = null;
 	}
 	
 	@EventHandler
 	public void onJoin(PlayerJoinEvent e)
 	{
 		if (!config.mmodeEnabled) {return;}
 		
 		if (!config.mmodeAdminsList.contains(e.getPlayer().getName()))
 		{
 			e.getPlayer().kickPlayer(config.mmodeKickMessage);
 		}
 		
 	}
 	
 	
 }
