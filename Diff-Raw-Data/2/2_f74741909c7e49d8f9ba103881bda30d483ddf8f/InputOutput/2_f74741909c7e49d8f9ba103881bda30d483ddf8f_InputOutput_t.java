 package com.matejdro.bukkit.monsterhunt;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.util.config.Configuration;
 
 public class InputOutput {
 private static Connection connection;
 	
 	
 	 public static synchronized Connection getConnection() {
 	        try {
 				if (connection == null || connection.isClosed()) {
 				    connection = createConnection();
 				}
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return connection;
 	    }
 
 	    private static Connection createConnection() {
 	        try {
 	            if (Settings.globals.getBoolean("Database.UseMySQL", false)) {
 	                Class.forName("com.mysql.jdbc.Driver");
 	                Connection ret = DriverManager.getConnection(Settings.globals.getString("Database.MySQLConn", ""), Settings.globals.getString("Database.MySQLUsername", ""), Settings.globals.getString("Database.MySQLPassword", ""));
 	                ret.setAutoCommit(false);
 	                return ret;
 	            } else {
 	                Class.forName("org.sqlite.JDBC");
 	                Connection ret = DriverManager.getConnection("jdbc:sqlite:plugins" + File.separator + "MonsterHunt" + File.separator + "MonsterHunt.sqlite");
 	                ret.setAutoCommit(false);
 	                return ret;
 	            }
 	        } catch (ClassNotFoundException e) {
 	            e.printStackTrace();
 	            return null;
 	        } catch (SQLException e) {
 	            e.printStackTrace();
 	            return null;
 	        }
 	    }
 	    	    
 	    public static Integer getHighScore(String player)
 	    {
 	    	Connection conn = getConnection();
 	    	PreparedStatement ps = null;
 			ResultSet set = null;
 			Integer score = null;
 			
 	    	try {
 				ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores WHERE name = ? LIMIT 1");
 			
             ps.setString(1, player);
             set = ps.executeQuery();
             
             if (set.next())
             	score = set.getInt("highscore");
             
             set.close();
             ps.close();
             conn.close();
 	    	} catch (SQLException e) {
 	    		MonsterHunt.log.log(Level.SEVERE,"[MonsterHunt] Error while retreiving high scores! - " + e.getMessage() );
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             return score;
             
 	    }
 	    
 	    public static Integer getHighScoreRank(String player)
 	    {
 	    	Connection conn = getConnection();
 	    	PreparedStatement ps = null;
 			ResultSet set = null;
 			Boolean exist = false;
             Integer counter = 0;
             
 	    	try {
 				ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores ORDER BY highscore DESC");
 			
             set = ps.executeQuery();
             
             
             while (set.next())
             {
             	counter++;
             	String name = set.getString("name");
             	if (name.equals(player))
             	{
             		exist = true;
             		break;
             	}
             }
             
             set.close();
             ps.close();
             conn.close();
             
 	    	} catch (SQLException e) {
 	    		MonsterHunt.log.log(Level.SEVERE,"[MonsterHunt] Error while retreiving high scores! - " + e.getMessage() );
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    	if (exist)
             	return counter;
             else
             	return null;
 	    }
 	    
 	    public static LinkedHashMap<String, Integer> getTopScores(int number)
 	    {
 	    	Connection conn = getConnection();
 	    	PreparedStatement ps = null;
 			ResultSet set = null;
 			LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
             
 	    	try {
 				ps = conn.prepareStatement("SELECT * FROM monsterhunt_highscores ORDER BY highscore DESC LIMIT ?");
 				ps.setInt(1, number);
 				
 	            set = ps.executeQuery();
 	            
 	            
 	            while (set.next())
 	            {
 	            	
 	            	String name = set.getString("name");
 	            	Integer score = set.getInt("highscore");
 	            	map.put(name, score);
 	            }
 	            
 	            set.close();
 	            ps.close();
 	            conn.close();
             
 	    	} catch (SQLException e) {
 	    		MonsterHunt.log.log(Level.SEVERE,"[MonsterHunt] Error while retreiving high scores! - " + e.getMessage() );
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    	return map;
 	    }
 	    
 	    public static void UpdateHighScore(String playername, int score)
 	    {
 	    	try {
 				Connection conn = InputOutput.getConnection();
 				PreparedStatement ps = conn.prepareStatement("REPLACE INTO monsterhunt_highscores VALUES (?,?)");
 				ps.setString(1, playername);
 				ps.setInt(2, score);
 				ps.executeUpdate();
 				conn.commit();
 				ps.close();
 				conn.close();
 			} catch (SQLException e) {
 				MonsterHunt.log.log(Level.SEVERE,"[MonsterHunt] Error while inserting new high score into DB! - " + e.getMessage() );
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
 
 	
 	public static void LoadSettings()
 	{	
 		if (!new File("plugins" + File.separator + "MonsterHunt").exists()) {
 			try {
 			(new File("plugins" + File.separator + "MonsterHunt")).mkdir();
 			} catch (Exception e) {
 			MonsterHunt.log.log(Level.SEVERE, "[MonsterHunt]: Unable to create plugins/MontsterHunt/ directory");
 			}
 			}
 		Settings.globals = new Configuration(new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt"));
 
 		
 		LoadDefaults();
 		
 		if (!new File("plugins" + File.separator + "MonsterHunt" + File.separator, "global.txt").exists()) 
 			{
 				for (Entry<String, Object> e : Settings.defaults.entrySet())
 				{
 					Settings.globals.setProperty(e.getKey(), e.getValue());
 				}
 				Settings.globals.save();
 			}
 		
 		Settings.globals.load();
 		
 		for (String n : Settings.globals.getString("EnabledWorlds").split(","))
 		{
 					MonsterHuntWorld mw = new MonsterHuntWorld(n);
 					Configuration config = new Configuration(new File("plugins" + File.separator + "MonsterHunt" + File.separator,n + ".yml"));
 					Settings settings = new Settings(config);
 					mw.settings = settings;
 				
 					HuntWorldManager.worlds.put(n, mw);
 		}
 		
 		String[] temp = Settings.globals.getString("HuntZone.FirstCorner", "0,0,0").split(",");
 		HuntZone.corner1 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
 		temp = Settings.globals.getString("HuntZone.SecondCorner", "0,0,0").split(",");
 		HuntZone.corner2 = new Location(null, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
 		temp = Settings.globals.getString("HuntZone.TeleportLocation", "0,0,0").split(",");
 		World world = MonsterHunt.instance.getServer().getWorld(Settings.globals.getString("HuntZone.World", MonsterHunt.instance.getServer().getWorlds().get(0).getName()));
 		HuntZone.teleport = new Location(world, Double.parseDouble(temp[0]), Double.parseDouble(temp[1]), Double.parseDouble(temp[2]));
 		
 		//Create zone world
 		MonsterHuntWorld mw = new MonsterHuntWorld(world.getName());
 		Configuration config = new Configuration(new File("plugins" + File.separator + "MonsterHunt" + File.separator + "zone.yml"));
 		Settings settings = new Settings(config);
 		mw.settings = settings;
 	
 		HuntWorldManager.HuntZoneWorld = mw;
 		
 	}
 	
 	public static void LoadDefaults()
 	{
 		Settings.defaults.put("StartTime", 13000);
 		Settings.defaults.put("EndTime", 23600);
 		Settings.defaults.put("DeathPenalty", 30);
 		Settings.defaults.put("TellTime", true);
 		Settings.defaults.put("CountBows", true);
 		Settings.defaults.put("EnableSignup", true);
 		Settings.defaults.put("EnableHighScores", true);
 		Settings.defaults.put("MinimumPointsPlace1", 1);
 		Settings.defaults.put("MinimumPointsPlace2", 1);
 		Settings.defaults.put("MinimumPointsPlace3", 1);
 		Settings.defaults.put("MinimumPlayers", 2);
 		Settings.defaults.put("StartChance", 100);
 		Settings.defaults.put("SkipDays", 0);
 		Settings.defaults.put("SignUpPeriodTime", 5);
 		Settings.defaults.put("AllowSignUpAfterHuntStart", false);
 		Settings.defaults.put("EnabledWorlds", MonsterHunt.instance.getServer().getWorlds().get(0).getName());
 		Settings.defaults.put("OnlyCountMobsSpawnedOutside", false);
 		Settings.defaults.put("OnlyCountMobsSpawnedOutsideHeightLimit", 0);
 		Settings.defaults.put("SkipToIfFailsToStart", -1);
 		Settings.defaults.put("AnnounceLead", true);
 		Settings.defaults.put("SelectionTool", 268);
 		Settings.defaults.put("HuntZoneMode", false);
 		
 		Settings.defaults.put("Rewards.EnableReward", false);
 		Settings.defaults.put("Rewards.EnableRewardEveryonePermission", false);
 		Settings.defaults.put("Rewards.RewardEveryone", false);
 		Settings.defaults.put("Rewards.NumberOfWinners", 3);
 		Settings.defaults.put("Rewards.RewardParametersPlace1", "3 3");
 		Settings.defaults.put("Rewards.RewardParametersPlace2", "3 2");
 		Settings.defaults.put("Rewards.RewardParametersPlace3", "3 1");
 		Settings.defaults.put("Rewards.RewardParametersEveryone", "3 1-1");
 
 		for (String i : new String[]{"Zombie", "Skeleton", "Creeper", "Spider", "Ghast", "Slime", "ZombiePigman", "Giant", "TamedWolf", "WildWolf", "ElectrifiedCreeper", "Player"})
 		{
 			Settings.defaults.put("Value." + i + ".General", 10);
 			Settings.defaults.put("Value." + i + ".Wolf", 7);
 			Settings.defaults.put("Value." + i + ".Arrow", 4);
 			Settings.defaults.put("Value." + i + ".283", 20);
 		}
 				
 		Settings.defaults.put("Database.UseMySQL", false);
 		Settings.defaults.put("Database.MySQLConn", "jdbc:mysql://localhost:3306/minecraft");
 		Settings.defaults.put("Database.MySQLUsername", "root");
 		Settings.defaults.put("Database.MySQLPassword", "password");
 		
 		Settings.defaults.put("Debug", false);
 		
 		Settings.defaults.put("Messages.StartMessage", "&2Monster Hunt have started in world <World>! Go kill those damn mobs!");
 		Settings.defaults.put("Messages.FinishMessageWinners", "Sun is rising, so monster Hunt is finished in world <World>! Winners of the today's match are: [NEWLINE] 1st place: <NamesPlace1> (<PointsPlace1> points) [NEWLINE] 2nd place: <NamesPlace2> (<PointsPlace2> points) [NEWLINE] 3rd place: <NamesPlace3> (<PointsPlace3> points)" );
 		Settings.defaults.put("Messages.KillMessageGeneral", "You have got <MobValue> points from killing that <MobName>. You have <Points> points so far. Keep it up!");
 		Settings.defaults.put("Messages.KillMessageWolf", "You have got <MobValue> points because your wolf killed <MobName>. You have <Points> points so far. Keep it up!");
 		Settings.defaults.put("Messages.KillMessageArrow", "You have got only <MobValue> points because you used bow when killing <MobName>. You have <Points> points so far. Keep it up!");
 		Settings.defaults.put("Messages.RewardMessage", "Congratulations! You have received <Items>");
 		Settings.defaults.put("Messages.DeathMessage","You have died, so your Monster Hunt score is reduced by 30%. Be more careful next time!");
 		Settings.defaults.put("Messages.NoBowMessage", "Your kill is not counted. Stop camping with your bow and get into the fight!");
 		Settings.defaults.put("Messages.SignupBeforeHuntMessage", "You have signed up for the next hunt in world <World>!");
 		Settings.defaults.put("Messages.SignupAtHuntMessage", "You have signed up for the hunt in in world <World>. Now hurry and kill some monsters!");
 		Settings.defaults.put("Messages.HighScoreMessage","You have reached a new high score: <Points> points!");
 		Settings.defaults.put("Messages.FinishMessageNotEnoughPoints", "Sun is rising, so monster Hunt is finished in world <World>! Unfortunately nobody killed enough monsters, so there is no winner.");
 		Settings.defaults.put("Messages.FinishMessageNotEnoughPlayers", "Sun is rising, so monster Hunt is finished in world <World>! Unfortunately there were not enough players participating, so there is no winner.");
 		Settings.defaults.put("Messages.MessageSignUpPeriod", "Sharpen your swords, strengthen your armor and type /hunt, because Monster Hunt will begin in several mintues in world <World>!");
 		Settings.defaults.put("Messages.MessageTooLateSignUp", "Sorry, you are too late to sign up. More luck next time!");
 		Settings.defaults.put("Messages.MessageAlreadySignedUp", "You are already signed up!");
 		Settings.defaults.put("Messages.MessageStartNotEnoughPlayers", "Monster Hunt was about to start, but unfortunately there were not enough players signed up. ");
 		Settings.defaults.put("Messages.KillMobSpawnedInsideMessage", "Your kill was not counted. Stop grinding in caves and go outside!");
 		Settings.defaults.put("Messages.MessageHuntStatusNotActive", "Hunt is currently not active anywhere");
 		Settings.defaults.put("Messages.MessageHuntStatusHuntActive", "Hunt is active in <Worlds>");
 		Settings.defaults.put("Messages.MessageHuntStatusLastScore", "Your last score in this world was <Points> points");
 		Settings.defaults.put("Messages.MessageHuntStatusNotInvolvedLastHunt", "You were not involved in last hunt in this world");
 		Settings.defaults.put("Messages.MessageHuntStatusNoKills", "You haven't killed any mob in this world's hunt yet. Hurry up!");
 		Settings.defaults.put("Messages.MessageHuntStatusCurrentScore", "Your current score in this world's hunt is <Points> points! Keep it up!");
 		Settings.defaults.put("Messages.MessageHuntStatusTimeReamining", "Keep up the killing! You have only <Timeleft>% of the night left in this world!");
 		Settings.defaults.put("Messages.MessageLead", "<Player> has just taken over lead with <Points> points!");
 		Settings.defaults.put("Messages.MessageHuntTeleNoHunt", "You cannot teleport to hunt zone when there is no hunt!");
 		Settings.defaults.put("Messages.MessageHuntTeleNotSignedUp", "You cannot teleport to hunt zone if you are not signed up to the hunt!");
 
 		Settings.defaults.put("HuntZone.FirstCorner", "0,0,0");
 		Settings.defaults.put("HuntZone.SecondCorner", "0,0,0");
 		Settings.defaults.put("HuntZone.TeleportLocation", "0,0,0");
 		Settings.defaults.put("HuntZone.World", MonsterHunt.instance.getServer().getWorlds().get(0).getName());
 	}
 	
 	public static void saveZone()
 	{
 		Settings.globals.setProperty("HuntZone.FirstCorner", String.valueOf(HuntZone.corner1.getBlockX()) + "," + String.valueOf(HuntZone.corner1.getBlockY()) + "," + String.valueOf(HuntZone.corner1.getBlockZ()));
 		Settings.globals.setProperty("HuntZone.SecondCorner", String.valueOf(HuntZone.corner2.getBlockX()) + "," + String.valueOf(HuntZone.corner2.getBlockY()) + "," + String.valueOf(HuntZone.corner2.getBlockZ()));
 		Settings.globals.setProperty("HuntZone.TeleportLocation", String.valueOf(HuntZone.teleport.getX()) + "," + String.valueOf(HuntZone.teleport.getY()) + "," + String.valueOf(HuntZone.teleport.getZ()));
 		Settings.globals.setProperty("HuntZone.World", HuntZone.teleport.getWorld().getName());
 		
 		Settings.globals.save();
 	}
 		
 	public static void PrepareDB()
     {
         Connection conn = null;
         Statement st = null;
         try {
             conn = InputOutput.getConnection();
             st = conn.createStatement();
            if (Settings.globals.getBoolean("Database.UseMySQL", false))
             {
             	st.executeUpdate("CREATE TABLE IF NOT EXISTS `monsterhunt_highscores` ( `name` varchar(250) NOT NULL DEFAULT '', `highscore` integer DEFAULT NULL, PRIMARY KEY (`name`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;");
             }
             else
             {
                 st.executeUpdate("CREATE TABLE IF NOT EXISTS \"monsterhunt_highscores\" (\"name\" VARCHAR PRIMARY KEY  NOT NULL , \"highscore\" INTEGER)");
   	
             }
                 conn.commit();
         } catch (SQLException e) {
             MonsterHunt.log.log(Level.SEVERE, "[MonsterHunt]: Error while creating tables! - " + e.getMessage());
     }
     }
 	
 }
