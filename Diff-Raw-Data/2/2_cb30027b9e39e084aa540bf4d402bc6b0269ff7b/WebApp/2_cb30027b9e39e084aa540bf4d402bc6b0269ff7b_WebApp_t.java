 /**
  * Meerkat Monitor - Network Monitor Tool
  * Copyright (C) 2011 Merkat-Monitor
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
 
 package org.meerkat.services;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.meerkat.dataSources.Visualization;
 import org.meerkat.db.EmbeddedDB;
 import org.meerkat.network.Availability;
 import org.meerkat.network.Latency;
 import org.meerkat.util.Counter;
 import org.meerkat.util.FileUtil;
 import org.meerkat.util.MasterKeyManager;
 import org.meerkat.util.StringUtil;
 import org.meerkat.webapp.WebAppActionResultThread;
 import org.meerkat.webapp.WebAppActionThread;
 import org.meerkat.webapp.WebAppEvent;
 import org.meerkat.webapp.WebAppResponse;
 
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class WebApp {
 	private static Logger log = Logger.getLogger(WebApp.class);
 
 	private String name;
 	private String url;
 	private String expectedString;
 	private String executeOnOffline = "";
 	@XStreamOmitField
 	public static String TYPE_WEBAPP = "WEBAPP";
 	@XStreamOmitField
 	public static String TYPE_WEBSERVICE = "WEBSERVICE";
 	@XStreamOmitField
 	public static String TYPE_DATABASE = "DATABASE";
 	@XStreamOmitField
 	public static String TYPE_SOCKET = "SOCKET";
 	@XStreamOmitField
 	public static String TYPE_SSH = "SSH";
 
 	@XStreamOmitField
 	MasterKeyManager mkm;
 
 	@XStreamOmitField
 	private String lastStatus = "NA"; // It may be online or offline - NA in the first run
 	@XStreamOmitField
 	private String actionExecOutput = "";
 	@XStreamOmitField
 	private List<WebAppEvent> events;
 	private List<String> groups;
 	@XStreamOmitField
 	private String filenameSuffix = ".html";
 	@XStreamOmitField
 	private String tempWorkingDir;
 	@XStreamOmitField
 	private String lastResponse = "";
 	@XStreamOmitField
 	private String appVersion;
 	@XStreamOmitField
 	private String configXMLFile = "";
 	private String type = TYPE_WEBAPP; // Default or set to webservice, ssh, etc.
 	private boolean enabled = true;
 	@XStreamOmitField
 	EmbeddedDB embDB;
 	@XStreamOmitField
 	Connection conn = null;
 
 	/**
 	 * WebApp
 	 * 
 	 * @param name
 	 *            WebApp name
 	 * @param url
 	 *            WebApp URL
 	 * @param expectedString
 	 *            WebApp expected string in the URL
 	 */
 	public WebApp(String name, String url, String expectedString) {
 		this.name = name;
 		this.url = url;
 		this.expectedString = expectedString;
 		this.actionExecOutput = "";
 		events = new CopyOnWriteArrayList<WebAppEvent>();
		groups = new ArrayList<String>();
 		mkm = new MasterKeyManager();
 
 		// Setup connection
 		if(conn == null){
 			embDB = new EmbeddedDB();
 			conn = embDB.getConnForQueries();
 		}
 
 	}
 
 	/**
 	 * WebApp
 	 * 
 	 * @param name
 	 * @param url
 	 * @param expectedString
 	 * @param executeOnOffline
 	 */
 	public WebApp(String name, String url, String expectedString,
 			String executeOnOffline) {
 		this.name = name;
 		this.url = url;
 		this.expectedString = expectedString;
 		this.executeOnOffline = executeOnOffline;
 		events = new ArrayList<WebAppEvent>();
 		groups = new ArrayList<String>();
 		mkm = new MasterKeyManager();
 	}
 
 	/**
 	 * WebApp
 	 */
 	public WebApp() {
 
 	}
 
 	/**
 	 * checkWebAppStatus
 	 * 
 	 * @return WebAppResponse
 	 */
 	public WebAppResponse checkWebAppStatus() {
 		// Set the response at this point to empty in case of no response at all
 		setCurrentResponse("");
 		int statusCode = 0;
 
 		// Create an instance of HttpClient.
 		HttpClient httpclient = httpClientSSLAuth();
 
 		WebAppResponse response = new WebAppResponse();
 		response.setResponseAppType();
 
 		// Create a method instance.
 		HttpGet httpget = new HttpGet(url);
 
 		// Measure the response time
 		Counter c = new Counter();
 		c.startCounter();
 
 		// Execute the method.
 		HttpResponse httpresponse = null;
 		try {
 			httpresponse = httpclient.execute(httpget);
 			// Set the http status
 			statusCode = httpresponse.getStatusLine().getStatusCode();
 
 		} catch (ClientProtocolException e) {
 			log.error("Client Protocol Exception", e);
 
 			response.setHttpStatus(0);
 
 			response.setHttpTextResponse(e.toString());
 			setCurrentResponse(e.toString());
 
 			response.setContainsWebAppExpectedString(false);
 
 			c.stopCounter();
 			response.setPageLoadTime(c.getDurationSeconds());
 
 			httpclient.getConnectionManager().shutdown();
 
 			return response;
 		} catch (IOException e) {
 			log.error("IOException - "+e.getMessage());
 
 			response.setHttpStatus(0);
 			response.setHttpTextResponse(e.toString());
 			setCurrentResponse(e.toString());
 
 			response.setContainsWebAppExpectedString(false);
 
 			c.stopCounter();
 			response.setPageLoadTime(c.getDurationSeconds());
 
 			httpclient.getConnectionManager().shutdown();
 
 			return response;
 		}
 
 		response.setHttpStatus(statusCode);
 
 		// Consume the response body
 		try {
 			httpresponse.getEntity().getContent().toString();
 		} catch (IllegalStateException e) {
 			log.error("IllegalStateException", e);
 		} catch (IOException e) {
 			log.error("IOException", e);
 		}
 
 		// Get the response
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new InputStreamReader(httpresponse
 					.getEntity().getContent()));
 		} catch (IllegalStateException e1) {
 			log.error("IllegalStateException in http buffer", e1);
 		} catch (IOException e1) {
 			log.error("IOException in http buffer", e1);
 		}
 
 		String readLine;
 		String responseBody = "";
 		try {
 			while (((readLine = br.readLine()) != null)) {
 				responseBody += "\n" + readLine;
 			}
 		} catch (IOException e) {
 			log.error("IOException in http response", e);
 		}
 
 		try {
 			br.close();
 		} catch (IOException e) {
 			log.error("Closing BufferedReader", e);
 		}
 
 		response.setHttpTextResponse(responseBody);
 		setCurrentResponse(responseBody);
 
 		// When HttpClient instance is no longer needed,
 		// shut down the connection manager to ensure
 		// immediate deallocation of all system resources
 		httpclient.getConnectionManager().shutdown();
 
 		if (statusCode != HttpStatus.SC_OK) {
 			log.warn("Httpstatus code: " + statusCode + " | Method failed: "
 					+ httpresponse.getStatusLine());
 		}
 
 		// Check if the response contains the expectedString
 		if (getCurrentResponse().contains(expectedString)) {
 			response.setContainsWebAppExpectedString(true);
 		}
 
 		// Stop the counter
 		c.stopCounter();
 		response.setPageLoadTime(c.getDurationSeconds());
 
 		return response;
 	}
 
 	/**
 	 * Httpclient to allow selfsigned certificates
 	 * 
 	 * @param origClient
 	 * @return HttpClient
 	 */
 	@SuppressWarnings("deprecation")
 	public HttpClient httpClientSSLAuth() {
 		HttpClient httpclient = new DefaultHttpClient();
 
 		// Accept SSL self signed
 		SSLContext ctx = null;
 		try {
 			ctx = SSLContext.getInstance("TLS");
 		} catch (NoSuchAlgorithmException e) {
 			log.error("Error getting SSL context", e);
 		}
 		X509TrustManager tm = new X509TrustManager() {
 
 			public void checkClientTrusted(X509Certificate[] xcs, String string)
 					throws CertificateException {
 			}
 
 			public void checkServerTrusted(X509Certificate[] xcs, String string)
 					throws CertificateException {
 			}
 
 			public X509Certificate[] getAcceptedIssuers() {
 				return null;
 			}
 		};
 
 		try {
 			ctx.init(null, new TrustManager[] { tm }, null);
 		} catch (KeyManagementException e) {
 			log.error("Error creating TrustManager", e);
 		}
 
 		SSLSocketFactory ssf = new SSLSocketFactory(ctx);
 		ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 		ClientConnectionManager ccm = httpclient.getConnectionManager();
 		SchemeRegistry sr = ccm.getSchemeRegistry();
 		sr.register(new Scheme("https", ssf, 443));
 
 		return new DefaultHttpClient(ccm, httpclient.getParams());
 	}
 
 	/**
 	 * getLatency
 	 * 
 	 * @return Latency
 	 */
 	public final String getLatency() {
 		URL u = null;
 		String hostToCheck = "";
 		if (url.contains(":")) {
 			try {
 				u = new URL(url);
 				hostToCheck = u.getHost();
 			} catch (MalformedURLException e) {
 				log.error("The webapp: " + name
 						+ " does not contain valid host.", e);
 			}
 		} else {
 			hostToCheck = url; // If no : present, then it's a direct host
 		}
 
 		Latency l = new Latency(hostToCheck);
 
 		return l.getLatency();
 	}
 
 	/**
 	 * getAvailability
 	 * 
 	 * @return Availability
 	 */
 	public final double getAvailability() {
 		Availability av = new Availability();
 		return av.getAvailability(this);
 	}
 
 	/**
 	 * getExpectedString
 	 * 
 	 * @return WebApp expected string
 	 */
 	public String getExpectedString() {
 		return expectedString;
 	}
 
 	/**
 	 * getLastStatus
 	 * 
 	 * @return WebApp last status
 	 */
 	public final String getlastStatus() {
 		return lastStatus;
 	}
 
 	/**
 	 * getName
 	 * 
 	 * @return WebApp name
 	 */
 	public final String getName() {
 		return name;
 	}
 
 	/**
 	 * getNumberOfTests
 	 * 
 	 * @return NumberOfTests
 	 */
 	public final int getNumberOfTests() {
 		return getNumberOfEvents();
 	}
 
 	/**
 	 * getUrl
 	 * 
 	 * @return WebApp URL
 	 */
 	public final String getUrl() {
 		return url;
 	}
 
 	/**
 	 * setExpectedString
 	 * 
 	 * @param expectedString
 	 *            WebApp expected string
 	 */
 	public final void setExpectedString(String expectedString) {
 		this.expectedString = expectedString;
 	}
 
 	/**
 	 * setLastStatus
 	 * 
 	 * @param lastStatus
 	 *            WebApp last status
 	 */
 	public final void setlastStatus(String lastStatus) {
 		this.lastStatus = lastStatus;
 	}
 
 	/**
 	 * setName
 	 * 
 	 * @param name
 	 *            WebApp name
 	 */
 	public final void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * setUrl
 	 * 
 	 * @param url
 	 *            WebApp URL
 	 */
 	public final void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * getExecuteOnOffline
 	 * 
 	 * @return ExecuteOnOffline action
 	 */
 	public final String getExecuteOnOffline() {
 		return executeOnOffline;
 	}
 
 	/**
 	 * setExecuteOnOffline
 	 * 
 	 * @param executeOnOffline
 	 */
 	public final void setExecuteOnOffline(String executeOnOffline) {
 		this.executeOnOffline = executeOnOffline;
 	}
 
 	/**
 	 * executeOfflineAction
 	 */
 	public final void executeOfflineAction() {
 		log.info("Taking action on offline: " + this.getName());
 		WebAppActionThread w = new WebAppActionThread(WebApp.this);
 		w.start();
 		WebAppActionResultThread r = new WebAppActionResultThread();
 		r.run(w, WebApp.this);
 	}
 
 	/**
 	 * getActionExecOutput
 	 * 
 	 * @return actionExecOutput
 	 */
 	public final String getActionExecOutput() {
 		return actionExecOutput;
 	}
 
 	/**
 	 * setActionExecOutput
 	 * 
 	 * @param actionExecOutput
 	 */
 	public final void setActionExecOutput(String actionExecOutput) {
 		this.actionExecOutput = actionExecOutput;
 	}
 
 	/**
 	 * addEvent
 	 * 
 	 * @param event
 	 */
 	public final void addEvent(WebAppEvent ev) {
 		PreparedStatement statement;
 		String queryInsert = "INSERT INTO MEERKAT.EVENTS(APPNAME, CRITICAL, DATEEV, ONLINE, AVAILABILITY, LOADTIME, LATENCY, HTTPSTATUSCODE, DESCRIPTION, RESPONSE) VALUES(";
 
 		String queryValues = "'"+ this.getName() +"', "+ev.isCritical()+", '"+ev.getDate()+"', '"+
 				ev.getStatus()+"', "+Double.valueOf(this.getAvailability())+", "+
 				Double.valueOf(ev.getPageLoadTime())+", ";
 
 		// Handle latency - may be null if host not available)
 		if(ev.getLatency() == null){
 			queryValues += null;
 		}else{
 			queryValues += ev.getLatency();
 		}
 
 		queryValues += ", "+Integer.valueOf(ev.getHttpStatusCode())+", '"+ev.getDescription()+"', ?";
 
 		if(ev.getCurrentResponse().length() > 20000){
 			// truncate the size of response
 			ev.setCurrentResponse(ev.getCurrentResponse().substring(0, 20000));
 			log.warn("Response of "+this.getName()+" bigger than 20000 (truncated!).");
 		}
 
 		try {
 			statement = conn.prepareStatement(queryInsert+queryValues+")");
 			statement.setString(1, ev.getCurrentResponse());
 			statement.execute();
 			statement.close();
 			conn.commit();
 		} catch (SQLException e) {
 			log.error("Failed to insert event into DB! - "+e.getMessage());
 		}
 
 		this.writeWebAppVisualizationDataFile();
 	}
 
 	/**
 	 * getEventList
 	 * 
 	 * @return EventList
 	 */
 	/**
 	public final Iterator<WebAppEvent> getEventListIterator() {
 		events = getEvents();
 		return events.iterator();
 	}
 	 */
 
 	/**
 	 * getEvents
 	 * @return events list
 	 */
 	 /**
 	private List<WebAppEvent> getEvents(){
 		if(conn == null){
 			embDB = new EmbeddedDB();
 			conn = embDB.getConnForQueries();
 		}
 
 		events = new CopyOnWriteArrayList<WebAppEvent>();
 
 		boolean critical;
 		String date;
 		boolean online;
 		String availability;
 		String loadTime;
 		String latency;
 		int httStatusCode;
 		String description;
 		String response;
 
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT ID, APPNAME, CRITICAL, DATEEV, ONLINE, AVAILABILITY, " +
 					"LOADTIME, LATENCY, HTTPSTATUSCODE, DESCRIPTION, RESPONSE " +
 					"FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'");
 			rs = ps.executeQuery();
 
 			while(rs.next()) {
 				critical = rs.getBoolean(3);
 				date = rs.getTimestamp(4).toString();
 				online = rs.getBoolean(5);
 				availability = String.valueOf(rs.getDouble(6));
 				loadTime = String.valueOf(rs.getDouble(7));
 				latency = String.valueOf(rs.getDouble(8));
 				httStatusCode = rs.getInt(9);
 				description = rs.getString(10);
 				response = rs.getString(11);
 
 				WebAppEvent currEv = new WebAppEvent(critical, date, online, availability, httStatusCode, description);
 				currEv.setID(rs.getInt(1));
 				currEv.setPageLoadTime(loadTime);
 				currEv.setLatency(latency);
 				currEv.setCurrentResponse(response);
 				events.add(currEv);
 			}
 
 			rs.close();
 			ps.close();
 
 		} catch (SQLException e) {
 			log.error("Failed query events from application "+this.getName());
 			log.error("", e);
 		}
 		return events;
 	}
 	//
 
 	/**
 	 * getAppLoadTimeAVG
 	 * @return
 	 */
 	public double getAppLoadTimeAVG(){
 		int decimalPlaces = 2;
 		double loadTimeAVG = 0.0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT AVG(LOADTIME) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'");
 			rs = ps.executeQuery();
 
 			rs.next();
 			loadTimeAVG = rs.getInt(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query number of events from application "+this.getName());
 			log.error("", e);
 		}
 
 		BigDecimal bd = new BigDecimal(loadTimeAVG);
 		bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_DOWN);
 		loadTimeAVG = bd.doubleValue();
 
 		return loadTimeAVG;
 	}
 
 
 	/**
 	 * getNumberOfEvents
 	 * 
 	 * @return NumberOfEvents
 	 */
 	public final int getNumberOfEvents() {
 		// Setup connection
 		if(conn == null){
 			embDB = new EmbeddedDB();
 			conn = embDB.getConnForQueries();
 		}
 
 		int numberOfEvents = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT COUNT(*) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'");
 			rs = ps.executeQuery();
 
 			rs.next();
 			numberOfEvents = rs.getInt(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query number of events from application "+this.getName());
 			log.error("", e);
 		}
 		return numberOfEvents;
 	}
 
 	/**
 	 * getNumberOfCriticalEvents
 	 * 
 	 * @return NumberOfCriticalEvents
 	 */
 	public final int getNumberOfCriticalEvents() {
 		int numberOfCriticalEvents = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT COUNT(*) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"' AND CRITICAL");
 			rs = ps.executeQuery();
 
 			rs.next();
 			numberOfCriticalEvents = rs.getInt(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query number of critical events from application "+this.getName());
 			log.error("", e);
 		}
 		return numberOfCriticalEvents;
 	}
 
 	/**
 	 * getNumberOfCriticalEvents
 	 * 
 	 * @return NumberOfCriticalEvents
 	 */
 	public final int getNumberOfOfflines() {
 		int numberOfOfflines = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT COUNT(*) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"' AND NOT ONLINE");
 			rs = ps.executeQuery();
 
 			rs.next();
 			numberOfOfflines = rs.getInt(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query number of offlines from application "+this.getName());
 			log.error("", e);
 		}
 		return numberOfOfflines;
 	}
 
 	/**
 	 * getLatencyAverage
 	 * 
 	 * @return Latency average
 	 */
 	public final double getLatencyAverage() {
 		double latencyAvg = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT AVG(LATENCY) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'");
 			rs = ps.executeQuery();
 
 			rs.next();
 			latencyAvg = rs.getDouble(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query average load time from application "+this.getName());
 			log.error("", e);
 		}
 		return latencyAvg;
 	}
 
 	/**
 	 * getLatencyAverage
 	 * 
 	 * @return Latency average
 	 */
 	private final double getAvailabilityAverage() {
 		double availAvg = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT AVG(AVAILABILITY) FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'");
 			rs = ps.executeQuery();
 
 			rs.next();
 			availAvg = rs.getDouble(1);
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query average availability from application "+this.getName());
 			log.error("", e);
 		}
 
 		BigDecimal bd = new BigDecimal(availAvg);
 		bd = bd.setScale(2, BigDecimal.ROUND_DOWN);
 		availAvg = bd.doubleValue();
 
 		return availAvg;
 	}
 
 	/**
 	 * getDataFileName
 	 * @return
 	 */
 	public final String getDataFileName() {
 		return this.name.replace(" ", "-") + filenameSuffix;
 	}
 
 	/**
 	 * getJSAnnotatedTimeLine
 	 * 
 	 * @return JSAnnotatedTimeLine
 	 */
 	/**
 	public final String getGoogleAnnotatedTimeLine() {
 		Visualization gv = new Visualization();
 		gv.setAppVersion(appVersion);
 		return gv.getAnnotatedTimeLine(this);
 	}
 	 */
 
 	/**
 	 * getJSDataTable
 	 * 
 	 * @return JSDataTable
 	 */
 	public final String getDataTable() {
 		Visualization gv = new Visualization();
 		gv.setAppVersion(appVersion);
 		return gv.getDataTable(this);
 	}
 
 	/**
 	 * writeWebAppDataFile
 	 */
 	public final void writeWebAppVisualizationDataFile() {
 		final WebApp curr = this;
 		// With many records this will be time consuming
 		Runnable visDataWriter = new Runnable(){
 			@Override
 			public void run() {
 				Visualization gv = new Visualization();
 				gv.setAppVersion(appVersion);
 				gv.writeWebAppVisualizationDataFile(curr);
 			}
 		};
 		Thread visDataWriterThread = new Thread(visDataWriter);
 		visDataWriterThread.start();
 	}
 
 	/**
 	 * 
 	 */
 	public final void writeWebAppVisualizationInfoWorkingOn() {
 		FileUtil fu = new FileUtil();
 		String pageContents = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n"
 				+ "<html>\n"
 				+ "<head>\n"
 				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><meta http-equiv=\"refresh\" content=\"5\"></meta>\n"
 				+ "<title>Meerkat Loading Data...</title>\n"
 				+ "<link rel=\"icon\"  href=\"/resources/faviconM.gif\"  type=\"image/x-icon\"></link>\n"
 				+ "</head>\n"
 				+ "<body>\n"
 				+ "<h4>"
 				+ "I'm working on data for \""+this.name+"\". <br /> Please come back later..."
 				+ "</h4>\n"
 				+ "</body>\n" + "</html>\n";
 
 		File tmp = new File(this.tempWorkingDir);
 		if (!tmp.exists()) {
 			if (!tmp.mkdirs()) {
 				log.error("ERROR creating temporary file: " + this.tempWorkingDir);
 			}
 		}
 		fu.writeToFile(tmp + "/" + this.getDataFileName(), pageContents);
 	}
 
 	/**
 	 * getTempDir
 	 */
 	public final String getTempDir() {
 		return tempWorkingDir;
 	}
 
 	/**
 	 * setTypeWebService
 	 */
 	public final void setTypeWebService() {
 		this.type = TYPE_WEBSERVICE;
 	}
 
 	/**
 	 * setTypeWebApp
 	 */
 	public final void setTypeWebApp() {
 		this.type = TYPE_WEBAPP;
 	}
 
 	/**
 	 * setTypeSQL
 	 */
 	public final void setTypeSQL() {
 		this.type = TYPE_DATABASE;
 	}
 
 	/**
 	 * setTypeSocketService
 	 */
 	public final void setTypeSocketService() {
 		this.type = TYPE_SOCKET;
 	}
 
 	/**
 	 * setTypeSSH
 	 */
 	public final void setTypeSSH() {
 		this.type = TYPE_SSH;
 	}
 
 	/**
 	 * getType
 	 * 
 	 * @return Type
 	 */
 	public final String getType() {
 		return type;
 	}
 
 	/**
 	 * setCurrentError
 	 * 
 	 * @param error
 	 */
 	public final void setCurrentResponse(String response) {
 		this.lastResponse = response;
 	}
 
 	/**
 	 * getCurrentError
 	 * 
 	 * @return
 	 */
 	public final String getCurrentResponse() {
 		return lastResponse;
 	}
 
 	/**
 	 * setTempWorkingDir
 	 * @param tempWorkingDir
 	 */
 	public final void setTempWorkingDir(String tempWorkingDir) {
 		this.tempWorkingDir = tempWorkingDir;
 	}
 
 	/**
 	 * setConfigXMLFile
 	 * @param configXMLFile
 	 */
 	public final void setConfigXMLFile(String configXMLFile) {
 		this.configXMLFile = configXMLFile;
 	}
 
 	public final String getConfigXMLFile() {
 		return this.configXMLFile;
 	}
 
 	/**
 	 * addGroup
 	 * 
 	 * @param group
 	 */
 	private final void addGroup(String group) {
 		if (!groups.contains(group) && !group.equalsIgnoreCase("")) {
 			groups.add(group);
 		}
 	}
 
 	/**
 	 * addGroups
 	 * 
 	 * @param groups
 	 */
 	public final void addGroups(String groupsList) {
 		groups = new ArrayList<String>();
 		StringUtil su = new StringUtil();
 		String[] groupsListArray = su.explodeStringToArray(groupsList, ",");
 		for (int i = 0; i < groupsListArray.length; i++) {
 			addGroup(groupsListArray[i]);
 		}
 	}
 
 	/**
 	 * getGroupIterator
 	 * 
 	 * @return
 	 */
 	public final Iterator<String> getGroupIterator() {
 		return groups.iterator();
 	}
 
 	/**
 	 * hasGroup
 	 * 
 	 * @param group
 	 * @return
 	 */
 	public final boolean hasGroup(String group) {
 		if (groups.contains(group)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * getNumberOfgroups
 	 * 
 	 * @return
 	 */
 	public final int getNumberOfGroups() {
 		return groups.size();
 	}
 
 	/**
 	 * setAppVersion
 	 * 
 	 * @param version
 	 */
 	public final void setAppVersion(String version) {
 		appVersion = version;
 	}
 
 	/**
 	 * getGroupsListString
 	 * 
 	 * @return
 	 */
 	public final String getGroupsListString() {
 		if (groups.size() == 0) {
 			return "";
 		}
 
 		Iterator<String> it = groups.iterator();
 		String currentGroup;
 		String groupsString = "";
 
 		while (it.hasNext()) {
 			currentGroup = it.next();
 			groupsString += currentGroup + ", ";
 		}
 
 		// Remove the last ,
 		int lastPos = groupsString.lastIndexOf(',');
 		groupsString = groupsString.substring(0, lastPos);
 
 		return groupsString;
 	}
 
 	/**
 	 * 
 	 * getLatencyIndicator
 	 * @return 	1 if last latency higher than latency average
 	 * 			-1 if last latency lower than latency average
 	 * 			0 if they are equal
 	 * 			(No decimal plates considered)
 	 */
 	public double getLatencyIndicator() {
 		double doubleLatencyAverage = getLatencyAverage();
 		BigDecimal bd = new BigDecimal(doubleLatencyAverage);
 		bd = bd.setScale(0, BigDecimal.ROUND_DOWN);
 		double latencyAverage = bd.doubleValue();
 
 		// get the value of last event
 		double lastLatency = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		int maxID = embDB.getMaxIDofApp(this.name);
 
 		try {
 			ps = conn.prepareStatement("SELECT ID, LATENCY "+
 					"FROM MEERKAT.EVENTS "+
 					"WHERE APPNAME LIKE '"+this.name+"' "+
 					"AND ID = "+maxID);
 
 			rs = ps.executeQuery();
 
 			while(rs.next()){
 				lastLatency = rs.getDouble(2);
 			}
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query average availability from application "+this.getName());
 			log.error("", e);
 		}
 
 		BigDecimal bd1 = new BigDecimal(lastLatency);
 		bd1 = bd1.setScale(2, BigDecimal.ROUND_DOWN);
 		lastLatency = bd1.doubleValue();
 
 		if(lastLatency > latencyAverage){
 			return 1;
 		}else if(lastLatency < latencyAverage){
 			return -1;
 		}
 
 		return 0;
 	}
 
 
 	/**
 	 * getAvailabilityIndicator
 	 * @return 	1 if last availability higher than availability average
 	 * 			-1 if last avail. lower than avail. average
 	 * 			0 if they are equal
 	 * 			(No decimal plates considered)
 	 */
 	public double getAvailabilityIndicator() {
 		double doubleAvailAverage = getAvailabilityAverage();
 		BigDecimal bd = new BigDecimal(doubleAvailAverage);
 		bd = bd.setScale(0, BigDecimal.ROUND_DOWN);
 		double availAverage = bd.doubleValue();
 
 		// get the value of last event
 		double lastAvailability = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		int maxId = embDB.getMaxIDofApp(this.getName());
 		try {
 			ps = conn.prepareStatement("SELECT ID, AVAILABILITY "+
 					"FROM MEERKAT.EVENTS "+
 					"WHERE APPNAME LIKE '"+this.getName()+"' "+
 					"AND ID = "+maxId);
 
 			rs = ps.executeQuery();
 
 			while(rs.next()){
 				lastAvailability = rs.getDouble(2);
 			}
 
 			rs.close();
 			ps.close();
 
 		} catch (SQLException e) {
 			log.error("Failed query average availability from application "+this.getName());
 			log.error("", e);
 		}
 
 		BigDecimal bd1 = new BigDecimal(lastAvailability);
 		bd1 = bd1.setScale(2, BigDecimal.ROUND_DOWN);
 		lastAvailability = bd1.doubleValue();
 
 		if(lastAvailability > availAverage){
 			return 1;
 		}else if(lastAvailability < availAverage){
 			return -1;
 		}
 
 		return 0;
 	}
 
 	/**
 	 * getLoadTimeIndicator
 	 * @return 	1 if last load time higher than load time average
 	 * 			-1 if last load time lower than load time average
 	 * 			0 if they are equal
 	 * 			(No decimal plates considered)
 	 */
 	public double getLoadTimeIndicator() {
 		double doubleLoadTimeAverage = getAppLoadTimeAVG();
 		BigDecimal bd = new BigDecimal(doubleLoadTimeAverage);
 		bd = bd.setScale(0, BigDecimal.ROUND_DOWN);
 		double loadTimeAverage = bd.doubleValue();
 
 		// get the value of last event
 		double lastLoadTime = 0;
 		PreparedStatement ps;
 		ResultSet rs = null;
 		int maxID = embDB.getMaxIDofApp(this.name);
 
 		try {
 			ps = conn.prepareStatement("SELECT ID, LOADTIME "+
 					"FROM MEERKAT.EVENTS "+
 					"WHERE APPNAME LIKE '"+this.name+"' "+
 					"AND ID = "+maxID);
 
 			rs = ps.executeQuery();
 
 			while(rs.next()){
 				lastLoadTime = rs.getDouble(2);
 			}
 
 			rs.close();
 			ps.close();
 			conn.commit();
 
 		} catch (SQLException e) {
 			log.error("Failed query average load time from application "+this.getName());
 			log.error("", e);
 		}
 
 		BigDecimal bd1 = new BigDecimal(lastLoadTime);
 		bd1 = bd1.setScale(3, BigDecimal.ROUND_DOWN);
 		lastLoadTime = bd1.doubleValue();
 
 		if(lastLoadTime > loadTimeAverage){
 			return 1;
 		}else if(lastLoadTime < loadTimeAverage){
 			return -1;
 		}
 
 		return 0;
 	}
 
 	/**
 	 * initialize
 	 */
 	public void initialize(String tempWorkingDir, String version) {
 		events = new ArrayList<WebAppEvent>();
 		setlastStatus("NA");
 		setTempWorkingDir(tempWorkingDir);
 		setAppVersion(version);
 		filenameSuffix = ".html";
 		mkm = new MasterKeyManager();
 	}
 
 	/**
 	 * @return the isActive
 	 */
 	public final boolean isActive() {
 		return enabled;
 	}
 
 	/**
 	 * @param isActive
 	 *            the enabled to set
 	 */
 	public final void setActive(Boolean isActive) {
 		this.enabled = isActive;
 	}
 
 	/**
 	 * getMasterKeyManager
 	 */
 	public final MasterKeyManager getMasterKeyManager(){
 		return this.mkm;
 	}
 
 	/**
 	 * removeAllEvents
 	 */
 	public final void removeAllEvents() {
 		// Remove DB events of this application
 		Connection conn = embDB.getConnForUpdates();
 		PreparedStatement statement = null;
 
 		String queryDelete = "DELETE FROM MEERKAT.EVENTS WHERE APPNAME LIKE '"+this.name+"'";
 
 		try {
 			statement = conn.prepareStatement(queryDelete);
 			statement.execute();
 
 			statement.close();
 			conn.commit();
 		} catch (SQLException e) {
 			log.error("Failed to remove events of "+this.name+" from DB! - "+e.getMessage());
 		}
 	}
 
 	/**
 	 * getCustomEventsList
 	 * @param appName
 	 * @param rows
 	 * @param rowBegin
 	 * @param rowEnd
 	 * @param orderBy
 	 * @param asdDSC
 	 * @return customEvents
 	 */
 	public final ArrayList<WebAppEvent> getCustomEventsList(String rowBegin, String rowEnd, String orderBy, String asdDSC){
 		ArrayList<WebAppEvent> customEvents = new ArrayList<WebAppEvent>();
 		Connection conn = embDB.getConnForQueries();
 		String orderByStr = "";
 
 		// Prevent null values if called direct link (outside Datatables)
 		if(rowBegin == null || rowEnd == null){
 			rowBegin = "0";
 			rowEnd = "10";
 		}
 
 		// Process order
 		if(orderBy == null){
 			orderBy = "0";
 		}
 		if(orderBy.equals("0")){
 			orderByStr = "ID";
 		}else if(orderBy.equals("1")){
 			orderByStr = "DATEEV";
 		}else if(orderBy.equals("2")){
 			orderByStr = "ONLINE";
 		}else if(orderBy.equals("3")){
 			orderByStr = "AVAILABILITY";
 		}else if(orderBy.equals("4")){
 			orderByStr = "LOADTIME";
 		}else if(orderBy.equals("5")){
 			orderByStr = "LATENCY";
 		}else if(orderBy.equals("6")){
 			orderByStr = "HTTPSTATUSCODE";
 		}else if(orderBy.equals("7")){
 			orderByStr = "DESCRIPTION";
 		}
 
 		int nRows = Integer.valueOf(rowEnd) - Integer.valueOf(rowBegin);
 
 		String fields = "SELECT ID, CRITICAL, DATEEV, ONLINE, AVAILABILITY, \n"+ 
 				"LOADTIME, LATENCY, HTTPSTATUSCODE, DESCRIPTION \n";
 
 		log.debug(" ");
 		log.debug("||-- APP: "+this.name);
 		log.debug("||-- Results: "+rowBegin+" to: "+rowBegin+nRows);
 		log.debug("||-- Order by: "+orderByStr+" "+asdDSC);
 
 		String eventsQuery = fields + "FROM MEERKAT.EVENTS \n"+
 				"WHERE APPNAME LIKE '"+this.name+"' \n"+
 				"ORDER BY "+orderByStr+" "+asdDSC+" \n" +
 				"OFFSET "+rowBegin+" ROWS FETCH NEXT "+nRows+" ROWS ONLY ";
 
 		int id;
 		boolean critical;
 		String date;
 		boolean online;
 		String availability;
 		String loadTime;
 		String latency;
 		int httStatusCode = 0;
 		String description;
 
 		PreparedStatement ps;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement(eventsQuery);
 			rs = ps.executeQuery();
 
 			while(rs.next()) {
 				id = rs.getInt(1);
 				critical = rs.getBoolean(2);
 				date = rs.getTimestamp(3).toString();
 				online = rs.getBoolean(4);
 				availability = String.valueOf(rs.getDouble(5));
 				loadTime = String.valueOf(rs.getDouble(6));
 				latency = String.valueOf(rs.getDouble(7));
 				httStatusCode = rs.getInt(8);
 				description = rs.getString(9);
 
 				WebAppEvent currEv = new WebAppEvent(critical, date, online, availability, httStatusCode, description);
 				currEv.setID(id);
 				currEv.setPageLoadTime(loadTime);
 				currEv.setLatency(latency);
 				customEvents.add(currEv);
 			}
 
 			rs.close();
 			ps.close();
 
 		} catch (SQLException e) {
 			log.error("Failed query events from application "+this.getName());
 			log.error("", e);
 			log.error("QUERY IS: "+eventsQuery);
 		}
 
 		return customEvents;
 	}
 
 
 
 }
