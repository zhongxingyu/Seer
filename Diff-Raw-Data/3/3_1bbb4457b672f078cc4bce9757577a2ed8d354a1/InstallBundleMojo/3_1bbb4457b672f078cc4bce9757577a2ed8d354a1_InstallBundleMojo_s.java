 /*
  * Copyright 2012 - Six Dimensions
  * 
  * This file is part of the CQ Package Plugin.
  * 
  * The CQ Package Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * The CQ Package Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with the CQ Package Plugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.sixdimensions.wcm.cq;
 
 import java.io.File;
 
 import org.apache.maven.plugin.MojoExecutionException;
 
 import com.sixdimensions.wcm.cq.service.CQService;
 import com.sixdimensions.wcm.cq.service.CQServiceConfig;
 
 /**
  * Installs the specified bundle into Adobe CQ to a configurable path. This path
  * should be something like /apps/{APP_NAME}/install so it will be picked up by
  * the CQ Bundle installer.
  * 
  * @author dklco
  * @phase install
  * @goal install-bundle
  */
 public class InstallBundleMojo extends AbstractCQMojo {
 
 	/**
 	 * Location of the bundle file. Default is
 	 * "${project.artifactId}-${project.version}.jar"
 	 * 
 	 * @parameter expression=
 	 *            "${project.build.directory}/${project.artifactId}-${project.version}.jar"
 	 * @required
 	 */
 	private File bundleFile;
 
 	/**
 	 * The path to upload the package to. Default is "/apps/bundles/install"
 	 * 
 	 * @parameter default-value="/apps/bundles/install"
 	 */
 	private String path;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.maven.plugin.AbstractMojo#execute()
 	 */
 	public void execute() throws MojoExecutionException {
 		getLog().info("execute");
 
 		getLog().info("Initializing");
 		CQServiceConfig config = new CQServiceConfig();
 		initConfig(config);
 
 		getLog().info(
 				"Connecting to server: " + config.getHost() + ":"
 						+ config.getPort());
 		getLog().info("Connecting with user: " + config.getUser());
 		CQService cqSvc = CQService.Factory.getService(config);
 
 		try {
 			getLog().info("Creating folders: " + path);
 			cqSvc.createFolder(path);
 
 			getLog().info(
 					"Uploading bundle " + bundleFile.getAbsolutePath()
 							+ " to path " + path);
 			cqSvc.uploadFile(bundleFile, path);
 
 			getLog().info("Bundle installation complete");
 		} catch (Exception e) {
			throw new MojoExecutionException("Exception installing bundle");
 		}
 	}
 
 }
