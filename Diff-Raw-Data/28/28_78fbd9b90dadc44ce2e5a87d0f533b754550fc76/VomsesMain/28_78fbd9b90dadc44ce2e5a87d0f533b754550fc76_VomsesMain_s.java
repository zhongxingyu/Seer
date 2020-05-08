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
 
 package org.glite.security.voms.admin.server.vomses;
 
 import java.net.URL;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.glite.security.voms.admin.util.SysconfigUtil;
 import org.italiangrid.utils.https.JettyRunThread;
 import org.italiangrid.utils.https.SSLOptions;
 import org.italiangrid.utils.https.ServerFactory;
 import org.italiangrid.utils.https.impl.canl.CANLListener;
 import org.italiangrid.voms.util.CertificateValidatorBuilder;
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 import ch.qos.logback.core.joran.spi.JoranException;
 import eu.emi.security.authn.x509.X509CertChainValidatorExt;
 
 public class VomsesMain {
 
 	static enum OptionArgs{
 		
 		HOST("host","The host where the service is running", true),
 		PORT("port", "The service will listen on this port", true),
 		SHUTDOWN_PORT("shutdown_port", "The service can be shut down contacting this port.", false),
 		CERT("cert", "The certificate used to setup the SSL context."),
 		KEY("key", "The key used to setup the SSL context."),
 		TRUSTDIR("trustdir", "The directory where IGTF trust anchors are found."),
 		CONFDIR("confdir", "The directory where VOMS Admin configuration lives.");
 		
 		
 		private OptionArgs(String optionName, String desc) {
 			this(optionName, desc, false);
 		}
 		
 		private OptionArgs(String optionName, String desc, boolean required) {
 			this.optionName = optionName;
 			this.description = desc;
 			this.required = required;
 		}
 		
 		private final String optionName;
 		private final String description;
 		private final boolean required;
 
 		/**
 		 * @return the optionName
 		 */
 		public synchronized String getOptionName() {
 			return optionName;
 		}
 		
 		/**
 		 * @return the description
 		 */
 		public synchronized String getDescription() {
 			return description;
 		}
 
 		/**
 		 * @return the required
 		 */
 		public synchronized boolean isRequired() {
 			return required;
 		}
 
 		@Override
 		public String toString() {
 			return optionName;
 		}
 	}
 	
 	private Options cliOptions;
 	private CommandLineParser parser = new GnuParser();
 	
 	private String confDir;
 	private String host;
 	private String port;
 	
 	private String shutdownPort;
 	private String shutdownPassword;
 	
 	private String certFile;
 	private String keyFile;
 	private String trustDir;
 	
 	private Server server;
 	private WebAppContext context;
 		
 	private int trustDirRefreshIntervalInMsec = 10 * 60 * 1000;
 	
 	private void configureLogging() {
 		
 		URL loggingConf = this.getClass().getClassLoader().getResource("logback.vomses.xml");
 		
 		LoggerContext lc = (LoggerContext) LoggerFactory
 				.getILoggerFactory();
 		
 		JoranConfigurator configurator = new JoranConfigurator();
 		
 		configurator.setContext(lc);
 		lc.reset();
 		
 		try {
 			configurator.doConfigure(loggingConf);
 		
 		} catch (JoranException e) {
 			
 			failAndExit("Error setting up the logging system",e);
 		
 		}
 		
 	}
 	
 	
 	private void failAndExit(String errorMessage, Throwable t){
 		
 		if (t != null) {
 
 			System.err.format("%s: %s\n", errorMessage, t.getMessage());
 			t.printStackTrace(System.err);
 		} else {
 			
 			System.err.println(errorMessage);
 			
 		}
 		
 		System.exit(1);
 		
 	}
 	
 	
 	private void initOptions(){
 		
 		cliOptions = new Options();
 
		for (OptionArgs o: OptionArgs.values())
			cliOptions.addOption(o.getOptionName(), o.isRequired(), o.getDescription());
 		
 	}
 	
 	public VomsesMain(String args[]) {
 		try{
 			initOptions();
 			parseCommandLineOptions(args);
 			configureLogging();
 			configureJettyServer();
 			start();
 			
 		}catch (Throwable t){
 			failAndExit("Error starting up VOMSES application",t);
 		}
 	}
 	
 	
 	private void configureJettyServer(){
 		
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
 				50,
 				10);
 		
 		
 		configureWebapp();
 		
 		HandlerCollection handlers = new HandlerCollection();
 		
 		handlers.setHandlers(new Handler[]{ 
 				context});
 		
 		server.setHandler(handlers);
 		
 	}
 	
 	private void checkStatus() throws Throwable{
 		
 		if (context.getUnavailableException() != null)
 			throw context.getUnavailableException();
 	}
 	
 	private void configureWebapp(){
 		
 		String webappResourceDir = this.getClass().getClassLoader().getResource("vomses-webapp").toExternalForm();
 		
 		context = new WebAppContext();
 		context.setContextPath("/");
 		context.setResourceBase(webappResourceDir);
 		context.setParentLoaderPriority(true);
 		context.setInitParameter("confdir", confDir);
 		context.setInitParameter("host", host);
 		context.setThrowUnavailableOnStartupException(true);
 		
 	}
 	protected SSLOptions getSSLOptions(){
 		SSLOptions options = new SSLOptions();
 
 		options.setCertificateFile(certFile);
 		options.setKeyFile(keyFile);
 		options.setTrustStoreDirectory(trustDir);
 		options.setTrustStoreRefreshIntervalInMsec(trustDirRefreshIntervalInMsec);
 		options.setNeedClientAuth(false);
 		options.setWantClientAuth(false);
 		return options;
 		
 	}
 	
 	
 	private void parseCommandLineOptions(String[] args){
 		
 		try {
 			
 			CommandLine cmdLine = parser.parse(cliOptions, args);
 			
 			Properties sysconfigProperties = SysconfigUtil.loadSysconfig();
 								
 			confDir = cmdLine.getOptionValue(OptionArgs.CONFDIR.getOptionName());
 			
 			if (confDir == null)
 				confDir = sysconfigProperties.getProperty(SysconfigUtil.SYSCONFIG_CONF_DIR);
 			
 			host = cmdLine.getOptionValue(OptionArgs.HOST.getOptionName(),"localhost");
 			port = cmdLine.getOptionValue(OptionArgs.PORT.getOptionName(), "8443");
 			shutdownPort = cmdLine.getOptionValue(OptionArgs.SHUTDOWN_PORT.getOptionName(),"9000");
 			
 			certFile = cmdLine.getOptionValue(OptionArgs.CERT.getOptionName(), "/etc/grid-security/hostcert.pem");
 			keyFile = cmdLine.getOptionValue(OptionArgs.KEY.getOptionName(), "/etc/grid-security/hostkey.pem");
 			trustDir = cmdLine.getOptionValue(OptionArgs.TRUSTDIR.getOptionName(), "/etc/grid-security/certificates");
 			
 			
 		} catch (ParseException e) {
 			
 			failAndExit("Error parsing command line arguments", e);
 
 		}
 	}
 	
	
 	private void start(){
 		
 		
 		JettyRunThread service =  new JettyRunThread(server);
 		try{
 			
 			service.start();
 			checkStatus();
 			service.join();
 			
 		}catch (Throwable t){
 			failAndExit("Error starting VOMSES service", t);
 		}
 		
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new VomsesMain(args);
 
 	}
 
 }
