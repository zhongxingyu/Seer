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
 package com.sixdimensions.wcm.cq.pack.service.impl;
 
 import java.io.File;
 
 import org.apache.maven.plugin.logging.Log;
 import org.json.JSONObject;
 
 import com.sixdimensions.wcm.cq.dao.HTTPServiceDAO;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerConfig;
 import com.sixdimensions.wcm.cq.pack.service.PackageManagerService;
 
 /**
  * Implementation of the Package Manager Service based on the new CQ Package
  * Manager API.
  * 
  * @author klcodanr
  */
 public class PackageManagerServiceImpl implements PackageManagerService {
 	private static enum COMMAND {
 		DELETE("?cmd=delete"), DRY_RUN("?cmd=dryrun"), INSTALL("?cmd=install"), UPLOAD(
 				"?cmd=upload");
 		private final String cmd;
 
 		COMMAND(String cmd) {
 			this.cmd = cmd;
 		}
 
 		public String getCmd() {
 			return cmd;
 		}
 	}
 
 	private static final String FILE_KEY = "package";
 	private static final String MESSAGE_KEY = "msg";
 	private static final String PACK_MGR_PATH = "/crx/packmgr/service/.json";
 	private static final String PACKAGE_BASE_PATH = "/etc/packages/";
 	private static final String SUCCESS_KEY = "success";
 	private PackageManagerConfig config;
 	private Log log;
 	private HTTPServiceDAO pmAPI;
 
 	/**
 	 * Create a new Package Manager Service instance.
 	 * 
 	 * @param config
 	 *            the configuration with which to instantiate the package
 	 *            manager service
 	 */
 	public PackageManagerServiceImpl(PackageManagerConfig config) {
 		log = config.getLog();
 		this.config = config;
 		pmAPI = new HTTPServiceDAO(config);
 	}
 
 	/**
 	 * Generates a url from the specified path and configuration.
 	 * 
 	 * @param path
 	 *            the path of the package to be updated
 	 * @return the url
 	 */
 	protected String assembleUrl(String path) {
 		log.debug("assembleUrl");
 		if (path.startsWith("/")) {
 			return config.getHost() + ":" + config.getPort() + PACK_MGR_PATH
 					+ path;
 		} else {
 			return config.getHost() + ":" + config.getPort() + PACK_MGR_PATH
 					+ PACKAGE_BASE_PATH + path;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#delete(java
 	 * .lang.String)
 	 */
 	public void delete(String path) throws Exception {
 		log.debug("delete");
 
 		log.info("Deleting package at path: " + path);
 		String responseStr = new String(pmAPI.doPost(assembleUrl(path)
 				+ COMMAND.DELETE.getCmd()), "UTF-8");
 		log.debug("Response: " + responseStr);
 
 		JSONObject result = new JSONObject(responseStr);
 		log.debug("Succeeded: " + result.getBoolean(SUCCESS_KEY));
 		log.debug("Message: " + result.getString(MESSAGE_KEY));
 
 		if (result.getBoolean(SUCCESS_KEY)) {
 			log.info("Delete succeeded");
 		} else {
 			log.warn("Delete failed: " + result.getString(MESSAGE_KEY));
 		}
 
 		if (!result.getBoolean(SUCCESS_KEY) && config.isErrorOnFailure()) {
 			throw new Exception("Failed to delete package: "
 					+ result.getString(MESSAGE_KEY));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#dryRun(java
 	 * .lang.String)
 	 */
 	public void dryRun(String path) throws Exception {
 		log.debug("dryRun");
 
 		log.info("Performing Dry Run on package at path: " + path);
 		String responseStr = new String(pmAPI.doPost(assembleUrl(path)
 				+ COMMAND.DRY_RUN.getCmd()), "UTF-8");
 		log.debug("Response: " + responseStr);
 
 		JSONObject result = new JSONObject(responseStr);
 		log.debug("Succeeded: " + result.getBoolean(SUCCESS_KEY));
 		log.debug("Message: " + result.getString(MESSAGE_KEY));
 
 		if (result.getBoolean(SUCCESS_KEY)) {
 			log.info("Dry Run succeeded");
 		} else {
 			log.warn("Dry Run failed: " + result.getString(MESSAGE_KEY));
 		}
 
 		if (!result.getBoolean(SUCCESS_KEY) && config.isErrorOnFailure()) {
 			throw new Exception("Failed to complete installation dry run: "
 					+ result.getString(MESSAGE_KEY));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#install(java
 	 * .lang.String)
 	 */
 	public void install(String path) throws Exception {
 		log.debug("install");
 
 		log.info("Installing package at path: " + path);
 		String responseStr = new String(pmAPI.doPost(assembleUrl(path)
 				+ COMMAND.INSTALL.getCmd()), "UTF-8");
 		log.debug("Response: " + responseStr);
 
 		JSONObject result = new JSONObject(responseStr);
 		log.debug("Succeeded: " + result.getBoolean(SUCCESS_KEY));
 		log.debug("Message: " + result.getString(MESSAGE_KEY));
 
 		if (result.getBoolean(SUCCESS_KEY)) {
 			log.info("Installation succeeded");
 		} else {
 			log.warn("Installation failed: " + result.getString(MESSAGE_KEY));
 		}
 
 		if (!result.getBoolean(SUCCESS_KEY) && config.isErrorOnFailure()) {
 			throw new Exception("Failed to complete installation dry run: "
 					+ result.getString(MESSAGE_KEY));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.sixdimensions.wcm.cq.pack.service.PackageManagerService#upload(java
 	 * .lang.String, java.io.File)
 	 */
 	public void upload(String path, File pkg) throws Exception {
 		log.debug("upload");
 
 		log.info("Uploading package " + pkg.getAbsolutePath() + " to path: "
 				+ path);
 		String responseStr = new String(pmAPI.postFile(assembleUrl(path)
 				+ COMMAND.UPLOAD.getCmd(), FILE_KEY, pkg), "UTF-8");
 		log.debug("Response: " + responseStr);
 		JSONObject result = new JSONObject(responseStr);
 
 		log.debug("Succeeded: " + result.getBoolean(SUCCESS_KEY));
 		log.debug("Message: " + result.getString(MESSAGE_KEY));
 
 		if (result.getBoolean(SUCCESS_KEY)) {
 			log.info("Upload succeeded");
 		} else {
 			log.warn("Upload failed: " + result.getString(MESSAGE_KEY));
 		}
 		if (!result.getBoolean(SUCCESS_KEY) && config.isErrorOnFailure()) {
			throw new Exception("Failed to upload package: "
 					+ result.getString(MESSAGE_KEY));
 		}
 	}
 }
