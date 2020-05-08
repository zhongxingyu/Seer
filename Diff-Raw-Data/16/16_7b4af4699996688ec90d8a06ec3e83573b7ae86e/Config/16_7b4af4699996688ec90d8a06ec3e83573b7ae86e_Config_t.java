 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2006-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.core;
 
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import netlab.hub.util.Logger;
 
 
 
 /**
  * @author ebranda
  */
 public class Config {
 	
 	static File root;
 	static Properties props;
 
 
 	/**
 	 * 
 	 */
 	public static void load(File rootDir) throws FileNotFoundException, IOException {
 		root = rootDir;
 		props = new Properties();
 		File confDir = new File(rootDir.getAbsolutePath()+File.separator+"conf");
 		File propsFile = new File(confDir, "hub.properties");
 		FileInputStream in = new FileInputStream(propsFile);
 		try {
 			props.load(in);
 		} finally {
 			in.close();
 		}
 	}
 	
 	public static String getAppName() {
 		return props.getProperty("app.name");
 	}
 	
 	public static String getAppVersion() {
 		// The build script will add the version property so return
 		// a dummy value if running from dev
 		if (props.containsKey("app.version")) {
 			return props.getProperty("app.version");
 		} else {
 			return "unspecified";
 		}
 	}
 	
 	public static String getAppBuild() {
 		// The build script will add the build property so return
 		// a dummy value if running from dev
 		if (props.containsKey("app.build")) {
 			return props.getProperty("app.build");
 		} else {
 			return "unspecified";
 		}
 	}
 	
 	public static String getAppMetadata() {
 		StringBuffer sb = new StringBuffer();
 		sb.append(getAppName()).append(" ")
 			.append(getAppVersion()).append(" ")
 				.append(getAppBuild());
 		return sb.toString();
 	}
 	
	public static int getAdminPort() {
		try {
			return Integer.parseInt(props.getProperty("app.adminport"));
		} catch(Exception e) {
			Logger.error("ServerConfig: Error getting server port number", e);
			return -1;
		}
 	}
 	
 	public static int getPort() {
 		try {
 			return Integer.parseInt(props.getProperty("server.port"));
 		} catch(Exception e) {
 			Logger.error("ServerConfig: Error getting server port number", e);
 			return -1;
 		}
 	}
 	
 	public static int getWebSocketPort() {
 		try {
 			return Integer.parseInt(props.getProperty("server.wsport"));
 		} catch(Exception e) {
 			Logger.debug("ServerConfig: Could not retrieve websocket server port number from server config");
 			return -1;
 		}
 	}
 	
 	public static String getCallHomeUrl() {
 		return getProperty("server.callhomeurl"); 
 	}
 	
 	public static boolean hasProperty(String prop) {
 		return props != null && props.getProperty(prop) != null;
 	}
 	
 	public static String getProperty(String prop) {
 		if (props != null) {
 			return props.getProperty(prop);
 		}
 		return null;
 	}
 
 }
