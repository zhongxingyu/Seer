 package com.dwarfholm.activitystats.braizhauler;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ActivityStats extends JavaPlugin {
 	private Vault vault;
 	private ASLocale locale;
 	private Logger log;
 	private ASData players;
 	private ASListener listener;
 	private ASConfig config;
 	
 	private FileConfiguration rolloverData = null;
 	private File rolloverDataFile = null;
 	
 	private Date lastPeriodRollover;
 	private Date lastDayRollover;
 	private Date lastWeekRollover;
 	private Date lastMonthRollover;
 	
 	private final long SHORT_PADDING = 500;
 	private final long MILLIS_PER_MINUTE = 60000;
 	private final long FULL_MILLIS_PER_DAY = 86400000;
 	private final long MILLIS_PER_DAY = FULL_MILLIS_PER_DAY - SHORT_PADDING;
 	private final long MILLIS_PER_WEEK = 7 * FULL_MILLIS_PER_DAY - SHORT_PADDING;
 	private final long MILLIS_PER_MONTH = 30 * FULL_MILLIS_PER_DAY - SHORT_PADDING;
 	
 	private ASTravelTimer travelTimer;
 	private ASPaymentTimer payTimer;
 	
 	private final String ROLLOVER_FILE_NAME = "data.yml";
 	private final String LOG_NAME = "ActivityStats";
 	private final String CONSOLE = "console";
 
 	
 	public void onLoad()	{
 		log = Logger.getLogger(LOG_NAME);
 		
 		config = new ASConfig(this);
 		config.reloadConfig();
 		rolloverDataFile = new File(getDataFolder(), ROLLOVER_FILE_NAME);
 		
 		locale = new ASLocale(this);
 		vault = new Vault(this);
 		
 		
 		players = new ASData(this);
 		ASLongData.setConfig(config);
 		
 		listener = new ASListener(this);
 		
 		travelTimer = new ASTravelTimer(this);
 		payTimer = new ASPaymentTimer(this);
 		
 		
 	}
 	
 	public void onEnable() {
 		config.onEnable();
 		reloadRolloverData();
 		players.createDatabase();
 		players.loadOnlinePlayers();
 		
 		locale.onEnable();
 		vault.connect();
 		
 		getCommand("activitystats").setExecutor(new ASCmdActivityStats(this));
 		getCommand("activity").setExecutor(new ASCmdActivity(this));
 		getCommand("nextpayday").setExecutor(new ASCmdNextPayDay(this));
 		
 		listener.register();
 		travelTimer.start();
 		payTimer.start();
 
 		info("ActivityStats Enabled");
 	}
 	
 	public void onDisable() {
 		listener.unregister();
 		getCommand("activitystats").setExecutor(null);
 		getCommand("activity").setExecutor(null);
 		getCommand("nextpayday").setExecutor(null);
 
 		payTimer.stop();
 		travelTimer.stop();
 		
 		vault.disconnect();
 	}
 	
 	
 	
 	
 	
 	public void loadPlayer(String name)	{	players.loadPlayer(name);	}
 	public void savePlayer(String name)	{	players.savePlayer(name);	}
 	public ASData getASPlayersList()	{	return players;	}
 	public ASPlayer getASPlayer(String name)	{
 		return players.getPlayer(name);
 	}
 	
 	public ASLocale getLocalization()	{
 		return locale;
 	}
 
 	public Economy econ()	{	return vault.econ;	}
 	public Permission perms()	{	return vault.perms;	}
 	
 	
 	public void msg(String sSender, String msg)	{
 		CommandSender csSender;
 		if (sSender.equalsIgnoreCase(CONSOLE))	{
 			csSender = (CommandSender)getServer().getConsoleSender();
 		} else	{
 			csSender = getServer().getPlayer(sSender);
 		}
 		msg (csSender, msg);
 	}
 	
 	public ASConfig config()	{
 		return config;
 	}
 
 	public boolean PeriodRolloverDue()	{
 		return ( System.currentTimeMillis() - lastPeriodRollover.getTime() ) >= config.iInterval * MILLIS_PER_MINUTE - SHORT_PADDING;
 	}
 	public boolean DayRolloverDue()	{
 		return ( System.currentTimeMillis() - lastDayRollover.getTime() ) >= MILLIS_PER_DAY;
 	}
 	public boolean WeekRolloverDue()	{
 		return ( System.currentTimeMillis() - lastWeekRollover.getTime() ) >= MILLIS_PER_WEEK;
 	}
 	public boolean MonthRolloverDue()	{
 		return ( System.currentTimeMillis() - lastMonthRollover.getTime() ) >= MILLIS_PER_MONTH;
 	}
 	
 	public Date getLastPeriodRollover()	{	return lastPeriodRollover;	}
 	public Date getLastDayRollover()	{	return lastDayRollover;	}
 	public Date getLastWeekRollover()	{	return lastWeekRollover;	}
 	public Date getLastMonthRollover()	{	return lastMonthRollover;	}
 	
 	public void rolledoverPeriod()	{
 		lastPeriodRollover.setTime(System.currentTimeMillis());
 		saveRollovers();
 	}
 	public void rolledoverDay()	{
 		lastDayRollover.setTime(System.currentTimeMillis());
 		saveRollovers();
 	}
 	public void rolledoverWeek()	{
 		lastWeekRollover.setTime(System.currentTimeMillis());
 		saveRollovers();
 	}
 	public void rolledoverMonth()	{
 		lastMonthRollover.setTime(System.currentTimeMillis());
 		saveRollovers();
 	}
 	
     public FileConfiguration getRolloverData() {
           if (rolloverData == null) {
               reloadRolloverData();
           }
           return rolloverData;
       }
 
     public void reloadRolloverData() {
     	rolloverData = YamlConfiguration.loadConfiguration(rolloverDataFile);
 
     	InputStream defaultDataStream = getResource(ROLLOVER_FILE_NAME);
     	if (defaultDataStream != null) {
     		YamlConfiguration defaultData = YamlConfiguration.loadConfiguration(defaultDataStream);
 
     		rolloverData.setDefaults(defaultData);
     	}
     	loadRolloverData();
     }
     
     public void loadRolloverData()	{
     	lastPeriodRollover = stringToDate(rolloverData.getString("lastrollover.period"));
     	lastDayRollover = stringToDate(rolloverData.getString("lastrollover.day"));
     	lastWeekRollover = stringToDate(rolloverData.getString("lastrollover.week"));
     	lastMonthRollover = stringToDate(rolloverData.getString("lastrollover.month"));
     }
     
 	private void saveRollovers()	{
 		rolloverData.set("lastrollover.period", dateToString(lastPeriodRollover));
 		rolloverData.set("lastrollover.day", dateToString(lastDayRollover));
 		rolloverData.set("lastrollover.week", dateToString(lastWeekRollover));
 		rolloverData.set("lastrollover.month", dateToString(lastMonthRollover));
 
 		saveRolloverData();
 	}

 	public void saveRolloverData() {
 	    if (rolloverData == null || rolloverDataFile == null) {
 	    	return;
 	    }
 	    try {
	        rolloverData.save(rolloverDataFile);
 	    } catch (IOException ex) {
 	        severe("Could not save config to " + rolloverDataFile);
 	    }
 	}
     	

	public int timeToPay()	{
		return (int)(( lastPeriodRollover.getTime()-System.currentTimeMillis() ) / MILLIS_PER_MINUTE + config.iInterval );
	}
	
     public void saveDefaultConfig() {
     	if (!rolloverDataFile.exists()) {
     		saveResource(ROLLOVER_FILE_NAME, false);
     	}
     }
 	
 
 	
 	private String dateToString(Date time)	{
 		return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(time);
 	}
 	
 	private Date stringToDate(String date)	{
 		Date time = null;
 		try {
 			time = DateFormat.getDateInstance().parse(date);
 		} catch (ParseException e) {
 			severe("Date Parse Error");
 		}
 		return time;
 	}
 	
 	public void payPlayer(ASPlayer player)	{
 		double amount;
 		double percent = getActivityPercent(player);
 		if (config.ecoModePercent())	{
 			amount = getPercentPayment(player);
 		} else if (config.ecoModeBoolean())	{
 			amount = getBooleanPayment(player);
 		} else	{ 
 			return;
 		}
 		msg(getServer().getPlayer(player.getName()), locale.getPaymentMessage(amount, percent));
 		vault.econ.depositPlayer(player.getName() , amount);
 	}
 	
 	public double getPercentPayment(ASPlayer player)	{
 		return config.ecoMin + ( config.ecoMax - config.ecoMin ) * getActivityPercent(player) ;
 	}
 	
 	public double getActivityPercent (ASPlayer player)	{
 		info("Player = " + player.getName() + " " + String.valueOf(player.getActivity()) + "/" + String.valueOf(config.iQuota));
 		return Math.max(0.0, Math.min( 1.0, ((double)player.getActivity()) / ((double)config.iQuota)));
 	}
 	public double getBooleanPayment(ASPlayer player)	{
 		if((double)player.curPeriod.getActivity() > config.iQuota)
 			return config.ecoMax;
 		else
 			return config.ecoMin;
 	}
 	
 	public void msg(CommandSender sender, String msg)	{	sender.sendMessage(msg);	}
 	
 	public void severe(String msg)	{	log.severe("["+LOG_NAME+"]"+msg);	}
 	public void warning(String msg)	{	log.warning("["+LOG_NAME+"]"+msg);	}
 	public void info(String msg)	{	log.info("["+LOG_NAME+"]"+msg);		}
 	public void fine(String msg)	{	log.fine("["+LOG_NAME+"]"+msg);		}
 	public void finer(String msg)	{	log.finer("["+LOG_NAME+"]"+msg);		}
 	public void finest(String msg)	{	log.finest("["+LOG_NAME+"]"+msg);	}
 
 	public void autoPromoterCheck(Player player)	{
 		String curRank = perms().getPrimaryGroup(player);
 		if ( config.promoterWatchingRank(curRank) )
 			if ( getASPlayer(player.getName()).total.getActivity() > config.promoterPoints(curRank) )
 				if ( perms().playerAddGroup(player, config.promoterRankTo(curRank)) )
 						info("AutoPromoted " + player.getName());
 		
 	}
 	
 	
 }
