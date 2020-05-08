 /*
  * This file is part of DropletPlayerData.
  *
  * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package org.spout.droplet.playerdata;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.h2.jdbcx.JdbcConnectionPool;
 
import org.spout.api.plugin.CommonPlugin;

 import org.spout.droplet.playerdata.configuration.DatabaseConfiguration;
 
public class DropletPlayerData extends CommonPlugin {
 	private DatabaseConfiguration config;
 	private JdbcConnectionPool pool;
 
 	@Override
 	public void onLoad() {
 		getLogger().info("loaded.");
 	}
 
 	@Override
 	public void onEnable() {
 		config = new DatabaseConfiguration(this.getDataFolder());
 		config.load();
 
 		String path;
 		try {
 			path = getDataFolder().getCanonicalPath();
 		} catch (IOException e) {
 			path = getDataFolder().getPath();
 		}
 		final String connUrl = DatabaseConfiguration.CONNECTION_STRING.getString().replaceAll(Pattern.quote("${plugin_data_dir}"), Matcher.quoteReplacement(path));
 		final String user = DatabaseConfiguration.USERNAME.getString();
 		getLogger().info("Attempting to connect to " + connUrl + ", with user [" + user + "]");
 
 		pool = JdbcConnectionPool.create(connUrl, user, DatabaseConfiguration.PASSWORD.getString());
 		pool.setMaxConnections(DatabaseConfiguration.POOL_SIZE.getInt());
 		getEngine().getEventManager().registerEvents(new DataEventListener(this), this);
 
 		getLogger().info("enabled.");
 	}
 
 	@Override
 	public void onDisable() {
 		pool.dispose();
 		getLogger().info("disabled.");
 	}
 
 	public DatabaseConfiguration getConfig() {
 		return config;
 	}
 
 	public JdbcConnectionPool getConnectionPool() {
 		return pool;
 	}
 }
