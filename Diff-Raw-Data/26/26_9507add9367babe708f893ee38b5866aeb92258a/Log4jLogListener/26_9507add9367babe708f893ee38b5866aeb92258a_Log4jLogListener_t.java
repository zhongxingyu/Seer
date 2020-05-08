 /*******************************************************************************
  * Copyright (c) 2007 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.logging;
 
 import org.apache.log4j.Logger;
 import org.eclipse.equinox.log.ExtendedLogEntry;
 import org.osgi.service.log.LogEntry;
 import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
 
 public class Log4jLogListener implements LogListener {
 
 	public void logged(LogEntry entry) {
		ExtendedLogEntry extendedEntry = (ExtendedLogEntry) entry;
		String loggerName = extendedEntry.getLoggerName();
		Logger logger = Logger.getLogger(loggerName != null ? loggerName : "<unknown-logger-name>"); //$NON-NLS-1$
 
		switch (extendedEntry.getLevel()) {
		case LogService.LOG_DEBUG:
			logger.debug(extendedEntry.getMessage(), extendedEntry.getException());
			break;
		case LogService.LOG_WARNING:
			logger.warn(extendedEntry.getMessage(), extendedEntry.getException());
			break;
		case LogService.LOG_ERROR:
			logger.error(extendedEntry.getMessage(), extendedEntry.getException());
			break;
		case LogService.LOG_INFO:
			logger.info(extendedEntry.getMessage(), extendedEntry.getException());
			break;
		default:
			break;
		}
 
 	}
 
 }
