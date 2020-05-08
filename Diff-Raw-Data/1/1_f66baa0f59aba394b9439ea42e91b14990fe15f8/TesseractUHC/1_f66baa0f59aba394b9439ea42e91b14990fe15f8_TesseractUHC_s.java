 package com.martinbrook.tesseractuhc;
 
 import org.bukkit.ChatColor;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.messaging.Messenger;
 
 import com.martinbrook.tesseractuhc.command.*;
 import com.martinbrook.tesseractuhc.listeners.ChatListener;
 import com.martinbrook.tesseractuhc.listeners.ClientMessageChannelListener;
 import com.martinbrook.tesseractuhc.listeners.LoginListener;
 import com.martinbrook.tesseractuhc.listeners.MatchListener;
 import com.martinbrook.tesseractuhc.listeners.SpectateListener;
 
 public class TesseractUHC extends JavaPlugin {
 	private static TesseractUHC instance = null;
 	public static final ChatColor MAIN_COLOR = ChatColor.GREEN, SIDE_COLOR = ChatColor.GOLD, OK_COLOR = ChatColor.GREEN, WARN_COLOR = ChatColor.LIGHT_PURPLE, ERROR_COLOR = ChatColor.RED,
 			DECISION_COLOR = ChatColor.GOLD, ALERT_COLOR = ChatColor.GREEN;
 	private UhcMatch match;
 	
 	//Plugin messages (to the autoreferee-client) constants
 	public static final String PLUGIN_CHANNEL = "autoref:referee";
 	public static final String PLUGIN_CHANNEL_ENC = "UTF-8";
 	public static final String PLUGIN_CHANNEL_WORLD = "UHCworld";
 	public static final char PLUGIN_CHANNEL_DELIMITER = '|';
 	
 	/**
 	 * Get the singleton instance of UhcTools
 	 * 
 	 * @return The plugin instance
 	 */
 	public static TesseractUHC getInstance() { return instance; }
 	
 	public UhcMatch getMatch() { return match; }
 
 	public void onEnable() {
 		
 		// Store singleton instance
 		instance = this;
 
 		saveDefaultConfig();
 		match = new UhcMatch(this, getServer().getWorlds().get(0), getConfig());
 		
 		setupPluginChannels();
 	
 		getServer().getPluginManager().registerEvents(new ChatListener(match), this);
 		getServer().getPluginManager().registerEvents(new LoginListener(match), this);
 		getServer().getPluginManager().registerEvents(new MatchListener(match), this);
 		getServer().getPluginManager().registerEvents(new SpectateListener(match), this);
 		getServer().getPluginManager().registerEvents(new ClientMessageChannelListener(match), this);
 
 		getCommand("tp").setExecutor(new TpCommand(this));
 		getCommand("heal").setExecutor(new HealCommand(this));
 		getCommand("feed").setExecutor(new FeedCommand(this));
 		getCommand("clearinv").setExecutor(new ClearinvCommand(this));
 		getCommand("renew").setExecutor(new RenewCommand(this));
 		getCommand("ready").setExecutor(new ReadyCommand(this));
 		getCommand("cdwb").setExecutor(new CdwbCommand(this));
 		getCommand("cdc").setExecutor(new CdcCommand(this));
 		getCommand("chatscript").setExecutor(new ChatscriptCommand(this));
 		getCommand("muteall").setExecutor(new MuteallCommand(this));
 		getCommand("permaday").setExecutor(new PermadayCommand(this));
 		getCommand("uhc").setExecutor(new UhcCommand(this));
 		getCommand("launch").setExecutor(new LaunchCommand(this));
 		getCommand("relaunch").setExecutor(new RelaunchCommand(this));
 		getCommand("calcstarts").setExecutor(new CalcstartsCommand(this));
 		getCommand("setvanish").setExecutor(new SetvanishCommand(this));
 		getCommand("players").setExecutor(new PlayersCommand(this));
 		getCommand("teams").setExecutor(new TeamsCommand(this));
 		getCommand("matchinfo").setExecutor(new MatchinfoCommand(this));
 		getCommand("setspawn").setExecutor(new SetspawnCommand(this));
 		getCommand("interact").setExecutor(new InteractCommand(this));
 		getCommand("tpd").setExecutor(new TpdCommand(this));
 		getCommand("tpl").setExecutor(new TplCommand(this));
 		getCommand("tpn").setExecutor(new TpnCommand(this));
 		getCommand("tp0").setExecutor(new Tp0Command(this));
 		getCommand("tps").setExecutor(new TpsCommand(this));
 		getCommand("tpcs").setExecutor(new TpcsCommand(this));
 		getCommand("tpp").setExecutor(new TppCommand(this));
 		getCommand("tpnext").setExecutor(new TpnextCommand(this));
 		getCommand("tpback").setExecutor(new TpbackCommand(this));
 		getCommand("gm").setExecutor(new GmCommand(this));
 		getCommand("vi").setExecutor(new ViCommand(this));
 		getCommand("kill").setExecutor(new KillCommand(this));
 		getCommand("notify").setExecutor(new NotifyCommand(this));
 		getCommand("n").setExecutor(new NotifyCommand(this));
 		getCommand("join").setExecutor(new JoinCommand(this));
 		getCommand("team").setExecutor(new TeamCommand(this));
 		getCommand("leave").setExecutor(new LeaveCommand(this));
 		getCommand("params").setExecutor(new ParamsCommand(this));
 		getCommand("pvp").setExecutor(new PvpCommand(this));
 		getCommand("spectate").setExecutor(new SpectateCommand(this));
 		getCommand("nv").setExecutor(new NvCommand(this));
 	}
 		
 		
 	public void onDisable(){
 		match.getConfig().saveMatchParameters();
 		this.match = null;
 		getServer().getScheduler().cancelTasks(this);
 	}
 	
 	/**
 	 * Registers the plugin channels for messages to the autoreferee-client
 	 */
 	public void setupPluginChannels()
 	{
 		Messenger m = getServer().getMessenger();
 
 		// setup referee plugin channels
 		m.registerOutgoingPluginChannel(this, PLUGIN_CHANNEL);
 		m.registerIncomingPluginChannel(this, PLUGIN_CHANNEL, new ClientMessageChannelListener(match));
 	}
 	
 
 }
