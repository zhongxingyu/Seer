 package net.betterverse;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BanManager extends JavaPlugin implements Listener {
     public static final Logger log = Logger.getLogger("Minecraft");
     public static Connection conn;
     public static BanManager instance = null;
     
 
     public static void log(String message) {
         log(Level.INFO, message);
     }
 
     public static void log(Level level, String message) {
         log.log(level, String.format("[BanManager] %s", message));
     }
 
 
     @Override
     public void onDisable() {
         log("v" + getDescription().getVersion() + " disabled.");
         try {
             conn.close();
         } catch (SQLException e) {
             log.log(Level.SEVERE, null, e);
         }
     }
     @Override
     public void onEnable() {
         instance = this;
         if(this.getConfig() == null){
             this.saveDefaultConfig();
         }
         log("v" + getDescription().getVersion() + " enabled.");
         getServer().getPluginManager().registerEvents(this, this);
         try {
             conn = DriverManager.getConnection("jdbc:mysql://"+ this.getConfig().getString("host") + ":"+ this.getConfig().getString("port") +"/"+this.getConfig().getString("database"), this.getConfig().getString("username"), this.getConfig().getString("password"));
         } catch (SQLException ex) {
             log("Please check your configuration!");
         }
         
         try {
           DatabaseMetaData  dbm = conn.getMetaData();
      
         ResultSet tables = dbm.getTables(null, null, "bans", null);
         if (tables.next() == false) {
                PreparedStatement st = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `bans` (`id` int(50) NOT NULL AUTO_INCREMENT,`username` varchar(50) NOT NULL,`reason` varchar(5000) NOT NULL,`closed` tinyint(1) NOT NULL DEFAULT '0',`banned_by` varchar(50) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=22 ;");
                st.executeUpdate(); 
         }
         } catch (SQLException ex) {
                //Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, ex);
                 log("Please check your configuration!");
         }
     }
     public BanManager getInstance(){
         return instance;
     }
     public String searchPlayer(String regex) {
         List<Player> players = this.getInstance().getServer().matchPlayer(regex);
         if (!players.isEmpty()) {
             return players.get(0).getName();
         } else {
             return regex;
         }
     }
     @EventHandler
     public void onPrePlayerLogin(PlayerPreLoginEvent event) { 
         try{
                 
                 PreparedStatement stmt = conn.prepareStatement("SELECT username FROM bans WHERE `username`=? AND `closed`=?");
                 stmt.setString(1, event.getName());
                 stmt.setBoolean(2, false);
                 ResultSet rs = stmt.executeQuery();
                 if(rs.next()){
                     event.setResult(PlayerPreLoginEvent.Result.KICK_OTHER);
                     event.disallow(event.getResult() ,  "You are banned from this server!");
                 }else{
                     event.setResult(PlayerPreLoginEvent.Result.ALLOWED);
                     event.allow();
                 }
                 rs.close();
                 stmt.close();
             } catch (SQLException ex) {
                 BanManager.log.log(Level.SEVERE, null, ex);
             }
     }
      @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 	Player player = null;
 	if (sender instanceof Player) {
 		player = (Player) sender;
         }
         if(cmd.getName().equalsIgnoreCase("ban")){
             if (args.length > 1){
             try{
                 PreparedStatement stmt = conn.prepareStatement("SELECT username FROM bans WHERE `username`=? AND `closed`=?");
                stmt.setString(1, searchPlayer(args[0]));
                 stmt.setBoolean(2, false);
                 Statement st = conn.createStatement();
                 ResultSet rs = stmt.executeQuery();
                Player target = Bukkit.getServer().getPlayer(searchPlayer(args[0]));
                String name;
                 if(rs.next()){
                     sender.sendMessage("User is already banned!");
                 }else{
                     String reason = "";
                     for(int i = 0; i<args.length-1;i++)
                     {
                         reason = reason+" "+args[i+1];
                     }
                     String insertStatement = "Insert into bans (username,reason,banned_by) values (?,?,?)";
                     PreparedStatement prepStmt = conn.prepareStatement(insertStatement);
                    prepStmt.setString(1, searchPlayer(args[0]));
                     prepStmt.setString(2, reason);
                     prepStmt.setString(3, sender.getName());
                     prepStmt.executeUpdate();
                     if(target != null){
                         if(target.isOnline()){
                            Bukkit.getServer().getPlayer(searchPlayer(args[0])).kickPlayer("You have been banned! " + reason);
                          }
                     }
                    sender.sendMessage("Banned user "+ searchPlayer(args[0]));
 
                 }
                 st.close();
                 rs.close();
                 stmt.close();
             } catch (SQLException ex) {
                 Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             return true;
             }
 	} 
         else if(cmd.getName().equalsIgnoreCase("unban")){
             if (args.length == 1){
             try{
                 PreparedStatement stmt = conn.prepareStatement("SELECT id FROM bans WHERE `username`=? AND `closed`=?");
                stmt.setString(1, searchPlayer(searchPlayer(args[0])));
                 stmt.setBoolean(2, false);
                 Statement st = conn.createStatement();
                 ResultSet rs = stmt.executeQuery();
                Player target = (Bukkit.getServer().getPlayer(searchPlayer(args[0])));
                 if(rs.next()){
                     PreparedStatement prepStmt = conn.prepareStatement("DELETE FROM bans WHERE `id`=?");
                     prepStmt.setString(1, rs.getString("id"));
                     prepStmt.executeUpdate();
                    sender.sendMessage("Unbanned user "+searchPlayer(args[0]));
                 }else{
                    sender.sendMessage("User "+searchPlayer(args[0])+ " is not banned!");
                 }
                 
 
                 st.close();
                 rs.close();
                 stmt.close();
             } catch (SQLException ex) {
                 Logger.getLogger(BanManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             return true;
             }
 	} 
 	return false; 
     }
     
 }
