 package com.dwarfholm.activitystats.braizhauler;
 
 import org.bukkit.configuration.Configuration;
 
 public class ASConfig {
 	private ActivityStats plugin;
 	private Configuration config;
 	
 	public String localeFileName;
 	
 	public int iInterval, iQuota, iBreakMax, iBreakMult, iPlaceMax, iPlaceMult, iTravelMax, iTravelMult, iChatMax, iChatMult,
 			iAnimalMax, iAnimalMult, iMonsterMax, iMonsterMult, iPlayerMax, iPlayerMult;
 	public boolean useMySQL;
 	public String sqlHostname, sqlPort, sqlUsername, sqlPassword, sqlDatabase, sqlPrefix, sqlURI;
 	
 	public String ecoMode;
 	public double ecoMin, ecoMax;
 	
 	
 	public int prmWatchedRanksCount;
 	public String[] prmWatchedRanks;
 	public int[] prmMinutePlayed;
 	public String[] prmRanksTo;
 	
 	public ASConfig(ActivityStats plugin)	{
 		this.plugin = plugin;
 	}
 	public void onEnable() {
 		reloadConfig();
 	}
 
 	public void reloadConfig()	{
 		plugin.reloadConfig();
 		config = plugin.getConfig().getRoot();
 		loadConfig();
 	}
 	
 	public void loadConfig()	{
 		localeFileName = config.getString("locale.filename");
 		
 		iInterval = config.getInt("interval");
 		iQuota = config.getInt("quota");
 		iBreakMax = config.getInt("block-break.max");
 		iBreakMult = config.getInt("block-break.multiplier");
		iPlaceMax = config.getInt("block-plack.max");
 		iPlaceMult = config.getInt("block-place.multiplier");
 		iTravelMax = config.getInt("blocks-traveled.max");
 		iTravelMult = config.getInt("blocks-traveled.multiplier");
 		iChatMax = config.getInt("chat-commands.max");
 		iChatMult = config.getInt("chat-commands.multiplier");
 		iAnimalMax = config.getInt("damage-animal.max");
 		iAnimalMult = config.getInt("damage-animal.multiplier");
 		iMonsterMax = config.getInt("damage-monster.max");
 		iMonsterMult = config.getInt("damage-monster.multiplier");
 		iPlayerMax = config.getInt("damage-player.max");
 		iPlayerMult = config.getInt("damager-player.multiplier");
 		
 		useMySQL = config.getBoolean("database.use-mysql");
 
 		sqlHostname = config.getString("database.mysql.hostname");
 		sqlPort = config.getString("database.mysql.port");
 		sqlUsername = config.getString("database.mysql.username");
 		sqlPassword = config.getString("database.mysql.password");
 		sqlDatabase = config.getString("database.mysql.database");
 		sqlPrefix = config.getString("database.mysql.tableprefix");
 		sqlURI = "jdbc:mysql://" + sqlHostname + ":" + sqlPort  + "/" + sqlDatabase;
 		
 		ecoMode = config.getString("economy.mode");
 		ecoMin = config.getDouble("economy.min");
 		ecoMax = config.getDouble("economy.max");
 		
 		loadPromoterRanks();
 	}
 	
 	private void loadPromoterRanks()	{
 		prmWatchedRanks = parseRankList(config.getString("autopromote.ranks-promoted"));
 		prmWatchedRanksCount = prmWatchedRanks.length;
 		prmMinutePlayed = new int[prmWatchedRanksCount];
 		prmRanksTo = new String[prmWatchedRanksCount];
 		for( int count = 0; count < prmWatchedRanksCount; count++)	{
 			prmMinutePlayed[count] = config.getInt("autopromote." + prmWatchedRanks[count] + ".minutes-played");     
 			prmRanksTo[count] = config.getString("autopromote." + prmWatchedRanks[count] + ".to-rank");
 		}
 	}
 	
 	private String[] parseRankList(String list)	{
 		return list.split(",\\s+");
 	}
 	
 	public boolean promoterWatchingRank(String rankName)	{
 		boolean watched = false;
 		for ( int count = 0; count < prmWatchedRanksCount; count++)	{
 			if (prmWatchedRanks[count].equalsIgnoreCase(rankName))
 				 watched = true;
 		}
 		return watched;
 	}
 	
 	public int promoterPoints(String rankName)	{
 		int minutes = 0, points = 0;
 		for ( int count = 0; count < prmWatchedRanksCount; count++)	{
 			if (prmWatchedRanks[count].equalsIgnoreCase(rankName))
 				 minutes = prmMinutePlayed[count];
 		}
 		points = (iQuota * minutes) / iInterval;
 		return points;
 	}
 	
 	public String promoterRankTo(String rankName)	{
 		String newRank = null;
 		for ( int count = 0; count < prmWatchedRanksCount; count++)	{
 			if (prmWatchedRanks[count].equalsIgnoreCase(rankName))
 				 newRank = prmRanksTo[count];
 		}
 		return newRank;
 	}
 	
 	public boolean ecoModePercent()	{	return ecoMode.equalsIgnoreCase("PERCENT");	}
 	public boolean ecoModeBoolean()	{	return ecoMode.equalsIgnoreCase("BOOLEAN");	}
 }
