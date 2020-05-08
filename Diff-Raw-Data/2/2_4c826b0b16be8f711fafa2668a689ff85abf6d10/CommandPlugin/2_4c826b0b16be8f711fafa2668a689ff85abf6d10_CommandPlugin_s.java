 package lilypad.bukkit.portal.command.plugin;
 
 import java.util.List;
 
 import lilypad.bukkit.portal.command.IConfig;
 import lilypad.bukkit.portal.command.IRedirector;
 import lilypad.bukkit.portal.command.ServerCommand;
 import lilypad.bukkit.portal.command.ServerQuickCommand;
 import lilypad.bukkit.portal.command.util.MessageConstants;
 import lilypad.client.connect.api.Connect;
 import lilypad.client.connect.api.request.impl.MessageRequest;
 import lilypad.client.connect.api.result.FutureResultListener;
 import lilypad.client.connect.api.result.StatusCode;
 import lilypad.client.connect.api.result.impl.MessageResult;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CommandPlugin extends JavaPlugin implements IConfig, IRedirector {
 
 	@Override
 	public void onLoad() {
 		super.getConfig().options().copyDefaults(true);
 		super.saveConfig();
 		super.reloadConfig();
 	}
 	
 	@Override
 	public void onEnable() {
 		super.getCommand("server").setExecutor(new ServerCommand(this, this));
 		if(this.isQuickCommands()) {
 			super.getServer().getPluginManager().registerEvents(new ServerQuickCommand(this, this), this);
 		}
 	}
 
 	public Connect getConnect() {
 		return super.getServer().getServicesManager().getRegistration(Connect.class).getProvider();
 	}
 	
 	public List<String> getAllowedServers() {
 		return super.getConfig().getStringList("allowed-servers");
 	}
 
 	public boolean isQuickCommands() {
 		return super.getConfig().getBoolean("quick-commands");
 	}
 
 	public void requestRedirect(final Player player, final String server) {
 		if(server.equals(this.getConnect().getSettings().getUsername())) {
 			return;
 		}
 		try {
 			this.getConnect().request(new MessageRequest(server, "lpPortal", "REQUEST " + player.getName())).registerListener(new FutureResultListener<MessageResult>() {
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
 	
 }
