 package me.sinnoh.MasterPromoteKills;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.sinnoh.MasterPromote.MasterPromote;
 import me.sinnoh.MasterPromote.Api.MPPlugin;
 import me.sinnoh.MasterPromoteKills.Metrics.Metrics;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MasterPromoteKills extends JavaPlugin implements MPPlugin
 {
 	
 	public static MasterPromoteKills instance;
 	private List<sPlayer> players;
 	private MasterPromote mainpl;
 	
 	public void onDisable()
 	{
 		sUtil.saveMap();
 	}
 	
 	
 	public void onEnable()
 	{
 		instance = this;
 		this.players = sUtil.loadMap();
 		loadConfig();
 		this.mainpl = (MasterPromote) Bukkit.getPluginManager().getPlugin("MasterPromote");
 		this.mainpl.registerMPPlugin(this);
 		getServer().getPluginManager().registerEvents(new MPListener(), this);
 		setupMetrics();
 	}
 	
 	
 	public void loadConfig()
 	{
 		getConfig().options().copyDefaults(true);
 		getConfig().addDefault("EnableRankedKills", true);
 		List<String> ranks = new ArrayList<String>();
 		ranks.add("Murderer,25");
 		getConfig().addDefault("KillRanks", ranks);
 		getConfig().addDefault("EnableRankedDeaths", false);
 		ranks = new ArrayList<String>();
 		ranks.add("Victim,25");
 		getConfig().addDefault("DeathRanks", ranks);
 		getConfig().addDefault("CountPVEKills", false);
 		getConfig().addDefault("CountPVEDeaths", false);
 		saveConfig();
 	}
 	
 	public sPlayer getsPlayer(Player player)
 	{
 		for(sPlayer sp : this.players)
 		{
 			if(sp.getName().equals(player.getName()))
 			{
 				return sp;
 			}
 		}
 		sPlayer sp = new sPlayer(player);
 		this.players.add(sp);
 		return sp;
 	}
 	
 	public List<sPlayer> getsPlayers()
 	{
 		return this.players;
 	}
 	
 	public Boolean setupMetrics()
 	{
 		try
 		{
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 			sUtil.log("PluginMetrics enabled!");
 			return true;
 		}catch(Exception e)
 		{
 			sUtil.log("Failed to enable PluginMetrics");
 			return false;
 		}
 	}
 
 
 	@Override
 	public Boolean reload() 
 	{
 		try
 		{
 			reloadConfig();
 			System.out.println("[MasterPromoteKills] reloaded!");
 			return true;
 		}catch(Exception e)
 		{
 			return false;
 		}
 	}
 
 
 	@Override
 	public void save()
 	{
 		synchronized (this.players) 
 		{
 			sUtil.saveMap();
 		}
 	}
 	
 	public MasterPromote getMasterPromote()
 	{
 		return this.mainpl;
 	}
 	
 }
