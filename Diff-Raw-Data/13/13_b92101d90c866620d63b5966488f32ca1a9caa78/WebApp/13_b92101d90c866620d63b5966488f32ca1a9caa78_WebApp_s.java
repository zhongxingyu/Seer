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
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
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
 import org.meerkat.network.Availability;
 import org.meerkat.network.Latency;
 import org.meerkat.network.LoadTime;
 import org.meerkat.util.Counter;
 import org.meerkat.util.StringUtil;
 import org.meerkat.webapp.WebAppActionResultThread;
 import org.meerkat.webapp.WebAppActionThread;
 import org.meerkat.webapp.WebAppEvent;
 import org.meerkat.webapp.WebAppResponse;
 
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class WebApp implements Serializable {
 
 	private static final long serialVersionUID = 366466730003252507L;
 	private static Logger log = Logger.getLogger(WebApp.class);
 
 	private String name;
 	private String url;
 	private String expectedString;
 	private String executeOnOffline = "";
 	@XStreamOmitField
 	private String lastStatus = "NA"; // It may be online or offline - NA in the
 										// first run
 	@XStreamOmitField
 	private int numberOfTests = 0;
 	@XStreamOmitField
 	private int numberOfOfflines = 0;
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
 	private Double prevAvailability = 0.0;
 	@XStreamOmitField
 	private String prevLatency = "0.0";
 	@XStreamOmitField
 	private String prevLoad = "0.0";
 	@XStreamOmitField
 	private String appVersion;
 	@XStreamOmitField
 	private String configXMLFile = "";
 	private String type = "webapp"; // Default or set to webservice
 	private boolean enabled = true;
 
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
 		super();
 		this.name = name;
 		this.url = url;
 		this.expectedString = expectedString;
 		this.actionExecOutput = "";
 		events = new ArrayList<WebAppEvent>();
 		groups = new ArrayList<String>();
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
 		super();
 		this.name = name;
 		this.url = url;
 		this.expectedString = expectedString;
 		this.executeOnOffline = executeOnOffline;
 		events = new ArrayList<WebAppEvent>();
 		groups = new ArrayList<String>();
 	}
 
 	/**
 	 * WebApp
 	 */
 	public WebApp() {
 		super();
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
 			prevLoad = c.getDurationSeconds();
 
 			httpclient.getConnectionManager().shutdown();
 
 			return response;
 		} catch (IOException e) {
 			log.error("IOException", e);
 
 			response.setHttpStatus(0);
 			response.setHttpTextResponse(e.toString());
 			setCurrentResponse(e.toString());
 
 			response.setContainsWebAppExpectedString(false);
 
 			c.stopCounter();
 			response.setPageLoadTime(c.getDurationSeconds());
 			prevLoad = c.getDurationSeconds();
 
 			httpclient.getConnectionManager().shutdown();
 
 			return response;
 		}
 		prevLoad = c.getDurationSeconds();
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
 		prevLatency = l.getLatency();
 
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
 	 * getNumberOfOfflines
 	 * 
 	 * @return NumberOfOfflines
 	 */
 	public final int getNumberOfOfflines() {
 		return numberOfOfflines;
 	}
 
 	/**
 	 * getNumberOfTests
 	 * 
 	 * @return NumberOfTests
 	 */
 	public final int getNumberOfTests() {
 		return numberOfTests;
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
 	 * setNumberOfOfflines
 	 * 
 	 * @param numberOfOfflines
 	 */
 	public final void increaseNumberOfOfflines() {
 		numberOfOfflines++;
 	}
 
 	/**
 	 * setNumberOfTests
 	 * 
 	 * @param numberOfTests
 	 */
 	public final void increaseNumberOfTests() {
 		numberOfTests++;
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
 	public final void addEvent(WebAppEvent event) {
 		events.add(event);
 		this.writeWebAppVisualizationDataFile();
 
 	}
 
 	/**
 	 * getEventList
 	 * 
 	 * @return EventList
 	 */
 	public final Iterator<WebAppEvent> getEventListIterator() {
 		return events.iterator();
 	}
 
 	/**
 	 * getNumberOfEvents
 	 * 
 	 * @return NumberOfEvents
 	 */
 	public final int getNumberOfEvents() {
 		return events.size();
 	}
 
 	/**
 	 * getNumberOfCriticalEvents
 	 * 
 	 * @return NumberOfCriticalEvents
 	 */
 	public final int getNumberOfCriticalEvents() {
 		int i = 0;
 		Iterator<WebAppEvent> it = this.getEventListIterator();
 		WebAppEvent ev;
 		while (it.hasNext()) {
 			ev = it.next();
 			if (ev.isCritical()) {
 				i++;
 			}
 		}
 
 		return i;
 	}
 
 	/**
 	 * getPageLoadsAverage
 	 * 
 	 * @return PageLoadsAverage
 	 */
 	public final String getLoadsAverage() {
 		LoadTime plt = new LoadTime();
 		return plt.getLoadsAverage(this);
 	}
 
 	/**
 	 * getLatencyAverage
 	 * 
 	 * @return Latency average
 	 */
 	public final String getLatencyAverage() {
 		Latency ltc = new Latency();
 		return ltc.getLatencyAverage(this);
 	}
 
 	public final String getDataFileName() {
 		return this.name.replace(" ", "-") + filenameSuffix;
 	}
 
 	/**
 	 * getJSAnnotatedTimeLine
 	 * 
 	 * @return JSAnnotatedTimeLine
 	 */
 	public final String getGoogleAnnotatedTimeLine() {
 		Visualization gv = new Visualization();
 		gv.setAppVersion(appVersion);
 		return gv.getAnnotatedTimeLine(this);
 	}
 
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
 		if (this.getTempDir() != null) {
 			Visualization gv = new Visualization();
 			gv.setAppVersion(appVersion);
 			gv.writeWebAppVisualizationDataFile(this);
 		}
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
 		this.type = "webservice";
 	}
 
 	/**
 	 * setTypeWebApp
 	 */
 	public final void setTypeWebApp() {
 		this.type = "webapp";
 	}
 
 	/**
 	 * setTypeSQL
 	 */
 	public final void setTypeSQL() {
 		this.type = "sql";
 	}
 
 	/**
 	 * setTypeSocketService
 	 */
 	public final void setTypeSocketService() {
 		this.type = "socketservice";
 	}
 
 	/**
 	 * setTypeSSH
 	 */
 	public final void setTypeSSH() {
 		this.type = "ssh";
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
 
 	public final void setTempWorkingDir(String tempWorkingDir) {
 		this.tempWorkingDir = tempWorkingDir;
 	}
 
 	public final Double getPrevAvailability() {
 		return prevAvailability;
 	}
 
 	public final void setPrevAvailability(Double prevAvailability) {
 		this.prevAvailability = prevAvailability;
 	}
 
 	public final String getPrevLatency() {
 		return prevLatency;
 	}
 
 	// public void setPrevLatency(String prevLatency) {
 	// this.prevLatency = prevLatency;
 	// }
 
 	public final String getPrevLoad() {
 		return prevLoad;
 	}
 
 	// public void setPrevLoad(String prevLoad) {
 	// this.prevLoad = prevLoad;
 	// }
 
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
 		// int lastPos = groupsString.lastIndexOf(",");
 		int lastPos = groupsString.lastIndexOf(',');
 		groupsString = groupsString.substring(0, lastPos);
 
 		return groupsString;
 	}
 
 	/**
 	 * getLatencyTrend Slope coefficient
 	 */
 	public double getLatencyTrend() {
 
 		// Clean events with latency = 'undefined'
 		Iterator<WebAppEvent> it = events.iterator();
 		List<WebAppEvent> notUndefLatencyEvents = new ArrayList<WebAppEvent>();
 		WebAppEvent currEv;
 
 		while (it.hasNext()) {
 			currEv = it.next();
 			if (!currEv.getLatency().equalsIgnoreCase("undefined")) {
 				notUndefLatencyEvents.add(currEv);
 			}
 		}
 
 		int numberOfEvents = notUndefLatencyEvents.size();
 
 		int pos = 1;
 		double sX = 0;
 		double sY = 0;
 		double sXX = 0;
 		double sXY = 0;
 
 		for (int i = 0; i < numberOfEvents; i++) {
 			sX = sX + pos;
 			sY = sY
 					+ Double.valueOf(notUndefLatencyEvents.get(pos - 1)
 							.getLatency());
 			sXX = sXX + pos * pos;
 			sXY = sXY
 					+ pos
 					* Double.valueOf(notUndefLatencyEvents.get(pos - 1)
 							.getLatency());
 			pos++;
 		}
 
 		// slope coefficient
 		double sc = ((sX * sY) - (numberOfEvents * sXY))
 				/ ((sX * sX) - (numberOfEvents * sXX));
 
 		notUndefLatencyEvents = null;
 		if (sc == -0.0 || sc == 0.0) {
 			return 0;
 		} else {
 			return sc;
 		}
 	}
 
 	/**
 	 * getAvailabilityTrend Slope coefficient
 	 */
 	public double getAvailabilityTrend() {
 		int numberOfEvents = events.size();
 
 		int pos = 1;
 		double sX = 0;
 		double sY = 0;
 		double sXX = 0;
 		double sXY = 0;
 
 		for (int i = 0; i < numberOfEvents; i++) {
 			sX = sX + pos;
 			sY = sY + Double.valueOf(events.get(pos - 1).getAvailability());
 			sXX = sXX + pos * pos;
 			sXY = sXY + pos
 					* Double.valueOf(events.get(pos - 1).getAvailability());
 			pos++;
 		}
 
 		// slope coefficient
 		double sc = ((sX * sY) - (numberOfEvents * sXY))
 				/ ((sX * sX) - (numberOfEvents * sXX));
 
 		if (sc == -0.0 || sc == 0.0) {
 			return 0;
 		} else {
 			return sc;
 		}
 	}
 
 	/**
 	 * getLoadTimeTrend Slope coefficient
 	 */
 	public double getLoadTimeTrend() {
 		int numberOfEvents = events.size();
 
 		int pos = 1;
 		double sX = 0;
 		double sY = 0;
 		double sXX = 0;
 		double sXY = 0;
 
 		for (int i = 0; i < numberOfEvents; i++) {
 			sX = sX + pos;
 			sY = sY + Double.valueOf(events.get(pos - 1).getPageLoadTime());
 			sXX = sXX + pos * pos;
 			sXY = sXY + pos
 					* Double.valueOf(events.get(pos - 1).getPageLoadTime());
 			pos++;
 		}
 
 		// slope coefficient
 		double sc = ((sX * sY) - (numberOfEvents * sXY))
 				/ ((sX * sX) - (numberOfEvents * sXX));
 
 		if (sc == -0.0 || sc == 0.0) {
 			return 0;
 		} else {
 			return sc;
 		}
 
 	}
 
 	/**
 	 * initialize
 	 */
 	public final void initialize(String tempWorkingDir, String version) {
 		events = new ArrayList<WebAppEvent>();
 		setlastStatus("NA");
 		setTempWorkingDir(tempWorkingDir);
 		setAppVersion(version);
 		filenameSuffix = ".html";
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
 
 }
