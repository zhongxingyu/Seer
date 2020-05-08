 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.configuration;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.security.GeneralSecurityException;
 import java.security.NoSuchProviderException;
 import java.security.PrivateKey;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.configuration.CompositeConfiguration;
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.JNDIConfiguration;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.configuration.SystemConfiguration;
 import org.apache.commons.ssl.PKCS8Key;
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.operations.VOMSPermission;
 import org.glite.security.voms.admin.util.DNUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import ch.qos.logback.core.util.StatusPrinter;
 
 public final class VOMSConfiguration {
	
	private static volatile VOMSConfiguration instance = null;
 
 	private static final String[] voRuntimeProperties = new String[] {
 			"VO_NAME", "GLITE_LOCATION", "GLITE_LOCATION_VAR", "VOMS_LOCATION" };
 
	public synchronized static VOMSConfiguration load(ServletContext context) {
 
 		if (instance != null)
 			throw new VOMSConfigurationException(
 					"VOMS configuration already loaded!");
 
 		instance = new VOMSConfiguration(context);
 
 		return instance;
 
 	}
 
 	public static VOMSConfiguration instance() {
 
 		if (instance == null)
 			throw new VOMSConfigurationException(
 					"VOMS configuration not loaded! Use the load method to load it.");
 
 		return instance;
 
 	}
 
 	private CompositeConfiguration config;
 
 	private ServletContext context;
 
 	Logger log = LoggerFactory.getLogger(VOMSConfiguration.class);
 
 	private X509Certificate serviceCertificate;
 
 	private PrivateKey servicePrivateKey;
 
 	private VOMSConfiguration(ServletContext ctxt) {
 
 		config = new CompositeConfiguration();
 
 		loadVersionProperties();
 
 		if (ctxt != null) {
 			context = ctxt;
 			loadVORuntimeProperties();
 			configureLogback();
 
 			if (!getVOName().equals("siblings")) {
 
 				loadServiceProperties();
 				if (getBoolean(
 						VOMSConfigurationConstants.VOMS_AA_SAML_ACTIVATE_ENDPOINT,
 						false))
 					loadServiceCredentials();
 
 			}
 
 		} else {
 
 			config.addConfiguration(new SystemConfiguration());
 
 		}
 
 		log.debug("VOMS-Admin configuration loaded!");
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#addProperty(java.lang.
 	 * String, java.lang.Object)
 	 */
 	public void addProperty(String arg0, Object arg1) {
 
 		config.addProperty(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.commons.configuration.Configuration#clear()
 	 */
 	public void clear() {
 
 		config.clear();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#clearProperty(java.lang
 	 * .String)
 	 */
 	public void clearProperty(String arg0) {
 
 		config.clearProperty(arg0);
 	}
 
 	private void configureLogback() {
 
 		if (context != null) {
 
 			InputStream is = null;
 			try {
 
 				is = getExternalLogbackConfiguration();
 
 			} catch (FileNotFoundException f) {
 
 				log.warn(f.getMessage());
 				if (log.isDebugEnabled()) {
 
 					log.warn(f.getMessage(), f);
 					log
 							.warn("External logging configuration not found, will use internal configuration instead.");
 
 				}
 			}
 
 			if (is == null)
 				is = context
 						.getResourceAsStream("/WEB-INF/classes/logback.runtime.xml");
 
 			if (is == null)
 				throw new VOMSConfigurationException(
 						"Error configuring logging system: logback.runtime.xml not found in application context!");
 
 			try {
 				LoggerContext lc = (LoggerContext) LoggerFactory
 						.getILoggerFactory();
 				JoranConfigurator configurator = new JoranConfigurator();
 
 				configurator.setContext(lc);
 				lc.reset();
 
 				configurator.doConfigure(is);
 				StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
 
 			} catch (JoranException e) {
 				e.printStackTrace();
 				throw new VOMSConfigurationException(
 						"Error configuring logging system: " + e.getMessage(),
 						e);
 
 			}
 
 		}
 	}
 
 	/*
 	 * private void configureLogging() {
 	 * 
 	 * if (context != null) {
 	 * 
 	 * try {
 	 * 
 	 * Properties log4jProps = new Properties();
 	 * 
 	 * InputStream is= null;
 	 * 
 	 * try{
 	 * 
 	 * is = getExternalLoggingConfiguration();
 	 * 
 	 * }catch(FileNotFoundException f){ log.warn(f.getMessage());log.warn(
 	 * "External logging configuration not found, will use internal configuration instead."
 	 * ); }
 	 * 
 	 * if (is == null) is = context
 	 * .getResourceAsStream("/WEB-INF/classes/log4j.runtime.properties");
 	 * 
 	 * if (is == null) throw new VOMSConfigurationException(
 	 * "Error configuring logging system: log4j.runtime.properties not found in application context!"
 	 * );
 	 * 
 	 * log4jProps.load(is); log4jProps.setProperty("log4j.vo", getVOName());
 	 * 
 	 * LogManager.resetConfiguration();
 	 * PropertyConfigurator.configure(log4jProps);
 	 * 
 	 * } catch (MalformedURLException e) {
 	 * 
 	 * log.error("Error configuring logging properties! " + e.getMessage(), e);
 	 * 
 	 * } catch (IOException e) {
 	 * log.error("Error configuring logging properties! " + e.getMessage(), e);
 	 * 
 	 * throw new VOMSConfigurationException(e.getMessage(), e); }
 	 * 
 	 * }
 	 * 
 	 * }
 	 */
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#containsKey(java.lang.
 	 * String)
 	 */
 	public boolean containsKey(String arg0) {
 
 		return config.containsKey(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBigDecimal(java.lang
 	 * .String)
 	 */
 	public BigDecimal getBigDecimal(String arg0) {
 
 		return config.getBigDecimal(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBigDecimal(java.lang
 	 * .String, java.math.BigDecimal)
 	 */
 	public BigDecimal getBigDecimal(String arg0, BigDecimal arg1) {
 
 		return config.getBigDecimal(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBigInteger(java.lang
 	 * .String)
 	 */
 	public BigInteger getBigInteger(String arg0) {
 
 		return config.getBigInteger(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBigInteger(java.lang
 	 * .String, java.math.BigInteger)
 	 */
 	public BigInteger getBigInteger(String arg0, BigInteger arg1) {
 
 		return config.getBigInteger(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBoolean(java.lang.String
 	 * )
 	 */
 	public boolean getBoolean(String arg0) {
 
 		return config.getBoolean(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBoolean(java.lang.String
 	 * , boolean)
 	 */
 	public boolean getBoolean(String arg0, boolean arg1) {
 
 		return config.getBoolean(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getBoolean(java.lang.String
 	 * , java.lang.Boolean)
 	 */
 	public Boolean getBoolean(String arg0, Boolean arg1) {
 
 		return config.getBoolean(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getByte(java.lang.String)
 	 */
 	public byte getByte(String arg0) {
 
 		return config.getByte(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getByte(java.lang.String,
 	 * byte)
 	 */
 	public byte getByte(String arg0, byte arg1) {
 
 		return config.getByte(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getByte(java.lang.String,
 	 * java.lang.Byte)
 	 */
 	public Byte getByte(String arg0, Byte arg1) {
 
 		return config.getByte(arg0, arg1);
 	}
 
 	public String getCustomizationPageAbsolutePath(String pageName) {
 		String basePath = getCustomizedContentPath() + "/" + pageName;
 		File f = new File(basePath);
 
 		if (f.exists() && f.canRead())
 			return f.getAbsolutePath();
 
 		return null;
 
 	}
 
 	public String getConfigurationDirectoryPath() {
 
 		String path = config.getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName();
 
 		return path;
 	}
 
 	private String getCustomizedContentPath() {
 
 		String pathName = config.getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName() + "/customized-content/";
 		return pathName;
 	}
 
 	public Properties getDatabaseProperties() {
 
 		String propFileName = getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName()
 				+ "/voms.database.properties";
 
 		Properties props = new Properties();
 
 		try {
 			props.load(new FileInputStream(propFileName));
 
 		} catch (IOException e) {
 
 			log
 					.error("Error loading database properties: "
 							+ e.getMessage(), e);
 			throw new VOMSException("Error loading database properties: "
 					+ e.getMessage(), e);
 		}
 
 		return props;
 
 	}
 
 	public String getDefaultGridAUPURL() {
 		return String.format("file://%s/etc/voms-admin/%s/grid-aup.txt",
 				getString("GLITE_LOCATION_VAR"), getVOName());
 	}
 
 	public String getDefaultVOAUPURL() {
 		return String.format("file://%s/etc/voms-admin/%s/vo-aup.txt",
 				getString("GLITE_LOCATION_VAR"), getVOName());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getDouble(java.lang.String
 	 * )
 	 */
 	public double getDouble(String arg0) {
 
 		return config.getDouble(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getDouble(java.lang.String
 	 * , double)
 	 */
 	public double getDouble(String arg0, double arg1) {
 
 		return config.getDouble(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getDouble(java.lang.String
 	 * , java.lang.Double)
 	 */
 	public Double getDouble(String arg0, Double arg1) {
 
 		return config.getDouble(arg0, arg1);
 	}
 
 	private InputStream getExternalLogbackConfiguration()
 			throws FileNotFoundException {
 
 		String path = getString("GLITE_LOCATION_VAR") + "/etc/voms-admin/"
 				+ getVOName() + "/logback.runtime.xml";
 
 		File f = new File(path);
 
 		if (!f.exists())
 			log.warn("External logging configuration not found at path '"
 					+ path + "'... ");
 		if (!f.canRead())
 			log.warn("External logging configuration is not readable: '" + path
 					+ "'... ");
 
 		return new FileInputStream(f);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getFloat(java.lang.String)
 	 */
 	public float getFloat(String arg0) {
 
 		return config.getFloat(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getFloat(java.lang.String,
 	 * float)
 	 */
 	public float getFloat(String arg0, float arg1) {
 
 		return config.getFloat(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getFloat(java.lang.String,
 	 * java.lang.Float)
 	 */
 	public Float getFloat(String arg0, Float arg1) {
 
 		return config.getFloat(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getInt(java.lang.String)
 	 */
 	public int getInt(String arg0) {
 
 		return config.getInt(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getInt(java.lang.String,
 	 * int)
 	 */
 	public int getInt(String arg0, int arg1) {
 
 		return config.getInt(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getInteger(java.lang.String
 	 * , java.lang.Integer)
 	 */
 	public Integer getInteger(String arg0, Integer arg1) {
 
 		return config.getInteger(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.commons.configuration.Configuration#getKeys()
 	 */
 	public Iterator getKeys() {
 
 		return config.getKeys();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getKeys(java.lang.String)
 	 */
 	public Iterator getKeys(String arg0) {
 
 		return config.getKeys(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getList(java.lang.String)
 	 */
 	public List getList(String arg0) {
 
 		return config.getList(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getList(java.lang.String,
 	 * java.util.List)
 	 */
 	public List getList(String arg0, List arg1) {
 
 		return config.getList(arg0, arg1);
 	}
 
 	public List getLocallyConfiguredVOs() {
 
 		String configDirPath = getString("GLITE_LOCATION_VAR");
 
 		if (configDirPath == null)
 			throw new VOMSConfigurationException(
 					"No value found for GLITE_LOCATION_VAR!");
 
 		List voList = new ArrayList();
 
 		File configDir = new File(configDirPath + File.separator + "etc"
 				+ File.separator + "voms-admin");
 
 		if (!configDir.exists())
 			throw new VOMSConfigurationException(
 					"Voms configuration directory does not exist");
 
 		File[] filez = configDir.listFiles();
 
 		if (filez != null)
 			for (int i = 0; i < filez.length; i++) {
 
 				if (filez[i].isDirectory()) {
 					log.debug("Found vo: " + filez[i].getName());
 					voList.add(filez[i].getName());
 				}
 
 			}
 
 		return voList;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getLong(java.lang.String)
 	 */
 	public long getLong(String arg0) {
 
 		return config.getLong(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getLong(java.lang.String,
 	 * long)
 	 */
 	public long getLong(String arg0, long arg1) {
 
 		return config.getLong(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getLong(java.lang.String,
 	 * java.lang.Long)
 	 */
 	public Long getLong(String arg0, Long arg1) {
 
 		return config.getLong(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getProperties(java.lang
 	 * .String)
 	 */
 	public Properties getProperties(String arg0) {
 
 		return config.getProperties(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getProperty(java.lang.
 	 * String)
 	 */
 	public Object getProperty(String arg0) {
 
 		return config.getProperty(arg0);
 	}
 
 	public X509Certificate getServiceCertificate() {
 		return serviceCertificate;
 	}
 
 	public PrivateKey getServicePrivateKey() {
 
 		return servicePrivateKey;
 	}
 
 	public ServletContext getServletContext() {
 		return context;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getShort(java.lang.String)
 	 */
 	public short getShort(String arg0) {
 
 		return config.getShort(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getShort(java.lang.String,
 	 * short)
 	 */
 	public short getShort(String arg0, short arg1) {
 
 		return config.getShort(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getShort(java.lang.String,
 	 * java.lang.Short)
 	 */
 	public Short getShort(String arg0, Short arg1) {
 
 		return config.getShort(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getString(java.lang.String
 	 * )
 	 */
 	public String getString(String arg0) {
 
 		return config.getString(arg0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getString(java.lang.String
 	 * , java.lang.String)
 	 */
 	public String getString(String arg0, String arg1) {
 
 		return config.getString(arg0, arg1);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.apache.commons.configuration.Configuration#getStringArray(java.lang
 	 * .String)
 	 */
 	public String[] getStringArray(String arg0) {
 
 		return config.getStringArray(arg0);
 	}
 
 	private String getVomsDatabasePropertiesFileName() {
 
 		String fileName = config.getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName()
 				+ "/voms.database.properties";
 
 		log.debug("voms.database.properties=" + fileName);
 
 		return fileName;
 
 	}
 
 	public String getVomsesConfigurationString() {
 
 		if (getString("vomses.configuration") == null) {
 			String vomsesConfString = loadVomsesConfigurationString();
 			setProperty("vomses.configuration", vomsesConfString);
 		}
 
 		return getString("vomses.configuration");
 
 	}
 
 	private String getVomsServicePropertiesFileName() {
 
 		String fileName = config.getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName() + "/voms.service.properties";
 
 		return fileName;
 
 	}
 
 	public String getVOName() {
 
 		return getString(VOMSConfigurationConstants.VO_NAME);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.commons.configuration.Configuration#isEmpty()
 	 */
 	public boolean isEmpty() {
 
 		return config.isEmpty();
 	}
 
 	private void loadServiceCredentials() {
 
 		String certificateFileName = getString(
 				VOMSConfigurationConstants.VOMS_AA_CERT_FILE,
 				"/etc/grid-security/hostcert.pem");
 
 		String privateKeyFileName = getString(
 				VOMSConfigurationConstants.VOMS_AA_KEY_FILE,
 				"/etc/grid-security/hostkey.pem");
 
 		/* load certificate */
 		log.info("Loading credentials for VOMS Attribute authority from:"
 				+ certificateFileName + "," + privateKeyFileName);
 
 		InputStream certificateInputStream;
 		try {
 
 			certificateInputStream = new FileInputStream(certificateFileName);
 
 		} catch (FileNotFoundException e) {
 
 			log
 					.error("Error loading service credentials: "
 							+ e.getMessage(), e);
 			throw new VOMSException("Error loading service credentials: "
 					+ e.getMessage(), e);
 
 		}
 
 		CertificateFactory certificateFactory;
 
 		try {
 			certificateFactory = CertificateFactory.getInstance("X509", "BC");
 
 		} catch (CertificateException e) {
 			log.error("Error instantiating X509 certificate factory: "
 					+ e.getMessage(), e);
 			throw new VOMSException(
 					"Error instantiating X509 certificate factory: "
 							+ e.getMessage(), e);
 		} catch (NoSuchProviderException e) {
 			throw new VOMSException(
 					"Error instantiating X509 certificate factory: "
 							+ e.getMessage(), e);
 		}
 
 		try {
 			serviceCertificate = (X509Certificate) certificateFactory
 					.generateCertificate(certificateInputStream);
 		} catch (CertificateException e) {
 			log.error("Error generating X509 certificate from input stream: "
 					+ e.getMessage(), e);
 			throw new VOMSException(
 					"Error generating X509 certificate from input stream: "
 							+ e.getMessage(), e);
 		}
 
 		try {
 
 			certificateInputStream.close();
 
 		} catch (IOException e) {
 			log.error("Error closing certificate input stream:"
 					+ e.getMessage(), e);
 			throw new VOMSException("Error closing certificate input stream:"
 					+ e.getMessage(), e);
 		}
 
 		log.info("SAML service credential's DN: "
 				+ DNUtil.getBCasX500(serviceCertificate
 						.getSubjectX500Principal()));
 
 		/* load private key */
 		FileInputStream fileInputStream = null;
 
 		try {
 			fileInputStream = new FileInputStream(privateKeyFileName);
 
 		} catch (FileNotFoundException e) {
 
 			log.error("Error opening private key file:" + e.getMessage(), e);
 			throw new VOMSException("Error opening private key file:"
 					+ e.getMessage(), e);
 		}
 
 		PKCS8Key pkcs8 = null;
 
 		try {
 			pkcs8 = new PKCS8Key(fileInputStream, "".toCharArray());
 
 		} catch (GeneralSecurityException e) {
 
 			log.error("Error parsing private key from input stream:"
 					+ e.getMessage(), e);
 			throw new VOMSException(
 					"Error parsing private key from input stream:"
 							+ e.getMessage(), e);
 
 		} catch (IOException e) {
 
 			log.error("Error opening private key file:" + e.getMessage(), e);
 			throw new VOMSException("Error opening private key file:"
 					+ e.getMessage(), e);
 
 		}
 
 		servicePrivateKey = pkcs8.getPrivateKey();
 
 	}
 
 	private void loadServiceProperties() {
 
 		String fileName = getVomsServicePropertiesFileName();
 
 		try {
 
 			PropertiesConfiguration vomsServiceProperties = new PropertiesConfiguration(
 					getVomsServicePropertiesFileName());
 			config.addConfiguration(vomsServiceProperties);
 
 		} catch (ConfigurationException e) {
 			log.error("Error loading service properties from " + fileName);
 
 			if (log.isDebugEnabled())
 				log.error(e.getMessage(), e);
 
 			throw new VOMSConfigurationException(
 					"Error loading service properties from " + fileName, e);
 
 		}
 
 		log.debug("VOMS Admin service properties loaded!");
 
 	}
 
 	private void loadVersionProperties() {
 
 		InputStream versionPropStream = this.getClass().getClassLoader()
 				.getResourceAsStream("version.properties");
 		PropertiesConfiguration versionProperties = new PropertiesConfiguration();
 
 		try {
 			versionProperties.load(versionPropStream);
 
 			config.addConfiguration(versionProperties);
 
 		} catch (ConfigurationException e) {
 
 			log.error("Error configuring version properties:" + e.getMessage(),
 					e);
 			throw new VOMSConfigurationException(
 					"Error configuring version properties!", e);
 
 		}
 	}
 
 	private String loadVomsesConfigurationString() {
 
 		String vomsesConfFileName = getString("GLITE_LOCATION_VAR")
 				+ "/etc/voms-admin/" + getVOName() + "/vomses";
 
 		try {
 
 			StringBuffer vomsesContent = new StringBuffer();
 			FileReader vomsesFileReader = new FileReader(vomsesConfFileName);
 
 			int c;
 			while ((c = vomsesFileReader.read()) != -1)
 				vomsesContent.append((char) c);
 
 			return vomsesContent.toString();
 
 		} catch (IOException e) {
 
 			log.error("Error loading vomses configuration file:"
 					+ e.getMessage(), e);
 			throw new VOMSException("Error loading vomses configuration file:"
 					+ e.getMessage(), e);
 		}
 
 	}
 
 	private void loadVORuntimeProperties() {
 
 		JNDIConfiguration jndiConfig;
 
 		try {
 
 			jndiConfig = new JNDIConfiguration();
 
 		} catch (NamingException e) {
 			log.error("Error accessing JNDI configuration properties: "
 					+ e.getMessage());
 			if (log.isDebugEnabled())
 				log.error(e.getMessage(), e);
 
 			throw new VOMSConfigurationException(
 					"Error accessing JNDI configuration properties: "
 							+ e.getMessage(), e);
 
 		}
 
 		SystemConfiguration systemConfig = new SystemConfiguration();
 
 		if (log.isDebugEnabled()) {
 
 			log.debug("JNDI Configuration contents: ");
 			Iterator jndiKeysIter = jndiConfig.getKeys();
 
 			while (jndiKeysIter.hasNext()) {
 
 				String key = (String) jndiKeysIter.next();
 				log.debug(key + " = " + jndiConfig.getProperty(key));
 
 			}
 
 			log.debug("System Configuration contents: ");
 			Iterator systemKeysIter = systemConfig.getKeys();
 
 			while (systemKeysIter.hasNext()) {
 
 				String key = (String) systemKeysIter.next();
 				log.debug(key + " = " + systemConfig.getProperty(key));
 			}
 		}
 
 		jndiConfig.setPrefix("java:comp/env");
 
 		// Add values coming from JNDI context
 		config.addConfiguration(jndiConfig);
 
 		for (int i = 0; i < voRuntimeProperties.length; i++)
 			if (!config.containsKey(voRuntimeProperties[i])) {
 				log
 						.debug("VO runtime property '"
 								+ voRuntimeProperties[i]
 								+ "' not found in JNDI context! Checking system properties for fallback value!");
 
 				if (!systemConfig.containsKey(voRuntimeProperties[i]))
 					throw new VOMSConfigurationException(
 							"Error loading voms-admin configuration: '"
 									+ voRuntimeProperties[i]
 									+ "' VO runtime property, needed to properly initialize voms-admin services, was not found "
 									+ "in JNDI naming service and in the process environment!");
 
 				// Property value found in environment
 				config.setProperty(voRuntimeProperties[i], systemConfig
 						.getString(voRuntimeProperties[i]));
 				log.debug("VO runtime property '" + voRuntimeProperties[i]
 						+ "' found in the system properties with value: '"
 						+ systemConfig.getString(voRuntimeProperties[i]));
 
 			}
 
 		// Rename the VO_NAME property for internal use
 		// FIXME: is this really needed?
 		config.setProperty(VOMSConfigurationConstants.VO_NAME, config
 				.getString("VO_NAME"));
 		System.setProperty(VOMSConfigurationConstants.VO_NAME, config
 				.getString("VO_NAME"));
 
 	}
 
 	public boolean pageHasCustomization(String pageName) {
 
 		String basePath = getCustomizedContentPath() + "/" + pageName;
 		File f = new File(basePath);
 
 		return (f.exists() && f.canRead());
 
 	}
 
 	public void setProperty(String arg0, Object arg1) {
 
 		config.setProperty(arg0, arg1);
 	}
 
 	public Configuration subset(String arg0) {
 
 		return config.subset(arg0);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<String> getExternalValidators(){
 		return config.getList(VOMSConfigurationConstants.VOMS_EXTERNAL_VALIDATOR_LIST);
 	}
 	
 	public String getExternalValidatorConfigClass(String pluginName){
 		
 		String configClassPropertyName = String.format("%s.%s.%s", 
 					VOMSConfigurationConstants.VOMS_EXTERNAL_VALIDATOR_PREFIX,
 					pluginName,
 					VOMSConfigurationConstants.VOMS_EXTERNAL_VALIDATOR_CONFIG_SUFFIX);
 				
 		return config.getString(configClassPropertyName);
 	}
 
 	public String getExternalValidatorProperty(String pluginName, String pluginPropertyName, String defaultValue){
 		
 		String propertyName = String.format("%s.%s.%s",
 				VOMSConfigurationConstants.VOMS_EXTERNAL_VALIDATOR_PREFIX,
 				pluginName,	pluginPropertyName);
 		
 		return config.getString(propertyName, defaultValue);
 		
 	}
 	
 	public String getExternalValidatorProperty(String pluginName, String pluginPropertyName){
 		
 		String propertyName = String.format("%s.%s.%s",
 				VOMSConfigurationConstants.VOMS_EXTERNAL_VALIDATOR_PREFIX,
 				pluginName,	pluginPropertyName);
 		
 		return config.getString(propertyName);
 		
 	}
 	
 	public void setRegistrationType(String registrationType){
 		
 		config.setProperty(VOMSConfigurationConstants.VOMS_INTERNAL_REGISTRATION_TYPE, registrationType);
 	}
 	
 	public String getRegistrationType(){
 		return config.getString(VOMSConfigurationConstants.VOMS_INTERNAL_REGISTRATION_TYPE, "default");
 	}
 	
 	
 	public VOMSPermission getUnauthenticatedClientPermissionMask(){
 		
 		String unauthenticatedClientPermMask = getString(VOMSConfigurationConstants.VOMS_UNAUTHENTICATED_CLIENT_PERMISSION_MASK,
 		"CONTAINER_READ|MEMBERSHIP_READ");
 		
 		VOMSPermission permMask = null;
 		
 		try{
 			
 			permMask = VOMSPermission.fromString(unauthenticatedClientPermMask);
 			
 		}catch(IllegalArgumentException e){
 			// Parse error on user set permission mask
 			log.error("Error parsing user set permission mask for unauthenticated client: '"+unauthenticatedClientPermMask+"'");
 			log.error(e.getMessage());
 			permMask = VOMSPermission.getContainerReadPermission().setMembershipReadPermission();
 			
 		}
 		
 		return permMask;
 		
 	}
 	
 }
