 package org.minecraftsmp.fizz;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.event.EventHandler;
 
 import org.bukkit.event.Listener;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.io.IOException;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 public final class Fizz extends JavaPlugin implements Listener {
 
 	private Connection webdb;
 	private Connection bukkitdb;
 	private ResultSet result;
 	
 	private String tablePrefix;
 	private String mineUser;
 	
 	private String translationTable;
 	
 	private String permissionsPlugin;
 	private String defaultGroup;
 	
 	private boolean doLoginMessage;
 	private String loginMessage;
 	
 	public void onEnable() {
 		if (!(Files.exists(Paths.get("plugins/Fizz/config.yml")))) {
 			this.saveDefaultConfig();
 			getLogger().info("Generated fresh configuration file.");
 		}
 		getServer().getPluginManager().registerEvents(this, this);
 		getLogger().info("Registered player login listener.");
 		getLogger().info("Connecting to MySQL database...");
 		try {
 			webdb = DriverManager.getConnection(
 				"jdbc:mysql://" +
 				this.getConfig().getString("web_database.host") +
 				":" + this.getConfig().getInt("web_database.port") +
 				"/" + this.getConfig().getString("web_database.database"),
 
 				this.getConfig().getString("web_database.user"), this.getConfig().getString("web_database.pass")
 			);
 			getLogger().info("Connected to PhpBB database successfully.");
 		} catch (SQLException e) {
 			getLogger().severe("Could not connect to the web MySQL database.");
 			System.err.println(e);
 		}
 		try {
 			bukkitdb = DriverManager.getConnection(
 				"jdbc:mysql://" +
 				this.getConfig().getString("bukkit_database.host") +
 				":" + this.getConfig().getInt("bukkit_database.port") +
 				"/" + this.getConfig().getString("bukkit_database.database"),
 
 				this.getConfig().getString("bukkit_database.user"), this.getConfig().getString("bukkit_database.pass")
 			);
 			getLogger().info("Connected to Bukkit database successfully.");
			bukkitdb.createStatement().executeQuery("CREATE TABLE IF NOT EXISTS `" + this.getConfig().getString("bukkit_database.database") + "`.`" + translationTable + "`");
 		} catch (SQLException e) {
 			getLogger().severe("Could not connect to the bukkit MySQL database.");
 			System.err.println(e);
 		}
		
		tablePrefix = this.getConfig().getString("web_database.table_prefix");
		mineUser = this.getConfig().getString("web_database.minecraft_username_field");
		
		translationTable = this.getConfig().getString("bukkit_database.translation_table");
		
		permissionsPlugin = this.getConfig().getString("permissions.plugin");
		defaultGroup = this.getConfig().getString("permissions.default_group");
		
		doLoginMessage = this.getConfig().getBoolean("messages.login");
		loginMessage = this.getConfig().getString("messages.login_message").replace("&", "§");
 	}
 
 	public void onDisable() {
 		HandlerList.unregisterAll((Listener)this);
 		getLogger().info("Unregistered all owned listeners.");
 		try {
 			webdb.close();
 			bukkitdb.close();
 			getLogger().info("Closed connections to all databases.");
 		} catch (SQLException e) {
 			System.err.println(e);
 			getLogger().severe("Couldn't close one or more database connections!");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		int userid = -1;
 		int groupid = -1;
 		String group = defaultGroup;
 		try {
 			result = webdb.createStatement().executeQuery(
 				"SELECT user_id FROM " + tablePrefix + "profile_fields_data WHERE " + mineUser + "='" +
 				event.getPlayer().getName() + "'"
 			);
 			result.next();
 			userid = result.getInt("user_id");
 		} catch (SQLException e) {
 			getLogger().severe("Failed to fetch a user ID from web database!");
 			System.err.println(e);
 			return;
 		}
 		try {
 			result = webdb.createStatement().executeQuery(
 				"SELECT group_id FROM " + tablePrefix + "users WHERE user_id='" + userid + "'"
 			);
 			result.next();
 			groupid = result.getInt("group_id");
 		} catch (SQLException e) {
 			System.err.println(e);
 			getLogger().severe("Failed to fetch a group ID from web database!");
 			return;
 		}
 		try {
 			result = bukkitdb.createStatement().executeQuery(
 				"SELECT group_name FROM " + translationTable + " WHERE group_id='" + groupid + "'"
 			);
 			result.next();
 			group = result.getString("group_name");
 		} catch (SQLException e) {
 			System.err.println(e);
 			getLogger().severe("Failed to fetch group name from bukkit database!");
 			return;
 		}
 		
 		String[] groups = {group};
 		PermissionsEx.getUser(event.getPlayer()).setGroups(groups);
 		if (doLoginMessage) {
 			event.getPlayer().sendRawMessage(loginMessage.replace("%group%", group));
 		}
 	}
 }
