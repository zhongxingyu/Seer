 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  SimpleDatabaseConfiguration.java is part of BukkitUtilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.utilities.persistence.configuration;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 
 import com.avaje.ebean.config.DataSourceConfig;
 import com.avaje.ebean.config.ServerConfig;
 import com.avaje.ebeaninternal.server.lib.sql.TransactionIsolation;
 
 import name.richardson.james.bukkit.utilities.logging.PrefixedLogger;
 
 public class SimpleDatabaseConfiguration extends AbstractConfiguration implements DatabaseConfiguration {
 
 	private static final Logger LOGGER = PrefixedLogger.getLogger(SimpleDatabaseConfiguration.class);
 
 	private final DataSourceConfig dataSourceConfig;
 	private final ServerConfig serverConfig;
 	private final File folder;
 	private final String pluginName;
 
 	public SimpleDatabaseConfiguration(final File file, final InputStream defaults, final String pluginName, final ServerConfig defaultServerConfig, final DataSourceConfig defaultDataSourceConfig) throws IOException {
 		super(file, defaults, false);
 		this.folder = file.getParentFile();
 		this.pluginName = pluginName;
 		this.serverConfig = defaultServerConfig;
 		this.dataSourceConfig = defaultDataSourceConfig;
 		this.serverConfig.setDataSourceConfig(defaultDataSourceConfig);
 		// configure database defaults from bukkit.yml
 		this.serverConfig.setDefaultServer(false);
 		this.serverConfig.setRegister(false);
 		this.serverConfig.setName(pluginName);
 		// configure datastore
 		final String username = this.getConfiguration().getString("username");
 		if (username != null) this.dataSourceConfig.setUsername(username);
 		final String password = this.getConfiguration().getString("password");
 		if (password != null) this.dataSourceConfig.setPassword(password);
 		final String driver = this.getConfiguration().getString("driver");
 		if (driver != null) this.dataSourceConfig.setDriver(driver);
 		final String isolation = this.getConfiguration().getString("isolation");
 		if (isolation != null) this.dataSourceConfig.setIsolationLevel(TransactionIsolation.getLevel(isolation));
 		// parse the database url
 		final String url = this.getConfiguration().getString("url");
		if (url != null) {
			this.dataSourceConfig.setUrl(this.replaceDatabaseString(url));
		} else {
			this.dataSourceConfig.setUrl(this.replaceDatabaseString(dataSourceConfig.getUrl()));
		}
 	}
 
 	public DataSourceConfig getDataSourceConfig() {
 		return this.dataSourceConfig;
 	}
 
 	public ServerConfig getServerConfig() {
 		return this.serverConfig;
 	}
 
 	private String replaceDatabaseString(String input) {
 		input = input.replaceAll("\\{DIR\\}", this.folder.getAbsolutePath() + File.separator);
 		input = input.replaceAll("\\{NAME\\}", this.pluginName.replaceAll("[^\\w_-]", ""));
 		return input;
 	}
 
 	@Override
 	public String toString() {
 		return "SimpleDatabaseConfiguration {" +
 		"dataSourceConfig=" + dataSourceConfig.toString() +
 		", serverConfig=" + serverConfig.toString() +
 		", folder=" + folder +
 		", pluginName='" + pluginName + '\'' +
 		", username='" + this.dataSourceConfig.getUsername() + '\'' +
 		", password='" + this.dataSourceConfig.getPassword().replaceAll(".", "*") +  '\'' +
 		", driver='" + this.dataSourceConfig.getDriver() + '\'' +
 		", isolation='" + this.dataSourceConfig.getIsolationLevel() + '\'' +
 		", url='" + this.dataSourceConfig.getUrl() + '\'' +
 		'}';
 	}
 }
