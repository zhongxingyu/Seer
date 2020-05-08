 package grisu.frontend.control.login;
 
 import grisu.control.ServiceInterface;
 import grisu.control.exceptions.ServiceInterfaceException;
 import grisu.frontend.control.UncaughtExceptionHandler;
 import grisu.jcommons.configuration.CommonGridProperties;
 import grisu.jcommons.constants.Constants;
 import grisu.jcommons.constants.GridEnvironment;
 import grisu.jcommons.dependencies.BouncyCastleTool;
 import grisu.jcommons.dependencies.ClasspathHacker;
 import grisu.jcommons.utils.DefaultGridSecurityProvider;
 import grisu.jcommons.utils.EnvironmentVariableHelpers;
 import grisu.jcommons.utils.JythonHelpers;
 import grisu.jcommons.view.cli.CliHelpers;
 import grisu.model.GrisuRegistryManager;
 import grisu.settings.ClientPropertiesManager;
 import grisu.settings.Environment;
 import grisu.utils.GrisuPluginFilenameFilter;
 import grith.jgrith.cred.AbstractCred;
 import grith.jgrith.cred.Cred;
 import grith.jgrith.cred.callbacks.CliCallback;
 import grith.jgrith.utils.CertificateFiles;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Date;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 
 import com.google.common.collect.ImmutableBiMap;
 import com.google.common.collect.ImmutableMap;
 
 public class LoginManager {
 
 	public static int REQUIRED_BACKEND_API_VERSION = 16;
 	public static final String DEFAULT_BACKEND = "testbed";
 	public static final int DEFAULT_PROXY_LIFETIME_IN_HOURS = 240;
 
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(LoginManager.class.getName());
 
 	public static final ImmutableBiMap<String, String> SERVICEALIASES = new ImmutableBiMap.Builder<String, String>()
 			.put("local", "Local")
 			.put("testbed",
 					"https://compute.test.nesi.org.nz/soap/GrisuService")
 					.put("bestgrid",
 							"https://compute.services.bestgrid.org/soap/GrisuService")
 							.put("nesi", "https://compute.nesi.org.nz/soap/GrisuService")
 							.put("dev",
 									"https://compute-dev.services.bestgrid.org/soap/GrisuService")
 									.put("bestgrid-test",
 											"https://compute-test.services.bestgrid.org/soap/GrisuService")
 											.put("local_ws_jetty", "http://localhost:8080/soap/GrisuService")
 											.put("local_ws", "http://localhost:8080/grisu-ws/soap/GrisuService")
 											.build();
 
 	public static final ImmutableMap<String, String> MYPROXY_SERVERS = new ImmutableMap.Builder<String, String>()
 			.put("Local",
 					GridEnvironment.getDefaultMyProxyServer() + ":"
 							+ GridEnvironment.getDefaultMyProxyPort())
 							.put("https://compute.test.nesi.org.nz/soap/GrisuService",
 									"myproxy.test.nesi.org.nz:7512")
 									.put("https://compute.nesi.org.nz/soap/GrisuService",
 											"myproxy.nesi.org.nz:7512")
 											.put("https://compute.services.bestgrid.org/soap/GrisuService",
 													"myproxy.nesi.org.nz:7512")
 													.put("https://compute-dev.services.bestgrid.org/soap/GrisuService",
 															"myproxy.nesi.org.nz:7512")
 															.put("https://compute-test.services.bestgrid.org/soap/GrisuService",
 																	"myproxy.nesi.org.nz:7512")
 																	.put("http://localhost:8080/grisu-ws/soap/GrisuService",
 																			GridEnvironment.getDefaultMyProxyServer() + ":"
 																					+ GridEnvironment.getDefaultMyProxyPort())
 																					.put("http://localhost:8080/soap/GrisuService",
 																							GridEnvironment.getDefaultMyProxyServer() + ":"
 																									+ GridEnvironment.getDefaultMyProxyPort()).build();
 
 	private static String CLIENT_NAME = setClientName(null);
 	private static String CLIENT_VERSION = setClientVersion(null);
 	public static String USER_SESSION = setUserSessionId(null);
 
 	public static volatile boolean environmentInitialized = false;
 
 	public static void addPluginsToClasspath() throws IOException {
 
 		ClasspathHacker.initFolder(Environment.getGrisuPluginDirectory(),
 				new GrisuPluginFilenameFilter());
 
 	}
 
 	public static String getClientName() {
 		return CLIENT_NAME;
 	}
 
 	public static String getClientVersion() {
 		return CLIENT_VERSION;
 	}
 
 	public static String getLoginUrl(String alias) {
 
 		String url = SERVICEALIASES.get(alias.toLowerCase());
 		if (StringUtils.isNotBlank(url)) {
 			return url;
 		} else {
 			return alias;
 		}
 
 	}
 
 	public static synchronized void initEnvironment() {
 
 		if (!environmentInitialized) {
 
 			// make sure tmp dir exists
 			String tmpdir = System.getProperty("java.io.tmpdir");
 			if (tmpdir.startsWith("~")) {
 				tmpdir = tmpdir.replaceFirst("~",
 						System.getProperty("user.home"));
 				System.setProperty("java.io.tmpdir", tmpdir);
 			}
 			File tmp = new File(tmpdir);
 			if (!tmp.exists()) {
 				myLogger.debug("Creating tmpdir: {}", tmpdir);
 				tmp.mkdirs();
 				if (!tmp.exists()) {
 					myLogger.error("Could not create tmp dir {}.", tmpdir);
 				}
 			}
 
 			java.util.logging.LogManager.getLogManager().reset();
 			// LoggerFactory.getLogger("root").setLevel(Level.OFF);
 
 			JythonHelpers.setJythonCachedir();
 
 			final String debug = CommonGridProperties
 					.getDefault()
 					.getGridProperty(
 							CommonGridProperties.Property.DEBUG_UNCAUGHT_EXCEPTIONS);
 
 			if ("true".equalsIgnoreCase(debug)) {
 				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
 			}
 
 			java.security.Security
 			.addProvider(new DefaultGridSecurityProvider());
 
 			java.security.Security
 			.setProperty("ssl.TrustManagerFactory.algorithm",
 					"TrustAllCertificates");
 
 			try {
 				BouncyCastleTool.initBouncyCastle();
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
 			try {
 				CertificateFiles.copyCACerts(false);
 			} catch (Exception e) {
 				myLogger.error("Problem copying root certificates.", e);
 			}
 
 			environmentInitialized = true;
 		}
 
 	}
 
 	public static synchronized void initGrisuClient(String clientname) {
 
 		Thread.currentThread().setName("main");
 
 		setClientName(clientname);
 
 		setClientVersion(grisu.jcommons.utils.Version
 				.get(clientname));
 
 		EnvironmentVariableHelpers.loadEnvironmentVariablesToSystemProperties();
 
 		initEnvironment();
 
 	}
 
 	public static ServiceInterface login(String backend, Cred cred,
 			boolean displayCliProgress) throws LoginException {
 
 		if (StringUtils.isBlank(backend)) {
 			String defaultUrl = ClientPropertiesManager
 					.getDefaultServiceInterfaceUrl();
 
 			if (StringUtils.isBlank(defaultUrl)) {
 				defaultUrl = DEFAULT_BACKEND;
 			}
 
 			backend = defaultUrl;
 		}
 
 		ClientPropertiesManager.setDefaultServiceInterfaceUrl(backend);
 
 		backend = getLoginUrl(backend);
 		try {
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress(
 						"Setting up environment...", true);
 			}
 			initEnvironment();
 
 			try {
 				addPluginsToClasspath();
 			} catch (final IOException e2) {
 				// TODO Auto-generated catch block
 				myLogger.warn(e2.getLocalizedMessage(), e2);
 				throw new RuntimeException(e2);
 			}
 
 			// do the cacert thingy
 			try {
 				final URL cacertURL = LoginManager.class
 						.getResource("/ipsca.pem");
 				final HttpSecureProtocol protocolSocketFactory = new HttpSecureProtocol();
 
 				TrustMaterial trustMaterial = null;
 				trustMaterial = new TrustMaterial(cacertURL);
 
 				// We can use setTrustMaterial() instead of addTrustMaterial()
 				// if we want to remove
 				// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 				protocolSocketFactory.addTrustMaterial(trustMaterial);
 
 				// Maybe we want to turn off CN validation (not recommended!):
 				protocolSocketFactory.setCheckHostname(false);
 
 				final Protocol protocol = new Protocol("https",
 						(ProtocolSocketFactory) protocolSocketFactory, 443);
 				Protocol.registerProtocol("https", protocol);
 			} catch (final Exception e) {
 				myLogger.error(e.getLocalizedMessage(), e);
 			}
 
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress("Uploading credential...",
 						true);
 			}
 
 			try {
 				cred.uploadMyProxy();
 			} catch (Exception e) {
 				throw new LoginException(
 						"Could not upload myproxy credential.", e);
 			}
 
 			ServiceInterface si;
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress("Logging in to backend...",
 						true);
 			}
 			try {
 
 				si = ServiceInterfaceFactory.createInterface(backend,
 						cred.getMyProxyUsername(), cred.getMyProxyPassword(),
 						cred.getMyProxyHost(),
 						Integer.toString(cred.getMyProxyPort()), null, -1,
 						null, null);
 				// loginParams.getHttpProxy(),
 				// loginParams.getHttpProxyPort(),
 				// loginParams.getHttpProxyUsername(),
 				// loginParams.getHttpProxyPassphrase());
 			} catch (ServiceInterfaceException e) {
 				throw new LoginException("Could not login to backend.", e);
 			} catch (Throwable t) {
 				t.printStackTrace();
 				throw new LoginException("Error while loggin in: "
 						+ t.getLocalizedMessage());
 			}
 
 			GrisuRegistryManager.registerServiceInterface(si, cred);
 			GrisuRegistryManager.getDefault(si).set(Constants.BACKEND, backend);
 
 			return si;
 		} finally {
 			if (displayCliProgress) {
 				CliHelpers.setIndeterminateProgress(false);
 
 			}
 		}
 
 	}
 
 	public static ServiceInterface loginCommandline(String backend)
 			throws LoginException {
 
 		LoginManager.initEnvironment();
 
 		AbstractCred c = AbstractCred.loadFromConfig(null, new CliCallback());
 
 		return login(backend, c, true);
 	}
 
 	public static String setClientName(String name) {
 
 		if (StringUtils.isBlank(name)) {
			name = "Unknown";
 		}
 		CLIENT_NAME = name;
 		MDC.put("client", name);
 
 		return name;
 
 	}
 
 	public static String setClientVersion(String version) {
 		if (StringUtils.isBlank(version)) {
 			version = "n/a";
 		}
 		CLIENT_VERSION = version;
 		MDC.put("client_version", version);
 
 		return version;
 	}
 
 	public static String setUserSessionId(String id) {
 		if (StringUtils.isBlank(id)) {
 			id = System.getProperty("user.name") + "_" + new Date().getTime();
 		}
 		USER_SESSION = id;
 		MDC.put("session", id);
 
 		return id;
 	}
 
 }
