 /*******************************************************************************
  * Copyright (c) Feb 17, 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.application;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.List;
 
 import org.zend.sdklib.internal.application.ZendConnection;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.CodeTrace;
 import org.zend.webapi.core.connection.data.CodeTraceFile;
 import org.zend.webapi.core.connection.data.CodeTracingList;
 import org.zend.webapi.core.connection.data.CodeTracingStatus;
 import org.zend.webapi.core.connection.data.CodeTracingStatus.State;
 import org.zend.webapi.core.progress.BasicStatus;
 import org.zend.webapi.core.progress.StatusCode;
 
 /**
  * Utility class which provides methods to perform operation related to code
  * tracing feature provided by Zend Server.
  * 
  * @author Wojciech Galanciak, 2012
  */
 public class ZendCodeTracing extends ZendConnection {
 
 	private String targetId;
 	private int offset;
 
 	public ZendCodeTracing(String targetId) {
 		super();
 		this.targetId = targetId;
 	}
 
 	public ZendCodeTracing(String targetId, IMappingLoader mappingLoader) {
 		super(mappingLoader);
 		this.targetId = targetId;
 	}
 
 	public ZendCodeTracing(String targetId, ITargetLoader loader) {
 		super(loader);
 		this.targetId = targetId;
 	}
 
 	public ZendCodeTracing(String targetId, ITargetLoader loader,
 			IMappingLoader mappingLoader) {
 		super(loader, mappingLoader);
 		this.targetId = targetId;
 	}
 
 	/**
 	 * Enables code tracing feature on Zend Server.
 	 * 
 	 * @return code tracing status
 	 */
 	public CodeTracingStatus enable() {
 		return enable(false);
 	}
 
 	/**
 	 * Enables code tracing feature on Zend Server.
 	 * 
 	 * @param restartPhp
 	 *            restart php during operation
 	 * @return code tracing status
 	 */
 	public CodeTracingStatus enable(boolean restartPhp) {
 		try {
 			return getClient(targetId).codeTracingEnable(restartPhp);
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during enabling code tracing for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Enabling Code Tracing", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during enabling code tracing for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Enabling Code Tracing", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Disables code tracing feature on Zend Server.
 	 * 
 	 * @return code tracing status
 	 */
 	public CodeTracingStatus disable() {
 		return disable(false);
 	}
 
 	/**
 	 * Disables code tracing feature on Zend Server.
 	 * 
 	 * @param restartPhp
 	 *            restart php during operation
 	 * @return code tracing status
 	 */
 	public CodeTracingStatus disable(boolean restartPhp) {
 		try {
 			return getClient(targetId).codeTracingDisable(restartPhp);
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during disabling code tracing for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Disabling Code Tracing", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during disabling code tracing for '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Disabling Code Tracing", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides status of code tracing feature.
 	 * 
 	 * @return <code>true</code> if code tracing is enabled; <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isEnabled() {
 		try {
 			CodeTracingStatus status = getClient(targetId)
 					.codeTracingIsEnabled();
 			return status.getTraceEnabled() == State.ON ? true : false;
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during checking code tracing state for '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Checking Code Tracing State", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during checking code tracing state for '{0}'",
 					targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Checking Code Tracing State", message, e));
 			log.error(e);
 		}
 		return false;
 	}
 
 	/**
 	 * Creates code trace for provided URL.
 	 * 
	 * @param appId
	 *            application id
 	 * @param url
 	 *            for which code trace should be generated
 	 * @return created code trace
 	 */
	public CodeTrace createTrace(String appId, URL url) {
 		try {
 			return getClient(targetId).codeTracingCreate(url.toString());
 		} catch (WebApiException e) {
 			String message = MessageFormat
					.format("Error during creating code tracing for '{0}' application on '{1}', url = {2}",
							appId, targetId, url.toString());
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Creating Code Tracing", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat
					.format("Error during creating code tracing for '{0}' application on '{1}', url = {2}",
							appId, targetId, url.toString());
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Creating Code Tracing", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Deletes code trace with specified id.
 	 * 
 	 * @param id
 	 *            code trace id
 	 * @return deleted code trace
 	 */
 	public CodeTrace deleteTrace(String id) {
 		try {
 			return getClient(targetId).codeTracingDelete(id);
 		} catch (WebApiException e) {
 			String message = MessageFormat
 					.format("Error during deleting code tracing with id = {0}, on '{1}' target",
 							id, targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Deleting Code Tracing", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat
 					.format("Error during deleting code tracing with id = {0}, on '{1}' target",
 							id, targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Deleting Code Tracing", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides list of code traces for this application. If passed argument is
 	 * <code>true</code>, first call of this method returns all code traces
 	 * generated for concrete application and each next call returns only code
 	 * traces generated after last call. If argument is <code>false</code>, it
 	 * provides all code traces which are available (without remembering the
 	 * offset).
 	 * 
 	 * @param all
 	 *            if it is <code>true</code> all code traces are returned,
 	 *            otherwise only those which were created after last call of
 	 *            this method
 	 * @param appId
 	 *            application(s) id, if not provided then returns traces for all
 	 *            applications
 	 * @return list of code traces
 	 */
 	public List<CodeTrace> getTraces(boolean all, String... appId) {
 		try {
 			CodeTracingList list = null;
 			if (all) {
 				list = getClient(targetId).codeTracingList(null, null, null,
 						null, appId);
 			} else {
 				list = offset != 0 ? getClient(targetId).codeTracingList(null,
 						offset, null, null, appId) : getClient(targetId)
 						.codeTracingList(null, offset, null, null, appId);
 				offset += list.getTraces().size();
 			}
 			return list.getTraces();
 		} catch (WebApiException e) {
 			String message = MessageFormat
 					.format("Error during retrieving code traces for '{0}' application(s) on '{1}'",
 							Arrays.toString(appId), targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Code Traces", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat
 					.format("Error during retrieving code traces for '{0}' application(s) on '{1}'",
 							Arrays.toString(appId), targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Retrieving Code Traces", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Provides code trace file for specified trace id.
 	 * 
 	 * @param traceId
 	 * @return code trace file
 	 */
 	public File get(String traceId) {
 		return get(traceId, getTempFolder());
 	}
 
 	/**
 	 * Provides code trace file for specified trace id.
 	 * 
 	 * @param traceId
 	 * @param destination
 	 *            location where code trace file will be stored
 	 * @return code trace file
 	 */
 	public File get(String traceId, File destination) {
 		try {
 			CodeTraceFile codeTraceFile = getClient(targetId)
 					.codeTracingDownloadTraceFile(traceId);
 			File file = new File(destination, codeTraceFile.getFilename());
 			if (file.exists()) {
 				file.delete();
 			}
 			file.createNewFile();
 			FileOutputStream stream = new FileOutputStream(file);
 			stream.write(codeTraceFile.getFileContent());
 			closeStream(stream);
 			if (file.length() == codeTraceFile.getFileSize()) {
 				return file;
 			} else {
 				log.error("Invalid file size.");
 			}
 		} catch (WebApiException e) {
 			String message = MessageFormat.format(
 					"Error during downloading code trace from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Downloading Code Trace", message, e));
 			log.error(message + "':");
 			log.error("\tpossible error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			String message = MessageFormat.format(
 					"Error during downloading code trace from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Downloading Code Trace", message, e));
 			log.error(e);
 		} catch (IOException e) {
 			String message = MessageFormat.format(
 					"Error during downloading code trace from '{0}'", targetId);
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Downloading Code Trace", message, e));
 			log.error(e);
 		}
 		return null;
 	}
 
 }
