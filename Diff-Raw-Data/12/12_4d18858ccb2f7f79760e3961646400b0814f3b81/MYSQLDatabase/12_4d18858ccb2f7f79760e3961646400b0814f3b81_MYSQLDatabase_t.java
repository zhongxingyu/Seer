 package com.codeski.webstats.databases;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldType;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.codeski.webstats.MaterialEvent;
 import com.codeski.webstats.Webstats;
 
 public class MYSQLDatabase extends Database {
 	private final String[] tables = {
 			"CREATE TABLE IF NOT EXISTS ws_damages (damage_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, damage_damager_type SMALLINT UNSIGNED, damage_damager_player INT UNSIGNED, damage_damager_material SMALLINT UNSIGNED, damage_damager_projectile SMALLINT UNSIGNED, damage_damaged_type SMALLINT UNSIGNED NOT NULL, damage_damaged_player INT UNSIGNED, damage_damaged_death BIT NOT NULL DEFAULT 0, damage_type SMALLINT UNSIGNED NOT NULL, damage_amount FLOAT UNSIGNED NOT NULL, damage_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, damage_world SMALLINT UNSIGNED NOT NULL, damage_x INT NOT NULL, damage_y INT NOT NULL, damage_z INT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_damage_types (type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, type_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_distances (distance_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, distance_player INT UNSIGNED NOT NULL, distance_type SMALLINT UNSIGNED NOT NULL, distance_count FLOAT UNSIGNED NOT NULL, distance_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, distance_world SMALLINT UNSIGNED NOT NULL, distance_from_x INT NOT NULL, distance_from_y INT NOT NULL, distance_from_z INT NOT NULL, distance_to_x INT NOT NULL, distance_to_y INT NOT NULL, distance_to_z INT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_distance_types (type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, type_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_eggs (egg_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, egg_player INT UNSIGNED NOT NULL, egg_count TINYINT UNSIGNED NOT NULL, egg_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, egg_world SMALLINT UNSIGNED NOT NULL, egg_x INT NOT NULL, egg_y INT NOT NULL, egg_z INT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_entity_types (type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, type_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_fishes (fish_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, fish_player INT UNSIGNED NOT NULL, fish_event SMALLINT UNSIGNED NOT NULL, fish_caught_entity SMALLINT UNSIGNED, fish_caught_player INT UNSIGNED, fish_caught_material SMALLINT UNSIGNED, fish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, fish_world SMALLINT UNSIGNED NOT NULL, fish_x INT NOT NULL, fish_y INT NOT NULL, fish_z INT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_fish_events (event_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, event_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_materials (material_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, material_player INT UNSIGNED, material_event SMALLINT UNSIGNED NOT NULL, material_type SMALLINT UNSIGNED NOT NULL, material_count SMALLINT UNSIGNED NOT NULL, material_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, material_world SMALLINT UNSIGNED NOT NULL, material_x INT NOT NULL, material_y INT NOT NULL, material_z INT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_material_events (event_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, event_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_material_types (type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, type_name VARCHAR(255) UNIQUE NOT NULL, type_block BIT NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_players (player_id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT, player_name CHAR(16) UNIQUE NOT NULL, player_uuid CHAR(36) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_sessions (session_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, session_player INT UNSIGNED NOT NULL, session_expired BIT NOT NULL DEFAULT 0, session_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, session_end DATETIME ON UPDATE CURRENT_TIMESTAMP)",
 			"CREATE TABLE IF NOT EXISTS ws_statistics (statistic_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, statistic_key VARCHAR(255) UNIQUE NOT NULL, statistic_value VARCHAR(255))",
 			"CREATE TABLE IF NOT EXISTS ws_uptimes (uptime_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, uptime_expired BIT NOT NULL DEFAULT 0, uptime_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, uptime_end DATETIME ON UPDATE CURRENT_TIMESTAMP)",
 			"CREATE TABLE IF NOT EXISTS ws_worlds (world_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, world_name VARCHAR(255) UNIQUE NOT NULL, world_uuid CHAR(36) UNIQUE NOT NULL, world_type SMALLINT UNSIGNED NOT NULL, world_environment SMALLINT UNSIGNED NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_world_environments (environment_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, environment_name VARCHAR(255) UNIQUE NOT NULL)",
 			"CREATE TABLE IF NOT EXISTS ws_world_types (type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT, type_name VARCHAR(255) UNIQUE NOT NULL)"
 	};
 
 	@Override
 	public void addMaterial(String player, String event, String type, String count, String world, String x, String y, String z) {
 		this.update("INSERT INTO ws_materials VALUES (DEFAULT, " + (player != null ? "(SELECT player_id FROM ws_players WHERE player_name = '" + player + "')" : "NULL") + ", (SELECT event_id FROM ws_material_events WHERE event_name = '" + event + "'), (SELECT type_id FROM ws_material_types WHERE type_name = '" + type + "'), " + count + ", DEFAULT, (SELECT world_id FROM ws_worlds WHERE world_name = '" + world + "'), " + x + ", " + y + ", " + z + ")");
 	}
 
 	@Override
 	public boolean connect(FileConfiguration configuration) {
 		// Connect to the database
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connection = DriverManager.getConnection("jdbc:mysql://" + configuration.getString("database.host") + "/" + configuration.getString("database.database") + "?user=" + configuration.getString("database.user") + "&password=" + configuration.getString("database.pass"));
 		} catch (ClassNotFoundException e) {
 			Webstats.severe("Unable to load database driver. Your configuration may be incorrect.");
 			return false;
 		} catch (SQLException e) {
 			Webstats.severe("Unable to connect to database. Your configuration may be incorrect.");
 			return false;
 		}
 		// Create the tables
 		for (String sql : tables)
 			this.update(sql);
		// The server is up now
		this.update("INSERT INTO ws_uptimes VALUES (DEFAULT, DEFAULT, DEFAULT, NULL)");
 		// Damage types
 		List<String> dl = this.getEnums("ws_damage_types", "type_name");
 		for (DamageCause d : DamageCause.values())
 			if (!dl.contains(d.toString()))
 				this.update("INSERT INTO ws_damage_types VALUES (DEFAULT, '" + d + "')");
 		// Entity types
 		List<String> el = this.getEnums("ws_entity_types", "type_name");
 		for (EntityType e : EntityType.values())
 			if (!el.contains(e.toString()))
 				this.update("INSERT INTO ws_entity_types VALUES (DEFAULT, '" + e + "')");
 		// Material types
 		List<String> ml = this.getEnums("ws_material_types", "type_name");
 		for (Material m : Material.values())
 			if (!ml.contains(m.toString()))
 				this.update("INSERT INTO ws_material_types VALUES (DEFAULT, '" + m + "', " + m.isBlock() + ")");
 		// Material events
 		List<String> mel = this.getEnums("ws_material_events", "event_name");
 		for (MaterialEvent m : MaterialEvent.values())
 			if (!mel.contains(m.toString()))
 				this.update("INSERT INTO ws_material_events VALUES (DEFAULT, '" + m + "')");
 		// World types
 		List<String> wl = this.getEnums("ws_world_types", "type_name");
 		for (WorldType w : WorldType.values())
 			if (!wl.contains(w.toString()))
 				this.update("INSERT INTO ws_world_types VALUES (DEFAULT, '" + w + "')");
 		// World environments
 		List<String> wel = this.getEnums("ws_world_environments", "environment_name");
 		for (Environment w : Environment.values())
 			if (!wel.contains(w.toString()))
 				this.update("INSERT INTO ws_world_environments VALUES (DEFAULT, '" + w + "')");
 		// List of worlds
 		ArrayList<World> currentWorlds = new ArrayList<World>();
 		Result worlds = this.query("SELECT world_name FROM ws_worlds ORDER BY world_id ASC");
 		try {
 			while (worlds.getResult().next())
 				currentWorlds.add(Bukkit.getWorld(worlds.getResult().getString(1)));
 			worlds.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		for (World w : Bukkit.getWorlds())
 			if (!currentWorlds.contains(w))
 				this.update("INSERT INTO ws_worlds VALUES (DEFAULT, '" + w.getName() + "', '" + w.getUID() + "', (SELECT type_id FROM ws_world_types WHERE type_name = '" + w.getWorldType() + "'), (SELECT environment_id FROM ws_world_environments WHERE environment_name = '" + w.getEnvironment() + "'))");
		// Mark each online player as having joined
 		for (Player p : Bukkit.getOnlinePlayers())
 			this.playerJoined(p.getName(), p.getUniqueId() + "");
 		return true;
 	}
 
 	@Override
 	public void disconnect() {
		// The server is down now
		this.update("UPDATE ws_uptimes SET uptime_expired = 1 WHERE uptime_expired = 0");
		// Mark each online player as having quit
 		for (Player p : Bukkit.getOnlinePlayers())
 			this.playerQuit(p.getName());
 		try {
 			connection.close();
 		} catch (SQLException ignore) {
 		}
 	}
 
 	public List<String> getEnums(String table, String column) {
 		List<String> enums = new ArrayList<String>();
 		Result res = this.query("SELECT " + column + " FROM " + table);
 		try {
 			while (res.getResult().next())
 				enums.add(res.getResult().getString(1));
 			res.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return enums;
 	}
 
 	@Override
 	public void playerJoined(String player, String uuid) {
 		this.update("INSERT IGNORE INTO ws_players (player_name, player_uuid) VALUES ('" + player + "', '" + uuid + "')");
 		this.update("INSERT INTO ws_sessions VALUES (DEFAULT, (SELECT player_id FROM ws_players WHERE player_name = '" + player + "'), DEFAULT, DEFAULT, NULL)");
 		this.update("INSERT INTO ws_statistics VALUES (DEFAULT, 'peak', " + Bukkit.getOnlinePlayers().length + ") ON DUPLICATE KEY UPDATE statistic_value = IF(" + Bukkit.getOnlinePlayers().length + " > statistic_value, " + Bukkit.getOnlinePlayers().length + ", statistic_value)");
 	}
 
 	@Override
 	public void playerQuit(String player) {
 		this.update("UPDATE ws_sessions SET session_expired = 1 WHERE session_expired = 0 AND session_player = (SELECT player_id FROM ws_players WHERE player_name = '" + player + "')");
 	}
 }
