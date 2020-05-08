 package net.mcforge.verification;
 
 import net.mcforge.API.ManualLoad;
 import net.mcforge.API.player.PlayerBlockChangeEvent;
 import net.mcforge.API.player.PlayerCommandEvent;
 import net.mcforge.API.player.PlayerConnectEvent;
 import net.mcforge.API.player.PlayerDisconnectEvent;
 import net.mcforge.API.plugin.Plugin;
 import net.mcforge.server.Server;
 import net.mcforge.util.properties.Properties;
 
 @ManualLoad
 public class AdminVerification extends Plugin {
 	@Override
 	public String getName() {
 		return "MCForge Admin Verification Plugin";
 	}
 	@Override
 	public String getAuthor() {
 		return "Arrem";
 	}
 	@Override
 	public String getVersion() {
 		return "1.0.0";
 	}
 	
 	private EventListener handler;
 	private CmdDeletePass cmdDeletePass = new CmdDeletePass();
 	private CmdPass cmdPass = new CmdPass();
 	private CmdSetPass cmdSetPass = new CmdSetPass();
 	private CmdVerification cmdVerification;
 
 	protected static int identificationPerm;
 	protected static boolean identificationToggle;
 
 	public AdminVerification(Server server) {
 		super(server);
 	}
 
 	public AdminVerification(Server server, java.util.Properties properties) {
 		super(server, properties);
 	}
 
 	@Override
 	public void onLoad(String[] args) {
 		handler = new EventListener();
 		getServer().getEventSystem().registerEvents(handler);
 		getServer().getCommandHandler().addCommand(cmdDeletePass);
 		getServer().getCommandHandler().addCommand(cmdPass);
 		getServer().getCommandHandler().addCommand(cmdSetPass);
 
 		Properties properties = getServer().getSystemProperties();
 
 		if (!properties.hasValue("Identification-Permission")) {
 			properties.addSetting("Identification-Permission", (identificationPerm = 100));
 			properties.addComment("Identification-Permission", "The minimal rank that has to identify");
 		}
 		else {
 			identificationPerm = properties.getInt("Identification-Permission");
 		}
 
 		if (!properties.hasValue("Identification-Toggle")) {
 			properties.addSetting("Identification-Toggle", (identificationToggle = false));
			properties.addComment("Identification-Toggle", "Whether staff can choose not to identify. Note that this is a security risk");
 		}
 		else {
 			identificationToggle = properties.getBool("Identification-Toggle");
 		}
 
 		if (identificationToggle) {
 			getServer().getCommandHandler().addCommand((cmdVerification = new CmdVerification()));
 		}
 	}
 
 	@Override
 	public void onUnload() {
 		PlayerCommandEvent.getEventList().unregister(handler);
 		PlayerBlockChangeEvent.getEventList().unregister(handler);
 		PlayerConnectEvent.getEventList().unregister(handler);
 		PlayerDisconnectEvent.getEventList().unregister(handler);
 
 		getServer().getCommandHandler().removeCommand(cmdDeletePass);
 		getServer().getCommandHandler().removeCommand(cmdPass);
 		getServer().getCommandHandler().removeCommand(cmdSetPass);
 		if (identificationToggle) {
 			getServer().getCommandHandler().removeCommand(cmdVerification);
 		}
 	}
 }
