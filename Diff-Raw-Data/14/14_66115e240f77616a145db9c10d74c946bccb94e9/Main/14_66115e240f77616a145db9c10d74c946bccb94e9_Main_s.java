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
 
 package org.glite.security.voms.admin.server;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.configuration.VOMSConfigurationConstants;
 import org.glite.security.voms.admin.util.SysconfigUtil;
 import org.italiangrid.utils.https.JettyAdminService;
 import org.italiangrid.utils.https.JettyRunThread;
 import org.italiangrid.utils.https.JettyShutdownTask;
 import org.italiangrid.utils.https.SSLOptions;
 import org.italiangrid.utils.https.ServerFactory;
 import org.italiangrid.utils.https.impl.canl.CANLListener;
 import org.italiangrid.voms.util.CertificateValidatorBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import eu.emi.security.authn.x509.X509CertChainValidatorExt;
 
 public class Main {
 
 	public static final Logger log = LoggerFactory.getLogger(Main.class);
 	
 	public static final String DEFAULT_WAR = "/usr/share/webapps/voms-admin.war";
 	
 	public static final String DEFAULT_HOST = "localhost";
 	public static final String DEFAULT_PORT = "15000";
 	public static final String DEFAULT_SHUTDOWN_PORT="15001";
 	
 	public static final String DEFAULT_CERT = "/etc/grid-security/hostcert.pem";
 	public static final String DEFAULT_KEY = "/etc/grid-security/hostkey.pem";
 	public static final String DEFAULT_TRUSTSTORE_DIR = "/etc/grid-security/certificates";
 	public static final String DEFAULT_TMP_PREFIX = "/var/tmp";
 	
	public static final long DEFAULT_TRUSTSTORE_REFRESH_INTERVAL = 60000L;
 
 	public static final int DEFAULT_MAX_CONNECTIONS = 50;
 	public static final int DEFAULT_MAX_REQUEST_QUEUE_SIZE = 200;
 	
 	private static final String ARG_WAR = "war";
 	private static final String ARG_WAR_DIR = "wardir";
 	private static final String ARG_VO = "vo";
 	private static final String ARG_CONFDIR = "confdir";
 	
 	
 	private Options cliOptions;
 	private CommandLineParser parser = new GnuParser();
 
 	private String war;
 	private String warDirectory;
 	private String vo;
 	private String confDir;
 	
 	private String host;
 	private String port;
 	private String shutdownPort;
 	private String shutdownPassword;
 	        
 	private String certFile;
 	private String keyFile;
 	private String trustDir;
 	private long trustDirRefreshIntervalInMsec;
 	
 	private Server server;
 	private JettyAdminService shutdownService;
 	
 	private WebAppContext vomsWebappContext;
 	
 	private File jettyTmpDir;
 	
 	private void initOptions(){
 		
 		cliOptions = new Options();
 
 		cliOptions.addOption(ARG_WAR, true, "The WAR used to start this server.");
 		cliOptions.addOption(ARG_WAR_DIR, true, "The directory where the unpacked VOMS Admin war lives.");
 		cliOptions.addOption(ARG_CONFDIR, true, "The configuration directory where VOMS configuration is stored.");
 		
 		Option voOption = new Option(ARG_VO, true,"The VO this server will run for.");
 		voOption.setRequired(true);
 		
 		cliOptions.addOption(voOption);
 		
 	}
 	
 	private void failAndExit(String errorMessage, Throwable t){
 		
 		if (t != null) {
 
 			System.err.format("%s: %s\n", errorMessage, t.getMessage());
 			
 		} else {
 			
 			System.err.println(errorMessage);
 			
 		}
 		
 		System.exit(1);
 		
 	}
 	
 	private void checkStartupConfiguration(){
 		
 		if (warDirectory == null){
 			File warFile = new File(war);
 		
 			if (!warFile.canRead() || !warFile.isFile()){
 				log.error("Web archive file is not readable or is not a regular file!");
 				System.exit(1);
 			}
 		}
 	}
 	
 	private void parseCommandLineOptions(String[] args){
 		
 		try {
 			
 			CommandLine cmdLine = parser.parse(cliOptions, args);
 			
 			Properties sysconfigProperties = SysconfigUtil.loadSysconfig();
 			String installationPrefix = SysconfigUtil.getInstallationPrefix();
 			
 			String defaultPrefixedWarPath = String.format("%s/%s",installationPrefix,DEFAULT_WAR).replaceAll("/+", "/"); 
 			
 			war = cmdLine.getOptionValue(ARG_WAR, defaultPrefixedWarPath);
 			warDirectory = cmdLine.getOptionValue(ARG_WAR_DIR);
 			
 			vo = cmdLine.getOptionValue(ARG_VO);
 			confDir = cmdLine.getOptionValue(ARG_CONFDIR);
 			
 			if (confDir == null)
 				confDir = sysconfigProperties.getProperty(SysconfigUtil.SYSCONFIG_CONF_DIR);
 			
 			System.setProperty(VOMSConfigurationConstants.VO_NAME, vo);
 			
 			
 		} catch (ParseException e) {
 			
 			failAndExit("Error parsing command line arguments", e);
 
 		}
 	}
 	
 	private void configureLogging() {
 		
 		String loggingConf = String.format("%s/%s/%s", confDir, vo, "logback.xml");
 		
 		File f = new File(loggingConf);
 
 		if (!f.exists())
 			failAndExit(String.format("Logging configuration not found at path '%s'",loggingConf), null);
 		
 		if (!f.canRead())
 			failAndExit(String.format("Logging configuration is not readable: '%s'", loggingConf), null);
 		
 		LoggerContext lc = (LoggerContext) LoggerFactory
 				.getILoggerFactory();
 		
 		JoranConfigurator configurator = new JoranConfigurator();
 		
 		configurator.setContext(lc);
 		lc.reset();
 		
 		try {
 			configurator.doConfigure(f);
 		
 		} catch (JoranException e) {
 			
 			failAndExit("Error setting up the logging system",e);
 		
 		}
 		
 	}
 	
 	
 	private void loadConfiguration() {
 		
 		VOMSConfiguration conf = VOMSConfiguration.load(null);
 		
 		conf.loadServiceProperties();
 		
 		host = conf.getString(VOMSConfigurationConstants.VOMS_SERVICE_HOSTNAME, DEFAULT_HOST);
 		port = conf.getString(VOMSConfigurationConstants.VOMS_SERVICE_PORT, DEFAULT_PORT);
 		shutdownPort = conf.getString(VOMSConfigurationConstants.SHUTDOWN_PORT, DEFAULT_SHUTDOWN_PORT);
 		shutdownPassword = conf.getString(VOMSConfigurationConstants.SHUTDOWN_PASSWORD, null);
 		
 		certFile = conf.getString(VOMSConfigurationConstants.VOMS_SERVICE_CERT, DEFAULT_CERT);
 		keyFile = conf.getString(VOMSConfigurationConstants.VOMS_SERVICE_KEY, DEFAULT_KEY);
 		
 		trustDir = conf.getString(VOMSConfigurationConstants.TRUST_ANCHORS_DIR);
		trustDirRefreshIntervalInMsec = conf.getLong(VOMSConfigurationConstants.TRUST_ANCHORS_REFRESH_PERIOD, 
				DEFAULT_TRUSTSTORE_REFRESH_INTERVAL);
 		
 		VOMSConfiguration.dispose();
 		
 	}
 	
 	private void logStartupConfiguration(){
 		log.info("Starting VOMS web services for VO {} binding on {}:{}", new Object[]{vo,host, port});
 		
 		if (warDirectory != null){
 			log.info("Unpacked web archive location: {}", warDirectory);
 		}else{
 			log.info("Web archive location: {}", war);
 		}
 		
 		log.info("Service credentials: {}, {}", certFile, keyFile);
 		log.info("Trust anchors directory: {}", trustDir);
 		log.info("Trust anchors directory refresh interval (in msecs): {}", trustDirRefreshIntervalInMsec);
 		log.info("Jetty temporary directory: {}", jettyTmpDir.toString());
 	}
 	
 	
 	public Main(String[] args) {
 		try{
 			
 			initOptions();
 			parseCommandLineOptions(args);
 			configureLogging();
 		}catch (Throwable t){
 			// Here we print the error to standard error as the logging setup could be incomplete
 			System.err.println("Error starting voms-admin server: "+t.getMessage());
 			t.printStackTrace(System.err);
 			System.exit(1);
 		}
 		
 		try{
 			loadConfiguration();
 			initJettyTmpDir();
 			logStartupConfiguration();
 			checkStartupConfiguration();
 		
 			configureJettyServer();
 			configureShutdownService();
 			start();
 		}catch(Throwable t){
 			log.error("Error starting voms-admin server:"+t.getMessage(), t);
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Initializes the Jetty temp directory as the default directory created by Jetty confuses
 	 * xwork which has a bug and doesn't find classes when the WAR is expanded in the tmp directory.
 	 * 
 	 * TODO: check if recent versions of xwork solve this.
 	 */
 	protected void initJettyTmpDir(){
 				
 		String baseDirPath = String.format("%s/%s/%s", DEFAULT_TMP_PREFIX,"voms-webapp", vo).replaceAll("/+", "/");
 		
 		File basePath = new File(baseDirPath);
 		
 		if (!basePath.exists()){
 			basePath.mkdirs();
 		}
 		
 		jettyTmpDir=basePath;
 	}
 
 	protected SSLOptions getSSLOptions(){
 		SSLOptions options = new SSLOptions();
 
 		options.setCertificateFile(certFile);
 		options.setKeyFile(keyFile);
 		options.setTrustStoreDirectory(trustDir);
 		options.setTrustStoreRefreshIntervalInMsec(trustDirRefreshIntervalInMsec);
 		
 		return options;
 		
 	}
 	
 	protected void setupVOName(){
 		
 		System.setProperty("VO_NAME", vo);
 		
 	}
 			
 	protected void configureWebApp(){
 		
 		vomsWebappContext = new WebAppContext();
 		vomsWebappContext.setContextPath(String.format("/voms/%s", vo));
 		vomsWebappContext.setTempDirectory(jettyTmpDir);
 		
 		if (warDirectory != null){
 			String webXMLPath = String.format("%s/WEB-INF/web.xml", warDirectory);
 			vomsWebappContext.setDescriptor(webXMLPath);
 			vomsWebappContext.setResourceBase(warDirectory);
 		}else
 			vomsWebappContext.setWar(war);
 		
 		vomsWebappContext.setParentLoaderPriority(true);
 		
 		vomsWebappContext.setInitParameter("VO_NAME", vo);
 		vomsWebappContext.setInitParameter("CONF_DIR", confDir);
 	}
 		
 	
 	protected void configureJettyServer(){
 		
 		SSLOptions options = getSSLOptions();
 		
 		CANLListener l = new CANLListener();
 		
 		X509CertChainValidatorExt validator = CertificateValidatorBuilder.
 				buildCertificateValidator(options.getTrustStoreDirectory(),
 						l,
 						l,
 						options.getTrustStoreRefreshIntervalInMsec());
 		
 		server = ServerFactory.newServer(host, 
 				Integer.parseInt(port),
 				getSSLOptions(),
 				validator,
 				DEFAULT_MAX_CONNECTIONS,
 				DEFAULT_MAX_REQUEST_QUEUE_SIZE);
 		
 		configureWebApp();
 		
 		HandlerCollection handlers = new HandlerCollection();
 		
 		handlers.setHandlers(new Handler[]{
 				vomsWebappContext});
 		
 		server.setHandler(handlers);
 		
 	}
 	
 	protected void configureShutdownService(){
 		 
 		shutdownService = new JettyAdminService("localhost",  Integer.parseInt(shutdownPort), shutdownPassword);
 		shutdownService.registerShutdownTask(new JettyShutdownTask(server));
 		
 	}
 	
 	private void checkStatus() throws Throwable{
 		
 		if (vomsWebappContext.getUnavailableException() != null)
 			throw vomsWebappContext.getUnavailableException();
 	}
 	
 	private void start() {
 		
 		JettyRunThread vomsService =  new JettyRunThread(server);
 		
 		vomsService.start();
 		
 		try{
 			
 			checkStatus();
 			shutdownService.start();
 			
 		
 		} catch(Throwable t){
 			
 			log.error("Error starting VOMS admin server: {}", t.getClass().getName(), t);
 			System.exit(-1);
 		
 		}
 	}
 
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		new Main(args);
 
 	}
 
 }
