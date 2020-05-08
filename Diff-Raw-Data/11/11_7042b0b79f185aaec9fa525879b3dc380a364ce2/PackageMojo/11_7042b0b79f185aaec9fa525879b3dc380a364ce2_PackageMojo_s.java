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
 package com.sixdimensions.wcm.cq.pack;
 
 import java.io.File;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerConfig;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerService;
 
 /**
  * Goal which uploads and installs a package to Adobe CQ.
  * 
  * @goal install-package
  * 
  * @phase install
  */
 public class PackageMojo extends AbstractMojo {
 
 	/**
 	 * Flag to determine whether or not to first delete the package before
 	 * uploading. Default is false.
 	 * 
 	 * @parameter default-value=false
 	 */
 	private boolean deleteFirst = true;
 
 	/**
 	 * Flag to determine whether or not to quit and throw an error when an API
 	 * call fails. Default is true.
 	 * 
 	 * @parameter default-value=true
 	 */
 	private boolean errorOnFailure = true;
 
 	/**
 	 * The host of the server to connect to, including protocol. Default is
 	 * 'http://localhost'.
 	 * 
 	 * @parameter default-value="http://localhost"
 	 */
 	private String host;
 
 	/**
 	 * Location of the file. Default is
 	 * "${project.artifactId}-${project.version}.${project.packaging}"
 	 * 
 	 * @parameter expression=
	 *            "${project.artifactId}-${project.version}.${project.packaging}"
 	 * @required
 	 */
 	private File packageFile;
 
 	/**
 	 * The password to use when connecting. Default is 'admin'.
 	 * 
 	 * @parameter default-value="admin"
 	 */
 	private String password;
 
 	/**
 	 * The path to upload the package to. Default is
 	 * "${project.artifactId}-${project.version}.${project.packaging}"
 	 * 
 	 * @parameter
 	 * @required expression=
	 *           "${project.artifactId}-${project.version}.${project.packaging}"
 	 */
 	private String path;
 
 	/**
 	 * The port of the server to connect to. Default is 'admin'.
 	 * 
 	 * @parameter default-value="4502"
 	 */
 	private String port;
 
 	/**
 	 * Flag to determine whether or not to only upload and not install the
 	 * package. False by default.
 	 * 
 	 * @parameter default-value=false
 	 */
 	private boolean uploadOnly;
 
 	/**
 	 * Flag to determine whether or not to use the Legacy API. False by default.
 	 * 
 	 * @parameter default-value=false
 	 */
 	private boolean useLegacy;
 
 	/**
 	 * The username to use when connecting. Default is 'admin'.
 	 * 
	 * @parameter
 	 */
 	private String user;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.maven.plugin.AbstractMojo#execute()
 	 */
 	public void execute() throws MojoExecutionException {
 		getLog().info("execute");
 
 		getLog().debug("Instantiating configuration object.");
 		PackageManagerConfig config = new PackageManagerConfig();
 		config.setErrorOnFailure(errorOnFailure);
 		config.setHost(host);
 		config.setLog(getLog());
 		config.setPassword(password);
 		config.setPort(port);
 		config.setUseLegacy(useLegacy);
 		config.setUser(user);
 
 		getLog().debug("Retrieving service");
 		PackageManagerService packageMgrSvc = PackageManagerService.Factory
 				.getPackageMgr(config);
 		try {
 			if(deleteFirst){
 				packageMgrSvc.delete(path);
 			}
 			packageMgrSvc.upload(path, packageFile);
 			getLog().info("Package upload successful");
 			if (!this.uploadOnly) {
 				packageMgrSvc.install(path);
 				getLog().info("Package installation successful");
 			}
 		} catch (Exception e) {
 			getLog().error("Exception uploading/installing package.", e);
 			throw new MojoExecutionException(
 					"Exception uploading/installing package.", e);
 		}
		getLog().info("PackageMojo Completed Successfully");
 	}
 }
