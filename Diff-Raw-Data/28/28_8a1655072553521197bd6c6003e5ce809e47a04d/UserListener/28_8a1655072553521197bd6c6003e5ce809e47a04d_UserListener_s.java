 package lilypad.bukkit.portal.user;
 
 import lilypad.bukkit.portal.IConnector;
 import lilypad.bukkit.portal.IRedirector;
 import lilypad.bukkit.portal.gate.Gate;
 import lilypad.bukkit.portal.gate.GateRegistry;
 import lilypad.client.connect.api.Connect;
 import lilypad.client.connect.api.MessageEvent;
 import lilypad.client.connect.api.MessageEventListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 

 public class UserListener implements Listener, MessageEventListener {
 
 	private Plugin plugin;
 	private GateRegistry gateRegistry;
 	private UserRegistry userRegistry;
 	private IConnector connector;
 	private IRedirector redirector;
 
 	public UserListener(Plugin plugin, GateRegistry gateRegistry, UserRegistry userRegistry, IConnector connector, IRedirector redirector) {
 		this.plugin = plugin;
 		this.gateRegistry = gateRegistry;
 		this.userRegistry = userRegistry;
 		this.connector = connector;
 		this.redirector = redirector;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
 		final Player player = playerJoinEvent.getPlayer();
 		User user = this.getUser(player.getName());
 		String fromServer = user.getFromServer();
 		if(fromServer == null) {
 			if(user.getServer().equals(this.connector.getConnect().getSettings().getUsername())) {
 				return;
 			}
 			this.redirector.redirectLastServer(user.getName(), user.getServer());
 			return;
 		}
		this.redirector.respondRedirect(player, fromServer);
 		user.setServer(this.connector.getConnect().getSettings().getUsername());
 		user.setFromServer(null);
 		if(this.plugin.getConfig().getBoolean("spawnAtPortalEndpoint", false)) {
 			final Gate gate = this.gateRegistry.getByDestinationServer(fromServer);
 			if(gate == null) {
 				return;
 			}
 			this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 				public void run() {
 					player.teleport(new Location(Bukkit.getServer().getWorld(gate.getOutwardWorld()), gate.getOutwardX(), gate.getOutwardY(), gate.getOutwardZ(), gate.getOutwardYaw(), 0));
 				}
 			}, 1L);
 		}
 	}
 
 	public void onMessage(Connect connect, MessageEvent messageEvent) {
 		if(!messageEvent.getChannel().equals("lpPortal")) {
 			return;
 		}
 		try {
 			String[] message = messageEvent.getMessageAsString().trim().split(" ");
 			if(message.length != 2) {
 				return;
 			}
 			if(message[0].equals("REQUEST")) {
 				this.redirector.redirect(message[1], this.connector.getConnect().getSettings().getUsername());
 				this.getUser(message[1]).setFromServer(messageEvent.getSender());
			} else if(message[0].equals("RESPONSE")) {
 				this.getUser(message[1]).setServer(messageEvent.getSender());
 			}
 		} catch(Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 
 	public User getUser(String name) {
 		if(!this.userRegistry.hasName(name)) {
 			this.userRegistry.register(new User(name, this.connector.getConnect().getSettings().getUsername()));
 		}
 		return this.userRegistry.getByName(name);
 	}
 
 }
