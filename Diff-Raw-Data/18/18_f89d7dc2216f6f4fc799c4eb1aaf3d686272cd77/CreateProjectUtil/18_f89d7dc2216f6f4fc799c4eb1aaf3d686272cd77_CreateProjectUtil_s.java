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
 package org.jabox.application;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.maven.MavenExecutionException;
 import org.apache.maven.artifact.InvalidRepositoryException;
 import org.apache.wicket.persistence.provider.ConfigXstreamDao;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.jabox.apis.IManager;
 import org.jabox.apis.cis.CISConnector;
 import org.jabox.apis.its.ITSConnector;
 import org.jabox.apis.its.ITSConnectorConfig;
 import org.jabox.apis.rms.RMSConnector;
 import org.jabox.apis.scm.SCMConnector;
 import org.jabox.apis.scm.SCMConnectorConfig;
 import org.jabox.apis.scm.SCMException;
 import org.jabox.model.DefaultConfiguration;
 import org.jabox.model.Project;
 import org.tmatesoft.svn.core.SVNException;
 import org.xml.sax.SAXException;
 
 import com.google.inject.Inject;
 
 public class CreateProjectUtil implements ICreateProjectUtil {
 
 	@Inject
 	protected IManager _manager;
 
 	public CreateProjectUtil() {
		// ((GuiceInjectorHolder) ((WicketApplication) WicketApplication.get())
		// .getMetaData(GuiceInjectorHolder.INJECTOR_KEY)).getInjector()
		// .injectMembers(this);
 		// // InjectorHolder.getInjector().inject(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jabox.application.ICreateProjectUtil#createProject(org.jabox.model.Project)
 	 */
 	public void createProject(final Project project) {
 		try {
 			createProjectMethod(project);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidRepositoryException e) {
 			e.printStackTrace();
 		} catch (MavenExecutionException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (SCMException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param project
 	 * @throws IOException
 	 * @throws InvalidRepositoryException
 	 * @throws MavenExecutionException
 	 * @throws SVNException
 	 * @throws SAXException
 	 * @throws SCMException
 	 * @throws IOException
 	 * @throws MavenExecutionException
 	 */
 	private void createProjectMethod(final Project project)
 			throws InvalidRepositoryException, SAXException, SCMException,
 			IOException, MavenExecutionException {
 		final DefaultConfiguration dc = ConfigXstreamDao.getConfig();
 
 		SCMConnectorConfig scmc = dc.getScm();
 
 		SCMConnector<SCMConnectorConfig> scm = _manager
 				.getScmConnectorInstance(scmc);
 
 		System.out.println("Using SCM: " + scm.toString());
 		File trunkDir = scm.createProjectDirectories(project, scmc);
 
 		// Create Project from template.
 		MavenCreateProject.createProjectWithMavenCore(project, trunkDir
 				.getAbsolutePath());
 
 		RMSConnector rms = _manager.getRmsConnectorInstance(dc.getRms());
 
 		File pomXml = new File(trunkDir, project.getName() + "/pom.xml");
 
 		// Set ScmUrl
 		project.setScmUrl(scmc.getScmUrl() + "/" + project.getName()
 				+ "/trunk/" + project.getName());
 
 		// Inject SCM, CIS, ITS, RMS & UTF8 Encoding configuration
 		try {
 			MavenConfigureSCM.injectScm(pomXml, dc.getScm(), project);
 			MavenConfigureCiManagement.injectCIS(pomXml, dc.getCis(), project);
 			MavenConfigureIssueManagement.injectIssueManagement(pomXml, dc
 					.getIts(), project);
 			MavenConfigureSourceEncoding.injectSourceEncoding(pomXml, project);
 			MavenConfigureSignArtifacts.injectSignArtifact(pomXml, project);
 
 			if (rms != null) {
 				MavenConfigureDistributionManagement.injectDistributionManager(
 						pomXml, dc.getRms());
 			}
 		} catch (XmlPullParserException e) {
 			e.printStackTrace();
 		}
 
 		// Commit Project
 		scm.commitProject(project, scmc);
 
 		// Create a directory structure in subversion for the project
 		// svn.createProject(project);
 
 		// Add files in the trunk.
 
 		// Add Project in Issue Tracking System
 		ITSConnectorConfig config = dc.getIts();
 		ITSConnector<ITSConnectorConfig> its = _manager
 				.getItsConnectorInstance(config);
 		if (its != null) {
 			// its
 			// .setUrl("http://localhost/cgi-bin/bugzilla/index.cgi?GoAheadAndLogIn=1");
 			// its.login("", "");
 
 			// its.setUrl("http://localhost/redmine");
 			its.login(config);
 			its.addProject(project, config);
 			its.addModule(project, config, project.getName(), "initial module",
 					"myemail@gmail.com");
 			its.addVersion(project, config, "0.0.1");
 
 			its.addRepository(project, config, scmc, scmc.getUsername(), scmc
 					.getPassword());
 		}
 
 		CISConnector cis = _manager.getCisConnectorInstance(dc.getCis());
 		if (cis != null) {
 			cis.addProject(project, dc.getCis());
 		}
 	}
 }
