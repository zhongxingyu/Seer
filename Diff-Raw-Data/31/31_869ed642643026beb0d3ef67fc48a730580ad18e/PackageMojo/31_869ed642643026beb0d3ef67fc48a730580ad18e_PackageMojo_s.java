 /*
  * Copyright 2012 - Six Dimensions
  * 
  * This file is part of the CQ Deploy Plugin.
  * 
  * The CQ Deploy Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * The CQ Deploy Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with the CQ Deploy Plugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.sixdimensions.wcm.cq.pack;
 
 import java.io.File;
 
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import com.sixdimensions.wcm.cq.AbstractCQMojo;
 import com.sixdimensions.wcm.cq.pack.service.AC_HANDLING;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerConfig;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerService;
 import com.sixdimensions.wcm.cq.service.CQServiceConfig;
 
 /**
  * Goal which uploads and installs a package to Adobe CQ.
  * 
  * @goal install-package
  * 
  * @phase install
  */
 public class PackageMojo extends AbstractCQMojo implements Mojo {
 
 	/**
 	 * Sets handing of access control information. Valid options are: clear,
 	 * ignore, merge, merge_preserve or overwrite. Not applicable for the legacy
 	 * package manager.
 	 * 
 	 * @parameter default-value=
 	 */
 	private String acHandling = "";
 
 	/**
 	 * Save interval value. When this number of nodes are updated, they will be
 	 * automatically saved. Not applicable for the legacy package manager.
 	 * 
 	 * @parameter default-value=1024
 	 */
 	private int autosave = 1024;
 
 	/**
 	 * Flag to determine whether or not to first delete the package before
 	 * uploading. Default is true.
 	 * 
 	 * @parameter default-value=true
 	 */
 	private boolean deleteFirst = true;
 
 	/**
 	 * Location of the file. Default is
 	 * "${project.artifactId}-${project.version}.${project.packaging}"
 	 * 
 	 * @parameter expression=
 	 *            "${project.build.directory}/${project.artifactId}-${project.version}.${project.packaging}"
 	 */
 	private File packageFile;
 
 	/**
 	 * The path to upload the package to. Default is
 	 * "${project.artifactId}-${project.version}.${project.packaging}"
 	 * 
 	 * @parameter expression=
 	 *            "${project.artifactId}-${project.version}.${project.packaging}"
 	 */
 	private String path;
 
 	/**
 	 * Sets the recursive flag, if true will load and install any packages
 	 * inside this package. Not applicable for the legacy package manager.
 	 * 
 	 * @parameter default-value=true
 	 */
 	private boolean recursive = true;
 
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
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.maven.plugin.AbstractMojo#execute()
 	 */
 	public void execute() throws MojoExecutionException {
 		getLog().info("execute");
 
 		PackageManagerConfig config = new PackageManagerConfig();
 		initConfig(config);
		config.setUseLegacy(useLegacy);
 
 		getLog().info(
 				"Connecting to server: " + config.getHost() + ":"
 						+ config.getPort());
 		getLog().info("Connecting with user: " + config.getUser());
 
 		getLog().debug("Retrieving service");
 		PackageManagerService packageMgrSvc = PackageManagerService.Factory
 				.getPackageMgr(config);
 		try {
 			if (deleteFirst) {
 				try {
					packageMgrSvc.delete(path);
 				} catch (Exception e) {
 					getLog().warn(
 							"Exception deleting existing package, continuing with installation.",
 							e);
 				}
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
 		getLog().info("Package Upload/Installation Completed Successfully");
 	}
 
 	public String getAcHandling() {
 		return acHandling;
 	}
 
 	public int getAutosave() {
 		return autosave;
 	}
 
 	public File getPackageFile() {
 		return packageFile;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.AbstractCQMojo#initConfig(com.sixdimensions.
 	 * wcm.cq.service.CQServiceConfig)
 	 */
 	protected void initConfig(CQServiceConfig cfg) {
 		super.initConfig(cfg);
 		PackageManagerConfig config = (PackageManagerConfig) cfg;
 		try {
 			config.setAcHandling(AC_HANDLING.valueOf(this.getAcHandling()));
 		} catch (IllegalArgumentException iae) {
 			getLog().info(
 					"Unable to parse Access Control Handler from: "
 							+ this.getAcHandling() + " using default");
 			config.setAcHandling(AC_HANDLING.DEFAULT);
 		}
 		config.setRecursive(this.isRecursive());
 		config.setAutosave(this.getAutosave());
 	}
 
 	public boolean isDeleteFirst() {
 		return deleteFirst;
 	}
 
 	public boolean isRecursive() {
 		return recursive;
 	}
 
 	public boolean isUploadOnly() {
 		return uploadOnly;
 	}
 
 	public boolean isUseLegacy() {
 		return useLegacy;
 	}
 
 	public void setAcHandling(String acHandling) {
 		this.acHandling = acHandling;
 	}
 
 	public void setAutosave(int autosave) {
 		this.autosave = autosave;
 	}
 
 	public void setDeleteFirst(boolean deleteFirst) {
 		this.deleteFirst = deleteFirst;
 	}
 
 	public void setPackageFile(File packageFile) {
 		this.packageFile = packageFile;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public void setRecursive(boolean recursive) {
 		this.recursive = recursive;
 	}
 
 	public void setUploadOnly(boolean uploadOnly) {
 		this.uploadOnly = uploadOnly;
 	}
 
 	public void setUseLegacy(boolean useLegacy) {
 		this.useLegacy = useLegacy;
 	}
 }
