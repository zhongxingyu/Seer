 // DataBaseUtility.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.webobjects.appserver.WOApplication;
 import com.webobjects.eoaccess.EOAdaptorChannel;
 import com.webobjects.eoaccess.EOModel;
 import com.webobjects.eoaccess.EOSQLExpressionFactory;
 import com.webobjects.foundation.NSDictionary;
 import com.webobjects.foundation.NSMutableDictionary;
 
 
 public class DataBaseUtility {
 	
 	public static Logger logger = Logger.getLogger("rujel.dbConnection");
 
 	public static void updateModel(EOModel model, int fromVer, EOAdaptorChannel ac) 
 																		throws Exception {
 		NSDictionary modelInfo = model.userInfo();
 		int modelVersion; 
 		if(modelInfo == null) {
 			throw new IllegalStateException("No modelInfo found");
 		} else {
 			String verStr = (String)modelInfo.valueForKeyPath("schemaVersion.number");
 			if(verStr == null)
 				throw new IllegalStateException("Model has no schema version");
 			modelVersion = Integer.parseInt(verStr);
 		}
 		String framework = (String)modelInfo.valueForKey("framework");
 		InputStream upd = WOApplication.application().resourceManager().inputStreamForResourceNamed(
 				"upd" + model.name() + ".sql", framework, null);
 		if(upd == null)
 			throw new IllegalStateException("No schema update script found");
 		BufferedReader update = new BufferedReader(new InputStreamReader(upd,"utf8"));
 		int recentVer = 0;
 		StringBuilder buf = new StringBuilder();
 		EOSQLExpressionFactory exprFactory = ac.adaptorContext().adaptor().expressionFactory();
 		lines:
 		while(true) {
 			String line = update.readLine();
 			if(line == null)
 				break;
 			line = line.trim();
 			if(line.length() == 0)
 				continue;
 			if(line.startsWith("--")) {
 				StringBuilder num = null;
 				for (int i = 2; i < line.length(); i++) {
 					char c = line.charAt(i);
 					if(Character.isWhitespace(c) || c == '-') {
 						if(num != null && num.length() > 0)
 							break;
 						continue;
 					}
 					if(c == 'v') {
 						num = new StringBuilder();
 						continue;
 					}
 					if(num == null)
 						continue lines;
 					if(Character.isDigit(c))
 						num.append(c);
 					else
 						break;
 				}
 				if(num != null && num.length() > 0) {
 					if(fromVer < recentVer)
 						fromVer = recentVer;
 					recentVer = Integer.parseInt(num.toString());
 					if(recentVer > modelVersion)
 						break lines;
 					if(fromVer < recentVer) {
 						buf.append("Updating schema for model '").append(model.name()).append('\'');
 						if(fromVer > 0) {
 							buf.append(" from v").append(fromVer);
 						}
 						for (int i = line.length(); i > 3; i--) {
 							char c = line.charAt(i -1);
 							if(c != '-' && !Character.isWhitespace(c)) {
 								buf.append(" to ").append(line.substring(2,i));
 								break;
 							}
 						}
 						logger.fine(buf.toString());
 					}
 					buf.delete(0, buf.length());
 				}
 				continue;
 			} else if(recentVer <= fromVer) {
 				continue;
 			}
 			if(buf.length() > 0)
 				buf.append('\n');
 			buf.append(line);
 			if(line.charAt(line.length() -1) == ';') {
 				line = buf.toString();
 				buf.delete(0, buf.length());
 				ac.evaluateExpression(exprFactory.expressionForString(line));
 				if(ac.isFetchInProgress())
 					ac.cancelFetch();
 			}
 		} // cycle lines
 		upd.close();
 		if(recentVer != modelVersion)
 			throw new IllegalStateException(
 					"Update script failed to update schema to actual model version");
 	}
 	
 	public static boolean createDatabaseForModel(EOModel model) {
 		NSDictionary modelInfo = model.userInfo();
 		if(modelInfo == null) {
 			return false;
 		}
 		String scriptName = (String)modelInfo.valueForKey("creationScript");
 		if(scriptName == null)
 			return false;
 		String framework = (String)modelInfo.valueForKey("framework");
 		InputStream script = WOApplication.application().resourceManager().
 				inputStreamForResourceNamed(scriptName, framework, null);
 		if(script == null) {
 			logger.log(WOLogLevel.WARNING, "Autocreation script '" + scriptName + 
 					"' for model '" + model.name() + "' was not found.");
 			return false;
 		}
 		return executeScript(script);
 	}
 	
 	public static boolean executeScript(InputStream script) {
 		return executeScript(script, null);
 	}
 	
 	public static boolean executeScript(InputStream script, NSDictionary params) {
 		if(script == null)
 			return false;
 		SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
 		if(dbSettings == null)
 			return false;
 		SettingsReader dbMapping = dbSettings.subreaderForPath("dbMapping", false);
 		NSMutableDictionary mapping = null;
 		if(params != null) {
 			mapping = new NSMutableDictionary();
 			Enumeration enu = params.keyEnumerator();
 			while (enu.hasMoreElements()) {
 				String key = (String) enu.nextElement();
 				Object value = params.objectForKey(key);
 				if(value instanceof CharSequence) {
 					mapping.takeValueForKey(value.toString(), key);
 					continue;
 				}
 				NSDictionary map = (NSDictionary)value;
 				String dbName = (String)map.valueForKey("dbName");
 				if(dbMapping != null)
 					dbName = dbMapping.get(dbName, dbName);
 				Object args = map.valueForKey("args");
 				if(args != null) {
 					if(args instanceof Object[])
 						dbName = String.format(dbName, (Object[])args);
 					else
 						dbName = String.format(dbName, args);
 				}
 				mapping.takeValueForKey(dbName, key);
 			} // params enumeration
 		}
 		if (dbMapping != null) {
 			Enumeration enu = dbMapping.keyEnumerator();
 			while (enu.hasMoreElements()) {
 				String key = (String) enu.nextElement();
 				if(key.indexOf("%s") >= 0)
 					continue;
 				if(mapping == null)
 					mapping = new NSMutableDictionary(dbMapping.get(key, key), key);
 				else
 					mapping.takeValueForKey(dbMapping.get(key, key), key);
 			}
 		}
 		try {
 		      Connection conn = getConnection(dbSettings);
 		      if(conn == null) {
 		    	  logger.log(WOLogLevel.SEVERE,
 		    			  "Failed to get database connection for script execution");
 		    	  return false;
 		      }
 		      executeScript(conn, script, mapping);
 		} catch (Exception e) {
 			logger.log(WOLogLevel.SEVERE,"Error executing database script",e);
 			return false;
 		}
 		return true;
 	}
 	
 	protected static Connection getConnection(SettingsReader dbSettings) throws Exception {
 		if(dbSettings == null)
 			dbSettings = SettingsReader.settingsForPath("dbConnection", false);
 		String serverURL = dbSettings.get("serverURL",null);
 		String urlSuffix = dbSettings.get("urlSuffix",null);
 		if(serverURL == null)
 			return null;
 		if(urlSuffix != null)
 			serverURL = serverURL + urlSuffix; 
 		Properties connectionProps = new Properties();
 		connectionProps.put("user", dbSettings.get("username", "rujel"));
 		connectionProps.put("password", dbSettings.get("password", "RUJELpassword"));
 		urlSuffix = dbSettings.get("driver",null);
 		if(urlSuffix != null) {
 			Class driverClass = Class.forName(urlSuffix);
 			Constructor driverConstructor = driverClass.getConstructor((Class[])null);
 			Driver driver = (Driver)driverConstructor.newInstance((Object[])null);
 			DriverManager.registerDriver(driver);
 		}
 		return DriverManager.getConnection(serverURL,connectionProps);
 	}
 	
 	protected static void executeScript(Connection conn, InputStream script, NSDictionary mapping)
 			throws SQLException, IOException {
 	      Statement stmnt = conn.createStatement();
 	      StringBuilder buf = new StringBuilder();
 	      BufferedReader sql = new BufferedReader(new InputStreamReader(script,"utf8"));
 	      int upd = 0;
 	      while(true) {
 	    	  String line = sql.readLine();
 	    	  if(line == null)
 	    		  break;
 	    	  line = line.trim();
 	    	  if(line.length() == 0)
 	    		  continue;
 	    	  if(line.startsWith("--"))
 	    		  continue;
 	    	  if(buf.length() > 0)
 	    		  buf.append('\n');
 	    	  buf.append(line);
 	    	  if(line.charAt(line.length() -1) == ';') {
 	    		  replace(buf, mapping);
 	    		  line = buf.toString();
 	    		  buf.delete(0, buf.length());
 	    		  upd += stmnt.executeUpdate(line);
 	    	  }
 	      }
 	}
 	
 	private static void replace(StringBuilder buf, NSDictionary mapping) {
 		if(mapping == null)
 			return;
 		Enumeration enu = mapping.keyEnumerator();
 		while (enu.hasMoreElements()) {
 			String key = (String) enu.nextElement();
 			String replacement = (String)mapping.valueForKey(key);
 			int idx = buf.indexOf(key);
 			while (idx >=0) {
				buf.replace(idx, idx + key.length(), replacement);
 				idx = buf.indexOf(key, idx + replacement.length());
 			}
 		}
 	}
 }
