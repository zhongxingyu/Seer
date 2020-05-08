 package com.googlecode.mojo.trac;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.model.DistributionManagement;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.settings.Settings;
 
 /**
  * Common super class.
  * 
  */
 public abstract class AbstractTracMojo extends AbstractMojo {
 
 	/**
 	 * @parameter expression="${project}"
 	 * 
 	 * @required
 	 */
 	protected MavenProject project;
 
 	/**
 	 * @parameter expression="${settings}"
 	 * 
 	 * @required
 	 */
 	protected Settings settings;
 
 	/**
 	 * Enable the interactive mode to set milestone.
 	 * 
 	 * @parameter expression="${interactive}" default-value="true"
 	 */
 	protected boolean interactive;
 
 	/**
 	 * URL of the trac.
 	 * 
 	 * @parameter
 	 * @required
 	 */
 	protected String url;
 
 	/**
 	 * Basic Auth.
 	 * 
 	 * @parameter
 	 */
 	protected BasicAuth basicAuth;
 
 	/**
 	 * Dry Run.
 	 * 
 	 * @parameter expression="${dryRun}"
 	 */
 	protected boolean dryRun;
 
 	/**
 	 * 
 	 * @parameter expression="${project.distributionManagement.repository.url}"
 	 * @readonly
 	 */
 	protected String distUrl;
 
 	/**
 	 * 
 	 * @parameter expression="${project.distributionManagement.snapshotRepository.url}"
 	 * @readonly
 	 */
 	protected String distSnapshotUrl;
 
 	/**
 	 * 
 	 * @parameter expression="${project.distributionManagement}"
 	 * @readonly
 	 */
 	protected DistributionManagement distributionManagement;
 
 	/**
 	 * @parameter
 	 */
 	protected DownloadUrls downloadUrls;
 
 	public void setDownloadUrls(DownloadUrls downloadUrls) {
 		getLog().debug("setDownloadUrls.");
 		this.downloadUrls = downloadUrls;
 	}
 
 	/**
 	 * set property "trac.distBaseUrl".
 	 * 
 	 * @param project
 	 */
 	public void setProject(MavenProject project) {
 		this.project = project;
 
 		String downloadUrl = getDistBaseUrl();
 
 		getLog().info("Setting \"trac.distBaseUrl\" = " + downloadUrl);
 
 		this.project.getProperties().put("trac.distBaseUrl", getDistBaseUrl());
 
 	}
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		try {
 			setup();
 			run();
 
 		} catch (RuntimeException e) {
 			handleException(e);
 		}
 	}
 
 	protected String getDistBaseUrl() {
 
 		if (downloadUrls == null) {
 			return "undefiened";
 		}
 
 		StringBuilder url = new StringBuilder();
 
 		if (project.getVersion().endsWith("-SNAPSHOT")) {
 			url.append(downloadUrls.getSnapshotUrl());
 		} else {
 			url.append(downloadUrls.getReleaseUrl());
 		}
 
 		if (!url.toString().endsWith("/")) {
 			url.append("/");
 		}
 
 		String groupId = project.getGroupId().replace('.', '/');
 		url.append(groupId);
 		url.append("/");
 		url.append(project.getArtifactId());
 		url.append("/");
 		url.append(project.getVersion());
 
 		return url.toString();
 	}
 
 	protected void setup() throws MojoExecutionException, MojoFailureException {
 	}
 
 	abstract protected void run() throws MojoExecutionException,
 			MojoFailureException;
 
 	protected TracXmlRpcClient getTracClient() {
 		return new TracXmlRpcClient(getLog(), url, basicAuth);
 	}
 
 	protected void handleException(Exception e) throws MojoExecutionException {
 		throw new MojoExecutionException("Error at maven-trac-plugin. "
 				+ e.getMessage(), e);
 	}
 
 }
