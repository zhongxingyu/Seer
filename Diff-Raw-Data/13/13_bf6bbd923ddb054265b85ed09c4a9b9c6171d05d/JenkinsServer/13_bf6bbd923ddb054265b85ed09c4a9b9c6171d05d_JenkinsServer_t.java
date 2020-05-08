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
 package org.jabox.cis.jenkins;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.wicket.persistence.provider.ConfigXstreamDao;
 import org.jabox.apis.embedded.AbstractEmbeddedServer;
 import org.jabox.environment.Environment;
 import org.jabox.model.DefaultConfiguration;
 import org.jabox.utils.DownloadHelper;
 import org.jabox.utils.SettingsModifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JenkinsServer extends AbstractEmbeddedServer {
 	private static final long serialVersionUID = 6774781526963880878L;
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(JenkinsServer.class);
 
 	private final String version = "1.436";
 
 	public static void main(final String[] args) throws Exception {
 		new JenkinsServer().startServerAndWait();
 	}
 
 	public String getServerName() {
 		return "jenkins";
 	}
 
 	@Override
 	public String getWarPath() {
 		injectPlugins();
 		injectConfigurations();
 		File downloadsDir = Environment.getDownloadsDir();
 
 		// Download the jenkins.war
 		File war = new File(downloadsDir, "jenkins.war");
 		if (!war.exists()) {
 			String url = "http://mirrors.jenkins-ci.org/war/" + version
 					+ "/jenkins.war";
 			DownloadHelper.downloadFile(url, war);
 		}
 		return war.getAbsolutePath();
 	}
 
 	private void injectConfigurations() {
 		injectConfiguration("hudson.tasks.Maven.xml");
 		injectConfiguration("hudson.plugins.sonar.SonarPublisher.xml");
 	}
 
 	public List<String> plugins = getDefaultPlugins();
 
 	public void injectPlugins() {
 		for (String plugin : plugins) {
 			injectPlugin(plugin);
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private List<String> getDefaultPlugins() {
 		List<String> defaultPlugins = new ArrayList<String>();
 
 		defaultPlugins.add("analysis-core:1.14");
 		defaultPlugins.add("dry:1.5");
 		defaultPlugins.add("pmd:3.10");
 		defaultPlugins.add("findbugs:4.14");
 		defaultPlugins.add("checkstyle:3.10");
 		defaultPlugins.add("m2release:0.6.1");
 		defaultPlugins.add("redmine:0.9");
 		defaultPlugins.add("git:1.1.3");
 		defaultPlugins.add("claim:1.7");
 		defaultPlugins.add("ci-game:1.17");
 		defaultPlugins.add("sonar:1.6.1");
		defaultPlugins.add("android-emulator:1.18");
 
 		return defaultPlugins;
 	}
 
 	private static void injectConfiguration(final String resource) {
 		File dest = new File(Environment.getHudsonHomeDir(), resource);
 
 		if (!dest.exists()) {
 			DefaultConfiguration dc = ConfigXstreamDao.getConfig();
 			InputStream is = JenkinsServer.class.getResourceAsStream(resource);
 
 			Map<String, String> values = new HashMap<String, String>();
 			values.put("${sonar.host.url}", dc.getCqm().getServer().getUrl());
 
 			try {
 				String data = SettingsModifier.parseInputStream(is, values);
 				FileUtils.writeStringToFile(dest, data);
 			} catch (IOException e) {
 				LOGGER.error("Error writting: " + dest.getAbsolutePath(), e);
 			}
 		}
 	}
 
 	private static void injectPlugin(final String plugin) {
 		String artifactId = plugin.replaceAll(":.*", "");
 		String version = plugin.replaceAll(".*:", "");
 		File dest = new File(getJenkinsPluginDir(), artifactId + ".hpi");
 		if (!dest.exists()) {
 			DownloadHelper.downloadFile(
 					"http://updates.jenkins-ci.org/download/plugins/"
 							+ artifactId + "/" + version + "/" + artifactId
 							+ ".hpi", dest);
 		}
 	}
 
 	private static File getJenkinsPluginDir() {
 		return new File(Environment.getHudsonHomeDir(), "plugins");
 	}
 }
