 package net.h31ix.anticheatbot;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.h31ix.updater.Updater;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.PircBotX;
 
 public class AntiCheatBot
 {
     private static final int VERSION_DELAY = 300000; // Every 5 minutes
     private static final int BUGS_DELAY = 60000; // Every minute
     private static final int GITHUB_DELAY = 10000; // Every 10 seconds
 
     public static String version = null;
     private static PircBotX bot;
     private static Channel channel;
     private static Connection conn = null;
     private static Statement state = null;
     private static String sqlUrl = null;
     private static String bugsUrl = null;
     private static String nickservPass = null;
     public static int bug_id = 0;
 
     public static Map<String, String> commands = new ConcurrentHashMap<String, String>();
     public static Map<String, String> messages = new ConcurrentHashMap<String, String>();
     public static Map<String, Integer> warnings = new ConcurrentHashMap<String, Integer>();
 
     public static void main(String[] args)
     {
         System.out.println("==Starting up==");
         bot = new PircBotX();
         getVersion();
         System.out.println("> Retrieved latest version...");
         updateCommits();
         System.out.println("> Updated github commits...");
         connectToSQL();
         System.out.println("> Connected to SQL...");
         getBugs();
         System.out.println("> Retrieved latest bugs...");
         getNickServPassword();
         System.out.println("> Obtained nickserv pass...");
         updateQueries();
         System.out.println("> Populated queries...");
         connectToServer();
         System.out.println("==Connected to server==");
         disconnectFromSQL();
     }
 
     private static void connectToServer()
     {
         bot.setName("AntiCheat");
 
         try
         {
             bot.connect("irc.esper.net");
         }
         catch (Exception ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
         bot.identify(nickservPass);
         bot.joinChannel("#anticheat");
         channel = bot.getChannel("#anticheat");
         bot.getListenerManager().addListener(new BotListener(bot, channel));
     }
 
     public static int addWarning(String nick)
     {
         int number = 1;
         if(warnings.containsKey(nick))
         {
             number = warnings.get(nick)+1;
         }
         warnings.put(nick, number);
         return number;
     }
 
     private static void connectToSQL()
     {
         try
         {
             boolean connect = conn == null;
             if(!connect)
             {
                 connect = conn.isClosed();
             }
             if(connect)
             {
                 if(sqlUrl == null)
                 {
                     String url = System.getenv("CLEARDB_DATABASE_URL");
                     String username = url.split("//")[1].split(":")[0];
                     String password = url.split(":")[2].split("@")[0];
                     String newUrl = "jdbc:"+url;
                     sqlUrl = newUrl.replace(username+":"+password+"@", "")+"&user="+username+"&password="+password;
                 }
                 conn = DriverManager.getConnection(sqlUrl);
                 state = conn.createStatement();
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void getNickServPassword()
     {
         // No need to connect to sql, this will only be run on initalization
         try
         {
             ResultSet rs = state.executeQuery("SELECT bot_value FROM bot_data WHERE bot_key='nickserv_password'");
             while (rs.next())
             {
                 nickservPass = rs.getString("bot_value");
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void disconnectFromSQL()
     {
         try
         {
             conn.close();
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void getVersion()
     {
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask()
         {
             @Override
             public void run()
             {
                 System.out.println("> Checking for new versions...");
                 Updater updater = new Updater("anticheat");
                 if(version == null)
                 {
                     version = updater.getLatestVersionString();
                 }
                 else if(!version.equalsIgnoreCase(updater.getLatestVersionString()))
                 {
                     updateVersion(updater.getLatestVersionString());
                 }
             }
         }, 0, VERSION_DELAY);
     }
 
     public static void closeBug(int id, String name)
     {
         try
         {
             Connection bugConn = DriverManager.getConnection(bugsUrl);
             PreparedStatement ps = bugConn.prepareStatement("UPDATE issues SET status=2, closedby=? WHERE id=?");
             ps.setString(1, name);
             ps.setInt(2, id);
             ps.executeUpdate();
             ps.close();
             bugConn.close();
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void getBugs()
     {
         if(bugsUrl == null)
         {
             bugsUrl = System.getenv("BUGS_DATABASE_URL");
         }
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask()
         {
             @Override
             public void run()
             {
                 updateBugs();
             }
         }, 0, BUGS_DELAY);
     }
 
     public static void updateBugs()
     {
         try
         {
             Connection bugConn = DriverManager.getConnection(bugsUrl);
             Statement bugState = bugConn.createStatement();
             ResultSet rs = bugState.executeQuery("SELECT id FROM issues ORDER BY id DESC LIMIT 0, 1");
             if(bug_id == 0)
             {
                 while (rs.next())
                 {
                     bug_id = rs.getInt("id");
                 }
             }
             else
             {
                 while (rs.next())
                 {
                     int id = rs.getInt("id");
                     if(id > bug_id)
                     {
                        rs = bugState.executeQuery("SELECT * FROM issues ORDER BY id DESC LIMIT 0, "+(id-bug_id));
                        while (rs.next())
                         {
                            id = rs.getInt("id");
                            String type = rs.getInt("type") == 1 ? "Bug report" : "Feature request";
                            String user = rs.getString("user");
                            String name = rs.getString("name");
                             bot.sendMessage(channel, "AntiCheat issue "+Colors.BOLD+id+Colors.NORMAL+" created: "+Colors.BOLD+type+Colors.NORMAL+" by "+Colors.BOLD+user+" | "+name+" | http://bugs.h31ix.net/issues.php?issue="+id);
                         }
                     }
                 }
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static void updateCommits()
     {
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask()
         {
             @Override
             public void run()
             {
                 try
                 {
                     // Open a connection to the page
                     URL url = new URL("http://gravitydevelopment.net/util/hook/githubhook.php");
                     HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                     httpConn.addRequestProperty("User-Agent", "Mozilla/4.76");
                     InputStreamReader inStream = new InputStreamReader(httpConn.getInputStream());
                     BufferedReader buff = new BufferedReader(inStream);
 
                     String line;
                     while((line = buff.readLine()) != null)
                     {
                         System.out.println("-> got line: "+line);
                         for(String s : line.split("<br>"))
                         {
                             bot.sendMessage(channel, s.replaceAll("<b>", Colors.BOLD).replaceAll("</b>", Colors.NORMAL));
                         }
                     }
                     httpConn = null;
                     inStream = null;
                     buff.close();
                     buff = null;
                 }
                 catch (Exception ex)
                 {
                     ex.printStackTrace();
                 }
             }
         }, 0, GITHUB_DELAY);
     }
 
     private static void updateVersion(String newVersion)
     {
         String topic = channel.getTopic().replaceAll(version, newVersion);
         bot.setTopic(channel, topic);
         bot.sendMessage(channel, "AntiCheat update. Version "+Colors.BOLD+version+Colors.BLACK+" -> "+Colors.BOLD+newVersion);
         version = newVersion;
     }
 
     public static String getBugDetails(String id)
     {
         try
         {
             URL url = new URL("http://bugs.h31ix.net/api.php?issue="+id);
             URLConnection urlConn = url.openConnection();
             InputStreamReader inStream = new InputStreamReader(urlConn.getInputStream());
             BufferedReader buff = new BufferedReader(inStream);
 
             String line = buff.readLine();
             urlConn = null;
             inStream = null;
             buff.close();
             buff = null;
             if(line.equals("NONE"))
             {
                 return "No bug report found by id "+id;
             }
             else
             {
                 return line.replaceAll("<b>", Colors.BOLD).replaceAll("</b>", Colors.NORMAL);
             }
         }
         catch (Exception ex)
         {
             ex.printStackTrace();
             return null;
         }
 
     }
 
     public static void updateQueries()
     {
         connectToSQL();
     	commands.clear();
         messages.clear();
         try
         {
             ResultSet rs = state.executeQuery("SELECT * FROM bot_queries");
             while (rs.next())
             {
                 int type = rs.getInt("type");
                 insertQuery(type, rs.getString("input"), rs.getString("output"));
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
         disconnectFromSQL();
     }
 
 
     public static String getResponse(int type, String query)
     {
         if(type == 1)
         {
             // Command
             return commands.get(query.toLowerCase());
         }
         else if(type == 2)
         {
             // Message
             return messages.get(query.toLowerCase());
         }
         return null;
     }
 
     public static void addQuery(int type, String input, String output)
     {
         connectToSQL();
         try
         {
             PreparedStatement ps = conn.prepareStatement("INSERT INTO bot_queries (input, type, output) VALUES (?, "+type+", ?)");
             ps.setString(1, input);
             ps.setString(2, output);
             ps.executeUpdate();
             ps.close();
             insertQuery(type, input, output);
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
         disconnectFromSQL();
     }
 
     public static void removeQuery(String input)
     {
         connectToSQL();
         try
         {
             PreparedStatement ps = conn.prepareStatement("DELETE FROM bot_queries WHERE input='"+input.toLowerCase()+"'");
             ps.executeUpdate();
             ps.close();
             deleteQuery(input);
         }
         catch (SQLException ex)
         {
             Logger.getLogger(AntiCheatBot.class.getName()).log(Level.SEVERE, null, ex);
         }
         disconnectFromSQL();
     }
 
     private static void insertQuery(int type, String input, String output)
     {
         if(type == 1)
         {
             // Command
             commands.put(input.toLowerCase(), output);
         }
         else if(type == 2)
         {
             // Message
             messages.put(input.toLowerCase(), output);
         }
     }
 
     private static void deleteQuery(String input)
     {
         commands.remove(input.toLowerCase());
         messages.remove(input.toLowerCase());
     }
 }
