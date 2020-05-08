 package cz.opt.morgulplugin.entity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import cz.opt.morgulplugin.MorgulPlugin;
 import cz.opt.morgulplugin.config.Config;
 import cz.opt.morgulplugin.database.DataBase;
 import cz.opt.morgulplugin.managers.ChatManager;
 import cz.opt.morgulplugin.structs.ChatChannel;
 import cz.opt.morgulplugin.structs.ChatStatus;
 import cz.opt.morgulplugin.structs.Stat;
 import cz.opt.morgulplugin.utils.Utils;
 
 public class MorgPlayer
 {
 	private static final String CONF_FILE = "player.conf";
 	private static final String SECTION = "Player";
 	private static int START_COINS;
 	private static int START_XP;
 	private static int START_LVL;
 	private int id;
 	private int m_coins;
 	private long lastPlayed;
 	private long minedBlocks;
 	private boolean logged;
 	private String name;
 	private String language;
 	private Player pl;
 	private SpoutPlayer spl;
 	private Hashtable<String, Stat> stats;
 	private ArrayList<ChatChannel> chatChannels;
 	private ChatStatus chatSt;
 	private Location logLoc;
 	
 	public String getChatStatusChannel()
 	{
 		return chatSt.getChannel();
 	}
 	
 	public void setChatStatusChannel(String channel)
 	{
 		chatSt.setChannel(channel);
 	}
 
 	public Hashtable<String, Stat> getStats()
 	{
 		return stats;
 	}
 
 	public static void init()
 	{
 		START_COINS = Integer.parseInt(Config.get(CONF_FILE, SECTION, "Starting_Coins"));
 		START_XP = Integer.parseInt(Config.get(CONF_FILE, SECTION, "Starting_Xp"));
 		START_LVL = Integer.parseInt(Config.get(CONF_FILE, SECTION, "Starting_Lvl"));
 	}
 	
 	public MorgPlayer(String name, Player pl, boolean logged)
 	{
 		this.name = name;
 		this.logged = logged;
 		this.pl = pl;
 		this.stats = new Hashtable<String, Stat>();
 		chatChannels = new ArrayList<ChatChannel>();
 		joinChannel(ChatManager.getWorldChannel());
 		if(name.contains("'"))
 		{
 			pl.kickPlayer("Player name is UnAllowed.");
 			pl.setBanned(true);
 			return;
 		}
 		loadUserData();
 		pl.teleport(logLoc);
 	}
 	
 	public void spoutInit()
 	{
 		chatSt = new ChatStatus();
 		spl.getMainScreen().attachWidget(MorgulPlugin.thisPlugin, chatSt.getButton());
 		spl.getMainScreen().attachWidget(MorgulPlugin.thisPlugin, chatSt.getLabel());
 	}
 	
 	private void loadUserData()
 	{
 		HashMap<Integer, HashMap<String, String>> rs = DataBase.query("SELECT * FROM players WHERE playername='" + name + "'");
 		if(rs.keySet().size() < 1)
 		{
 			newPlayer();
 			return;
 		}
 		id = Integer.parseInt(rs.get(1).get("id"));
 		logLoc = Utils.getLocFromString(rs.get(1).get("location"), pl.getWorld());
 		lastPlayed = Long.parseLong(rs.get(1).get("lastplayed"));
 		language = rs.get(1).get("lang");
 		rs = DataBase.query("SELECT * FROM player_accounts WHERE id='" + id + "'");
 		m_coins = Integer.parseInt(rs.get(1).get("morgul_coins"));
 		
 		loadStats(rs);
 	}
 	
 	private void loadStats(HashMap<Integer, HashMap<String, String>> rs)
 	{
 		rs = DataBase.query("SELECT * FROM player_stats WHERE id='" + id + "'");
 		List<HashMap<String, String>> all = Utils.mapToList(rs);
 		
 		for(int i = 0; i < all.size(); i++)
 		{
 			String statName = all.get(i).get("stat");
 			int xp = Integer.parseInt(all.get(i).get("xp"));
 			int lvl = Integer.parseInt(all.get(i).get("lvl"));
 			Stat stat = new Stat(xp, lvl, statName);
 			stats.put(statName, stat);
 			MorgulPlugin.debug("put " + statName);
 		}
 	}
 	
 	private void newPlayer()
 	{
 		DataBase.update("INSERT INTO players (playername, location) VALUES('" + this.name + "', '" + Utils.getLocAsString(getPlayer().getLocation()) + "')");
 		HashMap<Integer, HashMap<String, String>> rs = DataBase.query("SELECT * FROM players WHERE playername='" + name + "'");
 		stats.put("mining", new Stat(0, 1, "mining"));
 		stats.put("harvesting", new Stat(0, 1, "harvesting"));
 		stats.put("digging", new Stat(0, 1, "digging"));
 		
 		if(rs.keySet().size() < 1)
 			return;
 		
 		id = Integer.parseInt(rs.get(1).get("id"));
 		logLoc = Utils.getLocFromString(rs.get(1).get("location"), pl.getWorld());
 		lastPlayed = Long.parseLong(rs.get(1).get("lastplayed"));
 		minedBlocks = 0L;
 		language = "en";
 		
 		initDatabase();
 		
 		setM_coins(START_COINS);
 		MorgulPlugin.debug("New Player Created.");
 	}
 	
 	private void initDatabase()
 	{
 		DataBase.update("INSERT INTO player_accounts (id, morgul_coins) VALUES('" + id + "', '" + START_COINS + "')");
 		DataBase.update("INSERT INTO player_stats VALUES('" + id + "', 'mining', '" + START_XP + "', '" + START_LVL + "')");
 		DataBase.update("INSERT INTO player_stats VALUES('" + id + "', 'harvesting', '" + START_XP + "', '" + START_LVL + "')");
 		DataBase.update("INSERT INTO player_stats VALUES('" + id + "', 'digging', '" + START_XP + "', '" + START_LVL + "')");
 		DataBase.update("INSERT INTO overall_stats VALUES('" + id + "', '0')");
 	}
 	
 	private void commitStats()
 	{
 		List<Stat> st = Utils.mapToList(stats);
 		
 		for(Stat s : st)
 		{
 			MorgulPlugin.debug("commiting " + s.getName());
 			DataBase.update("UPDATE player_stats SET stat='" + s.getName() + "', xp='" + s.getXP() + "', lvl='" + s.getLevel() + "' WHERE id='" + id + "' AND stat='" + s.getName() + "'");
 		}
 		
 		MorgulPlugin.debug("commiting minedBlocks");
 		DataBase.update("UPDATE overall_stats SET mined_blocks='" + minedBlocks + "' WHERE id='" + id + "'");
 	}
 	
 	public void updateAccount()
 	{
 		DataBase.update("UPDATE player_accounts SET morgul_coins='" + m_coins + "' WHERE id='" + id + "'");
 	}
 	
 	public void disconnected()
 	{
		DataBase.update("UPDATE players SET location='" + Utils.getLocAsString(getPlayer().getLocation()) + "', lastplayed='" + System.currentTimeMillis() + "', lang='" + language + "' WHERE id='" + id + "'");
		commitStats();
 	}
 	
 	
 	/**
 	 * @return the logged
 	 */
 	public boolean isLogged()
 	{
 		return logged;
 	}
 	/**
 	 * @param logged the logged to set
 	 */
 	public void setLogged(boolean logged)
 	{
 		this.logged = logged;
 	}
 	/**
 	 * @return the id
 	 */
 	public int getId()
 	{
 		return id;
 	}
 	
 	public void addMinedBlocks(int count)
 	{
 		this.minedBlocks += count;
 	}
 	
 	public long getMinedBlocks()
 	{
 		return minedBlocks;
 	}
 	
 	public void setMinedBlocks(long minedBlocks)
 	{
 		this.minedBlocks = minedBlocks;
 	}
 	
 	public Player getPlayer()
 	{
 		return pl;
 	}
 
 	public Stat getStatByName(String name)
 	{
 		return stats.get(name);
 	}
 	
 	public void setStat(String name, Stat stat)
 	{
 		stats.put(name, stat);
 	}
 	
 	public long getLastPlayed()
 	{
 		return lastPlayed;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public int getM_coins()
 	{
 		return m_coins;
 	}
 
 	public void setM_coins(int m_coins)
 	{
 		this.m_coins = m_coins;
 		updateAccount();
 	}
 	
 	public void addM_coins(int count)
 	{
 		this.m_coins += count;
 		updateAccount();
 	}
 	
 	public void removeM_coins(int count)
 	{
 		this.m_coins -= count;
 		updateAccount();
 	}
 
 	public ArrayList<ChatChannel> getChatChannels()
 	{
 		return chatChannels;
 	}
 
 	public void joinChannel(ChatChannel e)
 	{
 		e.connect(this);
 	}
 	
 	public void addChannel(ChatChannel e)
 	{
		if(chatSt.getChannel().equalsIgnoreCase(e.getName()))
			chatSt.setChannel("All");
 		chatChannels.add(e);
 	}
 	
 	public void disconnectChannel(ChatChannel e)
 	{
 		e.disconnect(this);
 	}
 	
 	public void removeChannel(ChatChannel e)
 	{
 		chatChannels.remove(e);
 	}
 
 	public SpoutPlayer getSpoutPlayer()
 	{
 		return spl;
 	}
 
 	public void setSpoutPlayer(SpoutPlayer spl)
 	{
 		this.spl = spl;
 	}
 
 	public String getLanguage()
 	{
 		return language;
 	}
 
 	public void setLanguage(String language)
 	{
 		this.language = language.toLowerCase();
 	}
 }
