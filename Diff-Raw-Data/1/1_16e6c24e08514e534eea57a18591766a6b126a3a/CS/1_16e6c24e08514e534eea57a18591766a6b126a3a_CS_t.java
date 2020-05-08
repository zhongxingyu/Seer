 package rageteam.cookieslap.main;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 
 import rageteam.cookieslap.commands.HelpCommand;
 import rageteam.cookieslap.handlers.PlayerHandler;
 import rageteam.cookieslap.handlers.ServerHandler;
 import rageteam.cookieslap.managers.ArenasManager;
 import rageteam.cookieslap.managers.ConfigManager;
 import rageteam.cookieslap.managers.PlayerManager;
 import rageteam.cookieslap.objects.GameState;
 import rageteam.cookieslap.util.Chat;
 import rageteam.cookieslap.util.Logger;
 import rageteam.cookieslap.util.Misc;
 
 public class CS extends JavaPlugin{
 	
 	public GameState gameState = GameState.LOBBY;
 	public boolean canStart = false;
 	
 	//Command Classes
 	public HelpCommand cmdHelp;
 	
 	//Handler Classes
 	public ServerHandler serverHandler;
 	public PlayerHandler playerHandler;
 	
 	//Manager Classes
 	public PlayerManager playerManager;
 	public ConfigManager cfgManager;
 	public ArenasManager arenasmanager;
 	
 	//Util Classes
 	public Chat chat;
 	public Logger logger;
 	public Misc misc;
 	
 	//Scoreboard
 	ScoreboardManager manager;
 	Scoreboard board;
 	Objective obj;
 	public int timeInSeconds = 240;
 	public int onlinePlayers = Bukkit.getServer().getOnlinePlayers().length;
 	public int highScore = 0;
 	public int arenaID = 0;
 	
 	private void loadDependicies(){
 		
 		this.chat = new Chat(this);
 		this.logger = new Logger(this);
 		this.misc = new Misc(this);
 		
 		this.cmdHelp = new HelpCommand(this);
 		
 		this.playerManager = new PlayerManager(this);
 		this.cfgManager = new ConfigManager(this);
 		this.arenasmanager = new ArenasManager(this);
 		
 		this.playerHandler = new PlayerHandler(this);
 		this.serverHandler = new ServerHandler(this);
 	}
 	
 	private void loadHandlers(){
 		getServer().getPluginManager().registerEvents(playerHandler, this);
 		getServer().getPluginManager().registerEvents(serverHandler, this);
 	}
 	
 	private void registerCommands(){
 		getCommand("help").setExecutor(cmdHelp);
 		
 	}
 	
 	@Override
 	public void onEnable(){
 		loadDependicies();
 		loadHandlers();
 		registerCommands();
 		
 		logger.log(false, "Loading CS Dependicies");
 		
 		//ScoreBoard
 		manager = Bukkit.getScoreboardManager();
 		board = manager.getNewScoreboard();
 		obj = board.registerNewObjective("CookieSlap", "dummy");
 		
 		obj.setDisplayName(ChatColor.GRAY +  "  /toggleboard  " + ChatColor.WHITE + " to hide");
 		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 		
 		final Score time = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.LIGHT_PURPLE + "Time Left:" + ChatColor.RED));
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 			public void run(){
 				if(timeInSeconds != -1){
 					if(timeInSeconds != 0){
 						time.setScore(timeInSeconds);
						timeInSeconds--;
 					} else if(timeInSeconds <= 10 && timeInSeconds > 0){
 						note();
 					}
 				}
 			}
 		}, 0L, 20L);
 		
 		Score players = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.DARK_AQUA + "Players:" + ChatColor.RED));
 		players.setScore(onlinePlayers);
 		
 		Score arena = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.GREEN + "Arena ID:" + ChatColor.RED));
 		arena.setScore(arenaID);
 		
 		Score score = obj.getScore(Bukkit.getOfflinePlayer(ChatColor.YELLOW + "HighScore:" + ChatColor.RED));
 		score.setScore(highScore);
 		
 		for(Player player : Bukkit.getOnlinePlayers()){
 			player.setScoreboard(board);
 		}
 	}
 		
 		private void note() {
 			for(Player player : getServer().getOnlinePlayers()){
 				player.playSound(player.getLocation(), Sound.NOTE_PIANO, 10, 1);
 			}
 	}
 	
 	@Override
 	public void onDisable(){
 		
 	}
 
 }
