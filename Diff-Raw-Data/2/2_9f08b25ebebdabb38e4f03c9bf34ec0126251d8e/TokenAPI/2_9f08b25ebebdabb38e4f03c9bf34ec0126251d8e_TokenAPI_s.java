 package code.husky;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TokenAPI extends JavaPlugin implements Listener {
 
 	YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins/TokenAPI/config.yml"));
 	String usr = getUser();
 	String ps = getPass();
 	MySQL m = new MySQL(this.cfg.getString("MySQL-host"), Integer.toString(this.cfg.getInt("MySQL-port")), this.cfg.getString("MySQL-database"), this.usr, this.ps);
 	Connection c = null;
 	Statement qr = null;
 
 	public void onEnable() {
 		setupData();
 		setupMySQL();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	public void onDisable() {
 
 	}
 
 	private void setupData()
 	{
 		File f = new File("plugins/TokenAPI/config.yml");
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
 		if (!f.exists()) {
 			config.options().header("-- MySQL Settings --");
 			config.set("MySQL-host", "localhost");
 			config.set("MySQL-user", "root");
 			config.set("MySQL-database", "tokens");
 			config.set("MySQL-password", "root");
 			config.set("MySQL-port", 3306);
 			config.set("MySQL-table", "tokens");
 			try {
 				config.save(f);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private String getPass() {
 		YamlConfiguration ccfg = YamlConfiguration.loadConfiguration(new File("plugins/TokenAPI/config.yml"));
 		if (ccfg.getString("MySQL-password") == null) {
 			return "";
 		}
 		return ccfg.getString("MySQL-password");
 	}
 
 	private String getUser()
 	{
 		YamlConfiguration ccfg = YamlConfiguration.loadConfiguration(new File("plugins/TokenAPI/config.yml"));
 		if (ccfg.getString("MySQL-user") == null) {
 			return "";
 		}
 		return ccfg.getString("MySQL-user");
 	}
 
 	private void setupMySQL() {
 		this.c = this.m.open();
 	}
 
 	public void addToken(Player p, int amount) {
 		int l = amount -= 1;
 		try {
 			qr.executeUpdate("UPDATE  `tokens`.`tokens` SET  `tokens` =  '" + amount + "' WHERE  `tokens`.`PlayerName` =  '" + p.getName() + "' AND  `tokens`.`tokens` =" + l + ";");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void removeToken(Player p, int amount) {
 		int cur = getTokens(p);
 		int hurr = cur - amount;
 		int l = hurr -= 1;
 		try {
 			qr.executeUpdate("UPDATE  `tokens`.`tokens` SET  `tokens` =  '" + hurr + "' WHERE  `tokens`.`PlayerName` =  '" + p.getName() + "' AND  `tokens`.`tokens` =" + l + ";");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int getTokens(Player p) {
 		String name = p.getName();
 		int tokens = 0;
 		try {
 			ResultSet res = this.qr.executeQuery("SELECT * FROM " + this.cfg.getString("MySQL-table") + " WHERE PlayerName='" + name + "';");
 			res.next();
 			if(res.getString("PlayerName") == null) {
 				tokens = 0;
 			} else {
 				tokens = res.getInt("tokens");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return tokens;
 	}
 
 	@EventHandler
 	public void tokenListener(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		String name = p.getName();
 		ResultSet res;
 		try {
 			res = this.qr.executeQuery("SELECT * FROM '" + "tokens" + "' WHERE PlayerName='" + name + "';");
			if(res == null) {
 				qr.executeUpdate("INSERT INTO `tokens`.`tokens` (`PlayerName`, `tokens`) VALUES ('" + name + "', '0');");
 			}
 		} catch (SQLException e2) {
 			e2.printStackTrace();
 		}
 	}
 
 }
