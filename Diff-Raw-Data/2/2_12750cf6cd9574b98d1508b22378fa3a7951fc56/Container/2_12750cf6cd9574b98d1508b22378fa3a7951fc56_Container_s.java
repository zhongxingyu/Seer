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
 package org.jabox.model;
 
 import java.io.File;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.persistence.domain.BaseEntity;
 import org.codehaus.cargo.container.ContainerType;
 import org.codehaus.cargo.container.InstalledLocalContainer;
 import org.codehaus.cargo.container.configuration.ConfigurationType;
 import org.codehaus.cargo.container.configuration.LocalConfiguration;
 import org.codehaus.cargo.container.deployable.WAR;
 import org.codehaus.cargo.container.installer.Installer;
 import org.codehaus.cargo.container.installer.ZipURLInstaller;
 import org.codehaus.cargo.container.property.GeneralPropertySet;
 import org.codehaus.cargo.container.property.ServletPropertySet;
 import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
 import org.codehaus.cargo.generic.DefaultContainerFactory;
 import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
 import org.jabox.apis.embedded.EmbeddedServer;
 import org.jabox.environment.Environment;
 import org.jabox.utils.MavenSettingsManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A Project.
  * 
  * @author dimitris
  */
 public class Container extends BaseEntity implements Serializable {
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(Container.class);
 
 	private static final long serialVersionUID = 1L;
 	private String name;
 	private String port;
 	private String rmiPort;
 	private String ajpPort;
 	private String jvmArgs = "-Xms128m -Xmx512m -XX:PermSize=128m";
 	private static String[] DEFAULT_WEBAPPS = {
 			"org.jabox.cis.hudson.HudsonServer",
			"org.jabox.mrm.artifactory.ArtifactoryServer",
 			"org.jabox.sas.sonar.SonarServer" };
 
 	private List<String> webapps = Arrays.asList(DEFAULT_WEBAPPS);
 
 	@Override
 	public String toString() {
 		return "Container: " + name;
 	}
 
 	public void setName(final String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setPort(String port) {
 		this.port = port;
 	}
 
 	public String getPort() {
 		return port;
 	}
 
 	public void setJvmargs(String jvmargs) {
 		this.jvmArgs = jvmargs;
 	}
 
 	public String getJvmargs() {
 		return jvmArgs;
 	}
 
 	public void start() {
 		LOGGER.info("Starting Servlet Container");
 		Environment.configureEnvironmentVariables();
 
 		// (1) Optional step to install the container from a URL pointing to its
 		// distribution
 		Installer installer;
 		try {
 			installer = new ZipURLInstaller(new URL(
 					"http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.32/bin/"
 							+ getTomcatFilename()), Environment
 					.getDownloadsDir().getAbsolutePath());
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return;
 		}
 		installer.install();
 
 		// (2) Create the Cargo Container instance wrapping our physical
 		// container
 		LocalConfiguration configuration = (LocalConfiguration) new DefaultConfigurationFactory()
 				.createConfiguration("tomcat6x", ContainerType.INSTALLED,
 						ConfigurationType.STANDALONE, new File(Environment
 								.getBaseDir(), "cargo/" + getName())
 								.getAbsolutePath());
 		InstalledLocalContainer container = (InstalledLocalContainer) new DefaultContainerFactory()
 				.createContainer("tomcat6x", ContainerType.INSTALLED,
 						configuration);
 		container.setHome(installer.getHome());
 		container.setOutput(new File(Environment.getBaseDir(), "cargo/"
 				+ getName() + ".log").getAbsolutePath());
 
 		configuration.setProperty(ServletPropertySet.PORT, port);
 		configuration.setProperty(GeneralPropertySet.JVMARGS, jvmArgs);
 		configuration.setProperty(TomcatPropertySet.AJP_PORT, ajpPort);
 		configuration.setProperty(GeneralPropertySet.RMI_PORT, rmiPort);
 
 		passSystemProperties(container);
 		// Pass the system properties to the container
 		// Map<String, String> props = new HashMap<String, String>();
 		// Properties properties = System.getProperties();
 		// properties.entrySet();
 		// for (Entry<Object, Object> entry : properties.entrySet()) {
 		// entry.getKey();
 		// LOGGER.debug("Adding key: " + entry.getKey() + ":"
 		// + entry.getValue());
 		// props.put((String) entry.getKey(), (String) entry.getValue());
 		// }
 		// container.setSystemProperties(props);
 
 		MavenSettingsManager.writeCustomSettings();
 		try {
 			List<String> webapps = getWebapps();
 			for (String webapp : webapps) {
 				addEmbeddedServer(configuration, webapp);
 			}
 
 			// (4) Start the container
 			container.setTimeout(1200000);
 			container.start();
 			LOGGER.info("Servlet Container Started");
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.exit(100);
 		}
 	}
 
 	private void passSystemProperties(InstalledLocalContainer container) {
 		Map<String, String> props = new HashMap<String, String>();
 
 		// Hudson
 		props.put(Environment.HUDSON_PROPERTY, System
 				.getProperty(Environment.HUDSON_PROPERTY));
 
 		// Artifactory
 		props.put(Environment.ARTIFACTORY_PROPERTY, System
 				.getProperty(Environment.ARTIFACTORY_PROPERTY));
 
 		// Nexus
 		props.put(Environment.NEXUS_PROPERTY, System
 				.getProperty(Environment.NEXUS_PROPERTY));
 
 		container.setSystemProperties(props);
 	}
 
 	/**
 	 * @return the filename of apache tomcat. Depends on the OS.
 	 */
 	private static String getTomcatFilename() {
 		if (Environment.isWindowsPlatform()) {
 			return "apache-tomcat-6.0.32.zip";
 		}
 		return "apache-tomcat-6.0.32.tar.gz";
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
 		WAR war = new WAR(es.getWarPath());
 		war.setContext(es.getServerName());
 		configuration.addDeployable(war);
 	}
 
 	public void setAjpPort(String ajpPort) {
 		this.ajpPort = ajpPort;
 	}
 
 	public String getAjpPort() {
 		return ajpPort;
 	}
 
 	public void setWebapps(List<String> webapps) {
 		this.webapps = webapps;
 	}
 
 	public List<String> getWebapps() {
 		return webapps;
 	}
 
 }
