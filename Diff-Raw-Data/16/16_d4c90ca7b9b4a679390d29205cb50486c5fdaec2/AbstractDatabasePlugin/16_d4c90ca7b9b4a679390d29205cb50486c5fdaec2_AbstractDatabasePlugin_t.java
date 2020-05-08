 package name.richardson.james.bukkit.utilities.plugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.config.ServerConfig;
 
 import name.richardson.james.bukkit.utilities.persistence.configuration.SimpleDatabaseConfiguration;
 import name.richardson.james.bukkit.utilities.persistence.database.DatabaseLoader;
 import name.richardson.james.bukkit.utilities.persistence.database.DefaultDatabaseLoader;
 import name.richardson.james.bukkit.utilities.persistence.database.SQLiteDatabaseLoader;
 
 public abstract class AbstractDatabasePlugin extends AbstractPlugin {
 
 	private EbeanServer database;
 
 	@Override
 	public EbeanServer getDatabase() {
 		return this.database;
 	}
 
	@Override
	public String getArtifactId() {
		return "";
	}
 
 
 	/**
 	 * Attempt to load a {@link name.richardson.james.bukkit.utilities.persistence.database.SimpleDatabaseLoader} using the information from {@link DatabaseConfiguration},
 	 * failing that using the settings in bukkit.yml and initalise it.
 	 */
 	protected void loadDatabase()
 	throws IOException {
 		final File file = new File(this.getDataFolder().getPath() + File.separatorChar + AbstractPlugin.DATABASE_CONFIG_NAME);
 		final InputStream defaults = this.getResource(DATABASE_CONFIG_NAME);
 		final ServerConfig serverConfig = new ServerConfig();
 		this.getServer().configureDbConfig(serverConfig);
 		final SimpleDatabaseConfiguration configuration = new SimpleDatabaseConfiguration(file, defaults, this.getName(), serverConfig, serverConfig.getDataSourceConfig());
 		DatabaseLoader loader;
 		if (configuration.getDataSourceConfig().getDriver().contains("sqlite")) {
 			loader = new SQLiteDatabaseLoader(this.getClassLoader(), this.getDatabaseClasses(), configuration);
 		} else {
 			loader = new DefaultDatabaseLoader(this.getClassLoader(), this.getDatabaseClasses(), configuration);
 		}
 		loader.initalise();
 		this.database = loader.getEbeanServer();
 	}
 
 }
