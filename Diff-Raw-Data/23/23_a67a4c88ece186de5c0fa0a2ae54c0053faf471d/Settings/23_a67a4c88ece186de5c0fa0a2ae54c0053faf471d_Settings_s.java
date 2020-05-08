 /**
  * The Permissions project.
  * Copyright (C) 2012 Walker Crouse
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package net.windwaker.permissions.data;
 
 import net.windwaker.permissions.api.GroupManager;
 import net.windwaker.permissions.api.UserManager;
 import net.windwaker.permissions.data.file.FlatFileGroupManager;
 import net.windwaker.permissions.data.file.FlatFileUserManager;
 import net.windwaker.permissions.data.sql.SQLConnection;
 import net.windwaker.permissions.data.sql.SQLGroupManager;
 import net.windwaker.permissions.data.sql.SQLUserManager;
 import org.spout.api.util.config.Configuration;
 import org.spout.api.util.config.ConfigurationNode;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Settings {
 	private static final Settings settings = new Settings();
 	private static final Configuration data = new Configuration(new File("plugins/Permissions/settings.yml"));
	private static final ConfigurationNode dataManagement = new ConfigurationNode("data-management", "flat-file");
 	public static final ConfigurationNode SQL_PROTOCOL = new ConfigurationNode("sql.protocol", "mysql");
 	public static final ConfigurationNode SQL_URI = new ConfigurationNode("sql.host", "localhost");
 	public static final ConfigurationNode SQL_USERNAME = new ConfigurationNode("sql.username", "root");
 	public static final ConfigurationNode SQL_PASSWORD = new ConfigurationNode("sql.password", "minecraft");
 
 	private Settings() {
 
 	}
 	
 	public static Settings getInstance() {
 		return settings;
 	}
 	
 	public static void init() {
 		data.load();
 		data.addNodes(dataManagement, SQL_PROTOCOL, SQL_URI, SQL_USERNAME, SQL_PASSWORD);
 		if (dataManagement.getString().equalsIgnoreCase("sql")) {
 			SQLConnection.init();
 		}
 
 		data.save();
 	}
 	
 	public GroupManager createGroupManager() {
 		String data = dataManagement.getString();
 		if (data.equalsIgnoreCase("flat-file")) {
 			return new FlatFileGroupManager();
 		}
 		
 		if (data.equalsIgnoreCase("sql")) {
 			return new SQLGroupManager();
 		}
 
 		return new FlatFileGroupManager();
 	}
 	
 	public UserManager createUserManager() {
 		String data = dataManagement.getString();
 		if (data.equalsIgnoreCase("flat-file")) {
 			return new FlatFileUserManager();
 		}
 
 		if (data.equalsIgnoreCase("sql")) {
 			return new SQLUserManager();
 		}
 
 		return new FlatFileUserManager();
 	}
 }
