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
 package org.jabox.cis.hudson;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.wicket.Component;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.io.IOUtils;
 import org.jabox.apis.cis.CISConnector;
 import org.jabox.apis.cis.CISConnectorConfig;
 import org.jabox.environment.Environment;
 import org.jabox.model.DeployerConfig;
 import org.jabox.model.Project;
 import org.jabox.model.Server;
 import org.springframework.stereotype.Service;
 import org.xml.sax.SAXException;
 
 /**
  * TODO: Find a way to pass credentials to Hudson: by doing a post here:
  * http://localhost
  * :9090/hudson/scm/SubversionSCM/enterCredential?_httpUrlOfSubversion_
  * 
  * @author Administrator
  * 
  */
 @Service
 public class HudsonConnector implements CISConnector {
 	public static final String ID = "plugin.cis.hudson";
 
 	public String getName() {
 		return "Hudson";
 	}
 
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	/**
 	 * Implementation inspired by groovy code here:
 	 * http://wiki.hudson-ci.org/display/HUDSON/Authenticating+scripted+clients
 	 * 
 	 */
 	public boolean addProject(final Project project, final CISConnectorConfig dc)
 			throws IOException, SAXException {
 		HudsonConnectorConfig hcc = (HudsonConnectorConfig) dc;
 		String url = dc.getServer().getUrl();
 
 		HttpClient client = new HttpClient();
 		client.getState().setCredentials(
 				null,
 				null,
 				new UsernamePasswordCredentials(hcc.getUsername(), hcc
 						.getPassword()));
 
 		// Hudson does not do any authentication negotiation,
 		// ie. it does not return a 401 (Unauthorized)
 		// but immediately a 403 (Forbidden)
 		client.getState().setAuthenticationPreemptive(true);
 
 		String uri = url + "createItem?name=" + project.getName();
 		PostMethod post = new PostMethod(uri);
 		post.setDoAuthentication(true);
 
 		post.setRequestHeader("Content-type", "text/xml; charset=UTF-8");
 
 		InputStream is = getConfigXMLStream(project);
 
 		String body = parseInputStream(is, project);
 		post.setRequestBody(body);
 		try {
 			int result = client.executeMethod(post);
 			System.out.println("Return code: " + result);
 			for (Header header : post.getResponseHeaders()) {
 				System.out.println(header.toString().trim());
 			}
 			System.out.println(post.getResponseBodyAsString());
 		} finally {
 			post.releaseConnection();
 		}
 
 		// Trigger the Hudson build
 		PostMethod triggerBuild = new PostMethod(url + "/job/"
 				+ project.getName() + "/build");
 		client.executeMethod(triggerBuild);
 		return true;
 	}
 
 	/**
 	 * Returns the config.xml that should be used as template for the specific
 	 * project. This depends on the SCM implementation used.
 	 * 
 	 * @param project
 	 * @return
 	 */
 	private InputStream getConfigXMLStream(Project project) {
 		String configXML = "config.xml";
 		if (project.getScmUrl().startsWith("git://")) {
 			configXML = "config-git.xml";
 		}
 		return HudsonConnector.class.getResourceAsStream(configXML);
 	}
 
 	private String parseInputStream(final InputStream is, final Project project)
 			throws IOException {
 		StringWriter writer = new StringWriter();
 		IOUtils.copy(is, writer);
 		String theString = writer.toString();
 
 		String replace = theString.replace("${project.scmURL}", project
 				.getScmUrl());
 		replace = replace.replace("${project.issueURL}",
 				"http://localhost/redmine/");
 		replace = replace.replace("${goals}",
 				"clean findbugs:findbugs checkstyle:checkstyle pmd:pmd pmd:cpd deploy -B"
 						+ passCustomSettingsXml());
 		replace = replace.replace("${project.name}", project.getName());
 		return replace;
 	}
 
 	private String passCustomSettingsXml() {
		return " -s \"" + Environment.getCustomMavenHomeDir()
				+ "/settings.xml\"";
 	}
 
 	public DeployerConfig newConfig() {
 		return new HudsonConnectorConfig();
 	}
 
 	public Component newEditor(final String id, final IModel<Server> model) {
 		return new HudsonConnectorEditor(id, model);
 	}
 }
