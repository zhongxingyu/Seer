 package cz.opt.morgulplugin.entity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import cz.opt.morgulplugin.MorgulPlugin;
 import cz.opt.morgulplugin.config.Config;
 import cz.opt.morgulplugin.database.DataBase;
 import cz.opt.morgulplugin.managers.ChatManager;
 import cz.opt.morgulplugin.structs.ChatChannel;
 import cz.opt.morgulplugin.structs.Stat;
 import cz.opt.morgulplugin.utils.Utils;
 
 public class MorgPlayer
 {
 	private static final String SECTION = "Player";
 	private static int START_COINS;
 	private static int START_XP;
 	private static int START_LVL;
 	private int id;
 	private String name;
 	private Player pl;
 	private Hashtable<String, Stat> stats;
 	private ArrayList<ChatChannel> chatChannels;
 	private boolean logged;
 	private Location logLoc;
 	private long lastPlayed;
 	private int m_coins;
 	
 	public static void init()
 	{
 		START_COINS = Integer.parseInt(Config.get(SECTION, "Starting_Coins"));
 		START_XP = Integer.parseInt(Config.get(SECTION, "Starting_Xp"));
 		START_LVL = Integer.parseInt(Config.get(SECTION, "Starting_Lvl"));
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
 		rs = DataBase.query("SELECT * FROM player_accounts WHERE id='" + id + "'");
 		m_coins = Integer.parseInt(rs.get(1).get("morgul_coins"));
 		
 		// load stats
 		rs = DataBase.query("SELECT * FROM player_stats WHERE id='" + id + "'");
 		String statName = rs.get(1).get("stat");
 		int xp = Integer.parseInt(rs.get(1).get("xp"));
 		int lvl = Integer.parseInt(rs.get(1).get("lvl"));
 		Stat stat = new Stat(xp, lvl);
 		stats.put(statName, stat);
 	}
 	
 	private void newPlayer()
 	{
 		DataBase.update("INSERT INTO players (playername, location) VALUES('" + this.name + "', '" + Utils.getLocAsString(getPlayer().getLocation()) + "')");
 		HashMap<Integer, HashMap<String, String>> rs = DataBase.query("SELECT * FROM players WHERE playername='" + name + "'");
 		stats.put("mining", new Stat(0, 1));
 		if(rs.keySet().size() < 1)
 			return;
 		id = Integer.parseInt(rs.get(1).get("id"));
 		logLoc = Utils.getLocFromString(rs.get(1).get("location"), pl.getWorld());
 		lastPlayed = Long.parseLong(rs.get(1).get("lastplayed"));
 		DataBase.update("INSERT INTO player_accounts (id, morgul_coins) VALUES('" + id + "', '" + START_COINS + "')");
 		DataBase.update("INSERT INTO player_stats VALUES('" + id + "', 'mining', '" + START_XP + "', '" + START_LVL + "')");
 		setM_coins(START_COINS);
 		MorgulPlugin.debug("New Player Created.");
 	}
 	
 	private void commitStats()
 	{
 		Set<String> keys = stats.keySet();
 		
 		for(int i = 0; i < keys.size(); i++)
 		{
 			String key = keys.iterator().next();
 			Stat stat = stats.get(key);
 			DataBase.update("UPDATE player_stats SET stat='" + key + "', xp='" + stat.getXP() + "', lvl='" + stat.getLevel() + "' WHERE id='" + id + "'");
 		}
 
 	}
 	
 	public void updateAccount()
 	{
 		DataBase.update("UPDATE player_accounts SET morgul_coins='" + m_coins + "' WHERE id='" + id + "'");
 	}
 	
 	public void disconnected()
 	{
 		DataBase.update("UPDATE players SET location='" + Utils.getLocAsString(getPlayer().getLocation()) + "', lastplayed='" + System.currentTimeMillis() + "' WHERE id='" + id + "'");
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
 	
 	public void removeChannel(ChatChannel e)
 	{
		e.disconnect(this);
 	}
 }
