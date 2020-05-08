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
 
 public class Log4jLogListener implements LogListener {
 
 	public void logged(LogEntry entry) {
		ExtendedLogEntry eEntry = (ExtendedLogEntry) entry;
 
		Logger logger = Logger.getLogger(eEntry.getLoggerName());
 
		logger.info(eEntry.getMessage());
 	}
 
 }
