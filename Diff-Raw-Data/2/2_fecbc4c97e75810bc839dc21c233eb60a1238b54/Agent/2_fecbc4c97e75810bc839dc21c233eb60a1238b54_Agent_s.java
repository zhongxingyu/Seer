  /*
  *  Adito
  *
  *  Copyright (C) 2003-2006 3SP LTD. All Rights Reserved
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 			
 package com.adito.agent.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import com.maverick.http.AuthenticationCancelledException;
 import com.maverick.http.AuthenticationPrompt;
 import com.maverick.http.GetMethod;
 import com.maverick.http.HttpAuthenticator;
 import com.maverick.http.HttpAuthenticatorFactory;
 import com.maverick.http.HttpClient;
 import com.maverick.http.HttpConnection;
 import com.maverick.http.HttpException;
 import com.maverick.http.HttpResponse;
 import com.maverick.http.PasswordCredentials;
 import com.maverick.http.URLDecoder;
 import com.maverick.http.UnsupportedAuthenticationException;
 import com.maverick.multiplex.MultiplexedConnection;
 import com.maverick.multiplex.MultiplexedConnectionListener;
 import com.maverick.multiplex.Request;
 import com.maverick.multiplex.RequestHandler;
 import com.maverick.ssl.SSLIOException;
 import com.maverick.ssl.SSLTransportFactory;
 import com.maverick.ssl.SSLTransportImpl;
 import com.maverick.ssl.https.HttpsURLStreamHandlerFactory;
 import com.maverick.util.ByteArrayReader;
 import com.maverick.util.ByteArrayWriter;
 import com.adito.agent.client.applications.ApplicationManager;
 import com.adito.agent.client.networkplaces.NetworkPlaceManager;
 import com.adito.agent.client.tunneling.DefaultTunnel;
 import com.adito.agent.client.tunneling.LocalTunnelServer;
 import com.adito.agent.client.tunneling.TunnelInactivityMonitor;
 import com.adito.agent.client.tunneling.TunnelManager;
 import com.adito.agent.client.util.BrowserLauncher;
 import com.adito.agent.client.util.FileCleaner;
 import com.adito.agent.client.util.IOStreamConnectorListener;
 import com.adito.agent.client.util.TunnelConfiguration;
 import com.adito.agent.client.util.URI;
 import com.adito.agent.client.util.Utils;
 import com.adito.agent.client.util.URI.MalformedURIException;
 import com.adito.agent.client.webforwards.WebForwardManager;
 
 /**
  * Concrete implementation of an {@link AbstractVPNClient} that runs as
  * standalone applicaiton launched from the Adito web interface (via the
  * <i>Laumcher</i> applet.
  * <p>
  * See package description for more information.
  */
 public class Agent implements RequestHandler, MultiplexedConnectionListener {
 	
 	/*
	 * Replaced by build
 	 */
 	public final static String AGENT_VERSION = "999.999.999";
 
 	/** The hostname of the Adito proxy * */
 	protected String aditoHostname;
 
 	/** The port of the Adito proxy * */
 	protected int aditoPort;
 	
 	/** Are we using a secure port? **/
 	protected boolean isSecure = true;
 
 	/** The username for this session * */
 	protected String username;
 
 	/** The VPN client ticket for an authenticated session * */
 	protected String ticket;
 
 	/** The hostname of the local HTTPS proxy server * */
 	protected URI localProxyURL;
 
 	/** We store all the available proxy information here * */
 	protected static Hashtable proxiesIE = new Hashtable();
 
 	/** We store all the local bypass addresses here * */
 	protected static Vector proxyBypassIE = new Vector();
 
 	protected static Hashtable proxiesFF = new Hashtable();
 
 	protected static Vector proxyBypassFF = new Vector();
 
 	/** HttpClient instances are cached * */
 
 	protected AgentConfiguration agentConfiguration;
 
 	protected String serverVersion;
 
 	protected HttpClient client;
 
 	protected String defaultProxyHost;
 
 	protected int defaultProxyPort = 80;
 
 	protected PasswordCredentials defaultProxyCredentials;
 
 	protected String defaultProxyPreferredAuthentication;
 
 	protected boolean autoDetectProxies = true;
 
 	protected AuthenticationPrompt defaultProxyAuthenticationPrompt;
 
     protected AuthenticationPrompt defaultAuthenticationPrompt;
 
 	protected int defaultProxyType = HttpClient.PROXY_HTTP;
 
 	protected ApplicationManager applicationManager;
 
 	protected TunnelManager tunnelManager;
 
 	protected WebForwardManager webForwardManager;
 
 	protected NetworkPlaceManager networkPlaceManager;
 
 	boolean ticketIsPassword;
 	
 	// Public statics
 
 	/**
 	 * Client is connected
 	 */
 	public final static int STATE_CONNECTED = 1;
 
 	/**
 	 * Client is disconnected
 	 */
 	public final static int STATE_DISCONNECTED = 2;
 
 	// Private instance variables
 
 	private int currentState = STATE_DISCONNECTED;
 
 	private TXRXMonitor txm;
 
 	private AgentClientGUI gui;
 
 	private Vector extensions = new Vector();
 
 	private TunnelInactivityMonitor inactivityMonitor;
 
 	private KeepAliveThread keepAlive;
 
 	// Statics
 
 	MultiplexedConnection con = null;
 
 	HttpConnection httpConnection = null;
 
 	public static final String SHUTDOWN_REQUEST = "shutdown";
 
 	public static final String OPEN_URL_REQUEST = "openURL";
 
 	public static final String MESSAGE_REQUEST = "agentMessage";
 
 	public static final String UPDATE_RESOURCES_REQUEST = "updateResources";
 
 	public static final String SYNCHRONIZED_REQUEST = "synchronized";
     
     // #ifdef DEBUG
 	static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
 			.getLog(Agent.class);
 	// #endif
 
 	static {
 		HttpClient.setUserAgent("Agent"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Set the URL of the HTTPS proxy server to use for all outgoing connections
 	 * 
 	 * @param localProxyURL
 	 *            local proxy URL
 	 * @throws URI.MalformedURIException
 	 */
 	public void setLocalProxyURL(String localProxyURL)
 			throws URI.MalformedURIException {
 		this.localProxyURL = new URI(localProxyURL);
 
 		// FIXME What about HttpsURLConnection?
 	}
 
 	/**
 	 * Get the application manager for this agent.
 	 * 
 	 * @return application manager
 	 */
 	public ApplicationManager getApplicationManager() {
 		return applicationManager;
 	}
 
 	/**
 	 * Get the tunnel manager for this agent
 	 * 
 	 * @return tunnel mananger
 	 */
 	public TunnelManager getTunnelManager() {
 		return tunnelManager;
 	}
 
 	/**
 	 * Get the network place manager for this agent
 	 * 
 	 * @return network place manager
 	 */
 	public NetworkPlaceManager getNetworkPlaceManager() {
 		return networkPlaceManager;
 	}
 
 	/**
 	 * Get the version of the server this agent is connected to. Will be
 	 * <code>null</code> until connected.
 	 * 
 	 * @return server version
 	 */
 	public String getServerVersion() {
 		return serverVersion;
 	}
 
 	/**
 	 * Get the client version. This will be 999.999.999 when running
 	 * directly from classes or the agent extension version when
 	 * running from deployed version. 
 	 * 
 	 * @return client version
 	 */
 	public String getClientVersion() {
 		return AGENT_VERSION;
 	}
 
 	public MultiplexedConnection getConnection() {
 		return con;
 	}
 
 	/**
 	 * Get the hostname of the Adito proxy
 	 * 
 	 * @return hostname
 	 */
 	public String getAditoHost() {
 		return aditoHostname;
 	}
 
 	public String getTicket() {
 		return ticket;
 	}
 
 	/**
 	 * Get the port of the Adito proxy
 	 * 
 	 * @return port
 	 */
 	public int getAditoPort() {
 		return aditoPort;
 	}
 	
 	public boolean isSecure() {
 		return isSecure;
 	}
 
 	/**
 	 * Get the username for this session
 	 * 
 	 * @return username
 	 */
 	public String getUsername() {
 		return username;
 	}
 
 	public void onConnectionClose() {
 		currentState = STATE_DISCONNECTED;
 		if (getConfiguration().isSystemExitOnDisconnect()) {
 			startShutdownProcedure();
 		} else {
 			getGUI().showDisconnected();
 		}
 	}
 
 	public void onConnectionOpen() {
 		currentState = STATE_CONNECTED;
 	}
 
 	/**
 	 * Configure the proxy server using the currently set <i>Local Proxy URL</i>
 	 * (set by {@link #setLocalProxyURL(String)}.
 	 */
 	public void configureProxy() {
 
 		// Configure local proxy support
 		if (localProxyURL != null && !localProxyURL.equals("")) { //$NON-NLS-1$
 			// #ifdef DEBUG
 			log.info("Configuring HTTP proxy to "
 					+ obfuscateURL(localProxyURL.toString()));
 			// #endif
 			String userInfo = localProxyURL.getUserinfo();
 			String user = ""; //$NON-NLS-1$
 			String password = ""; //$NON-NLS-1$
 			if (userInfo != null && !userInfo.equals("")) { //$NON-NLS-1$
 				int idx = userInfo.indexOf(':');
 				user = URLDecoder.decode(userInfo);
 				if (idx != -1) {
 					password = URLDecoder.decode(userInfo.substring(idx + 1));
 					user = URLDecoder.decode(userInfo.substring(0, idx));
 				}
 			}
 			int port = localProxyURL.getPort();
 			setDefaultProxyType(localProxyURL.getScheme().equals("https") ? HttpClient.PROXY_HTTPS : HttpClient.PROXY_HTTP); //$NON-NLS-1$
 			setDefaultProxyHost(localProxyURL.getHost());
 			setDefaultProxyPort(port == -1 ? 80 : port);
 			if (!user.equals("")) { //$NON-NLS-1$
 				setDefaultProxyCredentials(new PasswordCredentials(user,
 						password));
 			}
 			if (localProxyURL.getQueryString() != null) {
 				setDefaultProxyPreferredAuthentication(localProxyURL
 						.getQueryString());
 			}
 		}
 		if (localProxyURL != null && !localProxyURL.equals("")) { //$NON-NLS-1$
 			setDefaultProxyAuthenticationPrompt(gui);
 		}
 
 	}
 
 	/**
 	 * Get the {@link HttpClient} that is being used by this VPN client for
 	 * communication with Adito.
 	 * 
 	 * @return http client
 	 */
 	private synchronized HttpClient getHttpClient() {
 		if (client == null) {
 			// #ifdef DEBUG
 			log.info("Creating HttpClient instance"); //$NON-NLS-1$
 			// #endif
 			client = new HttpClient(aditoHostname, aditoPort, isSecure);
 			client.setAuthenticationPrompt(defaultAuthenticationPrompt != null ? defaultAuthenticationPrompt : getGUI());
 			if (defaultProxyHost != null && !defaultProxyHost.equals("")) { //$NON-NLS-1$
 				// #ifdef DEBUG
 				log.info("Configuring proxies for HttpClient instance"); //$NON-NLS-1$
 				// #endif
 				client
 						.setProxyAuthenticationPrompt(defaultProxyAuthenticationPrompt);
 				client.setProxyHost(defaultProxyHost);
 				client.setProxyPort(defaultProxyPort);
 				client.setProxyType(defaultProxyType);
 
 				if (defaultProxyCredentials != null
 						&& defaultProxyCredentials.getUsername() != null
 						&& !defaultProxyCredentials.getUsername().equals("")) { //$NON-NLS-1$
 					/**
 					 * LDP - This used to set preemptive authentication but its
 					 * causing problems with NTLM
 					 */
 					client.setProxyCredentials(defaultProxyCredentials);
 
 					/**
 					 * LDP - It seems preemptive might be needed for Basic
 					 * authentication. There are lots of users seeing
 					 * EOFExceptions after 407 proxy authentication required.
 					 */
 					if ("BASIC"
 							.equalsIgnoreCase(defaultProxyPreferredAuthentication)) {
 						client.setProxyPreemptiveAuthentication(true);
 					}
 				}
 				client
 						.setProxyPreferedAuthentication("AUTO".equalsIgnoreCase(defaultProxyPreferredAuthentication) ? null //$NON-NLS-1$
 								: defaultProxyPreferredAuthentication);
 			}
 		}
 
 		return client;
 	}
 
 	/**
 	 * Constructor preventing direct instantiation.
 	 */
 	public Agent(AgentConfiguration agentConfiguration) {
 		this.agentConfiguration = agentConfiguration;
 
 		// Forced
 		if (getConfiguration().getGUIClass() != null) {
 			try {
 				gui = (AgentClientGUI) Class.forName(
 						getConfiguration().getGUIClass()).newInstance();
 			} catch (Exception e) {
 				//#ifdef DEBUG
 				log.error("Failed to create GUI", e);
 				//#endif
 			}
 		}
 
 		// 
 		if (gui == null) {
 
 			// SWT
 			try {
 				Class.forName("org.eclipse.swt.widgets.Display");
 				gui = (AgentClientGUI) Class
 						.forName(
 								"com.adito.agent.client.gui.swt.SWTSystemTrayGUI")
 						.newInstance();
 			} catch (Exception e) {
 				//#ifdef DEBUG
 				log.debug("Failed to create SWT GUI", e);
 				//#endif
 			}
 
 			// 
 			if (gui == null) {
 
 				// JDK6
 				if (Utils.checkVersion("1.6")) { //$NON-NLS-1$
 					try {
 						// yay, finally agent tray icon for linux
 						gui = (AgentClientGUI) Class
 								.forName(
 										"com.adito.agent.client.gui.awt.JDK6SystemTrayGUI")
 								.newInstance();
 					} catch (Exception e) {
 						//#ifdef DEBUG
 						log.debug("Failed to create JDK6 GUI", e);
 						//#endif
 					}
 				}
 
 				// Systray4j
 				if (gui == null
 						&& Utils.checkVersion("1.2") && System.getProperty("os.name").startsWith("Windows") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						&& !System
 								.getProperty("os.name").startsWith("Windows 98") //$NON-NLS-1$ //$NON-NLS-2$
 						&& !System
 								.getProperty("os.name").startsWith("Windows 95") //$NON-NLS-1$ //$NON-NLS-2$
 						&& !System
 								.getProperty("os.name").startsWith("Windows ME")) { //$NON-NLS-1$ //$NON-NLS-2$
 					try {
 						gui = (AgentClientGUI) Class
 								.forName(
 										"com.adito.agent.client.gui.awt.SystemTrayGUI")
 								.newInstance();
 					} catch (Exception e) {
 						//#ifdef DEBUG
 						log.debug("Failed to create JDIC GUI", e);
 						//#endif
 					}
 				}
 
 				// Fallback to basic frame GUI
 				if (gui == null) {
 					try {
 						gui = (AgentClientGUI) Class
 								.forName(
 										"com.adito.agent.client.gui.awt.BasicFrameGUI")
 								.newInstance();
 					} catch (Exception e) {
 						//#ifdef DEBUG
 						log.debug("Failed to create basic GUI", e);
 						//#endif
 					}
 
 					// No GUI? Probably embedded so use a dummy GUI
 					if (gui == null) {
 						gui = new DummyGUI();
 					}
 				}
 			}
 		}
 	}
 
 	// /**
 	// * Get the static instance of the {@link Agent}.
 	// *
 	// * @return instance
 	// */
 	// public static Agent getVPN() {
 	// if (vpn == null) {
 	// vpn = new Agent();
 	// }
 	// return vpn;
 	// }
 
 	/**
 	 * Get the current state. Will be one of {@link #STATE_DISCONNECTED} or
 	 * {@link #STATE_CONNECTED}.
 	 * 
 	 * @return state
 	 */
 	public int getState() {
 		return currentState;
 	}
 
 	/**
 	 * Get the object the stores various configuration options
 	 * 
 	 * @return configuration
 	 */
 	public AgentConfiguration getConfiguration() {
 		return agentConfiguration;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adito.vpn.base.AbstractVPNClient#init(java.lang.String,
 	 *      int, java.lang.String, java.lang.String)
 	 */
 	public void init() throws SecurityException, IOException {
 
         // Create connection object now so users of API can add connection listeners before connecting
 	    con = new MultiplexedConnection(new AgentChannelFactory(
                 this));
         
 		// #ifdef DEBUG
 		log.info("Allow untrusted hosts is set to " + System.getProperty("com.maverick.ssl.allowUntrustedCertificates", "false")); //$NON-NLS-1$ //$NON-NLS-2$
 		log.info("Cache directory is " + getConfiguration().getCacheDir());
 		// #endif
 
 		// #ifdef DEBUG
 		log.info("Initialising GUI"); //$NON-NLS-1$
 		// #endif
 		gui.init(this);
 		disconnected();
 
 		// Install and configure HTTP / HTTPS support
 		// #ifdef DEBUG
 		log.info("Installing Maverick SSL support for HTTPSURLStreamHandler"); //$NON-NLS-1$
 		// #endif
 
 		try {
 			HttpsURLStreamHandlerFactory.addHTTPSSupport();
 			configureProxy();
 		} catch (SecurityException se) {
 			disconnected();
 			throw se;
 		} catch (IOException ioe) {
 			disconnected();
 			throw ioe;
 		}
 	}
 
 	public void connect(String aditoHostname, int aditoPort,
 			boolean isSecure, String username, String ticket, boolean ticketIsPassword)
 			throws IOException, HttpException,
 			UnsupportedAuthenticationException,
 			AuthenticationCancelledException {
 
 		this.aditoHostname = aditoHostname;
 		this.aditoPort = aditoPort;
 		this.username = username;
 		this.ticket = ticket;
 		this.ticketIsPassword = ticketIsPassword;
 		this.isSecure = isSecure;
 
 		inactivityMonitor = new TunnelInactivityMonitor(this);
 		keepAlive = new KeepAliveThread();
 
 		gui.showTxRx();
 
 		connectAgent();
 
 		// Start the Tx/Rx monitor so we have some animated icons
 		txm = new TXRXMonitor(this);
 		txm.start();
 
 		gui.showIdle();
 		updateInformation();
 
 		inactivityMonitor.start();
 
 		if (getConfiguration().getKeepAlivePeriod() > 0)
 			keepAlive.start();
 
 		if (getConfiguration().isDisplayInformationPopups()) {
 			getGUI().popup(null, Messages.getString("VPNClient.nowRunning"), //$NON-NLS-1$   
 					Messages.getString("VPNClient.title"), "popup-agent", 5000); //$NON-NLS-1$
 		}
 		
 		try {
 			// Give the popup message some breathing space
 			// otherwise in SWT we loose it :(
 			Thread.sleep(3000);
 		} catch(InterruptedException ex) { }
 
 	}
 
 	public boolean processRequest(Request request, MultiplexedConnection con) {
 
 		if (request.getRequestName().equals(SHUTDOWN_REQUEST)) {
 			disconnectAgent();
 			return true;
 		} else if (request.getRequestName().equals(MESSAGE_REQUEST)) {
 			displayMessage(request);
 			return true;
 		} else if (request.getRequestName().equals(UPDATE_RESOURCES_REQUEST)) {
 			final ByteArrayReader bar = new ByteArrayReader(request.getRequestData());
 			Thread t = new Thread() {
 				public void run() {
 					try {
 						updateResources((int)bar.readInt());
 					}
 					catch(IOException e) {				
 					}
 				}
 			};
 			t.start();
 			return true;
 		} else if (request.getRequestName().equals(OPEN_URL_REQUEST)
 				&& request.getRequestData() != null) {
 			try {
 				ByteArrayReader msg = new ByteArrayReader(request
 						.getRequestData());
 				openURL(msg.readString(), msg.readString(), request);
 				return true;
 			} catch (IOException e) {
 				// #ifdef DEBUG
 				log.error("Failed to process openURL request", e);
 				// #endif
 				return false;
 			}
 		} else
 			return false;
 	}
 
 	public void postReply(MultiplexedConnection connection) {
 	}
 
 	private void displayMessage(Request request) {
 
 		try {
 			if (request.getRequestData() != null) {
 				ByteArrayReader msg = new ByteArrayReader(request
 						.getRequestData());
 				String title = msg.readString();
 				msg.readInt(); // type
 				String message = msg.readString();
 
 				getGUI().popup(
 						null,
 						MessageFormat.format(Messages
 								.getString("Agent.received"), new Object[] {
 								SimpleDateFormat.getDateTimeInstance().format(
 										new Date()), message }),
 						title.equals("") ? Messages
 								.getString("Agent.promptTitle") : title,
 						"popup-mail", 0);
 			}
 		} catch (IOException e) {
 			// #ifdef DEBUG
 			log.error("Failed to read message data from popup message request",
 					e);
 			// #endif
 		}
 
 	}
 
 	private boolean openURL(String link, String launchId, Request request) {
 
 		// #ifdef DEBUG
 		log.info("Request to open " + link + " via a temporary tunnel"); //$NON-NLS-1$ //$NON-NLS-2$
 		// #endif
 
 		// Parse the link address and create a new address for the browser
 		// to use
 		try {
 			URI url = new URI(link);
 			String linkHost = url.getHost();
 			int linkPort = url.getPort() == -1 ? (url.getScheme()
 					.equalsIgnoreCase("https") ? 443 : 80) : url.getPort(); //$NON-NLS-1$
 
 			// Start a tunnel
 
 			DefaultTunnel t = new DefaultTunnel(-1,
 					TunnelConfiguration.LOCAL_TUNNEL,
 					TunnelConfiguration.TCP_TUNNEL, null, 0, linkPort,
 					linkHost, false, false, linkHost + ":" + linkPort, launchId);
 
 			LocalTunnelServer listener = getTunnelManager().startLocalTunnel(t);
 
 			// #ifdef DEBUG
 			log.info("Tunneled web forward listener started on port "
 					+ listener.getLocalPort());
 			// #endif
 
 			ByteArrayWriter w = new ByteArrayWriter();
 			w.writeInt(listener.getLocalPort());
 			request.setRequestData(w.toByteArray());
 			return true;
 		} catch (Exception e) {
 			// #ifdef DEBUG
 			log.error("Failed to process openURL request", e);
 			// #endif
 		}
 		return false;
 	}
 
 	/**
 	 * Get the current GUI.
 	 * 
 	 * @return gui
 	 */
 	public AgentClientGUI getGUI() {
 		return gui;
 	}
 
 	public void updateInformation() {
 
 		// String msg = ""; //$NON-NLS-1$
 		// int count = getActiveDirectTunnels().size()
 		// + activeForwardingTunnels.size();
 		//
 		// for (Enumeration e = remoteListeners.elements();
 		// e.hasMoreElements();) {
 		// MultiplexedConnection con = (MultiplexedConnection) e.nextElement();
 		// count += con.getActiveChannels().length;
 		// }
 		// int ports = activeListeners.size() + remoteListeners.size();
 		// if (ports > 0) {
 		// if (count > 0) {
 		// msg = MessageFormat
 		// .format(
 		// Messages
 		// .getString("VPNClient.information.active"), new Object[] { new
 		// Integer(ports), new Integer(count) }); //$NON-NLS-1$
 		// } else {
 		// msg = MessageFormat
 		// .format(
 		// Messages
 		// .getString("VPNClient.information.notActive"), new Object[] { new
 		// Integer(ports) }); //$NON-NLS-1$
 		// }
 		// } else if (count > 0) {
 		// msg = MessageFormat
 		// .format(
 		// Messages
 		// .getString("VPNClient.information.remainingConnections"), new
 		// Object[] { new Integer(count) }); //$NON-NLS-1$
 		//
 		// }
 		//
 		// if (msg.equals("")) { //$NON-NLS-1$
 		// msg = Messages.getString("VPNClient.information.idle"); //$NON-NLS-1$
 		// }
 		//
 		// gui.setInfo(msg);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.adito.vpn.base.AbstractVPNClient#getTXIOListener()
 	 */
 	public IOStreamConnectorListener getTXIOListener() {
 		return txm != null ? txm.getTxListener() : null;
 	}
 
 	public IOStreamConnectorListener getRXIOListener() {
 		return txm != null ? txm.getRxListener() : null;
 	}
 
 	/**
 	 * Get the full URL to use for a redirect given the path. The appropriate
 	 * hostname and port will automatically be added.
 	 * 
 	 * @param path
 	 *            path
 	 * @return full URL including appropriate host and port
 	 */
 	protected String getRedirectUrl(String path) {
 		try {
 			new URL(path);
 			return path;
 		} catch (Exception e) {
 			return "https://" + getAditoHost() + ":" + getAditoPort() + path; //$NON-NLS-1$  //$NON-NLS-2$
 		}
 	}
 
 	protected void startExtensions(String extensionClasses) {
 		StringTokenizer classes = new StringTokenizer(extensionClasses, ","); //$NON-NLS-1$
 		while (classes.hasMoreTokens()) {
 			String cls = classes.nextToken();
 			try {
 				Class agent = Class.forName(cls);
 				AgentExtension ext = (AgentExtension) agent.newInstance();
 				// #ifdef DEBUG
 				log.info("Starting " + ext.getName());
 				// #endif
 				ext.init(this);
 				extensions.addElement(ext);
 			} catch (Exception e1) {
 				// #ifdef DEBUG
 				log.info("Unable to start extension " + cls, e1);
 				// #endif
 				continue;
 			}
 		}
 	}
 
 	private void connectAgent() throws IOException, HttpException,
 			UnsupportedAuthenticationException,
 			AuthenticationCancelledException {
 
 		HttpResponse response = null;
 		HttpAuthenticator authenticator = null;
 		String ticketToSend = ticket;
 		
 		boolean doPreemptive = ticketIsPassword;
 
 		try {
             
 			for (int i = 0; i < 3; i++) {
 
 	            HttpClient client = getHttpClient();
 	            if (doPreemptive) {
 	                client.setCredentials(new PasswordCredentials(username,
 	                        ticket));
 	                client.setPreferredAuthentication("Basic");
 	            } 
 	            else {
 	                client.setCredentials(null);
 	            }
 
 				// #ifdef DEBUG
 				log.info("Registering with the server"); //$NON-NLS-1$
 				log.info("Server is " + (isSecure ? "https://" : "http://") + getAditoHost() //$NON-NLS-1$
 						+ ":" + getAditoPort()); //$NON-NLS-1$
 				// #endif
 				GetMethod post = new GetMethod("/agent"); //$NON-NLS-1$
 
 	            client.setPreemtiveAuthentication(doPreemptive);
 				if (!doPreemptive && ticket != null) {
 					post.setParameter("ticket", ticket); //$NON-NLS-1$
 				}
 
 				post.setParameter(
 						"agentType", getConfiguration().getAgentType()); //$NON-NLS-1$ //$NON-NLS-2$
 				post.setParameter("locale", Locale.getDefault().toString()); //$NON-NLS-1$
 
 				response = client.execute(post);
 
 				if (response.getStatus() == 302) {
 					// Reset the client
 					this.client = null;
 
 					URL url = new URL(response.getHeaderField("Location")); //$NON-NLS-1$
 					aditoHostname = url.getHost();
 					if (url.getPort() > 0)
 						aditoPort = url.getPort();
 					continue;
 				} else if (response.getStatus() == 200) {
 					con.addListener(this);
 					httpConnection = response.getConnection(); // Preserve the
 					// connection
 					con.registerRequestHandler(MESSAGE_REQUEST, this);
 					con.registerRequestHandler(SHUTDOWN_REQUEST, this);
 					con.registerRequestHandler(OPEN_URL_REQUEST, this);
 					con.registerRequestHandler(UPDATE_RESOURCES_REQUEST, this);
 
 					// Start the protocol
 
 					con.startProtocol(
 							response.getConnection().getInputStream(), response
 									.getConnection().getOutputStream(), true);
 
 					// Synchronize and read back server information
 					Request syncRequest = new Request(SYNCHRONIZED_REQUEST);
 					con.sendRequest(syncRequest, true);
 					if (syncRequest.getRequestData() == null)
 						throw new IOException(
 								"Server failed to return version data");
 
 					ByteArrayReader reader = new ByteArrayReader(syncRequest
 							.getRequestData());
 					serverVersion = reader.readString();
 
 					/**
 					 * Initialize the managers. Tunnels are no longer recorded
 					 * here unless they are active. This simplifies the agent by
 					 * making it respond to start and stop requests from the new
 					 * persistent connection with Adito.
 					 */
 					tunnelManager = new TunnelManager(this);
 					applicationManager = new ApplicationManager(this);
 					webForwardManager = new WebForwardManager(this);
 					networkPlaceManager = new NetworkPlaceManager(this);
 					updateResources(-1);
 					return;
 				} else if (response.getStatus() == 401) {
 					authenticator = HttpAuthenticatorFactory
 							.createAuthenticator(
 									response.getConnection(),
 									response
 											.getHeaderFields("WWW-Authenticate"),
 									"WWW-Authenticate", "Authorization",
 									HttpAuthenticatorFactory.BASIC, post
 											.getURI());
 					if (authenticator.wantsPrompt()) {
 						if ( !( defaultAuthenticationPrompt != null ? defaultAuthenticationPrompt.promptForCredentials(false, authenticator) :
 						    getGUI().promptForCredentials(false, authenticator) ) ) {
 							throw new AuthenticationCancelledException();
 						}
 					}
 				} else if(response.getStatus() == 403) {
 				    if(doPreemptive || ticket != null) {
 				        doPreemptive = false;
 				        ticket = null;
 				    }
 				    else {
     					throw new IOException(MessageFormat.format(Messages
     							.getString("VPNClient.register.failed"),
     							new Object[] {
     									String.valueOf(response.getStatus()),
     									response.getReason() }));
 				    }
 				}
 			}
 
 			throw new IOException(Messages
 					.getString("VPNClient.register.tooManyRedirects")); //$NON-NLS-1$
 		} catch (IOException ioe) {
 			disconnected();
 			throw ioe;
 		} catch (HttpException httpe) {
 			disconnected();
 			throw httpe;
 		} catch (UnsupportedAuthenticationException uae) {
 			disconnected();
 			throw uae;
 		} catch (AuthenticationCancelledException ace) {
 			disconnected();
 			throw ace;
 		}
 
 	}
     
     public boolean isConnected() {
         return con != null && con.isRunning();
     }
 
 	public void disconnect() {
 		if (isConnected()) {
 			disconnectAgent();
 		}
 	}
 
 	/**
 	 * Shutdown the Agent.
 	 * 
 	 * @param deregister
 	 *            deregister the agent
 	 * @param threadDeRegister
 	 *            deregister in a thread
 	 */
 	public void startShutdownProcedure() {
 		// #ifdef DEBUG
 		log.info("Starting agent shutdown procedure."); //$NON-NLS-1$
 		// #endif
 
 		if (getConfiguration().isDisplayInformationPopups()) {
 			getGUI()
 					.popup(
 							null,
 							Messages.getString("VPNClient.shutdown.popupText"), Messages.getString("VPNClient.title"), "popup-agent", -1); //$NON-NLS-1$  //$NON-NLS-2$
 		}
 
 		AgentExtension ext;
 		for (Enumeration e = extensions.elements(); e.hasMoreElements();) {
 			ext = (AgentExtension) e.nextElement();
 
 			// #ifdef DEBUG
 			log.info("Stopping extension " + ext.getName()); //$NON-NLS-1$
 			// #endif
 			ext.exit();
 		}
 
 		getGUI().showDisconnected();
 
 		FileCleaner.deleteAllFiles();
 		
 		if (getConfiguration().isCleanOnExit()) {
 			if (Utils.isSupportedPlatform("Windows")) {
 				cleanupWindowsAgent();
 			} else if (Utils.isSupportedPlatform("Linux")) {
 				cleanupLinuxAgent();
 			} else {
 				// #ifdef DEBUG
 				log
 						.info("Agent cleanOnExit is not supported on this platform."); //$NON-NLS-1$
 				// #endif
 
 			}
 		}
 
 		if (getConfiguration().isSystemExitOnDisconnect()) {
 			scheduleExit();
 		} else {
 			gui.dispose();
 		}
 	}
 
 	/**
 	 * Get the default credentials used for proxy server authentication. By
 	 * default this will be <code>null</code>
 	 * 
 	 * @return default credentials used for proxy server authentication.
 	 */
 	public PasswordCredentials getDefaultProxyCredentials() {
 		return defaultProxyCredentials;
 	}
 
 	/**
 	 * Set the default credentials used for proxy server authentication.
 	 * 
 	 * @param defaultProxyCredentials
 	 *            default credentials used for proxy server authentication.
 	 */
 	public void setDefaultProxyCredentials(
 			PasswordCredentials defaultProxyCredentials) {
 		this.defaultProxyCredentials = defaultProxyCredentials;
 	}
 
 	/**
 	 * Get the default hostname for the proxy server to use. Use
 	 * <code>null</code> for no proxy server (the default).
 	 * 
 	 * @return proxy hostname or <code>null</code> for no proxy
 	 */
 	public String getDefaultProxyHost() {
 		return defaultProxyHost;
 	}
 
 	/**
 	 * Set the default hostname for the proxy server to use. Use
 	 * <code>null</code> for no proxy server (the default).
 	 * 
 	 * @param defaultProxyHost
 	 *            proxy hostname or <code>null</code> for no proxy
 	 */
 	public void setDefaultProxyHost(String defaultProxyHost) {
 		this.defaultProxyHost = defaultProxyHost;
 	}
 
 	/**
 	 * Set the default proxy port. By default this is <i>80</i>.
 	 * 
 	 * CHECK Why 80? IIS?
 	 * 
 	 * @return default proxy port number
 	 */
 	public int getDefaultProxyPort() {
 		return defaultProxyPort;
 	}
 
 	/**
 	 * Set the default proxy port. By default this is <i>80</i>.
 	 * 
 	 * CHECK Why 80? IIS?
 	 * 
 	 * @param defaultProxyPort
 	 *            default proxy port number
 	 */
 	public void setDefaultProxyPort(int defaultProxyPort) {
 		this.defaultProxyPort = defaultProxyPort;
 	}
 
 	/**
 	 * Get the default proxy type. This may be one of
 	 * {@link HttpClient#PROXY_HTTP}, {@link HttpClient#PROXY_HTTPS} or
 	 * {@link HttpClient#PROXY_NONE}. By default this is
 	 * {@link HttpClient#PROXY_HTTP}.
 	 * 
 	 * @return default proxy type
 	 */
 	public int getDefaultProxyType() {
 		return defaultProxyType;
 	}
 
 	/**
 	 * Set the default proxy type. This may be one of
 	 * {@link HttpClient#PROXY_HTTP}, {@link HttpClient#PROXY_HTTPS} or
 	 * {@link HttpClient#PROXY_NONE}. By default this is
 	 * {@link HttpClient#PROXY_HTTP}.
 	 * 
 	 * @param defaultProxyType
 	 *            default proxy type
 	 */
 	public void setDefaultProxyType(int defaultProxyType) {
 		this.defaultProxyType = defaultProxyType;
 	}
 
 	/**
 	 * Get the default preferred authentication type to use for proxy servers.
 	 * This may be one of {@link HttpAuthenticatorFactory#BASIC},
 	 * {@link HttpAuthenticatorFactory#NTLM},
 	 * {@link HttpAuthenticatorFactory#DIGEST},
 	 * {@link HttpAuthenticatorFactory#NONE} or <code>null</code> (Automatic).
 	 * The string value <i>"AUTO"</i> may be used instead of <code>null</code>.
 	 * 
 	 * @return default proxy authentication type
 	 */
 	public String getDefaultProxyPreferredAuthentication() {
 		return defaultProxyPreferredAuthentication;
 	}
 
 	/**
 	 * Set the default preferred authentication type to use for proxy servers.
 	 * This may be one of {@link HttpAuthenticatorFactory#BASIC},
 	 * {@link HttpAuthenticatorFactory#NTLM},
 	 * {@link HttpAuthenticatorFactory#DIGEST},
 	 * {@link HttpAuthenticatorFactory#NONE} or <code>null</code> (Automatic).
 	 * The string value <i>"AUTO"</i> may be used instead of <code>null</code>.
 	 * 
 	 * @param defaultProxyPreferredAuthentication
 	 *            default proxy authentication type
 	 */
 	public void setDefaultProxyPreferredAuthentication(
 			String defaultProxyPreferredAuthentication) {
 		this.defaultProxyPreferredAuthentication = defaultProxyPreferredAuthentication;
 	}
 
 	/**
 	 * Get the {@link AuthenticationPrompt} that will be used if the proxy
 	 * server requires authentication. This may happen if the default
 	 * authentication details have not been set using the methods in the class
 	 * or if those details are incorrect and the proxy server is requesting
 	 * again.
 	 * <p>
 	 * The prompt may for example display a GUI dialog box that will ask the
 	 * user for details or it may retrieve them from somewhere else such as a
 	 * password database.
 	 * <p>
 	 * If <code>null</code> is returned then no authenitcation prompt is set.
 	 * 
 	 * @return authentication prompt
 	 */
 	public AuthenticationPrompt getDefaultProxyAuthenticationPrompt() {
 		return defaultProxyAuthenticationPrompt;
 	}
 
 	/**
 	 * Set the {@link AuthenticationPrompt} that will be used if the proxy
 	 * server requires authentication. This may happen if the default
 	 * authentication details have not been set using the methods in the class
 	 * or if those details are incorrect and the proxy server is requesting
 	 * again.
 	 * <p>
 	 * The prompt may for example display a GUI dialog box that will ask the
 	 * user for details or it may retrieve them from somewhere else such as a
 	 * password database.
 	 * <p>
 	 * If <code>null</code> is returned then no authenitcation prompt is set.
 	 * 
 	 * @param defaultProxyAuthenticationPrompt
 	 *            authentication prompt
 	 */
 	public void setDefaultProxyAuthenticationPrompt(
 			AuthenticationPrompt defaultProxyAuthenticationPrompt) {
 		this.defaultProxyAuthenticationPrompt = defaultProxyAuthenticationPrompt;
 	}
 
     /**
      * Set the {@link AuthenticationPrompt} that will be used if the Adito
      * server requires authentication. This may happen if the default
      * authentication details have not been set using the methods in the class
      * or if those details are incorrect and the server is requesting
      * again.
      * <p>
      * The prompt may for example display a GUI dialog box that will ask the
      * user for details or it may retrieve them from somewhere else such as a
      * password database.
      * 
      * @param defaultAuthenticationPrompt
      *            authentication prompt
      */
     public void setDefaultAuthenticationPrompt(
             AuthenticationPrompt defaultAuthenticationPrompt) {
         this.defaultAuthenticationPrompt = defaultAuthenticationPrompt;
     }
 
 	/**
 	 * Utility method to hide any passwords that may be present in the userinfo
 	 * portion of a URL.
 	 * 
 	 * @param originalUrl
 	 *            original url
 	 * @return obfuscated url
 	 */
 	public static String obfuscateURL(String originalUrl) {
 		try {
 			URI url = new URI(originalUrl);
 			String userInfo = url.getUserinfo();
 			String user = ""; //$NON-NLS-1$
 			if (userInfo != null && !userInfo.equals("")) { //$NON-NLS-1$
 				int idx = userInfo.indexOf(':');
 				user = userInfo;
 				if (idx != -1) {
 					user = userInfo.substring(0, idx);
 				}
 				return url.getScheme()
 						+ "://" + user + ":***@" + url.getHost() + ":" + url.getPort() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						+ (url.getQueryString() != null ? ("?" + url.getQueryString()) : ""); //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				return originalUrl;
 			}
 
 		} catch (URI.MalformedURIException e) {
 			return originalUrl;
 		}
 	}
 
 	/**
 	 * Get a <code>Properties</code> that may be sent back to the server so it
 	 * can check if the client is in a supported environment. For example, the
 	 * sun.os.patch.level property may be used to deny clients that are running
 	 * Windows SP1.
 	 * 
 	 * @return system properties
 	 */
 	public static Properties getSystemPropertiesToSend() {
 		Properties p = new Properties();
 		setIfNotEmpty("java.version", p); //$NON-NLS-1$
 		setIfNotEmpty("java.vendor", p); //$NON-NLS-1$
 		setIfNotEmpty("sun.os.patch.level", p); //$NON-NLS-1$
 		setIfNotEmpty("os.name", p); //$NON-NLS-1$
 		setIfNotEmpty("os.version", p); //$NON-NLS-1$
 		setIfNotEmpty("os.arch", p); //$NON-NLS-1$
 		return p;
 	}
 
 	protected static void setIfNotEmpty(String name, Properties p) {
 		String v = System.getProperty(name);
 		if (v != null && !v.equals("")) { //$NON-NLS-1$
 			p.put(name, v);
 		}
 	}
 
 	private static String getCommandLineValue(String arg) {
 		int idx = arg.indexOf('=');
 		if (idx > -1)
 			return arg.substring(idx + 1);
 		else
 			return arg;
 	}
 
 	/**
 	 * Entry point.
 	 * 
 	 * @param args arguments
 	 */
 	public static void main(String[] args) throws Throwable {
 		try {
 			// #ifdef DEBUG
 			org.apache.log4j.BasicConfigurator.configure();
 			// #endif
 
             AgentArgs agentArgs = Agent.initArgs(args);
             
             Agent agent = Agent.initAgent(agentArgs);
             
             if (agentArgs.isDisableNewSSLEngine())
                 SSLTransportFactory.setTransportImpl(SSLTransportImpl.class);
 
 			agent.initMain(agentArgs.getHostname(), agentArgs.getPort(), agentArgs.isSecure(), agentArgs.getUsername(), agentArgs.getPassword(), agentArgs.getTicket());
 
 			if (agentArgs.getExtensionClasses() != null)
 				agent.startExtensions(agentArgs.getExtensionClasses());
 		} catch (Throwable t) {
 			// Catch any nasties to make sure we exit
 			// #ifdef DEBUG
 			if (log != null) {
 				log.error("Critical error, shutting down.", t); //$NON-NLS-1$
 			} else {
 				System.err.println("Critical error, shutting down.");
 				t.printStackTrace();
 			}
 			// #endif
 			throw t;
 		}
 	}
     
     protected static AgentArgs initArgs(String[] args) throws Throwable {
         int shutdown = -1;
         int webforwardInactivity = 300000;
         int tunnelInactivity = 600000;
 
         AgentArgs agentArgs = new AgentArgs();
         AgentConfiguration configuration = new AgentConfiguration();
 
         String hostHeader = null;
         String protocol = null;
         for (int i = 0; i < args.length; i++) {
             if (args[i].startsWith("host")) { //$NON-NLS-1$
                 hostHeader = getCommandLineValue(args[i]);
             } else if (args[i].startsWith("protocol")) { //$NON-NLS-1$
                 protocol = getCommandLineValue(args[i]);
             } else if (args[i].startsWith("username")) { //$NON-NLS-1$
                 agentArgs.setUsername(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("password")) { //$NON-NLS-1$
                 agentArgs.setPassword(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("localProxyURL")) { //$NON-NLS-1$
                 agentArgs.setLocalProxyURL(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("pluginProxyURL")) { //$NON-NLS-1$
                 agentArgs.setPluginProxyURL(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("ticket")) { //$NON-NLS-1$
                 agentArgs.setTicket(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("browserCommand")) { //$NON-NLS-1$
                 agentArgs.setBrowserCommand(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("userAgent")) { //$NON-NLS-1$
                 String userAgent = getCommandLineValue(args[i]);
                 agentArgs.setUserAgent(userAgent);
                 HttpClient.setUserAgent(userAgent);
             } else if (args[i].startsWith("locale")) { //$NON-NLS-1$
                 agentArgs.setLocaleName(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("disableNewSSLEngine")) { //$NON-NLS-1$
                 agentArgs.setDisableNewSSLEngine(Boolean.valueOf(getCommandLineValue(args[i])).booleanValue());
             } else if (args[i].startsWith("webforward.inactivity")) { //$NON-NLS-1$
                 try {
                     webforwardInactivity = Integer
                             .parseInt(getCommandLineValue(args[i]));
                 } catch (NumberFormatException ex) {
                 }
             } else if (args[i].startsWith("tunnel.inactivity")) { //$NON-NLS-1$
                 try {
                     tunnelInactivity = Integer
                             .parseInt(getCommandLineValue(args[i]));
                 } catch (NumberFormatException ex) {
                 }
             } else if (args[i].startsWith("shutdown")) { //$NON-NLS-1$
                 try {
                     shutdown = Integer
                             .parseInt(getCommandLineValue(args[i]));
                 } catch (NumberFormatException ex) {
                 }
             } else if (args[i].startsWith("log4j")) { //$NON-NLS-1$
                 agentArgs.setLogProperties(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("extensionClasses")) { //$NON-NLS-1$
                 agentArgs.setExtensionClasses(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("ignoreCertWarnings")) {
                 System.getProperties().put(
                         "com.maverick.ssl.allowUntrustedCertificates",
                         "true");
                 System.getProperties()
                         .put("com.maverick.ssl.allowInvalidCertificates",
                                 "true");
             } else if (args[i].startsWith("forceBasicUI")) {
                 configuration
                         .setGUIClass("com.adito.agent.client.gui.BasicFrameGUI");
             } else if (args[i].startsWith("displayInformationPopups")) {
                 configuration
                         .setDisplayInformationPopups(getCommandLineValue(
                                 args[i]).equalsIgnoreCase("true"));
             } else if (args[i]
                     .startsWith("remoteTunnelsRequireConfirmation")) {
                 configuration
                         .setRemoteTunnelsRequireConfirmation(getCommandLineValue(
                                 args[i]).equalsIgnoreCase("true"));
             } else if (args[i].startsWith("cleanOnExit")) {
                 configuration.setCleanOnExit(getCommandLineValue(args[i])
                         .equalsIgnoreCase("true"));
             } else if (args[i].startsWith("localhostAddress")) {
                 configuration.setLocalhostAddress(getCommandLineValue(args[i]));
             } else if (args[i].startsWith("dir")) {
                 configuration.setCacheDir(new File(
                         Utils.getHomeDirectory(),
                         getCommandLineValue(args[i])));
             } else if (args[i].startsWith("removeFiles")) {
                 StringTokenizer files = new StringTokenizer(
                         getCommandLineValue(args[i]), File.pathSeparator);
                 while (files.hasMoreTokens()) {
                     configuration.removeFileOnExit(new File(files
                             .nextToken()));
                 }
             } else if (args[i].startsWith("keepAlivePeriod")) {
                 configuration.setKeepAlivePeriod(Integer
                         .parseInt(getCommandLineValue(args[i])));
             } else if (args[i].startsWith("extensionId")) {
             	agentArgs.setExtensionId(getCommandLineValue(args[i]));
             }
         }
         
         if(hostHeader == null || protocol == null) {
         	throw new IOException("");
         } else {
         	int idx = hostHeader.indexOf(':');
         	if(idx > -1) {
         		String port = hostHeader.substring(idx+1);
         		agentArgs.setHostname(hostHeader.substring(0,idx));
                 try {
                     agentArgs.setPort(Integer.parseInt(port));
                 } catch (NumberFormatException ex) {
                 }
                 agentArgs.setSecure(protocol.equalsIgnoreCase("https"));
         	} else {
         		agentArgs.setHostname(hostHeader);
         		agentArgs.setPort(protocol.equalsIgnoreCase("http") ? 80 : 443);
         		agentArgs.setSecure(protocol.equalsIgnoreCase("https"));
         	}
         }
 
         if(isWindows64()) {
             configuration.setGUIClass("com.adito.agent.client.gui.BasicFrameGUI");
         }
         
         WinRegistry.setLocation(new File(configuration.getCacheDir(), "applications" + File.separator + agentArgs.getExtensionId()));
         
         /*
          * Load the message resources.
          */
         Locale.setDefault(Utils.createLocale(agentArgs.getLocaleName()));
 
         if (agentArgs.getLogProperties() == null) {
             System.out.println(Messages
                     .getString("VPNClient.main.debugModeWarning")); //$NON-NLS-1$
         }
 
         if (agentArgs.getPort() == -1 || agentArgs.getHostname() == null || agentArgs.getUsername() == null
                 || (agentArgs.getTicket() == null && agentArgs.getPassword() == null))
             throw new IOException(
                     Messages
                             .getString("VPNClient.main.missingCommandLineArguments")); //$NON-NLS-1$
 
         if (shutdown > -1)
             configuration.setShutdownPeriod(shutdown);
         configuration.setSystemExitOnDisconnect(true);
         configuration.setWebForwardInactivity(webforwardInactivity);
         configuration.setTunnelInactivity(tunnelInactivity);
 
         agentArgs.setAgentConfiguration(configuration);
         return agentArgs;
     }
     
     protected static Agent initAgent(AgentArgs agentArgs) throws Throwable {
         Agent agent = new Agent(agentArgs.getAgentConfiguration());
 
         // Setup the output stream
         PrintStream consolePrintStream = new PrintStream(agent.getGUI()
                 .getConsole());
         System.setErr(consolePrintStream);
         System.setOut(consolePrintStream);
 
         System.out.println("Java version "
                 + System.getProperty("java.version"));
         System.out.println("OS version " + System.getProperty("os.name"));
 
         // #ifdef DEBUG
         if (agentArgs.getLogProperties() != null) {
             File f = new File(agentArgs.getLogProperties());
             InputStream in = new FileInputStream(f);
             try {
                 Properties props = new Properties();
                 props.load(in);
                 File logfile = new File(f.getParent(), "agent.log"); //$NON-NLS-1$
                 props
                         .put(
                                 "log4j.appender.logfile.File", logfile.getAbsolutePath()); //$NON-NLS-1$
                 org.apache.log4j.PropertyConfigurator.configure(props);
                 log = org.apache.commons.logging.LogFactory
                         .getLog(Agent.class);
                 log.info("Configured logging"); //$NON-NLS-1$
             } finally {
                 in.close();
             }
         }
         
         Properties systemProperties = System.getProperties();
         String key;
         log.info("System properties:");
         for(Enumeration e = systemProperties.keys(); e.hasMoreElements();) {
             key = (String)e.nextElement();
             log.info("   " + key + ": " + systemProperties.getProperty(key));
         }
         // #endif
 
         agent.setupProxy(agentArgs.getLocalProxyURL(), agentArgs.getUserAgent(), agentArgs.getPluginProxyURL());
 
         if (agentArgs.getBrowserCommand() != null && !agentArgs.getBrowserCommand().equals("")) { //$NON-NLS-1$
 
             // #ifdef DEBUG
             log.info("Setting browser to " + agentArgs.getBrowserCommand()); //$NON-NLS-1$
             // #endif
             BrowserLauncher.setBrowserCommand(agentArgs.getBrowserCommand());
         }
         
         return agent;
     }
 	
 	private static boolean isWindows64() {
 
 	    String prop = System.getProperty("os.name");
 	    if (prop == null || prop.startsWith("Windows") == false)
 	      return false;
 
 	    prop = System.getProperty("os.arch");
 	    if (prop != null && prop.equalsIgnoreCase("amd64"))
 	      return true;
 
 	    prop = System.getProperty("java.vm.name");
 	    if (prop != null && prop.indexOf("64-Bit") != -1)
 	      return true;
 
 	    prop = System.getProperty("sun.arch.data.model");
 	    if (prop != null && prop.equals("64"))
 	      return true;
 
 	    return false;		
 	}
 	
 	private static boolean isWindowsVista() {
 		return System.getProperty("os.name").startsWith("Windows Vista");
 	}
     
     public static void initMain(Agent agent, AgentArgs agentArgs) {
         if (null != agent && null != agentArgs) {
         	agent.initMain(agentArgs.getHostname(), agentArgs.getPort(), agentArgs.isSecure(), agentArgs.getUsername(), agentArgs.getPassword(), agentArgs.getTicket());
             if (agentArgs.getExtensionClasses() != null)
                 agent.startExtensions(agentArgs.getExtensionClasses());
         }
     }
 
 	protected void initMain(String hostname, int port, boolean isSecure, String username, String password,
 			String ticket) {
 		try {
 			init();
 			connect(hostname, port, isSecure, username, ticket == null ? password
 					: ticket, ticket == null);
 		} catch (SSLIOException ex) {
 			// #ifdef DEBUG
 			log.info("An unexpected error has occured.", ex.getRealException()); //$NON-NLS-1$
 			log.info("Agent must now exit"); //$NON-NLS-1$
 			// #endif
 			gui.showDisconnected();
 			gui.error(Messages.getString("VPNClient.close"), null, //$NON-NLS-1$  
 					Messages.getString("VPNClient.error"), //$NON-NLS-1$ 
 					Messages.getString("VPNClient.failedToConnect"), ex); //$NON-NLS-1$
 			System.exit(4);
 		} catch (IOException ex) {
 			// #ifdef DEBUG
 			log.info("An unexpected error has occured.", ex); //$NON-NLS-1$
 			log.info("Agent must now exit"); //$NON-NLS-1$
 			// #endif
 			gui.error(Messages.getString("VPNClient.close"), null, //$NON-NLS-1$
 					Messages.getString("VPNClient.error"), //$NON-NLS-1$ 
 					Messages.getString("VPNClient.failedToConnect"), ex); //$NON-NLS-1$
 			gui.showDisconnected();
 			System.exit(4);
 		} catch (Throwable t) {
 			gui.error(Messages.getString("VPNClient.close"), null, //$NON-NLS-1$
 					Messages.getString("VPNClient.error"), //$NON-NLS-1$ 
 					Messages.getString("VPNClient.failedToConnect"), t); //$NON-NLS-1$
 			// #ifdef DEBUG
 			log.info("Critical failure", t); //$NON-NLS-1$
 			// #endif
 			gui.showDisconnected();
 			System.exit(4);
 		}
 	}
 
 	public void setupProxy(String localProxyURL, String userAgent,
 			String pluginProxyURL) throws MalformedURIException {
 		if (localProxyURL != null
 				&& !localProxyURL.equals("") && !localProxyURL.startsWith("browser://")) { //$NON-NLS-1$ //$NON-NLS-2$
 			// Use the user supplied proxy settings
 
 			// #ifdef DEBUG
 			log.info("Setting user specified local proxy URL to " + obfuscateURL(localProxyURL)); //$NON-NLS-1$
 			// #endif
 			setLocalProxyURL(localProxyURL);
 		} else {
 			// #ifdef DEBUG
 			log.info("Attempting to detect proxy settings using platform specific methods"); //$NON-NLS-1$
 			// #endif
 
 			if (localProxyURL != null && localProxyURL.startsWith("browser://")) { //$NON-NLS-1$
 		 	        
 				URI uri = new URI(localProxyURL);
 
 				/*
 				 * Try to determine the proxy settings by first usng platform /
 				 * browser specific method, then the proxy supplied by the Java
 				 * plugin.
 				 * 
 				 * TODO be more intelligent about which browse to try first -
 				 * use the userAgent parameter passed from the JSP
 				 */
 				String proxyURL = null;
 				if (userAgent != null) {
 
 					// TODO support more browsers
 
 					BrowserProxySettings proxySettings = null;
 					if (userAgent.indexOf("MSIE") != -1) { //$NON-NLS-1$
 						try {
 							// #ifdef DEBUG
 							log.info("Looking for IE"); //$NON-NLS-1$
 							// #endif
 							proxySettings = ProxyUtil.lookupIEProxySettings();
 						} catch (Throwable t) {
 							// #ifdef DEBUG
 							log
 									.error(
 											"Failed to get IE proxy settings, trying Firefox.", t); //$NON-NLS-1$
 							// #endif
 						}
 					}
 
 					if (proxySettings == null
 							&& userAgent.indexOf("Firefox") != -1) { //$NON-NLS-1$
 						try {
 							// #ifdef DEBUG
 							log.info("Looking for Firefox"); //$NON-NLS-1$
 							// #endif
 							proxySettings = ProxyUtil
 									.lookupFirefoxProxySettings();
 						} catch (Throwable t) {
 							// #ifdef DEBUG
 							log.error(
 									"Failed to get Firefox proxy settings.", t); //$NON-NLS-1$
 							// #endif
 						}
 					}
 					if (proxySettings != null) {
 						// #ifdef DEBUG
 						log.info("Found some proxy settings."); //$NON-NLS-1$
 						// #endif
 						ProxyInfo[] proxyInfo = proxySettings.getProxies();
 						for (int i = 0; proxyInfo != null
 								&& i < proxyInfo.length; i++) {
 							// #ifdef DEBUG
 							log
 									.info("Checking if " + obfuscateURL(proxyInfo[i].toUri()) + " is suitable."); //$NON-NLS-1$
 							// #endif
 							if (proxyInfo[i].getProtocol().equals("ssl") || proxyInfo[i].getProtocol().equals("https") //$NON-NLS-1$ //$NON-NLS-2$
 									|| proxyInfo[i].getProtocol().equals("all")) { //$NON-NLS-1$
 								StringBuffer buf = new StringBuffer("http://"); //$NON-NLS-1$
 								if (proxyInfo[i].getUsername() != null
 										&& !proxyInfo[i].getUsername().equals(
 												"")) { //$NON-NLS-1$
 									buf.append(proxyInfo[i].getUsername());
 									if (proxyInfo[i].getPassword() != null
 											&& !proxyInfo[i].getPassword()
 													.equals("")) { //$NON-NLS-1$
 										buf.append(":"); //$NON-NLS-1$
 										buf.append(proxyInfo[i].getPassword());
 									}
 									buf.append("@"); //$NON-NLS-1$
 								}
 								buf.append(proxyInfo[i].getHostname());
 								if (proxyInfo[i].getPort() != 0) {
 									buf.append(":"); //$NON-NLS-1$
 									buf.append(proxyInfo[i].getPort());
 								}
 								if (uri.getHost() != null) {
 									buf.append("?"); //$NON-NLS-1$
 									buf.append(uri.getHost());
 								}
 								proxyURL = buf.toString();
 								break;
 							}
 						}
 					} else {
 						// #ifdef DEBUG
 						log
 								.warn("No useragent supplied, automatic proxy could not check for browse type."); //$NON-NLS-1$
 						// #endif
 					}
 
 					// Use the proxy supplied by the plugin if it is
 					// available
 					if (proxyURL == null && pluginProxyURL != null
 							&& !pluginProxyURL.equals("")) { //$NON-NLS-1$
 						// #ifdef DEBUG
 						log.info("Using plugin supplied proxy settings."); //$NON-NLS-1$
 						// #endif
 						proxyURL = pluginProxyURL;
 					}
 				}
 
 				if (proxyURL != null) {
 					// #ifdef DEBUG
 					log
 							.info("Setting local proxy URL to " + obfuscateURL(proxyURL) + "."); //$NON-NLS-1$
 					// #endif
 					setLocalProxyURL(proxyURL);
 				}
 			}
 		}
 	}
 	
 
 
 	private void cleanupWindowsAgent() {
 
 		// #ifdef DEBUG
 		log.info("Clearing Windows agent cache"); //$NON-NLS-1$
 		// #endif
 
 		try {
 			String[] cmds = null;
 
 			String homeDir = Utils.getHomeDirectory();
 
 			File cwd = getConfiguration().getCacheDir();
 			// #ifdef DEBUG
 			log.info("Will remove " + cwd.getAbsolutePath()); //$NON-NLS-1$
 			// #endif
 			File scriptFile = new File(homeDir, "agent-cleanup.bat");
 			File launchFile = new File(homeDir,
 					"agent-cleanup-launch.bat");
 			String scriptContents = "@echo off\r\n"
 					+ "echo Agent is removing all downloaded files\r\n"
 					+ ":Repeat\r\n" + "rd /S /Q \"" + cwd.getAbsolutePath()
 					+ "\" > NUL 2>&1\r\n" + "if exist \""
 					+ cwd.getAbsolutePath() + "\" > NUL 2>&1 goto Repeat\r\n";
 
 			File toRemove;
 			for (Enumeration e = getConfiguration().getFilesToRemove(); e
 					.hasMoreElements();) {
 				toRemove = (File) e.nextElement();
 				if (toRemove.exists()) {
 					if (toRemove.isDirectory()) {
 						scriptContents += "rd /S /Q \""
 								+ toRemove.getAbsolutePath()
 								+ "\" > NUL 2>&1\r\n";
 					} else {
 						scriptContents += "del \"" + toRemove.getAbsolutePath()
 								+ "\" > NUL 2>&1\r\n";
 					}
 				}
 			}
 			scriptContents += "del \"" + scriptFile.getAbsolutePath()
 					+ "\" > NUL 2>&1 && exit\r\n";
 
 			String launchScript = "start \"Agent Cleanup\" /MIN \""
 					+ scriptFile.getAbsolutePath()
 					+ "\"\r\n"
 					+ "del \""
 					+ launchFile.getAbsolutePath() + "\" > NUL 2>&1\r\n";
 
 			cmds = new String[3];
 			cmds[0] = "cmd.exe";
 			cmds[1] = "/C";
 			cmds[2] = "\"" + launchFile.getAbsolutePath() + "\"";
 
 			FileOutputStream out = new FileOutputStream(scriptFile);
 			out.write(scriptContents.getBytes());
 			out.close();
 
 			out = new FileOutputStream(launchFile);
 			out.write(launchScript.getBytes());
 			out.close();
 
 			final Process proc = Runtime.getRuntime().exec(cmds);
 
 			Thread t1 = new Thread(new Runnable() {
 				public void run() {
 					try {
 						InputStream in = proc.getInputStream();
 						while (in.read() > -1)
 							;
 					} catch (IOException e) {
 					}
 				}
 			}, "CleanupAgentInput");
 			Thread t2 = new Thread(new Runnable() {
 				public void run() {
 					try {
 						InputStream in = proc.getErrorStream();
 						while (in.read() > -1)
 							;
 					} catch (IOException e) {
 					}
 				}
 			}, "CleanupAgentOutput");
 
 			t1.start();
 			t2.start();
 
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * Schedule a System.exit() in a thread. After the configured shutdown
 	 * period has elapsed the JVM will terminate.
 	 */
 	public void scheduleExit() {
 		Thread t = new Thread("ScheduledExit") {
 			public void run() {
 				try {
 					Thread.sleep(getConfiguration().getShutdownPeriod());
 				} catch (InterruptedException ex) {
 				}
 				gui.dispose();
 				// #ifdef DEBUG
 				log.info("Exiting JVM."); //$NON-NLS-1$
 				// #endif
 				System.exit(0);
 			}
 		};
 		t.start();
 	}
 
 	private void disconnected() {
 		currentState = STATE_DISCONNECTED;
 		getGUI().showDisconnected();
 	}
 
 	private void disconnectAgent() {
 
 		// #ifdef DEBUG
 		log.info("Disconnecting Agent"); //$NON-NLS-1$
 		// #endif
 		// Disconnect the multiplexed protocol
         keepAlive.stopThread();
 		con.disconnect("The agent is shutting down");
 		// This is a backup to ensure that the socket is released.
 		httpConnection.close();
 	}
 
 	private void cleanupLinuxAgent() {
 
 		// #ifdef DEBUG
 		log.info("Clearing Linux agent cache"); //$NON-NLS-1$
 		// #endif
 
 		try {
 			String[] cmds = null;
 			String homeDir = Utils.getHomeDirectory();
 			File cacheDir = getConfiguration().getCacheDir();
 			// #ifdef DEBUG
 			log.info("Will remove " + cacheDir.getAbsolutePath()); //$NON-NLS-1$
 			// #endif
 			File scriptFile = new File(homeDir, "agent-cleanup.sh");
 			File launchFile = new File(homeDir,
 					"agent-cleanup-launch.sh");
 
 			// clean up script
 			StringBuffer scriptContents = new StringBuffer();
 			scriptContents.append("#!/bin/sh\n");
 			scriptContents.append("sleep 3\n");
 			scriptContents.append("rm -fr \"");
 			scriptContents.append(cacheDir.getAbsolutePath());
 			scriptContents.append("\" \"");
 
 			File toRemove;
 			for (Enumeration e = getConfiguration().getFilesToRemove(); e
 					.hasMoreElements();) {
 				toRemove = (File) e.nextElement();
 				scriptContents.append(toRemove.getAbsolutePath());
 				scriptContents.append("\" \"");
 			}
 
 			scriptContents.append(scriptFile.getAbsolutePath());
 			scriptContents.append("\" \"");
 			scriptContents.append(launchFile.getAbsolutePath());
 			scriptContents.append("\"\n");
 
 			// launch script
 			StringBuffer launchScript = new StringBuffer();
 			launchScript.append("#!/bin/sh\n");
 			launchScript.append("nohup sh \"");
 			launchScript.append(scriptFile.getAbsolutePath());
 			launchScript.append("\" &\n");
 
 			cmds = new String[2];
 			cmds[0] = "sh";
 			cmds[1] = launchFile.getAbsolutePath();
 
 			FileOutputStream out = new FileOutputStream(scriptFile);
 			out.write(scriptContents.toString().getBytes());
 			out.close();
 
 			out = new FileOutputStream(launchFile);
 			out.write(launchScript.toString().getBytes());
 			out.close();
 
 			final Process proc = Runtime.getRuntime().exec(cmds);
 
 			Thread t1 = new Thread(new Runnable() {
 				public void run() {
 					try {
 						InputStream in = proc.getInputStream();
 						while (in.read() > -1)
 							;
 					} catch (IOException e) {
 					}
 				}
 			});
 			Thread t2 = new Thread(new Runnable() {
 				public void run() {
 					try {
 						InputStream in = proc.getErrorStream();
 						while (in.read() > -1)
 							;
 					} catch (IOException e) {
 					}
 				}
 			});
 
 			t1.start();
 			t2.start();
 
 		} catch (Exception e) {
 
 		}
 	}
 	
 	protected void updateResources(int resourceTypeId) {		
 		if (getConfiguration().isGetResources()) {
 			/* TODO we should consider moving the services into the appropriate extension instead
 			 * of all in the agent module. It should now be possible to rewrite them as agent
 			 * extensions
 			 */
 			
 			// Applications
 			if(resourceTypeId == -1 || resourceTypeId == ApplicationManager.APPLICATION_SHORTCUT_RESOURCE_TYPE_ID)
 				applicationManager.getApplicationResources();
 			
 			// Tunnels			
 			if(resourceTypeId == -1 || resourceTypeId == TunnelManager.TUNNEL_RESOURCE_TYPE_ID)
 				tunnelManager.getTunnelResources();
 			
 			// Web forwards
 			if(resourceTypeId == -1 || resourceTypeId == WebForwardManager.WEBFORWARD_RESOURCE_TYPE_ID)
 				webForwardManager.getWebForwardResources();
 			
 			// Network places
 			if(resourceTypeId == -1 || resourceTypeId == NetworkPlaceManager.NETWORK_PLACE_RESOURCE_TYPE_ID)
 				networkPlaceManager.getNetworkPlaceResources();
 		}
 	}
 
 	class KeepAliveThread extends Thread {
 
 		boolean running = true;
 
 		Request keepAlive = new Request("keepAlive");
 
 		public void run() {
 
 			long period = getConfiguration().getKeepAlivePeriod();
 
 			if(period > 0) {
 				while (running && con.isRunning()) {
 					try {
 						Thread.sleep(period);
 					} catch (InterruptedException e) {
 					}
 	
 					if (!running || !con.isRunning()) {
 						break;
 					}
 	
 					try {
 						con.sendRequest(keepAlive, true, getConfiguration()
 								.getKeepAliveTimeout());
 					} catch (InterruptedIOException ex) {
 						//#ifdef DEBUG
 						log.error("Keepalive packet timed out!! Agent connection is no longer operational", ex);
 						//#endif
 						if (getConfiguration().isDisplayInformationPopups()) {
 							getGUI().popup(
 								null,
 								Messages.getString("VPNClient.keepalive.timeout"), Messages.getString("VPNClient.title"), "popup-error", -1); //$NON-NLS-1$  //$NON-NLS-2$
 						}
 						try {
 							Thread.sleep(10000);
 						} catch (InterruptedException e) {
 						}
 						Agent.this.startShutdownProcedure();
 					} catch (IOException e) {
 						//#ifdef DEBUG
 						log.error("Keepalive IO error!! Agent connection is no longer operational", e);
 						//#endif
 						if (getConfiguration().isDisplayInformationPopups()) {
 							getGUI().popup(
 								null,
 								Messages.getString("VPNClient.keepalive.error"), Messages.getString("VPNClient.title"), "popup-error", -1); //$NON-NLS-1$  //$NON-NLS-2$
 						}
 						try {
 							Thread.sleep(10000);
 						} catch (InterruptedException e2) {
 						}
 						Agent.this.startShutdownProcedure();
 					}
 				}
 			}
 
 		}
 
 		public void stopThread() {
 			running = false;
 			interrupt();
 		}
 	}
 }
