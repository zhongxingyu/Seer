 // %1308103607:de.hattrickorganizer.net%
 /*
  * MyConnector.java
  *
  * Created on 7. April 2003, 09:36
  */
 package de.hattrickorganizer.net;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.Inflater;
 import java.util.zip.InflaterInputStream;
 
 import javax.swing.JOptionPane;
 
 import org.scribe.builder.ServiceBuilder;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.SignatureType;
 import org.scribe.model.Token;
 import org.scribe.model.Verb;
 import org.scribe.oauth.OAuthService;
 
 import sun.misc.BASE64Encoder;
 import de.hattrickorganizer.gui.HOMainFrame;
 import de.hattrickorganizer.gui.login.OAuthDialog;
 import de.hattrickorganizer.gui.login.ProxyDialog;
 import de.hattrickorganizer.logik.xml.XMLCHPPPreParser;
 import de.hattrickorganizer.logik.xml.XMLExtensionParser;
 import de.hattrickorganizer.logik.xml.XMLNewsParser;
 import de.hattrickorganizer.logik.xml.xmlTeamDetailsParser;
 import de.hattrickorganizer.model.Extension;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.News;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.Helper;
 import de.hattrickorganizer.tools.IOUtilities;
 import de.hattrickorganizer.tools.updater.VersionInfo;
 
 /**
  * DOCUMENT ME!
  * 
  * @author thomas.werth
  */
 public class MyConnector implements plugins.IDownloadHelper {
 	// ~ Static fields/initializers
 	// -----------------------------------------------------------------
 	static final private int chppID = 3330;
 	static final private String htUrl = "http://chpp.hattrick.org/chppxml.ashx";
 	public static String m_sIDENTIFIER = "HO! Hattrick Organizer V" + HOMainFrame.VERSION;
 	private static MyConnector m_clInstance;
 	private final static String VERSION_MATCHORDERS = "1.8";
 	private final static String VERSION_TRAINING = "1.5";
 	private final static String VERSION_MATCHLINEUP = "1.6";
 	private final static String VERSION_PLAYERS = "2.1";
 	private final static String VERSION_PLAYERDETAILS = "2.0";
 
 	private final static String CONSUMER_KEY = ">Ij-pDTDpCq+TDrKA^nnE9";
 	private final static String CONSUMER_SECRET = "2/Td)Cprd/?q`nAbkAL//F+eGD@KnnCc>)dQgtP,p+p";
 	// ~ Instance fields
 	// ----------------------------------------------------------------------------
 
 	private String m_ProxyUserName = "";
 	private String m_ProxyUserPWD = "";
 	private String m_sProxyHost = "";
 	private String m_sProxyPort = "";
 	private boolean m_bAuthenticated;
 	private boolean m_bProxyAuthentifactionNeeded;
 	private boolean m_bUseProxy;
 	private int m_iUserID = -1;
 
 	private OAuthService m_OAService;
 	private Token m_OAAccessToken;
 
 	private static boolean DEBUGSAVE = false;
 
 	// private final static String SAVEDIR = "C:/temp/ho/";
 
 	// ~ Constructors
 	// -------------------------------------------------------------------------------
 	/**
 	 * Creates a new instance of MyConnector.
 	 */
 	private MyConnector() {
 		m_OAService = new ServiceBuilder().provider(HattrickAPI.class)
 				.apiKey(Helper.decryptString(CONSUMER_KEY)).apiSecret(Helper.decryptString(CONSUMER_SECRET))
 				.signatureType(SignatureType.Header).build();
 		m_OAAccessToken = new Token(Helper.decryptString(gui.UserParameter.instance().AccessToken),
 				Helper.decryptString(gui.UserParameter.instance().TokenSecret));
 	}
 
 	// ~ Methods
 	// ------------------------------------------------------------------------------------
 
 	/**
 	 * Get the MyConnector instance.
 	 */
 	public static MyConnector instance() {
 		if (m_clInstance == null) {
 			m_clInstance = new MyConnector();
 		}
 		return m_clInstance;
 	}
 
 	/**
 	 * Sets the DEBUGSAVE flag. Setting the flag to true will save downloaded
 	 * CHPP files.
 	 * 
 	 * @param debugSave
 	 *            true to save downloaded CHPP files, false otherwise.
 	 */
 	public static void setDebugSave(boolean debugSave) {
 		DEBUGSAVE = debugSave;
 	}
 
 	public static String getResourceSite() {
 		return getPluginSite();
 	}
 
 	public static String getHOSite() {
 		return "http://ho1.sourceforge.net/";
 	}
 
 	public static String getPluginSite() {
 		return "http://ho1.sourceforge.net/onlinefiles";
 	}
 
 	/**
 	 * Fetch our arena
 	 * 
 	 * @return arena xml
 	 * 
 	 * @throws IOException
 	 */
 	public String getArena() throws IOException {
 		return getArena(-1);
 	}
 
 	/**
 	 * Fetch a specific arena
 	 * 
 	 * @param arenaId
 	 *            id of the arena to fetch (-1 = our arena)
 	 * @return arena xml
 	 * 
 	 * @throws IOException
 	 */
 	public String getArena(int arenaId) throws IOException {
 		String url = htUrl + "?file=arenadetails";
 		if (arenaId > 0)
 			url += "&arenaID=" + arenaId;
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * holt die Finanzen
 	 */
 	public String getEconomy() throws IOException {
 		final String url = htUrl + "?file=economy";
 
 		return getCHPPWebFile(url);
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// get-XML-Files
 	// //////////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * downloads an xml File from hattrick Behavior has changed with oauth, but
 	 * we try to convert old syntaxes.
 	 * 
 	 * @param file
 	 *            ex. = "?file=leaguedetails&[leagueLevelUnitID = integer]"
 	 * 
 	 * @return the complete file as String
 	 */
 	public String getHattrickXMLFile(String file) throws IOException {
 		String url;
 
 		// An attempt at solving old syntaxes.
 
 		if (file.contains("chppxml.axd")) {
 			file = file.substring(file.indexOf("?"));
 		} else if (file.contains(".asp")) {
 			String s = file.substring(0, file.indexOf("?")).replace(".asp", "").replace("/common/", "");
 			file = "?file=" + s + "&" + file.substring(file.indexOf("?") + 1);
 		}
 
 		url = htUrl + file;
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * lädt die Tabelle
 	 */
 	public String getLeagueDetails() throws IOException {
 		final String url = htUrl + "?file=leaguedetails";
 
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * lädt den Spielplan
 	 */
 	public String getLeagueFixtures(int season, int leagueID) throws IOException {
 		String url = htUrl + "?file=leaguefixtures";
 		if (season > 0)
 			url += "&season=" + season;
 		if (leagueID > 0)
 			url += "&leagueLevelUnitID=" + leagueID;
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * lädt das MatchArchiv als xml
 	 */
 	public String getMatchArchiv(int teamId, String firstDate, String lastDate) throws IOException {
 		String url = htUrl + "?file=matchesarchive";
 
 		if (teamId > 0) {
 			url += ("&teamID=" + teamId);
 		}
 
 		if (firstDate != null) {
 			url += ("&FirstMatchDate=" + firstDate);
 		}
 
 		if (lastDate != null) {
 			url += ("&LastMatchDate=" + lastDate);
 		}
 
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * lädt die Aufstellungsbewertung zu einem Spiel
 	 */
 	public String getMatchLineup(int matchId, int teamId) throws IOException {
 		String url = htUrl + "?file=matchlineup&version=" + VERSION_MATCHLINEUP;
 
 		if (matchId > 0) {
 			url += ("&matchID=" + matchId);
 		}
 
 		if (teamId > 0) {
 			url += ("&teamID=" + teamId);
 		}
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * lädt die Aufstellung zu einem Spiel
 	 */
 	public String getMatchOrder(int matchId) throws IOException {
 		String url = htUrl + "?file=matchorders&version=" + VERSION_MATCHORDERS + "&matchID=" + matchId
 				+ "&isYouth=false";
 		return getCHPPWebFile(url);
 	}
 
 	public String setMatchOrder(int matchId, String orderString) throws IOException {
 		String scope = "set_matchorder";
 		String urlpara = "?file=matchorders&version=" + VERSION_MATCHORDERS;
 		if (matchId > 0) {
 			urlpara += "&matchID=" + matchId;
 		}
 		urlpara += "&isYouth=false" + "&actionType=setmatchorder";
 
 		HashMap<String, String> paras = new HashMap<String, String>();
 		paras.put("lineup", orderString);
 		String result = readStream(postWebFileWithBodyParameters(htUrl + urlpara, paras, true, scope));
 		return result;
 	}
 
 	/**
 	 * lädt die Aufstellungsbewertung zu einem Spiel
 	 */
 	public String getMatchdetails(int matchId) throws IOException {
 		String url = htUrl + "?file=matchdetails";
 		if (matchId > 0) {
 			url += ("&matchID=" + matchId);
 		}
 		url += "&matchEvents=true";
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * Get Matches
 	 */
 	public String getMatches(int teamId, boolean forceRefresh) throws IOException {
 		String url = htUrl + "?file=matches";
 
 		if (teamId > 0)
 			url += "&teamID=" + teamId;
 		if (forceRefresh)
 			url += "&actionType=refreshCache";
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * Get Players
 	 */
 	public String getPlayers() throws IOException {
 		final String url = htUrl + "?file=players&version=" + VERSION_PLAYERS;
 
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * Setter for property m_bProxyAuthentifactionNeeded.
 	 * 
 	 * @param m_bProxyAuthentifactionNeeded
 	 *            New value of property m_bProxyAuthentifactionNeeded.
 	 */
 	public void setProxyAuthentifactionNeeded(boolean m_bProxyAuthentifactionNeeded) {
 		this.m_bProxyAuthentifactionNeeded = m_bProxyAuthentifactionNeeded;
 	}
 
 	/**
 	 * Getter for property m_bProxyAuthentifactionNeeded.
 	 * 
 	 * @return Value of property m_bProxyAuthentifactionNeeded.
 	 */
 	public boolean isProxyAuthentifactionNeeded() {
 		return m_bProxyAuthentifactionNeeded;
 	}
 
 	/**
 	 * Setter for property m_sProxyHost.
 	 * 
 	 * @param m_sProxyHost
 	 *            New value of property m_sProxyHost.
 	 */
 	public void setProxyHost(java.lang.String m_sProxyHost) {
 		this.m_sProxyHost = m_sProxyHost;
 	}
 
 	/**
 	 * Getter for property m_sProxyHost.
 	 * 
 	 * @return Value of property m_sProxyHost.
 	 */
 	public java.lang.String getProxyHost() {
 		return m_sProxyHost;
 	}
 
 	/**
 	 * Setter for property m_sProxyPort.
 	 * 
 	 * @param m_sProxyPort
 	 *            New value of property m_sProxyPort.
 	 */
 	public void setProxyPort(java.lang.String m_sProxyPort) {
 		this.m_sProxyPort = m_sProxyPort;
 	}
 
 	/**
 	 * Getter for property m_sProxyPort.
 	 * 
 	 * @return Value of property m_sProxyPort.
 	 */
 	public java.lang.String getProxyPort() {
 		return m_sProxyPort;
 	}
 
 	/**
 	 * Setter for property m_ProxyUserName.
 	 * 
 	 * @param m_ProxyUserName
 	 *            New value of property m_ProxyUserName.
 	 */
 	public void setProxyUserName(java.lang.String m_ProxyUserName) {
 		this.m_ProxyUserName = m_ProxyUserName;
 	}
 
 	/**
 	 * Getter for property m_ProxyUserName.
 	 * 
 	 * @return Value of property m_ProxyUserName.
 	 */
 	public String getProxyUserName() {
 		return m_ProxyUserName;
 	}
 
 	/**
 	 * Setter for property m_ProxyUserPWD.
 	 * 
 	 * @param m_ProxyUserPWD
 	 *            New value of property m_ProxyUserPWD.
 	 */
 	public void setProxyUserPWD(String m_ProxyUserPWD) {
 		this.m_ProxyUserPWD = m_ProxyUserPWD;
 	}
 
 	/**
 	 * Getter for property m_ProxyUserPWD.
 	 * 
 	 * @return Value of property m_ProxyUserPWD.
 	 */
 	public java.lang.String getProxyUserPWD() {
 		return m_ProxyUserPWD;
 	}
 
 	/**
 	 * holt die Teamdetails
 	 */
 	public String getTeamdetails(int teamId) throws IOException {
 		String url = htUrl + "?file=teamdetails";
 		if (teamId > 0) {
 			url += ("&teamID=" + teamId);
 		}
 
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * Get the training XML data.
 	 */
 	public String getTraining() throws IOException {
 		final String url = htUrl + "?file=training&version=" + VERSION_TRAINING;
 
 		return getCHPPWebFile(url);
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// get-HTML-Files
 	// //////////////////////////////////////////////////////////////////////////////
 	/**
 	 * Setter for property m_UseProxy.
 	 * 
 	 * @param m_UseProxy
 	 *            New value of property m_UseProxy.
 	 */
 	public void setUseProxy(boolean m_UseProxy) {
 		this.m_bUseProxy = m_UseProxy;
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// Accessor
 	// //////////////////////////////////////////////////////////////////////////////
 
 	/**
 	 * Getter for property m_UseProxy.
 	 * 
 	 * @return Value of property m_UseProxy.
 	 */
 	public boolean isUseProxy() {
 		return m_bUseProxy;
 	}
 
 	/**
 	 * holt die Vereinsdaten
 	 */
 	public String getVerein() throws IOException {
 		final String url = htUrl + "?file=club";
 		return getCHPPWebFile(url);
 	}
 
 	/**
 	 * Get the content of a web page in one string.
 	 */
 	private String getWebPage(String surl, boolean showError) throws IOException {
 		final InputStream resultingInputStream = getNonCHPPWebFile(surl, showError);
 		return readStream(resultingInputStream);
 	}
 
 	/**
 	 * holt die Weltdaten
 	 */
 	public String getWorldDetails() throws IOException {
 		final String url = htUrl + "?file=worlddetails";
 
 		return getCHPPWebFile(url);
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// Update Checker
 	// //////////////////////////////////////////////////////////////////////////////
 	public VersionInfo getLatestVersion() {
 		VersionInfo ret = new VersionInfo();
 		ret.setBeta(false);
 		ret.setVersion(HOMainFrame.VERSION);
 		try {
 			final String s = getWebPage(MyConnector.getPluginSite() + "/version.htm", false);
 			try {
 				ret.setVersion(Double.parseDouble(s));
 			} catch (NumberFormatException e) {
 				HOLogger.instance().debug(getClass(), "Error parsing version '" + s + "': " + e);
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), "Unable to connect to the update server (HO): " + e);
 		}
 		return ret;
 	}
 
 	/**
 	 * Get information about the latest HO beta.
 	 */
 	public VersionInfo getLatestBetaVersion() {
 		BufferedReader br = null;
 		InputStream is = null;
 		try {
 			is = getNonCHPPWebFile(MyConnector.getPluginSite() + "/betaversion.htm", false);
 			if (is != null) {
 				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 				VersionInfo ret = new VersionInfo();
 				String line;
 
 				while ((line = br.readLine()) != null) {
 					int pos = line.indexOf("=");
 					if (pos > 0) {
 						String key = line.substring(0, pos).trim();
 						String val = line.substring(pos + 1).trim();
 						ret.setValue(key, val);
 					}
 				}
 				if (ret.isValid()) {
 					HOLogger.instance().log(getClass(), "LatestBetaVersion: " + ret);
 					return ret;
 				}
 			} else {
 				HOLogger.instance().log(getClass(), "Unable to connect to the update server (HO).");
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), "Unable to connect to the update server (HO): " + e);
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 				if (is != null)
 					is.close();
 			} catch (IOException e) {
 			}
 		}
 		return null;
 	}
 
 	public Extension getEpvVersion() {
 		try {
 			final String s = getWebPage(MyConnector.getResourceSite() + "/downloads/epv.xml", false);
 
 			return (new XMLExtensionParser()).parseExtension(s);
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), "Unable to connect to the update server (EPV): " + e);
 			return new Extension();
 		}
 	}
 
 	public Extension getRatingsVersion() {
 		try {
 			final String s = getWebPage(MyConnector.getResourceSite() + "/downloads/ratings.xml", false);
 
 			return (new XMLExtensionParser()).parseExtension(s);
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), "Unable to connect to the update server (Ratings): " + e);
 			return new Extension();
 		}
 	}
 
 	public News getLatestNews() {
 		try {
 			final String s = MyConnector.instance().getWebPage(
 					MyConnector.getResourceSite() + "/downloads/news.xml", false);
 			XMLNewsParser parser = new XMLNewsParser();
 			return parser.parseNews(s);
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), "Unable to connect to the update server (News): " + e);
 			return new News();
 		}
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// Proxy
 	// //////////////////////////////////////////////////////////////////////////////
 	public void enableProxy() {
 		if (m_bUseProxy) {
 			System.getProperties().put("https.proxyHost", m_sProxyHost);
 			System.getProperties().put("https.proxyPort", m_sProxyPort);
 			System.getProperties().put("http.proxyHost", m_sProxyHost);
 			System.getProperties().put("http.proxyPort", m_sProxyPort);
 		} else {
 			System.getProperties().remove("https.proxyHost");
 			System.getProperties().remove("https.proxyPort");
 			System.getProperties().remove("http.proxyHost");
 			System.getProperties().remove("http.proxyPort");
 		}
 	}
 
 	/**
 	 * Get the region id for a certain team.
 	 */
 	public String fetchRegionID(int teamID) {
 		String xmlFile = "";
 
 		try {
 			xmlFile = htUrl + "?file=teamdetails&teamID=" + teamID;
 			xmlFile = getCHPPWebFile(xmlFile);
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(), e);
 			return "-1";
 		}
 
 		return new xmlTeamDetailsParser().fetchRegionID(xmlFile);
 	}
 
 	public InputStream getFileFromWeb(String url, boolean displaysettingsScreen) throws IOException {
 		return getFileFromWeb(url, displaysettingsScreen, false);
 	}
 
 	/**
 	 * Get a file from a web server as input stream.
 	 */
 	public InputStream getFileFromWeb(String url, boolean displaysettingsScreen, boolean showErrorMessage)
 			throws IOException {
 		if (displaysettingsScreen) {
			// Show Screen
			final ProxyDialog proxyDialog = new ProxyDialog(HOMainFrame.instance());
			proxyDialog.setVisible(true);
 		}
 		return getNonCHPPWebFile(url, showErrorMessage);
 	}
 
 	/**
 	 * Get the content of a normal (non-HT) web page in one string.
 	 */
 	public String getUsalWebPage(String url, boolean displaysettingsScreen, boolean shortTimeOut)
 			throws IOException {
 		return getUsalWebPage(url, displaysettingsScreen);
 	}
 
 	public String getUsalWebPage(String url, boolean displaysettingsScreen) throws IOException {
 		if (displaysettingsScreen) {
			// Show Screen
			final de.hattrickorganizer.gui.login.ProxyDialog proxyDialog = new de.hattrickorganizer.gui.login.ProxyDialog(
					de.hattrickorganizer.gui.HOMainFrame.instance());
			proxyDialog.setVisible(true);
 		}
 
 		return getWebPage(url, true);
 	}
 
 	/**
 	 * Get a web page using a URLconnection.
 	 */
 	private String getCHPPWebFile(String surl) {
 		String returnString = "";
 		OAuthDialog authDialog = null;
 		Response response = null;
 		int iResponse = 200;
 		boolean tryAgain = true;
 		try {
 			while (tryAgain == true) {
 				OAuthRequest request = new OAuthRequest(Verb.GET, surl);
 
 				infoHO(request);
 				if (m_OAAccessToken == null || m_OAAccessToken.getToken().length() == 0)
 					iResponse = 401;
 				else {
 					m_OAService.signRequest(m_OAAccessToken, request);
 					response = request.send();
 					iResponse = response.getCode();
 				}
 				switch (iResponse) {
 				case 200:
 				case 201:
 					// We are done!
 					returnString = readStream(getResultStream(response));
 					if (DEBUGSAVE) {
 						saveCHPP(surl, returnString);
 					}
 					String sError = XMLCHPPPreParser.getError(returnString);
 					if (sError.length() > 0)
 						throw new RuntimeException(sError);
 					tryAgain = false;
 					break;
 				case 401:
 					if (authDialog == null)
 						authDialog = new OAuthDialog(HOMainFrame.instance(), m_OAService, "");
 					authDialog.setVisible(true);
 					// A way out for a user unable to authorize for some reason
 					if (authDialog.getUserCancel() == true)
 						return null;
 					m_OAAccessToken = authDialog.getAccessToken();
 					if (m_OAAccessToken == null)
 						m_OAAccessToken = new Token(
 								Helper.decryptString(gui.UserParameter.instance().AccessToken),
 								Helper.decryptString(gui.UserParameter.instance().TokenSecret));
 					break;
 				case 407:
 					throw new RuntimeException("HTTP Response Code 407: Proxy authentication required.");
 				default:
 					throw new RuntimeException("HTTP Response Code: " + iResponse);
 				}
 			}
 		} catch (Exception sox) {
 			HOLogger.instance().error(getClass(), sox);
 			JOptionPane.showMessageDialog(null, surl + "\n" + sox.getMessage(), HOVerwaltung.instance().getLanguageString("Fehler"),
 					JOptionPane.ERROR_MESSAGE);
 			returnString = "";
 		}
 		return returnString;
 	}
 
 	private InputStream getNonCHPPWebFile(String surl, boolean showErrorMessage) {
 		InputStream returnStream = null;
 		try {
 			Response response = null;
 			OAuthRequest request = new OAuthRequest(Verb.GET, surl);
 			infoHO(request);
 			response = request.send();
 			int iResponse = response.getCode();
 			switch (iResponse) {
 			case 200:
 			case 201:
 				returnStream = getResultStream(response);
 				break;
 			case 407:
 				throw new RuntimeException(
 						"Download Error\nHTTP Response Code 407: Proxy authentication required.");
 			default:
 				throw new RuntimeException("Download Error\nHTTP Response Code: " + iResponse);
 			}
 		} catch (Exception sox) {
 			HOLogger.instance().error(getClass(), sox);
 			if (showErrorMessage)
 				JOptionPane.showMessageDialog(null, sox.getMessage() + "\nURL: " + surl, HOVerwaltung.instance().getLanguageString("Fehler"),
 						JOptionPane.ERROR_MESSAGE);
 			returnStream = null;
 		}
 		return returnStream;
 	}
 
 	/**
 	 * Post a web file containing body parameters
 	 * 
 	 * @param surl
 	 *            the full url with parameters
 	 * @param bodyprop
 	 *            A hash map of string, string where key is parameter key and
 	 *            value is parameter value
 	 * @param showErrorMessage
 	 *            Whether to show message on error or not
 	 * @param scope
 	 *            The scope of the request is required, if no scope, put "".
 	 *            Example: "set_matchorder".
 	 */
 	public InputStream postWebFileWithBodyParameters(String surl, HashMap<String, String> bodyParas,
 			boolean showErrorMessage, String scope) {
 		InputStream returnStream = null;
 		OAuthDialog authDialog = null;
 		Response response = null;
 		int iResponse = 200;
 		boolean tryAgain = true;
 		try {
 			while (tryAgain == true) {
 				OAuthRequest request = new OAuthRequest(Verb.POST, surl);
 				for (Map.Entry<String, String> entry : bodyParas.entrySet())
 					request.addBodyParameter(entry.getKey(), entry.getValue());
 				infoHO(request);
 				request.addHeader("Content-Type", "application/x-www-form-urlencoded");
 				if (m_OAAccessToken == null || m_OAAccessToken.getToken().length() == 0)
 					iResponse = 401;
 				else {
 					m_OAService.signRequest(m_OAAccessToken, request);
 					response = request.send();
 					iResponse = response.getCode();
 				}
 				switch (iResponse) {
 				case 200:
 				case 201:
 					// We are done!
 					returnStream = getResultStream(response);
 					String sError = XMLCHPPPreParser.getError(readStream(returnStream));
 					if (sError.length() > 0)
 						throw new RuntimeException(sError);
 					tryAgain = false;
 					break;
 				case 401:
 					if (authDialog == null)
 						authDialog = new OAuthDialog(HOMainFrame.instance(), m_OAService, scope);
 					authDialog.setVisible(true);
 					// A way out for a user unable to authorize for some reason
 					if (authDialog.getUserCancel() == true)
 						return null;
 					m_OAAccessToken = authDialog.getAccessToken();
 					if (m_OAAccessToken == null)
 						m_OAAccessToken = new Token(
 								Helper.decryptString(gui.UserParameter.instance().AccessToken),
 								Helper.decryptString(gui.UserParameter.instance().TokenSecret));
 					// Try again...
 					break;
 				case 407:
 					throw new RuntimeException(
 							"Download Error\nHTTP Response Code 407: Proxy authentication required.");
 				default:
 					throw new RuntimeException("Download Error\nHTTP Response Code: " + iResponse);
 				}
 			}
 		} catch (Exception sox) {
 			HOLogger.instance().error(getClass(), sox);
 			if (showErrorMessage)
 				JOptionPane.showMessageDialog(null, sox.getMessage() + "\nURL: " + surl, HOVerwaltung.instance().getLanguageString("Fehler"),
 						JOptionPane.ERROR_MESSAGE);
 			returnStream = null;
 		}
 		return returnStream;
 	}
 
 	private InputStream getResultStream(Response response) throws IOException {
 		InputStream resultingInputStream = null;
 		if (response != null) {
 			String encoding = response.getHeader("Content-Encoding");
 			if ((encoding != null) && encoding.equalsIgnoreCase("gzip")) {
 				resultingInputStream = new GZIPInputStream(response.getStream());
 				HOLogger.instance().log(getClass(), " Read GZIP.");
 			} else if ((encoding != null) && encoding.equalsIgnoreCase("deflate")) {
 				resultingInputStream = new InflaterInputStream(response.getStream(), new Inflater(true));
 				HOLogger.instance().log(getClass(), " Read Deflated.");
 			} else {
 				resultingInputStream = response.getStream();
 				HOLogger.instance().log(getClass(), " Read Normal.");
 			}
 		}
 		return resultingInputStream;
 	}
 
 	private String readStream(InputStream stream) throws IOException {
 		String sReturn = "";
 		if (stream != null) {
 			final BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
 			final StringBuffer s2 = new StringBuffer();
 			String line = bufferedreader.readLine();
 			if (line != null) {
 				s2.append(line);
 				while ((line = bufferedreader.readLine()) != null) {
 					s2.append('\n');
 					s2.append(line);
 				}
 			}
 			bufferedreader.close();
 			sReturn = s2.toString();
 		}
 		return sReturn;
 	}
 
 	// ///////////////////////////////////////////////////////////////////////////////
 	// Identifikation
 	// //////////////////////////////////////////////////////////////////////////////
 
 	private void infoHO(OAuthRequest request) {
 		request.addHeader("accept-language", "de");
 		request.setConnectionKeepAlive(true);
 		request.setConnectTimeout(60, TimeUnit.SECONDS);
 		request.addHeader("accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
 		request.addHeader("accept-encoding", "gzip, deflate");
 		request.addHeader("user-agent", m_sIDENTIFIER);
 
 		// ProxyAuth hier einbinden da diese Funk immer aufgerufen wird
 		if (m_bProxyAuthentifactionNeeded) {
 			final String pw = m_ProxyUserName + ":" + m_ProxyUserPWD;
 			final String epw = (new BASE64Encoder()).encode(pw.getBytes());
 			request.addHeader("Proxy-Authorization", "Basic " + epw);
 		}
 	}
 
 	/**
 	 * Save downloaded data to a temp-file for debugging purposes.
 	 * 
 	 * @param url
 	 *            the url where the content was downloaded from
 	 * @param content
 	 *            the content to save
 	 */
 	private void saveCHPP(String url, String content) {
 		File outDir = new File("tmp");
 		if (!outDir.exists()) {
 			outDir.mkdirs();
 		}
 
 		// for the filename, replace everything which is not a letter or digit
 		// with an underscore
 		char[] urlData = url.toCharArray();
 		for (int i = 0; i < urlData.length; i++) {
 			if (!Character.isLetterOrDigit(urlData[i])) {
 				urlData[i] = '_';
 			}
 		}
 
 		Date downloadDate = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
 		String outFileName = df.format(downloadDate) + "-" + String.valueOf(urlData) + ".txt";
 		File outFile = new File(outDir, outFileName);
 
 		df = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
 		StringBuilder builder = new StringBuilder();
 		builder.append("Downloaded at ").append(df.format(downloadDate)).append("\n");
 		builder.append("From ").append(url).append("\n\n");
 		builder.append(content);
 
 		try {
 			IOUtilities.writeToFile(builder.toString(), outFile, "UTF-8");
 		} catch (Exception e) {
 			HOLogger.instance().error(MyConnector.class, e);
 		}
 	}
 
 	final public static String getInitialHTConnectionUrl() {
 		return htUrl;
 	}
 
 	final public int getUserID() {
 		return m_iUserID;
 	}
 }
