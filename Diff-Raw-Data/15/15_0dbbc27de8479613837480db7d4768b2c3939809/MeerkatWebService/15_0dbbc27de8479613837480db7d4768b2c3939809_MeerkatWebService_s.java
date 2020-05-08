 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2012 Merkat-Monitor
  * mailto: contact AT meerkat-monitor DOT org
  * 
  * Meerkat Monitor is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Meerkat Monitor is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License
  * along with Meerkat Monitor.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.meerkat.ws;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import javax.annotation.Resource;
 import javax.jws.WebService;
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.ws.WebServiceContext;
 import javax.xml.ws.handler.MessageContext;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.log4j.Logger;
 import org.meerkat.httpServer.HttpServer;
 import org.meerkat.network.MailManager;
 import org.meerkat.services.SQLService;
 import org.meerkat.services.SecureShellSSH;
 import org.meerkat.services.SocketService;
 import org.meerkat.services.WebApp;
 import org.meerkat.services.WebServiceApp;
 import org.meerkat.util.MasterKeyManager;
 import org.meerkat.util.PropertiesLoader;
 import org.meerkat.webapp.WebAppCollection;
 import org.meerkat.webapp.WebAppResponse;
 
 @WebService
 public class MeerkatWebService implements MeerkatWebServiceManager{
 
 	private static Logger log = Logger.getLogger(MeerkatWebService.class);
 	private WebAppCollection wapc;
 	private HttpServer httpServer;
 	private MasterKeyManager mkm;
 	private static String propertiesFile = "meerkat.properties";
 
 	private Pattern specialCharsPattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
 		
 	@Resource WebServiceContext wsContext;
 
 	/**
 	 * MeerkatWebService
 	 * @param masterKey
 	 * @param webAppsCollection
 	 * @param httpServer
 	 */
 	public MeerkatWebService(MasterKeyManager mkm, WebAppCollection webAppsCollection, HttpServer httpServer){
 		this.mkm = mkm;
 		this.httpServer = httpServer;
 		this.wapc = webAppsCollection;
 	}
 
 	/**
 	 * getRequestClientIP
 	 * @return
 	 */
 	public final String getRequestClientIP(){
 		MessageContext mc = wsContext.getMessageContext();
 		HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST); 
 		return req.getRemoteAddr(); 
 	}
 
 	/**
 	 * checkKey
 	 * @param givenKey
 	 * @return
 	 */
 	@Override
 	public boolean checkKey(String givenKey){
 		if(givenKey != null && givenKey.length() > 0 && givenKey.equals(mkm.getMasterKey())){
 			return true;
 		}
 		return false;
 	}
 
 
 	@Override
 	public String getVersion() {
 		log.info("WS request ["+getRequestClientIP()+"]: getVersion()");
 		return wapc.getAppVersion();
 	}
 
 	@Override
 	public String addSSH(String masterKey, String name, String user, String passwd, String host, String port, 
 			String expectedResponse, String cmdToExecute, String executeOnOffline, String groups, String active){
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		// validate input
 		String invalidData = "";
 		// App name
 		if(name.length() <=0 || specialCharsPattern.matcher(name).find()){
 			invalidData += "Name; ";
 		}
 		if(host.length() <=0){
 			invalidData += "Host; ";
 		}
 		// port
 		try{ 
 			int iport = Integer.valueOf(port);
 			if(iport <= 0){
 				throw new Exception();
 			}
 		}catch(Exception e){
 			invalidData += "Port; ";
 		}
 
 		if(invalidData.length() > 0){
 			return "Invalid data: "+invalidData;
 		}
 
 		if(wapc.isWebAppByNamePresent(name)){
 			return "App already present with same name: "+name;
 		}
 
 		SecureShellSSH sshApp = new SecureShellSSH(name, user, passwd, host, port, expectedResponse, cmdToExecute);
 		sshApp.addGroups(groups);
 		sshApp.setActive(Boolean.parseBoolean(active));
 
 		WebAppResponse currentStatus = sshApp.checkWebAppStatus();
 		String online = "OFFLINE";
 		if(currentStatus.isOnline()){
 			online = "ONLINE";
 		}
 		wapc.addWebApp(sshApp);
 		wapc.getWebAppByName(name).writeWebAppVisualizationDataFile();
 		//wapc.writeWebAppCollectionMotionChart();
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 
 		log.info("WS request ["+getRequestClientIP()+"]: addSSH() with name: "+name);
 		return "SSH application added! Current status: "+online+".";
 	}
 
 
 	@Override
 	public String removeAppByName(String masterKey, String name) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		if(!wapc.isWebAppByNamePresent(name)){
 			return "Application "+name+" does not exist!";
 		}else{
 			int nEvents = wapc.resetAllAppDataFromName(name); // Delete DB records
 			WebApp tmp = wapc.getWebAppByName(name);
 			wapc.removeWebApp(tmp);
 			wapc.saveConfigXMLFile();
 			httpServer.refreshIndex();
 
 			log.info("WS request ["+getRequestClientIP()+"]: removeAppByName() named: "+name+"(and "+nEvents+" records from DB)");
 			return "Application "+name+" removed successfully.";
 		}
 	}
 
 	@Override
 	public String changeMasterKey(String currentMasterKey, String newMasterKey) {
 		if(!checkKey(currentMasterKey) || newMasterKey == null){
 			log.info("WS BAD request ["+getRequestClientIP()+"]: changeMasterKey(). Bad attempt to change MasterKey!");
 			return "Incorrect current key!";
 		}
 
 		if(newMasterKey.length() <=5){
 			return "Please choose a key with at least 6 characters.";
 		}
 
 		mkm.changeMasterKey(String.valueOf(newMasterKey));
 
 		log.info("WS request ["+getRequestClientIP()+"]: changeMasterKey(). (MasterKey changed!)");
 		return "Master key changed successfully.";
 
 
 	}
 
 	@Override
 	public String addSocket(String masterKey, String name, String host, String port,
 			String sendString, String expectedString, String executeOnOffline, 
 			String groups, String active) {
 
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		// validate input
 		String invalidData = "";
 		// App name
 		if(name.length() <=0 || specialCharsPattern.matcher(name).find()){
 			invalidData += "Name; ";
 		}
 		// host
 		if(host.length() <=0){
 			invalidData += "Host; ";
 		}
 		// port
 		try{ 
 			int iport = Integer.valueOf(port);
 			if(iport <= 0){
 				throw new Exception();
 			}
 		}catch(Exception e){
 			invalidData += "Port; ";
 		}
 
 		if(invalidData.length() > 0){
 			return "Invalid data: "+invalidData;
 		}
 
 		if(wapc.isWebAppByNamePresent(name)){
 			return "App already present with same name: "+name;
 		}
 
 
 		SocketService socketService = new SocketService(name, host, port, sendString, expectedString, executeOnOffline);
 		socketService.addGroups(groups);
 		socketService.setActive(Boolean.parseBoolean(active));
 		WebAppResponse currentStatus = socketService.checkWebAppStatus();
 		String online = "OFFLINE";
 		if(currentStatus.isOnline()){
 			online = "ONLINE";
 		}
 		wapc.addWebApp(socketService);
 		wapc.getWebAppByName(name).writeWebAppVisualizationDataFile();
 		//wapc.writeWebAppCollectionMotionChart();
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 
 		log.info("WS request ["+getRequestClientIP()+"]: addSocket() named: "+name);
 		return "Socket application added! Current status: "+online+".";
 	}
 
 	@Override
 	public String addDB(String masterKey, String name, String host,
 			String port, String instanceName, String username, String password,
 			String dbType, String query, String expectedResponse, String executeOnOffline,
 			String groups, String active) {
 
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		// validate input
 		String invalidData = "";
 		// App name
 		if(name.length() <=0 || specialCharsPattern.matcher(name).find()){
 			invalidData += "Name; ";
 		}
 		// App host
 		if(host.length() <=0){
 			invalidData += "Host; ";
 		}
 		// Port
 		try{ 
 			int iport = Integer.valueOf(port);
 			if(iport <= 0){
 				throw new Exception();
 			}
 		}catch(Exception e){
 			invalidData += "Port; ";
 		}
 		// App instance
 		if(instanceName.length() <=0){
 			invalidData += "Instance/Service name; ";
 		}
 		// Database type
 		if(!dbType.equalsIgnoreCase(SQLService.TYPE_MSSQL) && 
 				!dbType.equalsIgnoreCase(SQLService.TYPE_MYSQL) &&
 				!dbType.equalsIgnoreCase(SQLService.TYPE_ORA)){
 
 			invalidData += "Please add a valid database type! (" +
 					SQLService.TYPE_MYSQL+", "+
 					SQLService.TYPE_ORA+" or "+
 					SQLService.TYPE_MSSQL+"); ";
 		}
 
 		if(invalidData.length() > 0){
 			return "Invalid data: "+invalidData;
 		}
 
 		if(wapc.isWebAppByNamePresent(name)){
 			return "Invalid or duplicated name!";
 		}
 
 		SQLService sqlService = new SQLService(name, query, expectedResponse, host, port, instanceName, username, password);
 		sqlService.addGroups(groups);
 		sqlService.setActive(Boolean.parseBoolean(active));
 
 		// Set the correct database type
 		if(dbType.equals(SQLService.TYPE_MSSQL)){
 			sqlService.setDBTypeMSSQL();
 		}else if(dbType.equals(SQLService.TYPE_MYSQL)){
 			sqlService.setDBTypeMySQL();
 		}else if(dbType.equals(SQLService.TYPE_ORA)){
 			sqlService.setDBTypeORA();
 		}
 
 		sqlService.setExecuteOnOffline(executeOnOffline);
 
 		WebAppResponse currentStatus = sqlService.checkWebAppStatus();
 		String online = "OFFLINE";
 		if(currentStatus.isOnline()){
 			online = "ONLINE";
 		}
 		wapc.addWebApp(sqlService);
 		wapc.getWebAppByName(name).writeWebAppVisualizationDataFile();
 		//wapc.writeWebAppCollectionMotionChart();
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 
 		log.info("WS request ["+getRequestClientIP()+"]: addDB() named: "+name+", type: "+sqlService.getDBType());
 		return "Database application added! Current status: "+online+".";
 	}
 
 	@Override
 	public String addWeb(String masterKey, String name, String url, String expectedString,
 			String executeOnOffline, String groups, String active) {
 
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		// validate input
 		String invalidData = "";
 		// App name
 		if(name.length() <=0 || specialCharsPattern.matcher(name).find()){
 			invalidData += "Name; ";
 		}
 		// App URL
 		try{ 
 			new HttpGet(url);
 		}catch(Exception e){
 			invalidData += "URL; ";
 		}
 
 		if(invalidData.length() > 0){
 			return "Invalid data: "+invalidData;
 		}
 
 
 		if(wapc.isWebAppByNamePresent(name)){
 			return "App already present with same name: "+name;
 		}
 
 		WebApp webApp = new WebApp(name, url, expectedString, executeOnOffline);
 		webApp.addGroups(groups);
 		webApp.setActive(Boolean.parseBoolean(active));
 
 		WebAppResponse currentStatus = webApp.checkWebAppStatus();
 		String online = "OFFLINE";
 		if(currentStatus.isOnline()){
 			online = "ONLINE";
 		}
 		wapc.addWebApp(webApp);
 		wapc.getWebAppByName(name).writeWebAppVisualizationDataFile();
 		//wapc.writeWebAppCollectionMotionChart();
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 
 		log.info("WS request ["+getRequestClientIP()+"]: addWeb() named: "+name);
 		return "Web application added! Current status: "+online+".";
 	}
 
 	@Override
 	public String addWebService(String masterKey, String name, String url,
 			String soapAction, String sendXML, String responseXML, String executeOnOffline,
 			String groups, String active){
 
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		// validate input
 		String invalidData = "";
 		// App name
 		if(name.length() <=0 || specialCharsPattern.matcher(name).find()){
 			invalidData += "Name; ";
 		}
 		// App URL
 		if(url.length() <=0){
 			invalidData += "URL; ";
 		}
 		try{ 
 			new HttpGet(url);
 		}catch(Exception e){
 			invalidData += "URL; ";
 		}
 
 		if(invalidData.length() > 0){
 			return "Invalid data: "+invalidData;
 		}
 
 
 		if(wapc.isWebAppByNamePresent(name)){
 			return "App already present with same name: "+name;
 		}
 
 		WebServiceApp webServApp = new WebServiceApp(name, url, soapAction, executeOnOffline);
 		webServApp.setPostXML(sendXML);
 		webServApp.setResponseXML(responseXML);
 		webServApp.addGroups(groups);
 		webServApp.setActive(Boolean.parseBoolean(active));
 
 		WebAppResponse currentStatus = webServApp.checkWebAppStatus();
 		String online = "OFFLINE";
 		if(currentStatus.isOnline()){
 			online = "ONLINE";
 		}
 		wapc.addWebApp(webServApp);
 		wapc.getWebAppByName(name).writeWebAppVisualizationDataFile();
 		//wapc.writeWebAppCollectionMotionChart();
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 
 		log.info("WS request ["+getRequestClientIP()+"]: addWebService() named: "+name);
 		return "WebService added! Current status: "+online+".";
 	}
 
 	@Override
 	public String resetAllData(String masterKey) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		int nEvents = wapc.resetAllAppsData();
 		log.info("WS request ["+getRequestClientIP()+"]: resetAllData()");
 
 		return "Removed all "+nEvents+" applications events.";
 
 	}
 
 	@Override
 	public String resetAllAppDataFromName(String masterKey, String name) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		if(!wapc.isWebAppByNamePresent(name)){
 			return "Application: "+name+" - does not exist!";
 		}
 
 		int nEvents = wapc.resetAllAppDataFromName(name);
 		log.info("WS request ["+getRequestClientIP()+"]: resetAllAppDataFromName("+name+")");
 
 		return "Removed all events ("+nEvents+") from application: "+name;
 	}
 
 	@Override
 	public String shutdown(String masterKey) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		log.info("Shutting down Meerkat-Monitor...");
 		log.info("\tClosing Database...");
 		wapc.getEmbeddedDB().shutdownDB();
 		log.info("\tClosing application...");
 		log.info("Finished. Meerkat-Monitor stopped!");
 		log.info("");
 		System.exit(0);
 
 		return ""; // keep the compiler happy
 	}
 
 	@Override
 	public String removeAllApps(String masterKey) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		int nEvents = wapc.getNumberOfEventsInCollection();
 		int nApps = wapc.removeAllApps();
 		wapc.saveConfigXMLFile();
 
 		log.info("WS request ["+getRequestClientIP()+"]: removeAllApps()");
 
 		return "Deleted "+nApps+" application(s) [with "+nEvents+" events].";
 	}
 
 	@Override
 	public byte[] getProperties(String masterKey) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!".getBytes();
 		}
 
 		PropertiesLoader pL = new PropertiesLoader(propertiesFile);
 		byte[] contentBytes = PropertiesLoader.getPropertiesAsContentString(pL.getPropetiesFromFile()).getBytes();
 
 		return contentBytes;
 	}
 
 	@Override
 	public String updateProperties(String masterKey, byte[] properties) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		Properties prop;
 		try {
 			prop = PropertiesLoader.getStringContentAsProperties(new String(properties, "UTF-8"));
 			String validationStr = PropertiesLoader.validateStringProperties(new String(properties, "UTF-8"));
 			if(validationStr.length() > 0){ // Properties are not valid
 				return validationStr;
 			}
 		} catch (UnsupportedEncodingException e) {
 			return e.getMessage();
 		}
 
 		// update the key internally before saving the properties file
 		mkm.changeMasterKey(String.valueOf(prop.getProperty("meerkat.password.master")));
 
 		// Save the new properties
 		PropertiesLoader pLnew = new PropertiesLoader(propertiesFile);
 		pLnew.writePropertiesToFile(prop);
 
 		// update settings
 		httpServer.refreshIndex();
 
 		return "Properties updated!";
 	}
 
 	@Override
 	public String sendTestEmail(String masterKey,
 			String from,
 			String to,
 			String smtpServer,
 			String smtpPort,
 			String smtpSecurity,
 			String smtpUser,
 			String smtpPassword) {
 
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		MailManager testMailManager = new MailManager();
 		String result = testMailManager.sendTestEmailSettingsFromWebService(from, to, smtpServer, smtpPort, smtpSecurity, smtpUser, smtpPassword);
 
 		return result;
 	}
 
 	@Override
 	public String getAppList(String masterKey) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 
 		String appList = "";
 		Iterator<WebApp> it = wapc.getWebAppCollectionIterator();
 		WebApp currApp;
 		while(it.hasNext()){
 			currApp = it.next();
 			appList += currApp.getName()+", ";
 		}
 
 		// return the app list (with the last "," removed)
 		if(appList.length() > 0){
 			return appList.substring(0, appList.length()-2);
 		}else{
 			return "";
 		}
 
 	}
 
 	@Override
 	public String setActive(String masterKey, String appName, boolean active) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 		
 		if(!wapc.isWebAppByNamePresent(appName)){
 			return "Application "+appName+" does not exist.";
 		}
 		
 		WebApp theApp = wapc.getWebAppByName(appName);
 		theApp.setActive(active);
 		wapc.saveConfigXMLFile();
 		httpServer.refreshIndex();
 		
 		if(active){
 			return "OK - "+appName+" is now enabled.";
 		}else{
 			return "OK - "+appName+" is now disabled.";
 		}
 	}
 
 	@Override
 	public String isActive(String masterKey, String appName) {
 		if(!checkKey(masterKey)){
 			return "Incorrect key!";
 		}
 		
 		if(!wapc.isWebAppByNamePresent(appName)){
 			return "Application "+appName+" does not exist.";
 		}
 		
 		WebApp theApp = wapc.getWebAppByName(appName);
 		if(theApp.isActive()){
 			return "true";
 		}
 		return "false";
 	}
 
 
 
 
 }
