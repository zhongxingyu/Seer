 /* --------------------------------------------------------------------------
  * @author Hauke Walden
  * @created 28.06.2011 
  * Copyright 2011 by Hauke Walden 
  * All rights reserved.
  * --------------------------------------------------------------------------
  */
 
 package de.mbaaba.util;
 
 /**
  * Utility class that helps to configure proxy settings, SSL keys etc.
  * 
  * @author walden_h1
  */
 public final class CommConfigUtil {
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_PROXY_SET = "proxySet";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_HTTP_PROXY_HOST = "http.proxyHost";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_HTTP_PROXY_PORT = "http.proxyPort";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_HTTPS_PROXY_HOST = "https.proxyHost";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_HTTPS_PROXY_PORT = "https.proxyPort";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_TRUST_ALL_HTTPS_CERTIFICATES = "trustAllHttpsCertificates";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_TRUST_ALL_HOSTNAMES = "trustAllHostnames";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_KEYSTORE_PWD = "keystorePwd";
 
 	/** Key that is used to access values in the configurator. */
 	public static final String PROP_KEYSTORE_LOCATION = "keystoreLocation";
 
 	/** the logging instance */
 	private static final Logger LOG = new Logger(CommConfigUtil.class);
 
 	/** The configurator instance. */
 	private static Configurator configurator;
 
 	/**
 	 * Prohibit instantiation.
 	 */
 	private CommConfigUtil() {
 	}
 
 	/**
 	 * Initializes the ClientConfigUtil.
 	 * 
 	 * @param aConfigurator
 	 *            the a configurator
 	 */
 	public static void init(Configurator aConfigurator) {
 		configurator = aConfigurator;
 		final boolean trustAllHostnames = Boolean.valueOf(configurator.getProperty(PROP_TRUST_ALL_HOSTNAMES, "false"));
 		if (trustAllHostnames) {
 			SSLUtilities.trustAllHostnames();
 		}
 		final boolean trustAllHttpsCertificates = Boolean.valueOf(configurator.getProperty(PROP_TRUST_ALL_HTTPS_CERTIFICATES, "false"));
 		if (trustAllHttpsCertificates) {
 			SSLUtilities.trustAllHttpsCertificates();
 		}
 
 		final String keystorePwd = configurator.getProperty(PROP_KEYSTORE_PWD, "");
 		final String keystoreLocation = configurator.getProperty(PROP_KEYSTORE_LOCATION, "");
 
		// configure SSL
 		LOG.debug("Using keystore from " + keystoreLocation);
 		System.setProperty("javax.net.ssl.trustStore", keystoreLocation);
 		System.setProperty("javax.net.ssl.trustStorePassword", keystorePwd);
 
 		// configure proxy settings
 
 		final String propProxySet = configurator.getProperty(PROP_PROXY_SET, "");
 		System.setProperty(PROP_PROXY_SET, propProxySet);
 
 		final String propHttpProxyHost = configurator.getProperty(PROP_HTTP_PROXY_HOST, "");
 		System.setProperty(PROP_HTTP_PROXY_HOST, propHttpProxyHost);
 
 		final String propHttpProxyPort = configurator.getProperty(PROP_HTTP_PROXY_PORT, "");
 		System.setProperty(PROP_HTTP_PROXY_PORT, propHttpProxyPort);
 
 		final String propHttpsProxyHost = configurator.getProperty(PROP_HTTPS_PROXY_HOST, "");
 		System.setProperty(PROP_HTTPS_PROXY_HOST, propHttpsProxyHost);
 
 		final String propHttpsProxyPort = configurator.getProperty(PROP_HTTPS_PROXY_PORT, "");
 		System.setProperty(PROP_HTTPS_PROXY_PORT, propHttpsProxyPort);
 
 		final String propHttpNonProxyHosts = configurator.getProperty(PROP_HTTP_NON_PROXY_HOSTS, "");
 		System.setProperty(PROP_HTTP_NON_PROXY_HOSTS, propHttpNonProxyHosts);
 
 	}
 
 }
