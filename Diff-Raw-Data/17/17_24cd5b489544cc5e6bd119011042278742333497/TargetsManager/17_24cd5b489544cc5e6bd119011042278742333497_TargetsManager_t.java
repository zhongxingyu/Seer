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
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import org.zend.sdklib.internal.library.AbstractChangeNotifier;
 import org.zend.sdklib.internal.library.BasicStatus;
 import org.zend.sdklib.internal.target.UserBasedTargetLoader;
 import org.zend.sdklib.internal.target.ZendTarget;
 import org.zend.sdklib.internal.target.ZendTargetAutoDetect;
 import org.zend.sdklib.library.StatusCode;
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
 public class TargetsManager extends AbstractChangeNotifier {
 
 	public static final String DEFAULT_KEY = "sdk";
 
 	/**
 	 * All targets loaded in the manager
 	 */
 	private List<IZendTarget> all = new ArrayList<IZendTarget>(1);
 
 	/**
 	 * The mechanism that is responsible to load the targets
 	 */
 	private final ITargetLoader loader;
 
 	/**
 	 * Default target Id (for fast execution)
 	 */
 	private String defaultId = null;
 
 	public TargetsManager() {
 		this(new UserBasedTargetLoader());
 	}
 
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
 		if (this.all.size() > 0) {
 			defaultId = this.all.get(0).getId();
 		}
 	}
 
 	/**
 	 * @param target
 	 * @return
 	 * @throws WebApiException
 	 */
 	public synchronized IZendTarget add(IZendTarget target)
 			throws TargetException {
 		return add(target, false);
 	}
 
 	/**
 	 * @param target
 	 * @param suppressConnect
 	 * @return
 	 * @throws WebApiException
 	 */
 	public synchronized IZendTarget add(IZendTarget target,
 			boolean suppressConnect) throws TargetException {
 		if (!validTarget(target)) {
 			return null;
 		}
 
 		// try to connect to server
 		try {
 			if (!suppressConnect && !target.connect()) {
 				return null;
 			}
 		} catch (WebApiException e) {
 			throw new TargetException(e);
 		}
 
 		// notify loader on addition
 		this.loader.add(target);
 
 		// adds the target to the list
 		final boolean added = this.all.add(target);
 
 		if (this.all.size() == 1) {
 			defaultId = this.all.get(0).getId();
 		}
 		
 		if (added) {
 			statusChanged(new BasicStatus(StatusCode.UNKNOWN, "added target", "added target"));
 		}
 
 		return added ? target : null;
 	}
 
 	public synchronized IZendTarget remove(IZendTarget target) {
 		if (target == null) {
 			throw new IllegalArgumentException("Target cannot be null");
 		}
 		if (!this.all.contains(target)) {
 			throw new IllegalArgumentException("Target with id '"
 					+ target.getId() + "' does not exist.");
 		}
 
 		this.loader.remove(target);
 
 		// remove the specified target
 		final boolean removed = this.all.remove(target);
 
 		if (this.all.size() == 0) {
 			defaultId = null;
 		}
 
 		if (removed) {
 			statusChanged(new BasicStatus(StatusCode.UNKNOWN, "removed target", "removed target"));
 		}
 		
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
 	 * Returns a target that represents the localhost zend server.
 	 * Returned target may be not fully initialized, requiring some extra commands.
 	 * Clients should use {@link IZendTarget#isTemporary()} to test whether they can connect safely.
 	 * 
 	 * @param targetId
 	 *            target id to use, null if not specified
 	 * @param key
 	 *            key to use, null if not specified
 	 * @return the detected localhost target, or null if detection failed
 	 * @throws DetectionException 
 	 */
 	public synchronized IZendTarget detectLocalhostTarget(String targetId,
 			String key) throws DetectionException {
 
		if (targetId == null) {
			targetId = createUniqueId(null);
		}	
 		key = key != null ? key : DEFAULT_KEY + "." + System.getProperty("user.name");
 
 		final IZendTarget existing = getExistingLocalhost();
 		if (existing != null) {
 			return existing;
 		}
 
 		ZendTargetAutoDetect detection = null;
 		try {
 			detection = new ZendTargetAutoDetect();
 		} catch (IOException e) {
 			throw new MissingZendServerException(e);
 		}
 		
 		try {
 
 			// localhost not found - create one
 			final IZendTarget local = detection.createLocalhostTarget(targetId,
 					key);
 
 			return add(local);
 
 		} catch (IOException e) {
 
 			log.warning(e);
 			throw new PrivilegesException(e.getMessage());
 
 		} catch (TargetException e) {
 			Throwable cause = e.getCause();
 			if (cause instanceof WebApiException) {
 				WebApiException webE = (WebApiException) cause;
 				final ResponseCode responseCode = webE.getResponseCode();
 				int code = (responseCode != null) ? responseCode.getCode() : -1;
 				final String message = webE.getMessage();
 				
 				throw new ServerVersionException(code, message);
 			} else {
 				throw new DetectionException(e);
 			}
 		}
 	}
 
 	public synchronized String applyKeyToLocalhost(String key, String secretKey)
 			throws IOException {
 		final ZendTargetAutoDetect detection = new ZendTargetAutoDetect();
 		final String appliedSecretKey = detection
 				.applySecretKey(key, secretKey);
 		return appliedSecretKey;
 	}
 
 	private IZendTarget getExistingLocalhost() {
 		final IZendTarget[] list = getTargets();
 		for (IZendTarget t : list) {
 			if (ZendTargetAutoDetect.localhost.equals(t.getHost())) {
 				log.info(MessageFormat
 						.format("Local server {0} has been already detected with id {1}. ",
 								t.getHost(), t.getId()));
 				return t;
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
 	 * @param targetId
 	 * @param host
 	 * @param key
 	 * @param secretKey
 	 * @return
 	 */
 	public IZendTarget createTarget(String targetId, String host, String key,
 			String secretKey) {
 		return createTarget(targetId, host, key, secretKey, null);
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
 			String secretKey, Properties extraProperties) {
 		try {
 			final ZendTarget t = new ZendTarget(targetId, new URL(host),
 					key, secretKey);
 			
 			if (extraProperties != null && !extraProperties.isEmpty()) {
 				final Set<Entry<Object, Object>> entrySet = extraProperties.entrySet();
 				for (Entry<Object, Object> entry : entrySet) {
 					t.addProperty(entry.getKey().toString(), entry.getValue().toString());
 				}
 			}
 			
 			IZendTarget target = add(t);
 			if (target == null) {
 				return null;
 			}
 			return target;
 		} catch (MalformedURLException e) {
 			log.error("Error adding Zend Target " + targetId);
 			log.error("\tPossible error: " + e.getMessage());
 		} catch (TargetException e) {
 			log.error("Error adding Zend Target " + targetId);
 			log.error("\tPossible error: " + e.getMessage());
 		}
 		return null;
 	}
 
 	public IZendTarget updateTarget(String targetId, String host, String defaultServer, String key,
 			String secretKey) {
 		ZendTarget target = (ZendTarget) getTargetById(targetId);
 		if (target == null) {
 			log.info("Target with id '" + targetId + "' does not exist.");
 			return null;
 		}
 		try {
 			if (host != null) {
 				target.setHost(new URL(host));
 			}
 			if (defaultServer != null) {
 				target.setDefaultServerURL(new URL(defaultServer));
 			}
 			if (key != null) {
 				target.setKey(key);
 			}
 			if (secretKey != null) {
 				target.setSecretKey(secretKey);
 			}
 			if (!target.connect()) {
 				return null;
 			}
 			IZendTarget updated = loader.update(target);
 			if (updated != null) {
 				statusChanged(new BasicStatus(StatusCode.UNKNOWN, "updated target", "updated target"));
 			}
 			return updated;
 		} catch (MalformedURLException e) {
 			log.error(e);
 		} catch (WebApiException e) {
 			log.error("Error during updating Zend Target with id '" + targetId
 					+ "'");
 			log.error("\tPossible error: " + e.getMessage());
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
 			log.error(new IllegalArgumentException("Target cannot be null."));
 			return false;
 		}
 		if (target.getId() == null) {
 			log.error(new IllegalArgumentException(
 					"Target is not valid. Target id cannot be null."));
 			return false;
 		}
 		if (getTargetById(target.getId()) != null) {
 			log.error("Target with id '" + target.getId() + "' already exists.");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @return the default target id if exists. null if no default is assigned
 	 */
 	public synchronized String getDefaultTargetId() {
 		if (all.size() == 1) {
 			return all.get(0).getId();
 		}
 		return defaultId;
 	}
 	
 	/**
 	 * Creates new target id unique in target manager.
 	 * 
 	 * @param prefix Optional prefix for generated id. Might be null.
 	 * 
 	 * @return unique id.
 	 */
 	public String createUniqueId(String prefix) {
 		if (prefix == null) {
 			prefix = "";
 		}
 		
 		int idgenerator = getTargets().length;
 		String id;
 		do {
 			id = prefix + Integer.toString(idgenerator++);
 		} while (getTargetById(id) != null);
 		
 		return id;
 	}
 
 }
