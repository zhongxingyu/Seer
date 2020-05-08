 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.manager;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.zend.sdklib.internal.library.AbstractLibrary;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.internal.target.ZendTargetAutoDetect;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.sdklib.target.IZendTarget;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.response.ResponseCode;
 
 /**
  * Target environments manager for the This is a thread-safe class that can be
  * used across threads
  * 
  * @author Roy, 2011
  */
 /**
  * @author roy
  * 
  */
 public class TargetsManager extends AbstractLibrary {
 
 	private static final String DEFAULT_KEY = "sdk";
 
 	/**
 	 * All targets loaded in the manager
 	 */
 	private List<IZendTarget> all = new ArrayList<IZendTarget>(1);
 
 	/**
 	 * The mechanism that is responsible to load the targets
 	 */
 	private final ITargetLoader loader;
 
 	public TargetsManager(ITargetLoader loader) {
 		this.loader = loader;
 		final IZendTarget[] loadAll = loader.loadAll();
 		for (IZendTarget zTarget : loadAll) {
 			if (!validTarget(zTarget)) {
 				log.error(new IllegalArgumentException(
 						"Conflict found when adding " + zTarget.getId()));
 			} else {
 				this.all.add(zTarget);
 			}
 		}
 	}
 
 	public synchronized IZendTarget add(IZendTarget target)
 			throws WebApiException {
 		if (!validTarget(target)) {
 			log.error(new IllegalArgumentException(
 					"Conflict found when adding " + target.getId()));
 			return null;
 		}
 
 		// try to connect to server
 		if (!target.connect()) {
 			return null;
 		}
 
 		// notify loader on addition
 		this.loader.add(target);
 
 		// adds the target to the list
 		final boolean added = this.all.add(target);
 		return added ? target : null;
 	}
 
 	public synchronized IZendTarget remove(IZendTarget target) {
 		if (target == null) {
 			throw new IllegalArgumentException("target cannot be null");
 		}
 		if (!this.all.contains(target)) {
 			throw new IllegalArgumentException("provided target not found");
 		}
 
 		this.loader.remove(target);
 
 		// remove the specified target
 		final boolean removed = this.all.remove(target);
 		return removed ? target : null;
 	}
 
 	/**
 	 * Finds a target given target id
 	 * 
 	 * @param i
 	 * @return the specified target
 	 */
 	public synchronized IZendTarget getTargetById(String id) {
 		if (id == null) {
 			return null;
 		}
 
 		for (IZendTarget target : getTargets()) {
 			if (id.equals(target.getId())) {
 				return target;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns a target that represents the localhost zend server
 	 * 
 	 * @return zend target for localhost
 	 * @throws IOException
 	 * @throws WebApiException
 	 */
 	public synchronized IZendTarget detectLocalhostTarget() throws IOException {
 		String targetId = Integer.toString(getTargets().length);
 		return detectLocalhostTarget(targetId, DEFAULT_KEY);
 	}
 
 	/**
 	 * Returns a target that represents the localhost zend server
 	 * 
 	 * @param key
 	 * @return zend target for localhost
 	 * @throws IOException
 	 * @throws WebApiException
 	 */
 	public synchronized IZendTarget detectLocalhostTarget(String key)
 			throws IOException {
 		final String targetId = Integer.toString(getTargets().length);
 		return detectLocalhostTarget(targetId, key);
 	}
 
 	/**
 	 * Returns a target that represents the localhost zend server
 	 * 
 	 * @param targetId
 	 * @param key
 	 * @return zend target for localhost
 	 * @throws IOException
 	 * @throws WebApiException
 	 */
 	public synchronized IZendTarget detectLocalhostTarget(String targetId,
 			String key) throws IOException {
 		final IZendTarget[] list = getTargets();
 		targetId = targetId != null ? targetId : Integer
 				.toString(getTargets().length);
 		key = key != null ? key : DEFAULT_KEY;
 		for (IZendTarget t : list) {
 			if (ZendTargetAutoDetect.localhost.equals(t.getHost())) {
 				return t;
 			}
 		}
 		try {
 			// localhost not found - create one
 			final IZendTarget local = new ZendTargetAutoDetect()
 					.createLocalhostTarget(targetId, key);
 			return add(local);
 		} catch (IOException e) {
 			log.error(e.getMessage());
 			throw e;
 		} catch (WebApiException e) {
 			log.error("Coudn't connect to localhost server, please make "
					+ "sure your server is up and running. This tool works with "
					+ "version 5.5 and up.");
 			log.error("More information provided by localhost server:");
 			final ResponseCode responseCode = e.getResponseCode();
 			if (responseCode != null) {
 				log.error("\tError code: " + responseCode);
 			}
 			final String message = e.getMessage();
 			if (message != null) {
 				log.error("\tError message: " + message);
 			}
 		}
 		return null;
 	}
 
 	public synchronized IZendTarget[] getTargets() {
 		return (IZendTarget[]) this.all
 				.toArray(new ZendTarget[this.all.size()]);
 	}
 
 	/**
 	 * Creates and adds new target based on provided parameters.
 	 * 
 	 * @param host
 	 * @param key
 	 * @param secretKey
 	 * @return
 	 */
 	public IZendTarget createTarget(String host, String key, String secretKey) {
 		final String targetId = Integer.toString(getTargets().length);
 		return createTarget(targetId, host, key, secretKey);
 	}
 
 	/**
 	 * Creates and adds new target based on provided parameters.
 	 * 
 	 * @param targetId
 	 * @param host
 	 * @param key
 	 * @param secretKey
 	 * @return
 	 */
 	public IZendTarget createTarget(String targetId, String host, String key,
 			String secretKey) {
 		try {
 			IZendTarget target = add(new ZendTarget(targetId, new URL(host),
 					key, secretKey));
 			if (target == null) {
 				log.error("Error adding Zend Target " + targetId);
 				return null;
 			}
 			return target;
 		} catch (MalformedURLException e) {
 			log.error("Error adding Zend Target " + targetId);
 			log.error("\tpossible error " + e.getMessage());
 		} catch (WebApiException e) {
 			log.error("Error adding Zend Target " + targetId);
 			log.error("\tpossible error " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Check for conflicts and errors in new target
 	 * 
 	 * @param target
 	 * @return
 	 */
 	private boolean validTarget(IZendTarget target) {
 		if (target == null) {
 			throw new IllegalArgumentException("target cannot be null");
 		}
 
 		if (target.getId() == null) {
 			return false;
 		}
 
 		return null == getTargetById(target.getId());
 	}
 
 }
