 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.standalone;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.codehaus.cargo.container.ContainerType;
 import org.codehaus.cargo.container.InstalledLocalContainer;
 import org.codehaus.cargo.container.configuration.ConfigurationType;
 import org.codehaus.cargo.container.configuration.LocalConfiguration;
 import org.codehaus.cargo.container.deployable.WAR;
 import org.codehaus.cargo.container.installer.Installer;
 import org.codehaus.cargo.container.installer.ZipURLInstaller;
 import org.codehaus.cargo.generic.DefaultContainerFactory;
 import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
 import org.jabox.apis.embedded.EmbeddedServer;
 import org.jabox.environment.Environment;
 import org.jabox.utils.MavenSettingsManager;
 import org.jabox.utils.WebappManager;
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.resource.Resource;
 
 public class Start {
 	private static final String PACKAGE = "/jabox-webapp/";
 
 	public Start() {
 		try {
 			startEmbeddedJetty(false);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(final String[] args) throws Exception {
 		System.out.println(getJaboxWebapp());
 		System.out.println("Jabox home directory: " + Environment.getBaseDir());
 		Environment.configureEnvironmentVariables();
 		startEmbeddedJetty(true);
 	}
 
 	private static String getJaboxWebapp() {
 		Resource res = Resource.newClassPathResource(PACKAGE);
 		if (res == null) {
 			return "D:\\Documents\\My Developments\\Jabox\\workspace-jabox\\jabox\\jabox-webapp\\target\\jabox-webapp-0.0.6-SNAPSHOT\\";
 		}
 		return res.toString();
 	}
 
 	/**
 	 * 
 	 * @param startJabox
 	 *            If set to true the Jetty application is starting Jabox
 	 *            Application together with the embeddedServers.
 	 * @throws MalformedURLException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws ClassNotFoundException
 	 */
 	public static void startEmbeddedJetty(final boolean startJabox)
 			throws MalformedURLException {
 		// (1) Optional step to install the container from a URL pointing to its
 		// distribution
 		Installer installer;
 		installer = new ZipURLInstaller(
 				new URL(
 						"http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.32/bin/apache-tomcat-6.0.32.zip"),
 				new File(Environment.getBaseDir(), "cargo/installs")
 						.getAbsolutePath());
 		installer.install();
 
 		// (2) Create the Cargo Container instance wrapping our physical
 		// container
 		LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory()
 				.createConfiguration("tomcat6x", ContainerType.INSTALLED,
 						ConfigurationType.STANDALONE, new File(Environment
 								.getBaseDir(), "cargo/conf").getAbsolutePath());
 		InstalledLocalContainer container = (InstalledLocalContainer) new DefaultContainerFactory()
 				.createContainer("tomcat6x", ContainerType.INSTALLED,
 						configuration);
 		container.setHome(installer.getHome());
 		container.setOutput(new File(Environment.getBaseDir(),
 				"cargo/cargo.out").getAbsolutePath());
 		container.setSystemProperties(System.getProperties());
 
 		Server server = new Server();
 		MavenSettingsManager.writeCustomSettings();
 		SocketConnector connector = new SocketConnector();
 		// Set some timeout options to make debugging easier.
 		connector.setMaxIdleTime(1000 * 60 * 60);
 		connector.setSoLingerTime(-1);
 		connector.setPort(9090);
 		server.setConnectors(new Connector[] { connector });
 		try {
 			List<String> webapps = WebappManager.getWebapps();
 			for (String webapp : webapps) {
 				addEmbeddedServer(configuration, webapp);
 			}
 
 			if (startJabox) {
 				// Adding ROOT handler.
 				// NOTE: This should be added last on server.
 				WebAppContext bb = new WebAppContext();
 				bb.setServer(server);
 				bb.setContextPath("/");
 				bb.setWar(getJaboxWebapp());
 				File tempDir = new File(Environment.getBaseDirFile(),
 						"server-jabox");
 				tempDir.mkdirs();
 				bb.setTempDirectory(tempDir);
 				server.addHandler(bb);
 			}
 
 			System.out
 					.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
 			server.start();
 			BrowserStarter.openBrowser("http://localhost:9090/");
 
 			// (4) Start the container
			container.setTimeout(1200000);
 			container.start();
 			System.out.println(">>> Container started.");
 
 			if (startJabox) {
 				while (System.in.available() == 0) {
 					Thread.sleep(5000);
 				}
 				server.stop();
 				server.join();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(100);
 		}
 	}
 
 	/**
 	 * Helper function to add an embedded Server using the className to the
 	 * running Jetty Server.
 	 * 
 	 * @param configuration
 	 *            The Jetty server.
 	 * @param className
 	 *            The className of the EmbeddedServer.
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws ClassNotFoundException
 	 */
 	private static void addEmbeddedServer(
 			final LocalConfiguration configuration, final String className)
 			throws InstantiationException, IllegalAccessException,
 			ClassNotFoundException {
 		EmbeddedServer es = (EmbeddedServer) Class.forName(className)
 				.newInstance();
 		configuration.addDeployable(new WAR(es.getWarPath()));
 	}
 }
