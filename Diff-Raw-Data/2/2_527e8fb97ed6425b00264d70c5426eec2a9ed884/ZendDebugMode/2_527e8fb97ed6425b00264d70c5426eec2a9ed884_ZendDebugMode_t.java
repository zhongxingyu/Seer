 /*******************************************************************************
  * Copyright (c) Oct 2, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.application;
 
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.Map;
 
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.internal.application.ZendConnection;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.DebugMode;
 import org.zend.webapi.core.progress.BasicStatus;
 import org.zend.webapi.core.progress.StatusCode;
 
 /**
  * Utility class which provides methods to start and stop debug mode.
  * 
  * @author Wojciech Galanciak, 2012
  */
 public class ZendDebugMode extends ZendConnection {
 
 	public enum State {
 		STARTING,
 
 		STOPPING,
 
 		STARTED,
 
 		STOPPED,
 
 		ERROR
 	}
 
 	private String targetId;
 	private String[] filters;
 	private Map<String, String> options;
 
 	public ZendDebugMode(String targetId) {
 		super();
 		this.targetId = targetId;
 	}
 
 	public ZendDebugMode(String targetId, IMappingLoader mappingLoader) {
 		super(mappingLoader);
 		this.targetId = targetId;
 	}
 
 	public ZendDebugMode(String targetId, ITargetLoader loader) {
 		super(loader);
 		this.targetId = targetId;
 	}
 
 	public ZendDebugMode(String targetId, ITargetLoader loader,
 			IMappingLoader mappingLoader) {
 		super(loader, mappingLoader);
 		this.targetId = targetId;
 	}
 
 	public void setFilters(String[] filters) {
 		this.filters = filters;
 	}
 
 	public void setOptions(Map<String, String> options) {
 		this.options = options;
 	}
 
 	/**
 	 * Start debug mode on particular Zend Server.
 	 * 
 	 * @return
 	 * @throws WebApiException
 	 */
 	public State start() throws SdkException {
 		try {
 			WebApiClient client = getClient(targetId);
 			if (isStarted()) {
 				return State.STARTED;
 			} else {
 				DebugMode result = client
 						.studioStartDebugMode(filters, options);
 				if (result.getResult() == 1) {
 					return State.STARTING;
 				} else {
 					return State.ERROR;
 				}
 			}
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during starting debug mode for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Starting Debug Mode", message, e));
 			log.error(e);
 		} catch (WebApiException e) {
			throw new SdkException(e);
 		}
 		return State.ERROR;
 	}
 
 	/**
 	 * Stop debug mode on particular Zend Server.
 	 * 
 	 * @return
 	 * @throws WebApiException
 	 */
 	public State stop() throws SdkException {
 		try {
 			WebApiClient client = getClient(targetId);
 			if (!isStarted()) {
 				return State.STOPPED;
 			} else {
 				DebugMode result = client.studioStopDebugMode();
 				if (result.getResult() == 0) {
 					return State.STOPPING;
 				} else {
 					return State.ERROR;
 				}
 			}
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during stopping debug mode for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Stopping Debug Mode", message, e));
 			log.error(e);
 		} catch (WebApiException e) {
 			new SdkException(e);
 		}
 		return State.ERROR;
 	}
 
 	public boolean isStarted() throws SdkException {
 		try {
 			WebApiClient client = getClient(targetId);
 			DebugMode isStarted = client.studioIsDebugModeEnabled();
 			if (isStarted.getResult() == 0) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during checking debug mode state for '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Checking Debug Mode State", message, e));
 			log.error(e);
 		} catch (WebApiException e) {
 			new SdkException(e);
 		}
 		return false;
 	}
 
 }
