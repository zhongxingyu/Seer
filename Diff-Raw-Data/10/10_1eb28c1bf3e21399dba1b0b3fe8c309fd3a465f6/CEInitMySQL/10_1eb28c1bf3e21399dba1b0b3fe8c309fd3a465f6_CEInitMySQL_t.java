 package me.ccattell.plugins.completeeconomy.database;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
import static me.ccattell.plugins.completeeconomy.CompleteEconomy.plugin;
import org.bukkit.ChatColor;
 
 /**
  *
  * @author Charlie
  */
 public class CEInitMySQL {
 
     CEDatabase service = CEDatabase.getInstance();
     public Connection connection = service.getConnection();
     public Statement statement = null;
    public String pluginName;
 
     public void CEInitMySQL() {
     }
 
     public void initMYSQL() {
        pluginName = ChatColor.DARK_PURPLE + "[Complete Economy]" + ChatColor.RESET + " ";
         try {
             statement = connection.createStatement();
             String queryCE = "CREATE TABLE IF NOT EXISTS CEMain (player_name text NOT NULL, cash float NOT NULL, bank float NOT NULL, xp float NOT NULL, last_login int(11) NOT NULL) CHARACTER SET utf8 COLLATE utf8_general_ci";
             statement.executeUpdate(queryCE);
             String queryJobs = "CREATE TABLE IF NOT EXISTS CEJobs (player_name text NOT NULL, job text NOT NULL, experience int(11) NOT NULL, level int(11) NOT NULL, status text NOT NULL) CHARACTER SET utf8 COLLATE utf8_general_ci";
             statement.executeUpdate(queryJobs);
         } catch (SQLException e) {
            plugin.console.sendMessage(pluginName + ChatColor.GOLD + "Could not create MySQL tables: " + e.getMessage() + ChatColor.RESET);
         }
     }
 }
