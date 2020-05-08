 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.core.logging;
 
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.RienaStatus;
 import org.eclipse.riena.core.wire.Wire;
 
 /**
  * The {@code LoggerProvider} is responsible for tracking the {@code LoggerMill}
  * and retrieving logger instances.
  */
 public final class LoggerProvider {
 
	private volatile boolean loggerMillWired;
 	private final LoggerMill loggerMill = new LoggerMill();
 
 	private static final LoggerProvider INSTANCE = new LoggerProvider();
 
 	/**
 	 * Get the {@code LoggerProvider} singleton.
 	 * 
 	 * @return the one-and-only {@code LoggerProvider}
 	 */
 	public static LoggerProvider instance() {
 		return INSTANCE;
 	}
 
 	private LoggerProvider() {
 		// Singleton
 	}
 
 	/**
 	 * Get a logger for the given class.
 	 * 
 	 * @param clazz
 	 * @return a logger
 	 */
 	public Logger getLogger(final Class<?> clazz) {
 		return getLogger(clazz.getName());
 	}
 
 	/**
 	 * Get a logger with the given name.
 	 * 
 	 * @param name
 	 * @return a logger
 	 */
 	public Logger getLogger(final String name) {
 		initLoggerMill();
 		if (loggerMill.isReady()) {
 			return loggerMill.getLogger(name);
 		}
 		return DeferringLoggerFactory.createLogger(name, this);
 	}
 
 	/**
 	 * Get a real logger, i.e. not a deferring logger.
 	 * 
 	 * @param name
 	 * @return a real logger; otherwise null.
 	 */
 	Logger getRealLogger(final String name) {
 		if (!loggerMill.isReady()) {
 			return null;
 		}
 		return loggerMill.getLogger(name);
 	}
 
 	boolean hasReadyLoggerMill() {
 		return loggerMill.isReady();
 	}
 
 	/**
 	 * This has to be delayed until riena is active!! Otherwise this would cause
 	 * initialization exceptions.
 	 */
 	private void initLoggerMill() {
		if (loggerMillWired) {
 			return;
 		}
 		synchronized (loggerMill) {
			if (!loggerMillWired && RienaStatus.isActive()) {
 				Wire.instance(loggerMill).andStart();
				loggerMillWired = true;
 			}
 		}
 	}
 
 }
