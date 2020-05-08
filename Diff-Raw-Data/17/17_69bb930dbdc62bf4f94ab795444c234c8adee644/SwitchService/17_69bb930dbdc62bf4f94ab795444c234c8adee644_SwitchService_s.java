 package org.comtel.fritz.connect.service;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import javafx.beans.property.SimpleBooleanProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.beans.property.SimpleLongProperty;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.value.ChangeListener;
 import javafx.beans.value.ObservableValue;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.comtel.fritz.connect.FritzUtils;
 import org.comtel.fritz.connect.XmlUtils;
 import org.comtel.fritz.connect.cmd.SwitchCmd;
 import org.comtel.fritz.connect.device.SwitchDevice;
 import org.comtel.fritz.connect.exception.ServiceNotSupportedException;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class SwitchService {
 
 	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(SwitchService.class);
 
 	private final SimpleStringProperty hostProperty = new SimpleStringProperty("fritz.box");
 
 	private final SimpleStringProperty userProperty = new SimpleStringProperty("");
 	private final SimpleStringProperty pwdProperty = new SimpleStringProperty("");
 
 	private final long SID_CACHE_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
 	private final SimpleStringProperty sidCacheProperty = new SimpleStringProperty(null);
 	private final SimpleLongProperty sidCacheTimeProperty = new SimpleLongProperty(-1);
 
 	private final SimpleStringProperty protocolProperty = new SimpleStringProperty("http");
 
 	private final SimpleBooleanProperty sslProperty = new SimpleBooleanProperty(false);
 
 	private final SimpleIntegerProperty portProperty = new SimpleIntegerProperty(-1);
 
 	private final static String EMPTY_SID = "0000000000000000";
 
 	private final static String SID_REQUEST_URL = "/login_sid.lua";
 	private final static String SID_CREDENTIAL_URL = "/login_sid.lua?username=%s&response=%s";
 
 	private final static String SID_SWITCHCMD_URL = "/webservices/homeautoswitch.lua?switchcmd=%s&sid=%s";
 	private final static String SID_AIN_SWITCHCMD_URL = "/webservices/homeautoswitch.lua?ain=%s&switchcmd=%s&sid=%s";
 
 	private final DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
 
 	/**
 	 * FRITZ!BOX firmware >= FRITZ!OS 05.55 (homeautoswitch.lua)
 	 * <p>
 	 * FRITZ!OS 05.58-26414 BETA<br>
 	 * FRITZ!OS 05.59-26514 BETA<br>
 	 * 
 	 */
 	public SwitchService() {
 
 		hostProperty.addListener(new ChangeListener<String>() {
 			@Override
 			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
 				invalidateSidCache();
 			}
 		});
 
 		portProperty.addListener(new ChangeListener<Number>() {
 			@Override
 			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
 				invalidateSidCache();
 			}
 		});
 
 		sslProperty.addListener(new ChangeListener<Boolean>() {
 			@Override
 			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean ssl) {
 				protocolProperty.set(ssl ? "https" : "http");
 				invalidateSidCache();
 				if (ssl) {
 					try {
 						trustAllSslCertificate();
 					} catch (KeyManagementException | NoSuchAlgorithmException e) {
 						logger.error(e.getMessage(), e);
 					}
 				}
 			}
 		});
 	}
 
 	private void invalidateSidCache() {
 		sidCacheProperty.set(null);
 		sidCacheTimeProperty.set(-1);
 	}
 
 	public final SimpleStringProperty getHostProperty() {
 		return hostProperty;
 	}
 
 	public final SimpleStringProperty getUserProperty() {
 		return userProperty;
 	}
 
 	public final SimpleStringProperty getPwdProperty() {
 		return pwdProperty;
 	}
 
 	public final SimpleStringProperty getProtocolProperty() {
 		return protocolProperty;
 	}
 
 	public final SimpleBooleanProperty getSslProperty() {
 		return sslProperty;
 	}
 
 	public final SimpleIntegerProperty getPortProperty() {
 		return portProperty;
 	}
 
 	private void trustAllSslCertificate() throws NoSuchAlgorithmException, KeyManagementException {
 		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
 			@Override
 			public X509Certificate[] getAcceptedIssuers() {
 				return null;
 			}
 
 			@Override
 			public void checkServerTrusted(X509Certificate[] c, String s) throws CertificateException {
 			}
 
 			@Override
 			public void checkClientTrusted(X509Certificate[] c, String s) throws CertificateException {
 			}
 		} };
 
 		final SSLContext sc = SSLContext.getInstance("SSL");
 		sc.init(null, trustAllCerts, new java.security.SecureRandom());
 
 		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		HostnameVerifier allHostsValid = new HostnameVerifier() {
 			@Override
 			public boolean verify(String host, SSLSession session) {
 				logger.warn("auto verify host: {}", host);
 				return true;
 			}
 		};
 		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 	}
 
 	private String sendSwitchCmd(String switchcmd, String sid) throws MalformedURLException, IOException, ServiceNotSupportedException {
 		return sendSwitchCmd(null, switchcmd, sid);
 	}
 
 	private String sendSwitchCmd(String ain, String switchcmd, String sid) throws MalformedURLException, IOException, ServiceNotSupportedException {
 		final String path;
 		if (ain == null) {
 			path = String.format(SID_SWITCHCMD_URL, switchcmd, sid);
 		} else {
 			path = String.format(SID_AIN_SWITCHCMD_URL, ain, switchcmd, sid);
 		}
 		HttpURLConnection con = createConnection(path);
 
 		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
 			invalidateSidCache();
 			throw new IOException("response code: " + con.getResponseCode());
 		}
 		sidCacheTimeProperty.set(System.currentTimeMillis());
 
 		StringBuilder response = new StringBuilder();
 		try (Scanner scanner = new Scanner(con.getInputStream())) {
 			while (scanner.hasNextLine()) {
 				response.append(scanner.nextLine());
 			}
 		}
 		if (response.indexOf("filename=/webservices/homeautoswitch.lua") > 0) {
 			throw new ServiceNotSupportedException(response.toString());
 		}

 		return response.toString();
 
 	}
 
 	private String getCachedSessionId() throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
 		if (sidCacheProperty.get() == null || System.currentTimeMillis() - sidCacheTimeProperty.get() > SID_CACHE_TIMEOUT) {
 			sidCacheProperty.set(getSessionId());
 			sidCacheTimeProperty.set(System.currentTimeMillis());
 			logger.info("update session id: {}", sidCacheProperty.get());
 		}
 
 		return sidCacheProperty.get();
 	}
 
 	private String getSessionId() throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
 
 		HttpURLConnection con = createConnection(SID_REQUEST_URL);
 		Document doc = null;
 
 		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
 			throw new IOException("response code: " + con.getResponseCode());
 		}
 		doc = xmlFactory.newDocumentBuilder().parse(con.getInputStream());
 		logger.debug("{}", XmlUtils.docToString(doc, true));
 
 		if (doc == null) {
 			throw new IOException("SessionInfo element not available");
 		}
 
 		String sid = XmlUtils.getValue(doc.getDocumentElement(), "SID");
 
 		if (EMPTY_SID.equals(sid)) {
 			String challenge = XmlUtils.getValue(doc.getDocumentElement(), "Challenge");
 			sid = getSessionId(challenge);
 		}
 
 		if (sid == null || EMPTY_SID.equals(sid)) {
 			throw new IOException("sid request failed: " + sid);
 		}
 
 		return sid;
 
 	}
 
 	private String getSessionId(String challenge) throws IOException, ParserConfigurationException, SAXException, NoSuchAlgorithmException {
 		HttpURLConnection con = createConnection(String.format(SID_CREDENTIAL_URL, userProperty.get(), FritzUtils.getChallengeResponse(challenge, pwdProperty.get())));
 
 		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
 			throw new IOException("response code: " + con.getResponseCode());
 		}
 		Document doc2 = xmlFactory.newDocumentBuilder().parse(con.getInputStream());
 		logger.debug("{}", XmlUtils.docToString(doc2, true));
 		String sid = XmlUtils.getValue(doc2.getDocumentElement(), "SID");
 
 		return sid;
 
 	}
 
 	private final HttpURLConnection createConnection(String path) throws MalformedURLException, IOException {
 		URL url = new URL(protocolProperty.get(), hostProperty.get(), portProperty.get(), path);
 		HttpURLConnection con = (HttpURLConnection) url.openConnection();
 		logger.debug("Sending request to URL: {}", url);
 		return con;
 	}
 
 	public String getURL() {
 		try {
 			URL url = new URL(protocolProperty.get(), hostProperty.get(), portProperty.get(), "");
 			return url.toString();
 		} catch (MalformedURLException e) {
 			logger.error(e.getMessage(), e);
 			return e.getMessage();
 		}
 
 	}
 
 	public void validateConnection() throws Exception {
 		String sid = getCachedSessionId();
 		if (sid == null || EMPTY_SID.equals(sid) || sid.length() != EMPTY_SID.length()) {
 			throw new Exception("general error: " + sid);
 		}
 	}
 
 	public Collection<SwitchDevice> getSwitchDevices() throws ServiceNotSupportedException, Exception {
 		String sid = getCachedSessionId();
 
 		String resp = sendSwitchCmd(SwitchCmd.GETSWITCHLIST, sid);
 		List<String> ainList = Arrays.asList(resp.split(","));
 		logger.debug("GETSWITCHLIST: {}", ainList);
 
 		Set<SwitchDevice> deviceList = new HashSet<>();
 		for (String ain : ainList) {
 			if (!deviceList.add(new SwitchDevice(ain))) {
 				logger.error("double ain: {} detected", ain);
 			}
 		}
 
 		for (final SwitchDevice dev : deviceList) {
 			refreshSwitchDevice(dev);
 			logger.info("updated: {}", dev);
 		}
 		return deviceList;
 
 	}
 
 	public void updateDeviceState(final SwitchDevice dev) throws Exception {
 		logger.info("try to change device state: {}", dev);
 
 		String sid = getSessionId();
 
 		switch (dev.getState()) {
 		case ON:
 			dev.setState(sendSwitchCmd(dev.getAin(), SwitchCmd.SETSWITCHON, sid));
 			break;
 		case OFF:
 			dev.setState(sendSwitchCmd(dev.getAin(), SwitchCmd.SETSWITCHOFF, sid));
 			break;
 		default:
 			dev.setState(sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHSTATE, sid));
 			break;
 		}
 	}
 
 	public void refreshSwitchDevice(final SwitchDevice dev) throws Exception {
 		String sid = getCachedSessionId();
 
 		dev.setPresent("1".equals(sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHPRESENT, sid)));
 		dev.setName(sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHNAME, sid));
 		dev.setState(sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHSTATE, sid));
 		try {
 			String power = sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHPOWER, sid);
 			dev.setPower("inval".equals(power) ? 0 : Integer.valueOf(power));
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			dev.setPower(-1);
 		}
 		try {
 			String energy = sendSwitchCmd(dev.getAin(), SwitchCmd.GETSWITCHENERGY, sid);
 			dev.setEnergy("inval".equals(energy) ? 0 : Integer.valueOf(energy));
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			dev.setEnergy(-1);
 		}
 
 		logger.debug("updated: {}", dev);
 
 	}
 
 }
