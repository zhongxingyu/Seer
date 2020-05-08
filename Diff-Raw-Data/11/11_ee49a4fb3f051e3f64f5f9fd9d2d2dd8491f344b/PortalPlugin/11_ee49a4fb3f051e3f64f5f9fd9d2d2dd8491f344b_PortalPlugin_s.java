 package lilypad.bukkit.portal.plugin;
 
 import java.io.File;
 import java.util.Collections;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import lilypad.bukkit.portal.IConnector;
 import lilypad.bukkit.portal.IRedirector;
 import lilypad.bukkit.portal.command.PortalCommandExecutor;
 import lilypad.bukkit.portal.command.create.CreateListener;
 import lilypad.bukkit.portal.gate.Gate;
 import lilypad.bukkit.portal.gate.GateListener;
 import lilypad.bukkit.portal.gate.GateRegistry;
 import lilypad.bukkit.portal.storage.Storage;
 import lilypad.bukkit.portal.storage.impl.FileStorage;
 import lilypad.bukkit.portal.user.UserListener;
 import lilypad.bukkit.portal.user.UserRedirectorTask;
 import lilypad.bukkit.portal.user.UserRegistry;
 import lilypad.bukkit.portal.util.MessageConstants;
 import lilypad.client.connect.api.Connect;
 import lilypad.client.connect.api.request.impl.MessageRequest;
 import lilypad.client.connect.api.request.impl.RedirectRequest;
 import lilypad.client.connect.api.result.FutureResultListener;
 import lilypad.client.connect.api.result.StatusCode;
 import lilypad.client.connect.api.result.impl.MessageResult;
 
 public class PortalPlugin extends JavaPlugin implements IRedirector, IConnector {
 
 	private GateRegistry gateRegistry;
 	private GateListener gateListener;
 	private UserRegistry userRegistry;
 	private UserListener userListener;
 	private CreateListener createListener;
 	private Storage storage;
 
 	@Override
 	public void onLoad() {
 		super.getConfig().options().copyDefaults(true);
 		super.saveConfig();
 		super.reloadConfig();
 	}
 
 	@Override
 	public void onEnable() {
 		try {
 			this.gateRegistry = new GateRegistry();
 			this.gateListener = new GateListener(this.gateRegistry, this);
 			this.userRegistry = new UserRegistry();
 			this.userListener = new UserListener(this, this.gateRegistry, this.userRegistry, this, this);
 			this.createListener = new CreateListener(this.gateRegistry);
 			this.storage = new FileStorage(new File(this.getDataFolder(), "store_gate.dat"), new File(this.getDataFolder(), "store_user.dat"));
 			this.storage.setUserRegistry(this.userRegistry);
 			this.storage.loadUsers();
 			this.storage.setGateRegistry(this.gateRegistry);
 			this.storage.loadGates();
 			this.getServer().getPluginCommand("portal").setExecutor(new PortalCommandExecutor(this.gateRegistry, this.createListener));
 			this.getServer().getScheduler().scheduleSyncRepeatingTask(this, this.storage, 100L, 100L);
 			this.getServer().getPluginManager().registerEvents(this.gateListener, this);
 			this.getServer().getPluginManager().registerEvents(this.userListener, this);
 			this.getServer().getPluginManager().registerEvents(this.createListener, this);
 			this.getConnect().registerMessageEventListener(this.userListener);
 		} catch(Exception exception) {
 			exception.printStackTrace();
 		}
 		for(Player player : this.getServer().getOnlinePlayers()) {
 			this.userListener.getUser(player.getName());
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		try {
 			if(this.getConnect() != null) {
 				this.getConnect().unregisterMessageEventListener(this.userListener);
 			}
 			if(this.storage != null) {
 				this.storage.saveAll();
 			}
 			if(this.gateRegistry != null) {
 				this.gateRegistry.clear();
 			}
 			if(this.userRegistry != null) {
 				this.userRegistry.clear();
 			}
 		} catch(Exception exception) {
 			exception.printStackTrace();
 		} finally {
 			this.gateRegistry = null;
 			this.gateListener = null;
 			this.userRegistry = null;
 			this.userListener = null;
 			this.createListener = null;
 			this.storage = null;
 		}
 	}
 
 	public void requestRedirect(final Player player, Gate gate) {
 		try {
 			this.getConnect().request(new MessageRequest(gate.getDestinationServer(), "lpPortal", "REQUEST " + player.getName())).registerListener(new FutureResultListener<MessageResult>() {
 				public void onResult(MessageResult messageResult) {
 					if(messageResult.getStatusCode() == StatusCode.SUCCESS) {
 						return;
 					}
 					player.sendMessage(MessageConstants.format(MessageConstants.SERVER_OFFLINE));
 				}
 			});
 		} catch(Exception exception) {
			exception.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void announceRedirect(Player player) {
 		try {
 			this.getConnect().request(new MessageRequest(Collections.EMPTY_LIST, "lpPortal", "ANNOUNCE " + player.getName()));
 		} catch(Exception exception) {
			exception.printStackTrace();
 		}
 	}
 	
 	public void redirect(String username, String server) {
 		try {
 			this.getConnect().request(new RedirectRequest(server, username));
 		} catch(Exception exception) {
			exception.printStackTrace();
 		}
 	}
 	
 	public boolean redirectResult(String username, String server) {
 		try {
 			return this.getConnect().request(new RedirectRequest(server, username)).await().getStatusCode() == StatusCode.SUCCESS;
 		} catch(Exception exception) {
			exception.printStackTrace();
 		}
 		return false;
 	}
 
 	public void redirectLastServer(String username, String server) {
 		if(!this.getConfig().getBoolean("redirectLastServer", false)) {
 			return;
 		}
 		UserRedirectorTask userRedirector = new UserRedirectorTask(username, server, this);
 		userRedirector.setTaskId(this.getServer().getScheduler().runTaskTimerAsynchronously(this, userRedirector, 20L, 100L).getTaskId());
 	}
 	
 	public Connect getConnect() {
 		return this.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
 	}
 
 }
