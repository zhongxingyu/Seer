 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 /**
  * Log errors or informative messages to the Eclipse log. In future we may want
  * to register an {@link ILogListener} in order to create a custom log somewhere
  * for debugging purposes (~/eclipse-fedorapackager.log maybe).
  * 
  */
 public class FedoraPackagerLogger {
 	
 	/***/ public enum LogLevel {
 		/**
 		 * Show errors only in the log.
 		 */
 		ERROR,
 		/**
 		 * Show debug messages in the log.
 		 */
 		DEBUG
 	}
 
 	/**
 	 * Plug-in specific debug log status code.
 	 */
 	public static final int DEBUG_STATUS = 3;
 	/**
 	 * Plug-in specific error log status code.
 	 */
 	public static final int ERROR_STATUS = 1;
 
 	private ILog log;
 	private LogLevel currentLogLevel;
 	private static FedoraPackagerLogger instance;
 
 	private FedoraPackagerLogger() {
 		log = PackagerPlugin.getDefault().getLog();
 		setConfig();
 	}
 
 	/**
 	 * Get a FedoraPackagerLogger singleton.
 	 * 
 	 * @return The singleton instance.
 	 */
 	public static FedoraPackagerLogger getInstance() {
 		if (instance == null) {
 			instance = new FedoraPackagerLogger();
 		}
 		return instance;
 	}
 
 	/**
 	 * Logs errors.
 	 * 
 	 * @param message
 	 *            The human readable localized message.
 	 * @param throwable
 	 *            The exception which occurred.
 	 */
 	public void logError(String message, Throwable throwable) {
 		// Always log errors
 		log.log(new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID,
 				ERROR_STATUS, message, throwable));
 	}
	
	/**
	 * Logs errors.
	 * 
	 * @param message
	 *            A human readable localized message.
	 */
	public void logError(String message) {
		log.log(new Status(IStatus.ERROR, PackagerPlugin.PLUGIN_ID, message));
	}
 
 	/**
 	 * Logs informative debug messages. Messages are only logged if debugging
 	 * is turned on.
 	 * 
 	 * @param message
 	 *            A human readable localized message.
 	 */
 	public void logDebug(String message) {
 		if (currentLogLevel == LogLevel.DEBUG) {
 			log.log(new Status(IStatus.INFO, PackagerPlugin.PLUGIN_ID, message));
 		}
 	}
 
 	/**
 	 * Logs informative debug messages. Messages are only logged if debugging
 	 * is turned on.
 	 * 
 	 * @param message
 	 *            A human readable localized message.
 	 * @param reason
 	 *            The exception indicating what really happened.
 	 */
 	public void logDebug(String message, Throwable reason) {
 		if (currentLogLevel == LogLevel.DEBUG) {
 			log.log(new Status(IStatus.INFO, PackagerPlugin.PLUGIN_ID,
 					DEBUG_STATUS, message, reason));
 		}
 	}
 	
 	/**
 	 * Logs and info message to the error log.
 	 * 
 	 * @param message
 	 *            A human readable localized message.
 	 */
 	public void logInfo(String message) {
 		log.log(new Status(IStatus.INFO, PackagerPlugin.PLUGIN_ID, message));
 	}
 	
 	
 	/**
 	 * Refresh the log so that updated debug options are respected.
 	 */
 	public void refreshConfig() {
 		setConfig();
 	}
 
 	private void setConfig() {
 		// default to error log level. i.e. show only errors.
 		// Should we use DebugOptionsListener here?
 		if (isDebugEnabled()) {
 			currentLogLevel = LogLevel.DEBUG;
 		} else {
 			currentLogLevel = LogLevel.ERROR;
 		}
 	}
 	
 	private boolean isDebugEnabled() {
 		IPreferenceStore prefStore = PackagerPlugin.getDefault().getPreferenceStore();
 		boolean debugEnabled = prefStore.getBoolean(FedoraPackagerPreferencesConstants.PREF_DEBUG_MODE);
 		return (PackagerPlugin.inDebugMode() || debugEnabled);
 	}
 }
