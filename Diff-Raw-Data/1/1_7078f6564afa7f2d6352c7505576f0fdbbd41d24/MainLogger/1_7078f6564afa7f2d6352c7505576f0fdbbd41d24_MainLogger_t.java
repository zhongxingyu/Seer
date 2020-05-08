 package org.ultralogger;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.ultralogger.logger.*;
 import org.ultralogger.more.HistoryManager;
 import org.ultralogger.more.RollbackCommandExecutor;
 import org.ultralogger.more.Translater;
 import org.ultralogger.sql.SQL;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 public class MainLogger extends JavaPlugin{
 	public static MainLogger plugin;
 	private static final Logger log = Logger.getLogger("Minecraft");
 	public static final String pref = "[UltraLogger] v1.6.5 ";
 	
 	public Translater t;
 	public static final File lang = new File("./Log/lang.yml");
 
 	public static final File config= new File("./Log/config.yml");
     private boolean UpdateCheck=true;
 	private boolean player =true;
     private boolean chat=true;
     private boolean command=true;
     private boolean plugins =true;
     private boolean vehicle =true;
     private boolean block =true;
     private boolean craft =true;
     private boolean enchantment =true;
     private boolean entity =true;
     private boolean world =true;
     private boolean weather =true;
     private boolean inventory =true;
 
     private String sql_ip ="blank";
     private String sql_port ="3306";
     private String sql_user ="blank";
     private String sql_pass ="blank";
     private String sql_db = "blank";
     private String sql_table_prefix ="UL(v1.6)";
 	
 	private boolean append = true;
 	private int lines = 0;
 	private EnumPeriod period = EnumPeriod.DAY;
 	private Date date ;
 	
 	private PlayerLogger playerL;
 	private ChatLogger chatL;
 	private CommandLogger commandL;
 	private VehicleLogger vehicleL;
 	private CraftLogger craftL;
 	private EnchantmentLogger enchantmentL;
 	private BlockLogger blockL;
 	private WeatherLogger weatherL;
 	private WorldLogger worldL;
 	private EntityLogger entityL;
 	private PluginLogger pluginL;
 	private InventoryLogger inventoryL;
 	
 	private int block_hist_itemID =280;
 	private boolean block_history = false;
 	private HistoryManager hist_manager;
 	
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("/undo")&&args.length<3&&sender instanceof Player){ // If the player typed //undo then do the following...
 			Location loc =((Player) sender).getLocation();
 			loc.add(0, -2, 0);
 			RollbackCommandExecutor.undo((Player)(sender), loc);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("/undo")&&args.length==3&&sender instanceof Player){
 			int x = Integer.parseInt(args[0]);
 			int y = Integer.parseInt(args[1]);
 			int z = Integer.parseInt(args[2]);
 			Location loc = new Location(((Player) sender).getLocation().getWorld(),x,y,z);
 			RollbackCommandExecutor.undo((Player)(sender), loc);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("/undo")&&args.length==6&&sender instanceof Player){
 			int x = Integer.parseInt(args[0]);
 			int y = Integer.parseInt(args[1]);
 			int z = Integer.parseInt(args[2]);
 			Location loc = new Location(((Player) sender).getLocation().getWorld(),x,y,z);
 			int x1 = Integer.parseInt(args[3]);
 			int y1 = Integer.parseInt(args[4]);
 			int z1 = Integer.parseInt(args[5]);
 			Location loc1 = new Location(((Player) sender).getLocation().getWorld(),x1,y1,z1);
 			RollbackCommandExecutor.undo((Player)(sender), loc,loc1);
 			return true;
 		}
 		if(cmd.getName().equalsIgnoreCase("/redo")&&args.length<3&&sender instanceof Player){ // If the player typed //undo then do the following...
 			Location loc =((Player) sender).getLocation();
 			loc.add(0, -2, 0);
 			RollbackCommandExecutor.redo((Player)(sender), loc);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("/redo")&&args.length==3&&sender instanceof Player){
 			int x = Integer.parseInt(args[0]);
 			int y = Integer.parseInt(args[1]);
 			int z = Integer.parseInt(args[2]);
 			Location loc = new Location(((Player) sender).getLocation().getWorld(),x,y,z);
 			RollbackCommandExecutor.redo((Player)(sender), loc);
 			return true;
 		}
 		else if(cmd.getName().equalsIgnoreCase("/redo")&&args.length==6&&sender instanceof Player){
 			int x = Integer.parseInt(args[0]);
 			int y = Integer.parseInt(args[1]);
 			int z = Integer.parseInt(args[2]);
 			Location loc = new Location(((Player) sender).getLocation().getWorld(),x,y,z);
 			int x1 = Integer.parseInt(args[3]);
 			int y1 = Integer.parseInt(args[4]);
 			int z1 = Integer.parseInt(args[5]);
 			Location loc1 = new Location(((Player) sender).getLocation().getWorld(),x1,y1,z1);
 			RollbackCommandExecutor.redo((Player)(sender), loc,loc1);
 			return true;
 		}
 		//If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
 		return false; 
 	}
 	
 	public void onDisable() {
 		disable();
 		log.info(pref+"has been disabled");
 	}
 
 	
 	@SuppressWarnings("deprecation")
 	public void onEnable() {
 		plugin=this;
 		t=new Translater(lang);
 		loadConfig();
         if(UpdateCheck){
         	checkUpdates();
         }
 		if(period!=EnumPeriod.NEVER){
 			int days = Calendar.WEEK_OF_MONTH*7;
 			switch(period){
 			case DAY:
 				//check if the day or the week or the month is different
 				enableFileLoggers(Calendar.DAY_OF_MONTH!=date.getDay()||date.getDay()>=days-7&&date.getDay()<=days||Calendar.MONTH!=date.getMonth());
 				break;
 			case WEEK:
 				//check if the week or if the month is different
 				enableFileLoggers(date.getDay()>=days-7&&date.getDay()<=days||Calendar.MONTH!=date.getMonth());
 				break;
 			case MONTH:
 				//check if the month is different
 				enableFileLoggers(Calendar.MONTH!=date.getMonth());
 				break;
 			}
 		}
 		else{
 			enableFileLoggers(false);
 		}
 		sql_ip=sql_ip.trim();
         if(sql_ip.length()>0&&sql_ip!="blank"&&!sql_ip.equalsIgnoreCase("blank")&&sql_ip!="null"&&!sql_ip.equalsIgnoreCase("null")){
             SQL sql = new SQL(this,sql_ip,sql_port,sql_user,sql_pass,sql_db,sql_table_prefix);
 			if(block)
 				new org.ultralogger.sql.logger.BlockLogger(sql);
 			if(chat)
 				new org.ultralogger.sql.logger.ChatLogger(sql);
 			if(command)
 				new org.ultralogger.sql.logger.CommandLogger(sql);
 			if(vehicle)
 				new org.ultralogger.sql.logger.VehicleLogger(sql);
 			if(craft)
 				new org.ultralogger.sql.logger.CraftLogger(sql);
 			if(enchantment)
 				new org.ultralogger.sql.logger.EnchantmentLogger(sql);
 			if(weather)
 				new org.ultralogger.sql.logger.WeatherLogger(sql);
 			if(world)
 				new org.ultralogger.sql.logger.WorldLogger(sql);
 			if(player)
 				new org.ultralogger.sql.logger.PlayerLogger(sql);
 			if(entity)
 				new org.ultralogger.sql.logger.EntityLogger(sql);
 			if(inventory)
 				new org.ultralogger.sql.logger.InventoryLogger(sql);
 			if(plugins)
 				new org.ultralogger.sql.logger.PluginLogger(sql);
 		}
 		log.info(pref+"has been enabled");
 	}
 	
 	public void enableFileLoggers(boolean yes){
 		if(yes){
 			date = new Date(System.currentTimeMillis());
 			saveConfiguration();
 			if(chat)
 				chatL =new ChatLogger(this,append,lines,true);
 			if(command)
 				commandL = new CommandLogger(this,append,lines,true);
 			if(player)
 				playerL = new PlayerLogger(this,append,lines,true);
 			if(vehicle)
 				vehicleL = new VehicleLogger(this,append,lines,true);
 			if(craft)
 				craftL = new CraftLogger(this,append,lines,true);
 			if(enchantment)
 				enchantmentL = new EnchantmentLogger(this,append,lines,true);
 			if(block)
 				blockL=new BlockLogger(this,append,lines,true);
 			if(weather)
 				weatherL = new WeatherLogger(this,append,lines,true);
 			if(world)
 				worldL = new WorldLogger(this,append,lines,true);
 			if(entity)
 				entityL = new EntityLogger(this,append,lines,true);
 			if(plugins)
 				pluginL = new PluginLogger(this,append,lines,true);
 			if(inventory)
 				inventoryL = new InventoryLogger(this,append,lines,true);
 		}
 		else{
 			if(chat)
 				chatL =new ChatLogger(this,append,lines,date);
 			if(command)
 				commandL = new CommandLogger(this,append,lines,date);
 			if(player)
 				playerL = new PlayerLogger(this,append,lines,date);
 			if(vehicle)
 				vehicleL = new VehicleLogger(this,append,lines,date);
 			if(craft)
 				craftL = new CraftLogger(this,append,lines,date);
 			if(enchantment)
 				enchantmentL = new EnchantmentLogger(this,append,lines,date);
 			if(block)
 				blockL=new BlockLogger(this,append,lines,date);
 			if(weather)
 				weatherL = new WeatherLogger(this,append,lines,date);
 			if(world)
 				worldL = new WorldLogger(this,append,lines,date);
 			if(entity)
 				entityL = new EntityLogger(this,append,lines,date);
 			if(plugins)
 				pluginL = new PluginLogger(this,append,lines,date);
 			if(inventory)
 				inventoryL = new InventoryLogger(this,append,lines,date);
 		}
 		if(block_history)
 			hist_manager =new HistoryManager(this, block_hist_itemID);
 	}
 	
 	/**
 	 * Load the configuration
 	 */
 	public void loadConfig(){
 		if(!config.exists()){
 			new File("./Log").mkdir();
 			try {
 				if(config.createNewFile()){
 					log.info(pref+t.translate("config.created"));
 				}
 				else{
 					log.info(pref+t.translate("config.failed"));
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			date = new Date(0);
 			saveConfiguration();
 		}
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileReader(config));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
         UpdateCheck=Boolean.parseBoolean(prop.getProperty("Check for updates","true"));;
 		player=Boolean.parseBoolean(prop.getProperty("Player-logger","true"));
 		chat=Boolean.parseBoolean(prop.getProperty("Chat-logger","true"));
 		command=Boolean.parseBoolean(prop.getProperty("Command-logger","true"));
 		plugins=Boolean.parseBoolean(prop.getProperty("Plugin-logger","true"));
 		vehicle=Boolean.parseBoolean(prop.getProperty("Vehicle-logger","true"));
 		block=Boolean.parseBoolean(prop.getProperty("Block-logger","true"));
 		craft=Boolean.parseBoolean(prop.getProperty("Craft-logger","true"));
 		enchantment=Boolean.parseBoolean(prop.getProperty("Enchantment-logger","true"));
 		entity=Boolean.parseBoolean(prop.getProperty("Entity-logger","true"));
 		world=Boolean.parseBoolean(prop.getProperty("World-logger","true"));
 		weather=Boolean.parseBoolean(prop.getProperty("Weather-logger","true"));
 		inventory=Boolean.parseBoolean(prop.getProperty("Inventory-logger","true"));
 		append=Boolean.parseBoolean(prop.getProperty("Append","true"));
 		lines=Integer.parseInt( prop.getProperty("Max-Lines","0"));
 		period = EnumPeriod.fromString(prop.getProperty("NewFileEvery","DAY"));
 		date = new Date(Long.parseLong(prop.getProperty("LastDELtime","0")));
 		sql_ip=prop.getProperty("SQL-host", "blank");
         sql_port=prop.getProperty("SQL-port","3306");
         sql_user=prop.getProperty("SQL-name", "blank");
         sql_pass=prop.getProperty("SQL-pass", "blank");
         sql_db=prop.getProperty("SQL-database","ultralogger");
         sql_table_prefix=prop.getProperty("SQL-table-prefix","UL(v1.6)");
 		block_history = Boolean.parseBoolean(prop.getProperty("InGame-Block-History", "false"));
 		block_hist_itemID = Integer.parseInt(prop.getProperty("ItemID-toSee", "280"));
 		prop.clear();
 		prop=null;
 		saveConfiguration();
 	}
 
 
 	/**Save the configuration
 	 * 
 	 */
 	public void saveConfiguration() {
 		try {
 			PrintWriter out =new PrintWriter(config);
             out.println("Check for updates="+UpdateCheck);
 			out.println("InGame-Block-History="+block_history);
 			out.println("ItemID-toSee="+block_hist_itemID);
 			out.println("Block-logger="+block);
 			out.println("Chat-logger="+chat);
 			out.println("Command-logger="+command);
 			out.println("Craft-logger="+craft);
 			out.println("Enchantment-logger="+enchantment);
 			out.println("Entity-logger="+entity);
 			out.println("Inventory-logger="+inventory);
 			out.println("Player-logger="+player);
 			out.println("Plugin-logger="+plugins);
 			out.println("Vehicle-logger="+vehicle);
 			out.println("Weather-logger="+weather);
 			out.println("World-logger="+world);
 			out.println("Append="+append);
 			out.println("Max-Lines="+lines);
 			out.println("NewFileEvery="+period.toString());
 			out.println("SQL-host="+sql_ip);
 	        out.println("SQL-port="+sql_port);
 	        out.println("SQL-name="+sql_user);
 	        out.println("SQL-pass="+sql_pass);
 	        out.println("SQL-database="+sql_db);
 	        out.println("SQL-table-prefix="+sql_table_prefix);
 			out.println("#Do NOT modify anything under this line.");
 			if(date.compareTo(new Date(0))==0){
 				date =new Date(0);
 				out.println("LastDELtime="+0);
 			}
 			else{
 				out.println("LastDELtime="+date.getTime());
 			}
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * Show in the console log the latest build available
 	 */
 	public void checkUpdates(){
 		String url = "http://dev.bukkit.org/server-mods/ultralogger/";
 		if(urlExists(url)){
 			BufferedReader in = null;
 			try {
 				URL site = new URL(url);
 				in = new BufferedReader(new InputStreamReader(site.openStream()));
 				String inputLine;
 				while ((inputLine = in.readLine()) != null) {
 					if(inputLine.contains("Latest build :")){
 						String last =inputLine.substring(inputLine.indexOf("Lastest build :")+27,
 								inputLine.indexOf("1")+7).trim();
 						char C =last.charAt(last.length()-1);
 						if(C!='0'&&C!='1'&&C!='2'&&C!='3'&&C!='4'&&C!='5'&&C!='6'&&C!='7'&&C!='8'&&C!='9')
 							last=last.substring(0, last.length()-1);
 						log.info(pref+t.translate("last")+" "+last);
						break;
 					}
 				}
 				in.close();
 			}
 			catch (IOException ex)  {
 				ex.printStackTrace();
 			}
 			finally{
 				try {
 					in.close();
 				}
 				catch (IOException ex){
 					ex.printStackTrace();
 				}
 			}
 		}
 	}
 	/**Use to translate the specified key
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public String translate(String key){
 		return t.translate(key);
 	}
 	
 	/**
 	 * Disable all file loggers
 	 */
 	public void disable(){
 		if(chat)
 			chatL.disable();
 		if(command)
 			commandL.disable();
 		if(player)
 			playerL.disable();
 		if(vehicle)
 			vehicleL.disable();
 		if(craft)
 			craftL.disable();
 		if(enchantment)
 			enchantmentL.disable();
 		if(block)
 			blockL.disable();
 		if(weather)
 			weatherL.disable();
 		if(world)
 			worldL.disable();
 		if(entity)
 			entityL.disable();
 		if(plugins)
 			pluginL.disable();
 		if(inventory)
 			inventoryL.disable();
 		if(block_history){
 			try {
 				hist_manager.save();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	//STATIC METHODS
 	
 	/**Check if the specified url exists and can be reached
 	 * 
 	 * @param url the url to check
 	 * @return if the url exists
 	 */
 	public static boolean urlExists(String url) {
 		try {
 			URL site = new URL(url);
 			try {
 				site.openStream();
 				return true;
 			} catch (IOException ex) {
 				return false;
 			}
 		} catch (MalformedURLException ex) {
 			return false;
 		}
 	}
 	
 	/**Check if the specified player has the permission to see a block history
 	 * 
 	 */
 	public static boolean canSeeHistory(Player p){
 		return p.hasPermission("ultralogger.history") || p.isOp();
 	}
 	
 	/**
 	 * Check if the specified player has the permission to undo or redo a block destroying or placement
 	 */
 	public static boolean canRollBack(Player p){
 		return p.hasPermission("ultralogger.rollback") || p.isOp();
 	}
 	
 	/**
 	 * Check if the specified player has the [Admin] prefix
 	 */
 	public static boolean isAdmin(HumanEntity p){
 		return p.hasPermission("ultralogger.admin") || p.isOp();
 	}
 	
 	/**
 	 * Transform a location into flat files location log
 	 */
 	public static String transformToFlatLoc(Location loc){
 		return " "+plugin.translate("in")+" ["+(int)loc.getX()+","+(int)loc.getY()+","+(int)loc.getZ()+"]";
 	}
 	
 	
 }
