 package de.jaschastarke.bukkit.lib.database;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 
 import de.jaschastarke.database.DatabaseConfigurationException;
 import de.jaschastarke.database.Type;
 import de.jaschastarke.database.db.Database;
 
 public final class DBHelper {
     private DBHelper() {
     }
     
     private static YamlConfiguration configuration = null;
     public static YamlConfiguration getBukkitConfig() {
         if (configuration == null) {
             Server server = Bukkit.getServer();
             configuration = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));
             configuration.options().copyDefaults(true);
             configuration.setDefaults(YamlConfiguration.loadConfiguration(server.getClass().getClassLoader().getResourceAsStream("configurations/bukkit.yml")));
         }
         return configuration;
     }
     
     private static final String DATABASE_URL_NODE = "url";
     public static Database connect(final Plugin plugin) throws DatabaseConfigurationException {
         ConfigurationSection dbc = getBukkitConfig().getConfigurationSection("database");
         if (!dbc.contains(DATABASE_URL_NODE))
             throw new DatabaseConfigurationException("No Database-URL configured in bukkit.yaml");
         String url = dbc.getString(DATABASE_URL_NODE);
         String driver = dbc.getString("driver");
         Type type = Type.getType(url);
         
         Properties prop = new Properties();
         
         Database db;
         switch (type) {
             case MySQL:
                 prop.put("autoReconnect", "true");
                 prop.put("user", dbc.getString("username"));
                prop.put("password", dbc.getString("password"));
                 db = new de.jaschastarke.database.mysql.Database(driver);
                 db.connect(url, prop);
                 break;
             case SQLite:
                 url = url.replace("{DIR}", plugin.getDataFolder().getPath() + File.separatorChar);
                 url = url.replace("{NAME}", plugin.getName());
                 db = new de.jaschastarke.database.sqlite.Database(driver);
                 db.connect(url, prop);
                 break;
             default:
                 throw new DatabaseConfigurationException("Database-Type for Connection \"" + url + "\" not supported.");
         }
         return db;
     }
 }
