 /*
  * Copyright 2007 Sebastien Brunot (sbrunot@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.buildmonitor.monitors;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.codec.binary.Base64;
 
 import net.sourceforge.buildmonitor.BuildMonitor;
 import net.sourceforge.buildmonitor.BuildReport;
 import net.sourceforge.buildmonitor.MonitoringException;
 import net.sourceforge.buildmonitor.BuildReport.Status;
 import net.sourceforge.buildmonitor.dialogs.BambooPropertiesDialog;
 
 import org.xml.sax.InputSource;
 
 /**
  * Set of properties needed to monitor a bamboo server.
  */
 public class BambooProperties
 {
 	//////////////////////////////
 	// Constants
 	//////////////////////////////
 
 	private static final String BAMBOO_PASSWORD_PROPERTY_KEY = "bamboo.password";
 
 	private static final String BAMBOO_USERNAME_PROPERTY_KEY = "bamboo.username";
 
 	private static final String UPDATE_PERIOD_IN_SECONDS_PROPERTY_KEY = "update.period.in.seconds";
 
 	private static final String BAMBOO_SERVER_BASE_URL_PROPERTY_KEY = "bamboo.server.base.url";
 
 	private static final String BAMBOO_PROJECT_PROPERTY_KEY = "bamboo.server.project_keys";
 
 	private static final String USER_PROPERTIES_FILE = "bamboo-monitor.properties";
 
 	//////////////////////////////
 	// Instance attributes
 	//////////////////////////////
 
 	private String serverBaseUrl = null;
 	
 	private String username = null;
 	
 	private String password = null;
 	
 	private Integer updatePeriodInSeconds = null;
 
 	private String projectKeys = null;
 	
 	//////////////////////////////
 	// Getters and Setters
 	//////////////////////////////
 
 	/**
 	 * Get URL to the bamboo server
 	 * @return the URL to the bamboo server
 	 */
 	public String getServerBaseUrl()
 	{
 		return this.serverBaseUrl;
 	}
 
 	/**
 	 * Set URL to the bamboo server
 	 * @param theServerBaseUrl the URL to the bamboo server
 	 */
 	public void setServerBaseUrl(String theServerBaseUrl)
 	{
 		this.serverBaseUrl = theServerBaseUrl;
 		// trim the remaining / in server base url if it exists
 		if (this.serverBaseUrl != null && this.serverBaseUrl.endsWith("/"))
 		{
 			this.serverBaseUrl = this.serverBaseUrl.substring(0, this.serverBaseUrl.length() - 1);
 		}
 	}
 
 	/**
 	 * Get the period (in seconds) of build status update
 	 * @return the period (in seconds) of build status update
 	 */
 	public Integer getUpdatePeriodInSeconds()
 	{
 		return this.updatePeriodInSeconds;
 	}
 
 	/**
 	 * Set the period (in seconds) of build status update
 	 * @param theUpdatePeriodInSeconds the period (in seconds) of build status update
 	 */
 	public void setUpdatePeriodInSeconds(Integer theUpdatePeriodInSeconds)
 	{
 		this.updatePeriodInSeconds = theUpdatePeriodInSeconds;
 	}
 
 	/**
 	 * Get the bamboo user name
 	 * @return the bamboo user name
 	 */
 	public String getUsername()
 	{
 		return this.username;
 	}
 
 	/**
 	 * Set the bamboo user name
 	 * @param theUsername the bamboo user name
 	 */
 	public void setUsername(String theUsername)
 	{
 		this.username = theUsername;
 	}
 	
 	/**
 	 * Get the bamboo user password
 	 */
 	public String getPassword()
 	{
 		return this.password;
 	}
 
 	/**
 	 * Set the bamboo user password
 	 * @param thePassword the bamboo user password
 	 */
 	public void setPassword(String thePassword)
 	{
 		this.password = thePassword;
 	}
 	
 	/**
 	 * Get the bamboo project keys
 	 */
 	public List<String> getProjectKeys()
 	{
		if (this.projectKeys == null)
 		{
 			return null;
 		}
		return new ArrayList<String>(Arrays.asList(theProjectKeys.split(",")));
 	}
 
 	/**
 	 * Set the bamboo project keys
 	 * @param thePassword the bamboo user password
 	 */
 	public void setProjectKeys(String theProjectKeys)
 	{
 		this.projectKeys = theProjectKeys;
 	}
 
 	//////////////////////////////
 	// File persistence
 	//////////////////////////////
 	
 	/**
 	 * Load the properties from the {@link #USER_PROPERTIES_FILE} file in the
 	 * user home directory.
 	 */
 	public void loadFromFile() throws FileNotFoundException, IOException
 	{
 		// Load the content of the properties file into a Properties object
 		Properties bambooMonitorProperties = new Properties();
 		File bambooMonitorPropertiesFile = new File(System.getProperty("user.home"), USER_PROPERTIES_FILE);
 		if (bambooMonitorPropertiesFile.exists())
 		{
 			FileInputStream bambooMonitorPropertiesFileIS = new FileInputStream(bambooMonitorPropertiesFile);
 			bambooMonitorProperties.load(bambooMonitorPropertiesFileIS);
 			bambooMonitorPropertiesFileIS.close();
 		}
 		
 		// Update the attributes using the values defined in the properties file
 		synchronized (this)
 		{
 			setServerBaseUrl(bambooMonitorProperties.getProperty(BAMBOO_SERVER_BASE_URL_PROPERTY_KEY));
 
 			String updatePeriodInSecondsAsString = bambooMonitorProperties.getProperty(UPDATE_PERIOD_IN_SECONDS_PROPERTY_KEY);
 			if (updatePeriodInSecondsAsString != null)
 			{
 				try
 				{
 					setUpdatePeriodInSeconds(Integer.parseInt(updatePeriodInSecondsAsString));
 				}
 				catch(NumberFormatException e)
 				{
 					// Use a default value
 					setUpdatePeriodInSeconds(300);
 				}
 			}
 			setUsername(bambooMonitorProperties.getProperty(BAMBOO_USERNAME_PROPERTY_KEY));
 			setProjectKeys(bambooMonitorProperties.getProperty(BAMBOO_PROJECT_PROPERTY_KEY));
 			String proppassword = bambooMonitorProperties.getProperty(BAMBOO_PASSWORD_PROPERTY_KEY);
 			if (proppassword.startsWith("{base64}"))
 			{
 				proppassword = proppassword.substring(8);
 				proppassword = new String(Base64.decodeBase64(proppassword.getBytes()));
 			}
 			setPassword(proppassword);
 		}
 	}
 	
 	/**
 	 * Save the properties from the {@link #USER_PROPERTIES_FILE} file in the
 	 * user home directory.
 	 */
 	public void saveToFile() throws FileNotFoundException, IOException
 	{
 		// Build a Properties object that contains the values of the bamboo properties
 		Properties bambooMonitorProperties = new Properties();
 		synchronized (this)
 		{
 			String proppassword = "{base64}" + new String(Base64.encodeBase64(this.password.getBytes()));
 			bambooMonitorProperties.setProperty(BAMBOO_SERVER_BASE_URL_PROPERTY_KEY, this.serverBaseUrl);
 			bambooMonitorProperties.setProperty(BAMBOO_USERNAME_PROPERTY_KEY, this.username);
 			bambooMonitorProperties.setProperty(BAMBOO_PASSWORD_PROPERTY_KEY, proppassword);
			bambooMonitorProperties.setProperty(BAMBOO_PROJECT_PROPERTY_KEY, this.projectKeys);
 			bambooMonitorProperties.setProperty(UPDATE_PERIOD_IN_SECONDS_PROPERTY_KEY, "" + this.getUpdatePeriodInSeconds());
 		}
 		
 		// Store the Properties object in the file
 		File bambooMonitorPropertiesFile = new File(System.getProperty("user.home"), USER_PROPERTIES_FILE);
 		FileOutputStream buildMonitorPropertiesOutputStream = new FileOutputStream(bambooMonitorPropertiesFile);
 		bambooMonitorProperties.store(buildMonitorPropertiesOutputStream, "File last updated on " + new Date());
 		buildMonitorPropertiesOutputStream.close();
 	}
 }
 
